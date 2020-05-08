 package com.iappsam.test;
 
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.junit.runners.Suite.SuiteClasses;
 
 import com.iappsam.managers.sessions.ItemManagerSessionTest;
import com.iappsam.reporting.APPReportTest;
 import com.iappsam.search.SearchingTest;
 
 @RunWith(Suite.class)
@SuiteClasses({ ItemManagerSessionTest.class, SearchingTest.class, APPReportTest.class })
 public class TestSuite {
 
 }
