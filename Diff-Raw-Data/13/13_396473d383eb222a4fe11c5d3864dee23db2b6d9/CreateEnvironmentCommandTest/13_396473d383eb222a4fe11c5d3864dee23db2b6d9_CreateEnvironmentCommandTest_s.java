 package com.peoplemerge.ngds;
 
 /************************************************************************
  ** 
  ** Copyright (C) 2011 Dave Thomas, PeopleMerge. All rights reserved. Contact:
  * opensource@peoplemerge.com.
  ** 
  ** This file is part of the NGDS language.
  ** 
  ** Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  ** 
  ** http://www.apache.org/licenses/LICENSE-2.0
  ** 
  ** Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  ** 
  ** Other Uses Alternatively, this file may be used in accordance with the terms
  * and conditions contained in a signed written agreement between you and the
  * copyright owner.
  ************************************************************************/
 
 import static org.junit.Assert.*;
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 
 import java.io.File;
 import java.net.URL;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.slf4j.Logger;
 
 public class CreateEnvironmentCommandTest {
 
 	//Persistence persistence = mock(Persistence.class);
 	EnvironmentRepository repo = mock(EnvironmentRepository.class);
 	Dispatchable dispatch = mock(Dispatchable.class);
 	NodePool pool = mock(NodePool.class);
 	NamingService namingService = mock(NamingService.class);
 	KickstartServer kickstartServer = mock(KickstartServer.class);
 	Logger logger =  mock(Logger.class);
 
 	// "Create a new environment called development using 1 small nodes from dom0."
 	// Step to figure out where to create the node based on the dom0s in the
 	// MockRepo
 	// Step to use a connection method to connect to the dom0 and issue command
 	// to create the node
 	// Step to add the node to the naming service
 	// Step to add the environment to the repo
 
 	@Test
 	public void createOneNode() throws Exception {
 
 		// TODO the Dom0 abstraction with NodePool should be overloaded or
 		// perhaps rethought since we actually want to get the dom0 host from
 		// the repository state
 		CreateEnvironmentCommand command = buildCommand(1);
 		// so where is the libvirt / etc command encapsulated to hide
 		Step dummyStep = new Step(new ScriptedCommand("dummy"), pool);
 		when(pool.createStep(eq(Node.Type.SMALL), anyString())).thenReturn(
 				dummyStep);
 		ExitCode exitCode = command.execute();
 		assertEquals(ExitCode.SUCCESS, exitCode);
 		verify(pool).createStep(eq(Node.Type.SMALL), anyString());
 		verify(dispatch).dispatch(dummyStep);
 		Environment expected = new Environment("test");
		expected.addNode(new Node("test1", Node.Type.SMALL, pool));
 		verify(repo).save(eq(expected));
 
 	}
 
 	private CreateEnvironmentCommand buildCommand(int numNodes) {
 		CreateEnvironmentCommand.Builder createCommandBuilder = builder(numNodes);
 
 		CreateEnvironmentCommand command = createCommandBuilder.build();
 		dispatchSuccessful();
 		return command;
 	}
 
 	private void dispatchSuccessful() {
 		try {
 			when(dispatch.dispatch(any(Step.class))).thenReturn(ExitCode.SUCCESS);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private CreateEnvironmentCommand.Builder builder(int numNodes) {
 		CreateEnvironmentCommand.Builder createCommandBuilder = new CreateEnvironmentCommand.Builder(
 				"test", repo);
 		createCommandBuilder.withNodes(numNodes, Node.Type.SMALL, pool);
 		// TODO the dispatch method should probably be control-inverted.
 		createCommandBuilder.withDispatch(dispatch);
 		createCommandBuilder.withKickstartServer(kickstartServer);
 		return createCommandBuilder;
 	}
 
 	@Test
 	public void createMultiple() throws Exception {
 
 		CreateEnvironmentCommand command = buildCommand(2);
 		Step dummyStep = new Step(new ScriptedCommand("dummy"), pool);
 		when(pool.createStep(eq(Node.Type.SMALL), anyString())).thenReturn(
 				dummyStep);
 		ExitCode exitCode = command.execute();
 		assertEquals(ExitCode.SUCCESS, exitCode);
 		Environment expected = new Environment("test");
		expected.addNode(new Node("test1", Node.Type.SMALL, pool));
		expected.addNode(new Node("test2", Node.Type.SMALL, pool));
 		verify(repo).save(eq(expected));
 
 	}
 
 	@Test
 	public void addToNameService() throws Exception {
 		CreateEnvironmentCommand command = buildCommand(2);
 		Step dummyStep = new Step(new ScriptedCommand("dummy"), pool);
 		when(pool.createStep(eq(Node.Type.SMALL), anyString())).thenReturn(
 				dummyStep);
 		//when(repo)
 		/*
 		 * need to think more about how this will work outside of a test
 		 * verify(namingService).add("test1", "10.0.1.1");
 		 * verify(namingService).commit();
 		 */
 		// this code exercises the paths of adding to a nameservice but doesn't
 		// test the result.
 
 		ExitCode exitCode = command.execute();
 		assertEquals(ExitCode.SUCCESS, exitCode);
 
 	}
 
 	@Test
 	public void writeKickstartTemplate() throws Exception {
 		// TODO writeKickstartTemplate is really more like an integration test,
 		// consider relocating to another test
 		CreateEnvironmentCommand.Builder builder = builder(1);
 
 		File tempFile = File.createTempFile("test", "ks");
 		tempFile.deleteOnExit();
 		String tempDir = new File(tempFile.getParent()).getAbsolutePath();
 		builder
 				.withKickstartServer(new KickstartServer(tempDir,
 						new NfsMount(),new Puppet(new Node("puppetmaster1", "peoplemerge.com", "192.168.10.137"))));
 		CreateEnvironmentCommand command = builder.build();
 
 		Step dummyStep = new Step(new ScriptedCommand("dummy"), pool);
 		dispatchSuccessful();
 		when(pool.createStep(eq(Node.Type.SMALL), anyString())).thenReturn(
 				dummyStep);
 
 		// when(mockrepo.retrieve("global.kickstartserver")).thenReturn("localhost:/"
 		// + tempDir);
 		ExitCode exitCode = command.execute();
 		assertEquals(ExitCode.SUCCESS, exitCode);
 		File actualKs = new File(tempDir + "/test1.ks");
 		actualKs.deleteOnExit();
 		String actual = FileUtils.readFileToString(actualKs);
 		URL expectedUrl = this.getClass().getClassLoader().getResource(
 				"expected-test1.ks");
 		File expectedKs = new File(expectedUrl.getFile());
 		String expected = FileUtils.readFileToString(expectedKs);
 		assertEquals(expected, actual);
 
 	}
 
 	@Test
 	public void allStepsWriteLogs() {
 		// TODO for all of the major functions of the command, something should
 		// be logged. An abstraction should be developed that captures all of
 		// this. There are currently two abstractions that hold execution and
 		// can capture a log: Executable and Step. Steps that are executed do
 		// contain an output field.
 		CreateEnvironmentCommand.Builder builder = builder(1);
 		builder.withLogger(logger);
 		CreateEnvironmentCommand command = builder.build();
 
 		Step dummyStep = new Step(new ScriptedCommand("dummy"), pool);
 		when(pool.createStep(eq(Node.Type.SMALL), anyString())).thenReturn(
 				dummyStep);
 		command.execute();
 		verify(logger).info("Writing kickstart for test1");
 	}
 
 	@Ignore
 	@Test
 	public void history() {
 		// TODO every time a command is run, a record of the run and all details
 		// may be kept so it can be subsequently viewed. This should persist the
 		// object model that was instantiated, exit codes, and logs. If there
 		// was a script interpreted by antlr, it would be great to persist that,
 		// but how?
 	}
 
 	@Ignore
 	@Test
 	public void libvirt() {
 		// TODO when the znode goes away, the dom0 should wait until the node is
 		// "stopped" then restart it. This is coded but tests need to be
 		// written.
 
 	}
 
 }
