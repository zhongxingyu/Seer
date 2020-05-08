 package edu.mit.wi.plink;
 
 import edu.mit.wi.haploview.Options;
 import edu.mit.wi.haploview.Constants;
 import edu.mit.wi.haploview.PlinkResultsPanel;
 import org.jfree.data.xy.XYSeriesCollection;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYDataset;
 import org.jfree.chart.*;
 import org.jfree.chart.labels.StandardXYToolTipGenerator;
 import org.jfree.chart.entity.EntityCollection;
 import org.jfree.chart.entity.XYItemEntity;
 import org.jfree.chart.entity.ChartEntity;
 import org.jfree.chart.axis.ValueAxis;
 import org.jfree.chart.renderer.xy.XYDotRenderer;
 import org.jfree.chart.renderer.xy.XYItemRendererState;
 import org.jfree.chart.plot.*;
 import org.jfree.ui.RefineryUtilities;
 import org.jfree.ui.RectangleEdge;
 import org.w3c.dom.DOMImplementation;
 import org.w3c.dom.Document;
 import org.apache.batik.svggen.SVGGraphics2D;
 import org.apache.batik.dom.GenericDOMImplementation;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.geom.Rectangle2D;
 import java.util.Hashtable;
 import java.util.Vector;
 import java.util.StringTokenizer;
 import java.io.*;
 
 
 public class PlinkGraph implements Constants, ChartMouseListener {
     private JTable table;
     private PlinkResultsPanel parent;
     private PlinkTableModel plinkTableModel;
 
     private Hashtable nonChrInfo;
     private Hashtable[] chrInfo;
     private int[] seriesKeys, thresholdSigns, thresholdAxes;
     private JFrame plotFrame;
     private int yPlotType, xPlotType, baseDotSize;
     private double suggestive, significant;
     private boolean threeSizes, chroms, useSig, useSug;
 
     public PlinkGraph(String title, int yType, int yCol, int xType, int xCol, double sug, double sig, boolean suggest, boolean signif, int[] signs, int[] thresholds, int dotSize, int colorKey, boolean grid, File svgFile, int width, int height, JTable theTable, PlinkTableModel theModel, PlinkResultsPanel theParent){
         yPlotType = yType;
         xPlotType = xType;
         thresholdSigns = signs;
         thresholdAxes = thresholds;
         baseDotSize = dotSize;
         useSug = suggest;
         useSig = signif;
         threeSizes = (signs[0] == signs[1]) && (thresholds[0] == thresholds[1]) && useSug && useSig;
         chroms = Options.getSNPBased() && xCol == 2;
         table = theTable;
         parent = theParent;
         plinkTableModel = theModel;
 
 
         final XYSeriesCollection dataSet;
         if (chroms){
             dataSet = makeChrDataSet(yCol);
         }else{
             dataSet = makeDataSet(yCol,xCol,colorKey);
         }
 
         if (dataSet == null){
             return;
         }
 
         significant = sig;
         suggestive = sug;
 
         String rangeAxisName;
         if (yCol == -1){
             rangeAxisName = null;
         }else{
             if (yPlotType == UNTRANSFORMED_PLOT){
                 rangeAxisName = table.getColumnName(yCol);
             }else{
                 rangeAxisName = PLOT_TYPES[yPlotType] + "(" + table.getColumnName(yCol) + ")";
             }
         }
 
         String domainAxisName;
         if (xCol == -1 || chroms){
             domainAxisName = null;
         }else{
             if (xPlotType == UNTRANSFORMED_PLOT){
                 domainAxisName = table.getColumnName(xCol);
             }else{
                 domainAxisName = PLOT_TYPES[xPlotType] + "(" + table.getColumnName(xCol) + ")";
             }
         }
 
         boolean legend = false;
         if (chroms || (colorKey != -1 && dataSet.getSeriesCount() > 1)){
             legend = true;
         }
 
         JFreeChart chart = ChartFactory.createScatterPlot(title,domainAxisName,rangeAxisName,dataSet, PlotOrientation.VERTICAL,legend,true,false);
 
         XYPlot thePlot = chart.getXYPlot();
         if (useSug){
             if (thresholds[0] == 0){
                 thePlot.addRangeMarker(new ValueMarker(sug,Color.blue,new BasicStroke()));
             }else{
                 thePlot.addDomainMarker(new ValueMarker(sug,Color.blue,new BasicStroke()));
             }
         }
         if (useSig){
             if (thresholds[1] == 0){
                 thePlot.addRangeMarker(new ValueMarker(sig,Color.red,new BasicStroke()));
             }else{
                 thePlot.addDomainMarker(new ValueMarker(sig,Color.red,new BasicStroke()));
             }
         }
         if (chroms){
             thePlot.setDomainGridlinesVisible(false);
             thePlot.getDomainAxis().setTickMarksVisible(false);
             thePlot.getDomainAxis().setTickLabelsVisible(false);
         }
 
         if (!grid){
             thePlot.setDomainGridlinesVisible(false);
             thePlot.setRangeGridlinesVisible(false);
         }
         thePlot.setRenderer(new PlinkScatterPlotRenderer());
         thePlot.getRenderer().setToolTipGenerator(new PlinkToolTipGenerator());
         chart.setAntiAlias(false);
 
         Shape[] shapes = new Shape[]{new Rectangle2D.Double(-2,-3,20,5)};
         //shapes[0] = new Rectangle2D.Double(-2,-3,20,5);
         Paint[] paints = new Paint[]{
                 new Color(0xFF, 0x55, 0x55),
                 new Color(0x55, 0x55, 0xFF),
                 //new Color(0x55, 0xFF, 0x55), //light green
                 //new Color(0xFF, 0xFF, 0x55), //light yellow
                 new Color(0xFF, 0x55, 0xFF),
                 //new Color(0x55, 0xFF, 0xFF), //light cyan
                 Color.pink,
                 Color.gray,
                 ChartColor.DARK_RED,
                 ChartColor.DARK_BLUE,
                 ChartColor.DARK_GREEN,
                 ChartColor.DARK_YELLOW,
                 ChartColor.DARK_MAGENTA,
                 ChartColor.DARK_CYAN,
                 Color.darkGray,
                 ChartColor.LIGHT_RED,
                 ChartColor.LIGHT_BLUE,
                 //ChartColor.LIGHT_GREEN,
                 //ChartColor.LIGHT_YELLOW,
                 ChartColor.LIGHT_MAGENTA,
                 //ChartColor.LIGHT_CYAN,
                 Color.lightGray,
                 ChartColor.VERY_DARK_RED,
                 ChartColor.VERY_DARK_BLUE,
                 ChartColor.VERY_DARK_GREEN,
                 ChartColor.VERY_DARK_YELLOW,
                 ChartColor.VERY_DARK_MAGENTA,
                 ChartColor.VERY_DARK_CYAN,
                 ChartColor.VERY_LIGHT_RED,
                 ChartColor.VERY_LIGHT_BLUE,
                 //ChartColor.VERY_LIGHT_GREEN,
                 //ChartColor.VERY_LIGHT_YELLOW,
                 ChartColor.VERY_LIGHT_MAGENTA,
                 //ChartColor.VERY_LIGHT_CYAN
         };
         DrawingSupplier supplier = new DefaultDrawingSupplier(
                 paints,
                 DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
                 DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
                 DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
                 shapes
         );
         thePlot.setDrawingSupplier(supplier);
 
         if (svgFile != null){
             DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
             Document document = domImpl.createDocument(null, "svg", null);
             SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
             svgGenerator.getGeneratorContext().setPrecision(6);
             chart.draw(svgGenerator, new Rectangle2D.Double(0, 0, width, height), null);
             boolean useCSS = true;
             try{
                 Writer out = new OutputStreamWriter(new FileOutputStream(svgFile), "UTF-8");
                 svgGenerator.stream(out, useCSS);
             }catch (IOException ioe){
                 JOptionPane.showMessageDialog(parent,
                         "Error saving svg file.",
                         "IO Error",
                         JOptionPane.ERROR_MESSAGE);
                 return;
             }
         }
 
         ChartPanel panel = new ChartPanel(chart, true);
         panel.setPreferredSize(new Dimension(width,height));
         panel.setMinimumDrawHeight(10);
         panel.setMaximumDrawHeight(2000);
         panel.setMinimumDrawWidth(20);
         panel.setMaximumDrawWidth(2000);
         panel.addChartMouseListener(this);
         plotFrame = new JFrame("Plot");
         plotFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         plotFrame.addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 parent.disposePlot();
             }
         });
         plotFrame.setContentPane(panel);
         plotFrame.pack();
         RefineryUtilities.centerFrameOnScreen(plotFrame);
         plotFrame.setVisible(true);
         plotFrame.requestFocus();
         plotFrame.toFront();
     }
 
     public XYSeriesCollection makeChrDataSet(int col){
         int numRows = table.getRowCount();
        long[] maxPositions = new long[26];
 
         for (int i = 0; i < numRows; i++){
             String chrom = (String)table.getValueAt(i,0);
             if (chrom.equals("")){
                 continue;
             }
             int chr;
             if (chrom.equalsIgnoreCase("X")){
                 chr = 23;
             }else if (chrom.equalsIgnoreCase("Y")){
                 chr = 24;
             }else if (chrom.equalsIgnoreCase("XY")){
                 chr = 25;
            }else if (chrom.equalsIgnoreCase("MT")){
                chr = 26;
             }else{
                 chr = Integer.parseInt(chrom);
             }
             if (chr < 1){
                 continue;
             }
             long position = ((Long)table.getValueAt(i,2)).longValue();
             if (position > maxPositions[chr-1]){
                 maxPositions[chr-1] = position;
             }
         }
 
        long[] addValues = new long[27];
         long addValue = 0;
         addValues[0] = 0;
 
        for (int i = 1; i < 27; i++){
             addValue += maxPositions[i-1];
             addValues[i] = addValue;
         }
 
        XYSeries[] xyArray = new XYSeries[27];
         for(int i = 1; i < 23; i++){
             xyArray[i] = new XYSeries("Chr" + i);
         }
         xyArray[23] = new XYSeries("ChrX");
         xyArray[24] = new XYSeries("ChrY");
         xyArray[25] = new XYSeries("ChrXY");
        xyArray[26] = new XYSeries("ChrMT");
 
        chrInfo = new Hashtable[26];
        for (int i = 0; i < 26; i++){
             chrInfo[i] = new Hashtable();
         }
 
         for (int i = 0; i < numRows; i++){
             String chrom = (String)table.getValueAt(i,0);
             if (chrom.equals("")){
                 continue;
             }
             int chr;
             if (chrom.equalsIgnoreCase("X")){
                 chr = 23;
             }else if (chrom.equalsIgnoreCase("Y")){
                 chr = 24;
             }else if (chrom.equalsIgnoreCase("XY")){
                 chr = 25;
            }else if (chrom.equalsIgnoreCase("MT")){
                chr = 26;
             }else{
                 chr = Integer.parseInt(chrom);
             }
             if (chr < 1){
                 continue;
             }
             double c = Double.parseDouble(String.valueOf(table.getValueAt(i,2)));
             c += addValues[chr-1];
             c = c/1000;
             double f = -1;
 
             if (table.getValueAt(i,col) == null){
                 continue;
             }else if ((table.getValueAt(i,col)).equals(new Double(Double.NaN))){
                 continue;
             }
             else{
                 if (table.getValueAt(i,col) instanceof Double){
                     f = ((Double)table.getValueAt(i,col)).doubleValue();
                 }else{
                     JOptionPane.showMessageDialog(parent,
                             "The selected column does not appear to be numerical.",
                             "Invalid column",
                             JOptionPane.ERROR_MESSAGE);
                 }
             }
 
             if (yPlotType == LOG10_PLOT){
                 if (f < 0 || f > 1){
                     JOptionPane.showMessageDialog(parent,
                             "The selected column is not formatted correctly \n" +
                                     "for a -log10 plot.",
                             "Invalid column",
                             JOptionPane.ERROR_MESSAGE);
                     return null;
                 }
                 f = (Math.log(f)/Math.log(10))*-1;
             }else if (yPlotType == LN_PLOT){
                 f = Math.log(f);
             }
             long kbPos = Long.parseLong(String.valueOf(table.getValueAt(i,2)))/1000;
             String infoString = table.getValueAt(i,1) + ", Chr" + chrom + ":" + kbPos + ", " + table.getValueAt(i,col);
             chrInfo[chr-1].put(new Double(c),infoString);
             xyArray[chr].add(c,f);
         }
 
         XYSeriesCollection dataset = new XYSeriesCollection();
 
        seriesKeys = new int[27];
         int seriesIndex = 0;
        for (int i = 1; i < 27; i++){
             if (xyArray[i].getItemCount() > 0){
                 seriesKeys[seriesIndex] = i;
                 seriesIndex++;
                 dataset.addSeries(xyArray[i]);
             }
         }
         return dataset;
     }
 
     public XYSeriesCollection makeDataSet(int yCol, int xCol, int colorCol){
         int numRows = table.getRowCount();
         XYSeries[] xyArray = new XYSeries[0];
         XYSeries xys = new XYSeries("Data");
         Hashtable colorKey = new Hashtable();
         if (colorCol != -1){
             Vector seriesNames = new Vector();
             for (int i = 0; i < numRows; i++){
                 if (table.getValueAt(i,colorCol) != null){
                     if (!colorKey.containsKey(table.getValueAt(i,colorCol))){
                         colorKey.put(table.getValueAt(i,colorCol),new Integer(seriesNames.size()));
                         if (table.getValueAt(i,colorCol) instanceof Double){
                             seriesNames.add(String.valueOf(table.getValueAt(i,colorCol)));
                         }else{
                             seriesNames.add(table.getValueAt(i,colorCol));
                         }
                         if (seriesNames.size() > 50){
                             JOptionPane.showMessageDialog(parent,
                                     "The selected color key column contains more than 50 values.",
                                     "Invalid column",
                                     JOptionPane.ERROR_MESSAGE);
                             colorKey = null;
                             break;
                         }
                     }
                 }
             }
             if (colorKey != null){
                 xyArray = new XYSeries[seriesNames.size()];
                 for (int i = 0; i < seriesNames.size(); i++){
                     xyArray[i] = new XYSeries((String)seriesNames.get(i));
                 }
             }
         }else{
             colorKey = null;
         }
         nonChrInfo = new Hashtable();
         for (int i = 0; i < numRows; i++){
 
             double y;
             if (yCol == -1){
                 y = i;
             }else{
                 if (table.getValueAt(i,yCol) == null){
                     continue;
                 }else if ((table.getValueAt(i,yCol)).equals(new Double(Double.NaN))){
                     continue;
                 }else{
                     if (table.getValueAt(i,yCol) instanceof Double){
                         y = ((Double)table.getValueAt(i,yCol)).doubleValue();
                     }else{
                         JOptionPane.showMessageDialog(parent,
                                 "The selected column does not appear to be numerical.",
                                 "Invalid column",
                                 JOptionPane.ERROR_MESSAGE);
                         return null;
                     }
                 }
 
                 if (yPlotType == LOG10_PLOT){
                     if (y < 0 || y > 1){
                         JOptionPane.showMessageDialog(parent,
                                 "The selected column is not formatted correctly \n" +
                                         "for a -log10 plot.",
                                 "Invalid column",
                                 JOptionPane.ERROR_MESSAGE);
                         return null;
                     }
                     y = (Math.log(y)/Math.log(10))*-1;
                 }else if (yPlotType == LN_PLOT){
                     y = Math.log(y);
                 }
             }
 
             double x;
             if (xCol == -1){
                 x = i;
             }else{
                 if (table.getValueAt(i,xCol) == null){
                     continue;
                 }else if ((table.getValueAt(i,xCol)).equals(new Double(Double.NaN))){
                     continue;
                 }else{
                     if (table.getValueAt(i,xCol) instanceof Double){
                         x = ((Double)table.getValueAt(i,xCol)).doubleValue();
                     }else{
                         JOptionPane.showMessageDialog(parent,
                                 "The selected column does not appear to be numerical.",
                                 "Invalid column",
                                 JOptionPane.ERROR_MESSAGE);
                         return null;
                     }
                 }
                 if (xPlotType == LOG10_PLOT){
                     if (x < 0 || x > 1){
                         JOptionPane.showMessageDialog(parent,
                                 "The selected column is not formatted correctly \n" +
                                         "for a -log10 plot.",
                                 "Invalid column",
                                 JOptionPane.ERROR_MESSAGE);
                         return null;
                     }
                     x = (Math.log(x)/Math.log(10))*-1;
                 }else if (xPlotType == LN_PLOT){
                     x = Math.log(x);
                 }
             }
 
             if (colorKey == null){
                 xys.add(x,y);
             }else{
                 int series = ((Integer)colorKey.get(table.getValueAt(i,colorCol))).intValue();
                 xyArray[series].add(x,y);
             }
 
             String key = String.valueOf(x) + " " +  String.valueOf(y);
             String value;
             if (Options.getSNPBased()){
                 value = table.getValueAt(i,1) + ", Chr" + table.getValueAt(i,0) + ":" + table.getValueAt(i,2);
                 nonChrInfo.put(key,value);
             }else{
                 if (plinkTableModel.getFIDColumn() != -1 && plinkTableModel.getIIDColumn() != -1){
                     value = "FID: " + table.getValueAt(i,plinkTableModel.getFIDColumn()) + ", IID: " + table.getValueAt(i,plinkTableModel.getIIDColumn());
                     nonChrInfo.put(key,value);
                 }else{
                     nonChrInfo.put(key,key);
                 }
             }
         }
 
         XYSeriesCollection dataset = new XYSeriesCollection();
         if (colorKey == null){
             dataset.addSeries(xys);
         }else{
             for (int i = 0; i < xyArray.length; i++){
                 dataset.addSeries(xyArray[i]);
             }
         }
 
         return dataset;
     }
 
     public void disposePlot(){
         if (plotFrame != null){
             plotFrame.dispose();
             plotFrame.setContentPane(new JPanel());
         }
         chrInfo = null;
         nonChrInfo = null;
         seriesKeys = null;
         thresholdSigns = null;
         thresholdAxes = null;
         plotFrame = null;
     }
 
     public void chartMouseClicked(ChartMouseEvent chartMouseEvent) {
         ChartEntity ce = chartMouseEvent.getEntity();
         if (ce != null && ce.getToolTipText() != null){
             if (Options.getSNPBased()){
                 StringTokenizer st = new StringTokenizer(ce.getToolTipText(),",");
                 parent.jumpToMarker(st.nextToken());
             }else{
                 if (plinkTableModel.getFIDColumn() != -1 && plinkTableModel.getIIDColumn() != -1){
                     StringTokenizer st = new StringTokenizer(ce.getToolTipText(),", ");
                     st.nextToken(); //FID:
                     String fid = st.nextToken();
                     st.nextToken(); //IID:
                     String iid = st.nextToken();
                     parent.jumpToNonSNP(fid,iid);
                 }
             }
         }
     }
 
     public void chartMouseMoved(ChartMouseEvent chartMouseEvent) {
     }
 
     class PlinkScatterPlotRenderer extends XYDotRenderer {
 
         public PlinkScatterPlotRenderer(){
             super();
         }
 
         public void drawItem(Graphics2D graphics2D, XYItemRendererState xyItemRendererState, Rectangle2D rectangle2D, PlotRenderingInfo plotRenderingInfo, XYPlot xyPlot, ValueAxis valueAxis, ValueAxis valueAxis1, XYDataset xyDataset, int series, int item, CrosshairState crosshairState, int pass) {
             EntityCollection entities = null;
             Shape entityArea = null;
             if (plotRenderingInfo != null) {
                 entities = plotRenderingInfo.getOwner().getEntityCollection();
             }
             double x = xyDataset.getXValue(series, item);
             double y = xyDataset.getYValue(series, item);
             if (!(new Double(y).equals(new Double(Double.NaN))) && !(new Double(x).equals(new Double(Double.NaN)))) {
                 RectangleEdge xAxisLocation = xyPlot.getDomainAxisEdge();
                 RectangleEdge yAxisLocation = xyPlot.getRangeAxisEdge();
                 double transX = valueAxis.valueToJava2D(x, rectangle2D, xAxisLocation);
                 double transY = valueAxis1.valueToJava2D(y, rectangle2D,yAxisLocation);
 
                 graphics2D.setPaint(this.getItemPaint(series, item));
                 int dotSize = baseDotSize;
                 PlotOrientation orientation = xyPlot.getOrientation();
                 if (orientation == PlotOrientation.HORIZONTAL){ //JFreeChart allows the user to flip the axes
                     transX = valueAxis1.valueToJava2D(y, rectangle2D,yAxisLocation);
                     transY = valueAxis.valueToJava2D(x, rectangle2D, xAxisLocation);
                 }
                 if (useSug || useSig){
                     if (threeSizes){ //this means that both signs must be equivalent and both suggestive & significant are not -1
                         if (thresholdAxes[0] == 0){ //both are for y axis
                             if (thresholdSigns[0] == 0){  //>
                                 if (y > suggestive && y <= significant){
                                     dotSize += 2;
                                 }else if (y > significant){
                                     dotSize += 4;
                                 }
                             }else{  //<
                                 if (y < suggestive && y >= significant){
                                     dotSize += 2;
                                 }else if (y < significant){
                                     dotSize += 4;
                                 }
                             }
                         }else{ //both are for x axis
                             if (thresholdSigns[0] == 0){  //>
                                 if (x > suggestive && x <= significant){
                                     dotSize += 2;
                                 }else if (x > significant){
                                     dotSize += 4;
                                 }
                             }else{  //<
                                 if (x < suggestive && x >= significant){
                                     dotSize += 2;
                                 }else if (x < significant){
                                     dotSize += 4;
                                 }
                             }
                         }
                     }else{
                         if (thresholdAxes[0] == 0 && thresholdAxes[1] == 0){ //both y
                             if (!useSig){  //only use suggestive
                                 if (thresholdSigns[0] == 0){  //>
                                     if (y > suggestive){
                                         dotSize += 2;
                                     }
                                 }else { //<
                                     if (y < suggestive){
                                         dotSize += 2;
                                     }
                                 }
                             }else if (!useSug){ //only use significant
                                 if (thresholdSigns[1] == 0){ //>
                                     if (y > significant){
                                         dotSize += 2;
                                     }
                                 }else { //<
                                     if (y < significant){
                                         dotSize += 2;
                                     }
                                 }
                             }else{ //use both
                                 if (thresholdSigns[0] == 0 && thresholdSigns[1] == 1){  //suggestive is >, significant is <
                                     if (y > suggestive || y < significant){
                                         dotSize += 2;
                                     }
                                 }else if (thresholdSigns[0] == 1 && thresholdSigns[1] == 0){ //suggestive is <, significant is >
                                     if (y < suggestive || y > significant){
                                         dotSize += 2;
                                     }
                                 }else if (thresholdSigns[0] == 0){ //both >
                                     if (y > suggestive || y > significant){
                                         dotSize += 2;
                                     }
                                 }else{  //both <
                                     if (y < suggestive || y < significant){
                                         dotSize += 2;
                                     }
                                 }
                             }
                         }else if (thresholdAxes[0] == 1 && thresholdAxes[1] == 1){ //both x
                             if (!useSig){  //only use suggestive
                                 if (thresholdSigns[0] == 0){  //>
                                     if (x > suggestive){
                                         dotSize += 2;
                                     }
                                 }else { //<
                                     if (x < suggestive){
                                         dotSize += 2;
                                     }
                                 }
                             }else if (!useSug){ //only use significant
                                 if (thresholdSigns[1] == 0){ //>
                                     if (x > significant){
                                         dotSize += 2;
                                     }
                                 }else { //<
                                     if (x < significant){
                                         dotSize += 2;
                                     }
                                 }
                             }else{ //use both
                                 if (thresholdSigns[0] == 0 && thresholdSigns[1] == 1){  //suggestive is >, significant is <
                                     if (x > suggestive || x < significant){
                                         dotSize += 2;
                                     }
                                 }else if (thresholdSigns[0] == 1 && thresholdSigns[1] == 0){ //suggestive is <, significant is >
                                     if (x < suggestive || x > significant){
                                         dotSize += 2;
                                     }
                                 }else if (thresholdSigns[0] == 0){ //both >
                                     if (x > suggestive || x > significant){
                                         dotSize += 2;
                                     }
                                 }else{  //both <
                                     if (x < suggestive || x < significant){
                                         dotSize += 2;
                                     }
                                 }
                             }
                         }else if (thresholdAxes[0] == 0 && thresholdAxes[1] == 1){ //sug y, sig x
                             if (!useSig){  //only use suggestive
                                 if (thresholdSigns[0] == 0){  //>
                                     if (y > suggestive){
                                         dotSize += 2;
                                     }
                                 }else { //<
                                     if (y < suggestive){
                                         dotSize += 2;
                                     }
                                 }
                             }else if (!useSug){ //only use significant
                                 if (thresholdSigns[1] == 0){ //>
                                     if (x > significant){
                                         dotSize += 2;
                                     }
                                 }else { //<
                                     if (x < significant){
                                         dotSize += 2;
                                     }
                                 }
                             }else{ //use both
                                 if (thresholdSigns[0] == 0 && thresholdSigns[1] == 1){  //suggestive is >, significant is <
                                     if (y > suggestive || x < significant){
                                         dotSize += 2;
                                     }
                                 }else if (thresholdSigns[0] == 1 && thresholdSigns[1] == 0){ //suggestive is <, significant is >
                                     if (y < suggestive || x > significant){
                                         dotSize += 2;
                                     }
                                 }else if (thresholdSigns[0] == 0){ //both >
                                     if (y > suggestive || x > significant){
                                         dotSize += 2;
                                     }
                                 }else{  //both <
                                     if (y < suggestive || x < significant){
                                         dotSize += 2;
                                     }
                                 }
                             }
                         }else{ //sug x, sig y
                             if (!useSig){  //only use suggestive
                                 if (thresholdSigns[0] == 0){  //>
                                     if (x > suggestive){
                                         dotSize += 2;
                                     }
                                 }else { //<
                                     if (x < suggestive){
                                         dotSize += 2;
                                     }
                                 }
                             }else if (!useSug){ //only use significant
                                 if (thresholdSigns[1] == 0){ //>
                                     if (y > significant){
                                         dotSize += 2;
                                     }
                                 }else { //<
                                     if (y < significant){
                                         dotSize += 2;
                                     }
                                 }
                             }else{ //use both
                                 if (thresholdSigns[0] == 0 && thresholdSigns[1] == 1){  //suggestive is >, significant is <
                                     if (x > suggestive || y < significant){
                                         dotSize += 2;
                                     }
                                 }else if (thresholdSigns[0] == 1 && thresholdSigns[1] == 0){ //suggestive is <, significant is >
                                     if (x < suggestive || y > significant){
                                         dotSize += 2;
                                     }
                                 }else if (thresholdSigns[0] == 0){ //both >
                                     if (x > suggestive || y > significant){
                                         dotSize += 2;
                                     }
                                 }else{  //both <
                                     if (x < suggestive || y < significant){
                                         dotSize += 2;
                                     }
                                 }
                             }
                         }
                     }
                 }
                 graphics2D.fillRect((int) transX, (int) transY, dotSize, dotSize);
 
                 // add an entity for the item...
                 if (entities != null) {
                     if (entityArea == null) {
                         entityArea = new Rectangle2D.Double(transX, transY, dotSize, dotSize);
                     }
                     String tip = "";
                     if (getToolTipGenerator(series,item) != null) {
                         tip = getToolTipGenerator(series,item).generateToolTip(xyDataset, series, item);
                     }
                     String url = null;
                     if (getURLGenerator() != null) {
                         url = getURLGenerator().generateURL(xyDataset, series, item);
                     }
                     XYItemEntity entity = new XYItemEntity(entityArea, xyDataset, series, item, tip, url);
                     entities.add(entity);
                 }
 
                 // do we need to update the crosshair values?
                 if (xyPlot.isDomainCrosshairLockedOnData()) {
                     if (xyPlot.isRangeCrosshairLockedOnData()) {
                         // both axes
                         crosshairState.updateCrosshairPoint(x, y, 0, 0, transX, transY, orientation); //added 0s to these methods to account for multiple axes which we don't use
                     }
                     else {
                         // just the horizontal axis...
                         crosshairState.updateCrosshairX(x,0);
                     }
                 }
                 else {
                     if (xyPlot.isRangeCrosshairLockedOnData()) {
                         // just the vertical axis...
                         crosshairState.updateCrosshairY(y,0);
                     }
                 }
             }
         }
     }
 
     class PlinkToolTipGenerator extends StandardXYToolTipGenerator {
 
         public PlinkToolTipGenerator(){
             super();
         }
 
         public String generateToolTip(XYDataset dataset, int series, int item){
             if (chroms){
                 return (String)chrInfo[seriesKeys[series]-1].get(new Double(dataset.getXValue(series,item)));
             }else{
                 return (String)nonChrInfo.get(String.valueOf(dataset.getXValue(series,item)) + " " + String.valueOf(dataset.getYValue(series,item)));
             }
         }
     }
 
 }
