 /**
  * 
  */
 package controllers;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.SortedSet;
 
 import models.ActionObject;
 import models.ContentType;
 
 import org.apache.commons.io.FilenameUtils;
 import org.apache.tika.config.TikaConfig;
 import org.apache.tika.io.IOUtils;
 import org.apache.tika.mime.MediaType;
 import org.apache.tika.mime.MimeType;
 import org.apache.tika.mime.MimeTypeException;
 import org.apache.tika.mime.MimeTypes;
 
 import play.Logger;
 import play.mvc.Controller;
 import play.mvc.Result;
 import uk.bl.wa.access.qaop.QaopShot;
 
 import com.typesafe.config.Config;
 import com.typesafe.config.ConfigFactory;
 
 /**
  * @author Andrew Jackson <Andrew.Jackson@bl.uk>
  *
  */
 public class Actions extends Controller {
 
     public static Result index() {
         return ok("It works!");
     }
     
     // Should cache properly: http://www.playframework.com/documentation/2.2.1/JavaCache
     // Should async properly: http://www.playframework.com/documentation/2.2.1/JavaAsync
     
     public static Result qaop( String urlparam ) throws IOException { 
     	Logger.warn("URL Param: "+urlparam);
     	URL url = new URL(urlparam);
     	String filename = FilenameUtils.getName(urlparam);
     	File input = File.createTempFile("input", filename);
     	Logger.info("Writing to temp file: "+input.getAbsolutePath());
     	IOUtils.copy(url.openStream(), new FileOutputStream(input));
     	File tmp = File.createTempFile("spectrum", ".png");
     	QaopShot.takeScreenshot(input.getAbsolutePath(), tmp.getAbsolutePath(), 5);
     	Logger.info("Setting headers...");
     	response().setHeader("Content-Disposition", "inline;");
     	response().setContentType("image/png");
     	return ok(tmp);
     }
     
     public static Result types( String type ) {
     	// No type supplied:
 //    	if( type == null ) {
 //    		Logger.info("Should return list of all types.");
 //    		ok();
 //    	}
     	// Only type supplied:
 //    	if (subtype == null ) {
 //    		Logger.info("Should return list of all subtypes.");
 //    		ok();
 //    	}
     	// The ';' is actually valid to interpret as '&', so change it back:
     	type = type.replaceFirst("&",";");
    	type = type.replaceFirst("%20"," ");
     	Logger.info("Got type string "+type);
     	MediaType fulltype = MediaType.parse(type);
     	Logger.info("Got fulltype "+fulltype);
     	Logger.info("Looking up "+fulltype.getBaseType());
     	MimeType mt = null;
     	try {
 			mt = mimeTypes.getRegisteredMimeType(fulltype.getBaseType().toString());
 		} catch (MimeTypeException e) {
 			Logger.error("Unknown type.");
 			e.printStackTrace();
 		}
     	
     	MediaType parent = mimeTypes.getMediaTypeRegistry().getSupertype(fulltype);
     	SortedSet<MediaType> aliases = mimeTypes.getMediaTypeRegistry().getAliases(fulltype);
 		
 		List<MediaType> childTypes = new ArrayList<MediaType>();
 		for( MediaType at : mimeTypes.getMediaTypeRegistry().getTypes() ) {
 			if( fulltype.equals(mimeTypes.getMediaTypeRegistry().getSupertype(at)) ) {
 				childTypes.add(at);
 			}
 		}
 		
 		// Actions
 		List<ActionObject> actions = loadActions(type,"");
 		
 		// Build the page:
     	return ok( views.html.types.render( new ContentType(fulltype, mt, parent, aliases, childTypes, actions) ) );
     }
 
     
 	/* ---- */
 	
     /* Static code for managing types and actions */
     
     /* ---- */
     
 	static MimeTypes mimeTypes = TikaConfig.getDefaultConfig().getMimeRepository();
 	
 	public static List<ActionObject> loadActions( String contentType, String prefix ) {
 		HashMap<String,ActionObject> actions = new HashMap<String,ActionObject>();
 		Config conf = ConfigFactory.load();
 		
 		MediaType type = MediaType.parse(contentType);
 		Logger.info("Looking for actions that match type: "+type);
 
 		for( Config a : conf.getConfigList("actions") ) {
 			ActionObject ao = new ActionObject(a, prefix);
 			//Logger.debug("Looking at action: "+ao.action);
 			
 			// Look for a match:
 			for( String in : a.getStringList("types.in")) {
 				MediaType inType = MediaType.parse(in);
 				
 				if( type.equals(inType) || hasMatchingSupertype(type,inType) ) {
 					// Add to the list of options:
 					actions.put( ao.getAction(), ao);
 				}
 			}
 		}
 		List<ActionObject> actionList = new ArrayList<ActionObject>( actions.values() );
 		Collections.sort(actionList);
 		return actionList;
 	}
 	
 	private static boolean hasMatchingSupertype(MediaType type, MediaType inType) {
 		// Get the Tika type tree:
 		MediaType superType = mimeTypes.getMediaTypeRegistry().getSupertype(type);
 		
 		// IF there's a super-type, see it if matches:
 		if( superType != null ) {
 			if( superType.equals(inType) ) return true;
 			// Otherwise, look for next supertype.
 			return hasMatchingSupertype(superType, inType);
 		}
 		
 		return false;
 	}
 	
 	public static MimeType lookupMimeType( String mimeType ) {
 		MimeType mt = null;
 		try {
 			mt = mimeTypes.getRegisteredMimeType(mimeType);
 		} catch ( MimeTypeException e ) {
 			Logger.warn("Could not find registered type: "+mimeType);
 		}
 		try {
 			mt = mimeTypes.forName(mimeType);
 		} catch ( MimeTypeException e ) {
 			Logger.warn("Could not create type: "+mimeType);
 		}
 		
 		Logger.info("Got mimeType: "+mt);
 		if( mt == null ) return null;
 		
 		return mt;
 	}
 
 }
 
