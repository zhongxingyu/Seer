 package de.nordakademie.nakjava.server.persistence;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.LinkedList;
 import java.util.List;
 
 import de.nordakademie.nakjava.gamelogic.shared.playerstate.PlayerState;
 
 public abstract class DeckPersister {
 	// TODO why not overwrite file each time with current
 	// PlayerState.getSavedDecks()???
 	public static final String FOLDER = "persistence";
 
 	// public static void saveDeck(Deck deck, PlayerState playerState) {
 	// File file = new File(FOLDER + "/" + playerState.getName() + "_decks");
 	//
 	// if (!file.exists()) {
 	//
 	// ObjectOutputStream oos = null;
 	// try {
 	// file.createNewFile();
 	// oos = new ObjectOutputStream(new BufferedOutputStream(
 	// new FileOutputStream(file)));
 	//
 	// oos.writeObject(new LinkedList<Deck>());
 	//
 	// } catch (IOException e) {
 	// // TODO Auto-generated catch block
 	// e.printStackTrace();
 	// } finally {
 	// try {
 	// oos.close();
 	// } catch (Exception e) {
 	// }
 	// }
 	//
 	// List<Deck> decks = getDecks(playerState);
 	// decks.add(deck);
 	//
 	// try {
 	// oos = new ObjectOutputStream(new BufferedOutputStream(
 	// new FileOutputStream(file)));
 	// oos.writeObject(decks);
 	// try {
 	// oos.close();
 	// } catch (Exception e) {
 	// }
 	// } catch (IOException e) {
 	// // TODO Auto-generated catch block
 	// e.printStackTrace();
 	// }
 	//
 	// }
 	//
 	// }
 
 	public static void saveDecks(PlayerState playerState) {
 		File file = new File(playerState.getName() + "_decks");
 		ObjectOutputStream oos = null;
 		if (!file.exists()) {
 
 			try {
 				file.createNewFile();
 				oos = new ObjectOutputStream(new BufferedOutputStream(
 						new FileOutputStream(file)));
 
 				oos.writeObject(new LinkedList<Deck>());
 
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} finally {
 				try {
 					oos.close();
 				} catch (Exception e) {
 				}
 			}
 		}
 
 		List<Deck> decks = playerState.getSavedDecks();
 
 		try {
 			oos = new ObjectOutputStream(new BufferedOutputStream(
 					new FileOutputStream(file)));
 			oos.writeObject(decks);
 			try {
 				oos.close();
 			} catch (Exception e) {
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	public static List<Deck> getDecks(PlayerState playerState) {
 		File file = new File(playerState.getName() + "_decks");
 		List<Deck> result = new LinkedList<>();
 
 		if (file.exists()) {
 			ObjectInputStream ois = null;
 			try {
 				ois = new ObjectInputStream(new BufferedInputStream(
 						new FileInputStream(file)));
 				result = (List<Deck>) ois.readObject();
 			} catch (IOException | ClassNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} finally {
 				try {
 					ois.close();
 				} catch (Exception ex) {
 				}
 			}
 		}
 
 		return result;
 	}
 }
