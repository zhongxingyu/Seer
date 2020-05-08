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
 import com.daveoxley.cbus.CGateObject;
 import com.daveoxley.cbus.Group;
 import com.daveoxley.cbus.Response;
import java.io.Serializable;
 import java.net.UnknownHostException;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Dave Oxley <dave@daveoxley.co.uk>
  */
public class SceneAction implements Serializable {
     private int sceneId;
     private int sceneActionId;
     private String address;
     private char type;
     private int delay;
     private int duration;
     private int level;
     private int originalLevel;
     private Set<SceneActionCondition> sceneActionConditions;
 
     public String getAddress() {
         return address;
     }
 
     public void setAddress(String address) {
         this.address = address;
     }
 
     public int getDelay() {
         return delay;
     }
 
     public void setDelay(int delay) {
         this.delay = delay;
     }
 
     public int getDuration() {
         return duration;
     }
 
     public void setDuration(int duration) {
         this.duration = duration;
     }
 
     public int getLevel() {
         return level;
     }
 
     public void setLevel(int level) {
         this.level = level;
     }
 
     public int getSceneActionId() {
         return sceneActionId;
     }
 
     public void setSceneActionId(int sceneActionId) {
         this.sceneActionId = sceneActionId;
     }
 
     public int getSceneId() {
         return sceneId;
     }
 
     public void setSceneId(int sceneId) {
         this.sceneId = sceneId;
     }
 
     public char getType() {
         return type;
     }
 
     public void setType(char type) {
         this.type = type;
     }
 
     public int getOriginalLevel() {
         return originalLevel;
     }
 
     public void setOriginalLevel(int originalLevel) {
         this.originalLevel = originalLevel;
     }
 
     public Set<SceneActionCondition> getSceneActionConditions() {
         return sceneActionConditions;
     }
 
     public void setSceneActionConditions(Set<SceneActionCondition> sceneActionConditions) {
         this.sceneActionConditions = sceneActionConditions;
     }
 
     public String getGroupName() {
         try {
             Group group = (Group) ApplicationBean1.getCGateSession().getCGateObject(getAddress());
             return group.getName();
         } catch (Exception ex) {
             Logger.getLogger(Scene.class.getName()).log(Level.SEVERE, null, ex);
         }
         return "";
     }
 
     private transient Group group = null;
 
     void prepareAction(boolean run) throws CGateException {
         CGateObject cGateObject = null;
         try {
             cGateObject = ApplicationBean1.getCGateSession().getCGateObject(getAddress());
         }
         catch (Exception e) {
             throw new CGateException(e);
         }
         if (!(cGateObject instanceof Group))
             throw new IllegalStateException(getAddress() + " is not a Group");
 
         group = (Group)cGateObject;
         if (run)
             originalLevel = group.getLevel();
     }
 
     Response run() throws UnknownHostException, CGateConnectException, CGateException {
         if (getType() == 'O')
             return group.on();
         else if (getType() == 'F')
             return group.off();
         else if (getType() == 'R')
             return group.ramp(getLevel(), getDuration());
 
         throw new IllegalStateException(getType() + " is not 'O', 'F' or 'R'");
     }
 
     Response reset() throws UnknownHostException, CGateConnectException, CGateException {
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
 
         if (getType() == 'O') {
             if (originalLevel == 0)
                 return group.off();
             else if (originalLevel < 255)
                 return group.ramp(originalLevel, 0);
             return null;
         }
         else if (getType() == 'F') {
             if (originalLevel == 255)
                 return group.on();
             else if (originalLevel > 0)
                 return group.ramp(originalLevel, 0);
             return null;
         }
         else if (getType() == 'R') {
             return group.ramp(originalLevel, getDuration());
         }
         return null;
     }
 }
