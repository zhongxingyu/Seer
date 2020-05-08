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
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.XMLWriter;
 import org.freeswitch.esl.client.IEslEventListener;
 import org.freeswitch.esl.client.inbound.Client;
 import org.freeswitch.esl.client.transport.event.EslEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class EventListener {
 
 	private Logger logger = LoggerFactory.getLogger(getClass());
 	private EventManager eventManager;
 	private Client client = new Client();
 	private Map<String,String> users = new HashMap<String,String>();
 
 	private String host;
 	private int port;
 	private String password;
 	private String domain;
 	private String profile;
 	private String jobsDirectory;
 	private String fifoDirectory;
 
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
 
 	public void setJobsDirectory(String jobsDirectory) {
 		this.jobsDirectory = jobsDirectory;
 		createDirectory(jobsDirectory);
 	}
 
 	public void setFifoDirectory(String fifoDirectory) {
 		this.fifoDirectory = fifoDirectory;
 		createDirectory(fifoDirectory);
 	}
 
 	private void createDirectory(String jobsDirectory) {
 		if (jobsDirectory != null && !jobsDirectory.isEmpty())
 			new File(jobsDirectory).mkdir();
 	}
 
 	public void setEventManager(EventManager eventManager) {
 		this.eventManager = eventManager;
 	}
 
 	public void init() throws Exception {
 		logger.debug("init");
 		client.addEventListener(new IEslEventListener() {
 			public void eventReceived(EslEvent event) {
 				if (event.getEventName().equals("CUSTOM")) {
 					Map<String, String> headers = event.getEventHeaders();
 					String subclass = headers.get("Event-Subclass");
 					if (subclass.equals("sofia::register")) {
 						eventManager.connectSession(
 							headers.get("call-id"),
 							headers.get("from-user"),
 							headers.get("user-agent"),
 							headers.get("network-ip"));
 						client.sendAsyncApiCommand("sofia", "xmlstatus profile " + profile);
 					} else if (subclass.equals("sofia::unregister")) {
 						eventManager.disconnectSession(headers.get("call-id"));
 						client.sendAsyncApiCommand("sofia", "xmlstatus profile " + profile);
 					} else if (subclass.contains("fifo::")) {
 						String action = event.getEventHeaders().get("FIFO-Action");
 						if (action.equals("push")) {
 							eventManager.connectCall(
 								headers.get("Unique-ID"),
 								headers.get("Caller-Caller-ID-Number"),
 								headers.get("Caller-RDNIS"),
 								headers.get("FIFO-Name"));
 							client.sendAsyncApiCommand("fifo", "list_verbose " + headers.get("FIFO-Name"));
 						} else if (action.equals("bridge-caller-start")) {
 							eventManager.answerCall(
 								headers.get("Unique-ID"),
								headers.get("Other-Leg-Callee-ID-Name"));
 							client.sendAsyncApiCommand("fifo", "list_verbose " + headers.get("FIFO-Name"));
 						} else if (action.equals("bridge-caller-stop")) {
 							eventManager.hangupCall(headers.get("Unique-ID"));
 							client.sendAsyncApiCommand("fifo", "list_verbose " + headers.get("FIFO-Name"));
 						} else if (action.equals("abort")) {
 							eventManager.abortCall(headers.get("Unique-ID"));
 							client.sendAsyncApiCommand("fifo", "list_verbose " + headers.get("FIFO-Name"));
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
 						event.getEventHeaders().get("Job-Command") + " " +
 						event.getEventHeaders().get("Job-Command-Arg");
 					if (jobsDirectory != null && !jobsDirectory.isEmpty()) {
 						new File(jobsDirectory+"/"+command).mkdir();
 						OutputFormat format = OutputFormat.createPrettyPrint();
 						Writer writer = new FileWriter(
 							jobsDirectory+"/"+command+"/"+event.getEventHeaders().get("Job-UUID")+".xml");
 						XMLWriter xmlWriter = new XMLWriter(writer, format);
 						xmlWriter.write(document);
 						xmlWriter.close();
 					}
 					if (command.equals("sofia xmlstatus profile " + profile))
 						processPresence(document);
 					if (command.startsWith("fifo list_verbose"))
 						processFifo(document);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		client.connect(host, port, password, 2);
 		client.sendAsyncApiCommand("sofia", "xmlstatus profile " + profile);
 		client.setEventSubscriptions("plain", "all");
 	}
 
 	private void processPresence(Document document) {
 		users.clear();
 		for(Node node : (List<Node>)document.selectNodes("/profile/registrations/registration")) {
 			String userName = node.valueOf("user").replace("@"+domain,"");
 			String userStatus = node.valueOf("agent")+" "+node.valueOf("status").replace("(unknown)","");
 			users.put(userName, userStatus);
 		}
 	}
 
 	private void processFifo(Document document) throws Exception {
 		String fifo = document.selectSingleNode("/fifo_report/fifo").valueOf("@name");
 		List<Node> members = document.selectNodes("/fifo_report/fifo/outbound/member");
 		List<Node> callers = document.selectNodes("/fifo_report/fifo/callers/caller");
 		List<Node> bridges = document.selectNodes("/fifo_report/fifo/bridges/bridge");
 		List<String> activeMembers = new ArrayList<String>();
 		List<String> connectedMembers = new ArrayList<String>();
 		if (fifoDirectory != null && !fifoDirectory.isEmpty()) {
 			FileWriter writer = new FileWriter(fifoDirectory+"/"+fifo+".status");
 			for (Node node : callers) {
 				writer.append(node.valueOf("@timestamp")+" ");
 				writer.append(String.format("%10s",node.valueOf("@caller_id_number"))+"\n");
 			}
 			for (Node node : bridges) {
 				String agent[] =
 					node.valueOf("consumer/cdr/callflow/caller_profile/originator/originator_caller_profile/chan_name")
 					.replace("sofia/"+profile+"/sip:","")
 					.replace(":5060","")
 					.split("@");
 				writer.append(node.valueOf("@bridge_start")+" ");
 				writer.append(String.format("%10s",node.valueOf("caller/@caller_id_number"))+" -> ");
 				writer.append(agent[0]+" "+((users.get(agent[0])==null)?(""):(users.get(agent[0])))+"\n");
 				connectedMembers.add(agent[0]);
 			}
 			for (Node node : members) {
 				String member = node.getText().replace("user/","").replace("@"+domain,"");
 				if (!connectedMembers.contains(member)) {
 					writer.append("                                  ");
 					writer.append(member+" "+((users.get(member)==null)?(""):(users.get(member)))+"\n");
 				}
 				if (users.containsKey(member))
 					activeMembers.add(member);
 			}
 			writer.append("\n");
 			writer.close();
 		}
 		eventManager.count(fifo, members.size(), activeMembers.size(), callers.size(), bridges.size());
 	}
 
 	public void destroy() {
 		client.close();
 		logger.debug("destroy");
 	}
 }
