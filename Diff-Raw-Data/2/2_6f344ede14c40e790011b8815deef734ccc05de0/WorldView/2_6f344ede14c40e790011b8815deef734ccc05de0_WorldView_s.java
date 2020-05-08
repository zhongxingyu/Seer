 package world_viewer;
 
 /**
  * @author Ross Larson and Brett Poirier
  * @class INFO-I 400
  */
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 
 public class WorldView implements ActionListener {
 
 	private JPanel mainPanel = new JPanel();
 	private JPanel MapPanel = new JPanel();
 	private JPanel photoPanel = new JPanel();
 	private JPanel buttonPanel = new JPanel();
 
 	private JButton next = new JButton("Next Photo -->");
 	private JButton prev = new JButton("<-- Previous Photo");
 	private JLabel pLabel;
 	private JLabel photoLabel;
 	private JLabel countLabel;
 
 	private int iconS = 16;
 
 	private Icon disabledIcon = createImageIcon("Clear.png", "Node Disabled", iconS, iconS);
 	private Icon defaultIcon = createImageIcon("Blue1.png", "Node", iconS, iconS);
 	private Icon selectedIcon = createImageIcon("Green1.png", "Node Selected", iconS, iconS);
 
 	private static GoogleMap gps;
 	private static Boxfinder boxMaker;
 	private Box[][] boxes = new Box[36][36];
 	private ArrayList<Photo> boxPhotos = new ArrayList<Photo>();
 	private JButton[] buttons = new JButton[36 * 36];
 	private ImageIcon selectedPhotoImage;
 	private static int MID_HEIGHT = 400;
 	private final int PHOTO_WIDTH = 500;
 	private static int MAP_WIDTH = 500;
 
 	private static boolean useCache = false;
 	private static int pages = 10;
 	private static double scale = 1;
 
 	private int current;
 
 	public WorldView() {
 
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
 				"http://maps.google.com/maps/api/staticmap?center=0,0&zoom=1&size=500x400&scale=2&sensor=false",
 				"map", (int) (500*scale), (int) (400*scale)));
 
 		MapPanel.add(pLabel, BorderLayout.CENTER);
 		boxes = boxMaker.photoGrid;
 
 		for (int i = 0; i < 36; i++) {
 			for (int j = 0; j < 36; j++) {
 				if (boxes[i][j].photos.size() > 0) {
 					MapNode n = boxes[i][j].point;
 					int x = gps.longitudeToX(n.longitude);
 					int y = gps.latitudeToY(n.latitude);
 					JButton button = new JButton();
 					button.setLocation(x, y);
 					button.setSize(iconS, iconS);
 					button.setOpaque(false);
 					button.setActionCommand("node:" + i + ":" + j);
 					button.addActionListener(this);
 					button.setDisabledIcon(disabledIcon);
 					button.setIcon(defaultIcon);
 					button.setSelectedIcon(selectedIcon);
 					if (boxes[i][j].photos.size() > 0) {
 						button.setEnabled(true);
 					}
 					else {
 						button.setEnabled(false);
 					}
 
 					buttons[i + 36 * j] = button;
 					// System.out.println(button2.getText() + "  ,X = " +
 					// button2.getX() + "  ,Y = " + button2.getY());
 					pLabel.add(button);
 				}
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
 		photoLabel.setIcon(createImageIcon("Blue1.png", "Blue Orb", 0, 0));
 		photoLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		photoLabel.setVerticalAlignment(SwingConstants.CENTER);
 		photoLabel.setPreferredSize(new Dimension(PHOTO_WIDTH, MID_HEIGHT));
 
 		countLabel = new JLabel();
 		countLabel.setLayout(new BorderLayout(10, 10));
 		countLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		countLabel.setVerticalAlignment(SwingConstants.CENTER);
 		countLabel.setText("Photo 0/0");
 
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
 		buttonPanel.add(countLabel);
 
 		photoPanel.add(photoLabel, BorderLayout.CENTER);
 		photoPanel.add(buttonPanel, BorderLayout.NORTH);
 
 		return photoPanel;
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		String command = e.getActionCommand();
 		if (command.substring(0, 4).equals("node")) {
 			JButton jB = ((JButton) e.getSource());
 			String[] parts = command.split(":");
 			int i = Integer.parseInt(parts[1]);
 			int j = Integer.parseInt(parts[2]);
 			Box b = boxes[i][j];
 			// System.out.println(b.toString());
 
 			// copy all the photos over so we can transverse back and forth
 			boxPhotos.clear();
 			while (!b.photos.isEmpty()) {
 				boxPhotos.add(b.photos.poll());
 			}
 			// put photos back in PriorityQueue so we can come back here and
 			// have photos
 			for (Photo p : boxPhotos) {
 				b.photos.add(p);
 			}
 			countLabel.setText("Photo 1/" + boxPhotos.size());
 			// load up first photo
 			if (boxPhotos.size() >= 1) {
 				Photo p = boxPhotos.get(0);
 				//System.out.println(p.toString());
 				// System.out.println("(" + jB.getX() + ", " + jB.getY() + ")");
 				photoLabel.setIcon(createImageIcon(p.imageURL, p.title, 0, 0));
 				current = 0;
 				prev.setEnabled(false);
 				if (boxPhotos.size() > 1) {
 					next.setEnabled(true);
 				}
 				else {
 					next.setEnabled(false);
 				}
 			}
 			else {
 				photoLabel.setIcon(createImageIcon("Blue1.png", "No photos", 0, 0));
 				// System.out.println(photoLabel.getIcon().toString());
 			}
 			for (JButton jb : buttons) {
 				if (jb != null) {
 					jb.setIcon(defaultIcon);
 				}
 			}
 			jB.setIcon(selectedIcon);
 		}
 		else if (command.equals("next")) {
 			current++;
 			Photo p = boxPhotos.get(current);
 			// System.out.println(p.ID);
 			countLabel.setText("Photo " + (current + 1) + "/" + boxPhotos.size());
 			photoLabel.setIcon(createImageIcon(p.imageURL, p.title, 0, 0));
 			if (boxPhotos.size() == (current + 1)) {
 				next.setEnabled(false);
 			}
 			prev.setEnabled(true);
 		}
 		else if (command.equals("previous")) {
 			current--;
 			Photo p = boxPhotos.get(current);
 			// System.out.println(p.ID);
 			countLabel.setText("Photo " + (current + 1) + "/" + boxPhotos.size());
 			photoLabel.setIcon(createImageIcon(p.imageURL, p.title, 0, 0));
 			if (current == 0) {
 				prev.setEnabled(false);
 			}
 			next.setEnabled(true);
 		}
 		photoLabel.repaint();
 		photoPanel.getRootPane().revalidate();
 		photoPanel.getRootPane().repaint();
 		MapPanel.getRootPane().repaint();
 	}
 
 	private ImageIcon createImageIcon(String path, String description, int width, int height) {
 		java.net.URL imgURL = this.getClass().getResource(path);
 		if (imgURL != null) {
 			if (width <= 0)
 				width = -1;
 			if (height <= 0)
 				height = -1;
 			Image img = java.awt.Toolkit.getDefaultToolkit().createImage(imgURL)
 					.getScaledInstance(width, height, Image.SCALE_SMOOTH);
 
 			return new ImageIcon(img, description);
 		}
 		else {
 			try {
 				imgURL = new URL(path);
 			}
 			catch (MalformedURLException MUE) {
 			}
 			if (imgURL != null) {
 				if (width == 0 && height == 0) {
 					return new ImageIcon(imgURL, description);
 				}
 				else {
 					Image img = java.awt.Toolkit.getDefaultToolkit().createImage(imgURL).getScaledInstance(width, height, Image.SCALE_SMOOTH);
 					return new ImageIcon(img, description);
 				}
 
 				
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
 		WindowUtilities.setNativeLookAndFeel();
 		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
 		frame.setLocationRelativeTo(null);
 		frame.pack();
 		frame.setResizable(false);
 		if (screen.getWidth() <= 1000) {
 			scale = 1;
 		}
 		else if (screen.getWidth() < 1500) {
 			scale = 1.5;
 		}
 		else {
 			scale = 2;
 		}
 		gps = new GoogleMap(scale);
 		MID_HEIGHT = (int) (scale*400);
 		MAP_WIDTH = (int) (scale*500);
		frame.setSize(500+ MAP_WIDTH, 50 + MID_HEIGHT);
 		File cache = new File("cache.txt");
 		if (cache.exists()) {
 			// Custom button text
 			Object[] options = { "Load from cache", "Download from Flickr", "Cancel" };
 			int n = JOptionPane.showOptionDialog(frame, "How would you like to load images?",
 					"Image Load Options", JOptionPane.YES_NO_CANCEL_OPTION,
 					JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
 
 			if (n == 2) {
 				System.exit(0);
 			}
 			else if (n == 1) {
 				String s = (String) JOptionPane.showInputDialog(frame,
 						"How many pages would you like? Each page takes ~4 seconds",
 						"Number of Pages of Photos", JOptionPane.PLAIN_MESSAGE, null, null, "10");
 				int p = 10;
 				p = Integer.parseInt(s);
 				pages = p;
 			}
 			else {
 				useCache = true;
 			}
 		}
 		else {
 			String s = (String) JOptionPane.showInputDialog(frame,
 					"How many photos would you like? Each batch of 250 takes ~4 seconds",
 					"Number of Photos", JOptionPane.PLAIN_MESSAGE, null, null, "2500");
 			int p = 2500;
 			p = Integer.parseInt(s);
 			if (p == 0) {
 				System.exit(0);
 			}
 			pages = p;
 		}
 		boxMaker = new Boxfinder(useCache, pages);
 		frame.add(new WorldView().getPanel());
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
