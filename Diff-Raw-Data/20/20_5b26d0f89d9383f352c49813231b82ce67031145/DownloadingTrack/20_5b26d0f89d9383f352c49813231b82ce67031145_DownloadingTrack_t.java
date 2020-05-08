 package com.robonobo.core.api.model;
 
 import java.io.File;
 
 import com.robonobo.mina.external.buffer.PageBuffer;
 
 public class DownloadingTrack extends Track {
 	public enum DownloadStatus {
 		Paused, Downloading, Finished
 	};
 	private PageBuffer pageBuf;
 	private File file;
 	private DownloadStatus downloadStatus;
 	private int numSources;
 
 	public DownloadingTrack(Stream stream, File file, DownloadStatus status) {
 		super(stream);
 		this.file = file;
 		this.downloadStatus = status;
 		// Set our playback status here, it might get overridden
 		if(downloadStatus == DownloadStatus.Downloading)
 			setPlaybackStatus(PlaybackStatus.Downloading);
 		else if(downloadStatus == DownloadStatus.Paused)
 			setPlaybackStatus(PlaybackStatus.Queued);
 	}
 
 	public DownloadingTrack(DownloadingTrack t) {
 		super(t);
 		file = t.file;
 		downloadStatus = t.downloadStatus;
		pageBuf = t.pageBuf;
		numSources = t.numSources;
 	}
 	
 	@Override
 	public DownloadingTrack clone() {
 		return new DownloadingTrack(this);
 	}
 	
 	public long getBytesDownloaded() {
 		if(pageBuf == null)
 			return 0;
 		return pageBuf.getBytesReceived();
 	}
 
 	public File getFile() {
 		return file;
 	}
 
 	public PageBuffer getPageBuf() {
 		return pageBuf;
 	}
 
 	public void setPageBuf(PageBuffer pageBuf) {
 		this.pageBuf = pageBuf;
 	}
 
 	public DownloadStatus getDownloadStatus() {
 		return downloadStatus;
 	}
 
 	public void setDownloadStatus(DownloadStatus downloadStatus) {
 		this.downloadStatus = downloadStatus;
 	}
 	
 	public void setNumSources(int numSources) {
 		this.numSources = numSources;
 		transferStatus = new DownloadingTransferStatus(numSources);
 	}
 	
 	public int getNumSources() {
 		return numSources;
 	}
 }
