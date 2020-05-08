 package erekspeed;
 
 import ch.idsia.agents.Agent;
 import ch.idsia.benchmark.mario.environments.Environment;
 
 import java.io.FileInputStream;
 import java.io.ObjectInputStream;
 
 /**
  * Created by IntelliJ IDEA.
  * User: espeed
  * Date: Aug 18, 2009
  * Time: 5:46:34 PM
  * When run as agent loads
  */
 public class CuckooAgentLoader implements Agent {
 
 	CuckooSubAgent agent;
 
 	public CuckooAgentLoader() {
 
 		try {
 			long start, stop, elapsed;
 			start = System.currentTimeMillis();
			FileInputStream is = new FileInputStream("F:\\college\\6867\\project\\benchmark\\best_def_6392.0_805_5_133829_lhb-false_lt-0_13224532");
 			ObjectInputStream in = new ObjectInputStream(is);
 			agent = (CuckooSubAgent) in.readObject();
 			stop = System.currentTimeMillis();
 			elapsed = stop - start;
 
 			System.out.println("Time taken to load:" + elapsed);
 		}
 		catch (Exception ex) {
 			System.err.println(ex);
 		}
 		setName("Cuckoo Agent");
 	}
 
 	public void reset() {
 		agent.reset();
 	}
 
 	public boolean[] getAction() {
 		return agent.getAction();
 	}
 
 	public void integrateObservation(Environment environment) {
 		try {
 			agent.integrateObservation(environment);
 		}
 		catch (Exception ex) {
 			System.out.println("cuckoo agent loader:" + ex.toString());
 		}
 	}
 
 	public void giveIntermediateReward(float intermediateReward) {
 		//To change body of implemented methods use File | Settings | File Templates.
 	}
 
 
 	public String getName() {
 		return agent.getName();
 	}
 
 	public void setName(String name) {
 		agent.setName(name);
 	}
 
 	@Override
 	public void setObservationDetails(int rfWidth, int rfHeight, int egoRow,
 			int egoCol) {
 		// TODO Auto-generated method stub
 		
 	}
 }
