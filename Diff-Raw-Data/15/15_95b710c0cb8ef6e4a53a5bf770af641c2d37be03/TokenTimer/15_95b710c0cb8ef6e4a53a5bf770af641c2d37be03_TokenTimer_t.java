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
 
 package org.continuent.appia.protocols.total.token;
 
 import org.continuent.appia.core.AppiaEventException;
 import org.continuent.appia.core.AppiaException;
 import org.continuent.appia.core.Channel;
 import org.continuent.appia.core.Session;
 import org.continuent.appia.core.events.channel.Timer;
 
 /**
  * This class defines a TokenTimer
  * 
  * @author <a href="mailto:nunomrc@di.fc.ul.pt">Nuno Carvalho</a>
  * @version 1.0
  */
 public class TokenTimer extends Timer {
 
     /**
      * Creates a new TokenTimer.
      */
     public TokenTimer() {
         super();
     }
 
     /**
      * Creates a new TokenTimer.
      * @param when
      * @param channel
      * @param dir
      * @param source
      * @param qualifier
      * @throws AppiaEventException
      * @throws AppiaException
      */
     public TokenTimer(long when, Channel channel, int dir,
             Session source, int qualifier) throws AppiaEventException,
             AppiaException {
         super(when, "total_token_timer", channel, dir, source, qualifier);
     }
 
 }
