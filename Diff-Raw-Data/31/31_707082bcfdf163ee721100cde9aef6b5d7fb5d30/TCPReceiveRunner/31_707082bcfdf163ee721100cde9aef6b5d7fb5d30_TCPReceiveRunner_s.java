 package com.king.cai.service;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.nio.ByteBuffer;
 
 import com.king.cai.KingCAIConfig;
 import com.king.cai.examination.DownloadManager;
 
 import android.os.Bundle;
 import android.os.Message;
 
 public class TCPReceiveRunner extends FirableRunner{
 	private InputStream mInputStream = null;
 	private ByteBuffer mReceiveBuf = ByteBuffer.allocate(128 * 1024);
 	private int mPort = 0;
 	private String mPeerAddr = null;
 	private int mExpectSize = 0;
 	
 	public TCPReceiveRunner(Handler innerHandler, 
 						InputStream is, String peer, int port, boolean bCache) {
 		super(innerHandler);
 		mInputStream = is;
 		mPeerAddr = peer;
 		mPort = port;
 	}
 
 	@Override
 	protected void doRun() {
 		try {
 			mReceiveBuf.clear();
 			int totalReadSize = 0;
 			int subReadSize = -1;
 			do {
 				subReadSize = mInputStream.read(mReceiveBuf.array(), totalReadSize, mExpectSize); 
 				if (subReadSize > 0){
 					totalReadSize += subReadSize;
 				}
 			}while (subReadSize > 0 && mExpectSize > 0 && totalReadSize < mExpectSize);
 
 			if (subReadSize > 0){
 				if (mPort != KingCAIConfig.mTextSendPort){
 					Bundle bundle = contructBinaryBundle(mReceiveBuf);
 					fireMessage(bundle);
 				}else{
 					Bundle bundle = contructTextBundle(mPeerAddr, mReceiveBuf, totalReadSize);
 					fireMessage(bundle);
 				}
 			}
 		} catch (IOException e) {
 			KingService.getLogService().addLog(e.toString());
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	protected void onExit() {
 		mReceiveBuf = null;
 	}
 
 	@Override
 	protected void onStart() {		
 	}
 	
 	public void updateExpectSize(Integer size){
 		mExpectSize = size;
 	}
 }
