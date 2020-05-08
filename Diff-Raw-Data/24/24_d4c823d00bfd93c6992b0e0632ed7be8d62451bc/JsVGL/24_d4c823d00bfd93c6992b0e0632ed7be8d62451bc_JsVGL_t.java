 package edu.umb.jsVGL.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.i18n.client.Dictionary;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 import edu.umb.jsVGL.client.ModelBuilder.ModelBuilderUI;
 import edu.umb.jsVGL.client.VGL.VGLII;
 import edu.umb.jsVGL.client.VGL.UIimages.UIImageResource;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class JsVGL implements EntryPoint {
 
 	private final static String WELCOME_HTML = "<html><body>"
 			+ "<h3>Welcome to VGL</h3>"
 			+ "You can either:"
 			+ "<ul><li>Start a new problem by clicking the &quot;New Problem&quot; button above.</li>"
 			+ "<li>Open work you have saved on this problem by clicking the &quot;Open Work&quot; button above.</li></ul>"
 			+ "</body></html>";
 
 	private Button newPracticeProblemButton = null;
 	private Button newGradedProblemButton = null;
 	private Button openWorkButton = null;
 	private Button saveWorkButton = null;
 	private Button crossButton = null;
 	private Button superCrossButton = null;
 
 	private ListBox superCrossChoices = null;
 
	private SimplePanel modelBuilderPanel = null;
 	private FlowPanel summaryChartPanel = null;
 	private FlowPanel superCrossPanel = null;
 
 	private UIImageResource uiImageResource;
 
 	private VGLII vglII;
 
 	/**
 	 * This is the entry point method.
 	 */
 	public void onModuleLoad() {
 		uiImageResource = GWT.create(UIImageResource.class);
 		Dictionary params = Dictionary.getDictionary("Params");
 		vglII = new VGLII(params, this);
 		buildMainPanelUI();
 	}
 
 	private void buildMainPanelUI() {
 		newPracticeProblemButton = new Button("New Practice Problem");
 		RootPanel.get("newPracticeProblemButtonContainer").add(newPracticeProblemButton);
 		newPracticeProblemButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				vglII.newPracticeProblem();
 			}			
 		});
 
 		newGradedProblemButton = new Button("New Graded Problem");
 		RootPanel.get("newGradedProblemButtonContainer").add(newGradedProblemButton);
 		newGradedProblemButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				vglII.newGradedProblem();
 			}			
 		});
 
 		openWorkButton = new Button("Open Work");
 		RootPanel.get("openWorkButtonContainer").add(openWorkButton);
 
 		saveWorkButton = new Button("Save Work");
 		RootPanel.get("saveWorkButtonConatiner").add(saveWorkButton);
 		saveWorkButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				vglII.saveProblem();
 			}			
 		});
 		
 
 		crossButton = new Button("Cross Two");
 		RootPanel.get("crossButtonContainer").add(crossButton);
 		crossButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				saveWorkButton.setEnabled(true);
 				vglII.crossTwo(false);
 			}			
 		});
 		
 		setButtonState(false);
 
 		final TabPanel mainPanel = new TabPanel();
 
 		final FlowPanel instructionsPanel = new FlowPanel();
 		instructionsPanel.add(new HTML(WELCOME_HTML));
 		mainPanel.add(instructionsPanel, "Instructions");
 
		modelBuilderPanel = new SimplePanel();
		modelBuilderPanel.add(new Label("Please Start a problem before making a model."));
 		mainPanel.add(modelBuilderPanel, "Model Builder");
 
 		summaryChartPanel = new FlowPanel();
 		summaryChartPanel.add(new Label("SCUI"));
 		mainPanel.add(summaryChartPanel, "Summary Chart");
 
 		if (RootPanel.get("superCrossButtonContainer") != null) {
 			superCrossPanel = new FlowPanel();
 			superCrossPanel.add(new HTML(
 					"<h3>Super Cross</h3>"
 							+ "This carries out a cross with a large number of offpspring.<br>"
 							+ "It is useful for getting recombination frequency data.<br>"
 							+ "WARNING: it produces large files which are slow to save,<br>"
 							+ "so it should be used sparingly.<br>"
 							+"Choose the desired number of offspring from the list below:<br>"));
 			superCrossChoices = new ListBox();
 			superCrossChoices.addItem("100");
 			superCrossChoices.addItem("200");
 			superCrossChoices.addItem("500");
 			superCrossChoices.addItem("1000");
 			superCrossChoices.addItem("2000");
 			superCrossChoices.setVisibleItemCount(1);
 			superCrossPanel.add(superCrossChoices);
 			superCrossButton = new Button("Super Cross");
 			superCrossPanel.add(superCrossButton);
 			mainPanel.add(superCrossPanel, "Super Cross");
 		}
 
 		mainPanel.selectTab(0);
 		mainPanel.setSize("500px", "250px");
 		mainPanel.addStyleName("table-center");
 		RootPanel.get("mainPanelContainer").add(mainPanel);	
 	}
 
 	/*
 	 * two states
 	 * 	FALSE startup (no problem):
 	 * 		new Prob enabled
 	 * 		open work enabled
 	 * 		cross disabled
 	 * 		save disabled
 	 * 
 	 * TRUE have problem to work on
 	 * 		new prob disabled
 	 * 		open work disabled
 	 * 		cross enabled
 	 * 		save enabled
 	 */
 	public void setButtonState(boolean state) {
 		newPracticeProblemButton.setEnabled(!state);
 		newGradedProblemButton.setEnabled(!state);
 		openWorkButton.setEnabled(!state);
 		crossButton.setEnabled(state);
 		saveWorkButton.setEnabled(state);
 	}
 		
	public SimplePanel getModelBuilderPanel() {
 		return modelBuilderPanel;
 	}
 
 	public Panel getSummaryChartPanel() {
 		return summaryChartPanel;
 	}
 
 	public Panel getSuperCrossPanel() {
 		return superCrossPanel;
 	}
 
 	public int getSuperCrossChoice() {
 		if (superCrossChoices != null) {
 			return Integer.parseInt(superCrossChoices.getValue(superCrossChoices.getSelectedIndex()));
 		} else {
 			return 0;
 		}
 	}
 
 	public void newPracticeProblem() {
 		final DialogBox test = new DialogBox();
 		test.setHTML("&nbsp;&nbsp;Cage 2");
 
 		VerticalPanel panel = new VerticalPanel();
 		panel.setSize("100px", "100px");
 		Image closeButtonImage = new Image(uiImageResource.closeButton());
 		Anchor closeButton = new Anchor("");
 		closeButton.getElement().appendChild(closeButtonImage.getElement());
 		closeButton.setTitle("Close Cage");
 		panel.add(closeButton);
 		closeButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				test.hide();
 			}			
 		});
 		closeButton.setStyleName("TopRight");
 
 		Image male = new Image(uiImageResource.male());
 		panel.add(male);
 
 		test.add(panel);
 		test.center();
 	}
 }
