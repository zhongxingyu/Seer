 // Name: Ken Whittaker, Matt Kanoc, Brandon Lewis
 // Course: CSC 470, Section 1
 // Semester: Fall 2012
 // Instructor: Dr. Pulimood
 // Collaborative Project: Poll-Friend
 // Description: Maintains users for polls and organize in Hashtable
 // Filename: user.java
 // Last modified on: 12/3/2012
 
 import java.util.*;
 import java.io.*;
 
 public class user	{
 	String name;
 	Hashtable taken;
 	BufferedReader in;
 	BufferedWriter out;
 	
 	//This is the default for a new user
 	//It asks for a username and password
 	public user()	{
 		Scanner kb = new Scanner(System.in);
 		System.out.print("Confirm username: ");
 		name = kb.next();
 		taken = new Hashtable();
 	}
 	
 	//This is when a user tries to login with a username
 	//The program loads the polls taken from the text file
 	public user(String n)	throws java.io.IOException	{
 		name = n;
 		taken = new Hashtable();
 		try	{
 		in = new BufferedReader(new FileReader(name + ".txt"));
 		while (in.ready())	{
 			taken.put(in.readLine(), 1);
 		}
 		} catch (FileNotFoundException ex)	{
 			System.err.println(ex);
 		}
 	}
 	
 	//checks to see if a user has been polled from a certain poll
 	//For a given string, see if it is in a hashtable and return that.
 	public boolean hasPolled(String name)	{
 		return taken.containsKey(name);
 	}
 	
 	//The user has just taken a new poll, and the hashtable needs to be updated
 	public void tookPoll(String st)	{
		taken.put(1, st);
 	}
 	
 	//When a user logs out, their file needs to be updated.
 	public void store()	throws java.io.IOException {
		Collection c = taken.values();
		Iterator tmp = c.iterator();
		out = new BufferedWriter(new FileWriter(new File(name + ".txt"), true));
 		String fullstring = "";
 		while (tmp.hasNext())	{
			String temp = tmp.next().toString();
			System.out.println(temp);
			fullstring = fullstring + temp + "\n";
			System.out.println("I: " + fullstring);
 		}
 		
		out.write(fullstring);
		out.close();
 	}
 }
