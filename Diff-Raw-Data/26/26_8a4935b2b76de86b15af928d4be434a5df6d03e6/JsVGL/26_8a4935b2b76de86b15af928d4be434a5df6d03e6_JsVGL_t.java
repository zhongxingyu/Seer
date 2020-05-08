 package edu.umb.jsVGL.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.i18n.client.Dictionary;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CaptionPanel;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 import edu.umb.jsVGL.client.VGL.SavedProblemStrings;
 import edu.umb.jsVGL.client.VGL.VGLII;
 import edu.umb.jsVGL.client.VGL.UIimages.UIImageResource;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class JsVGL implements EntryPoint {
 	
 	private Button clearWorkspaceButton = null;
 	private Button newPracticeProblemButton = null;
 	private Button newGradedProblemButton = null;
 	private Button crossButton = null;
 	private Button superCrossButton = null;
 
 	private ListBox superCrossChoices = null;
 
 	private SimplePanel modelBuilderPanel = null;
 	private FlowPanel superCrossPanel = null;
 
 	private VGLII vglII;
 
 	private ScrollPanel cageScrollPanel;
 	private VerticalPanel cagesPanel;
 
 	/**
 	 * This is the entry point method.
 	 */
 	public void onModuleLoad() {
 		Dictionary params = Dictionary.getDictionary("Params");
 		vglII = new VGLII(params, this);
 		buildMainPanelUI(params);
 		exportMethods(this);
 	}
 
 	private void buildMainPanelUI(Dictionary params) {
 
 		clearWorkspaceButton = new Button("Clear Workspace");
 		clearWorkspaceButton.setStyleName("jsVGL_Button");
 		RootPanel.get("clearWorkspaceButtonContainer").add(clearWorkspaceButton);
 		clearWorkspaceButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				vglII.resetProblemSpace();
 				resetUI();
 				modelBuilderPanel.clear();
 				modelBuilderPanel.add(new Label("Please Start a problem before making a model."));
 			}			
 		});
 
 		newPracticeProblemButton = new Button("New Practice Problem");
 		newPracticeProblemButton.setStyleName("jsVGL_Button");
 		RootPanel.get("newPracticeProblemButtonContainer").add(newPracticeProblemButton);
 		newPracticeProblemButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				vglII.newPracticeProblem();
 			}			
 		});
 
 		newGradedProblemButton = new Button("New Graded Problem");
 		newGradedProblemButton.setStyleName("jsVGL_Button");
 		RootPanel.get("newGradedProblemButtonContainer").add(newGradedProblemButton);
 		newGradedProblemButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				vglII.newGradedProblem();
 			}			
 		});
 
 		crossButton = new Button("Cross Two");
 		crossButton.setStyleName("jsVGL_CrossButton");
 		RootPanel.get("crossButtonContainer").add(crossButton);
 		crossButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				vglII.crossTwo(false);
 			}			
 		});
 
 		setButtonState(false);
 
 		final TabPanel mainPanel = new TabPanel();
 
 		final FlowPanel instructionsPanel = new FlowPanel();
 		instructionsPanel.add(new HTML(TextStrings.WELCOME_TEXT));
 		mainPanel.add(instructionsPanel, "Instructions");
 
 		modelBuilderPanel = new SimplePanel();
 		modelBuilderPanel.add(new Label("Please Start a problem before making a model."));
 		mainPanel.add(modelBuilderPanel, "Genetic Model");
 
 		if (params.keySet().contains("SuperCrossEnabled") && Boolean.parseBoolean(params.get("SuperCrossEnabled"))) {
 			superCrossPanel = new FlowPanel();
 			superCrossPanel.add(new HTML(TextStrings.SUPER_CROSS_TEXT));
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
 			superCrossButton.addClickHandler(new ClickHandler() {
 				public void onClick(ClickEvent event) {
 					vglII.crossTwo(true);
 				}			
 			});
 
 			mainPanel.add(superCrossPanel, "Super Cross");
 		}
 		
 		final SimplePanel aboutPanel = new SimplePanel();
 		aboutPanel.setWidget(new HTML(TextStrings.ABOUT_jsVGL));
 		mainPanel.add(aboutPanel, "About jsVGL");
 
 		mainPanel.selectTab(0);
		mainPanel.setSize("650px", "250px");
 		mainPanel.addStyleName("table-center");
 		RootPanel.get("mainPanelContainer").add(mainPanel);	
 
 		cagesPanel = new VerticalPanel();
 		cageScrollPanel = new ScrollPanel(cagesPanel);
 		cageScrollPanel.setSize("650px", "500px");
 		CaptionPanel captionedCageScrollPanel = new CaptionPanel("Cages with Organisms for crossing");
 		captionedCageScrollPanel.add(cageScrollPanel);
 		captionedCageScrollPanel.setStyleName("jsVGL_CagePanel");
 		RootPanel.get("cagesContainer").add(captionedCageScrollPanel);
 	}
 	
 	/*
 	 * methods for interfacing with LMS
 	 */
 	
 	public native void exportMethods(JsVGL jsvgl) /*-{
 		$wnd.getStateXML = $entry(function() {return jsvgl.@edu.umb.jsVGL.client.JsVGL::getStateXML()();});
 		$wnd.setStateXML = $entry(function(xmlString) {return jsvgl.@edu.umb.jsVGL.client.JsVGL::setStateXML(Ljava/lang/String;)(xmlString);});
 		$wnd.getGradeXML = $entry(function() {return jsvgl.@edu.umb.jsVGL.client.JsVGL::getGradeXML()();});
 		$wnd.getGradeHTML = $entry(function() {return jsvgl.@edu.umb.jsVGL.client.JsVGL::getGradeHTML()();});
 	}-*/;
 	
 	public String getStateXML() {
 		return vglII.saveProblem().problemXML;
 	}
 	
 	public void setStateXML(String state) {
 		resetUI();
 		vglII.openProblem(state);
 	}
 	
 	public String getGradeXML() {
 		return vglII.saveProblem().gradeXML;
 	}
 	
 	public String getGradeHTML() {
 		return vglII.saveProblem().gradeHTML;
 	}
 
 	/*
 	 * two states
 	 * 	FALSE startup (no problem):
 	 * 		clear workspace disabled
 	 * 		new Prob enabled
 	 * 		open work enabled
 	 * 		cross disabled
 	 * 		save disabled
 	 * 
 	 * TRUE have problem to work on
 	 * 		clear workspace enabled
 	 * 		new prob disabled
 	 * 		open work disabled
 	 * 		cross enabled
 	 * 		save enabled
 	 */
 	public void setButtonState(boolean state) {
 		clearWorkspaceButton.setEnabled(state);
 		newPracticeProblemButton.setEnabled(!state);
 		newGradedProblemButton.setEnabled(!state);
 		crossButton.setEnabled(state);
 	}
 
 	public SimplePanel getModelBuilderPanel() {
 		return modelBuilderPanel;
 	}
 
 	public Panel getSuperCrossPanel() {
 		return superCrossPanel;
 	}
 
 	public VerticalPanel getCagesPanel() {
 		return cagesPanel;
 	}
 
 	public void scrollCagesToBottom() {
 		cageScrollPanel.scrollToBottom();
 	}
 
 	public void resetUI() {
 		cagesPanel.clear();
 		setButtonState(false);
 	}
 
 	public int getSuperCrossChoice() {
 		if (superCrossChoices != null) {
 			return Integer.parseInt(superCrossChoices.getValue(superCrossChoices.getSelectedIndex()));
 		} else {
 			return 0;
 		}
 	}
 
 }
