 package org.concord.datagraph.analysis.ui;
 
 import java.awt.EventQueue;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import javax.swing.JLabel;
 
 import org.concord.datagraph.analysis.Graph;
 import org.concord.datagraph.analysis.GraphAnalyzer;
 import org.concord.datagraph.analysis.GraphAnalyzer.AnnotationStyle;
 import org.concord.datagraph.analysis.GraphAnalyzerProvider;
 import org.concord.datagraph.analysis.GraphAnalyzerProvider.Type;
 import org.concord.datagraph.analysis.rubric.GraphRubric;
 import org.concord.datagraph.analysis.rubric.ResultSet;
 import org.concord.datagraph.state.OTDataCollector;
 import org.concord.datagraph.state.OTDataGraphable;
 import org.concord.graph.util.state.OTHideableAnnotation;
 
 public class ScoreLabel extends JLabel {
     private static final long serialVersionUID = 1L;
     private static final Logger logger = Logger.getLogger(ScoreLabel.class.getName());
     private static GraphAnalyzer graphAnalyzer;
     private OTDataCollector dataCollector;
     private OTDataGraphable graphable;
     private ResultSet results;
     private HashMap<GraphAnalyzer.AnnotationStyle, ArrayList<OTHideableAnnotation>> graphAnalysisAnnotations = new HashMap<GraphAnalyzer.AnnotationStyle, ArrayList<OTHideableAnnotation>>();
     private ArrayList<OTDataGraphable> graphAnalysisSegments = new ArrayList<OTDataGraphable>();
     private boolean annotationsVisible = false;
     private boolean annotations2Visible = false;
     private boolean segmentsVisible = false;
     private Graph segments;
 
     public ScoreLabel(OTDataCollector dataCollector) {
         this.dataCollector = dataCollector;
         this.graphable = dataCollector.getSource();
         
         addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent paramMouseEvent) {
                 if (graphAnalyzer != null) {
                     graphAnalyzer.displayHtmlReasonsPopup(ScoreLabel.this, results);
                 }
             }
         });
 
         setText("...");
 
         Thread t = new Thread(new Runnable() {
             public void run() {
                 graphAnalyzer = GraphAnalyzerProvider.findAnalyzer(Type.ANY);
 
                 if (graphAnalyzer == null) {
                     logger.severe("Couldn't get a GraphAnalyzer!!!");
                 } else {
                     EventQueue.invokeLater(new Runnable() {
                         public void run() {
                             calculateScore();
                         }
                     });
                 }
             }
         });
         t.start();
     }
 
     public void setGraphAnalysisAnnotationsVisible(boolean show, AnnotationStyle style) {
         switch (style) {
         case ONE:
             annotationsVisible = show;
             break;
         case TWO:
             annotations2Visible = show;
             break;
         default:
             break;
         }
         refreshAnnotations(style, show);
     }
     
     public void setGraphAnalysisSegmentsVisible(boolean show) {
         segmentsVisible = show;
         refreshSegments();
     }
 
     public void calculateScore() {
         // Need graph analysis service...
         if (graphAnalyzer != null && graphable.getRubric().size() > 0) {
             clearAnnotations(AnnotationStyle.ONE);
             clearAnnotations(AnnotationStyle.TWO);
             clearSegments();
             
             segments = graphAnalyzer.getSegments(graphable.getDataStore(), 0, 1, graphable.getSegmentingTolerance());
             GraphRubric rubric = graphAnalyzer.buildRubric(graphable.getRubric());
             results = graphAnalyzer.compareGraphs(rubric, segments);
             double scorePct = results.getScorePercent();
             setText(String.format("%2.0f%%", scorePct));
 
             setForeground(results.getResultColor());
             
             refreshAnnotations(AnnotationStyle.ONE, annotationsVisible);
             refreshAnnotations(AnnotationStyle.TWO, annotations2Visible);
         } else {
             setText("??");
         }
         repaint();
     }
     
     public ResultSet getResults() {
         return results;
     }
     
     public Graph getSegments() {
         return segments;
     }
     
     private void refreshAnnotations(AnnotationStyle style, boolean visible) {
         ArrayList<OTHideableAnnotation> annotations = graphAnalysisAnnotations.get(style);
         if (annotations != null && annotations.size() > 0) {
             for (OTHideableAnnotation ann : annotations) {
                 ann.setVisible(visible);
             }
        } else if (visible && results != null) {
             annotations = graphAnalyzer.annotateResults(dataCollector, results, style);
             graphAnalysisAnnotations.put(style, annotations);
         }
     }
     
     private void clearAnnotations(AnnotationStyle style) {
         ArrayList<OTHideableAnnotation> annotations = graphAnalysisAnnotations.get(style);
         if (annotations != null && annotations.size() > 0) {
             for (OTHideableAnnotation ann : annotations) {
                 ann.setVisible(false);
                 dataCollector.getLabels().remove(ann);
             }
             annotations.clear();
         }
     }
     
     private void refreshSegments() {
         if (graphAnalysisSegments != null && graphAnalysisSegments.size() > 0) {
             for (OTDataGraphable seg : graphAnalysisSegments) {
                 seg.setVisible(segmentsVisible);
             }
         } else if (segmentsVisible && results != null) {
             graphAnalysisSegments = graphAnalyzer.drawSegmentResults(dataCollector, segments);
         }
     }
     
     private void clearSegments() {
         if (graphAnalysisSegments != null && graphAnalysisSegments.size() > 0) {
             for (OTDataGraphable seg : graphAnalysisSegments) {
                 seg.setVisible(false);
                 dataCollector.getGraphables().remove(seg);
             }
             graphAnalysisSegments.clear();
         }
     }
 }
