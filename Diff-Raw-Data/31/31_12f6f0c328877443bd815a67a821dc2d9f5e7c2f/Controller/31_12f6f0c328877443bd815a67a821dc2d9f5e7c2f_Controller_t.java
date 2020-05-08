 package vooga.towerdefense.controller;
 
 import java.awt.Point;
 import vooga.towerdefense.model.GameMap;
 import vooga.towerdefense.model.GameModel;
 import vooga.towerdefense.view.TDView;
 
 public class Controller {
     private boolean onBuildMode;
     private GameModel myModel;
     private TDView myView;
     
     public Controller() {
         myView = new TDView(this);
         myModel = new GameModel(myView, null, new GameMap(800, 600, null));
         onBuildMode = false;
     }
     
     /**
      * handles a click to the map appropriately depending
      *          on the mode.
      * @param p is the location of the click
      */
     public void handleMapClick(Point p) {
         if (onBuildMode) {
             
         } 
         else {
             myModel.displayTileCoordinates(p);
         }
     }
     
     /**
      * paints the map on the view.
      */
     public void paintMap() {
         //TODO: paint the map to the view
     }
     
     public static void main(String[] args) {
         Controller c = new Controller();
         
     }
 }
