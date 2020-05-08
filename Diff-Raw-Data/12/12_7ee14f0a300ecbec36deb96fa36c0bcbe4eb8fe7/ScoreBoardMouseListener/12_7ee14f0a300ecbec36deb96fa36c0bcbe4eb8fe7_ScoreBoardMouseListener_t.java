 package ui.scoreboard;
 import model.*;
 
 
 import javax.swing.event.MouseInputAdapter;
 import java.awt.event.MouseEvent;
 import java.util.List;
 
 class ScoreBoardMouseListener extends MouseInputAdapter {
 
     private List<HotZone> hotZones;
     private ScoreBoard scoreBoard;
     private Game game;
     private Thread controlsHider;
     public ScoreBoardMouseListener(ScoreBoard sb, Game g, List<HotZone> hz) {
         this.hotZones = hz;
         this.scoreBoard = sb;
         this.game = g;
         this.controlsHider = new Thread() {
             public void run() {
                 while (true) {
                     try {
                         Thread.sleep(5000);
                         scoreBoard.setShowMouseControls(false);
                     }
                     catch (Exception ex) { 
                     }
                 }
             }
         };
  
     }
 
     public void mouseClicked(MouseEvent me) {
         try {
         resetHideCounter();
         synchronized(hotZones) {
             for (HotZone hz : hotZones) {
                 if (hz.zone.contains(me.getPoint())) {
                     if (hz.operation == HotZone.ops.INC) {
                        hz.player.setVP(hz.player.getSettlementVP()+1);
                     }
                     else if (hz.operation == HotZone.ops.DEC) {
                        hz.player.setVP(hz.player.getSettlementVP()-1);
                     }
                     else if (hz.operation == HotZone.ops.LA) {
                         if (hz.player != null) {
                             hz.player.add(Achievement.LargestArmy);
                         }
                         else {
                             game.getAchievement(Achievement.LargestArmy).remove(Achievement.LargestArmy);
                         }
                     }
                     else if (hz.operation == HotZone.ops.LR) {
                         if (hz.player != null) {
                             hz.player.add(Achievement.LongestRoad);
                         }
                         else {
                             game.getAchievement(Achievement.LongestRoad).remove(Achievement.LongestRoad);
                         }
                     }
                     return;
                 }
             }
         }
         }
         catch (Exception ex) {
             // Very Iffy....
         }
     }
 
     private void resetHideCounter() {
         synchronized(controlsHider) {
             scoreBoard.setShowMouseControls(true);
             if (controlsHider.getState() != Thread.State.NEW) {
                 try {
                     controlsHider.interrupt();
                 }
                 catch (IllegalThreadStateException ex) {
                     // so the thread wasn't running, fair enough.
                 }
             }
             else {
                 controlsHider.start();
             }
         } 
     }
 
     public void mouseMoved(MouseEvent me) {
         resetHideCounter();
     }
 }
