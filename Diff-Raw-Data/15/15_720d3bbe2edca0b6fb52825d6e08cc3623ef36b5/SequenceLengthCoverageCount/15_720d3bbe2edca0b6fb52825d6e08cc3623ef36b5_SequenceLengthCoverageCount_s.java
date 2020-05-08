 /*  
  *  Copyright (c) 2009-@year@. The GUITAR group at the University of Maryland. Names of owners of this group may
  *  be obtained by sending an e-mail to atif@cs.umd.edu
  * 
  *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
  *  documentation files (the "Software"), to deal in the Software without restriction, including without 
  *  limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  *  the Software, and to permit persons to whom the Software is furnished to do so, subject to the following 
  *  conditions:
  * 
  *  The above copyright notice and this permission notice shall be included in all copies or substantial 
  *  portions of the Software.
  *
  *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
  *  LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO 
  *  EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER 
  *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR 
  *  THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
  */
 package edu.umd.cs.guitar.testcase.plugin;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import java.util.Vector;
 
 import edu.umd.cs.guitar.model.GUITARConstants;
 import edu.umd.cs.guitar.model.IO;
 import edu.umd.cs.guitar.model.data.EFG;
 import edu.umd.cs.guitar.model.data.EventGraphType;
 import edu.umd.cs.guitar.model.data.EventType;
 import edu.umd.cs.guitar.model.data.StepType;
 import edu.umd.cs.guitar.model.data.TestCase;
 import edu.umd.cs.guitar.testcase.TestCaseGeneratorConfiguration;
 import edu.umd.cs.guitar.util.GUITARLog;
 import edu.umd.cs.guitar.util.TimeCounter;
 
 /**
  * 
  * Randomly generate EIG test case with a specific length
  * 
  * @author <a href="mailto:baonn@cs.umd.edu"> Bao N. Nguyen </a>
  * 
  */
 public class SequenceLengthCoverageCount extends TCPlugin {
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.umd.cs.guitar.testcase.plugin.TCPlugin#isValidArgs()
 	 */
 	@Override
 	public boolean isValidArgs() {
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.umd.cs.guitar.testcase.plugin.TCPlugin#generate()
 	 */
 	@Override
	public void generate(EFG efg, String outputDir, int nMaxNumber) {
 
 		this.efg = efg;
 
 		initialize();
 
 		List<EventType> eventList = efg.getEvents().getEvent();
 
 		List<EventType> interactionEventList = new ArrayList<EventType>();
 
 		for (EventType event : eventList) {
 				interactionEventList.add(event);
 		}
 
 		System.out.println("Counting potential test cases ...");
 
 		// --------------------------
 		// Count total number of test cases
 		long nAllTestCases = 0;
 		for (EventType e : interactionEventList) {
 			nAllTestCases += countTestCases(
 					TestCaseGeneratorConfiguration.LENGTH, e);
 		}
 
 		System.out.println("Total test cases: "
 				+ nAllTestCases);
 	}
 
 
 	private long countTestCases(int length, EventType root) {
 
 		if (length <= 1) {
 			return 1;
 		} else {
 			int count = 0;
 			for (EventType succEvent : succs.get(root)) {
 				count += countTestCases(length - 1, succEvent);
 			}
 
 			return count;
 		}
 	}
 
 
 }
