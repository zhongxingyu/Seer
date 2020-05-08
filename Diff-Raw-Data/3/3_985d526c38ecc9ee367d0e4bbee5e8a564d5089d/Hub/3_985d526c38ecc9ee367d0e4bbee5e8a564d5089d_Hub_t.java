 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package sim.courierworld;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map.Entry;
 import sim.broker.Broker;
 import sim.broker.BrokerWithAuction;
 import sim.broker.BrokerWithoutAuction;
 import sim.courier.Courier;
 import sim.field.grid.Grid2D;
 import sim.field.grid.SparseGrid2D;
 import sim.field.network.Network;
 import sim.user.User;
 import sim.util.Bag;
 
 /**
  *
  * @author indranil
  */
 public class Hub
 {
 
     public CourierWorld state;
     public List<Courier> localCouriers;// the couriers in the local substrate of this hub
     public List<Broker> brokers; // the local brokers for this hub
     public List<Node> localNodes; // local nodes that branch out from this hub
     public Node myNode;
 
     Hub(CourierWorld state)
     {
         this.state = state;
     }
 
     public void setup(int useIndex)
     {
         boolean isAdded = false;
         int randx = 0, randy = 0;
 
         brokers = new ArrayList<>();
         brokers.add(new BrokerWithAuction());
         brokers.add(new BrokerWithoutAuction());
 
         localCouriers = new ArrayList<>();
         for (int i = 0; i < state.maxNumCouriersPerHub; i++)
         {
             localCouriers.add(new Courier(false));
         }
 
 
         // first we generate an (x,y) coordinate for the hub
         // ensuring that it is the correct distance away from other hubs
         while (!isAdded)
         {
             isAdded = true;
             randx = state.random.nextInt(state.grid.getWidth());
             randy = state.random.nextInt(state.grid.getHeight());
 
             Bag neighbr = new Bag();
             neighbr = state.grid.getMooreNeighbors(randx, randy, state.distFromHubs, Grid2D.BOUNDED, neighbr, null, null);
 
             if (!neighbr.isEmpty())
             {
                 for (int i = 0; i < neighbr.numObjs; i++)
                 {
                     if (((Node) neighbr.get(i)).isHub())
                     {
                         isAdded = false;
                         break;
                     }
                 }
             }
         }
         // we create a node to hold this (the network is filled only user nodes
         // and hub nodes)
         Node hubNode = new Node(this);
         myNode = hubNode;
         // add the hub to the grid and the network
         state.grid.setObjectLocation(hubNode, randx, randy);
 
 
         // Now we are going to create the local cliques
         // specific to the hub.  These nodes are user nodes
         // they will eventually have couriers.
         double probGenNode = 0.8;
         int cours[] = new int[state.maxNumCouriersPerHub];
 
         // indices into localCouriers
         for (int i = 0; i < cours.length; i++)
         {
             cours[i] = i;
         }
 
         int courierCount = 0;
         localNodes = new ArrayList<>();
         for (int i = 0; i < state.numLocalNode; i++)
         {
             if (state.random.nextDouble() < probGenNode)
             {
                 User user = new User(state.numMaxPkgs, useIndex, hubNode, state.maxPolicyVal*state.random.nextDouble());
                 Node userNode = new Node(user);
 
                 int nodex = -1, nodey = -1;
 
                 while (!(nodex >= 0 && nodex < state.grid.getWidth() && nodey >= 0 && nodey < state.grid.getHeight()))
                 {
                     nodex = randx + (int) ((1.0 - 2.0 * state.random.nextDouble()) * state.localCliqueSize);
                     nodey = randy + (int) ((1.0 - 2.0 * state.random.nextDouble()) * state.localCliqueSize);
 
                     if (!(state.grid.getObjectsAtLocation(nodex, nodey) == null))
                     {
                         nodex = -1;//to look for another location
                     }
                 }
                 // added local node to grid
                 state.grid.setObjectLocation(userNode, nodex, nodey);
 
                // now add the couriers that service that user
                if (courierCount >= state.maxNumCouriersPerHub)
                 {
                     // add random number of couriers to the userNode
                     int numCouriers = state.random.nextInt(state.maxNumCouriersPerNode - state.minNumCouriersPerNode) + state.minNumCouriersPerNode;
                     // shuffle the couriers indices
                     for (int j = 0; j < state.maxNumCouriersPerHub; j++)
                     {
                         int shuffle = state.random.nextInt(state.maxNumCouriersPerHub - j) + j;
                         cours[j] = cours[shuffle];
                     }
 
                     for (int j = 0; j < numCouriers; j++)
                     {
                         userNode.addCourier(localCouriers.get(cours[j]));
                         localCouriers.get(cours[j]).addSource(userNode);
                     }
 
                 } else
                 {
                     // add the couriers to the node
                     int numCouriers = state.random.nextInt(state.maxNumCouriersPerNode - state.minNumCouriersPerNode) + state.minNumCouriersPerNode;
                     int j;
                     for (j = 0; j < numCouriers; j++)
                     {
                         userNode.addCourier(localCouriers.get(cours[j]));
                         localCouriers.get(courierCount++).addSource(userNode);
                         if (courierCount >= state.maxNumCouriersPerHub)
                         {
                             break;
                         }
                     }
 
                     numCouriers -= j;
 
 
                     for (int k = 0; k < numCouriers; k++)
                     {
                         userNode.addCourier(localCouriers.get(k));
                         localCouriers.get(k).addSource(userNode);
                     }
                 }
 
                 localNodes.add(userNode);
                 useIndex++;
             }
         }
 
         // Set up the couriers for the local clique
         for (Courier curCour : localCouriers)
         {
             curCour.randInit(localNodes, state, hubNode);
         }
     }
 }
