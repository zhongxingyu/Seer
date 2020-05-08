 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.util.HashMap;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JLayeredPane;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JToggleButton;
 import javax.swing.KeyStroke;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 
 import visualization.MapComponent;
 import visualization.SliderComponent;
 import controller.Controller;
 
 /**
  * 
  * @author Anders
  * @author Pacmans
  * @version 10. April 2012
  * 
  */
 public class GUI {
 	// This field contains the current version of the program.
 	private static final String VERSION = "Version 1.0";
 	// The main frame of our program.
 	private JFrame frame;
 	private Controller controller;
 	private JPanel contentPane, loadingPanel, optionPanel, roadtypeBoxes, checkboxPanel;
 	// The map from the controller
 	private MapComponent map;
 	private JLayeredPane layer;
 	private Component area = Box.createRigidArea(new Dimension(200,253));
 	private SliderComponent slider;
 	// A ButtonGroup with car, bike, and walk.
 	private ButtonGroup group;
 	private HashMap<String, JCheckBox> boxes = new HashMap<String, JCheckBox>();
 	private TransportationType selectedTransport;
 	// selected JToggleButton - 0 if car, 1 if bike, 2 if walk.
 	private int number;
 	private JLabel statusbar = new JLabel(" ");
 	private JCheckBox manualControlBox;
 	private Dimension windowSize = new Dimension(860, 655);
 
 	public GUI() {
 		makeFrame();
 		makeMenuBar();
 		makeRightPanel();
 		setupFrame();
 		controller = Controller.getInstance();
 	}
 
 	private void setupFrame() {
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.pack();
 		frame.setMinimumSize(windowSize);
 		frame.setSize(windowSize);
 		frame.setState(Frame.NORMAL);
 		// place the frame at the center of the screen and show.
 		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
 		frame.setLocation(d.width / 2 - frame.getWidth() / 2, d.height / 2
 				- frame.getHeight() / 2);
 		contentPane.setEnabled(false);
 		frame.setBackground(Color.darkGray);
 		updateGUI();
 	}
 
 	private void updateGUI() {
 		frame.pack();
 		frame.setVisible(true);
 //		area = Box.createRigidArea(roadtypeBoxes.getSize());
 		if (manualControlBox.isSelected()) {
 			area.setVisible(false);
 			roadtypeBoxes.setVisible(true);
 		} else {
 			roadtypeBoxes.setVisible(false);
 			area.setVisible(true);
 		}
 	}
 
 	public void setupMap() {
     map = controller.getMap();
     slider = controller.getSlider();
 
     map.addMouseWheelListener(new MouseWheelListener() {
       public void mouseWheelMoved(MouseWheelEvent e) {
        int zoom = map.getZoomLevel();
         slider.setSlider(zoom);
       }
     });
     frame.setVisible(false);
     contentPane.remove(loadingPanel);
 
     slider.setBounds(15,10,70,200);
 
     layer = new JLayeredPane();
     layer.add(map, new Integer(1));
     layer.add(slider, new Integer(2));
     layer.addComponentListener(new ComponentAdapter(){
 
       @Override
       public void componentResized(ComponentEvent e) {
         frame.repaint();
         map.setBounds(0,0,layer.getWidth(),layer.getHeight());
       }
     });
     contentPane.add(layer, "Center");
     contentPane.add(statusbar, "South");
 
     contentPane.setEnabled(true);
     frame.setBackground(Color.lightGray);
     updateGUI();
     map.setBounds(0,0,layer.getWidth(),layer.getHeight());
   }
 
 	private void makeFrame() {
 		// create the frame set the layout and border.
 		frame = new JFrame("Map Of Denmark");
 		contentPane = (JPanel) frame.getContentPane();
 		contentPane.setBorder(new EmptyBorder(4, 4, 4, 4));
 		contentPane.setLayout(new BorderLayout(5, 5));
 		loadingPanel = new JPanel(new FlowLayout(1));
 		loadingPanel.setBorder(new EmptyBorder(150, 6, 6, 6));
 		JLabel loadingLabel = new JLabel("Loading map...");
 		loadingLabel.setForeground(Color.white);
 		loadingLabel.setFont(new Font("Verdana", Font.BOLD, 40));
 		loadingPanel.add(loadingLabel);
 		contentPane.add(loadingPanel, "Center");
 	}
 
 	private void makeMenuBar() {
 		// Create key stroke shortcuts for the menu.
 		final int SHORTCUT_MASK = Toolkit.getDefaultToolkit()
 				.getMenuShortcutKeyMask();
 
 		// make the JMenuBar
 		JMenuBar menubar = new JMenuBar();
 		frame.setJMenuBar(menubar);
 
 		// create the JMenu fields.
 		JMenu menu;
 		JMenuItem item;
 
 		// create File menu
 		menu = new JMenu("File");
 		menubar.add(menu);
 
 		// make a new menu item and add a setAccelerator to use of shortcuts.
 		item = new JMenuItem("Quit");
 		item.setAccelerator(KeyStroke
 				.getKeyStroke(KeyEvent.VK_Q, SHORTCUT_MASK));
 		// create an actionlistener and call the method quit() when chosen.
 		item.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				quit();
 			}
 		});
 		menu.add(item);
 
 		// create Help menu
 		menu = new JMenu("Help");
 		menubar.add(menu);
 
 		item = new JMenuItem("About Map Of Denmark");
 		item.setAccelerator(KeyStroke
 				.getKeyStroke(KeyEvent.VK_A, SHORTCUT_MASK));
 		item.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				showAbout();
 			}
 		});
 		menu.add(item);
 	}
 
 	private void makeRightPanel() {
 		// initialize a new JPanel.
 		optionPanel = new JPanel();
 		// create a vertical BoxLayout on the optionPanel.
 		optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
 
 		// add the checkbox, and the other GUI to the right panel.
 		optionPanel.add(createRouteplanningBox());
 		optionPanel.add(createCheckbox());
 		optionPanel.add(area);
 		optionPanel.add(createZoomOutButton());
 		// add the optionPanel to the contentPanes borderlayout.
 		contentPane.add(optionPanel, "East");
 	}
 	
 	private JPanel createRouteplanningBox() {
 		JPanel routePlanning = new JPanel();
 		TitledBorder border = new TitledBorder(
 				new EtchedBorder(), "Route planning");
 		border = setHeadlineFont(border);
 		routePlanning.setBorder(border);
 		routePlanning.setLayout(new BoxLayout(routePlanning, BoxLayout.Y_AXIS));
 
 		// from row
     JLabel label = new JLabel("From");
     label = setLabelFont(label);
     
     JComboBox fromBox = new LiveSearchBox().getBox();
 
     JPanel fromPanel = new JPanel(new FlowLayout(2));
     fromPanel.add(label);
     fromPanel.add(fromBox);
 
     // to row
     label = new JLabel("To");
     label = setLabelFont(label);
     
     JComboBox toBox = new LiveSearchBox().getBox();
     JPanel toPanel = new JPanel(new FlowLayout(2));
     toPanel.add(label);
     toPanel.add(toBox);
 
 		// go button
 		JButton go = new JButton("Go");
 		go = setButtonText(go);
 		go.setPreferredSize(new Dimension(55, 33));
 		go.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				// get the selected transportation type and DO SOMETHING
 				// getSelectedTransportation()
 			}
 		});
 		JPanel goPanel = new JPanel(new FlowLayout(1));
 		goPanel.add(go);
 
 		routePlanning.add(fromPanel);
 		routePlanning.add(toPanel);
 		routePlanning.add(createTogglePanel());
 		routePlanning.add(goPanel);
 
 		return routePlanning;
 	}
 
 	private <T extends JComponent> T setLabelFont(T label) {
 		label.setFont(new Font("Verdana", Font.PLAIN, 14));
 		return label;
 	}
 
 	private TitledBorder setHeadlineFont(TitledBorder label) {
 		label.setTitleFont(new Font("Verdana", Font.BOLD, 15));
 		return label;
 	}
 
 	private <T extends JComponent> T setButtonText(T label) {
 		label.setFont(new Font("Verdana", Font.PLAIN, 14));
 		return label;
 	}
 
 	// toggleButtons in a ButtonGroup
 	private JPanel createTogglePanel() {
 		JPanel togglePanel = new JPanel(new FlowLayout(1));
 		group = new ButtonGroup();
 		ImageIcon icon = getScaledIcon(new ImageIcon("./src/icons/car.png"));
 		togglePanel.add(createJToggleButton(icon, true, TransportationType.CAR));
 
 		icon = getScaledIcon(new ImageIcon("./src/icons/bike.png"));
 		togglePanel.add(createJToggleButton(icon, false, TransportationType.BIKE));
 
 		icon = getScaledIcon(new ImageIcon("./src/icons/walk.png"));
 		togglePanel.add(createJToggleButton(icon, false, TransportationType.WALK));
 		return togglePanel;
 	}
 
 	private ImageIcon getScaledIcon(ImageIcon icon) {
 		Image img = icon.getImage();
 		Image newimg = img.getScaledInstance(30, 30,
 				java.awt.Image.SCALE_SMOOTH);
 		return new ImageIcon(newimg);
 	}
 
 	private JToggleButton createJToggleButton(ImageIcon ico, boolean selected,
 			TransportationType type) {
 		JToggleButton button = new JToggleButton();
 		final TransportationType _type = type;
 		if (selected == true)
 			button.setSelected(true);
 		button.setIcon(ico);
 		button.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent e) {
 				if (e.getStateChange() == ItemEvent.SELECTED) {
 					setSelectedTransportation(_type);
 					System.out.println(getSelectedTransportation());
 				}
 			}
 		});
 		group.add(button);
 		return button;
 	}
 
 	// return an enum
 	private TransportationType getSelectedTransportation() {
 		return selectedTransport;
 	}
 
 	// return 0 if car, 1 if bike, 2 if walk.
 	private void setSelectedTransportation(TransportationType type) {
 		selectedTransport = type;
 	}
 
 	private JPanel createZoomOutButton() {
     JPanel zoomPanel = new JPanel(new FlowLayout(1));
     JButton zoomOut = new JButton("Zoom out");
     zoomOut.setPreferredSize(new Dimension(110, 40));
     zoomOut = setButtonText(zoomOut);
     zoomOut.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         slider.setSlider(0);
         controller.showAll();
       }
     });
     zoomPanel.add(zoomOut);
     return zoomPanel;
   }
 
 	private JPanel createCheckbox() {
 		// initialize checkboxPanel
 		checkboxPanel = new JPanel();
 		checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
 		TitledBorder border = new TitledBorder(new EtchedBorder(), "Road types");
 		border = setHeadlineFont(border);
 		checkboxPanel.setBorder(border);
 		
 		// fill the checkboxPanel
 		JPanel manualPanel = new JPanel(new FlowLayout(0));
 		manualControlBox = new JCheckBox("Manual Control");
 		manualControlBox.setFont(new Font("Verdana", Font.CENTER_BASELINE, 15));
 		manualControlBox.setSelected(false);
 		manualControlBox.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent e) {
 			  if (e.getStateChange() == ItemEvent.SELECTED){
 			  	setRoadtypeSelections();
 			  	map.setManualControl(true);
 			  }
         else
           map.setManualControl(false);
         updateGUI();
       }
 		});
 		manualPanel.add(manualControlBox);
 
 		roadtypeBoxes = new JPanel(new GridLayout(7, 1));
 		roadtypeBoxes.add(createRoadtypeBox("Highways", true)); // Priority 1
 																// roads
 		roadtypeBoxes.add(createRoadtypeBox("Expressways", true)); // Priority 2
 		roadtypeBoxes.add(createRoadtypeBox("Primary roads", true)); // and so
 																		// on..
 		roadtypeBoxes.add(createRoadtypeBox("Secondary roads", true));
 		roadtypeBoxes.add(createRoadtypeBox("Normal roads", false));
 		roadtypeBoxes.add(createRoadtypeBox("Trails & streets", false));
 		roadtypeBoxes.add(createRoadtypeBox("Paths", false));
 		checkboxPanel.add(manualPanel);
 		checkboxPanel.add(roadtypeBoxes);
 		
 		return checkboxPanel;
 	}
 
 	private void setRoadtypeSelections()
 	{
 		boolean[] roadtypes = map.getRoadtypes();
 		
 		boxes.get("Highways").setSelected(roadtypes[0]);
 		boxes.get("Expressways").setSelected(roadtypes[1]);
 		boxes.get("Primary roads").setSelected(roadtypes[2]);
 		boxes.get("Secondary roads").setSelected(roadtypes[3]);
 		boxes.get("Normal roads").setSelected(roadtypes[4]);
 		boxes.get("Trails & streets").setSelected(roadtypes[5]);
 		boxes.get("Paths").setSelected(roadtypes[6]);
 	}
 	
 	private JPanel createRoadtypeBox(String string, boolean selected) {
 		JPanel fl = new JPanel(new FlowLayout(0));
 		JCheckBox box = new JCheckBox(string);
 		box = setLabelFont(box);
 		box.setSelected(selected);
 		box.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent e) {
 				JCheckBox box = (JCheckBox) e.getSource();
 				if (box.getText().equals("Highways"))
 					number = 1;
 				if (box.getText().equals("Expressways"))
 					number = 2;
 				if (box.getText().equals("Primary roads"))
 					number = 3;
 				if (box.getText().equals("Secondary roads"))
 					number = 4;
 				if (box.getText().equals("Normal roads"))
 					number = 5;
 				if (box.getText().equals("Trails & streets"))
 					number = 6;
 				if (box.getText().equals("Paths"))
 					number = 7;
 				if (e.getStateChange() == ItemEvent.SELECTED)
 					controller.updateMap(number, true);
 				else
 					controller.updateMap(number, false);
 			}
 		});
 		boxes.put(string, box);
 		fl.add(box);
 		return fl;
 	}
 
 	public void setStatus(String text) {
 		statusbar.setText(text);
 	}
 
 	public void quit() {
 		// Exits the application.
 		System.exit(0);
 	}
 
 	/**
 	 * Creates a message dialog which shows the current version of the
 	 * application.
 	 */
 	private void showAbout() {
 		JOptionPane.showMessageDialog(frame, "Map Of Denmark - " + VERSION
 				+ "\nMade by Claus, BjÃ¸rn, Phillip, Morten & Anders.",
 				"About Map Of Denmark", JOptionPane.INFORMATION_MESSAGE);
 	}
   
   public void enableFrame() {
 	  contentPane.setEnabled(true);
 	  frame.setBackground(Color.lightGray);
   }
 }
