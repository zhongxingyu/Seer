 package ca.deadman.gjtapi.raw.remote.webservices;
 
 /*
 	Copyright (c) 2003 Richard Deadman, Deadman Consulting (www.deadman.ca)
 
 	All rights reserved.
 
 	This software is dual licenced under the GPL and a commercial license.
 	If you wish to use under the GPL, the following license applies, otherwise
 	please contact Deadman Consulting at sales@deadman.ca for commercial licensing.
 
     ---
 
 	This program is free software; you can redistribute it and/or
 	modify it under the terms of the GNU General Public License
 	as published by the Free Software Foundation; either version 2
 	of the License, or (at your option) any later version.
 
 	This program is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU General Public License for more details.
 
 	You should have received a copy of the GNU General Public License
 	along with this program; if not, write to the Free Software
 	Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
 import java.util.HashMap;
 import java.util.Properties;
 import java.rmi.*;
 
 /**
  * This is the JAX-RPC definition of the remote interface for a webservice bridge for GJTAPI.
  * This defines the remote methods that the GJTAPI framework can make to access a remote
  * telephony system.
  * <P>Unlike the RMI and CORBA distribution bridges, web services does not support callbacks.
  * This means that we must employ a polling mechanism to listen for events from the web service end.
  *
  * @author: Richard Deadman
  **/
 
 public interface GJtapiWebServiceIF extends Remote {
 /**
   * Ask the Web service to register an event queue, so that telephony events can queued up for
   * delivery to the required client.
   *
   * @return a unique id for the queue.
    * @exception RemoteException A distribution (network) error has occured.
   *
   * @author: Richard Deadman
   **/
 int registerQueue() throws RemoteException;
 /**
   * Ask for the latest set of Events. This method will block until an event is ready to be processes, so
   * evs.length() should always be >= 1.
   *
   * @param id The queue id to retrieve events from
   * @return an array of Event objects.
    * @exception RemoteException A distribution (network) error has occured.
   *
   * @author: Richard Deadman
   **/
 EventHolder[] pollEvents(int id) throws RemoteException;
 /**
   * Allocate a media resource for a terminal
   *
   * @param terminal The terminal to attach a media resource to.
   * @param type A constant defining the type of resource to add.
   * @param params A dictionary of resource control parameters.
   * @return true if the resource is allocated
   **/
 boolean allocateMedia(String terminal, int type, HashMap params) throws RemoteException;
 /**
   * Answer a call that has appeared at a particular terminal
   *
   * @param call The system identifier for the call
   * @param address The address the call is to be answered on.
   * @param terminal the terminal to answer the call on.
   * @exception RemoteException A distribution problem has occured.
   **/
 void answerCall(int call, String address, String terminal) throws RemoteException, MobileJavaxException, MobileStateException;
 /**
  * Jcc command to attach a call's media stream to the call
  */
 public boolean attachMedia(int call, String address, boolean onFlag) throws RemoteException;
 /**
  * Jcc command to issue a tone on a call.
  */
 public void beep(int call) throws RemoteException;
 /**
  * Make a call.  Not that this follows the JTAPI sematics of an idle call
  * being created synchronusly (two connections).  Events from the raw provider
  * will indicate state transitions.
  *
  * @param id The callId reserved for the call.
  * @param address The logical address to make a call from
  * @param term The physical address for the call, if applicable
  * @param dest The destination address
  * @return A call Id.  This may be used later to track call progress.
  * @exception RemoteException A distribution (network) error has occured.
  *
  * @author: Richard Deadman
  **/
 int createCall(int id, String address, String term, String dest) throws RemoteException, MobileJavaxException,
 	  MobileStateException;
 /**
   * Free a media resource from a terminal
   *
   * @param terminal The terminal to release a media resource from.
   * @param type A constant defining the type of resource to release.
   * @return true if the resource is freed.
   **/
 boolean freeMedia(String terminal, int type) throws RemoteException;
 /**
  * Get a list of available addresses
  * Creation date: (2000-02-11 12:29:00)
  * @author: Richard Deadman
  * @return An array of address names
  * @exception RemoteException A distribution (network) error has occured.
  * @exception ResourceUnavailableException If the Address set is too big to return.
  */
 String[] getAddresses() throws RemoteException, MobileJavaxException;
 /**
  * Get all the addresses associated with an terminal.
  * Generally this is only called during dynamic Terminal exploration.
  * Creation date: (2000-06-05 12:30:54)
  * @author: Richard Deadman
  * @return An array of address names.
  * @param terminal The terminal we want addresses for.
  * @exception RemoteException A distribution (network) error has occured.
  * @exception InvalidArgumentException If the terminal is not known.
  */
 String[] getAddressesForTerminal(String terminal) throws RemoteException, MobileJavaxException;
 /**
  * Get the type of the Address - useful for Jcc classification.
  */
 int getAddressType(String name) throws RemoteException;
 /**
  *
  * @return net.sourceforge.gjtapi.raw.remote.webservices.MovableCallData
  * @param id net.sourceforge.gjtapi.CallId
  * @exception java.rmi.RemoteException The exception description.
  */
 MovableCallData getCall(int id) throws RemoteException;
 /**
  * Ask the raw TelephonyProvider to give a snapshot of all Calls on an Address.
  * <P>This will only be called on a TelephonyProvider that "trottle"s call events.
  * <P><B>Note:</B> This implies that the given Call will have events delivered on it until such time
  * as a "TelephonyProvider::releaseCallId(CallId)".
  * Creation date: (2000-06-20 15:22:50)
  * @author: Richard Deadman
  * @return A set of call data.
  * @param number The Address's logical number
  */
 MovableCallData[] getCallsOnAddress(String number) throws RemoteException;
 /**
  * Ask the raw TelephonyProvider to give a snapshot of all Calls at a Terminal.
  * <P>This will only be called on a TelephonyProvider that "trottle"s call events.
  * <P><B>Note:</B> This implies that the given Calls will have events delivered on it until such time
  * as a "TelephonyProvider::releaseCallId(CallId)".
  * Creation date: (2000-06-20 15:22:50)
  * @author: Richard Deadman
  * @return A set of call data.
  * @param name The Terminal's logical name
  */
 MovableCallData[] getCallsOnTerminal(String name) throws RemoteException;
 /**
  * Ask the raw provider to update the capabilities offered by the provider
  * This is expected to return a map of capability names to strings.  If the string starts with
  * 't' or 'T', the capability is turned on, otherwise it is turned off.  If the value is not found for
  * a key, the default value is used.
  * <P>To use this feature, the RawProvider needs to copy the GenericCapabilities.props file and change any
  * properties that are supported differently.  The the RawProvider could load the properties file into
  * a Properties object and return it.  If the default value is supported, then the corresponding line
  * may be omitted from the file.  Note that the generic layer may choose to ignore certain
  * settings if they are not supported at that layer.
  * Creation date: (2000-03-14 14:48:36)
  * @author: Richard Deadman
  * @return java.util.Map
  */
 Properties getCapabilities() throws RemoteException;
 /**
  * getDialledDigits method comment.
  */
 String getDialledDigits(int id, String address) throws RemoteException;
 /**
  * Get a list of available terminals.
  * This may be null if the Telephony (raw) Provider does not support Terminals.
  * If the Terminal set it too large, this will throw a ResourceUnavailableException
  * <P>Since we went to lazy connecting between Addresses and Terminals, this is called so
  * we don't have to follow all Address->Terminal associations to get the full set of Terminals.
  * Creation date: (2000-02-11 12:29:00)
  * @author: Richard Deadman
  * @return An array of terminal names, media type containers.
  * @exception ResourceUnavailableException if the set it too large to be returned dynamically.
  */
 TermData[] getTerminals() throws MobileJavaxException, RemoteException;
 /**
  * Get all the terminals associated with an address.
  * Creation date: (2000-02-11 12:30:54)
  * @author: Richard Deadman
  * @return An array of terminal name, media type containers.
  * @param address The address number we want terminal names for.
  * @throws InvalidArgumentException indicating that the address is unknown.
  * @exception RemoteException A distribution (network) error has occured.
  */
 TermData[] getTerminalsForAddress(String address) throws RemoteException, MobileJavaxException;
 /**
   * Put a call on hold (CallControlTerminalConnection)
   *
   * @param term The terminal that we want to make on hold
   * @param address The address on the terminal that holds the call leg to hold
   * @exception RemoteException A distribution (network) error has occured.
   *
   * @author: Richard Deadman
   **/
 void hold(int call, String address, String term) throws RemoteException, MobileStateException, MobileJavaxException;
 /**
  * Ask the RawProvider if the named terminal is media-capable.
  * Creation date: (2000-03-07 15:35:02)
  * @author: Richard Deadman
  * @return true if the terminal can be used with media control commands.
  * @param terminal The raw-provider specific unique name for the terminal
  */
 boolean isMediaTerminal(String terminal) throws RemoteException;
 /**
   * Join one call to another call (Connection)
   * For assisted transfer, this allows the joining of an on-hold call to an active call.
   * Unassisted transfer and single-step transfer can be accomplished in the JTAPI layer by
   * combining this call with a release() method call.
   *
   * @param call1 One call
   * @param call Another call
   * @param address The address of a TerminalConnection to be used as a ConferenceController
   * @param terminal The terminal of a TerminalConnection to be used as a ConferenceController
   * @return The new call.  This may be the same as one of the original calls.
   *
   * @exception RemoteException A distribution (network) error has occured.
   *
   * @author: Richard Deadman
   **/
 int join(int call1, int call2, String address, String terminal) throws RemoteException, MobileStateException, MobileJavaxException;
 	 /**
 	  * Start playing a set of audio streams named by the streamIds (may be urls).
 	  *
 	  * @param terminal The terminal to play the audio on.
 	  * @param streamIds The ids for the audi streams to play, usually URLs
 	  * @param int offset The number of milliseconds into the audio to start
 	  * @param rtcs A set of runtime control sets that tune the playing.
 	  * @param optArgs A dictionary of control arguments.
 	  *
 	  * @exception MobileResourceException A wrapper for a PlayerEvent that describes what went wrong.
 	  **/
 	void play(String terminal, String[] streamIds, int offset, RTCPair[] rtcs, HashMap optArgs) throws MobileResourceException, RemoteException;
 /**
   * Start recording an audio streams named by the streamId (may be urls).
   *
   * @param terminal The terminal to record the audio from.
   * @param streamId The id for the audio streams to create, usually a URL
   * @param rtcs A set of runtime control sets that tune the recording.
   * @param optArgs A dictionary of control arguments.
   *
   * @exception MobileResourceException A wrapper for a RecorderEvent that describes what went wrong.
   **/
 void record(String terminal, String streamId, RTCPair[] rtcs, HashMap optArgs) throws MobileResourceException, RemoteException;
 /**
   * Release a connection to a call (Connection)
   *
   * @param address The address that we want to release
   * @param call The call to disconnect from
   *
   * @exception RemoteException A distribution (network) error has occured.
   *
   * @author: Richard Deadman
   **/
 void release(String address, int call) throws RemoteException, MobileJavaxException, MobileStateException;
 /**
  * Tell the provider that it may release a call id for future use.  This is necessary to ensure that
  * provider call ids are not released until the JTAPI layer is notified of the death of the call.
  * Creation date: (2000-02-17 22:25:48)
  * @author: Richard Deadman
  * @param id The CallId that may be freed.
  */
 void releaseCallId(int id) throws RemoteException;
 /**
   * Remove a listener queue for RawEvents.
   * <P>If more than one client registers with the web service, the service must create event queues
   * for each client so that they all get all the generated events. When the remote end no longer wants
   * to poll for events, it should tell the web service to remove its queue.
   * <P>Of course, the web service may want to set a time out after which the event queue is tossed
   * away, so that it can handle clients who don't clean up after themselves.
   *
   * @param id Event queue id that is no longer needed
   * @return void
   * @exception RemoteException A distribution (network) error has occured.
   *
   * @author: Richard Deadman
   **/
 void removeQueue(int id) throws RemoteException;
 /**
  * Tell the RawProvider that Calls which reach this Address should have Call, Connection and TerminalConnections
  * events reported on them.
  * Creation date: (2000-03-07 15:17:16)
  * @author: Richard Deadman
  * @param address The name of the address to change the reporting status on.
  * @param flag true if reporting is to be enabled, false to stop events relating to the call.
  * @exception InvalidArgumentException If the address is not known by the telephony provider.
  * @exception ResourceUnavailableException If the provider could not find the reources to report the calls.
  */
 void reportCallsOnAddress(String address, boolean flag) throws RemoteException, MobileJavaxException;
 /**
  * Tell the RawProvider that Calls which reach this Terminal should have Call, Connection and TerminalConnections
  * events reported on them.
  * Creation date: (2000-03-07 15:17:16)
  * @author: Richard Deadman
  * @return true if the terminal is known, false otherwise
  * @param address The name of the terminal to change the reporting status on.
  * @param flag true if reporting is to be enabled, false to stop events relating to the call.
  * @exception InvalidArgumentException If the terminal is not known by the telephony provider.
  * @exception ResourceUnavailableException If the reporting could not be started due to resource constraints.
  */
 void reportCallsOnTerminal(String terminal, boolean flag) throws RemoteException, MobileJavaxException;
 /**
   * Tell the provider to reserve a call id for future use.  The provider does not have to hang onto it.
   * Creation date: (2000-02-16 14:48:48)
   * @author: Richard Deadman
   * @return The CallId created by the provider.
   * @param address The address the call will start from.
   * @exception RemoteException A distribution (network) error has occured.
   * @exception InvalidArgumentException The remote provider does not know the Address.
   *
   * @author: Richard Deadman
   **/
 int reserveCallId(String address) throws RemoteException, MobileJavaxException;
 /**
   * Receive DTMF tones from a terminal
   *
   * @param terminal The terminal the signal receiver is attached to.
   * @param num The number of signals to retrieve
   * @param syms A set of symbols patterns to return
   * @param rtcs A set of runtime control sets that tune the signalling.
   * @param optArgs A dictionary of control arguments.
   * @return A RawSigDetectEvent factory for creating an event that can be sent getSignalBuffer() to retrieve the signals.
   *
   * @exception MobileResourceException A wrapper for a SignalDetectorEvent that describes what went wrong.
   **/
 EventHolder retrieveSignals(String terminal, int num, int[] patterns, RTCPair[] rtcs, HashMap optArgs) throws MobileResourceException, RemoteException;
 /**
   * Play DTMF tones on a terminal
   *
   * @param terminal The terminal to record the audio from.
   * @param syms A set of symbols to play
   * @param rtcs A set of runtime control sets that tune the signalling.
   * @param optArgs A dictionary of control arguments.
   *
   * @exception MobileResourceException A wrapper for a SignalGeneratorEvent that describes what went wrong.
   **/
 void sendSignals(String terminal, int[] syms, RTCPair[] rtcs, HashMap optArgs) throws MobileResourceException, RemoteException;
 /**
  * setLoadControl method comment.
  */
 void setLoadControl(String startAddr, String endAddr, double duration, double admissionRate, double interval, int[] treatment) throws MobileJavaxException, RemoteException;
 /**
   * Perform any cleanup after my holder has finished with me.
   * Creation date: (2000-02-11 13:07:46)
   * @exception RemoteException A distribution (network) error has occured.
   *
   * @author: Richard Deadman
   **/
 void shutdown() throws RemoteException;
 /**
  * Stop any media resources attached to a terminal.
  * Creation date: (2000-03-09 16:08:12)
  * @author: Richard Deadman
  * @param terminal The terminal name.
  */
 void stop(String terminal) throws RemoteException;
 /**
  * Tell the RawProvider that a certain Call no longer needs to have Call, Connection and
  * TerminalConnections events reported.
  * Creation date: (2000-03-07 15:17:16)
  * @author: Richard Deadman
  * @return true if the call is known, false otherwise
  * @param call The handle on the call to turn state change reporting off for.
  */
 boolean stopReportingCall(int call) throws RemoteException;
 /**
  * Send Runtime control actions to media resources bound to a terminal.
  * Creation date: (2000-03-09 16:09:09)
  * @author: Richard Deadman
  * @param terminal The name of the terminal the media resources are bound to.
  * @param action The RTC action symbol to invoke on the media resources.
  */
 void triggerRTC(String terminal, int action) throws RemoteException;
 /**
   * Take a call off hold (CallControlTerminalConnection)
   *
   * @param term The terminal that we want to take off hold
   * @param call The call to reconnect to
   *
   * @exception RemoteException A distribution (network) error has occured.
   *
   * @author: Richard Deadman
   **/
 void unHold(int call, String address, String term) throws RemoteException, MobileStateException, MobileJavaxException;
 }
