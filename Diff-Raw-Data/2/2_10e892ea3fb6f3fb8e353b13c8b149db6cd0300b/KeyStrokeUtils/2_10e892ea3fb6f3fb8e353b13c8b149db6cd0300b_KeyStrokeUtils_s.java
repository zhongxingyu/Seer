 package kkckkc.jsourcepad.util.ui;
 
 import javax.swing.*;
 import java.awt.event.KeyEvent;
 
 public class KeyStrokeUtils {
 
     public static boolean matches(KeyStroke keyStroke, KeyEvent ks) {
         if (keyStroke == null) return false;
 
 		char keyChar = ks.getKeyChar();
 		int keyCode = ks.getKeyCode();
 
 		if (keyChar < 0x20) {
 			if (keyChar != keyCode) {
 				keyChar += 0x40;
 
 				if ((keyChar >= 'A') && (keyChar <= 'Z')) {
 					keyChar += 0x20;
 				}
 			}
 		}
 
 		return
 			(keyStroke.getModifiers() & 0xF) == (ks.getModifiers() & 0xF) &&
 			keyCode != 0 &&
 			(keyStroke.getKeyCode() == keyCode ||
 					(keyChar != KeyEvent.CHAR_UNDEFINED && Character.toLowerCase(keyStroke.getKeyChar()) == keyChar) ||
                    (KeyEvent.getKeyText(keyCode).charAt(0) == keyStroke.getKeyChar()));
     }
 
 }
