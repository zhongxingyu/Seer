 package com.jjensen.labs.enumcalc;
 
 import java.util.Scanner;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jjensen
  * Date: 10/12/13
  * Time: 7:41 PM
  */
 public class Calculator implements Runnable{
     private Scanner inputReader;
     private CalculatorEnum calculatorOperation;
     private double number1, number2, total;
     private boolean validInput;
     private char operator;
 
     @Override
     public void run() {
 
         displayGreeting("Welcome to Simple Calculator \nCreated by: Jonathan Jensen\n");
         do {
             do {
                 number1 = promptUserForNumberGetInt();
                 if(number1 != 0) {
                     validInput = true;
                 } else {
                     validInput = false;
                 }
             } while (!validInput);
 
             operator = promptUserAndGetOperator();
             calculatorOperation = getCalcOperation(operator);
 
            if(number2 != 0) {
                total = calculatorOperation.calculate(number2, number1);
                 System.out.println("Sub Total = " + total);
             }
 
             number2 = number1;
 
 
         } while(operator != '=');
 
         System.out.println("Total = " + total);
 
 
     }
 
 
 
     public Calculator() {
         inputReader = new Scanner(System.in);
         validInput = false;
         number1 = 0;
         number2 = 0;
     }
 
     private void displayGreeting(String greeting) {
         System.out.println(greeting);
     }
 
     public int promptUserForNumberGetInt(){
         promptUser("Enter number:");
         return getInt(getUserInput());
     }
 
     private void promptUser(String message) {
         System.out.println(message);
     }
 
     private String getUserInput(){
         return inputReader.nextLine();
     }
 
     private int getInt(String input){
 
         int result = 0;
         try {
             result = Integer.parseInt(input);
         } catch (NumberFormatException numberException) {
             System.out.println("Invalid input.");
 
         }
 
         return result;
     }
 
     private char getChar(String input) {
        return input.charAt(0);
     }
 
     private char promptUserAndGetOperator() {
         promptUser("Enter operator (ex: *, +, /, -, =): ");
         return getChar(getUserInput());
     }
 
     private CalculatorEnum getCalcOperation(char operator) {
         CalculatorEnum calculatorOperation;
 
         if(operator == '+') {
            calculatorOperation = CalculatorEnum.ADD;
 
         }  else if(operator == '-') {
             calculatorOperation = CalculatorEnum.SUBTRACT;
 
         }  else if(operator == '*') {
            calculatorOperation = CalculatorEnum.MULTIPLY;
 
         }   else {
             calculatorOperation = CalculatorEnum.DIVIDE;
         }
 
         return calculatorOperation;
     }
 }
