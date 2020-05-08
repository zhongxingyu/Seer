 package mapMaker;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 
 import javax.swing.JPanel;
 
 public class ControlsPanel extends JPanel {
 	private static final long serialVersionUID = 1L;
 	
 	private String blankButton	= "Blank (b)";
 	private String endButton 	= "End (e)";
 	private String grassButton 	= "Grass (g)";
 	private String startButton	= "Start (s)";
 	private String wallButton 	= "Wall (w)";
 	private String saveButton 	= "Save (C-S)";
 	private String deco1Button 	= "Deco1 (1)";
 	private String deco2Button 	= "Deco1 (2)";
 	private String deco3Button 	= "Deco1 (3)";
 	private String deco4Button 	= "Deco1 (4)";
 	private String deco5Button 	= "Deco1 (5)";
 	
 	public static String saveMessage = "", saveTime = "";
 	
 	private Font font1 = new Font("Arial", Font.PLAIN, 15);
 	
 	public ControlsPanel() {
 		setPreferredSize(new Dimension(3 * 32 + 6, Board.height));
 	}
 	
 	public void paint(Graphics g) {
 		super.paint(g);
 		
 		int ix = 0;
 		
 		g.setFont(font1);
 		g.setColor(Color.black);
 		g.drawString(blankButton, 	5, ix += 25);
 		g.drawString(endButton,   	5, ix += 25);
 		g.drawString(grassButton, 	5, ix += 25);
 		g.drawString(startButton, 	5, ix += 25);
 		g.drawString(wallButton,  	5, ix += 25);
 		g.drawString(deco1Button, 	5, ix += 50);
 		g.drawString(deco2Button, 	5, ix += 25);
 		g.drawString(deco3Button, 	5, ix += 25);
 		g.drawString(deco4Button, 	5, ix += 25);
 		g.drawString(deco5Button, 	5, ix += 25);
 		
 		g.drawString(saveButton,  	5, ix += 50);
 		
 		g.drawString(saveMessage,  	5, ix += 50);
 		g.drawString(saveTime,  	5, ix += 20);
 
		repaint();
 	}
 }
