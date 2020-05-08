 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package main;
 
 import logic.*;
 import cli.*;
 import java.util.Scanner;
 
 /**
  *
  * @author joel
  */ 
 public class Main {
     
     public static void main(String[] args) {
         Scanner in = new Scanner(System.in);
        System.out.println("Hello Player, what is your name?");
         String pName = in.nextLine();
        System.out.println("Hello " + pName + ", what will your dragon be named?");
         String dName = in.nextLine();
         Dragon dra = new Dragon(dName);
         Pack pac = new Pack(20);
         Item f = new Food("food", "used to feed dragon", 10, 10);
         pac.addItem(f);
         Player pla = new Player(pName, pac, dra);
         PlayerCLI pcli = new PlayerCLI(pla);
         
     }
     
 }
