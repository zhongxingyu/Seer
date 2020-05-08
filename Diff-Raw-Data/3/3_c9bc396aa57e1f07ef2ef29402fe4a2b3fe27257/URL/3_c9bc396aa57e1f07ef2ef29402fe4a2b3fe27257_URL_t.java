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
 
 package org.ietf.uri;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 /**
  * Class <CODE>URL</CODE> represents a Uniform Resource
  * Locator, a pointer to a "resource" on the World
  * Wide Web. A resource can be something as simple as a file or a
  * directory, or it can be a reference to a more complicated object,
  * such as a query to a database or to a search engine. More
  * information on the types of URLs and their formats can be found at:
  * <blockquote><pre>
  *     http://www.ncsa.uiuc.edu/demoweb/url-primer.html
  * </pre></blockquote>
  * <p>
  * In general, a URL can be broken into several parts. The previous
  * example of a URL indicates that the protocol to use is
  * <CODE>http</CODE> (HyperText Transport Protocol) and that the
  * information resides on a host machine named
  * <CODE>www.ncsa.uiuc.edu</CODE>. The information on that host
  * machine is named <CODE>demoweb/url-primer.html</CODE>. The exact
  * meaning of this name on the host machine is both protocol
  * dependent and host dependent. The information normally resides in
  * a file, but it could be generated on the fly. This component of
  * the URL is called the <i>file</i> component, even though the
  * information is not necessarily in a file.
  * <p>
  * A URL can optionally specify a "port", which is the
  * port number to which the TCP connection is made on the remote host
  * machine. If the port is not specified, the default port for
  * the protocol is used instead. For example, the default port for
  * <CODE>http</CODE> is <CODE>80</CODE>. An alternative port could be
  * specified as:
  * <blockquote><pre>
  *     http://www.ncsa.uiuc.edu:8080/demoweb/url-primer.html
  * </pre></blockquote>
  * <p>
  * A URL may have appended to it an "anchor", also known
  * as a "ref" or a "reference". The anchor is
  * indicated by the sharp sign character "#" followed by
  * more characters. For example,
  * <blockquote><pre>
  *     http://java.sun.com/index.html#chapter1
  * </pre></blockquote>
  * <p>
  * This anchor is not technically part of the URL. Rather, it
  * indicates that after the specified resource is retrieved, the
  * application is specifically interested in that part of the
  * document that has the tag <CODE>chapter1</CODE> attached to it. The
  * meaning of a tag is resource specific.
  * <p>
  * An application can also specify a "relative URL",
  * which contains only enough information to reach the resource
  * relative to another URL. Relative URLs are frequently used within
  * HTML pages. For example, if the contents of the URL:
  * <blockquote><pre>
  *     http://java.sun.com/index.html
  * </pre></blockquote>
  * contained within it the relative URL:
  * <blockquote><pre>
  *     FAQ.html
  * </pre></blockquote>
  * it would be a shorthand for:
  * <blockquote><pre>
  *     http://java.sun.com/FAQ.html
  * </pre></blockquote>
  * <p>
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
 public final class URL extends URI
   implements java.io.Serializable
 {
   /** The real java URL */
   private String url_string = null;
 
   /** The path component of the URL */
   private String path = null;
 
   /** The query part of the URL */
   private String query = null;
 
   /** The reference part of the URL */
   private String reference = null;
 
   /** The host name */
   private String host = null;
 
   /** The port, or default port number */
   private int port = -1;
 
   /** The user info part. Typically this is username/passwd */
   private String user_info = null;
 
   /** the protocol scheme type of this URL */
   private String scheme = null;
 
   /** The value of the hash code for this object instance */
   private int hash_code = -1;
 
   /** The external string representation of this URL */
   private String external_form = null;
 
   /** Flag to indicate that the URL is of the generic form "protocol:blah" */
   private boolean generic_url = false;
 
   /**
    * Creates a URL object from the specified protocol, host, port number, and
    * file. Specifying a port number of -1 indicates that the URL should use the
    * default port for the protocol.
    *
    * @param protocol The name of the protocol.
    * @param host The name of the host.
    * @param port The port number.
    * @param file The host file.
    * @exception MalformedURLException  if an unknown protocol is specified.
    */
   public URL(String protocol, String host, int port, String file)
       throws MalformedURLException
   {
     this.scheme = protocol;
     this.host = host;
 
     // if the port is -1 fetch the default port in it
     this.port = (port == -1) ? URIUtils.getDefaultPort(scheme) : port;
 
     // Check the path and query components. The file could contain query
     // and reference components
     String[] file_parts = URIUtils.stripFile(file);
     this.path = file_parts[0];
     this.query = file_parts[1];
     this.reference = file_parts[2];
   }
 
   /**
    * Creates an absolute URL from the specified <CODE>protocol</CODE>
    * name, <CODE>host</CODE> name, and <CODE>file</CODE> name. The
    * default port for the specified protocol is used.
    * <p>
    * This method is equivalent to calling the four-argument
    * constructor with the arguments being <CODE>protocol</CODE>,
    * <CODE>host</CODE>, <CODE>-1</CODE>, and <CODE>file</CODE>.
    *
    * @param      protocol   the protocol to use.
    * @param      host       the host to connect to.
    * @param      file       the file on that host.
    * @exception  MalformedURLException  if an unknown protocol is specified.
    */
   public URL(String protocol, String host, String file)
       throws MalformedURLException
   {
     this(protocol, host, -1, file);
   }
 
   /**
    * Creates a URL object from the String representation.
    * <p>
    * This constructor is equivalent to a call to the two-argument constructor
    * with a null first argument.
    *
    * @param spec The string to parse as a URL.
    * @exception MalformedURLException If the string specifies an
    *   unknown protocol or has no protocol
    */
   public URL(String spec)
       throws MalformedURLException
   {
     scheme = URIUtils.getScheme(spec);
 
     if(scheme == null)
       throw new MalformedURLException("No protocol scheme defined");
 
     String[] hostport = URIUtils.getHostAndPortFromUrl(spec);
 
     if(hostport != null)
     {
       host = hostport[0];
       port = (hostport[1] == null) ? 0 : Integer.parseInt(hostport[1]);
       generic_url = false;
     }
     else
     {
       generic_url = true;
     }
 
     path = URIUtils.getPathFromUrlString(spec);
 
     query = URIUtils.getQueryFromUrlString(spec);
    int refLoc = spec.lastIndexOf("#");
    if (refLoc > -1)
      reference = spec.substring(refLoc+1);
   }
 
   /**
    * Construct a URL from the context and a relative URL. Allows specifcation
    * of a base URL (for example in a web page) and then relative parts to
    * that.
    * <P>
    *
    * Creates a URL by parsing the specification <CODE>spec</CODE> within a
    * specified context. If the <CODE>context</CODE> argument is not
    * <CODE>null</CODE> and the <CODE>spec</CODE> argument is a partial URL
    * specification, then any of the strings missing components are inherited
    * from the <CODE>context</CODE> argument.
    * <P>
    *
    * The specification given by the <CODE>String</CODE> argument is parsed
    * to determine if it specifies a protocol using
    * <CODE>URIUtils.getScheme()</CODE>.
    *
    * <UL>
    * <LI>If the <CODE>spec</CODE> argument does not specify a protocol:
    *     <UL>
    *     <LI>If the context argument is not <CODE>null</CODE>, then the
    *         protocol is copied from the context argument.
    *     <LI>If the context argument is <CODE>null</CODE>, then a
    *         <CODE>MalformedURLException</CODE> is thrown.
    *     </UL>
    * <LI>If the <CODE>spec</CODE> argument does specify a protocol:
    *     <UL>
    *     <LI>If the context argument is <CODE>null</CODE>, or specifies a
    *         different protocol than the specification argument, the context
    *         argument is ignored.
    *     <LI>If the context argument is not <CODE>null</CODE> and specifies
    *         the same protocol as the specification, the <CODE>host</CODE>,
    *         <CODE>port</CODE> number are copied from the context argument
    *         into the newly created <CODE>URL</CODE>. The <CODE>file</CODE>
    *         argument comes from the spec and is appended to the file from
    *         the context. Thus a context of
    *         <PRE>http://www.ietf.org/working-groups/index.html</PRE>
    *         and a spec of
    *         <PRE>mydoc.html</PRE>
    *         would result in a URL of
    *         <PRE>http://www.ietf.org/mydoc.html</PRE>
    *     </UL>
    * </UL>
    *
    *
    * @param context The context in which to parse the specification.
    * @param relPart A <CODE>String</CODE> representation of a relative URL
    * @exception  MalformedURLException  if no protocol is specified, or an
    *               unknown protocol is found.
    */
   public URL(URL context, String relPart)
       throws MalformedURLException
   {
     String protocol = null;
     String local_host = null;
     int local_port = 0;
 
     protocol = URIUtils.getScheme(relPart);
 
     if(protocol == null)
     {
       if(context != null)
         scheme = context.scheme;
       else
         throw new MalformedURLException("No protocol specified");
     }
     else
     {
       // we have a protocol. The fetch the host and port info. That should
       // never barf due to a malformed URL (at least it shouldn't!)
       scheme = protocol;
       String[] hostport = URIUtils.getHostAndPortFromUrl(relPart);
 
       if(context != null)
       {
         if(context.scheme.equalsIgnoreCase(protocol))
         {
           local_host = context.host;
           local_port = context.port;
         }
         else
         {
           local_host = hostport[0];
           local_port = Integer.parseInt(hostport[1]);
         }
       }
       else
       {
         local_host = hostport[0];
         local_port = (hostport[1] == null) ? 0 : Integer.parseInt(hostport[1]);
       }
     }
 
     host = local_host;
     port = local_port;
   }
 
   /**
    * Construct a URL based on a java.net.URL. Strips all the information and
    * copies it internally.
    *
    * @param url The original URL
    */
   public URL(java.net.URL url)
   {
     scheme = url.getProtocol();
     host = url.getHost();
     port = url.getPort();
 
     String[] file_parts = URIUtils.stripFile(url.getFile());
     path = file_parts[0];
     query = file_parts[1];
     reference = url.getRef();  // ignore the stripped version. Always null
   }
 
   /**
    * Returns the port number of this <CODE>URL</CODE>.
    * Returns -1 if the port is not set.
    *
    * @return  the port number
    */
   public int getPort()
   {
     return port;
   }
 
   /**
    * Returns the protocol name this URL.
    *
    * @return  the protocol of this URL.
    */
   public String getProtocol()
   {
     return scheme;
   }
 
   /**
    * Returns the host name of this URL, if applicable.
    * Eg for "<CODE>file</CODE>" protocol, this is an empty string.
    *
    * @return  the host name of this URL
    */
   public String getHost()
   {
     return host;
   }
 
   /**
    * Returns the file name of this URL.
    *
    * @return  the file name of this URL
    * @deprecated Use getPath();
    */
   public String getFile()
   {
     return path;
   }
 
   /**
    * Returns the path portion of this URL. The path is
    * everything between the '/' following the host and first '?'.
    *
    * @return  The path of this URL.
    */
   public String getPath()
   {
     return path;
   }
 
   /**
    * Get the query portion of this URL. The query is everything between '?'
    * following the path, and any trailing '#'. If no '?' is in the URL but
    * there is a reference, this returns null
    *
    * @return The query portion of this URL, if defined
    */
   public String getQuery()
   {
     return query;
   }
 
   /**
    * Returns the anchor (also known as the "reference") of this
    * URL.
    *
    * @return The reference portion of this URL
    */
   public String getRef()
   {
     return reference;
   }
 
   /**
    * Compares two URLs, excluding the reference and query parts.
    * <P>
    * Returns true if this URL and the argument both refer to the same resource.
    * The two URLs might not both contain the same anchor.
    *
    * @param other The URL to compare against.
    * @return true if they reference the same remote object; false otherwise.
    */
   public boolean sameFile(URL other)
   {
     // Compare the protocols.
     if(!((scheme != null) && scheme.equalsIgnoreCase(other.scheme)))
       return false;
 
     // Compare the hosts.
     if(((host == null) && (other.host == null)) ||
        ((host != null) && !host.equalsIgnoreCase(other.host)))
       return false;
 
     // Compare the ports.
     if (port != other.port)
       return false;
 
     // Compare the files.
     if (!((path != null) && path.equals(other.path)))
         return false;
 
     return true;
   }
 
   /**
    * Establish a connection to the named resource. This partially maps the
    * I2R service request in providing a representation of the connection to
    * the resource without actually supply the resource itself. The resource
    * itself can be then obtained using the methods of the ResouceConnection
    * class.
    *
    * @return A reference to the connection to the resource.
    * @exception UnsupportedServiceException Resolution of the requested service
    *   type for this URN is not available
    * @see ResourceConnection
    */
   public ResourceConnection getResource()
     throws UnsupportedServiceException, IOException
   {
     URIResourceStream stream = ResourceManager.getProtocolHandler(scheme);
 
     if(stream == null)
       throw new UnsupportedServiceException(scheme + " is not supported");
 
     String full_path = (query != null) ? path + '?' + query : path;
     ResourceConnection resc = stream.openConnection(host, port, full_path);
 
     return resc;
   }
 
   /**
    * Establish a connection to all possible resolutions of this resource. This
    * partially maps to the I2Rs service request in providing all possible
    * representations of the URI without actually fetching the resource itself.
    * The resources themselves may be obtained using the methods of the
    * ResourceConnection class.
    * <P>
    * The current implementation just returns the a single connection which is
    * identical to what getResource() would return. More work needs to be done
    * on this.
    *
    * @return The list of connections to resources
    * @exception UnsupportedServiceException Resolution of the requested service
    *   type for this URN is not available
    * @see ResourceConnection
    */
   public ResourceConnection[] getResourceList()
     throws UnsupportedServiceException, IOException
   {
     URIResourceStream stream = ResourceManager.getProtocolHandler(scheme);
 
     String full_path = (query != null) ? path + '?' + query : path;
     ResourceConnection resc = stream.openConnection(host, port, full_path);
 
     ResourceConnection[] ret_val = { resc };
 
     return ret_val;
   }
 
   /**
    * I2L service request. Get the URL that represents this URI. Returns a
    * reference to <CODE>this</CODE>
    *
    * @return The URL representing this URI or null
    * @exception UnsupportedServiceException Resolution of the requested service
    *   type for this URI is not available
    */
   public URL getURL()
     throws UnsupportedServiceException, IOException
   {
     return this;
   }
 
   /**
    * I2Ls service request. Get the list of possible URLs that represents this
    * URN. Whether this results in a legal URL that really represents the URI
    * is dependent on the confirm property.
    * <P>
    * If a URL representation of this URI cannot be found through the resolver
    * service then this method returns null.
    * <P>
    * The current implementation just returns a reference to <CODE>this</CODE>
    * embedded in an array.
    * <P>
    * @return The list of URL representing this URI or null
    * @exception UnsupportedServiceException Resolution of the requested service
    *   type for this URI is not available
    */
   public URL[] getURLList()
     throws UnsupportedServiceException, IOException
   {
     URL[] ret_vals = { this };
 
     return ret_vals;
   }
 
   /**
    * I2C service request. Get the first URC that describes this URI.
    * If a URC cannot be determined for this URI then null is returned.
    *
    * @return The URC describing this URI.
    * @exception UnsupportedServiceException Resolution of the requested service
    *   type for this URI is not available
    */
   public URC getURC()
     throws UnsupportedServiceException, IOException
   {
     return null;
   }
 
   /**
    * I2C service request. Get the list of URCs that describes this URI.
    * If a URC cannot be determined for this URI then null is returned.
    *
    * @return The URC describing this URN.
    * @exception UnsupportedServiceException Resolution of the requested service
    *   type for this URN is not available
    */
   public URC[] getURCList()
     throws UnsupportedServiceException, IOException
   {
     return null;
   }
 
   /**
    * I2N service request. Get the first URN that describes this URI.
    * If a URN cannot be determined for this URI then null is returned.
    *
    * @return The list of equivalent URNs
    * @exception UnsupportedServiceException Resolution of the requested service
    *   type for this URN is not available
    */
   public URN getURN()
     throws UnsupportedServiceException, IOException
   {
     return null;
   }
 
   /**
    * I2Ns service request. Get the list of possible URNs that are also
    * equivalent descriptors of this resource. If no alternate representations
    * are available, then null is returned.
    *
    * @return The list of equivalent URNs
    * @exception UnsupportedServiceException Resolution of the requested service
    *   type for this URN is not available
    */
   public URN[] getURNList()
     throws UnsupportedServiceException, IOException
   {
     return null;
   }
 
   /**
    * Constructs a string representation of this <CODE>URL</CODE>. The
    * string is created by calling the <CODE>toExternalForm</CODE>
    * method of the stream protocol handler for this object.
    *
    * @return  a string representation of this object.
    * @see java.net.URL#URL(java.lang.String, java.lang.String, int, java.lang.String)
    * @see java.net.URLStreamHandler#toExternalForm(java.net.URL)
    */
   public String toString()
   {
     return toExternalForm();
   }
 
   /**
    * Constructs a string representation of this <CODE>URL</CODE>. The
    * string is created by calling the <CODE>toExternalForm</CODE>
    * method of the stream protocol handler for this object.
    *
    * @return  a string representation of this object.
    * @see java.net.URL#URL(java.lang.String, java.lang.String, int, java.lang.String)
    * @see java.net.URLStreamHandler#toExternalForm(java.net.URL)
    */
   public String toExternalForm()
   {
     if(external_form != null)
       return external_form;
 
     StringBuffer buffer = new StringBuffer(scheme);
 
     if(!generic_url)
       buffer.append("://");
     else
       buffer.append(':');
 
     if(user_info != null)
     {
       buffer.append(user_info);
       buffer.append('@');
     }
 
     if(host != null)
       buffer.append(host);
 
     if(port > 0)
     {
       buffer.append(':');
       buffer.append(port);
     }
 
     // don't prepend '/' to the path, because that is extracted by the
     // constructor. If it is there, then it will use it, if not then
     // doesn't matter as it may be a query anyway.
     if(path != null)
     {
       buffer.append(path);
     }
 
     if(query != null)
     {
       buffer.append('?');
       buffer.append(query);
     }
 
     if(reference != null)
     {
       buffer.append('#');
       buffer.append(reference);
     }
 
     external_form = buffer.toString();
 
     return external_form;
   }
 
   /**
    * Test for equality between this URN and any other URI. Implements the I=I
    * service request. If the object is not a URI then false is immediately
    * returned otherwise the alternate version of this method is called.
    *
    * @param o The object to compare against
    * @return true if they are equivalent URI
    */
   public boolean equals(Object o)
   {
     if(o instanceof URI)
       return equals((URI)o);
     else
       return false;
   }
 
   /**
    * Test for equality between this URN and any other URI. Implements the I=I
    * service request.
    * <P>
    * This implementation is partially crippled at the moment as it can only
    * handle another URL for comparison. True is returned if sameFile() returns
    * true and also the references and queries are the same.
    *
    * @param uri The URI to compare against
    * @return true if they are equivalent URI
    */
   public boolean equals(URI uri)
   {
     // tbd!  should use I=I service
     if(!(uri instanceof URL))
       return false;
 
     URL other = (URL)uri;
 
     // First test - check the file.
     if(!sameFile(other))
       return false;
 
     // Check query. Case sensitive checking here
     if(((query != null) && !query.equals(other.query)) ||
        (query != other.query))
       return false;
 
     // Check query. Case sensitive checking here
     if(((reference != null) && !reference.equals(other.reference)) ||
        (reference != other.reference))
       return false;
 
     return true;
   }
 
   /**
    * Creates an integer suitable for hash table indexing. The hashcode is based
    * on the addition of the all the hashcodes for the internal strings and the
    * port number represented as an integer.
    *
    * @return  a hash code for this <CODE>URL</CODE>.
    */
   public synchronized int hashCode()
   {
     if(hash_code != -1)
       return hash_code;
 
     int val = 0;
 
     // Generate the protocol part.
     if(scheme != null)
       val += scheme.hashCode();
 
     // Generate the host part.
     if(host != null)
       val += host.hashCode();
 
     // Generate the file part.
     if(path != null)
       val += path.hashCode();
 
     // Generate the query part
     if(query != null)
       val += query.hashCode();
 
     // Genrate the reference part
     if(reference != null)
       val += reference.hashCode();
 
     // Generate the port part.
     val += port;
 
     hash_code = val;
 
     return val;
   }
 }
