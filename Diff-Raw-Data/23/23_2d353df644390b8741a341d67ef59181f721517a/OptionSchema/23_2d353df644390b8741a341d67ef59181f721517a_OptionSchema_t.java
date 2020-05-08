 
 package iniconfigurationmanager.schema;
 
 import iniconfigurationmanager.rules.ValidationRule;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  *
  * @author Ondrej Klejch <ondrej.klejch@gmail.com>
  */
 public abstract class OptionSchema {
 
     private String name;
 
     private String sectionName;
 
     private boolean required;
 
     private String comment;
 
     private List< ValidationRule > validationRules;
 
     private List defaultValues;
 
     
     public OptionSchema() {
         this.validationRules = new LinkedList<ValidationRule>();
         this.required = false;
         this.comment = "";
     }
 
 
     protected OptionSchema setName( String name ) {
         this.name = name;
 
         return this;
     }
 
 
     public String getName() {
         return name;
     }
 
 
     protected OptionSchema setSectionName( String sectionName ) {
         this.sectionName = sectionName;
 
         return this;
     }
 
     public String getSectionName() {
         return sectionName;
     }
 
 
     public String getCanonicalName() {
         return String.format("%s#%s", sectionName, name);
     }
 
 
     public OptionSchema setComment( String comment ) {
         this.comment = comment;
 
         return this;
     }
 
 
     public String getComment() {
         return comment;
     }
 
     
     public OptionSchema setRequired() {
         this.required = true;
 
         return this;
     }
 
 
     public boolean isRequired() {
         return this.required;
     }
 
 
     public OptionSchema addValidationRule( ValidationRule rule ) {
         if( rule == null ) {
             throw new IllegalArgumentException( String.format(
                     SchemaError.NULL_VALIDATION_RULE.getMessage() ) );
         }
 
         if( ! rule.isAplicableOn( this ) ) {
             throw new IllegalArgumentException( String.format(
                     SchemaError.UNALLOWED_VALIDATION_RULE.getMessage() ) );
         }
 
         validationRules.add( rule );
 
         return this;
     }
 
 
     public List< ValidationRule > getValidationRules() {
         return validationRules;
     }
 
 
     public OptionSchema setDefaultValues(List defaultValues) {
         this.defaultValues = defaultValues;
 
         return this;
     }
 
 
     public List getDefaultValues() {
         return defaultValues;
     }
 
 
     public boolean hasDefaultValue() {
        return defaultValues != null && ! defaultValues.isEmpty();
     }
     
 
     public void accept(StructureVisitor visitor) {
         visitor.visit( this );
     }
 
 
     public abstract OptionData getOptionData();
     
 }
