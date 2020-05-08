 package xbrlcore.junit.instance;
 
 import java.io.File;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 import xbrlcore.instance.Instance;
 import xbrlcore.instance.InstanceFactory;
 import xbrlcore.instance.InstanceOutputter;
 import xbrlcore.instance.InstanceValidator;
 import static org.junit.Assert.*;
 
 /**
  * TODO: Add comment.
  * @author Daniel
  */
 public final class InstanceTestTmp {
 
     /**
      * TODO: Add comment.
      */
     @Test
     public void test1() {
         try {
             InstanceFactory f = InstanceFactory.get();
             Instance i = f.createInstance(new File(
                "xbrl\\test\\baselsolv\\instanz.xml"));
             assertNotNull(i);
             InstanceValidator iv = new InstanceValidator(new File(
                "xbrl\\test\\baselsolv\\instanz.xml"));
             iv.schemaValidation();
             InstanceOutputter io = new InstanceOutputter(i);
             io.getXMLString();
         } catch (Exception ex) {
             System.err.println(ex.toString());
             ex.printStackTrace();
             fail(ex.getMessage());
         }
     }
 
     /**
      * TODO: Add comment.
      */
     @Ignore("produces a StackOverflowError - this has to be checked")
     @Test
     public void largeInstance() {
         try {
             System.out.println("starte ...");
             long start = System.currentTimeMillis();
             InstanceFactory ifa = InstanceFactory.get();
             Instance i = ifa.createInstance(new File(
                "xbrl\\test\\large_instance\\A_instanz_all.xml"));
             assertNotNull(i);
             System.out.println(System.currentTimeMillis() - start);
             System.out.println("alles klar");
         } catch (Exception ex) {
             System.out.println(ex.toString());
             ex.printStackTrace();
             fail(ex.getMessage());
         }
     }
 }
