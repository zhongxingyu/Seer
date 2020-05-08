 package ikrs.httpd.resource; 
 
 /**
  * @author  Ikaros Kappler
  * @date    2012-10-11
  * @version 1.0.0
  **/
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.URI;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.UUID;
 import java.util.logging.Level;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock; 
 
 
 import ikrs.httpd.CustomUtil;
 import ikrs.httpd.HTTPFileFilter;
 import ikrs.httpd.HTTPHandler;
 import ikrs.httpd.ReadOnlyException;
 import ikrs.httpd.Resource;
 import ikrs.util.CaseInsensitiveComparator;
 import ikrs.util.CustomLogger;
 import ikrs.util.MIMEType;
 
 
 public class DefaultDirectoryResource
     extends AbstractDirectoryResource {
 
     /**
      * The output format (usually "TXT" or "HTML"). 
      * Later implementations might use subclassing to build different output types.
      **/
     private String outputFormat;
     
 
     /**
      * Create a new DefaultDirectoryResource.
      *
      * @param logger       A custom logger to write log messages to.
      * @param fileFilter   The file filter to use.
      * @param dir          The directory.
      * @param requestURI   The uri from the request (will be used to avoid printing the absolute file path).
      * @param format       The output format; currently "TXT" and "HTML" are supported (default is "TXT", if format is null). 
      * @param useFairLocks If set to true the created resource will use fair locks.
      **/
     public DefaultDirectoryResource( HTTPHandler handler,
 				     CustomLogger logger,
 				     HTTPFileFilter fileFilter,
 				     File dir,
 				     URI requestURI,
 				     UUID sid,
 				     String format,
 
 				     boolean useFairLocks )
 	throws NullPointerException {
 
 	super( handler, logger, fileFilter, dir, requestURI, sid, useFairLocks );
 
 	this.outputFormat             = format;
     }
 
 
     // --- BEGIN --------------------- AbstractDirectoryResource implementation ------------------------
     /**
      * This method is designated to build the data for the directory listing.
      *
      * Subclasses must implement this method and write the generated data into the given output stream.
      *
      *
      * @param sid The current session's ID.
      * @param out The output stream to write the list data to.
      * @throws IOException If any IO errors occur.
      **/
     public void generateDirectoryListing( UUID sid,
 					  OutputStream out )
 	throws IOException {
 
 
 	String title = "Index of " + this.getRequestURI().getPath() + getFormattedLineBreak(2);
 
 	if( this.isHTMLFormat() ) {
 
 	    String cssPath = "/system/styles/directory.list.css";
 
 	    StringBuffer htmlHeader = new StringBuffer();
 	    htmlHeader.
 		append( "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" ).
 		append( "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \n"  ).
 		append( "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"  ).
 		append( "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"  ).
 		append( "<head>\n"  ).
 		append( "<title>Contents of " ).append( this.getRequestURI().getPath() ).append( "</title>\n"  ).
 		append( "<link rel=\"icon\" href=\"/favicon.ico\" type=\"image/x-icon\" />\n"  ).
 		append( "<link rel=\"stylesheet\" type=\"text/css\" href=\"" ).append( cssPath ).append( "\" />" ).
 		append( "</head>\n"  ).
 		append( "\n"  ).
 		append( "<body>\n"  ).
 		append( "\n"  ).
 		// append( "<h3><img src=\"icon_mini.png\" alt=\"Icon\" id=\"icon_mini\" /> " ).append( title ).append( "</h3>\n"  ).
 		append( "<h2>" ).append( title ).append( "</h2>\n"  ).
 		append( "\n" );
 		
 	    out.write( htmlHeader.toString().getBytes() );
 	    
 	} else {
  
 	    out.write( title.getBytes() );
 
 	}
 
 
 
 	// First make the '..' navigation file (if parent exists)
 	if( this.isHTMLFormat() && !this.getRequestURI().getPath().equals("/") ) {
 	    
 	    String parentPath = new File(this.getRequestURI().getPath()).getParent();
 	    String parentLink = "<a href=\"" + parentPath + "\">parent dir</a><br/><br/>\n";
 	    out.write( parentLink.getBytes() );
 
 	}
 
 	if( this.isHTMLFormat() ) {
 	 
 	    String tableStart = "<table>\n";
 	    out.write( tableStart.getBytes() );
 	    
 	}
 
 
 	// Write temp file data into buffer
 	int totalTextWidth = this.generateFileListing( out );
 	
 
 	Date currentDate = new Date( System.currentTimeMillis() );
 	String footLine  = 		
 	    getFormattedLineBreak(2) +
 	    "Document generated " + this.getDateFormat().format( currentDate ) +
 	    getFormattedLineBreak();
 
 	if( this.isHTMLFormat() ) 
 	    footLine += "<hr />\n";
 	else
 	    footLine += CustomUtil.repeat( "=", totalTextWidth ) + getFormattedLineBreak();
 
 
 	footLine += this.getHTTPHandler().getSoftwareName() + getFormattedLineBreak();
 
 	
 	if( this.isHTMLFormat() ) {
 
 	    String htmlFooter =
 		"</table>\n" +
 		footLine + 
 		"</body>\n" +
 		"</html>\n";
 	    out.write( htmlFooter.getBytes() );
 
 	} else {
 
 	    out.write( footLine.getBytes() );
 	    
 	}
 
     }
 
 
     /**
      * This method returns the Content-Type this class generates.
      *
      * The returned MIME type must not be null.
      *
      * @return The Content-Type this class generates.
      **/
     public MIMEType getDirectoryListingType() {
 	if( this.isHTMLFormat() ) {
 	    
 	    // Hint: this generator creates XHTML (not HTML).
 	    //       The MIME type 'text/htm'l would be acceptable for most browsers, but
 	    //       the correct type is 'application/xhtml+xml'!
 	    return new MIMEType( "application/xhtml+xml" ); 
 
 	} else {
 
 	    return new MIMEType( "text/plain" );
 
 	}
     }
     // --- END ----------------------- AbstractDirectoryResource implementation ------------------------
     
     private int generateFileListing( OutputStream out ) 
 	throws IOException {
 
 	// WARNING: due to the AbstractDirectoryResource-specs it is NOT GUARANTEED that the file
 	//          is really a directory!
 
 
 	StringBuffer lineBuffer = new StringBuffer();
 	if( !this.getDirectoryFile().isDirectory() ) {
 
 	    // Print the file itself as a listing? :)
 	    // Print an error message? What about the status code in this case???
 
 	    //buildLine( lineBuffer, this.getDirectoryFile() );
 	    //out.write( lineBuffer.toString().getBytes() );
 	    //lineBuffer.delete( 0, lineBuffer.length() );
 
 	    String message = "File is a directory and cannot be listed.\n";
 	    out.write( message.getBytes() );
 
 	    return -1;
 	}
 
 
 	File[] files = this.getDirectoryFile().listFiles();
 	files = sortFiles( files );
 
 	if( this.isHTMLFormat() ) {
 	
 	    for( int i = 0; i < files.length; i++ ) {
 		
 		// Do NOT include files the file-filter does not allow to be listed!
 		if( !this.getFileFilter().acceptListing(files[i]) )
 		continue;  // Hide due to security reasons
 		
 		
 		buildHTMLLine( lineBuffer, files[i] );
 		out.write( lineBuffer.toString().getBytes("UTF-8") );
 		lineBuffer.delete( 0, lineBuffer.length() );
 		
 	    }
 
 	    return -1;
 
 	} else {
 
 	    List<String[]> tableData = new ArrayList<String[]>( files.length );
 	    int[] rowSizes = null;
 	    String[] rowData = null;
 	    for( int i = 0; i < files.length; i++ ) {
 		
 		rowData = collectRowData( files[i] );
 		tableData.add( rowData );
 
 		if( rowSizes == null )
 		    rowSizes = new int[ rowData.length ];
 
 		for( int c = 0; c < rowData.length; c++ ) {
 
 		    rowSizes[c] = Math.max( rowSizes[c], rowData[c].length() );
 		}
 
 	    } // END for
 	    
 	    // Generate text output
 	    int totalWidth = 80;     // 80 for the case there are not files
 	    for( int i = 0; i < tableData.size(); i++ ) {		
 
 		rowData = tableData.get( i );
 		buildTextLine( lineBuffer,
 			       rowData,
 			       rowSizes,
 			       4    // columnSpacing
 			       );
 		out.write( lineBuffer.toString().getBytes("UTF-8") );
 		totalWidth = lineBuffer.length() - 1;  // do not include line break
 		lineBuffer.delete( 0, lineBuffer.length() );
 
 	    }
 
 	    return totalWidth;
 	}
     }
 
     private String[] collectRowData( File file ) {
 	// 0: icon
 	// 1: name
 	// 2: size
 	// 3: type
 	// 4: date
 	String[] row = new String[ 5 ];
 	
 	for( int i = 0; i < row.length; i++ ) {
 	    row[i] =  getFileAttribute( file, i );
 	}
 
 	return row;
     }
 
     private void buildTextLine( StringBuffer lineBuffer,
 				String[] rowData,
 				int[] rowSizes,
 				int columnSpacing) {
 
 	for( int i = 0; i < rowData.length; i++ ) {
 
 	    String data = rowData[i];
 	    int len     = rowSizes[i];
 	    
 	    if( i == 2 ) {
 
 		// The length should be aligned right
 		lineBuffer.append( CustomUtil.repeat( " ", len - data.length()) );
 		lineBuffer.append( data );
 		
 	    } else {
 
 		// Other values should be aligned left	
 		lineBuffer.append( data );
 		lineBuffer.append( CustomUtil.repeat( " ", len - data.length()) );
 
 	    }
 
 	    // Print column spacing
 	    lineBuffer.append( CustomUtil.repeat( " ", columnSpacing) );
 	    
 	}
 
 	lineBuffer.append( "\n" );
 
     }
 
     private void buildHTMLLine( StringBuffer lineBuffer,
 				File file ) {
 	
 	if( this.isHTMLFormat() ) 
 	    lineBuffer.append( "<tr>\n" );
 
 	    
 	for( int i = 0; i < 5; i++ ) {
 	    
 	    if( this.isHTMLFormat() ) {
 		lineBuffer.append( "      <td" );
 		if( i == 0 || i == 2 ) // icon or size
 		    lineBuffer.append( " align=\"right\"" );
 		lineBuffer.append( ">" );
 	    } else if( i > 0 ) {
 		lineBuffer.append( "\t\t" );
 	    }
 	    
 
 	    lineBuffer.append( getFileAttribute(file, i) );
 
 
 	    if( this.isHTMLFormat() )
 		lineBuffer.append( "</td>\n" );
 	    
 	}
 
 
 	if( this.isHTMLFormat() )
 	    lineBuffer.append( "</tr>\n\n" );
 	else
 	    lineBuffer.append( "\n" );
 
     }
 
     private String getFileAttribute( File file,
 				     int field
 				     ) {
 
 		
 	switch( field ) {
 	case 0: 
 	    if( this.isHTMLFormat() ) {
 		if( file.isDirectory() ) return "<div class=\"icon_directory\"></div>";
 		else                     return "<div class=\"icon_file\"></div>";
 	    } else {
 		return "";
 	    }
 
 	case 1: 
 
 	    // File name [make an anchor!]
 	    if( this.isHTMLFormat() ) {
		if( this.getRequestURI().getPath() == null || this.getRequestURI().getPath().equals("/") )
 		    return "<a href=\"" + file.getName() + "\">" + file.getName() + "</a>";
		else
		    return "<a href=\"" + (this.getRequestURI().getPath() + "/") + file.getName() + "\">" + file.getName() + "</a>";
 	    } else {
 		return file.getName();
 	    }
 	    
 	case 2:  
  
 	    // File size [make nice dotted format]
 	    StringBuffer buffer = new StringBuffer();
 	    String strLength = Long.toString( file.length() );
 	    int i = strLength.length();
 	    while( i > 0 ) {
 
 		if( i < strLength.length() )
 		    buffer.insert( 0, "." );  // insert at beginning
 
 		buffer.insert( 0,   // insert at beginning
 			       strLength.substring( Math.max(0,i-3), 
 						    i )
 			       );
 
 		i -= 3;
 	    }
 	    buffer.append( " bytes" );
 	    
 	    //return Long.toString(file.length()) + " bytes";
 	    return buffer.toString();
 
 	case 3:
 	    
 	    // File type
 	    if( file.isDirectory() )
 		return "dir";
 	    else if( file.isFile() )
 		return "file";
 	    else 
 		return "?";
 
 	default:  
 
 	    // File timestamp [use configured DateFormat]
 	    long timeStamp = file.lastModified();
 	    Date date = new Date( timeStamp );
 	    return this.getDateFormat().format( date ); //return Long.toString(file.lastModified());  
 
 	}
 
     }
 
     private String getFormattedLineBreak( int count ) {
 	String buffer = "";
 	while( count > 0 ) {
 
 	    buffer += getFormattedLineBreak();
 
 	    count--;
 	}
 	return buffer;
     }
 
     private String getFormattedLineBreak() {
 	if( this.isHTMLFormat() ) 
 	    return "<br/>\n";
 	else
 	    return "\n";
     }
 
     private boolean isHTMLFormat() {
 	return (this.outputFormat != null && this.outputFormat.equalsIgnoreCase("HTML"));
     }
 
 
     private File[] sortFiles( File[] files ) {
 
 	TreeMap<String,File> dirMap = new TreeMap<String,File>( CaseInsensitiveComparator.sharedInstance );
 	TreeMap<String,File> fileMap = new TreeMap<String,File>( CaseInsensitiveComparator.sharedInstance );
  
 	for( int i = 0; i < files.length; i++ ) {
 
 	    if( files[i].isDirectory() )
 		dirMap.put( files[i].getName(), files[i] );
 	    else
 		fileMap.put( files[i].getName(), files[i] );
 
 	}
 
 	int i = 0;
 	Iterator<String> dirIter = dirMap.keySet().iterator();
 	while( dirIter.hasNext() ) {
 	    files[i++] = dirMap.get( dirIter.next() );
 	}
 
 	Iterator<String> fileIter = fileMap.keySet().iterator();
 	while( fileIter.hasNext() ) {
 	    files[i++] = fileMap.get( fileIter.next() );
 	}
 
 
 	return files;
     }
 
 }
