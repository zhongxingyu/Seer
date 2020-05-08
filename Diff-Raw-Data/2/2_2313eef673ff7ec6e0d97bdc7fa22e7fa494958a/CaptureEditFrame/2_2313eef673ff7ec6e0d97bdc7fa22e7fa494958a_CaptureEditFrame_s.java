 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package madsdf.shimmer.gui;
 
 import static com.google.common.base.Preconditions.*;
 import java.awt.Color;
 import java.awt.Paint;
 import java.io.BufferedOutputStream;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.Writer;
 import java.util.Arrays;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.swing.JComponent;
 import javax.swing.JFormattedTextField;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.text.DefaultFormatter;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.ValueAxis;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.ValueMarker;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.data.time.FixedMillisecond;
 import org.jfree.data.time.TimeSeries;
 import org.jfree.data.time.TimeSeriesCollection;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 
 /**
  *
  * @author julien
  */
 public class CaptureEditFrame extends javax.swing.JFrame {
     private XYSeries[] accelSeries = new XYSeries[3];
     private JFreeChart chart;
     
     private final int MOVEMENT_LENGTH = 150;
     
     private SpinnerNumberModel startSpinnerModel;
     private SpinnerNumberModel endSpinnerModel;
     private ValueMarker startMarker;
     private ValueMarker endMarker;
     
     private File saveFolder;
     private final String namePrefix;
     
     private float[][] accelData;
     
     private static float[][] arrCopy(float[][] arr) {
         float[][] out = new float[arr.length][];
         for (int i = 0; i < arr.length; ++i) {
             out[i] = new float[arr[i].length];
             for (int j = 0; j < arr[i].length; ++j) {
                 out[i][j] = arr[i][j];
             }
         }
         return out;
     }
     
     /**
      * Creates new form CaptureEditFrame
      */
     public CaptureEditFrame(String saveDir, String namePrefix, float[][] accel) {
         checkState(accel.length == 3);
         initComponents();
         
         this.namePrefix = namePrefix;
         this.saveFolder = new File(saveDir);
         this.accelData = arrCopy(accel);
         
         saveFolder.mkdirs();
         
         XYSeriesCollection accelCol = new XYSeriesCollection();
         accelSeries[0] = new XYSeries("X", true, false);
         accelCol.addSeries(accelSeries[0]);
         accelSeries[1] = new XYSeries("Y", true, false);
         accelCol.addSeries(accelSeries[1]);
         accelSeries[2] = new XYSeries("Z", true, false);
         accelCol.addSeries(accelSeries[2]);
         
         for (int i = 0; i < accel.length; ++i) {
             for (int j = 0; j < accel[i].length; ++j) {
                 accelSeries[i].add(j, accel[i][j]);
             }
         }
         
         startMarker = new ValueMarker(0);
         startMarker.setPaint(Color.BLUE);
         endMarker = new ValueMarker(accel[0].length - 1);
         endMarker.setPaint(Color.BLUE);
         
         startSpinnerModel = (SpinnerNumberModel) startSpinner.getModel();
         endSpinnerModel = (SpinnerNumberModel) endSpinner.getModel();
         startSpinnerModel.setMinimum(0);
         startSpinnerModel.setMaximum(accel[0].length - 1);
         endSpinnerModel.setMinimum(0);
         endSpinnerModel.setMaximum(accel[0].length - 1);
         endSpinnerModel.setValue(Math.max(MOVEMENT_LENGTH, accel[0].length - 1));
         spinnerSetCommitOnEdit(startSpinner);
         spinnerSetCommitOnEdit(endSpinner);
         
         chart = ChartFactory.createXYLineChart(
                 "Acceleration",
                 "Samples",
                 "Accel",
                 accelCol,
                 PlotOrientation.VERTICAL,
                 true,
                 false,
                 false);
         
         XYPlot plot = chart.getXYPlot();
         plot.addDomainMarker(startMarker);
         plot.addDomainMarker(endMarker);
         plot.setRangeGridlinesVisible(false);     // Hide the grid in the graph
         plot.setDomainGridlinesVisible(false);
         plot.setBackgroundPaint(Color.WHITE);
         ValueAxis axisAcc = plot.getDomainAxis();
         axisAcc.setTickMarksVisible(true);    // Define the tick count
         axisAcc.setMinorTickCount(10);
         axisAcc.setAutoRange(true);
         //axisAcc.setFixedAutoRange(NUM_VISIBLE);     // Define the number of visible value
         axisAcc.setTickLabelsVisible(true);  // Hide the axis labels
         
         plot.setRenderer(new XYLineAndShapeRenderer(true, false) {
           @Override
           public Paint lookupSeriesPaint(int series) {
               checkState(series >= 0 && series < 3);
               switch(series) {
                       case 0: return Color.RED;
                       case 1: return Color.GREEN;
                       case 2: return Color.BLUE;
                       default: return Color.BLACK;
               }
           }
       });
         
         ChartPanel cPanel = (ChartPanel)panAccel;
         cPanel.setChart(chart);
     }
     
     private static void spinnerSetCommitOnEdit(JSpinner spinner) {
         JComponent comp = spinner.getEditor();
         JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
         DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
         formatter.setCommitsOnValidEdit(true);
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         panAccel = new ChartPanel(null);
         jPanel1 = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
         startSpinner = new javax.swing.JSpinner();
         jLabel2 = new javax.swing.JLabel();
         endSpinner = new javax.swing.JSpinner();
         jLabel3 = new javax.swing.JLabel();
         commandSpinner = new javax.swing.JSpinner();
         saveButton = new javax.swing.JButton();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
 
         javax.swing.GroupLayout panAccelLayout = new javax.swing.GroupLayout(panAccel);
         panAccel.setLayout(panAccelLayout);
         panAccelLayout.setHorizontalGroup(
             panAccelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
         panAccelLayout.setVerticalGroup(
             panAccelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 188, Short.MAX_VALUE)
         );
 
         jLabel1.setText("Start");
 
         startSpinner.setModel(new javax.swing.SpinnerNumberModel());
         startSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 startSpinnerStateChanged(evt);
             }
         });
 
         jLabel2.setText("End");
 
         endSpinner.setModel(new javax.swing.SpinnerNumberModel());
         endSpinner.setEnabled(false);
         endSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 endSpinnerStateChanged(evt);
             }
         });
 
         jLabel3.setText("Command");
 
         commandSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
 
         saveButton.setText("Save");
         saveButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 saveButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(saveButton)
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addGroup(jPanel1Layout.createSequentialGroup()
                                 .addComponent(jLabel1)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(startSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jLabel2))
                             .addGroup(jPanel1Layout.createSequentialGroup()
                                 .addComponent(jLabel3)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(commandSpinner)))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(endSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap(131, Short.MAX_VALUE))
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel1)
                     .addComponent(startSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel2)
                     .addComponent(endSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel3)
                     .addComponent(commandSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addComponent(saveButton)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(panAccel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(panAccel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGap(18, 18, 18)
                 .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void startSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_startSpinnerStateChanged
         final int start = (Integer)startSpinnerModel.getValue();
         startMarker.setValue(start);
         endSpinnerModel.setValue(start + MOVEMENT_LENGTH);
     }//GEN-LAST:event_startSpinnerStateChanged
 
     private void endSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_endSpinnerStateChanged
         endMarker.setValue((Integer)endSpinnerModel.getValue());
     }//GEN-LAST:event_endSpinnerStateChanged
 
     private static void writeAxis(BufferedWriter w, String name, float[] vals) throws IOException {
         w.write(name + " : ");
         for (int i = 0; i < vals.length; ++i) {
             w.write(String.format("%.15g", vals[i]));
             if (i < vals.length - 1) {
                 w.write(";");
             }
         }
         w.write("\n");
     }
     
     private BufferedWriter createFile(int command) throws IOException {
        final String prefix = namePrefix + "_movement_ " + command;
         File[] existing = saveFolder.listFiles(new FilenameFilter() {
             @Override
             public boolean accept(File dir, String name) {
                 return name.startsWith(prefix);
             }
         });
         // Find the maximum sample number in existing files
         Pattern fpat = Pattern.compile(prefix + "_(\\d+).txt");
         int maxSample = 0;
         for (File f: existing) {
             final Matcher m = fpat.matcher(f.getName());
             if (m.matches()) {
                 final int num = Integer.parseInt(m.group(1));
                 maxSample = Math.max(maxSample, num);
             }
         }
         
         final String fname = prefix + "_" + (maxSample + 1) + ".txt";
         return new BufferedWriter(
                 new FileWriter(saveFolder.getAbsolutePath() + "/" + fname));
     }
     
     private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
         try {
             int command = (Integer)((SpinnerNumberModel)commandSpinner.getModel()).getValue();
             int start = (Integer)startSpinnerModel.getValue();
             int end = (Integer)endSpinnerModel.getValue();
             
             System.out.println("Saving from " + start + " to " + end +
                     "( => " + (end - start) + " samples");
             if (end - start != MOVEMENT_LENGTH) {
                 System.out.println("Too short movement !");
             }
             
             BufferedWriter saveWriter = createFile(command);
             
             // TODO: Should increment sample number ?
             saveWriter.write("COMMAND " + command + " SAMPLE 1\n");
             writeAxis(saveWriter, "Accel X", Arrays.copyOfRange(accelData[0], start, end));
             writeAxis(saveWriter, "Accel Y", Arrays.copyOfRange(accelData[1], start, end));
             writeAxis(saveWriter, "Accel Z", Arrays.copyOfRange(accelData[2], start, end));
             
             // TODO: Gyro is just fill-in data
             writeAxis(saveWriter, "Gyro X", Arrays.copyOfRange(accelData[0], start, end));
             writeAxis(saveWriter, "Gyro Y", Arrays.copyOfRange(accelData[1], start, end));
             writeAxis(saveWriter, "Gyro Z", Arrays.copyOfRange(accelData[2], start, end));
             saveWriter.close();
         } catch (IOException ex) {
             Logger.getLogger(CaptureEditFrame.class.getName()).log(Level.SEVERE, null, ex);
         }
     }//GEN-LAST:event_saveButtonActionPerformed
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) throws FileNotFoundException, IOException {
         /* Set the Nimbus look and feel */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
          * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
          */
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(CaptureEditFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(CaptureEditFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(CaptureEditFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(CaptureEditFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
         
         
         final float[][] accel1 = {
             {3497, 3523, 3655, 3665, 3652, 3565, 3449, 3319, 3271, 3175, 3105,
              2996, 2956, 2911, 2785, 2717, 2574, 2426, 2374, 2264, 2177, 2040,
              1930, 1797, 1567, 1309, 1068,  876,  776,  692,  676,  640,  473,
               315,  149,   46,   22,   22,   22,   26,   95,  161,  273,  342,
               456,  599,  808,  986, 1129, 1288, 1400, 1519, 1724, 1822, 1922,
              2008, 2066, 2125, 2144, 2172, 2322, 2906, 3722, 4074, 3872, 3743,
              3773, 3943, 4077, 4076, 4071, 3812, 3641, 3668, 3587, 3477, 3406,
              3344, 3258, 3175, 3108, 3004, 2970, 2924, 2867, 2852, 2838, 2779,
              2817, 2832, 2833, 2852, 2881, 2879, 2851, 2877, 2857, 2847, 2794,
              2758},
             {2353, 2399, 2427, 2454, 2462, 2466, 2402, 2392, 2372, 2276, 2191,
              2153, 2094, 2139, 2147, 2102, 2119, 2090, 2079, 2113, 2136, 2249,
              2270, 2273, 2278, 2255, 2226, 2165, 2134, 2074, 2013, 1974, 1913,
              1879, 1853, 1841, 1813, 1775, 1752, 1731, 1730, 1758, 1754, 1786,
              1784, 1806, 1793, 1792, 1807, 1800, 1831, 1857, 1869, 1942, 2009,
              2054, 2179, 2333, 2469, 2712, 2907, 3086, 3269, 3192, 3124, 3258,
              3354, 3279, 3143, 2869, 2595, 2457, 2352, 2266, 2207, 2168, 2139,
              2120, 2132, 2142, 2138, 2183, 2176, 2160, 2150, 2140, 2107, 2037,
              2057, 2082, 2081, 2087, 2083, 2108, 2082, 2052, 2070, 2062, 2076,
              2050},
             {1681, 1665, 1674, 1656, 1693, 1666, 1643, 1621, 1614, 1639, 1652,
              1738, 1759, 1778, 1793, 1804, 1850, 1882, 1890, 1917, 1967, 1945,
              1934, 1916, 1898, 1894, 1791, 1727, 1694, 1780, 1806, 1773, 1814,
              1828, 1856, 1852, 1837, 1826, 1780, 1750, 1729, 1729, 1658, 1684,
              1691, 1745, 1788, 1800, 1852, 1816, 1807, 1748, 1787, 1777, 1776,
              1851, 1846, 1807, 1681, 1578, 1496, 1508, 1692, 1867, 1700, 1446,
              1501, 1808, 2107, 2151, 2026, 1949, 1835, 1856, 1957, 1955, 1975,
              1990, 1971, 1987, 1968, 1945, 1923, 1900, 1888, 1899, 1947, 1954,
              1950, 1955, 1939, 1919, 1930, 1932, 1922, 1931, 1963, 1964, 1949,
              1948}
         };
         
         /* Create and display the form */
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 new CaptureEditFrame("movements", "", accel1).setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JSpinner commandSpinner;
     private javax.swing.JSpinner endSpinner;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel panAccel;
     private javax.swing.JButton saveButton;
     private javax.swing.JSpinner startSpinner;
     // End of variables declaration//GEN-END:variables
 }
