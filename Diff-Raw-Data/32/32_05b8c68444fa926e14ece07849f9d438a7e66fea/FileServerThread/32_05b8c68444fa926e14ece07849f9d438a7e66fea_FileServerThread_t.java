 package org.AndroidShareApp.core;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.Socket;
 
 import android.util.Log;
 import android.widget.ProgressBar;
 
 public class FileServerThread implements Runnable {
 
 	private String mPath;
 	private Socket mSocket;
 	private int mSize;
 	private ProgressBar mProgressBar;
 
 	public FileServerThread(String transfer, Socket socket) {
 		mPath = transfer.substring(transfer.indexOf(' ') + 1);
		mPath = NetworkManager.getInstance().getSharedByMeItemFullPath(mPath);
 		mSocket = socket;
 
 		Log.i("FileServerThread", "Created transfer with path \"" + mPath
 				+ "\" on socket \"" + mSocket + "\".");
 	}
 
 	@Override
 	public void run() {
 		double currentProgress = 0.0;
 		try {
 			int BLOCK_SIZE = NetworkProtocol.BLOCK_SIZE;
 
 			/* First, we read the file. */
 			File currentFile = new File(mPath);
 			mSize = (int) currentFile.length();
 			byte[] bytesToSend = new byte[mSize];
 
 			FileInputStream fis = new FileInputStream(currentFile);
 			BufferedInputStream in = new BufferedInputStream(fis);
 
 			in.read(bytesToSend, 0, bytesToSend.length);
 			in.close();
 
 			/* Then, we send it, BLOCK_SIZE per BLOCK_SIZE. */
 			OutputStream out = mSocket.getOutputStream();
 			currentProgress = 0.0;
 
 			int nPackets = (int) Math.ceil(((double) mSize)
 					/ ((double) BLOCK_SIZE));
 
 			Log.i("FileServerThread", "Started transfer on socket \"" + mSocket
 					+ "\".");
 
 			for (int i = 0; i < nPackets; i++) {
 				int sizeToSend = (mSize - i * BLOCK_SIZE >= BLOCK_SIZE) ? BLOCK_SIZE
 						: mSize - i * BLOCK_SIZE;
 
 				out.write(bytesToSend, i * BLOCK_SIZE, sizeToSend);
 				out.flush();
 
 				currentProgress += ((double) BLOCK_SIZE) / ((double) mSize);
 
 				synchronized (mProgressBar) {
 					if (mProgressBar != null) {
 						mProgressBar.setProgress((int) Math
 								.round(currentProgress));
 					}
 				}
 			}
 
 			out.close();
 			mSocket.close();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return;
 		}
 
 		Log.i("FileServerThread", "Ended transfer on socket \"" + mSocket
 				+ "\".");
 	}
 
 	public void registerCallback(ProgressBar progressBar) {
 		synchronized (mProgressBar) {
 			mProgressBar = progressBar;
 		}
 	}
 }
