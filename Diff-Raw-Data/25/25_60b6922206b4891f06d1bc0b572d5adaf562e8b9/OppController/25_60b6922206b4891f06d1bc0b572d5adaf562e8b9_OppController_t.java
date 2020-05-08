 package demo.client.local.game.controllers;
 
 import java.util.LinkedList;
 import java.util.Queue;
 
 import demo.client.local.game.gui.ControllableBoardDisplay;
 import demo.client.local.game.tools.DummyBus;
 import demo.client.shared.game.model.BoardModel;
 
 /**
  * A controller for canvases displaying remote opponents boards.
  * 
  * @author mbarkley <mbarkley@redhat.com>
  * 
  */
 public class OppController extends BoardController {
 
   private Queue<BoardModel> stateQueue = new LinkedList<BoardModel>();
   // True iff this board is currently controlling a display
   private boolean active;
 
   /**
    * Create an OppController instance.
    * 
    * @param boardDisplay
    *          The display to be controlled by this instance.
    */
   public OppController(ControllableBoardDisplay boardDisplay) {
     // Dummy objects do nothing, to prevent this controller from updating the displayed score or
     // sending messages through the bus.
     super(boardDisplay, new DummyController(), new DummyBus());
     setPaused(true);
   }
 
   /**
    * Add a game state to be drawn to the display.
    * 
    * @param state
    *          The state being queued for drawing.
    */
   public void addState(BoardModel state) {
     synchronized (stateQueue) {
       // While the board is inactive, only save the single most recent state
       if (!active) {
         stateQueue.clear();
       }
       stateQueue.add(state);
       if (isPaused()) {
         setPaused(false);
       }
     }
   }
 
   @Override
   protected void update() {
     if (active) {
       synchronized (stateQueue) {
         // Deal with row clearing animation before entering new state.
         int numFullRows = model.numFullRows();
         if (numFullRows > 0) {
           clearRows(numFullRows);
         }
         else if (!stateQueue.isEmpty()) {
           model = stateQueue.poll();
           reset();
           redraw();
         }
         else if (isPaused()) {
           if (model == null) {
             model = new BoardModel();
             reset();
           }
           redraw();
           boardDisplay.pause();
         }
       }
     }
   }
 
   /**
    * Check if this controller is currently active.
    * 
    * @return True iff this controller currently has control of a display.
    */
   public boolean isActive() {
     return active;
   }
 
   /**
    * Set whether or not this controller is actively controlling the display.
    * 
    * @param active
    *          True if this controller should be active. False otherwise.
    */
   public void setActive(boolean active) {
     this.active = active;
     if (!active) {
       while (stateQueue.size() > 1) {
         stateQueue.poll();
       }
     }
   }
 
 }
