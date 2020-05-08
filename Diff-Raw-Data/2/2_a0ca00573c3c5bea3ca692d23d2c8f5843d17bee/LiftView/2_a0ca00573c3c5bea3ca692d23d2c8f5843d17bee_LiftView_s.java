 /**
  * @author Nicolas
  */
 
 package lift;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 @SuppressWarnings("serial")
 public class LiftView extends JPanel {
 	
 	private Dimension cage = new Dimension(Lift.CAGE);
 	protected static final int gutter = 2;
 	private final int gutterButton = 5;
 	private int position = 0;
 	
 	/**
 	 * Constructor which needs the buttons to display
 	 * @param button
 	 */
 	public LiftView(JButton[] button) {
 		setLayout(null);
 		setBackground(Color.WHITE);
 		
 		this.initButtons(button);
		JLabel l = new JLabel("Ascenceur");
 		l.setBounds(Lift.WIDTH()/2 - 30, 10, 80, 20);
 		this.add(l);
 	}
 	
 	/**
 	 * Display the buttons
 	 * @param button
 	 */
 	private void initButtons(JButton[] button) {
 		for (int i=0; i<Lift.FLOOR(); i++) {
 			JButton b = button[i];
 			b.setBounds(Lift.WIDTH()/2 + cage.width/2 + 10*gutterButton, Lift.HEIGHT()/2 - (cage.height * Lift.FLOOR())/2 + (Lift.FLOOR()-i-1)*cage.height + cage.height/2 + gutter - b.getPreferredSize().height/2, b.getPreferredSize().width, b.getPreferredSize().height);
 			this.add(b);
 		}
 	}
 	
 	/**
 	 * Repaint lift after got the new position
 	 * @param value
 	 */
 	public void changePosition(int value) {
 		this.position = value;
 		repaint();
 	}
 	
 	@Override
 	/**
 	 * Paint component
 	 * @param graphics
 	 */
 	protected void paintComponent(Graphics graphics) {
 		super.paintComponent(graphics);		
 		
 		graphics.setColor(Color.BLACK);
 		graphics.drawLine(Lift.WIDTH()/2 - cage.width/2 - gutter, Lift.HEIGHT()/2 - (cage.height * Lift.FLOOR())/2, Lift.WIDTH()/2 - cage.width/2 - gutter, Lift.HEIGHT()/2 + (cage.height * Lift.FLOOR())/2);
 		graphics.drawLine(Lift.WIDTH()/2 + cage.width/2 + gutter, Lift.HEIGHT()/2 - (cage.height * Lift.FLOOR())/2, Lift.WIDTH()/2 + cage.width/2 + gutter, Lift.HEIGHT()/2 + (cage.height * Lift.FLOOR())/2);
 		graphics.drawLine(Lift.WIDTH()/2 - cage.width/2 - gutter, Lift.HEIGHT()/2 - (cage.height * Lift.FLOOR())/2, Lift.WIDTH()/2 + cage.width/2 + gutter, Lift.HEIGHT()/2 - (cage.height * Lift.FLOOR())/2);
 		graphics.drawLine(Lift.WIDTH()/2 - cage.width/2 - gutter, Lift.HEIGHT()/2 + (cage.height * Lift.FLOOR())/2, Lift.WIDTH()/2 + cage.width/2 + gutter, Lift.HEIGHT()/2 + (cage.height * Lift.FLOOR())/2);
 		graphics.setColor(Color.GRAY);
 		for (int i=1; i<Lift.FLOOR(); i++) {
 			graphics.drawLine(Lift.WIDTH()/2 - cage.width/2 - gutter, Lift.HEIGHT()/2 - (cage.height * Lift.FLOOR())/2 + cage.height*i, Lift.WIDTH()/2 - cage.width/2 + gutter, Lift.HEIGHT()/2 - (cage.height * Lift.FLOOR())/2 + cage.height*i) ;
 			graphics.drawLine(Lift.WIDTH()/2 + cage.width/2 + gutter, Lift.HEIGHT()/2 - (cage.height * Lift.FLOOR())/2 + cage.height*i, Lift.WIDTH()/2 + cage.width/2 - gutter, Lift.HEIGHT()/2 - (cage.height * Lift.FLOOR())/2 + cage.height*i) ;
 		}
 		
 		paintLift(graphics);
 	}
 	
 	/**
 	 * Paint lift cage
 	 * @param graphics
 	 */
 	private void paintLift(Graphics graphics) { 
 		graphics.setColor(Color.BLACK);
 		int x = Lift.WIDTH()/2 - cage.width/2;
 		int y = Lift.HEIGHT()/2 - (cage.height * Lift.FLOOR())/2 + cage.height*(Lift.FLOOR()-1) + gutter - this.position;
 		graphics.drawRect(x, y, cage.width, cage.height - 2*gutter);
 		
 		graphics.setColor(Color.ORANGE);
 		graphics.fillRect(x + 1, y + 1, cage.width - 1, cage.height - 2*gutter - 1);
 		
 		graphics.setColor(Color.GRAY);
 		graphics.drawLine(x + cage.width/2, y + 1, x + cage.width/2, y + cage.height - gutter - 3);
 	}	
 
 }
