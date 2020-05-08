 package sriracha.frontend.model.elements;
 
 import sriracha.frontend.model.ScalarProperty;
 import sriracha.frontend.*;
 import sriracha.frontend.model.*;
 
 import java.io.Serializable;
 
 /**
  * Created with IntelliJ IDEA.
  * User: shadiasfour
  * Date: 13-11-08
  * Time: 1:27 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Diode extends TwoPortElement implements Serializable{

    /**
     * Indicate the amount of diode create up to now.
     * Used to created matching diode model names.
     */
    private static int diodeNum = 0;

     private transient Property[] properties;
 
     private float is = 1;
     private String isUnit = "mA";
 
     private float  vt = 1;
     private String vtUnit = "mV";
 
     public Diode(CircuitElementManager elementManager)
     {
         super(elementManager);
     }
 
     public Property[] getProperties()
     {
         ScalarProperty IS_Property = new ScalarProperty("Current", "A") {
             @Override
             public String getValue() {
                 return String.valueOf(is);
             }
 
             @Override
             public void _trySetValue(String value) {
                 float floatValue = Float.parseFloat(value);
                 if (floatValue <= 0)
                     throw new NumberFormatException("Current value must be greater than zero");
                 is = floatValue;
             }
 
             @Override
             public String getUnit() {
                 return isUnit == null || isUnit.isEmpty() ? this.getBaseUnit() : isUnit;  //To change body of implemented methods use File | Settings | File Templates.
             }
 
             @Override
             public void setUnit(String unit) {
                 isUnit = unit;
             }
         };
 
         ScalarProperty VT_Property = new ScalarProperty("Threshold Voltage", "V") {
             @Override
             public String getValue() {
                 return String.valueOf(vt);
             }
 
             @Override
             public void _trySetValue(String value) {
                 float floatValue = Float.parseFloat(value);
                 if (floatValue <= 0)
                     throw new NumberFormatException("Threshold Voltage value must be greater than zero");
                 vt = floatValue;
             }
 
             @Override
             public String getUnit() {
                 return vtUnit == null || vtUnit.isEmpty() ? this.getBaseUnit() : vtUnit;  //To change body of implemented methods use File | Settings | File Templates.
             }
 
             @Override
             public void setUnit(String unit) {
                 vtUnit = unit;
             }
         };
         Property properties[] = {IS_Property, VT_Property};
 
         return properties;
     }
 
     @Override
     public String getType()
     {
         return "Diode";
     }
 
     @Override
     public String getNameTemplate()
     {
         return "D%d";
     }
 
    /**
     * The method is still imcomplete, but the idea is to create a matching model line
     * to each of the diode declared.
     *
     * @param nodes
     * @param crawler
     * @return
     */
     @Override
     public String toNetlistString(String[] nodes, NodeCrawler crawler)
     {
        diodeNum++;
        return ".MODEL " + diodeNum + " D (IS="+ is + ScalarProperty.translateUnit(isUnit) +
                 " vt=" + vt + ScalarProperty.translateUnit(vtUnit)  +")\n" + super.toNetlistString(nodes, crawler)
                + diodeNum;
     }
 }
