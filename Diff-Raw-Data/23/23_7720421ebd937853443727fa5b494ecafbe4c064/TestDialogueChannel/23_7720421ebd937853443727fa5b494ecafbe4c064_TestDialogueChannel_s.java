 /*
 * Copyright (c) 2002-2013 Nu Echo Inc. All rights reserved.
  */
 
 package com.nuecho.rivr.samples.voicemail.dialogue;
 
 import static org.junit.Assert.*;
 
 import javax.json.*;
 
 import org.hamcrest.*;
 import org.junit.rules.*;
 import org.slf4j.*;
 
 import com.nuecho.rivr.core.channel.synchronous.step.*;
 import com.nuecho.rivr.core.dialogue.*;
 import com.nuecho.rivr.core.util.*;
 import com.nuecho.rivr.voicexml.dialogue.*;
 import com.nuecho.rivr.voicexml.test.*;
 import com.nuecho.rivr.voicexml.turn.first.*;
 import com.nuecho.rivr.voicexml.turn.input.*;
 import com.nuecho.rivr.voicexml.turn.last.*;
 import com.nuecho.rivr.voicexml.turn.output.*;
 
 /**
  * @author Nu Echo Inc.
  */
 public class TestDialogueChannel extends ExternalResource {
     private final Logger mLogger;
     private final VoiceXmlDialogueFactory mFactory;
     private final Duration mTimeout;
     private VoiceXmlTestDialogueChannel mDialogueChannel;
 
     public TestDialogueChannel(VoiceXmlDialogueFactory factory) {
         this(factory, Duration.seconds(10), null);
     }
 
     public TestDialogueChannel(VoiceXmlDialogueFactory factory, Duration timeout, Logger logger) {
         mFactory = factory;
         mTimeout = timeout;
         if (logger == null) {
             logger = LoggerFactory.getLogger("com.nuecho.rivr.tests");
         }
 
         mLogger = logger;
     }
 
     @Override
     protected void before() throws Throwable {
         mDialogueChannel = new VoiceXmlTestDialogueChannel("Dialog Tests", mTimeout);
         VoiceXmlDialogueContext context = new VoiceXmlDialogueContext(mDialogueChannel,
                                                                       mLogger,
                                                                       "testDialogueId",
                                                                       "contextPath",
                                                                       "servletPath");
         mFactory.create(new TestDialogueInitializationInfo(context));
         VoicemailDialogue dialogue = new VoicemailDialogue();
         mDialogueChannel.startDialogue(dialogue, new VoiceXmlFirstTurn(), context);
     }
 
     @Override
     protected void after() {
         mDialogueChannel.dispose();
     }
 
     public final void checkThat(Matcher<VoiceXmlTestDialogueChannel> matcher) {
         assertThat(mDialogueChannel, matcher);
     }
 
     public final Step<VoiceXmlOutputTurn, VoiceXmlLastTurn> processDtmfRecognition(String dtmfString) {
         return mDialogueChannel.processDtmfRecognition(dtmfString);
     }
 
     public final Step<VoiceXmlOutputTurn, VoiceXmlLastTurn> processDtmfRecognition(String dtmfString,
                                                                                    JsonValue interpretation,
                                                                                    MarkInfo markInfo) {
         return mDialogueChannel.processDtmfRecognition(dtmfString, interpretation, markInfo);
     }
 
     public final Step<VoiceXmlOutputTurn, VoiceXmlLastTurn> processRecognition(JsonArray recognitionResult) {
         return mDialogueChannel.processRecognition(recognitionResult);
     }
 
     public final Step<VoiceXmlOutputTurn, VoiceXmlLastTurn> processRecognition(RecognitionInfo recognitionInfo) {
         return mDialogueChannel.processRecognition(recognitionInfo);
     }
 
     public final Step<VoiceXmlOutputTurn, VoiceXmlLastTurn> processScriptExecutionTurn(JsonValue value) {
         return mDialogueChannel.processScript(value);
     }
 
     public Step<VoiceXmlOutputTurn, VoiceXmlLastTurn> processValue(JsonValue value) {
         return mDialogueChannel.processValue(value);
     }
 
     public final Step<VoiceXmlOutputTurn, VoiceXmlLastTurn> processRecording(RecordingInfo recordingInfo) {
         return mDialogueChannel.processRecording(recordingInfo);
     }
 
     public final Step<VoiceXmlOutputTurn, VoiceXmlLastTurn> processNoAction() {
         return mDialogueChannel.processNoAction();
     }
 
     public final Step<VoiceXmlOutputTurn, VoiceXmlLastTurn> processInputTurn(VoiceXmlInputTurn inputTurn) {
         return mDialogueChannel.processInputTurn(inputTurn);
     }
 
     public final Step<VoiceXmlOutputTurn, VoiceXmlLastTurn> processInputTurn(VoiceXmlInputTurn inputTurn,
                                                                              Duration timeout) {
         return mDialogueChannel.processInputTurn(inputTurn, timeout);
     }
 
     public final Step<VoiceXmlOutputTurn, VoiceXmlLastTurn> processNoMatch() {
         return mDialogueChannel.processNoMatch();
     }
 
     public final Step<VoiceXmlOutputTurn, VoiceXmlLastTurn> processNoInput() {
         return mDialogueChannel.processNoInput();
     }
 
     public final Step<VoiceXmlOutputTurn, VoiceXmlLastTurn> processMaxSpeechTimeout() {
         return mDialogueChannel.processMaxSpeechTimeout();
     }
 
     public final Step<VoiceXmlOutputTurn, VoiceXmlLastTurn> processHangup() {
         return mDialogueChannel.processHangup();
     }
 
     public final Step<VoiceXmlOutputTurn, VoiceXmlLastTurn> processPlatformError() {
         return mDialogueChannel.processPlatformError();
     }
 
     public final Step<VoiceXmlOutputTurn, VoiceXmlLastTurn> processTransferResult(TransferStatusInfo transferStatusInfo) {
         return mDialogueChannel.processTransferResult(transferStatusInfo);
     }
 
     public final Step<VoiceXmlOutputTurn, VoiceXmlLastTurn> processTransferInvalidDestinationResult() {
         return mDialogueChannel.processTransferInvalidDestinationResult();
     }
 
     public final Step<VoiceXmlOutputTurn, VoiceXmlLastTurn> processTransferDisconnect() {
         return mDialogueChannel.processTransferDisconnect();
     }
 
     public final Step<VoiceXmlOutputTurn, VoiceXmlLastTurn> processEvent(String event) {
         return mDialogueChannel.processEvent(event);
     }
 
     private static final class TestDialogueInitializationInfo implements
             DialogueInitializationInfo<VoiceXmlInputTurn, VoiceXmlOutputTurn, VoiceXmlDialogueContext> {
         private final VoiceXmlDialogueContext mContext;
 
         public TestDialogueInitializationInfo(VoiceXmlDialogueContext context) {
             mContext = context;
         }
 
         @Override
         public VoiceXmlDialogueContext getContext() {
             return mContext;
         }
     }
 }
