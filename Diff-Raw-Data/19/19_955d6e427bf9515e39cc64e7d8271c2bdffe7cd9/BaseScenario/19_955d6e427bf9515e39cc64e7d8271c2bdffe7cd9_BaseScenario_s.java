 /**
  * 
  */
 package org.microworlds.scenario;
 
 import org.microworld.models.Person;
 import org.microworld.robots.Agent;
 import org.microworld.robots.DriverAgent;
 
 /**
  * @author riccardo
  * 
  */
 public class BaseScenario extends Scenario {
 	Person user = new Person();
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.microworlds.scenario.DycapoScenario#setUp()
 	 */
 	@Override
 	public void setUp() {
 		this.user.setUsername("urobo");
		this.user.setPassword("Password");
 		this.user.setEmail("rico.sleeps@gmail.com");
 		this.user.setPhone("030318236");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.microworlds.scenario.DycapoScenario#start()
 	 */
 	@Override
 	public void start() {
 		Agent agent = new DriverAgent(1);
 		agent.setUser(user);
 		agent.register(user);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.microworlds.scenario.DycapoScenario#stop()
 	 */
 	@Override
 	public void stop() {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.microworlds.scenario.DycapoScenario#load(java.lang.String)
 	 */
 	@Override
 	public void load(String resource) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
