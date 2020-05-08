 package com.hifiremote.jp1;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.StringSelection;
 import java.awt.datatransfer.Transferable;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.beans.PropertyChangeListener;
 import java.io.BufferedReader;
 import java.io.StringReader;
 import java.util.StringTokenizer;
 
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.TransferHandler;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableCellEditor;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class RMTablePanel.
  */
 public abstract class RMTablePanel< E > extends RMPanel implements ActionListener, ListSelectionListener
 {
 
   /**
    * Instantiates a new rM table panel.
    * 
    * @param model
    *          the model
    */
   public RMTablePanel( JP1TableModel< E > model )
   {
     this( model, BorderLayout.CENTER );
   }
 
   /**
    * Instantiates a new rM table panel.
    * 
    * @param tableModel
    *          the table model
    * @param location
    *          the location
    */
   public RMTablePanel( JP1TableModel< E > tableModel, String location )
   {
     super();
     model = tableModel;
     sorter = new TableSorter( model );
     table = new JP1Table( sorter );
     sorter.setTableHeader( table.getTableHeader() );
     // sorter.addMouseListenerToHeaderInTable( table );
     table.getSelectionModel().addListSelectionListener( this );
     table.getTableHeader().setToolTipText(
         "Click to sort in ascending order, or shift-click to sort in descending order." );
     table.addMouseListener( new MouseAdapter()
     {
       public void mouseClicked( MouseEvent e )
       {
         if ( e.getClickCount() != 2 )
           return;
         int row = table.getSelectedRow();
         if ( row == -1 )
           return;
         if ( !table.isCellEditable( row, table.columnAtPoint( e.getPoint() ) ) )
           editRowObject( row );
       }
     } );
     TransferHandler th = new TransferHandler()
     {
       protected Transferable createTransferable( JComponent c )
       {
         return new LocalObjectTransferable( new Integer( table.getSelectedRow() ) );
       }
 
       public int getSourceActions( JComponent c )
       {
         return TransferHandler.COPY_OR_MOVE;
       }
 
       public boolean canImport( JComponent comp, DataFlavor[] flavors )
       {
         boolean rc = false;
         for ( int i = 0; i < flavors.length; i++ )
         {
           if ( ( flavors[ i ] == DataFlavor.stringFlavor ) || ( flavors[ i ] == LocalObjectTransferable.getFlavor() ) )
           {
             rc = true;
             break;
           }
         }
         return rc;
       }
 
       public boolean importData( JComponent c, Transferable t )
       {
         boolean rc = false;
         if ( t.isDataFlavorSupported( DataFlavor.stringFlavor ) )
         {
           try
           {
             String s = ( String )( t.getTransferData( DataFlavor.stringFlavor ) );
             BufferedReader in = new BufferedReader( new StringReader( s ) );
             int colCount = table.getModel().getColumnCount();
             int addedRow = -1;
             int row = table.getSelectedRow();
             int col = table.getSelectedColumn();
             for ( String line = in.readLine(); line != null; line = in.readLine() )
             {
               if ( row == model.getRowCount() )
               {
                 model.addRow( createRowObject( null ) );
                 if ( addedRow == -1 )
                   addedRow = row;
               }
 
               StringTokenizer st = new StringTokenizer( line, "\t", true );
               int workCol = col;
               boolean done = false;
               String token = null;
               String prevToken = null;
               while ( !done )
               {
                 if ( workCol == colCount )
                   break;
                 if ( st.hasMoreTokens() )
                   token = st.nextToken();
                 else
                   token = null;
 
                 Object value = null;
                 int modelCol = table.convertColumnIndexToModel( workCol );
                 if ( token == null )
                 {
                   done = true;
                   if ( prevToken != null )
                     break;
                 }
                 else if ( token.equals( "\t" ) )
                 {
                   if ( prevToken == null )
                     token = null;
                   else
                   {
                     prevToken = null;
                     continue;
                   }
                 }
                 prevToken = token;
 
                 Class< ? > aClass = sorter.getColumnClass( modelCol );
                 if ( aClass == String.class )
                 {
                   if ( ( token != null ) && ( token.length() == 5 ) && token.startsWith( "num " )
                       && Character.isDigit( token.charAt( 4 ) ) )
                     value = token.substring( 4 );
                   else
                     value = token;
                 }
                 else
                   value = token;
 
                 sorter.setValueAt( value, row, modelCol );
                 workCol++ ;
               }
               row++ ;
             }
             if ( addedRow != -1 )
               sorter.fireTableRowsInserted( addedRow, row - 1 );
             sorter.fireTableRowsUpdated( popupRow, row - 1 );
           }
           catch ( Exception ex )
           {
             String message = ex.getMessage();
             if ( message == null )
               message = ex.toString();
             JP1Frame.showMessage( message, table );
             ex.printStackTrace( System.err );
           }
         }
         else if ( t.isDataFlavorSupported( LocalObjectTransferable.getFlavor() ) )
         {
           try
           {
             int dragRow = ( ( Integer )t.getTransferData( LocalObjectTransferable.getFlavor() ) ).intValue();
             int dropRow = table.getSelectedRow();
             if ( dropRow != dragRow )
             {
               dragRow = sorter.modelIndex( dragRow );
               dropRow = sorter.modelIndex( dropRow );
               model.moveRow( dragRow, dropRow );
 
               rc = true;
             }
           }
           catch ( Exception e )
           {
             e.printStackTrace( System.err );
           }
         }
         return rc;
       }
 
       public void exportToClipboard( JComponent comp, Clipboard clipboard, int action )
       {
         JTable table = ( JTable )comp;
         int[] selectedRows = table.getSelectedRows();
         int[] selectedCols = table.getSelectedColumns();
         StringBuilder buff = new StringBuilder( 200 );
         for ( int rowNum = 0; rowNum < selectedRows.length; rowNum++ )
         {
           if ( rowNum != 0 )
             buff.append( "\n" );
           for ( int colNum = 0; colNum < selectedCols.length; colNum++ )
           {
             if ( colNum != 0 )
               buff.append( "\t" );
             int selRow = selectedRows[ rowNum ];
             // int convertedRow = sorter.convertRowIndexToModel( selRow );
             int selCol = selectedCols[ colNum ];
             int convertedCol = table.convertColumnIndexToModel( selCol );
             Object value = table.getValueAt( selRow, selCol );
             if ( value != null )
             {
               DefaultTableCellRenderer cellRenderer = ( DefaultTableCellRenderer )table.getColumnModel().getColumn(
                   selCol ).getCellRenderer();
               if ( cellRenderer != null )
               {
                 cellRenderer.getTableCellRendererComponent( table, value, false, false, selRow, convertedCol );
                 value = cellRenderer.getText();
               }
               buff.append( value.toString() );
             }
           }
         }
         StringSelection data = new StringSelection( buff.toString() );
         clipboard.setContents( data, data );
       }
     };
     table.setTransferHandler( th );
 
     popup = new JPopupMenu();
     editItem = new JMenuItem( "Edit" );
     editItem.setEnabled( false );
     editItem.addActionListener( this );
     popup.add( editItem );
 
     newItem = new JMenuItem( "New" );
     newItem.addActionListener( this );
     newItem.setEnabled( false );
     popup.add( newItem );
 
     cloneItem = new JMenuItem( "Clone" );
     cloneItem.addActionListener( this );
     cloneItem.setEnabled( false );
     popup.add( cloneItem );
 
     deleteItem = new JMenuItem( "Delete" );
     deleteItem.addActionListener( this );
     deleteItem.setEnabled( false );
     popup.add( deleteItem );
 
     MouseAdapter mh = new MouseAdapter()
     {
       public void mousePressed( MouseEvent e )
       {
         showPopup( e );
       }
 
       public void mouseReleased( MouseEvent e )
       {
         showPopup( e );
       }
 
       private boolean showPopup( MouseEvent e )
       {
         if ( e.isPopupTrigger() )
         {
           finishEditing();
           popupRow = table.rowAtPoint( e.getPoint() );
           popup.show( table, e.getX(), e.getY() );
           return true;
         }
         else
           return false;
       }
     };
     table.addMouseListener( mh );
 
     MouseMotionAdapter mmh = new MouseMotionAdapter()
     {
       public void mouseDragged( MouseEvent e )
       {
         int tableCol = table.columnAtPoint( e.getPoint() );
         int modelCol = table.convertColumnIndexToModel( tableCol );
         if ( modelCol == 0 )
           table.getTransferHandler().exportAsDrag( table, e, TransferHandler.MOVE );
       }
     };
     table.addMouseMotionListener( mmh );
     table.initColumns( model );
     JScrollPane scrollPane = new JScrollPane( table );
     Dimension d = table.getPreferredScrollableViewportSize();
     d.width = table.getPreferredSize().width;
     table.setPreferredScrollableViewportSize( d );
     add( scrollPane, location );
    
    footerPanel = new JPanel( new BorderLayout() );
    add ( footerPanel, BorderLayout.PAGE_END );
     buttonPanel = new JPanel();
    footerPanel.add( buttonPanel, BorderLayout.PAGE_END );
 
     editButton = new JButton( "Edit" );
     editButton.addActionListener( this );
     editButton.setToolTipText( "Edit the selected item." );
     editButton.setEnabled( false );
     buttonPanel.add( editButton );
 
     newButton = new JButton( "New" );
     newButton.addActionListener( this );
     newButton.setToolTipText( "Add a new item." );
     buttonPanel.add( newButton );
 
     cloneButton = new JButton( "Clone" );
     cloneButton.addActionListener( this );
     cloneButton.setToolTipText( "Add a copy of the selected item." );
     cloneButton.setEnabled( false );
     buttonPanel.add( cloneButton );
 
     deleteButton = new JButton( "Delete" );
     deleteButton.addActionListener( this );
     deleteButton.setToolTipText( "Delete the selected item." );
     deleteButton.setEnabled( false );
     buttonPanel.add( deleteButton );
 
     upButton = new JButton( "Up" );
     upButton.addActionListener( this );
     upButton.setToolTipText( "Move the selected item up in the list." );
     upButton.setEnabled( false );
     buttonPanel.add( upButton );
 
     downButton = new JButton( "Down" );
     downButton.addActionListener( this );
     downButton.setToolTipText( "Move the selected item down in the list." );
     downButton.setEnabled( false );
     buttonPanel.add( downButton );
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see javax.swing.JComponent#setFont(java.awt.Font)
    */
   public void setFont( Font aFont )
   {
     super.setFont( aFont );
     if ( ( table == null ) || ( aFont == null ) )
       return;
     table.setRowHeight( aFont.getSize() + 2 );
     // if ( model != null )
     table.initColumns( model );
   }
 
   /**
    * Finish editing.
    */
   private void finishEditing()
   {
     int editRow = table.getEditingRow();
     if ( editRow != -1 )
     {
       TableCellEditor editor = table.getCellEditor( editRow, table.getEditingColumn() );
       if ( !editor.stopCellEditing() )
         editor.cancelCellEditing();
     }
   }
 
   /**
    * Creates the row object.
    * 
    * @param baseObject
    *          the base object
    * @return the e
    */
   protected abstract E createRowObject( E baseObject );
 
   /**
    * Gets the row object.
    * 
    * @param row
    *          the row
    * @return the row object
    */
   protected E getRowObject( int row )
   {
     if ( row != -1 )
       return model.getRow( sorter.modelIndex( row ) );
     return null;
   }
 
   /**
    * Can delete.
    * 
    * @param o
    *          the o
    * @return true, if successful
    */
   protected boolean canDelete( Object o )
   {
     return true;
   }
 
   /**
    * Do not delete.
    * 
    * @param o
    *          the o
    */
   protected void doNotDelete( Object o )
   {}
 
   /**
    * Edits the row object.
    * 
    * @param row
    *          the row
    */
   protected void editRowObject( int row )
   {
     E o = createRowObject( getRowObject( row ) );
     if ( o != null )
       model.setRow( sorter.modelIndex( row ), o );
   }
 
   // Interface ActionListener
   /*
    * (non-Javadoc)
    * 
    * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    */
   public void actionPerformed( ActionEvent e )
   {
     finishEditing();
     int row = 0;
     boolean select = false;
     Object source = e.getSource();
     if ( source.getClass() == JButton.class )
     {
       row = table.getSelectedRow();
       if ( row != -1 )
         select = true;
     }
     else
     {
       row = popupRow;
       if ( table.isRowSelected( row ) )
         select = true;
     }
     int modelRow = -1;
     if ( row != -1 )
       modelRow = sorter.modelIndex( row );
 
     if ( ( source == editButton ) || ( source == editItem ) )
     {
       editRowObject( row );
     }
     if ( ( source == newButton ) || ( source == newItem ) )
     {
       E o = createRowObject( null );
       if ( o == null )
         return;
       if ( row == -1 )
       {
         model.addRow( o );
         row = model.getRowCount();
       }
       else
       {
         model.insertRow( modelRow, o );
       }
 
       if ( select )
         table.setRowSelectionInterval( row, row );
     }
     if ( ( source == cloneButton ) || ( source == cloneItem ) )
     {
       E o = createRowObject( getRowObject( row ) );
       if ( o == null )
         return;
       if ( row == -1 )
       {
         model.addRow( o );
         row = model.getRowCount();
         modelRow = row;
       }
       else
       {
         model.insertRow( modelRow, o );
       }
 
       if ( select )
         table.setRowSelectionInterval( row, row );
     }
     else if ( ( source == deleteButton ) || ( source == deleteItem ) )
     {
       if ( !canDelete( model.getRow( sorter.modelIndex( row ) ) ) )
       {
         deleteButton.setEnabled( false );
         deleteItem.setEnabled( false );
         doNotDelete( model.getRow( modelRow ) );
       }
       else
       {
         int rowToSelect = row;
         if ( rowToSelect == ( sorter.getRowCount() - 1 ) )
           --rowToSelect;
         else
           ++rowToSelect;
         if ( select && ( rowToSelect > -1 ) )
           table.setRowSelectionInterval( rowToSelect, rowToSelect );
 
         model.removeRow( modelRow );
       }
     }
     else if ( ( source == upButton ) || ( source == downButton ) )
     {
       int sel = 0;
       int from = row;
       int to;
 
       if ( source == upButton )
       {
         from = row - 1;
         to = row;
         sel = from;
       }
       else
       // down button
       {
         from = row;
         to = row + 1;
         sel = to;
       }
       model.moveRow( sorter.modelIndex( from ), sorter.modelIndex( to ) );
       if ( select )
         table.setRowSelectionInterval( sel, sel );
     }
   }
 
   // Interface ListSelectionListener
   /*
    * (non-Javadoc)
    * 
    * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
    */
   public void valueChanged( ListSelectionEvent e )
   {
     if ( !e.getValueIsAdjusting() )
     {
       int row = table.getSelectedRow();
 
       boolean selected = ( row != -1 );
       editButton.setEnabled( selected );
       editItem.setEnabled( selected );
       cloneButton.setEnabled( selected );
       cloneItem.setEnabled( selected );
       deleteButton.setEnabled( selected );
       deleteItem.setEnabled( selected );
 
       boolean deleteAllowed = selected && canDelete( model.getRow( sorter.modelIndex( row ) ) );
       deleteButton.setEnabled( deleteAllowed );
       deleteItem.setEnabled( deleteAllowed );
 
       upButton.setEnabled( row > 0 );
       downButton.setEnabled( selected && ( row < ( sorter.getRowCount() - 1 ) ) );
     }
   }
 
   /**
    * Commit.
    */
   public void commit()
   {
     finishEditing();
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.hifiremote.jp1.RMPanel#addPropertyChangeListener(java.beans.PropertyChangeListener)
    */
   public void addPropertyChangeListener( PropertyChangeListener listener )
   {
     if ( ( model != null ) && ( listener != null ) )
       model.addPropertyChangeListener( listener );
   }
 
   /**
    * Gets the model.
    * 
    * @return the model
    */
   public JP1TableModel< E > getModel()
   {
     return model;
   }
 
   /** The table. */
   protected JP1Table table = null;
 
   /** The model. */
   protected JP1TableModel< E > model = null;
 
   /** The sorter. */
   protected TableSorter sorter = null;
 
   /** The button panel. */
   protected JPanel buttonPanel = null;
  
  protected JPanel footerPanel = null;
 
   /** The edit button. */
   private JButton editButton = null;
 
   /** The new button. */
   protected JButton newButton = null;
 
   /** The clone button. */
   protected JButton cloneButton = null;
 
   /** The delete button. */
   private JButton deleteButton = null;
 
   /** The up button. */
   private JButton upButton = null;
 
   /** The down button. */
   private JButton downButton = null;
 
   /** The popup row. */
   private int popupRow = 0;
 
   /** The popup. */
   protected JPopupMenu popup = null;
 
   /** The edit item. */
   private JMenuItem editItem = null;
 
   /** The new item. */
   private JMenuItem newItem = null;
 
   /** The clone item. */
   protected JMenuItem cloneItem = null;
 
   /** The delete item. */
   private JMenuItem deleteItem = null;
 }
