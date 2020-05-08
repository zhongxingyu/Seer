 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package madsdf.ardrone.utils;
 
 import com.google.common.collect.Lists;
 import java.io.StringReader;
 import java.util.List;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author julien
  */
 public class DataFileReaderTest {
     @Test
     public void testReadAll() throws Exception {
         final String data =
                 "COMMAND 5 SAMPLE 7\n" +
                 "Accel X : 2803;2815;2797;2776;\n" +
                 "Accel Y : 2311;2316;2291;2294;\n" +
                 "Accel Z : 1691;1680;1697;1722;\n" +
                 "Gyro X : 1832;1834;1842;1844;\n" +
                 "Gyro Y : 1661;1661;1661;1664;\n" +
                "Gyro Z : 1840;1840;1843;1846;\n";
         
         System.out.println("readAll");
         DataFileReader instance = new DataFileReader(new StringReader(data));
         DataFileReader.Sample s1 = new DataFileReader.Sample(5, 7,
                 new float[][]{
                     {2803, 2815, 2797, 2776},
                     {2311, 2316, 2291, 2294},
                     {1691, 1680, 1697, 1722}},
                 new float[][]{
                     {1832, 1834, 1842, 1844},
                     {1661, 1661, 1661, 1664},
                     {1840, 1840, 1843, 1846}}
                 );
        List<DataFileReader.Sample> expResult = Lists.newArrayList(s1);
         List result = instance.readAll();
         assertEquals(expResult, result);
     }
 }
