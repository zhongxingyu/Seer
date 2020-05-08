 package fNIRs;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.StandardCopyOption;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.InputMismatchException;
 
 import org.eclipse.swt.widgets.*;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.wb.swt.SWTResourceManager;
 
 import com.mathworks.toolbox.javabuilder.*;
 
 import zombie.DataMining;
 import zombie.Preprocess;
 
 import com.rapidminer.operator.OperatorException;
 import com.thehowtotutorial.splashscreen.JSplash;
 
 import java.awt.Color;
 
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.graphics.Point;
 
 public class Hello {
 	protected Shell shlFnirsDataProcessing;
 	private static ArrayList<Integer> indexList;
 	private static Workspace workspace;
 	private Text text;
 	private Text text_1;
 	private Text text_2;
 	private Text text_3;
 	private Text text_4;
 	private Text text_5;
 	private Text text_subName;
 	private static Preprocess pre;
 	private static DataMining dm;
 	private Text text_subName2;
 	private Text text_6;
 	private FileDialog fileDialog;
 	private Text text_7;
 	private Text text_dm_sub;
 
 	private String subjectName = "";
 	private String subjectNameH = "";
 	private String subjectNameOther = "";
 	private int sessionNum;
 	private int sessionNumH;
 	private int sessionNumOther;
 
 	// stats stuff:
 	private FNIRsStats.GroupedChannels StatsHb, StatsHbO;
 	boolean doHb, doHbO;
 
 	private static RapidDriver rapidDriver;
 	private Text groupFileBox;
 	private Text numChunksBox;
 	private Text decimalPlacesBox;
 	private Text outputDirectoryBox;
 	private Text text_dmoutput;
 	private Text text_hbother;
 	private Text text_hboother;
 	private Text text_subnameOther;
 	private Text text_conditionsother;
 
 	/*
 	 * Launch the application.
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			JSplash splash = new JSplash(Hello.class.getClassLoader()
 					.getResource("splash.png"), true, true, false, "", null,
 					Color.GREEN, Color.BLACK);
 			splash.setForeground(Color.GREEN);
 			splash.setProgress(0, "Animating zombies...");
 			splash.splashOn();
 			Thread.sleep(200);
 			Hello window = new Hello();
 			splash.setProgress(8, "Fetching brains...");
 			Thread.sleep(200);
 			indexList = new ArrayList<Integer>();
 			splash.setProgress(16, "Chasing down subjects...");
 			Thread.sleep(200);
 			pre = new Preprocess();
 			splash.setProgress(30, "Practicing moans...");
 			Thread.sleep(200);
 			dm = new DataMining();
 			splash.setProgress(49, "Digging graves...");
 			Thread.sleep(200);
 			rapidDriver = new RapidDriver();
 			splash.setProgress(100, "Zombies loaded!");
 			Thread.sleep(500);
 			splash.splashOff();
 			window.open();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Open the window.
 	 */
 	public void open() {
 		Display display = Display.getDefault();
 		int result = createContents();
 		if (result == 1)
 			return;
 		shlFnirsDataProcessing.open();
 		shlFnirsDataProcessing.layout();
 		while (!shlFnirsDataProcessing.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 	}
 
 	boolean setExists(File file) {
 		if (!file.exists()) {
 			infoBox("Warning!", "File does not exist");
 			return false;
 		}
 		return true;
 	}
 
 	void infoBox(String title, String message) {
 		MessageBox messageDialog = new MessageBox(shlFnirsDataProcessing,
 				SWT.ICON_WARNING);
 		messageDialog.setText(title);
 		messageDialog.setMessage(message);
 		messageDialog.open();
 		return;
 	}
 
 	void browse(Text text) {
 		String fileName = fileDialog.open();
 		if (fileName != null)
 			text.setText(fileName);
 	}
 
 	void enableList(ArrayList<Control> lst) {
 		for (Control item : lst) {
 			item.setEnabled(true);
 		}
 	}
 
 	void disableList(ArrayList<Control> lst) {
 		for (Control item : lst) {
 			item.setEnabled(false);
 		}
 	}
 
 	/**
 	 * Create contents of the window.
 	 */
 	protected int createContents() {
 		shlFnirsDataProcessing = new Shell(SWT.CLOSE | SWT.MIN | SWT.TITLE);
 		shlFnirsDataProcessing.setFont(SWTResourceManager.getFont("Segoe UI",
 				12, SWT.NORMAL));
 		shlFnirsDataProcessing.setImage(SWTResourceManager.getImage(
 				Hello.class, "/fNIRs/logo.png"));
 		shlFnirsDataProcessing.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		shlFnirsDataProcessing.setSize(1000, 568);
 		shlFnirsDataProcessing
 				.setText("Zombie MiNIR - fNIRs Data Processing and Analysis");
 
 		fileDialog = new FileDialog(shlFnirsDataProcessing, SWT.OPEN
 				| SWT.CANCEL);
 		DirectoryDialog dlg = new DirectoryDialog(shlFnirsDataProcessing);
 		dlg.setText("Select a workspace");
 		String selected = dlg.open();
 		if (selected == null)
 			return 1;
 		workspace = new Workspace(selected, pre);
 
 		final List list = new List(shlFnirsDataProcessing, SWT.BORDER
 				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.CENTER);
 		list.setForeground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
 		list.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		list.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		list.setBounds(10, 38, 226, 392);
 		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
 		list.setLayoutData(data);
 
 		workspace.loadSubjects(list);
 
 		CTabFolder tabFolder = new CTabFolder(shlFnirsDataProcessing,
 				SWT.BORDER | SWT.FLAT);
 		tabFolder.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		tabFolder.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		tabFolder.setSelectionBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		tabFolder.setSelectionForeground(SWTResourceManager
 				.getColor(SWT.COLOR_GREEN));
 		tabFolder.setSimple(false);
 		tabFolder.setFont(SWTResourceManager.getFont("Segoe UI", 14, SWT.BOLD));
 		tabFolder.setBounds(242, 10, 742, 522);
 
 		CTabItem tbtmLoadFiles = new CTabItem(tabFolder, SWT.BORDER | SWT.FLAT);
 		tbtmLoadFiles.setFont(SWTResourceManager.getFont("Segoe UI", 14,
 				SWT.BOLD));
 		tbtmLoadFiles.setText("  Load Subjects");
 
 		Composite composite = new Composite(tabFolder, SWT.NONE);
 		composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		composite.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD
 				| SWT.ITALIC));
 		tbtmLoadFiles.setControl(composite);
 
 		CTabFolder tabFolder_1 = new CTabFolder(composite, SWT.BORDER
 				| SWT.FLAT);
 		tabFolder_1.setBackgroundMode(SWT.INHERIT_DEFAULT);
 		tabFolder_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		tabFolder_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		tabFolder_1.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		tabFolder_1.setSelectionBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		tabFolder_1.setSelectionForeground(SWTResourceManager
 				.getColor(SWT.COLOR_GREEN));
 		tabFolder_1.setSimple(false);
 		tabFolder_1.setFont(SWTResourceManager.getFont("Segoe UI", 9,
 				SWT.NORMAL));
 		tabFolder_1.setBounds(10, 10, 718, 474);
 
 		CTabItem tbtmNewItem = new CTabItem(tabFolder_1, SWT.BORDER | SWT.FLAT);
 		tbtmNewItem.setFont(SWTResourceManager
 				.getFont("Segoe UI", 13, SWT.BOLD));
 		tbtmNewItem.setText("  ISS Oxyplex  ");
 
 		Composite composite_3 = new Composite(tabFolder_1, SWT.NONE);
 		composite_3.setSize(new Point(30, 30));
 		composite_3.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		composite_3.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		tbtmNewItem.setControl(composite_3);
 
 		final ArrayList<Control> loadItems = new ArrayList<Control>();
 
 		final Spinner spinner = new Spinner(composite_3, SWT.BORDER);
 		spinner.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		spinner.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		spinner.setSelection(20);
 		spinner.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		spinner.setBounds(579, 242, 47, 25);
 		loadItems.add(spinner);
 
 		final Label lblOf = new Label(composite_3, SWT.NONE);
 		lblOf.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		lblOf.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		lblOf.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.ITALIC));
 		lblOf.setBounds(30, 380, 213, 25);
 		lblOf.setText("Session: 1 of 1");
 		loadItems.add(lblOf);
 
 		final Button btnCheckButton = new Button(composite_3, SWT.CHECK);
 		btnCheckButton.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		btnCheckButton.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		btnCheckButton.setSelection(true);
 		btnCheckButton.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		btnCheckButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (btnCheckButton.getSelection())
 					spinner.setEnabled(true);
 				else
 					spinner.setEnabled(false);
 			}
 		});
 		btnCheckButton.setBounds(425, 241, 21, 28);
 		loadItems.add(btnCheckButton);
 
 		final Spinner num_sessions = new Spinner(composite_3, SWT.BORDER);
 		num_sessions
 				.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		num_sessions
 				.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		num_sessions.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		num_sessions.setMinimum(1);
 		num_sessions.setBounds(524, 32, 47, 25);
 
 		final Button btnEnter_1 = new Button(composite_3, SWT.NONE);
 		btnEnter_1.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
 		btnEnter_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		btnEnter_1.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		btnEnter_1.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		btnEnter_1.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				subjectName = text_subName.getText();
 
 				if (subjectName == "" || subjectName.matches(subjectNameH)
 						|| Arrays.asList(list.getItems()).contains(subjectName)) {
 					infoBox("Warning!", "Please enter a new name.");
 					return;
 				}
 
 				for (Control item : loadItems) {
 					item.setVisible(true);
 				}
 
 				sessionNum = 1;
 
 				lblOf.setText("Session: 1 of " + num_sessions.getText());
 
 				text_subName.setEnabled(false);
 				num_sessions.setEnabled(false);
 				btnEnter_1.setEnabled(false);
 
 			}
 		});
 		btnEnter_1.setBounds(580, 30, 106, 28);
 		btnEnter_1.setText("Enter");
 
 		Button btnEnter = new Button(composite_3, SWT.NONE);
 		btnEnter.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
 		btnEnter.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		btnEnter.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		btnEnter.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnEnter.setBounds(480, 381, 100, 28);
 		btnEnter.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				double freq = 0;
 				double hpf = 0;
 				double lpf = 0;
 
 				File newFile = new File(text.getText());
 				if (!setExists(newFile))
 					return;
 
 				File condFile = new File(text_6.getText());
 				if (!setExists(condFile))
 					return;
 
 				try {
 					freq = (Double.valueOf(text_1.getText())).doubleValue();
 					hpf = (Double.valueOf(text_2.getText())).doubleValue();
 					lpf = (Double.valueOf(text_3.getText())).doubleValue();
 				} catch (NumberFormatException e1) {
 					infoBox("Warning!", "Please fill in all frequencies.");
 					return;
 				}
 
 				JSplash splash = new JSplash(Hello.class.getClassLoader()
 						.getResource("splash.png"), true, true, false, "",
 						null, Color.BLACK, Color.BLACK);
 				splash.setAlwaysOnTop(true);
 				splash.splashOn();
 				splash.setAlwaysOnTop(false);
 				splash.setProgress(0, "Zombies have arrived...");
 				try {
 					Thread.sleep(500);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 					return;
 				}
 
 				char slideavg = 'n';
 				int interval = 0;
 
 				if (btnCheckButton.getSelection()) {
 					slideavg = 'y';
 					interval = (Integer.valueOf(spinner.getText())).intValue();
 				}
 
 				splash.setProgress(23, "Searching for brains...");
 				try {
 					Thread.sleep(500);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 					return;
 				}
 
 				try {
 					if (sessionNum == 1) {
 						workspace.addSubject(subjectName, newFile, condFile,
 								freq, hpf, lpf, slideavg, interval);
 					} else {
 						workspace.concatSession(subjectName, newFile, condFile,
 								freq, hpf, lpf, slideavg, interval);
 					}
 				} catch (InputMismatchException ime) {
 					splash.splashOff();
 					ime.printStackTrace();
 					workspace.removeSubject(subjectName);
 					infoBox("Error", "Conditions file must be a text file.");
 					return;
 				}
 				sessionNum++;
 
 				splash.setProgress(45, "Deep frying grey matter...");
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 					return;
 				}
 
 				if (sessionNum > Integer.valueOf(num_sessions.getText())
 						.intValue()) {
 					list.add(subjectName);
 					for (Control item : loadItems) {
 						item.setVisible(false);
 					}
 					text_subName.setEnabled(true);
 					num_sessions.setEnabled(true);
 					btnEnter_1.setEnabled(true);
 					text_subName.setText("");
 					num_sessions.setSelection(1);
 				} else {
 					lblOf.setText("Session: " + sessionNum + " of "
 							+ num_sessions.getText());
 				}
 
 				text.setText("");
 				text_6.setText("");
 				splash.setProgress(100, "Brains have been preprocessed!");
 				try {
 					Thread.sleep(500);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 					return;
 				}
 				splash.splashOff();
 			}
 		});
 		btnEnter.setText("Add");
 		loadItems.add(btnEnter);
 
 		text_subName = new Text(composite_3, SWT.BORDER);
 		text_subName
 				.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		text_subName
 				.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		text_subName.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		text_subName.setBounds(147, 32, 213, 25);
 
 		Button btnBrowse = new Button(composite_3, SWT.NONE);
 		btnBrowse.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
 		btnBrowse.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		btnBrowse.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		btnBrowse.setFont(SWTResourceManager
 				.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnBrowse.setBounds(580, 128, 108, 28);
 		btnBrowse.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				browse(text);
 			}
 		});
 		btnBrowse.setText("Browse...");
 		loadItems.add(btnBrowse);
 
 		Label lblSubjectName = new Label(composite_3, SWT.NONE);
 		lblSubjectName.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		lblSubjectName.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		lblSubjectName.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		lblSubjectName.setBounds(30, 32, 104, 25);
 		lblSubjectName.setText("Subject Name:");
 
 		text = new Text(composite_3, SWT.BORDER);
 		text.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		text.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		text.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		text.setBounds(150, 130, 424, 25);
 		loadItems.add(text);
 
 		Label lblDataFile = new Label(composite_3, SWT.NONE);
 		lblDataFile.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		lblDataFile.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		lblDataFile.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		lblDataFile.setBounds(30, 130, 67, 25);
 		lblDataFile.setText("Data File:");
 		loadItems.add(lblDataFile);
 
 		text_1 = new Text(composite_3, SWT.BORDER);
 		text_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		text_1.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		text_1.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		text_1.setText("2");
 		text_1.setBounds(285, 242, 50, 25);
 		loadItems.add(text_1);
 
 		Label lblNewLabel = new Label(composite_3, SWT.NONE);
 		lblNewLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		lblNewLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		lblNewLabel.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		lblNewLabel.setBounds(85, 242, 164, 25);
 		lblNewLabel.setText("Sampling Frequency:");
 		loadItems.add(lblNewLabel);
 
 		Label lblHz = new Label(composite_3, SWT.NONE);
 		lblHz.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		lblHz.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		lblHz.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		lblHz.setBounds(340, 244, 29, 25);
 		lblHz.setText("Hz");
 		loadItems.add(lblHz);
 
 		Label lblHighPassFilter = new Label(composite_3, SWT.NONE);
 		lblHighPassFilter.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		lblHighPassFilter.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		lblHighPassFilter.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		lblHighPassFilter.setBounds(85, 282, 196, 25);
 		lblHighPassFilter.setText("High Pass Filter Frequency:");
 		loadItems.add(lblHighPassFilter);
 
 		text_2 = new Text(composite_3, SWT.BORDER);
 		text_2.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		text_2.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		text_2.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		text_2.setText(".1");
 		text_2.setBounds(285, 282, 50, 25);
 		loadItems.add(text_2);
 
 		Label label = new Label(composite_3, SWT.NONE);
 		label.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		label.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		label.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		label.setText("Hz");
 		label.setBounds(340, 284, 29, 25);
 		loadItems.add(label);
 
 		Label lblLowPassFilter = new Label(composite_3, SWT.NONE);
 		lblLowPassFilter.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		lblLowPassFilter.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		lblLowPassFilter.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		lblLowPassFilter.setBounds(85, 322, 196, 25);
 		lblLowPassFilter.setText("Low Pass Filter Frequency:");
 		loadItems.add(lblLowPassFilter);
 
 		text_3 = new Text(composite_3, SWT.BORDER);
 		text_3.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		text_3.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		text_3.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		text_3.setText(".01");
 		text_3.setBounds(285, 322, 50, 25);
 		loadItems.add(text_3);
 
 		Label label_1 = new Label(composite_3, SWT.NONE);
 		label_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		label_1.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		label_1.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		label_1.setText("Hz");
 		label_1.setBounds(340, 324, 29, 25);
 		loadItems.add(label_1);
 
 		Label lblPreprocessingOptions = new Label(composite_3, SWT.NONE);
 		lblPreprocessingOptions.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		lblPreprocessingOptions.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		lblPreprocessingOptions.setFont(SWTResourceManager.getFont("Segoe UI",
 				12, SWT.BOLD));
 		lblPreprocessingOptions.setBounds(30, 90, 189, 25);
 		lblPreprocessingOptions.setText("Preprocessing Options:");
 		loadItems.add(lblPreprocessingOptions);
 
 		Label lblConditionsFile = new Label(composite_3, SWT.NONE);
 		lblConditionsFile.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		lblConditionsFile.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		lblConditionsFile.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		lblConditionsFile.setBounds(30, 180, 116, 25);
 		lblConditionsFile.setText("Conditions File:");
 		loadItems.add(lblConditionsFile);
 
 		text_6 = new Text(composite_3, SWT.BORDER);
 		text_6.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		text_6.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		text_6.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		text_6.setBounds(150, 180, 424, 25);
 		loadItems.add(text_6);
 
 		Button btnBrowse_3 = new Button(composite_3, SWT.NONE);
 		btnBrowse_3.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
 		btnBrowse_3.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		btnBrowse_3.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		btnBrowse_3.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		btnBrowse_3.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				browse(text_6);
 			}
 		});
 		btnBrowse_3.setText("Browse...");
 		btnBrowse_3.setBounds(580, 178, 108, 29);
 		loadItems.add(btnBrowse_3);
 
 		Label lblNumberOfSessions = new Label(composite_3, SWT.NONE);
 		lblNumberOfSessions.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		lblNumberOfSessions.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		lblNumberOfSessions.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		lblNumberOfSessions.setBounds(451, 32, 67, 25);
 		lblNumberOfSessions.setText("Sessions:");
 
 		final Button button_3 = new Button(composite_3, SWT.NONE);
 		button_3.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
 		button_3.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		button_3.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		button_3.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		button_3.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				workspace.removeSubject(subjectName);
 				for (Control item : loadItems) {
 					item.setVisible(false);
 				}
 				text_subName.setEnabled(true);
 				num_sessions.setEnabled(true);
 				btnEnter_1.setEnabled(true);
 				text_subName.setText("");
 				num_sessions.setSelection(1);
 				subjectName = "";
 			}
 		});
 		button_3.setText("Cancel");
 		button_3.setBounds(586, 381, 100, 28);
 		loadItems.add(button_3);
 
 		Label lblSlidingAverage = new Label(composite_3, SWT.NONE);
 		lblSlidingAverage.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		lblSlidingAverage.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		lblSlidingAverage.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		lblSlidingAverage.setBounds(450, 242, 121, 25);
 		lblSlidingAverage.setText("Sliding Average:");
 		loadItems.add(lblSlidingAverage);
 
 		for (Control item : loadItems) {
 			item.setVisible(false);
 		}
 
 		final Composite composite_4 = new Composite(tabFolder_1, SWT.NONE);
 		composite_4.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		composite_4.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		composite_4.setBounds(10, 96, 694, 254);
 		composite_4.setVisible(false);
 
 		final ArrayList<Control> loadHatachi = new ArrayList<Control>();
 
 		text_4 = new Text(composite_4, SWT.BORDER);
 		text_4.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		text_4.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		text_4.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		text_4.setBounds(146, 200, 396, 25);
 		loadHatachi.add(text_4);
 
 		text_5 = new Text(composite_4, SWT.BORDER);
 		text_5.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		text_5.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		text_5.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		text_5.setBounds(146, 250, 396, 25);
 		loadHatachi.add(text_5);
 
 		final Button btnNewButton = new Button(composite_4, SWT.NONE);
 		btnNewButton.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
 		btnNewButton
 				.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		btnNewButton
 				.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		btnNewButton.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		btnNewButton.setBounds(548, 198, 143, 28);
 		btnNewButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				browse(text_4);
 			}
 		});
 		btnNewButton.setText("Browse...");
 		loadHatachi.add(btnNewButton);
 
 		final Button btnBrowse_1 = new Button(composite_4, SWT.NONE);
 		btnBrowse_1.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
 		btnBrowse_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		btnBrowse_1.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		btnBrowse_1.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		btnBrowse_1.setBounds(548, 248, 143, 28);
 		btnBrowse_1.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				browse(text_5);
 			}
 		});
 		btnBrowse_1.setText("Browse...");
 		loadHatachi.add(btnBrowse_1);
 
 		final Label lblHboFile = new Label(composite_4, SWT.NONE);
 		lblHboFile.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		lblHboFile.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		lblHboFile.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		lblHboFile.setBounds(30, 250, 84, 25);
 		lblHboFile.setText("HbO File:");
 		loadHatachi.add(lblHboFile);
 
 		final Label lblHbFile = new Label(composite_4, SWT.NONE);
 		lblHbFile.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		lblHbFile.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		lblHbFile.setFont(SWTResourceManager
 				.getFont("Segoe UI", 12, SWT.NORMAL));
 		lblHbFile.setBounds(30, 200, 84, 25);
 		lblHbFile.setText("Hb File:");
 		loadHatachi.add(lblHbFile);
 
 		Button btnAdd = new Button(composite_4, SWT.NONE);
 		btnAdd.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
 		btnAdd.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		btnAdd.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		btnAdd.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnAdd.setBounds(485, 380, 100, 25);
 		btnAdd.setText("Add");
 		loadHatachi.add(btnAdd);
 
 		CTabItem tbtmStats = new CTabItem(tabFolder, SWT.BORDER | SWT.FLAT);
 		tbtmStats.setFont(SWTResourceManager.getFont("Segoe UI", 13, SWT.BOLD));
 		tbtmStats.setText("  Statistical Analysis ");
 
 		Composite composite_1 = new Composite(tabFolder, SWT.NONE);
 		composite_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		tbtmStats.setControl(composite_1);
 
 		Label lblChannelGrouping = new Label(composite_1, SWT.NONE);
 		lblChannelGrouping.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		lblChannelGrouping.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		lblChannelGrouping.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		lblChannelGrouping.setBounds(30, 69, 137, 25);
 		lblChannelGrouping.setText("Channel Grouping:");
 
 		groupFileBox = new Text(composite_1, SWT.BORDER);
 		groupFileBox
 				.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		groupFileBox
 				.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		groupFileBox.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		groupFileBox.setBounds(172, 69, 432, 25);
 
 		Button btnNewButton_2 = new Button(composite_1, SWT.NONE);
 		btnNewButton_2.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
 		btnNewButton_2.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		btnNewButton_2.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		btnNewButton_2.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				browse(groupFileBox);
 			}
 		});
 		btnNewButton_2.setBounds(610, 67, 100, 28);
 		btnNewButton_2.setText("Browse...");
 
 		final Button HbCheck = new Button(composite_1, SWT.CHECK);
 		HbCheck.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		HbCheck.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		HbCheck.setBounds(173, 100, 18, 25);
 		HbCheck.setText("Hb");
 
 		final Button HbOCheck = new Button(composite_1, SWT.CHECK);
 		HbOCheck.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		HbOCheck.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		HbOCheck.setBounds(236, 100, 18, 25);
 		HbOCheck.setText("HbO");
 
 		final List groupsList = new List(composite_1, SWT.BORDER | SWT.MULTI
 				| SWT.V_SCROLL | SWT.H_SCROLL);
 		groupsList.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		groupsList.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		groupsList.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		groupsList.setBounds(30, 229, 180, 237);
 		groupsList.setVisible(false);
 
 		final List conditionsList = new List(composite_1, SWT.BORDER
 				| SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
 		conditionsList.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		conditionsList.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		conditionsList.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		conditionsList.setBounds(224, 229, 116, 237);
 		conditionsList.setVisible(false);
 
 		final Label grouplbl = new Label(composite_1, SWT.NONE);
 		grouplbl.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		grouplbl.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		grouplbl.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		grouplbl.setBounds(30, 198, 66, 25);
 		grouplbl.setText("Groups:");
 		grouplbl.setVisible(false);
 
 		final Label condlbl = new Label(composite_1, SWT.NONE);
 		condlbl.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		condlbl.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		condlbl.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		condlbl.setBounds(224, 198, 116, 25);
 		condlbl.setText("Conditions:");
 		condlbl.setVisible(false);
 
 		final Label chunking = new Label(composite_1, SWT.NONE);
 		chunking.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		chunking.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		chunking.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		chunking.setBounds(390, 198, 86, 25);
 		chunking.setText("Chunking:");
 		chunking.setVisible(false);
 
 		final Label lblOutputDir = new Label(composite_1, SWT.NONE);
 		lblOutputDir
 				.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		lblOutputDir
 				.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		lblOutputDir.setText("Output Directory Name:"); // "Folder" ???
 		lblOutputDir.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		lblOutputDir.setBounds(390, 357, 180, 25);
 		lblOutputDir.setVisible(false);
 
 		numChunksBox = new Text(composite_1, SWT.BORDER);
 		numChunksBox
 				.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		numChunksBox
 				.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		numChunksBox.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		numChunksBox.setBounds(390, 230, 55, 25);
 		numChunksBox.setVisible(false);
 
 		final Label chunks = new Label(composite_1, SWT.NONE);
 		chunks.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		chunks.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		chunks.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		chunks.setBounds(451, 229, 86, 25);
 		chunks.setText("chunks");
 		chunks.setVisible(false);
 
 		final Label aprecision = new Label(composite_1, SWT.NONE);
 		aprecision.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		aprecision.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		aprecision.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		aprecision.setBounds(390, 277, 148, 25);
 		aprecision.setText("ANOVA Precision:");
 		aprecision.setVisible(false);
 
 		decimalPlacesBox = new Text(composite_1, SWT.BORDER);
 		decimalPlacesBox.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		decimalPlacesBox.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		decimalPlacesBox.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		decimalPlacesBox.setBounds(390, 308, 55, 25);
 		decimalPlacesBox.setVisible(false);
 
 		final Label dplace = new Label(composite_1, SWT.NONE);
 		dplace.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		dplace.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		dplace.setText("decimal places");
 		dplace.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		dplace.setBounds(454, 308, 116, 25);
 		dplace.setVisible(false);
 
 		final Button anovabtn = new Button(composite_1, SWT.NONE);
 		anovabtn.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
 		anovabtn.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		anovabtn.setVisible(false);
 		anovabtn.addSelectionListener(new SelectionAdapter() {
 			@Override
 			/*
 			 * widgetSelected ANOVA button handler
 			 */
 			public void widgetSelected(SelectionEvent e) {
 				// get the list of selected groups as a string array:
 				String[] groupsAry = groupsList.getSelection();
 				// convert it to an ArrayList so we can pass it to writeANOVAs:
 				ArrayList<String> groupsAryLst = new ArrayList<String>(Arrays
 						.asList(groupsAry));
 				if (groupsAryLst.isEmpty()) { // if no groups were selected,
 					// diplay error message box:
 					infoBox("Error",
 							"Please select at least one channel grouping to analyze.");
 					return; // and stop executing the ANOVA stuff.
 				}
 
 				// get the list of selected conditions as a string array:
 				String[] conditionsAry = conditionsList.getSelection();
 				// convert from String array to Integer ArrayList:
 				ArrayList<Integer> conditionsAryLst = new ArrayList<Integer>();
 				for (String condition : conditionsAry) {
 					// convert from string to Integer, then append onto
 					// ArrayList:
 					conditionsAryLst.add(Integer.valueOf(condition));
 				}
 				if (conditionsAryLst.isEmpty()) { // if no conditions were
 													// selected,
 					// diplay error message box:
 					infoBox("Error",
 							"Please select at least one condition to analyze.");
 					return; // and stop executing the ANOVA stuff.
 				}
 
 				// get the number of "chunks" to average the selected data
 				// sequence(s) into
 				// from the text box, then convert it to an int and store:
 				String numChunksStr = numChunksBox.getText();
 				if (numChunksStr.equals("")) {
 					// diplay error message box:
 					infoBox("Error",
 							"Please enter a number of \"chunks\" to split the "
 									+ "data into and average before calculating ANOVA p-values.");
 					return; // and stop executing the ANOVA stuff.
 				}
 				int numChunks = Integer.parseInt(numChunksStr);
 
 				// get the number of ANOVA output decimal places from the text
 				// box, then
 				// convert it to an int and store:
 				String numPlacesStr = decimalPlacesBox.getText();
 				if (numPlacesStr.equals("")) {
 					// diplay error message box:
 					infoBox("Error",
 							"Please enter a number of decimal places to output "
 									+ "for the p-values.");
 					return; // and stop executing the ANOVA stuff.
 				}
 				int numPlaces = Integer.parseInt(numPlacesStr);
 
 				JSplash splash = new JSplash(Hello.class.getClassLoader()
 						.getResource("splash.png"), true, true, false, "",
 						null, Color.BLACK, Color.BLACK);
 				splash.setAlwaysOnTop(true);
 				splash.splashOn();
 				splash.setAlwaysOnTop(false);
 				splash.setProgress(0, "Searching for brains...");
 				try {
 					Thread.sleep(200);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 					return;
 				}
 
 				// create a directory for the output files:
 				String outputDirectoryName = outputDirectoryBox.getText();
 				String outputDirectoryPath = workspace.getStatsPath() + "\\"
 						+ outputDirectoryName;
 				File statsOutputDirectory = new File(outputDirectoryPath);
 				// CHECK THE DIRECTORY DOES NOT EXIST?? (AND ASK THEM TO MAKE
 				// SURE)
 				statsOutputDirectory.mkdir(); // create new directory with name
 												// given
 
 				// update progress bar:
 				splash.setProgress(10, "Running Hb ANOVA...");
 				try {
 					Thread.sleep(200);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 					return;
 				}
 
 				// execute the requested ANOVAs and write them to the output
 				// file(s)!
 
 				// (possibly) execute Hb ANOVAs:
 				if (doHb) {
 					File outputFileHb = new File(outputDirectoryPath + "\\"
 							+ "p-values_Hb.csv");
 					// calculate ANOVAs and write them to the output file!!!
 					try {
 						FNIRsStats.writeANOVAs(outputFileHb, StatsHb,
 								groupsAryLst, conditionsAryLst, numChunks,
 								numPlaces);
 					} catch (Exception ex) {
 						splash.splashOff();
 						ex.printStackTrace();
 						infoBox("Error", "Calculating p-values failed.");
 						return;
 					}
 				}
 
 				// update progress bar:
 				splash.setProgress(55, "Running HbO ANOVA...");
 				try {
 					Thread.sleep(200);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 					return;
 				}
 
 				// (possibly) execute HbO ANOVAs:
 				if (doHbO) {
 					File outputFileHbO = new File(outputDirectoryPath + "\\"
 							+ "p-values_HbO.csv");
 					try {
 						FNIRsStats.writeANOVAs(outputFileHbO, StatsHbO,
 								groupsAryLst, conditionsAryLst, numChunks,
 								numPlaces);
 					} catch (Exception ex) {
 						splash.splashOff();
 						ex.printStackTrace();
 						infoBox("Error", "Calculating p-values failed.");
 						return;
 					}
 				}
 
 				// update progress bar:
 				splash.setProgress(100, "Zombie ANOVA done!");
 				try {
 					Thread.sleep(500);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 					return;
 				}
 
 				splash.splashOff(); // done with progress bar
 				System.out.println("Done writing ANOVAs!");
 			}
 		});
 		anovabtn.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		anovabtn.setBounds(390, 436, 155, 28);
 		anovabtn.setText("Compute P-values");
 
 		final Button clearBtn = new Button(composite_1, SWT.NONE);
 		clearBtn.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
 		clearBtn.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		clearBtn.setVisible(false);
 		clearBtn.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				HbCheck.setSelection(false);
 				HbOCheck.setSelection(false);
 				groupFileBox.setText("");
 				grouplbl.setVisible(false);
 				condlbl.setVisible(false);
 				groupsList.setVisible(false);
 				groupsList.removeAll();
 				conditionsList.setVisible(false);
 				conditionsList.removeAll();
 				chunking.setVisible(false);
 				aprecision.setVisible(false);
 				dplace.setVisible(false);
 				chunks.setVisible(false);
 				numChunksBox.setVisible(false);
 				numChunksBox.setText("");
 				decimalPlacesBox.setVisible(false);
 				decimalPlacesBox.setText("");
 				outputDirectoryBox.setVisible(false);
 				outputDirectoryBox.setText("");
 				anovabtn.setVisible(false);
 				lblOutputDir.setVisible(false);
 				clearBtn.setVisible(false);
 			}
 		});
 		clearBtn.setBounds(551, 436, 155, 28);
 		clearBtn.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		clearBtn.setText("Cancel");
 
 		outputDirectoryBox = new Text(composite_1, SWT.BORDER);
 		outputDirectoryBox.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		outputDirectoryBox.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		outputDirectoryBox.setBounds(390, 388, 316, 25);
 		outputDirectoryBox.setVisible(false);
 
 		Button btnLoadGroupsAnd = new Button(composite_1, SWT.NONE);
 		btnLoadGroupsAnd.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
 		btnLoadGroupsAnd.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		btnLoadGroupsAnd.addSelectionListener(new SelectionAdapter() {
 			@Override
 			/*
 			 * widgetSelected Description: event handler for
 			 * "Load Groups and Conditions" button populates groups and
 			 * conditions boxes and creates GroupedChannels objects for the Hb
 			 * and/or HbO data if the corresponding box was checked NICK: FINISH
 			 * THIS COMMENTING. WE SHOULD MAKE IT SO EVERY TIME THE BUTTON IS
 			 * PRESSED, THE GROUPS AND CONDITIONS BOXES ARE REPOPULATED WITH THE
 			 * PROPER NEW GROUP NAMES AND CONDITION NUMBERS.
 			 */
 			public void widgetSelected(SelectionEvent e) {
 				// get list of subjects to analyze data for:
 				ArrayList<String> subjects = new ArrayList<String>();
 				for (String s : list.getSelection()) {
 					subjects.add(s);
 				}
 				// and make sure at least one subject has been selected:
 				if (subjects.isEmpty()) { // if not,
 					// display an error and return:
 					infoBox("Error",
 							"Please select at least one subject to analyze.");
 					return;
 				}
 
 				// get path to channel grouping file to apply to data:
 				String groupFilePath = groupFileBox.getText();
 				// create the group file object:
 				File groupingsFile = new File(groupFilePath);
 				if (!groupingsFile.exists()) { // if the file does not exist,
 					// display an error and return:
 					infoBox("Error",
 							"Please specify a valid channel groupings file.");
 					return;
 				}
 
 				// determine and remember whether Hb is checked:
 				doHb = HbCheck.getSelection();
 				doHbO = HbOCheck.getSelection(); // and the same for HbO
 				// make sure Hb, HbO, or both are checked:
 				if (!doHb && !doHbO) { // if neither box was checked,
 					// display error message and return:
 					infoBox("Error", "Please select Hb, HbO, or both.");
 					return;
 				}
 
 				// Otherwise, we can build lists of Hb and/or HbO files:
 				// and populate them if the corresponding box was checked:
 				if (doHb) { // if Hb box was checked,
 					// create an ArrayList for the selected subjects' Hb files:
 					ArrayList<File> hbFiles = new ArrayList<File>();
 					// and add each subject's Hb file to the ArrayList:
 					for (String subject : subjects) {
 						File dataFile = workspace.getHb(subject);
 						if (dataFile != null && dataFile.exists()) {
 							hbFiles.add(dataFile);
 						} else {
 							infoBox("Error", "Hb file for " + subject
 									+ " does not exist.");
 							return;
 						}
 					}
 					// then produce a GroupedChannels object from those files
 					// and the
 					// groupings file:
 					StatsHb = FNIRsStats.processAllSubjectData(hbFiles,
 							groupingsFile);
 				}
 				if (doHbO) { // if HbO box was checked,
 					// create an ArrayList for the selected subjects' HbO files:
 					ArrayList<File> hbOFiles = new ArrayList<File>();
 					// and add each subject's HbO file to the ArrayList:
 					for (String subject : subjects) {
 						File dataFile = workspace.getHbO(subject);
 						if (dataFile != null && dataFile.exists()) {
 							hbOFiles.add(dataFile);
 						} else {
 							infoBox("Error", "HbO file for " + subject
 									+ " does not exist.");
 							return;
 						}
 					}
 					// then produce a GroupedChannels object from those files
 					// and the
 					// groupings file:
 					StatsHbO = FNIRsStats.processAllSubjectData(hbOFiles,
 							groupingsFile);
 				}
 
 				// Inform the user of channels that were assigned to more
 				// than one group or not assigned to a group:
 				FNIRsStats.GroupedChannels temp = null;
 				// both StatsHb and StatsHbO will contain the groups from
 				// the group file:
 				if (doHb) {
 					temp = StatsHb;
 				} else {
 					temp = StatsHbO;
 				}
 
 				// Notify user if channels are in more than one group or in
 				// no group:
 				if (temp.channelsMissing()) {
 					infoBox("Warning", temp.getMissingChannelsMsg());
 				}
 				if (temp.channelsDuplicated()) {
 					infoBox("Warning!", temp.getDuplicatedChannelsMsg());
 				}
 
 				// clear lists to prepare for new data:
 				groupsList.removeAll();
 				conditionsList.removeAll();
 
 				// Now, we can populate the group and condition names lists:
 				// first, figure out from where the group names can be obtained:
 				FNIRsStats.GroupedChannels statsData = null;
 				if (doHb) { // If the Hb data is defined,
 					statsData = StatsHb; // then we can get the lists from it.
 				} else { // Otherwise, the HbO data must be defined,
 					statsData = StatsHbO; // so we can get the lists from it
 											// instead.
 				}
 				// now actually populate the GUI's groups list:
 				for (String groupName : statsData.getGroupNames()) {
 					groupsList.add(groupName);
 				}
 				// and the condition numbers list:
 				for (Integer condition : statsData.getConditions()) {
 					conditionsList.add(condition.toString());
 				}
 
 				// auto-generated:
 				grouplbl.setVisible(true);
 				condlbl.setVisible(true);
 				groupsList.setVisible(true);
 				conditionsList.setVisible(true);
 				chunking.setVisible(true);
 				aprecision.setVisible(true);
 				dplace.setVisible(true);
 				chunks.setVisible(true);
 				numChunksBox.setVisible(true);
 				decimalPlacesBox.setVisible(true);
 				outputDirectoryBox.setVisible(true);
 				anovabtn.setVisible(true);
 				lblOutputDir.setVisible(true);
 				clearBtn.setVisible(true);
 			}
 		});
 		btnLoadGroupsAnd.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		btnLoadGroupsAnd.setBounds(246, 139, 276, 28);
 		btnLoadGroupsAnd.setText("Load Groups and Conditions");
 
 		Label lblPleaseSelect = new Label(composite_1, SWT.NONE);
 		lblPleaseSelect.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		lblPleaseSelect.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		lblPleaseSelect.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.BOLD | SWT.ITALIC));
 		lblPleaseSelect.setBounds(30, 25, 394, 25);
 		lblPleaseSelect
 				.setText("Please select subject(s) from the Subject List to begin");
 
 		Label lblHb_1 = new Label(composite_1, SWT.NONE);
 		lblHb_1.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		lblHb_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		lblHb_1.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		lblHb_1.setBounds(193, 101, 40, 25);
 		lblHb_1.setText("Hb");
 
 		Label label_4 = new Label(composite_1, SWT.NONE);
 		label_4.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		label_4.setText("HbO");
 		label_4.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		label_4.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		label_4.setBounds(256, 101, 55, 28);
 
 		CTabItem tbtmMachineLearning = new CTabItem(tabFolder, SWT.BORDER
 				| SWT.FLAT);
 		tbtmMachineLearning.setFont(SWTResourceManager.getFont("Segoe UI", 14,
 				SWT.BOLD));
 		tbtmMachineLearning.setText("  Data Mining ");
 
 		Composite composite_2 = new Composite(tabFolder, SWT.NONE);
 		composite_2.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		composite_2.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		tbtmMachineLearning.setControl(composite_2);
 
 		final Spinner spinner_avgseg = new Spinner(composite_2, SWT.BORDER);
 		spinner_avgseg.setMinimum(1);
 		spinner_avgseg.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		spinner_avgseg.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		spinner_avgseg.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		spinner_avgseg.setBounds(656, 148, 55, 25);
 
 		final Spinner spinner_seqlen1 = new Spinner(composite_2, SWT.BORDER);
		spinner_seqlen1.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		spinner_seqlen1.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		spinner_seqlen1.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		spinner_seqlen1.setMinimum(1);
 		spinner_seqlen1.setEnabled(false);
 		spinner_seqlen1.setBounds(656, 213, 55, 25);
 
 		final Spinner spinner_fbs = new Spinner(composite_2, SWT.BORDER);
 		spinner_fbs.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		spinner_fbs.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		spinner_fbs.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		spinner_fbs.setMinimum(1);
 		spinner_fbs.setEnabled(false);
 		spinner_fbs.setBounds(656, 308, 55, 25);
 
 		final Spinner spinner_alphsize = new Spinner(composite_2, SWT.BORDER);
 		spinner_alphsize.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		spinner_alphsize.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		spinner_alphsize.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		spinner_alphsize.setMinimum(1);
 		spinner_alphsize.setEnabled(false);
 		spinner_alphsize.setBounds(656, 243, 55, 25);
 
 		final Button radioAS = new Button(composite_2, SWT.RADIO);
 		radioAS.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		radioAS.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		radioAS.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				spinner_avgseg.setEnabled(true);
 				spinner_seqlen1.setEnabled(false);
 				spinner_alphsize.setEnabled(false);
 				spinner_fbs.setEnabled(false);
 			}
 		});
 		radioAS.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		radioAS.setSelection(true);
 		radioAS.setBounds(435, 118, 18, 25);
 		radioAS.setText("Averaged Segments");
 
 		final Button radioSAX = new Button(composite_2, SWT.RADIO);
 		radioSAX.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		radioSAX.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		radioSAX.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				spinner_avgseg.setEnabled(false);
 				spinner_seqlen1.setEnabled(true);
 				spinner_alphsize.setEnabled(true);
 				spinner_fbs.setEnabled(false);
 			}
 		});
 		radioSAX.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		radioSAX.setBounds(435, 183, 18, 25);
 		radioSAX.setText("SAX Segments");
 
 		Button radioFBS = new Button(composite_2, SWT.RADIO);
 		radioFBS.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		radioFBS.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		radioFBS.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				spinner_avgseg.setEnabled(false);
 				spinner_seqlen1.setEnabled(false);
 				spinner_alphsize.setEnabled(false);
 				spinner_fbs.setEnabled(true);
 			}
 		});
 		radioFBS.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		radioFBS.setBounds(435, 278, 18, 25);
 		radioFBS.setText("Feature-based Segments");
 
 		final ArrayList<Control> step2 = new ArrayList<Control>();
 		// final ArrayList<Control> step3 = new ArrayList<Control>();
 
 		final List list_1 = new List(composite_2, SWT.BORDER | SWT.MULTI
 				| SWT.V_SCROLL | SWT.H_SCROLL);
 		list_1.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		list_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		list_1.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		list_1.setBounds(30, 118, 117, 339);
 		step2.add(list_1);
 
 		text_dmoutput = new Text(composite_2, SWT.BORDER);
 		text_dmoutput.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		text_dmoutput.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		text_dmoutput.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		text_dmoutput.setBounds(432, 378, 279, 25);
 
 		Label lbldmoutput = new Label(composite_2, SWT.NONE);
 		lbldmoutput.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		lbldmoutput.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		lbldmoutput.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		lbldmoutput.setBounds(432, 348, 161, 25);
 		lbldmoutput.setText("Output File Name:");
 
 		final ArrayList<Integer> cond_list = new ArrayList<Integer>();
 
 		final ArrayList<Control> step1 = new ArrayList<Control>();
 
 		final Button btnHb_1 = new Button(composite_2, SWT.CHECK);
 		btnHb_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		btnHb_1.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnHb_1.setBounds(431, 32, 18, 25);
 		btnHb_1.setText("Hb");
 		step1.add(btnHb_1);
 
 		final Button btnHbO_1 = new Button(composite_2, SWT.CHECK);
 		btnHbO_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		btnHbO_1.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnHbO_1.setText("HbO");
 		btnHbO_1.setBounds(494, 32, 18, 25);
 		step1.add(btnHbO_1);
 
 		text_dm_sub = new Text(composite_2, SWT.BORDER);
 		text_dm_sub.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		text_dm_sub.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		text_dm_sub.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		text_dm_sub.setBounds(143, 32, 255, 25);
 		step1.add(text_dm_sub);
 
 		list.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (text_dm_sub.getEnabled()) {
 					String[] dmSubjects = list.getSelection();
 					if (dmSubjects.length > 0) {
 						String dmSubject = dmSubjects[dmSubjects.length - 1];
 						text_dm_sub.setText(dmSubject);
 						text_dmoutput.setText(dmSubject);
 					}
 				}
 			}
 		});
 
 		final List list_2 = new List(composite_2, SWT.BORDER | SWT.V_SCROLL
 				| SWT.H_SCROLL);
 		list_2.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		list_2.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		list_2.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		list_2.setBounds(175, 118, 224, 339);
 
 		for (String template : workspace.getTemplates()) {
 			list_2.add(template);
 		}
 
 		list_2.setSelection(0);
 
 		Button btnRun = new Button(composite_2, SWT.NONE);
 		btnRun.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
 		btnRun.setEnabled(false);
 		btnRun.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		btnRun.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnRun.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 
 				if ((list_1.getSelectionIndices()).length < 2) {
 					infoBox("Warning!", "Select at least 2 conditions.");
 					return;
 				}
 
 				JSplash splash = new JSplash(Hello.class.getClassLoader()
 						.getResource("splash.png"), true, true, false, "",
 						null, Color.BLACK, Color.BLACK);
 				splash.setAlwaysOnTop(true);
 				splash.splashOn();
 				splash.setAlwaysOnTop(false);
 
 				if ((list_2.getSelectionIndices()).length < 1) {
 					infoBox("Warning!", "Select a process.");
 					return;
 				}
 
 				splash.setProgress(5, "Searching for brains...");
 				try {
 					Thread.sleep(200);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 					return;
 				}
 
 				cond_list.clear();
 				for (Integer item : list_1.getSelectionIndices())
 					cond_list.add(item);
 				disableList(step2);
 
 				splash.setProgress(10, "Locating brains...");
 				try {
 					Thread.sleep(200);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 					return;
 				}
 
 				String name = text_dm_sub.getText();
 				File hbOutput = workspace.getHbOutput(name);
 				File hboOutput = workspace.getHbOOutput(name);
 				if (hbOutput.exists())
 					hbOutput.delete();
 				if (hboOutput.exists())
 					hboOutput.delete();
 
 				if (btnHb_1.getSelection()) {
 					rapidDriver.filter(cond_list, workspace.getHb(name),
 							hbOutput);
 				} else {
 					rapidDriver.empty(hbOutput);
 				}
 
 				if (btnHbO_1.getSelection()) {
 					rapidDriver.filter(cond_list, workspace.getHbO(name),
 							hboOutput);
 				} else {
 					rapidDriver.empty(hboOutput);
 				}
 
 				splash.setProgress(25, "Mining brain data...");
 				try {
 					Thread.sleep(200);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 					return;
 				}
 
 				try {
 					File rminput = workspace.getRMInput(name);
 					if (rminput.exists())
 						rminput.delete();
 					if (radioAS.getSelection()) {
 						int num_segs;
 						try {
 							num_segs = (Integer.valueOf(spinner_avgseg
 									.getText())).intValue();
 						} catch (NumberFormatException nfe) {
 							nfe.printStackTrace();
 							infoBox("Warning!",
 									"Number of segments requires an integer.");
 							return;
 						}
 						dm.rapidFormatConversion(hbOutput.getAbsolutePath(),
 								hboOutput.getAbsolutePath(), workspace
 										.getRMInput(name).getAbsolutePath(),
 								num_segs);
 					} else if (radioSAX.getSelection()) {
 						int seq_len;
 						int alph_size;
 						try {
 							seq_len = (Integer.valueOf(spinner_seqlen1
 									.getText())).intValue();
 							alph_size = (Integer.valueOf(spinner_alphsize
 									.getText())).intValue();
 						} catch (NumberFormatException nfe) {
 							nfe.printStackTrace();
 							infoBox("Warning!",
 									"Sequence length and Alphabet Size require integers.");
 							return;
 						}
 						dm.SAX_RapidFormatConversion(
 								hbOutput.getAbsolutePath(), hboOutput
 										.getAbsolutePath(), workspace
 										.getRMInput(name).getAbsolutePath(),
 								seq_len, alph_size);
 					} else {
 						int num_segs;
 						try {
 							num_segs = (Integer.valueOf(spinner_fbs.getText()))
 									.intValue();
 						} catch (NumberFormatException nfe) {
 							nfe.printStackTrace();
 							infoBox("Warning!",
 									"Number of Segments requires an integer.");
 							return;
 						}
 						dm.features_rapidFormatConversion(hbOutput
 								.getAbsolutePath(),
 								hboOutput.getAbsolutePath(), workspace
 										.getRMInput(name).getAbsolutePath(),
 								num_segs);
 					}
 				} catch (MWException mwe) {
 					mwe.printStackTrace();
 					splash.splashOff();
 					enableList(step1);
 					list_1.removeAll();
 					infoBox("Error", "Data Mining Failed.");
 					return;
 				}
 
 				splash.setProgress(80, "Processing brain data...");
 				try {
 					Thread.sleep(200);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 					return;
 				}
 
 				try {
 					// input file, process file, output file
 					File inputFile = workspace.getRMInput(name);
 					File outputFile = new File(workspace.getDMPath() + "\\"
 							+ text_dmoutput.getText() + ".xls");
 
 					if (outputFile.exists()) {
 						outputFile.delete();
 					}
 
 					try {
 						rapidDriver.run(inputFile,
 								rapidDriver.generateProcess(inputFile,
 										workspace.getTemplate(list_2
 												.getSelection()[0])),
 								outputFile);
 					} catch (IOException e1) {
 						splash.splashOff();
 						e1.printStackTrace();
 						infoBox("Error", "Run failed.");
 						enableList(step1);
 						list_1.removeAll();
 					}
 				} catch (OperatorException e1) {
 					splash.splashOff();
 					e1.printStackTrace();
 					infoBox("Error", "Run failed.");
 					enableList(step1);
 					list_1.removeAll();
 				}
 
 				splash.setProgress(95, "Gathering brain bits...");
 				try {
 					Thread.sleep(200);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 					return;
 				}
 
 				enableList(step1);
 				list_1.removeAll();
 				splash.setProgress(100, "Brain data successfully mined...");
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 					return;
 				}
 				splash.splashOff();
 			}
 		});
 		// btnRun.setBounds(216, 120, 485, 308);
 		btnRun.setBounds(432, 429, 135, 28);
 		btnRun.setText("Run");
 		step2.add(btnRun);
 
 		Button btnNext = new Button(composite_2, SWT.NONE);
 		btnNext.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
 		btnNext.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		btnNext.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnNext.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 
 				String subjectName = text_dm_sub.getText();
 
 				// check that the users choices are valid
 				if (subjectName == ""
 						|| !Arrays.asList(list.getItems())
 								.contains(subjectName)) {
 					infoBox("Warning!", "Subject " + subjectName
 							+ " does not exist.");
 					return;
 				}
 
 				if (!btnHb_1.getSelection() && !btnHbO_1.getSelection()) {
 					infoBox("Warning!", "Select Hb or HbO or both.");
 					return;
 				}
 
 				if (btnHb_1.getSelection()
 						&& workspace.getHb(subjectName) == null) {
 					infoBox("Warning!", "Subject " + subjectName
 							+ " does not have an Hb file.");
 					return;
 				}
 
 				if (btnHbO_1.getSelection()
 						&& workspace.getHbO(subjectName) == null) {
 					infoBox("Warning!", "Subject " + subjectName
 							+ " does not have an HbO file.");
 					return;
 				}
 
 				// move on to next set of gui controls
 				disableList(step1);
 				enableList(step2);
 
 				// fill conditions list
 				int n = workspace.getMaxCond(text_dm_sub.getText());
 				for (int i = 0; i <= n; i++) {
 					String strI = "" + i;
 					list_1.add(strI);
 				}
 			}
 		});
 		btnNext.setBounds(575, 30, 136, 28);
 		btnNext.setText("Fill Conditions");
 		step1.add(btnNext);
 
 		Label lblSubjectName_1 = new Label(composite_2, SWT.NONE);
 		lblSubjectName_1.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		lblSubjectName_1.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		lblSubjectName_1.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		lblSubjectName_1.setBounds(30, 32, 106, 25);
 		lblSubjectName_1.setText("Subject Name:");
 
 		Label lblNewLabel_1 = new Label(composite_2, SWT.NONE);
 		lblNewLabel_1.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		lblNewLabel_1.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		lblNewLabel_1.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		lblNewLabel_1.setBounds(30, 88, 126, 25);
 		lblNewLabel_1.setText("Conditions:");
 
 		Label lblDataRepresentation = new Label(composite_2, SWT.NONE);
 		lblDataRepresentation.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		lblDataRepresentation.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		lblDataRepresentation.setFont(SWTResourceManager.getFont("Segoe UI",
 				12, SWT.NORMAL));
 		lblDataRepresentation.setBounds(431, 88, 196, 25);
 		lblDataRepresentation.setText("Data Representation:");
 
 		Button btnCancel_DM = new Button(composite_2, SWT.NONE);
 		btnCancel_DM.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
 		btnCancel_DM.setEnabled(false);
 		btnCancel_DM
 				.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		btnCancel_DM.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		btnCancel_DM.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				disableList(step2);
 				list_1.removeAll();
 				enableList(step1);
 			}
 		});
 		btnCancel_DM.setBounds(573, 429, 138, 28);
 		btnCancel_DM.setText("Cancel");
 		step2.add(btnCancel_DM);
 
 		Label lblSelectProcess = new Label(composite_2, SWT.NONE);
 		lblSelectProcess.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		lblSelectProcess.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		lblSelectProcess.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		lblSelectProcess.setBounds(175, 88, 117, 25);
 		lblSelectProcess.setText("Processes:");
 
 		Label lblSegments = new Label(composite_2, SWT.NONE);
 		lblSegments.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.ITALIC));
 		lblSegments.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		lblSegments.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		lblSegments.setBounds(490, 148, 103, 25);
 		lblSegments.setText("Segments:");
 
 		Label lblNewLabel_2 = new Label(composite_2, SWT.NONE);
 		lblNewLabel_2.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.ITALIC));
 		lblNewLabel_2.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		lblNewLabel_2.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		lblNewLabel_2.setText("Sequence Length:");
 		lblNewLabel_2.setBounds(491, 213, 143, 25);
 
 		Label lblNewLabel_3 = new Label(composite_2, SWT.NONE);
 		lblNewLabel_3.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.ITALIC));
 		lblNewLabel_3.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		lblNewLabel_3.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		lblNewLabel_3.setBounds(490, 308, 138, 25);
 		lblNewLabel_3.setText("Segments:");
 
 		Label lblNewLabel_5 = new Label(composite_2, SWT.NONE);
 		lblNewLabel_5.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.ITALIC));
 		lblNewLabel_5.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		lblNewLabel_5.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		lblNewLabel_5.setBounds(491, 243, 102, 25);
 		lblNewLabel_5.setText("Alphabet Size:");
 
 		Label label_6 = new Label(composite_2, SWT.NONE);
 		label_6.setText("Hb");
 		label_6.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		label_6.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		label_6.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		label_6.setBounds(451, 34, 40, 25);
 
 		Label label_7 = new Label(composite_2, SWT.NONE);
 		label_7.setText("HbO");
 		label_7.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		label_7.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		label_7.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		label_7.setBounds(514, 34, 55, 28);
 
 		Label lblNewLabel_4 = new Label(composite_2, SWT.NONE);
 		lblNewLabel_4.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		lblNewLabel_4.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		lblNewLabel_4.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		lblNewLabel_4.setBounds(455, 118, 168, 25);
 		lblNewLabel_4.setText("Averaged Segments");
 
 		Label lblSaxSegments = new Label(composite_2, SWT.NONE);
 		lblSaxSegments.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		lblSaxSegments.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		lblSaxSegments.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		lblSaxSegments.setBounds(455, 183, 135, 25);
 		lblSaxSegments.setText("SAX Segments:");
 
 		Label lblFeaturebasedSements = new Label(composite_2, SWT.NONE);
 		lblFeaturebasedSements.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		lblFeaturebasedSements.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		lblFeaturebasedSements.setFont(SWTResourceManager.getFont("Segoe UI",
 				12, SWT.NORMAL));
 		lblFeaturebasedSements.setBounds(455, 278, 169, 25);
 		lblFeaturebasedSements.setText("Feature-based Sements:");
 
 		for (Control item : step2) {
 			item.setVisible(false);
 		}
 
 		/*
 		 * Button for filtering Button btnNewButton_1 = new Button(composite_2,
 		 * SWT.NONE); btnNewButton_1.addSelectionListener(new SelectionAdapter()
 		 * {
 		 * 
 		 * @Override public void widgetSelected(SelectionEvent e) {
 		 * //rapidDriver.run(); rapidDriver.filter(new
 		 * ArrayList(Arrays.asList(2)) , new
 		 * File("C:\\Users\\jssmith\\Desktop\\Workspace\\subjects\\Work\\Hb"),
 		 * new File("output")); } }); btnNewButton_1.setBounds(46, 173, 75, 25);
 		 * btnNewButton_1.setText("Run Process");
 		 */
 
 		Button btnRemove = new Button(shlFnirsDataProcessing, SWT.FLAT);
 		btnRemove.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
 		btnRemove.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		btnRemove.setFont(SWTResourceManager
 				.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnRemove.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 
 				if (list.getSelection().length == 0)
 					return;
 
 				MessageBox warn = new MessageBox(shlFnirsDataProcessing,
 						SWT.ICON_QUESTION | SWT.YES | SWT.NO);
 				warn.setText("Warning!");
 				warn.setMessage("Are you sure you want to remove all data on the selected subjects?");
 
 				int result = warn.open();
 				if (result != SWT.YES)
 					return;
 
 				for (String subject : list.getSelection()) {
 					workspace.removeSubject(subject);
 				}
 
 				for (Integer item : list.getSelectionIndices())
 					indexList.add(item);
 				int[] indices = new int[indexList.size()];
 				for (int i = 0; i < indexList.size(); i++)
 					indices[i] = indexList.get(i);
 				list.remove(indices);
 				indexList.clear();
 			}
 		});
 
 		btnRemove.setBounds(10, 504, 226, 28);
 		btnRemove.setText("Remove Subject(s)");
 
 		Label lblSubjectName2 = new Label(composite_4, SWT.NONE);
 		lblSubjectName2.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		lblSubjectName2.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		lblSubjectName2.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		lblSubjectName2.setBounds(30, 40, 105, 25);
 		lblSubjectName2.setText("Subject Name:");
 
 		text_subName2 = new Text(composite_4, SWT.BORDER);
 		text_subName2.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		text_subName2.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		text_subName2.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		text_subName2.setBounds(146, 40, 396, 25);
 
 		final Spinner num_channels_H = new Spinner(composite_4, SWT.BORDER);
 		num_channels_H.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		num_channels_H.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		num_channels_H.setSelection(52);
 		num_channels_H.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		num_channels_H.setBounds(283, 85, 47, 25);
 
 		Label lblNumberOfChannels = new Label(composite_4, SWT.NONE);
 		lblNumberOfChannels.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		lblNumberOfChannels.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		lblNumberOfChannels.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		lblNumberOfChannels.setBounds(205, 85, 69, 25);
 		lblNumberOfChannels.setText("Channels:");
 
 		final Spinner num_sessions_h = new Spinner(composite_4, SWT.BORDER);
 		num_sessions_h.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		num_sessions_h.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		num_sessions_h.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		num_sessions_h.setMinimum(1);
 		num_sessions_h.setBounds(105, 85, 47, 25);
 
 		final Label lblOf_H = new Label(composite_4, SWT.NONE);
 		lblOf_H.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		lblOf_H.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		lblOf_H.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.ITALIC));
 		lblOf_H.setText("Session: 1 of 1");
 		lblOf_H.setBounds(30, 380, 274, 20);
 		loadHatachi.add(lblOf_H);
 
 		final Button btnHb = new Button(composite_4, SWT.CHECK);
 		btnHb.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		btnHb.setBounds(390, 85, 14, 25);
 		btnHb.setText("Hb");
 
 		final Button btnHbo = new Button(composite_4, SWT.CHECK);
 		btnHbo.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		btnHbo.setBounds(453, 85, 14, 25);
 		btnHbo.setText("HbO");
 
 		final Button button_2 = new Button(composite_4, SWT.NONE);
 		button_2.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
 		button_2.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		button_2.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		button_2.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		button_2.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				subjectNameH = text_subName2.getText();
 
 				if (subjectNameH == ""
 						|| subjectNameH.matches(subjectName)
 						|| Arrays.asList(list.getItems())
 								.contains(subjectNameH)) {
 					infoBox("Warning", "Please enter a new name.");
 					return;
 				}
 
 				if (!btnHb.getSelection() && !btnHbo.getSelection()) {
 					infoBox("Warning", "Select Hb or HbO or both.");
 					return;
 				}
 
 				for (Control item : loadHatachi) {
 					item.setVisible(true);
 				}
 
 				if (!btnHb.getSelection()) {
 					text_4.setVisible(false);
 					btnNewButton.setVisible(false);
 					lblHbFile.setVisible(false);
 				}
 
 				if (!btnHbo.getSelection()) {
 					text_5.setVisible(false);
 					btnBrowse_1.setVisible(false);
 					lblHboFile.setVisible(false);
 				}
 
 				sessionNumH = 1;
 
 				lblOf_H.setText("Session: 1 of " + num_sessions_h.getText());
 
 				text_subName2.setEnabled(false);
 				num_sessions_h.setEnabled(false);
 				button_2.setEnabled(false);
 				num_channels_H.setEnabled(false);
 				btnHb.setEnabled(false);
 				btnHbo.setEnabled(false);
 			}
 		});
 		button_2.setText("Enter");
 		button_2.setBounds(548, 38, 143, 28);
 
 		btnAdd.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				File condFile = new File(text_7.getText());
 				if (!setExists(condFile)) {
 					return;
 				}
 
 				int channels = (Integer.valueOf(num_channels_H.getText()))
 						.intValue();
 
 				File HbFile = new File(text_4.getText());
 				File HbOFile = new File(text_5.getText());
 
 				if (!HbFile.exists() && !HbOFile.exists()) {
 					return;
 				}
 
 				JSplash splash = new JSplash(Hello.class.getClassLoader()
 						.getResource("splash.png"), true, true, false, "",
 						null, Color.BLACK, Color.BLACK);
 				splash.setAlwaysOnTop(true);
 				splash.splashOn();
 				splash.setAlwaysOnTop(false);
 
 				splash.setProgress(0, "Finding deoxygenated brains...");
 
 				if (HbFile.exists()) {
 					try {
 						pre.xlsreadfile(HbFile.getAbsolutePath(), "Hb",
 								channels);
 						HbFile = new File("Hb");
 					} catch (MWException e1) {
 						splash.splashOff();
 						e1.printStackTrace();
 						infoBox("Error", "Hb File must be an excel csv file.");
 						return;
 					}
 				} else {
 					HbFile = null;
 				}
 
 				splash.setProgress(18, "Finding oxygenated brains...");
 				try {
 					Thread.sleep(200);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 					return;
 				}
 
 				if (HbOFile.exists()) {
 					try {
 						pre.xlsreadfile(HbOFile.getAbsolutePath(), "HbO",
 								channels);
 						HbOFile = new File("HbO");
 					} catch (MWException e1) {
 						splash.splashOff();
 						e1.printStackTrace();
 						infoBox("Error", "HbO File must be an excel csv file.");
 						return;
 					}
 				} else {
 					HbOFile = null;
 				}
 
 				splash.setProgress(36, "Prepping brains...");
 				try {
 					Thread.sleep(200);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 				}
 
 				try {
 					if (sessionNumH == 1) {
 						workspace.addSubject(subjectNameH, HbFile, HbOFile,
 								condFile);
 					} else {
 						workspace.concatSession(subjectNameH, HbFile, HbOFile,
 								condFile);
 					}
 				} catch (InputMismatchException ime) {
 					splash.splashOff();
 					ime.printStackTrace();
 					workspace.removeSubject(subjectNameH);
 					infoBox("Error", "Conditions file must be a text file.");
 					return;
 				}
 
 				sessionNumH++;
 
 				splash.setProgress(40, "Searching for brains...");
 				try {
 					Thread.sleep(500);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 					return;
 				}
 
 				if (sessionNumH > Integer.valueOf(num_sessions_h.getText())
 						.intValue()) {
 					list.add(subjectNameH);
 					for (Control item : loadHatachi) {
 						item.setVisible(false);
 					}
 					text_subName2.setEnabled(true);
 					num_sessions_h.setEnabled(true);
 					button_2.setEnabled(true);
 
 					num_channels_H.setEnabled(true);
 					btnHb.setEnabled(true);
 					btnHbo.setEnabled(true);
 
 					text_subName2.setText("");
 					num_sessions_h.setSelection(1);
 				} else {
 					lblOf_H.setText("Session: " + sessionNumH + " of "
 							+ num_sessions_h.getText());
 				}
 
 				splash.setProgress(68, "Offering Hitachi-san some brains...");
 				try {
 					Thread.sleep(500);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 					return;
 				}
 
 				text_4.setText("");
 				text_5.setText("");
 				text_7.setText("");
 				text_subName2.setText("");
 
 				splash.setProgress(100, "Hitachi likes brains!");
 				try {
 					Thread.sleep(500);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 					return;
 				}
 				splash.splashOff();
 			}
 		});
 		loadHatachi.add(btnAdd);
 
 		CTabItem tbtmNewItem_2 = new CTabItem(tabFolder_1, SWT.BORDER
 				| SWT.FLAT);
 		tbtmNewItem_2.setFont(SWTResourceManager.getFont("Segoe UI", 13,
 				SWT.BOLD));
 		tbtmNewItem_2.setText("  Hitachi  ");
 		tbtmNewItem_2.setControl(composite_4);
 
 		Label label_3 = new Label(composite_4, SWT.NONE);
 		label_3.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		label_3.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		label_3.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		label_3.setText("Conditions File:");
 		label_3.setBounds(30, 300, 111, 25);
 		loadHatachi.add(label_3);
 
 		text_7 = new Text(composite_4, SWT.BORDER);
 		text_7.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		text_7.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		text_7.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		text_7.setBounds(146, 300, 396, 25);
 		loadHatachi.add(text_7);
 
 		CTabItem tabOther = new CTabItem(tabFolder_1, SWT.BORDER | SWT.FLAT);
 		tabOther.setFont(SWTResourceManager.getFont("Segoe UI", 13, SWT.BOLD));
 		tabOther.setText("  Other  ");
 
 		Composite composite_other = new Composite(tabFolder_1, SWT.NONE);
 		composite_other.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		composite_other.setVisible(false);
 		composite_other.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		tabOther.setControl(composite_other);
 
 		final ArrayList<Control> loadOther = new ArrayList<Control>();
 
 		text_hbother = new Text(composite_other, SWT.BORDER);
 		text_hbother
 				.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		text_hbother
 				.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		text_hbother.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		text_hbother.setBounds(146, 200, 396, 25);
 		loadOther.add(text_hbother);
 
 		text_hboother = new Text(composite_other, SWT.BORDER);
 		text_hboother.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		text_hboother.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		text_hboother.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		text_hboother.setBounds(146, 250, 396, 25);
 		loadOther.add(text_hboother);
 
 		final Button browse_hbother = new Button(composite_other, SWT.NONE);
 		browse_hbother.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
 		browse_hbother.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		browse_hbother.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				browse(text_hbother);
 			}
 		});
 		browse_hbother.setText("Browse...");
 		browse_hbother.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		browse_hbother.setBounds(548, 198, 133, 28);
 		loadOther.add(browse_hbother);
 
 		final Button browse_hboother = new Button(composite_other, SWT.NONE);
 		browse_hboother.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
 		browse_hboother.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		browse_hboother.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				browse(text_hboother);
 			}
 		});
 		browse_hboother.setText("Browse...");
 		browse_hboother.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		browse_hboother.setBounds(548, 248, 133, 28);
 		loadOther.add(browse_hboother);
 
 		final Label label_hboother = new Label(composite_other, SWT.NONE);
 		label_hboother.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		label_hboother.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		label_hboother.setText("HbO File:");
 		label_hboother.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		label_hboother.setBounds(30, 250, 84, 28);
 		loadOther.add(label_hboother);
 
 		final Label lbl_hbother = new Label(composite_other, SWT.NONE);
 		lbl_hbother.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		lbl_hbother.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		lbl_hbother.setText("Hb File:");
 		lbl_hbother.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		lbl_hbother.setBounds(30, 200, 84, 28);
 		loadOther.add(lbl_hbother);
 
 		final Spinner spinner_nsother = new Spinner(composite_other, SWT.BORDER);
 		spinner_nsother.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		spinner_nsother.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		spinner_nsother.setMinimum(1);
 		spinner_nsother.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		spinner_nsother.setBounds(105, 85, 47, 25);
 
 		final Label lbl_seshnum = new Label(composite_other, SWT.NONE);
 		lbl_seshnum.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		lbl_seshnum.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		lbl_seshnum.setText("Session: 1 of 1");
 		lbl_seshnum.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.ITALIC));
 		lbl_seshnum.setBounds(30, 380, 212, 20);
 		loadOther.add(lbl_seshnum);
 
 		final Button button_hbother = new Button(composite_other, SWT.CHECK);
 		button_hbother.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		button_hbother.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		button_hbother.setText("Hb");
 		button_hbother.setBounds(404, 85, 14, 25);
 
 		final Button button_hboother = new Button(composite_other, SWT.CHECK);
 		button_hboother.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		button_hboother.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		button_hboother.setText("HbO");
 		button_hboother.setBounds(467, 85, 14, 28);
 
 		final Button enter_other = new Button(composite_other, SWT.NONE);
 
 		Button add_other = new Button(composite_other, SWT.NONE);
 		add_other.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
 		add_other.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		add_other.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 
 				File condFile = new File(text_conditionsother.getText());
 				if (!setExists(condFile))
 					return;
 
 				int channels = (Integer.valueOf(spinner_nsother.getText()))
 						.intValue();
 
 				File Hb = new File(text_hbother.getText());
 				File HbO = new File(text_hboother.getText());
 
 				if (!Hb.exists() && !HbO.exists()) {
 					return;
 				}
 
 				File HbFile = new File("HbFile");
 				File HbOFile = new File("HbOFile");
 
 				if (text_hbother.getText() != "") {
 					try {
 						Files.copy(Hb.toPath(), HbFile.toPath()
 								.toAbsolutePath(),
 								StandardCopyOption.REPLACE_EXISTING);
 					} catch (IOException e2) {
 						e2.printStackTrace();
 						return;
 					}
 				} else {
 					HbFile = null;
 				}
 
 				if (text_hboother.getText() != "") {
 					try {
 						Files.copy(HbO.toPath(), HbOFile.toPath()
 								.toAbsolutePath(),
 								StandardCopyOption.REPLACE_EXISTING);
 					} catch (IOException e2) {
 						e2.printStackTrace();
 						return;
 					}
 				} else {
 					HbOFile = null;
 				}
 
 				// prog bar begin here
 				JSplash splash = new JSplash(Hello.class.getClassLoader()
 						.getResource("splash.png"), true, true, false, "",
 						null, Color.BLACK, Color.BLACK);
 				splash.setAlwaysOnTop(true);
 				splash.splashOn();
 				splash.setAlwaysOnTop(false);
 
 				try {
 					if (sessionNumOther == 1) {
 						workspace.addSubject(subjectNameOther, HbFile, HbOFile,
 								condFile);
 					} else {
 						workspace.concatSession(subjectNameOther, HbFile,
 								HbOFile, condFile);
 					}
 				} catch (InputMismatchException ime) {
 					splash.splashOff();
 					ime.printStackTrace();
 					workspace.removeSubject(subjectNameOther);
 					infoBox("Error", "Conditions file must be a text file.");
 					return;
 				}
 				sessionNumOther++;
 
 				splash.setProgress(33, "Searching for brains...");
 				try {
 					Thread.sleep(500);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 				}
 
 				if (sessionNumOther > Integer
 						.valueOf(spinner_nsother.getText()).intValue()) {
 					list.add(subjectNameOther);
 					for (Control item : loadOther) {
 						item.setVisible(false);
 					}
 					text_subnameOther.setEnabled(true);
 					spinner_nsother.setEnabled(true);
 					enter_other.setEnabled(true);
 
 					button_hbother.setEnabled(true);
 					button_hboother.setEnabled(true);
 
 					text_subnameOther.setText("");
 					spinner_nsother.setSelection(1);
 				} else {
 					lbl_seshnum.setText("Session: " + sessionNumOther + " of "
 							+ spinner_nsother.getText());
 				}
 
 				splash.setProgress(68, "Almost there...");
 				try {
 					Thread.sleep(500);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 				}
 
 				text_hbother.setText("");
 				text_hboother.setText("");
 				text_conditionsother.setText("");
 				text_subnameOther.setText("");
 
 				splash.setProgress(100, "Done!");
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 				}
 				splash.splashOff();
 			}
 		});
 		add_other.setText("Add");
 		add_other.setFont(SWTResourceManager
 				.getFont("Segoe UI", 12, SWT.NORMAL));
 		add_other.setBounds(475, 380, 100, 28);
 		loadOther.add(add_other);
 
 		Label label_subNameOther = new Label(composite_other, SWT.NONE);
 		label_subNameOther.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		label_subNameOther.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		label_subNameOther.setText("Subject Name:");
 		label_subNameOther.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		label_subNameOther.setBounds(30, 42, 105, 28);
 
 		text_subnameOther = new Text(composite_other, SWT.BORDER);
 		text_subnameOther.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		text_subnameOther.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		text_subnameOther.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		text_subnameOther.setBounds(146, 42, 396, 25);
 
 		final Button enter_other1 = new Button(composite_other, SWT.NONE);
 		enter_other1.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
 		enter_other1.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		enter_other1.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				subjectNameOther = text_subnameOther.getText();
 
 				if (subjectNameOther == ""
 						|| subjectNameOther.matches(subjectName)
 						|| subjectNameOther.matches(subjectNameH)
 						|| Arrays.asList(list.getItems()).contains(
 								subjectNameOther)) {
 					infoBox("Warning", "Please enter a new name.");
 					return;
 				}
 
 				if (!button_hbother.getSelection()
 						&& !button_hboother.getSelection()) {
 					infoBox("Warning", "Select Hb or HbO or both.");
 					return;
 				}
 
 				for (Control item : loadOther) {
 					item.setVisible(true);
 				}
 
 				if (!button_hbother.getSelection()) {
 					text_hbother.setVisible(false);
 					browse_hbother.setVisible(false);
 					lbl_hbother.setVisible(false);
 				}
 
 				if (!button_hboother.getSelection()) {
 					text_hboother.setVisible(false);
 					browse_hboother.setVisible(false);
 					label_hboother.setVisible(false);
 				}
 
 				sessionNumOther = 1;
 
 				lbl_seshnum.setText("Session: 1 of " + spinner_nsother.getText());
 
 				text_subnameOther.setEnabled(false);
 				spinner_nsother.setEnabled(false);
 				enter_other1.setEnabled(false);
 				button_hbother.setEnabled(false);
 				button_hboother.setEnabled(false);
 			}
 		});
 		enter_other1.setText("Enter");
 		enter_other1.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		enter_other1.setBounds(548, 40, 133, 28);
 
 		Label lbl_conditionsother = new Label(composite_other, SWT.NONE);
 		lbl_conditionsother.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		lbl_conditionsother.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		lbl_conditionsother.setText("Conditions File:");
 		lbl_conditionsother.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		lbl_conditionsother.setBounds(30, 300, 111, 28);
 		loadOther.add(lbl_conditionsother);
 
 		text_conditionsother = new Text(composite_other, SWT.BORDER);
 		text_conditionsother.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		text_conditionsother.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		text_conditionsother.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		text_conditionsother.setBounds(146, 300, 396, 25);
 		loadOther.add(text_conditionsother);
 
 		Button browse_condother = new Button(composite_other, SWT.NONE);
 		browse_condother.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
 		browse_condother.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		browse_condother.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				browse(text_conditionsother);
 			}
 		});
 		browse_condother.setText("Browse...");
 		browse_condother.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		browse_condother.setBounds(548, 298, 133, 28);
 		loadOther.add(browse_condother);
 
 		Label label_ppoptions = new Label(composite_other, SWT.NONE);
 		label_ppoptions.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		label_ppoptions.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		label_ppoptions.setText("Preprocessing Options:");
 		label_ppoptions.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.BOLD));
 		label_ppoptions.setBounds(30, 150, 189, 25);
 		loadOther.add(label_ppoptions);
 
 		Label lbl_numSessionsOther = new Label(composite_other, SWT.NONE);
 		lbl_numSessionsOther.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		lbl_numSessionsOther.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		lbl_numSessionsOther.setText("Sessions:");
 		lbl_numSessionsOther.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		lbl_numSessionsOther.setBounds(30, 85, 63, 25);
 
 		Button cancel_other = new Button(composite_other, SWT.NONE);
 		cancel_other.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
 		cancel_other
 				.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		cancel_other.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				workspace.removeSubject(subjectNameOther);
 				for (Control item : loadOther) {
 					item.setVisible(false);
 				}
 				text_subnameOther.setEnabled(true);
 				spinner_nsother.setEnabled(true);
 				enter_other1.setEnabled(true);
 
 				button_hbother.setEnabled(true);
 				button_hboother.setEnabled(true);
 
 				text_subnameOther.setText("");
 				spinner_nsother.setSelection(1);
 				subjectNameOther = "";
 			}
 		});
 		cancel_other.setText("Cancel");
 		cancel_other.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		cancel_other.setBounds(581, 380, 100, 28);
 		loadOther.add(cancel_other);
 
 		Label lblHb = new Label(composite_other, SWT.NONE);
 		lblHb.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		lblHb.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		lblHb.setBounds(424, 87, 32, 25);
 		lblHb.setText("Hb");
 
 		Label lblHbo = new Label(composite_other, SWT.NONE);
 		lblHbo.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		lblHbo.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		lblHbo.setBounds(487, 87, 55, 25);
 		lblHbo.setText("HbO");
 
 		/*
 		 * <<<<<<< HEAD Button btnBrowse_2 = new Button(composite_4, SWT.NONE);
 		 * btnBrowse_2.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 		 * SWT.NORMAL)); btnBrowse_2.addSelectionListener(new SelectionAdapter()
 		 * { =======
 		 */
 		Button btnBrowse_2 = new Button(composite_4, SWT.NONE);
 		btnBrowse_2.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
 		btnBrowse_2.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		btnBrowse_2.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		btnBrowse_2.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		btnBrowse_2.setBounds(548, 298, 143, 28);
 		btnBrowse_2.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				browse(text_7);
 			}
 		});
 
 		btnBrowse_2.setText("Browse...");
 		loadHatachi.add(btnBrowse_2);
 
 		Label label_2 = new Label(composite_4, SWT.NONE);
 		label_2.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		label_2.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		label_2.setText("Preprocessing Options:");
 		label_2.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
 		label_2.setBounds(30, 150, 189, 25);
 		loadHatachi.add(label_2);
 
 		Label lblSessions = new Label(composite_4, SWT.NONE);
 		lblSessions.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		lblSessions.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		lblSessions.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		lblSessions.setText("Sessions:");
 		lblSessions.setBounds(30, 85, 69, 25);
 
 		final Button btnCancel = new Button(composite_4, SWT.NONE);
 		btnCancel.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
 		btnCancel.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		btnCancel.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		btnCancel.setFont(SWTResourceManager
 				.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnCancel.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				workspace.removeSubject(subjectNameH);
 				for (Control item : loadHatachi) {
 					item.setVisible(false);
 				}
 				text_subName2.setEnabled(true);
 				num_sessions_h.setEnabled(true);
 				button_2.setEnabled(true);
 
 				num_channels_H.setEnabled(true);
 				btnHb.setEnabled(true);
 				btnHbo.setEnabled(true);
 
 				text_subName2.setText("");
 				num_sessions_h.setSelection(1);
 				subjectNameH = "";
 			}
 		});
 		btnCancel.setBounds(591, 380, 100, 25);
 		btnCancel.setText("Cancel");
 		loadHatachi.add(btnCancel);
 
 		Label label_5 = new Label(composite_4, SWT.NONE);
 		label_5.setText("Hb");
 		label_5.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		label_5.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		label_5.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		label_5.setBounds(410, 85, 28, 25);
 
 		Label label_8 = new Label(composite_4, SWT.NONE);
 		label_8.setText("HbO");
 		label_8.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		label_8.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		label_8.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		label_8.setBounds(473, 85, 55, 28);
 
 		Button btnNewButton_1 = new Button(shlFnirsDataProcessing, SWT.FLAT);
 		btnNewButton_1.pack();
 		btnNewButton_1.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
 		btnNewButton_1.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 
 				if (button_3.getVisible()) {
 					infoBox("Warning!",
 							"Cancel file loading before changing workspace.");
 					return;
 				}
 
 				if (btnCancel.getVisible()) {
 					infoBox("Warning!",
 							"Cancel file loading before changing workspace.");
 					return;
 				}
 
 				// new workspace
 				fileDialog = new FileDialog(shlFnirsDataProcessing, SWT.OPEN
 						| SWT.CANCEL);
 				DirectoryDialog dlg = new DirectoryDialog(
 						shlFnirsDataProcessing);
 				dlg.setText("Select Workspace");
 				String selected = dlg.open(); // annoying new folder bug
 				if (selected == null)
 					return;
 
 				// cancel dm
 				disableList(step2);
 				list_1.removeAll();
 				enableList(step1);
 
 				list.removeAll();
 
 				// set up new workspace
 				workspace = new Workspace(selected, pre);
 				workspace.loadSubjects(list);
 
 				list_2.removeAll();
 
 				for (String template : workspace.getTemplates()) {
 					list_2.add(template);
 				}
 
 				list_2.setSelection(0);
 
 				text_dm_sub.setText("");
 				text_dmoutput.setText("");
 
 			}
 		});
 		btnNewButton_1.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.NORMAL));
 		btnNewButton_1.setBounds(10, 436, 226, 28);
 		btnNewButton_1.setText("Change Workspace");
 
 		Label lblSubjectList = new Label(shlFnirsDataProcessing, SWT.BORDER);
 		lblSubjectList.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_BLACK));
 		lblSubjectList.setAlignment(SWT.CENTER);
 		lblSubjectList.setFont(SWTResourceManager.getFont("Segoe UI", 12,
 				SWT.BOLD));
 		lblSubjectList.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		lblSubjectList.setBounds(10, 10, 226, 28);
 		lblSubjectList.setText("Subject List");
 
 		for (Control item : loadHatachi) {
 			item.setVisible(false);
 		}
 
 		for (Control item : loadOther) {
 			item.setVisible(false);
 		}
 
 		tabFolder.setSelection(0);
 		tabFolder_1.setSelection(0);
 
 		Button btnClear = new Button(shlFnirsDataProcessing, SWT.FLAT);
 		btnClear.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
 		btnClear.setBounds(10, 470, 226, 28);
 		btnClear.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
 		btnClear.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnClear.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				list.deselectAll();
 				indexList.clear();
 			}
 		});
 		btnClear.setText("Clear Selections");
 
 		list_1.setVisible(true);
 		btnRun.setVisible(true);
 		btnCancel_DM.setVisible(true);
 
 		return 0;
 	}
 }
