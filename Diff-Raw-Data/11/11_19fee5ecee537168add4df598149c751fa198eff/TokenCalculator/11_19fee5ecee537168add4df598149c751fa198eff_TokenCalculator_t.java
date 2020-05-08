 package com.jaxio.clparty;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class TokenCalculator {
 
     // Assuming RackInferringSnitch and NetworkTopologyStrategie
     public static void main(String args[]) {
         // create a participant pool
         List<String> participants = new ArrayList<String>();
         for (int i=0; i < 100; i++) {
             participants.add("PrénomNom" +i);
         }
         
         // input
        int nbDataCenter = 1;
        int nbRackPerDataCenter = 1;
        int nbParticipantsPerRack = 4;
 
         
         List<Participant> nodes = calculate(participants, nbDataCenter, nbRackPerDataCenter, nbParticipantsPerRack);
         
         // print the DC/Rack/IP/Token assignments...
         for (Participant p : nodes) {
             System.out.println(p.toString());
         }
     }
     
     public static List<Participant> calculate(List<String> participants, int nbDataCenter, int nbRackPerDataCenter, int nbParticipantPerRack) {
         List<Participant> result = new ArrayList<Participant>();
         int participantIndex =0;
         
         int nbParticipantPerDataCenter = nbRackPerDataCenter * nbParticipantPerRack;
         
         for (int dc=1; dc <= nbDataCenter; dc++) {
             for (int rack=1; rack <= nbRackPerDataCenter; rack++) {
                 for (int positionInRack = 1; positionInRack <= nbParticipantPerRack ; positionInRack++) {                    
                     Participant p = new Participant();
                     p.name = participants.get(participantIndex++);
                     p.dc = dc;
                     p.rack = rack;
                     p.positionInRack = positionInRack;                    
                     p.nodeIndexInDataCenter = rack + (positionInRack - 1) * nbRackPerDataCenter;
                     setParticipantToken(nbParticipantPerDataCenter, p);
                     result.add(p);
                 }
             }
         }
         
         return result;           
     }
         
     
     public static void setParticipantToken(int nbParticipantPerDataCenter, Participant p) {
         
         BigInteger token = new BigInteger("2");
         
         token = token.pow(127);
        token = token.multiply(new BigInteger("" + (p.nodeIndexInDataCenter -1)));
        token = token.divide(new BigInteger("" + nbParticipantPerDataCenter));        
        
         // for better readability we round the token
         String tokenApprox = token.toString();
         if (tokenApprox.length() > 1) {
             tokenApprox = tokenApprox.substring(0,3) + tokenApprox.substring(3).replaceAll("[0-9]", "0");
         }
         
         // Do the DC +1 stuff
         tokenApprox = tokenApprox.substring(0,tokenApprox.length() -1) + (p.dc - 1); 
         p.token = tokenApprox;
     }
 }
