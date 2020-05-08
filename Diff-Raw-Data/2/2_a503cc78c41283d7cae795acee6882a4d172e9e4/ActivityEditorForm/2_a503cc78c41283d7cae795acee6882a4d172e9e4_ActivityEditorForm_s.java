 package de.aidger.view.forms;
 
 import static de.aidger.utils.Translation._;
 
 import java.awt.GridBagConstraints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Date;
 import java.util.List;
 import java.util.Vector;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import de.aidger.model.models.Activity;
 import de.aidger.model.models.Assistant;
 import de.aidger.model.models.Course;
 import de.aidger.view.models.ComboBoxModel;
 import de.aidger.view.models.UIAssistant;
 import de.aidger.view.models.UICourse;
 import de.aidger.view.tabs.ViewerTab.DataType;
 import de.aidger.view.utils.AutoCompletion;
 import de.aidger.view.utils.InputPatternFilter;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import de.unistuttgart.iste.se.adohive.model.IAssistant;
 import de.unistuttgart.iste.se.adohive.model.ICourse;
 
 /**
  * A form used for editing / creating new activities.
  * 
  * @author aidGer Team
  */
 @SuppressWarnings("serial")
 public class ActivityEditorForm extends JPanel {
 
     /**
      * A flag whether the form is in edit mode.
      */
     private boolean editMode = false;
 
     /**
      * Constructs an activity editor form.
      * 
      * @param activity
      *            The activity that will be edited
      */
     public ActivityEditorForm(Activity activity) {
         if (activity != null) {
             editMode = true;
         }
 
         initComponents();
 
         InputPatternFilter.addFilter(txtProcessor, ".{0,2}");
 
         txtProcessor
             .setToolTipText(_("Only a maximal length of 2 is allowed."));
 
         addNewActivity();
 
         if (editMode) {
             spDate.setValue(activity.getDate());
             txtSender.setText(activity.getSender());
             txtProcessor.setText(activity.getProcessor());
             txtType.setText(activity.getType());
             txtDocumentType.setText(activity.getDocumentType());
             txtContent.setText(activity.getContent());
             txtRemark.setText(activity.getRemark());
 
             ActivityLine al = activityLines.get(0);
 
             try {
                 ICourse c = (new Course()).getById(activity.getCourseId());
                 UICourse course = (c == null) ? new UICourse()
                         : new UICourse(c);
                 al.cmbCourse.setSelectedItem(course);
 
                 IAssistant a = (new Assistant()).getById(activity
                     .getAssistantId());
                 UIAssistant assistant = (a == null) ? new UIAssistant()
                         : new UIAssistant(a);
                 al.cmbAssistant.setSelectedItem(assistant);
             } catch (AdoHiveException e) {
             }
 
         }
     }
 
     /**
      * Adds a new activity line to the form.
      */
     @SuppressWarnings("unchecked")
     private void addNewActivity() {
         GridBagConstraints gridBagConstraints;
 
         JLabel lblCourse = new JLabel();
         lblCourse.setText(_("Course"));
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(lblCourse, gridBagConstraints);
 
         JComboBox cmbCourse = new JComboBox();
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(cmbCourse, gridBagConstraints);
 
         JLabel lblAssistant = new JLabel();
         lblAssistant.setText(_("Assistant"));
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
         gridBagConstraints.anchor = GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 50, 10, 10);
         add(lblAssistant, gridBagConstraints);
 
         JComboBox cmbAssistant = new JComboBox();
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(cmbAssistant, gridBagConstraints);
 
         JButton btnPlusMinus = new JButton();
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 4;
 
         AutoCompletion.enable(cmbAssistant);
         AutoCompletion.enable(cmbCourse);
 
         try {
             List<IAssistant> assistants = (new Assistant()).getAll();
 
             ComboBoxModel cmbAssistantModel = new ComboBoxModel(
                 DataType.Assistant);
 
             cmbAssistantModel.addElement(new UIAssistant());
 
             for (IAssistant a : assistants) {
                 Assistant assistant = new UIAssistant(a);
 
                 cmbAssistantModel.addElement(assistant);
             }
 
             List<ICourse> courses = (new Course()).getAll();
 
             ComboBoxModel cmbCourseModel = new ComboBoxModel(DataType.Course);
 
             cmbCourseModel.addElement(new UICourse());
 
             for (ICourse c : courses) {
                 Course course = new UICourse(c);
 
                 cmbCourseModel.addElement(course);
             }
 
             cmbAssistant.setModel(cmbAssistantModel);
             cmbCourse.setModel(cmbCourseModel);
         } catch (AdoHiveException e) {
         }
 
         ActivityLine al = new ActivityLine(lblCourse, cmbCourse, lblAssistant,
             cmbAssistant, btnPlusMinus);
 
         if (activityLines.isEmpty()) {
             btnPlusMinus.setIcon(new ImageIcon(getClass().getResource(
                 "/de/aidger/view/icons/plus-small.png")));
 
             gridBagConstraints.gridy = 4;
 
             btnPlusMinus.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     addNewActivity();
                 }
             });
 
             if (editMode) {
                 btnPlusMinus.setVisible(false);
             }
         } else {
             gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
 
             btnPlusMinus.setAction(new RemoveActivityAction(al));
         }
 
         add(btnPlusMinus, gridBagConstraints);
 
         activityLines.add(al);
     }
 
     /**
      * Get the courses of the activity.
      * 
      * @return The courses of the activity
      */
     public List<Course> getCourses() {
         List<Course> courses = new Vector<Course>();
 
         for (ActivityLine al : activityLines) {
             courses.add((Course) al.cmbCourse.getSelectedItem());
         }
 
         return courses;
     }
 
     /**
      * Get the assistants of the activity.
      * 
      * @return The assistants of the activity
      */
     public List<Assistant> getAssistants() {
         List<Assistant> assistants = new Vector<Assistant>();
 
         for (ActivityLine al : activityLines) {
             assistants.add((Assistant) al.cmbAssistant.getSelectedItem());
         }
 
         return assistants;
     }
 
     /**
      * Get the contents of the activity.
      * 
      * @return The contents of the activity
      */
     public String getContent() {
         return txtContent.getText();
     }
 
     /**
      * Get the date of the activity.
      * 
      * @return The date of the activity.
      */
     public java.sql.Date getDate() {
         return new java.sql.Date(((Date) spDate.getValue()).getTime());
     }
 
     /**
      * Get the type of document.
      * 
      * @return The type of document
      */
     public String getDocumentType() {
         return txtDocumentType.getText();
     }
 
     /**
      * Get the processor of the activity.
      * 
      * @return The processor of the activity.
      */
     public String getProcessor() {
         return txtProcessor.getText();
     }
 
     /**
      * Get remarks to the activity.
      * 
      * @return Remarks to the activity
      */
     public String getRemark() {
         return txtRemark.getText();
     }
 
     /**
      * Get the sender of the activity.
      * 
      * @return The sender of the activity.
      */
     public String getSender() {
         return txtSender.getText();
     }
 
     /**
      * Get the type of the activity.
      * 
      * @return The type of the activity
      */
     public String getType() {
         return txtType.getText();
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         lblDate = new javax.swing.JLabel();
         lblProcessor = new javax.swing.JLabel();
         lblSender = new javax.swing.JLabel();
         lblType = new javax.swing.JLabel();
         lblDocumentType = new javax.swing.JLabel();
         lblRemark = new javax.swing.JLabel();
         lblContent = new javax.swing.JLabel();
         spDate = new javax.swing.JSpinner();
         txtProcessor = new javax.swing.JTextField();
         txtSender = new javax.swing.JTextField();
         txtType = new javax.swing.JTextField();
         txtDocumentType = new javax.swing.JTextField();
         txtRemark = new javax.swing.JTextField();
         filler = new javax.swing.JLabel();
         scrollPane = new javax.swing.JScrollPane();
         txtContent = new javax.swing.JTextArea();
 
         setLayout(new java.awt.GridBagLayout());
 
         lblDate.setText(_("Date"));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(lblDate, gridBagConstraints);
 
         lblProcessor.setText(_("Processor"));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(lblProcessor, gridBagConstraints);
 
         lblSender.setText(_("Sender"));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 50, 10, 10);
         add(lblSender, gridBagConstraints);
 
         lblType.setText(_("Type"));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 50, 10, 10);
         add(lblType, gridBagConstraints);
 
         lblDocumentType.setText(_("Document Type"));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(lblDocumentType, gridBagConstraints);
 
         lblRemark.setText(_("Remark"));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 50, 10, 10);
         add(lblRemark, gridBagConstraints);
 
         lblContent.setText(_("Content"));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(lblContent, gridBagConstraints);
 
         spDate.setModel(new javax.swing.SpinnerDateModel());
        spDate.setEditor(new javax.swing.JSpinner.DateEditor(spDate, "MM.yyyy"));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(spDate, gridBagConstraints);
 
         txtProcessor.setMinimumSize(new java.awt.Dimension(200, 25));
         txtProcessor.setPreferredSize(new java.awt.Dimension(200, 25));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(txtProcessor, gridBagConstraints);
 
         txtSender.setMinimumSize(new java.awt.Dimension(200, 25));
         txtSender.setPreferredSize(new java.awt.Dimension(200, 25));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(txtSender, gridBagConstraints);
 
         txtType.setMinimumSize(new java.awt.Dimension(200, 25));
         txtType.setPreferredSize(new java.awt.Dimension(200, 25));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(txtType, gridBagConstraints);
 
         txtDocumentType.setMinimumSize(new java.awt.Dimension(200, 25));
         txtDocumentType.setPreferredSize(new java.awt.Dimension(200, 25));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(txtDocumentType, gridBagConstraints);
 
         txtRemark.setMinimumSize(new java.awt.Dimension(200, 25));
         txtRemark.setPreferredSize(new java.awt.Dimension(200, 25));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(txtRemark, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         add(filler, gridBagConstraints);
 
         txtContent.setColumns(20);
         txtContent.setRows(5);
         scrollPane.setViewportView(txtContent);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(scrollPane, gridBagConstraints);
     }// </editor-fold>//GEN-END:initComponents
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JLabel filler;
     private javax.swing.JLabel lblContent;
     private javax.swing.JLabel lblDate;
     private javax.swing.JLabel lblDocumentType;
     private javax.swing.JLabel lblProcessor;
     private javax.swing.JLabel lblRemark;
     private javax.swing.JLabel lblSender;
     private javax.swing.JLabel lblType;
     private javax.swing.JScrollPane scrollPane;
     private javax.swing.JSpinner spDate;
     private javax.swing.JTextArea txtContent;
     private javax.swing.JTextField txtDocumentType;
     private javax.swing.JTextField txtProcessor;
     private javax.swing.JTextField txtRemark;
     private javax.swing.JTextField txtSender;
     private javax.swing.JTextField txtType;
     // End of variables declaration//GEN-END:variables
 
     private final List<ActivityLine> activityLines = new Vector<ActivityLine>();
 
     /**
      * This class represents an activity line in the form.
      * 
      * @author aidGer Team
      */
     private class ActivityLine {
         public JLabel lblCourse;
         public JComboBox cmbCourse;
         public JLabel lblAssistant;
         public JComboBox cmbAssistant;
         public JButton btnPlusMinus;
 
         /**
          * Initializes an activity line.
          * 
          */
         public ActivityLine(JLabel lblCourse, JComboBox cmbCourse,
                 JLabel lblAssistant, JComboBox cmbAssistant,
                 JButton btnPlusMinus) {
             this.lblCourse = lblCourse;
             this.cmbCourse = cmbCourse;
             this.lblAssistant = lblAssistant;
             this.cmbAssistant = cmbAssistant;
             this.btnPlusMinus = btnPlusMinus;
         }
     }
 
     /**
      * Removes an activity line from the form by clicking on "-" button.
      * 
      * @author aidGer Team
      */
     private class RemoveActivityAction extends AbstractAction {
         /**
          * The activity line that will be removed.
          */
         private final ActivityLine activityLine;
 
         /**
          * Initializes the action.
          */
         public RemoveActivityAction(ActivityLine activityLine) {
             putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource(
                 "/de/aidger/view/icons/minus-small.png")));
 
             this.activityLine = activityLine;
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see
          * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
          * )
          */
         @Override
         public void actionPerformed(ActionEvent e) {
             remove(activityLine.lblCourse);
             remove(activityLine.cmbCourse);
             remove(activityLine.lblAssistant);
             remove(activityLine.cmbAssistant);
             remove(activityLine.btnPlusMinus);
 
             activityLines.remove(activityLine);
 
             revalidate();
         }
     }
 
 }
