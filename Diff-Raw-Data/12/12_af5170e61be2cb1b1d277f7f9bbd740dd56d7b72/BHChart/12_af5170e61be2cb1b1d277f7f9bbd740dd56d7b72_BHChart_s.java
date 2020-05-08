 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.bh.gui.chart;
 
 import java.awt.image.BufferedImage;
 
 import org.bh.gui.swing.IBHComponent;
 import org.bh.platform.Services;
 import org.bh.platform.i18n.ITranslator;
 import org.jfree.chart.JFreeChart;
 
 /**
  *
  * @author Marco Hammel
  */
 public abstract class BHChart implements IBHComponent {
     /**
      * definig access to get x axis title by runtime
      */
    protected static final String DIMX = "_x";
     /**
      * definig access to get y axis title by runtime
      */
    protected static final String DIMY = "_y";
 
     ITranslator translator;
     
     String key;
     JFreeChart chart;
     //private String inputHint;
 
     public BHChart(String key) {
         this.key = key;
         this.translator = Services.getTranslator();
     }
 
     @Override
     public String getHint() {
         return "";
     }
 
     @Override
     public String getKey() {
         return this.key;
     }
 
     /**
      * returns the created Chart of the <code>BHBarChart</code>
      *
      * @return JFreeChart BarChart
      */
     public JFreeChart getChart() {
         return chart;
     }
 
     /**
      * Set new text values in case of language change
      */
     protected void reloadText() {
         this.chart.getPlot().setNoDataMessage(translator.translate("noDataAvailable"));
         this.chart.setTitle(translator.translate(key));
         //TODO extend method for x and y title exchange
     }
     
     public BufferedImage getChartAsImage(int width, int height) {
     	return chart.createBufferedImage(width, height);
     }
 }
