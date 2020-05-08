 package algorithms;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.List;
 
 import messages.byzantinegenerals.ByzantineMessage;
 import teachnet.algorithm.BasicAlgorithm;
 
 /**
  * @author Tim Strehlow 316594, Bjoern Stabel 222128, Friedrich Maiwald 350570
  *         Gruppe 08
  */
 
 public class ByzantineGenerals extends BasicAlgorithm {
 
 	private int id;
 	String caption;
 	private Color color = Color.WHITE;
 
 	private boolean isLoyal;
 	private boolean isLeader;
 
 	private boolean currentValue;
 
 	private List<ByzantineMessage> receivedMsgs;
 
 	public void setup(java.util.Map<String, Object> config) {
 		this.id = (Integer) config.get("node.id");
 		// Z. Zt. wird einfach immer der Knoten mit der ID 0 der Leader
 		this.isLeader = (id == 0);
 		if (this.isLeader) {
 			this.color = Color.green;
 		}
 		this.currentValue = false;
 		this.receivedMsgs = new ArrayList<ByzantineMessage>();
 
 		this.isLoyal = ((this.id + 1) % 3 != 0);
 		if (!this.isLoyal) {
 			this.color = Color.red;
 		}
 		updateCaption();
 	}
 
 	@Override
 	public void initiate() {
 		if (this.isLeader) {
 			final ByzantineMessage msgToSend = new ByzantineMessage(
 					new int[] { this.id }, true, false);
 			for (int i = 0; i < checkInterfaces(); i++) {
 				send(i, msgToSend);
 			}
 		}
 	}
 
 	@Override
 	public void receive(int interf, Object message) {
 		if (message instanceof ByzantineMessage) {
 			final ByzantineMessage receivedMsg = (ByzantineMessage) message;
 
 			// drop messages that already contain this node's id
 			if (contains(receivedMsg.getIdPath(), this.id)) {
 				return;
 			}
 
 			// save message to list
 			this.receivedMsgs.add(receivedMsg);
 
 			// construct new message
 			final int[] oldIdPath = receivedMsg.getIdPath();
 
 			if (oldIdPath.length < getRounds()) {
 				// construct newIdPath from oldIdPath + this node's id
 				final int[] newIdPath = new int[oldIdPath.length + 1];
 				System.arraycopy(oldIdPath, 0, newIdPath, 0, oldIdPath.length);
 				newIdPath[newIdPath.length - 1] = this.id;
 
 				boolean valueToSend = receivedMsg.getValue();
 
 				// if this node is not loyal, revert the message's value
 				if (!isLoyal) {
 					valueToSend = !valueToSend;
 				}
 
 				final ByzantineMessage msgToSend = new ByzantineMessage(
 						newIdPath, valueToSend, !isLoyal);
 
 				for (int i = 0; i < checkInterfaces(); i++) {
 					// Message is sent to all nodes, except those that are
 					// already in the message's path
 					// relies on the interfaces to have the same id as the node
					// they are conencted to
 					if (i != interf && !contains(newIdPath, i)) {
 						send(i, msgToSend);
 					}
 				}
 			} else {
				// TODO Check if lieutenant has received all messages

				// wie berechnet man hier wann der general alle nachrichten
				// erhalten hat?
				// 400 ist der wert für 10 generäle und 3 Verräter
				// 25 für 7 generäle und 2 verräter
				// 3 für 4 generäle und 1 verräter
				if (this.receivedMsgs.size() == 25) {
 					buildMajority();
 				}
 			}
 		}
 
 	}
 
 	private boolean contains(int[] array, int value) {
 		for (final int arrValue : array) {
 			if (arrValue == value)
 				return true;
 		}
 		return false;
 	}
 
 	private void buildMajority() {
		System.out.println("buildMajority()");
 		// Build tree
 		int trueCount = 0, falseCount = 0;
 		for (ByzantineMessage msg : getMsgsWithShortestPath()) {
 			buildParents(msg);
 			buildMajorityRecursively(msg);
 			if (msg.getMajority()) {
 				trueCount++;
 			} else {
 				falseCount++;
 			}
 		}
 		this.currentValue = (trueCount >= falseCount);
 		updateCaption();
 	}
 
 	private void buildMajorityRecursively(final ByzantineMessage msgToStart) {
 		if (msgToStart.getChildren().size() > 0) {
 			int trueCount = 0, falseCount = 0;
 			for (ByzantineMessage msg : msgToStart.getChildren()) {
 				buildMajorityRecursively(msg);
 				if (msg.getMajority()) {
 					trueCount++;
 				} else {
 					falseCount++;
 				}
 			}
 			msgToStart.setMajority(trueCount >= falseCount);
 
 		} else {
 			msgToStart.setMajority(msgToStart.getValue());
 		}
 
 	}
 
 	private void buildParents(final ByzantineMessage parentMessage) {
 		for (final ByzantineMessage msg : receivedMsgs) {
 			if (msg.getIdPath().length == parentMessage.getIdPath().length + 1) {
 				boolean isParent = true;
 				for (int i = 0; i < parentMessage.getIdPath().length; i++) {
 					if (parentMessage.getIdPath()[i] != msg.getIdPath()[i]) {
 						isParent = false;
 						break;
 					}
 				}
 				if (isParent) {
 					parentMessage.addChild(msg);
 				}
 			}
 		}
 	}
 
 	private List<ByzantineMessage> getMsgsWithShortestPath() {
 		int shortestPathLength = Integer.MAX_VALUE;
 		List<ByzantineMessage> msgs = new ArrayList<ByzantineMessage>();
 		for (final ByzantineMessage msg : receivedMsgs) {
 			if (msg.getIdPath().length < shortestPathLength) {
 				shortestPathLength = msg.getIdPath().length;
 				msgs = new ArrayList<ByzantineMessage>();
 			} else if (msg.getIdPath().length == shortestPathLength) {
 				msgs.add(msg);
 			}
 		}
 		return msgs;
 	}
 
 	private int getRounds() {
 		return ((checkInterfaces() + 1) / 3) + 1;
 	}
 
 	private void updateCaption() {
 
 		this.caption = Integer.toString(id) + " value: " + this.currentValue;
 	}
 }
