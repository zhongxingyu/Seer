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
 import cnerydb.AbstractCondition;
 import cnerydb.ConditionProvider;
 import com.daveoxley.cbus.CGateException;
 import com.daveoxley.cbus.CGateObject;
 import com.daveoxley.cbus.Group;
 import com.daveoxley.cbus.Response;
 import java.text.SimpleDateFormat;
 import java.util.GregorianCalendar;
 import java.util.HashSet;
 import java.util.Set;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.Session;
 
 /**
  *
  * @author Dave Oxley <dave@daveoxley.co.uk>
  */
 abstract class AbstractRunner<S extends ConditionProvider<T>, T extends AbstractCondition> {
 
     private final static Log log = LogFactory.getLog(AbstractRunner.class);
 
     private final Set<T> conditions;
 
     AbstractRunner(Session session, S conditionProvider) throws Exception {
         conditionProvider = refresh(session, conditionProvider);
         this.conditions = new HashSet<T>(conditionProvider.getConditions());
     }
 
     protected Set<T> getConditions() {
         return conditions;
     }
 
     abstract S refresh(Session session, S conditionProvider) throws Exception;
 
     abstract Response runAndSchedule(boolean scheduledCheck) throws Exception;
 
     abstract boolean ignoreCondition(boolean scheduledCheck, T condition);
 
     abstract boolean doAction(T condition);
 
     boolean conditionsMet(boolean scheduledCheck) throws Exception {
         GregorianCalendar currentTime = new GregorianCalendar();
 
         GregorianCalendar latestAfterActionTime = null;
         GregorianCalendar earlisetBeforeActionTime = null;
         boolean doActionAfter = false;
         boolean doActionBefore = false;
         boolean groupSceneConditionMet = true;
         for (T sac : conditions) {
 
             if (!ignoreCondition(scheduledCheck, sac)) {
                 if (sac.getActionType() == 'T') {
                     GregorianCalendar actionTime = sac.getActionGregorian(currentTime);
                     if (sac.getTimeAfterBefore() == 'A') {
                         if ((latestAfterActionTime == null || !actionTime.before(latestAfterActionTime)) && actionTime.before(currentTime)) {
                             latestAfterActionTime = actionTime;
                             doActionAfter = doAction(sac);
                         }
                     } else if (sac.getTimeAfterBefore() == 'B') {
                         if ((earlisetBeforeActionTime == null || actionTime.before(earlisetBeforeActionTime)) && !actionTime.before(currentTime)) {
                             earlisetBeforeActionTime = actionTime;
                             doActionBefore = doAction(sac);
                         }
                     }
                     else
                         throw new IllegalStateException();
                 }
                 else if (sac.getActionType() == 'G') {
                     if (groupSceneConditionMet) {
                         CGateObject cGateObject = null;
                         try {
                             cGateObject = ApplicationBean1.getCGateSession().getCGateObject(sac.getDependGroup());
                         }
                         catch (Exception e) {
                             throw new CGateException(e);
                         }
                         if (!(cGateObject instanceof Group))
                             throw new IllegalStateException(sac.getDependGroup() + " is not a Group");
 
                         Group dependGroup = (Group)cGateObject;
                         groupSceneConditionMet = ((dependGroup.getLevel() > 0 && sac.getSceneGroupOnOff() == 'O') ||
                                (dependGroup.getLevel() == 0 && sac.getSceneGroupOnOff() == 'F')) && doAction(sac);
                     }
                 }
                 else if (sac.getActionType() == 'S') {
                     if (groupSceneConditionMet) {
                         SceneRunner dependSceneRunner = SceneRunnerFactory.getInstance().getSceneRunner(sac.getDependScene());
                         groupSceneConditionMet = (((dependSceneRunner.status == SceneRunner.ACTIVE ||
                                 dependSceneRunner.status == SceneRunner.ACTIVATING) && sac.getSceneGroupOnOff() == 'A') ||
                                 ((dependSceneRunner.status == SceneRunner.INACTIVE ||
                                dependSceneRunner.status == SceneRunner.RESETTING) && sac.getSceneGroupOnOff() == 'I')) &&
                                 doAction(sac);
                     }
                 }
                 else
                     throw new IllegalStateException();
             }
         }
 
         SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
         String timeLogMessage = "";
         if (latestAfterActionTime != null)
             timeLogMessage += "doActionAfter: " + doActionAfter + " - " + formatter.format(latestAfterActionTime.getTime());
         if (earlisetBeforeActionTime != null)
             timeLogMessage += "doActionBefore: " + doActionBefore + " - " + formatter.format(earlisetBeforeActionTime.getTime());
         if (latestAfterActionTime != null || earlisetBeforeActionTime != null)
            log.info(timeLogMessage);
 
         boolean timeConditionMet = (latestAfterActionTime == null || doActionAfter) && (earlisetBeforeActionTime == null || doActionBefore);
 
         log.info("timeConditionMet: " + timeConditionMet + ", groupSceneConditionMet: " + groupSceneConditionMet);
 
         return timeConditionMet && groupSceneConditionMet;
     }
 }
