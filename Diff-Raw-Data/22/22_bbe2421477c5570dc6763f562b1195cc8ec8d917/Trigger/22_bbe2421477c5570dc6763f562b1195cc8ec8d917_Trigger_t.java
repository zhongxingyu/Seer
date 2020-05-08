 /*
  * Copyright 2013 Emanuele Tamponi
  *
  * This file is part of object-graph.
  *
  * object-graph is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * object-graph is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with object-graph.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.objectgraph.core;
 
 import java.util.List;
 
 /**
  * Base class to use for reacting to {@link Event}s.
  * <p/>
  * Each time an {@link Event} reaches a {@link Node}, each Trigger registered to that Node gets checked and, if it is
  * the case, activated. To activate a Trigger, the method {@link #isTriggeredBy(Event)} has to return {@code true}. Upon
  * activation, the method {@link #action(Event)} is called. To access the {@link Node} connected with this Trigger, you
  * can use the {@link #getNode()} method, that returns the Node casted to the type used in the generic.
  * <p/>
  * A Trigger can <i>control</i> some properties, whose paths (relative to the Node that registered the Trigger) are given
  * by {@link #getControlledPaths()}. <i>Controlled</i> or <i>bound</i> properties are not restricted in any way, but you
  * can always ask a Node for its controlled properties, by using {@link Node#getControlledProperties()}. This would be
  * particularly useful in a GUI, for example.
  * <p/>
  * object-graph comes with some already implemented Triggers. See them as a reference or use them directly if they meet
  * your needs.
  *
  * @param <N> the subclass of {@link Node} returned by {@link #getNode()}
  */
 public abstract class Trigger<N extends Node> extends NodeHelper<N> {
 
     private boolean listening = true;
 
     /**
      * Return the list of controlled paths
      *
      * @return a list of paths, or an empty list if no paths are controlled by this Trigger
      */
     public abstract List<String> getControlledPaths();
 
     /**
      * Check if the {@link Event} activates this Trigger
      * <p/>
      * @param event the {@link Event} that reached the Node
      * @return {@code true} if this Trigger should be activated
      */
     protected abstract boolean isTriggeredBy(Event event);
 
     /**
      * The operation to do if this Trigger gets activated.
      * <p/>
      * You can execute any operation, without worrying about reactivating this same Trigger, as it cannot be reactivated
      * while executing its {@code action()} method.
      *
      * @param event the Event thatNode reached the Node
      */
     protected abstract void action(Event event);
 
    void check(Event event) {
         if (listening && isTriggeredBy(event)) {
             listening = false;
             action(event);
             listening = true;
         }
     }
 
 }
