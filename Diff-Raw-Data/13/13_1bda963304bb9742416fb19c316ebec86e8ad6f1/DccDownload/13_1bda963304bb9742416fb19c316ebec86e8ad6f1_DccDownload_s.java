 /*
  * Project: xdccBee
  * Copyright (C) 2009 snert@snert-lab.de,
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.snertlab.xdccBee.irc;
 
 import de.snertlab.xdccBee.ui.TableItemDownload;
 
 /**
  * @author holgi
  * 
  */
 
 public class DccDownload {
 
 	public static String STATE_DOWNLOAD_FINISHED = "finished";
 	public static String STATE_DOWNLOAD_WAITING = "waiting";
 	public static String STATE_DOWNLOAD_DOWNLOAD = "downloading";
 	public static String STATE_DOWNLOAD_ABORT = "abort";
 
 	private DccPacket dccPacket;
 	private DccFileTransfer dccFileTransfer;
 	private String downloadDirFilename;
 	private TableItemDownload tableItemDownload;
 	private MyTableItemDownloadThread downloadThread;
 	private String state;
 
 	public DccDownload(DccPacket dccPacket, String downloadDirFilename) {
 		this.dccPacket = dccPacket;
 		this.downloadDirFilename = downloadDirFilename;
 		setState(STATE_DOWNLOAD_WAITING);
 	}
 
 	public String getKey() {
 		return dccPacket.toString();
 	}
 
 	public void setDccFileTransfer(DccFileTransfer dccFileTransfer) {
 		this.dccFileTransfer = dccFileTransfer;
 	}
 
 	public DccFileTransfer getDccFileTransfer() {
 		return dccFileTransfer;
 	}
 
 	public boolean matchDccFileTransfer(DccFileTransfer dccFileTransfer) {
 		// assumes that package comes from sender in order, so when you request
 		// 2 packages 1 comes first and 2 comes second else packages will be
 		// saved under wrong name
 		if (dccPacket.getSender().equals(dccFileTransfer.getNick())
 				&& state.equals(STATE_DOWNLOAD_WAITING)) {
 			return true;
 		}
 		return false;
 	}
 
 	public DccPacket getDccPacket() {
 		return dccPacket;
 	}
 
 	public String getDownloadDirFilename() {
 		return downloadDirFilename;
 	}
 
 	public void setTableItemDownload(TableItemDownload tableItemDownload) {
 		this.tableItemDownload = tableItemDownload;
 	}
 
 	public void start() {
 		downloadThread = new MyTableItemDownloadThread(this);
 		downloadThread.start();
 	}
 
 	/**
 	 * 
 	 */
 	public void stop() {
 		if (downloadThread == null) {
 			setState(STATE_DOWNLOAD_ABORT);
 		} else {
 			downloadThread.stopMe();
 		}
 	}
 
 	private class MyTableItemDownloadThread extends Thread {
 
 		private boolean stop;
 		private String state;
 		private DccDownload dccDownload;
 
 		public MyTableItemDownloadThread(DccDownload dccDownload) {
 			this.dccDownload = dccDownload;
 		}
 
 		@Override
 		public void run() {
			while ((int) dccFileTransfer.getProgress() < dccFileTransfer
					.getSize()) {
 				if (stop)
 					break;
 				tableItemDownload.getDisplay().asyncExec(new Runnable() {
 					public void run() {
 						if (stop)
 							return;
 						dccDownload.setState(STATE_DOWNLOAD_DOWNLOAD);
 						tableItemDownload
 								.updateFileTransferDisplay(dccFileTransfer);
 					}
 				});
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 			state = STATE_DOWNLOAD_FINISHED;
 			if (stop) {
 				state = STATE_DOWNLOAD_ABORT;
 			}
 			tableItemDownload.getDisplay().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					dccDownload.setState(state);
 					dccFileTransfer.close();
 				}
 			});
 		}
 
 		public void stopMe() {
 			stop = true;
 		}
 	}
 
 	public String getState() {
 		return state;
 	}
 
 	public void setState(String state) {
 		this.state = state;
 		if (tableItemDownload != null) {
 			tableItemDownload.refreshState();
 		}
 	}
 
 }
