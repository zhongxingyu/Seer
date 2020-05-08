 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package agents.actors;
 
 /**
  *
  * @author Zagadka
  */
 import messages.Item;
 import behaviours.person.GetDirections;
 import behaviours.person.GetStoreLocation;
 import behaviours.person.ReceiveItem;
 import behaviours.person.ReceiveItemList;
 import jade.core.AID;
 import jade.core.Agent;
 import jade.domain.DFService;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.domain.FIPAAgentManagement.SearchConstraints;
 import jade.domain.FIPAAgentManagement.ServiceDescription;
 import jade.domain.FIPAException;
 import jade.lang.acl.ACLMessage;
 import java.util.ArrayList;
 import java.util.Random;
 import messages.AgentData;
 import simulator.logic.Move;
 
 public class GeneralPerson extends Agent implements agents.AgentInterface {
 
     private int posx, posy;
     private int[] destination = new int[2];
     private int wait = 3500;
     private int section;
     private int money = 0;
     private ArrayList<Item> shoppingbag = new ArrayList<Item>();
     public boolean busy = false;
     //For shopping puposes, the current item the person is looking at 
     //and the shop the item is at:
     private String item = null;
     private AID shop = null;
 
     @Override
     protected void setup() {
         Object[] args = getArguments();
 
         this.posx = Integer.parseInt(String.valueOf(args[0]).substring(0, 4));
         this.posy = Integer.parseInt(String.valueOf(args[0]).substring(4, 8));
         this.money = Integer.parseInt(String.valueOf(args[1]));
         this.section = Integer.parseInt(String.valueOf(args[2]));
 
 
         destination[0] = posx;
 
         destination[1] = posy;
 
         doWait(2000);
 
         System.out.println("Hallo World ! my name is " + this.getLocalName()
                 + " i am a straight up 'general person' agent.");
         Addme();
         addBehaviour(new GetDirections(this));
         addBehaviour(new ReceiveItemList(this));
         addBehaviour(new GetStoreLocation(this));
         addBehaviour(new ReceiveItem(this));
 
 
         browse();
 
     }
 
     /*
      * Browse a shop so far only the generalItems shop i supported:
      */
     public void browse() {
         busy = true;
         Random random = new Random();
 
         AID shops[] = searchDF("GeneralItems");
 
         shop = shops[random.nextInt(shops.length)];
 
         ACLMessage aclMessage = new ACLMessage(ACLMessage.REQUEST);
         aclMessage.setConversationId("GeneralItems");
         aclMessage.addReceiver(shop);
         aclMessage.setContent("giveItemList");
 
         this.send(aclMessage);
     }
 
     /*sending information that the agent is waiting to the collector:
      *     * 
      */
     public void waitingToCollector() {
         ACLMessage conMessage = new ACLMessage(ACLMessage.INFORM);
         AID collector = new AID(COLLECTOR, AID.ISLOCALNAME);
         
         conMessage.setConversationId("personwaiting");
         conMessage.setContent("waiting");
         conMessage.addReceiver(collector);
         this.send(conMessage);
     }
 
     /*
      * Function to make the actor choose an item at random from the list received 
      * via the ReceiveItemList behaviour.
      */
     public void chooseItem(String item) {//, AID shop){   
         this.item = item;
         //this.shop = shop;
         ACLMessage aclMessage = new ACLMessage(ACLMessage.REQUEST);
         aclMessage.setConversationId("givestorelocation");
         aclMessage.addReceiver(shop);
         aclMessage.setContent("givelocation");
         this.send(aclMessage);
     }
 
     public void buyItem() {
         sendMessage("buying: " + item);
 
         ACLMessage aclMessage = new ACLMessage(ACLMessage.PROPOSE);
         aclMessage.setConversationId("buyingitem");
         aclMessage.addReceiver(shop);
         aclMessage.setContent(item);
         this.send(aclMessage);
         //System.out.println("buying:" + item);
     }
 
     public void receiveItem(Item item) {
         shoppingbag.add(item);
 
         busy = false;
 
         //System.out.println(item.getCost() + " : " + this.getLocalName() + busy);  
 
         doWait(1000);
 
         browse();
 
 
     }
 
     public void moveToCross(int posx, int posy) {
         int[] to = {posx, posy};
         move(to);
 
         this.doWait(100);
 
         move(destination);
 
         this.doWait(100);
 
         buyItem();
 
     }
 
     public void movetoStore(int storesection, int[] destination) {
         this.destination = destination;
        if (storesection != this.section) {
            this.section = storesection;
             AID r = lookforValidCross(storesection);
             ACLMessage aclMessage = new ACLMessage(ACLMessage.REQUEST);
             aclMessage.addReceiver(r);
             aclMessage.setContent("givenewposition");
             aclMessage.setConversationId("directions");
             this.send(aclMessage);
         } else {
             move(destination);
             this.doWait(100);
 
             buyItem();
         }
 
     }
 
     private AID lookforValidCross(int desSection) {
         AID[] cross = searchDF("CrossSection");
         //System.out.print("\nCROSSSECTIONS: ");
         for (int i = 0; i < cross.length; i++) {
             String name = cross[i].getLocalName();
             if (name.contains(Integer.toString(this.section))
                     && name.contains(Integer.toString(desSection))) {
                 //System.out.println(name);
                 return cross[i];
             }
         }
         return null;
     }
     /*
      * Searching for service methods, one returns a list of AIDs offering the wanted
      * service, the other just returns a single one:
      */
 
     private AID getService(String service) {
         DFAgentDescription dfd = new DFAgentDescription();
         ServiceDescription sd = new ServiceDescription();
         sd.setType(service);
         dfd.addServices(sd);
         try {
             DFAgentDescription[] result = DFService.search(this, dfd);
             if (result.length > 0) {
                 return result[0].getName();
             }
         } catch (FIPAException fe) {
         }
         return null;
     }
 
     private AID[] searchDF(String service) {
         DFAgentDescription dfd = new DFAgentDescription();
         ServiceDescription sd = new ServiceDescription();
         sd.setType(service);
         dfd.addServices(sd);
 
         SearchConstraints ALL = new SearchConstraints();
         ALL.setMaxResults(new Long(-1));
         try {
             DFAgentDescription[] result = DFService.search(this, dfd, ALL);
             AID[] agents = new AID[result.length];
             for (int i = 0; i < result.length; i++) {
                 agents[i] = result[i].getName();
             }
             return agents;
         } catch (FIPAException fe) {
         }
         return null;
     }
 
     /*
      * Enables the person to move to a store, simulationwise.
      */
     public void move(int[] to) {
         int[] from = {posx, posy};
         //double[] to = {700, 700}; 
         Move move = new Move();
 
         sendMessage("moving to: " + shop.getLocalName());
 
         while (Math.abs(to[0] - posx) > 30 || Math.abs(to[1] - posy) > 30) {
             from = move.getNextPos(to, from, 100, 500);
             posx = (int) from[0];
             posy = (int) from[1];
             updatePosition();
             doWait(30);
 
         }
     }
 
     /*
      * Communication concerning graphics rendering:
      */
     public void sendMessage(String message) {
         AID r = new AID(RENDERAGENT, AID.ISLOCALNAME);
         ACLMessage aclMessage = new ACLMessage(ACLMessage.INFORM);
         aclMessage.setConversationId("conversation");
         aclMessage.addReceiver(r);
         aclMessage.setContent(message);
         this.send(aclMessage);
     }
 
     private void Addme() {
         AgentData position = new AgentData();
         position.setPosX(posx);
         position.setPosY(posy);
         position.setName(this.getLocalName());
         position.setType("person");
 
         AID render = new AID(RENDERAGENT, AID.ISLOCALNAME);
         ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
         msg.addReceiver(render);
         try {
             msg.setContentObject(position);
         } catch (Exception ex) {
         }
 
         send(msg);
     }
 
     private void updatePosition() {
         AgentData position = new AgentData();
         position.setPosX(posx);
         position.setPosY(posy);
         position.setName(this.getLocalName());
         position.setType("person");
 
         AID render = new AID(RENDERAGENT, AID.ISLOCALNAME);
         ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
         msg.setConversationId("positionupdate");
         msg.addReceiver(render);
         try {
             msg.setContentObject(position);
         } catch (Exception ex) {
         }
 
         send(msg);
     }
 }
