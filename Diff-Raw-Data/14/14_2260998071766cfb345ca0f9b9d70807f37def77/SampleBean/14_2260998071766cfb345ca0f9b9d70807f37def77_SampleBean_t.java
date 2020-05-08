 package edu.mit.cci.simulation.ice.beans;
 
import java.util.Date;

 import javax.faces.event.ActionEvent;
 
 import edu.mit.cci.simulation.model.DefaultScenario;
 
 public class SampleBean {
 	
 	private String name = "This is sample name";
 
 	public void setName(String name) {
 	    this.name = name;
     }
 
 	public String getName() {
 	    return name;
     }
 	
 	public long getScenariosCount() {
 		return DefaultScenario.countDefaultScenarios();
 	}
 	
 	
 	public void updateName(ActionEvent e) {
 		// ignore
		try {
		DefaultScenario s = DefaultScenario.findAllDefaultScenarios().get(0);
		
		s.setName("kokojambo! " + new Date());
		s.merge();
		
		System.out.println("merged? " + s.getName());
		}
		catch (Throwable ex) {
			ex.printStackTrace();
		}
 	}
 	
 }
