 package de.dmc.loggi.processors;
 
 import de.dmc.loggi.exceptions.ConfigurationException;
 import de.dmc.loggi.model.Attribute;
 import de.dmc.loggi.model.Column;
 import org.testng.Assert;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.util.Arrays;
 
 import static org.testng.Assert.assertEquals;
 
 /**
  * @author CptSpaetzle
  */
 
 public class AbstractColumnProcessorTest {
 
     @Test(expectedExceptions = ConfigurationException.class)
     public void testInitializeProcessorNull() throws Exception{
         AbstractColumnProcessor abstractColumnProcessor = new AbstractColumnProcessor(null) {
             @Override
            public Object getColumnValue(String record) {
                 return null;
             }
         };
     }
 
     protected Column createColumn(String name, Attribute... attributes){
         Column column = new Column();
         column.setName(name);
         column.setAttributes(Arrays.asList(attributes));
         return column;
     }
 
     protected Attribute createAttribute(String name, String value){
         Attribute attribute = new Attribute();
         attribute.setName(name);
         attribute.setValue(value);
         return attribute;
     }
 }
