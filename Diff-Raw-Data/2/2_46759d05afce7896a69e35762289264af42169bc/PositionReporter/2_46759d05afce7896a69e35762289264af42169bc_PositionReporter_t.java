 /*
  * Copyright 2010 Dozer Software, LLC
  * This software is licensed under the Simplified BSD License.
  * See license.txt for details.
  */
 
 package com.dozersoftware.snap;
 
 import org.jboss.soa.esb.actions.AbstractActionLifecycle;
 import org.jboss.soa.esb.client.ServiceInvoker;
 import org.jboss.soa.esb.helpers.ConfigTree;
 import org.jboss.soa.esb.listeners.message.MessageDeliverException;
 import org.jboss.soa.esb.message.Message;
 
 public class PositionReporter extends AbstractActionLifecycle {
 
 	private ConfigTree _config;
 
 	public PositionReporter(ConfigTree config) {
 		this._config = config;
 	}
 
 	public Message process(Message message) {
 		
 		try {
			new ServiceInvoker("NormOut", "NormProcessor").deliverAsync(message);
 		} catch (MessageDeliverException e) {
 			e.printStackTrace();
 		}
 		//System.out.println("Kicking a PositionReport!");
 		return message;
 	}
 	
 	public void exceptionHandler(Message message, Throwable exception) {
 		   System.out.println("!ERROR!");
 		   System.out.println(exception.getMessage());
 		   System.out.println("For Message: ");
 		   System.out.println(message.getBody().get());
 	}
 
 }
