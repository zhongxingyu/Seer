 /*
  * Source code in 3rd-party is licensed and owned by their respective
  * copyright holders.
  *
  * All other source code is copyright Tresys Technology and licensed as below.
  *
  * Copyright (c) 2012 Tresys Technology LLC, Columbia, Maryland, USA
  *
  * This software was developed by Tresys Technology LLC
  * with U.S. Government sponsorship.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.tresys.jalop.jnl.impl;
 
 
 import java.util.LinkedList;
 import java.util.logging.LogManager;
 
 import javax.xml.soap.MimeHeaders;
 
import mockit.Mock;
import mockit.MockUp;
 import mockit.Mocked;
 import mockit.NonStrictExpectations;
 import mockit.Verifications;
 import mockit.VerificationsInOrder;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.beepcore.beep.core.AbortChannelException;
 import org.beepcore.beep.core.BEEPException;
 import org.beepcore.beep.core.Channel;
 import org.beepcore.beep.core.InputDataStream;
 import org.beepcore.beep.core.InputDataStreamAdapter;
 import org.beepcore.beep.core.Message;
 import org.beepcore.beep.core.OutputDataStream;
 import org.beepcore.beep.core.ReplyListener;
 import org.beepcore.beep.core.Session;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.tresys.jalop.jnl.ConnectionHandler.ConnectError;
 import com.tresys.jalop.jnl.RecordType;
 import com.tresys.jalop.jnl.Role;
 import com.tresys.jalop.jnl.SubscribeRequest;
 import com.tresys.jalop.jnl.Subscriber;
 import com.tresys.jalop.jnl.SubscriberSession;
 import com.tresys.jalop.jnl.exceptions.JNLException;
 import com.tresys.jalop.jnl.exceptions.MissingMimeHeaderException;
 import com.tresys.jalop.jnl.exceptions.UnexpectedMimeValueException;
 import com.tresys.jalop.jnl.impl.messages.InitAckMessage;
 import com.tresys.jalop.jnl.impl.messages.InitNackMessage;
 import com.tresys.jalop.jnl.impl.messages.Utils;
 import com.tresys.jalop.jnl.impl.subscriber.SubscriberSessionImpl;
 
 public class InitListenerTest {
     // Needed to mock static functions in the Utils class.
     @Mocked
     private Utils utils;
     @Before
     public void setUp() {
         // Disable logging so the build doesn't get spammed.
         Logger.getRootLogger().setLevel(Level.OFF);
     }
     
     @Test (expected = AbortChannelException.class)
     public void testInitListenerThrowsExceptionOnReceiveNul(ContextImpl contextImpl, Message message) throws AbortChannelException {
         InitListener initListener = new InitListener(Role.Subscriber, RecordType.Audit, contextImpl);
         initListener.receiveNUL(message);
     }
 
     @Test (expected = AbortChannelException.class)
     public void testInitListenerThrowsExceptionOnReceiveAns(ContextImpl contextImpl, Message message) throws AbortChannelException {
         InitListener initListener = new InitListener(Role.Subscriber, RecordType.Audit, contextImpl);
         initListener.receiveANS(message);
     }
 
     @Test (expected = AbortChannelException.class)
     public void testInitListenerThrowsExceptionForInitAckWithIllegalDigest(final ContextImpl contextImpl, final InputDataStream ids, final InputDataStreamAdapter isa, final Message msg) throws MissingMimeHeaderException, UnexpectedMimeValueException, BEEPException {
         final InitAckMessage iam = new InitAckMessage("foo", "bar", new MimeHeaders());
         final LinkedList<String> allowedDigests = new LinkedList<String>();
         allowedDigests.add("other");
         final LinkedList<String> allowedEncs = new LinkedList<String>();
         allowedEncs.add("other");
         allowedEncs.add("foo");

         new NonStrictExpectations() {
             {
                 msg.getDataStream(); result = ids;
                 ids.getInputStream(); result = isa;
                 Utils.processInitAck(isa); result = iam;
                 contextImpl.getAllowedMessageDigests(); result = allowedDigests;
                 contextImpl.getAllowedXmlEncodings(); result = allowedEncs;
                 
             }
         };
         InitListener initListener = new InitListener(Role.Subscriber, RecordType.Audit, contextImpl);
         initListener.receiveRPY(msg);
     }
 
     @Test (expected = AbortChannelException.class)
     public void testInitListenerThrowsExceptionForInitAckWithIllegalEncoding(final ContextImpl contextImpl, final InputDataStream ids, final InputDataStreamAdapter isa, final Message msg) throws MissingMimeHeaderException, UnexpectedMimeValueException, BEEPException {
         final InitAckMessage iam = new InitAckMessage("foo", "bar", new MimeHeaders());
         final LinkedList<String> allowedDigests = new LinkedList<String>();
         allowedDigests.add("other");
         allowedDigests.add("bar");
         final LinkedList<String> allowedEncs = new LinkedList<String>();
         allowedEncs.add("other");
         new NonStrictExpectations() {
             {
                 msg.getDataStream(); result = ids;
                 ids.getInputStream(); result = isa;
                 Utils.processInitAck(isa); result = iam;
                 contextImpl.getAllowedMessageDigests(); result = allowedDigests;
                 contextImpl.getAllowedXmlEncodings(); result = allowedEncs;
             }
         };
         InitListener initListener = new InitListener(Role.Subscriber, RecordType.Audit, contextImpl);
         initListener.receiveRPY(msg);
     }
 
     @Test
     public void testInitListenerSendsSubscribeRequest(final ContextImpl contextImpl,
             final InputDataStream ids, final InputDataStreamAdapter isa,
             final Message msg, final Subscriber subscriber,
             final SubscribeRequest subRequest, final OutputDataStream ods,
             final Channel channel, final Session sess,
            final ReplyListener rpyListener) throws BEEPException, JNLException, InterruptedException {
         final InitAckMessage iam = new InitAckMessage("foo", "bar", new MimeHeaders());
         final LinkedList<String> allowedDigests = new LinkedList<String>();
         allowedDigests.add("bar");
         final LinkedList<String> allowedEncs = new LinkedList<String>();
         allowedEncs.add("foo");
        // mock up thread since this function is supposed to spawn a new thread, but 
        // don't actually want it to do that.
        new MockUp<Thread>() {
            @Mock
            public void start() {
                // do nothing
            }
        };
        
         new NonStrictExpectations() {
             {
                 msg.getDataStream(); result = ids;
                 ids.getInputStream(); result = isa;
                 Utils.processInitAck(isa); result = iam;
                 contextImpl.getAllowedMessageDigests(); result = allowedDigests;
                 contextImpl.getAllowedXmlEncodings(); result = allowedEncs;
                 contextImpl.getSubscriber(); result = subscriber;
                 subscriber.getSubscribeRequest((SubscriberSession) any); result = subRequest;
                 subRequest.getSerialId(); result = "12345";
                 msg.getChannel(); result = channel;
                 contextImpl.getDefaultPendingDigestMax(); result = 1;
                 contextImpl.getDefaultDigestTimeout(); result = 1;
                 channel.getSession(); result = sess;
                 Utils.createSubscribeMessage(anyString); result = ods;
             }
         };
 
         InitListener initListener = new InitListener(Role.Subscriber, RecordType.Audit, contextImpl);
         initListener.receiveRPY(msg);
         new VerificationsInOrder() {
             {
                 contextImpl.addSession(sess, (SubscriberSessionImpl) any);
                 channel.sendMSG(ods, (ReplyListener)any);
             }
         };
     }
     @Test (expected = AbortChannelException.class)
     public void testInitListenerThrowsException(final ContextImpl contextImpl, final InputDataStream ids, final InputDataStreamAdapter isa, final Message msg) throws MissingMimeHeaderException, UnexpectedMimeValueException, BEEPException {
         final LinkedList<ConnectError> errors = new LinkedList<ConnectError>();
         errors.add(ConnectError.UnauthorizedMode);
         final InitNackMessage inm = new InitNackMessage(errors, new MimeHeaders());
         new NonStrictExpectations() {
             {
                 msg.getDataStream(); result = ids;
                 ids.getInputStream(); result = isa;
                 Utils.processInitNack(isa); result = inm;
             }
         };
         InitListener initListener = new InitListener(Role.Subscriber, RecordType.Audit, contextImpl);
         initListener.receiveERR(msg);
     }
 
 }
 
