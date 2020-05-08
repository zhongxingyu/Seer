 // The Grinder
 // Copyright (C) 2001  Paco Gomez
 // Copyright (C) 2001  Philip Aston
 
 // This program is free software; you can redistribute it and/or
 // modify it under the terms of the GNU General Public License
 // as published by the Free Software Foundation; either version 2
 // of the License, or (at your option) any later version.
 
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 
 package net.grinder.plugin.http;
 
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.lang.reflect.Method;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.grinder.plugininterface.PluginThreadContext;
 import net.grinder.plugininterface.PluginException;
 import net.grinder.plugininterface.SimplePluginBase;
 import net.grinder.plugininterface.Test;
 import net.grinder.plugininterface.ThreadCallbacks;
 import net.grinder.util.FilenameFactory;
 import net.grinder.util.GrinderException;
 import net.grinder.util.GrinderProperties;
 import w3c.tools.codec.Base64Encoder;
 
 
 /**
  * Simple HTTP client benchmark.
  * 
  * @author Paco Gomez
  * @author Philip Aston
  * @version $Revision$
  */
 public class HttpPlugin extends SimplePluginBase
 {
     private PluginThreadContext m_pluginThreadContext = null;
     private FilenameFactory m_filenameFactory = null;
     private Map m_callData = new HashMap();
     private boolean m_logHTML = true;
     private HttpMsg m_httpMsg = null;
     private int m_currentIteration = 0; // How many times we've done all the URL's
     private final DateFormat m_dateFormat =
 	new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss zzz");
 
     private Object m_bean = null;
     private StringBean m_stringBean = null;
     private Map m_beanMethodMap = null;
 
     /**
      * Inner class that holds the data for a call.
      */
     protected class CallData implements HttpRequestData
     {
 	private String m_urlString;
 	private String m_okString;
 	private long m_ifModifiedSince = -1;
 	private String m_postString;
	private final String m_basicAuthenticationUserString;
	private final String m_basicAuthenticationPasswordString;
 	private final StringBuffer m_buffer = new StringBuffer();
 	private final Test m_test;
 	private Object[] m_noArgs = new Object[0];
 
 	public CallData(Test test) throws PluginException
 	{
 	    m_test = test;
 	    
 	    final GrinderProperties testParameters = m_test.getParameters();
 
 	    try {
 		m_urlString = testParameters.getMandatoryProperty("url");
 	    }
 	    catch (GrinderException e) {
 		throw new PluginException("URL for Test " +
 					  m_test.getTestNumber() +
 					  " not specified", e);
 	    }
 
 	    m_okString = testParameters.getProperty("ok", null);
 
 	    final String ifModifiedSinceString =
 		testParameters.getProperty("ifModifiedSince", null);
 
 	    if (ifModifiedSinceString != null) {
 		try {
 		    final Date date =
 			m_dateFormat.parse(ifModifiedSinceString);
 	
 		    m_ifModifiedSince = date.getTime();
 		}
 		catch (ParseException e) {
 		    m_pluginThreadContext.logError(
 			"Couldn't parse ifModifiedSince date '" +
 			ifModifiedSinceString + "'");
 		}
 	    }
 
 	    final String postFilename =
 		testParameters.getProperty("post", null);
 
 	    if (postFilename != null) {
 		try {
 		    final FileReader in = new FileReader(postFilename);
 		    final StringWriter writer = new StringWriter(512);
 		    
 		    char[] buffer = new char[4096];
 		    int charsRead = 0;
 
 		    while ((charsRead = in.read(buffer, 0, buffer.length)) > 0)
 		    {
 			writer.write(buffer, 0, charsRead);
 		    }
 		
 		    in.close();
 		    writer.close();
 		    m_postString = writer.toString();
 		}
 		catch (IOException e) {
 		    m_pluginThreadContext.logError(
 			"Could not read post data from " + postFilename);
 
 		    e.printStackTrace(System.err);
 		}
 	    }
 
	    m_basicAuthenticationUserString =
		testParameters.getProperty("basicAuthenticationUser", null);
 
	    m_basicAuthenticationPasswordString =
		testParameters.getProperty("basicAuthenticationPassword",
					   null);
 	}
 
 	private String replaceDynamicKeys(String original) 
 	    throws PluginException
 	{
 	    if (original == null || m_bean == null) {
 		return original;
 	    }
 	    else {
 		// CallData's belong to a HttpPlugin, and there's a
 		// plugin per thread so we can safely reuse our
 		// StringBuffer.
 		m_buffer.delete(0, m_buffer.length());
 		
 		int p = 0;
 		int lastP = p;
 
 		while (true) {
 		    if ((p = original.indexOf('<', lastP)) == -1) {
 			m_buffer.append(original.substring(lastP));
 			break;
 		    }
 		    else {
 			m_buffer.append(original.substring(lastP, p));
 
 			lastP = p + 1;
 		    
 			p = original.indexOf('>', lastP);
 
 			if (p == -1) {
 			    throw new PluginException(
 				"URL for Test " + m_test.getTestNumber() +
 				" malformed");    
 			}
 			
 			final String methodName = original.substring(lastP, p);
 
 			final Method method =
 			    (Method)m_beanMethodMap.get(methodName);
 
 			if (method == null ) {
 			    throw new PluginException(
 				"URL for Test " + m_test.getTestNumber() +
 				" refers to unknown string bean method '" +
 				methodName + "'");
 			}
 
 			try {
 			    m_buffer.append((String)method.invoke(m_bean,
 								  m_noArgs));
 			}
 			catch (Exception e) {
 			    throw new PluginException(
 				"Failure invoking string bean method '" +
 				methodName + "'", e);
 			}
 
 			lastP = p + 1;
 		    }
 		}
 
 		return m_buffer.toString();
 	    }
 	}
 
 	public String getURLString()
 	    throws PluginException
 	{
 	    return replaceDynamicKeys(m_urlString);
 	}
 
 	public String getPostString()
 	    throws PluginException
 	{
 	    return replaceDynamicKeys(m_postString);
 	}
 
 	public String getAuthorizationString()
 	    throws PluginException
 	{
	    if (m_basicAuthenticationUserString == null ||
		m_basicAuthenticationPasswordString == null) {
 		return null;
 	    }
 
 	    final String user =
		replaceDynamicKeys(m_basicAuthenticationUserString);
 
 	    final String password =
		replaceDynamicKeys(m_basicAuthenticationPasswordString);
 	    
 	    return
 		"Basic " +
 		new Base64Encoder(user + ":" + password).processString();
 	}
 
 	public String getContextURLString() { return null; }
 	public long getIfModifiedSince() { return m_ifModifiedSince; }
 	public String getOKString() { return m_okString; }
 
 	protected void setURLString(String s) { m_urlString = s; }
 	protected void setPostString(String s) { m_postString = s; }
 	protected void setIfModifiedSince(long l) { m_ifModifiedSince = l; }
 	protected void setOKString(String s) { m_okString = s; }
     }
 
     /**
      * This method initializes the plug-in.
      */    
     public void initialize(PluginThreadContext pluginThreadContext)
 	throws PluginException
     {
 	m_pluginThreadContext = pluginThreadContext;
 
 	final GrinderProperties parameters =
 	    pluginThreadContext.getPluginParameters();
 
 	m_filenameFactory = pluginThreadContext.getFilenameFactory();
 
 	boolean useCookies = parameters.getBoolean("keepSession", false);
 
 	if (useCookies) {
 	    m_pluginThreadContext.logError(
 		"'keepSession' has been renamed to 'useCookies'." +
 		"Please update your grinder.properties.");
 	}
 	else {
 	    useCookies = parameters.getBoolean("useCookies", true);
 	}
 
         // tily@sylo.org / 2000/02/16 mod to set version string on or off - breaks jrun otherwise
         final boolean useCookiesVersionString =
 	    parameters.getBoolean("useCookiesVersionString", true);
   
 
 	m_httpMsg = new HttpMsg(pluginThreadContext, useCookies,
 				useCookiesVersionString,
 				parameters.getBoolean(
 				    "followRedirects", false),
 				parameters.getBoolean(
 				    "timeIncludesTransaction", false));
 
 	m_logHTML = parameters.getBoolean("logHTML", false);
 
 	final String stringBeanClassName =
 	    parameters.getProperty("stringBean", null);
 
 	if (stringBeanClassName != null) {
 	    try {
 		m_pluginThreadContext.logMessage("Instantiating " +
 						 stringBeanClassName);
 
 		final Class stringBeanClass =
 		    Class.forName(stringBeanClassName);
 
 		m_bean = stringBeanClass.newInstance();
 
 		if (StringBean.class.isAssignableFrom(stringBeanClass)) {
 		    m_stringBean = (StringBean)m_bean;
 		    m_stringBean.initialize(m_pluginThreadContext);
 		}
 		else {
 		    m_pluginThreadContext.logMessage(
 			stringBeanClassName + " does not implement " +
 			StringBean.class.getName() +
 			", skipping initialisation");
 		}
 
 		m_beanMethodMap = new HashMap();
 
 		final Method[] methods = stringBeanClass.getMethods();
 
 		for (int i=0; i<methods.length; i++) {
 		    final String name = methods[i].getName();
 
 		    if (name.startsWith("get") &&
 			methods[i].getReturnType() == String.class &&
 			methods[i].getParameterTypes().length == 0) {
 			m_beanMethodMap.put(name, methods[i]);
 		    }
 		}
 	    }
 	    catch(ClassNotFoundException e) {
 		throw new PluginException(
 		    "The specified string bean class '" +
 		    stringBeanClassName + "' was not found.", e);
 	    }
 	    catch (Exception e){
 		throw new PluginException (
 		"An instance of the string bean class '" +
 		stringBeanClassName + "' could not be created.", e);
 	    }
 	}
     }
 
     public void beginCycle() throws PluginException
     {
 	if (m_stringBean != null) {
 	    m_stringBean.beginCycle();
 	}
 
 	// Reset cookie if necessary.
 	m_httpMsg.reset();      
     }
 
     /**
      * This method processes the URLs.
      */    
     public boolean doTest(Test test) throws PluginException
     {
 	if (m_stringBean != null) {
 	    m_stringBean.doTest(test);
 	}
 
 	final Integer testNumber = test.getTestNumber();
 	
 	CallData callData = (CallData)m_callData.get(testNumber);
 	
 	if (callData == null) {
 	    callData = createCallData(m_pluginThreadContext, test);
 	    m_callData.put(testNumber, callData);
 	}
 	
 	// Do the call.
 	final String page;
 
 	try {
 	    page = m_httpMsg.sendRequest(callData);
 	}
 	catch (IOException e) {
 	    throw new PluginException("HTTP IOException: " + e, e);
 	}
 	finally {
 	    // In case timeIncludesTransaction = false
 	    m_pluginThreadContext.stopTimer();
 	}
 
 	final boolean error;
 	final String okString = callData.getOKString();
 
 	if (page == null) {
 	    error = okString != null;
 	}
 	else {
 	    error = okString != null && page.indexOf(okString) == -1;
 	
 	    if (m_logHTML || error) {
 		final String filename =
 		    m_filenameFactory.createFilename("page",
 						     "_" + m_currentIteration +
 						     "_" + testNumber +
 						     ".html");
 		try {
 		    final BufferedWriter htmlFile =
 			new BufferedWriter(new FileWriter(filename, false));
 
 		    htmlFile.write(page);
 		    htmlFile.close();
 		}
 		catch (IOException e) {
 		    throw new PluginException("Error writing to " + filename +
 					      ": " + e, e);
 		}
 
 		if (error) {
 		    m_pluginThreadContext.logError(
 			"The 'ok' string ('" + okString +
 			"') was not found in the page received. " +
 			"The output has been written to '" + filename + "'");
 		}
 	    }
 	}
 
 	return !error;
     }
 
     /**
       * Give derived classes a chance to be interesting.
       */
     protected CallData createCallData(PluginThreadContext pluginThreadContext,
  				      Test test)
 	throws PluginException
     {
  	return new CallData(test);
     }
 
     public void endCycle() throws PluginException
     {
 	if (m_stringBean != null) {
 	    m_stringBean.endCycle();
 	}
 
 	m_currentIteration++;
     }
 }
