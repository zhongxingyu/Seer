 // The Grinder
// Copyright (C) 2001  Paco Gomez
// Copyright (C) 2001  Philip Aston
 
 // This program is free software; you can redistribute it and/or
 // modify it under the terms of the GNU General Public License
 // as published by the Free Software Foundation; either version 2
 // of the License, or (at your option) any later version.
 
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 
 package net.grinder.engine.process;
 
 import junit.framework.TestCase;
 import junit.swingui.TestRunner;
 //import junit.textui.TestRunner;
 
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 
 import net.grinder.plugininterface.Test;
 import net.grinder.util.GrinderProperties;
 
 
 /**
  * @author Philip Aston
  * @version $Revision$
  */
 public class TestTestData extends TestCase
 {
     public static void main(String[] args)
     {
 	TestRunner.run(TestTestData.class);
     }
 
     public TestTestData(String name)
     {
 	super(name);
     }
 
     protected void setUp()
     {
     }
 
     class MyTest implements Test
     {
 	private final Integer m_testNumber;
 	private final String m_description;
 	private final GrinderProperties m_parameters;
 
 	public MyTest(Integer testNumber, String description,
 		      GrinderProperties parameters)
 	{
 	    m_testNumber = testNumber;
 	    m_description = description;
 	    m_parameters = parameters;
 	}
 
 	public Integer getTestNumber()
 	{
 	    return m_testNumber;
 	}
 
 	public String getDescription()
 	{
 	    return m_description;
 	}
 
 	public GrinderProperties getParameters()
 	{
 	    return m_parameters;
 	}
     }
 
     private void applyCommonAssertions(TestData testData, Test test, 
 				       long sleepTime)
     {
 	assertEquals(test, testData.getTest());
 	assertEquals(sleepTime, testData.getSleepTime());
 	assertNotNull(testData.getStatistics());
 	assertNotNull(testData.toString());
     }
 
     public void test0() throws Exception
     {
 	final MyTest test = new MyTest(new Integer(99), "Some stuff",
 				       new GrinderProperties());
 	
 	final long sleepTime = 1234;
 	final TestData testData = new TestData(test, sleepTime);
 
 	applyCommonAssertions(testData, test, sleepTime);
     }
 
     public void test1() throws Exception
     {
 	final GrinderProperties properties = new GrinderProperties();
 	properties.put("Something", "blah");
 
 	final MyTest test = new MyTest(new Integer(-33), "", properties);
 	
 	final long sleepTime = 1234;
 	final TestData testData = new TestData(test, sleepTime);
 
 	applyCommonAssertions(testData, test, sleepTime);
     }
 }
