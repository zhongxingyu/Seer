 package edu.brown.cs32.MFTG.gui.properties;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.text.NumberFormat;
 
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 
 import edu.brown.cs32.MFTG.gui.Constants;
 import edu.brown.cs32.MFTG.gui.Constants.Orientation;
 import edu.brown.cs32.MFTG.gui.Constants.Properties;
 import edu.brown.cs32.MFTG.gui.Helper;
 import edu.brown.cs32.MFTG.gui.Houses;
 
 public abstract class NonstaticProperty extends PropertyPanel {
 
 	/* Game results and Heuristics */
 	private double _timeOwned;
 	private JLabel _profitField;
 	private double _profit;
 	private double _profitFactor = 200.;
 	
 	/* Percentage owned and profit rectangle */
 	protected Rectangle _profitBlock;
 	
 	
 	public NonstaticProperty(Properties myProperty) {
 		super(myProperty);
 		//TODO: take out preassignment
		_timeOwned = .5;
		_profit = 10000;
 		
 		initializeProfitBlock();
 		initializeProfitField();
 	}
 	
 	protected void moveProfitField (double x, double y) {
 		_profitField.setLocation((int) x, (int) y);
 	}
 	
 	public double getTimeOwned() {
 		return _timeOwned;
 	}
 	
 	public double getProfit() {
 		return _profit;
 	}
 	
 	public void updateProfit (double profit) {
 		_profit = profit;
 		NumberFormat numFormat = NumberFormat.getCurrencyInstance();
 		numFormat.setMaximumFractionDigits(0);
 		_profitField.setText(numFormat.format(_profit));
 	}
 	
 	public void updateTimeOwned(double timeOwned) {
 		_timeOwned = timeOwned;
 		_profitBlock.setSize(Constants.WIDTH, (int) (Constants.HEIGHT * _timeOwned));
 	}
 	
 	public void initializeProfitBlock () {
 		_profitBlock = new Rectangle();
 		_profitBlock.setSize(Constants.WIDTH, (int) (Constants.HEIGHT * _timeOwned));
 		_profitBlock.setLocation(0, 0);
 	}
 
 	public void initializeProfitField () {
 		_profitField = new JLabel();
 		NumberFormat numFormat = NumberFormat.getCurrencyInstance();
 		numFormat.setMaximumFractionDigits(0);
 		_profitField.setFont(Constants.FONT);
 		_profitField.setForeground(Color.BLACK);
 		_profitField.setHorizontalAlignment(JTextField.CENTER);
 
 		if(_orientation == Orientation.DOWN){
 			_profitField.setSize(_orientation.getWidth(), 40);
 			_profitField.setLocation(0, _orientation.getHeight()/2 -10);
 		}
 		else if ( _orientation == Orientation.UP) {
 			_profitField.setSize(_orientation.getWidth(), 40);
 			_profitField.setLocation(0, _orientation.getHeight()/2 - 50);
 		}
 		else if (_orientation == Orientation.LEFT) {
 			_profitField.setSize((int) (_orientation.getWidth()/2), 40);
 			_profitField.setLocation(_orientation.getWidth()/16, _orientation.getHeight()/2 - 30);
 		}
 		else {
 			_profitField.setSize((int) (_orientation.getWidth()/2), 40);
 			_profitField.setLocation(_orientation.getWidth()/2 - _orientation.getWidth()/16, _orientation.getHeight()/2 - 30);
 		}
 
 		_profitField.setBorder(BorderFactory.createLineBorder(Color.WHITE, 0));
 		_profitField.setOpaque(false);
		_profitField.setText(numFormat.format(_profit));
 		
 		this.add(_profitField);
 	}
 	
 	protected void paintComponent (Graphics g, BufferedImage picture, Color color, Color border) {
 		super.paintChildren(g);
 		
 		/* Draw to a buffered image which will then be resized and rotated appropriately */
 		BufferedImage image = new BufferedImage(Constants.WIDTH, Constants.HEIGHT, BufferedImage.TYPE_INT_RGB);
 		Graphics2D g2 = image.createGraphics();
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
 		/* Painting the background block */
 		g2.setColor(Constants.BACKGROUND_COLOR);
 		g2.fill(_spaceBackground);
 
 		g2.setColor(Color.WHITE);
 		g2.fill(_profitBlock);
 		
 		int alpha = (int) (255*_profit/_profitFactor);
 		if(alpha < 0) alpha = 0;
 		if(alpha > 255) alpha = 255;
 		Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
 		g2.setColor(c);
 		g2.fill(_profitBlock);
 
 		g2.setColor(border);
 		g2.setStroke(new BasicStroke(Constants.BORDER));
 		g2.draw(_spaceOutline);
 
 		/* Paint the picture */
 		picture = Helper.resize(picture, (int) (Constants.WIDTH*(9./10)), (int) (Constants.HEIGHT/2));
 		Image icon = Helper.transparent(picture);
 		g2.drawImage(icon, (int) (Constants.WIDTH/20), (int) (Constants.HEIGHT/2), null);
 		
 		/* Rotate and resize the image */
 		image = Helper.resize(image,  Constants.ACTUAL_WIDTH, Constants.ACTUAL_HEIGHT);
 		image = Helper.rotate(image, _orientation);
 
 		g.drawImage(image, 0, 0, null);
 	}
 	
 	protected void paintComponent (Graphics g, Color color, Houses houses, Color border) {
 		super.paintChildren(g);
 		
 		/* Draw to a buffered image which will then be resized and rotated appropriately */
 		BufferedImage image = new BufferedImage(Constants.WIDTH, Constants.HEIGHT, BufferedImage.TYPE_INT_RGB);
 		Graphics2D g2 = image.createGraphics();
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
 		/* Painting the background block */
 		g2.setColor(Constants.BACKGROUND_COLOR);
 		g2.fill(_spaceBackground);
 
 		g2.setColor(Color.WHITE);
 		g2.fill(_profitBlock);
 		
 		int alpha = (int) (255*_profit/_profitFactor);
 		if(alpha < 0) alpha = 0;
 		if(alpha > 255) alpha = 255;
 		Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
 		g2.setColor(c);
 		g2.fill(_profitBlock);
 		
 		houses.paint(g2);
 
 		g2.setColor(border);
 		g2.setStroke(new BasicStroke(Constants.BORDER));
 		g2.draw(_spaceOutline);
 
 		/* Rotate and resize the image */
 		image = Helper.resize(image,  Constants.ACTUAL_WIDTH, Constants.ACTUAL_HEIGHT);
 		image = Helper.rotate(image, _orientation);
 
 		g.drawImage(image, 0, 0, null);
 	}
 
 }
