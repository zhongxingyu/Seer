 /*
  * Copyright (c) 2013 Nu Echo Inc. All rights reserved.
  */
 
 package com.nuecho.rivr.samples.voicemail.dialogue;
 
 import static com.nuecho.rivr.voicexml.turn.output.interaction.InteractionBuilder.*;
 import static java.lang.String.*;
 
 import java.util.regex.*;
 
 import javax.json.*;
 
 import org.slf4j.*;
 
 import com.nuecho.rivr.core.channel.*;
 import com.nuecho.rivr.core.util.*;
 import com.nuecho.rivr.samples.voicemail.model.*;
 import com.nuecho.rivr.voicexml.dialogue.*;
 import com.nuecho.rivr.voicexml.rendering.voicexml.*;
 import com.nuecho.rivr.voicexml.turn.*;
 import com.nuecho.rivr.voicexml.turn.first.*;
 import com.nuecho.rivr.voicexml.turn.input.*;
 import com.nuecho.rivr.voicexml.turn.last.*;
 import com.nuecho.rivr.voicexml.turn.output.*;
 import com.nuecho.rivr.voicexml.turn.output.audio.*;
 import com.nuecho.rivr.voicexml.turn.output.grammar.*;
 import com.nuecho.rivr.voicexml.turn.output.interaction.*;
 import com.nuecho.rivr.voicexml.util.*;
 import com.nuecho.rivr.voicexml.util.json.*;
 
 /**
  * @author Nu Echo Inc.
  */
 public final class VoicemailDialogue implements VoiceXmlDialogue {
     private static final TimeValue DEFAULT_TIMEOUT = TimeValue.seconds(5);
 
     private final Logger mLog = LoggerFactory.getLogger(getClass());
 
     private static final String STATUS_PROPERTY = "status";
     private static final String STATUS_ERROR = "error";
     private static final String STATUS_INTERRUPTED = "interrupted";
     private static final String STATUS_SUCCESS = "success";
     private static final String STATUS_INVALID_USER = "invalid-user";
 
     private static final String CAUSE_PROPERTY = "cause";
 
     private static final Pattern DNIS = Pattern.compile(".*:([0-9]*)@.*:.*");
 
     private static final String RECORDING_LOCATION = "application.recording";
 
     private DialogueChannel<VoiceXmlInputTurn, VoiceXmlOutputTurn> mChannel;
     private String mContextPath;
 
     private boolean mNuBotMode;
 
     @Override
     public VoiceXmlLastTurn run(VoiceXmlFirstTurn firstTurn, VoiceXmlDialogueContext context) throws Exception {
         mContextPath = context.getContextPath();
         mChannel = context.getDialogueChannel();
         String status;
         JsonObjectBuilder resultObjectBuilder = JsonUtils.createObjectBuilder();
         try {
             status = runDialogue();
         } catch (InterruptedException exception) {
             Thread.currentThread().interrupt();
             status = STATUS_INTERRUPTED;
         } catch (Exception exception) {
             mLog.error("Error during dialogue", exception);
             status = STATUS_ERROR;
             JsonUtils.add(resultObjectBuilder, CAUSE_PROPERTY, ResultUtils.toJson(exception));
            return new VoiceXmlExitTurn(STATUS_ERROR, "com.nuecho.rivr");
         }
 
         JsonUtils.add(resultObjectBuilder, STATUS_PROPERTY, status);
         VariableDeclarationList variables = VariableDeclarationList.create(resultObjectBuilder.build());
 
         return new VoiceXmlExitTurn("result", variables);
     }
 
     private String runDialogue() throws Timeout, InterruptedException {
         detectNuBotInstrumentation();
 
         if (login() == null) return STATUS_INVALID_USER;
 
         // C03
         DtmfRecognitionConfiguration dtmfConfig = dtmfBargIn(1);
         InteractionTurn mainMenu = newInteractionBuilder("main-menu").addPrompt(dtmfConfig,
                                                                                 audio("vm-youhave"),
                                                                                 synthesis("1"),
                                                                                 audio("vm-Old"),
                                                                                 audio("vm-message"),
                                                                                 audio("vm-onefor"),
                                                                                 audio("vm-Old"),
                                                                                 audio("vm-messages"),
                                                                                 audio("vm-opts"))
                                                                      .build(dtmfConfig, DEFAULT_TIMEOUT);
 
         String menu;
         do {
             menu = processDtmfTurn(mainMenu);
             if ("0".equals(menu)) {
                 mailboxConfigure();
             } else if ("1".equals(menu)) {
                 messageMenu();
             } else if ("3".equals(menu)) {
                 advancedOptions();
             }
         } while (!"#".equals(menu));
 
         // C50
         processTurn(audio("good-bye", "vm-goodbye"));
 
         return STATUS_SUCCESS;
     }
 
     private void detectNuBotInstrumentation() throws Timeout, InterruptedException {
         ScriptExecutionTurn clidAndDnisTurn = new ScriptExecutionTurn("clidAndDnis");
         VariableDeclarationList dnisVariables = new VariableDeclarationList();
         dnisVariables.addVariable(new VariableDeclaration("dnis", "session.connection.local.uri"));
         clidAndDnisTurn.setVariables(dnisVariables);
 
         VoiceXmlInputTurn inputTurn = processTurn(clidAndDnisTurn);
         JsonObject result = (JsonObject) inputTurn.getJsonValue();
 
         if (result != null) {
             String dnis = result.getString("dnis");
             Matcher matcher = DNIS.matcher(dnis);
             if (!matcher.matches()) throw new IllegalArgumentException(format("Received invalid dnis [%s]", dnis));
             String extension = matcher.group(1);
             mNuBotMode = extension.startsWith("495");
             if (mNuBotMode) {
                 mLog.info("Running dialogue in NuBot mode (instrumented prompts)");
             }
         }
     }
 
     private void mailboxConfigure() throws Timeout, InterruptedException, HangUp, PlatformError {
         // C07
         InteractionTurn options = audioWithDtmf("mailbox-options", "vm-options", 1);
 
         // C08
         InteractionTurn record = record("record-name", "vm-rec-name");
 
         // C09
         // FIXME onNoMatch: sorry + reprompt. Missing sorry prompt, so always reprompt instead.
         InteractionTurn review = audioWithDtmf("confirm-name", "vm-review", 1);
 
         // C10
         MessageTurn saved = audio("message-saved", "vm-msgsaved");
 
         String selectedOption;
         do {
             selectedOption = processDtmfTurn(options);
             if ("3".equals(selectedOption)) {
                 processTurn(record).getRecordingInfo();
                 String reviewOption = processDtmfTurn(review);
                 // 1-save, 2-listen and review, 3-rerecord
                 if ("1".equals(reviewOption)) {
                     processTurn(saved);
                 }
             }
         } while (!"*".equals(selectedOption));
     }
 
     private MessageTurn audio(String interactionName, String audioName) {
         return new MessageTurn(interactionName, audio(audioName));
     }
 
     private void messageMenu() throws Timeout, InterruptedException {
         // C04 first message received "date" from "phone number" recording
         InteractionTurn playMessage = newInteractionBuilder("play-message").addPrompt(audio("vm-first"),
                                                                                       audio("vm-message"),
                                                                                       audio("vm-received")).build();
         // C05
         InteractionTurn callMenu = audioWithDtmf("call-menu", "vm-advopts", 1);
         // C17 which folder loop press dtmf for foldername messages
         InteractionTurn whichFolder = audioWithDtmf("ask-folder-to-save", "vm-savefolder", 1);
         // C18 message 1 savedto old messages
         MessageTurn messageSaved = audio("message-saved", "vm-savedto");
         String menu;
         processTurn(playMessage);
         do {
             menu = processDtmfTurn(callMenu);
             if ("9".equals(menu)) {
                 String folderNum = processDtmfTurn(whichFolder);
                 if ("1".equals(folderNum)) {// TODO do something with the folder
                     processTurn(messageSaved);
                 }
                 // TODO usually, 4 means previous, 5 replay and 6 next. just replay same for now.
             } else if ("4".equals(menu) || "5".equals(menu) || "6".equals(menu)) {
                 processTurn(playMessage);
             }
         } while (!"*".equals(menu)); // TODO '#' should exit voicemail.
     }
 
     private void advancedOptions() throws Timeout, InterruptedException {
         // C11
         InteractionTurn advancedMenu = audioWithDtmf("advanced-options", "vm-leavemsg", 1);
         // C12
         InteractionTurn extension = audioWithDtmf("ask-extension", "vm-extension", 4);
         // C14
 
         InteractionTurn message = record("ask-message", "vm-intro");
         // C15
         DtmfRecognitionConfiguration dtmfConfig = dtmfBargeIn("#");
         InteractionTurn toCall = newInteractionBuilder("ask-number-to-call").addPrompt(dtmfConfig,
                                                                                        audio("vm-enter-num-to-call"))
                                                                             .build(dtmfConfig, DEFAULT_TIMEOUT);
 
         // C16
         MessageTurn dialOut = audio("dial-out", "vm-dialout");
 
         String subMenu = processDtmfTurn(advancedMenu);
         if ("4".equals(subMenu)) {
             String numberToCall = processDtmfTurn(toCall);
             if ("1234".equals(numberToCall)) {
                 processTurn(dialOut);
                 // TODO Transfer
                 //            defaultHandlers(wrap(new BlindTransferTurn("dial-out", numberToCall))).doTurn(mChannel, null);
             }
         } else if ("5".equals(subMenu)) {
             String extensionToCall;
             do {
                 extensionToCall = processDtmfTurn(extension);
             } while (!validateExtension(extensionToCall));
 
             mChannel.doTurn(message, null); // what to do with the recording?
         }
     }
 
     private boolean validateExtension(String extensionToCall) throws Timeout, InterruptedException {
         if (!"1234".equals(extensionToCall)) {
             // C13
             processTurn(audio("invalid-extension", "pbx-invalid"));
             return false;
         }
         return true;
     }
 
     private User login() throws Timeout, InterruptedException, HangUp, PlatformError {
         TimeValue timeout = TimeValue.seconds(10);
         // C01
         InteractionTurn askLogin = audioWithDtmf("ask-login", "vm-login", 4, timeout);
         // C02
         InteractionTurn askPassword = audioWithDtmf("ask-password", "vm-password", 4, timeout);
         // C06
         InteractionTurn incorrect = audioWithDtmf("incorrect-mailbox", "vm-incorrect-mailbox", 4, timeout);
 
         String username;
         String password;
         int tries = 0;
         do {
             tries++;
             username = processDtmfTurn(askLogin);
             password = processDtmfTurn(askPassword);
             // Subsequent tries must use the other interaction.
             askLogin = incorrect;
         } while (!validate(username, password) && tries < 3);
         if (tries == 3) {
             processTurn(audio("goodbye", "vm-goodbye"));
             return null;
         }
         return new User(username, password);
     }
 
     private boolean validate(String username, String password) {
         // FIXME STUB, only to make NuBot's test scenarios work.
         return username.equals("4069") && password.equals("6522");
     }
 
     private AudioItem synthesis(String text) {
         return new SynthesisText(text);
     }
 
     private AudioItem audio(String audioName) {
         return new Recording(audioPath(audioName));
     }
 
     private DtmfRecognitionConfiguration dtmfBargIn(int dtmfLength) {
         GrammarReference grammarReference = new GrammarReference("builtin:dtmf/digits?length=" + dtmfLength);
         DtmfRecognitionConfiguration dtmfConfig = new DtmfRecognitionConfiguration(grammarReference);
         dtmfConfig.setTermChar("A");
         return dtmfConfig;
     }
 
     private DtmfRecognitionConfiguration dtmfBargeIn(String termChar) {
         GrammarReference grammarReference = new GrammarReference("builtin:dtmf/digits");
         DtmfRecognitionConfiguration dtmfConfig = new DtmfRecognitionConfiguration(grammarReference);
         dtmfConfig.setTermChar(termChar);
         return dtmfConfig;
     }
 
     private InteractionTurn audioWithDtmf(String interactionName, String audio, int dtmfLength) {
         return audioWithDtmf(interactionName, audio, dtmfLength, DEFAULT_TIMEOUT);
     }
 
     private InteractionTurn audioWithDtmf(String interactionName, String audio, int dtmfLength, TimeValue noInputTimeout) {
         DtmfRecognitionConfiguration dtmfconfig = dtmfBargIn(dtmfLength);
         return newInteractionBuilder(interactionName).addPrompt(dtmfconfig, audio(audio)).build(dtmfconfig,
                                                                                                 noInputTimeout);
     }
 
     private String audioPath(String audio) {
         String promptType = mNuBotMode ? "instrumented" : "original";
         return format("%s/%s/%s.ulaw", mContextPath, promptType, audio);
     }
 
     private InteractionTurn record(String interactionName, String audio) {
         RecordingConfiguration recordingConfiguration = new RecordingConfiguration();
         recordingConfiguration.setBeep(true);
         recordingConfiguration.setDtmfTerm(true);
         recordingConfiguration.setType("audio/x-wav");
         recordingConfiguration.setClientSideAssignationDestination(RECORDING_LOCATION);
         GrammarReference grammarReference = new GrammarReference("builtin:dtmf/digits?length=1");
         DtmfRecognitionConfiguration config = new DtmfRecognitionConfiguration(grammarReference);
         recordingConfiguration.setDtmfTermRecognitionConfiguration(config);
         recordingConfiguration.setPostAudioToServer(true);
 
         return newInteractionBuilder(interactionName).addPrompt(audio(audio)).build(recordingConfiguration,
                                                                                     TimeValue.seconds(10));
     }
 
     private String processDtmfTurn(InteractionTurn interaction) throws Timeout, InterruptedException {
         // Is there a better way?
         VoiceXmlInputTurn resultTurn = processTurn(interaction);
         RecognitionInfo result = resultTurn.getRecognitionInfo();
         if (result == null) return "";
         String rawDtmfs = result.getRecognitionResult().getJsonObject(0).getJsonString("utterance").getString();
         mLog.trace("Received {}", rawDtmfs);
         return rawDtmfs.replace(" ", "");
     }
 
     private VoiceXmlInputTurn processTurn(VoiceXmlOutputTurn outputTurn) throws Timeout, InterruptedException {
         VoiceXmlInputTurn inputTurn = mChannel.doTurn(outputTurn, null);
         while (VoiceXmlEvent.hasEvent(VoiceXmlEvent.NO_INPUT, inputTurn.getEvents())
                || VoiceXmlEvent.hasEvent(VoiceXmlEvent.NO_MATCH, inputTurn.getEvents())) {
             inputTurn = mChannel.doTurn(outputTurn, null);
         }
         if (VoiceXmlEvent.hasEvent(VoiceXmlEvent.CONNECTION_DISCONNECT_HANGUP, inputTurn.getEvents())) { throw new HangUp(); }
         if (VoiceXmlEvent.hasEvent(VoiceXmlEvent.ERROR, inputTurn.getEvents())) { throw new PlatformError(); }
         return inputTurn;
     }
 
 }
