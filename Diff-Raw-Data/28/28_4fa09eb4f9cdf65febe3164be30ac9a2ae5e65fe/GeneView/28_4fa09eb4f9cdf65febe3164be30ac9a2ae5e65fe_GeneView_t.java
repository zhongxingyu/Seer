 /*
  *   ngsv
  *   https://github.com/xcoo/ngsv
  *   Copyright (C) 2012, Xcoo, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package genome.view;
 
 import genome.config.Config;
 import genome.config.ViewerConfig;
 import genome.data.Bed;
 import genome.data.BedFragment;
 import genome.data.Chromosome;
 import genome.data.CytoBand;
 import genome.data.Sam;
 import genome.data.SamHistogram;
 import genome.data.ViewScale;
 import genome.db.SQLLoader;
 import genome.net.DataSelectionListener;
 import genome.net.Selection;
 import genome.net.WebSocket;
 import genome.view.SamSelectionDialogBox.SamSelectionDialongBoxListener;
 import genome.view.chart.BedChart;
 import genome.view.chart.ChartManager;
 import genome.view.chart.CytobandChart;
 import genome.view.chart.GeneChart;
 import genome.view.chart.HistogramChart;
 import genome.view.element.BedFragmentElement;
 import genome.view.element.CytobandElement;
 import genome.view.element.ExonElement;
 import genome.view.element.GeneElement;
 import genome.view.element.HistogramBinElement;
 import genome.view.element.RulerElement;
 import genome.view.ui.DebugView;
 import genome.view.ui.Indicator;
 import genome.view.ui.Ruler;
 import genome.view.ui.ScaleController;
 import genome.view.ui.ScaleControllerCallback;
 import genome.view.updater.BedUpdater;
 import genome.view.updater.GeneUpdater;
 import genome.view.updater.HistogramUpdater;
 
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import casmi.Applet;
 import casmi.AppletRunner;
 import casmi.KeyEvent;
 import casmi.MouseButton;
 import casmi.MouseEvent;
 import casmi.Trackball;
 import casmi.graphics.color.ColorSet;
 import casmi.graphics.color.GrayColor;
 import casmi.graphics.element.Line;
 import casmi.graphics.element.Text;
 import casmi.graphics.font.Font;
 import casmi.graphics.font.FontStyle;
 import casmi.graphics.object.GraphicsObject;
 import casmi.parser.JSON;
 
 /**
  * Class for Gene View
  *
  * @author K. Nishimura
  *
  */
 public class GeneView extends Applet implements SamSelectionDialongBoxListener, DataSelectionListener {
 
     private static final long serialVersionUID = 1L;
 
     static Logger logger = LoggerFactory.getLogger(GeneView.class);
     static String[] args;
 
     private static final String CONFIG_INI_PATH = "./config/ngsv.ini";
     private static final String VIEWER_INI_PATH = "./config/viewer.ini";
 
     private double scale = 1.0;
     private double minScale = 0.000004;
     private double maxScale = 2.0;
     private double wheelScaleFactor = 0.007;
 
     private Scroll scroll;
     private double mouseScrollSpeedFactor = 15.0;
 
     private long leftValue = 0, rightValue = 0;
 
     private long loadBinSize = 0;
     private long dispBinSize = 0;
     private long start = 0, end = 0;
 
     private GraphicsObject baseObject = new GraphicsObject();
 
     // Charts
     private static ChartManager chartManager;
     private GeneChart geneChart;
     private Map<Long, HistogramChart> histogramChartMap = new HashMap<Long, HistogramChart>();
     private CytobandChart cytobandChart;
     private Map<Long, BedChart> bedChartMap = new HashMap<Long, BedChart>();
 
     // Updaters
     private Map<Long, HistogramUpdater> histogramUpdaterMap = new HashMap<Long, HistogramUpdater>();
     private Map<Long, BedUpdater> bedUpdaterMap = new HashMap<Long, BedUpdater>();
     private GeneUpdater geneUpdater;
 
     // UI
     private Ruler ruler;
     private Text annotationText;
     private ScaleController scaleController;
     private Indicator indicator;
     private DebugView debugView;
 
     // ref genes
     private ViewScale viewScale;
 
     // sam, histogram, bed, and chromosome data
     private Sam[] samFiles;
     private List<Sam> selectedSamList = new ArrayList<Sam>();
 
     private Bed[] bedFiles;
     private List<Bed> selectedBedList = new ArrayList<Bed>();
 
     private Chromosome[] chromosomes;
     private Chromosome selectedChromosome;
 
     private CytoBand[] cytobands;
 
     private SQLLoader sqlLoader;
 
     // dialog box
     private SamSelectionDialogBox dbox;
 
     private boolean initializing = true;
     private boolean rotating = false;
 
     // trackball
     private Trackball trackball;
    private int prevMouseX = 0, prevMouseY = 0;
 
     private WebSocket webSocket;
 
     @Override
     public void setup() {
         // Load configuration
         // -----------------------------------------
         Config conf = Config.getInstance();
         if (args.length > 0)
             conf.load(args[0]);
         else
             conf.load(CONFIG_INI_PATH);
 
         ViewerConfig vconf = ViewerConfig.getInstance();
         vconf.load(VIEWER_INI_PATH);
 
         // Initialize global settings
         // -----------------------------------------
         setFPS(vconf.getFPS());
         setSize(vconf.getWindowWidth(), vconf.getWindowHeight());
         setBackGroundColor(ColorSet.BLACK);
 
         scale = vconf.getInitialScale();
         minScale = vconf.getMinScale();
         maxScale = vconf.getMaxScale();
         wheelScaleFactor = vconf.getWheelScaleFactor();
         mouseScrollSpeedFactor = vconf.getMouseScrollSpeedFactor();
 
         // load data
         // -----------------------------------------
 
         // load mysql database
         try {
             sqlLoader = new SQLLoader();
         } catch (Exception e) {
             logger.error("Failed to connect to database.");
             showAlert("Error", "Could not connect to database");
             System.exit(-1);
         }
 
         // Load sam files
         samFiles = sqlLoader.loadSamFiles();
         logger.info("Read " + samFiles.length + " sam files.");
 
         // Load bed files
         bedFiles = sqlLoader.loadBedFiles();
         logger.info("Read " + bedFiles.length + " bed files.");
 
         // Load chromosome
         chromosomes = sqlLoader.loadChromosome();
 
         // Load cytoband
         cytobands = sqlLoader.loadCytoBand();
         Map<String, Long> chrLengthMap = calcCytoBandLength(cytobands, chromosomes);
 
         // WebSocket
         try {
             webSocket = new WebSocket(this);
         } catch (URISyntaxException e) {
             logger.error("Failed to create WebSocket connection");
             e.printStackTrace();
             System.exit(-1);
         }
         webSocket.connect();
 
         // Dialog box for data selection
         dbox = new SamSelectionDialogBox(samFiles, bedFiles, chrLengthMap, sqlLoader, this);
 
         initUI();
     }
 
     public void initUI() {
         ViewerConfig vconf = ViewerConfig.getInstance();
         scroll = new Scroll(vconf.getScrollSpeedEps(), vconf.getScrollPowerFactor(),
             vconf.getScrollSpeedDampingFactor());
 
         trackball = new Trackball(getWidth(), getHeight());
 
         Font f = new Font("Sans-Serif", FontStyle.PLAIN, 12.0);
         annotationText = new Text("", f);
         annotationText.setStrokeColor(new GrayColor(0.75));
 
         scaleController = new ScaleController(minScale, maxScale, scale, getMouse());
         scaleController.setPosition(getWidth() - 30, getHeight() / 2);
         scaleController.addCallback(new ScaleControllerCallback() {
 
             @Override
             public void run(double value) {
                 scale = value;
             }
         });
 
         indicator = new Indicator(getWidth() - 40, getHeight() - 40);
 
         debugView = new DebugView(this);
         debugView.setPosition(getWidth() - 70, 20);
         debugView.setVisible(false);
 
         addObject(baseObject);
         addObject(annotationText);
         addObject(scaleController);
         addObject(indicator);
         addObject(debugView);
     }
 
     private Map<String, Long> calcCytoBandLength(CytoBand[] cytoBands, Chromosome[] chromosomes) {
         Map<String, Long> map = new HashMap<String, Long>();
 
         for (CytoBand b : cytoBands) {
             String chrName = findChromosome(b.getChrId(), chromosomes).getChromosome();
             b.setChrName(chrName);
             Long curr = map.get(b.getChrId());
 
             if (curr != null) {
                 if (curr < b.getEnd()) {
                     map.put(chrName, b.getEnd());
                 }
             } else {
                 map.put(chrName, b.getEnd());
             }
         }
 
         return map;
     }
 
     private Chromosome findChromosome(long chrId, Chromosome[] chromosomes) {
         for (Chromosome c : chromosomes) {
             if (chrId == c.getChrId()) {
                 return c;
             }
         }
         return null;
     }
 
     private Chromosome findChromosome(String chr, Chromosome[] chromosomes) {
         for(Chromosome c : chromosomes) {
             if (c.getChromosome().equalsIgnoreCase(chr) ||
                 c.getChromosome().equalsIgnoreCase("chr" + chr)) {
                 return c;
             }
         }
         return null;
     }
 
     private void reloadViewData(String chr, long start, long end) {
         logger.debug("Reloading view data...");
 
         selectedSamList.clear();
         selectedBedList.clear();
 
         selectedChromosome = findChromosome(chr, chromosomes);
         if (selectedChromosome == null) {
             return;
         }
 
         // load histogram
         for (Sam sam : samFiles) {
             if (sam.isSelected()) {
                 // Load histograms
                 SamHistogram[] shs = sqlLoader.loadSamHistogram(sam.getSamId());
                 sam.setSamHistograms(shs);
 
                 logger.info("Loaded " + shs.length + " SamHistograms.");
 
                 selectedSamList.add(sam);
             }
         }
 
         for (Bed bed : bedFiles) {
             if (bed.isSelected()) {
                 BedFragment[] bedFragments = sqlLoader.loadBedFragment(bed.getBedId(), selectedChromosome);
                 bed.setBedFragments(bedFragments);
                 selectedBedList.add(bed);
             }
         }
     }
 
     private void rebuildViewData(String chr, long start, long end) {
         logger.debug("Rebuilding view data...");
 
         // Clear existing objects
         baseObject.clear();
         histogramChartMap.clear();
         histogramUpdaterMap.clear();
         bedChartMap.clear();
         bedUpdaterMap.clear();
 
         chartManager = new ChartManager();
 
         Chromosome c = findChromosome(chr, chromosomes);
         if (c == null) return;
 
         scroll.setX(-(start + end)/ 2.0);
         scroll.setY(0.0);
         scale = ViewerConfig.getInstance().getInitialScale();
 
         setupRuler(start, end);
         setupHistogramCharts(selectedSamList);
         setupBedCharts(selectedBedList);
         setupCytobandChart(c.getChromosome());
         setupGeneChart();
     }
 
     private void setupRuler(long start, long end) {
         ruler = new Ruler(start, end, scale);
         ruler.getMainLine().set(start, 0, end, 0);
         baseObject.add(ruler);
     }
 
     private void setupHistogramCharts(List<Sam> sams) {
         for (Sam sam : sams) {
             setupHistogramChart(sam);
             histogramUpdaterMap.put(sam.getSamId(),
                 new HistogramUpdater(sqlLoader, annotationText, getMouse()));
         }
 
         int i = 0;
         for (Map.Entry<Long, HistogramChart> e : histogramChartMap.entrySet()) {
             HistogramChart chart = e.getValue();
             chart.setY(chart.getY() + (HistogramBinElement.MAX_HEIGHT + 10.0) * i++);
         }
     }
 
     private void setupHistogramChart(Sam sam) {
         HistogramChart chart = new HistogramChart(sam, scale, getMouse());
         chart.setY(ViewerConfig.getInstance().getHistogramPosY());
 
         histogramChartMap.put(sam.getSamId(), chart);
         baseObject.add(chart);
         chartManager.addChart(chart);
     }
 
     private void setupBedCharts(List<Bed> beds) {
         for (Bed bed : selectedBedList) {
             setupBedChart(bed);
             bedUpdaterMap.put(bed.getBedId(), new BedUpdater(sqlLoader, annotationText, getMouse()));
         }
 
         int i = 0;
         for (Map.Entry<Long, BedChart> e : bedChartMap.entrySet()) {
             BedChart chart = e.getValue();
             chart.setY(chart.getY() + (100.0 + 10.0) * i++);
         }
     }
 
     private void setupBedChart(Bed bed) {
         BedChart chart = new BedChart(bed, scale, getMouse());
         chart.setY(ViewerConfig.getInstance().getBedPosY());
 
         bedChartMap.put(bed.getBedId(), chart);
         baseObject.add(chart);
         chartManager.addChart(chart);
     }
 
     private void setupCytobandChart(String chr) {
         cytobandChart = new CytobandChart(cytobands, chr,
             ViewerConfig.getInstance().getCytobandHeight(), scale, getMouse());
         cytobandChart.setY(ViewerConfig.getInstance().getCytobandPosY());
 
         for (CytobandElement e : cytobandChart.getCytobandElementList()) {
             e.addMouseEventCallback(new AnnotationMouseOverCallback(e.getName(), annotationText, getMouse()));
         }
 
         baseObject.add(cytobandChart);
         chartManager.addChart(cytobandChart);
     }
 
     private void setupGeneChart() {
         geneChart = new GeneChart(scale, getMouse());
         geneChart.setY(ViewerConfig.getInstance().getRulerPosY());
         baseObject.add(geneChart);
         chartManager.addChart(geneChart);
         geneUpdater = new GeneUpdater(sqlLoader, annotationText, getMouse());
     }
 
     @Override
     public void update() {
         // Update scroll
         scroll.update(getFPS(), scale);
 
         if (initializing) return;
 
         leftValue = (long)(-scroll.getX() - 550.0 / scale);
         rightValue = (long)(-scroll.getX() + 550.0 / scale);
 
         // Update data.
         updateData();
 
         // Update graphics objects.
         updateElements();
     }
 
     private void updateData() {
         // Select appropriate binSize.
         long newDispBinSize = 100;
         long newLoadBinSize = 100;
 
         double base = 0.2;
         if (scale < base * 0.0001) {
             newDispBinSize = 10000000;
             newLoadBinSize = 1000000;
         } else if (scale < base * 0.0002) {
             newDispBinSize = 1000000;
             newLoadBinSize = 1000000;
         } else if (scale < base * 0.001) {
             newDispBinSize = 100000;
             newLoadBinSize = 10000;
         } else if (scale < base * 0.1 * 0.1 * 0.125) {
             newDispBinSize = 80000;
             newLoadBinSize = 10000;
         } else if (scale < base * 0.1 * 0.1 * 0.17) {
             newDispBinSize = 60000;
             newLoadBinSize = 10000;
         } else if (scale < base * 0.1 * 0.1 * 0.25) {
             newDispBinSize = 40000;
             newLoadBinSize = 10000;
         } else if (scale < base * 0.1 * 0.1 * 0.5) {
             newDispBinSize = 20000;
             newLoadBinSize = 10000;
         } else if (scale < base * 0.1 * 0.1) {
             newDispBinSize = 10000;
             newLoadBinSize = 10000;
         } else if (scale < base * 0.1 * 0.125) {
             newDispBinSize = 8000;
             newLoadBinSize = 100;
         } else if (scale < base * 0.1 * 0.17) {
             newDispBinSize = 6000;
             newLoadBinSize = 100;
         } else if (scale < base * 0.1 * 0.25) {
             newDispBinSize = 4000;
             newLoadBinSize = 100;
         } else if (scale < base * 0.1 * 0.5) {
             newDispBinSize = 2000;
             newLoadBinSize = 100;
         } else if (scale < base * 0.1) {
             newDispBinSize = 1000;
             newLoadBinSize = 100;
         } else if (scale < base * 0.125) {
             newDispBinSize = 800;
             newLoadBinSize = 100;
         } else if (scale < base * 0.17) {
             newDispBinSize = 600;
             newLoadBinSize = 100;
         } else if (scale < base * 0.25) {
             newDispBinSize = 400;
             newLoadBinSize = 100;
         } else if (scale < base * 0.5) {
             newDispBinSize = 200;
             newLoadBinSize = 100;
         }
 
         boolean showRefGene = true;
         if (scale < 0.0015) {
             showRefGene = false;
             geneChart.setVisible(false);
         } else {
             geneChart.setVisible(true);
         }
 
         // Calculate appropriate range.
         long newStart = leftValue  - (long)(700.0 / scale);
         long newEnd   = rightValue + (long)(700.0 / scale);
 
         if (leftValue < start + (long)(500.0 / scale) || end - (long)(500.0 / scale) < rightValue) {
             updateHistogram(newDispBinSize, newLoadBinSize, newStart, newEnd, true);
             updateBed(newStart, newEnd);
             if (showRefGene)
                 updateRefGene(newStart, newEnd);
 
             loadBinSize = newLoadBinSize;
             dispBinSize = newDispBinSize;
             start = newStart;
             end = newEnd;
         } else if (newLoadBinSize != loadBinSize) {
             updateHistogram(newDispBinSize, newLoadBinSize, newStart, newEnd, true);
 
             loadBinSize = newLoadBinSize;
             dispBinSize = newDispBinSize;
             start = newStart;
             end = newEnd;
         } else if (newDispBinSize != dispBinSize) {
             updateHistogram(newDispBinSize, newLoadBinSize, newStart, newEnd, false);
 
             loadBinSize = newLoadBinSize;
             dispBinSize = newDispBinSize;
             start = newStart;
             end = newEnd;
         }
     }
 
     private void updateHistogram(long newBinSize, long newLoadBinSize, long newStart, long newEnd, boolean loadDB) {
         for (Sam sam : selectedSamList) {
             SamHistogram sh = getLoadingSamHistogram(sam.getSamHistograms(), newLoadBinSize);
             if (sh == null) continue;
             HistogramUpdater updater = histogramUpdaterMap.get(sam.getSamId());
             HistogramChart chart = histogramChartMap.get(sam.getSamId());
             updater.setup(sh, selectedChromosome, newBinSize, newStart, newEnd, loadDB, chart, scale);
             updater.start();
         }
     }
 
     private SamHistogram getLoadingSamHistogram(SamHistogram[] samHistograms, long newLoadBinSize) {
         for (SamHistogram sh : samHistograms) {
             if (sh.getBinSize() == newLoadBinSize) {
                 return sh;
             }
         }
         return null;
     }
 
     private void updateBed(long newStart, long newEnd) {
         for (Bed bed : selectedBedList) {
             BedUpdater updater = bedUpdaterMap.get(bed.getBedId());
             BedChart chart = bedChartMap.get(bed.getBedId());
             updater.setup(bed, selectedChromosome, newStart, newEnd, chart, scale);
             updater.start();
         }
     }
 
     private void updateRefGene(long newStart, long newEnd) {
         geneUpdater.setup(selectedChromosome, newStart, newEnd, geneChart, scale);
         geneUpdater.start();
     }
 
     private void updateElements() {
         // Horizontal offset of scroll
         double offset = scroll.getX() * scale + getWidth() / 2.0;
 
         // Vertical offset
         baseObject.setY(scroll.getY() * 0.5);
 
         // Update ruler
         updateRuler(offset);
 
         // Update charts
         updateHistogramElements(offset);
         updateBedElements(offset);
         updateCytobandElements(offset);
         updateRefGeneElements(offset);
     }
 
     private void updateHistogramElements(double offset) {
         for (HistogramChart chart : histogramChartMap.values()) {
             for (HistogramBinElement e : chart.getHistogramBinElementList()) {
                 e.setScale(scale);
                 e.setPosition(offset + e.getBaseX(), e.getBaseY(), 0);
             }
         }
     }
 
     private void updateBedElements(double offset) {
         for (BedChart chart : bedChartMap.values()) {
             for (BedFragmentElement e : chart.getBedFragmentElementList()) {
                 e.setScale(scale);
                 e.setX(offset + e.getBaseX());
             }
         }
     }
 
     private void updateCytobandElements(double offset) {
         for (CytobandElement e : cytobandChart.getCytobandElementList()) {
             e.setScale(scale);
             e.setX(offset + e.getBaseX());
         }
     }
 
     private void updateRefGeneElements(double offset) {
         for (ExonElement e : geneChart.getExonElementList()) {
             e.setScale(scale);
             e.setPosition(offset + e.getBaseX(), e.getBaseY(), 0);
         }
 
         for (GeneElement e : geneChart.getGeneElementList()) {
             e.setScale(scale);
             e.setPosition(offset + e.getBaseX(), e.getBaseY(), 0);
         }
     }
 
     private void updateRuler(double offset) {
         long start = leftValue, end = rightValue;
         if (start < 0) start = 0;
         if (end < 0) end = 0;
         ruler.setStart(start);
         ruler.setEnd(end);
 
         Line mainLine = ruler.getMainLine();
         mainLine.setScale(scale);
         mainLine.setPosition(offset + (viewScale.getStart() + viewScale.getEnd()) / 2.0 * scale,
             ViewerConfig.getInstance().getRulerPosY(),
             0);
 
         long step = 100;
         if (scale < 0.000005) {
             step = 100000000;
         } else if (scale < 0.00005) {
             step = 10000000;
         } else if (scale < 0.0005) {
             step = 1000000;
         } else if (scale < 0.005) {
             step = 100000;
         } else if (scale < 0.05) {
             step = 10000;
         } else if (scale < 0.5) {
             step = 1000;
         }
 
         int idx = 0;
         for (long x = start - start % step; x < end; x += step) {
             if (ruler.getRulerElementList().size() <= idx) break;
 
             RulerElement e = ruler.getRulerElementList().get(idx);
 
             e.setInitialBaseX(x);
             e.getText().setText(Long.toString(x));
 
             e.setScale(scale);
             e.getLine().setPosition(offset + e.getBaseX(), ViewerConfig.getInstance().getRulerPosY(), 0);
             e.getText().setPosition(offset + e.getBaseX() + 3, ViewerConfig.getInstance().getRulerPosY() + 3, 0);
 
             e.getLine().setVisible(true);
             e.getText().setVisible(true);
 
             idx++;
         }
 
         for (int i = idx; i < ruler.getRulerElementList().size(); i++) {
             RulerElement e = ruler.getRulerElementList().get(i);
             e.getLine().setVisible(false);
             e.getText().setVisible(false);
         }
     }
 
     @Override
     public void keyEvent(KeyEvent e) {
         int keyCode = getKeyCode();
         switch (e) {
         case PRESSED:
             switch (keyCode) {
             case java.awt.event.KeyEvent.VK_ESCAPE:
                 // ESC to quit the application.
                 System.exit(0);
                 break;
             case java.awt.event.KeyEvent.VK_SPACE:
                 // Toggle scrolling and rotation mode
                 rotating = !rotating;
                 break;
             case java.awt.event.KeyEvent.VK_C:
                 // Show selection dialog box
                 dbox.setVisible(true);
                 break;
             case java.awt.event.KeyEvent.VK_R:
                 // Reset rotation
                 trackball.reset();
                 trackball.rotate(baseObject);
                 break;
             case java.awt.event.KeyEvent.VK_D:
                 // Toggle showing debug view
                 debugView.setVisible(!debugView.isVisible());
                 if (debugView.isVisible())
                     logger.info("Show DebugView");
                 else
                     logger.info("Hide DebugView");
                 break;
             default:
                 break;
             }
             break;
         default:
             break;
         }
     }
 
     @Override
     public void mouseEvent(MouseEvent e, MouseButton b) {
         switch (e) {
         case PRESSED:
            prevMouseX = getMouseX();
            prevMouseY = getMouseY();
             break;
 
         case RELEASED:
             if (chartManager.isDragging())
                 chartManager.releaseDragging();
             break;
 
         case DRAGGED:
             if (chartManager.isDragging()) break;
             switch (b) {
             case LEFT:
             {
                 if (rotating) {
                     int mouseX = getMouseX();
                     int mouseY = getMouseY();
 
                    trackball.update(mouseX, mouseY, prevMouseX, prevMouseY);
 
                     trackball.rotate(baseObject, getWidth() / 2.0, getHeight() / 2.0, 0.0);
 
                    prevMouseX = mouseX;
                    prevMouseY = mouseY;
                 } else {
                    double diffX = getMouseX() - getPrevMouseX();
                    double diffY = getMouseY() - getPrevMouseY();
                     scroll.setSpeedX(scroll.getSpeedX() + diffX * mouseScrollSpeedFactor);
                     scroll.setSpeedY(scroll.getSpeedY() + diffY * mouseScrollSpeedFactor);
                 }
 
                 break;
             }
 
             case RIGHT:
             {
                 int mouseX = getMouseX();
                 int mouseY = getMouseY();
 
                trackball.update(mouseX, mouseY, prevMouseX, prevMouseY);
 
                 trackball.rotate(baseObject, getWidth() / 2.0, getHeight() / 2.0, 0.0);
 
                prevMouseX = mouseX;
                prevMouseY = mouseY;
                 break;
             }
 
             default:
                 break;
             }
 
             break;
 
         case WHEEL_ROTATED:
         {
             double diff = getMouseWheelRotation();
             double scaleDiff = Math.abs(diff) * scale * wheelScaleFactor;
             scale += 0 < diff ? scaleDiff : -scaleDiff;
             if (scale < minScale) scale = minScale;
             else if (scale > maxScale) scale = maxScale;
             scaleController.setValue(scale);
             break;
         }
 
         default:
             break;
         }
     }
 
     @Override
     public void finishedSamSelection() {
         logger.debug("Finished selecting sam data.");
 
         dbox.setVisible(false);
 
         if (initializing)
             viewScale = new ViewScale(dbox.getChrName(), dbox.getStart(), dbox.getEnd());
         else
             viewScale.setViewScale(dbox.getChrName(), dbox.getStart(), dbox.getEnd());
 
         reloadViewData(viewScale.getChr(), viewScale.getStart(), viewScale.getEnd());
 
         synchronized (this.getListener()) {
             rebuildViewData(viewScale.getChr(), viewScale.getStart(), viewScale.getEnd());
             if (initializing) initializing = false;
         }
     }
 
     @Override
     public void finishedDataSelection(String jsonText) {
         Selection s = new JSON().decode(jsonText, Selection.class);
 
         for (Sam sam : samFiles) {
             sam.setSelected(s.hasBam(sam.getSamId()));
         }
 
         for (Bed bed : bedFiles) {
             bed.setSelected(s.hasBed(bed.getBedId()));
         }
 
         if (initializing)
             viewScale = new ViewScale(s.chromosome.name, s.chromosome.start, s.chromosome.end);
         else
             viewScale.setViewScale(s.chromosome.name, s.chromosome.start, s.chromosome.end);
 
         reloadViewData(s.chromosome.name, s.chromosome.start, s.chromosome.end);
 
         synchronized (this.getListener()) {
             rebuildViewData(s.chromosome.name, s.chromosome.start, s.chromosome.end);
             if (initializing) initializing = false;
         }
     }
 
     @Override
     public void exit() {
         sqlLoader.close();
         webSocket.close();
     }
 
     public static ChartManager getChartManager() {
         return chartManager;
     }
 
     public Indicator getIndicator() {
         return indicator;
     }
 
     public static void main(String[] args) {
         logger.info("Start NGSV");
 
         GeneView.args = args;
         AppletRunner.run("genome.view.GeneView", "NGSV: Next-Generation DNA Sequencing Viewer");
     }
 }
