 package edu.nrao.dss.client;
 
 import java.util.ArrayList;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.http.client.RequestBuilder;
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.Style.Orientation;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.Style.VerticalAlignment;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.HorizontalPanel;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.TabItem;
 import com.extjs.gxt.ui.client.widget.TabPanel;
 import com.extjs.gxt.ui.client.widget.VerticalPanel;
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
		rcp.setLayout(new TableLayout(2));
 		
 		TableData tdLeft = new TableData();
 		tdLeft.setVerticalAlign(VerticalAlignment.TOP);
 		tdLeft.setWidth("550");
 		
 		TableData tdRight = new TableData();
 		tdRight.setVerticalAlign(VerticalAlignment.TOP);
 		tdRight.setWidth("550");
 		
 		GeneralForm generalForm   = new GeneralForm();
 		HardwareForm hardwareForm = new HardwareForm();
 		SourceForm sourceForm     = new SourceForm();
 		DataForm dataForm         = new DataForm();
 		
 		hardwareForm.addObserver(sourceForm);
 		hardwareForm.addObserver(dataForm);
 		sourceForm.addObserver(dataForm);
 		
 		ContentPanel questions = new ContentPanel();
 		questions.setHeaderVisible(false);
 		questions.setHeading("Questions");
 		questions.setLayout(new RowLayout());
		questions.setScrollMode(Scroll.AUTO);
 		questions.setHeight(800);
 		questions.setBorders(true);
		questions.setAutoWidth(true);
		//questions.setWidth(550);
 		
 		questions.add(generalForm);
 		questions.add(hardwareForm);
 		questions.add(sourceForm);
 		questions.add(dataForm);
 		
 		rcp.add(questions, tdLeft);
 		
 		TabPanel results = new TabPanel();
 		results.setHeight(800);
		results.setAutoWidth(true);
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
 		
 		rcp.add(results);
 		
 		RootPanel.get().add(rcp);
 	}
 }
