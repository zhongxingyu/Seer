 package iniconfigurationmanager.schema;
 
 import iniconfigurationmanager.utils.InvalidOperationException;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 /**
  * SectionSchema represents section's schema in the configuration schema.
  */
 public class SectionSchema
         implements Iterable<OptionSchema> {
 
     private String name;
 
     private String comment;
 
     private Boolean required;
 
     private Map<String, OptionSchema> options;
 
 
     public SectionSchema() {
         this.options = new LinkedHashMap<String, OptionSchema>();
         this.required = false;
         this.comment = "";
     }
 
 
     /**
      * Sets name of this section.
      *
      * @param String name
      * @return SectionSchema this instance for fluent interface implementation
      */
     protected SectionSchema setName( String name ) {
         this.name = name;
 
         return this;
     }
 
 
     /**
      * Returns name of this section.
      * 
      * @return String
      */
     public String getName() {
         return name;
     }
 
 
     /**
      * Sets this section required.
      *
      * @return SectionSchema this instance for fluent interface implementation
      */
     public SectionSchema setReguired() {
         this.required = true;
 
         return this;
     }
 
 
     /**
      * Determines whether this section is required.
      * 
      * @return boolean
      */
     public boolean isRequired() {
         return this.required;
     }
 
 
     /**
      * Sets comment for this section schema.
      *
      * @param String comment
      * @return SectionSchema this instance for fluent interface implementation
      */
     public SectionSchema setComment( String comment ) {
         this.comment = comment;
 
         return this;
     }
 
 
     /**
      * Returns comment of this section.
      *
      * @return String
      */
     public String getComment() {
         return this.comment;
     }
 
 
     /**
      * Adds option to this options list.
      *
      * @param String name
      * @param String option
      * @return SectionSchema this instance for fluent interface implementation
      * @throws InvalidOperationException whether the option already exists
      * @throws IllegalArgumentException whether the option is null
      */
     public SectionSchema addOption( String name, OptionSchema option ) {
         if ( hasOption( name ) ) {
             throw new InvalidOperationException( String.format(
                     SchemaError.DUPLICIT_OPTION_SCHEMA.getMessage(), name ) );
         }
 
         if ( option == null ) {
             throw new IllegalArgumentException( String.format(
                     SchemaError.NULL_OPTION_SCHEMA.getMessage(), name ) );
         }
 
         option.setName( name ).setSectionName( this.name );
 
         options.put( name, option );
 
         return this;
     }
 
 
     /**
      * Determines whether the option exists.
      *
      * @param String name
      * @return boolean
      */
     public boolean hasOption( String name ) {
         return options.containsKey( name );
     }
 
 
     /**
      * Removes option from the options list.
      * 
      * @param String name
      * @return SectionSchema this instance for fluent interface implementation
      */
     public SectionSchema removeOption( String name ) {
         options.remove( name );
 
         return this;
     }
 
 
     /**
      * Returns option with the name.
      * 
      * @param String name
      * @return OptionSchema
      * @throws IllegalArgumentException whether the option doesn't exist
      */
     public OptionSchema getOption( String name ) {
         if ( !hasOption( name ) ) {
             throw new IllegalArgumentException( String.format(
                     SchemaError.UNDEFINED_OPTION_SCHEMA.getMessage(), name ) );
         }
 
         return options.get( name );
     }
 
 
     /**
      * Returns iterator for iterating over option's schemas
      * 
      * @return Iterator<OptionSchema>
      */
     public Iterator<OptionSchema> iterator() {
         return options.values().iterator();
     }
 
 
     /**
      * Accepts visitors implementing StructureVisitor interface.
      * 
      * @param visitor
      * @return SectionSchema this instance for fluent interface implementation
      */
     public SectionSchema accept( StructureVisitor visitor ) {
         visitor.visit( this );
 
         for ( OptionSchema option : options.values() ) {
             option.accept( visitor );
         }
 
         return this;
     }
 }
