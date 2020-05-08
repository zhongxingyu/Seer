 /*
  * The MIT License
  *
  * Copyright (c) 2012 Red Hat, Inc.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.jenkinsci.plugins.externalscheduler;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.hamcrest.Matchers.nullValue;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assume.assumeTrue;
 import hudson.model.Computer;
 import hudson.model.Node;
 import hudson.model.Queue;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.SortedSet;
 
import org.jenkinsci.plugins.externalscheduler.ItemMock;
import org.jenkinsci.plugins.externalscheduler.NodeAssignments;
import org.jenkinsci.plugins.externalscheduler.NodeMockFactory;
import org.jenkinsci.plugins.externalscheduler.RestScheduler;
import org.jenkinsci.plugins.externalscheduler.SchedulerException;
import org.jenkinsci.plugins.externalscheduler.Score;
import org.jenkinsci.plugins.externalscheduler.StateProviderMock;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 /**
  * Run the integration test against server specified by URL_PROPERTY. Skip if missing.
  *
  * @author ogondza
  */
 @RunWith(PowerMockRunner.class)
 @PrepareForTest({Node.class, Queue.BuildableItem.class, Computer.class})
 public class RestSchedulerIntegrationTest {
 
     public static final String URL_PROPERTY = "integration.remote.url";
 
     private final NodeAssignments assignments = NodeAssignments.builder()
             .assign(2, "assigned_solution")
             .build()
     ;
 
     private final NodeMockFactory nodeFactory = new NodeMockFactory();
 
     private final List<Node> nodes = new ArrayList<Node>();
 
     private RestScheduler pp;
 
     @Before
     public void setUp() throws MalformedURLException, InterruptedException, SchedulerException {
 
         final Properties properties = System.getProperties();
 
         assumeTrue(properties.containsKey(URL_PROPERTY));
 
         pp = new RestScheduler(new URL(
                 properties.getProperty(URL_PROPERTY)
         ));
 
         try {
             // Stop if not stopped
             pp.stop();
         } catch (Exception ex) {}
 
         Thread.sleep(2000);
     }
 
     @After
     public void tearDown() throws InterruptedException, SchedulerException {
 
         if (pp == null) return;
 
         Thread.sleep(2000);
         pp.stop();
     }
 
     @Test
     public void getName() throws MalformedURLException {
 
         assertEquals(
                 "hudson-queue-planning: JBQA Jenkins Drools planner",
                 pp.name()
         );
     }
 
     @Test
     public void getSolutionForSimilarItems() throws InterruptedException, SchedulerException {
 
         final List<Queue.BuildableItem> items = ItemMock.list();
 
         final Set<Node> nodeSet = getNodeSet("assigned_solution", 2, 2);
 
         items.add(ItemMock.create(nodeSet, 2, "First", 3));
         items.add(ItemMock.create(nodeSet, 3, "Second", 3));
 
         pp.queue(new StateProviderMock(items, nodes), assignments);
 
         Thread.sleep(1000);
 
         validateScore(pp.score());
 
         NodeAssignments assignments = pp.solution();
 
         assertThat(assignments.nodeName(2), equalTo("assigned_solution"));
         assertThat(assignments.nodeName(3), equalTo("assigned_solution"));
     }
 
     @Test
     public void getSolution() throws InterruptedException, SchedulerException {
 
         final List<Queue.BuildableItem> items = ItemMock.list();
         items.add(getItem(getNodeSet("get_solution", 2, 1)));
 
         pp.queue(new StateProviderMock(items, nodes), assignments);
 
         Thread.sleep(1000);
 
         validateScore(pp.score());
 
         NodeAssignments assignments = pp.solution();
         assertThat(assignments.nodeName(1), nullValue());
         assertThat(assignments.nodeName(2), notNullValue());
     }
 
     @Test
     public void getAssignedSolution() throws InterruptedException, SchedulerException {
 
         final List<Queue.BuildableItem> items = ItemMock.list();
         items.add(getItem(getNodeSet("get_assigned_solution", 2, 1)));
 
         pp.queue(new StateProviderMock(items, nodes), assignments);
 
         Thread.sleep(1000);
 
         validateScore(pp.score());
 
         NodeAssignments assignments = pp.solution();
         assertThat(assignments.nodeName(1), nullValue());
         assertThat(assignments.nodeName(2), equalTo("get_assigned_solution"));
     }
 
     /**
      * Test updating the queue several times and picking up the solution
      * @throws InterruptedException
     * @throws SchedulerException 
      */
     @Test
     public void getReassignedSolution() throws InterruptedException, SchedulerException {
 
         getSolution();
         getAssignedSolution();
         getSolution();
         getAssignedSolution();
     }
 
     @Test
     public void prioritizeOldestBuildOfAJobByTime() throws InterruptedException, SchedulerException {
 
         final Set<Node> master = getNodeSet("master", 1, 1);
 
         assertAssignedOlder(
                 master,
                 ItemMock.create(master, 1, "job", 0),
                 ItemMock.create(master, 2, "job", 1)
         );
     }
 
     @Test
     public void prioritizeOldestBuildOfAJobById() throws InterruptedException, SchedulerException {
 
         final Set<Node> master = getNodeSet("master", 1, 1);
 
         assertAssignedOlder(
                 master,
                 ItemMock.create(master, 1, "job", 0),
                 ItemMock.create(master, 2, "job", 0)
         );
     }
 
     private void assertAssignedOlder(
             final Set<Node> nodes, final Queue.BuildableItem older, final Queue.BuildableItem newer
     ) throws InterruptedException, SchedulerException {
 
         final List<Queue.BuildableItem> items = ItemMock.list();
         items.add(older);
         items.add(newer);
 
         pp.queue(new StateProviderMock(items, new ArrayList<Node> (nodes)), NodeAssignments.empty());
 
         Thread.sleep(1000);
 
         final NodeAssignments assignments = pp.solution();
         assertThat(assignments.nodeName(older), equalTo("master"));
         assertThat(assignments.nodeName(newer), nullValue());
     }
 
     private Queue.BuildableItem getItem(final Set<Node> nodeSet) {
 
         return ItemMock.create(
                 nodeSet, 2, "Single queue item", 3
         );
     }
 
     private SortedSet<Node> getNodeSet(String name, int executors, int freeexecutors) {
 
         final SortedSet<Node> set = nodeFactory.set();
         set.add(nodeFactory.node(name, executors, freeexecutors));
 
         return set;
     }
 
     private void validateScore(final Score score) {
 
         // assertThat(score, greaterThanOrEqualTo(0));
         // assertThat(score, lessThanOrEqualTo(1));
     }
 }
