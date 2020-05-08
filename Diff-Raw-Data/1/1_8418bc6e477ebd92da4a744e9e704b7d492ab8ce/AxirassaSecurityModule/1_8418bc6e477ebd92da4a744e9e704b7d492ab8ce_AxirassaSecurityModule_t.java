 
 package axirassa.ioc;
 
 import org.apache.shiro.realm.AuthorizingRealm;
 import org.apache.shiro.realm.Realm;
 import org.apache.tapestry5.ioc.Configuration;
 import org.apache.tapestry5.ioc.ServiceBinder;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.ioc.annotations.SubModule;
 import org.hibernate.Session;
 
 import axirassa.dao.UserDAO;
 import axirassa.webapp.services.AxirassaSecurityService;
 import axirassa.webapp.services.EntityRealm;
 import axirassa.webapp.services.UserCredentialsMatcher;
 import axirassa.webapp.services.internal.AxirassaSecurityServiceImpl;
 
 @SubModule({ DAOModule.class })
 public class AxirassaSecurityModule {
 	@Inject
 	private Session database;
 
 	@Inject
 	private UserDAO userDAO;
 
 
 	public static void bind(ServiceBinder binder) {
 		binder.bind(AuthorizingRealm.class, EntityRealm.class);
 		binder.bind(AxirassaSecurityService.class, AxirassaSecurityServiceImpl.class);
 	}
 
 
 	public void contributeWebSecurityManager(Configuration<Realm> configuration) {
 		EntityRealm realm = new EntityRealm(userDAO);
 		realm.setCredentialsMatcher(new UserCredentialsMatcher(database));
 		configuration.add(realm);
 	}
 }
