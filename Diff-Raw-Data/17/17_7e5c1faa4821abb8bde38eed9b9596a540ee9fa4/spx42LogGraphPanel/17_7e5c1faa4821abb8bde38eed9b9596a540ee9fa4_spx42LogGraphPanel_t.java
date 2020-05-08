 package de.dmarcini.submatix.pclogger.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.DateAxis;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.XYAreaRenderer;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.data.time.Second;
 import org.jfree.data.time.TimeSeries;
 import org.jfree.data.time.TimeSeriesCollection;
 import org.jfree.data.xy.XYDataset;
 import org.joda.time.DateTime;
 
 import de.dmarcini.submatix.pclogger.utils.ConnectDatabaseUtil;
 import de.dmarcini.submatix.pclogger.utils.LogForDeviceDatabaseUtil;
 import de.dmarcini.submatix.pclogger.utils.LogListComboBoxModel;
 
 public class spx42LogGraphPanel extends JPanel implements ActionListener
 {
   /**
    * 
    */
   private static final long   serialVersionUID = 1L;
   protected Logger            LOGGER           = null;
   private ConnectDatabaseUtil dbUtil           = null;
   private ResourceBundle      stringsBundle    = null;
   private File                dataDir          = null;
   private ChartPanel          chartPanel       = null;
   private JPanel              topPanel;
   private JPanel              bottomPanel;
   private JComboBox           deviceComboBox;
   private JComboBox           diveSelectComboBox;
   private JButton             computeGraphButton;
   private JLabel              lblLabel;
 
   @SuppressWarnings( "unused" )
   private spx42LogGraphPanel()
   {
     initPanel();
   }
 
   /**
    * Create the panel.
    * 
    * @param LOGGER
    * @param _dbUtil
    */
   public spx42LogGraphPanel( Logger LOGGER, final ConnectDatabaseUtil _dbUtil )
   {
     this.LOGGER = LOGGER;
     LOGGER.log( Level.FINE, "constructor..." );
     this.dbUtil = _dbUtil;
     initPanel();
   }
 
   /**
    * Initialisiere das Panel für die Verbindungen Project: SubmatixBTForPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 22.04.2012 TODO
    */
   private void initPanel()
   {
     setLayout( new BorderLayout( 0, 0 ) );
     topPanel = new JPanel();
     add( topPanel, BorderLayout.NORTH );
     deviceComboBox = new JComboBox();
     deviceComboBox.setFont( new Font( "Dialog", Font.PLAIN, 12 ) );
     deviceComboBox.setActionCommand( "change_device_to_display" );
     diveSelectComboBox = new JComboBox();
     diveSelectComboBox.setFont( new Font( "Dialog", Font.PLAIN, 12 ) );
     diveSelectComboBox.setActionCommand( "change_dive_to_display" );
     computeGraphButton = new JButton( "GRAPHBUTTON" );
     computeGraphButton.setActionCommand( "show_log_graph" );
     GroupLayout gl_topPanel = new GroupLayout( topPanel );
     gl_topPanel
             .setHorizontalGroup( gl_topPanel.createParallelGroup( Alignment.LEADING ).addGroup(
                     Alignment.TRAILING,
                     gl_topPanel.createSequentialGroup().addContainerGap().addComponent( deviceComboBox, 0, 235, Short.MAX_VALUE ).addGap( 18 )
                             .addComponent( diveSelectComboBox, GroupLayout.PREFERRED_SIZE, 282, GroupLayout.PREFERRED_SIZE ).addGap( 18 ).addComponent( computeGraphButton )
                             .addGap( 122 ) ) );
     gl_topPanel.setVerticalGroup( gl_topPanel.createParallelGroup( Alignment.LEADING ).addGroup(
             gl_topPanel
                     .createSequentialGroup()
                     .addContainerGap( GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE )
                     .addGroup(
                             gl_topPanel.createParallelGroup( Alignment.BASELINE )
                                     .addComponent( deviceComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE )
                                     .addComponent( diveSelectComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE )
                                     .addComponent( computeGraphButton ) ) ) );
     topPanel.setLayout( gl_topPanel );
     bottomPanel = new JPanel();
     add( bottomPanel, BorderLayout.SOUTH );
     lblLabel = new JLabel( "LABEL" );
     bottomPanel.add( lblLabel );
     chartPanel = null;
   }
 
   /**
    * Setze die Listener auf das Hauptobjekt Project: SubmatixBTForPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 22.04.2012
    * @param mainCommGUI
    *          das Hauptobjekt
    */
   public void setGlobalChangeListener( MainCommGUI mainCommGUI )
   {
     deviceComboBox.addMouseMotionListener( mainCommGUI );
     computeGraphButton.addMouseMotionListener( mainCommGUI );
     // die Aktionen mach ich im Objekt selber
     deviceComboBox.addActionListener( this );
     computeGraphButton.addActionListener( this );
   }
 
   /**
    * Setze alle Strings in die entsprechende Landessprache! Project: SubmatixBTForPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 22.04.2012
    * @param stringsBundle
    *          Resource für die Strings
    * @return in Ordnung oder nicht
    */
   public int setLanguageStrings( ResourceBundle stringsBundle )
   {
     this.stringsBundle = stringsBundle;
     try
     {
       deviceComboBox.setToolTipText( stringsBundle.getString( "spx42LogGraphPanel.deviceComboBox.tooltiptext" ) );
       diveSelectComboBox.setToolTipText( stringsBundle.getString( "spx42LogGraphPanel.diveSelectComboBox.tooltiptext" ) );
       computeGraphButton.setText( stringsBundle.getString( "spx42LogGraphPanel.computeGraphButton.text" ) );
       computeGraphButton.setToolTipText( stringsBundle.getString( "spx42LogGraphPanel.computeGraphButton.tooltiptext" ) );
     }
     catch( NullPointerException ex )
     {
       System.out.println( "ERROR set language strings <" + ex.getMessage() + "> ABORT!" );
       return( -1 );
     }
     catch( MissingResourceException ex )
     {
       System.out.println( "ERROR set language strings - the given key can be found <" + ex.getMessage() + "> ABORT!" );
       return( 0 );
     }
     catch( ClassCastException ex )
     {
       System.out.println( "ERROR set language strings <" + ex.getMessage() + "> ABORT!" );
       return( 0 );
     }
     clearDiveComboBox();
     return( 1 );
   }
 
   @Override
   public void actionPerformed( ActionEvent ev )
   {
     String cmd = ev.getActionCommand();
     String entry = null;
     int index, dbId;
     String device;
     //
     // /////////////////////////////////////////////////////////////////////////
     // Button
     if( ev.getSource() instanceof JButton )
     {
       // JButton srcButton = ( JButton )ev.getSource();
       // /////////////////////////////////////////////////////////////////////////
       // Anzeigebutton?
       if( cmd.equals( "show_log_graph" ) )
       {
         LOGGER.log( Level.FINE, "show log graph initiated." );
         // welches Device (wichtig um das richtige DB-File zu wählen)
         index = deviceComboBox.getSelectedIndex();
         if( index < 0 )
         {
           // kein Gerät ausgewählt
           LOGGER.log( Level.WARNING, "no device selected." );
           return;
         }
         device = dbUtil.getNameForAlias( ( String )deviceComboBox.getSelectedItem() );
         // welchen Tauchgang?
         index = diveSelectComboBox.getSelectedIndex();
         if( index < 0 )
         {
           LOGGER.log( Level.WARNING, "no dive selected." );
           return;
         }
         dbId = ( ( LogListComboBoxModel )diveSelectComboBox.getModel() ).getDatabaseIdAt( index );
         if( dbId < 0 )
         {
           LOGGER.log( Level.SEVERE, "can't find database id for dive." );
           return;
         }
         makeGraphForLog( dbId, device );
         return;
       }
       else
       {
         LOGGER.log( Level.WARNING, "unknown button command <" + cmd + "> recived." );
       }
       return;
     }
     // /////////////////////////////////////////////////////////////////////////
     // Combobox
     else if( ev.getSource() instanceof JComboBox )
     {
       JComboBox srcBox = ( JComboBox )ev.getSource();
       // /////////////////////////////////////////////////////////////////////////
       // Gerät zur Grafischen Darstellung auswählen
       if( cmd.equals( "change_device_to_display" ) )
       {
         entry = ( String )srcBox.getSelectedItem();
         LOGGER.log( Level.FINE, "device <" + entry + ">...Index: <" + srcBox.getSelectedIndex() + ">" );
         fillDiveComboBox( entry );
       }
       // /////////////////////////////////////////////////////////////////////////
       // Dive zur Grafischen Darstellung auswählen
       else if( cmd.equals( "change_dive_to_display" ) )
       {
         entry = ( String )srcBox.getSelectedItem();
         LOGGER.log( Level.FINE, "dive <" + entry + ">...Index: <" + srcBox.getSelectedIndex() + ">" );
         // fillDiveComboBox( entry );
       }
       else
       {
         LOGGER.log( Level.WARNING, "unknown combobox command <" + cmd + "> recived." );
       }
       return;
     }
     else
     {
       LOGGER.log( Level.WARNING, "unknown action command <" + cmd + "> recived." );
     }
   }
 
   /**
    * 
    * Anhand des Alias des Gerätes die Tauchgangsliste füllen
    * 
    * Project: SubmatixBTForPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de)
    * 
    *         Stand: 02.07.2012
    * @param deviceAlias
    */
   private void fillDiveComboBox( String deviceAlias )
   {
     LogForDeviceDatabaseUtil logDatabaseUtil;
     String device;
     DateTime dateTime;
     long javaTime;
     //
     // Alias fürs Gerät zurücksuchen
     //
     device = dbUtil.getNameForAlias( deviceAlias );
     if( device != null )
     {
       LOGGER.log( Level.FINE, "search dive list for device <" + device + ">..." );
       releaseGraph();
       //
       // richtige Datenbank öffnen
       //
       // das Datenbankutility initialisieren
       logDatabaseUtil = new LogForDeviceDatabaseUtil( LOGGER, this, device, dataDir.getAbsolutePath() );
       if( logDatabaseUtil.createConnection() == null )
       {
         logDatabaseUtil = null;
         return;
       }
       // Eine Liste der Dives lesen
       LOGGER.log( Level.FINE, "read dive list for device from DB..." );
       Vector<String[]> entrys = logDatabaseUtil.getDiveListForDevice( device );
       if( entrys.size() < 1 )
       {
         LOGGER.log( Level.INFO, "no dives for device <" + deviceAlias + "/" + device + "> found in DB." );
         clearDiveComboBox();
         logDatabaseUtil.closeDB();
         logDatabaseUtil = null;
         return;
       }
       //
       // Objekt für das Modell erstellen
       Vector<String[]> diveEntrys = new Vector<String[]>();
       // die erfragten details zurechtrücken
       // Felder sind:
       // H_DIVEID,
      // H_H_DIVENUMBERONSPX
       // H_STARTTIME,
       for( Enumeration<String[]> enu = entrys.elements(); enu.hasMoreElements(); )
       {
         String[] origSet = enu.nextElement();
         // zusammenbauen fuer Anzeige
        String[] elem = new String[3];
        // SPX-DiveNumber etwas einrücken, für vierstellige Anzeige
        elem[1] = String.format( "%4s", origSet[1] );
         // Die UTC-Zeit als ASCII/UNIX wieder zu der originalen Zeit für Java zusammenbauen
         try
         {
           // LOGGER.log( Level.FINE, "unix Timestamp <" + origSet[1] + ">..." );
          javaTime = Long.parseLong( origSet[2] ) * 1000;
           dateTime = new DateTime( javaTime );
          elem[0] = origSet[0];
          elem[2] = dateTime.toString( stringsBundle.getString( "MainCommGUI.timeFormatterString" ) );
         }
         catch( NumberFormatException ex )
         {
           LOGGER.log( Level.SEVERE, "Number format exception (value=<" + origSet[1] + ">: <" + ex.getLocalizedMessage() + ">" );
           elem[1] = "error";
         }
         diveEntrys.add( elem );
       }
       // aufräumen
       entrys.clear();
       entrys = null;
       // die box initialisieren
       LogListComboBoxModel listBoxModel = new LogListComboBoxModel( diveEntrys );
       diveSelectComboBox.setModel( listBoxModel );
       if( diveEntrys.size() > 0 )
       {
         diveSelectComboBox.setSelectedIndex( 0 );
       }
       logDatabaseUtil.closeDB();
       logDatabaseUtil = null;
     }
     else
     {
       LOGGER.log( Level.FINE, "no device found...." );
     }
   }
 
   /**
    * 
    * Die Combobox mit einem leeren Modell ausstatten...
    * 
    * Project: SubmatixBTForPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de)
    * 
    *         Stand: 03.07.2012
    */
   public void clearDiveComboBox()
   {
     Vector<String[]> diveEntrys = new Vector<String[]>();
     LogListComboBoxModel listBoxModel = new LogListComboBoxModel( diveEntrys );
     diveSelectComboBox.setModel( listBoxModel );
   }
 
   /**
    * 
    * Initialisiere alle Felder, Objekte, die zur grafischen Darstellung gebraucht werden
    * 
    * Project: SubmatixBTForPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de)
    * 
    *         Stand: 26.06.2012
    * @param connDev
    *          Falls verbunden, das aktuelle Gerät übergeben (für Voreinstellungen)
    * @param dDir
    * @throws Exception
    */
   public void initGraph( String connDev, File dDir ) throws Exception
   {
     String connDevAlias = null;
     //
     // entsorge für alle Fälle das Zeug von vorher
     releaseGraph();
     dataDir = dDir;
     if( connDev == null )
     {
       LOGGER.log( Level.FINE, "init graphic objects whitout active Device" );
     }
     else
     {
       LOGGER.log( Level.FINE, "init graphic objects Device <" + connDev + ">..." );
     }
     //
     // Ist überhaupt eine Datenbank zum Auslesen vorhanden?
     //
     if( dbUtil == null || ( !dbUtil.isOpenDB() ) )
     {
       throw new Exception( "no database object initiated!" );
     }
     // Alias fürs Gerät
     if( connDev != null )
     {
       connDevAlias = dbUtil.getAliasForName( connDev );
       LOGGER.log( Level.FINE, "Device <" + connDev + "> has alias <" + connDevAlias + ">..." );
     }
     //
     // Lese eine Liste der Tauchgänge für dieses Gerät
     //
     String[] entrys = dbUtil.readDevicesFromDatabase();
     if( entrys == null )
     {
       LOGGER.log( Level.WARNING, "no devices found in database." );
       if( stringsBundle != null )
       {
         showWarnBox( stringsBundle.getString( "spx42LogGraphPanel.warnBox.noDevicesInDatabase" ) );
       }
       return;
     }
     //
     // fülle deviceComboBox
     //
     ComboBoxModel portBoxModel = new DefaultComboBoxModel( entrys );
     deviceComboBox.setModel( portBoxModel );
     if( entrys.length > 1 )
     {
       // wenn kein Alias da ist, brauch ich auchnicht zu suchen
       if( connDevAlias != null )
       {
         // Alle Einträge testen
         for( int index = 0; index < entrys.length; index++ )
         {
           if( entrys[index].equals( connDevAlias ) )
           {
             deviceComboBox.setSelectedIndex( index );
             LOGGER.log( Level.FINE, "device alias found and set as index für combobox..." );
             break;
           }
         }
       }
     }
     //
     // gibt es einen Eintrag in der Combobox müßte ja deren entsprechende Liste von
     // Einträgen gelesen werden....
     //
     if( deviceComboBox.getSelectedIndex() != -1 )
     {
       connDevAlias = ( String )deviceComboBox.getSelectedItem();
       if( connDevAlias != null )
       {
         fillDiveComboBox( connDevAlias );
       }
     }
   }
 
   /**
    * 
    * Gib alle Felder,Objekte frei, die zur grafischen Darstellung gebraucht wurden, falls vorhanden
    * 
    * Project: SubmatixBTForPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de)
    * 
    *         Stand: 26.06.2012
    */
   public void releaseGraph()
   {
     LOGGER.log( Level.FINE, "release graphic objects..." );
     if( chartPanel != null )
     {
       chartPanel.removeAll();
       chartPanel.setEnabled( false );
       chartPanel.setVisible( false );
       remove( chartPanel );
       chartPanel = null;
     }
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
       LOGGER.log( Level.SEVERE, "ERROR showWarnDialog <" + ex.getMessage() + "> ABORT!" );
       return;
     }
     catch( MissingResourceException ex )
     {
       LOGGER.log( Level.SEVERE, "ERROR showWarnDialog <" + ex.getMessage() + "> ABORT!" );
       return;
     }
     catch( ClassCastException ex )
     {
       LOGGER.log( Level.SEVERE, "ERROR showWarnDialog <" + ex.getMessage() + "> ABORT!" );
       return;
     }
   }
 
   /**
    * 
    * Erzeuge ein XY-Dataset aus einem Vector
    * 
    * Project: SubmatixBTForPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de)
    * 
    *         Stand: 03.07.2012
    * @param diveList
    * @param x
    *          X-Achse (Sekundenoffset)
    * @param y
    *          Y-Achse
    * @return Datenset
    */
   private XYDataset createXYDataset( String scalaTitle, Vector<Integer[]> diveList, int x, int y, boolean isDepth )
   {
     final TimeSeries series = new TimeSeries( scalaTitle );
     long milis = 0;
     Integer[] dataSet;
     Date cDate = new Date( milis );
     //
     // alle Datensätze abklappern
     //
     for( Enumeration<Integer[]> enu = diveList.elements(); enu.hasMoreElements(); )
     {
       dataSet = enu.nextElement();
       if( isDepth )
       {
         double fDepth = new Double( dataSet[y] );
         fDepth = 0.00 - ( fDepth / 10.00 );
         series.add( new Second( cDate ), fDepth );
       }
       else
       {
         series.add( new Second( cDate ), new Double( dataSet[y] ) );
       }
       // das offset/schrittweite ist in Sekunden gespeichert
       milis += dataSet[x] * 1000;
       cDate.setTime( milis );
     }
     final TimeSeriesCollection dataset = new TimeSeriesCollection();
     dataset.addSeries( series );
     return dataset;
   }
 
   /**
    * 
    * Zeichne die eigentliche Grafik
    * 
    * Project: SubmatixBTForPC Package: de.dmarcini.submatix.pclogger.gui
    * 
    * @author Dirk Marciniak (dirk_marciniak@arcor.de)
    * 
    *         Stand: 03.07.2012
    * @param dbId
    *          dbid in Tabelle D_TABLE_DIVEDETAIL
    * @param device
    */
   private void makeGraphForLog( int dbId, String device )
   {
     LogForDeviceDatabaseUtil logDatabaseUtil;
     Vector<Integer[]> diveList;
     XYPlot thePlot;
     XYDataset depthDataSet, tempDataSet, ppo2DataSet;
     JFreeChart logChart;
     // das alte Zeug entsorgen
     releaseGraph();
     //
     // richtige Datenbank öffnen
     //
     // das Datenbankutility initialisieren
     logDatabaseUtil = new LogForDeviceDatabaseUtil( LOGGER, this, device, dataDir.getAbsolutePath() );
     if( logDatabaseUtil.createConnection() == null )
     {
       // Tja, das ging schief
       logDatabaseUtil = null;
       showWarnBox( stringsBundle.getString( "spx42LogGraphPanel.warnBox.noDiveDataFound" ) );
       return;
     }
     // Daten eines TG lesen
     LOGGER.log( Level.FINE, "read dive log from DB..." );
     diveList = logDatabaseUtil.readDiveDataFromId( dbId );
     if( diveList == null || diveList.isEmpty() )
     {
       return;
     }
     //
     // einen Plot machen (Grundlage des Diagramms)
     //
     LOGGER.log( Level.FINE, "create graph..." );
     thePlot = new XYPlot();
     //
     // Datumsachse umformatieren
     //
     final DateAxis axis = new DateAxis( stringsBundle.getString( "spx42LogGraphPanel.graph.dateAxisTitle" ) );
     axis.setDateFormatOverride( new SimpleDateFormat( "mm:ss" ) );
     thePlot.setDomainAxis( axis );
     //
     // Temperatur einfügen
     //
     LOGGER.log( Level.FINE, "create temp dataset" );
     tempDataSet = createXYDataset( stringsBundle.getString( "spx42LogGraphPanel.graph.tempScalaTitle" ), diveList, 0, 2, false );
     final NumberAxis tempAxis = new NumberAxis( stringsBundle.getString( "spx42LogGraphPanel.graph.tempAxisTitle" ) );
     tempAxis.setNumberFormatOverride( new DecimalFormat( "###.##" ) );
     final XYLineAndShapeRenderer lineTemperatureRenderer = new XYLineAndShapeRenderer( true, true );
     lineTemperatureRenderer.setSeriesPaint( 0, Color.RED );
     lineTemperatureRenderer.setSeriesShapesVisible( 0, false );
     lineTemperatureRenderer.setDrawSeriesLineAsPath( true );
     tempAxis.setAutoRangeIncludesZero( true );
     thePlot.setRangeAxis( 0, tempAxis );
     thePlot.mapDatasetToRangeAxis( 0, 0 );
     thePlot.setDataset( 0, tempDataSet );
     thePlot.setRenderer( 0, lineTemperatureRenderer );
     //
     // Partialdruck einfügen
     //
     LOGGER.log( Level.FINE, "create ppo2 dataset" );
     ppo2DataSet = createXYDataset( stringsBundle.getString( "spx42LogGraphPanel.graph.ppo2ScalaTitle" ), diveList, 0, 3, true );
     final NumberAxis ppo2Axis = new NumberAxis( stringsBundle.getString( "spx42LogGraphPanel.graph.ppo2AxisTitle" ) );
     final XYLineAndShapeRenderer ppo2Renderer = new XYLineAndShapeRenderer( true, true );
     ppo2Axis.setAutoRangeIncludesZero( true );
     thePlot.setRangeAxis( 1, ppo2Axis );
     thePlot.setDataset( 1, ppo2DataSet );
     thePlot.mapDatasetToRangeAxis( 1, 1 );
     ppo2Renderer.setSeriesPaint( 0, Color.CYAN );
     ppo2Renderer.setSeriesShapesVisible( 0, false );
     ppo2Renderer.setDrawSeriesLineAsPath( true );
     thePlot.setRenderer( 1, ppo2Renderer );
     //
     // die tiefe einfügen
     //
     LOGGER.log( Level.FINE, "create depth dataset" );
     depthDataSet = createXYDataset( stringsBundle.getString( "spx42LogGraphPanel.graph.depthScalaTitle" ), diveList, 0, 1, true );
     final NumberAxis depthAxis = new NumberAxis( stringsBundle.getString( "spx42LogGraphPanel.graph.depthAxisTitle" ) );
     final XYAreaRenderer areaDepthRenderer = new XYAreaRenderer( XYAreaRenderer.AREA );
     depthAxis.setAutoRangeIncludesZero( true );
     thePlot.setRangeAxis( 2, depthAxis );
     thePlot.setDataset( 2, depthDataSet );
     thePlot.mapDatasetToRangeAxis( 2, 2 );
     areaDepthRenderer.setSeriesPaint( 0, new Color( 0xa0a0ff ) );
     thePlot.setRenderer( 2, areaDepthRenderer );
     //
     // ein Chart zur Anzeige in einem Panel erzeugen
     //
     logChart = new JFreeChart( stringsBundle.getString( "spx42LogGraphPanel.graph.chartTitle" ), thePlot );
     //
     // ein Diagramm-Panel erzeugen
     //
     chartPanel = new ChartPanel( logChart );
     add( chartPanel, BorderLayout.CENTER );
     chartPanel.paint( chartPanel.getGraphics() );
    logChart.fireChartChanged();
     LOGGER.log( Level.FINE, "create graph...OK" );
   }
 }
