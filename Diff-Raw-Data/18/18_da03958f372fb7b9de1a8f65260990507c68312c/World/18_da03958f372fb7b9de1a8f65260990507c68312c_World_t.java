 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ship.world;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import media.ManagedSpriteSheet;
 import media.Renderable;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 
 import ship.Updatable;
 import ship.View;
 import ship.control.KeyReceiver;
 import ship.control.Keys;
 import ship.netcode.ShipProtocol;
 import ship.netcode.interaction.ActivatePackage;
 import ship.netcode.interaction.CreateTilePackage;
 import ship.netcode.interaction.DeleteTilePackage;
 import ship.world.player.Builder;
 import ship.world.player.Player;
 import ship.world.vehicle.ImmobileVehicle;
 import ship.world.vehicle.Vehicle;
 
 /**
  *
  * @author elegios
  */
 public class World implements Position, Renderable, Updatable, KeyReceiver {
     public static final int SKY_GRADIENT_MINIMUM = 220 * Vehicle.TW;
     public static final int SKY_GRADIENT_LENGTH  = 10;
     public static final int SKY_MAX_R = 62;
     public static final int SKY_MAX_G = 209;
     public static final int SKY_MAX_B = 255;
 
     private View view;
 
     private int x;
     private int y;
 
     private ManagedSpriteSheet tileset;
     private static Color[] skyColors;
 
     private ParallaxBackground paraBack;
 
     private ImmobileVehicle island;
     private Player[] players;
     private Player currPlayer;
     private List<Vehicle> vehicles;
 
     private float actionsPerTick;
     private float gravity;
     private float frictionFraction;
     private float airResist;
     private float fuelRate;
 
     private boolean updatePos;
     private int     timeTilUpdatePos;
     private int     updatePosInterval;
 
     public World(View view) throws SlickException {
         if (skyColors == null) {
             skyColors = new Color[256];
             for (int i = 0; i < 256; i++) {
                 skyColors[i] = new Color(Math.max(SKY_MAX_R - i, 0), Math.max(SKY_MAX_G - i, 0), Math.max(SKY_MAX_B - i, 0));
             }
         }
 
         this.view = view;
 
         paraBack = new ParallaxBackground(this);
 
         x = 0;
         y = 0;
 
         actionsPerTick   = 1.0f/1000;
         gravity          = 9.8f * 50;
         frictionFraction = 0.6f/100;
         airResist        = 0.2f;
         fuelRate         = 1.0f/1000;
 
         tileset = view.loader().loadManagedSpriteSheet("tiles", Vehicle.TW, Vehicle.TH);
 
         island  = new ImmobileVehicle(this, 0,  0, 0);
         vehicles = new ArrayList<>();
         for (int i = 0; i < 10; i++)
             vehicles.add(new Vehicle(this, i, island.getWidth()/2 + 128 +i*400, -32));
         for (Vehicle vehicle : vehicles)
             vehicle.generateStandardVehicle();
 
         players = new Player[view.numPlayers()];
         for (int i = 0; i < view.numPlayers(); i++)
             players[i] = new Player(this, i, island.getWidth()/2 + 32, -100 - i*10);
         currPlayer = players[view.playerId()];
 
         updatePos = false;
         updatePosInterval = 300; //TODO: figure out if the update frequency really should be a variable
         timeTilUpdatePos = updatePosInterval;
     }
 
     @Override
     public void update(GameContainer gc, int diff) {
         for (Player player : players)
             player.updateEarly(gc, diff);
 
         moveX(diff);
         collideX();
         moveY(diff);
         collideY();
 
         timeTilUpdatePos -= diff;
         if (timeTilUpdatePos < 0) {
             updatePos = true;
             timeTilUpdatePos += updatePosInterval;
         } else
             updatePos = false;
 
         for (Player player : players)
             player.update(gc, diff);
         for (Vehicle vehicle : vehicles)
             vehicle.update(gc, diff);
 
         x = Math.round(currPlayer.getX()) + currPlayer.getWidth()/2  - View.window().getWidth()/2;
         y = Math.round(currPlayer.getY()) + currPlayer.getHeight()/2 - View.window().getHeight()/2;
     }
 
     /**
      * Moves all movable objects horizontally.
      *
      * This method should be called exactly once per frame,
      * with one call to collideY following shortly thereafter.
      * @param diff the time since the last frame
      */
     private void moveX(int diff) {
         for (Vehicle vehicle : vehicles)
             vehicle.moveX(diff);
         for (Player player : players)
             player.moveX(diff);
     }
     /**
      * When a vehicle gets an updated position from the server
      * it calls this method to make sure that all players currently
      * standing on the vehicle are moved with it. This is done
      * by letting all players know that the vehicle has moved
      * and then letting them decide whether to do anything about it.
      * @param vehicle the Vehicle that has moved
      * @param move the distance it has moved
      */
     public void relMoveX(Vehicle vehicle, float move) {
         for (Player player : players)
             player.relMoveX(vehicle, move);
     }
     /**
      * Checks for horizontal collision between all objects, moving them
      * should a collision be detected.
      */
     private void collideX() {
         for (Vehicle vehicle : vehicles)
             vehicle.collideWithVehicleX(island);
         for (Vehicle vehicle : vehicles)
             collideVehicleX(vehicle);
 
         for (Player player : players)
             collidePlayerX(player);
     }
     /**
      * Moves all movable objects vertically.
      *
      * This method should be called exactly once per frame,
      * with one call to collideY following shortly thereafter.
      * @param diff the time since the last frame
      */
     private void moveY(int diff) {
         for (Vehicle vehicle : vehicles)
             vehicle.moveY(diff);
         for (Player player : players)
             player.moveY(diff);
     }
     /**
      * When a vehicle gets an updated position from the server
      * it calls this method to make sure that all players currently
      * standing on the vehicle are moved with it. This is done
      * by letting all players know that the vehicle has moved
      * and then letting them decide whether to do anything about it.
      * @param vehicle the Vehicle that has moved
      * @param move the distance it has moved
      */
     public void relMoveY(Vehicle vehicle, float move) {
         for (Player player : players)
             player.relMoveY(vehicle, move);
     }
     /**
      * Checks for vertical collision between all objects, moving them
      * should a collision be detected.
      */
     private void collideY() {
         for (Vehicle vehicle : vehicles)
             vehicle.collideWithVehicleY(island);
         for (Vehicle vehicle : vehicles)
             collideVehicleY(vehicle);
 
         for (Player player : players)
             collidePlayerY(player);
     }
 
     /**
      * First collides <code>vehicle</code> with all vehicle with a higher ID,
      * redoing collision with all vehicles, including those of lower ID, should
      * a collision be detected. Those of lower ID will result in a call to
      * collideVehicleX(other), while those with a higher ID will give a
      * vehicle.collideWithVehicleX(other). This makes collisions a little
      * bit more deterministic, since lower IDs will move out of the way of
      * higher IDs if that is possible.
      * @param vehicle the Vehicle which should be tested
      */
     public void collideVehicleX(Vehicle vehicle) {
         for (int i = (vehicles.indexOf(vehicle) + 1); i < vehicles.size(); i++)
             if (vehicles.get(i).getID() != vehicle.getID()) {
                 if (vehicles.get(i).getID() < vehicle.getID())
                     collideVehicleX(vehicles.get(i));
                 else if (vehicle.collideWithVehicleX(vehicles.get(i)))
                     i = -1;
                 if (i == vehicles.size() - 1 && vehicle.collideWithVehicleX(island))
                     i = -1;
             }
     }
 
     /**
      * First collides <code>vehicle</code> with all vehicle with a higher ID,
      * redoing collision with all vehicles, including those of lower ID, should
      * a collision be detected. Those of lower ID will result in a call to
      * collideVehicleY(other), while those with a higher ID will give a
      * vehicle.collideWithVehicleY(other). This makes collisions a little
      * bit more deterministic, since lower IDs will move out of the way of
      * higher IDs if that is possible.
      *
      * After that is complete collision is tested against all islands, once again
      * restarting from the lowest ID vehicle should a collision be detected.
      * @param vehicle the Vehicle which should be tested
      */
     public void collideVehicleY(Vehicle vehicle) {
         for (int i = (vehicles.indexOf(vehicle) + 1); i < vehicles.size(); i++) {
             if (vehicles.get(i).getID() != vehicle.getID()) {
                 if (vehicles.get(i).getID() < vehicle.getID())
                     collideVehicleY(vehicles.get(i));
                 else if (vehicle.collideWithVehicleY(vehicles.get(i))) {
                     i = -1;
                 } if (i == vehicles.size() - 1 && vehicle.collideWithVehicleY(island)) {
                     i = -1;
                 }
             }
         }
     }
 
     /**
      * Collides the <code>player</code> first with all vehicles, then with all islands,
      * moving the <code>player</code> should it be necessary.
      * @param player the Player to be checked
      */
     public void collidePlayerX(Player player) {
         for (Vehicle vehicle : vehicles)
             if (vehicle.overlaps(player)) {
                 float fixMove = vehicle.collideRectangleX(player, player.getAbsXSpeed() - vehicle.getAbsXSpeed());
                 if (fixMove != 0)
                     player.collisionFixPosX(fixMove, vehicle);
             }
 
         if (island.overlaps(player)) {
             float fixMove = island.collideRectangleX(player, player.getAbsXSpeed());
             if (fixMove != 0)
                 player.collisionFixPosX(fixMove, island);
         }
     }
 
     /**
      * Collides the <code>player</code> first with all vehicles, then with all islands,
      * moving the <code>player</code> should it be necessary.
      * @param player the Player to be checked
      */
     public void collidePlayerY(Player player) {
         for (Vehicle vehicle : vehicles)
             if (vehicle.overlaps(player)) {
                 float fixMove = vehicle.collideRectangleY(player, player.getAbsYSpeed() - vehicle.getAbsYSpeed());
                 if (fixMove != 0)
                     player.collisionFixPosY(fixMove, vehicle);
             }
 
         if (island.overlaps(player)) {
             float fixMove = island.collideRectangleY(player, player.getAbsYSpeed());
             if (fixMove != 0)
                 player.collisionFixPosY(fixMove, island);
         }
     }
 
     /**
      * Changes the player.ID.activate value to trigger an activate command
      * on the tile that <code>player<code> is overlapping in a Vehicle. Will
      * do nothing if there is no overlapping Vehicle or the overlapping
      * tile is empty.
      * @param player the player that is activating
      */
     public void activateUnderPlayer(Player player) {
         for (Vehicle vehicle : vehicles)
             if (vehicle.overlaps(player)) {
                 int vehX = vehicle.getTileXUnderPos(player.getX() + player.getWidth ()/2);
                 int vehY = vehicle.getTileYUnderPos(player.getY() + player.getHeight()/2);
                 if (vehX >= 0 && vehX < vehicle.WIDTH() &&
                     vehY >= 0 && vehY < vehicle.HEIGHT() &&
                     vehicle.tile(vehX, vehY) != null) {
                     if (view.net().isOnline())
                         view.net().send(ShipProtocol.ACTIVATE, new ActivatePackage(player.getID(), vehicle.getID(), vehX, vehY));
                    if (view.net().isServer() || !view.net().isClient())
                         vehicle.tile(vehX, vehY).activate(player);
                 }
             }
 
     }
 
     /**
      * Find and return the Vehicle with the given ID, or null if none can be found.
      * @param vehicleID the ID of the Vehicle to be returned
      * @return the Vehicle with the ID <code>vehicleID</code>
      */
     public Vehicle findVehicle(int vehicleID) {
         for (Vehicle vehicle : vehicles)
             if (vehicle.getID() == vehicleID)
                 return vehicle;
 
         return null;
     }
 
     public Player findPlayer(int playerID) {
         for (Player player : players)
             if (player.getID() == playerID)
                 return player;
 
         return null;
     }
 
     /**
      * Changes the player.ID.makeTile value that will trigger the creation
      * of a tile at the point underneath the player builder. Will do nothing
      * if there is no overlapping vehicle, no block to support the construction,
      * or already a block at the given point.
      * @param player the player doing the building
      */
     public void buildUnderPlayerBuilder(Player player) {
         for (Vehicle vehicle : vehicles) {
             int tx = vehicle.getTileXUnderPos(player.builder().getX() + player.builder().getWidth ()/2);
             int ty = vehicle.getTileYUnderPos(player.builder().getY() + player.builder().getHeight()/2);
             if (tx >= 1 && tx < vehicle.WIDTH() - 1 &&
                 ty >= 1 && ty < vehicle.HEIGHT() - 1)
                 if (!vehicle.existsAt(tx, ty) &&
                         (vehicle.existsAt(tx    , ty - 1) ||
                          vehicle.existsAt(tx + 1, ty    ) ||
                          vehicle.existsAt(tx    , ty + 1) ||
                          vehicle.existsAt(tx - 1, ty    ))) {
                    if (view.net().isOnline())
                         view.net().send(ShipProtocol.CREATE_TILE,
                                         new CreateTilePackage(player.getID(),
                                                               vehicle.getID(),
                                                               tx, ty,
                                                               view.inventory().getSelectedItem(),
                                                               view.inventory().getSelectedSubItem()));
                    if (view.net().isServer() || !view.net().isClient())
                         vehicle.addTile(view.inventory().getSelectedTile().create(view.inventory().getSelectedSubItem(), tx, ty));
                     break;
                 }
         }
 
     }
 
     /**
      * Changes the player.ID.deleTile value that will trigger the
      * destruction of the tile underneath the player builder. Does
      * nothing if there is no vehicle overlapping or no tile in
      * the given position
      * @param player the player doing the destroying
      */
     public void destroyUnderPlayerBuilder(Player player) {
         for (Vehicle vehicle : vehicles) {
             int tx = vehicle.getTileXUnderPos(player.builder().getX() + player.builder().getWidth ()/2);
             int ty = vehicle.getTileYUnderPos(player.builder().getY() + player.builder().getHeight()/2);
             if (tx >= 1 && tx < vehicle.WIDTH() - 1 &&
                 ty >= 1 && ty < vehicle.HEIGHT() - 1)
                 if (vehicle.existsAt(tx, ty)) {
                    if (view.net().isOnline())
                         view.net().send(ShipProtocol.DELETE_TILE, new DeleteTilePackage(player.getID(), vehicle.getID(), tx, ty));
                    if (view.net().isServer() || !view.net().isClient())
                         vehicle.remTile(vehicle.tile(tx, ty));
                     break;
                 }
         }
 
     }
 
     /**
      * Removes a given Vehicle from the internal list, effectively
      * destroying it.
      * @param vehicle the Vehicle to be removed
      */
     public void removeVehicleFromList(Vehicle vehicle) {
         vehicles.remove(vehicle);
     }
 
     @Override
     public void render(GameContainer gc, Graphics g) {
         renderBackgroundGradient();
 
         paraBack.render(gc, g);
 
         tileset.getSpriteSheet().startUse();
         island.render(gc, g);
         for (Vehicle vehicle : vehicles)
             vehicle.render(gc, g);
         tileset.getSpriteSheet().endUse();
 
         for (Player player : players)
             player.render(gc, g);
 
         if (currPlayer.builder().buildMode())
             renderBuilder(currPlayer.builder(), gc, g);
         g.drawString("xSpeed: " +Math.round(currPlayer.getAbsXSpeed()/32)+ " squ/igs\n" +
         		     "ySpeed: " +Math.round(currPlayer.getAbsYSpeed()/32)+ " squ/igs\n\nPlayer " +view.playerId(), 10, 100);
     }
     /**
      * Renders the highlight of the player builder, if in overlaps a position
      * where either building or destruction can be done.
      * @param builder the Builder whose highlight will be rendered.
      * @param gc the GameContainer in which the current game exists
      * @param g the Graphics object that draws everything
      */
     private void renderBuilder(Builder builder, GameContainer gc, Graphics g) {
         for (Vehicle vehicle : vehicles) {
             int tx = vehicle.getTileXUnderPos(builder.getX() + builder.getWidth ()/2);
             int ty = vehicle.getTileYUnderPos(builder.getY() + builder.getHeight()/2);
             if (tx >= 1 && tx < vehicle.WIDTH() - 1 &&
                 ty >= 1 && ty < vehicle.HEIGHT() - 1)
                 if (!vehicle.existsAt(tx, ty) &&
                         (vehicle.existsAt(tx    , ty - 1) ||
                          vehicle.existsAt(tx + 1, ty    ) ||
                          vehicle.existsAt(tx    , ty + 1) ||
                          vehicle.existsAt(tx - 1, ty    ))) {
                     builder.renderHighlight(gc, g, vehicle.ix() + tx*Vehicle.TW, vehicle.iy() + ty*Vehicle.TH, true);
                     break;
                 } else if (vehicle.existsAt(tx, ty)) {
                     builder.renderHighlight(gc, g, vehicle.ix() + tx*Vehicle.TW, vehicle.iy() + ty*Vehicle.TH, false);
                     break;
                 }
         }
 
     }
     /**
      * Renders the Background. Is no longer a gradient as that was to computationally
      * expensive, instead renders one color that is darker the further away from
      * the nearest island the player is.
      */
     private void renderBackgroundGradient() {
         long deltaX = View.window().getWidth ()/2 - island.ix() - island.getWidth ()/2;
         long deltaY = View.window().getHeight()/2 - island.iy() - island.getHeight()/2;
         float dist = (float) Math.sqrt(deltaX*deltaX + deltaY*deltaY) - SKY_GRADIENT_MINIMUM;
 
         View.window().getGraphics().drawString("dist: " + dist, 10, 200);
 
         int color = 0;
         if (dist > 0)
             color = Math.min(Math.round(dist/SKY_GRADIENT_LENGTH), 255);
 
         View.window().getGraphics().setBackground(skyColors[color]);
         View.window().getGraphics().flush();
     }
 
     /**
      * Check whether objects should update their values in DataVerse.
      * This will periodically be true.
      * @return true if values should be updated, false otherwise
      */
     public boolean updatePos() { return updatePos; }
 
     public View view() { return view; }
 
     public float  actionsPerTick()   { return actionsPerTick; }
     public float  gravity()          { return gravity; }
     public float  frictionFraction() { return frictionFraction; }
     public float  airResist()        { return airResist; }
     public Player currPlayer()       { return currPlayer; }
     public float  fuelRate()         { return fuelRate; }
 
     public List<Vehicle> vehicles() { return vehicles; }
 
     public float getX() { return -x; }
     public float getY() { return -y; }
 
     public int ix() { return Math.round(getX()); }
     public int iy() { return Math.round(getY()); }
 
     public boolean keyReleased(Keys keys, int key, char c) {
         return currPlayer.keyReleased(keys, key, c);
     }
 
     public boolean keyPressed(Keys keys, int key, char c) {
         return currPlayer.keyPressed(keys, key, c);
     }
 
 }
