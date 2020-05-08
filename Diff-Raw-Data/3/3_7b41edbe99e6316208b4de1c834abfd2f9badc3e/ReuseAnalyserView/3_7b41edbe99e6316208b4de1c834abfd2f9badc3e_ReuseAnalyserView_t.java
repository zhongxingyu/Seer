 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 package org.amanzi.awe.views.reuse.views;
 
 import java.awt.Color;
 import java.awt.Paint;
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import net.refractions.udig.project.ui.internal.dialogs.ColorEditor;
 import net.refractions.udig.ui.PlatformGIS;
 
 import org.amanzi.awe.catalog.neo.GeoNeo;
 import org.amanzi.awe.catalog.neo.NeoCatalogPlugin;
 import org.amanzi.awe.catalog.neo.upd_layers.events.RefreshPropertiesEvent;
 import org.amanzi.awe.report.editor.ReportEditor;
 import org.amanzi.awe.views.reuse.Distribute;
 import org.amanzi.awe.views.reuse.Messages;
 import org.amanzi.awe.views.reuse.ReusePlugin;
 import org.amanzi.awe.views.reuse.Select;
 import org.amanzi.integrator.awe.AWEProjectManager;
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.enums.ColoredFlags;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.GisTypes;
 import org.amanzi.neo.core.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.core.preferences.NeoCorePreferencesConstants;
 import org.amanzi.neo.core.propertyFilter.PropertyFilterModel;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.ActionUtil;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.core.utils.Pair;
 import org.amanzi.neo.core.utils.PropertyHeader;
 import org.amanzi.neo.core.utils.ActionUtil.RunnableWithResult;
 import org.apache.log4j.Logger;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
 import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.part.ViewPart;
 import org.geotools.brewer.color.BrewerPalette;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartMouseEvent;
 import org.jfree.chart.ChartMouseListener;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.LogarithmicAxis;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.axis.ValueAxis;
 import org.jfree.chart.entity.CategoryItemEntity;
 import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
 import org.jfree.chart.plot.CategoryPlot;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.renderer.category.BarRenderer;
 import org.jfree.chart.renderer.category.CategoryItemRenderer;
 import org.jfree.data.category.CategoryDataset;
 import org.jfree.data.general.AbstractDataset;
 import org.jfree.experimental.chart.swt.ChartComposite;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.TraversalPosition;
 import org.neo4j.graphdb.Traverser.Order;
 import org.rubypeople.rdt.core.IRubyProject;
 import org.rubypeople.rdt.internal.ui.wizards.NewRubyElementCreationWizard;
 
 /**
  * <p>
  * view "Reuse Analyser"
  * </p>
  * 
  * @author Cinkel_A
  * @since 1.0.0
  */
 public class ReuseAnalyserView extends ViewPart implements IPropertyChangeListener {
     private static final Logger LOGGER = Logger.getLogger(ReuseAnalyserView.class);
 
     /** String TOOL_TIP_LOG field */
     private static final String TOOL_TIP_LOG = Messages.ReuseAnalayserView_TOOL_TIP_LOG;
     private static final String TOOL_TIP_DATA = Messages.ReuseAnalayserView_TOOL_TIP_DATA;
     private static final String TOOL_TIP_PROPERTY = Messages.ReuseAnalayserView_TOOL_TIP_PROPERTY;
     private static final String TOOL_TIP_DISTRIBUTE = Messages.ReuseAnalayserView_TOOL_TIP_DISTRIBUTE;
     private static final String TOOL_TIP_SELECT = Messages.ReuseAnalayserView_TOOL_TIP_SELECT;
     private static final String TOOL_TIP_ADJACENCY = Messages.ReuseAnalayserView_TOOL_TIP_ADJACENCY;
     private static final String TOOL_TIP_SELECTED_VALUES = Messages.ReuseAnalayserView_TOOL_TIP_SELECTED_VALUES;
     // labels
     private static final String SELECT_LABEL = Messages.ReuseAnalayserView_SELECT_LABEL;
     private static final String DISTRIBUTE_LABEL = Messages.ReuseAnalayserView_DISTRIBUTE_LABEL;
     private static final String LABEL_INFO = Messages.ReuseAnalayserView_LABEL_INFO;
     private static final String LABEL_INFO_BLEND = Messages.ReuseAnalayserView_LABEL_INFO_BLEND;
     private static final String ERROR_TITLE = Messages.ReuseAnalayserView_ERROR_TITLE;
     private static final String LOG_LABEL = Messages.ReuseAnalayserView_LOG_LABEL;
 
     /** String ADJACENCY field */
     private static final String ADJACENCY = Messages.ReuseAnalayserView_FIELD_ADJACENCY;
     /** String PROPERTY_LABEL field */
     private static final String PROPERTY_LABEL = Messages.ReuseAnalayserView_FIELD_PROPERTY_LABEL;
     /** String GIS_LABEL field */
     private static final String GIS_LABEL = Messages.ReuseAnalayserView_FIELD_GIS_LABEL;
     /** String COUNT_AXIS field */
     private static final String COUNT_AXIS = Messages.ReuseAnalayserView_FIELD_COUNT_AXIS;
     /** String VALUES_DOMAIN field */
     private static final String VALUES_DOMAIN = Messages.ReuseAnalayserView_FIELD_VALUES_DOMAIN;
     private static final String ROW_KEY = Messages.ReuseAnalayserView_ROW_KEY;
     private static final String COLOR_LABEL = Messages.ReuseAnalayserView_COLOR_LABEL;
     private static final String REPORT_LABEL = Messages.ReuseAnalayserView_REPORT_LABEL;
 
     private final Map<String, String[]> aggregatedProperties = new HashMap<String, String[]>();
     private Label gisSelected;
     private Combo gisCombo;
     private Label propertySelected;
     private Combo propertyCombo;
     private Label lSelect;
     private Combo cSelect;
     private Label lDistribute;
     private Combo cDistribute;
     private HashMap<String, Node> members;
     protected List<String> propertyList;
     private Spinner spinAdj;
     private Label spinLabel;
     private ChartComposite chartFrame;
     private JFreeChart chart;
     private PropertyCategoryDataset dataset;
     private Node selectedGisNode = null;
     private ChartNode selectedColumn = null;
     private Text tSelectedInformation;
     private Label lSelectedInformation;
     private Button bLogarithmic;
     private ValueAxis axisNumeric;
     private LogarithmicAxis axisLog;
     private Composite mainView;
     private Label lLogarithmic;
     protected String propertyName = null;
     private Button bColorProperties;
     private Label lColorProperties;
     private boolean colorThema;
     private BrewerPalette currentPalette = null;
     private Label lPalette;
     private Combo cPalette;
     private Button blend;
     private ColorEditor colorLeft;
     private ColorEditor colorRight;
     private Label lBlend;
     private List<String> allFields;
     private List<String> numericFields;
     private Button bReport;
     private ColorEditor colorMiddle;
     private Button threeBlend;
     private Label lThreeBlend;
     private Label ltblendInformation;
     private Text ttblendInformation;
     private ChartNode middleColumn;
     private static final Color DEFAULT_COLOR = new Color(0.75f, 0.7f, 0.4f);
     private static final Color COLOR_SELECTED = Color.RED;
     private static final Color COLOR_LESS = Color.BLUE;
     private static final Color COLOR_MORE = Color.GREEN;
     private static final Color CHART_BACKGROUND = Color.WHITE;
     private static final Color PLOT_BACKGROUND = new Color(230, 230, 230);
     private static final String PALETTE_LABEL = "Palette";
     private static final String BLEND = "blend";
     private static final String TT_LEFT_BAR = "left bar color ";
     private static final String TT_RIGHT_BAR = "right bar color";
     private static final String TT_MIDLE_BAR = "third bar color";
     private static final RGB DEFAULT_LEFT = new RGB(255, 0, 0);
     private static final RGB DEFAULT_RIGHT = new RGB(0, 255, 0);
     private static final RGB DEFAULT_MIDDLE = new RGB(127, 127, 0);
     private static final String THIRD_BLEND = "third color";
     ReuseAnalyserModel model = null;
     private static final String ERROR_CHART = "Error Chart";
 
     @Override
     public void createPartControl(Composite parent) {
         aggregatedProperties.clear();
         mainView = parent;
         gisSelected = new Label(parent, SWT.NONE);
         gisSelected.setText(GIS_LABEL);
         gisCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
         gisCombo.setItems(getRootItems());
         gisCombo.setEnabled(true);
         propertySelected = new Label(parent, SWT.NONE);
         propertySelected.setText(PROPERTY_LABEL);
         propertyCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
         propertyCombo.setItems(new String[] {});
         propertyCombo.setEnabled(true);
         spinLabel = new Label(parent, SWT.NONE);
         spinLabel.setText(ADJACENCY);
         spinAdj = new Spinner(parent, SWT.BORDER);
         spinAdj.setMinimum(0);
         spinAdj.setIncrement(1);
         spinAdj.setDigits(0);
         spinAdj.setSelection(1);
         lDistribute = new Label(parent, SWT.NONE);
         lDistribute.setText(DISTRIBUTE_LABEL);
         cDistribute = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
         cDistribute.setItems(Distribute.getEnumAsStringArray());
         cDistribute.select(0);
         lSelect = new Label(parent, SWT.NONE);
         lSelect.setText(SELECT_LABEL);
         cSelect = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
         cSelect.setItems(Select.getEnumAsStringArray());
         cSelect.select(0);
         cSelect.setEnabled(false);
         lSelectedInformation = new Label(parent, SWT.NONE);
         lSelectedInformation.setText(LABEL_INFO);
         lLogarithmic = new Label(parent, SWT.NONE);
         lLogarithmic.setText(LOG_LABEL);
         bLogarithmic = new Button(parent, SWT.CHECK);
         bLogarithmic.setSelection(false);
         tSelectedInformation = new Text(parent, SWT.BORDER);
         bColorProperties = new Button(parent, SWT.CHECK);
         bColorProperties.setSelection(false);
         lColorProperties = new Label(parent, SWT.NONE);
         lColorProperties.setText(COLOR_LABEL);
         blend = new Button(parent, SWT.CHECK);
         blend.setSelection(true);
         lBlend = new Label(parent, SWT.NONE);
         lBlend.setText(BLEND);
         colorLeft = new ColorEditor(parent);
         colorLeft.getButton().setToolTipText(TT_LEFT_BAR);
         colorRight = new ColorEditor(parent);
         colorRight.getButton().setToolTipText(TT_RIGHT_BAR);
 
         threeBlend = new Button(parent, SWT.CHECK);
         threeBlend.setSelection(false);
         lThreeBlend = new Label(parent, SWT.NONE);
         lThreeBlend.setText(THIRD_BLEND);
 
         colorMiddle = new ColorEditor(parent);
         colorMiddle.getButton().setToolTipText(TT_MIDLE_BAR);
 
         lBlend.setVisible(false);
         blend.setVisible(false);
         lThreeBlend.setVisible(false);
         threeBlend.setVisible(false);
         colorLeft.getButton().setVisible(false);
         colorRight.getButton().setVisible(false);
         colorMiddle.getButton().setVisible(false);
         ltblendInformation = new Label(parent, SWT.NONE);
         ltblendInformation.setText(LABEL_INFO_BLEND);
         ttblendInformation = new Text(parent, SWT.BORDER);
         ttblendInformation.setVisible(false);
         ltblendInformation.setVisible(false);
         lPalette = new Label(parent, SWT.NONE);
         lPalette.setText(PALETTE_LABEL);
         cPalette = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
         cPalette.setItems(PlatformGIS.getColorBrewer().getPaletteNames());
         cPalette.select(0);
         lPalette.setVisible(false);
         cPalette.setVisible(false);
         bReport = new Button(parent, SWT.PUSH);
         bReport.setText(REPORT_LABEL);
         bReport.setVisible(false);
         bReport.addSelectionListener(new SelectionAdapter() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 generateReport();
             }
         });
         SelectionListener listener = new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 changeBlendColors();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         };
         colorLeft.addSelectionListener(listener);
         colorRight.addSelectionListener(listener);
         colorMiddle.addSelectionListener(listener);
         SelectionListener blendListener = new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 changeBlendTheme();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         };
         blend.addSelectionListener(blendListener);
         threeBlend.addSelectionListener(blendListener);
         cPalette.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 changePalette();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         bColorProperties.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 changeThema(bColorProperties.getSelection());
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         spinAdj.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 if (selectedColumn != null) {
                     setSelection(selectedColumn);
                 };
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
             }
         });
         bLogarithmic.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 logarithmicSelection();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         dataset = new PropertyCategoryDataset();
         chart = ChartFactory.createBarChart("SWTBarChart", VALUES_DOMAIN, COUNT_AXIS, dataset, PlotOrientation.VERTICAL, false, false, false);
         CategoryPlot plot = (CategoryPlot)chart.getPlot();
         NumberAxis rangeAxis = (NumberAxis)plot.getRangeAxis();
         rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
         // Craig: Don't bother with a legend when we have only one data type
         // LegendItemCollection legends = new LegendItemCollection();
         // legends.add(new LegendItem(ROW_KEY, defaultColor));
         // plot.setFixedLegendItems(legends);
         CategoryItemRenderer renderer = new CustomRenderer();
         renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
         plot.setRenderer(renderer);
         plot.setBackgroundPaint(PLOT_BACKGROUND);
         chart.setBackgroundPaint(CHART_BACKGROUND);
         // if: chartFrame = new ChartComposite(parent, 0, chart, FALSE); then font not zoomed, but
         // wrong column selection after resizing application
         chartFrame = new ChartComposite(parent, 0, chart, true);
         chartFrame.pack();
         setVisibleForChart(false);
         setToolTips();
         layoutComponents(parent);
         chartFrame.addChartMouseListener(new ChartMouseListener() {
             @Override
             public void chartMouseMoved(ChartMouseEvent chartmouseevent) {
             }
 
             @SuppressWarnings("unchecked")
             @Override
             public void chartMouseClicked(ChartMouseEvent chartmouseevent) {
                 if (!isColorThema()) {
                     if (chartmouseevent.getEntity() instanceof CategoryItemEntity) {
                         CategoryItemEntity entity = (CategoryItemEntity)chartmouseevent.getEntity();
                         Comparable columnKey = entity.getColumnKey();
                         ChartNode chartColumn = (ChartNode)columnKey;
                         if (selectedColumn == null || !selectedColumn.equals(chartColumn)) {
                             setSelection(chartColumn);
                         }
                     } else {
                         if (selectedColumn != null) {
                             setSelection(null);
                         }
                     }
                 } else {
                     if (threeBlend.getSelection() && chartmouseevent.getEntity() instanceof CategoryItemEntity) {
                         CategoryItemEntity entity = (CategoryItemEntity)chartmouseevent.getEntity();
                         Comparable columnKey = entity.getColumnKey();
                         middleColumn = (ChartNode)columnKey;
                         setMiddleSelectionName(middleColumn);
                         chartUpdate();
                     }
                 }
             }
         });
         SelectionListener gisComboSelectionListener = new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 int selectedGisInd = gisCombo.getSelectionIndex();
                 if (selectedGisInd < 0) {
                     propertyList = new ArrayList<String>();
                     setVisibleForChart(false);
                     tSelectedInformation.setText("");
                 } else {
                     Node rootNode = members.get(gisCombo.getText());
                     cSelect.setEnabled(isAggregatedDataset(rootNode));
                     formPropertyList(rootNode);
                 }
                 Collections.sort(propertyList);
                 propertyCombo.setItems(propertyList.toArray(new String[] {}));
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
             }
         };
         SelectionListener propComboSelectionListener = new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 if (propertyCombo.getSelectionIndex() < 0) {
                     setVisibleForChart(false);
                 } else {
                     final Node gisNode = members.get(gisCombo.getText());
                     propertyName = propertyCombo.getText();
                     if (isStringProperty(propertyName)) {
                         cDistribute.setEnabled(false);
                        cDistribute.select(0);
                         cSelect.setEnabled(false);
                        cSelect.select(0);
                     } else {
                         cDistribute.setEnabled(true);
                         boolean isAggregated = isAggregatedDataset(gisNode);
                         boolean isAggrProp = aggregatedProperties.keySet().contains(propertyName);
                         if (!isAggrProp) {
                             cSelect.select(0);
                         }
                         cSelect.setEnabled(isAggregated || isAggrProp);
                     }
                     findOrCreateAggregateNodeInNewThread(gisNode, propertyName);
                     // chartUpdate(aggrNode);
                 }
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         };
         SelectionListener selectComboSelectionListener = new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 if (!chartFrame.isVisible()) {
                     return;
                 }
                 findOrCreateAggregateNodeInNewThread(members.get(gisCombo.getText()), propertyCombo.getText());
                 // chartUpdate(aggrNode);
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         };
         gisCombo.addSelectionListener(gisComboSelectionListener);
         propertyCombo.addSelectionListener(propComboSelectionListener);
         cSelect.addSelectionListener(selectComboSelectionListener);
         cDistribute.addSelectionListener(selectComboSelectionListener);
         tSelectedInformation.addFocusListener(new FocusListener() {
 
             @Override
             public void focusLost(FocusEvent e) {
                 findSelectionInformation();
             }
 
             @Override
             public void focusGained(FocusEvent e) {
             }
         });
         tSelectedInformation.addKeyListener(new KeyListener() {
 
             @Override
             public void keyReleased(KeyEvent e) {
             }
 
             @Override
             public void keyPressed(KeyEvent e) {
                 if (e.keyCode == SWT.KEYPAD_CR || e.keyCode == 13) {
                     findSelectionInformation();
                 }
             }
         });
         ttblendInformation.addFocusListener(new FocusListener() {
 
             @Override
             public void focusLost(FocusEvent e) {
                 changeMiddleRange();
             }
 
             @Override
             public void focusGained(FocusEvent e) {
             }
         });
         ttblendInformation.addKeyListener(new KeyListener() {
 
             @Override
             public void keyReleased(KeyEvent e) {
             }
 
             @Override
             public void keyPressed(KeyEvent e) {
                 if (e.keyCode == SWT.KEYPAD_CR || e.keyCode == 13) {
                     changeMiddleRange();
                 }
             }
         });
         axisNumeric = ((CategoryPlot)chart.getPlot()).getRangeAxis();
         axisLog = new LogarithmicAxis(COUNT_AXIS);
         axisLog.setAllowNegativesFlag(true);
         axisLog.setAutoRange(true);
         setColorThema(false);
     }
 
     /**
      *Change the middle color position
      */
     protected void changeMiddleRange() {
         double valueToFind;
         try {
             valueToFind = Double.parseDouble(ttblendInformation.getText());
             middleColumn = findColumnByValue(valueToFind);
 
         } catch (NumberFormatException e) {
             ChartNode column = findColumnByName(ttblendInformation.getText());
             if (column != null) {
                 middleColumn = column;
             }
         }
         setMiddleSelectionName(middleColumn);
         chartUpdate();
     }
 
     /**
      * @param propertyName name of property
      * @return true if propertyName is String property
      */
     protected boolean isStringProperty(String propertyName) {
         return !numericFields.contains(propertyName) && !isAggregatedProperty(propertyName);
     }
 
     /**
      *change blend colors
      */
     protected void changeBlendColors() {
         saveColors();
         chartUpdate();
     }
 
     /**
      *save blend colors in aggregation node
      */
     private void saveColors() {
         Job job = new Job("saveColors aggr node") {
 
             @Override
             protected IStatus run(IProgressMonitor monitor) {
                 Transaction tx = NeoUtils.beginTransaction();
                 NeoUtils.addTransactionLog(tx, Thread.currentThread(), "saveColors");
                 try {
                     RGB rgbLeft = colorLeft.getColorValue();
                     if (rgbLeft != null) {
                         saveColor(dataset.getAggrNode(), INeoConstants.COLOR_LEFT, rgbLeft);
                     }
                     RGB rgbRight = colorRight.getColorValue();
                     if (rgbRight != null) {
                         saveColor(dataset.getAggrNode(), INeoConstants.COLOR_RIGHT, rgbRight);
                     }
                     RGB rgbMiddle = colorMiddle.getColorValue();
                     if (rgbMiddle != null) {
                         saveColor(dataset.getAggrNode(), INeoConstants.COLOR_MIDDLE, rgbMiddle);
                     }
                     if (dataset.getAggrNode() != null) {
                         String middleRange = getMiddleRange();
                         if (middleRange == null) {
                             middleRange = "";
                         }
                         dataset.getAggrNode().setProperty(INeoConstants.MIDDLE_RANGE, middleRange);
                     }
                     return Status.OK_STATUS;
                 } finally {
                     tx.finish();
                 }
             }
         };
         job.schedule();
         try {
             job.join();
         } catch (InterruptedException e) {
             // TODO Handle InterruptedException
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
     }
 
     /**
      * @return
      */
     protected String getMiddleRange() {
         String stringValue = middleColumn == null ? null : middleColumn.toString();
         return stringValue;
         // try {
         //
         // return stringValue.trim().isEmpty() ? getDefaultRange() :
         // Double.parseDouble(stringValue);
         // } catch (NumberFormatException e) {
         // return getDefaultRange();
         // }
 
     }
 
     /**
      * Gets the middle of range
      * 
      * @return
      */
     private ChartNode getDefaultRange() {
         int size = dataset.nodeList.size();
         return size > 0 ? dataset.nodeList.get(size / 2) : null;
     }
 
     /**
      * Save color in database
      * 
      * @param node node
      * @param property property name
      * @param rgb color
      */
     private void saveColor(Node node, String property, RGB rgb) {
         if (node == null || property == null || rgb == null) {
             return;
         }
         int[] array = new int[3];
         array[0] = rgb.red;
         array[1] = rgb.green;
         array[2] = rgb.blue;
         node.setProperty(property, array);
     }
 
     /**
      * change theme to blend
      */
     protected void changeBlendTheme() {
         setVisibleForColoredTheme(true);
         chartUpdate();
     }
 
     /**
      *
      */
     protected void changePalette() {
         if (cPalette.getSelectionIndex() >= 0) {
             String name = cPalette.getText();
             BrewerPalette palette = PlatformGIS.getColorBrewer().getPalette(name);
             if (palette == null) {
                 return;
             }
             currentPalette = palette;
         } else {
             currentPalette = null;
         }
 
         dataset.setPalette(currentPalette);
         chartUpdate();
     }
 
     /**
      * Change theme
      * 
      * @param coloredTheme - is colored theme?
      */
     protected void changeThema(boolean coloredTheme) {
         if (isColorThema() == coloredTheme) {
             return;
         }
         setColorThema(coloredTheme);
         setVisibleForColoredTheme(coloredTheme);
         setVisibleForNotColoredTheme(!coloredTheme);
         chartUpdate();
     }
 
     private boolean isAggregatedDataset(Node gisNode) {
         Node gis = NeoUtils.findGisNodeByChild(gisNode);
         if (gis == null) {
             return false;
         }
         GeoNeo geoNeo = new GeoNeo(NeoServiceProvider.getProvider().getService(), gis);
         boolean isAggregated = geoNeo.getGisType() != GisTypes.NETWORK;
         // if (!isAggregated) {
         // LOGGER.debug("GIS '" + geoNeo + "' is not drive: " + geoNeo.getGisType());
         // }
         return isAggregated;
     }
 
     /**
      * @return Returns the propertyName.
      */
     public String getPropertyName() {
         return propertyName;
     }
 
     /**
      *
      */
     private void setToolTips() {
         // adds to label
         lLogarithmic.setToolTipText(TOOL_TIP_LOG);
         lDistribute.setToolTipText(TOOL_TIP_DISTRIBUTE);
         lSelect.setToolTipText(TOOL_TIP_SELECT);
         lSelectedInformation.setToolTipText(TOOL_TIP_SELECTED_VALUES);
         gisSelected.setToolTipText(TOOL_TIP_DATA);
         propertySelected.setToolTipText(TOOL_TIP_PROPERTY);
         spinLabel.setToolTipText(TOOL_TIP_ADJACENCY);
 
         // adds to fields
         bLogarithmic.setToolTipText(TOOL_TIP_LOG);
         cDistribute.setToolTipText(TOOL_TIP_DISTRIBUTE);
         cSelect.setToolTipText(TOOL_TIP_SELECT);
         tSelectedInformation.setToolTipText(TOOL_TIP_SELECTED_VALUES);
         gisCombo.setToolTipText(TOOL_TIP_DATA);
         propertyCombo.setToolTipText(TOOL_TIP_PROPERTY);
         spinAdj.setToolTipText(TOOL_TIP_ADJACENCY);
     }
 
     /**
      *change logarithmicSelection
      */
     protected void logarithmicSelection() {
         CategoryPlot plot = (CategoryPlot)chart.getPlot();
         if (bLogarithmic.getSelection()) {
             plot.setRangeAxis(axisLog);
             axisLog.autoAdjustRange();
         } else {
             plot.setRangeAxis(axisNumeric);
         }
         chart.fireChartChanged();
     }
 
     /**
      *update select information
      */
     protected void findSelectionInformation() {
         String text = tSelectedInformation.getText();
         if (text == null || text.isEmpty()) {
             setSelectionName(selectedColumn);
             return;
         }
         try {
             double valueToFind = Double.parseDouble(text);
             ChartNode column = findColumnByValue(valueToFind);
             setSelection(column);
             return;
 
         } catch (NumberFormatException e) {
             setSelectionName(selectedColumn);
         }
     }
 
     /**
      * Finds column,which contains necessary value
      * 
      * @param valueToFind value to find
      * @return column or null
      */
     private ChartNode findColumnByValue(double valueToFind) {
         for (int i = 0; i < dataset.getColumnCount(); i++) {
             ChartNode column = (ChartNode)dataset.getColumnKey(i);
             if (column.containsValue(valueToFind)) {
                 return column;
             }
         }
         return null;
     }
 
     /**
      * Finds column,which contains necessary name
      * 
      * @param valueToFind value to find
      * @return column or null
      */
     private ChartNode findColumnByName(String columnName) {
         for (int i = 0; i < dataset.getColumnCount(); i++) {
             ChartNode column = (ChartNode)dataset.getColumnKey(i);
             if (column.toString().equals(columnName)) {
                 return column;
             }
         }
         return null;
     }
 
     /**
      * sets visibility of chart and depends element
      * 
      * @param isVisible - visibility
      */
     private void setVisibleForChart(boolean isVisible) {
         lColorProperties.setVisible(isVisible);
         bColorProperties.setVisible(isVisible);
         chartFrame.setVisible(isVisible);
         lLogarithmic.setVisible(isVisible);
         bLogarithmic.setVisible(isVisible);
         bReport.setVisible(isVisible);
         if (!isColorThema()) {
             setVisibleForNotColoredTheme(isVisible);
         } else {
             setVisibleForColoredTheme(isVisible);
         }
     }
 
     /**
      *sets visibility for colored theme
      * 
      * @param isVisible - visibility
      */
     private void setVisibleForColoredTheme(boolean isVisible) {
         boolean blendTheme = blend.getSelection();
         blend.setVisible(isVisible);
         lBlend.setVisible(isVisible);
         lPalette.setVisible(isVisible && !blendTheme);
         cPalette.setVisible(isVisible && !blendTheme);
         colorLeft.getButton().setVisible(isVisible && blendTheme);
         colorRight.getButton().setVisible(isVisible && blendTheme);
         lThreeBlend.setVisible(isVisible && blendTheme);
         threeBlend.setVisible(isVisible && blendTheme);
         boolean middleBlend = threeBlend.getSelection();
         colorMiddle.getButton().setVisible(isVisible && blendTheme && middleBlend);
         ltblendInformation.setVisible(isVisible && blendTheme && middleBlend);
         ttblendInformation.setVisible(isVisible && blendTheme && middleBlend);
     }
 
     /**
      *sets visibility for not colored theme
      * 
      * @param isVisible - visibility
      */
     private void setVisibleForNotColoredTheme(boolean isVisible) {
         lSelectedInformation.setVisible(isVisible);
         tSelectedInformation.setVisible(isVisible);
         spinLabel.setVisible(isVisible);
         spinAdj.setVisible(isVisible);
     }
 
     /**
      * Updates chart with new main node
      * 
      * @param aggrNode - new node
      */
     protected void chartUpdate(final Node aggrNode) {
         if (aggrNode == null) {
             setVisibleForChart(true);
             return;
         }
         String chartTitle = ActionUtil.runJobWithResult(new RunnableWithResult<String>() {
             String title;
 
             @Override
             public String getValue() {
                 return title;
             }
 
             @Override
             public void run() {
                 Transaction tx = NeoUtils.beginTransaction();
                 NeoUtils.addTransactionLog(tx, Thread.currentThread(), "setTitle");
                 try {
                     if ((Boolean)aggrNode.getProperty(INeoConstants.PROPERTY_CHART_ERROR_NAME, false)) {
                         title = null;
                         return;
                     }
                     title = aggrNode.getProperty(INeoConstants.PROPERTY_NAME_NAME, "").toString();
                 } finally {
                     tx.finish();
                 }
             }
 
         });
         if (chartTitle == null) {
             chart.setTitle(ERROR_CHART);
             chart.fireChartChanged();
             setVisibleForChart(true);
             return;
         }
         chart.setTitle(chartTitle);
         // dbJob.schedule();
         currentPalette = getPalette(aggrNode);
         colorLeft.setColorValue(getColorLeft(aggrNode));
         colorRight.setColorValue(getColorRight(aggrNode));
         colorMiddle.setColorValue(getColorMiddle(aggrNode));
 
         String[] array = cPalette.getItems();
         int index = -1;
         if (currentPalette != null) {
             for (int i = 0; i < array.length; i++) {
                 if (currentPalette.getName().equals(array[i])) {
                     index = i;
                     break;
                 }
 
             }
         }
         dataset.setAggrNode(aggrNode);
         // if (aggrNode.getProperty(INeoConstants.PROPERTY_CHART_ERROR_NAME,false))
         middleColumn = getMidleRange(aggrNode);
         if (middleColumn != null) {
             ttblendInformation.setText(middleColumn.toString());
         }
         if (index < 0) {
             changePalette();
         } else {
             cPalette.select(index);
         }
 
         setSelection(null);
         setVisibleForChart(true);
         chart.fireChartChanged();
     }
 
     /**
      * Gets color of right bar
      * 
      * @param aggrNode aggregation node
      * @return RGB
      */
     private RGB getColorRight(Node aggrNode) {
         if (aggrNode != null) {
             int[] colors = (int[])aggrNode.getProperty(INeoConstants.COLOR_RIGHT, null);
             if (colors != null) {
                 return new RGB(colors[0], colors[1], colors[2]);
             }
         }
         return DEFAULT_RIGHT;
     }
 
     /**
      * Gets color of middle bar
      * 
      * @param aggrNode aggregation node
      * @return RGB
      */
     private RGB getColorMiddle(Node aggrNode) {
         if (aggrNode != null) {
             int[] colors = (int[])aggrNode.getProperty(INeoConstants.COLOR_MIDDLE, null);
             if (colors != null) {
                 return new RGB(colors[0], colors[1], colors[2]);
             }
         }
         return DEFAULT_MIDDLE;
     }
 
     /**
      * Gets color of middle bar
      * 
      * @param aggrNode aggregation node
      * @return RGB
      */
     private ChartNode getMidleRange(Node aggrNode) {
         if (aggrNode != null) {
             String range = (String)aggrNode.getProperty(INeoConstants.MIDDLE_RANGE, null);
             if (range != null) {
                 return findColumnByName(range);
             }
         }
         return getDefaultRange();
     }
 
     /**
      * Gets color of left bar
      * 
      * @param aggrNode aggregation node
      * @return RGB
      */
     private RGB getColorLeft(Node aggrNode) {
         if (aggrNode != null) {
             int[] colors = (int[])aggrNode.getProperty(INeoConstants.COLOR_LEFT, null);
             if (colors != null) {
                 return new RGB(colors[0], colors[1], colors[2]);
             }
         }
         return DEFAULT_LEFT;
     }
 
     /**
      * Updates chart and redraw layer
      * 
      * @param aggrNode - new node
      */
     protected void chartUpdate() {
         Node gisNode = members.get(gisCombo.getText());
         Node aggrNode = dataset.getAggrNode();
         changeBarColor();
         chart.fireChartChanged();
         fireLayerDrawEvent(gisNode, aggrNode, null);
         if (model != null) {
             for (Pair<Node, Node> pair : model.getCorrelationUpdate()) {
                 fireLayerDrawEvent(pair.getLeft(), pair.getRight(), null);
             }
         }
     }
 
     protected void changeBarColor() {
         ChartNode selColumn = getSelectedColumn();
         int columnIndex = selColumn == null ? -1 : dataset.getColumnIndex(selColumn);
         ChartNode midColumn = middleColumn;
         int midColumnIndex = midColumn == null ? -1 : dataset.getColumnIndex(midColumn);
         RGB leftRgb = colorLeft.getColorValue();
         RGB rightRgb = colorRight.getColorValue();
         RGB middleRgb = colorMiddle.getColorValue();
         int size = dataset.nodeList.size() - 1;
         float ratio = 0;
         float ratio2 = 0;
         float perc = size <= 0 ? 1 : (float)1 / size;
         float percMid1 = midColumnIndex == 0 ? 1 : (float)1 / (midColumnIndex);
         float percMid2 = size - midColumnIndex == 0 ? 1 : (float)1 / (size - midColumnIndex);
         for (int i = 0; i < dataset.nodeList.size(); i++) {
             ChartNode chart = dataset.nodeList.get(i);
             if (isColorThema()) {
                 if (blend.getSelection()) {
                     if (threeBlend.getSelection() && midColumnIndex >= 0) {
                         if (leftRgb != null && rightRgb != null) {
                             RGB colrRgb;
                             if (i < midColumnIndex) {
                                 colrRgb = blend(leftRgb, middleRgb, ratio);
                                 ratio += percMid1;
                             } else if (i == midColumnIndex) {
                                 colrRgb = middleRgb;
                             } else {
                                 ratio2 += percMid2;
                                 if (ratio2 > 1) {
                                     ratio2 = 1;
                                 }
                                 colrRgb = blend(middleRgb, rightRgb, ratio2);
                             }
 
                             chart.saveColor(new Color(colrRgb.red, colrRgb.green, colrRgb.blue));
                         }
 
                     } else {
                         if (leftRgb != null && rightRgb != null) {
                             RGB colrRgb = blend(leftRgb, rightRgb, ratio);
                             ratio += perc;
                             chart.saveColor(new Color(colrRgb.red, colrRgb.green, colrRgb.blue));
                         }
                     }
                 } else {
                     if (currentPalette == null) {
                         chart.saveColor(null);
                     } else {
                         Color[] colors = currentPalette.getColors(currentPalette.getMaxColors());
                         int index = i % colors.length;
                         chart.saveColor(colors[index]);
                     }
                 }
 
             } else {
                 if (selColumn == null || columnIndex < 0) {
                     // we must clear color of node
                     chart.saveColor(null);
                 } else {
                     if (i == columnIndex) {
                         chart.saveColor(COLOR_SELECTED);
                         continue;
                     }
                     if (Math.abs(i - columnIndex) <= spinAdj.getSelection()) {
                         Color paint = i > columnIndex ? COLOR_MORE : COLOR_LESS;
                         chart.saveColor(paint);
                     } else {
                         // we must clear color of node
                         chart.saveColor(null);
                     }
                 }
             }
         }
     }
     
     /**
      * Select column
      * 
      * @param columnKey - column
      */
     private void setSelection(ChartNode columnKey) {
 
         Node realGis = NeoUtils.findGisNodeByChild(members.get(gisCombo.getText()));
         List<Node> correlated = new ArrayList<Node>();
         if (realGis == null) {
             correlated = NeoUtils.getCorrelationNetworks(members.get(gisCombo.getText()), NeoServiceProvider.getProvider().getService());
         }
         else {
             correlated.add(realGis);
         }
         
         Node aggrNode = dataset.getAggrNode();
         
         for (Node gisNode : correlated) {
             if (selectedColumn != null) {
                 selectedColumn = columnKey;
                 changeBarColor();
                 if (selectedGisNode.equals(gisNode)) {
                     fireLayerDrawEvent(gisNode, aggrNode, selectedColumn);
                 } else {
                     // drop old selection
                     selectedGisNode.removeProperty(INeoConstants.PROPERTY_SELECTED_AGGREGATION);
                     fireLayerDrawEvent(selectedGisNode, null, null);
                     selectedGisNode = gisNode;
                     fireLayerDrawEvent(selectedGisNode, aggrNode, selectedColumn);
                 }
             } else {
                 selectedColumn = columnKey;
                 selectedGisNode = gisNode;
                 changeBarColor();
                 fireLayerDrawEvent(gisNode, aggrNode, selectedColumn);
             }
         }
         setSelectionName(columnKey);
 
         chart.fireChartChanged();
     }
 
     /**
      * @param columnKey
      */
     private void setSelectionName(ChartNode columnKey) {
         if (columnKey == null) {
             tSelectedInformation.setText("");
         } else {
             tSelectedInformation.setText(columnKey.toString());
         }
     }
 
     /**
      * @param columnKey
      */
     private void setMiddleSelectionName(ChartNode columnKey) {
         if (columnKey == null) {
             ttblendInformation.setText("");
         } else {
             ttblendInformation.setText(columnKey.toString());
         }
     }
 
     /**
      * @return
      */
     private ChartNode getSelectedColumn() {
         return selectedColumn;
     }
 
     /**
      * fires layer redraw
      * 
      * @param columnKey property node for redraw action
      */
     protected void fireLayerDrawEvent(Node gisNode, Node aggrNode, ChartNode columnKey) {
         gisNode = NeoUtils.findGisNodeByChild(gisNode);
         // necessary for visible changes in renderers
         NeoServiceProvider.getProvider().commit();
         int adj = spinAdj.getSelection();
         Node columnNode = columnKey == null ? null : columnKey.getNode();
         int colInd = columnKey == null ? 0 : dataset.getColumnIndex(columnKey);
         int minInd = columnKey == null ? 0 : Math.max(colInd - adj, 0);
         int maxind = columnKey == null ? 0 : Math.min(colInd + adj, dataset.getColumnCount() - 1);
         Node node1 = columnKey == null ? null : ((ChartNode)dataset.getColumnKey(minInd)).getNode();
         Node node2 = columnKey == null ? null : ((ChartNode)dataset.getColumnKey(maxind)).getNode();
         RefreshPropertiesEvent event = new RefreshPropertiesEvent(gisNode, aggregatedProperties, aggrNode, columnNode, node1, node2);
         NeoCatalogPlugin.getDefault().getLayerManager().sendUpdateMessage(event);
     }
 
     /**
      * Finds aggregate node or creates if node does not exist
      * 
      * @param rootNode GIS node
      * @param propertyName name of property
      * @return necessary aggregates node
      */
     protected void findOrCreateAggregateNodeInNewThread(final Node rootNode, final String propertyName) {
         // TODO restore focus after job execute or not necessary?
         String select = cSelect.getText();
         // TODO Pechko_E: during refactoring of the following code
         // refactor also generateReport()
         if (!cSelect.isEnabled()) {
             select = Select.EXISTS.toString();
         }
         mainView.setEnabled(false);
         ComputeStatisticsJob job = new ComputeStatisticsJob(rootNode, propertyName, cDistribute.getText(), select);
         job.schedule();
     }
 
     private class ComputeStatisticsJob extends Job {
 
         private final Node gisNode;
         private final String propertyName;
         private Node node;
         private String distribute;
         private final String select;
 
         public ComputeStatisticsJob(Node gisNode, String propertyName, String distribute, String select) {
             super("calculating statistics");
             this.gisNode = gisNode;
             this.propertyName = propertyName;
             this.distribute = distribute;
             this.select = select;
         }
 
         @Override
         public IStatus run(IProgressMonitor monitor) {
             try {
                 if (!calculate(monitor)) {
                     if (!distribute.equals(Distribute.AUTO.toString())) {
                         distribute = Distribute.AUTO.toString();
                         final Runnable setAutoDistribute = new Runnable() {
                             @Override
                             public void run() {
                                 cDistribute.select(0);
 
                             }
                         };
                         ActionUtil.getInstance().runTask(setAutoDistribute, true);
                         calculate(monitor);
                     }
                 }
                 ActionUtil.getInstance().runTask(new Runnable() {
                     @Override
                     public void run() {
                         Transaction tx = NeoUtils.beginTransaction();
                         try {
                             chartUpdate(node);
                         } finally {
                             tx.finish();
                         }
                     }
                 }, true);
 
             } finally {
                 ActionUtil.getInstance().runTask(new Runnable() {
                     @Override
                     public void run() {
                         Transaction tx = NeoUtils.beginTransaction();
                         try {
                             mainView.setEnabled(true);
                         } finally {
                             tx.finish();
                         }
                     }
                 }, true);
             }
             return Status.OK_STATUS;
         }
 
         private boolean calculate(IProgressMonitor monitor) {
             Transaction tx = NeoUtils.beginTransaction();
             NeoUtils.addTransactionLog(tx, Thread.currentThread(), "ComputeStatisticsJob");
             try {
                 node = model.findOrCreateAggregateNode(gisNode, propertyName, isStringProperty(propertyName), distribute, select, monitor);
                 Boolean haveError = (Boolean)node.getProperty(INeoConstants.PROPERTY_CHART_ERROR_NAME, false);
                 if (haveError) {
                     final String errDescr = String.valueOf(node.getProperty(INeoConstants.PROPERTY_CHART_ERROR_DESCRIPTION, ""));
                     ActionUtil.getInstance().runTask(new Runnable() {
 
                         @Override
                         public void run() {
                             MessageDialog.openError(Display.getCurrent().getActiveShell(), ERROR_TITLE, errDescr);
                         }
                     }, true);
                 }
                 tx.success();
                 return !haveError;
             } finally {
                 tx.finish();
             }
         }
 
     }
 
     /**
      * @param propertyName
      * @return
      */
     private boolean isAggregatedProperty(String propertyName) {
         return aggregatedProperties.keySet().contains(propertyName);
     }
 
     /**
      * Creation list of property by selected node
      * 
      * @param node - selected node
      */
     private void formPropertyList(Node node) {
 
         aggregatedProperties.clear();
         propertyList = new ArrayList<String>();
         PropertyHeader propertyHeader = new PropertyHeader(node);
         allFields = Arrays.asList(propertyHeader.getAllFields());
         numericFields = Arrays.asList(propertyHeader.getNumericFields());
         propertyList.addAll(allFields);
         propertyList.addAll(propertyHeader.getNeighbourList());
         String[] channels = propertyHeader.getAllChannels();
         if (channels != null && channels.length > 0) {
             aggregatedProperties.put(INeoConstants.PROPERTY_ALL_CHANNELS_NAME, channels);
             propertyList.add(INeoConstants.PROPERTY_ALL_CHANNELS_NAME);
         }
         setVisibleForChart(false);
         final String nodeTypeId = getNodeTypeId(node);
         ReturnableEvaluator propertyReturnableEvalvator = new ReturnableEvaluator() {
 
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 return currentPos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME, "").equals(nodeTypeId);
             }
         };
         propertyList = new PropertyFilterModel().filerProperties(gisCombo.getText(), propertyList);
         model = new ReuseAnalyserModel(aggregatedProperties, propertyReturnableEvalvator, NeoServiceProvider.getProvider().getService());
 
     }
 
     /**
      * Gets the node type id.
      * 
      * @param node the node
      * @return the node type id
      */
     private String getNodeTypeId(Node node) {
         GraphDatabaseService service = NeoServiceProvider.getProvider().getService();
         return NeoUtils.getPrimaryType(node, service);
     }
 
     /**
      * Forms list of GIS nodes
      * 
      * @return array of GIS nodes
      */
     private String[] getRootItems() {
         GraphDatabaseService service = NeoServiceProvider.getProvider().getService();
         members = new LinkedHashMap<String, Node>();
         for (Node node : NeoUtils.getAllRootTraverser(service, null)) {
             String id = node.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
             members.put(id, node);
             for (Relationship ret : node.getRelationships(GeoNeoRelationshipTypes.VIRTUAL_DATASET, Direction.OUTGOING)) {
                 Node neigh = ret.getOtherNode(node);
                 members.put((String)neigh.getProperty(INeoConstants.PROPERTY_NAME_NAME), neigh);
             }
             for (Relationship ret : node.getRelationships(NetworkRelationshipTypes.NEIGHBOUR_DATA, Direction.OUTGOING)) {
                 Node neigh = ret.getOtherNode(node);
                 members.put(id + ": " + neigh.getProperty(INeoConstants.PROPERTY_NAME_NAME), neigh);
             }
             for (Relationship ret : node.getRelationships(NetworkRelationshipTypes.TRANSMISSION_DATA, Direction.OUTGOING)) {
                 Node neigh = ret.getOtherNode(node);
                 members.put(id + "-> " + neigh.getProperty(INeoConstants.PROPERTY_NAME_NAME), neigh);
             }
         }
         List<String> result = new ArrayList<String>(members.keySet());
         Collections.sort(result);
         return result.toArray(new String[] {});
     }
 
     /**
      * sets necessary layout
      * 
      * @param parent parent component
      */
     private void layoutComponents(Composite parent) {
         FormLayout layout = new FormLayout();
         layout.marginHeight = 0;
         layout.marginWidth = 0;
         layout.spacing = 0;
         parent.setLayout(layout);
 
         FormData dLabel = new FormData(); // bind to left & text
         dLabel.left = new FormAttachment(0, 5);
         dLabel.top = new FormAttachment(gisCombo, 5, SWT.CENTER);
         gisSelected.setLayoutData(dLabel);
 
         FormData dCombo = new FormData(); // bind to label and text
         dCombo.left = new FormAttachment(gisSelected, 2);
         dCombo.top = new FormAttachment(0, 2);
         dCombo.right = new FormAttachment(20, -5);
         gisCombo.setLayoutData(dCombo);
 
         dLabel = new FormData(); // bind to left & text
         dLabel.left = new FormAttachment(gisCombo, 10);
         dLabel.top = new FormAttachment(propertyCombo, 5, SWT.CENTER);
         propertySelected.setLayoutData(dLabel);
 
         dCombo = new FormData(); // bind to label and text
         dCombo.left = new FormAttachment(propertySelected, 2);
         dCombo.top = new FormAttachment(0, 2);
         dCombo.right = new FormAttachment(50, -5);
         propertyCombo.setLayoutData(dCombo);
 
         dLabel = new FormData(); // bind to left & text
         dLabel.left = new FormAttachment(propertyCombo, 10);
         dLabel.top = new FormAttachment(cDistribute, 5, SWT.CENTER);
         lDistribute.setLayoutData(dLabel);
 
         dCombo = new FormData(); // bind to label and text
         dCombo.left = new FormAttachment(lDistribute, 2);
         dCombo.top = new FormAttachment(0, 2);
         dCombo.right = new FormAttachment(68, -5);
         cDistribute.setLayoutData(dCombo);
 
         dLabel = new FormData(); // bind to left & text
         dLabel.left = new FormAttachment(cDistribute, 10);
         dLabel.top = new FormAttachment(cSelect, 5, SWT.CENTER);
         lSelect.setLayoutData(dLabel);
 
         dCombo = new FormData(); // bind to label and text
         dCombo.left = new FormAttachment(lSelect, 2);
         dCombo.top = new FormAttachment(0, 2);
         dCombo.right = new FormAttachment(82, -5);
         cSelect.setLayoutData(dCombo);
 
         dCombo = new FormData(); // bind to label and text
         dCombo.left = new FormAttachment(0, 2);
         dCombo.bottom = new FormAttachment(100, -2);
         bColorProperties.setLayoutData(dCombo);
 
         dLabel = new FormData();
         dLabel.left = new FormAttachment(bColorProperties, 2);
         dLabel.top = new FormAttachment(bColorProperties, 5, SWT.CENTER);
         lColorProperties.setLayoutData(dLabel);
 
         dCombo = new FormData(); // bind to label and text
         dCombo.left = new FormAttachment(lColorProperties, 2);
         dCombo.bottom = new FormAttachment(100, -2);
         bLogarithmic.setLayoutData(dCombo);
 
         dLabel = new FormData();
         dLabel.left = new FormAttachment(bLogarithmic, 2);
         dLabel.top = new FormAttachment(bLogarithmic, 5, SWT.CENTER);
         lLogarithmic.setLayoutData(dLabel);
 
         dLabel = new FormData();
         dLabel.left = new FormAttachment(blend, 5);
         dLabel.top = new FormAttachment(blend, 5, SWT.CENTER);
         lBlend.setLayoutData(dLabel);
 
         FormData dText = new FormData();
         dText.left = new FormAttachment(lLogarithmic, 15);
         dText.bottom = new FormAttachment(100, -2);
         blend.setLayoutData(dText);
         // ---
         dLabel = new FormData();
         dLabel.left = new FormAttachment(lBlend, 15);
         dLabel.bottom = new FormAttachment(100, -2);
         colorLeft.getButton().setLayoutData(dLabel);
 
         dLabel = new FormData();
         dLabel.left = new FormAttachment(colorLeft.getButton(), 15);
         dLabel.bottom = new FormAttachment(100, -2);
         colorRight.getButton().setLayoutData(dLabel);
 
         dLabel = new FormData();
         dLabel.left = new FormAttachment(threeBlend, 5);
         dLabel.top = new FormAttachment(threeBlend, 5, SWT.CENTER);
         lThreeBlend.setLayoutData(dLabel);
 
         dText = new FormData();
         dText.left = new FormAttachment(colorRight.getButton(), 15);
         dText.bottom = new FormAttachment(100, -2);
         threeBlend.setLayoutData(dText);
         // --->
         dLabel = new FormData();
         dLabel.left = new FormAttachment(lThreeBlend, 15);
         dLabel.bottom = new FormAttachment(100, -2);
         colorMiddle.getButton().setLayoutData(dLabel);
 
         dLabel = new FormData();
         dLabel.left = new FormAttachment(colorMiddle.getButton(), 15);
         dLabel.top = new FormAttachment(threeBlend, 5, SWT.CENTER);
         ltblendInformation.setLayoutData(dLabel);
 
         dText = new FormData();
         dText.left = new FormAttachment(ltblendInformation, 5);
         dText.bottom = new FormAttachment(100, -2);
         ttblendInformation.setLayoutData(dText);
         // --->
         // ---
         dLabel = new FormData();
         dLabel.left = new FormAttachment(lBlend, 15);
         dLabel.top = new FormAttachment(cPalette, 5, SWT.CENTER);
         lPalette.setLayoutData(dLabel);
 
         dText = new FormData();
         dText.left = new FormAttachment(lPalette, 5);
         dText.right = new FormAttachment(lPalette, 200);
         dText.bottom = new FormAttachment(100, -2);
         cPalette.setLayoutData(dText);
 
         dLabel = new FormData();
         dLabel.left = new FormAttachment(lLogarithmic, 15);
         dLabel.top = new FormAttachment(tSelectedInformation, 5, SWT.CENTER);
         lSelectedInformation.setLayoutData(dLabel);
 
         dText = new FormData();
         dText.left = new FormAttachment(lSelectedInformation, 5);
         dText.right = new FormAttachment(lSelectedInformation, 200);
         dText.bottom = new FormAttachment(100, -2);
         tSelectedInformation.setLayoutData(dText);
 
         dLabel = new FormData();
         dLabel.left = new FormAttachment(tSelectedInformation, 5);
         dLabel.top = new FormAttachment(spinAdj, 5, SWT.CENTER);
         spinLabel.setLayoutData(dLabel);
 
         FormData dSpin = new FormData();
         dSpin.left = new FormAttachment(spinLabel, 5);
         dSpin.top = new FormAttachment(tSelectedInformation, 5, SWT.CENTER);
         spinAdj.setLayoutData(dSpin);
 
         FormData dChart = new FormData(); // bind to label and text
         dChart.left = new FormAttachment(0, 5);
         dChart.top = new FormAttachment(gisSelected, 10);
         dChart.bottom = new FormAttachment(tSelectedInformation, -2);
         dChart.right = new FormAttachment(100, -5);
         chartFrame.setLayoutData(dChart);
 
         FormData dReport = new FormData();
         // dReport.left = new FormAttachment(ttblendInformation, 15);
         dReport.right = new FormAttachment(100, -2);
         dReport.top = new FormAttachment(tSelectedInformation, 5, SWT.CENTER);
         bReport.setLayoutData(dReport);
     }
 
     @Override
     public void setFocus() {
     }
 
     // /**
     // * <p>
     // * Implementation of ReturnableEvaluator Returns necessary MS or sector nodes
     // * </p>
     // *
     // * @author Cinkel_A
     // * @since 1.0.0
     // */
     // private static final class PropertyReturnableEvalvator implements ReturnableEvaluator {
     // private final HashSet<String> propertyList;
     //
     // public PropertyReturnableEvalvator() {
     // super();
     // propertyList = new HashSet<String>();
     // propertyList.add(NodeTypes.M.getId());
     // propertyList.add("mv");
     // propertyList.add(NodeTypes.SECTOR.getId());
     // propertyList.add(NodeTypes.HEADER_MS.getId());
     // propertyList.add(NodeTypes.PROBE.getId());
     // propertyList.add(NodeTypes.CALL.getId());
     // propertyList.add(NodeTypes.UTRAN_DATA.getId());
     // propertyList.add(NodeTypes.GPEH_EVENT.getId());
     // }
     //
     // @Override
     // public boolean isReturnableNode(TraversalPosition traversalposition) {
     // Node curNode = traversalposition.currentNode();
     // Object type = curNode.getProperty(INeoConstants.PROPERTY_TYPE_NAME, null);
     // return type != null && propertyList.contains(type);
     // }
     // }
 
     /**
      * <p>
      * Implementation of CategoryDataset Only for mapping. Does not support complete functionality.
      * </p>
      * 
      * @author Cinkel_A
      * @since 1.0.0
      */
     private static class PropertyCategoryDataset extends AbstractDataset implements CategoryDataset {
 
         /** long serialVersionUID field */
         private static final long serialVersionUID = -1941659139984700171L;
 
         private Node aggrNode;
         private List<String> rowList = new ArrayList<String>();
         private final List<ChartNode> nodeList = Collections.synchronizedList(new LinkedList<ChartNode>());
 
         /**
          * @return Returns the nodeList.
          */
         public List<ChartNode> getNodeList() {
             return nodeList;
         }
 
         /**
          * sets palette name into aggregation node
          * 
          * @param currentPalette
          */
         public void setPalette(final BrewerPalette currentPalette) {
             Job job = new Job("setPalette") {
                 @Override
                 protected IStatus run(IProgressMonitor monitor) {
                     Transaction tx = NeoUtils.beginTransaction();
                     NeoUtils.addTransactionLog(tx, Thread.currentThread(), "setPalette");
 
                     try {
                         if (aggrNode != null) {
                             if (currentPalette != null) {
                                 aggrNode.setProperty(INeoConstants.PALETTE_NAME, currentPalette.getName());
                             } else {
                                 aggrNode.removeProperty(INeoConstants.PALETTE_NAME);
                             }
                         }
                         return Status.OK_STATUS;
                     } finally {
                         tx.finish();
                     }
                 }
 
             };
             job.schedule();
             try {
                 job.join();
             } catch (InterruptedException e) {
                 // TODO Handle InterruptedException
                 throw (RuntimeException)new RuntimeException().initCause(e);
             }
         }
 
         PropertyCategoryDataset() {
             super();
             rowList = new ArrayList<String>();
             rowList.add(ROW_KEY);
             aggrNode = null;
         }
 
         /**
          * Gets aggregation node
          * 
          * @return aggregation node
          */
         public Node getAggrNode() {
             return aggrNode;
         }
 
         /**
          * Sets aggregation node
          * 
          * @param aggrNode new node
          */
         public void setAggrNode(final Node aggrNode) {
             Job job = new Job("setAggrNode") {
 
                 @Override
                 protected IStatus run(IProgressMonitor monitor) {
                     Transaction tx = NeoUtils.beginTransaction();
                     NeoUtils.addTransactionLog(tx, Thread.currentThread(), "setAggrNode");
                     try {
                         Iterator<Node> iteratorChild = aggrNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, ReturnableEvaluator.ALL_BUT_START_NODE,
                                 NetworkRelationshipTypes.CHILD, Direction.OUTGOING).iterator();
                         nodeList.clear();
                         while (iteratorChild.hasNext()) {
                             Node node = iteratorChild.next();
                             nodeList.add(new ChartNode(node));
                         }
                         return Status.OK_STATUS;
                     } finally {
                         tx.finish();
                     }
                 }
             };
             this.aggrNode = aggrNode;
             job.schedule();
             try {
                 job.join();
             } catch (InterruptedException e) {
                 // TODO Handle InterruptedException
                 throw (RuntimeException)new RuntimeException().initCause(e);
             }
             fireDatasetChanged();
         }
 
         @SuppressWarnings("unchecked")
         @Override
         public int getColumnIndex(Comparable comparable) {
             return nodeList.indexOf(comparable);
         }
 
         @Override
         public Comparable<ChartNode> getColumnKey(int i) {
             return nodeList.get(i);
         }
 
         @Override
         public List<ChartNode> getColumnKeys() {
             return nodeList;
         }
 
         @SuppressWarnings("unchecked")
         @Override
         public int getRowIndex(Comparable comparable) {
             return 0;
         }
 
         @Override
         public Comparable<String> getRowKey(int i) {
             return ROW_KEY;
         }
 
         @Override
         public List<String> getRowKeys() {
             return rowList;
         }
 
         @SuppressWarnings("unchecked")
         @Override
         public Number getValue(Comparable comparable0, Comparable comparable1) {
             if (!(comparable1 instanceof ChartNode)) {
                 return 0;
             }
             try {
                 return ((Number)((ChartNode)comparable1).getNode().getProperty(INeoConstants.PROPERTY_VALUE_NAME)).intValue();
             } catch (Exception e) {
                 // TODO Handle Exception
                 throw (RuntimeException)new RuntimeException().initCause(e);
             }
         }
 
         @Override
         public int getColumnCount() {
             return nodeList.size();
         }
 
         @Override
         public int getRowCount() {
             return 1;
         }
 
         @Override
         public Number getValue(int i, int j) {
             return getValue(i, getColumnKey(j));
         }
 
     }
 
     /**
      * <p>
      * Wrapper of chart node
      * </p>
      * 
      * @author Cinkel_A
      * @since 1.0.0
      */
     private static class ChartNode implements Comparable<ChartNode> {
         private final Node node;
         private final Double nodeKey;
         private final String columnValue;
         private Color color;
 
         ChartNode(Node aggrNode) {
             node = aggrNode;
             nodeKey = ((Number)aggrNode.getProperty(INeoConstants.PROPERTY_NAME_MIN_VALUE)).doubleValue();
             columnValue = aggrNode.getProperty(INeoConstants.PROPERTY_NAME_NAME, "").toString();
         }
 
         /**
          * Returns true if value in selected column;
          * 
          * @param value value to find
          * @return true if value in selected column;
          */
         public boolean containsValue(double value) {
             double minValue = (Double)node.getProperty(INeoConstants.PROPERTY_NAME_MIN_VALUE);
             double maxValue = (Double)node.getProperty(INeoConstants.PROPERTY_NAME_MAX_VALUE);
             return value >= minValue && (value == minValue || value < maxValue);
         }
 
         @Override
         public int compareTo(ChartNode o) {
             return Double.compare(getNodeKey(), o.getNodeKey());
         }
 
         /**
          * @return Returns the node.
          */
         public Node getNode() {
             return node;
         }
 
         @Override
         public String toString() {
             return columnValue;
         }
 
         /**
          * @return Returns the nodeKey.
          */
         public Double getNodeKey() {
             return nodeKey;
         }
 
         /**
          * save colors
          * 
          * @param color - color
          */
         public void saveColor(final Color color) {
             if (this.color != null && this.color.equals(color)) {
                 return;
             }
             this.color = color;
             if (node != null) {
                 Job job = new Job("save color") {
 
                     @Override
                     protected IStatus run(IProgressMonitor monitor) {
                         Transaction tx = NeoUtils.beginTransaction();
                         try {
                             Integer valueToSave = color == null ? null : color.getRGB();
                             if (valueToSave == null) {
                                 ColoredFlags flag = ColoredFlags.getFlagById((String)node.getProperty(INeoConstants.PROPERTY_FLAGGED_NAME, ColoredFlags.NONE.getId()));
                                 if(!flag.equals(ColoredFlags.NONE)){
                                     node.setProperty(INeoConstants.AGGREGATION_COLOR, flag.getRgb());
                                 }
                                 node.removeProperty(INeoConstants.AGGREGATION_COLOR);
                             } else {
                                 node.setProperty(INeoConstants.AGGREGATION_COLOR, valueToSave);
                             }
                             tx.success();
                         } finally {
                             tx.finish();
                         }
                         return Status.OK_STATUS;
                     }
                 };
                 job.schedule();
                 // job.join(); TODO test on necessary join
             }
         }
 
         /**
          * gets column color
          * 
          * @return color
          */
         public Color getColor() {
             return color;
         }
     }
 
     /**
      * A custom renderer that returns a different color for each items.
      */
     private class CustomRenderer extends BarRenderer {
         private static final long serialVersionUID = 8572109118229414964L;
 
         /**
          * Returns the paint for an item. Overrides the default behaviour inherited from
          * AbstractSeriesRenderer.
          * 
          * @param row the series.
          * @param column the category.
          * @return The item color.
          */
         @Override
         public Paint getItemPaint(final int row, final int column) {
             ChartNode col = dataset.getNodeList().get(column);
             return col == null || col.getColor() == null ? DEFAULT_COLOR : col.getColor();
 
         }
     }
 
     /**
      * updates list of gis nodes
      */
     public void updateGisNode() {
         setSelection(null);
         String[] gisItems = getRootItems();
         gisCombo.setItems(gisItems);
         propertyCombo.setItems(new String[] {});
         setVisibleForChart(false);
     }
 
     /**
      * @param column
      * @param color
      */
     public void setColumnColor(int column, Color color) {
         if (dataset != null) {
             ChartNode child = dataset.getNodeList().get(column);
             child.saveColor(color);
         }
     }
 
     /**
      * @param colorThema The colorThema to set.
      */
     private void setColorThema(boolean colorThema) {
         this.colorThema = colorThema;
     }
 
     /**
      * @return Returns the colorThema.
      */
     private boolean isColorThema() {
         return colorThema;
     }
 
     /**
      * gets palette
      * 
      * @return palette or null
      */
     public BrewerPalette getPalette(Node aggrNode) {
         if (aggrNode != null) {
             String palName = (String)aggrNode.getProperty(INeoConstants.PALETTE_NAME, null);
             if (palName != null) {
                 return PlatformGIS.getColorBrewer().getPalette(palName);
             }
         }
         return null;
     }
 
     /**
      * Blend color
      * 
      * @param bg left
      * @param fg right
      * @param factor factor (0-1)
      * @return RGB
      */
     public static RGB blend(RGB bg, RGB fg, float factor) {
         Assert.isLegal(bg != null);
         Assert.isLegal(fg != null);
         Assert.isLegal(factor >= -0.0001F && factor <= 1.0001F);
         if (factor < 0.0)
             factor = 0F;
         if (factor > 1.0)
             factor = 1F;
         float complement = 1.0F - factor;
         return new RGB((int)(complement * bg.red + factor * fg.red), (int)(complement * bg.green + factor * fg.green), (int)(complement * bg.blue + factor * fg.blue));
     }
 
     /**
      * Generates report based on selected values
      */
     private void generateReport() {
         IFile file;
         try {
             int i = 0;
             // Node node = selectedGisNode.getSingleRelationship(GeoNeoRelationshipTypes.NEXT,
             // Direction.OUTGOING).getEndNode();
             // find or create AWE and RDT project
             String aweProjectName = AWEProjectManager.getActiveProjectName();
             IRubyProject rubyProject;
             try {
                 rubyProject = NewRubyElementCreationWizard.configureRubyProject(null, aweProjectName);
             } catch (CoreException e2) {
                 // TODO Handle CoreException
                 throw (RuntimeException)new RuntimeException().initCause(e2);
             }
 
             final IProject project = rubyProject.getProject();
 
             while ((file = project.getFile(new Path(("report" + i) + ".r"))).exists()) {
                 i++;
             }
             // the following code depends on code from findOrCreateAggregateNodeInNewThread()
             final Select select = !cSelect.isEnabled() ? Select.EXISTS : Select.findSelectByValue(cSelect.getText());
             final String distribute = Distribute.findEnumByValue(cDistribute.getText()).getDescription();
             final String propName = propertyCombo.getText();
             StringBuffer sb = new StringBuffer("report '").append("Distribution analysis of ").append(gisCombo.getText()).append(" ").append(propName).append("' do\n");
             sb.append("  author '").append(System.getProperty("user.name")).append("'\n");
             sb.append("  date '").append(new SimpleDateFormat("yyyy-MM-dd").format(new Date())).append("'\n");
             sb.append("  text 'Distribution analysis of ").append(gisCombo.getText()).append(" ").append(propName).append(", with values distributed ")
                     .append(distribute).append(" and calculated using ").append(select.getDescription()).append("'\n");
             sb.append("  map 'Drive map', :map => GIS.maps.first.copy, :width => 600, :height => 400 do |m|\n");
             sb.append("    layer = m.layers.find(:type => 'drive').first\n");
             sb.append("  end\n");
             sb.append("  chart '").append(propName).append("' do |chart| \n");
             sb.append("    chart.domain_axis='Value'\n");
             sb.append("    chart.range_axis='Count'\n");
             sb.append("    chart.statistics='").append(gisCombo.getText()).append("'\n");
             sb.append("    chart.property='").append(propName).append("'\n");
             sb.append("    chart.distribute='").append(cDistribute.getText()).append("'\n");
             sb.append("    chart.select='").append(select.toString()).append("'\n");
             sb.append("  end\nend");
             LOGGER.debug("Report script:\n" + sb.toString());
             InputStream is = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
             file.create(is, true, null);
             is.close();
             getViewSite().getPage().openEditor(new FileEditorInput(file), ReportEditor.class.getName());
         } catch (Exception e) {
             e.printStackTrace();
             showErrorDlg(e);
         }
     }
 
     /**
      * Displays the error dialog for the exception
      * 
      * @param e exception to be printed
      */
     private void showErrorDlg(final Exception e) {
         final Display display = PlatformUI.getWorkbench().getDisplay();
         display.asyncExec(new Runnable() {
 
             @Override
             public void run() {
                 ErrorDialog.openError(display.getActiveShell(), "Error", "Report can't be created due to the following error:", new Status(Status.ERROR,
                         ReusePlugin.PLUGIN_ID, e.getClass().getName(), e));
             }
 
         });
     }
 
     @Override
     public void propertyChange(PropertyChangeEvent event) {
         if (event.getProperty().equals(NeoCorePreferencesConstants.FILTER_RULES)) {
             int selectedGisInd = gisCombo.getSelectionIndex();
             if (selectedGisInd < 0) {
                 propertyList = new ArrayList<String>();
                 setVisibleForChart(false);
                 tSelectedInformation.setText("");
             } else {
                 Node rootNode = members.get(gisCombo.getText());
                 cSelect.setEnabled(isAggregatedDataset(rootNode));
                 formPropertyList(rootNode);
             }
             Collections.sort(propertyList);
             propertyCombo.setItems(propertyList.toArray(new String[] {}));
         }
     }
 
     @Override
     public void init(IViewSite site) throws PartInitException {
         super.init(site);
         NeoCorePlugin.getDefault().getPluginPreferences().addPropertyChangeListener(this);
     }
 
     @Override
     public void dispose() {
         NeoCorePlugin.getDefault().getPluginPreferences().removePropertyChangeListener(this);
         super.dispose();
     }
 
 }
