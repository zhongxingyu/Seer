 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ship.world.player;
 
 import media.ManagedImage;
 import media.MediaLoader;
 import media.Renderable;
 
 import org.newdawn.slick.Font;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 
 import ship.Updatable;
 import ship.control.KeyReceiver;
 import ship.control.Keys;
 import ship.netcode.ShipProtocol;
 import ship.netcode.interaction.PlayerMovementPackage;
 import ship.netcode.movement.PlayerPositionPackage;
 import ship.netcode.movement.RelativePlayerPositionPackage;
 import ship.world.Position;
 import ship.world.Rectangle;
 import ship.world.RelativeMovable;
 import ship.world.World;
 import ship.world.vehicle.ImmobileVehicle;
 import ship.world.vehicle.Vehicle;
 
 /**
  *
  * @author elegios
  */
 public class Player implements Position, Renderable, Updatable, RelativeMovable, Rectangle, KeyReceiver {
     static final float JUMP_SPEED = -320;
     static final float MOVE_SPEED =  160; // 5 squ/igs
     static final float BASE_MASS  =  20;
 
    static final int NAME_HEIGHT = 20;
 
     private boolean collidedWithImobileX;
     private boolean collidedWithImobileY;
     private float   collisionLockX;
     private float   collisionLockY;
 
     private float mass;
 
     private int id;
 
     private float x;
     private float y;
 
     private float xSpeed;
     private float ySpeed;
 
     private PlayerPositionPackage toUpdatePos;
     private RelativePlayerPositionPackage toUpdateRel;
 
     private boolean moveRight;
     private boolean moveLeft;
     private boolean jump;
 
     private boolean downMotion;
     private RelativeMovable collidedY;
     private Vehicle lastVehicle;
     private boolean airResistX;
 
     private Builder builder;
 
     private int width;
     private int height;
 
     private World world;
 
     private MediaLoader loader;
 
     private ManagedImage player;
     private String name;
 
     public Player(World world, int id, int x, int y) throws SlickException {
         this.world      = world;
         this.id         = id;
 
         if (world.view().playerId() != id)
             name = world.view().net().getPlayerName(id);
         else
             name = null;
 
         loader = world.view().loader();
 
         player = loader.loadManagedImage("player");
         width  = player.getImage().getWidth();
         height = player.getImage().getHeight();
 
         builder = new Builder(world.view().inventory(), this);
 
         mass = BASE_MASS;
         this.x = x;
         this.y = y;
         xSpeed = 0.0f;
         ySpeed = 0.0f;
     }
 
     public void moveX(int diff) {
         x += getAbsXMove(diff);
 
         if (toUpdateRel != null) {
             toUpdateRel.xChecked(true);
 
             if (lastVehicle == null || lastVehicle.getID() != toUpdateRel.getVehicleId())
                 lastVehicle = world.findVehicle(toUpdateRel.getVehicleId());
 
             x      = toUpdateRel.getX() + lastVehicle.getX();
             xSpeed = toUpdateRel.getXSpeed();
 
         } else if (toUpdatePos != null) {
             toUpdatePos.xChecked(true);
 
             x      = toUpdatePos.getX();
             xSpeed = toUpdatePos.getXSpeed();
         }
     }
     public void moveY(int diff) {
         y += getAbsYMove(diff);
 
         if (toUpdateRel != null && toUpdateRel.xChecked()) {
             if (lastVehicle == null || lastVehicle.getID() != toUpdateRel.getVehicleId())
                 lastVehicle = world.findVehicle(toUpdateRel.getVehicleId());
 
             y      = toUpdateRel.getY() + lastVehicle.getY();
             ySpeed = toUpdateRel.getYSpeed();
 
             toUpdateRel = null;
 
         } else if (toUpdatePos != null && toUpdatePos.xChecked()) {
             y      = toUpdatePos.getY();
             ySpeed = toUpdatePos.getYSpeed();
 
             toUpdatePos = null;
         }
     }
 
     public void relMoveX(Vehicle vehicle, float move) {
         if (vehicle == lastVehicle)
             x += move;
     }
     public void relMoveY(Vehicle vehicle, float move) {
         if (vehicle == lastVehicle)
             y += move;
     }
 
 
     public void updateEarly(GameContainer gc, int diff) {
         if (lastVehicle != null && collidedY != null && collidedY instanceof Vehicle) {
             lastVehicle = (Vehicle) collidedY; //Fix bug where last vehicle touched is moved towards a player walking towards it
         }
 
         //move left
         if (moveLeft && !moveRight)
             if (airResistX) {
                 if (xSpeed > lastVehicle.getAbsXSpeed() - MOVE_SPEED) {
                     if (!lastVehicle.collidedWithImmobileY())
                         lastVehicle.pushX(mass * 32);
                     xSpeed -= 32;
                 }
             } else
                 if (xSpeed > - MOVE_SPEED)
                     xSpeed -= 32;
 
         //move right
         if (!moveLeft && moveRight)
             if (airResistX) {
                 if (xSpeed < lastVehicle.getAbsXSpeed() + MOVE_SPEED) {
                     if (!lastVehicle.collidedWithImmobileY())
                         lastVehicle.pushX(-mass * 32);
                     xSpeed += 32;
                 }
             } else
                 if (xSpeed < MOVE_SPEED)
                     xSpeed += 32;
 
         //jump
         if (collidedY != null && downMotion && jump) {
             collidedY.pushY(-JUMP_SPEED * mass);
             if (collidedY.collidedWithImmobileY())
                 ySpeed = JUMP_SPEED;
             else
                 ySpeed = JUMP_SPEED + collidedY.getAbsYSpeed();
         }
 
         downMotion = false;
         collidedY = null;
         collidedWithImobileX = false;
         collidedWithImobileY = false;
     }
 
     public void update(GameContainer gc, int diff) { //TODO: find out why position flickers for clients only
        if (collidedY != null)
            if (collidedY instanceof Vehicle) {
                lastVehicle = (Vehicle) collidedY;
            } else
                lastVehicle = null;

         if (lastVehicle == null || !doAirResistX(lastVehicle)) {
             boolean airResisted = false;
             for (Vehicle vehicle : world.vehicles())
                 if (doAirResistX(vehicle)) {
                     lastVehicle = vehicle;
                     airResisted = true;
                     break;
                 }
             if (!airResisted)
                 doAirResistX();
         }
         if (lastVehicle == null || !doAirResistY(lastVehicle)) {
             boolean airResisted = false;
             for (Vehicle vehicle : world.vehicles())
                 if (doAirResistY(vehicle)) {
                     airResisted = true;
                     break;
                 }
             if (!airResisted)
                 doAirResistY();
         }
 
         ySpeed += world.actionsPerTick() * diff * world.gravity();
 
         if (world.updatePos() && world.currPlayer() == this && world.view().net().isOnline()) {
             if (lastVehicle != null && !(lastVehicle instanceof ImmobileVehicle))
                 world.view().net().send(ShipProtocol.REL_PLAYER_POS, new RelativePlayerPositionPackage(id, lastVehicle.getID(),
                                                                                                        x - lastVehicle.getX(),
                                                                                                        y - lastVehicle.getY(),
                                                                                                        xSpeed, ySpeed));
 
             else
                 world.view().net().send(ShipProtocol.PLAYER_POS, new PlayerPositionPackage(id, x, y, xSpeed, ySpeed));
         }
 
     }
 
     private boolean doAirResistX(Vehicle vehicle) {
         int playX = vehicle.getTileXUnderPos(getX() + getWidth ()/2);
         int playY = vehicle.getTileYUnderPos(getY() + getHeight()/2);
 
         if (playX >= 0              && playX <  vehicle.WIDTH() &&
             playY >= vehicle.topY() && playY <= vehicle.botY ()) {
             if (xSpeed > 0) {
                 for (int i = playX; i <= vehicle.rightX(); i++) {
                     if (vehicle.existsAt(i, playY)) {
                         pushX(-(xSpeed - vehicle.getAbsXSpeed()) * world.airResist());
                         airResistX = true;
                         return true;
                     }
                 }
 
             } else if (xSpeed < 0) {
                 for (int i = playX; i >= vehicle.leftX(); i--) {
                     if (vehicle.existsAt(i, playY)) {
                         pushX(-(xSpeed - vehicle.getAbsXSpeed()) * world.airResist());
                         airResistX = true;
                         return true;
                     }
                 }
 
             }
         }
 
         return false;
     }
     private void doAirResistX() {
         pushX(-xSpeed * world.airResist());
         airResistX = false;
     }
 
     private boolean doAirResistY(Vehicle vehicle) {
         int playX = vehicle.getTileXUnderPos(getX() + getWidth ()/2);
         int playY = vehicle.getTileYUnderPos(getY() + getHeight()/2);
 
         if (playX >= vehicle.leftX() && playX <= vehicle.rightX() &&
             playY >= 0               && playY <  vehicle.HEIGHT()) {
             if (ySpeed > 0) {
                 for (int j = playY; j <= vehicle.botY(); j++) {
                     if (vehicle.existsAt(playX, j)) {
                         pushY(-(ySpeed - vehicle.getAbsYSpeed()) * world.airResist());
                         return true;
                     }
                 }
 
             } else if (ySpeed < 0) {
                 for (int j = playY; j >= vehicle.topY(); j--) {
                     if (vehicle.existsAt(playX, j)) {
                         pushY(-(ySpeed - vehicle.getAbsYSpeed()) * world.airResist());
                         return true;
                     }
                 }
 
             }
         }
 
         return false;
     }
     private void doAirResistY() {
         pushY(-ySpeed * world.airResist());
     }
 
     public void collisionFixPosX(float xMove, RelativeMovable collisionOrigin) {
         x += xMove;
 
         if (collisionOrigin.collidedWithImmobileX())
             collidedWithImobileX = true;
     }
 
     public void collisionFixPosY(float yMove, RelativeMovable collisionOrigin) {
         y += yMove;
         if (yMove < 0)
             downMotion = true;
         collidedY = collisionOrigin;
 
         if (collisionOrigin.collidedWithImmobileY())
             collidedWithImobileY = true;
     }
 
     public void render(GameContainer gc, Graphics g) {
         player.getImage().draw(ix(), iy());
         builder.render(gc, g);
 
         if (name != null) {
             Font font = world.view().fonts().name();
             font.drawString(ix() + width/2 - font.getWidth(name)/2, iy() - NAME_HEIGHT , name);
         }
     }
 
     public void pushX(float momentum) { xSpeed += momentum / mass; }
     public void pushY(float momentum) { ySpeed += momentum / mass; }
 
     public float getAbsXSpeed() { return xSpeed; }
     public float getAbsYSpeed() { return ySpeed; }
 
     public float getAbsXMove(int diff) { return getAbsXSpeed() * diff * world.actionsPerTick(); }
     public float getAbsYMove(int diff) { return getAbsYSpeed() * diff * world.actionsPerTick(); }
 
     public float getMass() { return mass; }
 
     public int getHeight() { return height; }
     public int getWidth()  { return  width; }
 
     public float getX2() { return getX() +  getWidth() - 1; }
     public float getY2() { return getY() + getHeight() - 1; }
 
     public float getX() { return x; }
     public float getY() { return y; }
 
     public int ix() { return Math.round(world.getX() + getX()); }
     public int iy() { return Math.round(world.getY() + getY()); }
 
     public boolean collidedWithImmobileX() { return collidedWithImobileX; }
     public boolean collidedWithImmobileY() { return collidedWithImobileY; }
     public float   collisionLockX()        { return collisionLockX;       }
     public float   collisionLockY()        { return collisionLockY;       }
 
     public void collidedWithImmobileX(boolean val) { collidedWithImobileX = val; }
     public void collidedWithImmobileY(boolean val) { collidedWithImobileY = val; }
     public void collisionLockX       (float   val) { collisionLockX       = val; }
     public void collisionLockY       (float   val) { collisionLockY       = val; }
 
     public Builder builder() { return builder; }
 
     public World world() { return world; }
 
     /*
      * TODO: create receiving mechanisms for the following:
      * - x, y, xSpeed, ySpeed
      * - mass
      * - moveLeft
      * - moveRight
      * - jump
      * - builder data
      */
 
     public void receivePlayerPositionPackage(PlayerPositionPackage pack) {
         if ((toUpdatePos == null || !toUpdatePos.xChecked()) && (toUpdateRel == null || !toUpdateRel.xChecked()))
             toUpdatePos = pack;
     }
 
     public void receiveRelativePlayerPositionPackage(RelativePlayerPositionPackage pack) {
         if ((toUpdatePos == null || !toUpdatePos.xChecked()) && (toUpdateRel == null || !toUpdateRel.xChecked()))
             toUpdateRel = pack;
     }
 
     public void receivePlayerMovementPackage(PlayerMovementPackage pack) {
         switch (pack.getType()) {
             case PlayerMovementPackage.MOVE_LEFT:
                 moveLeft = pack.getValue();
                 break;
 
             case PlayerMovementPackage.MOVE_RIGHT:
                 moveRight = pack.getValue();
                 break;
 
             case PlayerMovementPackage.JUMP:
                 jump = pack.getValue();
                 break;
         }
     }
 
     public boolean keyPressed(Keys keys, int key, char c) {
         if (key == keys.right()) {
             moveRight = true;
             if (world.view().net().isOnline())
                 world.view().net().send(ShipProtocol.PLAYER_MOVE, new PlayerMovementPackage(id, PlayerMovementPackage.MOVE_RIGHT, true));
             return true;
 
         } if (key == keys.left()) {
             moveLeft = true;
             if (world.view().net().isOnline())
                 world.view().net().send(ShipProtocol.PLAYER_MOVE, new PlayerMovementPackage(id, PlayerMovementPackage.MOVE_LEFT, true));
             return true;
 
         } if (key == keys.up()) {
             jump = true;
             if (world.view().net().isOnline())
                 world.view().net().send(ShipProtocol.PLAYER_MOVE, new PlayerMovementPackage(id, PlayerMovementPackage.JUMP, true));
             return true;
 
         } if (key == keys.activateDevice()) {
             world.activateUnderPlayer(this);
             return true;
         }
 
         return builder.keyPressed(keys, key, c);
     }
 
     public boolean keyReleased(Keys keys, int key, char c) {
         if (key == keys.right()) {
             moveRight = false;
             if (world.view().net().isOnline())
                 world.view().net().send(ShipProtocol.PLAYER_MOVE, new PlayerMovementPackage(id, PlayerMovementPackage.MOVE_RIGHT, false));
             return true;
 
         } else if (key == keys.left()) {
             moveLeft = false;
             if (world.view().net().isOnline())
                 world.view().net().send(ShipProtocol.PLAYER_MOVE, new PlayerMovementPackage(id, PlayerMovementPackage.MOVE_LEFT, false));
             return true;
 
         } else if (key == keys.up()) {
             jump = false;
             if (world.view().net().isOnline())
                 world.view().net().send(ShipProtocol.PLAYER_MOVE, new PlayerMovementPackage(id, PlayerMovementPackage.JUMP, false));
             return true;
         }
 
         return builder.keyReleased(keys, key, c);
     }
 
     public int getID() {
         return id;
     }
 
 }
