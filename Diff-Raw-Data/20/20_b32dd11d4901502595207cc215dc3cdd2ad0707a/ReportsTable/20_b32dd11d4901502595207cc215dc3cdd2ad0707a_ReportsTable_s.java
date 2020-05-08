 package com.vaadin.training.bugrap.view.reports.components;
 
 import com.vaadin.data.Property;
 import com.vaadin.data.util.BeanItemContainer;
 import com.vaadin.event.ItemClickEvent;
 import com.vaadin.training.bugrap.domain.entity.Report;
 import com.vaadin.training.bugrap.view.reports.ReportsPresenter;
 import com.vaadin.ui.Table;
 
 import java.util.List;
 
 public class ReportsTable extends Table {
 
     private ReportsPresenter presenter;
 
     public void setPresenter(ReportsPresenter presenter) {
         this.presenter = presenter;
     }
 
     public ReportsTable() {
         setSelectable(true);
         setImmediate(true);
         setSizeFull();
 
         addItemClickListener(new ItemClickEvent.ItemClickListener() {
             @Override
             public void itemClick(ItemClickEvent event) {
                 presenter.reportSelected((Report)event.getItemId());
             }
         });
     }
 
     public void showReports(List<Report> reports) {
         setContainerDataSource(new BeanItemContainer<Report>(Report.class, reports));
        setVisibleColumns(new Object[]{"priority", "type", "summary", "assigned", "timestamp", "projectVersion"});
     }
 }
