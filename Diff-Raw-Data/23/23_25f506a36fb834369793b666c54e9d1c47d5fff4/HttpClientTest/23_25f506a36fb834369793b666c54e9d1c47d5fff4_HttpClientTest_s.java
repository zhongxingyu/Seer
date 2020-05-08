 package pl.nlogn.sandbox.riak;
 
 
 import com.basho.riak.client.IRiakClient;
 import com.basho.riak.client.IRiakObject;
 import com.basho.riak.client.RiakFactory;
 import com.basho.riak.client.bucket.Bucket;
import com.basho.riak.client.cap.UnresolvedConflictException;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /*
 * Copyright 2012 Nlogn Paweł Sidoryk
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * User: pawel
 * Date: 11/28/12
 * Time: 9:41 PM
 */
 public class HttpClientTest {
     public static final String RIAK_URL = "http://192.168.0.4:8098/riak";
 
     public static final String REC_KEY1 = "conc1";
 
     private IRiakClient httpClient;
 
     private Bucket myBucket;
 
     @Before
     public void setUp() throws Exception {
         httpClient = RiakFactory.httpClient(RIAK_URL);
         myBucket = httpClient.fetchBucket("test1").execute();
         myBucket.store(REC_KEY1, "foo").execute();
     }
 
     @After
     public void tearDown() throws Exception {
        myBucket.delete(REC_KEY1).execute();
         httpClient.shutdown();
     }
 
     @Test
     public void testFetch() throws Exception {
         IRiakObject myObject = myBucket.fetch(REC_KEY1).execute();
         System.out.println(myObject.getValueAsString());
         System.out.println("VClock: " + myObject.getVClockAsString());
 
         myObject.setValue(myObject.getValueAsString() + 1);
         myObject = myBucket.store(myObject).returnBody(true).execute();
         System.out.println(myObject.getValueAsString());
         System.out.println("VClock: " + myObject.getVClockAsString());
 
        myBucket.delete(REC_KEY1).execute();
         myBucket.store(REC_KEY1, "foo").execute();
         myObject = myBucket.fetch(REC_KEY1).execute();
     }
 }
