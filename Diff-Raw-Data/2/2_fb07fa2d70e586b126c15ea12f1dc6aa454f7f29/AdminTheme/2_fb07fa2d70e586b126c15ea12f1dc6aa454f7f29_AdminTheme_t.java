 package main.origo.admin.themes;
 
 import main.origo.admin.annotations.Admin;
 import main.origo.core.annotations.Decorates;
 import main.origo.core.annotations.Theme;
 import main.origo.core.annotations.ThemeVariant;
 import main.origo.core.helpers.DefaultDecorator;
 import main.origo.core.helpers.ThemeHelper;
 import main.origo.core.ui.UIElement;
 import play.api.templates.Html;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.origo.admin.decorators.dashboard;
 import views.html.origo.admin.decorators.dashboard_item;
 import views.html.origo.admin.themes.AdminTheme.variant_main_only;
 
 import java.util.Collections;
 
 @Theme(id = "admin")
 public class AdminTheme {
 
     public static final String DEFAULT_VARIANT_NAME = "admin-default";
 
     @ThemeVariant(id = DEFAULT_VARIANT_NAME, regions = "main")
     public static Result renderDefaultTemplate(ThemeVariant.Context context) {
         return Controller.ok(variant_main_only.render(context));
     }
 
     private static String defaultDashboardClasses() {
         return "dashboard";
     }
 
     @Decorates(type = Admin.DASHBOARD)
     public static Html decorateDashboard(Decorates.Context context) {
         Html body = ThemeHelper.decorateChildren(context.uiElement, context.renderingContext);
         return dashboard.render(context.uiElement, body,
                 DefaultDecorator.combineAttributes(context.uiElement.getAttributes(),
                         Collections.singletonMap("class", defaultDashboardClasses() + " row-fluid")));
     }
 
     public static String defaultDashboardItemClasses() {
         return "dashboard-item item";
     }
 
     @Decorates(type = Admin.DASHBOARD_ITEM)
     public static Html decorateDashboardItem(Decorates.Context context) {
         Html body = ThemeHelper.decorateChildren(context.uiElement, context.renderingContext);
         return dashboard_item.render(context.uiElement, body,
                 DefaultDecorator.combineAttributes(context.uiElement.getAttributes(),
                         Collections.singletonMap("class", defaultDashboardItemClasses() + " span3")));
     }
 
     @Decorates(type = {UIElement.INPUT_SUBMIT, UIElement.INPUT_BUTTON, UIElement.INPUT_RESET})
     public static Html decorateButton(Decorates.Context context) {
         context.uiElement.addAttribute("class", "btn");
        return DefaultDecorator.decorate(context.uiElement, context.renderingContext);
     }
 
 }
