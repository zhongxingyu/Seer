 package edu.mit.cci.simulation.ice.beans;
 
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
 	}
 	
 }
