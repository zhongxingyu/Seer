 package edu.cmu.square.client.ui.reviewAndFinalizeRequirements;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.MouseOverEvent;
 import com.google.gwt.event.dom.client.MouseOverHandler;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DecoratedPopupPanel;
 import com.google.gwt.user.client.ui.DisclosurePanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
 
 import edu.cmu.square.client.exceptions.ExceptionHelper;
 import edu.cmu.square.client.model.GwtModesType;
 import edu.cmu.square.client.model.GwtProject;
 import edu.cmu.square.client.model.GwtQualityAttribute;
 import edu.cmu.square.client.model.GwtRating;
 import edu.cmu.square.client.model.GwtRequirement;
 import edu.cmu.square.client.model.GwtSoftwarePackage;
 import edu.cmu.square.client.model.StepStatus;
 import edu.cmu.square.client.navigation.State;
 import edu.cmu.square.client.remoteService.step.interfaces.ReviewAndFinalizeRequirementsService;
 import edu.cmu.square.client.remoteService.step.interfaces.ReviewAndFinalizeRequirementsServiceAsync;
 import edu.cmu.square.client.remoteService.step.interfaces.ReviewPackagesService;
 import edu.cmu.square.client.remoteService.step.interfaces.ReviewPackagesServiceAsync;
 import edu.cmu.square.client.ui.ChooseStepCase3.ChooseStepCase3Pilot;
 import edu.cmu.square.client.ui.core.BasePane;
 import edu.cmu.square.client.ui.core.SquareHyperlink;
 import edu.cmu.square.client.ui.reviewOfRequirementsByAcquisitionOrganization.ReviewOfRequirementsByAcquisitionPane;
 import edu.cmu.square.client.ui.reviewOfRequirementsByAcquisitionOrganization.ReviewOfRequirementsByAcquisitionPilot;
 //import edu.cmu.square.client.ui.reviewOfRequirementsByAcquisitionOrganization.ReviewOfRequirementsByAcquisitionPane.SummaryElementHyperLinkElement;
 
 
 public class ReviewAndFinalizeRequirementsPane extends BasePane
 {
 	private int projectID;
 	private List<GwtRequirement> listOfRequirements = new ArrayList<GwtRequirement>();
 	private List<GwtRequirement> listOfFilteredRequirements = new ArrayList<GwtRequirement>();
 	private List<GwtSoftwarePackage> softwarePackages;
 	private List<GwtQualityAttribute> attributes;
 	private List<GwtRating> ratings;
 	
 	GwtProject currentProject;
 	
 	private VerticalPanel vPane = new VerticalPanel();
 	private VerticalPanel vPaneData = new VerticalPanel();
 
 	private VerticalPanel vPaneCots = new VerticalPanel();
 	private VerticalPanel vPaneCotsData = new VerticalPanel();
 	private FlexTable matrix = new FlexTable();
 	private FlexTable matrixHeader = new FlexTable();
 	
 	private int modifyRequirementId = -1;
 
 	//private CreateRequirementDialog createRequirementDialog;
 	private GwtRequirement newRequirement;
 	private String lastSearch="";
 	
 	private int pageSize=10;
 	private int currentPage=1;
 	private int pageCount=0;
 	
 	boolean isReadOnly=false;
 	
 	final ReviewAndFinalizeRequirementsMessages messages = (ReviewAndFinalizeRequirementsMessages) GWT.create(ReviewAndFinalizeRequirementsMessages.class);
 	
 	
 	private ReviewPackagesServiceAsync packageService = GWT.create(ReviewPackagesService.class);
 	
 	public ReviewAndFinalizeRequirementsPane(State stateInfo)
 	{
 			super(stateInfo);
 			this.showLoadingStatusBar();
 			
 			ServiceDefTarget endpoint = (ServiceDefTarget) packageService;
 			endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "reviewPackages.rpc");
 			
 			currentProject = new GwtProject();
 			currentProject.setId(this.getCurrentState().getProjectID());
 			
 			isReadOnly = true;
 			
 			this.projectID = this.getCurrentState().getProjectID();
 			SetQueryString();
 			
 			loadAttributes();
 		
 			
 
 	}
 
 	public void SetQueryString()
 	{
 		
 			if(this.getQueryString().get("PageId")!=null)
 			{
 				
 					try
 					{
 					this.currentPage=Integer.parseInt(this.getQueryString().get("PageId"));
 					}
 					catch(Exception ex)
 					{
 						this.currentPage=1;
 					}
 				
 			}
 			
 			if(this.getQueryString().get("search")!=null)
 			{
 			 this.lastSearch=this.getQueryString().get("search");
 			}
 			
 	}
 	
 	// ------------------------------------------------------
 	// RPC Asynchronous calls section
 	// ------------------------------------------------------
 
 	public void loadRequirements()
 	{
 
 		ReviewAndFinalizeRequirementsServiceAsync service1 = GWT.create(ReviewAndFinalizeRequirementsService.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) service1;
 		//System.out.println("    ******We're at loadRequirement()");
 		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "reviewAndFinalizeRequirementsService.rpc");
 
 		GwtProject project = new GwtProject();
 		project.setId(this.getCurrentState().getProjectID());
 
 		service1.getRequirements(project, new AsyncCallback<List<GwtRequirement>>()
 		{
 
 				public void onFailure(Throwable caught)
 				{
 					ExceptionHelper.SquareRootRPCExceptionHandler(caught, "Retrieving Requirements");
 				}
 
 				public void onSuccess(List<GwtRequirement> result)
 				{
 					listOfRequirements = result;
 
 					initializePane();
 				}
 			});
 
 	}
 	
 	private void loadAttributes()
 	{
 		final StepStatus stepStatus =  StepStatus.NotStarted;
 		packageService.getQualityAttributes(currentProject,stepStatus, new AsyncCallback<List<GwtQualityAttribute> >()
 				{
 					
 					@Override
 					public void onSuccess(List<GwtQualityAttribute> result)
 					{
 						System.out.println("We got quality attribute: "+result.size());
 						attributes = result;
 						loadPackages();
 					}
 					
 					@Override
 					public void onFailure(Throwable caught)
 					{
 						Window.alert(messages.attributesRetrievalError());
 						ExceptionHelper.SquareRootRPCExceptionHandler(caught, "Retrieving Quality Attributes");
 						
 					}
 				});
 		
 	}
 
 	private void loadPackages()
 	{
 		final StepStatus stepStatus =  StepStatus.NotStarted;
 		packageService.getSoftwarePackages(currentProject,stepStatus, new AsyncCallback<List<GwtSoftwarePackage>>()
 		{
 			
 			@Override
 			public void onSuccess(List<GwtSoftwarePackage> result)
 			{
 				softwarePackages = result;
 				System.out.println("We got software packages: "+result.size());
 				loadRatings();
 				
 			}
 			
 			@Override
 			public void onFailure(Throwable caught)
 			{
 				Window.alert(messages.packagesRetrievalError());
 				ExceptionHelper.SquareRootRPCExceptionHandler(caught, "Retrieving Software Packages");
 				
 			}
 		});
 		
 	}
 
 	private void loadRatings()
 	{
 		packageService.getRateValues(currentProject.getId(), new AsyncCallback<List<GwtRating > >()
 				{
 					
 					@Override
 					public void onSuccess(List<GwtRating >  result)
 					{
 						System.out.println("We got ratings: "+result.size());
 						ratings = result;
 						drawRateMatrix();
 						getTotalsFromMatrix();
 						loadRequirements();
 						//PaneInitialization();
 					}
 					
 					@Override
 					public void onFailure(Throwable caught)
 					{
 						Window.alert(messages.ratingsRetrievalError());
 						ExceptionHelper.SquareRootRPCExceptionHandler(caught, "Retrieving Ratings");
 					}
 				});
 		
 	}
 	
 	public void addRequirement(GwtRequirement gwtRequirement, GwtProject gwtProject)
 	{
 		this.showStatusBar("adding...");
 
 		ReviewAndFinalizeRequirementsServiceAsync service1 = GWT.create(ReviewAndFinalizeRequirementsService.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) service1;
 		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "reviewAndFinalizeRequirementsService.rpc");
 
 		GwtProject project = new GwtProject();
 		project.setId(this.getCurrentState().getProjectID());
 
 		service1.addRequirement(gwtProject, gwtRequirement, new AsyncCallback<GwtRequirement>()
 		{
 
 				public void onFailure(Throwable caught)
 				{
 					ExceptionHelper.SquareRootRPCExceptionHandler(caught, "Adding Requirements");
 
 				}
 
 				public void onSuccess(GwtRequirement result)
 				{
 					newRequirement = result;
 					modifyRequirementId = newRequirement.getId();
 					loadRequirements();
 
 				}
 			});
 
 	}
 	
 
 	
 	public void removeRequirement(final GwtRequirement gwtRequirement)
 	{
 
 		this.showStatusBar("removing...");
 		ReviewAndFinalizeRequirementsServiceAsync service1 = GWT.create(ReviewAndFinalizeRequirementsService.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) service1;
 		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "reviewAndFinalizeRequirementsService.rpc");
 
 		GwtProject project = new GwtProject();
 		project.setId(this.getCurrentState().getProjectID());
 
 		service1.removeRequirement(gwtRequirement, new AsyncCallback<Void>()
 			{
 
 				public void onFailure(Throwable caught)
 				{
 					ExceptionHelper.SquareRootRPCExceptionHandler(caught, "Retriving Requirements");
 
 				}
 
 				@Override
 				public void onSuccess(Void result)
 				{
 
 					loadRequirements();
 				}
 
 			});
 
 	}
 
 	
 	public void updateRequirement(final GwtRequirement gwtRequirement, GwtProject gwtProject)
 	{
 
 		this.showStatusBar("updating...");
 
 		ReviewAndFinalizeRequirementsServiceAsync service1 = GWT.create(ReviewAndFinalizeRequirementsService.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) service1;
 		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "reviewAndFinalizeRequirementsService.rpc");
 
 		GwtProject project = new GwtProject();
 		project.setId(this.getCurrentState().getProjectID());
 
 		service1.updateRequirement(gwtProject, gwtRequirement, new AsyncCallback<Void>()
 			{
 
 				public void onFailure(Throwable caught)
 				{
 					ExceptionHelper.SquareRootRPCExceptionHandler(caught, "Retriving Requirements");
 				}
 
 				@Override
 				public void onSuccess(Void result)
 				{
 					modifyRequirementId = gwtRequirement.getId();
 					loadRequirements();
 				}
 			});
 	}
 
 	public void initializePane()
 
 	{
 		this.hideStatusBar();
 		VerticalPanel layout = new VerticalPanel();
 		
 		drawRateMatrix();
 		getTotalsFromMatrix();
 		drawQualityAttributes();
 		drawSoftwarePackages();
 		
 		drawMatrixPage();
 		
 		this.getContent().clear();
 		
 		this.getContent().add(vPane);
 		loadRequirementsTable();
 			
 		this.getContent().add(vPaneCots);
 		vPaneCotsData.add(layout);
 		layout.add(matrixHeader);
 		this.getContent().add(vPaneCotsData);
 		loadCotsTable();
 		
 //make a new loadCotTable for only reading
 //for example, loadReadOnlyCotsTable()
 		
 		if(currentState.getMode() == GwtModesType.ReadWrite){
 //add initial pane for both reading and writing.
 		}
 		
 		else if(currentState.getMode() == GwtModesType.ReadOnly){
 //add 
 		}
 		//layout.add(comparisonMatrixLabel);
 		//layout.add(matrixHeader);
 		//this.getContent().add(layout);
 
 	}
 	
 	public void loadCotsTable()
 	{
 		
 		//vPane.clear();
 		vPaneCots.setSpacing(0);
 		vPaneCots.setWidth("90%");
 		vPaneCots.setHeight("5%");
 
 		vPaneCots.add(getCotsTableHeaderRow());
 		//vPaneCots.add(vPaneCotsData);
 		addDoneButton();
 	}
 
 	public void loadReadOnlyCotsTable()
 	{
 		
 		//vPane.clear();
 		vPaneCots.setSpacing(0);
 		vPaneCots.setWidth("90%");
 		vPaneCots.setHeight("5%");
 
 		vPaneCots.add(getCotsTableHeaderRow());
 		//vPaneCots.add(vPaneCotsData);
 		//addDoneButton();
 	}
 
 	
 	public void drawRateMatrix()
 	{
 		matrix.clear();
 		//matrix.setBorderWidth(1);
 		matrix.setWidth("100%");
 		matrix.setStyleName("square-Matrix");
 		matrix.setCellSpacing(0);
 		drawQualityAttributes();
 		drawSoftwarePackages();
 		drawRateMatrixValues();
 
 	}
 
 	public void drawQualityAttributes()
 	{
 		FlexCellFormatter formatter = this.matrix.getFlexCellFormatter();
 		matrix.setWidget(0, 0, new Label(" "));
 		formatter.setStyleName(0, 0, "square-Matrix");
 
 		// Set the header rows  with techniques
 		for(int i=1; i<=attributes.size();i++)
 		{
 			
 			
 				Label techniqueLabel = new Label(attributes.get(i-1).getName());
 
 				final DecoratedPopupPanel simplePopup = new DecoratedPopupPanel(true);
 				// simplePopup.ensureDebugId("cwBasicPopup-simplePopup");
 				simplePopup.setWidth("150px");
 				simplePopup.setWidget(new HTML(attributes.get(i-1).getDescription()));
 
 				techniqueLabel.addMouseOverHandler(new MouseOverHandler(){
 
 					public void onMouseOver(MouseOverEvent event) 
 					{
 						Widget source = (Widget) event.getSource();
 						int left = source.getAbsoluteLeft() + 20;
 						int top = source.getAbsoluteTop() + 20;
 						simplePopup.setPopupPosition(left, top);
 						simplePopup.show();
 
 					}});
 				techniqueLabel.addMouseOutHandler(new MouseOutHandler(){
 
 
 					public void onMouseOut(MouseOutEvent event) {
 						simplePopup.hide();
 
 					}});
 
 
 
 				matrix.setWidget(0, i, techniqueLabel);
 				formatter.setHorizontalAlignment(0, i, HasHorizontalAlignment.ALIGN_CENTER);
 				formatter.setStyleName(0, i, "square-Matrix");		
 		}
 
 		matrix.setWidget(0, attributes.size()+1, new Label("Total"));
 		formatter.setHorizontalAlignment(0, attributes.size()+1, HasHorizontalAlignment.ALIGN_RIGHT);
 		formatter.setStyleName(0, attributes.size()+1,"square-Matrix");
 
 	}
 
 	public void drawSoftwarePackages()
 	{
 
 		FlexCellFormatter formatter = this.matrix.getFlexCellFormatter();
 		// Set the left columns with the evaluation criteria 
 		for(int j=0, widgetCount=0; j<softwarePackages.size();j++, ++widgetCount)
 		{
 			
 				Label softwarePackageLabel = new Label(softwarePackages.get(j).getName());
 
 				final DecoratedPopupPanel simplePopup = new DecoratedPopupPanel(true);
 				simplePopup.setWidth("150px");
 				simplePopup.setWidget(new HTML(softwarePackages.get(j).getDescription()));
 
 				softwarePackageLabel.addMouseOverHandler(new MouseOverHandler(){
 
 					public void onMouseOver(MouseOverEvent event) {
 						Widget source = (Widget) event.getSource();
 						int left = source.getAbsoluteLeft() + 40;
 						int top = source.getAbsoluteTop() + 20;
 						simplePopup.setPopupPosition(left, top);
 						simplePopup.show();
 
 					}});
 				softwarePackageLabel.addMouseOutHandler(new MouseOutHandler(){
 
 
 					public void onMouseOut(MouseOutEvent event) {
 						simplePopup.hide();
 
 					}});
 
 				if(j == 0)
 				{
 					matrix.setWidget(j+1,0 , softwarePackageLabel);
 					formatter.setHorizontalAlignment(j+1,0 , HasHorizontalAlignment.ALIGN_RIGHT);
 					formatter.setStyleName(j+1,0 ,  "square-Matrix");
 					matrix.setWidget(j+2,0 , new Label(""));
 					++widgetCount;
 
 				}
 				else
 				{
 					matrix.setWidget(widgetCount+1,0 , softwarePackageLabel);
 					formatter.setHorizontalAlignment(widgetCount+1,0 , HasHorizontalAlignment.ALIGN_RIGHT);
 					formatter.setStyleName(widgetCount+1,0 ,  "square-Matrix");
 				}
 			
 			
 
 		}
 
 	}
 	
 	private int getValueFromlistOfRateValues(int packageID, int attributeID)
 	{
 		for(int j=0; j<ratings.size();j++)
 		{
 			int aID=ratings.get(j).getAttributeId();
 			int pID =ratings.get(j).getPackageId();
 			
 			if(packageID==pID && attributeID==aID)
 			{
 				return ratings.get(j).getValue();
 			}			
 		}
 		return 0;
 	}
 	
 	public void drawRateMatrixValues()
 	{
 		FlexCellFormatter formatter = this.matrix.getFlexCellFormatter();
 		for(int i=0; i<attributes.size();i++)
 		{
 			
 			for(int j=0, widgetCount = 0; j<softwarePackages.size();j++, ++widgetCount)
 			{
 				int tID=attributes.get(i).getId();
 				int eID=softwarePackages.get(j).getId();
 				
 				int value = getValueFromlistOfRateValues(eID, tID);
 				
 				if(isReadOnly)
 				{
 					Label valueLabel = new Label(String.valueOf(value));
 					valueLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 					
 					
 					if(j==0)
 					{
 						matrix.setWidget(1, i+1, valueLabel);
 						formatter.setHorizontalAlignment(1, i+1, HasHorizontalAlignment.ALIGN_CENTER);
 						formatter.setStyleName(1, i+1,"square-Matrix");
 						++widgetCount;
 					}
 					else
 					{
 						matrix.setWidget(widgetCount+1, i+1, valueLabel);
 						formatter.setHorizontalAlignment(widgetCount+1, i+1, HasHorizontalAlignment.ALIGN_CENTER);
 						formatter.setStyleName(widgetCount+1, i+1,"square-Matrix");
 					}
 					
 					
 				}
 				RateValueLabel totalLabel= new RateValueLabel(attributes.get(i).getId());
 				totalLabel.setText("0");
 
 				if(widgetCount>1)
 				{
 				matrix.setWidget(widgetCount+1, attributes.size()+1, totalLabel);
 				formatter.setHorizontalAlignment(widgetCount+1, attributes.size()+1,  HasHorizontalAlignment.ALIGN_CENTER);
 				formatter.setStyleName(widgetCount+1, attributes.size()+1,"square-Matrix");
 				}
 			}
 			
 						
 		}
 	}
 	
 	
 	/**
 	 * Get the totals traversing the current Matrix FlexTable
 	 */
 	public void getTotalsFromMatrix()
 	{
 		System.out.println("rows: "+matrix.getRowCount()+" columns: "+matrix.getCellCount(0));
 		for(int i=3; i<=softwarePackages.size()+1;i++)
 		{	
 			int sum=0;
 			
 			for(int j=1; j<=attributes.size();j++)
 			{
 					Widget w =  matrix.getWidget(i, j);
 					Widget weight =  matrix.getWidget(1, j);
 					
 					if ( w instanceof RateValueTextbox) 
 					{
 						RateValueTextbox new_name = (RateValueTextbox) w;
 						RateValueTextbox weightVal = (RateValueTextbox) weight;
 						//Window.alert(new_name.getText());
 						sum += Integer.parseInt(new_name.getText()) * Integer.parseInt(weightVal.getText());
 							
 					}
 					else if( w instanceof Label)
 					{
 						Label new_name = (Label) w;						
 						Label weightVal = (Label) weight;
 						//Window.alert(new_name.getText());
 						sum += Integer.parseInt(new_name.getText()) * Integer.parseInt(weightVal.getText());
 					}
 					
 			}
 			
 				//Widget totalLabel =  matrix.getWidget(softwarePackages.size()+1, i);
 				Widget totalLabel =  matrix.getWidget(i, attributes.size()+1);
 
 				
 				RateValueLabel new_name = (RateValueLabel) totalLabel;
 		
 				new_name.setText(String.valueOf(sum));
 				//new_name.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		
 		}
 		
 		
 	}
 	private void drawMatrixPage()
 	{
 		FlexCellFormatter formatter1 = this.matrixHeader.getFlexCellFormatter();
 		this.matrixHeader.setWidth("100%");
 
 		VerticalPanel XPanel = new VerticalPanel();
 		VerticalPanel YPanel = new VerticalPanel();
 		
 		XPanel.add(new Label(messages.matrixLableX()));
 		//XPanel.add(addQualityAttribute);
 		
 		YPanel.add(new Label(messages.matrixLableY()));
 		//YPanel.add(addSoftwarePackage);
 		
 		this.matrixHeader.setWidget(1, 1, XPanel);
 		this.matrixHeader.setWidget(3, 0,YPanel);
 		this.matrixHeader.setWidget(4, 1, new Label(messages.rateLegend()));
 		//this.matrixHeader.setWidget(5, 1, cmdLoadTopTechnique);
 		
 		formatter1.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
 		formatter1.setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_CENTER);
 		formatter1.setHorizontalAlignment(2, 1, HasHorizontalAlignment.ALIGN_RIGHT);
 		formatter1.setHorizontalAlignment(3, 1, HasHorizontalAlignment.ALIGN_LEFT);
 		formatter1.setHorizontalAlignment(4, 1, HasHorizontalAlignment.ALIGN_LEFT);
 		formatter1.setHorizontalAlignment(5, 1, HasHorizontalAlignment.ALIGN_RIGHT);
 		
 		formatter1.setVerticalAlignment(3, 0, HasVerticalAlignment.ALIGN_MIDDLE);
 		
 		
 		if(isReadOnly)
 		{
 			if(GwtModesType.ReadWrite==this.currentState.getMode())
 			{
 				//this.matrixHeader.setWidget(2, 1, editRatingsLink);
 			}
 			
 			this.matrixHeader.setWidget(3, 1, matrix);
 					
 		}
 		else
 		{
 			//this.matrixHeader.setWidget(2, 1, finishRatingsLink);
 			this.matrixHeader.setWidget(3, 1, matrix);
 		}
 		
 	}
 
 	public void loadRequirementsTable()
 	{
 		//System.out.println("We're at loadRequirementTable");
 		filterRequirements(lastSearch);
 
 		vPane.clear();
 		vPane.setSpacing(0);
 		vPane.setWidth("90%");
 		vPane.setHeight("5%");
 
 		vPane.add(getRequirementHeaderRow());
 		vPane.add(vPaneData);
 		loadRequirementTableData();
 		
 	}
 
 	public void loadRequirementTableData()
 	{
 		vPaneData.clear();
 		vPaneData.setWidth("100%");
 
 		vPaneData.setSpacing(5);
 		int rowCount = 1;
 		for (GwtRequirement requirement : listOfFilteredRequirements)
 		{
 			vPaneData.add(getDataRow(rowCount, requirement));
 			rowCount++;
 		}
 
 		
 		if (rowCount == 1)
 		{
 
 			DisclosurePanel diclosure = new DisclosurePanel();
 
 			Label noRequirement = new Label(messages.noelementsFound());
 			noRequirement.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 			diclosure.add(noRequirement);
 			vPaneData.add(diclosure);
 			diclosure.setAnimationEnabled(true);
 			diclosure.setOpen(true);
 
 		}
 		
 		else
 		{
 			//loadPaginBar(); TODO if we want to do paging.
 		}
 	}
 	
 	
 	
 /*
 	public void loadPaginBar()
 	{
 		if(pageCount>1)
 		{
 			
 		HorizontalPanel pagingPanel = new HorizontalPanel();
 		
 		SquareHyperlink previousPage = new SquareHyperlink("<<");
 		SquareHyperlink nextPage = new SquareHyperlink(">>");
 		
 		if(currentPage>1)
 		{
 			pagingPanel.add(previousPage);
 			pagingPanel.add(new Label("  "));
 		}
 		for(int i=1;i<=pageCount;i++)
 		{
 			final SquareHyperlink pageLink = new SquareHyperlink(i+"");
 			pageLink.addClickHandler(new ClickHandler(){
 
 			
 				public void onClick(ClickEvent event)
 				{
 					currentPage=Integer.parseInt(pageLink.getText());
 					//navigatePaging();
 				filterRequirements(lastSearch);
 				loadRequirementTableData();
 					
 				}});
 		
 			if(currentPage==i)
 			{ 
 				pagingPanel.add(new Label(" "+i+" "));
 			}
 			else
 			{
 				pagingPanel.add(new Label(" "));
 				pagingPanel.add(pageLink);
 				pagingPanel.add(new Label(" "));
 			}
 			if(i<pageCount)
 			{
 				pagingPanel.add(new Label("|"));
 			}
 			
 			
 		}
 		
 		previousPage.addClickHandler(new ClickHandler(){
 
 			
 			public void onClick(ClickEvent event)
 			{
 				
 				moveToPreviousPage();
 				//navigatePaging();  This will force to use the history manager
 			
 				filterRequirements(lastSearch);
 				loadRequirementTableData();
 				
 			}});
 		
 		nextPage.addClickHandler(new ClickHandler(){
 
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				moveToNextPage();
 				//navigatePaging();
 				filterRequirements(lastSearch);
 				loadRequirementTableData();
 				
 			}});
 		
 		if(currentPage<pageCount)
 		{
 			pagingPanel.add(new Label("  "));
 			pagingPanel.add(nextPage);
 		}
 		
 		pagingPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		vPaneData.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		vPaneData.add(pagingPanel);
 		}
 	}
 	/*
 	public void navigatePaging()
 	{
 		String searchQuery="";
 		if(lastSearch.trim().length()!=0)
 		{
 			searchQuery="&search="+lastSearch;
 		}
 		
 		String queryString ="?PageId="+currentPage+searchQuery;
 		History.newItem(ReviewOfRequirementsByAcquisitionPilot.generateNavigationId(ReviewOfRequirementsByAcquisitionPilot.PageId.start)+queryString);
 		
 	}
 	*/
 	
 	
 	
 	public int getIndexById(int requirementId)
 	{
 		int count = 0;
 		for (GwtRequirement t : listOfFilteredRequirements)
 		{
 
 			if (t.getId().intValue() == requirementId)
 			{
 				return count;
 			}
 			count++;
 		}
 		return -1;
 	}
 	
 	public void filterRequirements(String searchRequirement)
 	{
 		
 		lastSearch=searchRequirement;
 		listOfFilteredRequirements.clear();
 		if (searchRequirement.trim().length() > 0)
 		{
 			for (GwtRequirement t : listOfRequirements)
 			{
 				if (t.getTitle().trim().toLowerCase().startsWith(searchRequirement.toLowerCase().trim()))
 				{
 					GwtRequirement filterRequirement = new GwtRequirement();
 					filterRequirement.setTitle(t.getTitle());
 					filterRequirement.setSecurity(t.isSecurity());
 					filterRequirement.setPrivacy(t.isPrivacy());
 											
 					filterRequirement.setDescription(t.getDescription());
 					filterRequirement.setPriority(t.getPriority());
 					filterRequirement.setStatus(t.getStatus());
 					filterRequirement.setId(t.getId());
 					
 					listOfFilteredRequirements.add(filterRequirement);
 				}
 			}
 		}
 		else
 		{
 			for (GwtRequirement t : listOfRequirements)
 			{
 				GwtRequirement filterRequirement = new GwtRequirement();
 
 				filterRequirement.setTitle(t.getTitle());
 				filterRequirement.setSecurity(t.isSecurity());
 				filterRequirement.setPrivacy(t.isPrivacy());
 										
 				filterRequirement.setDescription(t.getDescription());
 				filterRequirement.setPriority(t.getPriority());
 				filterRequirement.setStatus(t.getStatus());
 				filterRequirement.setId(t.getId());
 				
 				listOfFilteredRequirements.add(filterRequirement);
 
 			}
 
 		}
 		Collections.sort(listOfFilteredRequirements);
 		
 		//filterForPagin(currentPage);
 
 	}
 
 
 	public void filterForPagin(int currentPage)
 	{
 		
 		List<GwtRequirement> pageFilterRequirements= new ArrayList<GwtRequirement>();
 		pageCount=  listOfFilteredRequirements.size()/pageSize;
 		int minRequirementIndex=((currentPage-1)*pageSize);
 		int maxRequirementIndex=(((currentPage-1)*pageSize)+(pageSize-1));
 		
 		if((pageCount*pageSize)< listOfFilteredRequirements.size())
 		{
 		
 			pageCount=pageCount+1;
 			
 		}
 		
 		if(currentPage==pageCount)
 		{
 			int extraRequirements =listOfFilteredRequirements.size()-(pageCount*pageSize);
 			maxRequirementIndex=maxRequirementIndex+extraRequirements;
 		}
 		
 		if(currentPage>pageCount)
 		{
 			currentPage=1;
 		}
 		
 		
 		
 		
 		for(int i=0; i<listOfFilteredRequirements.size();i++ )
 		{
 			if(i>= minRequirementIndex && i<=maxRequirementIndex)
 			{
 				GwtRequirement req= new GwtRequirement();
 				//req.setRequirement(listOfFilteredRequirements.get(i).getRequirement());
 				req.setTitle(listOfFilteredRequirements.get(i).getTitle());
 				req.setSecurity(listOfFilteredRequirements.get(i).isSecurity());
 				req.setPrivacy(listOfFilteredRequirements.get(i).isPrivacy());
 				req.setDescription(listOfFilteredRequirements.get(i).getDescription());
 				req.setPriority(listOfFilteredRequirements.get(i).getPriority());
 				req.setStatus(listOfFilteredRequirements.get(i).getStatus());
 				req.setId(listOfFilteredRequirements.get(i).getId());
 				
 				
 				pageFilterRequirements.add(req);
 				
 			}
 			
 		}
 		
 		listOfFilteredRequirements.clear();
 		listOfFilteredRequirements=pageFilterRequirements;
 	
 		
 	}
 	
 	public void moveToNextPage()
 	{
 		if(currentPage<pageCount)
 		{
 			currentPage++;
 		}
 		
 	}
 	
 	public void moveToPreviousPage()
 	{
 		if(currentPage>1)
 		{
 			currentPage--;
 		}
 		
 	}
 	
 	public Widget getCotsTableHeaderRow()
 	{
 
 		FlexTable cotsTable = new FlexTable();
 		
 		cotsTable.setWidth("100%");
 		cotsTable.setCellSpacing(0);
 	
 		cotsTable.setWidget(0, 0, new Label("  "));
 		cotsTable.setWidget(2, 1, new Label(messages.cotsTableTitle()));
 				
 		return cotsTable;
 	}
 	
 	
 	public Widget getRequirementHeaderRow()
 	{
 
 		//FlexTable requirementTitleTable = new FlexTable();
 		FlexTable searchTable = new FlexTable();
 		
 		//requirementTitleTable.setWidth("100%");
 		//requirementTitleTable.setCellSpacing(0);
 		searchTable.setWidth("100%");
 		searchTable.setCellSpacing(0);
 	
 		//requirementTitleTable.setWidget(0, 0, new Label(messages.requirementTableTitle()));
 		
 		//requirementTitleTable.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
 		//requirementTitleTable.getCellFormatter().setStyleName(0, 0, "square-Categorize-TableHeader");
 		
 		searchTable.setWidget(0, 0, new Label(messages.requirementTableTitle()));
 		
 		searchTable.setWidget(1, 0, new Label(messages.requirementTableBarTitle()));
 		searchTable.setWidget(1, 1, new Label(messages.requirementTableBarDescription()));
 		searchTable.setWidget(1, 2, new Label(messages.requirementTableBarStatus()));
 		searchTable.setWidget(1, 3, new Label("      "));
 		
 		searchTable.getCellFormatter().setWidth(1, 0, "20%");
 		searchTable.getCellFormatter().setWidth(1, 1, "60%");
 		searchTable.getCellFormatter().setWidth(1, 2, "15%");
 		searchTable.getCellFormatter().setWidth(1, 3, "20%");
 		
 		searchTable.getCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER);
 		searchTable.getCellFormatter().setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_CENTER);
 		searchTable.getCellFormatter().setHorizontalAlignment(1, 2, HasHorizontalAlignment.ALIGN_CENTER);
 		searchTable.getCellFormatter().setHorizontalAlignment(1, 3, HasHorizontalAlignment.ALIGN_CENTER);
 
 		
 		searchTable.getCellFormatter().setStyleName(1, 0, "square-Categorize-TableHeader");
 		searchTable.getCellFormatter().setStyleName(1, 1, "square-Categorize-TableHeader");
 		searchTable.getCellFormatter().setStyleName(1, 2, "square-Categorize-TableHeader");
 		searchTable.getCellFormatter().setStyleName(1, 3, "square-Categorize-TableHeader");
 
 		return searchTable;
 	}
 
 	public Widget getDataRow(int rowCount, GwtRequirement req)
 	{
 		FlexTable rowTable = new FlexTable();
 		rowTable.setStyleName("square-agree-on-definition-flex"); //ASQUARE change!!!!
 		rowTable.setCellSpacing(4);
 		rowTable.setSize("100%", "50px");
 
 		Label reqTitleLabel = new Label(req.getTitle());
 		Label reqDescriptionLabel = new Label(req.getDescription());
 		Label reqStatusLabel = new Label(req.getStatus());
 		
 		
 		final SummaryElementHyperLinkElement viewDetailLink = new SummaryElementHyperLinkElement(req.getId(), "View Detail");
 		//final SummaryElementHyperLinkElement hyper2 = new SummaryElementHyperLinkElement(req.getId(), "Remove");
 
 		/*
 		hyper2.addClickHandler(new ClickHandler()
 			{
 
 				@Override
 				public void onClick(ClickEvent event)
 				{
 					boolean response = Window.confirm(messages.confirmDelete() + "?");
 					if (response)
 					{
 						GwtRequirement requirement = new GwtRequirement();
 						requirement.setId(hyper2.getrequirementirementId());
 						removeRequirement(requirement);
 					}
 				}
 			});
 			*/
 
 		if (this.getCurrentState().getMode().equals(GwtModesType.ReadOnly)||this.getCurrentState().getMode().equals(GwtModesType.ReadWrite))
 		{
 			HorizontalPanel links = new HorizontalPanel();
 			links.setStyleName("flex-link-bar");
 			links.add(viewDetailLink);
 			// links.add(new Label(" "));
 			//links.add(hyper2);
 			rowTable.setWidget(0, 3, links);
 		}
 		else
 		{
 			rowTable.setWidget(0, 2, new Label(" "));
 		}
 
 		//final ReviewOfRequirementsByAcquisitionPane reviewOfRequirementsByAcquisitionObject = this;
 
 //view detail hyperlink
 		viewDetailLink.addClickHandler(new ClickHandler()
 			{
 
 				@Override
 				public void onClick(ClickEvent event)
 				{
 
 					currentState.setCurrentRisk(viewDetailLink.getRequirementId());
 					currentState.setRiskCommand(1);
 					History.newItem(ReviewAndFinalizeRequirementsPilot.generateNavigationId(ReviewAndFinalizeRequirementsPilot.PageId.requirementDetail));
 
 				}
 	
 			});
 		int addedItemIndex = getIndexById(modifyRequirementId);
 
 		if (addedItemIndex != -1 && (addedItemIndex + 1) == rowCount)
 		{
 			yellowFadeHandler.add(rowTable);
 			modifyRequirementId = -1;
 
 		}
 
 		rowTable.setWidget(0, 0, reqTitleLabel);
 		rowTable.setWidget(0, 1, reqDescriptionLabel);
 		rowTable.setWidget(0, 2, reqStatusLabel);
 
 		rowTable.getCellFormatter().setWidth(0, 0, "20%");
 		rowTable.getCellFormatter().setWidth(0, 1, "60%");
 		rowTable.getCellFormatter().setWidth(0, 2, "15%");
 		rowTable.getCellFormatter().setWidth(0, 3, "20%");
 		
 		rowTable.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_CENTER);
 		rowTable.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
 
 		rowTable.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
 		rowTable.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT);
 		rowTable.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_CENTER);
 
 		rowTable.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
 		rowTable.getCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);
 		rowTable.getCellFormatter().setVerticalAlignment(0, 2, HasVerticalAlignment.ALIGN_TOP);
 
 		return rowTable;
 
 	}
 
 	public GwtRequirement getRequirementById(int id)
 	{
 		for (GwtRequirement t : listOfRequirements)
 		{
 			if (t.getId().intValue() == id)
 			{
 				return t;
 			}
 		}
 		return null;
 	}
 
 
 	// This commands are triggered from the dialog boxes
 	public void createCommand(GwtRequirement req)
 	{
 		GwtProject gwtProject = new GwtProject();
 		gwtProject.setId(projectID);
 		//addRequirement(requirement, gwtProject);
 
 	}
 
 	public void updateCommand(GwtRequirement requirement)
 	{
 
 		GwtProject gwtProject = new GwtProject();
 		gwtProject.setId(projectID);
 		updateRequirement(requirement, gwtProject);;
 	}
 	
 	private void addDoneButton()
 	{
 		FlexTable buttonPanel = new FlexTable();
 		Button done =new Button(messages.done());
 		done.addStyleName("square-button");
 		done.addClickHandler(new ClickHandler(){
 
 			public void onClick(ClickEvent event)
 			{
 				History.newItem(ChooseStepCase3Pilot.generateNavigationId(ChooseStepCase3Pilot.PageId.home));
 				
 			}});
 		buttonPanel.setWidget(0,0,new Label(" "));
		buttonPanel.setWidget(0,1,new Label(" "));
		buttonPanel.setWidget(1,1,new Label(" "));
		buttonPanel.setWidget(2,1,new Label(" "));
		buttonPanel.setWidget(2,2,done);
		buttonPanel.setWidth("100%");
		done.setWidth("10%");
		buttonPanel.getCellFormatter().setHorizontalAlignment(2,2,HasHorizontalAlignment.ALIGN_RIGHT);
		this.vPaneCotsData.add(buttonPanel);
 	}
 
 	class SummaryElementHyperLinkElement extends SquareHyperlink
 	{
 
 		private int requirementID;
 		public SummaryElementHyperLinkElement(int requirementID, String text)
 			{
 				super(text);
 				this.requirementID = requirementID;
 			}
 
 		public void setRequirementID(int requirementID)
 		{
 			this.requirementID = requirementID;
 		}
 
 		public int getRequirementId()
 		{
 			return requirementID;
 		}
 
 	}
 	
 	/**
 	 * 
 	 * the techniqueID as properties
 	 *
 	 */
 	class RateValueLabel extends Label
 	{
 		private int tecniqueID;
 		public RateValueLabel(int tecniqueID) {
 		
 			this.tecniqueID=tecniqueID;
 			
 			this.setWidth("35px");
 		}
 		
 		public void setTecniqueID(int tecniqueID) {
 			this.tecniqueID = tecniqueID;
 		}
 		public int getTecniqueID() {
 			return tecniqueID;
 		}
 
 	}
 	/**
 	 * 
 	 * This class extend the texbox to add 
 	 * the techniqueID and evaluationID as properties
 	 *
 	 */
 	class RateValueTextbox extends TextBox
 	{
 		private int tecniqueID;
 		private int evaluationID;
 		private String oldValue= "-1";
 		public RateValueTextbox(int tecniqueID, int evaluationID, int value) {
 		
 			this.tecniqueID=tecniqueID;
 			this.evaluationID=evaluationID;
 			
 			this.setWidth("35px");
 			this.setText(String.valueOf(value));;
 			this.setOldValue(String.valueOf(value));;
 			
 		}
 		public void setTecniqueID(int tecniqueID) {
 			this.tecniqueID = tecniqueID;
 		}
 		public int getTecniqueID() {
 			return tecniqueID;
 		}
 		public void setEvaluationID(int evaluationID) {
 			this.evaluationID = evaluationID;
 		}
 		public int getEvaluationID() {
 			return evaluationID;
 		}
 		public void setOldValue(String oldValue) {
 			this.oldValue = oldValue;
 		}
 		public String getOldValue() {
 			return oldValue;
 		}
 	}
 
 
 }
