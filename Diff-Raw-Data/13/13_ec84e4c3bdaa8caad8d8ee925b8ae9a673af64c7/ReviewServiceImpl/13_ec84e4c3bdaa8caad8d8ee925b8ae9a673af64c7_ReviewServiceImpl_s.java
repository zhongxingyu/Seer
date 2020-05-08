 
 package org.cluenet.cluebot.reviewinterface.server;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.cluenet.cluebot.reviewinterface.client.ReviewService;
 import org.cluenet.cluebot.reviewinterface.shared.Classification;
 import org.cluenet.cluebot.reviewinterface.shared.ClientClassification;
 import org.cluenet.cluebot.reviewinterface.shared.ReturnData;
 
 import com.google.appengine.api.datastore.Email;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.users.UserServiceFactory;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 /**
  * The server side implementation of the RPC service.
  */
 @SuppressWarnings( "serial" )
 public class ReviewServiceImpl extends RemoteServiceServlet implements
 		ReviewService {
 
 	@Override
 	public ReturnData reviewId( Integer id, Classification type, String comment ) throws Exception {
 		try {
 			User user = null;
 			user = User.findByEmail( new Email( UserServiceFactory.getUserService().getCurrentUser().getEmail() ) );
 			user.incClassifications();
 			
 			Edit edit = Edit.findById( id );
 			edit.newClassification( user, type, comment );
 	
 			return getId();
 		} catch( Exception e ) {
 			e.printStackTrace();
 			throw new IllegalArgumentException( e.getMessage() );
 		}
 	}
 
 	@Override
 	public ReturnData getId() throws Exception {
 		try {
 			User user = User.findByEmail( new Email( UserServiceFactory.getUserService().getCurrentUser().getEmail() ) );
 			if( user == null )
 				throw new IllegalArgumentException( "You have no user account." );
			EditGroup eg = EditGroup.getRandomEditGroup();
 			if( eg == null )
 				throw new IllegalArgumentException( "No more edit groups available." );
			Edit randomEdit = eg.getRandomEdit( user );
 			if( randomEdit == null )
 				throw new IllegalArgumentException( "No more edits available for " + eg.getName() + "." );
 			return new ReturnData(
 					randomEdit.getClientClass(),
 					user.getClientClass()
 			);
 		} catch( Exception e ) {
 			e.printStackTrace();
 			throw new IllegalArgumentException( e.getMessage() );
 		}
 	}
 
 	@Override
 	public ReturnData getId( Integer id ) throws Exception {
 		try {
 			User user = User.findByEmail( new Email( UserServiceFactory.getUserService().getCurrentUser().getEmail() ) );
 			if( user == null )
 				throw new IllegalArgumentException( "You have no user account." );
 			Edit edit = Edit.findById( id );
 			if( edit == null )
 				throw new IllegalArgumentException( "No such edit." );
 			return new ReturnData(
 					edit.getClientClass(),
 					user.getClientClass()
 			);
 		} catch( Exception e ) {
 			e.printStackTrace();
 			throw new IllegalArgumentException( e.getMessage() );
 		}
 	}
 
 	@Override
 	public List< ClientClassification > getClassifications( String start, Integer count ) throws Exception {
 		for( Integer i = 0 ; i < 10 ; i++ )
 			try {
 				Key key = null;
 				if( start != null && !start.equals( "" ) )
 					key = KeyFactory.stringToKey( start );
 			
 				List< ClientClassification > list = new ArrayList< ClientClassification >();
 				
 				User user = User.findByEmail( new Email( UserServiceFactory.getUserService().getCurrentUser().getEmail() ) );
 				if( user == null )
 					throw new IllegalArgumentException( "You have no user account." );
 				
 				for( EditClassification ec : EditClassification.findByUser( user, key, count ) )
 					list.add( ec.getClientClass( false, true ) );
 				
 				return list;
 			} catch( Exception e ) {
 				e.printStackTrace();
 				if( i >= 9 )
 					throw new IllegalArgumentException( e.getMessage() );
 			}
 		return null;
 	}
 }
