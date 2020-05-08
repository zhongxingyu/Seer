 package edu.wpi.tmathmeyer.security.network.memory;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 public class UserFileManager {
 	
 	/**
 	 * 
 	 * @param name the username to check for
 	 * @return whether this user exists
 	 */
 	public static boolean availableUsername(String name){
		mkdir();
 		File f = new File("users/"+name+".cu");
 		return !f.exists();
 	}
 	
 	
 	
 	
 	/**
 	 * 
 	 * @param name the username of the player
 	 * @param password the password of the player
 	 * @return whether the password was correct
 	 */
 	public static boolean login(String name, byte[] password){
		mkdir();
 		File f = new File("users/"+name+".cu");
 		try{
 			BufferedReader br = new BufferedReader(new FileReader(f));
 			String line;
 			while((line = br.readLine()) != null){
 				if (line.startsWith("password:")){
 					line = line.substring(9);
 					return password == getBytes(line);
 				}
 			}
 			throw new Exception("this user was found, yet there was no record of a password");
 		}
 		catch(Exception e){
 			return false;
 		}
 	}
 	
 	
 	public static byte[] getHashedPassword(String name){
		mkdir();
 		File f = new File("users/"+name+".cu");
 		try{
 			BufferedReader br = new BufferedReader(new FileReader(f));
 			String line;
 			while((line = br.readLine()) != null){
 				if (line.startsWith("password:")){
 					line = line.substring(9);
 					return getBytes(line);
 				}
 			}
 			throw new Exception("this user was found, yet there was no record of a password");
 		}
 		catch(Exception e){
 			return new byte[512];
 		}
 	}
 	
 	
 	
 	
 	/**
 	 * 
 	 * @param name the username
 	 * @param password the password as a byte array
 	 * @return
 	 */
 	public static boolean register(String name, byte[] password){
		mkdir();
 		try{
			if (!availableUsername(name))throw new Exception();
 			File f = new File("users/"+name+".cu");
 			f.createNewFile();
 			FileWriter fstream = new FileWriter(f);
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.write("password:"+getString(password));
 			out.close();
 			return true;
 		}
 		catch(Exception e){
 			return false;
 		}
 	}
 	
	public static void mkdir(){
		File f = new File("users/");
		f.mkdir();
	}
	
 	private static String getString(byte[] b){
 		StringBuilder sb = new StringBuilder();
 		for(int i=0;i<b.length;i++)
 			sb.append(b[i]+(i==b.length-1 ?"":","));
 		return sb.toString();
 	}
 	
 	private static byte[] getBytes(String s){
 		String[] byt = s.split(",");
 		byte[] b = new byte[byt.length];
 		for(int i=0;i<b.length;i++)
 			b[i] = Byte.parseByte(byt[i]);
 		return b;
 	}
 }
