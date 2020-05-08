 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2012, Red Hat, Inc., and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.aerogear.prodoctor.service;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.aerogear.prodoctor.model.Lead;
 import org.aerogear.unifiedpush.DefaultJavaSender;
 import org.aerogear.unifiedpush.resteasy.RestEasyClient;
 
 public class LeadSender {
 
 	private String serverURL = "http://localhost:8080/ag-push";
 
 	private String pushApplicationId = "0ef30a53-1096-43f9-ab1f-95f684551c7e";
 
 	private DefaultJavaSender defaultJavaSender = new DefaultJavaSender(
 			serverURL, new RestEasyClient());
 
 	public void sendLeads(List<String> users, Lead lead) {
		
 		Map json = new HashMap();
 		json.put("name", lead.getName());
 		json.put("location", lead.getLocation());
 		json.put("phone", lead.getPhoneNumber());
 		defaultJavaSender.sendTo(users, json, pushApplicationId);
 	}
 
 	public String getServerURL() {
 		return serverURL;
 	}
 
 	public void setServerURL(String serverURL) {
 		this.serverURL = serverURL;
 	}
 
 	public DefaultJavaSender getDefaultJavaSender() {
 		return defaultJavaSender;
 	}
 
 	public void setDefaultJavaSender(DefaultJavaSender defaultJavaSender) {
 		this.defaultJavaSender = defaultJavaSender;
 	}
 
 }
