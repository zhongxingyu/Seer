 package net.sourceforge.gjtapi;
 
 /*
 	Copyright (c) 1999,2002 Westhawk Ltd (www.westhawk.co.uk) 
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
 
 
 import javax.telephony.capabilities.CallCapabilities;
 import javax.telephony.*;
 import javax.telephony.events.*;
 import javax.telephony.callcontrol.*;
 import javax.telephony.privatedata.PrivateData;
 import net.sourceforge.gjtapi.events.*;
 import net.sourceforge.gjtapi.capabilities.GenCallCapabilities;
 import java.util.*;
 
 public class FreeCall implements CallControlCall, PrivateData {
 	private int state = Call.IDLE;
 	private GenericProvider provider = null;
 	private HashMap connections = new HashMap();	// Address name -> connections
 	private ListenerManager listMgr = null;
 	private CallId callID;
 	private boolean stoppedReporting = false;		// ensure we do it only once.
 
 	private FreeTerminalConnection confController = null;
 	private FreeTerminalConnection transController = null;
 /**
  * Protected constructor for a Call.
  * Initially the call starts in the Idle state.
  * @author Richard Deadman
  **/
 protected FreeCall() {
 	this.listMgr = new ListenerManager(this);
 }    
 /**
  * Internal CallCreator that uses a CallData object to flesh out the call.
  * Initially the call starts in the Idle state.
  * @author Richard Deadman
  **/
 FreeCall(CallData cd) {
 	this();
 	
 	this.setState(cd.callState);
 	int size = cd.connections.length;
 	for (int i = 0; i < size; i++) {
 		new FreeConnection(this, cd.connections[i], this.getGenProvider().getDomainMgr());
 	}
 }    
 /**
  * Add the Listener to the set of CallListeners.
  * This should only work if the CallListener isn't already registered for
  * Call, Connection or TerminalConnection events.
  **/
  public void addCallListener(CallListener l) {
 	 this.getListenerMgr().add(l);
   }                  
 /**
  * Add the Listener to the set of CallListeners for an Address.
  * Later when the Call leaves the Address, any CallListeners dependent only on this Address may be removed.
  **/
  void addCallListener(CallListener l, Address addr) {
 	 this.getListenerMgr().add(l, addr);
   }                    
 /**
  * Add the Listener to the set of CallListeners for a Terminal.
  * Later when the Call leaves the Terminal, any CallListeners dependent only on this Terminal may be removed.
  **/
  void addCallListener(CallListener l, Terminal term) {
 	 this.getListenerMgr().add(l, term);
   }                      
 /**
  * Add the Observer to the set of CallObserver for an Address.
  * Later when the Call leaves the Address, any CallObservers dependent only on this Address may be removed.
  **/
  void addCallObserver(CallObserver observer, Address addr) {
 	 this.getListenerMgr().add(observer, addr);
   }                      
 /**
  * Add the Observer to the set of CallObserver for a Terminal.
  * Later when the Call leaves the Terminal, any CallObservers dependent only on this Terminal may be removed.
  **/
  void addCallObserver(CallObserver observer, Terminal term) {
 	 this.getListenerMgr().add(observer, term);
   }                        
   /**
    * Store the Connection by its Address name
    **/
 void addConnection(FreeConnection c) {
 	connections.put(c.getAddressName(), c);
 }      
   public void addObserver(CallObserver observer) throws javax.telephony.ResourceUnavailableException, javax.telephony.MethodNotSupportedException {
 	this.getListenerMgr().add(observer);
   }          
 /**
    * Adds an additional party to an existing Call. This is sometimes called a
    * "single-step conference" because a party is conferenced into a Call
    * directly. The telephone address string provided as the argument must be
    * complete and valid.
    *
    * <H5>States of the Existing Connections</H5>
    *
    * The Call must  have at least two Connections in the
    * <CODE>CallControlConnection.ESTABLISHED</CODE> state. An additional
    * restriction requires that at most one other Connection may be in either
    * the <CODE>CallControlConnection.QUEUED</CODE>,
    * <CODE>CallControlConnection.OFFERED</CODE>, or
    * <CODE>CallControlConnection.ALERTING</CODE> state.
    * <p>
    * Some telephony platforms impose restrictions on the number of Connections
    * in a particular state. For instance, it is common to restrict the number
    * of "alerting" Connections to at most one. As a result, this method
    * requires that at most one other Connections is in the "queued",
    * "offering", or "alerting" state. (Note that the first two states
    * correspond to the core Connection "in progress" state). Although some
    * systems may not enforce this requirement, for consistency, JTAPI specifies
    * implementations must uphold the conservative requirement.
    *
    * <H5>The New Connection</H5>
    *
    * This method creates and returns a new Connection representing the new
    * party. This Connection must at least be in the
    * <CODE>CallControlConnection.IDLE</CODE> state. Its state may have
    * progressed beyond "idle" before this method returns, and should be
    * reflected by an event. This new Connection will progress as any normal
    * destination Connection on a Call. Typical scenarios for this Connection
    * are described by the <CODE>Call.connect()</CODE> method.
    * <p>
    * <B>Pre-conditions:</B>
    * <OL>
    * <LI>(this.getProvider()).getState() == Provider.IN_SERVICE
    * <LI>this.getState() == Call.ACTIVE
    * <LI>Let c[] = call.getConnections() where c.length >= 2
    * <LI>c[i].getCallControlState() == CallControlConnection.ESTABLISHED for
    * at least two i
    * <LI>c[j].getCallControlState() == CallControlConnection.QUEUED,
    * CallControlConnection.OFFERED, or CallControlConnection.ALERTING for at
    * most one c[j]
    * </OL>
    * <B>Post-conditions:</B>
    * <OL>
    * <LI>Let connection be the Connection created and returned
    * <LI>(this.getProvider()).getState() == Provider.IN_SERVICE
    * <LI>this.getState() == Call.ACTIVE
    * <LI>connection.getCallControlState() at least CallControlConnection.IDLE
    * <LI>ConnCreatedEv is delivered for connection
    * </OL>
    * @see javax.telephony.events.ConnCreatedEv
    * @param newParty The telephone address of the party to be added.
    * @return The new Connection associated with the added party.
    * @exception InvalidStateException Either the Provider is not "in service",
    * the Call is not "active" or the proper conditions on the Connections does
    * not exist.
    * as designated by the pre-conditions for this method.
    * @exception InvalidPartyException The destination address string is not
    * valid and/or complete.
    * @exception MethodNotSupportedException This method is not supported by
    * the implementation.
    * @exception PrivilegeViolationException The application does not have
    * the proper authority to invoke this method.
    * @exception ResourceUnavailableException An internal resource necessary
    * for the successful invocation of this method is not available.
  */
 public javax.telephony.Connection addParty(java.lang.String newParty) throws javax.telephony.InvalidStateException, javax.telephony.MethodNotSupportedException, javax.telephony.PrivilegeViolationException, javax.telephony.ResourceUnavailableException, javax.telephony.InvalidPartyException {
 	// test for transfer TerminalController
 	TerminalConnection conf = this.getConferenceController();
 	boolean controllerSet = true;
 	if (conf == null) {	// look for the first available TC
 		controllerSet = false;
 		Connection[] cs = this.getConnections();
 		for (int i = 0; i < cs.length; i++) {
 			TerminalConnection[] tcs = cs[i].getTerminalConnections();
 			if (tcs != null && tcs.length > 0) {
 				conf = tcs[0];
 				break;
 			}
 		}
 	}
 	if (conf == null)
 		throw new ResourceUnavailableException(ResourceUnavailableException.ORIGINATOR_UNAVAILABLE,
 					"Conference TerminalConnection not set and cannot be found");
 
 	// hold current call
 	if (conf instanceof CallControlTerminalConnection) {
 		((CallControlTerminalConnection)conf).hold();
 	}
 
 	try {	
 		// Create a consultation call
 		CallControlCall consult = (CallControlCall)this.getProvider().createCall();
 		consult.consult(conf, newParty);
 
 		// conference the call
 		this.setConferenceController(conf);
 		this.conference(consult);
 
 		// reset conference controller to null if not previously set
 		if (!controllerSet)
 			this.setConferenceController(null);
 	} catch (InvalidArgumentException iae) {
 		throw new ResourceUnavailableException(ResourceUnavailableException.UNKNOWN,
 				"Error conferencing the call");
 	}
 
 	// Find the remote connection
 	Connection conn[] = this.getConnections();
 	Connection rem = null;
 	for (int i = 0; i < conn.length; i++) {
 		if (conn[i].getAddress().getName().equals(newParty)) {
 			rem = conn[i];
 			break;
 		}
 	}
 	return rem;
 
 }
 /**
  * conference method comment.
  */
 public void conference(javax.telephony.Call otherCall) throws javax.telephony.InvalidStateException, javax.telephony.MethodNotSupportedException, javax.telephony.PrivilegeViolationException, javax.telephony.ResourceUnavailableException, javax.telephony.InvalidArgumentException {
 	TerminalConnection tc = this.getConferenceController();
 	if (tc == null) {
 		if ((tc = this.findCommonTC(otherCall)) == null) {
 			throw new InvalidArgumentException("No Conference controller set of available");
 		}
 	}
 	((GenericProvider)this.getProvider()).getRaw().join(this.getCallID(),
 		((FreeCall)otherCall).getCallID(),
 		tc.getConnection().getAddress().getName(),
 		tc.getTerminal().getName());
 }
 /**
  * connect an idle call from a terminal/address pair to anothter address
  *
  * @param origterm The outgoing terminal
  * @param origaddr The outgoing address on the terminal
  * @param dialedDigits A string representation of the remote address.
  * @return The new connections of the call.
  *
  * @author: Richard Deadman
  **/
 public Connection[] connect(Terminal origterm, Address origaddr, String dialedDigits) throws javax.telephony.ResourceUnavailableException, javax.telephony.PrivilegeViolationException, javax.telephony.InvalidPartyException, javax.telephony.InvalidArgumentException, javax.telephony.InvalidStateException, javax.telephony.MethodNotSupportedException {
 	GenericProvider gp = (GenericProvider) this.getProvider();
 
 	// check if the terminal has the address
 	Address[] ta = origterm.getAddresses();
 	int size = ta.length;
 	boolean found = false;
 	for (int i = 0; i < size; i++) {
 		if (ta[i].equals(origaddr)) {
 			found = true;
 			break;
 		}
 	}
 	if (!found)
 		throw new InvalidArgumentException();
 
 	// check the states
 	if (this.getState() != this.IDLE)
 		throw new InvalidStateException(this, InvalidStateException.CALL_OBJECT, this.getState());
 	if (gp.getState() != gp.IN_SERVICE)
 		throw new InvalidStateException(gp, InvalidStateException.PROVIDER_OBJECT, gp.getState());
 
 	// create a call id
 	this.setCallID(gp.getRaw().reserveCallId(origterm.getName()));
 
 	// register the call with its new id.
 	this.getGenProvider().getCallMgr().register(this);
 
 	// create two connections - they add themselves back to me
 	Connection[] connSet = new Connection[2];
 	connSet[0] = new FreeConnection(this, (FreeAddress) origaddr);
 	connSet[1] = new FreeConnection(this, dialedDigits);
 
 	// tell the service provider to hook up the connections
 	try {
 		provider.getRaw().createCall(this.getCallID(), origaddr.getName(), origterm.getName(), dialedDigits);
 	} catch (RawStateException rse) {
 		throw rse.morph((GenericProvider)this.getProvider());
 	}
 
 	// change the call state - do we need this?
 	//this.setState(Call.ACTIVE, Event.CAUSE_NEW_CALL);
 
 	//return getConnections();	// this method does not guarantee that they will be in the right order
 	return connSet;
 }
 /**
  * This creates a connection on a consultation call in an initiated state.  To use this, the
  * Connection must implement CallControlConnection.addToAddress(String newDigits).  We don't currently
  * implement this.
  */
 public javax.telephony.Connection consult(TerminalConnection tc) throws InvalidStateException, MethodNotSupportedException, PrivilegeViolationException, ResourceUnavailableException, InvalidArgumentException {
 	throw new MethodNotSupportedException();
 }
 /**
  * Create a consultation call on the same terminal as an existing call.  Once this is done, the existing
  * call moves into the held state and the consulation call becomes active.  The terminal connection is set as the transfer
  * and conference terminal connection for the old call, and the corresponding terminal connection for the
  * consultation call becomes its transfer and conference terminal connection.
  */
 public Connection[] consult(TerminalConnection tc, String dialedDigits) throws MethodNotSupportedException, ResourceUnavailableException, InvalidArgumentException, InvalidPartyException, InvalidStateException, PrivilegeViolationException {
 	// find the other call and hold it
 	CallControlCall mainCall = (CallControlCall)tc.getConnection().getCall();
 	((CallControlTerminalConnection)tc).hold();
 
 	// dial this call
 	Terminal origTerm = tc.getTerminal();
 	Connection newConn[] = this.connect(origTerm, tc.getConnection().getAddress(), dialedDigits);
 
 	// find which TC is to the same terminal
 	TerminalConnection newTC = null;
 	for (int i = 0; i < newConn.length; i++) {
 		TerminalConnection termConn[] = newConn[i].getTerminalConnections();
 		if (termConn != null) {
 			for (int j = 0; j < termConn.length; j++) {
 				if (termConn[j].getTerminal().equals(origTerm)) {
 					newTC = termConn[j];
 					break;
 				}
 			}
 		}
 	}
 
 	// set the controllers -- let it throw a NullPointerException since newTC == null is a logical error
 	this.setTransferController(newTC);
 	this.setConferenceController(newTC);
 
 	mainCall.setTransferController(tc);
 	mainCall.setConferenceController(tc);
 
 	return newConn;
 }
 /**
  * drop aa call by dropping all its connections.
  */
 public void drop() throws InvalidStateException, MethodNotSupportedException, PrivilegeViolationException, ResourceUnavailableException {
 	if (this.getState() != ACTIVE)
 		throw new InvalidStateException(this,
 					InvalidStateException.CALL_OBJECT,
 					this.getState(),
 					"Drop from non-active call");
 	
 	Connection[] cons = this.getConnections();
 
 	for (int i = 0; i < cons.length; i++) {
 		try {
 			cons[i].disconnect();
 		} catch (InvalidStateException ise) {
 			// eat it -- the call is now dropped
 			break;
 		}
 	}
 }
 /**
  * Clean up, in case we have not unregistered yet.
  * Creation date: (2000-06-22 13:38:56)
  * @author: Richard Deadman
  */
 public void finalize() {
 		// tell the raw TelephonyProvider to stop reporting my calls
 	this.stopReporting();
 		// Remove me from the Call cache
 	((GenericProvider)this.getProvider()).getCallMgr().removeCall(this);
 }
 /**
  * Find a common TerminalConnection between two calls, if one exists.  Otherwise return null.
  */
 private FreeTerminalConnection findCommonTC(Call otherCall) {
 	Connection[] cs = this.getConnections();
 	for (int i = 0; i < cs.length; i++) {
 		TerminalConnection[] tcs = cs[i].getTerminalConnections();
 		if (tcs != null) {
 			for (int j = 0; j < tcs.length; j++) {
 				Terminal t = tcs[j].getTerminal();
 				// now check the other call for the same Terminal
 				Connection[] cs2 = otherCall.getConnections();
 				for (int k = 0; k < cs2.length; k++) {
 					TerminalConnection[] tcs2 = cs2[k].getTerminalConnections();
 					for (int l = 0; l < tcs2.length; l++) {
 						if (tcs2[l].getTerminal().equals(t)) {
 							return (FreeTerminalConnection) tcs[j];
 						}
 					}
 				}
 			}
 		}
 	}
 	return null;
 }
   /**
    * Get a Connection that connects this call to a given Address.
    * Only return a cached value -- do not go to the TelephonyProvider to fault it in.
    * @param addrName The name of the Connection's Address
    * @return The currently cached Connection, or null.
    **/
 public FreeConnection getCachedConnection(String addrName) {
 	return (FreeConnection)this.connections.get(addrName);
 }          
   public CallCapabilities getCallCapabilities(Terminal term, Address addr) throws javax.telephony.InvalidArgumentException, javax.telephony.PlatformException {
 	return this.getCapabilities(term, addr);
   }          
 /**
  * getCalledAddress method comment.
  */
 public javax.telephony.Address getCalledAddress() {
 	return null;
 }
 	public CallId getCallID() {
 		return callID;
 	}
 /**
  * getCallingAddress method comment.
  */
 public javax.telephony.Address getCallingAddress() {
 	return null;
 }
 /**
  * getCallingTerminal method comment.
  */
 public javax.telephony.Terminal getCallingTerminal() {
 	return null;
 }
 /**
  * Return a copy of the set of currently registered CallListeners.
  * @return An array of the registered CallListeners for this call.
  **/
 public CallListener[] getCallListeners() {
 	Set cls = this.getListenerMgr().getCallListeners();
 	CallListener[] ret = new CallListener[cls.size()];
 	return (CallListener[])cls.toArray(ret);
 }
  /**
   * Returns the dynamic capabilities for the instance of the Call object. Dynamic capabilities tell the application which actions are
 	 possible at the time this method is invoked based upon the implementations knowledge of its ability to successfully perform the
 	 action. This determination may be based upon argument passed to this method, the current state of the call model, or some
 	 implementation-specific knowledge. These indications do not guarantee that a particular method can be successfully invoked,
 	 however. 
 
 	 <P>The dynamic call capabilities are based upon a Terminal/Address pair as well as the instance of the Call object. These parameters
 	 are used to determine whether certain call actions are possible at the present. For example, the
 	 CallCapabilities.canConnect() method will indicate whether a telephone call can be placed using the Terminal/Address pair
 	 as the originating endpoint. 
   **/
 public CallCapabilities getCapabilities(Terminal terminal, Address address) throws javax.telephony.InvalidArgumentException {
 	// must merge in terminal and address capabilities
 	return ((GenCallCapabilities)this.getProvider().getCallCapabilities()).getDynamic(this, terminal, address);
   }            
 /**
  * getConferenceController method comment.
  */
 public javax.telephony.TerminalConnection getConferenceController() {
 	return confController;
 }
 /**
  * getConferenceEnable method comment.
  */
 public boolean getConferenceEnable() {
 	return true;
 }
   public Connection[] getConnections() {
 	Connection[] ret = null;
 	synchronized (connections) {
 		if ( ! connections.isEmpty() ) {
                     ret = new Connection[connections.size()];
                     connections.values().toArray(ret);
                 }
 	}
 	return ret;
   }              
 /**
  * Internal accessor for my Provider.
  * @author Richard Deadman
  * @return The GenericProvider managing this call.
  **/
 private GenericProvider getGenProvider() {
 	return provider;
 }
 /**
  * getLastRedirectedAddress method comment.
  */
 public javax.telephony.Address getLastRedirectedAddress() {
 	return null;
 }
 /**
  * Return the proxy to the sets of Listeners for a call.
  * @return A TerminalConnection listener proxy.
  **/
 public TerminalConnectionListener getListener() {
 	return this.getListenerMgr();
 }
 /**
  * Internal accessor for the Listener manager.
  * Creation date: (2000-05-01 12:56:28)
  * @author: Richard Deadman
  * @return A collection of CallListeners and their association information.
  */
 private ListenerManager getListenerMgr() {
 	return listMgr;
 }
   public CallObserver[] getObservers() {
 	return (CallObserver[]) this.getListenerMgr().getCallObservers().toArray(new CallObserver[0]);
   }          
 /**
  * Get any PrivateData associated with my low-level object.
  */
 public Object getPrivateData() {
 	return ((GenericProvider)this.getProvider()).getRaw().getPrivateData(this.getCallID(), null, null);
 }
   public javax.telephony.Provider getProvider() {
 	return provider;
   }        
   public int getState() {
 	return state;
   }        
 /**
  * getTransferController method comment.
  */
 public TerminalConnection getTransferController() {
 	return transController;
 }
 /**
  * We may want to hook this into the capabilities?
  */
 public boolean getTransferEnable() {
 	return true;
 }
 /**
  * offHook method comment.
  */
 public javax.telephony.Connection offHook(Address origaddress, Terminal origterminal) throws InvalidStateException, MethodNotSupportedException, PrivilegeViolationException, ResourceUnavailableException {
 	throw new MethodNotSupportedException();
 }
 /**
  * Remove an Address's CallListeners and CallObservers if the Address was the only registrar for the listeners.
  **/
  public void removeCallListAndObs(Address addr) {
 	this.getListenerMgr().remove(addr);
   }              
 /**
  * Remove a Terminal's CallListeners and CallObservers if the Terminal was the only registrar for the listeners.
  **/
  public void removeCallListAndObs(Terminal term) {
 	this.getListenerMgr().remove(term);
   }                
   public void removeCallListener(CallListener l) {
 	this.getListenerMgr().remove(l);
   }          
   void removeConnection(Connection c) {
		connections.remove(c.getAddress().getName());
   }          
   public void removeObserver(CallObserver observer) {
 	this.getListenerMgr().remove(observer);
   }          
   /* send an ev to the now removed observer */
   void sendObservationEndedEv(CallObserver observer){
 	CallObservationEndedEv[] ev = new CallObservationEndedEv[1];
 	ev[0] = new FreeCallObservationEndedEv(this);
 	observer.callChangedEvent(ev);
   }              
 /**
  * Send PrivateData to my low-level object for processing.
  */
 public java.lang.Object sendPrivateData(java.lang.Object data) {
 	return ((GenericProvider)this.getProvider()).getRaw().sendPrivateData(this.getCallID(), null, null, data);
 }
   /**
    * Send a sanpshot describing the current state of the call.
    * If we had a true state machine, we could ask each state to generate the appropriate event.
    */
   protected void sendSnapShot(CallListener l){
 
 	// Send the call state
 	switch (this.getState()) {
 		case Call.ACTIVE: {
 			l.callActive(new FreeCallActiveEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, true, this));
 			break;
 		}
 		case Call.INVALID: {
 			l.callInvalid(new FreeCallInvalidEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, true, this));
 			break;
 		}
 	}
 
 	// now for each connection
   if (l instanceof ConnectionListener) {
 	ConnectionListener cl = (ConnectionListener)l;
 	Connection[] conns = this.getConnections();
 	if (conns != null)
 	  for (int i = conns.length-1; i > -1; i--) {
 		FreeConnection c = (FreeConnection)conns[i];
 		switch (c.getState()) {
 			case Connection.IDLE : {
 				cl.connectionCreated(new FreeConnCreatedEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, true, c));
 				break;
 			}
 			case Connection.INPROGRESS : {
 				cl.connectionInProgress(new FreeConnInProgressEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, true, c));
 				break;
 			}
 			case Connection.ALERTING : {
 				cl.connectionAlerting(new FreeConnAlertingEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, true, c));
 				break;
 			}
 			case Connection.CONNECTED : {
 				cl.connectionConnected(new FreeConnConnectedEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, true, c));
 				break;
 			}
 			case Connection.DISCONNECTED : {
 				cl.connectionDisconnected(new FreeConnDisconnectedEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, true, c));
 				break;
 			}
 			case Connection.UNKNOWN : {
 				cl.connectionUnknown(new FreeConnUnknownEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, true, c));
 				break;
 			}
 			case Connection.FAILED : {
 				cl.connectionFailed(new FreeConnFailedEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, true, c));
 				break;
 			}
 		}
 
 	  if (cl instanceof TerminalConnectionListener) {
 		TerminalConnectionListener tcl = (TerminalConnectionListener)cl;
 		// now for each terminal connection attached to the connection
 		TerminalConnection[] tcs = c.getTerminalConnections();
 		if (tcs != null)
 		  for (int j = tcs.length-1; j > -1; j--) {
 			FreeTerminalConnection tc = (FreeTerminalConnection)tcs[j];
 			switch (tc.getState()) {
 				case TerminalConnection.IDLE : {
 					tcl.terminalConnectionCreated(new FreeTermConnCreatedEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, true, tc));
 					break;
 				}
 				case TerminalConnection.RINGING : {
 					tcl.terminalConnectionRinging(new FreeTermConnRingingEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, true, tc));
 					break;
 				}
 				case TerminalConnection.ACTIVE : {
 					boolean talking = (tc.getCallControlState() == CallControlTerminalConnection.TALKING);
 					tcl.terminalConnectionActive(new FreeTermConnActiveEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, true, tc, talking));
 					break;
 				}
 				case TerminalConnection.DROPPED : {
 					tcl.terminalConnectionDropped(new FreeTermConnDroppedEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, true, tc));
 					break;
 				}
 				case TerminalConnection.UNKNOWN : {
 					tcl.terminalConnectionUnknown(new FreeTermConnUnknownEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, true, tc));
 					break;
 				}
 			}
 		  }
 	  }
 	}
   }
   }                  
   /**
    * Send a sanpshot describing the current state of the call.
    * If we had a true state machine, we could ask each state to generate the appropriate event.
    */
   protected void sendSnapShot(CallObserver o){
 	Set evs = new HashSet();
 
 	// note the call state
 	switch (this.getState()) {
 		case Call.ACTIVE: {
 			evs.add(new FreeCallActiveEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, true, this));
 			break;
 		}
 		case Call.INVALID: {
 			evs.add(new FreeCallInvalidEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, true, this));
 			break;
 		}
 	}
 
 	// now for each connection
 	Connection[] conns = this.getConnections();
 	if (conns != null)
 	  for (int i = conns.length-1; i > -1; i--) {
 		FreeConnection c = (FreeConnection)conns[i];
 		switch (c.getState()) {
 			case Connection.IDLE : {
 				evs.add(new FreeConnCreatedEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, false, c));
 				break;
 			}
 			case Connection.INPROGRESS : {
 				evs.add(new FreeConnInProgressEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, false, c));
 				break;
 			}
 			case Connection.ALERTING : {
 				evs.add(new FreeConnAlertingEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, false, c));
 				break;
 			}
 			case Connection.CONNECTED : {
 				evs.add(new FreeConnConnectedEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, false, c));
 				break;
 			}
 			case Connection.DISCONNECTED : {
 				evs.add(new FreeConnDisconnectedEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, false, c));
 				break;
 			}
 			case Connection.UNKNOWN : {
 				evs.add(new FreeConnUnknownEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, false, c));
 				break;
 			}
 			case Connection.FAILED : {
 				evs.add(new FreeConnFailedEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, false, c));
 				break;
 			}
 		}
 
 		// now for each terminal connection attached to the connection
 		TerminalConnection[] tcs = c.getTerminalConnections();
 		if (tcs != null) {
 		  boolean active = false;
 		  boolean talking = false;
 		  for (int j = tcs.length-1; j > -1; j--) {
 			active = false;
 			FreeTerminalConnection tc = (FreeTerminalConnection)tcs[j];
 			switch (tc.getCallControlState()) {
 				case CallControlTerminalConnection.IDLE : {
 					evs.add(new FreeTermConnCreatedEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, false, tc));
 					break;
 				}
 				case CallControlTerminalConnection.RINGING : {
 					evs.add(new FreeTermConnRingingEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, false, tc));
 					break;
 				}
 				case CallControlTerminalConnection.TALKING : {
 					evs.add(new FreeTermConnTalkingEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, false, tc));
 					active = true;
 					talking = true;
 					break;
 				}
 				case CallControlTerminalConnection.HELD : {
 					evs.add(new FreeTermConnHeldEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, false, tc));
 					active = true;
 					talking = false;
 					break;
 				}
 				case CallControlTerminalConnection.DROPPED : {
 					evs.add(new FreeTermConnDroppedEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, false, tc));
 					break;
 				}
 				case CallControlTerminalConnection.UNKNOWN : {
 					evs.add(new FreeTermConnUnknownEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, false, tc));
 					break;
 				}
 			}
 			if (active)
 				evs.add(new FreeTermConnActiveEv(Event.CAUSE_SNAPSHOT, Ev.META_SNAPSHOT, false, tc, talking));
 		  }
 		}
 	}
 
 	// now send the event set
 	o.callChangedEvent((CallEv[])evs.toArray(new CallEv[evs.size()]));
   }                      
 /**
  * Forward the event off to all observers
  * Creation date: (2000-02-14 15:06:59)
  * @author: Richard Deadman
  * @param ev The event to forward
  */
 public void sendToObservers(FreeCallEvent ev) {
 	// send to observers
 	FreeCallEvent[] evs = {ev};
 	this.getListenerMgr().sendEvents(evs);
 }
 	public void setCallID(CallId newCallID) {
 		callID = newCallID;
 	}
 /**
  * setConferenceController method comment.
  */
 public void setConferenceController(TerminalConnection tc) throws InvalidStateException, MethodNotSupportedException, ResourceUnavailableException, InvalidArgumentException {
 	if (tc instanceof FreeTerminalConnection)
 		confController = (FreeTerminalConnection)tc;
 	else
 		throw new InvalidArgumentException();
 }
 /**
  * setConferenceEnable method comment.
  */
 public void setConferenceEnable(boolean enable) throws javax.telephony.InvalidStateException, javax.telephony.MethodNotSupportedException, javax.telephony.PrivilegeViolationException, javax.telephony.InvalidArgumentException {}
 /**
  * Set PrivateData to be used in the next low-level command.
  */
 public void setPrivateData(java.lang.Object data) {
 	((GenericProvider)this.getProvider()).getRaw().setPrivateData(this.getCallID(), null, null, data);
 }
   void setProvider(javax.telephony.Provider newProvider) {
 	provider = (GenericProvider) newProvider;
   }          
 /**
  * Private state settor to be called by the toActive and toInvalid methods.
  * Creation date: (2000-04-26 10:12:38)
  * @author: Richard Deadman
  * @param newState The new state for the call.
  */
 private void setState(int newState) {
 	state = newState;
 }
 /**
  * setTransferController method comment.
  */
 public void setTransferController(TerminalConnection tc) throws InvalidStateException, MethodNotSupportedException, ResourceUnavailableException, InvalidArgumentException {
 	if (tc instanceof FreeTerminalConnection)
 		transController = (FreeTerminalConnection)tc;
 	else
 		throw new InvalidArgumentException();
 }
 /**
  * setTransferEnable method comment.
  */
 public void setTransferEnable(boolean enable) throws javax.telephony.InvalidStateException, javax.telephony.MethodNotSupportedException, javax.telephony.PrivilegeViolationException, javax.telephony.InvalidArgumentException {}
 /**
  * Tell the raw TelephonyProvider to stop reporting events on me.
  * <P>Don't bother synchronizing -- its unlikely that the gabage collector and an invalid
  * signal will collide, and if they do all we lose is an extra TelephonyProvider message.
  * Creation date: (2000-06-22 13:38:56)
  * @author: Richard Deadman
  */
 private void stopReporting() {
 	GenericProvider prov = (GenericProvider)this.getProvider();
 
 	if (!this.stoppedReporting && prov.getCallMgr().isDynamic()) {
 		// tell the raw TelephonyProvider to stop reporting my calls
 		prov.getRaw().stopReportingCall(this.getCallID());
 		this.stoppedReporting = true;
 	}
 }
 /**
  * Defines the actions to be taken when the Call moves to the Active state.
  * <P>We don't use a normal State Pattern here since our actions are state transition actions which
  * do not change the resulting state depending on the starting state.
  * Creation date: (2000-05-04 23:58:34)
  * @author: Richard
  */
 void toActive(int cause) {
 	this.setState(Call.ACTIVE);
 	
 	// notify any listeners
 	this.getGenProvider().dispatch(new FreeCallActiveEv(cause, this));
 }
 /**
  * Defines the actions to be taken when the Call moves to the Invalid state.
  * <P>We don't use a normal State Pattern here since our actions are state transition actions which
  * do not change the resulting state depending on the starting state.
  * Creation date: (2000-05-04 23:58:34)
  * @author: Richard Deadman
  */
 void toInvalid(int cause) {
 	// unHook any connections
 	Iterator conns = this.connections.values().iterator();
 	while (conns.hasNext()) {
 		((FreeConnection)conns.next()).toDisconnected(cause);
 	}
 
 	// set state
 	this.setState(Call.INVALID);
 
 	// notify any listeners, postpoing the cleanup until the event is processed
     this.getGenProvider().dispatch(new FreeCallInvalidEv(cause, this));
 }
 
 /**
  * Cleanup the call.
  * <P>This is called by the FreeCallInvalidEv.dispatch() method when the event
  * has been sent to everyone. Since they are in different packages, this method
  * must be public. It should not be called by any other objects.
  */
 public void cleanup() {
 	CallId callId = this.getCallID();
 	// check to make sure we haven't cleaned up already.
 	if (callId != null) {
 		// unregister any remaining listeners
 		this.getListenerMgr().removeAll();
 	
 		// tell the raw TelephonyProvider that it may now recycle the CallId
 		GenericProvider prov = this.getGenProvider();
 		prov.getRaw().releaseCallId(callId);
 	
 		// remove from framework
 		prov.getCallMgr().removeCall(this);
 		
 		// ensure we don't cleanup twice
 		this.setCallID(null);
 	}
 }
 /**
  * This overloaded version of this method transfers all participants
  * currently on this Call, with the exception of the transfer controller
  * participant, to another telephone address. This is often called a
  * "single-step transfer" because the transfer feature places another
  * telephone call and performs the transfer at one time. The telephone
  * address string given as the argument to this method must be valid and
  * complete.
  * <P>Note that if a transfer controller is not specified, one will be
  * choosen which may lead to an inappropriate call participant being
  * removed from the call.
  */
 public Connection transfer(String address) throws MethodNotSupportedException, ResourceUnavailableException, InvalidArgumentException, InvalidPartyException, InvalidStateException, PrivilegeViolationException {
 
 	// test for transfer TerminalController
 	TerminalConnection trans = this.getTransferController();
 	if (trans == null) {	// look for the first available TC
 		Connection[] cs = this.getConnections();
 		for (int i = 0; i < cs.length; i++) {
 			TerminalConnection[] tcs = cs[i].getTerminalConnections();
 			if (tcs != null && tcs.length > 0) {
 				trans = tcs[0];
 				break;
 			}
 		}
 	}
 	if (trans == null)
 		throw new InvalidArgumentException("Transfer TerminalConnection not set and cannot be found");
 
 	// hold the call
 	if (trans instanceof CallControlTerminalConnection) {
 		((CallControlTerminalConnection)trans).hold();
 	}
 	
 	// Create a consultation call
 	CallControlCall consult = (CallControlCall)this.getProvider().createCall();
 	consult.consult(trans, address);
 
 	// transfer the call
 	this.transfer(trans, consult);
 
 	// Find the remote connection
 	Connection conn[] = this.getConnections();
 	Connection rem = null;
 	for (int i = 0; i < conn.length; i++) {
 		if (conn[i].getAddress().getName().equals(address)) {
 			rem = conn[i];
 			break;
 		}
 	}
 	return rem;
 }
 /**
  * transfer by conferencing and then dropping off the call
  */
 public void transfer(Call otherCall) throws MethodNotSupportedException, ResourceUnavailableException, InvalidArgumentException, InvalidPartyException, InvalidStateException, PrivilegeViolationException {
 	// see if we can find a common terminal
 	FreeTerminalConnection tc = (FreeTerminalConnection)this.getTransferController();
 	if (tc == null) {	// look for the first shared TC
 		tc = this.findCommonTC(otherCall);
 	}
 	if (tc == null)
 		throw new InvalidArgumentException("Controlling TerminalConnection not found");
 
 	transfer(tc, otherCall);
 }
 /**
  * transfer by conferencing and then dropping off the call.
  * Note that this will drop the whole Connection off the call, and not just the TerminalConnection.
  */
 private void transfer(TerminalConnection tc, Call otherCall) throws MethodNotSupportedException, ResourceUnavailableException, InvalidArgumentException, InvalidPartyException, InvalidStateException, PrivilegeViolationException {
 	
 	// join the calls - don't use my conference method, since it uses the conference controller
 	TelephonyProvider rp = ((GenericProvider)this.getProvider()).getRaw();
 	CallId id = this.getCallID();
 	FreeConnection tcConn = (FreeConnection)tc.getConnection();
 	String tcAddress = tcConn.getAddress().getName();
 	String tcTerminal = tc.getTerminal().getName();
 	
 	rp.join(id, ((FreeCall)otherCall).getCallID(), tcAddress, tcTerminal);
 
 	// now drop the connection off the call
 	rp.release(tcAddress, id);
 	// and note that the old Connection is disconnected
 	tcConn.toDisconnected(Event.CAUSE_NORMAL);
 }
 
 	/**
 	 * Describe myself
 	 */
 	public String toString() {
 		StringBuffer sb = new StringBuffer();
 		switch (this.getState()) {
 			case Call.IDLE:
 				sb.append("Idle");
 				break;
 			case Call.ACTIVE:
 				sb.append("Active");
 				break;
 			default:
 				sb.append("Invalid");
 		}
 		sb.append(" call with ").append(this.getConnections().length)
 			.append(" connections.");
 		return sb.toString();
 	}
 }
