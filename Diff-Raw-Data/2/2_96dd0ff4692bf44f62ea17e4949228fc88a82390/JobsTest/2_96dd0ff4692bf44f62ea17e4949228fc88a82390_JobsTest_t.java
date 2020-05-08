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
 package br.com.caelum.integracao.server.queue;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.is;
 
 import java.net.UnknownHostException;
 
 import org.jmock.Expectations;
 import org.junit.Before;
 import org.junit.Test;
 
 import br.com.caelum.integracao.server.Build;
 import br.com.caelum.integracao.server.Client;
 import br.com.caelum.integracao.server.Config;
 import br.com.caelum.integracao.server.Project;
 import br.com.caelum.integracao.server.agent.Agent;
 import br.com.caelum.integracao.server.agent.Clients;
 import br.com.caelum.integracao.server.project.DatabaseBasedTest;
 
 public class JobsTest extends DatabaseBasedTest {
 
 	private Jobs jobs;
 	private Job running;
 	private Job finished;
 	private Job notStarted;
 	private Job startedTooManyTimes;
 
 	@Before
 	public void insertData() throws UnknownHostException {
 		this.jobs = new Jobs(database);
 		Clients clients = new Clients(database);
 
 		Client busy = new Client();
 		clients.register(busy);
 
 		this.running = new Job(null, null);
 		running.useClient(busy);
 		jobs.add(running);
 
 		this.finished = new Job(null, null);
 		finished.useClient(busy);
 		finished.setFinished(true);
 		jobs.add(finished);
 
 		this.notStarted = new Job(null, null);
 		jobs.add(notStarted);
 
 		final Build build = mockery.mock(Build.class);
 		this.startedTooManyTimes = new Job(build, null);
 		final Client client = mockery.mock(Client.class);
 		final Agent agent = mockery.mock(Agent.class);
 		final Project project = mockery.mock(Project.class);
 		final Config config = mockery.mock(Config.class);
 		mockery.checking(new Expectations() {
 			{
 				allowing(client).getAgent();
 				will(returnValue(agent));
 				allowing(agent).register(project);
 				will(returnValue(true));
 				allowing(config).getUrl(); will(returnValue("uri"));
				allowing(build).getProject(); will(returnValue(project));
 			}
 		});
 		for (int i = 0; i < 3; i++) {
 			mockery.checking(new Expectations() {
 				{
 					allowing(agent).execute(startedTooManyTimes, "uri", build);
 				}
 			});
 			this.startedTooManyTimes.executeAt(client, config);
 		}
 
 		database.getSession().flush();
 	}
 
 	@Test
 	public void shouldParseWellJobsRunning() {
 
 		assertThat(jobs.runningJobs().contains(running), is(equalTo(true)));
 		assertThat(jobs.runningJobs().contains(finished), is(equalTo(false)));
 		assertThat(jobs.runningJobs().contains(notStarted), is(equalTo(false)));
 	}
 
 	@Test
 	public void shouldParseWellNotYetStartedJobs() {
 
 		assertThat(jobs.todo().contains(running), is(equalTo(false)));
 		assertThat(jobs.todo().contains(finished), is(equalTo(false)));
 		assertThat(jobs.todo().contains(notStarted), is(equalTo(true)));
 		assertThat(jobs.todo().contains(startedTooManyTimes), is(equalTo(false)));
 	}
 
 }
