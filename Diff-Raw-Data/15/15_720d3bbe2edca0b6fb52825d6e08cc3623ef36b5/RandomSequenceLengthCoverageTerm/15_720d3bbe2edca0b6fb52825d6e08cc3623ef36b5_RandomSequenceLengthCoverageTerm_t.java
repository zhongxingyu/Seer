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
 public class RandomSequenceLengthCoverageTerm extends TCPlugin {
 
 	/**
 	 * 
 	 */
 	private static final int ALL_TEST_CASES = 0;
 	int nMaxNumber;
 
 	// Generate all possible test cases
 	boolean isGenAll;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.umd.cs.guitar.testcase.plugin.TCPlugin#isValidArgs()
 	 */
 	@Override
 	public boolean isValidArgs() {
 		return true;
 	}
 
 	private boolean isSelectedEvent(EventType event) {
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.umd.cs.guitar.testcase.plugin.TCPlugin#generate()
 	 */
 	@Override
	public void generate(EFG efg, String outputDir, int nMaxNumber,
                             boolean noDuplicateEvent, boolean treatTerminalEventSpecially) {
                // Note: noDuplicateEvent and treatTerminalEventSpecially haven't
                // been used here.
 
 		new File(outputDir).mkdir();
 
 		this.efg = efg;
 		this.nMaxNumber = nMaxNumber;
 
 		initialize();
 
 		List<EventType> eventList = efg.getEvents().getEvent();
 
 		List<EventType> interactionEventList = new ArrayList<EventType>();
 
 		for (EventType event : eventList) {
 			if (isSelectedEvent(event)) {
 				interactionEventList.add(event);
 			}
 		}
 
 		Random random = new Random();
 
 		System.out.println("Counting potential test cases ...");
 
 		// --------------------------
 		// Count total number of test cases
 		nAllTestCases = 0;
 		index = 0;
 
 		for (EventType e : interactionEventList) {
 			nAllTestCases += countTestCases(
 					TestCaseGeneratorConfiguration.LENGTH, e);
 		}
 
 		System.out.println("Total number of potential test cases: "
 				+ nAllTestCases);
 
 		lTestCaseIndex = new ArrayList<Integer>();
 		int i = 0;
 
 		if (nMaxNumber == 0) {
 			isGenAll = true;
 			nMaxNumber = nAllTestCases;
 		} else
 			nMaxNumber = (nAllTestCases > TestCaseGeneratorConfiguration.MAX_NUMBER) ? TestCaseGeneratorConfiguration.MAX_NUMBER
 					: nAllTestCases;
 
 		if (!isGenAll)
 			System.out.println("Generating random index.....");
 
 		while (i < nMaxNumber) {
 			int nRandomIndex = random.nextInt(nAllTestCases);
 			if (!lTestCaseIndex.contains(nRandomIndex)) {
 				lTestCaseIndex.add(nRandomIndex);
 				System.out.println(nRandomIndex);
 				i++;
 			}
 		}
 
 		index = 0;
 		generatedTestcases = 0;
 
 		// A loop to generate test case
 
 		TimeCounter timeCounter = new TimeCounter("RandomSequenceLength");
 		timeCounter.start();
 
 		for (EventType aEvent : interactionEventList) {
 			// generateRandomWithLength(TestCaseGeneratorConfiguration.LENGTH,
 			// new LinkedList<EventType>(), e, random);
 			LinkedList<EventType> initialList = new LinkedList<EventType>();
 			initialList.add(aEvent);
 			generateRandomWithLength(TestCaseGeneratorConfiguration.LENGTH,
 					initialList);
 		}
 
 		timeCounter.printInfo();
 
 		System.out.println("Done");
 
 	}
 
 	int index;;
 	int generatedTestcases;
 	int nAllTestCases;
 	// List<Integer> lGeneratedTestCase = new ArrayList<Integer>();
 	List<Integer> lTestCaseIndex;
 
 	/**
 	 * Count the total number of potential test cases
 	 * 
 	 * <p>
 	 * 
 	 * @param length
 	 * @param root
 	 * @return
 	 */
 	private int countTestCases(int length, EventType root) {
 
 		if (length <= 1) {
 			return 1;
 		} else {
 			int count = 0;
 
 			for (EventType succEvent : succs.get(root)) {
 
 				// Ignore non-interaction events
 				if (!isSelectedEvent(succEvent))
 					continue;
 				count += countTestCases(length - 1, succEvent);
 
 			}
 
 			return count;
 		}
 	}
 
 	private void generateRandomWithLength(int length,
 			LinkedList<EventType> postfix) {
 
 		if (generatedTestcases > nMaxNumber && nMaxNumber != ALL_TEST_CASES)
 			return;
 
 		if (length <= 1) {
 
 			EventType root = postfix.getFirst();
 			LinkedList<EventType> path = getPathToRoot(root);
 			index++;
 
 			if (path != null) {
 				if (isGenAll || lTestCaseIndex.contains(index)) {
 					// if (true) {
 					// Remove the root event itself to avoid duplication
 					if (path.size() > 0)
 						path.removeLast();
 
 					LinkedList<EventType> tTestCase = new LinkedList<EventType>();
 
 					// tTestCase.addAll(path);
 					// tTestCase.addAll(postfix);
 
 					String sTestName = TEST_NAME_PREFIX;
 
 					if (TestCaseGeneratorConfiguration.LENGTH < 30) {
 						for (EventType event : postfix) {
 							sTestName += ("_" + event.getEventId());
 						}
 					} else {
 						sTestName += ("_" + index);
 					}
 					sTestName += TEST_NAME_SUFIX;
 
 					System.out.println("Generating test case:"
 							+ generatedTestcases);
 
 					// Write to file
 
 					TestCase tc = factory.createTestCase();
 					List<StepType> lStep = new ArrayList<StepType>();
 
 					for (EventType e : path) {
 						StepType step = factory.createStepType();
 						step.setEventId(e.getEventId());
 						step.setReachingStep(true);
 						lStep.add(step);
 					}
 
 					for (EventType e : postfix) {
 						StepType step = factory.createStepType();
 						step.setEventId(e.getEventId());
 						step.setReachingStep(false);
 						lStep.add(step);
 					}
 
 					// Adding a terminal event
 					EventType terminal = getNeighborTerminal(postfix.getLast());
 
 					if (terminal != null) {
 
 						System.out.println("********GOT HERE**********");
 						System.out.println(terminal.getType());
 						StepType step = factory.createStepType();
 						step.setEventId(terminal.getEventId());
 						step.setReachingStep(false);
 						lStep.add(step);
 					}
 					tc.setStep(lStep);
 
 					String sPath = TestCaseGeneratorConfiguration.OUTPUT_DIR
 							+ File.separator + sTestName;
 					System.out.println("Writting to " + sPath);
 					IO.writeObjToFile(tc, sPath);
 
 					generatedTestcases++;
 				}
 
 			} else {
 				GUITARLog.log.info("root: " + root.getWidgetId());
 				GUITARLog.log.info(postfix + " is unreachable");
 
 			}
 
 		} else {
 			EventType lastEvent = postfix.getLast();
 			for (EventType succEvent : succs.get(lastEvent)) {
 				LinkedList<EventType> extendedPostfix = new LinkedList<EventType>(
 						postfix);
 				extendedPostfix.addLast(succEvent);
 				generateRandomWithLength(length - 1, extendedPostfix);
 			}
 		}
 	}
 
 	/**
 	 * Find a final terminal event
 	 * 
 	 * <p>
 	 * 
 	 * @param event
 	 * @return
 	 */
 	private EventType getNeighborTerminal(EventType event) {
 		List<EventType> neighborEventList = succs.get(event);
 
 		for (EventType e : neighborEventList) {
 			if (GUITARConstants.TERMINAL.equals(e.getType()))
 				return e;
 		}
 		return null;
 	}
 
 	/**
 	 * A Debugging method
 	 */
 	private void printGraph() {
 		// GUITARLog.log.info("Graph");
 
 		List<EventType> eventList = efg.getEvents().getEvent();
 		int eventGraphSize = eventList.size();
 		EventGraphType eventGraph = efg.getEventGraph();
 
 		for (int row = 0; row < eventGraphSize; row++) {
 
 			EventType currentEvent = eventList.get(row);
 			Vector<EventType> s = new Vector<EventType>();
 
 			for (int col = 0; col < eventGraphSize; col++) {
 
 				EventType event = eventList.get(col);
 
 				int relation = eventGraph.getRow().get(row).getE().get(col);
 
 				if (relation == GUITARConstants.FOLLOW_EDGE) {
 					GUITARLog.log.info(currentEvent.getEventId() + ","
 							+ currentEvent.getWidgetId() + "->"
 							+ event.getEventId() + "," + event.getWidgetId());
 				}
 
 			}
 			GUITARLog.log.info("");
 		}
 
 	}
 
 	/**
 	 * Just for debugging
 	 */
 	private void debug() {
 
 		GUITARLog.log.info("Debugging.........");
 		List<EventType> eventList = efg.getEvents().getEvent();
 		for (EventType e : eventList) {
 			GUITARLog.log.info("================================");
 			GUITARLog.log.info("Analyzing " + e.getEventId() + "-"
 					+ e.getWidgetId());
 			List<EventType> predEventList = preds.get(e);
 
 			String sEventList = "Pred: [";
 			for (EventType predEvent : predEventList) {
 				sEventList += (predEvent.getEventId() + "-"
 						+ predEvent.getWidgetId() + ", ");
 
 			}
 			sEventList += "]";
 			GUITARLog.log.info(sEventList);
 		}
 
 		GUITARLog.log.info("Done Debugging.........");
 
 	}
 
 }
