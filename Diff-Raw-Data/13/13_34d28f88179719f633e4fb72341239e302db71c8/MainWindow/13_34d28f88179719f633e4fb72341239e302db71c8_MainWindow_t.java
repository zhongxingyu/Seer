 package org.lttng.flightbox.ui;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import org.eclipse.linuxtools.lttng.jni.exception.JniException;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.lttng.flightbox.cpu.TraceEventHandlerProcess;
 import org.lttng.flightbox.cpu.TraceEventHandlerStats;
 import org.lttng.flightbox.dep.BlockingTaskListener;
 import org.lttng.flightbox.io.TraceEventHandlerModel;
 import org.lttng.flightbox.io.TraceEventHandlerModelMeta;
 import org.lttng.flightbox.io.TraceReader;
 import org.lttng.flightbox.model.SystemModel;
 import org.lttng.flightbox.model.Task;
 import org.lttng.flightbox.statistics.ResourceUsage;
 import org.lttng.flightbox.ui.ProcessUsageView.TableData;
 
 public class MainWindow {
 
 	class ExitListener extends SelectionAdapter {
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			Display display = e.display;
 			display.close();
 		}
 	}
 
 	private final CTabFolder folder;
 	private File traceDir;
 	ResourceUsage<Long> cpuStats;
 	CpuUsageView cpuView;
 	ProcessUsageView processView;
 	Shell shell;
 	Display display;
 	private final DependencyTreeView depTreeView;
 	private final ProcessUsageView depProcessView;
 
 	public MainWindow() {
 		this(new String[0]);
 	}
 
 	public MainWindow(String[] args) {
 
 		display = new Display();
 		shell = new Shell(display);
 		shell.setText("Flightbox");
 		shell.setSize(800, 800);
 		shell.setLayout(new FillLayout(SWT.VERTICAL));
 
 		folder = new CTabFolder(shell, SWT.NONE);
 
 		CTabItem cpuUsageTab = new CTabItem(folder, SWT.NONE);
 		cpuUsageTab.setText("CPU Usage");
 		SashForm sashForm = new SashForm(folder, SWT.VERTICAL);
 		cpuUsageTab.setControl(sashForm);
 
 		cpuView = new CpuUsageView(sashForm, SWT.BORDER);
 		processView = new ProcessUsageView(sashForm, SWT.BORDER);
 
 		cpuView.setProcessView(processView);
 		//processView.setCpuView(cpuView);
 
 		CTabItem depAnalysisTab = new CTabItem(folder, SWT.NONE);
 		depAnalysisTab.setText("Dependency analysis");
 		SashForm depSashForm = new SashForm(folder, SWT.VERTICAL);
 		depProcessView = new ProcessUsageView(depSashForm, SWT.BORDER);
 		depTreeView = new DependencyTreeView(depSashForm, SWT.BORDER);
 
 		depProcessView.getTable().addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				TableData data = (TableData) arg0.item.getData();
 				HashMap<Integer, TreeSet<Task>> tasks = depTreeView.getModel().getTasks();
 				Integer pid = new Long(data.pid).intValue();
 				if (tasks.containsKey(pid)) {
 					TreeSet<Task> taskSet = tasks.get(pid);
 					if (taskSet != null && !taskSet.isEmpty()) {
 						Task latest = taskSet.last();
 						depTreeView.setRootTask(latest);
 					}
 				}
 			}
 		});
 
 		depAnalysisTab.setControl(depSashForm);
 
 
 		class OpenListener extends SelectionAdapter {
 			private final MainWindow window;
 			public OpenListener(MainWindow window) {
 				this.window = window;
 			}
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
 				dialog.setText("Open trace");
 				String tracePath = dialog.open();
 				if (tracePath != null) {
 					File traceDir = new File(tracePath);
 					File traceMetadata = new File(tracePath, "metadata_0");
 					if (traceDir.isDirectory() && traceMetadata.isFile()) {
 						window.setTraceDir(traceDir);
 					}
 				}
 			}
 		}
 
 		//SettingsDialog settings = new SettingsDialog(shell);
 		//settings.open();
 
 		Menu menu = new Menu(shell, SWT.BAR);
 		Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
 
 		MenuItem fileMenuItem = new MenuItem(menu, SWT.CASCADE);
 		fileMenuItem.setText("&File");
 		fileMenuItem.setMenu(fileMenu);
 
 		MenuItem openMenuItem = new MenuItem(fileMenu, SWT.PUSH);
 		openMenuItem.setText("&Open");
 		openMenuItem.setAccelerator(SWT.CTRL + 'O');
 		openMenuItem.addSelectionListener(new OpenListener(this));
 		MenuItem separator = new MenuItem(fileMenu, SWT.SEPARATOR);
 		MenuItem exitItem = new MenuItem(fileMenu, SWT.PUSH);
 		exitItem.setText("E&xit");
 		exitItem.setAccelerator(SWT.CTRL + 'Q');
 		shell.setMenuBar(menu);
 
 		exitItem.addSelectionListener(new ExitListener());
 
 		if (args.length == 1) {
 			setTraceDir(new File(args[0]));
 		}
 
 		shell.open();
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 
 		shell.dispose();
 
 	}
 
 	public void setTraceDir(File traceDir) {
 		this.traceDir = traceDir;
 
 		SystemModel model = new SystemModel();
 		BlockingTaskListener listener = new BlockingTaskListener();
 		model.addTaskListener(listener);
		listener.setModel(model);
 
 		TraceEventHandlerModelMeta metaHandler = new TraceEventHandlerModelMeta();
 		metaHandler.setModel(model);
 		TraceReader reader = new TraceReader(this.traceDir.toString());
 		reader.register(metaHandler);
 
 		// FIXME: this should be a function
 		try {
 			reader.process();
 		} catch (JniException e) {
 			e.printStackTrace();
 			MessageBox msg = new MessageBox(shell);
 			msg.setMessage("Error while reading the trace");
 			msg.setText("Error");
 			msg.open();
 			return;
 		}
 
 		TraceEventHandlerStats cpuHandler = new TraceEventHandlerStats();
 		TraceEventHandlerProcess procHandler = new TraceEventHandlerProcess();
 		TraceEventHandlerModel modelHandler = new TraceEventHandlerModel();
 		modelHandler.setModel(model);
 		reader = new TraceReader(this.traceDir.toString());
 		reader.register(cpuHandler);
 		reader.register(procHandler);
 		reader.register(modelHandler);
 
 		try {
 			reader.process();
 		} catch (JniException e) {
 			e.printStackTrace();
 			MessageBox msg = new MessageBox(shell);
 			msg.setMessage("Error while reading the trace");
 			msg.setText("Error");
 			msg.open();
 			return;
 		}
 
 		cpuStats = cpuHandler.getUsageStats();
 		cpuView.setCpuStats(cpuStats);
 		cpuView.updateData();
 		cpuView.resetHighlight();
 
 		ResourceUsage<Long> procStats = procHandler.getUsageStats();
 		TreeMap<Long, Task> procInfo = procHandler.getProcInfo();
 		processView.setStats(procStats, procInfo);
 		processView.resetSumInterval();
 		depProcessView.setStats(procStats, procInfo);
 		depProcessView.resetSumInterval();
 		depTreeView.setModel(model);
 	}
 }
