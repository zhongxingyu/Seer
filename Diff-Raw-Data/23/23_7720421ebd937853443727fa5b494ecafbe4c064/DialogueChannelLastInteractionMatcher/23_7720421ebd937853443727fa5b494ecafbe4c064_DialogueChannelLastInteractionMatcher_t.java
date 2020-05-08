 /*
 * Copyright (c) 2013 Nu Echo Inc. All rights reserved.
  */
 
 package com.nuecho.rivr.samples.voicemail.helpers;
 
 import org.hamcrest.*;
 
 import com.nuecho.rivr.voicexml.test.*;
 import com.nuecho.rivr.voicexml.turn.output.*;
 
 /**
  * Match the last interaction turn made on the channel with a matcher.
  * 
  * @author Nu Echo Inc.
  */
 public class DialogueChannelLastInteractionMatcher extends BaseMatcher<VoiceXmlTestDialogueChannel> {
     private final Matcher<Interaction> mTurnMatcher;
 
     public DialogueChannelLastInteractionMatcher(Matcher<Interaction> matcher) {
         mTurnMatcher = matcher;
     }
 
     @Override
     public boolean matches(Object arg0) {
         if (!(arg0 instanceof VoiceXmlTestDialogueChannel)) return false;
         VoiceXmlTestDialogueChannel channel = (VoiceXmlTestDialogueChannel) arg0;
 
         return mTurnMatcher.matches(channel.getLastInteraction());
     }
 
     @Override
     public void describeTo(Description arg0) {
         arg0.appendText("last interaction ").appendDescriptionOf(mTurnMatcher);
     }
 }
