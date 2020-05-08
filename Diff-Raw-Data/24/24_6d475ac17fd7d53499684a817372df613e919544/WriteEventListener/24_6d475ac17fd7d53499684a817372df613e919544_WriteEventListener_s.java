 /**
  * WriteEventListener.java
  *
  * Copyright 2012 Niolex, Inc.
  *
  * Niolex licenses this file to you under the Apache License, version 2.0
  * (the "License"); you may not use this file except in compliance with the
  * License.  You may obtain a copy of the License at:
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
  * License for the specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.niolex.network.event;
 
 /**
  * The interface user need to implement in order to get the event
  * when packet sent.
  *
  * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
  * @version 1.0.0
  * @Date: 2012-6-14
  */
 public interface WriteEventListener {
 
 	/**
 	 * This event is fired just after we send the packet to remote peer.
 	 *
 	 * Notion! We fire this event only indicate that the data is sent to network
 	 * buffer, it will be transfered to remote if no I/O error occur.
 	 * So you can not be 100% sure that the remote will get this packet.
 	 *
 	 * @param wEvent
 	 */
 	public void afterSend(WriteEvent wEvent);
 
 }
