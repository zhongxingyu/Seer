 package fi.muikku.plugins.fish;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.ejb.Stateful;
 import javax.enterprise.context.ApplicationScoped;
 import javax.inject.Inject;
 
 import fi.muikku.WidgetLocations;
 import fi.muikku.controller.WidgetController;
 import fi.muikku.model.widgets.DefaultWidget;
 import fi.muikku.model.widgets.Widget;
 import fi.muikku.model.widgets.WidgetLocation;
 import fi.muikku.model.widgets.WidgetVisibility;
 import fi.muikku.plugin.PluginDescriptor;
 
 @ApplicationScoped
 @Stateful
 public class FishPluginDescriptor implements PluginDescriptor {
 
  private static final String FISH_WIDGET_LOCATION = "environment.header.left";
   private static final String FISH_WIDGET_NAME = "fish";
 
   @Inject
   private WidgetController widgetController;
 
   @Override
   public String getName() {
     return "fish";
   }
 
   @Override
   public void init() {
     Widget widget = widgetController.findWidget(FISH_WIDGET_NAME);
     if (widget == null) {
       widget = widgetController.createWidget(FISH_WIDGET_NAME, WidgetVisibility.EVERYONE);
     }
 
     WidgetLocation widgetLocation = widgetController.findWidgetLocation(FISH_WIDGET_LOCATION);
     if (widgetLocation == null) { // TODO: In a perfect world, there would be no null checks
       widgetLocation = widgetController.createWidgetLocation(FISH_WIDGET_LOCATION);
     }
 
     DefaultWidget defaultWidget = widgetController.findDefaultWidget(widget, widgetLocation);
     if (defaultWidget == null) {
       defaultWidget = widgetController.createDefaultWidget(widget, widgetLocation);
     }
   }
 
   @Override
   public List<Class<?>> getBeans() {
     return new ArrayList<Class<?>>(Arrays.asList(
     /* Controllers */
 
     FishWidgetController.class,
 
     /* Backing Beans */
 
     FishWidgetBackingBean.class));
   }
 
 }
