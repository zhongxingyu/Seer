 package com.janrain.steven.lztesting;
 
 import org.apache.log4j.Logger;
 
 import com.janrain.steven.lztesting.ui.LeaderElectorFrame;
 import com.netflix.curator.framework.CuratorFramework;
 import com.netflix.curator.framework.CuratorFrameworkFactory;
 import com.netflix.curator.framework.recipes.leader.LeaderSelector;
 import com.netflix.curator.framework.recipes.leader.LeaderSelectorListener;
 import com.netflix.curator.framework.state.ConnectionState;
 import com.netflix.curator.retry.ExponentialBackoffRetry;
 
 public class ElectionHandler implements LeaderSelectorListener {
 
 	private static ElectionHandler eh;
 	private static LeaderSelector leaderSelector;
 	Logger logger = Logger.getLogger(ElectionHandler.class);
 	private volatile boolean isLeader;
 	
 	public void stateChanged(CuratorFramework cf, ConnectionState state) {
 		logger.info("State Changed: "+state);
 		switch (state) {
 		case SUSPENDED:
 			isLeader = false;			
 			break;
 		case LOST:
 			leaderSelector.close();
 			System.exit(ConnectionState.LOST.ordinal());
 		default:
 			break;
 		}
 	}
 
 	public void takeLeadership(CuratorFramework cf) throws Exception {
 		logger.info("Elected leader of "+leaderSelector.getId());
 		isLeader = true;
 		while(isLeader) {
 			Thread.sleep(3333);
 			logger.info("still leader");
 		}
 		logger.info("Lost leader, requeueing");
 		leaderSelector.autoRequeue();
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		eh = new ElectionHandler();
 		javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 createAndShowGUI();
             }
         });
         CuratorFramework client = CuratorFrameworkFactory.newClient("bp-dev:2181", new ExponentialBackoffRetry(333, 3));
         client.start();
         leaderSelector = new LeaderSelector(client, "/"+ElectionHandler.class.getSimpleName(), eh);
         leaderSelector.setId("Leader Election Test");
         leaderSelector.start();
 	}
 
 	private static void createAndShowGUI() {
 		LeaderElectorFrame ui = new LeaderElectorFrame(eh);
 		ui.pack();
 		ui.setVisible(true);
 	}
 
 	public void stopProcessing() {
 		isLeader = false;	
		logger.info("Reliquishing leader");
 	}
 
 }
