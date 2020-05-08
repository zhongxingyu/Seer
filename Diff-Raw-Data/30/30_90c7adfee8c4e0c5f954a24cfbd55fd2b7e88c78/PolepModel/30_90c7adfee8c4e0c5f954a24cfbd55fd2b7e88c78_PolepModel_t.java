 package polep.domain.agent;
 
 import org.springframework.data.neo4j.annotation.NodeEntity;
 
 import agentspring.agent.AbstractAgent;
 import agentspring.agent.Agent;
 import agentspring.role.Role;
import agentspring.simulation.SimulationParameter;
 
 @NodeEntity
 public class PolepModel extends AbstractAgent implements Agent {
	
	@SimulationParameter(label = "Agents Cash Balance", from = 0, to = 3000)
	int simulationLength;

 
	public int getSimulationLength() {
		return simulationLength;
	}

	public void setSimulationLength(int simulationLength) {
		this.simulationLength = simulationLength;
	}
 
 	@Override
 	public String getName() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void setName(String arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 
 }
