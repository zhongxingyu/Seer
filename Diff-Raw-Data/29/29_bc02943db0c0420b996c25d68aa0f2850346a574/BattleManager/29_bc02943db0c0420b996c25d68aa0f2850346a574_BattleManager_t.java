 package robocode.rescue;
 
 import java.awt.geom.Rectangle2D;
import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import robocode.control.BattleSpecification;
 import robocode.control.BattlefieldSpecification;
 import robocode.control.RobocodeEngine;
 import robocode.control.RobotSpecification;
 import robocode.control.events.BattleAdaptor;
 import robocode.control.events.BattleCompletedEvent;
 import robocode.control.events.BattleFinishedEvent;
 import robocode.control.events.TurnEndedEvent;
 import robocode.control.snapshot.IRobotSnapshot;
 
 public class BattleManager {
 
     RobocodeEngine engine;
     private int numRounds;
     private int winner;
    private final AtomicBoolean battleCompleted = new AtomicBoolean(false);
     private String teamAName;
     private String teamBName;
     
     private final String teamA = "sibots.TimeATeam*";
     private final String teamB = "sibots.TimeBTeam*";
     private final int battlefieldWidth = 2427; //2427 proporcao aurea =P
     private final int battlefieldHeight = 1500;
     
     private final String initialPos;
     private final Rectangle2D.Double aRescueArea = new Rectangle2D.Double(0, 0, 200, battlefieldHeight); 
     private final Rectangle2D.Double bRescueArea = new Rectangle2D.Double(battlefieldWidth - 200, 0, 200, battlefieldHeight);
 
     public BattleManager(int numRounds) {
       this.numRounds = numRounds;
       winner = -1;
       
       RoboPos rAR = new RoboPos(battlefieldWidth, 750, 270);
       RoboPos rA1 = new RoboPos(200, 600, 90);
       RoboPos rA2 = new RoboPos(200, 700, 90);
       RoboPos rA3 = new RoboPos(200, 800, 90);
       RoboPos rA4 = new RoboPos(200, 900, 90);
       RoboPos rBR = new RoboPos(0, 750, 90);
       RoboPos rB1 = new RoboPos(battlefieldWidth - 200, 600, 270);
       RoboPos rB2 = new RoboPos(battlefieldWidth - 200, 700, 270);
       RoboPos rB3 = new RoboPos(battlefieldWidth - 200, 800, 270);
       RoboPos rB4 = new RoboPos(battlefieldWidth - 200, 900, 270);
 
       initialPos = rAR + "," + rA1 + "," + rA2 + "," + rA3 + "," + rA4 + ","
               + rBR + "," + rB1 + "," + rB2 + "," + rB3 + "," + rB4;
     }
 
     public int start(String teamAName, String teamBName, long timeout) {
         engine = new RobocodeEngine();
         BattleObserver listener = new BattleObserver();
         engine.addBattleListener(listener);
         
         //especificação da batalha
         BattlefieldSpecification bfSpec = new BattlefieldSpecification(
               battlefieldWidth, battlefieldHeight, new Rectangle2D.Double[]{aRescueArea, bRescueArea});
         RobotSpecification[] robots = engine.getLocalRepository(teamA + ", " + teamB);
         BattleSpecification battle = new BattleSpecification(numRounds, Long.MAX_VALUE, 0, false, bfSpec, robots);
         
         this.teamAName = teamAName;
         this.teamBName = teamBName;
         String teamNames = teamAName + "," + teamBName;
 
         
         System.out.println("[Robocode] " + teamAName + " vs " + teamBName);
         System.out.println("[Robocode] Batalha iniciada!");
         engine.setVisible(true);
         engine.runBattle(battle, initialPos, teamNames, false);
 
         if(timeout > 0) {
            long initialTime = System.currentTimeMillis();
            long remainingTime = timeout;
            synchronized(battleCompleted) {
              while(!battleCompleted.get() || remainingTime <= 0) {
                try {
                  battleCompleted.wait(remainingTime);
                  remainingTime = (initialTime + timeout) - System.currentTimeMillis();
                } catch (InterruptedException ex) {
                  Logger.getLogger(BattleManager.class.getName()).log(Level.SEVERE, null, ex);
                }
              }
             }
            if (!battleCompleted.get()) {
                 // timeout
                 engine.abortCurrentBattle();
             }
         } else {
             engine.waitTillBattleOver();
         }
 
         System.out.println("[Robocode] Batalha terminada!");
         
         try {            
           Thread.sleep(10000);
         } catch (InterruptedException ex) {
           Logger.getLogger(BattleManager.class.getName()).log(Level.SEVERE, null, ex);
         }        
         engine.close();
                 
         return winner;
     }
     
     public double getBattlefieldWidth() {
       return battlefieldWidth;
     }
     public double getBattlefieldHeight() {
       return battlefieldHeight;
     }
     public Rectangle2D.Double getRescueArea(char team) {
       if (team == 'a') {
         return aRescueArea;
       } else if (team == 'b') {
         return bRescueArea;
       } else {
         return null;
       }
     }
     
 
     private class BattleObserver extends BattleAdaptor {
       
         IRobotSnapshot[] robots;
 
         @Override
         public void onTurnEnded(TurnEndedEvent event) {
             robots = event.getTurnSnapshot().getRobots();
         }
         
         @Override
         public void onBattleFinished(BattleFinishedEvent event) {
           if (event.isAborted()) {
             
             System.out.println("\n[Robocode] TIMEOUT!");
             System.out.println("\n[Robocode] EMPATE!");
             System.out.println("[Robocode] Distancia do " + teamAName + " = "
                     + (robots[0].getX() - 200));
             System.out.println("[Robocode] Distancia do " + teamBName + " = "
                     + (battlefieldWidth - 200 - robots[5].getX()));
           }
           winner = -1;
         }
         
         @Override
         public void onBattleCompleted(BattleCompletedEvent event) {
           String winnerName;
           if (event.getWinnerTeam() == 0) {
             winnerName = teamAName;
           } else {
             winnerName = teamBName;
           }
           System.out.println("\n[Robocode] VENCEDOR: " + winnerName);
          synchronized(battleCompleted) {
            battleCompleted.set(true);
            battleCompleted.notify();
          }
           winner = event.getWinnerTeam();
         }
     }
 
     private class RoboPos {
 
         int x, y, ang;
 
         public RoboPos(int x, int y, int ang) {
             this.x = x;
             this.y = y;
             this.ang = ang;
         }
 
         public String toString() {
             return "(" + x + "," + y + "," + ang + ")";
         }
     }
 }
