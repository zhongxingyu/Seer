 package com.github.jvirtualkb;
 
 import java.awt.AWTException;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.lang.reflect.Method;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.WindowConstants;
import javax.swing.text.JTextComponent;
 
 /**
  * @author Fábio Gomes
  * 
  */
 public class VirtualKeyboard extends JDialog {
 
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 *
 	 */
 	public static final int MODE_ALPHANUMERIC = 0;
 	/**
 	 *
 	 */
 	public static final int MODE_ALPHABETIC = 1;
 	/**
 	 *
 	 */
 	public static final int MODE_NUMERIC = 2;
 
 	private static final char TK_TAB = '-';
 	private static final char TK_CAPS_LOCK = '>';
 	private static final char TK_SHIFT = '<';
 	private static final char TK_OK = '.';
 	private static final String KEYPAD = "-QWERTYUIOP>ASDFGHJKL\u2190<ZXCVBNM .";
 	private static final String NUMPAD = "6789234501\u2190.";
 
 	private Component source;
 	private Method eventProcessor;
 	private boolean sendEnterOnFinish;
 	private final Color clrBtnBackgrChk = new Color(53, 147, 194);
 	private Color clrBtnBackgrDef;
 	private Button btnTab;
 	private Button btnShift;
 	private Button btnCapsLock;
 
 	/**
 	 * 
 	 */
 	private final ActionListener keyAction = new ActionListener() {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			Button bt = (Button) e.getSource();
 			String txt = bt.getText();
 			if (bt == btnTab) {
 				sendKey('\t', KeyEvent.KEY_PRESSED);
 			} else if (bt == btnCapsLock) {
 				btnShift.release();
 				bt.toggle();
 			} else if (bt == btnShift) {
 				btnCapsLock.release();
 				bt.toggle();
 			} else if (txt.equals("\u2190")) {
 				sendKey('\b', KeyEvent.KEY_PRESSED);
 			} else if (txt.equals("OK")) {
 				setVisible(false);
 				if (sendEnterOnFinish) {
 					sendKey('\n', KeyEvent.KEY_PRESSED);
 				}
 			} else {
 				char ch = bt.getText().charAt(0);
 				if (btnShift != null && btnCapsLock != null) {
 					if (btnShift.isHeld()) {
 						btnShift.release();
 					} else if (!btnCapsLock.isHeld()) {
 						ch = Character.toLowerCase(ch);
 					}
 				}
 				sendKey(ch, KeyEvent.KEY_TYPED);
 			}
 		}
 	};
 
 	/**
 	 * @param source
 	 * @param mode
 	 * @throws AWTException
 	 */
 	public VirtualKeyboard(Component source, int mode) {
 		if (source == null) {
 			throw new NullPointerException("source cannot be null");
 		}
 		this.source = source;
 		this.eventProcessor = getEventProcessor(source.getClass());
 
 		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
 		setResizable(false);
 		setUndecorated(true);
 		getContentPane().setBackground(new Color(65, 92, 131));
 		setLayout(new FlowLayout());
 
 		JPanel p1, p2 = null;
 
 		if (mode != MODE_NUMERIC) {
 			p1 = new JPanel(new GridLayout(3, 1));
 			p1.setOpaque(false);
 			for (int i = 0; i < KEYPAD.length(); i++) {
 				if (i % 11 == 0) {
 					if (i > 0) {
 						p1.add(p2);
 					}
 					if (i == 22) {
 						p2 = new JPanel(new GridLayout(1, 11));
 					} else {
 						p2 = new JPanel(new GridLayout(1, 11));
 					}
 					p2.setOpaque(false);
 				}
 				char ch = KEYPAD.charAt(i);
 				Button btn = new Button(ch);
 				if (ch == TK_TAB) {
 					btnTab = btn;
 				} else if (ch == TK_CAPS_LOCK) {
 					btnCapsLock = btn;
 				} else if (ch == TK_SHIFT) {
 					btnShift = btn;
 				}
 				p2.add(btn);
 			}
 			p1.add(p2);
 			getContentPane().add(p1);
 		}
 
 		if (mode != MODE_ALPHABETIC) {
 			p1 = new JPanel(new GridLayout(0, 4));
 			p1.setOpaque(false);
 			for (int i = 0; i < NUMPAD.length(); i++) {
 				p1.add(new Button(NUMPAD.charAt(i)));
 			}
 			getContentPane().add(p1);
 		}
 
 		pack();
 		setLocationRelativeTo(null);
 		setAlwaysOnTop(true);
 	}
 
 	/**
 	 *
 	 */
 	@Override
 	public void setVisible(boolean b) {
 		int x = (int) source.getLocationOnScreen().getX();
 		int y = (int) (source.getLocationOnScreen().getY() + source.getSize().getHeight());
 		double xr = x + getSize().getWidth();
 		Dimension win = Toolkit.getDefaultToolkit().getScreenSize();
 		if (xr > win.getWidth()) {
 			x = (int) (x - (xr - win.getWidth()));
 		}
 		if (x < 0) {
 			x = 0;
 		}
 		setLocation(x, y);
		setCaretAtBeginning();
 		super.setVisible(b);
 	}
 
	private void setCaretAtBeginning() {
		if (source instanceof JTextComponent) {
			((JTextComponent) source).setCaretPosition(0);
		}
	}

 	public Component getSource() {
 		return source;
 	}
 
 	public void setSource(Component source) {
 		if (source == null) {
 			throw new NullPointerException("source cannot be null");
 		}
 		this.source = source;
 		this.eventProcessor = getEventProcessor(source.getClass());
 	}
 
 	public boolean isSendEnterOnFinish() {
 		return sendEnterOnFinish;
 	}
 
 	public void setSendEnterOnFinish(boolean sendEnterOnFinish) {
 		this.sendEnterOnFinish = sendEnterOnFinish;
 	}
 
 	/**
 	 * @author Fábio Gomes
 	 */
 	private class Button extends JButton {
 
 		private static final long serialVersionUID = 1L;
 
 		private Button(char ch) {
 			String val;
 			switch (ch) {
 			case TK_TAB:
 				val = "TAB";
 				break;
 			case TK_CAPS_LOCK:
 				val = "CAPS";
 				break;
 			case TK_SHIFT:
 				val = "SHIFT";
 				break;
 			case TK_OK:
 				val = "OK";
 				break;
 			default:
 				val = String.valueOf(ch);
 				break;
 			}
 			setText(val);
 			setPreferredSize(new Dimension(55, 50));
 			setFont(new Font("Sans-Serif", Font.BOLD, 14));
 			setMargin(new Insets(0, 0, 0, 0));
 			addActionListener(keyAction);
 			if (clrBtnBackgrDef == null) {
 				clrBtnBackgrDef = getBackground();
 			}
 			setOpaque(true);
 		}
 
 		private boolean isHeld() {
 			return getBackground().equals(clrBtnBackgrChk);
 		}
 
 		private void hold() {
 			setBackground(clrBtnBackgrChk);
 		}
 
 		private void release() {
 			setBackground(clrBtnBackgrDef);
 		}
 
 		private void toggle() {
 			if (isHeld()) {
 				release();
 			} else {
 				hold();
 			}
 		}
 
 	}
 
 	/**
 	 * @param cl
 	 */
 	private Method getEventProcessor(Class<?> cl) {
 		try {
 			Method m = cl.getDeclaredMethod("processKeyEvent", KeyEvent.class);
 			m.setAccessible(true);
 			return m;
 		} catch (NoSuchMethodException e) {
 			cl = cl.getSuperclass();
 			if (cl != null && !cl.equals(Object.class)) {
 				return getEventProcessor(cl);
 			} else
 				return null;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	/**
 	 * @param ch
 	 */
 	private void sendKey(char ch, int keyEvent) {
 		KeyEvent evt;
 		if (ch == '\b') {
 			evt = new KeyEvent(source, keyEvent, System.currentTimeMillis(), 0,
 					KeyEvent.VK_BACK_SPACE, '\b');
 		} else if (ch == '\t') {
 			evt = new KeyEvent(source, keyEvent, System.currentTimeMillis(), 0, KeyEvent.VK_TAB,
 					'\t');
 		} else if (ch == '\n') {
 			evt = new KeyEvent(source, keyEvent, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER,
 					'\n');
 		} else {
 			evt = new KeyEvent(source, keyEvent, System.currentTimeMillis(), 0,
 					KeyEvent.VK_UNDEFINED, ch);
 		}
 		try {
 			eventProcessor.invoke(source, evt);
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 	}
 
 }
