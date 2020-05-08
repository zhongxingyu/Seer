 package se.chalmers.dat255.risk.model;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.Scanner;
 
 import com.badlogic.gdx.Gdx;
 
 /**
  * Contains Maps with relations for the provinces on the game board and the
  * players controlling them.
  * 
  * Has methods for checking and dealing out ownership to territories.
  */
 
 public class WorldMap {
 
 	private final ArrayList<IProvince> allProvinces;
 
 	ArrayList<String> continent = new ArrayList<String>();
 	private HashMap<String, Player> ownership;
 	int[] bonuses; // Each id, corrseponds to a players continental bonus
 	ArrayList<Continent> continents; // All the continents that gives bonuses
 	// neighbours maps together each territory with all adjacent territories.
 	// It gets its information via the class constructor, which in turn reads
 	// all information
 	// from a text file.
 	private final HashMap<String, ArrayList<String>> neighbours;
 
 	public WorldMap(File provinceFile, File continentFile, Player[] players) {
 
 		HashMap<String, ArrayList<String>> tempNeighbours = new HashMap<String, ArrayList<String>>();
 		ArrayList<String> listOfProvinces = new ArrayList<String>();
 		ownership = new HashMap<String, Player>();
		bonuses=new int[players.length];
 		try {
 			Scanner scanner = new Scanner(provinceFile);
 			while (scanner.hasNextLine()) {
 				String line = scanner.nextLine();
 				String[] array = line.split("-");
 				String p1 = array[0];
 				listOfProvinces.add(p1);
 				ArrayList<String> list = new ArrayList<String>();
 				for (int i = 1; i < array.length; i++) {
 					list.add(array[i]);
 				}
 				tempNeighbours.put(p1, list);
 
 			}
 			continents = new ArrayList<Continent>();
 			scanner = new Scanner(continentFile);
 			String itsProvinces[];
 			while (scanner.hasNextLine()) {
 				String line = scanner.nextLine();
 				String[] array = line.split("-");
 				itsProvinces = new String[array.length - 2]; // Reserves space
 																// for all but
 																// bonus and the
 																// continent
 																// name
 
 				int nrOfContinents = 0;
 
 				for (int i = 2; i < array.length; i++) {
 					itsProvinces[i - 2] = array[i];
 				}
 				continents.add(new Continent(array[0], itsProvinces, Integer
 						.parseInt(array[0])));
 			}
 
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 
 		allProvinces = buildProvinces(listOfProvinces);
 		randomizeProvinces(listOfProvinces, players);
 		for (IProvince province : allProvinces) {
 			province.setColor(PlayerColor.getStringColor(this.getOwner(
 					province.getId()).getId()));
 		}
 
 		neighbours = new HashMap<String, ArrayList<String>>(tempNeighbours);
 	}
 
 	/**
 	 * Returns ownership of a certain territory.
 	 * 
 	 * @param A
 	 *            province name sent to the method
 	 * @return The owner of the province sent to the method
 	 */
 	public Player getOwner(String provinceName) {
 		return ownership.get(provinceName);
 	}
 
 	/**
 	 * Changes the ownership of a certain territory. Also changes the number of
 	 * provinces that the players involved controls.
 	 * 
 	 * @param Name
 	 *            of the province that will change owner.
 	 * @param Which
 	 *            player the ownership should change to.
 	 */
 
 	public void changeOwner(String provinceName, Player player) {
 		Player oldPlayer = ownership.get(provinceName);
 		oldPlayer.loseProvince();
 
 		ownership.put(provinceName, player);
 		player.gainProvince();
 		//
 	}
 
 	/**
 	 * Checks if two territories are adjacent.
 	 * 
 	 * @param provinceName1
 	 * @param provinceName2
 	 * @return True if the territories are next to each other.
 	 */
 	public boolean isNeighbours(String provinceName1, String provinceName2) {
 		ArrayList<String> list = neighbours.get(provinceName1);
 		if (list.contains(provinceName2)) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Method to deal random provinces to the players at the start of a game.
 	 * Every time a province is given to a player, it is removed from the list
 	 * of provinces.
 	 * 
 	 * @param List
 	 *            of all provinces
 	 * @param List
 	 *            of all players
 	 */
 	private void randomizeProvinces(ArrayList<String> provinceList,
 			Player[] players) {
 		ArrayList<String> temp = provinceList;
 		int nrOfPlayers = players.length, nrOfProvinces = provinceList.size();
 
 		Random randGen = new Random();
 		while (!temp.isEmpty()) {
 			for (Player player : players) {
 				if (nrOfProvinces > 0)
 					ownership.put(temp.remove(randGen.nextInt(nrOfProvinces)),
 							player);
 				nrOfProvinces--;
 			}
 		}
 	}
 
 	/**
 	 * Builds a list of Province objects from a list of province names
 	 * 
 	 * @param List
 	 *            of all province names
 	 * @return List of all province objects on the map
 	 */
 
 	private ArrayList<IProvince> buildProvinces(ArrayList<String> nameList) {
 		ArrayList<IProvince> provinceList = new ArrayList<IProvince>();
 		for (String s : nameList) {
 			provinceList.add(new Province(s));
 		}
 		return provinceList;
 	}
 
 	public int getBonus(Player player) {
 		return bonuses[player.getId()];
 	}
 
 	public void updateBonus(Continent updateContinent) {
 		updateContinent.update();
 		int continentBonus;
 		for (int i = 0; i < bonuses.length; i++)
 			bonuses[i] = 0;
 
 		for (Continent continent : continents) {
 			continentBonus = continent.getBonus();
 			if (continent.getContinentOwner() != null)
 				bonuses[continent.getContinentOwner().getId()] = +continentBonus;
 		}
 	}
 
 	public ArrayList<IProvince> getProvinces() {
 		return allProvinces;
 	}
 
 	/**
 	 * Class for representing continents. Contains the name of the continent,
 	 * all provinces in the continent, how many bonus points the continent gives
 	 * and who currently owns the continent.
 	 * 
 	 * Contains method for getting the bonus and the owner, and for updating who
 	 * currently owns the continent.
 	 * 
 	 */
 	private class Continent {
 		String continentName;
 		String[] provinces;
 		int bonus;
 		Player owner = null;
 
 		public Continent(String continentName, String[] provinces, int bonus) {
 			this.continentName = continentName;
 			this.provinces = provinces;
 			this.bonus = bonus;
 		}
 
 		public int getBonus() {
 			return bonus;
 		}
 
 		public Player getContinentOwner() {
 			return owner;
 		}
 
 		/**
 		 * Updates who owns the Continent. Steps through the list of provinces
 		 * in the continent and sees if the same person owns all of them. Runs
 		 * when someone takes a province from someone else.
 		 */
 		public void update() {
 			Player tempProvinceOwner = getOwner(provinces[0]);
 			for (String province : provinces) {
 				if (tempProvinceOwner != getOwner(province)) {
 					owner = null;
 					return;
 				}
 				tempProvinceOwner = getOwner(province);
 			}
 			owner = tempProvinceOwner;
 		}
 
 	}
 }
