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
 
 package cnerydb;
 
 import cnery.ApplicationBean1;
 import com.daveoxley.cbus.CGateConnectException;
 import com.daveoxley.cbus.CGateException;
 import com.daveoxley.cbus.Group;
 import com.daveoxley.cbus.Response;
 import java.io.Serializable;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.GregorianCalendar;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 /**
  *
  * @author Dave Oxley <dave@daveoxley.co.uk>
  */
 public class Scene implements Serializable {
     private int sceneId;
     private String name;
     private String groupAddress;
     private Set<SceneAction> sceneActions;
 
     public void activate() throws UnknownHostException, CGateConnectException, CGateException {
         GregorianCalendar startTime = new GregorianCalendar();
         ArrayList<SceneAction> delayOrderedActions = new ArrayList<SceneAction>(sceneActions);
         Collections.sort(delayOrderedActions, new Comparator<SceneAction>() {
 
             @Override
             public int compare(SceneAction o1, SceneAction o2) {
                 return (o1.getDelay() < o2.getDelay() ? -1 : (o1.getDelay() == o2.getDelay() ? 0 : 1));
             }
         });
 
         Session session = HibernateUtil.getSessionFactory().getCurrentSession();
         Transaction tx = null;
 
         try {
             tx = session.beginTransaction();
 
             ArrayList<Response> responses = new ArrayList<Response>();
             for (SceneAction sceneAction : delayOrderedActions) {
                 if (sceneAction.getDelay() <= 0) {
                     sceneAction.prepareAction(true);
                     session.update(sceneAction);
                 }
             }
 
             for (SceneAction sceneAction : delayOrderedActions) {
                 if (sceneAction.getDelay() > 0)
                 {
                     sceneAction.prepareAction(true);
                     session.update(sceneAction);
                     GregorianCalendar runTime = (GregorianCalendar) startTime.clone();
                     runTime.add(Calendar.SECOND, sceneAction.getDelay());
                     long when = runTime.getTimeInMillis();
                     long now;
                     while ((now = (new GregorianCalendar()).getTimeInMillis()) < when) {
                         long wait_time = when - now;
                         synchronized (this) {
                             try {
                                 wait(wait_time);
                             }
                             catch (InterruptedException ie) {}
                         }
                     }
                 }
 
                 responses.add(sceneAction.run());
             }
 
             for (Response response : responses)
                 response.handle200();
 
             tx.commit();
         } catch (Exception e) {
             tx.rollback();
             new CGateException(e);
         }
     }
 
     public void reset() throws UnknownHostException, CGateConnectException, CGateException {
         ArrayList<Response> responses = new ArrayList<Response>();
         for (SceneAction sceneAction : sceneActions) {
             sceneAction.prepareAction(false);
         }
 
         for (SceneAction sceneAction : sceneActions) {
             Response response = sceneAction.reset();
             if (response != null)
                 responses.add(response);
         }
 
         for (Response response : responses)
             response.handle200();
 
         for (SceneAction sceneAction : sceneActions) {
             Session session = HibernateUtil.getSessionFactory().getCurrentSession();
             Transaction tx = null;
 
             try {
                 tx = session.beginTransaction();
                 sceneAction.setOriginalLevel(-1);
                 tx.commit();
             } catch (Exception e) {
                 tx.rollback();
                 new CGateException(e);
             }
         }
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public int getSceneId() {
         return sceneId;
     }
 
     public void setSceneId(int sceneId) {
         this.sceneId = sceneId;
     }
 
     public String getGroupAddress() {
         return groupAddress;
     }
 
     public void setGroupAddress(String groupAddress) {
         this.groupAddress = groupAddress;
     }
 
     public Set getSceneActions() {
         return sceneActions;
     }
 
     public void setSceneActions(Set sceneActions) {
         this.sceneActions = sceneActions;
     }
 
     public String getGroupName() {
         try {
             Group group = (Group) ApplicationBean1.getCGateSession().getCGateObject(getGroupAddress());
             if (group != null)
                 return group.getName();
         } catch (Exception ex) {
             Logger.getLogger(Scene.class.getName()).log(Level.SEVERE, null, ex);
         }
         return "";
     }
 }
