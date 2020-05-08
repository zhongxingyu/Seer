 package ibfb.studyeditor.core;
 
 import ibfb.domain.core.*;
 import ibfb.lists.report.LabelReportWizardIterator;
 import ibfb.maize.core.MaizeFormulas;
 import ibfb.studyeditor.core.db.FieldbookCSVUtil;
 import ibfb.studyeditor.core.db.WorkbookSavingHelper;
 import ibfb.studyeditor.core.model.*;
 import ibfb.studyeditor.designs.DesignsClass;
 import ibfb.studyeditor.designs.DesignsGenerator;
 import ibfb.studyeditor.designs.DesignsUtils;
 import ibfb.studyeditor.export.FieldBookExcelExporter;
 import ibfb.studyeditor.export.FieldbookCSVExporter;
 import ibfb.studyeditor.export.FieldbookRExport;
 import ibfb.studyeditor.exportformaize.maizeExportWizardIterator;
 import ibfb.studyeditor.exportwizard.exportWizardIterator;
 import ibfb.studyeditor.exportwizard.exportWizardPanelGYTrait;
 import ibfb.studyeditor.gywizard.GYwizardWizardIterator;
 import ibfb.studyeditor.importwizard.ImportData;
 import ibfb.studyeditor.importwizard.importingVisualPanel2;
 import ibfb.studyeditor.importwizard.importingWizardIterator;
 import ibfb.studyeditor.reports.TraitsReportHelper;
 import ibfb.studyeditor.roweditors.*;
 import ibfb.studyeditor.util.Clipboard;
 import ibfb.studyeditor.util.DateUtil;
 import ibfb.studyeditor.util.LookupUtil;
 import ibfb.studyeditor.util.MyRenderer;
 import ibfb.studyeditor.util.RefreshBrowserHelper;
 import ibfb.studyexplorer.filters.CSVFiltro;
 import ibfb.studyexplorer.filters.ExcelFiltro;
 import ibfb.traits.traits.TraitsExplorerTopComponent;
 import java.awt.*;
 import java.awt.datatransfer.StringSelection;
 import java.io.File;
 import java.text.MessageFormat;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.regex.PatternSyntaxException;
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 import javax.swing.*;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.table.*;
 import net.java.balloontip.BalloonTip;
 import net.java.balloontip.utils.ToolTipUtils;
 import org.cimmyt.cril.ibwb.api.AppServicesProxy;
 import org.cimmyt.cril.ibwb.commongui.ConvertUtils;
 import org.cimmyt.cril.ibwb.commongui.DialogUtil;
 import org.cimmyt.cril.ibwb.commongui.FileUtils;
 import org.cimmyt.cril.ibwb.commongui.OSUtils;
 import org.cimmyt.cril.ibwb.commongui.select.list.DoubleListPanel;
 import org.cimmyt.cril.ibwb.commongui.select.list.DropTargetCommand;
 import org.cimmyt.cril.ibwb.commongui.select.list.SelectCommand;
 import org.cimmyt.cril.ibwb.domain.ContinuousConversion;
 import org.cimmyt.cril.ibwb.domain.ContinuousFunction;
 import org.cimmyt.cril.ibwb.domain.TmsConsistencyChecks;
 import org.cimmyt.cril.ibwb.domain.Traits;
 import org.cimmyt.cril.ibwb.domain.Transformations;
 import org.cimmyt.cril.ibwb.domain.constants.TypeDB;
 import org.cimmyt.cril.ibwb.domain.inventory.InventoryData;
 import org.netbeans.api.progress.ProgressHandle;
 import org.netbeans.api.progress.ProgressHandleFactory;
 import org.netbeans.api.settings.ConvertAsProperties;
 import org.openide.DialogDisplayer;
 import org.openide.NotifyDescriptor;
 import org.openide.WizardDescriptor;
 import org.openide.awt.ActionID;
 import org.openide.awt.ActionReference;
 import org.openide.util.Exceptions;
 import org.openide.util.Mutex;
 import org.openide.util.NbBundle;
 import org.openide.util.NbPreferences;
 import org.openide.windows.TopComponent;
 import org.openide.windows.WindowManager;
 
 @ConvertAsProperties(dtd = "-//ibfb.studyeditor.core//StudyEditor//EN",
 autostore = false)
 @TopComponent.Description(preferredID = "StudyEditorTopComponent",
 iconBase = "ibfb/studyeditor/images/newTrial16.png",
 persistenceType = TopComponent.PERSISTENCE_NEVER)
 @TopComponent.Registration(mode = "editor", openAtStartup = false)
 @ActionID(category = "Window", id = "ibfb.studyeditor.core.StudyEditorTopComponent")
 @ActionReference(path = "Menu/Window" /*
  * , position = 333
  */)
 @TopComponent.OpenActionRegistration(displayName = "#CTL_StudyEditorAction",
 preferredID = "StudyEditorTopComponent")
 public final class StudyEditorTopComponent extends TopComponent {
 
     private int crop = 0;
     final static String badchars = "`~!@#$%^&*()_+=\\|\"':;?/>.<, ";
     private JFileChooser selectorArchivo = new JFileChooser();
     private TableDataExporterHelper helper = new TableDataExporterHelper();
     private int fila = 0;
     TableRowSorter<TableModel> sorterTrialInformation;
     TableRowSorter<TableModel> sorterConstants;
     TableRowSorter<TableModel> sorterMeasurements;
     TableRowSorter<TableModel> sorterDesign;
     DefaultTableModel model = new DefaultTableModel();
     DefaultTableModel modelConstants = new DefaultTableModel();
     DefaultTableModel modelMeasurements = new DefaultTableModel();
     DesignTableModel modelDesign = new DesignTableModel();
     DefaultTableModel modelObs = new DefaultTableModel();
     private CSVFileManager csv;
     AlphaDesignsRowEditor alphaRowEditorStudy;
     private Workbook myWorkbook;
     private Workbook workbookAfterSave;
     private Workbook masterWorkbook;
     private String fileTemplate;
     private DefaultListModel listModelSelected = new DefaultListModel();
     private DefaultListModel listModelUnSelected = new DefaultListModel();
     private TransferHandler transfer = new ListItemTransferHandler(2);
     java.util.ArrayList<Variate> traits = new ArrayList<Variate>();
     DefaultListModel listModelA = new DefaultListModel();
     DefaultListModel listModelB = new DefaultListModel();
     public int trialStart = 0;
     public int trialEnd = 0;
     public int trialSelected = 0;
     public int triallOption = 0; //0=all trial, 1=selectedTrial, 2=rango de trials
     public String trialFile = "";
     public int opcionFiltro = 0;
     public int opcionExport = 0; //0=fieldlog, 1=to r, 2=excel file
     public int opcionImport = 0; //0=fieldlog, 1=excel 
     public File trialImportFile;
     private int extraColumns = 0;
     private ArrayList extraColumnsHeaders = new ArrayList<String>();
     private ArrayList headers = new ArrayList<String>();
     private ArrayList headersNew = new ArrayList<String>();
     private DesignsClass disenios = new DesignsClass();
     private static final int STUDY_PROPERTY_COL = 2;
     private static final int STUDY_SCALE_COL = 3;
     private static final int STUDY_VALUE_COL = 4;
     private static final int TRIAL_INSTANCE_COL = 0;
     private static final int TRIAL_PROPERTY_COL = 3;
     private static final int TRIAL_SCALE_COL = 4;
     private static final int TRIAL_VALUE_COL = 5;
     private int combinations = 0;
     private String[][] factorsDesignCad;
     private ArrayList<String> otherFactors;
     private ArrayList<String> children;
     private Study study;
     private DesignsUtils designsUtils;
     private List<Variate> availableTraits = new ArrayList<Variate>();
     private List<Variate> selectedTraits = new ArrayList<Variate>();
     private DoubleListPanel<Variate> doubleListPanel;
     private Variate traitToEvaluate;
     private String stringTraitToEvaluate = "GY";
     private boolean forMaster = false;
     private boolean conPreguntas = true;
     // get list of exsiting traits saved 
     private List<Variate> savedTraits;
     private SelectCommand unselectedCommand = new SelectCommand() {
 
         @Override
         public void execute() {
             Variate variate = doubleListPanel.getSelectedSourceItem();
             jTextFieldDescription.setText(variate.getDescription());
         }
     };
     private SelectCommand selectedCommand = new SelectCommand() {
 
         @Override
         public void execute() {
             Variate variate = doubleListPanel.getSelectedTargetItem();
             jTextFieldDescriptionSelected.setText(variate.getDescription());
         }
     };
 
     public boolean isConPreguntas() {
         return conPreguntas;
     }
 
     public void setConPreguntas(boolean conPreguntas) {
         this.conPreguntas = conPreguntas;
     }
 
     public boolean isForMaster() {
         return forMaster;
     }
 
     public void setForMaster(boolean forMaster) {
         this.forMaster = forMaster;
     }
 
     public String getStringTraitToEvaluate() {
         return stringTraitToEvaluate;
     }
 
     public void setStringTraitToEvaluate(String stringTraitToEvaluate) {
         this.stringTraitToEvaluate = stringTraitToEvaluate;
     }
 
     public Variate getTraitToEvaluate() {
         return traitToEvaluate;
     }
 
     public void setTraitToEvaluate(Variate traitToEvaluate) {
         this.traitToEvaluate = traitToEvaluate;
     }
 
     public List<Variate> getSelectedTraits() {
 
 
         return selectedTraits;
     }
 
     public void setSelectedTraits(List<Variate> selectedTraits) {
         this.selectedTraits = selectedTraits;
     }
     /**
      * Current study already exists?
      */
     private boolean studyAlreadyExists;
     /**
      * Particular information about Trial
      */
     private Study studyInfo;
     private BalloonTip tipSavingTrial;
     /**
      * Is it closing in progress?
      */
     private boolean closingEditor = false;
 
     public StudyEditorTopComponent() {
         initComponents();
         setName(NbBundle.getMessage(StudyEditorTopComponent.class, "CTL_StudyEditorTopComponent"));
         setToolTipText(NbBundle.getMessage(StudyEditorTopComponent.class, "HINT_StudyEditorTopComponent"));
         modelMeasurements = (DefaultTableModel) this.jTableObservations.getModel();
         sorterMeasurements = new TableRowSorter<TableModel>(modelMeasurements);
         this.jTableObservations.setRowSorter(sorterMeasurements);
         model = (DefaultTableModel) this.jTableTrialConditions.getModel();
         sorterTrialInformation = new TableRowSorter<TableModel>(model);
         this.jTableTrialConditions.setRowSorter(sorterTrialInformation);
         modelConstants = (DefaultTableModel) this.jTableConstants.getModel();
         sorterConstants = new TableRowSorter<TableModel>(modelConstants);
         this.jTableConstants.setRowSorter(sorterConstants);
         modelDesign = (DesignTableModel) this.jTableDesign.getModel();
         sorterDesign = new TableRowSorter<TableModel>(modelDesign);
         this.jTableDesign.setRowSorter(sorterDesign);
         modelObs = (DefaultTableModel) this.jTableObservations.getModel();
         assignCellEditorTrialCndition();
         assignCellEditorStudyConditions();
         this.jTableStudyConditions.getTableHeader().addMouseListener(new ColumnFitAdapter());
         this.jTableTrialConditions.getTableHeader().addMouseListener(new ColumnFitAdapter());
         this.jTableConstants.getTableHeader().addMouseListener(new ColumnFitAdapter());
         this.jTableOtherFactorLabels.getTableHeader().addMouseListener(new ColumnFitAdapter());
         this.jTableEntries.getTableHeader().addMouseListener(new ColumnFitAdapter());
         this.jTableDesign.getTableHeader().addMouseListener(new ColumnFitAdapter());
         this.jTableObservations.getTableHeader().addMouseListener(new ColumnFitAdapter());
         csv = new CSVFileManager(this.jTableObservations, new JList());
         deshabilitaSorters();
         designsUtils = new DesignsUtils(jTableDesign, jTextFieldEntries);
         doubleListPanel = new DoubleListPanel<Variate>(availableTraits, selectedTraits, unselectedCommand, selectedCommand);
         doubleListPanel.setSourceLabel(NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.unselected"));
         doubleListPanel.setTargetLabel(NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.selected"));
         doubleListPanel.setVisible(true);
         doubleListPanel.setDropTargetCommand(new TraitsDropTargetCommand());
         pnlSelectList.add(doubleListPanel);
 
         studyAlreadyExists = false;
 
         studyInfo = new Study();
 
         createBallonTips();
 
         jRadioButtonFilterTrialStudy.setVisible(false);
         jRadioButtonViewAllTrialStudy.setVisible(false);
         jSpinnerTrialStudy.setVisible(false);
         pnlExpConditionTrial.setVisible(false);
         // this.jTabbedPaneEditor.setEnabledAt(8, false);
 
     }
 
     public ArrayList<String> getChildren() {
         return children;
     }
 
     public void setChildren(ArrayList<String> children) {
         this.children = children;
     }
 
     public ArrayList<String> getOtherFactors() {
         return otherFactors;
     }
 
     public void setOtherFactors(ArrayList<String> otherFactors) {
         this.otherFactors = otherFactors;
     }
 
     public String[][] getFactorsDesignCad() {
         return factorsDesignCad;
     }
 
     public void setFactorsDesignCad(String[][] factorsDesignCad) {
         this.factorsDesignCad = factorsDesignCad;
     }
 
     public int getCombinations() {
         return combinations;
     }
 
     public void setCombinations(int combinations) {
         this.combinations = combinations;
     }
 
     public String getFileTemplate() {
         return fileTemplate;
     }
 
     public void setFileTemplate(String fileTemplate) {
         this.fileTemplate = fileTemplate;
     }
 
     public Workbook getMyWorkbook() {
         return myWorkbook;
     }
 
     public void setMyWorkbook(Workbook myWorkbook) {
         this.myWorkbook = myWorkbook;
     }
 
     public JSpinner getjSpinnerConstants() {
         return jSpinnerExpConditions;
     }
 
     public void setjSpinnerConstants(JSpinner jSpinnerConstants) {
         this.jSpinnerExpConditions = jSpinnerConstants;
     }
 
     public JSpinner getjSpinnerTrInformation() {
         return jSpinnerTrialInformation;
     }
 
     public void setjSpinnerTrInformation(JSpinner jSpinnerTrInformation) {
         this.jSpinnerTrialInformation = jSpinnerTrInformation;
     }
 
     public JTextField getjTextFieldEntries() {
         return jTextFieldEntries;
     }
 
     public void setjTextFieldEntries(JTextField jTextFieldEntries) {
         this.jTextFieldEntries = jTextFieldEntries;
     }
 
     public void asignRowEditor() {
         alphaRowEditorStudy = new AlphaDesignsRowEditor(jTableDesign, Integer.parseInt(this.jTextFieldEntries.getText()), designsUtils.getRep2(), designsUtils.getRep3(), designsUtils.getRep4());
         TableColumn valueColumn = jTableDesign.getColumnModel().getColumn(3);
         valueColumn.setCellEditor(alphaRowEditorStudy);
 
     }
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         buttonGroupTrInformation = new javax.swing.ButtonGroup();
         buttonGroupExpConditions = new javax.swing.ButtonGroup();
         buttonGroupDesign = new javax.swing.ButtonGroup();
         buttonGroupMeasurements = new javax.swing.ButtonGroup();
         jPopupMenuTraits = new javax.swing.JPopupMenu();
         jMenuItem1 = new javax.swing.JMenuItem();
         jMenuItem2 = new javax.swing.JMenuItem();
         jPopupMenuConstants = new javax.swing.JPopupMenu();
         jMenuItem3 = new javax.swing.JMenuItem();
         jMenuItem4 = new javax.swing.JMenuItem();
         jPopupMenuUnSelect = new javax.swing.JPopupMenu();
         jMenuItemUnSelect = new javax.swing.JMenuItem();
         jMenuItemUnselectAll = new javax.swing.JMenuItem();
         jPopupMenuSelect = new javax.swing.JPopupMenu();
         jMenuItemSelect = new javax.swing.JMenuItem();
         jMenuItemSelectAll = new javax.swing.JMenuItem();
         jToolBar1 = new javax.swing.JToolBar();
         buttonGroupMaster = new javax.swing.ButtonGroup();
         jTabbedPaneEditor = new javax.swing.JTabbedPane();
         pnlGeneralInformation = new javax.swing.JPanel();
         jScrollPane2 = new javax.swing.JScrollPane();
         jTableStudyConditions = new javax.swing.JTable();
         lblStudyConditions = new javax.swing.JLabel();
         JPanelData = new javax.swing.JPanel();
         jTextFieldObjective = new javax.swing.JTextField();
         lblTitle = new javax.swing.JLabel();
         jTextFieldStudy = new javax.swing.JTextField();
         jTextFieldTitle = new javax.swing.JTextField();
         lblStudy = new javax.swing.JLabel();
         lblObjective = new javax.swing.JLabel();
         lblStartDate = new javax.swing.JLabel();
         lblEndDate = new javax.swing.JLabel();
         jComboBoxStudyType = new javax.swing.JComboBox();
         jTextFieldPMKey = new javax.swing.JTextField();
         lblStudyType = new javax.swing.JLabel();
         lblProjectKey = new javax.swing.JLabel();
         lblInstances = new javax.swing.JLabel();
         jLabelInstances = new javax.swing.JLabel();
         jDateChooserStart = new com.toedter.calendar.JDateChooser();
         jDateChooserEnd = new com.toedter.calendar.JDateChooser();
         pnlTrialInformation = new javax.swing.JPanel();
         pnlTrialInformationFilter = new javax.swing.JPanel();
         jRadioButtonFilterTrialInfo = new javax.swing.JRadioButton();
         jRadioButtonAllTrials = new javax.swing.JRadioButton();
         pnlTrialInformationTrial = new javax.swing.JLabel();
         jSpinnerTrialInformation = new javax.swing.JSpinner();
         jScrollPane1 = new javax.swing.JScrollPane();
         jTableTrialConditions = new javax.swing.JTable();
         pnlExperimentalConditions = new javax.swing.JPanel();
         pnlExperimentalConditionFilter = new javax.swing.JPanel();
         jRadioButtonFilterExpConditions = new javax.swing.JRadioButton();
         jRadioButtonAllExpCondition = new javax.swing.JRadioButton();
         lblExpConditionsTrial = new javax.swing.JLabel();
         jSpinnerExpConditions = new javax.swing.JSpinner();
         jScrollPane5 = new javax.swing.JScrollPane();
         jTableConstants = new javax.swing.JTable();
         pnlGermplasmEntries = new javax.swing.JPanel();
         jScrollPane6 = new javax.swing.JScrollPane();
         jTableEntries = new javax.swing.JTable();
         jPanel13 = new javax.swing.JPanel();
         jLabel5 = new javax.swing.JLabel();
         jLabelTotalGermp = new javax.swing.JLabel();
         pnlOtherTreatment = new javax.swing.JPanel();
         jScrollPane3 = new javax.swing.JScrollPane();
         jTableOtherFactorLabels = new javax.swing.JTable();
         jScrollPane4 = new javax.swing.JScrollPane();
         jTableOtherFactors = new javax.swing.JTable();
         pnlExperimentalDesign = new javax.swing.JPanel();
         pnlExperimConditionsFilter = new javax.swing.JPanel();
         jRadioButtonFilterTrialStudy = new javax.swing.JRadioButton();
         pnlExpConditionTrial = new javax.swing.JLabel();
         jSpinnerTrialStudy = new javax.swing.JSpinner();
         jRadioButtonViewAllTrialStudy = new javax.swing.JRadioButton();
         jButtonRefreshDesign = new javax.swing.JButton();
         jLabelEntries = new javax.swing.JLabel();
         jTextFieldEntries = new javax.swing.JTextField();
         pnlExpDesignDesign = new javax.swing.JPanel();
         rbtnIndividualDesign = new javax.swing.JRadioButton();
         rbtnUseSameDesign = new javax.swing.JRadioButton();
         rBtnImportLayoutFile = new javax.swing.JRadioButton();
         lblFileName = new javax.swing.JLabel();
         txtFileName = new javax.swing.JTextField();
         btnBrowse = new javax.swing.JButton();
         jScrollPane8 = new javax.swing.JScrollPane();
         jTableDesign = new javax.swing.JTable();
         pnlTraits = new javax.swing.JPanel();
         jPanelTraits = new javax.swing.JPanel();
         jTextFieldDescription = new javax.swing.JTextField();
         jTextFieldDescriptionSelected = new javax.swing.JTextField();
         jLabel13 = new javax.swing.JLabel();
         jPanel9 = new javax.swing.JPanel();
         jButtonSelectTraits = new javax.swing.JButton();
         jButtonSync = new javax.swing.JButton();
         jPanel12 = new javax.swing.JPanel();
         pnlSelectList = new javax.swing.JPanel();
         pnlMeasurement = new javax.swing.JPanel();
         jPanel21 = new javax.swing.JPanel();
         jScrollPane7 = new javax.swing.JScrollPane();
         jTableObservations = new javax.swing.JTable();
         pnlMeasurementFilter = new javax.swing.JPanel();
         jRadioButtonFilterTrial = new javax.swing.JRadioButton();
         jRadioButtonAllTrials2 = new javax.swing.JRadioButton();
         jSpinnerTrial = new javax.swing.JSpinner();
         jRadioButtonFilterEntry = new javax.swing.JRadioButton();
         jSpinnerEntry = new javax.swing.JSpinner();
         lblTrialName = new javax.swing.JLabel();
         jTextTrialName = new javax.swing.JTextField();
         jPanel5 = new javax.swing.JPanel();
         btnPrintLabels = new javax.swing.JButton();
         jButtonSaveData = new javax.swing.JButton();
         jButtonExportData = new javax.swing.JButton();
         jButtonImportData = new javax.swing.JButton();
         jPanel1 = new javax.swing.JPanel();
         jScrollPane9 = new javax.swing.JScrollPane();
         jTableMaster = new javax.swing.JTable();
         jPanel6 = new javax.swing.JPanel();
         jButtonSaveMaster = new javax.swing.JButton();
         jButtonExportMaster = new javax.swing.JButton();
         pnlMeasurementFilter1 = new javax.swing.JPanel();
         jRadioButtonFilterTrialMaster = new javax.swing.JRadioButton();
         jRadioButtonAllTrialsMaster = new javax.swing.JRadioButton();
         jSpinnerTrialMaster = new javax.swing.JSpinner();
         jRadioButtonFilterEntry1 = new javax.swing.JRadioButton();
         jSpinnerEntry1 = new javax.swing.JSpinner();
         lblTrialNameMaster = new javax.swing.JLabel();
         jTextTrialNameMaster = new javax.swing.JTextField();
 
         org.openide.awt.Mnemonics.setLocalizedText(jMenuItem1, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jMenuItem1.text")); // NOI18N
         jMenuItem1.setToolTipText(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jMenuItem1.toolTipText")); // NOI18N
         jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem1ActionPerformed(evt);
             }
         });
         jPopupMenuTraits.add(jMenuItem1);
 
         org.openide.awt.Mnemonics.setLocalizedText(jMenuItem2, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jMenuItem2.text")); // NOI18N
         jMenuItem2.setToolTipText(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jMenuItem2.toolTipText")); // NOI18N
         jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem2ActionPerformed(evt);
             }
         });
         jPopupMenuTraits.add(jMenuItem2);
 
         org.openide.awt.Mnemonics.setLocalizedText(jMenuItem3, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jMenuItem3.text")); // NOI18N
         jMenuItem3.setToolTipText(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jMenuItem3.toolTipText")); // NOI18N
         jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem3ActionPerformed(evt);
             }
         });
         jPopupMenuConstants.add(jMenuItem3);
 
         org.openide.awt.Mnemonics.setLocalizedText(jMenuItem4, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jMenuItem4.text")); // NOI18N
         jMenuItem4.setToolTipText(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jMenuItem4.toolTipText")); // NOI18N
         jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem4ActionPerformed(evt);
             }
         });
         jPopupMenuConstants.add(jMenuItem4);
 
         org.openide.awt.Mnemonics.setLocalizedText(jMenuItemUnSelect, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jMenuItemUnSelect.text")); // NOI18N
         jMenuItemUnSelect.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItemUnSelectActionPerformed(evt);
             }
         });
         jPopupMenuUnSelect.add(jMenuItemUnSelect);
 
         org.openide.awt.Mnemonics.setLocalizedText(jMenuItemUnselectAll, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jMenuItemUnselectAll.text")); // NOI18N
         jMenuItemUnselectAll.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItemUnselectAllActionPerformed(evt);
             }
         });
         jPopupMenuUnSelect.add(jMenuItemUnselectAll);
 
         org.openide.awt.Mnemonics.setLocalizedText(jMenuItemSelect, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jMenuItemSelect.text")); // NOI18N
         jMenuItemSelect.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItemSelectActionPerformed(evt);
             }
         });
         jPopupMenuSelect.add(jMenuItemSelect);
 
         org.openide.awt.Mnemonics.setLocalizedText(jMenuItemSelectAll, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jMenuItemSelectAll.text")); // NOI18N
         jMenuItemSelectAll.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItemSelectAllActionPerformed(evt);
             }
         });
         jPopupMenuSelect.add(jMenuItemSelectAll);
 
         jToolBar1.setRollover(true);
 
         jTabbedPaneEditor.setMinimumSize(new java.awt.Dimension(0, 0));
         jTabbedPaneEditor.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jTabbedPaneEditorStateChanged(evt);
             }
         });
 
         jTableStudyConditions.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 "Condition", "Description", "Property", "Scale", "Value"
             }
         ) {
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, true
             };
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         jTableStudyConditions.getTableHeader().setReorderingAllowed(false);
         jTableStudyConditions.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
             public void propertyChange(java.beans.PropertyChangeEvent evt) {
                 jTableStudyConditionsPropertyChange(evt);
             }
         });
         jScrollPane2.setViewportView(jTableStudyConditions);
 
         org.openide.awt.Mnemonics.setLocalizedText(lblStudyConditions, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.lblStudyConditions.text")); // NOI18N
 
         JPanelData.setBorder(javax.swing.BorderFactory.createEtchedBorder());
 
         jTextFieldObjective.setText(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jTextFieldObjective.text")); // NOI18N
 
         org.openide.awt.Mnemonics.setLocalizedText(lblTitle, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.lblTitle.text")); // NOI18N
 
         jTextFieldStudy.setText(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jTextFieldStudy.text")); // NOI18N
         jTextFieldStudy.setMinimumSize(new java.awt.Dimension(14, 50));
 
         jTextFieldTitle.setText(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jTextFieldTitle.text")); // NOI18N
 
         org.openide.awt.Mnemonics.setLocalizedText(lblStudy, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.lblStudy.text")); // NOI18N
 
         org.openide.awt.Mnemonics.setLocalizedText(lblObjective, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.lblObjective.text")); // NOI18N
 
         org.openide.awt.Mnemonics.setLocalizedText(lblStartDate, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.lblStartDate.text")); // NOI18N
 
         org.openide.awt.Mnemonics.setLocalizedText(lblEndDate, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.lblEndDate.text")); // NOI18N
 
         jComboBoxStudyType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "International", "National", "Bank", "Foreign" }));
 
         jTextFieldPMKey.setText(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jTextFieldPMKey.text")); // NOI18N
 
         org.openide.awt.Mnemonics.setLocalizedText(lblStudyType, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.lblStudyType.text")); // NOI18N
 
         org.openide.awt.Mnemonics.setLocalizedText(lblProjectKey, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.lblProjectKey.text")); // NOI18N
 
         org.openide.awt.Mnemonics.setLocalizedText(lblInstances, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.lblInstances.text")); // NOI18N
 
         org.openide.awt.Mnemonics.setLocalizedText(jLabelInstances, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jLabelInstances.text")); // NOI18N
 
         jDateChooserStart.setDateFormatString(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jDateChooserStart.dateFormatString")); // NOI18N
         jDateChooserStart.addInputMethodListener(new java.awt.event.InputMethodListener() {
             public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                 jDateChooserStartInputMethodTextChanged(evt);
             }
             public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
             }
         });
         jDateChooserStart.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
             public void propertyChange(java.beans.PropertyChangeEvent evt) {
                 jDateChooserStartPropertyChange(evt);
             }
         });
 
         jDateChooserEnd.setDateFormatString(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jDateChooserEnd.dateFormatString")); // NOI18N
         jDateChooserEnd.addInputMethodListener(new java.awt.event.InputMethodListener() {
             public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                 jDateChooserEndInputMethodTextChanged(evt);
             }
             public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
             }
         });
         jDateChooserEnd.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
             public void propertyChange(java.beans.PropertyChangeEvent evt) {
                 jDateChooserEndPropertyChange(evt);
             }
         });
 
         javax.swing.GroupLayout JPanelDataLayout = new javax.swing.GroupLayout(JPanelData);
         JPanelData.setLayout(JPanelDataLayout);
         JPanelDataLayout.setHorizontalGroup(
             JPanelDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(JPanelDataLayout.createSequentialGroup()
                 .addGroup(JPanelDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(JPanelDataLayout.createSequentialGroup()
                         .addContainerGap()
                         .addGroup(JPanelDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(lblObjective)))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, JPanelDataLayout.createSequentialGroup()
                         .addGap(41, 41, 41)
                         .addComponent(lblStudy, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addGap(15, 15, 15)
                 .addGroup(JPanelDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(JPanelDataLayout.createSequentialGroup()
                         .addGroup(JPanelDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jTextFieldTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
                             .addComponent(jTextFieldObjective, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                             .addComponent(jTextFieldStudy, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE))
                         .addGap(140, 140, 140))
                     .addGroup(JPanelDataLayout.createSequentialGroup()
                         .addComponent(lblInstances)
                         .addGap(18, 18, 18)
                         .addComponent(jLabelInstances, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                 .addGroup(JPanelDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(lblStartDate, javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(lblEndDate, javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(lblStudyType, javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(lblProjectKey, javax.swing.GroupLayout.Alignment.TRAILING))
                 .addGap(18, 18, 18)
                 .addGroup(JPanelDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(jComboBoxStudyType, 0, 150, Short.MAX_VALUE)
                     .addComponent(jTextFieldPMKey, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                     .addComponent(jDateChooserStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jDateChooserEnd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addGap(250, 250, 250))
         );
         JPanelDataLayout.setVerticalGroup(
             JPanelDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(JPanelDataLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(JPanelDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(JPanelDataLayout.createSequentialGroup()
                         .addGroup(JPanelDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE, false)
                             .addComponent(jTextFieldStudy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(lblStudy))
                         .addGroup(JPanelDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(JPanelDataLayout.createSequentialGroup()
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(jTextFieldTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGroup(JPanelDataLayout.createSequentialGroup()
                                 .addGap(22, 22, 22)
                                 .addComponent(lblTitle)))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(JPanelDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jTextFieldObjective, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(lblObjective))
                         .addGap(18, 18, 18)
                         .addGroup(JPanelDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(lblInstances)
                             .addComponent(jLabelInstances)))
                     .addGroup(JPanelDataLayout.createSequentialGroup()
                         .addGroup(JPanelDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(JPanelDataLayout.createSequentialGroup()
                                 .addComponent(lblStartDate)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(lblEndDate))
                             .addGroup(JPanelDataLayout.createSequentialGroup()
                                 .addComponent(jDateChooserStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jDateChooserEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(JPanelDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(JPanelDataLayout.createSequentialGroup()
                                 .addComponent(lblStudyType)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(lblProjectKey))
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, JPanelDataLayout.createSequentialGroup()
                                 .addComponent(jComboBoxStudyType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jTextFieldPMKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         JPanelDataLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jTextFieldObjective, jTextFieldTitle, lblObjective, lblStudy, lblTitle});
 
         JPanelDataLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jTextFieldStudy, lblEndDate, lblStartDate});
 
         JPanelDataLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jComboBoxStudyType, jTextFieldPMKey, lblProjectKey, lblStudyType});
 
         javax.swing.GroupLayout pnlGeneralInformationLayout = new javax.swing.GroupLayout(pnlGeneralInformation);
         pnlGeneralInformation.setLayout(pnlGeneralInformationLayout);
         pnlGeneralInformationLayout.setHorizontalGroup(
             pnlGeneralInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlGeneralInformationLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlGeneralInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 1049, Short.MAX_VALUE)
                     .addComponent(JPanelData, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(lblStudyConditions, javax.swing.GroupLayout.Alignment.LEADING))
                 .addContainerGap())
         );
         pnlGeneralInformationLayout.setVerticalGroup(
             pnlGeneralInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlGeneralInformationLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(JPanelData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(33, 33, 33)
                 .addComponent(lblStudyConditions)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1311, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         jTabbedPaneEditor.addTab(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.pnlGeneralInformation.TabConstraints.tabTitle"), pnlGeneralInformation); // NOI18N
 
         pnlTrialInformationFilter.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.pnlTrialInformationFilter.border.title"))); // NOI18N
 
         buttonGroupTrInformation.add(jRadioButtonFilterTrialInfo);
         org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonFilterTrialInfo, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jRadioButtonFilterTrialInfo.text")); // NOI18N
         jRadioButtonFilterTrialInfo.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 jRadioButtonFilterTrialInfoItemStateChanged(evt);
             }
         });
 
         buttonGroupTrInformation.add(jRadioButtonAllTrials);
         jRadioButtonAllTrials.setSelected(true);
         org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonAllTrials, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jRadioButtonAllTrials.text")); // NOI18N
         jRadioButtonAllTrials.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 jRadioButtonAllTrialsItemStateChanged(evt);
             }
         });
 
         org.openide.awt.Mnemonics.setLocalizedText(pnlTrialInformationTrial, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.pnlTrialInformationTrial.text")); // NOI18N
 
         jSpinnerTrialInformation.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jSpinnerTrialInformationStateChanged(evt);
             }
         });
 
         javax.swing.GroupLayout pnlTrialInformationFilterLayout = new javax.swing.GroupLayout(pnlTrialInformationFilter);
         pnlTrialInformationFilter.setLayout(pnlTrialInformationFilterLayout);
         pnlTrialInformationFilterLayout.setHorizontalGroup(
             pnlTrialInformationFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlTrialInformationFilterLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jRadioButtonFilterTrialInfo)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(pnlTrialInformationTrial)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jSpinnerTrialInformation, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 812, Short.MAX_VALUE)
                 .addComponent(jRadioButtonAllTrials)
                 .addContainerGap())
         );
         pnlTrialInformationFilterLayout.setVerticalGroup(
             pnlTrialInformationFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlTrialInformationFilterLayout.createSequentialGroup()
                 .addGroup(pnlTrialInformationFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jRadioButtonAllTrials)
                     .addComponent(jSpinnerTrialInformation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(pnlTrialInformationFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(pnlTrialInformationTrial)
                         .addComponent(jRadioButtonFilterTrialInfo)))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jTableTrialConditions.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 "Trial", "Condition", "Description", "Property", "Scale", "Value"
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
             };
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, false, true
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         jTableTrialConditions.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
         jTableTrialConditions.getTableHeader().setReorderingAllowed(false);
         jTableTrialConditions.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
             public void propertyChange(java.beans.PropertyChangeEvent evt) {
                 jTableTrialConditionsPropertyChange(evt);
             }
         });
         jScrollPane1.setViewportView(jTableTrialConditions);
 
         javax.swing.GroupLayout pnlTrialInformationLayout = new javax.swing.GroupLayout(pnlTrialInformation);
         pnlTrialInformation.setLayout(pnlTrialInformationLayout);
         pnlTrialInformationLayout.setHorizontalGroup(
             pnlTrialInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlTrialInformationLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlTrialInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 1041, Short.MAX_VALUE)
                     .addComponent(pnlTrialInformationFilter, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         pnlTrialInformationLayout.setVerticalGroup(
             pnlTrialInformationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlTrialInformationLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(pnlTrialInformationFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1435, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         jTabbedPaneEditor.addTab(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.pnlTrialInformation.TabConstraints.tabTitle"), pnlTrialInformation); // NOI18N
 
         pnlExperimentalConditionFilter.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.pnlExperimentalConditionFilter.border.title"))); // NOI18N
 
         buttonGroupExpConditions.add(jRadioButtonFilterExpConditions);
         org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonFilterExpConditions, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jRadioButtonFilterExpConditions.text")); // NOI18N
         jRadioButtonFilterExpConditions.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 jRadioButtonFilterExpConditionsItemStateChanged(evt);
             }
         });
 
         buttonGroupExpConditions.add(jRadioButtonAllExpCondition);
         jRadioButtonAllExpCondition.setSelected(true);
         org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonAllExpCondition, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jRadioButtonAllExpCondition.text")); // NOI18N
         jRadioButtonAllExpCondition.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 jRadioButtonAllExpConditionItemStateChanged(evt);
             }
         });
 
         org.openide.awt.Mnemonics.setLocalizedText(lblExpConditionsTrial, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.lblExpConditionsTrial.text")); // NOI18N
 
         jSpinnerExpConditions.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jSpinnerExpConditionsStateChanged(evt);
             }
         });
 
         javax.swing.GroupLayout pnlExperimentalConditionFilterLayout = new javax.swing.GroupLayout(pnlExperimentalConditionFilter);
         pnlExperimentalConditionFilter.setLayout(pnlExperimentalConditionFilterLayout);
         pnlExperimentalConditionFilterLayout.setHorizontalGroup(
             pnlExperimentalConditionFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlExperimentalConditionFilterLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jRadioButtonFilterExpConditions)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(lblExpConditionsTrial)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jSpinnerExpConditions, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 812, Short.MAX_VALUE)
                 .addComponent(jRadioButtonAllExpCondition)
                 .addContainerGap())
         );
         pnlExperimentalConditionFilterLayout.setVerticalGroup(
             pnlExperimentalConditionFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlExperimentalConditionFilterLayout.createSequentialGroup()
                 .addGroup(pnlExperimentalConditionFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jRadioButtonAllExpCondition)
                     .addComponent(jSpinnerExpConditions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(pnlExperimentalConditionFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jRadioButtonFilterExpConditions)
                         .addComponent(lblExpConditionsTrial)))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jScrollPane5.setComponentPopupMenu(jPopupMenuConstants);
 
         jTableConstants.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 "Trial", "Constant", "Description", "Scale", "Value"
             }
         ) {
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, true
             };
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         jTableConstants.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
         jTableConstants.getTableHeader().setReorderingAllowed(false);
         jScrollPane5.setViewportView(jTableConstants);
 
         javax.swing.GroupLayout pnlExperimentalConditionsLayout = new javax.swing.GroupLayout(pnlExperimentalConditions);
         pnlExperimentalConditions.setLayout(pnlExperimentalConditionsLayout);
         pnlExperimentalConditionsLayout.setHorizontalGroup(
             pnlExperimentalConditionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlExperimentalConditionsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlExperimentalConditionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 1041, Short.MAX_VALUE)
                     .addComponent(pnlExperimentalConditionFilter, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         pnlExperimentalConditionsLayout.setVerticalGroup(
             pnlExperimentalConditionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlExperimentalConditionsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(pnlExperimentalConditionFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 1435, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         jTabbedPaneEditor.addTab(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.pnlExperimentalConditions.TabConstraints.tabTitle"), pnlExperimentalConditions); // NOI18N
 
         jTableEntries.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 "Entry"
             }
         ) {
             boolean[] canEdit = new boolean [] {
                 false
             };
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         jTableEntries.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
         jTableEntries.getTableHeader().setReorderingAllowed(false);
         jTableEntries.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
             public void propertyChange(java.beans.PropertyChangeEvent evt) {
                 jTableEntriesPropertyChange(evt);
             }
         });
         jScrollPane6.setViewportView(jTableEntries);
 
         org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jLabel5.text")); // NOI18N
 
         org.openide.awt.Mnemonics.setLocalizedText(jLabelTotalGermp, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jLabelTotalGermp.text")); // NOI18N
 
         javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
         jPanel13.setLayout(jPanel13Layout);
         jPanel13Layout.setHorizontalGroup(
             jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel13Layout.createSequentialGroup()
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(jLabel5)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jLabelTotalGermp, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(63, 63, 63))
         );
         jPanel13Layout.setVerticalGroup(
             jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel13Layout.createSequentialGroup()
                 .addContainerGap(49, Short.MAX_VALUE)
                 .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel5)
                     .addComponent(jLabelTotalGermp))
                 .addGap(34, 34, 34))
         );
 
         javax.swing.GroupLayout pnlGermplasmEntriesLayout = new javax.swing.GroupLayout(pnlGermplasmEntries);
         pnlGermplasmEntries.setLayout(pnlGermplasmEntriesLayout);
         pnlGermplasmEntriesLayout.setHorizontalGroup(
             pnlGermplasmEntriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlGermplasmEntriesLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlGermplasmEntriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 1049, Short.MAX_VALUE)
                     .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         pnlGermplasmEntriesLayout.setVerticalGroup(
             pnlGermplasmEntriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlGermplasmEntriesLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 1407, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         jTabbedPaneEditor.addTab(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.pnlGermplasmEntries.TabConstraints.tabTitle"), pnlGermplasmEntries); // NOI18N
 
         jTableOtherFactorLabels.setModel(new ibfb.studyeditor.core.model.TreatmentLabelsTableModel());
         jTableOtherFactorLabels.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
         jTableOtherFactorLabels.getTableHeader().setReorderingAllowed(false);
         jScrollPane3.setViewportView(jTableOtherFactorLabels);
 
         jTableOtherFactors.setModel(new ibfb.studyeditor.core.model.OtherTreatmentFactorsTableModel());
         jScrollPane4.setViewportView(jTableOtherFactors);
 
         javax.swing.GroupLayout pnlOtherTreatmentLayout = new javax.swing.GroupLayout(pnlOtherTreatment);
         pnlOtherTreatment.setLayout(pnlOtherTreatmentLayout);
         pnlOtherTreatmentLayout.setHorizontalGroup(
             pnlOtherTreatmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlOtherTreatmentLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlOtherTreatmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 1041, Short.MAX_VALUE)
                     .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 1049, Short.MAX_VALUE))
                 .addContainerGap())
         );
         pnlOtherTreatmentLayout.setVerticalGroup(
             pnlOtherTreatmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlOtherTreatmentLayout.createSequentialGroup()
                 .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 916, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 599, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         jTabbedPaneEditor.addTab(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.pnlOtherTreatment.TabConstraints.tabTitle"), pnlOtherTreatment); // NOI18N
 
         pnlExperimConditionsFilter.setBorder(javax.swing.BorderFactory.createEtchedBorder());
 
         buttonGroupDesign.add(jRadioButtonFilterTrialStudy);
         org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonFilterTrialStudy, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jRadioButtonFilterTrialStudy.text")); // NOI18N
         jRadioButtonFilterTrialStudy.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 jRadioButtonFilterTrialStudyItemStateChanged(evt);
             }
         });
 
         org.openide.awt.Mnemonics.setLocalizedText(pnlExpConditionTrial, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.pnlExpConditionTrial.text")); // NOI18N
 
         jSpinnerTrialStudy.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jSpinnerTrialStudyStateChanged(evt);
             }
         });
 
         buttonGroupDesign.add(jRadioButtonViewAllTrialStudy);
         org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonViewAllTrialStudy, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jRadioButtonViewAllTrialStudy.text")); // NOI18N
         jRadioButtonViewAllTrialStudy.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 jRadioButtonViewAllTrialStudyItemStateChanged(evt);
             }
         });
 
         jButtonRefreshDesign.setFont(new java.awt.Font("Lucida Grande", 1, 18)); // NOI18N
         jButtonRefreshDesign.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ibfb/studyeditor/images/sync.png"))); // NOI18N
         org.openide.awt.Mnemonics.setLocalizedText(jButtonRefreshDesign, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jButtonRefreshDesign.text")); // NOI18N
         jButtonRefreshDesign.setToolTipText(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jButtonRefreshDesign.toolTipText")); // NOI18N
         jButtonRefreshDesign.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonRefreshDesignActionPerformed(evt);
             }
         });
 
         org.openide.awt.Mnemonics.setLocalizedText(jLabelEntries, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jLabelEntries.text")); // NOI18N
 
         jTextFieldEntries.setEditable(false);
         jTextFieldEntries.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
         jTextFieldEntries.setHorizontalAlignment(javax.swing.JTextField.CENTER);
         jTextFieldEntries.setText(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jTextFieldEntries.text")); // NOI18N
 
         pnlExpDesignDesign.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.pnlExpDesignDesign.border.title"))); // NOI18N
 
         buttonGroupDesign.add(rbtnIndividualDesign);
         org.openide.awt.Mnemonics.setLocalizedText(rbtnIndividualDesign, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.rbtnIndividualDesign.text")); // NOI18N
         rbtnIndividualDesign.setToolTipText(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.rbtnIndividualDesign.toolTipText")); // NOI18N
 
         buttonGroupDesign.add(rbtnUseSameDesign);
         rbtnUseSameDesign.setSelected(true);
         org.openide.awt.Mnemonics.setLocalizedText(rbtnUseSameDesign, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.rbtnUseSameDesign.text")); // NOI18N
         rbtnUseSameDesign.setToolTipText(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.rbtnUseSameDesign.toolTipText")); // NOI18N
 
         buttonGroupDesign.add(rBtnImportLayoutFile);
         org.openide.awt.Mnemonics.setLocalizedText(rBtnImportLayoutFile, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.rBtnImportLayoutFile.text")); // NOI18N
         rBtnImportLayoutFile.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 rBtnImportLayoutFileItemStateChanged(evt);
             }
         });
 
         org.openide.awt.Mnemonics.setLocalizedText(lblFileName, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.lblFileName.text")); // NOI18N
 
         txtFileName.setText(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.txtFileName.text")); // NOI18N
         txtFileName.setEnabled(false);
         txtFileName.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 txtFileNameMouseReleased(evt);
             }
         });
 
         org.openide.awt.Mnemonics.setLocalizedText(btnBrowse, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.btnBrowse.text")); // NOI18N
         btnBrowse.setEnabled(false);
         btnBrowse.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnBrowseActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout pnlExpDesignDesignLayout = new javax.swing.GroupLayout(pnlExpDesignDesign);
         pnlExpDesignDesign.setLayout(pnlExpDesignDesignLayout);
         pnlExpDesignDesignLayout.setHorizontalGroup(
             pnlExpDesignDesignLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlExpDesignDesignLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlExpDesignDesignLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(pnlExpDesignDesignLayout.createSequentialGroup()
                         .addGroup(pnlExpDesignDesignLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(pnlExpDesignDesignLayout.createSequentialGroup()
                                 .addComponent(rBtnImportLayoutFile)
                                 .addGap(0, 279, Short.MAX_VALUE))
                             .addGroup(pnlExpDesignDesignLayout.createSequentialGroup()
                                 .addGap(21, 21, 21)
                                 .addComponent(lblFileName)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(txtFileName)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(btnBrowse)))
                         .addContainerGap())
                     .addGroup(pnlExpDesignDesignLayout.createSequentialGroup()
                         .addComponent(rbtnIndividualDesign)
                         .addContainerGap())
                     .addGroup(pnlExpDesignDesignLayout.createSequentialGroup()
                         .addComponent(rbtnUseSameDesign, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addGap(54, 54, 54))))
         );
         pnlExpDesignDesignLayout.setVerticalGroup(
             pnlExpDesignDesignLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlExpDesignDesignLayout.createSequentialGroup()
                 .addComponent(rbtnUseSameDesign)
                 .addGap(3, 3, 3)
                 .addComponent(rbtnIndividualDesign)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(rBtnImportLayoutFile)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(pnlExpDesignDesignLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblFileName)
                     .addComponent(txtFileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnBrowse))
                 .addContainerGap())
         );
 
         javax.swing.GroupLayout pnlExperimConditionsFilterLayout = new javax.swing.GroupLayout(pnlExperimConditionsFilter);
         pnlExperimConditionsFilter.setLayout(pnlExperimConditionsFilterLayout);
         pnlExperimConditionsFilterLayout.setHorizontalGroup(
             pnlExperimConditionsFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlExperimConditionsFilterLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jRadioButtonFilterTrialStudy)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(pnlExpConditionTrial)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jSpinnerTrialStudy, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(jRadioButtonViewAllTrialStudy)
                 .addGap(45, 45, 45)
                 .addComponent(jButtonRefreshDesign, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(pnlExpDesignDesign, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jLabelEntries)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jTextFieldEntries, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
         pnlExperimConditionsFilterLayout.setVerticalGroup(
             pnlExperimConditionsFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlExperimConditionsFilterLayout.createSequentialGroup()
                 .addGap(28, 28, 28)
                 .addGroup(pnlExperimConditionsFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jTextFieldEntries)
                     .addComponent(jLabelEntries))
                 .addGap(46, 46, 46))
             .addGroup(pnlExperimConditionsFilterLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlExperimConditionsFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jButtonRefreshDesign)
                     .addGroup(pnlExperimConditionsFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addComponent(jSpinnerTrialStudy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGroup(pnlExperimConditionsFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jRadioButtonFilterTrialStudy)
                             .addComponent(pnlExpConditionTrial))
                         .addComponent(jRadioButtonViewAllTrialStudy, javax.swing.GroupLayout.Alignment.TRAILING))
                     .addComponent(pnlExpDesignDesign, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
 
         pnlExperimConditionsFilterLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jLabelEntries, jTextFieldEntries});
 
         pnlExperimConditionsFilterLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jSpinnerTrialStudy, pnlExpConditionTrial});
 
         jTableDesign.setModel(new DesignTableModel());
         jTableDesign.getTableHeader().setReorderingAllowed(false);
         jTableDesign.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
             public void propertyChange(java.beans.PropertyChangeEvent evt) {
                 jTableDesignPropertyChange(evt);
             }
         });
         jScrollPane8.setViewportView(jTableDesign);
 
         javax.swing.GroupLayout pnlExperimentalDesignLayout = new javax.swing.GroupLayout(pnlExperimentalDesign);
         pnlExperimentalDesign.setLayout(pnlExperimentalDesignLayout);
         pnlExperimentalDesignLayout.setHorizontalGroup(
             pnlExperimentalDesignLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlExperimentalDesignLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlExperimentalDesignLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jScrollPane8, javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(pnlExperimConditionsFilter, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         pnlExperimentalDesignLayout.setVerticalGroup(
             pnlExperimentalDesignLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlExperimentalDesignLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(pnlExperimConditionsFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 1373, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         jTabbedPaneEditor.addTab(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.pnlExperimentalDesign.TabConstraints.tabTitle"), pnlExperimentalDesign); // NOI18N
 
         pnlTraits.setMaximumSize(new java.awt.Dimension(1000, 650));
         pnlTraits.setMinimumSize(new java.awt.Dimension(1000, 650));
 
         jPanelTraits.setPreferredSize(new java.awt.Dimension(900, 650));
 
         jTextFieldDescription.setEditable(false);
         jTextFieldDescription.setText(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jTextFieldDescription.text")); // NOI18N
 
         jTextFieldDescriptionSelected.setEditable(false);
         jTextFieldDescriptionSelected.setText(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jTextFieldDescriptionSelected.text")); // NOI18N
 
         org.openide.awt.Mnemonics.setLocalizedText(jLabel13, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jLabel13.text")); // NOI18N
 
         jPanel9.setBorder(javax.swing.BorderFactory.createEtchedBorder());
 
         jButtonSelectTraits.setFont(new java.awt.Font("Lucida Grande", 1, 18)); // NOI18N
         jButtonSelectTraits.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ibfb/studyeditor/images/searchDB.png"))); // NOI18N
         org.openide.awt.Mnemonics.setLocalizedText(jButtonSelectTraits, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jButtonSelectTraits.text")); // NOI18N
         jButtonSelectTraits.setToolTipText(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jButtonSelectTraits.toolTipText")); // NOI18N
         jButtonSelectTraits.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonSelectTraitsActionPerformed(evt);
             }
         });
 
         jButtonSync.setFont(new java.awt.Font("Lucida Grande", 1, 18)); // NOI18N
         jButtonSync.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ibfb/studyeditor/images/sync.png"))); // NOI18N
         org.openide.awt.Mnemonics.setLocalizedText(jButtonSync, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jButtonSync.text")); // NOI18N
         jButtonSync.setToolTipText(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jButtonSync.toolTipText")); // NOI18N
         jButtonSync.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonSyncActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
         jPanel9.setLayout(jPanel9Layout);
         jPanel9Layout.setHorizontalGroup(
             jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel9Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jButtonSync)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jButtonSelectTraits, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         jPanel9Layout.setVerticalGroup(
             jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel9Layout.createSequentialGroup()
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jButtonSelectTraits, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButtonSync, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))
         );
 
         javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
         jPanel12.setLayout(jPanel12Layout);
         jPanel12Layout.setHorizontalGroup(
             jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 100, Short.MAX_VALUE)
         );
         jPanel12Layout.setVerticalGroup(
             jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 100, Short.MAX_VALUE)
         );
 
         javax.swing.GroupLayout jPanelTraitsLayout = new javax.swing.GroupLayout(jPanelTraits);
         jPanelTraits.setLayout(jPanelTraitsLayout);
         jPanelTraitsLayout.setHorizontalGroup(
             jPanelTraitsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanelTraitsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanelTraitsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanelTraitsLayout.createSequentialGroup()
                         .addComponent(jTextFieldDescription, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
                         .addGap(162, 162, 162)
                         .addGroup(jPanelTraitsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jTextFieldDescriptionSelected, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                             .addComponent(jLabel13)))
                     .addGroup(jPanelTraitsLayout.createSequentialGroup()
                         .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 777, Short.MAX_VALUE))
                     .addComponent(pnlSelectList, javax.swing.GroupLayout.DEFAULT_SIZE, 949, Short.MAX_VALUE))
                 .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
         jPanelTraitsLayout.setVerticalGroup(
             jPanelTraitsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelTraitsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanelTraitsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanelTraitsLayout.createSequentialGroup()
                         .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(pnlSelectList, javax.swing.GroupLayout.DEFAULT_SIZE, 1373, Short.MAX_VALUE))
                     .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanelTraitsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelTraitsLayout.createSequentialGroup()
                         .addComponent(jLabel13)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jTextFieldDescriptionSelected, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(jTextFieldDescription, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
 
         javax.swing.GroupLayout pnlTraitsLayout = new javax.swing.GroupLayout(pnlTraits);
         pnlTraits.setLayout(pnlTraitsLayout);
         pnlTraitsLayout.setHorizontalGroup(
             pnlTraitsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanelTraits, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1069, Short.MAX_VALUE)
         );
         pnlTraitsLayout.setVerticalGroup(
             pnlTraitsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanelTraits, javax.swing.GroupLayout.DEFAULT_SIZE, 1532, Short.MAX_VALUE)
         );
 
         jTabbedPaneEditor.addTab(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.pnlTraits.TabConstraints.tabTitle"), pnlTraits); // NOI18N
 
         jPanel21.setPreferredSize(new java.awt.Dimension(900, 650));
 
         jScrollPane7.setAutoscrolls(true);
         jScrollPane7.setMinimumSize(new java.awt.Dimension(0, 0));
         jScrollPane7.setPreferredSize(new java.awt.Dimension(450, 400));
 
         jTableObservations.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
 
             }
         ));
         jTableObservations.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
         jTableObservations.getTableHeader().setReorderingAllowed(false);
         jScrollPane7.setViewportView(jTableObservations);
 
         pnlMeasurementFilter.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.pnlMeasurementFilter.border.title"))); // NOI18N
 
         buttonGroupMeasurements.add(jRadioButtonFilterTrial);
         org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonFilterTrial, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jRadioButtonFilterTrial.text")); // NOI18N
         jRadioButtonFilterTrial.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 jRadioButtonFilterTrialItemStateChanged(evt);
             }
         });
 
         buttonGroupMeasurements.add(jRadioButtonAllTrials2);
         jRadioButtonAllTrials2.setSelected(true);
         org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonAllTrials2, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jRadioButtonAllTrials2.text")); // NOI18N
         jRadioButtonAllTrials2.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 jRadioButtonAllTrials2ItemStateChanged(evt);
             }
         });
 
         jSpinnerTrial.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jSpinnerTrialStateChanged(evt);
             }
         });
 
         buttonGroupMeasurements.add(jRadioButtonFilterEntry);
         org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonFilterEntry, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jRadioButtonFilterEntry.text")); // NOI18N
         jRadioButtonFilterEntry.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 jRadioButtonFilterEntryItemStateChanged(evt);
             }
         });
 
         jSpinnerEntry.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jSpinnerEntryStateChanged(evt);
             }
         });
 
         org.openide.awt.Mnemonics.setLocalizedText(lblTrialName, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.lblTrialName.text")); // NOI18N
 
         jTextTrialName.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 jTextTrialNameKeyReleased(evt);
             }
         });
 
         javax.swing.GroupLayout pnlMeasurementFilterLayout = new javax.swing.GroupLayout(pnlMeasurementFilter);
         pnlMeasurementFilter.setLayout(pnlMeasurementFilterLayout);
         pnlMeasurementFilterLayout.setHorizontalGroup(
             pnlMeasurementFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlMeasurementFilterLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jRadioButtonFilterTrial)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jSpinnerTrial, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(31, 31, 31)
                 .addComponent(jRadioButtonFilterEntry)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jSpinnerEntry, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(lblTrialName)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jTextTrialName, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 375, Short.MAX_VALUE)
                 .addComponent(jRadioButtonAllTrials2)
                 .addContainerGap())
         );
         pnlMeasurementFilterLayout.setVerticalGroup(
             pnlMeasurementFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlMeasurementFilterLayout.createSequentialGroup()
                 .addGroup(pnlMeasurementFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jRadioButtonFilterTrial)
                     .addComponent(jSpinnerTrial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jRadioButtonFilterEntry)
                     .addGroup(pnlMeasurementFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jSpinnerEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(lblTrialName)
                         .addComponent(jTextTrialName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(jRadioButtonAllTrials2))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         pnlMeasurementFilterLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jRadioButtonFilterEntry, jSpinnerEntry});
 
         pnlMeasurementFilterLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jRadioButtonFilterTrial, jSpinnerTrial});
 
         jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jPanel5.border.title"))); // NOI18N
         jPanel5.setPreferredSize(new java.awt.Dimension(187, 85));
 
         btnPrintLabels.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ibfb/studyeditor/images/print24.png"))); // NOI18N
         org.openide.awt.Mnemonics.setLocalizedText(btnPrintLabels, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.btnPrintLabels.text")); // NOI18N
         btnPrintLabels.setToolTipText(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.btnPrintLabels.toolTipText")); // NOI18N
         btnPrintLabels.setEnabled(false);
         btnPrintLabels.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnPrintLabels.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnPrintLabels.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnPrintLabelsActionPerformed(evt);
             }
         });
 
         jButtonSaveData.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ibfb/studyeditor/images/save.png"))); // NOI18N
         org.openide.awt.Mnemonics.setLocalizedText(jButtonSaveData, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jButtonSaveData.text")); // NOI18N
         jButtonSaveData.setToolTipText(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jButtonSaveData.toolTipText")); // NOI18N
         jButtonSaveData.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jButtonSaveData.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jButtonSaveData.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonSaveDataActionPerformed(evt);
             }
         });
 
         jButtonExportData.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ibfb/studyeditor/images/export.png"))); // NOI18N
         org.openide.awt.Mnemonics.setLocalizedText(jButtonExportData, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jButtonExportData.text")); // NOI18N
         jButtonExportData.setToolTipText(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jButtonExportData.toolTipText")); // NOI18N
         jButtonExportData.setEnabled(false);
         jButtonExportData.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jButtonExportData.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jButtonExportData.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonExportDataActionPerformed(evt);
             }
         });
 
         jButtonImportData.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ibfb/studyeditor/images/csvFile.png"))); // NOI18N
         org.openide.awt.Mnemonics.setLocalizedText(jButtonImportData, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jButtonImportData.text")); // NOI18N
         jButtonImportData.setToolTipText(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jButtonImportData.toolTipText")); // NOI18N
         jButtonImportData.setEnabled(false);
         jButtonImportData.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jButtonImportData.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jButtonImportData.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonImportDataActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
         jPanel5.setLayout(jPanel5Layout);
         jPanel5Layout.setHorizontalGroup(
             jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel5Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jButtonSaveData, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(btnPrintLabels, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                     .addComponent(jButtonExportData, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jButtonImportData, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         jPanel5Layout.setVerticalGroup(
             jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel5Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jButtonSaveData)
                 .addGap(18, 18, 18)
                 .addComponent(btnPrintLabels, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(jButtonExportData)
                 .addGap(18, 18, 18)
                 .addComponent(jButtonImportData)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jPanel5Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnPrintLabels, jButtonExportData, jButtonImportData, jButtonSaveData});
 
         javax.swing.GroupLayout jPanel21Layout = new javax.swing.GroupLayout(jPanel21);
         jPanel21.setLayout(jPanel21Layout);
         jPanel21Layout.setHorizontalGroup(
             jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel21Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(pnlMeasurementFilter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(jPanel21Layout.createSequentialGroup()
                         .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         jPanel21Layout.setVerticalGroup(
             jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel21Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(pnlMeasurementFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 1435, Short.MAX_VALUE)
                     .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 1435, Short.MAX_VALUE))
                 .addContainerGap())
         );
 
         javax.swing.GroupLayout pnlMeasurementLayout = new javax.swing.GroupLayout(pnlMeasurement);
         pnlMeasurement.setLayout(pnlMeasurementLayout);
         pnlMeasurementLayout.setHorizontalGroup(
             pnlMeasurementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel21, javax.swing.GroupLayout.DEFAULT_SIZE, 1069, Short.MAX_VALUE)
         );
         pnlMeasurementLayout.setVerticalGroup(
             pnlMeasurementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel21, javax.swing.GroupLayout.DEFAULT_SIZE, 1532, Short.MAX_VALUE)
         );
 
         jTabbedPaneEditor.addTab(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.pnlMeasurement.TabConstraints.tabTitle"), pnlMeasurement); // NOI18N
 
         jPanel1.setEnabled(false);
 
         jScrollPane9.setAutoscrolls(true);
         jScrollPane9.setMinimumSize(new java.awt.Dimension(0, 0));
         jScrollPane9.setPreferredSize(new java.awt.Dimension(450, 400));
 
         jTableMaster.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
 
             }
         ));
         jTableMaster.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
         jTableMaster.getTableHeader().setReorderingAllowed(false);
         jScrollPane9.setViewportView(jTableMaster);
 
         jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jPanel6.border.title"))); // NOI18N
         jPanel6.setPreferredSize(new java.awt.Dimension(187, 85));
 
         jButtonSaveMaster.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ibfb/studyeditor/images/save.png"))); // NOI18N
         org.openide.awt.Mnemonics.setLocalizedText(jButtonSaveMaster, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jButtonSaveMaster.text")); // NOI18N
         jButtonSaveMaster.setToolTipText(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jButtonSaveMaster.toolTipText")); // NOI18N
         jButtonSaveMaster.setEnabled(false);
         jButtonSaveMaster.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jButtonSaveMaster.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jButtonSaveMaster.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonSaveMasterActionPerformed(evt);
             }
         });
 
         jButtonExportMaster.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ibfb/studyeditor/images/export.png"))); // NOI18N
         org.openide.awt.Mnemonics.setLocalizedText(jButtonExportMaster, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jButtonExportMaster.text")); // NOI18N
         jButtonExportMaster.setToolTipText(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jButtonExportMaster.toolTipText")); // NOI18N
         jButtonExportMaster.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jButtonExportMaster.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jButtonExportMaster.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonExportMasterActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
         jPanel6.setLayout(jPanel6Layout);
         jPanel6Layout.setHorizontalGroup(
             jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel6Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jButtonSaveMaster, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jButtonExportMaster, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                 .addContainerGap())
         );
         jPanel6Layout.setVerticalGroup(
             jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel6Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jButtonSaveMaster, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(jButtonExportMaster, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(1130, Short.MAX_VALUE))
         );
 
         jPanel6Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jButtonExportMaster, jButtonSaveMaster});
 
         pnlMeasurementFilter1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.pnlMeasurementFilter1.border.title"))); // NOI18N
 
         buttonGroupMaster.add(jRadioButtonFilterTrialMaster);
         org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonFilterTrialMaster, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jRadioButtonFilterTrialMaster.text")); // NOI18N
         jRadioButtonFilterTrialMaster.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 jRadioButtonFilterTrialMasterItemStateChanged(evt);
             }
         });
 
         buttonGroupMaster.add(jRadioButtonAllTrialsMaster);
         jRadioButtonAllTrialsMaster.setSelected(true);
         org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonAllTrialsMaster, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jRadioButtonAllTrialsMaster.text")); // NOI18N
         jRadioButtonAllTrialsMaster.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 jRadioButtonAllTrialsMasterItemStateChanged(evt);
             }
         });
 
         jSpinnerTrialMaster.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jSpinnerTrialMasterStateChanged(evt);
             }
         });
 
         buttonGroupMaster.add(jRadioButtonFilterEntry1);
         org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonFilterEntry1, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jRadioButtonFilterEntry1.text")); // NOI18N
         jRadioButtonFilterEntry1.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 jRadioButtonFilterEntry1ItemStateChanged(evt);
             }
         });
 
         jSpinnerEntry1.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jSpinnerEntry1StateChanged(evt);
             }
         });
 
         org.openide.awt.Mnemonics.setLocalizedText(lblTrialNameMaster, org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.lblTrialNameMaster.text")); // NOI18N
 
         jTextTrialNameMaster.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 jTextTrialNameMasterKeyReleased(evt);
             }
         });
 
         javax.swing.GroupLayout pnlMeasurementFilter1Layout = new javax.swing.GroupLayout(pnlMeasurementFilter1);
         pnlMeasurementFilter1.setLayout(pnlMeasurementFilter1Layout);
         pnlMeasurementFilter1Layout.setHorizontalGroup(
             pnlMeasurementFilter1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlMeasurementFilter1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jRadioButtonFilterTrialMaster)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jSpinnerTrialMaster, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(31, 31, 31)
                 .addComponent(jRadioButtonFilterEntry1)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jSpinnerEntry1, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(lblTrialNameMaster)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jTextTrialNameMaster, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 375, Short.MAX_VALUE)
                 .addComponent(jRadioButtonAllTrialsMaster)
                 .addContainerGap())
         );
         pnlMeasurementFilter1Layout.setVerticalGroup(
             pnlMeasurementFilter1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlMeasurementFilter1Layout.createSequentialGroup()
                 .addGroup(pnlMeasurementFilter1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jRadioButtonFilterTrialMaster)
                     .addComponent(jSpinnerTrialMaster, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jRadioButtonFilterEntry1)
                     .addGroup(pnlMeasurementFilter1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jSpinnerEntry1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(lblTrialNameMaster)
                         .addComponent(jTextTrialNameMaster, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(jRadioButtonAllTrialsMaster))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(pnlMeasurementFilter1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(pnlMeasurementFilter1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, 1445, Short.MAX_VALUE)
                     .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 1445, Short.MAX_VALUE))
                 .addContainerGap())
         );
 
         jTabbedPaneEditor.addTab(org.openide.util.NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jTabbedPaneEditor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(jTabbedPaneEditor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void jTableTrialConditionsPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jTableTrialConditionsPropertyChange
         if (this.jTableTrialConditions.getCellEditor() != null) {
             jTableTrialConditions.getCellEditor().stopCellEditing();
         }
         this.fila = this.jTableTrialConditions.getEditingRow();
         if (fila == -1) {
             return;
         }
         try {
             if (evt.getOldValue() == null) {
 
                 System.out.println("NO CAMBIO VALOR \n");
                 return;
             }
 
 
             if (evt.getNewValue() == null) {
                 Object valor = this.jTableTrialConditions.getValueAt(this.fila, 5).toString();
                 if (valor.equals("")) {
                     return;
                 }
                 System.out.println("FILA: " + fila);
                 System.out.println("Valor: " + valor + "\n");
                 Object condition = this.jTableTrialConditions.getValueAt(this.fila, 1).toString().toUpperCase();
                 Object property = this.jTableTrialConditions.getValueAt(this.fila, TRIAL_PROPERTY_COL).toString().toUpperCase();
                 Object scale = this.jTableTrialConditions.getValueAt(this.fila, TRIAL_SCALE_COL).toString().toUpperCase();
                 int instancia = Integer.parseInt(this.jTableTrialConditions.getValueAt(this.fila, TRIAL_INSTANCE_COL).toString());
                 if (property.equals(LookupUtil.PERSON) && scale.equals(LookupUtil.DBID)) {
                     LookupUtil.lookupPersonName(jTableTrialConditions, instancia, valor, TRIAL_PROPERTY_COL, TRIAL_SCALE_COL, TRIAL_VALUE_COL);
                 }
 
                 if (property.equals(LookupUtil.PERSON) && scale.equals(LookupUtil.DBCV)) {
                     LookupUtil.lookupPersonId(jTableTrialConditions, instancia, valor, TRIAL_PROPERTY_COL, TRIAL_SCALE_COL, TRIAL_VALUE_COL);
                 }
 
                 if (property.equals(LookupUtil.LOCATION) && scale.equals(LookupUtil.DBID)) {
                     LookupUtil.lookupLocationName(jTableTrialConditions, instancia, valor, TRIAL_PROPERTY_COL, TRIAL_SCALE_COL, TRIAL_VALUE_COL);
                 }
                 if (property.equals(LookupUtil.LOCATION) && scale.equals(LookupUtil.DBCV)) {
                     LookupUtil.lookupLocationId(jTableTrialConditions, instancia, valor, TRIAL_PROPERTY_COL, TRIAL_SCALE_COL, TRIAL_VALUE_COL);
                 }
 
 
                 if (condition.equals("PDATE-1 LATE")) {
                     DateUtil.compareDates(instancia, jTableTrialConditions, fila);
                     return;
                 }
 
                 if (condition.equals("PDATE-2 LATE")) {
                     DateUtil.compareDates(instancia, jTableTrialConditions, fila);
                     return;
                 }
             }
 
         } catch (NullPointerException error) {
         }
     }//GEN-LAST:event_jTableTrialConditionsPropertyChange
 
     private void jRadioButtonFilterTrialInfoItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonFilterTrialInfoItemStateChanged
         filterTrialInformationByTrial();
 
 }//GEN-LAST:event_jRadioButtonFilterTrialInfoItemStateChanged
 
     private void jRadioButtonAllTrialsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonAllTrialsItemStateChanged
         if (this.jRadioButtonAllTrials.isSelected()) {
             sorterTrialInformation.setRowFilter(null);
         }
 }//GEN-LAST:event_jRadioButtonAllTrialsItemStateChanged
 
     private void jSpinnerTrialInformationStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerTrialInformationStateChanged
         filterTrialInformationByTrial();
 }//GEN-LAST:event_jSpinnerTrialInformationStateChanged
 
     private void jRadioButtonFilterExpConditionsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonFilterExpConditionsItemStateChanged
         filterExperimentalConditionsByTrial();
 
 }//GEN-LAST:event_jRadioButtonFilterExpConditionsItemStateChanged
 
     private void jRadioButtonAllExpConditionItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonAllExpConditionItemStateChanged
         if (this.jRadioButtonAllExpCondition.isSelected()) {
             sorterConstants.setRowFilter(null);
         }
 }//GEN-LAST:event_jRadioButtonAllExpConditionItemStateChanged
 
     private void jSpinnerExpConditionsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerExpConditionsStateChanged
         filterExperimentalConditionsByTrial();
 }//GEN-LAST:event_jSpinnerExpConditionsStateChanged
 
     private void jRadioButtonFilterTrialStudyItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonFilterTrialStudyItemStateChanged
         filterDesignsByTrial();
 
 }//GEN-LAST:event_jRadioButtonFilterTrialStudyItemStateChanged
 
     private void jSpinnerTrialStudyStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerTrialStudyStateChanged
         filterDesignsByTrial();
 
 }//GEN-LAST:event_jSpinnerTrialStudyStateChanged
 
     private void jRadioButtonViewAllTrialStudyItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonViewAllTrialStudyItemStateChanged
         if (this.jRadioButtonViewAllTrialStudy.isSelected()) {
             sorterDesign.setRowFilter(null);
         }
 }//GEN-LAST:event_jRadioButtonViewAllTrialStudyItemStateChanged
 
     private void asignaClipboard() {
         this.jTableObservations.addKeyListener(new Clipboard((jTableObservations)));
     }
     private void jTableDesignPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jTableDesignPropertyChange
         if (this.jTableDesign.getCellEditor() != null) {
             jTableDesign.getCellEditor().stopCellEditing();
         }
 
         int colEditing = this.jTableDesign.getEditingColumn();
 
         this.fila = this.jTableDesign.getEditingRow();
 
         if (fila == -1) {
             return;
         }
 
         try {
             if (evt.getOldValue() == null) {
                 return;
             }
             if (evt.getNewValue() == null) {
                 designsUtils.checkDesignTable(colEditing, fila, rbtnUseSameDesign.isSelected());
             }
         } catch (NullPointerException error) {
         }
 
         JTableUtils.ajustaColumnsTable(jTableDesign, 2);
 
 }//GEN-LAST:event_jTableDesignPropertyChange
 
     private void jRadioButtonFilterTrialItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonFilterTrialItemStateChanged
         filterObservationsByTrial();
 
 }//GEN-LAST:event_jRadioButtonFilterTrialItemStateChanged
 
     private void jRadioButtonAllTrials2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonAllTrials2ItemStateChanged
         if (this.jRadioButtonAllTrials2.isSelected()) {
             sorterMeasurements.setRowFilter(null);
         }
 }//GEN-LAST:event_jRadioButtonAllTrials2ItemStateChanged
 
     private void jSpinnerTrialStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerTrialStateChanged
         filterObservationsByTrial();
 }//GEN-LAST:event_jSpinnerTrialStateChanged
 
     private void jRadioButtonFilterEntryItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonFilterEntryItemStateChanged
         filterObservationsByEntry();
 }//GEN-LAST:event_jRadioButtonFilterEntryItemStateChanged
 
     private void jSpinnerEntryStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerEntryStateChanged
         filterObservationsByEntry();
 }//GEN-LAST:event_jSpinnerEntryStateChanged
 
     private void filterTrialInformationByTrial() {
         if (this.jRadioButtonFilterTrialInfo.isSelected()) {
             int num = (Integer) this.jSpinnerTrialInformation.getValue();
             try {
                 sorterTrialInformation.setRowFilter(RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, num, 0));
             } catch (PatternSyntaxException pse) {
                 System.err.println("Bad regex pattern");
             }
         }
     }
 
     private void filterExperimentalConditionsByTrial() {
         if (this.jRadioButtonFilterExpConditions.isSelected()) {
             int num = (Integer) this.jSpinnerExpConditions.getValue();
             try {
                 sorterConstants.setRowFilter(RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, num, 0));
             } catch (PatternSyntaxException pse) {
                 System.err.println("Bad regex pattern");
             }
         }
     }
 
     private void filterDesignsByTrial() {
         if (this.jRadioButtonFilterTrialStudy.isSelected()) {
             int num = (Integer) this.jSpinnerTrialStudy.getValue();
             try {
                 sorterDesign.setRowFilter(RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, num, 0));
             } catch (PatternSyntaxException pse) {
                 System.err.println("Bad regex pattern");
             }
         }
     }
 
     public int getCROP() {
         return crop;
     }
 
     public void setCROP(int CROP) {
         this.crop = CROP;
     }
 
     private void filterObservationsByTrial() {
         if (this.jRadioButtonFilterTrial.isSelected()) {
             Integer num = (Integer) this.jSpinnerTrial.getValue();
             try {
                 ObservationsTableModel model = (ObservationsTableModel) jTableObservations.getModel();
                 sorterMeasurements = new TableRowSorter(model);
                 Integer colTrial = model.getHeaderIndex(ObservationsTableModel.TRIAL);
                 //sorterMeasurements.setRowFilter(RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, num, colTrial));
                 sorterMeasurements.setRowFilter(RowFilter.regexFilter(num.toString(), colTrial));
                 this.jTableObservations.setRowSorter(sorterMeasurements);
 
 
             } catch (PatternSyntaxException pse) {
                 System.err.println("Bad regex pattern");
             }
         }
     }
 
     private void filterObservationsByEntry() {
         if (this.jRadioButtonFilterEntry.isSelected()) {
             Integer num = (Integer) this.jSpinnerEntry.getValue();
             try {
                 ObservationsTableModel tableModel = (ObservationsTableModel) jTableObservations.getModel();
                 int colEntry = tableModel.getHeaderIndex(ObservationsTableModel.ENTRY);
                 //sorterMeasurements.setRowFilter(RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, num, colEntry));
                 sorterMeasurements.setRowFilter(RowFilter.regexFilter(num.toString(), colEntry));
             } catch (PatternSyntaxException pse) {
                 System.err.println("Bad regex pattern");
             }
         }
     }
 
     private void exportToExcel() {
         List<Constant> selectedConstants = WorkbookSavingHelper.getSelectedConstants(this);
 
         FieldBookExcelExporter excelExporter;
 
         if (isForMaster()) {
             excelExporter = new FieldBookExcelExporter(jTableMaster, fileTemplate, trialFile, study, myWorkbook, selectedConstants);
         } else {
             excelExporter = new FieldBookExcelExporter(jTableObservations, fileTemplate, trialFile, study, myWorkbook, selectedConstants);
 
         }
 
 
 
         excelExporter.setStudyConditionsTable(this.jTableStudyConditions);
         excelExporter.setTrialConditionsTable(this.jTableTrialConditions);
         excelExporter.setConstantsTable(this.jTableConstants);
         excelExporter.exportToExcel(triallOption, trialStart, trialEnd, trialSelected);
     }
 
     public boolean hasGY() {
         boolean hasGY = false;
         ObservationsTableModel modeloOriginal = (ObservationsTableModel) jTableObservations.getModel();
         if (modeloOriginal.findColumn(getStringTraitToEvaluate()) >= 0) {
             hasGY = true;
         }
         return hasGY;
     }
 
     public boolean hasGYbyDefault() {
         boolean hasGY = false;
         ObservationsTableModel modeloOriginal = (ObservationsTableModel) jTableObservations.getModel();
         if (modeloOriginal.findColumn("GY") >= 0) {
             hasGY = true;
         }
         return hasGY;
     }
 
     private void jButtonExportDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExportDataActionPerformed
         storeCellsInEditMode();
         this.setForMaster(false);
         switch (this.getCROP()) {
             case CROP.WHEAT:
                 NbPreferences.forModule(exportWizardPanelGYTrait.class).put("traitIndex", "-1");
                 if (!iniciaExportWizardStandar()) {
                     putTraitIndexDefault();
                 }
                 break;
 
             case CROP.MAIZE:
                 iniciaExportForMaize();
                 break;
 
             case CROP.OTHERCROPS:
                 NbPreferences.forModule(exportWizardPanelGYTrait.class).put("traitIndex", "-1");
                 if (!iniciaExportWizardStandar()) {
                     putTraitIndexDefault();
                 }
 
                 break;
 
         }
 
 }//GEN-LAST:event_jButtonExportDataActionPerformed
 
     private void exportToFieldlog() {
         FieldbookCSVExporter.exportToFieldlog(jTableObservations, trialFile, csv, triallOption, trialStart, trialEnd, trialSelected);
     }
 
     private void putTraitIndexDefault() {
         this.setStringTraitToEvaluate("GY");
         NbPreferences.forModule(exportWizardPanelGYTrait.class).put("traitIndex", "-1");
     }
 
     private void jButtonImportDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonImportDataActionPerformed
         //launchImportWizard();
         if (!launchImportWizard()) {
             return;
         }
 }//GEN-LAST:event_jButtonImportDataActionPerformed
 
     private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
         excludeColumns();
         //fillMeasurementsData();
     }//GEN-LAST:event_jMenuItem1ActionPerformed
 
     private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
 
         includeColumns();
         // fillMeasurementsData();
 
     }//GEN-LAST:event_jMenuItem2ActionPerformed
 
     private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
         //EXCLUDE
         int columna = 5;
 
         if (jTableConstants.getCellEditor() != null) {
             jTableConstants.getCellEditor().stopCellEditing(); //para el cellEdior
         }
 
         int seleccion[] = this.jTableConstants.getSelectedRows();
 
         for (int i = 0; i < seleccion.length; i++) {
             int j = seleccion[i];
             this.jTableConstants.setValueAt(false, j, columna);
 
         }
     }//GEN-LAST:event_jMenuItem3ActionPerformed
 
     private boolean loadGY() {
         WizardDescriptor wiz = new WizardDescriptor(new GYwizardWizardIterator());
         // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
         // {1} will be replaced by WizardDescriptor.Iterator.name()
         wiz.setTitleFormat(new MessageFormat("{0} ({1})"));
         wiz.setTitle(NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.gytitle"));
         if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
             return true;
         } else {
             return false;
         }
     }
 
     private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
         // INCLUDE
         int columna = 5;
 
         if (jTableConstants.getCellEditor() != null) {
             jTableConstants.getCellEditor().stopCellEditing(); //para el cellEdior
         }
 
         int seleccion[] = this.jTableConstants.getSelectedRows();
 
         for (int i = 0; i < seleccion.length; i++) {
             int j = seleccion[i];
             this.jTableConstants.setValueAt(true, j, columna);
 
         }
 }//GEN-LAST:event_jMenuItem4ActionPerformed
 
     private void jButtonSyncActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSyncActionPerformed
         if (!studyAlreadyExists) {
             fillObservationsData();
         } else {
             fillExistingObservationsData();
         }
 
         DialogUtil.displayInfo(StudyEditorTopComponent.class, "StudyEditorTopComponent.datasyncronized");
 
         this.jTabbedPaneEditor.setSelectedIndex(7);
 
     }//GEN-LAST:event_jButtonSyncActionPerformed
 
     private void jButtonSaveDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveDataActionPerformed
         closingEditor = true;
         saveFieldbook();
         return;
     }//GEN-LAST:event_jButtonSaveDataActionPerformed
 
     private void saveFieldbook() {
         storeCellsInEditMode();
         if (jTextTrialName.getText().trim().isEmpty()) {
             DialogUtil.displayError(NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.fillName"));
             jTextTrialName.requestFocus();
             return;
         }
         if (!studyAlreadyExists && trialNameAlreadyExists(jTextTrialName.getText().trim())) {
             String message = NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.trialNameAlreadyExists");
             DialogUtil.displayError(message);
             return;
         }
 
         String studyName = jTextFieldStudy.getText();
         FieldbookCSVUtil fieldbookCSVUtil = new FieldbookCSVUtil(jTableObservations, studyName);
         fieldbookCSVUtil.saveToCsv();
         final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.saving"));
         handle.start();
         (new SwingWorker<String, Object>() {
 
             @Override
             protected String doInBackground() throws Exception {
                 doSaveWorkbook();
 
                 return "";
             }
 
             @Override
             protected void done() {
                 super.done();
                 try {
                     String valor = get();
                     DialogUtil.displayInfo(NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.saved"));
                     enableMeasurementButtons();
                     RefreshBrowserHelper.refreshStudyBrowser();
 
                     if (!closingEditor) {
                         reLoadStudy();
                     }
 
 
                 } catch (InterruptedException ex) {
                     Exceptions.printStackTrace(ex);
                 } catch (ExecutionException ex) {
                     Exceptions.printStackTrace(ex);
                 } finally {
                     // close the progress handler
                     handle.finish();
 
                 }
             }
         }).execute();
 
 
     }
 
     private void reLoadStudy() {
 
         org.cimmyt.cril.ibwb.domain.Study studyAfterSave;
         studyAfterSave = AppServicesProxy.getDefault().appServices().getStudyByName(this.jTextTrialName.getText());
 
 
         Workbook workbook = null;
 
         try {
             System.out.println("BUSCANDO:" + studyAfterSave.getStudyid());
             workbook = AppServicesProxy.getDefault().appServices().getWorkbookFull(new Integer(studyAfterSave.getStudyid()), false);
 
 
         } catch (Exception e) {
 
             e.printStackTrace();
         }
 
 
 
         try {
 
             if (workbook == null) {
                 changeCursorWaitStatus(false);
                 //   String msgSaving = bundle.getString("OpenStudyAction.cannot");
 
                 //      NotifyDescriptor d = new NotifyDescriptor.Message(msgSaving, NotifyDescriptor.ERROR_MESSAGE);
                 //    DialogDisplayer.getDefault().notify(d);
 
             } else {
                 StudyEditorTopComponent studyWindow = new StudyEditorTopComponent();
                 studyWindow.setStudy(workbook.getStudy());
                 studyWindow.getjTextTrialName().setText(workbook.getStudy().getStudy());
                 studyWindow.setStudyAlreadyExists(true);
                 fillStudyData(studyWindow, workbook.getStudy());
                 studyWindow.assignStudyConditions(workbook.getStudyConditions());
 
                 fillTraits(studyWindow, workbook);
 
                 studyWindow.assignTrialConditions(workbook.getConditionsData());
 
                 studyWindow.assignGermplasmEntries(workbook.getEntryFactors(), workbook.getGermplasmData());
                 studyWindow.assignExperimentalConditions(workbook.getConstants());
                 studyWindow.loadDataFromDB();
 
                 inhabilitaTabs(studyWindow);
 
                 studyWindow.open();
                 studyWindow.requestActive();
                 changeCursorWaitStatus(false);
                 //     DialogUtil.displayInfo(bundle.getString("OpenStudyAction.opened"));
             }
         } catch (Exception ex) {
             Exceptions.printStackTrace(ex);
         }
 
 
         this.setConPreguntas(false);
         cierraTopComponent(this.getName());
 
     }
 
     public void cierraTopComponent(String topName) {
 
         try {
             ArrayList<TopComponent> opened = new ArrayList<TopComponent>(WindowManager.getDefault().getRegistry().getOpened());
 
             for (TopComponent t : opened) {
 
                 if (t.getName().equals(topName)) {
 
                     t.close();
                 }
             }
         } catch (NullPointerException ex) {
         }
 
     }
 
     private void inhabilitaTabs(StudyEditorTopComponent studyWindow) {
         studyWindow.disableTraitsSelection();
         studyWindow.jTabbedPaneEditor.setEnabledAt(5, false);
         studyWindow.jTabbedPaneEditor.setEnabledAt(4, false);
     }
 
     private void fillTraits(StudyEditorTopComponent studyWindow, Workbook workbook) {
         studyWindow.assignTraits(new ArrayList<Variate>(), workbook.getVariates());
         studyWindow.setMyWorkbook(workbook);
         studyWindow.setSelectedTraits(workbook.getVariates());
     }
 
     private void fillStudyData(StudyEditorTopComponent studyWindow, ibfb.domain.core.Study study) {
         studyWindow.setName(study.getStudy());
         studyWindow.jTextFieldStudy.setText(study.getStudy());
         studyWindow.jTextFieldObjective.setText(study.getObjective());
         studyWindow.jTextFieldTitle.setText(study.getTitle());
         Date start = study.getStarDate();
         Date end = study.getEndDate();
 
         String formato = ConvertUtils.DATE_PATTERN; //"dd-MMM-yyyy";
         SimpleDateFormat sdf = new SimpleDateFormat(formato);
 
 
         try {
             studyWindow.jDateChooserStart.setDate(start);
         } catch (NullPointerException ex) {
         }
 
         try {
             studyWindow.jDateChooserEnd.setDate(end);
         } catch (NullPointerException ex) {
         }
         try {
             studyWindow.jTextFieldPMKey.setText(myWorkbook.getStudy().getPmkey());
         } catch (NullPointerException ex) {
         }
         try {
             studyWindow.jComboBoxStudyType.setSelectedItem(myWorkbook.getStudy().getStudyType());
         } catch (NullPointerException ex) {
             studyWindow.jComboBoxStudyType.setSelectedItem(0);
         }
 
 
     }
 
     private void doSaveWorkbook() {
         WorkbookSavingHelper.saveFieldbook(this);
         disableTraitsSelection();
         jTextTrialName.setEnabled(false);
 
     }
 
     private void jButtonRefreshDesignActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRefreshDesignActionPerformed
         int instances = this.jTableDesign.getRowCount();
         for (int i = 0; i < instances; i++) {
             try {
                 if (jTableDesign.getValueAt(i, 2).toString().isEmpty()) {
                     JOptionPane.showMessageDialog(null, NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.fillFields"), "IBFIELDBOOK", JOptionPane.WARNING_MESSAGE);
                     return;
                 }
                 if (jTableDesign.getValueAt(i, 3).toString().isEmpty()) {
                     JOptionPane.showMessageDialog(null, NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.fillFields"), "IBFIELDBOOK", JOptionPane.WARNING_MESSAGE);
                     return;
                 }
                 if (jTableDesign.getValueAt(i, 4).toString().isEmpty()) {
                     JOptionPane.showMessageDialog(null, NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.fillFields"), "IBFIELDBOOK", JOptionPane.WARNING_MESSAGE);
                     return;
                 }
             } catch (NullPointerException ex) {
                 JOptionPane.showMessageDialog(null, NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.fillFields"), "IBFIELDBOOK", JOptionPane.WARNING_MESSAGE);
                 return;
             }
         }
 
         int opcion = JOptionPane.showConfirmDialog(null, NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.modifyDesign"), NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.caution"), JOptionPane.YES_NO_OPTION);
         if (opcion == 0) {
             final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.generating"));
             handle.start();
             (new SwingWorker<String, Object>() {
 
                 @Override
                 protected String doInBackground() throws Exception {
                     //fillMeasurementsData();
                     fillObservationsData();
                     return "";
                 }
 
                 @Override
                 protected void done() {
                     super.done();
                     try {
                         String valor = get();
                     } catch (InterruptedException ex) {
                         Exceptions.printStackTrace(ex);
                     } catch (ExecutionException ex) {
                         Exceptions.printStackTrace(ex);
                     } finally {
                         // close the progress handler
                         handle.finish();
                     }
                 }
             }).execute();
 
         }
 
 
     }//GEN-LAST:event_jButtonRefreshDesignActionPerformed
 
     private void btnPrintLabelsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintLabelsActionPerformed
         printLabels();
     }//GEN-LAST:event_btnPrintLabelsActionPerformed
 
     private void jTableStudyConditionsPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jTableStudyConditionsPropertyChange
         if (this.jTableStudyConditions.getCellEditor() != null) {
             jTableStudyConditions.getCellEditor().stopCellEditing();
         }
 
 
         this.fila = this.jTableStudyConditions.getEditingRow();
 
         if (fila == -1) {
             return;
         }
 
         try {
             if (evt.getOldValue() == null) {
 
                 System.out.println("NO CAMBIO VALOR \n");
                 return;
             }
 
 
             if (evt.getNewValue() == null) {
 
                 Object valor = this.jTableStudyConditions.getValueAt(this.fila, STUDY_VALUE_COL).toString();
                 if (valor.equals("")) {
                     return;
                 }
                 System.out.println("FILA: " + fila);
                 System.out.println("Valor: " + valor + "\n");
 
                 //Object condition = this.jTableStudyConditions.getValueAt(this.fila, 0).toString().toUpperCase();
                 Object property = this.jTableStudyConditions.getValueAt(this.fila, STUDY_PROPERTY_COL).toString().toUpperCase();
                 Object scale = this.jTableStudyConditions.getValueAt(this.fila, STUDY_SCALE_COL).toString().toUpperCase();
                 if (property.equals(LookupUtil.PERSON) && scale.equals(LookupUtil.DBID)) {
                     LookupUtil.lookupPersonName(jTableStudyConditions, valor, STUDY_PROPERTY_COL, STUDY_SCALE_COL, STUDY_VALUE_COL);
                 }
 
                 if (property.equals(LookupUtil.PERSON) && scale.equals(LookupUtil.DBCV)) {
                     LookupUtil.lookupPersonId(jTableStudyConditions, valor, STUDY_PROPERTY_COL, STUDY_SCALE_COL, STUDY_VALUE_COL);
                 }
 
                 if (property.equals(LookupUtil.LOCATION) && scale.equals(LookupUtil.DBID)) {
                     LookupUtil.lookupLocationName(jTableStudyConditions, valor, STUDY_PROPERTY_COL, STUDY_SCALE_COL, STUDY_VALUE_COL);
                 }
                 if (property.equals(LookupUtil.LOCATION) && scale.equals(LookupUtil.DBCV)) {
                     LookupUtil.lookupLocationId(jTableStudyConditions, valor, STUDY_PROPERTY_COL, STUDY_SCALE_COL, STUDY_VALUE_COL);
                 }
 
 
             }
 
 
         } catch (NullPointerException error) {
         }
     }//GEN-LAST:event_jTableStudyConditionsPropertyChange
 
     private void importFromCSV() {
         FileFilter[] filtros = new FileFilter[10];
         filtros = selectorArchivo.getChoosableFileFilters();
         for (int i = 0; i < filtros.length; i++) {
             FileFilter filtro = filtros[i];
             selectorArchivo.removeChoosableFileFilter(filtro);
         }
         File archivoNulo = new File("");
         selectorArchivo.setSelectedFile(archivoNulo);
         selectorArchivo.setFileSelectionMode(JFileChooser.FILES_ONLY);
         selectorArchivo.addChoosableFileFilter(new CSVFiltro());
         int resultado = selectorArchivo.showOpenDialog(null);
         if (resultado == JFileChooser.CANCEL_OPTION) {
             return;
         }
         if (!csv.isValid(selectorArchivo.getSelectedFile())) {
             DialogUtil.displayError(NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.fileInvalid"));
             return;
         }
 
 
         csv.readDATAnew(selectorArchivo.getSelectedFile());
     }
 
     public void fillDesignWorkbook() {
 
         designsUtils.setWorkbook(myWorkbook);
         designsUtils.setGermplasmEntries(Integer.parseInt(this.jTextFieldEntries.getText()));
     }
 
     public void fillDesign() {
 
         boolean hasBLOCKfactor = false;
         boolean hasREPLICATIONfactor = false;
         boolean hasFIELDfactorfactor = false;
         boolean hasCOLfactorfactor = false;
         boolean hasROWfactorfactor = false;
 
         DesignTableModel modeloTabla = new DesignTableModel();
         jTableDesign.setModel(modeloTabla);
         modeloTabla = (DesignTableModel) this.jTableDesign.getModel();
         jTableDesign.setRowSorter(null);
 
         int instances = Integer.parseInt(jLabelInstances.getText());
 
         Integer renglon = 0;
         for (int j = 0; j < instances; j++) {
             DesignBean designBean = new DesignBean();
             designBean.setTrialNumber(j + 1);
             modeloTabla.addDesignBean(designBean);
             renglon++;
         }
         //tcr.setHorizontalAlignment(SwingConstants.CENTER);
 
         if (this.jTableDesign.getRowCount() > 0) {
             this.jTableDesign.setRowSelectionInterval(0, 0);
 
 
         }
 
         int square = (int) Math.sqrt(Integer.parseInt(this.jTextFieldEntries.getText()));
         boolean conLattice = false;
         boolean conAlpha = false;
         boolean conRCBD = false;
         boolean conUnreplicated = false;
         boolean conIndividual = false;
 
         boolean hayFactores = !myWorkbook.getOtherFactors().isEmpty();
 
         if (!hayFactores) {
             conAlpha = true;
         }
 
         int numEntries = jTableEntries.getRowCount();
         if (org.cimmyt.cril.ibwb.commongui.MathUtils.isPrime(numEntries)) {
             conAlpha = false;
         }
 
 
         if (Math.pow(square, 2) == Integer.parseInt(this.jTextFieldEntries.getText())) {
             if (hasCOLfactorfactor && hasROWfactorfactor) {
                 conLattice = true;
             } else {
                 conLattice = false;
             }
         }
 
         conAlpha = designsUtils.alphaIsValid(numEntries);
 
         for (int i = 0; i < this.getMyWorkbook().getConditions().size(); i++) {
             if (this.getMyWorkbook().getConditions().get(i).getProperty().toUpperCase().equals("TRIAL INSTANCE")) {
                 if (this.getMyWorkbook().getConditions().get(i).getScale().toUpperCase().equals("NUMBER")) {
                     conIndividual = true;
                 } else if (this.getMyWorkbook().getConditions().get(i).getScale().toUpperCase().equals("NESTED NUMBER")) {
                     conIndividual = true;
                 }
                 break;
             }
         }
 
         for (int i = 0; i < this.getMyWorkbook().getFactors().size(); i++) {
 
             if (this.getMyWorkbook().getFactors().get(i).getProperty().equals("FIELD PLOT")) {
                 hasFIELDfactorfactor = true;
             }
 
             if (this.getMyWorkbook().getFactors().get(i).getProperty().equals("BLOCK")) {
                 hasBLOCKfactor = true;
             }
 
             if (this.getMyWorkbook().getFactors().get(i).getProperty().equals("REPLICATION")) {
                 hasREPLICATIONfactor = true;
             }
 
             if (this.getMyWorkbook().getFactors().get(i).getProperty().equals("COLUMN")) {
                 hasCOLfactorfactor = true;
             }
 
             if (this.getMyWorkbook().getFactors().get(i).getProperty().equals("ROW")) {
                 hasROWfactorfactor = true;
             }
         }
 
 
         if (conAlpha && hasBLOCKfactor) {
             conAlpha = true;
         } else {
             conAlpha = false;
         }
 
 
         if (hasREPLICATIONfactor) {
             conRCBD = true;
         }
 
 
         if (hasFIELDfactorfactor) {
             conUnreplicated = true;
         }
 
 
 
         String inicio = designsUtils.assignMainCellEditor(conAlpha, conLattice, conRCBD, conUnreplicated, conIndividual);
 
         for (int j = 0; j < instances; j++) {
             this.jTableDesign.setValueAt(inicio, j, 1);
             this.jTableDesign.setValueAt(null, j, 2);
             this.jTableDesign.setValueAt(null, j, 3);
             this.jTableDesign.setValueAt(null, j, 4);
             //this.jTableDesign.setValueAt(null, j, 5);
         }
 
         SpinnerModel modeloDesign = jSpinnerTrial.getModel();
         jSpinnerTrialStudy.setModel(modeloDesign);
 
         if (conAlpha) {
             int selected = designsUtils.assignCellEditorAlpha(numEntries);
             designsUtils.generateBlocksSize(selected);
             designsUtils.assignCellEditorBlockSize();
         } else {
             checkTable();
         }
 
         sorterDesign = new TableRowSorter<TableModel>(modeloTabla);
         this.jTableDesign.setRowSorter(sorterDesign);
     }
 
     private void checkTable() {
         if (this.jTableDesign.getCellEditor() != null) {
             jTableDesign.getCellEditor().stopCellEditing();
         }
 
         int colEditing = 1;
         this.fila = 0;
 
         try {
             designsUtils.checkDesignTable(colEditing, fila, rbtnUseSameDesign.isSelected());
         } catch (NullPointerException error) {
             System.out.println("ERROR EN TRIAL VISUAL PANEL 9: " + error);
         }
     }
 
     private void exportToR() {
         if (isForMaster()) {
             FieldbookRExport.exportToR(jTableMaster, trialFile, csv, triallOption, trialStart, trialEnd, trialSelected, this.getStringTraitToEvaluate());
 
         } else {
             FieldbookRExport.exportToR(jTableObservations, trialFile, csv, triallOption, trialStart, trialEnd, trialSelected, this.getStringTraitToEvaluate());
 
         }
     }
 
     private void jMenuItemUnSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemUnSelectActionPerformed
 }//GEN-LAST:event_jMenuItemUnSelectActionPerformed
 
     private void jMenuItemUnselectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemUnselectAllActionPerformed
 }//GEN-LAST:event_jMenuItemUnselectAllActionPerformed
 
     private void jMenuItemSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSelectActionPerformed
     }//GEN-LAST:event_jMenuItemSelectActionPerformed
 
     private void jMenuItemSelectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSelectAllActionPerformed
 }//GEN-LAST:event_jMenuItemSelectAllActionPerformed
 
     private void jButtonSelectTraitsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectTraitsActionPerformed
         TopComponent traitsExplorer = WindowManager.getDefault().findTopComponent("TraitsExplorerTopComponent");
         if (traitsExplorer == null) {
             traitsExplorer = new TraitsExplorerTopComponent();
         }
         traitsExplorer.open();
         TraitsExplorerTopComponent traitsExplorerTopComponent = (TraitsExplorerTopComponent) traitsExplorer;
         traitsExplorerTopComponent.updateTraitsTable();
         traitsExplorer.requestActive();
 }//GEN-LAST:event_jButtonSelectTraitsActionPerformed
 
     private void jTextTrialNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextTrialNameKeyReleased
         studyInfo.setStudy(jTextTrialName.getText());
         //jButtonSaveToExcel.setEnabled(!jTextTrialName.getText().trim().isEmpty());
     }//GEN-LAST:event_jTextTrialNameKeyReleased
 
     private void jTableEntriesPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jTableEntriesPropertyChange
         this.jLabelTotalGermp.setText(String.valueOf(this.jTableEntries.getRowCount()));
     }//GEN-LAST:event_jTableEntriesPropertyChange
 
     private void jButtonSaveMasterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveMasterActionPerformed
     }//GEN-LAST:event_jButtonSaveMasterActionPerformed
 
     private void jButtonExportMasterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExportMasterActionPerformed
         this.setForMaster(true);
 
         NbPreferences.forModule(exportWizardPanelGYTrait.class).put("traitIndex", "-1");
         if (!iniciaExportWizardStandar()) {
             putTraitIndexDefault();
         }
 
 
 
     }//GEN-LAST:event_jButtonExportMasterActionPerformed
 
     private void jRadioButtonFilterTrialMasterItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonFilterTrialMasterItemStateChanged
     }//GEN-LAST:event_jRadioButtonFilterTrialMasterItemStateChanged
 
     private void jRadioButtonAllTrialsMasterItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonAllTrialsMasterItemStateChanged
     }//GEN-LAST:event_jRadioButtonAllTrialsMasterItemStateChanged
 
     private void jSpinnerTrialMasterStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerTrialMasterStateChanged
     }//GEN-LAST:event_jSpinnerTrialMasterStateChanged
 
     private void jRadioButtonFilterEntry1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonFilterEntry1ItemStateChanged
     }//GEN-LAST:event_jRadioButtonFilterEntry1ItemStateChanged
 
     private void jSpinnerEntry1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerEntry1StateChanged
     }//GEN-LAST:event_jSpinnerEntry1StateChanged
 
     private void jTextTrialNameMasterKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextTrialNameMasterKeyReleased
     }//GEN-LAST:event_jTextTrialNameMasterKeyReleased
 
     private void jTabbedPaneEditorStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPaneEditorStateChanged
         switch (jTabbedPaneEditor.getSelectedIndex()) {
             case 8://MASTER SHEET
                 storeCellsInEditMode();
                 //---- IMPORTANT!!!!
                 // disable temporarilly TRANSFORMATIONS TABLES, SINCE BETA 45
                 //---- IMPORTANT!!!!        
 
                 //calculateMasterData();
                 break;
 
         }
     }//GEN-LAST:event_jTabbedPaneEditorStateChanged
 
     private void jDateChooserStartInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_jDateChooserStartInputMethodTextChanged
    }//GEN-LAST:event_jDateChooserStartInputMethodTextChanged
 
     private void jDateChooserStartPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jDateChooserStartPropertyChange
         if (evt.getPropertyName().equals("date")) {
 
             Date startValue = jDateChooserStart.getDate();
             Date endValue = jDateChooserEnd.getDate();
 
             try {
 
                 if (startValue.compareTo(endValue) > 0) {
 
                     NotifyDescriptor d = new NotifyDescriptor.Message(NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.late"), NotifyDescriptor.INFORMATION_MESSAGE);
                     DialogDisplayer.getDefault().notify(d);
                     jDateChooserStart.setDate(null);
                 }
             } catch (NullPointerException e) {
                 System.out.println("ERROR FECHAS " + e);
             }
         }
     }//GEN-LAST:event_jDateChooserStartPropertyChange
 
     private void jDateChooserEndInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_jDateChooserEndInputMethodTextChanged
    }//GEN-LAST:event_jDateChooserEndInputMethodTextChanged
 
     private void jDateChooserEndPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jDateChooserEndPropertyChange
 
         if (evt.getPropertyName().equals("date")) {
 
 
             Date startValue = jDateChooserStart.getDate();
             Date endValue = jDateChooserEnd.getDate();
 
             try {
 
                 if (startValue.compareTo(endValue) > 0) {
 
                     NotifyDescriptor d = new NotifyDescriptor.Message(NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.late"), NotifyDescriptor.INFORMATION_MESSAGE);
                     DialogDisplayer.getDefault().notify(d);
                     jDateChooserEnd.setDate(null);
                 }
             } catch (NullPointerException e) {
                 System.out.println("ERROR FECHAS " + e);
             }
         }
     }//GEN-LAST:event_jDateChooserEndPropertyChange
 
     private void rBtnImportLayoutFileItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rBtnImportLayoutFileItemStateChanged
         validateDesignButtons();
     }//GEN-LAST:event_rBtnImportLayoutFileItemStateChanged
 
     private void txtFileNameMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtFileNameMouseReleased
         importLayoutFromFile();
     }//GEN-LAST:event_txtFileNameMouseReleased
 
     private void btnBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseActionPerformed
         importLayoutFromFile();
     }//GEN-LAST:event_btnBrowseActionPerformed
 
     @Override
     public void componentOpened() {
     }
 
     public void defineTabs() {
         switch (this.getCROP()) {
             case CROP.WHEAT:
                if(jTabbedPaneEditor.getTabCount() > 8)
                    this.jTabbedPaneEditor.removeTabAt(8); //remove MASTER tab
                    // this.jTabbedPaneEditor.removeTabAt(7); //remove MASTER tab
 
 
                 break;
             case CROP.MAIZE:
 
 
                 break;
 
             case CROP.OTHERCROPS:
                if(jTabbedPaneEditor.getTabCount() > 8)
                    this.jTabbedPaneEditor.removeTabAt(8); //remove MASTER tab
                    //this.jTabbedPaneEditor.removeTabAt(8); //remove MASTER tab
 
                 break;
 
         }
     }
 
     @Override
     public void componentClosed() {
     }
 
     void writeProperties(java.util.Properties p) {
         p.setProperty("version", "1.0");
     }
 
     void readProperties(java.util.Properties p) {
         String version = p.getProperty("version");
     }
 
     private void assignCellEditorTrialCndition() {
         TrialConditionsRowEditor trialConditionsRowEditor = new TrialConditionsRowEditor(jTableTrialConditions);
         TableColumn valueColumn = this.jTableTrialConditions.getColumnModel().getColumn(5);
         valueColumn.setCellEditor(trialConditionsRowEditor);
 
         TableColumn columnValue = jTableTrialConditions.getColumnModel().getColumn(5);
         DefaultTableCellRenderer valueCellRenderer = new DefaultTableCellRenderer();
 
         valueCellRenderer.setToolTipText(NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.fillValue"));
         valueCellRenderer.setBackground(Color.YELLOW);
         columnValue.setCellRenderer(valueCellRenderer);
     }
 
     private void assignCellEditorStudyConditions() {
         ConditionsRowEditor trialConditionsRowEditor = new ConditionsRowEditor(jTableStudyConditions);
         TableColumn valueColumn = this.jTableStudyConditions.getColumnModel().getColumn(4);
         valueColumn.setCellEditor(trialConditionsRowEditor);
 
         TableColumn columnValue = jTableStudyConditions.getColumnModel().getColumn(4);
         DefaultTableCellRenderer valueCellRenderer = new DefaultTableCellRenderer();
         valueCellRenderer.setToolTipText(NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.fillValue"));
         valueCellRenderer.setBackground(Color.YELLOW);
         columnValue.setCellRenderer(valueCellRenderer);
     }
 
     public void assignDesignModel(DesignTableModel designTableModel) {
         DesignTableModel modeloTabla = new DesignTableModel();
         jTableDesign.setModel(modeloTabla);
         modeloTabla = (DesignTableModel) this.jTableDesign.getModel();
         jTableDesign.setRowSorter(null);
         for (DesignBean designBean : designTableModel.getDesignList()) {
             modeloTabla.addDesignBean(designBean);
         }
 
         int square = (int) Math.sqrt(Integer.parseInt(this.jTextFieldEntries.getText()));
         boolean conLattice = false;
         boolean conAlpha = false;
         boolean conRCBD = false;
         boolean conUnreplicated = false;
         boolean conIndividual = false;
 
 
         boolean hasBLOCKfactor = false;
         boolean hasREPLICATIONfactor = false;
         boolean hasFIELDfactorfactor = false;
         boolean hasCOLfactorfactor = false;
         boolean hasROWfactorfactor = false;
 
 
         boolean hayFactores = !myWorkbook.getOtherFactors().isEmpty();
 
         if (!hayFactores) {
             conAlpha = true;
         }
 
         int numEntries = jTableEntries.getRowCount();
 
 
 
         for (int i = 0; i < this.getMyWorkbook().getStudyConditions().size(); i++) {
             if (this.getMyWorkbook().getConditions().get(i).getProperty().toUpperCase().equals("TRIAL INSTANCE")) {
                 if (this.getMyWorkbook().getConditions().get(i).getScale().toUpperCase().equals("NUMBER")) {
                     conIndividual = true;
                 } else if (this.getMyWorkbook().getConditions().get(i).getScale().toUpperCase().equals("NESTED NUMBER")) {
                     conIndividual = true;
                 }
                 break;
             }
 
         }
 
         for (int i = 0; i < this.getMyWorkbook().getFactors().size(); i++) {
 
             if (this.getMyWorkbook().getFactors().get(i).getProperty().toUpperCase().equals("FIELD PLOT")) {
                 hasFIELDfactorfactor = true;
             }
 
             if (this.getMyWorkbook().getFactors().get(i).getProperty().toUpperCase().equals("BLOCK")) {
                 hasBLOCKfactor = true;
             }
 
             if (this.getMyWorkbook().getFactors().get(i).getProperty().toUpperCase().equals("REPLICATION")) {
                 hasREPLICATIONfactor = true;
             }
 
             if (this.getMyWorkbook().getFactors().get(i).getProperty().toUpperCase().equals("COLUMN")) {
                 hasCOLfactorfactor = true;
             }
 
             if (this.getMyWorkbook().getFactors().get(i).getProperty().toUpperCase().equals("ROW")) {
                 hasROWfactorfactor = true;
             }
         }
 
 
 
 
         if (org.cimmyt.cril.ibwb.commongui.MathUtils.isPrime(numEntries)) {
             conAlpha = false;
         }
 
 
         if (Math.pow(square, 2) == Integer.parseInt(this.jTextFieldEntries.getText())) {
             if (hasCOLfactorfactor && hasROWfactorfactor) {
                 conLattice = true;
             } else {
                 conLattice = false;
             }
         }
 
 
         if (designsUtils.alphaIsValid(numEntries)) {
 
             conAlpha = true;
         } else {
             conAlpha = false;
         }
 
 
         if (conAlpha && hasBLOCKfactor) {
             conAlpha = true;
         } else {
             conAlpha = false;
         }
 
         if (hasREPLICATIONfactor) {
             conRCBD = true;
         }
 
 
         if (hasFIELDfactorfactor) {
             conUnreplicated = true;
         }
 
 
         designsUtils.assignMainCellEditor(conAlpha, conLattice, conRCBD, conUnreplicated, conIndividual);
 
         SpinnerModel modeloDesign = jSpinnerTrial.getModel();
         jSpinnerTrialStudy.setModel(modeloDesign);
 
         if (conAlpha) {
             int selected = designsUtils.assignCellEditorAlpha(numEntries);
             designsUtils.generateBlocksSize(selected);
             //designsUtils.assignCellEditorBlockSize(designsUtils.getRep2(),designsUtils.getRep3(),designsUtils.getRep4());
 
             designsUtils.assignCellEditorBlockSize();
         }
 //        else {
 //            checkTable();
 //        }
 
         sorterDesign = new TableRowSorter<TableModel>(modeloTabla);
         this.jTableDesign.setRowSorter(sorterDesign);
         deshabilitaSorters();
 
     }
 
     private void MSG_NotImplementedYet() {
         DialogUtil.displayInfo("Not yet implemented");
     }
 
     private void includeColumns() {
         int opcion = JOptionPane.showConfirmDialog(null, "Are you sure to include this Traits? The -Measurements Table- will be modified ", "Caution!", JOptionPane.YES_NO_OPTION);
         if (opcion == 0) {
             int total = this.jTableObservations.getColumnCount();
             TableColumn myColumn = new TableColumn();
             myColumn.setHeaderValue("name");
             this.jTableObservations.addColumn(myColumn);
         }
     }
 
     private void cleanValuesForColumn(int col) {
         System.out.println("BORRAR COL " + col);
         for (int k = 0; k < jTableObservations.getRowCount(); k++) {
             this.jTableObservations.setValueAt("", k, col);
         }
     }
 
     private void excludeColumns() {
         int opcion = JOptionPane.showConfirmDialog(null, "Are you sure to exclude this Traits? The -Measurements Table- will be modified ", "Caution!", JOptionPane.YES_NO_OPTION);
         if (opcion == 0) {
         }
     }
 
     public void fillObservationsData() {
         List<Variate> selectedVariates = doubleListPanel.getTargetList();
         this.setSelectedTraits(selectedVariates);
 
         ObservationsTableModel tableModel = null;
         TreatmentLabelsTableModel otftm = (TreatmentLabelsTableModel) jTableOtherFactorLabels.getModel();
         DesignsGenerator designsGenerator = new DesignsGenerator(this.jTableEntries, this.jTextFieldEntries, myWorkbook, otftm);
 
         tableModel = new ObservationsTableModel(myWorkbook, selectedVariates);
 
         jTableObservations.setModel(tableModel);
         sorterMeasurements = new TableRowSorter<TableModel>(tableModel);
         this.jTableObservations.setRowSorter(sorterMeasurements);
 
         DesignTableModel designTableModel = (DesignTableModel) jTableDesign.getModel();
 
         int trials = 0;
         String rep = "";
         String blockSize = "";
         String blocksPerRep = "";
         Integer entries = 0;
         try {
             //trials = this.jTableDesign.getRowCount();
             trials = designTableModel.getRowCount();
             System.out.println("TRIAL FILLOB DATA: " + trials);
         } catch (NullPointerException ex) {
             trials = 0;
         }
         // tmsanchez
         // this because there is not other factors!
         if (this.myWorkbook.getOtherFactors().isEmpty()) {
             combinations = 1;
         } else {
 //            combinations = Integer.parseInt(myWorkbook.getOtherFactors().get(0).getValue().toString());
 //            int levels = 0;
 //            for (int index=1; index < myWorkbook.getOtherFactors().size(); index++) {
 //                levels = Integer.parseInt(myWorkbook.getOtherFactors().get(index).getValue().toString());
 //                combinations = combinations * levels;
 //            }
             combinations = myWorkbook.getOtherFactors().size();
         }
 
         for (int i = 0; i < trials; i++) {
 
             DesignBean bean = designTableModel.getDesignBean(i);
             String disenio = bean.getDesign();
             rep = bean.getReplications().toString();
             blockSize = bean.getBlockSize().toString();
             blocksPerRep = bean.getBlocksPerReplicate().toString();
 
             entries = Integer.parseInt(blockSize) * (Integer.parseInt(blocksPerRep));
             if (disenio.startsWith(DesignsClass.USER_DEFINED_DESIGN)) {
 
 
                 if (bean.getUserDefinedDesign() == null) {
                     DialogUtil.displayError(NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.specify"));
                 } else {
                     disenios.readUserDefinedDesign(i + 1, bean.getUserDefinedDesign(), tableModel, this.jTableEntries);
                 }
             } else if (disenio.equals(DesignsClass.ALFA_DESIGN)) {
                 disenios.setWorkbook(myWorkbook);
                 if (OSUtils.isMacOS()) {
                     disenios.runR_alpha(i + 1, entries, Integer.parseInt(rep), Integer.parseInt(blockSize));
 
                     if (!disenios.existeArchivo("alpha")) {
 
                         try {
                             Thread.sleep(1500);
                         } catch (InterruptedException ex) {
                             System.out.println("ERROR EN HILO ESPERA " + ex);
                         }
                     }
 
                     disenios.readAlphaDesign(i + 1, "alpha", tableModel, jTableEntries);
                     disenios.deleteWDforMac();
                 } else {
                     //disenios.runR_alphaWindows(entries, Integer.parseInt(rep), Integer.parseInt(blockSize));
                     disenios.runR_alphaWindows(i + 1, entries, Integer.parseInt(rep), Integer.parseInt(blockSize));
 
                     if (disenios.existeArchivo("alpha")) {
                         disenios.readAlphaDesign(i + 1, "alpha", tableModel, this.jTableEntries);
                         disenios.deleteWD(new File("C:" + File.separator + "R"));
                     } else {
                         disenios.runR_alphaWindowsExtra(entries, Integer.parseInt(rep), Integer.parseInt(blockSize), 155);
                         disenios.readAlphaDesign(i + 1, "alpha", tableModel, this.jTableEntries);
                         disenios.deleteWD(new File("C:" + File.separator + "R"));
                     }
 
                 }
             } else if (disenio.equals(DesignsClass.LATICE_DESIGN)) {
                 if (OSUtils.isMacOS()) {
                     disenios.runR_lattice(i + 1, entries, Integer.parseInt(rep), Integer.parseInt(blockSize));
                     disenios.readLatticeDesign(i + 1, "lattice" + (i + 1), tableModel, this.jTableEntries, otherFactors, factorsDesignCad, combinations);
                     disenios.deleteWDforMac();
                 } else {
                     disenios.runR_latticeWindows(entries, Integer.parseInt(rep), Integer.parseInt(blockSize));
                     disenios.readLatticeDesign(i + 1, "lattice", tableModel, this.jTableEntries, otherFactors, factorsDesignCad, combinations);
                     disenios.deleteWD(new File("C:" + File.separator + "R"));
                 }
             } else if (disenio.equals(DesignsClass.UNREPLICATED_DESIGH_WITH_RANDOMIZATION)) {
                 designsGenerator.generateUnreplicatedDesignWithRandomization(i + 1, tableModel, combinations);
             } else if (disenio.equals(DesignsClass.UNREPLICATED_DESIGH_WITHOUT_RANDOMIZATION)) {
                 designsGenerator.generateUnreplicatedDesignWithoutRandomization(i + 1, tableModel, combinations);
             } else {
                 //rep = this.jTableDesign.getValueAt(i, 2).toString();
                 rep = bean.getReplications().toString();
                 designsGenerator.generateRandomizeCompleteBlock(Integer.parseInt(rep), i + 1, tableModel, otherFactors, factorsDesignCad, combinations);
             }
         }
         int entriesTot = this.jTableEntries.getRowCount();
         this.jTextFieldEntries.setText(String.valueOf(entriesTot));
         SpinnerNumberModel mod1 = new SpinnerNumberModel(1, 1, trials, 1);
         SpinnerNumberModel mod3 = new SpinnerNumberModel(1, 1, entriesTot, 1);
         this.jSpinnerTrial.setModel(mod1);
         this.jSpinnerEntry.setModel(mod3);
         ajustaColumnsTable(this.jTableObservations, 2);
         for (int i = 0; i < jTableObservations.getColumnCount(); i++) {
             sorterMeasurements.setSortable(i, false);
         }
 
 
         this.jTabbedPaneEditor.setEnabledAt(7, true);
         this.jTabbedPaneEditor.setSelectedIndex(7);
 
         ObservationTableTooltips.assignTooltips(jTableObservations);
         // asignaClipboard();
         // System.out.println("CROP ACTIVO MEASUREMENTES: "+this.getCROP());
         fillMasterData(selectedVariates);
         ajustaColumnsTable(this.jTableMaster, 2);
 
         changeCursorWaitStatus(false);
 
     }
 
     private void fillMasterData(List fieldbookVariates) {
         List<Variate> selectedVariates = doubleListPanel.getTargetList();
         ObservationsTableModel tableModel = (ObservationsTableModel) jTableObservations.getModel();
         List<Variate> fieldbookVariatesOK = new ArrayList<Variate>();
 
         for (int i = 0; i < fieldbookVariates.size(); i++) {
             fieldbookVariatesOK.add(selectedVariates.get(i));
         }
 
         masterWorkbook = myWorkbook;
         masterWorkbook.setVariates(fieldbookVariatesOK);
         ObservationsTableModel tableModelMaster = new ObservationsTableModel(masterWorkbook, fieldbookVariatesOK);
         tableModelMaster.setIsMasterSheet(true);
 
         for (int i = 0; i < tableModel.getRowCount(); i++) {
             List<Object> filaOb = tableModel.getRow(i);
             ArrayList<Object> nueva = new ArrayList(filaOb);
             tableModelMaster.addRow(nueva);
         }
         this.jTableMaster.setModel(tableModelMaster);
         ObservationTableTooltips.assignTooltips(jTableMaster);
     }
 
     private void fillDataWithFormulas(ObservationsTableModel tableModel, ObservationsTableModel tableModelMaster, List<Variate> fieldbookVariates, List<Variate> masterVariates) {
 
         MaizeFormulas formulas = new MaizeFormulas();
 
         for (int i = 0; i < fieldbookVariates.size(); i++) {
 
             boolean estaCompleta = false;
             int p1 = -1, p2 = -1, p3 = -1, p4 = -1;
 
             try {
                 p1 = tableModel.getHeaderIndex("GrainYieldKg_FieldWt");
                 p2 = tableModel.getHeaderIndex("GrainMoisturePer");
                 p3 = tableModel.getHeaderIndex("PlotSize");
                 p4 = tableModel.getHeaderIndex("ShellPer");
                 estaCompleta = true;
             } catch (Exception e) {
                 estaCompleta = false;
             }
 
             if (estaCompleta) {
 
                 for (int j = 0; j < tableModel.getRowCount(); j++) {
                     double v1 = Double.parseDouble(tableModel.getValueAt(j, p1).toString());
                     double v2 = Double.parseDouble(tableModel.getValueAt(j, p2).toString());
                     double v3 = Double.parseDouble(tableModel.getValueAt(j, p3).toString());
                     double v4 = Double.parseDouble(tableModel.getValueAt(j, p4).toString());
 
                     double valor = formulas.GrainYieldTons_FieldWt(v1, v2, v3, v4);
 
                 }
 
 
                 System.out.println("EXISTE GrainYieldTons_FieldWt");
 
 
             } else {
                 System.out.println("NO EXISTE GrainYieldTons_FieldWt");
             }
 
 
 
 
 
             DefaultTableColumnModel columnModel = (DefaultTableColumnModel) this.jTableMaster.getColumnModel();
             columnModel.addColumn(null);
 
         }
 
 
 // mGrainYieldTons_FieldWt
 //
 //GrainYieldKg_FieldWt
 //GrainMoisturePer
 //PlotSize
 //ShellPer
 
 
 
     }
 
     public int findColumn(String name, DefaultTableModel model) {
         int colEntry = 0;
         colEntry = model.findColumn(name);
         return colEntry;
 
     }
 
     public void printLabels() {
         // TraitsReportHelper.printTraitsReport(jTableObservations);
         List<String> fields = new ArrayList<String>();
         ObservationsTableModel model = (ObservationsTableModel) jTableObservations.getModel();
         for (Object header : model.getHeaders()) {
             if (header instanceof Factor) {
                 Factor factor = (Factor) header;
                 fields.add(factor.getFactorName());
             } else if (header instanceof Variate) {
                 Variate variate = (Variate) header;
                 fields.add(variate.getVariateName());
             }
 
         }
         LabelReportWizardIterator.launchLabelReportWizard(model.getValues(), fields);
     }
 
     private void quitaCellEditors() {
         JTextField jtf = new JTextField();
         TableColumn valueColumn = this.jTableDesign.getColumnModel().getColumn(2);
         valueColumn.setCellEditor(new DefaultCellEditor(jtf));
         valueColumn = this.jTableDesign.getColumnModel().getColumn(3);
         valueColumn.setCellEditor(new DefaultCellEditor(jtf));
         valueColumn = this.jTableDesign.getColumnModel().getColumn(4);
         valueColumn.setCellEditor(new DefaultCellEditor(jtf));
     }
 
     private void deshabilitaSorters() {
         for (int i = 0; i < 5; i++) {
             sorterTrialInformation.setSortable(i, false);
             sorterConstants.setSortable(i, false);
             sorterDesign.setSortable(i, false);
         }
     }
 
     public void ajustaColumnsTable(JTable table, int margin) {
         for (int c = 0; c < table.getColumnCount(); c++) {
             ajustaColumn(table, c, 2);
         }
     }
 
     public void ajustaColumn(JTable table, int vColIndex, int margin) {
         TableModel modelPack = table.getModel();
         DefaultTableColumnModel colModel = (DefaultTableColumnModel) table.getColumnModel();
         TableColumn col = colModel.getColumn(vColIndex);
         int width = 0;
         TableCellRenderer renderer = col.getHeaderRenderer();
         if (renderer == null) {
             renderer = table.getTableHeader().getDefaultRenderer();
         }
         Component comp = renderer.getTableCellRendererComponent(
                 table, col.getHeaderValue(), false, false, 0, 0);
         width = comp.getPreferredSize().width;
         for (int r = 0; r < table.getRowCount(); r++) {
             renderer = table.getCellRenderer(r, vColIndex);
             comp = renderer.getTableCellRendererComponent(
                     table, table.getValueAt(r, vColIndex), false, false, r,
                     vColIndex);
             width = Math.max(width, comp.getPreferredSize().width);
         }
         width += 2 * margin;
         col.setPreferredWidth(width);
     }
 
     private boolean hayGYcolumn() {
 
         TableColumnModel tableColumns = this.jTableObservations.getColumnModel();
 
         for (int col = 0; col < tableColumns.getColumnCount(); col++) {
             TableColumn tableColumn = tableColumns.getColumn(col);
             if (tableColumn.getHeaderValue().toString().equals(stringTraitToEvaluate)) {
                 return true;
             }
         }
         return false;
     }
 
     public void configMyList() {
         cleanFields();
         traits = (java.util.ArrayList<Variate>) myWorkbook.getVariates();
     }
 
     public void cleanFields() {
         jTextFieldDescription.setText("");
         jTextFieldDescriptionSelected.setText("");
     }
 
     public void cleanList() {
         listModelUnSelected.clear();
         listModelSelected.clear();
     }
 
     private static void changeCursorWaitStatus(final boolean isWaiting) {
         Mutex.EVENT.writeAccess(new Runnable() {
 
             @Override
             public void run() {
                 try {
                     JFrame mainFrame =
                             (JFrame) WindowManager.getDefault().getMainWindow();
                     Component glassPane = mainFrame.getGlassPane();
                     if (isWaiting) {
                         glassPane.setVisible(true);
                         glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                     } else {
                         glassPane.setVisible(false);
                         glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                     }
                 } catch (Exception e) {
                 }
             }
         });
     }
 
     private boolean launchImportWizard() {
         ObservationsTableModel observationsTableModel = (ObservationsTableModel) jTableObservations.getModel();
         csv = new CSVFileManager(this.jTableObservations, new JList());
         int instanceCounter = observationsTableModel.getTrialCounter();
         ImportData importData = new ImportData(jTableObservations, csv, instanceCounter);
 
         WizardDescriptor.Iterator iterator = new importingWizardIterator();
         WizardDescriptor wizardDescriptor = new WizardDescriptor(iterator);
         importingVisualPanel2.setWizardDescriptor(wizardDescriptor);
 
         wizardDescriptor.setTitleFormat(new MessageFormat("{0} ({1})"));
         wizardDescriptor.setTitle(NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.import"));
         Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
         dialog.setVisible(true);
         dialog.toFront();
         boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
         if (!cancelled) {
 
             switch (opcionImport) {
                 case 0:
                     importData.importFromExcel(trialImportFile);
 
                     break;
                 case 1:
                     importData.importFromFieldroid(trialImportFile);
                     break;
 
                 case 2:
                     importData.importFromCSV(trialImportFile);
                     break;
             }
             return true;
         } else {
             return false;
         }
     }
 
     private void iniciaExportForMaize() {
 
         WizardDescriptor wiz = new WizardDescriptor(new maizeExportWizardIterator());
         wiz.setTitleFormat(new MessageFormat("{0} ({1})"));
         wiz.setTitle(NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.save"));
         DialogUtil.createDialog(wiz);
 
         boolean cancelled = wiz.getValue() != WizardDescriptor.FINISH_OPTION;
         if (!cancelled) {
 
             switch (opcionExport) {
                 case 0:
                     exportToFieldlog();
                     break;
 
                 case 2:
                     exportToExcel();
                     break;
             }
 
         }
     }
 
     private boolean iniciaExportWizardStandar() {
 
         WizardDescriptor.Iterator iterator = new exportWizardIterator();
         WizardDescriptor wizardDescriptor = new WizardDescriptor(iterator);
         wizardDescriptor.setTitleFormat(new MessageFormat("{0} ({1})"));
         wizardDescriptor.setTitle(NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.save"));
         DialogUtil.createDialog(wizardDescriptor);
 
         boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
         if (!cancelled) {
 
             switch (opcionExport) {
 
                 case 0:
                     exportToFieldlog();
 
                     break;
                 case 1:
                     exportToR();
                     break;
 
                 case 2:
                     exportToExcel();
                     break;
                 case 3:
                     exportToDataKapture();
                     exportToDataKaptureTraits();
                     break;
             }
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * Export the trial to CSV
      *
      * @author Raul Hernandez Toledo
      * @since 1.0
      */
     private void exportToDataKapture() {
         FieldbookCSVExporter.exportToDataKapture(jTableObservations, trialFile, csv, triallOption, trialStart, trialEnd, trialSelected);
     }
 
     /**
      * Export traits from trial to CSV
      *
      * @author Raul Hernandez Toledo
      * @since 1.0
      */
     private void exportToDataKaptureTraits() {
         FieldbookCSVExporter.exportToDataKaptureTraits(jTableObservations, trialFile, csv, triallOption, trialStart, trialEnd, trialSelected);
     }
 
     public int getMaxTrial() {
         SpinnerNumberModel mod = (SpinnerNumberModel) this.jSpinnerTrial.getModel();
         Comparable num = mod.getMaximum();
         num.toString();
         return Integer.parseInt(num.toString());
     }
 
     public void loadDataFromCsv() {
         String studyName = jTextFieldStudy.getText();
         FieldbookCSVUtil fieldbookCSVUtil = new FieldbookCSVUtil(jTableObservations, studyName);
         fieldbookCSVUtil.populateFiedlbook(jTableObservations, studyName);
         SpinnerNumberModel mod = (SpinnerNumberModel) this.jSpinnerTrial.getModel();
         mod.setMaximum(getMaxTrialsFromDb());
     }
 
     private int getMaxTrialsFromDb() {
         int maxTrials = 1;
 
         DefaultTableModel dtm = (DefaultTableModel) jTableObservations.getModel();
         int currentTrial = Integer.parseInt((String) dtm.getValueAt(0, 0));
         for (int row = 0; row < dtm.getRowCount(); row++) {
             String value = (String) dtm.getValueAt(0, 0);
             int trial = Integer.parseInt(value);
             if (trial != currentTrial) {
                 maxTrials++;
                 currentTrial = trial;
             }
         }
         return maxTrials;
     }
 
     public void assignStudyConditions(List<Condition> studyConditions) {
         StudyConditionsTableModel studyConditionsTableModel = new StudyConditionsTableModel(studyConditions);
         jTableStudyConditions.setModel(studyConditionsTableModel);
         assignCellEditorStudyConditions();
         deshabilitaSorters();
     }
 
     public void assignTrialConditions(List<Condition> trialConditions) {
         TrialConditionsTableModel tableModel = new TrialConditionsTableModel(trialConditions);
         this.jTableTrialConditions.setModel(tableModel);
         sorterTrialInformation = new TableRowSorter<TableModel>(tableModel);
         this.jTableTrialConditions.setRowSorter(sorterTrialInformation);
         assignCellEditorTrialCndition();
         deshabilitaSorters();
     }
 
     public void assignExperimentalConditions(List<Constant> constantList) {
         ExperimentalConditionsTableModel tableModel = new ExperimentalConditionsTableModel(constantList);
         jTableConstants.setModel(tableModel);
         sorterConstants = new TableRowSorter<TableModel>(tableModel);
         jTableConstants.setRowSorter(sorterConstants);
 
         TableColumn columnValue = jTableConstants.getColumnModel().getColumn(4);
         DefaultTableCellRenderer valueCellRenderer = new DefaultTableCellRenderer();
         valueCellRenderer.setToolTipText(NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.fillValue"));
         valueCellRenderer.setBackground(Color.YELLOW);
         columnValue.setCellRenderer(valueCellRenderer);
 
         deshabilitaSorters();
     }
 
     public void assignGermplasmEntries(List<Factor> factorHeaders, List<List<Object>> germplasmData) {
         GermplasmEntriesTableModel tableModel = new GermplasmEntriesTableModel(factorHeaders, germplasmData);
         this.jTableEntries.setModel(tableModel);
         DefaultTableCellRenderer tcr = new DefaultTableCellRenderer();
         tcr.setHorizontalAlignment(SwingConstants.CENTER);
         for (int col = 0; col < this.jTableEntries.getColumnCount(); col++) {
             this.jTableEntries.getColumnModel().getColumn(col).setCellRenderer(tcr);
         }
     }
 
     /**
      * Assigns
      */
     public void assignOtherTreatmentFactors(List<Factor> otherTreatmentFactors) {
 
         OtherTreatmentFactorsTableModel model = new OtherTreatmentFactorsTableModel(otherTreatmentFactors, false);
         jTableOtherFactors.setModel(model);
     }
 
     public void assignOtherTreatmentFactorLabels(List<FactorLabel> factorLabels) {
 
         TreatmentLabelsTableModel treatmentLabelsTableModel = new TreatmentLabelsTableModel();
         treatmentLabelsTableModel.setFactorLabels(factorLabels);
         jTableOtherFactorLabels.setModel(treatmentLabelsTableModel);
         TableColumn columnValue = jTableOtherFactorLabels.getColumnModel().getColumn(3);
         DefaultTableCellRenderer valueCellRenderer = new DefaultTableCellRenderer();
         valueCellRenderer.setToolTipText(NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.fillValue"));
         valueCellRenderer.setBackground(Color.YELLOW);
         columnValue.setCellRenderer(valueCellRenderer);
 
     }
 
     public Study getStudy() {
         return study;
     }
 
     public void setStudy(Study study) {
         this.study = study;
     }
 
     public void disableTraitsSelection() {
         //jButtonSync.setEnabled(false);
         //doubleListPanel.setEnabled(false);
         //jButtonSelectTraits.setEnabled(false);
     }
 
     public void loadDataFromDB() {
         jTextTrialName.setEnabled(false);
         jButtonSaveData.setEnabled(true);
         ObservationsTableModel tableModel = new ObservationsTableModel(myWorkbook, myWorkbook.getVariates());
         jTableObservations.setModel(tableModel);
         sorterMeasurements = new TableRowSorter<TableModel>(tableModel);
         this.jTableObservations.setRowSorter(sorterMeasurements);
         int factorLabelColumn = 0;
         for (Measurement measurement : myWorkbook.getMeasurements()) {
             Object[] rowToAdd = new Object[tableModel.getColumnCount()];
             factorLabelColumn = 0;
             for (Object factorLabel : measurement.getFactorLabelData()) {
                 rowToAdd[factorLabelColumn] = factorLabel;
                 factorLabelColumn++;
             }
             if (measurement.getMeasurementsData() != null) {
                 for (MeasurementData data : measurement.getMeasurementsData()) {
                     if (data.getVariateid() != null) {
                         int variateColumIndex = tableModel.getHeaderIndexForVariate(data.getVariateid());
                         if (variateColumIndex != -1) {
                             rowToAdd[variateColumIndex] = data.getValueData();
                         }
                     }
                 }
             }
             tableModel.addRow(rowToAdd);
         }
         int entriesTot = this.jTableEntries.getRowCount();
         this.jTextFieldEntries.setText(String.valueOf(entriesTot));
         int trials = getTotalTrialsFromObservations();
         jLabelInstances.setText(String.valueOf(trials));
         SpinnerNumberModel mod1 = new SpinnerNumberModel(1, 1, trials, 1);
         SpinnerNumberModel mod3 = new SpinnerNumberModel(1, 1, entriesTot, 1);
         this.jSpinnerTrial.setModel(mod1);
         this.jSpinnerEntry.setModel(mod3);
         ajustaColumnsTable(this.jTableObservations, 2);
 
         for (int i = 0; i < jTableObservations.getColumnCount(); i++) {
             sorterMeasurements.setSortable(i, false);
         }
 
         this.jTabbedPaneEditor.setEnabledAt(7, true);
         this.jTabbedPaneEditor.setSelectedIndex(7);
 
         ObservationTableTooltips.assignTooltips(jTableObservations);
         enableMeasurementButtons();
 
 
         if (giveMeCropFromDatabase() == 1) { //If is Maize
             fillMasterData(myWorkbook.getVariates());
             ajustaColumnsTable(this.jTableMaster, 2);
         }
 
         changeCursorWaitStatus(false);
         savedTraits = new ArrayList<Variate>();
         for (Variate variate : doubleListPanel.getTargetList()) {
             savedTraits.add(variate);
         }
     }
 
     private int getTotalTrialsFromObservations() {
         int totalTrials = 0;
         ObservationsTableModel tableModel = (ObservationsTableModel) jTableObservations.getModel();
         String currentTrial = "-999";
 
         for (int row = 0; row < tableModel.getRowCount(); row++) {
             String trialInRow = tableModel.getValueAt(row, 0).toString();
 
             if (!trialInRow.equals(currentTrial)) {
                 totalTrials++;
                 currentTrial = trialInRow;
             }
         }
         return totalTrials;
     }
 
     public void assignTraits(List<Variate> unselectedVariates, List<Variate> selectedVariates) {
         doubleListPanel.setSourceList(unselectedVariates);
         doubleListPanel.setTargetList(selectedVariates);
         doubleListPanel.fillListItems();
     }
 
     private void setValuesFunctionForColumn(ContinuousFunction func, String variateToFind) {
         changeCursorWaitStatus(true);
         ObservationsTableModel tableModel = (ObservationsTableModel) this.jTableObservations.getModel();
         ObservationsTableModel tableModelMaster = (ObservationsTableModel) this.jTableMaster.getModel();
         ScriptEngineManager manager = new ScriptEngineManager();
         ScriptEngine engine = manager.getEngineByName("js");
         Object operation = null;
         String formulaValores = func.getFormulaTraducida();
         int colMaster = tableModelMaster.findColumn(variateToFind);
 
         jTableMaster.getColumnModel().getColumn(colMaster).setHeaderRenderer(new MyRenderer(Color.YELLOW, Color.black));
 
         List<TmsConsistencyChecks> dependences = func.getTmsConsistencyChecksDependencys();
 
         for (int j = 0; j < tableModelMaster.getRowCount(); j++) {
             boolean hayError = false;
             formulaValores = func.getFormulaTraducida();
 
             for (int i = 0; i < dependences.size(); i++) {
                 org.cimmyt.cril.ibwb.domain.Traits trait = AppServicesProxy.getDefault().appServices().getTraits(dependences.get(i).getTraitid());
                 int colOriginal = tableModel.findColumn(trait.getTrname());
                 String var = "@" + String.valueOf(dependences.get(i).getTraitid());
 
                 try {
                     double valor = Double.valueOf(tableModel.getValueAt(j, colOriginal).toString());
                     formulaValores = formulaValores.replace(var, String.valueOf(valor));
                 } catch (Exception e) {
                     hayError = true;
                 }
 
             }
 
             // System.out.println("Formula " + j + "   :  " + formulaValores);
 
             if (!hayError) {
                 try {
                     operation = engine.eval(formulaValores);
                 } catch (ScriptException ex) {
                     System.out.println("ERROR EN FORMULA " + ex);
                     operation = 0;
                 }
 
                 //   System.out.println("Evaluado operacion 1: " + operation);
                 tableModelMaster.setValueAt(String.valueOf(operation), j, colMaster);
 
             }
 
         }
         changeCursorWaitStatus(false);
 
     }
 
     private void setValuesContinuousForColumn(String operador, double factor, String variateToFind) {
         ObservationsTableModel tableModel = (ObservationsTableModel) this.jTableObservations.getModel();
         ObservationsTableModel tableModelMaster = (ObservationsTableModel) this.jTableMaster.getModel();
 
         int colOriginal = tableModel.findColumn(variateToFind);
         int colMaster = tableModelMaster.findColumn(variateToFind);
 
         for (int i = 0; i < tableModelMaster.getRowCount(); i++) {
             double datoOriginal = 0;
             try {
                 datoOriginal = Double.valueOf(tableModel.getValueAt(i, colOriginal).toString());
             } catch (Exception e) {
                 datoOriginal = 0;
             }
             if (operador.equals("+")) {
                 try {
                     datoOriginal = datoOriginal + factor;
                 } catch (Exception e) {
                     datoOriginal = -9999;
                 }
             } else if (operador.equals("-")) {
                 try {
                     datoOriginal = datoOriginal - factor;
                 } catch (Exception e) {
                     datoOriginal = -9999;
                 }
             } else if (operador.equals("*")) {
                 try {
                     datoOriginal = datoOriginal * factor;
                 } catch (Exception e) {
                     datoOriginal = -9999;
                 }
             } else if (operador.equals("/")) {
                 try {
                     datoOriginal = datoOriginal / factor;
                 } catch (Exception e) {
                     datoOriginal = -9999;
                 }
             }
 
             tableModelMaster.setValueAt(String.valueOf(datoOriginal), i, colMaster);
         }
     }
 
     private void calculateMasterData() {
 
         ObservationsTableModel tableModel = (ObservationsTableModel) this.jTableObservations.getModel();
         ObservationsTableModel tableModelMaster = (ObservationsTableModel) this.jTableMaster.getModel();
         tableModelMaster.setIsMasterSheet(false);
 
         List<Variate> masterVariates = tableModelMaster.getVariateList();
 
 
         for (int i = 0; i < masterVariates.size(); i++) {
             Variate mVariate = masterVariates.get(i);
             String variateToFind = mVariate.getVariateName();
 
             System.out.println(mVariate.getVariateName());
             System.out.println(mVariate.getVariateId());
 
             List<TmsConsistencyChecks> lista = new ArrayList<TmsConsistencyChecks>();
             Transformations trans = null;
             String tipo = "D";//Default 
 
             try {
                 trans = AppServicesProxy.getDefault().appServices().getTransformationsByVariateid(mVariate.getVariateId());
                 tipo = trans.getTranstype();
                 System.out.println("EL TIPO ES: " + tipo);
             } catch (Exception e) {
                 System.out.println("EL TIPO ERROR ES: " + tipo);
 
             }
 
             if (tipo.equals("C")) {
                 ContinuousConversion conv = trans.getContinuousConversion();
                 double factor = conv.getFactor();
                 double res = 0;
                 String operador = conv.getOperator();
                 System.out.println("El operador es: " + operador);
                 System.out.println("El factor es: " + factor);
                 setValuesContinuousForColumn(operador, factor, variateToFind);
             } else if (tipo.equals("F")) {
                 ContinuousFunction func = trans.getContinuousFunction();
                 //System.out.println("FUNCION:" + func.getFunction());
                 // System.out.println("TRADUCIDA:" + func.getFormulaTraducida());
                 setValuesFunctionForColumn(func, variateToFind);
 
             } else {
                 setValuesContinuousForColumn("*", 1, variateToFind);
             }
         }
 
         tableModelMaster.setIsMasterSheet(true);
     }
 
     public void setNewStudy(Workbook savingWorkbook) {
         this.workbookAfterSave = savingWorkbook;
     }
 
     private class TraitsDropTargetCommand implements DropTargetCommand {
 
         @Override
         public void onDropExecute(String droppedText) {
             System.out.println(droppedText);
             String[] rows = droppedText.split("\n");
             for (String droppedRow : rows) {
                 String[] values = droppedRow.split("\t");
                 Integer traitId = Integer.parseInt(values[0].trim());
                 Traits trait = AppServicesProxy.getDefault().appServices().getTraits(traitId);
                 Variate variate = new Variate();
                 variate.setVariateName(trait.getTrname().trim());
                 variate.setDescription(trait.getTrdesc().trim());
                 variate.setScale("UNASSINGED");
                 variate.setMethod("UNNASSIGNED");
                 variate.setDataType("N");
 
                 if (trait.getMeasuredin() != null) {
                     if (trait.getMeasuredin().getScales() != null) {
                         variate.setScale(trait.getMeasuredin().getScales().getScname());
                         variate.setDataType(trait.getMeasuredin().getScales().getDtype());
                     }
 
                     if (trait.getMeasuredin().getTmsMethod() != null) {
                         variate.setMethod(trait.getMeasuredin().getTmsMethod().getTmname());
                     }
                 }
                 doubleListPanel.addToTarget(variate);
             }
         }
     }
 
     public DoubleListPanel<Variate> getDoubleListPanel() {
         return doubleListPanel;
     }
 
     public void setDoubleListPanel(DoubleListPanel<Variate> doubleListPanel) {
         this.doubleListPanel = doubleListPanel;
     }
 
     public boolean isStudyAlreadyExists() {
         return studyAlreadyExists;
     }
 
     public void setStudyAlreadyExists(boolean studyAlreadyExists) {
         this.studyAlreadyExists = studyAlreadyExists;
     }
 
     public Study getStudyInfo() {
         return studyInfo;
     }
 
     public void setStudyInfo(Study studyInfo) {
         this.studyInfo = studyInfo;
     }
 
     public JTextField getjTextTrialName() {
         return jTextTrialName;
     }
 
     public void setjTextTrialName(JTextField jTextTrialName) {
         this.jTextTrialName = jTextTrialName;
     }
 
     private void createBallonTips() {
         String msg = NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.jTextTrialName.Tip");
         tipSavingTrial = new BalloonTip(jTextTrialName, msg);
         ToolTipUtils.balloonToToolTip(tipSavingTrial, 500, 3000);
     }
 
     @Override
     public boolean canClose() {
         boolean result = true;
 
 
         if (conPreguntas) {
             String closeTitle = NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.closeFieldbook.title");
             String closeQuestion = NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.closeFieldbook.question");
             String provideTrialName = NbBundle.getMessage(StudyEditorTopComponent.class, "StudyEditorTopComponent.closeFieldbook.provideTrialName");
             Object response = DialogUtil.displayConfirmationDialog(closeQuestion, closeTitle, NotifyDescriptor.YES_NO_CANCEL_OPTION);
             boolean saveFieldbook = false;
 
             if (response.equals(NotifyDescriptor.YES_OPTION)) {
                 saveFieldbook = true;
             }
 
             if (response.equals(NotifyDescriptor.CANCEL_OPTION)) {
                 result = false;
             }
 
             if (saveFieldbook) {
                 if (jTextTrialName.getText().trim().isEmpty()) {
                     DialogUtil.displayError(provideTrialName);
                     result = false;
                 } else {
                     closingEditor = true;
                     saveFieldbook();
 
                 }
 
             }
 
         } else {
             result = true;
         }
 
 
         return result;
     }
 
     public JTable getjTableConstants() {
         return jTableConstants;
     }
 
     public void setjTableConstants(JTable jTableConstants) {
         this.jTableConstants = jTableConstants;
     }
 
     private boolean trialNameAlreadyExists(String trialName) {
         boolean result = false;
         if (AppServicesProxy.getDefault().appServices().getStudyByName(trialName) != null) {
             result = true;
         }
         return result;
 
     }
 
     public void enableMeasurementButtons() {
         btnPrintLabels.setEnabled(true);
         jButtonSaveData.setEnabled(true);
         jButtonExportData.setEnabled(true);
         jButtonImportData.setEnabled(true);
 
 
 
         if (giveMeCropFromDatabase() == 1) { //If is Maize
             System.out.println("TOTAL TAB: " + this.jTabbedPaneEditor.getTabCount());
             this.jTabbedPaneEditor.setEnabledAt(8, true);
         } else {
             if (jTabbedPaneEditor.getTabCount() > 8) {
                 this.jTabbedPaneEditor.remove(8);
             }
 
 
         }
 
 
     }
 
     /**
      * Stores all cell that are in editing mode
      */
     private void storeCellsInEditMode() {
         storeCellsInEditMode(jTableStudyConditions);
         storeCellsInEditMode(jTableTrialConditions);
         storeCellsInEditMode(jTableObservations);
         storeCellsInEditMode(jTableConstants);
     }
 
     /**
      * Stores a cell that is in editing mode
      *
      * @param jtable JTable to verify
      */
     private void storeCellsInEditMode(JTable jtable) {
         if (jtable.getCellEditor() != null) {
             jtable.getCellEditor().stopCellEditing();
         }
     }
 
     public int giveMeCropFromDatabase() {
         int theCrop = 2;  //0=Wheat   1=maize   2=other crops
         TypeDB tipo = AppServicesProxy.getDefault().appServices().getTypeDB();
         return tipo.getType();
     }
 
     private void validateDesignButtons() {
         boolean enableCustomDesign = rBtnImportLayoutFile.isSelected();
 
         txtFileName.setEnabled(enableCustomDesign);
         btnBrowse.setEnabled(enableCustomDesign);
     }
 
     private void fillExistingObservationsData() {
         List<Variate> selectedVariates = doubleListPanel.getTargetList();
         List<Variate> variatesToAdd = new ArrayList<Variate>();
         this.setSelectedTraits(selectedVariates);
 
         ObservationsTableModel tableModel = (ObservationsTableModel) jTableObservations.getModel();
 
         // check new traits to add
         for (Variate variate : selectedVariates) {
             if (!savedTraits.contains(variate)) {
                 variatesToAdd.add(variate);
             }
         }
         // then add it to model
         for (Variate variate : variatesToAdd) {
             tableModel.addNewTrait(variate);
         }
     }
 
     private void importLayoutFromFile() {
         File layoutFile = FileUtils.openFile();
         if (layoutFile != null) {
             txtFileName.setText(layoutFile.getPath());
             DesignTableModel dtm = (DesignTableModel) jTableDesign.getModel();
             for (DesignBean designBean : dtm.getDesignList()) {
                 designBean.setDesign(DesignsClass.USER_DEFINED_DESIGN);
                 designBean.setUserDefinedDesign(layoutFile);
             }
             dtm.fireTableDataChanged();
             try {
                 designsUtils.checkDesignTable(2, 0, true);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     public javax.swing.JPanel JPanelData;
     private javax.swing.JButton btnBrowse;
     private javax.swing.JButton btnPrintLabels;
     private javax.swing.ButtonGroup buttonGroupDesign;
     private javax.swing.ButtonGroup buttonGroupExpConditions;
     private javax.swing.ButtonGroup buttonGroupMaster;
     private javax.swing.ButtonGroup buttonGroupMeasurements;
     private javax.swing.ButtonGroup buttonGroupTrInformation;
     private javax.swing.JButton jButtonExportData;
     private javax.swing.JButton jButtonExportMaster;
     private javax.swing.JButton jButtonImportData;
     public static javax.swing.JButton jButtonRefreshDesign;
     private javax.swing.JButton jButtonSaveData;
     private javax.swing.JButton jButtonSaveMaster;
     public static javax.swing.JButton jButtonSelectTraits;
     public static javax.swing.JButton jButtonSync;
     public javax.swing.JComboBox jComboBoxStudyType;
     public com.toedter.calendar.JDateChooser jDateChooserEnd;
     public com.toedter.calendar.JDateChooser jDateChooserStart;
     private javax.swing.JLabel jLabel13;
     private javax.swing.JLabel jLabel5;
     public javax.swing.JLabel jLabelEntries;
     public javax.swing.JLabel jLabelInstances;
     private javax.swing.JLabel jLabelTotalGermp;
     private javax.swing.JMenuItem jMenuItem1;
     private javax.swing.JMenuItem jMenuItem2;
     private javax.swing.JMenuItem jMenuItem3;
     private javax.swing.JMenuItem jMenuItem4;
     private javax.swing.JMenuItem jMenuItemSelect;
     private javax.swing.JMenuItem jMenuItemSelectAll;
     private javax.swing.JMenuItem jMenuItemUnSelect;
     private javax.swing.JMenuItem jMenuItemUnselectAll;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel12;
     private javax.swing.JPanel jPanel13;
     private javax.swing.JPanel jPanel21;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JPanel jPanel6;
     private javax.swing.JPanel jPanel9;
     private javax.swing.JPanel jPanelTraits;
     private javax.swing.JPopupMenu jPopupMenuConstants;
     private javax.swing.JPopupMenu jPopupMenuSelect;
     private javax.swing.JPopupMenu jPopupMenuTraits;
     private javax.swing.JPopupMenu jPopupMenuUnSelect;
     private javax.swing.JRadioButton jRadioButtonAllExpCondition;
     private javax.swing.JRadioButton jRadioButtonAllTrials;
     private javax.swing.JRadioButton jRadioButtonAllTrials2;
     private javax.swing.JRadioButton jRadioButtonAllTrialsMaster;
     private javax.swing.JRadioButton jRadioButtonFilterEntry;
     private javax.swing.JRadioButton jRadioButtonFilterEntry1;
     private javax.swing.JRadioButton jRadioButtonFilterExpConditions;
     private javax.swing.JRadioButton jRadioButtonFilterTrial;
     private javax.swing.JRadioButton jRadioButtonFilterTrialInfo;
     private javax.swing.JRadioButton jRadioButtonFilterTrialMaster;
     private javax.swing.JRadioButton jRadioButtonFilterTrialStudy;
     private javax.swing.JRadioButton jRadioButtonViewAllTrialStudy;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JScrollPane jScrollPane5;
     private javax.swing.JScrollPane jScrollPane6;
     private javax.swing.JScrollPane jScrollPane7;
     private javax.swing.JScrollPane jScrollPane8;
     private javax.swing.JScrollPane jScrollPane9;
     private javax.swing.JSpinner jSpinnerEntry;
     private javax.swing.JSpinner jSpinnerEntry1;
     private javax.swing.JSpinner jSpinnerExpConditions;
     public javax.swing.JSpinner jSpinnerTrial;
     public javax.swing.JSpinner jSpinnerTrialInformation;
     public javax.swing.JSpinner jSpinnerTrialMaster;
     public javax.swing.JSpinner jSpinnerTrialStudy;
     public javax.swing.JTabbedPane jTabbedPaneEditor;
     public javax.swing.JTable jTableConstants;
     public javax.swing.JTable jTableDesign;
     public javax.swing.JTable jTableEntries;
     public javax.swing.JTable jTableMaster;
     public javax.swing.JTable jTableObservations;
     public javax.swing.JTable jTableOtherFactorLabels;
     private javax.swing.JTable jTableOtherFactors;
     public javax.swing.JTable jTableStudyConditions;
     public javax.swing.JTable jTableTrialConditions;
     public javax.swing.JTextField jTextFieldDescription;
     public javax.swing.JTextField jTextFieldDescriptionSelected;
     public javax.swing.JTextField jTextFieldEntries;
     public javax.swing.JTextField jTextFieldObjective;
     public javax.swing.JTextField jTextFieldPMKey;
     public javax.swing.JTextField jTextFieldStudy;
     public javax.swing.JTextField jTextFieldTitle;
     private javax.swing.JTextField jTextTrialName;
     private javax.swing.JTextField jTextTrialNameMaster;
     private javax.swing.JToolBar jToolBar1;
     private javax.swing.JLabel lblEndDate;
     private javax.swing.JLabel lblExpConditionsTrial;
     private javax.swing.JLabel lblFileName;
     private javax.swing.JLabel lblInstances;
     private javax.swing.JLabel lblObjective;
     private javax.swing.JLabel lblProjectKey;
     private javax.swing.JLabel lblStartDate;
     private javax.swing.JLabel lblStudy;
     private javax.swing.JLabel lblStudyConditions;
     private javax.swing.JLabel lblStudyType;
     private javax.swing.JLabel lblTitle;
     private javax.swing.JLabel lblTrialName;
     private javax.swing.JLabel lblTrialNameMaster;
     private javax.swing.JLabel pnlExpConditionTrial;
     private javax.swing.JPanel pnlExpDesignDesign;
     private javax.swing.JPanel pnlExperimConditionsFilter;
     private javax.swing.JPanel pnlExperimentalConditionFilter;
     private javax.swing.JPanel pnlExperimentalConditions;
     private javax.swing.JPanel pnlExperimentalDesign;
     private javax.swing.JPanel pnlGeneralInformation;
     private javax.swing.JPanel pnlGermplasmEntries;
     private javax.swing.JPanel pnlMeasurement;
     private javax.swing.JPanel pnlMeasurementFilter;
     private javax.swing.JPanel pnlMeasurementFilter1;
     private javax.swing.JPanel pnlOtherTreatment;
     private javax.swing.JPanel pnlSelectList;
     private javax.swing.JPanel pnlTraits;
     private javax.swing.JPanel pnlTrialInformation;
     private javax.swing.JPanel pnlTrialInformationFilter;
     private javax.swing.JLabel pnlTrialInformationTrial;
     private javax.swing.JRadioButton rBtnImportLayoutFile;
     private javax.swing.JRadioButton rbtnIndividualDesign;
     private javax.swing.JRadioButton rbtnUseSameDesign;
     private javax.swing.JTextField txtFileName;
     // End of variables declaration//GEN-END:variables
 }
