 package de.bedit.gaming.wormstats.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
import javax.persistence.*;
 import org.eclipse.persistence.annotations.PrivateOwned;
 
 @Entity
 @Table(name = "matchGame")
 @NamedQueries( {
         @NamedQuery(name = "getAllMatchGames", query = "SELECT m FROM MatchGame m"),
         @NamedQuery(name = "getMatchGameById", query = "SELECT m FROM MatchGame m WHERE m.id = :id"),
         @NamedQuery(name = "getMatchGameByTimestamp", query = "SELECT m FROM MatchGame m WHERE m.matchDate = :date"),
         @NamedQuery(name = "getAllMatchGamesByWinnerId", query = "SELECT m FROM MatchGame m WHERE m.winner.id = :id") })
 public class MatchGame implements Serializable {
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private long id;
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "matchDate", nullable = false)
     private Date matchDate;
     @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
     @PrivateOwned
     private List<CompetitorMatchStatistic> competitorMatchStatistics = new ArrayList<CompetitorMatchStatistic>();
     @OneToOne
     private Competitor winner;
 
     public Date getMatchDate() {
         return matchDate;
     }
 
     public void setMatchDate(Date matchDate) {
         this.matchDate = matchDate;
     }
 
     public Competitor getWinner() {
         return winner;
     }
 
     public void setWinner(Competitor winner) {
         this.winner = winner;
     }
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     public Date getDate() {
         return matchDate;
     }
 
     public void setDate(Date date) {
         this.matchDate = date;
     }
 
     public List<CompetitorMatchStatistic> getCompetitorMatchStatistics() {
         return competitorMatchStatistics;
     }
 
     public void setCompetitorMatchStatistics(
             List<CompetitorMatchStatistic> competitorMatchStatistics) {
         this.competitorMatchStatistics = competitorMatchStatistics;
     }
 }
