 package summative2013;
 
 import java.awt.GraphicsEnvironment;
 import java.awt.GraphicsDevice;
 import java.awt.Graphics;
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import summative2013.lifeform.Lifeform;
 import summative2013.phenomena.Weather;
 
 public class Summative extends JPanel implements KeyListener {
 
     private static GraphicsEnvironment ge;
     private static GraphicsDevice gd;
     private HashMap<Point, Lifeform> locToLife;
     private HashMap<Point, TERRAIN> locToTerrain;
     private final Object lock = new Object();
     private ArrayList<Weather> activeWeather;
     private static JFrame frame;
     private Rectangle screen;
     private boolean upPressed = false, downPressed = false, rightPressed = false, leftPressed = false;
 
     /**
      * Default constructor, Generates a Summative object that is the size of the
      * screen, with a map to fill it
      */
     public Summative() {
         Lifeform.summative = this;//sets the panel for all of the lifeforms to be this
         locToLife = new HashMap<Point, Lifeform>();//initializes our point, lifeform hashmap
         locToTerrain = new HashMap<Point, TERRAIN>();//initializes our point, terrain hashmap
         setSize(gd.getFullScreenWindow().getSize());//fullscreen the panel
         screen = new Rectangle(-1 * getWidth() / 20, -1 * getHeight() / 20, getWidth()/10, getHeight()/10);//sets up our screen rectangle to define our screen
        for (int i =screen.x; i <= screen.x+screen.width; i++) {
            for (int j =screen.y; j <= screen.y+screen.height; j++) {
                 mapGen(new Point(i, j));//generate initial point
             }
         }
        repaint();
         setFocusable(true);//allows us to actually use the keylistener
         addKeyListener(this);//makes keys do something
     }
 
     /**
      * Types of terrain available
      */
     public enum TERRAIN {
 
         LAND, SEA
     };
 
     /**
      * The Driver method to run the program
      *
      * @param args The command line parameters
      */
     public static void main(String[] args) {
         frame = new JFrame();//initializes our frame
 
         frame.setResizable(false);//can't resize, about to be fullscreen
         frame.setUndecorated(true);//no border
         frame.setLayout(null);//no layout, no components
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//stops the program when we close
 
         ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
         gd = ge.getDefaultScreenDevice();
         gd.setFullScreenWindow(frame);//makes full screen
         Summative s = new Summative();
         frame.add(s);
         s.locToTerrain.put(new Point(0, 0), TERRAIN.LAND);
         s.doMapGen(new Point(0, 0), 10);
         s.repaint();
         s.requestFocusInWindow();//keyListener activated
         frame.setVisible(true);
     }
 
     /**
      * Generates a map that radiates out from an initial point, not used anymore
      *
      * @param p The point to centre the generation around
      * @param i The number of iterations
      */
     public void doMapGen(Point p, int i) {
         if (i >= 0) {
             for (int x = p.x - 1; x <= p.x + 1; x++) {
                 for (int y = p.y - 1; y <= p.y + 1; y++) {
                     Point genPoint = new Point(x, y);
                     if ((x != p.x || y != p.y) && !locToTerrain.containsKey(new Point(x, y))) {//loop through points around
                         mapGen(genPoint);//fit that point
                     }
                     doMapGen(genPoint, i - 1);//radiate out
                 }
 
             }
         }
     }
 
     /**
      * Generates what type of land should be at the point
      *
      * @param point The point in which we want to generate some terrain
      */
     public void mapGen(Point point) {
         int lands = 0, seas = 0;//count adjacent land, sea
         for (int x = -1; x <= 1; x++) {
             for (int y = -1; y <= 1; y++) {
                 synchronized (lock) {
                     if (locToTerrain.get(new Point(point.x + x, point.y + y)) == TERRAIN.LAND) {
                         lands++;//add a land for each land within the 3x3 surrounding the block
                     } else if (locToTerrain.get(new Point(point.x + x, point.y + y)) == TERRAIN.SEA) {
                         seas++;//add a sea for each sea within the 3x3 surrounding block
                     }
                 }
             }
         }
         //Only land, have a small chance to generate a sea block
         if (seas == 0) {
             if (Math.random() < .95) {
                 locToTerrain.put(point, TERRAIN.LAND);
             } else {
                 locToTerrain.put(point, TERRAIN.SEA);
             }
             //Mostly land, but some sea, generate more sea
         } else if (lands > seas && seas > 0) {
             if (Math.random() < .95) {
                 locToTerrain.put(point, TERRAIN.SEA);
             } else {
                 locToTerrain.put(point, TERRAIN.LAND);
             }
             //Mostly sea, but some land, generate more
         } else if (seas > lands && lands > 0) {
             if (Math.random() < 0.95) {
                 locToTerrain.put(point, TERRAIN.LAND);
             } else {
                 locToTerrain.put(point, TERRAIN.SEA);
             }
         } else {
             if (Math.random() < 0.5) {
                 locToTerrain.put(point, TERRAIN.LAND);
             } else {
                 locToTerrain.put(point, TERRAIN.SEA);
             }
         }
     }
     //moves the whole system ahead
 
     public void advance() {
         for(Weather w:activeWeather){
             Iterator lifeIt = locToLife.entrySet().iterator();
             while (lifeIt.hasNext()) {
 
                 Map.Entry<Point, Lifeform> pairs = (Map.Entry) lifeIt.next();
 
                 if (w.getArea().contains(pairs.getKey())) {//if we are on the weather
                     pairs.getValue().act(w.getType());//act based on weather
                 }
             }
             Iterator terIt = locToTerrain.entrySet().iterator();
             HashMap<Point,TERRAIN> temp = new HashMap<Point,TERRAIN>();
             while(terIt.hasNext()){
                 Map.Entry<Point, TERRAIN> pairs = (Map.Entry)terIt.next();
                 if(w.getArea().contains(pairs.getKey())){
                     if(w.getType() == Weather.WEATHER.RAIN&&pairs.getValue() == TERRAIN.SEA){
                         //radiate water
                         Point[] points = {new Point(pairs.getKey().x,pairs.getKey().y), new Point (pairs.getKey().x-1,pairs.getKey().y), new Point(pairs.getKey().x+1,pairs.getKey().y),new Point(pairs.getKey().x,pairs.getKey().y-1),new Point(pairs.getKey().x,pairs.getKey().y+1)};
                         for(Point p:points){
                             temp.put(p,TERRAIN.SEA);
                         }
                     }
                     if(w.getType() == Weather.WEATHER.SUN &&pairs.getValue() == TERRAIN.LAND){
                         //radiate land
                         Point[] points = {new Point(pairs.getKey().x,pairs.getKey().y), new Point (pairs.getKey().x-1,pairs.getKey().y), new Point(pairs.getKey().x+1,pairs.getKey().y),new Point(pairs.getKey().x,pairs.getKey().y-1),new Point(pairs.getKey().x,pairs.getKey().y+1)};
                         for(Point p:points){
                             temp.put(p,TERRAIN.LAND);
                         }
                     }
                 }
             }
             for(Map.Entry<Point, TERRAIN> m:temp.entrySet()){
                 locToTerrain.put(m.getKey(),m.getValue());
             }
         }
     }
 
     /**
      * draws the graphics on the screen
      *
      * @param g The graphics object needed
      */
     @Override
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
         drawTerrain(g);//draws the terrain of the map
     }
 
     /**
      * Draws terrain, Green for land, Blue for sea
      *
      * @param g The graphics object to draw on
      */
     public void drawTerrain(Graphics g) {
         synchronized (lock) {
             for(int i = screen.x;i<screen.x+screen.width;i++){
                 for(int j = screen.y;j<screen.y+screen.height;j++){
                     if(locToTerrain.get(new Point(i,j))==TERRAIN.LAND)//if land, draw green
                         g.setColor(Color.GREEN);
                     else if (locToTerrain.get(new Point(i,j))==TERRAIN.SEA)//if sea draw blue
                         g.setColor(Color.BLUE);
                     g.fillRect((i - screen.x)*10,(j - screen.y)*10,10,10);//draw the block
                 }
             }
         }
     }
 
     //The lifeforms call this to kill themselves
     public void assistedSuicide(Point location) {
         locToLife.remove(location);
     }
 
     //Sends over the object in the hashmap
     public Lifeform lifeGet(Point location) {
         return locToLife.get(location);
     }
 
     //Sends over the object in the hashmap
     public TERRAIN terrainGet(Point location) {
         return locToTerrain.get(location);
     }
 
     //Adds in a new baby animal kinda thing
     public void add(Point p, Lifeform l) {
         locToLife.put(p, l);
         l.setLocation(p);
     }
 
     /**
      * Unused method
      *
      * @param e KeyEvent fired as a type event
      */
     @Override
     public void keyTyped(KeyEvent e) {
         e.consume();
     }
 
     /**
      * Checks which of the arrow keys have been hit and moves the perspective
      * accordingly
      *
      * @param e
      */
     @Override
     public void keyPressed(KeyEvent e) {
         //System.out.println("keyPressed");
         int keyCode = e.getKeyCode();
         if (keyCode == KeyEvent.VK_RIGHT) {
             rightPressed = true;//check off that right has been pressed
         } else if (keyCode == KeyEvent.VK_LEFT) {
             leftPressed = true;//check off that left has been pressed
         } else if (keyCode == KeyEvent.VK_UP) {
             upPressed = true;//check off that up has been pressed
         } else if (keyCode == KeyEvent.VK_DOWN) {
             downPressed = true;//check off that down has been pressed
         } else if (keyCode == KeyEvent.VK_ESCAPE){
             frame.dispose();
         }
         if (rightPressed) {
             moveRight();
         }
         if (leftPressed) {
             moveLeft();
         }
         if (upPressed) {
             moveUp();
         }
         if (downPressed) {
             moveDown();
         }
         updateMap();//generates map as needed
         repaint();
 
     }
 
     /**
      * Stores when the arrow keys have been released
      *
      * @param e The KeyEvent fired
      */
     @Override
     public void keyReleased(KeyEvent e) {
         int keyCode = e.getKeyCode();
         if (keyCode == KeyEvent.VK_RIGHT)//right isn't pressed anymore
         {
             rightPressed = false;
         }
         if (keyCode == KeyEvent.VK_LEFT)//left isn't pressed anymore
         {
             leftPressed = false;
         }
         if (keyCode == KeyEvent.VK_UP)//up isn't pressed anymore
         {
             upPressed = false;
         }
         if (keyCode == KeyEvent.VK_DOWN)//down isn't pressed anymore
         {
             downPressed = false;
         }
     }
 
     /**
      * Generates more map as it becomes visible to the user
      */
     public void updateMap(){
         for(int i = screen.x;i<=screen.x+screen.width;i++){//runs through x values
             for(int j = screen.y;j<=screen.y+screen.height;j++){//runs through y values
                 if(!locToTerrain.containsKey(new Point(i,j)))//do we have a point there?
                     mapGen(new Point(i,j));//create point in that map
             }
         }
     }
     /**
      * Moves the perspective towards the right
      */
     public void moveRight() {
         screen.translate(1, 0);//moves the perspective
     }
     /**
      * Moves the perspective to the left
      */
     public void moveLeft() {
         screen.translate(-1, 0);//moves "camera"
     }
     /**
      * Moves the perspective upwards
      */
     public void moveUp() {
         screen.translate(0, -1);//moves viewpoint
     }
     /**
      * Moves the perspective downward
      */
     public void moveDown() {
         screen.translate(0, 1);//moves centre of field of view
     }
 }
