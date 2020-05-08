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
        File file = new File("./intro");
         Scanner in = new Scanner(System.in);
        Scanner out = new Scanner(file);
        while (out.hasNext())
        {
            System.out.println(out.nextLine());
        }
         String pName = in.nextLine();
        System.out.println("Oh yes of course " + pName + "!");
        file = new File("./dragon");
        out = new Scanner(file);
        while (out.hasNext())
        {
            System.out.println(out.nextLine());
        }
         String dName = in.nextLine();
         Dragon dra = new Dragon(dName);
        System.out.println(dName + " is a good name for a dragon");
        file = new File("./dragon2");
        out = new Scanner(file);
        while (out.hasNext())
        {
            System.out.println(out.nextLine());
        }
         Pack pac = new Pack(20);
         Item f = new Food("food", "used to feed dragon", 10, 10);
         pac.addItem(f);
         Player pla = new Player(pName, pac, dra);
         PlayerCLI pcli = new PlayerCLI(pla);
         
     }
     
 }
