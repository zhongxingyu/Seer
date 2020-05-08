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
 import javax.telephony.*;
 import net.sourceforge.gjtapi.*;
 /**
 * These are the methods required by TelephonyProviders that support object "throttling" for JTAPI.
 * 
 * Throttling attempts to reduce the amount of information the service provider
 * has to report about calls by telling it when information is needed on calls.
 * So, for instance, I can tell the service provider that I want all call
 * information reported on certain addresses or terminals, so that if a call
 * exists, the GJTAPI layer will start receiving event notifications about it
 * and can track it's state.
 * 
 * If the user actively creates a call, this call object now exists in GJTAPI
 * and needs event notification when the call's state changes.
 * CoreTpi.createCall(...) implicitly says that events must be reported for
 * the call until GJTAPI says it doesn't need state information anymore
 * (which is when all handles to the Call have been dropped and the object is
 * being gc'd from the weak map), since GJTAPI has an object for the Call that
 * needs its state information synchronized. At this point, GJTAPI issues a
 * TrottleTpi.stopReportingCall(CallId) message.
 * 
 * The other time that a service provider should start reporting events on a
 * call is when TrottleTpi.getCall(CallId) is called. In this case, a Call
 * object is being re-constituted and GJTAPI needs its current state information.
 * 
 * So, basically, start reporting events on Calls when "CoreTpi.createCall(...)"
 * or "TrottleTpi.getCall(CallId)" is called and stop when
 * "ThrottleTpi.stopReportingCall(CallId)" is called.
 * 
 * Note that the CallId lifecycle may be different than the Call object, since
 * GJTAPI keeps a weak map from CallIds to Calls so that it can reconstitute
 * them when needed.
 * 
  * Creation date: (2000-10-04 13:46:43)
  * @author: Richard Deadman
  */
 public interface ThrottleTpi {
 /**
  * 
  * @return net.sourceforge.gjtapi.CallData
  * @param id net.sourceforge.gjtapi.CallId
  */
 net.sourceforge.gjtapi.CallData getCall(net.sourceforge.gjtapi.CallId id);
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
 CallData[] getCallsOnAddress(String number);
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
 CallData[] getCallsOnTerminal(String name);
 /**
  * Tell the RawProvider that Calls which reach this Address should have Call, Connection and TerminalConnections
  * events reported on them.
  * Creation date: (2000-03-07 15:17:16)
  * @author: Richard Deadman
  * @param address The name of the address to change the reporting status on.
  * @param flag true if reporting is to be enabled, false to stop events relating to the call.
  * @exception InvalidArgumentException If the address is not in my domain.
  * @exception ResourceUnavailableException If the provider could not find the reources to report the calls.
  */
 void reportCallsOnAddress(String address, boolean flag) throws InvalidArgumentException, ResourceUnavailableException;
 /**
  * Tell the RawProvider that Calls which reach this Terminal should have Call, Connection and TerminalConnections
  * events reported on them.
  * Creation date: (2000-03-07 15:17:16)
  * @author: Richard Deadman
  * @param terminal The name of the terminal to change the reporting status on.
  * @param flag true if reporting is to be enabled, false to stop events relating to the call.
  * @exception InvalidArgumentException If the terminal is not in my domain.
  * @exception ResourceUnavailableException If the provider could not find the reources to report the calls.
  */
 void reportCallsOnTerminal(String terminal, boolean flag) throws InvalidArgumentException, ResourceUnavailableException;
 /**
  * Tell the RawProvider that a certain Call no longer needs to have Call, Connection and
  * TerminalConnections events reported.
  * Creation date: (2000-03-07 15:17:16)
  * @author: Richard Deadman
  * @return true if the call is known, false otherwise
  * @param call The handle on the call to stop reporting on.
  */
 boolean stopReportingCall(CallId call);
 }
