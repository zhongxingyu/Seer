 package net.sf.ipn.app;
 
 import net.sf.ipn.app.page.Home;
 import wicket.ISessionFactory;
 import wicket.Session;
 import wicket.protocol.http.WebApplication;
import wicket.util.file.Folder;
import wicket.util.file.Path;
 import wicket.util.time.Duration;
 
 /**
  * 
  */
 public class IpnApplication extends WebApplication
 {
 	/**
 	 * Construct.
 	 */
 	public IpnApplication()
 	{
 		getPages().setHomePage(Home.class);
 		getSettings().setResourcePollFrequency(Duration.ONE_SECOND);
 	}
 
 	/**
 	 * @see wicket.protocol.http.WebApplication#getSessionFactory()
 	 */
 	public ISessionFactory getSessionFactory()
 	{
 		return new ISessionFactory()
 		{
 			public Session newSession()
 			{
 				return new IpnSession(IpnApplication.this);
 			}
 		};
 	}
 
 }
