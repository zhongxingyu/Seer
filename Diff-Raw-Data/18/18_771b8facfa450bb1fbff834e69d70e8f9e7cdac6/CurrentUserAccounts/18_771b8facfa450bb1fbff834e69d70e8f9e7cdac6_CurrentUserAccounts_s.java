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
 
 public class CurrentUserAccounts
 {
     private ArrayList<User> users;
     private String cuaFile;
 
 
     /**
      * 
      * @param cuaFile
      * @throws FatalError 
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
      * 
      * @param user
      */
     public void addUser(User user)
     {
         users.add(user);
     }
 
     /**
      * 
      * @param username
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
     
     public ArrayList<User> getAllUsers()
     {
     	return users;
     }
 
     /**
      * 
      * @param username
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
      * 
      * @param username
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
 
    public void diplayUsers()
     {
         for (User user : users)
         {
             System.out.println(user.getUsername() + " " + user.getType() + " " + user.getCredit());
         }
     }
     
     /**
      * Write the current user accounts file in memory to file, over-writing
      * the old current user accounts file.
      * @throws FatalError
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
