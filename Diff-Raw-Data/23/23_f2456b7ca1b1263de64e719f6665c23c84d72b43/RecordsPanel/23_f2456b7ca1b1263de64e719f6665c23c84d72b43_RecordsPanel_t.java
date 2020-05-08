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
 import javax.swing.DefaultListModel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.event.MouseInputAdapter;
 
 
 @SuppressWarnings("serial")
 public class RecordsPanel extends JPanel{
 
 	private ImagePanel _recordsLite, _backLite, _backDark;
 	private BufferedImage _background;
 	private Point _recordsLoc,  _backLoc;
 	private MonopolyGui _main;
 	private RecordsProfileScrollPane _recordsScrollPane;
 
 	private JList<String> _profileList;
 	private DefaultListModel<String> _profileListModel;
 	private RecordsSidePanel _recordsSidePanel;
 
 	private final int BUTTON_HEIGHT=Constants.FULL_HEIGHT/8;
 	private final int BUTTON_WIDTH=2*Constants.FULL_HEIGHT/3;
 	private final int START_HEIGHT=Constants.FULL_HEIGHT/8;
 	private final int START_WIDTH=Constants.FULL_WIDTH/6;
 	private final int BOTTOM_HEIGHT=Constants.FULL_HEIGHT*3/5;
 	private final int BOTTOM_WIDTH=Constants.FULL_WIDTH*15/16;
 
 	public RecordsPanel(MonopolyGui main) {
 		try {
 			_main=main;
 			java.awt.Dimension size = new java.awt.Dimension(Constants.FULL_WIDTH,Constants.FULL_HEIGHT);
 			this.setPreferredSize(size);
 			this.setSize(size);
 			this.setBackground(Color.GRAY);
 			this.setLayout(null);
 			_background = Helper.resize(ImageIO.read(new File("images/mountain2.png")),this.getWidth(),this.getHeight());
 
 			_recordsLite = new ImagePanel(Helper.resize(ImageIO.read(new File("images/RecordsLite.png")), BUTTON_WIDTH-40, BUTTON_HEIGHT));
 			_recordsLoc= new Point(START_WIDTH, START_HEIGHT);
 			_recordsLite.setLocation(_recordsLoc);
 
 			_backLite = new ImagePanel(Helper.resize(ImageIO.read(new File("images/BackLite.png")), 100, 50));
 			_backDark = new ImagePanel(Helper.resize(ImageIO.read(new File("images/BackDark.png")), 100, 50));
 			_backLoc= new Point(Constants.BACK_X, Constants.BACK_Y);
 			_backLite.setLocation(_backLoc);
 			_backDark.setLocation(_backLoc);
 			_backDark.setVisible(false);
 
 			addMouseListener(new MyMouseListener());
 
 			add(_recordsLite);
 			add(_backLite);
 			add(_backDark);
 			
 			_recordsSidePanel = new RecordsSidePanel();
 			_recordsSidePanel.setLocation(Constants.FULL_WIDTH/32+BOTTOM_WIDTH*2/3, START_HEIGHT+BUTTON_HEIGHT*6/5);
 			Dimension listSize = new Dimension(BOTTOM_WIDTH/3, BOTTOM_HEIGHT);
 			_recordsSidePanel.setSize(listSize);
 			_recordsSidePanel.setPreferredSize(listSize);
 			add(_recordsSidePanel);
 			
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
		_recordsSidePanel.setRecords(_main.getRecord(_profileList.getSelectedValue()));
 	}
 	
 	/**
 	 * resets the list of profiles 
 	 */
 	public void resetProfileList() {
 		_recordsScrollPane.addProfileNames();
 	}
 
 	private void addProfileList(){
 		_profileListModel = new DefaultListModel<>();
 		_profileList = new JList<>(_profileListModel);
 
 		_recordsScrollPane = new RecordsProfileScrollPane(_profileList, _profileListModel, _main, _recordsSidePanel);
 		_recordsScrollPane.setup();
 
 		_recordsScrollPane.setLocation(Constants.FULL_WIDTH/32-10, START_HEIGHT+BUTTON_HEIGHT*6/5);
 		Dimension listSize = new Dimension(BOTTOM_WIDTH*2/3, BOTTOM_HEIGHT);
 		_recordsScrollPane.setSize(listSize);
 		_recordsScrollPane.setPreferredSize(listSize);
 		add(_recordsScrollPane);
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
 			else {
 				fixPanels();
 			}
 		}
 
 		private void fixPanels() {
 			_backDark.setVisible(false);
 			_backLite.setVisible(true);
 
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
 
