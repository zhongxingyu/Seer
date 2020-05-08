 /**
  * 
  */
 package behaviours;
 
 import java.util.Iterator;
 
 import marketeer.Marketeer;
 import marketeer.Marketeer.PartType;
 import ontology.Bid;
 import ontology.MarketOntology;
 import jade.content.lang.Codec.CodecException;
 import jade.content.onto.OntologyException;
 import jade.core.AID;
 import jade.core.behaviours.OneShotBehaviour;
 import jade.domain.FIPAException;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.domain.FIPAAgentManagement.Property;
 import jade.domain.FIPAAgentManagement.ServiceDescription;
 import jade.lang.acl.ACLMessage;
 
 /**
  * @author Peter
  * 
  */
 public class BuyBehaviour extends OneShotBehaviour {
 	/**
 	 * @var long serialVersionUID
 	 */
 	private static final long serialVersionUID = -2152306702531710010L;
 
 	private static int conversationId = 0;
 
 	private Marketeer agent;
 	private PartType partType;
 	private int budget;
 
 	/**
 	 * @param a
 	 */
 	public BuyBehaviour(Marketeer agent, PartType partType, int budget) {
 		super(agent);
 		this.agent = agent;
 		this.partType = partType;
 		this.budget = budget;
 	}
 
 	@Override
 	public void action() {
 		try {
 			System.out.printf("BuyBehaviour quering for %s%n", partType);
 			DFAgentDescription[] hits = agent.queryForDFAuctions(partType);
 
 			int price, lowestPrice = Integer.MAX_VALUE;
 			AID bestAuctioneer = null;
 
 			if(hits.length == 0) {
 				System.out.printf("BuyBehaviour no hits, selling furnace%n");
 				agent.sellFurnaceRemains();
 				return;
 			} else {
 				for (DFAgentDescription agentDescription : hits) {
 					Iterator<ServiceDescription> services = agentDescription.getAllServices();
 					while (services.hasNext()) {
 						ServiceDescription service = services.next();
 						Iterator<Property> properties = service.getAllProperties();
 						while (properties.hasNext()) {
 							Property property = properties.next();
 							if (property.getName().equals("prijs")) {
								price = (int) property.getValue();
 								if (price < lowestPrice) {
 									lowestPrice = price;
 									bestAuctioneer = agentDescription.getName();
 								}
 								break;
 							}
 						}
 					}
 				}
 			}
 
 			System.out.printf("BuyBehaviour lowestPrice: %d sending bid%n", lowestPrice);
 			
 			agent.addBehaviour(new HandleBidResponse(agent, conversationId));
 			
 			Bid bid = new Bid();
 			bid.setName(partType.name());
 			
 			if(lowestPrice <= budget) {
 				System.out.printf("BuyBehaviour lowestPrice: %d sending bid%n", lowestPrice);
 			} else {
 				System.out.printf("BuyBehaviour lowestPrice: %d too high. Sending lower bid%n", lowestPrice);
 			}
 			
 			bid.setPrice(lowestPrice <= budget ? lowestPrice : budget);
 
 			ACLMessage bidMessage = new ACLMessage(ACLMessage.PROPOSE);
 			bidMessage.addReceiver(bestAuctioneer);
 			bidMessage.setOntology(MarketOntology.NAME);
 			bidMessage.setConversationId("" + conversationId);
 			agent.getContentManager().fillContent(bidMessage, bid);
 			agent.send(bidMessage);
 			
 			conversationId++;
 		} catch (FIPAException | CodecException | OntologyException fe) {
 			fe.printStackTrace();
 		}
 	}
 }
