 package ecologylab.net;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.*;
 import java.util.*;
 
 import javax.imageio.ImageIO;
 
 import ecologylab.generic.Debug;
 import ecologylab.generic.Files;
 import ecologylab.generic.Generic;
 import ecologylab.generic.StringTools;
 
 /**
  * Extends the URL with many features for the convenience and power of network programmers.
  * New class for manipulating and displaying URLs.
  * 
  * Uses lazy evaluation to minimize storage allocation.
  * 
  * @author andruid
  * @author eunyee
  * @author madhur
  */
 public class ParsedURL
 extends Debug
 {
 	/**
 	 * this is the no hash url, that is, the one with # and anything after it stripped out.
 	 */
    protected URL		url = null;
    /**
     * URL string with hash.
     */
    protected URL		hashUrl = null;
    
    protected URL		directory = null;
    
    protected String	string = null;
    
    /* lower case of the url string */
    protected String	lc = null;
    
    /* suffix string of the url */
    protected String	suffix = null;
    
    /* domain value string of the ulr */
    protected String	domain = null;
    
    File		file;
    
    public ParsedURL(URL url)
    {
       String hash = url.getRef();
       if (hash == null)
       {
       	 this.url		= url;
       	 this.hashUrl	= url;
       }
       else
       {
       	this.hashUrl	= url;
       	try 
 		{
 			// form no hash url (toss hash)
 			this.url		= new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile());
 		} catch (MalformedURLException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
       }
    }
    
    /**
     * Create a ParsedURL from a file.
     * If the file is a directory, append "/" to the path, so that relative URLs
     * will be formed properly later.
     * 
     * @param file
     */
    public ParsedURL(File file)
    {
    		try
 		{
    			String urlString = "file:///"+file.getAbsolutePath();
    			if (file.isDirectory())
    				urlString	+= "/";
 			this.url	= new URL(urlString);
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
       	if (isUndetectedMalformedURL(url))
   			return null;
       	return new ParsedURL(url);
       }
       catch (MalformedURLException e)
       {
       	Debug.println(urlErrorMsg(webAddr, errorDescriptor));
       	return null;
       }
       
     }
    
    /**
     * Determines a URL is malformed since Java fails to detect this. 
     * @param url
     * @return
     */
    private static boolean isUndetectedMalformedURL(URL url)
    {
 	   boolean isFileProtocol = url.getProtocol() == "file:";
 	   String host = url.getHost().trim();
 	   return ((!isFileProtocol && (host == "" || host == "/")) || (isFileProtocol && url.getPath().trim() != ""));
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
       
       ParsedURL result	= null;
       // if its not an absolute url string, parse url as relative
       if (webAddr.indexOf("://") == -1)
     	  result		= getRelativeToDocBase(webAddr, errorDescriptor);
       // otherwise, try forming it absolutely
       if (result == null)
       {
       	result		= getAbsolute(webAddr, errorDescriptor);
       }
       return result;
    }
    
    public final ParsedURL getRelative(String relativeURLPath, String errorDescriptor)
    {
    	 if (isFile())
 	 {
 		 File newFile	= Files.newFile(file, relativeURLPath);
 		 return new ParsedURL(newFile);
 	 }
 	 else
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
 			 URL resultURL	= new URL(base, relativeURLPath);
 		     result	= new ParsedURL(resultURL);
 		  }
 		  catch (MalformedURLException e)
 		  {
 		     println(urlErrorMsg(relativeURLPath, errorDescriptor));
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
  * Create ParsedURL using the codeBase(), and a relative url string. 
  * 
  * @return null if the codeBase is null.
  */
    public static ParsedURL getRelativeToCodeBase(String relativeURLPath, String errorDescriptor)
    {
 	  ParsedURL codeBase = Generic.codeBase();
 	  return (codeBase == null) ? null : codeBase.getRelative(relativeURLPath, errorDescriptor);
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
       	String path = url.getPath();
       	if (path != null)
       	{
       		result		= suffix(path.toLowerCase());
       	}
       	//TODO make sure that there isnt code somewhere testing suffix for null!
       	if (result == null)
       		result	= "";
         suffix		= result;
       }
       return result;
    }
   /**
    * Get the URL for the directory associated with this.
    * Requires looking for slash at the end, looking for a suffix or arguments.
    * As a result, we sometimes add a slash at the end, sometimes peel off the filename.
    * Result is cached a la lazy evaluation.
    * 
    * @return
    */
    public URL directory()
    {
	   	URL result	= StringTools.endsWithSlash(toString()) ?
			this.url : this.directory;
    		if (result == null)
    		{
    			String suffix	= suffix();
 			try 
 			{
 				String path		= url.getPath();
 				String args		= url.getQuery();
 				String protocol	= url.getProtocol();
 				String host		= url.getHost();
 				int port		= url.getPort();
 				if (suffix.length() == 0)
				{	// this is a directory that is unterminated by slash; we need to fix that
					
 					if (path.length() == 0)
 						result	= new URL(protocol, host, port, "/");
 					else
 					{
 						if ((args == null) || (args.length() == 0))
							result	= new URL(protocol, host, port, path + '/');
 						else // this is a tricky executable with no suffix
 						{
 							// result = null;
							// drop down into the next block, and peel off that suffix-less executable name
 						}
 					}
 				}
 				// else
 				if (result == null)
 				{	// you have a suffix, so we need to trim off the filename
 					int lastSlashIndex = path.lastIndexOf('/');
 					if (lastSlashIndex == -1)
 						// suffix, but not within any subdirectory
 						result	= new URL(protocol, host, port, "/");
 					else
 					{
 						String pathThroughLastSlash = path.substring(0, lastSlashIndex+1);
 						result	= new URL(protocol, host, port, pathThroughLastSlash);
 					}
 				}
 			} catch (MalformedURLException e)
 			{
 				debug("Unexpected ERROR forming directory.");
 				e.printStackTrace();
 			}
			this.directory		= result;
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
       String result	= ((afterDot == 0) || (afterDot < lastSlash)) ? "" 
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
 	   	return url;
    }
    
    public final URL hashUrl()
    {
 
    		if( hashUrl == null )
    			return url();
    		else
    			return hashUrl;
 
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
       "swf", "spl",   
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
 
    static final String[] imgSuffixStrings	= ImageIO.getReaderFormatNames();
   /* {
       "jpg", "jpeg", "pjpg", "pjpeg", "gif", "png", 
    }; */
    static final HashMap imgSuffixMap = 
 	      Generic.buildHashMapFromLCStrings(imgSuffixStrings); // formats from jdk contain lower & upper case
    
    static final String[] jpegMimeStrings	=
    {
       "jpg", "jpeg", "pjpg", "pjpeg", 
    };
    static final HashMap jpegSuffixMap = 
 	      Generic.buildHashMapFromStrings(jpegMimeStrings);
     static final String[] htmlMimeStrings	=
    {
       "html", "htm", "stm", "php", "jhtml", "jsp", "asp", "txt", "shtml",
       "pl", "plx", "exe"
    };
     static final String[] noAlphaMimeStrings		=
     {
  	   "bmp", "wbmp","jpg","jpeg", "pjpg","pjpeg"
     };
    static final HashMap noAlphaSuffixMap = Generic.buildHashMapFromStrings(noAlphaMimeStrings);
    
    static final HashMap htmlSuffixMap = 
       Generic.buildHashMapFromStrings(htmlMimeStrings);
    static final String[] pdfMimeStrings		=
    {
    		"pdf"
    };
    static final HashMap pdfSuffixMap =
    	  Generic.buildHashMapFromStrings(pdfMimeStrings);
    static final String[] rssMimeStrings		=
    {
    		"rss", "xml"
    };
    static final HashMap rssSuffixMap =
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
       return createFromHTML(addressString, false);
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
       return createFromHTML(this, addressString, fromSearchPage);      
    }
    protected static ParsedURL get(URL url, String addressString)
    {
    	  try 
 	  {
 		return new ParsedURL(new URL(url, addressString));
 	  } catch (MalformedURLException e) 
 	  {
 		 println("ParsedURL.get() cant from url from: " +
 		 		/*url +"\n\taddressString = "+*/ addressString);
 		 //e.printStackTrace();
 	  }
 	  return null;
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
  * @param tossHash if true, eliminate everything after 
  *  <code>#</code> in the URL.
  * 
  * @param tossArgs if true, eliminate everything after <code>&</code>
  * in the URL.
  * 
  * @return	The resulting ParsedURL. It may be null. It will never have 
  *		protocol <code>javascript:</code>.
  */
    public static ParsedURL createFromHTML(ParsedURL contextPURL,
 										  String addressString, 
 										  boolean fromSearchPage)
    {
       if ((addressString == null) || (addressString.length() == 0))
 		 return null;
       if( addressString.startsWith("#") )
       {
 			return get(contextPURL.url(), addressString);
       }
 
       String	lc	= addressString.toLowerCase();
       boolean javascript	= lc.startsWith("javascript:");
       
       // mine urls from javascript quoted strings
       if (javascript)
       {
 		 // !!! Could do an even better job here of mining quoted
 		 // !!! javascript strings.
 	//	 println("Container.newURL("+s);
 		 int http	= lc.lastIndexOf("http://");
 		 // TODO learn to mine PDFs as well as html!!
 		 int html	= lc.lastIndexOf(".html");
 		 int pdf	= lc.lastIndexOf(".pdf");
 //		 println("Container.newURL() checking javascript url:="+s+
 //			 " http="+http+" html="+html);
 		 if (http > -1)
 		 {  // seek absolute web addrs
 		 	if ((html > -1) && (http < html))
 		 	{
 		 		int end		= html + 5;
 			    addressString= addressString.substring(http, end);
 			    //println("Container.newURL fixed javascript:= " + s);
 			    lc		= lc.substring(http, end);
 			    javascript	= false;
 		 	}
 		 	else if ((pdf > -1) && (http < pdf))
 		 	{
 		 		int end = pdf + 4;
 			    addressString= addressString.substring(http, end);
 			    //println("Container.newURL fixed javascript:= " + s);
 			    lc		= lc.substring(http, end);
 			    javascript	= false;
 		 	}
 		 }
 		 else
 		 {
 		 	// seek relative addresses
 		 	
 		 	// need to find the bounds of a quoted string, if there is one
 		 }
 		 // !!! What we should really do here is find quoted strings
 		 // (usually with single quote, but perhaps double as well)
 		 // (use regular expressions?? - are they fast enough?)
 		 // and look at each one to see if either protocol is supported
 		 // or suffix is htmlMime or imgMime.
       }
       if (javascript)
       	return null;
 
       char argDelim	= '?';
       // url string always keep hash string.
       String hashString = StringTools.EMPTY_STRING;
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
       else
       {
       	// TODO do we really need to do any of this???????????????????????
 		 // 1) peel off hash
 		 int hashPos	= addressString.indexOf('#'); 
 //		 String hashString= StringTools.EMPTY_STRING;
 
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
 	    // 4) put back what we peeled off
 	    addressString	       += argString;
       	addressString	       += hashString;
     
      }
      int protocolEnd			= addressString.indexOf(":");
      if (protocolEnd != -1)
      {
      	// this is an absolute URL; check for supported protocol
      	String protocol			= addressString.substring(0, protocolEnd);
      	if (protocolIsUnsupported(protocol))
      		return null;
      }
      ParsedURL parsedUrl;
 	 if (contextPURL == null)
 	 {
 	 	parsedUrl = getAbsolute(addressString, "in createFromHTML()");
 	 }
 	 else
 	 	parsedUrl = get(contextPURL.directory(), addressString);
             
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
 
    /**
 	* Check whether the protocol is supported or not.
 	* Currently, only http and ftp are.
 	*/
    public boolean protocolIsSupported()
    {
       return (url != null) && protocolIsSupported(url.getProtocol());
    }
    
    /**
 	* Check whether the protocol is supported or not.
 	* Currently, only http and ftp are.
 	*/
    public static boolean protocolIsSupported(String protocol)
    {
       return supportedProtocols.containsKey(protocol);
    }
    
    /**
 	* Check whether the protocol is supported or not.
 	* Currently, only http and ftp are.
 	*/
    public boolean protocolIsUnsupported()
    {
       return (url != null) && protocolIsUnsupported(url.getProtocol());
    }
    
    /**
 	* Check whether the protocol is supported or not.
 	* Currently, only http and ftp are.
 	*/
    public static boolean protocolIsUnsupported(String protocol)
    {
       return unsupportedProtocols.containsKey(protocol);
    }
    
    /**
     * @return	true if this is an image file.
     */
     public boolean isImg()
     {
        return isImageSuffix(suffix());
     }
 
     /**
      * 
      * @param thatSuffix
      * @return	true if the suffix passed in is one for an image type that we can handle.
      */
 	public static boolean isImageSuffix(String thatSuffix)
 	{
 		return imgSuffixMap.containsKey(thatSuffix);
 	}
     /**
      * @return	true if this is a JPEG image file.
      */
     public boolean isJpeg()
     {
        return jpegSuffixMap.containsKey(suffix());
     }
     /**
      * @return	true if we can tell the image file wont have alpha, just from its suffix.
      * 			This is currently the case for jpeg and bmp.
      */
     public boolean isNoAlpha()
     {
        return jpegSuffixMap.containsKey(suffix());
     }
     
    /**
     * @param	suffix	file name suffix in lower case.
     */
    public boolean isHTML()
    {
 	   return htmlSuffixMap.containsKey(suffix());
    }
    
    public boolean isPDF()
    {
    	   return pdfSuffixMap.containsKey(suffix());
    }
    
    public boolean isRSS()
    {
    	   return rssSuffixMap.containsKey(suffix());
    }
    
    public String mimeType()
    {
 	   return isHTML() ? "text/html" : isPDF() ? "application/pdf" : isRSS() ? "xml/rss" : null;
    }
    
    /*
     * Check the suffix whether it is in the unsupportedMimes or not. 
     * If it is in the unsupportedMimes, return true, and if it is not, return false.
     */
    public boolean isUnsupported()
     {
         return unsupportedMimes.containsKey(suffix());
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
    /**
     * Return true if the other object is either a ParsedURL or a URL
     * that refers to the same location as this.
     */
    public boolean equals(Object other)
    {
 	  URL url = this.url;
       return ((other instanceof ParsedURL) && // (url != null) &&
 		 url.equals(((ParsedURL) other).url)) ||
 		 ((other instanceof URL) && url.equals((URL) other));
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
     
     /**
      * Form a new ParsedURL from this, and the args passed in. 
      * A question mark is appended to the String form of this, and then args are appended.
      * 
      * @param args
      * @return
      */
     public ParsedURL withArgs(String args)
     {
     	try 
     	{
 			URL url		= new URL(toString() + "?" + args);
 			return new ParsedURL(url);
 		} catch (MalformedURLException e)
 		{
 	    	return null;
 		}
 
     }
     /**
      * Returns the name of the file or directory denoted by this abstract pathname. 
      * This is just the last name in the pathname's name sequence. 
      * If the pathname's name sequence is empty, then the empty string is returned.
      * <p/>
      * Analagous to File.getName().
      * 
      * @return
      */
     public String getName()
     {
     	URL url			= this.url;
     	String path		= url.getPath();
     	int lastSlash	= path.lastIndexOf('/');
     	if (lastSlash > -1)
     	{
     		path		= path.substring(lastSlash+1);
     	}
     	return path;
     }
     
     /**
      * Promote the freeing of resources!
      * Free everything except the url, itself.
      * (Which means all can be reconstituted!)
      *
      */
     public void partialRecycle()
     {
     	domain			= null;
     	suffix			= null;
     	lc				= null;
     	string			= null;
     	directory		= null;
     	hashUrl			= null;
     }
     
     public PURLConnection connect(ConnectionHelper connectionHelper)
     {
     	URLConnection connection= null;
     	InputStream inStream	= null;
      	PURLConnection result	= null;
 
      // get an InputStream, and set the mimeType, if not bad
      if (isFile())
      {
      	File file = file();
      	if (file.isDirectory())
      		//result				= new FileDirectoryType(file, container, infoCollector);
      		connectionHelper.handleFileDirectory(file);
      	else
      	{
      		String suffix = suffix();
      		if (suffix != null)
      		{
  				//result			= getInstanceBySuffix(suffix);
  	    		//if (result != null)
  	    		if (connectionHelper.parseFilesWithSuffix(suffix))
  	    		{
  			      	try
  					{
  						inStream	= new FileInputStream(file);
  		 	    		result		= new PURLConnection(null, inStream);
  					} catch (FileNotFoundException e)
  					{
  						connectionHelper.badResult();
  			 			println("Can't open because FileNotFoundException: " + this);
  					}
  	    		}
      		}
      	}
      	return result;
      }
      else
      {	  // network based URL
     	  boolean bad			= false;
  	      try 
  	      {
  		    connection			= this.url().openConnection();
      
  		    // hack so google thinks we're a normal browser
  		    // (otherwise, it wont serve us)
  		    connection.setRequestProperty("user-agent", IE5_USER_AGENT);
 			
 			/*//TODO include more structure instead of this total hack!
 			if ("nytimes.com".equals(this.domain()))
 			{
 				String auth	= new sun.misc.BASE64Encoder().encode("fred66:fred66".getBytes());
 				connection.setRequestProperty("Authorization", auth);
 			}
  		    */
  		    
  		    String mimeType			= connection.getContentType();
  	
 		    //println("mimeType = '" + mimeType +"'");
 		    // no one uses the encoding header: connection.getContentEncoding();
 		    String unsupportedCharset = NetTools.isCharsetSupported(mimeType);
 			if (unsupportedCharset != null)
 		    {
 				connectionHelper.displayStatus("Cant process charset " + unsupportedCharset + " in " + this);
 		    	return null;
 		    }
  	
  		    // notice if url changed between request and retrieved connection
  		    // if so, this is a server-side redirect
  		    URL connectionURL			= connection.getURL();
  		    
  		    if (!this.equals(connectionURL)) // follow redirects!
  		    {
  		    	// avoid doubly stuffed urls
  		    	String connectionFile	= connectionURL.getFile();
  		    	String file				= url().getFile();
  	
  		    	if ((file.indexOf("http://") != -1) ||
  		    			(connectionFile.indexOf("http://") == -1))
  		    	{
  		    		if (connectionHelper.processRedirect(connectionURL))
  		    			inStream		= connection.getInputStream();
 		    			
  		    	}
  		    	else
 		    		println("WEIRD: skipping double stuffed url: " + connectionURL); 		    		
  		    }  
  		    else	// no redirect, eveything is kewl
 	    			inStream			= connection.getInputStream();
  	      }
  	      catch (FileNotFoundException e)
  	      { 
  			 bad			= true;
  			 println("Can't open because FileNotFoundException: " + this);
  	     }
  	      catch (IOException e)
  	      { 
  			 bad			= true;
  			 println("Can't open because " + e +" " + this);
  		  }
  	      catch (Exception e)	   // catch all exceptions, including security
  	      { 
  	      	bad				= true;
  	      	println("connect() caught " + e);
  	      	e.printStackTrace();
  	      }
  	      return ((inStream == null) || bad)? null : new PURLConnection(connection, inStream);
        } // end else network based URL
 
        //TODO -- how are the headers (like ContentType) read?
        // is the inputStream really created automatically for us behind the scences???
        // if so, we need to get it, close it, disconnect() it, etc. --
        // just because we read the headers???
     }
     
     final static String IE5_USER_AGENT	= 
 	      "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0";
 }
