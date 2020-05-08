 package div;
 
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 /**
  * Klassen handterer registrering, sletting og sok av sauer.
  * 
  * @author Ragnhild
  * 
  */
 public class SheepRegistration implements Serializable {
 
 	private ArrayList<Sheep> sheepList;
 
 	public SheepRegistration() {
 
 	}
 
 	/**
 	 * Registrerer en sau i databasen med navn, fodselsar, vekt, kjonn, eier og
 	 * gjeter.
 	 */
 	public int registerSheep(String name, int birthyear, int weight,
 			char gender, String owner, String shepherd, String latitude,
 			String longitude) throws Exception {
 
 		// Lager en foresp0rsel til server.
 		// retiningslinjer for kommunikasjon med server vil til en hver tid
 		// ligge i server.ComProtocol klassen
 		String query = name + "||" + owner + "||" + shepherd + "||" + gender
 				+ "||" + weight + "||" + 75 + "||" + 39 + "||" + birthyear + "||" + latitude + "," + longitude;
 
 		// sender foresp0rselen til serveren og faar tilbake respons
 		Object serverRespons = ClientConnection.sendObjectQuery(
 				"registersheep", query);
 
 		int id = 0;
 		// sjekke at melding ble mottatt
 		if (serverRespons instanceof Integer) {
 			id = (Integer) serverRespons;
 		} else if (serverRespons instanceof String) {
 			throw new Exception((String) serverRespons);
 		}
 
 		String timeStamp = new SimpleDateFormat("yyyy/MM/dd/HH-mm-ss")
 				.format(Calendar.getInstance().getTime());
 		Sheep tempSheep = new Sheep(id, name, birthyear, weight, gender, owner,
 				shepherd);
 		tempSheep.newLocation(latitude + "," + longitude, timeStamp);
 		sheepList.add(tempSheep);
 
 		return id;
 
 	}
 
 	/**
	 * Endrer en sau sine data. Kan endre navn, gjeter, kjnn og fdselsr.
 	 */
 	public void editSheep(int id, String name, String owner, String shepherd,
 			char gender, int weight, int birthyear) throws Exception {
 		Sheep s = sheepSearch(id);
 		s.setName(name);
 		s.setShepherd(shepherd);
 		s.setGender(gender);
 		s.setWeight(weight);
 		s.setBirthyear(birthyear);
 		String query = id + "||" + name + "||" + owner + "||" + shepherd + "||"
 				+ gender + "||" + weight + "||" + birthyear;
 		String serverRespons = ClientConnection.sendServerQuery("changesheep",
 				query);
 		if (!serverRespons.equals("changesheep success")) {
 			System.out.println(serverRespons);
 			System.out.println("Error. Can't change sheep information");
 		}
 
 	}
 
 	/**
 	 * Seltter en sau fra databasen. Returnerer true dersom alt gikk bra, false
 	 * ellers.
 	 */
 	public boolean deleteSheep(Sheep s) {
 		String query = "" + s.getId();
 
 		String serverRespons = ClientConnection.sendServerQuery("delsheep",
 				query);
 
 		if (serverRespons.equals("err")) {
 			System.out.println("Error. Can't delete sheep");
 			return false;
 		}
 
 		return sheepList.remove(s);
 	}
 
 	/**
 	 * Soker og returnerer sauen med en viss id.
 	 */
 	public Sheep sheepSearch(int id) {
 		for (Sheep s : sheepList) {
 			if (s.getId() == id) {
 				return s;
 			}
 		}
 		return null;
 	}
 
 	/**
	 * Finner en sau i databasen med en gitt eier og id p sau.
 	 */
 	public Sheep findSheep(String user, String id) throws Exception {
 		String query = user + "||" + id;
 
 		Object serverRespons = ClientConnection.sendObjectQuery("findsheep",
 				query);
 
 		if (serverRespons instanceof Sheep) {
 			return (Sheep) serverRespons;
 		} else if (serverRespons instanceof String) {
 			if (serverRespons.equals("findsheep no login")) {
 				throw new Exception("Not logged in");
 			} else if (serverRespons.equals("findsheep null input")) {
 				throw new Exception("Null input");
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Fyller opp sheepList med sauer fra databasen.
 	 */
 	public void updateSheepList(String user) throws Exception {
 		String query = user;
 
 		Object serverRespons = ClientConnection.sendObjectQuery(
 				"getownedsheep", query);
 
 		if (serverRespons instanceof ArrayList) {
 			this.sheepList = (ArrayList<Sheep>) serverRespons;
 		} else if (serverRespons instanceof String) {
 			if (serverRespons.equals("getownedsheep no login")) {
 				throw new Exception("Not logged in");
 			} else if (serverRespons.equals("getownedsheep null input")) {
 				throw new Exception("Null input");
 			}
 		}
 	}
 
 	public ArrayList<Sheep> getSheepList() {
 		return sheepList;
 	}
 
 	public ArrayList<Sheep> getAttackedSheepList(String user) {
 		String query = user;
 		Object serverRespons = ClientConnection.sendObjectQuery(
 				"getattackedsheeplist", query);
 		if (serverRespons instanceof ArrayList) {
 			return (ArrayList<Sheep>) serverRespons;
 		} else if (serverRespons instanceof String) {
 			System.out.println("Error. Can't get list of attacked sheep. ");
 		}
 		return null;
 
 	}
 
 	/**
 	 * Sender informasjon om angrep mot en sau til server.
 	 */
 	public void attackSheep(int id, String user) {
		String query = "" + id + "||" + user;
 		String serverRespons = ClientConnection.sendServerQuery("attacksheep",
 				query);
 		if (!serverRespons.equals("attacksheep success")) {
 			System.out.println("Error. Can't add attack information");
 		}
 	}
 
 	public ArrayList<SheepLocation> getLastLocations(int id) throws Exception {
 		String query = "" + id + "||" + 5;
		// Fr tilbake et Sheep-objekt
 		Object serverRespons = ClientConnection.sendObjectQuery("getsheeplog",
 				query);
 		if (serverRespons instanceof Sheep) {
 			// ((Sheep) serverRespons).getLocationLog();
 			ArrayList<SheepLocation> ret = new ArrayList<SheepLocation>();
 			for (SheepLocation s : ((Sheep) serverRespons).getLastLocations()) {
 				System.out.println(s);
 				ret.add(s);
 			}
 			return ret;
 		}
 		System.out.println(serverRespons);
 		throw new Exception("Error. Can't get last locations");
 
 	}
 
 }
