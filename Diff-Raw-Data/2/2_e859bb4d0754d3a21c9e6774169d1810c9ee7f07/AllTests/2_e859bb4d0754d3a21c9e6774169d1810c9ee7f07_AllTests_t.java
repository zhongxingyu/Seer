 package dk.itu.grp11.test;
 
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.junit.runners.Suite.SuiteClasses;
 
 /**
 * This test suite is responsible for running a complete test of the internal structures of the system.
 */
 @RunWith(Suite.class)
 @SuiteClasses({ LatLonToUTMTest.class, MapTest.class, NetworkTest.class, ParserTest.class, PathFinderTest.class,
 PointTest.class, RoadTest.class, SessionTest.class, TrafficDirectionTest.class })
 public class AllTests {
 
 }
