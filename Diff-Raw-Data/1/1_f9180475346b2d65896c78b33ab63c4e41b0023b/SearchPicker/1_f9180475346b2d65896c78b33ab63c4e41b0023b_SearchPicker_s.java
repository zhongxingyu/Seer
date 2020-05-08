 
 
 
 package overwatch.gui;
 
 import java.util.ArrayList;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import overwatch.core.Gui;
 import net.miginfocom.swing.MigLayout;
 
 
 
 
 
 /**
  * A modal dialogue for picking stuff in a separate window.
  * When a selection is made it generates onPick events with the object that was picked.
  * If cancelled or closed, the same happens except the reference is null.
  * 
  * @author  Lee Coakley
  * @version 2
  * @see 	SearchPanel
  */
 
 
 
 
 
 public class SearchPicker<T> extends JDialog
 {
 	private ArrayList<PickListener<T>> pickListeners;
 	private boolean picked;
 	
 	private SearchPanel<T> searchPanel;
 	private JButton        buttOkay;
 	private JButton        buttCancel;
 	
 	
 	
 	
 	
 	public SearchPicker( JFrame frame, String title, String label, ArrayList<NameRefPair<T>> searchables )
 	{
 		super( frame, title );
 		
 		setLayout(  new MigLayout( "", "[grow]", "[grow,fill][]" )  );
 		setDefaultCloseOperation( DISPOSE_ON_CLOSE );
 		
 		pickListeners = new ArrayList<PickListener<T>>();
 		picked        = false;
 		
 		setupComponents( label, searchables );
 		setupActions();
 		
 		setSize( 240, 384 ); // Golden ratio
 		setVisible( true );
 	}
 	
 	
 	
 	
 	
 	public void addPickListener( PickListener<T> pl ) {
 		pickListeners.add( pl );
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Internals
 	/////////////////////////////////////////////////////////////////////////
 	
 	
 	
 	private void setupComponents( String label, ArrayList<NameRefPair<T>> searchables )
 	{
 		searchPanel = new SearchPanel<T>( label, searchables );
 		buttOkay    = new JButton( "OK" );
 		buttCancel  = new JButton( "Cancel" );
 		
 		buttOkay.setEnabled( false );
 		
 		add( searchPanel, "wrap, grow" );
 		add( buttOkay, "split 2, alignx right" );
 		add( buttCancel );
 	}
 	
 	
 	
 	
 	
 	private void setupActions()
 	{
 		buttOkay.addActionListener( new ActionListener() {
 			public void actionPerformed( ActionEvent e ) {
 				notifyListeners( searchPanel.getSelectedItem() );
 				picked = true;
 				dispose();
 			}
 		});
 		
 		
 		
 		buttCancel.addActionListener( new ActionListener() {
 			public void actionPerformed( ActionEvent e ) {
 				dispose();
 			}
 		});
 		
 		
 		
 		this.addWindowListener( new WindowListener() {
 			public void windowOpened     ( WindowEvent e ) {}
 			public void windowIconified  ( WindowEvent e ) {}
 			public void windowDeiconified( WindowEvent e ) {}
 			public void windowDeactivated( WindowEvent e ) {}
 			public void windowClosing    ( WindowEvent e ) {}
 			public void windowClosed     ( WindowEvent e ) { onClose(); }
 			public void windowActivated  ( WindowEvent e ) {}
 		});
 		
 		
 		
 		searchPanel.addListSelectionListener( new ListSelectionListener() {
 			public void valueChanged( ListSelectionEvent e ) {
 				buttOkay.setEnabled( searchPanel.hasSelectedItem() );
 			}
 		});		
 	}
 	
 	
 	
 	
 	
 	private void notifyListeners( T elem ) {
 		for (PickListener<T> pl: pickListeners) {
 			pl.onPick( elem );
 		}
 	}
 	
 	
 	
 	
 	
 	private void onClose() {
 		if ( ! picked)
 			notifyListeners( null );
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Test
 	/////////////////////////////////////////////////////////////////////////
 	
 	public static void main( String[] args )
 	{
 		Gui.setNativeStyle();
 		
 		Integer[] nums  = { 1, 2, 3 };
 		String[]  names = { "One", "Two", "Three" };		
 		
 		SearchPicker<Integer> sp = new SearchPicker<Integer>( 
 			new JFrame(),
 			"Test",
 			"label",
 			new NameRefPairList<Integer>( nums, names )
 		);
 		
 		sp.addPickListener( new PickListener<Integer>() {
 			public void onPick( Integer picked ) {
 				System.out.println( "Pick event: " + picked );
 			}
 		});
 	}
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
