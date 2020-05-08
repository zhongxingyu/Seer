 package me.deal.client.view.menubar;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiConstructor;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Widget;
 import com.github.gwtbootstrap.*;
 import com.github.gwtbootstrap.client.ui.CheckBox;
 
 public class MenuWidget extends Composite {
 
 	private static MenuWidgetUiBinder uiBinder = GWT
 			.create(MenuWidgetUiBinder.class);
 	
 	interface MenuWidgetUiBinder extends UiBinder<Widget, MenuWidget> {
 	}
 
 	private final HandlerManager eventBus;
 		
 	public @UiConstructor MenuWidget(final HandlerManager eventBus) {
 		initWidget(uiBinder.createAndBindUi(this));
 		
 		this.eventBus = eventBus;
 		
 		bind();
 		initialize();
 	}
 
	//TODO: bind handlers for onhide filters ->
	//TODO: stop click propagation for dropdown
 	private void bind(){
 		
 		DiningCheckBox.addClickHandler(new ClickHandler() {
 	
 
 			public void onClick(ClickEvent event) {
 				event.stopPropagation();
 			}
 		});
 		
 		HealthCheckBox.addClickHandler(new ClickHandler() {
 
 			public void onClick(ClickEvent event) {
 				event.stopPropagation();
 			}
 		});
 		
 		FitnessCheckBox.addClickHandler(new ClickHandler() {
 
 			public void onClick(ClickEvent event) {
 				event.stopPropagation();
 			}
 		});
 		
 		RetailCheckBox.addClickHandler(new ClickHandler() {
 
 			public void onClick(ClickEvent event) {
 				event.stopPropagation();
 			}
 		});
 		
 		ServicesCheckBox.addClickHandler(new ClickHandler() {
 
 			public void onClick(ClickEvent event) {
 				event.stopPropagation();
 			}
 		});
 		
 //		ActivitiesCheckBox.addClickHandler(new ClickHandler() {
 //
 //			public void onClick(ClickEvent event) {
 //				event.stopPropagation();
 //			}
 //		});
 		
 		EventsCheckBox.addClickHandler(new ClickHandler() {
 
 			public void onClick(ClickEvent event) {
 				event.stopPropagation();
 			}
 		});
 		
 		SpecialInterestCheckBox.addClickHandler(new ClickHandler() {
 
 			public void onClick(ClickEvent event) {
 				event.stopPropagation();
 			}
 		});
 		
 	}
 	
 	@UiField
 	CheckBox DiningCheckBox;
 
 	@UiField
 	CheckBox HealthCheckBox;
 	
 	@UiField
 	CheckBox FitnessCheckBox;
 	
 	@UiField
 	CheckBox RetailCheckBox;
 	
 	@UiField
 	CheckBox ServicesCheckBox;
 	
 //	@UiField
 //	CheckBox ActivitiesCheckBox;
 	
 	@UiField
 	CheckBox EventsCheckBox;
 	
 	@UiField
 	CheckBox SpecialInterestCheckBox;
 		
 	@UiField
 	ModifiedDropdown FilterDropdown;
 	
 	
 	private void initialize() {
 		DiningCheckBox = new CheckBox("Dining");
 		HealthCheckBox = new CheckBox("Health");
 		FitnessCheckBox = new CheckBox("Fitness");
 		RetailCheckBox = new CheckBox("Retail");
 		ServicesCheckBox = new CheckBox("Services");
 //		ActivitiesCheckBox = new CheckBox("Activities");
 		EventsCheckBox = new CheckBox ("Events");
 		SpecialInterestCheckBox = new CheckBox ("Special Interests");
 		
 		FilterDropdown = new ModifiedDropdown("Filters");
 
 	}
 }
