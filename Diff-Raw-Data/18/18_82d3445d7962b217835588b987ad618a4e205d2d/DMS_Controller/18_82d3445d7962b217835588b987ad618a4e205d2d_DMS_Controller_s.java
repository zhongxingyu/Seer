 package controllers;
 
 import java.awt.Dimension;
 
 import javax.swing.ImageIcon;
 import javax.swing.JCheckBox;
 import javax.swing.JOptionPane;
 
import views.DMS_View;

 import lib.OSProperties;
 import models.Language_Model;
 
 /**
  * <p>Facilitates communication between the DMS_View and other
  * components/widgets needing display or data manipulation. The
  * main view of the program is instantiated here.
  *
  * @author Adam Childs
  */
 public class DMS_Controller
 {
 	Config_Controller config_file = new Config_Controller();
 	DMS_View view = null;
 	Language_Model lang_model = new Language_Controller().getModel();
 	OSProperties osp = new OSProperties();
 
 	/**
 	 * <p>Instantiates the DMS_Controller, which calls the initView()
 	 * method with default parameters supplied.
 	 */
 	public DMS_Controller()
 	{
 		// Determine what size the window should be on the user's screen
 		Dimension d = null;
 		if (osp.isHighResolution())
 			d = new Dimension(1200, 780);
 		else if (osp.isMidResolution())
 			d = new Dimension(1000, 650);
 		else if (osp.isLowMidResolution())
 			d = new Dimension(750, 550);
 		else if (osp.isLowResolution())
 			d = new Dimension(600, 390);
 		else
 			d = new Dimension(750, 550); // Default
 
		initView(lang_model.getProgramNameText(),
 				d,
				new Image_Controller().loadImage("../images/program_icon_large.png")
 				);
 	}
 
 	/**
 	 * <p>Instantiates the DMS_Controller, which calls the initView(t, d)
 	 * method to create the main view of the program. User may supply
 	 * parameters.
 	 *
 	 * @param t The title of the program to be displayed in the frame
 	 * @param d The dimensions of the frame/window (width, height)
 	 * @param i The Image to be displayed in the user's "explorer" bar or "dock"
 	 */
 	public DMS_Controller(String t, Dimension d, ImageIcon i)
 	{
 		initView(t, d, i);
 	}
 
 	/**
 	 * <p>Instantiates the main view of the program.
 	 *
 	 * @param t The title of the program to be displayed in the frame
 	 * @param d The dimensions of the frame/window (width, height)
 	 * @param i The Image to be displayed in the user's "explorer" bar or "dock"
 	 */
 	public void initView(String t, Dimension d, ImageIcon i)
 	{
 		view = new DMS_View(t, d, i);
 		welcomeDialog();
 	}
 
 	/**
 	 * Displays a JOptionPane welcoming message only if the
 	 * <welcomeMessage> in the XML config file is set to true.
 	 * The user may disable this popup by checking the check box
 	 * on the JOptionPane.
 	 */
 	private void welcomeDialog()
 	{
 		if (config_file.getWelcomeMessage()) // if welcome message is to be shown
 		{
			final JCheckBox checkbox = new JCheckBox( "Do not show this message again." );
			Object[] welcomeParams = { config_file.getWelcomeText(), checkbox };
 
 			JOptionPane.showMessageDialog(view.getFrame(),
 					welcomeParams,
					"Welcome",
 					JOptionPane.INFORMATION_MESSAGE);
 
 			config_file.setWelcomeMessage(!checkbox.isSelected());
 		} else {
 			System.out.println("SYSTEM: Welcome message not shown. Config file reads false.");
 		}
 	}
 
 	/**
 	 * Returns the view instance.
 	 * @return The view instance which displays the main view of the program.
 	 */
 	public DMS_View getView()
 	{
 		return view;
 	}
 }
