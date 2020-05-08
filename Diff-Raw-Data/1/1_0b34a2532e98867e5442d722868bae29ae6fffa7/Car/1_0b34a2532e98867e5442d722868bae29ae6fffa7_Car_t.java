 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package routeagents;
 
 import jade.core.AID;
 import jade.core.Agent;
 import jade.lang.acl.ACLMessage;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.Hashtable;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 /**
  *
  * @author Felipe V Nambara
  */
 public class Car extends Agent {
 
     int current = 0;
     boolean wait = false;
     ArrayList<Pair> pairs = new ArrayList<Pair>(); // route done by car    
 
     @Override
     protected void setup() {
 
         super.setup();
 
         RouteAgents.agents.add(this);
 
         addBehaviour(new CarReceiving(this));
 
         addBehaviour(new CarBehavior(this));
 
 
     }
 
     void startWay() {
 
         while (this.current != RouteAgents.graphRoute.length - 1) {
 
             ArrayList<Integer> neibhgours = getNeighbours();
 
             // Creating message to ask for route options
             StringBuffer message = new StringBuffer();
 
             message.append("01\n");
 
             for (int n : neibhgours) {
 
                 message.append(this.current);
                 message.append(";");
                 message.append(n);
                 message.append(";");
                 message.append("0");
                 message.append(";");
                 message.append("0");
                 message.append("\n");
 
             }
 
 
             ArrayList <Pair> ways = new ArrayList<Pair>();
             
             // Ask antoher agents for options
 
             ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
 
             for (Agent a : RouteAgents.agents) {
 
                 if (!a.getAID().getLocalName().equals(this.getAID().getLocalName())) {
 
                     msg.addReceiver(new AID(a.getLocalName(), AID.ISLOCALNAME));
 
                 }
 
             }
 
             msg.setContent(message.toString());
 
             this.send(msg);
 
             String[] parse;
 
             ACLMessage rec;
 
             while ((rec = receive()) != null) {
 
                 if (rec != null) {
 
                     parse = rec.getContent().split("\n");
 
                     if (parse[0].equals("02")) {
 
                         for (int i = 1; i < parse.length; i++) {
 
                             String[] pair = parse[i].split(";");
 
                             Pair p = new Pair(Integer.parseInt(pair[0]), Integer.parseInt(pair[1]), Double.parseDouble(pair[2]));
                                                         
                             ways.add(p);
 
                         }
 
                     }
 
                 }
             }
 
             boolean withoutOptions = true;
 
             //int loops = 0;
 
             for (Pair p : ways) {
                 withoutOptions = withoutOptions && neibhgours.indexOf(p.getEnd()) == -1;
             }
            withoutOptions = withoutOptions && ways.size() == neibhgours.size();
 
             // Trying to find options 15 times...
             /*while (ways.size() == 0 && loops <= 1) {
 
              // Doesn't have options if none of the agents did the same way
 
              for (Pair p : ways) {
              withoutOptions = withoutOptions && neibhgours.indexOf(p.getEnd()) == -1;
              }
 
              //System.out.println("agente " + this.getAID().getLocalName() + " aguardando resposta...");
 
              if (ways.size() > 0) {
              System.out.println("agente " + this.getAID().getLocalName() + " encontrou outros agentes que fizeram este caminho !!!!!!!!!!!!!!!!!!!!!!!!!");
              }
              // Give a time to agents make the wanted way
 
              try {
              Thread.sleep(100);
              } catch (InterruptedException ex) {
              Logger.getLogger(Car.class.getName()).log(Level.SEVERE, null, ex);
              }
 
              loops++;
              }*/
 
 
             Hashtable prob = new Hashtable();
 
             // Probabilities are the same if doesn't have options...
             if (withoutOptions) {
 
                 for (int n : neibhgours) {
 
                     prob.put(n, 100.00 / neibhgours.size());
 
                 }
 
             } else {
                 // ...or are proportional to the routes's intervals
                 double inverseTotal = 0;
                 Hashtable inverses = new Hashtable();
 
                 for (Pair pair : ways) {
 
                     double inverse = 1 / pair.getInterval();
                     inverses.put(pair.getEnd(), inverse);
                     inverseTotal += inverse;
 
                 }
 
                 for (int n : neibhgours) {
 
                     double p = ((Double) inverses.get(n)) * 100.00 / inverseTotal;
                     prob.put(n, p);
 
                 }
 
             }
 
             // Heuristic route choice based on the probabilities
             int v = 0;
             double x = Math.random() * 100;
             double y = 0;
 
             for (int n : neibhgours) {
 
                 y += (Double) prob.get(n);
                 if (x < y) {
                     v = n;
                     break;
                 }
 
             }
 
             System.out.println("agente " + this.getAID().getLocalName() + " saiu do vértice " + this.current + " para o " + v);
 
             moveTo(this.current, v);
 
         }
 
         System.out.println("agente " + this.getAID().getLocalName() + " finalizando caminho");
     }
 
     ArrayList<Integer> getNeighbours() {
 
         ArrayList<Integer> neighbours = new ArrayList<Integer>();
 
         for (int i = 0; i < RouteAgents.graphRoute[current].length; i++) {
 
             if (RouteAgents.graphRoute[current][i] == 1) {
 
                 neighbours.add(i);
 
             }
 
         }
 
         return neighbours;
     }
 
     void moveTo(int start, int end) {
 
         Pair pair = new Pair(start, end, calculateInterval(start, end));
 
         pairs.add(pair);
 
         this.current = end;
 
     }
 
     private Double calculateInterval(int start, int end) {
         Double interval;
 
         interval = RouteAgents.graphRoute[start][end] / RouteAgents.graphVelocity[start][end];
 
         return interval;
     }
 }
