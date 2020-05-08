 package manager;
 
 import entity.*; 
 
 import java.util.*;
 
 public class ChoreManager implements java.io.Serializable {
 
 	List<Chore> allChores = new ArrayList<Chore>();
 	
 	//default constructor
 	public ChoreManager() {
 		//create new items in the list.
 		//Chore status
 			//Available
 			//In-Progress
 			//Waiting (Means pending on parents to approve and award the points over to the child)
 			//Saboed (Someone sabo the mentioned Child)
 		
 		Chore chore1 = new Chore(allChores.size()+1, "Sweep the floor", "In-Progress", 4, "agurz","", "Empire 2");
 		allChores.add(chore1);
 		
 		Chore chore2 = new Chore(allChores.size()+1, "Clean up dinner table", "In-Progress", 2, "freda","", "Empire 2");
 		allChores.add(chore2);
 		
 		Chore chore3 = new Chore(allChores.size()+1, "Buy toilet paper", "Available", 2, "","", "Empire 2");
 		allChores.add(chore3);
 		
 		Chore chore4 = new Chore(allChores.size()+1, "Bathe the dog", "Pending", 3, "agurz","", "Empire 2");
 		allChores.add(chore4);
 		
 		Chore chore5 = new Chore(allChores.size()+1, "Bathe the cat", "In-Progress", 3, "denise","", "Empire 1");
 		allChores.add(chore5);
 		
 		Chore chore6 = new Chore(allChores.size()+1, "Bathe the hamster", "Saboed", 2, "agurz","freda", "Empire 2");
 		allChores.add(chore6);
 		
 		Chore chore7 = new Chore(allChores.size()+1, "Change sofa seats", "Available", 4, "","", "Empire 1");
 		allChores.add(chore7);
 		
 		Chore chore8 = new Chore(allChores.size()+1, "Wash the car", "Available", 6, "","", "Empire 3");
 		allChores.add(chore8);
 		
 		Chore chore9 = new Chore(allChores.size()+1, "Clean the window", "Available", 5, "","", "Empire 2");
 		allChores.add(chore9);
 		
 		Chore chore10 = new Chore(allChores.size()+1, "Clean the toilet", "Available", 4, "","", "Empire 2");
 		allChores.add(chore10);	
 	}
 	
 	/**
      * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
      * or the first access to SingletonHolder.INSTANCE, not before.
      */
 	private static class ChoreManagerHolder { 
         public static final ChoreManager INSTANCE = new ChoreManager();
 	}
 	
 	public static ChoreManager getInstance() {
         return ChoreManagerHolder.INSTANCE;
 	}
 	
 	public List<Chore> getAllChores() {
		return allChores;
 	}
 	
 	public List<Chore> getChoreToSpecificMember(String username) {
 		List<Chore> choresToDoTemp = new ArrayList<Chore>();
 		
 		Object user = FamilyManager.getInstance().getUser(username);
 		
 		if (user instanceof Child) {
 			Child currentChild = (Child) user;
 			
 			for (Chore c: this.getAllChores()) {
 				if (c.getEmpire().equalsIgnoreCase(currentChild.getEmpire()) //if same empire i add in AND
 				&& (c.getChoreTakenBy().equalsIgnoreCase(username) || c.getChoreStatus().equalsIgnoreCase("Available")) //chore belongs to you OR chore status is available i add in. 
 				
 				) {
 					choresToDoTemp.add(c);
 				}
 			}
 		} else {
 			Parent currentParent = (Parent) user;
 			
 			for (Chore c: this.getAllChores()) {
 				if (c.getEmpire().equalsIgnoreCase(currentParent.getEmpire())) {
 					choresToDoTemp.add(c);
 				}
 			}
 		}
 		
 		return choresToDoTemp;
 	}
 	
 	//get all chores that are not pending. 
 	public List<Chore> getChoreToSabo (String username) {
 		List<Chore> choresToSabo = new ArrayList<Chore>();
 		
 		
 		Object user = FamilyManager.getInstance().getUser(username);
 		
 		if (user instanceof Child) {
 			Child currentChild = (Child) user;
 			for (Chore c: this.getAllChores()) {
 				
 				if (c.getEmpire().equalsIgnoreCase(currentChild.getEmpire()) &&
 						(c.getChoreStatus().equalsIgnoreCase("Available") || (c.getChoreStatus().equalsIgnoreCase("Saboed") && !c.getSaboBy().equalsIgnoreCase(username) && c.getChoreTakenBy().equalsIgnoreCase(username)))
 						) {
 					choresToSabo.add(c);
 				}
 				
 			}		
 		} else {
 			Parent curentParent = (Parent) user;
 			for (Chore c: this.getAllChores()) {
 				if (c.getChoreStatus().equalsIgnoreCase("Available") || c.getChoreStatus().equalsIgnoreCase("Saboed")) {
 					choresToSabo.add(c);
 				}
 			}		
 		}
 		
 		
 		return choresToSabo;
 	}
 	
 	//add new chore to the arraylist.
 	public void addChore (Chore c) {
 		allChores.add(c);
 	}
 	
 	//add new chore to the arraylist.
 	public Chore getChore (int choreID) {
 		for (Chore c: this.getAllChores()) {
 			if (c.getChoreID() == choreID) {
 				return c;
 			}
 		}
 		return null;
 	}
 	
 	//removing the specific chore with the given username and empire
 	public void removeChore (int choreID, String username) {
 				
 		for (int i = 0; i < allChores.size(); i++) {
 			Chore c = allChores.get(i);
 			
 			//if chore ID matches and it belongs to the specific user.
 			if (c.getChoreID() == choreID && c.getChoreTakenBy().equalsIgnoreCase(username)) {
 				allChores.remove(i);
 			}
 		}
 		
 	}
 	
 	//getting chores that belongs to the specific empire
 	public List<Chore> getAllChoresForThisEmpire(String empire) {
 		List<Chore> choresForThisEmpire = new ArrayList<Chore>();
 		
 		for (Chore c: allChores) {
 			if (c.getEmpire().equalsIgnoreCase(empire)) {
 				choresForThisEmpire.add(c);
 			}
 		}
 		
 		return choresForThisEmpire;
 	}
 	
 	//getting pending chores under the specific empire
 	public List<Chore> getAllCompletedChoresUnderThisEmpire (String empire) {
 		List<Chore> allCompletedChoresUnderThisEmpire = new ArrayList<Chore>();
 		
 		for (Chore c: this.getAllChoresForThisEmpire(empire)) {
 			if (c.getChoreStatus().equalsIgnoreCase("Pending")) {
 				allCompletedChoresUnderThisEmpire.add(c);
 			}
 		}
 		return allCompletedChoresUnderThisEmpire;
 	}
 	
 	//getting !pending chores under the specific empire
 	public List<Chore> getAllNotCompletedChoresUnderThisEmpire (String empire) {
 		List<Chore> allNotCompletedChoresUnderThisEmpire = new ArrayList<Chore>();
 		
 		for (Chore c: this.getAllChoresForThisEmpire(empire)) {
 			if (!c.getChoreStatus().equalsIgnoreCase("Pending")) {
 				allNotCompletedChoresUnderThisEmpire.add(c);
 			}
 		}
 		return allNotCompletedChoresUnderThisEmpire;
 	}
 	
 	
 	//changing the status of the specific chore
 	public void setChoreStatus (Chore c, String status) {
 		
 		List<Chore> choresForThisEmpire = this.getAllChoresForThisEmpire(c.getEmpire());
 		
 		for (Chore temp: choresForThisEmpire) {
 			if (temp.getChoreID() == c.getChoreID()) {
 				temp.setStatus(status);
 			}
 		}
 	}
 	
 }
