 package ogo.spec.game.graphics.view;
 
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
 
 public class GUI extends Base {
 
     Game game;
     ClickListener clickListener;
     KeyListener keyListener;
     int clicki = -1, clickj = -1;
     Player player;
     Vector vViewChange = null;
     Creature currentCreature;
     Timer timer = new Timer(30);
     Map<Creature, CreatureView> creatureViews = new HashMap<Creature, CreatureView>();
     Wavefront landCreature, seaCreature, airCreature;
 
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
         AirCreature a = new AirCreature(map.getTile(0, 0), map);
         SeaCreature s = new SeaCreature(map.getTile(2, 2), map);
         map.getTile(0, 0).setInhabitant(a);
         map.getTile(1, 1).setInhabitant(new Food());
         map.getTile(2, 2).setInhabitant(s);
 
         Player p1 = new Player("1");
         Creature[] p1c = {a, s};
         p1.setCreatures(p1c);
         currentCreature = a;
         Player p2 = new Player("2");
         Creature[] p2c = {};
         p2.setCreatures(p2c);
         player = p1;
         Player[] players = new Player[2];
         players[0] = p1;
         players[1] = p2;
         try {
             game = new Game(players, map);
         } catch (Exception ex) {
             Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         creatureViews = new HashMap<Creature, CreatureView>();
         for (Player p : game) {
             for (Creature c : p) {
                 CreatureView creatureView = new CreatureView(c, timer);
                 creatureViews.put(c, creatureView);
             }
         }
         landCreature = new Wavefront();
         seaCreature = new Wavefront();
         airCreature = new Wavefront();
         String path = "src/ogo/spec/game/graphics/models/";
         try {
             landCreature.readWavefront(path + "land.obj", gl);
         } catch (FileNotFoundException ex) {
             Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
         }
         try {
             seaCreature.readWavefront(path + "sea.obj", gl);
         } catch (FileNotFoundException ex) {
             Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
         }
         try {
             airCreature.readWavefront(path + "air.obj", gl);
         } catch (FileNotFoundException ex) {
             Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         new Thread(timer).start();
         game.start();
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
         float height = gs.vWidth / (gs.w / gs.h);
         gl.glOrtho(-0.5 * gs.vWidth, 0.5 * gs.vWidth, -0.5 * height, 0.5 * height, 0.1, 1000);
         // Set camera.
         gl.glMatrixMode(GL_MODELVIEW);
         gl.glLoadIdentity();
 
 
         Vector dir = new Vector(cos(gs.phi) * cos(gs.theta),
                 sin(gs.phi) * cos(gs.theta),
                 sin(gs.theta));
 
         Vector eye = gs.cnt.add(dir.scale(gs.vDist));
 
         glu.gluLookAt(-40f, -40f, 30f, // eye point
                 gs.cnt.x(), gs.cnt.y(), gs.cnt.z(), // center point
                 0.0, 0.0, 1.0);   // up axis
 
 
         //glu.gluLookAt(eye.x(), eye.y(), eye.z(), // eye point
         //        gs.cnt.x(), gs.cnt.y(), gs.cnt.z(), // center point
         //        0, 0, 1); // up axis
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
            if (currentCreature.getPath() != null && clickListener.x != -1) {
                 currentCreature.select(game.getMap().getTile(clickj, clicki));
                 creatureViews.get(currentCreature).move(1000);
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
     }
 
     private void draw() {
         // Background color.
         gl.glClearColor(1f, 1f, 1f, 0f);
 
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
 
         bananad.bind(gl);
         bananan.bind(gl);
         bananas.bind(gl);
 
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
                     new GraphicalObjects(gl).drawCylinder(0.5f, 2);
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
 
         for (Player p : game) {
             for (Creature c : p) {
                 gl.glPushMatrix();
                 Vector currentLocation = creatureViews.get(c).getCurrentLocation();
                 gl.glTranslated(currentLocation.x(), currentLocation.y(), currentLocation.z());
                 //System.out.println(currentLocation);
                 if (c == currentCreature) {
                     gs.cnt = currentLocation;
                 }
                 //new GraphicalObjects(gl).drawCylinder(0.5f, 2);
                 if (c.getLife() > 0) {
                     if (c instanceof LandCreature) {
                     landCreature.drawTriangles();} else if (c instanceof SeaCreature) {
                         seaCreature.drawTriangles();} else if (c instanceof AirCreature) {
                             airCreature.drawTriangles();
                     }
                 }
                 gl.glPopMatrix();
 
             }
         }
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
 
     private final class ClickListener implements MouseListener {
 
         int x = -1, y = -1;
 
         @Override
         public void mouseClicked(MouseEvent e) {
             x = e.getX();
             y = e.getY();
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
 }
