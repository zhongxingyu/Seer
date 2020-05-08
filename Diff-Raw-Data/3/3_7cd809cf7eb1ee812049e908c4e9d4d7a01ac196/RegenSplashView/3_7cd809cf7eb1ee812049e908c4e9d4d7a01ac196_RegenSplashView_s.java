 package com.redhat.automationportalui.client.pav;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.core.client.JsArrayString;
 import com.google.gwt.dom.client.Style.FontWeight;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.PasswordTextBox;
 import com.google.gwt.user.client.ui.PushButton;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.redhat.automationportalui.client.AutomationPortalUIClientFactory;
 import com.redhat.automationportalui.client.constants.AutomationPortalUIConstants;
 import com.redhat.automationportalui.client.data.AutomationPortalResponseData;
 import com.redhat.automationportalui.client.data.StringList;
 import com.redhat.automationportalui.client.data.StringPair;
 import com.redhat.automationportalui.client.resources.APUI_Errors;
 import com.redhat.automationportalui.client.resources.CommonUIStrings;
 import com.redhat.automationportalui.client.resources.RegenSplashUIStrings;
 import com.redhat.automationportalui.client.template.AutomationPortalUITemplate;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.http.client.URL;
 
 /**
  * A GWT View that represents the RegenSplash UI
  */
 public class RegenSplashView
 {
 	private final static String REST_ENDPOINT = "rest/RegenSplash/get/json/Execute";
 	private final static String SITES_REST_ENDPOINT = "rest/RegenSplash/get/json/Sites";
 	private final static String PRODUCTS_REST_ENDPOINT = "rest/RegenSplash/get/json/Products";
 	private final AutomationPortalUITemplate template;
 	private final RegenSplashUIStrings uiStrings;
 	private final CommonUIStrings commonUiStrings;
 	private TextBox kerberosUsername;
 	private PasswordTextBox kerberosPassword;
 	private TextBox message;
 	private TextArea output;
 	private PushButton go;
 	private ListBox environments;
 	private ListBox products;
 
 	public RegenSplashView(final AutomationPortalUIClientFactory clientFactory, final AutomationPortalUITemplate template, final CommonUIStrings commonUiStrings, final APUI_Errors apuiErrors)
 	{
 		/* Get the translates log messages */
 		uiStrings = (RegenSplashUIStrings) GWT.create(RegenSplashUIStrings.class);
 		this.commonUiStrings = commonUiStrings;
 		this.template = template;
 	}
 
 	public void display()
 	{
 		enableUI(false);
 
 		template.getSubTitle().setText(commonUiStrings.RegenSplash());
 
 		final VerticalPanel topLevelPanel = new VerticalPanel();
 
 		topLevelPanel.add(new HTML(uiStrings.Description()));
 
 		final HTML descriptionLineTwo = new HTML(uiStrings.DescriptionLineTwo());
 		topLevelPanel.add(descriptionLineTwo);

		final HTML descriptionLineThree = new HTML(uiStrings.DescriptionLineThree());
		topLevelPanel.add(descriptionLineThree);
 		
 		final HTML pleaseBePatient = new HTML(commonUiStrings.PleaseBePatient());
 		pleaseBePatient.getElement().getStyle().setMarginBottom(2, Unit.EM);
 		pleaseBePatient.getElement().getStyle().setColor("red");
 		topLevelPanel.add(pleaseBePatient);
 
 		final Grid grid = new Grid(9, 2);
 		grid.getElement().getStyle().setMarginBottom(1, Unit.EM);
 		topLevelPanel.add(grid);
 
 		final HTML optionsLabel = new HTML(commonUiStrings.Options());
 		optionsLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
 		grid.setWidget(0, 0, optionsLabel);
 		
 		kerberosUsername = new TextBox();
 		grid.setWidget(1, 0, new HTML(commonUiStrings.KerberosUsername()));
 		grid.setWidget(1, 1, kerberosUsername);
 		
 		kerberosPassword = new PasswordTextBox();
 		grid.setWidget(2, 0, new HTML(commonUiStrings.KerberosPassword()));
 		grid.setWidget(2, 1, kerberosPassword);
 		
 		environments = new ListBox(false);
 		environments.addItem("", "");
 		grid.setWidget(3, 0, new HTML(uiStrings.Environment()));
 		grid.setWidget(3, 1, environments);
 
 		environments.addChangeHandler(new ChangeHandler()
 		{
 			@Override
 			public void onChange(final ChangeEvent event)
 			{
 				products.clear();
 				
 				final String tocUrl = environments.getValue(environments.getSelectedIndex());
 				
 				if (!tocUrl.isEmpty())
 				{
 					enableUI(false);
 
 					final String sitesRestUrl = AutomationPortalUIConstants.REST_SERVER_URL + PRODUCTS_REST_ENDPOINT + "?tocUrl=" + URL.encode(tocUrl);
 
 					// Send request to server and catch any errors.
 					final RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, sitesRestUrl);
 
 					try
 					{
 						builder.sendRequest(null, new RequestCallback()
 						{
 							@Override
 							public void onError(final Request request, final Throwable exception)
 							{
 								enableUI(true);
 							}
 
 							@Override
 							public void onResponseReceived(final Request request, final Response response)
 							{
 								try
 								{
 									if (200 == response.getStatusCode())
 									{
 										final String jsonResponse = response.getText();
 										final JsArrayString responseData = StringList.convertList(jsonResponse);
 
 										for (int i = 0; i < responseData.length(); ++i)
 										{
 											final String product = responseData.get(i);
 											products.addItem(product);
 										}
 									}
 									else
 									{
 										// displayError("Couldn't retrieve JSON ("
 										// +
 										// response.getStatusText() + ")");
 									}
 								}
 								finally
 								{
 									enableUI(true);
 								}
 							}
 						});
 					}
 					catch (final RequestException ex)
 					{
 						// displayError("Couldn't retrieve JSON");
 						enableUI(true);
 					}
 				}
 			}
 		});
 
 		products = new ListBox(false);
 		grid.setWidget(4, 0, new HTML(uiStrings.Product()));
 		grid.setWidget(4, 1, products);
 		
 		go = new PushButton(commonUiStrings.Go());
 		go.setWidth("10em");
 		go.setHeight("2em");
 		go.getElement().getStyle().setProperty("textAlign", "center");
 		go.getElement().getStyle().setProperty("display", "table-cell");
 		go.getElement().getStyle().setProperty("verticalAlign", "middle");
 		go.addClickHandler(new ClickHandler()
 		{
 			@Override
 			public void onClick(final ClickEvent event)
 			{
 				run();
 			}
 		});
 		grid.setWidget(5, 0, go);
 
 		final HTML resultsLabel = new HTML(commonUiStrings.Results());
 		resultsLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
 		resultsLabel.getElement().getStyle().setMarginTop(2, Unit.EM);
 		grid.setWidget(6, 0, resultsLabel);
 		
 		message = new TextBox();
 		message.setReadOnly(true);
 		message.setWidth("40em");
 		grid.setWidget(7, 0, new HTML(commonUiStrings.Message()));
 		grid.setWidget(7, 1, message);
 
 		output = new TextArea();
 		output.setReadOnly(true);
 		output.setWidth("40em");
 		output.setHeight("10em");
 		grid.setWidget(8, 0, new HTML(commonUiStrings.Output()));
 		grid.setWidget(8, 1, output);
 
 		
 
 		template.getContentPanel().setWidget(topLevelPanel);
 
 		final String sitesRestUrl = AutomationPortalUIConstants.REST_SERVER_URL + SITES_REST_ENDPOINT;
 
 		// Send request to server and catch any errors.
 		final RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, sitesRestUrl);
 
 		try
 		{
 			builder.sendRequest(null, new RequestCallback()
 			{
 				@Override
 				public void onError(final Request request, final Throwable exception)
 				{
 					enableUI(true);
 				}
 
 				@Override
 				public void onResponseReceived(final Request request, final Response response)
 				{
 					try
 					{
 						if (200 == response.getStatusCode())
 						{
 							final String jsonResponse = response.getText();
 							final JsArray<StringPair> responseData = StringPair.convertList(jsonResponse);
 
 							for (int i = 0; i < responseData.length(); ++i)
 							{
 								final StringPair pair = responseData.get(i);
 								environments.addItem(pair.getSecondString(), pair.getFirstString());
 							}
 						}
 						else
 						{
 							// displayError("Couldn't retrieve JSON (" +
 							// response.getStatusText() + ")");
 						}
 					}
 					finally
 					{
 						enableUI(true);
 					}
 				}
 			});
 		}
 		catch (final RequestException ex)
 		{
 			// displayError("Couldn't retrieve JSON");
 			enableUI(true);
 		}
 	}
 
 	private void run()
 	{
 		enableUI(false);
 		
 		this.message.setText("");
 		this.output.setText("");
 
 		final String restUrl = AutomationPortalUIConstants.REST_SERVER_URL + REST_ENDPOINT + 
 				"?tocUrl=" + URL.encodeQueryString(this.environments.getValue(this.environments.getSelectedIndex())) +
 				"&productName=" + URL.encodeQueryString(this.products.getValue(this.products.getSelectedIndex())) +
 				"&username=" + URL.encodeQueryString(this.kerberosUsername.getText()) +
 				"&password=" + URL.encodeQueryString(this.kerberosPassword.getText());
 
 		// Send request to server and catch any errors.
 		final RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, restUrl);
 
 		try
 		{
 			builder.sendRequest(null, new RequestCallback()
 			{
 				@Override
 				public void onError(final Request request, final Throwable exception)
 				{
 					enableUI(true);
 				}
 
 				@Override
 				public void onResponseReceived(final Request request, final Response response)
 				{
 					try
 					{
 						final String jsonResponse = response.getText();
 						final AutomationPortalResponseData responseData = AutomationPortalResponseData.convert(jsonResponse);
 						message.setText(responseData.getMessage());
 						output.setText(responseData.getOutput());
 					}
 					catch (final Exception ex)
 					{
 						// an unexpected error has occurred
 					}
 
 					enableUI(true);
 				}
 			});
 		}
 		catch (final RequestException ex)
 		{
 			// displayError("Couldn't retrieve JSON");
 			enableUI(true);
 		}
 	}
 
 	private void enableUI(final boolean enabled)
 	{
 		template.showLoadingImage(!enabled);
 		if (go != null)
 			go.setEnabled(enabled);
 		if (environments != null)
 			environments.setEnabled(enabled);
 		if (products != null)
 			products.setEnabled(enabled);
 	}
 }
