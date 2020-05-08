 package characters;
 
 import fap_java.Cell;
 import fap_java.Game;
 import fap_java.Params;
 import fap_java.Player;
 import fap_java.Team;
 
 public class Warlock extends Player{
     public Warlock(int id, Cell c, Game game,Team t) {
         super(id, c, game, 4, t);
     }
 
     public void getSkill() {
         if (this.getGame().getThread().getCount() - this.getLastSkill() >= this.getSkillTime()) {
             this.setLastSkill(this.getGame().getThread().getCount());
             System.out.println("*BANG*");
             //Send Skill :
             Cell current = this.getCurrent();
            current.setTrap(this.getTeam());
             //Add animation
         }
     }
     }
