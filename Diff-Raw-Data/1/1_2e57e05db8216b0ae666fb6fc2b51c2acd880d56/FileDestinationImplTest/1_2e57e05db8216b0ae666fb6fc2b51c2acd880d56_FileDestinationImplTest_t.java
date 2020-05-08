 /*
  * SoCoFo - Another source code formatter
  * Copyright (C) 2013  Dirk Strauss
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 /**
  * 
  */
 package com.googlecode.socofo.core.impl.io;
 
 import static org.testng.Assert.assertNull;
 import static org.testng.Assert.assertTrue;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.inject.Inject;
 
 import org.testng.annotations.Guice;
 import org.testng.annotations.Test;
 
 import com.googlecode.socofo.core.api.FileDestination;
 import com.googlecode.socofo.core.impl.TestInjectionPlan;
 
 /**
  * The fileDest tests.
  * 
  * @author Dirk Strauss
  * @version 1.0
  */
 @Guice(modules = { TestInjectionPlan.class })
@Test(singleThreaded = true)
 public class FileDestinationImplTest {
     /**
      * The test object.
      */
     @Inject
     private FileDestination to;
     
     /**
      * Test method for
      * {@link com.googlecode.socofo.core.provider.FileDestinationImpl#writeContent(java.lang.String, java.lang.String)}
      * .
      * 
      * @throws IOException
      *             if an IO error occurred
      */
     @Test
     public final void testWriteContent() throws IOException {
         final File targetFile = File.createTempFile("scf-filedestinationtest", ".txt");
         targetFile.delete();
         targetFile.deleteOnExit();
         to.setFile(targetFile);
         to.writeContent(null, null);
         assertTrue(!targetFile.exists(), "Target file exists, but should not (yet)!");
         to.writeContent("", null);
         assertTrue(targetFile.exists(), "Target file is not present but should!");
     }
     
     /**
      * Test method for
      * {@link com.googlecode.socofo.core.provider.FileDestinationImpl#setFile(java.io.File)} .
      */
     @Test
     public final void testSetFileNull() {
         to.setFile(null);
     }
     
     @Test
     public final void testSetFileCorrect() {
         to.setFile(new File("target/pom_delme.xml"));
     }
     
     @Test
     public final void testParseDestination() {
         assertNull(to.parseDestination(null, null, null));
     }
     
     public final void testParseDestination3() {
         // assertNull(to.parseDestination("backup", null, null));
         // assertNull(to.parseDestination("backup", null, ""));
     }
     
     public final void testParseDestination4() {
         // assertNull(to.parseDestination("backup", null, "backup3/Test.java"));
     }
     
 }
