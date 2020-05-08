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
 
 import cnery.ApplicationBean1;
 import cnerydb.SceneAction;
 import cnerydb.SceneActionCondition;
 import com.daveoxley.cbus.CGateConnectException;
 import com.daveoxley.cbus.CGateException;
 import com.daveoxley.cbus.CGateObject;
 import com.daveoxley.cbus.Group;
 import com.daveoxley.cbus.Response;
 import java.net.UnknownHostException;
 import java.util.GregorianCalendar;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.Session;
 
 /**
  *
  * @author Dave Oxley <dave@daveoxley.co.uk>
  */
 class SceneActionRunner extends AbstractRunner<SceneAction, SceneActionCondition> {
 
     private final static Log log = LogFactory.getLog(SceneActionRunner.class);
 
     private final SceneRunner sceneRunner;
 
     private SceneAction sceneAction;
 
     private final Group group;
 
     private boolean active = false;
 
     SceneActionRunner(Session session, SceneRunner sceneRunner, SceneAction sceneAction) throws Exception {
         super(session, sceneAction);
         this.sceneRunner = sceneRunner;
         this.sceneAction = sceneAction;
         CGateObject cGateObject = null;
         try {
             cGateObject = ApplicationBean1.getCGateSession().getCGateObject(sceneAction.getAddress());
         }
         catch (Exception e) {
             throw new CGateException(e);
         }
         if (!(cGateObject instanceof Group))
             throw new IllegalStateException(sceneAction.getAddress() + " is not a Group");
 
         this.group = (Group)cGateObject;
     }
 
     @Override
     SceneAction refresh(Session session, SceneAction _sceneAction) throws Exception {
         return _sceneAction;
     }
 
     int getDelay() {
         return sceneAction.getDelay();
     }
 
     private char getType() {
         return sceneAction.getType();
     }
 
     private int getLevel() {
         return sceneAction.getLevel();
     }
 
     private int getOriginalLevel() {
         return sceneAction.getOriginalLevel();
     }
 
     private int getDuration() {
         return sceneAction.getDuration();
     }
 
     void initialise(Session session) throws CGateException {
         sceneAction.setOriginalLevel(group.getLevel());
         session.merge(sceneAction);
     }
 
     @Override
     boolean ignoreCondition(boolean scheduledCheck, SceneActionCondition condition) {
         return (condition.getSceneState() == 'T' && scheduledCheck);
     }
 
     @Override
     boolean doAction(SceneActionCondition condition) {
         return condition.getAction() == 'A';
     }
 
     @Override
     Response runAndSchedule(boolean scheduledCheck) throws Exception {
         Response response = null;
 
         if (scheduledCheck && sceneRunner.status != SceneRunner.ACTIVE && sceneRunner.status != SceneRunner.ACTIVATING)
                 return null;
 
         log.info("Checking conditions for SceneAction " + sceneAction.getParent().getName() + "." +
                                  sceneAction.getId() + " : " + group.getAddress());
         if (conditionsMet(scheduledCheck))
             response = run();
         else if (scheduledCheck)
             response = reset(false);
 
         GregorianCalendar currentTime = new GregorianCalendar();
         GregorianCalendar nextCheckTime = null;
         for (SceneActionCondition sac : getConditions()) {
             if (sac.getSceneState() != 'T') {
                 if (sac.getActionType() == 'T') {
                     GregorianCalendar actionTime = sac.getNextActionGregorian(currentTime);
                     if (nextCheckTime == null || actionTime.before(nextCheckTime))
                         nextCheckTime = actionTime;
                 } else if (sac.getSceneState() != 'G') {
                     SceneStatusChangeCallback.getInstance().registerGroupListener(sac.getDependGroup(), this);
                 } else if (sac.getSceneState() != 'S') {
                     SceneRunner dependSceneRunner = SceneRunnerFactory.getInstance().getSceneRunner(sac.getDependScene());
                     dependSceneRunner.registerSceneListener(this);
                 }
             }
         }
         if (nextCheckTime != null)
             SceneActionScheduler.getInstance().scheduleRunner(nextCheckTime, this);
 
         return response;
     }
 
     private synchronized Response run() throws UnknownHostException, CGateConnectException, CGateException {
         if (active)
             return null;
         active = true;
 
         String logMessage = "run SceneAction " + sceneAction.getParent().getName() + "." +
                                   sceneAction.getId() + " : " + group.getAddress() + " - ";
         if (getType() == 'O') {
             log.info(logMessage + "on");
             return group.on();
         }
         else if (getType() == 'F') {
             log.info(logMessage + "off");
             return group.off();
         }
         else if (getType() == 'R') {
             log.info(logMessage + "ramp to " + getLevel() + " in " + getDuration());
             return group.ramp(getLevel(), getDuration());
         }
 
         throw new IllegalStateException(getType() + " is not 'O', 'F' or 'R'");
     }
 
     synchronized Response reset(boolean unregister) throws Exception {
 
         if (sceneRunner.getResetAction() == 'P') {
             int expectedLevel = -1;
             if (getType() == 'O')
                 expectedLevel = 255;
             else if (getType() == 'F')
                 expectedLevel = 0;
             else if (getType() == 'R')
                 expectedLevel = getLevel();
             else
                 throw new IllegalStateException(getType() + " is not 'O', 'F' or 'R'");
 
             int currentLevel = group.getLevel();
             if (currentLevel != expectedLevel)
                 return null;
         }
 
         if (unregister) {
             for (SceneActionCondition sac : getConditions()) {
                 if (sac.getSceneState() != 'G') {
                     SceneStatusChangeCallback.getInstance().unregisterGroupListener(sac.getDependGroup(), this);
                 } else if (sac.getSceneState() != 'S') {
                     SceneRunner dependSceneRunner = SceneRunnerFactory.getInstance().getSceneRunner(sac.getDependScene());
                     dependSceneRunner.unregisterSceneListener(this);
                 }
             }
         }
 
         if (!active)
             return null;
         active = false;
 
         int resetLevel = -1;
         if (sceneRunner.getResetAction() == 'P' || sceneRunner.getResetAction() == 'R')
             resetLevel = getOriginalLevel();
         else if (sceneRunner.getResetAction() == 'Z')
             resetLevel = 0;
 
         log.info("reset SceneAction " + sceneAction.getParent().getName() + "." +
                 sceneAction.getId() + " : " + group.getAddress() + " - level " + resetLevel);
 
         if (resetLevel < 0)
             return null;
 
         if (getType() == 'O') {
             if (resetLevel == 0)
                 return group.off();
             else if (resetLevel < 255)
                 return group.ramp(resetLevel, 0);
             return null;
         }
         else if (getType() == 'F') {
             if (resetLevel == 255)
                 return group.on();
             else if (resetLevel > 0)
                 return group.ramp(resetLevel, 0);
             return null;
         }
         else if (getType() == 'R') {
             return group.ramp(resetLevel, 0);
         }
         return null;
     }
 }
