 package pl.psnc.dl.wf4ever.portal.pages;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.RestartResponseException;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 
 import pl.psnc.dl.wf4ever.portal.MySession;
 
 public class TemplatePage
 	extends WebPage
 {
 
 	private static final long serialVersionUID = 1L;
 
 	@SuppressWarnings("unused")
 	private static final Logger log = Logger.getLogger(TemplatePage.class);
 
 
 	public TemplatePage(final PageParameters parameters)
 	{
 		getSession().bind();
 		MySession.get().persist();
 		final WebMarkupContainer redirect = new WebMarkupContainer("redirect");
 		String redirectionURL = parameters.get("redirectTo").toString();
 		if (redirectionURL != null) {
			int redirectionDelay = parameters.get("redirectDelay").toInt(3);
			final String content = "" + redirectionDelay + ";URL=" + redirectionURL;
 			redirect.add(new AttributeModifier("content", new Model<String>(content)));
			parameters.remove("redirectTo");
			parameters.remove("redirectDelay");
 		}
 		else {
 			redirect.setVisible(false);
 		}
 		add(redirect);
 
 		add(new BookmarkablePageLink<Void>("baner", HomePage.class));
 		add(new BookmarkablePageLink<Void>("banerTitle", HomePage.class));
 
 		add(new BookmarkablePageLink<Void>("menu-home", HomePage.class));
 		add(new BookmarkablePageLink<Void>("menu-myros", MyRosPage.class));
 		add(new BookmarkablePageLink<Void>("menu-sparql", SparqlEndpointPage.class));
 
 		WebMarkupContainer signedInAs = new WebMarkupContainer("signedInAs");
 		signedInAs.add(new AttributeModifier("data-original-title", new PropertyModel<String>(this, "signInTwipsy")));
 		add(signedInAs);
 		signedInAs.add(new AjaxFallbackLink<String>("signIn") {
 
 			private static final long serialVersionUID = -4458301162412620530L;
 
 
 			@Override
 			public void onClick(AjaxRequestTarget target)
 			{
 				if (MySession.get().isSignedIn()) {
 					MySession.get().signOut();
 					throw new RestartResponseException(getApplication().getHomePage());
 				}
 				else {
 					throw new RestartResponseException(AuthenticatePage.class);
 				}
 			}
 
 		}.add(new Label("signInText", new PropertyModel<String>(this, "signInButtonText"))));
 	}
 
 
 	public String getSignInButtonText()
 	{
 		if (MySession.get().isSignedIn()) {
 			return "Sign out";
 		}
 		else {
 			return "Sign in";
 		}
 	}
 
 
 	public String getSignInTwipsy()
 	{
 		if (MySession.get().isSignedIn()) {
 			return "Signed in as " + MySession.get().getUsername();
 		}
 		else {
 			return "Click to sign in!";
 		}
 	}
 }
