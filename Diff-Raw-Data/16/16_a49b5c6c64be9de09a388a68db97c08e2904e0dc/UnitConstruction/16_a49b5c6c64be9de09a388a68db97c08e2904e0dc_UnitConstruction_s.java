 package Project.Game.Behaviours.Constructive;
 
 import Project.Game.AI.SPL.Orders.AttackOrder;
 import Project.Game.AI.SPL.SPLQueue;
 import Project.Game.Entities.Entity;
 import Project.Game.Entities.EntityFactory;
 import Project.Game.Entities.Meta.Group;
 import Project.Game.Faction;
 import Project.Game.Main;
 import Project.Game.Resource.ResourceDrain;
 import Project.Game.Resource.ResourcePool;
 import Project.Game.Vector2D;
 
 /**
  * Builds units - better than SimpleConstruction
  */
 public class UnitConstruction extends BasicConstruction {
 
     // Current state for the object
     private UnitState state;
     private SPLQueue splQueue;
 
     private BuildOrder buildOrder = null;
 
     public UnitConstruction(Entity entity, Faction faction, ResourcePool resourcePool) {
         super(faction, entity, resourcePool);
 
         state = UnitState.Waiting;
         splQueue = faction.getSplQueue();
     }
 
     @Override
    public Vector2D getSpawnPoint() {
        return null;
    }

    @Override
     public void update() {
 
         switch (state) {
             case Waiting:
                 // check faction for new order to complete
 
                 if (splQueue.hasAttackOrder()) {
                     // Consume the order
                     AttackOrder order = splQueue.getNextAttackOrder();
                     buildOrder = new BuildOrder(order.getNumberOfUnits(), order.getLocation(), faction, getSpawnPoint());
                     state = UnitState.Building;
                     resourceDrain = new ResourceDrain(resourcePool, 1);
                 }
                 break;
             case Building:
                 // Complete next stage of construction for the current order
                 resource += resourceDrain.claimResource();
                 if (resource > 30) {
                     resource -= 30;
                     Entity entity = EntityFactory.getGroupedEntity(faction, buildOrder.group, getSpawnPoint(), 2);
                     buildOrder.group.addEntity(entity);
                     Main.GAME_LOOP.addEntity(entity);
                 }
                 // If group full, then we have built it all
                 if (buildOrder.group.isFull()) state = UnitState.AllBuilt;
                 break;
             case AllBuilt:
                 // Send off the group
                 buildOrder.group.switchToFollow(buildOrder.targetLocation);
                 // Clear and reset the variables ready for the next order
                 resourceDrain.deRegister();
                 resourceDrain = null;
                 buildOrder = null;
 
                 // switch state to waiting
                 state = UnitState.Waiting;
                 break;
         }
     }
 }
 
 enum UnitState {
     Waiting, Building, AllBuilt;
 }
 
 /**
  * Builds and registers a group, stores information from SPL to be used once group is full
  */
 class BuildOrder {
     // Number of units needed to fulfill the order
     int numberToBuild;
     // Group to store units in for the order
     Group group;
     // Location to send the units to in the end
     Vector2D targetLocation;
 
     BuildOrder(int numberToBuild, Vector2D targetLocation, Faction faction, Vector2D startLocation) {
         this.numberToBuild = numberToBuild;
         this.targetLocation = targetLocation;
 
         group = new Group(faction.getR(), faction.getG(), faction.getB(), startLocation, numberToBuild, faction);
         // Register the group
         Main.GAME_LOOP.addEntity(group);
 
     }
 }
