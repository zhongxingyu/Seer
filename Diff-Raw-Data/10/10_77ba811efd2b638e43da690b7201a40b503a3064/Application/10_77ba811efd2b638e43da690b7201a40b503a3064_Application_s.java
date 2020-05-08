 package controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import models.Message;
 import models.Node;
 
 import org.ow2.play.service.registry.api.Registry;
 import org.ow2.play.service.registry.api.RegistryException;
 
 import utils.Locator;
 
 public class Application extends PlayController {
 
 	public static void connect() {
 		List<Node> nodes = Node.all().fetch();
 		render(nodes);
 	}
 
 	public static void nodeConnect(Long id) {
 		Node n = Node.findById(id);
 		if (n != null) {
 			session.put("node", n.id);
 			flash.success("Connected to node %s", n.name);
 		} else {
 			flash.error("No such node");
 		}
 		Application.index();
 	}
 
 	public static void nodeDisconnect(Long id) {
 		session.remove("node");
 		flash.success("Disconnected...");
 		Application.index();
 	}
 
 	private static String getURL() {
 		Node n = Node.getCurrentNode();
 		if (n == null) {
 			flash.success("Please connect!");
 			connect();
 		}
 		return n.baseURL;
 	}
 	
 	public static void index() {
 		// redirect to topics
		TopicsController.topics();
 	}
 }
