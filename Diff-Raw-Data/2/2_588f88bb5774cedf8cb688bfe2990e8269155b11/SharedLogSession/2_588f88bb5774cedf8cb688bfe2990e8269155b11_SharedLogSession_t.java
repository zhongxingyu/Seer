 /**
  * Copyright (C) 2011 - 101loops.com <dev@101loops.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.crashnote.core.report.impl.session;
 
 import com.crashnote.core.config.Config;
 import com.crashnote.core.model.log.*;
 
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * Implementation of a {@link ILogSession} which uses {@link LogHeap} to store the log events
  * and a simple {@link HashMap} to manage the context data.
  */
 public class SharedLogSession
     implements ILogSession {
 
     /**
      * Collection of log events (=heap)
      */
     private final LogHeap heap;
 
     /**
      * Key/Value mapping of context data
      */
     private final Map<String, Object> context;
 
     // SETUP ======================================================================================
 
     public SharedLogSession() {
         this.heap = new LogHeap();
         this.context = new HashMap<String, Object>();
     }
 
     public SharedLogSession(final SharedLogSession session) {
         this.heap = new LogHeap(session.heap);
         this.context = new HashMap<String, Object>(session.context);
     }
 
     // INTERFACE ==================================================================================
 
     public ILogSession copy() {
         return this; // don't actually copy, just reference
     }
 
     public void clear() {
         clearEvents();
         clearCtx();
     }
 
     // ===== EVENTS
 
     public List<LogEvt<?>> getEvents() {
         return heap.getEvents();
     }
 
     public void addEvent(final LogEvt<?> evt) {
         heap.addEvt(evt);
     }
 
     public void clearEvents() {
         heap.clear();
     }
 
     public boolean isEmpty() {
        return heap.isEmpty();
     }
 
     // ===== CONTEXT
 
     public void putCtx(final String key, final Object val) {
         context.put(key, val);
     }
 
     public void removeCtx(final String key) {
         context.remove(key);
     }
 
     public void clearCtx() {
         context.clear();
     }
 
     public boolean hasContext() {
         return !context.isEmpty();
     }
 
     public Map<String, Object> getContext() {
         return context;
     }
 }
