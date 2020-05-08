 package core;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 import java.awt.image.BufferedImage;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollBar;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.SwingUtilities;
 
 /**
  * The main class for the Mapping program, handles the GUI and interaction with
  * the program
  *
  * @author Oliver Greenaway
  *
  */
 public class Mapper extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 
 	// JFrame Components
 	private JPanel drawingPane = new JPanel();
 	private JPanel menuPane = new JPanel();
 	private JPanel buttonPane = new JPanel();
 	private JPanel textOutputPane = new JPanel();
 	private JButton loadDataButton = new JButton("Load Data");
 	private JButton articulationButton = new JButton("Critical Points");
 	public static JTextArea textArea = new JTextArea();
 	private JScrollPane scrollingTextBox = new JScrollPane(textArea);
 	private JComboBox dropDown = new JComboBox();
 	List<Road> selectedRoads;
 
 	// Rendering Objects
 	BufferedImage buffer;
 
 	// Directory containing map data
 	private String dataDirectory = "";
 
 	// Map Objects
 	private Map map;
 
 	/**
 	 * Creates a Mapper instance initializing the GUI and ActionListeners
 	 */
 	public Mapper() {
 
 		// Sets title, size and close operation of the JFrame
 		super("Auckland Road System");
 		this.setSize(800, 800);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setResizable(true);
 
 		// Assign position and graphic variables to components
 		menuPane.setBackground(Color.lightGray);
 		buttonPane.setBackground(Color.lightGray);
 		drawingPane.setBackground(Color.white);
 		textOutputPane.setBackground(Color.lightGray);
 		loadDataButton.setSize(120, 30);
 		articulationButton.setSize(120, 30);
 		textArea.setRows(10);
 		textArea.setColumns(getWidth() / 15);
 		textArea.setEnabled(true);
 		textArea.setMaximumSize(new Dimension(1000, 500));
 		scrollingTextBox.setSize(textArea.getWidth(), textArea.getHeight());
 		dropDown.setMaximumRowCount(10);
		dropDown.setEditable(false);
 
 		// Create ActionListeners for each component
 		drawingPane.addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent e) {
 				panelMousePressed(e);
 			}
 
 			public void mouseClicked(MouseEvent e) {
 				panelMouseClicked(e);
 			}
 
 			public void mouseReleased(MouseEvent e) {
 				panelMouseReleased(e);
 			}
 		});
 		drawingPane.addMouseMotionListener(new MouseAdapter() {
 			public void mouseDragged(MouseEvent e) {
 				panelMouseDragged(e);
 			}
 		});
 		drawingPane.addMouseWheelListener(new MouseAdapter() {
 			public void mouseWheelMoved(MouseWheelEvent e) {
 				panelMouseWheelMoved(e);
 			}
 		});
 		ActionListener aListener = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				buttonPerformed(e);
 			}
 		};
 		loadDataButton.addActionListener(aListener);
 		articulationButton.addActionListener(aListener);
 		dropDown.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				dropDownAction(e);
 
 			}
 		});
 		dropDown.addItemListener(new ItemListener() {
 
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 
 				dropDownItemChange(e);
 
 			}
 		});
 
 		// Add components to the JFrame and display
 		Container con = this.getContentPane();
 		con.setLayout(new BorderLayout());
 		menuPane.setLayout(new BorderLayout());
 		con.add(menuPane, BorderLayout.NORTH);
 		con.add(drawingPane, BorderLayout.CENTER);
 		con.add(textOutputPane, BorderLayout.SOUTH);
 		buttonPane.add(loadDataButton);
 		buttonPane.add(articulationButton);
 		menuPane.add(buttonPane, BorderLayout.WEST);
 		menuPane.add(dropDown, BorderLayout.EAST);
 		textOutputPane.add(scrollingTextBox);
 		this.setVisible(true);
 	}
 
 	// Mouse position variables
 	private int pMouseX, pMouseY, mouseX, mouseY;
 
 	/**
 	 * Called when the mouse is pressed on the graphics panel
 	 *
 	 * @param e
 	 */
 	public void panelMousePressed(MouseEvent e) {
 		mouseX = e.getX();
 		mouseY = e.getY();
 	}
 
 	/**
 	 * Called when the mouse is released from the graphics panel
 	 *
 	 * @param e
 	 */
 	public void panelMouseReleased(MouseEvent e) {
 	}
 
 	/**
 	 * Called when the mouse is clicked on the graphics pane
 	 * @param e
 	 */
 	public void panelMouseClicked(MouseEvent e) {
 		if (map != null) {
 			if(SwingUtilities.isLeftMouseButton(e)){
 				map.clickedSourceNode(e.getX(), e.getY());
 			}else if(SwingUtilities.isRightMouseButton(e)){
 				map.clickedDestNode(e.getX(), e.getY());
 			}
 		}
 		repaint();
 	}
 
 	/**
 	 * Called when the mouse is dragged on the graphics pannel
 	 *
 	 * @param e
 	 */
 	public void panelMouseDragged(MouseEvent e) {
 		pMouseX = mouseX;
 		pMouseY = mouseY;
 		mouseX = e.getX();
 		mouseY = e.getY();
 		if (map != null) {
 			map.moveMap(pMouseX - mouseX, pMouseY - mouseY);
 		}
 		repaint();
 	}
 
 	/**
 	 * Called when the mouse wheel is moved
 	 *
 	 * @param e
 	 */
 	public void panelMouseWheelMoved(MouseWheelEvent e) {
 		map.zoom(e.getWheelRotation() * -1);
 		repaint();
 	}
 
 	/**
 	 * Called when a Button is pressed
 	 *
 	 * @param e
 	 */
 	public void buttonPerformed(ActionEvent e) {
 		if (e.getSource() == loadDataButton) {
 			JFileChooser fc = new JFileChooser();
 			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 			int returnValue = fc.showOpenDialog(this);
 			if (returnValue == JFileChooser.APPROVE_OPTION) {
 				repaint();
 				dataDirectory = fc.getSelectedFile().getPath() + "/";
 				map = new Map(dataDirectory);
 			} else {
 				return;
 			}
 		}else if(e.getSource() == articulationButton){
 			if(map != null){
 				map.findArticulations();
 			}
 		}
 		repaint();
 	}
 
 	/**
 	 * Called when the combo box is edited Edits the items in the combo box
 	 * based on the text currently in the combo box
 	 *
 	 * @param e
 	 */
 	public void dropDownAction(ActionEvent e) {
 		if (e.getActionCommand().equals("comboBoxEdited") && map != null) {
 			map.setSelectedRoad(null);
 			String text = (String) dropDown.getSelectedItem();
 			dropDown.removeAllItems();
 			dropDown.addItem(text);
 			selectedRoads = map.getTen(text);
 			for (Road r : selectedRoads) {
 				dropDown.addItem(r.getName());
 			}
 		}
 		repaint();
 	}
 
 	/**
 	 * Called when the item in the dropdown is changed, updates the selected road
 	 * to the selected item
 	 *
 	 * @param e
 	 */
 	public void dropDownItemChange(ItemEvent e) {
 		if (selectedRoads != null) {
 			for (Road r : selectedRoads) {
 				if (r.getName().equals(e.getItem())) {
 					map.setSelectedRoad(r);
 				}
 			}
 		}
 		repaint();
 	}
 
 	/**
 	 * Renders a graphical output to be displayed
 	 */
 	private void updateBuffer() {
 		buffer = new BufferedImage(drawingPane.getWidth(),
 				drawingPane.getHeight(), BufferedImage.TYPE_INT_RGB);
 		Graphics2D g2 = buffer.createGraphics();
 		g2.setColor(new Color(239, 232, 204));
 		g2.fillRect(0, 0, drawingPane.getWidth(), drawingPane.getHeight());
 		g2.setColor(Color.black);
 		if (map != null) {
 			map.draw(g2);
 		}
 		g2.dispose();
 	}
 
 	/**
 	 * Updates Graphics and displays the current buffer image onto the window
 	 */
 	public void paint(Graphics g) {
 		updateBuffer();
 		Graphics2D g2 = (Graphics2D) drawingPane.getGraphics();
 		if (buffer != null) {
 			g2.drawImage(buffer, 0, 0, this);
 		}
 		menuPane.repaint();
 		textOutputPane.repaint();
 	}
 
 	/**
 	 *
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		new Mapper();
 	}
 
 }
