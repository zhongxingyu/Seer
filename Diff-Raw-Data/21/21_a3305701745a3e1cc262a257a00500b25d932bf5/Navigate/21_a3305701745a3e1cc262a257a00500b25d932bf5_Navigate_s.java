 package ecologylab.services.messages;
 
 import java.applet.AppletContext;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import netscape.javascript.JSObject;
 
 import ecologylab.generic.Debug;
 import ecologylab.generic.ObjectRegistry;
 import ecologylab.generic.ParsedURL;
 
 /**
  * A message sent to the browser server to perform the navigation service.
  * 
  * @author blake
  */
 public class Navigate 
 extends RequestMessage
 implements MessageTypes
 {
 	public ParsedURL purl;
 	
 	public Navigate() {}
 	
 	public Navigate(ParsedURL purl)
 	{
 		this.purl 	= purl;
 	}
 
 	public ResponseMessage performService(ObjectRegistry objectRegistry)
 	{
 		if (purl == null)
 		{
			final String msg = "Incoming \"navigate\" packets require a URL argument";
			debug(msg);
			return new BadSemanticContentResponse(msg);
 		}
 			
 		try
 		{
 			//grab the objects we need.
 		    AppletContext 	appletContext 	= (AppletContext) objectRegistry.lookupObject("appletContext");
 		    JSObject 		jsWindow		= (JSObject) objectRegistry.lookupObject("jsWindow");
 		    String			goPrefix		= (String) objectRegistry.lookupObject("goPrefix");
 		    String			frame			= (String) objectRegistry.lookupObject("frame");
 			
 		    if (jsWindow != null)
 		    {
 //			    	create a URL so that we KNOW the address is well-formed.
 			   Debug.println("!!!use JSObject to navigate to " + purl);
 			   jsWindow.call("cmPopup", new Object[] {purl.toString()});
 		    }
 		    else if (appletContext != null)
 		    {
 //			          if (goPrefix != null)	   // popup.html?href=
 //			     		 purl = ParsedURL.getRelative((relToDoc? docBaseURL : codeBaseURL), (goPrefix+purl),
 //			     									   "Cant find " + (goPrefix+purl) +
 //			     									   " relative to " + docBaseURL.toExternalForm() + " "); 		 Debug.println("Playlet.go() USING popup.html cause JSObject=null");
 		    	URL url = new URL(goPrefix + purl);
 				Debug.println("!!!use goPrefix to navigate to " + url);
 				appletContext.showDocument(url, frame);
 		    }
 
			System.out.println("finished navigating");
 		}
 		catch (MalformedURLException e)
 		{
			System.err.println("BrowserServer: Malformed URL received: " + purl);
 			e.printStackTrace();
			return ResponseMessage.BADTransmissionResponse();
 		}
 		catch (Exception e)
 		{
			System.out.println("BrowserServer error: ");
 			e.printStackTrace();
 		}
		return ResponseMessage.OKResponse();
 	}
 }
