 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ui;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 
 /**
  *
  * @author Lasse
  */
 class CommandParser {
 
     private HashMap<String, Variable> variables;
     private static final char[] operators = new char[]{'+', '-', '*', '^'};
     private static final String operatorRegEx = "[+*^-]";
 
     public CommandParser() {
         variables = new HashMap<>();
     }
 
     public HashMap<String, Variable> getVariables() {
         return variables;
     }
 
     void ClearVariables() {
         variables.clear();
     }
 
     boolean parseCommand(String command) {
         switch (command) {
             case "help":
                 printHelp();
                 break;
             case "vars":
                 printVariableNames();
                 break;
             case "clear":
                 variables.clear();
                 break;
             default:
                 performVariableCommand(command);
         }
         return true;
     }
 
     private void printHelp() {
        System.out.println("Help is not yet implemented...");
     }
 
     private boolean isValidVariableName(String varName) {
         if (!Character.isLetter(varName.charAt(0))) {
             return false;
         }
         for (int i = 1; i < varName.length(); i++) {
             if (!Character.isLetterOrDigit(varName.charAt(i))) {
                 return false;
             }
         }
         return true;
     }
 
     private void printVariableNames() {
         if (variables.isEmpty()) {
             System.out.println("No variables in use.");
             return;
         }
         ArrayList<String> list = new ArrayList<>(variables.keySet());
         Collections.sort(list);
         System.out.println("Variables in use:");
         for (int i = 0; i < list.size(); i++) {
             String var = list.get(i);
             System.out.print(var);
             if (variables.get(var) == null) {
                 System.out.println("\t\t(null)");
             } else {
                 System.out.println("\t\t(" + variables.get(var).getType() + ")");
             }
         }
     }
 
     boolean performVariableCommand(String command) {
         command = command.trim();
         if (command.isEmpty()) {
             return true;
         }
         if (variables.containsKey(command)) {
             print(command, variables.get(command));
         }
         if (command.startsWith("disp(") && (command.endsWith(")") || command.endsWith(");"))) {
             if (command.endsWith(")")) {
                 display(command.substring(5, command.length() - 1));
                 return true;
             } else if (command.endsWith(");")) {
                 display(command.substring(5, command.length() - 2));
                 return true;
             }
         }
         Variable result;
         String varName = "ans";
         if (command.contains("=")) {
             String[] split = command.split("=");
             if (split.length > 2) {
                 System.err.println("Too many '=' in command!");
                 return false;
             }
             varName = split[0].trim();
             if (!isValidVariableName(varName)) {
                 System.err.println("'" + varName + "' not a valid variable name!");
                 return false;
             }
             command = split[1];
         }
 
         boolean print = !command.endsWith(";");
         if (!print) {
             command = command.substring(0, command.length() - 1);
         }
 
         result = calculateCommand(command);
 
         if (varName != null && result != null) {
             variables.put(varName, result);
         }
 
         if (print && result != null) {
             print(varName, result);
         }
 
         return result != null;
     }
 
     private Variable calculateCommand(String command) {
         if (command.length() < 1) {
             System.err.println("Unrecoginzed command!");
             return null;
         }
 
         command = command.trim();
 
         if (variables.containsKey(command)) {
             return variables.get(command);
         }
 
         if (ParseUtils.isDouble(command)) {
             return new Variable(ParseUtils.parseDouble(command));
         }
 
         if (!command.endsWith(")")) {
             System.err.println("Unrecognized command!");
             return null;
         }
 
         String[] split = command.split("\\(");
         int ind = split.length - 1;
         split[ind] = split[ind].substring(0, split[ind].length() - 1);
 
         if (split.length != 2) {
             System.err.println("Unrecognized command!");
             return null;
         }
 
         Variable result = new CommandProcesser(variables).processCommand(split);
 
         if (result != null && result.getType() == null) {
             result = null;
         }
 
         return result;
     }
 
     private void print(String varName, Variable result) {
         System.out.println();
         System.out.println(varName + " =");
         result.print();
     }
 
     private boolean display(String param) {
         String[] split = param.split(",");
         if (split.length != 1) {
             System.err.println("Function param expects 1 parameter!");
             return false;
         }
         if (param.startsWith("'") && param.endsWith("'")) {
             System.out.println();
             System.out.println(param.substring(1, param.length() - 1));
             System.out.println();
         } else if (variables.containsKey(param)) {
             variables.get(param).print();
         } else if (ParseUtils.isDouble(param)) {
             new Variable(ParseUtils.parseDouble(param)).print();
         } else {
             System.err.println("Parameter in function disp not a variable or a number!");
             return false;
         }
         return true;
     }
 }
