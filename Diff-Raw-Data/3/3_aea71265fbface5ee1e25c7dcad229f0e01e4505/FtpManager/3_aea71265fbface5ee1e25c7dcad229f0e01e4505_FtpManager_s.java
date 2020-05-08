 package com.luzi82.clockcam;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.SocketException;
 
 import org.apache.commons.net.ftp.FTP;
 import org.apache.commons.net.ftp.FTPClient;
 
 public class FtpManager extends Thread {
 
 	public static final long UPLOAD_TIME_DELAY = 30000;
 
 	public final String mServer;
 	public final String mUsername;
 	public final String mPassword;
 	public final String mRemoteDirectory;
 	public final String mLocalDirectory;
 	public FTPClient mFtp;
 	// public LinkedList<SendFileTask> mSendFileTaskQueue = new
 	// LinkedList<SendFileTask>();
 
 	public boolean mShouldRun = true;
 	public long mWatchDogTime;
 	private WatchDog mWatchDog;
 
 	public FtpManager(String aServer, String aUsername, String aPassword, String aRemoteDirectory, String aLocalDirectory) {
 		mServer = aServer;
 		mUsername = aUsername;
 		mPassword = aPassword;
 		mRemoteDirectory = aRemoteDirectory;
 		mLocalDirectory = aLocalDirectory;
 	}
 
 	public void run() {
 		ClockCamActivity.d("ftp run");
 		File localDir = new File(mLocalDirectory);
 		mWatchDogTime = System.currentTimeMillis();
 		mWatchDog = new WatchDog();
 		mWatchDog.start();
 		while (mShouldRun) {
 			// connect loop
 			ClockCamActivity.d("ftp connect");
 			try {
 				doConnect();
 				while (mShouldRun) {
 					// send file loop
 					ClockCamActivity.d("ftp send");
 					File[] fileList = null;
 					while (mShouldRun) {
 						// check file loop
 						ClockCamActivity.d("ftp check");
 						updateWatchdogTimer();
 						fileList = localDir.listFiles();
 						if ((fileList != null) && (fileList.length > 0)) {
 							break;
 						}
 						synchronized (this) {
 							try {
 								wait(30000);
 							} catch (InterruptedException e) {
 							}
 						}
 					}
 					if (!mShouldRun) {
 						break;
 					}
 					if (fileList == null)
 						continue;
 					updateWatchdogTimer();
 					for (File f : fileList) {
 						long now = System.currentTimeMillis();
 						long fTime = f.lastModified();
 						ClockCamActivity.d(String.format("ftp file %s", f.getName()));
 						if (now < fTime + UPLOAD_TIME_DELAY)
 							continue;
 						ClockCamActivity.d("ftp upload");
 						sendFile(f, mRemoteDirectory);
 						f.delete();
 						updateWatchdogTimer();
 					}
 					updateWatchdogTimer();
 					synchronized (this) {
 						try {
 							wait(5000);
 						} catch (InterruptedException e) {
 						}
 					}
 					updateWatchdogTimer();
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 				doDisconnect();
 			}
 			if (!mShouldRun) {
 				break;
 			}
 			synchronized (this) {
 				try {
 					wait(5000);
 				} catch (InterruptedException e) {
 				}
 			}
 		}
 		doDisconnect();
 	}
 
 	public void stopLater() {
 		mShouldRun = false;
 	}
 
 	private synchronized void doConnect() throws SocketException, IOException {
 		ClockCamActivity.d(String.format("server %s", mServer));
 		ClockCamActivity.d(String.format("username %s", mUsername));
 		ClockCamActivity.d(String.format("password %s", mPassword));
 		mFtp = new FTPClient();
 		// mFtp.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
 		mFtp.connect(mServer);
 		mFtp.login(mUsername, mPassword);
 		mFtp.setFileType(FTP.BINARY_FILE_TYPE);
 		// mFtp.enterLocalActiveMode();
 		mFtp.enterLocalPassiveMode();
 		mWatchDogTime = System.currentTimeMillis();
 	}
 
 	private synchronized void doDisconnect() {
 		if (mFtp != null) {
 			try {
 				mFtp.disconnect();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			mFtp = null;
 		}
 	}
 
 	// public synchronized void connect() {
 	// if (mShouldRun) {
 	// return;
 	// }
 	// mShouldRun = true;
 	// maintain();
 	// }
 
 	private void sendFile(File aFile, String aRemoteDirectory) throws IOException {
 		if (mFtp == null) {
 			throw new IOException();
 		}
 		BufferedInputStream bis = null;
 		try {
 			String remotePath = aRemoteDirectory + "/" + aFile.getName();
 
 			boolean done;
 
 			mFtp.makeDirectory(mRemoteDirectory);
 			mFtp.changeWorkingDirectory(mRemoteDirectory);
 			ClockCamActivity.d(mFtp.printWorkingDirectory());
 
 			// done = mFtp.setFileType(FTPClient.BINARY_FILE_TYPE);
 			// ClockCamActivity.d("setFileType " + done);
 
 			bis = new BufferedInputStream(new FileInputStream(aFile));
 			// mFtp.storeFile(aFile.getName(), bis);
 			done = mFtp.storeFile(remotePath, bis);
 			bis.close();
 			mFtp.noop();
 			// ClockCamActivity.d("storeFile " + done);
 			if (!done) {
 				throw new IOException();
 			}
 		} catch (IOException ioe) {
 			if (bis != null) {
 				try {
 					bis.close();
 				} catch (IOException ioe2) {
 				}
 			}
 			throw ioe;
 		}
 	}
 
 	private class WatchDog extends Thread {
 		public void run() {
 			while (mShouldRun) {
 				ClockCamActivity.d("WatchDog turn");
 				synchronized (this) {
 					try {
 						wait(30000);
 					} catch (InterruptedException e) {
 					}
 				}
 				if (!mShouldRun) {
 					break;
 				}
 				long now = System.currentTimeMillis();
 				if (now > mWatchDogTime + 120000) {
 					ClockCamActivity.d("WatchDog timeout, disconnect");
 					doDisconnect();
 				}else{
 					ClockCamActivity.d("WatchDog pass");
 				}
 			}
 		}
 	}
 
 	private void updateWatchdogTimer() throws IOException {
 		ClockCamActivity.d("updateWatchdogTimer");
 		mFtp.noop();
 		mWatchDogTime = System.currentTimeMillis();
 	}
 
 }
