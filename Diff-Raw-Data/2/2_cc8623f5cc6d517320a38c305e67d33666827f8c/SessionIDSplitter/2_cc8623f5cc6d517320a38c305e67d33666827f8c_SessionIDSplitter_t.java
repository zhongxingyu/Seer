 /*
  * $Id$
  */
 package org.xins.client;
 
 import org.xins.types.TypeValueException;
 import org.xins.util.service.TargetDescriptor;
 
 /**
  * Splitter that takes a client-side session ID and returns a server
  * identification and a server-side session ID.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.74
  */
 public interface SessionIDSplitter {
 
    /**
     * Splits the specified client-side session ID. An array is passed where
     * the result should be stored in. The array is guaranteed to have size 2.
     *
     * <p>The following is expected from this method:
     *
     * <ul>
     *    <li>the first element in the array should be set to the CRC-32 of the
     *        target API URL, as a {@link String} (see
    *        {@link TargetDescriptor#getCRC()}).
     *    <li>the second element in the array should be set to the server-side
     *        session identifier, specific to the target API (which is
     *        identified by the the CRC-32 checksum in the first element.)
     * </ul>
     *
     * <p>This method should <em>not</em> retain a reference to the
     * <code>result</code> array. If it does make changes to this array after
     * the call to this method is changed, the behaviour will be undefined.
     *
     * @param sessionID
     *    the client-side session identifier, should never be
     *    <code>null</code>.
     *
     * @param result
     *    the array to place the result in, should never be <code>null</code>
     *    and should always have <code>result.length == 2</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>sessionID == null || result == null || result.length != 2</code>.
     *
     * @throws TypeValueException
     *    if <code>sessionID</code> is not well-formatted as a client-side
     *    session identifier.
     */
    void splitSessionID(String sessionID, String[] result)
    throws IllegalArgumentException, TypeValueException;
 }
