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
 
 import com.daveoxley.cnery.entities.SceneAction;
 import com.daveoxley.cnery.entities.SceneActionCondition;
 import com.daveoxley.cbus.CGateConnectException;
 import com.daveoxley.cbus.CGateException;
 import com.daveoxley.cbus.CGateObject;
 import com.daveoxley.cbus.CGateSession;
 import com.daveoxley.cbus.Group;
 import com.daveoxley.cbus.Response;
 import com.daveoxley.cnery.actions.SceneActionHome;
 import com.daveoxley.cnery.entities.AbstractCondition;
 import com.daveoxley.cnery.entities.Scene;
 import java.net.UnknownHostException;
 import java.util.GregorianCalendar;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jboss.seam.Component;
 import org.jboss.seam.contexts.Contexts;
 
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
 
     SceneActionRunner(SceneRunner sceneRunner, SceneAction sceneAction) throws Exception {
         super(sceneAction);
         this.sceneRunner = sceneRunner;
         this.sceneAction = sceneAction;
         CGateObject cGateObject = null;
         try {
             CGateSession cGateSession = (CGateSession)Contexts.getApplicationContext().get("cGateSession");
             cGateObject = cGateSession.getCGateObject(sceneAction.getAddress());
         }
         catch (Exception e) {
             throw new CGateException(e);
         }
         if (!(cGateObject instanceof Group))
             throw new IllegalStateException(sceneAction.getAddress() + " is not a Group");
 
         this.group = (Group)cGateObject;
     }
 
     @Override
     SceneAction refresh(SceneAction _sceneAction) throws Exception {
         return _sceneAction;
     }
 
     int getDelay() {
         return sceneAction.getDelay();
     }
 
     private SceneAction.Type getType() {
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
 
     void initialise() throws CGateException {
         SceneActionHome sceneActionHome = (SceneActionHome)Component.getInstance(SceneActionHome.class, true);
         sceneActionHome.clearInstance();
         sceneActionHome.setId(sceneAction.getId());
         sceneAction = sceneActionHome.getInstance();
         sceneAction.setOriginalLevel(group.getLevel());
         sceneActionHome.update();
     }
 
     @Override
     boolean ignoreCondition(boolean scheduledCheck, SceneActionCondition condition) {
         return (condition.getSceneState() == SceneActionCondition.SceneState.TRIGGERED && scheduledCheck);
     }
 
     @Override
     boolean doAction(SceneActionCondition condition) {
        return condition.getAction() == SceneActionCondition.Action.ACTION;
     }
 
     @Override
     Response runAndSchedule(boolean scheduledCheck) throws Exception {
         Response response = null;
 
         if (scheduledCheck && sceneRunner.status != SceneRunner.ACTIVE && sceneRunner.status != SceneRunner.ACTIVATING)
                 return null;
 
         log.info("Checking conditions for SceneAction " + sceneAction.getScene().getName() + "." +
                                   group.getName() + "." + sceneAction.getId() + " : " + group.getAddress());
         if (conditionsMet(scheduledCheck))
             response = run();
         else if (scheduledCheck)
             response = reset(false);
 
         GregorianCalendar currentTime = new GregorianCalendar();
         GregorianCalendar nextCheckTime = null;
         for (SceneActionCondition sac : getConditions()) {
             if (sac.getSceneState() != SceneActionCondition.SceneState.TRIGGERED) {
                 if (sac.getActionType() == AbstractCondition.ActionType.TIME) {
                     GregorianCalendar actionTime = sac.getNextActionGregorian(currentTime);
                     if (nextCheckTime == null || actionTime.before(nextCheckTime))
                         nextCheckTime = actionTime;
                 } else if (sac.getActionType() != AbstractCondition.ActionType.GROUP) {
                     SceneStatusChangeCallback callback = (SceneStatusChangeCallback)Component.getInstance(SceneStatusChangeCallback.class);
                     callback.registerGroupListener(sac.getDependGroup(), this);
                 } else if (sac.getActionType() != AbstractCondition.ActionType.SCENE) {
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
 
         String logMessage = "run SceneAction " + sceneAction.getScene().getName() + "." +
                                   sceneAction.getId() + " : " + group.getAddress() + " - ";
         if (getType() == SceneAction.Type.ON) {
             log.info(logMessage + "on");
             return group.on();
         }
         else if (getType() == SceneAction.Type.OFF) {
             log.info(logMessage + "off");
             return group.off();
         }
         else if (getType() == SceneAction.Type.RAMP) {
             log.info(logMessage + "ramp to " + getLevel() + " in " + getDuration());
             return group.ramp(getLevel(), getDuration());
         }
 
         throw new IllegalStateException(getType() + " is not 'O', 'F' or 'R'");
     }
 
     synchronized Response reset(boolean unregister) throws Exception {
 
         if (sceneRunner.getResetAction() == Scene.ResetAction.RESET_UNLESS_CHANGED) {
             int expectedLevel = -1;
             if (getType() == SceneAction.Type.ON)
                 expectedLevel = 255;
             else if (getType() == SceneAction.Type.OFF)
                 expectedLevel = 0;
             else if (getType() == SceneAction.Type.RAMP)
                 expectedLevel = getLevel();
             else
                 throw new IllegalStateException(getType() + " is not 'O', 'F' or 'R'");
 
             int currentLevel = group.getLevel();
             if (currentLevel != expectedLevel)
                 return null;
         }
 
         if (unregister) {
             for (SceneActionCondition sac : getConditions()) {
                 if (sac.getActionType() != AbstractCondition.ActionType.GROUP) {
                     SceneStatusChangeCallback callback = (SceneStatusChangeCallback)Component.getInstance(SceneStatusChangeCallback.class);
                     callback.unregisterGroupListener(sac.getDependGroup(), this);
                 } else if (sac.getActionType() != AbstractCondition.ActionType.SCENE) {
                     SceneRunner dependSceneRunner = SceneRunnerFactory.getInstance().getSceneRunner(sac.getDependScene());
                     dependSceneRunner.unregisterSceneListener(this);
                 }
             }
         }
 
         if (!active)
             return null;
         active = false;
 
         int resetLevel = -1;
         if (sceneRunner.getResetAction() == Scene.ResetAction.RESET_UNLESS_CHANGED || sceneRunner.getResetAction() == Scene.ResetAction.RESET)
             resetLevel = getOriginalLevel();
         else if (sceneRunner.getResetAction() == Scene.ResetAction.ZERO)
             resetLevel = 0;
 
         log.info("reset SceneAction " + sceneAction.getScene().getName() + "." +
                 sceneAction.getId() + " : " + group.getAddress() + " - level " + resetLevel);
 
         if (resetLevel < 0)
             return null;
 
         if (getType() == SceneAction.Type.ON) {
             if (resetLevel == 0)
                 return group.off();
             else if (resetLevel < 255)
                 return group.ramp(resetLevel, 0);
             return null;
         }
         else if (getType() == SceneAction.Type.OFF) {
             if (resetLevel == 255)
                 return group.on();
             else if (resetLevel > 0)
                 return group.ramp(resetLevel, 0);
             return null;
         }
         else if (getType() == SceneAction.Type.RAMP) {
             return group.ramp(resetLevel, 0);
         }
         return null;
     }
 }
