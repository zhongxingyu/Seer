 package com.id.events;
 
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class KeyStroke {
   private static Map<Character, Character> shiftSymbols = new HashMap<Character, Character>();
   static {
     shiftSymbols.put('!', '1');
     shiftSymbols.put('@', '2');
     shiftSymbols.put('#', '3');
     shiftSymbols.put('$', '4');
     shiftSymbols.put('%', '5');
     shiftSymbols.put('^', '6');
     shiftSymbols.put('&', '7');
     shiftSymbols.put('*', '8');
     shiftSymbols.put('(', '9');
     shiftSymbols.put(')', '0');
   }
 
   public static KeyStroke fromChar(char c) {
     if (shiftSymbols.containsKey(c)) {
      return new KeyStroke(shiftSymbols.get(c), KeyEvent.SHIFT_MASK);
     }
     int mask = 0;
     if (Character.isUpperCase(c)) {
       mask = KeyEvent.SHIFT_MASK;
     }
     return new KeyStroke(c, mask);
   }
 
   public static KeyStroke fromVKey(int keyCode) {
     return new KeyStroke((char) keyCode, 0);
   }
 
   public static KeyStroke escape() {
     return KeyStroke.fromVKey(KeyEvent.VK_ESCAPE);
   }
 
   public static KeyStroke fromControlChar(char c) {
     return new KeyStroke(c, KeyEvent.CTRL_MASK);
   }
 
   private final char letter;
   private final int modifiers;
 
   public KeyStroke(char letter, int modifiers) {
     this.modifiers = modifiers;
     this.letter = isShiftDown() ? Character.toUpperCase(letter) : Character
         .toLowerCase(letter);
   }
 
  public static KeyStroke fromKeyEvent(KeyEvent event) {
    char c = (char) event.getKeyCode();
    if (event.isControlDown() && !event.isShiftDown()) {
      c = Character.toLowerCase(c);
    }
    return new KeyStroke(c, event.getModifiers());
  }

   public boolean isShiftDown() {
     return (modifiers & KeyEvent.SHIFT_MASK) != 0;
   }
 
   public boolean isControlDown() {
     return (modifiers & KeyEvent.CTRL_MASK) != 0;
   }
 
   public boolean isAltDown() {
     return (modifiers & KeyEvent.ALT_MASK) != 0;
   }
 
   public boolean isMetaDown() {
     return (modifiers & KeyEvent.META_MASK) != 0;
   }
 
   public char getLetter() {
     return letter;
   }
 
   public int getKeyCode() {
     return Character.toUpperCase(letter);
   }
 
   public char getKeyChar() {
     return getLetter();
   }
 
   public boolean isLetter() {
     return isKeyCodeForLetter(getKeyCode());
   }
 
   @Override
   public String toString() {
     StringBuffer buffer = new StringBuffer();
     buffer.append("KeyStroke[");
     buffer.append(getLetter()).append(", ");
     maybeAppend(buffer, "SHIFT", isShiftDown());
     maybeAppend(buffer, "CTRL", isControlDown());
     maybeAppend(buffer, "ALT", isAltDown());
     maybeAppend(buffer, "META", isMetaDown());
     buffer.append("]");
     return buffer.toString();
   }
 
   @Override
   public boolean equals(Object obj) {
     if (obj instanceof KeyStroke) {
       KeyStroke other = (KeyStroke) obj;
       return letter == other.letter && modifiers == other.modifiers;
     }
     return false;
   }
 
   @Override
   public int hashCode() {
     return letter * (modifiers + 1);
   }
 
   private void maybeAppend(StringBuffer buffer, String string, boolean append) {
     if (append) {
       buffer.append(string).append(", ");
     }
   }
 
   private static boolean isKeyCodeForLetter(int keyCode) {
     return ('a' <= keyCode && keyCode <= 'z')
         || ('A' <= keyCode && keyCode <= 'Z')
         || ('0' <= keyCode && keyCode <= '9')
        || (" `~!@#$%^&*()-_=+[{]}\\|;:,<.>/?".indexOf(keyCode) != -1)
        || keyCode == 39 /* single quote */|| keyCode == 222 /* double quote */;
   }
 
   public static List<KeyStroke> fromString(String string) {
     List<KeyStroke> result = new ArrayList<KeyStroke>();
     for (int i = 0; i < string.length(); i++) {
       result.add(KeyStroke.fromChar(string.charAt(i)));
     }
     return result;
   }
 
   public static KeyStroke backspace() {
     return KeyStroke.fromVKey(KeyEvent.VK_BACK_SPACE);
   }
 
   public static KeyStroke enter() {
     return KeyStroke.fromVKey(KeyEvent.VK_ENTER);
   }
 
   public static KeyStroke tab() {
     return KeyStroke.fromVKey(KeyEvent.VK_TAB);
   }
 }
