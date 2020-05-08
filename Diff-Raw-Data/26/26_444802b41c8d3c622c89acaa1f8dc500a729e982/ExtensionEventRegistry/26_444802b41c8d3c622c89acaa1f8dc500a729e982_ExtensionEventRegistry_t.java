 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.apache.myfaces.extensions.scripting.sandbox.extensionevents;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 /**
  * @author Werner Punz (latest modification by $Author$)
  * @version $Revision$ $Date$
  */
 
 public class ExtensionEventRegistry {
 
     Collection<ExtensionEventListener> _listeners = new ConcurrentLinkedQueue<ExtensionEventListener>();
 
     public void addListener(ExtensionEventListener listener) {
         _listeners.add(listener);
     }
 
     public void removeListener(ExtensionEvent listener) {
         _listeners.remove(listener);
     }
 
     public void removeAllListeners(Class eventType) {
         Iterator<ExtensionEventListener> it = _listeners.iterator();
        while (it.hasNext()) {
            if (it.next().getClass() == eventType) {
                 it.remove();
             }
         }
     }
 
     public void clear() {
         _listeners.clear();
     }
 
     public void sendEvent(ExtensionEvent evt) {
         for (ExtensionEventListener listener : _listeners) {
            if (listener.isListenerFor(evt)) {
                listener.handleEvent(evt);
            }
         }
     }
 
 }
