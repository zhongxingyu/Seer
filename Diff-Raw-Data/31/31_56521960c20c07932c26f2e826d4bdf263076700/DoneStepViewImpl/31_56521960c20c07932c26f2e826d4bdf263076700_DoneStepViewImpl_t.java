 package org.cotrix.web.importwizard.client.step.done;
 
 import org.cotrix.web.importwizard.client.resources.CotrixSimplePager;
 import org.cotrix.web.importwizard.client.resources.DataGridReportResource;
 import org.cotrix.web.importwizard.shared.ReportLog;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.DataGrid;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ResizeComposite;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class DoneStepViewImpl extends ResizeComposite implements DoneStepView {
 
 	protected static final String DOWNLOAD_URL = GWT.getModuleBaseURL()+"reportDownload";
 	
 	private static DoneStepViewUiBinder uiBinder = GWT.create(DoneStepViewUiBinder.class);
 
 	@UiTemplate("DoneStepView.ui.xml")
 	interface DoneStepViewUiBinder extends UiBinder<Widget, DoneStepViewImpl> {
 	}
 	
 	@UiField Button reportDownload;
 	
 	
 	@UiField(provided = true)
 	DataGrid<ReportLog> reportGrid;
 	
 	@UiField(provided = true)
 	SimplePager reportPager;
 	
 	protected ReportLogDataProvider dataProvider;
 
 	@Inject
 	public DoneStepViewImpl(ReportLogDataProvider dataProvider) {
 		this.dataProvider = dataProvider;
 		setupGrid();
 		
 		initWidget(uiBinder.createAndBindUi(this));
 	}
 	
 	@UiHandler("reportDownload")
 	protected void downloadReport(ClickEvent clickEvent) {
 		Window.open(DOWNLOAD_URL, "_blank", "");
 	}
 	
 	/** 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setVisible(boolean visible) {
 		super.setVisible(visible);
 		
 		//TODO we should use ResizeComposite instead of Composite
 		//The problem would be in the DeckLayoutPanel that don't call onresize
 		if (visible) {
 			reportGrid.onResize();
			reportPager.setPage(0);
 		}
 	}
 	
 	protected void setupGrid()
 	{
 		reportGrid = new DataGrid<ReportLog>(8, DataGridReportResource.INSTANCE);
 		reportGrid.setAutoHeaderRefreshDisabled(true);
 
 		reportGrid.setEmptyTableWidget(new Label("No data"));
 
 		SimplePager.Resources pagerResources = GWT.create(CotrixSimplePager.class);
 		reportPager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
 		reportPager.setDisplay(reportGrid);
 
 		Column<ReportLog, String> typeColumn = new Column<ReportLog, String>(new TextCell()) {
 			@Override
 			public String getValue(ReportLog object) {
 				return object.getType().toString();
 			}
 		};
 		typeColumn.setSortable(false);
 
 		reportGrid.addColumn(typeColumn);
 		reportGrid.setColumnWidth(typeColumn, "100px");
 		
 		Column<ReportLog, String> messageColumn = new Column<ReportLog, String>(new TextCell()) {
 			@Override
 			public String getValue(ReportLog object) {
 				return object.getMessage();
 			}
 		};
 		messageColumn.setSortable(false);
 
 		reportGrid.addColumn(messageColumn);
 
 		dataProvider.addDataDisplay(reportGrid);
 		
 	}
 	
 	public void loadReport()
 	{
 		Log.trace("requesting page 0 to report");
		reportPager.setPage(0);
 	}
 }
