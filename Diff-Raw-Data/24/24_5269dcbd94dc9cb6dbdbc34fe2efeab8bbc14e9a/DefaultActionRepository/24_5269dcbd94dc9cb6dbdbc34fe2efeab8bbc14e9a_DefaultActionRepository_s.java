 package be.occam.colloseum.action.repository.impl;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.lang.annotation.Annotation;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.ext.MessageBodyReader;
 import javax.ws.rs.ext.MessageBodyWriter;
 import javax.ws.rs.ext.Providers;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Repository;
 
 import be.occam.colloseum.action.Action;
 import be.occam.colloseum.action.ActionDTO;
 import be.occam.colloseum.action.repository.IActionRepository;
 import be.occam.colloseum.application.ProvidersRegistry;
 
 @Repository
 @Scope("singleton")
 public class DefaultActionRepository implements IActionRepository {
 	
 	protected final Logger logger
 		= LoggerFactory.getLogger( this.getClass() );
 	
 	protected final ActionDTO[] array 
 		= {};
 
 	protected MessageBodyWriter<ActionDTO> messageBodyWriter;
 	
 	protected MessageBodyReader<ActionDTO> messageBodyReader;
 	
 	@Value("${colloseum.actions.directory}")
 	protected String directory;
 	
 	@Override
 	public ActionDTO findOne(String id) {
 		return null;
 	}
 	
 	@Override
	public List<ActionDTO> findAll( Providers providers ) {
 		
 		try {
 			
 			if ( this.messageBodyReader == null ) {
 		
 				this.messageBodyReader
 					= ProvidersRegistry.registered().getMessageBodyReader( ActionDTO.class, ActionDTO.class, null, MediaType.APPLICATION_JSON_TYPE );
 			}
 			
 			File directory 
 				= new File( this.directory );
 			
 			List<ActionDTO> list 
 				= new LinkedList<ActionDTO>();
 			
 			
 			File[] files 
 				= directory.listFiles( );
 			
 			for ( File file : files ) {
 				
 				FileInputStream fis
 					= new FileInputStream( file );
 				
 				ActionDTO action 
 					= this.messageBodyReader.readFrom( 
 						ActionDTO.class,
 						ActionDTO.class,
 						new Annotation[] {},
 						MediaType.APPLICATION_JSON_TYPE,
 						null,
 						fis );
 				
 				list.add( action );
 				
 			}
 			
 			return list;
 			
 		}
 		catch( Exception e ) {
 			throw new RuntimeException( e ); 
 		}
 		
 	}
 
 	@Override
 	public ActionDTO persist( ActionDTO action ) {
 		
 		try {
 			
 			if ( this.messageBodyWriter == null ) {
 		
 				this.messageBodyWriter
 					= ProvidersRegistry.registered().getMessageBodyWriter( ActionDTO.class, ActionDTO.class, null, MediaType.APPLICATION_JSON_TYPE );
 			}
 			
 			StringBuilder b
 				= new StringBuilder( this.directory ).append( File.separator ).append( action.getId() );
 			
 			FileOutputStream fos
 				= new FileOutputStream( b.toString() );
 			
			this.messageBodyWriter.writeTo( action, Action.class, action.getClass(), new Annotation[] {}, MediaType.APPLICATION_JSON_TYPE, null, fos );
 			
 			return action;
 			
 		}
 		catch( Exception e ) {
 			throw new RuntimeException( e ); 
 		}
 		
 	}
 		
 }
