 /*****************************************************************************
  *                The Virtual Light Company Copyright (c) 1999
  *                               Java Source
  *
  * This code is licensed under the GNU Library GPL. Please read license.txt
  * for the full details. A copy of the LGPL may be found at
  *
  * http://www.gnu.org/copyleft/lgpl.html
  *
  * Project:    URI Class libs
  *
  * Version History
  * Date        TR/IWOR  Version  Programmer
  * ----------  -------  -------  ------------------------------------------
  *
  ****************************************************************************/
 
 package vlc.net.protocol.file;
 
 import java.io.*;
 import java.net.UnknownServiceException;
 import java.net.MalformedURLException;
 
 import org.ietf.uri.URL;
 import org.ietf.uri.URIUtils;
 import org.ietf.uri.ResourceConnection;
 
 /**
  * Representation of a file resource.
  * <P>
  *
  * Implements the connection as a standard file input stream. Files do not
  * have header information, so the default return values are left as is.
  * <P>
  *
  * For details on URIs see the IETF working group:
  * <A HREF="http://www.ietf.org/html.charters/urn-charter.html">URN</A>
  * <P>
  *
  * This softare is released under the
  * <A HREF="http://www.gnu.org/copyleft/lgpl.html">GNU LGPL</A>
  * <P>
  *
  * DISCLAIMER:<BR>
  * This software is the under development, incomplete, and is
  * known to contain bugs. This software is made available for
  * review purposes only. Do not rely on this software for
  * production-quality applications or for mission-critical
  * applications.
  * <P>
  *
  * Portions of the APIs for some new features have not
  * been finalized and APIs may change. Some features are
  * not fully implemented in this release. Use at your own risk.
  * <P>
  *
  * @author  Justin Couch
  * @version 0.7 (27 August 1999)
  */
 public class FileResourceConnection extends ResourceConnection
 {
   /** The pathname of the file */
   private File target_file;
 
   /** The input stream to the file. Not created until requested */
   private FileInputStream input_stream = null;
 
   /** The output stream from the file. Not created until requested */
   private FileOutputStream output_stream = null;
 
   /** The content type of the file */
   private String content_type = null;
 
   private String path;
   private String query;
   private String reference;
 
   /**
    * Create an instance of this connection.
    *
    * @param uri The URI to establish the connection to
    * @exception MalformedURLException We stuffed up something in the filename
    */
  protected FileResourceConnection(String uri)
     throws MalformedURLException
   {
    super(new URL("file://" + uri));
 
     // strip the query part from path to get the needed bits.
    String[] stripped_file = URIUtils.stripFile(uri);
 
    this.path = stripped_file[0];
     query = stripped_file[1];
     reference = stripped_file[2];
 
     // quick correction for win32 boxen if needed
     boolean is_win32 = System.getProperty("os.name").startsWith("Win") &&
                        System.getProperty("os.arch").equals("x86");
 
     if(is_win32)
     {
       path = path.replace('|', ':');
       if(path.charAt(0) == '/')
         path = path.substring(1);
     }
 
     target_file = new File(path);
   }
 
   /**
    * Get the input stream for this. Throws an UnknownServiceExeception if
    * there is no stream available.
    *
    * @return The unbuffered stream to the file
    */
   public InputStream getInputStream()
     throws IOException
   {
     if(input_stream == null)
     {
       input_stream = new FileInputStream(target_file);
     }
 
     return input_stream;
   }
 
   /**
    * Get the output stream for this. Throws an UnknownServiceExeception if
    * there is no stream available.
    *
    * @return The unbuffered stream to the file
    */
   public OutputStream getOutputStream()
     throws IOException
   {
     if(output_stream == null)
     {
       output_stream = new FileOutputStream(target_file);
     }
 
     return output_stream;
   }
 
   /**
    * Get the content type of the resource that this stream points to.
    * Returns a standard MIME type string. If the content type is not known then
    * <CODE>unknown/unknown</CODE> is returned (the default implementation).
    *
    * @return The content type of this resource
    */
   public String getContentType()
   {
     if(content_type == null)
     {
       content_type = findContentType(target_file.getName());
     }
 
     return content_type;
   }
 
   /**
    * Connect to the named resource if not already connected. If the file does
    * not exist, a FileNotFoundException is thrown.
    *
    * @exception IOException Could not find the named file.
    */
   public void connect()
     throws IOException
   {
     if(!target_file.exists())
       throw new FileNotFoundException("The file " + path + " does not exist");
 
     notifyConnectionEstablished("File \'" + path + "\' ready");
   }
 
   /**
    * Get the length of the content that is to follow on the stream. If the
    * length is unknown then -1 is returned. The content length is the
    * length of the file as returned by </CODE>File.length()</CODE>.
    *
    * @return The length of the content in bytes or -1
    */
   public int getContentLength()
   {
     try
     {
       getInputStream();
     }
     catch(IOException ioe)
     {
       return -1;
     }
 
     return (int)(target_file.length());
   }
 
   /**
    * Get the time that this object was last modified. This information comes
    * from the <CODE>File.lastModified()</CODE>
    * <P>
    * The time is in The result is the number of seconds since January 1, 1970
    * GMT.
    *
    * @return The time or 0 if unknown
    */
   public long getLastModified()
   {
     return target_file.lastModified();
   }
 
 }
