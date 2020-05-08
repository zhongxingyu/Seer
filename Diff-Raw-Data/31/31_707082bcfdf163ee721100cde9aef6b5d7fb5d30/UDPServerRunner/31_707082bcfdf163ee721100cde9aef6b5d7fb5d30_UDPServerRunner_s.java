 package com.king.cai.service;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.SocketException;
 import java.nio.ByteBuffer;
 
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 import com.king.cai.KingCAIConfig;
 import com.king.cai.service.FirableRunner;
 
 
 public class UDPServerRunner extends FirableRunner{
 	private DatagramSocket mDatagramSocket = null;
 	private DatagramPacket mDatagramPacket = null;
 	private ByteBuffer mMsgBuffer = ByteBuffer.allocate(8192);
 	private int mPort;
 	
 	public UDPServerRunner(Handler innerHandler, int port){
 		super(innerHandler);
 		mPort = port;
 		try {
 			if (mDatagramSocket != null){
 				mDatagramSocket.close();
 				mDatagramSocket = null;
 			}
 			mDatagramSocket = new DatagramSocket(mPort);
 			
 			mDatagramPacket = null;
 			mDatagramPacket = new DatagramPacket(mMsgBuffer.array(), mMsgBuffer.array().length);
 		}catch (SocketException e){
 			KingService.getLogService().addLog(e.toString());			
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	protected void doRun(){
 		try {
 			mMsgBuffer.clear();
 			if (mDatagramSocket != null && mDatagramPacket != null){ 
 				mDatagramSocket.receive(mDatagramPacket);
 				String peerAddr = mDatagramPacket.getAddress().getHostAddress().toString();
 				Log.d("UDPReceiver", mDatagramPacket.getAddress().getHostAddress().toString()
 						+ ":"+ new String(mDatagramPacket.getData(), KingCAIConfig.mCharterSet));
 				
 				String rawmsg = new String(mDatagramPacket.getData(), mDatagramPacket.getOffset(), 
 						mDatagramPacket.getLength(), KingCAIConfig.mCharterSet);
 				
				fireMessage(contructTextBundle(peerAddr, ByteBuffer.wrap(rawmsg.trim().getBytes())));
 			}
 		}catch(IOException e) {
 			KingService.getLogService().addLog(e.toString());			
 			e.printStackTrace();
 		}
 		mRunning = false;
 	}
 
 	@Override
 	protected void onExit() {
 		if (mDatagramSocket != null) {
 			mDatagramSocket.close();
 			mDatagramSocket = null;
 		}		
 		mDatagramPacket = null;
 		mMsgBuffer = null;
 	}
 
 	@Override
 	protected void onStart() {
 		// TODO Auto-generated method stub
 		
 	}
 }
