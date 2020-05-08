 
 package iniconfigurationmanager.options;
 
 import iniconfigurationmanager.parsing.RawValue;
 import iniconfigurationmanager.schema.OptionData;
 import iniconfigurationmanager.utils.StringUtils;
 
 /**
  *
  * @author Ondrej Klejch <ondrej.klejch@gmail.com>
  */
 public class StringOptionData extends OptionData {
 
     public StringOptionData() {
         
     }
 
     @Override
     protected Class getValueClass() {
         return String.class;
     }
 
     @Override
     protected Object parseValue(RawValue value) {
        return StringUtils.removeSlashes( value.getValue() );
     }
 
 
     @Override
     protected String valueToString(Object value) {
        return  StringUtils.addSlashes( value.toString() );
     }
 
 
 }
