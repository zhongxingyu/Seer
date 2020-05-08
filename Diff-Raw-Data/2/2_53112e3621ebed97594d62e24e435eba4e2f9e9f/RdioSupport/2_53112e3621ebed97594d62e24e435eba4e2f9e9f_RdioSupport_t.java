 package com.sleazyweasel.applescriptifier;
 
 public class RdioSupport implements ApplicationSupport {
     private final AppleScriptTemplate appleScriptTemplate;
 
     public RdioSupport(AppleScriptTemplate appleScriptTemplate) {
         this.appleScriptTemplate = appleScriptTemplate;
     }
 
     public void playPause() {
         appleScriptTemplate.executeKeyStroke(Application.RDIO, " ");
     }
 
     public void next() {
         appleScriptTemplate.executeKeyCode(Application.RDIO, AppleScriptTemplate.RIGHT_ARROW);
     }
 
     public void previous() {
        appleScriptTemplate.executeKeyCode(Application.RDIO, AppleScriptTemplate.LEFT_ARROW);
     }
 
     public void thumbsUp() {
     }
 
     public void thumbsDown() {
     }
 }
