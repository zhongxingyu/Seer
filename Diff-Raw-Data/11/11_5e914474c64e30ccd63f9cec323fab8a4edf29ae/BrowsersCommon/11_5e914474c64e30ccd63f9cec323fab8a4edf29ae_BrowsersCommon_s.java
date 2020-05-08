 package nl.nikhef.browsers;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import nl.nikhef.browsers.exception.BrowserExecutionException;
 import nl.nikhef.browsers.exception.BrowserNotAvailableException;
 import nl.nikhef.jgridstart.osutils.FileUtils;
 import nl.nikhef.jgridstart.osutils.PrivateFileWriter;
 
 /** Platform-agnostic parts of browser discovery and certificate installation.
  * <p>
  * Each platform-specific implementation of IBrowsers probably wants to derive
  * from this class.
  * <p>
  * TODO consistently use configfile id for browserid so it becomes platform-
  * independent. Now each backend wants to use its own naming. 
  * 
  */
 abstract class BrowsersCommon implements IBrowsers {
 
     static protected Logger logger = Logger.getLogger("nl.nikhef.browsers");
     
     /** List of known browsers parsed from {@literal browsers.properties} */
     protected HashMap<String, Properties> knownBrowsers = new HashMap<String, Properties>();
 
     /** List of available browsers.
      * <p>
      * This is used by the default implementation to get browser information
      * from. Do make sure to set this to something sensible in the children's
      * {@link #initialize} methods.
      */
     protected HashMap<String, Properties> availableBrowsers = null;
 
     
     public abstract void initialize() throws IOException;
 
     public abstract String getDefaultBrowser();
 
     public abstract void openUrl(String browserid, String urlString)
     	throws BrowserNotAvailableException, BrowserExecutionException;
     
     
     public Set<String> getBrowserList() {
 	return availableBrowsers.keySet();
     }
 
     public String getBrowserName(String browserid) {
 	String name = null;
 	Properties p = availableBrowsers.get(browserid);
 	if (p!=null) name = p.getProperty("desc");
 	if (name==null) {
 	    p = knownBrowsers.get(browserid);
 	    if (p!=null) name = p.getProperty("desc");
 	}
 	return name;
     }
     
     public Properties getBrowserProperties(String browserid)
     		throws BrowserNotAvailableException {
 
 	// check if browserid is present
 	if (!availableBrowsers.containsKey(browserid))
 	    throw new BrowserNotAvailableException(browserid);
 	
 	// return properties
 	return availableBrowsers.get(browserid);
     }
     
     public void openUrl(String urlString)
     		throws BrowserNotAvailableException, BrowserExecutionException {
 	openUrl(getDefaultBrowser(), urlString);
     }
 
     public void openUrl(List<String> browserids, String urlString)
     		throws BrowserExecutionException, BrowserNotAvailableException {
 	BrowserNotAvailableException firstException = null;
 	for (Iterator<String> it = browserids.iterator(); it.hasNext(); ) {
 	    try {
 		openUrl(it.next(), urlString);
 		return;
 	    } catch(BrowserNotAvailableException e) {
 		if (firstException==null) firstException = e;
 		continue;
 	    }
 	}
 	throw firstException;
     }
 
     
     /** Return the list of known browsers from {@literal browsers.properties}, load when needed */
     protected HashMap<String, Properties> getKnownBrowsers() {
 	if (knownBrowsers==null || knownBrowsers.size()==0) {
 	    try {
 		readKnownBrowsers();
 	    // exception should not occur since it's a class resource
 	    } catch (IOException e) { assert(false); }
 	}
 	return knownBrowsers;
     }
     
     /** Return the list of known browsers from {@literal browsers.properties} */
     public Set<String> getKnownBrowserList() {
 	return getKnownBrowsers().keySet();
     }
 
     /** Parse the file {@literal browsers.properties} */ 
     protected HashMap<String, Properties> readKnownBrowsers() throws IOException {
 	knownBrowsers.clear();
 	// OS detection, translate system OS name to keywords used in property names
 	String curOS = System.getProperty("os.name");
 	if (curOS.startsWith("Win")) curOS = "win";
 	else if (curOS.startsWith("Mac")) curOS = "mac";
 	else curOS = "lnx";
 	// load
 	Properties p = new Properties();
 	p.load(getClass().getResourceAsStream("browsers.properties"));
 	// split into browsers
 	for (Enumeration<?> it = p.propertyNames(); it.hasMoreElements(); ) {
 	    String key = (String)it.nextElement();
 	    String value = p.getProperty(key);
 	    int idx = key.indexOf('.');
 	    String browser = key.substring(0, idx);
 	    key = key.substring(idx+1);
 	    if (!knownBrowsers.containsKey(browser))
 		knownBrowsers.put(browser, new Properties());
 	    // If this is a system-specific property that matches the
 	    // current system, override the default property; otherwise
 	    // ignore operating-system-specific properties.
 	    final Pattern osKey = Pattern.compile("^(.*)\\.(win|mac|lnx)$");
 	    Matcher osMatch = osKey.matcher(key);
 	    if (osMatch.matches()) {
 		if (curOS.equals(osMatch.group(2)) )
		    knownBrowsers.get(browser).setProperty(osMatch.group(1), value);
 	    } else {
		knownBrowsers.get(browser).setProperty(key, value);
 	    }
 	}
 	return knownBrowsers;
     }
     
     /** Return Java's idea of the default browser, or {@code null} if none. */
     protected String findDefaultBrowserJava() {
 	// note that I haven't actually seen anything else than null here ...
 	return System.getProperty("deployment.browser.path");
     }
 
     public void installPKCS12(File pkcs)
     		throws BrowserNotAvailableException, BrowserExecutionException {
 	installPKCS12(getDefaultBrowser(), pkcs);
     }
 
     public void installPKCS12(String browserid, File pkcs)
 	    throws BrowserNotAvailableException, BrowserExecutionException {
 
 	// check if browserid is present
 	if (!availableBrowsers.containsKey(browserid))
 	    throw new BrowserNotAvailableException(browserid);
 
 	// run the installation command
 	String method = availableBrowsers.get(browserid).getProperty("certinst");
 	if ("system".equals(method)) {
 	    installPKCS12System(browserid, pkcs);
 	} else if ("browser".equals(method)) {
 	    openUrl(browserid, pkcs.toURI().toASCIIString());
 	} else if ("mozilla".equals(method)) {
 	    installPKCS12Mozilla(browserid, pkcs);
 	} else {
 	    throw new BrowserExecutionException(browserid, "invalid installation method: "+method);
 	}
     }
     
     /** Install a PKCS#12 file into the system's certificate store */
     abstract protected void installPKCS12System(String browserid, File pkcs) throws BrowserExecutionException;
     
     /** Install a PKCS#12 file into a Mozilla-type certificate store */
     protected void installPKCS12Mozilla(String browserid, File pkcs)
     	throws BrowserNotAvailableException, BrowserExecutionException {
 	File tmpdir = null, pkcsNew = null, htmlNew = null;
 	try {
 	    // create a temporary directory to store pkcs and html page
 	    tmpdir = FileUtils.createTempDir("jgridstart_certinst");
 	    pkcsNew = new File(tmpdir, "import.p12");
 	    FileUtils.CopyFile(pkcs, pkcsNew);
 	    htmlNew = new File(tmpdir, "certinstall_moz.html");
 	    OutputStream htmlWriter = new PrivateFileWriter(htmlNew).getOutputStream();
 	    InputStream htmlReader = getClass().getResourceAsStream(htmlNew.getName());
 	    while(htmlReader.available()>0) {
 		byte[] b = new byte[htmlReader.available()];
 		htmlWriter.write(b, 0, htmlReader.read(b));
 	    }
 	    htmlReader.close();
 	    htmlWriter.close();
 	    // delete files on exit; note that Java deletes in reverse order
 	    tmpdir.deleteOnExit();
 	    pkcsNew.deleteOnExit();
 	    htmlNew.deleteOnExit();
 	    // open page to install
 	    openUrl(browserid, htmlNew.toURI().toASCIIString());
 	} catch (IOException e) {
 	    // make sure the files are cleaned up now
 	    if (pkcsNew!=null) pkcsNew.delete();
 	    if (htmlNew!=null) htmlNew.delete();
 	    if (tmpdir!=null) tmpdir.delete();
 	    // and throw exception
 	    throw new BrowserExecutionException(browserid,  e);
 	} 
     }
 }
