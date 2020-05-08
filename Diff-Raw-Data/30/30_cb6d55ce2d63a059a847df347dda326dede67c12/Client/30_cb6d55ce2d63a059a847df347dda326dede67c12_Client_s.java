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
 package br.com.caelum.integracao.server;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.UnknownHostException;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 
 import org.apache.commons.httpclient.HttpException;
 
import br.com.caelum.integracao.http.Http;
 import br.com.caelum.integracao.http.Method;
 import br.com.caelum.integracao.server.action.Dispatcher;
 
 /**
  * Represents a client machine.
  * 
  * @author guilherme silveira
  */
 @Entity
 public class Client {
 
 	@Id
 	@GeneratedValue
 	private Long id;
 
 	private int port;
 
 	private String context;
 
 	private String host;
 
 	private String reason;
 	
 	private boolean busy;
 
 	private boolean active;
 
 	public String getBaseUri() {
 		return "http://" + this.getHost() + ":" + this.getPort() + this.getContext();
 	}
 
 	public Dispatcher getConnection(File logFile, String myUrl) throws UnknownHostException, IOException {
 		return new Dispatcher(this, logFile, myUrl);
 	}
 
 	public String getContext() {
 		return context;
 	}
 
 	public String getCurrentJob() {
 		return reason;
 	}
 
 	public String getHost() {
 		return host;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public int getPort() {
 		return port;
 	}
 
 	public void setContext(String context) {
 		this.context = context;
 	}
 
 	public void setCurrentJob(String reason) {
 		this.reason = reason;
 	}
 
 	public void setHost(String host) {
 		this.host = host;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public void setPort(int port) {
 		this.port = port;
 	}
 
 	public void setBusy(boolean busy) {
 		this.busy = busy;
 	}
 
 	public boolean isBusy() {
 		return busy;
 	}
 
 	public void work(String job) {
 		this.busy = true;
 		this.reason = job;
 	}
 
 	public void leaveJob() {
 		this.busy = false;
 		this.reason = "";
 	}
 
 	public void deactivate() {
 		this.active = false;
 	}
 	
 	public void activate() {
 		this.active = true;
 	}
 
 	public boolean isAlive() {
		Method post = new Http().post(getBaseUri() + "/job/current");
 		try {
 			post.send();
 		} catch (HttpException e) {
 			return false;
 		} catch (IOException e) {
 			return false;
 		}
 		if(post.getResult()==410) {
 			leaveJob();
 			return true;
 		}
 		if(post.getResult()!=200) {
 			return false;
 		}
 		return true;
 	}
 
 }
