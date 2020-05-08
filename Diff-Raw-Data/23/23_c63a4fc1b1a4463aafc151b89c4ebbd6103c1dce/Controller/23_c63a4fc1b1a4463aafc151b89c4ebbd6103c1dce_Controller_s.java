 package rainbow.controller.application;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import rainbowpc.Message;
 import rainbowpc.controller.ControllerProtocol;
 import rainbowpc.controller.messages.*;
 import rainbowpc.scheduler.messages.QueryFound;
 import rainbowpc.scheduler.messages.WorkBlockComplete;
 
 public class Controller extends Thread {
 
 	ExecutorService executor;
 	ControllerProtocol protocol;
 	NewQuery query;
 	BruteForcer current;
 	HashMap<String, Action> mapping;
 
 	public Controller() {
 		this("localhost");
 	}
 
 	public Controller(String host) {
 		executor = Executors.newSingleThreadExecutor();
 		try {
			protocol = new ControllerProtocol(host);
 		} catch (IOException e) {
 			System.out.println("Could not connect to scheduler, has it been started?");
 			System.exit(1);
 		}
 		mapping = ControllerMappingFactory.createMapping(this);
 	}
 
 	@Override
 	public void start() {
 		super.start();
 		executor.execute(protocol);
 	}
 
 	@Override
 	public void run() {
 		while (true) {
 			try {
 				Message message = protocol.getMessage();
 				Action action = mapping.get(message.getMethod());
 				action.execute(message);
 			} catch (InterruptedException ie) {
 				interrupt();
 				break;
 			}
 		}
 	}
 	BruteForceEventListener listener = new BruteForceEventListener() {
 
 		@Override
 		public void matchFound(String match) {
 			try {
 				protocol.sendMessage(new QueryFound(protocol.getId(), query, match));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		@Override
 		public void workBlockComplete(WorkBlockSetup b) {
 			try {
 				protocol.sendMessage(new WorkBlockComplete(protocol.getId(), 0, b));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	};
 
 	public void bruteForce(WorkBlockSetup block) {
 		current = new BruteForcer(query, block, listener);
		current.start();
 	}
 
	@Override
	public void interrupt() {
		super.interrupt();
		protocol.shutdown();
		executor.shutdown();
	}
 
 	public static void main(String[] s) {
 		if (s.length > 0) {
 			new Controller(s[0]).start();
 		} else {
 			new Controller().start();
 		}
 	}
 }
