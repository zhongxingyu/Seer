 package vooga.rts.gamedesign.strategy.upgradestrategy;
 
 import vooga.rts.action.InteractiveAction;
 import vooga.rts.commands.Command;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.InteractiveEntity;
 import vooga.rts.gamedesign.strategy.Strategy;
 import vooga.rts.gamedesign.upgrades.UpgradeNode;
 import vooga.rts.gamedesign.upgrades.UpgradeTree;
 import vooga.rts.util.Information;
 
 
 /**
  * 
  * This class implements UpgradeStrategy and is used as an instance in
  * InteractiveEntity for objects that receive upgrades. This class also
  * contains the UpgradeTree that indicates the sequences of upgrades available
  * for the InteractiveEntity that owns the class.
  * 
  * @author Wenshun Liu
  * 
  */
 public class CanUpgrade implements UpgradeStrategy {
     private UpgradeTree myUpgradeTree;
 
     /**
      * Sets the UpgradeTree of this CanUpgrade strategy. Creates actions for
      * the owner of the UpgradeTree.
      * 
      * @param upgradeTree the UpgradeTree of this CanUpgrade
      * @param owner the InteractiveEntity that owns the UpgradeTree
      */
     public void setUpgradeTree (UpgradeTree upgradeTree,
     		InteractiveEntity owner) {
         myUpgradeTree = upgradeTree;
         createUpgradeActions(owner);
     }
 
     /**
      * Creates the upgrade actions for the InteractiveEntity passed in
      * given the current upgrades available in the UpgradeTree.
      */
     public void createUpgradeActions (final InteractiveEntity entity) {
         for (final UpgradeNode upgrade : myUpgradeTree.getCurrentUpgrades()) {
         	final String commandName = "upgrade " + upgrade.getUpgradeName();
         	entity.addAction(commandName, new InteractiveAction(entity) {
                 @Override
                 public void update (Command command) {
                 }
 
                 @Override
                 public void apply () {
                     upgrade.apply(entity);
                 }
             });
             entity.addActionInfo(commandName, new Information(commandName,
             		"This upgrades " + upgrade.getUpgradeName(),
            		"buttons/unload.gif",null));  
         }
     }
 
     /**
      * Returns the UpgradeTree ties to this CanUpgrade
      * 
      * @return the UpgradeTree ties to this class.
      */
     public UpgradeTree getUpgradeTree () {
         return myUpgradeTree;
     }
 
     /**
      * Applies this CanUpgrade strategy to the InteractiveEntity passed in by
      * setting the strategy and the UpgradeTree for the InteractiveEntity,
      * and recreating the actions.
      * 
      * @param other the InteractiveEntity that will receive the effect of
      * this UpgradeStrategy
      */
     public void copyStrategy (InteractiveEntity other) {
         UpgradeStrategy newUpgrade = new CanUpgrade();
         newUpgrade.setUpgradeTree(getUpgradeTree(), other);
         other.setUpgradeStrategy(newUpgrade);
     }
 
 }
