 package com.hifiremote.jp1;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.awt.datatransfer.*;
 
 public class OutputPanel
   extends KMPanel implements ActionListener
 {
   public OutputPanel( DeviceUpgrade deviceUpgrade )
   {
     super( "Output", deviceUpgrade );
     BoxLayout bl = new BoxLayout( this, BoxLayout.Y_AXIS );
     setLayout( bl );
     setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
 
     clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 
     includeNotes = new JCheckBox( "Include embedded notes in upgrades (requires IR v 5.01 or later)", true );
     includeNotes.addActionListener( this );
     add( includeNotes );
 
     Box box = Box.createHorizontalBox();
     box.setBorder( BorderFactory.createEmptyBorder( 0, 0, 5, 0 ));
     add( box );
 
     JLabel label = new JLabel( "Device Upgrade Code" );
     label.setAlignmentY( 1f );
     box.add( label );
 
     box.add( box.createHorizontalGlue());
 
     copyDeviceUpgrade = new JButton( "Copy" );
     copyDeviceUpgrade.setAlignmentY( 1f );
     copyDeviceUpgrade.addActionListener( this );
     box.add( copyDeviceUpgrade );
 
     upgradeText = new JTextArea( 10, 40 );
     upgradeText.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
     upgradeText.setEditable( false );
     upgradeText.setDragEnabled( true );
     upgradeText.setBackground( label.getBackground());
     JScrollPane scroll = new JScrollPane( upgradeText );
     add( scroll );
 
     popup = new JPopupMenu();
     copyItem = new JMenuItem( "Copy" );
     copyItem.addActionListener( this );
     popup.add( copyItem );
 
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
 
       private void showPopup( MouseEvent e )
       {
         if ( e.isPopupTrigger() )
         {
           popover = ( JTextArea )e.getSource();
           popup.show( popover, e.getX(), e.getY());
         }
       }
     };
     upgradeText.addMouseListener( mh );
 
     add( Box.createVerticalStrut( 20 ));
 
     box = Box.createHorizontalBox();
     add( box );
     box.setBorder( BorderFactory.createEmptyBorder( 0, 0, 5, 0 ));
 
     protocolLabel = new JLabel( "Upgrade Protocol Code" );
     protocolLabel.setAlignmentY( 1f );
     box.add( protocolLabel );
     box.add( box.createHorizontalGlue());
 
     copyProtocolUpgrade = new JButton( "Copy" );
     copyProtocolUpgrade.setAlignmentY( 1f );
     copyProtocolUpgrade.addActionListener( this );
     box.add( copyProtocolUpgrade );
 
     protocolText = new JTextArea( 10, 40 );
     protocolText.setEditable( false );
     protocolText.setDragEnabled( true );
     protocolText.addMouseListener( mh );
     protocolText.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
     protocolText.setBackground( label.getBackground());
 
     scroll = new JScrollPane( protocolText );
     add( scroll );
   }
 
   private int adjust( int val )
   {
     int temp1 = val - 0x2C;
     int temp2 = val - 0x46;
     if ((( 0 <= temp1 ) && ( temp1 <= 0x0E ) && ( temp1 % 7 == 0 )) ||
         (( 0 <= temp2 ) && ( temp2 <= 0x0E ) && ( temp2 % 3 == 0 )))
     {
       val -= 0x13;
     }
     return val;
   }
 
   public void update()
   {
     boolean flag = includeNotes.isSelected();
     upgradeText.setText( deviceUpgrade.getUpgradeText( flag ));
     Protocol p = deviceUpgrade.getProtocol();
     String pVariant = p.getVariantName();
 
     Remote r = deviceUpgrade.getRemote();
 
    if ( r.supportsVariant( p.getID(), pVariant ))
     {
       protocolLabel.setForeground( Color.BLACK );
       protocolLabel.setText( "Upgrade Protocol Code" );
       protocolText.setText( null );
       return;
     }
     protocolLabel.setForeground( Color.RED );
     protocolLabel.setText( "Upgrade Protocol Code *** REQUIRED ***" );
     Processor processor = r.getProcessor();
     Hex code = p.getCode( r );
     if ( code != null )
     {
       code = processor.translate( code, r );
       Translate[] xlators = p.getCodeTranslators( r );
       if ( xlators != null )
       {
         Value[] values = deviceUpgrade.getParmValues();
         for ( int i = 0; i < xlators.length; i++ )
           xlators[ i ].in( values, code, null, -1 );
       }
 
       StringBuffer buff = new StringBuffer( 300 );
       buff.append( "Upgrade protocol 0 = " );
       buff.append( p.getID( r ).toString());
       buff.append( " (" );
       buff.append( processor.getFullName());
       buff.append( ")" );
       if ( flag )
       {
         buff.append( ' ' );
         buff.append( p.getName());
         buff.append( " (RM " );
         buff.append( KeyMapMaster.version );
         buff.append( ')' );
       }
       buff.append( "\n " );
       buff.append( code.toString( 16 ));
       buff.append( "\nEnd" );
       protocolText.setText( buff.toString());
     }
   }
 
   public void actionPerformed( ActionEvent e )
   {
     JTextArea area = null;
     Object source = e.getSource();
     if ( source == includeNotes )
     {
       update();
       return;
     }
     if ( source == copyDeviceUpgrade )
       area = upgradeText;
     else if ( source == copyProtocolUpgrade )
       area = protocolText;
     else // assume copyItem
       area = popover;
 
     StringSelection data = new StringSelection( area.getText());
     clipboard.setContents( data, data );
   }
 
   private JLabel protocolLabel = null;
   private JTextArea upgradeText = null;
   private JTextArea protocolText = null;
   private JTextArea popover = null;
   private JPopupMenu popup = null;
   private JMenuItem copyItem = null;
   private JButton copyDeviceUpgrade = null;
   private JButton copyProtocolUpgrade = null;
   private Clipboard clipboard = null;
   private JCheckBox includeNotes = null;
 }
