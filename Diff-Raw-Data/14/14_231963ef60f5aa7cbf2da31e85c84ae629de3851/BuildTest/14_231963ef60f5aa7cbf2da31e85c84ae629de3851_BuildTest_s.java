 /***
  * 
  * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 1. Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer. 2. Redistributions in
  * binary form must reproduce the above copyright notice, this list of
  * conditions and the following disclaimer in the documentation and/or other
  * materials provided with the distribution. 3. Neither the name of the
  * copyright holders nor the names of its contributors may be used to endorse or
  * promote products derived from this software without specific prior written
  * permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package br.com.caelum.integracao.server.project;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.is;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jmock.Expectations;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import br.com.caelum.integracao.server.Application;
 import br.com.caelum.integracao.server.Build;
 import br.com.caelum.integracao.server.Phase;
 import br.com.caelum.integracao.server.Project;
 import br.com.caelum.integracao.server.plugin.Plugin;
 import br.com.caelum.integracao.server.plugin.PluginToRun;
 import br.com.caelum.integracao.server.queue.Jobs;
 import br.com.caelum.integracao.server.scm.ScmControl;
 import br.com.caelum.integracao.server.scm.ScmException;
 
 public class BuildTest extends DatabaseBasedTest {
 
 	private Project project;
 	private List<Phase> phases;
 	private ScmControl control;
 	private Phase first;
 	private Phase second;
 	private Application app;
 	private ArrayList<PluginToRun> plugins;
 	private Jobs jobs;
 
 	@Before
 	public void configProject() throws ScmException {
 		this.jobs = mockery.mock(Jobs.class);
 		this.project = mockery.mock(Project.class);
 		this.phases = new ArrayList<Phase>();
 		this.control = mockery.mock(ScmControl.class, "control");
 		this.first = mockery.mock(Phase.class, "first");
 		this.second = mockery.mock(Phase.class, "second");
 		this.plugins = new ArrayList<PluginToRun>();
 		mockery.checking(new Expectations() {
 			{
 				allowing(project).getBaseDir();
 				will(returnValue(baseDir));
 				allowing(project).getControl();
 				will(returnValue(control));
 				allowing(project).getPhases();
 				will(returnValue(phases));
 				allowing(project).getPlugins();
 				will(returnValue(plugins));
 			}
 		});
 		this.app = mockery.mock(Application.class);
 	}
 
 	@Test
 	public void createsABuildWithCorrectBuildCount() {
 		mockery.checking(new Expectations() {
 			{
 				one(project).nextBuild();
 				will(returnValue(3L));
 				one(project).getBuildsDirectory();
 				will(returnValue(baseDir));
 			}
 		});
 		Build build = new Build(project);
 		assertThat(build.getBuildCount(), is(equalTo(3L)));
 		File dir = new File(baseDir, "build-3");
 		assertThat(dir.exists(), is(equalTo(true)));
 		mockery.assertIsSatisfied();
 	}
 
 	@Test
 	public void createsABuildWithCorrectCheckoutInfo() throws IllegalArgumentException, SecurityException,
 			InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
 			IOException, ScmException {
 		mockery.checking(new Expectations() {
 			{
 				one(project).nextBuild();
 				will(returnValue(3L));
 				allowing(project).getName();
 				will(returnValue("my-horses"));
 				one(control).checkoutOrUpdate((File) with(an(File.class)));
 				one(control).getRevision(tmpFile());
 				will(returnValue("my-revision"));
 				allowing(project).getBuildsDirectory();
 				will(returnValue(baseDir));
 			}
 		});
 		Build build = new Build(project);
 		build.start(jobs, database);
 		assertThat(build.getRevision(), is(equalTo("my-revision")));
 		File checkout = new File(baseDir, "build-3/checkout.txt");
 		assertThat(checkout.exists(), is(equalTo(true)));
 		mockery.assertIsSatisfied();
 	}
 
 	@Test
 	public void keepsTheCurrentPhaseAndInvokesRunAfterOnPluginsIfNoSuccess() throws InstantiationException,
 			IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException, ScmException {
 		phases.add(first);
 		phases.add(second);
 		mockery.checking(new Expectations() {
 			{
 				one(project).nextBuild();
 				will(returnValue(3L));
 				allowing(project).getName();
 				will(returnValue("my-horses"));
 				one(control).checkoutOrUpdate((File) with(an(File.class)));
 				one(control).getRevision(tmpFile());
 				will(returnValue("my-revision"));
 				allowing(project).getBuildsDirectory();
 				will(returnValue(baseDir));
 				one(first).getCommandCount();
 				will(returnValue(1));
 			}
 		});
 		final Build build = new Build(project);
 		mockery.checking(new Expectations() {
 			{
 				one(first).execute(build, jobs);
 				one(first).runAfter(build, database);
 				will(returnValue(true));
 			}
 		});
 		build.start(jobs, database);
 		assertThat(build.getCurrentPhase(), is(equalTo(0)));
		build.proceed("",0, 0, "no-result", false, app, database,jobs);
 		assertThat(build.getCurrentPhase(), is(equalTo(0)));
 		mockery.assertIsSatisfied();
 	}
 
 	@Test
 	public void keepsTheCurrentPhaseIfSuccessButPluginsFail() throws InstantiationException, IllegalAccessException,
 			InvocationTargetException, NoSuchMethodException, IOException, ScmException {
 		phases.add(first);
 		phases.add(second);
 		mockery.checking(new Expectations() {
 			{
 				one(project).nextBuild();
 				will(returnValue(3L));
 				allowing(project).getName();
 				will(returnValue("my-horses"));
 				one(control).checkoutOrUpdate((File) with(an(File.class)));
 				one(control).getRevision(tmpFile());
 				will(returnValue("my-revision"));
 				allowing(project).getBuildsDirectory();
 				will(returnValue(baseDir));
 				one(first).getCommandCount();
 				will(returnValue(1));
 			}
 		});
 		final Build build = new Build(project);
 		mockery.checking(new Expectations() {
 			{
 				one(first).execute(build, jobs);
 				one(first).runAfter(build, database);
 				will(returnValue(false));
 			}
 		});
 		build.start(jobs, database);
 		assertThat(build.getCurrentPhase(), is(equalTo(0)));
		build.proceed("",0, 0, "no-result", false, app, database, jobs);
 		assertThat(build.getCurrentPhase(), is(equalTo(0)));
 		mockery.assertIsSatisfied();
 	}
 
 	@Test
 	public void keepsTheCurrentPhaseIfThereAreStillCommandsToDo() throws InstantiationException,
 			IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException, ScmException {
 		phases.add(first);
 		phases.add(second);
 		mockery.checking(new Expectations() {
 			{
 				one(project).nextBuild();
 				will(returnValue(3L));
 				allowing(project).getName();
 				will(returnValue("my-horses"));
 				one(control).checkoutOrUpdate((File) with(an(File.class)));
 				one(control).getRevision(tmpFile());
 				will(returnValue("my-revision"));
 				allowing(project).getBuildsDirectory();
 				will(returnValue(baseDir));
 				one(first).getCommandCount();
 				will(returnValue(2));
 			}
 		});
 		final Build build = new Build(project);
 		mockery.checking(new Expectations() {
 			{
 				one(first).execute(build, jobs);
 			}
 		});
 		build.start(jobs, database);
 		assertThat(build.getCurrentPhase(), is(equalTo(0)));
		build.proceed("",0, 0, "no-result", true, app, database, jobs);
 		assertThat(build.getCurrentPhase(), is(equalTo(0)));
 		mockery.assertIsSatisfied();
 	}
 
 	@Test
 	public void changesThePhaseWhenAllCommandsWereExecuted() throws InstantiationException, IllegalAccessException,
 			InvocationTargetException, NoSuchMethodException, IOException, ScmException {
 		phases.add(first);
 		phases.add(second);
 		mockery.checking(new Expectations() {
 			{
 				one(project).nextBuild();
 				will(returnValue(3L));
 				allowing(project).getName();
 				will(returnValue("my-horses"));
 				one(control).checkoutOrUpdate((File) with(an(File.class)));
 				one(control).getRevision(tmpFile());
 				will(returnValue("my-revision"));
 				allowing(project).getBuildsDirectory();
 				will(returnValue(baseDir));
 				one(first).getCommandCount();
 				will(returnValue(1));
 			}
 		});
 		final Build build = new Build(project);
 		mockery.checking(new Expectations() {
 			{
 				one(first).execute(build, jobs);
 				one(second).execute(build, jobs);
 				one(first).runAfter(build, database);
 				will(returnValue(true));
 			}
 		});
 		build.start(jobs, database);
	assertThat(build.getCurrentPhase(), is(equalTo(0)));
		build.proceed("",0, 0, "no-result", true, app, database, jobs);
 		assertThat(build.getCurrentPhase(), is(equalTo(1)));
 		mockery.assertIsSatisfied();
 	}
 
 	@Test
 	public void shouldNotStartPhaseIfResultIsZero() {
 		Assert.fail("not yet implemented");
 	}
 
 	@Test
 	public void removeShouldEmptyItsDirectory() throws IOException {
 		mockery.checking(new Expectations() {
 			{
 				one(project).nextBuild();
 				will(returnValue(3L));
 				allowing(project).getBuildsDirectory();
 				will(returnValue(baseDir));
 			}
 		});
 		File buildDir = new File(baseDir, "build-3");
 		buildDir.mkdirs();
 		File file = new File(buildDir, "custom");
 		givenA(file, "custom-file-content");
 		Build build = new Build(project);
 		build.remove(database);
 		assertThat(file.exists(), is(equalTo(false)));
 		assertThat(buildDir.exists(), is(equalTo(false)));
 		mockery.assertIsSatisfied();
 	}
 
 	@Test
 	public void invokesTheBeforeMethodOfAPlugin() throws InstantiationException, IllegalAccessException,
 			InvocationTargetException, NoSuchMethodException, IOException, ScmException {
 		phases.add(first);
 		final PluginToRun firstPlugin = mockery.mock(PluginToRun.class, "firstPlugin");
 		final Plugin firstImplementation = mockery.mock(Plugin.class, "firstImplementation");
 		mockery.checking(new Expectations() {
 			{
 				one(project).nextBuild();
 				will(returnValue(3L));
 				allowing(project).getName();
 				will(returnValue("my-horses"));
 				one(control).checkoutOrUpdate((File) with(an(File.class)));
 				one(control).getRevision(tmpFile());
 				will(returnValue("my-revision"));
 				allowing(project).getBuildsDirectory();
 				will(returnValue(baseDir));
 				one(firstPlugin).getPlugin(database);
 				will(returnValue(firstImplementation));
 			}
 		});
 		this.plugins.add(firstPlugin);
 		final Build build = new Build(project);
 		mockery.checking(new Expectations() {
 			{
 				one(firstImplementation).before(build); will(returnValue(true));
 				one(first).execute(build, jobs);
 			}
 		});
 		build.start(jobs, database);
 		mockery.assertIsSatisfied();
 	}
 
 	@Test
 	public void doesntContinueTheStartProcessIfAPluginTellsItToStop() throws InstantiationException,
 			IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException, ScmException {
 		phases.add(first);
 		final PluginToRun firstPlugin = mockery.mock(PluginToRun.class, "firstPlugin");
 		final Plugin firstImplementation = mockery.mock(Plugin.class, "firstImplementation");
 		final PluginToRun secondPlugin = mockery.mock(PluginToRun.class, "secondPlugin");
 		this.plugins.add(firstPlugin);
 		this.plugins.add(secondPlugin);
 		mockery.checking(new Expectations() {
 			{
 				one(project).nextBuild();
 				will(returnValue(3L));
 				allowing(project).getName();
 				will(returnValue("my-horses"));
 				one(control).checkoutOrUpdate((File) with(an(File.class)));
 				one(control).getRevision(tmpFile());
 				will(returnValue("my-revision"));
 				allowing(project).getBuildsDirectory();
 				will(returnValue(baseDir));
 				one(firstPlugin).getPlugin(database);
 				will(returnValue(firstImplementation));
 				allowing(firstPlugin).getType();will(returnValue(BuildTest.class));
 			}
 		});
 		final Build build = new Build(project);
 		mockery.checking(new Expectations() {
 			{
 				one(firstImplementation).before(build); will(returnValue(false));
 			}
 		});
 		build.start(jobs, database);
 		assertThat(build.isFinished(), is(equalTo(true)));
 		assertThat(build.isSuccessSoFar(), is(equalTo(false)));
 		mockery.assertIsSatisfied();
 	}
 
 }
