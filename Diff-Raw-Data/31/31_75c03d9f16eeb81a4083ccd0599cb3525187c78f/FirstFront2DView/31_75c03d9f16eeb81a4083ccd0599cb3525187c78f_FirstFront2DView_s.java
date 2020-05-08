 package genlab.algog.gui.jfreechart.views;
 
 import genlab.algog.algos.meta.GeneticExplorationAlgoConstants;
 import genlab.algog.gui.jfreechart.instance.FirstFront2DInstance;
 import genlab.core.commons.WrongParametersException;
 import genlab.core.model.instance.IAlgoInstance;
 import genlab.core.model.instance.IParametersListener;
 import genlab.core.model.meta.basics.flowtypes.GenlabTable;
 import genlab.gui.actions.ShowParametersAction;
 import genlab.gui.algos.AbstractOpenViewAlgoExec;
 import genlab.gui.views.AbstractViewOpenedByAlgo;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.RowData;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Layout;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.JFreeChart;
 import org.jfree.data.xy.XYDataset;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 import org.jfree.experimental.chart.swt.ChartComposite;
 
 public class FirstFront2DView extends AbstractViewOpenedByAlgo<GenlabTable> implements IParametersListener {
 
 	public static final String VIEW_ID = "genlab.algog.gui.jfreechart.views.FirstFront2DView";
 	
 	protected String chartTitle = "xy";
 	protected String chartXLabel = "x";
 	protected String chartYLabel = "y";
 	
 	protected Integer glTableColumnXIdx = null;
 	protected Integer glTableColumnYIdx = null;
 	
 	XYSeries serie = null;
 			
 	protected JFreeChart chart = null;
 	protected ChartComposite compositeChart = null;
 	private Label labelIteration ;
 
 	private FormToolkit toolkit;
 	private ScrolledForm form;
 		
 	
 	
 	public FirstFront2DView() {
 		
 	}
 	
 	@Override
 	protected String getName(AbstractOpenViewAlgoExec exec) {
 		return exec.getAlgoInstance().getName();
 	}
 	
 	public void loadDataFromParameters(IAlgoInstance viewAlgoInstance) {
 		
 		// retrieve algo instance
 		FirstFront2DInstance ai = (FirstFront2DInstance)viewAlgoInstance;
 		
 		// ... so we can retrieve the values for the index parameters
 		
 		this.glTableColumnXIdx = (Integer) viewAlgoInstance.getValueForParameter(ai.getParameterColumnX());
 		this.glTableColumnYIdx = (Integer) viewAlgoInstance.getValueForParameter(ai.getParameterColumnY());
 		
 	}
 	
 	public void loadDataFromTable() {
 		
		if (lastVersionDataToDisplay == null)
 			return;
 		
 		try {
 			showBusy(true);
 
 			chartXLabel = lastVersionDataToDisplay.getColumnIdForIdx(glTableColumnXIdx);
 			chartYLabel = lastVersionDataToDisplay.getColumnIdForIdx(glTableColumnYIdx);
 		
 			// update labels
 			chart.getXYPlot().getDomainAxis(0).setLabel(chartXLabel);
 			chart.getXYPlot().getRangeAxis().setLabel(chartYLabel);
 			
 			// update data !
 			serie.setNotify(false);
 			serie.clear();
 			
 			// search for the first row to display
 			final String columnIteration = (String) lastVersionDataToDisplay.getTableMetaData(GeneticExplorationAlgoConstants.TABLE_METADATA_KEY_COLTITLE_ITERATION);
 			final int lastRowIdx = lastVersionDataToDisplay.getRowsCount()-1;
 			final Integer iterationToDisplay = (Integer)lastVersionDataToDisplay.getValue(lastRowIdx, columnIteration);
 			// search for the first line to display
 			int currentRow = lastRowIdx;
 			
 			Integer currentRowIteration = null;
 			do {
 				// we explore backwards from the final line to the first change in the exploration
 
 				Object dataX = lastVersionDataToDisplay.getValue(currentRow, glTableColumnXIdx);
 				Object dataY = lastVersionDataToDisplay.getValue(currentRow, glTableColumnYIdx);
 				
 				// ignore incomplete data
 		        if (dataX == null || dataY == null)
 		        	continue;
 		        
 		        // attempt case
 		        Number x;
 		        Number y;
 		        try {
 					x = (Number)lastVersionDataToDisplay.getValue(currentRow, glTableColumnXIdx);
 				} catch (ClassCastException e) {
 		        	messages.errorUser("wrong parameter: the column with index "+glTableColumnXIdx+" does not contains numbers", getClass());
 		        	throw new WrongParametersException("wrong parameter: the column with index "+glTableColumnXIdx+" does not contains numbers");
 		        	// TODO error !
 		        }
 		        try {
 					y = (Number)lastVersionDataToDisplay.getValue(currentRow, glTableColumnYIdx);
 		        } catch (ClassCastException e) {
 		        	messages.errorUser("wrong parameter: the column with index "+glTableColumnYIdx+" does not contains numbers", getClass());
 		        	throw new WrongParametersException("wrong parameter: the column with index "+glTableColumnYIdx+" does not contains numbers");
 		        }
 		        
 		        serie.add(x, y);
 		        
 				// shift to previous line
 				currentRow --;
 				currentRowIteration = (Integer)lastVersionDataToDisplay.getValue(currentRow, columnIteration);
 			} while (currentRow > 0 && currentRowIteration == iterationToDisplay);
 			
 			labelIteration.setText("results for iteration "+iterationToDisplay+" ("+(lastRowIdx-currentRow)+" Pareto efficient solutions)   ");
 
 		} finally {
 			serie.setNotify(true);
 			showBusy(false);	
 		}
 	}
 	
 	
 	@Override
 	public void createPartControl(Composite parent) {
 		
 		super.createPartControl(parent);
 
 		messages.traceTech("init the form...", getClass());
 		
 		toolkit = new FormToolkit(parent.getDisplay());
 		parent.setLayout(new FillLayout());
 		form = toolkit.createScrolledForm(parent);
 		RowLayout layout = new RowLayout(SWT.VERTICAL);
 		//layout.justify = true;
 		form.getBody().setLayout(layout);
 
 
 		labelIteration = toolkit.createLabel(form.getBody(), "no data displayed yet                                             ");
 
 		
 		messages.traceTech("init the jfreechart dataset...", getClass());
 		
 		XYDataset dataset = null;
 		{
 			XYSeriesCollection seriesAll = new XYSeriesCollection();
 		 
 			serie = new XYSeries("data");
 		    
 			seriesAll.addSeries(serie);
 			
 			dataset = seriesAll;
 		}
 		
 		
 		messages.traceTech("init the jfreechart chart...", getClass());
 
 		chart = ChartFactory.createScatterPlot(
 				chartTitle, 
 				chartXLabel, 
 				chartYLabel, 
 				dataset
 				);
 		chart.setAntiAlias(true);
 		
 		messages.traceTech("init the chart composite...", getClass());
 
 		final int preferedWidth = 900;
 		final int preferedHeight = 500;
 	
 		compositeChart = new ChartComposite(
 				form.getBody(), 
 				SWT.NONE, 
 				chart,
 				true // use a buffer
 				);	
 		compositeChart.setSize(preferedWidth, preferedHeight);
 		compositeChart.setLayoutData(new RowData(preferedWidth, preferedHeight));
 		compositeChart.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
 		
 		
 		// update display
 		form.layout(true);
 
 		// add parameters access
 		getViewSite().getActionBars().getToolBarManager().add(new ShowParametersAction());  
 
 	
 	}
 
 	@Override
 	public void setFocus() {
 		
 		if (form != null)
 			form.setFocus();
 		
 	}
 
 	@Override
 	public void parameterValueChanged(
 							IAlgoInstance ai, 
 							String parameterId,
 							Object novelValue) {
 		
 
 		loadDataFromParameters(ai);
 		
 		loadDataFromTable();
 		
 	}
 	
 	@Override
 	public void dispose() {
 		if (compositeChart != null)
 			compositeChart.dispose();
 		if (form != null)
 			form.dispose();
 		if (toolkit != null)
 			toolkit.dispose();
 		super.dispose();
 	}
 
 	@Override
 	public boolean isDisposed() {
 		return form.isDisposed();
 	}
 
 	@Override
 	protected void refreshDisplaySync() {
 
 		if (this.lastVersionDataToDisplay == null)
 			return;
 		
 		messages.traceTech("received data to display.", getClass());
 		
 		
 		// parameters were not loaded, let's load them
 		if (this.glTableColumnXIdx == null) {
 			this.algoInstance.addParametersListener(this);	
 			loadDataFromParameters(this.algoInstance);
 		}
 		
 		// if we are visible, let's display that
 		loadDataFromTable();	
 	}
 
 
 }
