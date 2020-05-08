 /*******************************************************************************
  * Copyright (c) 2010 Red Hat Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Inc. - initial API and implementation
  *******************************************************************************/
 package org.fedoraproject.eclipse.packager.koji;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.GeneralSecurityException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.apache.xmlrpc.client.XmlRpcClient;
 import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.fedoraproject.eclipse.packager.SSLUtils;
 import org.fedoraproject.eclipse.packager.koji.preferences.PreferencesConstants;
 
 // TODO Catch all Exceptions and return a unified error message
 public class KojiHubClient implements IKojiHubClient {
 	
 	public static String kojiHubUrl;	// set by setKojiHost
 	public static String kojiWebUrl;	// set by setKojiHost
 	private XmlRpcClientConfigImpl config;
 	private XmlRpcClient client;
 
 	static {
 		// set koji hostname from preference
 		IPreferenceStore kojiPrefStore = KojiPlugin.getDefault().getPreferenceStore();
 		initKojiUrls(kojiPrefStore.getString(PreferencesConstants.PREF_KOJI_HOST));
 	}
 	
 	public KojiHubClient() throws GeneralSecurityException, IOException {
 		// init SSL connection
 		SSLUtils.initSSLConnection();
 
 		config = new XmlRpcClientConfigImpl();
 		try {
 			config.setServerURL(new URL(KojiHubClient.kojiHubUrl));
 			config.setEnabledForExtensions(true);
 			config.setConnectionTimeout(30000);
 			client = new XmlRpcClient();
 			client.setTypeFactory(new KojiTypeFactory(client));
 			client.setConfig(config);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Sets Koji host according to preference and adjusts kojiHubUrl and kojiWebUrl
 	 * 
 	 * @param kojiHost
 	 */
 	private static void initKojiUrls(String kojiHost) {
		KojiHubClient.kojiHubUrl = "https://" + kojiHost+"/kojihub";
 		KojiHubClient.kojiWebUrl = "http://" + kojiHost;
 	}
 	
 	private void setSession(String sessionKey, String sessionID)
 			throws MalformedURLException {
 		config.setServerURL(new URL(KojiHubClient.kojiHubUrl + "?session-key=" + sessionKey //$NON-NLS-1$
 				+ "&session-id=" + sessionID)); //$NON-NLS-1$
 	}
 
 	private void discardSession() throws MalformedURLException {
 		config.setServerURL(new URL(KojiHubClient.kojiHubUrl));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.fedoraproject.eclipse.packager.IKojiHubClient#sslLogin()
 	 */
 	@Override
 	public String sslLogin() throws XmlRpcException, MalformedURLException {
 		ArrayList<String> params = new ArrayList<String>();
 		Object result = client.execute("sslLogin", params); //$NON-NLS-1$
 		HashMap<?, ?> hashMap = (HashMap<?, ?>) result;
 		String sessionKey = hashMap.get("session-key").toString(); //$NON-NLS-1$
 		String sessionID = hashMap.get("session-id").toString(); //$NON-NLS-1$
 		setSession(sessionKey, sessionID);
 		return result.toString();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.fedoraproject.eclipse.packager.IKojiHubClient#logout()
 	 */
 	@Override
 	public void logout() throws MalformedURLException, XmlRpcException {
 		ArrayList<String> params = new ArrayList<String>();
 		client.execute("logout", params); //$NON-NLS-1$
 		discardSession();
 	}
 
 	public String showSession() throws XmlRpcException {
 		ArrayList<String> params = new ArrayList<String>();
 		Object result = client.execute("showSession", params); //$NON-NLS-1$
 		return result.toString();
 	}
 
 	public String getLoggedInUser() throws XmlRpcException {
 		ArrayList<String> params = new ArrayList<String>();
 		Object result = client.execute("getLoggedInUser", params); //$NON-NLS-1$
 		return result.toString();
 	}
 
 	public String listUsers() throws XmlRpcException {
 		ArrayList<String> params = new ArrayList<String>();
 		Object result = client.execute("listUsers", params); //$NON-NLS-1$
 		return Arrays.asList((Object[]) result).toString();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.fedoraproject.eclipse.packager.IKojiHubClient#build(java.lang.String,
 	 * java.lang.String)
 	 */
 	@Override
 	public String build(String target, String scmURL, boolean scratch) throws XmlRpcException {
 		ArrayList<Object> params = new ArrayList<Object>();
 		params.add(scmURL);
 		params.add(target);
 		if (scratch) {
 			Map<String, Boolean> scratchParam = new HashMap<String, Boolean>();
 			scratchParam.put("scratch", true);
 			params.add(scratchParam);
 		}
 		Object result = client.execute("build", params); //$NON-NLS-1$
 		return result.toString();
 	}
 
 	public String hello() throws XmlRpcException {
 		ArrayList<String> params = new ArrayList<String>();
 		Object result = client.execute("hello", params); //$NON-NLS-1$
 		return (String) result;
 	}
 
 	public List<Object> getBuildTargets() throws XmlRpcException {
 		Object result = client.execute("getBuildTargets", new Object[0]); //$NON-NLS-1$
 		return Arrays.asList((Object[]) result);
 	}
 
 }
