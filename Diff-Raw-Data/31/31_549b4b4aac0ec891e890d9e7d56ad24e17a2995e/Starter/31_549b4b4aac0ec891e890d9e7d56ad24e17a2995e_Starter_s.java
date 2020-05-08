 package de.haw.tt2p.trafficcoordination.start;
 
 import org.openspaces.core.GigaSpace;
 import org.openspaces.core.GigaSpaceConfigurer;
 import org.openspaces.core.space.UrlSpaceConfigurer;
 
 import com.j_spaces.core.IJSpace;
 
 import de.haw.tt2p.trafficcoordination.game.TrafficManager;
 import de.haw.tt2p.trafficcoordination.topology.RoxelManager;
 import de.haw.tt2p.trafficcoordination.visualization.GUI;
 
 /**
  * Starts the Simulation. <br>
  * <br>
  *
  * A space with name 'traffic' must already be running:<br>
  * In '\gigaspaces\bin' start 'gs-agent.bat' and 'gs.ui.bat' for deployment.
  */
 public class Starter {
 
 	public static void main(String[] args) {
		IJSpace space = new UrlSpaceConfigurer("jini://*/*/traffic").space();
 		GigaSpace gigaSpace = new GigaSpaceConfigurer(space).gigaSpace();
 		RoxelManager roxelManager = new RoxelManager(gigaSpace);
 		roxelManager.init();
 		TrafficManager trafficManager = new TrafficManager(gigaSpace, 10);
		new GUI(gigaSpace, trafficManager);
 	}
 
 }
