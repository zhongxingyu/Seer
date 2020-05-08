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
 
 package net.grinder.statistics;
 
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
 public class TestStatistics extends TestCase
 {
     public static void main(String[] args)
     {
 	TestRunner.run(TestStatistics.class);
     }
 
     public TestStatistics(String name)
     {
 	super(name);
     }
 
     protected void setUp()
     {
     }
 
     public void testCreation() 
     {
 	final Statistics statistics = new Statistics();
 
 	assertEquals(0, statistics.getTransactions());
 	assertEquals(0, statistics.getErrors());
     }
 
     public void testAddsAndEquals() 
     {
 	final Statistics statistics0 = new Statistics();
 	final Statistics statistics1 = new Statistics();
 
 	assertEquals(statistics0, statistics0);
 	assertEquals(statistics0, statistics1);
 	
 	statistics0.addTransaction(700);
 	statistics0.addTransaction(300);
 	assert(!statistics0.equals(statistics1));
 	statistics1.addTransaction(500);
 	assert(!statistics0.equals(statistics1));
 	statistics1.addTransaction(500);
 	assertEquals(statistics0, statistics1);
 
 	statistics0.addError();
 	assert(!statistics0.equals(statistics1));
 	statistics1.addError();
 	assertEquals(statistics0, statistics1);
 
 	assertEquals(statistics0, statistics0);
 	assertEquals(statistics1, statistics1);
     }
 
     public void testGetClone()
     {
 	final Statistics statistics0 = new Statistics();
 	final Statistics statistics1 = statistics0.getClone();
 	assertEquals(statistics0, statistics1);
 	assert(statistics0 != statistics1);
 	assert(statistics0.getClass() == statistics1.getClass());
     }
 
     public void testAdd()
     {
 	final Statistics statistics0 = new Statistics();
 	final Statistics statistics1 = new Statistics();
 
 	// 0 + 0 = 0
 	statistics0.add(statistics1);
 	assertEquals(statistics0, statistics1);
 
 	// 0 + 1 = 1
 	statistics0.addTransaction(100);
 	statistics1.add(statistics0);
 	assertEquals(statistics0, statistics1);
 
 	// 1 + 1 != 1
 	statistics1.add(statistics0);
 	assert(!statistics0.equals(statistics1));
 
 	// 1 + 1 = 2
 	statistics0.add(statistics0);
 	assertEquals(statistics0, statistics1);
 
 	assertEquals(2, statistics0.getTransactions());
 	assertEquals(0, statistics0.getErrors());
     }
 
     public void testGetDelta()
     {
 	final Statistics statistics0 = new Statistics();
 	statistics0.addTransaction(1234);
 
 	final Statistics statistics1 = statistics0.getDelta(false);
 	assertEquals(statistics0, statistics1);
 
 	final Statistics statistics2 = statistics0.getDelta(true);
 	assertEquals(statistics0, statistics2);
 
 	final Statistics statistics3 = statistics0.getDelta(false);
 	assert(!statistics0.equals(statistics3));
 
 	statistics0.addError();
 
 	final Statistics statistics4 = statistics0.getDelta(true);
 	assertEquals(0, statistics4.getTransactions());
 	assertEquals(1, statistics4.getErrors());
 
 	statistics0.addTransaction(5678);
 	final Statistics statistics5 = statistics0.getDelta(true);
 	assertEquals(1, statistics5.getTransactions());
 	assertEquals(0, statistics5.getErrors());
     }
 
     public void testGetAverageTransactionTime()
     {
 	final Statistics statistics0 = new Statistics();
 
 	assert(Double.isNaN(statistics0.getAverageTransactionTime()));
 
 	final double accuracy = 1e-20;
 
 	statistics0.addTransaction(1);
 	assertEquals(1d, statistics0.getAverageTransactionTime(), accuracy);
 
 	statistics0.addTransaction(1);
 	assertEquals(1d, statistics0.getAverageTransactionTime(), accuracy);
 
 	statistics0.addTransaction(2);
 	assertEquals(4/3d, statistics0.getAverageTransactionTime(), accuracy);
 
 	statistics0.addTransaction(0);
 	assertEquals(1d, statistics0.getAverageTransactionTime(), accuracy);
     }
 }
