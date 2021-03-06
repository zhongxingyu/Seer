 /*
  * Copyright 2011-2013 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.springframework.data.redis.connection.jedis;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import org.junit.After;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.data.redis.RedisSystemException;
 import org.springframework.data.redis.connection.AbstractConnectionPipelineIntegrationTests;
 import org.springframework.data.redis.connection.DefaultStringTuple;
 import org.springframework.data.redis.connection.StringRedisConnection.StringTuple;
 import org.springframework.test.annotation.IfProfileValue;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import redis.clients.jedis.Tuple;
 
 /**
  * Integration test of {@link JedisConnection} pipeline functionality
  *
  * @author Jennifer Hickey
  *
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("JedisConnectionIntegrationTests-context.xml")
 public class JedisConnectionPipelineIntegrationTests extends
 		AbstractConnectionPipelineIntegrationTests {
 
 	/**
 	 * Individual results from closePipeline should be converted from
 	 * LinkedHashSet to List
 	 **/
 	private boolean convertResultToList;
 
 	@After
 	public void tearDown() {
 		try {
 			connection.flushDb();
 			connection.close();
 		} catch (Exception e) {
 			// Jedis leaves some incomplete data in OutputStream on NPE caused
 			// by null key/value tests
 			// Attempting to close the connection will result in error on
 			// sending QUIT to Redis
 		}
 		connection = null;
 	}
 
	@Ignore("DATAREDIS-141 Jedis dbSize/flush ops execute synchronously while pipelining")
	public void testDbSize() {
	}

	@Ignore("DATAREDIS-141 Jedis dbSize/flush ops execute synchronously while pipelining")
	public void testFlushDb() {
	}

 	@Ignore("DATAREDIS-143 Jedis ClassCastExceptions closing pipeline on certain ops")
 	public void testGetConfig() {
 	}
 
 	@Ignore("DATAREDIS-143 Jedis ClassCastExceptions closing pipeline on certain ops")
 	public void testWatch() {
 	}
 
 	@Ignore("DATAREDIS-143 Jedis ClassCastExceptions closing pipeline on certain ops")
 	public void testUnwatch() {
 	}
 
 	@Ignore("DATAREDIS-143 Jedis ClassCastExceptions closing pipeline on certain ops")
 	public void testSortStore() {
 	}
 
 	@Ignore("DATAREDIS-143 Jedis ClassCastExceptions closing pipeline on certain ops")
 	public void testMultiExec() {
 	}
 
 	@Ignore("DATAREDIS-143 Jedis NPE closing pipeline on certain ops")
 	public void testMultiDiscard() {
 	}
 
 	@Ignore("DATAREDIS-155 Exists returns true after key is supposed to expire")
 	public void testExpire() throws Exception {
 	}
 
 	@Ignore("DATAREDIS-155 Exists returns true after key is supposed to expire")
 	public void testSetEx() {
 	}
 
 	// Unsupported Ops
 	@Test(expected = RedisSystemException.class)
 	public void testBitSet() throws Exception {
 		super.testBitSet();
 	}
 
 	@Test(expected = RedisSystemException.class)
 	public void testRandomKey() {
 		super.testRandomKey();
 	}
 
 	@Test(expected = RedisSystemException.class)
 	public void testGetRangeSetRange() {
 		super.testGetRangeSetRange();
 	}
 
 	@Test(expected = RedisSystemException.class)
 	public void testPingPong() throws Exception {
 		super.testPingPong();
 	}
 
 	@Test(expected = RedisSystemException.class)
 	public void testInfo() throws Exception {
 		super.testInfo();
 	}
 
 	// Overrides, usually due to return values being Long vs Boolean or Set vs
 	// List
 
 	@Test
 	public void testRenameNx() {
 		connection.set("nxtest", "testit");
 		actual.add(connection.renameNX("nxtest", "newnxtest"));
 		actual.add(connection.get("newnxtest"));
 		actual.add(connection.exists("nxtest"));
 		verifyResults(Arrays.asList(new Object[] { 1l, "testit", false }), actual);
 	}
 
 	@Test
 	public void testKeys() throws Exception {
 		convertResultToList = true;
 		super.testKeys();
 	}
 
 	@Test
 	public void testZAddAndZRange() {
 		convertResultToList = true;
 		super.testZAddAndZRange();
 	}
 
 	@Test
 	public void testZIncrBy() {
 		convertResultToList = true;
 		super.testZIncrBy();
 	}
 
 	@Test
 	public void testZInterStore() {
 		convertResultToList = true;
 		super.testZInterStore();
 	}
 
 	@Test
 	public void testZRangeByScore() {
 		convertResultToList = true;
 		super.testZRangeByScore();
 	}
 
 	@Test
 	public void testZRangeByScoreOffsetCount() {
 		convertResultToList = true;
 		super.testZRangeByScoreOffsetCount();
 	}
 
 	@Test
 	public void testZRevRange() {
 		convertResultToList = true;
 		super.testZRevRange();
 	}
 
 	@Test
 	public void testZRem() {
 		convertResultToList = true;
 		super.testZRem();
 	}
 
 	@Test
 	public void testZRemRangeByScore() {
 		convertResultToList = true;
 		super.testZRemRangeByScore();
 	}
 
 	@Test
 	public void testZUnionStore() {
 		convertResultToList = true;
 		super.testZUnionStore();
 	}
 
 	@Test
 	public void testZRemRangeByRank() {
 		convertResultToList = true;
 		super.testZRemRangeByRank();
 	}
 
 	@Test
 	public void testHDel() throws Exception {
 		actual.add(connection.hSet("test", "key", "val"));
 		actual.add(connection.hDel("test", "key"));
 		actual.add(connection.hDel("test", "foo"));
 		actual.add(connection.hExists("test", "key"));
 		verifyResults(Arrays.asList(new Object[] { 1l, 1l, 0l, false }), actual);
 	}
 
 	@Test
 	public void testHSetGet() throws Exception {
 		String hash = getClass() + ":hashtest";
 		String key1 = UUID.randomUUID().toString();
 		String key2 = UUID.randomUUID().toString();
 		String value1 = "foo";
 		String value2 = "bar";
 		actual.add(connection.hSet(hash, key1, value1));
 		actual.add(connection.hSet(hash, key2, value2));
 		actual.add(connection.hGet(hash, key1));
 		actual.add(connection.hGetAll(hash));
 		Map<String, String> expected = new HashMap<String, String>();
 		expected.put(key1, value1);
 		expected.put(key2, value2);
 		verifyResults(Arrays.asList(new Object[] { 1l, 1l, value1, expected }), actual);
 	}
 
 	@Test
 	@IfProfileValue(name = "runLongTests", value = "true")
 	public void testExpireAt() throws Exception {
 		connection.set("exp2", "true");
 		actual.add(connection.expireAt("exp2", System.currentTimeMillis() / 1000 + 1));
 		Thread.sleep(2000);
 		actual.add(connection.exists("exp2"));
 		verifyResults(Arrays.asList(new Object[] { 1l, false }), actual);
 	}
 
 	@Test
 	@IfProfileValue(name = "runLongTests", value = "true")
 	public void testPersist() throws Exception {
 		connection.set("exp3", "true");
 		actual.add(connection.expire("exp3", 1));
 		actual.add(connection.persist("exp3"));
 		Thread.sleep(1500);
 		actual.add(connection.exists("exp3"));
 		verifyResults(Arrays.asList(new Object[] { 1l, 1l, true }), actual);
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	protected Object convertResult(Object result) {
 		Object convertedResult = super.convertResult(result);
 		if (convertedResult instanceof Set) {
 			if (convertResultToList) {
 				// Other providers represent zSets as Lists, so transform here
 				return new ArrayList((Set) result);
 			} else if (!(((Set) result).isEmpty())
 					&& ((Set) convertedResult).iterator().next() instanceof Tuple) {
 				List<StringTuple> tuples = new ArrayList<StringTuple>();
 				for (Tuple value : ((Set<Tuple>) convertedResult)) {
 					DefaultStringTuple tuple = new DefaultStringTuple(
 							(byte[]) value.getBinaryElement(), value.getElement(), value.getScore());
 					tuples.add(tuple);
 				}
 				return tuples;
 			}
 		}
 		return convertedResult;
 	}
 }
