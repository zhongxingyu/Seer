 package app.screens;
 
 import app.db.DBManager;
 import app.entities.Customer;
 import app.entities.Job;
 import app.utils.RowWrapTableModelListener;
 import app.utils.Utils;
 import app.utils.WorkTableModel;
 import com.alee.extended.date.WebDateField;
 import com.alee.laf.WebLookAndFeel;
 import jxl.Cell;
 import jxl.Sheet;
 import jxl.Workbook;
 import org.jdesktop.swingx.*;
 import org.jdesktop.swingx.painter.MattePainter;
 import org.jdesktop.swingx.renderer.DefaultTableRenderer;
 
 import javax.print.attribute.HashPrintRequestAttributeSet;
 import javax.print.attribute.PrintRequestAttributeSet;
 import javax.print.attribute.standard.OrientationRequested;
 import javax.swing.*;
 import javax.swing.Timer;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.plaf.ColorUIResource;
 import javax.swing.plaf.FontUIResource;
 import javax.swing.plaf.basic.BasicMenuUI;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.print.PrinterException;
 import java.io.File;
 import java.text.DateFormat;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Barak
  * Date: 27/09/13
  * Time: 01:55
  * To change this template use File | Settings | File Templates.
  */
 public class WorkLogScr extends JXFrame
 {
	public static Dimension DEFAULT_COMBO_SIZE = new Dimension(100, 25);
 	public static Dimension DEFAULT_DATE_SIZE = new Dimension(100, 25);
 	public static Dimension DEFAULT_WORKFILED_SIZE = new Dimension(250, 25);
 	public static Dimension DEFAULT_BUTTON_SIZE = new Dimension(120, 30);
 	public static Font DEFAULT_TEXT_FONT = new Font("Arial", Font.PLAIN, 15);
 	public static Font DEFAULT_LABEL_FONT = new Font("Arial", Font.BOLD, 14);
 	public static Font DEFAULT_TITLE_FONT = new Font("Arial", Font.BOLD, 16);
 	public static Color NEW_RECORD_COLOR = new Color(217,242,138);
 	public static Color NEW_RECORD_SELECTED_COLOR = new Color(149,191,21);
 	public static Color UPDATED_RECORD_COLOR = new Color(232,242, 99);
 	public static Color UPDATED_RECORD_SELECTED_COLOR = new Color(203, 205, 46);
 	public static MattePainter TITLE_PAINTER = new MattePainter(new GradientPaint(0, 30, Color.darkGray, 0, 0, Color.lightGray));
 	public static SimpleDateFormat defaultDateFormat = new SimpleDateFormat("dd/MM/yy");
 
 	private WorkTableModel workTableModel = new WorkTableModel();
 	private JXTable workTable = new JXTable(workTableModel);
 	private JXLabel currentDateLabel = new JXLabel();
 	private JXLabel fromDateLabel = new JXLabel("מתאריך:");
 	private WebDateField fromDatePicker = new WebDateField();
 	private JXLabel toDateLabel = new JXLabel("עד תאריך:");
 	private JXButton filterDatesButton = new JXButton("סנן תאריכים");
 	private JXLabel customerLabel = new JXLabel("לקוח:");
 	private JXComboBox customerCombo = new JXComboBox();
 	private JXButton resetButton = new JXButton("נקה חיפוש");
 	private WebDateField toDatePicker = new WebDateField(Calendar.getInstance().getTime());
 	private JXLabel workLabel = new JXLabel("עבודה שנעשתה:");
 	private JXTextField workField = new JXTextField();
 	private JXButton multipleAddButton = new JXButton("הוספה מרובה");
 	private JXButton removeButton = new JXButton("מחק רשומה");
 	private JXButton printButton = new JXButton("הדפס טבלה", new ImageIcon(Utils.scaleImage("/images/print.png", 25, 25)));
 	private JXButton importFromExcelButton = new JXButton("יבא מאקסל", new ImageIcon(Utils.scaleImage("/images/excel.png", 25, 25)));
 
 	public WorkLogScr() throws HeadlessException
 	{
 		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
 		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/tools.png")));
 		this.setTitle("יומן עבודות נועם");
 		Utils.setSoftSize(this, new Dimension(1200, 800));
 		this.setLocationRelativeTo(null);
 
 		try
 		{
 			initComponents();
 		}
 		catch (Exception e)
 		{
 			Utils.showExceptionMsg(this, e);
 			System.exit(0);
 		}
 
 		JXPanel helloPanel = new JXPanel();
 		Utils.setLineLayout(helloPanel);
 		Utils.addStandardRigid(helloPanel);
 		helloPanel.add(new JXLabel(new ImageIcon(Utils.scaleImage("/images/dad.png", 50, 50))));
 		Utils.addStandardRigid(helloPanel);
 		JXLabel helloLabel = new JXLabel("שלום נועם, התאריך היום");
 		helloLabel.setFont(DEFAULT_TITLE_FONT);
 		helloPanel.add(helloLabel);
 		Utils.addSmallRigid(helloPanel);
 		helloPanel.add(currentDateLabel);
 
 		JXPanel filterPanel = new JXPanel();
 		Utils.setLineLayout(filterPanel);
 		Utils.addStandardRigid(filterPanel);
 		filterPanel.add(fromDateLabel);
 		Utils.addStandardRigid(filterPanel);
 		filterPanel.add(fromDatePicker);
 		Utils.addStandardRigid(filterPanel);
 		filterPanel.add(toDateLabel);
 		Utils.addStandardRigid(filterPanel);
 		filterPanel.add(toDatePicker);
 		Utils.addStandardRigid(filterPanel);
 		filterPanel.add(filterDatesButton);
 		Utils.addStandardRigid(filterPanel);
 		filterPanel.add(customerLabel);
 		Utils.addStandardRigid(filterPanel);
 		filterPanel.add(customerCombo);
 		Utils.addStandardRigid(filterPanel);
 		filterPanel.add(workLabel);
 		Utils.addStandardRigid(filterPanel);
 		filterPanel.add(workField);
 		Utils.addStandardRigid(filterPanel);
 		filterPanel.add(resetButton);
 		Utils.addStandardRigid(filterPanel);
 
 		JXTitledPanel filterTitledPanel = new JXTitledPanel("סינון");
 		filterTitledPanel.setTitlePainter(TITLE_PAINTER);
 		filterTitledPanel.setTitleFont(DEFAULT_TITLE_FONT);
 		Utils.setPageLayout(filterTitledPanel);
 		Utils.addTinyRigid(filterTitledPanel);
 		filterTitledPanel.add(filterPanel);
 		Utils.addSmallRigid(filterTitledPanel);
 
 		workTable.setDefaultRenderer(Object.class, new WorkTableRenderer());
 		workTable.getTableHeader().setReorderingAllowed(false);
 		workTableModel.setRowHeightWrapper(workTable);
 		JScrollPane tableScrollPane = new JScrollPane(workTable);
 
 		JXTitledPanel tableTitledPanel = new JXTitledPanel("רישומי עבודות");
 		tableTitledPanel.setTitlePainter(TITLE_PAINTER);
 		tableTitledPanel.setTitleFont(DEFAULT_TITLE_FONT);
 		tableTitledPanel.add(tableScrollPane);
 
 		JXPanel editButtonsPanel = new JXPanel();
 		Utils.setLineLayout(editButtonsPanel);
 		Utils.addStandardRigid(editButtonsPanel);
 		editButtonsPanel.add(multipleAddButton);
 		Utils.addStandardRigid(editButtonsPanel);
 		editButtonsPanel.add(removeButton);
 		editButtonsPanel.add(Box.createHorizontalGlue());
 
 		JXPanel printAndExcelImportButtonsPanel = new JXPanel();
 		Utils.setLineLayout(printAndExcelImportButtonsPanel);
 		printAndExcelImportButtonsPanel.add(Box.createHorizontalGlue());
 		Utils.addStandardRigid(printAndExcelImportButtonsPanel);
 		printAndExcelImportButtonsPanel.add(printButton);
 		Utils.addStandardRigid(printAndExcelImportButtonsPanel);
 		printAndExcelImportButtonsPanel.add(importFromExcelButton);
 		Utils.addStandardRigid(printAndExcelImportButtonsPanel);
 
 		JXPanel lineButtonsPanel = new JXPanel();
 		lineButtonsPanel.setLayout(new BorderLayout());
 		lineButtonsPanel.add(editButtonsPanel, BorderLayout.EAST);
 		lineButtonsPanel.add(printAndExcelImportButtonsPanel, BorderLayout.WEST);
 
 		JXPanel pageButtonPanel = new JXPanel();
 		Utils.setPageLayout(pageButtonPanel);
 		Utils.addStandardRigid(pageButtonPanel);
 		pageButtonPanel.add(lineButtonsPanel);
 		Utils.addStandardRigid(pageButtonPanel);
 
 		JXPanel mainPanel = new JXPanel();
 		mainPanel.setLayout(new BorderLayout());
 		mainPanel.add(filterTitledPanel, BorderLayout.NORTH);
 		mainPanel.add(tableTitledPanel, BorderLayout.CENTER);
 		mainPanel.add(pageButtonPanel, BorderLayout.SOUTH);
 
 		this.getContentPane().setLayout(new BorderLayout());
 		this.getContentPane().add(helloPanel, BorderLayout.NORTH);
 		this.getContentPane().add(mainPanel, BorderLayout.CENTER);
 		this.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
 
 		this.setVisible(true);
 	}
 
 	private void initComponents() throws Exception
 	{
 		initDateComponents();
 		initMenuBar();
 		initSizesAndFonts();
 		initEventHandlers();
 		initHelloTimer();
 		initComponentsFromDB();
 	}
 
 	private void initDateComponents()
 	{
 		// Set the from date picker to be a month back
 		Calendar monthBack = Calendar.getInstance();
 		monthBack.add(Calendar.MONTH, -1);
 		fromDatePicker.setDate(monthBack.getTime());
 
 		fromDatePicker.setDateFormat(defaultDateFormat);
 		toDatePicker.setDateFormat(defaultDateFormat);
 	}
 
 	private void initMenuBar()
 	{
 		JMenuItem itemsMenuItem = new JMenuItem("פריטים");
 		itemsMenuItem.setFont(DEFAULT_LABEL_FONT);
 		itemsMenuItem.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				new InventoryScreen(WorkLogScr.this).setVisible(true);
 			}
 		});
 
 		JMenu inventoryMenu = new JMenu("מלאי");
 		inventoryMenu.add(itemsMenuItem);
 		inventoryMenu.setFont(DEFAULT_LABEL_FONT);
 		inventoryMenu.setUI(new BasicMenuUI());
 		inventoryMenu.updateUI();
 
 		JMenuBar menuBar = new JMenuBar();
 		menuBar.add(inventoryMenu);
 
 		this.setJMenuBar(menuBar);
 	}
 
 	private void initEventHandlers()
 	{
 		workTable.addMouseListener(new MouseAdapter()
 		{
 			@Override
 			public void mouseClicked(MouseEvent e)
 			{
 				int realSelectedRow = Utils.getRealRow(workTable.getSelectedRow(), workTable);
 
 				if (e.getClickCount() >= 2 && realSelectedRow != -1)
 				{
 					Job jobToUpdate = workTableModel.getCurrentEntityList().get(realSelectedRow);
 					JobRecordDialog recordDialog = new JobRecordDialog(WorkLogScr.this,jobToUpdate);
 
 					if (recordDialog.isFinished() && !jobToUpdate.equals(recordDialog.getReturnedJob()))
 					{
 						try
 						{
 							DBManager.getSingleton().updateJob(recordDialog.getReturnedJob());
 							workTableModel.updateRow(realSelectedRow, recordDialog.getReturnedJob());
 						}
 						catch (Exception e1)
 						{
 							Utils.showExceptionMsg(WorkLogScr.this, e1);
 							e1.printStackTrace();
 						}
 					}
 				}
 			}
 		});
 
 		workField.addKeyListener(new KeyAdapter()
 		{
 			@Override
 			public void keyTyped(KeyEvent e)
 			{
 				doFilter();
 			}
 		});
 
 		filterDatesButton.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				try
 				{
 					workTableModel.setInitialEntityList(
 						DBManager.getSingleton().getJobsByDates(fromDatePicker.getDate(), toDatePicker.getDate()));
 					doFilter();
 				}
 				catch (Exception e1)
 				{
 					Utils.showExceptionMsg(WorkLogScr.this, e1);
 					e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 				}
 			}
 		});
 
 		multipleAddButton.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				JobRecordDialog jobRecordDialog = new JobRecordDialog(WorkLogScr.this, null);
 				while (!jobRecordDialog.isFinished())
 				{
 					Job job  = jobRecordDialog.getReturnedJob();
 					if (job == null)
 					{
 						return;
 					}
 
 					workTableModel.addRow(job);
 					jobRecordDialog = new JobRecordDialog(WorkLogScr.this, null);
 				}
 			}
 		});
 
 		removeButton.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				int[] selectedRows = workTable.getSelectedRows();
 				if (selectedRows.length == 0) return;
 				try
 				{
 					List<Job> removedJobs = new ArrayList<Job>();
 
 					for (int selectedRow : selectedRows)
 					{
 						Job job = workTableModel.getCurrentEntityList().get(Utils.getRealRow(selectedRow, workTable));
 						DBManager.getSingleton().removeJob(job.getId());
 						removedJobs.add(job);
 					}
 
 					workTableModel.removeRows(removedJobs);
 				}
 				catch (Exception e1)
 				{
 					Utils.showExceptionMsg(WorkLogScr.this, e1);
 					e1.printStackTrace();
 				}
 			}
 		});
 
 		printButton.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				try
 				{
 					PrintRequestAttributeSet set = new HashPrintRequestAttributeSet();
 					set.add(OrientationRequested.LANDSCAPE);
 					workTable.print(JTable.PrintMode.FIT_WIDTH, null, null, true, set, true);
 				}
 				catch (PrinterException e1)
 				{
 					Utils.showExceptionMsg(WorkLogScr.this, e1);
 					e1.printStackTrace();
 				}
 			}
 		});
 
 		importFromExcelButton.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				try
 				{
 					importFromExcel();
 				}
 				catch (Exception e1)
 				{
 					Utils.showExceptionMsg(WorkLogScr.this, e1);
 					e1.printStackTrace();
 				}
 			}
 		});
 
 		customerCombo.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				doFilter();
 			}
 		});
 
 		resetButton.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				workField.setText("");
 				customerCombo.setSelectedIndex(0);
 				doFilter();
 			}
 		});
 	}
 
 	private void initHelloTimer()
 	{
 		currentDateLabel.setText(DateFormat.getDateTimeInstance
 			(DateFormat.SHORT, DateFormat.SHORT).format(Calendar.getInstance().getTime()));
 
 		Timer helloTimer = new Timer(60000, new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				currentDateLabel.setText(DateFormat.getDateTimeInstance
 					(DateFormat.SHORT, DateFormat.SHORT).format(Calendar.getInstance().getTime()));
 
 				currentDateLabel.repaint();
 			}
 		});
 		helloTimer.start();
 	}
 
 	private void initSizesAndFonts()
 	{
 		Utils.setSoftSize(customerCombo, DEFAULT_COMBO_SIZE);
 		Utils.setHardSize(fromDatePicker, DEFAULT_DATE_SIZE);
 		Utils.setHardSize(toDatePicker, DEFAULT_DATE_SIZE);
 		Utils.setSoftSize(workField, DEFAULT_WORKFILED_SIZE);
 		Utils.setSoftSize(filterDatesButton, DEFAULT_BUTTON_SIZE);
 		Utils.setSoftSize(multipleAddButton, DEFAULT_BUTTON_SIZE);
 		Utils.setSoftSize(removeButton, DEFAULT_BUTTON_SIZE);
 		Utils.setSoftSize(resetButton, DEFAULT_BUTTON_SIZE);
 		Utils.setSoftSize(printButton, new Dimension(150, 25));
 		Utils.setSoftSize(importFromExcelButton, new Dimension(150, 25));
 		fromDatePicker.setFont(DEFAULT_TEXT_FONT);
 		toDatePicker.setFont(DEFAULT_TEXT_FONT);
 
 		workTable.getTableHeader().setFont(DEFAULT_LABEL_FONT);
 		workTable.getColumn(WorkTableModel.DATE_COL).setMaxWidth(85);
 		workTable.getColumn(WorkTableModel.DATE_COL).setMinWidth(85);
 		workTable.getColumn(WorkTableModel.CUSTOMER_COL).setMinWidth(80);
 		workTable.getColumn(WorkTableModel.CUSTOMER_COL).setMaxWidth(120);
 		workTable.getColumn(WorkTableModel.CUSTOMER_COL).setPreferredWidth(120);
 		workTable.getColumn(WorkTableModel.JOBS_DESCR_COL).setMinWidth(500);
 		workTable.getColumn(WorkTableModel.PRICE_COL).setMinWidth(80);
 		workTable.getColumn(WorkTableModel.PRICE_COL).setMaxWidth(120);
 		workTable.getColumn(WorkTableModel.PRICE_COL).setPreferredWidth(120);
 		workTable.getColumn(WorkTableModel.REMARKS_COL).setMinWidth(100);
 		workTable.getColumn(WorkTableModel.REMARKS_COL).setPreferredWidth(100);
 		workTable.getColumn(WorkTableModel.REMARKS_COL).setMaxWidth(200);
 		workTable.setRowHeight(RowWrapTableModelListener.DEFAULT_ROW_HEIGHT);
 	}
 
 	private void importFromExcel() throws Exception
 	{
 		final JFileChooser chooser = new JFileChooser();
 		chooser.setCurrentDirectory(chooser.getFileSystemView().getParentDirectory(new File("C:\\")).getParentFile());
 		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 		chooser.setFileFilter(new FileNameExtensionFilter("Excel document (*.xls)", "xls"));
 		chooser.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
 		chooser.showDialog(WorkLogScr.this, "טען");
 
 		List<Job> jobs = new ArrayList<Job>();
 		File inputWorkbook = chooser.getSelectedFile();
 
 		if (chooser.getSelectedFile() == null)
 		{
 			return;
 		}
 
 		boolean isProblemOccured = false;
 		StringBuilder problematicRows = new StringBuilder("בעיה ביבוא של העבודות הבאות:").append("\n");
 		Workbook workbook;
 		try
 		{
 			workbook = Workbook.getWorkbook(inputWorkbook);
 			for (Sheet sheet : workbook.getSheets())
 			{
 				for (int row = 1; row < sheet.getRows(); row++)
 				{
 					Cell[] cells = sheet.getRow(row);
 					int customerIndex = 0;
 					int dateIndex = 1;
 					int jobDescIndex = 2;
 					int priceIndex = 3;
 					int remarksIndex = 4;
 
 					try
 					{
 						if (checkCellsValidity(cells, new int[]{customerIndex,dateIndex,jobDescIndex}))
 						{
 							Customer customer = new Customer(cells[customerIndex].getContents());
 							Date jobDate = defaultDateFormat.parse(cells[dateIndex].getContents());
 							String jobDescr = cells[jobDescIndex].getContents();
 							double price = (cells.length < priceIndex + 1 || Utils.isCellEmpty(cells[priceIndex])) ? 0 : Double.parseDouble(cells[priceIndex].getContents());
 							String remarks = (cells.length < remarksIndex + 1 || Utils.isCellEmpty(cells[remarksIndex])) ? "" : cells[remarksIndex].getContents();
 							jobs.add(new Job(jobDate, customer, jobDescr, price, remarks));
 						}
 					}
 					catch (Exception e)
 					{
 						e.printStackTrace();
 						problematicRows.append("בגיליון ").append(sheet.getName()).append(" בשורה מס' ").append(row).append("\n");
 						isProblemOccured = true;
 					}
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			Utils.showExceptionMsg(this, e);
 			e.printStackTrace();
 		}
 
 		if (isProblemOccured)
 		{
 			Utils.showErrorMsg(WorkLogScr.this, problematicRows.toString());
 			return;
 		}
 
 		for (Job job : jobs)
 		{
 			Customer customer = DBManager.getSingleton().getCustomerByName(job.getCustomer().getName());
 			if (customer == null)
 			{
 				customer = job.getCustomer();
 				customer.setId(DBManager.getSingleton().addCustomer(job.getCustomer()));
 			}
 
 			job.setCustomer(customer);
 			job.setId(DBManager.getSingleton().addJob(job));
 		}
 
 		workTableModel.getCurrentEntityList().addAll(jobs);
 		workTableModel.fireTableDataChanged();
 		Utils.initCustomerCombo(customerCombo, true);
 
 		Utils.showInfoMsg(this, "הנתונים נטענו בהצלחה!");
 	}
 
 	private boolean checkCellsValidity(Cell[] cells, int[] mustCells) throws Exception
 	{
 		boolean areAllCellsEmpty = true;
 
 		for (int i = 0; i < cells.length && areAllCellsEmpty; i++)
 		{
 			if (!Utils.isCellEmpty(cells[i]))
 			{
 				areAllCellsEmpty = false;
 			}
 		}
 
 		// If all the cells are empty we don't want to use that row
 		if (areAllCellsEmpty)
 		{
 			return false;
 		}
 		else
 		{
 			for (int mustCell : mustCells)
 			{
 				if (Utils.isCellEmpty(cells[mustCell]))
 				{
 					throw new Exception("אחד או יותר מהשדות ההכרחיים חסר!");
 				}
 			}
 
 			return true;
 		}
 	}
 
 	private void doFilter()
 	{
 		List<Job> preservedJobList = workTableModel.getInitialEntityList();
 		List<Job> filteredJobList = new ArrayList<Job>();
 		for (Job job : preservedJobList)
 		{
 			if (job.getJobDescription().contains(workField.getText()))
 			{
 				if (customerCombo.getSelectedItem().equals(Customer.ALL_VALUES))
 				{
 					filteredJobList.add(job);
 				}
 				else if (customerCombo.getSelectedItem().equals(job.getCustomer()))
 				{
 					filteredJobList.add(job);
 				}
 			}
 		}
 
 		workTableModel.setCurrentEntityList(filteredJobList);
 	}
 
 	private void initComponentsFromDB() throws Exception
 	{
 		Utils.initCustomerCombo(customerCombo, true);
 		workTableModel.setInitialEntityList
 			(DBManager.getSingleton().getJobsByDates(fromDatePicker.getDate(), toDatePicker.getDate()));
 	}
 
 	private class WorkTableRenderer extends DefaultTableRenderer
 	{
 		private JTextArea textArea = new JTextArea();
 
 		private WorkTableRenderer()
 		{
 			textArea.setLineWrap(true);
 			textArea.setWrapStyleWord(true);
 		}
 
 		@Override
 		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
 		{
 			int realRow = Utils.getRealRow(row, table);
 
 			if (column == WorkTableModel.DATE_COL)
 			{
 				value = DateFormat.getDateInstance().format(value);
 			}
 			else if (column == WorkTableModel.PRICE_COL)
 			{
 				value = NumberFormat.getCurrencyInstance().format(value);
 			}
 
 			Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
 
 			component.setFont(DEFAULT_TEXT_FONT);
 
 			if (column == WorkTableModel.JOBS_DESCR_COL)
 			{
 				textArea.setText(value.toString());
 				textArea.setBackground(component.getBackground());
 				textArea.setForeground(component.getForeground());
 
 				component = textArea;
 			}
 
 			if (workTableModel.getCurrentEntityList().get(realRow).isNewRecord())
 			{
 				component.setBackground(isSelected ? NEW_RECORD_SELECTED_COLOR : NEW_RECORD_COLOR);
 			}
 			else if (workTableModel.getCurrentEntityList().get(realRow).isUpdated())
 			{
 				component.setBackground(isSelected ? UPDATED_RECORD_SELECTED_COLOR : UPDATED_RECORD_COLOR);
 			}
 
 			return component;
 		}
 	}
 
 	private static void updateUIMangerAndLocale()
 	{
 		UIManager.put("ComboBox.background", new ColorUIResource(UIManager.getColor("TextField.background")));
 		UIManager.put("ComboBox.font", new FontUIResource(DEFAULT_TEXT_FONT));
 		UIManager.put("TextField.font", new FontUIResource(DEFAULT_TEXT_FONT));
 		UIManager.put("TextArea.font", new FontUIResource(DEFAULT_TEXT_FONT));
 		UIManager.put("Label.font", new FontUIResource(DEFAULT_LABEL_FONT));
 		UIManager.put("Button.font", new FontUIResource(DEFAULT_LABEL_FONT));
 		UIManager.put("OptionPane.okButtonText", "אישור");
 		UIManager.put("OptionPane.cancelButtonText", "ביטול");
 		UIManager.put("FileChooser.cancelButtonText", "ביטול");
 		UIManager.put("FileChooser.directoryOpenButtonText", "פתח");
 		UIManager.put("JTitledPanel.title.foreground", "black");
 		Locale.setDefault(new Locale("he", "IL"));
 	}
 
 	public static void main(String args[])
 	{
 		WebLookAndFeel.install();
 		updateUIMangerAndLocale();
 		new WorkLogScr();
 	}
 }
