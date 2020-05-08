 package com.gui;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 public class SidePanel {
 
 	private JPanel top_panel = new JPanel();
 	private JPanel bottom_panel = new JPanel();
 
 	public SidePanel(){
		initUI();
 	}
 	
 	public final void initUI(){
 		initTopPanel();
 	}
 	
 	public final JPanel initTopPanel(){
 		JButton new_image = new JButton("New Image");
 		top_panel.setLayout(new BoxLayout(top_panel, BoxLayout.Y_AXIS));
 		top_panel.add(new_image);
 		return top_panel;
 	}
 	
 }
