 package com.argility.centralpages.ui;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.apache.log4j.Logger;
 
 import com.argility.centralpages.data.StatsProdContainer;
 import com.vaadin.ui.Table;
 
 @SuppressWarnings("serial")
 public class StatsProdTable extends Table {
 
 	protected transient Logger log = Logger
 			.getLogger(this.getClass().getName());
 
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
 
 	public StatsProdTable(StatsProdContainer cont) {
 
 		setSizeFull();
 
 		setSelectable(true);
 		setImmediate(true);
 
 		setColumnCollapsingAllowed(true);
 		setColumnReorderingAllowed(true);
 
 		setContainerDataSource(cont);
 
 		setCellStyleGenerator(new StatsTableCellStyleGenerator());
 		// addStyleName("view");
 	}
 
 	public void addBrTotalCountFooter(StatsProdContainer cont) {
 		setFooterVisible(true);
 		setColumnFooter("brCde", cont.size() + " Rows");
 	}
 
 	@Override
 	protected String formatPropertyValue(Object rowId, Object colId,
 			com.vaadin.data.Property property) {
 		if (property.getType() == Date.class && property.getValue() != null) {
 			return sdf.format(property.getValue());
 		}
 
 		if (property.getType() == Boolean.class) {
 			boolean val = (Boolean) property.getValue();
 			if (val) {
 				return "YES";
 			} else {
 				return "NO";
 			}
 		}
 
 		return super.formatPropertyValue(rowId, colId, property);
 	};
 
 }
