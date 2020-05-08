 package nl.minicom.evenexus.core;
 
 import javax.inject.Singleton;
 
 import nl.minicom.evenexus.eveapi.importers.ImportManager;
 import nl.minicom.evenexus.gui.tables.columns.ColumnModel;
 import nl.minicom.evenexus.gui.tables.renderers.CurrencyRenderer;
 import nl.minicom.evenexus.gui.tables.renderers.DateTimeRenderer;
 import nl.minicom.evenexus.gui.tables.renderers.IntegerRenderer;
 import nl.minicom.evenexus.gui.utils.dialogs.BugReportDialog;
 import nl.minicom.evenexus.inventory.InventoryManager;
 import nl.minicom.evenexus.persistence.Database;
 import nl.minicom.evenexus.persistence.interceptor.Transactional;
 import nl.minicom.evenexus.persistence.interceptor.TransactionalInterceptor;
 import nl.minicom.evenexus.persistence.versioning.RevisionExecutor;
 import nl.minicom.evenexus.utils.ProxyManager;
 import nl.minicom.evenexus.utils.SettingsManager;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.matcher.Matchers;
 
 public class ApplicationModule extends AbstractModule {
 
 	@Override
 	protected void configure() {
 		Database database = new Database();
 		bind(Database.class).toInstance(database);
 		bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), 
 				new TransactionalInterceptor(database));
 		
 		bind(Application.class).in(Singleton.class);
 		bind(SettingsManager.class).in(Singleton.class);
 		bind(InventoryManager.class).in(Singleton.class);
 		bind(RevisionExecutor.class).in(Singleton.class);
 		bind(ProxyManager.class).in(Singleton.class);
 		bind(ImportManager.class).in(Singleton.class);
 		
 		bind(BugReportDialog.class).in(Singleton.class);
 		
 		bind(CurrencyRenderer.class).in(Singleton.class);
 		bind(IntegerRenderer.class).in(Singleton.class);
 		bind(DateTimeRenderer.class).in(Singleton.class);
 		
 		requestStaticInjection(ColumnModel.class);
 	}
 
 }
