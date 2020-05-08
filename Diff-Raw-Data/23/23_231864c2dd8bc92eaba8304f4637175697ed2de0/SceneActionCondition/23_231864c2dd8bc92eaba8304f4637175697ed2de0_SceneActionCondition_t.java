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
 
import java.io.Serializable;

 /**
  *
  * @author Dave Oxley <dave@daveoxley.co.uk>
  */
public class SceneActionCondition implements Serializable {
     private int sceneId;
     private int sceneActionId;
     private int sceneActionConditionId;
     private char sceneState;
     private char action;
     private char actionType;
     private char timeAfterBefore;
     private char timeWhen;
     private char timePlusMinus;
     private int timeMinutes;
     private String dependGroup;
     private Scene dependScene;
     private char sceneGroupOnOff;
 
     public int getSceneActionConditionId() {
         return sceneActionConditionId;
     }
 
     public void setSceneActionConditionId(int sceneActionConditionId) {
         this.sceneActionConditionId = sceneActionConditionId;
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
 
     public char getAction() {
         return action;
     }
 
     public void setAction(char action) {
         this.action = action;
     }
 
     public char getActionType() {
         return actionType;
     }
 
     public void setActionType(char actionType) {
         this.actionType = actionType;
     }
 
     public String getDependGroup() {
         return dependGroup;
     }
 
     public void setDependGroup(String dependGroup) {
         this.dependGroup = dependGroup;
     }
 
     public Scene getDependScene() {
         return dependScene;
     }
 
     public void setDependScene(Scene dependScene) {
         this.dependScene = dependScene;
     }
 
     public char getSceneGroupOnOff() {
         return sceneGroupOnOff;
     }
 
     public void setSceneGroupOnOff(char sceneGroupOnOff) {
         this.sceneGroupOnOff = sceneGroupOnOff;
     }
 
     public char getSceneState() {
         return sceneState;
     }
 
     public void setSceneState(char sceneState) {
         this.sceneState = sceneState;
     }
 
     public char getTimeAfterBefore() {
         return timeAfterBefore;
     }
 
     public void setTimeAfterBefore(char timeAfterBefore) {
         this.timeAfterBefore = timeAfterBefore;
     }
 
     public int getTimeMinutes() {
         return timeMinutes;
     }
 
     public void setTimeMinutes(int timeMinutes) {
         this.timeMinutes = timeMinutes;
     }
 
     public char getTimePlusMinus() {
         return timePlusMinus;
     }
 
     public void setTimePlusMinus(char timePlusMinus) {
         this.timePlusMinus = timePlusMinus;
     }
 
     public char getTimeWhen() {
         return timeWhen;
     }
 
     public void setTimeWhen(char timeWhen) {
         this.timeWhen = timeWhen;
     }
 }
