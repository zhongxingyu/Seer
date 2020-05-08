 package skillsplanner.gui.custom;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.border.Border;
 
 import skillsplanner.skills.SkillsTemplate;
 
 /**
  * A clickable panel with the option of storing a skill name. Used for Leveling up skills. Registers Right click as level down and left click as level up.
  *
  */
 public class ClickablePanel extends JPanel implements MouseListener{
 	private final int width = 250;
 	private final int height = 50;
 	Border raised;
 	Border lowered;
 	Border empty;
 	Color currentColor;
 	SkillsTemplate skill;
 	
 	public ClickablePanel(SkillsTemplate st){
 		this.setName(st.getName());
 		skill = st;
 		setDimensions();
 	}
 	
 	public ClickablePanel(){
 		setDimensions();
 	}
 	
 	/**
 	 * Sets the size for the clickable label as well as some other setup
 	 */
 	private void setDimensions(){
 		this.setMinimumSize(new Dimension(width,height));
 		//this.setPreferredSize(new Dimension(width,height));
 		this.setMaximumSize(new Dimension(Short.MAX_VALUE,height));
 		this.setToolTipText("Click to level up. Right click to level down");
 		
 		raised = BorderFactory.createRaisedBevelBorder();
 		lowered = BorderFactory.createLoweredBevelBorder();
 		empty = BorderFactory.createEtchedBorder(Color.white, Color.DARK_GRAY);
 		
 		//this.setBorder(lowered);
 		JLabel label = new JLabel(this.getName());
 		label.setVerticalTextPosition(JLabel.CENTER);
 		label.setForeground(Color.WHITE);
 		this.add(label);
 		
 		this.setCursor(new Cursor(Cursor.HAND_CURSOR));
 		this.addMouseListener(this);
 		
 		currentColor = new Color(0,0,0,220);
 		//this.setBackground(Color.BLACK);
 		this.setBackground(Color.WHITE);
 		this.setOpaque(false);
 		this.setBorder(empty);
 	}
 	
 	public void setSkillName(String skill){
 		this.setName(skill);
 	}
 	
 	public String getSkillName(){
 		return this.getName();
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		if(arg0.getButton() == MouseEvent.BUTTON1){
 			System.out.println("Left Click on "+this.getName());
 		}
 		else if(arg0.getButton() == MouseEvent.BUTTON2){
 			System.out.println("Middle Click on "+this.getName());
 		}
 		else if(arg0.getButton() == MouseEvent.BUTTON3){
 			System.out.println("Right Click" + this.getName());
 		}
 		
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 		currentColor = new Color(0,0,0,255); //full black
 		this.setBorder(raised);
 		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		currentColor = new Color(0,0,0,220); //slightly see through black
 		this.setBorder(empty);
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		this.setBorder(lowered);
 		
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		this.setBorder(raised);
 		
 	}
 	
 	/**
 	 * Ovverride the paint component to make a rounded look. Adapted from http://www.curious-creature.org/2007/08/01/rounded-corners-and-shadow-for-dialogs-extreme-gui-makeover/
 	 */
 	@Override
 	public void paintComponent(Graphics g){
 		//System.out.println("Paint called, color is :"+currentColor.toString()+ " " +currentColor.getAlpha());
 		int x = 2;
 		int y = 2;
 		int w = getWidth() - 4;
 		int h = getHeight() - 4;
 		int arc = 8;
 		
 		Graphics2D g2 = (Graphics2D) g.create();
 		
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 	            RenderingHints.VALUE_ANTIALIAS_ON);
 
 	    g2.setColor(currentColor);
 	    g2.fillRoundRect(x, y, w, h, arc, arc);
 
 	    g2.dispose();
 
 	}
 	
 	/**
 	 * Override the getPreferredSize method in order to define width and height
 	 */
 	@Override
 	public Dimension getPreferredSize(){
 		Dimension d = new Dimension(width,height);
 		
 		return d;
 	}
 
 }
