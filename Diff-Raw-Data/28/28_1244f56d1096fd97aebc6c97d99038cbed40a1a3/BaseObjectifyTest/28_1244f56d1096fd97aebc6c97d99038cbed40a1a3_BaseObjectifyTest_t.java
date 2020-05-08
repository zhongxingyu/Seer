 /**
  * Copyright (c) 2012 Sappenin Inc.
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
  */
 package com.sappenin.objectify;
 
 import org.junit.After;
 import org.junit.Before;
 
 import com.google.appengine.api.memcache.MemcacheService;
 import com.google.appengine.api.memcache.MemcacheServiceFactory;
 import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
 import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.googlecode.objectify.ObjectifyFilter;
 
 /**
  * All tests should extend this class to set up the GAE environment.
  * 
  * @see <a
  *      href="http://code.google.com/appengine/docs/java/howto/unittesting.html">Unit
  *      Testing in Appengine</a>
  * 
  * @author David Fuelling <sappenin@gmail.com>
  */
 public class BaseObjectifyTest
 {

 	protected LocalServiceTestHelper helper = new LocalServiceTestHelper(
 		// Our tests assume strong consistency, but a bug in the appengine test
 		// harness forces us to set this value to at least 1.
 		new LocalDatastoreServiceTestConfig().setDefaultHighRepJobPolicyUnappliedJobPercentage(1),
 		new LocalMemcacheServiceTestConfig(), new LocalTaskQueueTestConfig());
 
 	protected MemcacheService memcache;
 
 	@Before
 	public void setUp() throws Exception
 	{
 		this.helper.setUp();
 
 		memcache = MemcacheServiceFactory.getMemcacheService();
 	}
 
 	@After
 	public void tearDown()
 	{
		// This is normally done in ObjectifyFilter but that doesn't exist for
		// tests
		ObjectifyFilter.complete();

 		this.helper.tearDown();
 	}
 
 }
