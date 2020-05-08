 package aigilas.skills.impl;
 
 import aigilas.creatures.BaseCreature;
 import aigilas.skills.AnimationType;
 import aigilas.skills.BaseSkill;
 import aigilas.skills.SkillId;
 
 public class FlameHammerSkill extends BaseSkill {
     public FlameHammerSkill() {
         super(SkillId.Flame_Hammer, AnimationType.ROTATE);
     }
 
     @Override
     public void affect(BaseCreature target) {
         target.applyDamage(_id.Info.Magnitude, _source);

     }
 }
