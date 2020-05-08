 package edu.uwm.cs552;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Stroke;
 
 public class Train {
   public Train(RailGame g, Player p) {
     game = g;
     player = p;
   }
 
   /**
    * Tells the status of a train's ability to move to a specific coordinate<br />
    * <br />
    * 1: Allowed to move<br />
    * 0: Allowed, but must wait<br />
    * -1: Not allowed
    * 
    * @param coordinate Coordinate to move to
    * @return 1 if movement is allowed immediately, 0 if movement will be
    *         allowed eventually, or -1 if the movement is infeasible
    */
   public int canMove(HexCoordinate coordinate) {
     if (coordinate == null)
       return -1;
 
     if (location == null) {
       switch (game.getTerrain(coordinate)) {
       case SMALL:
       case MEDIUM:
       case LARGE:
         return 1;
       default:
         return -1;
       }
     }
     else {
       HexDirection direction = null;
       for (HexDirection d : HexDirection.values()) {
         if (location.move(d).equals(coordinate))
           direction = d;
       }
 
       if (direction == null)
         return -1;
 
       Rail rail = game.getRail(new HexEdge(location, direction));
       if (rail == null)
         return -1;
       else if (rail.canDriveOn())
         return 1;
       else
         return 0;
     }
   }
 
   public void move(HexCoordinate coordinate) {
     HexDirection direction = null;
     for (HexDirection d : HexDirection.values()) {
       if (location.move(d).equals(coordinate))
         direction = d;
     }
 
     if (direction == null)
       return; // error
 
     Rail rail = game.getRail(new HexEdge(location, direction));
     if (rail.canDriveOn()) {
       if (rail.getPlayer() != player) {
         rail.getPlayer().setCash(rail.getPlayer().getCash() + .5);
         player.setCash(player.getCash() - .5);
       }
       location = coordinate;
     }
   }
 
   public void draw(Graphics graphics, double scale) {
     if (location == null)
       return;
 
     Graphics2D g = (Graphics2D) graphics;
 
     Color oldColor = g.getColor();
     Stroke oldStroke = g.getStroke();
     g.setColor(player.getColor());
     g.setStroke(new BasicStroke(TRAIN_THICKNESS, BasicStroke.CAP_ROUND,
         BasicStroke.JOIN_ROUND));
 
     Point trainCenter = location.toPoint(scale);
     g.drawLine(trainCenter.x - TRAIN_SIZE, trainCenter.y - TRAIN_SIZE,
         trainCenter.x + TRAIN_SIZE, trainCenter.y + TRAIN_SIZE);
     g.drawLine(trainCenter.x + TRAIN_SIZE, trainCenter.y - TRAIN_SIZE,
         trainCenter.x - TRAIN_SIZE, trainCenter.y + TRAIN_SIZE);
 
     g.setColor(oldColor);
     g.setStroke(oldStroke);
   }
 
   private final int TRAIN_SIZE = 10, TRAIN_THICKNESS = 4;
   private Player player;
   private RailGame game;
   private HexCoordinate location;
 }
