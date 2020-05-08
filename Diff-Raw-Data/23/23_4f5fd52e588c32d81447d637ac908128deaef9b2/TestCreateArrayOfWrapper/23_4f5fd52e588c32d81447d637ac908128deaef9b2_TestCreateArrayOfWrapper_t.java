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
 
 import java.sql.Array;
 import java.sql.Connection;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.skife.jdbi.v2.DBI;
 import org.skife.jdbi.v2.IDBI;
 
 import com.nesscomputing.testing.lessio.AllowDNSResolution;
 import com.nesscomputing.testing.lessio.AllowNetworkAccess;
 
/**
 * This test requires a local postgres database and a "postgres" user that can connect to the database without a password.
 */
 @AllowDNSResolution
@AllowNetworkAccess(endpoints={"127.0.0.1:5432"})
 public class TestCreateArrayOfWrapper
 {
     private IDBI testDbi;
 
     @Before
     public void setUp()
     {
        testDbi = new DBI("jdbc:postgresql://localhost/postgres", "postgres", "");
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
