 package es.foxcav.foxcaves.api;
 
 import java.io.*;
 import java.net.HttpURLConnection;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 public class Uploader extends Authenticatable {
 	private UploadThread uploadThread;
 	private Thread uploadCheckerThread;
 
 	private boolean isRunning = false;
 
 	private Queue<UploadInfo> uploads = new ConcurrentLinkedQueue<UploadInfo>();
 
 	public void uploadStarted(UploadInfo uploadInfo) { }
 	public void uploadFinished(UploadInfo uploadInfo, boolean success, String error_or_link) { }
 	public void uploadProgress(UploadInfo uploadInfo, double progress) { }
 
 	public Uploader() {
 		super();
 		init();
 	}
 
 	public Uploader(String username, String password) {
 		super(username, password);
 		init();
 	}
 
 	private void init() {
 		isRunning = true;
 
 		uploadCheckerThread = new Thread() {
 			public void run() {
 				while (isRunning) {
 					do {
 						try {
 							Thread.sleep(100);
 						} catch(Exception e) { }
 					} while(uploadThread != null && uploadThread.isAlive());
 
 					if (uploads.size() > 0) {
 						uploadThread = new UploadThread(uploads.poll());
 						uploadThread.start();
 					}
 				}
 			}
 		};
 		uploadCheckerThread.start();
 	}
 
 	private void stop() {
 		isRunning = false;
 	}
 
	private UploadInfo makeUploadInfo(String filename, InputStream stream) {
 		if (username == null) {
			throw new RuntimeException("You must specify credentials either in the ctor or with setCredentials first!");
 		}
 
 		char c;
 		char[] cna = filename.toCharArray();
 		for (int i = 0; i < cna.length; i++) {
 			c = cna[i];
 			if (c == '<' || c == '>' || c == '\n' || c == '\t' || c == '\r' || c == '\0' || c == '#') {
 				cna[i] = '_';
 			}
 		}
 		filename = new String(cna);
 
 		return new UploadInfo(filename, stream);
 	}
 
 	public final String queueSync(String filename, InputStream stream) {
		UploadInfo uploadInfo = makeUploadInfo(filename, stream);
 		return uploadThreadInt(uploadInfo);
 	}
 
 	public final UploadInfo queueAsync(String filename, InputStream stream) {
		UploadInfo uploadInfo = makeUploadInfo(filename, stream);
 		uploads.add(uploadInfo);
 		return uploadInfo;
 	}
 
 	private class UploadThread extends Thread {
 		private final UploadInfo uploadInfo;
 
 		private UploadThread(UploadInfo uploadInfo) {
 			this.uploadInfo = uploadInfo;
 		}
 
 		public void run() {
 			uploadThreadInt(uploadInfo);
 		}
 	}
 
 	private String uploadThreadInt(UploadInfo uploadInfo) {
 		String filename = uploadInfo.filename;
 		InputStream stream = uploadInfo.stream;
 
 		try {
 			stream.reset();
 		}
 		catch(Exception e) { }
 
 		uploadStarted(uploadInfo);
 
 		try {
 			int length = stream.available();
 
 			HttpURLConnection httpURLConnection = makeHttpURLConnection("create?" + filename);
 			httpURLConnection.setRequestMethod("PUT");
 			httpURLConnection.setDoOutput(true);
 			httpURLConnection.setRequestProperty("Content-Length", String.valueOf(length));
 
 			OutputStream outputStream = httpURLConnection.getOutputStream();
 
 			byte[] buffer = new byte[256];
 			int readb;
 			int totalread = 0;
 			while (isRunning) {
 				readb = stream.read(buffer);
 				if (readb <= 0) break;
 				totalread += readb;
 				outputStream.write(buffer, 0, readb);
 				outputStream.flush();
 
 				uploadProgress(uploadInfo, (((double)totalread) / ((double)length)));
 			}
 
 			if(!isRunning) {
 				uploadFinished(uploadInfo, false, "Upload cancelled");
 				return null;
 			}
 
 			outputStream.close();
 			stream.close();
 
 			httpURLConnection.connect();
 
 			int responseCode = httpURLConnection.getResponseCode();
 			BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
 			filename = reader.readLine();
 			reader.close();
 
 			if(responseCode != 200) {
 				uploadFinished(uploadInfo, false, "Error uploading: " + filename + " (" + responseCode + ")");
 				return null;
 			}
 
 			filename = Constants.MAINURL + filename;
 
 			uploadFinished(uploadInfo, true, filename);
 
 			return filename;
 		}
 		catch (Exception e) {
 			uploadFinished(uploadInfo, false, "Internal error uploading: " + e.toString());
 		}
 
 		return null;
 	}
 
 	public static class UploadInfo
 	{
 		private static long lastUploadID = 0;
 
 		public final String filename;
 		public final long uploadID;
 
 		private final InputStream stream;
 
 		private UploadInfo(String filename, InputStream stream)
 		{
 			this.filename = filename;
 			this.stream = stream;
 			this.uploadID = lastUploadID++;
 		}
 	}
 }
