 package org.p000ison.dev.commandapi;
 
 import java.util.Arrays;
 
 /**
  * Represents a CallInformation
  */
 public class CallInformation {
 
     private String identifier;
     private String[] arguments;
     private final CommandSender sender;
 
     public CallInformation(String identifier, String[] arguments, CommandSender sender) {
         this.identifier = identifier;
         this.arguments = arguments;
         this.sender = sender;
     }
 
     public String getIdentifier() {
         return identifier;
     }
 
     public String[] getArguments() {
         return arguments;
     }
 
     public CommandSender getSender() {
         return sender;
     }
 
     public void reply(String message) {
         getSender().sendMessage(message);
     }
 
     void removeFirstArgument() {
         if (arguments.length > 0) {
             arguments = removeUntil(arguments, 1);
         }
     }
 
     public static String[] removeUntil(String[] original, int until) {
         String[] newArray = new String[original.length - until];
         System.arraycopy(original, until,       // from array[removeEnd]
                 newArray, 0,                    // to array[removeStart]
                 newArray.length);       // this number of elements
         return newArray;
     }
 
     void setIdentifier(String identifier) {
         this.identifier = identifier;
     }
 
     @Override
     public String toString() {
         return "CallInformation{" +
                 "identifier='" + identifier + '\'' +
                 ", arguments=" + Arrays.toString(arguments) +
                 ", sender=" + sender +
                 '}';
     }
 }
