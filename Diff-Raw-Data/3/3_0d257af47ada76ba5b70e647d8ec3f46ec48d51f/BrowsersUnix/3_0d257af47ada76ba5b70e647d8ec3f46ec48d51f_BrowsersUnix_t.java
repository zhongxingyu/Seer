 package nl.nikhef.jgridstart.install;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.regex.Pattern;
 
 import nl.nikhef.jgridstart.install.exception.BrowserExecutionException;
 import nl.nikhef.jgridstart.install.exception.BrowserNotAvailableException;
 import nl.nikhef.jgridstart.util.FileUtils;
 
 /** Unix/Linux/BSD/... implementation of browser discovery and launch. */
 class BrowsersUnix extends BrowsersCommon {
     
     private String defaultBrowser = null;
     
     @Override @SuppressWarnings("unchecked") // for clone() cast
     public void initialize() throws IOException {
 	boolean defaultBrowserFound = false;
 	
 	// find default browser
 	String defaultBrowserExe = null;
 	String defaultBrowserPath = findDefaultBrowserEnvironment();
 	if (defaultBrowserPath==null)
 	    defaultBrowserPath = findDefaultBrowserDesktop();
 	if (defaultBrowserPath==null)
 	    defaultBrowserPath = findDefaultBrowserJava();
 	// we only want the basename
 	if (defaultBrowserPath!=null)
 	    defaultBrowserExe = new File(defaultBrowserPath).getName();
 
 	// find known browsers, keep only which are in path
 	availableBrowsers = (HashMap<String, Properties>)readKnownBrowsers().clone();
 	for (Iterator<Entry<String, Properties>> it = availableBrowsers.entrySet().iterator(); it.hasNext(); ) {
 	    Entry<String, Properties> entry = it.next();
 	    Properties p = entry.getValue();
 	    // we need an exe property
 	    if (p.getProperty("exe")!=null) {
 		// first make sure default browser is in known browsers as well
 		// important when the default browser is not in PATH
 		if (p.getProperty("exe").equals(defaultBrowserExe)) {
 		    // set to full path so it can be found
 		    p.setProperty("exe", defaultBrowserPath);
 		    defaultBrowserFound = true;
 		    defaultBrowser = entry.getKey();
 		    continue;
 		}
 		// find using which; process spawning in unix is cheap
 		try {
 		    String[] cmd = new String[] { "which", p.getProperty("exe") };
 		    int ret = Runtime.getRuntime().exec(cmd).waitFor();
 		    if (ret==0) continue;
 		} catch (InterruptedException e) { }
 	    }
 	    // error or not found, remove from list
 	    it.remove();
 	}
 	
 	// add default browser as entry if not found
 	if (!defaultBrowserFound) {
 	    // dummy entry
 	    Properties p = new Properties();
 	    p.setProperty("desc", defaultBrowserExe);
 	    p.setProperty("exe", defaultBrowserPath);
 	    p.setProperty("certinst", "manual");
 	    defaultBrowser = defaultBrowserExe;
 	    availableBrowsers.put(defaultBrowser, p);
 	}
 	
     }
     
     /** Get default browser from environment $BROWSER, or null if unset. */
     private String findDefaultBrowserEnvironment() {
 	String browser = System.getenv("BROWSER");
 	if (browser==null) return null;
 	return normaliseBrowserPath(browser);
     }
     
     /** Normalise the browser path.
      * <p>
      * Currently this follows the symlink if its name is x-www-browser, so
      * that we find the actual browser pointed to by Debian's alternatives.
      */
     private String normaliseBrowserPath(String path) {
 	if (path.equals("x-www-browser") || path.endsWith("/x-www-browser")) {
 	    try {
 		String link1;
 		link1 = readLink(path);
 		if (link1==null) return path;
 		String link2 = readLink(link1);
 		if (link2==null) return link1;
 	    } catch(IOException e) {
 		return path;
 	    }
 	}
 	return path;
     }
     /** Unix readlink command, returns null it path was no symlink. 
      * @throws IOException */
     private String readLink(String path) throws IOException {
 	String output = "";
 	if (FileUtils.Exec(new String[] { "readlink", path }, output)!=0)
 	    return null;
 	return output.trim();
     }
     
     /** Get default browser from desktop environment */
     private String findDefaultBrowserDesktop() {
 	String browser = null;
 
 	if (System.getenv("GNOME_DESKTOP_SESSION_ID")!=null) {
 	    // get from gconf
 	    browser = findDefaultBrowserGConf();
 	    
 	} else if (System.getenv("KDE_FULL_SESSION")!=null) {
 	    // get from kde settings
 	    browser = findDefaultBrowserKDE("kde4");
 	    if (browser==null)
 		browser = findDefaultBrowserKDE("kde");
 	    
 	} else {
 	    // otherwise try all
 	    browser = findDefaultBrowserGConf();
 	    if (browser==null)
 		browser = findDefaultBrowserKDE("kde4");
 	    if (browser==null)
 		browser = findDefaultBrowserKDE("kde");
 	}
 	
 	return browser;
     }
     
     /** Get default browser from GConf setting */
     private String findDefaultBrowserGConf() {
 	// make sure it is enabled
 	if (!Boolean.valueOf(getGConfValue("/desktop/gnome/url-handlers/https/enabled")))
 	    return null;
 	// get value
 	// TODO keep command-line arguments ... !
 	String cmd = getGConfValue("/desktop/gnome/url-handlers/https/command");
 	if (cmd==null) return null;
 	return cmd.split("\\s", 2)[0];
     }
     /** Returns the value of a GConf setting, or null if not available. */
     private String getGConfValue(String key) {
 	StringBuffer output = new StringBuffer();
 	try {
 	    String[] cmd = new String[] { "gconftool-2", "-g", key };
 	    if (FileUtils.Exec(cmd, null, output)!=0)
 	    	return null;
 	} catch(IOException e) {
 	    return null;
 	}
	// still zero return value when key doesn't exist ... sigh
	if (output.toString().trim().equals("No value set for `"+key+"'"))
	    return null;
 	return output.toString().trim();
     }
     
     /** Get default browser from KDE setting.
      * <p>
      * The parameter specifies which kde version is looked for; they
      * have different dot-directories.
      * 
      * @param versionid either "kde" or "kde4"
      */
     private String findDefaultBrowserKDE(String versionid) {
 	// http://docs.kde.org/development/en/kdebase-runtime/userguide/configuration-files.html
 	String cfg = System.getenv("HOME") + "/." + versionid + "share/config/kdeglobals";
 	try {
 	    BufferedReader read = new BufferedReader(new FileReader(cfg));
 	    String line;
 	    boolean inGeneralSection = false;
 	    while ( (line = read.readLine()) != null ) {
 		// keep track of current section in ini file
 		if (line.trim().toLowerCase().startsWith("[general]"))
 		    inGeneralSection = true;
 		else if (line.trim().startsWith("["))
 		    inGeneralSection = false;
 		// catch default browser in general section
 		if (inGeneralSection && line.trim().toLowerCase().startsWith("BrowserApplication")) {
 		    final Pattern pat = Pattern.compile("^\\s*BrowserApplication(\\[.*?\\])?\\s*=\\s*(.*?)\\s*$");
 		    String browser = pat.matcher(line).group(2);
 		    // empty means default browser = get from mime-type
 		    // TODO implement mime-type parsing ... but let's wait for users to actually complain before investing more time
 		    if (browser == "") return "konqueror";
 		    // custom entry begins with exlamaction mark; not sure if this is always the case
 		    if (browser.startsWith("!")) browser = browser.substring(1);
 		    return browser;
 		}
 	    }
 	} catch (IOException e) { }
 	// not found
 	return null;
     }
     
 
     @Override
     public String getDefaultBrowser() {
 	return defaultBrowser;
     }
 
     @Override
     public void openUrl(String browserid, String urlString)
 	    throws BrowserNotAvailableException, BrowserExecutionException {
 	
 	// check if browserid is present
 	if (!availableBrowsers.containsKey(browserid))
 	    throw new BrowserNotAvailableException(browserid);
 	
 	// run the command
 	String[] cmd = new String[] {
 		availableBrowsers.get(browserid).getProperty("exe"),
 		urlString
 	};
 	try {
 	    // execute browser
 	    //   don't wait for this, since starting a new browser when the
 	    //   process isn't running yet can take a loooong time
 	    Runtime.getRuntime().exec(cmd);
 	    /*
 	    StringBuffer output = new StringBuffer();
 	    if ( FileUtils.Exec(cmd, null, output) != 0)
 		throw new BrowserExecutionException(browserid, output.toString());
 	    */
 	} catch (IOException e) {
 	    throw new BrowserExecutionException(browserid, e);
 	}
     }
 
     @Override
     protected void installPKCS12System(String browserid, File pkcs) throws BrowserExecutionException {
 	throw new BrowserExecutionException(browserid,
 		"There is no default certificate store on Unix/Linux,\n" +
 		"please install the certificate manually in your browser.");
     }
     
     @Override
     protected HashMap<String, Properties> readKnownBrowsers() throws IOException {
 	knownBrowsers = super.readKnownBrowsers();
 	// filter browsers with exe only
 	for (Iterator<Properties> it = knownBrowsers.values().iterator(); it.hasNext(); ) {
 	    Properties p = it.next();
 	    if (p.getProperty("exe")==null) it.remove();
 	}
 	return knownBrowsers;
     }
 }
