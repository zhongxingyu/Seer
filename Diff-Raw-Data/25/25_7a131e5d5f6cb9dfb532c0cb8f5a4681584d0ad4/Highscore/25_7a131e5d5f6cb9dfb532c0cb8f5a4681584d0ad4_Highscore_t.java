 package se.chalmers.kangaroo.io;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Scanner;
 
 import org.keyczar.Crypter;
 import org.keyczar.exceptions.KeyczarException;
 
 /**
  * This is a class that manages the highscore in a file.
  * 
  * @author alburgh
  * 
  */
 public class Highscore {
 	private static Highscore highscore;
 	private static final String FILE_NAME = "resources/highscore.txt";
 	private static final int nbrOfScores = 5;
 	private Crypter crypter;
 	private String encText;
 	private Highscore() {
 		try {
			this.crypter = new Crypter("resources/keyset");
 		} catch(Exception e) {
 			System.out.println("Something went horribly wrong!");
 			System.out.println(e.toString());
 		}
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
 	public void setHighscore(String playerName, int level, int time) throws IOException {
 		int[] times = getTimes(level);
 		String[] names = getNames(level);
 
 		if (times[nbrOfScores-1] > time) {
 			times[nbrOfScores-1] = time;
 			names[nbrOfScores-1] = playerName;
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
 			}
 		}
 		StringBuilder sb = new StringBuilder();
 
 		InputStream in = new FileInputStream(FILE_NAME);
 		Scanner sc = new Scanner(in);
 		while(sc.hasNext())
 			sb.append(sc.nextLine() +"\n");
 		sc.close();
 		
 		int start = sb.indexOf(names[0]+" "+times[0]);
 		int stop = sb.indexOf(names[nbrOfScores-1]+ " "+times[nbrOfScores-1-1])-names[nbrOfScores-1].length()-((Integer)times[nbrOfScores-1]).toString().length()-1;
 		StringBuilder str = new StringBuilder();
 		for(int i = 0; i < names.length; i++){
 			str.append(names[i]+" "+times[i]);
 		}
 		sb.replace(start, stop, str.toString());
 		/* Write the modified String to the file*/
 		FileWriter fw = new FileWriter(FILE_NAME);
 		BufferedWriter out = new BufferedWriter(fw);
 		try {
 			encText = crypter.encrypt(sb.toString());
 		} catch (KeyczarException e) {
 			e.toString();
 		}
 		out.write(encText);
 
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
 			byte[] b = fileToByteArray(new File(FILE_NAME));
 			byte[] decryptedByteArray = crypter.decrypt(b);
 			Scanner sc = new Scanner(byteArrayToFile(decryptedByteArray));
 			while (!sc.nextLine().equals("level" + level)) {
 				// Loop through all the other rows
 			}
 			for (int i = 0; i < names.length; i++) {
 				names[i] = sc.next();
 				sc.next(); // Will skip the time
 			}
 		} catch (KeyczarException e) {
 			System.out.println("Something bad happened!");
 			e.printStackTrace();
 		} catch (IOException e){
 			System.out.println("Something bad happened in io!");
 			e.printStackTrace();
 		}
 		return names;
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
 		byte[] decryptedByteArray = null;
 		try {
 			byte[] b = fileToByteArray(new File(FILE_NAME));
 
 			try {
 				decryptedByteArray = crypter.decrypt(b);
 			} catch (KeyczarException e) {
 				System.out.println("Decryption error");
 				e.printStackTrace();
 			}
 			Scanner sc = new Scanner(byteArrayToFile(decryptedByteArray));
 			while (!sc.nextLine().equals("level" + level)) {
 				// Loop through all the other rows
 			}
 			for (int i = 0; i < times.length; i++) {
 				sc.next(); // Will skip the names
 				times[i] = Integer.parseInt(sc.next());
 			}
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
 			InputStream in = new FileInputStream(FILE_NAME);
 			Scanner sc = new Scanner(in);
 			while (!sc.nextLine().equals("level" + level)) {
 				// Loop through all the other rows
 			}
 			for (int i = 0; i < times.length; i++) {
 				sc.next(); // Will skip the names
 				times[i] = Integer.parseInt(sc.next());
 			}
 		} catch (FileNotFoundException e) {
 			System.out.println("Could't find the specific file.");
 		} catch (NumberFormatException e) {
 			System.out.println("The times are not numbers.");
 		}
 		return time < times[nbrOfScores-1];
 	}
 	
 	public byte[] fileToByteArray(File f) throws IOException {
 
 		InputStream istream = new FileInputStream(f);
 
 		long l = f.length();
 		
 		byte[] bytes = new byte[(int)l];
 		
 		int offset = 0;
 		int numRead = 0;
 		
 		while(offset < bytes.length && (numRead=istream.read(bytes,offset,bytes.length-offset)) >=0) {
 			offset += numRead;
 		}
 		istream.close();
 		return bytes;
 	}
 	/**
 	 * Takes a byte array and converts it to a File and returns the file"
 	 * @param b
 	 * @return
 	 * @throws IOException
 	 */
 	public File byteArrayToFile(byte[] b) throws IOException {
 		OutputStream out = new FileOutputStream("temp");
 		out.write(b);
 		File f = new File("temp");
 		return f;
 	}
 }
