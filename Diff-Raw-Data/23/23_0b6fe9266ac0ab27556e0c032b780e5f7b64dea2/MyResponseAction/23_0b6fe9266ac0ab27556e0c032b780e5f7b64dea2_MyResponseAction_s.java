 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2006, JBoss Inc., and others contributors as indicated 
  * by the @authors tag. All rights reserved. 
  * See the copyright.txt in the distribution for a
  * full listing of individual contributors. 
  * This copyrighted material is made available to anyone wishing to use,
  * modify, copy, or redistribute it subject to the terms and conditions
  * of the GNU Lesser General Public License, v. 2.1.
  * This program is distributed in the hope that it will be useful, but WITHOUT A 
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
  * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public License,
  * v.2.1 along with this distribution; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
  * MA  02110-1301, USA.
  * 
  * (C) 2005-2006,
  * @author JBoss Inc.
  */
 package eai.msejdf.esb;
 
 import java.util.Map;
 
 import org.jboss.soa.esb.actions.AbstractActionLifecycle;
 import org.jboss.soa.esb.helpers.ConfigTree;
 import org.jboss.soa.esb.message.Message;
 import java.util.Map;
 import java.util.Set;
 
 public class MyResponseAction extends AbstractActionLifecycle {
 	protected ConfigTree _config;
 
 	public MyResponseAction(ConfigTree config) {
 		_config = config;
 	}
 
 	public Message noOperation(Message message) {
 		return message;
 	}
 
 	/*
 	 * Retrieve and output the webservice response.
 	 */
 	public Message process(Message message) throws Exception {
 
 		logHeader();
 
 		// The "responseLocation" property was set in jboss-esb.xml to
 		// "helloworld-response"
 		// Map responseMsg = (Map) message.getBody().get(Body.DEFAULT_LOCATION);
 		@SuppressWarnings("rawtypes")
 		Map responseMsg = (Map) message.getBody().get();
 		// String[] responseMsg2 = message.getBody().getNames();
 		System.out.println("MyResponseAction");
 		@SuppressWarnings("unchecked")
 		Set<String> set = responseMsg.keySet();
 		for (String user : set) {
 			System.out.println("Response Map is: " + user + "\t"
 					+ responseMsg.get(user));
 		}
 		// for (String user : responseMsg2) {
 		// System.out.println("MyResponseAction2: " + user);
 		// }
 
 		logFooter();
 		return message;
 	}
 
 	public void exceptionHandler(Message message, Throwable exception) {
 		logHeader();
 		System.out.println("MyResponseAction: " + "!ERROR!");
 		System.out.println("MyResponseAction: " + exception.getMessage());
 		System.out.println("MyResponseAction: " + "For Message: ");
 		System.out.println("MyResponseAction: " + message.getBody().get());
 		logFooter();
 	}
 
 	// This makes it easier to read on the console
 	private void logHeader() {
 		System.out
 				.println("&&&&&&&&&&&&&&&&&&&& MyResponseAction &&&&&&&&&&&&&&&&&&&&&&&&\n");
 	}
 
 	private void logFooter() {
 		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&\n");
 	}
 
 }
