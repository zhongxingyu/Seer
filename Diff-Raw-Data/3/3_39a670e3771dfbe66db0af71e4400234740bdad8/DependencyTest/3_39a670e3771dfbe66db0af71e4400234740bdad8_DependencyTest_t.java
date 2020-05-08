 package net.codjo.util;
 import java.io.File;
 import net.codjo.util.file.FileUtil;
 import org.junit.Test;
 
 import static org.junit.Assert.assertTrue;
 /**
  *
  */
 public class DependencyTest {
 
     @Test
     public void test_dependency() throws Exception {
        String pomContent = FileUtil.loadContent(new File("pom.xml")).replaceAll("\r", "");
         assertTrue("Cette librairie ne doit avoir aucune dpendance (hors JUnit)",
                    pomContent.contains("    <dependencies>\n"
                                        + "        <dependency>\n"
                                        + "            <groupId>junit</groupId>\n"
                                        + "            <artifactId>junit</artifactId>\n"
                                        + "            <scope>test</scope>\n"
                                        + "        </dependency>\n"
                                        + "    </dependencies>"));
     }
 }
