 import jxl.Cell;
 import jxl.Sheet;
 import jxl.Workbook;
 import org.jdesktop.swingx.*;
 import org.jdesktop.swingx.renderer.DefaultTableRenderer;
 
 import javax.print.attribute.HashPrintRequestAttributeSet;
 import javax.print.attribute.PrintRequestAttributeSet;
 import javax.print.attribute.standard.OrientationRequested;
 import javax.swing.*;
 import javax.swing.Timer;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.plaf.ColorUIResource;
 import javax.swing.plaf.FontUIResource;
 import javax.swing.table.AbstractTableModel;
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
     public static Dimension DEFAULT_BUTTON_SIZE = new Dimension(120, 25);
     public static Font DEFAULT_TEXT_FONT = new Font("Arial", Font.PLAIN, 15);
     public static Font DEFAULT_LABEL_FONT = new Font("Arial", Font.BOLD, 14);
     public static Font DEFAULT_TITLE_FONT = new Font("Arial", Font.BOLD, 16);
     public static Color NEW_RECORD_COLOR = new Color(217,242,138);
     public static Color NEW_RECORD_SELECTED_COLOR = new Color(149,191,21);
     public static Color UPDATED_RECORD_COLOR = new Color(232,242, 99);
     public static Color UPDATED_RECORD_SELECTED_COLOR = new Color(203, 205, 46);
 
     static
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
         Locale.setDefault(new Locale("he", "IL"));
     }
 
     private JXTable workTable = new JXTable(new WorkTableModel());
     private JXLabel currentDateLabel = new JXLabel();
     private JXLabel fromDateLabel = new JXLabel("מתאריך:");
     private JXDatePicker fromDatePicker = new JXDatePicker(Locale.getDefault());
     private JXLabel toDateLabel = new JXLabel("עד תאריך:");
     private JXButton filterDatesButton = new JXButton("סנן תאריכים");
     private JXLabel customerLabel = new JXLabel("לקוח:");
     private JXComboBox customerCombo = new JXComboBox();
     private JXButton resetButton = new JXButton("נקה חיפוש");
     private JXDatePicker toDatePicker = new JXDatePicker(Calendar.getInstance().getTime(), Locale.getDefault());
     private JXLabel workLabel = new JXLabel("עבודה שנעשתה:");
     private JXTextField workField = new JXTextField();
     private JXButton multipleAddButton = new JXButton("הוספה מרובה");
     private JXButton removeButton = new JXButton("מחק רשומה");
     private JXButton printButton = new JXButton("הדפס טבלה", new ImageIcon(Utils.scaleImage("print.png", 25 ,25)));
     private JXButton importFromExcelButton = new JXButton("יבא מאקסל", new ImageIcon(Utils.scaleImage("excel.png", 25 ,25)));
 
     public WorkLogScr() throws HeadlessException
     {
         this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("tools.png")));
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
         helloPanel.add(new JXLabel(new ImageIcon(Utils.scaleImage("dad.png", 50, 50))));
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
         filterTitledPanel.setTitleFont(DEFAULT_TITLE_FONT);
         Utils.setPageLayout(filterTitledPanel);
         Utils.addTinyRigid(filterTitledPanel);
         filterTitledPanel.add(filterPanel);
         Utils.addSmallRigid(filterTitledPanel);
 
         JScrollPane tableScrollPane = new JScrollPane(workTable);
 
         JXTitledPanel tableTitledPanel = new JXTitledPanel("רישומי עבודות");
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
         Utils.setSoftSize(customerCombo, DEFAULT_COMBO_SIZE);
         Utils.setSoftSize(fromDatePicker, DEFAULT_DATE_SIZE);
         Utils.setSoftSize(toDatePicker, DEFAULT_DATE_SIZE);
         Utils.setSoftSize(workField, DEFAULT_WORKFILED_SIZE);
         Utils.setSoftSize(filterDatesButton, DEFAULT_BUTTON_SIZE);
         Utils.setSoftSize(multipleAddButton, DEFAULT_BUTTON_SIZE);
         Utils.setSoftSize(removeButton, DEFAULT_BUTTON_SIZE);
         Utils.setSoftSize(resetButton, DEFAULT_BUTTON_SIZE);
         Utils.setSoftSize(printButton, new Dimension(150, 25));
         Utils.setSoftSize(importFromExcelButton, new Dimension(150, 25));
         fromDatePicker.setFont(DEFAULT_TEXT_FONT);
         toDatePicker.setFont(DEFAULT_TEXT_FONT);
 
         Calendar monthBack = Calendar.getInstance();
         monthBack.add(Calendar.MONTH, -1);
         fromDatePicker.setDate(monthBack.getTime());
 
         workTable.getTableHeader().setFont(DEFAULT_LABEL_FONT);
         workTable.getColumn(WorkTableModel.JOBS_DESCR_COL).setMinWidth(350);
         workTable.getColumn(WorkTableModel.REMARKS_COL).setMinWidth(250);
         workTable.setDefaultRenderer(Object.class, new WorkTableRenderer());
 
         workTable.addMouseListener(new MouseAdapter()
         {
             @Override
             public void mouseClicked(MouseEvent e)
             {
                 if (e.getClickCount() >= 2 && workTable.getSelectedRow() != -1)
                 {
                     Job jobToUpdate = ((WorkTableModel) workTable.getModel()).getCurrentJobList().get(workTable.getSelectedRow());
                     JobRecordDialog recordDialog = new JobRecordDialog(WorkLogScr.this,jobToUpdate);
 
                     if (recordDialog.isFinished() && !jobToUpdate.equals(recordDialog.getReturnedJob()))
                     {
                         try
                         {
                             DBManager.getSingleton().updateJob(recordDialog.getReturnedJob());
                             ((WorkTableModel)workTable.getModel()).getCurrentJobList().set(workTable.getSelectedRow(),
                                     recordDialog.getReturnedJob());
                             ((WorkTableModel)workTable.getModel()).fireTableDataChanged();
                         } catch (Exception e1)
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
                     ((WorkTableModel)workTable.getModel()).setPreservedJobList
                             (DBManager.getSingleton().getJobsByDates(fromDatePicker.getDate(), toDatePicker.getDate()));
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
 
                     ((WorkTableModel)workTable.getModel()).getCurrentJobList().add(job);
                     ((WorkTableModel) workTable.getModel()).fireTableDataChanged();
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
 
                     for (int i = 0; i < selectedRows.length; i++)
                     {
                         Job job = ((WorkTableModel)workTable.getModel()).getCurrentJobList().
                                 get(workTable.getRowSorter().convertRowIndexToModel(selectedRows[i]));
                         DBManager.getSingleton().removeJob(job.getId());
                         removedJobs.add(job);
                     }
 
                     ((WorkTableModel)workTable.getModel()).getCurrentJobList().removeAll(removedJobs);
                     ((WorkTableModel) workTable.getModel()).fireTableDataChanged();
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
 
         initComponentsFromDB();
     }
 
     public void importFromExcel() throws Exception
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
 
         SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
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
                             Date jobDate = dateFormat.parse(cells[dateIndex].getContents());
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
 
         ((WorkTableModel)workTable.getModel()).getCurrentJobList().addAll(jobs);
         ((WorkTableModel)workTable.getModel()).fireTableDataChanged();
         initCustomerCombo();
 
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
             for (int i = 0; i < mustCells.length; i++)
             {
                 if (Utils.isCellEmpty(cells[mustCells[i]]))
                 {
                     throw new Exception("אחד או יותר מהשדות ההכרחיים חסר!");
                 }
             }
 
             return true;
         }
     }
 
     private void doFilter()
     {
         List<Job> preservedJobList = ((WorkTableModel)workTable.getModel()).getPreservedJobList();
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
 
         ((WorkTableModel)workTable.getModel()).setCurrentJobList(filteredJobList);
     }
 
     private void initComponentsFromDB() throws Exception
     {
         initCustomerCombo();
         ((WorkTableModel)workTable.getModel()).setPreservedJobList
                 (DBManager.getSingleton().getJobsByDates(fromDatePicker.getDate(), toDatePicker.getDate()));
     }
 
     private void initCustomerCombo() throws Exception
     {
         List<Customer> customers = DBManager.getSingleton().getCustomers();
         customers.add(0, Customer.ALL_VALUES);
         customerCombo.setModel(new DefaultComboBoxModel(customers.toArray()));
     }
 
     private class WorkTableModel extends AbstractTableModel
     {
         private static final int DATE_COL = 0;
         private static final int CUSTOMER_COL = 1;
         private static final int JOBS_DESCR_COL = 2;
         private static final int PRICE_COL = 3;
         private static final int REMARKS_COL = 4;
 
         private final String[] COLUMN_NAMES = new String[]{"תאריך", "לקוח", "תיאור עבודה שנעשתה", "מחיר", "הערות"};
 
         private List<Job> currentJobList = new ArrayList<Job>();
         private List<Job> preservedJobList = new ArrayList<Job>();
 
         private WorkTableModel()
         {
         }
 
         private List<Job> getPreservedJobList()
         {
             return preservedJobList;
         }
 
         private void setPreservedJobList(List<Job> preservedJobList)
         {
             this.preservedJobList = preservedJobList;
             this.currentJobList = new ArrayList<Job>(preservedJobList);
             this.fireTableDataChanged();
         }
 
         public void setCurrentJobList(List<Job> currentJobList)
         {
             this.currentJobList = currentJobList;
             this.fireTableDataChanged();
         }
 
         private List<Job> getCurrentJobList()
         {
             return currentJobList;
         }
 
         @Override
         public String getColumnName(int column)
         {
             return COLUMN_NAMES[column];
         }
 
         @Override
         public int getRowCount()
         {
             return currentJobList.size();
         }
 
         @Override
         public int getColumnCount()
         {
             return COLUMN_NAMES.length;
         }
 
         @Override
         public Object getValueAt(int rowIndex, int columnIndex)
         {
             Job job = currentJobList.get(rowIndex);
 
             switch (columnIndex)
             {
                 case DATE_COL: return job.getJobDate();
                 case CUSTOMER_COL: return job.getCustomer();
                 case JOBS_DESCR_COL: return job.getJobDescription();
                 case PRICE_COL: return job.getPrice();
                 case REMARKS_COL: return job.getRemarks();
                 default: return null;
             }
         }
     }
 
     private class WorkTableRenderer extends DefaultTableRenderer
     {
         @Override
         public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
         {
             int realColumn = table.convertColumnIndexToModel(column);
 
             if (realColumn == WorkTableModel.DATE_COL)
             {
                 value = DateFormat.getDateInstance().format(value);
             }
             else if (realColumn == WorkTableModel.PRICE_COL)
             {
                 value = NumberFormat.getCurrencyInstance().format(value);
             }
 
             Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
 
             int realRow = table.getRowSorter().convertRowIndexToModel(row);
 
             if (((WorkTableModel)table.getModel()).getCurrentJobList().get(realRow).isNewRecord())
             {
                 component.setBackground(isSelected ? NEW_RECORD_SELECTED_COLOR : NEW_RECORD_COLOR);
             }
             else if (((WorkTableModel)table.getModel()).getCurrentJobList().get(realRow).isUpdated())
             {
                 component.setBackground(isSelected ? UPDATED_RECORD_SELECTED_COLOR : UPDATED_RECORD_COLOR);
             }
 
             component.setFont(DEFAULT_TEXT_FONT);
 
             return component;
         }
     }
 
     public static void main(String args[])
     {
         new WorkLogScr();
     }
 }
