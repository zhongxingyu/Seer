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
 
 package com.daveoxley.cnery.entities;
 
 import java.io.Serializable;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import org.jboss.seam.annotations.Name;
 
 /**
  *
  * @author Dave Oxley <dave@daveoxley.co.uk>
  */
 @Entity
 @Name("sceneActionConditionEntity")
 @Table(name="scene_action_condition")
 public class SceneActionCondition extends AbstractCondition implements Serializable {
     public enum SceneState {
         ACTIVE("While Scene is active"),
         TRIGGERED("When Scene is triggered");
 
         private final String description;
 
         SceneState(String description) {
             this.description = description;
         }
 
         public String getDescription() {
             return description;
         }
     };
     public enum Action {
        ALLOW("action"),
         NO_ACTION("do not action");
 
         private final String description;
 
         Action(String description) {
             this.description = description;
         }
 
         public String getDescription() {
             return description;
         }
     };
 
     private SceneAction sceneAction;
     private SceneState sceneState;
     private Action action;
 
     @ManyToOne
     @JoinColumn(name="scene_action_id")
     public SceneAction getSceneAction() { return sceneAction; }
     public void setSceneAction(SceneAction sceneAction) { this.sceneAction = sceneAction; }
 
     @Column(name="scene_state")
     @Enumerated(EnumType.STRING)
     public SceneState getSceneState() { return sceneState; }
     public void setSceneState(SceneState sceneState) { this.sceneState = sceneState; }
 
     @Column(name="action")
     @Enumerated(EnumType.STRING)
     public Action getAction() { return action; }
     public void setAction(Action action) { this.action = action; }
 }
