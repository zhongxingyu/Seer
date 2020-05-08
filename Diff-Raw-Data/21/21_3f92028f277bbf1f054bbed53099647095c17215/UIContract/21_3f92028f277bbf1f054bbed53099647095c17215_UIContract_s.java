 package de.aidger.view.models;
 
 import java.text.SimpleDateFormat;
 
 import de.aidger.model.models.Contract;
 import de.aidger.view.forms.ContractEditorForm.ContractType;
 import de.aidger.view.tabs.ViewerTab.DataType;
 import de.unistuttgart.iste.se.adohive.model.IContract;
 
 /**
  * The UI contract is used for prettier rendering of the model.
  * 
  * @author aidGer Team
  */
 public class UIContract extends Contract implements UIModel {
 
     /**
      * Initializes the Contract class.
      */
     public UIContract() {
     }
 
     /**
      * Initializes the Contract class with the given contract model.
      * 
      * @param c
      *            the contract model
      */
     public UIContract(IContract c) {
         super(c);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.aidger.model.AbstractModel#toString()
      */
     @Override
     public String toString() {
         return ContractType.valueOf(getType()).toString()
                 + " ("
                 + (new SimpleDateFormat("dd.MM.yy"))
                     .format(getCompletionDate()) + ")";
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.aidger.view.models.UIModel#getDataType()
      */
     @Override
     public DataType getDataType() {
         return DataType.Contract;
     }
 }
