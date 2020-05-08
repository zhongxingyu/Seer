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
 package com.lolay.citygrid.content.places.detail;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.lolay.citygrid.ClientFactory;
 import com.lolay.citygrid.InvokerException;
 import com.lolay.citygrid.content.places.detail.DetailAddress;
 import com.lolay.citygrid.content.places.detail.DetailClient;
 import com.lolay.citygrid.content.places.detail.DetailInvoker;
 import com.lolay.citygrid.content.places.detail.DetailLocation;
 import com.lolay.citygrid.content.places.detail.DetailResults;
 import com.lolay.citygrid.content.places.search.SearchIntegration;
 
 import junit.framework.TestCase;
 
 public class DetailIntegration extends TestCase {
 	private static final Log testDetailLog = LogFactory.getLog(SearchIntegration.class.getName() + ".testDetail");
 	private static final String baseUrl = "http://api.citygridmedia.com";
 	
 	public void testDetail() throws Exception {
 		Log log = testDetailLog;
 		log.trace("ENTER");
 		
 		DetailClient client = new ClientFactory(baseUrl).getDetail();
		DetailInvoker invoker = DetailInvoker.builder().listingId(10100230).clientIp("127.0.0.1")
			.publisher("test").placement("search_page").build();
 		
 		DetailResults results = null;
 		try {
 			long start = System.currentTimeMillis();
 			results = invoker.profile(client);
 			long end = System.currentTimeMillis();
 			log.trace(String.format("Get detail took %s ms", end - start));
 		} catch (InvokerException e) {
 			log.error(e.getErrorCodes(), e);
 			fail();
 		}
 		assertNotNull(results);
 		DetailLocation location = results.getLocation();
 		assertNotNull(location);
 		DetailAddress address = location.getAddress();
 		assertNotNull(address);
 		assertNotNull(address.getPostalCode());
 		// TODO add validation
 	}
 }
