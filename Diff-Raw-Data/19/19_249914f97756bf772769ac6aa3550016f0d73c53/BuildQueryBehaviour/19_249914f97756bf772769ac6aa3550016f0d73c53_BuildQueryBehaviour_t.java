 	package main.jadrin.waiter;
 
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 
 import main.jadrin.ontology.CheckElement;
 import main.jadrin.ontology.QueryOntology;
 import main.jadrin.ontology.Type;
 import main.jadrin.tools.Tokenizer;
 import morfologik.stemming.IStemmer;
 import morfologik.stemming.PolishStemmer;
 import jade.content.AgentAction;
 import jade.content.ContentElement;
 import jade.content.lang.Codec.CodecException;
 import jade.content.onto.OntologyException;
 import jade.content.onto.UngroundedException;
 import jade.content.onto.basic.Action;
 import jade.core.AID;
 import jade.core.Agent;
 import jade.core.behaviours.OneShotBehaviour;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 
 public class BuildQueryBehaviour extends OneShotBehaviour {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -7602120656772135185L;
 	private String query;
 	private AID[] bartenders;
 	private WaiterAgentGui gui;
 	BuildQueryBehaviour(Agent agent, String query, AID[] bartenders, WaiterAgentGui gui)
 	{
 		super(agent);
 		this.query = query;
 		this.bartenders = bartenders;		
 		this.gui = gui;
 	}
 	
 	@Override
 	public void action() {
 		boolean drinkQuery = false;
 		boolean ingredientQuery = false;
 		
 		IStemmer stemmer = new PolishStemmer();
 		String[] notPermitted = {"prep"};
 		String[] tokens = Tokenizer.getTokens(stemmer, query, notPermitted);
 		
 		String drink = "";
         ArrayList<String> ingredients = new ArrayList<String>();
 		for (String str: tokens)
 		{
 			ACLMessage query = new ACLMessage(ACLMessage.REQUEST);
 			query.setOntology(QueryOntology.NAME);
 			query.setLanguage(((WaiterAgent)myAgent).getCodecName());
 			CheckElement toCheck = new CheckElement();
 			toCheck.setName(str);
 			toCheck.setPartOf(false);
 			toCheck.setType(Type.UNKNOWN);
 			AgentAction act = toCheck;
 			for(AID bartender: bartenders)
 			{
 				Action actOperator = new Action(bartender, act);
 				try {
 					myAgent.getContentManager().fillContent(query,actOperator);
 				   }
 				   catch (Exception ex) { ex.printStackTrace(); }
				query.addReceiver(bartender);
 				myAgent.send(query);
 				ACLMessage msg = myAgent.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
 			
 							try {
 						ContentElement ce =  myAgent.getContentManager().extractContent(msg);
 						if (ce instanceof Action)
 						{
 							 toCheck = (CheckElement)((Action) ce).getAction(); 
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
 							
 					if (toCheck != null && toCheck.getType() == Type.DRINK)
 					{
 						drinkQuery=true;
 						drink = toCheck.getName();
 					}
 					else if (toCheck != null && toCheck.getType() == Type.INGREDIENT)
 					{
 						ingredientQuery=true;
						if(!ingredients.contains(toCheck.getName()))
 							ingredients.add(toCheck.getName());
 					}
					query.removeReceiver(bartender);
 			}
 				
 		}
 		
 		String result = "";
 		
 		if (drinkQuery && !ingredientQuery)
 		{
 			result = "Spytałeś o skład " + drink;
 		}
 		else if (!drinkQuery && ingredientQuery)
 		{
 			String ing = "";
 			for(String ingredient : ingredients)
 				ing += ingredient + ", ";
			if(ing.length() > 1)
				ing = ing.substring(0, ing.length() - 2);
 			
 			result = "Spytałeś co można zrobić z " + ing;
 		}
 		else if (drinkQuery && ingredientQuery)
 		{
 			String ing = "";
 			for(String ingredient : ingredients)
 				ing += ingredient + ", ";
			if(ing.length() > 1)
				ing = ing.substring(0, ing.length() - 2);
 			
 			result = "Spytałeś czego brakuje Ci do " + drink + " mając " + ing;
 		}
 		else
 		{
 			result =" Nie znam odpowiedzi na to pytanie";
 		}
 		
 		gui.setResponse(result);
 		gui.setEditable(true);
 		
 
 	}
 
 }
