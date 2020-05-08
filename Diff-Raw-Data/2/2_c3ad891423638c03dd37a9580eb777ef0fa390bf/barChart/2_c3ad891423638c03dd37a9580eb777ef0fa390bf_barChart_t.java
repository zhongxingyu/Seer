 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package yazLabChartLib;
 import java.awt.*;
 import javax.swing.*;
 /**
  *
  * @author ugurcan
  * took some help and code from http://www.roseindia.net/java/example/java/swing/draw-simple-bar-chart.shtml
  */
 public class barChart extends JPanel {
 private double[] values;
   private String[] name;
   int topNCount;
   int count,barCount;
   private String title;
   Color[] colors = {Color.BLUE,Color.CYAN,Color.GREEN,Color.MAGENTA,Color.ORANGE,Color.RED,Color.YELLOW,Color.PINK,Color.GRAY,Color.DARK_GRAY,Color.LIGHT_GRAY};
 
   public barChart(String[] lang,double[] val, String t,int topn,int cn) {
     name = lang;
     values = val;
     title = t;
     topNCount = topn;
     count = cn;
 
   }
   public void paintComponent(Graphics graphics) {
     super.paintComponent(graphics);
       if (values == null || values.length == 0)
       return;
                 if (count < topNCount){
                 barCount = count;
             }
             else {
                 barCount = topNCount;
             }
     double minValue = 0;
     double maxValue = 0;
     for (int i = 0; i < barCount; i++) {
       if (minValue > values[i])
         minValue = values[i];
       if (maxValue < values[i])
         maxValue = values[i];
     }
     Dimension dim = getSize();
     int clientWidth = dim.width;
     int clientHeight = dim.height;
     int barWidth = clientWidth / barCount;
     Font titleFont = new Font("Book Antiqua", Font.BOLD, 15);
     FontMetrics titleFontMetrics = graphics.getFontMetrics(titleFont);
     Font labelFont = new Font("Book Antiqua", Font.PLAIN, 10);
     FontMetrics labelFontMetrics = graphics.getFontMetrics(labelFont);
     int titleWidth = titleFontMetrics.stringWidth(title);
     int q = titleFontMetrics.getAscent();
     int p = (clientWidth - titleWidth) / 2;
     graphics.setFont(titleFont);
     graphics.drawString(title, p, q);
     int top = titleFontMetrics.getHeight();
     int bottom = labelFontMetrics.getHeight();
     if (maxValue == minValue)
       return;
     double scale = (clientHeight - top - bottom) / (maxValue - minValue);
    q = clientHeight - labelFontMetrics.getDescent();
     graphics.setFont(labelFont);
 
     for (int j = 0; j < barCount; j++) {
       int valueP = j * barWidth + 1;
       int valueQ = top;
       int height = (int) (values[j] * scale);
       if (values[j] >= 0)
         valueQ += (int) ((maxValue - values[j]) * scale);
       else {
         valueQ += (int) (maxValue * scale);
         height = -height;
       }
 
      graphics.setColor((Color)colors[j%10]);
       graphics.fillRect(valueP, valueQ, barWidth - 2, height);
       graphics.setColor(Color.black);
       graphics.drawRect(valueP, valueQ, barWidth - 2, height);
       int labelWidth = labelFontMetrics.stringWidth(name[j]);
       int valuesWidth = labelFontMetrics.stringWidth(String.valueOf(values[j]));
       p = j * barWidth + (barWidth - labelWidth) / 2;
       int vp = j * barWidth + (barWidth - valuesWidth) / 2 ;
       graphics.drawString(name[j], p, q);
       graphics.drawString(String.valueOf(values[j]), vp, valueQ);
     }
   }
 }
