 package fr.binome.elevator.model;
 
 import com.google.common.annotations.VisibleForTesting;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import static fr.binome.elevator.model.ElevatorResponse.*;
 
 public class CleverElevator extends Elevator {
 
     /** TODO: Rethink because of elevator empty + person calls at the current floor **/
     private boolean doorsAlreadyOpenAtThisLevel = false;
 
     private Map<Integer, Boolean> destinations = new HashMap<Integer, Boolean>() {{
         for (int i = MIN_LEVEL; i <= MAX_LEVEL; i++) {
             put(i, false);
         }
     }};
 
     private Map<Integer, Boolean> callsUp = new HashMap<Integer, Boolean>() {{
         for (int i = MIN_LEVEL; i <= MAX_LEVEL; i++) {
             put(i, false);
         }
     }};
 
     private Map<Integer, Boolean> callsDown = new HashMap<Integer, Boolean>() {{
         for (int i = MIN_LEVEL; i <= MAX_LEVEL; i++) {
             put(i, false);
         }
     }};
 
     private Map<Integer, Boolean> getCalls(String inWay) {
         if (ElevatorResponse.valueOf(inWay) == UP) {
             return callsUp;
         }
         if (ElevatorResponse.valueOf(inWay) == DOWN) {
             return callsDown;
         }
 
         return null;
     }
 
     private Map<Integer, Boolean> getCalls(ElevatorResponse inWay) {
         return getCalls(inWay.name());
     }
 
     private boolean isCalledBy(int byI) {
         return (callsUp.get(byI) || callsDown.get(byI));
     }
 
     @Override
     public void go(Integer floorToGo) {
         destinations.put(floorToGo, true);
     }
 
     @Override
     public void call(Integer atFloor, String inWay) {
         Map<Integer, Boolean> calls = getCalls(inWay);
         if (calls != null) {
             calls.put(atFloor, true);
         }
     }
 
     @Override
     public ElevatorResponse nextCommand() {
         if (doorIsOpen()) {
             return closeDoor();
         }
 
         if (doorsMustOpenAtThisLevel() && !doorsAlreadyOpenAtThisLevel) {
             return openDoor();
         }
         else {
             return nextWay();
         }
     }
 
     @VisibleForTesting
     boolean doorsMustOpenAtThisLevel() {
         Map<Integer, Boolean> calls = getCalls(way);
        boolean mustOpen = false;
 
         // Special case if we are at the maximum level or minimum level:
         //  in that case we open the doors to passengers going in the other way (they can only go one way)
         if (currentLevel == MIN_LEVEL || currentLevel == MAX_LEVEL) {
             mustOpen = isCalledBy(currentLevel);
         }
         else {
             mustOpen = (calls != null && calls.get(currentLevel));
         }
 
         return (destinations.get(currentLevel) || mustOpen);
     }
 
     @VisibleForTesting
     ElevatorResponse nextWay() {
         doorsAlreadyOpenAtThisLevel = false;
        ElevatorResponse result = NOTHING;
 
         if (way == UP && !needToGoUp()) {
             if (needToGoDown()) {
                 way = DOWN;
             }
             else {
                 way = NOTHING;
             }
         }
         if (way == DOWN && !needToGoDown()) {
             if (needToGoUp()) {
                 way = UP;
             }
             else {
                 way = NOTHING;
             }
         }
         if (way == NOTHING) {
             if (needToGoUp()) {
                 way = UP;
             }
             else if (needToGoDown()) {
                 way = DOWN;
             }
         }
 
         adjustLevel();
 
         return way;
     }
 
     private void adjustLevel() {
         if (way == UP) {
             currentLevel++;
         }
         else if (way == DOWN)  {
             currentLevel--;
         }
     }
 
    private void initWayForNextTick() {
        if (way == UP && currentLevel == MAX_LEVEL) {
            way = DOWN;
        }
        else if (way == DOWN && currentLevel == MIN_LEVEL) {
            way = UP;
        }
    }

     private boolean needToGoUp() {
         return (atLeastOneHeadedUp() || atLeastOneCallHigher());
     }
 
     private boolean needToGoDown() {
         return (atLeastOneHeadedDown() || atLeastOneCallLower());
     }
 
     private boolean atLeastOneHeadedUp() {
         boolean res = false;
         for (int i = currentLevel+1 ; i <= MAX_LEVEL; i++) {
             res = res || destinations.get(i);
         }
 
         return res;
     }
 
     private boolean atLeastOneHeadedDown() {
         boolean res = false;
         for (int i = currentLevel-1 ; i >= MIN_LEVEL; i--) {
             res = res || destinations.get(i);
         }
 
         return res;
     }
 
     private boolean atLeastOneCallHigher() {
         boolean res = false;
         for (int i = currentLevel+1 ; i <= MAX_LEVEL; i++) {
             res = res || isCalledBy(i);
         }
 
         return res;
     }
 
     private boolean atLeastOneCallLower() {
         boolean res = false;
         for (int i = currentLevel-1 ; i >= MIN_LEVEL; i--) {
             res = res || isCalledBy(i);
         }
 
         return res;
     }
 
     private ElevatorResponse openDoor() {
         doorsAlreadyOpenAtThisLevel = true;
 
         destinations.put(currentLevel, false);
         callsUp.put(currentLevel, false);
         callsDown.put(currentLevel, false);
 
         return stateDoors = OPEN;
     }
 
     private ElevatorResponse closeDoor() {
         return stateDoors = CLOSE;
     }
 
     private boolean doorIsOpen() {
         return OPEN == stateDoors;
     }
 
 }
