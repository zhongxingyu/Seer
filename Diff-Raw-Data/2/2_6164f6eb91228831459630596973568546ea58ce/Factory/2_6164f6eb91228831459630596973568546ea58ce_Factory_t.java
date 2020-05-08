 //////////////////////////////////////////////////////////////////////
 //
 // File: Factory.java
 //
 // Copyright (c) 2003-2005 TiVo Inc.
 //
 //////////////////////////////////////////////////////////////////////
 
 package com.tivo.hme.sdk;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLDecoder;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 import com.tivo.hme.interfaces.IApplication;
 import com.tivo.hme.interfaces.IArgumentList;
 import com.tivo.hme.interfaces.IContext;
 import com.tivo.hme.interfaces.IFactory;
 import com.tivo.hme.interfaces.IHmeConstants;
 import com.tivo.hme.interfaces.IHttpRequest;
 import com.tivo.hme.interfaces.IListener;
 import com.tivo.hme.interfaces.ILoader;
 import com.tivo.hme.interfaces.ILogger;
 import com.tivo.hme.sdk.io.FastInputStream;
 import com.tivo.hme.sdk.io.HmeInputStream;
 import com.tivo.hme.sdk.io.HmeOutputStream;
 import com.tivo.hme.sdk.util.Mp3Helper;
 
 /**
  * A factory of applications.
  *
  * @author      Adam Doppelt
  * @author      Arthur van Hoff
  * @author      Brigham Stevens
  * @author      Jonathan Payne
  * @author      Steven Samorodin
  */
 @SuppressWarnings("unchecked")
 public class Factory implements IFactory
 {
     /**
      * The listener for this factory.
      */
     protected IListener listener;
     
     /**
      * Class loader for the main class.
      */
     protected ClassLoader loader;
 
     /**
      * The factory uri prefix, used to accept connections.  It always starts
      * and ends with '/'.
      */
     protected String uri;
 
     /**
      * The asset uri used to load external assets. This might be null.
      */
     protected URL assetURI;
 
     /**
      * The factory title that will be displayed in the chooser.
      */
     protected String title;
 
     /**
      * The class used to create application instances.
      */
     protected Class clazz;
 
     /**
      * The list of active applications created by the factory.
      */
     protected Vector active;
 
     /**
      * Whether or not this factory is draining (waiting for the last instance to
      * finish) or not.
      */
     protected boolean isActive;
 
     /**
      * For associating data with the factory
      */
     protected Map factoryData;
 
     
     /**
      * A factory of applications.
      */
     public Factory()
     {
         this.active = new Vector();
     }
 
     
     /**
      * Init the factory. Subclasses should override this.
      */
     protected void init(IArgumentList args)
     {
     }
     
     /**
      * Create an instance of an application. Uses clazz by default.
      */
     public IApplication createApplication(IContext context) throws IOException
     {
         HmeOutputStream out = null;
         HmeInputStream in = null;
         if (context.getOutputStream() instanceof HmeOutputStream)
             out = (HmeOutputStream)context.getOutputStream();
         else
             out = new HmeOutputStream(context.getOutputStream());
         
         if (context.getInputStream() instanceof HmeInputStream)
             in = (HmeInputStream)context.getInputStream();
         else
             in = new HmeInputStream(context.getInputStream());
         
         // HME protocol starts here
         out.writeInt(IHmeProtocol.MAGIC);
         out.writeInt(IHmeProtocol.VERSION);
         out.flush();
         
         // read magic and version
         int magic = in.readInt();
         if (magic != IHmeProtocol.MAGIC) {
             throw new IOException("bad magic: 0x" + Integer.toHexString(magic));
         }
         int version = in.readInt();
         if (version >> 8 < IHmeProtocol.VERSION >> 8 ) {
             throw new IOException(
                     "version mismatch: " + 
                     ( version >> 8 ) + "." + ( version & 0xff ) +
                     " < " +
                     ( IHmeProtocol.VERSION >> 8 ) + "." + ( IHmeProtocol.VERSION & 0xff ) );
         }
 
         IApplication retApp = null;
         try {
             Application app = (Application)clazz.newInstance();
             app.setFactory(this);
             app.setContext(context, version);
             retApp = app;
         } catch (InstantiationException ex) {
             ex.printStackTrace();            
         } catch (IllegalAccessException ex) {
             ex.printStackTrace();
         }
         
         return retApp;
     }
     
     //
     // accessors
     //
 
     /**
      * Get the factory uri prefix.
      */
     public String getURI()
     {
         return uri;
     }
 
     /**
      * The URI for loading external assets.
      */
     public URL getAssetURI()
     {
         return assetURI;
     }
     
     /**
      * Get the factory's title.
      */
     public String getTitle()
     {
         return title;
     }
     
     /**
      * Set the uri prefix.
      */
     public void setURI(String uri)
     {
         if (!uri.startsWith("/")) {
             uri = "/" + uri;
         }
         if (!uri.endsWith("/")) {
             uri += "/";
         }
         this.uri = uri;
     }
     
     /**
      * Set the title.
      */
     public void setTitle(String title)
     {
         this.title = title;
     }
 
     /**
      * Set the asset URI.
      */
     public void setAssetURI(URL assetURI)
     {
         this.assetURI = assetURI;
     }
 
     /**
      * Returns false if the factory is draining, i.e., not accepting new
      * connections.
      */
     public boolean isActive()
     {
         return isActive;
     }
 
     /**
      * Returns the # of active connections.
      */
     public int getActiveCount()
     {
         return active.size();
     }
     
     protected long getMP3Duration(String uri)
     {
     	return -1;
     }
 
     /**
      * This is called by the HTTP server when the factory must handle an http
      * request.
      */
     private InputStream handleHTTP(IHttpRequest http, String uri) throws IOException
     {
         InputStream in = null;
         
         // check for query portion of URI - i.e. "?foo=bar" stuff
         String queryStr = getQuery(uri);
         String baseUri = removeQueryFromURI(uri);
         
         try {
             in = getStream(baseUri);
         } catch (IOException e) {
             http.reply(404, e.getMessage());
             return null;
         }
         
         long offset = 0;
         String range = http.get("Range");
         if ((range != null) && range.startsWith("bytes=") && range.endsWith("-")) {
             try {
                 offset = Long.parseLong(range.substring(6, range.length() - 1));
             } catch (NumberFormatException e) {
                 // ignore
             }
         }
         
         if (!http.getReplied()) {
             if (offset == 0) {
                 http.reply(200, "Media follows");
             } else {
                 http.reply(206, "Partial Media follows");
             }
         }
         
         String ct = null;
         if (baseUri.endsWith(".mpeg") || baseUri.endsWith(".mpg")) {
             ct = "video/mpeg";
         } else if (baseUri.endsWith(".mp3")) 
         {
             // set the MP3 content type string
             ct = "audio/mpeg";
             
             // Get the mp3 stream's duration
            long mp3duration = getMP3Duration(baseUri);
             System.out.println("MP3 stream duration: " + mp3duration);
             if (mp3duration > 0)
                 http.addHeader(IHmeConstants.TIVO_DURATION, "" + mp3duration);
             
             // do MP3 seek if needed
             if (in != null && queryStr != null)
             {
                 int seekTime = getSeekTime(queryStr);
                 Mp3Helper mp3Helper = new Mp3Helper(in, in.available());
                 in = mp3Helper.seek(seekTime);
             }
         }
         if (ct != null) {
             http.addHeader("Content-Type", ct);
         }
         
         if (offset > 0) {
             long total = in.available();
             http.addHeader("Content-Range", "bytes " + offset + "-" + (total-1) + "/" + total);
             in.skip(offset);
         }
         
         addHeaders(http, baseUri);
         
         if (http.get("http-method").equalsIgnoreCase("HEAD")) {
             return null;
         }
         
         return in;
         
     }
 
     /**
      * Subclasses can override this method to add more HTTP headers to a response.
      * 
      */
     protected void addHeaders(IHttpRequest http, String uri) throws IOException
     {
     }
 
     /**
      * A simple stream that responds correctly to in.available(). This is for
      * http streams.
      */
     static class URLStream extends FastInputStream
     {
         long contentLength;
         URLStream(InputStream in, long contentLength)
         {
             super(in, IHmeConstants.TCP_BUFFER_SIZE);
             this.contentLength = contentLength;
         }
         public int available()
         {
             return (int) contentLength;
         }
         public int read() throws IOException
         {
             contentLength -= 1;
             return in.read();
         }
         public int read(byte b[], int off, int length) throws IOException
         {
             int n = super.read(b, off, length);
             if (n > 0) {
                 contentLength -= n;
             }
             return n;
         }
         public long skip(long n) throws IOException
         {
             n = super.skip(n);
             if (n > 0) {
                 contentLength -= n;
             }
             return n;
         }
     }
 
     /**
      * Open a resource stream. Throws an exception if it cannot find the
      * requested stream.
      */
     public InputStream getStream(String uri) throws IOException
     {
         //
         // 1 - is it a full URI?
         //
         
         if (uri.startsWith("http://")) {
             URL url = new URL(uri);
             URLConnection conn = url.openConnection();
             return new URLStream(conn.getInputStream(), conn.getContentLength());
         }
 
         //
         // 2 - try package/uri in classpath
         //
 
         String pkg = clazz.getName();
         int last = pkg.lastIndexOf('.');
         if (last > 0) {
             pkg = pkg.substring(0, last).replace('.', '/');
             InputStream in = loader.getResourceAsStream(pkg + "/" + uri);
             if (in != null) {
                 return in;
             }
         }
 
         //
         // 3 - try uri in classpath
         //
         
         InputStream in = loader.getResourceAsStream(uri);        
         if (in != null) {
             return in;
         }
         
         throw new FileNotFoundException(uri);
     }
 
     /* (non-Javadoc)
      * @see com.tivo.hme.hosting.IFactory#addApplication(com.tivo.hme.hosting.IApplication)
      */
     public void addApplication(IApplication app)
     {
         active.addElement(app);
         log(ILogger.LOG_NOTICE, "HME receiver connected");
     }
 
     /* (non-Javadoc)
      * @see com.tivo.hme.hosting.IFactory#removeApplication(com.tivo.hme.hosting.IApplication)
      */
     public void removeApplication(IApplication app) 
     {
         log(ILogger.LOG_NOTICE, "HME receiver disconnected");        
 
         active.removeElement(app);
 
         // If we're not active and we just removed the last instance then remove
         // this from the listener.
         if (!isActive && active.size() == 0) {
             listener.remove(this);
         }
     }
     
     /**
      * Sets the factory to active or not.  An active factory accepts new factory
      * connections.
      */
     public void setActive(boolean isActive)
     {
         if (this.isActive != isActive) {
             this.isActive = isActive;
             if (!isActive) {
                 if (active.size() == 0) {
                     listener.remove(this);
                     close();
                 } else {
                     log(ILogger.LOG_NOTICE, "Start draining " + active.size() + " active");
                 }
             }
         }
     }
 
     /**
      * Close down the factory.  This is called for inactive applications with no
      * active connections and by shutdown().  Subclasses can override this to do
      * factory-specific things.
      */
     protected void close()
     {
     }
 
     /**
      * Shutdown the factory: remove it from the listener and close all the
      * connections.  This is called if there is a security error while loading
      * one of the application's classes.
      */
     public final void shutdown()
     {
         if (listener != null) {
             listener.remove(this);
             listener = null;
             for (int i = active.size(); --i >= 0;) {
                 IApplication app = (IApplication)active.elementAt(i);
                 try {
                     app.close();
                 } catch (RuntimeException e) {
                     System.out.println("Ignoring: " + e);
                 }
             }
             active.clear();
             if (loader instanceof ILoader) {
                 ((ILoader) loader).close();
             }
         }
     }
 
     /**
      * Convert to string for debugging.
      */
     public String toString()
     {
         String nm = getClass().getName();
         int i = nm.lastIndexOf('$');
         if (i >= 0) {
             nm = nm.substring(i+1);
         }
         return nm + "[" + uri + "," + title + "]";
     }
 
     public void log(int priority, String msg)
     {
         if (listener != null) {
             listener.getLogger().log(priority, msg);
         } else {
             System.out.println("LOG: " + msg);
         }
     }
 
     private String getField(String name)
     {
         try {
             return (String) clazz.getField(name).get(null);
         } catch (NoSuchFieldException e) {
         } catch (IllegalAccessException e) {
         }
         return null;
     }
 
     protected String getQuery(String uri)
     {
         String queryStr = null;
     
         int index = uri.lastIndexOf("?");
         if (index > 0)
         {
             queryStr = uri.substring(uri.lastIndexOf("?")+1);
             //System.out.println("**** queryStr = ->"+queryStr+"<-");
         }
         return queryStr;
     }
 
     protected String removeQueryFromURI(String uri)
     {
         int index = uri.lastIndexOf("?");
         if (index > 0)
         {
             uri = uri.substring(0, uri.lastIndexOf("?"));
             //System.out.println("**** uri minus queryStr = ->"+uri+"<-");
         }
         return uri;
     }
 
     protected int getSeekTime(String queryStr)
     {
         int timeToSkip = 0;
         if (queryStr != null && queryStr.indexOf("Seek=") != -1)
         {
             // determine how far to seek.
             int index = queryStr.indexOf("Seek=")+5;
             String sub = queryStr.substring(index);
             NumberFormat numFormatter = NumberFormat.getInstance();
             numFormatter.setParseIntegerOnly(true);
             
             try {
                 timeToSkip = numFormatter.parse(sub).intValue();
             } catch (ParseException e) {
                 // can't parse the number, return 0
             }
         }
         return timeToSkip;
     }
 
 	/* (non-Javadoc)
 	 * @see com.tivo.hme.hosting.IFactory#init(com.tivo.hme.hosting.IContext)
 	 */
 	public void initFactory(String appClassName, ClassLoader loader, IArgumentList args) 
     {
         this.loader = loader;
 
         try {
             // load the class and make sure it has a public empty constructor
             this.clazz = Class.forName(appClassName, true, loader);
             if (!IApplication.class.isAssignableFrom(clazz))
             {
                 log(ILogger.LOG_WARNING, clazz.getName() + " does not implement IApplication, can't construct application.");
             }
             this.clazz.getConstructor(new Class[0]);
         } 
         catch (ClassNotFoundException e) {
             e.printStackTrace();
         } 
         catch (SecurityException e) {
             e.printStackTrace();
         } 
         catch (NoSuchMethodException e) {
             e.printStackTrace();
         }
         
         // set uri
         String uri = this.getField("URI");
         if (uri == null) {
             uri = appClassName;
     
             int d2 = appClassName.lastIndexOf('.');
             if (d2 >= 0) {
                 int d1 = appClassName.lastIndexOf('.', d2 - 1);
                 if (d1 >= 0) {
                     uri = appClassName.substring(d1 + 1, d2);
                 } else {
                     uri = appClassName.substring(0, d2);
                 }
             }
         }
         this.setURI(uri);
     
         // set title
         this.title = this.getField("TITLE");
         if (this.title == null) {
             String classNoPackage = appClassName.substring(appClassName.lastIndexOf('.') + 1);
             this.title = classNoPackage;
         }
         String user = System.getProperty("hme.user");
         if (user != null) {
             this.title += " " + user;
         }
 
         getFactoryData().put(IFactory.HME_APPLICATION_CLASSNAME, appClassName);
         getFactoryData().put(IFactory.HME_VERSION_TAG, IHmeProtocol.VERSION_STRING);
 
         // tell jar class loaders about the factory because jar class loaders
         // will react to security errors in the jar file
         if (loader instanceof ILoader) {
             ((ILoader) loader).setFactory(this);
         }
        
         // initialize the factory
         this.init(args);
         
     }
 
 	/* (non-Javadoc)
 	 * @see com.tivo.hme.hosting.IFactory#destroy()
 	 */
 	public void destroyFactory() {
         shutdown();
 	}
 
 	/* (non-Javadoc)
 	 * @see com.tivo.hme.hosting.IFactory#getAppName()
 	 */
 	public String getAppName() {
 		return getURI();
 	}
 
 	/* (non-Javadoc)
 	 * @see com.tivo.hme.hosting.IFactory#getAppTitle()
 	 */
 	public String getAppTitle() {
 		return getTitle();
 	}
 
     /* (non-Javadoc)
      * @see com.tivo.hme.hosting.IFactory#setAppName(java.lang.String)
      */
     public void setAppName(String appName) {
         setURI(appName);
     }
     
     /* (non-Javadoc)
      * @see com.tivo.hme.hosting.IFactory#setAppTitle(java.lang.String)
      */
     public void setAppTitle(String title) {
         setTitle(title);
     }
 
 	/* (non-Javadoc)
 	 * @see com.tivo.hme.hosting.IFactory#fetchAsset(com.tivo.hme.hosting.IHttpRequest)
 	 */
 	public InputStream fetchAsset(IHttpRequest req) {
         InputStream inStr = null;
         try {
             String path = req.getURI();
 
             // Note the getURI.length() - 1 below.  That's in case the original
             // URL is missing the trailing slash, e.g., http://host:port/bar?arg=val
             String relpath = URLDecoder.decode(path.substring(getAppName().length() - 1), "UTF-8");
 
             // If the relative path is empty (except for query-style arguments) then
             // this is an application instantiation.  Otherwise, pass the relative
             // path to the factory so it can handle it.
             if (relpath.startsWith("/")) {
                 relpath = relpath.substring(1);
             }
             
             inStr = handleHTTP(req, relpath);
         }
         catch (IOException ex) {
             inStr = null;
             log(ILogger.LOG_WARNING , "unable to fetch asset: " + req.getURI());
         }
 		return inStr;
 	}
 
     /* (non-Javadoc)
      * @see com.tivo.hme.hosting.IFactory#getFactoryData()
      */
     public Map getFactoryData() {
         if (factoryData == null) {
             factoryData = new HashMap();
         }
         return factoryData;
     }
 
     /* (non-Javadoc)
      * @see com.tivo.hme.hosting.IFactory#setListener(com.tivo.hme.hosting.IListener)
      */
     public void setListener(IListener listener) {
         this.listener = listener;
     }
 
 }
