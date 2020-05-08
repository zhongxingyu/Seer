 package com.yahoo.dtf.config.transform;
 
 import java.util.HashMap;
 
 import com.yahoo.dtf.config.transform.Transformer;
 import com.yahoo.dtf.config.transform.converters.Converter;
 import com.yahoo.dtf.config.transform.converters.FromHexConverter;
 import com.yahoo.dtf.config.transform.converters.ToHexConverter;
 import com.yahoo.dtf.exception.ParseException;
 import com.yahoo.dtf.util.StringUtil;
 
 /**
  * @dtf.feature Convert Transformer
  * @dtf.feature.group Transformers
  * 
  * @dtf.feature.desc <p>
  *                   This is the DTF string conversion transformer and it can be 
  *                   used to convert between various formats such as hex to 
  *                   string and string to hex. In the currently available 
  *                   functions are:
  *                   </p>
  *                   <p> 
  *                   <b>to-hex([padding])</b><br/>
  *                   The to-hex function obviously converts an integer to a hex 
  *                   format. The padding argument is optional and if set will 
  *                   pad the resulting number with 0's on the left side.
  *                   </p>
  *                   <p>
  *                   <b>from-hex</b><br/>
  *                   The from-hex does the opposite of to-hex and converts a 
  *                   hexa-decimal number back to its decimal representation.
  *                   </p>
  *                   
  * @dtf.example
  * <sequence>
  *     <property name="int1" value="1234"/> 
  *     <property name="int2" value="42"/> 
  *
  *     <log>${int1:convert:to-hex}</log> 
  *     <log>${int2:convert:to-hex(4)}</log> 
  * </sequence>
  *
  * @dtf.example
  * <sequence>
  *     <property name="hex1" value="FFFF"/>
  *     <property name="hex2" value="FFFFFFFF"/>
  *     
  *     <log>${hex1:convert:from-hex}</log> 
  *     <log>${hex2:convert:from-hex}</log> 
  * </sequence>
  * 
  */
 public class ConvertTransformer implements Transformer {
     
     private static HashMap<String, Converter> converters = 
                                                new HashMap<String, Converter>();
     
     static { 
         converters.put("to-hex", new ToHexConverter());
         converters.put("from-hex", new FromHexConverter());
     }
     
     public String apply(String data, String expression) throws ParseException {
         int bracketIndex = expression.indexOf('(');
         String operator = null;
         String result = null;
         int padding = 0;
         
         if ( bracketIndex != -1 ) {
             operator = expression.substring(0,bracketIndex);
             padding = Integer.valueOf(expression.substring(bracketIndex+1,
                                                            expression.length()-1));
         } else { 
             operator = expression;
         }
         
         Converter converter = converters.get(operator);
 
         if ( converter != null ) { 
            return "" + StringUtil.padString(result, padding, '0');
         } 
         
         throw new ParseException("Unkown convert expression [" + operator + "]");
     }
 }
