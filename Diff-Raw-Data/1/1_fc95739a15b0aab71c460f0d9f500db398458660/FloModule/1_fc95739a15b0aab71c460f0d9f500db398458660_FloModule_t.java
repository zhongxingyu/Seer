 package net.sf.flophase.floweb;
 
 import java.util.Properties;
 
 import javax.inject.Singleton;
 
 import net.sf.flophase.floweb.account.AccountDAO;
 import net.sf.flophase.floweb.account.AccountService;
 import net.sf.flophase.floweb.account.AccountStore;
 import net.sf.flophase.floweb.account.FloAccountDAO;
 import net.sf.flophase.floweb.account.FloAccountService;
 import net.sf.flophase.floweb.account.FloAccountStore;
 import net.sf.flophase.floweb.cashflow.CashFlowDAO;
 import net.sf.flophase.floweb.cashflow.CashFlowImportStatus;
 import net.sf.flophase.floweb.cashflow.CashFlowImportStore;
 import net.sf.flophase.floweb.cashflow.CashFlowService;
 import net.sf.flophase.floweb.cashflow.CashFlowStore;
 import net.sf.flophase.floweb.cashflow.CashFlowTradeStore;
 import net.sf.flophase.floweb.cashflow.FloCashFlowDAO;
 import net.sf.flophase.floweb.cashflow.FloCashFlowImportStore;
 import net.sf.flophase.floweb.cashflow.FloCashFlowService;
 import net.sf.flophase.floweb.cashflow.FloCashFlowStore;
 import net.sf.flophase.floweb.cashflow.FloCashFlowTradeStore;
 import net.sf.flophase.floweb.common.Constants;
 import net.sf.flophase.floweb.common.ServletRequestWrapper;
 import net.sf.flophase.floweb.entry.EntryDAO;
 import net.sf.flophase.floweb.entry.EntryService;
 import net.sf.flophase.floweb.entry.EntryStore;
 import net.sf.flophase.floweb.entry.FloEntryDAO;
 import net.sf.flophase.floweb.entry.FloEntryService;
 import net.sf.flophase.floweb.entry.FloEntryStore;
 import net.sf.flophase.floweb.ui.FloUserInterfaceService;
 import net.sf.flophase.floweb.ui.UserInterfaceService;
 import net.sf.flophase.floweb.user.FloUserService;
 import net.sf.flophase.floweb.user.FloUserStore;
 import net.sf.flophase.floweb.user.UserService;
 import net.sf.flophase.floweb.user.UserStore;
 import net.sf.flophase.floweb.xaction.FloTransactionDAO;
 import net.sf.flophase.floweb.xaction.FloTransactionService;
 import net.sf.flophase.floweb.xaction.FloTransactionStore;
 import net.sf.flophase.floweb.xaction.TransactionDAO;
 import net.sf.flophase.floweb.xaction.TransactionService;
 import net.sf.flophase.floweb.xaction.TransactionStore;
 
 import org.apache.velocity.app.VelocityEngine;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.memcache.MemcacheService;
 import com.google.appengine.api.memcache.MemcacheServiceFactory;
 import com.google.appengine.api.taskqueue.Queue;
 import com.google.appengine.api.taskqueue.QueueFactory;
 import com.google.appengine.api.users.UserServiceFactory;
 import com.google.gson.ExclusionStrategy;
 import com.google.gson.FieldAttributes;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.inject.AbstractModule;
 import com.google.inject.Provider;
 import com.google.inject.TypeLiteral;
 import com.google.inject.servlet.ServletScopes;
 import com.googlecode.objectify.ObjectifyFilter;
 import com.googlecode.objectify.annotation.Parent;
 
 /**
  * This class performs all the bindings to allow for dependency injection.
  */
 public class FloModule extends AbstractModule {
 
 	@Override
 	protected void configure() {
 		bindGson();
 
 		bindVelocity();
 
 		bindAppEngineServices();
 
 		bindFloWebServices();
 	}
 
 	/**
 	 * Binds the velocity instance.
 	 */
 	private void bindVelocity() {
 		Properties props = new Properties();
 		props.setProperty("resource.loader", "class");
 		props.setProperty("class.resource.loader.class",
 				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
 
 		VelocityEngine velocity = new VelocityEngine();
 		velocity.init(props);
 
 		bind(VelocityEngine.class).toInstance(velocity);
 	}
 
 	/**
 	 * This binds all the logic interfaces to their implementations.
 	 */
 	protected void bindFloWebServices() {
 		bind(UserStore.class).to(FloUserStore.class);
 
 		bind(AccountDAO.class).to(FloAccountDAO.class);
 		bind(AccountService.class).to(FloAccountService.class);
 		bind(AccountStore.class).to(FloAccountStore.class);
 
 		bind(TransactionDAO.class).to(FloTransactionDAO.class);
 		bind(TransactionService.class).to(FloTransactionService.class);
 		bind(TransactionStore.class).to(FloTransactionStore.class);
 
 		bind(EntryDAO.class).to(FloEntryDAO.class);
 		bind(EntryService.class).to(FloEntryService.class);
 		bind(EntryStore.class).to(FloEntryStore.class);
 
 		bind(CashFlowDAO.class).to(FloCashFlowDAO.class);
 		bind(CashFlowStore.class).to(FloCashFlowStore.class).in(
 				ServletScopes.REQUEST);
 		bind(CashFlowImportStore.class).to(FloCashFlowImportStore.class);
 		bind(CashFlowTradeStore.class).to(FloCashFlowTradeStore.class);
 		bind(CashFlowService.class).to(FloCashFlowService.class);
 
 		bind(new TypeLiteral<ServletRequestWrapper<CashFlowImportStatus>>() {
 		}).toInstance(new ServletRequestWrapper<CashFlowImportStatus>());
 
 		bind(UserStore.class).to(FloUserStore.class);
 		bind(UserService.class).to(FloUserService.class);
 
 		bind(UserInterfaceService.class).to(FloUserInterfaceService.class);
 	}
 
 	/**
 	 * Bind all the App Engine specific services to their instances.
 	 */
 	protected void bindAppEngineServices() {
 		bind(com.google.appengine.api.users.UserService.class).toInstance(
 				UserServiceFactory.getUserService());
 		bind(MemcacheService.class).toInstance(
 				MemcacheServiceFactory.getMemcacheService());
 		bind(DatastoreService.class).toInstance(
 				DatastoreServiceFactory.getDatastoreService());
 		bind(Queue.class).toProvider(new Provider<Queue>() {
 
 			@Override
 			public Queue get() {
 				return QueueFactory.getDefaultQueue();
 			}
 
 		});
 
 		// objectify filter
 		bind(ObjectifyFilter.class).in(Singleton.class);
 	}
 
 	/**
 	 * This binds the JSON formatter to a properly configured instance.
 	 */
 	protected void bindGson() {
 		ExclusionStrategy strategy = new ExclusionStrategy() {
 
 			@Override
 			public boolean shouldSkipField(FieldAttributes f) {
 				// we want to skip this annotation when serializing to JSON as
 				// it is not necessary to know the parent.
 				return f.getAnnotation(Parent.class) != null;
 			}
 
 			@Override
 			public boolean shouldSkipClass(Class<?> clazz) {
 				return false;
 			}
 		};
 
 		Gson gson = new GsonBuilder()
 				.addSerializationExclusionStrategy(strategy)
				.serializeSpecialFloatingPointValues()
 				.setDateFormat(Constants.ISO_DATE_FORMAT).create();
 		bind(Gson.class).toInstance(gson);
 	}
 
 }
