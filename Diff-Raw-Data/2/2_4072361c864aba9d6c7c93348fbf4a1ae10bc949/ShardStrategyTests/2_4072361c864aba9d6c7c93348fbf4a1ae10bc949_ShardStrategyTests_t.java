 package com.pardot.service.analytics;
 
 import com.google.common.collect.Range;
 import com.pardot.service.tools.cobject.shardingstrategy.ShardStrategyException;
 import com.pardot.service.tools.cobject.shardingstrategy.ShardingStrategyMonthly;
 import com.pardot.service.tools.cobject.shardingstrategy.ShardingStrategyNone;
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 
 /**
  * Pardot, An ExactTarget Company
  * User: robrighter
  * Date: 4/16/13
  */
 public class ShardStrategyTests extends TestCase {
 	/**
 	 * Create the test case
 	 *
 	 * @param testName name of the test case
 	 */
 	public ShardStrategyTests( String testName ) {
 		super( testName );
 	}
 
 	/**
 	 * @return the suite of tests being tested
 	 */
 	public static Test suite() {
 		return new TestSuite( ShardStrategyTests.class );
 	}
 
	public void testShardingStrategyMonthly() throws ShardStrategyException {
 		ShardingStrategyMonthly subject = new ShardingStrategyMonthly();
 		DateTime d = new DateTime(2013,2,22,1,0,0, DateTimeZone.UTC);
 		long actual = subject.getShardKey(d.getMillis());
 		assertEquals("Should generate correct shard key for date", 158,actual);
 
 		//test with offset
 		subject = new ShardingStrategyMonthly();
 		subject.setOffset(20);
 		d = new DateTime(2013,2,22,1,0,0, DateTimeZone.UTC);
 		actual = subject.getShardKey(d.getMillis());
 		assertEquals("Should generate correct shard key for date given offset", 178,actual);
 
 		//test the range bounded
 		subject = new ShardingStrategyMonthly();
 		DateTime d1 = new DateTime(2013,2,22,2,0,0, DateTimeZone.UTC); //158
 		DateTime d2 = new DateTime(2014,2,22,2,0,0, DateTimeZone.UTC); //170
 		Range<Long> range = subject.getShardKeyRange(d1.getMillis(),d2.getMillis());
 		assertEquals("Range should have appropriate start point", 158L, range.lowerEndpoint().longValue());
 		assertEquals("Range should have appropriate end point", 170L, range.upperEndpoint().longValue());
 
 		//test range unbounded
 		subject = new ShardingStrategyMonthly();
 		d1 = new DateTime(2014,2,22,2,0,0, DateTimeZone.UTC); //170
 		range = subject.getShardKeyRange(d1.getMillis(),0);
 		assertEquals("Range should have appropriate start point", 170L, range.lowerEndpoint().longValue());
 		assertTrue("Range should have no upper bound", !range.hasUpperBound());
 		assertTrue("Range should have a lower bound",range.hasLowerBound());
 	}
 
 	public void testShardingStrategyNone() throws ShardStrategyException {
 		ShardingStrategyNone subject = new ShardingStrategyNone();
 		DateTime d = new DateTime(2013,2,22,1,0,0, DateTimeZone.UTC);
 		long actual = subject.getShardKey(d.getMillis());
 		assertEquals("Should generate correct shard key for date", 1L ,actual);
 
 		//test with offset
 		subject = new ShardingStrategyNone();
 		subject.setOffset(20);
 		d = new DateTime(2013,2,22,1,0,0, DateTimeZone.UTC);
 		actual = subject.getShardKey(d.getMillis());
 		assertEquals("Should generate correct shard key for date given offset", 21L ,actual);
 
 		//test the range bounded
 		subject = new ShardingStrategyNone();
 		DateTime d1 = new DateTime(2013,2,22,2,0,0, DateTimeZone.UTC);
 		DateTime d2 = new DateTime(2014,2,22,2,0,0, DateTimeZone.UTC);
 		Range<Long> range = subject.getShardKeyRange(d1.getMillis(),d2.getMillis());
 		assertEquals("Range should have appropriate start point", 1L , range.lowerEndpoint().longValue());
 		assertEquals("Range should have appropriate end point", 1L , range.upperEndpoint().longValue());
 
 		//test range unbounded
 		subject = new ShardingStrategyNone();
 		d1 = new DateTime(2014,2,22,2,0,0, DateTimeZone.UTC);
 		range = subject.getShardKeyRange(d1.getMillis(),0);
 		assertEquals("Range should have appropriate start point", 1L , range.lowerEndpoint().longValue());
 		assertTrue("Range should have no upper bound",!range.hasUpperBound());
 		assertTrue("Range should have a lower bound",range.hasLowerBound());
 	}
 }
