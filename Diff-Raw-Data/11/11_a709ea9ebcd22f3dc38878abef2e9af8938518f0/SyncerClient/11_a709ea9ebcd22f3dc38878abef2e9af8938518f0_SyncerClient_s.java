 package org.upsuper.playlistsyncer.client;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.nio.ByteBuffer;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import android.util.Log;
 import android.util.Pair;
 
 public class SyncerClient implements Runnable {
 
 	protected static final String PROTOCOL_NAME = "PLSY/1.0";
 	private static final int CONFIRM_CODE_LENGTH = 5;
 	private static final String CONFIRM_CODE_ALPHABET =
 			"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
 	private static final int BUFFER_SIZE = 4096;
 
 	protected static final List<String> AUDIO_EXTENSIONS =
 			Arrays.asList(new String[] {"mp3", "m4a"});
 
 	private InetAddress dstAddress;
 	private int dstPort;
 	private InputStream in;
 	private PrintStream out;
 	private byte[] buffer = new byte[BUFFER_SIZE];
 	MessageDigest digester;
 
 	private List<FileItem> fileList;
 	private List<FileItem> downloadList;
 
 	ClientListener listener;
 
 	public SyncerClient(InetAddress address, int port, ClientListener listener) {
 		dstAddress = address;
 		dstPort = port;
 		this.listener = listener;
 		try {
 			digester = MessageDigest.getInstance("MD5");
 		} catch (NoSuchAlgorithmException e) { }
 	}
 
 	@Override
 	public void run() {
 		try {
			Socket socket = new Socket(dstAddress, dstPort);
 			in = new BufferedInputStream(socket.getInputStream());
 			out = new PrintStream(socket.getOutputStream());
 
 			String confirmCode = generateConfirmCode();
 			listener.onConfirmCodeGenerated(confirmCode);
 			writeHeader(confirmCode);
 
 			readFileList();
 			removeUnusedFiles();
 			prepareDownloadList();
 			receiveFiles();
 			receivePlaylists();
 		} catch (IOException e) {
 			listener.onClientError(e);
 		}
 	}
 
 	protected String generateConfirmCode() {
 		Random rand = new Random();
 		char[] code = new char[CONFIRM_CODE_LENGTH];
 		for (int i = 0; i < CONFIRM_CODE_LENGTH; i++) {
 			int pos = rand.nextInt(CONFIRM_CODE_ALPHABET.length());
 			code[i] = CONFIRM_CODE_ALPHABET.charAt(pos);
 		}
 		return new String(code);
 	}
 
 	protected void writeHeader(String confirmCode) throws IOException {
 		out.println(PROTOCOL_NAME + " " + confirmCode);
 		if (out.checkError())
 			throw new IOException("writeHeader");
 	}
 
 	protected String readLine() throws IOException {
 		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
 		while (true) {
 			int b = in.read();
 			if (b == '\n')
 				break;
 			buffer.put((byte) b);
 		}
 		return new String(buffer.array(), 0, buffer.position(), "UTF-8");
 	}
 
 	protected Pair<String, String> splitLine(String line) throws IOException {
 		int seperator = line.lastIndexOf(' ');
 		return new Pair<String, String>(
 				line.substring(0, seperator),
 				line.substring(seperator + 1));
 	}
 
 	protected void readFileList() throws IOException {
 		fileList = new ArrayList<FileItem>();
 		while (true) {
 			String line = readLine();
 			if (line.isEmpty()) break;
 
 			Pair<String, String> splitLine = splitLine(line);
 			FileItem item = new FileItem();
 			item.fileId = splitLine.first;
 			item.size = Integer.valueOf(splitLine.second);
 			item.file = new File(Constants.MUSIC_DIRECTORY, item.fileId);
 			fileList.add(item);
 		}
 	}
 
 	protected void removeUnusedFiles(File file, Set<String> files) {
 		if (file.isDirectory()) {
 			for (File f : file.listFiles())
 				removeUnusedFiles(f, files);
 		} else {
 			String name = file.getName();
 			int dotpos = name.lastIndexOf('.');
 			if (dotpos >= 0) {
 				String ext = name.substring(dotpos + 1).toLowerCase();
 				if (!AUDIO_EXTENSIONS.contains(ext))
 					return;
 				if (!files.contains(file.getAbsolutePath())) {
 					file.delete();
 					Log.i("Client", "Removed: " + file.getAbsolutePath());
 				}
 			}
 		}
 	}
 
 	protected void removeUnusedFiles() {
 		Set<String> files = new HashSet<String>(fileList.size());
 		for (FileItem item : fileList)
 			files.add(item.file.getAbsolutePath());
 		removeUnusedFiles(Constants.MUSIC_DIRECTORY, files);
 	}
 
 	protected void prepareDownloadList() throws IOException {
 		downloadList = new LinkedList<FileItem>();
 		for (FileItem item : fileList) {
 			if (!item.file.exists() || item.file.length() != item.size)
 				downloadList.add(item);
 		}
 
 		List<String> list = new ArrayList<String>(downloadList.size());
 		for (FileItem item : downloadList)
 			list.add(item.fileId);
 		listener.onDownloadListAvailable(list);
 	}
 
 	protected void receiveFile(File file, int index, int size) throws IOException {
 		file.getParentFile().mkdirs();
 		FileOutputStream fileStream = new FileOutputStream(file);
 
 		try {
 			int received = 0;
 			while (received < size) {
 				int length = Math.min(BUFFER_SIZE, size - received);
 				int read = in.read(buffer, 0, length);
 				if (read == -1)
 					throw new IOException("receiveFile");
 				received += read;
 				fileStream.write(buffer, 0, read);
 				digester.update(buffer, 0, read);
 				listener.onDownloadProgress(index, received);
 			}
 
 			byte[] localDigest = digester.digest();
 			byte[] remoteDigest = new byte[localDigest.length];
 			in.read(remoteDigest, 0, remoteDigest.length);
 			if (!Arrays.equals(localDigest, remoteDigest))
 				throw new IOException("Wrong digest");
 		} catch (IOException e) {
 			file.delete();
 			throw e;
 		} finally {
 			fileStream.close();
 		}
 	}
 
 	protected void receiveFiles() throws IOException {
 		int size = downloadList.size();
 		for (int i = 0; i < size; i++) {
 			FileItem item = downloadList.get(i);
 			out.println(item.fileId);
 			if (out.checkError())
 				throw new IOException("sendDownloadList");
 
 			File file = new File(Constants.MUSIC_DIRECTORY, item.fileId);
 			listener.onStartDownload(i, item.size);
 			receiveFile(file, i, item.size);
 			listener.onFinishDownload(i, file);
 		}
 
 		out.println();
 		if (out.checkError())
 			throw new IOException("sendDownloadList");
 	}
 
 	protected void receivePlaylists() throws IOException {
 		Map<String, List<String>> playlists =
 				new LinkedHashMap<String, List<String>>();
 
 		while (true) {
 			String name = readLine();
 			if (name.isEmpty()) break;
 
 			List<String> playlist = new ArrayList<String>();
 			while (true) {
 				String line = readLine();
 				if (line.isEmpty()) break;
 				playlist.add(line);
 			}
 			playlists.put(name, playlist);
 		}
 
 		listener.onPlaylistsReceived(playlists);
 	}
 
 	private class FileItem {
 		public String fileId;
 		public int size;
 		public File file;
 	}
 
 	public interface ClientListener {
 
 		public void onConfirmCodeGenerated(String code);
 
 		public void onDownloadListAvailable(List<String> list);
 
 		public void onStartDownload(int index, int size);
 
 		public void onDownloadProgress(int index, int received);
 
 		public void onFinishDownload(int index, File file);
 
 		public void onPlaylistsReceived(Map<String, List<String>> playlists);
 
 		public void onClientError(Exception exception);
 
 	}
 
 }
