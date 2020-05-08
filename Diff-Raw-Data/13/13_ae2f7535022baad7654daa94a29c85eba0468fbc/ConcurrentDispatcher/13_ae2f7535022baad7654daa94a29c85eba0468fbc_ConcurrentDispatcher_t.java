 /**
  * ConcurrentDispatcher.java
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
 package org.apache.niolex.network.name.event;
 
 import java.util.Hashtable;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.niolex.network.Config;
 import org.apache.niolex.network.IPacketWriter;
 import org.apache.niolex.network.PacketData;
 import org.apache.niolex.network.name.bean.AddressRecord;
 import org.apache.niolex.network.packet.PacketTransformer;
 
 /**
  * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
  * @version 1.0.0
  * @Date: 2012-6-26
  */
 public class ConcurrentDispatcher implements IDispatcher {
 
 	private final ConcurrentHashMap<String, Hashtable<IPacketWriter, String>> map = new ConcurrentHashMap<String, Hashtable<IPacketWriter, String>>();
 
 	private final PacketTransformer transformer;
 
 
 	/**
 	 * Constructor.
 	 */
 	public ConcurrentDispatcher() {
 		super();
 		transformer = PacketTransformer.getInstance();
 	}
 
 	/**
 	 * Override super method
 	 * @see org.apache.niolex.network.name.event.IDispatcher#fireEvent(org.apache.niolex.network.name.bean.AddressRecord)
 	 */
 	@Override
 	public void fireEvent(AddressRecord rec) {
 		PacketData rc = transformer.getPacketData(Config.CODE_NAME_DIFF, rec);
 		Hashtable<IPacketWriter, String> queue = map.get(rec.getAddressKey());
 		if (queue != null) {
 			for (IPacketWriter wt : queue.keySet()) {
 				wt.handleWrite(rc.makeCopy());
 			}
 		}
 	}
 
 	/**
 	 * Override super method
 	 * @see org.apache.niolex.network.name.event.IDispatcher#register(java.lang.String, org.apache.niolex.network.IPacketWriter)
 	 */
 	@Override
 	public void register(String addressKey, IPacketWriter wt) {
 		Hashtable<IPacketWriter, String> queue = map.get(addressKey);
 		if (queue == null) {
 			synchronized (map) {
 				queue = map.get(addressKey);
 				if (queue == null) {
 					queue = new Hashtable<IPacketWriter, String>();
 					map.put(addressKey, queue);
 				}
 			}
 		}
 		// queue is ready.
 		queue.put(wt, "");
 	}
 
 	/**
 	 * Override super method
	 * @see org.apache.niolex.network.name.event.IDispatcher#handleClose(java.lang.String, org.apache.niolex.network.IPacketWriter)
 	 */
 	@Override
 	public void handleClose(String addressKey, IPacketWriter wt) {
 		Hashtable<IPacketWriter, String> queue = map.get(addressKey);
 		if (queue != null) {
 			queue.remove(wt);
 		}
 	}
 
 }
