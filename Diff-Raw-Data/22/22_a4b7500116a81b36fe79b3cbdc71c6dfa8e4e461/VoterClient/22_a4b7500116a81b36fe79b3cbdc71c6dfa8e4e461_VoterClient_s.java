 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
package src.org.evoting.voter;
 
 import java.io.IOException;
 import java.math.BigInteger;
 import java.net.Socket;
 import java.security.InvalidKeyException;
 import java.security.KeyFactory;
 import java.security.NoSuchAlgorithmException;
 import java.security.NoSuchProviderException;
 import java.security.PublicKey;
 import java.security.SecureRandom;
 import java.security.spec.InvalidKeySpecException;
 import org.cssi.paillier.cipher.Paillier;
 import org.cssi.paillier.cipher.PaillierException;
 import org.cssi.paillier.cipher.PaillierSimple;
 import org.cssi.paillier.spec.PaillierPublicKeyBetaSpec;
 import org.evoting.exception.VotingSchemeException;
import src.org.evoting.schemes.Ballot;
import src.org.evoting.schemes.KOutOfLVoting;
import src.org.evoting.schemes.OneOutOfLVoting;
import src.org.evoting.schemes.Voting;
import src.org.evoting.schemes.YesNoVoting;
 import org.utils.DataStreamUtils;
 
 /**
  * Class that defines a voter and its operations <p> TODO: close
  * {@link DataStreamUtils} TODO: support more than just {@link Paillier}
  *
  */
 public class VoterClient {
 
   private Socket socket;
   private DataStreamUtils dsu;
   private PublicKey publicKey;
   private Paillier paillier;
   private Voting voting;
 
   public VoterClient(Socket soc) throws IOException {
     this.socket = soc;
     this.dsu = new DataStreamUtils(socket.getInputStream(), socket.
             getOutputStream());
     this.paillier = new PaillierSimple();
   }
 
   /**
    * Return the voting scheme instance
    *
    * @return
    */
   public Voting getVoting() {
     return voting;
   }
 
   public PublicKey getPublicKey() {
     return publicKey;
   }
 
   /**
    * Set up a voting <p> In this method, the voter receive every necessary
    * information from the authority<br> It receives the voting scheme, the
    * number of voters and candidates and the {@link PublicKey}
    *
    * @throws NoSuchAlgorithmException
    * @throws NoSuchProviderException
    * @throws InvalidKeySpecException
    */
   public void setUpVoting() throws NoSuchAlgorithmException,
           NoSuchProviderException, InvalidKeySpecException, VotingSchemeException {
     // receive voting properties from server
     try {
 
       // first, receive the kind of voting that is taking place
       int votingType = dsu.readInt();
 
       switch(votingType) {
         case YesNoVoting.CODE:
           voting = new YesNoVoting();
           break;
         case OneOutOfLVoting.CODE:
           voting = new OneOutOfLVoting();
           break;
         case KOutOfLVoting.CODE:
           voting = new KOutOfLVoting();
           break;
         default:
           throw new VotingSchemeException("No such voting scheme");
       }
       
       //------- receive voting properties
       voting.readVotingProperties(dsu);
       // TODO: do this in readVotingProperties(); auth send the cipher being used
       voting.setCipher(new PaillierSimple());
       
       //------- receive voting candidates
       voting.readVotingCandidates(dsu);
 
       // receive public key
       byte[] pubKeyEnc = dsu.readBytes();
       //-------------------------
       KeyFactory keyFactory = KeyFactory.getInstance("Paillier", "CSSI");
       PaillierPublicKeyBetaSpec paiSpec = new PaillierPublicKeyBetaSpec(
               pubKeyEnc);
       this.publicKey = keyFactory.generatePublic(paiSpec);
       //-------------------------
     }
     catch (IOException ex) {
       System.err.println(ex.getMessage());
     }
   }
 
   /**
    * Submits the client vote
    *
    * @param voteOption From 0 to nrCandidates. 0 is blank vote
    */
   @Deprecated
   public void vote(Integer voteOption) throws IOException, PaillierException,
           InvalidKeyException, VotingSchemeException {
     // TODO: move this method to Voting, as abstract? Maybe not..
     BigInteger vote = null;
     if (voting == null) {
       throw new VotingSchemeException("No voting scheme defined");
     }
 
     if (voting instanceof YesNoVoting) {
       // yes/no voting
       vote = new BigInteger(Integer.toString(voteOption));
     }
     else {
       if (voting instanceof OneOutOfLVoting) {
         // 1-out-of-L voting
         int base = ((OneOutOfLVoting) getVoting()).getBase();
         if(voteOption == 0) // blank vote
           vote = BigInteger.ZERO;
         else { // vote in C1 is = base^(voteOption-1), voteOption=1
           voteOption--;
           vote = new BigInteger(Integer.toString(base)).pow(voteOption);
         }
       }
       else {
         if(voting instanceof KOutOfLVoting) {
           // K-out-of-L voting
           int base = ((KOutOfLVoting)getVoting()).getBase();
           // TODO: fazer isto na classe voting
         }
       }
     }
     // encrypt the vote
     BigInteger c = this.paillier.enc(publicKey, vote, new SecureRandom());
     System.out.println("Vote: " + vote);
     System.out.println("Vote enc: " + c);
     // send vote
     dsu.writeBigInteger(c);
 
   }
 
   public void submitBallot(Ballot ballot) throws IOException {
     if(ballot == null) {
       //TODO: throw exception
     }
     // send number of votes in the ballot
     dsu.writeInt(ballot.getVotes().size());
     // send the votes
     for(BigInteger v : ballot.getVotes()) {
       dsu.writeBigInteger(v);
     }
   }
 }
