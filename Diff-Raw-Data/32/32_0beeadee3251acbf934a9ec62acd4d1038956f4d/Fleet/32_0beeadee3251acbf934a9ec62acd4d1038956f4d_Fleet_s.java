 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import javax.imageio.ImageIO;
 
 /**
  * The Fleet class is constructed with its starting x and y co-ordinates, as well
  * those of where it is meant to end.
  */
 /**
  * Edited by KM
  */
 public class Fleet extends GamePiece{
 
     private int startX;
     private int startY;
     private int destinationX;
     private int destinationY;
     private int ships;
     
     private double xDouble,yDouble;
     
     /**
      * @return the starting 'x' co-ordinate of the fleet
      */
     public int getStartX(){
         return this.startX;
     }
     
     /**
      * @return the starting 'y' co-ordinate of the fleet
      */
     public int getStartY(){
         return this.startY;
     }
     
     /**
      * @return the 'x' co-ordinate of the fleets destination
      */
     public int getDestinationX(){
         return this.destinationX;
     }
     
     /**
      * @return the 'y' co-ordinate of the fleets destination
      */
     public int getDestinationY(){
         return this.destinationY;
     }
     
     /**
      * @return the frame
      */
     public double getFrame(){
     	return (double) this.frame;
     }
     
     private BufferedImage bufferedImage;
     private int clickRadius = 10; // 10px click radius
     double speed = 100; // arbitrary speed value
     
     
     //Distance between the planets and the time it'll take the fleet.
     double distance, time,frame; // frame is the percentage of the entire distance travelled per frame
     //Game Frames Per Second... would rather have this in a higher order class than this!
    public static int GAME_FPS = 120;
     
     
     public Fleet(int currentX, int currentY, int destinationX, int destinationY){
         this.x = currentX;
         this.y = currentY;
         
         xDouble=currentX;
         yDouble=currentY;
         
         this.startX = currentX;
         this.startY = currentY;
         
         this.destinationX = destinationX;
         this.destinationY = destinationY;
         
         
         //Calculates distance between planets.
        distance=Math.sqrt((this.destinationX-startX)*(this.destinationX-startY)+(this.destinationX-startY)*(this.destinationY-startY));
         //Calculates time by S/v and then takes what portion of the distance it must pass in 1 frame.
         time=distance/speed;
        frame=1/(GAME_FPS*time);
         
         BufferedImage b;
         try {
             File f = new File("resources/images/fleets/fleet1.png");
             b = ImageIO.read(f);
             bufferedImage=GamePiece.resize(b, 5);
             
         } catch (IOException e) {
         // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
     
     /**
      * @return a boolean stating whether or not the given coordinate is 'inside' the
      * fleet. Can be used for testing mouse-click-selection.
      */
     public boolean isClickInside(int x, int y){
         //Is the given coordinate's distance from the centre coordinate less than (or equal) the clickRadius?
         return (this.x-x)*(this.x-x)+(this.y-y)*(this.y-y)<=clickRadius*clickRadius;
     }
     
     /**
      * Draw the planet on the given Graphics2D object.
      */
     public void draw(Graphics2D g2d){
         g2d.drawImage(this.bufferedImage, this.getX(), this.getY(), null);
     }
     
     /**
      * Update the internal state of the fleet based on how much time has passed.
      * Do this to a fleet before you draw it on screen, otherwise it won't update!
      */
     public void update()//long nanosecondsPassed)
     {
         //Change X and Y coords as a function of the time passed since the last
         // update, and `this.speed`
         //NOTE: system.nanoTime() is not necessarily accurate to the real world, but *is*
         // internally consistent. Just FYI...
     	
     	//Made the speed a constant. It always changes with the same-ish distance per frame.
     	xDouble+=(double)(destinationX-startX)*frame;
         yDouble+=(double)(destinationY-startY)*frame;
     }
     
     public double getXDouble()
     {
      return xDouble;
     }
     public double getYDouble()
     {
      return yDouble;
     }
     
     public int getShips()
     {
      return ships;
     }
     
 }
