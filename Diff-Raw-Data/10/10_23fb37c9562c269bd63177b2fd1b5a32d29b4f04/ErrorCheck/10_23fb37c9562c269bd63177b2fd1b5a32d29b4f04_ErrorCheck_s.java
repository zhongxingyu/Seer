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
 import java.util.ListIterator;
 
 /**
  * Base class for the hint-based error system
  * <p/>
  * To understand the usefulness of this class and of the hint-based error system, consider how you should solve configuration
  * errors when using a GUI or some other approaches that let the user set some parameters without the possibility of
  * compile-time error checking. You want that the user is notified in some way that the configuration options that her
  * has used are invalid, either because they're not valid one by one or they are not consistent with each other. When using
  * a GUI, you don't want that the program is interrupted or the workflow gets stopped, because the current configuration
  * might be a work-in-progress, as the user has still not completed it or has just messed around and don't know how to fix
  * things. You want that some hints appear, that help the user fix the errors in the configuration. Perhaps you want to
  * block some actions until the errors are fixed, but in general you don't want to throw exceptions or such things.
  * <p/>
  * In these cases, the hint-based error system may come in your help. By using {@link Node#addErrorCheck(ErrorCheck)}
  * you can add error checks to the configuration of the relative Node. For example,
  * consider a {@link ObjectNode} that has a property {@code percent} of type {@code int} and you want that property to stay
  * between 0 and 100. You can do the following:
  * <pre>
  *     public class Example extends ObjectNode {
  *         {@literal @}Property int percent;
  *
  *         public Example() {
  *             addErrorCheck(new RangeCheck("percent", 0, 100));
  *         }
  *     }
  * </pre>
  * This way, you can still set {@code percent} with any integer, but if you invoke {@link com.objectgraph.core.Node#getErrors()},
  * or {@link RootedProperty#getErrors()} with a {@link RootedProperty} object connected with the "percent"
  * property, you obtain a set (or a map) of configuration errors. You could display it in
  * some way, by either showing it on an list view or by changing the color of the editor, or in any way you want.
  * <p/>
  * As any other {@link NodeHelper}, you can access the Node that registered the ErrorCheck through {@link #getNode()}.
  *
  * @param <N> The subtype of {@link Node} to which you want this ErrorCheck to be registered
  * @param <T> the type of value that we should check
  */
 public abstract class ErrorCheck<N extends Node, T> extends NodeHelper<N> {
 
     private final String path;
 
     private final Error.Level level;
 
     /**
      * Instantiate a new ErrorCheck on the given path
      *
      * @param path the path that will be checked using {@link #getError()}
      */
     public ErrorCheck(Error.Level level, String path) {
         this.path = path;
         this.level = level;
     }
 
     /**
      * Obtain the path to the checked property
      *
      * @return a String object with the path of the checked property
      */
     public String getPath() {
         return path;
     }
 
     /**
      *
      * @return
      */
     public Error.Level getLevel() {
         return level;
     }
 
     /**
      * Return the configuration {@link Error} using the current value.
      *
      * @return an {@link Error} instance, or {@code null} if this ErrorCheck shouldn't apply
      */
     public Error getError() {
         T value = getNode().get(getPath());
         if (value == null)
             return null;
         return getError(value);
     }
 
     /**
      * Given a value, returns a configuration error if it is the case.
      *
      * With this method you can use this ErrorCheck to obtain an error for a value not assigned to the relative property.
      *
      * @param value the value to check
      * @return an {@link Error} instance, or {@code null} if the error doesn't apply.
      */
     public Error getError(T value) {
        return new Error(level, getMessage(value));
     }
 
     protected abstract String getMessage(T value);
 
     /**
      *
      * @param list
      */
     public void filter(List<T> list) {
         ListIterator<T> it = list.listIterator();
         while (it.hasNext()) {
             T t = it.next();
             if (getError(t) != null)
                 it.remove();
         }
     }
 
 }
