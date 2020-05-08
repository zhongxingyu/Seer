 package lv.k2611a.domain;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import lv.k2611a.domain.unitgoals.Move;
 import lv.k2611a.domain.unitgoals.UnitGoal;
 import lv.k2611a.util.Point;
 
 public class Unit {
     private int id;
     private int x;
     private int y;
     private UnitType unitType;
     private ViewDirection viewDirection = ViewDirection.TOP;
     private List<UnitGoal> goals = new ArrayList<UnitGoal>();
     private int ticksSpentOnCurrentGoal;
     private int hp;
     private int ownerId;
     private int ticksCollectingSpice;
     private int ticksReloading;
 
     public UnitType getUnitType() {
         return unitType;
     }
 
 
     public void setUnitType(UnitType unitType) {
         this.unitType = unitType;
         this.hp = unitType.getHp();
     }
 
     public int getX() {
         return x;
     }
 
     public void setX(int x) {
         this.x = x;
     }
 
     public int getY() {
         return y;
     }
 
     public void setY(int y) {
         this.y = y;
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public ViewDirection getViewDirection() {
         return viewDirection;
     }
 
     public void setViewDirection(ViewDirection viewDirection) {
         this.viewDirection = viewDirection;
     }
 
     public UnitGoal getCurrentGoal() {
         if (goals.isEmpty()) {
             return null;
         }
         return goals.get(0);
     }
 
     public void insertGoalBeforeCurrent(UnitGoal goal) {
         goals.add(0, goal);
     }
 
     public void setGoal(UnitGoal goal) {
        UnitGoal currentGoal = getCurrentGoal();
         this.goals = new ArrayList<UnitGoal>();

        // finish current move.
        if (currentGoal != null) {
            if (currentGoal instanceof Move) {
                if (ticksSpentOnCurrentGoal > 0) {
                    Move move = new Move(viewDirection.apply(new Point(x, y)));
                    goals.add(move);
                }
            }
         }
         goals.add(goal);
     }
 
     public int getTicksSpentOnCurrentGoal() {
         return ticksSpentOnCurrentGoal;
     }
 
     public void setTicksSpentOnCurrentGoal(int ticksSpentOnCurrentGoal) {
         this.ticksSpentOnCurrentGoal = ticksSpentOnCurrentGoal;
     }
 
     public void removeGoal(UnitGoal goal) {
         this.goals.remove(goal);
     }
 
     public int getHp() {
         return hp;
     }
 
     public int getOwnerId() {
         return ownerId;
     }
 
     public void setOwnerId(int ownerId) {
         this.ownerId = ownerId;
     }
 
     public Point getPoint() {
        return new Point(x, y);
     }
 
     public int getTicksCollectingSpice() {
         return ticksCollectingSpice;
     }
 
     public void setTicksCollectingSpice(int ticksCollectingSpice) {
         this.ticksCollectingSpice = ticksCollectingSpice;
     }
 
     public void clearGoals() {
         this.goals = new ArrayList<UnitGoal>();
     }
 
     public int getTicksReloading() {
         return ticksReloading;
     }
 
     public void setTicksReloading(int ticksReloading) {
         this.ticksReloading = ticksReloading;
     }
 
     public void setHp(int hp) {
         this.hp = hp;
     }
 }
