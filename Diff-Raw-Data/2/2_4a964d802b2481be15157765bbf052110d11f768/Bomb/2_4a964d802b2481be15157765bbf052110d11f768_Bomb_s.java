 package bomberman.server.elements;
 
 import bomberman.server.Server;
 import bomberman.server.ServerThread;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class Bomb extends Element {
 
     private int sleeping_time = 4000;
     private int client_id;
     private boolean burst_ok = false;
     private List<Element> burning_list = new ArrayList<Element>();
     private List<Integer> fire = new ArrayList<Integer>();
 
     public void setSleepingTime(int sleeping_time) {
         this.sleeping_time = sleeping_time;
     }
 
     public int getSleepingTime() {
         return this.sleeping_time;
     }
 
     public void setClientId(int client_id) {
         this.client_id = client_id;
     }
 
     @Override
     public void burn() {
         this.burst();
 
         List element_to_del = new ArrayList();
         element_to_del.add(x);
         element_to_del.add(y);
         Server.sendAll("del_element", element_to_del);
     }
 
     public void delayBurst() {
         new Thread(new Runnable() {
 
             public void run() {
                 try {
                     Thread.sleep(sleeping_time);
                     burn();
                 } catch (InterruptedException e) {
                     System.out.println(e.getMessage());
                 }
             }
         }).start();
     }
 
     public void burst() {
         if (burst_ok) {
             return;
         }
         burst_ok = true;
 
         HashMap<Integer, ServerThread> players_threads = Server.getPlayersThreads();
 
         int index = this.x + Server.board.getCols() * this.y;
         Server.board.setElement(index, null);
         this.fire.add(index);
         for (ServerThread thread : players_threads.values()) {
             if (thread.getPostionX() == this.x && thread.getPositionY() == this.y) {
                 Server.killPlayer(thread.getClientId());
             }
         }
 
         for (int i = this.x + 1; i < Server.board.getCols(); i++) {
             if (!this.checkSquare(i, this.y)) {
                 break;
             }
         }
         for (int i = this.x - 1; i >= 0; i--) {
             if (!this.checkSquare(i, this.y)) {
                 break;
             }
         }
         for (int i = this.y + 1; i < Server.board.getRows(); i++) {
             if (!this.checkSquare(this.x, i)) {
                 break;
             }
         }
         for (int i = this.y - 1; i >= 0; i--) {
             if (!this.checkSquare(this.x, i)) {
                 break;
             }
         }
 
         for (Element element : this.burning_list) {
             element.burn();
         }
 
         Server.board.addFire(this.fire);
         Server.sendAll("burst_bomb", this.fire);
 
         Server.getPlayersThreads().get(this.client_id).decreaseNbBombs();
     }
 
     private boolean checkSquare(int i, int j) {
         HashMap<Integer, ServerThread> players_threads = Server.getPlayersThreads();
         int index = i + Server.board.getCols() * j;
 
         Element element = Server.board.getElements().get(index);
         if (element != null) {
             if (!element.isBreakable()) {
                 return false;
             }
             if (element instanceof Bomb) {
                 element.burn();
             } else {
                 this.burning_list.add(element);
                 this.fire.add(index);
                 return false;
             }
         }
         this.fire.add(index);
 
         for (ServerThread thread : players_threads.values()) {
            if (thread.getPostionX() == i && thread.getPositionY() == this.y) {
                 Server.killPlayer(thread.getClientId());
             }
         }
         return true;
     }
 }
