 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ui;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Scanner;
 import matrix.Matrix;
 
 /**
  *
  * @author lasse
  */
 public class UserInterface {
 
     private Scanner scanner;
     private HashMap<String, Matrix> variables;
 
     private UserInterface() {
         scanner = new Scanner(System.in);
         variables = new HashMap<>();
     }
 
     public static UserInterface getInstance() {
         return UserInterfaceHolder.INSTANCE;
     }
 
     private static class UserInterfaceHolder {
 
         private static final UserInterface INSTANCE = new UserInterface();
     }
 
     HashMap<String, Matrix> getVariables() {
         return variables;
     }
 
     public void run() {
         printWelcome();
         while (chooseAction());
         printExit();
     }
 
     private void printWelcome() {
         System.out.println("Welcome! For instructions type \"help\".");
     }
 
     private void printExit() {
         System.out.println("Bye bye!");
     }
 
     private void printHelp() {
         System.out.println("Help is not yet implemented...");
     }
 
     private boolean chooseAction() {
         System.out.print(">> ");
         String command = scanner.nextLine().trim().toLowerCase();
         if (isExitCommand(command)) {
             return false;
         }
 
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
 
     private boolean isExitCommand(String command) {
         return command.equals("exit") || command.equals("quit");
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
             System.out.print(list.get(i));
             if (variables.get(list.get(i)) == null) {
                 System.out.println("\t\t(null)");
             } else {
                 System.out.println();
             }
         }
     }
 
     private void performVariableCommand(String command) {
         Matrix result;
         String varName = null;
         if (command.contains("=")) {
             String[] split = command.split("=");
             if (split.length > 2) {
                 System.err.println("Too many '=' in command!");
                 return;
             }
             varName = split[0].trim();
             if (!isValidVariableName(varName)) {
                 System.err.println("'" + varName + "' not a valid variable name!");
                 return;
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
             result.print();
         }
     }
 
     private Matrix calculateCommand(String command) {
         if (command.length() < 1) {
             System.err.println("Unrecoginzed command!");
             return null;
         }
 
         command = command.trim();
 
         if (variables.containsKey(command)) {
            return new Matrix(variables.get(command));
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
 
         Matrix result = new CommandProcesser().processCommand(split);
         
         return result;
     }
 }
