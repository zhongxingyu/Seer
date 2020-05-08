 /*
  * Licensed to Lolay, Inc. under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  Lolay, Inc. licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  * http://github.com/lolay/citygrid/raw/master/LICENSE
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.    
  */
 package com.lolay.citygrid.profile;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.lolay.citygrid.ClientFactory;
 import com.lolay.citygrid.InvokerException;
 import com.lolay.citygrid.search.SearchIntegration;
 
 import junit.framework.TestCase;
 
 public class ProfileIntegration extends TestCase {
 	private static final Log testProfileLog = LogFactory.getLog(SearchIntegration.class.getName() + ".testProfile");
 	private static final String baseUrl = "http://api.citygridmedia.com";
 	
 	public void testProfile() throws Exception {
 		Log log = testProfileLog;
 		log.trace("ENTER");
 		
 		ProfileClient client = new ClientFactory(baseUrl).getProfile();
 		ProfileInvoker invoker = ProfileInvoker.builder().listingId(1).clientIp("127.0.0.1")
 			.publisher("acme").placement("junit").noLog(1).build();
 		
 		ProfileResults results = null;
 		try {
 			long start = System.currentTimeMillis();
 			results = invoker.profile(client);
 			long end = System.currentTimeMillis();
 			log.trace(String.format("Get profile took %s ms", end - start));
 		} catch (InvokerException e) {
 			log.error(e.getErrorCodes(), e);
 			fail();
 		}
 		assertNotNull(results);
 		ProfileLocation location = results.getLocation();
 		assertNotNull(location);
 		// TODO add validation
 	}
 }
