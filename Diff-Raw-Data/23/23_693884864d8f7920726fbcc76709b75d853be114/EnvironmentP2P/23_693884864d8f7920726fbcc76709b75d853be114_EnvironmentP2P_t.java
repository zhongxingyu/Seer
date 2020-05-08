 package P2PAgent;
 
 import java.util.ArrayList;
 
 import graphics.AbstractGridViewerWhitoutLocation;
 import logging.Logger;
 import scenario.P2PScenario;
 import KCAAgent.KCAAgent;
 import agent.AbstractAgent;
 import agent.AgentID;
 import agent.Location;
 import base.Environment;
 
 
 public class EnvironmentP2P extends Environment<SimulationP2P, P2PAgent>
 {
 	
 	public EnvironmentP2P(SimulationP2P parent, P2PScenario scenario)
 	{
 		this.parent=parent;
 		logger = new Logger();
 		selected = new ArrayList<AbstractAgent>();
 		agents= scenario.getAgents();
 		this.x=0;
 		this.y=0;
 		this.height=31;
 		this.width=31;
 		
 		// we initialize the agents
 		for(P2PAgent agent : agents.values()){
 			agent.setParent(this);
 			agent.setContacts(scenario.getContacts().get(agent.getId()));
 		}
 	}
 
 	public AgentID injectItem(AgentID agentID, Item item)
 	{
 		if(!agents.get(agentID).getItems().contains(item))
 		{
 			agents.get(agentID).getItems().add(item);
 			P2PAgent.setNumberItem(new Integer(P2PAgent.getNumberItem().intValue()+1));
 		}
 		return agentID;
 	}
 	
 	public AgentID injectItemWanted(AgentID agentID, Item itemWanted)
 	{
 		if((!agents.get(agentID).getItemsWanted().contains(itemWanted))&&(!agents.get(agentID).getItems().contains(itemWanted)))
 		{
 			agents.get(agentID).getItemsWanted().add(itemWanted);
 			P2PAgent.setNumberItemWanted(new Integer(P2PAgent.getNumberItem().intValue()+1));
 		}
 		return agentID;
 	}
 
 	@Override
 	public P2PAgent cellAt(double x, double y)
 	{
 		// TODO Auto-generated method stub
 		Location loc = new Location(x, y);
 		P2PAgent res = null;
 		double minDist = Double.POSITIVE_INFINITY;
 		for (P2PAgent cell : agents.values()) {
 			
 			double dist = loc.getDistance(AbstractGridViewerWhitoutLocation.getLocationOnTheGrid().get(cell));
 			if (dist < minDist) {
 				minDist = dist;
 				res = cell;
 			}
 		}
 		return res;
 	}
 	
 }
 
