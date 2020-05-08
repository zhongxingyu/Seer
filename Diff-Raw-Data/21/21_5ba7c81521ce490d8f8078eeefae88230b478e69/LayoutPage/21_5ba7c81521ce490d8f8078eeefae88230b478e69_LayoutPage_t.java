 package com.lyndir.lhunath.snaplog.webapp.page;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Splitter;
 import com.google.common.collect.Iterables;
 import com.lyndir.lhunath.lib.system.logging.Logger;
 import com.lyndir.lhunath.lib.wayward.behavior.CSSClassAttributeAppender;
 import com.lyndir.lhunath.lib.wayward.behavior.JSLink;
 import com.lyndir.lhunath.lib.wayward.component.AjaxLabelLink;
 import com.lyndir.lhunath.lib.wayward.component.GenericWebPage;
 import com.lyndir.lhunath.lib.wayward.component.LabelLink;
 import com.lyndir.lhunath.lib.wayward.i18n.KeyAppender;
 import com.lyndir.lhunath.lib.wayward.i18n.KeyMatch;
 import com.lyndir.lhunath.lib.wayward.js.AjaxHooks;
 import com.lyndir.lhunath.lib.wayward.js.JSUtils;
 import com.lyndir.lhunath.snaplog.webapp.SnaplogSession;
 import com.lyndir.lhunath.snaplog.webapp.page.model.LayoutPageModels;
 import com.lyndir.lhunath.snaplog.webapp.page.model.LayoutPageModels.TabItem;
 import com.lyndir.lhunath.snaplog.webapp.tab.SnaplogTab;
 import com.lyndir.lhunath.snaplog.webapp.tab.Tab;
 import com.lyndir.lhunath.snaplog.webapp.tool.SnaplogTool;
 import java.net.URI;
 import java.util.HashMap;
 import java.util.List;
