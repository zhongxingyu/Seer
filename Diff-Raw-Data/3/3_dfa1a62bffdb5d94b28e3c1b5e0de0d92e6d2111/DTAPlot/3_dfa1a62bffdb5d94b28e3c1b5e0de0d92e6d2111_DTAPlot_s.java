 /**
  * Copyright 2011 Kevin J. Jones (http://www.kevinjjones.co.uk)
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package uk.co.kevinjjones;
 
 import info.monitorenter.gui.chart.IAxis;
 import info.monitorenter.gui.chart.IAxis.AxisTitle;
 import info.monitorenter.gui.chart.IRangePolicy;
 import info.monitorenter.gui.chart.ITrace2D;
 import info.monitorenter.gui.chart.ZoomableChart;
 import info.monitorenter.gui.chart.axis.AAxis;
 import info.monitorenter.gui.chart.axis.AxisLinear;
 import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
 import info.monitorenter.gui.chart.rangepolicies.RangePolicyForcedPoint;
 import info.monitorenter.util.Range;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.SortedSet;
 import javax.swing.*;
 import javax.swing.plaf.FontUIResource;
 import net.iharder.dnd.FileDrop;
 import uk.co.kevinjjones.model.BasicError;
 import uk.co.kevinjjones.model.ROStream;
 import uk.co.kevinjjones.model.WithError;
 import uk.co.kevinjjones.vehicle.AFRStream;
 import uk.co.kevinjjones.vehicle.SpeedStream;
 import uk.co.kevinjjones.vehicle.TempStream;
 
 public class DTAPlot {
     
     // Main backgroud colour
     private static Color BKGDCOLOUR=new Color(0x041731);
 
     // All the UI components
     private ZoomableChart _chart;
     private JScrollBar _chartHScroll;
     private JScrollBar _chartVScroll;
     private JList _messages;
     private DefaultListModel _messagesModel;
     private JComboBox _lapCombo;
     private JCheckBox _autoSplitCheck;
     private JCheckBox _speedCheck;
     private JCheckBox _tpsCheck;
     private JCheckBox _mapCheck;
     private JCheckBox _rpmCheck;
     private JCheckBox _afrCheck;
     private JCheckBox _turboCheck;
     private JCheckBox _waterTempCheck;
     private JCheckBox _oilTempCheck;
     private JCheckBox _airTempCheck;
     private JCheckBox _wheelSlipCheck;
     //private JCheckBox _timeSlipCheck;
     private JButton _traceBtn;
     private JButton _clearBtn;
     private JButton _resetZoomBtn;
     private JButton _optionsBtn;
     private boolean _ignoreEvents = false;
     
     // Entry, loads any files passed on command line
     public static void main(String[] args) {
         
         DTAPlot.initializeFontSize(125);
         DTAPlot p = new DTAPlot();
         p.run();
 
         // Load any command line files
         RunManager mgr = RunManager.getInstance();
         for (int i = 0; i < args.length; i++) {
             p.loadFile(new File(args[i]));
         }
     }
     
     private DTAPlot() {
         super();
     }
 
     /**
      * Chart color picker
      * @param index - What color # to pick
      */
     private static Color getColor(int index) {
         Color c = Color.BLACK;
         switch (index % 6) {
             case 0:
                 c = new Color(228, 26, 28);
                 break;
             case 1:
                 c = new Color(55, 126, 184);
                 break;
             case 2:
                 c = new Color(77, 175, 74);
                 break;
             case 3:
                 c = new Color(152, 78, 163);
                 break;
             case 4:
                 c = new Color(255, 127, 0);
                 break;
             case 5:
                 c = new Color(166, 86, 40);
                 break;
         }
         return c;
     }
 
     /**
      * Get next colour to use for a trace.
      * Picks the least used color in existing traces.
      */
     private Color getNextColor() {
 
         // Find least used color
         int[] count = new int[6];
         SortedSet<ITrace2D> traces = _chart.getTraces();
         for (ITrace2D trace : traces) {
             for (int i = 0; i < 6; i++) {
                 if (trace.getColor().equals(getColor(i))) {
                     count[i] += 1;
                     break;
                 }
             }
         }
         int lowest = 0;
         int lowestCount = count[lowest];
         for (int i = 1; i < 6; i++) {
             if (count[i] < lowestCount) {
                 lowest = i;
                 lowestCount = count[i];
             }
         }
 
         return getColor(lowest);
     }
 
     /**
      * Setup UI components.
      */
     private void run() {
 
         // Create The main frame sized to be OK on netbook 800x600 displays
         final JFrame frame = new JFrame("DTA Plot v2.1 - http://westboost.github.com/");
         frame.setMinimumSize(new Dimension(400, 400));
         frame.setPreferredSize(new Dimension(1280, 720));
         RunManager.getInstance().setFrame(frame);
         
         final Container content = frame.getContentPane();
         content.setBackground(BKGDCOLOUR);
 
         // Top level layout is Spring
         SpringLayout layout = new SpringLayout();
         content.setLayout(layout);
 
         // Construct the menu area, min width = 100px
         JPanel menuArea = new JPanel();
        GridLayout menuLayout = new GridLayout(0, 1);
         menuArea.setLayout(menuLayout);
         menuArea.setMaximumSize(new Dimension(100, 0));
         menuArea.add(new JLabel("Select Session/Run"));
         menuArea.add(_lapCombo=new JComboBox());
         menuArea.add(_autoSplitCheck = new JCheckBox("Auto Split Runs"));
         menuArea.add(_speedCheck = new JCheckBox("Speed"));
 //        menuArea.add(_timeSlipCheck = new JCheckBox("Time Lag"));
         menuArea.add(_wheelSlipCheck = new JCheckBox("Wheel Slip"));
         menuArea.add(_tpsCheck = new JCheckBox("Throttle"));
         menuArea.add(_mapCheck = new JCheckBox("MAP"));
         menuArea.add(_rpmCheck = new JCheckBox("RPM"));
         menuArea.add(_turboCheck = new JCheckBox("Turbo"));
         menuArea.add(_afrCheck = new JCheckBox("AFR"));
         menuArea.add(_waterTempCheck = new JCheckBox("Water Temp"));
         menuArea.add(_oilTempCheck = new JCheckBox("Oil Temp"));
         menuArea.add(_airTempCheck = new JCheckBox("Air Temp"));
         menuArea.add(new JLabel("")); // Just a spacer
 
         menuArea.add(_clearBtn = new JButton("Clear Traces"));
         menuArea.add(_resetZoomBtn = new JButton("Reset Zoom"));
         menuArea.add(_traceBtn = new JButton("Other Traces..."));
         menuArea.add(_optionsBtn = new JButton("Options..."));
     
         // Construct the chart
         JPanel chartArea = new JPanel();
         BorderLayout chartLayout = new BorderLayout();
         chartArea.setLayout(chartLayout);
         _chart = new ZoomableChart();
         _chart.getAxesXBottom().get(0).setTitle("Time");
         _chart.getAxesXBottom().get(0).setPaintGrid(true);
         _chart.getAxesYLeft().get(0).setTitle("");
         _chart.getAxesYLeft().get(0).setPaintGrid(true);
         chartArea.add(_chart, BorderLayout.CENTER);
 
         _chartHScroll = new JScrollBar(JScrollBar.HORIZONTAL);
         chartArea.add(_chartHScroll, BorderLayout.PAGE_END);
         _chartVScroll = new JScrollBar(JScrollBar.VERTICAL);
         chartArea.add(_chartVScroll, BorderLayout.LINE_END);
 
         // And the message area
         _messagesModel = new DefaultListModel();
         _messagesModel.addElement(new BasicError(BasicError.WARN, "To load a logfile, drag and drop onto graph"));
         _messages = new JList(_messagesModel);
         _messages.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
         _messages.setLayoutOrientation(JList.VERTICAL);
         _messages.setVisibleRowCount(-1);
         BasicErrorRenderer der = new BasicErrorRenderer(frame);
         _messages.setFixedCellHeight(20);
         _messages.setCellRenderer(der);
         _messages.addListSelectionListener(der);
         _messages.validate();
         JScrollPane listScroller = new JScrollPane(_messages);
         listScroller.setBorder(null);
         
         // Build up
         content.add(menuArea);
         content.add(listScroller);
         content.add(chartArea);
 
         // Loads of constraints to align the three areas :-)
         layout.putConstraint(SpringLayout.WEST, menuArea,
                 5, SpringLayout.WEST, content);
         layout.putConstraint(SpringLayout.NORTH, menuArea,
                 5, SpringLayout.NORTH, content);
         layout.putConstraint(SpringLayout.SOUTH, menuArea,
                 -5, SpringLayout.SOUTH, content);
         
         layout.putConstraint(SpringLayout.WEST, listScroller,
                 5, SpringLayout.EAST, menuArea);
         layout.putConstraint(SpringLayout.NORTH, listScroller,
                 -85, SpringLayout.SOUTH, content);
         layout.putConstraint(SpringLayout.EAST, listScroller,
                 -5, SpringLayout.EAST, content);
         layout.putConstraint(SpringLayout.SOUTH, listScroller,
                -5, SpringLayout.SOUTH, content);
         
         layout.putConstraint(SpringLayout.WEST, chartArea,
                 5, SpringLayout.EAST, menuArea);
         layout.putConstraint(SpringLayout.EAST, chartArea,
                 -5, SpringLayout.EAST, content);
         layout.putConstraint(SpringLayout.NORTH, chartArea,
                 5, SpringLayout.NORTH, content);
         layout.putConstraint(SpringLayout.SOUTH, chartArea,
                 -5, SpringLayout.NORTH, listScroller);
                 
         // Handle file dropping
         FileDrop fd = new FileDrop(frame, new FileDrop.Listener() {
             @Override
             public void filesDropped(java.io.File[] files) {
                 RunManager mgr = RunManager.getInstance();
                 for (int i = 0; i < files.length; i++) {
                     loadFile(files[i]);
                 }
             }
         });
 
         // Handle clicks
         _chart.addMouseListener(
                 new MouseAdapter() {
 
                     @Override
                     public void mouseReleased(final MouseEvent m) {
                         if (!_ignoreEvents) {
                             resetChartScroll();
                         }
                     }
                 });
 
         _chartHScroll.addAdjustmentListener(
                 new AdjustmentListener() {
 
                     @Override
                     public void adjustmentValueChanged(AdjustmentEvent a) {
                         if (!_ignoreEvents) {
                             scrollHChart(a.getValue());
                         }
                     }
                 });
 
         _chartVScroll.addAdjustmentListener(
                 new AdjustmentListener() {
 
                     @Override
                     public void adjustmentValueChanged(AdjustmentEvent a) {
                         if (!_ignoreEvents) {
                             scrollVChart(a.getValue());
                         }
                     }
                 });
 
         _lapCombo.addActionListener(
                 new ActionListener() {
 
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         if (!_ignoreEvents) {
                             setOptions();
                         }
                     }
                 });
 
         _traceBtn.addActionListener(
                 new ActionListener() {
 
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         otherTraces(frame, getSelectedRun());
                     }
                 });
 
         _clearBtn.addActionListener(
                 new ActionListener() {
 
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         removeTraces();
                         _chart.zoomAll();
                         List<IAxis> axisl = _chart.getAxesYLeft();
                         for (int a = 0; a < axisl.size(); a++) {
                             axisl.get(a).setRangePolicy(new RangePolicyForcedPoint(0));
                         }
                         resetChartScroll();
                     }
                 });
 
         _resetZoomBtn.addActionListener(
                 new ActionListener() {
 
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         _chart.zoomAll();
                         List<IAxis> axisl = _chart.getAxesYLeft();
                         for (int a = 0; a < axisl.size(); a++) {
                             axisl.get(a).setRangePolicy(new RangePolicyForcedPoint(0));
                         }
                         resetChartScroll();
                     }
                 });
 
         _optionsBtn.addActionListener(
                 new ActionListener() {
 
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         setGlobalOptions(frame);
                     }
                 });
 
         _autoSplitCheck.addItemListener(
                 new ItemListener() {
 
                     @Override
                     public void itemStateChanged(ItemEvent e) {
                         setAutoSplit(content, e.getStateChange() == ItemEvent.SELECTED);
                     }
                 });
 
         _speedCheck.addItemListener(
                 new ItemListener() {
 
                     @Override
                     public void itemStateChanged(ItemEvent e) {
                         if (!_ignoreEvents) {
                             RunManager.Run run = getSelectedRun();
                             toggleTrace(run, SpeedStream.NAME, e.getStateChange() == ItemEvent.SELECTED);
                         }
                     }
                 });
 
         _tpsCheck.addItemListener(
                 new ItemListener() {
 
                     @Override
                     public void itemStateChanged(ItemEvent e) {
                         if (!_ignoreEvents) {
                             RunManager.Run run = getSelectedRun();
                             toggleTrace(run, Log.THROT_STREAM, e.getStateChange() == ItemEvent.SELECTED);
                         }
                     }
                 });
 
         _mapCheck.addItemListener(
                 new ItemListener() {
 
                     @Override
                     public void itemStateChanged(ItemEvent e) {
                         if (!_ignoreEvents) {
                             RunManager.Run run = getSelectedRun();
                             toggleTrace(run, Log.MAP_STREAM, e.getStateChange() == ItemEvent.SELECTED);
                         }
                     }
                 });
 
         _rpmCheck.addItemListener(
                 new ItemListener() {
 
                     @Override
                     public void itemStateChanged(ItemEvent e) {
                         if (!_ignoreEvents) {
                             RunManager.Run run = getSelectedRun();
                             toggleTrace(run, Log.RPM_STREAM, e.getStateChange() == ItemEvent.SELECTED);
                         }
                     }
                 });
 
         /*
         _timeSlipCheck.addItemListener(
                 new ItemListener() {
 
                     @Override
                     public void itemStateChanged(ItemEvent e) {
                         if (!_ignoreEvents) {
                             RunManager.Run run = getSelectedRun();
                             timeSlipTrace(run, e.getStateChange() == ItemEvent.SELECTED);
                         }
                     }
                 });
                 * 
                 */
 
         _turboCheck.addItemListener(
                 new ItemListener() {
 
                     @Override
                     public void itemStateChanged(ItemEvent e) {
                         if (!_ignoreEvents) {
                             RunManager.Run run = getSelectedRun();
                             toggleTrace(run, Log.TURB_STREAM, e.getStateChange() == ItemEvent.SELECTED);
                         }
                     }
                 });
 
         _afrCheck.addItemListener(
                 new ItemListener() {
 
                     @Override
                     public void itemStateChanged(ItemEvent e) {
                         if (!_ignoreEvents) {
                             RunManager.Run run = getSelectedRun();
                             toggleTrace(run, AFRStream.AFR_NAME, e.getStateChange() == ItemEvent.SELECTED);
                         }
                     }
                 });
 
         _wheelSlipCheck.addItemListener(
                 new ItemListener() {
 
                     @Override
                     public void itemStateChanged(ItemEvent e) {
                         if (!_ignoreEvents) {
                             RunManager.Run run = getSelectedRun();
                             toggleTrace(run, Log.SLIP_STREAM, e.getStateChange() == ItemEvent.SELECTED);
                         }
                     }
                 });
 
         _oilTempCheck.addItemListener(
                 new ItemListener() {
 
                     @Override
                     public void itemStateChanged(ItemEvent e) {
                         if (!_ignoreEvents) {
                             RunManager.Run run = getSelectedRun();
                             toggleTrace(run, TempStream.OIL_NAME, e.getStateChange() == ItemEvent.SELECTED);
                         }
                     }
                 });
 
         _waterTempCheck.addItemListener(
                 new ItemListener() {
 
                     @Override
                     public void itemStateChanged(ItemEvent e) {
                         if (!_ignoreEvents) {
                             RunManager.Run run = getSelectedRun();
                             toggleTrace(run, TempStream.WATER_NAME, e.getStateChange() == ItemEvent.SELECTED);
                         }
                     }
                 });
 
         _airTempCheck.addItemListener(
                 new ItemListener() {
 
                     @Override
                     public void itemStateChanged(ItemEvent e) {
                         if (!_ignoreEvents) {
                             RunManager.Run run = getSelectedRun();
                             toggleTrace(run, TempStream.AIR_NAME, e.getStateChange() == ItemEvent.SELECTED);
                         }
                     }
                 });
 
         // Enable the termination button [cross on the upper right edge]: 
         frame.addWindowListener(
                 new WindowAdapter() {
 
                     @Override
                     public void windowClosing(WindowEvent e) {
                         System.exit(0);
                     }
                 });
 
         // All ready to go
         frame.pack();
         frame.setVisible(true);
         setOptions();
     }
 
     /**
      * Load a log file.
      * Errors are displayed on the message panel so no return needed.
      * @param file 
      */
     private void loadFile(File file) {
         RunManager mgr = RunManager.getInstance();
         WithError<Boolean, BasicError> ok = new WithError(true);
         try {
             mgr.addLogfile(file, ok);
         } catch (IOException ex) {
             ok.addError(new BasicError(ex));
         }
         
         ArrayList<BasicError> errs = ok.errors();
         for (BasicError e : errs) {
             _messagesModel.add(0, e);
         }
 
         if (ok.value().booleanValue() == true) {
             addedLogs();
         }
         
     }
 
     /**
      * Display selection dialog for other traces for current run.
      * @param frame Frame to attach dialog to
      * @param run The current run
      */
     private void otherTraces(JFrame frame, RunManager.Run run) {
         final JDialog dialog = new JDialog(frame, "Other Traces", true);
 
         JPanel traceArea = new JPanel();
         GridLayout layout = new GridLayout(0, 4);
         traceArea.setLayout(layout);
 
         final ArrayList<JCheckBox> checks = new ArrayList();
         for (int t = 0; t < run.streamCount(); t++) {
             ROStream s = run.getStream(t);
             
             // Ignore streams displayed on UI
             if (s.name().equals(Log.SESSION_STREAM) || s.name().equals(Log.TIME_STREAM)
                     || s.name().equals(SpeedStream.NAME) || s.name().equals(Log.THROT_STREAM)
                     || s.name().equals(Log.MAP_STREAM) || s.name().equals(Log.RPM_STREAM)
                     || s.name().equals(Log.TURB_STREAM) || s.name().equals(AFRStream.AFR_NAME)
                     || s.name().equals(TempStream.WATER_NAME) || s.name().equals(TempStream.OIL_NAME)
                     || s.name().equals(TempStream.AIR_NAME) || s.name().equals(Log.SLIP_STREAM)) {
                 continue;
             }
             
             // Ignore streams that have been hidden
             if (s.getMeta("hide").equals("true")) {
                 continue;
             }
 
             JCheckBox cb = new JCheckBox(s.name());
             cb.setSelected(findTrace(run.name() + " " + s.description())!=null);
             checks.add(cb);
             traceArea.add(cb);
         }
 
         JPanel buttonArea = new JPanel();
         FlowLayout buttonLayout = new FlowLayout(FlowLayout.RIGHT);
         buttonArea.setLayout(buttonLayout);
         JButton clearBtn = new JButton("Clear All");
         JButton applyBtn = new JButton("Apply");
         JButton okBtn = new JButton("OK");
         buttonArea.add(clearBtn);
         buttonArea.add(applyBtn);
         buttonArea.add(okBtn);
 
         clearBtn.addActionListener(
                 new ActionListener() {
 
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         for (int i = 0; i < checks.size(); i++) {
                             checks.get(i).setSelected(false);
                         }
                     }
                 });
 
         applyBtn.addActionListener(
                 new ActionListener() {
 
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         RunManager.Run run = getSelectedRun();
                         for (int i = 0; i < checks.size(); i++) {
                             toggleTrace(run, checks.get(i).getText(), checks.get(i).isSelected());
                         }
                     }
                 });
 
 
         okBtn.addActionListener(
                 new ActionListener() {
 
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         RunManager.Run run = getSelectedRun();
                         for (int i = 0; i < checks.size(); i++) {
                             toggleTrace(run, checks.get(i).getText(), checks.get(i).isSelected());
                         }
                         dialog.setVisible(false);
                     }
                 });
 
         JPanel dialogPanel = new JPanel();
         BorderLayout dialogLayout = new BorderLayout();
         dialogPanel.setLayout(dialogLayout);
 
         dialogPanel.add(traceArea, BorderLayout.CENTER);
         dialogPanel.add(buttonArea, BorderLayout.PAGE_END);
 
         Container content = dialog.getContentPane();
         content.add(dialogPanel);
 
         dialog.pack();
         dialog.setLocationRelativeTo(frame);
         dialog.setVisible(true);
         
     }
 
     /**
      * Display selection dialog for global options
      * @param frame Frame to attach dialog to
      */
     private void setGlobalOptions(JFrame frame) {
 
         JDialog dialog = new JDialog(frame, "DTA System Units", true);
         Container content = dialog.getContentPane();
         OptionsDialog oDlg = new OptionsDialog();
         content.add(oDlg);
 
         dialog.pack();
         dialog.setLocationRelativeTo(frame);
         dialog.setVisible(true);
     }
 
     private void setAutoSplit(Container content, boolean on) {
         RunManager.getInstance().setAutoSplit(on);
     }
 
     private synchronized void addedLogs() {
 
         RunManager mgr = RunManager.getInstance();
         RunManager.Run[] runs = mgr.getRuns();
         for (int l = 0; l < runs.length; l++) {
             RunManager.Run r = runs[l];
 
             int at = -1;
             for (int i = 0; i < _lapCombo.getItemCount(); i++) {
                 if (((String) _lapCombo.getItemAt(i)).equals(r.name())) {
                     at = i;
                     break;
                 }
             }
 
             if (at == -1) {
                 _lapCombo.addItem(r.name());
                 if (r.isSplit()) {
                     toggleTrace(r, SpeedStream.NAME, true);
                 }
             }
         }
         _chart.zoomAll();
         resetChartScroll();
         setOptions();
     }
 
     private void resetChartScroll() {
         IAxis xa = _chart.getAxisX();
         if (xa != null) {
             double range = xa.getMaxValue() - xa.getMinValue();
             double shown = xa.getMax() - xa.getMin();
             if (range > 0 && range > shown) {
                 int steps = 10 * (int) Math.ceil(range / shown);
                 double rangePerStep = range / steps;
                 double startOffset = xa.getMin() - xa.getMinValue();
 
                 _ignoreEvents = true;
                 _chartHScroll.setVisible(true);
                 _chartHScroll.setMinimum(0);
                 _chartHScroll.setValue((int) Math.ceil(startOffset / rangePerStep));
                 _chartHScroll.setMaximum(steps);
                 _ignoreEvents = false;
             } else {
                 _chartHScroll.setVisible(false);
             }
         }
 
         // Scan all axis for smallest viewport vs the axis range
         int steps = 0;
         IAxis ya = null;
         List<IAxis> axisl = _chart.getAxesYLeft();
         for (int a = 0; a < axisl.size(); a++) {
             IAxis ax = axisl.get(a);
             double range = ax.getMaxValue() - Math.min(0, ax.getMinValue());
             double shown = ax.getMax() - ax.getMin();
             if (range > 0 && range > shown) {
                 int s = 10 * (int) Math.ceil(range / shown);
                 if (s > steps) {
                     steps = s;
                     ya = ax;
                 }
             }
         }
 
         // Now set per that axis
         _chartVScroll.setVisible(false);
         if (ya != null) {
             double range = ya.getMaxValue() - Math.min(0, ya.getMinValue());
             double shown = ya.getMax() - ya.getMin();
             if (range > 0 && range > shown) {
                 double rangePerStep = range / steps;
                 double startOffset = ya.getMin() - Math.min(0, ya.getMinValue());
 
                 _ignoreEvents = true;
                 _chartVScroll.setVisible(true);
                 _chartVScroll.setMinimum(0);
                 _chartVScroll.setMaximum(steps);
                 _chartVScroll.setValue(steps - 10 - (int) Math.ceil(startOffset / rangePerStep));
                 _ignoreEvents = false;
             }
         }
     }
 
     private void scrollHChart(int value) {
         IAxis xa = _chart.getAxisX();
         if (xa != null && _chartHScroll.isVisible()) {
             double range = xa.getMaxValue() - xa.getMinValue();
             double shown = xa.getMax() - xa.getMin();
 
             int steps = 10 * (int) Math.ceil(range / shown);
             double rangePerStep = range / steps;
 
             double min = value * rangePerStep;
             double max = min + (xa.getMax() - xa.getMin());
 
             _chart.zoom(min, max);
         }
     }
 
     private void scrollVChart(int value) {
         List<IAxis> axisl = _chart.getAxesYLeft();
         for (int a = 0; a < axisl.size(); a++) {
             IAxis ya = axisl.get(a);
             if (ya != null && _chartVScroll.isVisible()) {
                 double range = ya.getMaxValue() - Math.min(0, ya.getMinValue());
                 double shown = ya.getMax() - ya.getMin();
 
                 int steps = 10 * (int) Math.ceil(range / shown);
                 double rangePerStep = range / steps;
 
                 double min = (steps - 10 - value) * rangePerStep + Math.min(0, ya.getMinValue());
                 double max = min + (ya.getMax() - ya.getMin());
 
                 IRangePolicy zoomPolicy = new RangePolicyFixedViewport(new Range(min, max));
                 ya.setRangePolicy(zoomPolicy);
             }
         }
     }
 
     private synchronized RunManager.Run getSelectedRun() {
 
         String run = (String) _lapCombo.getSelectedItem();
 
         RunManager mgr = RunManager.getInstance();
         RunManager.Run[] runs = mgr.getRuns();
         for (int l = 0; l < runs.length; l++) {
             RunManager.Run r = runs[l];
             if (r.name().equals(run)) {
                 return r;
             }
         }
         return null;
     }
 
     private synchronized void setOptions() {
 
         _autoSplitCheck.setSelected(RunManager.getInstance().isAutoSplit());
 
         RunManager.Run run = getSelectedRun();
         if (run != null) {
             _ignoreEvents = true;
 
             _speedCheck.setEnabled(run.log().hasStream(SpeedStream.NAME));
             _tpsCheck.setEnabled(run.log().hasStream(Log.THROT_STREAM));
             _mapCheck.setEnabled(run.log().hasStream(Log.MAP_STREAM));
             _rpmCheck.setEnabled(run.log().hasStream(Log.RPM_STREAM));
             //_timeSlipCheck.setEnabled(run.log().hasStream(Log.TimeSlip_STREAM));
             _turboCheck.setEnabled(run.log().hasStream(Log.TURB_STREAM));
             _afrCheck.setEnabled(run.log().hasStream(Log.LAMB_STREAM));
             _waterTempCheck.setEnabled(run.log().hasStream(TempStream.WATER_NAME));
             _oilTempCheck.setEnabled(run.log().hasStream(TempStream.OIL_NAME));
             _airTempCheck.setEnabled(run.log().hasStream(TempStream.AIR_NAME));
             _wheelSlipCheck.setEnabled(run.log().hasStream(Log.SLIP_STREAM));
 
             _speedCheck.setSelected(false);
             _tpsCheck.setSelected(false);
             _mapCheck.setSelected(false);
             _rpmCheck.setSelected(false);
             //_timeSlipCheck.setSelected(false);
             _turboCheck.setSelected(false);
             _afrCheck.setSelected(false);
             _waterTempCheck.setSelected(false);
             _oilTempCheck.setSelected(false);
             _airTempCheck.setSelected(false);
             _wheelSlipCheck.setSelected(false);
 
             ITrace2D[] traces = _chart.getTraces().toArray(new ITrace2D[0]);
             String name = run.name();
             for (int i = 0; i < traces.length; i++) {
                 if (traces[i].getName().equals(name + " " + Log.getStreamDescription(SpeedStream.NAME))) {
                     _speedCheck.setSelected(true);
                 } else if (traces[i].getName().equals(name + " " + Log.getStreamDescription(Log.THROT_STREAM))) {
                     _tpsCheck.setSelected(true);
                 } else if (traces[i].getName().equals(name + " " + Log.getStreamDescription(Log.MAP_STREAM))) {
                     _mapCheck.setSelected(true);
                 } else if (traces[i].getName().equals(name + " " + Log.getStreamDescription(Log.RPM_STREAM))) {
                     _rpmCheck.setSelected(true);
 //                } else if (traces[i].getName().equals(name + " Time Lag")) {
 //                    _timeSlipCheck.setSelected(true);
                 } else if (traces[i].getName().equals(name + " " + Log.getStreamDescription(Log.TURB_STREAM))) {
                     _turboCheck.setSelected(true);
                 } else if (traces[i].getName().equals(name + " " + Log.getStreamDescription(AFRStream.AFR_NAME))) {
                     _afrCheck.setSelected(true);
                 } else if (traces[i].getName().equals(name + " " + Log.getStreamDescription(TempStream.WATER_NAME))) {
                     _waterTempCheck.setSelected(true);
                 } else if (traces[i].getName().equals(name + " " + Log.getStreamDescription(TempStream.OIL_NAME))) {
                     _oilTempCheck.setSelected(true);
                 } else if (traces[i].getName().equals(name + " " + Log.getStreamDescription(TempStream.AIR_NAME))) {
                     _airTempCheck.setSelected(true);
                 } else if (traces[i].getName().equals(name + " " + Log.getStreamDescription(Log.SLIP_STREAM))) {
                     _wheelSlipCheck.setSelected(true);
                 }
             }
 
             _chart.setToolTipText("Select an area with mouse to zoom in");
             _lapCombo.setToolTipText("Select run to change traces");
             _ignoreEvents = false;
         } else {
             _speedCheck.setEnabled(false);
             _tpsCheck.setEnabled(false);
             _mapCheck.setEnabled(false);
             _rpmCheck.setEnabled(false);
             //_timeSlipCheck.setEnabled(false);
             _turboCheck.setEnabled(false);
             _afrCheck.setEnabled(false);
             _waterTempCheck.setEnabled(false);
             _oilTempCheck.setEnabled(false);
             _airTempCheck.setEnabled(false);
             _wheelSlipCheck.setEnabled(false);
         }
     }
 
     private synchronized ITrace2D findTrace(String name) {
         ITrace2D[] traces = _chart.getTraces().toArray(new ITrace2D[0]);
         for (int i = 0; i < traces.length; i++) {
             if (traces[i].getName().equals(name)) {
                 return traces[i];
             }
         }
         return null;
     }
 
     private synchronized ITrace2D findTraceContains(String substring) {
         ITrace2D[] traces = _chart.getTraces().toArray(new ITrace2D[0]);
         for (int i = 0; i < traces.length; i++) {
             if (traces[i].getName().contains(substring)) {
                 return traces[i];
             }
         }
         return null;
     }
 
     private synchronized IAxis getAxis(String label) {
         List<IAxis> axisl = _chart.getAxesYLeft();
         IAxis axis = null;
         IAxis empty = null;
         for (int a = 0; a < axisl.size(); a++) {
             if (axisl.get(a).getTitle().equals(label)) {
                 axis = axisl.get(a);
                 break;
             }
             if (axisl.get(a).getTitle().length() == 0) {
                 empty = axisl.get(a);
             }
         }
         if (axis == null) {
             axis = empty;
             if (axis == null) {
                 AAxis a = new AxisLinear();
                 _chart.addAxisYLeft(a);
                 axis = a;
             }
             axis.setAxisTitle(new AxisTitle(label));
             axis.setRangePolicy(new RangePolicyForcedPoint(0));
         }
         return axis;
     }
 
     private synchronized void cleanAxis(String label) {
         List<IAxis> axisl = _chart.getAxesYLeft();
         for (int a = 0; a < axisl.size(); a++) {
             if (axisl.get(a).getTitle().equals(label)) {
                 if (a == 0) {
                     if (axisl.size() > 1) {
                         // Swap traces from last to first axis
                         IAxis last = axisl.get(axisl.size() - 1);
                         _chart.removeAxisYLeft(last);
 
                         ITrace2D[] traces = last.getTraces().toArray(new ITrace2D[0]);
                         last.removeAllTraces();
                         axisl.get(0).setTitle(last.getTitle());
                         for (int i = 0; i < traces.length; i++) {
                             axisl.get(0).addTrace(traces[i]);
                         }
 
                     } else {
                         axisl.get(a).setTitle("");
                     }
                 } else {
                     _chart.removeAxisYLeft(axisl.get(a));
                 }
             }
         }
     }
 
     private synchronized void removeTraces() {
         _chart.removeAllTraces();
         IAxis[] axisl = _chart.getAxesYLeft().toArray(new IAxis[0]);
         for (int a = 0; a < axisl.length; a++) {
             axisl[a].removeAllTraces();
             if (a == 0) {
                 axisl[a].setTitle("");
             } else {
                 _chart.removeAxisYLeft(axisl[a]);
             }
         }
         setOptions();
         _chart.zoomAll();
     }
     
     private synchronized void toggleTrace(RunManager.Run r, String streamName, boolean on) {
 
         ROStream s = r.getStream(streamName);
         assert(s!=null);
         String suffix = "";
         if (s.units() != null && !s.units().isEmpty()) {
             suffix = " (" + s.units() + ")";
         }
         String axisName = s.axis();
 
         ITrace2D trace = findTrace(r.name() + " " + s.description());
         if (on) {
             if (trace == null) {
                 IAxis axis = getAxis(axisName + suffix);
                 trace = new Trace(r.name() + " " + s.description());
                 trace.setColor(getNextColor());
                 _chart.addTrace(trace, _chart.getAxesXBottom().get(0), axis);
 
                 for (int i = 0; i < r.length(); i++) {
                     trace.addPoint(i * 0.1, s.getNumeric(i));
                 }
             }
         } else {
             if (trace != null) {
                 IAxis axis = getAxis(axisName + suffix);
                 axis.removeTrace(trace);
                 _chart.removeTrace(trace);
                 if (findTraceContains(axisName) == null) {
                     cleanAxis(axisName + suffix);
                 }
             }
 
         }
     }
 
     /*
      * private synchronized void steerTrace(RunManager.Run r, boolean on) {
      *
      * String axisName="Steering Angle (+ve Right, -ve Left)"; String
      * traceName=r.name() + " Steering Angle"; ITrace2D trace =
      * findTrace(traceName); if (on) { if (trace == null) { IAxis axis =
      * getAxis(axisName); trace = new Trace(traceName);
      * trace.setColor(getNextColor()); _chart.addTrace(trace,
      * _chart.getAxesXBottom().get(0), axis);
      *
      * double[] avg=new double[5]; int at=0; for (int i = 0; i < r.length();
      * i++) { //avg[at]=r.degrees(i); at=(at+1)%5;
      *
      * trace.addPoint(i * 0.1, (avg[0]+avg[1]+avg[2]+avg[3]+avg[4])/5); } } }
      * else { if (trace != null) { IAxis axis = getAxis(axisName);
      * axis.removeTrace(trace); _chart.removeTrace(trace); if
      * (findTraceContains(axisName) == null) { cleanAxis(axisName); } } } }
      *
      * private synchronized void latAccelTrace(RunManager.Run r, boolean on) {
      *
      * String axisName="Lat. Accel. (+ve Right, -ve Left)"; String
      * traceName=r.name() + " Lat. Accel."; ITrace2D trace =
      * findTrace(traceName); if (on) { if (trace == null) { IAxis axis =
      * getAxis(axisName); trace = new Trace(traceName);
      * trace.setColor(getNextColor()); _chart.addTrace(trace,
      * _chart.getAxesXBottom().get(0), axis);
      *
      * double[] avg=new double[5]; int at=0; for (int i = 0; i < r.length();
      * i++) { //avg[at]=r.latAccel(i); at=(at+1)%5;
      *
      * trace.addPoint(i * 0.1, (avg[0]+avg[1]+avg[2]+avg[3]+avg[4])/5); } } }
      * else { if (trace != null) { IAxis axis = getAxis(axisName);
      * axis.removeTrace(trace); _chart.removeTrace(trace); if
      * (findTraceContains(axisName) == null) { cleanAxis(axisName); } } } }
      *
      * private synchronized void longAccelTrace(RunManager.Run r, boolean on) {
      *
      * String axisName="Long. Accel. (g)"; String traceName=r.name() + " Long.
      * Accel."; ITrace2D trace = findTrace(traceName); if (on) { if (trace ==
      * null) { IAxis axis = getAxis(axisName); trace = new Trace(traceName);
      * trace.setColor(getNextColor()); _chart.addTrace(trace,
      * _chart.getAxesXBottom().get(0), axis);
      *
      * double[] avg=new double[5]; int at=0; for (int i = 0; i < r.length();
      * i++) { //avg[at]=r.speedKPH(i);
      *
      * double a=avg[at]-avg[(at+1)%5]; a=a*1000/3600; a=a/0.5; a=a/9.8;
      * trace.addPoint(i * 0.1, a); at=(at+1)%5; } } } else { if (trace != null)
      * { IAxis axis = getAxis(axisName); axis.removeTrace(trace);
      * _chart.removeTrace(trace); if (findTraceContains(axisName) == null) {
      * cleanAxis(axisName); } } }
     }
      */
     private synchronized void timeSlipTrace(RunManager.Run r, boolean on) {
         ITrace2D trace = findTrace(r.name() + " Time Lag");
         if (on) {
             if (trace == null) {
                 IAxis axis = getAxis("Time Lag (sec)");
                 trace = new Trace(r.name() + " Time Lag");
                 trace.setColor(getNextColor());
                 _chart.addTrace(trace, _chart.getAxesXBottom().get(0), axis);
                 for (int i = 0; i < r.length(); i++) {
                     //trace.addPoint(i * 0.1, r.timeSlip(i));
                 }
             }
         } else {
             if (trace != null) {
                 IAxis axis = getAxis("Time Lag (sec)");
                 axis.removeTrace(trace);
                 _chart.removeTrace(trace);
                 if (findTraceContains("Time Lag") == null) {
                     cleanAxis("Time Lag (sec)");
                 }
             }
         }
     }
     
     public static void initializeFontSize(int scale) {
         float multiplier = scale / 100.0f;
         UIDefaults defaults = UIManager.getDefaults();
         int i = 0;
         for (Enumeration e = defaults.keys(); e.hasMoreElements(); i++) {
             Object key = e.nextElement();
             Object value = defaults.get(key);
             if (value instanceof Font) {
                 Font font = (Font) value;
                 int newSize = Math.round(font.getSize() * multiplier);
                 if (value instanceof FontUIResource) {
                     UIManager.put(key, new FontUIResource(font.getName(), font.getStyle(), newSize));
                 } else {
                     UIManager.put(key, new Font(font.getName(), font.getStyle(), newSize));
                 }
             }
         }
     }
         
 }
