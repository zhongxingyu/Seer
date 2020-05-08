 package jfmi.gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 
 /** A FileSearchDialog provides a JDialog which allows a user to search for
   files.
   */
 public class FileSearchDialog<T extends Comparable<T>> extends JDialog {
 
 	// Private Class Fields
 	private static final boolean IS_MODAL = true;
 	private static final String TITLE = "Select Items";
 
 	// Private Instance Fields
 	FormBox formBox;
 	ListSelectionBox<T> listBox;
 
 	JButton searchButton;
 
 
 	//************************************************************
 	// PUBLIC Instance Methods
 	//************************************************************
 
 	/** Constructs a default FileSearchDialog with default title, form, and
 	  selection list. Before using this instance, the setSearchListener()
 	  method should be called.
 	  */
 	public FileSearchDialog()
 	{
 		this(null, TITLE, null, null, null);
 	}
 
 	/** Creates a FileSearchDialog with the specified owner frame, title, search
 	  form, and list of search items. The confirmListener, form, and list 
 	  parameters can be null, but they should be set before calling other methods.
 	  @param parent owner frame of the dialog
 	  @param title title string of the dialog
 	  @param searchListener an ActionListener to handle the event generated
 	  			when the user clicks the search confirmation button
 	  @param form a FormBox containing a search form for the user to fill out
 	  @param list a ListSelectionBox<T> which will display a list of pre-made
 	  		search criteria to the user
 	  */
 	public FileSearchDialog(
 			JFrame parent, 
 			String title, 
 			ActionListener searchListener,
 			FormBox form, 
 			ListSelectionBox<T> list
 	)
 	{
 		// Initialize instance
 		super(parent, title, IS_MODAL);
 		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
 		setVisible(false);
 		formBox = form;
 		listBox = list;
 
 		// Initialize button
 		searchButton = new JButton("Search");
 
 		// Add child components
 		Box content = Box.createVerticalBox();
 		content.add(formBox);
 		content.add(listBox);
 		content.add(Box.createVerticalStrut(5));
 		content.add(searchButton);
 
 		add(content);
 		pack();
 	}
 
 	/** Accesses the search form used by this instance.
 	  @return the search form component
 	  */
 	public FormBox getForm()
 	{
 		return formBox;
 	}
 
 	/** Accesses the list of search items used by this instance.
 	  @return the search list component
 	  */
 	public ListSelectionBox<T> getList()
 	{
 		return listBox;
 	}
 
 	/** Accesses the search button.
 	  @return a reference to the search button
 	  */
 	public JButton getSearchButton()
 	{
 		return searchButton;
 	}
 
 	/** Sets the search form used by this instance.
 	  @param form the search form to be used
 	  */
 	public void setForm(FormBox form)
 	{
 		formBox = form;
 	}
 
 	/** Sets the list of search criteria to be used by this instance.
 	  @param list a ListSelectionBox<T> of items the user can search for
 	  */
 	public void setList(ListSelectionBox<T> list)
 	{
 		listBox = list;
 	}
 
 	/** Sets the ActionListener to alert when the user clicks the search
 	  button. All other ActionListeners are removed.
 	  @param listener object to inform about ActionEvents
 	  */
 	public void setSearchListener(ActionListener listener)
 	{
 		for (ActionListener l : searchButton.getActionListeners()) {
 			searchButton.removeActionListener(l);
 		}		
 
 		searchButton.addActionListener(listener);
 	}
 
 }
