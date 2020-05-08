 
 
 
 package overwatch.core;
 
 import overwatch.controllers.TabController;
 import overwatch.gui.DialogueAnswer;
 import overwatch.gui.tabs.*;
 import java.awt.Component;
 import javax.swing.*;
 import javax.swing.event.*;
 
 
 
 
 
 /**
  * Core GUI class.
  * 
  * @author  Lee Coakley
  * @version 3
  */
 
 
 
 
 
 public class Gui extends JFrame
 {
 	// Statics
 	private static Gui currentInstance; // Main JFrame
 	
 
 	
 	// Instance GUI elements
 	public final JTabbedPane  tabPane;
 	public final PersonnelTab personnelTab;
 	public final RankTab      rankTab;
 	public final SupplyTab    supplyTab;
 	public final VehicleTab   vehicleTab;
 	
 	
 	
 	
 	
 	public Gui()
 	{
 		super( "Overwatch" );
 		
 		currentInstance = this;
 		
 		// Create
 		tabPane = new JTabbedPane();
 		personnelTab = addTab( "Personnel", new PersonnelTab() ); 
 		rankTab      = addTab( "Ranks",     new RankTab     () );      
 		vehicleTab   = addTab( "Vehicles",  new VehicleTab  () );   
 		supplyTab    = addTab( "Supplies",  new SupplyTab   () );    
 
 		add( tabPane );
 		
 		setDefaultCloseOperation( EXIT_ON_CLOSE );
 		pack();
 		setVisible(true);
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Get a reference to the most recently created Gui instance.
 	 */
 	public static Gui getCurrentInstance() {
 		return currentInstance;
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Register a TabController so that its respondToTabSelect() function
 	 * will be called when that particular tab is selected.
 	 * @param tabController
 	 */
	public void addTabSelectNotify( final TabController tabController )
 	{
 		tabPane.addChangeListener( new ChangeListener() {
 			public void stateChanged( ChangeEvent e )
 			{
				if (tabPane.getSelectedComponent() == tabController) {
 					tabController.respondToTabSelect();
 				}
 			}
 		});
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Internals
 	/////////////////////////////////////////////////////////////////////////
 	
 	private <T extends Component> T addTab( String name, T tab ) {
 		tabPane.addTab( name, tab );
 		return tab;
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Statics
 	/////////////////////////////////////////////////////////////////////////
 	
 	
 	
 	/**
 	 * Set native GUI style based on the current platform.
 	 * Must be called FIRST, before any GUI components are created.
 	 */
 	public static void setNativeStyle()
 	{
 		try {
 			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
 		} catch( Exception ex ) {
 			ex.printStackTrace();
 		}	
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Modally ask a question, with three possible answers.
 	 * @param title
 	 * @param question
 	 * @return DialogueAnswer
 	 */
 	public static DialogueAnswer askYesNoCancel( String title, String question )
 	{
 		int answer = JOptionPane.showConfirmDialog( Gui.currentInstance, question, title, JOptionPane.YES_NO_CANCEL_OPTION );
 		
 		switch (answer) {
 			case JOptionPane.YES_OPTION:    return DialogueAnswer.YES;
 			case JOptionPane.NO_OPTION:     return DialogueAnswer.NO;
 			default:						return DialogueAnswer.CANCEL;
 		}
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Modally confirm an action.
 	 * @param title
 	 * @param question
 	 * @return boolean
 	 */
 	public static boolean askConfirm( String title, String question )
 	{
 		int answer = JOptionPane.showConfirmDialog( Gui.currentInstance, question, title, JOptionPane.YES_NO_OPTION );
 		
 		switch (answer) {
 			case JOptionPane.YES_OPTION: return true;
 			default:					 return false;
 		}
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Show a modal warning dialogue.
 	 * @param title
 	 * @param message
 	 */
 	public static void showWarningDialogue( String title, String message ) {
 		JOptionPane.showMessageDialog( Gui.currentInstance, message, title, JOptionPane.WARNING_MESSAGE );
 	}
 	
 	
 	
 		
 	
 	/**
 	 * Show a modal error dialogue.
 	 * @param title
 	 * @param message
 	 */
 	public static void showErrorDialogue( String title, String message ) {
 		JOptionPane.showMessageDialog( Gui.currentInstance, message, title, JOptionPane.ERROR_MESSAGE );
 	}
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
