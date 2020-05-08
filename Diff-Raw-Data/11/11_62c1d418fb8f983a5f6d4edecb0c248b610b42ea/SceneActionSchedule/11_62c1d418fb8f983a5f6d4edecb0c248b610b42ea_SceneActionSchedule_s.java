 /**
  *  C-Nery - A home automation web application for C-Bus.
  *  Copyright (C) 2008,2009,2012  Dave Oxley <dave@daveoxley.co.uk>.
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
 
 import com.daveoxley.cnery.actions.SceneActionHome;
 import com.daveoxley.cnery.entities.AbstractCondition;
 import com.daveoxley.cnery.entities.Scene;
 import com.daveoxley.cnery.entities.SceneAction;
 import com.daveoxley.cnery.entities.SceneActionCondition;
 import com.workplacesystems.queuj.schedule.VariableSchedule;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import org.jboss.seam.Component;
 
 /**
  *
  * @author Dave Oxley <dave@daveoxley.co.uk>
  */
 public class SceneActionSchedule extends VariableSchedule {
 
     private SceneAction sceneAction;
 
     void setSceneAction(SceneAction sceneAction) {
         this.sceneAction = sceneAction;
     }
 
     @Override
     protected GregorianCalendar getNextRunTime(GregorianCalendar startTime) {
         SceneActionHome sah = (SceneActionHome)Component.getInstance(SceneActionHome.class, true);
         sah.clearInstance();
         sah.setId(sceneAction.getId());
         sceneAction = sah.getInstance();
         boolean firstRun = sceneAction.isFirstRun();
         Scene scene = sceneAction.getScene();
 
         GregorianCalendar nextCheckTime = null;
        if (firstRun) {
            nextCheckTime = (GregorianCalendar)startTime.clone();
            nextCheckTime.add(Calendar.SECOND, sceneAction.getDelay());
        }
         for (SceneActionCondition sac : sceneAction.getConditions()) {
             if ((sac.getSceneState() != SceneActionCondition.SceneState.TRIGGERED && scene.getStatePersistence() != Scene.StatePersistence.TRIGGER) || firstRun) {
                 if (sac.getActionType() == AbstractCondition.ActionType.TIME) {
                     GregorianCalendar actionTime = sac.getNextActionGregorian(startTime);
                     if (nextCheckTime == null || actionTime.before(nextCheckTime))
                         nextCheckTime = actionTime;
                 }
             }
         }
 
         return nextCheckTime;
     }
     
 }
