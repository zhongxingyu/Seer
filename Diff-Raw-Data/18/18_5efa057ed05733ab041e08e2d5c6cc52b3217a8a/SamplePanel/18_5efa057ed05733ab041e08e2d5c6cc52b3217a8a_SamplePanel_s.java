 package edu.usc.chla.vpicu.explorer.newui;
 
 import java.awt.BorderLayout;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.EtchedBorder;
 
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 
 import edu.usc.chla.vpicu.explorer.BaseProvider;
 
 public class SamplePanel extends JPanel implements SampleListener {
   
   private static final long serialVersionUID = 1L;
   
   private SampleInputPanel input;
   
   public SamplePanel(BaseProvider provider) {
     input = new SampleInputPanel(provider);
     input.addSampleListener(this);
        
     setLayout(new BorderLayout());
     setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
         "Sample"));
         
     add(this.input, BorderLayout.NORTH);
   }
   
   public SampleInputPanel getInputPanel() {
     return input;
   }
 
   @Override
   public void queryPerformed(SampleEvent e) {
     JFreeChart chart = e.getHistogram().getChart(e.getTitle());
    ChartPanel panel = new ChartPanel(chart);
    panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    add(panel, BorderLayout.CENTER);
    revalidate();
   }
 }
