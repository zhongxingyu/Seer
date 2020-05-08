 package org.halvors.lupi.util;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Level;
 
 import org.halvors.lupi.Lupi;
 import org.halvors.lupi.wolf.WolfManager;
 
 public class RandomNameUtil {
 	
 	private final List<String> wolfNames;
 	private final Lupi instance;
 	
 	public RandomNameUtil(Lupi instance) {
 		this.wolfNames = new ArrayList<String>();
 		initRandomNames();
 		this.instance = instance;
 	}
 	
 	public void reloadNames() {
 		wolfNames.clear();
 		initRandomNames();
 	}
 	
 	 /**
      * Generate the table of premade wolf names.
      */
     private void initRandomNames() {  
         try {
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(WolfManager.class.getResourceAsStream("wolfnames.txt")));
            
             while (true) {
                 String s1;
                 
                 if ((s1 = bufferedReader.readLine()) == null) {
                     break;
                 }
                 
                 s1 = s1.trim();
                 
                 if (s1.length() > 0) {
                     wolfNames.add(s1);
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         
         if(wolfNames.size() == 0) {
         	instance.log(Level.SEVERE, "ERROR: wolfnames.txt either was empty or did not end with a new line!");
         }
     }
     
     /**
      * Generate a random name.
      * 
      * @return String
      */
     public String getRandomName() {
         Random random = new Random();
         
         return wolfNames.get(random.nextInt(wolfNames.size()));
     }
 }
