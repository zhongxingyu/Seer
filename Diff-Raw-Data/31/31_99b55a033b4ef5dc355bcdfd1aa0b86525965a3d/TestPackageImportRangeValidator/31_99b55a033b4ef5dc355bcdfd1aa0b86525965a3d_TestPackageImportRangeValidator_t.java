 package com.atlassian.maven.plugins.amps.osgi;
 
 import junit.framework.TestCase;
 
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 
 import static org.mockito.Matchers.anyObject;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 
 import java.util.Iterator;
 import java.util.Map;
 import java.util.HashMap;
 
 /**
  *
  */
 public class TestPackageImportRangeValidator extends TestCase
 {
     private MavenProject project;
     private Log log;
     private PackageImportVersionValidator validator;
 
     @Override
     protected void setUp() throws Exception
     {
         super.setUp();
         project = mock(MavenProject.class);
         log = mock(Log.class);
        validator = new PackageImportVersionValidator(project, log, "foo");
     }
 
     public void testValidateWithProperVersion() throws MojoFailureException
     {
 
         validator.validate("foo.bar;version=\"[1.0,2.0)\"");
         verify(log, never()).warn(anyString());
     }
 
     public void testValidateNull()
     {
         validator.validate(null);
         verify(log, never()).warn(anyString());
     }
 
     public void testValidateEmpty()
     {
         validator.validate("");
         verify(log, never()).warn(anyString());
     }
 
     public void testValidateJustMinimum()
     {
         validator.validate("foo.bar;version=\"1.0\"");
         verify(log, never()).warn(anyString());
     }
 
     public void testValidateNoVersion()
     {
         validator.validate("foo.bar");
         verify(log).warn(anyString());
     }
 
     public void testValidateEmptyVersion()
     {
         validator.validate("foo.bar;version=\"\"");
         verify(log).warn(anyString());
     }
 
     public void testValidateNoVersionOneAttribute()
     {
         validator.validate("foo.bar;bar=foo");
         verify(log).warn(anyString());
     }
 
     public void testCompressImports()
     {
         Map<String, String> imports = PackageImportVersionValidator.compressPackages(new HashMap<String, String>()
         {{
                 put("foo.bar.baz.jim", "1.0");
                 put("foo", "1.0");
                 put("foo.bar.baz.sara", "1.0");
                 put("jim", "2.0");
             }});
         assertEquals(new HashMap<String, String>()
         {{
                 put("foo.bar.baz.*", "1.0");
                 put("foo", "1.0");
                 put("jim", "2.0");
             }}, imports);
     }
 
     public void testCompressImportsNotTooShort()
     {
         Map<String, String> imports = PackageImportVersionValidator.compressPackages(new HashMap<String, String>()
         {{
                 put("foo.bar", "1.0");
                 put("faz.bar", "1.0");
                 put("jim", "2.0");
             }});
         assertEquals(new HashMap<String, String>()
         {{
                 put("foo.bar", "1.0");
                 put("faz.bar", "1.0");
                 put("jim", "2.0");
             }}, imports);
     }
 
     public void testCompressImportsNotTooShort2()
     {
         Map<String, String> imports = PackageImportVersionValidator.compressPackages(new HashMap<String, String>()
         {{
                 put("foo.bar", "1.0");
                 put("fos.bar", "1.0");
                 put("jim", "2.0");
             }});
         assertEquals(new HashMap<String, String>()
         {{
                 put("foo.bar", "1.0");
                 put("fos.bar", "1.0");
                 put("jim", "2.0");
             }}, imports);
     }
 
     public void testCompressImportsNotIncludeOutcast()
     {
         Map<String, String> imports = PackageImportVersionValidator.compressPackages(new HashMap<String, String>()
         {{
                 put("foo.bar.sara.jim", "1.0");
                 put("foo", "1.0");
                 put("foo.bar.sara.jim.baz", "1.0");
                 put("foo.jim", "2.0");
             }});
         assertEquals(new HashMap<String, String>()
         {{
                 put("foo", "1.0");
                 put("foo.bar.sara.jim*", "1.0");
                 put("foo.jim", "2.0");
             }}, imports);
     }
 
     public void testCompressImportsNotAlwaysFullPackage()
     {
         Map<String, String> imports = PackageImportVersionValidator.compressPackages(new HashMap<String, String>()
         {{
                 put("foo.bar", "1.0");
                 put("foo.bar.baz", "1.0");
                 put("foo.bara", "2.0");
             }});
         assertEquals(new HashMap<String, String>()
         {{
                 put("foo.bar", "1.0");
                 put("foo.bar.baz", "1.0");
                 put("foo.bara", "2.0");
             }}, imports);
     }
 
     public void testCompressImportsRealWorld()
     {
         // note in this example, most of the packages were not on the classpath, and therefore, 
         Map<String, String> imports = PackageImportVersionValidator.compressPackages(new HashMap<String, String>()
         {{
                 put("com.atlassian.sal.api", "2.0");
                 put("com.mchange.v2.c3p0", "0.0.0");
                 put("javax.naming", "0.0.0");
                 put("javax.naming.spi", "0.0.0");
                 put("javax.net", "0.0.0");
                 put("javax.net.ssl", "0.0.0");
                 put("javax.security.cert", "0.0.0");
                 put("javax.servlet", "0.0.0");
                 put("javax.servlet.http", "0.0.0");
                 put("javax.sql", "0.0.0");
                 put("javax.swing", "0.0.0");
                 put("javax.swing.border", "0.0.0");
                 put("javax.swing.event", "0.0.0");
                 put("javax.swing.table", "0.0.0");
                 put("javax.swing.tree", "0.0.0");
                 put("org.apache.commons.dbcp", "0.0.0");
                 put("org.apache.lucene.analysis", "0.0.0");
                 put("org.apache.lucene.document", "0.0.0");
                 put("org.apache.lucene.index", "0.0.0");
                 put("org.apache.lucene.queryParser", "0.0.0");
                 put("org.apache.lucene.search", "0.0.0");
                 put("org.apache.lucene.search.similar", "0.0.0");
                 put("org.apache.lucene.store", "0.0.0");
                 put("org.apache.tools.ant", "0.0.0");
                 put("org.apache.tools.ant.types", "0.0.0");
                 put("org.logicalcobwebs.proxool", "0.0.0");
                 put("org.osgi.framework", "1.5");
                 put("org.osgi.util.tracker", "1.4");
                 put("org.slf4j", "1.5");
                 put("snaq.db", "0.0.0");
                 put("sun.security.action", "0.0.0");
                 put("org.apache.lucene.queryParser", "0.0.0");
                 put("org.apache.tools.ant.types", "0.0.0");
                 put("org.apache.lucene.document", "0.0.0");
             }});
         assertMapEquals(new HashMap<String, String>()
         {{
                 put("com.atlassian.sal.api", "2.0");
                 put("com.mchange.v2.c3p0", "0.0.0");
                 put("javax.naming", "0.0.0");
                 put("javax.naming.spi", "0.0.0");
                 put("javax.net", "0.0.0");
                 put("javax.net.ssl", "0.0.0");
                 put("javax.security.cert", "0.0.0");
                 put("javax.servlet", "0.0.0");
                 put("javax.servlet.http", "0.0.0");
                 put("javax.sql", "0.0.0");
                 put("javax.swing", "0.0.0");
                 put("javax.swing.border", "0.0.0");
                 put("javax.swing.event", "0.0.0");
                 put("javax.swing.table", "0.0.0");
                 put("javax.swing.tree", "0.0.0");
                 put("org.apache.commons.dbcp", "0.0.0");
                 put("org.apache.lucene.*", "0.0.0");
                 put("org.apache.tools.ant*", "0.0.0");
                 put("org.logicalcobwebs.proxool", "0.0.0");
                 put("org.osgi.framework", "1.5");
                 put("org.osgi.util.tracker", "1.4");
                 put("org.slf4j", "1.5");
                 put("snaq.db", "0.0.0");
                 put("sun.security.action", "0.0.0");
             }}, imports);
     }
 
     private void assertMapEquals(HashMap<String, String> expected, Map<String, String> actual)
     {
         for (Iterator<Map.Entry<String,String>> i = expected.entrySet().iterator(); i.hasNext(); )
         {
             Map.Entry<String,String> entry = i.next();
             if (actual.containsKey(entry.getKey()) && actual.get(entry.getKey()).equals(entry.getValue()))
             {
                 i.remove();
                 actual.remove(entry.getKey());
             }
         }
 
         if (!expected.isEmpty() || !actual.isEmpty())
         {
             StringBuilder sb = new StringBuilder();
             sb.append("Missing expected entries:\n");
             for (Map.Entry<String,String> entry : expected.entrySet())
             {
                 sb.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
             }
             sb.append("\nUnique actual entries:\n");
             for (Map.Entry<String,String> entry : actual.entrySet())
             {
                 sb.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
             }
             fail(sb.toString());
         }
     }
 }
