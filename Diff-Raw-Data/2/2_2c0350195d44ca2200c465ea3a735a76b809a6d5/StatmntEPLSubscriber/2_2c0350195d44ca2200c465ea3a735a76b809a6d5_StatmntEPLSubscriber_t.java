 /*
 Copyright (c) 2013 J. L. Canales Gasco
  
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
  
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
  
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA}]
 */
 
 package org.rotarysource.core.statements;
 
 import com.espertech.esper.client.EPException;
 import com.espertech.esper.client.EPServiceProvider;
 import com.espertech.esper.client.EPStatementException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class to create and register a single EPL statement associated to a subscriber. 
  * This is the simplest way to declare statements and joint it to POJO object. It provide a
  * way to take advantage of subscriber performance and capabilities proviced by Esper.
  * 
  * @author J.L. Canales
  */
 public class StatmntEPLSubscriber extends StatmntPrepare {
 	private static Logger  log = LoggerFactory.getLogger(StatmntEPLSubscriber.class);
 
 
     /**
      * Subscriber linked to this Statement Item
      */
     private Object subscriber;
 
 	/**
 	 * Create a new StatmntSingleQuery, for bean-style usage.
 	 */
 	public StatmntEPLSubscriber() {
 		super();
 	}
 
 	/**
 	 * Create a new StatmntEPLSubscriber, given a EPL statement
 	 * 
 	 * @param aiEplStatement
 	 *            EPL statement to initialize this Item
 	 */
 	public StatmntEPLSubscriber(String aiEplStatement) {
 		super(aiEplStatement);
     	subscriber = null;
 	}
 
 	/**
 	 * Method to Statement registering in a EventProcessor engine
 	 * 
 	 * @param EPServiceProvider
 	 *            . Esper Event Processor engine where register the statement.
 	 */
 	@Override
 	public void register(EPServiceProvider cepEngine) {
 		try{		
 			super.register(cepEngine);
 	
 			log.info("Adding Subscriber to : {}", this.getEplName());
 			
 			// Joining subscriber to EPL object
 			if(subscriber != null)
 				this.statementObj.setSubscriber(subscriber);
 			
 			log.info("Successfull subscriber registration for: {}", this.getEplName());
 		}catch (EPStatementException exception){
 			log.error("Failure registering EPL for: {}", this.getEplName());
 			throw exception;
 			
 		}catch (EPException exception){
			log.error("Failure subscriber registration for: {} ; Nexted Exception: {}", this.getEplName(), exception.getMessage());
 			throw exception;
 			
 		}
 	}
 
 
 
 	/**
 	* Set the Listeners list for this item.
 	* @param aiListeners Listeners List to initialize this item
 	*/
 	public void setSubscriber(Object aiSubscriber) {
 		this.subscriber = aiSubscriber;
 	}
 
 }
