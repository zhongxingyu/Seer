 package com.redhat.automationportalui.client.pav;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style.FontWeight;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.PasswordTextBox;
 import com.google.gwt.user.client.ui.PushButton;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.redhat.automationportalui.client.AutomationPortalUIClientFactory;
 import com.redhat.automationportalui.client.constants.AutomationPortalUIConstants;
 import com.redhat.automationportalui.client.data.AutomationPortalResponseData;
 import com.redhat.automationportalui.client.resources.APUI_Errors;
 import com.redhat.automationportalui.client.resources.BugzillaReportGeneratorUIStrings;
 import com.redhat.automationportalui.client.resources.CommonUIStrings;
 import com.redhat.automationportalui.client.template.AutomationPortalUITemplate;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.http.client.URL;
 
 /**
  * A GWT View that represents the Generate Bugzilla Report UI
  */
 public class BugzillaReportGeneratorView
 {
 	private final static String REST_ENDPOINT = "rest/BugzillaReportGenerator/get/json/Execute";
 	private final AutomationPortalUITemplate template;
 	private final BugzillaReportGeneratorUIStrings uiStrings;
 	private final CommonUIStrings commonUiStrings;
 	private TextBox bugzillaUsername;
 	private PasswordTextBox bugzillaPassword; 
 	private TextBox message;
 	private TextArea output;
 	private PushButton go;
 
 	public BugzillaReportGeneratorView(final AutomationPortalUIClientFactory clientFactory, final AutomationPortalUITemplate template, final CommonUIStrings commonUiStrings, final APUI_Errors apuiErrors)
 	{
 		/* Get the translates log messages */
 		uiStrings = (BugzillaReportGeneratorUIStrings) GWT.create(BugzillaReportGeneratorUIStrings.class);
 		this.commonUiStrings = commonUiStrings;
 		this.template = template;
 	}
 
 	public void display()
 	{
 		template.getSubTitle().setText(commonUiStrings.BugzillaReportGenerator());
 		
 		final VerticalPanel topLevelPanel = new VerticalPanel();
 
 		topLevelPanel.add(new HTML(uiStrings.Description()));
 		
 		final HTML descriptionLineTwo = new HTML(uiStrings.DescriptionLineTwo());
 		descriptionLineTwo.getElement().getStyle().setMarginBottom(2, Unit.EM);
 		topLevelPanel.add(descriptionLineTwo);
 		
 		final Grid grid = new Grid(7, 2);
 		topLevelPanel.add(grid);
 
 		final HTML optionsLabel = new HTML(commonUiStrings.Options());
 		optionsLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
 		grid.setWidget(0, 0, optionsLabel);
 		
 		bugzillaUsername = new TextBox();
 		bugzillaUsername.setWidth("40em");
 		grid.setWidget(1, 0, new HTML(uiStrings.BugzillaUsername()));
 		grid.setWidget(1, 1, bugzillaUsername);
 		
 		bugzillaPassword = new PasswordTextBox();
 		bugzillaPassword.setWidth("40em");
 		grid.setWidget(2, 0, new HTML(uiStrings.BugzillaPassword()));
 		grid.setWidget(2, 1, bugzillaPassword);
 		
 		go = new PushButton(commonUiStrings.Go());
 		go.setWidth("10em");
 		go.setHeight("2em");
 		go.getElement().getStyle().setProperty("textAlign", "center");
 		go.getElement().getStyle().setProperty("display", "table-cell");
 		go.getElement().getStyle().setProperty("verticalAlign", "middle");
 		go.addClickHandler(new ClickHandler(){
 			@Override
 			public void onClick(final ClickEvent event)
 			{
 				run();
 			}
 		});
 		grid.setWidget(3, 0, go);
 
 		final HTML resultsLabel = new HTML(commonUiStrings.Results());
 		resultsLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
 		resultsLabel.getElement().getStyle().setMarginTop(2, Unit.EM);
 		grid.setWidget(4, 0, resultsLabel);
 		
 		message = new TextBox();
 		message.setReadOnly(true);
 		message.setWidth("40em");
 		grid.setWidget(5, 0, new HTML(commonUiStrings.Message()));
 		grid.setWidget(5, 1, message);
 		
 		output = new TextArea();
 		output.setReadOnly(true);
 		output.setWidth("40em");
 		output.setHeight("10em");
 		grid.setWidget(6, 0, new HTML(commonUiStrings.Output()));
 		grid.setWidget(6, 1, output);
 		
 		template.getContentPanel().setWidget(topLevelPanel);
 		
 		enableUI(true);
 	}
 
 	private void run()
 	{
 		enableUI(false);
 		
 		final String username = bugzillaUsername.getText();
 		final String password = bugzillaPassword.getText();
 		
		final String restUrl = AutomationPortalUIConstants.REST_SERVER_URL + REST_ENDPOINT + "?bugzillaUsername=" + URL.encode(username) + "&bugzillaPassword=" + URL.encode(password); 
 		
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
 					if (200 == response.getStatusCode())
 					{
 						final String jsonResponse = response.getText();
 						final AutomationPortalResponseData responseData = AutomationPortalResponseData.convert(jsonResponse);
 						message.setText(responseData.getMessage());
 						output.setText(responseData.getOutput());
 					}
 					else
 					{
 						// displayError("Couldn't retrieve JSON (" +
 						// response.getStatusText() + ")");
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
 		go.setEnabled(enabled);
 		bugzillaUsername.setEnabled(enabled);
 		bugzillaPassword.setEnabled(enabled);	
 	}
 }
