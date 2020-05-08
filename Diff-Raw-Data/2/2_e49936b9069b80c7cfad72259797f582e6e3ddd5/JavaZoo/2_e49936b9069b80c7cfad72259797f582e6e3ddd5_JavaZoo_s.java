 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package crisostomolab5b;
 
 /**
  *
  * @author arscariosus
  */
import java.io.*;
 public class JavaZoo {
 
     public static String zooName = "Java Park n Zoo";
 
     public JavaZoo()
     {
     }
 
     public static String getZooName()
     {
         return zooName;
     }
 
 
     public static void main(String[] args) throws Exception {
         ZooAnimal myFirstAnimal = new ZooAnimal();
         Cage firstCage = new Cage();
         ZooKeeper myFirstZooKeeper = new ZooKeeper();
         
         myFirstAnimal.setName("Hershey");
         myFirstAnimal.setType("Wild Dog");
         myFirstZooKeeper.setName("John Crisostomo");
         myFirstZooKeeper.setTitle("Engr.John");
 
 
         System.out.println("Welcome to " + zooName + "!");
         System.out.println();
         //ZooKeeper Output
         System.out.println("My name is " + myFirstZooKeeper.getName() + " I am the Zoo Keeper.");
         System.out.println("You can call me  " + myFirstZooKeeper.getTitle());
         System.out.println("I am paid " + myFirstZooKeeper.getPayRate() + "/Hour");
         System.out.println("Do i have a degree  ?  " + myFirstZooKeeper.hasDegree());
 
 
         //Animal Output
         System.out.println();
         System.out.println("Over Here,We have : ");
         System.out.print( myFirstAnimal.getName());
         System.out.println(", he is a " + myFirstAnimal.getType());
         System.out.println(myFirstAnimal.getName()+ " is "+ myFirstAnimal.getAge() + " Years old");
         System.out.println("is "+ myFirstAnimal.getName() + " Hungry? " + myFirstAnimal.isHungry());
 
 
         //Cage Output
         System.out.println();
         System.out.print(myFirstAnimal.getName() + "'s habitat is a cage with ");
         System.out.print("a Length of " + firstCage.getLength());
         System.out.print(", a Width of " + firstCage.getWidth());
         System.out.println(" and a Height : " + firstCage.getHeight());
         System.out.print("As we can see " + myFirstAnimal.getName() + "'s ");
         firstCage.cleaned();
         System.out.println("Can we say that the Cage is Cleaned? --> " + firstCage.isClean());
         System.out.println("but is the Cage Covered? --> " + firstCage.isCovered());
 
         //Second Animal, using the other constructor
         ZooAnimal mySecondAnimal = new ZooAnimal("Kangaskhan","Kangaroo",5,false);
         ZooKeeper mySecondZooKeeper = new ZooKeeper("Pepe Smith", "Mr. Rock n Roll", 15.0, false);
         Cage secondCage = new Cage(15,20,15,false,true);
 
         System.out.println("Everyone! Look! " + myFirstAnimal.getName() + " is eating");
         myFirstAnimal.eat();
         System.out.println("Lets try to feed it again...");
         myFirstAnimal.eat();
         System.out.println("Wow, it looks like its sleepy.");
         myFirstAnimal.sleep();
 
         System.out.println("Lets Move On.");
 
         System.out.println();
         System.out.println("Hi! I'm " + mySecondZooKeeper.getName() + " I will be assisting " + myFirstZooKeeper.getTitle());
         System.out.println("You can Call me " + mySecondZooKeeper.getTitle());
         System.out.println("I am Paid " + mySecondZooKeeper.getPayRate() + "/Hour");
         System.out.println("do i have a degree?? --> " + mySecondZooKeeper.hasDegree());
 
 
         System.out.println();
         System.out.println("Here we Have " + mySecondAnimal.getName() + " a " + mySecondAnimal.getType() + " he is currently " + mySecondAnimal.getAge() + " years old.");
         System.out.println("is " + mySecondAnimal.getName() + " hungry? --> " + mySecondAnimal.isHungry());
         System.out.print("Let's try feeding " + mySecondAnimal.getName() + " !");
         System.out.println();
         mySecondZooKeeper.feedAnimal();
         System.out.println("Oops! it looks like we overfed him. and is pooping");
         System.out.println();
         mySecondZooKeeper.cleanCage();
         System.out.println("This is a huge cage! it measures "+ secondCage.getLength()+ " X "+secondCage.getWidth()+" X "+secondCage.getHeight());
         System.out.println("Is the Cage Clean? --> " + secondCage.isClean());
         System.out.println("Is the Cage Covered? --> " + secondCage.isCovered());
 
         System.out.println();
         System.out.println("Look! " + myFirstAnimal.getName() + " is awake again.");
         System.out.println("is " + myFirstAnimal.getName() + " Hungry? --> " + myFirstAnimal.isHungry());
     }   
 
 
 }
