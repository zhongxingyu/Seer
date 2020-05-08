 package com.hifiremote.jp1;
 
 import info.clearthought.layout.TableLayout;
 import java.beans.*;
 import javax.swing.*;
 import javax.swing.filechooser.*;
 import javax.swing.event.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 import java.io.*;
 import java.awt.datatransfer.*;
 
 public class DeviceEditorPanel
  extends JPanel
  implements ActionListener, ChangeListener, DocumentListener, PropertyChangeListener
 {
   private JTextField description = null;
   private JComboBox remoteList = null;
   private JComboBox deviceTypeList = null;
   private Remote[] remotes = new Remote[ 0 ];
   private ProtocolManager protocolManager = ProtocolManager.getProtocolManager();
   private JTabbedPane tabbedPane = null;
   private SetupPanel setupPanel = null;
   private FunctionPanel functionPanel = null;
   private ExternalFunctionPanel externalFunctionPanel = null;
   private ButtonPanel buttonPanel = null;
   private LayoutPanel layoutPanel = null;
   private OutputPanel outputPanel = null;
   private KeyMapPanel keyMapPanel = null;
   private DeviceUpgrade deviceUpgrade = null;
   private static File homeDirectory = null;
   private static String upgradeExtension = ".rmdu";
  private SwingPropertyChangeSupport propertyChangeSupport = null;
   public final static int ACTION_EXIT = 1;
   public final static int ACTION_LOAD = 2;
 
   public DeviceEditorPanel( DeviceUpgrade upgrade, Remote[] remotes )
   {
     super( new BorderLayout());
    propertyChangeSupport = new SwingPropertyChangeSupport( this );
 
     deviceUpgrade = upgrade;
     upgrade.addPropertyChangeListener( "protocol", this );
 
     tabbedPane = new JTabbedPane();
     add( tabbedPane, BorderLayout.CENTER );
 
     double b = 10;       // space around border/columns
     double i = 5;        // space between rows
     double f = TableLayout.FILL;
     double p = TableLayout.PREFERRED;
     double size[][] =
     {
       { b, p, b, f, b, p, b, p, b },                     // cols
       { b, p, i, p, b }         // rows
     };
     TableLayout tl = new TableLayout( size );
     JPanel panel = new JPanel( tl );
 
     JLabel label = new JLabel( "Description:" );
     panel.add( label, "1, 1" );
     description = new JTextField( 50 );
     description.setToolTipText( "Enter a short description for the upgrade being created." );
     label.setLabelFor( description );
     description.getDocument().addDocumentListener( this );
     panel.add( description, "3, 1, 7, 1" );
 
     new TextPopupMenu( description );
 
     label = new JLabel( "Remote:" );
     panel.add( label, "1, 3" );
     remoteList = new JComboBox( remotes );
     label.setLabelFor( remoteList );
     remoteList.setMaximumRowCount( 16 );
     remoteList.setPrototypeDisplayValue( "A Really Long Remote Control Name with an Extender and more" );
     remoteList.setToolTipText( "Choose the remote for the upgrade being created." );
     panel.add( remoteList, "3, 3" );
 
     label = new JLabel( "Device Type:" );
     panel.add( label, "5, 3" );
 //    String[] aliasNames = deviceUpgrade.getDeviceTypeAliasNames();
     deviceTypeList = new JComboBox();
     label.setLabelFor( deviceTypeList );
     deviceTypeList.setPrototypeDisplayValue( "A Device Type" );
     deviceTypeList.setToolTipText( "Choose the device type for the upgrade being created." );
     panel.add( deviceTypeList, "7, 3" );
 
     add( panel, BorderLayout.NORTH );
 
     setupPanel = new SetupPanel( deviceUpgrade );
     setupPanel.setToolTipText( "Enter general information about the upgrade." );
     currPanel = setupPanel;
     addPanel( setupPanel );
 
     functionPanel = new FunctionPanel( deviceUpgrade );
     functionPanel.setToolTipText( "Define function names and parameters." );
     addPanel( functionPanel );
 
     externalFunctionPanel = new ExternalFunctionPanel( deviceUpgrade );
     externalFunctionPanel.setToolTipText( "Define functions from other device codes." );
     addPanel( externalFunctionPanel );
 
     buttonPanel = new ButtonPanel( deviceUpgrade );
     buttonPanel.setToolTipText( "Assign functions to buttons." );
     addPanel( buttonPanel );
 
     layoutPanel = new LayoutPanel( deviceUpgrade );
     layoutPanel.setToolTipText( "Button Layout information." );
     addPanel( layoutPanel );
 
     keyMapPanel = new KeyMapPanel( deviceUpgrade );
     keyMapPanel.setToolTipText( "Printable list of buttons and their assigned functions" );
     addPanel( keyMapPanel );
 
     outputPanel = new OutputPanel( deviceUpgrade );
     outputPanel.setToolTipText( "The output to copy-n-paste into IR." );
     addPanel( outputPanel );
 
     setRemote( deviceUpgrade.getRemote());
 
     remoteList.addActionListener( this );
     deviceTypeList.addActionListener( this );
     tabbedPane.addChangeListener( this );
 
     refresh();
   }
   
   public void setDeviceUpgrade( DeviceUpgrade upgrade )
   {
     if ( deviceUpgrade != null )
       deviceUpgrade.removePropertyChangeListener( "protocol", this );
     deviceUpgrade = upgrade;
     deviceUpgrade.addPropertyChangeListener( "protocol", this );
     for ( int i = 0; i < tabbedPane.getTabCount(); ++i )
     {
       KMPanel panel = ( KMPanel )tabbedPane.getComponentAt( i );
       panel.setDeviceUpgrade( upgrade );
     }
 
     refresh();
   }
 
   public void setRemotes( Remote[] remotes )
   {
     Remote r = ( Remote )remoteList.getSelectedItem();
     remoteList.removeActionListener( this );
     remoteList.setModel( new DefaultComboBoxModel( remotes ));
     if ( r != null )
       remoteList.setSelectedItem( r );
     else
       remoteList.setSelectedIndex( 0 );
     remoteList.addActionListener( this );
   }
 
   public void addPanel( KMPanel panel )
   {
     tabbedPane.addTab( panel.getName(), null, panel, panel.getToolTipText());
   }
 
   public void addPanel( KMPanel panel, int index )
   {
     System.err.println( "KeyMapMaster.addPanel()" + panel );
     tabbedPane.insertTab( panel.getName(), null, panel, panel.getToolTipText(), index );
   }
 
   public void removePanel( KMPanel panel )
   {
     System.err.println( "KeyMapMaster.removePanel()" + panel );
     tabbedPane.removeTabAt( 1 );
     tabbedPane.validate();
   }
  
  public void addPropertyChangeListener( PropertyChangeListener l, String propertyName )
  {
    propertyChangeSupport.addPropertyChangeListener( propertyName, l );
  }
 
   public void setRemote( Remote remote )
   {
     if ( remoteList != null )
     {
       try
       {
         String[] aliasNames = remote.getDeviceTypeAliasNames();
         String alias = deviceUpgrade.getDeviceTypeAliasName();
         deviceTypeList.removeActionListener( this );
         deviceTypeList.setModel( new DefaultComboBoxModel( aliasNames ));
         deviceTypeList.setMaximumRowCount( aliasNames.length );
 
         int index = 0;
         for ( index = 0; index < aliasNames.length; index++ )
         {
           if ( aliasNames[ index ].equals( alias ))
             break;
         }
         while (( index == aliasNames.length ))
         {
           String msg = "Remote \"" + remote.getName() + "\" does not support the device type " +
           alias + ".  Please select one of the supported device types below to use instead.\n";
           
           String rc = ( String )JOptionPane.showInputDialog( null,
                                                              msg,
                                                              "Unsupported Device Type",
                                                              JOptionPane.ERROR_MESSAGE,
                                                              null,
                                                              aliasNames,
                                                              null );
           for ( index = 0; index < aliasNames.length; index++ )
           {
             if ( aliasNames[ index ].equals( rc ))
               break;
           }
         }
 
         deviceTypeList.setSelectedIndex( index );
 
         deviceUpgrade.setRemote( remote );
         deviceUpgrade.setDeviceTypeAliasName( aliasNames[ index ]);
         deviceTypeList.addActionListener( this );
         deviceUpgrade.checkSize();
       }
       catch ( Exception e )
       {
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter( sw );
         e.printStackTrace( pw );
         pw.flush();
         pw.close();
         JOptionPane.showMessageDialog( null, sw.toString(), "Remote Load Error",
                                        JOptionPane.ERROR_MESSAGE );
         System.err.println( sw.toString());
       }
     }
   }
 
   public void setDeviceTypeName( String aliasName )
   {
     if (( deviceTypeList != null ) && ( !aliasName.equals( deviceUpgrade.getDeviceTypeAliasName())))
     {
       deviceUpgrade.setDeviceTypeAliasName( aliasName );
       deviceTypeList.setSelectedItem( aliasName );
     }
   }
 
   // ActionListener Methods
   public void actionPerformed( ActionEvent e )
   {
     try
     {
       Object source = e.getSource();
 
       if ( source == remoteList )
       {
         Remote remote = ( Remote )remoteList.getSelectedItem();
        Remote oldRemote = deviceUpgrade.getRemote();
         setRemote( remote );
         currPanel.update();
         validateUpgrade();
        propertyChangeSupport.firePropertyChange( "remote", oldRemote, remote ); 
       }
       else if ( source == deviceTypeList )
       {
         String typeName = ( String )deviceTypeList.getSelectedItem();
        String oldDeviceType = deviceUpgrade.getDeviceTypeAliasName();
         setDeviceTypeName( typeName );
         currPanel.update();
       }
     }
     catch ( Exception ex )
     {
       ex.printStackTrace( System.err );
     }
   } // actionPerformed
 
   private void removeListeners()
   {
     description.getDocument().removeDocumentListener( this );
     remoteList.removeActionListener( this );
     deviceTypeList.removeActionListener( this );
   }
 
   private void addListeners()
   {
     description.getDocument().addDocumentListener( this );
     remoteList.addActionListener( this );
     deviceTypeList.addActionListener( this );
   }
 
   public void refresh()
   {
     String title = "RemoteMaster";
     File file = deviceUpgrade.getFile();
     if ( file != null )
       title = file.getAbsolutePath() + " - RemoteMaster";
 
     removeListeners();
     description.setText( deviceUpgrade.getDescription());
     String savedTypeName = deviceUpgrade.getDeviceTypeAliasName();
     Remote r = deviceUpgrade.getRemote();
 //    setRemote( r );
     remoteList.setSelectedItem( r );
     if ( remoteList.getSelectedItem() != r )
     {
       remoteList.addItem( r );
       remoteList.setSelectedItem( r );
     }
     deviceTypeList.setSelectedItem( savedTypeName );
     addListeners();
     KMPanel protocolPanel = deviceUpgrade.getProtocol().getPanel( deviceUpgrade );
     KMPanel tabPanel = ( KMPanel )tabbedPane.getComponentAt( 1 );
     if (( protocolPanel == null ) && ( tabPanel != functionPanel ))
       removePanel( protocolPanel );
     if (( protocolPanel != null ) && ( tabPanel != protocolPanel ))
       addPanel( protocolPanel, 1 );
     currPanel.update();
 
     validateUpgrade();
   }
 
   // ChangeListener methods
   private KMPanel currPanel = null;
   public void stateChanged( ChangeEvent e )
   {
     currPanel.commit();
     currPanel = ( KMPanel )(( JTabbedPane )e.getSource()).getSelectedComponent();
     currPanel.update();
     validateUpgrade();
   }
 
   public void validateUpgrade()
   {
     Remote r = deviceUpgrade.getRemote();
     Protocol p = deviceUpgrade.getProtocol();
     java.util.List< Protocol > protocols = protocolManager.getProtocolsForRemote( r );
     if ( !protocols.contains( p ) && !p.hasCode( r ))
     {
       System.err.println( "KeyMapMaster.validateUpgrade(), protocol " + p.getDiagnosticName() +
                           "is not compatible with remote " + r.getName());
 
       // Find a matching protocol for this remote
       Protocol match = null;
       String name = p.getName();
       for ( Protocol p2 : protocols )
       {
         if ( p2.getName().equals( name ))
         {
           match = p2;
           System.err.println( "\tFound one with the same name: " + p2.getDiagnosticName());
           break;
         }
       }
       if ( match != null )
         deviceUpgrade.setProtocol( match );
       else
         JOptionPane.showMessageDialog( this,
                                        "The selected protocol " + p.getDiagnosticName() +
                                        "\nis not compatible with the selected remote.\n" +
                                        "This upgrade will NOT function correctly.\n" +
                                        "Please choose a different protocol.",
                                        "Error", JOptionPane.ERROR_MESSAGE );
     }
   }
 
   private void updateDescription()
   {
     deviceUpgrade.setDescription( description.getText());
     currPanel.update();
   }
 
   public DeviceUpgrade getDeviceUpgrade()
   {
     return deviceUpgrade;
   }
 
   // DocumentListener methods
   public void changedUpdate( DocumentEvent e )
   {
     updateDescription();
   }
 
   public void insertUpdate( DocumentEvent e )
   {
     updateDescription();
   }
 
   public void removeUpdate( DocumentEvent e )
   {
     updateDescription();
   }
   
   // PropertyChangeListener
   public void propertyChange( PropertyChangeEvent evt )
   {
     Protocol protocol = ( Protocol )evt.getOldValue();
     System.err.print( "DeviceEditorPanel.propertyChange( " + protocol.getDiagnosticName() + ", " );
     KMPanel panel = protocol.getPanel( deviceUpgrade );
     protocol = ( Protocol )evt.getNewValue();
     System.err.println( protocol.getDiagnosticName());
     
     if ( panel != null ) 
       removePanel( panel );
     panel = protocol.getPanel( deviceUpgrade );
     if ( panel != null )
       addPanel( panel, 1 );
   }
 }
 
