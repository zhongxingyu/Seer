 package com.hifiremote.jp1;
 
 import info.clearthought.layout.TableLayout;
 import javax.swing.*;
 import javax.swing.filechooser.*;
 import javax.swing.event.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 import java.io.*;
 import java.awt.datatransfer.*;
 
 public class KeyMapMaster
  extends JFrame
  implements ActionListener, ChangeListener, DocumentListener
 {
   private static KeyMapMaster me = null;
  public static final String version = "v1.21";
   private Preferences preferences = null;
   private JMenuItem newItem = null;
   private JMenuItem openItem = null;
   private JMenuItem saveItem = null;
   private JMenuItem saveAsItem = null;
 //  private JMenuItem importItem = null;
   private JMenuItem importFromClipboardItem = null;
   private JMenu recentFileMenu = null;
   private JMenuItem exitItem = null;
   private JMenuItem manualItem = null;
   private JMenuItem editorItem = null;
   private JMenuItem aboutItem = null;
   private JLabel messageLabel = null;
   private JTextField description = null;
   private JComboBox remoteList = null;
   private JComboBox deviceTypeList = null;
   private Remote[] remotes = new Remote[ 0 ];
   private ProtocolManager protocolManager = ProtocolManager.getProtocolManager();
   private Remote currentRemote = null;
   private String currentDeviceTypeName = null;
   private JTabbedPane tabbedPane = null;
   private SetupPanel setupPanel = null;
   private FunctionPanel functionPanel = null;
   private ExternalFunctionPanel externalFunctionPanel = null;
   private ButtonPanel buttonPanel = null;
   private LayoutPanel layoutPanel = null;
   private OutputPanel outputPanel = null;
   private KeyMapPanel keyMapPanel = null;
   private ProgressMonitor progressMonitor = null;
   private DeviceUpgrade deviceUpgrade = null;
   private static File homeDirectory = null;
   private File propertiesFile = null;
   private static String upgradeExtension = ".rmdu";
   public final static int ACTION_EXIT = 1;
   public final static int ACTION_LOAD = 2;
 
   public KeyMapMaster( String[] args )
     throws Exception
   {
     super( "RemoteMaster" );
     me = this;
 
     File fileToOpen = parseArgs( args );
 
     setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
     setDefaultLookAndFeelDecorated( true );
     JDialog.setDefaultLookAndFeelDecorated( true );
     JFrame.setDefaultLookAndFeelDecorated( true );
     Toolkit.getDefaultToolkit().setDynamicLayout( true );
 
     preferences = new Preferences( homeDirectory, propertiesFile );
 
     addWindowListener( new WindowAdapter()
     {
       public void windowClosing( WindowEvent event )
       {
         try
         {
           if ( !promptToSaveUpgrade( ACTION_EXIT ))
             return;
           preferences.save( recentFileMenu );
         }
         catch ( Exception e )
         {
           System.err.println( "KeyMapMaster.windowClosing() caught an exception!" );
           e.printStackTrace( System.out );
         }
         System.exit( 0 );
       }
     });
 
     createMenus();
     
     preferences.load( recentFileMenu );
 
     deviceUpgrade = new DeviceUpgrade();
 
     Container mainPanel = getContentPane();
     tabbedPane = new JTabbedPane();
     mainPanel.add( tabbedPane, BorderLayout.CENTER );
 
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
     remoteList = new JComboBox();
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
 
     mainPanel.add( panel, BorderLayout.NORTH );
 
     messageLabel = new JLabel( " " );
     messageLabel.setForeground( Color.red );
 
     mainPanel.add( messageLabel, BorderLayout.SOUTH );
 
     protocolManager.load( new File( homeDirectory, "protocols.ini" ));
 
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
 
     RemoteManager rm = RemoteManager.getRemoteManager();
     preferences.setRDFPath( rm.loadRemotes( preferences.getRDFPath()));
 
     setRemotes();
 
     Remote r = null;
     String name = preferences.getLastRemoteName();
     if ( name != null )
       r = rm.findRemoteByName( name );
     if ( r == null )
       r = rm.getRemotes()[ 0 ];
     r.load();
     Protocol protocol =
       ( Protocol )protocolManager.getProtocolsForRemote( r ).elementAt( 0 );
     deviceUpgrade.setProtocol( protocol );
     setRemote( r );
     remoteList.setSelectedItem( r );
 
     remoteList.addActionListener( this );
     deviceTypeList.addActionListener( this );
     tabbedPane.addChangeListener( this );
 
     currPanel.update();
 
     clearMessage();
 
     Rectangle bounds = preferences.getBounds();
     if ( bounds != null )
       setBounds( bounds );
     else
       pack();
 
     loadUpgrade( fileToOpen );
 
     show();
   }
 
   public static KeyMapMaster getKeyMapMaster(){ return me;}
 
   public void setRemoteList( Remote[] remotes )
   {
     Remote r = ( Remote )remoteList.getSelectedItem();
     remoteList.removeActionListener( this );
     remoteList.setModel( new DefaultComboBoxModel( remotes ));
     remoteList.setSelectedItem( r );
     remoteList.addActionListener( this );
   }
 
   private void createMenus()
   {
     JMenuBar menuBar = new JMenuBar();
     setJMenuBar( menuBar );
     JMenu menu = new JMenu( "File" );
     menu.setMnemonic( KeyEvent.VK_F );
     menuBar.add( menu );
     newItem = new JMenuItem( "New" );
     newItem.setMnemonic( KeyEvent.VK_N );
     newItem.addActionListener( this );
     menu.add( newItem );
     openItem = new JMenuItem( "Open..." );
     openItem.setMnemonic( KeyEvent.VK_O );
     openItem.addActionListener( this );
     menu.add( openItem );
     saveItem = new JMenuItem( "Save" );
     saveItem.setMnemonic( KeyEvent.VK_S );
     saveItem.setEnabled( false );
     saveItem.addActionListener( this );
     menu.add( saveItem );
     saveAsItem = new JMenuItem( "Save as..." );
     saveAsItem.setMnemonic( KeyEvent.VK_A );
     saveAsItem.setDisplayedMnemonicIndex( 5 );
     saveAsItem.addActionListener( this );
     menu.add( saveAsItem );
 
     menu.addSeparator();
 
 //    importItem = new JMenuItem( "Import KM file..." );
 //    importItem.setMnemonic( KeyEvent.VK_K );
 //    importItem.addActionListener( this );
 //    menu.add( importItem );
 //
     importFromClipboardItem = new JMenuItem( "Import from Clipboard" );
     importFromClipboardItem.setMnemonic( KeyEvent.VK_C );
     importFromClipboardItem.addActionListener( this );
     menu.add( importFromClipboardItem );
 
     menu.addSeparator();
 
     recentFileMenu = new JMenu( "Recent" );
     recentFileMenu.setMnemonic( KeyEvent.VK_R );
     recentFileMenu.setEnabled( false );
     menu.add( recentFileMenu );
     menu.addSeparator();
 
     exitItem = new JMenuItem( "Exit" );
     exitItem.setMnemonic( KeyEvent.VK_X );
     exitItem.addActionListener( this );
     menu.add( exitItem );
 
     menu = new JMenu( "Options" );
     menu.setMnemonic( KeyEvent.VK_O );
     menuBar.add( menu );
 
     preferences.createMenuItems( menu );
 
     menu = new JMenu( "Advanced" );
     menu.setMnemonic( KeyEvent.VK_A );
     menuBar.add( menu );
 
     manualItem = new JMenuItem( "Manual Settings..." );
     manualItem.setMnemonic( KeyEvent.VK_M );
     manualItem.addActionListener( this );
     menu.add( manualItem );
 
     editorItem = new JMenuItem( "Protocol Editor..." );
     editorItem.setMnemonic( KeyEvent.VK_P );
     editorItem.addActionListener( this );
     menu.add( editorItem );
 
     menu = new JMenu( "Help" );
     menu.setMnemonic( KeyEvent.VK_H );
     menuBar.add( menu );
 
     aboutItem = new JMenuItem( "About..." );
     aboutItem.setMnemonic( KeyEvent.VK_A );
     aboutItem.addActionListener( this );
     menu.add( aboutItem );
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
 //    tabbedPane.remove( panel );
     tabbedPane.validate();
   }
 
   private void editManualSettings()
   {
     ManualSettingsDialog d =
       new ManualSettingsDialog( this, protocolManager.getManualProtocol());
     d.show();
   }
 
   private File parseArgs( String[] args )
   {
     homeDirectory = new File( System.getProperty( "user.dir" ));
     File fileToOpen = null;
     for ( int i = 0; i < args.length; i++ )
     {
       String arg = args[ i ];
       if ( arg.charAt( 0 ) == '-' )
       {
         char flag = arg.charAt( 1 );
         String parm = args[ ++i ];
         if ( flag == 'h' )
         {
           homeDirectory = new File( parm );
         }
         else if ( flag == 'p' )
         {
           propertiesFile = new File( parm );
         }
       }
       else
         fileToOpen = new File( arg );
     }
     try
     {
       System.setErr( new PrintStream( new FileOutputStream( new File ( homeDirectory, "rmaster.err" ))));
     }
     catch ( Exception e )
     {
       e.printStackTrace( System.err );
     }
     if ( propertiesFile == null )
     {
       propertiesFile = new File( homeDirectory, "RemoteMaster.properties" );
     }
 
     return fileToOpen;
   }
 
   public static void showMessage( String msg )
   {
     if ( msg.length() == 0 )
       msg = " ";
     me.messageLabel.setText( msg );
     Toolkit.getDefaultToolkit().beep();
   }
 
   public static void clearMessage()
   {
     me.messageLabel.setText( " " );
   }
 
   public void setRemotes()
   {
     if ( remoteList != null )
     {
       if ( preferences.getUsePreferredRemotes())
         try
         {
           remoteList.setModel( new DefaultComboBoxModel( preferences.getPreferredRemotes()));
         }
         catch ( Exception e )
         {
           e.printStackTrace( System.err );
         }
       else
         remoteList.setModel( new DefaultComboBoxModel( RemoteManager.getRemoteManager().getRemotes()));
     }
   }
 
   public void setRemote( Remote remote )
   {
     if (( remoteList != null ) && ( remote != currentRemote ))
     {
       try
       {
         remote.load();
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
 
         currentRemote = remote;
         deviceUpgrade.setRemote( remote );
         deviceUpgrade.setDeviceTypeAliasName( aliasNames[ index ]);
         deviceTypeList.addActionListener( this );
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
     if (( deviceTypeList != null ) && ( aliasName != currentDeviceTypeName ))
     {
       currentDeviceTypeName = aliasName;
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
         setRemote( remote );
         currPanel.update();
         validateUpgrade();
       }
       else if ( source == deviceTypeList )
       {
         String typeName = ( String )deviceTypeList.getSelectedItem();
         setDeviceTypeName( typeName );
         currPanel.update();
       }
       else if ( source == newItem )
       {
         if ( !promptToSaveUpgrade( ACTION_LOAD ))
           return;
         Protocol oldProtocol = deviceUpgrade.getProtocol();
         deviceUpgrade.reset();
         Protocol newProtocol = deviceUpgrade.getProtocol();
         if ( newProtocol != oldProtocol )
         {
           KMPanel oldPanel = oldProtocol.getPanel( deviceUpgrade );
           if ( oldPanel != null )
             removePanel( oldPanel );
           KMPanel newPanel = newProtocol.getPanel( deviceUpgrade );
           if ( newPanel != null )
             addPanel( newPanel, 1 );
           if (( oldPanel != null ) || ( newPanel != null ))
             tabbedPane.validate();
         }
         oldProtocol.reset();
         setTitle( "RemoteMapMaster" );
         description.setText( null );
         remoteList.setSelectedItem( deviceUpgrade.getRemote());
         deviceTypeList.setSelectedItem( deviceUpgrade.getDeviceTypeAliasName());
         saveItem.setEnabled( false );
         currPanel.update();
       }
       else if ( source == saveItem )
       {
         currPanel.commit();
         deviceUpgrade.store();
       }
       else if ( source == saveAsItem )
       {
         currPanel.commit();
         saveAs();
       }
       else if ( source == openItem )
       {
         if ( !promptToSaveUpgrade( ACTION_LOAD ))
           return;
         File file = getUpgradeFile( preferences.getUpgradePath());
         loadUpgrade( file );
       }
       else if ( source == importFromClipboardItem )
       {
         if ( !promptToSaveUpgrade( ACTION_LOAD ))
           return;
         Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
         Transferable clipData = clipboard.getContents( clipboard );
         if ( clipData != null )
         {
           try
           {
             if ( clipData.isDataFlavorSupported( DataFlavor.stringFlavor ))
             {
               String s =
                 ( String )( clipData.getTransferData( DataFlavor.stringFlavor ));
               BufferedReader in = new BufferedReader( new StringReader( s ));
               loadUpgrade( in );
             }
           }
           catch (Exception ex)
           {
             ex.printStackTrace( System.err );
           }
         }
       }
       else if ( source == exitItem )
       {
         dispatchEvent( new WindowEvent( this, WindowEvent.WINDOW_CLOSING ));
       }
       else if ( source == manualItem )
       {
         editManualSettings();
       }
       else if ( source == editorItem )
       {
         ProtocolEditor d = new ProtocolEditor( this );
         d.show();
       }
       else if ( source == aboutItem )
       {
         String text = "<html><b>RemoteMaster Device Upgrade Editor, " + version + "</b>" +
                       "<p>Get the latest version at <a href=\"http://controlremote.sourceforge.net\">http://controlremote.sourceforge.net</a></p>" +
                       "<p>Java version " + System.getProperty( "java.version" ) + " from " + System.getProperty( "java.vendor" ) + "</p>" +
                       "<p>RDFs loaded from <b>" + preferences.getRDFPath() + "</b></p>" +
                       "<p>Written primarily by <i>Greg Bush</i>, and now accepting donations " +
                       "at <a href=\"http://sourceforge.net/donate/index.php?user_id=735638\">http://sourceforge.net/donate/index.php?user_id=735638</a></p>" +
                       "<p>Other contributors include:<blockquote>" + "John&nbsp;S&nbsp;Fine, Nils&nbsp;Ekberg, Jon&nbsp;Armstrong, Robert&nbsp;Crowe, " +
                       "Mark&nbsp;Pauker, Mark&nbsp;Pierson, Mike&nbsp;England</blockquote></html>";
 
         JEditorPane pane = new JEditorPane( "text/html", text );
         pane.setEditable( false );
         pane.setBackground( getContentPane().getBackground());
         new TextPopupMenu( pane );
         JScrollPane scroll = new JScrollPane( pane );
         Dimension d = scroll.getPreferredSize();
         d.height = d.height / 2;
         d.width = ( d.width * 2 ) / 3;
         scroll.setPreferredSize( d );
 
         JOptionPane.showMessageDialog( this, scroll, "About RemoteMaster", JOptionPane.INFORMATION_MESSAGE );
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
 
   public static File promptForUpgradeFile( File path )
   {
     return me.getUpgradeFile( path );
   }
 
   public File getUpgradeFile( File path )
   {
     if ( path == null )
       path = preferences.getUpgradePath();
 
     File file = null;
     JFileChooser chooser = new JFileChooser( path );
     try
     {
       chooser.setAcceptAllFileFilterUsed( false );
     }
     catch ( Exception e )
     {
       e.printStackTrace( System.err );
     }
     chooser.setFileFilter( new AnyFileFilter());
     chooser.addChoosableFileFilter( new TextFileFilter());
     chooser.addChoosableFileFilter( new KMFileFilter());
     int returnVal = chooser.showOpenDialog( this );
     if ( returnVal == JFileChooser.APPROVE_OPTION )
     {
       file = chooser.getSelectedFile();
 
       int rc = JOptionPane.YES_OPTION;
       if ( !file.exists())
       {
         JOptionPane.showMessageDialog( this,
                                        file.getName() + " doesn't exist.",
                                        "File doesn't exist.",
                                        JOptionPane.ERROR_MESSAGE );
       }
       else if ( file.isDirectory())
       {
         JOptionPane.showMessageDialog( this,
                                        file.getName() + " is a directory.",
                                        "File doesn't exist.",
                                        JOptionPane.ERROR_MESSAGE );
       }
       else
       {
         String str = file.getName().toLowerCase();
         preferences.setUpgradePath( file.getParentFile());
       }
     }
     return file;
   }
 
   public void saveAs()
     throws IOException
   {
     JFileChooser chooser = new JFileChooser( preferences.getUpgradePath());
     chooser.setFileFilter( new KMFileFilter());
     File f = deviceUpgrade.getFile();
     if ( f == null )
     {
       String fname = deviceUpgrade.getDescription() + upgradeExtension;
       fname = fname.replace( '/', '-' );
       f = new File( preferences.getUpgradePath(), fname );
     }
     chooser.setSelectedFile( f );
     int returnVal = chooser.showSaveDialog( this );
     if ( returnVal == JFileChooser.APPROVE_OPTION )
     {
       String name = chooser.getSelectedFile().getAbsolutePath();
       if ( !name.toLowerCase().endsWith( upgradeExtension ))
         name = name + upgradeExtension;
       File file = new File( name );
       int rc = JOptionPane.YES_OPTION;
       if ( file.exists())
       {
         rc = JOptionPane.showConfirmDialog( this,
                                             file.getName() + " already exists.  Do you want to repalce it?",
                                             "Replace existing file?",
                                             JOptionPane.YES_NO_OPTION );
       }
       if ( rc == JOptionPane.YES_OPTION )
       {
         deviceUpgrade.store( file );
         updateRecentFiles( file );
         saveItem.setEnabled( true );
         setTitle( file.getCanonicalPath() + " - RemoteMaster" );
       }
     }
   }
 
   public static String[] getCustomNames()
   {
     return me.preferences.getCustomNames();
   }
 
   public boolean promptToSaveUpgrade( int action )
     throws IOException
   {
     int promptFlag = preferences.getPromptFlag();
     if ( promptFlag == Preferences.PROMPT_NEVER )
       return true;
     else if ( promptFlag != Preferences.PROMPT_ALWAYS )
     {
       if ( action != ACTION_EXIT )
         return true;
     }
 
     int rc = JOptionPane.showConfirmDialog( this,
 //                                            "All changes made to the current upgrade will be lost if you proceed.\n\n" +
                                             "Do you want to save the current upgrade before proceeding?",
                                             "Save upgrade?",
                                             JOptionPane.YES_NO_CANCEL_OPTION );
     if (( rc == JOptionPane.CANCEL_OPTION ) || ( rc == JOptionPane.CLOSED_OPTION ))
       return false;
     if ( rc == JOptionPane.NO_OPTION )
       return true;
 
     currPanel.commit();
     if ( deviceUpgrade.getFile() != null )
       deviceUpgrade.store();
     else
       saveAs();
     return true;
   }
 
   public void loadUpgrade( File file )
     throws Exception
   {
    if ( file == null )
       return;
 
     Protocol oldProtocol = deviceUpgrade.getProtocol();
     KMPanel oldPanel = oldProtocol.getPanel( deviceUpgrade );
     if ( oldPanel != null )
       removePanel( oldPanel );
     deviceUpgrade.reset();
     deviceUpgrade.load( file );
     Protocol newProtocol = deviceUpgrade.getProtocol();
     if ( newProtocol == null )
       return;
     KMPanel newPanel = newProtocol.getPanel( deviceUpgrade );
     if ( newPanel != null )
       addPanel( newPanel, 1 );
     if (( oldPanel != null ) || ( newPanel != null ))
       tabbedPane.validate();
     refresh();
 
     boolean isRMDU = file.getName().toLowerCase().endsWith( ".rmdu" );
 
     if ( isRMDU )
     {
       setTitle( file.getCanonicalPath() + " - RemoteMaster" );
       updateRecentFiles( file );
     }
     else
       setTitle( "RemoteMaster" );
     preferences.setUpgradePath( file.getParentFile());
   }
 
   private void updateRecentFiles( File file )
   {
     boolean isRMDU = file.getName().toLowerCase().endsWith( ".rmdu" );
 
     if ( isRMDU )
     {
       int i = recentFileMenu.getItemCount() - 1;
       while ( i >= 0 )
       {
         JMenuItem item = recentFileMenu.getItem( i );
         FileAction action = ( FileAction  )item.getAction();
         File f = action.getFile();
         if ( f.getAbsolutePath().equals( file.getAbsolutePath()))
           recentFileMenu.remove( i );
         --i;
       }
       i = recentFileMenu.getItemCount();
       while ( i > 9 )
         recentFileMenu.remove( --i );
       recentFileMenu.add( new JMenuItem( new FileAction( file )), 0 );
 
       recentFileMenu.setEnabled( true );
     }
   }
 
   public void loadUpgrade( BufferedReader reader )
     throws Exception
   {
     Protocol oldProtocol = deviceUpgrade.getProtocol();
     KMPanel oldPanel = oldProtocol.getPanel( deviceUpgrade );
     if ( oldPanel != null )
       removePanel( oldPanel );
     deviceUpgrade.reset();
     deviceUpgrade.load( reader );
     Protocol newProtocol = deviceUpgrade.getProtocol();
     KMPanel newPanel = newProtocol.getPanel( deviceUpgrade );
     if ( newPanel != null )
       addPanel( newPanel, 1 );
     if (( oldPanel != null ) || ( newPanel != null ))
       tabbedPane.validate();
     refresh();
   }
 
   private void refresh()
   {
     String title = "RemoteMaster";
     File file = deviceUpgrade.getFile();
     if ( file != null )
     try
     {
       title = file.getCanonicalPath() + " - RemoteMaster";
     }
     catch ( Exception e )
     {
       e.printStackTrace( System.err );
     }
 
     saveItem.setEnabled( file != null );
     removeListeners();
     description.setText( deviceUpgrade.getDescription());
     String savedTypeName = deviceUpgrade.getDeviceTypeAliasName();
     Remote r = deviceUpgrade.getRemote();
     setRemote( r );
     remoteList.setSelectedItem( r );
     if ( remoteList.getSelectedItem() != r )
     {
       remoteList.addItem( r );
       remoteList.setSelectedItem( r );
     }
     setDeviceTypeName( savedTypeName );
     addListeners();
     currPanel.update();
 
     validateUpgrade();
   }
 
   public void importUpgrade( BufferedReader in )
     throws Exception
   {
     deviceUpgrade.reset();
     deviceUpgrade.importUpgrade( in );
     setTitle( "RemoteMaster" );
     removeListeners();
     description.setText( deviceUpgrade.getDescription());
     String savedTypeName = deviceUpgrade.getDeviceTypeAliasName();
     Remote r = deviceUpgrade.getRemote();
     setRemote( r );
     remoteList.setSelectedItem( r );
     if ( remoteList.getSelectedItem() != r )
     {
       remoteList.addItem( r );
       remoteList.setSelectedItem( r );
     }
     setDeviceTypeName( savedTypeName );
     addListeners();
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
     SwingUtilities.updateComponentTreeUI( currPanel );
     validateUpgrade();
   }
 
   public static File getHomeDirectory()
   {
     return homeDirectory;
   }
 
   public void validateUpgrade()
   {
     Remote r = deviceUpgrade.getRemote();
     Protocol p = deviceUpgrade.getProtocol();
     Vector protocols = protocolManager.getProtocolsForRemote( r );
     if ( !protocols.contains( p ) && !p.hasCode( r ))
     {
       System.err.println( "KeyMapMaster.validateUpgrade(), protocol " + p.getDiagnosticName() +
                           "is not compatible with remote " + r.getName());
 
       // Find a matching protocol for this remote
       Protocol match = null;
       String name = p.getName();
       for ( Enumeration e = protocols.elements(); e.hasMoreElements(); )
       {
         Protocol p2 = ( Protocol )e.nextElement();
         if ( p2.getName().equals( name ))
         {
           match = p2;
           System.err.println( "\tFound one with the same name: " + p2.getDiagnosticName());
           break;
         }
       }
 
       if ( match != null )
       {
         System.err.println( "\tChecking for matching dev. parms" );
         DeviceParameter[] parms = p.getDeviceParameters();
         DeviceParameter[] parms2 = match.getDeviceParameters();
 
         int[] map = new int[ parms.length ];
         boolean parmsMatch = true;
         for ( int i = 0; i < parms.length; i++ )
         {
           name = parms[ i ].getName();
           System.err.print( "\tchecking " + name );
           boolean nameMatch = false;
           for ( int j = 0; j < parms2.length; j++ )
           {
             if ( name.equals( parms2[ j ].getName()))
             {
               map[ i ] = j;
               nameMatch = true;
               System.err.print( " has a match!" );
               break;
             }
           }
           System.err.println();
           parmsMatch = nameMatch;
           if ( !parmsMatch )
             break;
         }
         if ( parmsMatch )
         {
           // copy parameters from p to p2!
           System.err.println( "\tCopying dev. parms" );
           for ( int i = 0; i < map.length; i++ )
           {
             System.err.println( "\tfrom index " + i + " to index " + map[ i ]);
             parms2[ map[ i ]].setValue( parms[ i ].getValue());
           }
           System.err.println();
           System.err.println( "Setting new protocol" );
           p.convertFunctions( deviceUpgrade.getFunctions(), match );
           deviceUpgrade.setProtocol( match );
           return;
         }
       }
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
 
   public static Remote getRemote()
   {
     return me.deviceUpgrade.getRemote();
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
 
   private class KMFileFilter
     extends javax.swing.filechooser.FileFilter
   {
     //Accept all directories and all .km/.rmdu files.
     public boolean accept( File f )
     {
       boolean rc = false;
       if ( f.isDirectory())
         rc = true;
       else
       {
         String lowerName = f.getName().toLowerCase();
         if ( lowerName.endsWith( ".km" ) || lowerName.endsWith( upgradeExtension ))
           rc = true;
       }
       return rc;
     }
 
     //The description of this filter
     public String getDescription()
     {
       return "RemoteMaster device upgrade files";
     }
   }
 
   private class KMDirectoryFilter
     extends javax.swing.filechooser.FileFilter
   {
     //Accept all directories
     public boolean accept( File f )
     {
       boolean rc = false;
       if ( f.isDirectory())
         rc = true;
       return rc;
     }
 
     //The description of this filter
     public String getDescription()
     {
       return "Directories";
     }
   }
 
 
   private class TextFileFilter
     extends javax.swing.filechooser.FileFilter
   {
     //Accept all directories and all .km/.rmdu files.
     public boolean accept( File f )
     {
       boolean rc = false;
       if ( f.isDirectory())
         rc = true;
       else
       {
         String lowerName = f.getName().toLowerCase();
         if ( lowerName.endsWith( ".txt" ))
           rc = true;
       }
       return rc;
     }
 
     //The description of this filter
     public String getDescription()
     {
       return "KeyMapMaster device upgrade files";
     }
   }
 
   private class AnyFileFilter
     extends javax.swing.filechooser.FileFilter
   {
     //Accept all directories and all .km/.rmdu files.
     public boolean accept( File f )
     {
       boolean rc = false;
       if ( f.isDirectory())
         rc = true;
       else
       {
         String lowerName = f.getName().toLowerCase();
         if ( lowerName.endsWith( ".txt" ) || lowerName.endsWith( ".km" ) ||
              lowerName.endsWith( upgradeExtension ))
           rc = true;
       }
       return rc;
     }
 
     //The description of this filter
     public String getDescription()
     {
       return "All device upgrade files";
     }
   }
 }
