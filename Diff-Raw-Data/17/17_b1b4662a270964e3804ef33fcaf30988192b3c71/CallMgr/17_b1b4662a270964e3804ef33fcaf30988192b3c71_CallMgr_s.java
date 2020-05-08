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
 import javax.telephony.Call;
 import java.util.*;
 import net.sourceforge.gjtapi.util.*;
 /**
  * This is the Call Lookup manager that brokers call lookup requests for the Provider as well as
  * Addresses and Terminals.
  * <P>There are three type of smart accessors:
  * <OL>
  *  <LI><B>getCachedXXX</B> - this means that the item will be looked for in the call cache only.
  *  <LI><B>getFaultedXXX</B> - this means that the item will be looked for in the call cache and,
  * if it isn't found, requested (flushed in) from the raw TelephonyProvider.
  *  <LI><B>getLazyXXX</B> - this means that the item will be looked for in the call cache and, if
  * if it isn't found, a new empty one will be created.
  * </ol>
  * Creation date: (2000-06-15 14:03:03)
  * @author: Richard Deadman
  */
 class CallMgr {
 	private boolean dynamic = false;
 			// map of CallId -> Call that may have weak referents if throttling is supported
 	private Map callSet = null;
 			// hard holder for Calls to make sure observered Calls are not GC'd
 	private HashSet observedCalls = null;
 			// weak map of Calls to themselves.  This is a staging area for new Calls
 			// that have not yet been registered with a CallId (i.e. Provider.createCall())
 	private WeakHashMap idleCalls = new WeakHashMap();
 	private GenericProvider provider = null;
 	private TelephonyProvider raw = null;		// shortcut for provider->getRaw()
 /**
  * Constructor that determines which kind of map to keep and how to resolve Call requests.
  * Creation date: (2000-06-15 14:33:03)
  * @author: Richard Deadman
  * @param prov The GenericProvider that is needed to create calls.
  * @param isDynamic true if not all calls are reported from the TelephonyProvider.
  */
 CallMgr(GenericProvider prov, boolean isDynamic) {
 	super();
 
 	this.setProvider(prov);
 	this.setRaw(prov.getRaw());		// cache raw handle
 	this.setDynamic(isDynamic);
 	if (isDynamic) {
 		this.setCallSet(new WeakMap());
 		this.setObserved();
 	} else {
 		this.setCallSet(new HashMap());
 	}
 }
 /**
  * Find a call for the raw call handle.
  * Do not create a new call object, since we may just be looking to kill it anyway.
  * Creation date: (2000-02-14 10:18:50)
  * @author: Richard Deadman
  * @return The existing found Jtapi Call object or null.
  * @param id The raw wrapper call object
  */
 FreeCall getCachedCall(CallId id) {
 	return (FreeCall)this.getCallSet().get(id);
 }
 /**
  * Find a cached connection for the raw call handle and address.
  * Creation date: (2000-02-14 10:18:50)
  * @author: Richard Deadman
  * @return The found Jtapi Call object, or null if none is cached.
  * @param id The raw wrapper call object
  * @param address An address the call is connected to.
  */
 FreeConnection getCachedConnection(CallId id, String address) {
 	FreeCall call = this.getCachedCall(id);
 
 	if (call != null) {
 		return call.getCachedConnection(address);
 	}
 	return null;
 }
 /**
  * Find a cached terminal connection for the raw call handle, address name and terminal id.
  * Creation date: (2000-02-14 10:18:50)
  * @author: Richard Deadman
  * @return The found Jtapi TerminalConnection object, or null
  * @param id The raw wrapper call object
  * @param address An address the call is connected to.
  * @param terminal The name of the Terminal the connection is attached to.
  */
 FreeTerminalConnection getCachedTermConn(CallId id, String address, String terminal) {
 	FreeConnection connection = this.getCachedConnection(id, address);
 
 	if (connection != null)
 		// Create of insert the terminal connection
 		return connection.getCachedTermConn(terminal);
 	else
 		// indicate no cached TerminalConnection since the Connection doesn't exist
 		return null;
 }
 /**
  * Internal accessor for the CallId to Call map.
  * Creation date: (2000-06-15 14:29:15)
  * @author: Richard Deadman
  * @return A map of CallId to Call objects.
  */
 private Map getCallSet() {
 	return callSet;
 }
 /**
  * Return a Call for a given CallId.
  * If the Call is not in the map, then we assume that the call must be fetched from the
  * raw provider and reported on.
  * Creation date: (2000-06-15 14:51:37)
  * @author: Richard Deadman
  * @param id The logical low-level handle for the Call.
  * @return A tracked Call object, or null if the CallId is invalid
  */
 private FreeCall getFaultedCall(CallId id) {
 	FreeCall call = this.getCachedCall(id);
 
 	if (call == null && this.isDynamic()) {
 		// ask raw provider for CallData
 		CallData cd = this.getRaw().getCall(id);
 		if (cd != null) {
 			// create call and its objects
 			call = new FreeCall(cd);
 
 			// add the call to our set
 			this.register(call);
 		}
 	}
 
 	return call;
 }
 /**
  * Find or fault from the TelephonyProvider a connection for the raw call handle and address.
  * Creation date: (2000-02-14 10:18:50)
  * @author: Richard Deadman
  * @return The new or found Jtapi Call object
  * @param id The raw wrapper call object
  * @param address An address the call is connected to.
  */
 FreeConnection getFaultedConnection(CallId id, String address) {
 	// fist ensure the call is faulted in
 	this.getFaultedCall(id);
 
 	// now return the cached connection
 	return this.getCachedConnection(id, address);
 }
 /**
  * Find or fault in a terminal connection for the raw call handle, address name and terminal id.
  * We have evidence that this connection used to exist.
  * Creation date: (2000-02-14 10:18:50)
  * @author: Richard Deadman
  * @return The new or found Jtapi TerminalConnection object
  * @param id The raw wrapper call object
  * @param address An address the call is connected to.
  * @param terminal The name of the Terminal the connection is attached to.
  */
 FreeTerminalConnection getFaultedTermConn(CallId id, String address, String terminal) {
 	// first ensure the call is faulted in
 	this.getFaultedCall(id);
 
 	// now look for the cached TerminalConnection
 	return this.getCachedTermConn(id, address, terminal);
 }
 /**
  * Internal accessor for the set of Idle calls.
  * This is actually a WeakHashMap that gives us an effective WeakSet.
  * Creation date: (2000-06-26 0:21:53)
  * @author: Richard Deadman
  * @return A map of Idle calls to themselves.
  */
 private java.util.WeakHashMap getIdleCalls() {
 	return idleCalls;
 }
 /**
  * Find or create a call for the raw call handle.
  * This should be used to lazily create an empty call object for new call event.
  * Creation date: (2000-02-14 10:18:50)
  * @author: Richard Deadman
  * @return The new or found Jtapi Call object
  * @param id The raw wrapper call object
  */
 FreeCall getLazyCall(CallId id) {
 	FreeCall call = this.getCachedCall(id);
 
 	if (call == null) {
 		// No call was known
 		call = new FreeCall();
 		call.setProvider(this.getProvider());
 		call.setCallID(id);
 		this.register(call);
 	}
 		
 	return call;
 }
 /**
  * Find or create a connection for the raw call handle and address.
  * Creation date: (2000-02-14 10:18:50)
  * @author: Richard Deadman
  * @return The new or found Jtapi Call object
  * @param id The raw wrapper call object
  * @param address An address the call is connected to.
  */
 FreeConnection getLazyConnection(CallId id, String address) {
 	FreeConnection conn = this.getCachedConnection(id, address);
 
 	if (conn == null) {
 		FreeCall c = this.getLazyCall(id);
 		conn = new FreeConnection(c, address);
 	}
 	return conn;
 }
 /**
  * Find or create a terminal connection for the raw call handle, address name and terminal id.
  * Creation date: (2000-02-14 10:18:50)
  * @author: Richard Deadman
  * @return The new or found Jtapi TerminalConnection object
  * @param id The raw wrapper call object
  * @param address An address the call is connected to.
  * @param terminal The name of the Terminal the connection is attached to.
  */
 FreeTerminalConnection getLazyTermConn(CallId id, String address, String terminal) {
 	FreeTerminalConnection tc = this.getCachedTermConn(id, address, terminal);
 
 	if (tc == null) {	// create a new terminal connection
 		FreeConnection conn = this.getLazyConnection(id, address);
 		tc = conn.getLazyTermConn(terminal);
 	}
 
 	return tc;
 }
 /**
  * Insert the method's description here.
  * Creation date: (2000-06-19 13:05:38)
  * @author: Richard Deadman
  * @return net.sourceforge.gjtapi.GenericProvider
  */
 private GenericProvider getProvider() {
 	return provider;
 }
 /**
  * Internal accessor fot the low-level raw TelephonyProvider
  * Creation date: (2000-06-15 15:52:57)
  * @author: Richard Deadman
  * @return The low-level TelephonyProvider that controls the CallId lifecycles, takes call commands and generates call events.
  */
 private TelephonyProvider getRaw() {
 	return raw;
 }
 /**
  * Is this call map dynamic, in that calls are only reported once the TelephonyProvider has
  * requested them or asked for reporting of call that visit an Address or Terminal.
  * Creation date: (2000-06-15 14:25:04)
  * @author: Richard Deadman
  * @return true if calls are not automatically reported by the TelephonyProvider.
  */
 boolean isDynamic() {
 	return dynamic;
 }
 /**
  * Ask the raw provider for all call data associated with an Address.  For any Call not yet tracked, we
  * add it to the managed set.
  * Creation date: (2000-06-22 14:48:07)
  * @author: Richard Deadman
  * @param address net.sourceforge.gjtapi.FreeAddress
  */
 void loadCalls(FreeAddress address) {
 	CallData[] data = this.getRaw().getCallsOnAddress(address.getName());
 
 	int size = data.length;
 	for (int i = 0; i < size; i++) {
 		// check if call already being tracked
 		if (this.getCachedCall(data[i].id) == null) {
 			this.register(new FreeCall(data[i]));
 		}
 	}
 }
 /**
  * Ask the raw provider for all call data associated with a Terminal.  For any Call not yet tracked, we
  * add it to the managed set.
  * Creation date: (2000-06-22 14:48:07)
  * @author: Richard Deadman
  * @param address net.sourceforge.gjtapi.FreeAddress
  */
 void loadCalls(FreeTerminal terminal) {
 	CallData[] data = this.getRaw().getCallsOnTerminal(terminal.getName());
 
 	int size = data.length;
 	for (int i = 0; i < size; i++) {
 		// check if call already being tracked
 		if (this.getCachedCall(data[i].id) == null) {
 			this.register(new FreeCall(data[i]));
 		}
 	}
 }
 /**
  * Pre-register the given call with the Idle call set
  * Creation date: (2000-06-19 12:48:59)
  * @author: Richard Deadman
  * @param call The Idle call to register until it is activated.
  */
 void preRegister(FreeCall call) {
 	this.getIdleCalls().put(call, null);
 }
 /**
  * This is called by a Call that has been observed or Listened to so that it will be protected from
  * any garbage collection if dynamic tracking is used.
  * Creation date: (2000-06-23 10:59:02)
  * @author: Richard Deadman
  * @param call The call to protect from potential cache clearing.
  */
 void protect(FreeCall call) {
 	// check that we are using a WeakMap
 	HashSet observed = this.observedCalls;
 	if (observed != null) {
 		observed.add(call);
 	}
 }
 /**
  * Register the given call with the Call set.
  * Creation date: (2000-06-19 12:48:59)
  * @author: Richard Deadman
  * @param call The transient call we should track
  */
 void register(FreeCall call) {
 	// remove from idle set if it is there
 	this.getIdleCalls().remove(call);
 
 	// add to CallId tracked calls.
 	this.getCallSet().put(call.getCallID(), call);
 }
 /**
  * Remove a call from my list.
  * This is used to clean up Invalid calls.
  * Creation date: (2000-05-05 23:54:45)
  * @author: Richard Deadman
  * @param call An Invalid Call to remove
  */
 void removeCall(FreeCall call) {
 	if (call.getState() == Call.INVALID)
 		this.getCallSet().remove(call.getCallID());
 }
 /**
  * Internal setter for the CallId to call map.
  * This may reference actual Calls or References to Calls.
  * Creation date: (2000-06-15 14:29:15)
  * @author: Richard Deadman
  * @param newCallSet The new Map to store CallId to Call mappings in.
  */
 private void setCallSet(java.util.Map newCallSet) {
 	callSet = newCallSet;
 }
 /**
  * Set whether this call map dynamic, in that calls are only reported once the TelephonyProvider has
  * requested them or asked for reporting of call that visit an Address or Terminal.
  * Creation date: (2000-06-15 14:25:04)
  * @author: Richard Deadman
  * @param newDynamic true if calls are not automatically reported by the TelephonyProvider
  */
 private void setDynamic(boolean newDynamic) {
 	dynamic = newDynamic;
 }
 /**
  * Lazy creator that creates the holder for calls to protect them from garbage collection.
  * The existance of this set turns on "holding".
  * Creation date: (2000-06-23 11:44:43)
  * @author: Richard Deadman
  */
 private synchronized void setObserved() {
 	if (this.observedCalls == null)
 		this.observedCalls = new HashSet();
 }
 /**
  * Insert the method's description here.
  * Creation date: (2000-06-19 13:05:38)
  * @author: Richard Deadman
  * @param newProvider net.sourceforge.gjtapi.GenericProvider
  */
 private void setProvider(GenericProvider newProvider) {
 	provider = newProvider;
 }
 /**
  * Set the raw TelephonyProvider that I use to track the lifecycle of calls.
  * Creation date: (2000-06-15 15:52:57)
  * @author: Richard Deadman
  * @param newRaw The lower-level telephony provider that owns the call ids.
  */
 private void setRaw(TelephonyProvider newRaw) {
 	raw = newRaw;
 }
 /**
  * Return a collection of the Call values.
  * Creation date: (2000-06-15 14:51:37)
  * @author: Richard Deadman
  * @return A Collection of currently known Call objects.
  */
 FreeCall[] toArray() {
 	Collection vs = this.getCallSet().values();	// the active calls
 	vs.addAll(this.getIdleCalls().keySet());	// the idle calls
 	return (FreeCall[])vs.toArray(new FreeCall[vs.size()]);
 }
 /**
  * Describe myself
  * @return a string representation of the receiver
  */
 public String toString() {
 	StringBuffer sb = new StringBuffer("A call manager with ");
 	if (this.isDynamic())
 		sb.append("dynamcally queried");
 	else
 		sb.append("event reported");
	return sb.append(" calls: ").append(this.getCallSet().toString()).toString();
 }
 /**
  * This is called by a Call that is no longer observed or Listened to so that it will no longer be
  * protected from any garbage collection if dynamic tracking is used.
  * Creation date: (2000-06-23 10:59:02)
  * @author: Richard Deadman
  * @param call The call the free up for potential cache clearing.
  */
 void unProtect(FreeCall call) {
 	// check that we are using a WeakMap
 	HashSet observed = this.observedCalls;
 	if (observed != null) {
 		observed.remove(call);
 	}
 }
 }
