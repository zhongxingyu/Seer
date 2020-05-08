 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.evoting.authority;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.security.KeyPair;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.util.logging.Level;
 import java.util.logging.Logger;
import src.org.evoting.schemes.Ballot;
import src.org.evoting.schemes.Voting;
 import org.utils.DataStreamUtils;
 
 /**
  *
  * @author nc
  */
 public class TServer extends Thread {
 
   private static final Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
   private Socket client;
   private PublicKey pubKey;
   private PrivateKey privKey;
   private Voting voting;
 
   /**
    * Create a thread for each connected voter
    *
    * @param s
    * @param kP
    * @param vot
    */
   public TServer(Socket s, KeyPair kP, Voting vot) {
     this.client = s;
     this.pubKey = kP.getPublic();
     this.privKey = kP.getPrivate();
     this.voting = vot;
   }
 
   @Override
   public void run() {
     try {
       DataStreamUtils dsu = new DataStreamUtils(client.getInputStream(), client.
               getOutputStream());
 
       try {
         // first of all, send the voting type, so the voter can create its instance
         int votingType = voting.getCode();
         System.out.println("Voting type: " + votingType);
         
         dsu.writeInt(votingType);
         // send voting properties, like base
         voting.sendVotingProperties(dsu);
         // send voting candidates to the voter
         voting.sendVotingCandidates(dsu);
         // send publickey
         dsu.writeBytes(pubKey.getEncoded());
         System.out.println("Sent public key" + pubKey.getAlgorithm());
         // receive ballot
         //BigInteger vote = dsu.readBigInteger();
         int votes = dsu.readInt();
         Ballot ballot = new Ballot();
         for(int i = 0; i < votes; i++) {
           ballot.addVote(dsu.readBigInteger());
         }
         boolean receivedVote = voting.receiveBallot(ballot);
         System.out.println("Vote accepted: " + receivedVote);
 
       }
       catch (IOException e) {
         log.log(Level.SEVERE, e.getMessage(), e);
       }
       finally {
         dsu.close();
       }
     }
     catch (IOException e) {
       log.log(Level.SEVERE, e.getMessage(), e);
     }
   }
 }
