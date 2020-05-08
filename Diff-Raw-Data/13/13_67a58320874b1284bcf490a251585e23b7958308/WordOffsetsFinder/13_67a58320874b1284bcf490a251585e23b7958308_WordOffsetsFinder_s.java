 package org.hunmr.acejump.offsets;
 
import java.awt.event.KeyEvent;

 public class WordOffsetsFinder extends OffsetsFinder {
     @Override
     protected boolean isValidOffset(char c, String visibleText, int index, int offset, int caretOffset) {
        if (c == KeyEvent.VK_SPACE || c == ',') {
             return true;
         }
 
         boolean isBeginningChar = index == 0;
         if (isBeginningChar) {
             return true;
         }
 
         char charBeforeOffset = visibleText.charAt(index - 1);
        if (!Character.isLetterOrDigit(charBeforeOffset)) {
             return true;
         }
 
         char charAtOffset = visibleText.charAt(index);
         if (Character.isUpperCase(charAtOffset) && Character.isLowerCase(charBeforeOffset)) {
             return true;
         }
 
         return false;
     }
 }
