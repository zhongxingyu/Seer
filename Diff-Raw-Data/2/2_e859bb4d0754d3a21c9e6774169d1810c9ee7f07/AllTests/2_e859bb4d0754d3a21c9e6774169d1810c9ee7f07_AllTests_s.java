 package dk.itu.grp11.test;
 
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.junit.runners.Suite.SuiteClasses;
 
 /**
 * This test suite is responsible for running a complete test of the internal structures of the system.
* If these tests pass, all the methods in Commands, Nexus and DataBaseCom is working (in their basic functionality)
* as well as most of the tests in the Controller. TODO
 */
 @RunWith(Suite.class)
 @SuiteClasses({ LatLonToUTMTest.class, MapTest.class, NetworkTest.class, ParserTest.class, PathFinderTest.class,
 PointTest.class, RoadTest.class, SessionTest.class, TrafficDirectionTest.class })
 public class AllTests {
 
 }
