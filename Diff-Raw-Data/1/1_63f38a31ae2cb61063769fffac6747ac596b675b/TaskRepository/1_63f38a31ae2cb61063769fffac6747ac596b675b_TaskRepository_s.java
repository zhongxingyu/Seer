 /*******************************************************************************
  * Copyright (c) 2004 - 2007 University Of British Columbia and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     University Of British Columbia - initial API and implementation
  *     IBM Corporation - Bug 177320 Pending changes to internal class UpdateCore will break Tasks/Core
  *******************************************************************************/
 
 package org.eclipse.mylar.tasks.core;
 
 import java.net.MalformedURLException;
 import java.net.Proxy;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.TimeZone;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.mylar.core.MylarStatusHandler;
 import org.eclipse.mylar.core.net.WebClientUtil;
 
 /**
  * Note that task repositories use Strings for storing time stamps because using
  * Date objects led to the following problems:
  * <ul>
  * <li>Often we are unable to get the time zone of the repository so
  * interpreting the date string correctly doesn't work.</li>
  * <li>Even if we do know the time zone information the local clock may be
  * wrong. This can cause lost incoming when asking the repository for all
  * changes since date X.</li>
  * <li>The solution we have come up with thus far is not to interpret the date
  * as a DATE object but rather simply use the date string given to us by the
  * repository itself.</li>
  * </ul>
  * 
  * @author Mik Kersten
  * @author Rob Elves
  * @author Eugene Kuleshov
  */
 public class TaskRepository {
 
 	public static final String DEFAULT_CHARACTER_ENCODING = "UTF-8";
 
 	public static final String AUTH_PASSWORD = "org.eclipse.mylar.tasklist.repositories.password"; //$NON-NLS-1$ 
 
 	public static final String AUTH_USERNAME = "org.eclipse.mylar.tasklist.repositories.username"; //$NON-NLS-1$ 
 
 	public static final String ANONYMOUS_LOGIN = "org.eclipse.mylar.tasklist.repositories.anonymous";
 
 	public static final String AUTH_HTTP_PASSWORD = "org.eclipse.mylar.tasklist.repositories.httpauth.password"; //$NON-NLS-1$ 
 
 	public static final String AUTH_HTTP_USERNAME = "org.eclipse.mylar.tasklist.repositories.httpauth.username"; //$NON-NLS-1$ 
 
 	public static final String NO_VERSION_SPECIFIED = "unknown";
 
 	private static final String AUTH_SCHEME = "Basic";
 
 	private static final String AUTH_REALM = "";
 
 	private static final URL DEFAULT_URL;
 
 	public static final String PROXY_USEDEFAULT = "org.eclipse.mylar.tasklist.repositories.proxy.usedefault";
 
 	public static final String PROXY_HOSTNAME = "org.eclipse.mylar.tasklist.repositories.proxy.hostname";
 
 	public static final String PROXY_PORT = "org.eclipse.mylar.tasklist.repositories.proxy.port";
 
 	public static final String PROXY_USERNAME = "org.eclipse.mylar.tasklist.repositories.proxy.username";
 
 	public static final String PROXY_PASSWORD = "org.eclipse.mylar.tasklist.repositories.proxy.password";
 
 	// HACK: Lock used to work around race condition in
 	// Platform.add/get/flushAuthorizationInfo()
 	private static final Object LOCK = new Object();
 
 	// HACK: private credentials for headless operation
 	private static Map<String, Map<String, String>> credentials = new HashMap<String, Map<String, String>>();
 
 	private boolean isCachedUserName;
 
 	private String cachedUserName;
 
 	static {
 		URL url = null;
 		try {
 			url = new URL("http://eclipse.org/mylar");
 		} catch (Exception ex) {
 			// TODO ?
 		}
 		DEFAULT_URL = url;
 	}
 
 	private Map<String, String> properties = new LinkedHashMap<String, String>();
 
 	/**
 	 * for testing purposes
 	 */
 	public TaskRepository(String kind, String serverUrl) {
 		this(kind, serverUrl, NO_VERSION_SPECIFIED);
 	}
 
 	/**
 	 * for testing purposes sets repository time zone to local default time zone
 	 * sets character encoding to DEFAULT_CHARACTER_ENCODING
 	 */
 	public TaskRepository(String kind, String serverUrl, String version) {
 		this(kind, serverUrl, version, DEFAULT_CHARACTER_ENCODING, TimeZone.getDefault().getID());
 	}
 
 	public TaskRepository(String kind, String serverUrl, String version, String encoding, String timeZoneId) {
 		this.properties.put(IRepositoryConstants.PROPERTY_KIND, kind);
 		this.properties.put(IRepositoryConstants.PROPERTY_URL, serverUrl);
 		this.properties.put(IRepositoryConstants.PROPERTY_VERSION, version);
 		this.properties.put(IRepositoryConstants.PROPERTY_ENCODING, encoding);
 		this.properties.put(IRepositoryConstants.PROPERTY_TIMEZONE, timeZoneId);
 	}
 
 	public TaskRepository(String kind, String serverUrl, Map<String, String> properties) {
 		this.properties.put(IRepositoryConstants.PROPERTY_KIND, kind);
 		this.properties.put(IRepositoryConstants.PROPERTY_URL, serverUrl);
 		this.properties.putAll(properties);
 	}
 
 	public String getUrl() {
 		return properties.get(IRepositoryConstants.PROPERTY_URL);
 	}
 
 	// private String getProxyHostname() {
 	// return properties.get(PROXY_HOSTNAME);
 	// }
 
 	public void setUrl(String newUrl) {
 		properties.put(IRepositoryConstants.PROPERTY_URL, newUrl);
 	}
 
 	public boolean hasCredentials() {
 		String username = getUserName();
 		String password = getPassword();
 		return username != null && username.length() > 0 && password != null && password.length() > 0;
 	}
 
 	/**
 	 * The username is cached since it needs to be retrieved frequently (e.g.
 	 * for Task List decoration).
 	 */
 	public String getUserName() {
 		// NOTE: if anonymous, user name is "" string so we won't go to keyring
 		if (!isCachedUserName) {
 			cachedUserName = getUserNameFromKeyRing();
 		}
 		return cachedUserName;
 	}
 
 	private String getUserNameFromKeyRing() {
 		return getAuthInfo(AUTH_USERNAME);
 	}
 
 	public String getPassword() {
 		return getAuthInfo(AUTH_PASSWORD);
 	}
 
 	public String getProxyUsername() {
 		return getAuthInfo(PROXY_USERNAME);
 	}
 
 	public String getProxyPassword() {
 		return getAuthInfo(PROXY_PASSWORD);
 	}
 
 	public String getHttpUser() {
 		return getAuthInfo(AUTH_HTTP_USERNAME);
 	}
 
 	public String getHttpPassword() {
 		return getAuthInfo(AUTH_HTTP_PASSWORD);
 	}
 
 	public void setAuthenticationCredentials(String username, String password) {
 		setCredentials(username, password, AUTH_USERNAME, AUTH_PASSWORD);
 		cachedUserName = username;
 		isCachedUserName = true;
 	}
 
 	public void setProxyAuthenticationCredentials(String username, String password) {
 		setCredentials(username, password, PROXY_USERNAME, PROXY_PASSWORD);
 	}
 
 	public void setHttpAuthenticationCredentials(String username, String password) {
 		setCredentials(username, password, AUTH_HTTP_USERNAME, AUTH_HTTP_PASSWORD);
 	}
 
 	private void setCredentials(String username, String password, String userProperty, String passwordProperty) {
 		Map<String, String> map = getAuthInfo();
 		if (map == null) {
 			map = new HashMap<String, String>();
 		}
 
 		if (username != null) {
 			map.put(userProperty, username);
 		}
 		if (password != null) {
 			map.put(passwordProperty, password);
 		}
 		addAuthInfo(map);
 	}
 
 	public void flushAuthenticationCredentials() {
 		synchronized (LOCK) {
 			try {
 				if (Platform.isRunning()) {
 					try {
 						Platform.flushAuthorizationInfo(new URL(getUrl()), AUTH_REALM, AUTH_SCHEME);
 					} catch (MalformedURLException ex) {
 						Platform.flushAuthorizationInfo(DEFAULT_URL, getUrl(), AUTH_SCHEME);
 					}
 				} else {
 					Map<String, String> headlessCreds = getAuthInfo();
 					headlessCreds.clear();
 				}
 				isCachedUserName = false;
 			} catch (CoreException e) {
 				MylarStatusHandler.fail(e, "could not flush authorization credentials", true);
 			}
 		}
 	}
 
 	private void addAuthInfo(Map<String, String> map) {
 		synchronized (LOCK) {
 			try {
 				if (Platform.isRunning()) {
 					// write the map to the keyring
 					try {
 						Platform.addAuthorizationInfo(new URL(getUrl()), AUTH_REALM, AUTH_SCHEME, map);
 					} catch (MalformedURLException ex) {
 						Platform.addAuthorizationInfo(DEFAULT_URL, getUrl(), AUTH_SCHEME, map);
 					}
 				} else {
 					Map<String, String> headlessCreds = getAuthInfo();
 					headlessCreds.putAll(map);
 				}
 			} catch (CoreException e) {
 				MylarStatusHandler.fail(e, "Could not set authorization credentials", true);
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private Map<String, String> getAuthInfo() {
 		synchronized (LOCK) {
 			if (Platform.isRunning()) {
 				try {
 					return Platform.getAuthorizationInfo(new URL(getUrl()), AUTH_REALM, AUTH_SCHEME);
 				} catch (MalformedURLException ex) {
 					return Platform.getAuthorizationInfo(DEFAULT_URL, getUrl(), AUTH_SCHEME);
 				} catch (Exception e) {
 					MylarStatusHandler.fail(e, "Could not retrieve authentication credentials", false);
 				}
 			} else {
 				Map<String, String> headlessCreds = credentials.get(getUrl());
 				if (headlessCreds == null) {
 					headlessCreds = new HashMap<String, String>();
 					credentials.put(getUrl(), headlessCreds);
 				}
 				return headlessCreds;
 			}
 			return null;
 		}
 	}
 
 	private String getAuthInfo(String property) {
 		Map<String, String> map = getAuthInfo();
 		return map == null ? null : map.get(property);
 	}
 
 	public void clearCredentials() {
 
 	}
 
 	@Override
 	public boolean equals(Object object) {
 		if (object instanceof TaskRepository && getUrl() != null) {
 			return getUrl().equals(((TaskRepository) object).getUrl());
 		} else {
 			return super.equals(object);
 		}
 	}
 
 	@Override
 	public int hashCode() {
 		if (getUrl() != null) {
 			return getUrl().hashCode();
 		} else {
 			return super.hashCode();
 		}
 	}
 
 	@Override
 	public String toString() {
 		return getUrl();
 	}
 
 	/**
 	 * @return "<unknown>" if kind is unknown
 	 */
 	public String getKind() {
 		String kind = properties.get(IRepositoryConstants.PROPERTY_KIND);
 		if (kind != null) {
 			return kind;
 		} else {
 			return IRepositoryConstants.KIND_UNKNOWN;
 		}
 	}
 
 	public String getVersion() {
 		final String version = properties.get(IRepositoryConstants.PROPERTY_VERSION);
 		return version == null || "".equals(version) ? NO_VERSION_SPECIFIED : version;
 	}
 
 	public void setVersion(String ver) {
 		properties.put(IRepositoryConstants.PROPERTY_VERSION, ver == null ? NO_VERSION_SPECIFIED : ver);
 	}
 
 	public String getCharacterEncoding() {
 		final String encoding = properties.get(IRepositoryConstants.PROPERTY_ENCODING);
 		return encoding == null || "".equals(encoding) ? DEFAULT_CHARACTER_ENCODING : encoding;
 	}
 
 	/**
 	 * for testing purposes
 	 */
 	public void setCharacterEncoding(String characterEncoding) {
 		properties.put(IRepositoryConstants.PROPERTY_ENCODING, characterEncoding == null ? DEFAULT_CHARACTER_ENCODING
 				: characterEncoding);
 	}
 
 	public String getTimeZoneId() {
 		final String timeZoneId = properties.get(IRepositoryConstants.PROPERTY_TIMEZONE);
 		return timeZoneId == null || "".equals(timeZoneId) ? TimeZone.getDefault().getID() : timeZoneId;
 	}
 
 	public void setTimeZoneId(String timeZoneId) {
 		this.properties.put(IRepositoryConstants.PROPERTY_TIMEZONE, timeZoneId == null ? TimeZone.getDefault().getID()
 				: timeZoneId);
 	}
 
 	public String getSyncTimeStamp() {
 		return this.properties.get(IRepositoryConstants.PROPERTY_SYNCTIMESTAMP);
 	}
 
 	/**
 	 * ONLY for use by IRepositoryConstants. To set the sync time call
 	 * IRepositoryConstants.setSyncTime(repository, date);
 	 */
 	public void setSyncTimeStamp(String syncTime) {
 		this.properties.put(IRepositoryConstants.PROPERTY_SYNCTIMESTAMP, syncTime);
 	}
 
 	public void setRepositoryLabel(String repositoryLabel) {
 		this.properties.put(IRepositoryConstants.PROPERTY_LABEL, repositoryLabel);
 	}
 
 	/**
 	 * @return the URL if the label property is not set
 	 */
 	public String getRepositoryLabel() {
 		String label = properties.get(IRepositoryConstants.PROPERTY_LABEL);
 		if (label != null && label.length() > 0) {
 			return label;
 		} else {
 			return getUrl();
 		}
 	}
 
 	public Map<String, String> getProperties() {
 		return new LinkedHashMap<String, String>(this.properties);
 	}
 
 	public String getProperty(String name) {
 		return this.properties.get(name);
 	}
 
 	public void setProperty(String name, String value) {
 		this.properties.put(name, value);
 	}
 
 	public boolean hasProperty(String name) {
 		String value = getProperty(name);
 		return value != null && value.trim().length() > 0;
 	}
 
 	public void removeProperty(String key) {
 		this.properties.remove(key);
 	}
 
 	public Proxy getProxy() {
 		Proxy proxy = Proxy.NO_PROXY;
 		if (useDefaultProxy()) {
 			proxy = WebClientUtil.getPlatformProxy();
 		} else {
 
 			String proxyHost = getProperty(PROXY_HOSTNAME);
 			String proxyPort = getProperty(PROXY_PORT);
 			String proxyUsername = "";
 			String proxyPassword = "";
 			if (proxyHost != null && proxyHost.length() > 0) {
 				proxyUsername = getProxyUsername();
 				proxyPassword = getProxyPassword();
 			}
 			proxy = WebClientUtil.getProxy(proxyHost, proxyPort, proxyUsername, proxyPassword);
 		}
 		return proxy;
 	}
 
 	/**
 	 * Use platform proxy settings
 	 */
 	public boolean useDefaultProxy() {
 		return "true".equals(getProperty(PROXY_USEDEFAULT));
 	}
 
 	public void setAnonymous(boolean b) {
 		properties.put(ANONYMOUS_LOGIN, String.valueOf(b));
 	}
 
 	public boolean isAnonymous() {
 		return getProperty(ANONYMOUS_LOGIN) == null || "true".equals(getProperty(ANONYMOUS_LOGIN));
 	}
 }
