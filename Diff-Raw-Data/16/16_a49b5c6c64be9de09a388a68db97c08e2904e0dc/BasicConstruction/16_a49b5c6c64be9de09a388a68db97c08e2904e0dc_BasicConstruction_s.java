 package Project.Game.Behaviours.Constructive;
 
 import Project.Game.Entities.Entity;
 import Project.Game.Faction;
 import Project.Game.Resource.ResourceDrain;
 import Project.Game.Resource.ResourcePool;
 
 /**
  *
  */
 public abstract class BasicConstruction implements Construction {
 
     protected ResourcePool resourcePool;
     protected ResourceDrain resourceDrain;
 
     protected int resource = 0;
 
     private Entity entity;
     protected Faction faction;
 
     protected BasicConstruction(Faction faction, Entity entity, ResourcePool resourcePool) {
         this.faction = faction;
         this.entity = entity;
         this.resourcePool = resourcePool;
     }
 
     @Override
     public void update() {
 
     }
 
     @Override
     public Entity getEntity() {
         return entity;
     }
 }
