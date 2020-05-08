 /**
  * 
  */
 package ecologylab.services.distributed.server.varieties;
 
 import java.io.IOException;
 import java.net.BindException;
 import java.net.InetAddress;
 import java.nio.channels.SelectionKey;
 
 import ecologylab.collections.Scope;
 import ecologylab.net.NetTools;
 import ecologylab.services.distributed.server.clientsessionmanager.AbstractClientSessionManager;
 import ecologylab.services.distributed.server.contextmanager.KMLGetClientSessionManager;
 import ecologylab.services.messages.DefaultServicesTranslations;
 import ecologylab.services.messages.KmlRequest;
 import ecologylab.services.messages.KmlResponse;
 import ecologylab.xml.ElementState;
 import ecologylab.xml.TranslationScope;
 import ecologylab.xml.XMLTranslationException;
 import ecologylab.xml.library.kml.KMLTranslations;
 import ecologylab.xml.library.kml.Kml;
 
 /**
  * @author Zach
  * 
  */
 public class KmlServer extends HttpGetServer
 {
 	public static final Class	KML_MESSAGE_CLASSES[]	=
 																		{ KmlRequest.class,
 			KmlResponse.class									};
 	
 	public static String someKml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
 	"<kml xmlns=\"http://earth.google.com/kml/2.2\">\r\n" + 
 	"<Document>\r\n" + 
 	"  <name>BalloonStyle.kml</name>\r\n" + 
 	"  <open>1</open>\r\n" + 
 	"  <Placemark>\r\n" + 
 	"    <name>BalloonStyle</name>\r\n" + 
 	"    <description>An example of BalloonStyle</description>\r\n" + 
 	"    <Point>\r\n" + 
 	"      <coordinates>-122.370533,37.823842,0</coordinates>\r\n" + 
 	"    </Point>\r\n" + 
 	"  </Placemark>\r\n" + 
 	"</Document>\r\n" + 
 	"</kml>";
 
 	/**
 	 * @param portNumber
 	 * @param inetAddresses
 	 * @param requestTranslationSpace
 	 * @param objectRegistry
 	 * @param idleConnectionTimeout
 	 * @param maxPacketSize
 	 * @param kmlData
 	 *           the singleton Kml object that will be sent when Google Earth
 	 *           requests KML. This object can be modified by other parts of the
 	 *           application to change what information is displayed in Google
 	 *           Earth.
 	 * @throws IOException
 	 * @throws BindException
 	 */
 	public KmlServer(int portNumber, InetAddress[] inetAddresses,
 			TranslationScope requestTranslationSpace,
 			Scope objectRegistry, int idleConnectionTimeout,
 			int maxPacketSize, Kml kmlData) throws IOException, BindException
 	{
 		super(portNumber, inetAddresses, TranslationScope.get(
 				"double_threaded_logging " + inetAddresses[0].toString() + ":"
 						+ portNumber, KML_MESSAGE_CLASSES, requestTranslationSpace,
 				KMLTranslations.get()), objectRegistry, idleConnectionTimeout,
 				maxPacketSize);
 
 		this.applicationObjectScope.put(KmlRequest.KML_DATA, kmlData);
 	}
 
 	/**
 	 * @param portNumber
 	 * @param inetAddress
 	 * @param requestTranslationSpace
 	 * @param objectRegistry
 	 * @param idleConnectionTimeout
 	 * @param maxPacketSize
 	 * @throws IOException
 	 * @throws BindException
 	 */
 	public KmlServer(int portNumber, InetAddress inetAddress,
 			TranslationScope requestTranslationSpace,
 			Scope objectRegistry, int idleConnectionTimeout,
 			int maxPacketSize, Kml kmlData) throws IOException, BindException
 	{
 		this(portNumber, addressToAddresses(inetAddress),
 				requestTranslationSpace, objectRegistry, idleConnectionTimeout,
 				maxPacketSize, kmlData);
 	}
 
 	/**
 	 * @param args
 	 * @throws IOException
 	 * @throws BindException
 	 * @throws XMLTranslationException 
 	 */
 	public static void main(String[] args) throws BindException, IOException, XMLTranslationException
 	{
 		TranslationScope serverTranslations = DefaultServicesTranslations.get();
 
 		Kml kmlData = (Kml) ElementState.translateFromXMLCharSequence(someKml, KMLTranslations.get());
 		
 		KmlServer s = new KmlServer(8080, NetTools
 				.getAllInetAddressesForLocalhost(), serverTranslations,
 				new Scope(), 1000000, 1000000, kmlData);
 
 		s.start();
 	}
 
 	@Override protected AbstractClientSessionManager generateContextManager(
 			Object token, SelectionKey sk, TranslationScope translationSpaceIn,
 			Scope registryIn)
 	{
		return new KMLGetClientSessionManager(token, maxPacketSize, this.getBackend(),
 				this, sk, translationSpaceIn, registryIn);
 	}
 }
