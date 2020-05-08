 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class Stat {
 
 	// The name of the file the results are saved in;
 	private static final String FILENAME = "top5.txt";
 	
 	private static final int PLAYER_NUMBER = 5;
 	private static final String EMPTY = "empty 0";
 
 	/**
 	 * This list should contain top 5 pleyers names and their scores stored in a
 	 * saved text file
 	 * 
 	 * @return: top5 list
 	 * */
 	public static ArrayList<String> getTop5List() {
 		// TODO: implement bufferedReader and write results to ArrayList
 		try {
 			BufferedReader rd = new BufferedReader(new FileReader(FILENAME));
 			ArrayList<String> arr = new ArrayList<String>();
 			
 			for(int i=0; i<PLAYER_NUMBER; i++){
 				String tmp = rd.readLine();
 				if(tmp != null){
 					arr.add(tmp);
 				}
 				else{
 					arr.add(EMPTY);
 				}
 			}
 			rd.close();
 			return arr;
 			
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * Checking the result of a player, if it is worthy to be in a top 5 players
 	 * list
 	 * 
 	 * @param: player's score
 	 * @param: players's name
 	 * */
 	public static void save(int score, String name) {
 		if (isWorthy(score)) {
 			setInTop5(score, name);
 		}
 	}
 
 	/**
 	 * @param: players score
 	 * @return: true if the player is worthy to be in a top5 list
 	 * */
 	private static boolean isWorthy(int score) {
 		// TODO: implement top 5 check;
 		ArrayList<String> top5 = getTop5List();
		for(int i=PLAYER_NUMBER-1; i>=0; i--){
 			String tmp = top5.get(i);
			int tmpScore = Integer.parseInt(tmp.substring(tmp.indexOf(' ')+1));
 			if(score > tmpScore){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Set the player in a top 5 list
 	 * 
 	 * @param: player's score
 	 * @param: players's name
 	 * */
 	private static void setInTop5(int score, String name) {
 		// TODO: implement a bufferedWriter to save results in the text file;
 		try {
 			ArrayList<String> top5 = getTop5List();
			BufferedWriter br = new BufferedWriter(new FileWriter(FILENAME));
 			for(int i=0; i<PLAYER_NUMBER; i++){
 				String tmp = top5.get(i);
				int tmpScore = Integer.parseInt(tmp.substring(tmp.indexOf(' ')+1));
 				if(score > tmpScore){
 					top5.add(i, name + " " + score);
 					top5.remove(top5.size()-1);
					break;
 				}
 			}
 			for(int i=0; i<PLAYER_NUMBER; i++){
 				br.write(top5.get(i));
 				br.newLine();
 			}
 			br.flush();
 			br.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
