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
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.jmock.Expectations;
 import org.junit.Before;
 import org.junit.Test;
 
 import br.com.caelum.integracao.server.Client;
 import br.com.caelum.integracao.server.Clients;
 import br.com.caelum.integracao.server.Config;
 import br.com.caelum.integracao.server.project.BaseTest;
 
 public class DefaultJobQueueTest extends BaseTest{
 	
 	private Jobs jobs;
 	private DefaultJobQueue queue;
 	private Job first;
 	private Clients clients;
 	private Client firstClient;
 	private Config config;
 
 	@Before
 	public void config() {
 		this.jobs = mockery.mock(Jobs.class);
 		this.clients = mockery.mock(Clients.class);
 		this.config = mockery.mock(Config.class);
 		this.queue = new DefaultJobQueue(jobs, clients, config);
 		this.first = mockery.mock(Job.class, "first");
 		this.firstClient = mockery.mock(Client.class, "firstClient");
 	}
 	
 	@Test
 	public void shouldDoNothingIfThereIsNoJob() {
 		mockery.checking(new Expectations() {{
 			one(jobs).todo(); will(returnValue(new ArrayList<Client>()));
 			one(clients).freeClients(); will(returnValue(new ArrayList<Client>()));
 		}});
 		assertThat(queue.iterate(), is(equalTo(0)));
 	}
 
 	@Test
 	public void shouldNotExecuteJobIfThereAreNotEnoughClients() {
 		mockery.checking(new Expectations() {{
 			one(jobs).todo(); will(returnValue(Arrays.asList(first)));
 			one(clients).freeClients(); will(returnValue(new ArrayList<Client>()));
 		}});
 		assertThat(queue.iterate(), is(equalTo(0)));
 	}
 
 
 	@Test
 	public void shouldExecuteJobIfThereAreEnoughClients() {
 		mockery.checking(new Expectations() {{
 			one(jobs).todo(); will(returnValue(Arrays.asList(first)));
 			one(clients).freeClients(); will(returnValue(Arrays.asList(firstClient)));
 			one(firstClient).work(first, config); will(returnValue(true));
			one(firstClient).isAlive(); will(returnValue(true));
 		}});
 		assertThat(queue.iterate(), is(equalTo(1)));
 	}
 
 	@Test
 	public void shouldNotExecuteJobIfThereTheClientRefusesToDoIt() {
 		mockery.checking(new Expectations() {{
 			one(jobs).todo(); will(returnValue(Arrays.asList(first)));
 			one(clients).freeClients(); will(returnValue(Arrays.asList(firstClient)));
 			one(firstClient).work(first, config); will(returnValue(false));
			one(firstClient).isAlive(); will(returnValue(true));
 		}});
 		assertThat(queue.iterate(), is(equalTo(0)));
 	}
 
 }
