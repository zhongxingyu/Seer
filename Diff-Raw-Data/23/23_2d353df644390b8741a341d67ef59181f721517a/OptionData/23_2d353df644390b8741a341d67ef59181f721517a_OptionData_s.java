 
 package iniconfigurationmanager.schema;
 
 
 import iniconfigurationmanager.parsing.ValueLink;
 import iniconfigurationmanager.parsing.RawValue;
 import iniconfigurationmanager.parsing.Format;
 import iniconfigurationmanager.utils.StringUtils;
 import java.util.LinkedList;
 import java.util.List;
 
 
 /**
  *
  * @author Ondrej Klejch <ondrej.klejch@gmail.com>
  */
 public abstract class OptionData {
 
     private String name;
 
     private String sectionName;
 
     private String comment;
 
     private ConfigurationData configuration;
 
     private List< Object > values;
 
     
     public OptionData() {
         this.values = new LinkedList< Object >();
         this.comment = "";
     }
 
 
     protected OptionData setName( String name ) {
         this.name = name;
 
         return this;
     }
 
 
     protected OptionData setSectionName( String sectioName ) {
         this.sectionName = sectioName;
 
         return this;
     }
 
 
     protected OptionData setConfiguration( ConfigurationData configuration ) {
         this.configuration = configuration;
 
         return this;
     }
 
 
     public OptionData setComment( String schemaComment, String inputComment ) {
         StringBuilder sb = new StringBuilder();
         sb.append( schemaComment );
         sb.append( Format.NEWLINE );
         sb.append( inputComment );
 
         this.comment = StringUtils.formatComment( sb.toString() );
 
         return this;
     }
 
 
     private boolean hasComment() {
         return ! comment.trim().isEmpty();
     }
 
     
     public String getName() {
         return name;
     }
 
     
     public String getSectionName() {
         return sectionName;
     }
     
 
     public String getCanonicalName() {
         return String.format( Format.OPTION_CANONICAL_NAME_FORMAT,
                 sectionName, name);
     }
 
 
     public ValueLink getOptionLink() {
         return new ValueLink( getCanonicalName(), configuration );
     }
     
     public OptionData clear() {
         values.clear();
 
         return this;
     }
     
 
     public OptionData addValue( Object value ) {
         if( value instanceof ValueLink) {
             values.add( (ValueLink) value );
         } else if ( value instanceof RawValue ) {
             values.add( parseValue( (RawValue) value ) );
         } else {
             values.add( getValueClass().cast( value ) );
         }
 
         return this;
     }
 
     
     public OptionData setValue( Object value ) {
         clear();
         addValue( value );
 
         return this;
     }
 
 
     public OptionData setValues( List< Object > values ) {
         clear();
         for( Object value : values ) {
             addValue( value );
         }
 
         return this;
     }
 
 
     public Object getValue() {
         return values.iterator().next();
     }
 
 
     public < T > T getValue( T type ) {
         return (T) getValue();
     }
 
 
     public List< Object > getValues() {
         return getValues( new Object() );
     }
 
     
     public < T > List< T > getValues( T type ) {
         if( ! typeMatches( type ) ) {
             throw new ClassCastException();
         }
 
         List< T > valuesList = new LinkedList< T >();
         for( Object value : values ) {
             if( value instanceof ValueLink ) {
                 ValueLink link = (ValueLink) value;
                 valuesList.addAll( link.getValues( type ) );
             } else {
                 valuesList.add( (T) value );
             }
         }
 
         return valuesList;
     }
 
 
     private < T > boolean typeMatches( T type ) {
         return type.getClass().isAssignableFrom( getValueClass() );
     }
 
 
     public void accept( StructureVisitor visitor ) {
         visitor.visit( this );
     }
 
 
     public void accept( ValuesVisitor visitor ) {
         visitor.enter( this );
 
         for( Object value : values ) {
             if( value instanceof ValueLink ) {
                 ValueLink link = (ValueLink) value;
                 link.getLinkedOption().accept( visitor );
             } else {
                 visitor.visit( value );
             }
         }
 
         visitor.leave( this );
     }
 
 
     public boolean hasOnlyDefaultValues() {
         ConfigurationSchema schema = configuration.getSchema();
         if( ! schema.hasSection( sectionName ) ) {
             return false;
         }
 
         SectionSchema sectionSchema = schema.getSection( sectionName );
         if( ! sectionSchema.hasOption( name ) ) {
             return false;
         }
 
        OptionSchema optionSchena = sectionSchema.getOption( name );
        return containsSameValues( optionSchena.getDefaultValues(), values );
     }
 
 
     private boolean containsSameValues(
             List defaultValues, List<Object> values
     ) {
         return defaultValues.containsAll( values ) &&
                 values.containsAll( defaultValues );
     }
 
     @Override
     public String toString() {
         if( hasComment() ) {
             return String.format( Format.OPTION_WITH_COMMENT_FORMAT,
                     comment, name, valuesToString() );
         } else {
             return String.format( Format.OPTION_FORMAT,
                 name, valuesToString() );
         }
     }
 
 
     private String valuesToString() {
         StringBuilder sb = new StringBuilder();
         for( Object value : values ) {
             if( value instanceof ValueLink ) {
                 sb.append( ((ValueLink) value).toString() );
             } else {
                 sb.append( valueToString( value ) );
             }
 
             sb.append( Format.VALUES_COMMA_DELIMITER );
         }
 
         sb.deleteCharAt( sb.length() - 1 );
 
         return sb.toString();
     }
 
 
     protected abstract Class getValueClass();
 
     protected abstract Object parseValue( RawValue value );
 
     protected abstract String valueToString( Object value );
 
 }
