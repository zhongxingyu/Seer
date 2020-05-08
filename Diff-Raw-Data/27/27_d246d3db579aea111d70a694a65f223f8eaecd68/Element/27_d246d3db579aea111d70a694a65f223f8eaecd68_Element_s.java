 package bomberman.server.elements;
 
 import bomberman.server.Server;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public abstract class Element {
 
     protected int x;
     protected int y;
     protected boolean active = true;
     protected boolean breakable = true;
     protected boolean walkable = false;
     private int rebirth_delay = 10000;
 
     public static Map export(Element element) throws Exception {
         if (element == null || !element.active) {
             return null;
         }
         Map data = new HashMap();
 
         if (element instanceof Wall) {
             data.put("type", "wall");
         } else if (element instanceof Bomb) {
             data.put("type", "bomb");
         }
         data.put("breakable", element.isBreakable());
         data.put("walkable", element.isWalkable());
 
         if (data.isEmpty()) {
             throw new Exception("Unknown Element");
         }
         return data;
 
     }
 
     public void setX(int x) {
         this.x = x;
     }
 
     public void setY(int y) {
         this.y = y;
     }
 
     public int getX() {
         return this.x;
     }
 
     public int getY() {
         return this.y;
     }
 
     public void setBreakable(boolean breakable) {
         this.breakable = breakable;
     }
 
     public boolean isBreakable() {
         return this.breakable;
     }
 
     public boolean isWalkable() {
         return this.walkable;
     }
 
     public void setActive(boolean active) {
         this.active = active;
     }
 
     public boolean isActive() {
         return this.active;
     }
 
     public void burn() {
         this.setActive(false);
        this.delayReborn();
     }
 
     public void setRebirthDelay(int delay) {
         this.rebirth_delay = delay;
     }
 
     public int getRebirthDelay() {
         return this.rebirth_delay;
     }
 
    public void delayReborn() {
         final Element element = this;
         new Thread(new Runnable() {
 
             public void run() {
                 try {
                     if (!active) {
                         Thread.sleep(rebirth_delay);
                         List<Integer> positions = Server.getPlayersPositions();
                        if (!positions.contains(x + Server.board.getCols() * y) && !Server.board.isSquareOnFire(x, y)) {
                             setActive(true);
                             List element_to_add = new ArrayList();
                             element_to_add.add(Element.export(element));
                             element_to_add.add(x);
                             element_to_add.add(y);
                             Server.sendAll("add_element", element_to_add);
                         } else {
                            element.delayReborn();
                         }
                     }
                 } catch (Exception e) {
                     System.out.println(e.getMessage());
                 }
             }
         }).start();
     }
 }
