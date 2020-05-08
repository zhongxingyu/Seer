 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package rpsls;
 
 import java.util.Scanner;
 
 /**
  *
  * @author Jeremy
  */
 public class Attacks {
     int rock = 1;
     int paper = 2;
     int scissors = 3;
     int lizard = 4;
     int spock = 5;
     int attack;
     
     
     public void getAttack() {
         Scanner input = new Scanner(System.in);
         System.out.println("Enter your attack (1-5): ");
         this.attack = input.nextInt();
        if (this.attack <= 5){
         System.out.println("\nYour attack is " + this.attack + ".");
         } 
         else{
             System.out.println("Illegal attack. Please try again.");
             getAttack();
             
         }
         
     }
 }
 
 
 // 
