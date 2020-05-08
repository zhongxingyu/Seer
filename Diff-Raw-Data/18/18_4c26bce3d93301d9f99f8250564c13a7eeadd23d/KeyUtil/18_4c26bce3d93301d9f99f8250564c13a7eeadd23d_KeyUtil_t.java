 package robot;
 import java.awt.AWTException;
 import java.awt.Robot;
 import java.awt.event.KeyEvent;
 
 
 public class KeyUtil {
 	protected Robot rb = null;
 	public KeyUtil() {
 		try {
 			rb = new Robot();
 			rb.setAutoDelay(10);
 		} catch (AWTException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void open(String appName) {
 		spotlightOpen();
 		inputString(appName);
 		keyPress(KeyEvent.VK_ENTER);
 		rb.delay(1000);
 	}
 	
 	public void spotlightOpen() {
 		keyPress(KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_SPACE);
 	}
 	
 	static String symbols = ",./;[]\\1234567890-=";
 	static String shift_symbols = "<>?:{}|!@#$%^&*()_+";
 	static int[] symbol_keys = {
 		KeyEvent.VK_COMMA,
 		KeyEvent.VK_PERIOD,
 		KeyEvent.VK_SLASH,
 		KeyEvent.VK_QUOTE,
 		KeyEvent.VK_QUOTEDBL,
 		KeyEvent.VK_BACK_SLASH,
 
 		KeyEvent.VK_BACK_QUOTE
 	};
 	
 	public void inputString(String sequence) {
 		int idx = -1;
 		for (int c: sequence.toCharArray()) {
 			// if (c < ' ' || '~' < c) continue;
 			if ('a' <= c && c <= 'z') {
 				keyPress(Character.toUpperCase(c));
 			} else if (('A' <= c && c <= 'Z') ) {
 				keyPress(KeyEvent.VK_SHIFT, c);
 			} else if (c == '\'') {
 				keyPress(KeyEvent.VK_QUOTE);
 			} else if (c == '"') {
 				keyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_QUOTE);
 			} else if (c == '`') {
 				keyPress(KeyEvent.VK_BACK_QUOTE);
 			} else if (c == '~') {
 				keyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_QUOTE);
 			} else if ((idx = shift_symbols.indexOf(c)) != -1) {
 				keyPress(KeyEvent.VK_SHIFT, symbols.charAt(idx));
 			} else {
 				keyPress(c);
 			}
 		}
 	}
 
 	public void keyPress(int... keys) {
 		for (int key : keys) {
 			rb.keyPress(key);
 		}
		rb.delay(10);
 		for (int key : keys) {
 			rb.keyRelease(key);
 		}
 	}
 	
 	public void delay() {
 		rb.delay(200);
 	}
 }
