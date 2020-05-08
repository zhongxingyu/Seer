 package characters;
 
 import fap_java.Cell;
 import fap_java.Game;
 import fap_java.Player;
 import fap_java.Team;
 
 public class Archer extends Player{
     public Archer(int id, Cell c, Game game, Team t) {
         super(id, c, game, 5,t);
     }
 
     public void getSkill() {
         if (this.getGame().getThread().getCount() - this.getLastSkill() >= this.getSkillTime()) {
             this.setLastSkill(this.getGame().getThread().getCount());
            System.out.println("*BANG*");
             //Send Skill :
            //test archer branching
         }
     }
     }
