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
   private static final String version = "v 0.36";
   private JMenuItem newItem = null;
   private JMenuItem openItem = null;
   private JMenuItem saveItem = null;
   private JMenuItem saveAsItem = null;
   private JMenu recentFileMenu = null;
   private JLabel messageLabel = null;
   private JTextField description = null;
   private JComboBox remoteList = null;
   private JComboBox deviceTypeList = null;
   private Remote[] remotes = null;
   private Vector protocols = new Vector();
   private Remote currentRemote = null;
   private String currentDeviceTypeName = null;
   private SetupPanel setupPanel = null;
   private FunctionPanel functionPanel = null;
   private ExternalFunctionPanel externalFunctionPanel = null;
   private ButtonPanel buttonPanel = null;
   private OutputPanel outputPanel = null;
   private ProgressMonitor progressMonitor = null;
   private DeviceUpgrade deviceUpgrade = null;
   private File propertiesFile = null;
   private File rdfPath = null;
   private File kmPath = null;
   private String lastRemoteName = null;
   private String lastRemoteSignature = null;
 
   public KeyMapMaster()
     throws Exception
   {
     this( null );
   }
 
   public KeyMapMaster( File propertiesFile )
     throws Exception
   {
     super( "KeyMap Master " + version );
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
 
     this.propertiesFile = propertiesFile;
 
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
     recentFileMenu = new JMenu( "Recent" );
     menu.add( recentFileMenu );
 
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
 
     setupPanel = new SetupPanel( deviceUpgrade, protocols );
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
 
     loadPreferences();
 
     loadProtocols();
     deviceUpgrade.setProtocol(( Protocol )protocols.firstElement());
     setupPanel.protocolsLoaded( protocols );
 
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
     setRemote( remotes[ index ]);
     remoteList.setSelectedIndex( index );
 
     remoteList.addActionListener( this );
     deviceTypeList.addActionListener( this );
     tabbedPane.addChangeListener( this );
 
     currPanel.update();
 
     clearMessage();
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
   }
 
   private void loadProtocols()
     throws Exception
   {
     File f = new File( "protocols.ini" );
     while ( !f.canRead() )
     {
       JOptionPane.showMessageDialog( this, "Couldn't read " + f.getName() + "!",
                                      "Error", JOptionPane.ERROR_MESSAGE );
       JFileChooser chooser = new JFileChooser( System.getProperty( "user.dir" ));
       chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
       chooser.setDialogTitle( "Pick the file containing the protocol definitions" );
       int returnVal = chooser.showOpenDialog( this );
       if ( returnVal != JFileChooser.APPROVE_OPTION )
         System.exit( -1 );
       else
         f = chooser.getSelectedFile();
     }
     BufferedReader rdr = new BufferedReader( new FileReader( f ));
     Properties props = null;
     String name = null;
     Hex id = null;
     String type = null;
 
     while ( true )
     {
       String line = rdr.readLine();
       if ( line == null )
         break;
 
       if (( line.length() == 0 ) || ( line.charAt( 0 ) == '#' ))
         continue;
 
       while ( line.endsWith( "\\" ))
         line = line.substring(0, line.length() - 1 ) + rdr.readLine().trim();
 
       if ( line.charAt( 0 ) == '[' ) // begin new protocol
       {
         if ( name != null  )
         {
           Protocol protocol =
             ProtocolFactory.createProtocol( name, id, type, props );
           if ( protocol != null )
             protocols.add( protocol );
         }
         name = line.substring( 1, line.length() - 1 ).trim();
         props = new Properties();
         id = null;
         type = "Protocol";
       }
       else
       {
         StringTokenizer st = new StringTokenizer( line, "=", true );
         String parmName = st.nextToken().trim();
         String parmValue = null;
         st.nextToken(); // skip the =
         if ( !st.hasMoreTokens() )
           continue;
         else
           parmValue = st.nextToken( "" ).trim();
 
         if ( parmName.equals( "PID" ))
         {
           id = new Hex( parmValue );
         }
         else if ( parmName.equals( "Type" ))
         {
           type = parmValue;
         }
         else
         {
           props.setProperty( parmName, parmValue );
         }
       }
     }
     rdr.close();
     protocols.add( ProtocolFactory.createProtocol( name, id, type, props ));
 
     if ( protocols.size() == 0 )
     {
       JOptionPane.showMessageDialog( this, "No protocols were loaded!",
                                      "Error", JOptionPane.ERROR_MESSAGE );
       System.exit( -1 );
     }
 
     clearMessage();
   }
 
   public void setRemotes( Remote[] remotes )
   {
     if ( remoteList != null )
       remoteList.setModel( new DefaultComboBoxModel( remotes ));
   }
 
   public void setRemote( Remote remote )
   {
     System.err.println( "KeyMapMaster.setRemote( " + remote.getName() + " )" );
     System.err.println( "\tremoteList=" + remoteList + " and currentRemote=" + currentRemote );
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
         // add code to try to match the current device type to a
         // type in the new type list.
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
         deviceUpgrade.reset( remotes, protocols );
         setTitle( "KeyMapMaster " + version );
         description.setText( null );
         remoteList.setSelectedItem( deviceUpgrade.getRemote());
         deviceTypeList.setSelectedItem( deviceUpgrade.getDeviceTypeAliasName());
         saveItem.setEnabled( false );
         currPanel.update();
       }
       else if ( source == saveItem )
       {
         deviceUpgrade.store();
       }
       else if ( source == saveAsItem )
       {
         saveAs();
       }
       else if ( source == openItem )
       {
         if ( !promptToSaveUpgrade())
           return;
         JFileChooser chooser = new JFileChooser( kmPath );
         chooser.setFileFilter( new KMFileFilter());
         int returnVal = chooser.showOpenDialog( this );
         if ( returnVal == JFileChooser.APPROVE_OPTION )
         {
           File file = chooser.getSelectedFile();
           String name = file.getAbsolutePath();
           if ( !name.endsWith( ".km" ))
             file = new File( name + ".km" );
 
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
   }
 
   public void saveAs()
     throws IOException
   {
     JFileChooser chooser = new JFileChooser( kmPath );
     chooser.setFileFilter( new KMFileFilter());
     File f = deviceUpgrade.getFile();
     if ( f != null )
       chooser.setSelectedFile( f );
     int returnVal = chooser.showSaveDialog( this );
     if ( returnVal == JFileChooser.APPROVE_OPTION )
     {
       String name = chooser.getSelectedFile().getAbsolutePath();
       if ( !name.toLowerCase().endsWith( ".km" ))
         name = name + ".km";
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
         setTitle( "KeyMapMaster " + version + ": " + file.getName());
       }
     }
   }
 
   public boolean promptToSaveUpgrade()
     throws IOException
   {
     int rc = JOptionPane.showConfirmDialog( this,
 //                                            "All changes made to the current upgrade will be lost if you proceed.\n\n" + 
                                            "Do you want to save the currect upgrade before proceeding?",
                                             "Save upgrade?",
                                             JOptionPane.YES_NO_CANCEL_OPTION );
     System.err.println( "KeyMapMaster.promptToSaveUpgrade(), rc=" + rc );
     if (( rc == JOptionPane.CANCEL_OPTION ) || ( rc == JOptionPane.CLOSED_OPTION ))
       return false;
     if ( rc == JOptionPane.NO_OPTION )
       return true;
 
     saveAs();
     return true;                                            
   }
 
   public void openFile( File file )
     throws Exception
   {
     kmPath = file.getParentFile();
     deviceUpgrade.reset( remotes, protocols );
     deviceUpgrade.load( file, remotes, protocols );
     setTitle( "KeyMapMaster " + version + ": " + file.getName());
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
   }
   // ChangeListener methods
   private KMPanel currPanel = null;
   public void stateChanged( ChangeEvent e )
   {
     currPanel.commit();
     currPanel = ( KMPanel )(( JTabbedPane )e.getSource()).getSelectedComponent();
     currPanel.update();
   }
 
   private void loadPreferences()
     throws Exception
   {
     Properties props = new Properties();
     
     File userDir = new File( System.getProperty( "user.dir" ));
     System.err.println( "userDir is " + userDir.getAbsolutePath());
     if ( propertiesFile == null )
     {
       File temp = File.createTempFile( "kmj", null, userDir );
       System.err.println( "Created temp file " + temp.getName());
       File dir = null;
       if ( temp.canWrite())
       {
         System.err.println( "Can write" );
         temp.delete();
         dir = userDir;
       }
       else
       {
         System.err.println( "Can't write" );
         dir = new File( System.getProperty( "user.home" )); 
       }
 
       propertiesFile = new File( dir, "KeyMapMaster.properties" );
       System.err.println( "propertiesFIle is " + propertiesFile.getAbsolutePath());
     }
 
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
       rdfPath = new File( userDir, "rdf" );
 
     temp = props.getProperty( "KMPath" );
     if ( temp != null )
       kmPath = new File( temp );
     else
       kmPath = new File( userDir, "km" );
 
     temp = props.getProperty( "LookAndFeel" );
     if ( temp != null )
       UIManager.setLookAndFeel( temp );
 
     System.err.print( "Reading Remote.name:" );
     lastRemoteName = props.getProperty( "Remote.name" );
     System.err.println( "for " + lastRemoteName );
     lastRemoteSignature = props.getProperty( "Remote.signature" );
 
     for (int i = 0; i < 10; i++ )
     {
       temp = props.getProperty( "RecentFiles." + i );
       if ( temp == null )
         break;
       recentFileMenu.add( new FileAction( new File( temp )));
     }
     
     temp = props.getProperty( "Bounds" );
     if ( temp != null )
     {
       Rectangle r = new Rectangle();
       StringTokenizer st = new StringTokenizer( temp, "," );
       r.x = Integer.parseInt( st.nextToken());
       r.y = Integer.parseInt( st.nextToken());
       r.width = Integer.parseInt( st.nextToken());
       r.height = Integer.parseInt( st.nextToken());
       setBounds( r );
     }
     else
       pack();
 
     show();
   }
 
   private void savePreferences()
     throws Exception
   {
     Properties props = new Properties();
     props.setProperty( "RDFPath", rdfPath.getAbsolutePath());
     props.setProperty( "KMPath", kmPath.getAbsolutePath());
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
 
   public static void main( String[] args )
   {
     try
     {
       UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
       for ( int i = 0; i < info.length; i++ )
       {
         if ( info[ i ].getName().equals( "Windows" ))
         {
           UIManager.setLookAndFeel( info[ i ].getClassName());
           break;
         }
       }
       System.setErr( new PrintStream( new FileOutputStream( "km.err" )));
       KeyMapMaster km = null;
       if ( args.length > 0 )
         km = new KeyMapMaster( new File( args[ 0 ]));
       else
         km = new KeyMapMaster();
     }
     catch ( Exception e )
     {
       System.err.println( "Caught exception in KeyMapMaster.main()!" );
       e.printStackTrace( System.err );
     }
     System.err.flush();
   }
 
   private class KMFileFilter
     extends javax.swing.filechooser.FileFilter
   {
     //Accept all directories and all .km files.
     public boolean accept( File f )
     {
       boolean rc = false;
       if ( f.isDirectory())
         rc = true;
       else if ( f.getName().toLowerCase().endsWith( ".km" ))
         rc = true;
       return rc;
     }
 
     //The description of this filter
     public String getDescription()
     {
       return "KeyMapMaster files (*.km)";
     }
   }
 
   private class KMDirectoryFilter
     extends javax.swing.filechooser.FileFilter
   {
     //Accept all directories and all .km files.
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
 }
