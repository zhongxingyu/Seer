 /**
  *   The MIT License
  *
  *  Copyright 2011 Andrew James <ephphatha@thelettereph.com>.
  *
  *  Permission is hereby granted, free of charge, to any person obtaining a copy
  *  of this software and associated documentation files (the "Software"), to deal
  *  in the Software without restriction, including without limitation the rights
  *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  *  copies of the Software, and to permit persons to whom the Software is
  *  furnished to do so, subject to the following conditions:
  *
  *  The above copyright notice and this permission notice shall be included in
  *  all copies or substantial portions of the Software.
  *
  *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  *  THE SOFTWARE.
  */
 package au.edu.csu.bofsa;
 
 import java.util.List;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.GameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import au.edu.csu.bofsa.Behaviours.CreepFactoryBehaviour;
 import au.edu.csu.bofsa.Behaviours.TowerFactoryBehaviour;
 import au.edu.csu.bofsa.Events.Event;
 import au.edu.csu.bofsa.Events.EventSink;
 import au.edu.csu.bofsa.Signals.Signal;
 
 /**
  * @author ephphatha
  *
  */
 public class InGameStateST implements Comparable<Object>, CreepManager, EventSink, GameState, Runnable {
   private int stateID;
   
   protected GameLevelST map;
   
   protected List<Tower> towers;
   
   protected Queue<Creep> newCreeps;
   protected List<Creep> creeps;
   protected Queue<Creep> deadCreeps;
   
   protected Signal<CopyableDimension> tileSize;
 
   protected Signal<CopyableList<Pipe<CopyableVector2f>>> creepPositions;
 
   protected Thread updateThread;
 
   @SuppressWarnings("unused")
   private InGameStateST() {
     this(0);
   }
   
   public InGameStateST(int id) {
     this.stateID = id;
     
     this.towers = new CopyOnWriteArrayList<Tower>();
     this.creeps = new CopyOnWriteArrayList<Creep>();
     this.deadCreeps = new ConcurrentLinkedQueue<Creep>();
     this.newCreeps = new ConcurrentLinkedQueue<Creep>();
 
     this.creepPositions = new Signal<CopyableList<Pipe<CopyableVector2f>>>(new CopyableList<Pipe<CopyableVector2f>>());
     
     this.tileSize = new Signal<CopyableDimension>(new CopyableDimension(1,1));
   }
 
   @Override
   public int getID() {
     return this.stateID;
   }
 
   @Override
   public void init(GameContainer container, StateBasedGame game)
       throws SlickException {
   }
 
   @Override
   public void enter(GameContainer container, StateBasedGame game)
       throws SlickException {
     CreepFactoryBehaviour.loadResources();
     TowerFactoryBehaviour.loadResources();
     
     try {
       this.map = new GameLevelST("test");
     } catch (SlickException e) {
       e.printStackTrace();
     }
     
     this.updateThread = new Thread(this);
     
     if (this.getClass() == InGameStateST.class) {
       this.updateThread.start();
     }
   }
 
   @Override
   public void leave(GameContainer container, StateBasedGame game)
       throws SlickException {
     this.updateThread.interrupt();
     try {
       this.updateThread.join();
     } catch (InterruptedException e) {
       //Goggles
     }
 
     this.map = null;
 
     this.towers.clear();
     this.creeps.clear();
     this.deadCreeps.clear();
     this.newCreeps.clear();
   }
 
   @Override
   public void render(GameContainer container, StateBasedGame game, Graphics g)
       throws SlickException {
     if (this.map != null) {
       this.tileSize.write(new CopyableDimension(container.getWidth() / this.map.getWidth(), container.getHeight() / this.map.getHeight()));
 
       this.map.render(container, g);
       
       for (Tower t : this.towers) {
         t.draw(g);
       }
       
       for (Creep c : this.creeps) {
         c.draw(g);
       }
     }
   }
 
   @Override
   public void update(GameContainer container, StateBasedGame game, int delta)
       throws SlickException {
     Input input = container.getInput();
 
     Vector2f relativeInput = new Vector2f((float) input.getMouseX() / (float) container.getWidth(),
                                           (float) input.getMouseY() / (float) container.getHeight());
     
     if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
       CopyablePoint towerPos = new CopyablePoint((int) Math.floor(relativeInput.x * this.map.getWidth()),
                                                  (int) Math.floor(relativeInput.y * this.map.getHeight()));
       
       Tower t = this.map.spawnTower(towerPos, this.creepPositions, this.tileSize, this);
       
       if (t != null) {
         this.towers.add(t);
       }
     }
 
     if (input.isKeyPressed(Input.KEY_ESCAPE)) {
       game.enterState(BofSA.States.MAINMENU.ordinal());
     }
   }
 
   public void update(final float delta) {
     // Game logic
     
     this.creeps.addAll(this.newCreeps);
     this.newCreeps.clear();
     
     this.map.update(this, delta);
     
     for (final Tower t : this.towers) {
       t.call();
     }
     
     for (final Creep c : this.creeps) {
       c.call();
     }
 
     this.creeps.removeAll(this.deadCreeps);
     this.deadCreeps.clear();
   }
 
   @Override
   public void run() {
     long last = System.nanoTime();
     
     while (!Thread.currentThread().isInterrupted()) {
       long current = System.nanoTime();
       this.update((current - last) / 1E9f);
       last = current;
     }
   }
 
   @Override
   public void onDeath(Creep c) {
     this.deadCreeps.add(c);
   }
 
   @Override
   public void onSpawn(Creep c) {
     this.newCreeps.add(c);
   }
 
   @Override
   public void spawnCreep(
       CopyableVector2f position,
       Queue<CheckPoint> checkpoints) {
     Creep c = new Creep(this);
     
     CreepFactoryBehaviour.spawnCreep(
         position,
         checkpoints,
         c,
         c,
        tileSize,
         this,
         this.creepPositions);
     
     this.onSpawn(c);
   }
 
   @Override
   public void handleEvent(Event event) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void mouseClicked(int button, int x, int y, int clickCount) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void mouseDragged(int oldx, int oldy, int newx, int newy) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void mouseMoved(int oldx, int oldy, int newx, int newy) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void mousePressed(int button, int x, int y) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void mouseReleased(int button, int x, int y) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void mouseWheelMoved(int change) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void inputEnded() {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void inputStarted() {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public boolean isAcceptingInput() {
     // TODO Auto-generated method stub
     return false;
   }
 
   @Override
   public void setInput(Input input) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void keyPressed(int key, char c) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void keyReleased(int key, char c) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void controllerButtonPressed(int controller, int button) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void controllerButtonReleased(int controller, int button) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void controllerDownPressed(int controller) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void controllerDownReleased(int controller) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void controllerLeftPressed(int controller) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void controllerLeftReleased(int controller) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void controllerRightPressed(int controller) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void controllerRightReleased(int controller) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void controllerUpPressed(int controller) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void controllerUpReleased(int controller) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public int compareTo(Object o) {
     return this.hashCode() - o.hashCode();
   }
 }
