 package view;
 
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import model.Animation.*;
 import model.Animation.Short;
 import model.Model;
 
 public class AnimationPanel extends JPanel {
 
     private static final long serialVersionUID = 1L;
 
     private Model model;
     private Animator animator;
     private GraphPanel graphPanel;
 
     private JButton pauseButton, stopButton;
     private JLabel animLabel, animSpeedLabel, nodeSizeLabel, nodeVertDistLabel, nodeMinHorizDistLabel;
     private JSlider animSpeedSlider, nodeSizeSlider, nodeVertDistSlider, nodeMinHorizDistSlider;
     private JCheckBox showNodeIDCheckbox;//, showHelplinesCheckbox;
     private JComboBox animComboBox;
 
     private int nodeSize = 20;
     private int nodeVertDist = 40;
     private int nodeMinHorizDist = 30;
 
     private AnimationProducer animationAlgorithm = new Short();
 
     //index of the current animation-type
 
     public void setModel(Model model) {
         this.model = model;
     }
 
     public void setAnimator(Animator animator) {
         this.animator = animator;
     }
 
     public AnimationPanel(GraphPanel graphPanel) {
         this.graphPanel = graphPanel;
         setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
 
         animLabel = new JLabel("  Animation: ");
         add(animLabel);
 
         String[] animOptions = {"none", "short", "top-down", "top-down + childrenStartAtParentPos", "bottom-up", "left to right", "right to left", "recursive", "recursive + siblingsCompleteFirst", "random", "rotating circle"};
         animComboBox = new JComboBox(animOptions);
         animComboBox.setSelectedItem("short");
         animComboBox.addActionListener(new ActionListener(){
             @Override
             public void actionPerformed(ActionEvent e) {
                 switch ((String) animComboBox.getSelectedItem()) {
                     case "none":
                         setAnimationAlgorithm(new SingleFinalFrame());
                         break;
                     case "short":
                         setAnimationAlgorithm(new Short());
                         break;
                     case "top-down":
                         setAnimationAlgorithm(new HorizontalTopDown());
                         break;
                     case "top-down + childrenStartAtParentPos":
                         setAnimationAlgorithm(new HorizontalTDChildrenStartAtParentPosition());
                         break;
                     case "bottom-up":
                         setAnimationAlgorithm(new HorizontalBottomUp());
                         break;
                     case "left to right":
                         setAnimationAlgorithm(new VerticalLeftToRight());
                         break;
                     case "right to left":
                         setAnimationAlgorithm(new VerticalRightToLeft());
                         break;
                     case "recursive":
                         setAnimationAlgorithm(new NodewiseRecursively());
                         break;
                     case "recursive + siblingsCompleteFirst":
                         setAnimationAlgorithm(new NodewiseRecSiblingsCompleteFirst());
                         break;
                     case "random":
                         setAnimationAlgorithm(new NodewiseRandom());
                         break;
                     case "rotating circle":
                         setAnimationAlgorithm(new RotateCircleTest());
                 }
             }
         });
         add(animComboBox);
 
         JLabel blankLabel = new JLabel("         ");
         add(blankLabel);
 
         pauseButton = new JButton("pause");
         pauseButton.setEnabled(false);
         pauseButton.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseReleased(MouseEvent e) {
                 if (pauseButton.isEnabled()) {
                     togglePauseContinue();
                 }
             }
         });
         add(pauseButton);
 
         stopButton = new JButton("stop");
         stopButton.setEnabled(false);
         stopButton.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseReleased(MouseEvent e) {
                 if (stopButton.isEnabled()) {
                     animator.finish();
                 }
             }
         });
         add(stopButton);
 
         animSpeedLabel = new JLabel(" speed:");
         add(animSpeedLabel);
 
         animSpeedSlider = new JSlider();
         animSpeedSlider.setValue(30);
         animSpeedSlider.setMinimum(0);
         animSpeedSlider.setMaximum(58);
         animSpeedSlider.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent ce) {
                 if (!animSpeedSlider.getValueIsAdjusting()) {
                     changeAnimSpeed();
                 }
             }
         });
         add(animSpeedSlider);
 
         nodeSizeLabel = new JLabel("  size: " + nodeSize);
         add(nodeSizeLabel);
 
         nodeSizeSlider = new JSlider();
         nodeSizeSlider.setValue(20);
         nodeSizeSlider.setMaximum(80);
         nodeSizeSlider.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent ce) {
                 changeParams();
             }
         });
         add(nodeSizeSlider);
 
         nodeVertDistLabel = new JLabel("vert dist: " + nodeVertDist);
         add(nodeVertDistLabel);
 
         nodeVertDistSlider = new JSlider();
         nodeVertDistSlider.setValue(40);
         nodeVertDistSlider.setMaximum(150);
         nodeVertDistSlider.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent ce) {
                 changeParams();
             }
         });
         add(nodeVertDistSlider);
 
         nodeMinHorizDistLabel = new JLabel("horiz dist: " + nodeMinHorizDist);
         add(nodeMinHorizDistLabel);
 
         nodeMinHorizDistSlider = new JSlider();
         nodeMinHorizDistSlider.setValue(30);
         nodeMinHorizDistSlider.setMaximum(150);
         nodeMinHorizDistSlider.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent ce) {
                 changeParams();
             }
         });
         add(nodeMinHorizDistSlider);
 
         showNodeIDCheckbox = new JCheckBox("IDs");
         showNodeIDCheckbox.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseReleased(MouseEvent e) {
                 if (showNodeIDCheckbox.isEnabled()) {
                     setShowNodeID();
                 }
             }
         });
         add(showNodeIDCheckbox);
 
 //        showHelplinesCheckbox = new JCheckBox("helplines");
 //        showHelplinesCheckbox.setEnabled(false);
 //        showHelplinesCheckbox.addMouseListener(new MouseAdapter() {
 //            @Override
 //            public void mouseReleased(MouseEvent e) {
 //                if (showHelplinesCheckbox.isEnabled()) {
 //                    setShowHelplines();
 //                } else {
 //                    JOptionPane.showMessageDialog(null, "helplines can only be displayed in the animation-mode 'nodewise'", "only in nodewise animation", JOptionPane.PLAIN_MESSAGE);
 //                }
 //            }
 //        });
 //        add(showHelplinesCheckbox);
     }
 
 
     private void changeParams() {
         nodeSize = nodeSizeSlider.getValue();
         nodeVertDist = nodeVertDistSlider.getValue();
         nodeMinHorizDist = nodeMinHorizDistSlider.getValue();
 
         nodeSizeLabel.setText("nodeSize: " + nodeSize);
         nodeVertDistLabel.setText("nodeVertDist: " + nodeVertDist);
         nodeMinHorizDistLabel.setText("nodeMinHorizDist: " + nodeMinHorizDist);
 
         model.changeNodeParams(nodeSize, nodeVertDist, nodeMinHorizDist);
         graphPanel.setNodeSizeAndNodeVertDist(nodeSize, nodeVertDist);
         graphPanel.setTimeline(model.getTimeline());
     }
 
     private void changeAnimSpeed() {
         animator.setSpeed(60 - animSpeedSlider.getValue());
     }
 
 
     private void setShowNodeID() {
         graphPanel.setShowNodeID(showNodeIDCheckbox.isSelected());
     }
 
 //    private void setShowHelplines() {
 //        graphPanel.setShowHelplines(showHelplinesCheckbox.isSelected());
 //    }
 
     private void togglePauseContinue() {
         if (animator.isPaused()) {
             pauseButton.setText("pause");
             animator.proceed();
         } else {
             pauseButton.setText("continue");
             animator.pause();
         }
     }
 
     private void setAnimationAlgorithm(AnimationProducer animationAlgorithm){
         model.setAnimationAlgorithm(animationAlgorithm);
         graphPanel.setTimeline(model.getTimeline());
     }
 
     public void enableSwitch(boolean onoff) {
         pauseButton.setEnabled(!onoff);
         stopButton.setEnabled(!onoff);
     }
 }
