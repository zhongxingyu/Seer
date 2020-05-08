 package drinker;
 
 import drinker.utils.ObjectEventHandler;
 import drinker.utils.Pair;
 import drinker.utils.WorldEvent;
 import drinker.worldObjects.*;
 
 import java.io.PrintStream;
 import java.lang.reflect.Array;
 import java.util.*;
 
 public class World {
 
     public final int width;
     public final int height;
     public final PrintStream stream;
     private List<Point2D> possibleDirections;
 
     // defaults objects
     final Pole pole;
     final Tavern tavern;
     final Lamp lamp;
     final PoliceStation policeStation;
     final Policeman policeman;
     final Beggar beggar;
     final BottleHouse bottleHouse;
 
     private ArrayList<WorldObject> movableObjects;
     private ArrayList<Bottle> bottles;
     private ArrayList<WorldObject> worldObjects[][];
 
     protected WorldEvent onAddObjectEvent = new WorldEvent();
     protected WorldEvent onPreTickEvent = new WorldEvent();
     protected WorldEvent onPostTickEvent = new WorldEvent();
 
     @SuppressWarnings({"unchecked"})
     public World(boolean isHex, PrintStream stream) {
 
         this.width = 16;
         this.height = 17;
         this.stream = stream;
         this.movableObjects = new ArrayList<WorldObject>();
         this.bottles = new ArrayList<Bottle>();
 
 
         this.worldObjects = (ArrayList<WorldObject>[][])
                 Array.newInstance(ArrayList.class, width, height);
 
         possibleDirections = new ArrayList<Point2D>();
         possibleDirections.add(new Point2D(0, 1));
         possibleDirections.add(new Point2D(0, -1));
         possibleDirections.add(new Point2D(1, 0));
         possibleDirections.add(new Point2D(-1, 0));
         if (isHex) {
             possibleDirections.add(new Point2D(1, 1));
             possibleDirections.add(new Point2D(1, -1));
             possibleDirections.add(new Point2D(-1, 1));
             possibleDirections.add(new Point2D(-1, -1));
         }
 
         for (int i = 0; i < width; i++) {
             for (int j = 0; j < height; j++) {
                 worldObjects[i][j] = new ArrayList<WorldObject>();
             }
         }
 
         pole = new Pole(7, 7);
         tavern = new Tavern(9, 0);
         lamp = new Lamp(7, 3);
         policeStation = new PoliceStation(15, 3);
         policeman = new Policeman(lamp, policeStation, policeStation.getX(), policeStation.getY());
         bottleHouse = new BottleHouse(4, 16);
         beggar = new Beggar(bottleHouse, 4, 16);
 
         InitObjects();
 
 
     }
 
     private void InitObjects() {
 
         for (int i = 0; i < width; i++) {
             for (int j = 0; j < height; j++) {
                 this.addObject(new Ground(i, j));
             }
         }
 
 
         this.addObject(lamp);
         lamp.switchOn();
 
         for (int i = 0; i < width; i++) {
             this.addObject(new FieldBorder(i, height - 1));
             if (i == 9)
                 continue;
             this.addObject(new FieldBorder(i, 0));
         }
 
         for (int i = 0; i < height; i++) {
             if (i == 3)
                 continue;
             this.addObject(new FieldBorder(15, i));
         }
 
 
         this.addObject(pole);
         this.addObject(tavern);
         this.addObject(policeStation);
         this.addObject(policeman);
         this.addObject(beggar);
         this.addObject(bottleHouse);
         policeman.bindToLamp();
 
 
     }
 
     private int tickCursor = 0;
     private int tickCount = 0;
 
     public void tick() {
 
         tickCount++;
         tickCursor--;
 
         onPreTickEvent.emit(null);
 
         if (tickCount % 20 == 0) {
             addToper();
             return;
         }
 
         if (movableObjects.isEmpty()) {
             return;
         }
 
         if (tickCursor < 0) {
             tickCursor = movableObjects.size() - 1;
         }
 
         tickObject(movableObjects.get(tickCursor));
 
         onPostTickEvent.emit(null);
 
     }
 
     public void drawScene() {
 
         for (int j = 0; j < height; j++) {
             for (int i = 0; i < width; i++) {
 
                 stream.print(worldObjects[i][j].get(
                         worldObjects[i][j].size() - 1).draw());
 
             }
             stream.println();
         }
 
     }
 
     /**
      * Get possible directions for field
      *
      * @return unmodifiable collection
      */
     public Collection<Point2D> getDirections() {
 
         return Collections.unmodifiableCollection(possibleDirections);
 
     }
 
     public void addObject(WorldObject o) {
 
         // if(o.inc)
         if (o instanceof Bottle) {
             bottles.add((Bottle) o);
         }
 
         o.setWorld(this);
         if (o.isMovable())
             movableObjects.add(o);
 
         worldObjects[o.getX()][o.getY()].add(o);
 
         onAddObjectEvent.emit(o);
 
     }
 
     public void addAddObjectHandler(ObjectEventHandler handler) {
         this.onAddObjectEvent.add(handler);
     }
 
     public void addPreTickEvent(ObjectEventHandler handler) {
         onPreTickEvent.add(handler);
     }
 
     public void addPostTickEvent(ObjectEventHandler handler) {
         onPostTickEvent.add(handler);
     }
 
     public void removeObject(WorldObject o) {
         if (movableObjects.contains(o)) {
             movableObjects.remove(o);
         }
         if (worldObjects[o.getX()][o.getY()].contains(o)) {
             worldObjects[o.getX()][o.getY()].remove(o);
         }
     }
 
     public ArrayList<Bottle> getBottles() {
         return bottles;
     }
 
     public Collection<WorldObject> getObjectAtXY(int x, int y) {
 
         if (x < 0 || x >= this.width || y < 0 || y >= this.height)
             return null;
 
         if (worldObjects[x][y] == null)
             return null;
 
         return Collections.unmodifiableCollection(worldObjects[x][y]);
 
     }
 
 
     private void addToper() {
         addObject(new Toper(this, 9, 1));
     }
 
     private void tickObject(WorldObject o) {
 
         int x = o.getX();
         int y = o.getY();
 
         o.onWorldTick();
 
         int newX = o.getX();
         int newY = o.getY();
 
         if (newX != x || newY != y) {
             worldObjects[x][y].remove(o);
             for (WorldObject wo : worldObjects[newX][newY]) {
                 wo.onEnter(o);
             }
             worldObjects[newX][newY].add(o);
         }
 
         if (!o.isMovable()) {
             movableObjects.remove(o);
         }
 
 
     }
 
     /**
      * Is place free to move for policeman?
      * It reacts almost the same as toper
      *
      * @param x position in the world
      * @param y position in the world
      * @return is position free to move
      */
     public boolean isTakePlace(int x, int y) {
 
         Collection<WorldObject> collection = this.getObjectAtXY(x, y);
 
         if (collection == null) {
             return true;
         }
 
         for (WorldObject w : collection) {
             if (w.isReasonToStopToper() || w.isSuspendAble()) {
                 return true;
             }
         }
 
         return false;
 
     }
 
     public Pair<Point2D, Integer> findDirectionOnClosestPath(
             WorldObject start, WorldObject finish) {
 
         if (start.isSameLocation(finish))
             return new Pair<Point2D, Integer>(new Point2D(0, 0), 0);
 
 
         Queue<Point2D> queue = new
                 ArrayDeque<Point2D>();
 
         queue.add(new Point2D(start.getX(), start.getY()));
 
         Point2D[][] prevMatrix = (Point2D[][])
                 Array.newInstance(
                         (new Point2D(0, 0)).getClass(),
                         width, height);
 
         prevMatrix[start.getX()][start.getY()] = new Point2D(0, 0);
 
         while (!queue.isEmpty()) {
 
             Point2D next = queue.poll();
             boolean foundWay = false;
             for (Point2D nextStep : possibleDirections) {
                 if (foundWay = tryAddToQueue(next, nextStep, finish, queue, prevMatrix)) {
                     break;
                 }
             }
             if (foundWay) {
                 break;
             }
 
         }
 
         int nextPrevX = finish.getX();
         int nextPrevY = finish.getY();
 
         Point2D prevPoint = new Point2D();
         int dist = 0;
 
         if (prevMatrix[nextPrevX][nextPrevY] == null) {
             return null;
         }
 
         while (!(nextPrevX == start.getX() && nextPrevY == start.getY())) {
 
             prevPoint.x = nextPrevX;
             prevPoint.y = nextPrevY;
 
             nextPrevX = prevMatrix[prevPoint.x][prevPoint.y].x;
             nextPrevY = prevMatrix[prevPoint.x][prevPoint.y].y;
             dist++;
 
 
         }
 
         return new Pair<Point2D, Integer>(new Point2D(prevPoint.x - start.getX(),
                 prevPoint.y - start.getY()), dist);
 
 
     }
 
     /**
      * Adds new position if it's new and possible
      *
      * @param current    position
      * @param step       direction to near position
      * @param finish     destination object
      * @param queue      to add explored positions
      * @param prevMatrix to add prev
      * @return true if found way
      */
     private boolean tryAddToQueue(Point2D current,
                                   Point2D step,
                                   WorldObject finish,
                                   Queue<Point2D> queue,
                                   Point2D[][] prevMatrix) {
 
         int newX = current.x + step.x;
         int newY = current.y + step.y;
 
         if (newX == finish.getX() && newY == finish.getY()) {
             prevMatrix[newX][newY] = current.copy();
             return true;
         }
 
 
        if (isPossibleForStep(newX, newY) && !isTakePlace(newX, newY) 
                && prevMatrix[newX][newY] == null) {
             queue.add(new Point2D(newX, newY));
             prevMatrix[newX][newY] = current.copy();
         }
 
 
         return false;
 
     }
 
     public boolean isPossibleForStep(int x, int y) {
 
         Collection<WorldObject> collection = this.getObjectAtXY(x, y);
         if (collection != null) {
             if (collection.size() == 1) {
                 if (collection.iterator().next() instanceof FieldBorder) {
                     return false;
                 }
             }
         }
 
         return !(x >= width - 1 || x < 0 || y >= height - 1 || y < 1);
     }
 
     public boolean isPossibleForStep(Point2D point) {
         return isPossibleForStep(point.x, point.y);
     }
 
     /**
      * Translates coordinates from task to real field coordinates
      * @param x 
      * @param y
      * @return
      */
     public static Point2D fromYXtoWorldFieldCoordinates(int y, int x) {
 
         return new Point2D(x, y + 1);
 
     }
 
 }
