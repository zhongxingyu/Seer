 package polep.domain.agent;
 
 /**
  * @author RubenVerweij
  * In this class the EnergyProducer is defined. This producer has the following properties; cash, a set of power plants, a recency parameter, a experimentation
  * parameter and a price mark up
  */
 
 
 import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

 import polep.domain.technology.PowerPlant;
import agentspring.agent.AbstractAgent;
import agentspring.agent.Agent;
import agentspring.simulation.SimulationParameter;
 
 @NodeEntity
 public class EnergyProducer extends AbstractAgent implements Agent {
 
     
     @SimulationParameter(label = "Agents Cash Balance", from = 1, to = 100)
     private double cash;
     
     @RelatedTo(type = "OWNS", elementClass = PowerPlant.class, direction = Direction.OUTGOING)
     private Set<PowerPlant> powerPlantSet;
     
     @SimulationParameter(label = "The recency parameter", from = 0, to = 1)
     private double recencyParameter;
     
     @SimulationParameter(label = "The experimentation parameter", from = 0, to = 1)
     private double experimentationParameter;
     
     @SimulationParameter(label = "The price markup", from = 0, to = 1)
     private double priceMarkUp;
     
     String name;
 
 	public double getCash() {
 		return cash;
 	}
 
 	public void setCash(double cash) {
 		this.cash = cash;
 	}
 
 	public Set<PowerPlant> getPowerPlantSet() {
 		return powerPlantSet;
 	}
 
 	public void setPowerPlantSet(Set<PowerPlant> powerPlantSet) {
 		this.powerPlantSet = powerPlantSet;
 	}
 
 	public double getRecencyParameter() {
 		return recencyParameter;
 	}
 
 	public void setRecencyParameter(double recencyParameter) {
 		this.recencyParameter = recencyParameter;
 	}
 
 	public double getExperimentationParameter() {
 		return experimentationParameter;
 	}
 
 	public void setExperimentationParameter(double experimentationParameter) {
 		this.experimentationParameter = experimentationParameter;
 	}
 
 	public double getPriceMarkUp() {
 		return priceMarkUp;
 	}
 
 	public void setPriceMarkUp(double priceMarkUp) {
 		this.priceMarkUp = priceMarkUp;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 
 }
