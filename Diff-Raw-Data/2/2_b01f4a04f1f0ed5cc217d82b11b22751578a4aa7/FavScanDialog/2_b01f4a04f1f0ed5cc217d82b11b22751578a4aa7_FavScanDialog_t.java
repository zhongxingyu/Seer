 package com.hifiremote.jp1;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.ListSelectionModel;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 public class FavScanDialog extends JDialog implements ActionListener, ListSelectionListener
 {
 
   public FavScanDialog( Component c )
   {
     super( ( JFrame )SwingUtilities.getRoot( c ) );
     setTitle( "Fav/Scan" );
     setModal( true );
 
     JComponent contentPane = ( JComponent )getContentPane();
     contentPane.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
 
     JPanel panel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
 
     // Add the Fav/Scan definition controls
     Box macroBox = Box.createHorizontalBox();
     macroBox.setBorder( BorderFactory.createTitledBorder( "Fav/Scan Definition" ) );
     contentPane.add( macroBox, BorderLayout.CENTER );
 
     JPanel availableBox = new JPanel( new BorderLayout() );
     macroBox.add( availableBox );
     availableBox.add( new JLabel( "Available keys:" ), BorderLayout.NORTH );
     availableButtons.setFixedCellWidth( 100 );
     availableBox.add( new JScrollPane( availableButtons ), BorderLayout.CENTER );
     availableButtons.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
     availableButtons.addListSelectionListener( this );
 
     panel = new JPanel( new GridLayout( 4, 2, 2, 2 ) );
     panel.setBorder( BorderFactory.createEmptyBorder( 2, 0, 0, 0 ) );
     availableBox.add( panel, BorderLayout.SOUTH );
     add.addActionListener( this );
     panel.add( add );
     insert.addActionListener( this );
     panel.add( insert );
     addShift.addActionListener( this );
     panel.add( addShift );
     insertShift.addActionListener( this );
     panel.add( insertShift );
     addXShift.addActionListener( this );
     panel.add( addXShift );
     insertXShift.addActionListener( this );
     panel.add( insertXShift );
     addPause.addActionListener( this );
     panel.add( addPause );
     insertPause.addActionListener( this );
     panel.add( insertPause );
 
     macroBox.add( Box.createHorizontalStrut( 20 ) );
 
     JPanel keysBox = new JPanel( new BorderLayout() );
     macroBox.add( keysBox );
     keysBox.add( new JLabel( "Fav/Scan Keys:" ), BorderLayout.NORTH );
     macroButtons.setFixedCellWidth( 100 );
     keysBox.add( new JScrollPane( macroButtons ), BorderLayout.CENTER );
     macroButtons.setModel( macroButtonModel );
     macroButtons.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
     macroButtons.setCellRenderer( favScanButtonRenderer );
     macroButtons.addListSelectionListener( this );
 
     JPanel buttonBox = new JPanel( new GridLayout( 3, 2, 2, 2 ) );
     buttonBox.setBorder( BorderFactory.createEmptyBorder( 2, 0, 0, 0 ) );
     keysBox.add( buttonBox, BorderLayout.SOUTH );
     moveUp.addActionListener( this );
     buttonBox.add( moveUp );
     moveDown.addActionListener( this );
     buttonBox.add( moveDown );
     remove.addActionListener( this );
     buttonBox.add( remove );
     clear.addActionListener( this );
     buttonBox.add( clear );
 
     JPanel bottomPanel = new JPanel( new BorderLayout() );
     contentPane.add( bottomPanel, BorderLayout.SOUTH );
     // Add the notes
     panel = new JPanel( new BorderLayout() );
     bottomPanel.add( panel, BorderLayout.NORTH );
     panel.setBorder( BorderFactory.createTitledBorder( "Notes" ) );
     notes.setLineWrap( true );
     panel.add( new JScrollPane( notes, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
         ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER ) );
 
     // Add the action buttons
     panel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
     bottomPanel.add( panel, BorderLayout.SOUTH );
 
     okButton.addActionListener( this );
     panel.add( okButton );
 
     cancelButton.addActionListener( this );
     panel.add( cancelButton );
   }
 
   @Override
   public void actionPerformed( ActionEvent event )
   {
     Object source = event.getSource();
     Remote remote = config.getRemote();
     if ( source == okButton )
     {
       int length = macroButtonModel.getSize();
       if ( length == 0 )
       {
         showWarning( "You haven't included any keys in your macro!", 0 );
         return;
       }
       
       int keyCode = remote.getFavKey().getKeyCode();
 
       java.util.List< Short > keyList = new ArrayList< Short >();
       int j = 0;
       for ( int i = 0; i < length; ++i )
       {
         short keyValue = ( ( Number )macroButtonModel.elementAt( i ) ).shortValue();
         if ( keyValue != 0 )
         {
           keyList.add( keyValue );
           j++;
         }
         if ( keyValue == 0 || i == length - 1 )
         {
           for ( ; ( j % favKey.getEntrySize() ) != 0; j++ )
           {
             keyList.add( ( short )0 );
           }
         }
       }
       short[] keyCodes = new short[ keyList.size() ];
       for ( int i = 0; i < keyCodes.length; i++ )
       {
         keyCodes[ i ] = keyList.get( i );
       }
 
       String notesStr = notes.getText();
 
       favScan = new FavScan( keyCode, new Hex( keyCodes ), notesStr );
       favScan.setDeviceButton( deviceButton );
       setVisible( false );
     }
     else if ( source == cancelButton )
     {
       favScan = null;
       setVisible( false );
     }
     else if ( source == add )
     {
       addKey( getSelectedKeyCode(), 0 );
     }
     else if ( source == insert )
     {
       insertKey( getSelectedKeyCode(), 0 );
     }
     else if ( source == addShift )
     {
       addKey( getSelectedKeyCode(), remote.getShiftMask() );
     }
     else if ( source == insertShift )
     {
       insertKey( getSelectedKeyCode(), remote.getShiftMask() );
     }
     else if ( source == addXShift )
     {
       addKey( getSelectedKeyCode(), remote.getXShiftMask() );
     }
     else if ( source == insertXShift )
     {
       insertKey( getSelectedKeyCode(), remote.getXShiftMask() );
     }
     else if ( source == addPause )
     {
       addKey( 0, 0 );
     }
     else if ( source == insertPause )
     {
       insertKey( 0, 0 );
     }
     else if ( source == moveUp )
     {
       int index = macroButtons.getSelectedIndex();
       swap( index, index - 1 );
     }
     else if ( source == moveDown )
     {
       int index = macroButtons.getSelectedIndex();
       swap( index, index + 1 );
     }
     else if ( source == remove )
     {
       int index = macroButtons.getSelectedIndex();
       if ( canRemove( index ) )
       {  
         macroButtonModel.removeElementAt( index );
         int last = macroButtonModel.getSize() - 1;
         if ( index > last )
           index = last;
         macroButtons.setSelectedIndex( index );
       }
     }
     else if ( source == clear )
     {
       macroButtonModel.clear();
     }
     enableButtons();
   }
   
   private int getEntrySize( int index )
   {
     // Gets the size of the entry containing the index position in macroButtonModel
     while ( index > 0 && ( ( Number )macroButtonModel.elementAt( index - 1 ) ).intValue() != 0 )
     {
       index--;
     }
     int start = index;
     while ( index < macroButtonModel.getSize() && ( ( Number )macroButtonModel.elementAt( index ) ).intValue() != 0 )
     {
       index++;    
     }
     return index - start;   
   }
   
   private int  getEntryCount()
   {
     int count = 0;
     for ( int i = 0; i < macroButtonModel.getSize(); i++ )
     {
       if ( ( ( Number )macroButtonModel.elementAt( i ) ).intValue() == 0  || i == macroButtonModel.getSize() - 1 )
       {
         count++;
       }
     }
     return count;
   }
   
   /**
    * Adds the key.
    * 
    * @param mask
    *          the mask
    */
   private void addKey( int keyCode, int mask )
   {
 //    Integer value = new Integer( getSelectedKeyCode() | mask );
 //    keyCode |= mask;
     Integer value = new Integer( keyCode | mask );
     int size = macroButtonModel.getSize();
     if ( canAdd( size, value ) )
     {  
       macroButtonModel.addElement( value );
     }
   }
 
   /**
    * Insert key.
    * 
    * @param mask
    *          the mask
    */
   private void insertKey( int keyCode, int mask )
   {
 //    Integer value = new Integer( getSelectedKeyCode() | mask );
     keyCode |= mask;
     Integer value = new Integer( keyCode );
     int index = macroButtons.getSelectedIndex();
     int effectiveIndex = ( index == -1 ) ? 0 : index;
 
     if ( canAdd( effectiveIndex, value ) )
     {  
       macroButtonModel.add( effectiveIndex, value );
     }
     else
     {
       return;
     }  
 
     macroButtons.setSelectedIndex( index + 1 );
     macroButtons.ensureIndexIsVisible( index + 1 );
   }
   
   private boolean canAdd( int position, int key )
   {
     int size = macroButtonModel.getSize();
     int prevKey = ( position > 0 ) ? ( ( Number )macroButtonModel.elementAt( position - 1 ) ).intValue() : 0;
     boolean createsNewEntry = ( key == 0 || ( position == size && prevKey == 0 ) );
 
     if ( getEntrySize( position ) == favKey.getEntrySize() && !createsNewEntry )
     { 
       showWarning( "This would exceed the maximum number of keys without a Pause", 1 );
       return false;
     }
     if ( getEntryCount() == favKey.getMaxEntries() && createsNewEntry )
     {
       showWarning( "This would exceed the maximum number of entries", 1 );
       return false;
     }
     return true;
   }
   
   private boolean canRemove( int position )
   {
     int size = macroButtonModel.getSize();
     int key = ( ( Number )macroButtonModel.elementAt( position ) ).intValue();
     int nextKey = ( position < size - 1 ) ? ( ( Number )macroButtonModel.elementAt( position + 1 ) ).intValue() : 0;
     boolean combinesEntries = ( key == 0 && nextKey != 0 );
     
     if ( combinesEntries )
     {
       int combinedSize = getEntrySize( position ) + getEntrySize( position + 1 );
       if ( combinedSize > favKey.getEntrySize() )
       {
         showWarning( "This would create an entry exceeding maximum size", 1 );
         return false;
       }
     }
     return true;
   }
   
   private void swap( int index1, int index2 )
   {
     Object o1 = macroButtonModel.get( index1 );
     Object o2 = macroButtonModel.get( index2 );
     int value1 = ( ( Number )o1 ).intValue();
     int value2 = ( ( Number )o2 ).intValue();
     if ( value1 == 0 && value2 != 0 && ( ( index1 < index2 && !canAdd( index1, 1 ) )
         || ( index1 > index2 && !canAdd( index1 + 1, 1 ) ) ) )
     {
       return;
     }
     if ( value2 == 0 && value1 != 0 && ( ( index2 < index1 && !canAdd( index2, 1 ) )
         || ( index2 > index2 && !canAdd( index2 + 1, 1 ) ) ) )
     {
       return;
     }
     macroButtonModel.set( index1, o2 );
     macroButtonModel.set( index2, o1 );
     macroButtons.setSelectedIndex( index2 );
     macroButtons.ensureIndexIsVisible( index2 );
   }
   
   private int getSelectedKeyCode()
   {
     return ( ( Button )availableButtons.getSelectedValue() ).getKeyCode();
   }
   
   private void showWarning( String message, int type )
   {
     String title = ( type == 0 ) ? "Missing Information" : "Data Overflow";
     JOptionPane.showMessageDialog( this, message, title, JOptionPane.ERROR_MESSAGE );
   }
 
   @Override
   public void valueChanged( ListSelectionEvent e )
   {
     if ( e.getValueIsAdjusting() )
       return;
 
     enableButtons();
   }
 
   public static FavScan showDialog( Component locationComp, FavScan favScan, RemoteConfiguration config )
   {
     if ( dialog == null )
       dialog = new FavScanDialog( locationComp );
 
     dialog.setRemoteConfiguration( config );
     dialog.setFavKey( config.getRemote().getFavKey() );
     dialog.setFavScan( favScan );
     dialog.pack();
     dialog.setLocationRelativeTo( locationComp );
     dialog.setVisible( true );
     return dialog.favScan;
   }
 
   private void setFavScan( FavScan favScan )
   {
     this.favScan = null;
 
     availableButtons.setSelectedIndex( -1 );
     macroButtonModel.clear();
 
     if ( favScan == null )
     {
       deviceButton = config.getFavKeyDevButton();
       notes.setText( null );
       enableButtons();
       return;
     }
 
     deviceButton = favScan.getDeviceButton();
     
     short[] data = favScan.getData().getData();
     int entrySize = favKey.getEntrySize();
     for ( int i = 0; i < data.length; ++i )
     {
       if ( data[ i ] != 0 )
       {
         macroButtonModel.addElement( new Integer( data[ i ] ) );
       }
       if ( data[ i ] == 0 || ( ( i + 1 ) % entrySize ) == 0 )
       {
         while ( ( i < data.length ) &&  ( ( data[ i ] == 0 ) || ( i % entrySize ) != 0 ) )
         {  
           ++i;
         }
         --i;
         macroButtonModel.addElement( new Integer( 0 ) );
       }     
     }
 
     macroButtons.setSelectedIndex( -1 );
 
     notes.setText( favScan.getNotes() );
 
     enableButtons();
     
   }
 
   private void setRemoteConfiguration( RemoteConfiguration config )
   {
     if ( this.config == config )
       return;
 
     this.config = config;
     Remote remote = config.getRemote();
 
     java.util.List< Button > buttons = remote.getButtons();
     DefaultListModel model = new DefaultListModel();
     for ( Button b : buttons )
     {
      if ( b.canAssignToFav() || b.canAssignShiftedToFav() || b.canAssignXShiftedToFav() )
         model.addElement( b );
     }
     availableButtons.setModel( model );
 
     favScanButtonRenderer.setRemote( remote );
     
   }
 
   public void setFavKey( FavKey favKey )
   {
     this.favKey = favKey;
   }
 
   private void enableButtons()
   {
     Button b = ( Button )availableButtons.getSelectedValue(); 
     add.setEnabled( b != null && b.canAssignToFav() );
     insert.setEnabled( b != null && b.canAssignToFav() );
     addShift.setEnabled( b != null && b.canAssignShiftedToFav() );
     insertShift.setEnabled( b != null && b.canAssignShiftedToFav() );
     boolean xShiftEnabled = config.getRemote().getXShiftEnabled();
     addXShift.setEnabled( xShiftEnabled && b != null && b.canAssignXShiftedToFav() );
     insertXShift.setEnabled( xShiftEnabled && b != null && b.canAssignXShiftedToFav() );
 
     int selected = macroButtons.getSelectedIndex();
     moveUp.setEnabled( selected > 0 );
     moveDown.setEnabled( ( selected != -1 ) && ( selected < ( macroButtonModel.getSize() - 1 ) ) );
     remove.setEnabled( selected != -1 );
     clear.setEnabled( macroButtonModel.getSize() > 0 );
   }
   
   private static FavScanDialog dialog = null;
   
   /** The macro buttons. */
   private JList macroButtons = new JList();
 
   /** The move up. */
   private JButton moveUp = new JButton( "Move up" );
 
   /** The move down. */
   private JButton moveDown = new JButton( "Move down" );
 
   /** The remove. */
   private JButton remove = new JButton( "Remove" );
 
   /** The clear. */
   private JButton clear = new JButton( "Clear" );
 
   /** The ok button. */
   private JButton okButton = new JButton( "OK" );
 
   /** The cancel button. */
   private JButton cancelButton = new JButton( "Cancel" );
 
   
   /** The available buttons. */
   private JList availableButtons = new JList();
 
   /** The add. */
   private JButton add = new JButton( "Add" );
 
   /** The insert. */
   private JButton insert = new JButton( "Insert" );
 
   /** The add shift. */
   private JButton addShift = new JButton( "Add Shift" );
 
   /** The insert shift. */
   private JButton insertShift = new JButton( "Ins Shift" );
 
   /** The add x shift. */
   private JButton addXShift = new JButton( "Add xShift" );
 
   /** The insert x shift. */
   private JButton insertXShift = new JButton( "Ins xShift" );
   
   private JButton addPause = new JButton( "Add Pause" );
   
   private JButton insertPause = new JButton( "Ins Pause" );
   
   private FavScanButtonRenderer favScanButtonRenderer = new FavScanButtonRenderer();
   
   /** The macro button model. */
   private DefaultListModel macroButtonModel = new DefaultListModel();
   
   /** The notes. */
   private JTextArea notes = new JTextArea( 2, 10 );
   
   /** The config. */
   private RemoteConfiguration config = null;
   
   private FavKey favKey = null;
   
   private FavScan favScan;
   
   private DeviceButton deviceButton;
 
 
 }
