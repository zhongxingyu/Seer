 import java.util.HashMap;
 import java.util.Map;
 
 import processing.core.PGraphics;
 import TUIO.TuioCursor;
 
 import remixlab.tersehandling.core.*;
 import remixlab.tersehandling.generic.event.*;
 import remixlab.tersehandling.generic.agent.*;
 import remixlab.tersehandling.generic.profile.*;
 import remixlab.tersehandling.event.*;
 
 public class TUIOAgent
 extends
 GenericMotionAgent<GenericMotionProfile<MotionAction>, GenericClickProfile<ClickAction>>
 implements EventConstants {
 	
   PGraphics canvas;
   GenericDOF2Event<MotionAction> event, prevEvent;
   Map<Integer, Grabbable> grabMap = new HashMap<Integer, Grabbable>();
 
   public TUIOAgent(TerseHandler scn, String n, PGraphics canvas) {
     super(new GenericMotionProfile<MotionAction>(), 
     new GenericClickProfile<ClickAction>(), scn, n);
     this.canvas = canvas;
     // default bindings
     clickProfile().setClickBinding(TH_LEFT, 1, ClickAction.CHANGE_COLOR);
     profile().setBinding(TH_LEFT, MotionAction.CHANGE_POSITION);
   }
 
   public void addTuioCursor(TuioCursor tcur) {
     event = new GenericDOF2Event<MotionAction>(prevEvent, 
     tcur.getScreenX(canvas.width), tcur.getScreenY(canvas.height), 0, 0);
     Grabbable grabbable = updateGrabber(event);
     if (grabbable != null)
       grabMap.put(tcur.getCursorID(), grabbable);
   }
 
   // called when a cursor is moved
   public void updateTuioCursor(TuioCursor tcur) {
     Grabbable trackedGrabber = grabMap.get(tcur.getCursorID());
     if (trackedGrabber != null) {
       event = new GenericDOF2Event<MotionAction>(prevEvent, 
       tcur.getScreenX(canvas.width), 
       tcur.getScreenY(canvas.height), 0, TH_LEFT);
       disableTracking();
       setDefaultGrabber(trackedGrabber);
       handle(event);
       enableTracking();
     } 
     prevEvent = event.get();
   }
 
   // called when a cursor is removed from the scene
   public void removeTuioCursor(TuioCursor tcur) {
     event = new GenericDOF2Event<MotionAction>(prevEvent, -1000, -1000, 0, 0);
    updateGrabber(event);
     grabMap.remove(tcur.getCursorID());
   }
 }
 
