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
 
 import images.ImageHelper;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Composite;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import logic.Circle;
 import logic.Formulae;
 import logic.Game;
 import logic.Helper;
 import sprites.LooseFloat;
 import sprites.Sprite;
 
 public abstract class AbstractTower implements Tower {
 
    public static final float shadowAmount = 0.75F;
    protected static final float upgradeIncreaseFactor = 1.05F;
    
    // Keep track of the loaded images so they are only loaded once
    private static final Map<String, BufferedImage> towerImages =
          new HashMap<String, BufferedImage>();
    private static final Map<String, BufferedImage> rotatingImages =
          new HashMap<String, BufferedImage>();
    private static final Map<String, BufferedImage> towerButtonImages =
          new HashMap<String, BufferedImage>();
    
    private static final Map<Class<? extends AbstractTower>, Map<LooseFloat, BufferedImage>>
          rotatedImages = new HashMap<Class<? extends AbstractTower>, Map<LooseFloat,
          BufferedImage>>();
    
    public static final int turretThickness = 4;
    
    // Maps the ID of an aid tower to the aid factor it gives for each attribute
    private final Map<Integer, Map<Attribute, Double>> aidFactors =
          new HashMap<Integer, Map<Attribute, Double>>();
    private final Map<Attribute, Double> currentFactors = createCurrentFactors();
 
    private Map<Attribute, Integer> attributeLevels = createAttributeLevels();
 
    // The top left point of this tower
    private final Point topLeft;
    // The centre of this tower
    private final Point centre;
    private final Circle bounds;
    private final Rectangle boundingRectangle = new Rectangle();
    private final String name;
    // The number of clock ticks between each shot
    private double fireRate;
    // The number of clock ticks until this tower's next shot
    private double timeToNextShot = 0;
    private double range;
    private final double rangeUpgrade;
    private int twiceRange;
    private double bulletSpeed;
    private final double bulletSpeedUpgrade;
    private double damage;
 
    // The width/height of the image (as it's square)
    private final int width;
    private final int halfWidth;
    // The width of the turret from the centre of the tower
    private final int turretWidth;
 
    private final BufferedImage baseImage;
    private final BufferedImage rotatingImage;
    private final boolean imageRotates;
    private BufferedImage currentImage;
    private final BufferedImage buttonImage;
    
    private final Rectangle2D pathBounds;
 
    private boolean isSelected = false;
 
    private List<DamageNotifier> damageNotifiers = new ArrayList<DamageNotifier>();
    private long damageDealt = 0;
    private double fractionalDamageDealt = 0;
    private int kills = 0;
    private int killsLevel = 1;
    private int damageDealtLevel = 1;
    private long nextUpgradeDamage = Formulae.nextUpgradeDamage(killsLevel);
    private int nextUpgradeKills = Formulae.nextUpgradeKills(damageDealtLevel);
    
    private Comparator<Sprite> spriteComparator = new Sprite.FirstComparator();
    
    private List<Bullet> bulletsToAdd = new ArrayList<Bullet>();
 
    protected AbstractTower(Point p, Rectangle2D pathBounds, String name, int fireRate,
          double range, double bulletSpeed, double damage, int width, int turretWidth,
          boolean hasOverlay) {
       // TODO make images all dependant on the name of the tower
       centre = new Point(p);
       this.pathBounds = pathBounds;
       // Only temporary, it gets actually set later
       topLeft = new Point(0, 0);
       this.name = name;
       this.fireRate = fireRate;
       this.range = range;
       rangeUpgrade = range * (upgradeIncreaseFactor - 1);
       twiceRange = (int)(range * 2);
       this.bulletSpeed = bulletSpeed;
       bulletSpeedUpgrade = bulletSpeed * (upgradeIncreaseFactor - 1);
       this.damage = damage;
       this.width = width;
       halfWidth = width / 2;
       this.turretWidth = turretWidth;
       bounds = new Circle(centre, halfWidth);
       setTopLeft();
       setBounds();
       // Use the class name as the actual name could be anything (spaces, etc.)
       String className = getClass().getSimpleName();
       // Need to remove the 'Tower' off the end
       className = className.substring(0, className.length() - 5);
       String nameLowerCase = makeFirstCharacterLowerCase(className);
      baseImage = loadImage(towerButtonImages, width, "towers", nameLowerCase + ".png");
       imageRotates = (hasOverlay && turretWidth != 0);
       if(hasOverlay) {
          rotatingImage = loadImage(rotatingImages, width, "towers", "overlays",
             className + "Overlay.png");
       } else {
          rotatingImage = null;
       }
       currentImage = drawCurrentImage(rotatingImage);
       buttonImage = loadImage(towerButtonImages, 0, "buttons", "towers",
             className + "Tower.png");
    }
    
    private String makeFirstCharacterLowerCase(String s) {
       StringBuilder sb = new StringBuilder(s);
       sb.insert(0, Character.toLowerCase(sb.charAt(0)));
       sb.deleteCharAt(1);
       return sb.toString();
    }
 
    @Override
    public List<Bullet> tick(List<Sprite> sprites) {
       sprites = new ArrayList<Sprite>(sprites);
       Collections.sort(sprites, spriteComparator);
       // Decrements here so it's on every tick, not just when it is able to shoot
       timeToNextShot--;
       List<Bullet> fired = Collections.emptyList();
       if(imageRotates || timeToNextShot <= 0) {
          // If the image rotates, this needs to be done to find out the direction
          // to rotate to
          fired = fireBullets(sprites);
       }
       if (timeToNextShot <= 0 && fired.size() > 0) {
          timeToNextShot = fireRate;
          bulletsToAdd.addAll(fired);
       }
       List<Bullet> bullets = bulletsToAdd;
       bulletsToAdd = new ArrayList<Bullet>(bulletsToAdd.size());
       return bullets;
    }
 
    @Override
    public void draw(Graphics g) {
       if (isSelected) {
          drawRange(g);
       }
       g.drawImage(currentImage, (int) topLeft.getX(), (int) topLeft.getY(), null);
    }
 
    @Override
    public void drawShadowAt(Graphics g, Point p) {
       Graphics2D g2D = (Graphics2D) g;
       drawRange(g2D, p);
       // Save the current composite to reset back to later
       Composite c = g2D.getComposite();
       // Makes it so what is drawn is partly transparent
       g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, shadowAmount));  
       g2D.drawImage(currentImage, (int) p.getX() - halfWidth, (int) p.getY() - halfWidth, width,
             width, null);
       g2D.setComposite(c);
    }
 
    @Override
    public boolean doesTowerClashWith(Tower t) {
       Shape s = t.getBounds();
       if (s instanceof Circle) {
          Circle c = (Circle) s;
          double distance = Point.distance(centre.getX(), centre.getY(), t.getCentre().getX(), t
                .getCentre().getY());
          return distance < bounds.getRadius() + c.getRadius();
       } else {
          return bounds.intersects(s.getBounds2D());
       }
    }
    
    @Override
    public boolean canTowerBeBuilt(List<Polygon> path) {
       for(Polygon p : path) {
          if(bounds.intersects(p)) {
             return false;
          }
       }
       return true;
    }
 
    @Override
    public boolean contains(Point p) {
       if (boundingRectangle.contains(p)) {
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
    public String getName() {
       return name + " Tower";
    }
 
    @Override
    public Point getCentre() {
       return centre;
    }
 
    @Override
    public int getAttributeLevel(Attribute a) {
       return attributeLevels.get(a);
    }
 
    @Override
    public void raiseAttributeLevel(Attribute a, boolean boughtUpgrade) {
       if(boughtUpgrade) {
          attributeLevels.put(a, attributeLevels.get(a) + 1);
       }
       switch(a) {
          case Damage:
             upgradeDamage();
             break;
          case Range:
             upgradeRange();
             break;
          case Rate:
             upgradeFireRate();
             break;
          case Speed:
             upgradeBulletSpeed();
             break;
          case Special:
             upgradeSpecial();
             break;
          default:
             throw new RuntimeException("New Attribute has been added or something.");
       }
    }
    
    @Override
    public void aidAttribute(Attribute a, double factor, int towerID) {
       assert a != Attribute.Special : "Special cannot be aided";
       double currentFactor = currentFactors.get(a);
       if(factor == 1) { // This signals that this aid tower has been sold
          aidFactors.remove(towerID);
          // Find the best factor from the remaining factors
          for(Map<Attribute, Double> m : aidFactors.values()) {
             double d = m.get(a);
             if(d > factor) {
                factor = d;
             }
          }
          // and if it isn't the current factor, make it so, as the current factor
          // would've been removed.
          if(factor != currentFactor) {
             multiplyAttribute(a, factor / currentFactor);
             currentFactors.put(a, factor);
          }
       } else {
          if(!aidFactors.containsKey(towerID)) {
             aidFactors.put(towerID, new EnumMap<Attribute, Double>(Attribute.class));
          }
          aidFactors.get(towerID).put(a, factor);
          // This limits it to one aid tower's upgrade (the best one)
          if(factor > currentFactor) {
             // This applies the new upgrade and cancels out the last one
             multiplyAttribute(a, factor / currentFactor);
             currentFactors.put(a, factor);
          }
       }
    }
    
    @Override
    public String getStat(Attribute a) {
       switch(a) {
          case Damage:
             return Helper.format(damage, 2);
          case Range:
             return Helper.format(range, 0);
          case Rate:
             return Helper.format(fireRate / Game.CLOCK_TICKS_PER_SECOND, 2) + "s";
          case Speed:
             return Helper.format(bulletSpeed, 2);
          case Special:
             return getSpecial();
       }
       throw new RuntimeException("Invalid attribute: " + a + " given.");
    }
    
    @Override
    public String getStatName(Attribute a) {
       switch(a) {
          case Special:
             return getSpecialName();
          case Rate:
             return "Shoots Every";
          default:
             return a.toString();
       }
    }
 
    @Override
    public void select(boolean select) {
       isSelected = select;
    }
    
    @Override
    public Tower constructNew(Point p, Rectangle2D pathBounds) {
       try {
          Constructor<? extends Tower> c = this.getClass().getConstructor(Point.class,
                Rectangle2D.class);
          return c.newInstance(p, pathBounds);
       } catch(Exception e) {
          // No exception should be thrown if the superclass is reasonably behaved
          throw new RuntimeException("\nSuperclass of AbstractTower is not well behaved.\n" + e +
                " was thrown.\n Either fix this, or override constructNew()");
       }
    }
    
    @Override
    public void addDamageNotifier(DamageNotifier d) {
       damageNotifiers.add(d);
    }
 
    @Override
    public synchronized void increaseDamageDealt(double damage) {
       assert damage > 0 : "Damage given was negative or zero.";
       for(DamageNotifier d : damageNotifiers) {
          d.notifyOfDamage(damage);
       }
       long longDamage = (long)damage;
       damageDealt += longDamage;
       // Handling of fractional amounts
       fractionalDamageDealt += damage - longDamage;
       if(fractionalDamageDealt > 1) {
          longDamage = (long)fractionalDamageDealt;
          damageDealt += longDamage;
          fractionalDamageDealt -= longDamage;
       }
       if(damageDealt >= nextUpgradeDamage) {
          damageDealtLevel++;
          nextUpgradeDamage = Formulae.nextUpgradeDamage(damageDealtLevel);
          upgradeAllStats();
       }
    }
 
    @Override
    public synchronized void increaseKills(int kills) {
       assert kills > 0 : "Kills given was less than or equal to zero";
       for(DamageNotifier d : damageNotifiers) {
          d.notifyOfKills(kills);
       }
       this.kills += kills;
       if (this.kills >= nextUpgradeKills) {
          killsLevel++;
          nextUpgradeKills = Formulae.nextUpgradeKills(killsLevel);
          upgradeAllStats();
       }
    }
 
    @Override
    public long getDamageDealt() {
       return damageDealt;
    }
    
    @Override
    public long getDamageDealtForUpgrade() {
       return nextUpgradeDamage;
    }
 
    @Override
    public int getKills() {
       return kills;
    }
    
    @Override
    public int getKillsForUpgrade() {
       return nextUpgradeKills;
    }
 
    @Override
    public BufferedImage getButtonImage() {
       return buttonImage;
    }
    
    @Override
    public int getExperienceLevel() {
       // -1 so it starts at level 1
       return killsLevel + damageDealtLevel - 1;
    }
    
    @Override
    public void setSpriteComparator(Comparator<Sprite> c) {
       spriteComparator = c;
    }
    
    @Override
    public Comparator<Sprite> getSpriteComparator() {
       return spriteComparator;
    }
    
    protected double getFireRate() {
       return fireRate;
    }
    
    protected double getRange() {
       return range;
    }
    
    protected double getBulletSpeed() {
       return bulletSpeed;
    }
    
    protected double getDamage() {
       return damage;
    }
    
    protected double getTimeToNextShot() {
       return timeToNextShot;
    }
    
    protected abstract String getSpecial();
    
    protected abstract String getSpecialName();
    
    protected abstract Bullet makeBullet(double dx, double dy, int turretWidth, int range,
             double speed, double damage, Point p, Sprite s, Rectangle2D pathBounds);
    
    protected List<Bullet> makeBullets(double dx, double dy, int turretWidth, int range,
             double speed, double damage, Point p, Sprite s, Rectangle2D pathBounds) {
       return Helper.makeListContaining(makeBullet(dx, dy, turretWidth, range, speed,
             damage, p, s, pathBounds));
    }
    
    protected List<Bullet> fireBullets(List<Sprite> sprites) {
       for (Sprite s : sprites) {
          // Checking that the pathBounds contains the sprites position means a tower won't
          // shoot so that it's bullet almost immediately goes off screen and is wasted
          if (pathBounds.contains(s.getPosition()) && checkDistance(s)) {
             return fireBulletsAt(s, true);
          }
       }
       return Collections.emptyList();
    }
    
    protected boolean checkDistance(Sprite s) {
       return checkDistance(s, centre, range);
    }
    
    /**
     * Checks if the distance from the sprite to a point is less than the range.
     * @param s
     * @param p
     * @param range
     * @return
     */
    protected boolean checkDistance(Sprite s, Point p, double range) {
       if (!s.isAlive()) {
          return false;
       }
       double distance = p.distance(s.getPosition());
       return distance < range + s.getHalfWidth();
    }
    
    protected List<Bullet> fireBulletsAt(Sprite s, boolean rotateTurret) {
       return fireBulletsAt(s, centre, rotateTurret, turretWidth, range, bulletSpeed, damage);
    }
    
    protected List<Bullet> fireBulletsAt(Sprite s, Point p, boolean rotateTurret,
          int turretWidth, double range, double bulletSpeed, double damage) {
       double dx = s.getPosition().getX() - p.getX();
       double dy = s.getPosition().getY() - p.getY();
       if(imageRotates && rotateTurret) {
          currentImage = drawCurrentImage(rotateImage(ImageHelper.vectorAngle(dx, -dy)));
       }
       return makeBullets(dx, dy, turretWidth, (int)range, bulletSpeed, damage, p, s, pathBounds);
    }
 
    protected void upgradeDamage() {
       damage *= upgradeIncreaseFactor;
    }
 
    protected void upgradeRange() {
       range += currentFactors.get(Attribute.Range) * rangeUpgrade;
       twiceRange = (int)(range * 2);
    }
 
    protected void upgradeFireRate() {
       fireRate /= upgradeIncreaseFactor;
    }
 
    protected void upgradeBulletSpeed() {
       bulletSpeed += currentFactors.get(Attribute.Speed) * bulletSpeedUpgrade;
    }
 
    protected abstract void upgradeSpecial();
    
    protected void addExtraBullets(Bullet... bullets) {
       bulletsToAdd.addAll(Arrays.asList(bullets));
    }
    
    private BufferedImage loadImage(Map<String, BufferedImage> map, int width,
          String... imagePath) {
       String imageName = imagePath[imagePath.length - 1];
       if(!map.containsKey(imageName)) {
          if(width == 0) {
             map.put(imageName, ImageHelper.makeImage(imagePath));
          } else {
             map.put(imageName, ImageHelper.makeImage(width, width, imagePath));
          }
       }
       return map.get(imageName);
    }
    
    private BufferedImage drawCurrentImage(BufferedImage rotatingOverlay) {
       BufferedImage image = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB_PRE);
       Graphics2D g = image.createGraphics();
       g.drawImage(baseImage, 0, 0, null);
       g.drawImage(rotatingOverlay, 0, 0, null);
       return image;
    }
    
    private void upgradeAllStats() {
       upgradeDamage();
       upgradeRange();
       upgradeFireRate();
       upgradeBulletSpeed();
       upgradeSpecial();
    }
    
    private void drawRange(Graphics g) {
       drawRange(g, centre);
    }
    
    private void drawRange(Graphics g, Point p) {
       int topLeftRangeX = (int)(p.getX() - range);
       int topLeftRangeY = (int)(p.getY() - range);
       Graphics2D g2D = (Graphics2D) g;
       Composite c = g2D.getComposite();
       g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F));  
       g2D.setColor(Color.WHITE);
       g2D.fillOval(topLeftRangeX, topLeftRangeY, twiceRange, twiceRange);
       g2D.setComposite(c);
       g2D.setColor(Color.BLACK);
       g2D.drawOval(topLeftRangeX, topLeftRangeY, twiceRange, twiceRange);
    }
 
    private void setTopLeft() {
       topLeft.setLocation((int) centre.getX() - halfWidth, (int) centre.getY() - halfWidth);
    }
 
    private void setBounds() {
       boundingRectangle.setBounds((int) topLeft.getX(), (int) topLeft.getY(), width, width);
       bounds.setCentre(centre);
    }
    
    private void multiplyAttribute(Attribute a, double factor) {
       assert a != Attribute.Special : "Special cannot be simply multiplied";
       switch(a) {
          case Damage:
             damage *= factor;
             return;
          case Range:
             range *= factor;
             twiceRange = (int)(range * 2);
             return;
          case Rate:
             fireRate /= factor;
             return;
          case Speed:
             bulletSpeed *= factor;
             return;
       }
    }
    
    private Map<Attribute, Double> createCurrentFactors() {
       Map<Attribute, Double> map = new EnumMap<Attribute, Double>(Attribute.class);
       for(Attribute a : Attribute.values()) {
          if(a != Attribute.Special) {
             map.put(a, 1.0);
          }
       }
       return map;
    }
    
    private BufferedImage rotateImage(double angle) {
       if(!rotatedImages.containsKey(getClass())) {
          // I use a TreeMap as otherwise I'd need to implement hashCode in LooseFloat
          rotatedImages.put(getClass(), new TreeMap<LooseFloat, BufferedImage>());
       }
       Map<LooseFloat, BufferedImage> m = rotatedImages.get(getClass());
       // Use LooseFloat to reduce precision so rotated images are less likely
       // to be duplicated
       LooseFloat f = new AbstractTowerLooseFloat(angle);
       if(!m.containsKey(f)) {
          m.put(f, ImageHelper.rotateImage(rotatingImage, angle));
       }
       return m.get(f);
    }
    
    private Map<Attribute, Integer> createAttributeLevels() {
       Map<Attribute, Integer> map = new EnumMap<Attribute, Integer>(Attribute.class);
       for(Attribute a : Attribute.values()) {
          // All levels start at 1
          map.put(a, 1);
       }
       return map;
    }
    
    private class AbstractTowerLooseFloat extends LooseFloat {
       
       public AbstractTowerLooseFloat(float f) {
          super(f);
       }
       
       public AbstractTowerLooseFloat(double d) {
          super(d);
       }
 
       @Override
       protected float getPrecision() {
          return 0.012F;
       }
    }
    
    private static BufferedImage createRotatingImage(int width, int turretWidth) {
       assert turretWidth > 0 : "turretWidth must be > 0";
       BufferedImage turretCentre = ImageHelper.makeImage("towers", "overlays", "turrets",
             "turretCentre.png");
       BufferedImage image = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB_PRE);
       Graphics2D g = image.createGraphics();
       g.drawImage(turretCentre, width/2 - turretThickness/2, width/2 - turretThickness/2, null);
       g.setColor(Color.BLACK);
       g.fillRect(width/2 - turretThickness/2, width/2 - turretWidth, turretThickness, turretWidth);
       // TODO perhaps make the end of the turret a little prettier
       return image;
    }
    
    public static void main(String... args) {
       for(int i = 1; i <= 25; i++) {
          ImageHelper.writePNG(createRotatingImage(50, i), "towers", "rotatingOverlays", "turrets",
                "turret" + i + ".png");
       }
    }
 
 }
