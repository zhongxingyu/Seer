 package pikater;
 
 import jade.content.ContentElement;
 import jade.content.lang.Codec;
 import jade.content.lang.Codec.CodecException;
 import jade.content.lang.sl.SLCodec;
 import jade.content.onto.Ontology;
 import jade.content.onto.OntologyException;
 import jade.content.onto.UngroundedException;
 import jade.content.onto.basic.Action;
 import jade.content.onto.basic.Result;
 import jade.core.AID;
 import jade.core.Agent;
 import jade.core.Profile;
 import jade.core.behaviours.TickerBehaviour;
 import jade.domain.FIPAException;
 import jade.domain.FIPANames;
 import jade.domain.FIPAService;
 import jade.domain.FIPAAgentManagement.NotUnderstoodException;
 import jade.domain.FIPAAgentManagement.RefuseException;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 import jade.proto.AchieveREResponder;
 import jade.proto.ContractNetInitiator;
 import jade.proto.SubscriptionResponder.Subscription;
 import jade.util.leap.ArrayList;
 import jade.util.leap.Iterator;
 import jade.util.leap.List;
 import jade.wrapper.ControllerException;
 import jade.wrapper.PlatformController;
 import jade.wrapper.StaleProxyException;
 
 import java.sql.Timestamp;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Vector;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 import java.util.LinkedList;
 import java.util.regex.Pattern;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 import pikater.ontology.messages.DeleteTempFiles;
 
 import pikater.ontology.messages.CreateAgent;
 import pikater.ontology.messages.Data;
 import pikater.ontology.messages.Eval;
 import pikater.ontology.messages.Evaluation;
 import pikater.ontology.messages.EvaluationMethod;
 import pikater.ontology.messages.Execute;
 import pikater.ontology.messages.GetAllMetadata;
 import pikater.ontology.messages.GetDuration;
 import pikater.ontology.messages.GetFileInfo;
 import pikater.ontology.messages.GetFiles;
 import pikater.ontology.messages.GetTheBestAgent;
 import pikater.ontology.messages.Id;
 import pikater.ontology.messages.ImportFile;
 import pikater.ontology.messages.LoadResults;
 import pikater.ontology.messages.MessagesOntology;
 import pikater.ontology.messages.Metadata;
 import pikater.ontology.messages.Option;
 import pikater.ontology.messages.Results;
 import pikater.ontology.messages.SaveMetadata;
 import pikater.ontology.messages.SaveResults;
 import pikater.ontology.messages.SavedResult;
 import pikater.ontology.messages.Solve;
 import pikater.ontology.messages.Task;
 import pikater.ontology.messages.TranslateFilename;
 import pikater.ontology.messages.UpdateMetadata;
 
 public class Agent_Duration extends Agent {
 
 	private static final long serialVersionUID = -5555820420884978956L;
 
 	Codec codec = new SLCodec();
     Ontology ontology = MessagesOntology.getInstance();
     
     List durations = new ArrayList();
     
     int t = 10000; //ms
     AID aid = null;
     int id = 0;
     
     @Override
     protected void setup() {
             	
         getContentManager().registerLanguage(codec);
         getContentManager().registerOntology(ontology);
         
         // create linear regression agent
         // send message to AgentManager to create an agent
 		ACLMessage msg_ca = new ACLMessage(ACLMessage.REQUEST);
 		msg_ca.addReceiver(new AID("agentManager", false));
 		msg_ca.setLanguage(codec.getName());
 		msg_ca.setOntology(ontology.getName());
 		
 		CreateAgent ca = new CreateAgent();
 		ca.setType("LinearRegression");
 		ca.setName("DurationServiceRegression");
 //		List args = new ArrayList();
 //		args.add("weka.classifiers.functions.LinearRegression");
 //		ca.setArguments(args);				
 		
 		Action a = new Action();
 		a.setAction(ca);
 		a.setActor(this.getAID());
 				
 		String agent_name = null;
 
 		try {
 			getContentManager().fillContent(msg_ca, a);	
 			ACLMessage msg_name = FIPAService.doFipaRequestClient(this, msg_ca);
 			agent_name = msg_name.getContent();
 			aid = new AID(agent_name, AID.ISLOCALNAME);
 		} catch (FIPAException e) {
 			System.err.println("Exception while adding DurationServiceRegression agent: " + e);		
 		} catch (CodecException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (OntologyException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}					
         
         addBehaviour(new Test(this, t));			  
         
         MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchOntology(ontology.getName()), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
 
         addBehaviour(new AchieveREResponder(this, mt) {
 
             private static final long serialVersionUID = 1L;
 
             @Override
             protected ACLMessage handleRequest(ACLMessage request)
                     throws NotUnderstoodException, RefuseException {
 
                 try {
                     Action a = (Action) getContentManager().extractContent(request);
 
                     if (a.getAction() instanceof GetDuration) {
                         GetDuration gd = (GetDuration) a.getAction();
 
                         int duration = gd.getDuration();                       
                         
                         ACLMessage reply = request.createReply();
                         reply.setPerformative(ACLMessage.INFORM);
                         
                         reply.setContent(Float.toString(countDuration(duration)));
                         // Result r = new Result(gd, countDuration(duration));                        
 
                         return reply;
                     }
                 } catch (OntologyException e) {
                     e.printStackTrace();
                 } catch (CodecException e) {
                     e.printStackTrace();
                 }
 
                 ACLMessage failure = request.createReply();
                 failure.setPerformative(ACLMessage.FAILURE);
                 return failure;
             }
         });
     }    
     
     private float countDuration(int duration){    	
     	float number_of_LRs = 0; 
     	
     	int i = 1;
     	while (duration > 0){
     		float d;
     		if (duration < t){
     			d = duration;
     		}
     		else {
     			d = t;
     		}
     		
     		number_of_LRs += d / (Float)durations.get(durations.size()-i);
     		duration = duration - t;
     		
     		i++;    		
     	}
     			
     	System.out.println("duration:" + duration
     			+ ", number_of_LRs: " + number_of_LRs
     			+ ", i: " + i);
     	    	
     	return number_of_LRs;
     }
   
     protected class Test extends TickerBehaviour {
 
 		private static final long serialVersionUID = -2200601967185243650L;
 
 		public Test(Agent a, long period) {
 			super(a, period);
 			
 		}
 
 		protected void onTick() {
 			  // compute linear regression on random (but the same) dataset
 			  addBehaviour(new ExecuteTask(myAgent, createCFPmessage(aid)));  			    			  
 		} 
     }
     
 	protected class ExecuteTask extends ContractNetInitiator{
 
 		private static final long serialVersionUID = -4895199062239049907L;
 				
 		ACLMessage cfp; 
 		
 		public ExecuteTask(jade.core.Agent a, ACLMessage cfp) {
 			super(a, cfp);
 			this.cfp = cfp;
 		}
 
 		protected void handlePropose(ACLMessage propose, Vector v) {
 			// System.out.println(myAgent.getLocalName()+": Agent "+propose.getSender().getName()+" proposed "+propose.getContent());
 		}
 		
 		protected void handleRefuse(ACLMessage refuse) {
 			System.out.println(myAgent.getLocalName()+": Agent "+refuse.getSender().getName()+" refused");
 		}
 		
 		protected void handleFailure(ACLMessage failure) {
 			if (failure.getSender().equals(myAgent.getAMS())) {
 				// FAILURE notification from the JADE runtime: the receiver
 				// does not exist
 				System.out.println("Responder does not exist");
 			}
 			else {
 				System.out.println(myAgent.getLocalName()+": Agent "+failure.getSender().getName()+" failed");
 			}
 		}
 		
 		protected void handleAllResponses(Vector responses, Vector acceptances) {
 			// Evaluate proposals.
 			int bestProposal = Integer.MAX_VALUE;
 			AID bestProposer = null;
 			ACLMessage accept = null;
 			Enumeration e = responses.elements();
 			while (e.hasMoreElements()) {
 				ACLMessage msg = (ACLMessage) e.nextElement();
 				if (msg.getPerformative() == ACLMessage.PROPOSE) {
 					ACLMessage reply = msg.createReply();
 					reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
 					acceptances.addElement(reply);
 					int proposal = Integer.parseInt(msg.getContent());
 					if (proposal < bestProposal) {
 						bestProposal = proposal;
 						bestProposer = msg.getSender();
 						accept = reply;
 					}
 				}
 			}
 			// Accept the proposal of the best proposer
 			if (accept != null) {
 				// System.out.println(myAgent.getLocalName()+": Accepting proposal "+bestProposal+" from responder "+bestProposer.getName());
 				
 				try {
 					ContentElement content = getContentManager().extractContent(cfp);
 					Execute execute = (Execute) (((Action) content).getAction());
 					
 					Action a = new Action();
 					a.setAction(execute);
 					a.setActor(myAgent.getAID());
 												
 					getContentManager().fillContent(accept, a);
 
 				} catch (CodecException exception) {
 					// TODO Auto-generated catch block
 					exception.printStackTrace();
 				} catch (OntologyException exception) {
 					// TODO Auto-generated catch block
 					exception.printStackTrace();
 				}
 				
 				accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);				
 			}						
 			// TODO - if there is no proposer...
 		}
 				
 		protected void handleInform(ACLMessage inform) {
 			System.out.println(myAgent.getLocalName()+": Agent "+inform.getSender().getName()+" successfully performed the requested action");
 																			
 			ContentElement content;
 			try {
 				content = getContentManager().extractContent(inform);
 				if (content instanceof Result) {
 					Result result = (Result) content;					
 					List tasks = (List)result.getValue();
 					Task t = (Task) tasks.get(0);
 					
 					if (durations.size() > 100000) { // over 27 hours
 						durations.remove(0);
 					}
 					
 					// save the duration of the computation to the list
 					List ev = ((Evaluation)t.getResult()).getEvaluations();
 					Iterator itr = ev.iterator();
 					while (itr.hasNext()) {
 						Eval eval = (Eval) itr.next();
 						if(eval.getName().equals("duration")){
 							durations.add(eval.getValue());
 						}							
 					}							  			
 				}												
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
 		}			
 	} // end of call for proposal bahavior
 
         
     protected ACLMessage createCFPmessage(AID aid) {
 
 		// create CFP message for Linear Regression Computing Agent							  		
 		ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
 		cfp.setLanguage(codec.getName());
 		cfp.setOntology(ontology.getName());
 		cfp.addReceiver(aid);
 		cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
 
 		// We want to receive a reply in 10 secs
 		cfp.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
 
 		pikater.ontology.messages.Agent ag = new pikater.ontology.messages.Agent();
 		ag.setType("LinearRegression");
 		ag.setOptions(new ArrayList());
 
 		Data d = new Data();
		d.setTest_file_name("data/files/ffc587f1abf9cee29f011640d577ef22");
		d.setTrain_file_name("data/files/ffc587f1abf9cee29f011640d577ef22");
		d.setExternal_test_file_name("lineardata.arff");
		d.setExternal_train_file_name("lineardata.arff");
 		d.setMode("train_only");
 		
 		Task t = new Task();
 		Id _id = new Id();
 		_id.setIdentificator(Integer.toString(id));
 		t.setId(_id);
 		id++;
 		
 		t.setAgent(ag);
 		t.setData(d);
 		
 		EvaluationMethod em = new EvaluationMethod();
 		em.setName("Standard"); // TODO don't evaluate at all
 		
 		t.setEvaluation_method(em);
 		
 		t.setGet_results("after_each_computation");
 		t.setSave_results(false);
 
 		Execute ex = new Execute();
 		ex.setTask(t);
 		
 		try {
 			Action a = new Action();
 			a.setAction(ex);
 			a.setActor(this.getAID());
 										
 			getContentManager().fillContent(cfp, a);
 
 		} catch (CodecException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (OntologyException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 				
 		return cfp;
 
 	} // end createCFPmessage()
     
 }
