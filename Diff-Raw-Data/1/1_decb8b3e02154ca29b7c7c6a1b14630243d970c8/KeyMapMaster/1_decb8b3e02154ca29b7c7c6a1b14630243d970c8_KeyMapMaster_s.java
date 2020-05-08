 package com.hifiremote.jp1;
 
 import info.clearthought.layout.TableLayout;
 import javax.swing.*;
 import javax.swing.filechooser.*;
 import javax.swing.event.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 import java.io.*;
 
 public class KeyMapMaster
  extends JFrame
  implements ActionListener, ChangeListener, DocumentListener
 {
   private static KeyMapMaster me = null;
   private static final String version = "v 0.60";
   private JMenuItem newItem = null;
   private JMenuItem openItem = null;
   private JMenuItem saveItem = null;
   private JMenuItem saveAsItem = null;
   private JMenuItem importItem = null;
   private JMenu recentFileMenu = null;
   private JLabel messageLabel = null;
   private JTextField description = null;
   private JComboBox remoteList = null;
   private JComboBox deviceTypeList = null;
   private Remote[] remotes = null;
   private ProtocolManager protocolManager = new ProtocolManager();
   private Remote currentRemote = null;
   private String currentDeviceTypeName = null;
   private SetupPanel setupPanel = null;
   private FunctionPanel functionPanel = null;
   private ExternalFunctionPanel externalFunctionPanel = null;
   private ButtonPanel buttonPanel = null;
   private OutputPanel outputPanel = null;
   private LayoutPanel layoutPanel = null;
   private ProgressMonitor progressMonitor = null;
   private DeviceUpgrade deviceUpgrade = null;
   private static File homeDirectory = null;
   private File propertiesFile = null;
   private File rdfPath = null;
   private File upgradePath = null;
   private String lastRemoteName = null;
   private String lastRemoteSignature = null;
   private Rectangle bounds = null;
   private Vector recentFiles = new Vector();
   private static String upgradeExtension = ".rmdu";
   private static String upgradeDirectory = "Upgrades";
 
   public KeyMapMaster( String[] args )
     throws Exception
   {
     super( "RemoteMaster " + version );
     File fileToOpen = parseArgs( args );
 
     loadPreferences();
 
     setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
     setDefaultLookAndFeelDecorated( true );
     JDialog.setDefaultLookAndFeelDecorated( true );
     JFrame.setDefaultLookAndFeelDecorated( true );
     Toolkit.getDefaultToolkit().setDynamicLayout( true );
     me = this;
 
     addWindowListener( new WindowAdapter()
     {
       public void windowClosing( WindowEvent event )
       {
         try
         {
           if ( !promptToSaveUpgrade())
             return;
           savePreferences();
         }
         catch ( Exception e )
         {
           System.err.println( "KeyMapMaster.windowClosing() caught an exception!" );
           e.printStackTrace( System.out );
         }
         System.exit( 0 );
       }
     });
 
     deviceUpgrade = new DeviceUpgrade();
 
     JMenuBar menuBar = new JMenuBar();
     setJMenuBar( menuBar );
     JMenu menu = new JMenu( "File" );
     menuBar.add( menu );
     newItem = new JMenuItem( "New" );
     newItem.addActionListener( this );
     menu.add( newItem );
     openItem = new JMenuItem( "Open..." );
     openItem.addActionListener( this );
     menu.add( openItem );
     saveItem = new JMenuItem( "Save" );
     saveItem.setEnabled( false );
     saveItem.addActionListener( this );
     menu.add( saveItem );
     saveAsItem = new JMenuItem( "Save as..." );
     saveAsItem.addActionListener( this );
     menu.add( saveAsItem );
 
     menu.addSeparator();
     importItem = new JMenuItem( "Import KM file..." );
     importItem.addActionListener( this );
     menu.add( importItem );
 
     menu.addSeparator();
     recentFileMenu = new JMenu( "Recent" );
     menu.add( recentFileMenu );
     for ( Enumeration e = recentFiles.elements(); e.hasMoreElements(); )
       recentFileMenu.add( new FileAction(( File )e.nextElement()));
 
     menu = new JMenu( "Look and Feel" );
     menuBar.add( menu );
 
     ButtonGroup group = new ButtonGroup();
     String lookAndFeelName = UIManager.getLookAndFeel().getClass().getName();
     UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
     for ( int i = 0; i < info.length; i++ )
     {
       JRadioButtonMenuItem item = new JRadioButtonMenuItem( info[ i ].getName());
       item.setActionCommand( info[ i ].getClassName());
       if ( info[ i ].getClassName().equals( lookAndFeelName ))
         item.setSelected( true );
       group.add( item );
       menu.add( item );
       item.addActionListener( this );
     }
 
     Container mainPanel = getContentPane();
     JTabbedPane tabbedPane = new JTabbedPane();
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
     label.setLabelFor( description );
     description.getDocument().addDocumentListener( this );
     panel.add( description, "3, 1, 7, 1" );
 
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
     String[] aliasNames = deviceUpgrade.getDeviceTypeAliasNames();
     deviceTypeList = new JComboBox( aliasNames );
     deviceTypeList.setMaximumRowCount( aliasNames.length );
     label.setLabelFor( deviceTypeList );
     deviceTypeList.setPrototypeDisplayValue( "A Device Type" );
     deviceTypeList.setToolTipText( "Choose the device type for the upgrade being created." );
     panel.add( deviceTypeList, "7, 3" );
 
     mainPanel.add( panel, BorderLayout.NORTH );
 
     messageLabel = new JLabel( " " );
     messageLabel.setForeground( Color.red );
 
     mainPanel.add( messageLabel, BorderLayout.SOUTH );
 
     protocolManager.load( new File( homeDirectory, "protocols.ini" ));
 
     setupPanel = new SetupPanel( deviceUpgrade, protocolManager );
     currPanel = setupPanel;
     tabbedPane.addTab( "Setup", null, setupPanel, "Enter general information about the upgrade." );
 
     functionPanel = new FunctionPanel( deviceUpgrade );
     tabbedPane.addTab( "Functions", null, functionPanel,
                        "Define function names and parameters." );
 
     externalFunctionPanel = new ExternalFunctionPanel( deviceUpgrade );
     tabbedPane.addTab( "External Functions", null, externalFunctionPanel,
                        "Define functions from other device codes." );
 
     buttonPanel = new ButtonPanel( deviceUpgrade );
     tabbedPane.addTab( "Buttons", null, buttonPanel,
                        "Assign functions to buttons." );
 
     outputPanel = new OutputPanel( deviceUpgrade );
     tabbedPane.addTab( "Output", null, outputPanel,
                        "The output to copy-n-paste into IR." );
 
     layoutPanel = new LayoutPanel( deviceUpgrade );
     tabbedPane.addTab( "Layout", null, layoutPanel ,
                        "Button Layout information." );
 
     loadRemotes();
     setRemotes( remotes );
 
     System.err.println( "Setting default remote" );
     int index = 0;
     if ( lastRemoteName != null )
     {
       System.err.print( "Searcing for " + lastRemoteName );
       index = Arrays.binarySearch( remotes, lastRemoteName );
       System.err.println( " index is " + index );
     }
     if ( index < 0 )
       index = 0;
 
     Remote temp = remotes[ index ];
     String firstName = ( String )protocolManager.getNames().firstElement();
     Protocol protocol = protocolManager.findProtocolForRemote( temp, firstName );
     deviceUpgrade.setProtocol( protocol );
     setRemote( temp );
     remoteList.setSelectedIndex( index );
 
     remoteList.addActionListener( this );
     deviceTypeList.addActionListener( this );
     tabbedPane.addChangeListener( this );
 
     currPanel.update();
 
     clearMessage();
 
     if ( bounds != null )
       setBounds( bounds );
     else
       pack();
 
     if ( fileToOpen != null )
     {
       openFile( fileToOpen );
     }
     show();
 
     deviceUpgrade.setChanged( false );
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
 
   private void loadRemotes()
     throws Exception
   {
     File[] files = new File[ 0 ];
     File dir = rdfPath;
     FilenameFilter filter = new FilenameFilter()
     {
       public boolean accept( File dir, String name )
       {
         return name.toLowerCase().endsWith( ".rdf" );
       }
     };
 
     while ( files.length == 0 )
     {
       files = dir.listFiles( filter );
       if ( files.length == 0 )
       {
         JOptionPane.showMessageDialog( this, "No RDF files were found!",
                                        "Error", JOptionPane.ERROR_MESSAGE );
         JFileChooser chooser = new JFileChooser( dir );
         chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
         chooser.setFileFilter( new KMDirectoryFilter());
         chooser.setDialogTitle( "Choose the directory containing the RDFs" );
         int returnVal = chooser.showOpenDialog( this );
         if ( returnVal != JFileChooser.APPROVE_OPTION )
           System.exit( -1 );
         else
           dir = chooser.getSelectedFile();
       }
     }
     rdfPath = dir;
 
     progressMonitor = new ProgressMonitor( this, "Loading remotes",
                                            "", 0, files.length );
     progressMonitor.setProgress( 0 );
     progressMonitor.setMillisToDecideToPopup( 1000 );
 
     remotes = new Remote[ files.length ];
     for ( int i = 0; i < files.length; i++ )
     {
       File rdf = files[ i ];
       progressMonitor.setNote( "Loading " + rdf.getName());
       remotes[ i ] = new Remote( rdf );
       progressMonitor.setProgress( i );
     }
 
     progressMonitor.setNote( "Sorting remotes" );
     Arrays.sort( remotes );
     progressMonitor.setProgress( files.length );
     progressMonitor.close();
   } // loadRemotes
 
   public void setRemotes( Remote[] remotes )
   {
     if ( remoteList != null )
       remoteList.setModel( new DefaultComboBoxModel( remotes ));
   }
 
   public void setRemote( Remote remote )
   {
     if (( remoteList != null ) && ( remote != currentRemote ))
     {
       currentRemote = remote;
       deviceUpgrade.setRemote( remote );
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
         validateUpgrade();
         currPanel.update();
       }
       else if ( source == deviceTypeList )
       {
         String typeName = ( String )deviceTypeList.getSelectedItem();
         setDeviceTypeName( typeName );
         currPanel.update();
       }
       else if ( source == newItem )
       {
         if ( !promptToSaveUpgrade())
           return;
         deviceUpgrade.reset( remotes, protocolManager );
         setTitle( "RemoteMapMaster " + version );
         description.setText( null );
         remoteList.setSelectedItem( deviceUpgrade.getRemote());
         deviceTypeList.setSelectedItem( deviceUpgrade.getDeviceTypeAliasName());
         saveItem.setEnabled( false );
         currPanel.update();
         deviceUpgrade.setChanged( false );
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
         if ( !promptToSaveUpgrade())
           return;
         JFileChooser chooser = new JFileChooser( upgradePath );
         chooser.setFileFilter( new KMFileFilter());
         int returnVal = chooser.showOpenDialog( this );
         if ( returnVal == JFileChooser.APPROVE_OPTION )
         {
           File file = chooser.getSelectedFile();
           String name = file.getAbsolutePath();
           if ( !name.endsWith( upgradeExtension ) && !name.endsWith( ".km" ))
             file = new File( name + upgradeExtension );
 
           int rc = JOptionPane.YES_OPTION;
           if ( !file.exists())
           {
             JOptionPane.showMessageDialog( this,
                                            file.getName() + " doesn't exist exists.",
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
             openFile( file );
           }
         }
       }
       else if ( source == importItem )
       {
         if ( !promptToSaveUpgrade())
           return;
         JFileChooser chooser = new JFileChooser( upgradePath );
         chooser.setFileFilter( new TextFileFilter());
         int returnVal = chooser.showOpenDialog( this );
         if ( returnVal == JFileChooser.APPROVE_OPTION )
         {
           File file = chooser.getSelectedFile();
           String name = file.getAbsolutePath();
           if ( !name.endsWith( ".txt" ))
             file = new File( name + ".txt" );
 
           int rc = JOptionPane.YES_OPTION;
           if ( !file.exists())
           {
             JOptionPane.showMessageDialog( this,
                                            file.getName() + " doesn't exist exists.",
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
             importFile( file );
           }
         }
       }
       else if ( source.getClass() == JRadioButtonMenuItem.class )
       {
         UIManager.setLookAndFeel((( JRadioButtonMenuItem )source ).getActionCommand());
         SwingUtilities.updateComponentTreeUI( this );
       }
     }
     catch ( Exception ex )
     {
       ex.printStackTrace( System.err );
     }
   } // actionPerformed
 
   public void saveAs()
     throws IOException
   {
     JFileChooser chooser = new JFileChooser( upgradePath );
     chooser.setFileFilter( new KMFileFilter());
     File f = deviceUpgrade.getFile();
     if ( f != null )
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
         saveItem.setEnabled( true );
         setTitle( "RemoteMaster " + version + ": " + file.getName());
       }
     }
   }
 
   public boolean promptToSaveUpgrade()
     throws IOException
   {
     if ( !deviceUpgrade.hasChanged())
       return true;
 
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
 
   public void openFile( File file )
     throws Exception
   {
     upgradePath = file.getParentFile();
     deviceUpgrade.reset( remotes, protocolManager );
     deviceUpgrade.load( file, remotes, protocolManager );
     setTitle( "RemoteMaster " + version + ": " + file.getName());
     description.setText( deviceUpgrade.getDescription());
     saveItem.setEnabled( true );
     remoteList.removeActionListener( this );
     deviceTypeList.removeActionListener( this );
     String savedTypeName = deviceUpgrade.getDeviceTypeAliasName();
     setRemote( deviceUpgrade.getRemote());
     remoteList.setSelectedItem( deviceUpgrade.getRemote());
     setDeviceTypeName( savedTypeName );
     remoteList.addActionListener( this );
     deviceTypeList.addActionListener( this );
     currPanel.update();
 
     int itemCount = recentFileMenu.getItemCount();
     for ( int i = 0; i < itemCount; i++ )
     {
       JMenuItem item = recentFileMenu.getItem( i );
       FileAction action = ( FileAction  )item.getAction();
       File f = action.getFile();
       if ( f.equals( file ))
       {
         recentFileMenu.remove( i );
         itemCount--;
       }
     }
     while ( itemCount > 9 )
       recentFileMenu.remove( --itemCount );
     recentFileMenu.add( new JMenuItem( new FileAction( file )), 0 );
 
     validateUpgrade();
     deviceUpgrade.setChanged( false );
   }
 
   public void importFile( File file )
     throws Exception
   {
     deviceUpgrade.reset( remotes, protocolManager );
     deviceUpgrade.importFile( file, remotes, protocolManager );
     setTitle( "RemoteMaster " + version );
     description.setText( deviceUpgrade.getDescription());
     remoteList.removeActionListener( this );
     deviceTypeList.removeActionListener( this );
     String savedTypeName = deviceUpgrade.getDeviceTypeAliasName();
     setRemote( deviceUpgrade.getRemote());
     remoteList.setSelectedItem( deviceUpgrade.getRemote());
     setDeviceTypeName( savedTypeName );
     remoteList.addActionListener( this );
     deviceTypeList.addActionListener( this );
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
 
   private void loadPreferences()
     throws Exception
   {
     Properties props = new Properties();
 
 //  File userDir = new File( System.getProperty( "user.dir" ));
 //  System.err.println( "userDir is " + userDir.getAbsolutePath());
 //  if ( propertiesFile == null )
 //  {
 //    File temp = File.createTempFile( "kmj", null, userDir );
 //    System.err.println( "Created temp file " + temp.getName());
 //    File dir = null;
 //    if ( temp.canWrite())
 //    {
 //      System.err.println( "Can write" );
 //      temp.delete();
 //      dir = userDir;
 //    }
 //    else
 //    {
 //      System.err.println( "Can't write" );
 //      dir = new File( System.getProperty( "user.home" ));
 //    }
 //
 //    propertiesFile = new File( dir, "RemoteMaster.properties" );
 //    System.err.println( "propertiesFIle is " + propertiesFile.getAbsolutePath());
 //  }
 //
     if ( propertiesFile.canRead())
     {
       FileInputStream in = new FileInputStream( propertiesFile );
       props.load( in );
       in.close();
     }
 
     String temp = props.getProperty( "RDFPath" );
     if ( temp != null )
       rdfPath = new File( temp );
     else
     {
       rdfPath = new File( homeDirectory, "rdf" );
       while ( !rdfPath.exists())
         rdfPath = rdfPath.getParentFile();
     }
 
     temp = props.getProperty( "UpgradePath" );
     if ( temp == null )
       temp = props.getProperty( "KMPath" );
     if ( temp != null )
       upgradePath = new File( temp );
     else
     {
       upgradePath = new File( homeDirectory, upgradeDirectory );
       while ( ! upgradePath.exists())
         upgradePath = upgradePath.getParentFile();
     }
 
     String defaultLookAndFeel = UIManager.getSystemLookAndFeelClassName();
     temp = props.getProperty( "LookAndFeel", defaultLookAndFeel );
     try
     {
       UIManager.setLookAndFeel( temp );
       SwingUtilities.updateComponentTreeUI( this );
     }
     catch ( Exception e )
     {
       System.err.println( "Exception thrown when setting look and feel to " + temp );
     }
 
     lastRemoteName = props.getProperty( "Remote.name" );
     lastRemoteSignature = props.getProperty( "Remote.signature" );
 
     for (int i = 0; i < 10; i++ )
     {
       temp = props.getProperty( "RecentFiles." + i );
       if ( temp == null )
         break;
       recentFiles.add( new File( temp ));
     }
 
     temp = props.getProperty( "Bounds" );
     if ( temp != null )
     {
       bounds = new Rectangle();
       StringTokenizer st = new StringTokenizer( temp, "," );
       bounds.x = Integer.parseInt( st.nextToken());
       bounds.y = Integer.parseInt( st.nextToken());
       bounds.width = Integer.parseInt( st.nextToken());
       bounds.height = Integer.parseInt( st.nextToken());
     }
   }
 
   private void savePreferences()
     throws Exception
   {
     Properties props = new Properties();
     props.setProperty( "RDFPath", rdfPath.getAbsolutePath());
     props.setProperty( "UpgradePath", upgradePath.getAbsolutePath());
     props.setProperty( "LookAndFeel", UIManager.getLookAndFeel().getClass().getName());
     Remote remote = deviceUpgrade.getRemote();
     props.setProperty( "Remote.name", remote.getName());
     props.setProperty( "Remote.signature", remote.getSignature());
 
     for ( int i = 0; i < recentFileMenu.getItemCount(); i++ )
     {
       JMenuItem item = recentFileMenu.getItem( i );
       FileAction action = ( FileAction )item.getAction();
       File f = action.getFile();
       props.setProperty( "RecentFiles." + i, f.getAbsolutePath());
     }
 
     int state = getExtendedState();
     if ( state != Frame.NORMAL )
       setExtendedState( Frame.NORMAL );
     Rectangle bounds = getBounds();
     props.setProperty( "Bounds", "" + bounds.x + ',' + bounds.y + ',' + bounds.width + ',' + bounds.height );
 
     FileOutputStream out = new FileOutputStream( propertiesFile );
     props.store( out, null );
     out.flush();
     out.close();
   }
 
   public void validateUpgrade()
   {
     Remote r = deviceUpgrade.getRemote();
     Protocol p = deviceUpgrade.getProtocol();
     Vector protocols = protocolManager.getProtocolsForRemote( r );
     if ( !protocols.contains( p ))
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
 
   // DocumentListener methods
   public void changedUpdate( DocumentEvent e )
   {
     deviceUpgrade.setDescription( description.getText());
   }
 
   public void insertUpdate( DocumentEvent e )
   {
     deviceUpgrade.setDescription( description.getText());
   }
 
   public void removeUpdate( DocumentEvent e )
   {
     deviceUpgrade.setDescription( description.getText());
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
 
   private class FileAction
     extends AbstractAction
   {
     private File file = null;
     public FileAction( File file )
     {
       super( file.getAbsolutePath());
       this.file = file;
     }
 
     public void actionPerformed( ActionEvent e )
     {
       try
       {
         if ( promptToSaveUpgrade())
           openFile( file );
       }
       catch ( Exception ex )
       {
         ex.printStackTrace( System.err );
       }
     }
 
     public File getFile()
     {
       return file;
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
 }
