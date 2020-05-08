 package data;
 
 import java.util.List;
 
 public class PartyVotesDAOImpl implements PartyVotesDAO {
     @SuppressWarnings("unchecked")
     public List<PartyVotes> list() {
         return PartyVotes.em()
                 .createNamedQuery("getPartyResults")
                 .getResultList();
     }
 
     @SuppressWarnings("unchecked")
    public int getTotalVotes() {
         return ((Number) (
                 PartyVotes.em().createNamedQuery("getTotalVotes").getSingleResult())
                 ).intValue();
     }
 }
