 /**
  * The Accord Project, http://accordproject.org
  * Copyright (C) 2005-2013 Rafael Marins, http://rafaelmarins.com
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.neociclo.isdn.netty.handler;
 
 import static java.lang.String.*;
 import static org.jboss.netty.channel.Channels.*;
 import static org.jboss.netty.buffer.ChannelBuffers.*;
 import static org.neociclo.capi20.parameter.Reject.*;
 import static org.neociclo.isdn.netty.channel.MessageBuilder.*;
 
 import java.nio.charset.Charset;
 
 import org.apache.mina.statemachine.annotation.State;
 import org.apache.mina.statemachine.annotation.Transition;
 import org.apache.mina.statemachine.annotation.Transitions;
 import org.apache.mina.statemachine.context.StateContext;
 import org.apache.mina.statemachine.event.Event;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.ChannelEvent;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelFutureListener;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.neociclo.capi20.CapiException;
 import org.neociclo.capi20.message.CapiMessage;
 import org.neociclo.capi20.message.ConnectActiveInd;
 import org.neociclo.capi20.message.ConnectB3ActiveInd;
 import org.neociclo.capi20.message.ConnectB3Conf;
 import org.neociclo.capi20.message.ConnectB3Ind;
 import org.neociclo.capi20.message.ConnectB3Resp;
 import org.neociclo.capi20.message.ConnectConf;
 import org.neociclo.capi20.message.DataB3Conf;
 import org.neociclo.capi20.message.DataB3Ind;
 import org.neociclo.capi20.message.DisconnectB3Conf;
 import org.neociclo.capi20.message.DisconnectB3Ind;
 import org.neociclo.capi20.message.DisconnectConf;
 import org.neociclo.capi20.message.DisconnectInd;
 import org.neociclo.capi20.message.ResetB3Conf;
 import org.neociclo.capi20.message.ResetB3Ind;
 import org.neociclo.capi20.parameter.Flag;
 import org.neociclo.capi20.parameter.Info;
 import org.neociclo.capi20.parameter.Reason;
 import org.neociclo.isdn.netty.channel.IsdnChannel;
 import org.neociclo.isdn.netty.channel.IsdnChannelConfig;
 import org.neociclo.netty.statemachine.SimpleStateMachineHandler;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Rafael Marins
  */
 public class IsdnConnectionHandler extends SimpleStateMachineHandler {
 
 	private static final Charset US_ASCII_CHARSET = Charset.forName("US-ASCII");
 
     private static final Logger LOGGER = LoggerFactory.getLogger(IsdnConnectionHandler.class);
 
     public static final String ISDN_CONNECTED_EVENT_ATTR = "Isdn.connectFuture";
     public static final String ISDN_CLOSE_REQUESTED_EVENT_ATTR = "Isdn.closeRequestedEvent";
     public static final String ISDN_RECEIVE_BUF_ATTR = "Isdn.receiveBuffer";
 
     /** General state of the protocol handler; the state it is initialized. */
     @State
     public static final String PLCI = "GENERAL";
 
     @State(PLCI)
     public static final String PLCI_IDLE = "P-0";
 
     @State(PLCI)
     public static final String WF_CONNECT_CONF = "P-0.1 [WF_CONNECT_CONF]";
     
     @State(PLCI)
     public static final String WF_CONNECT_ACTIVE_IND = "P-1.1";
     
     @State(PLCI)
     public static final String WF_DISCONNECT_CONF = "P-5 [WF_DISCONNECT_CONF]";
     
     @State(PLCI)
     public static final String P4_WF_CONNECT_ACTIVE_IND = "P-4 [WF_CONNECT_ACTIVE_IND]";
 
     @State(PLCI)
     public static final String PLCI_ACTIVE = "P-ACT";
 
     @State(PLCI_ACTIVE)
     public static final String NCCI_IDLE = "N-0";
 
     @State(NCCI_IDLE)
     public static final String DO_CONNECT_B3_REQ = "P-1.2 [DO_CONNECT_B3_REQ]";
 
     @State(PLCI_ACTIVE)
     public static final String WF_CONNECT_B3_CONF = "N-0.2 [WF_CONNECT_B3_CONF]";
     
     @State(PLCI_ACTIVE)
     public static final String WF_CONNECT_B3_ACTIVE_IND = "N-2 [WF_CONNECT_B3_ACTIVE_IND]";
 
     @State(PLCI_ACTIVE)
     public static final String WF_RESET_B3_CONF = "N-0.3 [WF_RESET_B3_CONF]";
 
     @State(PLCI_ACTIVE)
     public static final String NCCI_ACTIVE = "N-ACT";
     
     @State(PLCI_ACTIVE)
     public static final String WF_DISCONNECT_B3_CONF = "N-4 [WF_DISCONNECT_B3_CONF]";
 
     public IsdnConnectionHandler() {
         super();
     }
 
     /**
      * Server mode method used to hold ChannelEvent#CONNECTED to sendUpstream()
      * on CONNECT_B3_ACTIVE_IND.
      */
     @Transition(on = CHANNEL_CONNECTED, in = PLCI)
     public void retainChannelConnectedEvent(StateContext stateCtx, ChannelStateEvent e) {
         LOGGER.trace("retainChannelConnectedEvent()");
         stateCtx.setAttribute(ISDN_CONNECTED_EVENT_ATTR, e);
     }
 
     /**
      * Client mode method used to hold ChannelEvent#CONNECT_REQUESTED to sendUpstream()
      * on CONNECT_B3_ACTIVE_IND.
      */
     @Transition(on = CONNECT_REQUESTED, in = PLCI)
     public void retainConnectRequestedEvent(StateContext stateCtx,ChannelHandlerContext ctx,  ChannelStateEvent e) {
         LOGGER.trace("retainConnectRequestedEvent()");
         stateCtx.setAttribute(ISDN_CONNECTED_EVENT_ATTR, e);
 
         // forward CONNET_REQUESTED to the Sink after event retention 
         ctx.sendDownstream(e);
     }
 
     // -------------------------------------------------------------------------
     // Physical Link control :: 
     // -------------------------------------------------------------------------
 
     @Transition(on = CHANNEL_CONNECTED, in = PLCI_IDLE, next = WF_CONNECT_CONF)
     public void plciConnectReq(IsdnChannel channel, StateContext stateCtx, ChannelStateEvent e) throws CapiException {
 
         LOGGER.trace("plciConnectReq()");
 
         CapiMessage connectReq = createConnectRequest(channel);
         channel.write(connectReq);
 
     }
 
     @Transition(on = MESSAGE_RECEIVED, in = WF_CONNECT_CONF, next = WF_CONNECT_ACTIVE_IND)
     public void plciConnectConf(IsdnChannel channel, ConnectConf msgConf) throws CapiException {
 
         LOGGER.trace("plciConnectConf()");
 
         Info response = msgConf.getInfo();
         if (response != Info.REQUEST_ACCEPTED) {
             LOGGER.debug("PLCI connect failed. Connect Confirmation: info = {}.", response);
            close(channel);
             throw new CapiException(msgConf.getInfo(), "PLCI connect failed.");
         }
 
         // keep the PLCI information on IsdnChannel
         IsdnChannelConfig config = channel.getConfig();
         config.setPlci(msgConf.getPlci());
 
     }
 
     @Transitions ({
         @Transition(on = MESSAGE_RECEIVED, in = WF_CONNECT_ACTIVE_IND, next = DO_CONNECT_B3_REQ),
         @Transition(on = MESSAGE_RECEIVED, in = P4_WF_CONNECT_ACTIVE_IND, next = NCCI_IDLE)        
     })
     public void plciConnectActiveInd(final IsdnChannel channel, final StateContext stateCtx,
             ConnectActiveInd activeInd, final ChannelHandlerContext ctx) throws CapiException {
 
         LOGGER.trace("plciConnectActiveInd()");
 
         // TODO shoud I keep the received LLC in channel config ?
 
         // reply with CONNECT_ACTIVE_RESP
         CapiMessage activeResp = replyConnectActiveResp(activeInd);
 
         channel.write(activeResp);
 
     }
 
     @Transitions ({
         @Transition(on = MESSAGE_RECEIVED, in = WF_CONNECT_CONF, next = PLCI_IDLE),
         @Transition(on = MESSAGE_RECEIVED, in = WF_CONNECT_ACTIVE_IND, next = PLCI_IDLE),
         @Transition(on = MESSAGE_RECEIVED, in = WF_DISCONNECT_CONF, next = PLCI_IDLE),
         @Transition(on = MESSAGE_RECEIVED, in = PLCI_ACTIVE, next = PLCI_IDLE)
     })
     public void plciDisconnectInd(final IsdnChannel channel, final StateContext stateCtx, DisconnectInd disconInd)
             throws CapiException {
 
         final Reason reason = disconInd.getReason();
         LOGGER.trace("plciDisconnectInd() :: reason = {}", reason);
 
         CapiMessage disconResp = replyDisconnectResp(disconInd);
 
         ChannelFuture writeFuture = channel.write(disconResp);
         writeFuture.addListener(new ChannelFutureListener() {
             public void operationComplete(ChannelFuture future) throws Exception {
                 if (!channel.isConnected()) {
                     // retrieve CHANNEL_CONNECTED event and clear attribute
                     ChannelStateEvent channelConnected = (ChannelStateEvent) stateCtx
                             .getAttribute(ISDN_CONNECTED_EVENT_ATTR);
                     stateCtx.setAttribute(ISDN_CONNECTED_EVENT_ATTR, null);
                     // set FAILED on connectFuture
                     channelConnected.getFuture().setFailure(
                             new Exception(new Exception(format("DISCONNECT_IND - %s", reason))));
                 }
                 close(channel);
             }
         });
 
     }
 
     @Transition(on = MESSAGE_RECEIVED, in = PLCI_IDLE)
     public void plciIdleDisconnectInd(final IsdnChannel channel, final StateContext stateCtx, DisconnectInd disconInd) {
         LOGGER.trace("plciIdleDisconnectInd() :: ignoring");
     }
 
     /**
      * Triggered on DISCONNECT_B3_CONF or DISCONNECT_B3_RESP (NCCI level)
      * handling.
      */
     public void plciDisconnectReq(IsdnChannel channel) throws CapiException {
 
         LOGGER.trace("plciDisconnectReq()");
 
         CapiMessage disconReq = createDisconnectReq(channel);
         channel.write(disconReq);
 
     }
 
     @Transitions ({
         @Transition(on = MESSAGE_RECEIVED, in = WF_DISCONNECT_CONF, next = PLCI_IDLE),
         @Transition(on = MESSAGE_RECEIVED, in = NCCI_IDLE, next = PLCI_IDLE) // might happen in server mode        
     })
     public void plciDisconnectConf(IsdnChannel channel, StateContext stateCtx, DisconnectConf conf,
             ChannelHandlerContext ctx) throws CapiException {
 
         LOGGER.trace("plciDisconnectConf()");
 
         Info response = conf.getInfo();
         if (response != Info.REQUEST_ACCEPTED) {
             LOGGER.debug("Disconnect failed. Confirmation: info = {}.", response);
             throw new CapiException(conf.getInfo(), "Disconnect failed.");
         }
 
         // forward the ChannelEvent#CLOSE_REQUESTED caught on
         // ncciDisconnectB3Req() to down layers with sendDownstream()
         ChannelStateEvent closeRequested = (ChannelStateEvent) stateCtx.getAttribute(ISDN_CLOSE_REQUESTED_EVENT_ATTR);
         if (closeRequested != null) {
             stateCtx.setAttribute(ISDN_CLOSE_REQUESTED_EVENT_ATTR, null);
             ctx.sendDownstream(closeRequested);
 
         }
 
     }
 
     // -------------------------------------------------------------------------
     // Logical Connection control
     // -------------------------------------------------------------------------
 
     /**
      * Triggered on CONNECT_ACTIVE_IND handling.
      */
     @Transition( on = WRITE_COMPLETE, in = DO_CONNECT_B3_REQ, next = WF_CONNECT_B3_CONF)
     public void ncciConnectB3Req(IsdnChannel channel, StateContext stateCtx) throws CapiException {
 
         LOGGER.trace("ncciConnectB3Req()");
 
         CapiMessage connectB3Req = createConnectB3Req(channel);
         channel.write(connectB3Req);
 
     }
 
     @Transition(on = MESSAGE_RECEIVED, in = WF_CONNECT_B3_CONF, next = WF_CONNECT_B3_ACTIVE_IND)
     public void ncciConnectB3Conf(IsdnChannel channel, ConnectB3Conf msgConf) throws CapiException {
 
         LOGGER.trace("ncciConnectB3Conf()");
 
         Info response = msgConf.getInfo();
         if (response != Info.REQUEST_ACCEPTED) {
             LOGGER.debug("NCCI connect B3 failed. Confirmation: info = {}.", response);
             throw new CapiException(msgConf.getInfo(), "NCCI connect B3 failed.");
         }
 
         // store the NCCI information on LogicalChannel
         IsdnChannelConfig config = channel.getConfig();
         config.setNcci(msgConf.getNcci());
 
     }
 
     @Transition(on = MESSAGE_RECEIVED, in = NCCI_IDLE, next = WF_CONNECT_B3_ACTIVE_IND)
     public void ncciConnectB3Ind(IsdnChannel channel, ConnectB3Ind ind) {
 
         LOGGER.trace("ncciConnectB3Ind()");
 
         // store the NCCI information on LogicalChannel
         IsdnChannelConfig config = channel.getConfig();
         config.setNcci(ind.getNcci());
 
         // send CONNECT_B3_RESP back
         ConnectB3Resp resp = replyConnectB3Ind(ind, ACCEPT_CALL);
         channel.write(resp);
 
     }
 
     @Transition(on = MESSAGE_RECEIVED, in = WF_CONNECT_B3_ACTIVE_IND, next = NCCI_ACTIVE)
     public void ncciConnectB3ActiveInd(final IsdnChannel channel, final StateContext stateCtx,
             ConnectB3ActiveInd conB3ActiveInd, final ChannelHandlerContext ctx) throws CapiException {
 
         LOGGER.trace("ncciConnectB3ActiveInd()");
 
         // TODO should I keep the received NCPI in logical channel (handshake)?
 
         // reply with CONNECT_B3_ACTIVE_RESP
         CapiMessage conB3ActiveResp = replyConnectB3ActiveResp(conB3ActiveInd);
         ChannelFuture writeFuture = channel.write(conB3ActiveResp);
 
         // set connected after write completed
         writeFuture.addListener(new ChannelFutureListener() {
             public void operationComplete(ChannelFuture future) throws Exception {
 
                 // retrieve CHANNEL_CONNECTED event 
                 ChannelStateEvent channelConnected = (ChannelStateEvent) stateCtx
                         .getAttribute(ISDN_CONNECTED_EVENT_ATTR);
                 stateCtx.setAttribute(ISDN_CONNECTED_EVENT_ATTR, null);
 
                 // mark connectFuture as succesful
                 LOGGER.trace("ncciConnectB3ActiveInd() :: channelConnected event = " + channelConnected);
                 channel.setConnected();
                 channelConnected.getFuture().setSuccess();
 
                 // set channel connected and raise the ChannelEvent#CONNECTED
                 // caught on CHANNEL_CONNECTED with sendUpstream()
                 ctx.sendUpstream(channelConnected);
 
             }
         });
 
     }
 
     @Transition(on = CLOSE_REQUESTED, in = NCCI_ACTIVE, next = WF_DISCONNECT_B3_CONF)
     public void ncciDisconnectB3Req(IsdnChannel channel, StateContext stateCtx, ChannelStateEvent e)
             throws CapiException {
 
         LOGGER.trace("ncciDisconnectB3Req()");
 
         CapiMessage disconnectB3Req = createDisconnectB3Req(channel);
         channel.write(disconnectB3Req);
 
         // hold ChannelEvent#CLOSE_REQUESTED to sendDownstream() on
         // DISCONNECT_CONF (PLCI level)
         stateCtx.setAttribute(ISDN_CLOSE_REQUESTED_EVENT_ATTR, e);
 
     }
 
     @Transition(on = MESSAGE_RECEIVED, in = WF_DISCONNECT_B3_CONF, next = NCCI_IDLE)
     public void ncciDisconnectB3Conf(IsdnChannel channel, DisconnectB3Conf conf) throws CapiException {
 
         LOGGER.trace("ncciDisconnectB3Conf()");
 
         try {
             Info response = conf.getInfo();
             if (response != Info.REQUEST_ACCEPTED) {
                 LOGGER.debug("NCCI disconnect B3 failed. Confirmation: info = {}.", response);
                 throw new CapiException(conf.getInfo(), "NCCI disconnect B3 failed.");
             }
         } finally {
             // trigger the plciDisconnectReq() manually
             plciDisconnectReq(channel);
         }
 
     }
 
     @Transitions ({
         @Transition(on = MESSAGE_RECEIVED, in = WF_CONNECT_B3_CONF, next = NCCI_IDLE),
         @Transition(on = MESSAGE_RECEIVED, in = WF_CONNECT_B3_ACTIVE_IND, next = NCCI_IDLE),
         @Transition(on = MESSAGE_RECEIVED, in = WF_DISCONNECT_B3_CONF, next = NCCI_IDLE),
         @Transition(on = MESSAGE_RECEIVED, in = NCCI_ACTIVE, next = NCCI_IDLE)
     })
     public void ncciDisconnectB3Ind(final IsdnChannel channel, DisconnectB3Ind disconB3Ind) throws CapiException {
 
         LOGGER.trace("ncciDisconnectB3Ind()");
 
         CapiMessage disconB3Resp = replyDisconnectB3Resp(disconB3Ind);
         channel.write(disconB3Resp);
     }
 
     @Transition(on = MESSAGE_RECEIVED, in = NCCI_IDLE)
     public void ncciIdleDisconnectB3Ind(final IsdnChannel channel, DisconnectB3Ind disconB3Ind) throws CapiException {
         LOGGER.trace("ncciIdleDisconnectB3Ind() :: ignoring");
     }
 
     // -------------------------------------------------------------------------
     // Packet Assembler/Disassembler control
     // -------------------------------------------------------------------------
 
     @Transition(on = MESSAGE_RECEIVED, in = NCCI_ACTIVE)
     public void ncciDataB3Ind(IsdnChannel channel, StateContext stateCtx, DataB3Ind dataInd, ChannelHandlerContext ctx, ChannelEvent channelEvent) throws CapiException {
 
     	if (LOGGER.isTraceEnabled()) {
 	        try {
 	            LOGGER.trace("ncciDataB3Ind() :: data = {}", new String(dataInd.getB3Data(), US_ASCII_CHARSET));
 	        } catch (Throwable t) {
 	            LOGGER.trace("ncciDataB3Ind()");
 	        }
     	}
 
         CapiMessage dataResp = replyDataB3Resp(dataInd);
 //        channel.write(dataResp);
         write(ctx, channelEvent.getFuture(), dataResp);
 
         ChannelBuffer buf = (ChannelBuffer) stateCtx.getAttribute(ISDN_RECEIVE_BUF_ATTR);
         if (buf == null) {
             buf = ChannelBuffers.dynamicBuffer();
         }
 
         // handle the more-data bit check
         ChannelBuffer data = wrappedBuffer(dataInd.getB3Data());
         buf.writeBytes(data);
 
         if (dataInd.hasFlag(Flag.MORE_DATA_BIT)) {
             // accumulate buffer on session state context
         	if (LOGGER.isTraceEnabled()) {
 	            LOGGER.trace("ncciDataB3Ind() :: accumulating data buffer... {}", dataInd);
         	}
             stateCtx.setAttribute(ISDN_RECEIVE_BUF_ATTR, buf);
         } else {
             // send the complete DATA to upper layer
             fireMessageReceived(channel, buf);
             stateCtx.setAttribute(ISDN_RECEIVE_BUF_ATTR, null);
         }
 
 
     }
     @Transition (on = WRITE_REQUESTED, in = NCCI_ACTIVE)
     public void ncciDataB3Req(IsdnChannel channel, ChannelBuffer message, ChannelHandlerContext ctx, ChannelEvent channelEvent) throws CapiException {
 
         if (message == ChannelBuffers.EMPTY_BUFFER) {
             // send flush() signal downstream
             LOGGER.warn("ncciDataB3Req() :: empty buffer");
             handleEvent(WRITE_REQUESTED, ctx, channelEvent);
             return;
         }
 
         if (LOGGER.isTraceEnabled()) {
 	        try {
 	            LOGGER.trace("ncciDataB3Req() :: data = {}", message.duplicate().toString(US_ASCII_CHARSET));
 	        } catch (Throwable t) {
 	            LOGGER.trace("ncciDataB3Req()");
 	        }
         }
 
         CapiMessage dataReq = createDataB3Req(channel, message);
 //        channel.write(dataReq);
         write(ctx, channelEvent.getFuture(), dataReq);
         
     }
 
     @Transition(on = MESSAGE_RECEIVED, in = NCCI_ACTIVE)
     public void ncciDataB3Conf(IsdnChannel channel, DataB3Conf dataConf) throws CapiException {
 
         LOGGER.trace("ncciDataB3Conf()");
 
         // TODO process the dataCon.getInfo() accordingly
     }
 
 
     // -------------------------------------------------------------------------
     // Flow control
     // -------------------------------------------------------------------------
 
     @Transition(on = MESSAGE_RECEIVED, in = NCCI_ACTIVE, next = WF_RESET_B3_CONF) //, next = WF_DISCONNECT_B3_CONF)
     public void ncciResetB3Ind(final IsdnChannel channel, StateContext stateCtx, ResetB3Ind resetInd) throws CapiException {
 
         LOGGER.trace("ncciResetB3Ind()");
 
         stateCtx.setAttribute(ISDN_RECEIVE_BUF_ATTR, null);
 
         CapiMessage resetResp = createResetB3Resp(channel);
 //        ChannelFuture resetRespFuture = channel.write(resetResp);
         channel.write(resetResp);
 
 //        CapiMessage resetReq = createResetB3Req(channel);
 //        channel.write(resetReq);
 
 //        resetRespFuture.addListener(new ChannelFutureListener() {
 //            public void operationComplete(ChannelFuture future) throws Exception {
 //                // send a subsequent DISCONNECT_B3_REQ
 //                CapiMessage disconnectB3Req = createDisconnectB3Req(channel);
 //                channel.write(disconnectB3Req);
 //            }
 //        });
 
     }
 
     @Transition(on = MESSAGE_RECEIVED, in = WF_RESET_B3_CONF, next = NCCI_ACTIVE)
     public void ncciResetB3Conf(IsdnChannel channel, ResetB3Conf conf) {
         LOGGER.trace("ncciResetB3Conf()");
     }
 
     // -------------------------------------------------------------------------
     // Exception handling
     // -------------------------------------------------------------------------
 
     @Transition(on = EXCEPTION_CAUGHT, in = PLCI, next = PLCI_IDLE)
     public void error(IsdnChannel channel, StateContext stateCtx, ChannelHandlerContext ctx, ExceptionEvent event) {
         LOGGER.error("Unexpected error.", event.getCause());
         if (!channel.isConnected()) {
             // retrieve CHANNEL_CONNECTED event and clear attribute
             ChannelStateEvent channelConnected = (ChannelStateEvent) stateCtx.getAttribute(ISDN_CONNECTED_EVENT_ATTR);
             stateCtx.setAttribute(ISDN_CONNECTED_EVENT_ATTR, null);
             // set FAILED on connectFuture 
             channelConnected.getFuture().setFailure(new Exception(format("ERROR - %s", event.getCause().getMessage())));
         } else {
             ctx.sendUpstream(event);
         }
         close(channel);
     }
 
     /**
      * Delegate unhandled events to Netty pipeline in the correct direction.
      */
     @Transition(on = ANY, in = PLCI)
     public void unhandledEvent(Event smEvent, ChannelHandlerContext ctx, ChannelEvent channelEvent) throws Exception {
 
         String name = (String) smEvent.getId();
         LOGGER.trace("UNHANDLED :: on = {} , in = {}, event = {} ", new Object[] { name,
                 getStateContext(ctx).getCurrentState().getId(), channelEvent });
 
         handleEvent(name, ctx, channelEvent);
     }
 
 }
