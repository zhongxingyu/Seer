 package md.frolov.legume.client.gin;
 
 import com.google.gwt.activity.shared.ActivityManager;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.inject.client.GinModules;
 import com.google.gwt.inject.client.Ginjector;
 import com.google.gwt.place.shared.PlaceController;
 import com.google.gwt.place.shared.PlaceHistoryHandler;
 import com.google.web.bindery.event.shared.EventBus;
 
 import md.frolov.legume.client.Application;
 import md.frolov.legume.client.Messages;
 import md.frolov.legume.client.elastic.ElasticSearchService;
 import md.frolov.legume.client.elastic.model.ModelFactory;
 import md.frolov.legume.client.service.ColorizeService;
 import md.frolov.legume.client.service.ConfigurationService;
 import md.frolov.legume.client.ui.MainView;
 import md.frolov.legume.client.ui.components.ConnectionManagerComponent;
import md.frolov.legume.client.util.ColorUtils;
 
 @GinModules({BusinessModule.class, ConstantsModule.class, MVPModule.class, ViewModule.class})
 public interface WidgetInjector extends Ginjector
 {
     WidgetInjector INSTANCE = GWT.create(WidgetInjector.class);
     
     MainView mainView();
     ConnectionManagerComponent connectionManagerComponent();
     
     Messages messages();
     
     EventBus eventBus();
     ActivityManager activityManager();
     PlaceHistoryHandler placeHistoryHandler();
     PlaceController placeController();
 
    ColorUtils colorUtils();

     ConfigurationService configurationService();
 
     ModelFactory modelFactory();
     ElasticSearchService elasticSearchService();
 
     Application application();
 
     ColorizeService colorizeService();
 }
