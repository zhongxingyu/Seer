 /**
  * Copyright 2012 Thorsten Höger, RestDoc.org
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
  * obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
  * and limitations under the License.
  */
 package org.restdoc.api;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.codehaus.jackson.annotate.JsonProperty;
 import org.codehaus.jackson.annotate.JsonPropertyOrder;
 
 /**
  * @author hoegertn
  * 
  */
@JsonPropertyOrder({ "path", "body", "headers" })
 public class ExampleRequest {
 
 	@JsonProperty("path")
 	private String path;
 
 	@JsonProperty("body")
 	private String body;
 
 	private final Map<String, String> headers = new HashMap<String, String>();
 
 	/**
 	 * 
 	 */
 	public ExampleRequest() {
 		//
 	}
 
 	/**
 	 * @param path
 	 * @param body
 	 */
 	public ExampleRequest(final String path, final String body) {
 		this.path = path;
 		this.body = body;
 	}
 
 	/**
 	 * @return the path
 	 */
 	public String getPath() {
 		return this.path;
 	}
 
 	/**
 	 * @param path
 	 *            the path to set
 	 */
 	public void setPath(final String path) {
 		this.path = path;
 	}
 
 	/**
 	 * @return the body
 	 */
 	public String getBody() {
 		return this.body;
 	}
 
 	/**
 	 * @param body
 	 *            the body to set
 	 */
 	public void setBody(final String body) {
 		this.body = body;
 	}
 
 	/**
 	 * @return the headers
 	 */
 	public Map<String, String> getHeaders() {
 		return this.headers;
 	}
 
 	// #############################################################
 	// nice builder methods
 	// #############################################################
 
 	/**
 	 * @param header
 	 * @param value
 	 * @return this
 	 */
 	public ExampleRequest header(final String header, final String value) {
 		this.getHeaders().put(header, value);
 		return this;
 	}
 
 }
