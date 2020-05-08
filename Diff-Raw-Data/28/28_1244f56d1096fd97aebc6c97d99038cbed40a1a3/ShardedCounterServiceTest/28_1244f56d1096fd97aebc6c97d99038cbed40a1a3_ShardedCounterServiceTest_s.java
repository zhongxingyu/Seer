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
 package com.sappenin.objectify.shardedcounter.service;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Logger;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.appengine.api.memcache.MemcacheServiceFactory;
 import com.google.appengine.api.taskqueue.QueueFactory;
 import com.google.appengine.api.taskqueue.dev.LocalTaskQueue;
 import com.google.appengine.api.taskqueue.dev.QueueStateInfo;
 import com.google.appengine.api.urlfetch.URLFetchServicePb.URLFetchRequest;
 import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
 import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
 import com.google.common.base.Optional;
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.ObjectifyService;
 import com.sappenin.objectify.BaseObjectifyTest;
 import com.sappenin.objectify.shardedcounter.data.Counter;
 import com.sappenin.objectify.shardedcounter.data.Counter.CounterStatus;
 import com.sappenin.objectify.shardedcounter.data.CounterShard;
 import com.sappenin.objectify.translate.UTCReadableInstantTranslatorFactory;
 
 /**
  * Test class for {@link ShardedCounterService}.
  * 
  * @author David Fuelling <sappenin@gmail.com>
  */
 public class ShardedCounterServiceTest extends BaseObjectifyTest
 {
 	private static final Logger logger = Logger.getLogger(ShardedCounterServiceTest.class.getName());
 
 	private static final String DELETE_COUNTER_SHARD_QUEUE_NAME = "deleteCounterShardQueue";
 	private static final String TEST_COUNTER1 = "test-counter1";
 	private static final String TEST_COUNTER2 = "test-counter2";
 
 	CounterService shardedCounterService;
 
 	LocalTaskQueueTestConfig.TaskCountDownLatch countdownLatch;
 
 	public static class DeleteShardedCounterDeferredCallback extends LocalTaskQueueTestConfig.DeferredTaskCallback
 	{
 		private static final long serialVersionUID = -2113612286521272160L;
 
 		@Override
 		protected int executeNonDeferredRequest(URLFetchRequest req)
 		{
 			// Do Nothing in this callback. This callback is only here to
 			// simulate a task-queue
 			// run.
 
 			// See here:
 			// http://stackoverflow.com/questions/6632809/gae-unit-testing-taskqueue-with-testbed
 			// The dev app server is single-threaded, so it can't run tests in
 			// the background properly. Thus, we test that the task was added to
 			// the queue properly. Then, we manually run the shard-deletion code
 			// and assert that it's working properly.
 			return 200;
 		}
 	}
 
 	@Before
 	public void setUp() throws Exception
 	{
 		// Don't call super.setUp because we initialize slightly differently
 		// here...
 
 		countdownLatch = new LocalTaskQueueTestConfig.TaskCountDownLatch(1);
 
 		// See
 		// http://www.ensor.cc/2010/11/unit-testing-named-queues-spring.html
 		// NOTE: THE QUEUE XML PATH RELATIVE TO WEB APP ROOT, More info
 		// below
 		// http://stackoverflow.com/questions/11197058/testing-non-default-app-engine-task-queues
 		final LocalTaskQueueTestConfig localTaskQueueConfig = new LocalTaskQueueTestConfig()
 			.setDisableAutoTaskExecution(false).setQueueXmlPath("src/test/resources/queue.xml")
 			.setTaskExecutionLatch(countdownLatch).setCallbackClass(DeleteShardedCounterDeferredCallback.class);
 
 		// Use a different queue.xml for testing purposes
 		helper = new LocalServiceTestHelper(
 			new LocalDatastoreServiceTestConfig().setDefaultHighRepJobPolicyUnappliedJobPercentage(0.01f),
 			new LocalMemcacheServiceTestConfig(), localTaskQueueConfig);
 		helper.setUp();
 
 		memcache = MemcacheServiceFactory.getMemcacheService();
 
 		ObjectifyService.ofy().clear();
 		// Must be added before registering entities...
 		ObjectifyService.factory().getTranslators().add(new UTCReadableInstantTranslatorFactory());
 
 		ObjectifyService.factory().register(Counter.class);
 		ObjectifyService.factory().register(CounterShard.class);
 
 		shardedCounterService = new ShardedCounterService(MemcacheServiceFactory.getMemcacheService());
 	}
 
 	@After
 	public void tearDown()
 	{
		ObjectifyService.ofy().clear();
 		super.tearDown();
 	}
 
 	// /////////////////////////
 	// Unit Tests
 	// /////////////////////////
 
 	@Test(expected = RuntimeException.class)
 	public void testShardedCounterServiceConstructor_NullMemcache()
 	{
 		shardedCounterService = new ShardedCounterService(null);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testShardedCounterServiceConstructor_ValidMemcache_0Shards()
 	{
 		ShardedCounterServiceConfiguration.Builder builder = new ShardedCounterServiceConfiguration.Builder();
 		builder.withNumInitialShards(0);
 	}
 
 	@Test(expected = RuntimeException.class)
 	public void testShardedCounterServiceConstructor_ValidMemcache_NegativeShards()
 	{
 		ShardedCounterServiceConfiguration.Builder builder = new ShardedCounterServiceConfiguration.Builder();
 		builder.withNumInitialShards(-10);
 	}
 
 	@Test
 	public void testShardedCounterServiceConstructor_DefaultShards()
 	{
 		shardedCounterService = new ShardedCounterService(memcache);
 		Optional<Counter> optCounter = shardedCounterService.getCounter(TEST_COUNTER1);
 		assertFalse(optCounter.isPresent());
 	}
 
 	@Test
 	public void testShardedCounterServiceConstructor_NoRelativeUrlPath()
 	{
 		ShardedCounterServiceConfiguration.Builder builder = new ShardedCounterServiceConfiguration.Builder();
 		builder.withDeleteCounterShardQueueName(DELETE_COUNTER_SHARD_QUEUE_NAME);
 		ShardedCounterServiceConfiguration config = new ShardedCounterServiceConfiguration(builder);
 		shardedCounterService = new ShardedCounterService(memcache, config);
 
 		Optional<Counter> optCounter = shardedCounterService.getCounter(TEST_COUNTER1);
 		assertFalse(optCounter.isPresent());
 	}
 
 	@Test
 	public void testShardedCounterServiceConstructorFull()
 	{
 		ShardedCounterServiceConfiguration.Builder builder = new ShardedCounterServiceConfiguration.Builder();
 
 		builder.withDeleteCounterShardQueueName(DELETE_COUNTER_SHARD_QUEUE_NAME).withNumInitialShards(10)
 			.withRelativeUrlPathForDeleteTaskQueue("RELATIVE-URL-PATH-FOR-DELETE-QUEUE");
 		ShardedCounterServiceConfiguration config = new ShardedCounterServiceConfiguration(builder);
 		shardedCounterService = new ShardedCounterService(memcache, config);
 
 		Optional<Counter> optCounter = shardedCounterService.getCounter(TEST_COUNTER1);
 		assertFalse(optCounter.isPresent());
 	}
 
 	// ///////////////////
 	// ///////////////////
 	// ///////////////////
 
 	@Test(expected = RuntimeException.class)
 	public void testCreateCounter_Null()
 	{
 		shardedCounterService.create(null);
 	}
 
 	@Test(expected = RuntimeException.class)
 	public void testCreateCounter_Empty()
 	{
 		shardedCounterService.create("");
 	}
 
 	@Test(expected = RuntimeException.class)
 	public void testCreateCounter_Blank()
 	{
 		shardedCounterService.create(" ");
 	}
 
 	@Test
 	public void testCreateCounter_ValidName()
 	{
 		Counter counter1 = shardedCounterService.create(TEST_COUNTER1);
 		assertNotNull(counter1);
 		assertNotNull(counter1.getTypedKey());
 
 		Counter counter2 = shardedCounterService.create(TEST_COUNTER2);
 		assertNotNull(counter2);
 		assertNotNull(counter2.getTypedKey());
 	}
 
 	public void testCreateCounter_AlreadyExists()
 	{
 		Counter counter1 = shardedCounterService.create(TEST_COUNTER1);
 		assertNotNull(counter1);
 		assertNotNull(counter1.getTypedKey());
 
 		// Second create should be fine unless the counter is being deleted.
 		counter1 = shardedCounterService.create(TEST_COUNTER1);
 		assertNotNull(counter1);
 		assertNotNull(counter1.getTypedKey());
 	}
 
 	@Test(expected = RuntimeException.class)
 	public void testCreateCounter_AlreadyExists_Deleting()
 	{
 		Counter counter1 = shardedCounterService.create(TEST_COUNTER1);
 		assertNotNull(counter1);
 		assertNotNull(counter1.getTypedKey());
 		counter1.setCounterStatus(CounterStatus.DELETING);
 		ObjectifyService.ofy().save().entity(counter1).now();
 
 		counter1 = shardedCounterService.create(TEST_COUNTER1);
 	}
 
 	// ///////////////////
 	// ///////////////////
 	// ///////////////////
 
 	@Test(expected = RuntimeException.class)
 	public void testIncrement_CounterIsBeingDeleted() throws InterruptedException
 	{
 		Counter counter = shardedCounterService.create(TEST_COUNTER1);
 		counter.setCounterStatus(CounterStatus.DELETING);
 		// Store this in the Datastore to trigger the exception below...
 		ObjectifyService.ofy().save().entity(counter).now();
 
 		shardedCounterService.increment(TEST_COUNTER1, 1);
 	}
 
 	@Test
 	public void testIncrement_DefaultNumShards() throws InterruptedException
 	{
 		shardedCounterService = new ShardedCounterService(memcache);
 		doCounterIncrementAssertions(TEST_COUNTER1, 50);
 	}
 
 	@Test
 	public void testIncrement_Specifiy1Shard() throws InterruptedException
 	{
 		shardedCounterService = initialShardedCounterService(1);
 		doCounterIncrementAssertions(TEST_COUNTER1, 50);
 	}
 
 	@Test
 	public void testIncrement_Specifiy3Shard() throws InterruptedException
 	{
 		shardedCounterService = initialShardedCounterService(1);
 		doCounterIncrementAssertions(TEST_COUNTER1, 50);
 	}
 
 	@Test
 	public void testIncrement_Specifiy10Shards() throws InterruptedException
 	{
 		shardedCounterService = initialShardedCounterService(10);
 		doCounterIncrementAssertions(TEST_COUNTER1, 50);
 	}
 
 	// ///////////////////
 	// ///////////////////
 	// ///////////////////
 
 	@Test(expected = RuntimeException.class)
 	public void testDecrement_CounterIsBeingDeleted() throws InterruptedException
 	{
 		Counter counter = shardedCounterService.create(TEST_COUNTER1);
 		counter.setCounterStatus(CounterStatus.DELETING);
 		// Store this in the Datastore to trigger the exception below...
 		ObjectifyService.ofy().save().entity(counter).now();
 
 		shardedCounterService.decrement(TEST_COUNTER1);
 	}
 
 	@Test
 	public void testDecrement_DefaultNumShards() throws InterruptedException
 	{
 		shardedCounterService = new ShardedCounterService(memcache);
 		doCounterDecrementAssertions(TEST_COUNTER1, 50);
 	}
 
 	@Test
 	public void testDecrement_Specifiy1Shard() throws InterruptedException
 	{
 		shardedCounterService = initialShardedCounterService(1);
 		doCounterDecrementAssertions(TEST_COUNTER1, 50);
 	}
 
 	@Test
 	public void testDecrement_Specifiy3Shard() throws InterruptedException
 	{
 		shardedCounterService = initialShardedCounterService(3);
 		doCounterDecrementAssertions(TEST_COUNTER1, 50);
 	}
 
 	@Test
 	public void testDecrement_Specifiy10Shards() throws InterruptedException
 	{
 		shardedCounterService = initialShardedCounterService(10);
 		doCounterDecrementAssertions(TEST_COUNTER1, 50);
 	}
 
 	// ///////////////////
 	// ///////////////////
 	// ///////////////////
 
 	@Test
 	public void testIncrementDecrementInterleaving()
 	{
 		shardedCounterService.create(TEST_COUNTER1);
 		shardedCounterService.create(TEST_COUNTER2);
 
 		shardedCounterService.increment(TEST_COUNTER1, 1);
 		shardedCounterService.increment(TEST_COUNTER2, 1);
 		shardedCounterService.increment(TEST_COUNTER1, 1);
 		shardedCounterService.increment(TEST_COUNTER2, 1);
 		shardedCounterService.increment(TEST_COUNTER2, 1);
 		shardedCounterService.increment(TEST_COUNTER1, 1);
 		shardedCounterService.increment(TEST_COUNTER2, 1);
 
 		assertEquals(3, shardedCounterService.getCounter(TEST_COUNTER1).get().getApproximateCount());
 		assertEquals(4, shardedCounterService.getCounter(TEST_COUNTER2).get().getApproximateCount());
 
 		shardedCounterService.increment(TEST_COUNTER1, 1);
 		shardedCounterService.increment(TEST_COUNTER2, 1);
 		shardedCounterService.increment(TEST_COUNTER1, 1);
 		shardedCounterService.increment(TEST_COUNTER2, 1);
 		shardedCounterService.increment(TEST_COUNTER2, 1);
 		shardedCounterService.increment(TEST_COUNTER1, 1);
 		shardedCounterService.increment(TEST_COUNTER2, 1);
 
 		assertEquals(6, shardedCounterService.getCounter(TEST_COUNTER1).get().getApproximateCount());
 		assertEquals(8, shardedCounterService.getCounter(TEST_COUNTER2).get().getApproximateCount());
 
 		shardedCounterService.decrement(TEST_COUNTER1);
 		shardedCounterService.decrement(TEST_COUNTER2);
 		shardedCounterService.decrement(TEST_COUNTER1);
 		shardedCounterService.decrement(TEST_COUNTER2);
 		shardedCounterService.decrement(TEST_COUNTER2);
 		shardedCounterService.decrement(TEST_COUNTER1);
 		shardedCounterService.decrement(TEST_COUNTER2);
 
 		assertEquals(3, shardedCounterService.getCounter(TEST_COUNTER1).get().getApproximateCount());
 		assertEquals(4, shardedCounterService.getCounter(TEST_COUNTER2).get().getApproximateCount());
 
 		shardedCounterService.decrement(TEST_COUNTER1);
 		shardedCounterService.decrement(TEST_COUNTER2);
 		shardedCounterService.decrement(TEST_COUNTER1);
 		shardedCounterService.decrement(TEST_COUNTER2);
 		shardedCounterService.decrement(TEST_COUNTER2);
 		shardedCounterService.decrement(TEST_COUNTER1);
 		shardedCounterService.decrement(TEST_COUNTER2);
 
 		assertEquals(0, shardedCounterService.getCounter(TEST_COUNTER1).get().getApproximateCount());
 		assertEquals(0, shardedCounterService.getCounter(TEST_COUNTER2).get().getApproximateCount());
 	}
 
 	@Test
 	public void testDecrementAll() throws InterruptedException
 	{
 		// Use 3 shards
 		shardedCounterService = initialShardedCounterService(3);
 		shardedCounterService.create(TEST_COUNTER1);
 		shardedCounterService.increment(TEST_COUNTER1, 10);
 
 		shardedCounterService.create(TEST_COUNTER2);
 		shardedCounterService.increment(TEST_COUNTER2, 10);
 
 		// Decrement 20
 		for (int i = 0; i < 10; i++)
 		{
 			logger.info("Decrement #" + i + " of 9 for counter 1");
 			shardedCounterService.decrement(TEST_COUNTER1);
 			logger.info("Decrement #" + i + " of 9 for counter 2");
 			shardedCounterService.decrement(TEST_COUNTER2);
 		}
 
 		assertEquals(0, shardedCounterService.getCounter(TEST_COUNTER1).get().getApproximateCount());
 		assertEquals(0, shardedCounterService.getCounter(TEST_COUNTER2).get().getApproximateCount());
 	}
 
 	@Test
 	public void testDecrementNegative() throws InterruptedException
 	{
 		// Use 3 shards
 		shardedCounterService = initialShardedCounterService(3);
 		shardedCounterService.create(TEST_COUNTER1);
 		shardedCounterService.increment(TEST_COUNTER1, 10);
 
 		shardedCounterService.create(TEST_COUNTER2);
 		shardedCounterService.increment(TEST_COUNTER2, 10);
 
 		// Decrement 20
 		for (int i = 0; i < 20; i++)
 		{
 			shardedCounterService.decrement(TEST_COUNTER1);
 			shardedCounterService.decrement(TEST_COUNTER2);
 		}
 
 		assertEquals(0, shardedCounterService.getCounter(TEST_COUNTER1).get().getApproximateCount());
 		assertEquals(0, shardedCounterService.getCounter(TEST_COUNTER2).get().getApproximateCount());
 	}
 
 	// ///////////////////
 	// ///////////////////
 	// ///////////////////
 
 	@Test(expected = RuntimeException.class)
 	public void testDeleteCounter_Null()
 	{
 		shardedCounterService.delete(null);
 	}
 
 	@Test(expected = RuntimeException.class)
 	public void testDeleteCounter_Empty()
 	{
 		shardedCounterService.delete("");
 	}
 
 	@Test(expected = RuntimeException.class)
 	public void testDeleteCounter_Blank()
 	{
 		shardedCounterService.delete(" ");
 	}
 
 	@Test
 	public void testDeleteCounter_NoneExists()
 	{
 		shardedCounterService.delete(TEST_COUNTER1);
 		Optional<Counter> optCounter = shardedCounterService.getCounter(TEST_COUNTER1);
 		assertFalse(optCounter.isPresent());
 
 		shardedCounterService.delete(TEST_COUNTER2);
 		optCounter = shardedCounterService.getCounter(TEST_COUNTER2);
 		assertFalse(optCounter.isPresent());
 	}
 
 	@Test
 	public void testDeleteCounterWith_NonDefaultQueue() throws InterruptedException
 	{
 		ShardedCounterServiceConfiguration.Builder builder = new ShardedCounterServiceConfiguration.Builder();
 		builder.withDeleteCounterShardQueueName(DELETE_COUNTER_SHARD_QUEUE_NAME);
 		ShardedCounterServiceConfiguration config = new ShardedCounterServiceConfiguration(builder);
 		shardedCounterService = new ShardedCounterService(memcache, config);
 
 		shardedCounterService.create(TEST_COUNTER1);
 		shardedCounterService.delete(TEST_COUNTER1);
 		assertPostDeleteCallSuccess(TEST_COUNTER1);
 	}
 
 	@Test
 	public void testDeleteCounterWith_NonDefaultQueueAndNonDefaultPath() throws InterruptedException
 	{
 		ShardedCounterServiceConfiguration.Builder builder = new ShardedCounterServiceConfiguration.Builder();
 		builder.withDeleteCounterShardQueueName(DELETE_COUNTER_SHARD_QUEUE_NAME);
 		builder.withRelativeUrlPathForDeleteTaskQueue("/coolpath");
 		ShardedCounterServiceConfiguration config = new ShardedCounterServiceConfiguration(builder);
 		shardedCounterService = new ShardedCounterService(memcache, config);
 
 		shardedCounterService.create(TEST_COUNTER1);
 		shardedCounterService.delete(TEST_COUNTER1);
 		assertPostDeleteCallSuccess(TEST_COUNTER1);
 	}
 
 	@Test
 	public void testDeleteWith1Shard() throws InterruptedException
 	{
 		Counter counter1 = shardedCounterService.create(TEST_COUNTER1);
 		assertNotNull(counter1);
 		assertNotNull(counter1.getTypedKey());
 
 		shardedCounterService.delete(TEST_COUNTER1);
 		assertPostDeleteCallSuccess(TEST_COUNTER1);
 
 		Counter counter2 = shardedCounterService.create(TEST_COUNTER2);
 		assertNotNull(counter2);
 		assertNotNull(counter2.getTypedKey());
 
 		shardedCounterService.delete(TEST_COUNTER2);
 		assertPostDeleteCallSuccess(TEST_COUNTER2);
 	}
 
 	@Test
 	public void testDeleteWith3Shards() throws InterruptedException
 	{
 		// Use 3 shards
 		shardedCounterService = initialShardedCounterService(3);
 		shardedCounterService.create(TEST_COUNTER1);
 		// Fill in multiple shards
 		for (int i = 0; i < 20; i++)
 		{
 			// Ensures that, statistically, 3 shards will be created
 			shardedCounterService.increment(TEST_COUNTER1, 1);
 		}
 
 		// Create test-counter2 so that the get below of CounterShards is
 		// accurately tested with existing shards for other counters.
 		shardedCounterService.create(TEST_COUNTER2);
 		// Fill in multiple shards
 		for (int i = 0; i < 20; i++)
 		{
 			// Ensures that, statistically, 3 shards will be created
 			shardedCounterService.increment(TEST_COUNTER2, 1);
 		}
 
 		// ///////////////
 		// Verify Counter Counts
 		// ///////////////
 
 		// Clear Memcache
 		this.memcache.clearAll();
 
 		Optional<Counter> optCounter1 = shardedCounterService.getCounter(TEST_COUNTER1);
 		assertTrue(optCounter1.isPresent());
 		assertEquals(20, optCounter1.get().getApproximateCount());
 
 		Optional<Counter> optCounter2 = shardedCounterService.getCounter(TEST_COUNTER2);
 		assertTrue(optCounter2.isPresent());
 		assertEquals(20, optCounter2.get().getApproximateCount());
 
 		// ///////////////
 		// Assert that 6 CounterShards Exist (3 for each Counter)
 		// ///////////////
 		this.assertAllCounterShardsExists(TEST_COUNTER1, 3);
 		this.assertAllCounterShardsExists(TEST_COUNTER2, 3);
 
 		// ///////////////
 		// Delete Counter 1
 		// ///////////////
 		shardedCounterService.delete(TEST_COUNTER1);
 		assertPostDeleteCallSuccess(TEST_COUNTER1);
 
 		// ///////////////
 		// Assert that Counter2 still has shards around
 		// ///////////////
 		assertAllCounterShardsExists(TEST_COUNTER2, 3);
 	}
 
 	@Test
 	public void testDeleteWith10Shards() throws InterruptedException
 	{
 		// Use 10 shards
 		shardedCounterService = initialShardedCounterService(10);
 		shardedCounterService.create(TEST_COUNTER1);
 		// Fill in multiple shards
 		for (int i = 0; i < 50; i++)
 		{
 			// Ensures that, statistically, 10 shards will be created with ~5
 			// each
 			shardedCounterService.increment(TEST_COUNTER1, 1);
 		}
 
 		// ///////////////
 		// Verify Counter Counts
 		// ///////////////
 
 		Optional<Counter> optCounter1 = shardedCounterService.getCounter(TEST_COUNTER1);
 		assertTrue(optCounter1.isPresent());
 		assertEquals(50, optCounter1.get().getApproximateCount());
 
 		// ///////////////
 		// Assert that 10 CounterShards Exist
 		// ///////////////
 
 		assertAllCounterShardsExists(TEST_COUNTER1, 10);
 
 		// ///////////////
 		// Delete Counter 1
 		// ///////////////
 
 		// See here:
 		// http://stackoverflow.com/questions/6632809/gae-unit-testing-taskqueue-with-testbed
 		// The dev app server is single-threaded, so it can't run tests in the
 		// background properly. Thus, we test that the task was added to the
 		// queue properly. Then, we manually run the shard-deletion code and
 		// assert that it's working properly.
 
 		// This asserts that the task was added to the queue properly...
 		shardedCounterService.delete(TEST_COUNTER1);
 		assertPostDeleteCallSuccess(TEST_COUNTER1);
 	}
 
 	// Tests counters with up to 15 shards and excerises each shard
 	// (statistically, but not perfectly)
 	@Test
 	public void testIncrement_XShards() throws InterruptedException
 	{
 		for (int i = 1; i <= 15; i++)
 		{
 			shardedCounterService = this.initialShardedCounterService(i);
 
 			doCounterIncrementAssertions(TEST_COUNTER1 + "-" + i, 15);
 		}
 	}
 
 	// Tests counters with up to 15 shards and excerises each shard
 	// (statistically, but not perfectly)
 	@Test
 	public void testDecrement_XShards() throws InterruptedException
 	{
 		for (int i = 1; i <= 15; i++)
 		{
 			shardedCounterService = this.initialShardedCounterService(i);
 
 			doCounterDecrementAssertions(TEST_COUNTER1 + "-" + i, 15);
 		}
 	}
 
 	// /////////////////////////
 	// Private Helpers
 	// /////////////////////////
 
 	private void doCounterIncrementAssertions(String counterName, int numIterations) throws InterruptedException
 	{
 		// ////////////////////////
 		// With Memcache Caching
 		// ////////////////////////
 		shardedCounterService.create(counterName + "-1");
 		for (int i = 1; i <= numIterations; i++)
 		{
 			shardedCounterService.increment(counterName + "-1", 1);
 			assertEquals(i, shardedCounterService.getCounter(counterName + "-1").get().getApproximateCount());
 		}
 
 		// ////////////////////////
 		// No Memcache Caching
 		// ////////////////////////
 		shardedCounterService.create(counterName + "-2");
 		for (int i = 1; i <= numIterations; i++)
 		{
 			this.memcache.clearAll();
 			shardedCounterService.increment(counterName + "-2", 1);
 			this.memcache.clearAll();
 			assertEquals(i, shardedCounterService.getCounter(counterName + "-2").get().getApproximateCount());
 		}
 
 		// ////////////////////////
 		// Memcache Cleared BEFORE Increment Only
 		// ////////////////////////
 		shardedCounterService.create(counterName + "-3");
 		for (int i = 1; i <= numIterations; i++)
 		{
 			// Simulate Capabilities Disabled
 			this.memcache.clearAll();
 			shardedCounterService.increment(counterName + "-3", 1);
 			assertEquals(i, shardedCounterService.getCounter(counterName + "-3").get().getApproximateCount());
 		}
 
 		// ////////////////////////
 		// Memcache Cleared AFTER Increment Only
 		// ////////////////////////
 		shardedCounterService.create(counterName + "-4");
 		// Do this with no cache before the get()
 		for (int i = 1; i <= numIterations; i++)
 		{
 			// Simulate Capabilities Disabled
 			shardedCounterService.increment(counterName + "-4", 1);
 			this.memcache.clearAll();
 			assertEquals(i, shardedCounterService.getCounter(counterName + "-4").get().getApproximateCount());
 		}
 
 	}
 
 	private void doCounterDecrementAssertions(String counterName, int numIterations) throws InterruptedException
 	{
 		shardedCounterService.create(counterName + "-1");
 		shardedCounterService.increment(counterName + "-1", numIterations);
 
 		// ////////////////////////
 		// With Memcache Caching
 		// ////////////////////////
 		for (int i = 1; i <= numIterations; i++)
 		{
 			shardedCounterService.decrement(counterName + "-1");
 			assertEquals(numIterations - i, shardedCounterService.getCounter(counterName + "-1").get()
 				.getApproximateCount());
 		}
 
 		// /////////////////////////
 		// Reset the counter
 		// /////////////////////////
 		shardedCounterService.increment(counterName + "-1", numIterations);
 		assertEquals(numIterations, shardedCounterService.getCounter(counterName + "-1").get().getApproximateCount());
 
 		// ////////////////////////
 		// No Memcache Caching
 		// ////////////////////////
 		for (int i = 1; i <= numIterations; i++)
 		{
 			this.memcache.clearAll();
 			shardedCounterService.decrement(counterName + "-1");
 			this.memcache.clearAll();
 			assertEquals(numIterations - i, shardedCounterService.getCounter(counterName + "-1").get()
 				.getApproximateCount());
 		}
 
 		// /////////////////////////
 		// Reset the counter
 		// /////////////////////////
 		shardedCounterService.increment(counterName + "-1", numIterations);
 		assertEquals(numIterations, shardedCounterService.getCounter(counterName + "-1").get().getApproximateCount());
 
 		// ////////////////////////
 		// Memcache Cleared BEFORE Decrement Only
 		// ////////////////////////
 		for (int i = 1; i <= numIterations; i++)
 		{
 			this.memcache.clearAll();
 			shardedCounterService.decrement(counterName + "-1");
 			assertEquals(numIterations - i, shardedCounterService.getCounter(counterName + "-1").get()
 				.getApproximateCount());
 		}
 
 		// /////////////////////////
 		// Reset the counter
 		// /////////////////////////
 		shardedCounterService.increment(counterName + "-1", numIterations);
 		assertEquals(numIterations, shardedCounterService.getCounter(counterName + "-1").get().getApproximateCount());
 
 		// ////////////////////////
 		// Memcache Cleared AFTER Decrement Only
 		// ////////////////////////
 		for (int i = 1; i <= numIterations; i++)
 		{
 			shardedCounterService.decrement(counterName + "-1");
 			this.memcache.clearAll();
 			assertEquals(numIterations - i, shardedCounterService.getCounter(counterName + "-1").get()
 				.getApproximateCount());
 		}
 
 	}
 
 	/**
 	 * Create a new {@link CounterService} with the specified
 	 * "number of initial shards".
 	 * 
 	 * @param numInitialShards
 	 * @return
 	 */
 	private CounterService initialShardedCounterService(int numInitialShards)
 	{
 		ShardedCounterServiceConfiguration.Builder builder = new ShardedCounterServiceConfiguration.Builder();
 		builder.withNumInitialShards(numInitialShards);
 		ShardedCounterServiceConfiguration config = new ShardedCounterServiceConfiguration(builder);
 
 		ShardedCounterService service = new ShardedCounterService(memcache, config);
 		return service;
 	}
 
 	/**
 	 * Asserts that the {@code numExpectedTasksInQueue} matches the actual
 	 * number of tasks in the queue.
 	 */
 	private void assertNumTasksInQueue(int numExpectedTasksInQueue)
 	{
 		LocalTaskQueue ltq = LocalTaskQueueTestConfig.getLocalTaskQueue();
 		QueueStateInfo qsi = ltq.getQueueStateInfo().get(
 			QueueFactory.getQueue(DELETE_COUNTER_SHARD_QUEUE_NAME).getQueueName());
 		assertEquals(numExpectedTasksInQueue, qsi.getTaskInfo().size());
 	}
 
 	/**
 	 * After calling {@link ShardedCounterService#delete(String)}, the following
 	 * code asserts that a task was properly added to a task queue, and then
 	 * manually deletes the counterShards (simulating what would happen in a
 	 * real task queue).
 	 * 
 	 * @throws InterruptedException
 	 */
 	private void assertPostDeleteCallSuccess(String counterName) throws InterruptedException
 	{
 		Optional<Counter> optCounter = shardedCounterService.getCounter(counterName);
 		assertEquals(CounterStatus.DELETING, optCounter.get().getCounterStatus());
 
 		// See here:
 		// http://stackoverflow.com/questions/6632809/gae-unit-testing-taskqueue-with-testbed
 		// The dev app server is single-threaded, so it can't run tests in the
 		// background properly. Thus, we test that the task was added to the
 		// queue properly. Then, we manually run the shard-deletion code and
 		// assert that it's working properly.
 
 		if (countdownLatch.getCount() == 1)
 		{
 			this.waitForCountdownLatchThenReset();
 		}
 		// By this point, the task should be processed in the queue and should
 		// not exist...
 		this.assertNumTasksInQueue(0);
 
 		this.shardedCounterService.onTaskQueueCounterDeletion(counterName);
 		this.assertAllCounterShardsExists(counterName, 0);
 
 		optCounter = shardedCounterService.getCounter(counterName);
 		assertFalse(optCounter.isPresent());
 	}
 
 	/**
 	 * Does a "consistent" lookup for all counterShards to ensure they exist in
 	 * the datastore.
 	 */
 	private void assertAllCounterShardsExists(String counterName, int numCounterShardsToGet)
 	{
 		for (int i = 0; i < numCounterShardsToGet; i++)
 		{
 			// The following command does a query, which is only eventually
 			// consistent. This fails the unit-test occasionally because we
 			// can't yet set the HRD to always consistent. Thus, we do a get()
 			// for all 10 shards and ensure they're there.
 			// List<CounterShard> allCounterShards =
 			// ObjectifyService.ofy().load().type(CounterShard.class).list();
 
 			Key<CounterShard> shardKey = Key.create(CounterShard.class, counterName + "-" + i);
 			CounterShard counterShard = ObjectifyService.ofy().load().key(shardKey).get();
 			assertNotNull(counterShard);
 		}
 
 		if (numCounterShardsToGet == 0)
 		{
 			// Assert that no counterShards exists
 			Key<CounterShard> shardKey = Key.create(CounterShard.class, counterName + "-" + numCounterShardsToGet);
 			CounterShard counterShard = ObjectifyService.ofy().load().key(shardKey).get();
 			assertTrue(counterShard == null);
 		}
 		else
 		{
 			// Assert that no more shards exist for this counterShard starting
 			// at {@code numCounterShardsToGet}
 			Key<CounterShard> shardKey = Key.create(CounterShard.class, counterName + "-" + numCounterShardsToGet);
 			CounterShard counterShard = ObjectifyService.ofy().load().key(shardKey).get();
 			assertTrue(counterShard == null);
 		}
 	}
 
 	private void waitForCountdownLatchThenReset() throws InterruptedException
 	{
 		if (countdownLatch.getCount() != 0)
 		{
 			countdownLatch.awaitAndReset(5L, TimeUnit.SECONDS);
 		}
 	}
 
 }
