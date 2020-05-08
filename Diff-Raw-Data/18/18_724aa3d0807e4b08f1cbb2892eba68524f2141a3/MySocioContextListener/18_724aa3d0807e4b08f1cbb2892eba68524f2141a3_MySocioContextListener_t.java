 /**
  * 
  */
 package net.mysocio.context;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 import net.mysocio.authentication.AuthenticationResourcesManager;
 import net.mysocio.authentication.facebook.FacebookAuthenticationManager;
 import net.mysocio.authentication.google.GoogleAuthenticationManager;
 import net.mysocio.authentication.linkedin.LinkedinAuthenticationManager;
 import net.mysocio.authentication.lj.LjAuthenticationManager;
 import net.mysocio.authentication.twitter.TwitterAuthenticationManager;
 import net.mysocio.authentication.vkontakte.VkontakteAuthenticationManager;
 import net.mysocio.data.IDataManager;
 import net.mysocio.data.accounts.facebook.FacebookAccount;
 import net.mysocio.data.accounts.google.GoogleAccount;
 import net.mysocio.data.accounts.lj.LjAccount;
 import net.mysocio.data.management.AccountsManager;
 import net.mysocio.data.management.CamelContextManager;
 import net.mysocio.data.management.DataManagerFactory;
 import net.mysocio.data.management.DefaultResourcesManager;
 import net.mysocio.data.management.MongoDataManager;
 import net.mysocio.ui.managers.basic.DefaultUiManager;
 
 import org.apache.activemq.camel.component.ActiveMQComponent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.code.morphia.Datastore;
 import com.google.code.morphia.Morphia;
 import com.mongodb.Mongo;
import com.mongodb.ServerAddress;
 
 /**
  * @author Aladdin
  * 
  */
 public class MySocioContextListener implements ServletContextListener {
 	private static final Logger logger = LoggerFactory.getLogger(MySocioContextListener.class);
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.
 	 * ServletContextEvent)
 	 */
 	@Override
 	public void contextDestroyed(ServletContextEvent arg0) {
 		CamelContextManager.stopContext();
 		DataManagerFactory.closeDataConnection();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * javax.servlet.ServletContextListener#contextInitialized(javax.servlet
 	 * .ServletContextEvent)
 	 */
 	@Override
 	public void contextInitialized(ServletContextEvent arg0) {
 		System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
 		System.setProperty("file.encoding", "UTF-8");
 		Morphia morphia = new Morphia();
 		morphia.mapPackage("net.mysocio.data")
 				.mapPackage("net.mysocio.ui.data.objects")
 				.mapPackage("net.mysocio.connection");
 		try {
			Datastore ds = new Morphia().createDatastore(new Mongo("mongodb-mysocio.jelastic.dogado.eu", 27017), "mySocio", "admin", "z1mtq70Yt1".toCharArray());
 			IDataManager manager = new MongoDataManager(ds);
 			DataManagerFactory.init(manager);
 		} catch (Exception e) {
 			logger.error("Error initializing database", e);
 		}
 		DefaultUiManager.init();
 		ServletContext servletContext = arg0.getServletContext();
 		DefaultResourcesManager.init(servletContext.getRealPath(""));
 		AuthenticationResourcesManager.init(servletContext.getRealPath(""));
 		CamelContextManager.addComponent("activemq", ActiveMQComponent
 				.activeMQComponent("vm://localhost?broker.persistent=true"));
 		CamelContextManager.initContext();
 		AccountsManager.getInstance().addAccount(GoogleAccount.ACCOUNT_TYPE,
 				new GoogleAuthenticationManager());
 		AccountsManager.getInstance().addAccount(FacebookAccount.ACCOUNT_TYPE,
 				new FacebookAuthenticationManager());
 		AccountsManager.getInstance().addAccount("twitter",
 				new TwitterAuthenticationManager());
 		AccountsManager.getInstance().addAccount("vkontakte",
 				new VkontakteAuthenticationManager());
 		AccountsManager.getInstance().addAccount("linkedin",
 				new LinkedinAuthenticationManager());
 		AccountsManager.getInstance().addAccount(LjAccount.ACCOUNT_TYPE,
 				new LjAuthenticationManager());
 	}
 }
