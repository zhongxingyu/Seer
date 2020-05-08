 package gui;
 
 
 import java.util.*;
 
 import dungeons.*;
 import gameObjects.*;
 import gameObjects.Abilities.*;
 
 // TODO: Can not import all classes with .*
 import gameObjects.Class.*;
import gameObjects.Class.Berserker;
import gameObjects.Class.Cleric;
import gameObjects.Class.Mage;
import gameObjects.Class.Samurai;

import gameObjects.Items.*;
 
 // TODO: Can not import all races with .*
 import gameObjects.Race.*;
import gameObjects.Race.Elf;
import gameObjects.Race.Human;
import gameObjects.Race.Ork;
 
public class DungeonsAndDragonsGame {
   
   static Random rand = new Random();
   static Scanner input = new Scanner(System.in);
 
   static iPlayerCharacter player;
   static IDungeon dungeon;
   
   
 	public static void main(String[] args)
 	{
 		System.out.println("Welcome to Dungeons and Dragons 21st Century Command Line Style");
 		
 		
 		player = selectRace();
 		player = selectClass(player);
 		
 		
 		System.out.println("Your Character is: Test character");
 		System.out.println("You are entering the XXXX dungeon");
 		
 		do {
 			
 			System.out.println("Please choose from the options below\n");
 			
 			System.out.println("1. Move Character");
 			System.out.println("2. Use Item/Equip Item");
 			System.out.println("3. Move");
 			System.out.println("4. Rest");
 			System.out.println("5. Get");
 			
 			Scanner scanner = new Scanner(System.in);
 			
 			System.out.print("Choice -->");
 			
 			int choice = scanner.nextInt();
 			
 			switch (choice)
 			{
 				case 1: 
 					System.out.println("Move character");
 				break;
 			}
 			
 		} while (true);
 	}
 	
 	public static iPlayerCharacter selectRace() {
 	  int choice;
 	  String name;
 	  
 	  System.out.print("Adventurer, what is your name?");
 	  name = input.next();
 	  
 	  while (true) {  	  
       System.out.println("Please select a race: ");
       System.out.println("1. Elf");
       System.out.println("2. Human");
       System.out.println("3. Ork");
       System.out.print(": ");
       choice = input.nextInt();
     
       switch (choice) {
         case 1: 
           return new Elf(name);
         case 2:
           return new Human(name);
         case 3: 
           return new Ork(name);
       }
     }    
     
 	}
 	
 	
 	public static iPlayerCharacter selectClass(iPlayerCharacter character) {
 	  int choice;
 	  
 	  
     while (true) {  	  
       System.out.println("Please select a class: ");
       System.out.println("1. Berserker");
       System.out.println("2. Cleric");
       System.out.println("3. Mage");
       System.out.println("4. Samurai");
       System.out.print(": ");
       choice = input.nextInt();
     
       switch (choice) {
         case 1: 
           return new Berserker((GameCharacter)character);
         case 2:
           return new Cleric((GameCharacter)character);
         case 3: 
           return new Mage((GameCharacter)character);
         case 4: 
           return new Samurai((GameCharacter)character);
       }
     }    
 	}
 	
 }
