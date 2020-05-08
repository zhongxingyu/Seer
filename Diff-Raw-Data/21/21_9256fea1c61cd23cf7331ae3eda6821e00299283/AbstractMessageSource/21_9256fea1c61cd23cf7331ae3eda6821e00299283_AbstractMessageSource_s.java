 /*
  * Copyright 2013 Roland Gisler, GISLER iNFORMATiK, Switzerland.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package ch.gitik.bpms.common;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
 * Basisklasse fuer eine Message-Quelle. Registriert und verwaltet
 * MessageListener.
  * @author Roland Gisler
  */
 public abstract class AbstractMessageSource {
 
    private List<MessageListener> listerners = new ArrayList<MessageListener>();
 
    /**
     * Registriert einen Listener.
     * @param listener
     *           MessageListener.
     */
   public void addMessageListener(MessageListener listener) {
       this.listerners.add(listener);
    }
 
    /**
     * Informiert alle Listener mit einer Message.
     * @param msg
     *           Message.
     */
   protected void fireMessageReceived(Message msg) {
      for (MessageListener listener : this.listerners) {
          listener.messageReceived(msg);
       }
    }
 
    /**
     * Deregistriert einen Listener.
     * @param listener
     *           MessageListener.
     */
   public void removeMessageListener(MessageListener listener) {
       this.listerners.remove(listener);
    }
 }
