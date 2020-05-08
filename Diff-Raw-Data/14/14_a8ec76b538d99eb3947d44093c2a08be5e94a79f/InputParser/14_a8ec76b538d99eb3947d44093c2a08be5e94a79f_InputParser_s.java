 package client;
 
 import shared.*;
 import java.util.*;
 import java.io.*;
 
 public class InputParser {
     
     /**
      * Client Actions
      */
     public static final String LIST = "/list";
 
     public static final String EXIT = "/exit";
     
     public static final String WHISPER = "/whisper";
     
 
     /**
      * Parse message
      */
     public static ClientMessage parse(ChatClient client, String message) {
 	
 	if (message.startsWith(LIST)) return parseListMessage(client, message);
 	else if (message.startsWith(EXIT)) return parseExitMessage(client, message);
 	else if (message.startsWith(WHISPER)) return parseWhipserMessage(client, message);
 	
 	else {
 	    return MessageMaker.sayMessage(client.getID(),
 					    UUID.randomUUID(),
 					    message);
 	}
     }
 
     private static ClientMessage parseListMessage(ChatClient client, String message) {
 	return MessageMaker.listMessage(client.getID(), UUID.randomUUID());
     }
 
     private static ClientMessage parseExitMessage(ChatClient client, String message) {
 	return null;
     }
 
     private static ClientMessage parseWhipserMessage(ChatClient client, String message) {
 	String[] parts = message.split(" ");
 	if (parts.length < 3) {
 	    return null;
 	}
 	String text = "";
 	for (int i = 2; i < parts.length; i++) {
	    text += parts[i];
 	}
 	return MessageMaker.whisperMessage(client.getID(), parts[1], UUID.randomUUID(),
 					   text);
 
     }
     
 }
