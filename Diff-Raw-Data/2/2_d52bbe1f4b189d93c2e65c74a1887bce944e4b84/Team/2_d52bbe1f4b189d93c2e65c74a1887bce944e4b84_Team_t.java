 package models;
 
 import java.util.Set;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 import play.db.jpa.Model;
 
 /**
  *
  * @author screencast
  */
 @Entity
 public class Team extends Model {
 
     @ManyToMany(cascade = CascadeType.ALL)
     public Set<Player> players;
     @OneToMany(mappedBy = "team1")
     public Set<Match> matches1;
     @OneToMany(mappedBy = "team2")
     public Set<Match> matches2;
 
     public void addPlayer(Player player) {
         if (players.size() < 2) {
             players.add(player);
         }
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         int i = 0;
         for (Player p : players) {
             sb.append(p.name);
             i++;
             if (i < players.size()) {
                sb.append(" <br> ");
             }
         }
         return sb.toString();
     }
 }
