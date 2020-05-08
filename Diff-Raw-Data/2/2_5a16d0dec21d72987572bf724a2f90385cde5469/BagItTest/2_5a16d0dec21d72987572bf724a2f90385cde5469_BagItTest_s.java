 package no.uio.duo.bagit;
 
 /*
 Copyright (c) 2012, University of Oslo
 
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 * Neither the name of the University of Oslo nor the
 names of its contributors may be used to endorse or promote products
 derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE UNIVERSITY OF OSLO BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 import static org.junit.Assert.*;
 
 import gov.loc.repository.bagit.*;
 import gov.loc.repository.bagit.filesystem.DirNode;
 import gov.loc.repository.bagit.filesystem.FileSystem;
 import gov.loc.repository.bagit.filesystem.FileSystemNode;
 import gov.loc.repository.bagit.filesystem.impl.FileFileSystem;
 import gov.loc.repository.bagit.filesystem.impl.ZipFileSystem;
 import gov.loc.repository.bagit.impl.ManifestWriterImpl;
 import gov.loc.repository.bagit.utilities.FormatHelper;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.LineIterator;
 import org.junit.Test;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.*;
 
 public class BagItTest {
 
     /*
         create a bag from an existing directory in the correct format
      */
 
     @Test
     public void testBagConstructorExistingBag() throws Exception {
 
         BagIt testBagIt = new BagIt(System.getProperty("user.dir") + "/src/test/resources/testbags/testbag1");
         Bag bag = testBagIt.theBag;
 
         try {
 
             assertTrue(bag.verifyValid().isSuccess());
             assertEquals(4, bag.getPayload().size());
             assertEquals(7, bag.getTags().size());
             assertEquals(1, bag.getPayloadManifests().size());
             assertEquals(1, bag.getTagManifests().size());
             BagItTxt bagIt = bag.getBagItTxt();
             assertEquals("UTF-8", bagIt.getCharacterEncoding());
             assertEquals(bag.getVersion().versionString, bagIt.getVersion());
 
 
         } finally {
 
             bag.close();
         }
 
 
     }
 
     /*
         create empty bag
      */
 
     /*
     @Test
     public void testBagConstructorEmptyBag() throws Exception {
 
         BagIt testBagIt = new BagIt();
         Bag bag = testBagIt.theBag;
         //BagFactory bagFactory = testBagIt.bagFactory;
 
         try {
 
             assertFalse(bag.verifyValid().isSuccess());  // we don't have a payload manifest or bagit.txt
 
             // we haven't had any files to the pay load yet
             assertEquals(0, bag.getPayload().size());
 
             // we shouldn't have any tagfiles yet
             assertEquals(0, bag.getTags().size());
 
             // now add some data files
             testBagIt.addFinalFile(new File(System.getProperty("user.dir") + "/src/test/resources/testbags/testfiles/bagitspec.pdf"));
 
             // check we can now fetch the file
             assertNotNull(bag.getBagFile("data/final/bagitspec.pdf"));
 
             // check that the files have been added to the payload
             assertEquals(1, bag.getPayload().size());
 
             // now add some supporting files
             testBagIt.addSupportingFile(new File(System.getProperty("user.dir") + "/src/test/resources/testbags/testfiles/test1.txt"), "open");
             testBagIt.addSupportingFile(new File(System.getProperty("user.dir") + "/src/test/resources/testbags/testfiles/test2.txt"), "open");
 
             // we should now have 3
             assertEquals(3, bag.getPayload().size());
 
             // check we can get the files
             assertNotNull(bag.getBagFile("data/supporting/test1.txt"));
             assertNotNull(bag.getBagFile("data/supporting/test2.txt"));
             assertNull(bag.getBagFile("data/supporting/test3.txt"));
 
             // we still don't have a valid bag... create our manifest
             assertFalse(bag.verifyValid().isSuccess());
 
             // now generate our tagfiles
             testBagIt.generateTagFiles();
 
             // now generate our manifest
             testBagIt.generateManifests();
 
             // are we now valid?
             assertTrue(bag.verifyValid().isSuccess());
 
         }
         finally {
 
             bag.close();
         }
 
     }
     */
 
     /*
         create a bag and get primaries
      */
     @Test
     public void testBagGetPrimaries() throws Exception {
 
         BagIt testBagIt = new BagIt(System.getProperty("user.dir") + "/src/test/resources/testbags/testbag1");
         Bag bag = testBagIt.theBag;
 
         try {
 
             assertTrue(bag.verifyValid().isSuccess());
 
             // check the formats file exists
             assertTrue(bag.getBagFile("tagfiles/formats.txt").exists());
 
             // check the final sequence file exists
 
             // check how many entries there are
             assertEquals(4, testBagIt.formatMap.size());
 
             // check the primary entry
             assertEquals("image/jpeg", testBagIt.formatMap.get("data/final/segway.jpg"));
 
             // check the final sequence file exists
             assertTrue(bag.getBagFile("tagfiles/final.sequence.txt").exists());
 
             // look through tagfiles/final.sequence.txt
             BagFile bagFinalSequence = bag.getBagFile("tagfiles/final.sequence.txt");
 
             // superfluous check
             assertTrue(bagFinalSequence.exists());
 
             // get the primaries
            TreeMap<Integer, BaggedItem> primaries = testBagIt.getSequencedPrimaries();
             assertEquals("segway.jpg", primaries.get(1).getFilename());
             assertEquals("image/jpeg", primaries.get(1).getFormat());
             assertEquals((Integer)1, primaries.get(1).getSequence());
 
             // get the open secondaries
             TreeMap<Integer, BaggedItem> secondaries = testBagIt.getSequencedSecondaries("open");
             assertEquals("segway-detail.jpg", secondaries.get(1).getFilename());
             assertEquals("image/jpeg", secondaries.get(1).getFormat());
             assertEquals((Integer)1, secondaries.get(1).getSequence());
 
 
         } finally {
 
             bag.close();
         }
 
 
 
     }
 
     /*
        create a bag from a zip and check values
     */
     /*
     @Test
     public void testBagZip() throws Exception {
 
         BagIt testBagIt = new BagIt(System.getProperty("user.dir") + "/src/test/resources/testbags/testbag2.zip");
         Bag bag = testBagIt.theBag;
 
         try {
 
             // check the bag file
 
             assertEquals("application/zip", testBagIt.getMimetype());
             assertEquals("9aa9ab28654f1b995cb414ea387cb100", testBagIt.getMD5());
             assertEquals("zip", testBagIt.getPackaging());
 
         } finally {
 
             bag.close();
         }
 
     }*/
 }
