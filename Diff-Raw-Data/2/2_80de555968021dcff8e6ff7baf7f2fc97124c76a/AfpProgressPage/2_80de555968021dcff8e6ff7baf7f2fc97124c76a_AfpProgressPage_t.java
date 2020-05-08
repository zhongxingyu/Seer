 package org.amanzi.awe.afp.wizards;
 
 import java.awt.Color;
 import java.lang.reflect.InvocationTargetException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.TimeZone;
 
 import javax.swing.SwingUtilities;
 
 import org.amanzi.awe.afp.executors.AfpProcessExecutor;
 import org.amanzi.awe.afp.executors.AfpProcessProgress;
 import org.amanzi.awe.afp.exporters.AfpExporter;
 import org.amanzi.awe.afp.loaders.AfpOutputFileLoaderJob;
 import org.amanzi.awe.afp.models.AfpModel;
 import org.amanzi.awe.console.AweConsolePlugin;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.jobs.ISchedulingRule;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.axis.ValueAxis;
 import org.jfree.chart.plot.CategoryPlot;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.data.category.CategoryDataset;
 import org.jfree.data.category.DefaultCategoryDataset;
 import org.jfree.data.time.DateRange;
 import org.jfree.data.time.Millisecond;
 import org.jfree.data.time.RegularTimePeriod;
 import org.jfree.data.time.TimeSeries;
 import org.jfree.data.time.TimeSeriesDataItem;
 import org.jfree.data.xy.XYDataset;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 import org.jfree.experimental.chart.swt.ChartComposite;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.jfree.data.time.TimeSeriesCollection;
 
 
 public class AfpProgressPage extends AfpWizardPage implements AfpProcessProgress{
 	
 	JFreeChart chart;
 	TimeSeries series[];
 	TimeSeriesCollection dataset = new TimeSeriesCollection();
     XYLineAndShapeRenderer renderer;
     Shell parentShell;
 	private AfpProcessExecutor afpJob;
 	private AfpExporter exportJob;
 	private AfpOutputFileLoaderJob loadJob;
     //private JFreeChart chart;
     private Button[] colorButtons = new Button[8];
 	private String[] graphParams = new String[]{
 		"Total",
 		"Sector Separations",
 		"Interference",
 		"Site Separations",
 		"Neighbour",
 		"Frequency Constraints",
 		"Triangulation",
 		"Shadowing"
 	};
 	private Color[] seriesColors = new Color[] {
 			Color.RED,
 			Color.ORANGE,
 			Color.YELLOW,
 			Color.GREEN,
 			Color.BLUE,
 			Color.LIGHT_GRAY,
 			Color.GRAY,
 			Color.CYAN			
 	};
 	Button stopButton;
 	private boolean[] seriesVisible = new boolean[]{ true,true,true,true,true,true,true,true};
 	private Button[] paramButtons = new Button[8];
 	private Table progressTable;
 	Group summaryGroup;
 	Label[] summaryData = new Label[5];
 	
 	public AfpProgressPage(String pageName, AfpModel model, String desc) {
 		super(pageName, model);
         setTitle(AfpImportWizard.title);
         setDescription(desc);
         setPageComplete (false);
 	}
 
 	
 	@Override
 	public void createControl(Composite parent) {
 		this.parentShell = parent.getShell();
 		Composite main = new Composite(parent, SWT.NONE);
    	 	main.setLayout(new GridLayout(2, false));
    	 	main.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 2, 2));
    	 	
    	 	summaryGroup = new Group(main, SWT.NONE);
    	 	summaryGroup.setLayout(new GridLayout(2, false));
    	 	summaryGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1));
    	 	summaryGroup.setText("Summary");
    	 	
    	 	new Label(summaryGroup, SWT.LEFT).setText("Selected Sectors:");
    	 	summaryData[0] = new Label(summaryGroup, SWT.LEFT);
 	   	new Label(summaryGroup, SWT.LEFT).setText("Selected TRXs:");
 	   	summaryData[1] = new Label(summaryGroup, SWT.LEFT);
 	   	new Label(summaryGroup, SWT.LEFT).setText("BCCH TRXs:");
 	   	summaryData[2] = new Label(summaryGroup, SWT.LEFT);
 	   	new Label(summaryGroup, SWT.LEFT).setText("TCH Non/BB Hopping TRXs");
 	   	summaryData[3] = new Label(summaryGroup, SWT.LEFT);
 	   	new Label(summaryGroup, SWT.LEFT).setText("TCH SY Hopping TRXs");
 	   	summaryData[4] = new Label(summaryGroup, SWT.LEFT);
 	   	for(Label l: summaryData) {
 	   		l.setText("0");
 	   	}
    	 	
 	   	TabFolder tabFolder =new TabFolder(main, SWT.NONE | SWT.BORDER);
 		tabFolder.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 1, 2));
 		
 		TabItem item1 =new TabItem(tabFolder,SWT.NONE);
 		item1.setText("Graph");
 		
         //chart = createChart();
 		Group graphGroup = new Group(tabFolder, SWT.NONE);
 		graphGroup.setLayout(new GridLayout(6, false));
 		graphGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1));
 		
 		chart = createChart(createDataset());
 		GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, true, 6 ,1);
 		gridData.heightHint = 200;
 		gridData.minimumWidth = 100;
 	
 		final ChartComposite frame = new ChartComposite(graphGroup, SWT.NONE, chart, true);
 		frame.setLayoutData(gridData);
 		
 		
 		Display display = parent.getShell().getDisplay();
 		for (int i = 0; i < graphParams.length; i++){
 			colorButtons[i] = new Button(graphGroup, SWT.PUSH);
 			colorButtons[i].setBackground(
 					new org.eclipse.swt.graphics.Color(display, 
 							this.seriesColors[i].getRed(),
 							this.seriesColors[i].getGreen(),this.seriesColors[i].getBlue())); 
 					
 			paramButtons[i] = new Button(graphGroup, SWT.CHECK);
 			paramButtons[i].setSelection(true);
 			paramButtons[i].setText(graphParams[i]);
 			paramButtons[i].setData(new Integer(i));
 			paramButtons[i].setEnabled(false);
 			paramButtons[i].addSelectionListener(new SelectionAdapter(){
 	    		@Override
 				public void widgetSelected(SelectionEvent e) {
     				Button button = ((Button)e.getSource());
 	    			
 	    			if(button.getData() != null) {
 	    				int i = ((Integer)button.getData()).intValue();
 	    				
 	    				if(button.getSelection()) {
 	    					seriesVisible[i] = true;
 	    					dataset.addSeries(series[i]);
 	    				} else {
 	    					seriesVisible[i] = false;
 	    					dataset.removeSeries(series[i]);
 	    				}
 	    		        for(int k=0, j=0;k<series.length;k++) {
 	    		        	if(seriesVisible[k]) {
 	    		        		renderer.setSeriesPaint(j, seriesColors[k]);
 	    		        		j++;
 	    		        	}
 	    		        }
 	    			}
 				}
 	    	});
 
 		}
 		paramButtons[0].setEnabled(true);
 		colorButtons[0].setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 4));
 		paramButtons[0].setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 4));
 		colorButtons[5].setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, true, 1, 2));
 		paramButtons[5].setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, true, 1, 2));
 		
 		item1.setControl(graphGroup);
 		
 		TabItem item2 =new TabItem(tabFolder,SWT.NONE);
 		item2.setText("Table");
 		
 		Group tableGroup = new Group(tabFolder, SWT.NONE);
 		tableGroup.setLayout(new GridLayout(6, false));
 		tableGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1));
 		
 		progressTable = new Table(tabFolder, SWT.VIRTUAL | SWT.MULTI);
 		progressTable.setHeaderVisible(true);
 		progressTable.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, true));
 		
 		String[] headers = {"Time", "Total", "Sector Separations", "Site Separations", "Frequency Constrinats", "Interference"};
 	    for (String item : headers) {
 	      TableColumn column = new TableColumn(progressTable, SWT.NONE);
 	      column.setText(item);
 	    }
 	    
 		item2.setControl(progressTable);
 		
 		
 		Group controlGroup = new Group(main, SWT.NONE);
 		controlGroup.setLayout(new GridLayout(1, false));
 		controlGroup.setLayoutData(new GridData(GridData.FILL, GridData.END, true, false, 1, 1));
 		controlGroup.setText("Control");
 		
 		Button pauseButton = new Button(controlGroup, SWT.PUSH);
 		pauseButton.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, false, false));
 		pauseButton.setText("Pause");
 		pauseButton.setEnabled(false);
 		pauseButton.addSelectionListener(new SelectionListener(){
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 		});
 		
 		Button resumeButton = new Button(controlGroup, SWT.PUSH);
 		resumeButton.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, false, false));
 		resumeButton.setText("Resume");
 		resumeButton.setEnabled(false);
 		resumeButton.addSelectionListener(new SelectionListener(){
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 		});
 		
 		stopButton = new Button(controlGroup, SWT.PUSH);
 		stopButton.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, true, true));
 		stopButton.setText("Stop");
 		stopButton.addSelectionListener(new SelectionListener(){
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				// TODO Auto-generated method stub
 				afpJob.cancel();
 				//((AfpImportWizard)getContainer()).isDone = true;
 				//((WizardDialog)getContainer()).close();
 			}
 			
 		});
 		
 		setPageComplete (true);
 		setControl(main);
 	}
 	
 	@Override
 	public IWizardPage getPreviousPage(){
 		return null;
 	}
 	
 	@Override
 	public void refreshPage() {
 	    int[][] items = model.getSelectedCount();
 	    int[] totals = new int[items.length];
 
 	    for(int i=0;i< items.length;i++) {
 	    	totals[i] =0;
 	    	for(int item: items[i]) {
 	    		totals[i]+= item;
 	    	}
 	    }
 	    int i=0;
 	    for(Label l: summaryData) {
 	   		l.setText(""+totals[i]);
 	   		i++;
 	   	}
 	    summaryGroup.layout();
 
 		((AfpImportWizard)this.getWizard()).setDone(false);
 		
 		executeAfpEngine();
 
 	}
 	public void addProgressTableItem (String[] itemValues){
 		TableItem item = new TableItem(progressTable, SWT.NONE);
 	    
 	    for (int i = 0; i < itemValues.length; i++) {
 	    	item.setText(i, itemValues[i]);
 	    	progressTable.getColumn(i).pack();
 	    }
 	}
 
 	private XYDataset createDataset() {
 
 		series = new TimeSeries[graphParams.length];
         
 		for(int i=0; i< graphParams.length;i++) {
 			series[i] = new TimeSeries(graphParams[i]);
 			
 	        dataset.addSeries(series[i]);
 		}
         return dataset;
         
     }
 
 	/**
 	 * Creates a chart.
 	 * 
 	 * @param dataset
 	 *            dataset.
 	 * 
 	 * @return A chart.
 	 */
 	 private JFreeChart createChart(final XYDataset dataset) {
 	        
 	        // create the chart...
 	        final JFreeChart chart = ChartFactory.createTimeSeriesChart(
 	            null,      // chart title
 	            "Time",                      // x axis label
 	            "Sectors",                      // y axis label
 	            dataset,                  // data
 	            false,                     // include legend
 	            true,                     // tooltips
 	            false                     // urls
 	        );
 
 	        chart.setBackgroundPaint(Color.white);
 
 	        // get a reference to the plot for further customisation...
 	        final XYPlot plot = chart.getXYPlot();
 	        plot.setBackgroundPaint(Color.lightGray);
 	        plot.setDomainGridlinePaint(Color.white);
 	        plot.setRangeGridlinePaint(Color.white);
 	        ValueAxis timeaxis = plot.getDomainAxis();
 	        timeaxis.setAutoRange(true);
 	        timeaxis.setFixedAutoRange(60000.0);
 	        timeaxis.setRange(new DateRange(System.currentTimeMillis(),System.currentTimeMillis() + 1000000));
 	        renderer = new XYLineAndShapeRenderer();
 	        renderer.setSeriesLinesVisible(0, true);
 	        renderer.setSeriesShapesVisible(1, false);
 	        plot.setRenderer(renderer);
 	        for(int i=0;i<series.length;i++) {
 	        	renderer.setSeriesPaint(i, this.seriesColors[i]);
 	        }
 
 	        // change the auto tick unit selection to integer units only...
 	        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
 	        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
 	        //rangeAxis.setRange(new DateRange(System.currentTimeMillis(),System.currentTimeMillis() + 1000000));
 	        //rangeAxis.setLowerBound(min)
 	        // OPTIONAL CUSTOMISATION COMPLETED.
 	                
 	        return chart;
 	    }
 
 
 	@Override
 	public void onProgressUpdate(int result,long time, long remaingtotal,
 			long sectorSeperations, long siteSeperation, long freqConstraints,
 			long interference, long neighbor, long tringulation, long shadowing) {
 		
 		
 		if(result == 0) {
 			RegularTimePeriod t = new Millisecond(new Date(time));
 	
 			series[0].add(new TimeSeriesDataItem(t,remaingtotal),false);
 			
 			Display.getDefault().syncExec( new Runnable() {
 				public void run() {
 					series[0].fireSeriesChanged();
 					TimeSeriesDataItem lastItem = series[0].getDataItem(series[0].getItemCount()-1);
 					//series[0].getItemCount(); 
 					//model.setTableItems(new String[]{new Date().toString(), "dummy", "dummy", "dummy", "dummy", "dummy"});
 					long t = lastItem.getPeriod().getFirstMillisecond();
 					Date d = new Date(t);
 					SimpleDateFormat df = new SimpleDateFormat();
					df.applyPattern("H:mm:ss.S");
 					addProgressTableItem(new String[] {df.format(d), ""+lastItem.getValue(), "    ", "    ", "    ", "    "});
 				}
 			});
 		} else {
 			Display.getDefault().syncExec( new Runnable() {
 				public void run() {
 					stopButton.setEnabled(false);
 					MessageBox messageBox = new MessageBox(parentShell,SWT.OK| SWT.ICON_INFORMATION); 
 					messageBox.setMessage("AFP Engine: Execution finished"); 
 					messageBox.open();
 					AfpImportWizard wizard = (AfpImportWizard)getWizard();
 					wizard.setDone(true);
 					setPageComplete(true);
 					
 				}
 			});
 			
 		}
 	}
 	
 	void executeAfpEngine() {
 		class SchedulingRule implements ISchedulingRule {
 		      public boolean isConflicting(ISchedulingRule rule) {
 		         return rule == this;
 		      }
 		      public boolean contains(ISchedulingRule rule) {
 		         return rule == this;
 		      }
 		}
 		SchedulingRule rule = new SchedulingRule();
 		exportJob = model.getExporter();
 		exportJob.setRule(rule);
 		
 		loadJob = new AfpOutputFileLoaderJob("Load generated Network", model.getDatasetNode(), model.getAfpNode(), exportJob);
 		loadJob.setRule(rule);
 		
 		if(afpJob == null) {
 			model.executeAfpEngine(this, exportJob);
 			afpJob = model.getExecutor();
 			afpJob.setRule(rule);
 			exportJob.schedule();
 			afpJob.schedule();
 			loadJob.schedule();
 		}		    
 	}
 }
