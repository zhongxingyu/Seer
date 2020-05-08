 package ua.org.dector.ludumdare.ld24;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.math.Rectangle;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import static ua.org.dector.ludumdare.ld24.Renderer.BLOCK_SIZE;
 
 /**
  * @author dector
  */
 public class Level {
     public static final int NOTHING = 0;
     public static final int BLOCK   = 0x000000ff;
     public static final int SPAWN   = 0x00ff00ff;
     public static final int EXIT    = 0xff00ffff;
     public static final int WATER   = 0x0000ffff;
     public static final int DEATH   = 0xff0000ff;
     public static final int GLASS   = 0xffff00ff;
 
     public static final int AB_SWIM = 0x009900ff;
     public static final int AB_GAS  = 0x66ccffff;
     public static final int AB_SLICK= 0xff99ffff;
     public static final int AB_NORMAL= 0xffffffff;
     public static final int AB_SOLID= 0x9933ffff;
     public static final int AB_LIQUID= 0x00ffffff;
 
     int width;
     int height;
     Tile[][] map;
     
     boolean wasCollided;
     int collidedCount;
     int waterCount;
 
     boolean paused = false;
 
     Renderer renderer;
 
     int spawnX;
     int spawnY;
 
     boolean started;
 
     Player player;
     Map<Point, Point> tubes;
     
     public void load(String file) {
         tubes = new HashMap<Point, Point>();
 
         Map<Integer, Point> unpairedTubes = new HashMap<Integer, Point>();
         
         Pixmap p = new Pixmap(Gdx.files.internal(file));
         
         width = p.getWidth();
         height = p.getHeight();
         map = new Tile[width][height];
 
         int pix;
         int tmp;
         for (int x = 0; x < width; x++) {
             for (int y = 0; y < height; y++) {
                 pix = p.getPixel(x, height - y - 1);
 //                System.out.printf("%d:%d %s%n", x, y, Integer.toHexString(t));
                 
                 switch (pix) {
                     case BLOCK:
                         map[x][y] = Tile.BLOCK; break;
                     case SPAWN: {
                         player = new Player(BLOCK_SIZE * x, BLOCK_SIZE * y);
                         map[x][y] = Tile.SPAWN;
                         spawnX = x * BLOCK_SIZE;
                         spawnY = y * BLOCK_SIZE;
                     } break;
                     case EXIT: {
                         map[x][y] = Tile.EXIT;
                     } break;
                     case DEATH: {
                         map[x][y] = Tile.DEATH;
                     } break;
                     case WATER: {
                         map[x][y] = Tile.WATER;
                     } break;
                     case GLASS: {
                         map[x][y] = Tile.GLASS;
                     } break;
                     
                     case AB_SWIM: map[x][y] = Tile.AB_SWIM; break;
                     case AB_GAS:  map[x][y] = Tile.AB_GAS; break;
                     case AB_SLICK:map[x][y] = Tile.AB_SLICK; break;
                     case AB_NORMAL:map[x][y] = Tile.AB_NORMAL; break;
                     case AB_LIQUID:map[x][y] = Tile.AB_LIQUID; break;
                     case AB_SOLID:map[x][y] = Tile.AB_SOLID; break;
 
                     default: {  //  Test for tube
                         tmp = (pix & 0xff000000) >>> 24;
 
                         if (tmp == 0xCC) {
                             int tubeId = (pix & 0x0000ff00) >>> 8;
                             Point tubePoint = new Point(x, y);
 
                             int tubeDir = (pix & 0x00ff0000) >>> 16;
 
                             Tile tile = null;
                             switch (tubeDir) {
                                 case 0: tile = Tile.TUBE_UP; break;
                                 case 0xff: tile = Tile.TUBE_RIGHT; break;
                                 case 0x66: tile = Tile.TUBE_LEFT; break;
                                 case 0x99: tile = Tile.TUBE_DOWN; break;
                             }
 
                             map[x][y] = tile;
 
                             if (unpairedTubes.containsKey(tubeId)) {
                                 Point otherPoint = unpairedTubes.remove(tubeId);
 
                                 tubes.put(tubePoint, otherPoint);
                                 tubes.put(otherPoint, tubePoint);
                             } else {
                                 unpairedTubes.put(tubeId, tubePoint);
                             }
                         }
                     }
                 }
             }
         }
     }
 
     public void update(float dt) {
         player.update(dt);
 
         tryToMovePlayer();
     }
 
     private void tryToMovePlayer() {
         if (player.jumpCommand) {
             int nextBlock = (player.gravityDirection < 0) ? -1 : 2;
 
             Tile tile1 = map[(int)player.x / BLOCK_SIZE][(int)player.y / BLOCK_SIZE + nextBlock];
             Tile tile2 = map[(int)player.x / BLOCK_SIZE + 1][(int)player.y / BLOCK_SIZE + nextBlock];
 
             boolean onTheGround = player.state != State.SWIM
                     && (tile1 == Tile.BLOCK || tile1 == Tile.GLASS || tile1 == Tile.TUBE_UP || tile1 == Tile.TUBE_RIGHT
                     || tile1 == Tile.TUBE_DOWN || tile1 == Tile.TUBE_LEFT
                     || tile2 == Tile.BLOCK || tile2 == Tile.GLASS || tile2 == Tile.TUBE_UP || tile2 == Tile.TUBE_RIGHT
                     || tile2 == Tile.TUBE_DOWN || tile2 == Tile.TUBE_LEFT);
 
             if (! player.isJumping && onTheGround && ! player.abilities.contains(Ability.SLICK)) {
 //                Sounds.get().play(Sounds.JUMP);
                 player.vy -= player.gravityDirection * Player.JUMPING;
                 player.isJumping = true;
             }
 
             player.jumpCommand = false;
         }
 
         Rectangle pr = new Rectangle((int)player.x / BLOCK_SIZE, (int)Math.floor(player.y / BLOCK_SIZE), 1, 1);
         Rectangle[] rs;
 
         boolean collided = false;
 
         player.x += player.vx;
         pr.setX((int)player.x / BLOCK_SIZE);
         rs = checkCollisions();
         for (Rectangle r : rs) {
             if (pr.overlaps(r)) {
                 if (player.vx < 0)
                     player.x = (r.x + 1) * BLOCK_SIZE + 0.01f;
                 else
                     player.x = (r.x - 1) * BLOCK_SIZE - 0.01f;
 
                 collided = true;
             }
         }
 
         if (collided) { player.vx = 0; /*player.ax = 0;*/ }
         collided = false;
 
         player.y += player.vy;
         pr.setX((int)player.x / BLOCK_SIZE);
         pr.setY((int)Math.floor(player.y / BLOCK_SIZE));
         rs = checkCollisions();
         for (Rectangle r : rs) {
             if (pr.overlaps(r)) {
                 if (player.vy < 0)
                     player.y = (r.y + 1) * BLOCK_SIZE + 0.01f;
                 else
                     player.y = (r.y - 1) * BLOCK_SIZE - 0.01f;
 
                 collided = true;
             }
         }
 
         if (collided) {
             if (player.isJumping && player.vy * player.gravityDirection > 0)
                 player.isJumping = false;
 
             if (player.gravityDirection > 0 && player.vy > 0) {
                 if (! player.abilities.contains(Ability.SLICK)) {
                     player.gravityDirection = -1;
                     wasCollided = true;
                     collidedCount = 50;
                 } 
                 
                 player.abilities.remove(Ability.GAS);
             }
 
             player.vy = 0;
             player.ay = 0;
 
             if (! player.gravityAffection) {
                 wasCollided = true;
                 collidedCount++;
             }
 
         } else if (! player.gravityAffection && wasCollided) {
             if (collidedCount > 5) {
                 player.clearSlick();
                 wasCollided = false;
             }
         }
     }
 
     private Rectangle[] checkCollisions() {
         int px = (int)player.x / BLOCK_SIZE;
         int py = (int)Math.floor(player.y / BLOCK_SIZE);
 
         int[] x = { px, px + 1, px + 1, px };
         int[] y = { py, py, py + 1, py + 1 };
 
         Tile[] tiles = { map[x[0]][y[0]], map[x[1]][y[1]], map[x[2]][y[2]], map[x[3]][y[3]] };
         
         Rectangle[] r = { new Rectangle(), new Rectangle(), new Rectangle(), new Rectangle() };
 
         boolean inWater = false;
         boolean broke = false;
         
         for (int i = 0; i < tiles.length; i++) {
             if (tiles[i] != null)
                 switch (tiles[i]) {
                     case BLOCK: r[i].set(x[i], y[i], 1, 1); break;
                     case EXIT: {
                         player.win = true;
                         Sounds.get().play(Sounds.WIN);
                     } break;
                     case DEATH: die(); break;
                     case WATER: {
                         if (! inWater) inWater = true;
                     } break;
                     case GLASS: {
                         if (! player.abilities.contains(Ability.SOLID))
                             r[i].set(x[i], y[i], 1, 1);
 
                         if (Math.abs(player.vy) == Player.MAX_SPEED_Y && player.abilities.contains(Ability.SOLID)) {
                             map[x[i]][y[i]] = null;
                             broke = true;
                             
                             Sounds.get().play(Sounds.CRASH);
                         }
                     } break;
                     case TUBE_UP:
                     case TUBE_RIGHT:
                     case TUBE_DOWN:
                     case TUBE_LEFT: {
                         if (! player.abilities.contains(Ability.LIQUID)) {
                             r[i].set(x[i], y[i], 1, 1);
                         } else {
                             Point otherTube = tubes.get(new Point(x[i], y[i]));
 
                             if (otherTube != null)
                                 switch (map[otherTube.x][otherTube.y]) {
                                     case TUBE_UP: {
                                         if (player.vy < 0) {
                                             player.stop();
                                             player.x = otherTube.x * BLOCK_SIZE;
                                             player.y = otherTube.y * BLOCK_SIZE + BLOCK_SIZE + 1;
                                             player.abilities.remove(Ability.LIQUID);
                                             Sounds.get().play(Sounds.WHEEP);
                                         }
                                     } break;
                                     case TUBE_RIGHT: {
                                         if (player.vx < 0) {
                                             player.stop();
                                             player.x = otherTube.x * BLOCK_SIZE + BLOCK_SIZE + 1;
                                             player.y = otherTube.y * BLOCK_SIZE;
                                             player.abilities.remove(Ability.LIQUID);
                                             Sounds.get().play(Sounds.WHEEP);
                                         }
                                     } break;
                                     case TUBE_DOWN: {
                                         if (player.vy > 0) {
                                             player.stop();
                                             player.x = otherTube.x * BLOCK_SIZE;
                                             player.y = otherTube.y * BLOCK_SIZE - BLOCK_SIZE - 1;
                                             player.abilities.remove(Ability.LIQUID);
                                             Sounds.get().play(Sounds.WHEEP);
                                         }
                                     } break;
                                     case TUBE_LEFT: {
                                         if (player.vx > 0) {
                                             player.stop();
                                             player.x = otherTube.x * BLOCK_SIZE - BLOCK_SIZE - 1;
                                             player.y = otherTube.y * BLOCK_SIZE;
                                             player.abilities.remove(Ability.LIQUID);
                                             Sounds.get().play(Sounds.WHEEP);
                                         }
                                     } break;
                                 }
                         }
                     } break;
                     case AB_SWIM: {
                         player.abilities.add(Ability.SWIM);
                         removeTile(x[i], y[i]);
                         Sounds.get().play(Sounds.POWER_UP);
                     } break;
                     case AB_GAS: {
                         player.gravityDirection = 1;
                         player.abilities.add(Ability.GAS);
                         removeTile(x[i], y[i]);
                         Sounds.get().play(Sounds.POWER_UP);
                     } break;
                     case AB_SLICK: {
                         player.gravityAffection = false;
                         player.abilities.add(Ability.SLICK);
                         removeTile(x[i], y[i]);
                         Sounds.get().play(Sounds.POWER_UP);
                        collidedCount = 0;
                     } break;
                     case AB_NORMAL: {
                         player.gravityAffection = true;
                         player.gravityDirection = -1;
                         player.abilities.clear();
                         removeTile(x[i], y[i]);
                         Sounds.get().play(Sounds.POWER_UP);
                     } break;
                     case AB_SOLID: {
                         player.canJump = false;
                         player.abilities.add(Ability.SOLID);
                         removeTile(x[i], y[i]);
                         Sounds.get().play(Sounds.POWER_UP);
                     } break;
                     case AB_LIQUID: {
                         player.abilities.add(Ability.LIQUID);
                         removeTile(x[i], y[i]);
                         Sounds.get().play(Sounds.POWER_UP);
                     } break;
                     default: r[i].set(-1, -1, 1, 1); break;
 
                 }
         }
 
         if (broke) {
             player.abilities.remove(Ability.SOLID);
             player.canJump = true;
         }
 
         if (player.state == State.SWIM) {
             if (! inWater) {
                 waterCount++;
 
                 if (waterCount > 25) {
                     player.state = State.RUNNING;
                     player.abilities.remove(Ability.SWIM);
                 }
             }
         } else if (inWater) {
             if (! player.abilities.contains(Ability.SWIM))
                 die();
             else {
                 player.state = State.SWIM;
                 waterCount = 0;
             }
         }
         
         return r;
     }
 
     private void removeTile(int x, int y) {
         map[x][y] = null;
     }
 
     private void die() {
         restart();
     }
 
     void restart() {
         Sounds.get().play(Sounds.HIT);
         load(Levelset.getLevel());
         renderer.loadLevelTexs();
         collidedCount = 0;
         waterCount = 0;
         wasCollided = false;
         paused = false;
         player.restart(spawnX, spawnY);
     }
 
 
 }
 
