 
 import java.util.Random;
 
 /**
  * The AI for a single brave soldier
  * @author kviiri
  */
 public class Lion {
     public static final int STATE_TURRET = 0;   //Stationary, waiting for an enemy to reach range
     public static final int STATE_REINFORCE = 1; //Attempts to reach our own flag to defend it
     public static final int STATE_DEFENSIVE_CRUISE = 2; //Cruises near our own flag and seeks to defend it
     public static final int STATE_OFFENSIVE_CRUISE = 3; //Cruises near enemy flag and seeks to support any capture attempts
     public static final int STATE_DODGE = 4;  //Think there's a grenade hurled at you? Then move, you idiot!
     public static final int STATE_RUSH = 5;     //Go for the flag!
     public static final int STATE_OFFENSE = 6;  //Gun down any bad guys and take the flag
     public static final int STATE_FINISH_CAPTURE = 7;   //Once we have the flag, of course
     public static final int STATE_DESTROY = 8;  //Randomish cruising with destruction of enemies
     public static final int STATE_INTERCEPT = 9;    //Take out the flag carrier pronto
     public static final int STATE_RESPAWNING = 10;  //Herra alikersantti, entä VMTL-taistelijat?
     
     private int state;
     Things.Soldier soldier;
     Donkey boss;
     Things t = new Things();
     public Lion(int state, Things.Soldier s, Donkey boss) {
         this.state = state;
         soldier = s;
         this.boss = boss;
     }
     public String getAction() {
         switch(state) {
             case STATE_REINFORCE :
                 return getReinforceAction();
             case STATE_DEFENSIVE_CRUISE :
                 return getDefensiveCruiseAction();
             case STATE_OFFENSIVE_CRUISE :
                 return getOffensiveCruiseAction();
             case STATE_DODGE :
                 return getDodgeAction();
             case STATE_RUSH :
                 return getRushAction();
             case STATE_OFFENSE :
                 return getOffenseAction();
             case STATE_FINISH_CAPTURE :
                 return getFinishCaptureAction();
             case STATE_INTERCEPT :
                 return getInterceptAction();
             case STATE_RESPAWNING :
             default :
                 return null;
         }
         
     }
 
     private String getReinforceAction() {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
     private String getDefensiveCruiseAction() {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
     private String getOffensiveCruiseAction() {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
     private String getDodgeAction() {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
     private String getRushAction() {
         if(!boss.weHaveFlag()) {
            if(Math.random() < 0.8) return boss.getMoveOrder(this.soldier.x, this.soldier.y, boss.enemyFlag.x, boss.enemyFlag.y);
            else return getRandomAction();
         }
         else {
             if(this.soldier.flag.equalsIgnoreCase("No")) {
                 if(Math.random() < 0.6) {
                     return getRandomAction();
                 }
             }
            return boss.getMoveOrder(this.soldier.x, this.soldier.y, boss.flagStandX, boss.flagStandY);
         }
     }
 
     private String getOffenseAction() {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
     private String getFinishCaptureAction() {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
     private String getInterceptAction() {
         throw new UnsupportedOperationException("Not yet implemented");
     }
     private String getRandomAction() {
         Random rnd = new Random();
         int action = rnd.nextInt(5);
         if(action == 0) return "S";
         if(action == 1) return "U";
         if(action == 2) return "R";
         if(action == 3) return "D";
         if(action == 4) return "L";
         return "S";
     }
 }
