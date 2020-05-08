 package org.cotrix.web.client;
 
 import java.util.EnumMap;
 
 import org.cotrix.web.client.presenter.CotrixWebPresenter;
 import org.cotrix.web.client.presenter.UserBarPresenter;
 import org.cotrix.web.client.view.Home;
 import org.cotrix.web.codelistmanager.client.CotrixManagerAppGinInjector;
 import org.cotrix.web.importwizard.client.CotrixImportAppGinInjector;
 import org.cotrix.web.menu.client.presenter.CotrixMenuGinInjector;
 import org.cotrix.web.menu.client.presenter.MenuPresenter;
 import org.cotrix.web.share.client.CotrixModule;
 import org.cotrix.web.share.client.CotrixModuleController;
 import org.cotrix.web.share.client.event.CotrixBus;
 import org.cotrix.web.share.client.event.SwitchToModuleEvent;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.inject.Inject;
 import com.google.web.bindery.event.shared.EventBus;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class AppControllerImpl implements AppController {
 	
 	protected EventBus cotrixBus;
 	protected CotrixWebPresenter cotrixWebPresenter;
 	protected EnumMap<CotrixModule, CotrixModuleController> controllers = new EnumMap<CotrixModule, CotrixModuleController>(CotrixModule.class);
 	protected CotrixModuleController currentController;
 	
 	@Inject
 	public AppControllerImpl(@CotrixBus EventBus cotrixBus, CotrixWebPresenter cotrixWebPresenter) {
 		this.cotrixBus = cotrixBus;
 		this.cotrixWebPresenter = cotrixWebPresenter;
 		
 		bind();
 		
 		initMenu();
 		
 		initUserBar();
 		
 		Home home = new Home();
 		addModule(home);
 		
 		CotrixImportAppGinInjector importInjector = CotrixImportAppGinInjector.INSTANCE;
 		addModule(importInjector.getController());
 		
 		CotrixManagerAppGinInjector managerInjector = CotrixManagerAppGinInjector.INSTANCE;
 		addModule(managerInjector.getController());
 		
 		/*CotrixPublishAppGinInjector publishInjector = CotrixPublishAppGinInjector.INSTANCE;
 		addModule(publishInjector.getController());*/
 		
		showModule(CotrixModule.HOME);
 	}
 	
 	protected void bind()
 	{
 		cotrixBus.addHandler(SwitchToModuleEvent.TYPE, new SwitchToModuleEvent.SwitchToModuleHandler() {
 			
 			@Override
 			public void onSwitchToModule(SwitchToModuleEvent event) {
 				showModule(event.getModule());		
 			}
 		});
 	}
 	
 	protected void initMenu()
 	{
 		CotrixMenuGinInjector menuInjector = CotrixMenuGinInjector.INSTANCE;
 		MenuPresenter menuPresenter = menuInjector.getMenuPresenter();
 		cotrixWebPresenter.setMenu(menuPresenter);
 	}
 	
 	protected void initUserBar()
 	{
 		UserBarPresenter presenter = AppGinInjector.INSTANCE.getPresenter();
 		cotrixWebPresenter.setUserBar(presenter);
 	}
 	
 	protected void addModule(CotrixModuleController controller)
 	{
 		cotrixWebPresenter.add(controller);
 		controllers.put(controller.getModule(), controller);
 	}
 	
 	protected void showModule(CotrixModule cotrixModule)
 	{
 		if (controllers.containsKey(cotrixModule)) {
 		if (currentController!=null) currentController.deactivate();
 		currentController = controllers.get(cotrixModule);
 		cotrixWebPresenter.showModule(cotrixModule);
 		currentController.activate();
 		} else Log.warn("Missing module "+cotrixModule+" forgot to register it?");
 	}
 
 	public void go(HasWidgets container) {
 		cotrixWebPresenter.go(container);
 	}
 
 }
