 /*******************************************************************************
  * Copyright (c) 2008, 2009 SOPERA GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
 * 
  * Contributors:
  *     SOPERA GmbH - initial API and implementation
  *******************************************************************************/
 /**
  *
  */
 package org.eclipse.swordfish.core.test.util.mock;
 
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import javax.jbi.messaging.MessageExchange;
 
 import org.eclipse.swordfish.core.Interceptor;
 import org.eclipse.swordfish.core.SwordfishException;
 
 /**
  * @author dwolz
  *
  */
 public class MockInterceptor implements Interceptor {
    private List<MessageExchange> exchanges = new CopyOnWriteArrayList<MessageExchange>();
 	public Map<String, ?> getProperties() {
		// TODO Auto-generated method stub
		return null;
 	}
 
 
 	public void process(MessageExchange exchange) throws SwordfishException {
 	    exchanges.add(exchange);
 	}
 
     public List<MessageExchange> getExchanges() {
         return exchanges;
     }
 }
