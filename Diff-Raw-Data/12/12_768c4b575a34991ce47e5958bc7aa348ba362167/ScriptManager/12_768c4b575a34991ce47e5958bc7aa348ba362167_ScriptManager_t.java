 package com.popodeus.chat;
 
 import com.gargoylesoftware.htmlunit.*;
 import com.gargoylesoftware.htmlunit.xml.XmlPage;
 import com.gargoylesoftware.htmlunit.html.DomElement;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.gargoylesoftware.htmlunit.html.HtmlElement;
 
 import javax.script.Bindings;
 import javax.script.SimpleBindings;
 import java.util.*;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.io.*;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.text.NumberFormat;
 
 import sun.org.mozilla.javascript.internal.Scriptable;
 import org.jibble.pircbot.User;
 import org.w3c.dom.Element;
 
 /**
  * Loads and runs scripts that act as commands
  * photodeus
  * Sep 2, 2009
  */
 public class ScriptManager {
 	private static Logger log = Logger.getLogger("com.popodeus.chat.ScriptManager");
 
 	public static final String UTF8 = "UTF-8";
 	public static final int DEFAULT_HTTP_TIMEOUT = 8000;
 	private WebClient client;
 
 	private Set<String> ignoredNicks;
 	private Map<String, TriggerScript> scriptcache;
 	private Map<MsLuvaLuva.Event, List<EventScript>> eventscriptcache;
 	private Bindings globalBindings;
 
 	public Scriptable last_result;
 	private BotCallbackAPI bot;
 	private static long DEFAULT_TIMEOUT = 10 * 1000L; // milliseconds
 	private int DEFAULT_IGNORE_TIME = 10 * 60; // 10 hours
 	private File scriptDir;
 	private File variableCacheDir;
 
 	public ScriptManager(final BotCallbackAPI bot, File scriptDir, File scriptVariableDir) {
 		this.bot = bot;
 		this.scriptcache = new HashMap<String, TriggerScript>(64);
 		this.globalBindings = new SimpleBindings();
 		this.ignoredNicks = new HashSet<String>(10);
 		this.scriptDir = scriptDir;
 		this.variableCacheDir = scriptVariableDir;
 		this.client = null;
 		compileEventScripts();
 		loadScriptVars();
 	}
 
 	public void loadScriptVars() {
 		log.entering(getClass().getName(), "loadScriptVars", variableCacheDir);
 		File[] fs = variableCacheDir.listFiles(new FilenameFilter() {
 			public boolean accept(final File file, final String s) {
 				return file.getName().endsWith(".dat");
 			}
 		});
 		if (fs != null && fs.length > 0) {
 			Arrays.sort(fs);
 
 			File f = fs[fs.length-1];
 			if (f.exists()) {
 				log.info("Loading script variables from " + f);
 				ObjectInputStream ois = null;
 				try {
 					ois = new ObjectInputStream(new FileInputStream(f));
 					while (ois.available() > 0) {
 						String key = ois.readUTF();
 						Object value = ois.readObject();
 						log.finest(key + ": " + value);
 						globalBindings.put(key, value);
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				} finally {
 					if (ois != null) {
 						try {
 							ois.close();
 						} catch (IOException e) {
 						}
 					}
 				}
 			}
 		}
 		/*
 		log.info("Loading all script variables");
 		for (File f : cachedir.listFiles()) {
 			try {
 				ScriptableInputStream ois = new ScriptableInputStream(new FileInputStream(f), null);
 				String var = f.getName().replace(".dat", "");
 				Scriptable tmp = (Scriptable) ois.readObject();
 				log.fine(var + " = " + tmp);
 				scriptvars.put(var, tmp);
 				ois.close();
 			} catch (Exception ex) {
 				log.log(Level.SEVERE, "Failed to load variable cache from " + f + ". ", ex);
 			}
 		}
 		*/
 	}
 
 	public void saveScriptVars() {
 		log.entering(getClass().getName(), "saveScriptVars", variableCacheDir);
 		File f = new File(variableCacheDir, "variables-" + System.currentTimeMillis() + ".dat");
 		log.info("Saving script variables into " + f);
 		ObjectOutputStream oos = null;
 		try {
 			oos = new ObjectOutputStream(new FileOutputStream(f));
 			for (Map.Entry<String, Object> e : globalBindings.entrySet()) {
 				String key = e.getKey();
 				Object val = e.getValue();
				try {
					if (val instanceof Serializable) {
						Serializable s = (Serializable) val;
						oos.writeUTF(key);
						oos.writeObject(s);
					}
				} catch (Exception ex) {
					log.info("Unable to serialize: " + key + " => " + val);
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (oos != null) {
 				try {
 					oos.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 		/*
 		log.info("Saving all script variables to disk");
 		if (cachedir != null) {
 			// Clear out old files first...
 			for (File f : cachedir.listFiles()) {
 				f.delete();
 			}
 			// Now start making new ones
 			for (Map.Entry<String, Scriptable> e : scriptvars.entrySet()) {
 				File f = new File(cachedir, e.getKey() + ".dat");
 				try {
 					Scriptable var = e.getValue();
 					Scriptable scope = var.getParentScope();
 					ScriptableOutputStream oos = new ScriptableOutputStream(new FileOutputStream(f), scope);
 					oos.writeObject(var);
 					oos.close();
 				} catch (Exception ex) {
 					log.log(Level.SEVERE, "Failed to save variable cache into " + f + ". ", ex);
 				}
 			}
 		}
 		*/
 	}
 
 	/**
 	 * Recompiles all scripts in in the script directory
 	 * that was designated during construction of this ScriptManager.
 	 */
 	public void compileEventScripts() {
 		compileEventScripts(scriptDir);
 	}
 
 	/**
 	 * (Re)Compiles a set of scripts found in the given directory
 	 * and fills the current script cache with those.
 	 * Giving a different script directory as parameter will not change the default
 	 * directory given during construction of this ScriptManager.
 	 * @param scriptdir
 	 * @see #setScriptDir
 	 */
 	public void compileEventScripts(File scriptdir) {
 		log.info("Compiling event scripts...");
 		eventscriptcache = new HashMap<MsLuvaLuva.Event, List<EventScript>>(MsLuvaLuva.Event.values().length * 2);
 		FilenameFilter ff = new FilenameFilter() {
 			public boolean accept(final File dir, final String name) {
 				return name.endsWith(".js");
 			}
 		};
 		for (MsLuvaLuva.Event evt : MsLuvaLuva.Event.values()) {
 			File dir = new File(scriptdir, evt.name());
 			if (dir.exists()) {
 				File[] scripts = dir.listFiles(ff);
 				Arrays.sort(scripts);
 				List<EventScript> trsc = new ArrayList<EventScript>(scripts.length);
 				for (File script : scripts) {
 					log.finer("Compiling: " + script.toString());
 					BufferedReader reader = null;
 					try {
 						reader = new BufferedReader(new FileReader(script));
 						trsc.add(new EventScript(script.getName(), reader));
 					} catch (Exception e) {
 						log.log(Level.SEVERE, script.getName() + ": " + e);
 					} finally {
 						try {
 							if (reader != null) {
 								reader.close();
 							}
 						} catch (IOException e) {
 						}
 					}
 				}
 				log.fine(evt + ": " + trsc.size() + " scripts");
 				eventscriptcache.put(evt, trsc);
 			}
 		}
 		log.info("Done compiling");
 	}
 
 	public void runOnEventScript(final BotCallbackAPI bot, final MsLuvaLuva.Event event, final String sender, final String login, final String hostname, final String message, final String channel) {
 		log.entering(getClass().getName(), "runOnEventScript", new String[]{
 				sender, message, channel
 		});
 		log.finer(sender + ", " + event);
 		List<EventScript> scripts = eventscriptcache.get(event);
 		if (scripts != null) {
 			for (EventScript eventscript : scripts) {
 				log.finest("evaluating script: " + eventscript.getName());
 				if (eventscript.runScript(getAPI(eventscript), sender,
 						login, hostname, message,
 						channel, eventscript.getName(), message)) {
 					log.finer(eventscript.getName() + ": early cancel. No processing of other scripts");
 					break;
 				}
 			}
 		} else {
 			log.finest("No event scripts for " + event);
 		}
 	}
 
 	public File getScriptDir() {
 		return scriptDir;
 	}
 
 	/**
 	 * Sets the default directory from where to load scripts
 	 * @param scriptDir
 	 */
 	public void setScriptDir(final File scriptDir) {
 		this.scriptDir = scriptDir;
 	}
 
 	/*
 	public TriggerScript getTriggerScript(final String cmd) {
 		return scriptcache.get(cmd);
 	}
 	*/
 
 	public TriggerScript getTriggerScript(final String cmd) {
 		TriggerScript triggerScript;
 		if (scriptcache.containsKey(cmd)) {
 			log.finest("Script has compiled cache for " + cmd);
 			triggerScript = scriptcache.get(cmd);
 		} else {
 			triggerScript = compileTriggerScript(cmd);
 			scriptcache.put(cmd, triggerScript);
 		}
 		return triggerScript;
 	}
 	public void clearTriggerSciptCache() {
 		scriptcache.clear();
 	}
 
 	protected boolean runTriggerScript(final BotCallbackAPI bot,
 									   final String sender,
 									   final String login, final String hostname,
 									   final String message,
 									   final String channel,
 									   final String cmd,
 									   final String param) {
 		TriggerScript triggerScript = getTriggerScript(cmd);
 		log.fine("triggerScript: " + triggerScript);
 
 		if (triggerScript != null) {
 			log.finer("triggerScript.hasTimeoutPassed: " + triggerScript.hasTimeoutPassed());
 			if (!triggerScript.hasTimeoutPassed()) {
 				String timeoutmsg = "Too fast - Timeout is " + (triggerScript.getTimeout() / 1000) + "s. Try again later.";
 				bot.notice(sender, timeoutmsg);
 			} else {
 				return triggerScript.runScript(getAPI(triggerScript), sender, login, hostname, message,
 						channel, cmd, param);
 			}
 			/*
 			Scriptable response = (Scriptable) bindings.get(SCRIPTVAR_RESPONSE);
 			ScriptableObject response_to = (ScriptableObject) bindings.get(SCRIPTVAR_RESPONSE_TO);
 			if (null != response && null != response_to) {
 				API.say(response_to.toString(), response.toString());
 			}
 			if (bindings.get(SCRIPTVAR_NO_TIMEOUT) != null) {
 				return false;
 			}
 			*/
 		} else {
 			bot.notice(sender, "Unknown command: " + cmd);
 			log.fine(sender + "@" + hostname + " tried to invoke unknown command: " + cmd);
 		}
 		return false;
 	}
 
 	protected TriggerScript compileTriggerScript(final String cmd) {
 		TriggerScript retval = null;
 		File script = new File(scriptDir, cmd + ".js");
 		log.finest("Locating script file " + script);
 		if (script.exists()) {
 			BufferedReader reader = null;
 			try {
 				reader = new BufferedReader( new FileReader(script) );
 				log.finer("Compiling " + script);
 				retval = new TriggerScript(cmd, reader);
 			} catch (Exception e) {
 				log.log(Level.SEVERE, e.getMessage(), e);
 			} catch (Error e) {
 				log.log(Level.SEVERE, e.getLocalizedMessage(), e);
 			} finally {
 				if (reader != null) {
 					try {
 						reader.close();
 					} catch (IOException e) { /* no op */ }
 				}
 			}
 		} else {
 			log.finer(script + " not found");
 		}
 		return retval;
 	}
 
 	public String normalize(final String nick) {
 		return nick.replace("_", "").replaceAll("[&~%@+]", "").toLowerCase();
 	}
 
 	public static final BrowserVersion FIREFOX =
 			new BrowserVersion(
 					"Mozilla",
 					"5.0",
 					"Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2",
 					"1.8",
 					3.5f);
 
 	public ScriptAPI getAPI(final ScriptBase script) {
 		return new ScriptAPI() {
 			public boolean addIgnore(final String nick, final String ident, final String host) {
 				return addIgnore(nick, ident, host, DEFAULT_IGNORE_TIME);
 			}
 
 			public boolean addIgnore(final String nick, final String ident, final String host, final int minutes) {
 				if (nick != null) {
 					ignoredNicks.add(normalize(nick));
 				}
 				if (host != null) {
 					ignoredNicks.add(host);
 				}
 				return true;
 			}
 
 			public boolean removeIgnore(final String nick, final String ident, final String host) {
 				if (nick != null) {
 					ignoredNicks.remove(normalize(nick));
 				}
 				if (host != null) {
 					ignoredNicks.remove(host);
 				}
 				return true;
 			}
 
 			public boolean isIgnored(final String nick, final String ident, final String host) {
 				return ignoredNicks.contains(normalize(nick))
 						|| ignoredNicks.contains(host);
 			}
 
 			public String[] listIgnores() {
 				return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
 			}
 
 			public User[] getUsers(final String channel) {
 				return bot.getUsers(channel);
 			}
 
 			public Page getPage(final String url) {
 				return getPage(url, DEFAULT_HTTP_TIMEOUT);
 			}
 
 			public String getPageAsText(final String url) {
 				log.finer("getPageAsText: " + url);
 				Page page = getPage(url);
 				if (page != null) {
 					String x = page.getWebResponse().getContentAsString();
 					log.finest(x);
 					return x;
 				}
 				return "";
 			}
 
 			public synchronized Page getPage(final String url, final int timeout) {
 				log.fine("getPage: " + url);
 				try {
 					Page retval;
 					if (client == null) {
 						log.finest("new WebClient");
 						client = new WebClient(FIREFOX);
 						client.addRequestHeader("accept-charset", "UTF-8");
 						client.addRequestHeader("accept-language", "en-US");
 						client.setJavaScriptEnabled(false);
 						client.setTimeout(timeout);
 						CookieManager cookiemanager = new CookieManager();
 						client.setCookieManager(cookiemanager);
 					}
 					//cookiemanager.clearCookies();
 					log.finest("new WebRequestSettings");
 					WebRequestSettings settings = new WebRequestSettings(new URL(url));
 					settings.setCharset("UTF-8");
 					retval = client.getPage(settings);
 					log.finer(url + " => " + retval);
 					log.finer("retval: " + retval);
 					return retval;
 				} catch (Exception e) {
 					log.log(Level.INFO, "...exiting", e);
 					//log.log(Level.SEVERE, "...exiting", e);
 					return null;
 				}
 			}
 
 			/**
 			 * Returns the DomElement given by the XPath statement or null if nothing found
 			 * @param page Either an XmlPage or HtmlPage as returned by {@link #getPage(String)}
 			 * @param xpath XPath expression
 			 * @see XmlPage
 			 * @see HtmlPage
 			 * @see #getAsText(com.gargoylesoftware.htmlunit.Page, String)
 			 */
 			public DomElement getByXpath(final Page page, final String xpath) {
 				if (page == null) {
 					return null;
 				}
 				if (page instanceof HtmlPage) {
 					HtmlPage html = (HtmlPage) page;
 					return html.getFirstByXPath(xpath);
 				}
 				if (page instanceof XmlPage) {
 					XmlPage xml = (XmlPage) page;
 					return xml.getFirstByXPath(xpath);
 				}
 				return null;
 			}
 
 			public String encode(final String param) {
 				try {
 					return URLEncoder.encode(param, UTF8);
 				} catch (UnsupportedEncodingException e) {
 					return param;
 				}
 			}
 
 			public Element getElementById(final Page page, final String id) {
 				if (page instanceof HtmlPage) {
 					HtmlPage html = (HtmlPage) page;
 					return html.getElementById(id);
 				}
 				if (page instanceof XmlPage) {
 					XmlPage xml = (XmlPage) page;
 					return xml.getElementById(id);
 				}
 				return null;
 			}
 
 			public List getElementsByAttribute(final Element parent, final String tagname, final String attribute, final String value) {
 				if (parent instanceof HtmlElement) {
 					return ((HtmlElement)parent).getElementsByAttribute(tagname, attribute, value);
 				}
 				if (parent instanceof DomElement) {
 					return ((DomElement)parent).getElementsByTagName(tagname);
 				}
 				return new ArrayList(0);
 			}
 
 			/**
 			 * Either returns the asText() value of a DomElement or empty zero length string if
 			 * nothing was found.
 			 * @param page Either an XmlPage or HtmlPage as returned by {@link #getPage(String)}
 			 * @param xpath XPath expression
 			 * @see XmlPage
 			 * @see HtmlPage
 			 */
 			public String getAsText(final Page page, final String xpath) {
 				DomElement elm = getByXpath(page, xpath);
 				if (elm != null) {
 					return elm.asText();
 				}
 				return "";
 			}
 
 			public int getTimeout(final String script) {
 				ScriptBase csc = scriptcache.get(script);
 				if (csc != null) {
 					return csc.getTimeout();
 				}
 				return -1;
 			}
 
 			public void setTimeout(final String script, final int timeout) {
 				if (script == null || "*".equals(script)) {
 					for (ScriptBase csc : scriptcache.values()) {
 						csc.setTimeout(timeout);
 					}
 				} else {
 					ScriptBase csc = scriptcache.get(script);
 					if (csc != null) {
 						csc.setTimeout(timeout);
 					}
 				}
 			}
 
 			public void setValue(String key, Object value) {
 				log.finest("setValue: " + key + " => " + value);
 				globalBindings.put(key, value);
 			}
 
 			public Object removeValue(String key) {
 				Object removed = globalBindings.remove(key);
 				log.finest("removeValue: " + key + " => " + removed);
 				return removed;
 			}
 
 			public Object getValue(String key) {
 				return globalBindings.get(key);
 			}
 
 			public void say(final String target, final String line) {
 				log.fine("Say: " + target + ": " + line);
 				bot.say(target, line);
 			}
 
 			public void action(final String target, final String line) {
 				log.fine("Act: " + target + ": " + line);
 				bot.act(target, line);
 			}
 
 			public void notice(final String notice, final String line) {
 				log.fine("Notice: " + notice + ": " + line);
 				bot.notice(notice, line);
 			}
 
 			public void reinit() {
 				log.info("Reinitialize");
 				bot.reinitialize();
 			}
 
 			public void email(final String recipient, final String topic, final String htmlmessage) {
 				log.info("email => " + recipient);
 				// TODO email function
 			}
 
 			public long getStartupTime() {
 				return bot.getStartupTime();
 			}
 
 			public long getConnectTime() {
 				return bot.getConnectTime();
 			}
 
 			public void info(final String message) {
 				log.info(message);
 			}
 
 			public void debug(final String message) {
 				log.finer(message);
 			}
 
 			public long getLastRun() {
 				return script.getLastRun();
 			}
 
 			public String getBotNick() {
 				return bot.getNick();
 			}
 
 			public boolean hasVoice(final String channel, final String nick) {
 				return bot.hasVoice(channel, nick);
 			}
 
 			public boolean isHalfOp(final String channel, final String nick) {
 				return bot.isHalfOp(channel, nick);
 			}
 
 			public boolean isOp(final String channel, final String nick) {
 				return bot.isOp(channel, nick);
 			}
 
 			public boolean isAdmin(final String channel, final String nick) {
 				return bot.isAdmin(channel, nick);
 			}
 
 			public boolean isOwner(final String channel, final String nick) {
 				return bot.isOwner(channel, nick);
 			}
 
 			public void clearScriptCacheFor(final String cmd) {
 				log.finer("Clearing cache for " + cmd);
 				scriptcache.remove(cmd);
 			}
 			public void clearScriptCache() {
 				log.finer("Clearing all script caches");
 				scriptcache.clear();
 				compileEventScripts(scriptDir);
 			}
 
 			public void reloadLoggingConfig() {
 				log.info("Reloading logging configuration");
 				try {
 					LogManager.getLogManager().readConfiguration();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 
 			public String getGreeting() {
 				return bot.getGreeting();
 			}
 
 			public String formatNum(final double num) {
 				return NumberFormat.getNumberInstance().format(num);
 			}
 
 			public String secondsAsPassedTime(final int seconds) {
 				// Untested
 				if (seconds < 0) {
 					return String.valueOf(seconds);
 				}
 				if (seconds < 60) {
 					return seconds + " seconds ago";
 				}
 				if (seconds < 60*60) {
 					return Math.floor(seconds/60) + " minutes ago";
 				}
 				if (seconds < 24*60*60) {
 					int h = seconds / (60 * 60);
 					int min = (seconds - (h * 60)) / 60;
 					return h + " hours and " + min + " minutes ago";
 				}
 				int d = seconds / (24 * 60 * 60);
 				int h = (seconds - d * 24 * 60 * 60) / (60 * 60);
 				int m = seconds % 60;
 				return h + " days and " + h + " hours ago";
 			}
 
 			public void flushScriptVars() {
 				saveScriptVars();
 			}
 
 			/**
 			 * Shuts down the bot.
 			 * TODO privilege check
 			 */
 			public boolean byebye() {
 				log.info("Shutting down bot...");
 				//if (hasPrivileges(nick, ident, host))
 				return bot.byebye();
 			}
 		};
 	}
 }
