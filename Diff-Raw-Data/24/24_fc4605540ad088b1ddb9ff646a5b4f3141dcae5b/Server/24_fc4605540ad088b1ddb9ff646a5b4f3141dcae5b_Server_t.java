 package com.github.dtf.rpc.server;
 
 import java.net.ServerSocket;
 
 import com.google.protobuf.BlockingService;
 
 public class Server {
 	private Class<?> protocol;
 	private BlockingService impl;
 	private ServerSocket ss;
 	private Listener listener;
 	private Handler handler;
	private String host;
	private int port;
 	private byte[] dataBuffer;
 	
	public Server(Class<?> protocol, BlockingService protocolImpl, String host, int port) {
 		this.protocol = protocol;
 		this.impl = protocolImpl;
		this.host = host;
		this.port = port;
 //		SocketAddress add = new InetSocketAddress(host, port);
 		listener = new Listener(this, host, port);
 		handler = new Handler(this);
 	}
 
 	/*public void run() {
 		Socket clientSocket = null;
 		DataOutputStream dos = null;
 		DataInputStream dis = null;
 		try {
 			ss = new ServerSocket(port);
 		} catch (IOException e) {
 		}
 		int testCount = 10; //
 
 		while (testCount-- > 0) {
 			try {
 				clientSocket = ss.accept();
 				dos = new DataOutputStream(clientSocket.getOutputStream());
 				dis = new DataInputStream(clientSocket.getInputStream());
 				int dataLen = dis.readInt();
 				byte[] dataBuffer = new byte[dataLen];
 				int readCount = dis.read(dataBuffer);
 				byte[] result = processOneRpc(dataBuffer);
 
 				dos.writeInt(result.length);
 				dos.write(result);
 				dos.flush();
 			} catch (Exception e) {
 			}
 		}
 		try {
 			dos.close();
 			dis.close();
 			ss.close();
 		} catch (Exception e) {
 		}
 		;
 
 	}*/
 
 	/** Starts the service. Must be called before any calls will be handled. */
 	public synchronized void start() {
 		// responder.start();
 		listener.registCallback(new Reader(this));
 		listener.start();
 		handler.start();
 		// handlers = new Handler[handlerCount];
 		//
 		// for (int i = 0; i < handlerCount; i++) {
 		// handlers[i] = new Handler(i);
 		// handlers[i].start();
 		// }
 	}
 
 	/*public byte[] processOneRpc(byte[] data) throws Exception {
 		RequestProto request = RequestProto.parseFrom(data);
 		String methodName = request.getMethodName();
 		MethodDescriptor methodDescriptor = impl.getDescriptorForType()
 				.findMethodByName(methodName);
 		Message response = impl.callBlockingMethod(methodDescriptor, null,
 				request);
 		return response.toByteArray();
 	}*/
 
 	public byte[] getDataBuffer() {
 		return dataBuffer;
 	}
 
 	public void setDataBuffer(byte[] dataBuffer) {
 		this.dataBuffer = dataBuffer;
 	}
 
 	public BlockingService getBlockingService() {
 		return impl;
 	}
 }
