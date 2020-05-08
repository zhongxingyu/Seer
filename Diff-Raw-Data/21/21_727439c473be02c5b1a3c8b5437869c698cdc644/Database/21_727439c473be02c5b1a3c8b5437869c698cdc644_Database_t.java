 /**
  * 
  */
 package server;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import UserInfo.Account;
 import UserInfo.Recipe;
 import UserInfo.User;
 
 /**
  * @author hacheson
  * Fake database storage system.
  */
 public class Database {
 
 	HashMap<String, String> userToPassword_;
 	HashMap<String, Account> nameToAccount_;
 	//HashMap<String, User> nameToUser_; 
 
 	public Database(){
 		userToPassword_ = new HashMap<String, String>();
 		nameToAccount_ = new HashMap<String, Account>();
 		userToPassword_.put("Hannah", "Hannah");
 		userToPassword_.put("Natalie", "Natalie");
 		userToPassword_.put("Jon", "Jon");
 		userToPassword_.put("Eddie", "Beyonce");
 		
		populateDataBase();
 		///ACCOUNT THING
 	
 	}
 	
 	/**
 	 * Returns whether a password is valid given a username.
 	 * @param user User to check.
 	 * @return String user to check.
 	 */
 	public String getPasswordFromUser(String user){
 		//System.out.println( "returning " + userToPassword_.get(user));
 		return userToPassword_.get(user);
 	}
 	
 	public void addUser(String user, String password){
 		userToPassword_.put(user, password);
 	}
 	
 	public void removeUser(String user, String password){
 		userToPassword_.remove(user);
 	}
 	
 	public Account getAccountFromUser(String name){
 		return nameToAccount_.get(name);
 	}
 	
 	public void populateDataBase(){
 
 		//Hannah
 		User hann = new User("Hannah", userToPassword_.get("Hannah"));
 		hann.setAddress("LittleField");
 		HashSet<String> i = new HashSet<String>();
 		i.add("apples");
 		i.add("pears");
 		i.add("articokes");
 		String[] arr = {"chicken", "bread", "cheese"};
 		ArrayList<String> ingredients = new ArrayList<String>(Arrays.asList(arr));
 		
 		HashSet<String> sl = new HashSet<String>();
 		sl.add("2 apples");
 		sl.add("2 pears");
 		sl.add("2 articokes");
 
 		Recipe rec = new Recipe("Chicken Kiev", "111", "Place stuff in a bowl and mix it.", ingredients);
 		Recipe rec2 = new Recipe("Chicken Kiev2", "2111", "2Place stuff in a bowl and mix it.", ingredients);
 		HashSet<Recipe> recipes = new HashSet<Recipe>();
 		recipes.add(rec);
 		recipes.add(rec2);
 		
 		Account acc = new Account(hann);
 		acc.setRecipes(recipes);
 		acc.setIngredients(i);
 		acc.setShoppingList(sl);
 
 		nameToAccount_.put("Hannah", acc);
 		
 		//Eddie
 		User eddie = new User("Eddie", userToPassword_.get("Beyonce"));
		eddie.setAddress("Near Darwins");
 		i = new HashSet<String>();
 		i.add("BEER");
 		i.add("QUINA");
 		String[] arr2 = {"protien", "ice", "milk", "banana"};
 		ingredients = new ArrayList<String>(Arrays.asList(arr2));
 		
 		sl = new HashSet<String>();
 		sl.add("MORE BEER");
 
 
 		rec = new Recipe("Protein Smoothie", "r123", "Put ingredients in Blender. Blenderize.", ingredients);
 		recipes = new HashSet<Recipe>();
 		recipes.add(rec);
 		
		acc = new Account(eddie);
 		acc.setRecipes(recipes);
 		acc.setIngredients(i);
 		acc.setShoppingList(sl);
 		nameToAccount_.put("Eddie", acc);
 		
 		//Jon
 		User Jon = new User("Jon", userToPassword_.get("Jon"));
		Jon.setAddress("Brown University");
 		i = new HashSet<String>();
 		i.add("Peanuts");
 		i.add("Hipster Food");
 		String[] arr3 = {"PBR", "glasses", "hipstah swag"};
 		ingredients = new ArrayList<String>(Arrays.asList(arr3));
 		
 		sl = new HashSet<String>();
 		sl.add("Pshhhh");
 
 
 		rec = new Recipe("Awesomeness", "r/id#", "Keep On Keeping On", ingredients);
 		recipes = new HashSet<Recipe>();
 		recipes.add(rec);
 		
		acc = new Account(Jon);
 		acc.setRecipes(recipes);
 		acc.setIngredients(i);
 		acc.setShoppingList(sl);
 		nameToAccount_.put("Jon", acc);
 		
 		//Natalie
 		User Natalie = new User("Natalie", userToPassword_.get("Natalie"));
		Natalie.setAddress("CIT");
 		i = new HashSet<String>();
 		i.add("String Cheese");
 		i.add("Left Over Mac n Cheese");
 		String[] arr4 = {"Cheese", "Beer", "Bread"};
 		ingredients = new ArrayList<String>(Arrays.asList(arr4));
 		
 		sl = new HashSet<String>();
 		sl.add("...too poor for food");
 
 		rec = new Recipe("Cheesey Beer Bread", "r/cbb", "Bake", ingredients);
 		recipes = new HashSet<Recipe>();
 		recipes.add(rec);
 		
		acc = new Account(Natalie);
 		acc.setRecipes(recipes);
 		acc.setIngredients(i);
 		acc.setShoppingList(sl);
 		nameToAccount_.put("Natalie", acc);
 
 		
 	}
 	
 }
