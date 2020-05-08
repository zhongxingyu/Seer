 package com.golddigger;
 
 import java.util.List;
 
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.servlet.Context;
 import org.mortbay.jetty.servlet.DefaultServlet;
 import org.mortbay.jetty.servlet.ServletHolder;
 
 import com.golddigger.core.AppContext;
 import com.golddigger.core.GoldDiggerServlet;
 import com.golddigger.core.Service;
 import com.golddigger.model.Map;
 import com.golddigger.model.Player;
 import com.golddigger.templates.BlankGameTemplate;
 import com.golddigger.templates.TestGameTemplate;
 
 public class TestServer {
 	private final static int PORT = 8066;
 	private final static String CONTEXT = "golddigger";
 	private Server server;
 	
 	public TestServer(){
 		try {
             server = new Server(PORT);
             Context root = new Context(server, "/", Context.SESSIONS);
             root.addServlet(new ServletHolder(new GoldDiggerServlet()), "/" + CONTEXT + "/*");
 //            root.setResourceBase(new File("./target/site").getAbsolutePath());
             root.addServlet(DefaultServlet.class.getName(), "/");
             server.start();
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
 	}
 	
 	public void stop(){
 		try {
 			server.stop();
 		} catch (Exception e){
 			throw new RuntimeException(e);
 		}
 	}
 	
 	public static void main(String[] args){
 		new TestServer();
 
 		AppContext.add(new BlankGameTemplate());
 		AppContext.add(new BlankGameTemplate());
 		
 		Player[] players = loadPlayers();
 		for (Player player : players){
 			AppContext.add(player);
 		}
 	}
 	
 	private static Player[] loadPlayers(){
 		return new Player[]{new Player("brett","secret")};
 	}
 }
