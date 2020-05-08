 /*
  * Copyright (c) 2013 Nu Echo Inc. All rights reserved.
  */
 
 package com.nuecho.rivr.samples.helloworld;
 
import static com.nuecho.rivr.voicexml.turn.input.VoiceXmlEvent.*;
import static com.nuecho.rivr.voicexml.turn.output.interaction.InteractionBuilder.*;
import static com.nuecho.rivr.voicexml.util.json.JsonUtils.*;

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

 /**
  * @author Nu Echo Inc.
  */
 public final class HelloWorldDialogue implements VoiceXmlDialogue {
 
     private static final String CAUSE_PROPERTY = "cause";
    private final Logger mDialogueLog = LoggerFactory.getLogger("hello-world");
 
     @Override
     public VoiceXmlLastTurn run(VoiceXmlFirstTurn firstTurn, VoiceXmlDialogueContext context) throws Exception {
        // The dialogue termination cause. "Normal" by default.
         JsonValue cause = wrap("Normal");
 
        mDialogueLog.info("Starting dialogue");
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
            mDialogueLog.error("Error during dialogue execution", exception);
             cause = ResultUtils.toJson(exception);
         }
        mDialogueLog.info("Ending dialogue");
 
         // Build the JSON result returned to the calling application/context.
         JsonObjectBuilder resultObjectBuilder = JsonUtils.createObjectBuilder();
         resultObjectBuilder.add(CAUSE_PROPERTY, cause);
         VariableDeclarationList variables = VariableDeclarationList.create(resultObjectBuilder.build());
 
         return new VoiceXmlExitTurn("result", variables);
     }
 }
