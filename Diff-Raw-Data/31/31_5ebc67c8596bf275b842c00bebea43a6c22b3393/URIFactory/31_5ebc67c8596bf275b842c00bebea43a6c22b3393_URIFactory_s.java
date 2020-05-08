 /*
  * Copyrighted 2012-2013 Netherlands eScience Center.
  *
  * Licensed under the Apache License, Version 2.0 (the "License").  
  * You may not use this file except in compliance with the License. 
  * For details, see the LICENCE.txt file location in the root directory of this 
  * distribution or obtain the Apache License at the following location: 
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  * 
  * For the full license, see: LICENCE.txt (located in the root folder of this distribution). 
  * ---
  */
 // source: 
 
 package nl.esciencecenter.ptk.net;
 
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 import nl.esciencecenter.ptk.util.StringUtil;
 import nl.esciencecenter.ptk.util.URLUTF8Encoder;
 
 /**
  * URI Factory. Most methods are shadowed from URI so it can also be used as URI Proxy object.
  * Use this factory to chain a sequence of URI modification methods.
  * 
  * Use toURI() to create the resulting URI. 
  * 
  * @author Piter T. de Boer
  */
 public class URIFactory implements Serializable // because it can be embedded in a URI like class. 
 {
     private static final long serialVersionUID = 6425053125412658256L;
 
     /** Path seperator character for URIs = '/' */
     public final static char SEP_CHAR = '/';
 
     /** Windows backslash or '\\' */
     public final static char DOS_SEP_CHAR = '\\';
 
     /** Seperator character but as String ( "/" ) */
     public final static String SEP_CHAR_STR = "" + SEP_CHAR;
 
     public final static char QUERY_CHAR = '?';
 
     public final static char FRAGMENT_CHAR = '#';
 
     /**
      * Default URI attribute separator '&amp;' in URL: http://../?ArgumentA=1&amp;ArgumentB=2"
      */
     public static final String ATTRIBUTE_SEPERATOR = "&";
 
     /** URI list seperator ';' used to parse (create) URI string representations */
     public static final String URI_LIST_SEPERATOR = ";";
 
     // =======================================================================
     // Class Methods
     // =======================================================================
 
     public static String uripath(String orgpath)
     {
         return uripath(orgpath, true);
     }
 
     public static String uripath(String orgpath, boolean makeAbsolute)
     {
         // use local File Seperator to normalize path.
         return uripath(orgpath, makeAbsolute, java.io.File.separatorChar);
     }
 
     /**
      * Produce URI compatible path and do other normalizations.
      * <ul>
      * <li>Flip <code>localSepChar</code> to (URI compatible) forward slashes
      * <li>changes DOS paths into absolute DOS paths: for example: 'c:' into
      * '/c:/'
      * <li>prefixes all paths with '/' to make it absolute , unless
      * makeAbsolute=false
      * </ul>
      * 
      * @param orgpath
      *            - original path.
      * @param makeAbsolute
      *            - prefix optional relative paths with '/' to make them
      *              absolute.
      * @param localSepChar
      *            - separator char to 'flip' to URI seperator char '/'
      */
     public static String uripath(String orgpath, boolean makeAbsolute, char localSepChar)
     {
         if (orgpath == null)
             return ""; // default path="";
 
         if (orgpath.length() == 0)
             return ""; // default path="";
 
         //
         // Ia) If platform seperator char if '\', replace with URI seperator
         // '/'.
         //
 
         String newpath = orgpath;
         // For now convert ALL backslashes:
         newpath = orgpath.replace('\\', SEP_CHAR);
 
         // Ib) strip optional double slashes "c:\subdir\/path" =>
         // "c:/subdir//path" => c:/subdir/path
         newpath = newpath.replaceAll(SEP_CHAR + "+", SEP_CHAR_STR);
 
         // II) Convert relative path to absolute by inserting '/'
         // c:/subdir/path => /c:/subdir/path
 
         if (makeAbsolute == true)
         {
             if (newpath.charAt(0) != SEP_CHAR)
             {
                 newpath = SEP_CHAR + newpath;
             }
         }
 
         //
         // III) Windows conversion
         // "C:" => /C:/" is always absolute !
 
         // Canonical paths vs. Absolute paths:
         /*
          * Yet another windows relative path hack: Windows interprets "C:..."
          * also as relative if the current directory on "C:" is 'C:/windows',
          * the path "C:subdir" will result in "C:/windows/subdir"
          * 
          * Add extra '/' to make it absolute C:/ Note that java (under windows)
          * accepts paths like '/c:/dir' !
          */
 
         // newpath=newpath.replaceAll("(/[a-zA-Z]:)([^/])","\1/\2");
 
         //
         // IIIa insert '/' if path starts with "C:" or "[a-zA-Z]:"
         // IIIb convert "/c:path => /c:/path"
 
         String dosPrefixRE = "[/]*[a-zA-Z]:.*";
 
         // detect DOS path:
         if ((newpath.length() >= 2) && (newpath.matches(dosPrefixRE)))
         {
             // prefix with '/' to normalize absolute DOS path :
             if (newpath.charAt(0) != '/')
                 newpath = "/" + newpath;
 
             // insert "/" between ":" and path:
             // "/C:<path>" => "/C:/<path>"
             if ((newpath.length() >= 4) && (newpath.charAt(2) == ':') && (newpath.charAt(3) != SEP_CHAR))
             {
                 newpath = "/" + newpath.charAt(1) + ":/" + newpath.substring(3);
             }
         }
 
         // convert: "/C:" => "/C:/"
         if ((newpath.length() == 3) && (newpath.charAt(2) == ':'))
         {
             newpath = newpath + SEP_CHAR;
         }
         else if ((newpath.length() == 4) && (newpath.charAt(3) == SEP_CHAR))
         {
             // keep "/C:/..."
         }
         else if ((newpath.length() > 1) && (newpath.charAt(newpath.length() - 1) == SEP_CHAR))
         {
             // Strip last '/' if it isn't an absolute (Windows) drive path path
             // like example: '/C:/'
 
             newpath = newpath.substring(0, newpath.length() - 1);
         }
 
         // finally: now strip multiple slashes '/' to normalise the path
         newpath = newpath.replaceAll("/+", "/");
 
         // Debug("uri path="+newpath);
         return newpath;
     }
 
     public static String stripExtension(String name)
     {
         if (name == null)
             return null;
 
         int index = name.length();
         index--;
 
         // scan last part of path
 
         while ((index >= 0) && (name.charAt(index) != '.'))
         {
             index--;
         }
 
         // index now points to '.' char or is -1; (before beginning of the name)
 
         if (index < 0)
             return name; // no dot => NO extension !
 
         return name.substring(0, index);
     }
 
     public static String extension(String name)
     {
         if (name == null)
             return null;
 
         int index = name.length();
         index--;
 
         // scan last part of path
 
         while ((index >= 0) && (name.charAt(index) != '.'))
         {
             index--;
         }
 
         // index now points to '.' char or is -1; (before beginning of the name)
         index++; // skip '.';
 
         return name.substring(index, name.length());
     }
 
     public static String encode(String string)
     {
         String encoded = URLUTF8Encoder.encode(string);
         return encoded;
     }
 
     /** Returns basename part (last part) of path String. */
     public static String basename(String path)
     {
         // default cases: null,empty and root path:
 
         if (path == null)
             return null;
 
         if (path.equalsIgnoreCase(""))
             return "";
 
         int index = 0;
         int strlen = path.length();
 
         index = strlen - 1;// start at end of string
 
         if (path.equalsIgnoreCase(SEP_CHAR_STR))
             return SEP_CHAR_STR;
 
         // special case, path ENDS with '/' which must be ignored
 
         if (path.charAt(index) == SEP_CHAR)
         {
             index--;
             strlen--;
         }
 
         while ((index >= 0) && (path.charAt(index) != SEP_CHAR))
         {
             index--;
         }
 
         index++;
 
         // index points to character after '/' or is zero
         return path.substring(index, strlen);
     }
 
     /**
      * Returns the dirname part of the URI compatible path ! (parent directory
      * path) of path. Note: use URI.uripath to sanitize and normalize a path!
      * <p>
      * Special cases:
      * <ul>
      * <li>dirname of null is null
      * <li>dirname of the empty string "" is ""
      * <li>The dirname of "/" = "/"
      * </ul>
      * 
      * @see basename
      */
     public static String dirname(String path)
     {
         // null path
         if (path == null)
             return null;
 
         int index = 0;
         int strlen = path.length();
 
         index = strlen - 1;// start at end of string
 
         // Empty and root path:
 
         // relative path, cannot return dirname
         if (path.equalsIgnoreCase(""))
             return "";
 
         // root path of root is root itself
         if (path.equalsIgnoreCase(SEP_CHAR_STR))
             return SEP_CHAR_STR;
 
         // special case, path ENDS with '/' which must be ignored
         if (path.charAt(index) == SEP_CHAR)
         {
             index--;
             strlen--;
         }
 
         // move backwards to first seperator
         while ((index >= 0) && (path.charAt(index) != SEP_CHAR))
         {
             index--;
         }
 
         if (index == 0)
         {
             // first char (path[0]) == '/' => special case root dir encountered:
             return SEP_CHAR_STR;
         }
         else if (index < 0)
         {
             // no seperator found: this means this is a RELATIVE path with no
             // parent dirname !
             return "";
         }
 
         // index points to character '/'.
 
         return path.substring(0, index);
     }
 
     // =======================================================================
     // Instance
     // =======================================================================
 
     private String scheme;
 
     private String hostname;
 
     private int port;
 
     private String userInfo;
 
     /** 
      * Contains path <em>or</em> scheme specific part without authorization,etc 
      */
     private String pathOrReference;
 
     private String query;
 
     private String fragment;
 
     // === Field guards === //
 
     private boolean hasAuthority = false;
 
     private boolean isReference = false; 
 
     public URIFactory(URI uri)
     {
         init(uri);
     }
 
     protected URIFactory()
     {
        ; // null 
     }
 
     public URIFactory(final String uristr) throws URISyntaxException
     {
         init(uristr);
     }
 
     public URIFactory(String scheme, String schemeSpecificPart)
     {
         init(scheme, null, null, -1, schemeSpecificPart, null, null);
     }
 
    public URIFactory(String scheme, String host, String path)
    {
        init(scheme, null, host, -1, path, null, null);
    }

     public URIFactory(String scheme, String host, int port, String path)
     {
         init(scheme, null, host, port, path, null, null);
     }

 
     public URIFactory(String scheme, String userInfo, String host, int port, String path, String query, String fragment)
     {
         init(scheme, userInfo, host, port, path, query, fragment);
     }
 
     public URIFactory(final URL url) throws URISyntaxException
     {
         init(url.toURI());
     }
 
     public URIFactory(final URIFactory uri)
     {
         this.copyFrom(uri);
     }
 
     /**
      * Initialize by smartly parsing the provided String. Checks whether the
      * string is encoded or decoded.
      * 
      * @param uriStr
      *            - Relative or Absolute URI String. Might be URI encoded.
      * 
      * @throws URISyntaxException
      */
     protected void init(final String uriStr) throws URISyntaxException
     {
         if (uriStr == null)
             throw new URISyntaxException("<NULL>", "URI String can not be null");
 
         URISyntaxException ex;
 
         // ===
         // TODO: other URI parsing class !
         // URI parse bug. Java.net.URI doesn't allow empty scheme specific
         // nor missing authority parts after "://"
         // ===
 
         int index = uriStr.indexOf(':');
 
         String sspStr = null;
 
         if (index >= 0)
             sspStr = uriStr.substring(index + 1, uriStr.length());
 
         if (sspStr != null)
             if (StringUtil.isEmpty(sspStr) || StringUtil.equals(sspStr, "/") || StringUtil.equals(sspStr, "//"))
             {
                 // Parse: "scheme:", "scheme:/" "scheme://".
                 // create scheme only URI
                 this.scheme = uriStr.substring(0, index);
                 this.hasAuthority = false;
                 return;
             }
 
         try
         {
             URI uri = new URI(uriStr);
             // use URI initializer: contains patches for URI parsing ! (better
             // use initializors!)
             init(uri);
             return;
         }
         catch (URISyntaxException e1)
         {
             ex = e1;
             // Be Flexible: could be not encoded string: try to encode it first
             // There is no way to detect whether the string is encoded if it
             // contains a valid '%'.
             try
             {
                 // Try to encode the String.
 
                 URI uri = new URI(encode(uriStr));
                 init(uri); // use URI initializer
                 return;
             }
             catch (URISyntaxException e2)
             {
                 ex = e2;
             }
         }
 
         throw ex;
     }
 
     /**
      * Initialize Factory using provided URI object.
      */
     protected void init(final URI _uri)
     {
         // All URIs are normalized! 
         URI uri = _uri.normalize();
         
         String newScheme = uri.getScheme();
 
         boolean hasAuth = StringUtil.notEmpty(uri.getAuthority());
 
         String newUserInf = uri.getUserInfo();
         
         // Resolve hostname here ?
         String newHost = uri.getHost(); 
 
         int newPort = uri.getPort();
 
         // ===========
         // Hack to get path,Query and Fragment part from SchemeSpecific part if
         // the URI is a reference URI.
         // The java.net.URI doesn't parse "file:relativePath?query" correctly
         // ===========
         String pathOrRef = null;
         String newQuery = null;
         String newFraq = null;
 
         if ((hasAuth == false) && (uri.getRawSchemeSpecificPart().startsWith("/") == false))
         {
             // Parse Reference URI or Relative URI which is not URL compatible:
             // hack to parse query and fragment from non URL compatible
             // URI
             try
             {
                 // check: "ref:<blah>?query#frag"
                 URI tempUri = null;
 
                 tempUri = new URI(newScheme + ":/" + uri.getRawSchemeSpecificPart());
                 String path2 = tempUri.getPath();
                 // get path but skip added '/':
                 // set Path but keep as Reference ! (path is reused for that)
 
                 pathOrRef = path2.substring(1, path2.length());
                 newQuery = tempUri.getQuery();
                 newFraq = tempUri.getFragment();
             }
             catch (URISyntaxException e)
             {
                 // Global.errorPrintStacktrace(e);
                 // Reference URI: ref:<blahblahblah> without starting '/' !
                 // store as is without parsing:
                 pathOrRef = uri.getRawSchemeSpecificPart();
             }
         }
         else
         {
             // plain PATH URI: get Path,Query and Fragment.
             pathOrRef = uri.getPath();
             newQuery = uri.getQuery();
             newFraq = uri.getFragment();
         }
 
         // ================================
         // use normalized initializer
         // ================================
         init(newScheme, newUserInf, newHost, newPort, pathOrRef, newQuery, newFraq);
     }
 
     /**
      * Master Initializer. All fields are given and no exception is thrown.
      * Fields should be decoded. * Path can be relative or absolute but must be
      * normalized with forward slashes.
      */
     private void init(String newscheme, String userinf, String newhost, int newport, String newpath, String newquery,
             String newfrag)
     {
         
         // must be null or uri will add empty values
         if (StringUtil.isEmpty(newhost))
             newhost = null; // null => no hostname
 
         if (StringUtil.isEmpty(userinf))
             userinf = null; // null => no userinfo
 
         if (StringUtil.isEmpty(newquery))
             newquery = null; // null => no userinfo
 
         if (StringUtil.isEmpty(newfrag))
             newfrag = null; // null => no userinfo
 
         // port value of -1 leaves out port in URI !
         // port==0 is use protocol default, SSH => 22, etc.
         if (newport <= 0)
             newport = -1;
 
         // ===
         // AUTHORITY ::= [ <userinfo> '@' ] <hostname> [ ':' <port> ]
         // ===
         this.hasAuthority = StringUtil.notEmpty(userinf) || StringUtil.notEmpty(newhost) || (port > 0);
 
         // ===
         // Feature: Strip ':' after scheme
         // ===
         if ((newscheme != null) && (newscheme.endsWith(":")))
             newscheme = newscheme.substring(0, newscheme.length() - 1);
 
         // ===
         // Relative URI: does NOT have a scheme nor Authority !
         // examples: "dirname/tmp", "#label", "?query#fragment","local.html",
         // ===
         // boolean isRelativeURI=StringUtil.isEmpty(newscheme) &&
         // (hasAuthority==false);
 
         // ===
         // PATH
         // Sanitize path, but keep relative paths or reference paths intact
         // if there is no authority !
         // ====
         newpath = uripath(newpath, hasAuthority);
 
         // store duplicates (or null) !
         this.scheme = StringUtil.duplicate(newscheme);
         this.userInfo = StringUtil.duplicate(userinf);
         this.hostname = StringUtil.duplicate(newhost); // Keep hostname as-is:
                                                        // //
                                                        // resolveHostname(hostname);
         this.port = newport;
         this.pathOrReference = StringUtil.duplicate(newpath);
         this.query = StringUtil.duplicate(newquery);
         this.fragment = StringUtil.duplicate(newfrag);
     }
 
     public URIFactory clone()
     {
         return duplicate();
     }
 
     public URIFactory duplicate()
     {
         URIFactory fac = new URIFactory();
         fac.copyFrom(this);
         return fac;
     }
 
     // ========================================================================
     // Setters/Getters/Changers of actuals fields.
     // ========================================================================
 
     protected void setScheme(String newScheme)
     {
         this.scheme = newScheme;
     }
     
     protected void setHostname(String newHostname)
     {
         this.hostname=newHostname; 
     }
 
     protected void setPath(String newPath)
     {
         setPath(newPath, hasAuthority());
     }
 
     protected void setPort(int newPort)
     {
         this.port = newPort;
     }
 
     protected void setQuery(String newQuery)
     {
         this.query = newQuery;
     }
 
     /** Set new Path and format it to default URI path */
     protected void setPath(String path, boolean makeAbsolute)
     {
         // decode and normalize path
         this.pathOrReference = uripath(path, makeAbsolute);
     }
 
     protected void copyFrom(URIFactory loc)
     {
         // field guards:
         this.hasAuthority = loc.hasAuthority;
         this.isReference = loc.isReference;
 
         // duplicate Strings !
         this.scheme = StringUtil.duplicate(loc.scheme);
         this.userInfo = StringUtil.duplicate(loc.userInfo);
         this.hostname = StringUtil.duplicate(loc.hostname);
         this.port = loc.port;
         this.pathOrReference = StringUtil.duplicate(loc.pathOrReference);
         this.query = StringUtil.duplicate(loc.query);
         this.fragment = StringUtil.duplicate(loc.fragment);
     }
 
     public String getBasename()
     {
         return basename(pathOrReference);
     }
 
     /**
      * returns basename part (last part) of this location.
      * 
      * @param withExtension
      *            whether to keep the extension.
      */
     public String getBasename(boolean withExtension)
     {
         String name = basename(getPath());
 
         if (withExtension == true)
             return name;
         else
             return stripExtension(name);
     }
 
     public String getPath()
     {
         return pathOrReference;
     }
 
     public String getHostname()
     {
         return hostname;
     }
 
     public int getPort()
     {
         return port;
     }
 
     public String getFragment()
     {
         return this.fragment;
     }
 
     /**
      * @return decoded reference component of URI
      */
     public String getReference()
     {
         return pathOrReference;
     }
 
     public boolean isAbsolute()
     {
         return (scheme != null);
     }
 
     public boolean isRelative()
     {
         return (scheme == null);
     }
 
     public boolean hasScheme(String otherScheme)
     {
         return StringUtil.equals(this.scheme, otherScheme);
     }
 
     public String getScheme()
     {
         return this.scheme;
     }
 
     public boolean hasAuthority()
     {
         return this.hasAuthority;
     }
 
     public boolean hasUserInfo()
     {
         return (userInfo != null);
     }
 
     /**
      * Get last part of filename starting from a '.' but ignore fragment and/or
      * query part !
      */
     public String getExtension()
     {
         return extension(getPath());
     }
 
     public String getQuery()
     {
         return this.query;
     }
 
     public String getUserInfo()
     {
         return this.userInfo;
     }
 
     public URIFactory changeUserInfo(String user)
     {
         userInfo = user;
         return this;
     }
 
     /** Returns dirname part of path */
     public String getDirname()
     {
         return dirname(this.pathOrReference);
     }
 
     /**
      * Returns array of path elements. For example "/dev/zero" = {"dev","zero"}
      */
     public String[] getPathElements()
     {
         String path = getPath();
 
         if (path == null)
             return null;
 
         // strip starting '/' to avoid empty path as first path element
         if (path.startsWith(SEP_CHAR_STR))
             path = path.substring(1);
 
         return path.split(SEP_CHAR_STR);
     }
 
     // ========================================================================
     // URIFactory methods, change fields but return this URIFactory in most
     // cases, but might create a new Factory instance.
     // ========================================================================
 
     /** Changes scheme and returns </em>this<em> */
     public URIFactory changeScheme(String newScheme)
     {
         this.setScheme(newScheme);
         return this;
     }
 
     /** Changes port and returns </em>this<em> */
     public URIFactory changePort(int newPort)
     {
         this.setPort(newPort);
         return this;
     }
 
     /** Changes path and returns </em>this<em> */
     public URIFactory changePath(String path)
     {
         this.setPath(path);
         return this;
     }
 
     /** Changes query part and returns </em>this<em> */
     public URIFactory changeQuery(String newQuery)
     {
         this.setQuery(newQuery);
         return this;
     }
 
     public URIFactory changeHostname(String newHostname)
     {
         this.setHostname(newHostname);
         return this;
     }
     
     /**
      * Appends plain string to this URI's String representation.
      *  Does <em>NOT</em> check for fragments or query strings. 
      *  When appending file system paths use explicit appendPath()!
      */
     public URIFactory appendString(String substr) throws URISyntaxException
     {
         return new URIFactory(this.toString() + substr);
     }
 
     /**
      * Creates new location by appending path to this one added seperator char
      * between path elements. To added query and/or fragments string use
      * append().s
      */
     public URIFactory appendPath(String dirname)
     {
         String oldpath = getPath();
         String newpath = null;
 
         if (oldpath == null)
             oldpath = "";
 
         if (StringUtil.isEmpty(dirname))
         {
             return this; // nothing to append
         }
         else if (dirname.charAt(0) == SEP_CHAR)
         {
             newpath = oldpath + dirname;
         }
         else if (dirname.charAt(0) == DOS_SEP_CHAR)
         {
             newpath = oldpath + SEP_CHAR + dirname.substring(1);
         }
         else
         {
             newpath = oldpath + SEP_CHAR + dirname;
         }
 
         // sanitize path:
         setPath(newpath, true);
 
         return this;
     }
 
     /**
      * Resolves optional relative URI using this URI as base location. Note: the
      * last part (basename) of the URI is stripped, assuming the URI starts from
      * a file, for example "http://myhost/index.html", base location =
      * "http://myhost/". Any relative url starts from "http://myhost/" NOT
      * "http://myhost/index.html" Also the supplied string must match URI paths
      * (be %-encoded).
      * 
      * @throws URISyntaxException
      */
     public URIFactory resolveSibling(String reluri) throws URISyntaxException
     {
         if ((reluri == null) || (reluri == ""))
             return this;
 
         // resolve must have ENCODED URI paths...
         URI uri;
 
         // normalize relative uri:
         String encodedPath;
         char c0 = reluri.charAt(0);
         // keep query and fragment uri as-is.
         if ((c0 == '#') || (c0 == '?'))
             encodedPath = reluri;
         else
             encodedPath = encode(uripath(reluri, false));
         uri = toURI().resolve(encodedPath); // *encoded* URI
 
         init(uri);
 
         return this;
     }
 
     /**
      * Resolve DECODED (no %-chars) relative path against this URI, assuming
      * this URI is a (directory) PATH and not a file or url location. The
      * default URI.resolve() method strips the last part (basename) of an URI
      * and uses that as base URI (for example the "index.html" part). Also the
      * relative URIs must be %-coded when they contain spaces !
      * 
      * This method doesn't strip the last part of the URI.
      * 
      * @param relpath
      *            decoded relative path without fragments or queries ('#' or
      *            '?')
      * @return
      * @throws URISyntaxException
      */
     public URIFactory resolvePath(String relpath) throws URISyntaxException
     {
         // add dummy html, and use URI 'resolve' (which expects .html file)
         URIFactory fac = appendPath("dummy.html");
 
         // resolves encoded path!
         fac.resolveSibling(URIFactory.uripath(relpath, false)); 
 
         return fac;
     }
 
     public URIFactory getParent()
     {
         URIFactory fac = duplicate();
         fac.pathOrReference=dirname(this.pathOrReference);
         return fac;
     }
     
     // ========================================================================
     // URI Formatter methods
     // ========================================================================
 
     public URI toURI() throws URISyntaxException
     {
         if (this.hasAuthority())
         {
             return new URI(this.scheme, this.userInfo, this.hostname, this.port, this.getPath(), this.query,
                     this.fragment);
 
         }
         else
         {
             // create Reference URI
             return new URI(this.scheme, null, null, -1, this.getReference(), this.query, this.fragment);
         }
     }
 
     /** 
      * calls toURI().toURL() 
      */ 
     public URL toURL() throws MalformedURLException, URISyntaxException
     {
         return toURI().toURL();
     }
 
     /**
      * Explicit encode URI. 
      */
     public URI toEncodedURI() throws URISyntaxException
     {
         return new URI(scheme, userInfo, hostname, port, encode(pathOrReference), encode(query), encode(fragment));
     }
 
     public String toString()
     {
         return toNormalizedString();
     }
 
     /**
      * This method returns the <em>Decoded</em> URI string. For an URI compatible string
      * (with %XX encoding) use toURI().toString() or toURIString() !
      * 
      * @return normalized and decoded URI String.
      */
     public String toNormalizedString()
     {
         // suport relative VRLs!
         String str = "";
 
         if (this.isAbsolute() == true)
             str += scheme + ":";
 
         if (this.hasAuthority)
         {
             str += SEP_CHAR_STR + SEP_CHAR_STR;
 
             if ((userInfo != null) && (userInfo.compareTo("") != 0))
                 str += userInfo + "@";
 
             if (hostname != null)
             {
                 str += hostname;
                 if (port > 0)
                     str += ":" + port;
             }
         }
 
         // path could start without "/" !
         if (pathOrReference != null)
             str += pathOrReference;
         else
             str += SEP_CHAR; // still end with '/' for consistancy !
 
         if (query != null)
             str += "?" + query;
 
         if (fragment != null)
             str += "#" + fragment;
 
         return str;
     }
 
 }
