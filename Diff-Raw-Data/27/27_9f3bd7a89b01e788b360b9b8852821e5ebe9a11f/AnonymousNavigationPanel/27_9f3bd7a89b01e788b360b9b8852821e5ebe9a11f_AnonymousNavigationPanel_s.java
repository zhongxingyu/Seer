 package de.flower.rmt.ui.page.base;
 
import de.flower.common.ui.panel.BasePanel;
import de.flower.rmt.ui.app.Links;
import de.flower.rmt.ui.page.about.AboutPage;
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.link.AbstractLink;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 
 /**
  * @author flowerrrr
  */
 public class AnonymousNavigationPanel extends BasePanel {
 
     public static final String HOME = "home";
 
     public AnonymousNavigationPanel() {
         super("navigationPanel");
 
         add(new BookmarkablePageLink("about", AboutPage.class));
 
        add(createMenuItem(HOME, Links.contextRoot("home"), null));
     }
 
     public WebMarkupContainer createMenuItem(String pageName, AbstractLink link, final INavigationPanelAware page) {
         WebMarkupContainer li = new WebMarkupContainer(pageName);
         li.add(link);
         if (page != null && page.getActiveTopBarItem().equals(pageName)) {
             li.add(AttributeModifier.append("class", "active"));
         }
         return li;
     }
 }
