 package mobile.team4.game;
 
 import android.graphics.Canvas;
import android.util.Log;
 import android.view.SurfaceHolder;
 
 public class GameLoopThread extends Thread {
 	private SurfaceHolder _surfaceHolder;
     private Game _game;
     private boolean _run = false;
     
     public GameLoopThread(SurfaceHolder surfaceHolder, Game game) {
         _surfaceHolder = surfaceHolder;
         _game = game;
     }
     
     public SurfaceHolder getSurfaceHolder() {
         return _surfaceHolder;
     }
 
     public void setRunning(boolean run) {
         _run = run;
     }
 
     @Override
     public void run() {
         Canvas c;
         while (_run) {
             c = null;
             try {
                 c = _surfaceHolder.lockCanvas(null);
                 synchronized (_surfaceHolder) {
                 	_game.updateState();
             	    _game.updateInput();
             	    _game.updateAnimations();
             	    _game.updateSound();
                     _game.updateVideo(c);
                 }
             } finally {
                 // do this in a finally so that if an exception is thrown
                 // during the above, we don't leave the Surface in an
                 // inconsistent state
                 if (c != null) {
                     _surfaceHolder.unlockCanvasAndPost(c);
                 }
             }
         }
     }
 }
