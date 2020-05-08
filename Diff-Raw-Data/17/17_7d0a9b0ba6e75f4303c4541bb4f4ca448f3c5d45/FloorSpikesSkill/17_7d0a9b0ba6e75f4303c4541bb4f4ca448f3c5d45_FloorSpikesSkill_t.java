 package aigilas.skills.impl;
 
 import aigilas.creatures.BaseCreature;
 import aigilas.skills.AnimationType;
 import aigilas.skills.BaseSkill;
 import aigilas.skills.SkillId;
 
 public class FloorSpikesSkill extends BaseSkill {
    public FloorSpikesSkill() {
         super(SkillId.Floor_Spikes, AnimationType.STATIONARY, Float.MAX_VALUE, true);
     }
 
     @Override
    public void affect(BaseCreature target) {
        target.applyDamage(_id.Info.Magnitude, _source);
     }
 }
