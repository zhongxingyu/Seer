 package fr.utc.nf28.moka;
 
 import fr.utc.nf28.moka.environment.MokaEnvironment;
 import jade.core.ProfileImpl;
 import jade.core.Runtime;
 import jade.wrapper.AgentContainer;
 
 public class Launcher {
 	public static void main(String[] args) {
 		final Runtime runtime = Runtime.instance();
 		try {
 			final AgentContainer container = runtime.createMainContainer(new ProfileImpl("profile.txt"));
 			final MokaEnvironment environment = MokaEnvironment.getInstance();
			container.createNewAgent("ConnectionAgent", "fr.utc.nf28.moka.agents.connection.ConnectionAgent",
					new Object[]{environment}).start();
			container.createNewAgent("ItemCreationAgent", "fr.utc.nf28.moka.agents.itemcreation.ItemCreationAgent",
					new Object[]{environment}).start();
			container.createNewAgent("ItemEditionAgent", "fr.utc.nf28.moka.agents.itemedition.ItemEditionAgent",
					new Object[]{environment}).start();
			container.createNewAgent("ItemLockingAgent", "fr.utc.nf28.moka.agents.itemedition.ItemLockingAgent",
					new Object[]{environment}).start();
			container.createNewAgent("WebSocketAgent", "fr.utc.nf28.moka.agents.websocket.WebSocketAgent",
					null).start();
			container.createNewAgent("RestServerAgent", "fr.utc.nf28.moka.agents.rest.RestServerAgent",
					null).start();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
