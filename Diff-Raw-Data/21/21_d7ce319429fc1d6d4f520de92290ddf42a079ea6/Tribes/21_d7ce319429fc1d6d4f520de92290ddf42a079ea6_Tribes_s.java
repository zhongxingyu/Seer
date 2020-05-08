 package com.first.tribes.core;
 
 import com.first.tribes.core.ui.FPSRenderer;
 import com.first.tribes.core.ui.toolbar.PushPullTool;
 import com.first.tribes.core.ui.toolbar.SpawnAvatarTool;
 import com.first.tribes.core.util.Timer;
 import com.first.tribes.core.util.Timer.TimerTask;
 import com.first.tribes.core.util.Updatee;
 import java.util.ArrayList;
 import java.util.ConcurrentModificationException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import static playn.core.PlayN.*;
 
 import playn.core.*;
 import playn.core.Keyboard.Event;
 import playn.core.Keyboard.TypedEvent;
 import playn.core.Mouse.ButtonEvent;
 import playn.core.Mouse.MotionEvent;
 import playn.core.Mouse.WheelEvent;
 import pythagoras.f.Point;
 
 public class Tribes implements Game {
 
     public static int SCREEN_WIDTH;
     public static int SCREEN_HEIGHT;
     public static boolean SHIFT = false;
     protected TribesWorld world;
     private List<Updatee> updatees;
 
     public Tribes(int width, int height) {
         SCREEN_WIDTH = width;
         SCREEN_HEIGHT = height;
     }
 
     @Override
     public void init() {
         world = new TribesWorld(this);
         updatees = new ArrayList<Updatee>();
         
         registerUpdatee(new EndGame(this));
 
         graphics().rootLayer().add(world.getLayer());
         graphics().rootLayer().add(graphics().createImmediateLayer(new FPSRenderer()));
                 
         pointer().setListener(new TribesPointerListener());
         keyboard().setListener(new TribesKeyListener());
 //        mouse().setListener(new TribesMouseListener());
     }
 
     @Override
     public void paint(float alpha) {
     }
 
     @Override
     public void update(float delta) {
         world.update(delta);
         try {
             for (Updatee updatee : updatees) {
                 updatee.update(delta);
             }
         } catch (ConcurrentModificationException e) {
         }
     }
 
     @Override
     public int updateRate() {
         return 25;
     }
 
     public void unregisterUpdatee(Updatee aThis) {
         updatees.remove(aThis);
 
     }
 
     public void registerUpdatee(Updatee aThis) {
         updatees.add(aThis);
     }
 
     private class TribesKeyListener implements Keyboard.Listener {
 
         private static final float VIEWPORT_KEY_SHIFT = 20f;
         private static final float ZOOM_AMOUNT = 1.05f;
         private static final long REPEAT_KEY_DELAY = 5;
         private Map<Integer, Timer> timerMap = new HashMap<Integer, Timer>();
 
         @Override
         public void onKeyDown(Event event) {
 
             if (!timerMap.containsKey(event.key().ordinal())) {
                 Timer timer = new Timer(Tribes.this);
                 timer.schedule(new KeyRepeater(event.key()), 0,
                         REPEAT_KEY_DELAY);
                 timerMap.put(event.key().ordinal(), timer);
             }
 
             switch (event.key()) {
                 case SHIFT:
                     Tribes.SHIFT = true;
                     break;
                 case L:
                    SpawnAvatarTool.setCurrentTrait(5);
                     break;
                 case A:
                     SpawnAvatarTool.setCurrentTrait(0);
                     break;
                 case S:
                     SpawnAvatarTool.setCurrentTrait(1);
                     break;
                 case C:
                      break;
                 case I:
                    SpawnAvatarTool.setCurrentTrait(3);
                     break;
                 case H:
                    SpawnAvatarTool.setCurrentTrait(4);
                     break;
                 case R:
                    SpawnAvatarTool.setCurrentTrait(7);
                     break;
                 case M:
                    SpawnAvatarTool.setCurrentTrait(6);
                     break;
 
                 case K0:
                 case K1:
                 case K2:
                 case K3:
                 case K4:
                 case K5:
                 case K6:
                 case K7:
                 case K8:
                 case K9:
                     world.toolbar().keyPress(event.key());
                     break;
 
             }
         }
 
         @Override
         public void onKeyTyped(TypedEvent event) {
         }
 
         @Override
         public void onKeyUp(Event event) {
             Timer timer = timerMap.remove(event.key().ordinal());
             timer.cancel();
             if (event.key() == Key.SHIFT) {
                 Tribes.SHIFT = false;
             }
         }
 
         private class KeyRepeater implements TimerTask {
 
             private Key key;
 
             public KeyRepeater(Key key) {
                 this.key = key;
             }
 
             public void run() {
                 switch (key) {
                     case UP:
                         world.moveViewPort(0, -VIEWPORT_KEY_SHIFT);
                         break;
                     case DOWN:
                         world.moveViewPort(0, VIEWPORT_KEY_SHIFT);
                         break;
                     case LEFT:
                         world.moveViewPort(-VIEWPORT_KEY_SHIFT, 0);
                         break;
                     case RIGHT:
                         world.moveViewPort(VIEWPORT_KEY_SHIFT, 0);
                         break;
                     case MINUS:
                         world.zoomDelta(ZOOM_AMOUNT);
                         break;
                     case PLUS:
                     case EQUALS:
 
                         world.zoomDelta(1 / ZOOM_AMOUNT);
                         break;
                 }
             }
         }
     }
 
     class TribesPointerListener implements Pointer.Listener {
 
         PointerFocusable focused = null;
 
         @Override
         public void onPointerStart(Pointer.Event event) {
             focused = world.press(event.x(), event.y());
         }
 
         @Override
         public void onPointerEnd(Pointer.Event event) {
             focused.release(event.x(), event.y());
             focused = null;
         }
 
         @Override
         public void onPointerDrag(Pointer.Event event) {
             focused.drag(event.x(), event.y());
         }
 
         @Override
         public void onPointerCancel(Pointer.Event event) {
         }
     }
 
     public interface PointerFocusable {
 
         PointerFocusable press(float x, float y);
 
         void release(float x, float y);
 
         void drag(float x, float y);
     }
 
     private class TribesMouseListener implements Mouse.Listener {
 
         @Override
         public void onMouseDown(ButtonEvent event) {
         }
 
         @Override
         public void onMouseUp(ButtonEvent event) {
         }
 
         @Override
         public void onMouseMove(MotionEvent event) {
         }
 
         @Override
         public void onMouseWheelScroll(WheelEvent event) {
         }
     }
 }
