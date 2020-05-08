 package aigilas.statuses.impl;
 
 import aigilas.creatures.BaseCreature;
 import aigilas.creatures.CreatureAction;
 import aigilas.statuses.BaseStatus;
 
 public class BurnStatus extends BaseStatus {
     public BurnStatus(BaseCreature target)
 
     {
         super(target);
 
         _prevents.add(CreatureAction.Movement);
     }
 
     @Override
     public void update() {
         super.update();
        _target.applyDamage(1);
     }
 }
