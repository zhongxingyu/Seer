 /*
  * Modified by Eunyee Koh
  */
 
 package cm.generic;
 
 import java.io.File;
 import java.net.*;
 import java.util.*;
 
 /**
  * New class for manipulating and displaying URLs.
  * 
  * Uses lazy evaluation to minimize storage allocation.
  */
 public class ParsedURL
 extends Debug
 {
    protected URL		url = null;
    protected String	string = null;
    
    /* lower case of the url string */
    protected String	lc = null;
    
    /* suffix string of the url */
    protected String	suffix = null;
    
    /* domain value string of the ulr */
    protected String	domain = null;
    
    File		file;
    
    /* ParsedURL Constructor */
    public ParsedURL()
    {
    }
    
    public ParsedURL(URL url)
    {
       this.url	= url;     
    }
    
    public ParsedURL(File file)
    {
    		try
 		{
 			this.url	= new URL("file://"+file.getAbsolutePath());
 		} catch (MalformedURLException e)
 		{
 			e.printStackTrace();
 		}
    		this.file	= file;
    }
    /* 
     * Constructor with a url string parameter. 
     * get absolute URL with getAbsolute() method. 
     */
 /*
    public ParsedURL(String urlString)
    {
    	    // The second parameter of getAbolute method is error description. 
    		this.url = getAbsolute(urlString, "").url();
    }
 */
    ///////////////////////////////////////////////////////////////////////
    
    /* 
     * Create Absolute URL
     * 
     * @param	webAddr	url string
     * @param	errorDescriptor	which will be printed out in the trace file if there is something happen
     * 			converting from the url string to URL.	
     * @return URL from url string parameter named webAddr.
     */
    public static ParsedURL getAbsolute(String webAddr, String errorDescriptor)
    {
        try
       {
       	URL url		= new URL(webAddr);
       	return new ParsedURL(url);
       }
       catch (MalformedURLException e)
       {
       	Debug.println(urlErrorMsg(webAddr, errorDescriptor));
       	return null;
       }
     }
 /*   
    public URL getURL(String webAddr)
    {
       return getURL(webAddr, "");
    }
 */   
 /**
  * Uses an absolute URL, if the String parameter looks like that,
  * or one that's relative to docBase, if it looks a relative URL.
  */
    public static ParsedURL getRelativeOrAbsolute(String webAddr, String errorDescriptor)
    {
       if (webAddr == null)
       	return null;
       
       // if its a relative address string, make it that way
       ParsedURL result	= getRelativeToDocBase(webAddr, errorDescriptor);
       // otherwise, try forming it absolutely
       if (result == null)
       {
       	result		= getAbsolute(webAddr, errorDescriptor);
       }
       return result;
    }
    
    public final ParsedURL getRelative(String relativeURLPath, String errorDescriptor)
    {
    	 return getRelative(url, relativeURLPath, errorDescriptor);
    }
    
 /**
  * Form a new ParsedURL, relative from a supplied base URL.
  * @param relativeURLPath
  * @param errorDescriptor
  * @return
  */ 
    public static ParsedURL getRelative(URL base, String relativeURLPath, String errorDescriptor)
    {
       if (relativeURLPath == null)
       	return null;
       
       ParsedURL result	= null;
        if (!relativeURLPath.startsWith("http://") && !relativeURLPath.startsWith("ftp://"))
        {
 	  try
 	  {
 	     result	= new ParsedURL(new URL(base, relativeURLPath));
 	  }
 	  catch (MalformedURLException e)
 	  {
 	     Debug.println(urlErrorMsg(relativeURLPath, errorDescriptor));
 	  }
        }      
       
       return result;
    }
    
    public static URL getURL(URL base, String path, String error)
    {
       // ??? might want to allow this default behaviour ???
       if (path == null)
 	 return null;
       try 
       {
 		//System.err.println("\nGENERIC - base, path, error = \n" + base + "\n" + path);
 		URL newURL = new URL(base,path);
 		//System.err.println("\nNEW URL = " + newURL);
 	 return newURL;
       } catch (MalformedURLException e) 
       {
 	 if (error != null)
 	    throw new Error(e + "\n" + error + " " + base + " -> " + path);
 	 return null;
       }
    }
    
 /** 
  * Create ParsedURL with doc base and relative url string. 
  * 
  * @return null if the docBase is null.
  */
    public static ParsedURL getRelativeToDocBase(String relativeURLPath, String errorDescriptor)
    {
    		ParsedURL docBase = Generic.docBase();
    		return (docBase == null) ? null : docBase.getRelative(relativeURLPath, errorDescriptor);
    } 
    
 /**
  * Return url error message string.
  */
    static String urlErrorMsg(String webAddr, String errorDescriptor)
    {
       return "CANT open " + errorDescriptor + " " + webAddr +
 	      " because it doesn't look like a web address.";
    }    
 
 /**
  * Uses lazy evaluation to minimize storage allocation.
  * 
  * @return The URL as a String.
  */
   public String toString()
    {
       String result	= string;
       if (result == null)
       {
       	result		= StringTools.pageString(url);
       	string		= result;
       }
       return result;
    }
 /**
  * Uses lazy evaluation to minimize storage allocation.
  * 
  * @return Lower case rendition of the URL String.
  */
    public String lc()
    {
       String result	= lc;
       if (result == null)
       {
       	result		= toString().toLowerCase();
       	lc		= result;
       }
       return result;
    }
 /**
  * Uses lazy evaluation to minimize storage allocation.
  * 
  * @return	The suffix of the filename, in lower case.
  */
    public String suffix()
    {
       String result	= suffix;
       if (result == null)
       {
       	result		= suffix(lc());
       	suffix		= result;
       }
       return result;
    }
    
    /**
     * Uses lazy evaluation to minimize storage allocation.
     * 
     * @return	The domain of the URL.
     */
    public String domain()
    {
       String result	= domain;
       if (result == null)
       {
       	result		= StringTools.domain(url);
       	domain		= result;
       }
       return result;
    }
 /**
  * @return	The suffix of the filename, in whatever case is found in the input string.
  */
   public static String suffix(String lc)
    {
       int afterDot	= lc.lastIndexOf('.') + 1;
       int lastSlash	= lc.lastIndexOf('/');
       String result	= 
 	 ((afterDot == 0) || (afterDot < lastSlash)) ? "" 
 	    : lc.substring(afterDot);
       return result;
    }
 
   /**
    * Uses lazy evaluation to minimize storage allocation.
    * 
    * @return	the URL.
    */
    public final URL url()
    {
    	if( url == null )
    		return null;
    	return url;
    }
    
    /**
     * Uses lazy evaluation to minimize storage allocation.
     * 
     * @return	the ParsedURL.
     */
    public ParsedURL noAnchorParsedURL()
    {
       if (url.getRef() == null)
       	return this;
       // else
       return new ParsedURL(StringTools.urlNoAnchor(url));
    }
    
    /**
     * Uses lazy evaluation to minimize storage allocation.
     * 
     * @return	the no anchor URL.
     */
    public URL noAnchorURL()
    {
     if (url.getRef() == null)
    	 return this.url();
          // else
     return StringTools.urlNoAnchor(url);   	
    }
    
    /*
     * return noAnchor no query page string 
     */
    public String noAnchorNoQueryPageString()
    {
       if ((url.getRef() == null) && (url.getQuery() == null))
 	 return toString();
       else
 	 return StringTools.noAnchorNoQueryPageString(url);
    }
    
    /*
     * return no anchor no page string.
     */
    public String noAnchorPageString()
    {
    	return StringTools.noAnchorPageString(url);
    }
 /**
  * @return true if the suffix of this is equal to that of the argument.
  */
    public final boolean hasSuffix(String s)
    {
    	  return lc().endsWith(s);
  //     return suffix().equals(s);
    }
 
    final static String unsupportedMimeStrings[]	=
    {
       "ai", "bmp", "eps", "ps", "psd", "svg", "tif", "vrml",
       "doc", "xls", "pps", "ppt", "adp", "rtf", 
       "vbs", "vsd", "wht", 
       "aif", "aiff", "aifc", "au", "mp3", "wav", "ra", "ram", 
       "wm", "wma", "wmf", "wmp", "wms", "wmv", "wmx", "wmz",
       "avi", "mov", "mpa", "mpeg", "mpg", "ppj",
       "swf", "spl", "pdf",
 // "pdf" will be deleted by Eunyee
 //	  "swf", "sql",  
       "qdb", 
       "cab", "chm", "gzip", "hqx", "jar", "lzh", "tar", "zip", 
       "xml", "xsl",
    };
    final static HashMap unsupportedMimes	= 
       Generic.buildHashMapFromStrings(unsupportedMimeStrings);
 
    static final String[] unsupportedProtocolStrings = 
    {
       "mailto", "vbscript", "news", "rtsp", "https",
    };
    static final HashMap unsupportedProtocols = 
       Generic.buildHashMapFromStrings(unsupportedProtocolStrings);
 
    static final String[] supportedProtocolStrings = 
    {
       "http", "ftp",
    };
    static final HashMap supportedProtocols = 
       Generic.buildHashMapFromStrings(supportedProtocolStrings);
 
    static final String[] imgMimeStrings	=
    {
       "jpg", "jpeg", "pjpg", "pjpeg", "gif", "png", 
    };
    static final HashMap imgMimes = 
       Generic.buildHashMapFromStrings(imgMimeStrings);
    static final String[] htmlMimeStrings	=
    {
       "html", "htm", "stm", "php", "jhtml", "jsp", "asp", "txt", "shtml",
       "pl", "plx", "exe"
    };
    static final HashMap htmlMimes = 
       Generic.buildHashMapFromStrings(htmlMimeStrings);
    static final String[] pdfMimeStrings		=
    {
    		"pdf"
    };
    static final HashMap pdfMimes =
    	  Generic.buildHashMapFromStrings(pdfMimeStrings);
    static final String[] rssMimeStrings		=
    {
    		"rss", "xml"
    };
    static final HashMap rssMimes =
    	  Generic.buildHashMapFromStrings(rssMimeStrings);
 
 
 /**
  * Called while processing (parsing) HTML.
  * Used to create new <code>ParsedURL</code>s from urlStrings in
  * response to such as the <code>a</code> element's <code>href</code>
  * attribute, the <code>img</code> element's <code>src</code> attribute,
  * etc.
  * <p>
  * Does processing of some fancy stuff, like, in the case of
  * <code>javascript:</code> URLs, it mines them for embedded absolute
  * URLs, if possible, and uses only those embedded URLs.
  * 
  * @param addressString	This may be specify a relative or absolute url.
  * 
  * @return	The resulting ParsedURL. It may be null. It will never have 
  *		protocol <code>javascript:</code>.
  */
    public ParsedURL createFromHTML(String addressString)
    {
       return createFromHTML( addressString, false, false);
    }
 /**
  * Called while processing (parsing) HTML.
  * Used to create new <code>ParsedURL</code>s from urlStrings in
  * response to such as the <code>a</code> element's <code>href</code>
  * attribute, the <code>img</code> element's <code>src</code> attribute,
  * etc.
  * <p>
  * Does processing of some fancy stuff, like, in the case of
  * <code>javascript:</code> URLs, it mines them for embedded absolute
  * URLs, if possible, and uses only those embedded URLs.
  * 
  * @param addressString	This may be specify a relative or absolute url.
  * 
  * @param fromSearchPage If false, then add <code>/</code> to the end
  * of the URL if it seems to be a directory.
  * 
  * @return	The resulting ParsedURL. It may be null. It will never have 
  *		protocol <code>javascript:</code>.
  */
    public ParsedURL createFromHTML(String addressString, 
 				   boolean fromSearchPage)
    {
       return createFromHTML(addressString, fromSearchPage, false);      
    }
 
 /**
  * Called while processing (parsing) HTML.
  * Used to create new <code>ParsedURL</code>s from urlStrings in
  * response to such as the <code>a</code> element's <code>href</code>
  * attribute, the <code>img</code> element's <code>src</code> attribute,
  * etc.
  * <p>
  * Does processing of some fancy stuff, like, in the case of
  * <code>javascript:</code> URLs, it mines them for embedded absolute
  * URLs, if possible, and uses only those embedded URLs.
  * 
  * @param addressString	This may be specify a relative or absolute url.
  * 
  * @param fromSearchPage If false, then add <code>/</code> to the end
  * of the URL if it seems to be a directory.
  * 
  * @param tossArgsAndHash if true, eliminate everything after <code>?</code>
  * and <code>#</code> in the URL.
  * 
  * @return	The resulting ParsedURL. It may be null. It will never have 
  *		protocol <code>javascript:</code>.
  */
    public ParsedURL createFromHTML(String addressString, 
 				   boolean fromSearchPage, 
 				   boolean tossArgsAndHash)
    {
 	  return createFromHTML(url(), addressString, fromSearchPage, 
 							tossArgsAndHash);
    }
 /**
  * Called while processing (parsing) HTML.
  * Used to create new <code>ParsedURL</code>s from urlStrings in
  * response to such as the <code>a</code> element's <code>href</code>
  * attribute, the <code>img</code> element's <code>src</code> attribute,
  * etc.
  * <p>
  * Does processing of some fancy stuff, like, in the case of
  * <code>javascript:</code> URLs, it mines them for embedded absolute
  * URLs, if possible, and uses only those embedded URLs.
  * 
  * @param addressString	This may be specify a relative or absolute url.
  * 
  * @param fromSearchPage If false, then add <code>/</code> to the end
  * of the URL if it seems to be a directory.
  * 
  * @param tossArgsAndHash if true, eliminate everything after <code>?</code>
  * and <code>#</code> in the URL.
  * 
  * @return	The resulting ParsedURL. It may be null. It will never have 
  *		protocol <code>javascript:</code>.
  */
    public static ParsedURL createFromHTML(URL contextURL,
 										  String addressString, 
 										  boolean fromSearchPage, 
 										  boolean tossArgsAndHash)
    {
       if ((addressString == null) || (addressString.length() == 0))
	 return null;
 //      debugA("addressString="+addressString);
       
 	 if (contextURL != null)
 	 {
 		 String urlString = contextURL.toString();	 				 
 	
 		 //if the base url is a folder but not ended with "/", we need to add one "/"
 		 if ((urlString.charAt(urlString.length()-1))!='/')
 		 {
 		 	//println(utlString.lastIndexOf('/')+ " : " + utlString.length());
 		 	String lastPart = urlString.substring(urlString.lastIndexOf("/"), urlString.length());
 		 		
 		 	if(lastPart.indexOf(".")!=-1)	 	
 		 	lastPart =  urlString.substring(urlString.lastIndexOf(".")+1);	 		 	
 		 	if ((!imgMimes.containsKey(lastPart))&&(!htmlMimes.containsKey(lastPart)))		 	
 				// use new ParsedURL constructor.
 		 		contextURL = getAbsolute(urlString, "").url();
 	
 		 }
 	 }
       URL newUrl = null;
       String	lc	= addressString.toLowerCase();
       boolean javascript	= lc.startsWith("javascript:");
       // special: seeking to data mine urls from javascript quoted strings
       if (javascript)
       {
 		 // !!! Could do an even better job here of mining quoted
 		 // !!! javascript strings.
 	//	 println("Container.newURL("+s);
 		 int http	= lc.lastIndexOf("http://");
 		 int html	= lc.lastIndexOf(".html");
 //		 println("Container.newURL() checking javascript url:="+s+
 //			 " http="+http+" html="+html);
 		 if ((http > -1) && (html > -1) && (http < html))
 		 {
 		    addressString= addressString.substring(http, html + 5);
 //		    println("Container.newURL fixed javascript:= " + s);
 		    lc		= lc.substring(http, html + 5);
 		    javascript	= false;
 		 }
 		 // !!! What we should really do here is find quoted strings
 		 // (usually with single quote, but perhaps double as well)
 		 // (use regular expressions?? - are they fast enough?)
 		 // and look at each one to see if either protocol is supported
 		 // or suffix is htmlMime or imgMime.
       }
       if (javascript)
 	 return null;
 
       // used to check for mime here!!! now we do that where the crawler is 
       // if !crawlable(lc) ...
 
       char argDelim	= '?';
       if (fromSearchPage)
       {
 	 // handle embedded http://
 	 int lastHttp	= addressString.lastIndexOf("http://");
 	 // usually ? but could be &
 	 if (lastHttp > 0)
 	 {
 	    // this is search engine crap
 	    addressString		= addressString.substring(lastHttp);
 //	    debugA("now addressString="+addressString);
 	    // handle any embedded args (for google mess)
 	    argDelim		= '&';
 	 }
       }
 //      if (!fromSearchPage)
       else
       {
 	 // 1) peel off hash
 	 int hashPos	= addressString.indexOf('#'); 
 	 String hashString= StringTools.EMPTY_STRING;
 	 if (hashPos > -1)
 	 {
 	    hashString	= addressString.substring(hashPos);
 	    addressString		= addressString.substring(0, hashPos);
 	 }
 	 // 2) peel off args
 	 int argPos	= addressString.indexOf(argDelim);
 	 String argString	= StringTools.EMPTY_STRING;
 	 if (argPos > -1)
 	 {
 	    argString	= addressString.substring(argPos);
 	    addressString		= addressString.substring(0, argPos);
 	 }
 	 else
 	 {
 	    // 3) if what's left is a directory (w/o a mime type),add slash
 	    int endingSlash	= addressString.lastIndexOf('/');
 	    int lastChar	= addressString.length() - 1;
 	    if (endingSlash == -1)
 	       endingSlash++;
 	    if ((lastChar > 0) &&
 		(lastChar != endingSlash) &&
 		(addressString.substring(endingSlash).indexOf('.') == -1))
 	       addressString	       += '/';
 	 }
 	 if (!tossArgsAndHash)
 	    // 4) put back what we peeled off
 	    addressString	       += argString + hashString;
       }
       
       ParsedURL parsedUrl;
       try
       {
 		 if (contextURL == null)	   
 			newUrl = new URL(addressString);
 		 else
 		 {
 			newUrl = new URL(contextURL, addressString);	       
 		 }	      
 		 
 		 parsedUrl		= new ParsedURL(newUrl);	 
       }
       catch (MalformedURLException e)
       {
 		 parsedUrl		= null;
 		 println("ParsedURL.createFromHTML() cant access malformed url:\n\t" +
 				 contextURL +"\n\taddressString = "+ addressString);
		 e.printStackTrace();
       }
             
       return parsedUrl;
    }
 /**
  * 
  * @return A String version of the URL path, in which all punctuation characters have been changed into spaces.
  */
    public String removePunctuation()
    {
    	  return StringTools.removePunctuation(toString());
    }
  
    /*
     * return true if they have same domains. 
     * return false if they have different domains. 
     */
    public boolean sameDomain(ParsedURL other)
    {
    		return (other != null) && domain().equals(other.domain());
    }
    
    /*
     * return true if they have same hosts.
     * return false if they have different hosts.
     */
    public boolean sameHost(ParsedURL other)
    {
    		return (other != null) && url.getHost().equals(other.url().getHost());
    }
    
   
    /**
     * @param lc	String form of a url, already converted to lower case.
     * 
     * @return true if this seems to be a web addr we can crawl to.
     * 			(currently that means html).
     **/
    public boolean crawlable()
     {
        return protocolIsSupported() && !unsupportedMimes.containsKey(suffix());
     }  
 
    	/*
    	 * check whether the protocol is supported or not from unsupportedProtocols.
    	 */
    public boolean protocolIsSupported()
    {
       return
 	 (url != null) && supportedProtocols.containsKey(url.getProtocol());
    }
    
    /**
     * @param	suffix	file name suffix in lower case.
     */
     public  boolean isImg()
     {
        suffix = suffix();
         return (suffix != null) && (suffix.length() != 0) && 
 		 	imgMimes.containsKey(suffix);
     }
     
    /**
     * @param	suffix	file name suffix in lower case.
     */
    public boolean isHTML()
       {
 	 suffix = suffix();
         return htmlMimes.containsKey(suffix);
       }
    
    public boolean isPDF()
    {
    		suffix = suffix();
    		return pdfMimes.containsKey(suffix);
    }
    
    public boolean isRSS()
    {
    		suffix = suffix();
    		return rssMimes.containsKey(suffix);
    }
    
    public String mimeType()
    {
    		if( isHTML() )
    			return "text/html";
    		else if( isPDF() )
    			return "application/pdf";
    		else if( isRSS() )
    			return "xml/rss";
    		else
    			return null;
    }
    
    /*
     * Check the suffix whether it is in the unsupportedMimes or not. 
     * If it is in the unsupportedMimes, return true, and if it is not, return false.
     */
    public boolean isUnsupported()
     {
        suffix = suffix();
         return unsupportedMimes.containsKey(suffix);
     }
    
    /*
     * return the inverse of isUnsupported().
     * Then, if the suffix is in the unsupportedMimes, return false, and if it is not, return true.
     */
    public boolean supportedMime()
    {
       return !isUnsupported();
    }
    /**
     * @return		the path to the directory assosciated w this URL.
     */
    public String directoryString()
    {
          String path	= url.getFile();
          int	args		= path.indexOf("?");
 
          if (args > -1)
          	path		= path.substring(0,args);
          int	lastSlash	= path.lastIndexOf("/");
          int	lastDot		= path.lastIndexOf(".");
          if (lastDot > lastSlash)
          	path		= path.substring(0,lastSlash); 
 
          int portNum		= url.getPort();
          String port		= (portNum == -1) ? "" : ":" + portNum;
 	 String host		= url.getHost();
          String protocol	= url.getProtocol();
 
 	 int stringLength	= protocol.length() + 3 + host.length()
 	    + port.length() + path.length();
 
 	 StringBuffer buffy	= new StringBuffer(stringLength);
 	 buffy.append(protocol).append("://").append(host).
 	    append(port).append(path);
 
          return buffy.toString();  // dont copy; wont reuse buffy
    }
    public boolean equals(Object o)
    {
       return (o instanceof ParsedURL) && 
 	 ((ParsedURL) o).url().equals(this.url);
    }
    public int hashCode()
    {
       return /* (url == null) ? -1 : */ url.hashCode();
    }
    String shortString;
 /**
  * A shorter string for displaing in the modeline for debugging, and
  * in popup messages.
  */
     public String shortString()
    {
       String shortString	= this.shortString;
       if (shortString == null)
       {
 	 URL url		= this.url;
 	 if (url == null)
 	    shortString		= "null";
 	 else
 	 {
 	    String file		= url.getFile();
 	    shortString		= url.getHost() + "/.../" + 
 	       file.substring(file.lastIndexOf('/') + 1);
 	 }
 	 this.shortString	= shortString;
       }
       return shortString;
    }
     
     /**
      * True if this ParsedURL represents an entity on the local file system.
      * @return
      */
     public boolean isFile()
     {
     	return file != null;
     }
     
     /**
      * @return The file system object associated with this, if this is an entity on
      * the local file system, or null, otherwise.
      */
     public File file()
     {
     	return file;
     }
 }
