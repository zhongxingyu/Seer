 package de.mancino.auctioneer.webapp;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.authorization.strategies.role.Roles;
 import org.apache.wicket.behavior.AbstractBehavior;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import de.mancino.auctioneer.bo.ErrorLogBO;
 
 public class BasePage extends WebPage {
 
     @SpringBean
     private ErrorLogBO errorLogBO;
 
     public BasePage() {
         addMenuBarLink("linkToHome", HomePage.class, true);
         addMenuBarLink("linkToPriceWatch", PriceWatchPage.class, AuctioneerSession.get().isSignedIn());
         /*
         addMenuBarLink("linkToPurchaseOrder", PurchaseOrderPage.class, AuctioneerSession.get().getRoles() != null &&
                 AuctioneerSession.get().getRoles().contains(Roles.ADMIN));
         */
         addMenuBarLink("linkToCrafting", CraftingPage.class, AuctioneerSession.get().isSignedIn());
         addMenuBarLink("linkToFarming", FarmingPage.class, AuctioneerSession.get().isSignedIn());
         addMenuBarLink("linkToBargains", BargainPage.class, AuctioneerSession.get().isSignedIn());
         addMenuBarLink("linkToItems", ItemPage.class, AuctioneerSession.get().getRoles() != null &&
                 AuctioneerSession.get().getRoles().contains(Roles.ADMIN));
         addMenuBarLink("linkToManage", ManagePage.class, AuctioneerSession.get().getRoles() != null &&
                 AuctioneerSession.get().getRoles().contains(Roles.ADMIN));
         addMenuBarLink("linkToErrorLog", ErrorLogPage.class, AuctioneerSession.get().getRoles() != null &&
                 AuctioneerSession.get().getRoles().contains(Roles.ADMIN)).add(getErrorLogLabel());
         addMenuBarLink("linkToCashLog", CashLogPage.class, AuctioneerSession.get().getRoles() != null &&
                 AuctioneerSession.get().getRoles().contains(Roles.ADMIN));
         addMenuBarLink("linkToAdminConsole", AdminConsolePage.class, AuctioneerSession.get().getRoles() != null &&
                 AuctioneerSession.get().getRoles().contains(Roles.ADMIN));
         addMenuBarLink("linkToLogin", LoginPage.class, !AuctioneerSession.get().isSignedIn());
         add(new Link<String>("logoutLink") {
             private static final long serialVersionUID = 1L;
 
             @Override
             public void onClick() {
                 AuctioneerSession.get().invalidate();
                 setResponsePage(HomePage.class);
             }
 
             @Override
             public boolean isEnabled() {
                 return true;
             }
 
             @Override
             public boolean isVisible() {
                 return AuctioneerSession.get().isSignedIn();
             }
         });
         add(new Label("pageTitle", new Model<String>("Auctioneer")));
     }
 
     protected void clearErrors() {
         AuctioneerSession.get().setLastErrors(errorLogBO.getAll().size());
     }
 
     private Label getErrorLogLabel() {
         final int errorCount = errorLogBO.getAll().size() - AuctioneerSession.get().getLastErrors();
         final String labelText = errorCount > 0 ? " (" + errorCount + ")" : "";
         return new Label("errorCount", labelText);
     }
 
     protected void setPageTitle(final String pageTitle) {
         get("pageTitle").setDefaultModel(new Model<String>(pageTitle));
       }
 
     private Link<String> addMenuBarLink(final String wicketId, final Class<? extends WebPage> pageClass,
             final boolean isVisible) {
         final Link<String> link = new Link<String>(wicketId) {
                     private static final long serialVersionUID = 1L;
 
                     @Override
                     public void onClick() {
                         setResponsePage(pageClass);
                     }
 
                     @Override
                     public boolean isEnabled() {
                         return !(getPage().getClass().equals(pageClass));
                     }
 
                     @Override
                     public boolean isVisible() {
                         return isVisible;
                     }
                 };
         link.add(new AbstractBehavior() {
             private static final long serialVersionUID = 1L;
             @Override
             public void onComponentTag(Component component, ComponentTag tag) {
                 super.onComponentTag(component, tag);
                 if (!link.isEnabled()) {
                     tag.append("class", "pageSelected", " ");
                 }
             }
         });
         add(link);
         return link;
     }
 }
