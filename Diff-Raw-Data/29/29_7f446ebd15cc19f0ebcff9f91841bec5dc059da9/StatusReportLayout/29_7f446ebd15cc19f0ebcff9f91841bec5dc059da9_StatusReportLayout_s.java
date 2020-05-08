 package com.vaadin.training.bugrap.view.reports.components;
 
 import com.vaadin.data.Property;
 import com.vaadin.training.bugrap.domain.entity.ProjectVersion;
 import com.vaadin.training.bugrap.view.mvp.Presenter;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.NativeSelect;
 
 import java.util.List;
 
 public class StatusReportLayout extends HorizontalLayout {
 
     private final NativeSelect projectVersionsSelect;
    private Presenter presenter;
 
    public void setPresenter(Presenter presenter) {
         this.presenter = presenter;
     }
 
     public StatusReportLayout() {
         Label reportsForLabel = new Label("Reports for");
         addComponent(reportsForLabel);
         setComponentAlignment(reportsForLabel, Alignment.MIDDLE_CENTER);
 
         projectVersionsSelect = new NativeSelect();
         projectVersionsSelect.setNullSelectionAllowed(false);
         projectVersionsSelect.setImmediate(true);
         projectVersionsSelect.addValueChangeListener(new Property.ValueChangeListener() {
             @Override
             public void valueChange(Property.ValueChangeEvent event) {
                 String version = (String)event.getProperty().getValue();
                 presenter.projectVersionChanged(version);
             }
         });
         addComponent(projectVersionsSelect);
 
         setSpacing(true);
     }
 
     public void updateProjectVersions(List<ProjectVersion> versions) {
         projectVersionsSelect.removeAllItems();
 
         if(versions.size() > 1) {
             projectVersionsSelect.addItem("");
             projectVersionsSelect.setItemCaption("", "All versions");
             projectVersionsSelect.setValue("");
         }
 
         for (ProjectVersion version : versions) {
             projectVersionsSelect.addItem(version.getVersion());
         }
 
         if(versions.size() == 1) {
             projectVersionsSelect.setValue(versions.iterator().next().getVersion());
         }
     }
 }
