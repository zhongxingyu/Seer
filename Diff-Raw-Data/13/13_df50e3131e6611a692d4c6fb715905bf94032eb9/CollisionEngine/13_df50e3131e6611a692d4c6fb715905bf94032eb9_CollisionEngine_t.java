 package com.gravity.physics;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.newdawn.slick.geom.Shape;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.gravity.map.GameWorld;
 
 public class CollisionEngine {
     
     private final GameWorld gameMap;
     private final Set<Entity> entities;
     private final List<Entity> terrainCallColls;
     private final List<Entity> terrainNoCalls;
     
     public CollisionEngine(GameWorld map) {
         gameMap = map;
         entities = Sets.newIdentityHashSet();
         terrainNoCalls = map.getTerrainEntitiesNoCalls();
         terrainCallColls = map.getTerrainEntitiesCallColls();
     }
     
     public boolean addEntity(Entity entity) {
         return entities.add(entity);
     }
     
     public boolean removeEntity(Entity entity) {
         return entities.remove(entity);
     }
     
     public boolean addTerrainWithCollisions(Entity entity) {
         return terrainCallColls.add(entity);
     }
     
     public boolean removeTerrainWithCollisions(Entity entity) {
         return terrainCallColls.remove(entity);
     }
     
     private static final int PARTS_PER_TICK = 5;
     
     public void update(int ticks) {
         // for each entity, get their desired movement. Then alert
         // entities of any collisions with each other. Re-adjust collisions
         // until everything works out. Then update everybody
         Preconditions.checkArgument(ticks > 0, "Number of ticks must be positive: ticks=" + ticks);
         
         int increment = ticks < PARTS_PER_TICK ? 1 : ticks / PARTS_PER_TICK;
         Map<Entity, List<Collision>> collisions;
         for (int time = increment; time <= ticks; time += increment) {
             // terrain does not change, not calling update on it
             collisions = computeCollisions(increment);
             if (!collisions.isEmpty()) {
                 for (Entry<Entity, List<Collision>> c : collisions.entrySet()) {
                     c.getKey().handleCollisions(increment, c.getValue());
                 }
                 collisions = computeCollisions(increment);
                for (int tries = 0; !collisions.isEmpty() && tries < 5; tries++) {
                     if (!collisions.isEmpty()) {
                         for (Entry<Entity, List<Collision>> c : collisions.entrySet()) {
                             c.getKey().rehandleCollisions(increment, c.getValue());
                         }
                     }
                     collisions = computeCollisions(increment);
                 }
                 if (!collisions.isEmpty()) {
                     throw new RuntimeException("Could not properly rehandle collisions after 3 rehandles!");
                 }
             }
             for (Entity e : entities) {
                 e.tick(increment);
             }
         }
         
     }
     
     // Package private for testing
     Map<Entity, List<Collision>> computeCollisions(int ticks) {
         Map<Entity, List<Collision>> collisions = Maps.newIdentityHashMap();
         Collision coll;
         for (Entity a : entities) {
             for (Entity b : terrainNoCalls) {
                 coll = collisionLines(a, b, ticks);
                 if (coll != null) {
                     addCollisionToMap(collisions, a, coll);
                 }
             }
         }
         for (Entity a : entities) {
             for (Entity b : terrainCallColls) {
                 coll = collisionLines(a, b, ticks);
                 if (coll != null) {
                     addCollisionToMap(collisions, a, coll);
                     addCollisionToMap(collisions, b, coll);
                 }
             }
         }
         //@formatter:off
         /*//No rabbit-on-rabbit business
         for (Entity a : entities) {
             for (Entity b : entities) {
                 if (a != b) {
                     coll = collisionLines(a, b, ticks);
                     if (coll != null) {
                         addCollisionToMap(collisions, a, coll);
                         addCollisionToMap(collisions, b, coll);
                     }
                 }
             }
         }
         */
         //@formatter:on
         
         return collisions;
     }
     
     private void addCollisionToMap(Map<Entity, List<Collision>> colls, Entity ent, Collision coll) {
         if (colls.containsKey(ent)) {
             colls.get(ent).add(coll);
         } else {
             colls.put(ent, Lists.newArrayList(coll));
         }
     }
     
     // Package private for testing
     static boolean isIntersecting(float[] p1, float[] p2, float[] p3, float[] p4) {
         double unknownA, unknownB;
         unknownA = (((p4[0] - p3[0]) * (double) (p1[1] - p3[1])) - ((p4[1] - p3[1]) * (p1[0] - p3[0])))
                 / (((p4[1] - p3[1]) * (p2[0] - p1[0])) - ((p4[0] - p3[0]) * (p2[1] - p1[1])));
         unknownB = (((p2[0] - p1[0]) * (double) (p1[1] - p3[1])) - ((p2[1] - p1[1]) * (p1[0] - p3[0])))
                 / (((p4[1] - p3[1]) * (p2[0] - p1[0])) - ((p4[0] - p3[0]) * (p2[1] - p1[1])));
         
         if (unknownA >= 0 && unknownA <= 1 && unknownB >= 0 && unknownB <= 1) {
             return true;
         }
         return false;
     }
     
     // Package private for testing
     static Collision collisionLines(Entity entityA, Entity entityB, int time) {
         
         Shape a = entityA.getShape(time);
         Shape b = entityB.getShape(time);
         Map<Integer, List<Integer>> aCollisions = Maps.newHashMap();
         Map<Integer, List<Integer>> bCollisions = Maps.newHashMap();
         getShapeIntersections(a, b, aCollisions, bCollisions);
         
         // if true, then bCollisions better be empty as well.
         if (!aCollisions.isEmpty()) {
             Set<Integer> aPoints = Sets.newHashSet();
             Set<Integer> bPoints = Sets.newHashSet();
             getIntersectPoints(a, b, aCollisions, aPoints, bPoints);
             getIntersectPoints(b, a, bCollisions, bPoints, aPoints);
             return new Collision(entityA, entityB, time, aPoints, bPoints);
         }
         
         return null;
     }
     
     public static void getShapeIntersections(Shape a, Shape b, Map<Integer, List<Integer>> aCollisions, Map<Integer, List<Integer>> bCollisions) {
         // @formatter:off
         /*
          * Intersection formula used:
          *      (x4 - x3)(y1 - y3) - (y4 - y3)(x1 - x3)
          * UA = ---------------------------------------
          *      (y4 - y3)(x2 - x1) - (x4 - x3)(y2 - y1)
          *      
          *      (x2 - x1)(y1 - y3) - (y2 - y1)(x1 - x3)
          * UB = ---------------------------------------
          *      (y4 - y3)(x2 - x1) - (x4 - x3)(y2 - y1)
          *      
          * if UA and UB are both between 0 and 1 then the lines intersect.
          * 
          * Source: http://local.wasp.uwa.edu.au/~pbourke/geometry/lineline2d/
          */
         // @formatter:on
         float points[] = a.getPoints(); // (x3, y3) and (x4, y4)
         float thatPoints[] = b.getPoints(); // (x1, y1) and (x2, y2)
         int length = points.length;
         int thatLength = thatPoints.length;
         double unknownA;
         double unknownB;
         
         if (!a.closed()) {
             length -= 2;
         }
         if (!b.closed()) {
             thatLength -= 2;
         }
         
         // x1 = thatPoints[j]
         // x2 = thatPoints[j + 2]
         // y1 = thatPoints[j + 1]
         // y2 = thatPoints[j + 3]
         // x3 = points[i]
         // x4 = points[i + 2]
         // y3 = points[i + 1]
         // y4 = points[i + 3]
         for (int i = 0; i < length; i += 2) {
             int iNext = i + 2;
             if (iNext >= points.length) {
                 iNext = 0;
             }
             
             for (int j = 0; j < thatLength; j += 2) {
                 int jNext = j + 2;
                 if (jNext >= thatPoints.length) {
                     jNext = 0;
                 }
                 
                 unknownA = (((points[iNext] - points[i]) * (double) (thatPoints[j + 1] - points[i + 1])) - ((points[iNext + 1] - points[i + 1]) * (thatPoints[j] - points[i])))
                         / (((points[iNext + 1] - points[i + 1]) * (thatPoints[jNext] - thatPoints[j])) - ((points[iNext] - points[i]) * (thatPoints[jNext + 1] - thatPoints[j + 1])));
                 unknownB = (((thatPoints[jNext] - thatPoints[j]) * (double) (thatPoints[j + 1] - points[i + 1])) - ((thatPoints[jNext + 1] - thatPoints[j + 1]) * (thatPoints[j] - points[i])))
                         / (((points[iNext + 1] - points[i + 1]) * (thatPoints[jNext] - thatPoints[j])) - ((points[iNext] - points[i]) * (thatPoints[jNext + 1] - thatPoints[j + 1])));
                 
                 if (unknownA >= 0 && unknownA <= 1 && unknownB >= 0 && unknownB <= 1) {
                     if (aCollisions.containsKey(i / 2)) {
                         aCollisions.get(i / 2).add(j / 2);
                     } else {
                         aCollisions.put(i / 2, Lists.newArrayList(j / 2));
                     }
                     if (bCollisions.containsKey(j / 2)) {
                         bCollisions.get(j / 2).add(i / 2);
                     } else {
                         bCollisions.put(j / 2, Lists.newArrayList(i / 2));
                     }
                 }
             }
         }
     }
     
     public static void getIntersectPoints(Shape a, Shape b, Map<Integer, List<Integer>> aCollisions, Set<Integer> aPoints, Set<Integer> bPoints) {
         int aLength = a.getPointCount();
         int bLength = b.getPointCount();
         float[] centerB = b.getCenter();
         for (int aLine : aCollisions.keySet()) {
             List<Integer> bLines = aCollisions.get(aLine);
             for (int bLine : bLines) {
                 List<Integer> aAdj;
                 if (bLines.contains((bLine + 1) % bLength)) {
                     // line-vertex collision
                     aPoints.add(aLine);
                     aPoints.add((aLine + 1) % aLength);
                     bPoints.add((bLine + 1) % bLength);
                 } else if (bLines.contains((bLine + 2) % bLength)) {
                     if (isIntersecting(centerB, b.getPoint((bLine + 1) % bLength), a.getPoint(aLine), a.getPoint((aLine + 1) % aLength))) {
                         // line-line collision
                         aPoints.add(aLine);
                         aPoints.add((aLine + 1) % aLength);
                         bPoints.add((bLine + 1) % bLength);
                         bPoints.add((bLine + 2) % bLength);
                     }
                 } else if ((aAdj = aCollisions.get((aLine + 1) % aLength)) != null) {
                     // vertex-vertex collision
                     if (aAdj.contains((bLine + 1) % bLength)) {
                         aPoints.add((aLine + 1) % aLength);
                         bPoints.add((bLine + 1) % bLength);
                     } else if (aAdj.contains((bLine + bLength - 1) % bLength)) {
                         aPoints.add((aLine + 1) % aLength);
                         bPoints.add(bLine);
                     }
                 }
             }
         }
     }
 }
