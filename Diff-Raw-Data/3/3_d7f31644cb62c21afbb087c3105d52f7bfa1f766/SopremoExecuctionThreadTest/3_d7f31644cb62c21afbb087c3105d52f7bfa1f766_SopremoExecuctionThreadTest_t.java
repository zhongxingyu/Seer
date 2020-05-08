 /***********************************************************************************************************************
  *
  * Copyright (C) 2010-2013 by the Stratosphere project (http://stratosphere.eu)
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  *
  **********************************************************************************************************************/
 package eu.stratosphere.sopremo.server;
 
 import static org.powermock.api.mockito.PowerMockito.mock;
 import static org.powermock.api.mockito.PowerMockito.when;
 import static org.powermock.api.mockito.PowerMockito.whenNew;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.HashMap;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import eu.stratosphere.api.common.Plan;
 import eu.stratosphere.configuration.Configuration;
 import eu.stratosphere.nephele.client.JobClient;
 import eu.stratosphere.nephele.client.JobExecutionException;
 import eu.stratosphere.nephele.client.JobExecutionResult;
 import eu.stratosphere.nephele.jobgraph.JobGraph;
 import eu.stratosphere.sopremo.base.Selection;
 import eu.stratosphere.sopremo.execution.ExecutionRequest;
 import eu.stratosphere.sopremo.execution.ExecutionRequest.ExecutionMode;
 import eu.stratosphere.sopremo.execution.ExecutionResponse.ExecutionState;
 import eu.stratosphere.sopremo.execution.SopremoID;
 import eu.stratosphere.sopremo.expressions.ComparativeExpression;
 import eu.stratosphere.sopremo.expressions.ComparativeExpression.BinaryOperator;
 import eu.stratosphere.sopremo.expressions.ConstantExpression;
 import eu.stratosphere.sopremo.expressions.OrExpression;
 import eu.stratosphere.sopremo.expressions.UnaryExpression;
 import eu.stratosphere.sopremo.io.Sink;
 import eu.stratosphere.sopremo.io.Source;
 import eu.stratosphere.sopremo.operator.SopremoPlan;
 import eu.stratosphere.sopremo.testing.SopremoTestUtil;
 import eu.stratosphere.sopremo.type.JsonUtil;
 import static org.mockito.Matchers.*;
 
 /**
  */
 @RunWith(PowerMockRunner.class)
 @PrepareForTest({ SopremoExecutionThread.class, JobClient.class })
 public class SopremoExecuctionThreadTest {
 
 	private SopremoJobInfo jobInfo;
 
 	private JobClient mockClient;
 
 	private SopremoExecutionThread thread;
 
 	static SopremoPlan createPlan() {
 		final SopremoPlan plan = new SopremoPlan();
 		final Source input = new Source(SopremoTestUtil.createTemporaryFile("input"));
 		final Selection selection = new Selection().
 			withCondition(
 				new OrExpression(
 					new UnaryExpression(JsonUtil.createPath("0", "mgr")),
 					new ComparativeExpression(JsonUtil.createPath("0", "income"), BinaryOperator.GREATER,
 						new ConstantExpression(30000)))).
 			withInputs(input);
 		final Sink output = new Sink(SopremoTestUtil.createTemporaryFile("output")).withInputs(selection);
 		plan.setSinks(output);
 		return plan;
 	}
 
 	@Before
 	public void setup() throws Exception {
 		this.jobInfo = new SopremoJobInfo(SopremoID.generate(),
 			new ExecutionRequest(createPlan()), new Configuration());
 		this.thread = new SopremoExecutionThread(this.jobInfo, new InetSocketAddress(0)) {
 			/*
 			 * (non-Javadoc)
 			 * @see
 			 * eu.stratosphere.sopremo.server.SopremoExecutionThread#getJobGraph(eu.stratosphere.api.plan.Plan)
 			 */
 			@Override
 			JobGraph getJobGraph(final Plan pactPlan) {
 				return new JobGraph();
 			}
 		};
 
 		this.mockClient = mock(JobClient.class);
 		whenNew(JobClient.class).withArguments(any(), any(), any()).thenReturn(this.mockClient);
		whenNew(JobClient.class).withArguments(any(), any()).thenReturn(this.mockClient);
 	}
 
 	@Test
 	public void testSuccessfulExecution() throws Exception {
 		when(this.mockClient.submitJobAndWait()).thenReturn(new JobExecutionResult(1, new HashMap<String, Object>()));
 
 		this.thread.run();
 		Assert.assertSame(this.jobInfo.getDetail(), ExecutionState.FINISHED, this.jobInfo.getStatus());
 		Assert.assertSame("", this.jobInfo.getDetail());
 	}
 
 	@Test
 	public void testSuccessfulExecutionWithStatistics() throws Exception {
 		this.jobInfo.getInitialRequest().setMode(ExecutionMode.RUN_WITH_STATISTICS);
 		when(this.mockClient.submitJobAndWait()).thenReturn(new JobExecutionResult(1, new HashMap<String, Object>()));
 
 		this.thread.run();
 		Assert.assertSame(this.jobInfo.getDetail(), ExecutionState.FINISHED, this.jobInfo.getStatus());
 		Assert.assertNotSame("", this.jobInfo.getDetail());
 	}
 
 	@Test
 	public void testFailBeforeRunning() throws Exception {
 		whenNew(JobClient.class).withArguments(any(), any(), any()).thenThrow(new IOException("io"));
		whenNew(JobClient.class).withArguments(any(), any()).thenThrow(new IOException("io"));
 
 		this.thread.run();
 		Assert.assertSame(ExecutionState.ERROR, this.jobInfo.getStatus());
 		Assert.assertNotSame("", this.jobInfo.getDetail());
 	}
 
 	@Test
 	public void testFailDuringRun() throws Exception {
 		when(this.mockClient.submitJobAndWait()).thenThrow(new JobExecutionException("jee", false));
 
 		this.thread.run();
 		Assert.assertSame(ExecutionState.ERROR, this.jobInfo.getStatus());
 		Assert.assertNotSame("", this.jobInfo.getDetail());
 	}
 
 }
