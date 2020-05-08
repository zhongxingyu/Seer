 package com.vaadin.training.bugrap.view.reports.components;
 
 import com.vaadin.data.Property;
 import com.vaadin.event.ShortcutListener;
 import com.vaadin.training.bugrap.view.reports.ReportsPresenter;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.TextField;
 
 public class ManageButtonsLayout extends HorizontalLayout {
     private ReportsPresenter presenter;
 
     public void setPresenter(ReportsPresenter presenter) {
         this.presenter = presenter;
     }
 
     public ManageButtonsLayout() {
         setWidth("100%");
 
         Button reportBugButton = new Button("Report a bug", new Button.ClickListener() {
             @Override
             public void buttonClick(Button.ClickEvent event) {
                 presenter.reportBugButtonClicked();
             }
         });
         addComponent(reportBugButton);
         setComponentAlignment(reportBugButton, Alignment.BOTTOM_CENTER);
 
         Button requestFeatureButton = new Button("Request a feature");
         addComponent(requestFeatureButton);
         setComponentAlignment(requestFeatureButton, Alignment.BOTTOM_CENTER);
 
         Button manageProjectButton = new Button("Manage project");
         addComponent(manageProjectButton);
         setComponentAlignment(manageProjectButton, Alignment.BOTTOM_CENTER);
 
         TextField searchReportsField = new TextField("");
        searchReportsField.setInputPrompt("Search reports...");
         searchReportsField.setWidth("200px");
         searchReportsField.setImmediate(true);
         searchReportsField.addValueChangeListener(new Property.ValueChangeListener() {
             @Override
             public void valueChange(Property.ValueChangeEvent event) {
                 presenter.searchReports((String)event.getProperty().getValue());
             }
         });
         addComponent(searchReportsField);
         setComponentAlignment(searchReportsField, Alignment.BOTTOM_RIGHT);
         setExpandRatio(searchReportsField, 1.0f);
 
         setSpacing(true);
     }
 }
