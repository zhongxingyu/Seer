 /**
  * Swift Ticket -- Back End
  *
  * Copyright (C) 2013, Jonathan Gillett, Daniel Smullen, and Rayan Alfaheid
  * All rights reserved.
  *
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.google.common.base.Strings;
 
 /**
  * CurrentUserAccounts Provides methods and attributes for interacting with the
  * current user accounts file in memory, and outputting to disk.
  * 
  * @author Jonathan Gillett
  * @author Daniel Smullen
  * @author Rayan Alfaheid
  */
 public class CurrentUserAccounts
 {
     private ArrayList<User> users;
     private String cuaFile;
 
 
     /**
      * Constructor for the class. Takes the path to the available tickets file, and opens it.
      * 
      * @param cuaFile The path to the current user accounts file. Unix-style paths only.
      * 
      * @throws FatalError Fatal errors occur under the following circumstances:
      * 			<br>The Current User Accounts file is missing, or not found at the path specified.
      */
     public CurrentUserAccounts(String cuaFile) throws FatalError
     {
         File file = new File(cuaFile);
 
         if (file.exists())
         {
             this.cuaFile = cuaFile;
             this.users = new ArrayList<User>();
             
             /* Parse the current user accounts file */
             parse();
         }
         else
         {
             throw new FatalError(ExceptionCodes.CUA_NOT_FOUND, cuaFile);
         }
     }
 
     /**
      * Adds a User object to the current user accounts file collection.
      * 
      * @param user The User object to add to the current user accounts file collection.
      */
     public void addUser(User user)
     {
         users.add(user);
     }
 
     /**
      * Removes a User object from the current user accounts file collection, based
      * on the user's name. If there exists no User object with the specified username,
      * nothing happens.
      * 
      * @param username String containing the name of the user to delete.
      */
     public void deleteUser(String username)
     {
         for (User user : users)
         {
             if (user.getUsername().equals(username))
             {
                 users.remove(user);
                 break;
             }
         }
     }
     
     /**
      * Access method for the contents of the current user accounts file.
      * 
      * @return Returns an ArrayList of User objects, which is the contents of the entire
      * current user accounts file collection.
      */
     public ArrayList<User> getAllUsers()
     {
     	return users;
     }
 
     /**
      * Access method for a particular User object in the collection, by name. If
      * there exists no User object with the username specified, nothing happens.
      * 
      * @param username String specifying the username of the User object to match to.
      * 
      * @return Returns a User object with the username specified. Returns null if
      * no User object with that name exists.
      */
     public User getUser(String username)
     {
         for (User user : users)
         {
             if (user.getUsername().equals(username))
             {
                 return user;
             }
         }
         
         return null;
     }
 
     /**
      * Determines whether a specified User exists in the current user 
      * accounts file, corresponding to the username specified.
      * 
      * @param username
      * 
      * @return Returns true if the username specified is in the current user
      * accounts file. Returns false if the username is not found.
      */
     public boolean hasUser(String username)
     {
         for (User user : users)
         {
             if (user.getUsername().equals(username))
             {
                 return true;
             }
         }
         
         return false;
     }
 
     /**
      * Outputs all the entries in the current user accounts file to the console.
      * Output contains the username, the user type, and their credit amount.
      */
     public void displayUsers()
     {
         for (User user : users)
         {
             System.out.println(user.getUsername() + " " + user.getType() + " " + user.getCredit());
         }
     }
     
     /**
      * Writes the current user accounts file in memory to file, over-writing
      * the old current user accounts file.
      * 
      * @throws FatalError Fatal errors occur under the following circumstances:
      * 			<br>The current user accounts file is corrupted.
      */
     public void write() throws FatalError
     {
         BufferedWriter writer;
         String entry;
         
         try 
         {
            File file = new File(cuaFile + ".log");
  
             /* Create the file if it does not exist since parsed */
             if (!file.exists())
             {
                 file.createNewFile();
             }
     
             writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
             
             /* Format each entry and write it to the file */
             for (User user : users)
             {                
                 /* Format the username */
                 entry = Strings.padEnd(user.getUsername(), 16, '_');
                 
                 /* Format the account type */
                 entry += user.getType() + "_";
                 
                 /* Format the credit amount */
                 entry += Strings.padStart(String.format("%.2f", user.getCredit()), 9, '0') + "\n";
                 
                 
                 writer.write(entry);
             }
             
             /* Add the END of file identifier and close the file */
             writer.write("END________________000000.00");
             writer.close();
  
         } 
         catch (IOException e)
         {
             e.printStackTrace();
             throw new FatalError(ExceptionCodes.CORRUPT_CUA, cuaFile);
         }
     }
 
     /**
      * Parses the current user accounts file entries, matching the specified format
      * based on regular expressions.
      * 
      * @throws FatalError Fatal errors occur under the following circumstances:
      * 			<br>The Current User Accounts File is missing, or not found at the path specified.
      * 			<br>The current user accounts file is corrupted.
      */
     private void parse() throws FatalError
     {
         BufferedReader reader;
 
         String line;
         Pattern re = Pattern.compile("^([A-Za-z0-9_]{1,15}?)_+(AA|FS|BS|SS)_([0-9]{6}\\.[0-9]{2})$");
         Pattern reEnd = Pattern.compile("END_{16}0{6}\\.0{2}");
         Matcher match;
 
         try
         {
             reader = new BufferedReader(new FileReader(cuaFile));
         }
         catch (FileNotFoundException e1)
         {
             e1.printStackTrace();
             throw new FatalError(ExceptionCodes.CUA_NOT_FOUND, cuaFile);
         }
 
         try
         {
             while ((line = reader.readLine()) != null)
             {
                 /* Stop if END of file reached */
                 if (reEnd.matcher(line).matches())
                 {
                     break;
                 }
 
                 match = re.matcher(line);
                 
                 /* Add each ticket found to the list of tickets */
                 if (Validate.cuaEntry(line) && match.matches())
                 {
                     /* Get each match from the entry in the file */
                     String username = match.group(1);
                     String type = match.group(2);
                     Double credit = Double.parseDouble(match.group(3));
 
                     /* Add the ticket object to the array of tickets */
                     users.add(new User(username, type, credit));
                 }
                 else
                 {
                     throw new FatalError(ExceptionCodes.CORRUPT_CUA, cuaFile);
                 }
             }
             reader.close();
         }
         catch (IOException e)
         {
             e.printStackTrace();
             throw new FatalError(ExceptionCodes.CORRUPT_CUA, cuaFile);
         }
         finally
         {
             try
             {
                 if (reader != null)
                 {
                     reader.close();
                 }
             }
             catch (IOException ex)
             {
                 ex.printStackTrace();
                 throw new FatalError(ExceptionCodes.CORRUPT_CUA, cuaFile);
             }
         }
     }
 }
