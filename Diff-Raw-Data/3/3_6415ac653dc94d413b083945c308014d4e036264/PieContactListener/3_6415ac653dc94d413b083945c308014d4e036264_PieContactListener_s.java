 package com.example.homokaasuthegame;
 
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.util.debug.Debug;
 import org.andengine.util.debug.Debug.DebugLevel;
 
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.ContactImpulse;
 import com.badlogic.gdx.physics.box2d.ContactListener;
 import com.badlogic.gdx.physics.box2d.Manifold;
 
 public class PieContactListener implements ContactListener
 {
     private final MainActivity activity;
     private boolean timerRunning = false;
 
     public PieContactListener(MainActivity activity) {
         this.activity = activity;
     }
 
     @Override
     public void beginContact(Contact arg0) {
     }
 
     @Override
     public void endContact(Contact arg0) {
     }
 
     @Override
     public void postSolve(Contact contact, ContactImpulse impulse) {
         /* If body A was hit */
         if(contact.getFixtureA() != null)
         {
             if (Pie.class.isInstance(contact.getFixtureA().getBody().getUserData())) {
                 Pie pie = (Pie)contact.getFixtureA().getBody().getUserData();
                 // Eval
                 addTimer();
             } else if (Enemy.class.isInstance(contact.getFixtureA().getBody().getUserData())) {
                 Enemy enemy = (Enemy)contact.getFixtureA().getBody().getUserData();
                 // Eval
                 addTimer();
             }
         }
 
         /* If body B was hit */
         if(contact.getFixtureB() != null)
         {
             if (Pie.class.isInstance(contact.getFixtureB().getBody().getUserData())) {
                 Pie pie = (Pie)contact.getFixtureB().getBody().getUserData();
                 // Eval
                 addTimer();
             } else if (Enemy.class.isInstance(contact.getFixtureB().getBody().getUserData())) {
                 Enemy enemy = (Enemy)contact.getFixtureB().getBody().getUserData();
                 // Eval
                 addTimer();
             }
         }
     }
 
     @Override
     public void preSolve(Contact arg0, Manifold arg1) {
     }
 
     private void addTimer() {
         if (timerRunning == true)
             return;
         timerRunning = true;
 
         activity.getEngine().registerUpdateHandler(new TimerHandler(3.0f, new ITimerCallback()
         {
             @Override
             public void onTimePassed(final TimerHandler pTimerHandler)
             {
                 activity.getEngine().unregisterUpdateHandler(pTimerHandler);
                 timerRunning = false;
                 if (activity.eatPie() == false) {
                     activity.gameOVer();
                     Debug.log(DebugLevel.ALL, "Game over!");
                 }
 
                 Debug.log(DebugLevel.ALL, "Timer!");
             }
         }));
     }
 }
