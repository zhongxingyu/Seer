 
 
 
 package overwatch.gui;
 
 import java.util.ArrayList;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.event.ListSelectionListener;
 import net.miginfocom.swing.MigLayout;
 
 
 
 
 
 /**
  * Panel for searching a list of short text items.
  * 
  * @author  Lee Coakley
  * @version 3
  * @see     NameRefPair
  */
 
 
 
 
 
 public class SearchPanel<T> extends JPanel
 {
 	private ArrayList<NameRefPair<T>> searchableItems;
 	 
 	private JLabel                label;
 	private JTextField            searchField;
 	private JButton               searchClear;
 	private JList<NameRefPair<T>> searchList;
 	private JScrollPane           scrollPane;
 	
 	
 	
 	
 	
 	private SearchPanel()
 	{
 		super(  new MigLayout("filly", "[grow]", "[][fill,grow][]")  );
 	}
 	
 	
 	
 	
 	
 	public SearchPanel( String labelText )
 	{
 		this(  labelText,  new ArrayList<NameRefPair<T>>()  );
 	}
 	
 	
 	
 	
 	
 	public SearchPanel( String labelText, ArrayList<NameRefPair<T>> searchables )
 	{
 		this();
 		setup( labelText, searchables );
 	}
 	
 	
 	
 	
 	
 	public SearchPanel( String labelText, T[] searchables )
 	{
 		this();
 		setup( labelText, toPairList(searchables) );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Generates an event when the selected list item changes.
 	 * @param lis
 	 */
 	public void addListSelectionListener( ListSelectionListener lis ) {
 		searchList.addListSelectionListener( lis );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Generates an event when the clear button is used.
 	 * @param lis
 	 */
 	public void addClearButtonListener( ActionListener lis ) {
 		searchClear.addActionListener( lis );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Set object/string pairs to be considered for searching/display.
 	 * Searching does not alter this data.
 	 * If the search field is empty then everything is displayed.
 	 */
 	public void setSearchableItems( ArrayList<NameRefPair<T>> items ) {		
 		searchableItems = (ArrayList<NameRefPair<T>>) items.clone();
 		resetDisplayedItems();
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Set object/string pairs to be considered for searching/display.
 	 * Searching does not alter this data.
 	 * If the search field is empty then everything is displayed.
 	 */
 	public void setSearchableItems( T[] array ) {		
 		setSearchableItems( toPairList(array) );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * As before, except for generic objects.
 	 * This would be an overload and a separate constructor, but 
 	 * Java's shitty type-erasure-based generics make that impossible. 
 	 */
 	public void setSearchableItemsByPlainList( ArrayList<T> items )
 	{		
 		ArrayList<NameRefPair<T>> pairs = new ArrayList<NameRefPair<T>>();
 		
 		for (T ref: items) {
 			pairs.add(  new NameRefPair<T>(ref, ref.toString())  );
 		}
 		
 		setSearchableItems( pairs );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Get the selected item.
 	 * Returns null if nothing is selected.
 	 * @return Selected item
 	 */
 	public T getSelectedItem()
 	{
 		if (this.hasSelectedItem()) {
 			return searchList.getSelectedValue().ref;
 		} else {
 			return null;
 		}
 	}
 	
 	
 	
 	
 	
 	public String getSelectedItemName()
 	{
 		if (this.hasSelectedItem()) {
 			return searchList.getSelectedValue().name;
 		} else {
 			return null;
 		}
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Sets the selected item.
 	 * If null, any selected item is de-selected.
 	 * @param item Referenced object
 	 * @return whether it existed
 	 */
 	public boolean setSelectedItem( T item ) 
 	{
 		if (item == null) {
 			searchList.clearSelection();
 		}
 		
 		doSearchClear();
 		
 		for (int i=0; i<getSearchableItemCount(); i++) {
 			if (searchableItems.get( i ).ref.equals(item)) {
 				setSelectedIndex( i );
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	
 	
 	
 
 	public boolean hasSelectedItem() {
 		return (!searchList.isSelectionEmpty());
 	}
 	
 	
 	
 	public int getSearchableItemCount() {
 		return searchableItems.size();
 	}
 	
 	
 	
 	public int getDisplayedItemCount() {
 		return searchList.getModel().getSize();
 	}
 	
 	
 	
 	public int getSelectedIndex() {
 		return searchList.getSelectedIndex();
 	}
 	
 	
 	
 	public void setSelectedIndex( int index ) {
 		searchList.setSelectedIndex( index );
 	}
 	
 	
 	
 	public String getSearchInput() {
 		return searchField.getText();
 	}
 	
 	
 	
 	public void clearSearch() {
 		doSearchClear();
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Internals
 	/////////////////////////////////////////////////////////////////////////
 	
 	private void setup( String labelText, ArrayList<NameRefPair<T>> searchableItems )
 	{
 		setupComponents( labelText );
 		setupActions();
 		setSearchableItems( searchableItems );
 	}
 	
 	
 	
 	
 	
 	private void setupComponents( String labelText )
 	{		
 		label       = new JLabel( labelText );
 		searchField = new JTextField( 12 );
 		searchClear = new JButton( "Clear" );
 		
 		searchList = new JList<NameRefPair<T>>();
 		searchList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
 		
 		scrollPane = new JScrollPane( searchList );
 		scrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
 		scrollPane.setVerticalScrollBarPolicy  ( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS      );
 		
 		add( label,       "wrap, growy 0" );
 		add( scrollPane,  "wrap, grow, hmin 72px" );
 		add( searchField, "growx, split 2" );
 		add( searchClear );
 	}
 	
 	
 	
 	
 	
 	private void setupActions()
 	{
 		// Search textfield
 		searchField.addKeyListener( new KeyListener() {
 			public void keyTyped   (KeyEvent ev) {}
 			public void keyPressed (KeyEvent ev) {}
 			public void keyReleased(KeyEvent ev) {  doSearchAndDisplayResults();  } 
 		} );
 		
 		
 		// Search clear button
 		searchClear.addActionListener( new ActionListener() { 
 			public void actionPerformed(ActionEvent ev) {  doSearchClear();  }
 		} );
 	}
 	
 	
 	
 	
 	
 	private void doSearchAndDisplayResults()
 	{
 		String userStr = searchField.getText();
 		
 		if (userStr.isEmpty()) {
 			resetDisplayedItems();
 		}
 		else 
 		{
 			ArrayList<NameRefPair<T>> searchResult = new ArrayList<NameRefPair<T>>();
 			
 			for (NameRefPair<T> item: searchableItems) 
 				if (searchCompare( item.name, userStr )) 
 					searchResult.add( item );
 
 			setDisplayedItems( searchResult );
 		}
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Compare the list item string with the user's search input.
 	 * Any partial case-insensitive match counts.
 	 * @param ref Reference string (internal)
 	 * @param com Comparison string (user input)
 	 * @return Bool, whether com matches ref according to the search criterion.
 	 */
 	private boolean searchCompare( String ref, String com )
 	{
 		String refLower = ref.toLowerCase();
 		String comLower = com.toLowerCase();
 		return refLower.contains( comLower );
 	}
 	
 	
 	
 	
 	
 	private void doSearchClear() {
 		searchField.setText( "" );
 		resetDisplayedItems();
 	}
 	
 	
 
 	
 	
 	private void setDisplayedItems( ArrayList<NameRefPair<T>> items ) {
 		searchList.setListData(  items.toArray(  new NameRefPair[items.size()]  )  );
 	}
 	
 	
 	
 	
 	
 	private void resetDisplayedItems() {
 		setDisplayedItems( searchableItems );
 	}
 	
 	
 	
 	
 	
 	private ArrayList<NameRefPair<T>> toPairList( T[] array )
 	{
 		ArrayList<NameRefPair<T>> list = new ArrayList<NameRefPair<T>>();
 		
 		for (T el: array) {
 			list.add( new NameRefPair<T>( el, el.toString() ) );
 		}
 		
 		return list;
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Test
 	/////////////////////////////////////////////////////////////////////////
 	
 	public static void main( String[] args )
 	{
 		javax.swing.JFrame frame = new javax.swing.JFrame();
 		
 		String[] searchables = { "Test", "Another test", "yes another" };
 		SearchPanel<String> sp = new SearchPanel<String>( "Test", searchables );
 		
 		frame.add( sp );
 		frame.setVisible( true );
 	}
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
