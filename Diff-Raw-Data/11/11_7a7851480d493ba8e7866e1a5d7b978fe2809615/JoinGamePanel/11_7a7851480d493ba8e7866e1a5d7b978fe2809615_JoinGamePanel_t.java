 package edu.brown.cs32.MFTG.gui;
 
 import java.awt.Color;
 import java.awt.Desktop;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.TextField;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import javax.imageio.ImageIO;
 import javax.swing.ButtonGroup;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.event.MouseInputAdapter;
 
 
 
 public class JoinGamePanel extends JPanel{
 	private ImagePanel _createLite, _backLite, _backDark, _goLite, _goDark;
 	private BufferedImage _background;
 	private Point _createLoc, _whiteLoc, _backLoc, _goLoc;
 	private JoinBottomPanel _bottomPanel;
 	private MonopolyGui _main;
 
 	private final int BUTTON_HEIGHT=Constants.FULL_HEIGHT/8;
 	private final int BUTTON_WIDTH=2*Constants.FULL_HEIGHT/3;
 	private final int START_HEIGHT=Constants.FULL_HEIGHT/8;
 	private final int START_WIDTH=Constants.FULL_WIDTH/6;
 
 	public JoinGamePanel(MonopolyGui main) {
 		try {
 			_main=main;
 			java.awt.Dimension size = new java.awt.Dimension(Constants.FULL_WIDTH,Constants.FULL_HEIGHT);
 			this.setPreferredSize(size);
 			this.setSize(size);
 			this.setBackground(Color.GRAY);
 			this.setLayout(null);
 			_background = Helper.resize(ImageIO.read(new File("images/mountain2.png")),this.getWidth(),this.getHeight());
 
 			_createLite = new ImagePanel(Helper.resize(ImageIO.read(new File("images/JoinGameLite.png")), BUTTON_WIDTH-40, BUTTON_HEIGHT));
 			_createLoc= new Point(START_WIDTH, START_HEIGHT);
 			_createLite.setLocation(_createLoc);
 
 			_whiteLoc= new Point(Constants.FULL_WIDTH/32, START_HEIGHT+BUTTON_HEIGHT*6/5);
 			_bottomPanel=new JoinBottomPanel();
 			_bottomPanel.setLocation(_whiteLoc);
 
 			_backLite = new ImagePanel(Helper.resize(ImageIO.read(new File("images/BackLite.png")), 100, 50));
 			_backDark = new ImagePanel(Helper.resize(ImageIO.read(new File("images/BackDark.png")), 100, 50));
 			_backLoc= new Point(20, this.getHeight()-_backDark.getHeight()-10);
 			_backLite.setLocation(_backLoc);
 			_backDark.setLocation(_backLoc);
 			_backDark.setVisible(false);
 
 			_goLite = new ImagePanel(Helper.resize(ImageIO.read(new File("images/GoLite.png")), 100, 50));
 			_goDark = new ImagePanel(Helper.resize(ImageIO.read(new File("images/GoDark.png")), 100, 50));
 			_goLoc= new Point(_whiteLoc.getLocation().x+_bottomPanel.getWidth()-_goLite.getWidth()-30, _whiteLoc.getLocation().y+_goDark.getHeight()-20);
 			_goLite.setLocation(_goLoc);
 			_goDark.setLocation(_goLoc);
 			_goDark.setVisible(false);
 
 
 			addMouseListener(new MyMouseListener());
 
 			add(_createLite);
 			add(_backLite);
 			add(_backDark);
 			add(_goLite);
 			add(_goDark);
 			add(_bottomPanel);
 
 
 		} catch (IOException e) {
 			System.out.println("ERROR: "+e.getMessage());
 			System.exit(1);
 		}
 
 	}
 	@Override
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		Graphics2D brush = (Graphics2D) g;
 		brush.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		brush.drawImage(_background, 0, 0, null);
 
 	}
 
 	private class MyMouseListener extends MouseInputAdapter{
 		public MyMouseListener() {
 			super();
 		}
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 			int xloc=e.getX();
 			int yloc=e.getY();
 			if(intersects(xloc,yloc,_backLite,_backLoc)) {
 				_backLite.setVisible(false);
 				_backDark.setVisible(true);
 				repaint();
 			}
 			else if(intersects(xloc,yloc,_goLite,_goLoc)) {
 				_goLite.setVisible(false);
 				_goDark.setVisible(true);
 				repaint();
 			}
 			if(e.getX()>276 &&e.getX()<503&&e.getY()>581 &&e.getY()<636) {
 				String url = "https://itunes.apple.com/us/app/foodler/id615802139?mt=8"; // path to your new file
 
 				// open the default web browser for the HTML page
 				try {
 					Desktop.getDesktop().browse(new URI(url));
 				} catch (IOException e1) {
 				} catch (URISyntaxException e1) {
 				}
 
 			}
 
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 
 			int xloc=e.getX();
 			int yloc=e.getY();
 			if(intersects(xloc,yloc,_backLite,_backLoc)) {
 				if(_backDark.isVisible()) {
 					fixPanels();
 					_main.switchPanels("lobby");
 				}
 				else {
 					fixPanels();
 				}
 
 			}
 			else if(intersects(xloc,yloc,_goLite,_goLoc)) {
 				if(_goDark.isVisible()) {
 					fixPanels();
 					System.out.println("joining game");
 					_main.getClient().connectAndRun();
					_main.switchPanels("board");
 				}
 				else {
 					fixPanels();
 				}
 
 			}
 			else {
 				fixPanels();
 			}
 		}
 
 		private void fixPanels() {
 			_backDark.setVisible(false);
 			_backLite.setVisible(true);
 			_goDark.setVisible(false);
 			_goLite.setVisible(true);
 			repaint();
 		}
 		private boolean intersects(int xloc, int yloc, JPanel img, Point loc) {
 			if(xloc>=loc.x && xloc<=loc.x+img.getWidth() && yloc>=loc.y && yloc<=loc.y+img.getHeight()) {
 				return true;
 			}
 			return false;
 		}
 
 	}
 
 
 }
 
