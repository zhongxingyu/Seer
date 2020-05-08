 /*
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /*
  * Now for some meat. This class represents a cannon.
  */
 package cargo;
 
 import celestial.Ship.Projectile;
 import celestial.Ship.Ship;
 import engine.Entity;
 import engine.Entity.State;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import javax.swing.ImageIcon;
 import lib.Parser;
 import lib.Soundling;
 import universe.SolarSystem;
 import universe.Universe;
 
 /**
  *
  * @author Nathan Wiehoff
  */
 public class Weapon extends Equipment {
     //for storing the texture so it doesn't have to be reloaded every time
 
     transient Image raw_tex;
     transient BufferedImage tex;
     int width;
     int height;
     //weapon properties
     protected double damage;
     protected double speed;
     protected boolean guided;
     protected Item ammoType;
     private String explosion = "None";
     //special effects
     private String fireEffectAsset;
     //guided weapons
     double accel;
     double turning;
     //sounds
     private transient Soundling fireEffect;
     private boolean loopFireEffect = false;
     //sound switched
     private double timeSinceLastActivation;
 
     public Weapon(String name) {
         super(name);
         init();
     }
 
     public void periodicUpdate(double tpf) {
         super.periodicUpdate(tpf);
         //update last activation timer
         timeSinceLastActivation += tpf;
         //update sounds
         updateSound();
     }
 
     public void initGraphics() {
         try {
             if (host.getUniverse() != null) {
                 /*
                  * Generate Graphics
                  */
                 if (ammoType == null) {
                     //the projectile name is the image
                     //get the image
                     raw_tex = host.getUniverse().getCache().getProjectileSprite(getName());
                 } else {
                     //the ammo type is the image
                     raw_tex = host.getUniverse().getCache().getProjectileSprite(ammoType.getName());
                 }
                 //create the usable version
                 ImageIcon icon = new ImageIcon(raw_tex);
                 height = (icon.getIconHeight());
                 width = (icon.getIconWidth());
                 tex = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                 /*
                  * Generate audio
                  */
                 if (fireEffectAsset != null) {
                     fireEffect = new Soundling(getName() + " " + System.nanoTime(), fireEffectAsset, loopFireEffect);
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public boolean isFiringEffect() {
         if (loopFireEffect) {
             if (timeSinceLastActivation <= (coolDown + (1 / coolDown) * tpf)) {
                 return true;
             } else {
                 return false;
             }
         } else {
             return true;
         }
     }
 
     private void updateSound() {
         //kill sound if needed
         if (!isFiringEffect()) {
             if (fireEffect != null) {
                 if (fireEffect.isPlaying()) {
                     fireEffect.stop();
                 }
             }
         }
     }
 
     public void disposeGraphics() {
         //dispose graphics
         raw_tex = null;
         tex = null;
         //dispose of audio
         if (fireEffect != null) {
             fireEffect.stop();
             fireEffect = null;
         }
     }
 
     private void init() {
         //get weapon stuff now
         Parser parse = Universe.getCache().getWeaponCache();
         ArrayList<Parser.Term> terms = parse.getTermsOfType("Weapon");
         Parser.Term relevant = null;
         for (int a = 0; a < terms.size(); a++) {
             String termName = terms.get(a).getValue("name");
             if (termName.matches(getName())) {
                 //get the stats we want
                 relevant = terms.get(a);
                 //and end
                 break;
             }
         }
         if (relevant != null) {
             setName(relevant.getValue("name"));
             setType(relevant.getValue("type"));
             setMass(Double.parseDouble(relevant.getValue("mass")));
             setDamage(Double.parseDouble(relevant.getValue("damage")));
             setRange(Double.parseDouble(relevant.getValue("range")));
             setSpeed(Double.parseDouble(relevant.getValue("speed")));
             setCoolDown(Double.parseDouble(relevant.getValue("refire")));
             String exp = relevant.getValue("explosion");
             if (exp != null) {
                 setExplosion(exp);
             }
             //guided?
             {
                 String test = relevant.getValue("guided");
                 if (test != null) {
                     guided = Boolean.parseBoolean(test);
                 }
                 if (guided) {
                     //retrieve all the stats
                     accel = Double.parseDouble(relevant.getValue("accel"));
                     turning = Double.parseDouble(relevant.getValue("turning"));
                 }
             }
             //ammo?
             {
                 String test = relevant.getValue("ammo");
                 if (test != null) {
                     //get the item
                     ammoType = new Item(test);
                 }
             }
             //go boom?
             fireEffectAsset = relevant.getValue("fireEffect");
             String loopClipTest = relevant.getValue("loop");
             if (loopClipTest != null) {
                 loopFireEffect = Boolean.parseBoolean(loopClipTest);
             } else {
                 loopFireEffect = false;
             }
         } else {
             System.out.println("The item " + getName() + " does not exist in WEAPONS.txt");
         }
     }
 
     @Override
     public void activate(Entity target) {
         if (getCoolDown() <= getActivationTimer() && enabled) {
             if (fire()) {
                 setActivationTimer(0); //restart cooldown
             }
         }
     }
 
     private boolean hasTarget() {
         if (getType().matches("missile") || getType().matches("battery") || getType().matches("turret")) {
             if (host.getTarget() != null) {
                 if (host.distanceTo(host.getTarget()) <= range
                         && host.getTarget().getState() == State.ALIVE) {
                     return true;
                 } else {
                     return false;
                 }
             } else {
                 return false;
             }
         } else {
             return true;
         }
     }
 
     private boolean fire() {
         if (enabled) {
             if (hasAmmo() && hasTarget()) {
                 double theta = host.getTheta();
                 if (getType().matches(Item.TYPE_TURRET) || getType().matches(Item.TYPE_BATTERY)) {
                     if (host.getTarget() != null) {
                         return turretFire(theta);
                     }
                 } else {
                     return simpleFire(theta);
                 }
             }
         }
         return false;
     }
 
     @Override
     public void killSounds() {
         host.stopSound(fireEffect);
         if (fireEffect != null) {
            if (fireEffect.isPlaying()) {
                fireEffect.stop();
            }
         }
     }
 
     public void useAmmo() {
         if (ammoType != null) {
             ArrayList<Item> cargo = host.getCargoBay();
             for (int a = 0; a < cargo.size(); a++) {
                 Item tmp = cargo.get(a);
                 if (tmp.getName().matches(ammoType.getName())) {
                     if (tmp.getGroup().matches(ammoType.getGroup())) {
                         if (tmp.getType().matches(ammoType.getType())) {
                             if (tmp.getQuantity() > 1) {
                                 tmp.setQuantity(tmp.getQuantity() - 1);
                             } else {
                                 cargo.remove(tmp);
                             }
                             break;
                         }
                     }
                 }
             }
         }
     }
 
     public boolean hasAmmo() {
         if (ammoType != null) {
             if (host.getNumInCargoBay(ammoType) > 0) {
                 //and return true;
                 return true;
             }
             return false;
         } else {
             return true;
         }
     }
 
     public double getDamage() {
         return damage;
     }
 
     public void setDamage(double damage) {
         this.damage = damage;
     }
 
     public double getSpeed() {
         return speed;
     }
 
     public void setSpeed(double speed) {
         this.speed = speed;
     }
 
     public String toString() {
         String ret = "";
         if (ammoType == null) {
             ret = super.toString();
         } else if (host != null) {
             ret = super.toString();
             ret += " <" + host.getNumInCargoBay(ammoType) + ">";
         } else {
             ret = super.toString();
         }
         return ret;
     }
 
     public String getExplosion() {
         return explosion;
     }
 
     public void setExplosion(String explosion) {
         this.explosion = explosion;
     }
 
     private boolean turretFire(double theta) {
         //use any ammo
         useAmmo();
         //create projectile
         String tName = "";
         if (ammoType != null) {
             tName = ammoType.getName();
         } else {
             tName = getName();
         }
         Projectile pro = new Projectile(host, tName, tName, raw_tex, tex, width, height);
         pro.init(false);
         //calculate relative position from hardpoint
         double hT = getSocket().getT();
         double hR = getSocket().getR();
         double dx = Math.cos(hT + (theta) - Math.PI) * hR;
         double dy = Math.sin(hT + (theta - Math.PI)) * hR;
         //store position
         pro.setX((host.getX() + host.getWidth() / 2) - pro.getWidth() / 2 + dx);
         pro.setY((host.getY() + host.getHeight() / 2) - pro.getHeight() / 2 + dy);
         pro.setLastX(pro.getX());
         pro.setLastY(pro.getY());
         //get target position
         double tx = host.getTarget().getX() + host.getTarget().getWidth() / 2;
         double ty = host.getTarget().getY() + host.getTarget().getHeight() / 2;
         //calculate theta
         double mx = pro.getX() - tx;
         double my = pro.getY() - ty;
         double tTheta = Math.atan2(my, mx);
         //calculate speed
         double pdx = speed * Math.cos(tTheta - Math.PI);
         double pdy = speed * Math.sin(tTheta - Math.PI);
         //add to host vector
         pro.setVx(host.getVx() + pdx);
         pro.setVy(host.getVy() + pdy);
         //store angle
         pro.setTheta(tTheta);
         //store physics
         pro.setDamage(damage);
         pro.setMaxRange(getRange());
         pro.setMass(getMass());
         pro.setSpeed(speed);
         pro.setExplosion(explosion);
         //store AI
         pro.setGuided(guided);
         pro.setRange(range);
         if (guided) {
             pro.setFuel(Double.MAX_VALUE);
             pro.setMaxFuel(Double.MAX_VALUE);
             //store stats
             pro.setSensor(range);
             pro.setAccel(accel);
             pro.setMaxRange(getRange() * 1.5);
             pro.setTurning(turning);
         }
         //determine if OOS or not
         SolarSystem playerSys = host.getUniverse().getPlayerShip().getCurrentSystem();
         if (host.getCurrentSystem() == playerSys) {
             //add to universe
             pro.setCurrentSystem(host.getCurrentSystem());
             host.getCurrentSystem().putEntityInSystem(pro);
         } else {
             //deal damage directly
             Ship tvp = host.getTarget();
             if (tvp != null) {
                 tvp.dealDamage(damage);
                 tvp.setLastBlow(host);
             }
         }
         //play fire effect
         if (fireEffect != null) {
             if (!fireEffect.isPlaying()) {
                 host.playSound(fireEffect);
             } else {
                 if (!loopFireEffect) {
                     fireEffect.play();
                 }
             }
         }
         //reset timer
         timeSinceLastActivation = 0;
         return true;
     }
 
     private boolean simpleFire(double theta) {
         //use any ammo
         useAmmo();
         //create projectile
         String tName = "";
         if (ammoType != null) {
             tName = ammoType.getName();
         } else {
             tName = getName();
         }
         Projectile pro = new Projectile(host, tName, tName, raw_tex, tex, width, height);
         pro.init(false);
         //calculate relative position from hardpoint
         double hT = getSocket().getT();
         double hR = getSocket().getR();
         double dx = Math.cos(hT + (theta) - Math.PI) * hR;
         double dy = Math.sin(hT + (theta - Math.PI)) * hR;
         //store position
         pro.setX((host.getX() + host.getWidth() / 2) - pro.getWidth() / 2 + dx);
         pro.setY((host.getY() + host.getHeight() / 2) - pro.getHeight() / 2 + dy);
         pro.setLastX(pro.getX());
         pro.setLastY(pro.getY());
         //calculate speed
         double pdx = speed * Math.cos(theta - Math.PI);
         double pdy = speed * Math.sin(theta - Math.PI);
         //add to host vector
         pro.setVx(host.getVx() + pdx);
         pro.setVy(host.getVy() + pdy);
         //store angle
         pro.setTheta(host.getTheta());
         //store physics
         pro.setDamage(damage);
         pro.setMaxRange(getRange());
         pro.setMass(getMass());
         pro.setSpeed(speed);
         pro.setExplosion(explosion);
         //store AI
         pro.setGuided(guided);
         pro.setRange(range);
         if (guided) {
             pro.setFuel(Double.MAX_VALUE);
             pro.setMaxFuel(Double.MAX_VALUE);
             //store stats
             pro.setSensor(range);
             pro.setAccel(accel);
             pro.setMaxRange(getRange() * 1.5);
             pro.setTurning(turning);
         }
         //determine if OOS or not
         SolarSystem playerSys = host.getUniverse().getPlayerShip().getCurrentSystem();
         if (host.getCurrentSystem() == playerSys) {
             //add to universe
             pro.setCurrentSystem(host.getCurrentSystem());
             host.getCurrentSystem().putEntityInSystem(pro);
         } else {
             //deal damage directly
             Ship tvp = host.getTarget();
             if (tvp != null) {
                 tvp.dealDamage(damage);
                 tvp.setLastBlow(host);
             }
         }
         //play fire effect
         if (fireEffect != null) {
             if (!fireEffect.isPlaying()) {
                 host.playSound(fireEffect);
             } else {
                 if (!loopFireEffect) {
                     fireEffect.play();
                 }
             }
         }
         //reset timer
         timeSinceLastActivation = 0;
         return true;
     }
 }
