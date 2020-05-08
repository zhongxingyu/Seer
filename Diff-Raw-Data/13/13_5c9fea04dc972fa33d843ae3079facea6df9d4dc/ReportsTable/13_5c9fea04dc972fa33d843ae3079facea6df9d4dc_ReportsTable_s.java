 package com.vaadin.training.bugrap.view.reports.components;
 
 import com.vaadin.data.Property;
 import com.vaadin.data.util.BeanItemContainer;
 import com.vaadin.event.ItemClickEvent;
 import com.vaadin.training.bugrap.domain.entity.Report;
 import com.vaadin.training.bugrap.view.reports.ReportsPresenter;
 import com.vaadin.training.bugrap.view.reports.components.converter.TimestampConverter;
 import com.vaadin.ui.CssLayout;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Table;
 
 import java.util.Date;
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
                 presenter.reportSelected((Report) event.getItemId());
             }
         });
         addGeneratedColumn("priority", new ColumnGenerator() {
             @Override
             public Object generateCell(Table source, Object itemId, Object columnId) {
                 Report report = (Report) itemId;
 
                 HorizontalLayout layout = new HorizontalLayout();
                 layout.setSpacing(true);
                 layout.addStyleName("priority");
                for (int i = 0; i < report.getPriority().ordinal(); i++) {
                     layout.addComponent(new Label("|"));
                 }
 
                 return layout;
             }
         });
     }
 
     public void showReports(List<Report> reports) {
         setContainerDataSource(new BeanItemContainer<Report>(Report.class, reports));
         setVisibleColumns(new Object[]{"priority", "type", "summary", "resolution", "assigned", "timestamp", "projectVersion"});
         sort(new Object[]{"priority"}, new boolean[]{false});
 
         setConverter("timestamp", new TimestampConverter());
         setColumnHeader("timestamp", "Reported");
     }
 }
