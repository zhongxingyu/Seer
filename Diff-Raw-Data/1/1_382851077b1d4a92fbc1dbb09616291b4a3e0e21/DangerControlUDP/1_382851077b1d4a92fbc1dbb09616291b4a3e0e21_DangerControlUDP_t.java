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
 *@version 0.3
 *@since 2012-11-18
 *
 * KD Tree / Listening Object Interface for the danger zone application.
 * Uses UDP networking because it is expected that the sockets are used for interprocess communication and therefore
 * loss won't be a problem.
 */
 public class DangerControlUDP  extends DangerControl{
 	/**
 	*Debug variable, if specified as true, output messages will be displayed. 
 	*/
 	static boolean debugOn = true;
 
 	/**
 	*Socket to accept incoming queries to the Danger Control interface, listens on port 5480
 	*/
 	DatagramSocket clientListener = null;
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
 	*Packet recieved by server from the client
 	*/
 	DatagramPacket request = null;
 
 	/**
 	*Classifer interface to allow for feed back to the classifier from incoming command messages.
 	*/
 	BayesTrainer classifier = new BayesTrainer();
 
 	/**
 	*Variable to control continous listening by the server instead of a time out.
 	*/
 	static boolean continous = false;
 
 	/**
 	*The url that the output of the commands will be send to
 	*/
 	public static final String URL_TO_SEND_TO = "http://localhost/Server/Java/danger_zone/test.php";
 
 	/**
 	*Creates an instance of the DangerControl class.
 	*/
 	public DangerControlUDP() throws Exception{
 		//5480 For Listening, 5481 to send back out
 		clientListener = new DatagramSocket(port_number);
 		//clientListener.setSoTimeout(int_timeout);
 		//Construct the Tree to hold the danger zones (note this should be replaced by a tree building from sql function)
 		this.createTestTree();
 		clientListener.setReuseAddress(true);
 
 
 	}
 
 	/**
 	*Trains the instance of the classifier that this Control structure has. 
 	*@param password The password to the database the classifier uses
 	*@param debugOn True if the user wishes for debug messages to print, false if otherwise.
 	*/
 	public void trainBayes(String password,boolean debugOn){
 		classifier.run(password,debugOn);
 		classifier.close();
 	}
 
 	
 	/**
 	*Run this instance of DangerControl for the specified amount of time as determined by time out.
 	*/
 	public void run() throws Exception{
 		System.out.println("Running Server with Timeout");
 		//Fun Fact, Java supports labels. I didn't know Java liked Spaghetti
 		Running:
 		while(System.currentTimeMillis() < long_timeout){
 			request = new DatagramPacket(new byte[1024], 1024);
 			this.read(request);
 			
 		}
 		//Cleanup
 		clientListener.close();
 		
 	}
 
 	/**
 	*Run the instance of Danger Control continously without a timeout, only a kill message passed or a kill command from the OS will shut down the instance
 	*@param continous True for if the control structure should run the entire time, false will result in this instance not running at all.
 	*/
 	public void run(boolean continous) throws Exception{
 		System.out.println("Running Server Continously");
 		DangerControlUDP.continous = continous;
 		while(DangerControlUDP.continous){
 			request = new DatagramPacket(new byte[1024], 1024);
 			System.out.println("Reading Packet");
 			this.read(request);
 			
 		}
 		//Cleanup
 		clientListener.close();	
 		classifier.close();
 	}
 
 	/**
 	*Readings incoming messages and calls the dispatcher to send responses
 	*/
 	public void read(DatagramPacket request) throws Exception{
 		
 
 		// Block until the host receives a UDP packet.
 	    clientListener.receive(request);
 
 		// Obtain references to the packet's array of bytes.
       	byte[] buf = request.getData();
 
       	// Wrap the bytes in a byte array input stream,
       	// so that you can read the data as a stream of bytes.
       	ByteArrayInputStream bais = new ByteArrayInputStream(buf);
 
       	// Wrap the byte array output stream in an input stream reader,
       	// so you can read the data as a stream of characters.
       	InputStreamReader isr = new InputStreamReader(bais);
 
       	// Wrap the input stream reader in a bufferred reader,
       	// so you can read the character data a line at a time.
       	// (A line is a sequence of chars terminated by any combination of \r and \n.) 
       	BufferedReader incomingStream = new BufferedReader(isr);
 
       	// The message data is contained in a single line, so read this line.
       	String line;
 	
 		//Loop through incoming message from udp
 		
 		while((line = incomingStream.readLine()) != null){
 			System.out.println(line);
 			this.handleLine(line,request);	
 		}
 		
 	}
 
 	public void handleLine(String line,DatagramPacket request){
 		
 			//We should use some type of switch or something to figure out what function to call from the command parser
 			if(line.indexOf(CommandParser.CMD_LON) != -1 && line.indexOf(CommandParser.CMD_LAT) != -1){
 				//Handle the command and respond to it
 				try{ 
 					Stack<DangerNode> temp = this.handleGeoCommand(line.trim());
 					this.dispatchResponse(temp,request);
 				}catch(Exception e){
 					System.out.println("Error handling Geo Command: '"  + line + "' is not properly formed");
 					System.out.println("Exception: " + e.getMessage());
 					for(StackTraceElement element : e.getStackTrace()){
 						System.out.println("Trace: " + element.toString());
 					}
 				}
 				//Force the stream to spit back to the client
 			}else if(line.trim().equals(CommandParser.KILL)){
 				//We've found the kill server command in the line, so seppuku.
 				System.out.println("Recieved Kill Code");
 				DangerControlUDP.continous = false;
 				long_timeout = 0;
 			}else if(line.indexOf(CommandParser.CMD_CLASSIFY)!=-1){
 				//Handle the classification
 				String cat = this.handleClassify(CommandParser.parseClassifyCommand(line));
 				try{ 	
 					if(cat.equals("D")){
 						this.dispatchClassResponse("Dangerous",request);
 					}else if(cat.equals("S")){
 						this.dispatchClassResponse("Safe",request);
 					}else{
 						this.dispatchClassResponse("Ill formed request",request);
 					}
 				}catch(Exception e){
 					System.out.println("Error handling Classification Command: \"" + line + "\" is not properly formed");
 					System.out.println(e.getMessage());	
 				}
 			}else if(line.indexOf(CommandParser.CMD_TRAIN)!=-1){
 				//Train the data.
 				String [] parsed = CommandParser.parseTrainCommand(line);
 				//First element is category
 				boolean commited = false;
 				if(parsed[0].equals(CommandParser.OPT_DANGER)){
 					commited = classifier.trainOnText(parsed[1].trim(),NaiveBayes.CAT_DANGER);
 				}else if(parsed[0].equals(CommandParser.OPT_SAFE)){
 					commited = classifier.trainOnText(parsed[1].trim(),NaiveBayes.CAT_SAFE);
 				}else{
 					System.out.println("Unknown category");
 				}
 				this.dispatchTrainResponse(commited, request);;
 			}
 			//We can extend right here to implement more commands
 	}
 
 	/**
 	*Dispatches the training response to the client.
 	*@param committed Whether or not the training was sucessful
 	*@param request the packet to use to figure out addresses to send back to the user.
 	*/
 	public void dispatchTrainResponse(boolean commited, DatagramPacket request){
 		JSONObject response = new JSONObject();
 		String responseString = commited ? "Yes" : "No";
 		response.put("Response", responseString);
 		InetAddress clientHost = request.getAddress();
 		int clientPort = request.getPort();
 		byte[] buf = (response.toString() + "\0").getBytes();
 	    DatagramPacket reply = new DatagramPacket(buf, buf.length, clientHost, clientPort);
 	    try{
 	    	clientListener.send(reply);
 	    }catch(IOException ioe){
 	    	System.out.println("Could not send the packet back to the client");
 	    	System.out.println("IOException: " +ioe.getMessage());
 	    }
 	}
 
 
 	/**
 	*Dispatches the class response to the client.
 	*@param responseString the string to send back to the user.
 	*@param request the packet to use to figure out addresses to send back to the user.
 	*/
 	public void dispatchClassResponse(String responseString, DatagramPacket request){
 		JSONObject response = new JSONObject();
 		response.put("Response", responseString);
 		InetAddress clientHost = request.getAddress();
 		int clientPort = request.getPort();
 		byte[] buf = (response.toString() + "\0").getBytes();
 	    DatagramPacket reply = new DatagramPacket(buf, buf.length, clientHost, clientPort);
 	    try{ 
 	 	   clientListener.send(reply);
 		}catch (Exception e) {
 			System.out.println("could not send response to client");
 			System.out.println("Exception: " + e.getMessage());
 		}
 	}
 
 
 	/**
 	*Dispatches a response back to the client of the nearest neighbors to the point they asked for.
 	*@param neighbors The nearest zones returned by the search for the tree
 	*/
 	public void dispatchResponse(Stack<DangerNode> neighbors,DatagramPacket request){
 		//Lets send the response as a json array of the nodes
 		JSONObject response = new JSONObject();
 		response.put("neighbors", neighbors);
 		// Send reply.
 	    InetAddress clientHost = request.getAddress();
 	    int clientPort = request.getPort();
 	    byte[] buf = (response.toString() + "\0").getBytes();
 	    DatagramPacket reply = new DatagramPacket(buf, buf.length, clientHost, clientPort);
 	    try{ 
 	 	   clientListener.send(reply);
 		}catch (Exception e) {
 			System.out.println("could not send response to client");
 			System.out.println("Exception: " + e.getMessage());
 		}
 	}
 
 	
 
 	/**
 	*Parses a command in the GEO COMMAND format, will return the results of searching the tree for the specified coordinate and number of near zones
 	*@param geoCommand String command in the GEO COMMAND format;
 	*@return returns the results of searching the tree for the coordinate.
 	*/
 	public Stack<DangerNode> handleGeoCommand(String geoCommand){
 		float[] geoCmd = null;
 		//Parse information from the message:
 		geoCmd = CommandParser.parseGeoCommand(geoCommand);
 		if(geoCmd != null){
 			//We have recieved the Coordinates and should play with the tree
 			//System.out.println("Searching tree for " + geoCmd[0] + " " + geoCmd[1]);
 			if(dangerZones == null){
 				System.out.println("Error: No Tree Initailized");
 				return null;
 			}
 			return dangerZones.nearestNeighbor(new float[]{geoCmd[0],geoCmd[1]},(int)geoCmd[2]);
 
 		}
 		return null;
 }
 
 	public static void main(String argv[]) throws Exception
 	{
 		
 		DangerControlUDP control = new DangerControlUDP();		
 		control.run();
 
 
 	}
 
 }
