 package de.lessvoid.nifty.controls.scrollbar.inputmapping;
 
 import de.lessvoid.nifty.input.NiftyInputEvent;
 import de.lessvoid.nifty.input.NiftyInputMapping;
 import de.lessvoid.nifty.input.keyboard.KeyboardInputEvent;
 
 public class ScrollbarInputMapping implements NiftyInputMapping {
 
   public NiftyInputEvent convert(final KeyboardInputEvent inputEvent) {
     if (inputEvent.isKeyDown()) {
       if (inputEvent.getKey() == KeyboardInputEvent.KEY_DOWN) {
         return NiftyInputEvent.MoveCursorDown;
       } else if (inputEvent.getKey() == KeyboardInputEvent.KEY_UP) {
         return NiftyInputEvent.MoveCursorUp;
       } else if (inputEvent.getKey() == KeyboardInputEvent.KEY_TAB) {
         if (inputEvent.isShiftDown()) {
           return NiftyInputEvent.PrevInputElement;
         } else {
           return NiftyInputEvent.NextInputElement;
         }
       }
     }
     return null;
   }
 }
