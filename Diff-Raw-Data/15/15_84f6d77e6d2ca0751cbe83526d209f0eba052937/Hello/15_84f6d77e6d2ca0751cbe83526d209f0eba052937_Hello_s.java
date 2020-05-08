 package fNIRs;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.StandardCopyOption;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DragDetectEvent;
 import org.eclipse.swt.events.DragDetectListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.wb.swt.SWTResourceManager;
 import org.eclipse.swt.widgets.Composite;
 
 import com.mathworks.toolbox.javabuilder.*;
 
 import preprocess_2.Preprocess;
 
 import org.eclipse.swt.*;
 import org.eclipse.swt.events.*;
 import org.eclipse.swt.layout.*;
 import org.eclipse.swt.widgets.*;
 
 import com.rapidminer.operator.OperatorException;
 
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
 	private Text text_subName2;
 	private Text text_6;
 	private FileDialog fileDialog;
 	private Text text_7;
 	private Text text_dm_sub;
 	
 	private String subjectName = null;  // make sure these are different before adding new subject!
 	private String subjectNameH = null;
 	private int sessionNum;
 	private int sessionNumH;
 
 	
 	private static RapidDriver rapidDriver;
 	private Text text_8;
 	private Text text_9;
 	private Text text_10;
 
 
 	/*
 	 * Launch the application.
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			Hello window = new Hello();
 			indexList = new ArrayList<Integer>();
 			pre = new Preprocess();
 			//pre.rapidFormatConversion("Hb_output.txt", "HbO_output.txt", "rapid_output.xls");
 			//workspace = new Workspace("C:\\Users\\shammond\\Desktop\\CS_Seminar\\fNIRs\\workspace", pre);
 			rapidDriver = new RapidDriver();
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
 		createContents();
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
 			errorBox("Warning!","File does not exist");
 			return false;
 		}
 		return true;
 	}
 	
 	void errorBox(String title,String message) {
 		MessageBox messageDialog = new MessageBox(shlFnirsDataProcessing, SWT.ERROR);
 	    messageDialog.setText(title);
 	    messageDialog.setMessage(message);
 	    messageDialog.open();
 		return;
 	}
 	
 	void browse(Text text) {
 		String fileName = fileDialog.open();
 		if (fileName!=null)
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
 	protected void createContents() {
 		shlFnirsDataProcessing = new Shell();
 		shlFnirsDataProcessing.setImage(SWTResourceManager.getImage(Hello.class, "/fNIRs/logo.png"));
 		shlFnirsDataProcessing.setBackground(SWTResourceManager.getColor(255, 255, 255));
 		shlFnirsDataProcessing.setSize(1000, 600);
 		shlFnirsDataProcessing.setText("Zombie MiNIR - fNIRs Data Processing and Analysis");
 		
 		fileDialog = new FileDialog(shlFnirsDataProcessing, SWT.OPEN);
 		DirectoryDialog dlg = new DirectoryDialog(shlFnirsDataProcessing);
 		dlg.setText("Select Workspace");
 	    String selected = dlg.open(); // cannot seem to name New Folder for some reason
 	    //System.out.println(selected);
 	    workspace = new Workspace(selected,pre);
 		
 		final List list = new List(shlFnirsDataProcessing, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
 		list.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		list.setBounds(10, 10, 226, 460);
 
 		workspace.loadSubjects(list);
 		
 		Menu menu = new Menu(shlFnirsDataProcessing, SWT.BAR);
 		shlFnirsDataProcessing.setMenuBar(menu);
 		
 		MenuItem mntmHelp_1 = new MenuItem(menu, SWT.CASCADE);
 		mntmHelp_1.setText("Help");
 		
 		Menu menu_1 = new Menu(mntmHelp_1);
 		mntmHelp_1.setMenu(menu_1);
 		
 		MenuItem mntmHelp = new MenuItem(menu_1, SWT.NONE);
 		mntmHelp.setText("Tutorial");
 		
 		Button btnClear = new Button(shlFnirsDataProcessing, SWT.NONE);
 		btnClear.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnClear.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				list.deselectAll();
 				indexList.clear();
 			}
 		});
 		btnClear.setBounds(10, 476, 226, 25);
 		btnClear.setText("Clear Selections");
 		
 		TabFolder tabFolder = new TabFolder(shlFnirsDataProcessing, SWT.NONE);
 		tabFolder.setFont(SWTResourceManager.getFont("Segoe UI", 13, SWT.BOLD));
 		tabFolder.setBounds(242, 10, 742, 522);
 		
 		TabItem tbtmLoadFiles = new TabItem(tabFolder, SWT.NONE);
 		tbtmLoadFiles.setText("  Load File(s) / Preprocessing  ");
 		
 		Composite composite = new Composite(tabFolder, SWT.NONE);
 		composite.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD | SWT.ITALIC));
 		tbtmLoadFiles.setControl(composite);
 
 		TabFolder tabFolder_1 = new TabFolder(composite, SWT.NONE);
 		tabFolder_1.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD | SWT.ITALIC));
 		tabFolder_1.setBounds(10, 10, 718, 474);
 
 		TabItem tbtmNewItem = new TabItem(tabFolder_1, SWT.NONE);
 		tbtmNewItem.setText("ISS Oxyplex");
 
 		Composite composite_3 = new Composite(tabFolder_1, SWT.NONE);
 		composite_3.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		tbtmNewItem.setControl(composite_3);
 		
 		final ArrayList<Control> loadItems = new ArrayList<Control>();
 		
 		final Spinner spinner = new Spinner(composite_3, SWT.BORDER);
 		spinner.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		spinner.setEnabled(false);
 		spinner.setBounds(568, 228, 47, 25);
 		loadItems.add(spinner);
 		
 		final Label lblOf = new Label(composite_3, SWT.NONE);
 		lblOf.setAlignment(SWT.CENTER);
 		lblOf.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		lblOf.setBounds(330, 355, 75, 21);
 		lblOf.setText("1 of 1");
 		loadItems.add(lblOf);
 		
 		final Button btnCheckButton = new Button(composite_3, SWT.CHECK);
 		btnCheckButton.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnCheckButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (btnCheckButton.getSelection())
 					spinner.setEnabled(true);
 				else
 					spinner.setEnabled(false);
 			}
 		});
 		btnCheckButton.setBounds(425, 228, 134, 25);
 		btnCheckButton.setText("Sliding Average:");
 		loadItems.add(btnCheckButton);
 		
 		final Spinner num_sessions = new Spinner(composite_3, SWT.BORDER);
 		num_sessions.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		num_sessions.setMinimum(1);
 		num_sessions.setBounds(543, 29, 47, 25);
 		
 		final Button btnEnter_1 = new Button(composite_3, SWT.NONE);
 		btnEnter_1.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnEnter_1.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				
 				subjectName = text_subName.getText();
 				
 				if (subjectName == "" || subjectName == subjectNameH || Arrays.asList(list.getItems()).contains(subjectName)) {
 					MessageBox messageDialog = new MessageBox(shlFnirsDataProcessing, SWT.ERROR);
 				    messageDialog.setText("Warning!");
 				    messageDialog.setMessage("Please enter a new name");
 				    messageDialog.open();
 					return;
 				}
 				
 				for (Control item : loadItems) {
 					item.setVisible(true);
 				}
 				
 				sessionNum = 1;
 				
 				lblOf.setText("1 of " + num_sessions.getText());
 				
 				text_subName.setEnabled(false);
 				num_sessions.setEnabled(false);
 				btnEnter_1.setEnabled(false);
 				
 			}
 		});
 		btnEnter_1.setBounds(596, 30, 80, 25);
 		btnEnter_1.setText("Enter");
 		
 		Button btnEnter = new Button(composite_3, SWT.NONE);
 		btnEnter.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnEnter.setBounds(330, 390, 75, 25);
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
 				}
 				catch (NumberFormatException e1) {
 					MessageBox messageDialog = new MessageBox(shlFnirsDataProcessing, SWT.ERROR);
 				    messageDialog.setText("Warning!");
 				    messageDialog.setMessage("Please fill in all frequencies");
 				    messageDialog.open();
 				}
 				
 				char slideavg = 'n';
 				int interval = 0;
 					
 				if (btnCheckButton.getSelection()) {
 					slideavg = 'y';
 					interval = (Integer.valueOf(spinner.getText())).intValue();
 				}
 				
 				if (sessionNum==1) {
 					workspace.addSubject(subjectName, newFile, condFile, freq, hpf, lpf, slideavg, interval);
 				}
 				else {
 					workspace.concatSession(subjectName, newFile, condFile, freq, hpf, lpf, slideavg, interval);
 				}
 				sessionNum++;
 				
 				if (sessionNum>Integer.valueOf(num_sessions.getText()).intValue()) {
 					list.add(subjectName);
 					for (Control item : loadItems) {
 						item.setVisible(false);
 					}
 					text_subName.setEnabled(true);
 					num_sessions.setEnabled(true);
 					btnEnter_1.setEnabled(true);
 					text_subName.setText("");
 					num_sessions.setSelection(1);
 				}
 				else {
 					lblOf.setText(sessionNum + " of " + num_sessions.getText());
 				}
 				
 				text.setText("");
 				text_6.setText("");
 			}
 
 		});
 		btnEnter.setText("Add");
 		loadItems.add(btnEnter);
 		
 		text_subName = new Text(composite_3, SWT.BORDER);
 		text_subName.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		text_subName.setBounds(140, 30, 220, 25);
 		
 		Button btnBrowse = new Button(composite_3, SWT.NONE);
 		btnBrowse.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnBrowse.setBounds(568, 126, 108, 25);
 		btnBrowse.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				browse(text);
 			}
 		});
 		btnBrowse.setText("Browse...");
 		loadItems.add(btnBrowse);
 		
 		Label lblSubjectName = new Label(composite_3, SWT.NONE);
 		lblSubjectName.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		lblSubjectName.setBounds(30, 30, 104, 25);
 		lblSubjectName.setText("Subject Name:");
 		
 		text = new Text(composite_3, SWT.BORDER);
 		text.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		text.setBounds(150, 125, 411, 25);
 		loadItems.add(text);
 		
 		Label lblDataFile = new Label(composite_3, SWT.NONE);
 		lblDataFile.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		lblDataFile.setBounds(30, 125, 67, 25);
 		lblDataFile.setText("Data File:");
 		loadItems.add(lblDataFile);
 		
 		text_1 = new Text(composite_3, SWT.BORDER);
 		text_1.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		text_1.setText("2");
 		text_1.setBounds(285, 228, 50, 25);
 		loadItems.add(text_1);
 		
 		Label lblNewLabel = new Label(composite_3, SWT.NONE);
 		lblNewLabel.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		lblNewLabel.setBounds(85, 228, 164, 25);
 		lblNewLabel.setText("Sampling Frequency:");
 		loadItems.add(lblNewLabel);
 		
 		Label lblHz = new Label(composite_3, SWT.NONE);
 		lblHz.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		lblHz.setBounds(340, 228, 29, 25);
 		lblHz.setText("Hz");
 		loadItems.add(lblHz);
 		
 		Label lblHighPassFilter = new Label(composite_3, SWT.NONE);
 		lblHighPassFilter.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		lblHighPassFilter.setBounds(85, 268, 196, 25);
 		lblHighPassFilter.setText("High Pass Filter Frequency:");
 		loadItems.add(lblHighPassFilter);
 		
 		text_2 = new Text(composite_3, SWT.BORDER);
 		text_2.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		text_2.setText(".1");
 		text_2.setBounds(285, 268, 50, 25);
 		loadItems.add(text_2);
 		
 		Label label = new Label(composite_3, SWT.NONE);
 		label.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		label.setText("Hz");
 		label.setBounds(340, 268, 29, 25);
 		loadItems.add(label);
 		
 		Label lblLowPassFilter = new Label(composite_3, SWT.NONE);
 		lblLowPassFilter.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		lblLowPassFilter.setBounds(85, 308, 196, 25);
 		lblLowPassFilter.setText("Low Pass Filter Frequency:");
 		loadItems.add(lblLowPassFilter);
 		
 		text_3 = new Text(composite_3, SWT.BORDER);
 		text_3.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		text_3.setText(".01");
 		text_3.setBounds(285, 308, 50, 25);
 		loadItems.add(text_3);
 		
 		Label label_1 = new Label(composite_3, SWT.NONE);
 		label_1.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		label_1.setText("Hz");
 		label_1.setBounds(340, 308, 29, 25);
 		loadItems.add(label_1);
 		
 		Label lblPreprocessingOptions = new Label(composite_3, SWT.NONE);
 		lblPreprocessingOptions.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
 		lblPreprocessingOptions.setBounds(30, 84, 189, 25);
 		lblPreprocessingOptions.setText("Preprocessing Options:");
 		loadItems.add(lblPreprocessingOptions);
 		
 		Label lblConditionsFile = new Label(composite_3, SWT.NONE);
 		lblConditionsFile.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		lblConditionsFile.setBounds(30, 165, 116, 25);
 		lblConditionsFile.setText("Conditions File:");
 		loadItems.add(lblConditionsFile);
 		
 		text_6 = new Text(composite_3, SWT.BORDER);
 		text_6.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		text_6.setBounds(150, 165, 411, 25);
 		loadItems.add(text_6);
 		
 		Button btnBrowse_3 = new Button(composite_3, SWT.NONE);
 		btnBrowse_3.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnBrowse_3.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				browse(text_6);
 			}
 		});
 		btnBrowse_3.setText("Browse...");
 		btnBrowse_3.setBounds(568, 165, 108, 25);
 		loadItems.add(btnBrowse_3);
 		
 		Label lblNumberOfSessions = new Label(composite_3, SWT.NONE);
 		lblNumberOfSessions.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		lblNumberOfSessions.setBounds(390, 30, 147, 25);
 		lblNumberOfSessions.setText("Number of Sessions:");
 		
 		Button button_3 = new Button(composite_3, SWT.NONE);
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
 			}
 		});
 		button_3.setText("Cancel");
 		button_3.setBounds(416, 389, 84, 27);
 		loadItems.add(button_3);
 		
 		for (Control item : loadItems) {
 			item.setVisible(false);
 		}
 		
 		final Composite composite_4 = new Composite(tabFolder_1, SWT.NONE);
 		composite_4.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		composite_4.setBounds(10, 96, 694, 254);
 		composite_4.setVisible(false);
 		
 		final ArrayList<Control> loadHatachi = new ArrayList<Control>();
 		
 		text_4 = new Text(composite_4, SWT.BORDER);
 		text_4.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		text_4.setBounds(146, 190, 396, 25);
 		loadHatachi.add(text_4);
 		
 		text_5 = new Text(composite_4, SWT.BORDER);
 		text_5.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		text_5.setBounds(146, 230, 396, 25);
 		loadHatachi.add(text_5);
 		
 		final Button btnNewButton = new Button(composite_4, SWT.NONE);
 		btnNewButton.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnNewButton.setBounds(548, 190, 127, 25);
 		btnNewButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				browse(text_4);
 			}
 		});
 		btnNewButton.setText("Browse...");
 		loadHatachi.add(btnNewButton);
 		
 		final Button btnBrowse_1 = new Button(composite_4, SWT.NONE);
 		btnBrowse_1.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnBrowse_1.setBounds(548, 230, 127, 25);
 		btnBrowse_1.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				browse(text_5);
 			}
 		});
 		btnBrowse_1.setText("Browse...");
 		loadHatachi.add(btnBrowse_1);
 		
 		final Label lblHboFile = new Label(composite_4, SWT.NONE);
 		lblHboFile.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		lblHboFile.setBounds(30, 230, 84, 25);
 		lblHboFile.setText("HbO File:");
 		loadHatachi.add(lblHboFile);
 		
 		final Label lblHbFile = new Label(composite_4, SWT.NONE);
 		lblHbFile.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		lblHbFile.setBounds(30, 190, 84, 25);
 		lblHbFile.setText("Hb File:");
 		loadHatachi.add(lblHbFile);
 		
 		Button btnAdd = new Button(composite_4, SWT.NONE);
 		btnAdd.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnAdd.setBounds(330, 390, 75, 25);
 		btnAdd.setText("Add");
 		loadHatachi.add(btnAdd);
 		
 		TabItem tbtmStats = new TabItem(tabFolder, SWT.NONE);
 		tbtmStats.setText("  Statistical Analysis ");
 		
 		Composite composite_1 = new Composite(tabFolder, SWT.NONE);
 		tbtmStats.setControl(composite_1);
 		
 		Label lblChannelGrouping = new Label(composite_1, SWT.NONE);
 		lblChannelGrouping.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD | SWT.ITALIC));
 		lblChannelGrouping.setBounds(30, 30, 161, 25);
 		lblChannelGrouping.setText("Channel Grouping:");
 		
 		text_8 = new Text(composite_1, SWT.BORDER);
 		text_8.setBounds(145, 70, 439, 25);
 		
 		Button btnNewButton_2 = new Button(composite_1, SWT.NONE);
 		btnNewButton_2.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnNewButton_2.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				browse(text_8);
 			}
 		});
 		btnNewButton_2.setBounds(590, 70, 116, 25);
 		btnNewButton_2.setText("Browse...");
 		
 		final Button HbCheck = new Button(composite_1, SWT.CHECK);
 		HbCheck.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 			}
 		});
 		HbCheck.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		HbCheck.setBounds(30, 70, 48, 25);
 		HbCheck.setText("Hb");
 		
 		final Button HbOCheck = new Button(composite_1, SWT.CHECK);
 		HbOCheck.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		HbOCheck.setBounds(80, 70, 64, 25);
 		HbOCheck.setText("HbO");
 		
 		final List list_2 = new List(composite_1, SWT.BORDER);
 		list_2.setEnabled(false);
 		list_2.setBounds(30, 182, 188, 278);
 
 		final List list_3 = new List(composite_1, SWT.BORDER);
 		list_3.setEnabled(false);
 		list_3.setBounds(240, 182, 188, 278);
 		
 		final Label grouplbl = new Label(composite_1, SWT.NONE);
 		grouplbl.setEnabled(false);
 		grouplbl.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		grouplbl.setBounds(30, 151, 55, 25);
 		grouplbl.setText("Groups:");
 		
 		final Label condlbl = new Label(composite_1, SWT.NONE);
 		condlbl.setEnabled(false);
 		condlbl.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		condlbl.setBounds(240, 151, 116, 25);
 		condlbl.setText("Conditions:");
 		
 		final Label chunking = new Label(composite_1, SWT.NONE);
 		chunking.setEnabled(false);
 		chunking.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		chunking.setBounds(484, 151, 86, 25);
 		chunking.setText("Chunking:");
 		
 		text_9 = new Text(composite_1, SWT.BORDER);
 		text_9.setEnabled(false);
 		text_9.setBounds(484, 182, 55, 25);
 		
 		final Label chunks = new Label(composite_1, SWT.NONE);
 		chunks.setEnabled(false);
 		chunks.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		chunks.setBounds(545, 182, 86, 25);
 		chunks.setText("chunks");
 		
 		final Label aprecision = new Label(composite_1, SWT.NONE);
 		aprecision.setEnabled(false);
 		aprecision.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		aprecision.setBounds(484, 238, 148, 25);
 		aprecision.setText("ANOVA Precision:");
 		
 		text_10 = new Text(composite_1, SWT.BORDER);
 		text_10.setEnabled(false);
 		text_10.setBounds(484, 269, 55, 25);
 		
 		final Label dplace = new Label(composite_1, SWT.NONE);
 		dplace.setEnabled(false);
 		dplace.setText("decimal places");
 		dplace.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		dplace.setBounds(545, 269, 116, 25);
 		
 		final Button anovabtn = new Button(composite_1, SWT.NONE);
 		anovabtn.setEnabled(false);
 		anovabtn.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		anovabtn.setBounds(484, 358, 130, 25);
 		anovabtn.setText("ANOVA");
 		
 		final Label run = new Label(composite_1, SWT.NONE);
 		run.setEnabled(false);
 		run.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		run.setBounds(484, 327, 148, 25);
 		run.setText("Processes to Run:");
 		
 		Button btnLoadGroupsAnd = new Button(composite_1, SWT.NONE);
 		btnLoadGroupsAnd.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 			    // prepare to do stats things!
 			    // gotta worry about whether to do Hb data, HbO data, or both:
 			    // THIS CAN PROBABLY ALL BE REWRITTEN AS A METHOD THAT DOES EITHER HB OR
 			    //    HBO
 			    String outputDirectoryName = null; // GET FROM GUI ELSEWHERE & SAVE
 			    File groupFile = null; // GET FROM GUI ELSEWHERE & SAVE
 			    ArrayList<String> subjects = new ArrayList<String>(); // REMOVE THIS 
 			    ArrayList<File> hbFiles = null;
 			    ArrayList<File> hbOFiles = null;
 			    // IT SEEMS WE NEED TO SAVE THE VALUE OF HbCheck.getSelection() and
 			    //    HbOCheck.getSelection() GLOBALLY OR SOMETHING IN ORDER TO ACCESS
 			    //    THEM HERE
 			    // if (HbCheck.getSelection()) { // if Hb box is checked,
 			    // 	hbFiles = new ArrayList<File>(); // create an ArrayList for the 
                             //                                      //    selected subjects' Hb files
 			    // }
 			    // if (HBOCheck.getSelection()) { // if HbO box is checked,
 			    // 	hbOFiles = new ArrayList<File>(); // create an ArrayList for the 
                             //                                       //    selected subjects' HbO files
 			    // }
 			    if (hbFiles == null && hbOFiles == null) { // if neither box was checked
 				// UNCOMMENT errorBox(...) WHEN THAT CODE EXISTS (AFTER PULLING)
 				//errorBox("Error", "Please select Hb, HbO, or both.");
 				return;				
 			    }
 			    // iterate through the List of selected subjects:
 			    for (String subject: list.getSelection()) {
 				subjects.add(subject); // WE WON'T ACTUALLY USE THESE
 				if (hbFiles != null) { // if we want to analyze Hb files,
 				    hbFiles.add(workspace.getHb(subject)); // store this subject's
 				}
 				if (hbOFiles != null) { // if we want to analyze HbO files, 
 				    hbOFiles.add(workspace.getHbO(subject)); // store this subject's
 				}
 			    }
 			    //FNIRsStats.printList(subjects); // VERIFICATION--REMOVE LATER
 			    // output!
 			    // UNCOMMENT ONCE IT EXISTS:
 			    // File statsOutputDirectory = new File(getStatsPath() + "\\" +
 			    // 					 outputDirectoryName);
 			    // CHECK THE DIRECTORY DOES NOT EXIST??
 			    // UNCOMMENT ONCE IT EXISTS:
 			    // statsOutputDirectory.mkdir(); // create new directory with name given
 			    if (hbFiles != null) { // if we're analyzing Hb data
 				// UNCOMMENT ONCE IT EXISTS:				
 				// File statsHbOutputFile = new File(getStatsPath() + "\\" +
 				// 				  outputDirectoryName + "\\Hb");
 				FNIRsStats.GroupedChannels statsHb =
 				    FNIRsStats.processAllSubjectData(hbFiles, groupFile);
 				// writeANOVAs(statsHbOutputFile,
 				// 	    statsHb,
 				// 	    ...
 			    }
 			    if (hbOFiles != null) { // if we're analyzing HbO data
 				// UNCOMMENT ONCE IT EXISTS:
 				// File statsHbOOutputFile = new File(getStatsPath() + "\\" +
 				// 				   outputDirectoryName + "\\HbO");
 				FNIRsStats.GroupedChannels statsHb =
 				    FNIRsStats.processAllSubjectData(hbFiles, groupFile);
 				// writeANOVAs(statsHbOutputFile,
 				// 	    statsHb,
 				// 	    ...
 			    }
 			    
 			    
 			    grouplbl.setEnabled(true);
 			    condlbl.setEnabled(true);
 			    list_2.setEnabled(true);
 			    list_3.setEnabled(true);
 			    chunking.setEnabled(true);
 			    aprecision.setEnabled(true);
 			    dplace.setEnabled(true);
 			    chunks.setEnabled(true);
 			    run.setEnabled(true);
 			    text_9.setEnabled(true);
 			    text_10.setEnabled(true);
 			    anovabtn.setEnabled(true);
 			}
 	});
 		btnLoadGroupsAnd.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 	btnLoadGroupsAnd.setBounds(240, 115, 276, 25);
 		btnLoadGroupsAnd.setText("Load Groups and Conditions");
 		
 		TabItem tbtmMachineLearning = new TabItem(tabFolder, SWT.NONE);
 		tbtmMachineLearning.setText("  Data Mining ");
 		
 		Composite composite_2 = new Composite(tabFolder, SWT.NONE);
 		tbtmMachineLearning.setControl(composite_2);
 		
 		final ArrayList<Control> step2 = new ArrayList<Control>();
 		final ArrayList<Control> step3 = new ArrayList<Control>();
 		
 		final List list_1 = new List(composite_2, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
 		list_1.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		list_1.setBounds(30, 120, 180, 308);
 		step2.add(list_1);
 				
 		final ArrayList<Integer> cond_list = new ArrayList<Integer>();
 		
 		Button btnNewButton_1 = new Button(composite_2, SWT.NONE);
 		btnNewButton_1.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				
 				if ((list_1.getSelectionIndices()).length==0) {
 					MessageBox messageDialog = new MessageBox(shlFnirsDataProcessing, SWT.ERROR);
 				    messageDialog.setText("Warning!");
 				    messageDialog.setMessage("Select at least 1 condition.");
 				    messageDialog.open();
 					return;
 				}
 				
 				cond_list.clear(); 
 				for (Integer item: list_1.getSelectionIndices())
 					cond_list.add(item);
 				System.out.println(cond_list);
 				disableList(step2);
 				enableList(step3);
 			}
 		});
 		
 		btnNewButton_1.setBounds(30, 434, 180, 25);
 		btnNewButton_1.setText("Select");
 		step2.add(btnNewButton_1);
 		
 		for (Control item : step2) {
 			item.setEnabled(false);
 		}
 		
 		final ArrayList<Control> step1 = new ArrayList<Control>();
 		
 		final Button btnHb_1 = new Button(composite_2, SWT.CHECK);
 		btnHb_1.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnHb_1.setBounds(440, 30, 44, 25);
 		btnHb_1.setText("Hb");
 		step1.add(btnHb_1);
 		
 		final Button btnHbO_1 = new Button(composite_2, SWT.CHECK);
 		btnHbO_1.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnHbO_1.setText("HbO");
 		btnHbO_1.setBounds(490, 30, 54, 25);
 		step1.add(btnHbO_1);
 		
 		text_dm_sub = new Text(composite_2, SWT.BORDER);
 		text_dm_sub.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		text_dm_sub.setBounds(143, 30, 255, 25);
 		step1.add(text_dm_sub);
 		
 		list.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (text_dm_sub.getEnabled()) {
 					String[] dmSubjects = list.getSelection();
 					String dmSubject = dmSubjects[dmSubjects.length-1];
 					text_dm_sub.setText(dmSubject);
 				}
 			} 
 		});
 		
 		Button btnRun = new Button(composite_2, SWT.NONE);
 		btnRun.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnRun.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				disableList(step3);
 				String name = text_dm_sub.getText();
 				if (btnHb_1.getSelection()) {
 					rapidDriver.filter(cond_list, workspace.getHb(name), workspace.getHbOutput(name));
 				}
 				else {
 					rapidDriver.empty(workspace.getHbOutput(name));
 				}
 				
 				if (btnHbO_1.getSelection()) {
 					rapidDriver.filter(cond_list, workspace.getHbO(name), workspace.getHbOOutput(name));
 				}
 				else {
 					rapidDriver.empty(workspace.getHbOOutput(name));
 				}
 				
 				boolean done = false;
 				
 				try {
 					pre.rapidFormatConversion(workspace.getHbOutput(name).getAbsolutePath(),workspace.getHbOOutput(name).getAbsolutePath(),workspace.getRMInput(name).getAbsolutePath());
 					done = true;
 				}
 				catch(MWException mwe) {
 					mwe.printStackTrace();
 					done = false;
 				}
 				
 				if (done) {
 					try {
 						rapidDriver.run(workspace.getRMInput(name));
 					} catch (OperatorException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}
 				}
 				
 				enableList(step1);
 			}
 		});
 		btnRun.setBounds(216, 120, 485, 339);
 		btnRun.setText("Run");
 		step3.add(btnRun);
 		
 		for (Control item : step3) {
 			item.setEnabled(false);
 		}
 		
 		Button btnNext = new Button(composite_2, SWT.NONE);
 		btnNext.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnNext.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (text_dm_sub.getText() == "" || !Arrays.asList(list.getItems()).contains(text_dm_sub.getText())) {
 					MessageBox messageDialog = new MessageBox(shlFnirsDataProcessing, SWT.ERROR);
 				    messageDialog.setText("Warning!");
 				    messageDialog.setMessage("Subject " + text_dm_sub.getText() + " does not exist.");
 				    messageDialog.open();
 					return;
 				}
 				if (!btnHb_1.getSelection() && !btnHbO_1.getSelection()) {
 					MessageBox messageDialog = new MessageBox(shlFnirsDataProcessing, SWT.ERROR);
 				    messageDialog.setText("Warning!");
 				    messageDialog.setMessage("Select Hb or HbO or both.");
 				    messageDialog.open();
 					return;
 				}
 				disableList(step1);
 				enableList(step2);
 				
 				int n = workspace.getMaxCond(text_dm_sub.getText());
 				
 				for (int i=0; i<=n; i++) {
 					String strI = "Condition #" + i;
 					list_1.add(strI);
 				}
 			}
 		});
 		btnNext.setBounds(575, 30, 126, 25);
 		btnNext.setText("Next");
 		step1.add(btnNext);
 		
 		Label lblSubjectName_1 = new Label(composite_2, SWT.NONE);
 		lblSubjectName_1.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		lblSubjectName_1.setBounds(30, 30, 106, 25);
 		lblSubjectName_1.setText("Subject Name:");
 		
 		Label lblNewLabel_1 = new Label(composite_2, SWT.NONE);
 		lblNewLabel_1.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		lblNewLabel_1.setBounds(30, 90, 126, 25);
 		lblNewLabel_1.setText("Select Conditions:");
 
 		/*
 		Button for filtering
 		Button btnNewButton_1 = new Button(composite_2, SWT.NONE);
 		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				//rapidDriver.run();
 				rapidDriver.filter(new ArrayList(Arrays.asList(2)) , 
 						new File("C:\\Users\\jssmith\\Desktop\\Workspace\\subjects\\Work\\Hb"),
 						new File("output"));
 			}
 		});
 		btnNewButton_1.setBounds(46, 173, 75, 25);
 		btnNewButton_1.setText("Run Process");
 		 */
 		
 		
 		Button btnRemove = new Button(shlFnirsDataProcessing, SWT.NONE);
 		btnRemove.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnRemove.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				for (Integer item: list.getSelectionIndices())
 					indexList.add(item);
					//workspace.removeSubject(list.getItem((int)item));
 				int[] indices = new int[indexList.size()];
 				for (int i=0; i<indexList.size();i++)
 					indices[i] = indexList.get(i);
 				list.remove(indices);
 				indexList.clear();
 			}
 		});
 		btnRemove.setBounds(10, 507, 226, 25);
 		btnRemove.setText("Remove Files");
 		
 		Label lblSubjectName2 = new Label(composite_4, SWT.NONE);
 		lblSubjectName2.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		lblSubjectName2.setBounds(30, 30, 105, 25);
 		lblSubjectName2.setText("Subject Name:");
 		
 		text_subName2 = new Text(composite_4, SWT.BORDER);
 		text_subName2.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		text_subName2.setBounds(140, 30, 396, 25);
 		
 		final Spinner num_channels_H = new Spinner(composite_4, SWT.BORDER);
 		num_channels_H.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		num_channels_H.setBounds(422, 75, 47, 25);
 		
 		Label lblNumberOfChannels = new Label(composite_4, SWT.NONE);
 		lblNumberOfChannels.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		lblNumberOfChannels.setBounds(265, 75, 149, 25);
 		lblNumberOfChannels.setText("Number of Channels:");
 		
 		final Label label_4 = new Label(composite_4, SWT.NONE);
 		label_4.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		label_4.setVisible(false);
 		label_4.setText("File Does Not Exist");
 		label_4.setBounds(306, 339, 105, 15);
 		
 		final Spinner num_sessions_h = new Spinner(composite_4, SWT.BORDER);
 		num_sessions_h.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		num_sessions_h.setMinimum(1);
 		num_sessions_h.setBounds(182, 75, 47, 25);
 		
 		final Label lblOf_H = new Label(composite_4, SWT.NONE);
 		lblOf_H.setAlignment(SWT.CENTER);
 		lblOf_H.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		lblOf_H.setText("1 of 1");
 		lblOf_H.setBounds(330, 355, 74, 20);
 		loadHatachi.add(lblOf_H);
 		
 		final Button btnHb = new Button(composite_4, SWT.CHECK);
 		btnHb.setBounds(512, 75, 46, 16);
 		btnHb.setText("Hb");
 		
 		final Button btnHbo = new Button(composite_4, SWT.CHECK);
 		btnHbo.setBounds(564, 75, 47, 16);
 		btnHbo.setText("HbO");
 		
 		final Button button_2 = new Button(composite_4, SWT.NONE);
 		button_2.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		button_2.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				subjectNameH = text_subName2.getText();
 				
 				System.out.println(subjectName);
 				
 				if (subjectNameH == "" || subjectNameH == subjectName || Arrays.asList(list.getItems()).contains(subjectNameH)) {
 					MessageBox messageDialog = new MessageBox(shlFnirsDataProcessing, SWT.ERROR);
 				    messageDialog.setText("Warning!");
 				    messageDialog.setMessage("Please enter a new name");
 				    messageDialog.open();
 					return;
 				}
 				
 				if (!btnHb.getSelection() && !btnHbo.getSelection()) {
 					MessageBox messageDialog = new MessageBox(shlFnirsDataProcessing, SWT.ERROR);
 				    messageDialog.setText("Warning!");
 				    messageDialog.setMessage("Select Hb or HbO or both.");
 				    messageDialog.open();
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
 				
 				lblOf_H.setText("1 of " + num_sessions_h.getText());
 				
 				text_subName2.setEnabled(false);
 				num_sessions_h.setEnabled(false);
 				button_2.setEnabled(false);
 				num_channels_H.setEnabled(false);
 				btnHb.setEnabled(false);
 				btnHbo.setEnabled(false);
 			}
 		});
 		button_2.setText("Enter");
 		button_2.setBounds(542, 30, 133, 25);
 
 		btnAdd.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				
 				File condFile = new File(text_7.getText());
 				if (!setExists(condFile))
 					return;
 				
 				int channels = (Integer.valueOf(num_channels_H.getText())).intValue();
 				
 				File Hb = new File(text_4.getText());
 				File HbO = new File(text_5.getText());
 				
 				File HbFile = new File("HbFile");
 				File HbOFile = new File("HbOFile");
 				
 				//FileInputStream input = new FileInputStream(HbFile);
 				//FileOutputStream output = new FileOutputStream(Hb);
 				
 				if (text_4.getText()!="") {
 					try {
 						Files.copy(Hb.toPath(),HbFile.toPath().toAbsolutePath(),StandardCopyOption.REPLACE_EXISTING);
 					} catch (IOException e2) {
 						// TODO Auto-generated catch block
 						e2.printStackTrace();
 						return;
 					}
 				}
 				else {
 					HbFile = null;
 				}
 				
 				if (text_5.getText()!="") {				
 					try {
 						Files.copy(HbO.toPath(),HbOFile.toPath().toAbsolutePath(),StandardCopyOption.REPLACE_EXISTING);
 					} catch (IOException e2) {
 						// TODO Auto-generated catch block
 						e2.printStackTrace();
 						return;
 					}
 				}
 				else {
 					HbOFile = null;
 				}
 				
 				if (HbFile==null && HbOFile==null) {
 					return;
 				}
 
 				/*if (HbFile.exists()) {
 					try {
 						pre.xlsreadfile(HbFile.getAbsolutePath(), "Hb", channels);
 						HbFile = new File("Hb");
 					} catch (MWException e1) {
 						e1.printStackTrace();
 					}
 				}
 				
 				if (HbOFile.exists()) {
 					try {
 						pre.xlsreadfile(HbOFile.getAbsolutePath(), "HbO", channels);
 						HbOFile = new File("HbO");
 					} catch (MWException e1) {
 						e1.printStackTrace();
 					}
 				}*/
 
 				if (sessionNumH==1) {
 					workspace.addSubject(subjectNameH, HbFile, HbOFile, condFile);
 				}
 				else{
 					workspace.concatSession(subjectNameH, HbFile, HbOFile, condFile);
 				}
 				sessionNumH++;
 				
 				if (sessionNumH>Integer.valueOf(num_sessions_h.getText()).intValue()) {
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
 				}
 				else {
 					lblOf_H.setText(sessionNumH + " of " + num_sessions_h.getText());
 				}
 				
 				text_4.setText("");
 				text_5.setText("");
 				text_7.setText("");
 				text_subName2.setText("");
 			}
 		});
 		loadHatachi.add(btnAdd);
 		
 		TabItem tbtmNewItem_2 = new TabItem(tabFolder_1, SWT.NONE);
 		tbtmNewItem_2.setText("Hitachi");
 		tbtmNewItem_2.setControl(composite_4);
 		
 		Label label_3 = new Label(composite_4, SWT.NONE);
 		label_3.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		label_3.setText("Conditions File:");
 		label_3.setBounds(30, 270, 111, 25);
 		loadHatachi.add(label_3);
 		
 		text_7 = new Text(composite_4, SWT.BORDER);
 		text_7.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		text_7.setBounds(146, 270, 396, 25);
 		loadHatachi.add(text_7);
 		
 /*<<<<<<< HEAD
 		Button btnBrowse_2 = new Button(composite_4, SWT.NONE);
 		btnBrowse_2.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnBrowse_2.addSelectionListener(new SelectionAdapter() {
 ======= */
 		Button btnBrowse_2 = new Button(composite_4, SWT.NONE);
 		btnBrowse_2.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		btnBrowse_2.setBounds(548, 270, 127, 25);
 		btnBrowse_2.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				browse(text_7);
 			}
 		});
 
 		btnBrowse_2.setText("Browse...");
 		loadHatachi.add(btnBrowse_2);
 		
 		Label label_2 = new Label(composite_4, SWT.NONE);
 		label_2.setText("Preprocessing Options:");
 		label_2.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
 		label_2.setBounds(30, 128, 189, 25);
 		loadHatachi.add(label_2);
 		
 		Label label_5 = new Label(composite_4, SWT.NONE);
 		label_5.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
 		label_5.setText("Number of Sessions:");
 		label_5.setBounds(30, 75, 149, 25);
 		
 		Button btnCancel = new Button(composite_4, SWT.NONE);
 		btnCancel.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
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
 			}
 		});
 		btnCancel.setBounds(411, 390, 84, 25);
 		btnCancel.setText("Cancel");
 		loadHatachi.add(btnCancel);
 		
 		TabItem tbtmOther = new TabItem(tabFolder_1, SWT.NONE);
 		tbtmOther.setText("Other");
 		
 		for (Control item : loadHatachi) {
 			item.setVisible(false);
 		}
 	}
 }
