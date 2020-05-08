 /*
  * Copyright 2007 Amazon Technologies, Inc. or its affiliates.
  * Amazon, Amazon.com and Carbonado are trademarks or registered trademarks
  * of Amazon Technologies, Inc. or its affiliates.  All rights reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.amazon.carbonado.repo.indexed;
 
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import com.amazon.carbonado.Query;
 import com.amazon.carbonado.Repository;
 import com.amazon.carbonado.Storage;
 
 import com.amazon.carbonado.TestUtilities;
 import com.amazon.carbonado.stored.OverIndexedUserAddress;
 
 /**
  * 
  *
  * @author Brian S O'Neill
  */
 public class TestCoveringIndex extends TestCase {
     public static void main(String[] args) {
         junit.textui.TestRunner.run(suite());
     }
 
     public static TestSuite suite() {
         return new TestSuite(TestCoveringIndex.class);
     }
 
     private Repository mRepository;
 
     public TestCoveringIndex(String name) {
         super(name);
     }
 
     protected void setUp() throws Exception {
         super.setUp();
         mRepository = TestUtilities.buildTempRepository("indexed");
     }
 
     protected void tearDown() throws Exception {
         super.tearDown();
         mRepository.close();
         mRepository = null;
     }
 
     public void testFullCoverage() throws Exception {
         assertTrue(mRepository instanceof IndexedRepository);
 
         Storage<OverIndexedUserAddress> storage =
             mRepository.storageFor(OverIndexedUserAddress.class);
 
         Query<OverIndexedUserAddress> query =
             storage.query("country > ? & city != ? & state = ? & postalCode = ?");
 
         StringBuffer buf = new StringBuffer();
         query.printPlan(buf);
         String plan = buf.toString();
 
         String expected =
             "filter: postalCode = ?\n" +
             "  index scan: com.amazon.carbonado.stored.OverIndexedUserAddress\n" +
             "  ...index: {properties=[+state, +city, +country, +line2, +line1, ~addressID], unique=true}\n" + 
             "  ...identity filter: state = ?\n" +
            "  ...covering filter: country > ? & city != ?\n";
 
         //System.out.println(plan);
         assertEquals(expected, plan);
 
         query = query.withValues("D", "Springfield", "Unknown", "12345");
 
         buf = new StringBuffer();
         query.printPlan(buf);
         plan = buf.toString();
 
         expected =
             "filter: postalCode = 12345\n" +
             "  index scan: com.amazon.carbonado.stored.OverIndexedUserAddress\n" +
             "  ...index: {properties=[+state, +city, +country, +line2, +line1, ~addressID], unique=true}\n" + 
             "  ...identity filter: state = Unknown\n" +
            "  ...covering filter: country > D & city != Springfield\n";
 
         //System.out.println(plan);
         assertEquals(expected, plan);
 
         assertEquals(0, query.count());
         assertEquals(0, query.fetch().toList().size());
 
         // Insert some test data.
         OverIndexedUserAddress address;
 
         address = storage.prepare();
         address.setAddressID(1);
         address.setLine1("abc");
         address.setLine2("123");
         address.setCity("Springfield");
         address.setState("Illinois");
         address.setCountry("USA");
         address.setPostalCode("12345");
         address.insert();
 
         address = storage.prepare();
         address.setAddressID(2);
         address.setLine1("abc");
         address.setLine2("123");
         address.setCity("Springfield");
         address.setState("Unknown");
         address.setCountry("USA");
         address.setPostalCode("12345");
         address.insert();
 
         address = storage.prepare();
         address.setAddressID(3);
         address.setLine1("abc");
         address.setLine2("123");
         address.setCity("San Dimas");
         address.setState("Unknown");
         address.setCountry("USA");
         address.setPostalCode("12345");
         address.insert();
 
         address = storage.prepare();
         address.setAddressID(4);
         address.setLine1("abc");
         address.setLine2("123");
         address.setCity("San Dimas");
         address.setState("Unknown");
         address.setCountry("USA");
         address.setPostalCode("54321");
         address.insert();
 
         address = storage.prepare();
         address.setAddressID(5);
         address.setLine1("abc");
         address.setLine2("123");
         address.setCity("San Dimas");
         address.setState("California");
         address.setCountry("USA");
         address.setPostalCode("12345");
         address.insert();
 
         address = storage.prepare();
         address.setAddressID(6);
         address.setLine1("abc");
         address.setLine2("123");
         address.setCity("Greenwood");
         address.setState("Unknown");
         address.setCountry("Canada");
         address.setPostalCode("12345");
         address.insert();
 
         assertEquals(1, query.count());
 
         address = query.loadOne();
         assertEquals(3, address.getAddressID());
     }
 }
