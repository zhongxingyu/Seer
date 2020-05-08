 package polep.domain.agent;
 
 import org.springframework.data.neo4j.annotation.NodeEntity;
 
 import agentspring.agent.AbstractAgent;
 import agentspring.agent.Agent;
 import agentspring.role.Role;
 
 @NodeEntity
 public class PolepModel extends AbstractAgent implements Agent {
 
 
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
