 package com.unotournamentboard.model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Predicate;
 import org.neo4j.graphdb.Direction;
 import org.springframework.data.neo4j.annotation.GraphId;
 import org.springframework.data.neo4j.annotation.NodeEntity;
 import org.springframework.data.neo4j.annotation.RelatedTo;
 
 /**
  * @author hgastaud
  * 
  */
 @NodeEntity
 public class Tournament {
 
     @GraphId
     private Long id;
 
     private String name;
 
     private Date startDate;
 
     private Date endDate;
 
     private Integer maxOfPoints = 0;
 
     private Integer maxOfHooks = 0;
 
     @RelatedTo(type = "PLAYED_IN", direction = Direction.INCOMING)
     private Set<Player> players;
 
     @RelatedTo(type = "ROUND_OF", direction = Direction.OUTGOING)
     private Set<Round> rounds;
 
     private transient List<PlayerInTournament> tournamentPlayers;
 
     public Tournament() {
         // Do nothing.
     }
 
     public Tournament(String name, int maxPoints, int maxHooks, Date start, Date end) {
         this.setName(name);
         this.setMaxOfPoints(maxOfPoints);
         this.setMaxOfHooks(maxOfHooks);
         this.setStartDate(start);
         this.setEndDate(end);
         this.setPlayers(new HashSet<Player>());
         this.setRounds(new HashSet<Round>());
         this.setTournamentPlayers(new ArrayList<PlayerInTournament>());
     }
 
     public void startTournament() {
         for (Player eachPlayer : this.getPlayers()) {
             this.getTournamentPlayers().add(new PlayerInTournament(eachPlayer));
         }
     }
 
     public void addANewRound(Round round) {
         this.getRounds().add(round);
         this.addTheFinalPointsForAllPlayers(round);
         int roundNumber = this.getRounds().size();
         this.sortTheTournamentPlayer();
         int lastPositionPoints = this.getTheLastPositionPoints();
         this.removePlayersAndAddHooks(roundNumber, lastPositionPoints);
     }
 
     public boolean isTournamentFinsih() {
         PlayerInTournament secondPlayer = this.getTournamentPlayers().get(1);
         return secondPlayer.getFinishRound() > 0;
     }
 
     /**
      * @param roundNumber
      * @param lastPositionPoints
      */
     protected void removePlayersAndAddHooks(int roundNumber, int lastPositionPoints) {
         for (PlayerInTournament eachPlayer : this.getTournamentPlayers()) {
             if (eachPlayer.getFinishRound() < 0) {
                 if (eachPlayer.getPoints() >= this.getMaxOfPoints()) {
                     if (eachPlayer.getNumberOfHooks() < this.getMaxOfHooks()) {
                         eachPlayer.addHook(lastPositionPoints);
                     } else {
                         eachPlayer.gameOver(roundNumber);
                     }
                 }
             }
         }
     }
 
     /**
      * @return
      */
     protected int getTheLastPositionPoints() {
         int lastPositionPoints = 0;
         for (int i = this.getTournamentPlayers().size() - 1; i >= 0; i--) {
             PlayerInTournament eachPlayer = this.getTournamentPlayers().get(i);
             Integer eachPlayerPoints = eachPlayer.getPoints();
             if (eachPlayer.getFinishRound() < 0 && eachPlayerPoints < this.getMaxOfPoints()) {
                 lastPositionPoints = eachPlayerPoints;
                 break;
             }
         }
         return lastPositionPoints;
     }
 
     /**
      * 
      */
     protected void sortTheTournamentPlayer() {
         Collections.sort(this.getTournamentPlayers(), new Comparator<PlayerInTournament>() {
 
             public int compare(PlayerInTournament o1, PlayerInTournament o2) {
                if (o1.getFinishRound() > 0 && o2.getFinishRound() > 0) {
                    if (o1.getFinishRound() < o2.getFinishRound()) {
                        return 1;
                     } else {
                        if (o1.getFinishRound() > o2.getFinishRound()) {
                            return -1;
                        } else {
                            return o1.getPoints().compareTo(o2.getPoints());
                        }
                    }
                } else {
                    if (o1.getFinishRound() == o2.getFinishRound()) {
                         return o1.getPoints().compareTo(o2.getPoints());
                    } else {
                        return Integer.valueOf(o1.getFinishRound()).compareTo(Integer.valueOf(o2.getFinishRound()));
                     }
                 }
             }
         });
     }
 
     /**
      * @param round
      */
     protected void addTheFinalPointsForAllPlayers(Round round) {
         Iterator<PlayedRelationship> playersPlayed = round.getPlayersPlayed();
         while (playersPlayed.hasNext()) {
             final PlayedRelationship eachPlayerPlayed = playersPlayed.next();
             PlayerInTournament foundPlayer = (PlayerInTournament) CollectionUtils.find(this.getTournamentPlayers(), new Predicate() {
 
                 public boolean evaluate(Object object) {
                     PlayerInTournament eachPlayer = (PlayerInTournament) object;
                     return eachPlayer.getPlayer().equals(eachPlayerPlayed.getPlayer());
                 }
             });
             if (foundPlayer != null) {
                 foundPlayer.addPoints(eachPlayerPlayed.getPoints());
             }
         }
     }
 
     public Tournament(String name, int maxPoints, int maxHooks, Date start) {
         this(name, maxPoints, maxHooks, start, null);
     }
 
     public Tournament(String name, int maxPoints, int maxHooks) {
         this(name, maxPoints, maxHooks, new Date(), null);
     }
 
     public Tournament(String name, int maxPoints) {
         this(name, maxPoints, 0, new Date(), null);
     }
 
     public void addPlayer(Player player) {
         this.getPlayers().add(player);
     }
 
     public void removePlayer(Player player) {
         this.getPlayers().remove(player);
     }
 
     public int getRoundsPLayed() {
         return this.getRounds().size();
     }
 
     /**
      * @return the id
      */
     public Long getId() {
         return id;
     }
 
     /**
      * @param id
      *            the id to set
      */
     public void setId(Long id) {
         this.id = id;
     }
 
     /**
      * @return the name
      */
     public String getName() {
         return name;
     }
 
     /**
      * @param name
      *            the name to set
      */
     public void setName(String name) {
         this.name = name;
     }
 
     /**
      * @return the startDate
      */
     public Date getStartDate() {
         return startDate;
     }
 
     /**
      * @param startDate
      *            the startDate to set
      */
     public void setStartDate(Date startDate) {
         this.startDate = startDate;
     }
 
     /**
      * @return the endDate
      */
     public Date getEndDate() {
         return endDate;
     }
 
     /**
      * @param endDate
      *            the endDate to set
      */
     public void setEndDate(Date endDate) {
         this.endDate = endDate;
     }
 
     /**
      * @return the players
      */
     public Set<Player> getPlayers() {
         return players;
     }
 
     /**
      * @param players
      *            the players to set
      */
     public void setPlayers(Set<Player> players) {
         this.players = players;
     }
 
     /**
      * @return the rounds
      */
     public Set<Round> getRounds() {
         return rounds;
     }
 
     /**
      * @param rounds
      *            the rounds to set
      */
     public void setRounds(Set<Round> rounds) {
         this.rounds = rounds;
     }
 
     /**
      * @return the maxOfPoints
      */
     public Integer getMaxOfPoints() {
         return maxOfPoints;
     }
 
     /**
      * @param maxOfPoints
      *            the maxOfPoints to set
      */
     public void setMaxOfPoints(Integer maxOfPoints) {
         this.maxOfPoints = maxOfPoints;
     }
 
     /**
      * @return the maxOfHooks
      */
     public Integer getMaxOfHooks() {
         return maxOfHooks;
     }
 
     /**
      * @param maxOfHooks
      *            the maxOfHooks to set
      */
     public void setMaxOfHooks(Integer maxOfHooks) {
         this.maxOfHooks = maxOfHooks;
     }
 
     protected List<PlayerInTournament> getTournamentPlayers() {
         return tournamentPlayers;
     }
 
     protected void setTournamentPlayers(List<PlayerInTournament> tournamentPlayers) {
         this.tournamentPlayers = tournamentPlayers;
     }
 
 }
