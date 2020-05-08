 /**
  * Copyright (C) 2012 Ness Computing, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.nesscomputing.jdbc.wrappers;
 
 import java.net.URI;
 import java.sql.Array;
 import java.sql.Connection;
 
 import com.google.inject.Guice;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.google.inject.Stage;
 import com.google.inject.name.Named;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.skife.jdbi.v2.IDBI;
 
 import com.nesscomputing.config.Config;
 import com.nesscomputing.config.ConfigModule;
 import com.nesscomputing.db.postgres.junit.EmbeddedPostgresRules;
 import com.nesscomputing.db.postgres.junit.EmbeddedPostgresTestDatabaseRule;
 import com.nesscomputing.jdbc.DatabaseModule;
 import com.nesscomputing.lifecycle.junit.LifecycleRule;
 import com.nesscomputing.lifecycle.junit.LifecycleRunner;
 import com.nesscomputing.lifecycle.junit.LifecycleStatement;
 import com.nesscomputing.testing.lessio.AllowDNSResolution;
 
 @AllowDNSResolution
 @RunWith(LifecycleRunner.class)
 public class TestCreateArrayOfWrapper
 {
     @LifecycleRule
     public final LifecycleStatement lifecycleRule = LifecycleStatement.serviceDiscoveryLifecycle();
 
     @Rule
     public EmbeddedPostgresTestDatabaseRule postgresRule = EmbeddedPostgresRules.embeddedDatabaseRule(URI.create("classpath:/sql"));
 
     @Inject
     @Named("test")
     private IDBI testDbi;
 
     @Before
     public void setUp()
     {
         final Config config = postgresRule.getTweakedConfig("test");
         final Injector inj = Guice.createInjector(Stage.PRODUCTION,
                                                   new ConfigModule(config),
                                                   lifecycleRule.getLifecycleModule(),
                                                   new DatabaseModule("test"));
 
         inj.injectMembers(this);
 
         Assert.assertNotNull(testDbi);
     }
 
     @Test
     public void testSimple() throws Exception
     {
         Connection c = null;
         try {
             c = testDbi.open().getConnection();
             final Array s = c.createArrayOf("varchar", new String [] { "a", "b" });
             Assert.assertNotNull(s);
         }
         finally {
             if (c != null) {
                 c.close();
             }
         }
     }
 }
