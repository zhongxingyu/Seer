 package pikater;
 
 import jade.content.lang.Codec;
 import jade.content.lang.Codec.CodecException;
 import jade.content.lang.sl.SLCodec;
 import jade.content.onto.Ontology;
 import jade.content.onto.OntologyException;
 import jade.content.onto.basic.Action;
 import jade.core.AID;
 import jade.core.Agent;
 import jade.core.Profile;
 import jade.domain.DFService;
 import jade.domain.FIPAException;
 import jade.domain.FIPANames;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.domain.FIPAAgentManagement.NotUnderstoodException;
 import jade.domain.FIPAAgentManagement.RefuseException;
 import jade.domain.FIPAAgentManagement.ServiceDescription;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 import jade.proto.AchieveREResponder;
 import jade.util.leap.List;
 import jade.wrapper.AgentController;
 import jade.wrapper.ContainerController;
 import jade.wrapper.ControllerException;
 import jade.wrapper.PlatformController;
 import jade.wrapper.StaleProxyException;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.sql.Connection;
 import java.sql.Timestamp;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 
 import pikater.ontology.messages.CreateAgent;
 import pikater.ontology.messages.Execute;
 import pikater.ontology.messages.LoadAgent;
 import pikater.ontology.messages.MessagesOntology;
 import pikater.ontology.messages.SaveAgent;
 
 public class Agent_AgentManager extends Agent {
 
 	
 	Connection db;
 	Logger log;
 	Codec codec = new SLCodec();
 	Ontology ontology = MessagesOntology.getInstance();
 	
 	private String path = System.getProperty("user.dir") + System.getProperty("file.separator");
 
 	private HashMap<String, String> agentTypes = new HashMap<String, String>();
 	private HashMap<String, Object[]> agentOptions = new HashMap<String, Object[]>();	
 
	private boolean no_log = false;
 	
 	@Override
 	protected void setup() {
 		Object[] args = getArguments();
     	if (args != null && args.length > 0) {
 			int i = 0;
 						
 			while (i < args.length){
 				// System.out.println(args[i]);
 				if (args[i].equals("no_log")){					
 					no_log = true;
 				}
 				i++;
 			}
 		}				
 		
 		try {
 			//db = DriverManager.getConnection(
 			//		"jdbc:hsqldb:file:data/db/pikaterdb", "", "");
 
 			String hostAddress = this.getProperty(Profile.MAIN_HOST, null);
 			
 			Logger.getRootLogger()
 					.addAppender(
 							new FileAppender(new PatternLayout(
 									"%r [%t] %-5p %c - %m%n"), "log_" + hostAddress));
 
 			log = Logger.getLogger(Agent_AgentManager.class);
             
 			if (no_log){
 				log.setLevel(Level.OFF);
 			}
 			else{
 				log.setLevel(Level.TRACE);	
 			}
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	
 		File data = new File("saved");
         if (!data.exists()) {
             log.info("Creating directory saved");
             if (data.mkdirs()) {
                 log.info("Succesfully created directory saved");
             } else {
                 log.error("Error creating directory saved");
             }
         }
         
 		getContentManager().registerLanguage(codec);
 		getContentManager().registerOntology(ontology);
 
 		getAgentTypesFromFile();
 		
 		MessageTemplate mt = MessageTemplate.and(MessageTemplate
 				.MatchOntology(ontology.getName()), MessageTemplate
 				.MatchPerformative(ACLMessage.REQUEST));
 
 		addBehaviour(new AchieveREResponder(this, mt) {
 
 			private static final long serialVersionUID = 7L;
 
 			@Override
 			protected ACLMessage handleRequest(ACLMessage request)
 					throws NotUnderstoodException, RefuseException {
 				
 				log.info("Agent " + getLocalName() + " received request: "
 						+ request.getContent());
/*				System.out.println(getLocalName() + ": Queue size: " 
 						+ myAgent.getCurQueueSize() + " " + myAgent.getQueueSize());
*/				
 				try {
 					Action a = (Action) getContentManager().extractContent(
 							request);			        
 
 					if (a.getAction() instanceof LoadAgent) {
 						LoadAgent la = (LoadAgent) a.getAction();
 							Execute fa = la.getFirst_action();
 							
 							Agent newAgent = null;
 							
 							if (la.getObject() != null){
 								newAgent = (Agent) toObject(la.getObject());
 							}
 							else {
 														
 								// read agent from file 
 							    String filename = "saved" + System.getProperty("file.separator") 
 							    	+  la.getFilename() + ".model";
 							        
 							    //Construct the ObjectInputStream object
 							    ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(filename));
 							            
 							    newAgent = (Agent) inputStream.readObject();
 							} 
 						    
 						    System.out.print(getLocalName() + ": Resurrected agent : "+newAgent);
 						    // TODO kdyz se ozivuje 2x ten samej -> chyba
 						    
 						    
 						    if (newAgent != null){
 								// get a container controller for creating new agents						    	
 						    	
 						    	ContainerController container = getContainerController();
 						    	AgentController controller = container.acceptNewAgent(la.getFilename(), newAgent);
 						    	controller.start();						    	
 						    							    	
 							}
 						    else {
 						    	throw new ControllerException("Agent not created.");
 						    }
 																					
 							log.info("Loaded agent:   " + la.getFilename());
 														
 							ACLMessage reply = null;								
 														
 							if (fa != null){
 								// send message with fa action to the loaded agent
 								
 								Action ac = new Action();
 								ac.setAction(fa);
 								ac.setActor(request.getSender());								
 								
 								ACLMessage first_message = new ACLMessage(ACLMessage.REQUEST);								
 								first_message.setLanguage(codec.getName());
 								first_message.setOntology(ontology.getName());
 								first_message.addReceiver(new AID(la.getFilename(), AID.ISLOCALNAME));
 								first_message.clearAllReplyTo();
 								first_message.addReplyTo(request.getSender());
 								first_message.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);								
 								first_message.setConversationId(request.getConversationId());
 								
 								getContentManager().fillContent(first_message, ac);
 
                                                                 /*reply = request.createReply();
 
                                                                 ACLMessage first_reply = null;
 
                                                                 try {
                                                                     first_reply = FIPAService.doFipaRequestClient(Agent_AgentManager.this, first_message);
                                                                     System.err.println("GOT REPLY");
                                                                 }
                                                                 catch (FIPAException fe) {
                                                                     fe.printStackTrace();
                                                                     reply.setPerformative(ACLMessage.FAILURE);
                                                                     return reply;
                                                                 }
 
                                                                 FileWriter fw = new FileWriter("message");
                                                                 fw.write(first_reply.getContent());
                                                                 fw.close();
 
                                                                 reply.setPerformative(ACLMessage.INFORM);
                                                                 reply.setContent(first_reply.getContent());
                                                                  *
                                                                  */
 
 								send(first_message);
 							}						
 							reply = request.createReply();
 							reply.setContent("Agent "+newAgent.getLocalName()+" resurected.");
 							reply.setPerformative(ACLMessage.INFORM);
 							
 														
 							return reply;
 					}
 					
 					if (a.getAction() instanceof SaveAgent) {
 							// write it into database
 							SaveAgent sa = (SaveAgent) a.getAction();
 
 							int userID = sa.getUserID();
 							// pikater.ontology.messages.Data data = sa.getData();
 							
 							pikater.ontology.messages.Agent agent = sa.getAgent();							
 							
 							String name = agent.getName(); // TODO - zajistit unikatni pro konkretniho uzivatele
 							Timestamp currentTimestamp = 
 								new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
 
 
 							String filename = userID + "_" + name + "_" 
 								+ currentTimestamp.toString().replace(":", "-").replace(" ", "_");
 							
 							
 							// save serialized object to file
 							byte [] object = sa.getAgent().getObject();
 							ObjectOutputStream oos = new ObjectOutputStream(
 									new FileOutputStream("saved" + System.getProperty("file.separator") + filename + ".model"));												
 							
 							Agent newAgent = (Agent) (toObject(object));						    							
 														
 							oos.writeObject(toObject(object));
 							oos.flush();
 							oos.close();
 							log.info("Agent "+ name +" saved to file" + filename + ".model");
 																					
 							/*
 							String query = "UPDATE results SET (finish, objectFilename) " +
 									"VALUES ("								
 								+ "\'" + currentTimestamp + "\',"								
 								+ "\'" + filename
 								+ "\')";  						
 												
 							Statement stmt = db.createStatement();
 							log.info("Executing query: " + query);							
 
 							stmt.executeUpdate(query);
 							*/							
 							
 							ACLMessage reply = request.createReply();
 							reply.setContent(filename);
 							reply.setPerformative(ACLMessage.INFORM);
 
 							return reply;
 							
 					} // end of SaveAgent
 
 					/* 
 					 	if (a.getAction() instanceof GetSavedAgents){
 						GetSavedAgents gsa = (GetSavedAgents) a.getAction();
 						
 						int userID = gsa.getUserID();
 													
 						String query = "SELECT * FROM results WHERE userID = " + userID;
 						log.info("Executing query " + query);
 
 						Statement stmt = db.createStatement();
 						ResultSet rs = stmt.executeQuery(query);
 						
 						List agents = new ArrayList();			
 						
 						while( rs.next() ){
 							pikater.ontology.messages.Agent agent = new pikater.ontology.messages.Agent();
 							agent.setName(rs.getString("name"));
 							agent.setSaved_timestamp(rs.getString("finish"));
 							agent.setSaved_object_filename(rs.getString("objectFilename"));
 							agent.setType(rs.getString("type"));
 							agent.setSaved_train_filename(rs.getString("trainFilename"));
 							
 							agents.add(agent);
 						}					
 						
 						stmt.close();
 						
 						
 						ACLMessage reply = request.createReply();
 						reply.setPerformative(ACLMessage.INFORM);
 						
 						Result r = new Result(a.getAction(), agents); 
 						getContentManager().fillContent(reply, r);
 
 						return reply;												
 					}	
 					*/									
 				 	if (a.getAction() instanceof CreateAgent){
 						CreateAgent ca = (CreateAgent) a.getAction();
 																	
 						String agent_name;
 						if (ca.getName() != null){
 							agent_name = ca.getName();
 						}
 						else{
 							agent_name = ca.getType();
 						}
 						
 						agent_name = createAgent(ca.getType(), agent_name, ca.getArguments());
 						
 						ACLMessage reply = request.createReply();
 						reply.setPerformative(ACLMessage.INFORM);
 						reply.setContent(agent_name);
 						System.out.println(myAgent.getLocalName()+": Agent "+agent_name+" created.");
 						
 						return reply;												
 					}	
 				
 				} catch (OntologyException e) {
 					e.printStackTrace();
 					log.error("Problem extracting content: " + e.getMessage());
 				} catch (CodecException e) {
 					e.printStackTrace();
 					log.error("Codec problem: " + e.getMessage());
 				} catch (FileNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (StaleProxyException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (ControllerException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (ClassNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				// } catch (FIPAException e) {
 					// TODO Auto-generated catch block
 				//	e.printStackTrace();
 				}
 
 				ACLMessage failure = request.createReply();
 				failure.setPerformative(ACLMessage.FAILURE);
 				log.error("Failure responding to request: "
 						+ request.getContent());
 				return failure;
 			}
 		});
 	}
 
     public static byte[] getBytesFromFile(File file) throws IOException {
         InputStream is = new FileInputStream(file);
     
         // Get the size of the file
         long length = file.length();
     
         if (length > Integer.MAX_VALUE) {
             // File is too large
         }
     
         // Create the byte array to hold the data
         byte[] bytes = new byte[(int)length];
     
         // Read in the bytes
         int offset = 0;
         int numRead = 0;
         while (offset < bytes.length
                && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
             offset += numRead;
         }
     
         // Ensure all the bytes have been read in
         if (offset < bytes.length) {
             throw new IOException("Could not completely read file "+file.getName());
         }
     
         // Close the input stream and return bytes
         is.close();
         return bytes;
     }
     
     public static Object toObject(byte[] bytes) throws IOException, ClassNotFoundException{
     	Object object = null;
     	
     	object = new java.io.ObjectInputStream(new
     			java.io.ByteArrayInputStream(bytes)).readObject(); 
     	
     	return object;
     } 
     
 
     private String getDateTime() {
         DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH.mm.ss");
         Date date = new Date();
         return dateFormat.format(date);
     }
     
 	private String createAgent(String type, String name, List args){
 		// get a container controller for creating new agents
 		PlatformController container = getContainerController();				
 		
 		Object[] Args1 = new Object[0];
 		Object[] Args2 = new Object[0];
 
 		if (agentOptions.get(type) != null){
 			Args1 = agentOptions.get(type);						
 		}
 		
 		if (args != null){
 			Args2 = args.toArray();
 		}
 		
 		int size = Args1.length + Args2.length;
 		Object[] Args = new Object[size];	    
 		int i = 0;
 		for (Object o: Args1){
 			Args[i] = o;
 			i++;
 		}
 		for (Object o: Args2){
 			Args[i] = o;
 			i++;
 		}			
 		
 		/*
 		System.out.println("name: "+name);
 		System.out.println("type: "+agentTypes.get(type));
 		System.out.println("args: "+Args);
 		*/				
 		
 		boolean agent_created = false;
 		while (!agent_created){		
 			try {
 				AgentController agent = container.createNewAgent(name, agentTypes.get(type), Args);
 				agent.start();
 				agent_created = true; // no exception occured
 			} catch (ControllerException e) {
 				// System.err.print(getLocalName() + " :");
 				// e.printStackTrace();
 				// try again with a different name				
 				name = generateName(name);
 				// System.err.print(getLocalName() + " : new name: " + name);
 				agent_created = false;
 			}
 		
 		}
 		// provide agent time to register with DF etc.
 		doWait(300);
 		
 		return name;
 	}
 
 	private String generateName(String name) {
 		int i = 0;
 		while (name.charAt(name.length()-i-1) >= 48 &&
 				name.charAt(name.length()-i-1) <= 57){
 			i++;			
 		}
 		
 		if (i == 0){
 			// no numbers
 			return name += "0";
 		}
 		else{
 			int number = Integer.parseInt(name.substring(name.length()-i, name.length()));
 			number++;
 			return name.substring(0, name.length()-i) + number;
 		}
 				
 	}
 	
 	private String generateName_old(String agentType) {
 		int number = 0;
 		String name = agentType + number;
 		boolean success = false;
 		while (!success) {
 			// try to find an agent with "name"
 			DFAgentDescription template = new DFAgentDescription();
 			ServiceDescription sd = new ServiceDescription();
 			sd.setName(name);
 			template.addServices(sd);
 			try {
 				DFAgentDescription[] result = DFService.search(this, template);
 				// if the agent with this name already exists, increase number
 				if (result.length > 0) {
 					number++;
 					name = agentType + number;
 				} else {
 					success = true;
 					return name;
 				}
 			} catch (FIPAException fe) {
 				fe.printStackTrace();
 			}
 		}
 		return null;
 	}
 	
 	private void getAgentTypesFromFile(){
 		// Sets up a file reader to read the agent_types file
 		FileReader input;
 		try {
 			input = new FileReader(path + "agent_types");
 			// Filter FileReader through a Buffered read to read a line at a
 			// time
 			BufferedReader bufRead = new BufferedReader(input);
 			String line = bufRead.readLine();
 
 			// Read through file one line at time
 			while (line != null) {
 				String[] agentClass = line.split(":");
 				agentTypes.put(agentClass[0], agentClass[1]);
 				if(agentClass.length>2){
 					Object[] opts = new Object[agentClass.length-2];
 					for(int i = 0; i < opts.length; i++)
 						opts[i] = agentClass[i+2];
 					this.agentOptions.put(agentClass[0], opts);
 				}
 				line = bufRead.readLine();
 			}
 
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		agentTypes.put("ChooseXValues", "pikater.Agent_ChooseXValues");
                 agentTypes.put("GASearch", "pikater.Agent_GASearch");
                 agentTypes.put("SimulatedAnnealing", "pikater.Agent_SimulatedAnnealing");
 		agentTypes.put("RandomSearch", "pikater.Agent_RandomSearch");
 		agentTypes.put("OptionsManager", "pikater.Agent_OptionsManager");
 
 	}
     
 }
