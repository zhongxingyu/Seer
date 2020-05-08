 package fr.utbm.vi51.environment;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.janusproject.kernel.agent.Agent;
 import org.janusproject.kernel.status.Status;
 import org.janusproject.kernel.status.StatusFactory;
 
 import fr.utbm.vi51.agent.Insect;
 import fr.utbm.vi51.configs.Consts;
 
 /**
  * @author Top-K
  * 
  */
 public final class Environment extends Agent {
 
     private static Environment evt;
 
     private List<WorldObject> objects;
     private List<Insect> insects;
     private Square[][][] map;
     // width = x, height = y, depth = z
     private final int mapWidth;
     private final int mapHeight;
     private final int mapDepth;
     private Logger log = Logger.getLogger(Environment.class.getName());
     private long lastTime;
 
     private Environment() {
         mapHeight = 25;
         mapWidth = 25;
         mapDepth = 5;
         map = new Square[mapWidth][mapHeight][mapDepth];
         objects = new LinkedList<WorldObject>();
         insects = new LinkedList<Insect>();
     }
 
     public static Environment getInstance() {
         if (evt == null) {
             evt = new Environment();
         }
 
         return evt;
     }
 
     public int getMapWidth() {
         return mapWidth;
     }
 
     public int getMapHeight() {
         return mapHeight;
     }
 
     public int getMapDepth() {
         return mapDepth;
     }
 
     @Override
     public Status activate(Object... params) {
         return StatusFactory.ok(this);
     }
 
     public List<WorldObject> getObjects() {
         return objects;
     }
 
     public void setObjects(List<WorldObject> objects) {
         this.objects = objects;
     }
 
     public Square[][][] getMap() {
         return map;
     }
 
     public void setMap(Square[][][] map) {
         this.map = map;
     }
 
     public void addWorldObject(WorldObject wo) {
         synchronized (objects) {
             objects.add(wo);
         }
         map[(int) wo.getPosition().x][(int) wo.getPosition().y][(int) wo
                 .getPosition().z].getObjects().add(wo);
     }
 
     @Override
     public Status live() {
 
         int diffTime = (int) (this.getTimeManager().getCurrentDate().getTime() - lastTime);
         LinkedList<WorldObject> toRemove = new LinkedList<WorldObject>();
         synchronized (objects) {
             for (WorldObject o : objects) {
                 if (o instanceof Body) {
                     Body b = (Body) o;
                     Action a = b.getAction();
                     if (a != null && a.testAction()) {
                         a.doAction();
                         b.setAction(null);
                     }
                    if (a != null && !a.testAction()){
                        b.setAction(null);
                    }
                     if(b instanceof InsectBody) {
                         InsectBody ib = ((InsectBody) b);
                         ib.setHunger(ib.getHunger() + diffTime);
                         if(ib.getHunger() > Consts.MAXHUNGER) {
                             toRemove.add(ib);
                             ib.die();
                         }
                     }
                 }
                 if (o instanceof Pheromone) {
                     ((Pheromone) o).strength -= diffTime;
                     if (((Pheromone) o).strength < 0) {
                         toRemove.add(o);
                     }
                 }
             }
 
         }
         for (WorldObject wo : toRemove) {
             objects.remove(wo);
             map[wo.getPosition().x][wo.getPosition().y][wo.getPosition().z].getObjects().remove(wo);
         }
         lastTime = this.getTimeManager().getCurrentDate().getTime();
         diffTime = (int) (this.getTimeManager().getCurrentDate().getTime() - lastTime);
         try {
             if(30-diffTime > 0){
                 Thread.sleep(30-diffTime);
             }
         } catch (InterruptedException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return null;
     }
 
 }
