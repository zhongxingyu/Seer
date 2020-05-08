 package net.sourceforge.gjtapi;
 
 /*
  Copyright (c) 2002 8x8 Inc. (www.8x8.com) 
 
  All rights reserved. 
 
  Permission is hereby granted, free of charge, to any person obtaining a 
  copy of this software and associated documentation files (the 
  "Software"), to deal in the Software without restriction, including 
  without limitation the rights to use, copy, modify, merge, publish, 
  distribute, and/or sell copies of the Software, and to permit persons 
  to whom the Software is furnished to do so, provided that the above 
  copyright notice(s) and this permission notice appear in all copies of 
  the Software and that both the above copyright notice(s) and this 
  permission notice appear in supporting documentation. 
 
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
  OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT 
  OF THIRD PARTY RIGHTS. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR 
  HOLDERS INCLUDED IN THIS NOTICE BE LIABLE FOR ANY CLAIM, OR ANY SPECIAL 
  INDIRECT OR CONSEQUENTIAL DAMAGES, OR ANY DAMAGES WHATSOEVER RESULTING 
  FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, 
  NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION 
  WITH THE USE OR PERFORMANCE OF THIS SOFTWARE. 
 
  Except as contained in this notice, the name of a copyright holder 
  shall not be used in advertising or otherwise to promote the sale, use 
  or other dealings in this Software without prior written authorization 
  of the copyright holder.
  */
 import javax.telephony.callcontrol.CallControlCallObserver;
 import net.sourceforge.gjtapi.events.*;
 import javax.telephony.*;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * This is a package-visible Observer and Listener Manager object that a Call
  * can use to track its set of Observers and Listeners. Listeners are added and
  * removed from here for each call. Note that when a listener is removed,
  * removing from an address or terminal will decrement the usage count, whereas
  * a full remove will remove from all lists.
  * <P>
  * The add and remove methods are synchronized to control orderly access, but
  * the event triggering is not. It is placed in a separate class for clarity.
  * Creation date: (2000-05-01 10:57:44)
  * 
  * @author: Richard Deadman
  */
 class ListenerManager implements TerminalConnectionListener {
 
     /**
      * internal Listener Holder that holds a Listener and all its registers
      * Listeners are tagged by the Terminals or Address that applied them. A
      * "null" indicates that a listener has been directly applied and the
      * Address and Terminal connectivity no longer applies.
      */
     private class ListenerStatus {
         boolean explicit = false;
         Set<Terminal> terminals = new HashSet<Terminal>();
         Set<Address> addresses = new HashSet<Address>();
 
         /**
          * Constructor for an explicit Listener
          */
         ListenerStatus() {
             this.explicit = true;
         }
 
         /**
          * Constructor for a Listener Status triggered by a visitation to an
          * Address with registered CallListeners
          */
         ListenerStatus(Address addr) {
             this.addresses.add(addr);
         }
 
         /**
          * Constructor for a Listener Status triggered by a visitation to an
          * Terminal with registered CallListeners
          */
         ListenerStatus(Terminal term) {
             this.terminals.add(term);
         }
 
         void addApplier(Address addr) {
             if (!explicit)
                 this.addresses.add(addr);
         }
 
         void addApplier(Terminal term) {
             if (!explicit)
                 this.terminals.add(term);
         }
 
         void setExplicit() {
             this.explicit = true;
             this.addresses = null;
             this.terminals = null;
         }
 
         boolean isExplicit() {
             return this.explicit;
         }
 
         /**
          * Remove a Address return true if the Listener should now be removed.
          */
         boolean remove(Address addr) {
             this.addresses.remove(addr);
             return this.shouldRemove();
         }
 
         /**
          * Remove a Terminal
          * 
          * @return ture if the held Listener should now be removed.
          */
         boolean remove(Terminal term) {
             this.terminals.remove(term);
             return this.shouldRemove();
         }
 
         /**
          * Detemine if the Listener is not longer applicable to the Call
          */
         boolean shouldRemove() {
             if (!this.isExplicit() && this.terminals.isEmpty()
                     && this.addresses.isEmpty())
                 return true;
             return false;
         }
     }
 
     /**
      * Internal HashMap wrapper that catches put and remove calls to turn on
      * throttling
      */
 	private class ListenerMap extends ConcurrentHashMap<CallListener, ListenerStatus> {
         static final long serialVersionUID = 0L; // never serialized
 
         public ListenerStatus put(CallListener list, ListenerStatus status) {
         	ListenerStatus res = super.put(list, status);
 
             // send a snapshot back to the key
             getCall().sendSnapShot(list);
 
             return res;
         }
     }
     
 	private class ObserverMap extends ConcurrentHashMap<CallObserver, ListenerStatus> {
         static final long serialVersionUID = 0L; // never serialized
 
         public ListenerStatus put(CallObserver obs, ListenerStatus status) {
         	ListenerStatus res = super.put(obs, status);
 
             // send a snapshot back to the key
             getCall().sendSnapShot(obs);
 
             return res;
         }
     }
     
 
     /**
      * A map of CallObservers to their ListenerStatus object that track who
      * applied the observer to the call.
      */
     private ObserverMap obsMap = new ObserverMap();
     private Set<CallObserver> ccObservers = new HashSet<CallObserver>(); // the subset of observers that
                                                 // listen for callcontrol events
     /**
      * A map of Listeners to their ListenerStatus objects that track who applied
      * the Listener
      */
     private ListenerMap listMap = new ListenerMap(); // all listeners, with their
                                                 // status holder
     private Set<CallListener> connLists = new HashSet<CallListener>(); // subset of listMap.keySet() that
                                             // also receives Connection events
     private Set<CallListener> tcLists = new HashSet<CallListener>(); // subset of connSet that also
                                             // receives TerminalConnection
                                             // events
     // The call that these Listeners are registered against
     private FreeCall call = null;
 
     /**
      * Constructor for a Set of CallListeners for a Call
      */
     ListenerManager(FreeCall c) {
         this.call = c;
     }
 
     /**
      * Add a CallListener to my managed set as an explicitly set CallListener.
      * These are only removed through Call.removeCallListener(). Creation date:
      * (2000-05-01 11:28:09)
      * 
      * @author: Richard Deadman
      * @param l
      *                The new or updated listener
      */
     synchronized void add(CallListener l) {
     	ListenerMap lists = this.getCallListMap();
         ListenerStatus status = (ListenerStatus) lists.get(l);
         if (status == null) {
             status = new ListenerStatus();
             lists.put(l, status);
             this.protect(); // mark as needing gc protection
         } else {
             status.setExplicit();
         }
 
         // see if we need to add to subtype lists
         this.addSubTypes(l);
     }
 
     /**
      * Add a CallListener to my managed set and associate it with an Address
      * that triggered the addition. Creation date: (2000-05-01 11:28:09)
      * 
      * @author: Richard Deadman
      * @param l
      *                The new or updated listener
      * @param addr
      *                An Address visited by a call that held this listener as
      *                one of its CallListeners.
      */
     synchronized void add(CallListener l, Address addr) {
     	ListenerMap map = this.getCallListMap();
         ListenerStatus status = (ListenerStatus) map.get(l);
         if (status == null) {
             status = new ListenerStatus(addr);
             map.put(l, status);
             this.protect(); // mark as needing gc protection
         } else {
             status.addApplier(addr);
         }
 
         // see if we need to register it for extra events
         this.addSubTypes(l);
     }
 
     /**
      * Add a CallListener to my managed set and associate it with a Terminal
      * that triggered the addition. Creation date: (2000-05-01 11:28:09)
      * 
      * @author: Richard Deadman
      * @param l
      *                The new or updated listener
      * @param term
      *                A Terminal visited by a call that held this listener as
      *                one of its CallListeners.
      */
     synchronized void add(CallListener l, Terminal term) {
     	ListenerMap map = this.getCallListMap();
         ListenerStatus status = (ListenerStatus) map.get(l);
         if (status == null) {
             status = new ListenerStatus(term);
             map.put(l, status);
             this.protect(); // mark as needing gc protection
         } else {
             status.addApplier(term);
         }
 
         // see if we need to register it for extra events
         this.addSubTypes(l);
     }
 
     /**
      * Add a CallObserver to my managed set as an explicitly set CallObserver.
      * These are only removed through Call.removeCallObserver(). Creation date:
      * (2000-05-01 11:28:09)
      * 
      * @author: Richard Deadman
      * @param o
      *                The new or updated observer
      */
     synchronized void add(CallObserver o) {
     	ObserverMap obs = this.getObsMap();
         synchronized (obs) {
             ListenerStatus status = (ListenerStatus) obs.get(o);
             if (status == null) {
                 status = new ListenerStatus();
                 obs.put(o, status);
                 this.protect(); // mark as needing gc protection
             } else {
                 status.setExplicit();
             }
 
             // see if we need to add to subtype lists
             this.addSubTypes(o);
         }
     }
 
     /**
      * Add a CallObserver to my managed set and associate it with an Address
      * that triggered the addition. Creation date: (2000-05-01 11:28:09)
      * 
      * @author: Richard Deadman
      * @param o
      *                The new or updated observer
      * @param addr
      *                An Address visited by a call that held this observer as
      *                one of its CallObservers.
      */
     synchronized void add(CallObserver o, Address addr) {
     	ObserverMap map = this.getObsMap();
         synchronized (map) {
             ListenerStatus status = (ListenerStatus) map.get(o);
             if (status == null) {
                 status = new ListenerStatus(addr);
                 map.put(o, status);
                 this.protect(); // mark as needing gc protection
             } else {
                 status.addApplier(addr);
             }
 
             // see if we need to register it for extra events
             this.addSubTypes(o);
         }
     }
 
     /**
      * Add a CallObserver to my managed set and associate it with a Terminal
      * that triggered the addition. Creation date: (2000-05-01 11:28:09)
      * 
      * @author: Richard Deadman
      * @param o
      *                The new or updated observer
      * @param term
      *                A Terminal visited by a call that held this observer as
      *                one of its CallObservers.
      */
     synchronized void add(CallObserver o, Terminal term) {
         ObserverMap map = this.getObsMap();
         synchronized (map) {
             ListenerStatus status = (ListenerStatus) map.get(o);
             if (status == null) {
                 status = new ListenerStatus(term);
                 map.put(o, status);
                 this.protect(); // mark as needing gc protection
             } else {
                 status.addApplier(term);
             }
 
             // see if we need to register it for extra events
             this.addSubTypes(o);
         }
     }
 
     /**
      * This adds a Call Listener to the Connection and TerminalConnection sets
      * if it has the right signature
      */
     private void addSubTypes(CallListener cl) {
         if (cl instanceof ConnectionListener) {
             this.getConnLists().add(cl);
             if (cl instanceof TerminalConnectionListener) {
                 this.getTcLists().add(cl);
             }
         }
     }
 
     /**
      * This adds a Call Observer to the CallControl set if it is of the right
      * type
      */
     private void addSubTypes(CallObserver co) {
         if (co instanceof javax.telephony.callcontrol.CallControlCallObserver) {
             this.getCcObservers().add(co);
         }
     }
 
     /**
      * callActive method comment.
      */
     public void callActive(javax.telephony.CallEvent event) {
         Iterator<CallListener> it = this.getCallListenerIterator();
         while (it.hasNext()) {
             it.next().callActive(event);
         }
     }
 
     /**
      * callEventTransmissionEnded method comment.
      */
     public void callEventTransmissionEnded(javax.telephony.CallEvent event) {
         Iterator<CallListener> it = this.getCallListenerIterator();
         while (it.hasNext()) {
             it.next().callEventTransmissionEnded(event);
         }
     }
 
     /**
      * callInvalid method comment.
      */
     public void callInvalid(javax.telephony.CallEvent event) {
         Iterator<CallListener> it = this.getCallListenerIterator();
         while (it.hasNext()) {
             it.next().callInvalid(event);
         }
     }
 
     /**
      * connectionAlerting method comment.
      */
     public void connectionAlerting(javax.telephony.ConnectionEvent event) {
         Iterator<CallListener> it = this.getConnListeners();
         while (it.hasNext()) {
         	CallListener o = it.next();
             if (o instanceof ConnectionListener)
                 ((ConnectionListener) o).connectionAlerting(event);
         }
     }
 
     /**
      * connectionConnected method comment.
      */
     public void connectionConnected(javax.telephony.ConnectionEvent event) {
         Iterator<CallListener> it = this.getConnListeners();
         while (it.hasNext()) {
         	CallListener o = it.next();
             if (o instanceof ConnectionListener)
                 ((ConnectionListener) o).connectionConnected(event);
         }
     }
 
     /**
      * connectionCreated method comment.
      */
     public void connectionCreated(javax.telephony.ConnectionEvent event) {
         Iterator<CallListener> it = this.getConnListeners();
         while (it.hasNext()) {
         	CallListener o = it.next();
             if (o instanceof ConnectionListener)
                 ((ConnectionListener) o).connectionCreated(event);
         }
     }
 
     /**
      * connectionDisconnected method comment.
      */
     public void connectionDisconnected(javax.telephony.ConnectionEvent event) {
         Iterator<CallListener> it = this.getConnListeners();
         while (it.hasNext()) {
         	CallListener o = it.next();
             if (o instanceof ConnectionListener)
                 ((ConnectionListener) o).connectionDisconnected(event);
         }
     }
 
     /**
      * connectionFailed method comment.
      */
     public void connectionFailed(javax.telephony.ConnectionEvent event) {
         Iterator<CallListener> it = this.getConnListeners();
         while (it.hasNext()) {
         	CallListener o = it.next();
             if (o instanceof ConnectionListener)
                 ((ConnectionListener) o).connectionFailed(event);
         }
     }
 
     /**
      * connectionInProgress method comment.
      */
     public void connectionInProgress(javax.telephony.ConnectionEvent event) {
         Iterator<CallListener> it = this.getConnListeners();
         while (it.hasNext()) {
         	CallListener o = it.next();
             if (o instanceof ConnectionListener)
                 ((ConnectionListener) o).connectionInProgress(event);
         }
     }
 
     /**
      * connectionUnknown method comment.
      */
     public void connectionUnknown(javax.telephony.ConnectionEvent event) {
         Iterator<CallListener> it = this.getConnListeners();
         while (it.hasNext()) {
         	CallListener o = it.next();
             if (o instanceof ConnectionListener)
                 ((ConnectionListener) o).connectionUnknown(event);
         }
     }
 
     /**
      * Internal accessor for the managed call
      * 
      * @return The Call I manage Listeners for
      */
     private FreeCall getCall() {
         return this.call;
     }
 
     /**
      * Base Call Listener iterator accessor Creation date: (2000-05-01 11:35:21)
      * 
      * @author: Richard Deadman
      * @return An Iterator over all registered callListeners.
      */
     private Iterator<CallListener> getCallListenerIterator() {
         return this.getCallListMap().keySet().iterator();
     }
 
     /**
      * Base Call Listener Set accessor Creation date: (2000-05-01 11:35:21)
      * 
      * @author: Richard Deadman
      * @return The set of managed call listeners.
      */
     Set<CallListener> getCallListeners() {
         return this.getCallListMap().keySet();
     }
 
     /**
      * Internal Listener to status map accessor. Creation date: (2000-05-01
      * 11:35:21)
      * 
      * @author: Richard Deadman
      * @return The Map of CallListeners to CallListener status holders.
      */
     private ListenerMap getCallListMap() {
         return this.listMap;
     }
 
     /**
      * Base Call Observer Set accessor Creation date: (2000-05-01 11:35:21)
      * 
      * @author: Richard Deadman
      * @return The set of managed call observers.
      */
     Set<CallObserver> getCallObservers() {
         return this.getObsMap().keySet();
     }
 
     /**
      * Internal CallControl Observer set accessor Creation date: (2000-05-01
      * 11:35:21)
      * 
      * @author: Richard Deadman
      * @return The set of CXallControlCallObservers.
      */
     private Set<CallObserver> getCcObservers() {
         return this.ccObservers;
     }
 
     /**
      * Base Connection Listener iterator accessor Creation date: (2000-05-01
      * 11:35:21)
      * 
      * @author: Richard Deadman
      * @return An Iterator over all registered ConnectionListeners.
      */
     private Iterator<CallListener> getConnListeners() {
         return this.getConnLists().iterator();
     }
 
     /**
      * Internal accessor for the Set of ConnectionListeners Creation date:
      * (2000-05-02 9:49:25)
      * 
      * @author: Richard Deadman
      * @return A set of ConnectionListeners for Connection and Call events.
      */
     private Set<CallListener> getConnLists() {
         return connLists;
     }
 
     /**
      * Internal Observer to status map accessor. Creation date: (2000-05-01
      * 11:35:21)
      * 
      * @author: Richard Deadman
      * @return The Map of CallObservers to status holders.
      */
     private ObserverMap getObsMap() {
         return this.obsMap;
     }
 
     /**
      * TerminalConnection Listener iterator accessor Creation date: (2000-05-01
      * 11:35:21)
      * 
      * @author: Richard Deadman
      * @return An Iterator over all registered TerminalConnectionListeners.
      */
     private Iterator<CallListener> getTcListeners() {
         return this.getTcLists().iterator();
     }
 
     /**
      * Internal accessor for the Set of TerminalConnectionListeners Creation
      * date: (2000-05-02 9:49:25)
      * 
      * @author: Richard Deadman
      * @return A set of TerminalConnectionListeners for TerminalConnection,
      *         Connection and Call events.
      */
     private Set<CallListener> getTcLists() {
         return tcLists;
     }
 
     /**
      * Note if their are no registered listeners or observers. Creation date:
      * (2000-06-23 11:58:01)
      * 
      * @author: Richard Deadman
      * @return boolean
      */
     boolean isEmpty() {
         return (this.getCallListMap().isEmpty() && this.getObsMap().isEmpty());
     }
 
     /**
      * multiCallMetaMergeEnded method comment.
      */
     public void multiCallMetaMergeEnded(javax.telephony.MetaEvent event) {
         Iterator<CallListener> it = this.getCallListenerIterator();
         while (it.hasNext()) {
             it.next().multiCallMetaMergeEnded(event);
         }
     }
 
     /**
      * multiCallMetaMergeStarted method comment.
      */
     public void multiCallMetaMergeStarted(javax.telephony.MetaEvent event) {
         Iterator<CallListener> it = this.getCallListenerIterator();
         while (it.hasNext()) {
             it.next().multiCallMetaMergeStarted(event);
         }
     }
 
     /**
      * multiCallMetaTransferEnded method comment.
      */
     public void multiCallMetaTransferEnded(javax.telephony.MetaEvent event) {
         Iterator<CallListener> it = this.getCallListenerIterator();
         while (it.hasNext()) {
             it.next().multiCallMetaTransferEnded(event);
         }
     }
 
     /**
      * multiCallMetaTransferStarted method comment.
      */
     public void multiCallMetaTransferStarted(javax.telephony.MetaEvent event) {
         Iterator<CallListener> it = this.getCallListenerIterator();
         while (it.hasNext()) {
             it.next().multiCallMetaTransferStarted(event);
         }
     }
 
     /**
      * If we are about to add our first observer or listener, mark this call as
      * needing garbage collection protection. Creation date: (2000-06-23
      * 12:06:47)
      * 
      * @author: Richard Deadman
      */
     private void protect() {
         // check if we should protect the call
         if (this.isEmpty()) {
             FreeCall call = this.getCall();
             ((GenericProvider) call.getProvider()).getCallMgr().protect(call);
         }
     }
 
     /**
      * Remove a CallListener applier to my managed set and the CallListener
      * itself, if necessary. Creation date: (2000-05-01 11:28:09)
      * 
      * @author: Richard Deadman
      * @param addr
      *                An Address visited by a call that held this listener as
      *                one of its CallListeners.
      */
     synchronized void remove(Address addr) {
         ListenerMap lists = this.getCallListMap();
         LinkedList<CallListener> itemsToRemove = new LinkedList<CallListener>();
         Iterator<CallListener> it = lists.keySet().iterator();
         while (it.hasNext()) {
             CallListener cl = it.next();
             ListenerStatus status = lists.get(cl);
             if (status != null) {
                 if (status.remove(addr)) {
                     // the address was the last listener handle -- remove the
                     // listener, subtypes and notify
                 	
                 	// we hold it and remove it outside the loop to avoid a ConcurrentModificationException
                 	// we can't just use it.remove() since we have to do some extra logic
                 	itemsToRemove.add(cl);
                 }
             }
         }
         
         // Now remove any found items
         for(CallListener cl : itemsToRemove) {
        	lists.remove(cl);
         }
 
         // now remove any registered observers
         ObserverMap obs = this.getObsMap();
         LinkedList<CallObserver> observersToRemove = new LinkedList<CallObserver>();
         Iterator<CallObserver> observerIterator = obs.keySet().iterator();
         while (observerIterator.hasNext()) {
             CallObserver co = observerIterator.next();
             ListenerStatus status = obs.get(co);
             if (status != null) {
                 if (status.remove(addr)) {
                     // the address was the last listener handle -- remove the
                     // observer, subtypes and notify
                 	
                 	// we hold it and remove it outside the loop to avoid a ConcurrentModificationException
                 	// we can't just use it.remove() since we have to do some extra logic
                 	observersToRemove.add(co);
                 }
             }
         }
 
         // Now remove any found items
         for(CallObserver co : observersToRemove) {
        	lists.remove(co);
         }
 
     }
 
     /**
      * Remove a CallListener from my managed set. To be registered as ahigher
      * level listener, it must be registered as a base CallListener. Creation
      * date: (2000-05-01 11:28:09)
      * 
      * @author: Richard Deadman
      */
     synchronized void remove(CallListener cl) {
         boolean removed = false;
 
         removed = (this.getCallListMap().remove(cl) != null);
 
         if (removed) {
             // attempt to remove from sub-type sets
             this.removeSubTypes(cl);
 
             // trigger the callListener that it is no longer being triggered
             // Observer code may thow exception
             try {
             	cl.callEventTransmissionEnded(new net.sourceforge.gjtapi.events.FreeCallObservationEndedEv(
                             this.getCall()));
             } catch (Exception ex) {
             	// No-op code -- should we log it?
             }
 
             // check if we should unprotect the call
             this.unProtect();
         }
     }
 
     /**
      * Remove a CallObserver from my managed set. To be registered as a higher
      * level observer, it must be registered as a base CallObserver. Creation
      * date: (2000-05-01 11:28:09)
      * 
      * @author: Richard Deadman
      */
     synchronized void remove(CallObserver co) {
         boolean removed = false;
 
         removed = (this.getObsMap().remove(co) != null);
 
         if (removed) {
             // attempt to remove from sub-type sets
             this.removeSubTypes(co);
 
             // trigger the callObserver that it is no longer being triggered
             FreeCallObservationEndedEv[] evs = new FreeCallObservationEndedEv[1];
             evs[0] = new FreeCallObservationEndedEv(this.getCall());
             // Observer code may thow exception
             try {
             	co.callChangedEvent(evs);
             } catch (Exception ex) {
             	// No-op -- we could log it?
             }
 
             // check if we should unprotect the call
             this.unProtect();
         }
     }
 
     /**
      * Remove a CallListener applier to my managed set and the CallListener
      * itself, if necessary. Creation date: (2000-05-01 11:28:09)
      * 
      * @author: Richard Deadman
      * @param term
      *                A Terminal visited by a call that held this listener as
      *                one of its CallListeners.
      */
     synchronized void remove(Terminal term) {
         ListenerMap lists = this.getCallListMap();
         LinkedList<CallListener> itemsToRemove = new LinkedList<CallListener>();
         Iterator<CallListener> it = lists.keySet().iterator();
         while (it.hasNext()) {
             CallListener cl = it.next();
             ListenerStatus status = lists.get(cl);
             if (status != null) {
                 if (status.remove(term)) {
                     // the address was the last listener handle -- remove the
                     // listener, subtypes and notify
                 	
                 	// we hold it and remove it outside the loop to avoid a ConcurrentModificationException
                 	// we can't just use it.remove() since we have to do some extra logic
                 	itemsToRemove.add(cl);
                 }
             }
         }
         
         // Now remove any found items
         for(CallListener cl : itemsToRemove) {
        	lists.remove(cl);
         }
 
         // now remove any registered observers
         ObserverMap obs = this.getObsMap();
         LinkedList<CallObserver> observersToRemove = new LinkedList<CallObserver>();
         Iterator<CallObserver> observerIterator= obs.keySet().iterator();
         while (observerIterator.hasNext()) {
             CallObserver co = observerIterator.next();
             ListenerStatus status = obs.get(co);
             if (status != null) {
                 if (status.remove(term)) {
                     // the address was the last listener handle -- remove the
                     // observer, subtypes and notify
                 	
                 	// we hold it and remove it outside the loop to avoid a ConcurrentModificationException
                 	// we can't just use it.remove() since we have to do some extra logic
                 	observersToRemove.add(co);
                 }
             }
         }
 
         // Now remove any found items
         for(CallObserver co : observersToRemove) {
        	lists.remove(co);
         }
 
     }
 
     /**
      * Remove all listeners from the call's listener manager. Creation date:
      * (2000-05-01 11:28:09)
      * 
      * @author: Richard Deadman
      */
     synchronized void removeAll() {
         Iterator<CallListener> it = this.getCallListenerIterator();
         while (it.hasNext()) {
             this.remove(it.next());
         }
 
         Iterator<CallObserver> observerIterator = this.getObsMap().keySet().iterator();
         while (observerIterator.hasNext()) {
             this.remove(observerIterator.next());
         }
     }
 
     /**
      * This removes a Call Listener to the Connection and TerminalConnection
      * sets if it has the right type
      */
     private void removeSubTypes(CallListener cl) {
         if (cl instanceof ConnectionListener) {
             this.getConnLists().remove(cl);
             if (cl instanceof TerminalConnectionListener) {
                 this.getTcLists().remove(cl);
             }
         }
     }
 
     /**
      * This removes a Call Observer to the CallControl sets if it has the right
      * type
      */
     private void removeSubTypes(CallObserver co) {
         if (co instanceof javax.telephony.callcontrol.CallControlCallObserver) {
             this.getCcObservers().remove(co);
         }
     }
 
     /**
      * Forward the event set onto all registered CallObservers. Creation date:
      * (2000-05-02 14:16:38)
      * 
      * @author: Richard Deadman
      * @param evs
      *                CallEv array of events.
      */
     void sendEvents(FreeCallEvent[] evs) {
         Iterator<CallObserver> it = this.getCallObservers().iterator();
         while (it.hasNext()) {
             it.next().callChangedEvent(evs);
         }
 
         // now see if we have any CallControlObservers
         Set<CallObserver> cco = this.getCcObservers();
         if (cco.size() > 0) {
             // Morph the CallEvs to CallCtlEvs
             CCCallEv[] ccevs = CCCallEv.toCcEvents(evs);
             if (ccevs.length > 0) {
                 // delegate off to CallCtlObservers
                 it = cco.iterator();
                 while (it.hasNext()) {
                     ((CallControlCallObserver) it.next())
                             .callChangedEvent(ccevs);
                 }
             }
         }
     }
 
     /**
      * singleCallMetaProgressEnded method comment.
      */
     public void singleCallMetaProgressEnded(javax.telephony.MetaEvent event) {
         Iterator<CallListener> it = this.getCallListenerIterator();
         while (it.hasNext()) {
             it.next().singleCallMetaProgressEnded(event);
         }
     }
 
     /**
      * singleCallMetaProgressStarted method comment.
      */
     public void singleCallMetaProgressStarted(javax.telephony.MetaEvent event) {
         Iterator<CallListener> it = this.getCallListenerIterator();
         while (it.hasNext()) {
             it.next().singleCallMetaProgressStarted(event);
         }
     }
 
     /**
      * singleCallMetaSnapshotEnded method comment.
      */
     public void singleCallMetaSnapshotEnded(javax.telephony.MetaEvent event) {
         Iterator<CallListener> it = this.getCallListenerIterator();
         while (it.hasNext()) {
             it.next().singleCallMetaSnapshotEnded(event);
         }
     }
 
     /**
      * singleCallMetaSnapshotStarted method comment.
      */
     public void singleCallMetaSnapshotStarted(javax.telephony.MetaEvent event) {
         Iterator<CallListener> it = this.getCallListenerIterator();
         while (it.hasNext()) {
             it.next().singleCallMetaSnapshotStarted(event);
         }
     }
 
     /**
      * terminalConnectionActive method comment.
      */
     public void terminalConnectionActive(
             javax.telephony.TerminalConnectionEvent event) {
         Iterator<CallListener> it = this.getTcListeners();
         while (it.hasNext()) {
             CallListener o = it.next();
             if (o instanceof TerminalConnectionListener)
                 ((TerminalConnectionListener) o)
                         .terminalConnectionActive(event);
         }
     }
 
     /**
      * terminalConnectionCreated method comment.
      */
     public void terminalConnectionCreated(
             javax.telephony.TerminalConnectionEvent event) {
         Iterator<CallListener> it = this.getTcListeners();
         while (it.hasNext()) {
             CallListener o = it.next();
             if (o instanceof TerminalConnectionListener)
                 ((TerminalConnectionListener) o)
                         .terminalConnectionCreated(event);
         }
     }
 
     /**
      * terminalConnectionDropped method comment.
      */
     public void terminalConnectionDropped(
             javax.telephony.TerminalConnectionEvent event) {
         Iterator<CallListener> it = this.getTcListeners();
         while (it.hasNext()) {
             CallListener o = it.next();
             if (o instanceof TerminalConnectionListener)
                 ((TerminalConnectionListener) o)
                         .terminalConnectionDropped(event);
         }
     }
 
     /**
      * terminalConnectionPassive method comment.
      */
     public void terminalConnectionPassive(
             javax.telephony.TerminalConnectionEvent event) {
         Iterator<CallListener> it = this.getTcListeners();
         while (it.hasNext()) {
             CallListener o = it.next();
             if (o instanceof TerminalConnectionListener)
                 ((TerminalConnectionListener) o)
                         .terminalConnectionPassive(event);
         }
     }
 
     /**
      * terminalConnectionRinging method comment.
      */
     public void terminalConnectionRinging(
             javax.telephony.TerminalConnectionEvent event) {
         Iterator<CallListener> it = this.getTcListeners();
         while (it.hasNext()) {
             CallListener o = it.next();
             if (o instanceof TerminalConnectionListener)
                 ((TerminalConnectionListener) o)
                         .terminalConnectionRinging(event);
         }
     }
 
     /**
      * terminalConnectionUnknown method comment.
      */
     public void terminalConnectionUnknown(
             javax.telephony.TerminalConnectionEvent event) {
         Iterator<CallListener> it = this.getTcListeners();
         while (it.hasNext()) {
             CallListener o = it.next();
             if (o instanceof TerminalConnectionListener)
                 ((TerminalConnectionListener) o)
                         .terminalConnectionUnknown(event);
         }
     }
 
     /**
      * If we just removed our last observer or listener, mark this call as being
      * available for garbage collection. Creation date: (2000-06-23 12:06:47)
      * 
      * @author: Richard Deadman
      */
     private void unProtect() {
         // check if we should unprotect the call
         if (this.isEmpty()) {
             FreeCall call = this.getCall();
             ((GenericProvider) call.getProvider()).getCallMgr().unProtect(call);
         }
     }
 }
