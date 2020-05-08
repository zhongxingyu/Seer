 package danger_zone;
 import java.io.*;
 import java.net.*;
 import java.util.Timer;
 import java.util.Stack;
 import java.util.Map;
 //http://code.google.com/p/json-simple/
 import org.json.simple.JSONObject;
 
 
 
 /**
 *@author Ethan Eldridge <ejayeldridge @ gmail.com>
 *@version 0.1
 *@since 2012-10-5
 *
 * KD Tree / Listening Object Interface for the danger zone application.
 * Interface providing functionality to DangerControl UDP and TCP
 */
 public abstract class DangerControl{
 	/**
 	*Debug variable, if specified as true, output messages will be displayed. 
 	*/
 	static boolean debugOn = true;
 	/**
 	*Socket to accept incoming queries to the Danger Control interface, listens on port 5480
 	*/
 	ServerSocket clientListener = null;
 	/**
 	*Timeout for the DangerControl program's clientListener, this must be set in integer form (Seconds)
 	*/
 	static int int_timeout = 5;
 	/**
 	*Timeout for the DangerControl program itself, this is used during debugging and will probably be removed in release implementations
 	*/
 	long long_timeout = System.currentTimeMillis() + 1000*int_timeout;
 	/**
 	*Socket that will hold the incoming traffic coming from the clientListener
 	*/
 	Socket incoming = null;
 	/**
 	*Data Structure to hold the dangerZones from the database. 
 	*/
 	DangerNode dangerZones = null;
 	/**
 	*Port number to communicate to the client with
 	*/
 	static int port_number = 5480;
 	/**
 	*Classifer interface to allow for feed back to the classifier from incoming command messages.
 	*/
 	BayesTrainer classifier = new BayesTrainer();
 
 	/**
 	*Variable to control continous listening by the server instead of a time out.
 	*/
 	static boolean continous = false;
 
 	public abstract void trainBayes(String password,boolean debugOn);
 	public abstract void run() throws Exception;
 	public abstract void run(boolean continous) throws Exception;
 	public abstract Stack<DangerNode> handleGeoCommand(String geoCommand);
 	
 	/**
 	*Sets the root node to the Danger Node Tree
 	*@param dn The node to the root of the tree.
 	*/
 	public void setRootNode(DangerNode dn){
 		dangerZones = dn;
 	}
 
 	/**
 	*Creates a small testing tree
 	*/
 	public void createTestTree(){
 		dangerZones = new DangerNode(9,9,1);
 		dangerZones.addNode(new DangerNode(7,2,4));
 		dangerZones.addNode(new DangerNode(12,12,5));
 		dangerZones.addNode(new DangerNode(15,13,6));
 		this.dangerZones = DangerNode.reBalanceTree(dangerZones);
 	}
 
 	/**
 	*Classifies the tweet from the passed in line using the classifier.
 	*@param line The line to be classified
 	*@result Returns a D or S depending on the category the line is classified into, or an empty string if the category is not recognized.
 	*/
 	public String handleClassify(String line){
 		int cat = classifier.classify(line);
 		switch(cat){
 			case NaiveBayes.CAT_DANGER:
 				return "D";
 			case NaiveBayes.CAT_SAFE:
 				return "S";
 			default:
 				return "";
 		}
 	}
 }
