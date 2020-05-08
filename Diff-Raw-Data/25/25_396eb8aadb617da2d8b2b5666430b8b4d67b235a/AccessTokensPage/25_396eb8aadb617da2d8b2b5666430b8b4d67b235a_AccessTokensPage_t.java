 package pl.psnc.dl.wf4ever.portal.pages.users;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.purl.wf4ever.rosrs.client.common.users.AccessToken;
 import org.purl.wf4ever.rosrs.client.common.users.UserManagementService;
 
 import pl.psnc.dl.wf4ever.portal.MySession;
 import pl.psnc.dl.wf4ever.portal.PortalApplication;
 import pl.psnc.dl.wf4ever.portal.model.User;
 import pl.psnc.dl.wf4ever.portal.pages.TemplatePage;
 import pl.psnc.dl.wf4ever.portal.pages.util.MyFeedbackPanel;
 
 /**
  * This page presents a list of access tokens of the current user.
  * 
  * @author Piotr Hołubowicz
  * 
  */
 @AuthorizeInstantiation("USER")
 public class AccessTokensPage extends TemplatePage {
 
     /** id. */
     private static final long serialVersionUID = 1L;
 
     /** Logger. */
     private static final Logger LOG = Logger.getLogger(AccessTokensPage.class);
 
     /** List view of all user's access tokens. */
     private ListView<AccessToken> list;
 
     /** List of all user's access tokens. */
     private List<AccessToken> accessTokens = null;
 
 
     /**
      * Constructor.
      * 
      * @param pageParameters
      *            page parameters
      */
     @SuppressWarnings("serial")
     public AccessTokensPage(PageParameters pageParameters) {
         super(pageParameters);
 
         final MyFeedbackPanel feedbackPanel = new MyFeedbackPanel("feedbackPanel");
         feedbackPanel.setOutputMarkupId(true);
         add(feedbackPanel);
 
         add(new BookmarkablePageLink<Void>("profile", ProfilePage.class));
         add(new BookmarkablePageLink<Void>("tokens", AccessTokensPage.class));
         add(new BookmarkablePageLink<Void>("generate", GenerateAccessTokenPage.class));
 
         final User user = ((MySession) getSession()).getUser();
 
         PortalApplication app = ((PortalApplication) getApplication());
         accessTokens = UserManagementService.getAccessTokens(app.getRodlURI(), app.getAdminToken(), user.getURI()
                 .toString());
 
         final Form<?> form = new Form<Void>("form");
         add(form);
         list = new ListView<AccessToken>("tokensListView", new PropertyModel<List<AccessToken>>(this, "accessTokens")) {
 
             protected void populateItem(ListItem<AccessToken> item) {
                 final AccessToken token = (AccessToken) item.getModelObject();
                 item.add(new Label("clientName", token.getClient().getName()));
                 item.add(new Label("created", token.getCreated() != null ? token.getCreated().toString() : "--"));
                 item.add(new Label("lastUsed", token.getLastUsed() != null ? token.getLastUsed().toString() : "--"));
                 item.add(new AjaxButton("revoke", form) {
 
                     @Override
                     protected void onError(AjaxRequestTarget arg0, Form<?> arg1) {
                         // TODO Auto-generated method stub
 
                     }
 
 
                     @Override
                     protected void onSubmit(AjaxRequestTarget target, Form<?> arg1) {
                         PortalApplication app = ((PortalApplication) getApplication());
                         try {
                             UserManagementService.deleteAccessToken(app.getRodlURI(), app.getAdminToken(),
                                 token.getToken());
                             accessTokens.remove(token);
                             target.add(form);
                         } catch (Exception e) {
                             LOG.error(e.getMessage());
                             error(e.getMessage());
                         }
                     }
                 });
             }
         };
         list.setOutputMarkupId(true);
         form.add(list);
     }
 
 
     public List<AccessToken> getAccessTokens() {
         return accessTokens;
     }
 
 
     public void setAccessTokens(List<AccessToken> accessTokens) {
         this.accessTokens = accessTokens;
     }
 }
