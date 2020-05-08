 package turing.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Polygon;
 
 import javax.swing.BorderFactory;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.SwingConstants;
 
 import turing.interfaces.GUI;
 
 public class TapeHead extends JComponent {
 
 	int[] xPoints;
 	int[] yPoints;
 	int nPoints;
 	Polygon headArrow;
 	
 	GUI gui;
 	
 	/**
 	 * Creates a new Tape Head.
 	 * @param gui The GUI for the Turing machine simulator.
 	 */
 	public TapeHead(GUI gui)
 	{
 		this.gui=gui;
 		
 		init();
 		
 		if(TuringGUI.DEBUG)
 		{
 			this.setBackground(Color.GREEN);
 			this.setOpaque(true);
 		}
 	}
 	
 	/**
 	 * Initializes the style for the tape head.
 	 */
 	private void init()
 	{
 		this.resizeTapeHead();
 		
 		this.setForeground(Color.WHITE);
 	}
 	
 	public void resizeTapeHead()
 	{
 		int w = (GUI.TAPE_HEAD_FONT.getSize()*6/5)+(GUI.TAPE_HEAD_FONT.getSize()*7/16)*(""+gui.getSimulator().getCurrentState()).length()+(GUI.TAPE_HEAD_FONT.getSize()*2/5);
 		int h = (int)(GUI.TAPE_HEAD_FONT.getSize()*2);
 		this.setMinimumSize(new Dimension(w,h));
 		this.setPreferredSize(new Dimension(w,h));
 		this.setMaximumSize(new Dimension(w,h));
		this.setSize(new Dimension(w,h));
 	}
 	
 	@Override
 	public void paintComponent(Graphics g)
	{	
 		resizeTapeHead();
 		
 		//Javadoc requests that if isOpaque is true, the background be filled.
 		//As we are not using super's implementation of paintComponent,
 		//This code fills the background if isOpaque is true.
 		if(this.isOpaque())
 		{
 			g.setColor(this.getBackground());
 			g.fillRect(0, 0, this.getWidth(), this.getHeight());
 		}
 		
 		g.setColor(Color.BLACK);
 		
 		//Fill the rectangle containing the text:
 		g.fillRect(0, this.getHeight()-(int)(GUI.TAPE_HEAD_FONT.getSize()*1.25), this.getWidth(), (int)(GUI.TAPE_HEAD_FONT.getSize()*1.25));
 		
 		//Initializing the point of the tape head.
 		//If this is called in init, getWidth returns 0
 		//As the width has been set when this method is called, I put this code here.
 		xPoints = new int[] {0,this.getWidth()/2,this.getWidth()};
 		yPoints = new int[] {this.getHeight()-(int)(GUI.TAPE_HEAD_FONT.getSize()*1.25),0,this.getHeight()-(int)(GUI.TAPE_HEAD_FONT.getSize()*1.25)};
 		nPoints = 3;
 		headArrow = new Polygon(xPoints,yPoints,nPoints);
 		
 		g.fillPolygon(headArrow);
 		
 		//Variables for text position:
 		String stateString = ""+gui.getSimulator().getCurrentState();
 		int tapeHeadFontSize = GUI.TAPE_HEAD_FONT.getSize();
 		Font TAPE_HEAD_SUBSCRIPT_FONT = new Font(GUI.TAPE_HEAD_FONT.getFamily(),GUI.TAPE_HEAD_FONT.getStyle(),(GUI.TAPE_HEAD_FONT.getSize()*3)/4);
 		int tapeHeadSubscriptFontSize = TAPE_HEAD_SUBSCRIPT_FONT.getSize();
 		int horizontalCenter = this.getWidth()/2;
 		
 		//Text position:
 		g.setColor(Color.WHITE);
 		g.setFont(GUI.TAPE_HEAD_FONT);
 		g.drawString("S", (tapeHeadFontSize*2/5),this.getHeight()-(tapeHeadFontSize/2));
 		g.setFont(TAPE_HEAD_SUBSCRIPT_FONT);
 		g.drawString(stateString, (tapeHeadFontSize*6/5), this.getHeight()-(tapeHeadSubscriptFontSize/2));
 	}
 	
 	/**
 	 * Updates the tape head to the current state of the Turing machine.
 	 * <p>
 	 * Call this method every time the GUI's <code>update</code> method is called.
 	 * @see GUI#update() 
 	 */
 	public void update()
 	{
 		repaint();
 	}
 }
