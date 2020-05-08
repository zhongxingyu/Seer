 package nl.ecb.samp.ericrp.model;
 
 import java.util.List;
import nl.ecb.samp.ericrp.model.Character;
 public class Account {
 	private String Password,Username,Email;
 	private int ID;
 	private List<Character> characters;
 	public Account(String username, String password, String email, int iD,List<Character> characters) {
 		super();
 		Password = password;
 		Username = username;
 		Email = email;
 		ID = iD;
 		this.characters = characters;
 		
 	}
 	public static Account load(String username, String password, String email, int iD,List characters){
 		return new Account(username, password, email, iD, characters);
 	}
 	public void unload(){
 		//TODO add unload code or something
 	}
 	public String getPassword() {
 		return Password;
 	}
 	public void setPassword(String password) {
 		Password = password;
 	}
 	public String getUsername() {
 		return Username;
 	}
 	public void setUsername(String username) {
 		Username = username;
 	}
 	public String getEmail() {
 		return Email;
 	}
 	public void setEmail(String email) {
 		Email = email;
 	}
 	public int getID() {
 		return ID;
 	}
 	public void setID(int iD) {
 		ID = iD;
 	}
 	public List<Character> getCharacters() {
 		return characters;
 	}
 	public void setCharacters(List<Character> characters) {
 		this.characters = characters;
 	}
 
 
 }
