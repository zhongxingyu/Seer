 package gov.nih.nci.cagwas.web.action;
 
 import gov.nih.nci.cagwas.application.zip.ZipFindingsHelper;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.log4j.Logger;
 
 /**
  * The RemoteContentHelper class will retrieve remote content from a provided
  * URL.
  * <P>
  * @author mholck
  */
 public class RemoteContentHelper
 {
 	private static Logger logger = Logger.getLogger(RemoteContentHelper.class);
 	
 	private boolean propertiesLoaded = false;
 	private Properties mailProperties = null;
 	
 	/**
 	 * getRemoteContent will take the propertyName passed in and read the mail.properties
 	 * file to get the necessary URL to get the remote content and will return this
 	 * content as a String
 	 * <P>
 	 * @param propertyName The property name in mail.properties to use for remote content
 	 * @return String The remote content or null if not available
 	 */
 	public String getRemoteContent(String propertyName)
 	{
 		String responseBody = null;
 		
 		// Load the properties files
 		if (!propertiesLoaded)
 			loadProperties();
 		
 		String remoteUrl = mailProperties.getProperty("remote.url");
 		String remoteBody = mailProperties.getProperty(propertyName);
		if(remoteUrl != null && remoteBody != null){
			responseBody = getContent(remoteUrl + remoteBody);
		}
 		return responseBody;
 	}
 	
 	/**
 	 * getProperty will return a property value given the name passed in if it is in the
 	 * mail.properties file
 	 * <P>
 	 * @param propertyName The property name to get the value of
 	 * @return String The value of the property
 	 */
 	public String getProperty(String propertyName)
 	{
 		String property = null;
 		
 		if (!propertiesLoaded)
 			loadProperties();
 		
 		property = mailProperties.getProperty(propertyName);
 		
 		return property;
 	}
 	
 	/**
 	 * loadProperties loads the mail.properties file for accessing the properties
 	 * for remote content
 	 */
 	private void loadProperties()
 	{
 		mailProperties = new Properties();
 		String fileName = System.getProperty(ZipFindingsHelper.getMailPropertiesFilename());
 		
 		try
 		{
 			FileInputStream in = new FileInputStream(fileName);
 			mailProperties.load(in);
 		}
 		catch (FileNotFoundException e)
 		{
 			logger.error("Unable to open the mail.properties file", e);
 		}
 		catch (IOException e) 
 		{
 			logger.error("Error reading the mail.properties file", e);
 		}
 		
 		propertiesLoaded = true;
 	}
 	
 	/**
 	 * getContent will connect to the passed in address and read in the contents and
 	 * return them as a String.
 	 * <P>
 	 * @param addr The URL address to read the contents from
 	 * @return String the contents or null if unable to connect
 	 */
 	private String getContent(String addr)
 	{
 		String responseBody = null;
 		
 		HttpClient client = new HttpClient();
 		client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
 		HttpMethod method = new GetMethod(addr);
 		method.setFollowRedirects(true);
 		
 		try
 		{
 			client.executeMethod(method);
 			responseBody = method.getResponseBodyAsString();
 		}
 		catch (HttpException e)
 		{
 			logger.error("Error connecting to remote site", e);
 		}
 		catch (IOException e)
 		{
 			logger.error("Error connecting to remote site", e);
 		}
 		
 		return responseBody;
 	}
 }
