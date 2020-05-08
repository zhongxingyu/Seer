 package com.seitenbau.micgwaf.generator;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.net.URLClassLoader;
import java.util.Collection;
 
 import javax.tools.JavaCompiler;
 import javax.tools.ToolProvider;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.Before;
 import org.junit.Test;
 
 public class GeneratedSourcesTest
 {
   File compileRootDir = new File("target/manually-compiled-classes");
   
   @Before
   public void generateAndCompile() throws Exception
   {
     File componentDir = new File("src/test/resources/com/seitenbau/micgwaf/page");
     File generatedSourcesDir = new File("target/generated-sources");
     File generatedExtensionsDir = new File("target/generated-extension-sources");
     
     File compileRootDir = new File("target/manually-compiled-classes");
 
     FileUtils.deleteDirectory(generatedSourcesDir);
     FileUtils.deleteDirectory(generatedExtensionsDir);
     FileUtils.deleteDirectory(compileRootDir);
     
     Generator generator = new Generator();
     generator.generateComponent(
         componentDir, 
         generatedSourcesDir, 
         generatedExtensionsDir,
         "com.seitenbau.micgwaf.test.generated");
 
    Collection<File> files = FileUtils.listFiles(generatedSourcesDir, new String[] {"java"}, true);
    files.addAll(FileUtils.listFiles(generatedExtensionsDir, new String[] {"java"}, true));
    String[] args = new String[files.size() + 6];
    int i = 6;
    for (File file : files)
    {
      args[i] = file.getAbsolutePath();
      ++i;
    }
    args[0] = "-g";
    args[1] = "-d";
    args[2] = compileRootDir.getAbsolutePath();
    args[3] = "-verbose";
    args[4] = "-sourcepath";
    args[5] = generatedSourcesDir.getAbsolutePath() + ";" + generatedExtensionsDir.getAbsolutePath();

     compileRootDir.mkdirs();
     JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    compiler.run(null, null, null, args);
}
   
   
   @Test
   public void testRender() throws Exception
   {
     URLClassLoader classLoader = URLClassLoader.newInstance(
         new URL[] { compileRootDir.toURI().toURL(), new File("target/classes").toURI().toURL() }, 
         null );
     Class<?> cls = Class.forName(
         "com.seitenbau.micgwaf.test.generated.root.RootExtension",
         true, 
         classLoader);
     Constructor<?> rootConstructor = cls.getConstructors()[0];
     Object root = rootConstructor.newInstance(new Object[] {null});
     StringWriter stringWriter = new StringWriter();
     Method renderMethod = cls.getMethod("render", Writer.class);
     renderMethod.invoke(root, stringWriter);
     System.out.println(stringWriter);
     File componentDir = new File("src/test/resources/com/seitenbau/micgwaf/page");
     String expected = FileUtils.readFileToString(new File(componentDir, "expected/expected.xhtml"));
     expected = expected.replace("\r\n", "\n");
     assertEquals(expected, stringWriter.toString());
   }
 }
