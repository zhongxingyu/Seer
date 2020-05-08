 package control;
 
 import instructions.BaseInstruction;
 import java.util.ArrayList;
 import java.util.List;
 import exceptions.IllegalInstructionException;
 
 
 public class Preparser {
     private Environment myEnvironment;
 
     public Preparser (Environment environment) {
         myEnvironment = environment;
     }
 
     private class ReturnValues {
         public List<String> list;
         public int counterChange;
 
         public ReturnValues (List<String> rest, int counter) {
             list = rest;
             counterChange = counter;
         }
     }
 
     /**
      * Takes original string of commands and adds list notations to make it
      * easier to parse later.
      * 
      * @param s original commands
      * @return commands with brackets added 
      */
     public String addBrackets (String s) throws IllegalInstructionException {
         String[] words = s.split("\\s+");
        if (words.length < 3)
            return s;
         List<String> wordsList = new ArrayList<String>();
         for (int i = 0; i < words.length; i++) {
             wordsList.add(words[i]);
         }
 
         ReturnValues rv = recurse(wordsList, 1);
         wordsList = rv.list;
        
 
         StringBuilder sb = new StringBuilder();
         for (String str : wordsList) {
             sb.append(str);
             sb.append(" ");
         }
        String allBracketsAdded = sb.toString();
        System.out.println(allBracketsAdded);
        return allBracketsAdded;
     }
 
     private ReturnValues recurse (List<String> wordsList, int argCount)
                                                                        throws IllegalInstructionException {
         List<String> restOfList = new ArrayList<String>();
         int counter = 0;
         for (int i = 0; i < argCount; i++) {
             String command = wordsList.get(counter);
            if (command.charAt(0) == '[' || command.charAt(0) == ']') {
                counter++;
                i--;
            }
            else if (getArgumentCount(command) == -1) {
                 counter++;
             }
             else {
                 wordsList.add(counter, "[");
                 counter++;
                 restOfList = createRestOfList(wordsList, counter);
                 ReturnValues rv = recurse(restOfList, getArgumentCount(command));
                 counter += rv.counterChange;
                 merge(wordsList, rv.list);
                 counter++;
                 wordsList.add(counter, "]");
                 counter++;
             }
         }
         return new ReturnValues(wordsList, counter);
     }
 
     private int getArgumentCount (String s) throws IllegalInstructionException {
         try {
             BaseInstruction base = myEnvironment.systemInstructionSkeleton(s);
             return base.getNumberOfArguments();
         }
         catch (IllegalInstructionException e) {
            if (isNumber(s) || s.charAt(0) == ':') {
                 return -1;
             }
             else {
                 throw new IllegalInstructionException(s);
             }
         }
     }
 
     private boolean isNumber (String s) {
         try {
             Integer.parseInt(s);
             return true;
         }
         catch (NumberFormatException e) {
             return false;
         }
     }
 
     private List<String> createRestOfList (List<String> wordsList, int index) {
         List<String> result = new ArrayList<String>();
         for (int i = index + 1; i < wordsList.size(); i++) {
             result.add(wordsList.get(i));
         }
         int counter = wordsList.size() - index - 1;
         for (int i = 0; i < counter; i++) {
             wordsList.remove(index + 1);
         }
         return result;
     }
 
     private void merge (List<String> wordsList, List<String> restOfList) {
         for (String s : restOfList) {
             wordsList.add(s);
         }
     }
 
 }
