 /*
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  **     http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  */
 
 package com.google.code.geobeagle.bcaching.communication;
 
 import static org.easymock.EasyMock.expect;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.powermock.api.easymock.PowerMock.createMock;
 import static org.powermock.api.easymock.PowerMock.replayAll;
 import static org.powermock.api.easymock.PowerMock.verifyAll;
 
 import com.google.code.geobeagle.bcaching.communication.BCachingException;
 import com.google.code.geobeagle.bcaching.communication.BCachingList;
 import com.google.code.geobeagle.bcaching.communication.BCachingListImportHelper;
 import com.google.code.geobeagle.bcaching.communication.BCachingListImporter;
 
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import java.util.Hashtable;
 
 @RunWith(PowerMockRunner.class)
 public class BCachingListImporterTest {
 
     @Test
     public void testGetCacheList() throws BCachingException {
         BCachingListImportHelper bCachingListImportHelper = createMock(BCachingListImportHelper.class);
         BCachingList bcachingList = createMock(BCachingList.class);
 
         Hashtable<String, String> params = new Hashtable<String, String>();
         expect(bCachingListImportHelper.importList(params)).andReturn(bcachingList);
         replayAll();
         assertEquals(bcachingList, new BCachingListImporter(params, bCachingListImportHelper)
                .getCacheList(12, 7777));
         verifyAll();
 
         assertEquals("12", params.get("first"));
         assertEquals(BCachingListImporter.MAX_COUNT, params.get("maxcount"));
         assertEquals("7777", params.get("since"));
     }
 
     @Test
     public void testGetTotalCount() throws BCachingException {
         BCachingListImportHelper bCachingListImportHelper = createMock(BCachingListImportHelper.class);
         BCachingList bcachingList = createMock(BCachingList.class);
 
         Hashtable<String, String> params = new Hashtable<String, String>();
         params.put("first", "112");
         expect(bCachingListImportHelper.importList(params)).andReturn(bcachingList);
         expect(bcachingList.getTotalCount()).andReturn(42);
         replayAll();
         assertEquals(42, new BCachingListImporter(params, bCachingListImportHelper)
                 .getTotalCount("7777"));
         verifyAll();
 
         assertFalse( params.containsKey("first"));
         assertEquals("0", params.get("maxcount"));
         assertEquals("7777", params.get("since"));
     }
 }
