 import java.awt.Graphics2D;
 
 import java.awt.image.BufferedImage;
 import javax.imageio.ImageIO;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Random;
 
 import java.util.*;
 import java.awt.*;
 
 
 /**
  * The Planet class is constructed with the x and y coordinates, as well as a radius.
  */
 public class Planet extends GamePiece
 {
 
     //Center coordinates.
     private int xCenter,yCenter;
 
     public int radius;
     
     
     private int resources;
     private String name;
     private int attRadius;
     private Player owner;
     private Player capturer;
     private int persentage;
     
     HashMap<Player, Integer> ships= new HashMap<Player, Integer>();
     
         //Number of planet images we have.
     public static int imagesNum=18;
     //The planet images.
     public static BufferedImage[] images;
 
     //Min and Max radius
     public static int minRadius;
     public static int maxRadius;
 
 
     //Randomness.
     private static Random random=new Random();
 
     //Reads all the planet images from their image files.
     static
     {
         minRadius=Universe.getMinPlanetSize();
         maxRadius=Universe.getMaxPlanetSize();
 
         images=new BufferedImage [imagesNum];
         for(int i=1;i<=imagesNum;i++)
         {
             try
             {
                 images[i-1] = ImageIO.read(new File("resources/images/planets/planet"+i+".png"));
             }
             catch (IOException e)
             {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
     }
     public Planet(int x, int y)
     {
         this(x, y, Universe.randomBetween(minRadius,maxRadius));
     }
 
     public Planet(int x, int y, int radius)
     {
         this(x, y, radius, random.nextInt(imagesNum));
     }
 
     public Planet(int x, int y, int radius, int j)
     {
      owner=null;
      attRadius=radius*2;
 	 this.x = x;
 	 this.y = y;
 	 xCenter=x+radius;
 	 yCenter=y+radius;
 	 this.radius = radius;
 	 image=resize(images[j],radius);
     }
 
     public int getXCenter()
     {
         return xCenter; 
     }
     public int getYCenter()
     {
         return yCenter;
     }
 
 
     /**
      * @return the radius of the planet.
      */
     public int getRadius()
     {
         return radius;
     }
 
     /**
      * @return draw the planet.
      */
     /*public void draw(Graphics2D g2d)
     {
         g2d.drawImage(this.bufferedImage, this.getX(), this.getY(), null);
     }*/
 
 
     /* public void setImage(String path) {
         try {
             File f = new File(path);
             this.bufferedImage = ImageIO.read(f);
         } catch (IOException e) {
         // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }*/
 
     /**
      * @return a boolean stating whether or not the given coordinate is 'inside' the
      * planet. Can be used for testing mouse-click-selection.
      */
     
     public int getResourceValue()
     {
      return resources; 
     }
     public String getName()
     {
      return name;
     }
     public int travelRadius()
     {
      return attRadius;
     }
     public boolean coordinateWithinTravelRadius(int x, int y)
     {
      return (xCenter-x)*(xCenter-x)+(yCenter-x)*(yCenter-x)<=attRadius*attRadius;
     }
 
    public Player dominantPlayer()
     {
     return capturer;
     }
     public int percentCaptured()
     {
      return persentage;
     }
     public int getPlayerShips(Player p)
     {
      return ships.get(p);
     }
     
     public Player[] getPlayers()
     {
      Set<Player> a=ships.keySet();
      return (Player[])a.toArray();
     }
     
     public void addFleet(Fleet f)
     {
     	Player p=f.getPlayer();
     	int s=f.getShips();
     	if(ships.get(p)==null)ships.put(p,s);
     	else
     	{
     		int sh=ships.get(p)+s;
     		ships.put(p,sh);
     	}
     }
     
     public Color getColor()
     {
      return owner.getColor();
     }
     
     public void setPlayerShips(Player p, int n) // set the number of ships the given player has to be n
     {
      ships.put(p, n);
     }
     
     public boolean isFarEnoughAwayFrom(Planet p, int distance) 
     {
 
         int dx = xCenter - p.getXCenter();
         int dy = yCenter - p.getYCenter();
 
         return (dx*dx)+(dy*dy)>=(distance+radius+p.radius)*(distance+radius+p.radius);
     }
 
     public boolean isCoordinateInside(int x, int y)
     {
         //Is the coordinate's distance from the center coordinate less than (or equal) the radius?
         return (xCenter-x)*(xCenter-x)+(yCenter-y)*(yCenter-y)<=radius*radius;
     }
     
     private void setPercentCaptured(int i)
     {
      persentage=i;
     }
 
 
     /**
      * perform all battle, capture, or regeneration actions applicable to the planet
      */
     public void update() {
 
         // TODO: check whether a battle is being fought
         // TODO: check whether the planet is being captured
         // TODO: check whether the planet is generating ships
     }
 
 
     /**
      *  Update the ship counts due to battle, for one cycle.
      * 
      *  Each ship has a 0.5 chance of destroying an enemy ship/dealing one unit of damage.
      *  Damage is equally between players, simultaneously, every time this is generated.
      *  
      *  1 unit of damage ~= 1 enemy ship destroyed (rounded)
      */
     private void performBattle() {
 
         int[] damageDealtByPlayers = new int[this.getPlayers().length];
 
         // calculate the amount of damage/ships each player does in total  
         for (int playerNo=0; playerNo < this.getPlayers().length; playerNo++) {
             Player p = this.getPlayers()[playerNo];
 
             for (int i=0; i < this.getPlayerShips(p); i++) {
                 if (Planet.random.nextBoolean()) damageDealtByPlayers[playerNo] += 1;
             }
         }
 
         // for every player that isn't itself, set the damage dealt to it to be (damage done by other players / (total number of players - itself) ) 
         int[] damageDealtToPlayers = new int[this.getPlayers().length];
         for (int player=0; player < this.getPlayers().length; player++) {
             for (int playerDoingDamage=0; playerDoingDamage < this.getPlayers().length; playerDoingDamage++) {
                 if (playerDoingDamage != player) {
                     damageDealtToPlayers[player] += (int) (damageDealtByPlayers[playerDoingDamage] / (this.getPlayers().length - 1) );
                 }
             }
         }
 
         // set number of ships for every player to be last number - damage just done to player
         for (int i=0; i < this.getPlayers().length; i++) {
             Player p = this.getPlayers()[i];
                     int shipsLastCycle = this.getPlayerShips(p);
             this.setPlayerShips(p, shipsLastCycle - damageDealtToPlayers[i]);
         }
     }
 
     /**
      * Add the number of new ships generated this cycle to the planet's owner
      */
     private void generateShips() {
 
         // ensure that there is only one player in charge.
         if (this.percentCaptured() == 100) {
             
             Player p = this.getPlayer();
             // number of new ships each cycle == planet resources
             int shipsGeneratedThisCycle = this.getResourceValue();
             this.setPlayerShips(p, (this.getPlayerShips(p) + shipsGeneratedThisCycle));
         }
     }
     
     /**
      * Update the capture percentage, if there is only one player on the planet.
      * If the planet is 100% captured, set the owner to be the currently dominant player. 
      */
     private void performCapture()
     {
         if (this.getPlayers().length > 1) {
             return;
         }
         if (this.percentCaptured() >= 100) {
            this.owner = this.dominantPlayer();
             this.capturer = null;
             return;
         }
         
         int currentlyCaptured = this.percentCaptured();
         int increaseInCapture = 1;
         // FIXME: write a calculation so that the percentage captured depends on the planet's resourceValue
         
         this.setPercentCaptured(currentlyCaptured + increaseInCapture);
     }
 }
