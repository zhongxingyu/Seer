 package vooga.towerdefense.model;
 
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.util.List;
 import vooga.towerdefense.gameElements.Wave;
 import vooga.towerdefense.view.TDView;
 
 
 public class GameModel {
     private TDView myView;
     private List<Wave> myWaves;
     private GameMap myGameMap;
     private double myWaveTimeElapsed;
     private Wave myCurrentWave;
     
     public GameModel(TDView view, List<Wave> waves, GameMap gameMap) {
         myView = view;
         myWaves = waves;
         myGameMap = gameMap;
         myWaveTimeElapsed = 0;
 //        startNextWave();
     }
     
     public void displayTileCoordinates(Point p) {
         Tile t = myGameMap.getTile(p);
         Point center = t.getCenter();
         myView.getTowerInfoScreen().displayInformation("(" + center.getX() + ", " + center.getY() + ")");
//        myView.getTowerInfoScreen().displayInformation(p.toString());
     }
    
     public void update (double elapsedTime) {
         updateWave(elapsedTime);
         myGameMap.update(elapsedTime);
     }
 
     public void updateWave (double elapsedTime) {
         myWaveTimeElapsed += elapsedTime;
         if (myWaveTimeElapsed > myCurrentWave.getDuration()) {
             myWaveTimeElapsed = 0;
             startNextWave();
         }
     }
 
     private void startNextWave () {
         if (myWaves.iterator().hasNext())
             myCurrentWave = myWaves.iterator().next();
         else
         // TODO: add win behavior
         System.out.println("you win!");
     }
 
     public void display (Graphics2D pen) {
         myGameMap.paint(pen);
     }
 }
