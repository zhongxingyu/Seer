 package fr.binome.elevator.model;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Ordering;
 
 import javax.annotation.Nullable;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static fr.binome.elevator.model.ElevatorResponse.*;
 
 public class CleverElevator extends Elevator {
 
     // TODO: Rethink because of elevator empty + person calls at the current floor
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
 
     @Override
     public void reset(Integer lowerFloor, Integer higherFloor, Integer cabinSize, String cause) {
         super.reset(lowerFloor, higherFloor, cabinSize, cause);
 
         doorsAlreadyOpenAtThisLevel = false;
 
         destinations.clear();
         callsUp.clear();
         callsDown.clear();
 
         for (int i = MIN_LEVEL; i <= MAX_LEVEL; i++) {
             destinations.put(i, false);
             callsUp.put(i, false);
             callsDown.put(i, false);
         }
     }
 
     @Override
     public void go(Integer floorToGo) {
         destinations.put(floorToGo, true);
     }
 
     @Override
     public void call(Integer floorLevel, String way) {
         Map<Integer, Boolean> calls = getCalls(way);
 
         if (calls != null) {
             calls.put(floorLevel, true);
         }
     }
 
     @Override
     public ElevatorResponse nextCommand() {
         if (doorIsOpen()) {
             return closeDoor();
         }
 
         if (doorsMustOpenAtThisLevel() && doorsCanOpenAtThisLevel() && !doorsAlreadyOpenAtThisLevel) {
             return openDoor();
         }
         else {
             return nextWay();
         }
     }
 
     @VisibleForTesting
     boolean doorsCanOpenAtThisLevel() {
        return destinations.get(currentLevel) || cabinPersonCount < CABIN_SIZE;
     }
 
     @VisibleForTesting
     boolean doorsMustOpenAtThisLevel() {
         Map<Integer, Boolean> calls = getCalls(way);
         boolean mustOpen;
 
         // Special case if we are at the maximum level or minimum level:
         //  in that case we open the doors to passengers going in the other way (they can only go one way)
         if ((way == DOWN && currentLevel == getLowestCallLevel()) || (way == UP && currentLevel == getHighestCallLevel())) {
             mustOpen = hasAtLeastOneCallNoMatterWay(currentLevel);
         }
         else {
             mustOpen = (calls != null && calls.get(currentLevel));
         }
 
         return (destinations.get(currentLevel) || mustOpen);
     }
 
     @VisibleForTesting
     ElevatorResponse nextWay() {
         doorsAlreadyOpenAtThisLevel = false;
 
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
 
     @VisibleForTesting
     Integer getHighestCallLevel() {
         List<Integer> allCalls = Lists.newArrayList();
 
         allCalls.addAll(filterTrue(destinations));
         allCalls.addAll(filterTrue(callsUp));
         allCalls.addAll(filterTrue(callsDown));
 
         return allCalls.isEmpty() ? MAX_LEVEL : Ordering.natural().max(allCalls);
     }
 
     @VisibleForTesting
     Integer getLowestCallLevel() {
         List<Integer> allCalls = Lists.newArrayList();
 
         allCalls.addAll(filterTrue(destinations));
         allCalls.addAll(filterTrue(callsUp));
         allCalls.addAll(filterTrue(callsDown));
 
         return allCalls.isEmpty() ? MIN_LEVEL : Ordering.natural().min(allCalls);
     }
 
     private List<Integer> filterTrue(final Map<Integer, Boolean> calls) {
 
         return Lists.newArrayList(Iterables.filter(calls.keySet(), new Predicate<Integer>() {
             @Override
             public boolean apply(@Nullable Integer level) {
                 return calls.get(level);
             }
         }));
     }
 
     private Map<Integer, Boolean> getCalls(String way) {
         if (ElevatorResponse.valueOf(way) == UP) {
             return callsUp;
         }
 
         if (ElevatorResponse.valueOf(way) == DOWN) {
             return callsDown;
         }
 
         return null;
     }
 
     private Map<Integer, Boolean> getCalls(ElevatorResponse inWay) {
         return getCalls(inWay.name());
     }
 
     private void adjustLevel() {
         if (way == UP) {
             currentLevel++;
         }
         else if (way == DOWN) {
             currentLevel--;
         }
     }
 
     private boolean hasAtLeastOneCallNoMatterWay(int level) {
         return (callsUp.get(level) || callsDown.get(level));
     }
 
     private boolean needToGoUp() {
         return (atLeastOneHeadedUp() || atLeastOneCallHigher());
     }
 
     private boolean needToGoDown() {
         return (atLeastOneHeadedDown() || atLeastOneCallLower());
     }
 
     private boolean atLeastOneHeadedUp() {
         boolean res = false;
 
         for (int i = currentLevel + 1; i <= MAX_LEVEL; i++) {
             res = res || destinations.get(i);
         }
 
         return res;
     }
 
     private boolean atLeastOneHeadedDown() {
         boolean res = false;
 
         for (int i = currentLevel - 1; i >= MIN_LEVEL; i--) {
             res = res || destinations.get(i);
         }
 
         return res;
     }
 
     private boolean atLeastOneCallHigher() {
         boolean res = false;
 
         for (int i = currentLevel + 1; i <= MAX_LEVEL; i++) {
             res = res || hasAtLeastOneCallNoMatterWay(i);
         }
 
         return res;
     }
 
     private boolean atLeastOneCallLower() {
         boolean res = false;
 
         for (int i = currentLevel - 1; i >= MIN_LEVEL; i--) {
             res = res || hasAtLeastOneCallNoMatterWay(i);
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
