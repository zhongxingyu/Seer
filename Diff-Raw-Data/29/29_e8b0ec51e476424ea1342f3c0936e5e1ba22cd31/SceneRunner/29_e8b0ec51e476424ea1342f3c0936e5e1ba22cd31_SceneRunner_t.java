 /**
  *  C-Nery - A home automation web application for C-Bus.
  *  Copyright (C) 2008  Dave Oxley <dave@daveoxley.co.uk>.
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
 
 import cnerydb.HibernateUtil;
 import cnerydb.Scene;
 import cnerydb.SceneAction;
 import cnerydb.SceneCondition;
 import com.daveoxley.cbus.CGateException;
 import com.daveoxley.cbus.Response;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.GregorianCalendar;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
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
 
     SceneRunner(Session session, Scene scene) throws Exception {
         super(session, scene);
     }
 
     @Override
     Scene refresh(Session session, Scene _scene) throws Exception {
         if (actionRunners == null)
             actionRunners = new ArrayList<SceneActionRunner>();
 
         for (SceneActionRunner sceneActionRunner : actionRunners)
             SceneActionScheduler.getInstance().unscheduleRunner(sceneActionRunner);
         SceneActionScheduler.getInstance().unscheduleRunner(this);
 
         actionRunners.clear();
         session.evict(_scene);
         this.scene = (Scene)session.get(Scene.class, _scene.getId());
         for (SceneAction sceneAction : this.scene.getSceneActions()) {
             actionRunners.add(new SceneActionRunner(session, this, sceneAction));
         }
 
         Collections.sort(actionRunners, new Comparator<SceneActionRunner>() {
 
             @Override
             public int compare(SceneActionRunner o1, SceneActionRunner o2) {
                 return (o1.getDelay() < o2.getDelay() ? -1 : (o1.getDelay() == o2.getDelay() ? 0 : 1));
             }
         });
         return this.scene;
     }
 
     char getResetAction() {
         return scene.getResetAction();
     }
 
     private char getStatePersistence() {
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
 
         if (getStatePersistence() == 'T') {
             GregorianCalendar scheduledTime = (GregorianCalendar)startTime.clone();
             scheduledTime.add(Calendar.MINUTE, getMinutes());
             SceneActionScheduler.getInstance().scheduleRunner(scheduledTime, this);
         }
 
         ArrayList<Response> responses = new ArrayList<Response>();
         try {
             Session session = HibernateUtil.getSessionFactory().getCurrentSession();
             Transaction tx = null;
             try {
                 tx = session.beginTransaction();
 
                 for (SceneActionRunner sceneActionRunner : actionRunners) {
                     if (sceneActionRunner.getDelay() <= 0)
                         sceneActionRunner.initialise(session);
                 }
                 tx.commit();
             } catch (Exception e) {
                 tx.rollback();
                 throw new CGateException(e);
             }
 
             for (SceneActionRunner sceneActionRunner : actionRunners) {
                 if (sceneActionRunner.getDelay() > 0) {
 
                     session = HibernateUtil.getSessionFactory().getCurrentSession();
                     try {
                         tx = session.beginTransaction();
 
                         sceneActionRunner.initialise(session);
 
                         tx.commit();
                     } catch (Exception e) {
                         tx.rollback();
                         throw new CGateException(e);
                     }
                 }
 
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
                 if (getStatePersistence() == 'N')
                     reset();
             }
         }
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
             if (getResetAction() != 'C') {
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
 
             Session session = HibernateUtil.getSessionFactory().getCurrentSession();
             Transaction tx = null;
 
             try {
                 tx = session.beginTransaction();
                 for (SceneAction sceneAction : scene.getSceneActions()) {
                     session.refresh(sceneAction);
                     sceneAction.setOriginalLevel(-1);
                 }
                 refresh(session, scene);
 
                 tx.commit();
             } catch (Exception e) {
                 tx.rollback();
                 throw new CGateException(e);
             }
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
                 synchronized (listenersMutex) {
                     for (SceneActionRunner sceneActionRunner : sceneListeners)
                         try {
                         sceneActionRunner.runAndSchedule(true);
                     } catch (Exception e) {
                         new CGateException(e);
                     }
                 }
             }
         });
         thread.start();
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
         return condition.getAction() == 'A';
     }
 }
