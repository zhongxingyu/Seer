 /*
  * Created on Jan 15, 2005
  */
 package cc.warlock.client.internal;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 
 import cc.warlock.client.ICommand;
 import cc.warlock.client.ICommandHistory;
 import cc.warlock.client.IStream;
 import cc.warlock.client.IWarlockClient;
 import cc.warlock.client.IWarlockClientViewer;
 import cc.warlock.network.IConnection;
 
 
 /**
  * @author Marshall
  */
 public abstract class WarlockClient implements IWarlockClient {
 
 	protected IConnection connection;
 	protected ArrayList<IWarlockClientViewer> viewers;
 	protected ICommandHistory commandHistory = new CommandHistory();
 	protected String streamPrefix;
 	
 	public WarlockClient () {
 		viewers = new ArrayList<IWarlockClientViewer>();
 		streamPrefix = "client:" + hashCode() + ":";
 	}
 	
 	// IWarlockClient methods
 	
 	public ICommandHistory getCommandHistory() {
 		return commandHistory;
 	}
 	
 	public abstract void connect(String server, int port, String key) throws IOException;
 	
 	public void send(String command) {
 		send(new Command(command, new Date()));
 	}
 	
 	public void send(ICommand command) {
 		if(connection == null) {
 			// Not yet connected to server
 			return;
 		}
 		
 		if (!command.isInHistory())
 			commandHistory.addCommand(command);
 		
		/* FIXME: instead of stopping prompting, we should call a method
		 * displayCommand() from the stream a lot of different places send 
		 * commands, all of them should be updated with the described
		 * functionality
 		 */
 		getDefaultStream().donePrompting();
 		
 		try {
 			connection.send(command.getCommand() + "\n");
 		} catch(IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public Collection<IWarlockClientViewer> getViewers() {
 		return viewers;
 	}
 	
 	public void addViewer(IWarlockClientViewer viewer) {
 		viewers.add(viewer);
 	}
 	
 	public IStream getDefaultStream() {
 		return getStream(IWarlockClient.DEFAULT_STREAM_NAME);
 	}
 	
 	public IStream getStream(String streamName) {
 		return Stream.fromName(streamPrefix + streamName);
 	}
 	
 	public IConnection getConnection() {
 		return connection;
 	}
 }
