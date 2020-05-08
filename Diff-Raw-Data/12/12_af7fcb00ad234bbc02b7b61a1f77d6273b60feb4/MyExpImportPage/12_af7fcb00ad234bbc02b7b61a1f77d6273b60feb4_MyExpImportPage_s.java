 /**
  * 
  */
 package pl.psnc.dl.wf4ever.myexpimport.pages;
 
 import org.apache.wicket.request.http.handler.RedirectRequestHandler;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.scribe.oauth.OAuthService;
 
 import pl.psnc.dl.wf4ever.myexpimport.model.ImportModel;
 import pl.psnc.dl.wf4ever.myexpimport.model.myexp.User;
 import pl.psnc.dl.wf4ever.myexpimport.services.MyExpApi;
 import pl.psnc.dl.wf4ever.myexpimport.utils.WicketUtils;
 import pl.psnc.dl.wf4ever.myexpimport.wizard.ImportWizard;
 
 /**
  * @author Piotr Hołubowicz
  *
  */
 public class MyExpImportPage
 	extends TemplatePage
 {
 
 	private static final long serialVersionUID = 4637256013660809942L;
 
 
 	public MyExpImportPage(PageParameters pageParameters)
 	{
 		super(pageParameters);
 		if (willBeRedirected)
 			return;
 
 		OAuthService service = MyExpApi.getOAuthService(
			WicketUtils.getCompleteUrl(this, MyExpImportPage.class),
			getMyExpConsumerKey(), getMyExpConsumerSecret());
 
 		try {
 			User myExpUser = retrieveMyExpUser(getMyExpAccessToken(), service);
 			ImportModel model = new ImportModel(myExpUser);
 			content.add(new ImportWizard("wizard", model));
 		}
 		catch (Exception e) {
 			String page = urlFor(ErrorPage.class, null).toString()
 					+ "?message=" + e.getMessage();
 			getRequestCycle().scheduleRequestHandlerAfterCurrent(
 				new RedirectRequestHandler(page));
 			content.setVisible(false);
 			return;
 		}
 	}
 
 }
