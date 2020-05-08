 package de.aidger.view.tabs;
 
 import static de.aidger.utils.Translation._;
 
import java.awt.Dimension;
 import java.awt.event.KeyEvent;
 import java.util.List;
 
import javax.swing.Box;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.KeyStroke;
 
 import de.aidger.controller.ActionNotFoundException;
 import de.aidger.controller.ActionRegistry;
 import de.aidger.controller.actions.EditorCancelAction;
 import de.aidger.controller.actions.EditorSaveAction;
 import de.aidger.model.AbstractModel;
 import de.aidger.model.models.Activity;
 import de.aidger.model.models.Assistant;
 import de.aidger.model.models.Contract;
 import de.aidger.model.models.Course;
 import de.aidger.model.models.Employment;
 import de.aidger.model.models.FinancialCategory;
 import de.aidger.model.models.HourlyWage;
 import de.aidger.view.UI;
 import de.aidger.view.forms.AssistantEditorForm;
 import de.aidger.view.forms.ContractEditorForm;
 import de.aidger.view.forms.CourseEditorForm;
 import de.aidger.view.forms.EmploymentEditorForm;
 import de.aidger.view.forms.FinancialCategoryEditorForm;
 import de.aidger.view.forms.HourlyWageEditorForm;
 import de.aidger.view.tabs.ViewerTab.DataType;
 
 /**
  * A tab which will be used to add and edit the data.
  * 
  * @author aidGer Team
  */
 @SuppressWarnings("serial")
 public class EditorTab extends Tab {
 
     /**
      * The data model for the editor.
      */
     @SuppressWarnings("unchecked")
     protected AbstractModel model = null;
 
     /**
      * The type of the data.
      */
     private final DataType type;
 
     /**
      * A flag whether the tab is in edit mode.
      */
     private boolean editMode = false;
 
     /**
      * Constructs a data editor tab.
      * 
      * @param type
      *            type of the data
      */
     public EditorTab(DataType type) {
         this.type = type;
 
         init();
     }
 
     /**
      * Constructs a data editor tab.
      * 
      * @param type
      *            type of the data
      * @param model
      *            the data model
      */
     @SuppressWarnings("unchecked")
     public EditorTab(DataType type, AbstractModel model) {
         this.type = type;
         this.model = model;
         editMode = true;
 
         init();
     }
 
     /**
      * Return a parseable string that lets the UI initalise the tab.
      * 
      * @return A parseable string
      */
     @Override
     public String toString() {
         String ret = getClass().getName() + "<" + DataType.class.getName()
                 + "@" + type;
         if (model != null) {
             if (model.getClass().equals(HourlyWage.class)) {
                 HourlyWage h = (HourlyWage) model;
                 ret += "<" + h.getClass().getName() + "@"
                         + h.getQualification() + "@" + h.getMonth() + "@"
                         + h.getYear();
             } else {
                 ret += "<" + model.getClass().getName() + "@" + model.getId();
             }
         }
 
         return ret;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.aidger.view.tabs.Tab#getPredecessor()
      */
     @Override
     public Tab getPredecessor() {
         Tab p = super.getPredecessor();
 
         if (p == null) {
             p = new ViewerTab(getType());
         }
 
         return p;
     }
 
     /**
      * Initializes the components and the button actions.
      */
     private void init() {
         initComponents();
 
         try {
             btnSave.setAction(ActionRegistry.getInstance().get(
                 EditorSaveAction.class.getName()));
 
             btnCancel.setAction(ActionRegistry.getInstance().get(
                 EditorCancelAction.class.getName()));
 
             // shortcuts
             getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                 KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "saveModel");
 
             getActionMap().put(
                 "saveModel",
                 ActionRegistry.getInstance().get(
                     EditorSaveAction.class.getName()));
         } catch (ActionNotFoundException e) {
             UI.displayError(e.getMessage());
         }
     }
 
     /**
      * Returns the data model.
      * 
      * @return the data model
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
             case Employment:
                 model = new Employment();
                 break;
             case Contract:
                 model = new Contract();
                 break;
             case Activity:
                 model = new Activity();
                 break;
             }
         }
 
         return model;
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
             case Employment:
                 return _("Edit employment");
             case Contract:
                 return _("Edit contract");
             case Activity:
                 return _("Edit activity");
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
         case Employment:
             return _("Add employment");
         case Contract:
             return _("Add contract");
         case Activity:
             return _("Add activity");
         }
 
         return "";
     }
 
     /**
      * Retrieves whether the tab is in edit mode.
      * 
      * @return whether the tab is in edit mode
      */
     public boolean isEditMode() {
         return editMode;
     }
 
     /**
      * Returns the type of the data that is added or edited.
      * 
      * @return the type of the data
      */
     public DataType getType() {
         return type;
     }
 
     /**
      * Returns the data editor form.
      * 
      * @return the data editor form.
      */
     public JPanel getEditorForm() {
         return editorForm;
     }
 
     /**
      * Creates the editor form.
      * 
      * @return the created editor form
      */
     public JPanel createEditorForm() {
         switch (type) {
         case Course:
             return new CourseEditorForm((Course) model, listModels);
         case Assistant:
             return new AssistantEditorForm((Assistant) model);
         case FinancialCategory:
             return new FinancialCategoryEditorForm((FinancialCategory) model);
         case HourlyWage:
             return new HourlyWageEditorForm((HourlyWage) model);
         case Employment:
             return new EmploymentEditorForm((Employment) model, listModels);
         case Contract:
             return new ContractEditorForm((Contract) model);
         default:
             return new JPanel();
         }
     }
 
     /**
      * Updates the hints panel due to validation failures.
      * 
      * @param model
      *            the model whose validation failed
      */
     @SuppressWarnings("unchecked")
     public void updateHints(AbstractModel model) {
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
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         editorForm = createEditorForm();
         buttons = new javax.swing.JPanel();
         btnSave = new javax.swing.JButton();
         btnCancel = new javax.swing.JButton();
         filler = new javax.swing.JLabel();
         hints = new javax.swing.JPanel();
         filler2 = new javax.swing.JLabel();
 
         setLayout(new java.awt.GridBagLayout());
 
         editorForm.setBorder(javax.swing.BorderFactory.createTitledBorder(
             javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1),
             getTabName()));
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
             .createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1,
                 1, 1, 1), _("Hints")));
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
