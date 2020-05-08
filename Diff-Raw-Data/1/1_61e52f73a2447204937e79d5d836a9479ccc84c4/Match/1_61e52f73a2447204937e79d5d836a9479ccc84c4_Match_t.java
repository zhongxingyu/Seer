 package org.fofo.entity;
 
 import java.util.Date;
 import java.util.UUID;
 import javax.persistence.*;
 
 /**
  *
  * @author mohamed, Anatoli
  *
  */
 @Entity
 @Table(name = "Match_")
 public class Match {
 
     @Id
     @Column(name = "ID_MATCH")
     private String idMatch;
     @OneToOne
     @JoinColumn(name = "HOME_T_NAME", referencedColumnName = "NAME")
     private Team home;
     @OneToOne
     @JoinColumn(name = "VISITOR_T_NAME", referencedColumnName = "NAME")
     private Team visitor;
     @Temporal(TemporalType.DATE)
     @Column(name = "MATCH_DATE")
     private Date matchDate;
     @Column(name = "place")
     private String place;
     @Column(name = "goalsHome")
     private int goalsHome;
     @Column(name = "goalsVisiting")
     private int goalsVisiting;
     @Column(name = "observations")
     private String observations;
     
     @ManyToOne
     @JoinColumn(name = "REFEREE", referencedColumnName = "NIF")
     private Referee referee;
 
     //private Stadium
     /**
      *
      */
     public Match() {
         this.idMatch = UUID.randomUUID().toString(); //L'ha de generar Match, ningu altre
 
     }
 
     /**
      *
      * @param home
      * @param visitor
      */
     public Match(Team home, Team visitor) {
         this.home = home;
         this.visitor = visitor;
         this.idMatch = UUID.randomUUID().toString(); //L'ha de generar Match, ningu altre
     }
 
     /**
      *
      * @return
      */
     public String getIdMatch() {
         return idMatch;
     }
 
     /**
      *
      * @return
      */
     public Team getHome() {
         return home;
     }
 
     /**
      *
      * @param home
      */
     public void setHome(Team home) {
         this.home = home;
     }
 
     /**
      *
      * @return
      */
     public Team getVisitor() {
         return visitor;
     }
 
     /**
      *
      * @param visitor
      */
     public void setVisitor(Team visitor) {
         this.visitor = visitor;
     }
 
     /**
      *
      * @return
      */
     public Date getMatchDate() {
         return this.matchDate;
     }
 
     /**
      *
      * @param matchDate
      */
     public void setMatchDate(Date matchDate) {
         this.matchDate = matchDate;
     }
     /*
      * public void setNif(String nif) { this.nif = nif; }
      *
      * public String getNif() { return nif; }
      */
 
     @Override
     public boolean equals(Object obj) {
 
         if (!(obj instanceof Match)) {
             return false;
         }
 
         Match m = (Match) obj;
 
         return m.idMatch.equals(this.idMatch)
                 && ((m.home == null && this.home == null) || m.home.equals(this.home))
                 && ((m.visitor == null && this.visitor == null) || m.visitor.equals(this.visitor))
                 && ((m.referee == null && this.referee == null) || m.referee.equals(this.referee));
         //getName().equals(home.getName()) && 
         //m.visitor.getName().equals (visitor.getName());
     }
 
 
     @Override
     public String toString() {
 
         return "<Match:" + home.getName() + "-" + visitor.getName() + ">";
     }
 
     /**
      *
      * @return
      */
     public Referee getReferee() {
         return this.referee;
     }
 
     /**
      *
      * @param ref
      */
     public void setReferee(Referee ref) {
         this.referee = ref;
     }
 
     public int getGoalsHome() {
         return goalsHome;
     }
 
     public void setGoalsHome(int goalsHome) {
         this.goalsHome = goalsHome;
     }
 
     public int getGoalsVisiting() {
         return goalsVisiting;
     }
 
     public void setGoalsVisiting(int goalsVisiting) {
         this.goalsVisiting = goalsVisiting;
     }
 
     public String getObservations() {
         return observations;
     }
 
     public void setObservations(String observations) {
         this.observations = observations;
     }
 
     public String getPlace() {
         return place;
     }
 
     public void setPlace(String place) {
         this.place = place;
     }
     
     
     
     
 }
