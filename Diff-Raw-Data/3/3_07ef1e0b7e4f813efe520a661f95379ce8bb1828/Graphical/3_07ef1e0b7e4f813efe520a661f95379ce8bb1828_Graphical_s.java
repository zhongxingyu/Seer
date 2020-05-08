 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 
 public class Graphical extends JFrame{
 	
 	private static final long serialVersionUID = 8898424205695958845L;
 	private JLabel statusbar, devName;
 	private JPanel mousepanel;
 //	private JScrollPane scrollpane;
 	
 	public Graphical(){
 		super("ColorPicker");
 		mousepanel = new JPanel();
 		mousepanel.setLayout(new BorderLayout());
 		
 	//	mousepanel.setPreferredSize(new Dimension(getSize().width, getSize().height*2));
 	//	scrollpane = new JScrollPane(mousepanel);
 	//	scrollpane.setSize(getSize().width, getSize().height*2);
 		
 		add(mousepanel, BorderLayout.CENTER);
 		
 	//	add(scrollpane, BorderLayout.CENTER);
 
 		
 		statusbar = new JLabel("◄ HUE ►  ||  ▲ SATURATION ▼  ||  MouseWheelRotation/Scrolling: BRIGHTNESS  ||  Left_Click: RGB Value of Selected Color");
 	    statusbar.setHorizontalAlignment(SwingConstants.CENTER);
 	    
 	    devName = new JLabel("by:- Rahul Jain");
 	    devName.setHorizontalAlignment(SwingConstants.RIGHT);
 		
 		mousepanel.add(statusbar, BorderLayout.NORTH);
 		mousepanel.add(devName, BorderLayout.SOUTH);
 		addMouseListener(new Mouseclass());
 		
 		Mouseclass mc = new Mouseclass();
 		mousepanel.addMouseListener(mc);
 		mousepanel.addMouseMotionListener(mc);
 		mousepanel.addMouseWheelListener(mc);
 	}
 	
 	private class Mouseclass implements MouseListener, MouseMotionListener, MouseWheelListener{
 		
 		private float h, s;
 		private float b=0.5f;
 		
 		private static final int UP = 1;
 	    private static final int DOWN = 2;
 	    
 	    private float changeInB=0.0f;
 	    private int countWheelRotations=0;
 	    
 	    public void defaultColorChange(MouseEvent e, float b) {
 			h=(1/(float)getSize().width)*(float)e.getX();
 			s=(1/(float)getSize().height)*(float)e.getY();
 		//	int wi = getSize().width;
 		//	int he = getSize().height;
 			Color c = Color.getHSBColor(h,s,b);
 			mousepanel.setBackground(c);
 		//	statusbar.setText(String.format("%f %f %d %d %f %d", h, s, wi, he, changeInB, countWheelRotations));
 		}
 		
 		@Override
 		public void mouseMoved(MouseEvent e) {
 			defaultColorChange(e, b);
 		}
 
 		@Override
 		public void mouseWheelMoved(MouseWheelEvent e) {
 			countWheelRotations = e.getWheelRotation();
 	        int direction = (Math.abs(countWheelRotations) > 0) ? UP : DOWN;
 	        changeInB = (float)countWheelRotations/100;
 	        
 	        if (direction == UP) {
 	            b+=changeInB;
 	        } 
 	          else {
 	        	  b-=changeInB;
 	          }
 
 	        if (b < 0.0f) {
 	        	  b = 1.0f;
 	          }
 	          else if(b>1.0f) {
 	        	  b=0.0f;
 	          }
 	        
 	        defaultColorChange(e, b);
 		}
 		
 		@Override
 		public void mouseClicked(MouseEvent e) {
 			
 			int rgb = Color.HSBtoRGB(h, s, b);
 			Color properRGB = new Color(rgb);
 			
 			int redD=properRGB.getRed();
 			String redH = Integer.toHexString(redD);
 			redH=redH.toUpperCase();
 			int greenD=properRGB.getGreen();
 			String greenH = Integer.toHexString(greenD);
 			greenH=greenH.toUpperCase();
 			int blueD=properRGB.getBlue();
 			String blueH = Integer.toHexString(blueD);
 			blueH=blueH.toUpperCase();
 	//		int alphaD=properRGB.getAlpha();
 	//		String alphaH = Integer.toHexString(alphaD);
 	//		alphaH=alphaH.toUpperCase();
 			
 			String rgbColorString = String.format("RGB_Hex_Value: #%s%s%s\nRed: %d, %s\nGreen: %d, %s\nBlue: %d, %s", 
 									redH,greenH,blueH, redD,redH, greenD,greenH, blueD,blueH);
 
 	//		String hsbColorString = String.format("", args)
 			
 			JTextArea textArea = new JTextArea(rgbColorString);
 			String devInfo = "\n\n\t©SLX";
 			textArea.append(devInfo);
 			
 			textArea.setEditable(false);
 			textArea.setBackground(new Color(236,236,236));
 			ImageIcon icon = new ImageIcon("qwerty.png");
 			JOptionPane.showMessageDialog(null,
 										textArea,
 										"Color Info",
 										JOptionPane.INFORMATION_MESSAGE, 
 										icon );
 		}
 
 		@Override
 		public void mouseDragged(MouseEvent e) {
 	//		defaultColorChange(e);
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent e) {
 	//		defaultColorChange(e);		
 		}
 
 		@Override
 		public void mouseExited(MouseEvent e) {
 	//		defaultColorChange(e);
 		}
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 	//		defaultColorChange(e);
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 	//		defaultColorChange(e);
 		}
 		
 	}
 	
 }
 
 
 
 
