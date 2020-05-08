 package aigilas.statuses;
 
 import aigilas.creatures.BaseCreature;
 import aigilas.creatures.CreatureAction;
 import aigilas.creatures.StatBuff;
 import aigilas.entities.Elements;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class BaseStatus {
     protected final List<CreatureAction> _prevents = new ArrayList<CreatureAction>();
 
     protected boolean _wasPassed = false;
     protected int _strength = 0;
     protected final int _maxStrength = 100;
     protected boolean _isActive = true;
     protected BaseCreature _target;
     protected StatBuff _buff = null;
     protected boolean _buffMax = false;
     protected final List<Elements> _blockedElements = new ArrayList<Elements>();
     protected Status impl;
 
     protected final HashMap<StatusComponent, List<Status>> _passables = new HashMap<StatusComponent, List<Status>>();
 
     protected BaseStatus(BaseCreature target) {
        this(target, null);
     }
 
     protected BaseStatus(BaseCreature target, Status impl) {
        _target = target;
         this.impl = impl;
        _strength = _maxStrength;
        setup();
     }
 
     public boolean isActive() {
         return _isActive;
     }
 
     public boolean prevents(CreatureAction action) {
         for (CreatureAction prevent : _prevents) {
             if (prevent == action) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean isElementBlocked(Elements element) {
         for (Elements blocked : _blockedElements) {
             if (blocked == element) {
                 return true;
             }
         }
         return false;
     }
 
     public void passOn(BaseCreature target, StatusComponent componentType) {
         if (_passables.containsKey(componentType)) {
             for (Status contagion : _passables.get(componentType)) {
                 StatusFactory.apply(target, contagion);
             }
             _wasPassed = _passables.containsKey(componentType);
         }
 
     }
 
     protected void add(Status statusId, StatusComponent componentType) {
         if (!_passables.containsKey(componentType)) {
             _passables.put(componentType, new ArrayList<Status>());
         }
         _passables.get(componentType).add(statusId);
     }
 
     public void update() {
         if (_isActive) {
             _strength--;
             if (_strength <= 0) {
                 cleanup();
                 _isActive = false;
             }
         }
     }
 
     public void act() {
 
     }
 
     private void cycleBuff() {
         if (_buff != null) {
             _target.addBuff(_buff, _buffMax);
         }
     }
 
     public void setup() {
         cycleBuff();
     }
 
     public void cleanup() {
         cycleBuff();
     }
 }
