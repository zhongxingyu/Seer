 package ru.skalodrom_rf.web;
 
 import org.apache.wicket.Page;
 import org.apache.wicket.Request;
 import org.apache.wicket.RequestCycle;
 import org.apache.wicket.Response;
 import org.apache.wicket.protocol.http.WebApplication;
 import org.apache.wicket.protocol.http.WebRequest;
 import org.apache.wicket.request.IRequestCycleProcessor;
 import org.apache.wicket.request.target.coding.IndexedParamUrlCodingStrategy;
 import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
 import org.apache.wicket.util.file.Folder;
 import org.apache.wicket.util.file.Path;
 import org.springframework.orm.hibernate3.HibernateTransactionManager;
 import ru.skalodrom_rf.web.hibernate.TransactionalWebRequestCycle;
 import ru.skalodrom_rf.web.pages.*;
 
 import javax.annotation.Resource;
 
 /**
  * Application object for your web application.
  */
 public class WicketApplication extends WebApplication{
     @Resource
     IRequestCycleProcessor openSessionInView;
     @Resource
     HibernateTransactionManager transactionManager;
 
     /**
      * Constructor
      */
 	public WicketApplication(){
 	}
 	
 	/**
 	 * @see org.apache.wicket.Application#getHomePage()
 	 */
 	public Class<? extends Page> getHomePage(){
 		return IndexPage.class;
 	}
 
     @Override
     protected void init() {
         super.init();
         addComponentInstantiationListener(new SpringComponentInjector(this));
 
         mountBookmarkablePage("/register.html", RegisterPage.class);
 
         mountBookmarkablePage("/login.html", LoginPage.class);
         mountBookmarkablePage("/activate.html", ActivateUserPage.class);
 
         mountBookmarkablePage("/sendMessage.html", SendMessagePage.class);
         mountBookmarkablePage("/skalodrom.html", SkalodromPage.class);
         mountBookmarkablePage("/index.html", IndexPage.class);
         mountBookmarkablePage("/reminder.html", ReminderPage.class);
        mountBookmarkablePage("/profileView.html", ProfileViewPage.class);
         mountBookmarkablePage("/profileEdit.html", ProfileEditPage.class);
         mountBookmarkablePage("/FileNotFound.html", FileNotFoundPage.class);
         mountBookmarkablePage("/message.html", MessagePage.class);
         mount(new IndexedParamUrlCodingStrategy("/users",ProfileViewPage.class));
 
         getMarkupSettings().setDefaultMarkupEncoding("UTF-8");
         getMarkupSettings().setStripWicketTags(true);
         getDebugSettings().setAjaxDebugModeEnabled(false);
         if (DEVELOPMENT.equalsIgnoreCase(getConfigurationType())){
             Path resourceFinder = new Path(new Folder("src/main/resources"));
             getResourceSettings().setResourceFinder(resourceFinder);
         }
     }
 
 
 
     @Override
     public RequestCycle newRequestCycle(Request request, Response response) {
         return new TransactionalWebRequestCycle(transactionManager, this, (WebRequest)request,response);
     }
 }
