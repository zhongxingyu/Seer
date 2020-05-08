 package se.chalmers.kangaroo.io;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Writer;
 import java.util.Scanner;
 
 /**
  * This is a class that manages the highscore in a file.
  * 
  * @author alburgh
  * 
  */
 public class Highscore {
 	private static Highscore highscore;
 	private static final String FILE_NAME = "resources/highscore.txt";
 	private static final int nbrOfScores = 10;
 	
 	private Highscore() {
 	}
 
 	/**
 	 * Returns the only instance of this class.
 	 */
 	public static synchronized Highscore getInstance() {
 		if (highscore == null)
 			highscore = new Highscore();
 		return highscore;
 	}
 
 	/**
 	 * When a level is finished this should be called. Will automatically add
 	 * the player to the highscore if the time is low enough.
 	 * 
 	 * @param playerName
 	 *            , the name appearing on the highscore
 	 * @param level
 	 *            , the number of the level
 	 * @param time
 	 *            , the time in millis
 	 */
	public void setHighscore(String playerName, int level, int time, int nbrOfDeaths){
 		int[] times = getTimes(level);
 		String[] names = getNames(level);
 		int[] deaths = getDeaths(level);
 
 		if (times[nbrOfScores-1] > time) {
 			times[nbrOfScores-1] = time;
 			names[nbrOfScores-1] = playerName;
			deaths[nbrOfScores-1] = nbrOfDeaths;
 		}
 		/*
 		 * Since it already should be sorted I only need to make one bubbleSort
 		 * move
 		 */
 		for (int i = times.length - 1; i > 0; i--) {
 			if (times[i] < times[i - 1]) {
 				int tmpTime = times[i];
 				times[i] = times[i - 1];
 				times[i - 1] = tmpTime;
 
 				String tmpName = names[i];
 				names[i] = names[i - 1];
 				names[i - 1] = tmpName;
 				
 				int tmpDeath = deaths[i];
 				deaths[i] = deaths[i-1];
 				deaths[i-1] = tmpDeath;
 			}
 		}
 		StringBuilder sb = new StringBuilder();
 		try{
 		InputStream in = new FileInputStream(FILE_NAME);
 		Scanner sc = new Scanner(in);
 		boolean jumpToLevel = true;
 		while(sc.hasNext() && jumpToLevel){
 			String tmp = sc.nextLine();
 			sb.append(tmp +"\n");
 			if(tmp.equals("level"+level))
 				jumpToLevel = false;
 		}
 		for(int i = 0; i < names.length; i++){
 			sb.append(names[i]+ " "+ times[i] + " "+ deaths[i]+ " ");
 		}
 		jumpToLevel = true;
 		while(sc.hasNext() && jumpToLevel)
 			jumpToLevel = !sc.nextLine().equals("level"+(level+1));
 		sb.append("\nlevel"+(level+1));
 		while(sc.hasNext())
 			sb.append("\n"+sc.nextLine());
 		sc.close();
 		in.close();
 
 		/* Write the modified String to the file*/
 		Writer w = new FileWriter(FILE_NAME);
 		w.write(sb.toString());
 		w.close();
 		}catch(IOException io){
 			
 		}
 
 	}
 	/**
 	 * Returns a string array consisting of the player names in order. If the
 	 * file can't be found, an empty array will be returned.
 	 * 
 	 * @param level
 	 *            , the number of the level
 	 * @return all the names.
 	 * @throws IOException 
 	 */
 	public String[] getNames(int level){
 		String[] names = new String[nbrOfScores];
 		try {
 			InputStream in = new FileInputStream("resources/highscore.txt");
 			Scanner sc = new Scanner(in);
 			while (!sc.nextLine().equals("level" + level)) {
 				// Loop through all the other rows
 			}
 			for (int i = 0; i < names.length; i++) {
 				names[i] = sc.next();
 				sc.next(); // Will skip the time
 				sc.next(); // Will skip the deaths
 			}
 			sc.close();
 			in.close();
 		} catch (IOException e){
 			System.out.println("Something bad happened in io!");
 			e.printStackTrace();
 		}
 		
 		return names;
 	}
 
 	public int[] getDeaths(int level){
 		int[] deaths = new int[nbrOfScores];
 		try {
 			InputStream in = new FileInputStream("resources/highscore.txt");
 			Scanner sc = new Scanner(in);
 			while (!sc.nextLine().equals("level" + level)) {
 				// Loop through all the other rows
 			}
 			for (int i = 0; i < deaths.length; i++) {
 				sc.next(); // Will skip the name
 				sc.next(); // Will skip the time
 				deaths[i] = Integer.parseInt(sc.next());
 			}
 			sc.close();
 			in.close();
 		} catch (IOException e){
 			System.out.println("Something bad happened in io!");
 			e.printStackTrace();
 		}
 		return deaths;
 	}
 	
 	/**
 	 * Returns a int array consisting of the player times in order. Will return
 	 * an empty array if the file is not found.
 	 * 
 	 * @param level
 	 *            , the number of the level
 	 * @return all the times
 	 */
 	public int[] getTimes(int level){
 		int[] times = new int[nbrOfScores];
 		try {
 			InputStream in = new FileInputStream("resources/highscore.txt");
 			Scanner sc = new Scanner(in);
 			while (!sc.nextLine().equals("level" + level)) {
 				// Loop through all the other rows
 			}
 			for (int i = 0; i < times.length; i++) {
 				sc.next(); // Will skip the names
 				times[i] = Integer.parseInt(sc.next());
 				sc.next(); // Will skip the deaths
 			}
 			sc.close();
 			in.close();
 		} catch (FileNotFoundException e) {
 			System.out.println("Could't find the specific file.");
 		} catch (NumberFormatException e) {
 			System.out.println("The times are not numbers.");
 		} catch (IOException e){
 			System.out.println("Something bad happened in io!");
 			e.printStackTrace();
 		}
 		return times;
 	}
 	/**
 	 * Will return true if the time is low enough to be on the highscore.
 	 * If this returns true the setHighscore() method will do something. 
 	 * @param level, the number of the level
 	 * @param time, the time in millis
 	 * @return true if the time is good enough for the highscore
 	 */
 	public boolean willMakeHighscore(int level, int time){
 		int[] times = new int[nbrOfScores];
 		try {
 			InputStream in = new FileInputStream("resources/highscore.txt");
 			Scanner sc = new Scanner(in);
 			while (!sc.nextLine().equals("level" + level)) {
 				// Loop through all the other rows
 			}
 			for (int i = 0; i < times.length; i++) {
 				sc.next(); // Will skip the names
 				times[i] = Integer.parseInt(sc.next());
 			}
 			sc.close();
 			in.close();
 		} catch (FileNotFoundException e) {
 			System.out.println("Could't find the specific file.");
 		} catch (NumberFormatException e) {
 			System.out.println("The times are not numbers.");
 		} catch (IOException e){
 			System.out.println("Something bad happened in io!");
 			e.printStackTrace();
 		}
 		return time < times[nbrOfScores-1];
 	}
 }
