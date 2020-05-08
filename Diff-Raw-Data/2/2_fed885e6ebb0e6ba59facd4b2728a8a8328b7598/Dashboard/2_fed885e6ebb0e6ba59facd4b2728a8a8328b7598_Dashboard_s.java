 package infodoc.basic.component;
 
 import infodoc.basic.BasicConstants;
 import infodoc.core.InfodocConstants;
 import infodoc.core.container.InfodocContainerFactory;
 import infodoc.core.dto.Case;
 import infodoc.core.dto.Form;
 import infodoc.core.dto.ActivityInstance;
 import infodoc.core.dto.User;
 import infodoc.core.ui.activity.ActivityExecutorHelper;
 import infodoc.core.ui.cases.CaseBox;
 import infodoc.core.ui.common.InfodocTheme;
 
 import java.util.List;
 
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.terminal.ExternalResource;
 import com.vaadin.terminal.ThemeResource;
 import com.vaadin.ui.Accordion;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.CustomComponent;
 import com.vaadin.ui.Embedded;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.HorizontalSplitPanel;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Layout;
 import com.vaadin.ui.OptionGroup;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 
 import enterpriseapp.EnterpriseApplication;
 import enterpriseapp.Utils;
 
 public class Dashboard extends CustomComponent {
 	
 	private static final long serialVersionUID = 1L;
 	
 	private HorizontalSplitPanel split = new HorizontalSplitPanel();
 	private Accordion accordion = new Accordion();
 	private VerticalLayout myInstancesLayout = new VerticalLayout();
 	private VerticalLayout activityHistoryLayout = new VerticalLayout();
 	private OptionGroup formSelect;
 
 	private User user;
 	private int numberOfLastActivities = BasicConstants.infodocBasicNumberOfLastActivities;
 
 	public Dashboard() {
 		split.setSplitPosition(50);
 		setCompositionRoot(split);
 	}
 	
 	@Override
 	public void attach() {
 		user = (User) getApplication().getUser();
 		
 		myInstancesLayout.setMargin(true);
 		myInstancesLayout.setSpacing(true);
 		
 		activityHistoryLayout.setMargin(true);
 		activityHistoryLayout.setSpacing(true);
 		
 		accordion.setSizeFull();
 		accordion.setStyleName(InfodocTheme.ACCORDION_OPAQUE);
 		
 		if(user.getUserGroup().isAccessBasicModule()) {
 			accordion.addTab(myInstancesLayout, BasicConstants.uiMyCases);
 		}
 		
 		if(user.getUserGroup().isAccessLastActivityInstances()) {
 			accordion.addTab(activityHistoryLayout, BasicConstants.uiLastActivityInstances);
 		}
 		
 		if(accordion.getComponentCount() == 0) {
 			split.setSplitPosition(0);
 		} else {
 			split.setFirstComponent(accordion);
 		}
 		
 		formSelect = new OptionGroup();
 		formSelect.setStyleName("horizontal");
 		formSelect.setImmediate(true);
 		formSelect.addListener(new ValueChangeListener() {
 			private static final long serialVersionUID = 1L;
 			@Override
 			public void valueChange(ValueChangeEvent event) {
 				updateMyInstances();
 			}
 		});
 				
 		updateMyInstances();
 		formSelect.setValue(InfodocConstants.uiShowAllCases);
 		
 		if(user.getUserGroup().isAccessLastActivityInstances()) {
 			updateActivityHistory(numberOfLastActivities);
 		}
 		
 		split.setSecondComponent(getEmbeddedWebPage());
 	}
 	
 	public void updateMyInstances() {
 		updateFormFilter();
 		
 		myInstancesLayout.removeAllComponents();
 		
 		Button refreshButton = new Button();
 		refreshButton.setDescription(BasicConstants.uiRefresh);
 		refreshButton.setIcon(new ThemeResource(InfodocTheme.iconUpdate));
 		
 		refreshButton.addListener(new ClickListener() {
 			private static final long serialVersionUID = 1L;
 			@Override
 			public void buttonClick(ClickEvent event) {
 				updateMyInstances();
 			}
 		});
 		
 		HorizontalLayout toolBarLayout = new HorizontalLayout();
 		toolBarLayout.setWidth("100%");
 		
 		toolBarLayout.addComponent(formSelect);
 		toolBarLayout.addComponent(refreshButton);
 		toolBarLayout.setComponentAlignment(refreshButton, Alignment.TOP_RIGHT);
 		
 		myInstancesLayout.addComponent(toolBarLayout);
 		
 		List<Form> forms = InfodocContainerFactory.getFormContainer().findByUserId(user.getId());
 		boolean instancesAdded = false;
 		
 		for(Form form : forms) {
			if(formSelect.getValue() == null || formSelect.getValue().equals(InfodocConstants.uiShowAllCases) || formSelect.getValue().equals(form)) {
 				List<Case> instances = InfodocContainerFactory.getCaseContainer().findMyCases(user.getId(), form.getId());
 				
 				for(Case instance : instances) {
 					instancesAdded = true;
 					addCase(instance);
 				}
 			}
 		}
 		
 		if(!instancesAdded) {
 			myInstancesLayout.addComponent(new Label(BasicConstants.uiEmptyCasesList, Label.CONTENT_XHTML));
 		}
 	}
 	
 	public void updateFormFilter() {
 		List<Form> forms = InfodocContainerFactory.getFormContainer().findByUserId(user.getId());
 		
 		if(!forms.isEmpty()) {
 			formSelect.addItem(InfodocConstants.uiShowAllCases);
 		}
 		
 		for(Form form : forms) {
 			if(!form.isHideInDashboard()) {
 				int total = InfodocContainerFactory.getCaseContainer().findMyCases(user.getId(), form.getId()).size();
 				
 				formSelect.addItem(form);
 				formSelect.setItemCaption(form, form.getName() + " (" + total + ")");
 			}
 		}
 	}
 	
 	public void addCase(final Case instance) {
 		addActivityInstance(InfodocContainerFactory.getCaseContainer().getLastActivityInstance(instance), myInstancesLayout, false, true, false, true, true);
 	}
 	
 	public void updateActivityHistory(final int count) {
 		activityHistoryLayout.removeAllComponents();
 		
 		Button refreshButton = new Button();
 		refreshButton.setDescription(BasicConstants.uiRefresh);
 		refreshButton.setIcon(new ThemeResource(InfodocTheme.iconUpdate));
 		
 		refreshButton.addListener(new ClickListener() {
 			private static final long serialVersionUID = 1L;
 			@Override
 			public void buttonClick(ClickEvent event) {
 				numberOfLastActivities = BasicConstants.infodocBasicNumberOfLastActivities;
 				updateActivityHistory(numberOfLastActivities);
 			}
 		});
 		
 		activityHistoryLayout.addComponent(refreshButton);
 		activityHistoryLayout.setComponentAlignment(refreshButton, Alignment.TOP_RIGHT);
 		
 		List<ActivityInstance> activityInstances = InfodocContainerFactory.getActivityInstanceContainer().findLast(count);
 		
 		if(activityInstances.isEmpty()) {
 			activityHistoryLayout.addComponent(new Label(BasicConstants.uiEmptyActivitiesList, Label.CONTENT_XHTML));
 		} else {
 			for(ActivityInstance instance : activityInstances) {
 				addActivityInstance(instance, activityHistoryLayout, true, false, true, true, false);
 			}
 		}
 		
 		Button showMoreButton = new Button(BasicConstants.uiSeeMore);
 		showMoreButton.setStyleName(InfodocTheme.BUTTON_LINK);
 		
 		showMoreButton.addListener(new ClickListener() {
 			private static final long serialVersionUID = 1L;
 			@Override
 			public void buttonClick(ClickEvent event) {
 				numberOfLastActivities += BasicConstants.infodocBasicNumberOfLastActivities;
 				updateActivityHistory(numberOfLastActivities);
 			}
 		});
 		
 		activityHistoryLayout.addComponent(showMoreButton);
 		activityHistoryLayout.setComponentAlignment(showMoreButton, Alignment.BOTTOM_CENTER);
 	}
 	
 	public void addActivityInstance(final ActivityInstance instance, Layout layout, boolean showUser, boolean showActions, boolean showActivity, boolean showDate, boolean bold) {
 		Button button = new Button(instance.getCaseDto().toString());
 		button.setIcon(new ThemeResource(instance.getCaseDto().getForm().getIcon()));
 		
 		if(bold) {
 			button.setStyleName(InfodocTheme.BUTTON_LINK_BOLD);
 		} else {
 			button.setStyleName(InfodocTheme.BUTTON_LINK);
 		}
 		
 		button.addListener(new Button.ClickListener() {
 			private static final long serialVersionUID = 1L;
 			
 			@Override
 			public void buttonClick(ClickEvent event) {
 				showCase(instance.getCaseDto());
 			}
 			
 		});
 		
 		HorizontalLayout activityInstanceLayout = new HorizontalLayout();
 		activityInstanceLayout.setSpacing(true);
 		
 		if(showUser) {
 			Label userLabel = new Label("<span class='" + InfodocTheme.CLASS_INITIAL_USERS + "'>" + instance.getUser().getLogin() + "<span>", Label.CONTENT_XHTML);
 			
 			if(instance.getComments() != null && !instance.getComments().isEmpty()) {
 				userLabel.setValue(userLabel.getValue().toString() + ": <span class='" + InfodocTheme.CLASS_COMMENTS + "'>&quot;" + instance.getComments().trim() + "&quot;</span>");
 			}
 			
 			activityInstanceLayout.addComponent(userLabel);
 		}
 		
 		activityInstanceLayout.addComponent(button);
 		
 		if(showActivity) {
 			activityInstanceLayout.addComponent(new Embedded(null, ActivityExecutorHelper.getIcon(instance.getActivity(), user)));
 			activityInstanceLayout.addComponent(new Label("<b>" + instance.getActivity().getName() + "</b>", Label.CONTENT_XHTML));
 		}
 		
 		if(showDate) {
 			Label dateLabel = new Label("<small>" + Utils.dateTimeToString(instance.getExecutionTime()) + "</small>", Label.CONTENT_XHTML);
 			activityInstanceLayout.addComponent(dateLabel);
 		}
 		
 		if(showActions) {
 			Panel panel = new Panel();
 			panel.addComponent(activityInstanceLayout);
 			panel.addComponent(ActivityExecutorHelper.getAvailableActivitiesLayout(instance.getCaseDto(), user));
 			
 			layout.addComponent(panel);
 			
 		} else {
 			layout.addComponent(activityInstanceLayout);
 		}
 	}
 	
 	public void showCase(Case instance) {
 		instance = InfodocContainerFactory.getCaseContainer().getEntity(instance.getId());
 		
 		Window window = new Window(instance.getForm().getName());
 		window.setWidth("680px");
 		window.setHeight("480px");
 		window.setModal(true);
 		window.addComponent(new CaseBox(instance));
 		
 		getApplication().getMainWindow().addWindow(window);
 	}
 
 	public Embedded getEmbeddedWebPage() {
 		String url = user.getDashboardUrl();
 		
 		if(url == null || url.isEmpty()) {
 			url = Utils.getWebContextPath(EnterpriseApplication.getInstance()) + BasicConstants.infodocBasicDefaultDashboardUrl;
 		}
 		
 		Embedded page = new Embedded("", new ExternalResource(url));
 		page.setType(Embedded.TYPE_BROWSER);
 		page.setSizeFull();
 		
 		return page;
 	}
 
 }
