 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cvrp.classes;
 
 import cvrp.exceptions.TabuListFullException;
 import cvrp.exceptions.*;
 import cvrp.interfaces.Move;
 import cvrp.interfaces.Tabu;
 import cvrp.interfaces.NeighborhoodStructure;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Itera entre todos los vecinos y para cada uno les selecciona una ruta y
  * posicion dentro de la ruta aleatoria.
  *
  * Note que puede insertarse en la misma ruta pero en diferente posicion y que
  * puede dejar rutas vacias como tambien crear nuevas rutas.
  *
  * @author tamerdark
  */
 public class NeighborhoodStructureClassic implements NeighborhoodStructure {
 
   @Override
   public List<Neighbor> generateNeighborhood(Solution s, List<Tabu> tabuList) throws TabuListFullException {
     Instance instance = s.getInstance();
     
     if(instance.getNEIGHBORHOOD_GENERATOR().equals("F"))
       return this.generateFullNeighborhood(s, tabuList);
     else if(instance.getNEIGHBORHOOD_GENERATOR().equals("R"))
       return this.generateRandomNeighborhood(s, tabuList);
     else if(instance.getNEIGHBORHOOD_GENERATOR().equals("G"))
       return this.generateGranularNeighborhood(s, tabuList); 
     return null;
   }
   
   public Neighbor generateNeighbor(Solution s, List<Tabu> tabuList, List<Integer> customers) 
             throws UnexpectedAmountOfCustomersException , TabuListFullException {
     if( customers.size() != 1 ) {
         throw new UnexpectedAmountOfCustomersException();
     }
     int customer = customers.get(0);
     int customersSize = s.getInstance().getCustomersNumber();
     int iterationsWithoutMove = 0;
     while(true) {
         int targetRoute = (int)(Math.random()*customersSize);
         Route r = s.getRoute(targetRoute);
         int positionInsideRoute = (int)(Math.random()*(r.size() - 1)) + 1;
         SingleMove m = new SingleMove(customer, targetRoute, positionInsideRoute);
         // If the route is the same, try to generate another number until
         // it has tried 2* tabulist.size() times.
         if((targetRoute == s.getRouteNumber(customer) && 
             (positionInsideRoute == s.getCustomerPosition(customer) 
           || positionInsideRoute == s.getCustomerPosition(customer) + 1)
            ) || tabuList.contains(m.generateTabu())
           ) {
           if( iterationsWithoutMove > 2*tabuList.size()){
               throw new TabuListFullException();
           }
           ++iterationsWithoutMove;
           continue;
         }
        Neighbor n = new Neighbor(s, m);
        return n;
 
     }       
   }
     
   private List<Neighbor> generateFullNeighborhood(Solution s , List<Tabu> tabuList) throws TabuListFullException {
     Instance instance = s.getInstance();
     int customersNumber = instance.getCustomersNumber();
     List<Neighbor> neighbors = new ArrayList<Neighbor>();
     List<Integer> customer = new ArrayList<Integer>(1);
     for(int i = 1; i <= customersNumber; i++) {
       customer.add(i);
       try {
         neighbors.add(this.generateNeighbor(s, tabuList, customer));
       } catch (UnexpectedAmountOfCustomersException ex) {
         Logger.getLogger(NeighborhoodStructureClassic.class.getName()).log(Level.SEVERE, null, ex);
       } catch (TabuListFullException t){
         throw t;
       }
       customer.clear();
     }
     return neighbors;
   }
 
   private List<Neighbor> generateRandomNeighborhood(Solution s , List<Tabu> tabuList) throws TabuListFullException {
     Instance instance = s.getInstance();
     int customerNumber = instance.getCustomersNumber();
     int randomCustomers = (int)Math.ceil(Math.random()*customerNumber);
     List<Neighbor> neighbors = new ArrayList<Neighbor>();
     List<Integer> customer = new ArrayList<Integer>(1);
     for(int i = 1; i <= randomCustomers; i++) {
       customer.add((int)Math.ceil(Math.random()*customerNumber));
       try {
         neighbors.add(this.generateNeighbor(s, tabuList, customer));
       } catch (UnexpectedAmountOfCustomersException ex) {
         Logger.getLogger(NeighborhoodStructureClassic.class.getName()).log(Level.SEVERE, null, ex);
       } catch (TabuListFullException t){
         throw t;
       }
       customer.clear();
     }
     return neighbors;
   }
 
   private List<Neighbor> generateGranularNeighborhood(Solution s , List<Tabu> tabuList) {
     throw new UnsupportedOperationException("Not supported yet.");
   }
 
 
 }
