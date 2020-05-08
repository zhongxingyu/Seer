 package com.alexrnl.commons.utils.object;
 
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.junit.runners.Suite.SuiteClasses;
 
 /**
  * Test suite for the object package.
  * @author Alex
  */
 @RunWith(Suite.class)
@SuiteClasses({ AttributeComparatorTest.class, AutoCompareTest.class, ComparisonError.class,
 	HashCodeUtilsTest.class, ReflectUtilsTest.class })
 public class ObjectTests {
 }
