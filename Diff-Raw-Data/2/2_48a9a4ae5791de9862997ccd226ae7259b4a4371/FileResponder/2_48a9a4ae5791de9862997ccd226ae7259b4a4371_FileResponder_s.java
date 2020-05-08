 package org.twuni.common.net.http.responder;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import org.twuni.common.net.http.request.Request;
 import org.twuni.common.net.http.response.Response;
 import org.twuni.common.net.http.response.Status;
 
 import eu.medsea.mimeutil.MimeType;
 import eu.medsea.mimeutil.MimeUtil2;
 
 public class FileResponder implements Responder {
 
 	private static final MimeType UNKNOWN_TYPE = new MimeType( "application/octet-stream" );
 
 	private final File parent;
 	private final MimeUtil2 mime;
 
 	public FileResponder( File parent ) {
 		this.parent = parent;
 		this.mime = new MimeUtil2();
 		mime.registerMimeDetector( "eu.medsea.mimeutil.detector.MagicMimeMimeDetector" );
 	}
 
 	@Override
 	public Response respondTo( Request request ) {
 		try {
 			File file = new File( parent, request.getResource() );
 			byte [] buffer = readFully( file );
 			return new Response( Status.OK, getContentType( buffer ), buffer );
 		} catch( FileNotFoundException exception ) {
 			return new Response( Status.NOT_FOUND );
 		} catch( IOException exception ) {
 			return new Response( Status.INTERNAL_SERVER_ERROR );
 		}
 	}
 
 	private String getContentType( byte [] buffer ) {
 		return mime.getMimeTypes( buffer, UNKNOWN_TYPE ).iterator().next().toString();
 	}
 
 	protected byte [] readFully( File file ) throws FileNotFoundException, IOException {
 		FileInputStream in = new FileInputStream( file );
 		byte [] buffer = new byte [(int) file.length()];
		for( int length = 0; length < buffer.length; length += in.read( buffer ) ) {
 		}
 		in.close();
 		return buffer;
 	}
 
 }
