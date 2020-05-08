 package polep.domain.market;
 
 /*
  * Energy Market Agent establishes the value of lost load, which is a price cap in case the market doesn't clear. 
  * It also determines the clearing price, based on a sorting of all the bids received and an existing demand. 
  * 
  *question: is demand set here or is it part of the environment?
  */
 
 import org.springframework.data.neo4j.annotation.NodeEntity;
 
 import agentspring.agent.AbstractAgent;
 import agentspring.agent.Agent;
 
 @NodeEntity
 public class EnergyMarket extends AbstractAgent implements Agent {
 
     
     double clearingPrice;    
     double valueOfLostLoad; 
     double demand; 
 
    
     public double getClearingPrice() {
 		return clearingPrice;
 	}
 
 	public double getValueOfLostLoad() {
 		return valueOfLostLoad;
 	}
 
 
 	public void setValueOfLostLoad(double valueOfLostLoad) {
 		this.valueOfLostLoad = valueOfLostLoad;
 	}
 
 
 	public double getDemand() {
 		return demand;
 	}
 
 
 	//public void setDemand(double demandSet) {
     //    this.demand = demandSet;
     }
 	
 	/*
 	 * 
 	 * Methods
 	 * Search for bids of the current time step from the graph database
 		Sort bids according to bid price
 		Determines market clearing price depending on demand.
 		Store ClearingPrice in database
 		*/
    
 
 }
