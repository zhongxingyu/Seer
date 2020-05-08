 /**
  * 
  */
 package edu.txstate.hearts.utils;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 
 /**
  * Holds implementation for the creation and modification of data belonging to
  * an individual user
  * 
  * @author Jonathan Shelton, Maria Poole,,,
  * 
  */
 public class UserData {
 	
 	private final String userName;
 	
 	public UserData(String userName)
 	{
 		this.userName = userName;
 	}
 
 	public void createUserDataFile(List<String> achievements) throws IOException {
 		if (!ReadFiles.getRecords().contains(userName)) {
 			addUserNameToFile();
 		}
 
 		File name = new File(userName + ".txt");
 		try {
			FileWriter output = new FileWriter(name, true);// open/create file
 			for (String achievement : achievements) {
 				output.write(achievement + "\n");
 			}
 			output.close();
 		} catch (IOException ioe) {
 			System.err.println("IOException: " + ioe.getMessage());
 			throw ioe;
 		}
 
 	}// end of function
 
 	public void addUserNameToFile() throws IOException {
 		if(!ReadFiles.getRecords().contains(userName))
 		{
 		try {
 			String filename = "Users.txt";
 			FileWriter fw = new FileWriter(filename, true); // the true will
 															// append the new
 															// data
 			fw.write(userName + "\n");// appends the string to the file
 			fw.close();
 			ReadFiles.addUserRecord(userName);
 		} catch (IOException ioe) {
 			System.err.println("IOException: " + ioe.getMessage());
 			throw ioe;
 		}
 		}
 
 	}
 	
 	public void writeAchievements(Map<String, Boolean> achievements)
 	{
 		Set<String> keySet = achievements.keySet();
 		List<String> passedAchievements = new ArrayList<String>();
 		for(String key: keySet)
 		{
 			Boolean passed = achievements.get(key);
 			if(passed.booleanValue())
 				passedAchievements.add(key);
 		}
 		try {
 			createUserDataFile(passedAchievements);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }// end of class
