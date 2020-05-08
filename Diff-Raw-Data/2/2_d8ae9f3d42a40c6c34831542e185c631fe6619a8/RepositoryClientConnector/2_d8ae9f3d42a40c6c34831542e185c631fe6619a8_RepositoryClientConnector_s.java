 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is Gmodel.
  *
  * The Initial Developer of the Original Code is
  * Sofismo AG (Sofismo).
  * Portions created by the Initial Developer are
  * Copyright (C) 2009-2011 Sofismo AG.
  * All Rights Reserved.
  *
  * Contributor(s):
  * Chul Kim
  * ***** END LICENSE BLOCK ***** */
 
 package org.s23m.cell.repository.client.connector;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.s23m.cell.repository.client.RepositoryClient;
 import org.s23m.cell.repository.client.server.ConfigValues;
 import org.s23m.cell.serialization.container.ArtefactContainer;
 import org.s23m.cell.serialization.serializer.ProtocolType;
 import org.s23m.cell.serialization.serializer.SerializationType;
 import org.s23m.cell.serialization.serializer.Serializer;
 import org.s23m.cell.serialization.serializer.SerializerHolder;
 
 import com.rabbitmq.client.Channel;
 import com.rabbitmq.client.Connection;
 import com.rabbitmq.client.ConnectionFactory;
 import com.rabbitmq.client.RpcClient;
 import com.rabbitmq.client.ShutdownSignalException;
 
 public class RepositoryClientConnector implements RepositoryClient {
 
 	private RpcClient clientService;
 
 	private static class RepositoryClientConnectorHolder {
 		public static final RepositoryClientConnector CONNECTOR = new RepositoryClientConnector();
 	}
 
 	private RepositoryClientConnector() {
 		initClient();
 	}
 
 	private void initClient() {
 		final ConnectionFactory cfconn = new ConnectionFactory();
 		cfconn.setHost(ConfigValues.getString("RepositoryClientServer.HOST_NAME"));
 		cfconn.setPort(Integer.parseInt(ConfigValues.getString("RepositoryClientServer.PORT")));
 		cfconn.setVirtualHost(ConfigValues.getString("RepositoryClientServer.VHOST_NAME"));
 		cfconn.setUsername(ConfigValues.getString("RepositoryClientServer.USER_NAME"));
 		cfconn.setPassword(ConfigValues.getString("RepositoryClientServer.PW"));
 		Connection conn;
 		try {
 			conn = cfconn.newConnection();
 			final Channel ch = conn.createChannel();
 			clientService = new RpcClient(ch, "", ConfigValues.getString("RepositoryClientServer.QUEUE"));
 		} catch (final IOException ex) {
			throw new IllegalStateException("Client set up is failed",ex);
 		}
 	}
 
 	public static RepositoryClient getComponent() {
 		return RepositoryClientConnectorHolder.CONNECTOR;
 	}
 
 	public ArtefactContainer get(final ArtefactContainer artifact) throws UnsupportedOperationException {
 		final Map<String,Object> artifactsToGet = new HashMap<String,Object>();
 		Map<String,Object> returnedArtifacts = null;
 		final Serializer sz = SerializerHolder.getGmodelInstanceSerializer(SerializationType.XML);
 		artifactsToGet.put(artifact.getContentType(), sz.serializeContainer(artifact));
 		try {
 			returnedArtifacts = clientService.mapCall(artifactsToGet);
 			//			if (container.getContentType().equals(SerializationType.CONTAINMENT_TREE.name())) {
 			//				new ArtifactContainerContentMapper()
 			//				.recreateInstancesFromArtifactMap(returnedArtifacts);
 			//			}
 		} catch (final ShutdownSignalException ex) {
 			throw new UnsupportedOperationException("Service invocation is failed",ex);
 		} catch (final IOException ex) {
 			throw new UnsupportedOperationException("Service invocation is failed",ex);
 		}
 		final String serializedResponse = returnedArtifacts.entrySet().iterator().next().getValue().toString();
 		return sz.unmarshallContainer(serializedResponse);
 	}
 
 	public void put(final ArtefactContainer artifact) throws UnsupportedOperationException {
 		get(artifact);
 	}
 
 	public String getName() {
 		return CLIENT_ID.toString();
 	}
 
 	public ProtocolType getProtocolType() {
 		return ProtocolType.REPOSITORY_CLIENT;
 	}
 
 }
