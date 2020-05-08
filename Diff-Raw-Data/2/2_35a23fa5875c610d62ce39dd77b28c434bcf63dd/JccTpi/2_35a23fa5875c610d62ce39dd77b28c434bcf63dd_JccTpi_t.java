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
 /**
  * This includes all methods that are required by any Jcc implementation.
  * AttachMedia() and beep() are ignored if
  * JccTpi is implemented.  GetAddressType and getDialledDigits() returns null.
  * SetLoadControl() will MethodNotSupportedException.
  * Creation date: (2000-10-04 13:59:27)
  * @author: Richard Deadman
  */
public interface JccTpi extends CoreTpi {
 /**
  * Attach or detach media streams from the logical connection.
  * Creation date: (2000-11-01 15:47:06)
  * @return true if the request succeeded/
  * @param call The reference to the logical call
  * @param address The name of the address that identifies the connection
  * @param onFlag True if we should attach media; false to detach
  */
 boolean attachMedia(CallId call, String address, boolean onFlag);
 /**
  * 
  * @param call net.sourceforge.gjtapi.CallId
  */
 void beep(net.sourceforge.gjtapi.CallId call);
 /**
  * Query the Telephony provider for the type of the address based on JccAddress statics.
  * Creation date: (2000-11-01 15:42:43)
  * @return Denote whether it is an IP address or a telephone address with a
 	particular numbering scheme.
  * @param name The unique name for the address.
  */
 int getAddressType(String name);
 /**
  * 
  * @return java.lang.String
  * @param id net.sourceforge.gjtapi.CallId
  * @param address java.lang.String
  */
 java.lang.String getDialledDigits(net.sourceforge.gjtapi.CallId id, java.lang.String address);
 /**
  * This method imposes or removes load control on calls made to the specified addresses.
 The implementation can throw the MethodNotSupportedException if the platform does not support the load control
 functionality. Note that a policy object may be designed to define the policy to be implemented by the platform as a result
 of this method instead of defining the policy through the given parameters. This might be designed in the future
 specifications.
  * <P>Note that currently the Jcc specification does not define "treatment".
  * Creation date: (2000-11-10 12:26:54)
  * @param startAddr the lower address of the range
  * @param endAddr the upper address of the range, or null if we are specifying a single address
  * @param duration specifies the duration in milliseconds for which the load control should be set. Duration of 0 indicates that the load control should be removed. Duration of -1 indicates an infinite duration (i.e until disabled by the application). Duration of -2 indicates network default duration.
  * @param admissionRate the call admission rate of the call load control mechanism used.
  * @param interval the type of call load control mechanism to use, the interval (in milliseconds) between calls that are admitted.
  * @param treatment specifies the treatment of the calls that are not admitted.The contents of this parameter are ignored if the load control duration is set to zero.
  * @exception javax.telephony.MethodNotSupportedException The exception description.
  */
 void setLoadControl(String startAddr, String endAddr, double duration, double admissionRate, double interval, int[] treatment) throws javax.telephony.MethodNotSupportedException;
 }
