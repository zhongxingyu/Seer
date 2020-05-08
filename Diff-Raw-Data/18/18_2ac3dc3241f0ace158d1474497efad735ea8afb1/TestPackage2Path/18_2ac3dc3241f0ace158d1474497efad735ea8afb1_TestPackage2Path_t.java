 package org.hackystat.sensor.ant.jdepend;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.util.ArrayList;
 import org.junit.Test;
 
 /**
  * Tests the Package2Path processor.
  * 
  * @author Philip Johnson
  */
 public class TestPackage2Path {
  private String sep = System.getProperty("file.separator");
  private String path1 = "c:" + sep + "foo" + sep + "bar" + sep + "Baz.java";
  private String path2 = "c:" + sep + "foo" + sep + "bar" + sep + "baz" + sep + "Baz.java";
  private String path3 = "c:" + sep + "foo" + sep + "bar" + sep + "Qux.java";

    /**
    * Tests Package2Path by setting up some sample data and running some queries on it.
    * @throws Exception If a program error occurs.
    */
   @Test
   public void testPackage2Path() throws Exception {
     ArrayList<File> fileList = new ArrayList<File>();
     fileList.add(new File(path1));
     fileList.add(new File(path2));
     fileList.add(new File(path3));
     
     Package2Path package2path = new Package2Path(fileList);
    assertEquals("test1", "c:" + sep + "foo" + sep + "bar", package2path.getPath("foo.bar"));
    assertEquals("test2", "c:" + sep + "foo" + sep + "bar" + sep + "baz", 
        package2path.getPath("foo.bar.baz"));
     assertEquals("test3", null, package2path.getPath("foo.bar.baz.qux"));
   }
 
 }
