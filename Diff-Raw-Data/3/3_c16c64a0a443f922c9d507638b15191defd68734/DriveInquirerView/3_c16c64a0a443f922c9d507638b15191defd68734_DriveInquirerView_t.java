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
 package org.amanzi.awe.views.drive.views;
 
 import java.awt.Color;
 import java.awt.Paint;
 import java.awt.Shape;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import net.refractions.udig.catalog.IGeoResource;
 import net.refractions.udig.project.ILayer;
 import net.refractions.udig.project.IMap;
 import net.refractions.udig.project.ui.ApplicationGIS;
 import net.refractions.udig.ui.PlatformGIS;
 import net.refractions.udig.ui.graphics.Glyph;
 
 import org.amanzi.awe.catalog.neo.GeoConstant;
 import org.amanzi.awe.catalog.neo.GeoNeo;
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.enums.GisTypes;
 import org.amanzi.neo.core.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.core.utils.Pair;
 import org.amanzi.neo.core.utils.PropertyHeader;
 import org.amanzi.neo.index.MultiPropertyIndex;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DateTime;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Slider;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.part.ViewPart;
 import org.geotools.brewer.color.BrewerPalette;
 import org.jfree.chart.ChartUtilities;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.AxisLocation;
 import org.jfree.chart.axis.DateAxis;
 import org.jfree.chart.axis.LogarithmicAxis;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.axis.ValueAxis;
 import org.jfree.chart.event.ChartProgressEvent;
 import org.jfree.chart.event.ChartProgressListener;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
 import org.jfree.chart.renderer.xy.XYBarRenderer;
 import org.jfree.data.Range;
 import org.jfree.data.category.CategoryDataset;
 import org.jfree.data.time.Millisecond;
 import org.jfree.data.time.TimeSeries;
 import org.jfree.data.time.TimeSeriesCollection;
 import org.jfree.data.time.TimeSeriesDataItem;
 import org.jfree.data.xy.AbstractIntervalXYDataset;
 import org.jfree.data.xy.AbstractXYDataset;
 import org.jfree.experimental.chart.swt.ChartComposite;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.ReturnableEvaluator;
 import org.neo4j.api.core.StopEvaluator;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.api.core.TraversalPosition;
 import org.neo4j.api.core.Traverser.Order;
 
 
 
 /**
  * <p>
  * Drive Inquirer View
  * </p>
  * 
  * @author Cinkel_A
  * @since 1.0.0
  */
 public class DriveInquirerView extends ViewPart {
 
     /** int MIN_FIELD_WIDTH field */
     private static final int MIN_FIELD_WIDTH = 50;
     /** Color PROPERTY fields */
     private static final Color[] PROPERTY_COLORS = new Color[]{Color.red,Color.black,Color.blue,
                                                                Color.green,Color.magenta,Color.yellow};
     
     private static final int MIN_PROP_COUNT = 2;
     private static final int MAX_PROP_COUNT = 6;
     /**
      * The ID of the view as specified by the extension.
      */
     public static final String ID = "org.amanzi.awe.views.drive.views.DriveInquirerView";
     private static final String CHART_TITLE = "";
     private static final String LOG_LABEL = "Logarithmic counts";
     private static final String PALETTE_LABEL = "Palette";
     private static final String ALL_EVENTS = "all events";
     protected static final String EVENT = "event_type";
     // memento keys
     private static final String MEM_DRIVE = "MEM_DRIVE";
     private static final String MEM_ADD_PROPERTY_COUNT = "MEM_ADD_PROPERTY_COUNT";
     private static final String MEM_ADD_PROPERTY = "MEM_ADD_PROPERTY";
     private static final String MEM_EVENT = "MEM_EVENT";
     private static final String MEM_START_TIME = "MEM_START_TIME";
     private static final String MEM_TIME_LENGTH = "MEM_TIME_LENGTH";
     private static final String MEM_LOGARITHM = "MEM_LOGARITHM";
     private static final String MEM_PALETTE = "MEM_PALETTE";
     private static final long SLIDER_STEP = 1000;//1 sek
 
     private Action addProperty;
     private JFreeChart chart;
     private ChartCompositeImpl chartFrame;
     private Combo cDrive;
     private Combo cEvent;
     private List<Combo> cProperties;
     private List<Label> addPropLabels;
     private Composite addPropBar;
     private Button bLeft;
     private Button bLeftHalf;
     private Button bRight;
     private Button bRightHalf;
     private Button bReport;
     private LinkedHashMap<String, Node> gisDriveNodes;
     private DateTime dateStart;
     private Long dateStartTimestamp = null;
     private Spinner sLength;
     private Label lLogarithmic;
     private Button bLogarithmic;
     private Label lPalette;
     private Combo cPalette;
     private Composite buttonLine;
     private List<TimeDataset> xydatasets;
     private EventDataset eventDataset;
     private List<LogarithmicAxis> axisLogs;
     private List<ValueAxis> axisNumerics;
     private ArrayList<String> eventList;
     private DateAxis domainAxis;
     private Long beginGisTime;
     private Long endGisTime;
     private Long selectedTime;
     // private List<Node> dataset;
     private int currentIndex;
     private int currentPropertyCount = MIN_PROP_COUNT;
     private TableViewer table;
     private TableLabelProvider labelProvider;
     private TableContentProvider provider;
     private Slider slider;
     /**
      * Index for Timestamps
      */
     MultiPropertyIndex<Long> timestampIndex = null;
     private Long oldStartTime = null;
     private Integer oldTimeLength = null;
     // init values
     private String initDrive;
     private String initEvent;
     private String initTime;
     private Integer initTimeLen;
     private String initLogarithm;
     private String initPalette;
     private Integer initPropCount;
     private List<String> initProperties;
     
     @Override
     public void createPartControl(Composite parent) {
         Composite frame = new Composite(parent, SWT.FILL);
         FormLayout formLayout = new FormLayout();
         formLayout.marginHeight = 0;
         formLayout.marginWidth = 0;
         formLayout.spacing = 0;
         frame.setLayout(formLayout);
         
         Composite child = new Composite(frame, SWT.FILL);
         FormData fData = new FormData();
         fData.top = new FormAttachment(0, 2);
         fData.left = new FormAttachment(0, 2);
         fData.right = new FormAttachment(100, -2);
 
         child.setLayoutData(fData);
         final GridLayout layout = new GridLayout(12, false);
         child.setLayout(layout);
         Label label = new Label(child, SWT.FLAT);
         label.setText(Messages.DriveInquirerView_label_drive);
         label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
         cDrive = new Combo(child, SWT.DROP_DOWN | SWT.READ_ONLY);
 
         GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
         layoutData.minimumWidth = MIN_FIELD_WIDTH;
         cDrive.setLayoutData(layoutData);
 
         label = new Label(child, SWT.FLAT);
         label.setText(Messages.DriveInquirerView_label_event);
         label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
         cEvent = new Combo(child, SWT.DROP_DOWN | SWT.READ_ONLY);
 
         layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
         layoutData.minimumWidth = MIN_FIELD_WIDTH;
         cEvent.setLayoutData(layoutData);
 
         cProperties = new ArrayList<Combo>(MAX_PROP_COUNT);
         
         label = new Label(child, SWT.NONE);
         label.setText(Messages.DriveInquirerView_label_property+"1:");
         label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
         Combo cProperty1 = new Combo(child, SWT.DROP_DOWN | SWT.READ_ONLY);
         layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
         layoutData.minimumWidth = MIN_FIELD_WIDTH;
         cProperty1.setLayoutData(layoutData);
         
         cProperties.add(cProperty1);
 
         label = new Label(child, SWT.FLAT);
         label.setText(Messages.DriveInquirerView_label_property+"2:");
         label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
         Combo cProperty2 = new Combo(child, SWT.DROP_DOWN | SWT.READ_ONLY);
 
         layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
         layoutData.minimumWidth = MIN_FIELD_WIDTH;
         cProperty2.setLayoutData(layoutData);
 
         cProperties.add(cProperty2);
         
         label = new Label(child, SWT.FLAT);
         label.setText(Messages.DriveInquirerView_label_start_time);
         label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
         dateStart = new DateTime(child, SWT.FILL | SWT.BORDER | SWT.TIME | SWT.LONG);
         GridData dateStartlayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
         dateStartlayoutData.minimumWidth = 75;
         dateStart.setLayoutData(dateStartlayoutData);
         
         label = new Label(child, SWT.FLAT);
         label.setText(Messages.DriveInquirerView_label_length);
         label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
         sLength = new Spinner(child, SWT.BORDER);
         sLength.setMinimum(1);
         sLength.setMaximum(1000);
         sLength.setSelection(5);
         GridData timeLenlayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
         timeLenlayoutData.minimumWidth = 45;
         sLength.setLayoutData(timeLenlayoutData);
         
         
         
         addPropBar = new Composite(frame, SWT.FILL);
         fData = new FormData();
         fData.top = new FormAttachment(child,2);
         fData.left = new FormAttachment(0, 2);
         fData.right = new FormAttachment(100, -2);
         addPropBar.setLayoutData(fData); 
         final GridLayout layout2 = new GridLayout(12, true);
         addPropBar.setLayout(layout2);
         addPropLabels = new ArrayList<Label>();
         for(int i=MIN_PROP_COUNT;i<MAX_PROP_COUNT; i++){
             label = new Label(addPropBar, SWT.NONE);
             label.setText(Messages.DriveInquirerView_label_property+(i+1)+":");
             label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
             label.setVisible(i<currentPropertyCount);
             Combo cProperty = new Combo(addPropBar, SWT.DROP_DOWN | SWT.READ_ONLY);
             layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
             layoutData.minimumWidth = MIN_FIELD_WIDTH;
             cProperty.setLayoutData(layoutData);
             cProperty.setVisible(i<currentPropertyCount);
             
             addPropLabels.add(label);
             cProperties.add(cProperty);
         }
         
         IMenuManager mm = getViewSite().getActionBars()
         .getMenuManager();        
         addProperty = new Action(Messages.DriveInquirerView_menu_add_property){
             @Override
             public void run(){
                 addNewProperty();
             }
         };
         mm.add(addProperty);
         
         chart = createChart();
         chartFrame = new ChartCompositeImpl(frame, SWT.NONE, chart, true);
         fData = new FormData();
         fData.top = new FormAttachment(addPropBar, 2);
         fData.left = new FormAttachment(0, 2);
         fData.right = new FormAttachment(100, -2);
         fData.bottom = new FormAttachment(100, -130);
 
         chartFrame.setLayoutData(fData);
 
         slider = new Slider(frame, SWT.NONE);
         slider.setValues(MIN_FIELD_WIDTH, 0, 300, 1, 1, 1);
         fData = new FormData();
         fData.left = new FormAttachment(0, 0);
         fData.right = new FormAttachment(100, 0);
         fData.top = new FormAttachment(chartFrame, 2);
         slider.setLayoutData(fData);
         slider.pack();
         table = new TableViewer(frame, SWT.BORDER | SWT.FULL_SELECTION);
         fData = new FormData();
         fData.left = new FormAttachment(0, 0);
         fData.right = new FormAttachment(100, 0);
         fData.top = new FormAttachment(slider, 2);
         fData.bottom = new FormAttachment(100, -30);
         table.getControl().setLayoutData(fData);
 
 
         labelProvider = new TableLabelProvider();
         labelProvider.createTableColumn();
         provider = new TableContentProvider();
         table.setContentProvider(provider);
 
         buttonLine = new Composite(frame, SWT.NONE);
         fData = new FormData();
         fData.left = new FormAttachment(0, 2);
         fData.right = new FormAttachment(100, -2);
         fData.bottom = new FormAttachment(100, -2);
         buttonLine.setLayoutData(fData);
         formLayout = new FormLayout();
         buttonLine.setLayout(formLayout);
 
         bLeft = new Button(buttonLine, SWT.PUSH);
         bLeft.setText("<<");
         bLeftHalf = new Button(buttonLine, SWT.PUSH);
         bLeftHalf.setText("<");
 
         bRight = new Button(buttonLine, SWT.PUSH);
         bRight.setText(">>");
         bRightHalf = new Button(buttonLine, SWT.PUSH);
         bRightHalf.setText(">");
 
         bReport = new Button(buttonLine, SWT.PUSH);
         bReport.setText("Report");
         
         FormData formData = new FormData();
         formData.left = new FormAttachment(0, 5);
         bLeft.setLayoutData(formData);
 
         formData = new FormData();
         formData.left = new FormAttachment(bLeft, 5);
         bLeftHalf.setLayoutData(formData);
 
         formData = new FormData();
         formData.right = new FormAttachment(100, -5);
         bRight.setLayoutData(formData);
 
         formData = new FormData();
         formData.right = new FormAttachment(bRight, -5);
         bRightHalf.setLayoutData(formData);
 
         lLogarithmic = new Label(buttonLine, SWT.NONE);
         lLogarithmic.setText(LOG_LABEL);
         bLogarithmic = new Button(buttonLine, SWT.CHECK);
         bLogarithmic.setSelection(false);
         lPalette = new Label(buttonLine, SWT.NONE);
         lPalette.setText(PALETTE_LABEL);
         cPalette = new Combo(buttonLine, SWT.DROP_DOWN | SWT.READ_ONLY);
         cPalette.setItems(PlatformGIS.getColorBrewer().getPaletteNames());
         cPalette.select(0);
 
         FormData dCombo = new FormData();
         dCombo.left = new FormAttachment(bLeftHalf, 10);
         dCombo.top = new FormAttachment(bLeftHalf, 0, SWT.CENTER);
         bLogarithmic.setLayoutData(dCombo);
 
         FormData dLabel = new FormData();
         dLabel.left = new FormAttachment(bLogarithmic, 2);
         dLabel.top = new FormAttachment(bLogarithmic, 5, SWT.CENTER);
         lLogarithmic.setLayoutData(dLabel);
 
         dCombo = new FormData();
         dCombo.left = new FormAttachment(lLogarithmic, 10);
         dCombo.top = new FormAttachment(cPalette, 5, SWT.CENTER);
         lPalette.setLayoutData(dCombo);
 
         dCombo = new FormData();
         dCombo.left = new FormAttachment(lPalette, 2);
         cPalette.setLayoutData(dCombo);
         
         FormData dReport  = new FormData();
         dReport.left = new FormAttachment(cPalette, 2);
         bReport.setLayoutData(dReport);
 
         setsVisible(false);
         cDrive.setItems(getDriveItems());
 
         addListeners();
         initializeStartupProperties();
     }
 
     /**
      * initialize startup properies;
      */
     private void initializeStartupProperties() {
         if (!setProperty(cDrive, initDrive)) {
             return;
         }
         formPropertyLists();
         currentPropertyCount = initPropCount;
         for(int i=0; i<currentPropertyCount;i++){
             setProperty(cProperties.get(i),initProperties.get(i));
         }
         setProperty(cEvent, initEvent);
         try {
             if (initTime != null) {
                 long beginTime = Long.parseLong(initTime);
                 setBeginTime(beginTime);
             }
         } catch (NumberFormatException e) {
             e.printStackTrace();
             NeoCorePlugin.error(e.getLocalizedMessage(), e);
         }
         if (initTimeLen != null) {
             sLength.setSelection(initTimeLen);
         }
         setProperty(cPalette, initPalette);
         try {
             if (initLogarithm != null) {
                 boolean l = Boolean.parseBoolean(initLogarithm);
                 bLogarithmic.setSelection(l);
             }
         } catch (NumberFormatException e) {
             e.printStackTrace();
             NeoCorePlugin.error(e.getLocalizedMessage(), e);
         }
         if (bLogarithmic.getSelection()) {
             XYPlot plot = (XYPlot)chart.getPlot();
             for(int i=0; i<currentPropertyCount;i++){
                LogarithmicAxis axisLog = axisLogs.get(i);
                plot.setRangeAxis(currentPropertyCount-i, axisLog);
                axisLog.autoAdjustRange();
             }
         }
         updateChart();
     }
 
     /**
      * Sets value into property
      * 
      * @param combo - Combo
      * @param value - value
      * @return if sets is correctly - return true else false
      */
     private boolean setProperty(Combo combo, String value) {
         if (combo == null || value == null) {
             return false;
         }
         for (int i = 0; i < combo.getItemCount(); i++) {
             if (combo.getItem(i).equals(value)) {
                 combo.select(i);
                 return true;
             }
         }
         return false;
     }
 
     /**
      * set chart visible
      * 
      * @param visible - is visible?
      */
     private void setsVisible(boolean visible) {
         chartFrame.setVisible(visible);
         table.getControl().setVisible(visible);
         buttonLine.setVisible(visible);
         slider.setVisible(visible);
     }
 
     /**
      *add listeners
      */
     private void addListeners() {
         cDrive.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 changeDrive();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetDefaultSelected(e);
             }
         });
         cEvent.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 updateProperty();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         
         for(Combo property : cProperties){
             addListenerToProperty(property);
         }
         
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
         bRightHalf.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 rightHalf();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         // change
         // dateStart.addSelectionListener(new SelectionListener() {
         //
         // @Override
         // public void widgetSelected(SelectionEvent e) {
         // changeDate();
         // }
         //
         // @Override
         // public void widgetDefaultSelected(SelectionEvent e) {
         // widgetSelected(e);
         // }
         // });
         dateStart.addFocusListener(new FocusListener() {
 
             @Override
             public void focusLost(FocusEvent e) {
                 changeDate();
             }
 
             @Override
             public void focusGained(FocusEvent e) {
             }
         });
         dateStart.addKeyListener(new KeyListener() {
 
             @Override
             public void keyReleased(KeyEvent e) {
                 if (e.keyCode == '\r' || e.keyCode == SWT.KEYPAD_CR) {
                     changeDate();
                 }
             }
 
             @Override
             public void keyPressed(KeyEvent e) {
             }
         });
         // change time length
         sLength.addFocusListener(new FocusListener() {
 
             @Override
             public void focusLost(FocusEvent e) {
                 changeTimeLenght();
             }
 
             @Override
             public void focusGained(FocusEvent e) {
             }
         });
         sLength.addKeyListener(new KeyListener() {
 
             @Override
             public void keyReleased(KeyEvent e) {
                 if (e.keyCode == '\r' || e.keyCode == SWT.KEYPAD_CR) {
                     changeTimeLenght();
                 }
             }
 
             @Override
             public void keyPressed(KeyEvent e) {
             }
         });
 
         bRight.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 right();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         bLeft.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 left();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         bLeftHalf.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 leftHalf();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         cPalette.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 fireEventUpdateChart();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         slider.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 changeSlider();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         chart.addProgressListener(new ChartProgressListener() {
 
             @Override
             public void chartProgress(ChartProgressEvent chartprogressevent) {
                 if (chartprogressevent.getType() != 2) {
                     return;
                 }
                 long domainCrosshairValue = (long)chart.getXYPlot().getDomainCrosshairValue();
                 if (domainCrosshairValue!=selectedTime){
                     selectedTime=domainCrosshairValue;
                     slider.setSelection((int)((selectedTime-beginGisTime)/SLIDER_STEP));
                 }
                 labelProvider.refreshTable();
                 table.setInput(0);
                 table.refresh();
             }
         });
         bReport.addSelectionListener(new SelectionAdapter(){
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 generateReport();
             }
             
         });
     }
     
     private void addNewProperty() {
         addPropLabels.get(currentPropertyCount-MIN_PROP_COUNT).setVisible(true);
         cProperties.get(currentPropertyCount++).setVisible(true);
         
         if(currentPropertyCount==MAX_PROP_COUNT){
             addProperty.setEnabled(false);
         }
         updateChart();
     }
 
     private void addListenerToProperty(Combo cProperty) {
         SelectionListener listener = new SelectionListener() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 updateProperty();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         };
         cProperty.addSelectionListener(listener);
     }
 
     /**
      * Change time length
      */
     protected void changeTimeLenght() {
         if (!isTimeLengthChanged()) {
             return;
         }
         updateChart();
         oldTimeLength = sLength.getSelection();
     }
 
     /**
      * @return
      */
     private boolean isTimeLengthChanged() {
         return oldTimeLength == null || sLength.getSelection() != oldTimeLength;
     }
 
     private void generateReport() {
         GregorianCalendar calendar = new GregorianCalendar();
         calendar.setTimeInMillis(beginGisTime);
         calendar.set(GregorianCalendar.HOUR_OF_DAY, dateStart.getHours());
         calendar.set(GregorianCalendar.MINUTE, dateStart.getMinutes());
         calendar.set(GregorianCalendar.SECOND, dateStart.getSeconds());
         System.out.println("[DEBUG]calendar.getTimeInMillis()"+calendar.getTimeInMillis());// TODO delete debug info
         
       
         final String TRAVERSE_NEXT_ALL = "traverse(:outgoing, :NEXT, :all)\n";
         final String TRAVERSE_CHILD_1 = "traverse(:outgoing, :CHILD, 1)\n";
         //TODO fix when drive loader will be fixed
 //        Long start_time=(((dateStart.getHours()-2L)*60+dateStart.getMinutes())*60+dateStart.getSeconds())*1000;
         Long start_time=calendar.getTimeInMillis();
         Long end_time=start_time+sLength.getSelection()*60*1000;
         if (selectedTime==null)
             selectedTime=start_time;
         Long delta_sec=2L;
         Long delta_msec=delta_sec*1000;
         
 //        System.out.println("time: "+dateStart.getHours()+":"+dateStart.getMinutes()+":"+dateStart.getSeconds());
         StringBuffer sb = new StringBuffer("report 'Drive ").append(cDrive.getText()).append("' do\n  author '").append(
                 System.getProperty("user.name")).append("'\n  date '")
                 .append(new SimpleDateFormat("yyyy-MM-dd").format(new Date())).append("'\n  chart 'Drive ").append(cDrive.getText()).append("' do\n");
         sb.append("    self.type=:time\n");
   
         //event dataset
         sb.append("    select 'event dataset', :categories=>'timestamp', :values=>'event_type', :time_period=>:millisecond, :event=>'")
         .append(cEvent.getText()).append("' do\n");
 //        sb.append("      from{\n");
         sb.append("      from{\n");
         sb.append("        ").append(TRAVERSE_CHILD_1);
         sb.append("        ").append("where {self[:type]=='gis' and self[:name]=='").append(cDrive.getText()).append("'}\n");
         sb.append("      }\n");
         sb.append("      ").append(TRAVERSE_NEXT_ALL);
         sb.append("      stop{property? 'timestamp' and self[:timestamp]>").append(end_time).append("}\n");
 //        sb.append("      }\n");
 //        sb.append("      ").append(TRAVERSE_CHILD_1);
         sb.append("      ").append("where {(property? 'timestamp' and self[:timestamp]<=").append(end_time)
         .append(" and self[:timestamp]>=").append(start_time).append(") and property? 'event_type'");
         if (!cEvent.getText().equals(ALL_EVENTS)){
             sb.append(" and self[:event_type]=='").append(cEvent.getText()).append("'}\n");
         }else{
             sb.append("}\n");
         }
         sb.append("    end\n");
         //property datasets
         String prefix = "    select 'property datasets', :categories=>'timestamp', :values=>['";
         for(int i=0; i<currentPropertyCount; i++){
             sb.append(prefix)
             .append(cProperties.get(i).getText());
             prefix = "', '";
         }
         sb.append("'], :time_period=>:millisecond do\n");
         sb.append("      from{\n");
 //        sb.append("        from{\n");
         sb.append("        ").append(TRAVERSE_CHILD_1);
         sb.append("        ").append("where {self[:type]=='gis' and self[:name]=='").append(cDrive.getText()).append("'}\n");
         sb.append("        }\n");
         sb.append("        ").append(TRAVERSE_NEXT_ALL);
         sb.append("        stop{property? 'timestamp' and self[:timestamp]>").append(end_time).append("}\n");
 //        sb.append("      }\n");
 //        sb.append("      ").append(TRAVERSE_CHILD_1);
         sb.append("      ").append("where {(property? 'timestamp' and self[:timestamp]<=").append(end_time)
         .append(" and self[:timestamp]>=").append(start_time);
         prefix = ") and (property? '";
         for(int i=0; i<currentPropertyCount; i++){
             sb.append(prefix).append(cProperties.get(i).getText());
             prefix = "' or property? '";
         }
         sb.append("')}\n");
         sb.append("    end\n");
         sb.append("  end\n");//chart end
         
         //table
         sb.append("  table 'Drive table' do\n");
         prefix = "    select 'drive table data', :properties=>['id','type','time','timestamp', 'event_type', '";
         for(int i=0; i<currentPropertyCount; i++){
             sb.append(prefix).append(cProperties.get(i).getText());
             prefix = "', '";
         }
         sb.append("'] do\n");
         sb.append("      from{\n");
 //        sb.append("        from{\n");
         sb.append("        ").append(TRAVERSE_CHILD_1);
         sb.append("        ").append("where {self[:type]=='gis' and self[:name]=='").append(cDrive.getText()).append("'}\n");
         sb.append("      }\n");
         sb.append("      ").append(TRAVERSE_NEXT_ALL);
         sb.append("      stop{property? 'timestamp' and self[:timestamp]>").append(selectedTime+delta_msec).append("}\n");
 //        sb.append("      }\n");
 //        sb.append("      ").append(TRAVERSE_CHILD_1);
         sb.append("    ").append("where {(property? 'timestamp' and self[:timestamp]<=").append(selectedTime+delta_msec)
         .append(" and self[:timestamp]>=").append(selectedTime-delta_msec);
         prefix = ") and (property? 'event_type' or property? '";
         for(int i=0; i<currentPropertyCount; i++){
             sb.append(prefix).append(cProperties.get(i).getText());
             prefix = "' or property? '";
         }
         sb.append("')}\n");
         sb.append("    end\n");
         sb.append("  end\n");
         sb.append("end\n");
         
         
 //        StringBuffer sb1 = new StringBuffer("report 'Drive ").append(cDrive.getText()).append("' do\n  author '").append(
 //                System.getProperty("user.name")).append("'\n  date '")
 //                .append(new SimpleDateFormat("yyyy-MM-dd").format(new Date())).append("'\n  chart '").append(cDrive.getText()).append("' do\n    self.drive='")
 //                .append(cDrive.getText()).append("'\n    self.event='")
 //                .append(cEvent.getText()).append("'\n    self.property1='")
 //                .append(cProperty1.getText()).append("'\n    self.property2='")
 //                .append(cProperty2.getText()).append("'\n    self.start_time='")
 //                .append(dateStart.getHours()).append(":").append(dateStart.getMinutes()).append(":").append(dateStart.getSeconds())
 //                .append("'\n    self.length='").append(sLength.getSelection()).append("'\n  end\nend");
         
         IProject project = ResourcesPlugin.getWorkspace().getRoot().getProjects()[0];//TODO correct
         IFile file;
         int i = 0;
         while ((file = project.getFile(new Path(("report" + i) + ".r"))).exists()) {
             i++;
         }
         System.out.println("Report script:\n" + sb.toString());
         InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
         try {
             file.create(is, true, null);
             is.close();
         } catch (CoreException e) {
             // TODO Handle CoreException
             throw (RuntimeException)new RuntimeException().initCause(e);
         } catch (IOException e) {
             // TODO Handle IOException
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
         try {
             getViewSite().getPage().openEditor(new FileEditorInput(file), "org.amanzi.splash.editors.ReportEditor");
         } catch (PartInitException e) {
             // TODO Handle PartInitException
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
     }
 
     /**
      *change slider position
      */
     protected void changeSlider() {
         chartFrame.dropAnchor();
         int i = slider.getSelection();
         XYPlot xyplot = (XYPlot)chart.getPlot();
         ValueAxis valueaxis = xyplot.getDomainAxis();
         Range range = valueaxis.getRange();
         Double d = beginGisTime+ (i / (double)(slider.getMaximum()-slider.getMinimum())) * (endGisTime-beginGisTime);
         selectedTime=d.longValue();
         xyplot.setDomainCrosshairValue(selectedTime.doubleValue());       
         double l = xyplot.getDomainCrosshairValue()-d;
         Long beginTime = getBeginTime();
         int timeWindowLen = getLength();
         Long endTime=beginTime + timeWindowLen;
         if (selectedTime<beginTime||selectedTime>endTime){
 //            selectedTime=Math.min(endGisTime, selectedTime);
 //            selectedTime=Math.max(beginGisTime, selectedTime);
             Long windowStartTime=selectedTime<beginTime?Math.max(beginGisTime, beginTime-timeWindowLen):Math.min(endGisTime, beginTime+timeWindowLen);
             if (selectedTime<windowStartTime||selectedTime>windowStartTime+timeWindowLen){
                 windowStartTime= selectedTime;
             }
            setBeginTime(windowStartTime);
            updateChart();
         }
        chart.fireChartChanged();
     }
 
     /**
      *begin time+=length
      */
     protected void rightHalf() {
         Long time = getBeginTime();
         int length = getLength();
         time += length / 2;
         if (time > endGisTime) {
             nextGis();
         } else {
             setBeginTime(time);
             updateChart();
         }
     }
 
     /**
      * Sets begin time
      * 
      * @param time - time
      */
     @SuppressWarnings("deprecation")
     private void setBeginTime(Long time) {
         dateStartTimestamp = time;
         Date date = new Date(time);
         dateStart.setHours(date.getHours());
         dateStart.setMinutes(date.getMinutes());
         dateStart.setSeconds(date.getSeconds());
     }
 
     /**
      * go to right
      */
     private void right() {
         long time = Math.max(beginGisTime, endGisTime - getLength());
         if (getBeginTime() >= time) {
             nextGis();
         } else {
             setBeginTime(time);
             updateChart();
         }
 
     }
 
     /**
      * begin time-=length
      */
     private void leftHalf() {
         Long time = getBeginTime();
         int length = getLength();
         time -= length / 2;
         if (time < beginGisTime) {
             previosGis();
         } else {
             setBeginTime(time);
             updateChart();
         }
     }
 
     /**
      * go to left
      */
     private void left() {
         long time = beginGisTime;
         if (getBeginTime() <= time) {
             previosGis();
         } else {
             setBeginTime(time);
             updateChart();
         }
 
     }
 
     /**
      *go to previos gis node
      */
     private void previosGis() {
         // int index = currentIndex;
         if (currentIndex <= 0) {
             return;
         }
         currentIndex--;
         cDrive.select(currentIndex);
         changeDrive();
         // if (index < 0) {
         // index = size - 1;
         // }
         // currentIndex = index;
         // Node root = dataset.get(currentIndex);
         // Node mp = getFirstMpNode(root);
         // Long time = NeoUtils.getNodeTime(mp);
         // beginGisTime = time;
         // mp = getLastMpNode(mp);
         // endGisTime = NeoUtils.getNodeTime(mp);
         // setBeginTime(time);
         // updateChart();
     }
 
     /**
      * get length from spin
      * 
      * @return length (milliseconds)
      */
     private int getLength() {
         return sLength.getSelection() * 60 * 1000;
     }
 
     /**
      *go to next gis node
      */
     private void nextGis() {
         int size = cDrive.getItemCount();
         if (currentIndex < 0 || currentIndex + 1 >= size) {
             return;
         }
         currentIndex++;
         cDrive.select(currentIndex);
         changeDrive();
     }
 
     /**
      *change drive
      */
     protected void changeDate() {
         if (!isStartDateChanged()) {
             return;
         }
         setTimeFromField();
         Node gis = getGisDriveNode();
 
         if (gis == null) {
             return;
         }
         updateChart();
         oldStartTime = getBeginTime();
     }
 
     /**
      * Sets time from datetime field
      */
     private void setTimeFromField() {
         GregorianCalendar cl = new GregorianCalendar();
         cl.setTimeInMillis(dateStartTimestamp);
         cl.set(Calendar.HOUR_OF_DAY, dateStart.getHours());
         cl.set(Calendar.MINUTE, dateStart.getMinutes());
         cl.set(Calendar.SECOND, dateStart.getSeconds());
         dateStartTimestamp = cl.getTimeInMillis();
 
     }
 
     /**
      *Check changing start date
      * 
      * @return true if start date was changed
      */
     private boolean isStartDateChanged() {
         return oldStartTime == null || !getBeginTime().equals(oldStartTime);
     }
 
     /**
      *change logarithmicSelection
      */
     protected void logarithmicSelection() {
         XYPlot plot = (XYPlot)chart.getPlot();
         if (bLogarithmic.getSelection()) {
             for(int i=0; i<MAX_PROP_COUNT; i++){
                 LogarithmicAxis axisLog = axisLogs.get(i);
                 plot.setRangeAxis(MAX_PROP_COUNT-i,axisLog);
                 axisLog.autoAdjustRange();
             }
         } else {
             for(int i=0; i<MAX_PROP_COUNT; i++){
                 ValueAxis axisNum = axisNumerics.get(i);
                 plot.setRangeAxis(MAX_PROP_COUNT-i,axisNum);
             }
         }
         chart.fireChartChanged();
     }
 
     /**
      *changed property
      */
     protected void updateProperty() {
         Long beginTime = getBeginTime();
         String propertyName;
         for(int i=0; i<currentPropertyCount;i++){
             propertyName = cProperties.get(i).getText();
             TimeDataset xydataset = xydatasets.get(i);
             if (!propertyName.equals(xydataset.getPropertyName())) {
                 xydataset.updateDataset(propertyName, beginTime, sLength.getSelection(), propertyName);
             }
         }
         propertyName = cEvent.getText();
         if (!propertyName.equals(eventDataset.getPropertyName())) {
             eventDataset.propertyName = propertyName;
             eventDataset.update();
             // eventDataset.updateDataset(propertyName, dataset.get(currentIndex), beginTime,
             // sLength.getSelection(), propertyName);
         }
         // TODO should we update map if only event was changed?
         fireEventUpdateChart();
 
     }
 
     /**
      * get begin time
      * 
      * @return Long
      */
     
     @SuppressWarnings("deprecation")
     private Long getBeginTime() {
        if(dateStartTimestamp == null){
            return null;
        }
         Date date = new Date(dateStartTimestamp);
         date.setHours(dateStart.getHours());
         date.setMinutes(dateStart.getMinutes());
         date.setSeconds(dateStart.getSeconds());
         return date.getTime();
     }
 
     /**
      *change drive dataset
      */
     protected void changeDrive() {
         if (cDrive.getSelectionIndex() < 0) {
             setsVisible(false);
         } else {
 
             formPropertyLists();
             updateChart();
         }
     }
 
     /**
      *update chart
      */
     private void updateChart() {
         if(cDrive.getText().isEmpty()){
             return;
         }
         Node gis = getGisDriveNode();
         String event = cEvent.getText();
         if (gis == null || event.isEmpty() || hasEmptyProperties()) {
             setsVisible(false);
         }
         chart.getTitle().setVisible(false);
         // chart.setTitle(CHART_TITLE + " " + NeoUtils.getSimpleNodeName(root, ""));
         Integer length = sLength.getSelection();
         Long time = getBeginTime();
         Date date = new Date(time);
         domainAxis.setMinimumDate(date);
         domainAxis.setMaximumDate(new Date(time + length * 1000 * 60));
 
         if(xydatasets.size()<currentPropertyCount){
             addDatasets();
         }
         
         for(int i=0; i<currentPropertyCount; i++){
             TimeDataset xydataset = xydatasets.get(i);
             String property = cProperties.get(i).getText();
             xydataset.updateDataset(property, time, length, property);
         }
         eventDataset.updateDataset(cEvent.getText(), time, length, cEvent.getText());
         setsVisible(true);
         fireEventUpdateChart();
     }
     
     private void addDatasets(){
         int start = xydatasets.size();
         XYPlot  xyplot = (XYPlot)chart.getPlot();
         for(int i=start; i<currentPropertyCount;i++){
             TimeDataset xydataset = new TimeDataset();
             StandardXYItemRenderer standardxyitemrenderer = new StandardXYItemRenderer();
             standardxyitemrenderer.setBaseShapesFilled(true);
             int number = MAX_PROP_COUNT-i;
             xyplot.setDataset(number,xydataset);
             xyplot.setRenderer(number, standardxyitemrenderer);
             NumberAxis numberaxis = new NumberAxis(getPropertyYAxisName(i));
             numberaxis.setAutoRangeIncludesZero(false);
             xyplot.setRangeAxis(number, numberaxis);
             xyplot.setRangeAxisLocation(number, AxisLocation.BOTTOM_OR_LEFT);
             xyplot.mapDatasetToRangeAxis(number, number);
             xydatasets.add(xydataset);
         }
         
         for(int i=start; i<currentPropertyCount; i++){
             ValueAxis axisNumeric = xyplot.getRangeAxis(MAX_PROP_COUNT-i);
             LogarithmicAxis axisLog = new LogarithmicAxis(axisNumeric.getLabel());
             axisLog.setAllowNegativesFlag(true);
             axisLog.setAutoRange(true);
             
             Color color = getColorForProperty(i);
             axisLog.setTickLabelPaint(color);
             axisLog.setLabelPaint(color);
             axisNumeric.setTickLabelPaint(color);
             axisNumeric.setLabelPaint(color);
             
             axisNumerics.add(axisNumeric);
             axisLogs.add(axisLog);
             xyplot.getRenderer(MAX_PROP_COUNT-i).setSeriesPaint(0, color);
         }
     }
     
     private boolean hasEmptyProperties(){
         for(int i = 0;i<currentPropertyCount;i++){
             if(cProperties.get(i).getText().isEmpty()){
                 return true;
             }
         }
         return false;
     }
 
     /**
      *fires event for chart changed
      */
     private void fireEventUpdateChart() {
         IMap activeMap = ApplicationGIS.getActiveMap();
         Node gis = getGisDriveNode();
         if (activeMap != ApplicationGIS.NO_MAP) {
             try {
                 for (ILayer layer : activeMap.getMapLayers()) {
                     IGeoResource resourse = layer.findGeoResource(GeoNeo.class);
                     if (resourse != null) {
                         GeoNeo geo = resourse.resolve(GeoNeo.class, null);
                         if (gis != null && geo.getMainGisNode().equals(gis)) {
                             setProperty(geo);
                             layer.refresh(null);
                         } else {
                             dropProperty(geo);
                         }
 
                     }
                 }
             } catch (IOException e) {
                 throw (RuntimeException)new RuntimeException().initCause(e);
             }
         }
         chart.fireChartChanged();
     }
 
     /**
      *remove property from geo
      * 
      * @param geo
      */
     private void dropProperty(GeoNeo geo) {
         if (geo.getGisType() != GisTypes.DRIVE) {
             return;
         }
         Object map = geo.getProperties(GeoNeo.DRIVE_INQUIRER);
         if (map != null) {
             geo.setProperty(GeoNeo.DRIVE_INQUIRER, null);
         }
     }
 
     /**
      * Sets property in geo for necessary
      * 
      * @param geo
      */
     private void setProperty(GeoNeo geo) {
         Node gisNode = getGisDriveNode();
         if (!geo.getMainGisNode().equals(gisNode)) {
             return;
         }
         HashMap<String, Object> map = new HashMap<String, Object>();
         Long beginTime = getBeginTime();
         map.put(GeoConstant.Drive.BEGIN_TIME, beginTime);
         map.put(GeoConstant.Drive.END_TIME, beginTime + getLength());
         double crosshair = ((XYPlot)chart.getPlot()).getDomainCrosshairValue();
         Long nodeId = getSelectedProperty1(crosshair);
         Long id = null;
         // gets id of parent mp node
         if (nodeId != null) {
             Node node = NeoUtils.getNodeById(nodeId);
             if (node != null) {
                 Relationship singleRelationship = node.getSingleRelationship(NetworkRelationshipTypes.CHILD, Direction.INCOMING);
                 if (singleRelationship != null) {
                     node = singleRelationship.getOtherNode(node);
                     id = node.getId();
                 }
             }
         }
         map.put(GeoConstant.Drive.SELECT_PROPERTY1, id);
         nodeId = getSelectedProperty2(crosshair);
         id = null;
         // gets id of parent mp node
         if (nodeId != null) {
             Node node = NeoUtils.getNodeById(nodeId);
             if (node != null) {
                 Relationship singleRelationship = node.getSingleRelationship(NetworkRelationshipTypes.CHILD, Direction.INCOMING);
                 if (singleRelationship != null) {
                     node = singleRelationship.getOtherNode(node);
                     id = node.getId();
                 }
             }
         }
         map.put(GeoConstant.Drive.SELECT_PROPERTY2, id);
         map.put(GeoConstant.SELECTED_EVENT, cEvent.getText());
         map.put(GeoConstant.EVENT_LIST, Collections.unmodifiableList(eventList));
         map.put(GeoConstant.Drive.SELECT_PALETTE, cPalette.getText());
 
         geo.setProperty(GeoNeo.DRIVE_INQUIRER, map);
     }
 
     /**
      * get id of selected ms node for property 2
      * 
      * @param crosshair - crosshair value
      * @return node id or null
      */
     private Long getSelectedProperty2(double crosshair) {
         Integer result = getCrosshairIndex(xydatasets.get(1), crosshair);
         if (result != null) {
             return xydatasets.get(0).collection.getSeries(0).getDataItem(result).getValue().longValue();
         } else {
             return null;
         }
     }
 
     /**
      * get id of selected ms node for property 1
      * 
      * @param crosshair - crosshair value
      * @return node id or null
      */
     private Long getSelectedProperty1(double crosshair) {
         Integer result = getCrosshairIndex(xydatasets.get(0), crosshair);
         if (result != null) {
             return xydatasets.get(1).collection.getSeries(0).getDataItem(result).getValue().longValue();
         } else {
             return null;
         }
     }
 
     /**
      * Gets index of crosshair data item
      * 
      * @param xydataset
      * @param crosshair
      * @return index or null
      */
     private Integer getCrosshairIndex(TimeDataset dataset, Number crosshair) {
         return getCrosshairIndex(dataset.collection, crosshair);
     }
 
     private Integer getCrosshairIndex(TimeSeriesCollection collection, Number crosshair) {
         if (crosshair == null) {
             return null;
         }
         int[] item = collection.getSurroundingItems(0, crosshair.longValue());
         Integer result = null;
         if (item[0] >= 0) {
             result = item[0];
         }
         // else if (item[1] >= 0) {
         // result = item[1];
         // }
         return result;
     }
     /**
      *forms all property depends of gis
      */
     private void formPropertyLists() {
         Transaction tx = NeoUtils.beginTransaction();
         try {
             Node gis = getGisDriveNode();
             currentIndex = cDrive.getSelectionIndex();
             PropertyHeader propertyHeader = new PropertyHeader(gis);
             Collection<String> events = propertyHeader.getEvents();
             eventList = new ArrayList<String>();
             eventList.add(ALL_EVENTS);
             if (events != null) {
                 eventList.addAll(events);
             }
             cEvent.setItems(eventList.toArray(new String[0]));
             cEvent.select(0);
             String[] array = propertyHeader.getNumericFields();
             for(Combo property : cProperties){
                 property.setItems(array);
                 if(array.length > 0){
                     property.select(0);
                 }
             }
             initializeIndex(cDrive.getText());
             Pair<Long, Long> minMax = NeoUtils.getMinMaxTimeOfDataset(gis, null);
             beginGisTime = minMax.getLeft();
             endGisTime = minMax.getRight();
             selectedTime=beginGisTime;
             slider.setMaximum((int)((endGisTime-beginGisTime)/SLIDER_STEP));
             slider.setSelection(0);
             selectedTime=beginGisTime;
             setBeginTime(beginGisTime);
             chart.getXYPlot().setDomainCrosshairValue(selectedTime);
 
         } finally {
             tx.finish();
         }
 
     }
 
     /**
      * get gis node
      * 
      * @return node
      */
     private Node getGisDriveNode() {
         return gisDriveNodes == null ? null : gisDriveNodes.get(cDrive.getText());
     }
 
     /**
      * get Drive list
      * 
      * @return String[]
      */
     private String[] getDriveItems() {
         NeoService service = NeoServiceProvider.getProvider().getService();
         Node refNode = service.getReferenceNode();
         gisDriveNodes = new LinkedHashMap<String, Node>();
 
         Transaction tx = service.beginTx();
         try {
             for (Relationship relationship : refNode.getRelationships(Direction.OUTGOING)) {
                 Node node = relationship.getEndNode();
                 Object type = node.getProperty(INeoConstants.PROPERTY_GIS_TYPE_NAME, "").toString();
                 if (NeoUtils.isGisNode(node) && type.equals(GisTypes.DRIVE.getHeader())||NodeTypes.OSS.checkNode(node)) {
                     String id = NeoUtils.getSimpleNodeName(node, null);
                     gisDriveNodes.put(id, node);
                 }
             }
 
             return gisDriveNodes.keySet().toArray(new String[] {});
         } finally {
             tx.finish();
         }
     }
 
     @Override
     public void setFocus() {
     }
 
     /**
      * Creates the Chart based on a dataset
      */
     private JFreeChart createChart() {
 
         XYBarRenderer xyarearenderer = new EventRenderer();
         eventDataset = new EventDataset();
         NumberAxis rangeAxis = new NumberAxis("Events");
         rangeAxis.setVisible(false);
         domainAxis = new DateAxis("Time");
         XYPlot xyplot = new XYPlot(eventDataset, domainAxis, rangeAxis, xyarearenderer);
         
         xydatasets = new ArrayList<TimeDataset>(MAX_PROP_COUNT);
         for(int i=0; i<currentPropertyCount;i++){
             TimeDataset xydataset = new TimeDataset();
             StandardXYItemRenderer standardxyitemrenderer = new StandardXYItemRenderer();
             standardxyitemrenderer.setBaseShapesFilled(true);
             int number = MAX_PROP_COUNT-i;
             xyplot.setDataset(number,xydataset);
             xyplot.setRenderer(number, standardxyitemrenderer);
             NumberAxis numberaxis = new NumberAxis(getPropertyYAxisName(i));
             numberaxis.setAutoRangeIncludesZero(false);
             xyplot.setRangeAxis(number, numberaxis);
             xyplot.setRangeAxisLocation(number, AxisLocation.BOTTOM_OR_LEFT);
             xyplot.mapDatasetToRangeAxis(number, number);
             xydatasets.add(xydataset);
         }
 
         xyplot.setDomainCrosshairVisible(true);
         xyplot.setDomainCrosshairLockedOnData(false);
         xyplot.setRangeCrosshairVisible(false);
         
  
         
         JFreeChart jfreechart = new JFreeChart(CHART_TITLE, JFreeChart.DEFAULT_TITLE_FONT, xyplot, true);
 
         ChartUtilities.applyCurrentTheme(jfreechart);
         jfreechart.getTitle().setVisible(false);
         
         axisNumerics = new ArrayList<ValueAxis>(MAX_PROP_COUNT);
         axisLogs = new ArrayList<LogarithmicAxis>(MAX_PROP_COUNT);
         xyplot.getRenderer(0).setSeriesPaint(0, new Color(0, 0, 0, 0));
         for(int i=0; i<currentPropertyCount; i++){
             ValueAxis axisNumeric = xyplot.getRangeAxis(MAX_PROP_COUNT-i);
             LogarithmicAxis axisLog = new LogarithmicAxis(axisNumeric.getLabel());
             axisLog.setAllowNegativesFlag(true);
             axisLog.setAutoRange(true);
             
             Color color = getColorForProperty(i);
             axisLog.setTickLabelPaint(color);
             axisLog.setLabelPaint(color);
             axisNumeric.setTickLabelPaint(color);
             axisNumeric.setLabelPaint(color);
             
             axisNumerics.add(axisNumeric);
             axisLogs.add(axisLog);
             xyplot.getRenderer(MAX_PROP_COUNT-i).setSeriesPaint(0, color);
         }
         
         return jfreechart;
 
     }
     
     private Color getColorForProperty(int propNum){
         return PROPERTY_COLORS[propNum];        
     }
 
     /**
      * @return
      */
     private String getPropertyYAxisName(int propNum) {
         return "";
     }
 
     /**
      * <p>
      * Dataset for event
      * </p>
      * 
      * @author Cinkel_A
      * @since 1.0.0
      */
     private class EventDataset extends AbstractIntervalXYDataset {
         /** long serialVersionUID field */
         private static final long serialVersionUID = 1L;
         
         private Long beginTime;
         private Long length;
         private TimeSeries series;
         private TimeSeriesCollection collection;
         private String propertyName;
 
         /**
          * @return Returns the propertyName.
          */
         public String getPropertyName() {
             return propertyName;
         }
 
         /**
          * update dataset with new data
          * 
          * @param name - dataset name
          * @param root - root node
          * @param beginTime - begin time
          * @param length - length
          * @param propertyName - property name
          * @param event - event value
          */
         public void updateDataset(String name, Long beginTime, int length, String propertyName) {
             this.beginTime = beginTime;
             this.length = (long)length * 1000 * 60;
             this.propertyName = propertyName;
             collection = new TimeSeriesCollection();
             createSeries(name, propertyName);
             collection.addSeries(series);
             this.fireDatasetChanged();
         }
 
         /**
          * update dataset
          */
         public void update() {
             if (collection.getSeriesCount() > 0) {
                 collection.getSeries(0).setKey(propertyName);
             }
             this.fireDatasetChanged();
         }
 
         /**
          * constructor
          */
         public EventDataset() {
             super();
             beginTime = null;
             length = null;
             series = null;
             collection = new TimeSeriesCollection();
             propertyName = null;
         }
 
         /**
          * Create time series
          * 
          * @param name name of serie
          * @param propertyName property name
          */
         protected void createSeries(String name, String propertyName) {
             Transaction tx = NeoUtils.beginTransaction();
             try {
                 series = new TimeSeries(name);
                 Iterator<Node> nodeIterator = getNodeIterator(beginTime, length).iterator();
                 while (nodeIterator.hasNext()) {
                     Node node = nodeIterator.next();
                     Long time = NeoUtils.getNodeTime(node);
                     node = getSubNode(node, propertyName);
                     if (node == null) {
                         continue;
                     }
 
                     series.addOrUpdate(new Millisecond(new Date(time)), node.getId());
                 }
             } finally {
                 tx.finish();
             }
         }
 
         public Node getSubNode(Node node, final String propertyName) {
             Iterator<Node> iterator = node.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     Node node = currentPos.currentNode();
                     boolean result = node.hasProperty(EVENT);
                     return result;
                 }
             }, NetworkRelationshipTypes.CHILD, Direction.OUTGOING).iterator();
             return iterator.hasNext() ? iterator.next() : null;
         }
 
         @Override
         public int getSeriesCount() {
             return collection.getSeriesCount();
         }
 
         @Override
         @SuppressWarnings("unchecked")
         public Comparable getSeriesKey(int i) {
             return collection.getSeriesKey(i);
         }
 
         @Override
         public Number getEndX(int i, int j) {
             return collection.getEndX(i, j);
         }
 
         @Override
         public Number getEndY(int i, int j) {
             return 1;
         }
 
         @Override
         public Number getStartX(int i, int j) {
             return collection.getStartX(i, j);
         }
 
         @Override
         public Number getStartY(int i, int j) {
             return 1;
         }
 
         @Override
         public int getItemCount(int i) {
             return collection.getItemCount(i);
         }
 
         @Override
         public Number getX(int i, int j) {
             return collection.getX(i, j);
         }
 
         @Override
         public Number getY(int i, int j) {
             return 1;
         }
     }
 
     /**
      * <p>
      * Event renderer
      * </p>
      * 
      * @author Cinkel_A
      * @since 1.0.0
      */
     private class EventRenderer extends XYBarRenderer {
 
         /** long serialVersionUID field */
         private static final long serialVersionUID = 1L;
 
         public EventRenderer() {
             super();
         }
 
         @Override
         public Shape getItemShape(int row, int column) {
             return super.getItemShape(row, column);
         }
 
         @Override
         public Paint getItemFillPaint(int row, int column) {
             return super.getItemFillPaint(row, column);
         }
 
         @Override
         public Paint getItemPaint(int row, int column) {
             TimeSeriesDataItem item = eventDataset.series.getDataItem(column);
             Node node = NeoServiceProvider.getProvider().getService().getNodeById(item.getValue().longValue());
             Color color = getEventColor(node);
             return color;
         }
 
     }
 
     /**
      * <p>
      * Time dataset Now it simple wrapper of TimeSeriesCollection But if cache is not possible need
      * be refactored for use database access
      * </p>
      * 
      * @author Cinkel_A
      * @since 1.0.0
      */
     private class TimeDataset extends AbstractXYDataset implements CategoryDataset {
 
         /** long serialVersionUID field */
         private static final long serialVersionUID = 1L;
         
         private Long beginTime;
         private Long length;
         private TimeSeries series;
         private TimeSeriesCollection collection;
         private String propertyName;
 
         /**
          * @return Returns the propertyName.
          */
         public String getPropertyName() {
             return propertyName;
         }
 
         /**
          * update dataset with new data
          * 
          * @param name - dataset name
          * @param root - root node
          * @param beginTime - begin time
          * @param length - length
          * @param propertyName - property name
          * @param event - event value
          */
         public void updateDataset(String name, Long beginTime, int length, String propertyName) {
             this.beginTime = beginTime;
             this.length = (long)length * 1000 * 60;
             this.propertyName = propertyName;
             collection = new TimeSeriesCollection();
             createSeries(name, propertyName);
             collection.addSeries(series);
             this.fireDatasetChanged();
         }
 
         /**
          * constructor
          */
         public TimeDataset() {
             super();
             beginTime = null;
             length = null;
             series = null;
             collection = new TimeSeriesCollection();
             propertyName = null;
         }
 
         /**
          * Create time series
          * 
          * @param name name of serie
          * @param propertyName property name
          */
         protected void createSeries(String name, String propertyName) {
             Transaction tx = NeoUtils.beginTransaction();
             try {
                 series = new TimeSeries(name);
                 Iterator<Node> nodeIterator = getNodeIterator(beginTime, length).iterator();
                 while (nodeIterator.hasNext()) {
                     Node node = nodeIterator.next();
                     Long time = NeoUtils.getNodeTime(node);
                     node = getSubNode(node, propertyName);
                     if (node == null) {
                         continue;
                     }
 
                     series.addOrUpdate(new Millisecond(new Date(time)), node.getId());
                 }
             } finally {
                 tx.finish();
             }
         }
 
         /**
          * get necessary subnodes of mp node
          * 
          * @param node - node
          * @param event - event value
          * @param propertyName - property name
          * @return subnode
          */
         public Node getSubNode(Node node, final String propertyName) {
             Iterator<Node> iterator = node.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     Node node = currentPos.currentNode();
                     return node.hasProperty(propertyName);
                 }
             }, NetworkRelationshipTypes.CHILD, Direction.OUTGOING).iterator();
             return iterator.hasNext() ? iterator.next() : null;
         }
 
 
         @Override
         public int getSeriesCount() {
             return collection.getSeriesCount();
         }
 
         @Override
         @SuppressWarnings("unchecked")
         public Comparable getSeriesKey(int i) {
             return collection.getSeriesKey(i);
         }
 
         @Override
         public int getItemCount(int i) {
             return collection.getItemCount(i);
         }
 
         @Override
         public Number getX(int i, int j) {
             return collection.getX(i, j);
         }
 
         @Override
         public Number getY(int i, int j) {
             return (Number)NeoServiceProvider.getProvider().getService().getNodeById(collection.getY(i, j).longValue())
                     .getProperty(propertyName);
         }
 
         @Override
         public int getColumnIndex(Comparable comparable) {
             return 0;
         }
 
         @Override
         public Comparable getColumnKey(int i) {
             return null;
         }
 
         @Override
         public List getColumnKeys() {
             return null;
         }
 
         @Override
         public int getRowIndex(Comparable comparable) {
             return 0;
         }
 
         @Override
         public Comparable getRowKey(int i) {
             return null;
         }
 
         @Override
         public List getRowKeys() {
             return null;
         }
 
         @Override
         public Number getValue(Comparable comparable, Comparable comparable1) {
             return null;
         }
 
         @Override
         public int getColumnCount() {
             return 0;
         }
 
         @Override
         public int getRowCount() {
             return 0;
         }
 
         @Override
         public Number getValue(int i, int j) {
             return null;
         }
 
     }
 
     /**
      * get event color
      * 
      * @param node node
      * @return color
      */
     public Color getEventColor(Node node) {
         String event = node.getProperty(EVENT, ALL_EVENTS).toString();
         int alpha = 0;
         if (ALL_EVENTS.equals(eventDataset.propertyName) || event.equals(eventDataset.propertyName)) {
             alpha = 255;
         }
         int i = eventList.indexOf(event);
         if (i < 0) {
             i = 0;
         }
         BrewerPalette palette = PlatformGIS.getColorBrewer().getPalette(cPalette.getText());
         Color[] colors = palette.getColors(palette.getMaxColors());
         int index = i % colors.length;
         Color color = colors[index];
         return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
     }
 
 
 
     private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
 
         private final ArrayList<TableColumn> columns = new ArrayList<TableColumn>();
         /** int DEF_SIZE field */
         protected static final int DEF_SIZE = 150;
         @Override
         public Image getColumnImage(Object element, int columnIndex) {
             NodeWrapper wr = provider.nodeWrapper;
             if (columnIndex == 3 && wr.nEvents[(Integer)element] != null) {
                 Color eventColor = getEventColor(wr.nEvents[(Integer)element]);
                 return Glyph.palette(new Color[] {eventColor}).createImage();
             }
             return getImage(element);
         }
 
         public void refreshTable(){
             Table tabl = table.getTable();
             TableColumn col = getColumn(0);
             col.setText("Time");
             col.setWidth(DEF_SIZE);
             col.setResizable(true);
 
             for(int i=1; i<=currentPropertyCount; i++){
                 col = getColumn(i);
                 col.setText(Messages.DriveInquirerView_label_property+(i));
                 col.setWidth(DEF_SIZE);
                 col.setResizable(true);
             }
             
             col = getColumn(currentPropertyCount+1);
             col.setText("events");
             columns.add(col);
             col.setWidth(DEF_SIZE);
             col.setResizable(true);
             for(int i=currentPropertyCount+1; i<MAX_PROP_COUNT+1; i++){
                 col = getColumn(i);
                 col.setText("");
                 col.setWidth(DEF_SIZE);
                 col.setResizable(true);
             }
             tabl.setHeaderVisible(true);
             tabl.setLinesVisible(true);
             table.setLabelProvider(this);
             table.refresh();
         }
         
         private TableColumn getColumn(int colNum){
             if(colNum<columns.size()){
                 return columns.get(colNum);
             }
             TableColumn column = new TableViewerColumn(table, SWT.LEFT).getColumn();
             columns.add(column);
             return column;
         }
         
         /**
          *create column table
          */
         public void createTableColumn() {
             Table tabl = table.getTable();
             TableViewerColumn column;
             TableColumn col;
             if (columns.isEmpty()) {
                 column = new TableViewerColumn(table, SWT.LEFT);
                 col = column.getColumn();
                 col.setText("Time");
                 columns.add(col);
                 col.setWidth(DEF_SIZE);
                 col.setResizable(true);
 
                 for(int i=0; i<MAX_PROP_COUNT; i++){
                     column = new TableViewerColumn(table, SWT.LEFT);
                     col = column.getColumn();
                     col.setText(Messages.DriveInquirerView_label_property+(i+1));
                     columns.add(col);
                     col.setWidth(DEF_SIZE);
                     col.setResizable(true);
                 }
                 
                 column = new TableViewerColumn(table, SWT.LEFT);
                 col = column.getColumn();
                 col.setText("events");
                 columns.add(col);
                 col.setWidth(DEF_SIZE);
                 col.setResizable(true);
 
             }
             tabl.setHeaderVisible(true);
             tabl.setLinesVisible(true);
             table.setLabelProvider(this);
             table.refresh();
         }
 
         @Override
         public String getColumnText(Object element, int columnIndex) {
             NodeWrapper wr = provider.nodeWrapper;
             int index = (Integer)element;
             SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
             if (columnIndex == 0) {
                 Long time = wr.time[index];
                 return time == null ? "" : df.format(new Date(time));
             } 
             if (columnIndex <= currentPropertyCount && wr.nProperties.get(columnIndex-1)[index] != null) {
                 return wr.nProperties.get(columnIndex-1)[index].getProperty(wr.propertyNames.get(columnIndex-1), "").toString();
             }
             if (columnIndex == currentPropertyCount+1) {
                 if (wr.nEvents[index] != null) {
                     return wr.nEvents[index].getProperty(EVENT, "").toString();
                 }
             }
             return "";
         }
 
     }
 
     /*
      * The content provider class is responsible for providing objects to the view. It can wrap
      * existing objects in adapters or simply return objects as-is. These objects may be sensitive
      * to the current input of the view, or ignore it and always show the same content (like Task
      * List, for example).
      */
 
     private class TableContentProvider implements IStructuredContentProvider {
 
         private final NodeWrapper nodeWrapper = new NodeWrapper();
 
         public TableContentProvider() {
         }
 
         @Override
         public Object[] getElements(Object inputElement) {
             return new Integer[] {0, 1, 2};
         }
 
         @Override
         public void dispose() {
         }
 
         @Override
         public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
             if (newInput == null) {
                 return;
             }
             labelProvider.refreshTable();
             Double crosshair = ((XYPlot)chart.getPlot()).getDomainCrosshairValue();
             nodeWrapper.propertyNames.clear();
             for(int i=0; i<currentPropertyCount; i++){
                 nodeWrapper.propertyNames.add(cProperties.get(i).getText());
                 changeName(labelProvider.columns.get(i+1), nodeWrapper.propertyNames.get(i));
             }
             nodeWrapper.eventName = cEvent.getText();
             changeName(labelProvider.columns.get(currentPropertyCount+1), nodeWrapper.eventName);
             for (int i = 0; i < 2; i++) {
                 nodeWrapper.nEvents[i] = null;
                 for(int j=0; j<currentPropertyCount;j++){
                     nodeWrapper.nProperties.get(j)[i] = null;
                 }
                 nodeWrapper.time[i] = null;
             }
             if (crosshair < 0.1) {
                 return;
             }
             nodeWrapper.time[1] = crosshair.longValue();
             nodeWrapper.time[0] = getPreviousTime(nodeWrapper.time[1]);
             nodeWrapper.time[2] = getNextTime(nodeWrapper.time[1]);
             for(int i=0; i<currentPropertyCount; i++){
                 fillProperty(crosshair, xydatasets.get(i).collection, nodeWrapper.nProperties.get(i), nodeWrapper.time);
             }
             fillProperty(crosshair, eventDataset.collection, nodeWrapper.nEvents, nodeWrapper.time);
 
 
 
         }
 
         /**
          * @param tableColumn
          * @param name
          */
         private void changeName(TableColumn tableColumn, String name) {
             if (!tableColumn.getText().equals(name)){
                 tableColumn.setText(name);
             }
         }
 
         /**
          * @param crosshair
          * @param dataset
          * @param nodes
          */
         private void fillProperty(double crosshair, TimeSeriesCollection dataset, Node[] nodes, Long[] time) {
             Integer index1 = getCrosshairIndex(dataset, time[1]);
             if (index1 != null) {
                 nodes[1] = NeoUtils.getNodeById(dataset.getSeries(0).getDataItem(index1).getValue()
                         .longValue());
                 if (index1 > 0) {
                     nodes[0] = NeoUtils.getNodeById(dataset.getSeries(0).getDataItem(index1 - 1)
                             .getValue().longValue());
                 }
                 if (index1 + 1 < dataset.getSeries(0).getItemCount()) {
                     nodes[2] = NeoUtils.getNodeById(dataset.getSeries(0).getDataItem(index1 + 1)
                             .getValue().longValue());
                 }
             }
         }
 
     }
 
     private class NodeWrapper {
         List<String> propertyNames = new ArrayList<String>(MAX_PROP_COUNT);
         String eventName;
         Long[] time = new Long[3];
         List<Node[]> nProperties = new ArrayList<Node[]>(MAX_PROP_COUNT);
         Node[] nEvents = new Node[3];
         
         /**
          * 
          */
         public NodeWrapper() {
             for(int i=0; i<MAX_PROP_COUNT; i++){
                 nProperties.add(new Node[3]);
             }
         }
     }
 
     /**
      * get iterable of necessary mp nodes
      * 
      * @param root - root
      * @param beginTime - begin time
      * @param length - end time
      * @return
      */
     private Iterable<Node> getNodeIterator(final Long beginTime, final Long length) {
         return timestampIndex.searchTraverser(new Long[] {beginTime}, new Long[] {beginTime + length + 1});
     }
 
     /**
      * @param time
      * @return
      */
     public Long getNextTime(Long time) {
         XYPlot xyplot = (XYPlot)chart.getPlot();
         ValueAxis valueaxis = xyplot.getDomainAxis();
         Range range = valueaxis.getRange();
 
         return time == null ? null : (long)Math.min(time + 1000, range.getUpperBound());
     }
 
     /**
      * @param time
      * @return
      */
     public Long getPreviousTime(Long time) {
         XYPlot xyplot = (XYPlot)chart.getPlot();
         ValueAxis valueaxis = xyplot.getDomainAxis();
         Range range = valueaxis.getRange();
 
         return time == null ? null : (long)Math.max(time - 1000, range.getLowerBound());
     }
 
     /**
      *update drive combobox
      */
     public void updateGisNode() {
         int oldInd = cDrive.getSelectionIndex();
         String item = oldInd >= 0 ? cDrive.getItem(oldInd) : null;
         String[] driveItems = getDriveItems();
         cDrive.setItems(driveItems);
         if (oldInd >= 0) {
             for (int i = 0; i < driveItems.length; i++) {
                 if (item.equals(driveItems[i])) {
                     cDrive.select(i);
                     break;
                 }
             }
         }
 
     }
 
     /**
      * Initialized Timestamp index for dataset
      * 
      * @param datasetName name of dataset
      */
     private void initializeIndex(String datasetName) {
         try {
             timestampIndex = NeoUtils.getTimeIndexProperty(datasetName);
             timestampIndex.initialize(NeoServiceProvider.getProvider().getService(), null);
         } catch (IOException e) {
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
     }
 
     @Override
     public void saveState(IMemento memento) {
         super.saveState(memento);
         String drive = cDrive.getText();
         String event = cEvent.getText();
         Long startTime = getBeginTime();
         String time = startTime == null ? null : startTime.toString();
         Integer timeLength = sLength.getSelection();
         Boolean logarithm = bLogarithmic.getSelection();
         String paletteName = cPalette.getText();
         memento.putString(MEM_DRIVE, drive);
         memento.putString(MEM_EVENT, event);
         memento.putString(MEM_START_TIME, time);
         memento.putInteger(MEM_TIME_LENGTH, timeLength);
         memento.putString(MEM_LOGARITHM, logarithm.toString());
         memento.putString(MEM_PALETTE, paletteName);
         memento.putInteger(MEM_ADD_PROPERTY_COUNT, currentPropertyCount);
         for(int i=0;i<currentPropertyCount;i++){
             memento.putString(MEM_ADD_PROPERTY+i, cProperties.get(i).getText());
         }           
     }
 
     @Override
     public void init(IViewSite site, IMemento memento) throws PartInitException {
         super.init(site, memento);
         if (memento == null) {
             return;
         }
         initDrive = memento.getString(MEM_DRIVE);
         initEvent = memento.getString(MEM_EVENT);
         initTime = memento.getString(MEM_START_TIME);
         initTimeLen = memento.getInteger(MEM_TIME_LENGTH);
         initLogarithm = memento.getString(MEM_LOGARITHM);
         initPalette = memento.getString(MEM_PALETTE);
         initPropCount = memento.getInteger(MEM_ADD_PROPERTY_COUNT);
         initProperties = new ArrayList<String>(initPropCount);
         for(int i=0; i<initPropCount; i++){
             initProperties.add(memento.getString(MEM_ADD_PROPERTY+i));
         }
     }
     /**
      * 
      * <p>
      * temporary class for avoid bug: if anchor is set - the crosshair do not change by slider changing 
      *  remove if more correctly way will be found
      * </p>
      * @author Cinkel_A
      * @since 1.0.0
      */
 public static class ChartCompositeImpl extends ChartComposite{
 
     
     public ChartCompositeImpl(Composite frame, int none, JFreeChart chart, boolean b) {
         super(frame,none,chart,b);
     }
     /**
      * drop anchor;
      */
     public void dropAnchor(){
         setAnchor(null);
     }
 }
 }
