 package com.argility.centralpages.ui.view;
 
 import java.util.Date;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import com.argility.centralpages.CentralpagesApplication;
 import com.argility.centralpages.dao.ProductionStatsDAO;
 import com.argility.centralpages.data.ProductionStats;
 import com.argility.centralpages.ui.ProductionStatsStyleGenerator;
 import com.argility.centralpages.ui.ProductionStatsTable;
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.util.BeanItemContainer;
 import com.vaadin.data.validator.IntegerValidator;
 import com.vaadin.data.validator.RegexpValidator;
 import com.vaadin.event.ShortcutAction.KeyCode;
 import com.vaadin.terminal.ThemeResource;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.Reindeer;
 import com.vaadin.ui.DateField;
 import com.vaadin.ui.Form;
 import com.vaadin.ui.NativeSelect;
 import com.vaadin.ui.TextArea;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalSplitPanel;
 
 @SuppressWarnings("serial")
 public class ProductionStatsView extends VerticalSplitPanel implements
 		Property.ValueChangeListener {
 
 	public static final String SEARCH_BR_CDE = "Search by branch";
 	public static final String SEARCH_DATE = "Search by date";
 	public static final String SEARCH_AUDIT = "Search by audit";
 
 	protected transient Logger log = Logger
 			.getLogger(this.getClass().getName());
 
 	protected ProductionStatsDAO dao;
 	private NativeSelect select;
 	private TextField tf;
 	private DateField dateField;
 	private RegexpValidator branchValidator = new RegexpValidator("[0-9][0-9][0-9][0-9]",
 		"Must be 4 digits long!");
 	private IntegerValidator intValidator = new IntegerValidator("Must be an integer value");
 
 	protected ProductionStatsTable table;
 	private Form form;
 
 	public ProductionStatsView() {
 		dao = (ProductionStatsDAO) CentralpagesApplication.getInstance()
 				.getSpringContext().getBean("productionStatsDAO");
 
 		setSizeFull();
 		//addStyleName("view");
 		getSearchForm();
 	}
 
 	private void setProductionStatsTable(List<ProductionStats> data) {
 		BeanItemContainer<ProductionStats> cont = new BeanItemContainer<ProductionStats>(
 				ProductionStats.class, data);
 
 		table = new ProductionStatsTable(null, cont);
 		table.setSortContainerPropertyId("dateTime");
 		table.setSortAscending(false);
 
 		table.setCellStyleGenerator(new ProductionStatsStyleGenerator());
 	}
 
 	private void wireTable() {
 		table.setSizeFull();
 		setFirstComponent(table);
 		setSplitPosition(100);
 	}
 	
 	private Form getSearchForm() {
 
 		form = new Form();
 
 		select = new NativeSelect("Please select search type");
 		select.setWidth("30%");
 		select.addItem(SEARCH_BR_CDE);
 		select.addItem(SEARCH_DATE);
 		select.addItem(SEARCH_AUDIT);
 		select.setNullSelectionAllowed(false);
 		select.setValue(SEARCH_BR_CDE);
 		select.setImmediate(true);
 
 		select.addListener(this);
 		
 		tf = new TextField("Branch: ");
 		tf.setColumns(20);
 		
 		tf.addValidator(branchValidator);
 		tf.setRequired(true);
 		tf.setRequiredError("Required field");
		tf.setInputPrompt("Enter 4 digit branch");
 
 		dateField = new DateField("Choose Date");
 		dateField.setVisible(false);
 		dateField.setRequired(true);
 		dateField.setResolution(DateField.RESOLUTION_DAY);
 		
 		Button b = new Button("Search", new ClickListener() {
 			
 			public void buttonClick(ClickEvent event) {
 				
 				if (select.getValue().equals(SEARCH_BR_CDE)) {
 					if (!tf.isValid()) return;
 					setProductionStatsTable(dao.getAllProductionStatsForBranch(tf.getValue()+""));
 				} else if (select.getValue().equals(SEARCH_DATE)) {
 					if (!dateField.isValid()) return;
 					Date d = (Date)dateField.getValue();
 					log.info("Date is " + d.toString());
 					setProductionStatsTable(dao.getAllProductionStatsForDate(d));
 				} else if (select.getValue().equals(SEARCH_AUDIT)) {
 					if (!tf.isValid()) return;
 					Integer search = Integer.parseInt(tf.getValue()+"");
 					setProductionStatsTable(dao.getAllProductionStatsForAudit(search));
 				}
 				wireTable();
 			}
 		});
 		
 		b.setIcon(new ThemeResource("icons/Search.png"));
 		
 		b.setClickShortcut(KeyCode.ENTER);
 		
 		form.addField("select", select);
 		form.addField("branch", tf);
 		form.addField("date", dateField);
 		form.addField("submit", b);
 		
 		tf.focus();
 		
 		setMargin(true);
 		
 		setFirstComponent(form);
 		setSplitPosition(100);
 		
 		return form;
 	}
 
 	public void valueChange(ValueChangeEvent event) {
 		Property property = event.getProperty();
 		int splitPos = 60;
 		TextArea ta = null;
 
 		if (property == table) {
 			Item item = table.getItem(table.getValue());
 
 			if (item == null) {
 				setSplitPosition(100);
 			} else {
 				Property prop = item.getItemProperty("stackTrace");
 				if (prop == null || "".equals(prop.getValue())) {
 					prop.setValue("Log entry is just a notification, there is no error stack trace");
 					splitPos = 90;
 				}
 
 				ta = new TextArea(prop);
 				ta.setWidth("100%");
 				ta.setReadOnly(true);
 				ta.setSizeFull();
 				setSecondComponent(ta);
 				setSplitPosition(splitPos);
 			}
 		} else if (property == select) {
 			if (select.getValue().equals(SEARCH_BR_CDE)) {
 				//form.removeItemProperty("branch");
 				form.getField("branch").setVisible(true);
 				tf.removeValidator(intValidator);
 				tf.addValidator(branchValidator);
 				form.getField("date").setVisible(false);
 			} else if (select.getValue().equals(SEARCH_DATE)) {
 				form.getField("date").setVisible(true);
 				form.getField("branch").setVisible(false);
 			} else if (select.getValue().equals(SEARCH_AUDIT)) {
 				form.getField("branch").setVisible(true);
 				tf.removeValidator(branchValidator);
 				tf.addValidator(intValidator)
 ;				((TextField)form.getField("branch")).setInputPrompt("Enter search audit id");
 				form.getField("date").setVisible(false);
 			}
 		}
 
 	}
 }
