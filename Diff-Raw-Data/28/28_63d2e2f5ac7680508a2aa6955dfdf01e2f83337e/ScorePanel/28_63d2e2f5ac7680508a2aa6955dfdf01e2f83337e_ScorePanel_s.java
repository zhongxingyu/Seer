 package de.phaenovum.view;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.FlowLayout;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 import javax.swing.border.Border;
 
 public class ScorePanel extends JPanel{
 	private static final int GAP = 10;
 	private ColorPanel panel = new ColorPanel(5);
 	private Border border;
 
 
 	public ScorePanel(){
 		border = BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP);
 		this.setBorder(border);
 		panel.setLayout(new BorderLayout());
 		panel.setLayout(new FlowLayout());
 		this.add(panel);
 	}
 
 	public Component addTeam(Component comp){
 		return panel.add(comp);
 	}
 
 }
