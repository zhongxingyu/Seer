 package Klient;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 
 public class Main {
 	//Main class. Handles initial loading of saved bookmarks, contains the object where these are stored, the klient backend
 	//object and the main user interface gui object, as well as the save and load functions of bookmarks.
 
	public static GUI gui = new GUI();
 	public static Klient klient = new Klient();
 	public static Bookmark mainData;
 
 	public static void main(String[] args) {
 
 		//Tries to load data. Prints to console the results. If loading failed because a file was not found,
 		//creates an empty bookmark object.
 		try {
 			mainData = loadData();
 			System.out.println("Succeeding in loading bookmarks!");
 			mainData.listAllBookmarks();
 		} catch (FileNotFoundException e) {
 			System.out.println("File bookmark.data not found. Creating a new mainBookmark.");
 			mainData = new Bookmark();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} 
 	}
 
 
 	public static void saveData(Bookmark saveBookmark){
 		ObjectOutputStream output = null;
 
 		//Open file for writing
 		try {
 			output = new ObjectOutputStream(new FileOutputStream("bookmarks.data"));
 		} catch (FileNotFoundException e) {
 			System.out.println("File could not be opened.");
 		} catch (IOException e) {			
 			e.printStackTrace();
 		}
 
 		if(output != null){
 
 			//Write to file
 			try {
 				output.writeObject(saveBookmark);
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 
 
 			//Close output
 			try {
 				output.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public static Bookmark loadData() throws FileNotFoundException, IOException{
 		Bookmark loadedBookmark = null;
 		ObjectInputStream input = null;
 
 		//Open input stream
 		input = new ObjectInputStream(new FileInputStream("bookmarks.data"));
 
 
 		if(input != null){
 
 			//Read bookmark from file
 			Object obj = null;
 			try {
 				obj = input.readObject();
 			} catch (ClassNotFoundException e1) {
 				System.out.println("Trying to load an object of unknown class type.");
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 			if( (obj != null) && (obj instanceof Bookmark) ){
 				loadedBookmark = (Bookmark)obj;
 			}
 
 			//Close input stream
 			try {
 				input.close();
 			} catch (IOException e) {
 				System.out.println("Could not close file.");
 			}
 		}		
 		return loadedBookmark;
 	}
 }
