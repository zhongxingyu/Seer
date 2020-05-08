 package org.investovator.controller.command.agent;
 
 import org.investovator.agentsimulation.api.JASAFacade;
 import org.investovator.core.commons.simulationengine.MarketOrder;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * @author Amila Surendra
  * @version $Revision
  */
 public class UnmatchedOrdersCommand extends AgentGameCommand {
 
     private HashMap<String,ArrayList<MarketOrder>> result = null;
     private String userName;
 
     public UnmatchedOrdersCommand(String userName){
         this.userName = userName;
     }
 
     public HashMap<String,ArrayList<MarketOrder>> getOrders(){
         return result;
     }
 
     @Override
     public void execute() {
//        result=facade.getUserUnmatchedOrders(userName);
     }
 }
