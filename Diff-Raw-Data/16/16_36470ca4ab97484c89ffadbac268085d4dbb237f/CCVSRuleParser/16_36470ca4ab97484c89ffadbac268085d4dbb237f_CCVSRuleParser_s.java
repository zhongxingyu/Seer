 package zebrapuzzle.parse;
 
 import zebrapuzzle.resolve.rules.CRule;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Class that implement file parser  for puzzle rules
  * For use necessary set input file name
  * invoke parse() for handle file
  * after ger result through getters
  * <p/>
  * User: mf
  * Date: 06.06.12
  * Time: 12:42
  */
 public class CCVSRuleParser implements IRuleParser {
     private static final Charset ENCODING = Charset.forName("UTF-8");
     private static final String POSITION = "position"; // name for position
 
     /**
      * Constants described meaning values in the file
      */
     private static final int RULE_TYPE = 0;
     private static final int SOURCE_PROPERTY = 1;
     private static final int SOURCE_VALUE = 2;
     private static final int TARGET_PROPERTY = 3;
     private static final int TARGET_VALUE = 4;
 
     private int iNumberOfHouses;
 
     /**
      * Contain all rules from stream
      */
    private List<CRule> rules;
     /**
      * Contain all field names
      */
    private List<String> properties;
 
     /**
      * Values  a[x][y]="Cat"
      */
    private List<List<String>> values;

    private CCVSRuleParser() {
        rules = new LinkedList<>();
        properties = new ArrayList<>();
        values = new ArrayList<>();
    }
 
 
     /**
      * Default constructor
      */
     public CCVSRuleParser(InputStream ruleStream) {
        super();
         parse(ruleStream);
     }
 
     @Override
     public int getNumberOfHouses() {
         return iNumberOfHouses;
     }
 
     public List<CRule> getRules() {
         return rules;
     }
 
     /**
      * Get all field names.
      */
     @Override
     public String[] getProperties() {
         return asArray(properties);
     }
 
     @Override
     public String[][] getValues() {
         String[][] valueArray = new String[values.size()][];
         for (int index = 0; index < values.size(); index++) {
             valueArray[index] = asArray(values.get(index));
         }
         return valueArray;
     }
 
     private String[] asArray(List<String> list) {
         return list.toArray(new String[list.size()]);
     }
 
     /**
      * Perform parsing on the file
      */
     private void parse(InputStream ruleStream) {
         String sFileLine = "";
         try (BufferedReader reader = getRuleReader(ruleStream)) {
             iNumberOfHouses = Integer.parseInt(reader.readLine()); // get the first line from file
             while ((sFileLine = reader.readLine()) != null) {
                 String[] asLineWorlds = sFileLine.split(";");
                 CRule rule = new CRule();
                 rule.type = CRule.Type.valueOf(asLineWorlds[RULE_TYPE]);
                 if (asLineWorlds.length > SOURCE_VALUE) {
                     rule.sourceProperty = getPropertiesOrderNumber(asLineWorlds[SOURCE_PROPERTY]);
                     rule.sourceValue = getValueOrderNumber(asLineWorlds[SOURCE_VALUE], asLineWorlds[SOURCE_PROPERTY]);
                 }
                 if (asLineWorlds.length > TARGET_VALUE) {
                     rule.targetProperty = getPropertiesOrderNumber(asLineWorlds[TARGET_PROPERTY]);
                     rule.targetValue = getValueOrderNumber(asLineWorlds[TARGET_VALUE], asLineWorlds[TARGET_PROPERTY]);
                     rules.add(rule);
                 }
             }
 
         } catch (IOException e) {
             throw new RuntimeException("Rules can not be read", e);
         } catch (NumberFormatException e) {
             throw new NumberFormatException("Number of houses in rule file is not an integer: " + sFileLine);
         }
     }
 
     /**
      * Get the file context
      */
     private BufferedReader getRuleReader(InputStream ruleStream) {
         return new BufferedReader(new InputStreamReader(ruleStream, ENCODING));
     }
 
     private int getPropertiesOrderNumber(String property) {
         if (property.equals(POSITION)) {
             return POSITION_INDEX;
         }
 
         return getOrderNumber(property, properties);
     }
 
     private int getValueOrderNumber(String value, String property) {
         int indexOfProperty = getPropertiesOrderNumber(property);
         if (indexOfProperty == POSITION_INDEX) {
             int position;
             try {
                 position = Integer.parseInt(value) - 1;
             } catch (NumberFormatException e) {
                 throw new NumberFormatException("Position number in rule file is not an integer: " + value);
             }
             if (position < 0 || iNumberOfHouses <= position) {
                 throw new IllegalArgumentException("Position number in rule file is wrong: " + value);
             }
             return position;
         }
 
         List<String> valueList;
         if (indexOfProperty == values.size()) {
             valueList = new ArrayList<>();
             values.add(valueList);
         } else {
             valueList = values.get(indexOfProperty);
         }
         return getOrderNumber(value, valueList);
     }
 
     private int getOrderNumber(String item, List<String> items) {
         for (int index = 0; index < items.size(); index++) {
             if (items.get(index).equals(item)) {
                 return index;
             }
         }
 
         items.add(item);
         return items.size() - 1;
     }
 }
