 package de.aidger.view.tabs;
 
 import static de.aidger.utils.Translation._;
 
 import java.awt.Dimension;
 import java.util.List;
 
 import javax.swing.Box;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import de.aidger.controller.ActionNotFoundException;
 import de.aidger.controller.ActionRegistry;
 import de.aidger.controller.actions.masterdata.MasterDataEditorCancelAction;
 import de.aidger.controller.actions.masterdata.MasterDataEditorSaveAction;
 import de.aidger.model.AbstractModel;
 import de.aidger.model.models.Assistant;
 import de.aidger.model.models.Course;
 import de.aidger.model.models.FinancialCategory;
 import de.aidger.model.models.HourlyWage;
 import de.aidger.view.UI;
 import de.aidger.view.forms.AssistantEditorForm;
 import de.aidger.view.forms.CourseEditorForm;
 import de.aidger.view.forms.FinancialCategoryEditorForm;
 import de.aidger.view.forms.HourlyWageEditorForm;
 import de.aidger.view.tabs.MasterDataViewerTab.MasterDataType;
 
 /**
  * A tab which will be used to add and edit the master data.
  * 
  * @author aidGer Team
  */
 @SuppressWarnings("serial")
 public class MasterDataEditorTab extends Tab {
 
     /**
      * The master data model for the editor.
      */
     @SuppressWarnings("unchecked")
     protected AbstractModel model = null;
 
     /**
      * The type of the master data.
      */
     private final MasterDataType type;
 
     /**
      * Constructs a master data editor tab.
      * 
      * @param type
      *            type of the master data
      */
     public MasterDataEditorTab(MasterDataType type) {
         this.type = type;
 
         init();
     }
 
     /**
      * Constructs a master data editor tab.
      * 
      * @param type
      *            type of the master data
      * @param model
      *            the master data model
      */
     @SuppressWarnings("unchecked")
     public MasterDataEditorTab(MasterDataType type, AbstractModel model) {
         this.type = type;
         this.model = model;
 
         init();
     }
 
     /**
      * Initializes the components and the button actions.
      */
     private void init() {
         initComponents();
 
         try {
             btnSave.setAction(ActionRegistry.getInstance().get(
                     MasterDataEditorSaveAction.class.getName()));
 
             btnCancel.setAction(ActionRegistry.getInstance().get(
                     MasterDataEditorCancelAction.class.getName()));
         } catch (ActionNotFoundException e) {
             UI.displayError(e.getMessage());
         }
     }
 
     /**
      * Returns the master data model.
      * 
      * @return the master data model
      */
     @SuppressWarnings("unchecked")
     public AbstractModel getModel() {
         if (model == null) {
             switch (type) {
             case Course:
                 model = new Course();
                 break;
             case Assistant:
                 model = new Assistant();
                 break;
             case FinancialCategory:
                 model = new FinancialCategory();
                 break;
             case HourlyWage:
                 model = new HourlyWage();
                 break;
             }
         }
 
        model.setNew(false);

         return model;
     }
 
     /**
      * Sets the master data model.
      * 
      * @param model
      *            the master data model that will be set
      */
     @SuppressWarnings("unchecked")
     public void setModel(AbstractModel model) {
         this.model = model;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.aidger.view.tabs.Tab#getTabName()
      */
     @Override
     public String getTabName() {
         if (isEditMode()) {
             switch (type) {
             case Course:
                 return _("Edit course");
             case Assistant:
                 return _("Edit assistant");
             case FinancialCategory:
                 return _("Edit financial category");
             case HourlyWage:
                 return _("Edit hourly wage");
             }
         }
 
         switch (type) {
         case Course:
             return _("Add course");
         case Assistant:
             return _("Add assistant");
         case FinancialCategory:
             return _("Add financial category");
         case HourlyWage:
             return _("Add hourly wage");
         }
 
         return "";
     }
 
     /**
      * Retrieves whether the tab is in edit mode.
      * 
      * @return whether the tab is in edit mode
      */
     public boolean isEditMode() {
         return model != null;
     }
 
     /**
      * Returns the type of the master data that is added or edited.
      * 
      * @return the type of the master data
      */
     public MasterDataType getType() {
         return type;
     }
 
     /**
      * Returns the master data editor form.
      * 
      * @return the master data editor form.
      */
     public JPanel getEditorForm() {
         if (editorForm != null) {
             return editorForm;
         }
 
         switch (type) {
         case Course:
             return new CourseEditorForm((Course) model);
         case Assistant:
             return new AssistantEditorForm((Assistant) model);
         case FinancialCategory:
             return new FinancialCategoryEditorForm((FinancialCategory) model);
         case HourlyWage:
             return new HourlyWageEditorForm((HourlyWage) model);
         default:
             return new JPanel();
         }
     }
 
     /**
      * Updates the hints panel due to validation failures.
      */
     @SuppressWarnings("unchecked")
     public void updateHints() {
         List<String> hints = model.getErrors();
 
         this.hints.removeAll();
 
         for (String hint : hints) {
             this.hints.add(new JLabel(hint));
 
             this.hints.add(Box.createRigidArea(new Dimension(0, 5)));
         }
 
         this.hints.revalidate();
 
         model.resetErrors();
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed"
     // <editor-fold defaultstate="collapsed"
     // <editor-fold defaultstate="collapsed"
     // <editor-fold defaultstate="collapsed"
     // <editor-fold defaultstate="collapsed"
     // <editor-fold defaultstate="collapsed"
     // <editor-fold defaultstate="collapsed"
     // <editor-fold defaultstate="collapsed"
     // <editor-fold defaultstate="collapsed"
     // desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         editorForm = getEditorForm();
         buttons = new javax.swing.JPanel();
         btnSave = new javax.swing.JButton();
         btnCancel = new javax.swing.JButton();
         filler = new javax.swing.JLabel();
         hints = new javax.swing.JPanel();
         filler2 = new javax.swing.JLabel();
 
         setLayout(new java.awt.GridBagLayout());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         add(editorForm, gridBagConstraints);
 
         btnSave.setText(_("Save"));
         buttons.add(btnSave);
 
         btnCancel.setText(_("Cancel"));
         buttons.add(btnCancel);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         add(buttons, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.weighty = 1.0;
         add(filler, gridBagConstraints);
 
         hints.setBorder(javax.swing.BorderFactory
                 .createTitledBorder(_("Hints")));
         hints.setLayout(new javax.swing.BoxLayout(hints,
                 javax.swing.BoxLayout.Y_AXIS));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         add(hints, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridheight = 3;
         gridBagConstraints.weightx = 1.0;
         add(filler2, gridBagConstraints);
     }// </editor-fold>//GEN-END:initComponents
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnCancel;
     private javax.swing.JButton btnSave;
     private javax.swing.JPanel buttons;
     private javax.swing.JPanel editorForm;
     private javax.swing.JLabel filler;
     private javax.swing.JLabel filler2;
     private javax.swing.JPanel hints;
     // End of variables declaration//GEN-END:variables
 
 }
