 package fr.atbdx.lightningtalk.domaine;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 
 public class Session {
 
     private String titre;
     private Participant orateur;
     private String description;
     protected List<Participant> votants = new ArrayList<Participant>();
 
     public Session(String titre, String description, Participant orateur) {
         this.titre = StringUtils.trim(titre);
         this.orateur = orateur;
         this.description = description;
     }
 
     public String getTitre() {
         return titre;
     }
 
     public Participant getOrateur() {
         return orateur;
     }
 
     public String getDescription() {
         return description;
     }
 
     public boolean peutVoter(Participant votant) {
        return votant != null && !votants.contains(votant);
     }
 
     public Iterable<Participant> getVotants() {
         return votants;
     }
 
     public int getNombreDeVotes() {
         return votants.size();
     }
 
     public void ajouterUnVote(Participant participant) {
        if (peutVoter(participant)) {
            votants.add(participant);
        }
 
     }
 
     public void supprimerUnVote(Participant participant) {
         votants.remove(participant);
 
     }
 
 }
