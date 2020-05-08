 package gsingh.learnkirtan;
 
 import gsingh.learnkirtan.keys.Key;
 
 import java.util.Scanner;
 
 import javax.swing.JOptionPane;
 
 public class Parser {
 
 	private static final int gap = 500;
 
 	private static boolean stop = false;
 	private static boolean pause = false;
 	private static Key[] keys = Main.keys;
 	private static int key = 0;
 
 	public static void parseAndPlay(String text, double tempo) {
 
 		int holdCount;
 
 		Scanner scanner = new Scanner(text);
 		String note = scanner.next("[A-Za-z.']+");
 		String next = null;
 
 		while (!stop) {
 			holdCount = 1;
 
 			if (isPaused())
 				pause();
 
 			// TODO: Unnecessary?
 			if (stop) {
 				stop = false;
 				break;
 			}
 
 			if (!scanner.hasNext("[A-Za-z.'-]+"))
 				stop = true;
 
 			while (scanner.hasNext("[A-Za-z.'-]+")) {
 				next = scanner.next("[A-Za-z.'-]+");
 				if (next.equals("-"))
 					holdCount++;
 				else
 					break;
 			}
 
 			int count = 0;
 			for (int i = 0; i < 3; i++) {
 				if (note.substring(i, i + 1).matches("[A-Za-z-]"))
 					break;
 				count++;
 			}
 			String prefix = note.substring(0, count);
 			String suffix = "";
 			note = note.substring(count);
 			note = note.toUpperCase();
 			int index = note.indexOf(".");
 			if (index == -1)
 				index = note.indexOf("'");
 			if (index != -1) {
 				suffix = note.substring(index);
 				note = note.substring(0, index);
 			}
 
 			System.out.println(prefix + note + suffix);
 			if (note.equals("SA")) {
 				key = 10;
 			} else if (note.equals("RE")) {
 				key = 12;
 			} else if (note.equals("GA")) {
 				key = 14;
 			} else if (note.equals("MA")) {
 				key = 15;
 			} else if (note.equals("PA")) {
 				key = 17;
 			} else if (note.equals("DHA")) {
 				key = 19;
 			} else if (note.equals("NI")) {
 				key = 21;
 			} else {
 				System.out.println("Invalid note.");
 				JOptionPane.showMessageDialog(null, "Error: Invalid note.",
 						"Error", JOptionPane.ERROR_MESSAGE);
 				break;
 			}
 
 			// TODO: Check if notes have valid modifiers
 			if (prefix.contains("'")) {
 				key--;
 			}
 			if (prefix.contains(".")) {
 				key -= 12;
 			}
 			if (suffix.contains("'")) {
 				key++;
 			}
 			if (suffix.contains(".")) {
 				key += 12;
 			}
 			System.out.println(pause);
 			if (key > 0 && key < 48) {
 				keys[key].playOnce((int) (holdCount * gap / tempo));
 				note = next;
 			} else {
 				System.out.println("Invalid note.");
 				JOptionPane.showMessageDialog(null, "Error: Invalid note.",
 						"Error", JOptionPane.ERROR_MESSAGE);
 				break;
 			}
 		}
 	}
 
 	public static void stop() {
 		stop = true;
 		pause = false;
 	}
 
 	public static void setPause() {
 		pause = true;
 	}
 
 	public static void pause() {
 		while (pause) {
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public static boolean isPaused() {
 		return pause;
 	}
 
 	public static void play() {
 		pause = false;
 	}
 }
