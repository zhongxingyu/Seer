 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package heartsim;
 
import heartsim.util.StringUtils;
 import java.awt.Point;
 import java.awt.Shape;
 import java.awt.geom.AffineTransform;
 import java.util.ArrayList;
 import java.util.Collections;
import java.util.HashMap;
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
    private Map<HeartTissue, Boolean> tissues = new HashMap<HeartTissue, Boolean>();
     private String tissueNames[][];
     private boolean cells[][];
     private boolean completed = false;
     private boolean initialised = false;
     private int progress = 0;
     private String tissueLoading = "None";
 
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
         // notify listeners that cell generation has started
         fireGenerationStarted();
 
         Thread thread = new Thread(new CellGeneratorRunnable());
         thread.setName("Cell generator");
         thread.start();
 
         // notify listeners that cell generation has finished
         fireGenerationCompleted();
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
 
     public void documentLoadingStarted(SVGDocumentLoaderEvent arg0)
     {
     }
 
     public void documentLoadingCompleted(SVGDocumentLoaderEvent arg0)
     {
         // new svg loaded so we are not initialised
         initialised = false;
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
             if (!initialised)
             {
                 loadTissues();
                 initialised = true;
             }
 
             createDataArray();
             completed = true;
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
 
                     for(HeartTissue t:tissues.keySet())
                     {
                         if(t.getName().equals(title))
                         {
                             t.getElements().add(element);
                             exists = true;
                             break;
                         }
                     }
 
                     if(!exists)
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
             fireGenerationStarted();
 
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
     }
 }
