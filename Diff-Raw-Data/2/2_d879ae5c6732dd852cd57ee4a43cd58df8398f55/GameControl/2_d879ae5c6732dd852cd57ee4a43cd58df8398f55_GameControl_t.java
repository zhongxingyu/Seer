 package org.ivan.simple.game;
 
 import android.content.Context;
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.widget.Toast;
 
 import org.ivan.simple.UserControlType;
 import org.ivan.simple.game.controls.ControlChangeObserver;
 import org.ivan.simple.game.controls.ObtainedControl;
 import org.ivan.simple.game.controls.UserControl;
 import org.ivan.simple.game.controls.UserControlProvider;
 import org.ivan.simple.game.sound.SoundManager;
 import org.ivan.simple.game.tutorial.SolutionStep;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 public class GameControl implements ControlChangeObserver {
 	GameView view;
     private ControlsFactory controlsFactory;
     private UserControlProvider controlProvider;
 
     private boolean robotMode = false;
     private Iterator<SolutionStep> autoControls = new ArrayList<SolutionStep>().iterator();
 	
 	private GameManager gameLoopThread;
 	private boolean paused;
 
 	private SoundManager soundManager;
 	
 	public GameControl(final GameView view) {
 		this.view  = view;
         controlsFactory = new ControlsFactory(this);
         initControlProvider();
         soundManager = view.getGameContext().app().getSoundManager();
 	}
 
     protected boolean scanControl(MotionEvent event) {
 		if(paused || robotMode) return false;
 		return controlProvider.obtainControl(event);
 	}
 
     protected void restartGame() {
         stopManager();
        win = false;
        detonate = false;
         view.initGame();
         startManager();
     }
 	
 	protected boolean isRunning() {
 		return gameLoopThread.isRunning();
 	}
 	
 	public void startManager() {
         if(gameLoopThread != null && gameLoopThread.isRunning()) return;
 		System.out.println("Start game loop");
 		gameLoopThread = new GameManager(view);
 		gameLoopThread.setRunning(true);
 		gameLoopThread.start();
 		paused = false;
 	}
 	
 	public void stopManager() {
 		if(gameLoopThread == null) return;
         postStopManager();
 		boolean retry = true;
         while (retry) {
            try {
                gameLoopThread.join();
                retry = false;
            } catch (InterruptedException e) {
                System.out.println("Interrupted exception: " + e);
            }
         }
 	}
 
     public void postStopManager() {
         if(gameLoopThread == null) return;
         System.out.println("Stop game loop");
         paused = true;
         gameLoopThread.setRunning(false);
     }
 	
 	protected GameManager getGameLoopThread() {
 		return gameLoopThread;
 	}
 	
 	protected void playSound() {
 		if(view.getGameContext().app().getSound()) {
 			soundManager.playSound(
                     view.level.model.hero.currentMotion,
                     view.level.model.hero.finishingMotion,
                     view.level.model.getHeroCell(),
                     view.prevCell
             );
 		}
 	}
 
     public void setAutoControls(Iterator<SolutionStep> autoControls) {
         robotMode = true;
         this.autoControls = autoControls;
     }
 
 
     protected UserControl getUserControl() {
         UserControl control;
         if(!robotMode) {
             control = controlProvider.getUserControl();
         } else {
             if(autoControls.hasNext()) {
                 SolutionStep step = autoControls.next();
                 control = new ObtainedControl(step.getControl());
                 view.guideAnimation.init(step);
                 toastMessage(view.getContext(), step.getMessage());
             } else {
                 control = new ObtainedControl(UserControlType.IDLE);
                 view.getGameContext().stopTutorial();
             }
         }
         return control;
     }
 
     private Toast lastToast;
     public void toastMessage(final Context context, final String message) {
         view.post(new Runnable() {
             @Override
             public void run() {
                 if(message.length() > 0) {
                     if(lastToast != null) lastToast.cancel();
                     lastToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
                     lastToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                     lastToast.show();
                 }
             }
         });
     }
 
     @Override
     public void notifyObserver() {
         initControlProvider();
     }
 
     private void initControlProvider() {
         controlProvider = controlsFactory.createControlProvider(
                 view.getGameContext().app().getSettingsModel().getControlsType());
     }
 
     private boolean detonate = false;
     protected void playDetonateSound() {
         if (!detonate) {
             detonate = true;
             if (view.getGameContext().app().getSound()) {
                 soundManager.playDetonate();
             }
         }
     }
 
     private boolean win = false;
     protected void playWinSound() {
         if (!win) {
             win = true;
             if (view.getGameContext().app().getSound()) {
                 soundManager.playWin();
             }
         }
     }
 
     public void releaseResources() {
         if(view.background != null) view.background.recycle();
     }
 }
