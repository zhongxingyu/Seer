 package ogo.spec.game.graphics.view;
 
 import com.jogamp.opengl.util.FPSAnimator;
 import ogo.spec.game.model.*;
 import com.jogamp.opengl.util.gl2.GLUT;
 import com.jogamp.opengl.util.texture.Texture;
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.event.*;
 import java.io.FileNotFoundException;
 import javax.media.opengl.GL;
 import static javax.media.opengl.GL2.*;
 import static java.lang.Math.*;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.media.opengl.GL2;
 import javax.media.opengl.GLDrawable;
 import javax.media.opengl.GLException;
 import javax.media.opengl.awt.GLJPanel;
 import javax.media.opengl.glu.GLU;
 import javax.swing.UIManager;
 
 public class GUI extends Base {
 
     Game game;
     ClickListener clickListener;
     KeyListener keyListener;
     int clicki = -1, clickj = -1;
     final static int SEACREATURE = 1;
     final static int AIRCREATURE = 2;
     final static int LANDCREATURE = 3;
     final static int FOOD = 4;
     Player player;
     Vector vViewChange = null;
     Creature currentCreature;
     Timer timer = new Timer(30);
     Thread timerThread;
     Map<Creature, CreatureView> creatureViews = new HashMap<Creature, CreatureView>();
     Wavefront models;
     
     boolean gameStarted;
     float[][] materials = {Materials.BLUE_PLASTIC, Materials.RED_PLASTIC, Materials.YELLOW_PLASTIC, Materials.GREEN_PLASTIC, Materials.ORANGE_PLASTIC, Materials.BROWN_PLASTIC};
 
     public GUI() {
         super();
         
         this.gameStarted = false;
     }
 
     /**
      * Constructs GUI class.
      */
     public GUI(Game game, Player player) {
         super();
         this.game = game;
         this.player = player;
         
         this.gameStarted = false;
     }
     
     public void close(){
         super.close();
         timerThread.stop();
     }
 
     /**
      * Called upon the start of the application. Primarily used to configure
      * OpenGL.
      */
     @Override
     public void initialize() {
         GLJPanel glPanel = (GLJPanel) frame.glPanel;
         clickListener = new ClickListener();
         glPanel.addMouseListener(clickListener);
         keyListener = new NumberKeysListener();
         glPanel.addKeyListener(keyListener);
 
         // Enable blending.
         gl.glEnable(GL_BLEND);
         gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 
         // Enable anti-aliasing.
         gl.glEnable(GL_LINE_SMOOTH);
         //gl.glEnable(GL_POLYGON_SMOOTH);
         gl.glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
         //gl.glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);
 
         // Enable depth testing.
         gl.glEnable(GL_DEPTH_TEST);
         gl.glDepthFunc(GL_LESS);
 
         // Enable textures.
         gl.glEnable(GL_TEXTURE_2D);
         gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
 
         // Create game object
         if (game == null) {
             Random generator = new Random(0);
             TileType[][] types = new TileType[50][50];
             for (int i = 0; i < types.length; i++) {
                 for (int j = 0; j < types[0].length; j++) {
                     int type = generator.nextInt(3);
                     switch (type) {
                         case 0:
                             types[j][i] = TileType.DEEP_WATER;
                             break;
                         case 1:
                             types[j][i] = TileType.LAND;
                             break;
                         case 2:
                             types[j][i] = TileType.SHALLOW_WATER;
                             break;
                     }
                 }
             }
             GameMap map = new GameMap(types);
             AirCreature a1 = new AirCreature(map.getTile(1, 0), map, 0);
             SeaCreature s1 = new SeaCreature(map.getTile(2, 0), map, 1);
             LandCreature l1 = new LandCreature(map.getTile(3, 0), map, 2);
             AirCreature a2 = new AirCreature(map.getTile(1, 1), map, 3);
             SeaCreature s2 = new SeaCreature(map.getTile(2, 1), map, 4);
             LandCreature l2 = new LandCreature(map.getTile(3, 1), map, 5);
             map.getTile(5, 0).setInhabitant(new Food(0));
 
             Player p1 = new Player("1",0);
             Creature[] p1c = {a1, s1, l1};
             p1.setCreatures(p1c);
             Player p2 = new Player("2",1);
             Creature[] p2c = {a2, s2, l2};
             p2.setCreatures(p2c);
             player = p1;
             currentCreature = a1;
             Player[] players = new Player[2];
             players[0] = p1;
             players[1] = p2;
             try {
                 game = new Game(players, map, 0);
             } catch (Exception ex) {
                 Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         currentCreature = player.getCreatures()[0];
 
         creatureViews = new HashMap<Creature, CreatureView>();
         for (Player p : game) {
             for (Creature c : p) {
                 CreatureView creatureView = new CreatureView(c, timer);
                 creatureViews.put(c, creatureView);
             }
         }
         models = new Wavefront();
 
 
         String path = "src/ogo/spec/game/graphics/models/";
         try {
             models.readWavefront(path + "land.obj", gl);
             models.normalize();
             gl.glNewList(LANDCREATURE, GL_COMPILE);
             models.drawTriangles();
             gl.glEndList();
             //Seacreature
             models.readWavefront(path + "sea.obj", gl);
             models.normalize();
             gl.glNewList(SEACREATURE, GL_COMPILE);
             gl.glPushMatrix();
             gl.glTranslatef(0f, 1f, 0f);
             gl.glRotatef(90f, 1f, 0f, 0f);
             models.drawTriangles();
             gl.glPopMatrix();
             gl.glEndList();
             //Aircreature
             models.readWavefront(path + "air.obj", gl);
             models.normalize();
             gl.glNewList(AIRCREATURE, GL_COMPILE);
             gl.glPushMatrix();
             gl.glTranslatef(0f, 1f, 0f);
             gl.glRotatef(90f, 1f, 0f, 0f);
             models.drawTriangles();
             gl.glPopMatrix();
             gl.glEndList();
             //Food
             models.readWavefront(path + "food.obj", gl);
             models.normalize2();
             gl.glNewList(FOOD, GL_COMPILE);
             models.drawTriangles();
             gl.glEndList();
         } catch (FileNotFoundException ex) {
             Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         if(!gameStarted){
             timerThread = new Thread(timer);
             timerThread.start();
             game.start();
             gameStarted = true;
         }
     }
 
     /**
      * Configures the viewing transform.
      */
     @Override
     public void setView() {
         // Select part of window.
         gl.glViewport(0, 0, gs.w, gs.h);
 
         // Set projection matrix.
         gl.glMatrixMode(GL_PROJECTION);
         gl.glLoadIdentity();
         float height = gs.vWidth / ((float)gs.w / gs.h);
         gl.glOrtho(-0.5 * gs.vWidth, 0.5 * gs.vWidth, -0.5 * height, 0.5 * height, 0.1, 1000);
         // Set camera.
         gl.glMatrixMode(GL_MODELVIEW);
         gl.glLoadIdentity();
 
 
         Vector dir = new Vector(cos(gs.phi) * cos(gs.theta),
                 sin(gs.phi) * cos(gs.theta),
                 sin(gs.theta));
 
         Vector eye;
         eye = debug ? gs.cnt.add(dir.scale(gs.vDist)):
         new Vector(gs.cnt.x() - 40f, gs.cnt.y() - 40f, gs.cnt.z() + 30f);
 
         glu.gluLookAt(eye.x(), eye.y(), eye.z(), // eye point
                 gs.cnt.x(), gs.cnt.y(), gs.cnt.z(), // center point
                 0, 0, 1); // up axis
     }
 
     /**
      * Draws the entire scene.
      */
     @Override
     public void drawScene() {
         if (clickListener.x != -1) {
             int x = clickListener.x;
             int y = clickListener.y;
 
             clickListener.x = -1;
             clickListener.y = -1;
             handleMouseClick(x, y);
             //System.out.println(game.getMap().getTile(clicki, clickj).getX() + "," + game.getMap().getTile(clicki, clickj).getY());
             if (clicki != -1) {
                 currentCreature.select(game.getMap().getTile(clicki, clickj));
                 creatureViews.get(currentCreature).move(Creature.TICKS_PER_TILE_AVG * Game.TICK_TIME_IN_MS);
             }
 
             //gs.cnt = vViewChange.add(new Vector(clickj, clicki, 0));
 
         }
         gl.glMatrixMode(GL_MODELVIEW);
 
         // Enable lighting
         gl.glEnable(GL_LIGHTING);
         gl.glEnable(GL_LIGHT0);
         gl.glEnable(GL_NORMALIZE);
 
         // Draw stuff.
         draw();
         if (player.isAttacking()) {
             drawAttackNotice(200);
         }
         drawMiniMap(gs.h/2);
     }
 
     private void draw() {
         // Background color.
         //gl.glClearColor(1f, 1f, 1f, 0f);
         float[] player_color = materials[player.getId()];
         gl.glClearColor(player_color[4], player_color[5], player_color[6],
                 player_color[7]);
 
         // Clear background.
         gl.glClear(GL_COLOR_BUFFER_BIT);
 
         // Clear depth buffer.
         gl.glClear(GL_DEPTH_BUFFER_BIT);
 
         // Set color to black.
         gl.glColor3f(0f, 0f, 0f);
 
         // Draw layer under map.
         gl.glBindTexture(GL_TEXTURE_2D, 0);
         gl.glColor3f(1, 0, 1);
         gl.glBegin(GL_QUADS);
         float v = 000;
         gl.glVertex3f(-v, -v, -1);
         gl.glVertex3f(-v, v, -1);
         gl.glVertex3f(v, v, -1);
         gl.glVertex3f(v, -v, -1);
         gl.glEnd();
 
         // Draw map.
         drawMap(game.getMap());
 
         /*float[] material = {
          0.24725f, 0.1995f, 0.0745f, 1.0f, //ambient
          0.75164f, 0.60648f, 0.22648f, 1.0f, //diffuse
          0.628281f, 0.555802f, 0.366065f, 1.0f, //specular
          51.2f //shininess
          };*/
         /*float[] material = {
          0.000000f, 0.000000f, 0.000000f, 1f,
          1.000000f, 1.000000f, 1.000000f, 1f,
          1.000000f, 1.000000f, 1.000000f, 1f,
          512.000000f};*/
 
         float[] material = {
             0f, 0f, 0f, 1.0f, //ambient
             1f, 1f, 1f, 1.0f, //diffuse
             1f, 1f, 1f, 1.0f, //specular
             51.2f //shininess
         };
 
         gl.glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, material, 0);
         gl.glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, material, 4);
         gl.glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, material, 8);
         gl.glMaterialfv(GL_FRONT_AND_BACK, GL_SHININESS, material, 12);
         gl.glTranslated(0.5, 0.5, 0.5);
         //glut.glutWireCube(1);
         //gl.glTranslated(-0.5, -0.5, -0.5);
         //w.drawTriangles();
 
     }
 
     private void drawMap(GameMap map) throws GLException {
         gl.glPushMatrix();
         //gl.glTranslatef(-map.getHeight() / 2, -map.getWidth() / 2, 0.0f);
         if (vViewChange == null) {
             vViewChange = Vector.O;
             gs.cnt = vViewChange;
         }
         setMaterial(Materials.WHITE);
         for (int i = 0; i < map.getHeight(); i++) {
             for (int j = 0; j < map.getWidth(); j++) {
                 // Load unique name for this tile.
                 gl.glLoadName(i * map.getHeight() + j + 1);
 
                 TileType type = map.getTile(i, j).getType();
                 gl.glColor3f(1, 1, 1);
                 switch (type) {
                     case DEEP_WATER:
                         //gl.glColor3f(0, 0, 1);
                         deepWater.bind(gl);
                         break;
                     case SHALLOW_WATER:
                         //gl.glColor3f(0, 1, 0);
                         shallowWater.bind(gl);
                         break;
                     case LAND:
                         //gl.glColor3f(1, 0, 0);
                         land.bind(gl);
                         break;
                 }
                 // Draw tile.
                 gl.glBegin(GL_QUADS);
                 gl.glNormal3f(0, 0, 1);
                 gl.glTexCoord2d(0, 0);
                 gl.glVertex3d(0, 0, 0);
                 gl.glTexCoord2d(1, 0);
                 gl.glVertex3d(1, 0, 0);
                 gl.glTexCoord2d(1, 1);
                 gl.glVertex3d(1, 1, 0);
                 gl.glTexCoord2d(0, 1);
                 gl.glVertex3d(0, 1, 0);
                 gl.glEnd();
                 //}
 
                 // Draw inhabitants.
                 gl.glPushMatrix();
                 gl.glPushAttrib(GL_CURRENT_BIT);
                 //empty.bind(gl);
                 Inhabitant inhabitant = map.getTile(i, j).getInhabitant();
                 gl.glPushMatrix();
                 // TODO: replace by more meaningful, non-glut objects.
                 gl.glTranslatef(0.5f, 0.5f, 0);
 
                 /*if (inhabitant instanceof LandCreature) {
                  gl.glColor3f(0, 1, 0);
                  //gl.glRotatef(90, 1, 0, 0);
                  //glut.glutSolidTeapot(0.5);
                  new GraphicalObjects(gl).drawCylinder(0.5f, 2);
                  } else */
 
                 if (inhabitant instanceof Food) {
                     gl.glColor3f(1, 1, 1);
                     //gl.glRotatef(90, 1, 0, 0);
                     //glut.glutSolidTeapot(0.5);
                     //new GraphicalObjects(gl).drawCylinder(0.5f, 2);
                     empty.bind(gl);
                     gl.glPushMatrix();
                     gl.glTranslated(-0.6, -0.5, 0);
                     setMaterial(Materials.GOLD);
                     gl.glCallList(FOOD);
                     setMaterial(Materials.WHITE);
                     gl.glPopMatrix();
                 }
                 gl.glPopMatrix();
 
                 gl.glPopAttrib();
                 gl.glPopMatrix();
 
                 // Move to the next column.
                 gl.glTranslatef(1, 0, 0);
             }
             //Move to the next row and back to the first column.
             gl.glTranslatef(-map.getHeight(), 1, 0);
         }
         gl.glPopMatrix();
 
         empty.bind(gl);
         //for (Player p : game) {
         for (int i = 0; i < game.getPlayers().length; i++) {
             Player p = game.getPlayers()[i];
             setMaterial(materials[i]);
             for (Creature c : p) {
                 if (c.getMoveCooldown() == 0) {
                     creatureViews.get(c).move(Creature.TICKS_PER_TILE_AVG * Game.TICK_TIME_IN_MS);
                 }
                 gl.glPushMatrix();
                 double angle = creatureViews.get(c).getCurrentAngle();
                 Vector currentLocation = creatureViews.get(c).getCurrentLocation();
                 Tile currentTile = c.getPath().getCurrentTile();
                 gl.glLoadName(currentTile.getY() * map.getHeight() + currentTile.getX() + 1);
                 //System.out.println(currentLocation);
                 if (c == currentCreature && !debug) {
                     gs.cnt = currentLocation;
                 }
                 if (c.isAlive()) {
                     gl.glTranslated(currentLocation.x(), currentLocation.y(), currentLocation.z());
                     drawBar((double) c.getLife() / Creature.MAX_LIFE, 1, false);
                     gl.glPushMatrix();
                     if (angle == -90 || angle == 180) {
                         gl.glTranslatef(0f, 1.0f, 0.0f);
                     }
                     if (angle == 90 || angle == 180) {
                         gl.glTranslatef(1f, 0.0f, 0.0f);
                     }
                     if (angle == 135f) {
                         gl.glTranslatef(1.2f, 0.5f, 0f);
                     }
                     if (angle == -135f) {
                         gl.glTranslatef(0.5f, 1.2f, 0f);
                     }
                     if (angle == 45f) {
                         gl.glTranslatef(0.6f, -0.25f, 0f);
                     }
                     if (angle == -45f) {
                         gl.glTranslatef(-0.25f, 0.5f, 0f);
                     }
                     gl.glRotatef((float) angle, 0f, 0f, 1f);
                     if (c instanceof LandCreature) {
                         gl.glCallList(LANDCREATURE);
                         gl.glPopMatrix();
                     } else if (c instanceof SeaCreature) {
                         gl.glCallList(SEACREATURE);
                         //Extra popmatrix, if-statement
                         gl.glPopMatrix();
                     } else if (c instanceof AirCreature) {
                         gl.glCallList(AIRCREATURE);
                         //Extra popmatrix, if-statement
                         gl.glPopMatrix();
                         drawBar((double) ((AirCreature) c).getEnergy() / AirCreature.MAX_ENERGY, 1.3, true);
                     }
                 }
                 gl.glPopMatrix();
 
             }
         }
     }
 
     private void drawMiniMap(int w) {
     int width = 1, height = 1;
 
         final double AR = (double) width / height;  // aspect ratio
         int h = (int) (w / AR); // height of the clock in pixels
     
     
         GameMap map = game.getMap();
         //Set Viewport
         gl.glViewport(gs.w-w, 0, w, h); // define a viewport for the clock
         gl.glClear(GL_DEPTH_BUFFER_BIT); // clear z buffer
         // Set projection matrix.
         gl.glMatrixMode(GL_PROJECTION);
         gl.glPushMatrix();
         gl.glLoadIdentity();
         gl.glMatrixMode(GL_MODELVIEW);
         gl.glPushMatrix();
         gl.glLoadIdentity();
         glu.gluLookAt(0.0, 1.0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0);
         gl.glDisable(GL_LIGHTING);
         gl.glMatrixMode(GL_PROJECTION);
         float t = (1.5f / (float) map.getHeight());
         gl.glTranslatef(-0.5f + t, -1f, 0f);
         for (int i = 0; i < map.getHeight(); i++) {
             for (int j = 0; j < map.getWidth(); j++) {
                 TileType type = map.getTile(i, j).getType();
                 gl.glColor3f(1, 1, 1);
                 Inhabitant inhabitant = map.getTile(i, j).getInhabitant();
                 if (inhabitant instanceof Creature && ((Creature) inhabitant).isAlive()) {
                     gl.glColor3f(1, 0, 0);
                     red.bind(gl);
                 } else {
                     switch (type) {
                         case DEEP_WATER:
                             deepWater.bind(gl);
                             break;
                         case SHALLOW_WATER:
                             shallowWater.bind(gl);
                             break;
                         case LAND:
                             land.bind(gl);
                             break;
 
                     }
                 }
                 gl.glBegin(GL_QUADS);
                 gl.glNormal3f(0, 0, t);
                 gl.glTexCoord2d(0, 0);
                 gl.glVertex3d(0, 0, 0);
                 gl.glTexCoord2d(1, 0);
                 gl.glVertex3d(t, 0, 0);
                 gl.glTexCoord2d(1, 1);
                 gl.glVertex3d(t, 0, t);
                 gl.glTexCoord2d(0, 1);
                 gl.glVertex3d(0, 0, t);
                 gl.glEnd();
                 gl.glTranslatef(t, 0f, 0f);
             }
             gl.glTranslatef(-1.5f, t, 0);
         }
 
         // Restore the original matrices.
         gl.glMatrixMode(GL_PROJECTION);
         gl.glPopMatrix();
         gl.glMatrixMode(GL_MODELVIEW);
         gl.glPopMatrix();
 
         gl.glViewport(0, 0, gs.w, gs.h); // restore viewport
         gl.glEnable(GL_LIGHTING); // re-enable lighting
     }
 
     private void drawAttackNotice(int w) {
         int width = 1, height = 1;
 
         final double AR = (double) width / height;  // aspect ratio
         int h = (int) (w / AR); // height of the clock in pixels
         gl.glViewport(0, 0, w, h); // define a viewport for the clock
 
         // Set projection matrix to display the clock with the correct size.
         gl.glMatrixMode(GL_PROJECTION);
         gl.glPushMatrix();
         gl.glLoadIdentity();
 
         gl.glOrtho(0.0f, width, height, 0.0f, 0.0f, 0.01f);
 
         setMaterial(Materials.WHITE);
         warning.bind(gl);
         // Draw the clock.
         gl.glMatrixMode(GL_MODELVIEW);
         gl.glPushMatrix();
         gl.glLoadIdentity();
         gl.glBegin(GL_QUADS);
         gl.glTexCoord2f(0, 0);
         gl.glVertex2f(0, 0);
         gl.glTexCoord2f(0, 1);
         gl.glVertex2f(0, 1);
         gl.glTexCoord2f(1, 1);
         gl.glVertex2f(1, 1);
         gl.glTexCoord2f(1, 0);
         gl.glVertex2f(1, 0);
         gl.glEnd();
 
         // Restore the original projection and modelview matrices.
         gl.glMatrixMode(GL_PROJECTION);
         gl.glPopMatrix();
         gl.glMatrixMode(GL_MODELVIEW);
         gl.glPopMatrix();
 
         gl.glViewport(0, 0, gs.w, gs.h); // restore viewport
     }
 
     private void handleMouseClick(int x, int y) {
         y = gs.h - y;
         int buffsize = 64;
         IntBuffer buff = IntBuffer.allocate(buffsize);
         gl.glSelectBuffer(buffsize, buff);
         IntBuffer view = IntBuffer.allocate(4);
         gl.glGetIntegerv(GL_VIEWPORT, view);
         gl.glRenderMode(GL_SELECT);
         gl.glInitNames();
         gl.glPushName(0);
         gl.glMatrixMode(GL_PROJECTION);
 
         gl.glPushMatrix();
         gl.glLoadIdentity();
         glu.gluPickMatrix(x, y, 1.0, 1.0, view);
        float height = gs.vWidth / (gs.w / gs.h);
         gl.glOrtho(-0.5 * gs.vWidth, 0.5 * gs.vWidth, -0.5 * height, 0.5 * height, 0.1, 1000);
         gl.glMatrixMode(GL_MODELVIEW);
         gl.glPushMatrix();
         draw();
         gl.glPopMatrix();
         gl.glMatrixMode(GL_PROJECTION);
         gl.glPopMatrix();
 
         int hits = gl.glRenderMode(GL_RENDER);
         int clickcode = 0;
 
         //get last element (N.B. usually i=3 is the actual correct value)
         for (int i = buffsize - 1; i >= 0; i--) {
             if (buff.get(i) != 0) {
                 clickcode = buff.get(i);
                 break;
             }
         }
         int h = game.getMap().getHeight();
         clicki = hits > 0 ? (clickcode - 1) / h : -1;
         clickj = hits > 0 ? (clickcode - 1) % h : -1;
 
         gl.glMatrixMode(GL_MODELVIEW);
     }
 
     private void setMaterial(float[] material) {
         gl.glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, material, 0);
         gl.glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, material, 4);
         gl.glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, material, 8);
         gl.glMaterialfv(GL_FRONT_AND_BACK, GL_SHININESS, material, 12);
     }
 
     private void drawBar(double life, double height, boolean yellow) {
         gl.glPushMatrix();
         red.disable(gl); // disable texture
         gl.glTranslated(0, 1, height);
         gl.glRotated(-45, 0, 0, 1);
         gl.glRotated(90, 1, 0, 0);
         gl.glTranslated((sqrt(2) - 1) / 2, 0, 0);
         HealthBar.draw(gl, life, 0.25, yellow);
         red.enable(gl); // enable texture
         gl.glPopMatrix();
     }
 
     private final class ClickListener implements MouseListener {
 
         int x = -1, y = -1;
 
         @Override
         public void mouseClicked(MouseEvent e) {
             if (e.getButton() == MouseEvent.BUTTON1) {
                 x = e.getX();
                 y = e.getY();
             }
         }
 
         @Override
         public void mousePressed(MouseEvent e) {
         }
 
         @Override
         public void mouseReleased(MouseEvent e) {
         }
 
         @Override
         public void mouseEntered(MouseEvent e) {
         }
 
         @Override
         public void mouseExited(MouseEvent e) {
         }
     }
 
     private final class NumberKeysListener implements KeyListener {
 
         @Override
         public void keyTyped(KeyEvent e) {
         }
 
         @Override
         public void keyPressed(KeyEvent e) {
             if (Character.isDigit(e.getKeyChar())) {
                 int parseInt = Integer.parseInt(e.getKeyChar() + "");
                 if (parseInt > 0 && parseInt <= player.getCreatures().length) {
                     currentCreature = player.getCreatures()[parseInt - 1];
                 }
             }
         }
 
         @Override
         public void keyReleased(KeyEvent e) {
         }
     }
 
     public static void main(String args[]) {
         new GUI();
     }
 
     public static class Materials {
         // Array containing parameters for a green plastic material. 
 
         public final static float[] GREEN_PLASTIC = {
             0.0f, 0.0f, 0.0f, 1.0f, //ambient
             0.1f, 0.35f, 0.1f, 1.0f, //diffuse
             0.45f, 0.55f, 0.45f, 1.0f, //specular
             32f //shininess
         };
         //Array containing parameteres for a yellow plastic materail.
         public final static float[] YELLOW_PLASTIC = {
             0.0f, 0.0f, 0.0f, 1.0f, //ambient
             0.5f, 0.5f, 0.0f, 1.0f, //diffuse
             0.60f, 0.60f, 0.50f, 1.0f, //specular
             32f //shininess
         };
         // Array containing parameteres for a red plastic material.
         public final static float[] RED_PLASTIC = {
             0.0f, 0.0f, 0.0f, 1.0f, //ambient
             1.0f, 0f, 0.0f, 1.0f, //diffuse
             0.60f, 0.60f, 0.50f, 1.0f, //specular
             32f //shininess
         };
         // Array containing parameters for a blue plastci material.
         public final static float[] BLUE_PLASTIC = {
             0.0f, 0.0f, 0.0f, 1.0f, //ambient
             0f, 0.5f, 1.0f, 1.0f, //diffuse
             0.60f, 0.60f, 0.50f, 1.0f, //specular
             32f //shininess
         };
         // Array containing parameters for an orange plastic material.
         public final static float[] ORANGE_PLASTIC = {
             0.0f, 0.0f, 0.0f, 1.0f, //ambient
             1f, 0.65f, 0.0f, 1.0f, //diffuse
             0.5f, 0.5f, 0.5f, 1.0f, //specular
             90f //shininess
         };
         // Array containing parameters  for a brown plastic material.
         public final static float[] BROWN_PLASTIC = {
             0.0f, 0.0f, 0.0f, 1.0f, //ambient
             0.36f, 0.2f, 0.01f, 1.0f, //diffuse
             0.5f, 0.5f, 0.5f, 1.0f, //specular
             0f //shininess
         };
         // Array containing parameters  for a gold material.
         public final static float[] GOLD = {
             0.24725f, 0.1995f, 0.0745f, 1.0f, //ambient
             0.75164f, 0.60648f, 0.22648f, 1.0f, //diffuse
             0.628281f, 0.555802f, 0.366065f, 1.0f, //specular
             51.2f //shininess
         };
         // Array containing parameters for a white material (for textures).
         public static float[] WHITE = {
             0.0f, 0.0f, 0.0f, 1.0f, //ambient
             1f, 1f, 1f, 1.0f, //diffuse
             1f, 1f, 1f, 1.0f, //specular
             0f //shininess
         };
     }
 }
