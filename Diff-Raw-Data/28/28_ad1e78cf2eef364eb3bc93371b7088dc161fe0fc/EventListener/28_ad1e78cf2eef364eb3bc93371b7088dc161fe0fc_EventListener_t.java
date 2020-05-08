 /*
  * Copyright (c) 2010 Eugene Prokopiev <enp@itx.ru>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package ru.itx.ccm.beans;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Node;
 import org.freeswitch.esl.client.IEslEventListener;
 import org.freeswitch.esl.client.inbound.Client;
 import org.freeswitch.esl.client.transport.event.EslEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 public class EventListener {
 
 	private Logger logger = LoggerFactory.getLogger(getClass());
 	private EventManager eventManager;
 	private Client client = new Client();
 	private List<String> users = new ArrayList<String>();
 
 	private String host;
 	private int port;
 	private String password;
 	private String domain;
	private String profile;
 
 	public void setHost(String host) {
 		this.host = host;
 	}
 
 	public void setPort(int port) {
 		this.port = port;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public void setDomain(String domain) {
 		this.domain = domain;
 	}
 
	public void setProfile(String profile) {
		this.profile = profile;
	}

 	public void setEventManager(EventManager eventManager) {
 		this.eventManager = eventManager;
 	}
 
 	public void init() throws Exception {
 		logger.debug("init");
 		client.addEventListener(new IEslEventListener() {
 			public void eventReceived(EslEvent event) {
 				if (event.getEventName().equals("CUSTOM")) {
 					Map<String,String> headers = event.getEventHeaders();
 					String subclass = headers.get("Event-Subclass");
 					if (subclass.equals("sofia::register")) {
 						eventManager.connectSession(
 							headers.get("call-id"),
 							headers.get("from-user"),
 							headers.get("user-agent"),
 							headers.get("network-ip"));
						client.sendAsyncApiCommand("sofia", "xmlstatus profile "+profile);
 					} else if (subclass.equals("sofia::unregister")) {
 						eventManager.disconnectSession(headers.get("call-id"));
						client.sendAsyncApiCommand("sofia", "xmlstatus profile "+profile);
 					}else if (subclass.contains("fifo::")) {
 						String action = event.getEventHeaders().get("FIFO-Action");
 						if (action.equals("push")) {
 							eventManager.connectCall(
 								headers.get("Unique-ID"),
 								headers.get("Caller-Caller-ID-Number"),
 								headers.get("Caller-RDNIS"),
 								headers.get("FIFO-Name"));
 							client.sendAsyncApiCommand("fifo", "list_verbose "+headers.get("FIFO-Name"));
 						} else if (action.equals("bridge-caller-start")) {
 							eventManager.answerCall(
 								headers.get("Unique-ID"),
 								headers.get("Other-Leg-Callee-ID-Name"));
 							client.sendAsyncApiCommand("fifo", "list_verbose "+headers.get("FIFO-Name"));
 						} else if (action.equals("bridge-caller-stop")) {
 							eventManager.hangupCall(headers.get("Unique-ID"));
 							client.sendAsyncApiCommand("fifo", "list_verbose "+headers.get("FIFO-Name"));
 						} else if (action.equals("abort")) {
 							eventManager.abortCall(headers.get("Unique-ID"));
 							client.sendAsyncApiCommand("fifo", "list_verbose "+headers.get("FIFO-Name"));
 						} else if (action.equals("post-dial") && !headers.get("result").equals("success")) {
 							eventManager.failCall(
 								headers.get("caller-uuid"),
 								headers.get("originate_string").split("/")[1].split("@")[0],
 								headers.get("cause"));
 						}
 					}
 				}
 			}
 			public void backgroundJobResultReceived(EslEvent event) {
 				StringBuilder eventBody = new StringBuilder("");
 				for (String eventBodyLine : event.getEventBodyLines())
 					eventBody.append(eventBodyLine);
 				try {
 					Document document = DocumentHelper.parseText(eventBody.toString());
 					String command =
 						event.getEventHeaders().get("Job-Command")+" "+
 						event.getEventHeaders().get("Job-Command-Arg");
					if (command.equals("sofia xmlstatus profile "+profile))
 						processPresence(document);
 					if (command.startsWith("fifo list_verbose"))
 						processFifo(document);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		client.connect(host, port, password, 2);
		client.sendAsyncApiCommand("sofia", "xmlstatus profile "+profile);
 		client.setEventSubscriptions("plain", "all");
 	}
 
 	private void processPresence(Document document) {
 		users.clear();
 		for(Node node : (List<Node>)document.selectNodes("/profile/registrations/registration/user"))
 			users.add(node.getText());
 	}
 
 	private void processFifo(Document document) {
 		String fifo = document.selectSingleNode("/fifo_report/fifo").valueOf("@name");
 		List<Node> members = document.selectNodes("/fifo_report/fifo/outbound/member");
 		List<String> activeMembers = new ArrayList<String>();
 		for (Node node : members) {
 			String user = node.getText().replace("user/","");
 			if (users.contains(user))
 				activeMembers.add(user.replace("@"+domain,""));
 		}
 		List<Node> callers = document.selectNodes("/fifo_report/fifo/callers/caller");
 		List<Node> bridges = document.selectNodes("/fifo_report/fifo/bridges/bridge");
 		eventManager.count(fifo, members.size(), activeMembers.size(), callers.size(), bridges.size());
 	}
 
 	public void destroy() {
 		client.close();
 		logger.debug("destroy");
 	}
 }
