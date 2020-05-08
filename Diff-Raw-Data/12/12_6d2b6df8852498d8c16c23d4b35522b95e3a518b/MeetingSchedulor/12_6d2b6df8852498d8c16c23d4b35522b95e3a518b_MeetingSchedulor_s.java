 package org.janusproject.demos.meetingscheduler.gui;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.janusproject.demos.meetingscheduler.agent.BaseAgent;
 import org.janusproject.kernel.Kernel;
 import org.janusproject.kernel.address.AgentAddress;
 import org.janusproject.kernel.util.sizediterator.SizedIterator;
 
 public class MeetingSchedulor {
 
 	private Kernel kernel;
 	private mainFrame mainFrame;
 	private Map<String, AgentFrame> mapping;
 
 	public MeetingSchedulor(Kernel kernel) {
 		this.kernel = kernel;
 		this.mainFrame = new mainFrame();
 		this.mainFrame.setSchedulor(this);
 		this.mapping = new TreeMap<String, AgentFrame>();
 	}
 	
 	public void start() {
 		this.mainFrame.setVisible(true);
 	}
 
 	
 	public List<String> getAllAgents(){
 		List<String> agents = new ArrayList<String>();
 		for (SizedIterator<AgentAddress> iterator = this.kernel.getAgents(); iterator.hasNext();) {
 			AgentAddress aagent = iterator.next();
			agents.add(aagent.getName());
 		}
		return agents;		
 	}
 	
 	public void addAgent(String name){
 		BaseAgent agent = new BaseAgent();
 		this.kernel.submitLightAgent(agent, name);
 		this.kernel.launchDifferedExecutionAgents();
 		AgentFrame aframe = new AgentFrame(agent);
 		mapping.put(name, aframe);
 	}
 	
 	public void showAgentFrame(String name){
 		
 	}
 	
 
 }
