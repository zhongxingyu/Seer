 package gfx;
 
 import inputhandler.Action;
 import java.awt.Color;
 import java.awt.TextField;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.TextEvent;
 import java.awt.event.TextListener;
 import java.util.ArrayList;
 
 public class MenuField extends TextField {
 
 	public enum FieldType {
 		TEXT, IP, KEY;
 	}
 
 	public String msg;
 	private FieldType type;
 	private int colums;
 	public Action action;
 
 	public static ArrayList<MenuField> menuFields = new ArrayList<MenuField>();
 
 	public MenuField(int colums, final FieldType type, Action action) {
 		this.colums = colums;
 		this.type = type;
 		this.action = action;
 		menuFields.add(this);
 		setUp();
 	}
 
 	public MenuField(int colums, FieldType type) {
 		this.colums = colums;
 		this.type = type;
 		setUp();
 	}
 
 	public void setUp() {
 		setColumns(colums);
 		addTextListener(new TextListener() {
 			@Override
 			public void textValueChanged(TextEvent arg0) {
 				msg = getText();
 				System.out.println(msg);
 				if (type == FieldType.IP
 						&& msg.matches("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$")) {
 					setBackground(Menu.green);
 				} else if (type == FieldType.TEXT) {
 					if (msg.matches("[A-Za-z0-9]{1,9}"))
 						setBackground(Menu.green);
 					else
 						setBackground(Menu.pink);
 				} else if (type == FieldType.KEY) {
 					msg = msg.toUpperCase();
 					if (((getText().length() == 0 && msg.length() != 0) || msg.length() > 1) && !msg.matches("LEFT|RIGHT|UP|DOWN")) {
 						setText(Character.toString(msg.charAt(0)));
 					}
 					if (!getText().matches("LEFT|RIGHT|UP|DOWN") && msg.matches("LEFT|RIGHT|UP|DOWN"))
						setText(msg);
 				}
 
 				if (msg.length() == 0)
 					setBackground(Color.white);
 				System.out.println(msg.length());
 			}
 		});
 		addFocusListener(new FocusListener() {
 			private String tmpMsg;
 
 			@Override
 			public void focusLost(FocusEvent arg0) {
 				if (getText().length() == 0 && tmpMsg.length() != 0)
 					setText(tmpMsg);
 			}
 
 			@Override
 			public void focusGained(FocusEvent arg0) {
 				tmpMsg = getText();
 				setText("");
 			}
 		});
 		addKeyListener(new KeyListener() {
 
 			@Override
 			public void keyTyped(KeyEvent arg0) {
 			}
 
 			@Override
 			public void keyReleased(KeyEvent arg0) {
 			}
 
 			@Override
 			public void keyPressed(KeyEvent arg0) {
 				if (arg0.getKeyCode() == KeyEvent.VK_LEFT)
 					setText("LEFT");
 				if (arg0.getKeyCode() == KeyEvent.VK_RIGHT)
 					setText("RIGHT");
 				if (arg0.getKeyCode() == KeyEvent.VK_DOWN)
 					setText("DOWN");
 				if (arg0.getKeyCode() == KeyEvent.VK_UP)
 					setText("UP");
 			}
 		});
 	}
 
 	public void reset() {
 		setText(KeyEvent.getKeyText((action.getBind())));
 		setBackground(Color.WHITE);
 	}
 }
