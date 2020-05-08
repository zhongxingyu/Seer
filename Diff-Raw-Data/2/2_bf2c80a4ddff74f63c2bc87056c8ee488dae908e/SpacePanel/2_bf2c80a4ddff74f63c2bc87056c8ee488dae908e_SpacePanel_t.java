 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 public class SpacePanel extends JPanel {
 
 	public static final int DEFAULT_WINDOW_SIZE = 740;
 	public static final int DEFAULT_SIZE = 35;
 	private static final int SCALAR = 1;
 	private static final int SHIFT = DEFAULT_WINDOW_SIZE / 2;
 	private static final int BUTTONSIZE = 40;
 	private UndiscoveredWorldsGUI uw;
 	private Player player;
 	private Location currentLocation;
 	private JLabel locationName;
 	private JLabel fuelLabel;
 	private JLabel empty = new JLabel("OUT OF FUEL");
 	private JButton quit = new JButton();
 
 	public SpacePanel(Player player, UndiscoveredWorldsGUI uw) {
 		this.uw = uw;
 		this.player = player;
 		this.setBackground(Color.BLACK);
 		locationName = new JLabel("");
 		fuelLabel = new JLabel("Ship Fuel: " + player.getFuelLevel());
 		populateSpace();
 	}
 
 	public void populateSpace() {
 		this.removeAll();
 		this.setLayout(null);
 		this.setBounds(0, 0, DEFAULT_WINDOW_SIZE, DEFAULT_WINDOW_SIZE);
 		currentLocation = player.getLoc();
 		Location[] locations = currentLocation.getChild();
 		locationName.setBounds(200, 0, 200, 30);
 		fuelLabel.setBounds(0, 50, 200, 30);
 		fuelLabel.setForeground(Color.GREEN);
 		this.add(fuelLabel);
 
 		JButton optionsButton = new JButton();
 		optionsButton.setSize(BUTTONSIZE, BUTTONSIZE);
 		optionsButton.setLocation(
 				DEFAULT_WINDOW_SIZE - optionsButton.getWidth() - 5, 0);
 		optionsButton.setIcon(new ImageIcon("Art" + File.separator
 				+ "SettingsButton.png"));
 		optionsButton.setSelectedIcon(new ImageIcon("Art" + File.separator
 				+ "SettingsButtonPressed.png"));
 		optionsButton.setToolTipText("Options Menu");
 		optionsButton.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent arg0) {
 				UWOptionsPanel optionWindow = new UWOptionsPanel(uw);
 				optionWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 				optionWindow.setSize(400, 150);
 				optionWindow.setLocation(
 						(int) getParent().getLocationOnScreen().getX()
 								+ ((getParent().getWidth()) / 2)
 								- (optionWindow.getWidth() / 2),
 						(int) getParent().getLocationOnScreen().getY()
 								+ ((getParent().getHeight()) / 2)
 								- (optionWindow.getHeight() / 2));
 				optionWindow.setVisible(true);
 				optionWindow.setResizable(false);
 			}
 		});
 
 		JButton inventoryButton = new JButton();
 		// Someone will make art
 		inventoryButton.setSize(BUTTONSIZE, BUTTONSIZE);
 		inventoryButton.setLocation(
 				DEFAULT_WINDOW_SIZE - optionsButton.getWidth()
 						- inventoryButton.getWidth() - 10, 0);
 		inventoryButton.setIcon(new ImageIcon("Art" + File.separator
 				+ "InventoryButton.png"));
 		inventoryButton.setSelectedIcon(new ImageIcon("Art" + File.separator
 				+ "InventoryButtonPressed.png"));
 		inventoryButton.setToolTipText("Inventory");
 		inventoryButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				player.revertPrices();
 				InventoryFrame inventoryFrame = createIF();
				inventoryFrame.setSize(500, 320);
 				inventoryFrame.setResizable(false);
 				inventoryFrame.setLocation(
 						(int) getParent().getLocationOnScreen().getX()
 								+ ((getParent().getWidth()) / 2)
 								- (inventoryFrame.getWidth() / 2),
 						(int) getParent().getLocationOnScreen().getY()
 								+ ((getParent().getHeight()) / 2)
 								- (inventoryFrame.getHeight() / 2));
 				inventoryFrame.setVisible(true);
 				inventoryFrame
 						.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 			}
 		});
 
 		JButton back = new JButton();
 		back.setIcon(new ImageIcon("Art" + File.separator + "BackButton.png"));
 		back.setSelectedIcon(new ImageIcon("Art" + File.separator
 				+ "BackButtonPressed.png"));
 		back.setOpaque(false);
 		back.setBorderPainted(false);
 		back.setContentAreaFilled(false);
 		back.setToolTipText("Return to " + currentLocation.getParent());
 		back.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if (player.tryToWin()) {
 					Object[] options = { "Quit to Main Menu"};
 					int n = JOptionPane
 							.showOptionDialog(
 									getParent(),
 									"You have assembled your drive and won the game."
 									+ "\nYou left with $" + player.getMoney(),
 									"Game Won!", JOptionPane.YES_OPTION,
 									JOptionPane.QUESTION_MESSAGE, null,
 									options, options[0]);
 
 					if (n == JOptionPane.YES_OPTION) {
 						uw.resetGame();
 					}
 				} else {
 					player.setLoc(currentLocation.getParent());
 					player.useFuel();
 					populateSpace();
 					playerStatusChanged();
 				}
 			}
 
 		});
 		back.setLocation(0, 0);
 		back.setSize(BUTTONSIZE, BUTTONSIZE);
 		back.setVisible(true);
 		fuelLabel.setText("Ship Fuel: " + player.getFuelLevel());
 		switch (currentLocation.whatAmI()) {
 		case 4:
 			locationName.setText("Orbital");
 			break;
 		case 3:
 			locationName.setText("Star System");
 			break;
 		case 2:
 			locationName.setText("Sector");
 			break;
 		case 1:
 			locationName.setText("Cluster");
 			break;
 		case 0:
 			back.setVisible(false);
 			break;
 		default:
 			locationName.setText("Error");
 		}
 		locationName.setBackground(Color.WHITE);
 		// this.add(locationName);
 		this.add(optionsButton);
 		this.add(back);
 		this.add(inventoryButton);
 		if (locations != null) {
 
 			for (int i = 0; i < locations.length; i++) {
 				Location loc = locations[i];
 				int whoNum = loc.whatAmI();
 				String who = "";
 				switch (whoNum) {
 				case 4:
 					who = "Orbital";
 					break;
 				case 3:
 					who = "Star System";
 					break;
 				case 2:
 					who = "Sector";
 					break;
 				case 1:
 					who = "Cluster";
 					break;
 				case 0:
 					who = "Galaxy";
 				default:
 					locationName.setText("Error");
 				}
 				JButton b = new JButton();
 				/*
 				 * if(currentLocation.whatAmI() == 1){ b.setBounds(SCALAR *
 				 * loc.GetX() + SHIFT, SCALAR * loc.GetY() + SHIFT, 100, 100);
 				 * b.setIcon(new ImageIcon(loc.GetPic(75))); }else{
 				 * b.setBounds(SCALAR * loc.GetX() + SHIFT, SCALAR * loc.GetY()
 				 * + SHIFT, 50, 50); b.setIcon(new
 				 * ImageIcon(loc.GetPic(DEFAULT_SIZE))); }
 				 */
 				b.setBounds(SCALAR * loc.GetX() + SHIFT, SCALAR * loc.GetY()
 						+ SHIFT, 50, 50);
 				b.setIcon(new ImageIcon(loc.GetNavImage()));
 				b.setBackground(Color.BLUE);
 				LocationListener handler = new LocationListener(i);
 				b.addActionListener(handler);
 				b.setOpaque(false);
 				b.setBorderPainted(false);
 				b.setContentAreaFilled(false);
 				b.setToolTipText("<HTML><BODY BGCOLOR=#000 STYLE=padding:5px;><FONT COLOR=#00FF00>"
 						+ "Name: "
 						+ loc.toString()
 						+ "<BR />"
 						+ loc.GetDetails()
 						+ "<BR />"
 						+ "Location: "
 						+ ((!loc.toString().equals(loc.getParent().toString())) ? loc
 								.getParent().toString() : "Galaxy")
 						+ "</FONT></BODY></HTML>");
 
 				this.add(b);
 			}
 			renderScene(locations);
 			repaint();
 		} else {
 			TradingMenu menu = new TradingMenu(player);
 			menu.setSize(650, 230);
 			menu.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 			menu.setResizable(false);
 			menu.setLocation(
 					(int) getParent().getLocationOnScreen().getX()
 							+ ((getParent().getWidth()) / 2)
 							- (menu.getWidth() / 2),
 					(int) getParent().getLocationOnScreen().getY()
 							+ ((getParent().getHeight()) / 2)
 							- (menu.getHeight() / 2));
 			menu.setVisible(true);
 			player.setLoc(currentLocation.getParent());
 			populateSpace();
 		}
 	}
 
 	public InventoryFrame createIF() {
 		return new InventoryFrame(player, this);
 	}
 
 	public void playerStatusChanged() {
 		if (player.hasFuel() == false) {
 			// End the game with a loss
 
 			this.setBackground(Color.DARK_GRAY);
 			this.removeAll();
 			int xsize = 400;
 			int ysize = 80;
 			empty.setFont(new Font(empty.getFont().getFontName(), Font.BOLD, 24));
 			empty.setBounds(SHIFT - (xsize / 2), 20, xsize, ysize);
 			empty.setForeground(Color.WHITE);
 			empty.setHorizontalAlignment(JLabel.CENTER);
 			empty.setVerticalAlignment(JLabel.TOP);
 			empty.setVisible(true);
 
 			quit.setBounds(SHIFT - (xsize / 2), 100, xsize, ysize);
 			quit.setIcon(new ImageIcon("Art" + File.separator + "LostButton.png"));
 			quit.setSelectedIcon(new ImageIcon("Art" + File.separator + "LostButtonPressed.png"));
 			quit.setOpaque(false);
 			quit.setContentAreaFilled(false);
 			quit.setBorderPainted(false);
 			quit.setVisible(true);
 			quit.addActionListener(new ActionListener() {
 
 				public void actionPerformed(ActionEvent arg0) {
 					uw.resetGame(); // resets the game(obviously).
 				}
 			});
 
 			add(empty);
 			add(quit);
 		} else {
 			empty.setVisible(false);
 			quit.setVisible(false);
 			this.setBackground(Color.BLACK);
 		}
 		fuelLabel.setText("Ship Fuel: " + player.getFuelLevel());
 	}
 
 	public void renderScene(Location[] locations) {
 		JLabel Header = new JLabel(player.getLoc().toString());
 		Header.setForeground(Color.GREEN);
 		Header.setBounds(0, 0, 400, 100);
 		this.add(Header);
 		for (int j = 0; j < locations.length; j++) {
 			Location loc = locations[j];
 			JLabel p;
 			if (loc.getClass() != Ring.class && loc.getClass() != Cluster.class) {
 				p = new JLabel(new ImageIcon(loc.GetPic(DEFAULT_SIZE)));
 				p.setBackground(Color.BLACK);
 				p.setOpaque(false);
 				int length = loc.GetPic().getWidth();
 				p.setBounds((int) (loc.GetX() + SHIFT - (length / 3)),
 						(int) (loc.GetY() + SHIFT - (length / 3)), length,
 						length);
 				this.add(p);
 			}
 			if (loc.getClass() == Cluster.class) {
 				int length = 100;
 				p = new JLabel(new ImageIcon(PictureAlbum.getScaledSquareImage(
 						loc.GetPic(), length)));
 				p.setBackground(Color.BLACK);
 				p.setOpaque(false);
 				p.setBounds((int) (loc.GetX() + SHIFT),
 						(int) (loc.GetY() + SHIFT), length, length);
 				this.add(p);
 			}
 		}
 		for (int j = 0; j < locations.length; j++) // Rings need to be rendered
 													// after planets
 		{
 			Location loc = locations[j];
 			JLabel p;
 			if (loc.getClass() == Ring.class) {
 				int length = 250 + (j * 60);
 				p = new JLabel(new ImageIcon(PictureAlbum.getScaledSquareImage(
 						loc.GetPic(), length)));
 				p.setBackground(Color.BLACK);
 				p.setOpaque(false);
 				p.setBounds((int) (SHIFT - (length / 2)),
 						(int) (SHIFT - (length / 2)), length, length);
 				this.add(p);
 			}
 		}
 		if (player.getLoc().getClass() == Cluster.class) {
 			JLabel singularity = new JLabel(new ImageIcon(player.getLoc()
 					.GetCenterImage()));
 			singularity.setBounds(SHIFT - 64, SHIFT - 64, 128, 128);
 			this.add(singularity);
 		}
 		if (locations[0].getParent().whatAmI() == 3) {
 
 			JLabel center = new JLabel(new ImageIcon(
 					locations[0].GetCenterImage()));
 			center.setBounds(SHIFT
 					- (locations[0].GetCenterImage().getWidth() / 2), SHIFT
 					- (locations[0].GetCenterImage().getHeight() / 2),
 					locations[0].GetCenterImage().getWidth(), locations[0]
 							.GetCenterImage().getHeight());
 			this.add(center);
 		} else if (player.getLoc().getClass() == Galaxy.class) {
 			JLabel center = new JLabel(new ImageIcon(
 					PictureAlbum.getScaledSquareImage(player.getLoc()
 							.GetCenterImage(), DEFAULT_WINDOW_SIZE)));
 			center.setBounds(0, 0, DEFAULT_WINDOW_SIZE, DEFAULT_WINDOW_SIZE);
 			this.add(center);
 		} else {
 			JLabel center = new JLabel(
 					new ImageIcon(PictureAlbum.getScaledSquareImage(
 							locations[0].GetCenterImage(), DEFAULT_WINDOW_SIZE)));
 			center.setBounds(0, 0, DEFAULT_WINDOW_SIZE, DEFAULT_WINDOW_SIZE);
 			this.add(center);
 		}
 
 		JLabel bg = new JLabel(new ImageIcon(PictureAlbum.getScaledSquareImage(
 				player.getLoc().GetBGImage(), DEFAULT_WINDOW_SIZE)));
 		bg.setBounds(0, 0, DEFAULT_WINDOW_SIZE, DEFAULT_WINDOW_SIZE);
 		this.add(bg);
 	}
 
 	private class LocationListener implements ActionListener {
 
 		private int index; // The index of the child location
 
 		public LocationListener(int index) {
 			this.setIndex(index);
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (player.hasFuel()) {
 				if (player.getLoc().getClass() == Galaxy.class) {
 					player.getLoc().Generate(index);
 					player.setLoc(currentLocation.getChild(-1));
 				} else {
 					player.setLoc(currentLocation.getChild(index));
 				}
 				populateSpace();
 				player.useFuel();
 				fuelLabel.setText("Ship Fuel: " + player.getFuelLevel());
 				playerStatusChanged();
 			}
 		}
 
 		public int getIndex() {
 			return index;
 		}
 
 		public void setIndex(int index) {
 			this.index = index;
 		}
 
 	}
 
 }
