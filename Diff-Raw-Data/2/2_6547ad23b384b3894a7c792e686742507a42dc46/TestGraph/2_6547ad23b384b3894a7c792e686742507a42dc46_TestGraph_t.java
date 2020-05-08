 // The Grinder
 // Copyright (C) 2000, 2001  Paco Gomez
 // Copyright (C) 2000, 2001  Philip Aston
 
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
 
 package net.grinder.console.swingui;
 
 import junit.framework.TestCase;
 import junit.swingui.TestRunner;
 //import junit.textui.TestRunner;
 
 import java.awt.BorderLayout;
 import java.text.DecimalFormat;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 
 import net.grinder.statistics.PeakStatisticExpression;
 import net.grinder.statistics.StatisticExpression;
 import net.grinder.statistics.StatisticExpressionFactory;
 import net.grinder.statistics.StatisticsIndexMap;
 import net.grinder.statistics.TestStatistics;
 import net.grinder.statistics.TestStatisticsFactory;
 
 
 
 /**
  * @author Philip Aston
  * @version $Revision$
  */
 public class TestGraph extends TestCase
 {
     public static void main(String[] args)
     {
 	TestRunner.run(TestGraph.class);
     }
 
     public TestGraph(String name)
     {
 	super(name);
     }
 
     private int m_pauseTime = 1;
     private Random s_random = new Random();
     private JFrame m_frame;
 
     protected void setUp() throws Exception
     {
 	m_frame = new JFrame("Test Graph");
     }
 
     protected void tearDown() throws Exception
     {
 	m_frame.dispose();
     }
 
     private void createUI(JComponent component) throws Exception
     {
         m_frame.getContentPane().add(component, BorderLayout.CENTER);
         m_frame.pack();
         m_frame.setVisible(true);
     }
 
     public void testRamp() throws Exception
     {
 	final Graph graph = new Graph(25);
 	createUI(graph);
 
 	graph.setMaximum(150);
 
 	for (int i=0; i<150; i++) {
 	    graph.add(i);
 	    pause();
 	}
     }
 
     public void testRandom() throws Exception
     {
 	final Graph graph = new Graph(100);
 	createUI(graph);
 
 	graph.setMaximum(1);
 
 	for (int i=0; i<200; i++) {
 	    graph.add(s_random.nextDouble());
 	    pause();
 	}
     }
 
     public void testLabelledGraph() throws Exception
     {
 	final TestStatisticsFactory testStatisticsFactory
 	    = TestStatisticsFactory.getInstance();
 
 	final StatisticsIndexMap indexMap =
 	    StatisticsIndexMap.getProcessInstance();
 
 	final StatisticsIndexMap.LongIndex periodIndex =
 	    indexMap.getIndexForLong("period");
 
 	final StatisticExpressionFactory statisticExpressionFactory =
 	    StatisticExpressionFactory.getInstance();
 
 	final StatisticExpression tpsExpression =
 	    statisticExpressionFactory.createExpression(
 		"(* 1000 (/(+ untimedTransactions timedTransactions) period))"
 		);
 
 	final PeakStatisticExpression peakTPSExpression =
 	    statisticExpressionFactory.createPeak(
 		indexMap.getIndexForDouble("peakTPS"), tpsExpression);
 
 	final LabelledGraph labelledGraph =
 	    new LabelledGraph("Test", new Resources(), tpsExpression,
 			      peakTPSExpression);
 
 	createUI(labelledGraph);
 
 	double peak = 0d;
 
 	final TestStatistics cumulativeStatistics =
 	    testStatisticsFactory.create();
 
 	final DecimalFormat format = new DecimalFormat();
 
 	final int period = 1000;
 
 	for (int i=0; i<200; i++) {
 	    final TestStatistics intervalStatistics =
 		testStatisticsFactory.create();
 
 	    intervalStatistics.setValue(periodIndex, period);
 
 	    while (s_random.nextInt() > 0) {
 		intervalStatistics.addTransaction();
 	    }
 
 	    long time;
 
 	    while ((time = s_random.nextInt()) > 0) {
 		intervalStatistics.addTransaction(time % 10000);
 	    }
 
 	    while (s_random.nextFloat() > 0.95) {
 		intervalStatistics.addError();
 	    }
 
 	    cumulativeStatistics.add(intervalStatistics);
 	    cumulativeStatistics.setValue(periodIndex, (1+i)*period);
 
	    peakTPSExpression.update(intervalStatistics, cumulativeStatistics);
 	    labelledGraph.add(intervalStatistics, cumulativeStatistics,
 			      format);
 	    pause();
 	}
     }
 
     private void pause() throws Exception
     {
 	if (m_pauseTime > 0) {
 	    Thread.sleep(m_pauseTime);
 	}
     }
 }
 
