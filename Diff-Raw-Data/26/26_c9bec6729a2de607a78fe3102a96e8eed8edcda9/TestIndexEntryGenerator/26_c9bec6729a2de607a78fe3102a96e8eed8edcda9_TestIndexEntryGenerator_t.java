 /*
  * Copyright 2006 Amazon Technologies, Inc. or its affiliates.
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
 
 import java.util.Map;
 
 import junit.framework.TestSuite;
import com.amazon.carbonado.synthetic.SyntheticStorableReferenceAccess;
 import com.amazon.carbonado.synthetic.TestSyntheticStorableBuilders;
 import com.amazon.carbonado.info.StorableIndex;
 import com.amazon.carbonado.info.StorableInfo;
 import com.amazon.carbonado.info.StorableIntrospector;
 import com.amazon.carbonado.info.StorableProperty;
 import com.amazon.carbonado.info.Direction;
 import com.amazon.carbonado.Storable;
 
 import com.amazon.carbonado.stored.StorableTestBasic;
 
 /**
  *
  *
  * @author Brian S O'Neill
  */
 public class TestIndexEntryGenerator extends TestSyntheticStorableBuilders {
     public static void main(String[] args) {
         junit.textui.TestRunner.run(suite());
     }
 
     public static TestSuite suite() {
         return new TestSuite(TestIndexEntryGenerator.class);
     }
 
     public TestIndexEntryGenerator(String name) {
         super(name);
     }
 
     /**
      * test index generator
      */
     public void test_IndexGenerator() throws Exception {
         for (TestSyntheticStorableBuilders.TestDef test : TestSyntheticStorableBuilders.TESTS) {
             StorableIndex<StorableTestBasic> indexDesc = newStorableIndex(test);
            SyntheticStorableReferenceAccess<StorableTestBasic> access =
                IndexEntryGenerator.getIndexAccess(indexDesc);
 
            Class s = access.getReferenceClass();
 
             validateIndexEntry(test, s);
             exerciseStorable(s);
 
             StorableTestBasic master =
                 mRepository.storageFor(StorableTestBasic.class).prepare();
             populate(master);
             master.insert();
 
             Storable index = mRepository.storageFor(s).prepare();
            access.copyFromMaster(index, master);
             index.insert();
 
             Storable indexChecker = mRepository.storageFor(s).prepare();
            access.copyFromMaster(indexChecker, master);
             assertTrue(indexChecker.tryLoad());
 
             StorableTestBasic masterChecker =
                 mRepository.storageFor(StorableTestBasic.class).prepare();
            access.copyToMasterPrimaryKey(indexChecker, masterChecker);
             masterChecker.load();
             assertEquals(master, masterChecker);
 
            assertTrue(access.isConsistent(index, master));
             masterChecker =
                 mRepository.storageFor(StorableTestBasic.class).prepare();
             master.copyAllProperties(masterChecker);
            assertTrue(access.isConsistent(index, masterChecker));
             masterChecker.setId(-42);
            assertFalse(access.isConsistent(index, masterChecker));
 
         }
     }
 
     /**
      * @param test
      * @return StorableInfo for this test
      */
     private StorableIndex<StorableTestBasic> newStorableIndex(TestSyntheticStorableBuilders.TestDef test) {
         StorableInfo info = StorableIntrospector.examine(test.mClass);
         final Map<String, ? extends StorableProperty> allProps = info.getAllProperties();
 
         TestSyntheticStorableBuilders.IndexDef[] indexDefinitions = test.mProps;
         StorableProperty[] props = new StorableProperty[indexDefinitions.length];
         Direction[] dirs = new Direction[indexDefinitions.length];
         int i = 0;
         for (TestSyntheticStorableBuilders.IndexDef p : indexDefinitions) {
             props[i] = allProps.get(p.getProp());
             dirs[i] = p.getDir();
             i++;
         }
         StorableIndex<StorableTestBasic> index = new StorableIndex<StorableTestBasic>(props, dirs);
         return index;
     }
 
 
 }
