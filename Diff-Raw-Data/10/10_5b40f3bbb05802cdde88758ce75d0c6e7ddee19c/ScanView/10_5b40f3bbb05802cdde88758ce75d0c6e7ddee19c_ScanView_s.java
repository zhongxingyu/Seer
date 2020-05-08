 package ru.spbau.bioinf.tagfinder.ui;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JComponent;
 import ru.spbau.bioinf.tagfinder.Analyzer;
 import ru.spbau.bioinf.tagfinder.Configuration;
 import ru.spbau.bioinf.tagfinder.Peak;
 import ru.spbau.bioinf.tagfinder.Scan;
 
 public class ScanView extends JComponent {
 
     private static NumberFormat df = NumberFormat.getInstance();
     static {
         df.setMaximumFractionDigits(2);
     }
 
     private Configuration conf;
 
     private Scan scan;
     private Dimension dimension = new Dimension(1000, 10);
     private List<List<Peak[]>> components = new ArrayList<List<Peak[]>>();
 
     public ScanView(Configuration conf) {
         this.conf = conf;
     }
 
     public void setScan(Scan scan) {
         this.scan = scan;
         Analyzer analyzer = new Analyzer(conf);
         List<List<Peak>> components = analyzer.getComponents(scan);
         this.components.clear();
         int totalTags = 0;
         for (List<Peak> component : components) {
             List<Peak[]> tags = analyzer.getTags(component);
             totalTags += tags.size();
             this.components.add(tags);
         }
 
         dimension = new Dimension((int)scan.getPrecursorMass() + 200, components.size() * 20 + totalTags * 15);
         invalidate();
     }
 
     @Override
     public Dimension getMinimumSize() {
         return dimension;
     }
 
     @Override
     public Dimension getPreferredSize() {
         return dimension;
     }
 
     @Override
     public Dimension getMaximumSize() {
         return dimension;
     }
 
     @Override
     public void paint(Graphics g) {
         if (scan != null) {
             List<Peak> peaks = scan.getPeaks();
             for (Peak peak : peaks) {
                 int value = (int)peak.getValue();
                 g.drawLine(value, 5, value, 15);
             }
             double precursorMass = scan.getPrecursorMass();
             int total = (int) precursorMass;
             g.drawLine(total, 5, total, 15);
             g.drawString(df.format(precursorMass), total + 3, 15);
         }
 
         int start = 20;
         for (int i = 0; i < components.size(); i++) {
             List<Peak[]> component =  components.get(i);
             double min = scan.getPrecursorMass();
             for (Peak[] tag : component) {
                 double v = tag[0].getValue();
                 if (v < min) {
                     min = v;
                 }
             }
             g.drawString("Component " + (i + 1), 3, start + 20);
             start += 20;
             for (Peak[] tag : component) {
                for (Peak peak : tag) {
                     int value = (int)(peak.getValue() - min);
                     g.drawLine(value, start, value, start + 10);
                 }
                 start += 20;
             }
         }
     }
 }
