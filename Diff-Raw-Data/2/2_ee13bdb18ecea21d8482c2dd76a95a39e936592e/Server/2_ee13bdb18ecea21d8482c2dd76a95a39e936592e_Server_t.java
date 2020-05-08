 package org.jlfoster.cosc310.restaraunt;
 import java.util.Random;
 
 public class Server {
 
 	Random randomGenerator = new Random();
 	private	String[] Server = new String[2];
 
 	public void setServer(){
 		Server[0] = "Joseph";
 		Server[1] = "Brittany";
 	}
 
 	public void setServer(String str1, String str2){
 		Server[0] = str1;
 		Server[1] = str2;
 	}
 
 	public String getServer(){
 		int number = randomGenerator.nextInt(100);
		return Server[number%2];
 	}
 
 }
 
