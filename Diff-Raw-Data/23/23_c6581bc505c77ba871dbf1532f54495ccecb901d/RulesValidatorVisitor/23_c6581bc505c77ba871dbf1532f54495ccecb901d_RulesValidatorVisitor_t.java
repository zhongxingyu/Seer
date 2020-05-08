 package iniconfigurationmanager.validators;
 
 /**
 * <code>validateValidationRules</code> validate Option by its
 * <code>ValidationRule</code>s
 *
 */
 import iniconfigurationmanager.schema.StructureVisitor;
 import iniconfigurationmanager.rules.ValidationRule;
 import iniconfigurationmanager.schema.ConfigurationData;
 import iniconfigurationmanager.schema.OptionData;
 import iniconfigurationmanager.schema.OptionSchema;
 import iniconfigurationmanager.schema.SectionData;
 import iniconfigurationmanager.schema.SectionSchema;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 public class RulesValidatorVisitor
         implements StructureVisitor {
 
     /**
      * <code>schemaOptions</code> holds canonical name and rules for options
      */
     private Map<String, OptionSchema> schemaOptions;
 
     private ValidationResult result;
 
     public RulesValidatorVisitor() {
        this.result = new ValidationResult();
        this.schemaOptions = new LinkedHashMap<String, OptionSchema>();
     }
 
 
     /**
      * Method run validation and merge validation result od each rules
      * that have option
      */
     public void visit( OptionData option ) {
         OptionSchema schema = schemaOptions.get( option.getCanonicalName() );

        if( schema == null ) {
            return;
        }

         List<ValidationRule> optionRules = schema.getValidationRules();
         for ( ValidationRule validationRule : optionRules ) {
             result.mergeResults( validationRule.validate( option ) );
         }
     }
 
     /**
      * Method run add option schema whit his canonical name and schema
      */
 
     public void visit( OptionSchema option ) {
         schemaOptions.put( option.getCanonicalName(), option );
     }
 
 
     public void visit( SectionData section ) {
         return;
     }
 
 
     public void visit( SectionSchema section ) {
         return;
     }
 
 
     public ValidationResult getResult() {
         return result;
     }
 
 
     public void visit( ConfigurationData data ) {
         return;
     }    
     
 }
