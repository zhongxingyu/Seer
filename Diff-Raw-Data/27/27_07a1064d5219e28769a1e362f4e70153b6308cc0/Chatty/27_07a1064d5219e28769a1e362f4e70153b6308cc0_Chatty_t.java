 package chatty;
 
 import gui.FeedListener;
 import gui.MainFrame;
 
 import java.io.IOException;
 import java.util.Scanner;
 
 import network.NetworkHandler;
 import network.NetworkListener;
 import network.client.Client;
 import network.server.Server;
 
 public class Chatty {
 	private NetworkHandler networkHandler;
 
 	public static void main(String[] args) {
 		new Chatty().run();
 	}
 
 	private void run() {
		int port = Config.DEFAULT_PORT;
		String hostname = Config.DEFAULT_HOST;
 
 		MainFrame gui = new MainFrame(this);
 		FeedListener feedListener = gui.getFeedWindow();
 		networkHandler = new NetworkHandler(port, hostname, gui);
 	}
 
 	public NetworkHandler getNetworkHandler() {
 		return networkHandler;
 	}
 }
