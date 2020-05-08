 /**
  * Copyright 2012 Tejeswar Das
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package net.dovemq.transport.session;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.atomic.AtomicLong;
 
 import net.dovemq.transport.connection.CAMQPConnectionInterface;
 import net.dovemq.transport.connection.CAMQPIncomingChannelHandler;
 import net.dovemq.transport.frame.CAMQPFrame;
 import net.dovemq.transport.frame.CAMQPFrameConstants;
 import net.dovemq.transport.frame.CAMQPFrameHeader;
 import net.dovemq.transport.frame.CAMQPMessagePayload;
 import net.dovemq.transport.link.CAMQPLinkMessageHandler;
 import net.dovemq.transport.link.CAMQPLinkSenderInterface;
 import net.dovemq.transport.link.LinkRole;
 import net.dovemq.transport.protocol.CAMQPEncoder;
 import net.dovemq.transport.protocol.CAMQPSyncDecoder;
 import net.dovemq.transport.protocol.data.CAMQPControlAttach;
 import net.dovemq.transport.protocol.data.CAMQPControlBegin;
 import net.dovemq.transport.protocol.data.CAMQPControlDetach;
 import net.dovemq.transport.protocol.data.CAMQPControlDisposition;
 import net.dovemq.transport.protocol.data.CAMQPControlEnd;
 import net.dovemq.transport.protocol.data.CAMQPControlFlow;
 import net.dovemq.transport.protocol.data.CAMQPControlTransfer;
 import net.dovemq.transport.protocol.data.CAMQPDefinitionDeliveryState;
 import net.dovemq.transport.protocol.data.CAMQPDefinitionError;
 import net.jcip.annotations.GuardedBy;
 import net.jcip.annotations.Immutable;
 import net.jcip.annotations.ThreadSafe;
 
 import org.apache.log4j.Logger;
 import org.jboss.netty.buffer.ChannelBuffer;
 
 /**
  * AMQP Session implementation
  * @author tejdas
  */
 @ThreadSafe
 class CAMQPSession implements CAMQPIncomingChannelHandler, CAMQPSessionInterface, Runnable
 {
     @Immutable
     private static class Transfer
     {
         Transfer(CAMQPControlTransfer transferFrame, CAMQPMessagePayload payload, CAMQPLinkSenderInterface linkSender)
         {
             super();
             this.transferFrame = transferFrame;
             this.payload = payload;
             this.linkSender = linkSender;
         }
         final CAMQPControlTransfer transferFrame;
         final CAMQPMessagePayload payload;
         final CAMQPLinkSenderInterface linkSender;
     }
 
     @Immutable
     static class CAMQPChannel
     {
         final CAMQPConnectionInterface getAmqpConnection()
         {
             return amqpConnection;
         }
         final int getChannelId()
         {
             return channelId;
         }
         CAMQPChannel(CAMQPConnectionInterface amqpConnection, int channelId)
         {
             super();
             this.amqpConnection = amqpConnection;
             this.channelId = channelId;
         }
         private final CAMQPConnectionInterface amqpConnection;
         private final int channelId;
     }
 
     private static final Logger log = Logger.getLogger(CAMQPSession.class);
 
     /*
      * The following attributes are used to schedule a recurring send of
      * flow-frame until the session is not under flow-control state any longer.
      */
     @GuardedBy("this")
     private Date lastFlowSent = new Date();
     @GuardedBy("this")
     private boolean isFlowSendScheduled = false;
 
     private final Map<Long, CAMQPLinkMessageHandler> linkReceivers = new ConcurrentHashMap<Long, CAMQPLinkMessageHandler>();
 
     private final CAMQPSessionStateActor stateActor;
     @GuardedBy("stateActor")
     private boolean closePending = false;
 
     /*
      * AMQP Connection/channel state
      */
     @GuardedBy("stateActor")
     private int outgoingChannelNumber = 0;
     int getOutgoingChannelNumber()
     {
         synchronized (stateActor)
         {
             return outgoingChannelNumber;
         }
     }
     @GuardedBy("stateActor")
     private int incomingChannelNumber = 0;
 
     private volatile CAMQPConnectionInterface connection = null;
 
     private volatile CAMQPDispositionSender dispositionSender = null;
 
     CAMQPConnectionInterface getConnection()
     {
         return connection;
     }
 
     /*
      * I/O state
      */
     private final AtomicLong deliveryId = new AtomicLong(0L);
 
     @GuardedBy("this")
     private boolean sendInProgress = false;
     @GuardedBy("this")
     private ConcurrentLinkedQueue<Transfer> unsentTransfers = new ConcurrentLinkedQueue<Transfer>();
 
     /*
      * Flow control state
      */
     @GuardedBy("this")
     private long nextOutgoingTransferId = 0;
     @GuardedBy("this")
     private long outgoingWindow;
     @GuardedBy("this")
     private long incomingWindow;
 
     /*
      * computed after begin, transfer and flow frames received from peer
      */
     @GuardedBy("this")
     private long remoteOutgoingWindow = -1;
     @GuardedBy("this")
     private long nextIncomingTransferId = -1;
     @GuardedBy("this")
     private long remoteIncomingWindow = -1;
 
     private final String sessionId;
     String getSessionId()
     {
         return sessionId;
     }
 
     /*
      * Called during passive session attach
      */
     CAMQPSession(CAMQPConnectionInterface connection, CAMQPSessionStateActor stateActor)
     {
         super();
         this.connection = connection;
         this.stateActor = stateActor;
         outgoingWindow = CAMQPSessionManager.getMaxOutgoingWindowSize();
         incomingWindow = CAMQPSessionManager.getMaxIncomingWindowSize();
         sessionId = UUID.randomUUID().toString();
     }
 
     /*
      * Called during active session attach
      */
     CAMQPSession(CAMQPConnectionInterface connection)
     {
         super();
         this.connection = connection;
         stateActor = new CAMQPSessionStateActor(this);
         outgoingWindow = CAMQPSessionManager.getMaxOutgoingWindowSize();
         incomingWindow = CAMQPSessionManager.getMaxIncomingWindowSize();
         sessionId = UUID.randomUUID().toString();
     }
 
     public void transferExecuteFailed(long failedTransferId)
     {
         CAMQPDefinitionError error = new CAMQPDefinitionError();
         error.setDescription("failed to execute"); // TODO
         closeInternal(error);
     }
 
     /**
      * Called by AMQP session initiator to send a BEGIN frame,
      * and wait for the BEGIN frame from the AMQP peer, after
      * which it transitions to MAPPED state.
      *
      */
     void open()
     {
         /*
          * In the case of session initiator, a. reserve a txChannel from
          * underlying CAMQPConnectionInterface b. register with CAMQPSessionFrameHandler
          * that will then dispatch the incoming BEGIN control. The BEGIN
          * control cannot be directly dispatched to the session because the
          * session's rxChannel is not known yet, and hence not registered with
          * the CAMQPConnectionInterface.
          */
         outgoingChannelNumber = connection.reserveOutgoingChannel();
         connection.registerSessionHandshakeInProgress(outgoingChannelNumber, this);
 
         CAMQPControlBegin beginControl = new CAMQPControlBegin();
 
         setFlowControlAttributes(beginControl);
         CAMQPSessionControlWrapper beginContext = new CAMQPSessionControlWrapper(outgoingChannelNumber, beginControl);
 
         stateActor.sendBegin(beginContext);
         stateActor.waitForMapped();
     }
 
     private synchronized void setFlowControlAttributes(CAMQPControlBegin beginControl)
     {
         beginControl.setIncomingWindow(incomingWindow);
         beginControl.setOutgoingWindow(outgoingWindow);
         beginControl.setNextOutgoingId(nextOutgoingTransferId);
     }
 
     /**
      * Called after the Session accepter receives BEGIN frame.
      * It responds by sending a BEGIN frame to the initiator,
      * and transitions to MAPPED state
      * @param receivedData
      */
     void beginReceived(CAMQPSessionControlWrapper receivedData)
     {
         /*
          * In the case of session initiator's peer:
          * a. Get the rxChannel from FrameHeader and register with underlying CAMQPConnectionInterface
          * b. reserve a txChannel from underlying CAMQPConnectionInterface
          */
         incomingChannelNumber = receivedData.getChannelNumber();
         outgoingChannelNumber = connection.reserveOutgoingChannel();
         connection.register(incomingChannelNumber, this);
 
         CAMQPControlBegin peerBeginControl = (CAMQPControlBegin) receivedData.getSessionControl();
         retrieveRemoteFlowControlAttributes(peerBeginControl);
 
         CAMQPControlBegin beginControl = new CAMQPControlBegin();
         beginControl.setRemoteChannel(incomingChannelNumber);
         setFlowControlAttributes(beginControl);
         CAMQPSessionControlWrapper beginContext = new CAMQPSessionControlWrapper(outgoingChannelNumber, beginControl);
 
         stateActor.sendBegin(beginContext);
     }
 
     private synchronized void retrieveRemoteFlowControlAttributes(CAMQPControlBegin peerBeginControl)
     {
         retrieveAndSetRemoteFlowControlAttributes(peerBeginControl.getOutgoingWindow(),
                 peerBeginControl.getNextOutgoingId(), peerBeginControl.getIncomingWindow());
     }
 
     @GuardedBy("this")
     void retrieveAndSetRemoteFlowControlAttributes(long remoteOutgoingWindow, long nextIncomingTransferId, long remoteIncomingWindow)
     {
         this.remoteOutgoingWindow = remoteOutgoingWindow;
         this.nextIncomingTransferId = nextIncomingTransferId;
         this.remoteIncomingWindow = remoteIncomingWindow;
     }
 
     /*
      * Begin received by the Session initiator
      */
     void beginResponse(CAMQPControlBegin peerBeginControl, CAMQPFrameHeader frameHeader)
     {
         /*
          * In the case of session initiator: Get the rxChannel from FrameHeader
          * and register with underlying CAMQPConnectionInterface
          */
         incomingChannelNumber = frameHeader.getChannelNumber();
         retrieveRemoteFlowControlAttributes(peerBeginControl);
 
         connection.register(incomingChannelNumber, this);
         CAMQPSessionControlWrapper beginContext = new CAMQPSessionControlWrapper(outgoingChannelNumber, peerBeginControl);
         stateActor.beginReceived(beginContext);
     }
 
     void mapped()
     {
         CAMQPSessionManager.sessionCreated(connection.getKey(), outgoingChannelNumber, this);
         String logInfo = String.format("Session is attached to txChannel: %d and rxChannel: %d", outgoingChannelNumber, incomingChannelNumber);
         log.info(logInfo);
         dispositionSender = new CAMQPDispositionSender(this);
         dispositionSender.start();
         CAMQPSessionManager.getSessionSendFlowScheduler().registerSession(sessionId, this);
     }
 
     /**
      * Close the AMQP session
      */
     @Override
     public void close()
     {
         closeInternal(null);
     }
 
     private void closeInternal(CAMQPDefinitionError error)
     {
         synchronized (stateActor)
         {
             if (!isSessionActive())
             {
                 log.info("Session already closed or closing in progress");
                 return;
             }
 
             closePending = true;
         }
 
         CAMQPControlEnd endControl = new CAMQPControlEnd();
         if (error != null)
         {
             endControl.setError(error);
         }
         CAMQPSessionControlWrapper endContext = new CAMQPSessionControlWrapper(outgoingChannelNumber, endControl);
         stateActor.sendEnd(endContext);
         stateActor.waitForUnmapped();
     }
 
     void endReceived(CAMQPControlEnd receivedData)
     {
         CAMQPControlEnd endControl = new CAMQPControlEnd();
         CAMQPSessionControlWrapper endContext = new CAMQPSessionControlWrapper(outgoingChannelNumber, endControl);
         stateActor.sendEnd(endContext);
     }
 
     void unmapped()
     {
         CAMQPSessionManager.sessionClosed(connection.getKey(), this, outgoingChannelNumber);
         dispositionSender.stop();
         CAMQPSessionManager.getSessionSendFlowScheduler().unregisterSession(sessionId);
 
         connection.detach(outgoingChannelNumber, incomingChannelNumber);
         synchronized (stateActor)
         {
             connection = null;
             outgoingChannelNumber = 0;
             incomingChannelNumber = 0;
         }
 
         for (Long linkHandle : linkReceivers.keySet())
         {
             CAMQPLinkMessageHandler linkReceiver = linkReceivers.get(linkHandle);
             if (linkReceiver != null)
                 linkReceiver.sessionClosed();
         }
     }
 
     @Override
     public void channelAbruptlyDetached()
     {
        System.out.println("Session abruptly closed");
        log.warn("Session abrupt closed");
         stateActor.channelAbruptlyDetached();
     }
 
     @Override
     public void sendLinkControlFrame(ChannelBuffer encodedLinkControlFrame)
     {
         CAMQPChannel channel = getChannel();
         if (channel != null)
         {
             channel.getAmqpConnection().sendFrame(encodedLinkControlFrame, channel.getChannelId());
         }
     }
 
     /**
      * Called by Link layer to get the delivery Id for the next
      * transfer frame.
      */
     @Override
     public long getNextDeliveryId()
     {
         return deliveryId.getAndIncrement();
     }
 
     @GuardedBy("this")
     private boolean canSendTransfer()
     {
         return (!unsentTransfers.isEmpty()) && (remoteIncomingWindow > 0);
     }
 
     /**
      * Sends the transfer frame to the peer AMQP session.
      *
      * If the sending transfer frames is in progress by another thread
      * the calling thread enqueues the transfer frame (to be picked up
      * by the sender thread) and returns.
      *
      * If the remote session's incoming window is closed, the calling
      * thread just enqueues the transfer frame and returns.
      *
      * Otherwise, the calling thread sends the transfer frame and decrements the
      * remote session's incoming window size.
      *
      * If the remoteIncomingWindow falls below a configurable threshold,
      * the calling thread sends a flow-frame (with echo), notifying the peer
      * to send a flow response with an updated remoteIncomingWindow, if possible.
      *
      * Finally, if there are outstanding transfer frames waiting to be sent,
      * the called thread enqueues a runnable to send the transfer frames.
      */
     @Override
     public void sendTransfer(CAMQPControlTransfer transferFrame, CAMQPMessagePayload payload, CAMQPLinkSenderInterface linkSender)
     {
         CAMQPChannel channel = getChannel();
         if (channel == null)
         {
             throw new CAMQPSessionClosedException("Underlying channel is detached");
         }
 
         /*
          * TODO: if unsentTransfers.add() fails, throw an exception
          * indicating to the Link layer that the outgoing-window is full.
          */
         synchronized (this)
         {
             if (!unsentTransfers.isEmpty())
             {
                 assert(sendInProgress || (remoteIncomingWindow <= 0));
                 unsentTransfers.add(new Transfer(transferFrame, payload, linkSender));
                 return;
             }
 
             if (sendInProgress || (remoteIncomingWindow <= 0))
             {
                 unsentTransfers.add(new Transfer(transferFrame, payload, linkSender));
                 return;
             }
 
             sendInProgress = true;
             nextOutgoingTransferId++;
             /*
              * Decrement remoteIncomingWindow and if has fallen below
              * MIN_INCOMING_WINDOW_SIZE_THRESHOLD, send a flow frame
              * asking the peer to send an updated remoteIncomingWindow.
              */
             remoteIncomingWindow--;
             if (remoteIncomingWindow < CAMQPSessionConstants.MIN_INCOMING_WINDOW_SIZE_THRESHOLD)
             {
                 isFlowSendScheduled = true;
             }
         }
 
         sendTransferFrame(transferFrame, payload, channel);
         /*
          * Notify the link layer that the transfer frame has been sent
          */
         linkSender.messageSent(transferFrame);
 
         synchronized (this)
         {
             /*
              * There are more transfer frames in the outgoing window waiting
              * to be sent. Schedule a runnable to send the transfer frames
              * only if we are not under flow control
              */
             if (!canSendTransfer())
             {
                 sendInProgress = false;
                 return;
             }
         }
 
         CAMQPSessionManager.getExecutor().execute(this);
     }
 
     /**
      * Called by Link Layer to send a flow frame
      */
     @Override
     public void sendFlow(CAMQPControlFlow flow)
     {
         CAMQPChannel channel = getChannel();
         if (channel != null)
         {
             synchronized (this)
             {
                 lastFlowSent = new Date();
                 populateFlowFrame(flow);
             }
             sendFlowFrame(flow, channel);
         }
     }
 
     /**
      * Flow frame is sent in the following cases:
      *
      * From Link layer: sendFlow() : send right away : do not schedule a flow re-send.
      *
      * From session layer: in response to peer's flow frame (echo flag true) : send right away : do not schedule a flow re-send.
      *
      * From session layer: after detecting a flow control: send only if the last send was done before FLOW_SENDER_INTERVAL. Also
      * schedule for a flow-frame send after FLOW_SENDER_INTERVAL.
      *
      * From session layer : a scheduled timer :
      *      If a flow control frame has been sent in the past within FLOW_SENDER_INTERVAL,  do not send flow control frame, but schedule
      *      another one in the future after time left.
      *      If it is not under flow control anymore, do not send flow control.
             Otherwise send and schedule for a flow-frame send after FLOW_SENDER_INTERVAL.
      *
      * Do NOT schedule sending of another flow-frame, if acting as a Session Receiver.
      */
     void requestOrProvideCreditIfUnderFlowControl(Date now)
     {
         CAMQPChannel channel = getChannel();
         if (channel == null)
         {
             return;
         }
         CAMQPControlFlow flow = null;
 
         synchronized (this)
         {
             if (!isFlowSendScheduled)
             {
                 return;
             }
 
             if (!isSenderUnderFlowControl() && !isRemoteSenderUnderFlowControl())
             {
                 /*
                  * Neither SessionSender or peer SessionSender is not under flow-control.
                  * so no need to send flow frame.
                  */
                 isFlowSendScheduled = false;
                 return;
             }
 
             if ((now.getTime() - lastFlowSent.getTime()) >= CAMQPSessionConstants.FLOW_SENDER_INTERVAL)
             {
                 flow = new CAMQPControlFlow();
                 populateFlowFrame(flow);
                 flow.setEcho(true);
                 lastFlowSent = now;
             }
         }
 
         if (flow != null)
         {
             sendFlowFrame(flow, channel);
         }
     }
 
     /**
      * If there are outstanding transfer frames to be sent, but the remoteIncomingWindow falls below
      * a threshold, we want to proactively send a flow frame, to get an updated remoteIncomingWindow
      * @return
      */
     @GuardedBy("this")
     private boolean isSenderUnderFlowControl()
     {
         return (!unsentTransfers.isEmpty() && remoteIncomingWindow < CAMQPSessionConstants.MIN_INCOMING_WINDOW_SIZE_THRESHOLD);
     }
 
     @GuardedBy("this")
     private boolean isRemoteSenderUnderFlowControl()
     {
         return ((incomingWindow < CAMQPSessionConstants.MIN_INCOMING_WINDOW_SIZE_THRESHOLD) ||
                 (remoteOutgoingWindow < CAMQPSessionConstants.MIN_INCOMING_WINDOW_SIZE_THRESHOLD));
     }
 
     /**
      * If the caller of sendTransfer() finds outstanding transfer frames to be sent,
      * it submits a runnable that acts as a transfer frame sender, and sends the
      * outstanding transfer frames.
      */
     @Override
     public void run()
     {
         while (true)
         {
             Transfer transfer = null;
             CAMQPChannel channel = getChannel();
 
             synchronized (this)
             {
                 if ((channel != null) && canSendTransfer())
                 {
                     /*
                      * Still outstanding unsent transfer frames
                      * and remoteIncomingWindow is open
                      */
                     transfer = unsentTransfers.poll();
                     nextOutgoingTransferId++;
                     /*
                      * Decrement remoteIncomingWindow and if has fallen below
                      * MIN_INCOMING_WINDOW_SIZE_THRESHOLD, send a flow frame
                      * asking the peer to send an updated remoteIncomingWindow.
                      */
                     remoteIncomingWindow--;
                     if (isSenderUnderFlowControl())
                     {
                         isFlowSendScheduled = true;
                     }
                 }
                 else
                 {
                     sendInProgress = false;
                     return;
                 }
             }
 
             /*
              * Send the transfer frame and notify link layer
              */
             sendTransferFrame(transfer.transferFrame, transfer.payload, channel);
             transfer.linkSender.messageSent(transfer.transferFrame);
         }
     }
 
     @GuardedBy("this")
     private void populateFlowFrame(CAMQPControlFlow flow)
     {
         flow.setOutgoingWindow(outgoingWindow);
         flow.setIncomingWindow(incomingWindow);
         flow.setNextOutgoingId(nextOutgoingTransferId);
         flow.setNextIncomingId(nextIncomingTransferId);
     }
 
     private static void sendFlowFrame(CAMQPControlFlow flow, CAMQPChannel channel)
     {
         CAMQPEncoder encoder = CAMQPEncoder.createCAMQPEncoder();
         CAMQPControlFlow.encode(encoder, flow);
         ChannelBuffer encodedTransfer = encoder.getEncodedBuffer();
         channel.getAmqpConnection().sendFrame(encodedTransfer, channel.getChannelId());
     }
 
     private static void sendTransferFrame(CAMQPControlTransfer transfer, CAMQPMessagePayload payload, CAMQPChannel channel)
     {
         CAMQPEncoder encoder = CAMQPEncoder.createCAMQPEncoder();
         CAMQPControlTransfer.encode(encoder, transfer);
         encoder.writePayload(payload);
         ChannelBuffer encodedTransfer = encoder.getEncodedBuffer();
         channel.getAmqpConnection().sendFrame(encodedTransfer, channel.getChannelId());
     }
 
     CAMQPChannel getChannel()
     {
         synchronized (stateActor)
         {
             if (isSessionActive())
             {
                 return new CAMQPChannel(connection, outgoingChannelNumber);
             }
             return null;
         }
     }
 
     @GuardedBy("stateActor")
     private boolean isSessionActive()
     {
         return ((connection != null) && (!closePending) && (stateActor.getCurrentState() == State.MAPPED));
     }
 
     /**
      * Decodes and dispatches the incoming frame
      */
     @Override
     public void frameReceived(CAMQPFrame frame)
     {
         if (frame.getHeader().getFrameType() == CAMQPFrameConstants.AMQP_FRAME_TYPE)
         {
             CAMQPSyncDecoder decoder = CAMQPSyncDecoder.createCAMQPSyncDecoder();
             decoder.take(frame.getBody());
             String controlName = decoder.readSymbol();
             if (isSessionControlFrame(controlName))
             {
                 processSessionControlFrame(controlName, frame);
             }
             else if (isLinkControlFrame(controlName))
             {
                 processLinkControlFrame(controlName, frame);
             }
             else if (isFlowFrame(controlName))
             {
                 processFlowFrame(frame);
             }
             else if (isTransferFrame(controlName))
             {
                 processTransferFrame(controlName, frame);
             }
         }
     }
 
     private static boolean isSessionControlFrame(String controlName)
     {
         return ((controlName.equalsIgnoreCase(CAMQPControlBegin.descriptor)) || (controlName.equalsIgnoreCase(CAMQPControlEnd.descriptor)));
     }
 
     private static boolean isLinkControlFrame(String controlName)
     {
         return ((controlName.equalsIgnoreCase(CAMQPControlAttach.descriptor)) ||
                 (controlName.equalsIgnoreCase(CAMQPControlDetach.descriptor)) ||
                 (controlName.equalsIgnoreCase(CAMQPControlDisposition.descriptor)));
     }
 
     private static boolean isFlowFrame(String controlName)
     {
         return (controlName.equalsIgnoreCase(CAMQPControlFlow.descriptor));
     }
 
     private static boolean isTransferFrame(String controlName)
     {
         return (controlName.equalsIgnoreCase(CAMQPControlTransfer.descriptor));
     }
 
     /**
      * Processes an incoming flow-frame. Updates the flow-control attributes.
      * If the remoteIncomingWindow opens up (becomes non-zero), and there
      * are outstanding unsent transfer frames because of flow-control, it creates
      * a Runnable to send the unsent transfer frames.
      *
      * @param frame
      */
     private void processFlowFrame(CAMQPFrame frame)
     {
         ChannelBuffer body = frame.getBody();
         if (body == null)
         {
             return;
         }
         CAMQPSyncDecoder decoder = CAMQPSyncDecoder.createCAMQPSyncDecoder();
         decoder.take(body);
         CAMQPControlFlow flowFrame = CAMQPControlFlow.decode(decoder);
 
         boolean unsentTransferFramesPending = false;
         CAMQPControlFlow echoedFlowFrame = null;
 
         CAMQPChannel channel = (needFlowFrameEcho(flowFrame))? getChannel() : null;
         synchronized (this)
         {
             if (flowFrame.getNextOutgoingId() > nextIncomingTransferId)
                 nextIncomingTransferId = flowFrame.getNextOutgoingId();
 
             /*
              * update remote outgoing and incoming windows
              */
             remoteOutgoingWindow = flowFrame.getOutgoingWindow();
 
             remoteIncomingWindow =
                 flowFrame.getIncomingWindow() - (nextOutgoingTransferId - flowFrame.getNextIncomingId());
 
             if (!sendInProgress && canSendTransfer())
             {
                 /*
                  * If the remote incoming window opens up and we have outstanding transfer
                  * frames waiting to be sent, then start sending.
                  */
                 sendInProgress = true;
                 unsentTransferFramesPending = true;
             }
 
             /*
              * Send back a echo Flow frame if it is a session Flow only.
              * Otherwise, let the Link layer send it.
              */
             if (channel != null)
             {
                 lastFlowSent = new Date();
                 echoedFlowFrame = new CAMQPControlFlow();
                 populateFlowFrame(echoedFlowFrame);
                 echoedFlowFrame.setEcho(false); // we do not want echo flow frame ping-pong
             }
         }
 
         if (echoedFlowFrame != null)
         {
             sendFlowFrame(echoedFlowFrame, channel);
         }
 
         /*
          * Dispatch the flow frame to link layer, if the link handle is set.
          */
         if (flowFrame.isSetHandle())
         {
             CAMQPLinkMessageHandler linkReceiver = linkReceivers.get(flowFrame.getHandle());
             if (linkReceiver != null)
                 linkReceiver.flowReceived(flowFrame);
         }
 
         if (unsentTransferFramesPending)
         {
             CAMQPSessionManager.getExecutor().execute(this);
         }
     }
 
     /**
      * We need to send a flow-frame in response to an incoming flow-frame if
      * the incoming flow-frame has echo flag set to true, and it is not targeted
      * for the link layer.
      *
      * @param flowFrame
      * @return
      */
     private boolean needFlowFrameEcho(CAMQPControlFlow flowFrame)
     {
         return (flowFrame.isSetEcho() && flowFrame.getEcho() && (!flowFrame.isSetHandle()));
     }
 
     /**
      * Processes an incoming transfer frame (AMQP session receiver)
      *
      * @param controlName
      * @param frame
      */
     private void processTransferFrame(String controlName, CAMQPFrame frame)
     {
         ChannelBuffer body = frame.getBody();
         if (body == null)
         {
             return;
         }
         CAMQPSyncDecoder decoder = CAMQPSyncDecoder.createCAMQPSyncDecoder();
         decoder.take(body);
         CAMQPControlTransfer transferFrame = CAMQPControlTransfer.decode(decoder);
         CAMQPMessagePayload payload = decoder.getPayload();
 
         synchronized (this)
         {
             if (incomingWindow <= 0)
             {
                 /*
                  * TODO
                  * Peer is not honoring the session flow control.
                  * Reject the incoming transfer frame and close the session
                  */
                 log.warn("CAMQPSession.processTransferFrame(): received transfer frames with incomingWindow :" + incomingWindow);
             }
             else
             {
                 nextIncomingTransferId++;
                 remoteOutgoingWindow--;
                 incomingWindow--;
             }
 
             /*
              * Schedule sending of a flow-frame, if remote sender is under flow-control.
              */
             if (isRemoteSenderUnderFlowControl())
             {
                 isFlowSendScheduled = true;
             }
         }
 
         /*
          * dispatch the transfer frame to a LinkReceiver
          */
         CAMQPLinkMessageHandler linkReceiver = linkReceivers.get(transferFrame.getHandle());
         if (linkReceiver != null)
             linkReceiver.transferReceived(transferFrame.getDeliveryId(), transferFrame, payload);
 
         return;
     }
 
     /**
      * Processes Link control frames (attach and detach)
      * @param controlName
      * @param frame
      */
     private void processLinkControlFrame(String controlName, CAMQPFrame frame)
     {
         ChannelBuffer body = frame.getBody();
         if (body == null)
         {
             return;
         }
 
         CAMQPLinkMessageHandler linkReceiver = null;
         CAMQPSyncDecoder decoder = CAMQPSyncDecoder.createCAMQPSyncDecoder();
         decoder.take(body);
         if (controlName.equalsIgnoreCase(CAMQPControlAttach.descriptor))
         {
             CAMQPControlAttach data = CAMQPControlAttach.decode(decoder);
             linkReceiver = CAMQPSessionManager.getLinkReceiverFactory().linkAccepted(this, data);
             linkReceivers.put(data.getHandle(), linkReceiver);
             linkReceiver.attachReceived(data);
         }
         else if (controlName.equalsIgnoreCase(CAMQPControlDetach.descriptor))
         {
             CAMQPControlDetach data = CAMQPControlDetach.decode(decoder);
             linkReceiver = linkReceivers.get(data.getHandle());
             if (linkReceiver != null)
             {
                 linkReceiver.detachReceived(data);
             }
         }
         else if (controlName.equalsIgnoreCase(CAMQPControlDisposition.descriptor))
         {
             dispatchDispositionFrame(decoder);
          }
     }
 
     /**
      * Processes incoming disposition frame, and dispatch it to Link layer. The
      * DispositionRange could contain transferIds means for different link
      * end-points. So, it first builds a collection of the transferIds. Then it
      * iterates through all the registered link end-points (with the same role)
      * passing the collection. Each link end-point processes the transferIds
      * that originated from it, and removes it from the collection and returns
      * the collection back to this method.
      *
      * @param decoder
      */
     private void dispatchDispositionFrame(CAMQPSyncDecoder decoder)
     {
         CAMQPControlDisposition data = CAMQPControlDisposition.decode(decoder);
         Collection<Long> disposedIds = new LinkedList<Long>();
 
         /*
          * Read the role, outcome, range and settled flag from the disposition
          * frame.
          */
         Object newState = data.getState();
         Object outcome = null;
         if ((newState != null) && (newState instanceof CAMQPDefinitionDeliveryState))
         {
             CAMQPDefinitionDeliveryState deliveryState = (CAMQPDefinitionDeliveryState) newState;
             outcome = deliveryState.getOutcome();
         }
         boolean isMessageSettledByPeer = data.isSetSettled()? data.getSettled() : false;
         boolean role = data.getRole();
         long firstDisposedId = data.getFirst();
         long lastDisposedId = data.isSetLast()? data.getLast() : data.getFirst();
 
         for (long disposedId = firstDisposedId; disposedId <= lastDisposedId; disposedId++)
         {
             disposedIds.add(disposedId);
         }
 
         LinkRole expectedRole = role? LinkRole.LinkSender : LinkRole.LinkReceiver;
         Set<Long> linkReceiverKeys = linkReceivers.keySet();
         for (long linkReceiverKey : linkReceiverKeys)
         {
             if (disposedIds.isEmpty())
             {
                 break;
             }
             CAMQPLinkMessageHandler linkReceiver = linkReceivers.get(linkReceiverKey);
             if ((linkReceiver != null) && linkReceiver.getRole() == expectedRole)
             {
                 /*
                  * Pass the dispositionIds collection to the link endpoint, and
                  * get back the collection containing the unprocessed Ids.
                  */
                 disposedIds = linkReceiver.dispositionReceived(disposedIds, isMessageSettledByPeer, outcome);
             }
         }
     }
 
     private void processSessionControlFrame(String controlName, CAMQPFrame frame)
     {
         CAMQPFrameHeader frameHeader = frame.getHeader();
         ChannelBuffer body = frame.getBody();
 
         if (body == null)
         {
             return;
         }
         incomingChannelNumber = frameHeader.getChannelNumber();
         CAMQPSyncDecoder decoder = CAMQPSyncDecoder.createCAMQPSyncDecoder();
         decoder.take(body);
         if (controlName.equalsIgnoreCase(CAMQPControlEnd.descriptor))
         {
             CAMQPControlEnd data = CAMQPControlEnd.decode(decoder);
             stateActor.endReceived(data);
         }
     }
 
     @Override
     public void registerLinkReceiver(Long linkHandle, CAMQPLinkMessageHandler linkReceiver)
     {
         linkReceivers.put(linkHandle, linkReceiver);
     }
 
     /**
      * Called by the Link layer to acknowledge completion
      * of processing of transfer frame. Results in incomingWindow
      * incrementing by 1.
      *
      * This mechanism allows throttling
      * if the link receiver is not able to process the transfer frames
      * at the same rate as the link sender is sending the frames.
      */
     @Override
     public void ackTransfer(long transferId)
     {
         synchronized (this)
         {
             incomingWindow++;
         }
     }
 
     /**
      * Parks the deliveryId with CAMQPDispositionSender for a scheduled sending
      * of batched dispositions.
      */
     @Override
     public void sendDisposition(long deliveryId, boolean settleMode, boolean role, Object newState)
     {
         dispositionSender.insertDispositionRange(deliveryId, role, settleMode, newState);
     }
 }
