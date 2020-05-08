 /*
  * Copyright (c) 2012-2013 Open Source Community - <http://www.peerfact.org>
  * Copyright (c) 2011-2012 University of Paderborn - UPB
  * Copyright (c) 2005-2011 KOM - Multimedia Communications Lab
  *
  * This file is part of PeerfactSim.KOM.
  * 
  * PeerfactSim.KOM is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  * 
  * PeerfactSim.KOM is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with PeerfactSim.KOM.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package org.peerfact.impl.overlay.unstructured.zeroaccess.components;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.log4j.Logger;
 import org.peerfact.api.common.ConnectivityEvent;
 import org.peerfact.api.common.Message;
 import org.peerfact.api.common.Operation;
 import org.peerfact.api.common.OperationCallback;
 import org.peerfact.api.transport.TransInfo;
 import org.peerfact.api.transport.TransLayer;
 import org.peerfact.impl.overlay.unstructured.zeroaccess.message.BaseMessage;
 import org.peerfact.impl.overlay.unstructured.zeroaccess.message.GetLMessage;
 import org.peerfact.impl.overlay.unstructured.zeroaccess.message.RetLMessage;
 import org.peerfact.impl.overlay.unstructured.zeroaccess.operation.GetLOperation;
 import org.peerfact.impl.transport.TransMsgEvent;
 
 /**
  * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  * This part of the Simulator is not maintained in the current version of
  * PeerfactSim.KOM. There is no intention of the authors to fix this
  * circumstances, since the changes needed are huge compared to overall benefit.
  * 
  * If you want it to work correctly, you are free to make the specific changes
  * and provide it to the community.
  * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! !!!!!!!!!!!!!!!!!!!!!!!!!!!!
  * 
  * 
  * @author <info@peerfact.org>
  * @version 05/06/2011
  * 
  */
 public class ZeroAccessCrawlOverlayNode extends ZeroAccessOverlayNode {
 
 	final public Logger log = Logger
 			.getLogger(ZeroAccessCrawlOverlayNode.class);
 
 	private TransLayer transLayer;
 
 	private ConcurrentHashMap<ZeroAccessOverlayID, ZeroAccessOverlayContact> nodesMap = new ConcurrentHashMap<ZeroAccessOverlayID, ZeroAccessOverlayContact>();
 
 	private LinkedList<ZeroAccessOverlayContact> unProbedNodeList = new LinkedList<ZeroAccessOverlayContact>();
 
 	public ZeroAccessCrawlOverlayNode(TransLayer transLayer,
 			ZeroAccessOverlayID peerId,
 			int numConn, long delayAcceptConnection, long refresh,
 			long contactTimeout, long descriptorTimeout, short port) {
 		super(transLayer, peerId, numConn, delayAcceptConnection, refresh,
 				contactTimeout, descriptorTimeout, port);
 
 		this.transLayer = transLayer;
 		transLayer.addTransMsgListener(this, this.getPort());
 
 		this.routingTable = new ZeroAccessOverlayRoutingTable(peerId);
 
 	}
 
 	@Override
 	public TransLayer getTransLayer() {
 		return transLayer;
 	}
 
 	@Override
 	public void connectivityChanged(ConnectivityEvent ce) {
 		//
 	}
 
 	@Override
 	public void messageTimeoutOccured(int commId) {
 		//
 	}
 
 	@Override
 	public void receive(Message msg, TransInfo senderInfo, int commId) {
 		//
 	}
 
 	@Override
 	public void messageArrived(TransMsgEvent receivingEvent) {
 		Message message = receivingEvent.getPayload();
 
 		if (isActive()) {
 			// accept messages only if connection to peer exists
 			if (message instanceof BaseMessage) {
 				if (message instanceof GetLMessage) {
 					processGetL(receivingEvent);
 				} else if (message instanceof RetLMessage) {
 					processRetL(receivingEvent);
 				}
 			}
 		}
 	}
 
 	private void processGetL(TransMsgEvent receivingEvent) {
 		int i = 1;
 		i++;
 	}
 
 	private void processRetL(TransMsgEvent receivingEvent) {
 
 		RetLMessage retLMessage = (RetLMessage) receivingEvent
 				.getPayload();
 
 		LinkedList<ZeroAccessOverlayContact> contact_list = retLMessage
 				.getContacts();
 
 		for (int i = 0; i < contact_list.size(); i++)
 		{
 			ZeroAccessOverlayContact node = contact_list.get(i);
 			if (!nodesMap.contains(node.getOverlayID()))
 			{
 				nodesMap.putIfAbsent(node.getOverlayID(), node);
 				unProbedNodeList.push(node);
 			}
 		}
		log.warn("Current size of crawling: " + nodesMap.size());
 		scheduleCrawl();

 	}
 
 	public void initForCrawl()
 	{
 		List<TransInfo> bootstrapInfos = ZeroAccessBootstrapManager
 				.getInstance().getBootstrapInfo();
 		for (int i = 0; i < bootstrapInfos.size(); i++)
 		{
 			GetLOperation getLOperation = new GetLOperation(this,
 					bootstrapInfos.get(i), new OperationCallback<Object>() {
 						@Override
 						public void calledOperationFailed(
 								Operation<Object> op) {
 							//
 						}
 
 						@Override
 						public void calledOperationSucceeded(
 								Operation<Object> op) {
 							//
 						}
 					});
 			getLOperation.scheduleImmediately();
 		}
 	}
 
 	public void scheduleCrawl() {
 		while (!unProbedNodeList.isEmpty())
 		{
 			ZeroAccessOverlayContact node = unProbedNodeList.pop();
 			GetLOperation getLOperation = new GetLOperation(this,
 					node.getTransInfo(), new OperationCallback<Object>() {
 						@Override
 						public void calledOperationFailed(
 								Operation<Object> op) {
 							//
 						}
 
 						@Override
 						public void calledOperationSucceeded(
 								Operation<Object> op) {
 							//
 						}
 					});
 			getLOperation.scheduleImmediately();
 		}
 
 	}
 
 	@Override
 	public String toString() {
 		return this.getOverlayID().toString();
 	}
 }
