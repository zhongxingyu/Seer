 // Copyright (C) 2003 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.console.model;
 
 import junit.framework.TestCase;
 import junit.swingui.TestRunner;
 //import junit.textui.TestRunner;
 
 import java.io.File;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import net.grinder.common.GrinderProperties;
 
 
 /**
  * @author Philip Aston
  * @version $Revision$
  */
 public class TestScriptDistributionFiles extends TestCase {
 
   public static void main(String[] args) {
     TestRunner.run(TestScriptDistributionFiles.class);
   }
 
   public TestScriptDistributionFiles(String name) {
     super(name);
   }
 
   private File[] m_testFiles = {
     new File("a redder shade of neck"),
     new File("on a whiter shade of trash"),
     new File("and this emery board"),
     new File("is giving me a rash"),
   };
 
   private List m_testFilesAsList = Arrays.asList(m_testFiles);
 
   {
     Collections.sort(m_testFilesAsList);
   }
 
   public void testConstructionWithEmptyParameters() throws Exception {
     final ScriptDistributionFiles files =
       new ScriptDistributionFiles("", new GrinderProperties());
 
    assertEquals(new File(".").getCanonicalFile(), files.getRootDirectory());
     assertNull(files.getScriptFile());
     assertEquals(0, files.getAdditionalFiles().length);
   }
 
   public void testConstruction1() throws Exception {
 
     final GrinderProperties properties = new GrinderProperties() { {
       setProperty("rootDirectory", "abc");
       setProperty("scriptFile", "AFile.py");
       setProperty("additionalFiles.1", "anotherFile");
       setProperty("additionalFiles.2", "yetAnotherFile");
       setProperty("additionalFiles.3", "oneMoreAnotherFile");
     }};
     
     final ScriptDistributionFiles files =
       new ScriptDistributionFiles("", properties);
 
     assertEquals(new File("abc"), files.getRootDirectory());
     assertEquals(new File("AFile.py"), files.getScriptFile());
 
     final File[] additionalFiles = files.getAdditionalFiles();
     
     assertEquals("anotherFile", additionalFiles[0].getPath());
     assertEquals("oneMoreAnotherFile", additionalFiles[1].getPath());
     assertEquals("yetAnotherFile", additionalFiles[2].getPath());
   }
 
   public void testConstruction2() throws Exception {
 
     final GrinderProperties properties = new GrinderProperties() { {
       setProperty("foo.rootDirectory", "");
       setProperty("foo.scriptFile", "cope with spaces");
       setProperty("foo.additionalFiles.1", "anotherFile");
       setProperty("foo.additionalFiles.2", "yetAnotherFile");
       setProperty("foo.additionalFiles.3", "oneMoreAnotherFile");
       setProperty("bah.additionalFiles.4", "notThere");
     }};
     
     final ScriptDistributionFiles files =
       new ScriptDistributionFiles("foo.", properties);
 
     assertEquals(new File(""), files.getRootDirectory());
     assertEquals(new File("cope with spaces"), files.getScriptFile());
 
     final File[] additionalFiles = files.getAdditionalFiles();
     
     assertEquals("anotherFile", additionalFiles[0].getPath());
     assertEquals("oneMoreAnotherFile", additionalFiles[1].getPath());
     assertEquals("yetAnotherFile", additionalFiles[2].getPath());
 
     assertEquals(3, additionalFiles.length);
   }
 
   public void testAddToProperties() throws Exception {
     final GrinderProperties properties = new GrinderProperties() { {
       setProperty("bah.rootDirectory", "blind date with a chancer");
       setProperty("bah.scriptFile", "we had oysters and dry lancers");
       setProperty("bah.additionalFiles.0", "and the check when it arrived");
       setProperty("bah.additionalFiles.1", "we went dutch, dutch, dutch");
     }};
 
     final ScriptDistributionFiles files =
       new ScriptDistributionFiles("bah.", properties);
 
     final GrinderProperties properties2 = new GrinderProperties();
     files.addToProperties(properties2);
 
     assertEquals(properties, properties2);
   }
 
   public void testSetRootDirectory() throws Exception {
     final ScriptDistributionFiles files =
       new ScriptDistributionFiles("", new GrinderProperties());
 
     files.setRootDirectory(m_testFiles[0]);
     assertEquals(m_testFiles[0], files.getRootDirectory());
 
     files.setRootDirectory(m_testFiles[1]);
     assertEquals(m_testFiles[1], files.getRootDirectory());
   }
 
   public void testSetFiles() throws Exception {
 
     final ScriptDistributionFiles files =
       new ScriptDistributionFiles("", new GrinderProperties());
 
     files.setFiles(m_testFiles[2], Collections.EMPTY_SET);
     assertEquals(m_testFiles[2], files.getScriptFile());
     assertEquals(0, files.getAdditionalFiles().length);
     
     files.setFiles(new File("foo"), m_testFilesAsList);
     assertEquals(new File("foo"), files.getScriptFile());
 
     assertEquals(m_testFilesAsList,
 		 Arrays.asList(files.getAdditionalFiles()));
   }
 
   public void testEquals() throws Exception {
 
     final ScriptDistributionFiles files1 =
       new ScriptDistributionFiles("", new GrinderProperties());
 
     final ScriptDistributionFiles files2 =
       new ScriptDistributionFiles("", new GrinderProperties());
 
     assertEquals(files1, files2);
     assertEquals(files2, files1);
 
     files1.setRootDirectory(m_testFiles[3]);
     
     assertTrue(!files1.equals(files2));
     assertTrue(!files2.equals(files1));
 
     files2.setRootDirectory(m_testFiles[3]);
     assertEquals(files1, files2);
 
     files2.setFiles(m_testFiles[0], Collections.EMPTY_SET);
     assertTrue(!files1.equals(files2));
 
     files1.setFiles(m_testFiles[0], Collections.EMPTY_SET);
     assertEquals(files1, files2);
 
     files1.setFiles(m_testFiles[1], Collections.EMPTY_SET);
     assertTrue(!files1.equals(files2));
 
     files2.setFiles(m_testFiles[1], Collections.EMPTY_SET);
     assertEquals(files1, files2);
 
     files1.setFiles(m_testFiles[0], m_testFilesAsList);
     assertTrue(!files1.equals(files2));
 
     files2.setFiles(m_testFiles[0], m_testFilesAsList);
     assertEquals(files1, files2);
   }
 
   public void testSerialisation() throws Exception {
 
     final ScriptDistributionFiles original0 =
       new ScriptDistributionFiles("", new GrinderProperties());
     original0.setRootDirectory(m_testFiles[0]);
     original0.setFiles(m_testFiles[1], m_testFilesAsList);
 
     final ScriptDistributionFiles original1 =
       new ScriptDistributionFiles("", new GrinderProperties());
 
     final ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
 
     final ObjectOutputStream objectOutputStream =
       new ObjectOutputStream(byteOutputStream);
 
     objectOutputStream.writeObject(original0);
     objectOutputStream.writeObject(original1);
     objectOutputStream.close();
 
     final ObjectInputStream objectInputStream =
       new ObjectInputStream(
 	new ByteArrayInputStream(byteOutputStream.toByteArray()));
 
     final Object received0 = objectInputStream.readObject();
     final Object received1 = objectInputStream.readObject();
 
     assertEquals(original0, received0);
     assertEquals(original1, received1);
   }
 }
