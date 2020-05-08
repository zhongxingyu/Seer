 package kkckkc.jsourcepad.util.action;
 
 import java.awt.Event;
 import java.awt.Toolkit;
 
 import javax.swing.KeyStroke;
 
 public class KeyStrokeUtils {
 
 	public static Object getKeyStroke(String value) {
 		if (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() == Event.CTRL_MASK) {
			value = value.replaceAll("virtual-alt", "alt");
			value = value.replaceAll("virtual-ctrl", "meta");
 			value = value.replaceAll("menu", "ctrl");
 		} else if (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() == Event.META_MASK) {
			value = value.replaceAll("virtual-alt", "alt");
			value = value.replaceAll("virtual-ctrl", "ctrl");
 			value = value.replaceAll("menu", "meta");
 		} else {
			value = value.replaceAll("virtual-alt", "meta");
			value = value.replaceAll("virtual-ctrl", "ctrl");
 			value = value.replaceAll("menu", "alt");
 		}
 		
 		return KeyStroke.getKeyStroke(value);
     }
 	
 }
