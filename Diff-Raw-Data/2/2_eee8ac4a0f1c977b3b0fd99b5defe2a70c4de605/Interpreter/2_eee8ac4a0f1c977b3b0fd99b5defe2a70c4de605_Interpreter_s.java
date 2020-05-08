 import java.util.LinkedList;
 import java.util.StringTokenizer;
 
 //performs basic functions for interpreting messages
 public class Interpreter {
 
     /**
      * convert a string to a linked list, treating each word as one list object.
      * @param data the string to be converted
      * @return LinkedList<String> the converted data as a list.
      */
     public LinkedList<String> stringToLinkedList(String data) {
         LinkedList<String> words = new LinkedList<String>();
         if (data.isEmpty()) {
             return words;
         }
         StringTokenizer token = new StringTokenizer(data);
         while (token.hasMoreTokens()) {
             words.add(token.nextToken());
         	
         }
         return words;
     }
 
     /**
      * convert a linked list to a string, 
      * adding whitespace between each list object.
      * @param data the list to be converted
      * @return String the converted data as a String.
      */
     public String linkedListToString(LinkedList<String> data) {
         String result = new String();
         result = data.get(0);
         for (int i = 1; i < data.size(); i++) {
             result = result.concat(" " + data.get(i));
         }
         return result;
     }
     
     public String packetDataToString(byte[] data) {
     	String allData = new String(data);
    	return (allData.substring(0, 22));
     }
 }
