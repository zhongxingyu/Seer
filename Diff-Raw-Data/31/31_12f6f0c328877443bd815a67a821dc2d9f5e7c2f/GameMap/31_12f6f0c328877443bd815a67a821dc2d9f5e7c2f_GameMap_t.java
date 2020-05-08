 package vooga.towerdefense.model;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.List;
 import vooga.towerdefense.gameElements.GameElement;
 import vooga.towerdefense.gameElements.Unit;
 import vooga.towerdefense.util.Location;
 
 
 public class GameMap {
 
     // a normal computer screen will have
     private final int TILE_SIZE = 25;
 
     private List<Unit> myUnits;
     private List<GameElement> myGameElements;
     private Tile[][] myGrid;
     private Location myDestination;
     private Dimension myDimension;
 
    public GameMap (int width, int height, Location destination) {
         myUnits = new ArrayList<Unit>();
         myDimension = new Dimension(width, height);
         initializeGrid();
         myDestination = destination;
     }
 
     private void initializeGrid () {
         myGrid = new Tile[(int) (myDimension.getWidth() / TILE_SIZE)][(int) (myDimension
                 .getHeight() / TILE_SIZE)];
        for (int i = 0; i < myGrid.length; i++) {
            for (int j = 0; j < myGrid[i].length; j++) {
                 int xLocation = (int) (i * TILE_SIZE + TILE_SIZE / 2);
                 int yLocation = (int) (j * TILE_SIZE + TILE_SIZE / 2);
                 // TODO: replace booleans with parsed values from file
                 myGrid[i][j] = new Tile(new Point(xLocation, yLocation),
                                         true, true);
            }        
        }
 //        for (int i = 0; i < myGrid.length; ++i) {
 //            for (int j = 0; j < myGrid[i].length; ++j) {
//                System.out.print(myGrid[i][j] + " ");
 //            }
//            System.out.println();
 //        }
     }
 
     public void update (double elapsedTime) {
         updateUnits(elapsedTime);
         updateTiles(elapsedTime);
     }
 
     private void updateUnits (double elapsedTime) {
         for (Unit unit : myUnits) {
             unit.update(elapsedTime, null);
         }
     }
 
     private void updateTiles (double elapsedTime) {
         for (int i = 0; i < myGrid.length; ++i) {
             for (int j = 0; j < myGrid[i].length; ++j) {
                 myGrid[i][j].update(elapsedTime);
             }
         }
     }
 
     public void spawnUnit (Unit u) {
         myUnits.add(u);
     }
 
     public void buildTower (int i, int j, GameElement t) {
         myGrid[i][j].setTower(t);
     }
 
     public void sellTower (int i, int j) {
         myGrid[i][j].deleteTower();
     }
 
     public Tile getTile (Point point) {
         return myGrid[(int) (point.getX() / TILE_SIZE)][(int) (point.getY() / TILE_SIZE)];
     }
 
     public void paint (Graphics2D pen) {
         for (Unit u : myUnits) {
             u.paint(pen);
         }
         for (GameElement e : myGameElements)
             e.paint(pen);
 
     }
 }
