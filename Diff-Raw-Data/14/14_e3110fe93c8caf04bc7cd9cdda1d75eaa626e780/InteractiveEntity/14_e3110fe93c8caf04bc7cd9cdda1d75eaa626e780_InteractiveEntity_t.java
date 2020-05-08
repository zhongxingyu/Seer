 package vooga.rts.gamedesign.sprite;
 
 import java.awt.Dimension;
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.Map;
 
 import vooga.rts.gamedesign.factories.Factory;
import vooga.rts.gamedesign.sprite.rtsprite.EntityVisitor;
 import vooga.rts.gamedesign.sprite.rtsprite.IAttackable;
 import vooga.rts.gamedesign.sprite.rtsprite.Projectile;
 
 import vooga.rts.gamedesign.strategy.attackstrategy.AttackStrategy;
 import vooga.rts.gamedesign.strategy.attackstrategy.CannotAttack;
 import vooga.rts.gamedesign.upgrades.ArmorUpgradeNode;
 import vooga.rts.gamedesign.upgrades.UpgradeNode;
 import vooga.rts.gamedesign.upgrades.UpgradeTree;
 import vooga.rts.util.Location;
 import vooga.rts.util.Pixmap;
 import vooga.rts.util.Sound;
 
 /**
  * This class is the extension of GameEntity. It represents shapes that are
  * able to upgrade (to either stat of its current properties or add new
  * properties) and attack others.
  * 
  * @author Ryan Fishel
  * @author Kevin Oh
  * @author Francesco Agosti
  * @author Wenshun Liu
  *
  */
 public class InteractiveEntity extends GameEntity implements IAttackable{
 
     private UpgradeTree myUpgradeTree;
     private Sound mySound;
     private AttackStrategy myAttackStrategy;
     private int myArmor;
 
     private Map<String, Factory> myMakers; //WHERE SHOULD THIS GO??
 
     public InteractiveEntity (Pixmap image, Location center, Dimension size, Sound sound, int teamID, int health) {
         super(image, center, size, teamID, health);
         myMakers = new HashMap<String, Factory>(); //WHERE SHOULD THIS GO?
         myUpgradeTree =new UpgradeTree();
         mySound = sound;
         myAttackStrategy = new CannotAttack();
 
         UpgradeNode armor = new ArmorUpgradeNode("armor1","myHealth",40); //TESTING
         myUpgradeTree.addUpgrade(armor); //TESTING
     }
 
     /** 
      *  This would accept RTSpriteVisitors and behave according to the 
      *  visitor's visit method. This code will always run 
      *  RTSpriteVisitor.visit(this). "this" being the subclass of RTSprite. 
      * @throws CloneNotSupportedException 
      */
    public void accept(EntityVisitor visitor) {
             visitor.visit(this);

     }
   
 
     public void accept(Projectile p) {
         p.attack(this);
     }
 
 
     public Sound getSound(){
         return mySound;
     }
 
     public void visit(IAttackable a){
     	try {
             myAttackStrategy.attack(a);
         }
         catch (CloneNotSupportedException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
             
     }
 
 
     /**
      * Sets the attack strategy for an interactive. Can set the interactive
      * to CanAttack or to CannotAttack and then can specify how it would
      * attack.
      * 
      * @param newStrategy is the new attack strategy that the interactive
      *        will have
      */
     public void setAttackStrategy(AttackStrategy newStrategy){
         myAttackStrategy = newStrategy;
     }
     /**
      * Returns the current attack strategy of the interactive
      * 
      * @return the current attack strategy
      */
     public AttackStrategy getAttackStrategy () {
         return myAttackStrategy;
     }
 
     /**
      * upgrades the interactive based on the selected upgrade
      * 
      * @param upgradeNode is the upgrade that the interactive will get
      * @throws NoSuchMethodException 
      * @throws InstantiationException 
      * @throws InvocationTargetException 
      * @throws IllegalAccessException 
      * @throws SecurityException 
      * @throws IllegalArgumentException 
      */
     public void upgradeNode (UpgradeNode upgradeNode) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
         upgradeNode.apply(this);
     }
 
     public UpgradeTree getTree(){
         return myUpgradeTree;
     }
 
     public int calculateDamage(int damage) {
         return damage * (1-(myArmor/(myArmor+100)));
     }
 }