import java.util.Map;
 import net.link.safeonline.wicket.component.linkid.LinkIDLoginLink;
 import org.apache.wicket.*;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.feedback.FeedbackMessage;
 import org.apache.wicket.feedback.IFeedbackMessageFilter;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.MarkupStream;
 import org.apache.wicket.markup.html.WebComponent;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.LoadableDetachableModel;
 
 
 /**
  * <h2>{@link LayoutPage}<br> <sub>[in short] (TODO).</sub></h2>
  *
  * <p> <i>Jan 28, 2010</i> </p>
  *
  * @author lhunath
  */
 public class LayoutPage extends GenericWebPage<LayoutPageModels> {
 
     protected final Logger logger = Logger.get( getClass() );
 
     private static final String CONTENT_PANEL = "contentPanel";
 
     final WebMarkupContainer userEntry;
     final WebMarkupContainer userSummary;
     final WebMarkupContainer tabsContainer;
     final WebMarkupContainer contentContainer;
     final WebMarkupContainer messages;
 
     final HashMap<SnaplogTool, Panel> toolPanels;
     final LoadableDetachableModel<List<? extends SnaplogTool>> tools;
 
     /**
      * Create a new {@link LayoutPage}.
      */
     public LayoutPage() {
 
         super( new LayoutPageModels().getModel() );
         getModelObject().attach( this );
 
         // Ajax Hooks
         AjaxHooks.installAjaxEvents( this );
         AjaxHooks.installPageEvents( this, new AjaxHooks.IPageListener() {
             @Override
             public void onReady(final AjaxRequestTarget target, final String pageUrl) {
 
                 URI uri = URI.create( pageUrl );
                 if (uri.getFragment() == null)
                     // No fragment, don't try to load state from it.
                     return;
 
                 Iterable<String> arguments = Splitter.on( '/' ).split( uri.getFragment() );
                 String tabFragment = arguments.iterator().next();
 
                 for (final Tab tab : Tab.values()) {
                     if (tab.get().getFragment().equalsIgnoreCase( tabFragment )) {
                         // Apply tab state from fragment.
                         Panel tabPanel = tab.get().getPanel( CONTENT_PANEL );
                         tab.get().applyFragmentState( tabPanel, Iterables.toArray( arguments, String.class ) );
 
                         setActiveTab( tab, tabPanel, target );
                         break;
                     }
                 }
             }
         } );
 
         // Page Title.
         Label pageTitle = new Label( "pageTitle", getModelObject().pageTitle() );
 
         // User Login.
         userEntry = new WebMarkupContainer( "userEntry" ) {
 
             @Override
             public boolean isVisible() {
 
                 return !SnaplogSession.get().isAuthenticated();
             }
         };
         userEntry.add( new Label( "userGuessWelcome", getModelObject().userGuessWelcome() ) );
         userEntry.add( new LinkIDLoginLink( "userLogin" ) );
 
         // User Summary.
         userSummary = new WebMarkupContainer( "userSummary" ) {
 
             @Override
             public boolean isVisible() {
 
                 return SnaplogSession.get().isAuthenticated();
             }
         };
         userSummary.add( new Label( "userBadge", getModelObject().userBadge() ) );
         userSummary.add( new BookmarkablePageLink<Page>( "userName", Page.class ) {
 
             @Override
             protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
 
                 replaceComponentTagBody( markupStream, openTag, SnaplogSession.get().getActiveUser().getUserName() );
             }
         } );
         userSummary.add( new LabelLink( "userMessages", getModelObject().userMessages() ) {
 
             @Override
             public boolean isVisible() {
 
                 // TODO: return messageCount > 0;
                 return true;
             }
 
             @Override
             public void onClick() {
 
                 // TODO: do something.
             }
         } );
         userSummary.add( new LabelLink( "userRequests", getModelObject().userRequests() ) {
 
             @Override
             public boolean isVisible() {
 
                 // TODO: return requestCount > 0;
                 return true;
             }
 
             @Override
             public void onClick() {
 
                 // TODO: do something.
             }
         } );
         userSummary.add( new Link<Object>( "userLogout" ) {
 
             @Override
             public void onClick() {
 
                 Session.get().invalidate();
                 throw new RestartResponseException( LayoutPage.class );
             }
         } );
 
         // Page Tab.
         tabsContainer = new WebMarkupContainer( "tabsContainer" );
         ListView<TabItem> headTabs = new ListView<TabItem>( "tabs", getModelObject().tabs() ) {
 
             @Override
             protected void populateItem(final ListItem<TabItem> item) {
 
                 final TabItem itemModel = item.getModelObject();
 
                 item.add( new AjaxLabelLink( "link", itemModel.title() ) {
 
                     @Override
                     public void onClick(final AjaxRequestTarget target) {
 
                         setActiveTab( itemModel.getObject(), target );
                     }
                 } );
                 item.add( CSSClassAttributeAppender.ofString( item.getModelObject().styleClass() ) );
                 item.setVisible( itemModel.getObject().get().isVisible() );
             }
         };
         tabsContainer.add( headTabs );
         tabsContainer.setOutputMarkupId( true );
 
         // Toolbar.
         toolPanels = new HashMap<SnaplogTool, Panel>();
         tools = new LoadableDetachableModel<List<? extends SnaplogTool>>() {
 
             @Override
             protected List<? extends SnaplogTool> load() {
 
                 toolPanels.clear();
                 List<? extends SnaplogTool> _tools = getModelObject().activeTab().getObject().get().listTools();
 
                 // Load the panels for the tools and assign them a markup ID.
                 for (final SnaplogTool tool : _tools) {
                     Panel panel = tool.getPanel( "panel" );
                     panel.setOutputMarkupId( true );
                     toolPanels.put( tool, panel );
                 }
 
                 return _tools;
             }
         };
 
         add( new ListView<SnaplogTool>( "tools", tools ) {
 
             @Override
             protected void populateItem(final ListItem<SnaplogTool> item) {
 
                 SnaplogTool tool = item.getModelObject();
                 item.add( new Label( "link", tool.getTitle() ).add( CSSClassAttributeAppender.of( tool.getTitleClass() ) ).add(
                         new JSLink( "popup", toolPanels.get( tool ).getMarkupId(), "toggle" ) ) );
                 item.setVisible( tool.isVisible() );
             }
 
             @Override
             public boolean isVisible() {
 
                 for (final SnaplogTool tool : getList())
                     if (tool.isVisible())
                         return true;
 
                 return false;
             }
         } );
 
         // Global Messages.
         add( messages = new WebMarkupContainer( "messages" ) {
             {
                 setOutputMarkupId( true );
 
                 add( new FeedbackPanel( "errors", new IFeedbackMessageFilter() {
 
                     @Override
                     public boolean accept(final FeedbackMessage message) {
 
                         return message.getLevel() >= FeedbackMessage.WARNING;
                     }
                 } ) {
 
                     @Override
                     public boolean isVisible() {
 
                         return anyMessage();
                     }
                 } );
                 add( new FeedbackPanel( "infos", new IFeedbackMessageFilter() {
 
                     @Override
                     public boolean accept(final FeedbackMessage message) {
 
                         return message.getLevel() <= FeedbackMessage.INFO;
                     }
                 } ) {
                     @Override
                     public boolean isVisible() {
 
                         return anyMessage();
                     }
                 } );
             }} );
 
         // Page Content.
         add( (contentContainer = new WebMarkupContainer( "contentContainer" ) {
 
             {
                 add( new ListView<SnaplogTool>( "toolPanels", tools ) {
                     @Override
                     protected void populateItem(final ListItem<SnaplogTool> item) {
 
                         SnaplogTool tool = item.getModelObject();
                         item.add( toolPanels.get( tool ).setVisible( tool.isVisible() ) );
                     }
                 } );
                 add( getInitialContent( CONTENT_PANEL ) );
             }}).setMarkupId( "content" /* TODO: Wicket should REALLY dig this out of the markup! */ ).setOutputMarkupId( true ) );
 
         add( pageTitle, userEntry, userSummary, tabsContainer );
     }
 
     /**
      * Change the currently active tab on this page.  <b>Note: Only valid when the response page is a LayoutPage.</b>
      *
      * @param tab The tab to activate and display a new panel for.
      */
     public static void setActiveTab(final Tab tab) {
 
         setActiveTab( tab, null );
     }
 
     /**
      * Change the currently active tab on this page.  <b>Note: Only valid when the response page is a LayoutPage.</b>
      *
      * @param tab    The tab to activate and display a new panel for.
      * @param target The AJAX request that we should use to update the page with or <code>null</code> if we aren't responding via AJAX.
      */
     public static void setActiveTab(final Tab tab, final AjaxRequestTarget target) {
 
         Page responsePage = RequestCycle.get().getResponsePage();
         Preconditions.checkState( LayoutPage.class.isInstance( responsePage ),
                                   "Can't change the active tab; response page is not LayoutPage but %s.", responsePage );
 
         LayoutPage layoutPage = (LayoutPage) responsePage;
         layoutPage.setActiveTab( tab, null, target );
     }
 
     /**
      * Change the currently active tab on this page.
      *
      * @param tab      The tab to activate and display the given panel for.
      * @param tabPanel The panel to load for this tab or <code>null</code> if we should make a new panel for this tab.
      * @param target   The AJAX request that we should use to update the page with or <code>null</code> if we aren't responding via AJAX.
      */
     protected void setActiveTab(final Tab tab, final Panel tabPanel, final AjaxRequestTarget target) {
 
         getModelObject().activeTab().setObject( tab );
 
         Panel contentPanel = tabPanel;
         if (contentPanel == null)
             contentPanel = tab.get().getPanel( CONTENT_PANEL );
         contentContainer.addOrReplace( contentPanel );
 
         if (target != null) {
             target.addComponent( tabsContainer );
             target.addComponent( contentContainer );
         }
     }
 
     /**
      * Override me to define a custom panel to show initially when this page is constructed.
      *
      * @param wicketId The wicket ID that the panel should use.
      *
      * @return The panel to show when the page first loads.
      */
     protected Component getInitialContent(final String wicketId) {
 
         return new WebComponent( wicketId );
     }
 
     /**
      * Add components to the AJAX target that should be reloaded during every AJAX event on this page.
      *
      * @param target The AJAX request target to add page components to.
      */
     public void addComponents(final AjaxRequestTarget target) {
 
         checkNotNull( target, "Given target cannot be null." );
 
         target.addComponent( messages );
        target.addListener( new AjaxRequestTarget.IListener() {
            @Override
            public void onBeforeRespond(final Map<String, Component> map, final AjaxRequestTarget target) {

            }
 
            @Override
            public void onAfterRespond(final Map<String, Component> map, final AjaxRequestTarget.IJavascriptResponse response) {

                SnaplogTab<?> activeTab = getModelObject().activeTab().getObject().get();
                Component contentPanel = contentContainer.get( CONTENT_PANEL );
                if (activeTab.getPanelClass().isInstance( contentPanel ))
                    response.addJavascript( "window.location.hash = " + JSUtils.quote( Joiner.on( '/' ).join( activeTab.getFragmentState( (Panel) contentPanel ) ) ) );
            }
        } );
     }
 
     /**
      * <h2>{@link Messages}<br> <sub>[in short] (TODO).</sub></h2>
      *
      * <p> <i>Mar 31, 2010</i> </p>
      *
      * @author lhunath
      */
     public interface Messages {
 
         /**
          * @param albumOwnerBadge The badge character of the owner of the currently viewed album.
          * @param albumOwnerName  The name of the owner of the currently viewed album.
          *
          * @return Text that will go in the page's title.
          */
         String pageTitle(char albumOwnerBadge, String albumOwnerName);
 
         /**
          * @param userBadge The badge character of the logged-in user.
          * @param userName  The name of the logged-in user.
          *
          * @return Welcoming text greeting the logged-in user.
          */
         String userWelcome(char userBadge, String userName);
 
         /**
          * @param userBadge The badge of the user we guess is using the page.
          * @param userName  The name of the user we guess is using the page.
          *
          * @return Welcoming the user back. The user has not yet authenticated himself. The identification is just a guess.
          */
         String userWelcomeBack(char userBadge, String userName);
 
         /**
          * @return The designation of a user who we can't identify.
          */
         String userNameUnknown();
 
         /**
          * @param messageCount The amount of messages the user has.
          *
          * @return Text indicating the user has messages.
          */
         String userMessages(@KeyAppender(value = @KeyMatch(ifNum = 1, key = "singular", elseKey = "plural"), useValue = true)//
                 int messageCount);
 
         /**
          * @param requestCount The amount of pending requests.
          *
          * @return Text indicating there are pending requests for the active user.
          */
         String userRequests(@KeyAppender(value = @KeyMatch(ifNum = 1, key = "singular", elseKey = "plural"), useValue = true)//
                 int requestCount);
 
         /**
          * @param userBadge The focused user's badge.
          * @param userName  The focused user's userName.
          *
          * @return A text indicating that the given user is the one currently focusing on.
          */
         String focusedUser(char userBadge, String userName);
 
         /**
          * @param albumName The name of the album that's being focused on.
          *
          * @return A text indicating what the user's currently focusing on.
          */
         // TODO: If we want to allow focusing other content; this may need improvement. If not, this may be simplified?
         String focusedContent(@KeyAppender(nullKey = "none", notNullKey = "album", useValue = true)//
                 String albumName);
     }
 }
