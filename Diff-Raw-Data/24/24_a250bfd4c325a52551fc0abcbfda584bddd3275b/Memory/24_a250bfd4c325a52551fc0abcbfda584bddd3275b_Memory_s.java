 package model;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.*;
 
 public class Memory {
 	private int done;
 
 	private ArrayList<User> users = new ArrayList<User>();
 	private ArrayList<Card> cards = new ArrayList<Card>();
 
 
 	public Memory() throws IOException, ClassNotFoundException {
 		reset();
 	}
 	public void addUser(int id, String username, int score){
 		User adding = new User(id, username, score);
 		users.add(adding);
 	}
 
 	public void save(String saveFile) throws IOException {
 		ObjectOutputStream oos = null;
 		try {
 			FileOutputStream fout = new FileOutputStream(saveFile);
 			oos = new ObjectOutputStream(fout);
 
 			oos.writeObject(users);
 
 			System.out.println("Serializing successfully completed");
 		} finally {
 			try {
 				if (oos != null)
 					oos.close();
 			} finally {
 				oos.close();
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public void load(String loadFile) throws ClassNotFoundException,
 			IOException {
 
 		ObjectInputStream ois = null;
 
 		try {
 			FileInputStream fin = new FileInputStream(loadFile);
 			ois = new ObjectInputStream(fin);
 
 			users = (ArrayList<User>) ois.readObject();
 		} catch (IOException e) {
 			save(loadFile);
 			load(loadFile);
 		} catch (ClassNotFoundException e) {
 			save(loadFile);
 			throw e;
 		}
 	}
 
 	public void reset() throws IOException, ClassNotFoundException {
 		// Reset everything but the score
 		load("test.lst");
 		int k=0;
 		for (int j = 0; j < 2; j++) {
 		for (int i = 0; i < 5; i++) {
 			
 			cards.add(new Card(k,i));//k goes from 0 to 9 for 10 cards
 			cards.add(new Card(k,i));//And i goes from 0 to 4 (two times) for 10 cards
 			//System.out.println("k: "+k+" i:"+i);
 			k++;
 		}
 			
 		}
 
 	}
 
 
 	public boolean checkIfPair(Card card1,Card card2) {
 
 		if(card1.getPairId()==card2.getPairId()){
 			//System.out.println(card1.getPairId());
 			//System.out.println(card2.getPairId());
 			return true;
 		}
 		else{
 			return false;
 		}
 		
 	}
 
 	public boolean checkIfDone() {
 		
 		for (int i = 0; i < (cards.size()/4); i++) {
 			if(cards.get(i).getFound()){
 				done++;
 				//System.out.println("done: "+done);
 				//System.out.println("cards size: "+cards.size());
 				if(done==cards.size()){
 					return true;
 				}	
 			}
 		}
 		return false;
 		
 	}
 
 	public void saveUserInfo() {
 
 	}
 
 	public ArrayList<User> getUsers() {
 		return users;
 	}
<<<<<<< HEAD
 	
 	public ArrayList<Card> getCards() { // I know im sending the acutal array of cards and not a copy.. thats the point
 		return cards;
=======
 	public int getNoOfUsers(){
 		return users.size();
>>>>>>> 8edc98548534cfefa1409a202176310ccf650b34
 	}
 
 	public void loginButton(String inputText) {
 
 		Iterator<User> itr = users.iterator();
 		int u = 0;
 		while (itr.hasNext()) {
 
 			if (inputText == users.get(u).getName()) {
 				u += 1;
 			} // loggedInLayout(); i userinterface ska k'o'ras
 
 			else {
 				u += 1;
 			} // faildLogin.setVisible(true); ska k'o'ras i user interface
 		}
 	}
 
 }
