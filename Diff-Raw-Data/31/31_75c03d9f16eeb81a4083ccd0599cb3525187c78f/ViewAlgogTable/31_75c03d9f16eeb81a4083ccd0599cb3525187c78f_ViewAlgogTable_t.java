 package genlab.algog.gui.jfreechart.views;
 
 import genlab.algog.algos.meta.GeneticExplorationAlgoConstants;
 import genlab.core.model.meta.basics.flowtypes.GenlabTable;
 import genlab.gui.algos.AbstractOpenViewAlgoExec;
 import genlab.gui.views.AbstractViewOpenedByAlgo;
 
 import java.awt.Color;
 import java.text.NumberFormat;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowData;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Layout;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.ui.forms.widgets.Section;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.annotations.XYLineAnnotation;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.experimental.chart.swt.ChartComposite;
 
 public final class ViewAlgogTable extends AbstractViewOpenedByAlgo<GenlabTable> {
 
 	public static final String VIEW_ID = "genlab.algog.gui.jfreechart.views.ViewAlgoGTable";
 	
 	protected Map<String, float[][]> goal2serie = new HashMap<String, float[][]>();
 	protected Map<String, JFreeChart> goal2chart = new HashMap<String, JFreeChart>();
 	protected Map<String, ChartComposite> goal2compositeChart = new HashMap<String, ChartComposite>();
 	protected Map<String, XYLineAnnotation> goal2targetAnnotation = new HashMap<String, XYLineAnnotation>();
 
 	protected Map<String, float[][]> gene2serie = new HashMap<String, float[][]>();
 	protected Map<String, JFreeChart> gene2chart = new HashMap<String, JFreeChart>();
 	protected Map<String, ChartComposite> gene2compositeChart = new HashMap<String, ChartComposite>();
 	
 	private FormToolkit toolkit;
 	private ScrolledForm form;
 	
 	private Section sectionGenes;
 	private Section sectionGoals;
 	
 	private Composite compoGoals;
 	private Composite compoGenes;
 		
 	/**
 	 * stores the max row line for which data was already displayed 
 	 * (avoid to redisplay everything)
 	 */
 	private Integer dataProcessedUntilRow = 0;
 	
 	
 	public ViewAlgogTable() {
 		
 	}
 	
 	@Override
 	protected String getName(AbstractOpenViewAlgoExec exec) {
 		return exec.getAlgoInstance().getName();
 	}
 	
 	private float[][] createEmptySerie() {
 		float[][] serie = new float[2][];
 		serie[0] = new float[0];
 		serie[1] = new float[0];
 		return serie;
 	}
 	
 	protected FastScatterPlotWithLines createFastScatteredChart(int maxIterations, float[][] serie) {
 		
 		
 		final NumberAxis domainAxis = new NumberAxis("iterations");
 		domainAxis.setRangeWithMargins(0, maxIterations);
 		domainAxis.setAutoRange(false);
 		domainAxis.setNumberFormatOverride(NumberFormat.getNumberInstance());
 		domainAxis.setAutoTickUnitSelection(true);
 		
 		final NumberAxis rangeAxis = new NumberAxis("value");
 		//rangeAxis.setAutoRangeIncludesZero(false);
 		//rangeAxis.setAutoTickUnitSelection(true);
 		rangeAxis.setAutoRange(true);
 		
 		FastScatterPlotWithLines plot = new FastScatterPlotWithLines(serie, domainAxis, rangeAxis);
         
 		// TODO lines with decent space
 		plot.setDomainGridlinesVisible(false);
 		plot.setRangeGridlinesVisible(false);
 
 		plot.setBackgroundPaint(Color.WHITE);
 
 		
 		return plot;
 	}
 	
 	protected void createWidgetsForGoal(String goal, Map<String,String> goalMetadata, int maxIterations) {
 
 		messages.traceTech("init the jfreechart dataset for goal "+goal+" ...", getClass());
 		
 		float[][] serie = createEmptySerie();
 				
 		messages.traceTech("init the jfreechart chart for goal "+goal+"...", getClass());
 		
 		// create chart
 		final FastScatterPlotWithLines plot = createFastScatteredChart(maxIterations, serie);
 			
 		final JFreeChart chart = new JFreeChart("goal "+goal+": target and values for iterations", plot);
 		chart.setAntiAlias(true);
 		chart.setBackgroundPaint(Color.WHITE);
 
         
 		messages.traceTech("init the chart composite for goal "+goal+"...", getClass());
 
 		final int preferedWidth = 900;
 		final int preferedHeight = 300;
 	
 		ChartComposite compositeChart = new ChartComposite(
 				compoGoals, 
 				SWT.NONE, 
 				chart,
 				true // use a buffer
 				);	
 		compositeChart.setSize(preferedWidth, preferedHeight);
 		compositeChart.setLayoutData(new RowData(preferedWidth, preferedHeight));
 		toolkit.adapt(compositeChart, true, true);
 		//compositeChart.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
 		
 		// save all of them
 		goal2chart.put(goal, chart);
 		goal2compositeChart.put(goal, compositeChart);
 		goal2serie.put(goal, serie);
 		
 		// update display
 		compoGoals.layout(true);
 		sectionGoals.layout(true);
 		
 	}
 	
 	protected boolean displayDataForGoal(String goal, Map<String,String> goalMetadata, String columnIteration, int maxIterations) {
 
 		float[][] serie = goal2serie.get(goal);
 		boolean widgetCreated = false;
 		
 		// create if necessary
 		if (serie == null) {
 			createWidgetsForGoal(goal, goalMetadata, maxIterations);
 			serie = goal2serie.get(goal);
 			widgetCreated = true;
 		}
 		
 		// update data
 		final String columnValue = goalMetadata.get(GeneticExplorationAlgoConstants.TABLE_COLUMN_GOAL_METADATA_VALUE_VALUE);
 		// TODO fitness ? final String columnFitness = goalMetadata.get(GeneticExplorationAlgo.TABLE_COLUMN_METADATA_VALUE_FITNESS);
 		final String columnTarget = goalMetadata.get(GeneticExplorationAlgoConstants.TABLE_COLUMN_GOAL_METADATA_VALUE_TARGET);
 		
 		// find the value for this target (it is available only for an individual which succedeed, so maybe 
 		// it's not in the first row ... or even in no row !
 		float targetValue;
 		{
 			Number valueTarget = null;
 			for (int i=0; i<lastVersionDataToDisplay.getRowsCount(); i++) {
 				valueTarget = (Number)lastVersionDataToDisplay.getValue(i, columnTarget);
 				if (valueTarget != null)
 					break;
 			}
 			// maybe no individual was computed with sucess; in this case there is no target transmitted, 
 			// => cancel the display
 			if (valueTarget == null) {
 				return widgetCreated;
 			}
 			targetValue = valueTarget.floatValue();
 		}
 		final FastScatterPlotWithLines plot = ((FastScatterPlotWithLines)goal2chart.get(goal).getPlot());
 		plot.setNotify(false);
 		
 		// update data !
 		try {
 			final int rowsToDisplay = lastVersionDataToDisplay.getRowsCount();
 			
 			float yLowest, yHighest;
 			
 			if (serie[0].length != 0) {
 				// init the values for first discovery of ranges !
 				yLowest = plot.getRangeLow();
 				yHighest = plot.getRangeUp();
 			} else {
 				// reuse previous values
 				yLowest = Float.POSITIVE_INFINITY;
 				yHighest = Float.NEGATIVE_INFINITY;
 			}
 			
 			// resize dataset
 			resizeDataSet(serie, rowsToDisplay);
 			
 			for (int rowId=dataProcessedUntilRow; rowId<rowsToDisplay; rowId++) {
 		    	
 				try {
 					Object dataX = lastVersionDataToDisplay.getValue(rowId, columnIteration);
 					Object dataY = lastVersionDataToDisplay.getValue(rowId, columnValue);
 					
 					// ignore incomplete data
 			        if (dataX == null || dataY == null)
 			        	continue;
 			        
 			        final float x = ((Number)dataX).floatValue();
 			        final float y = ((Number)dataY).floatValue();
 			        
 			        serie[0][rowId] = x;
 			        serie[1][rowId] = y;
 			      
 			        if (y < yLowest)
 			        	yLowest = y;
 			        if (y > yHighest)
 			        	yHighest = y;
 			        
 				} catch (ClassCastException e) {
 					// ignore, but this should not happen !
 				}
 		    }
 			
 
 			// has the line ? 
 			if (!plot.hasHorizontalLine()) {
 				plot.addHorizontalLine(targetValue, "target", Color.DARK_GRAY);
 			}
 			
 			if (yLowest > targetValue)
 				yLowest = targetValue;
 			if (yHighest < targetValue)
 				yHighest = targetValue;
 			
 			plot.setRange(yLowest, yHighest);				
 			// update data
 			// no need to update, will be displayed already plot.setData(serie);
 			
 			plot.setNotify(true);
 			
 		} finally {
 
 		}
 		
 		return widgetCreated;
 	}
 	
 	private void resizeDataSet(float[][] serie, int novelSize) {
 		
 		serie[0] = Arrays.copyOf(serie[0], novelSize);
 		serie[1] = Arrays.copyOf(serie[1], novelSize);
 		
 	}
 
 	private boolean displayDataForGene(String gene, String columnValue, 
 			Map<String,Object> metadata, String columnIteration,
 			Integer maxIterations) {
 		
 		float[][] serie = gene2serie.get(gene);
 		boolean widgetCreated = false;
 
 		
 		// create if necessary
 		if (serie == null) {
 			createWidgetsForGene(gene, maxIterations);
 			serie = gene2serie.get(gene);
 			widgetCreated = true;
 		}
 		
 		final FastScatterPlotWithLines plot = ((FastScatterPlotWithLines)gene2chart.get(gene).getPlot());
 		plot.setNotify(false);
 		
 		// update data !
 		try {
 			final int rowsToDisplay = lastVersionDataToDisplay.getRowsCount();
 			
 
 			float yLowest, yHighest;
 			
 			if (serie[0].length != 0) {
 				// reuse previous values
 				yLowest = plot.getRangeLow();
 				yHighest = plot.getRangeUp();
 			} else {
 				// init the values for first discovery of ranges !
 				yLowest = Float.POSITIVE_INFINITY;
 				yHighest = Float.NEGATIVE_INFINITY;
 			}
 			
 			// resize dataset
 			resizeDataSet(serie, rowsToDisplay);
 			
 					
 			for (int rowId=dataProcessedUntilRow ; rowId<rowsToDisplay; rowId++) {
 		    	
 				try {
 					Object dataX = lastVersionDataToDisplay.getValue(rowId, columnIteration);
 					Object dataY = lastVersionDataToDisplay.getValue(rowId, columnValue);
 					
 					// ignore incomplete data
 			        if (dataX == null || dataY == null)
 			        	continue;
 			        
 			        final float x = ((Number)dataX).floatValue();
 			        final float y = ((Number)dataY).floatValue();
 			        
 			        
 			        serie[0][rowId] = x;
 			        serie[1][rowId] = y;
 
 			        if (y < yLowest)
 			        	yLowest = y;
 			        if (y > yHighest)
 			        	yHighest = y;
 			        
 				} catch (ClassCastException e) {
 					// ignore, but this should not happen !
 				}
 		    }
 		
 
 			// has the line ? 
 			if (!plot.hasHorizontalLine() && metadata.containsKey(GeneticExplorationAlgoConstants.TABLE_COLUMN_GENE_METADATA_KEY_MIN)) {
 				final Double min = (Double)metadata.get(GeneticExplorationAlgoConstants.TABLE_COLUMN_GENE_METADATA_KEY_MIN);
 				final Double max = (Double)metadata.get(GeneticExplorationAlgoConstants.TABLE_COLUMN_GENE_METADATA_KEY_MAX);
 				plot.addHorizontalLine(min.floatValue(), "min", Color.BLUE);
 				plot.addHorizontalLine(max.floatValue(), "max", Color.BLUE);
 			}
 
			if (!Double.isInfinite(yHighest) && !Double.isInfinite(yLowest))
				plot.setRange(yLowest, yHighest);
 			
 			
 		} finally {
 			// TODO ?
 		}
 		
 		plot.setNotify(true);
 		
 		return widgetCreated;
 	}
 	
 	
 	private void createWidgetsForGene(String gene, Integer maxIterations) {
 
 		messages.traceTech("init the jfreechart dataset for gene "+gene+" ...", getClass());
 		
 		final float[][] serie = createEmptySerie();
 		
 		messages.traceTech("init the jfreechart chart for gene "+gene+"...", getClass());
 		
 		// create chart
 		final FastScatterPlotWithLines plot = createFastScatteredChart(maxIterations, serie);
 		
 		final JFreeChart chart = new JFreeChart("gene "+gene+" values per iteration", plot);
 		chart.setAntiAlias(true);
 		chart.setBackgroundPaint(Color.WHITE);
         
 		messages.traceTech("init the chart composite for gene "+gene+"...", getClass());
 
 		final int preferedWidth = 900;
 		final int preferedHeight = 300;
 	
 		ChartComposite compositeChart = new ChartComposite(
 				compoGenes, 
 				SWT.NONE, 
 				chart,
 				true // use a buffer
 				);	
 		compositeChart.setSize(preferedWidth, preferedHeight);
 		compositeChart.setLayoutData(new RowData(preferedWidth, preferedHeight));
 		toolkit.adapt(compositeChart, true, true);
 		
 		
 		// save all of them
 		gene2chart.put(gene, chart);
 		gene2compositeChart.put(gene, compositeChart);
 		gene2serie.put(gene, serie);
 		
 		// update display
 		compoGenes.layout(true);
 		sectionGenes.layout(true);
 
 	}
 
 	public void loadDataFromTable() {
 		
 		if (lastVersionDataToDisplay == null)
 			return;
 		
 		
 		try {
 			showBusy(true);
 			
 			boolean widgetCreated = false;
 
 			final String columnIteration = (String) lastVersionDataToDisplay.getTableMetaData(GeneticExplorationAlgoConstants.TABLE_METADATA_KEY_COLTITLE_ITERATION);
 			final Integer maxIterations = (Integer)lastVersionDataToDisplay.getTableMetaData(GeneticExplorationAlgoConstants.TABLE_METADATA_KEY_MAX_ITERATIONS);
 			
 			{
 				Object metadataRawGoals = lastVersionDataToDisplay.getTableMetaData(GeneticExplorationAlgoConstants.TABLE_METADATA_KEY_GOALS2COLS);
 				if (metadataRawGoals == null) {
 					messages.warnTech("unable to find the expected metadata in this table; maybe it does not comes from a genetic algorithm ?", getClass());
 					return;
 				}
 				Map<String,Map<String,String>> metadata = null;
 				try {
 					metadata = (Map<String,Map<String,String>>)metadataRawGoals;
 				} catch (ClassCastException e) {
 					messages.warnTech("wrong metadata in this table; maybe it does not comes from a genetic algorithm ?", getClass());
 					return;
 				}
 					
 				// display goals
 				for (String goal: metadata.keySet()) {
 										
 					widgetCreated = displayDataForGoal(goal, metadata.get(goal), columnIteration, maxIterations) || widgetCreated;
 					
 				}
 			}
 			
 			{
 				Object metadataRawGenes = lastVersionDataToDisplay.getTableMetaData(GeneticExplorationAlgoConstants.TABLE_METADATA_KEY_GENES2METADATA);
 				if (metadataRawGenes == null) {
 					messages.warnTech("unable to find the expected metadata in this table; maybe it does not comes from a genetic algorithm ?", getClass());
 					return;
 				}
 				Map<String,Object> metadataValues = null;
 				try {
 					metadataValues = (Map<String,Object>)metadataRawGenes;
 				} catch (ClassCastException e) {
 					messages.warnTech("wrong metadata in this table; maybe it does not comes from a genetic algorithm ?", getClass());
 					return;
 				}
 					
 				// display genes
 				for (String gene: metadataValues.keySet()) {
 					final Map<String, Object> metadata = (Map<String, Object>) metadataValues.get(gene);
 					final String columnValue = (String)metadata.get(GeneticExplorationAlgoConstants.TABLE_COLUMN_GENE_METADATA_KEY_VALUE);
 	
 					widgetCreated = displayDataForGene(gene, columnValue, metadata, columnIteration, maxIterations) || widgetCreated;
 					
 				}
 			}
 			
 			dataProcessedUntilRow = lastVersionDataToDisplay.getRowsCount();
 
 		
 			if (widgetCreated) {
 				form.reflow(true);
 				form.getParent().layout(true, true);
 			}
 		} catch(RuntimeException e) {
 			messages.warnTech("an error occured while displaying algog data: "+e.getMessage(), getClass(), e);
 			e.printStackTrace();
 		} finally {
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
 		Layout layout = new GridLayout(1, false);
 		form.getBody().setLayout(layout);
 
 		
 		sectionGoals = toolkit.createSection(form.getBody(),  Section.TITLE_BAR);
 		sectionGoals.setExpanded(true);
 		sectionGoals.setText("Measures and targets");
 		sectionGoals.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
 		
 		compoGoals = toolkit.createComposite(sectionGoals);
 		compoGoals.setLayout(new RowLayout(SWT.VERTICAL));
 		sectionGoals.setClient(compoGoals);
 		
 		sectionGenes = toolkit.createSection(form.getBody(),  Section.TITLE_BAR);
 		sectionGenes.setExpanded(true);
 		sectionGenes.setText("Genes");
 		sectionGenes.setLayout(new RowLayout(SWT.VERTICAL));
 		sectionGenes.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
 		
 		compoGenes = toolkit.createComposite(sectionGenes);
 		compoGenes.setLayout(new RowLayout(SWT.VERTICAL));
 		sectionGenes.setClient(compoGenes);
 		
 		
 		// update display
 		form.layout(true);
 		form.reflow(true);
 		form.getParent().layout(true, true);
 		
 		// now listen for part display
 		
 		
 	}
 
 	@Override
 	public void setFocus() {
 		
 		if (form != null)
 			form.setFocus();
 		
 	}
 
 	@Override
 	public void dispose() {
 		
 		// clear charts
 		for (ChartComposite compositeChart : goal2compositeChart.values()) {
 			compositeChart.dispose();
 		}
 		goal2chart.clear();
 		goal2compositeChart.clear();
 		goal2serie.clear();
 		goal2targetAnnotation.clear();
 		
 		if (form != null)
 			form.dispose();
 		if (toolkit != null)
 			toolkit.dispose();
 		
 		super.dispose();
 	}
 
 	@Override
 	public boolean isDisposed() {
 		return form != null && form.isDisposed();
 	}
 
 	@Override
 	public String toString() {
 		return getClass().getSimpleName();
 	}
 
 
 	@Override
 	protected void refreshDisplaySync() {
 		loadDataFromTable();	
 		
 	}
 
 }
