 package no.ctryti.dagensatuio;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import android.util.Log;
 
 public abstract class Settings {
 	
 	private Settings() {} /* Prevent instantiation */
 	
 	public static final String TAG = "DagensAtUiO";
 	
 	public enum Place {
 		/*
 		 * Special cases, these don't have the same format on the page, so can't
 		 * be parsed with the current parser
 		 */
 		/*
 		NIH ("Norges idrettshgskoles kafe","http://www.sio.no/wps/wcm/connect/migration/sio/mat+og+drikke/dagens+middag/nih"),
 		OLE ("Kafe Ole","http://www.sio.no/wps/wcm/connect/migration/sio/mat+og+drikke/dagens+middag/kafe+ole"),
 		MHS ("Musikkhgskolens kafe","http://www.sio.no/wps/wcm/connect/migration/sio/mat+og+drikke/dagens+middag/musikkhogskolens+kafe+ny"),
 		NVH("Norges veterinrhgskole","http://www.sio.no/wps/wcm/connect/migration/sio/mat+og+drikke/dagens+middag/veterinerhogskolen"),
 		 */
 		AHO        ("AHO-kafeen","http://www.sio.no/wps/wcm/connect/migration/sio/mat+og+drikke/dagens+middag/aho"), 
 		FORSKNINGSV("Forskningsveien","http://www.sio.no/wps/wcm/connect/migration/sio/mat+og+drikke/dagens+middag/forskningsveien"), 
		FREDERIKKE ("Frederikke kaf","http://www.sio.no/wps/wcm/connect/migration/sio/mat+og+drikke/dagens+middag/frederikke+kafe"), 
 		IFI        ("Informatikkafeen","http://www.sio.no/wps/wcm/connect/migration/sio/mat+og+drikke/dagens+middag/informatikkafeen+ny"), 
 		HELGA      ("Kafe Helga","http://www.sio.no/wps/wcm/connect/migration/sio/mat+og+drikke/dagens+middag/kafe+helga"), 
 		NOVA       ("Kafe Nova","http://www.sio.no/wps/wcm/connect/migration/sio/mat+og+drikke/dagens+middag/kafe+nova"), 
 		SEILDUKEN  ("Kafe Seilduken","http://www.sio.no/wps/wcm/connect/migration/sio/mat+og+drikke/dagens+middag/kafe+seilduken"), 
 		SV         ("SV Kafeen","http://www.sio.no/wps/wcm/connect/migration/sio/mat+og+drikke/dagens+middag/sv+kafeen+ny"),
 		ODONTOLOGI ("Odontologikafeen","http://www.sio.no/wps/wcm/connect/migration/sio/mat+og+drikke/dagens+middag/odontologikafeen"), 
 		PREKLINISK ("Preklinisk kafe","http://www.sio.no/wps/wcm/connect/migration/sio/mat+og+drikke/dagens+middag/preklinisk+kafe"), 
 		;
 
 		private final String name;
 		private final URI uri;
 
 		Place(String name, String uri) {
 			URI tmp = null;
 			try {
 				tmp = new URI(uri);
 			} catch (URISyntaxException e) {
 				Log.e(TAG, "Could not create URI: " + uri);
 			}
 			this.uri = tmp;
 			this.name = name;
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public URI getURI() {
 			return uri;
 		}
 	}
 }
