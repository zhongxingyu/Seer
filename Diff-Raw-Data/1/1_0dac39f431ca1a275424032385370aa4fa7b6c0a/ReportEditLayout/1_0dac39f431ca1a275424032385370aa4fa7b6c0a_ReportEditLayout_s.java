 package com.vaadin.training.bugrap.view.reports.components;
 
 import com.vaadin.data.fieldgroup.FieldGroup;
 import com.vaadin.data.util.BeanItem;
 import com.vaadin.event.ShortcutAction;
 import com.vaadin.server.BrowserWindowOpener;
 import com.vaadin.training.bugrap.domain.entity.*;
 import com.vaadin.training.bugrap.view.reports.ReportsPresenter;
 import com.vaadin.ui.*;
 
 public class ReportEditLayout extends VerticalLayout {
 
     private ReportsPresenter presenter;
 
     private FieldGroup fieldGroup;
 
     private final NativeSelect priorityCombobox;
     private final NativeSelect typeCombobox;
     private final NativeSelect statusCombobox;
     private final NativeSelect assignedCombobox;
     private final NativeSelect versionCombobox;
     private final TextArea descriptionTextArea;
     private final Label reportSummaryLabel;
 
     private final Button updateButton;
     private final Button newWindowButton;
     private final TextField reportSummaryField;
 
     public void setPresenter(ReportsPresenter presenter) {
         this.presenter = presenter;
     }
 
     public ReportEditLayout() {
         setSizeFull();
         setMargin(true);
 
         HorizontalLayout headerLayout = new HorizontalLayout();
         headerLayout.setSpacing(true);
         headerLayout.setWidth("100%");
 
         newWindowButton = new Button("New window", new Button.ClickListener() {
             @Override
             public void buttonClick(Button.ClickEvent event) {
                 presenter.newWindowReportButtonClicked();
             }
         });
 
         headerLayout.addComponent(newWindowButton);
 
         reportSummaryLabel = new Label();
         reportSummaryLabel.setWidth("100%");
         headerLayout.addComponent(reportSummaryLabel);
         headerLayout.setExpandRatio(reportSummaryLabel, 1.0f);
 
         reportSummaryField = new TextField();
         reportSummaryField.setNullRepresentation("");
         reportSummaryField.setVisible(false);
         reportSummaryField.setWidth("100%");
         headerLayout.addComponent(reportSummaryField);
 
         addComponent(headerLayout);
 
         HorizontalLayout reportFormLayout = new HorizontalLayout();
 
         reportFormLayout.setSpacing(true);
 
         priorityCombobox = new NativeSelect("Priority");
         priorityCombobox.setNullSelectionAllowed(false);
         for (ReportPriority priority : ReportPriority.values()) {
             priorityCombobox.addItem(priority);
 
             StringBuilder builder = new StringBuilder();
             for (int i = ReportPriority.values().length; i >= priority.ordinal(); i--) {
                 builder.append("I");
             }
             priorityCombobox.setItemCaption(priority, builder.toString());
         }
         reportFormLayout.addComponent(priorityCombobox);
         priorityCombobox.setNullSelectionAllowed(false);
 
         typeCombobox = new NativeSelect("Type");
         typeCombobox.setNullSelectionAllowed(false);
         for (ReportType reportType : ReportType.values()) {
             typeCombobox.addItem(reportType);
             typeCombobox.setItemCaption(reportType, reportType.toString());
         }
         reportFormLayout.addComponent(typeCombobox);
 
         statusCombobox = new NativeSelect("Status");
         statusCombobox.setNullSelectionAllowed(false);
         for (ReportStatus reportStatus : ReportStatus.values()) {
             statusCombobox.addItem(reportStatus);
             statusCombobox.setItemCaption(reportStatus, reportStatus.toString());
         }
         reportFormLayout.addComponent(statusCombobox);
 
         assignedCombobox = new NativeSelect("Assigned to");
         assignedCombobox.setNullSelectionAllowed(false);
         reportFormLayout.addComponent(assignedCombobox);
 
         versionCombobox = new NativeSelect("Version");
         versionCombobox.setNullSelectionAllowed(false);
         reportFormLayout.addComponent(versionCombobox);
 
         updateButton = new Button("Update", new Button.ClickListener() {
             @Override
             public void buttonClick(Button.ClickEvent event) {
                 try {
                     fieldGroup.commit();
 
                     presenter.reportUpdated();
                 } catch (FieldGroup.CommitException e) {
                     Notification.show("Can't save the report", Notification.Type.ERROR_MESSAGE);
                 }
             }
         });
 
         reportFormLayout.addComponent(updateButton);
         reportFormLayout.setComponentAlignment(updateButton, Alignment.BOTTOM_CENTER);
 
         Button revertButton = new Button("Revert", new Button.ClickListener() {
             @Override
             public void buttonClick(Button.ClickEvent event) {
                 fieldGroup.discard();
             }
         });
         reportFormLayout.addComponent(revertButton);
         reportFormLayout.setComponentAlignment(revertButton, Alignment.BOTTOM_CENTER);
 
         addComponent(reportFormLayout);
 
         descriptionTextArea = new TextArea();
         descriptionTextArea.setSizeFull();
         descriptionTextArea.setNullRepresentation("");
         addComponent(descriptionTextArea);
         setExpandRatio(descriptionTextArea, 1.0f);
 
         setSpacing(true);
     }
 
     public void showReport(Report report) {
         fieldGroup = new FieldGroup(new BeanItem<Report>(report));
         fieldGroup.bind(reportSummaryField, "summary");
         fieldGroup.bind(priorityCombobox, "priority");
         fieldGroup.bind(typeCombobox, "type");
         fieldGroup.bind(statusCombobox, "status");
         fieldGroup.bind(assignedCombobox, "assigned");
         fieldGroup.bind(versionCombobox, "projectVersion");
         fieldGroup.bind(descriptionTextArea, "description");
 
         if (report.getProjectVersion() != null) {
             Project project = report.getProjectVersion().getProject();
             populateDataFromProject(project);
         }
 
         reportSummaryLabel.setValue(report.getSummary());
         priorityCombobox.setValue(report.getPriority());
         typeCombobox.setValue(report.getType());
         statusCombobox.setValue(report.getStatus());
         versionCombobox.setValue(report.getProjectVersion());
         assignedCombobox.setValue(report.getAssigned());
         descriptionTextArea.setValue(report.getDescription());
 
         updateButton.setClickShortcut(ShortcutAction.KeyCode.ENTER);
         updateButton.focus();
     }
 
     public void hideNewWindowButton() {
         newWindowButton.setVisible(false);
     }
 
     public void populateDataFromProject(Project project) {
         versionCombobox.removeAllItems();
         for (ProjectVersion projectVersion : project.getProjectVersions()) {
             versionCombobox.addItem(projectVersion);
             versionCombobox.setItemCaption(projectVersion, projectVersion.getVersion());
         }
 
         assignedCombobox.removeAllItems();
         for (User user : project.getParticipants()) {
             assignedCombobox.addItem(user);
             assignedCombobox.setItemCaption(user, user.getName());
         }
     }
 
     public void enableEditableSummary() {
         reportSummaryLabel.setVisible(false);
         reportSummaryField.setVisible(true);
     }
 }
