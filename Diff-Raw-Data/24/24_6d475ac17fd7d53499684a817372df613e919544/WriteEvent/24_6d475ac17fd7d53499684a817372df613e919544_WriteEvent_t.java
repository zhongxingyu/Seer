 /**
  * WriteEvent.java
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
 
 import org.apache.niolex.network.IPacketWriter;
 import org.apache.niolex.network.PacketData;
 
 /**
  * Packet write event.
  * Our system use asynchronization way to send data.
  * That is, when you call #IPacketWriter.handleWrite, we just put the packet in to
  * the out going queue. System will send it when network is ready.
  *
  * So, how could you be sure that your packet is send? Use event based methods.
  *
 * @see org.apache.niolex.network.IPacketWriter#addEventListener(WriteEventListener)
  *
  * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
  * @version 1.0.0
  * @Date: 2012-6-14
  */
 public class WriteEvent {
 
 	/**
 	 * The packet writer who send this packet.
 	 */
 	private IPacketWriter packetWriter;
 
 	/**
 	 * The packet just sent to remote peer.
 	 */
 	private PacketData packetData;
 
 	public IPacketWriter getPacketWriter() {
 		return packetWriter;
 	}
 
 	public void setPacketWriter(IPacketWriter packetWriter) {
 		this.packetWriter = packetWriter;
 	}
 
 	public PacketData getPacketData() {
 		return packetData;
 	}
 
 	public void setPacketData(PacketData packetData) {
 		this.packetData = packetData;
 	}
 
 }
