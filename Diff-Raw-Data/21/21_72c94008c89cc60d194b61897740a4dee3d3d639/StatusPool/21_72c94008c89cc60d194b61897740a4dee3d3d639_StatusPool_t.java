 package aigilas.statuses;
 
 import aigilas.creatures.BaseCreature;
 import aigilas.creatures.CreatureAction;
 import aigilas.entities.Elements;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class StatusPool {
     private final List<BaseStatus> _statuses = new ArrayList<BaseStatus>();
 
     public boolean allows(CreatureAction action) {
        for (int ii = 0; ii < _statuses.size(); ii++) {
            BaseStatus status = _statuses.get(ii);
            if (status.prevents(action)) {
                 return false;
             }
         }
         return true;
     }
 
     public void add(BaseStatus status) {
         _statuses.add(status);
     }
 
     public void update() {
         for (int ii = 0; ii < _statuses.size(); ii++) {
             _statuses.get(ii).update();
             if (!_statuses.get(ii).isActive()) {
                 _statuses.remove(_statuses.get(ii));
                 ii--;
             }
         }
     }
 
     public void act() {
        for (int ii = 0; ii < _statuses.size(); ii++) {
            BaseStatus status = _statuses.get(ii);
            status.act();
         }
     }
 
     public void passOn(BaseCreature target, StatusComponent componentType) {
        for (int ii = 0; ii < _statuses.size(); ii++) {
            BaseStatus status = _statuses.get(ii);
            status.passOn(target, componentType);
         }
     }
 
     public boolean isElementBlocked(Elements element) {
        for (int ii = 0; ii < _statuses.size(); ii++) {
            BaseStatus status = _statuses.get(ii);
             if (status.isElementBlocked(element)) {
                 return true;
             }
         }
         return false;
     }
 }
