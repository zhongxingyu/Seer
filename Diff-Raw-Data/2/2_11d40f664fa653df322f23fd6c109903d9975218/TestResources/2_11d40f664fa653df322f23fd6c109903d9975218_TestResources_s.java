 // Copyright (C) 2004 Philip Aston
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
 
 package net.grinder.console.common;
 
 import junit.framework.TestCase;
 
 import java.io.File;
 
 import net.grinder.testutility.CountingPrintWriter;
 import net.grinder.testutility.FileUtilities;
 
 
 /**
  *  Unit test case for {@link Resources}.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public class TestResources extends TestCase {
 
   private final CountingPrintWriter m_errorWriter =
     new CountingPrintWriter();
 
   public void testResources() throws Exception {
     final Resources resources = new Resources(getClass().getName());
     final Resources resources2 = new Resources("TestResources");
 
     resources.setErrorWriter(m_errorWriter);
     resources2.setErrorWriter(m_errorWriter);
 
     assertEquals("file1", resources.getString("resourceFile"));
     assertEquals("file2", resources2.getString("resourceFile"));
 
     assertTrue(!m_errorWriter.called());
   }
   
   public void testGetString() throws Exception {
     final Resources resources = new Resources(getClass().getName());
     resources.setErrorWriter(m_errorWriter);
 
     assertEquals("", resources.getString("notthere"));
     assertTrue(m_errorWriter.called());
 
     assertNull(resources.getString("notthere", false));
     assertTrue(!m_errorWriter.called());
 
     assertEquals("", resources.getString("notthere", true));
     assertTrue(m_errorWriter.called());
 
     assertEquals("A property value", resources.getString("key"));
     assertTrue(!m_errorWriter.called());
   }
 
 
   public void testGetImageIcon() throws Exception {
     final Resources resources = new Resources(getClass().getName());
     resources.setErrorWriter(m_errorWriter);
 
     assertNull(resources.getImageIcon("notthere"));
     assertTrue(!m_errorWriter.called());
 
     assertNull(resources.getImageIcon("notthere", true));
     assertTrue(m_errorWriter.called());
 
     assertNull(resources.getImageIcon("notthere", false));
     assertTrue(!m_errorWriter.called());
 
     assertNull(resources.getImageIcon("resourceFile", false));
     assertTrue(m_errorWriter.called());
 
     assertNotNull(resources.getImageIcon("image", false));
     assertTrue(!m_errorWriter.called());
   }
 
   public void testGetStringFromFile() throws Exception {
     final Resources resources = new Resources(getClass().getName());
     resources.setErrorWriter(m_errorWriter);
 
     assertNull(resources.getStringFromFile("notthere", false));
     assertTrue(!m_errorWriter.called());
 
     assertNull(resources.getStringFromFile("notthere", true));
     assertTrue(m_errorWriter.called());
 
     assertNull(resources.getStringFromFile("resourceFile", false));
     assertTrue(m_errorWriter.called());
 
     final String helloWorld = resources.getStringFromFile("aFile", true);
     assertTrue(!m_errorWriter.called());
     assertEquals("Hello world\n", helloWorld);
 
     final File file =
       new File(
        Resources.class.getResource("resources/helloWorld.txt").getFile());
 
     FileUtilities.setCanAccess(file, false);
 
     final String noResource = resources.getStringFromFile("aFile", false);
     assertNull(noResource);
     assertTrue(m_errorWriter.called());
 
     FileUtilities.setCanAccess(file, true);;
   }
 }
