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
  *  (C) Liam Byrne, 2008 - 09.
  */
 
 package towers;
 
 import java.awt.Point;
 import java.awt.Shape;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.EnumMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import sprites.Sprite;
 
 
 public class AidTower extends AbstractTower {
    
    private static final int xpDivisor = 10;
    private static final int baseValue = 5;
    private static final int upgradeAidAmount = 1;
    private final Map<Attribute, Integer> aidAmounts = makeAidAmounts();
    private static int nextID = 0;
    private final int id = nextID++;
    // This set keeps references to towers that are sold keeping them alive
    // I don't think it's much of a memory issue though.
    private final Set<Tower> aidingTowers = new HashSet<Tower>();
    private List<Tower> towers;
    private final DamageNotifier damageNotifier = new AidDamageNotifier();
    private boolean isSold = false;
 
    public AidTower(Point p, List<Shape> pathBounds) {
       super(p, pathBounds, "Aid", 0, 100, 0, 0, 50, 0, false);
    }
    
    @Override
    public String getStat(Attribute a) {
       if(a == Attribute.Special) {
          return super.getStat(Attribute.Range);
       } else {
          return aidAmounts.get(a) + "%";
       }
    }
 
    @Override
    public String getStatName(Attribute a) {
       if(a == Attribute.Special) {
          return "Range of Effect";
       } else {
          return a.toString() + " Bonus";
       }
    }
 
    @Override
   public List<Bullet> tick(List<Sprite> sprites, boolean levelInProgress) {
       if(!isSold) {
          for(Tower t : towers) {
             if(!(t instanceof AidTower) && !aidingTowers.contains(t)) {
                if(super.getCentre().distance(t.getCentre()) < getRange()) {
                  aidingTowers.add(t);
                   aid(t);
                   t.addDamageNotifier(damageNotifier);
                }
             }
          }
       }
       return Collections.emptyList();
    }
    
    public void setTowers(List<Tower> towers) {
       assert this.towers == null : "List of towers is should not be set again";
       this.towers = towers;
    }
    
    @Override
    public Comparator<Sprite> getSpriteComparator() {
       return null;
    }
    
    @Override
    public void sell() {
       isSold = true;
       for(Tower t : aidingTowers) {
          for(Attribute a : aidAmounts.keySet()) {
             t.aidAttribute(a, 1, id);
          }
       }
    }
 
    @Override
    protected String getSpecial() {
       throw new RuntimeException("This should not be called due to getStat being overriden");
    }
 
    @Override
    protected String getSpecialName() {
       throw new RuntimeException("This should not be called due to getStatName being overriden");
    }
 
    @Override
    protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
          double damage, Point p, Sprite s, List<Shape> pathBounds) {
       throw new RuntimeException("makeBullet called on AidTower which doesn't shoot");
    }
    
    @Override
    protected void upgradeDamage() {
       upgradeAttribute(Attribute.Damage);
    }
    
    @Override
    protected void upgradeRange() {
       upgradeAttribute(Attribute.Range);
    }
    
    @Override
    protected void upgradeBulletSpeed() {
       upgradeAttribute(Attribute.Speed);
    }
    
    @Override
    protected void upgradeFireRate() {
       upgradeAttribute(Attribute.Rate);
    }
 
    @Override
    protected void upgradeSpecial() {
       super.upgradeRange();
       aidAll();
    }
    
    private static Map<Attribute, Integer> makeAidAmounts() {
       Map<Attribute, Integer> map = new EnumMap<Attribute, Integer>(Attribute.class);
       for(Attribute a : Attribute.values()) {
          map.put(a, baseValue);
       }
       map.remove(Attribute.Special);
       return map;
    }
    
    private void upgradeAttribute(Attribute a) {
       assert a != Attribute.Special : "Special is dealt with separately";
       aidAmounts.put(a, aidAmounts.get(a) + upgradeAidAmount);
       aidAll();
    }
    
   private void aidAll() {
       for(Tower t : aidingTowers) {
          aid(t);
       }
    }
    
    private void aid(Tower t) {
       for(Attribute a : aidAmounts.keySet()) {
          t.aidAttribute(a, 1 + aidAmounts.get(a) / 100.0, id);
       }
    }
    
    private class AidDamageNotifier implements DamageNotifier {
       
       private int killsSinceLastIncrease = 0;
       
       public synchronized void notifyOfKills(int kills) {
          killsSinceLastIncrease += kills;
          // Makes it get a kill for every xpDivisor kills it was notified of
          if(killsSinceLastIncrease >= xpDivisor) {
             int reducedKills = killsSinceLastIncrease / xpDivisor;
             increaseKills(reducedKills);
             killsSinceLastIncrease -= reducedKills * xpDivisor;
          }
       }
       
       public void notifyOfDamage(double damage) {
          increaseDamageDealt(damage / xpDivisor);
       }
    }
 }
