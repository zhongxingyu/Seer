 package world_viewer;
 
 /**
  * @author Ross Larson and Brett Poirier
  * @class INFO-I 400
  */
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 
 /**
  * @authors Ross Larson, Jon Gold, & Kyle Sprouls
  * @class CSCI-C 343
  * 
  */
 public class GoogleMap2 implements ActionListener {
 
 	private JPanel mainPanel = new JPanel();
 	private JPanel MapPanel = new JPanel();
 	private JPanel photoPanel = new JPanel();
 	private JPanel buttonPanel = new JPanel();
 
 	private JButton next = new JButton("Next Photo -->");
 	private JButton prev = new JButton("<-- Previous Photo");
 	private JLabel pLabel;
 	private JLabel photoLabel;
 	private String string = "";
 
 	private Color ButtonDefault = Color.CYAN;
	private Icon disabledIcon = createImageIcon("PNG/Clear.png",
			"Node Disabled");
	private Icon defaultIcon = createImageIcon("PNG/Blue1.png", "Node");
	private Icon selectedIcon = createImageIcon("PNG/Green1.png",
			"Node Selected");
 
 	private GoogleMap gps = new GoogleMap();
 	private Boxfinder boxMaker = new Boxfinder();
 	private Box[][] boxes = new Box[36][36];
 	private ArrayList<Photo> boxPhotos = new ArrayList<Photo>();
 	// TODO do I need this?
 	private JButton[] buttons = new JButton[36 * 36];
 	private ImageIcon selectedPhotoImage;
 	private final int MID_HEIGHT = 600;
 	private final int PHOTO_WIDTH = 500;
 	private final int MAP_WIDTH = 600;
 
 	private int current;
 
 	public GoogleMap2() {
 
 		JPanel MapPanel = createMapPanel();
 		JPanel photoPanel = createPhotoPanel();
 
 		mainPanel.setBackground(Color.BLACK);
 		mainPanel.setLayout(new BorderLayout(10, 10));
 		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 		mainPanel.add(MapPanel, BorderLayout.WEST);
 		mainPanel.add(photoPanel, BorderLayout.EAST);
 	}
 
 	private JPanel createMapPanel() {
 
 		MapPanel.setOpaque(false);
 
 		MapPanel.setBackground(Color.WHITE);
 		MapPanel.setPreferredSize(new Dimension(MAP_WIDTH, MID_HEIGHT));
 		pLabel = new JLabel(selectedPhotoImage);
 		pLabel.setIcon(createImageIcon(
 				"http://maps.google.com/maps/api/staticmap?center=0,0&zoom=3&size=900x900&sensor=false",
 				"map"));
 
 		MapPanel.add(pLabel, BorderLayout.CENTER);
 		boxes = boxMaker.photoGrid;
 
 		for (int i = 0; i < 36; i++) {
 			for (int j = 0; j < 36; j++) {
 				Node n = boxes[i][j].point;
 				int x = gps.longitudeToX(n.longitude);
 				int y = gps.latitudeToY(n.latitude);
 				JButton button2 = new JButton();
 				button2.setLocation(x - 23, y - 13);
 				button2.setSize(46, 26);
 				button2.setActionCommand("node:" + i + ":" + j);
 				button2.addActionListener(this);
 				button2.setDisabledIcon(disabledIcon);
 				button2.setIcon(defaultIcon);
 				button2.setSelectedIcon(selectedIcon);
 				button2.setEnabled(true);
 
 				buttons[i + 36 * j] = button2;
 				// System.out.println(button2.getText() + "  ,X = " +
 				// button2.getX() + "  ,Y = " + button2.getY());
 				pLabel.add(button2);
 			}
 		}
 
 		MapPanel.add(pLabel, BorderLayout.CENTER);
 
 		return MapPanel;
 	}
 
 	// /WE WANT photos TO APPEAR
 	private JPanel createPhotoPanel() {
 		photoPanel.setBackground(Color.WHITE);
 		photoPanel.setPreferredSize(new Dimension(PHOTO_WIDTH, MID_HEIGHT));
 		photoPanel.setLayout(new BorderLayout(10, 10));
 
 		photoLabel = new JLabel();
 		photoLabel.setLayout(new BorderLayout(10, 10));
 		photoLabel.setIcon(createImageIcon("iu.jpg", "Indiana University"));
 		photoLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		photoLabel.setVerticalAlignment(SwingConstants.CENTER);
 		photoLabel.setPreferredSize(new Dimension(PHOTO_WIDTH, MID_HEIGHT));
 
 		prev.setActionCommand("previous");
 		next.setActionCommand("next");
 
 		prev.addActionListener(this);
 		next.addActionListener(this);
 
 		prev.setEnabled(false);
 		next.setEnabled(false);
 
 		buttonPanel.setBackground(Color.WHITE);
 		buttonPanel.setPreferredSize(new Dimension(PHOTO_WIDTH, 50));
 
 		buttonPanel.add(prev);
 		buttonPanel.add(next);
 
 		photoPanel.add(photoLabel, BorderLayout.CENTER);
 		photoPanel.add(buttonPanel, BorderLayout.NORTH);
 
 		return photoPanel;
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		String command = e.getActionCommand();
 		if (command.substring(0, 4).equals("node")) {
 			JButton buttonClicked = (JButton) e.getSource();
 			String[] parts = command.split(":");
 			int i = Integer.parseInt(parts[1]);
 			int j = Integer.parseInt(parts[2]);
 			Box b = boxes[i][j];
 			Node n = b.point;
 			// System.out.println("Button pressed. It was for box #: " + b.ID);
 
 			// copy all the photos over so we can transverse back and forth
 			while (!b.photos.isEmpty()) {
 				boxPhotos.add(b.photos.poll());
 			}
 			// put photos back in PriorityQueue so we can come back here and
 			// have photos
 			for (Photo p : boxPhotos) {
 				b.photos.add(p);
 			}
 			// load up first photo
 			if (boxPhotos.size() >= 1) {
 				Photo p = boxPhotos.get(0);
 				photoLabel.setIcon(createImageIcon(p.imageURL, p.title));
 				current = 0;
 				if (boxPhotos.size() > 1) {
 					next.setEnabled(true);
 					prev.setEnabled(false);
 				}
 				/*
 				 * use this for loop if you need to modify every button for
 				 * (JButton jb : buttons) { jb.setBackground(ButtonDefault);
 				 * 
 				 * }
 				 */
 			}
 			else {
 				// System.out.println("pathPhotos is empty.");
 			}
 		}
 		else if (command.equals("next")) {
 			current++;
 			photoLabel.setIcon(createImageIcon(boxPhotos.get(current).imageURL,
 					boxPhotos.get(current).title));
 			if (boxPhotos.size() == (current + 1)) {
 				next.setEnabled(false);
 			}
 			prev.setEnabled(true);
 		}
 		else if (command.equals("previous")) {
 			current--;
 			photoLabel.setIcon(createImageIcon(boxPhotos.get(current).imageURL,
 					boxPhotos.get(current).title));
 			if (current == 0) {
 				prev.setEnabled(false);
 			}
 			next.setEnabled(true);
 		}
 		photoPanel.getRootPane().revalidate();
 		photoPanel.getRootPane().repaint();
 		MapPanel.getRootPane().repaint();
 	}
 
 	private ImageIcon createImageIcon(String path, String description) {
		java.net.URL imgURL = getClass().getResource(path);
 		if (imgURL != null) {
 			return new ImageIcon(imgURL, description);
 		}
 		else {
 			try {
 				imgURL = new URL(path);
 			}
 			catch (MalformedURLException MUE) {
 			}
 			if (imgURL != null) {
 				return new ImageIcon(imgURL, description);
 			}
 			else {
 				System.err.println("Couldn't find file: " + path);
 				return null;
 			}
 		}
 	}
 
 	public JPanel getPanel() {
 		return mainPanel;
 	}
 
 	private static void createAndShowGUI() {
 		JFrame frame = new JFrame("Map");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		WindowUtilities.setMotifLookAndFeel();
 		frame.add(new GoogleMap2().getPanel());
 		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
 		frame.setSize(900, 750);
 		frame.setLocationRelativeTo(null);
 		frame.pack();
 		if (screen.getHeight() <= 800)
 			frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
 		frame.setVisible(true);
 		frame.toFront();
 	}
 
 	/**
 	 * Starts the GUI and program
 	 * 
 	 * @param args
 	 *            - There should be NO arguments from the command line (they
 	 *            will be ignored)
 	 */
 	public static void main(String[] args) {
 		javax.swing.SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				createAndShowGUI();
 			}
 		});
 
 	}
 
 }
