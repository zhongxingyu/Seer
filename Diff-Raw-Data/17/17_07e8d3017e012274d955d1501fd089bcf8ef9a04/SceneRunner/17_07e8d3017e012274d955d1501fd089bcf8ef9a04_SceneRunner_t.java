 /**
  *  C-Nery - A home automation web application for C-Bus.
  *  Copyright (C) 2008,2009  Dave Oxley <dave@daveoxley.co.uk>.
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as
  *  published by the Free Software Foundation, either version 3 of the
  *  License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of the GNU Affero General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package com.daveoxley.cnery.scenes;
 
 import com.daveoxley.cnery.actions.SceneHome;
 import com.daveoxley.cnery.entities.Scene;
 import com.daveoxley.cnery.entities.SceneAction;
 import com.daveoxley.cnery.entities.SceneCondition;
 import com.daveoxley.cbus.CGateException;
 import com.daveoxley.cbus.Response;
 import com.daveoxley.cnery.actions.SceneActionHome;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.GregorianCalendar;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jboss.seam.Component;
 import org.jboss.seam.async.Asynchronous;
 
 /**
  *
  * @author Dave Oxley <dave@daveoxley.co.uk>
  */
 public final class SceneRunner extends AbstractRunner<Scene, SceneCondition> {
 
     private final static Log log = LogFactory.getLog(SceneRunner.class);
 
     private Scene scene;
     private ArrayList<SceneActionRunner> actionRunners;
 
     final static int INACTIVE = 0;
     final static int ACTIVATING = 1;
     final static int ACTIVE = 2;
     final static int RESETTING = 3;
 
     int status = INACTIVE;
 
     private final Object statusMutex = new Object();
 
     private final ArrayList<SceneActionRunner> sceneListeners = new ArrayList<SceneActionRunner>();
 
     private final Object listenersMutex = new Object();
 
     SceneRunner(Scene scene) throws Exception {
         super(scene);
     }
 
     @Override
     Scene refresh(Scene _scene) throws Exception {
         if (actionRunners == null)
             actionRunners = new ArrayList<SceneActionRunner>();
 
         for (SceneActionRunner sceneActionRunner : actionRunners)
             SceneActionScheduler.getInstance().unscheduleRunner(sceneActionRunner);
         SceneActionScheduler.getInstance().unscheduleRunner(this);
 
         SceneHome sceneHome = (SceneHome)Component.getInstance(SceneHome.class, true);
         actionRunners.clear();
         sceneHome.clearInstance();
         sceneHome.setId(_scene.getId());
         this.scene = sceneHome.getInstance();
         for (SceneAction sceneAction : this.scene.getSceneActions()) {
             actionRunners.add(new SceneActionRunner(this, sceneAction));
         }
 
         Collections.sort(actionRunners, new Comparator<SceneActionRunner>() {
 
             @Override
             public int compare(SceneActionRunner o1, SceneActionRunner o2) {
                 return (o1.getDelay() < o2.getDelay() ? -1 : (o1.getDelay() == o2.getDelay() ? 0 : 1));
             }
         });
         return this.scene;
     }
 
     Scene.ResetAction getResetAction() {
         return scene.getResetAction();
     }
 
     private Scene.StatePersistence getStatePersistence() {
         return scene.getStatePersistence();
     }
 
     private int getMinutes() {
         return scene.getMinutes();
     }
 
     public void activate() throws Exception {
         synchronized (statusMutex) {
             if (status == ACTIVATING)
                 return;
             if (status == RESETTING) {
                 try {
                     statusMutex.wait();
                 }
                 catch (InterruptedException ie) {}
             }
             log.info("Checking conditions for scene " + scene.getName());
             if (!conditionsMet(false))
                 return;
 
             status = ACTIVATING;
         }
 
         log.info("Activating scene " + scene.getName());
 
         callbackListeners();
 
         GregorianCalendar startTime = new GregorianCalendar();
 
         if (getStatePersistence() == Scene.StatePersistence.TIMED_ACTIVE) {
             GregorianCalendar scheduledTime = (GregorianCalendar)startTime.clone();
             scheduledTime.add(Calendar.MINUTE, getMinutes());
             SceneActionScheduler.getInstance().scheduleRunner(scheduledTime, this);
         }
 
         ArrayList<Response> responses = new ArrayList<Response>();
         try {
             immediateInitialise();
 
             for (SceneActionRunner sceneActionRunner : actionRunners) {
                 if (sceneActionRunner.getDelay() > 0)
                     initialise(sceneActionRunner);
 
                 GregorianCalendar runTime = (GregorianCalendar) startTime.clone();
                 runTime.add(Calendar.SECOND, sceneActionRunner.getDelay());
                 long when = runTime.getTimeInMillis();
                 long now;
                 synchronized (statusMutex) {
                     while ((now = (new GregorianCalendar()).getTimeInMillis()) < when && status == ACTIVATING) {
                         long wait_time = when - now;
                         try {
                             statusMutex.wait(wait_time);
                         }
                         catch (InterruptedException ie) {}
                     }
 
                     if (status != ACTIVATING)
                         return;
                 }
 
                 Response resp = sceneActionRunner.runAndSchedule(false);
                 if (resp != null)
                     responses.add(resp);
             }
         }
         finally {
 
             synchronized (statusMutex) {
                 if (status == ACTIVATING)
                     status = ACTIVE;
                 else
                     statusMutex.notifyAll();
             }
 
             try {
                 for (Response response : responses)
                     response.handle200();
             }
             finally {
                 if (getStatePersistence() == Scene.StatePersistence.TRIGGER)
                     reset();
             }
         }
     }
 
     private void immediateInitialise() throws CGateException {
         for (SceneActionRunner sceneActionRunner : actionRunners) {
             if (sceneActionRunner.getDelay() <= 0)
                 initialise(sceneActionRunner);
         }
     }
 
     private void initialise(SceneActionRunner sceneActionRunner) throws CGateException {
         sceneActionRunner.initialise();
     }
 
     public void reset() throws Exception {
         synchronized (statusMutex) {
             if (status == RESETTING)
                 return;
             if (status == ACTIVATING) {
                 status = RESETTING;
                 statusMutex.notifyAll();
                 try {
                     statusMutex.wait();
                 }
                 catch (InterruptedException ie) {}
             }
             status = RESETTING;
         }
 
         log.info("Resetting scene " + scene.getName());
 
         callbackListeners();
 
         try {
             if (getResetAction() != Scene.ResetAction.LEAVE) {
                 ArrayList<Response> responses = new ArrayList<Response>();
 
                 Collections.reverse(actionRunners);
                 for (SceneActionRunner sceneActionRunner : actionRunners) {
                     Response response = sceneActionRunner.reset(true);
                     if (response != null)
                         responses.add(response);
                 }
 
                 for (Response response : responses)
                     response.handle200();
             }
 
             SceneActionHome sceneActionHome = (SceneActionHome)Component.getInstance(SceneActionHome.class, true);
             for (SceneAction sceneAction : scene.getSceneActions()) {
                 sceneActionHome.clearInstance();
                 sceneActionHome.setId(sceneAction.getId());
                 sceneAction = sceneActionHome.getInstance();
                 sceneAction.setOriginalLevel(-1);
                 sceneActionHome.update();
             }
            refresh(scene);
         }
         finally {
             synchronized (statusMutex) {
                 status = INACTIVE;
                 statusMutex.notifyAll();
             }
         }
     }
 
     void registerSceneListener(SceneActionRunner sceneActionRunner) {
         synchronized (listenersMutex) {
             if (!sceneListeners.contains(sceneActionRunner))
                 sceneListeners.add(sceneActionRunner);
         }
     }
 
     void unregisterSceneListener(SceneActionRunner sceneActionRunner) {
         synchronized (listenersMutex) {
             if (sceneListeners.contains(sceneActionRunner))
                 sceneListeners.remove(sceneActionRunner);
         }
     }
 
     private void callbackListeners() {
         Thread thread = new Thread(new Runnable() {
 
             @Override
             public void run() {
                 async.execute(null);
             }
         });
         thread.start();
     }
 
     private final CallbackListeners async = new CallbackListeners();
 
     private final class CallbackListeners extends Asynchronous {
 
         @Override
         public void execute(Object timer) {
             synchronized (listenersMutex) {
                 for (SceneActionRunner sceneActionRunner : sceneListeners)
                     try {
                     sceneActionRunner.runAndSchedule(true);
                 } catch (Exception e) {
                     new CGateException(e);
                 }
             }
         }
 
         @Override
         protected void handleException(Exception exception, Object time) {}
 
     }
 
     @Override
     Response runAndSchedule(boolean scheduledCheck) throws Exception {
         reset();
         return null;
     }
 
     @Override
     boolean ignoreCondition(boolean scheduledCheck, SceneCondition condition) {
         return false;
     }
 
     @Override
     boolean doAction(SceneCondition condition) {
         return condition.getAction() == SceneCondition.Action.ALLOW;
     }
 }
