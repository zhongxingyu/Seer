 package org.jboss.pressgang.ccms.contentspec.processor.structures;
 
 public class ProcessingOptions {
 
     private boolean validate = false;
     private boolean ignoreChecksum = false;
     private boolean allowEmptyLevels = false;
     private boolean allowNewTopics = true;
     private boolean strictTitles = false;
     private boolean translation = false;
 
     public boolean isValidating() {
         return validate;
     }
 
     public void setValidating(boolean validating) {
         this.validate = validating;
     }
 
     public boolean isIgnoreChecksum() {
         return ignoreChecksum;
     }
 
     public void setIgnoreChecksum(boolean ignoreChecksum) {
         this.ignoreChecksum = ignoreChecksum;
     }
 
     public boolean isAllowEmptyLevels() {
         return allowEmptyLevels;
     }
 
     public void setAllowEmptyLevels(boolean allowEmptyLevels) {
         this.allowEmptyLevels = allowEmptyLevels;
     }
 
     public boolean isAllowNewTopics() {
         return allowNewTopics;
     }
 
     public void setAllowNewTopics(boolean allowNewTopics) {
         this.allowNewTopics = allowNewTopics;
     }
 
     public boolean isStrictTitles() {
         return strictTitles;
     }
 
     public void setStrictTitles(boolean strictTitles) {
         this.strictTitles = strictTitles;
     }
 
     public boolean isTranslation() {
         return translation;
     }
 
     public void setTranslation(boolean translation) {
         this.translation = translation;
     }
 }
