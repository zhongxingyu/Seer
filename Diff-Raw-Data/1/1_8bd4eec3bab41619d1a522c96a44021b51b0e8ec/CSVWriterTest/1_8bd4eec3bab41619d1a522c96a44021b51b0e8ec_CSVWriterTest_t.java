 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.jmxdatamart.Extractor;
 
 import java.util.HashMap;
 import java.util.Map;
 import org.jmxdatamart.common.CVSCommon;
 import org.jmxdatamart.common.DataType;
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 /**
  *
  * @author Binh Tran <mynameisbinh@gmail.com>
  */
 public class CSVWriterTest {
     
      public void testEnclose() {
          String s = "Hello, World";
          assertEquals(  CVSCommon.GENERAL_ENCLOSE + "Hello, World" + CVSCommon.GENERAL_ENCLOSE,
                         CSVWriter.enclose(s).toString());
      }
      
      @Test
      public void testLineUpResult() {
          Map<Attribute, Object> result;
          CSVWriter csvw = new CSVWriter(
                  new MBeanData("aBean", "MBEAN!!11!", null, true), 
                  "");
          
          result = new HashMap<Attribute, Object>();
          result.put(new Attribute("A", "Alpha", DataType.INT), new Integer(7));
          result.put(new Attribute("B", "Beta", DataType.STRING), "Hello World");
          csvw.writeResult(result);
          
          result = new HashMap<Attribute, Object>();
          result.put(new Attribute("A", "Alpha", DataType.INT), new Integer(42));
          result.put(new Attribute("B", "Beta", DataType.STRING), "Cruel World");
          csvw.writeResult(result);
          
          result = new HashMap<Attribute, Object>();
          result.put(new Attribute("C", "Sigma", DataType.INT), new Integer(-1));
          result.put(new Attribute("B", "Beta", DataType.STRING), "Bye World");
          csvw.writeResult(result);
                  
      }
 }
