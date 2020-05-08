 package P2PAgent;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import logging.Log;
 
 import P2PAgent.MessageP2P.Type;
 import agent.AbstractAgent;
 import agent.AbstractMeasure.NumericMeasure;
 import agent.AgentID;
 import agent.Measure;
 import agent.MeasureName;
 import agent.Measures;
 import base.Message;
 
 /**
  * class which will simulate the basic behaviors of an agent on a p2p network
  * 
  * @author Guillaume Masson
  * 
  */
 public class P2PAgent extends AbstractAgent {
 	/** Set of items that our agent has */
 	private Set<Item>							items;
 	/** Set which contains the items that our agent wants */
 	private Set<Item>							itemsWanted;
 	/** Map which contains the pending queries from the others agents */
 	private Map<AgentID, Set<Item>>				pendingQueries;
 	/** Map which contains the items location that our agent knows */
 	private Map<Item, Set<AgentID>>				itemsLocation;
 	/** Set of the other agent that our agent knows */
 	private Set<AgentID>						contacts;
 	/** all the measures of our agent */
 	private Measures							measures;
 	/**
 	 * probability that our agent will send a request about a file that it
 	 * doesn't want
 	 */
 	private NumericMeasure						probability;
 	/** inbox for all the messages(request, data...) that our agent received */
 	private List<MessageP2P<?>>					waitingMessage;
 	/** Map to do the link between an id and an agent */
 	private static HashMap<AgentID, P2PAgent>	directory;
 	/** Environment where our agent evolve */
 	private EnvironmentP2P						parent;
 	/**its a graphical variable to know if an agent is selected*/
 	private boolean selected;
 	/**graphical variable to know the global number of items */
 	private static Integer numberItem;
 	private static Integer numberItemWanted;
 	private static Integer numberItemLocation;
 	
 	
 	public P2PAgent(EnvironmentP2P parent, AgentID id) {
 		super(id);
 		this.items = new HashSet<Item>();
 		this.itemsWanted = new HashSet<Item>();
 		this.pendingQueries = new HashMap<AgentID, Set<Item>>();
 		this.itemsLocation = new HashMap<Item, Set<AgentID>>();
 		this.contacts = new HashSet<AgentID>();
 		this.measures = new Measures(this.id);
 		this.probability = (NumericMeasure) this.measures.createMeasure(new NumericMeasure(0.5,
 				MeasureName.PROBABILITY));
 		this.waitingMessage = new ArrayList<MessageP2P<?>>();
 		this.parent = parent;
 		this.selected=false;
 
 		if (directory == null) {
 			directory = new HashMap<AgentID, P2PAgent>();
 		}
 		P2PAgent.directory.put(this.id, this);
 		
 		if(numberItem == null)
 			numberItem = new Integer(0);
 		
 		if(numberItemWanted == null)
 			numberItemWanted = new Integer(0);
 		
 		if(numberItemLocation == null)
 			numberItemLocation = new Integer(0);
 	}
 
 	/** function to print some basic things about an agent */
 	@Override
 	protected void agentPrint() {
 		// TODO Auto-generated method
 		StringBuffer statPrint = new StringBuffer();
 		statPrint.append("\" agent ").append(this.id);
 		statPrint.append(" has ").append(this.items.size()).append(" items");
 		statPrint.append(" and it knows ").append(this.itemsLocation.size()).append("items");
 		statPrint.append(" and it wants ").append(this.itemsWanted.size()).append("items");
 
 		log.li("~", statPrint);
 	}
 
 	/*
 	 * protected void addPartner(AgentID agent, int step) { if (contacts.size()
 	 * >= maxNumberOfContacts) { this.deleteUselessContact(); }
 	 * this.contacts.put(agent, new Integer(step)); }
 	 * 
 	 * protected void deleteUselessContact() { int smallestStep=-1; AgentID
 	 * uselessAgent; for(Entry<AgentID, Integer> partner :
 	 * this.contacts.entrySet()) {
 	 * if((smallestStep>=partner.getValue().intValue()) || (smallestStep==-1)) {
 	 * smallestStep=partner.getValue().intValue();
 	 * uselessAgent=partner.getKey(); } } }
 	 */
 
 	@Override
 	/**
 	 * function to send a message to the others agents
 	 * @param to
 	 * @param msg
 	 */
 	protected void sendMessage(AgentID to, Message<?> msg) {
 		MessageP2P<?> msgP2P = (MessageP2P<?>) msg;
 		if ((P2PAgent.directory.containsKey(to)) && (to != msg.getFrom())) {
 			P2PAgent.getAgentById(to).receiveMessage(msg);
 			Set<Integer> idItem = new HashSet<Integer>();
 			if(msgP2P.getType() != Type.SEND_LOCATION)
 			{
 				for(Item item : (Set<Item>) msg.getContents())
 				{
 					idItem.add(new Integer(item.getItemID()));
 				}
 			}
 			else
 			{
 
 				for(Entry<AgentID, Set<Item>> itemsLocation : ((Map<AgentID, Set<Item>>) msg.getContents()).entrySet())
 				{
 					for(Item item : itemsLocation.getValue())
 					{
 						idItem.add(new Integer(item.getItemID()));
 					}
 				}
 			}
 			log.lf("sending "+msgP2P.getType()+ " for the items "+idItem.toString()+" to the agent ~ :", to.name);
 		}
 
 	}
 
 	@Override
 	/**function to receive a message from the others agents
 	 * 
 	 * @param msg
 	 */
 	public void receiveMessage(Message<?> msg) {
 		MessageP2P<?> msgP2P = (MessageP2P<?>) msg;
 		Set<Integer> idItem = new HashSet<Integer>();
 		if(msgP2P.getType() != Type.SEND_LOCATION)
 		{
 			for(Item item : (Set<Item>) msg.getContents())
 			{
 				idItem.add(new Integer(item.getItemID()));
 			}
 		}
 		else
 		{
 
 			for(Entry<AgentID, Set<Item>> itemsLocation : ((Map<AgentID, Set<Item>>) msg.getContents()).entrySet())
 			{
 				for(Item item : itemsLocation.getValue())
 				{
 					idItem.add(new Integer(item.getItemID()));
 				}
 			}
 		}
 		log.lf("received "+msgP2P.getType()+ " for the items "+idItem.toString()+" from the agent ~", msg.getFrom().name);
 		
 
 		this.waitingMessage.add((MessageP2P<?>) msg);
 		//System.out.println(this.id + " reois " + msg);
 	}
 
 	/**
 	 * step for one agent
 	 */
 	@Override
 	public void step() {
 		// TODO Auto-generated method
 
 		// print some information about the agent in the logs
 		agentPrint();
 
 		// we treat all the waitingMessage that we had previously received
 		this.waitingMessageTreatment();
 
 		// Treatment of the pending queries
 		this.pendingQueriesTreatement();
 
 		// we send our requests to our contacts about the items that our agent
 		// wants
 		if (!this.itemsWanted.isEmpty()) {
 			for (AgentID contact : this.contacts) {
 				// System.out.println(this.id+" veut "+ this.itemsWanted);
 				this.sendMessage(contact, new MessageP2P<Set<Item>>(this.id, Type.REQUEST_ITEM, this.itemsWanted));
 			}
 		}
 	}
 
 	/**
 	 * Treatment of the pending queries
 	 */
 	private void pendingQueriesTreatement() {
 		AgentID p2pAgent = null;
 		// our agent checks all the pendingQueries and try to respond
 		for (Entry<AgentID, Set<Item>> queries : this.pendingQueries.entrySet()) {
 			Iterator<Item> iteratorQueries = null;
 			Set<Item> itemsToSend = new HashSet<Item>();
 			Map<AgentID, Set<Item>> locationOfItemsRequested = new HashMap<AgentID, Set<Item>>(); // Map
 																									// which
 																									// contains
 																									// the
 																									// locations
 																									// of
 																									// the
 																									// items
 																									// that
 																									// a
 																									// contact
 																									// asking
 																									// for
 			Set<Item> itemToRemove = new HashSet<Item>(); // Set which contains
 															// the items that we
 															// will remove from
 															// the
 															// pendingQueries
 															// after treatment
 			Set<Item> locationToRequest = new HashSet<Item>();// Set which
 																// contains the
 																// list of items
 																// of which we
 																// have to ask
 																// the location
 																// to our
 																// contact to
 																// help an other
 																// agent
 
 			p2pAgent = queries.getKey();
 			iteratorQueries = queries.getValue().iterator();
 			for (Iterator<Item> it = iteratorQueries; it.hasNext();) {
 				Item requestedItem = it.next();
 				// if our agent possessed the requested item, he will send it
 				if (items.contains(requestedItem)) {
 					// here he will send the item
 					if (P2PAgent.getAgentById(p2pAgent).getItemsWanted().contains(requestedItem)) {
 						itemsToSend.add(requestedItem);
 						// System.out.println(this.id+ "envoie "+requestedItem);
 						itemToRemove.add(requestedItem);
 					}
 					// here if the contact doesn't want the item, but just the
 					// location, he will send it or if we don't have it we send
 					// it to an other contact with an certain probability
 					else {
 						if (!locationOfItemsRequested.containsKey(this.id)) {
 							Set<Item> itemPossessed = new HashSet<Item>();
 							locationOfItemsRequested.put(this.id, itemPossessed);
 						}
 						locationOfItemsRequested.get(this.id).add(requestedItem);
 						itemToRemove.add(requestedItem);
 					}
 				}
 				// if our agent doesn't possessed the item requested but he
 				// knows where to find it, he will inform the agent who queries
 				// this item
 				else if (this.itemsLocation.containsKey(requestedItem)) {
 					Iterator<AgentID> possessors = this.itemsLocation.get(requestedItem).iterator();
 					AgentID possessor = possessors.next();// we take the last
 															// agent of the list
 					// System.out.println("possesseur"+possessor);
 					if (!locationOfItemsRequested.containsKey(possessor) && (possessor != p2pAgent)) {
 						Set<Item> itemPossessed = new HashSet<Item>();
 						locationOfItemsRequested.put(possessor, itemPossessed);
 					}
 					// to be sure that we don't answer to an old request, we
 					// check if the agent who ask the location is not the same
 					// agent who want it
 					if (possessor != p2pAgent) {
 						locationOfItemsRequested.get(possessor).add(requestedItem);
 					}
 					itemToRemove.add(requestedItem);
 				}
 				// else our agent interrogate his contacts with a certain
 				// probability
 				else {
 
 					double calculateProba = Math.random();
 					// we calculate the probability, to know if we have to send
 					// the request to our contacts
 					if (calculateProba <= this.probability.getValue().doubleValue()) {
 						// System.out.println(this.id+" ajoute requete "+requestedItem);
 
 						// we will send the location for the item
 						locationToRequest.add(requestedItem);
 						itemToRemove.add(requestedItem);
 					}
 				}
 			}
 			// if we have some items location to ask, we send the request
 			if (!locationToRequest.isEmpty()) {
 				for (AgentID contact : this.contacts) {
 					this.sendMessage(contact, new MessageP2P<Set<Item>>(this.id, Type.ASK_LOCATION,
 							locationToRequest));
 				}
 			}
 			// if we have some items locations, we send them here
 			if (!locationOfItemsRequested.isEmpty()) {
 				this.sendMessage(queries.getKey(), new MessageP2P<Map<AgentID, Set<Item>>>(this.id,
 						Type.SEND_LOCATION, locationOfItemsRequested));
 			}
 			// if we have some items to send, we send them here
 			if (!itemsToSend.isEmpty()) {
 				this.sendMessage(queries.getKey(), new MessageP2P<Set<Item>>(this.id,
 						Type.SEND_ITEM, itemsToSend));
 			}
 			// if we have some request to send, we send them here
 			this.pendingQueries.get(p2pAgent).removeAll(itemToRemove);
 			itemToRemove.clear();
 		}
 
 	}
 
 	/**
 	 * Treatment of the messages that the agent has previously received
 	 */
 	private void waitingMessageTreatment() {
 		if (!waitingMessage.isEmpty()) {
 			for (MessageP2P<?> msg : waitingMessage) {
 				switch (msg.getType()) {
 					// An other agent asks our agent if he has these items
 					case REQUEST_ITEM:
 						// System.out.println("pour"+this.id+"requete de "+msg.getFrom()+""+msg.getContents());
 						Set<Item> queriesItems = null;
 						if (this.pendingQueries.containsKey(msg.getFrom())) {
 							queriesItems = this.pendingQueries.get(msg.getFrom());
 						} else {
 							queriesItems = new HashSet<Item>();
 						}
 						queriesItems.addAll((Set<Item>) msg.getContents());
 						this.pendingQueries.put(msg.getFrom(), queriesItems);
 					break;
 	
 					// an other agent informs our agent where to find some items
 					// that he wants
 					case SEND_LOCATION:
 						Map<AgentID, Set<Item>> responseToOurRequests = (Map<AgentID, Set<Item>>) msg
 								.getContents();
 	
 						for (Entry<AgentID, Set<Item>> information : responseToOurRequests.entrySet()) {
 							Set<Item> itemsForUs = new HashSet<Item>();
 							for (Item itemCheck : information.getValue()) {
 								//if we needed this item, we will send a request
 								if (this.itemsWanted.contains(itemCheck)) {
 									itemsForUs.add(itemCheck);
 								}
 								
 								//else if we look if we already possessed his location
 								else if (this.itemsLocation.containsKey(itemCheck)) {
 									this.itemsLocation.get(itemCheck).add(information.getKey());
 								} else {
 									Set<AgentID> agentLocation = new HashSet<AgentID>();
 									agentLocation.add(information.getKey());
 									this.itemsLocation.put(itemCheck, agentLocation);
 									
 									numberItemLocation= new Integer(numberItemLocation.intValue() + 1);
 								}
 							}
 							// our agent reacts by sending a request to the agent
 							// which has the requested items
 							if (!itemsForUs.isEmpty()) {
 								this.sendMessage(information.getKey(), new MessageP2P<Set<Item>>(
 										this.id, Type.REQUEST_ITEM, itemsForUs));
 	
 							}
 						}
 						break;
 	
 					// response to a request
 					case SEND_ITEM:
 						if (this.items.addAll((Set<Item>) msg.getContents()))
 							numberItem = new Integer(numberItem.intValue() + ((Set<Item>) msg.getContents()).size());
 						
 						if(this.itemsWanted.removeAll((Set<Item>) msg.getContents()))
 							numberItemWanted = new Integer(numberItemWanted.intValue() - ((Set<Item>) msg.getContents()).size());
 						
 					break;
 	
 					// ask for the location of an item
 					case ASK_LOCATION:
 						if (!this.pendingQueries.containsKey(msg.getFrom())) {
 							Set<Item> itemsLocRequested = new HashSet<Item>();
 							this.pendingQueries.put(msg.getFrom(), itemsLocRequested);
 						}
 						this.pendingQueries.get(msg.getFrom()).addAll((Set<Item>) msg.getContents());
 	
 					break;
 	
 					}
 			}
 			// we erase the inbox at the end
 			waitingMessage.clear();
 		}
 	}
 
 	@Override
 	public Measure<?> getMeasure(MeasureName measure) {
 		// TODO Auto-generated method stub
 		return this.measures.getMeasures().get(measure);
 	}
 
 	@Override
 	public Map<?, ?> getAllMeasures() {
 		// TODO Auto-generated method stub
 		return this.measures.getMeasures();
 	}
 
 	/** return an agent thanks to is id */
 	private static P2PAgent getAgentById(AgentID id) {
 		return P2PAgent.directory.get(id);
 	}
 
 	/** permit to get the items wanted by an agent */
 	public Set<Item> getItemsWanted() {
 		return itemsWanted;
 	}
 
 
 	/**Permit to set the environment of an agent*/
 	public void setParent(EnvironmentP2P parent) {
 		this.parent = parent;
 	}
 
 	/** Permit to get the contact of an agent */
 	public Set<AgentID> getContacts() {
 		return contacts;
 	}
 
 	/** Permit to set the contact of an agent */
 	public void setContacts(Set<AgentID> contacts) {
 		this.contacts = contacts;
 	}
 
 	public Set<Item> getItems() {
 		return items;
 	}
 	
 	@Override
 	public boolean isSelected()
 	{
 		// TODO Auto-generated method stub
 		return selected;
 	}
 
 	@Override
 	public void toggleSelected()
 	{
 		// TODO Auto-generated method stub
 		selected = !selected;
 		if (selected)
 			parent.addSelected(this);
 		else
 			parent.removeSelected(this);
 		parent.doUpdate();
 	}
 
 	public static Integer getNumberItem()
 	{
 		return numberItem;
 	}
 
 	public static void setNumberItem(Integer numberItem)
 	{
 		P2PAgent.numberItem = numberItem;
 	}
 
 	public static Integer getNumberItemWanted()
 	{
 		return numberItemWanted;
 	}
 
 	public static void setNumberItemWanted(Integer numberItemWanted)
 	{
 		P2PAgent.numberItemWanted = numberItemWanted;
 	}
 
 	public static Integer getNumberItemLocation()
 	{
 		return numberItemLocation;
 	}
 
 	public static void setNumberItemLocation(Integer numberItemLocation)
 	{
 		P2PAgent.numberItemLocation = numberItemLocation;
 	}
 	
 
 	/*static class test {
 		public static void main(String[] args) {
 			P2PAgent agent1 = new P2PAgent(null, new AgentID("" + 1));
 			P2PAgent agent2 = new P2PAgent(null, new AgentID("" + 2));
 			P2PAgent agent3 = new P2PAgent(null, new AgentID("" + 3));
 			P2PAgent agent4 = new P2PAgent(null, new AgentID("" + 4));
 
 			agent1.contacts.add(agent3.id);
 			agent3.contacts.add(agent4.id);
 			agent4.contacts.add(agent2.id);
 			agent2.contacts.add(agent3.id);
 
 			Item item1 = new Item(1);
 			Item item2 = new Item(2);
 			Item item3 = new Item(3);
 			Item item4 = new Item(4);
 
 			agent1.items.add(item1);
 			agent1.items.add(item4);
 			agent3.items.add(item2);
 			agent3.items.add(item1);
 			agent2.items.add(item3);
 
 			agent1.itemsWanted.add(item2);
 			agent1.itemsWanted.add(item3);
 			agent2.itemsWanted.add(item1);
 
 			System.out.println(item2);
 			System.out.println(item3);
 			System.out.println(item1);
 			List<P2PAgent> agents = new ArrayList<P2PAgent>();
 			agents.add(agent1);
 			agents.add(agent2);
 			agents.add(agent3);
 			agents.add(agent4);
 
 			int cpt2 = 0;
 			while (cpt2 != 10) {
 				for (P2PAgent agent : agents) {
 					agent.step();
 				}
 				cpt2++;
 				System.out.println("tour: " + cpt2);
 			}
 		}
 
 	}*/
 }
