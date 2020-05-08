 package mq.communicator;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.List;
 
 import mapping.Barcode;
 import mapping.Seesaw;
 import mapping.TreasureObject;
 
 import peno.htttp.DisconnectReason;
 import peno.htttp.PlayerHandler;
 import peno.htttp.Tile;
 import simulator.pilot.AbstractPilot;
 import simulator.viewport.SimulatorPanel;
 
 public class APHandler implements PlayerHandler {
 
     private AbstractPilot pilot;
     private SimulatorPanel panel;
 
     public APHandler(AbstractPilot pilot, SimulatorPanel panel) {
         this.pilot = pilot;
         this.panel = panel;
     }
 
     public AbstractPilot getPilot() {
         return pilot;
     }
 
     @Override
     public void gameStarted() {
         getPilot().startExploring();
         System.out.println("[HTTTP] Game started!");
 
     }
 
     @Override
     public void gameStopped() {
         System.out.println("[HTTTP] Game stopped!");
 
     }
 
     @Override
     public void gamePaused() {
         System.out.println("[HTTTP] Game paused!");
 
     }
 
     @Override
     public void playerJoined(String playerID) {
         System.out.println("[HTTTP] " + playerID + " has joined the game!");
     }
 
     @Override
     public void playerJoining(String playerID) {
 
     }
 
     @Override
     public void playerDisconnected(String playerID, DisconnectReason reason) {
 
     }
 
     @Override
     public void playerReady(String playerID, boolean isReady) {
 
     }
 
     @Override
     public void playerFoundObject(String playerID, int playerNumber) {
         System.out.println("[HTTTP] Player " + playerNumber + " (" + playerID
                 + ") has found his object!");
     }
 
     @Override
     public void gameWon(int teamNumber) {
         System.out.println("[HTTTP] Team " + teamNumber + " has won the game!");
     }
 
     @Override
     public void gameRolled(int playerNumber, int objectNumber) {
         getPilot().setPlayerNumber(playerNumber - 1);
         System.out.println("[HTTTP] Game rolled, your number is "
                 + (playerNumber - 1) + " and your objectnumber is "
                 + objectNumber + ".");
         panel.makeReadyToPlay();
         try {
             pilot.getCenter().setReady(true);
         } catch (Exception e) {
 
         }
     }
 
     @Override
     public void teamConnected(String partnerID) {
         System.out
                 .println("[HTTTP] Partner (" + partnerID + ") has connected.");
         pilot.setUpdatePosition(true);
         if (panel.getDummyPilot() != null)
             panel.getDummyPilot().activate();
         ArrayList<peno.htttp.Tile> vector = new ArrayList<peno.htttp.Tile>();
         for (mapping.Tile tile : pilot.getMapGraphConstructed().getTiles())
             vector.add(new peno.htttp.Tile((long) tile.getPosition().getX(),
                     (long) tile.getPosition().getY(), tile.getToken()));
         try {
             pilot.getCenter().getClient().sendTiles(vector);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public void teamPosition(double x, double y, double angle) {
 
     }
 
     @Override
     public void teamTilesReceived(List<Tile> tiles) {
         Point point1 = null, point2 = null, ourPoint1 = null, ourPoint2 = null;
         String[] info;
         for (peno.htttp.Tile tile : tiles) {
             info = tile.getToken().split("\\.");
             if (info.length == 3 && !info[2].equals("V"))
                 for (mapping.Tile ourTile : pilot.getMapGraphConstructed()
                         .getTiles()) {
                     if (ourTile.getContent() instanceof Barcode
                             && ourTile.getContent().getValue() == Integer
                                     .valueOf(info[2])) {
                         point1 = new Point((int) tile.getX(), (int) tile.getY());
                         ourPoint1 = ourTile.getPosition();
 
                         Point pointN = new Point((int) point1.getX(),
                                 (int) point1.getY() + 1);
                         Point pointZ = new Point((int) point1.getX(),
                                 (int) point1.getY() - 1);
                         Point pointE = new Point((int) point1.getX() + 1,
                                 (int) point1.getY());
                         Point pointW = new Point((int) point1.getX() - 1,
                                 (int) point1.getY());
                         for (peno.htttp.Tile tileNZ : tiles) {
                             String[] infoTileNZ = tileNZ.getToken()
                                     .split("\\.");
                             if (tileNZ.getX() == pointN.x
                                     && tileNZ.getY() == pointN.y
                                     && (infoTileNZ[0].equals("Seesaw") || (infoTileNZ.length == 3 && infoTileNZ[2]
                                             .equals("V"))))
                                 point2 = pointN;
                             else if (tileNZ.getX() == pointZ.x
                                     && tileNZ.getY() == pointZ.y
                                     && (infoTileNZ[0].equals("Seesaw") || (infoTileNZ.length == 3 && infoTileNZ[2]
                                             .equals("V"))))
                                 point2 = pointZ;
                             else if (tileNZ.getX() == pointE.x
                                     && tileNZ.getY() == pointE.y
                                     && (infoTileNZ[0].equals("Seesaw") || (infoTileNZ.length == 3 && infoTileNZ[2]
                                             .equals("V"))))
                                 point2 = pointE;
                             else if (tileNZ.getX() == pointW.x
                                     && tileNZ.getY() == pointW.y
                                     && (infoTileNZ[0].equals("Seesaw") || (infoTileNZ.length == 3 && infoTileNZ[2]
                                             .equals("V"))))
                                 point2 = pointW;
                         }
 
                         for (mapping.Tile ourTileNZ : ourTile
                                 .getReachableNeighboursIgnoringSeesaw()) {
                             if (ourTileNZ.getContent() instanceof Seesaw
                                     || ourTileNZ.getContent() instanceof TreasureObject)
                                 ourPoint2 = ourTileNZ.getPosition();
                         }
                     }
                 }
         }
         if (point1 != null && point2 != null && ourPoint1 != null
                 && ourPoint2 != null) {
             System.out.println("[HTTTP] Similar tiles found! " + point1 + " "
                     + ourPoint1 + " -- " + point2 + " " + ourPoint2);
            if(panel.getDummyPilot() != null)
            	panel.getDummyPilot().setMap(pilot.getMapGraphConstructed().generateMapFromTiles(tiles));
         } else
             System.out.println("[HTTTP] No similar tiles found yet!");
     }
 }
