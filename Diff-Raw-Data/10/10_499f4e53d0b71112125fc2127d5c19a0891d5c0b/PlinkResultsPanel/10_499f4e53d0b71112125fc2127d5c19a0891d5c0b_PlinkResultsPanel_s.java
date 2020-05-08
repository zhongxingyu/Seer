 package edu.mit.wi.haploview;
 
 import edu.mit.wi.plink.PlinkTableModel;
 
 import javax.swing.*;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.border.TitledBorder;
 import java.util.Vector;
 import java.util.Hashtable;
 import java.awt.*;
 import java.awt.geom.Rectangle2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.io.FileWriter;
 import java.io.BufferedWriter;
 
 import org.jfree.data.xy.*;
 import org.jfree.chart.*;
 import org.jfree.chart.entity.EntityCollection;
 import org.jfree.chart.entity.XYItemEntity;
 import org.jfree.chart.labels.StandardXYToolTipGenerator;
 import org.jfree.chart.axis.ValueAxis;
 import org.jfree.chart.renderer.xy.XYItemRendererState;
 import org.jfree.chart.renderer.xy.XYDotRenderer;
 import org.jfree.chart.plot.*;
 import org.jfree.ui.RefineryUtilities;
 import org.jfree.ui.RectangleEdge;
 
 public class PlinkResultsPanel extends JPanel implements ActionListener, Constants {
     private JTable table;
     private PlinkTableModel plinkTableModel;
     private TableSorter sorter;
 
     String[] chromNames = {"","1","2","3","4","5","6","7","8","9","10",
             "11","12","13","14","15","16","17","18","19","20","21","22","X","Y"};
     String[] signs = {"",">=","<=","="};
     private JComboBox chromChooser, genericChooser, signChooser, removeChooser;
     private NumberTextField chromStart, chromEnd;
     private JTextField valueField, markerField;
     private JPanel filterPanel;
     private Vector originalColumns;
     private Hashtable removedColumns;
     private Hashtable[] info;
 
     private int startPos, endPos, numResults;
     private double significant, suggestive;
     private String chromChoice, columnChoice, signChoice, value, marker;
     private HaploView hv;
 
 
     public PlinkResultsPanel(HaploView h, Vector results, Vector colNames, Vector filters){
         hv = h;
 
         setLayout(new GridBagLayout());
 
         plinkTableModel = new PlinkTableModel(colNames,results);
         sorter = new TableSorter(plinkTableModel);
         numResults = plinkTableModel.getRowCount();
         removedColumns = new Hashtable(1,1);
         originalColumns = (Vector)plinkTableModel.getUnknownColumns().clone();
 
 
         table = new JTable(sorter);
         table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         table.getTableHeader().setReorderingAllowed(false);
         //due to an old JTable bug this is necessary to activate a horizontal scrollbar
         if (table.getColumnCount() > 15 && table.getRowCount() > 60){
             table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
         }
         //table.getModel().addTableModelListener(this);
 
         sorter.setTableHeader(table.getTableHeader());
         table.getColumnModel().getColumn(0).setPreferredWidth(60);
 
         final PlinkCellRenderer renderer = new PlinkCellRenderer();
         try{
             table.setDefaultRenderer(Class.forName("java.lang.Double"), renderer);
             table.setDefaultRenderer(Class.forName("java.lang.Integer"), renderer);
             table.setDefaultRenderer(Class.forName("java.lang.Long"), renderer);
             table.setDefaultRenderer(Class.forName("java.lang.String"),renderer);
         }catch (Exception e){
         }
 
         JScrollPane tableScroller = new JScrollPane(table);
         //tableScroller.setMinimumSize(new Dimension(800,600));
         //tableScroller.setMaximumSize(new Dimension(800,(int)tableScroller.getPreferredSize().getHeight()));
 
 
         JPanel mainFilterPanel = new JPanel();
         mainFilterPanel.setMinimumSize(new Dimension(700,40));
         mainFilterPanel.add(new JLabel("Chromosome:"));
         chromChooser = new JComboBox(chromNames);
         mainFilterPanel.add(chromChooser);
         mainFilterPanel.add(new JLabel("Start kb:"));
         chromStart = new NumberTextField("",6,false);
         mainFilterPanel.add(chromStart);
         mainFilterPanel.add(new JLabel("End kb:"));
         chromEnd = new NumberTextField("",6,false);
         mainFilterPanel.add(chromEnd);
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
 
         JPanel extraFilterPanel = new JPanel();
         extraFilterPanel.add(new JLabel("Marker:"));
         markerField = new JTextField(8);
         extraFilterPanel.add(markerField);
         JButton doMarkerFilter = new JButton("Go");
         doMarkerFilter.setActionCommand("marker filter");
         doMarkerFilter.addActionListener(this);
         extraFilterPanel.add(doMarkerFilter);
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
         if (hv.getPlinkDups()){
             moreResults.setEnabled(false);
         }
         JButton plotButton = new JButton("Plot");
         plotButton.addActionListener(this);
 
         filterPanel = new JPanel(new GridBagLayout());
         GridBagConstraints a = new GridBagConstraints();
         filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Viewing " + numResults + " results"));
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
         filterPanel.add(moreResults,a);
         a.gridx = 1;
         a.anchor = GridBagConstraints.SOUTH;
         filterPanel.add(plotButton,a);
         a.gridx = 2;
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
         c.gridy = 2;
         add(goPanel,c);
 
         if (filters != null){
             if (((String)filters.get(0)).equals("")){
                 chromChooser.setSelectedIndex(0);
             }else{
                 chromChooser.setSelectedIndex(new Integer((String)filters.get(0)).intValue());
             }
 
 
             if (((String)filters.get(1)).equals("") || ((String)filters.get(2)).equals("")){
                 chromStart.setText("");
                 chromEnd.setText("");
             }else if (Integer.parseInt((String)filters.get(1)) >= 0 && Integer.parseInt((String)filters.get(2)) >= 0){
                 chromStart.setText((String)filters.get(1));
                 chromEnd.setText((String)filters.get(2));
             }
 
             if (!((String)filters.get(3)).equals("0")){
                 genericChooser.setSelectedIndex(Integer.parseInt((String)filters.get(3)));
                 signChooser.setSelectedIndex(Integer.parseInt((String)filters.get(4)));
                 valueField.setText((String)filters.get(5));
             }
 
 
             doFilters();
         }
 
     }
 
     public void doMarkerFilter(){
         clearSorting();
         plinkTableModel.filterMarker(marker);
         countResults();
     }
 
     public void doFilters(){
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
 
         if (genericChooser.getSelectedIndex() > 0){
             columnChoice = (String)genericChooser.getSelectedItem();
 
             if (signChooser.getSelectedIndex() > 0){
                 signChoice = (String)signChooser.getSelectedItem();
 
                 if (!(valueField.getText().equals(""))){
                     value = valueField.getText();
                 }
             }
         }
 
         clearSorting();
         plinkTableModel.filterAll(chromChoice,startPos,endPos,columnChoice,signChoice,value);
         countResults();
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
 
         if (table.getColumnCount() > 15 && table.getRowCount() > 60){
             table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
         }
 
         clearSorting();
         plinkTableModel.resetFilters();
         chromChooser.setSelectedIndex(0);
         chromChoice = null;
         chromStart.setText("");
         startPos = -1;
         chromEnd.setText("");
         endPos = -1;
         genericChooser.setSelectedIndex(0);
         genericChooser.updateUI();
         columnChoice = null;
         signChooser.setSelectedIndex(0);
         signChoice = null;
         valueField.setText("");
         value = null;
         markerField.setText("");
         marker = null;
         countResults();
     }
 
     public void clearSorting(){
         for (int i = 0; i < table.getColumnCount(); i++){
             sorter.setSortingStatus(i,TableSorter.NOT_SORTED);
         }
     }
 
     public void countResults(){
         numResults = plinkTableModel.getRowCount();
         filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Viewing " + numResults + " results"));
         repaint();
     }
 
     public XYSeriesCollection makeDataSet(int col){
         int numRows = table.getRowCount();
         long[] maxPositions = new long[25];
 
         for (int i = 0; i < numRows; i++){
             String chrom = (String)table.getValueAt(i,0);
             int chr = 0;
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
 
         info = new Hashtable[25];
         for (int i = 0; i < 24; i++){
             info[i] = new Hashtable();
         }
 
         for (int i = 0; i < numRows; i++){
             String chrom = (String)table.getValueAt(i,0);
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
 
             try{
                 f = ((Double)table.getValueAt(i,col)).doubleValue();
             }catch (ClassCastException cce){
                 JOptionPane.showMessageDialog(this,
                         "The selected column is not formatted correctly \n" +
                                 "for a -log10 plot.",
                         "Invalid column",
                         JOptionPane.ERROR_MESSAGE);
                 return null;
             }catch (NullPointerException npe){ //this can happen with blank table values from additional results
                 continue;
             }
 
             if (f < 0 || f > 1){
                 JOptionPane.showMessageDialog(this,
                         "The selected column is not formatted correctly \n" +
                                 "for a -log10 plot.",
                         "Invalid column",
                         JOptionPane.ERROR_MESSAGE);
                 return null;
             }
             f = (Math.log(f)/Math.log(10))*-1;
             long kbPos = Long.parseLong(String.valueOf(table.getValueAt(i,2)))/1000;
             String infoString = table.getValueAt(i,1) + ", Chr" + chrom + ":" + kbPos + ", " + table.getValueAt(i,col);
             info[chr-1].put(new Double(c),infoString);
             xyArray[chr].add(c,f);
         }
 
         XYSeriesCollection dataset = new XYSeriesCollection();
 
         for (int i = 1; i < 25; i++){
             if (xyArray[i].getItemCount() > 0){
                 dataset.addSeries(xyArray[i]);
             }
         }
         return dataset;
     }
 
     public void makeChart(int plotType, int col, double sig, double sug){
         XYSeriesCollection dataSet = makeDataSet(col);
         if (dataSet == null){
             return;
         }
 
         significant = sig;
         suggestive = sug;
 
         JFreeChart chart = ChartFactory.createScatterPlot(null,
                 null, PLOT_TYPES[plotType] + "(" + table.getColumnName(col) + ")", dataSet, PlotOrientation.VERTICAL, true, true, false);
         BasicStroke stroke = new BasicStroke();
         ValueMarker sugMarker = new ValueMarker(sug,Color.blue,stroke);
         chart.getXYPlot().addRangeMarker(sugMarker);
         ValueMarker sigMarker = new ValueMarker(sig,Color.red,stroke);
         chart.getXYPlot().addRangeMarker(sigMarker);
         chart.getXYPlot().setDomainGridlinesVisible(false);
         chart.getXYPlot().getDomainAxis().setTickMarksVisible(false);
         chart.getXYPlot().getDomainAxis().setTickLabelsVisible(false);
         PlinkScatterPlotRenderer xyd = new PlinkScatterPlotRenderer();
         chart.getXYPlot().setRenderer(xyd);
         Shape[] shapes = new Shape[1];
         shapes[0] = new Rectangle2D.Double(-2,-3,20,5);
         DrawingSupplier supplier = new DefaultDrawingSupplier(
             DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE,
             DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
             DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
             DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
             shapes
         );
         chart.getXYPlot().setDrawingSupplier(supplier);
         PlinkToolTipGenerator xyt = new PlinkToolTipGenerator();
         xyd.setToolTipGenerator(xyt);
         chart.setAntiAlias(false);
         ChartPanel panel = new ChartPanel(chart, true);
         panel.setPreferredSize(new Dimension(750,300));
         panel.setMinimumDrawHeight(10);
         panel.setMaximumDrawHeight(2000);
         panel.setMinimumDrawWidth(20);
         panel.setMaximumDrawWidth(2000);
         JFrame plotFrame = new JFrame(table.getColumnName(col));
         plotFrame.setContentPane(panel);
         plotFrame.pack();
         RefineryUtilities.centerFrameOnScreen(plotFrame);
         plotFrame.setVisible(true);
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
         Vector filters = new Vector();
 
         if (chromChoice != null && !chromChoice.equals("")){
             if(chromChoice.equals("X")){
                 filters.add("23");
             }else if (chromChoice.equals("Y")){
                 filters.add("24");
             }else{
                 filters.add(new Integer(chromChooser.getSelectedIndex()).toString());
             }
 
             filters.add(new Integer(startPos).toString());
             filters.add(new Integer(endPos).toString());
 
         }else{
             filters.add("");
             filters.add("0");
             filters.add("0");
         }
 
         filters.add(new Integer(genericChooser.getSelectedIndex()).toString());
         filters.add(new Integer(signChooser.getSelectedIndex()).toString());
         filters.add(valueField.getText());
 
         hv.setPlinkFilters(filters);
         RegionDialog rd = new RegionDialog(hv,gotoChrom,gotoMarker,markerPosition,"Go to Region");
         rd.pack();
         rd.setVisible(true);
     }
 
     public void exportTable(File outfile) throws IOException, HaploViewException{
         BufferedWriter plinkWriter = new BufferedWriter(new FileWriter(outfile));
         for (int i = 0; i < plinkTableModel.getColumnCount(); i++){
             plinkWriter.write(plinkTableModel.getColumnName(i)+"\t");
         }
         plinkWriter.newLine();
 
         for (int i = 0; i < plinkTableModel.getRowCount(); i++){
             for (int j = 0; j < plinkTableModel.getColumnCount(); j++){
                 if (plinkTableModel.getValueAt(i,j) == null){
                     plinkWriter.write("-"+"\t");
                 }else{
                     plinkWriter.write(plinkTableModel.getValueAt(i,j)+"\t");
                 }
             }
             plinkWriter.newLine();
         }
         plinkWriter.close();
     }
 
     public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();
         if (command.equals("Filter")){
             doFilters();
         }else if (command.equals("marker filter")){
             marker = markerField.getText();
             if (!(marker.equals(""))){
                 doMarkerFilter();
             }
         }
         else if (command.equals("Reset")){
             clearFilters();
         }else if (command.equals("Load Additional Results")){
             HaploView.fc.setSelectedFile(new File(""));
             int returned = HaploView.fc.showOpenDialog(this);
             if (returned != JFileChooser.APPROVE_OPTION) return;
             File file = HaploView.fc.getSelectedFile();
             String fullName = file.getParent()+File.separator+file.getName();
             String[] inputs = {null,null,fullName,null};
             hv.readWGA(inputs);
         }else if (command.equals("Remove")){
             if (removeChooser.getSelectedIndex() > 0){
                 TableColumn deletedColumn = table.getColumn(removeChooser.getSelectedItem());
                 removedColumns.put(removeChooser.getSelectedItem(),deletedColumn);
                 table.removeColumn(deletedColumn);
                 removeChooser.removeItemAt(removeChooser.getSelectedIndex());
                 removeChooser.setSelectedIndex(removeChooser.getItemCount()-1);
                 genericChooser.setSelectedIndex(0);
                 if (table.getColumnCount() < 18){
                     table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
                 }
                 repaint();
             }
         }else if (command.equals("Plot")){
             PlotOptionDialog pod = new PlotOptionDialog(hv,this,"Plot Options",plinkTableModel.getUnknownColumns());
             pod.pack();
             pod.setVisible(true);
         }
         else if (command.equals("Go to Selected Region")){
             gotoRegion();
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
             if (y != Double.NaN) {
                 RectangleEdge xAxisLocation = xyPlot.getDomainAxisEdge();
                 RectangleEdge yAxisLocation = xyPlot.getRangeAxisEdge();
                 double transX = valueAxis.valueToJava2D(x, rectangle2D, xAxisLocation);
                 double transY = valueAxis1.valueToJava2D(y, rectangle2D,yAxisLocation);
 
                 graphics2D.setPaint(this.getItemPaint(series, item));
                 PlotOrientation orientation = xyPlot.getOrientation();
                 if (orientation == PlotOrientation.HORIZONTAL) {
                     if (y > suggestive && y <= significant && suggestive != -1){
                         graphics2D.fillRect((int) transY, (int) transX, 4, 4);
                     }else if (y > significant && significant != -1){
                         graphics2D.fillRect((int) transY, (int) transX, 6, 6);
                     }else{
                         graphics2D.fillRect((int) transY, (int) transX, 2, 2);
                     }
                 }
                 else if (orientation == PlotOrientation.VERTICAL) {
                     if (y > suggestive && y <= significant && suggestive != -1){
                         graphics2D.fillRect((int) transX, (int) transY, 4, 4);
                     }else if (y > significant && significant != -1){
                         graphics2D.fillRect((int) transX, (int) transY, 6, 6);
                     }else{
                         graphics2D.fillRect((int) transX, (int) transY, 2, 2);
                     }
                 }
 
                 // add an entity for the item...
                 if (entities != null && y > suggestive) {
                     if (entityArea == null) {
                         entityArea = new Rectangle2D.Double(transX - 2, transY - 2, 20, 20);
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
            return (String)info[series].get(new Double(dataset.getXValue(series,item)));
         }
     }
 }
