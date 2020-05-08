 package com.hifiremote.jp1;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.util.*;
 import javax.swing.*;
 import javax.swing.text.*;
 import javax.swing.event.*;
 import javax.swing.table.*;
 
 public class DeviceCombinerPanel
   extends KMPanel
   implements ListSelectionListener
 {
   public DeviceCombinerPanel( DeviceUpgrade devUpgrade )
   {
     super( "Device Combiner", devUpgrade );
     setToolTipText( "Combine multiple devices into a single upgrade" );
     setLayout( new BorderLayout());
 
     System.err.println( "DeviceCombinerPanel ctor: deviceCount = " + 
                         (( DeviceCombiner )deviceUpgrade.getProtocol()).getDevices().size());
     
     model = new AbstractTableModel()
     {
       public String getColumnName( int col )
       {
         return titles[ col ];
       }
       
       public Class getColumnClass( int col )
       {
         return classes[ col ];
       }
 
       public boolean isCellEditable( int row, int col )
       {
         if ( col == 4 )
           return true;
         return false;
       }
 
       public int getColumnCount(){ return titles.length; }
       public int getRowCount()
       {
         DeviceCombiner deviceCombiner = ( DeviceCombiner )deviceUpgrade.getProtocol();
         int rows = deviceCombiner.getDevices().size();
         System.err.println( "DeviceCombinerPanel.TableModel.getRowCount: rows=" + rows );
         return rows;
       }
 
       public Object getValueAt( int row, int col )
       {
         DeviceCombiner deviceCombiner = ( DeviceCombiner )deviceUpgrade.getProtocol();
         CombinerDevice device = ( CombinerDevice )deviceCombiner.getDevices().elementAt( row );
         if ( device == null )
           return null;
         if ( device.getProtocol() == null )
           return null;
         if ( col == 0 )
           return new Integer( row + 1 );
         else if ( col == 1 )
           return device.getProtocol().getName();
         else if ( col == 2 )
          return device.getProtocol().getID( deviceUpgrade.getRemote());
         else if ( col == 3 )
         {
           return device.getFixedData();
         }
         else if ( col == 4 )
           return device.getNotes();
         return null;
       }
 
       public void setValueAt( Object value, int row, int col )
       {
         if ( col == 4 )
         {
           DeviceCombiner deviceCombiner = ( DeviceCombiner )deviceUpgrade.getProtocol();
           CombinerDevice device = ( CombinerDevice )deviceCombiner.getDevices().elementAt( row );
           device.setNotes(( String )value );
         }
       }
     };
     table = new JTableX( model );
 //    add( table.getTableHeader(), BorderLayout.NORTH );
     table.getSelectionModel().addListSelectionListener( this );
     DefaultCellEditor e = ( DefaultCellEditor )table.getDefaultEditor( String.class );
     new TextPopupMenu(( JTextComponent )e.getComponent());
     table.addMouseListener( new MouseAdapter()
     {
       public void mouseClicked( MouseEvent e )
       {
         if ( e.getClickCount() < 2 )
           e.consume();
         else
           editDevice();
       }
     });
 
     add( new JScrollPane( table ), BorderLayout.CENTER );
 
     ActionListener al = new ActionListener()
     {
       public void actionPerformed( ActionEvent e )
       {
         Object source = e.getSource();
         if ( source == addButton )
         {
           CombinerDeviceDialog d = 
             new CombinerDeviceDialog( RemoteMaster.getFrame(),
                                       null, 
                                       deviceUpgrade.getRemote());
           d.setVisible( true );
           if ( d.getUserAction() == JOptionPane.OK_OPTION )
           {
             DeviceCombiner combiner = ( DeviceCombiner )deviceUpgrade.getProtocol();
             Vector< CombinerDevice > devices = combiner.getDevices();
             int newRow = devices.size();
             CombinerDevice device = d.getCombinerDevice();
             devices.add( device );
             model.fireTableRowsInserted( newRow, newRow );
           }
         }
         else if ( source == importButton )
         {
           File file = KeyMapMaster.promptForUpgradeFile( null );
           if ( file == null )
             return;
           DeviceUpgrade importedUpgrade = new DeviceUpgrade();
           try
           {
             importedUpgrade.load( file, false );
             Remote remote = deviceUpgrade.getRemote();
             importedUpgrade.setRemote( remote );
 
             Protocol importedProtocol = importedUpgrade.getProtocol();
             
             if ( !remote.getProcessor().getName().equals( "S3C80" ))
             {
               if ( remote.supportsVariant( importedProtocol.getID(), importedProtocol.getVariantName()))
               {
                 JOptionPane.showMessageDialog( null,
                                                "Device Combiner can only combine protocol that are built into the remote. " +
                                                "The device upgrade you tried to import uses the '" +
                                                importedProtocol.getName() + "' protocol, which is not built into the " +
                                                remote.getName() + " remote.",
                                                "Incompatible Upgrade",
                                                JOptionPane.ERROR_MESSAGE );
                 return;
               }
             }
             if ( importedProtocol.getDefaultCmd().length() > 1 )
             {
               JOptionPane.showMessageDialog( null,
                                              "Device Combiner can only combine protocol that use 1-byte commands.  " +
                                              "The device upgrade you tried to import uses the '" +
                                              importedProtocol.getName() + "' protocol, which uses " +
                                              importedProtocol.getDefaultCmd().length() + "-byte commands.",
                                              "Incompatible Upgrade",
                                              JOptionPane.ERROR_MESSAGE );
               return;
             }
 
             FunctionImportDialog d = new FunctionImportDialog( null, importedUpgrade );
             d.setVisible( true );
             if ( d.getUserAction() == JOptionPane.OK_OPTION )
             {
               CombinerDevice device = new CombinerDevice( importedProtocol, importedUpgrade.getParmValues());
               DeviceCombiner combiner = ( DeviceCombiner )deviceUpgrade.getProtocol();
               Vector< CombinerDevice > devices = combiner.getDevices();
               int index = devices.size();
               Integer indexInt = new Integer( index );
               devices.add( device );
               
               Vector< Function > importedFunctions = d.getSelectedFunctions();
               if ( importedFunctions.size() > 0 )
               {
                 Vector< Function > functions = deviceUpgrade.getFunctions();
                 int firstRow =  functions.size();
                 for ( Enumeration en = importedFunctions.elements(); en.hasMoreElements(); )
                 {
                   Function f = ( Function )en.nextElement();
                   Function newF = new Function();
                   Hex hex = combiner.getDefaultCmd();
                   combiner.setValueAt( 0, hex, indexInt );
                   short efc = EFC.parseHex( f.getHex(), importedProtocol.getCmdIndex());
                   EFC.toHex( efc, hex, combiner.getCmdIndex());
                   newF.setHex( hex );
                   newF.setName( f.getName());
                   newF.setNotes( f.getNotes());
                   functions.add( newF );
                 }
               }
               model.fireTableRowsInserted( index, index );
             }
           }
           catch ( Exception ex )
           {
             ex.printStackTrace( System.err );
             JOptionPane.showMessageDialog( null,
                                            "An error occurred loading the device upgrade from " +
                                            file.getName() + ".  Please see rmaster.err for more details.",
                                            "Device Upgrade Load Error",
                                            JOptionPane.ERROR_MESSAGE );
           }
         }
         else if ( source == editButton )
         {
           editDevice();
         }
         else if ( source == removeButton )
         {
           int row = table.getSelectedRow();
           DeviceCombiner combiner = ( DeviceCombiner )deviceUpgrade.getProtocol();
           Vector devices = combiner.getDevices();
           Vector functions = deviceUpgrade.getFunctions();
           for ( Enumeration en = functions.elements(); en.hasMoreElements(); )
           {
             Function f = ( Function )en.nextElement();
             Hex hex = f.getHex();
             if ( hex == null )
               continue;
             int i = (( Choice )combiner.getValueAt( 0, hex )).getIndex();
             if ( i > row )
             {
               --i;
               if ( i < 0 ) i = 0;
               combiner.setValueAt( 0, hex, new Integer( i )); 
             }
           }
           devices.removeElementAt( row );
           model.fireTableRowsDeleted( row, row );
         }
         update();
       }
     };
 
     JPanel panel = new JPanel();
 
     addButton = new JButton( "Add" );
     addButton.addActionListener( al );
     panel.add( addButton );
 
     importButton = new JButton( "Import" );
     importButton.addActionListener( al );
     panel.add( importButton );
 
     editButton = new JButton( "Edit" );
     editButton.addActionListener( al );
     panel.add( editButton );
 
     removeButton = new JButton( "Remove" );
     removeButton.addActionListener( al );
     panel.add( removeButton );
     
     add( panel, BorderLayout.SOUTH );
     initColumns( table );
   }
 
   private void editDevice()
   {
     DeviceCombiner combiner = ( DeviceCombiner )deviceUpgrade.getProtocol();
     Vector< CombinerDevice > devices = combiner.getDevices();
     int row = table.getSelectedRow();
     CombinerDevice device = ( CombinerDevice )devices.elementAt( row );
     CombinerDeviceDialog d = 
       new CombinerDeviceDialog( RemoteMaster.getFrame(),
                                 device, 
                                 deviceUpgrade.getRemote());
     d.setVisible( true );
     if ( d.getUserAction() == JOptionPane.OK_OPTION )
     {
       devices.setElementAt( d.getCombinerDevice(), row );
       model.fireTableRowsUpdated( row, row );
     }
   }
 
   protected void setColumnWidth( JTable table, int col, String text )
   {
     JLabel l = ( JLabel )
       table.getTableHeader().getDefaultRenderer().getTableCellRendererComponent( table, text, false, false, 0, col );
     int width = l.getPreferredSize().width + 2;
     TableColumn column = table.getColumnModel().getColumn( col );
     column.setMinWidth( width / 2 );
     column.setPreferredWidth( width );
     column.setMaxWidth(( width * 3 ) / 2 );
   }
   
   protected void initColumns( JTable table )
   {
     setColumnWidth( table, 0, "16" );
     setColumnWidth( table, 2, "FF FF" );
     table.doLayout();
   }
 
   public void update()
   {
     DeviceCombiner combiner = ( DeviceCombiner )deviceUpgrade.getProtocol();
     boolean flag = combiner.getDevices().size() < 16;
     addButton.setEnabled( flag );
     importButton.setEnabled( flag );
     int row = table.getSelectedRow();
     flag = row != -1;
     editButton.setEnabled( flag );
     if ( flag )
     {
       for ( Function f : deviceUpgrade.getFunctions())
       {
         if ( f.getHex() == null )
           continue;
         int temp = 0;
         Object val = combiner.getValueAt( 0, f.getHex());
         if ( val instanceof Choice )
           temp = (( Choice )val ).getIndex();
         else if ( val instanceof Number )
           temp = (( Number )val ).intValue();
         if ( temp == row )
         {
           flag = false;
           break;
         }
       }
     }
     removeButton.setEnabled( flag );
   }
 
   // Interface ListSelectionListener
   public void valueChanged( ListSelectionEvent e )
   {
     if ( !e.getValueIsAdjusting() )
     {
       int row = table.getSelectedRow();
       boolean flag = ( row != -1 );
       editButton.setEnabled( flag );
       if ( flag )
       {
         DeviceCombiner combiner = ( DeviceCombiner )deviceUpgrade.getProtocol();
         Vector functions = deviceUpgrade.getFunctions();
         for ( Enumeration en = functions.elements(); en.hasMoreElements(); )
         {
           Function f = ( Function )en.nextElement();
           if ( f.getHex() == null )
             continue;
           int temp = (( Choice )combiner.getValueAt( 0, f.getHex())).getIndex();
           if ( temp == row )
           {
             flag = false;
             break;
           }
         }
       }
       removeButton.setEnabled( flag );
     }
   }
 
   private static String[] titles = { "#",           "Protocol",   "PID",      "Fixed Data", "Notes" };
   private static Class[] classes = { Integer.class, String.class, Hex.class,  Hex.class,    String.class };
 
   private AbstractTableModel model = null;
   private JTableX table = null;
   private JButton addButton = null;
   private JButton importButton = null;
   private JButton editButton = null;
   private JButton removeButton = null;
 }
