 package drinker;
 
 import drinker.WorldObjects.*;
 
 import java.io.PrintStream;
 import java.lang.reflect.Array;
 import java.util.*;
 
 public class World {
 
     public final int width;
     public final int height;
     public final PrintStream stream;
 
     private ArrayList<WorldObject> movableObjects;
     private ArrayList<WorldObject> worldObjects[][];
 
     @SuppressWarnings({"unchecked"})
     public World(int width, int height, PrintStream stream) {
 
         this.width = width;
         this.height = height;
         this.stream = stream;
         this.movableObjects = new ArrayList<WorldObject>();
 
         this.worldObjects = (ArrayList<WorldObject>[][])
                 Array.newInstance(new ArrayList<WorldObject>().getClass(),
                         width, height);
 
         for (int i = 0; i < width; i++) {
             for (int j = 0; j < height; j++) {
                 worldObjects[i][j] = new ArrayList<WorldObject>();
             }
         }
 
         InitObjects();
 
     }
 
     private void InitObjects() {
 
         Lamp l = new Lamp(7, 3);
         this.addObject(l);
         l.switchOn();
 
         for (int i = 0; i < 16; i++) {
             if (i == 9)
                 continue;
             this.addObject(new WhiteSpace(i, 0));
         }
 
         for (int i = 0; i < 16; i++) {
             if (i == 3)
                 continue;
             this.addObject(new WhiteSpace(15, i));
         }
 
         this.addObject(new Pole(7, 7));
         this.addObject(new Tavern(9, 0));
         PoliceStation policeStation = new PoliceStation(15, 3);
 
         this.addObject(new Policeman(l, policeStation,
                 policeStation.getX(), policeStation.getY()));
 
         this.addObject(policeStation);
 
 
     }
 
     private int tickCursor = 0;
     private int tickCount = 0;
 
     public void tick() {
 
         tickCount++;
         tickCursor--;
 
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
 
 
     }
 
     public void drawScene() {
 
         for (int j = 0; j < width; j++) {
             for (int i = 0; i < height; i++) {
                 if (!worldObjects[i][j].isEmpty()) {
                     stream.print(worldObjects[i][j].get(
                             worldObjects[i][j].size() - 1).draw());
                 } else {
                     stream.print(WorldObject.drawDefault());
                 }
             }
             stream.println();
         }
 
     }
 
 
     public void addObject(WorldObject o) {
 
         o.setWorld(this);
         if (o.isMovable())
             movableObjects.add(o);
 
         worldObjects[o.getX()][o.getY()].add(o);
 
     }
 
     public void removeObject(WorldObject o) {
         if (movableObjects.contains(o)) {
             movableObjects.remove(o);
         }
         if (worldObjects[o.getX()][o.getY()].contains(o)) {
             worldObjects[o.getX()][o.getY()].remove(o);
         }
     }
 
     public Collection<WorldObject> getObjectAtXY(int x, int y) {
 
         if (x < 0 || x >= this.width || y < 0 || y >= this.height)
             return null;
 
         if (worldObjects[x][y] == null)
             return null;
 
         return Collections.unmodifiableCollection(worldObjects[x][y]);
 
     }
 
     public boolean isTakePlace(int x, int y) {
 
         Collection<WorldObject> collection = this.getObjectAtXY(x, y);
 
         if (collection == null)
             return true;
 
         for (WorldObject w : collection) {
             if (w.isTakePlace())
                 return true;
         }
 
 
         return false;
 
     }
 
 
     private void addToper() {
         addObject(new Toper(9, 1));
     }
 
     private void tickObject(WorldObject o) {
 
         int x = o.getX();
         int y = o.getY();
 
         o.onTick();
 
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
 
     public Point2D findDirectionOnClosestPath(
             WorldObject start, WorldObject finish) {
 
         if (start.isSameLocation(finish))
             return new Point2D(0, 0);
 
 
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
 
             Point2D nextStep = new Point2D(0, 0);
 
             nextStep.x = -1;
             nextStep.y = 0;
             if (tryAddToQueue(next, nextStep, finish, queue, prevMatrix)) {
                 break;
             }
 
             nextStep.x = 1;
             nextStep.y = 0;
             if (tryAddToQueue(next, nextStep, finish, queue, prevMatrix)) {
                 break;
             }
             
             nextStep.x = 0;
             nextStep.y = 1;
             if (tryAddToQueue(next, nextStep, finish, queue, prevMatrix)) {
                 break;
             }
             
             nextStep.x = 0;
             nextStep.y = -1;
             if (tryAddToQueue(next, nextStep, finish, queue, prevMatrix)) {
                 break;
             }
         }
 
         int nextPrevX = finish.getX();
         int nextPrevY = finish.getY();
 
         Point2D prevPoint = new Point2D();
 
         while (!(nextPrevX == start.getX() && nextPrevY == start.getY())) {
 
             if (prevMatrix[nextPrevX][nextPrevY] == null)
                 return null;
 
             prevPoint.x = nextPrevX;
             prevPoint.y = nextPrevY;
 
             nextPrevX = prevMatrix[prevPoint.x][prevPoint.y].x;
             nextPrevY = prevMatrix[prevPoint.x][prevPoint.y].y;
 
 
         }
 
         return new Point2D(prevPoint.x - start.getX(),
                 prevPoint.y - start.getY());
 
 
     }
 
     private boolean tryAddToQueue(Point2D current,
                                   Point2D step,
                                   WorldObject finish,
                                   Queue<Point2D> queue,
                                   Point2D[][] prevMatrix) {
 
         int newX = current.x + step.x;
         int newY = current.y + step.y;
 
         if (newX == finish.getX() && newY == finish.getY()) {
            prevMatrix[newX][newY] = current.clone();
             return true;
         }
         
         
         if (!isTakePlace(newX, newY) && prevMatrix[newX][newY] == null) {
             queue.add(new Point2D(newX, newY));
            prevMatrix[newX][newY] = current.clone();
         }
 
 
         return false;
 
     }
 
 
 }
