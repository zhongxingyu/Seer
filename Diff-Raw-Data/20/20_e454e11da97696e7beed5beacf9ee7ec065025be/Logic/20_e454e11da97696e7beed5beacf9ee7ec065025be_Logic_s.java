 package com.group7.dragonwars.engine;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.PriorityQueue;
 import java.util.Set;
 
 import android.util.Log;
 
 /* Class containing things like damage calculation and path finding. */
 public class Logic {
 
     public List<Position> findPath(GameMap map, Unit unit, Position destination) {
         return AStar(map, unit, destination);
     }
    
     public List<Position> destinations(GameMap map, Unit unit) {
     	// return AStar(map, unit, null, false);
	return List<Position>(0);
     }
 
     public Pair<Double, Double> calculateDamage(GameMap map, Unit attacker,
             Unit defender) {
         return new Pair<Double, Double>
                (calculateRawDamage(map, attacker, defender),
                 calculateCounterDamage(map, attacker, defender));
     }
 
     public Double calculateRawDamage(GameMap map, Unit attacker, Unit defender) {
         final Double DEFENDER_DISADVANTAGE = 0.75;
         GameField attackerField = map.getField(attacker.getPosition());
         GameField defenderField = map.getField(defender.getPosition());
 
         Double attackerMod = attackerField.getAttackModifier();
         Double defenderMod = defenderField.getDefenseModifier();
 
         Double defense = defender.getHealth() * (defenderMod / 100);
         defense *= DEFENDER_DISADVANTAGE;
 
         Double rawDamage = attacker.getHealth() * (attackerMod / 100);
 
         Double damage = rawDamage - defense;
 
         return (defense < 0) ? 0 : damage;
     }
 
     public Double calculateCounterDamage(GameMap map, Unit attacker, Unit defender) {
         Double initialDamage = calculateRawDamage(map, attacker, defender);
         Double defenderHealth = defender.getHealth() - initialDamage;
         defenderHealth = (defenderHealth < 0) ? 0 : defenderHealth;
 
         return calculateTheoreticalCounterDamage(map, defender, attacker,
                 defenderHealth);
     }
 
     private Double calculateTheoreticalCounterDamage(GameMap map, Unit attacker,
             Unit defender, Double atkHealth) {
         /* No defense disadvantage on a counter. */
         GameField attackerField = map.getField(attacker.getPosition());
         GameField defenderField = map.getField(defender.getPosition());
 
         Double attackerMod = attackerField.getAttackModifier();
         Double defenderMod = defenderField.getDefenseModifier();
 
         Double defense = defender.getHealth() * (defenderMod / 100);
 
         Double rawDamage = atkHealth * (attackerMod / 100);
 
         Double damage = rawDamage - defense;
 
         return (defense < 0) ? 0 : damage;
     }
 
     private List<Position> AStar(GameMap map, Unit unit, Position destination) {
     	// setting path to true will cause the original behaviour, the path between unit and destination will be returned
     	// otherwise destination is ignored and the list of possible destinations for unit (based on the unit's maxMovement) is instead returned
         if (!map.isValidField(destination)) {
             List<Position> dummy = new ArrayList<Position>(0);
             return dummy;
         }
 
         Set<Position> expanded = new HashSet<Position>();
         Comparator<Pair<List<Position>, Double>> comp = new AStarComparator();
         PriorityQueue<Pair<List<Position>, Double>> queue = new PriorityQueue<Pair<List<Position>, Double>>(
             10, comp);
         List<Position> root = new ArrayList<Position>();
         root.add(unit.getPosition());
 
         queue.add(new Pair<List<Position>, Double>(root, 0.0));
 
         while (queue.size() != 0) {
             Pair<List<Position>, Double> posP = queue.poll();
             List<Position> poss = posP.getLeft();
            
              //String debug = "[ "; for (Position p : poss) { debug +=
              //p.toString() + " "; } debug += "]"; Log.v(null, debug);
             
             Position lastPos = poss.get(poss.size() - 1);
 
            if (lastPos.equals(destination)) 
                 return poss;
            
             if (expanded.contains(lastPos))
                 continue;
            
             expanded.add(lastPos);
             /* Get heuristic */
             Integer h = getManhattanDistance(lastPos, destination);
             /* Get cost */
             Double g = getMovementCost(map, unit, lastPos);
             Double pathCost = h + g + posP.getRight();
 
             if (pathCost > unit.getRemainingMovement())
             	continue;
 
             for (Position p : getAdjacentPositions(lastPos)) {
                 if (map.isValidField(p) && map.getField(p).doesAcceptUnit(unit)) {
                     if (!map.getField(p).hostsUnit()) {
                         Player op = map.getField(p).getUnit().getOwner();
 
                         if (!op.equals(unit.getOwner()))
                             continue;
                     }
 
                     List<Position> plan = new ArrayList<Position>(poss);
                     plan.add(p);
                     queue.add(new Pair<List<Position>, Double>(plan, pathCost));
                 }
             }
         }
 
 	List<Position> dummy = new ArrayList<Position>();
 	return dummy; /* Search failed */
     }
 
     private List<Position> getAdjacentPositions(Position pos) {
         List<Position> positions = new ArrayList<Position>();
         positions.add(new Position(pos.getX(), pos.getY() + 1));
         positions.add(new Position(pos.getX(), pos.getY() - 1));
         positions.add(new Position(pos.getX() + 1, pos.getY()));
         positions.add(new Position(pos.getX() - 1, pos.getY()));
         return positions;
     }
 
     private class AStarComparator implements
         Comparator<Pair<List<Position>, Double>> {
         public int compare(Pair<List<Position>, Double> p1,
                            Pair<List<Position>, Double> p2) {
             if (p1.getRight() < p2.getRight())
                 return -1;
 
             if (p1.getRight() > p2.getRight())
                 return 1;
 
             return 0;
         }
     }
 
     private Double getMovementCost(GameMap map, Unit unit, Position origin) {
         /* g(x) for search */
         // flying units ignore this; always 1
         return (100 / map.getField(origin).getMovementModifier());
     }
 
     public Set<Position> getAttackableUnitPositions(GameMap map, Unit unit) {
         Set<Position> atkFields = getAttackableFields(map, unit);
         Set<Position> atkUnits = new HashSet<Position>();
 
         for (Position p : atkFields) {
             if (map.getField(p).hostsUnit()) {
                 Player uOwner = map.getField(p).getUnit().getOwner();
 
                 if (!uOwner.equals(unit.getOwner()))
                     atkUnits.add(p);
             }
         }
 
         return atkUnits;
     }
 
     private Set<Position> getAttackableFields(GameMap map, Unit unit) {
         if (!unit.isRanged())
             return getPositionsInRange(map, unit.getPosition(), 1.0);
 
         RangedUnit ru = (RangedUnit) unit;
         return getPositionsInRange(map, ru.getPosition(), ru.getMinRange(),
                                    ru.getMaxRange());
     }
 
     private Set<Position> getPositionsInRange(GameMap map, Position origin,
             Double range) {
         Set<Position> positions = new HashSet<Position>();
         Double maxr = Math.ceil(range);
 
         for (Integer x = 0; x < maxr * 2; x++) {
             for (Integer y = 0; y < maxr * 2; y++) {
                 Position newP = new Position(x, y);
 
                 // Pair<Integer, Integer> dist = getManhattanDistance(origin,
                 // newP);
                 if (x < maxr)
                     newP = new Position(newP.getX() - x, newP.getY());
                 else if (x > maxr)
                     newP = new Position(newP.getX() + x, newP.getY());
 
                 if (y < maxr)
                     newP = new Position(newP.getX(), newP.getY() - x);
                 else if (y > maxr)
                     newP = new Position(newP.getX(), newP.getY() + y);
 
                 if (newP.equals(origin) || !map.isValidField(newP))
                     continue;
 
                 positions.add(newP);
 
             }
         }
 
         return positions;
     }
 
     private Set<Position> getPositionsInRange(GameMap map, Position origin,
             Double minRange, Double maxRange) {
         Set<Position> positions = getPositionsInRange(map, origin, maxRange);
         Set<Position> filtered = new HashSet<Position>();
 
         for (Position p : positions) {
             Pair<Integer, Integer> dist;
             dist = getDistanceAway(origin, p);
 
             if (Math.hypot(dist.getLeft(), dist.getRight()) < minRange)
                 continue;
 
             filtered.add(p);
         }
 
         return filtered;
     }
 
     /* Used as a heuristic for A* */
     private Integer getManhattanDistance(Position origin, Position destination) {
         /* h(x) */
         Pair<Integer, Integer> distance = getDistanceAway(origin, destination);
 
         return distance.getLeft() + distance.getRight();
     }
 
     private Pair<Integer, Integer> getDistanceAway(Position origin,
             Position destination) {
         Integer x = Math.abs(origin.getX() - destination.getX());
         Integer y = Math.abs(origin.getY() - destination.getY());
         return new Pair<Integer, Integer>(x, y);
     }
 
 }
