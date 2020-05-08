 /**
  * Appia: Group communication and protocol composition framework library
  * Copyright 2006 University of Lisbon
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  *
  * Initial developer(s): Alexandre Pinto and Hugo Miranda.
  * Contributor(s): See Appia web page for a list of contributors.
  */
  /*
  * Created on Mar 16, 2004
  *
  * To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package org.continuent.appia.test.xml.ecco;
 
 import org.continuent.appia.core.Layer;
 import org.continuent.appia.core.Session;
 import org.continuent.appia.core.events.channel.ChannelClose;
 import org.continuent.appia.core.events.channel.ChannelInit;
 import org.continuent.appia.protocols.common.RegisterSocketEvent;
 
 /**
  * This class defines a EccoLayer
  * 
  * @author Jose Mocito
  * @version 1.0
  */
 public class EccoLayer extends Layer {
 
     /**
      * Creates a new EccoLayer.
      */
 	public EccoLayer() {
 		
 		evRequire = new Class[]{
 		        ChannelInit.class,
 		};
         
 		evProvide = new Class[] {
           RegisterSocketEvent.class,      
         };
 		
 		evAccept = new Class[]{
                 ChannelInit.class,
                 ChannelClose.class,
                 RegisterSocketEvent.class,
                 MyEccoEvent.class,
         };
 	}
 	
 	/**
      * Creates the session for this protocol.
	 * @see Layer#createSession()
 	 */
 	public Session createSession() {
 		return new EccoSession(this);
 	}
 }
