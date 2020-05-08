 package edu.nrao.dss.client;
 
 import java.util.ArrayList;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.http.client.RequestBuilder;
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.Style.Orientation;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.Style.VerticalAlignment;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.HorizontalPanel;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.TabItem;
 import com.extjs.gxt.ui.client.widget.TabPanel;
 import com.extjs.gxt.ui.client.widget.VerticalPanel;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
 import com.extjs.gxt.ui.client.widget.layout.ColumnData;
 import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.layout.RowLayout;
 import com.extjs.gxt.ui.client.widget.layout.TableData;
 import com.extjs.gxt.ui.client.widget.layout.TableLayout;
 import com.google.gwt.user.client.ui.RootPanel;
 
 import edu.nrao.dss.client.data.DataGrid;
 import edu.nrao.dss.client.data.InputData;
 import edu.nrao.dss.client.data.ResultsData;
 import edu.nrao.dss.client.forms.DataForm;
 import edu.nrao.dss.client.forms.GeneralForm;
 import edu.nrao.dss.client.forms.HardwareForm;
 import edu.nrao.dss.client.forms.SourceForm;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class Calculator_ui implements EntryPoint {
 	public TabPanel tabPanel;
 
 	public void onModuleLoad() {
 		initLayout();
 	}
 	
 	private void initLayout(){
 		
 		// Root Content Panel
 		ContentPanel rcp = new ContentPanel();
 		rcp.setFrame(true);
 		rcp.setHeading("Sensitivity Calculator");
 		TableLayout tableLayout = new TableLayout(2);
 		tableLayout.setCellVerticalAlign(VerticalAlignment.TOP);
 		rcp.setLayout(tableLayout);
 		
 		TableData tdLeft = new TableData();
 		tdLeft.setVerticalAlign(VerticalAlignment.TOP);
 		tdLeft.setWidth("550");
 		
 		TableData tdRight = new TableData();
 		tdRight.setVerticalAlign(VerticalAlignment.TOP);
 		tdRight.setWidth("550");
 		
 		final GeneralForm generalForm   = new GeneralForm();
 		final HardwareForm hardwareForm = new HardwareForm();
 		final SourceForm sourceForm     = new SourceForm();
 		final DataForm dataForm         = new DataForm();
 		
 		hardwareForm.addObserver(sourceForm);
 		hardwareForm.addObserver(dataForm);
 		sourceForm.addObserver(dataForm);
 		
 		ContentPanel questions = new ContentPanel();
 		questions.setHeaderVisible(false);
 		questions.setHeading("Questions");
 		questions.setLayout(new RowLayout());
 		questions.setScrollMode(Scroll.AUTOY);
 		questions.setHeight(860);
 		//questions.setAutoHeight(true);
 		questions.setBorders(true);
 		//questions.setAutoWidth(true);
 		questions.setWidth(550);
 		
 		questions.add(generalForm);
 		questions.add(hardwareForm);
 		questions.add(sourceForm);
 		questions.add(dataForm);
 		
 		rcp.add(questions, tdLeft);
 		
 		ContentPanel right = new ContentPanel();
 		//right.setHeaderVisible(false);
 		right.setHeading("Controls");
 		right.setLayout(new TableLayout());
 		
 		Button update = new Button("Update Results");
 		update.addSelectionListener(new SelectionListener<ButtonEvent>() {
 
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				if (generalForm.isValid()) {
 				    generalForm.submit();
 				}
 				if (hardwareForm.isValid()) {
 					hardwareForm.submit();
 				}
 				if (sourceForm.isValid()) {
 					sourceForm.submit();
 				}
 				if (dataForm.isValid()) {
 					dataForm.submit();
 				}
 				
 			}
 			
 		});
 		TableData td = new TableData();
 		td.setHorizontalAlign(HorizontalAlignment.CENTER);
 		td.setMargin(20);
 		td.setPadding(10);
 		right.add(update, td);
 		
 		TabPanel results = new TabPanel();
 		results.setLayoutData(new RowLayout());
 		results.setHeight(800);
 		//results.setAutoHeight(true);
 		//results.setAutoWidth(true);
 		results.setWidth(600);
 		
 		TabItem resultsFormatted = new TabItem("Results");  
 		resultsFormatted.setScrollMode(Scroll.AUTO);  
 		resultsFormatted.addStyleName("pad-text");
 		resultsFormatted.setAutoLoad(ResultsData.getResultsRB());
 		results.add(resultsFormatted);
 		
 		TabItem resultsGrid = new TabItem("Result Grids");
 		resultsGrid.setLayout(new RowLayout());
 		resultsGrid.setScrollMode(Scroll.AUTO);
 		
 		resultsGrid.add(ResultsData.getResultsGrid());
 		resultsGrid.add(InputData.getInputGrid());
 		results.add(resultsGrid);
 		
 		right.add(results);
 		rcp.add(right);
 		
 		RootPanel.get().add(rcp);
 	}
 }
