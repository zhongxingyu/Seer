 package com.atlassian.plugin.osgi.util;
 
 import com.atlassian.plugin.osgi.container.impl.DefaultPackageScannerConfiguration;
 import com.atlassian.plugin.osgi.hostcomponents.HostComponentRegistration;
 import com.atlassian.plugin.osgi.hostcomponents.impl.MockRegistration;
 import junit.framework.TestCase;
 import org.twdata.pkgscanner.ExportPackage;
 
 import javax.print.attribute.AttributeSet;
 import javax.print.attribute.HashAttributeSet;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 import java.util.ArrayList;
 import java.util.List;
 
 public class TestOsgiHeaderUtil extends TestCase {
 
     public void testConstructAutoExports()
     {
         List<ExportPackage> exports = new ArrayList<ExportPackage>();
         exports.add(new ExportPackage("foo.bar", "1.0"));
         exports.add(new ExportPackage("foo.bar", "1.0-asdf-asdf"));
         StringBuilder sb = new StringBuilder();
         OsgiHeaderUtil.constructAutoExports(sb, exports);
 
        assertEquals("foo.bar;version=1.0,foo.bar,", sb.toString());
     }
 
     public void testDetermineExportsIncludeServiceInterfaces()
     {
         List<HostComponentRegistration> regs = new ArrayList<HostComponentRegistration> () {{
             add(new MockRegistration(new HashAttributeSet(), AttributeSet.class));
             add(new MockRegistration(new DefaultTableModel(), TableModel.class));
         }};
         String imports = OsgiHeaderUtil.determineExports(regs, new DefaultPackageScannerConfiguration());
         assertNotNull(imports);
         System.out.println(imports.replace(',','\n'));
         assertTrue(imports.contains(AttributeSet.class.getPackage().getName()));
         assertTrue(imports.contains("javax.swing.event"));
     }
 }
