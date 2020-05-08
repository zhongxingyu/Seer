 package com.vaadin.training.bugrap.view.reports.components;
 
 import com.vaadin.data.Property;
 import com.vaadin.training.bugrap.client.components.distbar.Segment;
 import com.vaadin.training.bugrap.components.distbar.DistributionBar;
 import com.vaadin.training.bugrap.domain.entity.ProjectVersion;
 import com.vaadin.training.bugrap.view.reports.ReportsPresenter;
 import com.vaadin.ui.*;
 
 import java.util.List;
 
 public class StatusReportLayout extends HorizontalLayout {
 
     private final NativeSelect projectVersionsSelect;
     private final Property.ValueChangeListener valueChangeListener;
     private ReportsPresenter presenter;
     private ProjectVersion allVersions;
 
     public void setPresenter(ReportsPresenter presenter) {
         this.presenter = presenter;
     }
 
     public StatusReportLayout() {
         setWidth("100%");
 
         Label reportsForLabel = new Label("Reports for");
         addComponent(reportsForLabel);
         setComponentAlignment(reportsForLabel, Alignment.MIDDLE_CENTER);
 
         projectVersionsSelect = new NativeSelect();
         projectVersionsSelect.setNullSelectionAllowed(false);
         projectVersionsSelect.setImmediate(true);
         valueChangeListener = new Property.ValueChangeListener() {
             @Override
             public void valueChange(Property.ValueChangeEvent event) {
                 ProjectVersion version = (ProjectVersion) event.getProperty().getValue();
 
                 if (allVersions.equals(version)) {
                     version = null;
                 }
                 presenter.projectVersionChanged(version);
             }
         };
 
         addComponent(projectVersionsSelect);
 
         DistributionBar distributionBar = new DistributionBar();
         distributionBar.addSegmentClickListener(new DistributionBar.SegmentClickListener() {
             @Override
             public void segmentClicked(Segment segment) {
                 Notification.show("Segment " + segment.name() + " was clicked!");
             }
         });
        distributionBar.setClosed(50);
        distributionBar.setInProgress(30);
         distributionBar.setUnassigned(1500);
         distributionBar.setWidth("100%");
         addComponent(distributionBar);
         setExpandRatio(distributionBar, 1.0f);
 
         setSpacing(true);
     }
 
     public void updateProjectVersions(List<ProjectVersion> versions) {
         projectVersionsSelect.removeValueChangeListener(valueChangeListener);
 
         projectVersionsSelect.removeAllItems();
 
         if(versions.size() > 1) {
             allVersions = new ProjectVersion();
             projectVersionsSelect.addItem(allVersions);
             projectVersionsSelect.setItemCaption(allVersions, "All versions");
             projectVersionsSelect.setValue(allVersions);
         }
 
         for (ProjectVersion version : versions) {
             projectVersionsSelect.addItem(version);
             projectVersionsSelect.setItemCaption(version, version.getVersion());
         }
 
         if(versions.size() == 1) {
             projectVersionsSelect.setValue(versions.iterator().next());
         }
 
         projectVersionsSelect.addValueChangeListener(valueChangeListener);
     }
 }
