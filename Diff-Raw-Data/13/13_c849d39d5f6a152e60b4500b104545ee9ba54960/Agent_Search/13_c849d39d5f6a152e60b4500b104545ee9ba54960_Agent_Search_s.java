 package pikater;
 
 import java.io.*;
 
 import org.hsqldb.lib.Iterator;
 
 import pikater.ontology.messages.Eval;
 import pikater.ontology.messages.ExecuteParameters;
 import pikater.ontology.messages.GetParameters;
 import pikater.ontology.messages.GetOptions;
 import pikater.ontology.messages.MessagesOntology;
 import pikater.ontology.messages.Option;
 import jade.content.ContentElement;
 import jade.content.lang.Codec;
 import jade.content.lang.Codec.CodecException;
 import jade.content.lang.sl.SLCodec;
 import jade.content.onto.Ontology;
 import jade.content.onto.OntologyException;
 import jade.content.onto.UngroundedException;
 import jade.content.onto.basic.Action;
 import jade.content.onto.basic.Result;
 import jade.core.Agent;
 import jade.core.behaviours.Behaviour;
 import jade.domain.DFService;
 import jade.domain.FIPAException;
 import jade.domain.FIPANames;
 import jade.domain.FIPAService;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.domain.FIPAAgentManagement.NotUnderstoodException;
 import jade.domain.FIPAAgentManagement.ServiceDescription;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 import jade.proto.AchieveREResponder;
 import jade.util.leap.ArrayList;
 import jade.util.leap.List;
 
 public abstract class Agent_Search extends Agent {	
 
 	private static final long serialVersionUID = 8637677510056974015L;
 	private Codec codec = new SLCodec();
 	private Ontology ontology = MessagesOntology.getInstance();
 	
	protected int query_block_size = 3;
 
 	private List search_options = null;
 	private List schema = null;
 	
 	protected abstract String getAgentType();
 	protected abstract List generateNewSolutions(List solutions, float[][] evaluations); //returns List of Options
 	protected abstract boolean finished();
 	protected abstract void updateFinished(float[][] evaluations);
 	protected abstract void loadSearchOptions(); // load the appropriate options before sending the first parameters
 	
 	protected ACLMessage getParameters(ACLMessage request) {
 		ACLMessage reply = request.createReply();
 		
 		pikater.ontology.messages.Agent agent = null;
 
 		String optPath = System.getProperty("user.dir") +
 			System.getProperty("file.separator") + "options" + 
 			System.getProperty("file.separator") + getAgentType() + ".opt";
 
 		// read options from file
 		try {
 			/* Sets up a file reader to read the options file */
 			FileReader input = new FileReader(optPath);
 			/*
 			 * Filter FileReader through a Buffered read to read a line at a
 			 * time
 			 */
 			BufferedReader bufRead = new BufferedReader(input);
 
 			String line; // String that holds current file line
 			int count = 0; // Line number of count
 			// Read first line
 			line = bufRead.readLine();
 			count++;
 
 			// list of ontology.messages.Option
 			List _options = new ArrayList();
 			agent = new pikater.ontology.messages.Agent();
 			agent.setName(getLocalName());
 			agent.setType(getAgentType());
 			
 			// Read through file one line at time. Print line # and line
 			while (line != null) {
 				System.out.println("    " + count + ": " + line);
 
 				// parse the line
 				String delims = "[ ]+";
 				String[] params = line.split(delims, 11);
 
 				if (params[0].equals("$")) {
 					
 					String dt = null; 										
 					if (params[2].equals("boolean")) {
 						dt = "BOOLEAN";
 					}
 					if (params[2].equals("float")) {
 						dt = "FLOAT";
 					}
 					if (params[2].equals("int")) {
 						dt = "INT";
 					}
 					if (params[2].equals("mixed")) {
 						dt = "MIXED";
 					}					
 					
 					float numArgsMin;
 					float numArgsMax;
 					float rangeMin = 0;
 					float rangeMax = 0;
 					String range;
 					List set = null;
 					
 					if (dt.equals("BOOLEAN")){
 						numArgsMin = 1;
 						numArgsMax = 1;
 						range = null;						
 					}
 					else{
 						numArgsMin = Float.parseFloat(params[3]);
 						numArgsMax = Float.parseFloat(params[4]);
 						range = params[5];
 
 						if (range.equals("r")){
 							rangeMin = Float.parseFloat(params[6]);
 							rangeMax = Float.parseFloat(params[7]);
 						}
 						if (range.equals("s")){
 							set = new ArrayList();
 							String[] s = params[6].split("[ ]+");
 							for (int i=0; i<s.length; i++){
 								set.add(s[i]);
 							}
 						}
 					}
 					
 					Option o = new Option(params[1], dt,
 							numArgsMin, numArgsMax,
 							range, rangeMin, rangeMax, set,
 							params[params.length-3],
 							params[params.length-2],
 							params[params.length-1]);
 					
 					_options.add(o);
 					
 				}
 
 				line = bufRead.readLine();
 
 				count++;
 			}
 			agent.setOptions(_options);
 			bufRead.close();
 
 			reply.setPerformative(ACLMessage.INFORM);
 
 			// Prepare the content
 			ContentElement content = getContentManager()
 					.extractContent(request); // TODO exception block?
 			Result result = new Result((Action) content, agent);
 
 			getContentManager().fillContent(reply, result);
 			
 		} catch (ArrayIndexOutOfBoundsException e) {
 			e.printStackTrace();
 			reply.setPerformative(ACLMessage.FAILURE);
 			reply.setContent(e.getMessage());
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			reply.setPerformative(ACLMessage.FAILURE);
 			reply.setContent(e.getMessage());
 		} catch (CodecException e) {
 			e.printStackTrace();
 			reply.setPerformative(ACLMessage.FAILURE);
 			reply.setContent(e.getMessage());
 		} catch (OntologyException e) {
 			e.printStackTrace();
 			reply.setPerformative(ACLMessage.FAILURE);
 			reply.setContent(e.getMessage());
 		} catch (IOException e) {
 			e.printStackTrace();
 			reply.setPerformative(ACLMessage.FAILURE);
 			reply.setContent(e.getMessage());
 		}
 		
 		return reply;
 	} // end getParameters
 	
 	//Run the search protocol
 	/*protected ACLMessage runSearchProtocol(ACLMessage request, GetParameters gnp) {
 		search_options = gnp.getSearch_options();
 		schema = gnp.getSchema();														
 		loadSearchOptions();
 		
 		List solutions_new = null;
 		List evaluations = null;
 		ACLMessage reply = request.createReply();
 		try{
 			while (!finished()){
 				ExecuteParameters ep = new ExecuteParameters();
 				solutions_new = generateNewSolutions(solutions_new, evaluations);
 				ep.setSolutions(solutions_new); // List of Lists of Options
 
 				Action a = new Action();
 				a.setAction(ep);
 				a.setActor(getAID());
 
 				ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
 				req.addReceiver(request.getSender());
 				req.setLanguage(codec.getName());
 				req.setOntology(ontology.getName());
 				req.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
 
 				getContentManager().fillContent(req, a);
 
 				ACLMessage get_next_parameters_results = FIPAService.doFipaRequestClient(this, req);
 				// extract List of Evaluations from response
 				ContentElement content = getContentManager().extractContent(get_next_parameters_results);				
 				if (content instanceof Result) {					
 					Result result = (Result) content;
 					evaluations = (List)((List)result.getValue()).get(1);
 					solutions_new = (List)((List)result.getValue()).get(0);
 				}
 				updateFinished(evaluations);							
 			}
 			reply.setPerformative(ACLMessage.INFORM);
 			reply.setContent("finished");
 		} catch (FIPAException e) {
 			e.printStackTrace();
 			reply.setContent(e.getMessage());
 			reply.setPerformative(ACLMessage.FAILURE);
 		} catch (CodecException e) {
 			e.printStackTrace();
 			reply.setContent(e.getMessage());
 			reply.setPerformative(ACLMessage.FAILURE);
 		} catch (OntologyException e) {
 			e.printStackTrace();
 			reply.setContent(e.getMessage());
 			reply.setPerformative(ACLMessage.FAILURE);
 		}
 		
 		return reply;
 
 	}*/
 	
 	protected List getSchema() {
 		if(schema != null){
 			return schema;
 		}else{
 			return new ArrayList();
 		}
 		
 	}
 	protected List getSearch_options() {
 		if(search_options!=null){
 			return search_options;
 		}else{
 			return new ArrayList();
 		}
 	}
 	
 	/*Converts List of Evals to an array of values - at the moment only error_rate*/
 	private float[] namedEvalsToFitness(List named_evals) {
 		float[] res = new float[1];//named_evals.size...
 		jade.util.leap.Iterator itr = named_evals.iterator();
 		while(itr.hasNext()){
 			Eval e = (Eval)itr.next();
 			if(e.getName().compareTo("error_rate")==0)
 				res[0]=e.getValue();
 		}
 		return res;
 	}
 	
 	protected boolean registerWithDF() {
 		// register with the DF
 
 		DFAgentDescription description = new DFAgentDescription();
 		// the description is the root description for each agent
 		// and how we prefer to communicate.
 
 		description.setName(getAID());
 		// the service description describes a particular service we
 		// provide.
 		ServiceDescription servicedesc = new ServiceDescription();
 		// the name of the service provided (we just re-use our agent name)
 		servicedesc.setName(getLocalName());
 
 		// The service type should be a unique string associated with
 		// the service.s
 		String typeDesc = getAgentType();
 
 		servicedesc.setType(typeDesc);
 
 		// the service has a list of supported languages, ontologies
 		// and protocols for this service.
 		// servicedesc.addLanguages(language.getName());
 		// servicedesc.addOntologies(ontology.getName());
 		// servicedesc.addProtocols(InteractionProtocol.FIPA_REQUEST);
 
 		description.addServices(servicedesc);
 
 		// add "Search agent service"
 		ServiceDescription servicedesc_g = new ServiceDescription();
 
 		servicedesc_g.setName(getLocalName());
 		servicedesc_g.setType("Search");
 		description.addServices(servicedesc_g);
 
 		// register synchronously registers us with the DF, we may
 		// prefer to do this asynchronously using a behaviour.
 		try {
 			DFService.register(this, description);
 			System.out.println(getLocalName()
 					+ ": successfully registered with DF; service type: "
 					+ typeDesc);
 			return true;
 		} catch (FIPAException e) {
 			System.err.println(getLocalName()
 					+ ": error registering with DF, exiting:" + e);
 			// doDelete();
 			return false;
 
 		}
 	} // end registerWithDF
 	
 	private class RequestServer extends AchieveREResponder {
 		private static final long serialVersionUID = 6214306716273574418L;
 		GetOptions get_option_action;
 		GetParameters get_next_parameters_action;
 		public RequestServer(Agent a) {
 			super(a, MessageTemplate
 					.and(	MessageTemplate
 									.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
 							MessageTemplate.and(MessageTemplate
 									.MatchPerformative(ACLMessage.REQUEST),
 									MessageTemplate.and(MessageTemplate
 											.MatchLanguage(codec.getName()),
 											MessageTemplate.MatchOntology(ontology
 													.getName())))));
 			this.registerPrepareResultNotification(new Behaviour(a) {
 				boolean cont;
 				List solutions_new = null;
 				float evaluations[][] = null;
 				int queriesToProcess = 0;
 				@Override
 				public void action() {
 					cont = false;
 					if(get_option_action != null){
 						System.out.println("OK: GetOptions");
 						ACLMessage reply = getParameters((ACLMessage)getDataStore().get(REQUEST_KEY));
 						getDataStore().put(RESULT_NOTIFICATION_KEY, reply);
 					}
 					if(get_next_parameters_action!=null){
 						cont = true;
 						ACLMessage requestMsg = (ACLMessage)getDataStore().get(REQUEST_KEY);
 						if(queriesToProcess == 0){//skoncili jsme nebo zacali jeden cyklus query
 							
 							if(solutions_new == null){
 								System.out.println("OK: Pars - Nove solutiony vygenerovat");
								//zacatek - nastavani optionu
								search_options = get_next_parameters_action.getSearch_options();
								schema = get_next_parameters_action.getSchema();														
								loadSearchOptions();
 							}else{
 								//postprocess
 								System.out.println("OK: Pars - Update");
 								updateFinished(evaluations);
 							}
 							
 							if (finished()) {
 								//konec vsech evaluaci
 								System.out.println("OK: Pars - Ukoncovani");
 								solutions_new = null; 
 								evaluations = null; 
 								cont = false;
 								ACLMessage reply = ((ACLMessage)getDataStore().get(REQUEST_KEY)).createReply();
 								reply.setPerformative(ACLMessage.INFORM);
 								//TODO: co se posila zpet?
 								reply.setContent("finished");
 								
 								getDataStore().put(RESULT_NOTIFICATION_KEY, reply);
 							}else{
 								//nova vlna evaluaci - generovani query
 								System.out.println("OK: Pars - nove solutiony poslat");
 								solutions_new = generateNewSolutions(solutions_new, evaluations);
 								if(solutions_new!= null)
 									evaluations = new float[solutions_new.size()][];
 								queriesToProcess = solutions_new.size();
 								for(int i = 0; i < solutions_new.size(); i++){
 									//posli queries
 									ExecuteParameters ep = new ExecuteParameters();
 									//TODO zmena ExecuteParameters na jeden prvek
 									List solution_list = new ArrayList(1);
 									solution_list.add(solutions_new.get(i));
 									ep.setSolutions(solution_list);
 
 									Action action = new Action();
 									action.setAction(ep);
 									action.setActor(getAID());
 
 									//nedalo by se to klonovat?
 									ACLMessage query = new ACLMessage(ACLMessage.QUERY_REF);
 									query.addReceiver(requestMsg.getSender());
 									query.setLanguage(codec.getName());
 									query.setOntology(ontology.getName());
 									query.setProtocol(FIPANames.InteractionProtocol.FIPA_QUERY);
 									//identifikace query a jeho odpovedi!!!
 									query.setConversationId(Integer.toString(i));
 									try {
 										getContentManager().fillContent(query, action);
 									} catch (CodecException e) {
 										// TODO Auto-generated catch block
 										e.printStackTrace();
 									} catch (OntologyException e) {
 										// TODO Auto-generated catch block
 										e.printStackTrace();
 									}
 									myAgent.send(query);
 
 								}
 							}
 							
 						}else{//Cekame na vypocty - odpovedi na QUERY
 							//TODO: FAILURE
 							//and protocol FIPANames.InteractionProtocol.FIPA_QUERY???
 							ACLMessage response = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
 							if(response == null)
 								block();//elseif zadna zprava inform - cekej
 							else{
 								System.out.println("!OK: Pars - Prisla evaluace");
 								//prisla evaluace - odpoved na QUERY
 								//prirad inform ke spravnemu query
 								int id = Integer.parseInt(response.getConversationId());
 								Result res;
 								try {
 									res = (Result)getContentManager().extractContent(response);
 									List named_evals = (List)res.getValue();
 									evaluations[id]=namedEvalsToFitness(named_evals);
 								} catch (UngroundedException e) {
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 								} catch (CodecException e) {
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 								} catch (OntologyException e) {
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 								}
 								
 								queriesToProcess--;
 							}
 						}
 					}
 					//handle informs as query results
 				}
 
 
 				@Override
 				public boolean done() {
 					return !cont;
 				}
 				
 			});
 		}
 		
 		@Override
 		protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException{
 			System.out.println("request:" + request);
 
 			get_option_action = null;
 			get_next_parameters_action = null;
 			ContentElement content;
 			try {
 				content = getContentManager().extractContent(request);
 
 				if (((Action) content).getAction() instanceof GetOptions) {			
 					get_option_action = (GetOptions) ((Action) content).getAction();
 					System.out.println("get_option_action" + get_option_action);
 					return null;
 				} else if (((Action) content).getAction() instanceof GetParameters){
 					get_next_parameters_action = (GetParameters) ((Action) content).getAction();
 					ACLMessage agree = request.createReply();
 					agree.setPerformative(ACLMessage.AGREE);
 					agree.setContent(Integer.toString(query_block_size));
 					return agree; //or REFUSE, sometimes
 					//return null;
 				}
 			} catch (UngroundedException e) {
 				e.printStackTrace();
 			} catch (CodecException e) {
 				e.printStackTrace();
 			} catch (OntologyException e) {
 				e.printStackTrace();
 			}
 			throw new NotUnderstoodException("Not understood");
 		}
 		/*
 		@Override
 		protected ACLMessage prepareResultNotification(ACLMessage request,
 				ACLMessage response) {
 			if(get_option_action != null){
 				return getParameters(request);
 			}
 			if(get_next_parameters_action!=null){
 				return runSearchProtocol(request, get_next_parameters_action);
 			}
 			return null;
 		}*/
 
 	}
 		
 	protected void setup() {
 
 		System.out.println(getLocalName() + " is alive...");
 
 		getContentManager().registerLanguage(codec);
 		getContentManager().registerOntology(ontology);
 
 		registerWithDF();
 		
 		addBehaviour(new RequestServer(this));
 		
 	} // end setup()
 		
 }
