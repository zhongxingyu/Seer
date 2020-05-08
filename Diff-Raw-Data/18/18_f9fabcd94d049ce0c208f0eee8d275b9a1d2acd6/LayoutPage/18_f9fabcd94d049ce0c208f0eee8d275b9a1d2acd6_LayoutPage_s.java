 package com.lyndir.lhunath.snaplog.webapp.page;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import com.lyndir.lhunath.lib.system.localization.UseKey;
 import com.lyndir.lhunath.lib.system.logging.Logger;
 import com.lyndir.lhunath.lib.wayward.behavior.CSSClassAttributeAppender;
 import com.lyndir.lhunath.lib.wayward.component.AjaxLabelLink;
 import com.lyndir.lhunath.lib.wayward.component.GenericWebPage;
 import com.lyndir.lhunath.lib.wayward.component.LabelLink;
 import com.lyndir.lhunath.lib.wayward.i18n.KeyAppender;
 import com.lyndir.lhunath.lib.wayward.i18n.KeyMatch;
 import com.lyndir.lhunath.snaplog.webapp.SnaplogSession;
 import com.lyndir.lhunath.snaplog.webapp.page.model.LayoutPageModels;
 import com.lyndir.lhunath.snaplog.webapp.page.model.LayoutPageModels.TabItem;
 import com.lyndir.lhunath.snaplog.webapp.page.util.LayoutPageUtils;
 import net.link.safeonline.wicket.component.linkid.LinkIDLoginLink;
 import org.apache.wicket.Component;
 import org.apache.wicket.Page;
 import org.apache.wicket.RestartResponseException;
 import org.apache.wicket.Session;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.behavior.StringHeaderContributor;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.MarkupStream;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 
 
 /**
  * <h2>{@link LayoutPage}<br>
  * <sub>[in short] (TODO).</sub></h2>
  *
  * <p>
  * <i>Jan 28, 2010</i>
  * </p>
  *
  * @author lhunath
  */
 public class LayoutPage extends GenericWebPage<LayoutPageModels> {
 
     protected final Logger logger = Logger.get( getClass() );
 
     private static final String TOOLS_PANEL = "toolsPanel";
     private static final String CONTENT_PANEL = "contentPanel";
 
     final WebMarkupContainer userEntry;
     final WebMarkupContainer userSummary;
     final WebMarkupContainer tabsContainer;
     final WebMarkupContainer toolbar;
     WebMarkupContainer tools;
     final WebMarkupContainer container;
 
 
     /**
      * Create a new {@link LayoutPage}.
      */
     public LayoutPage() {
 
         super( new LayoutPageModels().getModel() );
         getModelObject().attach( this );
 
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
 
                TabItem itemModel = item.getModelObject();
 
                 item.add( new AjaxLabelLink( "link", itemModel.title() ) {
 
                     @Override
                     public void onClick(final AjaxRequestTarget target) {
 
                         // TAB click.
                         LayoutPageUtils.setActiveTab( itemModel.getObject(), null );
                     }
                 } );
                 item.add( CSSClassAttributeAppender.ofString( item.getModelObject().styleClass() ) );
                 item.setVisible( itemModel.getObject().get().isVisible() );
             }
         };
         tabsContainer.add( headTabs );
         tabsContainer.setOutputMarkupId( true );
 
         // Toolbar.
         add( toolbar = new WebMarkupContainer( "toolbar" ) {
 
             final IModel<Component> activeTools = new LoadableDetachableModel<Component>() {
 
                 @Override
                 protected Component load() {
 
                     Component toolsPanel = LayoutPageUtils.getActiveTab().get().getTools( TOOLS_PANEL );
                     if (toolsPanel == null)
                         toolsPanel = new WebMarkupContainer( TOOLS_PANEL ).setVisible( false );
 
                     return toolsPanel;
                 }
             };
 
             {
                 add( new Label( "focussedUser", getModelObject().focussedUser() ) );
                 add( new Label( "focussedContent", getModelObject().focussedContent() ) );
             }
 
 
             @Override
             protected void onBeforeRender() {
 
                 addOrReplace( activeTools.getObject() );
 
                 super.onBeforeRender();
             }
 
             @Override
             public boolean isVisible() {
 
                 return activeTools.getObject().isVisible() || getModelObject().focussedUser().getObject() != null;
             }
         } );
 
         // Page Content.
         add( (container = new WebMarkupContainer( "container" ) {
 
             @Override
             protected void onBeforeRender() {
 
                 Panel activeContent = SnaplogSession.get().getActiveContent();
                 if (activeContent == null)
                     activeContent = LayoutPageUtils.getActiveTab().get().getPanel( CONTENT_PANEL );
                 addOrReplace( activeContent );
 
                 add( new StringHeaderContributor( LayoutPageUtils.trackJS( activeContent ) ) );
 
                 super.onBeforeRender();
             }
         }).setOutputMarkupId( true ) );
         SnaplogSession.get().setActiveContent( getInitialContentPanel( CONTENT_PANEL ) );
 
         add( pageTitle, userEntry, userSummary, tabsContainer );
     }
 
     /**
      * Override me to define a custom panel to show initially when this page is constructed.
      *
      * @param wicketId The wicket ID that the panel should use.
      *
      * @return The panel to show when the page first loads.
      */
     protected Panel getInitialContentPanel(@SuppressWarnings("unused") final String wicketId) {
 
         return null;
     }
 
     /**
      * Reload the page using the given target.
      *
      * @param target The target that will be servicing the reload response.
      */
     public void reloadFor(final AjaxRequestTarget target) {
 
         checkNotNull( target, "Can't reload without a target." );
 
         target.addComponent( tabsContainer );
         target.addComponent( container );
     }
 
 
     /**
      * <h2>{@link Messages}<br>
      * <sub>[in short] (TODO).</sub></h2>
      *
      * <p>
      * <i>Mar 31, 2010</i>
      * </p>
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
         @UseKey
         String pageTitle(char albumOwnerBadge, String albumOwnerName);
 
         /**
          * @param userBadge The badge character of the logged-in user.
          * @param userName  The name of the logged-in user.
          *
          * @return Welcoming text greeting the logged-in user.
          */
         @UseKey
         String userWelcome(char userBadge, String userName);
 
         /**
          * @param userBadge The badge of the user we guess is using the page.
          * @param userName  The name of the user we guess is using the page.
          *
          * @return Welcoming the user back. The user has not yet authenticated himself. The identification is just a
          *         guess.
          */
         @UseKey
         String userWelcomeBack(char userBadge, String userName);
 
         /**
          * @return The designation of a user who we can't identify.
          */
         @UseKey
         String userNameUnknown();
 
         /**
          * @param messageCount The amount of messages the user has.
          *
          * @return Text indicating the user has messages.
          */
         @UseKey
         String userMessages(
                 @KeyAppender(value = @KeyMatch(ifNum = 1, key = "singular", elseKey = "plural"), useValue = true)//
                         int messageCount);
 
         /**
          * @param requestCount The amount of pending requests.
          *
          * @return Text indicating there are pending requests for the active user.
          */
         @UseKey
         String userRequests(
                 @KeyAppender(value = @KeyMatch(ifNum = 1, key = "singular", elseKey = "plural"), useValue = true)//
                         int requestCount);
 
         /**
          * @param userBadge The focussed user's badge.
          * @param userName  The focussed user's userName.
          *
          * @return A text indicating that the given user is the one currently focusing on.
          */
         String focussedUser(char userBadge, String userName);
 
         /**
          * @param albumName The name of the album that's being focussed on.
          *
          * @return A text indicating what the user's currently focusing on.
          */
         // TODO: If we want to allow focusing other content; this may need improvement. If not, this may be simplified?
         String focussedContent(
                 @KeyAppender(nullKey = "none", notNullKey = "album", useValue = true)//
                         String albumName);
     }
 }
