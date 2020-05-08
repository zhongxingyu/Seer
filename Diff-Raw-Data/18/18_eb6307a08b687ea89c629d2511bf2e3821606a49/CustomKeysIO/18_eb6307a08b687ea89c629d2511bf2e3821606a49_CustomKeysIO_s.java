 package se.chalmers.kangaroo.io;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Writer;
 import java.util.Scanner;
 
 /**
  * This is a class that will save your custom keys to a file. You will of course
  * be able to load them as well.
  * 
  * @author alburgh
  * 
  */
 public class CustomKeysIO {
 	private static CustomKeysIO instance;
	private static int[] keys = new int[4];
 	private static final String FILE_NAME = "resources/customkeys.txt";
 
 	/* Private constructor, so only one instance will be created. */
 	private CustomKeysIO() {
		loadKeys();
 	}
 
 	/**
 	 * Returns the only instance of this class.
 	 * @return
 	 */
 	public static synchronized CustomKeysIO getInstance() {
 		if(instance == null)
 			instance = new CustomKeysIO();
 		return instance;
 	}
 	/**
 	 * Returns the current keys set in the file.
 	 * @return the key codes. 
 	 */
 	public int[] getKeys() {
 		loadKeys();
		return keys;
 	}
 	/**
 	 * Use the KeyEvent.keycodes to set your keys.
 	 * You can then use getKeys to return them.
 	 * @param key, the array of key codes. 
 	 */
 	public void setKeys(int[] key) {
 		try {
 			Writer w = new FileWriter(FILE_NAME);
 			for (int i = 0; i < key.length; i++) {
 				w.write(key[i] + "\n");
 			}
 			w.close();
 		} catch (FileNotFoundException e) {
 			System.out.println("There is no such file");
 		} catch (IOException e) {
 			System.out.println("Something went wrong with the io writing");
 		}
 	}
 	/* Used to make sure that the latest version of the keys are returned. */
 	private static void loadKeys() {
 		try {
 			InputStream in = new FileInputStream(FILE_NAME);
 			Scanner sc = new Scanner(in);
 			while (sc.hasNext()) {
				int i = 0;
				keys[i] = Integer.parseInt(sc.nextLine());
 				i++;
 			}
 		} catch (FileNotFoundException e) {
 			System.out.println("No such file exists");
 		}
 	}
 
 }
