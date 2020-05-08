 package org.osteo.ij.plugin;
 
 import ij.IJ;
 import ij.ImagePlus;
 import ij.gui.Overlay;
 import ij.gui.Roi;
 import ij.measure.Measurements;
 import ij.measure.ResultsTable;
 import ij.plugin.ContrastEnhancer;
 import ij.plugin.ImageCalculator;
 import ij.plugin.PlugIn;
 import ij.plugin.filter.Binary;
 import ij.plugin.filter.ParticleAnalyzer;
 import ij.plugin.filter.RankFilters;
 import ij.plugin.frame.RoiManager;
 import ij.process.AutoThresholder;
 import ij.process.ByteProcessor;
 import ij.process.ColorProcessor;
 import ij.process.ImageProcessor;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import org.osteo.ij.morph.GrayMorphology_;
 import trainableSegmentation.WekaSegmentation;
 
 /**
  * Supplies all the features for classifying and quantifying osteoclast images.
  *
  * @author David Rueda
  */
 public class Osteoclasts_ extends AbstractOsteoclasts implements PlugIn {
 
     private final String mfRadiusName = "Median filter radius";
     private final String mfRepeatName = "Median filter repetitions";
     private final String paMinSizeName = "Particle analyzer min size";
     private final String paMinCircName = "Particle analyzer min circularity";
     private final String paMaxCircName = "Particle analyzer max circularity";
     private final String incBright = "Increase Brightness";
     /**
      * Little window with a few buttons.
      */
     private static JFrame miniWin;
     private JPanel miniWinInfosPanel;
     /**
      * One unique color for managing overlays.
      */
     public static final Color OVERLAY_COLOR = Color.magenta;
     /**
      * Simulate infinity.
      */
     protected final static double INFINITY = 1.0 / 0.0;
 
     /**
      * Basic constructor.
      */
     public Osteoclasts_() {
     }
 
     /**
      * Run the osteoclast counting plugin.
      *
      * @param ip
      */
     @Override
     public void run(String arg) {
         if (miniWin == null) {
             runMiniWin();
             logToMiniWin("Welcome Ana! ;)");
         } else {
             miniWin.setVisible(true);
             miniWin.requestFocus();
         }
     }
 
     /**
      * Several actions that the plugin allows the user to do.
      */
     private enum Actions {
 
         PA("Analyze Particles", "compute the final results and save them in a csv file"),
         CLASS("Run Classifier", "analyze the original images"),
         OVERLAYS("Get Overlay", "analyze classified images"),
         RM_OVERLAYS("Reset Overlay", "delete all ROIs in the selected image");
         private String name;
         private String desc;
 
         public String getDesc() {
             return this.desc;
         }
 
         public String getName() {
             return this.name;
         }
 
         Actions(String name, String desc) {
             this.name = name;
             this.desc = desc;
         }
     }
 
     /**
      * Close the little user interface.
      */
     private void closeMiniWin() {
         if (miniWin != null) {
             miniWin.setVisible(false);
         }
         miniWin = null;
     }
 
     /**
      * Display little message into little window.
      *
      * @param message
      */
     private void logToMiniWin(final String message) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 Osteoclasts_.this.miniWinInfosPanel.add(new JLabel(message));
                 Osteoclasts_.this.miniWinInfosPanel.updateUI();
                 miniWin.pack();
             }
         });
     }
 
     /**
      * Open the plugin's window.
      */
     private void runMiniWin() {
         miniWin = new JFrame();
         miniWin.setLayout(new BorderLayout());
         miniWin.setVisible(true);
         miniWin.setTitle("ღosteoclastsღ");
 
         final JPanel actionsPanel = new JPanel(new GridBagLayout());
         GridBagConstraints c = new GridBagConstraints();
         c.gridwidth = GridBagConstraints.REMAINDER;
         c.fill = GridBagConstraints.HORIZONTAL;
 
         ActionListener actionListener = new ActionListener() {
             public void actionPerformed(ActionEvent ae) {
                 JButton source = (JButton) ae.getSource();
                 ImageOperationsWorker iow = new ImageOperationsWorker(Osteoclasts_.this);
 
                 if (source.getText().equals(Actions.PA.getName())) {
                     iow.setMethodToInvoke("pa");
                 } else if (source.getText().equals(Actions.CLASS.getName())) {
                     iow.setMethodToInvoke("classify");
                 } else if (source.getText().equals(Actions.OVERLAYS.getName())) {
                     iow.setMethodToInvoke("overlays");
                 } else if (source.getText().equals(Actions.RM_OVERLAYS.getName())) {
                     iow.setMethodToInvoke("rmOverlays");
                 }
 
                 iow.execute();
             }
         };
 
         for (Actions action : Actions.values()) {
             JButton actionButton = new JButton(action.getName());
             actionButton.setToolTipText(action.getDesc());
             actionsPanel.add(actionButton, c);
             actionButton.addActionListener(actionListener);
         }
 
         miniWinInfosPanel = new JPanel();
 
         miniWin.getContentPane().add(miniWinInfosPanel, BorderLayout.NORTH);
         miniWin.getContentPane().add(actionsPanel, BorderLayout.CENTER);
         miniWin.pack();
     }
 
     private boolean loadClassifier(WekaSegmentation weka, String classifierPath)
             throws Exception {
         if (classifierPath == null || classifierPath.isEmpty()) {
             throw new Exception("No classifier file specified!");
         }
 
         IJ.log("Loading Weka classifier from " + classifierPath + "...");
         // Try to load Weka model (classifier and train header)
         System.gc();
         if (!weka.loadClassifier(classifierPath)) {
             throw new Exception(
                     "Error when loading Weka classifier from file");
         }
 
         IJ.log("Read header from " + classifierPath
                 + " (number of attributes = "
                 + weka.getTrainHeader().numAttributes() + ")");
         if (weka.getTrainHeader().numAttributes() < 1) {
             throw new Exception(
                     "Error: No attributes were found on the model header");
         }
 
         IJ.log("Loaded " + classifierPath);
         return true;
     }
 
     /**
      * Run the classifier on selected image.
      */
     void classify() {
         try {
             ImagePlus imp = getCurrentImp();
 
             String classifierPath = IJ.getFilePath("Tell me please, where the classifier is.");
 
             if (imp.getProcessor().getNChannels() != 3) {
                 IJ.error("The script can be processed only on RGB images.");
                 return;
             }
 
             WekaSegmentation weka = new WekaSegmentation();
 //            weka.setTrainingImage(imp);
             if (loadClassifier(weka, classifierPath)) {
                 weka.applyClassifier(0, true);
                 ImagePlus result = weka.getClassifiedImage();
 
 
                 String path = IJ.getFilePath("Where should the results be saved?");
                 if (path != null) {
                     File file = new File(path);
 
                     if (file.exists()) {
                         boolean nonetheless = IJ.showMessageWithCancel(
                                 "Save results...",
                                 "\"" + file.getName() + "\" already exists.\nDo you want to replace it?");
                         if (nonetheless) {
                             IJ.saveAs(result, "Tiff", path);
                         }
                     } else {
                         IJ.saveAs(result, "Tiff", path);
                     }
                 }
             }
         } catch (Exception ex) {
             IJ.error(ex.getMessage());
         }
     }
     
     /**
      * Will remove the overlay for the selected image.
      */
     void rmOverlays() {
         ImagePlus imp = getCurrentImp();
         imp.setOverlay(null);
     }
 
     /**
      * Analyze probability image.
      *
      * @param workingImg
      * @param os
      * @return
      */
     private ImagePlus applyIPP(ImagePlus workingImg, OptionSet os) {
         RankFilters rf = new RankFilters();
 
         workingImg.getStack().deleteLastSlice();
         workingImg.setProcessor(workingImg.getProcessor().convertToByte(true));
         ImageProcessor workingProcessor = workingImg.getProcessor();
 
         // increase brightness in working image
         if (os.getOption(incBright).isSelected()) {
             // close 5px
             GrayMorphology_ gm = new GrayMorphology_();
 //            gm.setup("radius=5 type=circle operator=close", workingImg);
             GrayMorphology_.radius = 5f;
             GrayMorphology_.options = 0;
             GrayMorphology_.showoptions = false;
             GrayMorphology_.morphoptions = 3;
             gm.imp = workingImg;
             gm.run(workingProcessor);
             // median filter
             Double n = Double.parseDouble(os.getOptionValue(mfRepeatName));
             Integer medRadius = Integer.parseInt(os.getOptionValue(mfRadiusName).split("\\.")[0]);
             for (int i = 0; i < n; i++) {
                 rf.rank(workingProcessor, medRadius, RankFilters.MEDIAN);
             }
         }
 
         ImagePlus edgesImg = workingImg.duplicate();
         ImageProcessor edgesProcessor = edgesImg.getProcessor();
 
         // find edges & sharpen
         edgesProcessor.findEdges();
 
         // subtract
         ImageCalculator imgCalc = new ImageCalculator();
         ImagePlus finalImg = imgCalc.run("Subtract create", workingImg, edgesImg);
         ImageProcessor finalProcessor = finalImg.getProcessor();
 
         // post processing
         rf.rank(edgesProcessor, 5d, RankFilters.MEDIAN);
 
         // enhance contrast
         ContrastEnhancer ce = new ContrastEnhancer();
         ce.stretchHistogram(finalImg, 0.35);
 
         // auto threshold
         finalProcessor.setAutoThreshold(AutoThresholder.Method.Minimum, true);
         finalProcessor.autoThreshold();
         finalProcessor.invert();
 
         // close & fill holes
         Binary binary = new Binary();
         binary.setup("close", finalImg);
         binary.run(finalProcessor);
         binary.setup("fill", finalImg);
         binary.run(finalProcessor);
 
         // analyze particles
         ResultsTable paResults = new ResultsTable();
         ParticleAnalyzer particleAnalyzer = new ParticleAnalyzer(
                 ParticleAnalyzer.IN_SITU_SHOW
                 | ParticleAnalyzer.INCLUDE_HOLES
                 | ParticleAnalyzer.SHOW_OVERLAY_OUTLINES,
                 Measurements.AREA | Measurements.MEAN
                 | Measurements.MIN_MAX | Measurements.CENTROID
                 | Measurements.PERIMETER
                 | Measurements.SHAPE_DESCRIPTORS,
                 paResults,
                 Double.parseDouble(os.getOptionValue(paMinSizeName)),
                 INFINITY,
                 Double.parseDouble(os.getOptionValue(paMinCircName)),
                 Double.parseDouble(os.getOptionValue(paMaxCircName)));
         particleAnalyzer.analyze(finalImg);
 
         return finalImg;
     }
 
     /**
      * Get the overlays. Analyze probability images, which are the results of
      * the classification.
      */
     void overlays() {
         ImagePlus imp = getCurrentImp();
         Overlay o = imp.getOverlay() == null ? new Overlay() : imp.getOverlay();
 
         OptionSet os = new OptionSet();
         Option mf_radius = new Option(
                 mfRadiusName, Option.Type.STRING);
         mf_radius.setCurrentValue("5");
         Option mf_repets = new Option(
                 mfRepeatName, Option.Type.STRING);
         mf_repets.setCurrentValue("4");
         Option pa_minSize = new Option(
                 paMinSizeName, Option.Type.STRING);
         pa_minSize.setCurrentValue("200");
         Option pa_minCirc = new Option(
                 paMinCircName, Option.Type.STRING);
         pa_minCirc.setCurrentValue("0.00001");
         Option pa_maxCirc = new Option(
                 paMaxCircName, Option.Type.STRING);
         pa_maxCirc.setCurrentValue("0.8");
         Option inc_bright = new Option(
                 incBright, Option.Type.STRING);
         inc_bright.setSelected(false);
 
         os.add(mf_radius);
         os.add(mf_repets);
         os.add(pa_minSize);
         os.add(pa_minCirc);
         os.add(pa_maxCirc);
         os.add(inc_bright);
 
         ImagePlus maskResult = applyIPP(imp.duplicate(), os);
         imp.setOverlay(maskResult.getOverlay());
     }
 
     /**
      * After classification is corrected (by hand), runs the particle analysis
      * and save the results in a csv file.
      */
     void pa() {
         ImagePlus imp = getCurrentImp();
         Overlay o = imp.getOverlay();
 
         RoiManager rm = new RoiManager(true);
         for (int i = 0; i < o.size(); i++) {
             rm.addRoi(o.get(i));
         }
 
         byte[] maskBytes = new byte[imp.getWidth() * imp.getHeight()];
         Arrays.fill(maskBytes, (byte) 255);
         ImagePlus mask = new ImagePlus("mask", new ByteProcessor(imp.getWidth(), imp.getHeight(), maskBytes));
 
         for (Roi roi : rm.getRoisAsArray()) {
             mask.getProcessor().setColor(Color.BLACK);
             mask.getProcessor().fill(roi);
         }
 
         // analyze particles
         ResultsTable paResults = new ResultsTable();
         ParticleAnalyzer particleAnalyzer = new ParticleAnalyzer(
                 ParticleAnalyzer.IN_SITU_SHOW
                 | ParticleAnalyzer.INCLUDE_HOLES
                 | ParticleAnalyzer.SHOW_NONE,
                 Measurements.AREA | Measurements.MEAN
                 | Measurements.MIN_MAX | Measurements.CENTROID
                 | Measurements.PERIMETER
                 | Measurements.SHAPE_DESCRIPTORS,
                 paResults,
                 0d, INFINITY, 0d, 1d);
         particleAnalyzer.analyze(mask);
         try {
             String path = IJ.getFilePath("Where should the results be saved?");
             if (path != null) {
                 File file = new File(path);
 
                 if (file.exists()) {
                     boolean nonetheless = IJ.showMessageWithCancel(
                             "Save results...",
                             "\"" + file.getName() + "\" already exists.\nDo you want to replace it?");
                     if (nonetheless) {
                         paResults.saveAs(path);
                     }
                 } else {
                     paResults.saveAs(path);
                 }
             }
         } catch (IOException ex) {
             IJ.log("Unable to save the results file.");
             log(ex.getMessage());
         }
     }
 
     /**
      * Color threshold. It's not working well... It actually is under
      * implementation... :D Since I don't need it for the plugin, I won't
      * continue developing this method until I have to.
      *
      * @param impLocal
      * @param color
      * @return
      */
     private ImagePlus threshold(ImagePlus impLocal, Color color) {
         int[] min = new int[3];
         int[] max = new int[3];
         String[] filter = new String[3];
 
         impLocal.show();
 
         ColorProcessor cpLocal = ((ColorProcessor) impLocal.getProcessor());
         ImagePlus[] channels = new ImagePlus[3];
         for (int i = 0; i < 3; i++) {
             ByteProcessor bp = new ByteProcessor(impLocal.getWidth(), impLocal.getHeight(), cpLocal.getChannel(i));
             channels[i] = new ImagePlus("channel " + i, bp);
             channels[i].show();
         }
 
         min[0] = color.getRed();
         max[0] = 255;
         filter[0] = "pass";
         min[1] = color.getGreen();
         max[1] = 255;
         filter[1] = "pass";
         min[2] = color.getBlue();
         max[2] = 255;
         filter[2] = "pass";
 
         for (int i = 0; i < 3; i++) {
             ImagePlus channel = channels[0];
             ImageProcessor channelProcessor = channel.getProcessor();
             channelProcessor.setThreshold(min[i], max[i], ImageProcessor.BLACK_AND_WHITE_LUT);
             channelProcessor.threshold(min[i]);
 
             if (filter[i].equals("stop")) {
                 IJ.run("Invert");
             }
         }
 
         ImageCalculator ic = new ImageCalculator();
         ImagePlus binary = ic.run("AND create", channels[0], channels[1]);
         binary = ic.run("AND create", binary, channels[2]);
 
         binary.show();
         return binary;
     }
 
     /**
      * Log message into console.
      *
      * @param message
      */
     private void log(String message) {
         System.out.println(message);
     }
 }
