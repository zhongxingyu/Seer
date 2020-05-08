 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.ListIterator;
 
 import org.eclipse.swt.widgets.Display;
 import java.util.Map;
 
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowData;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.wb.swt.SWTResourceManager;
 import org.swtchart.Chart;
 import org.swtchart.ILineSeries;
 import org.swtchart.ISeries;
 import org.swtchart.ISeries.SeriesType;
 
 public class MainWindow {
 	
 	public static Shell shlSwtApplication;
 	public static Composite composite;
 	public static Table table;
 	private static final FormToolkit formToolkit = new FormToolkit(Display.getDefault());
 	public static Chart history;
 	public static Button btnRecompile;
 	public static CUDACode ppCUDACode;
 	public static PTXScanner ppPTXScanner;
 	public static int currentLine;
 	public static String CUfilename;
 	public static File CUpath;
 	public static File PTXpath;
 	public static Display display;
 	public static CUDAGauge uncoalesced;
 	public static CUDAGauge occupancy;
 	public static CUDAGauge conflicts;
 	public static ProfileMap pMap;
 	public static String [] commandLineArgs;
 	public static ArrowCanvas arrow_canvas;
 	public static ArrayList<Double> execution_times = new ArrayList<Double>();
 	final static File ding = new File( "res" + File.separator + "ding.wav" );
 	private static TableColumn tblclmnCycles;
 	
 
 	public static void main(String[] args) {
 
 		/**********************************************************************************
 		 * BEGIN WINDOW CONTROL DECLARATIONS
 		 */
 		commandLineArgs = args;
 		display = Display.getDefault();
 		shlSwtApplication = new Shell(SWT.DIALOG_TRIM);
 		shlSwtApplication.setDragDetect(false);
 		shlSwtApplication.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
 		shlSwtApplication.setSize(1094, 504);
 		shlSwtApplication.setText("CUDA IDE");
 		shlSwtApplication.setLayout(new GridLayout(3, false));
 
 		Label lblHistory = formToolkit.createLabel(shlSwtApplication,"PERFORMANCE HISTORY", SWT.NONE);
 		lblHistory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
 		GridData gd_lblHistory = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1);
 		gd_lblHistory.heightHint = 12;
 		lblHistory.setLayoutData(gd_lblHistory);
 		lblHistory.setFont(SWTResourceManager.getFont("Segoe UI", 7, SWT.NORMAL));
 		new Label(shlSwtApplication, SWT.NONE);
 
 		Label lblEfficiencyMetrics = new Label(shlSwtApplication, SWT.NONE);
 		lblEfficiencyMetrics.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
 		formToolkit.adapt(lblEfficiencyMetrics, true, true);
 		lblEfficiencyMetrics.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
 		lblEfficiencyMetrics.setFont(SWTResourceManager.getFont("Segoe UI", 7, SWT.NORMAL));
 		lblEfficiencyMetrics.setText("EFFICIENCY METRICS");
 
 		history = new Chart(shlSwtApplication, SWT.BORDER);
 		history.getTitle().setText("");
 		history.getAxisSet().getXAxis(0).getTick().setVisible(false);
 		history.getAxisSet().getYAxis(0).getTick().setVisible(false);
 		history.getAxisSet().getXAxis(0).getTitle().setText("");
 		history.getAxisSet().getYAxis(0).getTitle().setText("");
 		history.getLegend().setVisible( false );
 		GridData gd_history = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
 		gd_history.heightHint = 160;
 		gd_history.widthHint = 534;
 		history.setLayoutData(gd_history);
 		formToolkit.adapt(history);
 		formToolkit.paintBordersFor(history);
 		new Label(shlSwtApplication, SWT.NONE);
 
 		composite = new Composite(shlSwtApplication, SWT.NONE);
 		composite.setEnabled(false);
 		composite.setLayout(new RowLayout(SWT.HORIZONTAL));
 		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
 		gd_composite.widthHint = 489;
 		composite.setLayoutData(gd_composite);
 		formToolkit.adapt(composite);
 		composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
 		formToolkit.paintBordersFor(composite);
 		
 		
 		occupancy = new CUDAGauge( composite, SWT.NONE );
 		occupancy.setGaugeType("occupancy");
 		occupancy.setLayoutData(new RowData(160, 160));
 		formToolkit.adapt(occupancy);
 		occupancy.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
 		formToolkit.paintBordersFor(occupancy);	
 		
 		uncoalesced = new CUDAGauge( composite, SWT.NONE );
 		uncoalesced.setGaugeType( "uncoalesced" );
 		uncoalesced.setLayoutData(new RowData(160, 160));
 		formToolkit.adapt(uncoalesced);
 		uncoalesced.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
 		formToolkit.paintBordersFor(uncoalesced);		
 		
 		conflicts = new CUDAGauge(composite, SWT.NONE );
 		conflicts.setGaugeType( "bankconflicts" );
 		conflicts.setLayoutData(new RowData(160, 160));
 		formToolkit.adapt(conflicts);
 		conflicts.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
 		formToolkit.paintBordersFor(conflicts);
 		
 		Label lblCudaCode = formToolkit.createLabel(shlSwtApplication,"CUDA CODE", SWT.NONE);
 		lblCudaCode.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
 		GridData gd_lblCudaCode = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1);
 		gd_lblCudaCode.heightHint = 13;
 		lblCudaCode.setLayoutData(gd_lblCudaCode);
 		lblCudaCode.setFont(SWTResourceManager.getFont("Segoe UI", 7,SWT.NORMAL));
 		new Label(shlSwtApplication, SWT.NONE);
 
 		Label lblPtxAssemblerBreakdown = formToolkit.createLabel(shlSwtApplication, "PTX ASSEMBLER BREAKDOWN", SWT.NONE);
 		lblPtxAssemblerBreakdown.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
 		GridData gd_lblPtxAssemblerBreakdown = new GridData(SWT.LEFT,SWT.BOTTOM, false, false, 1, 1);
 		gd_lblPtxAssemblerBreakdown.heightHint = 12;
 		lblPtxAssemblerBreakdown.setLayoutData(gd_lblPtxAssemblerBreakdown);
 		lblPtxAssemblerBreakdown.setFont(SWTResourceManager.getFont("Segoe UI",7, SWT.NORMAL));
 
 		ppCUDACode = new CUDACode(shlSwtApplication, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
 		ppCUDACode.setMarginColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_DARK_SHADOW));
 		ppCUDACode.setLeftMargin(50);
 		ppCUDACode.setTabs(3);
 		ppCUDACode.setDoubleClickEnabled(false);
 		ppCUDACode.setDragDetect(false);
 		ppCUDACode.setWordWrap(false);
 		GridData gd_ppCUDACode = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
 		gd_ppCUDACode.heightHint = 197;
 		gd_ppCUDACode.widthHint = 324;
 		ppCUDACode.setLayoutData(gd_ppCUDACode);
 
 		// add custom event listeners
 		
 		arrow_canvas = new ArrowCanvas(shlSwtApplication, SWT.NONE);
 		GridData gd_arrow_canvas = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
 		gd_arrow_canvas.widthHint = 46;
 		gd_arrow_canvas.heightHint = 208;
 		arrow_canvas.setLayoutData(gd_arrow_canvas);
 		arrow_canvas.setVisible(false);
 		formToolkit.adapt(arrow_canvas);
 		formToolkit.paintBordersFor(arrow_canvas);
 		
 		table = new Table(shlSwtApplication, SWT.BORDER | SWT.FULL_SELECTION);
 		GridData gd_table = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
 		gd_table.heightHint = 166;
 		gd_table.widthHint = 375;
 		table.setLayoutData(gd_table);
 		table.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 		
 		tblclmnCycles = new TableColumn(table, SWT.NONE);
 		tblclmnCycles.setWidth(49);
 		tblclmnCycles.setText("Cycles");
 
 		TableColumn tblclmnInstruction = new TableColumn(table, SWT.NONE);
 		tblclmnInstruction.setResizable(false);
 		tblclmnInstruction.setWidth(136);
 		tblclmnInstruction.setText("Instruction");
 		
 		TableColumn tblclmnArgument = new TableColumn(table, SWT.NONE);
 		tblclmnArgument.setResizable(false);
 		tblclmnArgument.setWidth(100);
 		tblclmnArgument.setText("Argument 1");
 
 		TableColumn tblclmnArgument_1 = new TableColumn(table, SWT.NONE);
 		tblclmnArgument_1.setResizable(false);
 		tblclmnArgument_1.setWidth(100);
 		tblclmnArgument_1.setText("Argument 2");
 
 		TableColumn tblclmnArgument_2 = new TableColumn(table, SWT.LEFT);
 		tblclmnArgument_2.setResizable(false);
 		tblclmnArgument_2.setWidth(100);
 		tblclmnArgument_2.setText("Argument 3");
 		new Label(shlSwtApplication, SWT.NONE);
 		new Label(shlSwtApplication, SWT.NONE);
 		btnRecompile = new Button(shlSwtApplication, SWT.NONE);
 		btnRecompile.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
 		btnRecompile.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
 		GridData gd_btnRecompile = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
 		gd_btnRecompile.heightHint = 27;
 		btnRecompile.setLayoutData(gd_btnRecompile);
 		formToolkit.adapt(btnRecompile, true, true);
 		btnRecompile.setText("RECOMPILE");
 		
 		btnRecompile.addMouseListener(new MouseAdapter() {
 			
 			public void mouseDown(MouseEvent e) {
 				if ( MainWindow.btnRecompile.isEnabled() ) {
 					try {
 						MainWindow.ppCUDACode.save(MainWindow.CUpath.getPath());
 			            Display.getCurrent().asyncExec(new Runnable() {
 			               public void run() {
 			                  try {
 			                	  MainWindow.compile( MainWindow.commandLineArgs );
 			                  } catch (Exception e) {
 			                  }
 			               }
 			            });
 						
 					} catch (Exception x) {
 						/*
 						 * MessageBox mb = new MessageBox( shell, SWT.OK ); mb.setText(
 						 * "Fatal Error" ); mb.setMessage( e.getMessage() ); mb.open();
 						 */
 						
 	//					System.out.println(MainWindow.cur_pb.directory());			
 	//					System.out.println(MainWindow.cur_pb.environment());			
 	//					System.out.println(MainWindow.cur_pb.command());
 	
 						System.err.println(x.getMessage());
 						MainWindow.display.dispose();
 						System.exit(1);
 					} finally {
 	
 					}
 				}
 			}
 			
 		});
 		
 
 		/*******************************************************************
 		 * END WINDOW CONTROL DECLARATIONS
 		 */
 
 		// Program Initialization
 		try {
 
 			// check arguments
 			if (args.length < 1)
 				throw new RuntimeException(
 						"usage: CUDAIDE filename.cu [other files needed for compilation]");
 			if (args[0].substring(args[0].length() - 3).toLowerCase()
 					.equals(".cu") == false)
 				throw new RuntimeException(
 						"usage: CUDAIDE filename.cu [other files needed for compilation]");
 
 			// open and read the CU file
 			CUpath = new File(args[0]);
 			if (!CUpath.exists())
 				throw new IOException(CUpath.getPath() + " does not exist!");
 			CUfilename = CUpath.getName();	
 			
 			shlSwtApplication.open();
 			shlSwtApplication.layout();
 			arrow_canvas.setDimensions(ppCUDACode.getBounds(), table.getBounds());
 
 			compile( commandLineArgs );
 			
 		} catch (Throwable e) {
 			/*
 			 * MessageBox mb = new MessageBox( shell, SWT.OK ); mb.setText(
 			 * "Fatal Error" ); mb.setMessage( e.getMessage() ); mb.open();
 			 */
 			
 //			System.out.println(cur_pb.directory());			
 //			System.out.println(cur_pb.environment());			
 //			System.out.println(cur_pb.command());
 
 			System.err.println(e.getMessage());
 			display.dispose();
 			System.exit(1);
 		} finally {
 
 		}
 
 		while (!shlSwtApplication.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 	}
 
 	public static void compile( String [] args ) throws IOException, InterruptedException
 	{
 		MainWindow.btnRecompile.setEnabled( false );
 		/*************** compile CU into commented PTX *****************/
 		// build command line and execute
 		ArrayList<String> command = new ArrayList<String>(Arrays.asList(
 				"nvcc", "-ptx", "-Xopencc=\"-LIST:source=on\" -O0",
 				"-Xptxas=-O0"));
 		command.addAll(Arrays.asList(args));
 		Runtime rt = Runtime.getRuntime();
 
 		ProcessBuilder cur_pb = new ProcessBuilder(command);
 
 		
 		Process cur_p = cur_pb.start();
 
 		if (cur_p.waitFor() != 0) { // if NVCC compilation fails . . .
 			BufferedReader stderr = new BufferedReader(
 					new InputStreamReader(cur_p.getErrorStream()));
 			String err = "";
 			String err_in;
 			while ((err_in = stderr.readLine()) != null)
 				err += err_in;
 			throw new IOException("Compile failed: " + err);
 		}
 		
 
 		// create File object for the newly-generated PTX file and make it
 		// temporary
 		PTXpath = new File(CUfilename.substring(0,
 				CUfilename.length() - 3).concat(".ptx"));
 		if (!PTXpath.exists())
 			throw new IOException(PTXpath.getPath() + " does not exist!");
 		ppPTXScanner = new PTXScanner(); 
 		ppPTXScanner.readIn(CUpath.getPath(), PTXpath.getPath());
 		PTXpath.deleteOnExit();
 		
 		/**************** compile CU again into executable and run it for profiling ********************/
 		// set up command line
 		command.clear();
 		command.addAll(Arrays.asList("time", "nvcc", "-run"));
 		command.addAll(Arrays.asList(args));
 		// execute NVCC and run the program
 		cur_pb = new ProcessBuilder(command);
 
 		Map<String, String> env = cur_pb.environment();
 		env.put("COMPUTE_PROFILE", "1");
 		env.put("COMPUTE_PROFILE_LOG", "cudaide.log");
 		env.put("COMPUTE_PROFILE_CONFIG", "config");
 		cur_pb.redirectErrorStream();
 		
 		cur_p = cur_pb.start();
 		BufferedReader stdin = new BufferedReader(new InputStreamReader(
 				cur_p.getInputStream()));
 		String in = "";
 		String in_in;
 		while ((in_in = stdin.readLine()) != null){
 			in += in_in;
 		}
 
 		System.out.println(in);
 
 		command.clear();
 		command.addAll(Arrays.asList("time", "./test/a.out"));
 		command.addAll(Arrays.asList(args));
 		cur_pb = new ProcessBuilder(command);
 
 		cur_pb.redirectErrorStream();
 		
 		BufferedReader stderr = new BufferedReader(new InputStreamReader(
 				cur_p.getErrorStream()));
 		String err = "";
 		String err_in;
 		double dataPoint = 0.0; 
 		while ((err_in = stderr.readLine()) != null){
 			if(err_in.contains("user"))
 				dataPoint = Float.parseFloat(err_in.substring(0, err_in.indexOf("user")));
 		}
 		
 		BufferedReader CUcode = new BufferedReader(new FileReader(CUpath));
 		String cudacode = "";
 		String cudacode_in;
 		while ((cudacode_in = CUcode.readLine()) != null)
 			cudacode += cudacode_in + "\n";
 		ppCUDACode.setText(cudacode);
 		ppCUDACode.resetCaret();
 
 		CUcode.close();		
 		pMap = new ProfileMap("cudaide.log");
 		ChangeGauges( (int)(pMap.average("occupancy") * 100.0), (int)((1.0 - (pMap.average("gld_incoherent") / (pMap.average("gld_incoherent") + pMap.average("gld_coherent")))) * 100.0), (int)((1 - (pMap.average("warp_serialize") / pMap.average("instructions"))) * 100.0));
 		// play sound
 		try {
 			AudioInputStream au = AudioSystem.getAudioInputStream( MainWindow.ding );
 			Clip clip = AudioSystem.getClip();
 			
 			clip.open( au );
 			clip.start();
 			
 		} catch ( Exception ex ) {
 			// do nothing--no sound is okay, too
 		}
 //		System.out.println("" + dataPoint);
 		if(dataPoint != 0)
 			MarkChart( dataPoint );
 		MainWindow.btnRecompile.setEnabled( true );
 	}
 		
 	public static void ChangeGauges( int occupancy_stat, int uncoalesced_stat, int conflicts_stat ) // each 0~100
 	{
 		occupancy.moveNeedleTo( occupancy_stat );
 		uncoalesced.moveNeedleTo( uncoalesced_stat );
 		conflicts.moveNeedleTo( conflicts_stat );
 	}
 	
 	public static void MarkChart( double exec_time ) {
 		MainWindow.execution_times.add( exec_time );
 		double plot[] = new double [ MainWindow.execution_times.size() ];
 		Iterator<Double> iter = MainWindow.execution_times.iterator();
 		for ( int i = 0; i < MainWindow.execution_times.size(); i++ )
 			plot[i] = iter.next();
 		if ( MainWindow.history.getSeriesSet().getSeries("history") != null )
 			MainWindow.history.getSeriesSet().deleteSeries( "history" );
 		ILineSeries series = (ILineSeries)MainWindow.history.getSeriesSet().createSeries( SeriesType.LINE, "history" );
 		series.setLineWidth( 3 );
 		series.setSymbolSize( 5 );
 		series.setAntialias( SWT.ON );
 		series.setYSeries( plot );
 		
 		MainWindow.history.getAxisSet().adjustRange();
 		MainWindow.history.redraw();
 		
 	}
 }
 	
