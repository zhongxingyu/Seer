 package propra2012.gruppe33.networktest;
 
 import propra2012.gruppe33.PreMilestoneApp;
 
 import com.foxnet.rmi.pattern.change.AdminSessionServer;
 import com.indyforge.twod.engine.graphics.rendering.scenegraph.Scene;
 import com.indyforge.twod.engine.graphics.rendering.scenegraph.SceneProcessor;
 import com.indyforge.twod.engine.graphics.rendering.scenegraph.SceneProcessor.NetworkMode;
 import com.indyforge.twod.engine.graphics.rendering.scenegraph.network.ResetNetworkTimeChange;
 import com.indyforge.twod.engine.graphics.rendering.scenegraph.network.SceneChange;
 
 public class ServerApp {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws Exception {
 
 		// Create a new scene as server
		SceneProcessor serverProcessor = new SceneProcessor(NetworkMode.Server);

		// Open the server on 1337
		serverProcessor.openServer(1337);
 
 		while (true) {
 			System.out.println("Started ?");
 			System.in.read();
 			break;
 			//
 			// synchronized (serverProcessor.adminSessionServer()) {
 			// if (serverProcessor.adminSessionServer().sessionCount() != 2) {
 			// System.out.println("Session count != 2");
 			//
 			// } else {
 			// serverProcessor.adminSessionServer().acceptingSessions(
 			// false);
 			// break;
 			// }
 			// }
 		}
 
 		// // Create peer
 		// Frame frame = GraphicsRoutines.createFrame(serverProcessor,
 		// "Bomberman SERVER", 800, 600);
 
 		Scene scene = PreMilestoneApp.createDemoGame(1, 2);
 		// Iterator<Entity> filter = new FilteredIterator<Entity>(new
 		// NameFilter(
 		// "Kr0e"), scene.childIterator(true, true));
 		// final UUID regKey = filter.next().registrationKey();
 		//
 		//
 		//
 		//
 		//
 		//
 		//
 
 		AdminSessionServer<SceneProcessor> server = serverProcessor
 				.adminSessionServer();
 
 		// Apply the scene change
 		server.combined().applyChange(new SceneChange(scene));
 		System.out.println("Send change");
 
 		Thread.sleep(1000);
 
 		// Reset the network time every where
 		server.combined().applyChange(new ResetNetworkTimeChange());
 
 		// Start the processor
 		serverProcessor.start(60);
 	}
 }
