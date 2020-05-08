 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 // intended to check consistency
 public class DNA {
 	Client client;
 	BlockingQueue<String> source;
 
 	static enum Command {
 		UP, DOWN, LEFT, RIGHT, QUIT
 	};
 	static Map<String, Command> commands;
 
 	DNA(Client client) {
 		this.client = client;
 		this.source = this.client.addListener();
 
 		this.client.write("dnalab;");
 	}
 
 	public boolean handleCommand(String data) {
 		Command cmd = commands.get(data);
 		if (cmd == null) {
 			return false;
 		}
 
 		handleCommand(cmd);
 		return true;
 	}
 
 	public boolean closed() {
 		return (this.source == null);
 	}
 
 	private void handleCommand(Command cmd) {
 		switch (cmd) {
 		case LEFT:
 			this.client.write("se;");
 			this.client.write("ga;");
 			break;
 		case RIGHT:
 			this.client.write("a;");
 			this.client.write("ba;");
 			break;
 		case UP:
 			this.client.write("la;");
 			this.client.write("pr;");
 			break;
 		case DOWN:
 			this.client.write("ty;");
 			this.client.write("ma;");
 			break;
 		case QUIT:
			this.client.write("quit;");
 			this.source = null;
 			this.client = null;
 			return;
 		}
 
 		try {
 			while (this.source.poll(500, TimeUnit.MILLISECONDS) != null) {
 				// will handle result here
 			}
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	static {
 		commands = new HashMap<String, Command>();
 		commands.put("l;", Command.LEFT);
 		commands.put("r;", Command.RIGHT);
 		commands.put("u;", Command.UP);
 		commands.put("d;", Command.DOWN);
 		commands.put("quit;", Command.QUIT);
 		commands.put("q;", Command.QUIT);
 	}
 }
