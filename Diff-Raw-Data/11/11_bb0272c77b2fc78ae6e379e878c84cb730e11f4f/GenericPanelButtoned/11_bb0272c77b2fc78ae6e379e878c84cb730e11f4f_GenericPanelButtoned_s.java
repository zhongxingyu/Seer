 
 
 
 package overwatch.gui;
 
 import java.awt.event.ActionListener;
 
 
 
 
 
 /**
  * GenericPanel with new/save/delete buttons and ActionListener functions pre-integrated.
  * 
  * @author  Lee Coakley
 * @version 1
  */
 
 
 
 
 
 public class GenericPanelButtoned<T> extends GenericPanel<T>
 {
 	protected StandardButtonTriplet buttons;
 	
 	
 	
 	
 	
 	/**
 	 * Create a generic panel with add/save/delete buttons.
 	 * @param searchLabelText
 	 * @param mainLabelText
 	 */
 	public GenericPanelButtoned( String searchLabelText )
 	{
 		super( searchLabelText );
 		buttons = addNewSaveDeleteButtons();
 	}
 	
 	
 	
 	
 	
 	// Button events
 	public void addNewListener   ( ActionListener e ) { buttons.addNew.addActionListener(e); }
 	public void addSaveListener  ( ActionListener e ) { buttons.save  .addActionListener(e); }
 	public void addDeleteListener( ActionListener e ) { buttons.delete.addActionListener(e); }
 }
