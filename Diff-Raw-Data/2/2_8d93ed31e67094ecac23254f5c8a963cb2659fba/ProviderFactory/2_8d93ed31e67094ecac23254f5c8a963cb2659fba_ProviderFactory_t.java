 package net.sourceforge.gjtapi.raw;
 
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
 import net.sourceforge.gjtapi.*;
 import net.sourceforge.gjtapi.capabilities.Capabilities;
 import net.sourceforge.gjtapi.raw.*;
 import javax.telephony.*;
 /**
  * This is a factory for creating a TelephonyProvider wrapper around a coreTpi implementation.
  * <P>The big problem is that the TPI architecture relies on capabilities being dyanically
  * assigned and plugged in.  Unfortunatly, both media and PrivateData rely on interface
  * implementation to determine if a capability is supported.  Since it is too hard to create
  * generic objects at runtime that selectively implement all these interfaces as more
  * capabilites are added, we choose to provide default No-op behaviour when MethodNotSupported
  * cannot be thrown.
  * Creation date: (2000-10-04 14:19:45)
  * @author: Richard Deadman
  */
 public class ProviderFactory implements TelephonyProvider {
 	private CoreTpi core;
 	private BasicJtapiTpi basicJtapi;
 	private CCTpi callControl;
 	private MediaTpi media;
 	private PrivateDataTpi privateData;
 	private ThrottleTpi throttle;
 	private JccTpi jcc;
 /**
  * ProviderFactory constructor comment.
  */
 private ProviderFactory(CoreTpi tpi) {
 	super();
 
 	this.core = tpi;
 
 	if (tpi instanceof BasicJtapiTpi) {
 		this.basicJtapi = (BasicJtapiTpi)tpi;
 	}
 	if (tpi instanceof CCTpi) {
 		this.callControl = (CCTpi)tpi;
 	}
 	if (tpi instanceof MediaTpi) {
 		this.media = (MediaTpi)tpi;
 	}
 	if (tpi instanceof ThrottleTpi) {
 		this.throttle = (ThrottleTpi)tpi;
 	}
 	if (tpi instanceof PrivateDataTpi) {
 		this.privateData = (PrivateDataTpi)tpi;
 	}
 	if (tpi instanceof JccTpi) {
 		this.jcc = (JccTpi)tpi;
 	}
 }
 /**
  * addListener method comment.
  */
 public void addListener(TelephonyListener ro) {
 	this.core.addListener(ro);
 }
 /**
  * allocateMedia method comment.
  */
 public boolean allocateMedia(String terminal, int type, java.util.Dictionary resourceArgs) {
 	if (this.media != null)
 		return this.media.allocateMedia(terminal, type, resourceArgs);
 	else
 		return false;
 }
 /**
  * answerCall method comment.
  */
 public void answerCall(CallId call, String address, String terminal) throws PrivilegeViolationException, MethodNotSupportedException, ResourceUnavailableException, RawStateException {
 	this.core.answerCall(call, address, terminal);
 }
 /**
  * attachMedia method comment.
  */
 public boolean attachMedia(net.sourceforge.gjtapi.CallId call, java.lang.String address, boolean onFlag) {
 	if (this.jcc != null)
 		return this.jcc.attachMedia(call, address, onFlag);
 	else
 		return true;	// silent ignore
 }
 /**
  * beep method comment.
  */
 public void beep(net.sourceforge.gjtapi.CallId call) {
 	if (this.jcc != null)
 		this.jcc.beep(call);
 }
 /**
  * createCall method comment.
  */
 public CallId createCall(CallId id, String address, String term, String dest) throws MethodNotSupportedException, RawStateException, ResourceUnavailableException, InvalidPartyException, InvalidArgumentException, PrivilegeViolationException {
 	return this.core.createCall(id, address, term, dest);
 }
 /**
  * Create a wrapper TelephonyProvider if necessary.
  * Creation date: (2000-10-04 14:30:23)
  * @return net.sourceforge.gjtapi.TelephonyProvider
  * @param tpi net.sourceforge.gjtapi.raw.CoreTpi
  */
 public static TelephonyProvider createProvider(CoreTpi tpi) {
 	if (tpi instanceof TelephonyProvider)
 		return (TelephonyProvider)tpi;
 	else
 		return new ProviderFactory(tpi);
 }
 /**
  * freeMedia method comment.
  */
 public boolean freeMedia(String terminal, int type) {
 	if (this.media != null)
 		return this.media.freeMedia(terminal, type);
 	else
 		return false;
 }
 /**
  * getAddresses method comment.
  */
 public java.lang.String[] getAddresses() throws ResourceUnavailableException {
 	if (this.basicJtapi != null)
 		return this.basicJtapi.getAddresses();
 	else
 		throw new ResourceUnavailableException(ResourceUnavailableException.OBSERVER_LIMIT_EXCEEDED,
 				"Address querying not supported");
 
 }
 /**
  * getAddresses method comment.
  */
 public java.lang.String[] getAddresses(String terminal) throws InvalidArgumentException {
 	if (this.basicJtapi != null)
 		return this.basicJtapi.getAddresses(terminal);
 	else
 		return null;
 ;
 }
 /**
  * getAddressType method comment.
  */
 public int getAddressType(java.lang.String name) {
 	if (this.jcc != null)
 		return this.jcc.getAddressType(name);
 	else
 		return jain.application.services.jcc.JccAddress.UNDEFINED;	// Can't return type
 }
 /**
  * getCall method comment.
  */
 public CallData getCall(CallId id) {
 	if (this.throttle != null)
 		return this.throttle.getCall(id);
 	else
 		return null;
 }
 /**
  * getCallsOnAddress method comment.
  */
 public net.sourceforge.gjtapi.CallData[] getCallsOnAddress(String number) {
 	if (this.throttle != null)
 		return this.throttle.getCallsOnAddress(number);
 	else
 		return null;
 }
 /**
  * getCallsOnTerminal method comment.
  */
 public net.sourceforge.gjtapi.CallData[] getCallsOnTerminal(String name) {
 	if (this.throttle != null)
 		return this.throttle.getCallsOnTerminal(name);
 	else
 		return null;
 }
 /**
  * getCapabilities method comment.
  */
 public java.util.Properties getCapabilities() {
 	java.util.Properties props = this.core.getCapabilities();
 	if (this.callControl == null) {
 		props.put(Capabilities.HOLD, "f");
 		props.put(Capabilities.JOIN, "f");
 		props.put(Capabilities.RELEASE, "f");
 	}
 
 	if (this.media == null) {
 		props.put(Capabilities.MEDIA, "f");
 		props.put(Capabilities.ALL_MEDIA_TERMINALS, "f");
 		props.put(Capabilities.ALLOCATE_MEDIA, "f");
 	}
 
 	if (this.throttle == null) {
 		props.put(Capabilities.THROTTLE, "f");
 		props.put(Capabilities.DYNAMIC_ADDRESSES, "f");
 	}
 
 	if (this.privateData == null) {
 		props.put(Capabilities.PROV + Capabilities.GET, "f");
 		props.put(Capabilities.PROV + Capabilities.SEND, "f");
 		props.put(Capabilities.PROV + Capabilities.SET, "f");
 	
 		props.put(Capabilities.CALL + Capabilities.GET, "f");
 		props.put(Capabilities.CALL + Capabilities.SEND, "f");
 		props.put(Capabilities.CALL + Capabilities.SET, "f");
 	
 		props.put(Capabilities.ADDR + Capabilities.GET, "f");
 		props.put(Capabilities.ADDR + Capabilities.SEND, "f");
 		props.put(Capabilities.ADDR + Capabilities.SET, "f");
 	
 		props.put(Capabilities.TERM + Capabilities.GET, "f");
 		props.put(Capabilities.TERM + Capabilities.SEND, "f");
 		props.put(Capabilities.TERM + Capabilities.SET, "f");
 	
 		props.put(Capabilities.CONN + Capabilities.GET, "f");
 		props.put(Capabilities.CONN + Capabilities.SEND, "f");
 		props.put(Capabilities.CONN + Capabilities.SET, "f");
 	
 		props.put(Capabilities.TERM_CONN + Capabilities.GET, "f");
 		props.put(Capabilities.TERM_CONN + Capabilities.SEND, "f");
 		props.put(Capabilities.TERM_CONN + Capabilities.SET, "f");
 	}
 	
 	return props;
 }
 /**
  * getDialledDigits method comment.
  */
 public java.lang.String getDialledDigits(net.sourceforge.gjtapi.CallId id, java.lang.String address) {
 	if (this.jcc != null)
 		return this.jcc.getDialledDigits(id, address);
 	else
 		return null;	// signal that they are not known
 }
 /**
  * getPrivateData method comment.
  */
 public Object getPrivateData(CallId call, String address, String terminal) {
 	if (this.privateData != null)
 		return this.privateData.getPrivateData(call, address, terminal);
 	else
 		return null;
 }
 /**
  * getTerminals method comment.
  */
 public net.sourceforge.gjtapi.TermData[] getTerminals() throws ResourceUnavailableException {
 	if (this.basicJtapi != null)
 		return this.basicJtapi.getTerminals();
 	else
            throw new ResourceUnavailableException(ResourceUnavailableException.OBSERVER_LIMIT_EXCEEDED, "Terminal querying not supported");
 }
 /**
  * getTerminals method comment.
  */
 public net.sourceforge.gjtapi.TermData[] getTerminals(String address) throws InvalidArgumentException {
 	if (this.basicJtapi != null)
 		return this.basicJtapi.getTerminals(address);
 	else
 		return null;
 }
 /**
  * hold method comment.
  */
 public void hold(CallId call, String address, String terminal) throws MethodNotSupportedException, RawStateException, PrivilegeViolationException, ResourceUnavailableException {
 	if (this.callControl != null) {
 		this.callControl.hold(call, address, terminal);
 	} else {
 		throw new MethodNotSupportedException();
 	}
 }
 /**
  * initialize method comment.
  */
 public void initialize(java.util.Map props) throws ProviderUnavailableException {
 	this.core.initialize(props);
 }
 /**
  * isMediaTerminal method comment.
  */
 public boolean isMediaTerminal(String terminal) {
 	if (this.media != null)
 		return this.media.isMediaTerminal(terminal);
 	else
 		return false;
 }
 /**
  * join method comment.
  */
 public CallId join(CallId call1, CallId call2, String address, String terminal) throws MethodNotSupportedException, RawStateException, PrivilegeViolationException, InvalidArgumentException, ResourceUnavailableException {
 	if (this.callControl != null)
 		return this.callControl.join(call1, call2, address, terminal);
 	else
 		throw new MethodNotSupportedException();
 }
 /**
  * play method comment.
  */
 public void play(String terminal, java.lang.String[] streamIds, int offset, javax.telephony.media.RTC[] rtcs, java.util.Dictionary optArgs) throws javax.telephony.media.MediaResourceException {
 	if (this.media != null)
 		this.media.play(terminal, streamIds, offset, rtcs, optArgs);
 }
 /**
  * record method comment.
  */
 public void record(String terminal, String streamId, javax.telephony.media.RTC[] rtcs, java.util.Dictionary optArgs) throws javax.telephony.media.MediaResourceException {
 	if (this.media != null)
 		this.media.record(terminal, streamId, rtcs, optArgs);
 }
 /**
  * release method comment.
  */
 public void release(String address, CallId call) throws PrivilegeViolationException, MethodNotSupportedException, ResourceUnavailableException, RawStateException {
 	if (this.callControl != null)
 		this.callControl.release(address, call);
 	else
 		throw new MethodNotSupportedException();
 }
 /**
  * releaseCallId method comment.
  */
 public void releaseCallId(CallId id) {
 	this.core.releaseCallId(id);
 }
 /**
  * removeListener method comment.
  */
 public void removeListener(TelephonyListener ro) {
 	this.core.removeListener(ro);
 }
 /**
  * reportCallsOnAddress method comment.
  */
 public void reportCallsOnAddress(String address, boolean flag) throws ResourceUnavailableException, InvalidArgumentException {
 	if (this.throttle != null)
 		this.throttle.reportCallsOnAddress(address, flag);
 }
 /**
  * reportCallsOnTerminal method comment.
  */
 public void reportCallsOnTerminal(String terminal, boolean flag) throws ResourceUnavailableException, InvalidArgumentException {
 	if (this.throttle != null)
 		this.throttle.reportCallsOnTerminal(terminal, flag);
 }
 /**
  * reserveCallId method comment.
  */
 public CallId reserveCallId(String address) throws InvalidArgumentException {
 	return this.core.reserveCallId(address);
 }
 /**
  * retrieveSignals method comment.
  */
 public RawSigDetectEvent retrieveSignals(String terminal, int num, javax.telephony.media.Symbol[] patterns, javax.telephony.media.RTC[] rtcs, java.util.Dictionary optArgs) throws javax.telephony.media.MediaResourceException {
 	if (this.media != null) {
 		return this.media.retrieveSignals(terminal, num, patterns, rtcs, optArgs);
 	} else
 		return null;
 }
 /**
  * sendPrivateData method comment.
  */
 public Object sendPrivateData(CallId call, String address, String terminal, Object data) {
 	if (this.privateData != null) {
 		return this.privateData.sendPrivateData(call, address, terminal, data);
 	} else
 		return null;
 }
 /**
  * sendSignals method comment.
  */
 public void sendSignals(String terminal, javax.telephony.media.Symbol[] syms, javax.telephony.media.RTC[] rtcs, java.util.Dictionary optArgs) throws javax.telephony.media.MediaResourceException {
 	if (this.media != null) {
 		this.media.sendSignals(terminal, syms, rtcs, optArgs);
 	}
 }
 /**
  * setLoadControl method comment.
  */
 public void setLoadControl(java.lang.String startAddr, java.lang.String endAddr, double duration, double admissionRate, double interval, int[] treatment) throws javax.telephony.MethodNotSupportedException {
 	if (this.jcc != null)
 		this.jcc.setLoadControl(startAddr, endAddr, duration, admissionRate, interval, treatment);
 	else
 		throw new MethodNotSupportedException("Low-level Provider does not implement Jcc methods");
 }
 /**
  * setPrivateData method comment.
  */
 public void setPrivateData(CallId call, String address, String terminal, Object data) {
 	if (this.privateData != null) {
 		this.privateData.setPrivateData(call, address, terminal, data);
 	}
 }
 /**
  * shutdown method comment.
  */
 public void shutdown() {
 	this.core.shutdown();
 }
 /**
  * stop method comment.
  */
 public void stop(String terminal) {
 	if (this.media != null) {
 		this.media.stop(terminal);
 	}
 }
 /**
  * stopReportingCall method comment.
  */
 public boolean stopReportingCall(CallId call) {
 	if (this.throttle != null) {
 		return this.throttle.stopReportingCall(call);
 	} else
 		return false;
 }
 /**
  * Returns a String that represents the value of this object.
  * @return a string representation of the receiver
  */
 public String toString() {
 	// Insert code to print the receiver here.
 	// This implementation forwards the message to super. You may replace or supplement this.
 	return this.core.toString();
 }
 /**
  * triggerRTC method comment.
  */
 public void triggerRTC(String terminal, javax.telephony.media.Symbol action) {
 	if (this.media != null) {
 		this.media.triggerRTC(terminal, action);
 	}
 }
 /**
  * unHold method comment.
  */
 public void unHold(CallId call, String address, String terminal) throws MethodNotSupportedException, RawStateException, PrivilegeViolationException, ResourceUnavailableException {
 	if (this.callControl != null)
 		this.callControl.unHold(call, address, terminal);
 	else
 		throw new MethodNotSupportedException();
 }
 }
