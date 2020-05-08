 /*
  * Copyright (c) 2013 Nu Echo Inc. All rights reserved.
  */
 
 package com.nuecho.rivr.samples.voicemail.helpers;
 
 import org.hamcrest.*;
 
 import com.nuecho.rivr.core.channel.*;
 import com.nuecho.rivr.voicexml.test.*;
 import com.nuecho.rivr.voicexml.turn.output.*;
 
 /**
  * @author Nu Echo Inc.
  */
 public final class DialogueMatchers {
     private DialogueMatchers() {}
 
     /**
     * Match the interaction with the supplied name.
      * 
      * @param name The name of the interaction.
      */
     public static Matcher<Interaction> nameIs(String name) {
         return new InteractionNameMatcher(name);
     }
 
     /**
     * Match the last interaction turn made on the channel with the supplied
      * name.
      * 
      * @param name The expected name of the last interaction.
      */
     public static Matcher<VoiceXmlTestDialogueChannel> lastInteractionNameIs(String name) {
         return new DialogueChannelLastInteractionMatcher(nameIs(name));
     }
 
     /**
     * Match the last interaction turn made on the channel with a matcher.
      * 
      * @param matcher The matcher to apply to the last interaction.
      */
     public static Matcher<VoiceXmlTestDialogueChannel> lastInteraction(Matcher<Interaction> matcher) {
         return new DialogueChannelLastInteractionMatcher(matcher);
     }
 
     /**
     * Match a dialogue that its execution is complete, ie the dialogue
      * associated with it returned a {@link LastTurn}.
      */
     public static Matcher<VoiceXmlTestDialogueChannel> isDone() {
         return new DialogueChannelIsdoneMatcher();
     }
 }
