 /**
  * Analytica - beta version - Systems Monitoring Tool
  *
  * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
  * KleeGroup, Centre d'affaire la Boursidire - BP 159 - 92357 Le Plessis Robinson Cedex - France
  *
  * This program is free software; you can redistribute it and/or modify it under the terms
  * of the GNU General Public License as published by the Free Software Foundation;
  * either version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with this program;
  * if not, see <http://www.gnu.org/licenses>
  */
 package io.analytica.hcube;
 
 import io.analytica.hcube.cube.HCounterType;
 import io.analytica.hcube.cube.HCube;
 import io.analytica.hcube.cube.HCubeBuilder;
 import io.analytica.hcube.cube.HMetric;
 import io.analytica.hcube.cube.HMetricBuilder;
 import io.analytica.hcube.cube.HMetricKey;
 import io.analytica.hcube.dimension.HCategory;
 import io.analytica.hcube.dimension.HCubeKey;
 import io.analytica.hcube.dimension.HTime;
 import io.analytica.hcube.dimension.HTimeDimension;
 import io.analytica.hcube.impl.HCubeManagerImpl;
 import io.analytica.hcube.plugins.store.memory.MemoryHCubeStorePlugin;
 import io.analytica.hcube.query.HQuery;
 import io.analytica.hcube.query.HQueryBuilder;
 import io.analytica.hcube.query.HQueryUtil;
 import io.analytica.hcube.result.HPoint;
 import io.analytica.hcube.result.HResult;
 import io.analytica.hcube.result.HSerie;
 import io.vertigo.kernel.lang.DateBuilder;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * Test.
  * 
  *  - a request ==> page, duration, status 
  * 
  * @author pchretien
  */
 public final class HCubeManagerTest {
 	private static final String APP_NAME = "MY_APP";
 	private static final String PAGES = "PAGES";
 	private final HCubeManager cubeManager = new HCubeManagerImpl(new MemoryHCubeStorePlugin());
 
 	//private final HCubeManager cubeManager = new HCubeManagerImpl(new LuceneHCubeStorePlugin());
 
 	@Test
 	public void testQuery() {
 		final Date start = new Date();
 		final HQuery query1 = new HQueryBuilder()//
 				.on(HTimeDimension.Hour)//
 				.from(new DateBuilder(start).addHours(-3).toDateTime())//
 				.to(start)//
 				.with(PAGES)//
 				.build();
 
 		final HQuery query2 = new HQueryBuilder()//
 				.on(HTimeDimension.Hour)//
 				.from("NOW-3h")//
 				.to("NOW")//
 				.with(PAGES)//
 				.build();
 		//---
 		Assert.assertEquals(3, HQueryUtil.findTimes(query1.getTimeSelection()).size()); //3 HOURS
 		Assert.assertEquals(3, HQueryUtil.findTimes(query2.getTimeSelection()).size()); //3 HOURS
 		Assert.assertTrue(HQueryUtil.findTimes(query1.getTimeSelection()).containsAll(HQueryUtil.findTimes(query2.getTimeSelection())));
 		Assert.assertTrue(HQueryUtil.findTimes(query2.getTimeSelection()).containsAll(HQueryUtil.findTimes(query1.getTimeSelection())));
 		Assert.assertEquals(query1.toString(), query2.toString());
 	}
 
 	@Test
 	public void testQuery2() {
 		final HQuery query = new HQueryBuilder()//
 				.on(HTimeDimension.Hour)//
 				.from("NOW")//
 				.to("NOW+3d")//
 				.with(PAGES)//
 				.build();
 		//---
 		Assert.assertEquals(3 * 24, HQueryUtil.findTimes(query.getTimeSelection()).size()); //72 hours 
 	}
 
 	/*
 	 * Query must not accept a 'date from' > 'date to'.
 	 */
 	@Test(expected = Exception.class)
 	public void testQueryFail() {
 		new HQueryBuilder()//
 				.on(HTimeDimension.Hour)//
 				.from("NOW")//
 				.to("NOW-3m")//
 				.with(PAGES)//
 				.build();
 	}
 
 	/*
 	 * Checking categories before and after populating data.
 	 */
 	@Test
 	public void testCategoriesDictionnary() throws ParseException {
 		final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
 		final Date start = dateFormat.parse("2012/12/12");
 		final int days = 1;
 		//----	
 		Assert.assertEquals(0, cubeManager.getStore().getAllRootCategories(APP_NAME).size());
 		Assert.assertEquals(0, cubeManager.getStore().getAllSubCategories(APP_NAME, new HCategory(PAGES)).size());
 		//---
 		populateData(cubeManager, start, days);
 		//----	
 		Assert.assertEquals(1, cubeManager.getStore().getAllRootCategories(APP_NAME).size());
 		Assert.assertEquals(1, cubeManager.getStore().getAllSubCategories(APP_NAME, new HCategory(PAGES)).size());
 	}
 
 	/**
 	 * data are collected during 10 days from 2012/12/12.
 	 */
 	@Test
 	public void testLoadAndQuery() throws ParseException {
 		final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
 		final Date start = dateFormat.parse("2012/12/12");
 		final int days = 10;
 		final Date end = dateFormat.parse("2012/12/13");
 
 		//----	
 		populateData(cubeManager, start, days);
 		//----	
 		final HQuery query = new HQueryBuilder()//
 				.on(HTimeDimension.Hour)//
 				.from(start)//
 				.to(end)//
 				.with(PAGES)//
 				.build();
 
 		final HResult result = cubeManager.getStore().execute(APP_NAME, query);
 
 		Assert.assertEquals(query, result.getQuery());
 
 		//Check : 1 category
 		Assert.assertEquals(1, result.getAllCategories().size());
 
 		//Check : 24 cubes(per hour) by day
 		Assert.assertEquals(24, result.getSerie(new HCategory(PAGES)).getCubes().size());
 
 		//Check : serie contains 2 metric (DURATION and WEIGHT)
 		Assert.assertEquals(2, result.getSerie(new HCategory(PAGES)).getMetrics().size());
 
 		//
 		final HSerie serie = result.getSerie(new HCategory(PAGES));
 		Assert.assertEquals(new HCategory(PAGES), serie.getCategory());
 
 		for (final Entry<HTime, HCube> entry : serie.getCubes().entrySet()) {
 			HCube cube = entry.getValue();
 			//	HTime time = entry.getKey();
 			Assert.assertTrue(cube.toString().startsWith("{"));
 			Assert.assertTrue(cube.toString().endsWith("}"));
 			Assert.assertEquals(2, cube.getMetrics().size());
 			final HMetric metric = cube.getMetric(new HMetricKey("DURATION", true));
 			Assert.assertEquals(100 * 60 * 2, metric.getCount(), 0);
 			Assert.assertEquals(100, metric.getMean(), 0);
 			Assert.assertEquals(1, metric.get(HCounterType.min), 0);
 		}
 		final HMetric metric = serie.getMetric(new HMetricKey("DURATION", true));
 		Assert.assertTrue(metric.toString().startsWith("{"));
 		Assert.assertTrue(metric.toString().endsWith("}"));
 
 		Assert.assertEquals("DURATION", metric.getKey().getName());
 		Assert.assertEquals(100 * 60 * 2 * 24, metric.getCount(), 0);
 		Assert.assertEquals(100, metric.getMean(), 0);
 		Assert.assertTrue(metric.get(HCounterType.stdDev) > 0);
 
 		Assert.assertEquals(1, metric.get(HCounterType.min), 0);
 		//---
 
 		final List<HPoint> points = serie.getPoints(new HMetricKey("DURATION", true));
 		Assert.assertEquals(24, points.size());
 		int h = 0;
 		for (final HPoint point : serie.getPoints(new HMetricKey("DURATION", true))) {
 			Assert.assertEquals(h, point.getDate().getHours());
 			h++;
 			Assert.assertEquals(100 * 60 * 2, point.getMetric().getCount(), 0);
 			Assert.assertEquals(100, point.getMetric().getMean(), 0);
 			Assert.assertEquals(1, point.getMetric().get(HCounterType.min), 0);
 		}
 
 	}
 
 	/**
 	 * data are collected during 10 days from 2012/12/12.
 	 */
 	@Test
 	public void testLoadAndQuery2() throws ParseException {
 		final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
 		final Date start = dateFormat.parse("2012/12/12");
 		final int days = 1;
 		final Date end = dateFormat.parse("2012/12/13");
 
 		//----	
 		populateData(cubeManager, start, days);
 		//----	
 		final HQuery query = new HQueryBuilder()//
 				.on(HTimeDimension.SixMinutes)//
 				.from(start)//
 				.to(end)//
 				.with(PAGES)//
 				.build();
 
 		final HResult result = cubeManager.getStore().execute(APP_NAME, query);
 
 		Assert.assertEquals(query, result.getQuery());
 
 		//Check : 1 category
 		Assert.assertEquals(1, result.getAllCategories().size());
 
 		//Check : 10*24 cubes per minute
 		Assert.assertEquals(240, result.getSerie(new HCategory(PAGES)).getCubes().size());
 	}
 
 	@Test
 	public void testMetrics() {
 		final HMetricKey metricKey = new HMetricKey("TEST", true);
 		//---
 		final HMetricBuilder metricBuilder = new HMetricBuilder(metricKey);
 		double sqrSum = 0;
 		for (int i = 0; i < 100; i++) {
 			metricBuilder.withValue(1000 - i);
 			metricBuilder.withValue(1000 + i);
 			sqrSum += (1000 - i) * (1000 - i) + (1000 + i) * (1000 + i);
 		}
 		final HMetric metric = metricBuilder.build();
 		Assert.assertEquals(200, metric.getCount());
 		Assert.assertEquals(1000, metric.getMean(), 0);
 		Assert.assertEquals(100 * 2000, metric.get(HCounterType.sum), 0);
 		Assert.assertEquals(1000 - 99, metric.get(HCounterType.min), 0);
 		Assert.assertEquals(1000 + 99, metric.get(HCounterType.max), 0);
 		Assert.assertEquals(sqrSum, metric.get(HCounterType.sqrSum), 0);
 	}
 
 	@Test
 	public void testHistogram() {
 		final HMetricKey metricKey = new HMetricKey("TEST", true);
 		//---
 		final HMetricBuilder metricBuilder = new HMetricBuilder(metricKey);
 		metricBuilder.withValue(0);//<0
 		metricBuilder.withValue(1);//<1
 		metricBuilder.withValue(2);//<2
 		metricBuilder.withValue(3);//<5
 		metricBuilder.withValue(4);//<5
 		metricBuilder.withValue(5);//<5
 		metricBuilder.withValue(6);//<10
 		metricBuilder.withValue(7);//<10
 		metricBuilder.withValue(8);//<10
 		metricBuilder.withValue(9);//<10
 		metricBuilder.withValue(10);//<10
 		metricBuilder.withValue(11);//<20
 		metricBuilder.withValue(35);//<50
 		metricBuilder.withValue(455);//<500
 		metricBuilder.withValue(355);//<500
 		metricBuilder.withValue(111222333);//<200000000
 
 		final HMetric metric = metricBuilder.build();
 		final Map<Double, Long> histogram = metric.getDistribution().getData();
 
 		Assert.assertEquals(1, histogram.get(0d), 0);
 		Assert.assertEquals(1, histogram.get(1d), 0);
 		Assert.assertEquals(1, histogram.get(2d), 0);
 		Assert.assertEquals(3, histogram.get(5d), 0);
 		Assert.assertEquals(5, histogram.get(10d), 0);
 		Assert.assertEquals(1, histogram.get(20d), 0);
 		Assert.assertEquals(1, histogram.get(50d), 0);
 		Assert.assertEquals(2, histogram.get(500d), 0);
 		Assert.assertEquals(1, histogram.get(200000000d), 0);
 	}
 
 	@Test
 	public void testTimeDimension() {
 		final Date start = new Date();
 		HTime now;
 		//---
 		now = new HTime(start, HTimeDimension.Minute);
 		Assert.assertEquals((start.getMinutes() + 1) % 60, new Date(now.next().inMillis()).getMinutes());
 		//---
 		now = new HTime(start, HTimeDimension.Hour);
 		Assert.assertEquals((start.getHours() + 1) % 24, new Date(now.next().inMillis()).getHours());
 		//---
 		now = new HTime(start, HTimeDimension.Day);
 		Assert.assertEquals((start.getDay() + 1) % 7, new Date(now.next().inMillis()).getDay()); //getDay = day of the week
 		//---
 		now = new HTime(start, HTimeDimension.Month);
 		Assert.assertEquals((start.getMonth() + 1) % 12, new Date(now.next().inMillis()).getMonth());
 		//---
 		now = new HTime(start, HTimeDimension.Year);
 		Assert.assertEquals((start.getYear() + 1), new Date(now.next().inMillis()).getYear());
 	}
 
 	/**
 	 * data are collected during 10 days from 2012/12/12.
 	 * @throws ParseException Date parsing error 
 	 */
 	@Test
 	public void testMergeOnHTime() throws ParseException {
 		final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 		final Date current = dateFormat.parse("2012/12/12 12:12:12");
 		final HMetricKey durationKey = new HMetricKey("DURATION", true);
 		final HMetricKey weightKey = new HMetricKey("WEIGHT", false);
 		//----	
 		addCube(cubeManager, current, 64, durationKey, weightKey);
 		//----	
 		checkMergedMetric(cubeManager, HTimeDimension.Minute, //
 				new DateBuilder(current).addMinutes(-1).toDateTime(), //
 				new DateBuilder(current).addMinutes(2).toDateTime(), //
 				weightKey, 1, 64, 64, 64);
 		checkMergedMetric(cubeManager, HTimeDimension.SixMinutes, //
 				new DateBuilder(current).addMinutes(-6).toDateTime(), //
 				new DateBuilder(current).addMinutes(12).toDateTime(), //
 				weightKey, 1, 64, 64, 64);
 		checkMergedMetric(cubeManager, HTimeDimension.Hour, //
 				new DateBuilder(current).addHours(-1).toDateTime(), //
 				new DateBuilder(current).addHours(2).toDateTime(), //
 				weightKey, 1, 64, 64, 64);
 		checkMergedMetric(cubeManager, HTimeDimension.Day, //
 				new DateBuilder(current).addDays(-1).toDateTime(), //
 				new DateBuilder(current).addDays(2).toDateTime(), //
 				weightKey, 1, 64, 64, 64);
 		checkMergedMetric(cubeManager, HTimeDimension.Month, //
 				new DateBuilder(current).addMonths(-1).toDateTime(), //
 				new DateBuilder(current).addMonths(2).toDateTime(), //
 				weightKey, 1, 64, 64, 64);
 		checkMergedMetric(cubeManager, HTimeDimension.Year, //
 				new DateBuilder(current).addYears(-1).toDateTime(), //
 				new DateBuilder(current).addYears(2).toDateTime(), //
 				weightKey, 1, 64, 64, 64);
 	}
 
 	@Test
 	public void testMergeOnHTime2() throws ParseException {
 		final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 		final Date current = dateFormat.parse("2012/12/12 12:12:12");
 		final HMetricKey durationKey = new HMetricKey("DURATION", true);
 		final HMetricKey weightKey = new HMetricKey("WEIGHT", false);
 		//----	
 		addCube(cubeManager, current, 64, durationKey, weightKey);
 		addCube(cubeManager, new DateBuilder(current).addMinutes(2).toDateTime(), 256, durationKey, weightKey);
 		addCube(cubeManager, new DateBuilder(current).addMinutes(2 * 6).toDateTime(), 1024, durationKey, weightKey);
 		addCube(cubeManager, new DateBuilder(current).addHours(2).toDateTime(), 4096, durationKey, weightKey);
 		addCube(cubeManager, new DateBuilder(current).addDays(2).toDateTime(), 16384, durationKey, weightKey);
 		addCube(cubeManager, new DateBuilder(current).addMonths(-2).toDateTime(), 65536, durationKey, weightKey); //remove 2 months in order to stay the same year
 		addCube(cubeManager, new DateBuilder(current).addYears(2).toDateTime(), 262144, durationKey, weightKey);
 
 		//----	
 		checkMergedMetric(cubeManager, HTimeDimension.Minute, //
 				new DateBuilder(current).addMinutes(-1).toDateTime(), //
 				new DateBuilder(current).addMinutes(2).toDateTime(), //
 				weightKey, 1, 64, 64, 64);
 		checkMergedMetric(cubeManager, HTimeDimension.SixMinutes, //
 				new DateBuilder(current).addMinutes(-1 * 6).toDateTime(), //
 				new DateBuilder(current).addMinutes(2 * 6).toDateTime(), //
 				weightKey, 2, 64 + 256, 64, 256);
 		checkMergedMetric(cubeManager, HTimeDimension.Hour, //
 				new DateBuilder(current).addHours(-1).toDateTime(), //
 				new DateBuilder(current).addHours(2).toDateTime(), //
 				weightKey, 3, 64 + 256 + 1024, 64, 1024);
 		checkMergedMetric(cubeManager, HTimeDimension.Day, //
 				new DateBuilder(current).addDays(-1).toDateTime(), //
 				new DateBuilder(current).addDays(2).toDateTime(), //
 				weightKey, 4, 64 + 256 + 1024 + 4096, 64, 4096);
 		checkMergedMetric(cubeManager, HTimeDimension.Month, //
 				new DateBuilder(current).addMonths(-1).toDateTime(), //
 				new DateBuilder(current).addMonths(2).toDateTime(), //
 				weightKey, 5, 64 + 256 + 1024 + 4096 + 16384, 64, 16384);
 		checkMergedMetric(cubeManager, HTimeDimension.Year, //
 				new DateBuilder(current).addYears(-1).toDateTime(), //
 				new DateBuilder(current).addYears(2).toDateTime(), //
 				weightKey, 6, 64 + 256 + 1024 + 4096 + 16384 + 65536, 64, 65536);
 	}
 
 	//-------------------------------------------------------------------------	
 	//---------------------------STATIC ---------------------------------------	
 	//-------------------------------------------------------------------------	
 	private static void addCube(final HCubeManager cubeManager, final Date current, final int weightValue, final HMetricKey duration, final HMetricKey weight) {
 		final HCategory category = new HCategory(PAGES, "WELCOME");
 		final HTime time = new HTime(current, HTimeDimension.Minute);
 		final HCubeKey cubeKey = new HCubeKey(time, category/*, location*/);
 		final HMetric metricMetric = new HMetricBuilder(duration)//
 				.withValue(100)//
 				.build();
 		final HMetric weightMetric = new HMetricBuilder(weight)//
 				.withValue(weightValue)//
 				.build();
 		final HCube cube = new HCubeBuilder()//
 				.withMetric(metricMetric)//
 				.withMetric(weightMetric)//
 				.build();
 		cubeManager.getStore().push(APP_NAME, cubeKey, cube);
 	}
 
 	private static void populateData(final HCubeManager cubeManager, final Date startDate, final int days) {
 		final long start = System.currentTimeMillis();
 		System.out.println("start = " + startDate);
 		final HCategory category = new HCategory(PAGES, "WELCOME");
 
 		final HMetricKey duration = new HMetricKey("DURATION", true);
 		final HMetricKey weight = new HMetricKey("WEIGHT", false);
 
 		long mc = 0;
 		for (int day = 0; day < days; day++) {
 			for (int h = 0; h < 24; h++) {
 				for (int min = 0; min < 60; min++) {
 					final Date current = new DateBuilder(startDate).addDays(day).addHours(h).addMinutes(min).toDateTime();
 					final HTime time = new HTime(current, HTimeDimension.Minute);
 					//--------		
 					final HCubeKey cubeKey = new HCubeKey(time, category);
 
 					final HMetricBuilder metricBuilder = new HMetricBuilder(duration);
 					for (int i = 0; i < 100; i++) {
 						metricBuilder.withValue(100 - i);
 						metricBuilder.withValue(100 + i);
 					}
 
 					final HMetric weightMetric = new HMetricBuilder(weight).withValue(h).build();
 
 					final HCube cube = new HCubeBuilder()//
 							.withMetric(metricBuilder.build())//
 							.withMetric(weightMetric)//
 							.build();
 
 					cubeManager.getStore().push(APP_NAME, cubeKey, cube);
 					//--
 					checkMemory(cubeManager, APP_NAME, day);
 					if ((mc++) % (60 * 24 * 10) == 0) {
 						System.out.println(">>> day/hour/min = " + day + "/" + h + "/" + min + " in " + (System.currentTimeMillis() - start) + " ms");
 					}
 				}
 			}
 		}
 
 	}
 
 	private static void checkMemory(final HCubeManager cubeManager, String appName, final long day) {
 		if ((Runtime.getRuntime().totalMemory() / Runtime.getRuntime().maxMemory()) > 0.9) {
 			cubeManager.getStore().count(appName);
 			System.gc();
 			//---
 			if ((Runtime.getRuntime().totalMemory() / Runtime.getRuntime().maxMemory()) > 0.9) {
 				System.out.println(">>>> total mem =" + Runtime.getRuntime().totalMemory());
 				System.out.println(">>>> max  mem =" + Runtime.getRuntime().maxMemory());
 				System.out.println(">>>> mem total > 90% - days =" + day);
 				System.out.println(">>>> cubes count =" + cubeManager.getStore().count(appName));
 
 				System.out.println(">>>> cube footprint =" + (Runtime.getRuntime().maxMemory() / cubeManager.getStore().count(appName)) + " octets");
 				try {
 					Thread.sleep(1000 * 20);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				System.exit(0);
 			}
 		}
 	}
 
	private static void checkMergedMetric(final HCubeManager cubeManager, final HTimeDimension timeDimension, final Date start, final Date end, final HMetricKey weight, final int espectedCount, final double espectedSum, final double espectedMin, final double espectedMax) {
 		//----	
 		final HQuery query = new HQueryBuilder()//
 				.on(timeDimension)//
 				.from(start)//
 				.to(end)//
 				.with(PAGES)//
 				.build();
 
 		final HResult result = cubeManager.getStore().execute(APP_NAME, query);
 		Assert.assertEquals(query, result.getQuery());
 		//Check : 1 category
 		Assert.assertEquals(1, result.getAllCategories().size());
 		//Check : 10*24 cubes per minute
 		final Map<HTime, HCube> cubes = result.getSerie(new HCategory(PAGES)).getCubes();
 		Assert.assertEquals(3, cubes.size());
		assertMetricEquals(cubes.get(new HTime(start, timeDimension)), weight, 0, 0, Double.NaN, Double.NaN, Double.NaN);
		assertMetricEquals(cubes.get(1), weight, espectedCount, espectedSum, espectedSum / espectedCount, espectedMin, espectedMax);
		assertMetricEquals(cubes.get(2), weight, 0, 0, Double.NaN, Double.NaN, Double.NaN);
 	}
 
 	private static void assertMetricEquals(final HCube hCube, final HMetricKey metricKey, final double count, final double sum, final double mean, final double min, final double max) {
 		assertMetricEquals(hCube.getMetric(metricKey), count, sum, mean, min, max);
 	}
 
 	private static void assertMetricEquals(final HMetric metric, final double count, final double sum, final double mean, final double min, final double max) {
 		if (metric != null) {
 			Assert.assertEquals(count, metric.get(HCounterType.count), 0);
 			Assert.assertEquals(count, metric.getCount(), 0); //test accesseur rapide
 			Assert.assertEquals(sum, metric.get(HCounterType.sum), 0);//test accesseur rapide 0);
 			Assert.assertEquals(mean, metric.get(HCounterType.mean), 0);
 			Assert.assertEquals(mean, metric.getMean(), 0);//test accesseur rapide
 			Assert.assertEquals(min, metric.get(HCounterType.min), 0);
 			Assert.assertEquals(max, metric.get(HCounterType.max), 0);
 		} else {
 			Assert.assertEquals(count, 0, 0);
 			Assert.assertEquals(sum, 0, 0);
 			Assert.assertTrue(Double.isNaN(mean));
 			Assert.assertTrue(Double.isNaN(min));
 			Assert.assertTrue(Double.isNaN(max));
 		}
 	}
 }
