 /*
  * Copyright (C) 2012 Roman Elizarov
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.avrbuddy.xbee.api;
 
 import org.avrbuddy.conn.Connection;
 import org.avrbuddy.conn.SerialConnection;
 import org.avrbuddy.log.Log;
 import org.avrbuddy.log.LoggedThread;
 import org.avrbuddy.util.State;
 
 import java.io.*;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author Roman Elizarov
  */
 public class XBeeConnection {
     private static final Logger log = Log.getLogger(XBeeConnection.class);
 
     private static final int CLOSED = 1;
 
     public static final long DEFAULT_TIMEOUT = 3000;
     public static final long BROADCAST_TIMEOUT = 1000;
 
     private final SerialConnection serial;
     private final DataInputStream in;
     private final DataOutputStream out;
     private final Thread reader;
     private final XBeeFrameListenerList listenerList = new XBeeFrameListenerList();
     private final State state = new State();
     private byte lastFrameId;
 
     private int maxPayloadSize;
 
     // -------------- PUBLIC FACTORY --------------
 
     public static XBeeConnection open(SerialConnection serial) throws IOException {
         XBeeConnection conn = new XBeeConnection(serial);
         try {
             conn.configureConnection();
         } catch (IOException e) {
             conn.close();
             throw e;
         }
         return conn;
     }
 
     // -------------- PUBLIC LOW-LEVER OPERATIONS --------------
 
     public void close() {
         if (!state.set(CLOSED))
             return;
         serial.close();
         Object[] listeners = listenerList.getListeners();
         for (int i = 0; i < listeners.length; i += 2)
             ((XBeeFrameListener) listeners[i + 1]).connectionClosed();
         synchronized (this) {
             notifyAll();
         }
     }
 
     public <F> void addListener(Class<F> frameClass, XBeeFrameListener<F> listener) {
         if (state.is(CLOSED))
             return;
         listenerList.addListener(frameClass, listener);
     }
 
     public <F> void removeListener(Class<F> frameClass, XBeeFrameListener<F> listener) {
         listenerList.removeListener(frameClass, listener);
     }
 
     public synchronized void sendFrames(XBeeFrame... frames) throws IOException {
         for (XBeeFrame frame : frames) {
             log.finer("-> " + frame);
         }
         for (XBeeFrame frame : frames) {
             sendFrameInternal(frame);
         }
     }
 
     public synchronized XBeeFrameWithId[] buildFramesWithId(XBeeFrameWithId.Builder... builders) {
         XBeeFrameWithId[] frames = new XBeeFrameWithId[builders.length];
         for (int i = 0; i < builders.length; i++) {
             frames[i] = buildFrameWithId(builders[i]);
         }
         return frames;
     }
 
     private Object lock() {
         return this;
     }
 
     public synchronized <F> void sendFramesAndWaitWithListener(long timeout,
             Class<F> frameClass, XBeeTerminatingFrameListener<F> listener, XBeeFrame... frames)
             throws IOException
     {
         addListener(frameClass, listener);
         try {
             sendFrames(frames);
             long waitTill = System.currentTimeMillis() + timeout;
             while (timeout > 0 && !listener.isTerminated()) {
                 wait(timeout);
                 timeout = waitTill - System.currentTimeMillis();
             }
         } catch (InterruptedException e) {
             throw ((InterruptedIOException) new InterruptedIOException().initCause(e));
         } finally {
             removeListener(frameClass, listener);
         }
     }
 
     public synchronized XBeeFrameWithId[] sendFramesWithIdAndWaitResponses(long timeout, final XBeeFrameWithId... frames) throws IOException {
         final XBeeFrameWithId[] responses = new XBeeFrameWithId[frames.length];
         XBeeTerminatingFrameListener<XBeeFrameWithId> listener = new XBeeTerminatingFrameListener<XBeeFrameWithId>() {
             private boolean terminated;
 
             @Override
             public boolean isTerminated() {
                 return terminated;
             }
 
             public void frameReceived(XBeeFrameWithId frame) {
                 int waitCnt = 0;
                 for (int i = 0; i < frames.length; i++) {
                     XBeeFrameWithId waitFrame = frames[i];
                     if (responses[i] == null) {
                         if (frame.isResponseFor(waitFrame))
                             responses[i] = frame;
                         else
                             waitCnt++;
                     }
                 }
                 if (waitCnt == 0)
                     terminated = true;
             }
 
             @Override
             public void connectionClosed() {
                 terminated = true;
             }
         };
         sendFramesAndWaitWithListener(timeout, XBeeFrameWithId.class, listener, frames);
         return responses;
     }
 
     public XBeeFrameWithId[] sendFramesWithIdSeriallyAndWait(long timeout, XBeeFrameWithId.Builder... builders) throws IOException {
         XBeeFrameWithId[] responses = new XBeeFrameWithId[builders.length];
         for (int i = 0; i < builders.length; i++) {
             XBeeFrameWithId[] res = sendFramesWithIdAndWaitResponses(timeout, buildFramesWithId(builders[i]));
             responses[i] = res[0];
             if (XBeeUtil.getStatus(res) != XBeeAtResponseFrame.STATUS_OK)
                 break;
         }
         return responses;
     }
 
     // -------------- HIGH-LEVER PUBLIC OPERATION --------------
 
     public Connection openTunnel(XBeeAddress destination) throws IOException {
         return new XBeeTunnel(this, destination, getMaxPayloadSize());
     }
 
     // destination == null to change destination of local node via local AT commands
     public void changeRemoteDestination(XBeeAddress destination, XBeeAddress target) throws IOException {
         log.info(String.format("Changing destination for %s to %s", destination, target));
         XBeeUtil.checkStatus(sendFramesWithIdSeriallyAndWait(DEFAULT_TIMEOUT,
                 XBeeAtFrame.newBuilder(destination)
                         .setAtCommand("DH").setData(target.getHighAddressBytes()),
                 XBeeAtFrame.newBuilder(destination)
                         .setAtCommand("DL").setData(target.getLowAddressBytes())));
     }
 
     // destination == null to query destination of local node via local AT commands
     public int queryRemoteDestination(XBeeAddress destination, final XBeeTerminatingDestinationVisitor visitor)
             throws IOException
     {
         log.info(String.format("Querying destination for %s", destination));
         final boolean broadcast = XBeeAddress.BROADCAST.equals(destination);
         final XBeeFrameWithId[] frames = buildFramesWithId(
                 XBeeAtFrame.newBuilder(destination).setAtCommand("DH"),
                 XBeeAtFrame.newBuilder(destination).setAtCommand("DL"));
         final Map<XBeeAddress, XBeeAtResponseFrame[]> responses = new HashMap<XBeeAddress, XBeeAtResponseFrame[]>();
         responses.put(destination, new XBeeAtResponseFrame[2]);
         XBeeTerminatingFrameListener<XBeeAtResponseFrame> frameListener = new XBeeTerminatingFrameListener<XBeeAtResponseFrame>() {
             private boolean terminated;
 
             @Override
             public boolean isTerminated() {
                 return terminated || visitor.isTerminated();
             }
 
             @Override
             public void frameReceived(XBeeAtResponseFrame frame) {
                 for (int i = 0; i < frames.length; i++)
                     if (frame.isResponseFor(frames[i])) {
                         XBeeAtResponseFrame[] r = responses.get(frame.getSource());
                         if (r == null)
                             responses.put(frame.getSource(), r = new XBeeAtResponseFrame[2]);
                         r[i] = frame;
                         int status = XBeeUtil.getStatus(r);
                         if (status == XBeeAtResponseFrame.STATUS_OK)
                             visitor.visitNodeDestination(frame.getSource(), getDestFromResponses(r));
                         if (status != XBeeUtil.STATUS_TIMEOUT && !broadcast)
                             terminated = true;
                     }
             }
 
             @Override
             public void connectionClosed() {
                  terminated = true;
             }
         };
         // send frames serially
         for (XBeeFrameWithId frame : frames) {
             sendFramesAndWaitWithListener(broadcast ? BROADCAST_TIMEOUT : DEFAULT_TIMEOUT,
                     XBeeAtResponseFrame.class, frameListener, frame);
         }
         return broadcast ? XBeeAtResponseFrame.STATUS_OK : XBeeUtil.getStatus(responses.get(destination));
     }
 
     // destination == null to query destination of local node via local AT commands
     public XBeeAddress queryRemoteDestination(XBeeAddress destination, int attempts) throws IOException {
         final XBeeAddress[] result = new XBeeAddress[1];
         int status;
         do {
             status = queryRemoteDestination(destination, new XBeeTerminatingDestinationVisitor() {
                 @Override
                 public boolean isTerminated() {
                     return result[0] != null;
                 }
 
                 @Override
                 public void visitNodeDestination(XBeeAddress node, XBeeAddress nodeDestination) {
                     result[0] = nodeDestination;
                 }
             });
        } while (result[0] == null && --attempts > 0);
         if (status != XBeeAtResponseFrame.STATUS_OK)
             throw new XBeeException(XBeeUtil.formatStatus(status));
         return result[0];
     }
 
     private XBeeAddress getDestFromResponses(XBeeFrameWithId[] responses) {
         return XBeeAddress.valueOf(responses[0].getData(), responses[1].getData());
     }
 
     // destination == null to reset local node via local AT commands
     public void resetRemoteHost(XBeeAddress destination) throws IOException {
         log.info("Resetting remote host " + destination);
         XBeeUtil.checkStatus(sendFramesWithIdSeriallyAndWait(DEFAULT_TIMEOUT,
                 XBeeAtFrame.newBuilder(destination).setAtCommand("D3").setData(new byte[]{4}),
                 XBeeAtFrame.newBuilder(destination).setAtCommand("D3").setData(new byte[]{0})));
     }
 
     // -------------- PRIVATE CONSTRUCTOR AND HELPER METHODS --------------
 
     private XBeeConnection(SerialConnection serial) {
         this.serial = serial;
         in = new DataInputStream(new UnescapeStream(serial.getInput()));
         out = new DataOutputStream(new EscapeStream(serial.getOutput()));
         reader = new Reader();
     }
 
     private void configureConnection() throws IOException {
         // enable inbound flow control to make sure we can receive all answers (signal RTS)
         serial.setHardwareFlowControl(SerialConnection.FLOW_CONTROL_IN);
         serial.drainInput();
         // now start reader thread to parse input
         reader.start();
         // configure API MODE
         log.fine("Configuring API mode " + (byte) 2);
         if (XBeeAtResponseFrame.STATUS_OK != XBeeUtil.getStatus(
                 sendFramesWithIdAndWaitResponses(DEFAULT_TIMEOUT, buildFramesWithId(
                         XBeeAtFrame.newBuilder().setAtCommand("AP").setData((byte) 2)))))
             throw new IOException("No valid XBee device detected. Check that XBee is configured with API firmware and baud rate");
         // enable hardware flow control - RTS & CTS
         log.fine("Configuring RTS and CTS flow control");
         if (XBeeAtResponseFrame.STATUS_OK != XBeeUtil.getStatus(
                 sendFramesWithIdAndWaitResponses(DEFAULT_TIMEOUT, buildFramesWithId(
                         XBeeAtFrame.newBuilder().setAtCommand("D6").setData((byte) 1),
                         XBeeAtFrame.newBuilder().setAtCommand("D7").setData((byte) 1)))))
             throw new IOException("Failed to enable RTS and CTS flow control on XBee");
         // enable outbound flow control
         serial.setHardwareFlowControl(SerialConnection.FLOW_CONTROL_IN | SerialConnection.FLOW_CONTROL_OUT);
     }
 
     private XBeeFrame nextFrame() throws IOException {
         while (true) {
             int skipped = 0;
             while (readByteOrEOF() != XBeeUtil.FRAME_START)
                 skipped++; // skip bytes
             if (skipped != 0)
                 log.log(Level.WARNING, "Skipped " + skipped + " bytes before start of frame");
             int length = in.readShort() & 0xffff;
             byte[] frame = new byte[length + 4];
             frame[0] = XBeeUtil.FRAME_START;
             frame[1] = (byte) (length >> 8);
             frame[2] = (byte) length;
             in.readFully(frame, 3, length + 1);
             try {
                 return XBeeFrame.parse(frame);
             } catch (IllegalArgumentException e) {
                 log.log(Level.WARNING, e.getMessage());
             }
         }
     }
 
     private byte readByteOrEOF() throws IOException {
         int b = serial.getInput().read();
         if (b < 0)
             throw new EOFException("Port is closed");
         return (byte) b;
     }
 
     private byte nextFrameId() {
         lastFrameId++;
         if (lastFrameId == 0)
             lastFrameId = 1;
         return lastFrameId;
     }
 
 
     private XBeeFrameWithId buildFrameWithId(XBeeFrameWithId.Builder builder) {
         return builder.setFrameId(nextFrameId()).build();
     }
 
     private void sendFrameInternal(XBeeFrame frame) throws IOException {
         byte[] data = frame.getFrame();
         serial.getOutput().write(data[0]);
         out.write(data, 1, data.length - 1);
         out.flush();
     }
 
     private int getMaxPayloadSize() throws IOException {
         if (maxPayloadSize != 0)
             return maxPayloadSize;
         log.fine("Querying max payload size");
         XBeeFrameWithId[] response = sendFramesWithIdAndWaitResponses(DEFAULT_TIMEOUT, buildFramesWithId(
                 XBeeAtFrame.newBuilder().setAtCommand("NP")));
         if (XBeeUtil.getStatus(response) != XBeeAtResponseFrame.STATUS_OK)
             throw new IOException("Cannot determine max payload size for XBee");
         byte[] data = response[0].getData();
         if (data.length != 2)
             throw new IOException("Unrecognized response for max payload size request");
         return maxPayloadSize = ((data[0] & 0xff) << 8) + (data[1] & 0xff);
     }
 
     @SuppressWarnings({"unchecked"})
     private void dispatch(XBeeFrame frame) {
         Object[] listeners = listenerList.getListeners();
         for (int i = 0; i < listeners.length; i += 2) {
             Class<?> frameClass = (Class<Object>) listeners[i];
             if (frameClass.isInstance(frame))
                 ((XBeeFrameListener) listeners[i + 1]).frameReceived(frame);
         }
         synchronized (this) {
             notifyAll();
         }
     }
 
     private class Reader extends LoggedThread {
         Reader() {
             super(serial.toString());
         }
 
         @Override
         public void run() {
             try {
                 while (!state.is(CLOSED)) {
                     XBeeFrame frame = nextFrame();
                     log.finer("<- " + frame);
                     dispatch(frame);
                 }
             } catch (EOFException e) {
                 // ignored, exit
             } catch (InterruptedIOException e) {
                 // ignored, exit
             } catch (Exception e) {
                 log.log(Level.SEVERE, null, e);
             }
             close();
         }
     }
 }
