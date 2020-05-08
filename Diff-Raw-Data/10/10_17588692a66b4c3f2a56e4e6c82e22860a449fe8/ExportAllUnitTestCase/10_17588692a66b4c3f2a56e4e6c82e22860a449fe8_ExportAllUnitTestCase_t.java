 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 package org.jboss.test.deployers.vfs.classloader.test;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import org.jboss.classloader.plugins.system.DefaultClassLoaderSystem;
 import org.jboss.classloader.spi.ClassLoaderSystem;
 import org.jboss.deployers.structure.spi.classloading.ExportAll;
 import org.jboss.deployers.vfs.plugins.classloader.VFSClassLoaderPolicy;
 import org.jboss.test.BaseTestCase;
 import org.jboss.virtual.VFS;
 import org.jboss.virtual.VirtualFile;
 
 /**
  * ExportAllUnitTestCase.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public class ExportAllUnitTestCase extends BaseTestCase
 {
    protected void testExportAll(ExportAll exportAll, Map<String, String> expected, String... urls) throws Exception
    {
       Set<String> empty = Collections.emptySet();
       testExportAll(exportAll, expected, empty, urls);
    }
 
    protected void testExportAll(ExportAll exportAll, Map<String, String> expected, Set<String> empty, String... urls) throws Exception
    {
       testExportAllAbsolute(exportAll, expected, empty, urls);
       testExportAllFromBase(exportAll, expected, empty, urls);
    }
 
    protected void testExportAllCommon(ExportAll exportAll, Map<String, String> expected, Set<String> empty, VirtualFile[] files) throws Exception
    {
       VFSClassLoaderPolicy policy = VFSClassLoaderPolicy.createVFSClassLoaderPolicy(files);
       policy.setExportAll(exportAll);
       
       String[] packageNames = policy.getPackageNames();
       Set<String> actual = makeSet(packageNames);
       assertEquals(expected.keySet(), actual);
       
       ClassLoaderSystem system = new DefaultClassLoaderSystem();
       ClassLoader classLoader = system.registerClassLoaderPolicy(policy);
       
       for (Map.Entry<String, String> entry : expected.entrySet())
       {
          String packageName = entry.getKey();
         String resource = packageName.replace('.', '/') + "/notempty";
          InputStream is = classLoader.getResourceAsStream(resource);
         if (empty.contains(packageName))
             assertNull("Did not expect resource: " + resource, is);
          else
          {
             assertNotNull("Did not find resource: " + resource, is);
             String contents = getContents(is);
             assertEquals(entry.getValue(), contents);
          }
       }
    }
 
    protected void testExportAllFromBase(ExportAll exportAll, Map<String, String> expected, Set<String> empty, String... urls) throws Exception
    {
       URL baseURL = getResource("/classloader");
       assertNotNull(baseURL);
       VirtualFile base = VFS.getRoot(baseURL);
       VirtualFile[] files = new VirtualFile[urls.length];
       for (int i = 0; i < urls.length; ++i)
          files[i]= base.findChild(urls[i]);
       
       testExportAllCommon(exportAll, expected, empty, files);
    }
 
    protected void testExportAllAbsolute(ExportAll exportAll, Map<String, String> expected, Set<String> empty, String... urls) throws Exception
    {
       VirtualFile[] files = new VirtualFile[urls.length];
       for (int i = 0; i < urls.length; ++i)
       {
          String urlString = "/classloader/" + urls[i];
          URL url = getResource(urlString);
          assertNotNull("Expected to find resource: " + urlString, url);
          files[i]= VFS.getRoot(url);
       }
       
       testExportAllCommon(exportAll, expected, empty, files);
    }
    
    public void testExportAllJar1() throws Exception
    {
       Map<String,String> expected = makeSimpleMap("testjar1",
             "",
             "package1",
             "package2", 
             "package2.subpackage1",
             "package2.subpackage2",
             "package2.subpackage3"
       );
 
       testExportAll(ExportAll.ALL, expected, "testjar1");
    }
 
    public void testExportAllJar1NonEmpty() throws Exception
    {
       Map<String, String> expected = makeSimpleMap("testjar1",
             "",
             "package1",
             "package2", 
             "package2.subpackage1",
             "package2.subpackage2",
             "package2.subpackage3"
       );
 
       testExportAll(ExportAll.NON_EMPTY, expected, "testjar1");
    }
    
    public void testExportAllJar2() throws Exception
    {
       Map<String,String> expected = makeSimpleMap("testjar2",
             "",
             "package1"
       );
       
       Set<String> empty = makeSet("");
 
       testExportAll(ExportAll.ALL, expected, empty, "testjar2");
    }
 
    public void testExportAllJar2NonEmpty() throws Exception
    {
       Map<String, String> expected = makeSimpleMap("testjar2",
             "package1"
       );
 
       testExportAll(ExportAll.NON_EMPTY, expected, "testjar2");
    }
    
    public void testExportAllJar1And2() throws Exception
    {
       Map<String,String> expected = makeSimpleMap("testjar1",
             "",
             "package1",
             "package2", 
             "package2.subpackage1",
             "package2.subpackage2",
             "package2.subpackage3"
       );
 
       testExportAll(ExportAll.ALL, expected, "testjar1", "testjar2");
    }
 
    public void testExportAllJar1And2NonEmpty() throws Exception
    {
       Map<String, String> expected = makeSimpleMap("testjar1",
             "",
             "package1",
             "package2", 
             "package2.subpackage1",
             "package2.subpackage2",
             "package2.subpackage3"
       );
 
       testExportAll(ExportAll.NON_EMPTY, expected, "testjar1", "testjar2");
    }
    
    public void testExportAllJar2And1() throws Exception
    {
       Map<String,String> expected = makeComplexMap(
             "", "testjar1",
             "package1", "testjar2",
             "package2", "testjar1",
             "package2.subpackage1", "testjar1",
             "package2.subpackage2", "testjar1",
             "package2.subpackage3", "testjar1"
       );
 
       testExportAll(ExportAll.ALL, expected, "testjar2", "testjar1");
    }
 
    public void testExportAllJar2And1NonEmpty() throws Exception
    {
       Map<String, String> expected = makeComplexMap(
             "", "testjar1",
             "package1", "testjar2",
             "package2", "testjar1",
             "package2.subpackage1", "testjar1",
             "package2.subpackage2", "testjar1",
             "package2.subpackage3", "testjar1"
       );
 
       testExportAll(ExportAll.NON_EMPTY, expected, "testjar2", "testjar1");
    }
 
    protected String getContents(InputStream is) throws Exception
    {
       StringBuilder builder = new StringBuilder();
       InputStreamReader reader = new InputStreamReader(is);
       int character = reader.read();
       while (character != -1)
       {
          builder.append((char) character);
          character = reader.read();
       }
       return builder.toString();
    }
    
    protected Set<String> makeSet(String... elements)
    {
       assertNotNull(elements);
       Set<String> result = new HashSet<String>();
       for (String string : elements)
          result.add(string);
       return result;
    }
    
    protected Map<String,String> makeSimpleMap(String prefix, String... elements)
    {
       assertNotNull(prefix);
       assertNotNull(elements);
       Map<String, String> result = new HashMap<String, String>();
       for (String string : elements)
          result.put(string, prefix + "." + string);
       return result;
    }
    
    protected Map<String,String> makeComplexMap(String... elements)
    {
       assertNotNull(elements);
       Map<String, String> result = new HashMap<String, String>();
       for (int i = 0; i < elements.length; i += 2)
          result.put(elements[i], elements[i+1] + '.' + elements[i]);
       return result;
    }
 
    public static Test suite()
    {
       return new TestSuite(ExportAllUnitTestCase.class);
    }
 
    public ExportAllUnitTestCase(String name) throws Throwable
    {
       super(name);
    }
 }
