 package tesseract.menuitems;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JTextField;
 import javax.vecmath.Vector3f;
 
 import tesseract.World;
 
 /**
  * Abstract class for menu items.
  * 
  * @author Jesse Morgan, Steve Bradshaw
  */
 public abstract class TesseractMenuItem 
 	extends JMenuItem implements ActionListener {
 
 	/**
 	 * Serial ID.
 	 */
 	private static final long serialVersionUID = 1839955501629795920L;
 	
 	/**
 	 * A Default radius.
 	 */
 	private static final float DEFAULT_RADIUS = 0.1f;
 	
 	/**
 	 * A Default position.
 	 */
 	private static final Vector3f DEFAULT_POSITION = new Vector3f(0,0,0);
 	
 	/**
 	 * The reference to the world.
 	 */
 	protected World myWorld;
 	
 	/**
 	 * The default button
 	 */
 	private JCheckBox my_default_button;
 	
 	/**
 	 * A Parameter setting JFrame
 	 */
 	private JFrame my_param_frame;
 	
 	/**
 	 * A position.
 	 */
 	private Vector3f my_position;
 	
 	/**
 	 * A radius field.
 	 */
 	private float my_radius;
 	
 	/**
 	 * A mass field.
 	 */
 	private float my_mass;
 	
 	/**
 	 * A position input.
 	 */
 	private JTextField my_position_input;
 	
 	/**
 	 * A radius input.
 	 */
 	private JTextField my_radius_input;
 	
 	/**
 	 * A mass input.
 	 */
 	private JTextField my_mass_input;
 	
 	/**
 	 * The button that get all inputs for shape
 	 */
 	private JButton my_enter_button;
 	
 	/**
 	 * Constructor.
 	 * 
 	 * @param theWorld The world in which to add.
 	 * @param theLabel The label for the menu item.
 	 */
 	public TesseractMenuItem(final World theWorld, final String theLabel) {
 		super(theLabel);
 		
 		myWorld = theWorld;
 		
 		my_position = new Vector3f(0,0,0);
 		my_radius = 0f;
 		my_mass = 1;
 		
 		addActionListener(this);
 	}
 	
 	/**
 	 * Utility method to parse a string formatted as x,y,z into a vector3f.
 	 * 
 	 * @param input A string to parse.
 	 * @return A vector3f.
 	 */
 	protected Vector3f parseVector(final String input)  {
 		String[] split = input.split(",");
 		
 		float x = Float.parseFloat(split[0]);
 		float y = Float.parseFloat(split[1]);
 		float z = Float.parseFloat(split[2]);
 
 		return new Vector3f(x, y, z);
 	}
 	
 	protected void createParameterMenu() {
 		
 		my_param_frame = new JFrame("Parameters");
 		my_param_frame.setBackground(Color.GRAY);
 		my_param_frame.setLayout(new GridLayout(5,1));
 		
 		Toolkit tk = Toolkit.getDefaultToolkit();
 	    Dimension screenSize = tk.getScreenSize();
 	    int screenHeight = screenSize.height;
 	    int screenWidth = screenSize.width;
 	    my_param_frame.setSize(screenWidth / 4, screenHeight / 4);
 	    my_param_frame.setLocation(screenWidth / 4, screenHeight / 4);
 	    
 	    my_position_input = new JTextField(10);
 	    my_position_input.setText("0,0,0");
 	    my_radius_input = new JTextField(10);
 	    my_radius_input.setText(".1");
 	    my_mass_input = new JTextField(10);
 	    my_mass_input.setText("1");
 	    
 	    JLabel position_label = new JLabel("Enter Position:  ");
 	    JLabel radius_label = new JLabel("Enter Radius:  ");
 	    JLabel mass_label = new JLabel("Enter Mass:  ");
 	    
 	    my_enter_button = new JButton("ENTER");
 	    
 	    my_default_button = new JCheckBox("Default Shape   ");
 	    
 	    my_param_frame.add(my_default_button);
 	    my_param_frame.add(position_label);
 	    my_param_frame.add(my_position_input);
 	    my_param_frame.add(radius_label);
 	    my_param_frame.add(my_radius_input);
 	    my_param_frame.add(mass_label);
 	    my_param_frame.add(my_mass_input);
 	    my_param_frame.add(my_enter_button);
 
 	    my_param_frame.setAlwaysOnTop(true);
 	    my_param_frame.pack();
 	    my_param_frame.setVisible(isVisible());
 	}
 	
 	protected JCheckBox getDefaultButton() {
 		return my_default_button;
 	}
 	
 	protected JFrame getParamFrame() {
 		return my_param_frame;
 	}
 	
 	protected JButton getEnterButton() {
 		return my_enter_button;
 	}
 
 	/**
 	 * @return the defaultRadius
 	 */
 	public static float getDefaultRadius() {
 		return DEFAULT_RADIUS;
 	}
 
 	/**
 	 * @return the defaultPosition
 	 */
 	public static Vector3f getDefaultPosition() {
 		return DEFAULT_POSITION;
 	}
 	
 	/**
 	 * @return the input position.
 	 */
 	public Vector3f getPosition() {
 		return my_position;
 	}
 	
 	/**
 	 * @return the radius.
 	 */
 	public float getRadius() {
 		return my_radius;
 	}
 	
 	/**
 	 * @return the mass.
 	 */
 	public float getMass() {
 		return my_mass;
 	}
 	
 	/**
 	 * 
 	 * @param the_position the new position
 	 */
 	public void setPosition(final Vector3f the_position) {
 		my_position = the_position;
 	}
 	
 	/**
 	 * @param the_radius float sets the radius.
 	 */
 	public void setRadius(final float the_radius) {
 		my_radius = the_radius;
 	}
 	
 	/**
 	 * @param the_mass float sets the mass.
 	 */
 	public void setMass(final float the_mass) {
 		my_mass = the_mass;
 	}
 	
 	/**
 	 * @return the input position input
 	 */
 	public JTextField getPositionField() {
 		return my_position_input;
 	}
 	
 	/**
 	 * @return the radius input.
 	 */
 	public JTextField getRadiusField() {
 		return my_radius_input;
 	}
 	
 	/**
 	 * @return the mass input.
 	 */
 	public JTextField getMassField() {
 		return my_mass_input;
 	}
 }
