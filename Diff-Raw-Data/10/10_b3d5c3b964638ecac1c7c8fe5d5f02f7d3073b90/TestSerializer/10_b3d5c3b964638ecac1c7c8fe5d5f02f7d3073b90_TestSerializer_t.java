 package nl.astraeus.persistence.serialize;
 
 import nl.astraeus.persistence.Persistent;
 import nl.astraeus.persistence.SimpleSerializer;
 import org.junit.Ignore;
 
 import java.io.*;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 /**
  * User: rnentjes
  * Date: 7/7/12
  * Time: 10:25 PM
  */
 @Ignore
 public class TestSerializer {
     public static void main(String [] args) throws Exception {
         new TestSerializer();
     }
 
     public static class SimpleObject implements Serializable {
 
         private String test = "TEST";
 
     }
 
     public static class SimpleModel implements Persistent<Long> {
 
         private Long id;
 
         public Long getId() {
             return id;
         }
 
         public int compareTo(Object o) {
             return 0;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         @Override
         public SimpleModel clone() throws CloneNotSupportedException {
             return (SimpleModel)super.clone();
         }
     }
 
     public static class TestSer extends SimpleModel {
 
     }
 
     public static class TestSer2 extends SimpleModel {
 
         private TestSer testSer = new TestSer();
         private Collection c = new LinkedList<String>();
         private Collection c2 = new TreeSet<String>();
 
         public TestSer2() {
             c.add("Test");
 
             c2.add("Test1");
             c2.add("Test2");
             SortedSet bla;
             TreeSet ts;
         }
     }
 
     public TestSerializer() throws Exception {
         writeObjectTest();
     }
 
 
     public void writeObjectTest() throws Exception {
         TestSer2 test2 = new TestSer2();
 
         File file = new File("test.out");
         System.out.println(file.getCanonicalPath());
         FileOutputStream fos = new FileOutputStream(file);
         DataOutputStream dos = new DataOutputStream(fos);
 
         SimpleSerializer ser = new SimpleSerializer();
 
         ser.writeSingleObject(dos, new SimpleObject());
 
         dos.close();
         fos.close();
     }
 
     public void writeObjectsTest() throws Exception {
         SimpleSerializer ser = new SimpleSerializer();
 
         TestSer test = new TestSer();
         File file = new File("test.out");
         System.out.println(file.getCanonicalPath());
         FileOutputStream fos = new FileOutputStream(file);
         ser.writeObject(fos, test);
         fos.close();
 
         TestSer2 test2 = new TestSer2();
         file = new File("test2.out");
         System.out.println(file.getCanonicalPath());
         fos = new FileOutputStream(file);
         ser.writeObject(fos, test2);
         fos.close();
 
         FileInputStream fis = new FileInputStream(file);
         Object object = ser.readObject(fis);
         System.out.println("Read object: "+object);
         fis.close();
     }
 
 
 }
