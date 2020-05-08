 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTextField;
 import javax.swing.WindowConstants;
 
 import net.miginfocom.swing.MigLayout;
 
 public class OttoFrame extends JFrame {
 	private static final long serialVersionUID = 911078009934658231L;
 	
 	ImageArea ia = new ImageArea(this);
 
 	private JButton capture_farm = new JButton("capture");
     
 	private JButton locate_farm = new JButton("locate");
 	// TODO: remove these defaults -- should come from model.
 	JLabel farm_location = new JLabel("0,0") ;
 	
 	private JButton locate_coop = new JButton("locate");
 	JLabel coop_location = new JLabel("0,0") ;
 	
 	private JButton locate_primers = new JButton("locate");
 	// TODO: *_location are now invisible data stores for the points.
 	// change to j.a.Point or ruby array or something non-ui.
 	// Something that doesn't require unparsing and reparsing ints, hm? 
 	JLabel primers_location = new JLabel("0,0") ;
	private JTextField primers_x = new JTextField(3);
	private JTextField primers_y = new JTextField(3);
 
 	private JButton locate_premiums = new JButton("locate");
 	JLabel premiums_location = new JLabel("0,0") ;
	private JTextField premiums_x = new JTextField(3);
	private JTextField premiums_y = new JTextField(3);
 	
 	private JTextField white_premiums = new JTextField(3);
 	private JTextField brown_premiums = new JTextField(3);
 	private JTextField black_premiums = new JTextField(3);
 	private JTextField golden_premiums = new JTextField(3);
 	
 	public void setImage(BufferedImage img) {
 		ia.setImage(img);
 	}
 	
 	public OttoFrame() {
 		this.setTitle("Farmer Otto");
		String constraints = ""; // = "debug";
 		JPanel panel = 
 			new JPanel(
 				new MigLayout(
 						constraints, "20[right][right][right][right]10"));
 
 		addSeparator(panel, "capture farm");
 		panel.add(capture_farm, "wrap para");
 		
 		addSeparator(panel, "locate farm");
 		panel.add(locate_farm);		
 		panel.add(new JLabel("top left of farm area"), "span 3, wrap para");
 	
 		addSeparator(panel, "coop");
 		panel.add(locate_coop);
 		panel.add(new JLabel("middle bottom nest"), "span 3, wrap para");
 
 		addSeparator(panel, "primer pen");
 		panel.add(locate_primers);
 		panel.add(new JLabel("back left chicken eye"), "span 3, wrap");
 		panel.add(new JLabel("rows"), "split 2");
		panel.add(primers_x);
 		panel.add(new JLabel("cols"), "split 2");
		panel.add(primers_y, "wrap para");

 		
 		addSeparator(panel, "premium pen");
 		panel.add(locate_premiums);
 		panel.add(new JLabel("back left chicken eye"), "span 2, wrap");
 		panel.add(new JLabel("rows"), "split 2");
		panel.add(premiums_x);
 		panel.add(new JLabel("cols"), "split 2");		
		panel.add(premiums_y, "wrap para");		
 		
 		addSeparator(panel, "premium colors");
 		panel.add(new JLabel("white"), "split 2");
 		panel.add(white_premiums);
 		panel.add(new JLabel("brown"), "split 2");
 		panel.add(brown_premiums, "wrap");
 		
 		panel.add(new JLabel("black"), "split 2");
 		panel.add(black_premiums);
 		panel.add(new JLabel("golden"), "split 2");
 		panel.add(golden_premiums, "wrap para");
 		
 		addSeparator(panel, "fill in stuff above first");
 		JButton tend = new JButton("tend coop");
 		tend.setEnabled(false);
 		panel.add(tend);
 		JButton stop = new JButton("stop");
 		stop.setEnabled(false);
 		panel.add(stop, "wrap para");
 		panel.add(new JLabel("turn on CAPS LOCK to pause, then click stop"), "span 2, wrap");
 		panel.add(new JLabel("or turn off CAPS LOCK to continue"), "span 2, wrap para");
 		
 		JScrollPane scroller = new JScrollPane(ia) ;
 		scroller.setPreferredSize (new Dimension (880,660));
 		panel.add(scroller, "dock east");
 
 		getContentPane().add(panel);
 		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 		pack();
 	}
 	
 	private static void addSeparator(JPanel panel, String text) {
 		JLabel l = new JLabel(text);
 		l.setForeground(new Color(0, 70, 213));
 		panel.add(l, "gapbottom 1, span, split 2, aligny center, gaptop 10");
 		panel.add(new JSeparator(), "gapleft rel, growx, gaptop 10");
 	}
 
 
 	public static void main(String[] args) {
 		new OttoFrame().setVisible(true);
 	}
 
 }  // class OttoFrame
 
 class ImageArea extends JPanel {
 	private static final long serialVersionUID = 2402431474843357440L;
 
 	private Image image;
 	private OttoFrame frame;
 	
 	BasicStroke wideStroke = new BasicStroke(4.0f);
 	BasicStroke narrowStroke = new BasicStroke(2.0f);
 	
 	public ImageArea(OttoFrame frame) {
 		super();
 		this.frame = frame;
 	}
 	
 	public void paintComponent (Graphics g1) {
 		// Repaint the component's background.
 		super.paintComponent (g1);
 		Graphics2D g = (Graphics2D) g1; 
 		
 		// If an image has been defined, draw that image using the Component
 		// layer of this ImageArea object as the ImageObserver.
 		if (image != null)
 			g.drawImage (image, 0, 0, this);
 		
 		drawSpot(g, "farm", frame.farm_location.getText());
 		drawSpot(g, "coop", frame.coop_location.getText());
 		drawSpot(g, "primers", frame.primers_location.getText());
 		drawSpot(g, "premiums", frame.premiums_location.getText());
 	}
 
 	private void drawSpot(Graphics2D g, String item, String xy) {
 		if (xy.length() < 3) return;
 		// TODO: check for parsing exceptions.
 		int x = Integer.parseInt(xy.split(",")[0]);
 		if (0 == x) return;
 		int y = Integer.parseInt(xy.split(",")[1]);
 		int radius = 16;
 		int half   = 8;
 		
 		g.setStroke(wideStroke);
 		g.setColor(Color.red) ;
 		g.drawOval(x-half, y-half, radius, radius);
 		
 		// draw an "X" through the circle to mark the center.
 		// Maybe just draw a big X instead of the circle?
 		g.setStroke(narrowStroke);
 		g.drawLine(x+3-half, y+3-half, x+radius-3-half, y+radius-3-half);
 		g.drawLine(x+radius-3-half, y+3-half, x+3-half, y+radius-3-half);
 		
 		// draw a label for the circle
 		
 		g.setColor(Color.white);
 		g.fillRect(x+radius-20, y+radius-6, 80, 16);
 		
 		g.setColor(Color.blue);
 		g.drawString(item, x, y+radius+7);
 	}
 
 	
 	public void setImage(Image image)
 	{
 		// Save the image for later repaint.
 		this.image = image;
 
 		// Set this panel's preferred size to the image's size, to influence the
 		// display of scroll bars.
 		setPreferredSize(new Dimension (image.getWidth(this),
 				image.getHeight(this)));
 
 		// Present scroll bars as necessary.
 		revalidate ();
 
 		// Update the image displayed on the panel.
 		repaint ();
 	}
 
 }  // class ImageArea
