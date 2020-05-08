 package main.origo.admin.interceptors.content;
 
 import main.origo.admin.helpers.DashboardHelper;
 import main.origo.admin.themes.AdminTheme;
 import main.origo.core.Node;
 import main.origo.core.annotations.Core;
 import main.origo.core.annotations.Interceptor;
 import main.origo.core.annotations.OnLoad;
 import main.origo.core.annotations.Provides;
 import main.origo.core.ui.Element;
 import models.origo.admin.AdminPage;
 import models.origo.core.BasicPage;
 import models.origo.core.RootNode;
 import views.html.origo.admin.decorators.basicpage.list;
 
 import java.util.List;
 
 @Interceptor
 public class BasicPageAdminListProvider {
 
     /**
      * Provides a type with the static name 'origo.admin.basicpage.list'.
      *
      * @param context contains a root node with an node id
      * @return a node to be presented as part of the admin UI
      */
     @Provides(type = Core.Type.NODE, with = BasicPageAdminProvider.LIST_TYPE)
     public static Node createListPage(Provides.Context context) {
         AdminPage page = new AdminPage((RootNode)context.node());
         page.setTitle("List Basic Pages");
         page.addElement(DashboardHelper.createBreadcrumb(BasicPageAdminProvider.BASE_TYPE), AdminTheme.topMeta());
         return page;
     }
 
     /**
      * Adds content to the nodes with the static name 'origo.admin.basicpage.list'.
      *
      * @param context contains a node of the type 'origo.admin.basicpage.list'.
      */
     @OnLoad(type = Core.Type.NODE, with = BasicPageAdminProvider.LIST_TYPE)
     public static void createListPage(OnLoad.Context context) {
         List<BasicPage> basicPages = BasicPage.findAllLatestVersions();
 
         context.node().addElement(new Element.Raw().setBody(list.render(basicPages)));
     }
 
 
 }
