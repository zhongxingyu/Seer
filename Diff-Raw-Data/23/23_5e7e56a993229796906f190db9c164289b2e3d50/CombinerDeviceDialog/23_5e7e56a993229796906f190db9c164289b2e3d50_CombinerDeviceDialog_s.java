 package com.hifiremote.jp1;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.text.*;
 import java.util.*;
 import javax.swing.text.*;
 import javax.swing.*;
 import javax.swing.border.*;
 import javax.swing.event.*;
 import info.clearthought.layout.*;
 
 public class CombinerDeviceDialog
   extends JDialog
   implements ActionListener, ItemListener, DocumentListener, FocusListener, Runnable
 {
   public CombinerDeviceDialog( JFrame owner, CombinerDevice dev, Remote r )
   {
     super( owner, "Combiner Device", true );
     createGui( owner, dev, r );
   }
     
   public CombinerDeviceDialog( JDialog owner, CombinerDevice dev, Remote r )
   {
     super( owner, "Combiner Device", true );
     createGui( owner, dev, r );
   }
     
   private void createGui( Component owner, CombinerDevice dev, Remote r )
   {
     setLocationRelativeTo( owner );
     Container contentPane = getContentPane();
 
     protocolHolder = new JPanel( new BorderLayout());
     protocolHolder.setBorder( BorderFactory.createTitledBorder( "Protocol Parameters" ));
 
     Insets insets = protocolHolder.getInsets();
     double bt = insets.top;
     double bl = insets.left + 10;
     double br = insets.right;
     double bb = insets.bottom;
     double b = 10;       // space around border
     double i = 5;        // space between rows
     double v = 20;       // space between groupings
     double c = 10;       // space between columns
     double f = TableLayout.FILL;
     double p = TableLayout.PREFERRED;
     double size[][] =
     {// 0  1   2  3  4  5   6  7   8  9  10 11
       { b, bl, p, b, p, br, b, f, b },             // cols
       { b, p,  i, p, v, bt, p, bb, i, f, b }    // rows
     };
     tl = new TableLayout( size );
     mainPanel = new JPanel( tl );
     contentPane.add( mainPanel, BorderLayout.CENTER );
 
     JLabel label = new JLabel( "Protocol:", SwingConstants.RIGHT );
     mainPanel.add( label, "2, 1" );
 
     boolean allowUpgrades =  r.getProcessor().getFullName().equals( "S3C80" );
     Vector allProtocols = 
       ProtocolManager.getProtocolManager().getProtocolsForRemote( r, allowUpgrades );
     Vector< Protocol > protocols = new Vector< Protocol >();
     if ( allowUpgrades )
       protocols.add( new ManualProtocol( null, null ));
     for ( Enumeration e = allProtocols.elements(); e.hasMoreElements(); )
     {
       Protocol protocol = ( Protocol )e.nextElement();
       if ( protocol.getDefaultCmd().length() == 1 )
       {
         protocols.add( protocol );
       }
     }
     if ( dev == null )
       device = new CombinerDevice(( Protocol )protocols.elementAt( 1 ), new Value[ 0 ]);
     else
       device = new CombinerDevice( dev );
 
     device.getProtocol().reset();
 
     protocolList = new JComboBox( protocols );
     protocolList.addActionListener( this );
     label.setLabelFor( protocolList );
     protocolList.setToolTipText( "Select the protocol to be used for this device upgrade from the drop-down list." );
     mainPanel.add( protocolList, "4, 1" );
 
     deviceNotes = new JTextField();
     new TextPopupMenu( deviceNotes );
     deviceNotes.getDocument().addDocumentListener( this );
     JPanel temp = new JPanel( new BorderLayout());
     temp.add( deviceNotes, BorderLayout.CENTER );
     temp.setBorder( BorderFactory.createTitledBorder( "Notes" ));
 
     mainPanel.add( temp, "7, 1, 7, 3" );
 
     label = new JLabel( "Protocol ID:", SwingConstants.RIGHT );
     mainPanel.add( label, "2, 3" );
 
     protocolID = new JTextField();
     label.setLabelFor( protocolID );
     protocolID.setEditable( false );
     protocolID.setToolTipText( "This is the protocol ID that corresponds to the selected protocol." );
     mainPanel.add( protocolID, "4, 3" );
 
     label = new JLabel( "Fixed Data:", SwingConstants.RIGHT );
     mainPanel.add( label, "2, 6" );
 
     fixedData = new JTextField( " " );
     fixedData.setEditable( false );
     mainPanel.add( fixedData, "4, 6" );
 
     boolean flag = ( device.getProtocol().getClass() == ManualProtocol.class );
     fixedData.setEditable( flag );
     protocolID.setEditable( flag );
 
     mainPanel.add( protocolHolder, "1, 5, 5, 7" );
 
     protocolNotes = new JTextArea( 15, 60 );
     protocolNotes.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
     protocolNotes.setText((( Protocol )protocols.elementAt( 0 )).getNotes()); 
     protocolNotes.setBackground( label.getBackground());
     protocolNotes.setToolTipText( "Notes about the selected protocol." );
     protocolNotes.setEditable( false );
     protocolNotes.setLineWrap( true );
     protocolNotes.setWrapStyleWord( true );
     JScrollPane scrollPane = new JScrollPane( protocolNotes );
     scrollPane.setBorder( 
       BorderFactory.createCompoundBorder( 
         BorderFactory.createTitledBorder( "Protocol Notes" ),
         scrollPane.getBorder()));
     mainPanel.add( scrollPane, "1, 9, 7, 9" );
 
     JPanel panel = new JPanel( new FlowLayout( FlowLayout.RIGHT ));
     contentPane.add( panel, BorderLayout.SOUTH ); 
 
     okButton = new JButton( "OK" );
     okButton.addActionListener( this );
     panel.add( okButton );
 
     cancelButton = new JButton( "Cancel" );
     cancelButton.addActionListener( this );
     panel.add( cancelButton );
 
     update();
     pack();
 
     Rectangle rect = getBounds();
     int x = rect.x - rect.width / 2;
     int y = rect.y - rect.height / 2;
     setLocation( x, y );
   }
 
   public void update()
   {
     Protocol p = device.getProtocol();
     p.setDeviceParms( device.getValues());
     updateParameters();
     protocolList.setSelectedItem( p );
     if ( protocolList.getSelectedItem() != p )
     {
       protocolList.addItem( p );
       protocolList.setSelectedItem( p );
 
     }
     fixedData.getDocument().removeDocumentListener( this );
     protocolID.getDocument().removeDocumentListener( this );
     deviceNotes.getDocument().removeDocumentListener( this );
 
     fixedData.setText( device.getFixedData().toString());
    Hex id = p.getID();
     if ( id != null )
       protocolID.setText( id.toString());
     else
       protocolID.setText( null );
     deviceNotes.setText( device.getNotes());
 
     fixedData.getDocument().addDocumentListener( this );
     protocolID.getDocument().addDocumentListener( this );
     deviceNotes.getDocument().addDocumentListener( this );
     
     protocolNotes.setText( p.getNotes());
     protocolNotes.setCaretPosition( 0 );
 
 //    boolean allowUpgrades =  r.getProcessor().getFullName().equals( "S3C80" );
 
   }
 
   public void updateParameters()
   {
     DeviceParameter[] newParameters = device.getProtocol().getDeviceParameters();
     if ( parameters != newParameters )
     {
       removeParameters( parameters );
       parameters = newParameters;
       addParameters( parameters );
     }
   }
 
   private void removeParameters( DeviceParameter[] parameters )
   {
     if ( parameters != null )
     {
       for ( int i = 0; i < parameters.length; i++ )
       {
         parameters[ i ].removeListener( this );
         mainPanel.remove( parameters[ i ].getLabel());
         mainPanel.remove( parameters[ i ].getComponent());
         tl.deleteRow( 6 );
         tl.deleteRow( 6 );
       }
     }
   }
 
   private void addParameters( DeviceParameter[] parameters )
   {
     if ( parameters != null )
     {
       int row = 6;
       for ( int i = 0; i < parameters.length; i++ )
       {
         parameters[ i ].addListener( this );
         tl.insertRow( row, TableLayout.PREFERRED );
         mainPanel.add( parameters[ i ].getLabel(), "2, " + row );
         mainPanel.add( parameters[ i ].getComponent() , "4, " + row );
         row++;
         tl.insertRow( row++, 5 );
       }
       TableLayoutConstraints tlc = tl.getConstraints( protocolHolder );
       mainPanel.remove( protocolHolder );
       mainPanel.add( protocolHolder, tlc );
     }
   }
 
 
   public void updateFixedData()
   {
     Protocol p = device.getProtocol();
     device.setValues( p.getDeviceParmValues());
     fixedData.getDocument().removeDocumentListener( this );
     fixedData.setText( device.getFixedData().toString());
     fixedData.getDocument().addDocumentListener( this );
   }
 
   // ActionListener Methods
   public void actionPerformed( ActionEvent e )
   {
     Object source = e.getSource();
 
     if ( source == protocolList )
     {
       Protocol newProtocol = ( Protocol )protocolList.getSelectedItem();
       Protocol oldProtocol = device.getProtocol();
       if ( newProtocol != oldProtocol )
       {
         oldProtocol.reset();
         protocolID.getDocument().removeDocumentListener( this );
        Hex id = newProtocol.getID();
         if ( id != null )
           protocolID.setText( id.toString());
         else
           protocolID.setText( null );
         protocolID.getDocument().addDocumentListener( this );
         device.setProtocol( newProtocol );
         updateParameters();
         fixedData.getDocument().removeDocumentListener( this );
         fixedData.setText( newProtocol.getFixedData( newProtocol.getDeviceParmValues()).toString());
         fixedData.getDocument().addDocumentListener( this );
         boolean flag = ( newProtocol.getClass() == ManualProtocol.class );
         fixedData.setEditable( flag );
         protocolID.setEditable( flag );
         validate();
         protocolNotes.setText( newProtocol.getNotes());
         protocolNotes.setCaretPosition( 0 );
         protocolNotes.revalidate();
       }
     }
     if ( source == cancelButton )
     {
       userAction = JOptionPane.CANCEL_OPTION;
       setVisible( false );
       removeParameters( parameters );
       device.getProtocol().reset();
       dispose();
     }
     else if ( source == okButton )
     {
       userAction = JOptionPane.OK_OPTION;
       setVisible( false );
       removeParameters( parameters );
       device.getProtocol().reset();
       dispose();
     }
     else // must be a protocol parameter
       updateFixedData();
   } // actionPerformed
 
   // DocumentListener
   public void changedUpdate( DocumentEvent e )
   {
     docUpdated( e );
   }
 
   public void insertUpdate( DocumentEvent e )
   {
     docUpdated( e );
   }
 
   public void removeUpdate( DocumentEvent e )
   {
     docUpdated( e );
   }
 
   private void docUpdated( DocumentEvent e )
   {
     Document doc = e.getDocument();
     if ( doc == protocolID.getDocument())
       (( ManualProtocol )device.getProtocol()).setID( new Hex( protocolID.getText()));
     else if ( doc == fixedData.getDocument())
       (( ManualProtocol )device.getProtocol()).setRawHex( new Hex( fixedData.getText()));
     else if ( doc == deviceNotes.getDocument())
       device.setNotes( deviceNotes.getText());
     else
       updateFixedData();
   }
 
   // FocusListener
   public void focusGained( FocusEvent e )
   {
     controlToSelectAll = ( JTextComponent )e.getSource();
     SwingUtilities.invokeLater( this );
   }
 
   public void focusLost( FocusEvent e )
   {
   }
 
   // ItemListener
   public void itemStateChanged( ItemEvent e )
   {
     updateFixedData();
   }
 
   // Runnable
   public void run()
   {
     controlToSelectAll.selectAll();
   }
 
   public int getUserAction()
   {
     return userAction;
   }
 
   public CombinerDevice getCombinerDevice()
   {
     return device;
   }
 
   private CombinerDevice device = null;
   private JPanel mainPanel = null;
   private JPanel protocolHolder = null;
   private JComboBox protocolList = null;
   private JTextField deviceNotes = null;
   private JTextField protocolID = null;
   private JTextField fixedData = null;
   private JTextArea protocolNotes = null;
   private TableLayout tl = null;
   private DeviceParameter[] parameters = null;
   private JTextComponent controlToSelectAll = null;
 
   private JButton okButton = null;
   private JButton cancelButton = null;
   
   private int userAction = JOptionPane.CANCEL_OPTION;
 }
