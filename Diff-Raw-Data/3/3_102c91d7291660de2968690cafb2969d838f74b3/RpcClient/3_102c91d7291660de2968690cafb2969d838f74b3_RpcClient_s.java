 /**
  * RpcClient.java
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
 package org.apache.niolex.network.rpc;
 
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.lang.reflect.Type;
 import java.net.InetSocketAddress;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.apache.niolex.commons.reflect.MethodUtil;
 import org.apache.niolex.commons.util.SystemUtil;
 import org.apache.niolex.network.Config;
 import org.apache.niolex.network.IClient;
 import org.apache.niolex.network.IPacketHandler;
 import org.apache.niolex.network.IPacketWriter;
 import org.apache.niolex.network.PacketData;
 import org.apache.niolex.network.rpc.anno.RpcMethod;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The basic RpcClient, send and receive Rpc packets, do client stub here too.
  *
  * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
  * @version 1.0.0, Date: 2012-6-1
  */
 public class RpcClient implements PoolableInvocationHandler, IPacketHandler {
 	private static final Logger LOG = LoggerFactory.getLogger(RpcClient.class);
 
 	/**
 	 * Save the execution map.
 	 */
 	private final Map<Method, Short> executeMap = new HashMap<Method, Short>();
 
 	/**
 	 * The serial generator.
 	 */
 	private final AtomicInteger auto = new AtomicInteger(1);
 
 	/**
 	 * The time to sleep between retry.
 	 */
 	private int sleepBetweenRetryTime = Config.RPC_SLEEP_BT_RETRY;
 
 	/**
 	 * Times to retry get connected.
 	 */
 	private int connectRetryTimes = Config.RPC_CONNECT_RETRY_TIMES;
 
 	/**
 	 * The PacketClient to send and receive Rpc packets.
 	 */
 	private final IClient client;
 
 	/**
 	 * The RPC invoker to do the real method invoke.
 	 */
 	private final RemoteInvoker invoker;
 
 	/**
 	 * The data translator.
 	 */
 	private final IConverter converter;
 
 	/**
 	 * The status of this Client.
 	 */
 	private Status connStatus;
 
 	/**
 	 * The connection status of this RpcClient.
 	 *
 	 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 	 * @version 1.0.0, Date: 2012-6-2
 	 */
 	public static enum Status {
 		INNITIAL, CONNECTED, CLOSED
 	}
 
 	/**
 	 * Create a RpcClient with this client as the backed communication tool.
 	 * The PacketClient will be managed internally, please use this.connect() to connect.
 	 *
 	 * @param client the backed communication tool
 	 * @param invoker use this to send packets to server and wait for response
 	 * @param converter use this to serialize data
 	 */
 	public RpcClient(IClient client, RemoteInvoker invoker, IConverter converter) {
 		super();
 		this.client = client;
 		this.invoker = invoker;
 		this.converter = converter;
 		this.client.setPacketHandler(this);
 		this.connStatus = Status.INNITIAL;
 	}
 
 	/**
 	 * Connect the backed communication client, and set the internal status.
 	 * @throws IOException
 	 */
 	public void connect() throws IOException {
 		this.client.connect();
 		this.connStatus = Status.CONNECTED;
 	}
 
 	/**
 	 * Stop this client, and stop the backed communication client.
 	 */
 	public void stop() {
 		this.connStatus = Status.CLOSED;
 		this.client.stop();
 	}
 
 	/**
 	 * Get the Rpc Service Client Stub powered by this rpc client.
 	 *
 	 * @param c The interface you want to have stub.
 	 * @return the stub
 	 */
 	@SuppressWarnings("unchecked")
     public <T> T getService(Class<T> c) {
 		this.addInferface(c);
 		return (T) Proxy.newProxyInstance(RpcClient.class.getClassLoader(),
                 new Class[] {c}, this);
 	}
 
 	/**
 	 * Check the client status before doing remote call and after response.
 	 */
 	private void checkStatus() {
 		RpcException rep = null;
 		switch (connStatus) {
 			case INNITIAL:
 				rep = new RpcException("Client not connected.", RpcException.Type.NOT_CONNECTED, null);
 				throw rep;
 			case CLOSED:
 				rep = new RpcException("Client closed.", RpcException.Type.CONNECTION_CLOSED, null);
 				throw rep;
 		}
 	}
 
 	/**
 	 * This is the override of super method.
 	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
 	 */
 	@Override
 	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
 		checkStatus();
 		RpcException rep = null;
 		Short rei = executeMap.get(method);
 		if (rei != null) {
 			// 1. Prepare parameters
 			byte[] arr;
 			if (args == null || args.length == 0) {
 				arr = new byte[0];
 			} else {
 				arr = converter.serializeParams(args);
 			}
 			// 2. Create PacketData
 			PacketData rc = new PacketData(rei, arr);
 			// 3. Generate serial number
 			serialPacket(rc);
 
 			// 4. Invoke, send packet to server and wait for result
 			PacketData sc = invoker.invoke(rc, client);
 
 			// 5. Process result.
 			if (sc == null) {
 				checkStatus();
 				rep = new RpcException("Timeout for this remote procedure call.",
 						RpcException.Type.TIMEOUT, null);
 				throw rep;
 			} else {
 				int exp = sc.getReserved() - rc.getReserved();
 				boolean isEx = false;
 				// 127 + 1 = -128
 				// -128 - 127 = -255
 				if (exp == 1 || exp == -255) {
 					isEx = true;
 				}
 				Object ret = prepareReturn(sc.getData(), method.getGenericReturnType(), isEx);
 				if (isEx) {
 					rep = (RpcException) ret;
 					throw rep;
 				}
 				return ret;
 			}
 		} else {
 			rep = new RpcException("The method you want to invoke is not a remote procedure call.",
 					RpcException.Type.METHOD_NOT_FOUND, null);
 			throw rep;
 		}
 	}
 
 	/**
 	 * Generate serial number
 	 * The serial number will be 1, 3, 5, ...
 	 *
 	 * @param rc
 	 */
 	private void serialPacket(PacketData rc) {
 		short seri = (short) (auto.getAndAdd(2));
 		rc.setReserved((byte) seri);
 		rc.setVersion((byte) (seri >> 8));
 	}
 
 	/**
 	 * Set the Rpc Configs, this method will parse all the configurations and generate execute map.
 	 * @param interfs
 	 */
 	public void addInferface(Class<?> interfs) {
 		Method[] arr = MethodUtil.getMethods(interfs);
 		for (Method m : arr) {
 			if (m.isAnnotationPresent(RpcMethod.class)) {
 				RpcMethod rp = m.getAnnotation(RpcMethod.class);
 				Short rei = executeMap.put(m, rp.value());
 				if (rei != null) {
 					LOG.warn("Duplicate configuration for code: {}", rp.value());
 				}
 			}
 		} // End of arr
 	}
 
 	/**
 	 * De-serialize returned byte array into objects.
 	 *
 	 * @param ret
 	 * @param type
 	 * @param isEx
 	 * @return the object
 	 * @throws Exception
 	 */
 	protected Object prepareReturn(byte[] ret, Type type, boolean isEx) throws Exception {
 		if (isEx) {
 			type = RpcException.class;
 		} else if (type == null || type.toString().equalsIgnoreCase("void")) {
 			return null;
 		}
 		return converter.prepareReturn(ret, type);
 	}
 
 	/**
 	 * We delegate all read packets to invoker.
 	 *
 	 * Override super method
 	 * @see org.apache.niolex.network.IPacketHandler#handleRead(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)
 	 */
 	@Override
 	public void handleRead(PacketData sc, IPacketWriter wt) {
 		this.invoker.handleRead(sc, wt);
 	}
 
 	/**
 	 * We will retry to connect to server in this method.
 	 *
 	 * Override super method
 	 * @see org.apache.niolex.network.IPacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)
 	 */
 	@Override
 	public void handleClose(IPacketWriter wt) {
 		if (this.connStatus == Status.CLOSED) {
 			return;
 		}
 		this.connStatus = Status.INNITIAL;
 		if (!retryConnect()) {
 			LOG.error("We can not re-connect to server after retry times, RpcClient with stop.");
 			// Try to shutdown this Client, inform all the threads.
 			this.connStatus = Status.CLOSED;
 			this.client.stop();
 			this.invoker.handleClose(wt);
 		}
 	}
 
 	/**
 	 * Try to re-connect to server.
 	 *
 	 * @return true if connected
 	 */
 	private boolean retryConnect() {
 		for (int i = 0; i < connectRetryTimes; ++i) {
 		    SystemUtil.sleep(sleepBetweenRetryTime);
 			LOG.info("RPC Client try to reconnect to server round {} ...", i);
 			try {
 				client.connect();
 				this.connStatus = Status.CONNECTED;
 				return true;
 			} catch (IOException e) {
 				// Not connected.
 				LOG.info("Try to re-connect to server failed. {}", e.toString());
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Get Connection Status of this rpc client.
 	 *
 	 * @return current status
 	 */
 	public Status getConnStatus() {
 		return connStatus;
 	}
 
 	/**
 	 * Get Connection Status of this rpc client.
 	 *
 	 * @return true if this RpcClient is valid and ready to work.
 	 */
 	public boolean isValid() {
         return connStatus == Status.CONNECTED;
     }
 
 	/**
      * @return The string representation of the remote peer. i.e. The IP address.
      */
 	public String getRemoteName() {
         return client.getRemoteName();
     }
 
     /**
 	 * Set the time in milliseconds that client with sleep between retry to connect
 	 * to server.
 	 *
 	 * @param sleepBetweenRetryTime
 	 */
 	public void setSleepBetweenRetryTime(int sleepBetweenRetryTime) {
 		this.sleepBetweenRetryTime = sleepBetweenRetryTime;
 	}
 
 	/**
 	 * Set retry times.
 	 *
 	 * @param connectRetryTimes
 	 */
 	public void setConnectRetryTimes(int connectRetryTimes) {
 		this.connectRetryTimes = connectRetryTimes;
 	}
 
 	/**
 	 * Set the socket connect timeout.
 	 * This method must be called before {@link #connect()}
 	 *
 	 * @param timeout
 	 */
 	public void setConnectTimeout(int timeout) {
 		this.client.setConnectTimeout(timeout);
 	}
 
 	/**
      * Set the server Internet address this client want to connect
      * This method must be called before {@link #connect()}
      *
      * @param serverAddress
      */
     public void setServerAddress(InetSocketAddress serverAddress) {
         client.setServerAddress(serverAddress);
     }
 
 }
