 /**
  * Copyright (c) 2013 SAP
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the SAP nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL SAP BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.sopeco.webui.client.helper.push;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.sopeco.webui.shared.push.PushDomain;
 import org.sopeco.webui.shared.push.PushPackage;
 import org.sopeco.webui.shared.rpc.PushRPC;
 import org.sopeco.webui.shared.rpc.PushRPCAsync;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 /**
  * 
  * @author Marius Oehler
  * 
  */
 public final class ServerPush implements AsyncCallback<List<PushPackage>> {
 
 	private static final Logger LOGGER = Logger.getLogger(ServerPush.class.getName());
 
 	private static ServerPush singleton;
 
 	private static PushRPCAsync pushRPC = GWT.create(PushRPC.class);
 	private boolean waiting = false;
 	private boolean running = false;
 
 	private Map<PushDomain, List<PushListener>> pushListenerMap;
 
 	public synchronized static void start() {
 		if (singleton == null) {
 			singleton = new ServerPush();
 		}
 		singleton.running = true;
 		singleton.sendRequest();
 	}
 
 	public synchronized static void stop() {
 		if (singleton == null) {
 			return;
 		}
 		singleton.running = false;
 	}
 
 	public static void registerListener(PushDomain domain, PushListener listener) {
 		singleton.register(domain, listener);
 	}
 
 	public static void removeListener(PushDomain domain, PushListener listener) {
 		singleton.remove(domain, listener);
 	}
 
 	public static void clear() {
 		singleton.pushListenerMap = new HashMap<PushDomain, List<PushListener>>();
 	}
 
 	private ServerPush() {
 		pushListenerMap = new HashMap<PushDomain, List<PushListener>>();
 	}
 
 	private void register(PushDomain domain, PushListener listener) {
 		LOGGER.info("Register pushListener on domain " + domain);
 		if (!pushListenerMap.containsKey(domain)) {
 			pushListenerMap.put(domain, new ArrayList<PushListener>());
 		}
 		pushListenerMap.get(domain).add(listener);
 	}
 
 	private void remove(PushDomain domain, PushListener listener) {
 		if (!pushListenerMap.containsKey(domain)) {
 			return;
 		}
 		pushListenerMap.get(domain).remove(listener);
 	}
 
 	@Override
 	public void onFailure(Throwable caught) {
 		throw new RuntimeException(caught);
 	}
 
 	@Override
 	public void onSuccess(List<PushPackage> result) {
 		for (PushPackage p : result) {
 			if (p.getDomain() == null) {
 				continue;
 			}
 
 			if (pushListenerMap.containsKey(p.getDomain())) {
 				for (PushListener listener : pushListenerMap.get(p.getDomain())) {
 					listener.receive(p);
 				}
 			} else {
 				LOGGER.info("No PushListener for domain " + p.getDomain());
 			}
 		}
 
 		waiting = false;
 		sendRequest();
 	}
 
	private synchronized void sendRequest() {
 		if (waiting || !running) {
 			return;
 		} else {
 			waiting = true;
 		}
 		pushRPC.push(this);
 	}
 
 }
