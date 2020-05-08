 /**
  * 
  */
 package ecologylab.standalone.wifiGpsControls;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.IOException;
 import java.util.EnumSet;
 import java.util.TooManyListenersException;
 
 import javax.swing.BoxLayout;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 import stec.jenie.NativeException;
 import ecologylab.appframework.ApplicationEnvironment;
 import ecologylab.appframework.PropertiesAndDirectories;
 import ecologylab.collections.Scope;
 import ecologylab.net.NetTools;
 import ecologylab.sensor.location.NMEAReader;
 import ecologylab.sensor.location.gps.data.GPSConstants;
 import ecologylab.sensor.location.gps.data.GPSDatum;
 import ecologylab.sensor.location.gps.gui.GPSConnectionControls;
 import ecologylab.sensor.location.gps.gui.GPSController;
 import ecologylab.sensor.location.gps.gui.meter.GPSArcConstellationMeter;
import ecologylab.sensor.location.gps.listener.GPSDataPrinter;
 import ecologylab.sensor.location.gps.listener.GPSDataUpdatedListener;
 import ecologylab.sensor.location.gps.listener.GPSDataUpdater;
 import ecologylab.sensor.network.wireless.RunnableWiFiAdapter;
 import ecologylab.sensor.network.wireless.gui.WiFiAdapterConnectionControls;
 import ecologylab.sensor.network.wireless.gui.WiFiConnectionController;
 import ecologylab.services.distributed.server.varieties.KmlServer;
 import ecologylab.services.logging.Logging;
 import ecologylab.services.logging.WiFiGPSStatusOp;
 import ecologylab.services.messages.DefaultServicesTranslations;
 import ecologylab.xml.TranslationScope;
 import ecologylab.xml.XMLTranslationException;
 import ecologylab.xml.library.geom.Rectangle2DDoubleState;
 import ecologylab.xml.library.kml.Kml;
 import gnu.io.CommPortIdentifier;
 import gnu.io.NoSuchPortException;
 import gnu.io.PortInUseException;
 import gnu.io.UnsupportedCommOperationException;
 
 /**
  * @author Zachary O. Toups (toupsz@cs.tamu.edu)
  * 
  */
 public class WiFiGPSControls extends ApplicationEnvironment implements GPSDataUpdatedListener,
 		ActionListener, WindowListener, GPSController, WiFiConnectionController
 {
 	JFrame									mainFrame;
 
 	GPSConstants						currentGPSReportedPos;
 
 	Rectangle2DDoubleState	virtualField;
 
 	Timer										t;
 
 	/**
 	 * The GPS object will come from the GPS controls.
 	 */
 	NMEAReader											gps;
 
 	GPSDataUpdater					updater		= new GPSDataUpdater();
 
 	/**
 	 * The WiFiAdapter, which doesn't require any extra special controls to start it up will be
 	 * instantiated in this class.
 	 */
 	RunnableWiFiAdapter			wifi;
 
 	KmlServer								kmlServer;
 
 	Logging									logging;
 
 	Kml											kmlData;
 
 	GPSDatum								datum;
 
 	WiFiGPSStatusOp					currentOp	= new WiFiGPSStatusOp();
 
 	int											w					= 400;
 
 	int											h					= 200;
 
 	/**
 	 * @param applicationName
 	 * @throws XMLTranslationException
 	 * @throws IOException
 	 * @throws NoSuchPortException
 	 */
 	public WiFiGPSControls(String applicationName) throws XMLTranslationException,
 			NoSuchPortException, IOException
 	{
 		super(applicationName);
 
 		configure();
 	}
 
 	/**
 	 * @param applicationName
 	 * @param translationSpace
 	 * @param args
 	 * @param prefsAssetVersion
 	 * @throws XMLTranslationException
 	 * @throws IOException
 	 * @throws NoSuchPortException
 	 */
 	public WiFiGPSControls(String applicationName, TranslationScope translationSpace, String[] args,
 			float prefsAssetVersion) throws XMLTranslationException, NoSuchPortException, IOException
 	{
 		super(applicationName, translationSpace, args, prefsAssetVersion);
 
 		configure();
 	}
 
 	/**
 	 * @param applicationName
 	 * @param args
 	 * @throws XMLTranslationException
 	 * @throws IOException
 	 * @throws NoSuchPortException
 	 */
 	public WiFiGPSControls(String applicationName, String[] args) throws XMLTranslationException,
 			NoSuchPortException, IOException
 	{
 		super(applicationName, args);
 
 		configure();
 	}
 
 	/**
 	 * @param baseClass
 	 * @param applicationName
 	 * @param args
 	 * @throws XMLTranslationException
 	 * @throws IOException
 	 * @throws NoSuchPortException
 	 */
 	public WiFiGPSControls(Class baseClass, String applicationName, String[] args)
 			throws XMLTranslationException, NoSuchPortException, IOException
 	{
 		super(baseClass, applicationName, args);
 
 		configure();
 	}
 
 	/**
 	 * @param baseClass
 	 * @param applicationName
 	 * @param translationSpace
 	 * @param args
 	 * @param prefsAssetVersion
 	 * @throws XMLTranslationException
 	 * @throws IOException
 	 * @throws NoSuchPortException
 	 */
 	public WiFiGPSControls(Class baseClass, String applicationName,
 			TranslationScope translationSpace, String[] args, float prefsAssetVersion)
 			throws XMLTranslationException, NoSuchPortException, IOException
 	{
 		super(baseClass, applicationName, translationSpace, args, prefsAssetVersion);
 
 		configure();
 	}
 
 	private void configure() throws NoSuchPortException, IOException
 	{
 		debug("setting up GPS");
 		this.gps = new NMEAReader();
 		this.gps.addGPSDataListener(this.currentOp);
 		this.gps.addGPSDataListener(updater);
		
		debug("setting up gps printer");
		GPSDataPrinter p = new GPSDataPrinter();
		this.gps.addGPSDataListener(p);
 
 		debug("setting up datum");
 		GPSDataUpdater d = new GPSDataUpdater();
 		this.gps.addGPSDataListener(d);
 		datum = d.getDatum();
 
 		debug("setting up WiFi");
 		this.wifi = new RunnableWiFiAdapter(1000);
 		this.wifi.addListener(this.currentOp);
 
 		debug("setting up logging");
 		this.logging = new Logging("GPSWiFiData " + System.currentTimeMillis() + ".xml", false, 10,
 				Logging.LOG_TO_MEMORY_MAPPED_FILE, null, 0);
 		this.logging.start();
 
 		debug("constructing KML data");
 		kmlData = WiFiGPSKMLDataManager.configureKml(wifi, gps);
 
 		debug("launching KML server");
 		TranslationScope serverTranslations = DefaultServicesTranslations.get();
 		this.kmlServer = new KmlServer(8080, NetTools.getAllInetAddressesForLocalhost(),
 				serverTranslations, new Scope(), 1000000, 1000000, kmlData);
 		kmlServer.start();
 
 		debug("configuring visualizer");
 		configureFromPrefs();
 		setupControls();
 
 		debug("starting log recording at 1Hz");
 		t = new Timer(1000, this);
 		t.start();
 	}
 
 	/**
 	 * 
 	 */
 	private void setupControls()
 	{
 		JPanel gpsParts = new JPanel();
 		JPanel wiFiParts = new JPanel();
 
 		gpsParts.setLayout(new BoxLayout(gpsParts, BoxLayout.PAGE_AXIS));
 		wiFiParts.setLayout(new BoxLayout(wiFiParts, BoxLayout.PAGE_AXIS));
 
 		this.mainFrame = new JFrame(PropertiesAndDirectories.applicationName());
 
 		this.mainFrame.addWindowListener(this);
 
 		this.updater.addDataUpdatedListener(this);
 
 		gpsMeter = new GPSArcConstellationMeter(datum);
 
 		gpsMeter.setPreferredSize(new Dimension(350, 140));
 
 		GPSConnectionControls gpsControls = new GPSConnectionControls(this);
 		gpsControls.setPreferredSize(new Dimension(350, 200));
 
 		WiFiAdapterConnectionControls wifiControls = new WiFiAdapterConnectionControls(this);
 		wifiControls.setPreferredSize(new Dimension(350, 200));
 
 		gpsParts.add(gpsControls);
 		gpsParts.add(gpsMeter);
 
 		wiFiParts.add(wifiControls);
 
 		this.mainFrame.getContentPane().add(gpsParts);
 		this.mainFrame.getContentPane().add(wiFiParts);
 
 		this.mainFrame.setLayout(new FlowLayout());
 
 		this.mainFrame.setVisible(true);
 		this.mainFrame.setSize(w, h + 200);
 		this.mainFrame.pack();
 		this.mainFrame.invalidate();
 	}
 
 	/**
 	 * @throws IOException
 	 * @throws NoSuchPortException
 	 * 
 	 */
 	private void configureFromPrefs() throws NoSuchPortException, IOException
 	{
 	}
 
 	/**
 	 * @throws NoSuchPortException
 	 * @see ecologylab.sensor.location.gps.gui.GPSController#connectGPS(ecologylab.sensor.location.NMEAReader)
 	 */
 	public boolean connectGPS(CommPortIdentifier portId, int baud) throws PortInUseException,
 			UnsupportedCommOperationException, IOException, TooManyListenersException,
 			NoSuchPortException
 	{
 		this.gps.disconnect();
 
 		this.gps.setup(portId, baud);
 
 		this.gps.connect();
 
 		return this.gps.connected();
 	}
 
 	/**
 	 * @see ecologylab.sensor.location.gps.gui.GPSController#disconnectGPS()
 	 */
 	public void disconnectGPS()
 	{
 		this.gps.disconnect();
 	}
 
 	/**
 	 * @param args
 	 * @throws XMLTranslationException
 	 * @throws IOException
 	 * @throws NoSuchPortException
 	 */
 	public static void main(String[] args) throws XMLTranslationException, NoSuchPortException,
 			IOException
 	{
 		new WiFiGPSControls("Control Panel");
 	}
 
 	/**
 	 * @see ecologylab.sensor.location.gps.listener.GPSDataUpdatedListener#gpsDatumUpdated(ecologylab.sensor.location.gps.data.GPSDatum)
 	 */
 	public void gpsDatumUpdated(GPSDatum datum)
 	{
 		this.currentGPSReportedPos = datum;
 	}
 
 	/**
 	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 	public void actionPerformed(ActionEvent e)
 	{
 		this.logging.logAction(this.currentOp);
 
 		mainFrame.repaint();
 	}
 
 	/**
 	 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
 	 */
 	public void windowActivated(WindowEvent e)
 	{
 	}
 
 	/**
 	 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
 	 */
 	public void windowClosed(WindowEvent e)
 	{
 	}
 
 	/**
 	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
 	 */
 	public void windowClosing(WindowEvent e)
 	{
 		debug("Window closed.");
 
 		if (gps.connected())
 		{
 			gps.disconnect();
 		}
 
 		if (wifi.connected())
 		{
 			wifi.disconnect();
 		}
 
 		t.stop();
 
 		this.logging.stop();
 
 		System.exit(1);
 	}
 
 	/**
 	 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
 	 */
 	public void windowDeactivated(WindowEvent e)
 	{
 	}
 
 	/**
 	 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
 	 */
 	public void windowDeiconified(WindowEvent e)
 	{
 	}
 
 	/**
 	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
 	 */
 	public void windowIconified(WindowEvent e)
 	{
 	}
 
 	/**
 	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
 	 */
 	public void windowOpened(WindowEvent e)
 	{
 	}
 
 	/**
 	 * @see ecologylab.sensor.location.gps.gui.GPSController#getGps()
 	 */
 	public NMEAReader getGps()
 	{
 		return gps;
 	}
 
 	/**
 	 * Indicates which GPS update operations this is interested in; in this case, all of them.
 	 */
 	private static EnumSet<GPSUpdateInterest>	interestSet	= EnumSet.of(GPSUpdateInterest.LAT_LON,
 																														GPSUpdateInterest.ALT,
 																														GPSUpdateInterest.OTHERS);
 
 	private GPSArcConstellationMeter					gpsMeter;
 
 	public EnumSet<GPSUpdateInterest> getInterestSet()
 	{
 		return interestSet;
 	}
 
 	public boolean connectWiFi() throws NativeException
 	{
 		this.wifi.connect();
 
 		return wifi.connected();
 	}
 
 	public void disconnectWiFi()
 	{
 		this.wifi.disconnect();
 	}
 
 	public RunnableWiFiAdapter getWiFiAdapter()
 	{
 		return wifi;
 	}
 }
