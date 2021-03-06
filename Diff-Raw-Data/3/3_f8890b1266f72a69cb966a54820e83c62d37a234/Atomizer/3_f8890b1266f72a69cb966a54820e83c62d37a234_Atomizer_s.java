 /*
  * Copyright 2013 JBoss, by Red Hat, Inc
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.jboss.errai.otec.client.atomizer;
 
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimap;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.event.dom.client.KeyEvent;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.EventListener;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.ValueBoxBase;
 import org.jboss.errai.otec.client.OTEngine;
 import org.jboss.errai.otec.client.OTEntity;
 import org.jboss.errai.otec.client.util.DiffUtil;
 
 /**
  * @author Mike Brock
  */
 public abstract class Atomizer {
   private Atomizer() {
   }
 
   private static final Multimap<Object, HandlerRegistration> HANDLER_REGISTRATION_MAP
       = HashMultimap.create();
 
   public static void syncWidgetWith(final OTEngine engine, final OTEntity entity, final ValueBoxBase widget) {
 
     final EntityChangeStreamImpl entityChangeStream = new EntityChangeStreamImpl(engine, entity);
 
     widget.setValue(entity.getState().get());
 
     HANDLER_REGISTRATION_MAP.put(widget, widget.addKeyDownHandler(new KeyDownHandler() {
       @Override
       public void onKeyDown(final KeyDownEvent event) {
         if (shouldIgnoreKeyPress(event)) {
           return;
         }
 
         if (widget.getSelectedText().length() > 0) {
           entityChangeStream.notifyDelete(widget.getCursorPos(), widget.getSelectedText());
         }
         else if (event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE) {
           final int index = widget.getCursorPos() - 1;
           entityChangeStream.notifyDelete(index, String.valueOf(widget.getText().charAt(index)));
         }
         else if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
           entityChangeStream.notifyInsert(widget.getCursorPos(), "\n");
         }
       }
     }));
 
     HANDLER_REGISTRATION_MAP.put(widget, widget.addKeyPressHandler(new KeyPressHandler() {
       @Override
       public void onKeyPress(final KeyPressEvent event) {
         if (shouldIgnoreKeyPress(event)) {
           return;
         }
 
         if (event.getUnicodeCharCode() != 0) {
           entityChangeStream.notifyInsert(widget.getCursorPos(), String.valueOf(event.getCharCode()));
         }
       }
     }));
 
     DOM.setEventListener(widget.getElement(), new EventListener() {
       @Override
       public void onBrowserEvent(Event event) {
         if (event.getTypeInt() == Event.ONPASTE) {
           final String before = (String) entity.getState().get();
           new Timer() {
             @Override
             public void run() {
               final String after = (String) widget.getValue();
               final DiffUtil.Delta diff = DiffUtil.diff(before, after);
 
               entityChangeStream.notifyInsert(diff.getCursor(), diff.getDeltaText());
             }
           }.schedule(1);
         }
         widget.onBrowserEvent(event);
       }
     });
 
     DOM.sinkEvents(widget.getElement(), DOM.getEventsSunk(widget.getElement()) | Event.ONPASTE);
 
     new Timer() {
       @Override
       public void run() {
         entityChangeStream.flush();
       }
     }.scheduleRepeating(750);
   }
 
   private static boolean shouldIgnoreKeyPress(KeyEvent event) {
     if (event.isMetaKeyDown() || event.isControlKeyDown()) {
       return true;
     }
 
     int keyCode;
     if (event instanceof KeyDownEvent) {
       keyCode = ((KeyDownEvent) event).getNativeKeyCode();
     }
     else if (event instanceof KeyPressEvent) {
       keyCode = ((KeyPressEvent) event).getUnicodeCharCode();
       return keyCode == 0;
     }
     else {
       return true;
     }
 
     switch (keyCode) {
       case KeyCodes.KEY_DOWN:
       case KeyCodes.KEY_LEFT:
       case KeyCodes.KEY_UP:
       case KeyCodes.KEY_RIGHT:
       case KeyCodes.KEY_ESCAPE:
       case KeyCodes.KEY_PAGEDOWN:
       case KeyCodes.KEY_PAGEUP:
       case KeyCodes.KEY_HOME:
       case KeyCodes.KEY_END:
         return true;
     }
 
     return false;
   }
 
 }
