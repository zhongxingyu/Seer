 package iniconfigurationmanager.schema;
 
 import iniconfigurationmanager.parsing.ValueLink;
 import iniconfigurationmanager.parsing.RawValue;
 import iniconfigurationmanager.parsing.Format;
 import iniconfigurationmanager.utils.StringUtils;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Abstract OptionData class represents option's data in the configuration.
  */
 public abstract class OptionData {
 
     private String name;
 
     private String sectionName;
 
     private String comment;
 
     private ConfigurationData configuration;
 
     private List<Object> values;
 
 
     public OptionData() {
         this.values = new LinkedList<Object>();
         this.comment = "";
     }
 
 
     /**
      * Sets name of this option
      * 
      * @param String name
      * @return OptionData this instance for fluent interface
      */
     protected OptionData setName( String name ) {
         this.name = name;
 
         return this;
     }
 
 
     /**
      * Sets name of section that this option belongs to
      *
      * @param String sectionName
      * @return OptionData this instance for fluent interface
      */
     protected OptionData setSectionName( String sectionName ) {
         this.sectionName = sectionName;
 
         return this;
     }
 
 
     /**
      * Sets configuration data that this option belongs to
      *
      * @param configuration
      * @return OptionData this instance for fluent interface
      */
     protected OptionData setConfiguration( ConfigurationData configuration ) {
         this.configuration = configuration;
 
         return this;
     }
 
 
     /**
      * Sets comment for this option by merging the comment from the schema and
      * the comment from the configuration input.
      * 
      * @param String schemaComment
      * @param String inputComment
      * @return OptionData this instance for fluent interface
      */
     public OptionData setComment( String schemaComment, String inputComment ) {
         StringBuilder sb = new StringBuilder();
         sb.append( schemaComment );
         sb.append( Format.NEWLINE );
         sb.append( inputComment );
 
         this.comment = StringUtils.formatComment( sb.toString() );
 
         return this;
     }
 
 
     /**
      * Determines whether this option has a comment
      * @return
      */
     private boolean hasComment() {
         return !comment.trim().isEmpty();
     }
 
 
     /**
      * Returns name of this option
      * 
      * @return String
      */
     public String getName() {
         return name;
     }
 
 
     /**
      * Returns name of the section that this option belongs to
      * 
      * @return String
      */
     public String getSectionName() {
         return sectionName;
     }
 
 
     /**
      * Returns canonical name of this option. It's a string in
      * section_name#option_name format.
      * 
      * @return String
      */
     public String getCanonicalName() {
         return String.format( Format.OPTION_CANONICAL_NAME_FORMAT,
                 sectionName, name );
     }
 
 
     /**
      * Return ValueLink to this option
      * 
      * @return ValueLink
      */
     public ValueLink getOptionLink() {
         return new ValueLink( getCanonicalName(), configuration );
     }
 
 
     /**
      * Sets values of this option to empty list.
      *
      * @return OptionData this instance for fluent interface
      */
     public OptionData clear() {
         values.clear();
 
         return this;
     }
 
 
     /**
      * Adds value to this values list depending on the value's type.
      * ValueLink is added directly.
      * RawValue is parsed by parseValue method and then added.
      * The rest is casted to the type defined by geValueClass and then added.
      * 
      * @param Object value
      * @return OptionData this instance for fluent interface
      */
     public OptionData addValue( Object value ) {
         if ( value instanceof ValueLink ) {
             values.add( (ValueLink) value );
         } else if ( value instanceof RawValue ) {
             values.add( parseValue( (RawValue) value ) );
         } else {
            values.add( getValueClass().cast( value ) );
         }
 
         return this;
     }
 
 
     /**
      * Sets this values list to the value by clearing that list by clear()
      * method and then adding this value by addValue() method.
      * 
      * @param Object value
      * @return OptionData this instance for fluent interface
      */
     public OptionData setValue( Object value ) {
         clear();
         addValue( value );
 
         return this;
     }
 
 
     /**
      * Sets this values list to the values by clearing that list by clear()
      * method and then adding every value from values by addValue() method.
      *
      * @param List<Object> values
      * @return OptionData this instance for fluent interface
      */
     public OptionData setValues( List<Object> values ) {
         clear();
         for ( Object value : values ) {
             addValue( value );
         }
 
         return this;
     }
 
 
     /**
      * Return first value of the values list.
      *
      * @return Object
      */
     public Object getValue() {
         return values.iterator().next();
     }
 
 
     /**
      * Return first value of the values list casted to the T type.
      *
      * @param <T> type to which the value has to be casted.
      * @param type instance of the T
      * @return T 
      */
     public <T> T getValue( T type ) {
         return (T) getValue();
     }
 
 
     /**
      * Returns values as list of Object
      * 
      * @return List<Object>
      */
     public List<Object> getValues() {
         return getValues( new Object() );
     }
 
 
     /**
      * Returns values as a list of objects casted to T type
      *
      * @param <T> type to which the value has to be casted.
      * @param type instance of the T
      * @return List<T>
      */
     public <T> List<T> getValues( T type ) {
         if ( !typeMatches( type ) ) {
             throw new ClassCastException();
         }
 
         List<T> valuesList = new LinkedList<T>();
         for ( Object value : values ) {
             if ( value instanceof ValueLink ) {
                 ValueLink link = (ValueLink) value;
                 valuesList.addAll( link.getValues( type ) );
             } else {
                 valuesList.add( (T) value );
             }
         }
 
         return valuesList;
     }
 
 
     /**
      * Determine whether the given type mathes with type defined by 
      * getValueClass()
      * 
      * @param <T> type to which the value has to be casted.
      * @param type instance of the T
      * @return boolean
      */
     private <T> boolean typeMatches( T type ) {
         return type.getClass().isAssignableFrom( getValueClass() );
     }
 
 
     /**
      * Accepts visitors implementing StructureVisitor pattern
      *
      * @param StructureVisitor visitor
      * @return OptionData this instance for fluent interface
      */
     public OptionData accept( StructureVisitor visitor ) {
         visitor.visit( this );
 
         return this;
     }
 
 
     /**
      * Accepts visitors implementing ValuesVisitor pattern
      *
      * @param visitor
      * @return OptionData this instance for fluent interface
      */
     public OptionData accept( ValuesVisitor visitor ) {
         visitor.enter( this );
 
         for ( Object value : values ) {
             if ( value instanceof ValueLink ) {
                 ValueLink link = (ValueLink) value;
                 link.getLinkedOption().accept( visitor );
             } else {
                 visitor.visit( value );
             }
         }
 
         visitor.leave( this );
 
         return this;
     }
 
 
     /**
      * Determines whether this option has only default values.
      *
      * @return boolean
      */
     public boolean hasOnlyDefaultValues() {
         ConfigurationSchema schema = configuration.getSchema();
         if ( !schema.hasSection( sectionName ) ) {
             return false;
         }
 
         SectionSchema sectionSchema = schema.getSection( sectionName );
         if ( !sectionSchema.hasOption( name ) ) {
             return false;
         }
 
         OptionSchema optionSchema = sectionSchema.getOption( name );
         if ( !optionSchema.hasDefaultValue() ) {
             return false;
         }
 
 
         return containsSameValues( optionSchema.getDefaultValues(), values );
     }
 
 
     /**
      * Determines whether the given lists are equal.
      *
      * @param List defaultValues
      * @param List<Object> values
      * @return boolean
      */
     private boolean containsSameValues(
             List defaultValues, List<Object> values ) {
         return defaultValues.containsAll( values ) &&
                 values.containsAll( defaultValues );
     }
 
 
     @Override
     public String toString() {
         if ( hasComment() ) {
             return String.format( Format.OPTION_WITH_COMMENT_FORMAT,
                     comment, name, valuesToString() );
         } else {
             return String.format( Format.OPTION_FORMAT,
                     name, valuesToString() );
         }
     }
 
 
     /**
      * Prints values to the list separated by ,
      *
      * @return String
      */
     private String valuesToString() {
         StringBuilder sb = new StringBuilder();
         for ( Object value : values ) {
             if ( value instanceof ValueLink ) {
                 sb.append( ((ValueLink) value).toString() );
             } else {
                 sb.append( valueToString( value ) );
             }
 
             sb.append( Format.VALUES_COMMA_DELIMITER );
         }
 
         sb.deleteCharAt( sb.length() - 1 );
 
         return sb.toString();
     }
 
 
     /**
      * Returns class reflection of a type that represents this option's values
      * type.
      * 
      * @return Class
      */
     protected abstract Class getValueClass();
 
 
     /**
      * Parses value into object that is compatible with type defined by
      * getValueClass().
      * 
      * @param RawValue value
      * @return Object
      */
     protected abstract Object parseValue( RawValue value );
 
 
     /**
      * Prints given value to string.
      * 
      * @param Object value
      * @return String
      */
     protected abstract String valueToString( Object value );
 }
