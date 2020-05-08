 /**
  * Created on Jun 25, 2008
  */
 package bias.extension.Reminder;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dialog.ModalityType;
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.UUID;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollBar;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JSplitPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.JToolBar;
 import javax.swing.ListSelectionModel;
 import javax.swing.RowFilter;
 import javax.swing.RowSorter.SortKey;
 import javax.swing.SortOrder;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SwingUtilities;
 import javax.swing.border.LineBorder;
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.RowSorterEvent;
 import javax.swing.event.RowSorterListener;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableModel;
 import javax.swing.table.TableRowSorter;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 
 import bias.Constants;
 import bias.core.Attachment;
 import bias.core.BackEnd;
 import bias.extension.ToolExtension;
 import bias.extension.ToolRepresentation;
 import bias.extension.Reminder.editor.HTMLEditorPanel;
 import bias.extension.Reminder.xmlb.Entries;
 import bias.extension.Reminder.xmlb.Entry;
 import bias.extension.Reminder.xmlb.ObjectFactory;
 import bias.gui.CustomHTMLEditorKit;
 import bias.gui.FrontEnd;
 import bias.utils.AppManager;
 import bias.utils.CommonUtils;
 import bias.utils.PropertiesUtils;
 import bias.utils.Validator;
 
 import com.toedter.calendar.JDateChooser;
 
 /**
  * @author kion
  */
 public class Reminder extends ToolExtension {
    private static final long serialVersionUID = 1L;
     
     // TODO [P2] column widths should be stored as relative (% of whole table width) values
     
     private static final ImageIcon ICON = new ImageIcon(CommonUtils.getResourceURL(Reminder.class, "icon.png"));
     private static final ImageIcon ICON_ADD = new ImageIcon(CommonUtils.getResourceURL(Reminder.class, "add.png"));
     private static final ImageIcon ICON_DELETE = new ImageIcon(CommonUtils.getResourceURL(Reminder.class, "del.png"));
     
     private static final String SEPARATOR = "/";
     
     private static final String PERIODIC_DATE_DAILY = "Daily";
     private static final String PERIODIC_DATE_DAILY_WEEKDAYS_ONLY = PERIODIC_DATE_DAILY + ".WeekDaysOnly";
     private static final String PERIODIC_DATE_DAILY_WEEKEND_ONLY =  PERIODIC_DATE_DAILY + ".WeekEndOnly";
     private static final String PERIODIC_DATE_WEEKLY = "Weekly";
     private static final String PERIODIC_DATE_MONTHLY = "Monthly";
     private static final String PERIODIC_DATE_QUARTERLY = "Quarterly";
     private static final String PERIODIC_DATE_YEARLY = "Yearly";
 
     private String[] PERIODIC_DATE_VALUES = new String[] { 
         PERIODIC_DATE_DAILY, 
         PERIODIC_DATE_DAILY_WEEKDAYS_ONLY, 
         PERIODIC_DATE_DAILY_WEEKEND_ONLY, 
         PERIODIC_DATE_WEEKLY, 
         PERIODIC_DATE_MONTHLY, 
         PERIODIC_DATE_QUARTERLY, 
         PERIODIC_DATE_YEARLY
     };
     
     private Map<Integer, String> DAYS = buildDays();
     
     private Map<Integer, String> buildDays() {
         Map<Integer, String> map = new HashMap<Integer, String>();
         map.put(1, getMessage("Sunday"));
         map.put(2, getMessage("Monday"));
         map.put(3, getMessage("Tuesday"));
         map.put(4, getMessage("Wednesday"));
         map.put(5, getMessage("Thursday"));
         map.put(6, getMessage("Friday"));
         map.put(7, getMessage("Saturday"));
         return map;
     }
     
     private Map<Integer, String> MONTHES = buildMonthes();
     
     private Map<Integer, String> buildMonthes() {
         Map<Integer, String> map = new HashMap<Integer, String>();
         map.put(1, getMessage("January"));
         map.put(2, getMessage("February"));
         map.put(3, getMessage("March"));
         map.put(4, getMessage("April"));
         map.put(5, getMessage("May"));
         map.put(6, getMessage("June"));
         map.put(7, getMessage("July"));
         map.put(8, getMessage("August"));
         map.put(9, getMessage("September"));
         map.put(10, getMessage("October"));
         map.put(11, getMessage("November"));
         map.put(12, getMessage("December"));
         return map;
     }
     
     private Map<TimeUnit, String> PERIODS = buildPeriods();
     
     private Map<TimeUnit, String> buildPeriods() {
         Map<TimeUnit, String> map = new HashMap<TimeUnit, String>();
         map.put(TimeUnit.MINUTES, getMessage("Minutes"));
         map.put(TimeUnit.HOURS, getMessage("Hours"));
         map.put(TimeUnit.DAYS, getMessage("Days"));
         return map;
     }
     
     private Map<String, String> PERIODIC_DATE_LABELS = buildPeriodicDateLabels();
     
     private Map<String, String> buildPeriodicDateLabels() {
         Map<String, String> map = new HashMap<String, String>();
         map.put(PERIODIC_DATE_WEEKLY, getMessage("tooltip.weekly"));
         map.put(PERIODIC_DATE_MONTHLY, getMessage("tooltip.monthly"));
         map.put(PERIODIC_DATE_QUARTERLY, getMessage("tooltip.quarterly"));
         map.put(PERIODIC_DATE_YEARLY, getMessage("tooltip.yearly"));
         return map;
     }
     
     private static Calendar currCal = new GregorianCalendar();
     static {
         currCal.setTime(new Date());
     }
     
     private static final String PROPERTY_SORT_BY_COLUMN = "SORT_BY_COLUMN";
     private static final String PROPERTY_SORT_ORDER = "SORT_BY_ORDER";
     private static final String PROPERTY_SELECTED_ROW = "SELECTED_ROW";
     private static final String PROPERTY_DIVIDER_LOCATION = "DIVIDER_LOCATION";
     private static final String PROPERTY_COLUMNS_WIDTHS = "COLUMNS_WIDTHS";
     private static final String PROPERTY_SCROLLBAR_VERT = "SCROLLBAR_VERT";
     private static final String PROPERTY_SCROLLBAR_HORIZ = "SCROLLBAR_HORIZ";
     private static final String PROPERTY_CARET_POSITION = "CARET_POSITION";
     
     private static final int MAX_SORT_KEYS_NUMBER = 3;
     
     private static final String SCHEMA_LOCATION = "http://bias.sourceforge.net/addons/ReminderSchema.xsd";
 
     private int[] sortByColumn = new int[MAX_SORT_KEYS_NUMBER];
     
     private SortOrder[] sortOrder = new SortOrder[MAX_SORT_KEYS_NUMBER];
     
     private static final SimpleDateFormat DATE_FORMAT_LONG = new SimpleDateFormat("yyyy-MM-dd");
     
     private static final SimpleDateFormat DATE_FORMAT_SHORT = new SimpleDateFormat("MM-dd");
     
     private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd.MM.yyyy @ HH:mm:ss");
 
     private Map<UUID, HTMLEditorPanel> editorPanels = new HashMap<UUID, HTMLEditorPanel>();
 
     private JPanel mainPanel = null;
     
     private JTable reminderEntriesTable = null;
     
     private JSplitPane splitPane = null;
     
     private TableRowSorter<TableModel> sorter;
     
     private Properties props;
     
     private byte[] data;
     
     private Map<UUID, ScheduledExecutorService> tasks;
     
     private Map<UUID, String> checkmarkDates;
     
     private static JAXBContext context;
 
     private static Unmarshaller unmarshaller;
 
     private static Marshaller marshaller;
 
     private static ObjectFactory objFactory = new ObjectFactory();
 
     public Reminder(UUID id, byte[] data, byte[] settings) throws Throwable {
         super(id, data, settings);
         initialize();
         mainPanel.revalidate();
     }
     
     private static JAXBContext getContext() throws JAXBException {
         if (context == null) {
             context = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
         }
         return context;
     }
 
     private static Unmarshaller getUnmarshaller() throws JAXBException {
         if (unmarshaller == null) {
             unmarshaller = getContext().createUnmarshaller();
         }
         return unmarshaller;
     }
 
     private static Marshaller getMarshaller() throws JAXBException {
         if (marshaller == null) {
             marshaller = getContext().createMarshaller();
             marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, SCHEMA_LOCATION);
             marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
         }
         return marshaller;
     }
     
     private void initialize() throws Throwable {
         applySettings(getSettings());
         initGUI();
         parseData();
         String selRow = props.getProperty(PROPERTY_SELECTED_ROW);
         if (!Validator.isNullOrBlank(selRow) && reminderEntriesTable.getRowCount() > 0 && reminderEntriesTable.getRowCount() > Integer.valueOf(selRow)) {
             reminderEntriesTable.setRowSelectionInterval(Integer.valueOf(selRow), Integer.valueOf(selRow));
         }
         String divLoc = props.getProperty(PROPERTY_DIVIDER_LOCATION);
         if (!Validator.isNullOrBlank(divLoc)) {
             splitPane.setDividerLocation(Integer.valueOf(divLoc));
         }
         String colW = props.getProperty(PROPERTY_COLUMNS_WIDTHS);
         if (!Validator.isNullOrBlank(colW)) {
             String[] colsWs = colW.split(":");
             int cc = reminderEntriesTable.getColumnModel().getColumnCount();
             for (int i = 0; i < cc; i++) {
                 reminderEntriesTable.getColumnModel().getColumn(i).setPreferredWidth(Integer.valueOf(colsWs[i]));
             }
         }
         if (splitPane.getBottomComponent() != null) {
             HTMLEditorPanel htmlEditorPanel = ((HTMLEditorPanel) splitPane.getBottomComponent());
             JScrollPane sc = ((JScrollPane) htmlEditorPanel.getComponent(0));
             JScrollBar sb = sc.getVerticalScrollBar();
             if (sb != null) {
                 String val = props.getProperty(PROPERTY_SCROLLBAR_VERT);
                 if (val != null) {
                     sb.setVisibleAmount(0);
                     sb.setValue(sb.getMaximum());
                     sb.setValue(Integer.valueOf(val));
                 }
             }
             sb = sc.getHorizontalScrollBar();
             if (sb != null) {
                 String val = props.getProperty(PROPERTY_SCROLLBAR_HORIZ);
                 if (val != null) {
                     sb.setVisibleAmount(0);
                     sb.setValue(sb.getMaximum());
                     sb.setValue(Integer.valueOf(val));
                 }
             }
             String caretPos = props.getProperty(PROPERTY_CARET_POSITION);
             if (!Validator.isNullOrBlank(caretPos)) {
                 htmlEditorPanel.setCaretPosition(Integer.valueOf(caretPos));
             }
         }
     }
     
     public void applySettings(byte[] settings) {
         props = PropertiesUtils.deserializeProperties(settings);
         for (int i = 0; i < MAX_SORT_KEYS_NUMBER; i++) {
             int sortByColumn = -1;
             String sortByColumnStr = props.getProperty(PROPERTY_SORT_BY_COLUMN + i);
             if (!Validator.isNullOrBlank(sortByColumnStr)) {
                 sortByColumn = Integer.valueOf(sortByColumnStr);
             }
             this.sortByColumn[i] = sortByColumn;
             SortOrder sortOrder = null;
             String sortOrderStr = props.getProperty(PROPERTY_SORT_ORDER + i);
             if (!Validator.isNullOrBlank(sortOrderStr)) {
                 sortOrder = SortOrder.valueOf(sortOrderStr);
             }
             this.sortOrder[i] = sortOrder;
         }
     }
     
     private void parseData() throws Throwable {
         try {
             if (getData() != null && !Arrays.equals(getData(), data)) {
                 data = getData();
                 Collection<ReminderEntry> newEntries = new LinkedList<ReminderEntry>();
                 if (data.length != 0) {
                     Entries entries = (Entries) getUnmarshaller().unmarshal(new ByteArrayInputStream(getData()));
                     for (Entry e : entries.getEntry()) {
                         ReminderEntry reminderEntry = new ReminderEntry();
                         reminderEntry.setId(UUID.fromString(e.getId()));
                         reminderEntry.setTitle(e.getTitle());
                         reminderEntry.setDescription(e.getDescription());
                         reminderEntry.setDate(e.getDate());
                         reminderEntry.setCheckmarkDate(e.getCheckmarkDate());
                         reminderEntry.setTime(e.getTime());
                         newEntries.add(reminderEntry);
                     }
                 }
                 Collection<ReminderEntry> existingEntries = getReminderEntries();
                 for (ReminderEntry re : existingEntries) {
                     if (!newEntries.contains(re)) {
                         int idx = findRowIdxByID(re.getId().toString());
                         if (idx != -1) {
                             ((DefaultTableModel) reminderEntriesTable.getModel()).removeRow(idx);
                             cleanUpUnUsedAttachments(re.getId());
                             tasks.remove(re.getId());
                         }
                     }
                 }
                 for (ReminderEntry re : newEntries) {
                     if (!existingEntries.contains(re)) {
                         addReminderEntry(re);
                     }
                 }
             }
         } catch (Throwable t) {
             FrontEnd.displayErrorMessage("Failed to parse reminder-list XML data file!", t);
             throw t;
         }
     }
     
     private void initGUI() throws Throwable {
         try {
             if (mainPanel == null) {
                 mainPanel = new JPanel(new BorderLayout());
                 JToolBar toolbar = initToolBar();
                 DefaultTableModel model = new DefaultTableModel() {
                     private static final long serialVersionUID = 1L;
                     public boolean isCellEditable(int rowIndex, int mColIndex) {
                         if (mColIndex == 1) return true;
                         return false;
                     }
                 };
                 reminderEntriesTable = new JTable(model);
                 reminderEntriesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                 
                 model.addColumn("ID");
                 model.addColumn(getMessage("Title"));
                 model.addColumn(getMessage("Date"));
                 model.addColumn(getMessage("Time"));
                 
                 reminderEntriesTable.getColumnModel().getColumn(2).setCellRenderer(new DateCellRenderer());
 
                 // hide ID column
                 TableColumn idCol = reminderEntriesTable.getColumnModel().getColumn(0);
                 reminderEntriesTable.getColumnModel().removeColumn(idCol);
                 
                 sorter = new TableRowSorter<TableModel>(model);
                 sorter.setSortsOnUpdates(true);
                 sorter.setMaxSortKeys(MAX_SORT_KEYS_NUMBER);
                 List<SortKey> sortKeys = new LinkedList<SortKey>();
                 for (int i = 0; i < MAX_SORT_KEYS_NUMBER; i++) {
                     if (sortByColumn[i] != -1 && sortOrder[i] != null) {
                         SortKey sortKey = new SortKey(sortByColumn[i], sortOrder[i]);
                         sortKeys.add(sortKey);
                     }
                 }
                 sorter.setSortKeys(sortKeys);
                 sorter.addRowSorterListener(new RowSorterListener(){
                     public void sorterChanged(RowSorterEvent e) {
                         if (e.getType().equals(RowSorterEvent.Type.SORT_ORDER_CHANGED)) {
                             List<? extends SortKey> sortKeys = sorter.getSortKeys();
                             for (int i = 0; i < MAX_SORT_KEYS_NUMBER; i++) {
                                 if (i < sortKeys.size()) {
                                     SortKey sortKey = sortKeys.get(i);
                                     sortByColumn[i] = sortKey.getColumn();
                                     sortOrder[i] = sortKey.getSortOrder();
                                 } else {
                                     sortByColumn[i] = -1;
                                     sortOrder[i] = null;
                                 }
                             }
                         }
                     }
                 });
                 reminderEntriesTable.setRowSorter(sorter);
                 
                 JPanel entriesPanel = new JPanel(new BorderLayout());
                 splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
                 splitPane.setDividerSize(3);
                 splitPane.setTopComponent(new JScrollPane(reminderEntriesTable));
                 entriesPanel.add(splitPane, BorderLayout.CENTER);
                 
                 reminderEntriesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
                     public void valueChanged(ListSelectionEvent e) {
                         if (!e.getValueIsAdjusting()) {
                             DefaultTableModel model = (DefaultTableModel) reminderEntriesTable.getModel();
                             int rn = reminderEntriesTable.getSelectedRow();
                             if (rn == -1) {
                                 splitPane.setBottomComponent(null);
                             } else {
                                 int dl = -1;
                                 if (splitPane.getBottomComponent() != null) {
                                     dl = splitPane.getDividerLocation();
                                 }
                                 rn = reminderEntriesTable.convertRowIndexToModel(rn);
                                 UUID id = UUID.fromString((String) model.getValueAt(rn, 0));
                                 splitPane.setBottomComponent(editorPanels.get(id));
                                 if (dl != -1) {
                                     splitPane.setDividerLocation(dl);
                                 } else {
                                     splitPane.setDividerLocation(0.5);
                                 }
                             }
                         }
                     }
                 });
 
                 mainPanel.add(entriesPanel, BorderLayout.CENTER);
                 final JTextField filterText = new JTextField();
                 filterText.addCaretListener(new CaretListener(){
                     @SuppressWarnings("unchecked")
                     public void caretUpdate(CaretEvent e) {
                         TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) reminderEntriesTable.getRowSorter();
                         sorter.setRowFilter(RowFilter.regexFilter("(?i)" + filterText.getText()));
                     }
                 });
                 JPanel filterPanel = new JPanel(new BorderLayout());
                 filterPanel.add(new JLabel(getMessage("Filter")), BorderLayout.WEST);
                 filterPanel.add(filterText, BorderLayout.CENTER);
                 mainPanel.add(filterPanel, BorderLayout.NORTH);
                 mainPanel.add(toolbar, BorderLayout.SOUTH);
             }
         } catch (Throwable t) {
             FrontEnd.displayErrorMessage("Failed to initialize GUI!", t);
             throw t;
         }
     }
     
     private JToolBar initToolBar() {
         JToolBar toolbar = new JToolBar();
         toolbar.setFloatable(false);
         JButton buttAdd = new JButton(ICON_ADD);
         buttAdd.setToolTipText(getMessage("Add.Reminder"));
         buttAdd.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e) {
                 JPanel p = new JPanel(new GridLayout(8, 1));
                 p.add(new JLabel(getMessage("Date")));
                 final JDateChooser dateChooser = new JDateChooser(new Date());
                 p.add(dateChooser);
                 final JCheckBox periodicCB = new JCheckBox(getMessage("Periodic"));
                 p.add(periodicCB);
                 final JComboBox perCB = new JComboBox(PERIODIC_DATE_VALUES);
                 perCB.setRenderer(new DefaultListCellRenderer(){
                     private static final long serialVersionUID = 1L;
                     @Override
                     public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                         String dValue = getMessage((String) value);
                         return super.getListCellRendererComponent(list, dValue, index, isSelected, cellHasFocus);
                     }
                 });
                 perCB.setEnabled(false);
                 p.add(perCB);
                 final SpinnerNumberModel perSM = new SpinnerNumberModel();
                 perSM.setMinimum(1);
                 perSM.setMaximum(7);
                 perSM.setStepSize(1);
                 perSM.setValue(1);
                 final JLabel dayL = new JLabel();
                 dayL.setVisible(false);
                 final JSpinner perSpinner = new JSpinner(perSM);
                 perSpinner.addChangeListener(new ChangeListener(){
                     public void stateChanged(ChangeEvent e) {
                         if (perCB.getSelectedItem().equals(PERIODIC_DATE_WEEKLY)) {
                             dayL.setText(DAYS.get(((Number) perSpinner.getValue()).intValue()));
                         }
                     }
                 });
                 perSpinner.setEnabled(false);
                 JPanel perP = new JPanel(new GridLayout(1, 2));
                 perP.add(dayL);
                 perP.add(perSpinner);
                 p.add(perP);
                 periodicCB.addChangeListener(new ChangeListener(){
                     public void stateChanged(ChangeEvent e) {
                         if (periodicCB.isSelected()) {
                             perCB.setEnabled(true);
                             if (perCB.getSelectedItem().equals(PERIODIC_DATE_YEARLY)) {
                                 dateChooser.setEnabled(true);
                                 perSpinner.setEnabled(false);
                                 dayL.setVisible(false);
                             } else if (perCB.getSelectedItem().equals(PERIODIC_DATE_DAILY)) {
                                 dateChooser.setEnabled(false);
                                 perSpinner.setEnabled(false);
                                 dayL.setVisible(false);
                             } else {
                                 dateChooser.setEnabled(false);
                                 perSpinner.setEnabled(true);
                                 if (perCB.getSelectedItem().equals(PERIODIC_DATE_WEEKLY)) {
                                     dayL.setVisible(true);
                                     dayL.setText(DAYS.get(((Number) perSpinner.getValue()).intValue()));
                                 }
                             }
                         } else {
                             perCB.setEnabled(false);
                             perSpinner.setEnabled(false);
                             dayL.setVisible(false);
                             dateChooser.setEnabled(true);
                         }
                     }
                 });
                 perCB.addItemListener(new ItemListener(){
                     public void itemStateChanged(ItemEvent e) {
                         if (perCB.getSelectedItem().equals(PERIODIC_DATE_YEARLY)) {
                             dateChooser.setEnabled(true);
                             perSpinner.setEnabled(false);
                             dayL.setVisible(false);
                         } else {
                             dateChooser.setEnabled(false);
                             if (perCB.getSelectedItem().equals(PERIODIC_DATE_WEEKLY)) {
                                 perSM.setMaximum(7);
                                 perSM.setValue(1);
                                 perSpinner.setEnabled(true);
                                 dayL.setVisible(true);
                                 dayL.setText(DAYS.get(((Number) perSpinner.getValue()).intValue()));
                             } else if (perCB.getSelectedItem().equals(PERIODIC_DATE_MONTHLY) || perCB.getSelectedItem().equals(PERIODIC_DATE_QUARTERLY)) {
                                 perSM.setMaximum(31);
                                 perSM.setValue(1);
                                 perSpinner.setEnabled(true);
                                 dayL.setVisible(false);
                             } else {
                                 perSpinner.setEnabled(false);
                                 dayL.setVisible(false);
                             }
                         }
                         perCB.setToolTipText(PERIODIC_DATE_LABELS.get(perCB.getSelectedItem()));
                     }
                 });
                 Calendar currCal = new GregorianCalendar();
                 currCal.setTime(new Date());
                 p.add(new JLabel(getMessage("Time")));
                 final JPanel timeP = new JPanel(new GridLayout(1, 2));
                 SpinnerNumberModel sm = new SpinnerNumberModel();
                 sm.setMinimum(0);
                 sm.setMaximum(23);
                 sm.setStepSize(1);
                 sm.setValue(currCal.get(Calendar.HOUR_OF_DAY));
                 final JSpinner hour = new JSpinner(sm);
                 SpinnerNumberModel sm2 = new SpinnerNumberModel();
                 sm2.setMinimum(0);
                 sm2.setMaximum(59);
                 sm2.setStepSize(1);
                 sm2.setValue(currCal.get(Calendar.MINUTE));
                 final JSpinner minute = new JSpinner(sm2);
                 timeP.add(hour);
                 timeP.add(minute);
                 p.add(timeP);
                 p.add(new JLabel(getMessage("Title")));
                 String title = JOptionPane.showInputDialog(
                         mainPanel, 
                         new Component[]{p},
                         getMessage("New.Reminder"),
                         JOptionPane.PLAIN_MESSAGE
                         );
                 if (!Validator.isNullOrBlank(title)) {
                     ReminderEntry reminderEntry = new ReminderEntry();
                     reminderEntry.setId(UUID.randomUUID());
                     reminderEntry.setDate(dateChooser.isEnabled() ? 
                                 (periodicCB.isSelected() ? PERIODIC_DATE_YEARLY + SEPARATOR : Constants.EMPTY_STR) + (periodicCB.isSelected() ? DATE_FORMAT_SHORT.format(dateChooser.getDate()) : DATE_FORMAT_LONG.format(dateChooser.getDate())) : 
                                 (String) perCB.getSelectedItem() + (perSpinner.isEnabled() ? SEPARATOR + perSpinner.getValue().toString() : Constants.EMPTY_STR));
                     reminderEntry.setTime(formatTime(hour.getValue().toString(), minute.getValue().toString()));
                     reminderEntry.setTitle(title);
                     reminderEntry.setDescription(Constants.EMPTY_STR);
                     addReminderEntry(reminderEntry);
                 }
             }
         });
         JButton buttDel = new JButton(ICON_DELETE);
         buttDel.setToolTipText(getMessage("Delete.Reminder"));
         buttDel.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e) {
                 DefaultTableModel model = (DefaultTableModel) reminderEntriesTable.getModel();
                 if (reminderEntriesTable.getSelectedRow() != -1) {
                     int idx = sorter.convertRowIndexToModel(reminderEntriesTable.getSelectedRow());
                     UUID id = UUID.fromString((String) model.getValueAt(idx, 0));
                     model.removeRow(idx);
                     cleanUpUnUsedAttachments(id);
                     ScheduledExecutorService task = tasks.get(id);
                     if (task != null) {
                         task.shutdownNow();
                         tasks.remove(id);
                     }
                 }
             }
         });
         toolbar.add(buttAdd);
         toolbar.add(buttDel);
         return toolbar;
     }
     
     private class DateCellRenderer extends DefaultTableCellRenderer {
         private static final long serialVersionUID = 1L;
         public DateCellRenderer() {
             super();
         }
         public void setValue(Object value) {
             if ((value != null)) {
                 String v = ((String) value);
                 if (v.contains(SEPARATOR)) {
                     String[] vv = v.split(SEPARATOR);
                     String s = vv[0];
                     if (s.equals(PERIODIC_DATE_WEEKLY)) {
                         Integer day = Integer.valueOf(vv[1]);
                         value = getMessage(PERIODIC_DATE_WEEKLY) + SEPARATOR + DAYS.get(day);
                     } else if (s.equals(PERIODIC_DATE_MONTHLY) || s.equals(PERIODIC_DATE_QUARTERLY)) {
                         value = getMessage(s) + SEPARATOR + vv[1];
                     } else if (s.equals(PERIODIC_DATE_YEARLY)) {
                         String[] vvv = vv[1].split("-");
                         Integer month = Integer.valueOf(vvv[0]);
                         value = getMessage(s) + SEPARATOR + MONTHES.get(month) + SEPARATOR + vvv[1];
                     }
                 } else if (v.equals(PERIODIC_DATE_DAILY)) {
                     value = getMessage(PERIODIC_DATE_DAILY);
                 } else if (v.equals(PERIODIC_DATE_DAILY_WEEKDAYS_ONLY)) {
                     value = getMessage(PERIODIC_DATE_DAILY_WEEKDAYS_ONLY);
                 } else if (v.equals(PERIODIC_DATE_DAILY_WEEKEND_ONLY)) {
                     value = getMessage(PERIODIC_DATE_DAILY_WEEKEND_ONLY);
                 } else {
                     String[] vv = v.split("-");
                     Integer month = Integer.valueOf(vv[1]);
                     value = vv[0] + SEPARATOR + MONTHES.get(month) + SEPARATOR + vv[2];
                 }
             }
             super.setValue(value);
         }
     }
 
     private int findRowIdxByID(String id) {
         DefaultTableModel model = (DefaultTableModel) reminderEntriesTable.getModel();
         for (int i = 0; i < model.getRowCount(); i++) {
             if (id.equals(model.getValueAt(i, 0))) {
                 return i;
             }
         }
         return -1;
     }
     
     private String formatTime(String hour, String minute) {
         return (hour.length() == 1 ? "0" + hour : hour) + ":" + (minute.length() == 1 ? "0" + minute : minute);
     }
     
     private void addReminderEntry(final ReminderEntry reminderEntry) {
         DefaultTableModel model = (DefaultTableModel) reminderEntriesTable.getModel();
         int idx = reminderEntriesTable.getSelectedRow();
         if (idx != -1) {
             reminderEntriesTable.getSelectionModel().removeSelectionInterval(0, idx);
         }
         splitPane.setBottomComponent(null);
         reminderEntriesTable.setVisible(false);
         model.addRow(new Object[]{
                 reminderEntry.getId().toString(),
                 reminderEntry.getTitle(),
                 reminderEntry.getDate(), 
                 reminderEntry.getTime()
                 });
         if (reminderEntry.getId() != null && reminderEntry.getDescription() != null) {
             editorPanels.put(reminderEntry.getId(), new HTMLEditorPanel(getId(), reminderEntry.getDescription()));
         }
         reminderEntriesTable.setVisible(true);
         if (tasks == null) {
             tasks = new HashMap<UUID, ScheduledExecutorService>();
         }
         if (checkmarkDates == null) {
         	checkmarkDates = new HashMap<UUID, String>();
         }
         Date currDate = new Date();
         Long delay = null;
         boolean periodic;
         int calendarField = -1;
         Calendar cal = new GregorianCalendar();
         Date checkmarkDate;
         boolean displayNow = false;
         try {
         	checkmarkDate = Validator.isNullOrBlank(reminderEntry.getCheckmarkDate()) ? null : DATE_TIME_FORMAT.parse(reminderEntry.getCheckmarkDate());
         	if (checkmarkDate != null) {
     			checkmarkDates.put(reminderEntry.getId(), DATE_TIME_FORMAT.format(checkmarkDate));
         	}
             if (reminderEntry.getDate().startsWith(PERIODIC_DATE_DAILY) 
                     || reminderEntry.getDate().startsWith(PERIODIC_DATE_YEARLY) 
                     || reminderEntry.getDate().startsWith(PERIODIC_DATE_WEEKLY)
                     || reminderEntry.getDate().startsWith(PERIODIC_DATE_MONTHLY)
                     || reminderEntry.getDate().startsWith(PERIODIC_DATE_QUARTERLY)) {
                 periodic = true;
                 if (reminderEntry.getDate().startsWith(PERIODIC_DATE_DAILY)) {
                     String[] time = reminderEntry.getTime().split(":");
                     Integer hour = Integer.valueOf(time[0]);
                     Integer minute = Integer.valueOf(time[1]);
                     cal.setTime(currDate);
                     cal.set(Calendar.HOUR_OF_DAY, hour);
                     cal.set(Calendar.MINUTE, minute);
                     cal.set(Calendar.SECOND, 0);
                     delay = cal.getTimeInMillis() - currDate.getTime();
                     if (delay < 0) { 
                         if (checkmarkDate != null) {
                             Calendar checkmarkCal = new GregorianCalendar();
                         	checkmarkCal.setTime(checkmarkDate);
                         	displayNow = checkmarkCal.get(Calendar.DAY_OF_YEAR) != cal.get(Calendar.DAY_OF_YEAR);
                         } else {
                         	displayNow = true;
                         }
                         cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 1);
                         delay = cal.getTimeInMillis() - currDate.getTime();
                     }
                     calendarField = Calendar.DAY_OF_YEAR;
                 } else if (reminderEntry.getDate().startsWith(PERIODIC_DATE_WEEKLY)) {
                     Integer day = Integer.valueOf(reminderEntry.getDate().split(SEPARATOR)[1]);
                     String[] time = reminderEntry.getTime().split(":");
                     Integer hour = Integer.valueOf(time[0]);
                     Integer minute = Integer.valueOf(time[1]);
                     cal.setTime(currDate);
                     cal.set(Calendar.DAY_OF_WEEK, day);
                     cal.set(Calendar.HOUR_OF_DAY, hour);
                     cal.set(Calendar.MINUTE, minute);
                     cal.set(Calendar.SECOND, 0);
                     delay = cal.getTimeInMillis() - currDate.getTime();
                     if (delay < 0) { 
                         if (checkmarkDate != null) {
                             Calendar checkmarkCal = new GregorianCalendar();
                         	checkmarkCal.setTime(checkmarkDate);
                         	displayNow = checkmarkCal.get(Calendar.WEEK_OF_YEAR) != cal.get(Calendar.WEEK_OF_YEAR);
                         } else {
                         	displayNow = true;
                         }
                         cal.set(Calendar.WEEK_OF_YEAR, cal.get(Calendar.WEEK_OF_YEAR) + 1);
                         delay = cal.getTimeInMillis() - currDate.getTime();
                     }
                     calendarField = Calendar.WEEK_OF_YEAR;
                 } else if (reminderEntry.getDate().startsWith(PERIODIC_DATE_MONTHLY) || reminderEntry.getDate().startsWith(PERIODIC_DATE_QUARTERLY)) {
                     Integer day = Integer.valueOf(reminderEntry.getDate().split(SEPARATOR)[1]);
                     String[] time = reminderEntry.getTime().split(":");
                     Integer hour = Integer.valueOf(time[0]);
                     Integer minute = Integer.valueOf(time[1]);
                     cal.setTime(currDate);
                     cal.set(Calendar.DAY_OF_MONTH, day);
                     cal.set(Calendar.HOUR_OF_DAY, hour);
                     cal.set(Calendar.MINUTE, minute);
                     cal.set(Calendar.SECOND, 0);
                     boolean quarterly = reminderEntry.getDate().startsWith(PERIODIC_DATE_QUARTERLY);
                     if (quarterly) {
                     	int month = cal.get(Calendar.MONTH);
                     	if (month < 3) {
                     		month = 3;
                     	} else {
                     		int diff = month % 3;
                     		if (diff != 0) {
                             	month += 3 - diff;
                     		}
                     	}
                     	cal.set(Calendar.MONTH, month);
                     }
                     delay = cal.getTimeInMillis() - currDate.getTime();
                     if (delay < 0) { 
                         if (checkmarkDate != null) {
                             Calendar checkmarkCal = new GregorianCalendar();
                         	checkmarkCal.setTime(checkmarkDate);
                         	displayNow = checkmarkCal.get(Calendar.MONTH) != cal.get(Calendar.MONTH);
                         } else {
                         	displayNow = true;
                         }
                         cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + (quarterly ? 3 : 1));
                         delay = cal.getTimeInMillis() - currDate.getTime();
                     }
                     calendarField = Calendar.MONTH;
                 } else if (reminderEntry.getDate().startsWith(PERIODIC_DATE_YEARLY)) {
                     String dateStr = reminderEntry.getDate().split(SEPARATOR)[1];
                     Date date = DATE_FORMAT_SHORT.parse(dateStr);
                     String[] time = reminderEntry.getTime().split(":");
                     Integer hour = Integer.valueOf(time[0]);
                     Integer minute = Integer.valueOf(time[1]);
                     cal.setTime(date);
                     cal.set(Calendar.YEAR, currCal.get(Calendar.YEAR));
                     cal.set(Calendar.HOUR_OF_DAY, hour);
                     cal.set(Calendar.MINUTE, minute);
                     cal.set(Calendar.SECOND, 0);
                     delay = cal.getTimeInMillis() - currDate.getTime();
                     if (delay < 0) { 
                         if (checkmarkDate != null) {
                             Calendar checkmarkCal = new GregorianCalendar();
                         	checkmarkCal.setTime(checkmarkDate);
                         	displayNow = checkmarkCal.get(Calendar.YEAR) != cal.get(Calendar.YEAR);
                         } else {
                         	displayNow = true;
                         }
                         cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 1);
                         delay = cal.getTimeInMillis() - currDate.getTime();
                     }
                     calendarField = Calendar.YEAR;
                 }
             } else {
                 periodic = false;
                 try {
                     Date date = DATE_FORMAT_LONG.parse(reminderEntry.getDate());
                     String[] time = reminderEntry.getTime().split(":");
                     Integer hour = Integer.valueOf(time[0]);
                     Integer minute = Integer.valueOf(time[1]);
                     cal.setTime(date);
                     cal.set(Calendar.HOUR_OF_DAY, hour);
                     cal.set(Calendar.MINUTE, minute);
                     cal.set(Calendar.SECOND, 0);
                     delay = cal.getTimeInMillis() - currDate.getTime();
                     if (delay < 0) delay = 0L;
                 } catch (ParseException e) {
                     FrontEnd.displayErrorMessage("Failed to parse date for reminder entry " + reminderEntry.getTitle(), e);
                 }
             }
             if (delay != null) {
                 final long dl = displayNow ? 60000L : delay;
                 final boolean pd = periodic;
                 final int cf = calendarField;
     			Calendar now = new GregorianCalendar();
     			now.setTime(new Date());
     			while (cal.before(now)) {
     				cal.set(calendarField, cal.get(calendarField) + (reminderEntry.getDate().startsWith(PERIODIC_DATE_QUARTERLY) ? 3 : 1));
     			};
                 final Calendar initialDate = cal;
                 final ScheduledExecutorService taskExecutor = new ScheduledThreadPoolExecutor(1);
                 Runnable task = new Runnable(){
                     public void run() {
                         runTask(taskExecutor, reminderEntry, pd, cf, initialDate);
                     }
                 };
                 taskExecutor.schedule(task, dl, TimeUnit.MILLISECONDS);
                 tasks.put(reminderEntry.getId(), taskExecutor);
             }
         } catch (Exception e) {
             FrontEnd.displayErrorMessage("Failed to parse reminder entry '" + reminderEntry.getTitle() + "'", e);
         }
     }
     
     private void runTask(final ScheduledExecutorService taskExecutor, final ReminderEntry reminderEntry, final boolean periodic, final int calendarField, final Calendar initialDate) {
     	SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
             	Date currDate = new Date();
     			Calendar now = new GregorianCalendar();
     			now.setTime(currDate);
 				boolean postpone = false;
 				// if there was specified to display reminder on either weekdays or weekend only...
 				if (periodic && calendarField == Calendar.DAY_OF_YEAR && reminderEntry.getDate().startsWith(PERIODIC_DATE_DAILY)) {
 					int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
 					if (reminderEntry.getDate().equals(PERIODIC_DATE_DAILY_WEEKDAYS_ONLY)) {
 						if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
 							postpone = true;
 						}
 					} else if (reminderEntry.getDate().equals(PERIODIC_DATE_DAILY_WEEKEND_ONLY)) {
 						if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
 							postpone = true;
 						}
 					}
 				}
 				if (postpone) {
 					// ... and it's not a time to display it - postpone reminder by one day...
         			do {
 						initialDate.set(calendarField, initialDate.get(calendarField) + 1);
         			} while (initialDate.before(now));
                     Runnable r = new Runnable(){
                         public void run() {
                             runTask(taskExecutor, reminderEntry, periodic, calendarField, initialDate);
                         }
                     };
                     long currMS = currDate.getTime();
                     long delay = initialDate.getTimeInMillis() - currMS;
                     taskExecutor.schedule(r, delay, TimeUnit.MILLISECONDS);
 				} else {
 					// ... otherwise (no "weekdays/weekend only" restriction was specified) - display reminder
 			        final JDialog dialog = new JDialog(null, ModalityType.MODELESS);
 			        JTextPane detailsTextPane = new JTextPane();
 			        detailsTextPane.setEditable(false);
 			        detailsTextPane.setEditorKit(new CustomHTMLEditorKit());
 			        detailsTextPane.addHyperlinkListener(new HyperlinkListener() {
 			            public void hyperlinkUpdate(HyperlinkEvent e) {
 			                if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
 			                    if (e.getDescription().startsWith(Constants.ENTRY_PROTOCOL_PREFIX)) {
 			                        String idStr = e.getDescription().substring(Constants.ENTRY_PROTOCOL_PREFIX.length());
 			                        dialog.setVisible(false);
 			                        FrontEnd.restoreMainWindow();
 			                        FrontEnd.switchToVisualEntry(UUID.fromString(idStr));
 			                        dialog.setVisible(true);
 			                    } else {
 			                        try {
 			                            AppManager.getInstance().handleAddress(e.getDescription());
 			                        } catch (Exception ex) {
 			                            FrontEnd.displayErrorMessage(ex);
 			                        }
 			                    }
 			                }
 			            }
 			        });
 			        JScrollPane detailsPane = new JScrollPane(detailsTextPane);
 			        detailsTextPane.setText(editorPanels.get(reminderEntry.getId()).getUnparsedCode());
 			        JPanel p = new JPanel(new BorderLayout());
 			        p.add(new JLabel(reminderEntry.getTitle()), BorderLayout.NORTH);
 			        p.add(detailsPane, BorderLayout.CENTER);
 			        JPanel pControls = new JPanel(new GridLayout(2, 1));
 			        final int idx = findRowIdxByID(reminderEntry.getId().toString()); // check if reminder entry is still in the list
 			        JButton doneButt = new JButton(getMessage("Done") + " (" + ((periodic && idx != -1) ? getMessage("remind.next.time") : getMessage("delete.this.reminder")) + ")");
 			        doneButt.addActionListener(new ActionListener(){
 			            public void actionPerformed(ActionEvent e) {
 			                dialog.setVisible(false);
 			                if (idx != -1) {
 			                    if (periodic) {
 			                    	Date currDate = new Date();
 			            			checkmarkDates.put(reminderEntry.getId(), DATE_TIME_FORMAT.format(currDate));
 			            			Calendar now = new GregorianCalendar();
 			            			now.setTime(currDate);
 			            			do {
 			            				initialDate.set(calendarField, initialDate.get(calendarField) + (reminderEntry.getDate().startsWith(PERIODIC_DATE_QUARTERLY) ? 3 : 1));
 			            			} while (initialDate.before(now));
 			                        Runnable r = new Runnable(){
 			                            public void run() {
 			                                runTask(taskExecutor, reminderEntry, periodic, calendarField, initialDate);
 			                            }
 			                        };
 			                        long currMS = currDate.getTime();
 			                        long delay = initialDate.getTimeInMillis() - currMS;
 			                        taskExecutor.schedule(r, delay, TimeUnit.MILLISECONDS);
 			                    } else {
 			                        ((DefaultTableModel) reminderEntriesTable.getModel()).removeRow(idx);
 			                        cleanUpUnUsedAttachments(reminderEntry.getId());
 			                        tasks.remove(reminderEntry.getId());
 			                    }
 			                }
 			            }
 			        });
 			        pControls.add(doneButt);
 			        JPanel remPanel = new JPanel(new GridLayout(1, 3));
 			        final SpinnerNumberModel sm = new SpinnerNumberModel();
 			        sm.setMinimum(1);
 			        sm.setStepSize(1);
 			        sm.setValue(5);
 			        final JSpinner remSpinner = new JSpinner(sm);
 			        final JComboBox remComboBox = new JComboBox();
 			        remComboBox.addItem(TimeUnit.MINUTES);
 			        remComboBox.addItem(TimeUnit.HOURS);
 			        remComboBox.addItem(TimeUnit.DAYS);
 			        remComboBox.setRenderer(new DefaultListCellRenderer(){
 			            private static final long serialVersionUID = 1L;
 			            @Override
 			            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
 			                String dValue = PERIODS.get((TimeUnit) value);
 			                return super.getListCellRendererComponent(list, dValue, index, isSelected, cellHasFocus);
 			            }
 			        });
 			        remComboBox.setSelectedItem(TimeUnit.MINUTES);
 			        JButton remButt = new JButton(getMessage("Remind.again.in"));
 			        remButt.addActionListener(new ActionListener(){
 			            public void actionPerformed(ActionEvent e) {
 			                dialog.setVisible(false);
 			                taskExecutor.schedule(new Runnable(){
 			                    public void run() {
 			                        runTask(taskExecutor, reminderEntry, periodic, calendarField, initialDate);
 			                    }
 			                }, ((Integer) remSpinner.getValue()).longValue(), (TimeUnit) remComboBox.getSelectedItem());
 			            }
 			        });
 			        remPanel.add(remButt);
 			        remPanel.add(remSpinner);
 			        remPanel.add(remComboBox);
 			        pControls.add(remPanel);
 			        p.add(pControls, BorderLayout.SOUTH);
 			        dialog.setAlwaysOnTop(true);
 			        dialog.setUndecorated(true);
 			        dialog.setContentPane(p);
 			        dialog.getRootPane().setBorder(new LineBorder(Color.RED, 3, true));
 			        dialog.pack();
 			        dialog.setLocation(
 			                (Toolkit.getDefaultToolkit().getScreenSize().width - dialog.getWidth()) / 2, 
 			                (Toolkit.getDefaultToolkit().getScreenSize().height - dialog.getHeight()) / 2);
 			        Toolkit.getDefaultToolkit().beep();
 			        dialog.setVisible(true);
 				}
 			}
 		});
     }
     
     private Collection<ReminderEntry> getReminderEntries() {
         Collection<ReminderEntry> entries = new LinkedList<ReminderEntry>();
         DefaultTableModel model = (DefaultTableModel) reminderEntriesTable.getModel();
         for (int i = 0; i < model.getRowCount(); i++) {
             ReminderEntry entry = new ReminderEntry();
             UUID id = UUID.fromString((String) model.getValueAt(i, 0));
             entry.setId(id);
             entry.setTitle((String) model.getValueAt(i, 1));
             entry.setDate((String) model.getValueAt(i, 2));
             entry.setTime((String) model.getValueAt(i, 3));
             HTMLEditorPanel editorPanel = editorPanels.get(id);
             entry.setDescription(editorPanel.getCode());
             entries.add(entry);
         }
         return entries;
     }
 
     private void cleanUpUnUsedAttachments(UUID id) {
         try {
             HTMLEditorPanel editorPanel = editorPanels.get(id);
             Collection<String> usedAttachmentNames = editorPanel.getProcessedAttachmentNames();
             Collection<Attachment> atts = BackEnd.getInstance().getAttachments(getId());
             for (Attachment att : atts) {
                 if (!usedAttachmentNames.contains(att.getName())) {
                     BackEnd.getInstance().removeAttachment(getId(), att.getName());
                 }
             }
         } catch (Exception ex) {
             // if some error occurred while cleaning up unused attachments,
             // ignore it, these attachments will be removed next time Bias persists data
         }
     }
     
     /* (non-Javadoc)
      * @see bias.extension.ToolExtension#getRepresentation()
      */
     @Override
     public ToolRepresentation getRepresentation() {
         JButton button = new JButton(ICON);
         button.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e) {
                 try {
                     parseData();
                     FrontEnd.displayDialog(mainPanel, Reminder.class.getSimpleName());
                 } catch (Throwable t) {
                     FrontEnd.displayErrorMessage(Reminder.class.getSimpleName() +  " :: failed to parse data file!", t);
                 }
             }
         });
         return new ToolRepresentation(button, null);
     }
     
     /* (non-Javadoc)
      * @see bias.extension.Extension#serializeData()
      */
     public byte[] serializeData() throws Throwable {
         Entries entries = objFactory.createEntries();
         for (ReminderEntry entry : getReminderEntries()) {
             Entry e = objFactory.createEntry();
             e.setId(entry.getId().toString());
             e.setTitle(entry.getTitle());
             e.setDescription(entry.getDescription());
             e.setDate(entry.getDate());
             e.setCheckmarkDate(checkmarkDates.get(entry.getId()));
             e.setTime(entry.getTime());
             entries.getEntry().add(e);
         }
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         getMarshaller().marshal(entries, baos);
         return baos.toByteArray();
     }
 
     /* (non-Javadoc)
      * @see bias.extension.Extension#serializeSettings()
      */
     public byte[] serializeSettings() throws Throwable {
         for (int i = 0; i < MAX_SORT_KEYS_NUMBER; i++) {
             if (sortByColumn[i] != -1 && sortOrder[i] != null) {
                 props.setProperty(PROPERTY_SORT_BY_COLUMN + i, "" + sortByColumn[i]);
                 props.setProperty(PROPERTY_SORT_ORDER + i, sortOrder[i].name());
             } else {
                 props.remove(PROPERTY_SORT_BY_COLUMN + i);
                 props.remove(PROPERTY_SORT_ORDER + i);
             }
         }
         int idx = reminderEntriesTable.getSelectedRow();
         if (idx != -1) {
             props.setProperty(PROPERTY_SELECTED_ROW, "" + reminderEntriesTable.getSelectedRow());
         } else {
             props.remove(PROPERTY_SELECTED_ROW);
         }
         int dl = splitPane.getDividerLocation();
         if (dl != -1) {
             props.setProperty(PROPERTY_DIVIDER_LOCATION, "" + dl);
         } else {
             props.remove(PROPERTY_DIVIDER_LOCATION);
         }
         StringBuffer colW = new StringBuffer();
         int cc = reminderEntriesTable.getColumnModel().getColumnCount();
         for (int i = 0; i < cc; i++) {
             colW.append(reminderEntriesTable.getColumnModel().getColumn(i).getWidth());
             if (i < cc - 1) {
                 colW.append(":");
             }
         }
         props.setProperty(PROPERTY_COLUMNS_WIDTHS, colW.toString());
         if (splitPane.getBottomComponent() != null) {
             HTMLEditorPanel htmlEditorPanel = ((HTMLEditorPanel) splitPane.getBottomComponent());
             JScrollPane sc = ((JScrollPane) htmlEditorPanel.getComponent(0));
             JScrollBar sb = sc.getVerticalScrollBar();
             if (sb != null && sb.getValue() != 0) {
                 props.setProperty(PROPERTY_SCROLLBAR_VERT, "" + sb.getValue());
             } else {
                 props.remove(PROPERTY_SCROLLBAR_VERT);
             }
             sb = sc.getHorizontalScrollBar();
             if (sb != null && sb.getValue() != 0) {
                 props.setProperty(PROPERTY_SCROLLBAR_HORIZ, "" + sb.getValue());
             } else {
                 props.remove(PROPERTY_SCROLLBAR_HORIZ);
             }
             int cp = htmlEditorPanel.getCaretPosition();
             props.setProperty(PROPERTY_CARET_POSITION, "" + cp);
         } else {
             props.remove(PROPERTY_SCROLLBAR_VERT);
             props.remove(PROPERTY_SCROLLBAR_HORIZ);
         }
         return PropertiesUtils.serializeProperties(props);
     }
 
 }
