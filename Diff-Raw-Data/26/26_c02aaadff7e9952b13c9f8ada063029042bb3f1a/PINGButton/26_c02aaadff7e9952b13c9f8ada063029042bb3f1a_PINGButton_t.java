package com.hackathon.ping.objects;
 
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import java.awt.Color;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 public class PINGButton extends JButton {
 	
 	private final Color defaultColor = Color.WHITE;
 	private final Color hoverColor   = Color.LIGHT_GRAY;
 	
 	/**
 	 * Keeping Eclipse Happy
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public PINGButton(String text) {
 		this(new ImageIcon(), text);
 	}
 	
 	public PINGButton(Icon icon) {
 		this(icon, "");
 	}
 	
 	public PINGButton(Icon icon, String text) {
 		super(text, icon);
 		setBackground(defaultColor);
 		addMouseListener(new MouseAdapter() {
 
 			@Override
 			public void mouseEntered(MouseEvent a) {
 				doMouseEnt();
 			}
 
 			@Override
 			public void mouseExited(MouseEvent a) {
 				doMouseEx();
 			}
 			
 		});
 		this.setBorderPainted(false);
 	}
 	
 	
 	private void doMouseEx() {
 		this.setBackground(defaultColor);
 	}
 	
 	private void doMouseEnt() {
 		this.setBackground(hoverColor);
 	}
 }
