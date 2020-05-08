 /* Copyright (c) 2006-2007 Jan S. Rellermeyer
  * Information and Communication Systems Research Group (IKS),
  * Department of Computer Science, ETH Zurich.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of ETH Zurich nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package ch.ethz.iks.r_osgi;
 
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.SocketException;
 import ch.ethz.iks.r_osgi.impl.RemoteOSGiMessageImpl;
 
 /**
  * Abstract base class for all R-OSGi Messages.
  * 
  * @author Jan S. Rellermeyer, ETH Zurich
  * @since 0.6
  */
 public abstract class RemoteOSGiMessage {
 
 	/**
 	 * type code for lease messages.
 	 */
 	public static final short LEASE = 1;
 
 	/**
 	 * type code for fetch service messages.
 	 */
 	public static final short FETCH_SERVICE = 2;
 
 	/**
 	 * type code for deliver service messages.
 	 */
 	public static final short DELIVER_SERVICE = 3;
 
 	/**
 	 * type code for deliver bundle messages.
 	 */
 	public static final short DELIVER_BUNDLE = 4;
 
 	/**
 	 * type code for invoke method messages.
 	 */
 	public static final short INVOKE_METHOD = 5;
 
 	/**
 	 * type code for method result messages.
 	 */
 	public static final short METHOD_RESULT = 6;
 
 	/**
 	 * type code for remote event messages.
 	 */
 	public static final short REMOTE_EVENT = 7;
 
 	/**
 	 * type code for time offset messages.
 	 */
 	public static final short TIME_OFFSET = 8;
 
 	/**
 	 * type code for service attribute updates.
 	 */
 	public static final short STATE_UPDATE = 9;
 
 	/**
 	 * the type code or functionID in SLP notation.
 	 */
 	protected short funcID;
 
 	/**
 	 * Get the function ID (type code) of the message.
 	 * 
 	 * @see ch.ethz.iks.r_osgi.RemoteOSGiMessageImpl#getFuncID()
 	 * @return the type code.
 	 * @since 0.6
 	 */
 	public final short getFuncID() {
 		return funcID;
 	}
 
 	/**
 	 * get the transaction ID of the message.
 	 * 
 	 * @return the xid.
 	 * @since 0.6
 	 */
 	public abstract int getXID();
 
 	/**
 	 * send the message.
 	 * 
 	 * @param output
 	 *            the object output stream.
 	 * @since 0.6
 	 */
	public abstract void send(final ObjectOutputStream output);
 
 	/**
 	 * parse a message from a <code>DataInput</code>.
 	 * 
 	 * @param input
 	 *            the <code>DataInput</code>
 	 * @return the parsed message.
 	 * @throws SocketException
 	 *             if the underlying socket is closed.
 	 * @since 0.6
 	 */
 	public static RemoteOSGiMessage parse(final ObjectInputStream input)
 			throws SocketException {
 		return RemoteOSGiMessageImpl.parse(input);
 	}
 
 	/**
 	 * rewrite the serviceURLs contained in the message for bridging.
 	 * 
 	 * @param protocol
 	 *            the protocol.
 	 * @param host
 	 *            the host name/address.
 	 * @param port
 	 *            the port.
 	 * @throws IllegalArgumentException
 	 *             if the result is an invalid SLP serviceURL.
 	 */
 	public abstract void rewrite(final String protocol, final String host,
 			final int port) throws IllegalArgumentException;
 
 }
