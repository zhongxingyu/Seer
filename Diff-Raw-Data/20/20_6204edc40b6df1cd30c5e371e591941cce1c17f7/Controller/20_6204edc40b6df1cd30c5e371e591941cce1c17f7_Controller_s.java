 package vooga.towerdefense.controller;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import vooga.towerdefense.controller.modes.ControlMode;
 import vooga.towerdefense.controller.modes.SelectMode;
 import vooga.towerdefense.gameElements.GameElement;
 import vooga.towerdefense.model.GameMap;
 import vooga.towerdefense.model.GameModel;
 import vooga.towerdefense.model.Tile;
 import vooga.towerdefense.view.TDView;
 
 
 /**
  * As part of a MVC framework, the Controller controls how the view interacts
  * with the model.
  * 
  * @author Jimmy Longley
  * @author Erick Gonzalez
  */
 public class Controller {
     private GameModel myModel;
     private TDView myView;
     private ControlMode myControlMode;
 
     //TODO: controller constructor should take waves & map in order to initialize GameModel?
     public Controller () {
         myModel = new GameModel(this, null, new GameMap(null, 800, 600, null));
         myView = new TDView(this);
         myControlMode = new SelectMode();
     }
 
     /**
      * handles a click to the map appropriately depending
      * on the mode.
      * 
      * @param p is the location of the click
      */
     public void handleMapClick (Point p) {
         myControlMode.handleMapClick(p, this);
     }
 
     public void displayTileCoordinates (Point p) {
         Tile t = myModel.getTile(p);
         Point center = t.getCenter();
         System.out.println(center);
         myView.getTowerInfoScreen().displayInformation(center.toString());
     }
     
    private void fixItemOnMap (GameElement item, Point p) {
         Tile myTile = myModel.getTile(p);
         myTile.setTower(item);
         myModel.getMap().addToMap(item);
         displayMap();
     }
     
     public void displayMap() {
         myView.getMapScreen().update();
     }
     
     public void update(double elapsedTime) {
         myModel.update(elapsedTime);
     }
     
     public void paintMap(Graphics pen) {
         myModel.paintMap((Graphics2D) pen);
     }
 
 }
