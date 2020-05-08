 package org.cluenet.cluebot.reviewinterface.server;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 
 @SuppressWarnings( "serial" )
 public class APIImpl extends HttpServlet {
 	private Element getUser( Document doc, String key ) {
 		User user = User.findByKey( key );
 		Element element = doc.createElement( "User" );
 		
 		Element ekey = doc.createElement( "Key" );
 		ekey.appendChild( doc.createTextNode( KeyFactory.keyToString( user.getKey() ) ) );
 		element.appendChild( ekey );
 		
 		Element nick = doc.createElement( "Nick" );
 		nick.appendChild( doc.createTextNode( user.getNick() ) );
 		element.appendChild( nick );
 		
 		Element classifications = doc.createElement( "Classifications" );
 		classifications.appendChild( doc.createTextNode( user.getClassifications().toString() ) );
 		element.appendChild( classifications );
 		
 		return element;
 	}
 	
 	private Element processGetUser( Document doc, HttpServletRequest req ) {
 		Element element = doc.createElement( "GetUser" );
 		for( String key : req.getParameter( "guKeys" ).split( ":" ) )
 			element.appendChild( getUser( doc, key ) );
 		return element;
 	}
 	
 	private Element processListUsers( Document doc, HttpServletRequest req ) {
 		Element element = doc.createElement( "ListUsers" );
 		for( User user : User.list() ) {
 			Element userElement = doc.createElement( "UserKey" );
 			userElement.appendChild( doc.createTextNode( KeyFactory.keyToString( user.getKey() ) ) );
 			element.appendChild( userElement );
 		}
 		return element;
 	}
 	
 	private Element getEdit( Document doc, String key ) {
 		Edit edit = Edit.findByKey( key );
 		Element element = doc.createElement( "Edit" );
 		
 		Element ekey = doc.createElement( "Key" );
 		ekey.appendChild( doc.createTextNode( KeyFactory.keyToString( edit.getKey() ) ) );
 		element.appendChild( ekey );
 		
 		Element id = doc.createElement( "ID" );
 		id.appendChild( doc.createTextNode( edit.getId().toString() ) );
 		element.appendChild( id );
 		
 		Element weight = doc.createElement( "Weight" );
 		weight.appendChild( doc.createTextNode( edit.getWeight().toString() ) );
 		element.appendChild( weight );
 		
 		Element required = doc.createElement( "Required" );
 		required.appendChild( doc.createTextNode( edit.getRequired().toString() ) );
 		element.appendChild( required );
 		
 		Element constructive = doc.createElement( "Constructive" );
 		constructive.appendChild( doc.createTextNode( edit.getConstructive().toString() ) );
 		element.appendChild( constructive );
 		
 		Element skipped = doc.createElement( "Skipped" );
 		skipped.appendChild( doc.createTextNode( edit.getSkipped().toString() ) );
 		element.appendChild( skipped );
 		
 		Element vandalism = doc.createElement( "Vandalism" );
 		vandalism.appendChild( doc.createTextNode( edit.getVandalism().toString() ) );
 		element.appendChild( vandalism );
 		
 		Element classification = doc.createElement( "Classification" );
 		classification.appendChild( doc.createTextNode( edit.getClassification().toString() ) );
 		element.appendChild( classification );
 		
 		Element comments = doc.createElement( "Comments" );
 		for( String comment : edit.getComments() ) {
 			Element commentElement = doc.createElement( "Comment" );
 			commentElement.appendChild( doc.createTextNode( comment ) );
 			comments.appendChild( commentElement );
 		}
 		element.appendChild( comments );
 		
 		Element users = doc.createElement( "Users" );
 		for( Key userKey : edit.getUsers() ) {
 			Element userKeyElement = doc.createElement( "UserKey" );
 			userKeyElement.appendChild( doc.createTextNode( KeyFactory.keyToString( userKey ) ) );
 			users.appendChild( userKeyElement );
 		}
 		element.appendChild( users );
 		
 		return element;
 	}
 	
 	private Element processGetEdit( Document doc, HttpServletRequest req ) {
 		Element element = doc.createElement( "GetEdit" );
 		for( String key : req.getParameter( "geKeys" ).split( ":" ) )
 			element.appendChild( getEdit( doc, key ) );
 		return element;
 	}
 	
 	private Element getEditGroup( Document doc, String key ) {
 		EditGroup eg = EditGroup.findByKey( key );
 		Element element = doc.createElement( "EditGroup" );
 		
 		Element ekey = doc.createElement( "Key" );
 		ekey.appendChild( doc.createTextNode( KeyFactory.keyToString( eg.getKey() ) ) );
 		element.appendChild( ekey );
 		
 		Element name = doc.createElement( "Name" );
 		name.appendChild( doc.createTextNode( eg.getName() ) );
 		element.appendChild( name );
 		
 		Element weight = doc.createElement( "Weight" );
 		weight.appendChild( doc.createTextNode( eg.getWeight().toString() ) );
 		element.appendChild( weight );
 		
 		Element edits = doc.createElement( "Edits" );
 		for( Key editKey : eg.getEdits() ) {
 			Element editKeyElement = doc.createElement( "EditKey" );
 			editKeyElement.appendChild( doc.createTextNode( KeyFactory.keyToString( editKey ) ) );
 			edits.appendChild( editKeyElement );
 		}
 		element.appendChild( edits );
 		
 		Element reviewed = doc.createElement( "Reviewed" );
 		for( Key editKey : eg.getReviewed() ) {
 			Element editKeyElement = doc.createElement( "EditKey" );
 			editKeyElement.appendChild( doc.createTextNode( KeyFactory.keyToString( editKey ) ) );
 			reviewed.appendChild( editKeyElement );
 		}
 		element.appendChild( reviewed );
 		
 		Element done = doc.createElement( "Done" );
 		for( Key editKey : eg.getDone() ) {
 			Element editKeyElement = doc.createElement( "EditKey" );
 			editKeyElement.appendChild( doc.createTextNode( KeyFactory.keyToString( editKey ) ) );
 			done.appendChild( editKeyElement );
 		}
 		element.appendChild( done );
 		
 		return element;
 	}
 	
 	private Element processGetEditGroup( Document doc, HttpServletRequest req ) {
 		Element element = doc.createElement( "GetEditGroup" );
 		for( String key : req.getParameter( "gegKeys" ).split( ":" ) )
 			element.appendChild( getEditGroup( doc, key ) );
 		return element;
 	}
 	
 	private Element processListEditGroups( Document doc, HttpServletRequest req ) {
 		Element element = doc.createElement( "ListEditGroups" );
 		for( EditGroup eg : EditGroup.list() ) {
 			Element editGroupElement = doc.createElement( "EditGroupKey" );
 			editGroupElement.appendChild( doc.createTextNode( KeyFactory.keyToString( eg.getKey() ) ) );
 			element.appendChild( editGroupElement );
 		}
 		return element;
 	}
 	
 	private Element processHelp( Document doc, HttpServletRequest req ) {
 		Element element = doc.createElement( "Help" );
 		
 		String helpMsg = "\nClueBot NG Review Interface API\n" +
 				"\n" +
 				"Options:\n" +
 				"|-Users:\n" +
 				"| |-listUsers - List keys for all users known to the system.\n" +
 				"| | `- http://cluebotreview.g.cluenet.org/api?listUsers\n" +
 				"| `-getUser - Get user information from user key.\n" +
 				"|   |-guKeys - Colon seperated list of user keys.\n" +
 				"|   `- http://cluebotreview.g.cluenet.org/api?getUser&guKeys=key1:key2:...:keyN\n" +
 				"|-Edits:\n" +
 				"| `-getEdit - Get edit information from edit key.\n" +
 				"|   |-geKeys - Colon seperated list of edit keys.\n" +
 				"|   `- http://cluebotreview.g.cluenet.org/api?getEdit&geKeys=key1:key2:...:keyN\n" +
 				"|-EditGroups:\n" +
 				"| |-listEditGroups - List keys for all EditGroups known to the system.\n" +
 				"| | `- http://cluebotreview.g.cluenet.org/api?listEditGroups\n" +
 				"| `-getEditGroup - Get EditGroup information from EditGroup key.\n" +
 				"|   |-gegKeys - Colon seperated list of EditGroup keys.\n" +
 				"|   `- http://cluebotreview.g.cluenet.org/api?getEditGroup&gegKeys=key1:key2:...:keyN\n" +
 				"`-Help:\n" +
 				"  `-help - Get help documentation\n" +
 				"    |- http://cluebotreview.g.cluenet.org/api?help\n" +
 				"    `- http://cluebotreview.g.cluenet.org/api\n";
 		
 		element.appendChild( doc.createTextNode( helpMsg ) );
 		
 		return element;
 	}
 	
 	public void doPost( HttpServletRequest req, HttpServletResponse res ) throws IOException {
 		doGet( req, res );
 	}
 	
 	@SuppressWarnings( "unchecked" )
 	public void doGet( HttpServletRequest req, HttpServletResponse res ) throws IOException {
		res.setContentType( "text/xml" );
 		try {
 			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 			Document doc = docBuilder.newDocument();
 			
 			Element root = doc.createElement( "API" );
 			doc.appendChild( root );
 			
 			Map params = req.getParameterMap();
 			if( params.containsKey( "listUsers" ) )
 				root.appendChild( processListUsers( doc, req ) );
 			if( params.containsKey( "getUser" ) )
 				root.appendChild( processGetUser( doc, req ) );
 			if( params.containsKey( "getEdit" ) )
 				root.appendChild( processGetEdit( doc, req ) );
 			if( params.containsKey( "listEditGroups" ) )
 				root.appendChild( processListEditGroups( doc, req ) );
 			if( params.containsKey( "getEditGroup" ) )
 				root.appendChild( processGetEditGroup( doc, req ) );
 			if( params.size() == 0 || params.containsKey( "help" ) )
 				root.appendChild( processHelp( doc, req ) );
 			
 			TransformerFactory transformerFactory = TransformerFactory.newInstance();
 			Transformer transformer = transformerFactory.newTransformer();
 			DOMSource source = new DOMSource( doc );
 			StreamResult result =  new StreamResult( res.getOutputStream() );
 			transformer.transform(source, result);
 		} catch( Exception e ) {
 			
 		}
 	}
 }
