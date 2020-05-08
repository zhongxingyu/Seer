 package org.geworkbench.algorithms;
 
 import org.geworkbench.analysis.AbstractSaveableParameterPanel;
 import org.geworkbench.util.TrainingProgressListener;
 import org.geworkbench.util.ClassifierException;
 import org.geworkbench.util.ProgressGraph;
 import org.geworkbench.util.TrainingTask;
 import org.geworkbench.util.threading.SwingWorker;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
 import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
 import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
 import org.geworkbench.bison.annotation.DSAnnotationContext;
 import org.geworkbench.bison.annotation.CSAnnotationContextManager;
 import org.geworkbench.bison.annotation.CSAnnotationContext;
 import org.geworkbench.bison.algorithm.classification.CSClassifier;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.swing.*;
 import java.io.Serializable;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.*;
 import java.util.ArrayList;
 
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.builder.DefaultFormBuilder;
 
 /**
  * A partial implementation of the interface for a machine learning algorithm.
  * Extending classes will add their own parameters to the form and generate their own validation classifiers.
  *
  * @author John Watkinson
  */
 public abstract class AbstractTrainingPanel extends AbstractSaveableParameterPanel implements Serializable, TrainingProgressListener {
     static Log log = LogFactory.getLog(AbstractTrainingPanel.class);
 
     public static final String DEFAULT_TRAINING_MESSAGE = "Training Progress";
     protected JFormattedTextField numberFolds = new JFormattedTextField();
     protected ProgressGraph progressGraph = new ProgressGraph(0, 20, 100);
     protected JLabel trainingMessage = new JLabel(DEFAULT_TRAINING_MESSAGE);
     protected JLabel falsePositives = new JLabel();
     protected JLabel falseNegatives = new JLabel();
     protected JLabel truePositives = new JLabel();
     protected JLabel trueNegatives = new JLabel();
     protected DSMicroarraySet maSet = null;
     private DSPanel<DSGeneMarker> selectionPanel;
     protected JButton crossTest = new JButton("Test via Cross Validation");
     protected JButton cancelTest = new JButton("Cancel Test");
     private SwingWorker worker;
     private TrainingTask trainingTask = null;
 
     protected void setTrainingStatus(String message) {
         trainingMessage.setText(DEFAULT_TRAINING_MESSAGE);
         truePositives.setText(message);
         truePositives.repaint();
         falsePositives.setText(message);
         falsePositives.repaint();
         trueNegatives.setText(message);
         trueNegatives.repaint();
         falseNegatives.setText(message);
         falseNegatives.repaint();
     }
 
     public int getNumberFolds() {
         return ((Number) numberFolds.getValue()).intValue();
     }
 
     public void setMaSet(DSMicroarraySet maSet) {
         this.maSet = maSet;
     }
 
     public DSMicroarraySet getMaSet() {
         return maSet;
     }
 
     public void setMarkerPanel(DSPanel<DSGeneMarker> selectionPanel) {
         this.selectionPanel = selectionPanel;
     }
 
     public DSItemList<DSGeneMarker> getActiveMarkers() {
         if (selectionPanel != null) {
             DSMicroarraySetView<DSGeneMarker, DSMicroarray> maView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(maSet);
             maView.setMarkerPanel(selectionPanel);
            DSPanel<DSGeneMarker> activeMarkers = maView.getMarkerPanel().activeSubset();
            if (activeMarkers.size() > 0) {
                return activeMarkers;
            } else {
                return maSet.getMarkers();
            }
         } else {
             return maSet.getMarkers();
         }
     }
 
     public void stepUpdate(float value) {
         progressGraph.addPoint((int) value);
         progressGraph.repaint();
     }
 
     public void stepUpdate(String message, float value) {
         progressGraph.setDescription(message);
         stepUpdate(value);
     }
 
     public void rebuildForm() {
         removeAll();
         FormLayout layout = new FormLayout(
                 "right:max(80dlu;pref), 3dlu, max(70dlu;pref), 3dlu, right:max(70dlu;pref), 3dlu, max(70dlu;pref)",
                 "");
         DefaultFormBuilder builder = new DefaultFormBuilder(layout);
         builder.setDefaultDialogBorder();
 
         addParameters(builder);
 
         builder.nextLine();
 
         builder.appendSeparator("Test Classifier Accuracy");
         builder.append("Number of Cross Validation Folds", numberFolds);
         builder.append("", crossTest);
         builder.append(trainingMessage, progressGraph);
         builder.append("", cancelTest);
         builder.nextLine();
         builder.appendSeparator("Cross Validation Results");
         builder.append("True Positives", truePositives);
         builder.append("False Positives", falsePositives);
         builder.append("True Negatives", trueNegatives);
         builder.append("False Negatives", falseNegatives);
 
         add(builder.getPanel());
         invalidate();
     }
 
     /**
      * Implementing classes will add their parameters to the form builder here.
      */
     protected abstract void addParameters(DefaultFormBuilder builder);
 
     protected void jbInit() throws Exception {
         initUI();
         numberFolds = new JFormattedTextField(3);
 
         crossTest.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
 
                 worker = new SwingWorker() {
                     int numTruePositives = 0, numFalseNegatives = 0, numFalsePositives = 0, numTrueNegatives = 0;
                     String errorString = null;
 
                     protected Object doInBackground() throws Exception {
                         truePositives.setText("Working...");
                         truePositives.repaint();
                         falsePositives.setText("Working...");
                         falsePositives.repaint();
                         trueNegatives.setText("Working...");
                         trueNegatives.repaint();
                         falseNegatives.setText("Working...");
                         falseNegatives.repaint();
 
                         DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager.getInstance().getCurrentContext(maSet);
 
                         DSItemList<DSGeneMarker> markers = getActiveMarkers();
 
                         java.util.List<float[]> caseData = new ArrayList<float[]>();
                         AbstractTraining.addMicroarrayData(context.getActivatedItemsForClass(CSAnnotationContext.CLASS_CASE), caseData, markers);
                         java.util.List<float[]> controlData = new ArrayList<float[]>();
                         AbstractTraining.addMicroarrayData(context.getActivatedItemsForClass(CSAnnotationContext.CLASS_CONTROL), controlData, markers);
                         java.util.List<float[]> testData = new ArrayList<float[]>();
                         AbstractTraining.addMicroarrayData(context.getActivatedItemsForClass(CSAnnotationContext.CLASS_TEST), testData, markers);
 
                         int numFolds = ((Number) numberFolds.getValue()).intValue();
                         KFoldCrossValidation cross = new KFoldCrossValidation(numFolds, caseData, controlData);
 
 
                         try {
                             for (int i = 0; i < cross.getNumFolds() && !isCancelled(); i++) {
                                 KFoldCrossValidation.CrossValidationData crossData = cross.getData(i);
                                 log.debug("Training classifier data set " + (i + 1) + "/" + numFolds);
                                 trainingMessage.setText(DEFAULT_TRAINING_MESSAGE + " (fold " + (i + 1) + "/" + numFolds + ")");
 
                                 java.util.List<float[]> trainingCaseData = crossData.getTrainingCaseData();
                                 java.util.List<float[]> trainingControlData = crossData.getTrainingControlData();
                                 CSClassifier classifier = trainForValidation(trainingCaseData, trainingControlData);
                                 log.debug("Classifier training complete.");
 
                                 int numPositive = 0;
                                 for (float[] values : crossData.getTestCaseData()) {
                                     if (classifier.classify(values) == 0) {
                                         numPositive++;
                                     }
                                 }
                                 numTruePositives += numPositive;
                                 numFalseNegatives += (crossData.getTestCaseData().size() - numPositive);
 
                                 numPositive = 0;
                                 for (float[] values : crossData.getTestControlData()) {
                                     if (classifier.classify(values) == 0) {
                                         numPositive++;
                                     }
                                 }
                                 numFalsePositives += numPositive;
                                 numTrueNegatives += (crossData.getTestControlData().size() - numPositive);
                             }
 
                             if (!isCancelled()) {
                                 log.debug("Results of " + numFolds + " fold analysis: ");
                                 log.debug("FP\tFN\tTP\tTN");
                                 log.debug(numFalsePositives + "\t" + numFalseNegatives + "\t" + numTruePositives + "\t" + numTrueNegatives);
                             } else {
                                 log.debug("Training cancelled.");
                             }
 
                         } catch (ClassifierException e1) {
                             log.error(e1);
                             errorString = e1.getMessage();
                             setTrainingStatus("Error");
                         } catch (Exception e) {
                             e.printStackTrace();
                             log.error(e);
                         }
 
                         return null;
                     }
 
                     public boolean cancel(boolean mayInterruptIfRunning) {
                         if (trainingTask != null) {
                             trainingTask.setCancelled(true);
                             trainingTask.setTrainingProgressListener(null);
                         }
                         setTrainingStatus("");
                         progressGraph.clearPoints();
                         progressGraph.setDescription("");
                         progressGraph.repaint();
                         return super.cancel(mayInterruptIfRunning);
                     }
 
                     public void done() {
                         if (errorString != null) {
                             JOptionPane.showMessageDialog(AbstractTrainingPanel.this, errorString);
                             setTrainingStatus("");
                         } else {
                             trainingMessage.setText(DEFAULT_TRAINING_MESSAGE);
                             truePositives.setText("" + numTruePositives);
                             truePositives.repaint();
                             falsePositives.setText("" + numFalsePositives);
                             falsePositives.repaint();
                             trueNegatives.setText("" + numTrueNegatives);
                             trueNegatives.repaint();
                             falseNegatives.setText("" + numFalseNegatives);
                             falseNegatives.repaint();
                         }
                         progressGraph.clearPoints();
                         progressGraph.repaint();
                     }
                 };
 
                 worker.execute();
             }
         });
 
         cancelTest.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (!worker.isDone()) {
                     worker.cancel(true);
                 }
             }
         });
 
         setLayout(new BorderLayout());
         rebuildForm();
     }
 
     /**
      * Implementing classes can initialize GUI elements here.
      */
     protected abstract void initUI();
 
     /**
      * Implementing classes will generate a classifier for the training data provided here.
      */
     protected abstract CSClassifier trainForValidation(java.util.List<float[]> trainingCaseData, java.util.List<float[]> trainingControlData) throws ClassifierException;
 
     public void setTrainingTask(TrainingTask trainingTask) {
         this.trainingTask = trainingTask;
         trainingTask.setTrainingProgressListener(AbstractTrainingPanel.this);
     }
 
 }
