 package gov.nih.nci.ncicb.cadsr.loader.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.*;
 
 import gov.nih.nci.ncicb.cadsr.loader.util.RunMode;
 import gov.nih.nci.ncicb.cadsr.loader.event.ReviewEvent;
 import gov.nih.nci.ncicb.cadsr.loader.event.ReviewEventType;
 import gov.nih.nci.ncicb.cadsr.loader.event.ReviewListener;
 
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.ReviewableUMLNode;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.UMLNode;
 
 import gov.nih.nci.ncicb.cadsr.loader.ui.event.NavigationEvent;
 import gov.nih.nci.ncicb.cadsr.loader.ui.event.NavigationListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import gov.nih.nci.ncicb.cadsr.loader.UserSelections;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class ApplyButtonPanel extends JPanel implements ActionListener, PropertyChangeListener
 {
 
   private JCheckBox reviewButton;
   private JButton applyButton;
 
   static final String SAVE = "APPLY", 
     REVIEW = "REVIEW";
 
 
   private List<ReviewListener> reviewListeners 
     = new ArrayList<ReviewListener>();
 
   private List<PropertyChangeListener> propChangeListeners 
     = new ArrayList<PropertyChangeListener>();
 
   private List<NavigationListener> navigationListeners 
     = new ArrayList<NavigationListener>();
 
   private Editable viewPanel;
   private ReviewableUMLNode node;
 
   private RunMode runMode = null;
 
   public ApplyButtonPanel(Editable viewPanel, ReviewableUMLNode node) {
     this.viewPanel = viewPanel;
     this.node = node;
 
     UserSelections selections = UserSelections.getInstance();
     
     runMode = (RunMode)(selections.getProperty("MODE"));
 
 
     applyButton = new JButton("Apply");
 
     if(runMode.equals(RunMode.Reviewer))
       reviewButton = new JCheckBox("<html> Model <br> Owner <br> Verified</html");
     else if(runMode.equals(RunMode.Curator))
       reviewButton = new JCheckBox("<html>Human<br>Verified</html>");
     else { 
       reviewButton = new JCheckBox("<html>Not<br>Available</html>");
       reviewButton.setVisible(false);
     }
 
     reviewButton.setSelected(node.isReviewed());
 
     reviewButton.setActionCommand(REVIEW);
     applyButton.setActionCommand(SAVE);
     applyButton.addActionListener(this);
     reviewButton.addActionListener(this);
 
     this.add(applyButton);
     this.add(reviewButton);
 
 
   }
 
   void update() {
     reviewButton.setSelected(node.isReviewed());
   }
 
   public void propertyChange(PropertyChangeEvent e) 
   {
     if(e.getPropertyName().equals(SAVE)) {
       setApplyButtonState((Boolean)e.getNewValue());
     } else if(e.getPropertyName().equals(REVIEW)) {
       reviewButton.setEnabled((Boolean)e.getNewValue());
     }
   }
 
   public void addNavigationListener(NavigationListener listener) 
   {
     navigationListeners.add(listener);
   }
 
   public void actionPerformed(ActionEvent evt) {
     AbstractButton button = (AbstractButton)evt.getSource();
 
     if(button.getActionCommand().equals(SAVE)) {
       try {
         viewPanel.applyPressed();
 
       } catch (ApplyException e){
       } // end of try-catch
     }
     ReviewEvent event = new ReviewEvent();
     event.setUserObject(node); 
     event.setReviewed(reviewButton.isSelected());
     
     if(runMode.equals(RunMode.Reviewer)) {
       event.setType(ReviewEventType.Owner);
     } else if (runMode.equals(RunMode.Curator)) {
       event.setType(ReviewEventType.Curator);
     } else return;
     
     fireReviewEvent(event);
 
     if(button.getActionCommand().equals(REVIEW)) {
       
       //if item is reviewed go to next item in the tree
       if(reviewButton.isSelected()) {
         NavigationEvent goToNext = new NavigationEvent(NavigationEvent.NAVIGATE_NEXT);
         fireNavigationEvent(goToNext);
       }
     }
   }
 
   void init(boolean reviewState) {
     setApplyButtonState(false);
 
     reviewButton.setEnabled(reviewState);
 
   }
 
   private void fireReviewEvent(ReviewEvent event) {
     for(ReviewListener l : reviewListeners)
       l.reviewChanged(event);
   }
 
   public void addReviewListener(ReviewListener listener) {
     reviewListeners.add(listener);
   }
 
   private void setApplyButtonState(boolean b) {
     applyButton.setEnabled(b);
 
     PropertyChangeEvent evt = new PropertyChangeEvent(this, SAVE, null, b);
     firePropertyChangeEvent(evt);
   }
 
   private void firePropertyChangeEvent(PropertyChangeEvent evt) {
     for(PropertyChangeListener l : propChangeListeners) 
       l.propertyChange(evt);
   }
 
   public void addPropertyChangeListener(PropertyChangeListener l) {
     propChangeListeners.add(l);
   }
 
 
   public void navigate(NavigationEvent evt) {  
     if(applyButton.isEnabled()) {
       if(JOptionPane.showConfirmDialog(this, "There are unsaved changes in this concept, would you like to apply the changes now?", "Unsaved Changes", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) 
       {
         try {
           viewPanel.applyPressed();
         } catch (ApplyException e){
         } // end of try-catch
      } else {
        applyButton.setEnabled(false);
       }
     }
   }
 
   private void fireNavigationEvent(NavigationEvent event) 
   {
     for(NavigationListener l : navigationListeners)
       l.navigate(event);
   }
 
 }
