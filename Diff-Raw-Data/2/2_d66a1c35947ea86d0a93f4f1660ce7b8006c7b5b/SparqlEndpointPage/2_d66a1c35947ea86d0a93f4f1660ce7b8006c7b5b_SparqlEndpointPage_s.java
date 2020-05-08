 package pl.psnc.dl.wf4ever.portal.pages;
 
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.Page;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextArea;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.protocol.http.RequestUtils;
 import org.apache.wicket.request.Url;
 import org.apache.wicket.request.cycle.RequestCycle;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 
 import pl.psnc.dl.wf4ever.portal.PortalApplication;
 import pl.psnc.dl.wf4ever.portal.pages.util.MyAjaxButton;
 import pl.psnc.dl.wf4ever.portal.pages.util.MyComponentFeedbackPanel;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 
 public class SparqlEndpointPage
 	extends TemplatePage
 {
 
 	private static final long serialVersionUID = 1L;
 
 	private String query;
 
 	private String url;
 
 	private String result;
 
 
 	@SuppressWarnings("serial")
 	public SparqlEndpointPage(final PageParameters parameters)
 	{
 		super(parameters);
 		add(new MyFeedbackPanel("feedbackPanel"));
 		Form< ? > form = new Form<Void>("form");
 		add(form);
 
 		final TextArea<String> queryTA = new TextArea<String>("query", new PropertyModel<String>(this, "query")) {
 
 			@Override
 			protected void onValid()
 			{
 				add(AttributeModifier.remove("style"));
 			};
 
 
 			@Override
 			protected void onInvalid()
 			{
 				add(AttributeModifier.replace("style", "border-color: #EE5F5B"));
 			};
 		};
 		queryTA.setRequired(true);
 		queryTA.setOutputMarkupId(true);
 		form.add(queryTA);
 		final Panel queryFeedback = new MyComponentFeedbackPanel("queryFeedback", queryTA);
 		queryFeedback.setOutputMarkupId(true);
 		form.add(queryFeedback);
 		final Label urlLabel = new Label("url", new PropertyModel<String>(this, "url"));
 		urlLabel.setOutputMarkupId(true);
 		form.add(urlLabel);
 		final Label resultLabel = new Label("result", new PropertyModel<String>(this, "result"));
 		resultLabel.setOutputMarkupId(true);
 		form.add(resultLabel);
 
 		form.add(new MyAjaxButton("execute", form) {
 
 			@Override
 			protected void onSubmit(AjaxRequestTarget target, Form< ? > form)
 			{
 				super.onSubmit(target, form);
 
 				Client client = Client.create();
 				try {
 					WebResource webResource = client.resource(getEndpointUrl().toString());
					String response = webResource.header("Content-type", "application/x-turtle").get(String.class);
 					setResult(response);
 				}
 				catch (Exception e) {
 					error(e.getMessage());
 				}
 				target.add(queryTA);
 				target.add(queryFeedback);
 				target.add(resultLabel);
 			}
 
 
 			@Override
 			protected void onError(AjaxRequestTarget target, Form< ? > form)
 			{
 				super.onError(target, form);
 				target.add(queryTA);
 				target.add(queryFeedback);
 			}
 		});
 
 		form.add(new MyAjaxButton("generateURL", form) {
 
 			@Override
 			protected void onSubmit(AjaxRequestTarget target, Form< ? > form)
 			{
 				super.onSubmit(target, form);
 				try {
 					setUrl(getEndpointUrl().toString());
 				}
 				catch (MalformedURLException | URISyntaxException e) {
 					error(e.getMessage());
 				}
 				target.add(queryTA);
 				target.add(queryFeedback);
 				target.add(urlLabel);
 			}
 
 
 			@Override
 			protected void onError(AjaxRequestTarget target, Form< ? > form)
 			{
 				super.onError(target, form);
 				target.add(queryTA);
 				target.add(queryFeedback);
 				target.add(urlLabel);
 			}
 		});
 	}
 
 
 	protected URL getEndpointUrl()
 		throws URISyntaxException, MalformedURLException
 	{
 		URI uri = ((PortalApplication) getApplication()).getSparqlEndpointURL().toURI();
 		return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), "query=" + query, uri.getFragment()).toURL();
 	}
 
 
 	/**
 	 * Get an absoulte URL for a Page and parameters. E.g.
 	 * http://localhost/wicket/Page?param1=value
 	 * 
 	 * @param pageClass
 	 *            Page Class
 	 * @param parameters
 	 *            Params
 	 * @param <C>
 	 *            Page Class
 	 * @return Absolute Url
 	 */
 	public static <C extends Page> String getAbsoluteUrl(final Class<C> pageClass, final PageParameters parameters)
 	{
 
 		CharSequence resetUrl = RequestCycle.get().urlFor(pageClass, parameters);
 		String abs = RequestUtils.toAbsolutePath("/", resetUrl.toString());
 		final Url url = Url.parse(abs);
 		return RequestCycle.get().getUrlRenderer().renderFullUrl(url);
 	}
 
 
 	/**
 	 * @return the query
 	 */
 	public String getQuery()
 	{
 		return query;
 	}
 
 
 	/**
 	 * @param query
 	 *            the query to set
 	 */
 	public void setQuery(String query)
 	{
 		this.query = query;
 	}
 
 
 	/**
 	 * @return the result
 	 */
 	public String getResult()
 	{
 		return result;
 	}
 
 
 	/**
 	 * @param result
 	 *            the result to set
 	 */
 	public void setResult(String result)
 	{
 		this.result = result;
 	}
 
 
 	/**
 	 * @return the url
 	 */
 	public String getUrl()
 	{
 		return url;
 	}
 
 
 	/**
 	 * @param url
 	 *            the url to set
 	 */
 	public void setUrl(String url)
 	{
 		this.url = url;
 	}
 }
