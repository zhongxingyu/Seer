 package com.atlassian.plugin.osgi.factory.transform.stage;
 
 import com.atlassian.plugin.PluginAccessor;
 import com.atlassian.plugin.PluginParseException;
 import com.atlassian.plugin.JarPluginArtifact;
 import com.atlassian.plugin.osgi.factory.transform.TransformContext;
 import com.atlassian.plugin.osgi.factory.transform.model.SystemExports;
 import com.atlassian.plugin.osgi.hostcomponents.HostComponentRegistration;
 import com.atlassian.plugin.test.PluginJarBuilder;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.spi.Filter;
 import org.osgi.framework.Constants;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.jar.Attributes;
 import java.util.jar.Manifest;
 
 import javax.print.attribute.AttributeSet;
 
 import junit.framework.TestCase;
 
 public class TestGenerateManifestStage extends TestCase
 {
     private GenerateManifestStage stage;
 
     @Override
     public void setUp()
     {
         stage = new GenerateManifestStage();
     }
 
     public void testGenerateManifest() throws Exception
     {
         final File file = new PluginJarBuilder()
                 .addFormattedResource(
                         "atlassian-plugin.xml",
                         "<atlassian-plugin key='com.atlassian.plugins.example' name='Example Plugin'>",
                         "  <plugin-info>",
                         "    <description>",
                         "      A sample plugin for demonstrating the file format.",
                         "    </description>",
                         "    <version>1.1</version>",
                         "    <vendor name='Atlassian Software Systems Pty Ltd' url='http://www.atlassian.com'/>",
                         "  </plugin-info>",
                         "</atlassian-plugin>")
                 .addFormattedJava(
                         "com.mycompany.myapp.Foo",
                         "package com.mycompany.myapp; public class Foo {}")
                 .build();
 
         final TransformContext context = new TransformContext(Collections.<HostComponentRegistration> emptyList(), SystemExports.NONE, new JarPluginArtifact(file),
             null, PluginAccessor.Descriptor.FILENAME);
 
         final Attributes attrs = executeStage(context);
 
         assertEquals("1.1", attrs.getValue(Constants.BUNDLE_VERSION));
         assertEquals("com.atlassian.plugins.example", attrs.getValue(Constants.BUNDLE_SYMBOLICNAME));
         assertEquals("A sample plugin for demonstrating the file format.", attrs.getValue(Constants.BUNDLE_DESCRIPTION));
         assertEquals("Atlassian Software Systems Pty Ltd", attrs.getValue(Constants.BUNDLE_VENDOR));
         assertEquals("http://www.atlassian.com", attrs.getValue(Constants.BUNDLE_DOCURL));
         assertEquals(null, attrs.getValue(Constants.EXPORT_PACKAGE));
         assertEquals(".", attrs.getValue(Constants.BUNDLE_CLASSPATH));
         assertEquals("*;timeout:=60", attrs.getValue("Spring-Context"));
         assertEquals(null, attrs.getValue(Constants.IMPORT_PACKAGE));
 
     }
 
     public void testGenerateManifestWithProperInferredImports() throws Exception
     {
         final File file = new PluginJarBuilder().addPluginInformation("someKey", "someName", "1.0").build();
         final TransformContext context = new TransformContext(null, SystemExports.NONE, new JarPluginArtifact(file), null, PluginAccessor.Descriptor.FILENAME);
         context.getExtraImports().add(AttributeSet.class.getPackage().getName());
         final Attributes attrs = executeStage(context);
 
         assertTrue(attrs.getValue(Constants.IMPORT_PACKAGE).contains(AttributeSet.class.getPackage().getName()));
 
     }
 
     public void testGenerateManifestMergeHostComponentImportsWithExisting() throws Exception
     {
         final File plugin = new PluginJarBuilder("plugin")
                 .addFormattedResource("META-INF/MANIFEST.MF",
                         "Manifest-Version: 1.0",
                         "Import-Package: javax.swing",
                         "Bundle-SymbolicName: my.foo.symbolicName",
                         "Bundle-ClassPath: .,foo")
                 .addResource("foo/bar.txt", "Something")
                 .addPluginInformation("innerjarcp", "Some name", "1.0")
                 .build();
 
         final TransformContext context = new TransformContext(null, SystemExports.NONE, new JarPluginArtifact(plugin), null, PluginAccessor.Descriptor.FILENAME);
         context.getExtraImports().add(AttributeSet.class.getPackage().getName());
         final Attributes attrs = executeStage(context);
         assertEquals("my.foo.symbolicName", attrs.getValue(Constants.BUNDLE_SYMBOLICNAME));
         assertEquals(".,foo", attrs.getValue(Constants.BUNDLE_CLASSPATH));
         final String importPackage = attrs.getValue(Constants.IMPORT_PACKAGE);
         assertTrue(importPackage.contains(AttributeSet.class.getPackage().getName()+","));
         assertTrue(importPackage.contains("javax.swing"));
 
     }
 
     public void testGenerateManifestFailNoSpringWithExisting() throws Exception
     {
         final File plugin = new PluginJarBuilder("plugin")
                 .addFormattedResource("META-INF/MANIFEST.MF",
                         "Manifest-Version: 1.0",
                         "Import-Package: javax.swing",
                         "Bundle-SymbolicName: my.foo.symbolicName",
                         "Bundle-ClassPath: .,foo")
                 .addResource("foo/bar.txt", "Something")
                 .addPluginInformation("innerjarcp", "Some name", "1.0")
                 .build();
 
         final TransformContext context = new TransformContext(null, SystemExports.NONE, new JarPluginArtifact(plugin), null, PluginAccessor.Descriptor.FILENAME);
         context.setShouldRequireSpring(true);
         context.getExtraImports().add(AttributeSet.class.getPackage().getName());
         try
         {
             executeStage(context);
             fail();
         }
         catch (PluginParseException ex)
         {
             //success
         }
     }
 
     public void testGenerateManifest_innerjars() throws URISyntaxException, PluginParseException, IOException
     {
         final File innerJar = new PluginJarBuilder("innerjar1").build();
         final File innerJar2 = new PluginJarBuilder("innerjar2").build();
         final File plugin = new PluginJarBuilder("plugin")
                 .addFile("META-INF/lib/innerjar.jar", innerJar)
                 .addFile("META-INF/lib/innerjar2.jar", innerJar2)
                 .addPluginInformation("innerjarcp", "Some name", "1.0")
                 .build();
 
         final TransformContext context = new TransformContext(null, SystemExports.NONE, new JarPluginArtifact(plugin), null, PluginAccessor.Descriptor.FILENAME);
         final Attributes attrs = executeStage(context);
 
         final Collection classpathEntries = Arrays.asList(attrs.getValue(Constants.BUNDLE_CLASSPATH).split(","));
         assertEquals(3, classpathEntries.size());
         assertTrue(classpathEntries.contains("."));
         assertTrue(classpathEntries.contains("META-INF/lib/innerjar.jar"));
         assertTrue(classpathEntries.contains("META-INF/lib/innerjar2.jar"));
     }
 
     public void testGenerateManifest_innerjarsInImports() throws Exception, PluginParseException, IOException
     {
         final File innerJar = new PluginJarBuilder("innerjar")
                 .addFormattedJava("my.Foo",
                         "package my;",
                         "import org.apache.log4j.Logger;",
                         "public class Foo{",
                         "   Logger log;",
                         "}")
                 .build();
         assertNotNull(innerJar);
         final File plugin = new PluginJarBuilder("plugin")
                 .addJava("my.Bar", "package my;import org.apache.log4j.spi.Filter; public class Bar{Filter log;}")
                 .addFile("META-INF/lib/innerjar.jar", innerJar)
                 .addPluginInformation("innerjarcp", "Some name", "1.0")
                 .build();
 
         final TransformContext context = new TransformContext(null, SystemExports.NONE, new JarPluginArtifact(plugin), null, PluginAccessor.Descriptor.FILENAME);
         final Attributes attrs = executeStage(context);
 
         assertEquals("1.0", attrs.getValue(Constants.BUNDLE_VERSION));
         assertEquals("innerjarcp", attrs.getValue(Constants.BUNDLE_SYMBOLICNAME));
 
         final Collection classpathEntries = Arrays.asList(attrs.getValue(Constants.BUNDLE_CLASSPATH).split(","));
         assertEquals(2, classpathEntries.size());
         assertTrue(classpathEntries.contains("."));
         assertTrue(classpathEntries.contains("META-INF/lib/innerjar.jar"));
 
         final Collection imports = Arrays.asList(attrs.getValue("Import-Package").split(","));
         assertEquals(2, imports.size());
         assertTrue(imports.contains(Logger.class.getPackage().getName() + ";resolution:=\"optional\""));
         assertTrue(imports.contains(Filter.class.getPackage().getName() + ";resolution:=\"optional\""));
     }
 
     public void testGenerateManifestWithBundleInstructions() throws Exception
     {
         final File plugin = new PluginJarBuilder("plugin")
                 .addPluginInformation("test.plugin", "test.plugin", "1.0")
                 .addJava("foo.MyClass", "package foo; public class MyClass{}")
                 .addJava("foo.internal.MyPrivateClass", "package foo.internal; public class MyPrivateClass{}")
                 .build();
 
         final TransformContext context = new TransformContext(null, SystemExports.NONE, new JarPluginArtifact(plugin), null, PluginAccessor.Descriptor.FILENAME);
         context.getBndInstructions().put("Export-Package", "!*.internal.*,*");
         final Attributes attrs = executeStage(context);
         assertEquals("test.plugin", attrs.getValue(Constants.BUNDLE_SYMBOLICNAME));
         assertEquals("foo", attrs.getValue(Constants.EXPORT_PACKAGE));
     }
 
     public void testGenerateManifestWithHostAndExternalImports() throws Exception
     {
         final File plugin = new PluginJarBuilder("plugin")
                 .addPluginInformation("test.plugin", "test.plugin", "1.0")
                 .build();
 
         SystemExports exports = new SystemExports("foo.bar,foo.baz;version=\"1.0\"");
         final TransformContext context = new TransformContext(null, exports, new JarPluginArtifact(plugin), null, PluginAccessor.Descriptor.FILENAME);
         context.getBndInstructions().put("Import-Package", "foo.bar,foo.baz");
         final Attributes attrs = executeStage(context);
         assertEquals("test.plugin", attrs.getValue(Constants.BUNDLE_SYMBOLICNAME));
 
         String imports = attrs.getValue(Constants.IMPORT_PACKAGE);
         assertTrue(imports.contains("foo.baz;version=\"[1.0,1.0]\""));
         assertTrue(imports.contains("foo.bar"));
     }
 
     private Attributes executeStage(final TransformContext context) throws IOException
     {
         stage.execute(context);
         final Manifest mf = new Manifest(new ByteArrayInputStream(context.getFileOverrides().get("META-INF/MANIFEST.MF")));
         final Attributes attrs = mf.getMainAttributes();
         return attrs;
     }
 }
