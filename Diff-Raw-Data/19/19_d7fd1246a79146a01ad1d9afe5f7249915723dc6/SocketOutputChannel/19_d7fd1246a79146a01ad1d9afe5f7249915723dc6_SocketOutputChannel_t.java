 /*
  * Copyright (C) 2013 IEL of ISCAS
  * Project:CSDTP
  * Author: voidmain
  * Create Date: Apr 24, 20135:20:17 PM
  */
 package cn.ac.iscas.iel.csdtp.channel;
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import org.codehaus.jackson.map.ObjectWriter;
 
 import cn.ac.iscas.iel.csdtp.data.Frame;
 
 /**
  * Use socket as the output channel
  * 
  * @Project CSDTP
  * @Package cn.ac.iscas.iel.csdtp.channel
  * @Class SocketOutputChannel
  * @Date Apr 24, 2013 5:20:17 PM
  * @author voidmain
  */
 public class SocketOutputChannel extends OutputChannel {
 
 	protected Socket mSenderSocket;
 	protected DataOutputStream mOutStream;
 
 	protected String mIp;
 	protected int mPort;
 
 	public SocketOutputChannel(String ip, int port) {
 		super();
 		mIp = ip;
 		mPort = port;
 	}
 
 	@Override
 	public void prepare() {
 		try {
 			mSenderSocket = new Socket(mIp, mPort);
 			mSenderSocket.setTcpNoDelay(true);
 			mSenderSocket.setKeepAlive(true);
 
 			mOutStream = new DataOutputStream(mSenderSocket.getOutputStream());
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void sendFrame(Frame frameData) {
 		try {
 			if (mMapper.canSerialize(frameData.getClass())) {
 				ObjectWriter writer = mMapper.writer();
				String jsonContent = writer.writeValueAsString(frameData) + "\n";
 				mOutStream.writeUTF(jsonContent);
 				mOutStream.flush();
 			} else {
 				System.out.println("cannot serialize the object");
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void cleanUp() {
 		if (mSenderSocket != null) {
 			try {
 				mSenderSocket.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 				mSenderSocket = null;
 			}
 		}
 	}
 
 }
