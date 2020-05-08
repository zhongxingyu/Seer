 /*
  *  This file is part of Pac Defence.
  *
  *  Pac Defence is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Pac Defence is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Pac Defence.  If not, see <http://www.gnu.org/licenses/>.
  *  
  *  (C) Liam Byrne, 2008.
  */
 
 package towers;
 
 import gui.Circle;
 import images.ImageHelper;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Shape;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 import java.util.List;
 
 import sprites.Sprite;
 
 public abstract class AbstractTower implements Tower {
    
    // The color of the range of the tower when drawn
    private static final Color rangeColour = new Color(255, 255, 255, 100);
    private static final float upgradeIncreaseFactor = 1.1F;
    
    private int damageLevel = 1;
    private int rangeLevel = 1;
    private int rateLevel = 1;
    private int speedLevel = 1;
    private int specialLevel = 1;
       
    // The top left point of this tower
    private final Point topLeft;
    // The centre of this tower
    private final Point centre;
    private final Circle bounds;
    private final Rectangle boundingRectangle = new Rectangle();
    private final String name;
    // The number of clock ticks between each shot
    private int fireRate;
    // The number of clock ticks until this tower's next shot
    private int timeToNextShot = 0;
    private int range;
    private int twiceRange;
    private double bulletSpeed;
    private double damage;
    
    // The width/height of the image (as it's square)
    private final int width;
    private final int halfWidth;
    // The width of the turret from the centre of the tower
    private final int turretWidth;
 
    private final BufferedImage originalImage;
    private BufferedImage currentImage;
    
    private boolean isSelected = false;
    
 
    public AbstractTower(Point p, String name, int fireRate, int range, double bulletSpeed,
          double damage, int width, int turretWidth, BufferedImage image) {
       centre = new Point(p);
       // Only temporary, it gets actually set later
       topLeft = new Point(0, 0);
       this.name = name;
       this.fireRate = fireRate;
       this.range = range;
       twiceRange = range * 2;
       this.bulletSpeed = bulletSpeed;
       this.damage = damage;
       this.width = width;
       halfWidth = width / 2;
       bounds = new Circle(centre, halfWidth);
       this.turretWidth = turretWidth;
       setTopLeft();
       setBounds();
       originalImage = ImageHelper.resize(image, width, width);
       currentImage = originalImage;
    }
 
    @Override
    public Bullet tick(List<Sprite> sprites) {
       for (Sprite s : sprites) {
          if (checkDistance(s)) {
             double dx = s.getPosition().getX() - centre.getX();
             double dy = s.getPosition().getY() - centre.getY();
             //System.out.println(dx + " " + dy);
             currentImage = ImageHelper.rotateImage(originalImage, dx, -dy);
             if (timeToNextShot > 0) {
                timeToNextShot--;
                return null;
             } else {
                timeToNextShot = fireRate;
                return new Bullet(dx, dy, turretWidth, range, bulletSpeed, damage, centre);
             }
          }
       }
       return null;
    }
 
    @Override
    public void draw(Graphics g) {
       if(!isSelected) {
          g.drawImage(currentImage, (int) topLeft.getX(), (int) topLeft.getY(), null);
       }
    }
 
    @Override
    public void drawSelected(Graphics g) {
       drawShadow(g);
    }
    
    @Override
    public void drawShadow(Graphics g) {
       // System.out.println(p.getX() + " " + p.getY());
       int topLeftX = (int) topLeft.getX();
       int topLeftY = (int) topLeft.getY();
       int topLeftRangeX = (int) centre.getX() - range;
       int topLeftRangeY = (int) centre.getY() - range;
       g.setColor(rangeColour);
       g.fillOval(topLeftRangeX, topLeftRangeY, twiceRange, twiceRange);
       g.setColor(Color.BLACK);
       g.drawOval(topLeftRangeX, topLeftRangeY, twiceRange, twiceRange);
       // Change this so it's semi-seethrough
       g.drawImage(currentImage, topLeftX, topLeftY, width, width, null);
 
    }
 
    @Override
    public boolean towerClash(Tower t) {
       Shape s = t.getBounds();
       if(s instanceof Circle) {
          Circle c = (Circle)s;
          double distance = Point.distance(centre.getX(), centre.getY(), t.getCentre().getX(), 
                t.getCentre().getY());
          return distance < bounds.getRadius() + c.getRadius();
       } else {
          return bounds.intersects(s.getBounds2D());
       }
    }
    
    @Override
    public boolean contains(Point p) {
       if(boundingRectangle.contains(p)) {
          int x = (int) (p.getX() - centre.getX() + halfWidth);
          int y = (int) (p.getY() - centre.getY() + halfWidth);
          // RGB of zero means a completely alpha i.e. transparent pixel
          return currentImage.getRGB(x, y) != 0;
       }
       return false;
    }
    
    @Override
    public Shape getBounds() {
       return bounds;
    }
 
    @Override
    public Rectangle getBoundingRectangle() {
       return boundingRectangle;
    }
    
    @Override
    public String getName() {
       return name;
    }
    
    @Override
    public int getRange() {
       return range;
    }
    
    @Override
    public Point getCentre() {
       return centre;
    }
    
    @Override
    public void setCentre(Point p) {
       centre.setLocation(p);
       setTopLeft();
       setBounds();
    }
    
    @Override
    public int getAttributeLevel(Attribute a) {
       if(a == Tower.Attribute.Damage) {
          return damageLevel;
       } else if(a == Tower.Attribute.Range) {
          return rangeLevel;
       } else if (a == Tower.Attribute.Rate) {
          return rateLevel;
       } else if (a == Tower.Attribute.Speed) {
          return speedLevel;
       } else if (a == Tower.Attribute.Special) {
          return specialLevel;
       } else {
          throw new RuntimeException("Extra attribute has been added without changing " +
                "getAttributeLevel in AbstractTower");
       }
    }
    
    @Override
    public void raiseAttributeLevel(Attribute a) {
       if(a == Tower.Attribute.Damage) {
          damageLevel++;
          damage *= upgradeIncreaseFactor;
       } else if(a == Tower.Attribute.Range) {
          rangeLevel++;
          range *= upgradeIncreaseFactor;
          twiceRange = range * 2;
       } else if (a == Tower.Attribute.Rate) {
          rateLevel++;
         fireRate *= upgradeIncreaseFactor;
       } else if (a == Tower.Attribute.Speed) {
          speedLevel++;
          bulletSpeed *= upgradeIncreaseFactor;
       } else if (a == Tower.Attribute.Special) {
          specialLevel++;
          // TODO
       } else {
          throw new RuntimeException("Extra attribute has been added without changing " +
                "raiseAttributeLevel in AbstractTower");
       }
    }
    
    @Override
    public double getDamage() {
       return damage;
    }
    
    @Override
    public int getFireRate() {
       return fireRate;
    }
    
    @Override
    public double getBulletSpeed() {
       return bulletSpeed;
    }
    
    @Override
    public String getSpecial() {
       //TODO Implement this
       return "none";
    }
    
    @Override
    public void select(boolean select) {
       isSelected = select;
    }
    
    @Override
    public abstract Tower constructNew();
    
 
    private boolean checkDistance(Sprite s) {
       Point2D sPos = s.getPosition();
       if(!s.isAlive()) {
          return false;
       }
       double distance = Point.distance(centre.getX(), centre.getY(), sPos.getX(), sPos.getY());
       return distance < range + s.getHalfWidth();
    }
    
    private void setTopLeft() {
       topLeft.setLocation((int) centre.getX() - halfWidth, (int) centre.getY() - halfWidth);
    }
    
    private void setBounds() {
       boundingRectangle.setBounds((int) topLeft.getX(), (int) topLeft.getY(), width, width);
    }
 
 }
