 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 
 package org.geogit.api;
 
 import org.geogit.storage.GraphDatabase;
 import org.geogit.storage.ObjectDatabase;
 import org.geogit.storage.RefDatabase;
 import org.geogit.storage.StagingDatabase;
 import org.geogit.storage.memory.HeapObjectDatabse;
 import org.geogit.storage.memory.HeapRefDatabase;
 import org.geogit.storage.memory.HeapStagingDatabase;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Scopes;
 
 /**
  *
  */
 public class MemoryModule extends AbstractModule {
 
     private Platform testPlatform;
 
     /**
      * @param testPlatform
      */
     public MemoryModule(Platform testPlatform) {
         this.testPlatform = testPlatform;
     }
 
     @Override
     protected void configure() {
         if (testPlatform != null) {
             bind(Platform.class).toInstance(testPlatform);
         }
         bind(ObjectDatabase.class).to(HeapObjectDatabse.class).in(Scopes.SINGLETON);
         bind(StagingDatabase.class).to(HeapStagingDatabase.class).in(Scopes.SINGLETON);
         bind(RefDatabase.class).to(HeapRefDatabase.class).in(Scopes.SINGLETON);
         bind(GraphDatabase.class).to(TestNeo4JGraphDatabase.class).in(Scopes.SINGLETON);
     }
 
 }
