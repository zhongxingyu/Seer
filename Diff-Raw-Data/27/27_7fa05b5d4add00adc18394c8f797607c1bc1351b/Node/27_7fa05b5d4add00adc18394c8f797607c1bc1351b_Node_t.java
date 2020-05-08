 import java.util.ArrayList;
import java.sql.Date;
 
 public class Node
 {
   int nodeID, xCoord, yCoord, radioRange, numNodes, neighborSize;
   ArrayList<Node> allNodes = new ArrayList<Node>();
   ArrayList<Node> myNeighbors = new ArrayList<Node>();
   EDReceive eRecv = new EDReceive();
   PacketTransmitter pktTrans = new PacketTransmitter();
   PacketReceiver pktRecv = new PacketReceiver();
   RnFDataSend rnfSend = new RnFDataSend();
   ExpDataSend expSend = new ExpDataSend();
   ArrayList<Gradient> gradients = new ArrayList<Gradient>();
 
   public Node(int nodeID, int xCoord, int yCoord, int radioRange, int numNodes)
   {
     this.nodeID = nodeID;
     this.xCoord = xCoord;
     this.yCoord = yCoord;
     this.radioRange = radioRange;
     this.numNodes = numNodes;
     System.out.print("Node: "+nodeID+" has been created: ("+this.xCoord+","+this.yCoord+")\n");
     //System.out.print("numNodes = "+this.numNodes);
   }
 
   public int getID()
   {
     return this.nodeID;
   }
 
   public int getXCoord()
   {
     return this.xCoord;
   }
 
   public int getYCoord()
   {
     return this.yCoord;
   }
 
   public void setAllNodes(ArrayList<Node> allNodes)
   {
     this.allNodes = allNodes;
   }
 
   public void findNeighbors()
   {
     for(int i=0; i<numNodes; i++)
     {
       if(this.xCoord != allNodes.get(i).xCoord && this.yCoord != allNodes.get(i).yCoord)
       {
         int xDiff = Math.abs(allNodes.get(i).xCoord - this.xCoord);
         int yDiff = Math.abs(allNodes.get(i).yCoord - this.yCoord);
         //System.out.print("xDiff = "+xDiff+"\n");
         if(xDiff <= radioRange && yDiff <= radioRange)
         {
           myNeighbors.add(allNodes.get(i));
           //System.out.print("Found Neighbor: "+ allNodes.get(i).xCoord+","+allNodes.get(i).yCoord+ ")\n");
         }
       }
     }
 
     int neighborSize = myNeighbors.size();
     if(neighborSize==0)
     {
       System.out.print("Neighbor size = "+neighborSize+" Exit now.\n");
       System.exit(0);
     }else{
       System.out.print("Neighbor size = "+neighborSize+"\n");
     }
 
     System.out.print("Neigbors of Node ("+ xCoord+","+yCoord+ "):\n");
     for(int i=0; i<neighborSize; i++)
     {
       System.out.print("\t("+ myNeighbors.get(i).getXCoord()+","+myNeighbors.get(i).getYCoord()+ ")\n");
     }
 
     pktTrans.setNeighbors(myNeighbors);
   }
 
   public boolean processNode()
   {
     System.out.print("Node = "+this.nodeID+" is processing its node"+"\n");
     return false; //or true depending on if processing was successful
   }
 
   public void broadcastInterestMessage(Interest intr)
   {
    //just some values for now TODO: change them to make sense.
    char packetType = 'a';
    String senderID = "senderIDtest";
    double seqNumber = 1337;
    Date interval = new Date(1337);

    Packet pkt = new Packet(packetType, senderID, seqNumber, interval);
     //TODO: build broad interest message to be sent to PacketTransmitter
 //    PTrans.addPacket(pkt);
   }
 
   public void receivePacket(Packet pkt)
   {
     pktRecv.receivePacket(pkt);
   }
 }
