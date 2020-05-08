 /**
  * 
  */
 package org.iplantc.de.client.desktop.views;
 
 import java.util.List;
 
 import org.iplantc.core.resources.client.DEHeaderStyle;
 import org.iplantc.core.resources.client.IplantResources;
 import org.iplantc.core.uicommons.client.collaborators.presenter.ManageCollaboratorsPresenter.MODE;
 import org.iplantc.core.uicommons.client.collaborators.views.ManageCollaboratorsDailog;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.models.WindowState;
 import org.iplantc.core.uicommons.client.util.WindowUtil;
 import org.iplantc.core.uicommons.client.widgets.IPlantAnchor;
 import org.iplantc.de.client.Constants;
 import org.iplantc.de.client.DeResources;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.desktop.widget.Desktop;
 import org.iplantc.de.client.events.NotificationCountUpdateEvent;
 import org.iplantc.de.client.events.NotificationCountUpdateEvent.NotificationCountUpdateEventHandler;
 import org.iplantc.de.client.events.ShowAboutWindowEvent;
 import org.iplantc.de.client.events.ShowSystemMessagesEvent;
 import org.iplantc.de.client.notifications.views.ViewNotificationMenu;
 import org.iplantc.de.client.preferences.views.PreferencesDialog;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Widget;
 import com.sencha.gxt.core.client.XTemplates;
 import com.sencha.gxt.widget.core.client.button.TextButton;
 import com.sencha.gxt.widget.core.client.container.AbstractHtmlLayoutContainer.HtmlData;
 import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
 import com.sencha.gxt.widget.core.client.container.HtmlLayoutContainer;
 import com.sencha.gxt.widget.core.client.container.MarginData;
 import com.sencha.gxt.widget.core.client.container.SimpleContainer;
 import com.sencha.gxt.widget.core.client.event.HideEvent;
 import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
 import com.sencha.gxt.widget.core.client.event.SelectEvent;
 import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
 import com.sencha.gxt.widget.core.client.event.ShowEvent;
 import com.sencha.gxt.widget.core.client.event.ShowEvent.ShowHandler;
 import com.sencha.gxt.widget.core.client.menu.Menu;
 import com.sencha.gxt.widget.core.client.menu.SeparatorMenuItem;
 import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
 
 /**
  * Default DE View as Desktop
  * 
  * FIXME JDS Move more UI construction into ui.xml
  * 
  * @author sriram
  * 
  */
 public class DEViewImpl implements DEView {
 
     private static DEViewUiBinder uiBinder = GWT.create(DEViewUiBinder.class);
 
     @UiField
     SimpleContainer headerPanel;
     @UiField
     SimpleContainer mainPanel;
 
     @UiField
     MarginData centerData;
     @UiField
     BorderLayoutContainer con;
 
     private NotificationIndicator lblNotifications;
     private ViewNotificationMenu notificationsView;
 
     private final Widget widget;
 
     private final DeResources resources;
     private final EventBus eventBus;
 
     private DEView.Presenter presenter;
     private final Desktop desktop;
     private final HeaderTemplate r;
     private final DEHeaderStyle headerResources;
     private final IPlantAnchor sysMsgsMenuItem;
 
     private Menu userMenu;
 
     @UiTemplate("DEView.ui.xml")
     interface DEViewUiBinder extends UiBinder<Widget, DEViewImpl> {
     }
 
     interface HeaderTemplate extends XTemplates {
         @XTemplate(source = "template_de.html")
         public SafeHtml render(DEHeaderStyle style);
     }
 
     public DEViewImpl(final DeResources resources, final EventBus eventBus) {
         this.resources = resources;
         this.eventBus = eventBus;
         widget = uiBinder.createAndBindUi(this);
 
         desktop = new Desktop(resources, eventBus);
         con.remove(con.getCenterWidget());
         con.setCenterWidget(desktop, centerData);
 
         resources.css().ensureInjected();
         con.setStyleName(resources.css().iplantcBackground());
         initEventHandlers();
 
         headerResources = IplantResources.RESOURCES.getHeaderStyle();
         headerResources.ensureInjected();
         r = GWT.create(HeaderTemplate.class);
 
         sysMsgsMenuItem = new IPlantAnchor(I18N.DISPLAY.systemMessagesLabel(), -1, new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
                 EventBus.getInstance().fireEvent(new ShowSystemMessagesEvent());
                 userMenu.hide();
             }
         });
     }
 
     @Override
     public Widget asWidget() {
         return widget;
     }
 
     private void initEventHandlers() {
         EventBus eventbus = EventBus.getInstance();
 
         // handle data events
         eventbus.addHandler(NotificationCountUpdateEvent.TYPE,
                 new NotificationCountUpdateEventHandler() {
 
                     @Override
                     public void onCountUpdate(NotificationCountUpdateEvent ncue) {
                         int new_count = ncue.getTotal();
                         if (new_count > 0 && new_count > lblNotifications.getCount()) {
                             notificationsView.fetchUnseenNotifications();
                         }
                         notificationsView.setUnseenCount(new_count);
                         lblNotifications.setCount(new_count);
 
                     }
                 });
     }
 
     @Override
     public void drawHeader() {
         HtmlLayoutContainer c = new HtmlLayoutContainer(r.render(headerResources));
         headerPanel.setWidget(c);
         c.add(buildHtmlActionsPanel(), new HtmlData(".menu_container"));
     }
 
     private HtmlLayoutContainer buildHtmlActionsPanel() {
         HtmlLayoutContainerTemplate templates = GWT.create(HtmlLayoutContainerTemplate.class);
 
         HtmlLayoutContainer c = new HtmlLayoutContainer(templates.getTemplate());
         c.add(buildNotificationMenu(I18N.DISPLAY.notifications()), new HtmlData(".cell1"));
         c.add(buildActionsMenu(), new HtmlData(".cell2"));
         ToolBar helpbar = buildHelpMenu();
         c.add(helpbar, new HtmlData(".cell5"));
         c.add(new HTML("&nbsp;&nbsp;"), new HtmlData(".cell3"));
         c.add(new HTML("&nbsp;&nbsp;"), new HtmlData(".cell4"));
         return c;
 
     }
 
     private ToolBar buildHelpMenu() {
         TextButton help = new TextButton();
         help.setId("idForumMenuItem");
         help.addSelectHandler(new SelectHandler() {
 
             @Override
             public void onSelect(SelectEvent event) {
                 WindowUtil.open(Constants.CLIENT.forumsUrl());
             }
         });
         help.setToolTip(I18N.DISPLAY.help());
         help.setIcon(IplantResources.RESOURCES.help());
         ToolBar helpbar = new ToolBar();
        helpbar.setPixelSize(40, 30);
         helpbar.add(help);
         return helpbar;
     }
 
     public interface HtmlLayoutContainerTemplate extends XTemplates {
         @XTemplate("<table width=\"100%\" height=\"100%\"><tbody><tr><td height=\"100%\" class=\"cell1\"/><td class=\"cell3\"/><td class=\"cell2\"/><td class=\"cell4\"/><td class=\"cell5\"/></tr></tbody></table>")
         SafeHtml getTemplate();
     }
 
     private ToolBar buildNotificationMenu(String menuHeaderText) {
         lblNotifications = new NotificationIndicator(0);
         lblNotifications.ensureDebugId("lblNotifyCnt");
 
         final TextButton button = new TextButton(menuHeaderText);
         button.setHeight(18);
         button.ensureDebugId("id" + menuHeaderText);
         notificationsView = new ViewNotificationMenu(eventBus);
         notificationsView.setStyleName(resources.css().de_header_menu_body());
         notificationsView.addShowHandler(new ShowHandler() {
 
             @Override
             public void onShow(ShowEvent event) {
                 notificationsView.addStyleName(resources.css().de_header_menu());
             }
         });
         notificationsView.addHideHandler(new HideHandler() {
             @Override
             public void onHide(HideEvent event) {
                 notificationsView.removeStyleName(resources.css().de_header_menu());
             }
         });
         button.setMenu(notificationsView);
         button.getElement().setAttribute("data-intro",
                 org.iplantc.core.resources.client.messages.I18N.TOUR.introNotifications());
         button.getElement().setAttribute("data-position", "left");
         button.getElement().setAttribute("data-step", "4");
         ToolBar bar = new ToolBar();
         bar.setPixelSize(120, 30);
         bar.add(button);
         bar.add(lblNotifications);
         return bar;
     }
 
     private ToolBar buildActionsMenu() {
         final TextButton button = new TextButton();
         button.setHeight(18);
         button.setIcon(IplantResources.RESOURCES.userMenu());
         button.ensureDebugId("id" + I18N.DISPLAY.settings());
         final Menu menu = buildUserMenu();
         button.setMenu(menu);
         menu.addShowHandler(new ShowHandler() {
 
             @Override
             public void onShow(ShowEvent event) {
                 menu.addStyleName(resources.css().de_header_menu());
 
             }
         });
         menu.addHideHandler(new HideHandler() {
             @Override
             public void onHide(HideEvent event) {
                 menu.removeStyleName(resources.css().de_header_menu());
             }
         });
 
         button.getElement().setAttribute("data-intro",
                 org.iplantc.core.resources.client.messages.I18N.TOUR.introSettings());
         button.getElement().setAttribute("data-position", "left");
         button.getElement().setAttribute("data-step", "5");
 
         ToolBar bar = new ToolBar();
         bar.setPixelSize(50, 30);
         bar.add(button);
         return bar;
     }
 
     private Menu buildUserMenu() {
         userMenu = buildMenu();
 
         userMenu.add(buildPrefMenuItem());
         userMenu.add(buildCollabMenuItem());
 
         userMenu.add(sysMsgsMenuItem);
 
         userMenu.add(new SeparatorMenuItem());
 
         userMenu.add(buildHelpMenuItem());
         userMenu.add(buildIntroMenuItem());
         userMenu.add(buildContactMenuItem());
         userMenu.add(buildAboutMenuItem());
 
         userMenu.add(new SeparatorMenuItem());
 
         userMenu.add(buildLogoutMenuItem());
 
         return userMenu;
     }
 
     private IPlantAnchor buildLogoutMenuItem() {
         IPlantAnchor anchor = new IPlantAnchor(I18N.DISPLAY.logout(), -1, new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
                 // doLogout();
                 presenter.doLogout();
                 userMenu.hide();
             }
         });
 
         anchor.setId("idLogoutMenuItem");
         return anchor;
     }
 
     private IPlantAnchor buildAboutMenuItem() {
         IPlantAnchor anchor = new IPlantAnchor(I18N.DISPLAY.about(), -1, new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
                 // displayAboutDe();
                 EventBus.getInstance().fireEvent(new ShowAboutWindowEvent());
                 userMenu.hide();
             }
         });
         anchor.setId("idAboutMenuItem");
         return anchor;
     }
 
     private IPlantAnchor buildContactMenuItem() {
         IPlantAnchor anchor = new IPlantAnchor(I18N.DISPLAY.contactSupport(), -1, new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
                 WindowUtil.open(Constants.CLIENT.supportUrl());
                 userMenu.hide();
             }
         });
         anchor.setId("idSupportMenuItem");
         return anchor;
     }
 
     private IPlantAnchor buildIntroMenuItem() {
         IPlantAnchor anchor = new IPlantAnchor(I18N.DISPLAY.introduction(), -1, new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
                 presenter.doWelcomeIntro();
                 userMenu.hide();
             }
         });
         anchor.setId("idIntroMenuItem");
         return anchor;
     }
 
     private IPlantAnchor buildHelpMenuItem() {
         IPlantAnchor anchor = new IPlantAnchor(I18N.DISPLAY.documentation(), -1, new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
                 WindowUtil.open(Constants.CLIENT.deHelpFile());
                 userMenu.hide();
             }
         });
         anchor.setId("idDocMenuItem");
         return anchor;
     }
 
     private IPlantAnchor buildCollabMenuItem() {
         IPlantAnchor anchor = new IPlantAnchor(I18N.DISPLAY.collaborators(), -1, new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
                 ManageCollaboratorsDailog dialog = new ManageCollaboratorsDailog(MODE.MANAGE);
                 dialog.show();
                 userMenu.hide();
             }
         });
         anchor.setId("idCollabMenuItem");
         return anchor;
     }
 
     private IPlantAnchor buildPrefMenuItem() {
         IPlantAnchor anchor = new IPlantAnchor(I18N.DISPLAY.preferences(), -1, new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
                 buildAndShowPreferencesDialog();
                 userMenu.hide();
             }
 
         });
         anchor.setId("idPrefMenuItem");
         return anchor;
     }
 
     private void buildAndShowPreferencesDialog() {
         PreferencesDialog d = new PreferencesDialog();
         d.show();
     }
 
     private Menu buildMenu() {
         Menu d = new Menu();
         d.setStyleName(resources.css().de_header_menu_body());
         return d;
     }
 
     @Override
     public void setPresenter(DEView.Presenter presenter) {
         this.presenter = presenter;
     }
 
     /**
      * A Label with a setCount method that can set the label's styled text to the count when it's greater
      * than 0, or setting empty text and removing the style for a count of 0 or less.
      * 
      * @author psarando
      * 
      */
     private class NotificationIndicator extends HTML {
 
         int count;
 
         public NotificationIndicator(int initialCount) {
             super();
             setWidth("18px");
             setStyleName(resources.css().de_notification_indicator());
             setCount(initialCount);
         }
 
         public int getCount() {
             return count;
         }
 
         public void setCount(int count) {
             this.count = count;
             if (count > 0) {
                 setText(String.valueOf(count));
                 addStyleName(resources.css().de_notification_indicator_highlight());
                 Window.setTitle("(" + count + ") " + I18N.DISPLAY.rootApplicationTitle());
             } else {
                 setHTML(SafeHtmlUtils.fromSafeConstant("&nbsp;&nbsp;"));
                 removeStyleName(resources.css().de_notification_indicator_highlight());
                 Window.setTitle(I18N.DISPLAY.rootApplicationTitle());
             }
         }
     }
 
     @Override
     public List<WindowState> getOrderedWindowStates() {
         return desktop.getOrderedWindowStates();
     }
 
     @Override
     public void restoreWindows(List<WindowState> windowStates) {
         for (WindowState ws : windowStates) {
             desktop.restoreWindow(ws);
         }
     }
 
     /**
      * @see DEView#updateUnseenSystemMessageCount(long)
      */
     @Override
     public void updateUnseenSystemMessageCount(final long numUnseenSysMsgs) {
         String lbl = I18N.DISPLAY.systemMessagesLabel();
         if (numUnseenSysMsgs > 0) {
             lbl += " (" + numUnseenSysMsgs + ")";
         }
         sysMsgsMenuItem.setText(lbl);
     }
 
     @Override
     public Desktop getDesktop() {
         return desktop;
     }
 
 }
