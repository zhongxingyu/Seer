 package com.hifiremote.jp1;
 
 import java.awt.*;
 import javax.swing.border.*;
 import javax.swing.*;
 import java.text.*;
 import javax.swing.text.*;
 import java.awt.event.*;
 import javax.swing.event.*;
 import java.util.*;
 import info.clearthought.layout.*;
 import java.beans.*;
 
 public class SetupPanel
   extends KMPanel
   implements ActionListener, ItemListener, PropertyChangeListener, DocumentListener, FocusListener, Runnable
 {
   public SetupPanel( DeviceUpgrade deviceUpgrade )
   {
     super( "Setup", deviceUpgrade );
 
     protocolHolder = new JPanel( new BorderLayout());
     Border border = BorderFactory.createTitledBorder( "Protocol Parameters" );
     protocolHolder.setBorder( border );
 
     Insets insets = border.getBorderInsets( protocolHolder );
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
     {
       { b, bl, p, b, p, br, c, f, b },                     // cols
       { b, p, v, p, i, p, v, bt, p, bb, f, b }         // rows
     };
     tl = new TableLayout( size );
     setLayout( tl );
 
     JLabel label = new JLabel( "Setup Code:", SwingConstants.RIGHT );
     add( label, "2, 1" );
 
     NumberFormatter nf = new NumberFormatter( new DecimalFormat( "0000" ));
     nf.setValueClass( Integer.class );
     nf.setMinimum( new Integer( 0 ));
     nf.setMaximum( new Integer( 2047));
     nf.setCommitsOnValidEdit( true );
 
     setupCode = new JFormattedTextField( nf );
     setupCode.addPropertyChangeListener( this );
     setupCode.addFocusListener( this );
     label.setLabelFor( setupCode );
     setupCode.setToolTipText( "Enter the desired setup code (between 0 and 2047) for the device upgrade." );
 
     add( setupCode, "4, 1" );
 
     label = new JLabel( "Protocol:", SwingConstants.RIGHT );
     add( label, "2, 3" );
 
     protocolList = new JComboBox();
     protocolList.addActionListener( this );
     label.setLabelFor( protocolList );
     protocolList.setToolTipText( "Select the protocol to be used for this device upgrade from the drop-down list." );
     add( protocolList, "4, 3" );
 
     label = new JLabel( "Protocol ID:", SwingConstants.RIGHT );
     add( label, "2, 5" );
 
     protocolID = new JTextField();
     label.setLabelFor( protocolID );
     protocolID.setEditable( false );
     protocolID.setToolTipText( "This is the protocol ID that corresponds to the selected protocol." );
     add( protocolID, "4, 5" );
 
     add( protocolHolder, "1, 7, 5, 9" );
     label = new JLabel( "Fixed Data:", SwingConstants.RIGHT );
     add( label, "2, 8" );
 
     fixedData = new JTextField();
     fixedData.setEditable( false );
     add( fixedData, "4, 8" );
 
    JPanel notesPanel = new JPanel( new BorderLayout());
    notes = new JTextArea();
     notes.setToolTipText( "Enter any notes about this device upgrade." );
     notes.setLineWrap( true );
     notes.setWrapStyleWord( true );
    notesPanel.setBorder( BorderFactory.createTitledBorder( "Upgrade Notes" ));
    notesPanel.add( new JScrollPane( notes ), BorderLayout.CENTER );
     notes.getDocument().addDocumentListener( this );
    add( notesPanel, "7, 1, 7, 9" );
 
    notesPanel = new JPanel( new BorderLayout());
     protocolNotes = new JTextArea();
     protocolNotes.setBackground( label.getBackground());
     protocolNotes.setToolTipText( "Notes about the selected protocol." );
     protocolNotes.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
     protocolNotes.setEditable( false );
     protocolNotes.setLineWrap( true );
     protocolNotes.setWrapStyleWord( true );
    notesPanel.setBorder( BorderFactory.createTitledBorder( "Protocol Notes" ));
    notesPanel.add( new JScrollPane( protocolNotes ), BorderLayout.CENTER );
    add( notesPanel, "1, 10, 7, 10" );
   } // SetupPanel
 
   public void update()
   {
     updateInProgress = true;
     setupCode.setValue( new Integer( deviceUpgrade.getSetupCode()));
     Protocol p = deviceUpgrade.getProtocol();
     Remote remote = deviceUpgrade.getRemote();
     Vector protocols = ProtocolManager.getProtocolManager().getProtocolsForRemote( remote );
     if ( !protocols.contains( p ))
     {
       // ??? There should be a better way to handle this (the current protocol is
       // incompatible with the current remote), but this way is at least better than
       // the old way of displaying the first compatible protocol.
       protocols = new Vector( protocols );
       protocols.add( p );
     }
 
     p.setDeviceParms( deviceUpgrade.getParmValues());
     updateParameters();
     protocolList.setModel( new DefaultComboBoxModel( protocols ));
     protocolList.setSelectedItem( p );
     protocolID.setText( p.getID().toString());
     notes.setText( deviceUpgrade.getNotes());
     fixedData.setText( p.getFixedData().toString());
     protocolNotes.setText( p.getNotes());
     protocolNotes.setCaretPosition( 0 );
     updateInProgress = false;
   }
 
   public void updateParameters()
   {
     DeviceParameter[] newParameters = deviceUpgrade.getProtocol().getDeviceParameters();
     if ( parameters != newParameters )
     {
       if ( parameters != null )
       {
         for ( int i = 0; i < parameters.length; i++ )
         {
           parameters[ i ].removeListener( this );
           remove( parameters[ i ].getLabel());
           remove( parameters[ i ].getComponent());
           tl.deleteRow( 8 );
           tl.deleteRow( 8 );
         }
       }
       parameters = newParameters;
       if ( parameters != null )
       {
         int row = 8;
         for ( int i = 0; i < parameters.length; i++ )
         {
           parameters[ i ].addListener( this );
           tl.insertRow( row, TableLayout.PREFERRED );
           add( parameters[ i ].getLabel(), "2, " + row );
           add( parameters[ i ].getComponent() , "4, " + row );
           row++;
           tl.insertRow( row++, 5 );
         }
         TableLayoutConstraints tlc = tl.getConstraints( protocolHolder );
         remove( protocolHolder );
         add( protocolHolder, tlc );
       }
     }
   }
 
   public void updateFixedData()
   {
     Protocol p = deviceUpgrade.getProtocol();
     p.initializeParms();
     deviceUpgrade.setParmValues( p.getDeviceParmValues());
     fixedData.setText( p.getFixedData().toString());
   }
 
   // ActionListener Methods
   public void actionPerformed( ActionEvent e )
   {
     Object source = e.getSource();
 
     if ( source == protocolList )
     {
       Protocol newProtocol = getSelectedProtocol();
       Protocol oldProtocol = deviceUpgrade.getProtocol();
       if ( newProtocol != oldProtocol )
       {
         KMPanel panel = oldProtocol.getPanel( deviceUpgrade );
         if ( panel != null )
           KeyMapMaster.getKeyMapMaster().removePanel( panel );
         if ( newProtocol != null && oldProtocol != null && !updateInProgress )
           oldProtocol.convertFunctions( deviceUpgrade.getFunctions(), newProtocol );
         oldProtocol.reset();
         protocolID.setText( newProtocol.getID().toString());
         deviceUpgrade.setProtocol( newProtocol );
         panel = newProtocol.getPanel( deviceUpgrade );
         if ( panel != null )
           KeyMapMaster.getKeyMapMaster().addPanel( panel, 1 );
         updateParameters();
         fixedData.setText( newProtocol.getFixedData().toString());
         revalidate();
         protocolNotes.setText( newProtocol.getNotes());
         protocolNotes.setCaretPosition( 0 );
         protocolNotes.revalidate();
       }
     }
     else // must be a protocol parameter
       updateFixedData();
   } // actionPerformed
 
   public Protocol getSelectedProtocol()
   {
     Protocol protocol = ( Protocol )protocolList.getSelectedItem();
     return protocol;
   }
 
   public void commit()
   {
     deviceUpgrade.getProtocol().updateFunctions( deviceUpgrade.getFunctions());
   }
 
   private void updateNotes()
   {
     deviceUpgrade.setNotes( notes.getText());
   }
 
   private void updateSetupCode()
   {
     int val = (( Integer )setupCode.getValue()).intValue();
     deviceUpgrade.setSetupCode( val );
   }
 
   private void docChanged( DocumentEvent e )
   {
     if ( !updateInProgress )
     {
       Document doc = e.getDocument();
       if ( doc == notes.getDocument() )
         updateNotes();
       else
         updateFixedData();
     }
   }
 
   // DocumentListener
   public void changedUpdate( DocumentEvent e )
   {
     docChanged( e );
   }
 
   public void insertUpdate( DocumentEvent e )
   {
     docChanged( e );
   }
 
   public void removeUpdate( DocumentEvent e )
   {
     docChanged( e );
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
     if ( !updateInProgress )
       updateFixedData();
   }
 
   // PropertyChangeListener methods
   public void propertyChange( PropertyChangeEvent e )
   {
     if ( !updateInProgress )
     {
       if (( e.getSource() == setupCode ) && e.getPropertyName().equals( "value" ))
       {
         updateSetupCode();
       }
     }
   }
 
   // Runnable
   public void run()
   {
    controlToSelectAll.selectAll();
   }
 
   private JFormattedTextField setupCode = null;
   private JRadioButton useEFC = null;
   private JRadioButton useOBC = null;
   private JComboBox protocolList = null;
   private JTextField protocolID = null;
   private JTextArea notes = null;
   private JPanel protocolHolder = null;
   private JTextField fixedData = null;
   private JTextArea protocolNotes = null;
   private DeviceParameter[] parameters = null;
   private TableLayout tl;
   private boolean updateInProgress = false;
   private static DecimalFormat nf = new DecimalFormat( "0000" );
   private JTextComponent controlToSelectAll = null;
 }
 
