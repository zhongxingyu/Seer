 package edu.brown.cs32.MFTG.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.AbstractAction;
 import javax.swing.DefaultListModel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.KeyStroke;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.MouseInputAdapter;
 
 import edu.brown.cs32.MFTG.networking.ProfileManager;
 import edu.brown.cs32.MFTG.tournament.Profile;
 
 
 @SuppressWarnings("serial")
 public class ChooseProfilePanel extends JPanel{
 
 	private ImagePanel _chooseLite, _backLite, _backDark, _selectLite, _selectDark;
 	private BufferedImage _background;
 	private Point _chooseLoc,  _backLoc, _selectLoc;
 	private MonopolyGui _main;
 	private ChooseProfileScrollPane _profileScrollPane;
 
 	private JList<String> _profileList;
 	private DefaultListModel<String> _listModel;
 	private boolean _selectActivated;
 
 	private final int BUTTON_HEIGHT=Constants.FULL_HEIGHT/8;
 	private final int BUTTON_WIDTH=2*Constants.FULL_HEIGHT/3;
 	private final int START_HEIGHT=Constants.FULL_HEIGHT/8;
 	private final int START_WIDTH=Constants.FULL_WIDTH/6;
 	private final int BOTTOM_HEIGHT=Constants.FULL_HEIGHT*3/5;
 	private final int BOTTOM_WIDTH=Constants.FULL_WIDTH*15/16;
 
 	public ChooseProfilePanel(MonopolyGui main) {
 		try {
 			_main=main;
 			java.awt.Dimension size = new java.awt.Dimension(Constants.FULL_WIDTH,Constants.FULL_HEIGHT);
 			this.setPreferredSize(size);
 			this.setSize(size);
 			this.setBackground(Color.GRAY);
 			this.setLayout(null);
 			_background = Helper.resize(ImageIO.read(new File("images/mountain2.png")),this.getWidth(),this.getHeight());
 
 			_chooseLite = new ImagePanel(Helper.resize(ImageIO.read(new File("images/ChooseProfile.png")), BUTTON_WIDTH-40, BUTTON_HEIGHT));
 			_chooseLoc= new Point(START_WIDTH, START_HEIGHT);
 			_chooseLite.setLocation(_chooseLoc);
 
 			_backLite = new ImagePanel(Helper.resize(ImageIO.read(new File("images/BackLite.png")), 100, 50));
 			_backDark = new ImagePanel(Helper.resize(ImageIO.read(new File("images/BackDark.png")), 100, 50));
 			_backLoc= new Point(Constants.BACK_X, Constants.BACK_Y);
 			_backLite.setLocation(_backLoc);
 			_backDark.setLocation(_backLoc);
 			_backDark.setVisible(false);
 
 			_selectLite = new ImagePanel(Helper.resize(ImageIO.read(new File("images/SelectLite.png")), 120, 50));
 			_selectDark = new ImagePanel(Helper.resize(ImageIO.read(new File("images/SelectDark.png")), 120, 50));
 			_selectLoc= new Point(this.getWidth()-_selectLite.getWidth()-30, this.getHeight()-_selectLite.getHeight()-40);
 			_selectLite.setLocation(_selectLoc);
 			_selectDark.setLocation(_selectLoc);
 			_selectDark.setVisible(false);
 			_selectLite.setVisible(true);
 
 			addMouseListener(new MyMouseListener());
 
 			add(_chooseLite);
 			add(_backLite);
 			add(_backDark);
 			add(_selectLite);
 			add(_selectDark);
 
 			addProfileList();
 		} catch (IOException e) {
 			System.out.println("ERROR: "+e.getMessage());
 			System.exit(1);
 		}
 
 	}
 	
 	/**
 	 * grabs focus
 	 */
 	public void giveFocusToList() {
 		_profileList.grabFocus();
		_profileList.setSelectedIndex(0);
 	}
 	
 	/**
 	 * resets the list of profiles 
 	 */
 	public void resetProfileList() {
 		_profileScrollPane.addProfileNames();
 	}
 
 	private void addProfileList(){
 		_listModel = new DefaultListModel<>();
 		_profileList = new JList<>(_listModel);
 
 		_profileScrollPane = new ChooseProfileScrollPane(_profileList, _listModel, _main);
 		_profileScrollPane.setup();
 
 		_profileScrollPane.setLocation(Constants.FULL_WIDTH/32, START_HEIGHT+BUTTON_HEIGHT*6/5);
 		Dimension listSize = new Dimension(BOTTOM_WIDTH, BOTTOM_HEIGHT);
 		_profileScrollPane.setSize(listSize);
 		_profileScrollPane.setPreferredSize(listSize);
 		add(_profileScrollPane);
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
 			else if(intersects(xloc,yloc,_selectLite,_selectLoc)) {
 				_selectLite.setVisible(false);
 				_selectDark.setVisible(true);
 				repaint();
 			}
 
 
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 
 			int xloc=e.getX();
 			int yloc=e.getY();
 			if(intersects(xloc,yloc,_backLite,_backLoc)) {
 				if(_backDark.isVisible()) {
 					fixPanels();
 					_main.switchPanels("greet");
 				}
 				else {
 					fixPanels();
 				}
 
 			}
 			else if(intersects(xloc,yloc,_selectLite,_selectLoc)) {
 				if(_selectDark.isVisible()) {
 					fixPanels();
 					_profileScrollPane.processClick();
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
 			_selectDark.setVisible(false);
 			_selectLite.setVisible(true);			repaint();
 		}
 		private boolean intersects(int xloc, int yloc, JPanel img, Point loc) {
 			if(xloc>=loc.x && xloc<=loc.x+img.getWidth() && yloc>=loc.y && yloc<=loc.y+img.getHeight()) {
 				return true;
 			}
 			return false;
 		}
 
 	}
 }
 
