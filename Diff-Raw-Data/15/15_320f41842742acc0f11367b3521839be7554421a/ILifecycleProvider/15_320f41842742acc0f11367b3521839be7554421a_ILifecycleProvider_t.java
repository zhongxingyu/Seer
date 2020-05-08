 package com.ewerp.mud.plugins;
 
 /**
  * Copyright 2012 Curtis Boyden
  * <p/>
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * <p/>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p/>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 /**
  * {@link ILifecycleProvider} is implemented by the main controller of the
  * system.
  *
  * @author cboyden
  */
 public interface ILifecycleProvider {
 
     /**
      * Register an {@link IPlugin} as a listener of the system lifecycle state.
      *
      * @param listener A listener for the system lifecycle state <br />
      *                 <ul>
      *                 <li>{@link ILifecycleListener} : A valid listener implementing
      *                 {@link ILifecycleListener}</li>
      *                 <li>null : A null value will result in an
      *                 {@link IllegalArgumentException}</li>
      *                 <ul>
      *                 <p/>
      *                 throws IllegalArgumentException Must be thrown if
      *                 {@link ILifecycleListener} is null
     *
     * @throws IllegalArgumentException Must be thrown if {@link ILifecycleListener} is null
      */
     public void registerListener(ILifecycleListener listener) throws IllegalArgumentException;
 
     /**
      * Unregister an {@link IPlugin} as a listener of the system lifecycle state.
      *
      * @param listener An existing listener for the system lifecycle state <br />
      *                 <ul>
      *                 <li>{@link ILifecycleListener} : A valid listener implementing
      *                 {@link ILifecycleListener}</li>
      *                 <li>null : Nothing will happen if listener is null</li>
      *                 <li>If the listener cannot be found, nothing will happen</li>
      *                 <ul>
      *                 <p/>
      */
    public void unregisterListener(ILifecycleListener listener);
 }
