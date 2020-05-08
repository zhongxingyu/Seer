 package org.synyx.minos.core.web.tags.menu;
 
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.servlet.tags.RequestContextAwareTag;
 
 import org.synyx.minos.core.web.UrlUtils;
 import org.synyx.minos.core.web.menu.Menu;
 import org.synyx.minos.core.web.menu.MenuItem;
 import org.synyx.minos.core.web.menu.MenuItems;
 import org.synyx.minos.core.web.menu.MenuProvider;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.jsp.JspWriter;
 
 
 /**
  * Tag rendering the applications menu using {@link MenuProvider} to get the needed {@link MenuItems}.
  *
  * @author Oliver Gierke - gierke@synyx.de
  * @author Marc Kannegiesser - kannegiesser@synyx.de
  */
 public class MenuTag extends RequestContextAwareTag {
 
     private static final long serialVersionUID = 3562560895559874960L;
 
     private String menuId = "MAIN";
 
     private String renderBean = null;
 
     private MenuRenderer renderer;
 
     @Override
     protected int doStartTagInternal() throws Exception {
 
         MenuProvider menuProvider = getMenuProvider();
 
         if (menuProvider == null) {
             return 0;
         }
 
         MenuItems menuItems = menuProvider.getMenu(getMenuId());
 
         if (menuItems.isEmpty()) {
             return 0;
         }
 
         renderer = getMenuRenderer();
 
         StringBuilder builder = new StringBuilder();
 
         MenuMetaInfo info = new MenuMetaInfo();
         info.setId(id);
 
         JspWriter out = pageContext.getOut();
 
         out.append(renderer.beforeMenu(info));
         buildHtmlMenu(menuItems, builder, 0);
         out.append(builder.toString());
         out.append(renderer.afterMenu(info));
 
         return 0;
     }
 
 
     /**
      * Builds an HTML menu from the given {@link MenuItem}s. Traverses the items down to the leafs, if they contain
      * submenues.
      *
      * @param menuItems
      * @param builder
      * @throws IOException
      */
     private void buildHtmlMenu(MenuItems menuItems, StringBuilder builder, Integer depth) throws IOException {
 
         String path = getPathWithinApplication(getRequest());
 
         for (Menu item : menuItems) {
             MenuMetaInfo info = new MenuMetaInfo();
             info.setActive(item.isActiveFor(path));
             info.setDescription(resolveMessage(item.getDesciption()));
             info.setId(item.getTitle().replace('.', '_'));
             info.setDepth(depth);
             info.setTitle(resolveMessage(item.getTitle()));
             info.setParent(item.hasSubMenues());
 
             if (item.getUrl() != null) {
                 info.setUrl(UrlUtils.toUrl(item.getUrl(), getRequest()));
             } else {
                 info.setUrl(null);
             }
 
             builder.append(renderer.beforeMenuItem(info));
             builder.append(renderer.renderItem(info));
 
             if (renderer.proceedWithRenderingSubmenus(info) && item.hasSubMenues()) {
                builder.append(renderer.beforeSubmenuItems(info));
                 buildHtmlMenu(item.getSubMenues(), builder, depth + 1);
                builder.append(renderer.afterSubmenuItems(info));
             }
 
             builder.append(renderer.afterMenuItem(info));
         }
     }
 
 
     private String getPathWithinApplication(HttpServletRequest request) {
 
         String uri = request.getRequestURI();
         String contextPath = request.getContextPath();
         String servletPath = request.getServletPath();
 
         return uri.substring(contextPath.length() + servletPath.length(), uri.length());
     }
 
 
     /**
      * Resolves the given resource bundle key to a localized message. Returns the key if no message can be found.
      *
      * @param key
      * @return
      */
     private String resolveMessage(String key) {
 
         return getApplicationContext().getMessage(key, null, getRequestContext().getLocale());
     }
 
 
     /**
      * Returns the current {@link HttpServletRequest}.
      *
      * @return
      */
     private HttpServletRequest getRequest() {
 
         return (HttpServletRequest) this.pageContext.getRequest();
     }
 
 
     /**
      * Returns the {@link WebApplicationContext}.
      *
      * @return
      */
     private WebApplicationContext getApplicationContext() {
 
         return getRequestContext().getWebApplicationContext();
     }
 
 
     private MenuProvider getMenuProvider() {
 
         try {
             return getApplicationContext().getBean("menuProvider", MenuProvider.class);
         } catch (RuntimeException ex) {
             return null;
         }
     }
 
 
     private MenuRenderer getMenuRenderer() {
 
         try {
             return getApplicationContext().getBean(renderBean, MenuRenderer.class);
         } catch (RuntimeException e) {
             return getApplicationContext().getBean("defaultMenuRenderer", MenuRenderer.class);
         }
     }
 
 
     public void setMenuId(String menuId) {
 
         this.menuId = menuId != null ? menuId : "MAIN";
     }
 
 
     public String getMenuId() {
 
         return menuId;
     }
 
 
     public String getRenderBean() {
 
         return renderBean;
     }
 
 
     public void setRenderBean(String argRenderBean) {
 
         renderBean = argRenderBean;
     }
 }
