 /*
  * $Id$
  * $Revision$ $Date$
  * 
  * ==============================================================================
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.wicketstuff.dojo.examples;
 
 import org.apache.wicket.Request;
 import org.apache.wicket.Response;
 import org.apache.wicket.protocol.http.WebApplication;
 import org.apache.wicket.protocol.http.WebSession;
 
 /**
  * Subclass of WebSession for ExampleApplication to allow easy and typesafe
  * access to session properties.
  */
 public final class ExampleSession extends WebSession {
 	// TODO Add any session properties here
 
 	/**
 	 * Constructor
 	 * 
 	 * @param application
 	 *            The application
 	 * @param request 
 	 */
 	protected ExampleSession(final WebApplication application, Request request, Response response) {
		super(application, request, response);
 	}
 }
