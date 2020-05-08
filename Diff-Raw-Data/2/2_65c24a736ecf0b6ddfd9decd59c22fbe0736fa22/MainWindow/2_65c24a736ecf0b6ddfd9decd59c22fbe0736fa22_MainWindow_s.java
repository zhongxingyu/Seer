 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Display;
 import java.util.Map;
 import org.eclipse.swt.custom.CaretEvent;
 import org.eclipse.swt.custom.CaretListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowData;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.wb.swt.SWTResourceManager;
 import org.swtchart.Chart;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 
 public class MainWindow {
 	private static ProcessBuilder cur_pb;
 	public static Table table;
 	private static final FormToolkit formToolkit = new FormToolkit(
 			Display.getDefault());
 	private static Chart history;
	private static Button btnRecompile;
 	public static CUDACode ppCUDACode;
 	public static PTXScanner ppPTXScanner;
 	public static int currentLine;
 	/**
 	 * Launch the application.
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		/**********************************************************************************
 		 * BEGIN WINDOW CONTROL DECLARATIONS
 		 */
 		Display display = Display.getDefault();
 		Shell shlSwtApplication = new Shell(SWT.DIALOG_TRIM);
 		shlSwtApplication.setDragDetect(false);
 		shlSwtApplication.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_WIDGET_BACKGROUND));
 		shlSwtApplication.setSize(1094, 504);
 		shlSwtApplication.setText("CUDA IDE");
 		shlSwtApplication.setLayout(new GridLayout(3, false));
 
 		Label lblHistory = formToolkit.createLabel(shlSwtApplication,
 				"PERFORMANCE HISTORY", SWT.NONE);
 		lblHistory.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_WIDGET_BACKGROUND));
 		GridData gd_lblHistory = new GridData(SWT.LEFT, SWT.BOTTOM, false,
 				false, 1, 1);
 		gd_lblHistory.heightHint = 12;
 		lblHistory.setLayoutData(gd_lblHistory);
 		lblHistory.setFont(SWTResourceManager
 				.getFont("Segoe UI", 7, SWT.NORMAL));
 		new Label(shlSwtApplication, SWT.NONE);
 
 		Label lblEfficiencyMetrics = new Label(shlSwtApplication, SWT.NONE);
 		lblEfficiencyMetrics.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM,
 				false, false, 1, 1));
 		formToolkit.adapt(lblEfficiencyMetrics, true, true);
 		lblEfficiencyMetrics.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_WIDGET_BACKGROUND));
 		lblEfficiencyMetrics.setFont(SWTResourceManager.getFont("Segoe UI", 7,
 				SWT.NORMAL));
 		lblEfficiencyMetrics.setText("EFFICIENCY METRICS");
 
 		history = new Chart(shlSwtApplication, SWT.BORDER);
 		history.getTitle().setText("");
 		history.getAxisSet().getXAxis(0).getTick().setVisible(false);
 		history.getAxisSet().getYAxis(0).getTick().setVisible(false);
 		history.getAxisSet().getXAxis(0).getTitle().setText("");
 		history.getAxisSet().getYAxis(0).getTitle().setText("");
 		GridData gd_history = new GridData(SWT.FILL, SWT.CENTER, false, false,
 				1, 1);
 		gd_history.heightHint = 160;
 		gd_history.widthHint = 518;
 		history.setLayoutData(gd_history);
 		formToolkit.adapt(history);
 		formToolkit.paintBordersFor(history);
 		new Label(shlSwtApplication, SWT.NONE);
 
 		Composite composite = new Composite(shlSwtApplication, SWT.NONE);
 		composite.setEnabled(false);
 		composite.setLayout(new RowLayout(SWT.HORIZONTAL));
 		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, false, false,
 				1, 1);
 		gd_composite.widthHint = 489;
 		composite.setLayoutData(gd_composite);
 		formToolkit.adapt(composite);
 		composite.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_WIDGET_BACKGROUND));
 		formToolkit.paintBordersFor(composite);
 
 		CUDAGauge occupancyGauge = new CUDAGauge(composite, SWT.NONE);
 		occupancyGauge.setGaugeType("occupancy");
 		occupancyGauge.setLayoutData(new RowData(160, 160));
 		formToolkit.adapt(occupancyGauge);
 		occupancyGauge.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_WIDGET_BACKGROUND));
 		formToolkit.paintBordersFor(occupancyGauge);
 		CUDAGauge uncoalescedGauge = new CUDAGauge(composite, SWT.NONE );
 		uncoalescedGauge.setGaugeType( "uncoalesced" );
 		uncoalescedGauge.setLayoutData(new RowData(160, 160));
 		formToolkit.adapt(uncoalescedGauge);
 		uncoalescedGauge.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
 		formToolkit.paintBordersFor(uncoalescedGauge);
 		
 		CUDAGauge bankconflictGauge = new CUDAGauge(composite, SWT.NONE );
 		bankconflictGauge.setGaugeType( "bankconflicts" );
 		bankconflictGauge.setLayoutData(new RowData(160, 160));
 		formToolkit.adapt(bankconflictGauge);
 		bankconflictGauge.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_WIDGET_BACKGROUND));
 		formToolkit.paintBordersFor(bankconflictGauge);
 
 		Label lblCudaCode = formToolkit.createLabel(shlSwtApplication,
 				"CUDA CODE", SWT.NONE);
 		lblCudaCode.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_WIDGET_BACKGROUND));
 		GridData gd_lblCudaCode = new GridData(SWT.LEFT, SWT.BOTTOM, false,
 				false, 1, 1);
 		gd_lblCudaCode.heightHint = 13;
 		lblCudaCode.setLayoutData(gd_lblCudaCode);
 		lblCudaCode.setFont(SWTResourceManager.getFont("Segoe UI", 7,
 				SWT.NORMAL));
 		new Label(shlSwtApplication, SWT.NONE);
 
 		Label lblPtxAssemblerBreakdown = formToolkit.createLabel(
 				shlSwtApplication, "PTX ASSEMBLER BREAKDOWN", SWT.NONE);
 		lblPtxAssemblerBreakdown.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_WIDGET_BACKGROUND));
 		GridData gd_lblPtxAssemblerBreakdown = new GridData(SWT.LEFT,
 				SWT.BOTTOM, false, false, 1, 1);
 		gd_lblPtxAssemblerBreakdown.heightHint = 12;
 		lblPtxAssemblerBreakdown.setLayoutData(gd_lblPtxAssemblerBreakdown);
 		lblPtxAssemblerBreakdown.setFont(SWTResourceManager.getFont("Segoe UI",
 				7, SWT.NORMAL));
 
 		ppCUDACode = new CUDACode(shlSwtApplication, SWT.BORDER | SWT.MULTI
 				| SWT.V_SCROLL | SWT.H_SCROLL);
 		ppCUDACode.setTabs(3);
 		ppCUDACode.setDoubleClickEnabled(false);
 		ppCUDACode.setDragDetect(false);
 		ppCUDACode.setWordWrap(false);
 		GridData gd_ppCUDACode = new GridData(SWT.FILL, SWT.FILL, false, false,
 				1, 1);
 		gd_ppCUDACode.heightHint = 197;
 		gd_ppCUDACode.widthHint = 514;
 		ppCUDACode.setLayoutData(gd_ppCUDACode);
 
 		// add custom event listeners
 		
 		ArrowCanvas canvas_1 = new ArrowCanvas(shlSwtApplication, SWT.NONE);
 		GridData gd_canvas_1 = new GridData(SWT.FILL, SWT.FILL, true, false, 1,
 				1);
 		gd_canvas_1.widthHint = 46;
 		gd_canvas_1.heightHint = 208;
 		canvas_1.setLayoutData(gd_canvas_1);
 		canvas_1.setVisible(false);
 		formToolkit.adapt(canvas_1);
 		formToolkit.paintBordersFor(canvas_1);
 		ppCUDACode.setCanvas(canvas_1);
 		
 		table = new Table(shlSwtApplication, SWT.BORDER | SWT.FULL_SELECTION);
 		GridData gd_table = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
 		gd_table.heightHint = 166;
 		gd_table.widthHint = 375;
 		table.setLayoutData(gd_table);
 		table.setFont(SWTResourceManager.getFont("Segoe UI", 7, SWT.NORMAL));
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 
 		TableColumn tblclmnInstruction = new TableColumn(table, SWT.NONE);
 		tblclmnInstruction.setResizable(false);
 		tblclmnInstruction.setWidth(157);
 		tblclmnInstruction.setText("Instruction");
 		
 		TableColumn tblclmnArgument = new TableColumn(table, SWT.NONE);
 		tblclmnArgument.setResizable(false);
 		tblclmnArgument.setWidth(110);
 		tblclmnArgument.setText("Argument 1");
 
 		TableColumn tblclmnArgument_1 = new TableColumn(table, SWT.NONE);
 		tblclmnArgument_1.setResizable(false);
 		tblclmnArgument_1.setWidth(110);
 		tblclmnArgument_1.setText("Argument 2");
 
 		TableColumn tblclmnArgument_2 = new TableColumn(table, SWT.LEFT);
 		tblclmnArgument_2.setResizable(false);
 		tblclmnArgument_2.setWidth(108);
 		tblclmnArgument_2.setText("Argument 3");
 		new Label(shlSwtApplication, SWT.NONE);
 		new Label(shlSwtApplication, SWT.NONE);
 		btnRecompile = new CUDARecompileButton(shlSwtApplication, SWT.NONE);
 		btnRecompile.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
 		btnRecompile.setFont(SWTResourceManager
 				.getFont("Segoe UI", 9, SWT.BOLD));
 		GridData gd_btnRecompile = new GridData(SWT.RIGHT, SWT.CENTER, false,
 				false, 1, 1);
 		gd_btnRecompile.heightHint = 27;
 		btnRecompile.setLayoutData(gd_btnRecompile);
 		formToolkit.adapt(btnRecompile, true, true);
 		btnRecompile.setText("RECOMPILE");
 		btnRecompile.addGauge( occupancyGauge );
 		btnRecompile.addGauge( uncoalescedGauge );
 		btnRecompile.addGauge( bankconflictGauge );
 		
 
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
 			File CUpath = new File(args[0]);
 			if (!CUpath.exists())
 				throw new IOException(CUpath.getPath() + " does not exist!");
 			String CUfilename = CUpath.getName();
 
 			/*************** compile CU into commented PTX *****************/
 			// build command line and execute
 			ArrayList<String> command = new ArrayList<String>(Arrays.asList(
 					"nvcc", "-ptx", "-Xopencc=\"-LIST:source=on\" -O0",
 					"-Xptxas=-O0"));
 			command.addAll(Arrays.asList(args));
 			Runtime rt = Runtime.getRuntime();
 
 			cur_pb = new ProcessBuilder(command);
 
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
 			File PTXpath = new File(CUfilename.substring(0,
 					CUfilename.length() - 3).concat(".ptx"));
 			if (!PTXpath.exists())
 				throw new IOException(PTXpath.getPath() + " does not exist!");
 			ppPTXScanner = new PTXScanner(); 
 			ppPTXScanner.readIn(CUpath.getPath(), PTXpath.getPath());
 			PTXpath.deleteOnExit();
 			
 			/**************** compile CU again into executable and run it for profiling ********************/
 			// set up command line
 			command.clear();
 			command.addAll(Arrays.asList("nvcc", "-run"));
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
 			BufferedReader stderr = new BufferedReader(new InputStreamReader(
 					cur_p.getErrorStream()));
 			String in = "";
 			String in_in;
 			while ((in_in = stdin.readLine()) != null)
 				in += in_in;
 			System.out.println(in);
 
 			shlSwtApplication.open();
 			shlSwtApplication.layout();
 
 			canvas_1.setDimensions(ppCUDACode.getBounds(), table.getBounds());
 
 			BufferedReader CUcode = new BufferedReader(new FileReader(CUpath));
 			String cudacode = "";
 			String cudacode_in;
 			while ((cudacode_in = CUcode.readLine()) != null)
 				cudacode += cudacode_in + "\n";
 			ppCUDACode.setText(cudacode);
 			ppCUDACode.resetCaret();
 
 			CUcode.close();
 
 		} catch (Throwable e) {
 			/*
 			 * MessageBox mb = new MessageBox( shell, SWT.OK ); mb.setText(
 			 * "Fatal Error" ); mb.setMessage( e.getMessage() ); mb.open();
 			 */
 			
 			System.out.println(cur_pb.directory());			
 			System.out.println(cur_pb.environment());			
 			System.out.println(cur_pb.command());
 
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
 
 }
