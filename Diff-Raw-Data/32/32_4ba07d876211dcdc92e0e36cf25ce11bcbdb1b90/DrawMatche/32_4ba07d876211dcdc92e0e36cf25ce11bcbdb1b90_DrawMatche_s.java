 package Graph;
 
 import GameConfig.GameConfig;
 import run.GameSession;
 import run.MainFrame;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: User
  * Date: 20.08.13
  * Time: 15:42
  * To change this template use File | Settings | File Templates.
  */
 public class DrawMatche extends JComponent implements ActionListener {
 
     /** If sets <b>TRUE</b> this component is draggable */
     private boolean draggable = true;
     /** 2D Point representing the coordinate where mouse is, relative parent container */
     protected Point anchorPoint;
     /** Default mouse cursor for dragging action */
     protected Cursor draggingCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
     /** If sets <b>TRUE</b> when dragging component,
      it will be painted over each other (z-Buffer change) */
     protected boolean overbearing = false;
     private TexturePaint texture;
     private boolean active = true;
     private GameSession session = GameSession.getInstance();
     private Integer id;
     private Integer rotateDegree;
     /** Basic Timer animation info */
     final static int CYCLE_TIME = 2000; // One cycle takes 2 seconds
     int currentResolution = 50; // current Timer resolution
     Timer timer = null; // animation Timer
     long cycleStart; // track start time for each cycle
     boolean linear = true; // (l) linear vs. non-linear motion
     private int moveX;
     private int moveY;
     private int moveMinX;
     private int moveMinY;
     private String color;
 
     public Integer getId(){
         return this.id;
     }
 
     public DrawMatche(Integer id) {
         this.id = id;
         addDragListeners();
         setOpaque(true);
         setBackground(new Color(45, 46, 49));
         BufferedImage randomTexture = this.loadImages();
         this.texture = new TexturePaint(randomTexture, new Rectangle(0, 0, GameConfig.barXSize, GameConfig.barYSize));
         Random r = new Random();
         rotateDegree = Math.abs(r.nextInt(20));
     }
 
     public void setBeginCoordinates(int x, int y){
         this.moveMinX = x;
         this.moveMinY = y;
     }
 
     @Override
     public void paintComponent(Graphics g) {
 
         super.paintComponent(g);
         doDrawing(g);
     }
 
     private void doDrawing(Graphics g) {
 
         Graphics2D g2d = (Graphics2D) g;
         g2d.rotate(Math.toRadians(rotateDegree-10));
         g2d.setPaint(this.texture);
         g2d.fillRoundRect(0, GameConfig.barRotateSize/2, GameConfig.barXSize, GameConfig.barYSize, 5, 5);
     }
 
     /**
      * Add Mouse Motion Listener with drag function
      */
     private void addDragListeners() {
         /** This handle is a reference to THIS because in next Mouse Adapter
          "this" is not allowed */
         final DrawMatche handle = this;
         addMouseMotionListener(new MouseAdapter() {
 
             @Override
             public void mouseMoved(MouseEvent e) {
                 anchorPoint = e.getPoint();
                 GameSession.User user = session.getCurrentUser();
                 Integer count = user.getCount();
                 if(count <= 0 || user.getType().equals("robot") || session.getButton().is_new()) return;
                 setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
             }
 
             @Override
             public void mouseDragged(MouseEvent e) {
                 int anchorX = anchorPoint.x;
                 int anchorY = anchorPoint.y;
                 GameSession.User user = session.getCurrentUser();
                 Integer count = user.getCount();
                 if(count <= 0 || user.getType().equals("robot") || session.getButton().is_new()) return;
 
                 Point parentOnScreen = getParent().getLocationOnScreen();
                 Point mouseOnScreen = e.getLocationOnScreen();
                 Point position = new Point(mouseOnScreen.x - parentOnScreen.x -
                         anchorX, mouseOnScreen.y - parentOnScreen.y - anchorY);
                 setLocation(position);
 
                 //Change Z-Buffer if it is "overbearing"
                 if (overbearing) {
                     getParent().setComponentZOrder(handle, 0);
                     repaint();
                 }
 
                 removeThisMatche();
             }
         });
     }
 
     private void removeThisMatche(){
         GameSession.User user = session.getCurrentUser();
         String color = user.getColor();
         Map basketCoordinates = GameConfig.basketCoordinates.get(color);
         Integer boxX = (Integer) basketCoordinates.get("x");
         Integer boxY = (Integer) basketCoordinates.get("y");
         Integer basketWith = (Integer) basketCoordinates.get("xSize");
 
         if(((color.equals("white") && getLocation().getX() > boxX)|| (color.equals("black") && getLocation().getX() < (boxX + basketWith))) && getLocation().getY() > boxY){
             active = false;
             removeDragListeners();
            session.remooveMatche(id);
            user.updateCount();
             CountDisplay counter = user.getCounter();
             counter.setCount(user.getType(), user.getCount());
             counter.repaint();
         }
         if(session.getAviableMatches()<=0){
             ShowTablo tablo = session.getShowTablo();
             tablo.setWinners();
             ChangeUserButton button = session.getButton();
             button.setText("игра окончена");
             button.setInactive();
             button.repaint();
             tablo.repaint();
         }
     }
 
     public boolean getActive(){
         return active;
     }
 
     private BufferedImage loadImages() {
 
         try {
             Random r = new Random();
             Integer num = Math.abs(r.nextInt(4) + 1);
             String name = GameConfig.textureNames.get(num);
             BufferedImage image = ImageIO.read(this.getClass().getResource(name));
             return image;
         } catch (IOException ex) {
             Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
             return null;
         }
     }
 
     public void removeDragListeners() {
         for (MouseMotionListener listener : this.getMouseMotionListeners()) {
             removeMouseMotionListener(listener);
         }
         setCursor(Cursor.getDefaultCursor());
     }
 
     /**
      * Get the value of overbearing
      *
      * @return the value of overbearing
      */
     public boolean isOverbearing() {
         return overbearing;
     }
 
     /**
      * Set the value of overbearing
      *
      * @param overbearing new value of overbearing
      */
     public void setOverbearing(boolean overbearing) {
         this.overbearing = overbearing;
     }
 
     /** Creates a new instance of SmoothAnimation */
     public void move(String color) {
         this.color = color;
         this.moveX = this.moveMinX;
         this.moveY = this.moveMinY;
         cycleStart = System.nanoTime() / 1000000;
         startTimer(currentResolution);
     }
 
     /**
      * This method handles the events from the Swing Timer
      */
     public void actionPerformed(ActionEvent ae) {
         // calculate the fraction elapsed of the animation and call animate()
         // to alter the values accordingly
         long currentTime = System.nanoTime() / 1000000;
         long totalTime = currentTime - cycleStart;
         if (totalTime > CYCLE_TIME) {
             cycleStart = currentTime;
         }
         float fraction = (float) totalTime / CYCLE_TIME;
         fraction = Math.min(1.0f, fraction);
         fraction = 1 - Math.abs(1 - (2 * fraction));
         animate(fraction);
     }
 
     /**
      * Animate the opacity and location factors, according to the current
      * fraction.
      */
     public void animate(float fraction) {
 
         System.out.println("in animate" + moveX);
         if(moveX > GameConfig.windowXSize || moveY > (GameConfig.leftBoxY + GameConfig.barYSize/2) || moveX < GameConfig.leftBoxX/2 || moveY<0 ){
             timer.stop();
             removeThisMatche();
             return;
         }
         float animationFactor;
         if (linear) {
             animationFactor = fraction;
         } else {
             // Our "nonlinear" motion just uses a sin function to get a
             // simple bounce behavior
             animationFactor = (float) Math.sin(fraction * (float) Math.PI / 2);
         }
         // Clamp the value to make sure it does not exceed the bounds
         animationFactor = Math.min(animationFactor, 1.0f);
         animationFactor = Math.max(animationFactor, 0.0f);
         Map basketCoords = GameConfig.basketCoordinates.get(this.color);
         System.out.println("user id is " + session.getCurrentUser().getId());
         moveX = moveX + (int) (.5f + animationFactor * (float) ((Integer)basketCoords.get("x") - moveMinX));
         moveY = moveY + (int) (.5f + animationFactor * (float) ((Integer)basketCoords.get("y") - moveMinY));
         System.out.println("select matches=" + moveX);
         Point position = new Point(moveX, moveY);
         setLocation(position);
         // The move animation will calculate a location based on a linear
         // interpolation between its start and end points using the fraction
         // redisplay our component with the new animated values
         repaint();
     }
 
     /**
      * Moves the frame rate up or down by changing the Timer resolution
      */
     private void changeResolution(boolean faster) {
         if (faster) {
             currentResolution -= 5;
         } else {
             currentResolution += 5;
         }
         currentResolution = Math.max(currentResolution, 0);
         currentResolution = Math.min(currentResolution, 500);
         startTimer(currentResolution);
     }
 
     /**
      * Starts the animation
      */
     private void startTimer(int resolution) {
         if (timer != null) {
             timer.stop();
             timer.setDelay(resolution);
         } else {
             timer = new Timer(resolution, this);
         }
         timer.start();
     }
 }
