 /*
  * #%L
  * Bitrepository Integrity Service
  * %%
  * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.pillar.integration.func.getchecksums;
 
 import java.util.List;
 import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumType;
 import org.bitrepository.client.exceptions.NegativeResponseException;
 import org.bitrepository.common.utils.Base16Utils;
 import org.bitrepository.pillar.integration.func.PillarFunctionTest;
 import org.testng.Assert;
import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 public class GetChecksumTest extends PillarFunctionTest {
 
    @BeforeClass
    public void retrieveFirst2Files() {
        //ToDo
    }

     @Test ( groups = {"pillar-integration-test"} )
     public void testMD5Checksums() throws NegativeResponseException {
         addDescription("Test the pillar support for MD5 type checksums");
         addReference("Not implemented");
         pillarFileManager.ensureNumberOfFilesOnPillar(2, testMethodName);
 
         addStep("Request MD5 checksums for all files on the pillar",
             "A list (at least 2 long) of MD5 checksums should be returned.");
         ChecksumSpecTYPE checksumSpec = new ChecksumSpecTYPE();
         checksumSpec.setChecksumType(ChecksumType.MD5);
         List<ChecksumDataForChecksumSpecTYPE> checksums = pillarFileManager.getChecksums(checksumSpec, null);
         Assert.assertTrue(checksums.size() >= 2, "The length of the returned checksums were less that 2");
 
         addStep("Retrieve the first two files and verify that the checksums are correct",
             "Not implemented");
         // ToDo implement this
     }
     
     @Test ( groups = {"pillar-integration-test"} )
     public void testSHA1Checksums() throws NegativeResponseException {
         addDescription("Test the pillar support for SHA1 type checksums");
         addReference("Not implemented");
         pillarFileManager.ensureNumberOfFilesOnPillar(2, testMethodName);
 
         addStep("Request SHA1 checksums for all files on the pillar",
             "A list (at least 2 long) of SHA1 checksums should be returned.");
         ChecksumSpecTYPE checksumSpec = new ChecksumSpecTYPE();
         checksumSpec.setChecksumType(ChecksumType.SHA1);
         List<ChecksumDataForChecksumSpecTYPE> checksums = pillarFileManager.getChecksums(checksumSpec, null);
         Assert.assertTrue(checksums.size() >= 2, "The length of the returned checksums were less that 2");
 
         addStep("Retrieve the first two files and verify that the checksums are correct",
             "Not implemented");
         // ToDo implement this
     }
 
     @Test ( groups = {"pillar-integration-test"} )
     public void testMD5SaltChecksums() throws NegativeResponseException {
         addDescription("Test the pillar support for MD5 type checksums with a salt");
         addReference("Not implemented");
         pillarFileManager.ensureNumberOfFilesOnPillar(2, testMethodName);
 
         addStep("Request salted MD5 checksums for all files on the pillar",
             "A list (at least 2 long) of MD5 checksums should be returned.");
         ChecksumSpecTYPE checksumSpec = new ChecksumSpecTYPE();
        checksumSpec.setChecksumType(ChecksumType.HMAC_MD5);
         checksumSpec.setChecksumSalt(Base16Utils.encodeBase16("abab"));
         List<ChecksumDataForChecksumSpecTYPE> checksums = pillarFileManager.getChecksums(checksumSpec, null);
         Assert.assertTrue(checksums.size() >= 2, "The length of the returned checksums were less that 2");
 
         addStep("Retrieve the first two files and verify that the checksums are correct",
             "Not implemented");
         // ToDo implement this
     }
     
     @Test ( groups = {"pillar-integration-test"} )
     public void testSHA1SaltChecksums() throws NegativeResponseException {
         addDescription("Test the pillar support for SHA1 type checksums with a salt");
         addReference("Not implemented");
         pillarFileManager.ensureNumberOfFilesOnPillar(2, testMethodName);
 
         addStep("Request salted SHA1 checksums for all files on the pillar",
             "A list (at least 2 long) of SHA1 checksums should be returned.");
         ChecksumSpecTYPE checksumSpec = new ChecksumSpecTYPE();
        checksumSpec.setChecksumType(ChecksumType.HMAC_SHA1);
         checksumSpec.setChecksumSalt(Base16Utils.encodeBase16("abab"));
         List<ChecksumDataForChecksumSpecTYPE> checksums = pillarFileManager.getChecksums(checksumSpec, null);
         Assert.assertTrue(checksums.size() >= 2, "The length of the returned checksums were less that 2");
 
         addStep("Retrieve the first two files and verify that the checksums are correct",
             "Not implemented");
         // ToDo implement this
     }
     
 }
