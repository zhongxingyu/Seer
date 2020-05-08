 package edu.mit.wi.haploview;
 
 import edu.mit.wi.plink.PlinkTableModel;
 
 import javax.swing.*;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.border.TitledBorder;
 import java.util.Vector;
 import java.util.Hashtable;
 import java.util.StringTokenizer;
 import java.awt.*;
 import java.awt.geom.Rectangle2D;
 import java.awt.event.*;
 import java.io.File;
 import java.io.IOException;
 import java.io.FileWriter;
 import java.io.BufferedWriter;
 
 import org.jfree.data.xy.*;
 import org.jfree.chart.*;
 import org.jfree.chart.entity.EntityCollection;
 import org.jfree.chart.entity.XYItemEntity;
 import org.jfree.chart.entity.ChartEntity;
 import org.jfree.chart.labels.StandardXYToolTipGenerator;
 import org.jfree.chart.axis.ValueAxis;
 import org.jfree.chart.renderer.xy.XYItemRendererState;
 import org.jfree.chart.renderer.xy.XYDotRenderer;
 import org.jfree.chart.plot.*;
 import org.jfree.ui.RefineryUtilities;
 import org.jfree.ui.RectangleEdge;
 
 public class PlinkResultsPanel extends JPanel implements ActionListener, Constants, ChartMouseListener {
     private JTable table;
     private PlinkTableModel plinkTableModel;
     private TableSorter sorter;
 
     String[] chromNames = {"","1","2","3","4","5","6","7","8","9","10",
             "11","12","13","14","15","16","17","18","19","20","21","22","X","Y"};
     String[] signs = {"",">",">=","=","<=","<"};
     private JComboBox chromChooser, genericChooser, signChooser, removeChooser;
     private NumberTextField chromStart, chromEnd;
     private JTextField valueField, markerField;
     private JPanel filterPanel;
     private Vector originalColumns;
     private Hashtable removedColumns, nonChrInfo;
     private Hashtable[] chrInfo;
     private int[] seriesKeys, thresholdSigns, thresholdAxes;
 
     private JFrame plotFrame;
     private int yPlotType, xPlotType, baseDotSize;
     private double suggestive, significant;
     private boolean threeSizes, chroms, useSig, useSug;
 
     private String chosenMarker;
     private HaploView hv;
 
 
     public PlinkResultsPanel(HaploView h, Vector results, Vector colNames, boolean dups, Hashtable remove){
         hv = h;
 
         setLayout(new GridBagLayout());
 
         plinkTableModel = new PlinkTableModel(colNames,results);
         sorter = new TableSorter(plinkTableModel);
        removedColumns = new Hashtable();
         originalColumns = (Vector)plinkTableModel.getUnknownColumns().clone();
 
 
         table = new JTable(sorter);
         table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         table.setSelectionBackground(Color.lightGray);
         table.getTableHeader().setReorderingAllowed(false);
         //due to an old JTable bug this is necessary to activate a horizontal scrollbar
         if (table.getColumnCount() >= 20 && table.getRowCount() > 60){
             table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
         }
         //table.getModel().addTableModelListener(this);
 
         sorter.setTableHeader(table.getTableHeader());
         table.getColumnModel().getColumn(0).setPreferredWidth(60);
 
         if (Options.getSNPBased()){
             final PlinkCellRenderer renderer = new PlinkCellRenderer();
             try{
                 table.setDefaultRenderer(Class.forName("java.lang.Double"), renderer);
                 table.setDefaultRenderer(Class.forName("java.lang.Integer"), renderer);
                 table.setDefaultRenderer(Class.forName("java.lang.Long"), renderer);
                 table.setDefaultRenderer(Class.forName("java.lang.String"),renderer);
             }catch (Exception e){
             }
         }
 
         JScrollPane tableScroller = new JScrollPane(table);
 
 
         JPanel mainFilterPanel = new JPanel();
         JPanel extraFilterPanel = new JPanel();
         mainFilterPanel.setMinimumSize(new Dimension(700,40));
         if (Options.getSNPBased()){
             mainFilterPanel.add(new JLabel("Chromosome:"));
             chromChooser = new JComboBox(chromNames);
             mainFilterPanel.add(chromChooser);
             mainFilterPanel.add(new JLabel("Start kb:"));
             chromStart = new NumberTextField("",6,false, false);
             mainFilterPanel.add(chromStart);
             mainFilterPanel.add(new JLabel("End kb:"));
             chromEnd = new NumberTextField("",6,false, false);
             mainFilterPanel.add(chromEnd);
             extraFilterPanel.add(new JLabel("Goto Marker:"));
             markerField = new JTextField(8);
             extraFilterPanel.add(markerField);
             JButton doMarkerFilter = new JButton("Go");
             doMarkerFilter.setActionCommand("marker filter");
             doMarkerFilter.addActionListener(this);
             extraFilterPanel.add(doMarkerFilter);
         }
         mainFilterPanel.add(new JLabel("Other:"));
         genericChooser = new JComboBox(plinkTableModel.getUnknownColumns());
         genericChooser.setSelectedIndex(-1);
         mainFilterPanel.add(genericChooser);
         signChooser = new JComboBox(signs);
         signChooser.setSelectedIndex(-1);
         mainFilterPanel.add(signChooser);
         valueField = new JTextField(8);
         mainFilterPanel.add(valueField);
         JButton doFilter = new JButton("Filter");
         doFilter.addActionListener(this);
         mainFilterPanel.add(doFilter);
 
         extraFilterPanel.add(new JLabel("Remove Column:"));
         removeChooser = new JComboBox(plinkTableModel.getUnknownColumns());
         extraFilterPanel.add(removeChooser);
         JButton removeButton = new JButton("Remove");
         removeButton.addActionListener(this);
         extraFilterPanel.add(removeButton);
 
         JButton resetFilters = new JButton("Reset");
         resetFilters.addActionListener(this);
         JButton moreResults = new JButton("Load Additional Results");
         moreResults.addActionListener(this);
         if (dups){
             moreResults.setEnabled(false);
         }
         JButton fisherButton = new JButton("Combine P-Values");
         fisherButton.addActionListener(this);
 
         JButton plotButton = new JButton("Plot");
         plotButton.addActionListener(this);
 
         filterPanel = new JPanel(new GridBagLayout());
         GridBagConstraints a = new GridBagConstraints();
         filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),
                 "Viewing " + plinkTableModel.getRowCount() + " results"));
         ((TitledBorder)(filterPanel.getBorder())).setTitleColor(Color.black);
 
         a.gridwidth = 5;
         a.anchor = GridBagConstraints.CENTER;
         a.weightx = 1;
         filterPanel.add(mainFilterPanel,a);
         a.gridy = 1;
         filterPanel.add(extraFilterPanel,a);
         a.gridy = 2;
         a.gridwidth = 1;
         a.anchor = GridBagConstraints.SOUTHWEST;
         a.insets = new Insets(5,0,0,0);
         if (Options.getSNPBased()){
             filterPanel.add(moreResults,a);
             a.gridx = 1;
             filterPanel.add(fisherButton,a);
             a.gridx = 2;
             a.anchor = GridBagConstraints.SOUTH;
         }
         filterPanel.add(plotButton,a);
         a.gridx = 3;
         a.anchor = GridBagConstraints.SOUTHEAST;
         filterPanel.add(resetFilters,a);
 
 
         JPanel goPanel = new JPanel();
         JButton goButton = new JButton("<html><b>Go to Selected Region</b>");
         goButton.addActionListener(this);
         goButton.setActionCommand("Go to Selected Region");
         goPanel.add(goButton);
 
         GridBagConstraints c = new GridBagConstraints();
         c.fill = 1;
         c.weightx = 1;
         c.weighty = 1;
         c.insets = new Insets(10,50,10,50);
         add(tableScroller, c);
         c.weighty = 0;
         c.gridy = 1;
         add(filterPanel, c);
         if (Options.getSNPBased()){
             c.gridy = 2;
             add(goPanel,c);
         }
 
         if (remove != null){
             for (int i = 1; i < plinkTableModel.getUnknownColumns().size(); i++){
                 if (remove.containsKey(plinkTableModel.getUnknownColumns().get(i))){
                     removeColumn((String)plinkTableModel.getUnknownColumns().get(i),i);
                 }
             }
         }
     }
 
     public void jumpToMarker(String marker){
         for (int i = 0; i < table.getRowCount(); i++){
             String currMarker = (String) table.getValueAt(i,1);
             if (currMarker.equalsIgnoreCase(marker)){
                 table.changeSelection(i,1,false,false);
                 break;
             }
         }
     }
 
     public void jumpToNonSNP(String fid, String iid){
         if (plinkTableModel.getFIDColumn() != -1 && plinkTableModel.getIIDColumn() != -1){
             for (int i = 0; i < table.getRowCount(); i++){
                 String currentFid = (String)table.getValueAt(i,plinkTableModel.getFIDColumn());
                 String currentIid = (String)table.getValueAt(i,plinkTableModel.getIIDColumn());
                 if (currentFid.equals(fid) && currentIid.equals(iid)){
                     table.changeSelection(i,1,false,false);
                     break;
                 }
             }
         }
     }
 
     public void setChosenMarker(String chosenMarker) {
         this.chosenMarker = chosenMarker;
     }
 
     public String getChosenMarker() {
         return chosenMarker;
     }
 
     public Vector getSNPs(){
         return plinkTableModel.getSNPs();
     }
 
     public Object getValueAt(int row, int col){
         return plinkTableModel.getValueAt(row,col);
     }
 
     public void doFilters(){
         String chromChoice = "";
         int startPos = -1;
         int endPos = -1;
         if (Options.getSNPBased()){
             chromChoice = (String)chromChooser.getSelectedItem();
 
             if (chromStart.getText().equals("")){
                 startPos = -1;
             }else{
                 startPos = Integer.parseInt(chromStart.getText());
             }
 
             if (chromEnd.getText().equals("")){
                 endPos = -1;
             }else{
                 endPos = Integer.parseInt(chromEnd.getText());
             }
             if (startPos > endPos){
                 JOptionPane.showMessageDialog(this.getParent(),
                         "End position must be greater then start position.",
                         "Invalid value",
                         JOptionPane.ERROR_MESSAGE);
                 return;
             }
         }
 
         String columnChoice = null;
         String signChoice = null;
         String value = null;
 
         if (genericChooser.getSelectedIndex() > 0){
             columnChoice = (String)genericChooser.getSelectedItem();
 
             if (signChooser.getSelectedIndex() > 0){
                 signChoice = (String)signChooser.getSelectedItem();
 
                 if (!(valueField.getText().equals(""))){
                     value = valueField.getText();
                 }
             }
         }
 
         reSort();
         plinkTableModel.filterAll(chromChoice,startPos,endPos,columnChoice,signChoice,value);
         countResults();
     }
 
     public void removeColumn(String col, int index){
         TableColumn deletedColumn = table.getColumn(col);
         removedColumns.put(col,deletedColumn);
         table.removeColumn(deletedColumn);
         removeChooser.removeItemAt(index);
         removeChooser.setSelectedIndex(removeChooser.getItemCount()-1);
         genericChooser.setSelectedIndex(0);
         if (table.getColumnCount() < 20){
             table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
         }
         repaint();
     }
 
     public void clearFilters(){
         if (removedColumns.size() > 0){
             for (int i = 1; i < removeChooser.getItemCount(); i++){
                 TableColumn deletedColumn = table.getColumn(removeChooser.getItemAt(i));
                 removedColumns.put(removeChooser.getItemAt(i),deletedColumn);
                 table.removeColumn(deletedColumn);
             }
 
             removeChooser.removeAllItems();
             removeChooser.addItem("");
 
             for (int i = 1; i < originalColumns.size(); i++){
                 TableColumn addedColumn = (TableColumn)removedColumns.get(originalColumns.get(i));
                 table.addColumn(addedColumn);
                 removeChooser.addItem(originalColumns.get(i));
             }
             removedColumns.clear();
         }
 
         if (table.getColumnCount() >= 20 && table.getRowCount() > 60){
             table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
         }
 
         plinkTableModel.resetFilters();
         if (Options.getSNPBased()){
             chromChooser.setSelectedIndex(0);
             chromStart.setText("");
             chromEnd.setText("");
             markerField.setText("");
         }
         genericChooser.setSelectedIndex(0);
         genericChooser.updateUI();
         signChooser.setSelectedIndex(0);
         valueField.setText("");
         reSort();
         countResults();
     }
 
     public void reSort(){
         for (int i = 0; i < table.getColumnCount(); i++){
             sorter.setSortingStatus(i,sorter.getSortingStatus(i));
         }
     }
 
     public void countResults(){
         filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),
                 "Viewing " + plinkTableModel.getRowCount() + " results"));
         repaint();
     }
 
     public XYSeriesCollection makeChrDataSet(int col){
         int numRows = table.getRowCount();
         long[] maxPositions = new long[25];
 
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
 
         long[] addValues = new long[25];
         long addValue = 0;
         addValues[0] = 0;
 
         for (int i = 1; i < 25; i++){
             addValue += maxPositions[i-1];
             addValues[i] = addValue;
         }
 
         XYSeries[] xyArray = new XYSeries[26];
         for(int i = 1; i < 23; i++){
             xyArray[i] = new XYSeries("Chr" + i);
         }
         xyArray[23] = new XYSeries("ChrX");
         xyArray[24] = new XYSeries("ChrY");
         xyArray[25] = new XYSeries("ChrXY");
 
         chrInfo = new Hashtable[25];
         for (int i = 0; i < 24; i++){
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
                     JOptionPane.showMessageDialog(this,
                             "The selected column does not appear to be numerical.",
                             "Invalid column",
                             JOptionPane.ERROR_MESSAGE);
                 }
             }
 
             if (yPlotType == LOG10_PLOT){
                 if (f < 0 || f > 1){
                     JOptionPane.showMessageDialog(this,
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
 
         seriesKeys = new int[26];
         int seriesIndex = 0;
         for (int i = 1; i < 25; i++){
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
                         if (seriesNames.size() > 20){
                             JOptionPane.showMessageDialog(this,
                                     "The selected color key column contains more than 20 values.",
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
                         JOptionPane.showMessageDialog(this,
                                 "The selected column does not appear to be numerical.",
                                 "Invalid column",
                                 JOptionPane.ERROR_MESSAGE);
                         return null;
                     }
                 }
 
                 if (yPlotType == LOG10_PLOT){
                     if (y < 0 || y > 1){
                         JOptionPane.showMessageDialog(this,
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
                         JOptionPane.showMessageDialog(this,
                                 "The selected column does not appear to be numerical.",
                                 "Invalid column",
                                 JOptionPane.ERROR_MESSAGE);
                         return null;
                     }
                 }
                 if (xPlotType == LOG10_PLOT){
                     if (x < 0 || x > 1){
                         JOptionPane.showMessageDialog(this,
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
 
     public void makeChart(String title, int yType, int yCol, int xType, int xCol, double sug, double sig, int[] signs, int[] thresholds, int dotSize, int colorKey){
         hv.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         yPlotType = yType;
         xPlotType = xType;
         thresholdSigns = signs;
         thresholdAxes = thresholds;
         baseDotSize = dotSize;
         threeSizes = (signs[0] == signs[1]) && (thresholds[0] == thresholds[1]) && useSug && useSig;
         chroms = Options.getSNPBased() && xCol == 2;
 
         XYSeriesCollection dataSet = null;
         if (chroms){
             dataSet = makeChrDataSet(yCol);
         }else{
             dataSet = makeDataSet(yCol,xCol,colorKey);
         }
 
         if (dataSet == null){
             hv.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
 
         JFreeChart chart = ChartFactory.createScatterPlot(title,domainAxisName,rangeAxisName,dataSet,PlotOrientation.VERTICAL,legend,true,false);
 
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
         thePlot.setRenderer(new PlinkScatterPlotRenderer());
         thePlot.getRenderer().setToolTipGenerator(new PlinkToolTipGenerator());
         chart.setAntiAlias(false);
 
         Shape[] shapes = new Shape[1];
         shapes[0] = new Rectangle2D.Double(-2,-3,20,5);
         DrawingSupplier supplier = new DefaultDrawingSupplier(
                 DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE,
                 DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
                 DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
                 DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
                 shapes
         );
         thePlot.setDrawingSupplier(supplier);
 
         ChartPanel panel = new ChartPanel(chart, true);
         panel.setPreferredSize(new Dimension(750,300));
         panel.setMinimumDrawHeight(10);
         panel.setMaximumDrawHeight(2000);
         panel.setMinimumDrawWidth(20);
         panel.setMaximumDrawWidth(2000);
         panel.addChartMouseListener(this);
         plotFrame = new JFrame("Plot");
         plotFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         plotFrame.addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 chrInfo = null;
                 nonChrInfo = null;
                 seriesKeys = null;
                 thresholdSigns = null;
                 thresholdAxes = null;
                 plotFrame = null;
             }
         });
         plotFrame.setContentPane(panel);
         plotFrame.pack();
         RefineryUtilities.centerFrameOnScreen(plotFrame);
         plotFrame.setVisible(true);
         plotFrame.requestFocus();
         plotFrame.toFront();
         hv.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
     }
 
     public void gotoRegion(){
         if (table.getSelectedRow() == -1){
             JOptionPane.showMessageDialog(this,
                     "Please select a region.",
                     "Invalid value",
                     JOptionPane.ERROR_MESSAGE);
             return;
         }
         String gotoChrom = (String)table.getValueAt(table.getSelectedRow(),0);
         String gotoMarker = (String)table.getValueAt(table.getSelectedRow(),1);
         long markerPosition = ((Long)(table.getValueAt(table.getSelectedRow(),2))).longValue();
 
         RegionDialog rd = new RegionDialog(hv,gotoChrom,gotoMarker,
                 this,markerPosition,"Go to Region");
         rd.pack();
         rd.setVisible(true);
     }
 
     public Vector getUnknownColumns(){
         return plinkTableModel.getUnknownColumns();
     }
 
     public void exportTable(File outfile) throws IOException, HaploViewException{
         BufferedWriter plinkWriter = new BufferedWriter(new FileWriter(outfile));
         for (int i = 0; i < table.getColumnCount(); i++){
             if (table.getColumnName(i).equalsIgnoreCase("CHROM")){
                 plinkWriter.write("CHR"+"\t");
             }else if (table.getColumnName(i).equalsIgnoreCase("MARKER")){
                 plinkWriter.write("SNP"+"\t");
             }else{
                 plinkWriter.write(table.getColumnName(i)+"\t");
             }
         }
         plinkWriter.newLine();
 
         for (int i = 0; i < table.getRowCount(); i++){
             for (int j = 0; j < table.getColumnCount(); j++){
                 if (table.getValueAt(i,j) == null){
                     plinkWriter.write("-"+"\t");
                 }else{
                     plinkWriter.write(table.getValueAt(i,j)+"\t");
                 }
             }
             plinkWriter.newLine();
         }
         plinkWriter.close();
     }
 
     public void disposePlot(){
         if (plotFrame != null){
             plotFrame.dispose();
         }
         chrInfo = null;
         nonChrInfo = null;
         seriesKeys = null;
         thresholdSigns = null;
         thresholdAxes = null;
         plotFrame = null;
     }
 
     public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();
         if (command.equals("Filter")){
             doFilters();
         }else if (command.equals("marker filter")){
             String marker = markerField.getText();
             if (!(marker.equals(""))){
                 jumpToMarker(marker);
             }
         }else if (command.equals("Reset")){
             clearFilters();
         }else if (command.equals("Load Additional Results")){
             HaploView.fc.setSelectedFile(new File(""));
             int returned = HaploView.fc.showOpenDialog(this);
             if (returned != JFileChooser.APPROVE_OPTION) return;
             File file = HaploView.fc.getSelectedFile();
             String fullName = file.getParent()+File.separator+file.getName();
             String[] inputs = {null,null,fullName,null,null,null,null};
             if (removedColumns.size() > 0){
                 hv.setRemovedColumns(removedColumns);
             }
             hv.readWGA(inputs);
         }else if (command.equals("Combine P-Values")){
             FisherCombinedDialog fcd = new FisherCombinedDialog("Fisher Combine");
             fcd.pack();
             fcd.setVisible(true);
         }else if (command.equals("Remove")){
             if (removeChooser.getSelectedIndex() > 0){
                 removeColumn((String)removeChooser.getSelectedItem(),removeChooser.getSelectedIndex());
             }
         }else if (command.equals("Plot")){
             PlotOptionDialog pod = new PlotOptionDialog("Plot Options");
             pod.pack();
             pod.setVisible(true);
         }else if (command.equals("Go to Selected Region")){
             gotoRegion();
         }
     }
 
     public void chartMouseClicked(ChartMouseEvent chartMouseEvent) {
         ChartEntity ce = chartMouseEvent.getEntity();
         if (ce != null && ce.getToolTipText() != null){
             if (Options.getSNPBased()){
                 StringTokenizer st = new StringTokenizer(ce.getToolTipText(),",");
                 jumpToMarker(st.nextToken());
                 hv.requestFocus();
                 hv.toFront();
             }else{
                 if (plinkTableModel.getFIDColumn() != -1 && plinkTableModel.getIIDColumn() != -1){
                     StringTokenizer st = new StringTokenizer(ce.getToolTipText(),", ");
                     st.nextToken(); //FID:
                     String fid = st.nextToken();
                     st.nextToken(); //IID:
                     String iid = st.nextToken();
                     jumpToNonSNP(fid,iid);
                     hv.requestFocus();
                     hv.toFront();
                 }
             }
         }
     }
 
     public void chartMouseMoved(ChartMouseEvent chartMouseEvent) {
     }
 
     class PlotOptionDialog extends JDialog implements ActionListener {
         private JComboBox yColumnChooser, xColumnChooser, yPlotChooser, xPlotChooser, signChooser1, signChooser2, thresholdChooser1, thresholdChooser2, dotChooser, colorKeyChooser;
         private JLabel label1, label2;
         private NumberTextField sigThresh, sugThresh;
         private JTextField titleField;
         private String[] signs = {">","<"};
         private String[] thresholds = {"Y-Axis","X-Axis"};
         private String[] dotSizes = {"Small","Medium","Large"};
 
         public PlotOptionDialog (String title) {
             super(hv,title);
 
             Vector columns = new Vector(plinkTableModel.getUnknownColumns());
             columns.add("Index");
 
             Vector xCols = new Vector();
             if (Options.getSNPBased()){
                 xCols.add("Chromosomes");
                 for (int i = 1; i < columns.size(); i++){
                     xCols.add(columns.get(i));
                 }
             }else{
                 for (int i = 0; i < columns.size(); i++){
                     xCols.add(columns.get(i));
                 }
             }
 
             JPanel contents = new JPanel();
             contents.setLayout(new BoxLayout(contents,BoxLayout.Y_AXIS));  //TODO: GridBag
 
             JPanel titlePanel = new JPanel();
             titlePanel.add(new JLabel("Title:"));
             titleField = new JTextField(20);
             titlePanel.add(titleField);
             JPanel xPanel = new JPanel();
             xPanel.add(new JLabel("X-Axis:"));
             xColumnChooser = new JComboBox(xCols);
             xColumnChooser.addActionListener(this);
             xPanel.add(xColumnChooser);
             xPanel.add(new JLabel("Scale:"));
             xPlotChooser = new JComboBox(PLOT_TYPES);
             xPanel.add(xPlotChooser);
             JPanel yPanel = new JPanel();
             yPanel.add(new JLabel("Y-Axis:"));
             yColumnChooser = new JComboBox(columns);
             yColumnChooser.addActionListener(this);
             yPanel.add(yColumnChooser);
             yPanel.add(new JLabel("Scale:"));
             yPlotChooser = new JComboBox(PLOT_TYPES);
             yPlotChooser.addActionListener(this);
             yPanel.add(yPlotChooser);
             JPanel sugPanel = new JPanel();
             thresholdChooser1 = new JComboBox(thresholds);
             sugPanel.add(thresholdChooser1);
             label1 = new JLabel("Threshold 1 (Blue Line)");
             sugPanel.add(label1);
             signChooser1 = new JComboBox(signs);
             sugPanel.add(signChooser1);
             sugThresh = new NumberTextField("",6,true,true);
             sugPanel.add(sugThresh);
             JPanel sigPanel = new JPanel();
             thresholdChooser2 = new JComboBox(thresholds);
             sigPanel.add(thresholdChooser2);
             label2 = new JLabel("Threshold 2 (Red Line) ");
             sigPanel.add(label2);
             signChooser2 = new JComboBox(signs);
             sigPanel.add(signChooser2);
             sigThresh = new NumberTextField("",6,true,true);
             sigPanel.add(sigThresh);
             JPanel dotPanel = new JPanel();
             dotPanel.add(new JLabel("Data Point Size:"));
             dotChooser = new JComboBox(dotSizes);
             dotChooser.setSelectedIndex(1);
             dotPanel.add(dotChooser);
             dotPanel.add(new JLabel("Color Key:"));
             colorKeyChooser = new JComboBox(plinkTableModel.getUnknownColumns());
             dotPanel.add(colorKeyChooser);
 
             if (Options.getSNPBased()){
                 yColumnChooser.setSize(xColumnChooser.getSize());
                 xPlotChooser.setEnabled(false);
                 thresholdChooser1.setEnabled(false);
                 thresholdChooser2.setEnabled(false);
                 colorKeyChooser.setEnabled(false);
             }
 
             JPanel choicePanel = new JPanel();
             JButton okButton = new JButton("OK");
             okButton.addActionListener(this);
             this.getRootPane().setDefaultButton(okButton);
             choicePanel.add(okButton);
             JButton cancelButton = new JButton("Cancel");
             cancelButton.addActionListener(this);
             choicePanel.add(cancelButton);
 
             contents.add(titlePanel);
             contents.add(xPanel);
             contents.add(yPanel);
             contents.add(sugPanel);
             contents.add(sigPanel);
             contents.add(dotPanel);
             contents.add(choicePanel);
             setContentPane(contents);
 
             this.setLocation(this.getParent().getX() + 100,
                     this.getParent().getY() + 100);
             this.setModal(true);
             this.setResizable(false);
         }
 
         public void actionPerformed(ActionEvent e) {
             String command = e.getActionCommand();
             if(command.equals("Cancel")) {
                 this.dispose();
             }else if (command.equals("OK")){
                 if (xColumnChooser.getSelectedIndex() == 0 && !Options.getSNPBased()){
                     JOptionPane.showMessageDialog(this,
                             "Please select a column to plot on the X-Axis.",
                             "Invalid value",
                             JOptionPane.ERROR_MESSAGE);
                     return;
                 }
                 if (yColumnChooser.getSelectedIndex() == 0){
                     JOptionPane.showMessageDialog(this,
                             "Please select a column to plot on the Y-Axis.",
                             "Invalid value",
                             JOptionPane.ERROR_MESSAGE);
                     return;
                 }
                 int yColumn = yColumnChooser.getSelectedIndex()-1; //accounts for ""
                 int yPlotType = yPlotChooser.getSelectedIndex();
                 int xColumn = xColumnChooser.getSelectedIndex();
                 int xPlotType = xPlotChooser.getSelectedIndex();
                 int colorColumn = colorKeyChooser.getSelectedIndex()-1;
                 if (Options.getSNPBased()){
                     yColumn += 3; //accounts for 3 known columns (chrom,marker,position)
                     colorColumn += 3;
                     xColumn += 2;
                 }else{
                     xColumn -= 1;
                     if (plinkTableModel.getFIDColumn() != -1){
                         yColumn += 1;
                         xColumn += 1;
                         colorColumn += 1;
                     }
                     if (plinkTableModel.getIIDColumn() != -1){
                         yColumn += 1;
                         xColumn += 1;
                         colorColumn += 1;
                     }
                 }
                 if ((xColumnChooser.getSelectedItem().equals("Index") && yColumnChooser.getSelectedItem().equals("Index")) ||
                         (xColumnChooser.getSelectedItem().equals("Chromosomes") && yColumnChooser.getSelectedItem().equals("Index"))){
                     JOptionPane.showMessageDialog(this,
                             "You must have at least one explicit axis.",
                             "Invalid value",
                             JOptionPane.ERROR_MESSAGE);
                     return;
                 }else{
                     if (xColumnChooser.getSelectedItem().equals("Index")){
                         xColumn = -1;
                     }else if (yColumnChooser.getSelectedItem().equals("Index")){
                         yColumn = -1;
                     }
                 }
                 if (colorKeyChooser.getSelectedIndex() < 1){
                     colorColumn = -1;
                 }
 
                 double suggestive, significant;
                 try{
                     if (sugThresh.getText().equals("")){
                         useSug = false;
                         suggestive = -1;
                     }else{
                         suggestive = Double.parseDouble(sugThresh.getText());
                         useSug = true;
                     }
 
                     if (sigThresh.getText().equals("")){
                         useSig = false;
                         significant = -1;
                     }else{
                         significant = Double.parseDouble(sigThresh.getText());
                         useSig = true;
                     }
                 }catch(NumberFormatException nfe){
                     JOptionPane.showMessageDialog(this,
                             "Thresholds must be numerical.",
                             "Invalid value",
                             JOptionPane.ERROR_MESSAGE);
                     return;
                 }
 
                 int[] signs = new int[2];
                 signs[0] = signChooser1.getSelectedIndex();
                 signs[1] = signChooser2.getSelectedIndex();
                 int[] thresholds = new int[2];
                 thresholds[0] = thresholdChooser1.getSelectedIndex();
                 thresholds[1] = thresholdChooser2.getSelectedIndex();
                 int dotSize = 2 + (dotChooser.getSelectedIndex()*2);
                 if (plotFrame != null){
                     plotFrame.dispose();
                 }
                 this.dispose();
                 makeChart(titleField.getText(),yPlotType,yColumn,xPlotType,xColumn,suggestive,significant,signs,thresholds,dotSize,colorColumn);
             }else if (e.getSource() instanceof JComboBox){
                 if (xColumnChooser.getSelectedItem().equals("Chromosomes")){
                     xPlotChooser.setSelectedIndex(0);
                     xPlotChooser.setEnabled(false);
                     thresholdChooser1.setSelectedIndex(0);
                     thresholdChooser1.setEnabled(false);
                     thresholdChooser2.setSelectedIndex(0);
                     thresholdChooser2.setEnabled(false);
                     colorKeyChooser.setSelectedIndex(0);
                     colorKeyChooser.setEnabled(false);
                 }else if (xColumnChooser.getSelectedItem().equals("Index")){
                     xPlotChooser.setSelectedIndex(0);
                     xPlotChooser.setEnabled(false);
                     thresholdChooser1.setEnabled(true);
                     thresholdChooser2.setEnabled(true);
                     colorKeyChooser.setEnabled(true);
                 }else{
                     xPlotChooser.setEnabled(true);
                     thresholdChooser1.setEnabled(true);
                     thresholdChooser2.setEnabled(true);
                     colorKeyChooser.setEnabled(true);
                 }
 
                 if (yColumnChooser.getSelectedItem().equals("Index")){
                     yPlotChooser.setSelectedIndex(0);
                     yPlotChooser.setEnabled(false);
                 }else{
                     yPlotChooser.setEnabled(true);
                 }
                 if (yPlotChooser.getSelectedItem().equals("-log10")){
                     label1.setText("Suggestive (Blue Line)");
                     label2.setText("Significant (Red Line) ");
                 }else{
                     label1.setText("Threshold 1 (Blue Line)");
                     label2.setText("Threshold 2 (Red Line) ");
                 }
             }
         }
     }
 
     class FisherCombinedDialog extends JDialog implements ActionListener{
         private JComboBox pval1, pval2, pval3, pval4, pval5;
         private Vector columnIndeces;
         FisherCombinedDialog(String title){
             super(hv,title);
 
             JPanel contents = new JPanel();
             contents.setLayout(new BoxLayout(contents,BoxLayout.Y_AXIS));
             contents.setPreferredSize(new Dimension(150,200));
 
            if (removedColumns != null){
                if (removedColumns.size() > 0){
                    clearFilters();
                }
            }

             Vector doubleColumns = new Vector();
             columnIndeces = new Vector();
             doubleColumns.add("");
             for (int i = 1; i < plinkTableModel.getUnknownColumns().size(); i++){
                 for (int j = 0; j < 50; j++){
                     if (table.getValueAt(j,i+2) != null){
                         if (table.getValueAt(j,i+2) instanceof Double){
                             doubleColumns.add(plinkTableModel.getUnknownColumns().get(i));
                             columnIndeces.add(new Integer(i-1));
                         }
                         break;
                     }else if (j == 49){
                         doubleColumns.add(plinkTableModel.getUnknownColumns().get(i));
                         columnIndeces.add(new Integer(i-1));
                     }
                 }
             }
 
             JPanel pval1Panel = new JPanel();
             pval1Panel.add(new JLabel("Pval 1:"));
             pval1 = new JComboBox(doubleColumns);
             pval1Panel.add(pval1);
             JPanel pval2Panel = new JPanel();
             pval2Panel.add(new JLabel("Pval 2:"));
             pval2 = new JComboBox(doubleColumns);
             pval2Panel.add(pval2);
             JPanel pval3Panel = new JPanel();
             pval3Panel.add(new JLabel("Pval 3:"));
             pval3 = new JComboBox(doubleColumns);
             pval3Panel.add(pval3);
             JPanel pval4Panel = new JPanel();
             pval4Panel.add(new JLabel("Pval 4:"));
             pval4 = new JComboBox(doubleColumns);
             pval4Panel.add(pval4);
             JPanel pval5Panel = new JPanel();
             pval5Panel.add(new JLabel("Pval 5:"));
             pval5 = new JComboBox(doubleColumns);
             pval5Panel.add(pval5);
 
             JPanel choicePanel = new JPanel();
             JButton goButton = new JButton("Go");
             goButton.addActionListener(this);
             choicePanel.add(goButton);
             JButton cancelButton = new JButton("Cancel");
             cancelButton.addActionListener(this);
             choicePanel.add(cancelButton);
 
             contents.add(pval1Panel);
             contents.add(pval2Panel);
             contents.add(pval3Panel);
             contents.add(pval4Panel);
             contents.add(pval5Panel);
             contents.add(choicePanel);
 
             setContentPane(contents);
             this.setLocation(this.getParent().getX() + 100,
                     this.getParent().getY() + 100);
             this.setModal(true);
             this.getRootPane().setDefaultButton(goButton);
         }
 
         public void actionPerformed(ActionEvent e) {
             String command = e.getActionCommand();
 
             if (command.equals("Cancel")){
                 this.dispose();
             }else if (command.equals("Go")){
                 int[] pCols = new int[5];
                 int numPvals = 0;
 
                 if (pval1.getSelectedIndex() > 0){
                     numPvals++;
                     pCols[0] = ((Integer)columnIndeces.get(pval1.getSelectedIndex()-1)).intValue();
                 }
                 if (pval2.getSelectedIndex() > 0){
                     numPvals++;
                     pCols[1] = ((Integer)columnIndeces.get(pval2.getSelectedIndex()-1)).intValue();
                 }
                 if (pval3.getSelectedIndex() > 0){
                     numPvals++;
                     pCols[2] = ((Integer)columnIndeces.get(pval3.getSelectedIndex()-1)).intValue();
                 }
                 if (pval4.getSelectedIndex() > 0){
                     numPvals++;
                     pCols[3] = ((Integer)columnIndeces.get(pval4.getSelectedIndex()-1)).intValue();
                 }
                 if (pval5.getSelectedIndex() > 0){
                     numPvals++;
                     pCols[4] = ((Integer)columnIndeces.get(pval5.getSelectedIndex()-1)).intValue();
                 }
 
                 if (numPvals < 2){
                     JOptionPane.showMessageDialog(this,
                             "Please choose at least 2 pvalue columns.",
                             "Invalid selection",
                             JOptionPane.ERROR_MESSAGE);
                     return;
                 }
 
                 String cols = "";
                 for (int i = 0; i < pCols.length; i++){
                     if (pCols[i] > 0){
                         cols = cols + pCols[i] + " ";
                     }
                 }
 
                 String[] inputs = {null,null,null,null,cols,null,null};
                 this.dispose();
                 hv.readWGA(inputs);
             }
         }
     }
 
     class PlinkCellRenderer extends DefaultTableCellRenderer {
         public Component getTableCellRendererComponent
                 (JTable table, Object value, boolean isSelected,
                  boolean hasFocus, int row, int column)
         {
             Component cell = super.getTableCellRendererComponent
                     (table, value, isSelected, hasFocus, row, column);
             String thisMarker = (String)table.getValueAt(row,1);
             cell.setForeground(Color.black);
             cell.setBackground(Color.white);
 
             if (isSelected){
                 cell.setBackground(table.getSelectionBackground());
             }else{
                 cell.setBackground(table.getBackground());
             }
 
             if (thisMarker.equals(hv.getChosenMarker())){
                 cell.setBackground(Color.cyan);
             }
 
             if (table.getColumnName(column).startsWith("P_COMBINED")){
                 if (((Double)table.getValueAt(row,column)).doubleValue() == 1.0E-16){
                     cell.setForeground(Color.red);
                 }
             }
             return cell;
         }
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
                         crosshairState.updateCrosshairPoint(x, y, transX, transY, orientation);
                     }
                     else {
                         // just the horizontal axis...
                         crosshairState.updateCrosshairX(x);
                     }
                 }
                 else {
                     if (xyPlot.isRangeCrosshairLockedOnData()) {
                         // just the vertical axis...
                         crosshairState.updateCrosshairY(y);
                     }
                 }
             }
         }
     }
 
     class PlinkToolTipGenerator extends StandardXYToolTipGenerator{
 
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
