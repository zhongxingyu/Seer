 package cs437.som.demo;
 
import cs437.som.util.SOMBuilderConfigPanel;
 import cs437.som.TrainableSelfOrganizingMap;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
import java.io.File;
 import java.io.IOException;
 import java.util.regex.Pattern;
 
 /**
  * A form for running multiple EdgeDetectors and comparing their output to a
  * reference image.
  */
 public class EdgeDetectionRunner implements PropertyChangeListener {
     private static final Pattern positiveInteger = Pattern.compile("[1-9]\\d*");
 
     private JRadioButton exhaustiveRadioButton;
     private JRadioButton sampledRadioButton;
     private JTextField iterationCountInput;
     private SOMBuilderConfigPanel mapConfig;
     private JComboBox<String> outputImageCmb;
     private JButton trainButton;
     private JButton runButton;
     private JPanel edgeDetectionRunnerForm;
     private JLabel referenceLabel;
     private JLabel outputLabel;
     private JFrame holdingFrame;
 
     private BufferedImage inputImage = null;
     private BufferedImage outputImage = null;
     private BufferedImage normalImage = null;
 
     private EdgeDetector ed = null;
 
     /**
      * Create a new EdgeDetectionRunner.
      */
     public EdgeDetectionRunner() {
         createUIComponents();
         validateForm();
         createHoldingFrame();
     }
 
     /**
      * Create an enclosing JFrame and show it.
      */
     private void createHoldingFrame() {
         holdingFrame = new JFrame();
         holdingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         holdingFrame.getContentPane().add(edgeDetectionRunnerForm);
         holdingFrame.pack();
         holdingFrame.setVisible(true);
     }
 
     /**
      * Set up listeners on the training style radio buttons.
      */
     private void setupTrainingRadios() {
         iterationCountInput.setEnabled(false);
         exhaustiveRadioButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (exhaustiveRadioButton.isSelected()) {
                     iterationCountInput.setEnabled(false);
                     trainButton.setEnabled(true);
                 }
             }
         });
 
         sampledRadioButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (sampledRadioButton.isSelected()) {
                     iterationCountInput.setEnabled(true);
                 }
             }
         });
     }
 
     /**
      * Validate the current input and (en-/)disable controls appropriately.
      */
     private void validateForm() {
         boolean mapValid = mapConfig.isValid();
         boolean iterationCountValid = validateIterationCount();
         boolean exhaustiveSelected = exhaustiveRadioButton.isSelected();
 
         boolean enableTrain = mapValid &&
                 (exhaustiveSelected || iterationCountValid);
 
         trainButton.setEnabled(enableTrain);
         runButton.setEnabled(ed != null);
     }
 
     /**
      * Validate the contents of {@code iterationCountInput} and update
      * its background color appropriately.
      *
      * @return True if the contents are valid; false otherwise.
      */
     private boolean validateIterationCount() {
         boolean iterationCountIsValid = false;
         if (positiveInteger.matcher(iterationCountInput.getText()).matches()) {
             iterationCountInput.setBackground(Color.WHITE);
             iterationCountIsValid = true;
         } else {
             iterationCountInput.setBackground(Color.RED);
         }
 
         return iterationCountIsValid;
     }
 
     /**
      * Create and train an EdgeDetector based on the form input.
      */
     private void trainMap() {
         if (exhaustiveRadioButton.isSelected()) {
             createExhaustiveSOM();
         } else {
             createSampleSOM();
         }
 
         runButton.setEnabled(true);
     }
 
     /**
      * Create and train an EdgeDetector that uses sampled input.
      */
     private void createSampleSOM() {
         int iterations = Integer.parseInt(iterationCountInput.getText());
         TrainableSelfOrganizingMap som = mapConfig.createSOM(9, iterations);
         ed = EdgeDetector.trainRandomlyFromMap(som, iterations);
     }
 
     /**
      * Create and train an EdgeDetector that uses exhaustive training.
      */
     private void createExhaustiveSOM() {
         int iterations = EdgeDetector.threeRaiseNine;
         TrainableSelfOrganizingMap som = mapConfig.createSOM(9, iterations);
         ed = EdgeDetector.trainExhaustivelyFromMap(som);
     }
 
     /**
      * Run the currently trained map and display the results.
      */
     private void runMap() {
         try {
            inputImage = ImageIO.read(new File("image.jpg"));
        } catch (IOException ignored) { }
 
         outputImage = ed.runOnImage(inputImage);
         normalImage = ed.normalizeImage(outputImage);
 
         displayImages();
         holdingFrame.pack();
     }
 
     /**
      * Display the input and output images, if they exist.
      */
     private void displayImages() {
         if (inputImage != null) {
             referenceLabel.setIcon(new ImageIcon(inputImage));
             displayOutputImage();
         }
     }
 
     /**
      * Display the output images, if they exist.
      */
     private void displayOutputImage() {
         if (outputImage == null || normalImage == null) {
             return;
         }
         if (outputImageCmb.getSelectedIndex() == 0) {
             outputLabel.setIcon(new ImageIcon(outputImage));
         } else {
             outputLabel.setIcon(new ImageIcon(normalImage));
         }
     }
 
     @Override
     public String toString() {
         return "EdgeDetectionRunner";
     }
 
     /**
      * Custom creation and initialization of UI components.
      */
     private void createUIComponents() {
         mapConfig.addPropertyChangeListener(this);
 
         setupTrainingRadios();
 
         iterationCountInput.addCaretListener(new CaretListener() {
             public void caretUpdate(CaretEvent e) {
                 validateIterationCount();
             }
         });
 
         trainButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) { trainMap(); }
         });
         runButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) { runMap(); }
         });
         outputImageCmb.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) { displayOutputImage(); }
         });
     }
 
     /**
      * Listen for validity changes on the SOMBuilderConfigPanel.
      *
      * {@inheritDoc}
      */
     public void propertyChange(PropertyChangeEvent evt) {
         if (evt.getOldValue() != evt.getNewValue()) {
             validateForm();
         }
     }
 
     public static void main(String[] args) {
         new EdgeDetectionRunner();
     }
 }
