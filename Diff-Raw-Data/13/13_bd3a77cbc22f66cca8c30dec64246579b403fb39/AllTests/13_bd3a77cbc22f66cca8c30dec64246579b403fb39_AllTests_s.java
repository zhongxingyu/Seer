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
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import junit.swingui.TestRunner;
 //import junit.textui.TestRunner;
 
 
 /**
  * @author Philip Aston
  * @version $Revision$
  */
 public class AllTests
 {
     public static void main(String[] args)
     {
 	TestRunner.run(AllTests.class);
     }
 
     public static Test suite()
     {
 	final TestSuite suite = new TestSuite();
 	suite.addTest(new TestSuite(TestTestData.class));
 	suite.addTest(new TestSuite(TestTestStatistics.class));
 	return suite;
     }
 }
