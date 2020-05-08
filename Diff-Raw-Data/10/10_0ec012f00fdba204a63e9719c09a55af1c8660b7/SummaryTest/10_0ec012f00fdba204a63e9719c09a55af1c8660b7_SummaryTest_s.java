 package com.castlemon.jenkins.performance.domain.reporting;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.agileware.test.PropertiesTester;
 import org.junit.Test;
 
 public class SummaryTest {
 
 	@Test
 	public void testProperties() throws Exception {
 		PropertiesTester tester = new PropertiesTester();
 		tester.testAll(Summary.class);
 	}
 
 	@Test
 	public void testGetFormattedAverageDurationSingle() {
 		Summary summary = new Summary();
 		PerformanceEntry entry = new PerformanceEntry();
 		entry.setElapsedTime(1000000000l);
 		List<PerformanceEntry> entries = new ArrayList<PerformanceEntry>();
 		entries.add(entry);
 		summary.setEntries(entries);
 		Assert.assertEquals("1 sec", summary.getFormattedAverageDuration());
 	}
 
 	@Test
 	public void testGetFormattedAverageDurationMultiple() {
 		Summary summary = new Summary();
 		List<PerformanceEntry> entries = new ArrayList<PerformanceEntry>();
 		PerformanceEntry entry1 = new PerformanceEntry();
 		entry1.setElapsedTime(1000000000l);
 		entries.add(entry1);
 		PerformanceEntry entry2 = new PerformanceEntry();
 		entry2.setElapsedTime(5000000000l);
 		entries.add(entry2);
 
 		summary.setEntries(entries);
 		Assert.assertEquals("3 secs", summary.getFormattedAverageDuration());
 	}
 
 	@Test
 	public void testGetFormattedAverageDurationRounding() {
 		Summary summary = new Summary();
 		List<PerformanceEntry> entries = new ArrayList<PerformanceEntry>();
 		PerformanceEntry entry1 = new PerformanceEntry();
 		entry1.setElapsedTime(1000000000l);
 		entries.add(entry1);
 		PerformanceEntry entry2 = new PerformanceEntry();
 		entry2.setElapsedTime(6000000000l);
 		entries.add(entry2);
 		summary.setEntries(entries);
 		Assert.assertEquals("3 secs 500 ms",
 				summary.getFormattedAverageDuration());
 	}
 }
