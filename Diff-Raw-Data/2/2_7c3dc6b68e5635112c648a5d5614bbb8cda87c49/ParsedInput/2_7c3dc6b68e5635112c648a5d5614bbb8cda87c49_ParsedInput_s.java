 package mud.network.server.input.interpreter;
 
 import java.util.ArrayList;
 
 /**
  * Parsed client input is generated when receiving a message from a client. This
  * provides quick and easy to the original client message, meta-data about the
  * message, or specific parts of it.
  *
  * @author Japhez
  */
 public class ParsedInput {
 
     private String originalInput;
     private ArrayList<String> words;
     private int wordCount;
 
     public ParsedInput(String input) {
         //Save the original input for access later if necessary
         this.originalInput = input;
         //Create an array of words
         this.words = getAllWords(input);
         //Record the number of words
         wordCount = words.size();
     }
 
     public String getOriginalInput() {
         return originalInput;
     }
 
     public int getWordCount() {
         return wordCount;
     }
 
     public ArrayList<String> getWords() {
         return words;
     }
 
     /**
      * Returns a string consisting of the words starting at the passed word
      * index, or null if out of bounds.
      *
      * For example: Passing 1 returns the first word and all following words.
      *
      * @param startingWordNumber
      * @return the words from the starting index to the end, or null if out of
      * bounds
      */
     public String getWordsStartingAtIndex(int startingWordNumber) {
         int size = words.size();
         if (startingWordNumber <= size) {
             String result = "";
             for (int i = startingWordNumber; i < size; i++) {
                 result += words.get(i) + " ";
                 System.out.println("adding " + words.get(i));
             }
             //Trim edge whitespace
             result = result.trim();
             return result;
         } else {
             return null;
         }
     }
 
     /**
      * Breaks the passed input into individual words and returns them in a
      * ArrayList of Strings.
      *
      * @param input the input to break up
      * @return an ArrayList of the passed String as individual words
      */
     private ArrayList<String> getAllWords(String input) {
         input = input.trim();
         ArrayList<String> results = new ArrayList<>();
         int wordStartIndex = 0;
         int wordEndIndex = 0;
         while (wordEndIndex < input.length()) {
             //Move end index up until word is done
             while (wordEndIndex < input.length() && input.charAt(wordEndIndex) != ' ') {
                 wordEndIndex++;
             }
             results.add(input.substring(wordStartIndex, wordEndIndex));
             //Bring word start index up to end
             wordStartIndex = wordEndIndex;
             //Skip whitespace until next word start is found
             while (wordStartIndex < input.length() && input.charAt(wordStartIndex) == ' ') {
                 wordStartIndex++;
             }
             //Move end index up to start index
             wordEndIndex = wordStartIndex;
         }
         return results;
     }
 
     /**
      * @return the first word of the message
      */
     public String getFirstWord() {
         return words.get(0);
     }
 
     /**
      * @param wordIndex
      * @return the word at the passed index, or null if out of bounds
      */
     public String getWordAtIndex(int wordIndex) {
         //Make sure the word is in bounds of the array
        if (wordIndex < (wordCount - 1) && wordIndex > -1) {
             return words.get(wordIndex);
         }
         return null;
     }
 }
