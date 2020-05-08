 /*
  * Copyright (c) 2013 Nu Echo Inc. All rights reserved.
  */
 
 package com.nuecho.rivr.samples.helloworld;
 
 import javax.json.*;
 
 import org.slf4j.*;
 
 import com.nuecho.rivr.core.dialogue.*;
 import com.nuecho.rivr.voicexml.dialogue.*;
 import com.nuecho.rivr.voicexml.rendering.voicexml.*;
 import com.nuecho.rivr.voicexml.turn.*;
 import com.nuecho.rivr.voicexml.turn.first.*;
 import com.nuecho.rivr.voicexml.turn.input.*;
 import com.nuecho.rivr.voicexml.turn.last.*;
 import com.nuecho.rivr.voicexml.turn.output.audio.*;
 import com.nuecho.rivr.voicexml.turn.output.interaction.*;
 import com.nuecho.rivr.voicexml.util.*;
 import com.nuecho.rivr.voicexml.util.json.*;
import static com.nuecho.rivr.voicexml.turn.output.interaction.InteractionBuilder.*;
import static com.nuecho.rivr.voicexml.util.json.JsonUtils.*;
import static com.nuecho.rivr.voicexml.turn.input.VoiceXmlEvent.*;
 /**
  * @author Nu Echo Inc.
  */
 public final class HelloWorldDialogue implements VoiceXmlDialogue {
 
    private static final String DIALOG_ID_MDC_KEY = "dialogId";
     private static final String CAUSE_PROPERTY = "cause";
    private final Logger mDialogLog = LoggerFactory.getLogger("hello.world");
 
     @Override
     public VoiceXmlLastTurn run(VoiceXmlFirstTurn firstTurn, VoiceXmlDialogueContext context) throws Exception {
        // Just some logging stuff.
        MDC.put(DIALOG_ID_MDC_KEY, context.getDialogueId());

        // The dialog termination cause. "Normal" by default.
         JsonValue cause = wrap("Normal");
 
        mDialogLog.info("Starting dialog");
         try {
             // Play a prompt
             InteractionTurn turn = newInteractionBuilder("hello").addPrompt(new SynthesisText("Hello World!")).build();
             VoiceXmlInputTurn inputTurn = DialogueUtils.doTurn(context, turn);
 
             // Handling hangup or error events
             if (hasEvent(CONNECTION_DISCONNECT_HANGUP, inputTurn.getEvents())) {
                 cause = wrap("Hangup");
             }
             if (hasEvent(ERROR, inputTurn.getEvents())) {
                 cause = wrap(inputTurn.getEvents().get(0).getMessage());
             }
         } catch (InterruptedException exception) {
             Thread.currentThread().interrupt();
             cause = wrap("Interrupted");
         } catch (Exception exception) {
            mDialogLog.error("Error during dialog", exception);
             cause = ResultUtils.toJson(exception);
         }
        mDialogLog.info("Ending dialog");
 
         // Build the JSON result returned to the calling application/context.
         JsonObjectBuilder resultObjectBuilder = JsonUtils.createObjectBuilder();
         resultObjectBuilder.add(CAUSE_PROPERTY, cause);
         VariableDeclarationList variables = VariableDeclarationList.create(resultObjectBuilder.build());
 
         return new VoiceXmlExitTurn("result", variables);
     }
 }
