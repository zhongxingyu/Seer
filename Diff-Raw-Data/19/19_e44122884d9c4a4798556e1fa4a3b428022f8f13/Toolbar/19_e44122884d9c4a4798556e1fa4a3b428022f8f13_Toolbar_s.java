 /**
  * 
  */
 package no.whg.mini;
 
 import java.awt.*;
 import javax.swing.ImageIcon;
 import javax.swing.JToolBar;
 import javax.swing.JButton;
 /**
  * Class that handles the toolbar of the program, save buttons etc
  * @author Fredrik, Peer
  *
  */
 public class Toolbar extends JToolBar
 {
 	public Toolbar()
 	{
 		JToolBar toolBar = new JToolBar();
 		toolBar.setMinimumSize(new Dimension(400, 50));
 		toolBar.setMaximumSize(new Dimension(400,80));
		setFloatable(false);
 		setRollover(true);
 		
 		JButton newButton = new JButton();
 		
 		ImageIcon newImage = new ImageIcon("gbleditor_icons/NEW.GIF");
 		newButton = new JButton(newImage);
 		newButton.setToolTipText("Create a new layout");
 		add(newButton);
 		
 		ImageIcon newOpenImage = new ImageIcon("gbleditor_icons/OPENDOC.GIF");
 		newButton = new JButton(newOpenImage);	
 		newButton.setToolTipText("Open document");
 		add(newButton);
 		
 		ImageIcon newSaveImage = new ImageIcon("gbleditor_icons/SAVE.GIF");
 		newButton = new JButton(newSaveImage);
 		newButton.setToolTipText("Save");
 		add(newButton);
 		
 		addSeparator();
 		
 		ImageIcon newSaveJavaImage = new ImageIcon("gbleditor_icons/SAVEJAVA.GIF");
 		newButton = new JButton(newSaveJavaImage);
 		newButton.setToolTipText("Generate the code");
 		add(newButton);
 		
 		ImageIcon newExecuteImage = new ImageIcon("gbleditor_icons/ExecuteProject.GIF");
 		newButton = new JButton(newExecuteImage);
 		newButton.setToolTipText("Run the program");
 		add(newButton);
 		
 		addSeparator();
 		
 		ImageIcon newRowImage = new ImageIcon("gbleditor_icons/NEWROW.GIF");
 		newButton = new JButton(newRowImage);
 		newButton.setToolTipText("Add a new row");
 		add(newButton);
 		
 		ImageIcon newRowDownImage = new ImageIcon("gbleditor_icons/MoveRowDown.GIF");
 		newButton = new JButton(newRowDownImage);
 		newButton.setToolTipText("Move row down");
 		add(newButton);
 		
 		ImageIcon newRowUpImage = new ImageIcon("gbleditor_icons/MoveRowUp.GIF");
 		newButton = new JButton(newRowUpImage);
 		newButton.setToolTipText("Move row up");
 		add(newButton);
 		
 		addSeparator();
 		
 		ImageIcon newHelpImage = new ImageIcon("gbleditor_icons/HELP.GIF");
 		newButton = new JButton(newHelpImage);
 		newButton.setToolTipText("Help");
 		add(newButton);
 	}
 	
 
 }
