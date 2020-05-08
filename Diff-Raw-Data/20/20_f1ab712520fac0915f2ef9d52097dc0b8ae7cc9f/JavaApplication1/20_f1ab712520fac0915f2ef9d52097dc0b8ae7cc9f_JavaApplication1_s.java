 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gubas.javaapplication1;
 
 import gubas.components.GubasChart;
 import gubas.components.TableComponent;
 import gubas.forms.*;
 import gubas.images.Images;
 import java.awt.Color;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.axis.ValueAxis;
 import org.jfree.chart.labels.XYToolTipGenerator;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
 import org.jfree.chart.renderer.xy.XYItemRenderer;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.data.xy.DefaultXYDataset;
 import org.jfree.data.xy.XYDataset;
 
 /**
  *
  * @author Przemysław
  */
 public class JavaApplication1 {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         DataForm f = new DataForm();
  
         TableComponent tc = new TableComponent(new Object[][]{{5,1,0}, {0,12,0}, {1,0,0}}, new String[]{"A","B", "C"});
         tc.setOpaque(false);
         f.add(tc);
         //showChart(f);
         //showCandleChart(f);
         FormsCaller.callNewWindow("Table window", f);
     }
     
     
     public static void showCandleChart(BaseForm f){
         JFreeChart chart =  ChartFactory.createCandlestickChart("Candlesticks", "Date", "Price", FakeSeries.getOHLCSeriesCollection(), true);
         ChartPanel p = new ChartPanel(chart);
         DefaultXYDataset seria = new DefaultXYDataset();
         seria.addSeries("Seria A", FakeSeries.getLineOHLCSeries());
         chart.getXYPlot().setDataset(1, seria);
         chart.getXYPlot().setRenderer(1, new XYLineAndShapeRenderer());
         GubasChart ch = new GubasChart(p);
         chart.setBackgroundImage(Images.getImageIcon(Images.RedGradientBackground).getImage());
         chart.getXYPlot().setBackgroundImage(Images.getImageIcon(Images.GreenGradientBackground).getImage());
         f.add(ch);
     }
     
     public static void showChart(BaseForm f){
         DefaultXYDataset seria = new DefaultXYDataset();
         seria.addSeries("Seria A", FakeSeries.getASeries());
         XYItemRenderer rend = new StandardXYItemRenderer(StandardXYItemRenderer.LINES);
         rend.setBaseItemLabelsVisible(Boolean.TRUE);
         rend.setSeriesToolTipGenerator(0, new XYToolTipGenerator() {
 
             @Override
             public String generateToolTip(XYDataset xyd, int i, int i1) {
                 return xyd.getXValue(i, i1) + ", "+ xyd.getYValue(i, i1);
             }
         });
         
         JFreeChart chart =  new JFreeChart(new XYPlot(seria, getXAxis(), getYAxis(), rend));
         ChartPanel p = new ChartPanel(chart);
         GubasChart ch = new GubasChart(p);
         chart.setBackgroundImage(Images.getImageIcon(Images.RedGradientBackground).getImage());
         chart.getXYPlot().setBackgroundImage(Images.getImageIcon(Images.GreenGradientBackground).getImage());
         f.add(ch);
     }
     
     private static ValueAxis getYAxis(){
         return new NumberAxis();
     }
     private static ValueAxis getXAxis(){
         return new NumberAxis();
     }
     
 }
