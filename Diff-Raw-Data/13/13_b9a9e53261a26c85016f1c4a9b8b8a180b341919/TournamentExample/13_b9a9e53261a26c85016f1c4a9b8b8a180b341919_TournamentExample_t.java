 package game;
 
 import java.util.HashSet;
 import java.util.Set;
 
 public class TournamentExample {
 
     /**
      * Entrant represents a competitor in some game.
      */
     public static class Entrant {
         protected int skill;
         protected String name;
         
         public Entrant(int skill, String name) {
             this.skill = skill;
             this.name = name;
         }
         
         /**
          * @return the skill of the entrant; higher is better.
          */
         public int skill() {
             return skill;
         }
         
         /**
          * @return the name of the entrant.
          */
         public String name() {
             return name;
         }
         
         @Override
         public boolean equals(Object other) {
            if(!(other instanceof Entrant)) {
                 return false;
             }
             Entrant otherEntrant = (Entrant) other;
             return otherEntrant.skill == this.skill && 
             		otherEntrant.name.equals(this.name);
         }
         
         @Override
         public int hashCode() {
         	return this.name.hashCode() + this.skill;
         }
     }
     
     /**
      * Team represents a competitor in some game consisting of many players.
      */
     public static class Team extends Entrant {       
         private Set<String> playerNames;
         
         public Team(int skill, String name, Set<String> playerNames) {
             super(skill, name);
             this.playerNames = playerNames;
         }
         
         /**
          * @return the names of the players on the team.
          */
         public Set<String> getPlayerNames() {
             return playerNames;
         }
         
         @Override
         public boolean equals(Object other) {
            if(!(other instanceof Team)) {
                 return false;
             }
             Team otherTeam = (Team) other;
             return otherTeam.skill == this.skill && otherTeam.name.equals(this.name) &&
                     otherTeam.playerNames.containsAll(this.playerNames) &&
                     this.playerNames.containsAll(otherTeam.playerNames);
         }
         
         @Override
         public int hashCode() {
             return super.hashCode() + this.playerNames.hashCode();
         }
     }
     
     /**
      * Tournament represents a single elimination tournament.
      */
     public interface Tournament {
         /**
          * @return the winner of the tournament -- an entrant whose skill is greater than or equal
          * to all the other entrants in the tournament.
          */
         public Entrant winner();
         
         /**
          * @return the set of entrants that participated in the tournament.
          */
         public Set<Entrant> entrants();
     }
     
     /**
      * Bye represents a leaf node in the tournament tree -- a game with only one
      * entrant.
      */
     public static class Bye implements Tournament {
         
         private final Entrant entrant;
         
         public Bye(Entrant entrant) {
             this.entrant = entrant;
         }
         
         @Override
         public Entrant winner() {
             return this.entrant;
         }
 
         @Override
         public Set<Entrant> entrants() {
             Set<Entrant> entrants = new HashSet<Entrant>();
             entrants.add(entrant);
             return entrants;
         }
         
         @Override
         public boolean equals(Object other) {
             if (!(other.getClass().equals(getClass()))) {
                 return false;
             }
             Bye otherBye = (Bye) other;
             return entrant.equals(otherBye.entrant);
         }
         
         @Override
         public int hashCode() {
             return entrant.hashCode();
         }
     }
     
     /**
      * Game represents a single game in a tournament, along with the games
      * contained in its subtree in the tournament.
      */
     public static class Game implements Tournament {
         
         private final Tournament t1, t2;
         
         public Game(Tournament t1, Tournament t2) {
             this.t1 = t1;
             this.t2 = t2;
         }
         
         @Override
         public Entrant winner() {
             Entrant w1 = t1.winner();
             Entrant w2 = t2.winner();
             if (w1.skill() >= w2.skill()) {
                 return w1;
             } else {
                 return w2;
             }
         }
 
         @Override
         public Set<Entrant> entrants() {
             Set<Entrant> entrants = new HashSet<Entrant>();
             entrants.addAll(t1.entrants());
             entrants.addAll(t2.entrants());
             return entrants;
         }
         
         @Override
         public boolean equals(Object other) {
             if (!(other.getClass().equals(getClass()))) {
                 return false;
             }
             Game otherGame = (Game) other;
             return t1.equals(otherGame.t1) && t2.equals(otherGame.t2);
         }
         
         @Override
         public int hashCode() {
             return t1.hashCode() + t2.hashCode();
         }
     }
 }
