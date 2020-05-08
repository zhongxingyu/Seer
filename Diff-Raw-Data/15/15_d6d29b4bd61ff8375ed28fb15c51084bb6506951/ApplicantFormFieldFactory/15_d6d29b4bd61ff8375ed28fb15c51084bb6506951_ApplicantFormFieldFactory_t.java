 package vaadinForm;
 
 import com.vaadin.data.Item;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.Field;
 import com.vaadin.ui.FormFieldFactory;
 import com.vaadin.ui.OptionGroup;
 import com.vaadin.ui.TextArea;
 import com.vaadin.ui.TextField;
 
 /**
  * Field factory class for generating proper fields to the form.
  * @author Juho
  */
 public class ApplicantFormFieldFactory implements FormFieldFactory {
     
     /**
      * Creates a field based on the item's property id.
      * @param item The item where the property belongs to.
      * @param propertyId The Id of the property.
      * @param uiContext The component where the field is presented.
      * @return Field for the specified property.
      */
     public Field createField(Item item, Object propertyId, Component uiContext){
         String pid = propertyId.toString();
         
         if("firstName".equals(pid)){
            return getRequiredTextField("First name", "First name is missing");
         }
         else if("lastName".equals(pid)){
            return getRequiredTextField("Last name", "Last name is missing");                        
         }
         else if("gender".equals(pid)){
             OptionGroup genderSelect = new OptionGroup("Gender");
             genderSelect.addItem("Male");
             genderSelect.addItem("Female");
             //genderSelect.setNullSelectionAllowed(false);
             genderSelect.setNullSelectionItemId("Male");
             return genderSelect;
         }
         else if("arguments".equals(pid)){
             TextArea argumentsArea = new TextArea("Why are you applying for this job?");
             argumentsArea.setNullRepresentation("");
             return argumentsArea;
         }
 
         return null;
     
     }
     
     /**
      * Creates a text field that is marked required in the form.
      * @param caption The caption for the text field.
      * @return Returns a TextField object.
      */
    private TextField getRequiredTextField(String caption, String requiredErrorMessage){
         TextField field = new TextField(caption);
         field.setNullRepresentation("");
        field.setRequiredError(requiredErrorMessage);
        field.setRequired(true);
//        field.setValidationVisible(true);
        //field.setImmediate(true);
         return field;
     }
 }
