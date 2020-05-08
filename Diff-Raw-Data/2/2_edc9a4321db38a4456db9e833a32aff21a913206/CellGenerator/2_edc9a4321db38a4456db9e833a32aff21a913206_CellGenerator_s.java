 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package heartsim;
 
 import java.awt.Point;
 import java.awt.Shape;
 import java.awt.geom.AffineTransform;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import org.apache.batik.gvt.GraphicsNode;
 import org.apache.batik.swing.JSVGCanvas;
 import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
 import org.apache.batik.swing.svg.SVGDocumentLoaderListener;
 import org.apache.batik.util.SVGConstants;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 /**
  *
  * @author Lee Boynton
  */
 public class CellGenerator implements SVGDocumentLoaderListener
 {
     private List<CellGeneratorListener> listeners = Collections.synchronizedList(new ArrayList<CellGeneratorListener>());
     private JSVGCanvas canvas;
     private Map<HeartTissue, Boolean> tissues = new LinkedHashMap<HeartTissue, Boolean>();
     private String tissueNames[][];
     private boolean cells[][];
     private boolean completed = false;
     private boolean initialised = false;
     private int progress = 0;
     private String tissueLoading = "None";
     private int stimulusRow = 0;
     private int stimulusCol = 0;
     private boolean stimulusLocationSet;
 
     public CellGenerator(JSVGCanvas canvas)
     {
         this.canvas = canvas;
         canvas.addSVGDocumentLoaderListener(this);
     }
 
     public int getProgress()
     {
         return progress;
     }
 
     public boolean isCompleted()
     {
         return completed;
     }
 
     public String getTissueLoading()
     {
         return tissueLoading;
     }
 
     public void addGeneratorListener(CellGeneratorListener listener)
     {
         listeners.add(listener);
     }
 
     public void removeGeneratorListener(CellGeneratorListener listener)
     {
         if (listeners.contains(listener))
         {
             listeners.remove(listener);
         }
     }
 
     public void fireGenerationStarted()
     {
         for (CellGeneratorListener l : listeners)
         {
             l.cellGenerationStarted();
         }
     }
 
     public void fireGenerationCompleted()
     {
         for (CellGeneratorListener l : listeners)
         {
             l.cellGenerationCompleted();
         }
     }
 
     public boolean[][] getCells()
     {
         return cells;
     }
 
     public void run()
     {
         Thread thread = new Thread(new CellGeneratorRunnable());
         thread.setName("Cell generator");
         thread.start();
     }
 
     public List<HeartTissue> getTissues()
     {
        return new ArrayList(tissues.keySet());
     }
 
     public boolean isEnabled(HeartTissue tissue)
     {
         return tissues.get(tissue);
     }
 
     public String[][] getTissueNames()
     {
         return tissueNames;
     }
 
     public void disableTissue(HeartTissue tissue)
     {
         tissues.put(tissue, false);
         run();
     }
 
     public void enableTissue(HeartTissue tissue)
     {
         tissues.put(tissue, true);
         run();
     }
 
     public int getStimulusColumn()
     {
         return stimulusCol;
     }
 
     public int getStimulusRow()
     {
         return stimulusRow;
     }
 
     public boolean isStimulusLocationSet()
     {
         return stimulusLocationSet;
     }
 
     public void documentLoadingStarted(SVGDocumentLoaderEvent arg0)
     {
     }
 
     public void documentLoadingCompleted(SVGDocumentLoaderEvent arg0)
     {
         // new svg loaded so we are not initialised
         initialised = false;
         stimulusLocationSet = false;
     }
 
     public void documentLoadingCancelled(SVGDocumentLoaderEvent arg0)
     {
     }
 
     public void documentLoadingFailed(SVGDocumentLoaderEvent arg0)
     {
     }
 
     public class CellGeneratorRunnable implements Runnable
     {
         public void run()
         {
             // notify listeners that cell generation has started
             fireGenerationStarted();
 
             if (!initialised)
             {
                 loadTissues();
                 initialised = true;
             }
 
             createDataArray();
             completed = true;
 
             // notify listeners that cell generation has finished
             fireGenerationCompleted();
         }
 
         private void loadTissues()
         {
             tissues.clear();
 
             NodeList nodes = canvas.getSVGDocument().getElementsByTagName(SVGConstants.SVG_PATH_TAG);
 
             int i = 0;
             while (nodes.item(i) != null)
             {
 
                 Element element = (Element) nodes.item(i);
 
                 NodeList titles = element.getElementsByTagName(SVGConstants.SVG_TITLE_TAG);
 
                 // get the first title
                 if (titles.item(0) != null)
                 {
                     String title = titles.item(0).getTextContent();
 
                     boolean exists = false;
 
                     for (HeartTissue t : tissues.keySet())
                     {
                         if (t.getName().equals(title))
                         {
                             t.getElements().add(element);
                             exists = true;
                             break;
                         }
                     }
 
                     if (!exists)
                     {
                         HeartTissue tissue = new HeartTissue(title);
                         tissue.getElements().add(element);
                         tissues.put(tissue, true);
                     }
                 }
 
                 i++;
             }
         }
 
         // TODO: Split up
         private void createDataArray()
         {
             cells = new boolean[canvas.getPreferredSize().height][canvas.getPreferredSize().width];
             tissueNames = new String[canvas.getPreferredSize().height][canvas.getPreferredSize().width];
 
             for (HeartTissue tissue : tissues.keySet())
             {
                 if (!tissues.get(tissue))
                 {
                     Application.getInstance().output(tissue.getName() + " is disabled, skipping");
                     continue;
                 }
 
                 tissueLoading = tissue.getName();
 
                 Application.getInstance().output("Generating cells for " + tissueLoading);
 
                 for (Element element : tissue.getElements())
                 {
                     GraphicsNode node = canvas.getUpdateManager().getBridgeContext().getGraphicsNode(element);
 
                     if (node != null)
                     {
                         AffineTransform elementsAt = node.getGlobalTransform();
                         Shape selectionHighlight = node.getOutline();
                         AffineTransform at = canvas.getRenderingTransform();
                         at.concatenate(elementsAt);
                         Shape s = at.createTransformedShape(selectionHighlight);
 
                         if (s == null)
                         {
                             break;
                         }
 
                         if (tissue.getProfile().getName().equals("Sinoatrial node"))
                         {
                             determineStimulusCell(s);
                         }
 
                         for (int row = 0; row < canvas.getPreferredSize().height; row++)
                         {
                             for (int col = 0; col < canvas.getPreferredSize().width; col++)
                             {
                                 if (s.contains(new Point(col, row)))
                                 {
                                     cells[row][col] = true;
                                     tissueNames[row][col] = tissueLoading;
                                 }
                             }
 
                             // row completed
                             progress = (int) (((row + 1) / (double) canvas.getPreferredSize().height) * 100);
                         }
                     }
                 }
             }
 
             fireGenerationCompleted();
         }
 
         private void determineStimulusCell(Shape s)
         {
             stimulusLocationSet = true;
 
             stimulusRow = (int) (((s.getBounds().getMaxY() -
                     s.getBounds().getMinY()) / 2) + s.getBounds().getMinY()) + 2;
 
             stimulusCol = (int) (((s.getBounds().getMaxX() -
                     s.getBounds().getMinX()) / 2) + s.getBounds().getMinX()) + 2;
 
             Application.getInstance().output("Set stimulus location to row " +
                     stimulusRow + " column " + stimulusCol);
         }
     }
 }
