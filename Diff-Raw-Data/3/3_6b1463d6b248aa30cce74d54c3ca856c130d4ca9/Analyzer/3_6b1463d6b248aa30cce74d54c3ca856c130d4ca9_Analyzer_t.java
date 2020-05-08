 package ru.spbau.bioinf.tagfinder;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import ru.spbau.bioinf.tagfinder.util.XmlUtil;
 import ru.spbau.bioinf.tagfinder.view.Table;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 public class Analyzer {
     private  Configuration conf;
 
     public Analyzer(Configuration conf) {
         this.conf = conf;
     }
 
     public static void main(String[] args) throws Exception {
         Configuration conf = new Configuration(args
                 , "mod3"
         );
         Analyzer analyzer = new Analyzer(conf);
         Map<Integer, Scan> scans = conf.getScans();
         for (int scanId : scans.keySet()) {
             //int scanId = 510;
             //int scanId = 3008
             //890
 
             Scan scan = scans.get(scanId);
 
             //analyzer.printEdges(scan);
             analyzer.showPasses(scan);
         }
     }
 
     private void printEdges(Scan scan) throws Exception {
         Map<Integer,Integer> msAlignResults = conf.getMSAlignResults();
         int proteinId = msAlignResults.get(scan.getId());
         System.out.println("proteinId = " + proteinId);
         Protein p = conf.getProteins().get(proteinId);
         System.out.println("protein = " + p.getName());
         System.out.println(p.getSimplifiedAcids());
         double precursorMassShift = PrecursorMassShiftFinder.getPrecursorMassShift(conf, scan);
         List<Peak> peaks = scan.createSpectrumWithYPeaks(precursorMassShift);
 
         int n = peaks.size();
 
         for (int i = 0; i < n; i++) {
             Peak peak = peaks.get(i);
             System.out.print(peak.getValue() + " " + peak.getPeakType().name() + " ");
             for (int j = i+1; j < n; j++) {
                 Peak next =  peaks.get(j);
                 double[] limits = conf.getEdgeLimits(peak, next);
                 for (Acid acid : Acid.values()) {
                     if (acid.match(limits)) {
                         System.out.print(acid.name() + " " + next.getValue() + " ");
                     }
                 }
             }
             System.out.println();
         }
     }
 
     public void showPasses(Scan scan) throws IOException {
         double precursorMassShift = PrecursorMassShiftFinder.getPrecursorMassShift(conf, scan);
         List<Peak> peaks = scan.createSpectrumWithYPeaks(precursorMassShift);
         int n = peaks.size();
 
         for (int i = 0; i < n; i++) {
             peaks.get(i).setComponentId(i);
         }
 
         GraphUtil.generateEdges(conf, peaks);
 
         for (Peak peak : peaks) {
             peak.populatePrev();
         }
 
         int i = 0;
         while (i < peaks.size() - 1) {
             Peak p1 = peaks.get(i);
             Peak p2 = peaks.get(i + 1);
             if (p2.getValue() - p1.getValue() < 0.1) {
                 if (p1.isParent(p2)) {
                     peaks.remove(p2);
                     p1.addCopy(p2);
                 } else if (p2.isParent(p1)) {
                     peaks.remove(p1);
                     p2.addCopy(p1);
                 } else {
                     i++;
                 }
             } else {
                 i++;
             }
         }
 
         boolean done;
 
         do {
             done = true;
             for (Peak peak : peaks) {
                 if (peak.updateComponentId()) {
                     done = false;
                 }
             }
         } while (!done);
 
         Document doc = new Document();
         Element root = new Element("scan");
         doc.setRootElement(root);
        XmlUtil.addElement(root, "precursor-mass", scan.getPrecursorMass());
 
         boolean[] componentDone = new boolean[n];
 
         for (Peak p : peaks) {
             int componentId = p.getComponentId();
             if (!componentDone[componentId]) {
                 List<Peak> component = new ArrayList<Peak>();
                 for (Peak peak : peaks) {
                     if (peak.getComponentId() == componentId) {
                         component.add(peak);
                     }
                 }
                 if (component.size() > 1) {
                     Table table = getComponentView(component);
                     root.addContent(table.toXML());
                     componentDone[componentId] = true;
                 }
             }
         }
 
         XmlUtil.saveXml(doc, conf.getScanXmlFile(scan));
     }
 
     private Table getComponentView(List<Peak> peaks) {
         Table table = new Table();
 
         int row = 0;
 
         Peak[] firstTag = null;
         Peak[] bestTag;
         do {
             bestTag = GraphUtil.findBestTag(peaks);
 
             if (bestTag.length > 1) {
                 int bestCol = 0;
                 if (firstTag == null) {
                     firstTag = bestTag;
                 } else {
 
                     double score = 10E+30;
                     for (int col = -bestTag.length + 1; col < firstTag.length - 1; col++) {
                         int match = 0;
                         double total = 0;
                         for (int i = 0; i < bestTag.length; i++) {
                             int pos = col + i;
                             if (pos >= 0 && pos < firstTag.length) {
                                 match++;
                                 total += Math.abs(firstTag[pos].getValue() - bestTag[i].getValue());
                             }
                         }
                         double newScore = total / match;
                         if (newScore < score) {
                             score = newScore;
                             bestCol = col;
                         }
                     }
                 }
                 table.addTag(row, bestCol * 2, bestTag);
                 clearPath(bestTag);
                 row++;
             }
         } while (bestTag.length > 1);
         return table;
     }
 
     private void clearPath(Peak[] bestTag) {
         for (int i = 0; i < bestTag.length - 1; i++) {
             bestTag[i].removeNext(bestTag[i + 1]);
         }
     }
 
 }
