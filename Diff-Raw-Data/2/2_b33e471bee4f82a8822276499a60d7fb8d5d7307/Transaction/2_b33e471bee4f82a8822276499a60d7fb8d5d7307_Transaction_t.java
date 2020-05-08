 package billsplit.engine;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 public class Transaction extends BalanceChange {
 	public static Transaction current;
 	private Event eventOwner;
 	private PaymentMatrix matrix;
 	
 	/*
 	 * Constructors
 	 */
 	
 	public Transaction(ArrayList<Participant> participants) {
 		// call other constructor w/ empty list of items
 		this(participants, new ArrayList<Item>());
 	}
 	
 	public Transaction(ArrayList<Participant> participants, ArrayList<Item> items) {
 		super(participants,new ArrayList<Double>()); //parent BalanceChange constructor
 		this.matrix = new PaymentMatrix(participants,items);
 	}
 	
 	/*
 	 * Getter, setter and checker methods
 	 */
 	
 	public boolean containsItem(Item item) { 
 		return this.matrix.contains(item);
 	}
 	
 	public ArrayList<Item> getItems() {
 		return this.matrix.getItems();
 	}
 	
 	public void removeItem(Item item) {
 		this.matrix.removeItem(item);
 		this.updateTotals();
 	}
 	
 	public boolean addItem(Item item) {
 		return this.matrix.addItem(item);
 	}
 	
 	public Event getEventOwner() {
 		return eventOwner;
 	}
 
 	public void setEventOwner(Event eventOwner) {
 		this.eventOwner = eventOwner;
 	}
 	
 	/*
 	 * The following methods manipulate the actual payers/payees
 	 */
 	
 	/**
 	 * Clear any existing payers set for item, and then evenly split that item
 	 * cost among the specified participants.
 	 * @param item
 	 * @param participants
 	 */
 	public void setPayers(Item item, ArrayList<Participant> participants) {
 		//todo: check that there are more than 0 participants
 		matrix.reset(item); //remove any other payments on the item as a starting point
 		double amtPerPerson = item.getCost() / participants.size();
 		for (int j=0; j<participants.size(); j++) {
 			Participant p = participants.get(j);
 			this.matrix.setAmount(p, item, amtPerPerson);
 		}
 		this.updateTotals();
 	}
 	
 	/**
 	 * Using this method ASSUMES that you want to split payment evenly between the 
 	 * participants currently assigned to the item and the new participant p.
 	 * It will reset any custom payment values for all other participants already
 	 * assigned to pay for item to an even split between all involved.
 	 * @param item
 	 * @param p
 	 */
 	public void addPayer(Item item, Participant p) {
 		ArrayList<Participant> currentPayers = matrix.getPayers(item);
 		currentPayers.add(p);
 		this.setPayers(item, currentPayers); //evenly split btw new set of payers
 	}
 	
 	/**
 	 * Removes a payer from an item. Does NOT adjust the amounts that other payers are 
 	 * paying for that item (call setPayers() again on the remaining payers to redistribute
 	 * the amount evenly between them). Also note, this does not remove that participant
 	 * from the overall transaction; it only zeros their amount for that particular item.
 	 * @param item
 	 * @param p
 	 */
 	public void removePayer(Item item, Participant p) {
 		this.matrix.setAmount(p, item, 0.0);
 		this.updateTotals();
 	}
 	
 	/**
 	 * Literally splits every item cost evenly between all available participants.
 	 * We might want to revisit how useful this even is...
 	 */
 	public void splitAllEvenly() {
 		ArrayList<Item> items = this.matrix.getItems();
 		ArrayList<Participant> participants = this.matrix.getParticipants();
 		for (int j=0; j<items.size();j++) {
 			Item item = items.get(j);
 			this.setPayers(item, participants); //split item among all participants evenly
 		}
 	}
 	
 	public void resetSplitForItem(Item item) {
 		this.matrix.reset(item);
 		this.updateTotals();
 	}
 	
 	public void resetSplitForParticipant(Participant p) {
 		this.matrix.reset(p);
 		this.updateTotals();
 	}
 	
 	/**
 	 * Call the setAmountForPerson method inherited from balanceChange for each
 	 * participant in this Transaction. Call this method after any changes to
 	 * make sure the overall totals are correct (should be very quick to run).
 	 */
 	private void updateTotals() {
 		for (Participant p : this.participants) {
 			double total = this.matrix.getTotalForParticipant(p);
 			this.setAmountForPerson(p, total);
 		}
 	}
 	
 	/**
 	 * This is a placeholder method for some kind of OCR (or other external)
 	 * input to alter this Transaction object.
 	 */
 	public void fromExternal() {
 		throw new UnsupportedOperationException("Not implemented yet.");
 	}
 }
 
 class PaymentMatrix {
 	
 	private ArrayList<ArrayList<Double>> m; //will be accessed m[participant #][item #] = amount
 	private HashMap<Participant,Integer> participantMap;
 	private HashMap<Item,Integer> itemMap;
 	
 	public PaymentMatrix(ArrayList<Participant> participants,
 			ArrayList<Item> items) {
 		// Create 2d array using arraylists, initialized all to 0.0
 		m = new ArrayList<ArrayList<Double>>(participants.size());
 		for (int i=0; i<participants.size(); i++) {
 			ArrayList<Double> newRow = (new ArrayList<Double>(items.size()));
 			for (int j=0; j<items.size(); j++) {
 				newRow.add(j, Double.valueOf(0.0));
 			}
 			m.add(i,newRow);
 		}
		itemMap = new HashMap<Item,Integer>();
		participantMap = new HashMap<Participant,Integer>();
 	}
 	
 	public boolean contains(Participant p) {
 		return participantMap.containsKey(p);
 	}
 	
 	public boolean contains(Item i) {
 		return itemMap.containsKey(i);
 	}
 	
 	public void removeParticipant(Participant p) throws Exception{
 		int pid = this.getParticipantID(p);
 		participantMap.remove(p);
 		m.remove(pid);
 		
 	}
 	
 	public void removeItem(Item i) {
 		int iid = this.getItemID(i);
 		itemMap.remove(i);
 		for (int j=0; j<m.size();j++) {
 			m.get(j).remove(iid);
 		}
 		m.remove(iid);
 	}
 	
 	public boolean addParticipant(Participant p){
 		if (this.contains(p)) {
 			return false;
 		} else {
 			int newpid = participantMap.size();
 			participantMap.put(p, newpid);
 			ArrayList<Double> newRow = new ArrayList<Double>(itemMap.size());
 			for (int j=0; j<itemMap.size();j++) { newRow.set(j, Double.valueOf(0.0)); }
 			m.add(newRow); // add new participant column
 			return true;
 		}
 	}
 	
 	public boolean addItem(Item i) {
 		if (this.contains(i)) {
 			return false;
 		} else {
 			int newiid = itemMap.size();
 			itemMap.put(i, newiid);
 			for (int j=0; j<m.size(); j++) {
 				m.get(j).add(0.0); // add new item to each participant's column
 			}
 			return true;
 		}
 	}
 	
 	public double getAmount(Participant p, Item i) {
 		int pid = this.getParticipantID(p);
 		int iid = this.getItemID(i);
 		return m.get(pid).get(iid);
 	}
 	
 	public void setAmount(Participant p, Item i, double amount) {
 		int pid = this.getParticipantID(p);
 		int iid = this.getItemID(i);
 		m.get(pid).set(iid, amount);
 	}
 	
 	private int getParticipantID(Participant p) throws RuntimeException  {
 		if (this.contains(p)) {
 			int pid = participantMap.get(p);
 			return pid;
 		} else {
 			throw new RuntimeException(String.format("Participant %s does not exist in this PaymentMatrix.",p.getName()));
 		}
 	}
 	
 	private int getItemID(Item i) throws RuntimeException  {
 		if (this.contains(i)) {
 			int iid = itemMap.get(i);
 			return iid;
 		} else {
 			throw new RuntimeException(String.format("Item %s does not exist in this PaymentMatrix.",i.getName()));
 		}
 	}
 	
 	public ArrayList<Item> getItems() {
 		ArrayList<Item> items = new ArrayList<Item>(itemMap.size());
 				
 		for (HashMap.Entry<Item, Integer> entry : itemMap.entrySet()) {
 		    Item key = entry.getKey();
 		    int value = entry.getValue();
 		    items.set(value, key);
 		}
 		
 		return items;
 	}
 	
 	public ArrayList<Participant> getParticipants() {
 		ArrayList<Participant> participants = new ArrayList<Participant>(participantMap.size());
 				
 		for (HashMap.Entry<Participant, Integer> entry : participantMap.entrySet()) {
 			Participant key = entry.getKey();
 		    int value = entry.getValue();
 		    participants.set(value, key);
 		}
 		
 		return participants;
 	}
 	
 	public void reset(Participant p) {
 		int pid = this.getParticipantID(p);
 		ArrayList<Double> thisRow = m.get(pid);
 		for (int j=0; j<itemMap.size();j++) { thisRow.set(j, Double.valueOf(0.0)); }
 	}
 	
 	public void reset(Item i) {
 		int iid = this.getItemID(i);
 		for (int j=0; j<m.size();j++) {
 			m.get(j).set(iid,0.0); //reset amt to 0
 		}
 	}
 	
 	public ArrayList<Participant> getPayers(Item i) {
 		int iid = this.getItemID(i);
 		ArrayList<Participant> payers = new ArrayList<Participant>();
 		for (int j=0; j<m.size();j++) {
 			if (m.get(j).get(iid) > 0.0) {
 				payers.add(this.getKeyByValue(participantMap, j));
 			}
 		}
 		return payers;
 	}
 	
 	public ArrayList<Item> getPurchases(Participant p) {
 		int pid = this.getParticipantID(p);
 		ArrayList<Item> items = new ArrayList<Item>();
 		for (int j=0; j<itemMap.size();j++) {
 			if (m.get(pid).get(j) > 0.0) {
 				items.add(this.getKeyByValue(itemMap, j));
 			}
 		}
 		return items;
 	}
 	
 	public double getTotalForParticipant(Participant p) {
 		double total = 0.0;
 		ArrayList<Item> purchases = this.getPurchases(p);
 		for (int j=0; j<purchases.size(); j++) {
 			double contrib = this.getAmount(p, purchases.get(j));
 			total += contrib;
 		}
 		return total;
 	}
 	
 	private <T, E> T getKeyByValue(Map<T, E> map, E value) {
 	    for (Entry<T, E> entry : map.entrySet()) {
 	        if (value.equals(entry.getValue())) {
 	            return entry.getKey();
 	        }
 	    }
 	    return null;
 	}
 }
