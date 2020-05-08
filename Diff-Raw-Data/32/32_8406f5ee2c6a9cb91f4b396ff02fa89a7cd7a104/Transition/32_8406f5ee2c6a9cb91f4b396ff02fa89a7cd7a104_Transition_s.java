 package org.faur.model;
 
 import org.faur.api.model.IPlace;
 import org.faur.api.model.ITransition;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * (C) Copyright 2013 Faur Ioan-Aurel.
  * <p/>
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl-2.1.html
  * <p/>
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  * <p/>
  * Created with IntelliJ IDEA.
  * User: faur
  * Date: 10/19/13
  * Time: 5:36 PM
  */
 public class Transition implements ITransition {
     private static final int TOKENS_TO_REMOVE = 1;
     private static final int TOKENS_TO_ADD = 1;
 
     private String name;
     private int minTime;
     private int maxTime;
     private int time;
     private boolean fireCalled;
     private int timer;
     List<IPlace> inputPlaces;
     List<IPlace> outputPlaces;
 
     public Transition(String name) {
         this.name = name;
         this.minTime = 0;
         this.maxTime = 1;
         this.time = 0;
         this.timer = 0;
         this.fireCalled = false;
         this.inputPlaces = new ArrayList<IPlace>();
         this.outputPlaces = new ArrayList<IPlace>();
     }
     @Override
     public String getName() {
         return this.name;
     }
 
     @Override
     public void setName(String transitionName) {
         this.name = transitionName;
     }
 
     @Override
     public boolean fire() {
        if (isEnabled()) {
             if (!fireCalled) {
                // remove 1 token from every input location but do not insert them
                // in the output locations. Also set the timer
                for (IPlace place : inputPlaces) {
                    place.removeTokens(TOKENS_TO_REMOVE);
                 }
                timer = time--;
                fireCalled = true;
             } else {
                 timer--;
                 if (timer <= 0) {
                     // insert 1 token in every output location and then reset fireCalled;
                     for (IPlace place : outputPlaces) {
                        place.removeTokens(TOKENS_TO_ADD);
                     }
                     fireCalled = false;
                     return true;
                 }
             }
        }
         return false;
     }
 
     @Override
     public List<IPlace> getInputPlaces() {
         return inputPlaces;
     }
 
     @Override
     public void setInputPlaces(List<IPlace> inputPlaces) {
         this.inputPlaces = inputPlaces;
     }
 
     @Override
     public List<IPlace> getOutputPlaces() {
         return this.outputPlaces;
     }
 
     @Override
     public void setOutputPlaces(List<IPlace> outputPlaces) {
         this.outputPlaces = outputPlaces;
     }
 
     @Override
     public void setTime(int minTime, int maxTime) {
         if (minTime >= 0) {
             this.minTime = minTime;
         }
 
         if (maxTime >= this.minTime) {
             this.maxTime = maxTime;
         } else {
             this.minTime = 0;
             this.maxTime = 1;
         }
 
         this.time = this.minTime + (int)(Math.random() * this.maxTime);
     }
 
     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append(this.name).append("[ ").append(String.valueOf(this.time)).append(" ]");
         return builder.toString();
     }
 
     private boolean isEnabled() {
         for (IPlace place : inputPlaces) {
             if (place.getNrOfTokens() <= 0) {
                 return false;
             }
         }
         return true;
     }
 }
