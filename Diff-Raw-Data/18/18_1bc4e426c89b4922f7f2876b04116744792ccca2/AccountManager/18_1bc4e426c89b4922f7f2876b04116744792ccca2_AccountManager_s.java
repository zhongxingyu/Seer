 /*
  * Copyright (c) 2009  Mel Nicholson.
  * All Rights Reserved.
  */
 package mel.security;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 
 /**
  * This class handles logins.  All operations that change data must be
  * committed to disk before they return.  All commands must work in many
  * threads at once once (with locks for safety as needed)
  *
  * Each account should be represented by one .act file (e.g. richard.act)
  * @author charidan
  */
 public class AccountManager
 {
 
     private File dir;
     private static AccountManager singleton = null;
     public static final String defaultDir = "C:/gameServer/accounts";
 
     public AccountManager(String directoryName)
     {
         if (singleton != null)
         {
             throw new Error("Only one Account Manager!");
         }
         dir = new File(directoryName);
         singleton = this;
     }
 
     public static AccountManager getAccountManager()
     {
         return singleton;
     }
 
     /**
      * Create an account if it doesn't already exist.  Do not allow
      * someone to use the empty string, nor any account that starts with
      * an underscore, nor any account which contains a colon.
      *
      * @param name the user name
      * @param passwd the initial password
      * @throws java.lang.SecurityException if account already exists
      */
     synchronized public void createAccount(String name, String passwd)
             throws SecurityException
     {
         if(name.equals("")) throw new SecurityException("Illegal Account Name");
         if(name.contains(":")) throw new SecurityException("Illegal Account Character - Colon.");
         if(name.charAt(0) == '_') throw new SecurityException("Illegal Account Name - Name may not begin with underscore");
         File f = new File(dir, name + ".act");
         Account acc = new Account(name, passwd);
         try
         {
             if(f.exists()) throw new SecurityException("Could not Create Account - Account Already Exists");
             f.createNewFile();
             saveAccount(acc);
         } catch (IOException ex)
         {
             throw new SecurityException("Could not create account.", ex);
         }
     }
 
     synchronized public void deleteAccount(String name)
             throws SecurityException
     {
         File f = new File(dir, name + ".act");
         if (!f.delete())
         {
             throw new SecurityException("Account could not be deleted.");
         }
     }
 
     /**
      * @param name the user name
      * @return the named account 
      * @throws java.lang.SecurityException if no such account
      */
     //TODO cache the account so it doesn't get reloaded a gazillion times
     synchronized public Account getAccount(String name)
             throws SecurityException
     {
        File f = new File(dir, name + ".act");
         if (!f.exists())
         {
             throw new SecurityException("Could not access account - Account does not exist.");
         }
         try
         {
             BufferedReader fr = new BufferedReader(new FileReader(f));
             Account a = new Account(name, fr.readLine());
             String s = fr.readLine();
             while (s != null && !s.equals("endAuth"))
             {
                 a.addAuthorization(s);
                 s = fr.readLine();
             }
             fr.close();
             return a;
         } catch (FileNotFoundException ex)
         {
             throw new SecurityException("Could not access account - Account does not exist.", ex);
         } catch (IOException ex)
         {
             throw new SecurityException("Could not read file.", ex);
         }
     }
 
     synchronized public void addAuthorization(String feature, String accName)
     {
         Account acc = getAccount(accName);
         if(acc == null) throw new SecurityException("No such account");
         acc.addAuthorization(feature);
         saveAccount(acc);
     }
 
     synchronized public void setPassword(String name, String newPassword) throws SecurityException
     {
         Account acc = getAccount(name);
         if(acc == null) throw new SecurityException("No such account");
         acc.setPassword(newPassword);
         saveAccount(acc);
     }
 
     synchronized void saveAccount(Account acc)
     {
         File f = new File(dir, acc.getName() + ".act");
         if (!f.exists())
         {
             throw new SecurityException("Could not save account - Account does not exist.");
         }
         try
         {
             PrintWriter fw = new PrintWriter(new FileWriter(f));
             fw.println(acc.getPassword());
             for (String s : acc.getAuth())
             {
                 fw.println(s);
             }
             fw.println("endAuth");
             fw.close();
         } catch (IOException ex)
         {
             throw new SecurityException("Could not save account.", ex);
         }
     }
 
     /*synchronized public boolean isAuthorized(User u, String feature)
     {
         Account a = getAccount(u.getName());
         if(a == null) return false;
         return a.isAuthorized(feature);
     }*/
 
     // TEST CODE ONLY BELOW THIS POINT
     private static void ABORT(String msg)
     {
         System.err.println(msg);
         System.exit(1);
     }
 
     /**
      * A simple input test utility
      *
      * @param prompt What to show the user
      * @return the 1st word of the line the user entered with spaces
      * and other words stripped out
      * @throws java.io.IOException
      */
     private static String PROMPT(String prompt) throws IOException
     {
         System.out.print(prompt);
         String line = input.readLine();
         // get 1st word
         int start = 0;
         while (start < line.length() && Character.isWhitespace(line.charAt(start)))
         {
             start++;
         }
         int end = start;
         while (end < line.length() && !Character.isWhitespace(line.charAt(end)))
         {
             end++;
         }
 
         // don't execute blank lines
         if (start == end)
         {
             return null;
         }
 
         return line.substring(start, end);
     }
     private static BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
     private static boolean abort = false;
 
     private static void doTest() throws IOException
     {
         String command = PROMPT("acctTest% ");
 
         if (command == null)
         {
             return; // don't execute blank lines
         }
         try
         {
             if ("exit".equals(command))
             {
                 abort = true;
             } else if ("create".equals(command))
             {
                 doCreate();
             } else if ("delete".equals(command))
             {
                 doDelete();
             } else if ("get".equals(command))
             {
                 doGet();
             } else if ("auth".equals(command))
             {
                 doAddAuth();
             } else
             {
                 System.out.println("Unknown Command: " + command);
             }
         } catch (SecurityException ex)
         {
             ex.printStackTrace();
         }
 
     }
 
     private static void doCreate() throws IOException
     {
         String name = PROMPT("AccName?% ");
         String passwd = PROMPT("Password?% ");
 
         getAccountManager().createAccount(name, passwd);
         System.out.println("Created!");
     }
 
     private static void doDelete() throws IOException
     {
         String name = PROMPT("AccName?% ");
 
         getAccountManager().deleteAccount(name);
         System.out.println("Deleted!");
     }
 
     private static void doGet() throws IOException
     {
         String name = PROMPT("AccName?% ");
         String passwd = PROMPT("Password?% ");
 
         Account acc = getAccountManager().getAccount(name);
         acc.authenticate(passwd);
         System.out.println("Gotted!");
         System.out.println("Authorized Features: ");
         for(String s: acc.getAuth()) System.out.println(s);
     }
 
     private static void doAddAuth() throws IOException
     {
         String name = PROMPT("AccName?% ");
         String feature = PROMPT("AuthWhat?% ");
 
         getAccountManager().addAuthorization(feature, name);
         System.out.println("Authorized!");
     }
 
     public static void main(String[] args) throws IOException
     {
         File testDir = new File(defaultDir);
         if (!testDir.exists() && !testDir.mkdirs())
         {
             ABORT("Could not create dir");
         }
         new AccountManager(defaultDir);
         while (!abort)
         {
             doTest();
         }
     }
 }
