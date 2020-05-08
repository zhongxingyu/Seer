 package aigilas.skills;
 
 import aigilas.GameplayLogger;
 import aigilas.creatures.BaseCreature;
 import sps.bridge.Command;
 import sps.util.MathHelper;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class SkillPool {
     private final List<SkillId> _skills = new ArrayList<SkillId>();
     private int _currentSkillSlot = 0;
     private final BaseCreature _owner;
     private final HashMap<SkillId, Integer> _usageCounter = new HashMap<SkillId, Integer>();
     private final HashMap<Command, SkillId> _hotSkills = new HashMap<Command, SkillId>();
 
     public SkillPool(BaseCreature owner) {
         _owner = owner;
     }
 
     public void add(SkillId skill) {
         if (skill == null && _skills.size() == 0) {
             _skills.add(SkillId.No_Skill);
             findCurrent();
             return;
         }
         if (!_skills.contains(skill)) {
             _skills.add(skill);
         }
         if (_skills.contains(SkillId.No_Skill)) {
             _skills.remove(SkillId.No_Skill);
             _currentSkillSlot = _skills.indexOf(skill);
         }
     }
 
     private SkillId findCurrent() {
         normalizeSlot();
         return _skills.get(_currentSkillSlot);
     }
 
     public void add(List<SkillId> getLevelSkills) {
         if (getLevelSkills.size() == 0) {
             _skills.add(SkillId.No_Skill);
             return;
         }
         for (SkillId skill : getLevelSkills) {
             if (!_skills.contains(skill)) {
                 _skills.add(skill);
             }
         }
     }
 
     public void cycle(int velocity) {
         _currentSkillSlot += velocity;
         normalizeSlot();
         findCurrent();
     }
 
     private void normalizeSlot() {
        _currentSkillSlot = MathHelper.clamp(_currentSkillSlot, 0, _skills.size() - 1);
     }
 
     public String getActiveName() {
         return _skills.size() > 0 ? findCurrent().Info.Name : SkillId.No_Skill.Info.Name;
     }
 
     private void removeNone() {
         _skills.remove(SkillId.No_Skill);
     }
 
     public void useActive() {
         if (findCurrent() == SkillId.No_Skill) {
             removeNone();
             _currentSkillSlot = 0;
         }
         if (_skills.size() > 0) {
             useSkill(findCurrent());
         }
     }
 
     private void useSkill(SkillId skillId) {
         SkillFactory.activateIfAble(findCurrent(), _owner);
         GameplayLogger.log(_owner.toString() + " used " + findCurrent().toString());
         if (!_usageCounter.containsKey(skillId)) {
             _usageCounter.put(skillId, 0);
         }
         _usageCounter.put(skillId, _usageCounter.get(skillId) + 1);
     }
 
     SkillId _leastUsed;
 
     public void removeLeastUsed() {
         for (SkillId skillId : _skills) {
             if (!_usageCounter.containsKey(skillId)) {
                 _usageCounter.put(skillId, 0);
             }
         }
         _leastUsed = null;
         for (SkillId key : _usageCounter.keySet()) {
             if ((_leastUsed == null || _leastUsed == SkillId.Forget_Skill || _usageCounter.get(key) < _usageCounter.get(_leastUsed)) && key != SkillId.Forget_Skill) {
                 _leastUsed = key;
             }
         }
         if (_leastUsed != SkillId.Forget_Skill) {
             _skills.remove(_leastUsed);
         }
     }
 
     public int count() {
         return _skills.size();
     }
 
     public void makeActiveSkillHot(Command hotSkillSlot) {
         if (!_hotSkills.containsKey(hotSkillSlot)) {
             _hotSkills.put(hotSkillSlot, findCurrent());
         }
         _hotSkills.put(hotSkillSlot, findCurrent());
     }
 
     public boolean setHotSkillsActive(Command hotkey) {
         if (_hotSkills.containsKey(hotkey)) {
             for (int ii = 0; ii < _skills.size(); ii++) {
                 if (_skills.get(ii) == _hotSkills.get(hotkey)) {
                     _currentSkillSlot = ii;
                     findCurrent();
                     return true;
                 }
             }
         }
         return false;
     }
 
     public String getHotSkillName(Command hotSkillSlot) {
         if (_hotSkills.containsKey(hotSkillSlot)) {
             return _hotSkills.get(hotSkillSlot).Info.Name;
         }
         return SkillId.No_Skill.Info.Name;
     }
 
     public float getCurrentCost() {
         return SkillFactory.getCost(findCurrent());
     }
 
     public SkillId getActive() {
         return findCurrent();
     }
 }
