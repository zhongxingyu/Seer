 package de.dmarcini.submatix.pclogger.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.io.File;
 import java.io.IOException;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Locale;
 import java.util.MissingResourceException;
 import java.util.Properties;
 import java.util.ResourceBundle;
 import java.util.Vector;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.FileHandler;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JSpinner;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.apache.commons.cli.BasicParser;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import de.dmarcini.submatix.pclogger.comm.BTCommunication;
 import de.dmarcini.submatix.pclogger.res.ProjectConst;
 import de.dmarcini.submatix.pclogger.utils.ConfigReadWriteException;
 import de.dmarcini.submatix.pclogger.utils.DeviceComboBoxModel;
 import de.dmarcini.submatix.pclogger.utils.DirksConsoleLogFormatter;
 import de.dmarcini.submatix.pclogger.utils.LogDerbyDatabaseUtil;
 import de.dmarcini.submatix.pclogger.utils.ReadConfig;
 import de.dmarcini.submatix.pclogger.utils.SPX42Config;
 import de.dmarcini.submatix.pclogger.utils.SPX42GasList;
 import de.dmarcini.submatix.pclogger.utils.SpxPcloggerProgramConfig;
 import de.dmarcini.submatix.pclogger.utils.WriteConfig;
 
 //@formatter:off
 /**
  * @author dmarc
  *
  */
 public class MainCommGUI extends JFrame implements ActionListener, MouseMotionListener, ChangeListener, ItemListener
 {  //
   private enum programTabs {
     TAB_CONNECT,
     TAB_CONFIG,
     TAB_GASLIST,
     TAB_GASPRESET,
     TAB_LOGREAD,
     TAB_LOGGRAPH,
     TAB_FILEMANAGER
   }
   private static final long        serialVersionUID    = 3L;
   private final static int         VERY_CONSERVATIVE   = 0;
   private final static int         CONSERVATIVE        = 1;
   private final static int         MODERATE            = 2;
   private final static int         AGGRESSIVE          = 3;
   private final static int         VERY_AGGRESSIVE     = 4;                                            ;
   private int                      licenseState        = -1;
   private int                      customConfig        = -1;
   private LogDerbyDatabaseUtil     databaseUtil        = null;
   private int                      waitForMessage      = 0;
   //
   // @formatter:on
   private JFrame                   frmMainwindowtitle;
   private JTabbedPane              tabbedPane;
   private spx42ConnectPanel        connectionPanel;
   private spx42ConfigPanel         configPanel;
   private spx42GaslistEditPanel    gasConfigPanel;
   private spx42GasPresetEditPanel  gasPresetPanel;
   private spx42LoglistPanel        logListPanel;
   private spx42LogGraphPanel       logGraphPanel;
   private spx42FileManagerPanel    fileManagerPanel;
   private JMenuItem                mntmExit;
   private JMenu                    mnLanguages;
   private JMenu                    mnFile;
   private JMenu                    mnOptions;
   private JMenu                    mnHelp;
   private JMenuItem                mntmHelp;
   private JMenuItem                mntmInfo;
   private JTextField               statusTextField;
   private JMenuItem                mntmOptions;
   // private final static int CUSTOMIZED = 5;
   private static ResourceBundle    stringsBundle       = null;
   private Locale                   programLocale       = null;
   private String                   timeFormatterString = "yyyy-MM-dd - hh:mm:ss";
   @SuppressWarnings( "unused" )
   private final File               programDir          = new File( System.getProperty( "user.dir" ) );
   static Logger                    LOGGER              = null;
   static Handler                   fHandler            = null;
   static Handler                   cHandler            = null;
   private BTCommunication          btComm              = null;
   private final ArrayList<String>  messagesList        = new ArrayList<String>();
   private final SPX42Config        currentConfig       = new SPX42Config();
   private SPX42Config              savedConfig         = null;
   private SPX42GasList             currGasList         = null;
   private SpxPcloggerProgramConfig progConfig          = null;
   private PleaseWaitDialog         wDial               = null;
   private boolean                  ignoreAction        = false;
   private static Level             optionLogLevel      = Level.FINE;
   private static boolean           readBtCacheOnStart  = false;
   private static File              logFile             = null;
   private static File              databaseDir         = null;
   private static boolean           DEBUG               = false;
   private static final Pattern     fieldPatternDp      = Pattern.compile( ":" );
   private static final Pattern     fieldPatternUnderln = Pattern.compile( "[_.]" );
 
   /**
    * Launch the application.
    * 
    * @param args
    */
   public static void main( String[] args )
   {
     CommandLine cmd = null;
     //
     // Kommandozeilenargumente parsen
     //
     if( null == ( cmd = parseCliOptions( args ) ) )
     {
       System.err.println( "Error while scanning CLI-Args...." );
       System.exit( -1 );
     }
     if( cmd.hasOption( "loglevel" ) )
     {
       optionLogLevel = parseLogLevel( cmd.getOptionValue( "loglevel" ) );
     }
     if( cmd.hasOption( "cacheonstart" ) )
     {
       readBtCacheOnStart = true;
     }
     if( cmd.hasOption( "logfile" ) )
     {
       logFile = parseNewLogFile( cmd.getOptionValue( "logfile" ) );
     }
     if( cmd.hasOption( "debug" ) )
     {
       DEBUG = true;
     }
     if( cmd.hasOption( "databasedir" ) )
     {
       databaseDir = parseNewDatabaseDir( cmd.getOptionValue( "databasedir" ) );
     }
     //
     // Style bestimmen, wenn möglich
     //
     EventQueue.invokeLater( new Runnable() {
       @Override
       public void run()
       {
         try
         {
           UIManager.setLookAndFeel( UIManager.getLookAndFeel() );
           // Set cross-platform Java L&F (also called "Metal")
           UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() );
         }
         catch( UnsupportedLookAndFeelException ex )
         {
           System.out.print( "fallback to standart look an feel.." );
         }
         catch( ClassNotFoundException ex )
         {
           System.out.print( "fallback to standart look an feel.." );
         }
         catch( InstantiationException ex )
         {
           System.out.print( "fallback to standart look an feel.." );
         }
         catch( IllegalAccessException ex )
         {
           System.out.print( "fallback to standart look an feel.." );
         }
         try
         {
           //
           // das Mainobjekt erzeugen
           //
           MainCommGUI window = new MainCommGUI();
           window.frmMainwindowtitle.setVisible( true );
         }
         catch( Exception e )
         {
           System.err.println( "Exception: " + e.getLocalizedMessage() + "\n" );
           e.printStackTrace();
         }
       }
     } );
   }
 
   /**
    * CLI-Optionen einlesen Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 28.01.2012
    * @param args
    * @return
    */
   private static CommandLine parseCliOptions( String[] args )
   {
     // Optionenobjet anlegen
     Options options = new Options();
     //
     // Optionen für das Parsing anlegen und zu den Optionen zufügen
     //
     // Logleven festlegen
     Option optLogLevel = new Option( "l", "loglevel", true, "set loglevel for program" );
     options.addOption( optLogLevel );
     // Bluethooth Caching Abfrage
     Option optBtCaching = new Option( "c", "cacheonstart", false, "read cached bt devices on start" );
     options.addOption( optBtCaching );
     // Logfile abgefragt?
     Option optLogFile = new Option( "f", "logfile", true, "set logfile, \"OFF\" set NO logfile" );
     options.addOption( optLogFile );
     // Debugging aktivieren
     Option optDebug = new Option( "d", "debug", false, "set debugging for a lot of GUI effects" );
     options.addOption( optDebug );
     // Daternverzeichnis?
     Option optDatabaseDir = new Option( "s", "databasedir", true, "set database directory" );
     options.addOption( optDatabaseDir );
     // Parser anlegen
     CommandLineParser cliParser = new BasicParser();
     // Argumente parsen!
     try
     {
       return( cliParser.parse( options, args ) );
     }
     catch( ParseException ex )
     {
       System.err.println( "Parser error: " + ex.getLocalizedMessage() );
       return( null );
     }
   }
 
   /**
    * Aus dem String von Loglevel den Logging-Wert machen Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 28.01.2012
    * @param optionValue
    * @return
    */
   private static Level parseLogLevel( String level )
   {
     String levelString = level.toUpperCase();
     // SEVERE (highest value) ERROR
     // WARNING WARNUNG
     // INFO INFO
     // CONFIG CONFIG
     // FINE DEBUG
     // FINER
     // FINEST (lowest value)
     if( levelString.equals( "FINEST" ) )
     {
       return( Level.FINE );
     }
     if( levelString.equals( "FINER" ) )
     {
       return( Level.FINE );
     }
     if( levelString.equals( "FINE" ) )
     {
       return( Level.FINE );
     }
     if( levelString.equals( "DEBUG" ) )
     {
       return( Level.FINE );
     }
     if( levelString.equals( "CONFIG" ) )
     {
       return( Level.CONFIG );
     }
     if( levelString.equals( "INFO" ) )
     {
       return( Level.INFO );
     }
     if( levelString.equals( "WARNING" ) )
     {
       return( Level.WARNING );
     }
     if( levelString.equals( "SERVE" ) )
     {
       return( Level.SEVERE );
     }
     return( Level.OFF );
   }
 
   /**
    * 
    * Neues Datenverzeichnis
    * 
    * Project: SubmatixBTForPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de)
    * 
    *         Stand: 18.07.2012
    */
   private static File parseNewDatabaseDir( String optionValue )
   {
     File tempLogFile;
     try
     {
       tempLogFile = new File( optionValue );
       return( tempLogFile );
     }
     catch( NullPointerException ex )
     {
       System.err.println( "parseNewDatabaseDir: Dirname was <null>" );
       return( null );
     }
   }
 
   /**
    * Setze das neue Logfile, wenn gewünscht Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 28.01.2012
    * @param optionValue
    * @return File
    */
   private static File parseNewLogFile( String optionValue )
   {
     File tempLogFile = null;
     File parentDir = null;
     if( optionValue.equals( "NONE" ) )
     {
       // abschalten des Logfiles
       return( null );
     }
     try
     {
       tempLogFile = new File( optionValue );
     }
     catch( NullPointerException ex )
     {
       System.err.println( "parseNewLogFile: Logfilename was <null>" );
       return( logFile );
     }
     try
     {
       parentDir = new File( tempLogFile.getParent() );
     }
     catch( NullPointerException ex )
     {
       // nix Parent, ist nur eine Datei....
       return( tempLogFile );
     }
     if( parentDir.exists() && parentDir.isDirectory() )
     {
       return( tempLogFile );
     }
     System.err.println( "parseNewLogFile: Logfile Directory not exists! (" + parentDir.getAbsolutePath() + ")" );
     return( logFile );
   }
 
   /**
    * Create the application.
    * 
    * @throws ConfigReadWriteException
    * @throws IOException
    */
   public MainCommGUI() throws IOException, ConfigReadWriteException
   {
     setDefaultLookAndFeelDecorated( isDefaultLookAndFeelDecorated() );
     // Konfiguration aus der Datei einlesen
     ReadConfig rcf = new ReadConfig();
     progConfig = rcf.getConfigClass();
     if( logFile != null )
     {
       // wenn auf der Kommandozeile was anderes vorgegeben ist...
       progConfig.setLogFile( logFile );
     }
     makeLogger( progConfig.getLogFile(), optionLogLevel );
     if( databaseDir != null )
     {
       // wenn auf der Kommandozeile ein neues Verzeichnis angegeben wurde
       progConfig.setDatabaseDir( databaseDir );
     }
     if( !progConfig.getDatabaseDir().isDirectory() )
     {
       if( false == progConfig.getDatabaseDir().mkdirs() )
       {
         LOGGER.log( Level.SEVERE, "can't create data directory <" + progConfig.getDatabaseDir().getAbsolutePath() + ">" );
         System.exit( -1 );
       }
       LOGGER.log( Level.FINE, "created data directory <" + progConfig.getDatabaseDir().getAbsolutePath() + ">" );
     }
     try
     {
       programLocale = Locale.getDefault();
       stringsBundle = ResourceBundle.getBundle( "de.dmarcini.submatix.pclogger.lang.messages", programLocale );
     }
     catch( MissingResourceException ex )
     {
       System.err.println( "ERROR get resources <" + ex.getMessage() + "> try standart Strings..." );
      stringsBundle = ResourceBundle.getBundle( "de.dmarcini.submatix.pclogger.lang.messages_en" );
     }
     prepareDatabase();
     currentConfig.setLogger( LOGGER );
     btComm = new BTCommunication( LOGGER, databaseUtil );
     btComm.addActionListener( this );
     try
     {
       initializeGUI();
     }
     catch( SQLException ex )
     {
       System.err.println( "ERROR while create GUI: <" + ex.getLocalizedMessage() + ">" );
       ex.printStackTrace();
       System.exit( -1 );
     }
     catch( ClassNotFoundException ex )
     {
       System.err.println( "ERROR while create GUI: <" + ex.getLocalizedMessage() + ">" );
       ex.printStackTrace();
       System.exit( -1 );
     }
     // Listener setzen (braucht auch die Maps)
     setGlobalChangeListener();
     btComm.refreshNameArray();
     Vector<String[]> entrys = btComm.getNameArray();
     DeviceComboBoxModel portBoxModel = new DeviceComboBoxModel( entrys );
     connectionPanel.deviceToConnectComboBox.setModel( portBoxModel );
     initLanuageMenu( programLocale );
     if( !DEBUG )
     {
       configPanel.setAllConfigPanlelsEnabled( false );
       gasConfigPanel.setAllGasPanelsEnabled( false );
       logListPanel.setAllLogPanelsEnabled( false );
       setElementsConnected( false );
     }
     if( readBtCacheOnStart )
     {
       LOGGER.log( Level.INFO, "call discover btdevices cached..." );
       btComm.discoverDevices( true );
       connectionPanel.setElementsDiscovering( true );
     }
     if( setLanguageStrings() < 1 )
     {
       System.exit( -1 );
     }
     waitForMessage = 0;
   }
 
   /**
    * Wenn ein element meint, was zu melden...
    */
   @Override
   public void actionPerformed( ActionEvent ev )
   {
     if( ignoreAction ) return;
     // /////////////////////////////////////////////////////////////////////////
     // Meine Actions
     if( ev.getID() > ActionEvent.ACTION_FIRST )
     {
       processMessageActions( ev );
       return;
     }
     // /////////////////////////////////////////////////////////////////////////
     // MENÜ
     else if( ev.getSource() instanceof JMenuItem )
     {
       processMenuActions( ev );
       return;
     }
     // /////////////////////////////////////////////////////////////////////////
     // Button
     else if( ev.getSource() instanceof JButton )
     {
       processButtonActions( ev );
       return;
     }
     // /////////////////////////////////////////////////////////////////////////
     // Combobox
     else if( ev.getSource() instanceof JComboBox )
     {
       processComboBoxActions( ev );
       return;
     }
   }
 
   /**
    * Ändere Heliumanteil vom Gas Nummer X Project: SubmatixBTForPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 18.04.2012
    * @param gasNr
    *          welches Gas denn
    * @param he
    *          Heliumanteil
    */
   private void changeHEFromGas( int gasNr, int he )
   {
     int o2;
     o2 = currGasList.getO2FromGas( gasNr );
     ignoreAction = true;
     if( gasConfigPanel.getHeSpinnerMap() == null ) return;
     if( gasConfigPanel.getO2SpinnerMap() == null ) return;
     if( he < 0 )
     {
       he = 0;
       ( gasConfigPanel.getHeSpinnerMap().get( gasNr ) ).setValue( 0 );
     }
     else if( he > 100 )
     {
       // Mehr als 100% geht nicht!
       // ungesundes Zeug!
       o2 = 0;
       he = 100;
       ( gasConfigPanel.getHeSpinnerMap().get( gasNr ) ).setValue( he );
       ( gasConfigPanel.getO2SpinnerMap().get( gasNr ) ).setValue( o2 );
       LOGGER.log( Level.WARNING, String.format( "change helium (max) in Gas %d Value: <%d/0x%02x>...", gasNr, he, he ) );
     }
     else if( ( o2 + he ) > 100 )
     {
       // Auch hier geht nicht mehr als 100%
       // Sauerstoff verringern!
       o2 = 100 - he;
       ( gasConfigPanel.getO2SpinnerMap().get( gasNr ) ).setValue( o2 );
       LOGGER.log( Level.FINE, String.format( "change helium in Gas %d Value: <%d/0x%02x>, reduct O2 <%d/0x%02x...", gasNr, he, he, o2, o2 ) );
     }
     else
     {
       LOGGER.log( Level.FINE, String.format( "change helium in Gas %d Value: <%d/0x%02x> O2: <%d/0x%02x>...", gasNr, he, he, o2, o2 ) );
     }
     currGasList.setGas( gasNr, o2, he );
     gasConfigPanel.setDescriptionForGas( gasNr, o2, he );
     ignoreAction = false;
   }
 
   /**
    * Ändere Sauerstoffanteil vom Gas Nummer X Project: SubmatixBTForPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 18.04.2012
    * @param gasNr
    *          welches Gas
    * @param o2
    *          Sauerstoffanteil
    */
   private void changeO2FromGas( int gasNr, int o2 )
   {
     int he;
     he = currGasList.getHEFromGas( gasNr );
     ignoreAction = true;
     if( gasConfigPanel.getHeSpinnerMap() == null ) return;
     if( gasConfigPanel.getO2SpinnerMap() == null ) return;
     if( o2 < 0 )
     {
       // das Zeut ist dann auch ungesund!
       o2 = 0;
       ( gasConfigPanel.getO2SpinnerMap().get( gasNr ) ).setValue( 0 );
     }
     else if( o2 > 100 )
     {
       // Mehr als 100% geht nicht!
       o2 = 100;
       he = 0;
       ( gasConfigPanel.getHeSpinnerMap().get( gasNr ) ).setValue( he );
       ( gasConfigPanel.getO2SpinnerMap().get( gasNr ) ).setValue( o2 );
       LOGGER.log( Level.WARNING, String.format( "change oxygen (max) in Gas %d Value: <%d/0x%02x>...", gasNr, o2, o2 ) );
     }
     else if( ( o2 + he ) > 100 )
     {
       // Auch hier geht nicht mehr als 100%
       // Helium verringern!
       he = 100 - o2;
       ( gasConfigPanel.getHeSpinnerMap().get( gasNr ) ).setValue( he );
       LOGGER.log( Level.FINE, String.format( "change oxygen in Gas %d Value: <%d/0x%02x>, reduct HE <%d/0x%02x...", gasNr, o2, o2, he, he ) );
     }
     else
     {
       LOGGER.log( Level.FINE, String.format( "change oxygen in Gas %d Value: <%d/0x%02x>...", gasNr, o2, o2 ) );
     }
     currGasList.setGas( gasNr, o2, he );
     // erzeuge und setze noch den Gasnamen
     // färbe dabei gleich die Zahlen ein
     gasConfigPanel.setDescriptionForGas( gasNr, o2, he );
     ignoreAction = false;
   }
 
   /**
    * Programmsprache wechseln Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 19.12.2011
    * @param cmd
    *          Sprachenkürzel
    */
   private void changeProgramLanguage( String cmd )
   {
     String[] langs = null;
     langs = cmd.split( "_" );
     if( langs.length > 1 && langs[1] != null )
     {
       programLocale = new Locale( langs[0], langs[1] );
     }
     else
     {
       programLocale = new Locale( langs[0] );
     }
     Locale.setDefault( programLocale );
     if( currentConfig != null )
     {
       // da verändern sich die Einstellungen, daher ungültig setzen
       currentConfig.setWasInit( false );
     }
     setLanguageStrings();
   }
 
   /**
    * 
    * Hilfsfunktion zum start speichern/update eines Tauchlogs
    * 
    * Project: SubmatixBTForPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de)
    * 
    *         Stand: 12.07.2012
    * @param logListEntry
    */
   private int computeLogRequest( Integer[] logListEntry )
   {
     // liegt ein updatewunsch vor?
     if( logListEntry[1] > 0 )
     {
       LOGGER.log( Level.WARNING, "dive logentry alredy there. Ask user for continue..." );
       //@formatter:off
       // Zeige dem geneigten User eine Dialogbox, in welcher er entscheiden muss: Update oder überspringen
       Object[] options =
       { 
         stringsBundle.getString( "MainCommGui.updateWarnDialog.updateButton" ),
         stringsBundle.getString( "MainCommGui.updateWarnDialog.cancelButton" )
       };
       int retOption =  JOptionPane.showOptionDialog( 
               null, 
               stringsBundle.getString( "MainCommGui.updateWarnDialog.messageUpdate" ), 
               stringsBundle.getString( "MainCommGui.updateWarnDialog.headLine" ), 
               JOptionPane.DEFAULT_OPTION, 
               JOptionPane.WARNING_MESSAGE, 
               null, 
               options, 
               options[0] 
               );
       //@formatter:on
       if( retOption == 1 )
       {
         LOGGER.log( Level.INFO, "user has cancel to update divelog entry." );
         return( 0 );
       }
       else
       {
         // update datensatz!
         logListPanel.setNextLogIsAnUpdate( true, logListEntry[0] );
       }
     }
     // datensatz anlegen
     wDial = new PleaseWaitDialog( stringsBundle.getString( "PleaseWaitDialog.title" ), stringsBundle.getString( "PleaseWaitDialog.waitForReadDive" ) );
     wDial.setDetailMessage( String.format( stringsBundle.getString( "PleaseWaitDialog.readDiveNumber" ), logListEntry[0] ) );
     wDial.setVisible( true );
     // Sag dem SPX er soll alles schicken
     LOGGER.log( Level.FINE, "send command to spx: send logfile number <" + logListEntry[0] + ">" );
     btComm.readLogDetailFromSPX( logListEntry[0] );
     return( 1 );
   }
 
   /**
    * Verbine mit SPX42 Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 20.12.2011
    */
   private void connectSPX()
   {
     int itemIndex = connectionPanel.deviceToConnectComboBox.getSelectedIndex();
     // Welche Schnittstelle?
     if( itemIndex == -1 )
     {
       LOGGER.log( Level.WARNING, "no connection device selected!" );
       showWarnBox( stringsBundle.getString( "MainCommGUI.warnDialog.notDeviceSelected.text" ) );
       return;
     }
     //
     // ist das Gerät als Online gefunden markiert?
     //
     if( ( ( DeviceComboBoxModel )connectionPanel.deviceToConnectComboBox.getModel() ).getWasOnlineAt( itemIndex ) )
     {
       // gerätenamen holen
       String deviceName = ( ( DeviceComboBoxModel )connectionPanel.deviceToConnectComboBox.getModel() ).getDeviceIdAt( itemIndex );
       LOGGER.log( Level.FINE, "connect via device <" + deviceName + ">..." );
       if( btComm.isConnected() )
       {
         // ist verbunden, was nun?
         return;
       }
       else
       {
         // nicht verbunden, tu was!
         try
         {
           wDial = new PleaseWaitDialog( stringsBundle.getString( "PleaseWaitDialog.title" ), stringsBundle.getString( "PleaseWaitDialog.pleaseWaitForConnect" ) );
           wDial.setVisible( true );
           btComm.connectDevice( deviceName );
         }
         catch( Exception ex )
         {
           showErrorDialog( ex.getLocalizedMessage() );
           LOGGER.log( Level.SEVERE, "Exception: <" + ex.getMessage() + ">" );
         }
       }
     }
     else
     {
       String deviceName = ( ( DeviceComboBoxModel )connectionPanel.deviceToConnectComboBox.getModel() ).getDeviceIdAt( itemIndex );
       String deviceAlias = ( ( DeviceComboBoxModel )connectionPanel.deviceToConnectComboBox.getModel() ).getDeviceAliasAt( itemIndex );
       showErrorDialog( String.format( stringsBundle.getString( "MainCommGUI.errorDialog.deviceNotConnected" ), deviceName + "/" + deviceAlias ) );
       LOGGER.warning( "the device <" + deviceName + "> was not online!" );
     }
   }
 
   /**
    * 
    * Decodiere die Nachricht über einen Logverzeichniseintrag
    * 
    * Project: SubmatixBTForPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de)
    * 
    *         Stand: 05.05.2012
    * @param entryMsg
    * @return
    */
   private String decodeLogDirEntry( String entryMsg )
   {
     // Message etwa so <~41:21:9_4_10_20_44_55.txt:22>
     String[] fields;
     String fileName;
     int number, max;
     int day, month, year, hour, minute, second;
     //
     // Felder aufteilen
     fields = fieldPatternDp.split( entryMsg );
     if( fields.length < 4 )
     {
       LOGGER.log( Level.SEVERE, "recived message for logdir has lower than 4 fields. It is wrong! Abort!" );
       return( null );
     }
     // Wandel die Nummerierung in Integer um
     try
     {
       number = Integer.parseInt( fields[1], 16 );
       max = Integer.parseInt( fields[3], 16 );
     }
     catch( NumberFormatException ex )
     {
       LOGGER.log( Level.SEVERE, "Fail to convert Hex to int: " + ex.getLocalizedMessage() );
       return( null );
     }
     fileName = fields[2];
     // verwandle die Dateiangabe in eine lesbare Datumsangabe
     // Format des Strings ist ja
     // TAG_MONAT_JAHR_STUNDE_MINUTE_SEKUNDE
     // des Beginns der Aufzeichnung
     fields = fieldPatternUnderln.split( fields[2] );
     try
     {
       day = Integer.parseInt( fields[0] );
       month = Integer.parseInt( fields[1] );
       year = Integer.parseInt( fields[2] ) + 2000;
       hour = Integer.parseInt( fields[3] );
       minute = Integer.parseInt( fields[4] );
       second = Integer.parseInt( fields[5] );
     }
     catch( NumberFormatException ex )
     {
       LOGGER.log( Level.SEVERE, "Fail to convert Hex to int: " + ex.getLocalizedMessage() );
       return( null );
     }
     // So, die Angaben des SPX sind immer im Localtime-Format
     // daher werde ich die auch so interpretieren
     // Die Funktion macht das in der default-Lokalzone, sollte also
     // da sein, wio der SPX auch ist... (schwieriges Thema)
     DateTime tm = new DateTime( year, month, day, hour, minute, second );
     DateTimeFormatter fmt = DateTimeFormat.forPattern( timeFormatterString );
     return( String.format( "%d;%s;%s;%d", number, fileName, tm.toString( fmt ), max ) );
   }
 
   /**
    * Verbindung trennen Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 20.12.2011
    */
   private void disconnectSPX()
   {
     if( btComm.isConnected() )
     {
       LOGGER.log( Level.FINE, "disconnect SPX42..." );
       btComm.disconnectDevice();
     }
   }
 
   /**
    * Eventuell geordnetes Aufräumen hier Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 18.12.2011
    */
   private void exitProgram()
   {
     if( databaseUtil != null )
     {
       if( databaseUtil.isOpenDB() )
       {
         databaseUtil.closeDB();
       }
       databaseUtil = null;
     }
     if( btComm != null )
     {
       if( btComm.isConnected() )
       {
         btComm.disconnectDevice();
         try
         {
           Thread.sleep( 350 );
         }
         catch( InterruptedException ex )
         {}
       }
     }
     // testen, ob da noch was zurückgeschrieben werden muss
     if( progConfig != null )
     {
       if( progConfig.isWasChanged() )
       {
         try
         {
           LOGGER.log( Level.INFO, "write config to file..." );
           new WriteConfig( progConfig );
         }
         catch( IOException ex )
         {
           ex.printStackTrace();
         }
         catch( ConfigReadWriteException ex )
         {
           ex.printStackTrace();
         }
       }
     }
     System.exit( 0 );
   }
 
   /**
    * Initialize the contents of the frame.
    * 
    * @throws ClassNotFoundException
    * @throws SQLException
    */
   private void initializeGUI() throws SQLException, ClassNotFoundException
   {
     frmMainwindowtitle = new JFrame();
     frmMainwindowtitle.setFont( new Font( "Arial", Font.PLAIN, 12 ) );
     frmMainwindowtitle.setSize( new Dimension( 810, 600 ) );
     frmMainwindowtitle.setResizable( false );
     frmMainwindowtitle.setIconImage( Toolkit.getDefaultToolkit().getImage( MainCommGUI.class.getResource( "/de/dmarcini/submatix/pclogger/res/apple.png" ) ) );
     frmMainwindowtitle.setTitle( "TITLE" );
     frmMainwindowtitle.setBounds( 100, 100, 800, 600 );
     frmMainwindowtitle.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
     frmMainwindowtitle.getContentPane().setLayout( new BorderLayout( 0, 0 ) );
     statusTextField = new JTextField();
     statusTextField.setEditable( false );
     statusTextField.setText( "-" );
     frmMainwindowtitle.getContentPane().add( statusTextField, BorderLayout.SOUTH );
     statusTextField.setColumns( 10 );
     tabbedPane = new JTabbedPane( JTabbedPane.TOP );
     frmMainwindowtitle.getContentPane().add( tabbedPane, BorderLayout.CENTER );
     tabbedPane.addMouseMotionListener( this );
     // Connection Panel
     connectionPanel = new spx42ConnectPanel( LOGGER, databaseUtil, btComm );
     tabbedPane.addTab( "CONNECTION", null, connectionPanel, null );
     tabbedPane.setEnabledAt( programTabs.TAB_CONNECT.ordinal(), true );
     // config Panel
     configPanel = new spx42ConfigPanel( LOGGER );
     tabbedPane.addTab( "CONFIG", null, configPanel, null );
     tabbedPane.setEnabledAt( programTabs.TAB_CONFIG.ordinal(), true );
     // GASPANEL
     gasConfigPanel = new spx42GaslistEditPanel( LOGGER, progConfig );
     tabbedPane.addTab( "GAS", null, gasConfigPanel, null );
     tabbedPane.setEnabledAt( programTabs.TAB_GASLIST.ordinal(), true );
     // GASPRESETPANEL
     gasPresetPanel = new spx42GasPresetEditPanel( LOGGER, databaseUtil, progConfig );
     tabbedPane.addTab( "PRESET", null, gasPresetPanel, null );
     tabbedPane.setEnabledAt( programTabs.TAB_GASPRESET.ordinal(), true );
     // Loglisten Panel
     logListPanel = new spx42LoglistPanel( LOGGER, this, databaseUtil );
     tabbedPane.addTab( "LOG", null, logListPanel, null );
     tabbedPane.setEnabledAt( programTabs.TAB_LOGREAD.ordinal(), true );
     // Grafik Panel
     logGraphPanel = new spx42LogGraphPanel( LOGGER, databaseUtil, progConfig );
     tabbedPane.addTab( "GRAPH", null, logGraphPanel, null );
     tabbedPane.setEnabledAt( programTabs.TAB_LOGGRAPH.ordinal(), true );
     // import/export Panel
     fileManagerPanel = new spx42FileManagerPanel( LOGGER, this, databaseUtil, progConfig );
     tabbedPane.addTab( "EXPORT", null, fileManagerPanel, null );
     tabbedPane.setEnabledAt( programTabs.TAB_FILEMANAGER.ordinal(), true );
     // MENÜ
     JMenuBar menuBar = new JMenuBar();
     frmMainwindowtitle.setJMenuBar( menuBar );
     mnFile = new JMenu( "FILE" );
     menuBar.add( mnFile );
     mntmExit = new JMenuItem( "EXIT" );
     mntmExit.setIcon( new ImageIcon( MainCommGUI.class.getResource( "/de/dmarcini/submatix/pclogger/res/176.png" ) ) );
     mntmExit.setActionCommand( "exit" );
     mntmExit.addActionListener( this );
     mntmExit.addMouseMotionListener( this );
     mntmExit.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_X, InputEvent.CTRL_MASK ) );
     mnFile.add( mntmExit );
     mnLanguages = new JMenu( "LANGUAGES" );
     mnLanguages.addMouseMotionListener( this );
     menuBar.add( mnLanguages );
     mnOptions = new JMenu( "OPTIONS" );
     mnOptions.addMouseMotionListener( this );
     menuBar.add( mnOptions );
     mntmOptions = new JMenuItem( "PROPERTYS" );
     mntmOptions.addMouseMotionListener( this );
     mntmOptions.addActionListener( this );
     mntmOptions.setActionCommand( "set_propertys" );
     mnOptions.add( mntmOptions );
     mnHelp = new JMenu( "HELP" );
     mnHelp.addMouseMotionListener( this );
     menuBar.add( mnHelp );
     mntmHelp = new JMenuItem( "HELP" );
     mntmHelp.addActionListener( this );
     mntmHelp.setActionCommand( "help" );
     mntmHelp.addMouseMotionListener( this );
     mnHelp.add( mntmHelp );
     mntmInfo = new JMenuItem( "INFO" );
     mntmInfo.addActionListener( this );
     mntmInfo.setActionCommand( "info" );
     mntmInfo.addMouseMotionListener( this );
     mntmInfo.setIcon( new ImageIcon( MainCommGUI.class.getResource( "/javax/swing/plaf/metal/icons/ocean/expanded.gif" ) ) );
     mntmInfo.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_I, InputEvent.CTRL_MASK ) );
     mnHelp.add( mntmInfo );
     connectionPanel.discoverProgressBar.setVisible( false );
   }
 
   /**
    * verfügbare Sprachen in Menü eintragen Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 18.12.2011
    * @param programLocale
    */
   private void initLanuageMenu( Locale programLocale )
   {
     ResourceBundle rb;
     Enumeration<String> enu;
     String key = null;
     String cmd = null;
     try
     {
       ignoreAction = true;
       // Lies die Resource aus
       rb = ResourceBundle.getBundle( "de.dmarcini.submatix.pclogger.lang.languages" );
       // Alle KEYS lesen
       enu = rb.getKeys();
       try
       {
         while( enu.hasMoreElements() )
         {
           JMenuItem menuItem = new JMenuItem();
           key = enu.nextElement();
           cmd = rb.getString( key );
           menuItem.setText( key );
           menuItem.addActionListener( this );
           menuItem.setActionCommand( cmd );
           menuItem.addMouseMotionListener( this );
           mnLanguages.add( menuItem );
         }
       }
       catch( NullPointerException ex )
       {
         statusTextField.setText( "ERROR set language strings" );
         System.out.println( "ERROR set language strings <" + ex.getMessage() + "> ABORT!" );
         System.exit( -1 );
       }
       catch( MissingResourceException ex )
       {
         statusTextField.setText( "ERROR set language strings" );
         System.out.println( "ERROR set language strings <" + ex.getMessage() + "> ABORT!" );
         System.exit( -1 );
       }
       catch( ClassCastException ex )
       {
         statusTextField.setText( "ERROR set language strings" );
         System.out.println( "ERROR set language strings <" + ex.getMessage() + "> ABORT!" );
         System.exit( -1 );
       }
       finally
       {
         ignoreAction = false;
       }
     }
     catch( NullPointerException ex )
     {
       statusTextField.setText( "ERROR set language strings" );
       System.out.println( "ERROR set language strings <" + ex.getMessage() + "> ABORT!" );
       System.exit( -1 );
     }
     catch( MissingResourceException ex )
     {
       statusTextField.setText( "ERROR set language strings - the given key can be found" );
       System.out.println( "ERROR set language strings - the given key can be found <" + ex.getMessage() + "> ABORT!" );
       System.exit( -1 );
     }
     catch( ClassCastException ex )
     {
       statusTextField.setText( "ERROR set language strings" );
       System.out.println( "ERROR set language strings <" + ex.getMessage() + "> ABORT!" );
       System.exit( -1 );
     }
     finally
     {
       ignoreAction = false;
     }
   }
 
   /**
    * Checkbox hat sich verändert
    */
   @Override
   public void itemStateChanged( ItemEvent ev )
   {
     if( ignoreAction ) return;
     // ////////////////////////////////////////////////////////////////////////
     // Checkbox Event?
     if( ev.getSource() instanceof JCheckBox )
     {
       JCheckBox cb = ( JCheckBox )ev.getItemSelectable();
       String cmd = cb.getActionCommand();
       // //////////////////////////////////////////////////////////////////////
       // Dynamische Gradienten?
       if( cmd.equals( "dyn_gradients_on" ) )
       {
         LOGGER.log( Level.FINE, "dynamic gradients <" + cb.isSelected() + ">" );
         currentConfig.setDynGradientsEnable( cb.isSelected() );
       }
       // //////////////////////////////////////////////////////////////////////
       // Deepstops
       else if( cmd.equals( "deepstops_on" ) )
       {
         LOGGER.log( Level.FINE, "depstops <" + cb.isSelected() + ">" );
         currentConfig.setDeepStopEnable( cb.isSelected() );
       }
       // //////////////////////////////////////////////////////////////////////
       // Passive Semiclose Rebreather Mode?
       else if( cmd.equals( "individuals_pscr_on" ) )
       {
         LOGGER.log( Level.FINE, "pscr mode  <" + cb.isSelected() + ">" );
         currentConfig.setPscrModeEnabled( cb.isSelected() );
       }
       // //////////////////////////////////////////////////////////////////////
       // Sensor warning on/off
       else if( cmd.equals( "individual_sensors_on" ) )
       {
         LOGGER.log( Level.FINE, "sensors on  <" + cb.isSelected() + ">" );
         currentConfig.setSensorsEnabled( cb.isSelected() );
       }
       // //////////////////////////////////////////////////////////////////////
       // Warnungen an/aus
       else if( cmd.equals( "individuals_warnings_on" ) )
       {
         LOGGER.log( Level.FINE, "warnings on  <" + cb.isSelected() + ">" );
         currentConfig.setSountEnabled( cb.isSelected() );
       }
       // //////////////////////////////////////////////////////////////////////
       // Bailout checkbox für ein Gas?
       else if( cmd.startsWith( "bailout:" ) )
       {
         String[] fields = fieldPatternDp.split( cmd );
         try
         {
           int idx = Integer.parseInt( fields[1] );
           LOGGER.log( Level.FINE, String.format( "Bailout %s changed.", cmd ) );
           currGasList.setBailout( idx, cb.isSelected() );
         }
         catch( NumberFormatException ex )
         {
           LOGGER.log( Level.SEVERE, "Exception while recive bailout checkbox event: " + ex.getLocalizedMessage() );
         }
       }
       // //////////////////////////////////////////////////////////////////////
       // Diluent 1 für ein Gsas setzen?
       else if( cmd.startsWith( "diluent1:" ) )
       {
         String[] fields = fieldPatternDp.split( cmd );
         try
         {
           int idx = Integer.parseInt( fields[1] );
           LOGGER.log( Level.FINE, String.format( "Diluent 1  to %d changed.", idx ) );
           if( cb.isSelected() )
           {
             currGasList.setDiluent1( idx );
           }
         }
         catch( NumberFormatException ex )
         {
           LOGGER.log( Level.SEVERE, "Exception while recive diluent1 checkbox event: " + ex.getLocalizedMessage() );
         }
       }
       // //////////////////////////////////////////////////////////////////////
       // Diluent 2 für ein Gsas setzen?
       else if( cmd.startsWith( "diluent2:" ) )
       {
         String[] fields = fieldPatternDp.split( cmd );
         try
         {
           int idx = Integer.parseInt( fields[1] );
           LOGGER.log( Level.FINE, String.format( "Diluent 2  to %d changed.", idx ) );
           if( cb.isSelected() )
           {
             currGasList.setDiluent2( idx );
           }
         }
         catch( NumberFormatException ex )
         {
           LOGGER.log( Level.SEVERE, "Exception while recive diluent1 checkbox event: " + ex.getLocalizedMessage() );
         }
       }
       else
       {
         LOGGER.log( Level.WARNING, "unknown item changed: <" + cb.getActionCommand() + "> <" + cb.isSelected() + ">" );
       }
     }
   }
 
   /**
    * Systemlogger machen! Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 18.12.2011
    * @param logFile
    * @param logLevel
    */
   private void makeLogger( File logFile, Level logLevel )
   {
     Properties loggingProperties = new Properties();
     String name = MainCommGUI.class.getPackage().getName();
     name = name.replace( ".gui", "" );
     try
     {
       loggingProperties.put( ".level", "FINEST" );
       // Root-Logger Handler spezifizieren
       loggingProperties.put( ".handlers", "java.util.logging.ConsoleHandler, java.util.logging.FileHandler" );
       // Konfiguration des ConsoleHandlers
       loggingProperties.put( "java.util.logging.ConsoleHandler.formatter", "java.util.logging.SimpleFormatter" );
       loggingProperties.put( "java.util.logging.ConsoleHandler.level", "FINEST" );
       // Konfiguration des FileHandlers
       loggingProperties.put( "java.util.logging.FileHandler.pattern", logFile.getAbsolutePath() );
       loggingProperties.put( "java.util.logging.FileHandler.limit", "100000" );
       loggingProperties.put( "java.util.logging.FileHandler.count", "1" );
       loggingProperties.put( "java.util.logging.FileHandler.formatter", "java.util.logging.SimpleFormatter" );
       loggingProperties.put( "java.util.logging.FileHandler.level", "FINEST" );
       // Properties an LogManager übergeben
       // ////////////////////////////////////////////////////////////////
       PipedOutputStream pos = new PipedOutputStream();
       PipedInputStream pis = new PipedInputStream( pos );
       loggingProperties.store( pos, "" );
       pos.close();
       LogManager.getLogManager().readConfiguration( pis );
       pis.close();
       //
       LOGGER = Logger.getLogger( MainCommGUI.class.getSimpleName() );
       cHandler = new ConsoleHandler();
       cHandler.setFormatter( new DirksConsoleLogFormatter( name ) );
       if( logFile != null )
       {
         fHandler = new FileHandler();
         fHandler.setFormatter( new DirksConsoleLogFormatter( name ) );
         fHandler.setLevel( logLevel );
         LOGGER.addHandler( fHandler );
       }
     }
     catch( SecurityException ex )
     {
       System.out.println( "ERROR create File Logger: <" + ex.getMessage() + ">" );
       System.exit( -1 );
     }
     catch( IOException ex )
     {
       System.out.println( "ERROR create File Logger: <" + ex.getMessage() + ">" );
       System.exit( -1 );
     }
     cHandler.setLevel( logLevel );
     LOGGER.addHandler( cHandler );
     LOGGER.setUseParentHandlers( false );
   }
 
   @Override
   public void mouseDragged( MouseEvent ev )
   {}
 
   /**
    * Wenn sich die Maus über was bewegt...
    */
   @Override
   public void mouseMoved( MouseEvent ev )
   {
     // Ist die Maus da irgendwo hingefahren?
     if( ev.getSource() instanceof JButton )
     {
       setStatus( ( ( JButton )ev.getSource() ).getToolTipText() );
     }
     else if( ev.getSource() instanceof JComboBox )
     {
       setStatus( ( ( JComboBox )ev.getSource() ).getToolTipText() );
     }
     else if( ev.getSource() instanceof JMenuItem )
     {
       setStatus( ( ( JMenuItem )ev.getSource() ).getToolTipText() );
     }
     else if( ev.getSource() instanceof JSpinner )
     {
       setStatus( ( ( JSpinner )ev.getSource() ).getToolTipText() );
     }
     else if( ev.getSource() instanceof JCheckBox )
     {
       setStatus( ( ( JCheckBox )ev.getSource() ).getToolTipText() );
     }
     else if( ev.getSource() instanceof JTable )
     {
       setStatus( ( ( JTable )ev.getSource() ).getToolTipText() );
     }
     else
     {
       setStatus( "" );
     }
   }
 
   /**
    * Die Statusbar soll sich bewegen Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 10.01.2012
    */
   private void moveStatusBar()
   {
     if( connectionPanel.discoverProgressBar.getMaximum() == connectionPanel.discoverProgressBar.getValue() )
     {
       connectionPanel.discoverProgressBar.setValue( 0 );
       return;
     }
     connectionPanel.discoverProgressBar.setValue( connectionPanel.discoverProgressBar.getValue() + 1 );
   }
 
   /**
    * Datenbankfür das Programm vorbereiten oder erzeugen Project: SubmatixBTForPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 24.04.2012
    */
   private void prepareDatabase()
   {
     // Verbindung zum Datenbanktreiber
     databaseUtil = new LogDerbyDatabaseUtil( LOGGER, progConfig.getDatabaseDir(), this );
     if( databaseUtil == null )
     {
       LOGGER.log( Level.SEVERE, "can connect to database drivers!" );
       System.exit( -1 );
     }
     // öffne die Datenbank
     // ging das?
     try
     {
       if( databaseUtil.createConnection() == null )
       {
         System.exit( -1 );
       }
     }
     catch( SQLException ex )
     {
       ex.printStackTrace();
       System.exit( -1 );
     }
     catch( ClassNotFoundException ex )
     {
       ex.printStackTrace();
       System.exit( -1 );
     }
     // hier ist alles gut...
   }
 
   /**
    * Bearbeitet Button Actions Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 07.01.2012
    * @param ev
    *          Avtion event
    */
   private void processButtonActions( ActionEvent ev )
   {
     String cmd = ev.getActionCommand();
     // /////////////////////////////////////////////////////////////////////////
     // Verbinde mit Device
     if( cmd.equals( "connect" ) )
     {
       if( btComm != null )
       {
         waitForMessage = 0; // auf erst mal nix warten...
         connectSPX();
       }
     }
     // /////////////////////////////////////////////////////////////////////////
     // Trenne Verbindung
     else if( cmd.equals( "disconnect" ) )
     {
       if( btComm != null )
       {
         disconnectSPX();
       }
     }
     // /////////////////////////////////////////////////////////////////////////
     // lese Config aus Device
     else if( cmd.equals( "read_config" ) )
     {
       if( btComm != null )
       {
         if( btComm.isConnected() )
         {
           wDial = new PleaseWaitDialog( stringsBundle.getString( "PleaseWaitDialog.title" ), stringsBundle.getString( "PleaseWaitDialog.readSpxConfig" ) );
           wDial.setMax( BTCommunication.CONFIG_READ_KDO_COUNT );
           wDial.setVisible( true );
           btComm.readConfigFromSPX42();
           // warte auf diese Nachricht....
           waitForMessage = ProjectConst.MESSAGE_SPXALIVE;
         }
         else
         {
           showWarnBox( stringsBundle.getString( "MainCommGUI.warnDialog.notConnected.text" ) );
         }
       }
     }
     // /////////////////////////////////////////////////////////////////////////
     // schreibe Config auf SPX42
     else if( cmd.equals( "write_config" ) )
     {
       if( btComm != null )
       {
         if( !currentConfig.isInitialized() || savedConfig == null )
         {
           showWarnBox( stringsBundle.getString( "MainCommGUI.warnDialog.notConfig.text" ) );
           return;
         }
         writeConfigToSPX( savedConfig );
       }
     }
     // /////////////////////////////////////////////////////////////////////////
     // Geräteliste neu lesen
     else if( cmd.equals( "refresh_bt_devices" ) )
     {
       LOGGER.log( Level.INFO, "call discover btdevices..." );
       btComm.discoverDevices( false );
       connectionPanel.setElementsDiscovering( true );
     }
     // /////////////////////////////////////////////////////////////////////////
     // PIN für Gerät setzen
     else if( cmd.equals( "set_pin_for_dev" ) )
     {
       LOGGER.log( Level.INFO, "call set pin for device..." );
       setPinForDevice();
     }
     // /////////////////////////////////////////////////////////////////////////
     // Alias editor zeigen
     else if( cmd.equals( "alias_bt_devices_on" ) )
     {
       LOGGER.log( Level.INFO, "alias editor show..." );
       connectionPanel.setAliasesEditable( true );
     }
     // /////////////////////////////////////////////////////////////////////////
     // Alias editor zeigen
     else if( cmd.equals( "alias_bt_devices_off" ) )
     {
       LOGGER.log( Level.INFO, "alias editor hide..." );
       connectionPanel.setAliasesEditable( false );
     }
     // /////////////////////////////////////////////////////////////////////////
     // ich will die Gasliste haben!
     else if( cmd.equals( "read_gaslist" ) )
     {
       LOGGER.log( Level.INFO, "call read gaslist from device..." );
       if( btComm != null )
       {
         if( btComm.isConnected() )
         {
           wDial = new PleaseWaitDialog( stringsBundle.getString( "PleaseWaitDialog.title" ), stringsBundle.getString( "PleaseWaitDialog.readGaslist" ) );
           wDial.setMax( BTCommunication.CONFIG_READ_KDO_COUNT );
           wDial.setVisible( true );
           btComm.readGaslistFromSPX42();
         }
         else
         {
           showWarnBox( stringsBundle.getString( "MainCommGUI.warnDialog.notConnected.text" ) );
         }
       }
     }
     // /////////////////////////////////////////////////////////////////////////
     // ich will die Gasliste schreiben!
     else if( cmd.equals( "write_gaslist" ) )
     {
       LOGGER.log( Level.INFO, "call write gaslist to device..." );
       if( btComm != null )
       {
         if( btComm.isConnected() && currGasList.isInitialized() )
         {
           wDial = new PleaseWaitDialog( stringsBundle.getString( "PleaseWaitDialog.title" ), stringsBundle.getString( "PleaseWaitDialog.writeGasList" ) );
           wDial.setMax( BTCommunication.CONFIG_READ_KDO_COUNT );
           wDial.setVisible( true );
           btComm.writeGaslistToSPX42( currGasList, currentConfig.getFirmwareVersion() );
         }
         else
         {
           if( !btComm.isConnected() )
           {
             showWarnBox( stringsBundle.getString( "MainCommGUI.warnDialog.notConnected.text" ) );
           }
           else
           {
             showWarnBox( stringsBundle.getString( "MainCommGUI.warnDialog.gasNotLoadet.text" ) );
           }
         }
       }
     }
     // /////////////////////////////////////////////////////////////////////////
     // lese Logdir aus Device
     else if( cmd.equals( "read_logdir_from_spx" ) )
     {
       if( btComm != null )
       {
         if( btComm.isConnected() )
         {
           if( logListPanel.prepareReadLogdir( btComm.getConnectedDevice() ) )
           {
             // Die Liste direkt vom SPX einlesen!
             // an dieser Stelle muss ich sicherstellen, daß ich die Masseinheiten des SPX42 kenne
             // ich gehe mal von den Längeneinheiten aus!
             // also Meter/Fuss
             wDial = new PleaseWaitDialog( stringsBundle.getString( "PleaseWaitDialog.title" ), stringsBundle.getString( "PleaseWaitDialog.readLogDir" ) );
             wDial.setVisible( true );
             if( !currentConfig.isInitialized() )
             {
               // jetzt wird es schwierig. erfrage erst mal die Config!
               btComm.readConfigFromSPX42();
             }
             // Sag dem SPX er soll alles schicken
             btComm.readLogDirectoryFromSPX();
           }
         }
         else
         {
           showWarnBox( stringsBundle.getString( "MainCommGUI.warnDialog.notConnected.text" ) );
         }
       }
     }
     // /////////////////////////////////////////////////////////////////////////
     // lese Logfile aus Device
     else if( cmd.equals( "read_logfile_from_spx" ) )
     {
       if( btComm != null )
       {
         if( btComm.isConnected() )
         {
           int logListLen = logListPanel.prepareDownloadLogdata( btComm.getConnectedDevice() );
           if( logListLen == 0 )
           {
             showWarnBox( stringsBundle.getString( "MainCommGUI.warnDialog.notLogentrySelected.text" ) );
             return;
           }
           if( !currentConfig.isInitialized() )
           {
             showWarnBox( stringsBundle.getString( "MainCommGUI.warnDialog.notConfig.text" ) );
             return;
           }
           Integer[] logListEntry = logListPanel.getNextEntryToRead();
           if( logListEntry != null )
           {
             computeLogRequest( logListEntry );
           }
         }
         else
         {
           showWarnBox( stringsBundle.getString( "MainCommGUI.warnDialog.notConnected.text" ) );
         }
       }
     }
     // /////////////////////////////////////////////////////////////////////////
     // Da hab ich nix passendes gefunden!
     else
     {
       LOGGER.log( Level.WARNING, "unknown button command <" + cmd + "> recived." );
     }
     return;
   }
 
   /**
    * Bearbeitet Combobox actions Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 07.01.2012
    * @param ev
    *          Avtion event
    */
   private void processComboBoxActions( ActionEvent ev )
   {
     String cmd = ev.getActionCommand();
     String entry = null;
     JComboBox srcBox = ( JComboBox )ev.getSource();
     // /////////////////////////////////////////////////////////////////////////
     // Auswahl welches Gerät soll verbunden werden
     if( connectionPanel.deviceToConnectComboBox.equals( srcBox ) )
     {
       if( srcBox.getSelectedIndex() == -1 )
       {
         // nix selektiert
         return;
       }
       entry = ( String )srcBox.getItemAt( srcBox.getSelectedIndex() );
       LOGGER.log( Level.FINE, "select port <" + entry + ">..." );
     }
     // /////////////////////////////////////////////////////////////////////////
     // Letzter Decostop auf 3 oder 6 Meter
     else if( cmd.equals( "deco_last_stop" ) )
     {
       entry = ( String )srcBox.getSelectedItem();
       LOGGER.log( Level.FINE, "deco last stop <" + entry + ">..." );
       currentConfig.setLastStop( srcBox.getSelectedIndex() );
     }
     // /////////////////////////////////////////////////////////////////////////
     // Preset für Deco-Gradienten ausgewählt
     else if( cmd.equals( "deco_gradient_preset" ) )
     {
       entry = ( String )srcBox.getSelectedItem();
       LOGGER.log( Level.FINE, "gradient preset <" + entry + ">, Index: <" + srcBox.getSelectedIndex() + ">..." );
       currentConfig.setDecoGfPreset( srcBox.getSelectedIndex() );
       // Spinner setzen
       setGradientSpinnersAfterPreset( srcBox.getSelectedIndex() );
     }
     // /////////////////////////////////////////////////////////////////////////
     // Autosetpoint Voreinstellung
     else if( cmd.equals( "set_autosetpoint" ) )
     {
       entry = ( String )srcBox.getSelectedItem();
       LOGGER.log( Level.FINE, "autosetpoint preset <" + entry + ">..." );
       currentConfig.setAutoSetpoint( srcBox.getSelectedIndex() );
     }
     // /////////////////////////////////////////////////////////////////////////
     // Setpoint für höchsten PPO2 Wert einstellen
     else if( cmd.equals( "set_highsetpoint" ) )
     {
       entry = ( String )srcBox.getSelectedItem();
       LOGGER.log( Level.FINE, "hightsetpoint <" + entry + ">..." );
       currentConfig.setMaxSetpoint( srcBox.getSelectedIndex() );
     }
     // /////////////////////////////////////////////////////////////////////////
     // Helligkeit des Displays
     else if( cmd.equals( "set_disp_brightness" ) )
     {
       entry = ( String )srcBox.getSelectedItem();
       LOGGER.log( Level.FINE, "brightness <" + entry + ">..." );
       currentConfig.setDisplayBrithtness( srcBox.getSelectedIndex() );
     }
     // /////////////////////////////////////////////////////////////////////////
     // Ausrichtung des Displays
     else if( cmd.equals( "set_display_orientation" ) )
     {
       entry = ( String )srcBox.getSelectedItem();
       LOGGER.log( Level.FINE, "orientation <" + entry + ">..." );
       currentConfig.setDisplayOrientation( srcBox.getSelectedIndex() );
     }
     // /////////////////////////////////////////////////////////////////////////
     // Grad Celsius oder Fahrenheit einstellen
     else if( cmd.equals( "set_temperature_unit" ) )
     {
       entry = ( String )srcBox.getSelectedItem();
       LOGGER.log( Level.FINE, "temperature unit <" + entry + ">..." );
       currentConfig.setUnitTemperature( srcBox.getSelectedIndex() );
     }
     // /////////////////////////////////////////////////////////////////////////
     // Maßeinheit für Tiefe festlegen
     else if( cmd.equals( "set_depth_unit" ) )
     {
       entry = ( String )srcBox.getSelectedItem();
       LOGGER.log( Level.FINE, "depth unit <" + entry + ">..." );
       currentConfig.setUnitDepth( srcBox.getSelectedIndex() );
       configPanel.setUnitDepth( srcBox.getSelectedIndex() );
     }
     // /////////////////////////////////////////////////////////////////////////
     // Süßwasser oder Salzwasser
     else if( cmd.equals( "set_salnity" ) )
     {
       entry = ( String )srcBox.getSelectedItem();
       LOGGER.log( Level.FINE, "salnity <" + entry + ">..." );
       currentConfig.setUnitSalnyty( srcBox.getSelectedIndex() );
     }
     // /////////////////////////////////////////////////////////////////////////
     // Loginterval einstellen
     else if( cmd.equals( "set_loginterval" ) )
     {
       entry = ( String )srcBox.getSelectedItem();
       LOGGER.log( Level.FINE, "loginterval <" + entry + ">..." );
       currentConfig.setLogInterval( srcBox.getSelectedIndex() );
     }
     // /////////////////////////////////////////////////////////////////////////
     // Anzahl der sensoren für Messung/Warung einstellen
     else if( cmd.equals( "set_sensorwarnings" ) )
     {
       entry = ( String )srcBox.getSelectedItem();
       LOGGER.log( Level.FINE, "sensorwarnings <" + entry + ">...Index: <" + srcBox.getSelectedIndex() + ">" );
       currentConfig.setSensorsCount( srcBox.getSelectedIndex() );
     }
     else
     {
       LOGGER.log( Level.WARNING, "unknown combobox command <" + cmd + "> recived." );
     }
   }
 
   /**
    * Bearbeitet Menüaktionen Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 07.01.2012
    * @param ev
    *          Avtion event
    */
   private void processMenuActions( ActionEvent ev )
   {
     String cmd = ev.getActionCommand();
     // /////////////////////////////////////////////////////////////////////////
     // Menü EXIT Programm
     if( cmd.equals( "exit" ) )
     {
       // Ordentlich verlassen ;-)
       tabbedPane.setSelectedIndex( programTabs.TAB_CONNECT.ordinal() );
       exitProgram();
     }
     // /////////////////////////////////////////////////////////////////////////
     // Info Box anzeigen
     else if( cmd.equals( "info" ) )
     {
       LOGGER.log( Level.FINE, "Call INFO-Dialog..." );
       showInfoDialog();
     }
     // /////////////////////////////////////////////////////////////////////////
     // Hilfe Box anzeigen
     else if( cmd.equals( "help" ) )
     {
       LOGGER.log( Level.FINE, "Call HELP-Dialog..." );
       showHelpForm();
     }
     // /////////////////////////////////////////////////////////////////////////
     // Box für diverse Einstellungen anzeigen
     else if( cmd.equals( "set_propertys" ) )
     {
       showPropertysDialog();
     }
     // /////////////////////////////////////////////////////////////////////////
     // Sprachenmenü wurde ausgewählt
     else if( cmd.startsWith( "lang_" ) )
     {
       // welche Sprache hättens denn gern?
       LOGGER.log( Level.FINE, "Change Language..." );
       String lang = cmd.replace( "lang_", "" );
       LOGGER.log( Level.INFO, "change language to <" + lang + ">" );
       changeProgramLanguage( lang );
     }
     else
     {
       LOGGER.log( Level.WARNING, "unknown menu command <" + cmd + "> recived." );
     }
     return;
   }
 
   /**
    * Bearbeitet meine "Messages" Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 07.01.2012
    * @param ev
    *          Avtion event
    */
   private void processMessageActions( ActionEvent ev )
   {
     String cmd = ev.getActionCommand();
     int actionId = ev.getID();
     //
     // wenn ich auf eine Nachricht warten soll, um das "WARTE"-Fenster zu schliessen
     //
     if( waitForMessage != 0 )
     {
       if( waitForMessage == actionId )
       {
         // es ist die Nachricht, auf die ich waren soll
         if( wDial != null )
         {
           wDial.dispose();
           wDial = null;
         }
         // den Merker auf null setzen!
         waitForMessage = 0;
       }
     }
     switch ( actionId )
     {
     // /////////////////////////////////////////////////////////////////////////
     // Hab was gelesen!
       case ProjectConst.MESSAGE_READ:
         LOGGER.log( Level.FINE, "READ Command!" );
         // soll den reader Thread und die GUI nicht blockieren
         // daher nur in die Liste schmeissen (die ist thread-sicher)
         if( ( !cmd.isEmpty() ) && ( !cmd.equals( "\n" ) ) )
         {
           messagesList.add( cmd );
           LOGGER.log( Level.FINE, "RECIVED: <" + cmd + ">" );
         }
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Gerätename ausgelesen
       case ProjectConst.MESSAGE_MANUFACTURER_READ:
         LOGGER.log( Level.INFO, "Device Manufacturer Name from SPX42 <" + cmd + "> recived..." );
         currentConfig.setDeviceName( cmd );
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Firmwareversion gelesen!
       case ProjectConst.MESSAGE_FWVERSION_READ:
         LOGGER.log( Level.INFO, "Firmware Version <" + cmd + "> recived..." );
         currentConfig.setFirmwareVersion( cmd );
         configPanel.setFirmwareLabel( cmd );
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Seriennummer vom SPX42
       case ProjectConst.MESSAGE_SERIAL_READ:
         LOGGER.log( Level.INFO, "Serial Number from SPX42 recived..." );
         configPanel.setSerialNumber( cmd );
         currentConfig.setSerial( cmd );
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Decompressionseinstellungen gelesen
       case ProjectConst.MESSAGE_DECO_READ:
         LOGGER.log( Level.INFO, "DECO propertys from SPX42 recived..." );
         if( wDial != null )
         {
           wDial.incrementProgress();
         }
         if( currentConfig.setDecoGf( cmd ) )
         {
           LOGGER.log( Level.INFO, "DECO propertys set to GUI..." );
           configPanel.setDecoGradient();
           LOGGER.log( Level.INFO, "DECO propertys set to GUI...OK" );
         }
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Einheiten Einstellungen vom SPX42 gelesen
       case ProjectConst.MESSAGE_UNITS_READ:
         LOGGER.log( Level.INFO, "UNITS propertys from SPX42 recived..." );
         if( wDial != null )
         {
           wDial.incrementProgress();
         }
         if( currentConfig.setUnits( cmd ) )
         {
           LOGGER.log( Level.INFO, "UNITS propertys set to GUI..." );
           configPanel.setUnits();
         }
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Displayeinstellungen gelesen
       case ProjectConst.MESSAGE_DISPLAY_READ:
         LOGGER.log( Level.INFO, "DISPLAY propertys from SPX42 recived..." );
         if( wDial != null )
         {
           wDial.incrementProgress();
         }
         if( currentConfig.setDisplay( cmd ) )
         {
           LOGGER.log( Level.INFO, "DISPLAY propertys set to GUI..." );
           configPanel.setDisplayPropertys();
         }
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Einstellungen zum O2 Setpint gelesen
       case ProjectConst.MESSAGE_SETPOINT_READ:
         LOGGER.log( Level.INFO, "SETPOINT propertys from SPX42 recived..." );
         if( wDial != null )
         {
           wDial.incrementProgress();
         }
         if( currentConfig.setSetpoint( cmd ) )
         {
           LOGGER.log( Level.INFO, "SETPOINT propertys set to GUI..." );
           configPanel.setSetpoint();
         }
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Einstellungen für Individuell gelesen (Extra-Lizenz erforderlich )
       case ProjectConst.MESSAGE_INDIVID_READ:
         LOGGER.log( Level.INFO, "INDIVIDUAL propertys from SPX42 recived..." );
         if( wDial != null )
         {
           wDial.incrementProgress();
         }
         if( currentConfig.setIndividuals( cmd ) )
         {
           LOGGER.log( Level.INFO, "INDIVIDUAL propertys set to GUI..." );
           configPanel.setIndividuals( true );
         }
         else
         {
           configPanel.setIndividuals( false );
         }
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Der Lizenzstatus
       case ProjectConst.MESSAGE_LICENSE_STATE_READ:
         LOGGER.log( Level.INFO, "lizense state from SPX42 recived..." );
         currentConfig.setLicenseStatus( cmd );
         licenseState = currentConfig.getLicenseState();
         customConfig = currentConfig.getCustomEnabled();
         gasConfigPanel.setLicenseState( licenseState, customConfig );
         gasConfigPanel.setLicenseLabel( stringsBundle );
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Versuche Verbindung mit Bluetooht Gerät
       case ProjectConst.MESSAGE_CONNECTING:
         LOGGER.log( Level.INFO, "CONNECTING..." );
         setElementsInactive( true );
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Device wurde verbunden
       case ProjectConst.MESSAGE_CONNECTED:
         LOGGER.log( Level.INFO, "CONNECT" );
         setElementsConnected( true );
         // Gleich mal Fragen, wer da dran ist!
         btComm.askForDeviceName();
         btComm.askForSerialNumber();
         btComm.askForLicenseFromSPX();
         btComm.askForFirmwareVersion();
         connectionPanel.setAliasesEditable( false );
         connectionPanel.refreshAliasTable();
         // ware, bis die Nachricht FWVERSION_READ kommt, um das wartefenster zu schliessen
         waitForMessage = ProjectConst.MESSAGE_FWVERSION_READ;
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Device wurde getrennt
       case ProjectConst.MESSAGE_DISCONNECTED:
         LOGGER.log( Level.INFO, "DISCONNECT" );
         if( wDial != null )
         {
           wDial.dispose();
           wDial = null;
         }
         setElementsConnected( false );
         configPanel.setAllConfigPanlelsEnabled( false );
         gasConfigPanel.setElementsGasMatrixPanelEnabled( false );
         connectionPanel.refreshAliasTable();
         if( tabbedPane.getSelectedIndex() != programTabs.TAB_CONNECT.ordinal() )
         {
           showWarnBox( stringsBundle.getString( "MainCommGui.warnDialog.connectionClosed" ) );
         }
         if( tabbedPane.getSelectedIndex() != programTabs.TAB_LOGGRAPH.ordinal() )
         {
           // wen nicht grad loggrafik angezeigt wird, auf den Connecttab wechseln
           tabbedPane.setSelectedIndex( programTabs.TAB_CONNECT.ordinal() );
         }
         break;
       // /////////////////////////////////////////////////////////////////////////
       // BT Discovering war erfolgreich
       case ProjectConst.MESSAGE_BTRECOVEROK:
         connectionPanel.setElementsDiscovering( false );
         refillPortComboBox();
         break;
       // /////////////////////////////////////////////////////////////////////////
       // BT Discovering war fehlerhaft
       case ProjectConst.MESSAGE_BTRECOVERERR:
         connectionPanel.setElementsDiscovering( false );
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Nachricht bitte noch warten
       case ProjectConst.MESSAGE_BTWAITFOR:
         moveStatusBar();
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Kein Gerät zum Verbinden gefunden!
       case ProjectConst.MESSAGE_BTNODEVCONN:
         LOGGER.log( Level.SEVERE, "no device found..." );
         showWarnBox( stringsBundle.getString( "MainCommGUI.warnDialog.notDeviceSelected.text" ) );
         setElementsConnected( false );
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Gerät benötigt PIN
       case ProjectConst.MESSAGE_BTAUTHREQEST:
         LOGGER.log( Level.INFO, "authentification requested..." );
         setPinForDevice();
         connectSPX();
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Discover Nachricht gesendet
       case ProjectConst.MESSAGE_BTMESSAGE:
         connectionPanel.setBtMessage( "device: " + cmd );
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Lebenszeichen mit Ackuspannugn empfangen
       case ProjectConst.MESSAGE_SPXALIVE:
         LOGGER.log( Level.INFO, "acku value from spx42 recived..." );
         setAckuValue( cmd );
         if( savedConfig == null )
         {
           currentConfig.setWasInit( true );
         }
         savedConfig = new SPX42Config( currentConfig );
         configPanel.setAllConfigPanlelsEnabled( true );
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Nachricht, daß da etwas passiert, also Hinweisbox weiterzählen lassen
       case ProjectConst.MESSAGE_PROCESS_NEXT:
         if( wDial != null )
         {
           if( cmd == null )
           {
             wDial.incrementProgress();
           }
           else
           {
             wDial.setProgress( cmd );
           }
         }
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Nachricht, daß die Hinweisbox geschlossen werden kann
       case ProjectConst.MESSAGE_PROCESS_END:
         if( wDial != null )
         {
           wDial.dispose();
           wDial = null;
         }
         if( cmd != null )
         {
           if( cmd.equals( "config_write" ) )
           {
             savedConfig = new SPX42Config( currentConfig );
           }
         }
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Nachricht über den Empfang einer Gaseinstellung
       case ProjectConst.MESSAGE_GAS_READ:
         // Gibts schon ein GasListObjekt
         if( currGasList == null )
         {
           // erzeuge eines!
           currGasList = new SPX42GasList( LOGGER );
         }
         // läßt sich das Teil parsen?
         if( currGasList.setGas( cmd ) )
         {
           // ist alle initialisiert?
           if( currGasList.isInitialized() )
           {
             // wenn die Gaskiste initialisiert ist
             gasConfigPanel.setElementsGasMatrixPanelEnabled( true );
             // Mach mal alles in die Spinner rein
             if( gasConfigPanel.getHeSpinnerMap() == null ) return;
             if( gasConfigPanel.getO2SpinnerMap() == null ) return;
             if( gasConfigPanel.getDiluent1Map() == null ) return;
             if( gasConfigPanel.getDiluent2Map() == null ) return;
             if( gasConfigPanel.getBailoutMap() == null ) return;
             ignoreAction = true;
             for( int i = 0; i < currGasList.getGasCount(); i++ )
             {
               ( gasConfigPanel.getHeSpinnerMap().get( i ) ).setValue( currGasList.getHEFromGas( i ) );
               ( gasConfigPanel.getO2SpinnerMap().get( i ) ).setValue( currGasList.getO2FromGas( i ) );
               gasConfigPanel.setDescriptionForGas( i, currGasList.getO2FromGas( i ), currGasList.getHEFromGas( i ) );
               // ist dieses Gas Diluent 1?
               if( currGasList.getDiulent1() == i )
               {
                 ( gasConfigPanel.getDiluent1Map().get( i ) ).setSelected( true );
               }
               // ist dieses Gas Diluent 2?
               if( currGasList.getDiluent2() == i )
               {
                 ( gasConfigPanel.getDiluent2Map().get( i ) ).setSelected( true );
               }
               // Status als Bailoutgas?
               if( currGasList.getBailout( i ) == 3 )
               {
                 ( gasConfigPanel.getBailoutMap().get( i ) ).setSelected( true );
               }
               else
               {
                 ( gasConfigPanel.getBailoutMap().get( i ) ).setSelected( false );
               }
             }
             ignoreAction = false;
             // dann kann das fenster ja wech!
             if( wDial != null )
             {
               wDial.dispose();
               wDial = null;
             }
           }
         }
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Nachricht Syncronisation wird beendet
       case ProjectConst.MESSAGE_SYCSTAT_OFF:
         LOGGER.log( Level.WARNING, "SPX42 switched SYNC OFF! Connetion will failure...." );
         // disconnect!
         btComm.disconnectDevice();
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Nachricht Gase wurden erfolgreich geschrieben
       case ProjectConst.MESSAGE_GAS_WRITTEN:
         LOGGER.log( Level.FINE, "gas written to SPX..." );
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Nachricht ein Logbuch Verzeichniseintrag wurde gelesen
       case ProjectConst.MESSAGE_DIRENTRY_READ:
         LOGGER.log( Level.FINE, "logdir entry recived..." );
         String decodet = decodeLogDirEntry( cmd );
         if( decodet != null )
         {
           logListPanel.addLogdirEntry( decodet );
         }
         if( !logListPanel.isReadingComplete() )
         {
           // hab noch zu tun...
           if( wDial != null )
           {
             wDial.incrementProgress();
           }
         }
         else
         {
           // dann kann das fenster ja wech!
           if( wDial != null )
           {
             wDial.dispose();
             wDial = null;
           }
         }
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Nachricht: Start einer Logdatenübermittling
       case ProjectConst.MESSAGE_LOGENTRY_START:
         LOGGER.log( Level.FINE, "start transfer logentry <" + cmd + ">..." );
         logListPanel.startTransfer( cmd, currentConfig.getUnitSystem() );
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Nachricht: Logzeile übertragen
       case ProjectConst.MESSAGE_LOGENTRY_LINE:
         LOGGER.log( Level.FINE, "recive one log line from SPX..." );
         logListPanel.addLogLineFromSPX( cmd );
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Nachricht: Logzeile übertragen
       case ProjectConst.MESSAGE_LOGENTRY_STOP:
         LOGGER.log( Level.FINE, "logfile transfer done..." );
         // Ab auf die Platte ind die DB damit!
         logListPanel.writeCacheToDatabase();
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Nachricht: Logdirectory aus Cache neu aufbauen
       case ProjectConst.MESSAGE_LOGDIRFROMCACHE:
         LOGGER.log( Level.FINE, "log directory from cache rebuilding..." );
         logListPanel.addLogDirFromCache();
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Nachricht: Daten gesichert....
       case ProjectConst.MESSAGE_DB_SUCCESS:
         LOGGER.log( Level.FINE, "loglist transfer success..." );
         // dann kann das fenster ja wech!
         if( wDial != null )
         {
           wDial.dispose();
           wDial = null;
         }
         if( btComm != null )
         {
           if( btComm.isConnected() )
           {
             Integer[] logListEntry = logListPanel.getNextEntryToRead();
             if( logListEntry != null )
             {
               // unterscheide Update oder Neu
               computeLogRequest( logListEntry );
             }
             else
             {
               // Da sind keine Einträge mehr zu lesen. Mach ein Update der Logverzeichnisliste
               if( btComm != null )
               {
                 if( btComm.isConnected() )
                 {
                   // Kann ich vom Cache lesen?
                   if( logListPanel.canReadFromCache() )
                   {
                     // Baue die Liste mit dem Cache wieder auf
                     ev = new ActionEvent( this, ProjectConst.MESSAGE_LOGDIRFROMCACHE, "from_ache" );
                     actionPerformed( ev );
                   }
                   else
                   {
                     // lese die Liste der Logeinträge neu ein
                     if( logListPanel.prepareReadLogdir( btComm.getConnectedDevice() ) )
                     {
                       // Sag dem SPX er soll alles schicken
                       wDial = new PleaseWaitDialog( stringsBundle.getString( "PleaseWaitDialog.title" ), stringsBundle.getString( "PleaseWaitDialog.readLogDir" ) );
                       wDial.setVisible( true );
                       btComm.readLogDirectoryFromSPX();
                     }
                   }
                 }
                 else
                 {
                   showWarnBox( stringsBundle.getString( "MainCommGUI.warnDialog.notConnected.text" ) );
                 }
               }
             }
           }
           else
           {
             showWarnBox( stringsBundle.getString( "MainCommGUI.warnDialog.notConnected.text" ) );
           }
         }
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Nachricht: Datenbankfehler....
       case ProjectConst.MESSAGE_DB_FAIL:
         LOGGER.log( Level.FINE, "loglist transfer failed..." );
         // dann kann das fenster ja wech!
         if( wDial != null )
         {
           wDial.dispose();
           wDial = null;
         }
         if( cmd != null )
         {
           showErrorDialog( stringsBundle.getString( "spx42LoglistPanel.logListLabel.text" ) + "\n" + cmd );
         }
         else
         {
           showErrorDialog( stringsBundle.getString( "spx42LoglistPanel.logListLabel.text" ) );
         }
         logListPanel.removeFailedDataset();
         break;
       // /////////////////////////////////////////////////////////////////////////
       // Nichts traf zu....
       default:
         LOGGER.log( Level.WARNING, "unknown message recived!" );
         break;
     }
     return;
   }
 
   /**
    * Die devicebox neu befüllen Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 11.01.2012
    */
   private void refillPortComboBox()
   {
     Vector<String[]> entrys = btComm.getNameArray();
     DeviceComboBoxModel portBoxModel = new DeviceComboBoxModel( entrys );
     connectionPanel.deviceToConnectComboBox.setModel( portBoxModel );
   }
 
   /**
    * Ackuwert des SPX anzeigen Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 22.01.2012
    */
   private void setAckuValue( String vl )
   {
     LOGGER.log( Level.FINE, "Value: <" + vl + ">" );
     double ackuValue = 0.0;
     Pattern fieldPatternDp = Pattern.compile( ":" );
     String[] fields = fieldPatternDp.split( vl );
     if( fields.length > 1 )
     {
       int val = Integer.parseInt( fields[1], 16 );
       ackuValue = ( float )( val / 100.0 );
       // Hauptfenster
       frmMainwindowtitle.setTitle( stringsBundle.getString( "MainCommGUI.frmMainwindowtitle.title" ) + " "
               + String.format( stringsBundle.getString( "MainCommGUI.ackuLabel.text" ), ackuValue ) );
       LOGGER.log( Level.FINE, String.format( "Acku value: %02.02f", ackuValue ) );
     }
   }
 
   /**
    * Setze combobox für Deco Gradienten Preset entsprechend der Angaben in den Spinnern Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 13.04.2012
    * @param decoValue
    */
   private void setDecoComboAfterSpinnerChange()
   {
     int currentPreset = currentConfig.getDecoGfPreset();
     ignoreAction = true;
     configPanel.setDecoGradientenPreset( currentPreset );
     ignoreAction = false;
   }
 
   /**
    * Elemente abhängig vom Connectstatus erlauben/sperren Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 22.12.2011
    * @param active
    */
   private void setElementsConnected( boolean active )
   {
     connectionPanel.setElementsConnected( active );
     tabbedPane.setEnabledAt( programTabs.TAB_CONFIG.ordinal(), active );
     tabbedPane.setEnabledAt( programTabs.TAB_GASLIST.ordinal(), active );
     tabbedPane.setEnabledAt( programTabs.TAB_LOGREAD.ordinal(), active );
     if( !active )
     {
       configPanel.setSerialNumber( "-" );
       configPanel.setFirmwareLabel( "-" );
       if( savedConfig != null )
       {
         savedConfig = null;
       }
       currentConfig.clear();
     }
   }
 
   /**
    * Beim Verbindungsaufbau inaktiv zeigen Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 13.04.2012
    * @param active
    *          Aktiv oder nicht
    */
   private void setElementsInactive( boolean active )
   {
     connectionPanel.setElementsInactive( active );
     tabbedPane.setEnabledAt( programTabs.TAB_CONFIG.ordinal(), active );
   }
 
   /**
    * Die Callbacks setzen, wenn sich in den Panels was ändert! Project: SubmatixBTForPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 22.04.2012
    */
   private void setGlobalChangeListener()
   {
     tabbedPane.addChangeListener( this );
     connectionPanel.setGlobalChangeListener( this );
     configPanel.setGlobalChangeListener( this );
     gasConfigPanel.setGlobalChangeListener( this );
     logListPanel.setGlobalChangeListener( this );
     logGraphPanel.setGlobalChangeListener( this );
   }
 
   /**
    * Wurde das Preset verändert, Spinner entsprechend ausfüllen Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 13.04.2012
    * @param selectedIndex
    *          Der Index der Combobox
    */
   private void setGradientSpinnersAfterPreset( int selectedIndex )
   {
     // nach Preset einstellen?
     switch ( selectedIndex )
     {
       case VERY_CONSERVATIVE:
       case CONSERVATIVE:
       case MODERATE:
       case AGGRESSIVE:
       case VERY_AGGRESSIVE:
         // in den oben genannten Fällen die Spinner auf den Preset einstellen
         ignoreAction = true;
         configPanel.setDecoGradientenSpinner();
         configPanel.setDecoGradientenPreset( selectedIndex );
         ignoreAction = false;
         LOGGER.log( Level.FINE, "spinner corrected for preset." );
         break;
     }
   }
 
   /**
    * Setze alle Strings im Form Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 18.12.2011
    */
   private int setLanguageStrings()
   {
     // so, ignoriere mal alles....
     ignoreAction = true;
     try
     {
       stringsBundle = ResourceBundle.getBundle( "de.dmarcini.submatix.pclogger.lang.messages", programLocale );
     }
     catch( MissingResourceException ex )
     {
       System.out.println( "ERROR get resources <" + ex.getMessage() + "> ABORT!" );
       return( -1 );
     }
     try
     {
       setStatus( "" );
       timeFormatterString = stringsBundle.getString( "MainCommGUI.timeFormatterString" );
       // Hauptfenster
       frmMainwindowtitle.setTitle( stringsBundle.getString( "MainCommGUI.frmMainwindowtitle.title" ) );
       // Menü
       mnFile.setText( stringsBundle.getString( "MainCommGUI.mnFile.text" ) );
       mnFile.setToolTipText( stringsBundle.getString( "MainCommGUI.mnFile.tooltiptext" ) );
       mntmExit.setText( stringsBundle.getString( "MainCommGUI.mntmExit.text" ) );
       mntmExit.setToolTipText( stringsBundle.getString( "MainCommGUI.mntmExit.tooltiptext" ) );
       mnLanguages.setText( stringsBundle.getString( "MainCommGUI.mnLanguages.text" ) );
       mnLanguages.setToolTipText( stringsBundle.getString( "MainCommGUI.mnLanguages.tooltiptext" ) );
       mnOptions.setText( stringsBundle.getString( "MainCommGUI.mnOptions.text" ) );
       mnOptions.setToolTipText( stringsBundle.getString( "MainCommGUI.mnOptions.tooltiptext" ) );
       mntmOptions.setText( stringsBundle.getString( "MainCommGUI.mntmOptions.text" ) );
       mntmOptions.setToolTipText( stringsBundle.getString( "MainCommGUI.mntmOptions.tooltiptext" ) );
       mnHelp.setText( stringsBundle.getString( "MainCommGUI.mnHelp.text" ) );
       mnHelp.setToolTipText( stringsBundle.getString( "MainCommGUI.mnHelp.tooltiptext" ) );
       mntmHelp.setText( stringsBundle.getString( "MainCommGUI.mntmHelp.text" ) );
       mntmHelp.setToolTipText( stringsBundle.getString( "MainCommGUI.mntmHelp.tooltiptext" ) );
       mntmInfo.setText( stringsBundle.getString( "MainCommGUI.mntmInfo.text" ) );
       mntmInfo.setToolTipText( stringsBundle.getString( "MainCommGUI.mntmInfo.tooltiptext" ) );
       // //////////////////////////////////////////////////////////////////////
       // //////////////////////////////////////////////////////////////////////
       // Tabbed Panes
       // //////////////////////////////////////////////////////////////////////
       // Tabbed Pane connect
       tabbedPane.setTitleAt( programTabs.TAB_CONNECT.ordinal(), stringsBundle.getString( "spx42ConnectPanel.title" ) );
       connectionPanel.setLanguageStrings( stringsBundle, btComm.isConnected() );
       // //////////////////////////////////////////////////////////////////////
       // Tabbed Pane config
       tabbedPane.setTitleAt( programTabs.TAB_CONFIG.ordinal(), stringsBundle.getString( "spx42ConfigPanel.title" ) );
       configPanel.setLanguageStrings( stringsBundle );
       // //////////////////////////////////////////////////////////////////////
       // Tabbed Pane gas
       tabbedPane.setTitleAt( programTabs.TAB_GASLIST.ordinal(), stringsBundle.getString( "spx42GaslistEditPanel.title" ) );
       gasConfigPanel.setLanguageStrings( stringsBundle );
       // //////////////////////////////////////////////////////////////////////
       // Tabbed Pane gaspreset
       tabbedPane.setTitleAt( programTabs.TAB_GASPRESET.ordinal(), stringsBundle.getString( "spx42GasPresetEditPanel.title" ) );
       gasPresetPanel.setLanguageStrings( stringsBundle );
       // //////////////////////////////////////////////////////////////////////
       // Tabbed Pane log
       tabbedPane.setTitleAt( programTabs.TAB_LOGREAD.ordinal(), stringsBundle.getString( "spx42LoglistPanel.title" ) );
       logListPanel.setLanguageStrings( stringsBundle );
       // //////////////////////////////////////////////////////////////////////
       // Tabbed Pane graph
       tabbedPane.setTitleAt( programTabs.TAB_LOGGRAPH.ordinal(), stringsBundle.getString( "spx42LogGraphPanel.title" ) );
       logGraphPanel.setLanguageStrings( stringsBundle );
       // //////////////////////////////////////////////////////////////////////
       // Tabbed Pane import/export
       tabbedPane.setTitleAt( programTabs.TAB_FILEMANAGER.ordinal(), stringsBundle.getString( "fileManagerPanel.title" ) );
       fileManagerPanel.setLanguageStrings( stringsBundle );
     }
     catch( NullPointerException ex )
     {
       statusTextField.setText( "ERROR set language strings" );
       System.out.println( "ERROR set language strings <" + ex.getMessage() + "> ABORT!" );
       return( -1 );
     }
     catch( MissingResourceException ex )
     {
       statusTextField.setText( "ERROR set language strings - the given key can be found" );
       System.out.println( "ERROR set language strings - the given key can be found <" + ex.getMessage() + "> ABORT!" );
       return( 0 );
     }
     catch( ClassCastException ex )
     {
       statusTextField.setText( "ERROR set language strings" );
       System.out.println( "ERROR set language strings <" + ex.getMessage() + "> ABORT!" );
       return( 0 );
     }
     finally
     {
       ignoreAction = false;
     }
     return( 1 );
   }
 
   /**
    * Setze PIN für Gerät in der Auswahl Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 22.01.2012
    */
   private void setPinForDevice()
   {
     String deviceName = null;
     ImageIcon icon = null;
     String pinString = null;
     // Welche Schnittstelle?
     if( connectionPanel.deviceToConnectComboBox.getSelectedIndex() == -1 )
     {
       LOGGER.log( Level.WARNING, "no connection device selected!" );
       showWarnBox( stringsBundle.getString( "MainCommGUI.warnDialog.notDeviceSelected.text" ) );
       return;
     }
     deviceName = ( ( DeviceComboBoxModel )( connectionPanel.deviceToConnectComboBox.getModel() ) ).getDeviceIdAt( connectionPanel.deviceToConnectComboBox.getSelectedIndex() );
     icon = new ImageIcon( MainCommGUI.class.getResource( "/de/dmarcini/submatix/pclogger/res/Unlock.png" ) );
     pinString = ( String )JOptionPane.showInputDialog( this, stringsBundle.getString( "MainCommGUI.setPinDialog.text" ) + " <" + deviceName + ">",
             stringsBundle.getString( "MainCommGUI.setPinDialog.headline" ), JOptionPane.PLAIN_MESSAGE, icon, null, btComm.getPinForDevice( deviceName ) );
     if( pinString != null )
     {
       btComm.setPinForDevice( deviceName, pinString );
     }
   }
 
   /**
    * Statustext in der Statuszeile setzen Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 24.12.2011
    * @param msg
    */
   private void setStatus( String msg )
   {
     if( statusTextField != null )
     {
       statusTextField.setText( msg );
     }
   }
 
   /**
    * 
    * Zeige eine Fehlermeldung mit Hinweistext ind Icon
    * 
    * Project: SubmatixBTForPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de)
    * 
    *         Stand: 25.06.2012
    * @param header
    * @param message
    */
   private void showErrorDialog( String message )
   {
     ImageIcon icon = null;
     try
     {
       icon = new ImageIcon( MainCommGUI.class.getResource( "/de/dmarcini/submatix/pclogger/res/Terminate.png" ) );
       JOptionPane.showMessageDialog( this, message, stringsBundle.getString( "MainCommGUI.errorDialog.headline" ), JOptionPane.INFORMATION_MESSAGE, icon );
     }
     catch( NullPointerException ex )
     {
       statusTextField.setText( "ERROR showErrorBox" );
       LOGGER.log( Level.SEVERE, "ERROR showErrorBox <" + ex.getMessage() + "> ABORT!" );
       return;
     }
     catch( MissingResourceException ex )
     {
       statusTextField.setText( "ERROR showErrorBox" );
       LOGGER.log( Level.SEVERE, "ERROR showErrorBox <" + ex.getMessage() + "> ABORT!" );
       return;
     }
     catch( ClassCastException ex )
     {
       statusTextField.setText( "ERROR showErrorBox" );
       LOGGER.log( Level.SEVERE, "ERROR showErrorBox <" + ex.getMessage() + "> ABORT!" );
       return;
     }
   }
 
   /**
    * Zeige ein Hilfe-Fenster Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 28.01.2012
    */
   private void showHelpForm()
   {
     new HelpFrameClass( programLocale, LOGGER );
   }
 
   /**
    * 
    * Zeige eine klein Info über das Proggi an Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * Project: SubmatixBTForPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de)
    * 
    *         Stand: 05.01.2012
    */
   private void showInfoDialog()
   {
     try
     {
       ProgramInfoDialog pDial = new ProgramInfoDialog( stringsBundle );
       pDial.showDialog();
     }
     catch( NullPointerException ex )
     {
       statusTextField.setText( "ERROR showInfoDialog" );
       LOGGER.log( Level.SEVERE, "ERROR showInfoDialog <" + ex.getMessage() + "> ABORT!" );
       return;
     }
     catch( MissingResourceException ex )
     {
       statusTextField.setText( "ERROR showInfoDialog" );
       LOGGER.log( Level.SEVERE, "ERROR showInfoDialog <" + ex.getMessage() + "> ABORT!" );
       return;
     }
     catch( ClassCastException ex )
     {
       statusTextField.setText( "ERROR showInfoDialog" );
       LOGGER.log( Level.SEVERE, "ERROR showInfoDialog <" + ex.getMessage() + "> ABORT!" );
       return;
     }
   }
 
   /**
    * 
    * Zeige einen Optionendialog zur Einstellung von Programmgeschichten
    * 
    * Project: SubmatixBTForPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de)
    * 
    *         Stand: 18.07.2012
    */
   private void showPropertysDialog()
   {
     if( btComm != null )
     {
       if( btComm.isConnected() )
       {
         showErrorDialog( stringsBundle.getString( "MainCommGUI.errorDialog.onlyNotConnected" ) );
         return;
       }
     }
     LOGGER.log( Level.FINE, "create an show propertys dialog..." );
     ProgramProperetysDialog pDial = new ProgramProperetysDialog( stringsBundle, progConfig, LOGGER );
     // pDial.setVisible( true );
     if( pDial.showModal() )
     {
       LOGGER.log( Level.FINE, "dialog whith OK closed...." );
       // progConfig = pDial.getProcConfig();
       if( progConfig.isWasChanged() )
       {
         // Wenn da was passieren sollte, muss die DB geschlossen sein.
         if( databaseUtil != null )
         {
           databaseUtil.closeDB();
         }
         showWarnBox( "RESTART PROGRAMM!" );
         pDial.dispose();
         exitProgram();
       }
       LOGGER.log( Level.FINE, "dialog whith OK closed NO Changes...." );
     }
     else
     {
       LOGGER.log( Level.FINE, "dialog canceled...." );
     }
     pDial.dispose();
     LOGGER.log( Level.FINE, "dialog disposed..." );
   }
 
   /**
    * Zeigt eine Warnung an Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 07.01.2012
    * @param msg
    *          Warnmessage
    */
   private void showWarnBox( String msg )
   {
     ImageIcon icon = null;
     try
     {
       icon = new ImageIcon( MainCommGUI.class.getResource( "/de/dmarcini/submatix/pclogger/res/Abort.png" ) );
       JOptionPane.showMessageDialog( this, msg, stringsBundle.getString( "MainCommGUI.warnDialog.headline" ), JOptionPane.WARNING_MESSAGE, icon );
     }
     catch( NullPointerException ex )
     {
       statusTextField.setText( "ERROR showWarnDialog" );
       LOGGER.log( Level.SEVERE, "ERROR showWarnDialog <" + ex.getMessage() + "> ABORT!" );
       return;
     }
     catch( MissingResourceException ex )
     {
       statusTextField.setText( "ERROR showWarnDialog" );
       LOGGER.log( Level.SEVERE, "ERROR showWarnDialog <" + ex.getMessage() + "> ABORT!" );
       return;
     }
     catch( ClassCastException ex )
     {
       statusTextField.setText( "ERROR showWarnDialog" );
       LOGGER.log( Level.SEVERE, "ERROR showWarnDialog <" + ex.getMessage() + "> ABORT!" );
       return;
     }
   }
 
   @Override
   public void stateChanged( ChangeEvent ev )
   {
     JSpinner currSpinner = null;
     int currValue;
     if( ignoreAction ) return;
     // //////////////////////////////////////////////////////////////////////
     // war es ein spinner?
     if( ev.getSource() instanceof JSpinner )
     {
       currSpinner = ( JSpinner )ev.getSource();
       // //////////////////////////////////////////////////////////////////////
       // Deco gradient Hith
       if( currSpinner.equals( configPanel.getDecoGradientenHighSpinner() ) )
       {
         // wert für High ändern
         currValue = ( Integer )currSpinner.getValue();
         LOGGER.log( Level.FINE, String.format( "change decoGradientHighSpinner <%d/%x>...", currValue, currValue ) );
         currentConfig.setDecoGfHigh( currValue );
         setDecoComboAfterSpinnerChange();
       }
       // //////////////////////////////////////////////////////////////////////
       // Deco gradient Low
       else if( currSpinner.equals( configPanel.getDecoGradientenLowSpinner() ) )
       {
         // Wert für LOW ändern
         currValue = ( Integer )currSpinner.getValue();
         LOGGER.log( Level.FINE, String.format( "change decoGradientLowSpinner <%d/%x>...", currValue, currValue ) );
         currentConfig.setDecoGfLow( currValue );
         setDecoComboAfterSpinnerChange();
       }
       else
       {
         // //////////////////////////////////////////////////////////////////////
         // Nix gefunden, also versuch mal die Listen durch
         if( gasConfigPanel.getHeSpinnerMap() == null ) return;
         if( gasConfigPanel.getO2SpinnerMap() == null ) return;
         for( int gasNr = 0; gasNr < currGasList.getGasCount(); gasNr++ )
         {
           if( currSpinner.equals( gasConfigPanel.getO2SpinnerMap().get( gasNr ) ) )
           {
             // O2 Spinner betätigt
             // Gas <gasNr> Sauerstoffanteil ändern
             currValue = ( Integer )currSpinner.getValue();
             changeO2FromGas( gasNr, currValue );
             return;
           }
           else if( currSpinner.equals( gasConfigPanel.getHeSpinnerMap().get( gasNr ) ) )
           {
             // Heliumspinner betätigt
             // Gas <gasNr> Heliumanteil ändern
             currValue = ( Integer )currSpinner.getValue();
             changeHEFromGas( gasNr, currValue );
             return;
           }
         }
         LOGGER.log( Level.WARNING, "unknown spinner recived!" );
       }
     }
     // //////////////////////////////////////////////////////////////////////
     // war es ein tabbedPane
     // //////////////////////////////////////////////////////////////////////
     else if( ev.getSource() instanceof JTabbedPane )
     {
       if( tabbedPane.equals( ev.getSource() ) )
       {
         int tabIdx = tabbedPane.getSelectedIndex();
         LOGGER.log( Level.FINE, String.format( "tabbedPane changed to %02d!", tabIdx ) );
         //
         // ist es das Grafikpanel?
         //
         if( tabIdx == programTabs.TAB_LOGGRAPH.ordinal() )
         {
           LOGGER.log( Level.FINE, "graph tab select, init grapic..." );
           String connDev = null;
           if( btComm != null )
           {
             connDev = btComm.getConnectedDevice();
           }
           // Grafiksachen initialisieren
           try
           {
             logGraphPanel.initGraph( connDev );
           }
           catch( Exception ex )
           {
             LOGGER.log( Level.SEVERE, "initGraph Exception: <" + ex.getLocalizedMessage() + ">" );
             showErrorDialog( stringsBundle.getString( "MainCommGui.errorDialog.openGraphWindow" ) );
             return;
           }
         }
         else
         {
           // grafiksachen freigeben
           logGraphPanel.releaseGraph();
         }
         //
         // ist es das Exportpanel zum exportieren der Daten (importieren für Service)
         //
         if( tabIdx == programTabs.TAB_FILEMANAGER.ordinal() )
         {
           LOGGER.log( Level.FINE, "export/import tab select, init db..." );
           String connDev = null;
           if( btComm != null )
           {
             connDev = btComm.getConnectedDevice();
           }
           // Panel initialisieren
           try
           {
             fileManagerPanel.initData( connDev );
           }
           catch( Exception ex )
           {
             LOGGER.log( Level.SEVERE, "initData Exception: <" + ex.getLocalizedMessage() + ">" );
             showErrorDialog( stringsBundle.getString( "MainCommGui.errorDialog.openExportWindow" ) );
             return;
           }
         }
         //
         // ist es das Config Panel?
         //
         if( tabIdx == programTabs.TAB_CONFIG.ordinal() )
         {
           LOGGER.log( Level.FINE, "config tab select, init gui..." );
           configPanel.prepareConfigPanel( currentConfig );
         }
         else
         {
           // Daten freigeben
           configPanel.releaseConfig();
         }
         //
         // ist es das Loglistpanel
         //
         if( tabIdx == programTabs.TAB_LOGREAD.ordinal() )
         {
           // Panel initialisieren
           LOGGER.log( Level.FINE, "logreader tab select, init gui..." );
           String connDev = null;
           if( btComm != null )
           {
             connDev = btComm.getConnectedDevice();
           }
           logListPanel.prepareLogListPanel( connDev );
         }
         else
         {
           logListPanel.releasePanel();
         }
         //
         // ist es das Gaspanel?
         //
         if( tabIdx == programTabs.TAB_GASLIST.ordinal() )
         {
           // Panel initialisieren
           LOGGER.log( Level.FINE, "gaslist tab select, init gui..." );
           gasConfigPanel.prepareGasslistPanel();
         }
         else
         {
           // Panel Daten freigeben
           gasConfigPanel.releasePanel();
         }
         //
         // ist es das Gas Preset Panel?
         //
         if( tabIdx == programTabs.TAB_GASPRESET.ordinal() )
         {
           // Panel initialisieren
           LOGGER.log( Level.FINE, "gas preset tab select, init gui..." );
           gasPresetPanel.setMouseMoveListener( this );
           gasPresetPanel.prepareGasslistPanel();
         }
         else
         {
           // Panel Daten freigeben
           gasPresetPanel.releasePanel();
         }
       }
     }
     else
     {
       LOGGER.log( Level.WARNING, "unknown source type recived!" );
     }
   }
 
   /**
    * Schreibe die aktuelle Konfiguration in den SPX42 Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 11.04.2012
    * @param cnf
    *          Config objekt
    */
   private void writeConfigToSPX( SPX42Config cnf )
   {
     //
     wDial = new PleaseWaitDialog( stringsBundle.getString( "PleaseWaitDialog.title" ), stringsBundle.getString( "PleaseWaitDialog.writeSpxConfig" ) );
     wDial.setMax( BTCommunication.CONFIG_WRITE_KDO_COUNT );
     wDial.resetProgress();
     wDial.setVisible( true );
     LOGGER.log( Level.INFO, "write config to SPX42..." );
     btComm.writeConfigToSPX( currentConfig );
   }
 }
