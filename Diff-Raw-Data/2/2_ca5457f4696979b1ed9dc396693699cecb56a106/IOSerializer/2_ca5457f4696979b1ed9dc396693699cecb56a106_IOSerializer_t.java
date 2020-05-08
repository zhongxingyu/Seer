 package iogame;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import exceptions.GameIOException;
 import game.Game;
 
 /**
  * This class handles game saving and loading through serialization.
  */
 public class IOSerializer {
 
 	/**
 	 * Creates a new game from a saved game file.
 	 * 
 	 * @param f
	 * 		The file to load the game from
 	 * @return Game
 	 * @throws IOException
 	 * @throws FileNotFoundException
 	 * @throws InvalidLoadedFileException
 	 */
 	public static Game load(File f) throws GameIOException,
 			FileNotFoundException, IOException {
 		ObjectInputStream file = null;
 		try {
 			file = new ObjectInputStream(new BufferedInputStream(
 					new FileInputStream(f)));
 			return (Game) file.readObject();
 
 		} catch (ClassNotFoundException e) {
 			throw new GameIOException();
 		} finally {
 			if (file != null) {
 				file.close();
 				System.out.println("Archivo cargado");
 			}
 		}
 	}
 
 	/**
 	 * Saves the game in the specified file.
 	 * 
 	 * @param f
 	 *            The file to save the game into
 	 * @throws FileNotFoundException 
 	 * @throws IOException
 	 */
 	public static void save(Game game, File f) throws FileNotFoundException, IOException {
 		ObjectOutputStream file = null;
 		try {
 			file = new ObjectOutputStream(new BufferedOutputStream(
 					new FileOutputStream(f)));
 			file.writeObject(game);
 
 		} finally {
 			if (file != null) {
 				file.close();
 				System.out.println("Archivo guardado en " + f.getName());
 			}
 		}
 
 	}
 }
