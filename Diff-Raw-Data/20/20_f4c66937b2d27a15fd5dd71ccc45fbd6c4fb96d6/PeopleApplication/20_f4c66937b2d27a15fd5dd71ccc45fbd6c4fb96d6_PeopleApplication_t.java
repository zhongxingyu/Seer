 /* Author Name: Evan Armour
  * CIS331, Section 2, Fall 2013
  * Homework 4
  * Purpose: This assignment introduces object-oriented concepts, such as 
  * encapsulation and information hiding, as well as method overloading and 
  * manipulating class instances with getters and setters.
 */
 package HW4;
 import javax.swing.*;
 
 public class PeopleApplication {
   public static void main (String args[]) 
   {
     Person.addPerson("Evan", "Armour", 10, "Single", "MaLe");
     Person.addPerson("Rory", "Salzberger", 20, "Single", "MaLe");
     Person.addPerson("jeff", "may", 30, "Single", "MaLe");
     Person.addPerson("susan", "waRNER", 40, "MARRied", "feMaLe");
     Person.addPerson("Gilbert", "Armour", 55, "Single", "MaLe");
     Person.addPerson("Morgan", "Wampler", 60, "Single", "feMaLe");
     Person.addPerson("Amanda", "Farmer", 70, "Single", "feMaLe");
     Person.addPerson("james", "Cameron", 80, "Single", "MaLe");
     Person.addPerson("Andrew", "Andrews", 90, "Single", "MaLe");
     
     menuChoice();
   }
 
   public static void menuChoice()
   {
     int choiceInt = 666;
     
     do
     {
       String choice = JOptionPane.showInputDialog(null, "Choose an option:\n"
             + " 1: Add New Person\n"
             + " 2: Display All Names\n"
             + " 3: Get Person Info\n"
             + " 4: Display Average Age\n"
             + " 5: Quit",
             "Person Managment Console!", 3);
 
       try
       {
         choiceInt = Integer.parseInt(choice);
       }
       catch (Exception e)
       {
         choiceInt = 666;
       }
 
       if(choiceInt > 5)
         JOptionPane.showMessageDialog(null, "ERROR: Incorrect choice or "
                 + "non-integer entered!", "ERROR", 0);
 
       switch(choiceInt)
       {
         case 1: //addperson
           System.out.println("Working!");
           break;
         case 2: //displaynames
          if(Person.getTotPeople() == 0)
          {
            JOptionPane.showMessageDialog(null, "There are no people to get"
                    + " information from!", "ERROR", 0);
            break;
          }
           JOptionPane.showMessageDialog(null, "All persons:\n" + Person.listPersons(), "List of Persons", 1);
           break;
         case 3: //getinfo
          if(Person.getTotPeople() == 0)
          {
            JOptionPane.showMessageDialog(null, "There are no people to get"
                    + " information from!", "ERROR", 0);
            break;
          }
           break;
         case 4: //averageage
          if(Person.getTotPeople() == 0)
          {
            JOptionPane.showMessageDialog(null, "There are no people to get"
                    + " information from!", "ERROR", 0);
            break;
          }
           JOptionPane.showMessageDialog(null, "Average age of all persons is: " 
                   + Person.averageAge(), "Average Age", 1);
           break;
         case 5: //quit
           break;
         default:
             break;
       }
     }while(choiceInt != 5);
   } 
 }
