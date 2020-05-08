 package com.scholastic.sbam.client.uiobjects.uiapp;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.Style.SelectionMode;
 import com.extjs.gxt.ui.client.Style.SortDir;
 import com.extjs.gxt.ui.client.data.BasePagingLoader;
 import com.extjs.gxt.ui.client.data.BeanModel;
 import com.extjs.gxt.ui.client.data.BeanModelReader;
 import com.extjs.gxt.ui.client.data.LoadConfig;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.data.PagingLoadConfig;
 import com.extjs.gxt.ui.client.data.PagingLoadResult;
 import com.extjs.gxt.ui.client.data.PagingLoader;
 import com.extjs.gxt.ui.client.data.RpcProxy;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.ComponentEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.GridEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.util.KeyNav;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.Html;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.button.ToolButton;
 import com.extjs.gxt.ui.client.widget.form.FieldSet;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.LabelField;
 import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.grid.GridView;
 import com.extjs.gxt.ui.client.widget.grid.RowExpander;
 import com.extjs.gxt.ui.client.widget.grid.filters.DateFilter;
 import com.extjs.gxt.ui.client.widget.grid.filters.GridFilters;
 import com.extjs.gxt.ui.client.widget.grid.filters.NumericFilter;
 import com.extjs.gxt.ui.client.widget.grid.filters.StringFilter;
 import com.extjs.gxt.ui.client.widget.layout.CardLayout;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.layout.FormData;
 import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
 import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.scholastic.sbam.client.services.AgreementTermGetService;
 import com.scholastic.sbam.client.services.AgreementTermGetServiceAsync;
 import com.scholastic.sbam.client.services.AgreementTermSearchService;
 import com.scholastic.sbam.client.services.AgreementTermSearchServiceAsync;
 import com.scholastic.sbam.client.uiobjects.fields.BoundDateField;
 import com.scholastic.sbam.client.uiobjects.fields.DateRangeBinder;
 import com.scholastic.sbam.client.uiobjects.fields.EnhancedComboBox;
 import com.scholastic.sbam.client.uiobjects.foundation.AppSleeper;
 import com.scholastic.sbam.client.uiobjects.foundation.FieldFactory;
 import com.scholastic.sbam.client.uiobjects.foundation.GridSupportPortlet;
 import com.scholastic.sbam.client.util.IconSupplier;
 import com.scholastic.sbam.client.util.UiConstants;
 import com.scholastic.sbam.shared.exceptions.ServiceNotReadyException;
 import com.scholastic.sbam.shared.objects.AgreementTermInstance;
 import com.scholastic.sbam.shared.objects.AgreementTermTuple;
 import com.scholastic.sbam.shared.objects.GenericCodeInstance;
 import com.scholastic.sbam.shared.objects.SynchronizedPagingLoadResult;
 import com.scholastic.sbam.shared.util.AppConstants;
 
 public class AgreementTermSearchPortlet extends GridSupportPortlet<AgreementTermTuple> implements AppSleeper, AppPortletRequester {
 	
 	protected static final int LOAD_LIMIT			= AppConstants.STANDARD_LOAD_LIMIT;
 	protected static final int FILTER_LISTEN_PERIOD = 500;	//	This is a little higher, because we're doing a database scan on the back end, so it's not very fast
 	
 	protected final AgreementTermSearchServiceAsync	agreementTermSearchService  = GWT.create(AgreementTermSearchService.class);
 	protected final AgreementTermGetServiceAsync    agreementTermGetService		= GWT.create(AgreementTermGetService.class);
 //	protected final AgreementGetServiceAsync    	agreementGetService			= GWT.create(AgreementGetService.class);
 	
 	protected CardLayout						cards;
 	protected ContentPanel						searchPanel;
 	protected FormPanel							displayCard;
 	protected Grid<ModelData>					grid;
 //	protected LiveGridView						liveView;		LiveView removed because local filters don't work with it
 	protected GridView							gridView;
 	
 	protected RowExpander						noteExpander;
 	
 	protected ListStore<ModelData>				store;
 	protected TextField<String>					filterField;
 	protected EnhancedComboBox<BeanModel>		dateTypeField;
 	protected BoundDateField					fromDateField;
 	protected BoundDateField					toDateField;
 	protected DateRangeBinder					startEndRangeBinder;
 	protected Timer								filterListenTimer;
 	protected String							filter = "";
 	protected Date								fromDate = new Date();
 	protected Date								toDate = new Date();
 	protected ListStore<BeanModel>				dateTypeStore;
 	protected GenericCodeInstance				dateType = new GenericCodeInstance("end", "End Date");
 	protected GridFilters						columnFilters;
 	
 	protected PagingLoader<PagingLoadResult<AgreementTermTuple>> agreementLoader;
 
 	protected LabelField						agreementIdField;
 	protected LabelField						agreementTypeField;
 	protected LabelField						linkIdField;
 	protected LabelField						ucnField;
 	protected LabelField						addressField;
 	protected LabelField						customerTypeField;
 	protected LabelField						altIds;
 	protected ListStore<ModelData>				agreementTermsStore;
 	protected Grid<ModelData>					agreementTermsGrid;
 	protected FieldSet							agreementsFieldSet;
 	
 	protected AppPortletProvider				portletProvider;
 	
 	protected int								focusAgreementId;
 	protected int								focusAgreementTermId;
 	protected AgreementTermTuple				focusAgreementTerm;
 	
 	protected long								searchSyncId = 0;
 	
 	public AgreementTermSearchPortlet() {
 		super();
 	}
 	
 	public AgreementTermSearchPortlet(String helpTextId) {
 		super(helpTextId);
 	}
 
 	@Override  
 	protected void onRender(Element parent, int index) {
 		super.onRender(parent, index);
 		
 		setPortletRenderValues();
 		
 		setLayout(new FitLayout());
 		LayoutContainer outerContainer = new LayoutContainer();
 		add(outerContainer);
 		
 		cards = new CardLayout();
 		outerContainer.setLayout(cards);
 		
 		//	We need this to be able to put the tool bar into the top
 		searchPanel = new ContentPanel(new FitLayout());
 		searchPanel.setHeaderVisible(false);
 		searchPanel.setBorders(false);
 //		searchPanel.setHeight(540);
 		
 		setThis();
 		addGrid();
 		setFilter();
 		initializeFilter();
 		
 		outerContainer.add(searchPanel);
 		
 		createDisplayCard();
 		outerContainer.add(displayCard);
 		
 		if (focusAgreementId > 0)
 			loadAgreementTerm(focusAgreementId, focusAgreementTermId);
 		if (filter != null && filter.length() > 0)
 			loadFiltered(filter, dateType, fromDate, toDate);
 	}
 	
 	public void setPortletRenderValues() {
 		setHeading("Agreement Term Search");
 		setToolTip(UiConstants.getQuickTip("Use this portlet to search for agreement terms by date and product."));
 	}
 	
 	@Override
 	public String getPresenterToolTip() {
 		if (focusAgreementTerm != null) {
 			return "ID " + AppConstants.appendCheckDigit(focusAgreementTerm.getAgreement().getId()) + "-" + focusAgreementTerm.getAgreementTerm().getTermId();
 		}
 		if (focusAgreementId != 0 && focusAgreementTermId != 0)
 			if (filter != null && filter.length() > 0)
 				return "ID " + focusAgreementId + "-" + focusAgreementTermId + " found for '" + filter + "'";
 			else
 				return "ID " + focusAgreementId + "-" + focusAgreementTermId;
 		if (filter != null && filter.length() > 0)
 			return "Agreement Search for '" + filter + "'";
 		return "Search for agreement terms.";
 	}
 	
 	public void initializeFilter() {
 		filterField.setValue(filter);
 		if (dateType != null) dateTypeField.setValue(GenericCodeInstance.obtainModel(dateType));
 		startEndRangeBinder.setUnbound();
 		if (fromDate != null) fromDateField.setValue(fromDate);
 		if (toDate != null) toDateField.setValue(toDate);
 		startEndRangeBinder.setBound();
 	}
 	
 	private void createDisplayCard() {
 		FormData formData = new FormData("100%");
 		displayCard = new FormPanel();
 
 		displayCard.setPadding(10);  
 		displayCard.setFrame(true); 
 		displayCard.setHeaderVisible(false);  
 		displayCard.setBodyBorder(true);
 		displayCard.setBodyStyleName("subtle-form");
 		displayCard.setButtonAlign(HorizontalAlignment.CENTER);
 		displayCard.setLabelAlign(LabelAlign.RIGHT);
 		displayCard.setLabelWidth(100); 
 		
 		ToolButton returnTool = new ToolButton("x-tool-left") {
 				@Override
 				protected void onClick(ComponentEvent ce) {
 					cards.setActiveItem(searchPanel);
 					focusAgreementTerm = null;
 					focusAgreementId = 0;
 					focusAgreementTermId = 0;
 					updateUserPortlet();
 					updatePresenterLabel();
 				}
 			};
 		returnTool.enable();
 		
 		ToolBar displayBar = new ToolBar();
 		displayBar.add(returnTool);
 		displayBar.add(new SeparatorToolItem());
 		displayBar.add(new Html("<b>Selected Agreement</b>"));
 		displayCard.setTopComponent(displayBar);
 
 		agreementIdField = new LabelField();
 		agreementIdField.setFieldLabel("ID");
 		displayCard.add(agreementIdField, formData);
 
 		agreementTypeField = new LabelField();
 		agreementTypeField.setFieldLabel("Type");
 		displayCard.add(agreementTypeField, formData);
 		
 		ucnField = new LabelField();  
 		ucnField.setFieldLabel("UCN :");
 		displayCard.add(ucnField, formData);
 		
 		addressField = new LabelField(); 
 		displayCard.add(addressField, formData); 
 		
 		altIds = new LabelField(); 
 		altIds.setFieldLabel("Alternate IDs :");
 		displayCard.add(altIds, formData); 
 		
 		customerTypeField = new LabelField(); 
 		customerTypeField.setFieldLabel("Customer Type :");
 		displayCard.add(customerTypeField, formData);
 		
 		addAgreementTermsGrid(formData);
 		addButtons(formData);
 		
 //		Button returnButton = new Button("Return");
 //		returnButton.addSelectionListener(new SelectionListener<ButtonEvent>() {  
 //				@Override  
 //					public void componentSelected(ButtonEvent ce) {
 //						cards.setActiveItem(searchPanel);
 //					}  
 //			});
 //		displayCard.addButton(returnButton);
 	}
 	
 
 	
 	protected void addAgreementTermsGrid(FormData formData) {
 		List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
 		
 		columns.add(getDisplayColumn("productCode",			"Product Code",				100,
 					"This is the product code for this term."));
 		columns.add(getDisplayColumn("product.description",	"Product",					250,
 					"This is the product for this term."));
 		columns.add(getDisplayColumn("startDate",			"Start",					80,		true, UiConstants.APP_DATE_TIME_FORMAT,
 					"This is the start date for this term."));
 		columns.add(getDisplayColumn("endDate",				"End",						80,		true, UiConstants.APP_DATE_TIME_FORMAT,
 					"This is the end date for this term."));
 		columns.add(getDisplayColumn("terminateDate",		"Siteinate",				80,		true, UiConstants.APP_DATE_TIME_FORMAT,
 					"This is the terminate date for this term."));
 		columns.add(getDisplayColumn("dollarValue",			"Value",					80,		true, UiConstants.DOLLARS_FORMAT,
 					"This is the dollar value for this term."));
 		
 		ColumnModel cm = new ColumnModel(columns);  
 
 		agreementTermsStore = new ListStore<ModelData>();
 		
 		agreementTermsGrid = new Grid<ModelData>(agreementTermsStore, cm);  
 		agreementTermsGrid.setBorders(true);
 		agreementTermsGrid.setHeight(180);
 		agreementTermsGrid.setStripeRows(true);
 		agreementTermsGrid.setColumnLines(true);
 		agreementTermsGrid.setHideHeaders(false);
 		agreementTermsGrid.setWidth(cm.getTotalWidth() + 5);
 		
 		//	Open a new portlet to display an agreement when a row is selected
 		agreementTermsGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); 
 		final AppPortlet thisPortlet = this; 
 		agreementTermsGrid.getSelectionModel().addListener(Events.SelectionChange,  
 				new Listener<SelectionChangedEvent<ModelData>>() {  
 					public void handleEvent(SelectionChangedEvent<ModelData> be) {  
 						if (be.getSelection().size() > 0) {
 						//	System.out.println("Agreement " + ((BeanModel) be.getSelectedItem()).get("idCheckDigit"));
 							AgreementTermInstance agreementTerm = (AgreementTermInstance) ((BeanModel) be.getSelectedItem()).getBean();
 							AgreementPortlet portlet = (AgreementPortlet) portletProvider.getPortlet(AppPortletIds.AGREEMENT_DISPLAY);
 							portlet.setAgreementId(agreementTerm.getAgreementId());
 							if (focusAgreementTerm != null) {
 								String foundFor = constructFilterDescription();
 								portlet.setIdentificationTip("Found for " + foundFor + "");
 							}
 //							Old, simple way
 //							int insertCol = (portalColumn == 0) ? 1 : 0;
 //							portletProvider.insertPortlet(portlet, portalRow, insertCol);
 //							New, more thorough way
 							portletProvider.insertPortlet(portlet, portalRow, thisPortlet.getInsertColumn());
 							agreementTermsGrid.getSelectionModel().deselectAll();
 						} 
 					}
 			});
 	
 		agreementsFieldSet = new FieldSet();
 		agreementsFieldSet.setBorders(true);
 		agreementsFieldSet.setHeading("Current and Pending Terms");// 		displayCard.add(new LabelField("<br/><i>Existing Agreements</i>"));
 		agreementsFieldSet.setCollapsible(true);
 		agreementsFieldSet.setToolTip(UiConstants.getQuickTip("These are the current or pending agreement terms for this agreement."));
 		agreementsFieldSet.add(agreementTermsGrid);	//, new FormData(cm.getTotalWidth() + 10, 200));
 		
 	//	displayCard.add(new LabelField(""));	// Used as a spacer
 		displayCard.add(agreementsFieldSet, formData);	//	new FormData("95%")); // new FormData(cm.getTotalWidth() + 20, 200));
 		
 	}
 	
 	public String constructFilterDescription() {
 		return filter;
 	}
 	
 	protected void addButtons(FormData formData) {
 		
 		ToolBar toolBar = new ToolBar();
 		toolBar.setAlignment(HorizontalAlignment.CENTER);
 		toolBar.setToolTip(UiConstants.getQuickTip("Use these buttons to edit this agreement."));
 		
 		Button newAgreementButton = new Button("Edit Agreement");
 		newAgreementButton.setToolTip(UiConstants.getQuickTip("Use this button to edit this agreement."));
 		IconSupplier.forceIcon(newAgreementButton, IconSupplier.getNewIconName());
 		newAgreementButton.addSelectionListener(new SelectionListener<ButtonEvent>() {  
 				@Override
 				public void componentSelected(ButtonEvent ce) {
 					AgreementPortlet portlet = (AgreementPortlet) portletProvider.getPortlet(AppPortletIds.AGREEMENT_DISPLAY);
 					portlet.setAgreementId(focusAgreementId);
 					int insertCol = (portalColumn == 0) ? 1 : 0;
 					portletProvider.insertPortlet(portlet, portalRow, insertCol);
 				}  
 			});
 		toolBar.add(newAgreementButton);
 		
 		displayCard.add(toolBar);
 	}
 	
 	protected void showAgreement(BeanModel model) {
 		showAgreementTerm((AgreementTermTuple) model.getBean());
 		// If the agreement term came without a set of other terms, load it again (with the terms)...
 		if (focusAgreementTerm.getAgreement().getAgreementTerms() == null)
 			loadAgreementTerm(focusAgreementId, focusAgreementTermId);
 	}
 	
 	protected void showAgreementTerm(AgreementTermTuple instance) {
 		focusAgreementTerm = instance;
 		
 		if (instance == null)
 			return;
 		
 		focusAgreementId = focusAgreementTerm.getAgreementTerm().getAgreementId();
 		focusAgreementTermId = focusAgreementTerm.getAgreementTerm().getTermId();
 		
 		registerUserCache(focusAgreementTerm, "Found for " + filter);
 		updateUserPortlet();
 
 		agreementIdField.setValue(focusAgreementTerm.getAgreement().getIdCheckDigit());
 		agreementTypeField.setValue(focusAgreementTerm.getAgreement().getAgreementType().getDescriptionAndCode());
 		ucnField.setValue(focusAgreementTerm.getAgreement().getBillUcn());
 		
 		if (focusAgreementTerm.getAgreement().getInstitution() == null) {
 			addressField.setValue("");
 			altIds.setValue("");
 			customerTypeField.setValue("");
 		} else {
 			addressField.setValue(focusAgreementTerm.getAgreement().getInstitution().getHtmlAddress());
 			if (focusAgreementTerm.getAgreement().getInstitution().getAlternateIds() == null 
 			||  focusAgreementTerm.getAgreement().getInstitution().getAlternateIds().length() == 0)
 				altIds.setValue("None");
 			else
 				altIds.setValue(focusAgreementTerm.getAgreement().getInstitution().getAlternateIds().replace(",", ", "));
 			customerTypeField.setValue(focusAgreementTerm.getAgreement().getInstitution().getPublicPrivateDescription() + " / " + 
 					focusAgreementTerm.getAgreement().getInstitution().getGroupDescription() + " &rArr; " + 
 					focusAgreementTerm.getAgreement().getInstitution().getTypeDescription());
 		}
 		
 		agreementTermsStore.removeAll();
 		if (focusAgreementTerm.getAgreement().getAgreementTerms() != null)
 			for (AgreementTermInstance term : focusAgreementTerm.getAgreement().getAgreementTerms())
 				agreementTermsStore.add(AgreementTermInstance.obtainModel(term));
 		
 		cards.setActiveItem(displayCard);
 
 		if (presenter != null)
 			presenter.updateLabel(this);
 		
 	}
 
 //	@Override
 //	protected BeanModel getModel(AgreementTermInstance instance) {
 //		BeanModel model = super.getModel(instance);
 //		String display = "<b>" + instance.getId() + "</b> : " + instance.getLastStartDate() + " &ndash; " + instance.getEndDate();
 //		if (!instance.getFirstStartDate().equals(instance.getLastStartDate()))
 //			display += " (since " + instance.getFirstStartDate() + ")";
 //		model.set("display", display);
 //		return model;
 //	}
 	
 	protected ListStore<BeanModel> getDateTypeStore() {
 		if (dateTypeStore != null)
 			return dateTypeStore;
 		return UiConstants.getGenericCodeStore(new String [] [] { {"start", "Start Date"}, {"end", "End Date"}, {"terminate", "Terminate"} });
 	}
 	
 	protected void setFilter() {
 		
 		ToolBar toolBar = new ToolBar();
 		toolBar.setAlignment(HorizontalAlignment.LEFT);
 		toolBar.getAriaSupport().setLabel("Filters");
 
 //		LiveToolItem removed because it requires a LiveGridView, which doesn't allow for local filtering
 //		LiveToolItem item = new LiveToolItem();  
 //		item.bindGrid(grid); 
 //		toolBar.add(item); 
 		
 		dateTypeField = FieldFactory.getComboField("dateType", "Date", 80, getDateTypeStore(), "name");
 		dateTypeField.setWidth(80);
 		dateTypeField.enable();
 		dateTypeField.setAllowBlank(false);
 //		dateTypeField.setEditable(false);
 		dateTypeField.setForceSelection(true);
 		if (dateType != null)
 			dateTypeField.setValue(dateTypeField.getStore().findModel(dateType.getCode()));
 		else
 			dateTypeField.setValue(dateTypeField.getStore().getAt(0));
 		toolBar.add(new LabelToolItem("Type: "));
 		toolBar.add(dateTypeField);
 		
 		Date todayDate = getTodayDate();
 		
 		fromDateField = FieldFactory.getBoundDateField("From");
 		fromDateField.setValue(todayDate);
 		fromDateField.enable();
 		toolBar.add(new LabelToolItem("From: "));
 		toolBar.add(fromDateField);
 		
 		toDateField = FieldFactory.getBoundDateField("To");
 		toDateField.setValue(todayDate);
 		toDateField.enable();toolBar.add(new LabelToolItem("To: "));
 		toolBar.add(toDateField);
 		
 		startEndRangeBinder = new DateRangeBinder();
 		fromDateField.bindLow(startEndRangeBinder);
 		toDateField.bindHigh(startEndRangeBinder);
 		
 //		toolBar.add(new FillToolItem());  
 		
 		toolBar.add(new LabelToolItem("Filter by: "));
 		
 		filterField = getFilterBox();
 		filterField.getAriaSupport().setLabelledBy(toolBar.getItem(0).getId());
 //		filter.setRawValue(filter);
 		toolBar.add(filterField);
 		
 		searchPanel.setTopComponent(toolBar);
 	}
 	
 	public Date getTodayDate() {
 		Date today = new Date();
 		
 		DateTimeFormat format = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_MEDIUM);	//field.getPropertyEditor().getFormat();
 
 	    try {
 		    String value = format.format(today);
 	    	return format.parse(value);	//	field.getPropertyEditor().convertStringValue(value);
 	    } catch (Exception e) {
 	    	return new Date();
 	    }
 	}
 	
 	protected TextField<String> getFilterBox() {
 
 		TextField<String> filterField = new TextField<String>();  
 		filterField.setWidth(150); 
 		filterField.setEmptyText("Enter search criteria here...");
 		
 		setFilterListenTimer(filterField);	// This method sends messages using a timer... it is less responsive, but so bothers the server less, and is a little more reliable
 		
 		return filterField;
 	}
 	
 	protected void setFilterListenTimer(final TextField<String> filterField) {
 		filterListenTimer = new Timer() {
 			  @Override
 			  public void run() {
 			//	  System.out.println("Filter: " + filter);
 			//	  System.out.println(combo.getRawValue() +  " / " + combo.getValue() + " / " + combo.getOriginalValue());
 				  String value = (filterField.getRawValue() == null)?"":filterField.getRawValue().trim();
 				  GenericCodeInstance newDateType = dateTypeField.getValue().getBean();
 				  Date   newFromDate = fromDateField.getValue();
 				  Date	 newToDate = toDateField.getValue();
 				  if (dateType != null && fromDate != null && toDate != null) {
 					  if (!value.equals(filter.trim())
 					  ||  !newFromDate.equals(fromDate)
 					  ||  !newToDate.equals(toDate)
 					  ||  !newDateType.getCode().equals(dateType.getCode()))
 						  loadFiltered(filterField.getRawValue(), newDateType, newFromDate, newToDate);
 				  }
 			  }
 			};
 
 			filterListenTimer.scheduleRepeating(FILTER_LISTEN_PERIOD);
 	}
 	
 	protected void addGrid() {
 		agreementLoader = getAgreementLoader(); 
 
 		agreementLoader.setSortDir(SortDir.ASC);  
 		agreementLoader.setSortField("id");  
 
 		agreementLoader.setRemoteSort(false);
 
 		store = new ListStore<ModelData>(agreementLoader); 
  
 		List<ColumnConfig> columns = new ArrayList<ColumnConfig>();  
 		     
 //		ColumnConfig name = new ColumnConfig("id", "Agreement #", 80);  
 //		name.setRenderer(new GridCellRenderer<ModelData>() {  
 //
 //		  public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,  
 //		      ListStore<ModelData> store, Grid<ModelData> grid) {  
 //		    return "<b>"  
 //		        + model.get("institutionName")  
 //		        + "</b>";  
 //		  }  
 //
 //		});
 		
 		columns.add(getDisplayColumn("agreement.idCheckDigit",					"Agreement #",		80));  
 		columns.add(getDisplayColumn("agreement.institution.institutionName",	"Institution",		150));   
 		columns.add(getDisplayColumn("agreementTerm.product.description",		"Product",			150));
 		columns.add(getDisplayColumn("agreementTerm.termType.description",		"Type",				80));
 		columns.add(getDisplayColumn("agreementTerm.dollarValue",				"Value",			50,		true, UiConstants.DOLLARS_FORMAT));
 		columns.add(getDisplayColumn("agreementTerm.startDate",					"Start",		 	70,		true, UiConstants.APP_DATE_TIME_FORMAT)); 
 		columns.add(getDisplayColumn("agreementTerm.endDate",					"End",			 	70,		true, UiConstants.APP_DATE_TIME_FORMAT)); 
 
 		//	Hidden agreement term columns
		columns.add(getHiddenColumn("agreementTerm.terminateDate",				"Terminate",		 70,		true, UiConstants.APP_DATE_TIME_FORMAT)); 
				//	Hidden institution columns
 		columns.add(getHiddenColumn("agreement.institution.state",						"State",			50));
 		columns.add(getHiddenColumn("agreement.institution.country",					"Country",			50));    
 		columns.add(getHiddenColumn("agreement.institution.typeCode",					"Cust Type Code", 	50));
 		columns.add(getHiddenColumn("agreement.institution.typeDescription",			"Cust Type", 		100,			false));    
 		columns.add(getHiddenColumn("agreement.institution.groupCode",					"Type Group Code", 	50));  
 		columns.add(getHiddenColumn("agreement.institution.groupDescription",			"Type Group", 		100,			false));    
 		columns.add(getHiddenColumn("agreement.institution.publicPrivateCode",			"Public/Private", 	50));  
 		columns.add(getHiddenColumn("agreement.institution.publicPrivateDescription",	"Public/Private Desc", 	100,	false));
 		
 		noteExpander = getNoteExpander("agreementTermNote");
 		columns.add(noteExpander);
 		
 		ColumnModel cm = new ColumnModel(columns);  
 
 		grid = new Grid<ModelData>(store, cm);  
 		grid.setBorders(true);  
 		grid.setAutoExpandColumn("agreementTerm.product.description");  
 		grid.setLoadMask(true);
 		grid.setStripeRows(true);
 		grid.setColumnLines(true);
 		
 		//	Switch to the display card when a row is selected
 		grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); 
 		addRowListener(grid);
 		
 		addLocalGridFilters();
 
 //		LiveGridView replaced with GridView because local filters don't work with a LiveGridView
 //		liveView = new LiveGridView();  
 //		liveView.setEmptyText("Enter filter criteria to search for agreements.");
 //		liveView.setCacheSize(100);
 //		grid.setView(liveView);
 		
 		gridView = new GridView();
 		gridView.setEmptyText("Enter filter criteria to search for agreements.");
 		grid.setView(gridView);
 		grid.getAriaSupport().setLabelledBy(this.getHeader().getId() + "-label"); 
 		
 		addGridPlugins(grid);
 		
 		searchPanel.add(grid);
 		  
 	}
 	
 	protected void addLocalGridFilters() {
 		columnFilters = new GridFilters();  
 		columnFilters.setLocal(true);
 		columnFilters.setAutoReload(false);
 		
 		columnFilters.addFilter(new NumericFilter("agreement.idCheckDigit"));
 		columnFilters.addFilter(new NumericFilter("agreementTerm.dollarValue"));
 		columnFilters.addFilter(new DateFilter("agreementTerm.startDate"));
 		columnFilters.addFilter(new DateFilter("agreementTerm.endDate"));
 		columnFilters.addFilter(new StringFilter("agreement.institution.institutionName"));
 		columnFilters.addFilter(new StringFilter("agreementTerm.product.description"));
 		columnFilters.addFilter(new StringFilter("agreementTerm.termType.description"));
 		
 		grid.addPlugin(columnFilters);
 	}
 	
 	public void addGridPlugins(Grid<ModelData> grid) {
 		grid.addPlugin(noteExpander);
 	}
 	
 	protected void addRowListener(Grid<?> grid) {
 		grid.addListener(Events.RowClick, new Listener<GridEvent<?>>() {
 		      public void handleEvent(GridEvent<?> be) {
 		    	  onRowSelected(be);
 		      }
 		    });
 		grid.addListener(Events.SelectionChange, new Listener<GridEvent<?>>() {
 		      public void handleEvent(GridEvent<?> be) {
 		    	  onRowSelected(be);
 		      }
 		    });
 		
 		new KeyNav<GridEvent<?>>(grid) {
 		      @Override
 		      public void onUp(GridEvent<?> ce) {
 		        onRowSelected((BeanModel) ce.getGrid().getSelectionModel().getSelectedItem());
 		      }
 		      
 		      @Override
 		      public void onDown(GridEvent<?> ce) {
 	        	onRowSelected((BeanModel) ce.getGrid().getSelectionModel().getSelectedItem());
 		      }
 		    };
 	}
 	
 	@Override
 	public void onRowSelected(BeanModel beanModel) {
 		if (beanModel != null)
 			showAgreement(beanModel);
 	}
 	
 	protected void setThis() {
 		this.setLayout(new FitLayout());
 		this.setHeight(forceHeight);
 		IconSupplier.setIcon(this, IconSupplier.getAgreementIconName());
 	}
 	
 	/**
 	 * Instigate an asynchronous load with a filter value.
 	 * @param filter
 	 */
 	protected void loadFiltered(String filter, GenericCodeInstance dateType, Date fromDate, Date toDate) {
 		if (filter == null || filter.length() == 0)
 			return;
 		if (dateType == null)
 			return;
 		if (fromDate == null)
 			return;
 		if (toDate == null)
 			return;
 		
 		this.filter = filter;
 		this.dateType = dateType;
 		this.fromDate = fromDate;
 		this.toDate = toDate;
 		
 		updateUserPortlet();
 		
 		agreementLoader.load();
 	}
 	
 	/**
 	 * Construct and return a loader to handle returning a list of institutions.
 	 * @return
 	 */
 	protected PagingLoader<PagingLoadResult<AgreementTermTuple>> getAgreementLoader() {
 		// proxy and reader  
 		RpcProxy<PagingLoadResult<AgreementTermTuple>> proxy = new RpcProxy<PagingLoadResult<AgreementTermTuple>>() {  
 			@Override  
 			public void load(Object loadConfig, final AsyncCallback<PagingLoadResult<AgreementTermTuple>> callback) {
 		    	
 				// This could be as simple as calling userListService.getUsers and passing the callback
 				// Instead, here the callback is overridden so that it can catch errors and alert the users.  Then the original callback is told of the failure.
 				// On success, the original callback is just passed the onSuccess message, and the response (the list).
 				
 				AsyncCallback<SynchronizedPagingLoadResult<AgreementTermTuple>> myCallback = new AsyncCallback<SynchronizedPagingLoadResult<AgreementTermTuple>>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else if (caught instanceof ServiceNotReadyException)
 								MessageBox.alert("Alert", "The " + caught.getMessage() + " is not available at this time.  Please try again in a few minutes.", null);
 						else {
 							MessageBox.alert("Alert", "Agreement load failed unexpectedly.", null);
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 						}
 						callback.onFailure(caught);
 					}
 
 					public void onSuccess(SynchronizedPagingLoadResult<AgreementTermTuple> syncResult) {
 						if(syncResult.getSyncId() != searchSyncId)
 							return;
 						
 						PagingLoadResult<AgreementTermTuple> result = syncResult.getResult();
 						if ( result.getData() == null || result.getData().size() == 0 ) {
 							if (result.getTotalLength() > 0)
 								gridView.setEmptyText(result.getTotalLength() + " agreements qualify (too many to display).<br/>Please enter filter criteria to narrow your search.");
 							else if (filter.length() == 0)
 								gridView.setEmptyText("Enter filter criteria to search for agreements.");
 							else
 								gridView.setEmptyText("Please enter filter criteria to narrow your search.");
 						}
 						callback.onSuccess(result);
 					}
 				};
 
 //				final List<Filter> saveFilters = columnFilters.getFilterData();
 //				columnFilters.clearFilters();
 				
 				searchSyncId = System.currentTimeMillis();
 				if (setFilterParameters( (LoadConfig) loadConfig ))
 					invokeSearchService((PagingLoadConfig) loadConfig, searchSyncId, myCallback);
 		    }  
 		};
 		BeanModelReader reader = new BeanModelReader();
 		
 		// loader and store  
 		PagingLoader<PagingLoadResult<AgreementTermTuple>> loader = new BasePagingLoader<PagingLoadResult<AgreementTermTuple>>(proxy, reader)
 //			Use the below code if filtering will be done on the server site
 //			{  
 //		      @Override  
 //		      protected Object newLoadConfig() {  
 //		        BasePagingLoadConfig config = new BaseFilterPagingLoadConfig();  
 //		        return config;  
 //		      }  
 //		    }
 		;
 		return loader;
 	}
 	
 	/**
 	 * Set any parameters to be passed to the backend service (through loadConfig properties) from any fields.
 	 * 
 	 * @param loadConfig
 	 */
 	public boolean setFilterParameters(LoadConfig loadConfig) {
 //		if (fromDateField.getValue() == null || toDateField.getValue() == null || dateTypeField.getValue() == null)
 //			return false;
 		
 		loadConfig.set("dateType", dateType.getCode());	//	dateTypeField.getValue().get("code"));
 		loadConfig.set("fromDate", fromDate);	//	fromDateField.getValue());
 		loadConfig.set("toDate", toDate);	//	toDateField.getValue());
 		loadConfig.set("filter", filter);
 		loadConfig.set("limit", LOAD_LIMIT);
 //		loadConfig.set("id", filter);
 		
 		return true;
 	}
  	
 	public void invokeSearchService(PagingLoadConfig loadConfig, long searchSyncId, AsyncCallback<SynchronizedPagingLoadResult<AgreementTermTuple>> myCallback) {
 		grid.mask("Searching for agreements terms...");
 		agreementTermSearchService.searchAgreementTerms((PagingLoadConfig) loadConfig, searchSyncId, myCallback);
 	}
 
 	protected void loadAgreementTerm(final int agreementId, final int termId) {
 		agreementTermGetService.getAgreementTerm(agreementId, termId, true, false,
 				new AsyncCallback<AgreementTermTuple>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else {
 							MessageBox.alert("Alert", "Agreement Term access failed unexpectedly.", null);
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 						}
 					}
 
 					public void onSuccess(AgreementTermTuple agreement) {
 						showAgreementTerm(agreement);
 					}
 			});
 	}
 
 //	protected void loadAgreement(final int agreementId) {
 //		agreementGetService.getAgreement(agreementId, true,
 //				new AsyncCallback<AgreementInstance>() {
 //					public void onFailure(Throwable caught) {
 //						// Show the RPC error message to the user
 //						if (caught instanceof IllegalArgumentException)
 //							MessageBox.alert("Alert", caught.getMessage(), null);
 //						else {
 //							MessageBox.alert("Alert", "Agreement access failed unexpectedly.", null);
 //							System.out.println(caught.getClass().getName());
 //							System.out.println(caught.getMessage());
 //						}
 //					}
 //
 //					public void onSuccess(AgreementInstance agreement) {
 //						showAgreement(agreement);
 //					}
 //			});
 //	}
 	
 	@Override
 	public void onExpand() {
 		super.onExpand();
 		awaken();
 	}
 	
 	@Override
 	public void onCollapse() {
 		super.onCollapse();
 		sleep();
 	}
 	
 	/**
 	 * Turn on the listener timer when waking up.
 	 */
 	@Override
 	public void awaken() {
 		if (this.isExpanded())
 			if (filterListenTimer != null)
 				filterListenTimer.scheduleRepeating(FILTER_LISTEN_PERIOD);
 	}
 
 	/**
 	 * Turn off the listener timer when going to sleep.
 	 */
 	@Override
 	public void sleep() {
 		if (filterListenTimer != null) {
 			filterListenTimer.cancel();
 		}
 	}
 
 	@Override
 	public void setAppPortletProvider(AppPortletProvider portletProvider) {
 		this.portletProvider = portletProvider;
 	}
 	
 	@Override
 	public String getShortPortletName() {
 		if (focusAgreementId > 0)
 			return "UCN " + focusAgreementId;
 		if (filter == null || filter.length() == 0)
 			return "UCN Search";
 		if (filter.length() > 10)
 			return "UCNs for " + filter.substring(0, 10);
 		return "UCNs for " + filter;
 	}
 	
 	@Override
 	public boolean allowDuplicatePortlets() {
 		//	Not allowed for a particular institution
 		if (focusAgreementId > 0)
 			return false;
 		//	Allowed for any general search
 		return true;
 	}
 	
 	@Override
 	public String getPortletIdentity() {
 		if (focusAgreementId <= 0)
 			return super.getPortletIdentity();
 		return this.getClass().getName() + ":" + focusAgreementId;
 	}
 
 	public int getFocusAgreementId() {
 		return focusAgreementId;
 	}
 
 	public void setFocusAgreementId(int focusAgreementId) {
 		this.focusAgreementId = focusAgreementId;
 	}
 
 	public int getFocusAgreementTermId() {
 		return focusAgreementTermId;
 	}
 
 	public void setFocusAgreementTermId(int focusAgreementTermId) {
 		this.focusAgreementTermId = focusAgreementTermId;
 	}
 
 	public String getFilter() {
 		return filter;
 	}
 
 	public void setFilter(String filter) {
 		this.filter = filter;
 	}
 
 	@Override
 	public void setFromKeyData(String keyData) {
 		if (keyData == null)
 			return;
 		
 		String oldDateType		= null;
 		String oldFromDate		= null;
 		String oldToDate		= null;
 		String oldFilter		= null;
 		String oldAgreementId   = null;
 		String oldTermId		= null;
 		
 		System.out.println("keyData " + keyData);
 		keyData = keyData.replace("\\:", "'''");	//	Remove any escaped :
 		String [] parts = keyData.split(":");
 		
 		if (parts.length > 0)
 			oldAgreementId = parts [0].trim();
 		if (parts.length > 1)
 			oldTermId = parts [1].trim();
 		if (parts.length > 2)
 			oldDateType = parts [2].trim();
 		if (parts.length > 3)
 			oldFromDate = parts [3].trim();
 		if (parts.length > 4)
 			oldToDate = parts [4].trim();
 		if (parts.length > 5)
 			oldFilter = parts [5].trim().replace("''''", ":"); //	Restore any escaped :
 		
 		if (oldFilter != null && oldFilter.length() > 0) {
 			filter = oldFilter;
 		}
 		
 		if (oldDateType != null) {
 			if (getDateTypeStore().findModel(oldDateType) != null)
 				dateType = getDateTypeStore().findModel(oldDateType).getBean();
 		}
 		
 		DateTimeFormat format = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_MEDIUM);
 		if (oldFromDate != null)
 			fromDate = format.parse(oldFromDate);
 		if (oldToDate != null)
 			toDate = format.parse(oldToDate);
 		
 		if (oldAgreementId != null && oldAgreementId.length() > 0) {
 			try {
 				focusAgreementId = Integer.parseInt(oldAgreementId);
 				focusAgreementTermId = Integer.parseInt(oldTermId);
 			} catch (NumberFormatException e) {
 			}
 		}
 	}
 
 	@Override
 	public String getKeyData() {
 		DateTimeFormat format = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_MEDIUM);
 		if (focusAgreementTerm == null)
 			return "::" + dateType.getCode() + ":" + format.format(fromDate) + ":" + format.format(toDate) + ":" + filter;
 		else	//	Note that we escape any ":" value in the filter with \\:
 			return focusAgreementTerm.getAgreementTerm().getAgreementId() + ":" + focusAgreementTerm.getAgreementTerm().getTermId() + ":"
 			 			+ dateType.getCode() + ":" + format.format(fromDate) + ":" + format.format(toDate) + ":" + filter.replace(":", "\\:");
 	}
 
 }
