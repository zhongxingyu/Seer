 package wicket.contrib.velocity;
 
 import java.io.IOException;
 import java.io.InputStream;
import java.net.URL;
 import java.util.Properties;
 
 import javax.servlet.ServletContext;
 
 import org.apache.velocity.app.Velocity;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import wicket.Application;
 import wicket.Component;
 import wicket.IInitializer;
 import wicket.WicketRuntimeException;
 import wicket.protocol.http.WebApplication;
 import wicket.util.file.WebApplicationPath;
 import wicket.util.lang.Packages;
 import wicket.util.resource.IResourceStream;
 
 /**
  * An implementation of {@link wicket.IInitializer} for the Velocity Runtime
  * Singleton. If Application is an instance of WebApplication, Initializer will
  * retrieve "velocityPropertiesFolder" as an initparam to point to the directory
  * the properties file lives in, and "velocity.properties" for the name of the
  * properties file. If the params don't exist, then velocity.properties next to
  * this class will be loaded.
  */
 public class Initializer implements IInitializer
 {
 	/** Log. */
 	private static final Logger log = LoggerFactory.getLogger(Component.class);
 
 	private String velocityPropertiesFile = "velocity.properties";
 
 	private String velocityPropertiesFolder;
 
 	/**
 	 * @see wicket.IInitializer#init(wicket.Application)
 	 */
 	public void init(Application application)
 	{
 		Properties props = getVelocityProperties(application);
 
 		try
 		{
 			if (null != props)
 			{
 				Velocity.init(props);
 			}
 			else
 			{
 				Velocity.init();
 			}
 			log.info("Initialized Velocity successfully");
 		}
 		catch (Exception e)
 		{
 			throw new WicketRuntimeException(e);
 		}
 	}
 
 	private Properties getVelocityProperties()
 	{
		ClassLoaderResourceStreamLocator clrsr = new ClassLoaderResourceStreamLocator();
 		String absolutePath = Packages.absolutePath(Initializer.class,
 				velocityPropertiesFile);
 		IResourceStream irs = clrsr.locate(Initializer.class, absolutePath);
 
 		try
 		{
 			InputStream is = irs.getInputStream();
 			Properties props = new Properties();
 			props.load(is);
 			return props;
 		}
 		catch (Exception e)
 		{
 			throw new WicketRuntimeException(e);
 		}
 	}
 
 	private Properties getVelocityProperties(Application application)
 	{
 		if (application instanceof WebApplication)
 		{
 			return getVelocityProperties((WebApplication) application);
 		}
 		else
 		{
 			return getVelocityProperties();
 		}
 	}
 
 	private Properties getVelocityProperties(WebApplication webapp)
 	{
 		ServletContext sc = webapp.getServletContext();
 		velocityPropertiesFolder = sc.getInitParameter("velocityPropertiesFolder");
 		String propsFile = webapp.getServletContext().getInitParameter(
 				"velocity.properties");
 
 		if (null != propsFile)
 		{
 			velocityPropertiesFile = propsFile;
 		}
 
 		if (null != velocityPropertiesFolder)
 		{
 			WebApplicationPath webPath = new WebApplicationPath(sc);
 			webPath.add(velocityPropertiesFolder);
			URL url = webPath.find(velocityPropertiesFile);
 			try
 			{
				InputStream is = url.openStream();
 				Properties props = new Properties();
 				props.load(is);
 				return props;
 			}
 			catch (IOException e)
 			{
 				throw new WicketRuntimeException(e);
 			}
 		}
 		else
 		{
 			return getVelocityProperties();
 		}
 	}
 
 }
