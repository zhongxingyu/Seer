 package node;
 import java.io.IOException;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Queue;
 
 import common.Common;
 import common.MessageController;
 import common.Request;
 import common.Resource;
 import common.Triplet;
 import common.Tuple;
 
 public class Node {
 	protected String nodeId;
 	protected Resource localResource;
 
 	protected MessageController messageController;
 	protected Common.State NodeState;
 	protected final static Logger LOGGER = Logger.getLogger(Node.class.getName());
 	private static FileHandler logFile;
 
 
 
 	public Node(String nodeId, String fileName) throws IOException
 	{
 		this.nodeId = nodeId;
 		this.NodeState = Common.State.ACTIVE;
 		
 		//Bind the resource
 		if(!fileName.equals("")){
 			this.localResource = new Resource(fileName, Common.FilePath);
 			Common.ReadLineCount = this.localResource.GetLineCount();
 		}
 
 		//RMQ Message Controller.
 		this.messageController = new MessageController(this.nodeId);
 
 		//Logging Specific
 		logFile = new FileHandler(Common.FilePath+"/"+this.nodeId+".log", true);
 		logFile.setFormatter(new SimpleFormatter());
 		LOGGER.setLevel(Level.INFO); //Sets the default level if not provided.		
		LOGGER.addHandler(logFile);		
 	}	
 
 	public void DeclareExchanges( ArrayList<Triplet<String, String, Boolean>> exchanges) throws IOException
 	{
 		//Declare exchanges for the sender.
 		for(Triplet<String, String, Boolean> exchange : exchanges)
 		{
 			this.messageController.DeclareExchange(exchange.x, exchange.y, exchange.z);
 		}
 	}
 	
 	public void InitializeConsumer() throws IOException
 	{
 		this.messageController.InitializeConsumer();
 	}
 
 	//Add a new log entry.
 	public void AddLogEntry(String message, Level level){		
 		LOGGER.logp(level, this.getClass().toString(), "", message);		
 	}
 
 
 }
