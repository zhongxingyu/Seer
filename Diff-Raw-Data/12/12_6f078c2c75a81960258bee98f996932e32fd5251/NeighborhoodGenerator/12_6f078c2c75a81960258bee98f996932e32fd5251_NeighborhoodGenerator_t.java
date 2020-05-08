 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cvrp.abstracts;
 
 import cvrp.classes.Neighbor;
 import cvrp.classes.Solution;
 import cvrp.exceptions.TabuListFullException;
 import cvrp.interfaces.Tabu;
 import java.util.List;
 
 /** A neighborhood generator determines how many neighbors and how they are going to be selected
  * in order to make a neighborhood from a given solution.
  * 
  * Examples of classes that implements this interface could be:
  * 
  * FullNeighborhoodGenerator generate all the neighborhoods in the search space
  * RandomNeighborhoodGenerator generate a random subset of the neighborhoods in the search space
  * GranularNeighborhoodGenerator only short arcs are considered to generate neighborhoods. *
  *
  * @author tamerdark
  */
 public abstract class NeighborhoodGenerator {
     
     /** Generate the whole neighborhood and then apply a filter to return a subset
      * of them.
      * 
      * @param s 
      * @param tabuList
      * @return 
      */
     public abstract List<Neighbor> generateNeighborhood(Solution s , List<Tabu> tabuList)
             throws TabuListFullException;
     
    
     
     public int [] generateCustomers(int customersNumber, int requiredCustomers) {
       
       int [] customers = new int [arraySize(requiredCustomers, customersNumber)];
         
       int k = 0;
       
      for(int i = 1; i <= customersNumber; i++) {
         
         if(requiredCustomers == 1) {
           customers[k] = i;
           k++;
         }
         else if(requiredCustomers == 2) {
          for(int j = i+1; j <= customersNumber; j++) {
             customers[k] = i;
             customers[k+1] = j; 
             k = k + 2;
           }
         } 
       }
       return customers;
     }
     
     private int arraySize(int requiredCustomers, int customersNumber) { 
       int size = 0;
       if(requiredCustomers == 1)
         size = customersNumber;
       else if(requiredCustomers == 2) {
         int s = 0;
         for(int i = 1; i < customersNumber; i++)
           s = s + (customersNumber - i);
         size = s;
       }
       return size*requiredCustomers;
     }
 }
