 package org.corewall.ui.data.internal;
 
 import org.corewall.ui.app.DefaultMenuContribution;
 import org.corewall.ui.app.MenuBuilder;
 import org.corewall.ui.app.MenuContribution;
 import org.corewall.ui.data.DataManager;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.multibindings.Multibinder;
 
 /**
  * Standard menu contributions for the DataManager app.
  * 
  * @author Josh Reed (jareed@andrill.org)
  */
 public class DataManagerModule extends AbstractModule {
 
 	@Override
 	protected void configure() {
 		// register standard menu contributions
 		Multibinder<MenuContribution> menu = Multibinder.newSetBinder(binder(), MenuContribution.class);
 
 		menu.addBinding().toInstance(
 				new DefaultMenuContribution(DataManager.APPLICATION_ID, MenuBuilder.menu("File", "New"),
 						new NewProjectAction()));
 	}
 }
