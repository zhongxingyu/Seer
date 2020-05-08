 /*
 	Copyright (c) 2005 Deadman Consulting Inc. (www.deadman.ca) 
 
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
 package net.sourceforge.gjtapi.raw.asterisk;
 
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.telephony.InvalidArgumentException;
 import javax.telephony.InvalidPartyException;
 import javax.telephony.MethodNotSupportedException;
 import javax.telephony.PrivilegeViolationException;
 import javax.telephony.ProviderUnavailableException;
 import javax.telephony.ResourceUnavailableException;
 
 import net.sourceforge.gjtapi.CallId;
 import net.sourceforge.gjtapi.RawStateException;
 import net.sourceforge.gjtapi.TelephonyListener;
 import net.sourceforge.gjtapi.TermData;
 import net.sourceforge.gjtapi.capabilities.Capabilities;
 import net.sourceforge.gjtapi.raw.BasicJtapiTpi;
 
 /**
  * @author rdeadman
  *
  * This is a provider for the Asterisk open source Linux
  * PBX system.
  */
 public class AsteriskProviderDraft implements BasicJtapiTpi {
 
 	// variables
 	private Set listeners = new HashSet();
 	
 	/**
 	 * Create an instance of the provider.
 	 */
 	public AsteriskProviderDraft() {
 		super();
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sourceforge.gjtapi.raw.BasicJtapiTpi#getAddresses()
 	 */
 	public String[] getAddresses() throws ResourceUnavailableException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sourceforge.gjtapi.raw.BasicJtapiTpi#getAddresses(java.lang.String)
 	 */
 	public String[] getAddresses(String terminal)
 			throws InvalidArgumentException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sourceforge.gjtapi.raw.BasicJtapiTpi#getTerminals()
 	 */
 	public TermData[] getTerminals() throws ResourceUnavailableException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sourceforge.gjtapi.raw.BasicJtapiTpi#getTerminals(java.lang.String)
 	 */
 	public TermData[] getTerminals(String address)
 			throws InvalidArgumentException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sourceforge.gjtapi.raw.BasicJtapiTpi#release(java.lang.String, net.sourceforge.gjtapi.CallId)
 	 */
 	public void release(String address, CallId call)
 			throws PrivilegeViolationException, ResourceUnavailableException,
 			MethodNotSupportedException, RawStateException {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sourceforge.gjtapi.raw.CoreTpi#addListener(net.sourceforge.gjtapi.TelephonyListener)
 	 */
 	public void addListener(TelephonyListener ro) {
 		this.listeners.add(ro);
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sourceforge.gjtapi.raw.CoreTpi#answerCall(net.sourceforge.gjtapi.CallId, java.lang.String, java.lang.String)
 	 */
 	public void answerCall(CallId call, String address, String terminal)
 			throws PrivilegeViolationException, ResourceUnavailableException,
 			MethodNotSupportedException, RawStateException {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sourceforge.gjtapi.raw.CoreTpi#createCall(net.sourceforge.gjtapi.CallId, java.lang.String, java.lang.String, java.lang.String)
 	 */
 	public CallId createCall(CallId id, String address, String term, String dest)
 			throws ResourceUnavailableException, PrivilegeViolationException,
 			InvalidPartyException, InvalidArgumentException, RawStateException,
 			MethodNotSupportedException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sourceforge.gjtapi.raw.CoreTpi#getCapabilities()
 	 */
 	public Properties getCapabilities() {
 		Properties capabilities = new Properties();
 		// change these once we have support for throttling, media or Jcat media
 		capabilities.put(Capabilities.THROTTLE, "f");
 		capabilities.put(Capabilities.MEDIA, "f");
 		capabilities.put(Capabilities.ALL_MEDIA_TERMINALS, "f");
 		capabilities.put(Capabilities.ALLOCATE_MEDIA, "f");
 		return capabilities;
 	}
 
 	/* Here we take the map of provider parameter pairs and do
 	 * any required initialization on Asterisk, such as linking
 	 * to the appropriate machine.
 	 * @see net.sourceforge.gjtapi.raw.CoreTpi#initialize(java.util.Map)
 	 */
 	public void initialize(Map props) throws ProviderUnavailableException {
 		// look for machine IP address or name
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sourceforge.gjtapi.raw.CoreTpi#releaseCallId(net.sourceforge.gjtapi.CallId)
 	 */
 	public void releaseCallId(CallId id) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sourceforge.gjtapi.raw.CoreTpi#removeListener(net.sourceforge.gjtapi.TelephonyListener)
 	 */
 	public void removeListener(TelephonyListener ro) {
 		this.listeners.remove(ro);
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sourceforge.gjtapi.raw.CoreTpi#reserveCallId(java.lang.String)
 	 */
 	public CallId reserveCallId(String address) throws InvalidArgumentException {
 		// TODO Must change constructor as we determine how to identify the call
		return new AsteriskCallId();
 	}
 
 	/* Do any Asterisk API shutdown commands here
 	 * @see net.sourceforge.gjtapi.raw.CoreTpi#shutdown()
 	 */
 	public void shutdown() {
 		// TODO Auto-generated method stub
 
 	}
 	
 	// The callback method
 	public void callback()
 	{
 		
 	}
 
 	
 }
