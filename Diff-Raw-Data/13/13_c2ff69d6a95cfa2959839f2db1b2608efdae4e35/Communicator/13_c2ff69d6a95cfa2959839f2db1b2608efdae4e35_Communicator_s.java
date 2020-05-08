 package nl.groep5.xchange.communication;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import javafx.collections.FXCollections;
 import javafx.collections.ObservableList;
 
 import javax.naming.CommunicationException;
 
 import nl.groep5.xchange.Downloader;
 import nl.groep5.xchange.Settings;
 import nl.groep5.xchange.models.DownloadableFile;
 import nl.groep5.xchange.models.Peer;
 
 public class Communicator {
 	private static ObservableList<Peer> peers = FXCollections
 			.observableArrayList();
 
 	private static ObservableList<DownloadableFile> searchResults = FXCollections
 			.observableArrayList();
 
 	private static NameServer nameServer = new NameServer(Settings
 			.getInstance().getNameServerIp(), Settings.getNameServerPort());
 
 	private static Router router = new Router(Settings.getInstance()
 			.getRouterIp(), Settings.getRouterPort());
 
 	private static StorageServer storageServer = new StorageServer(Settings
 			.getInstance().getStorageServerIp(),
 			Settings.getStorageServerPort());
 
 	public static void resetConnections() {
 		nameServer = new NameServer(Settings.getInstance().getNameServerIp(),
 				Settings.getNameServerPort());
 
 		router = new Router(Settings.getInstance().getRouterIp(),
 				Settings.getRouterPort());
 
 		storageServer = new StorageServer(Settings.getInstance()
 				.getStorageServerIp(), Settings.getStorageServerPort());
 	}
 
 	public static boolean signUpToNameServer() {
 		try {
 			String response = nameServer.sendCommand("ADD");
 			return response.equals("OK");
 		} catch (IOException | CommunicationException e) {
 			if (Settings.DEBUG) {
 				System.out.println("could not connect to nameserver.");
 			}
 		}
 		return false;
 	}
 
 	public static void updatePeers() {
 		try {
 			String result = nameServer.sendCommand("LIST");
 			peers.clear();
 			for (String s : result.split(Settings.getSplitChar())) {
 				peers.add(new Peer(s));
 			}
 		} catch (IOException e) {
 			if (Settings.DEBUG) {
 				System.out.println("Failed to update peerlist.");
 			}
 		} catch (CommunicationException e) {
 			if (Settings.DEBUG) {
 				System.out.println("Failed to update peerlist. Server error.");
 			}
 		}
 	}
 
 	public static ObservableList<DownloadableFile> search(String pattern) {
 		searchResults.clear();
 		for (Peer peer : peers) {
 			try {
 				searchOnPeer(peer, pattern);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return searchResults;
 	}
 
 	public static void searchOnPeer(Peer peer, String pattern)
 			throws UnknownHostException, IOException {
 		Socket socket = new Socket(peer.getIp(), peer.getPort());
 
 		BufferedReader bufferedReader = new BufferedReader(
 				new InputStreamReader(socket.getInputStream()));
 		PrintWriter printWriter = new PrintWriter(socket.getOutputStream(),
 				true);
 		printWriter.println("SEARCH " + pattern);
 
 		String line = null;
 		do {
 			line = bufferedReader.readLine();
 		} while (line == null);
 
 		if (line.startsWith("FAIL"))
 			throw new IOException();
 
 		String[] result = line.split(Settings.getSplitChar());
 
 		if (result.length % 2 == 0) {
 			for (int i = 0; i < result.length; i += 2) {
 
 				final DownloadableFile downloadableFile = new DownloadableFile(
 						result[i], result[i + 1], peer);
 
 				searchResults.add(downloadableFile);
 			}
 		}
 		if (Settings.DEBUG) {
 			System.out.println("RESULT: " + line);
 		}
 	}
 
 	public static ObservableList<Peer> getPeers() {
 		return peers;
 	}
 
 	public static ObservableList<DownloadableFile> getSearchResults() {
 		return searchResults;
 	}
 
 	public static void clearSearchResults() {
 		searchResults.clear();
 	}
 
 	public static void startDownload(DownloadableFile downloadableFile) {
 		Downloader downLoader = new Downloader(downloadableFile);
 		downloadableFile.setDownloader(downLoader);
 		downLoader.start();
 	}
 
 	public static byte[] GetBlockFromPeer(DownloadableFile downloadableFile,
 			int curBlock, int size) throws UnknownHostException, IOException {
 
 		Socket socket = new Socket(downloadableFile.getPeer().getIp(),
 				downloadableFile.getPeer().getPort());
 
 		BufferedInputStream bufferedInputStream = new BufferedInputStream(
 				socket.getInputStream(), Settings.getBlockSize());
 		PrintWriter printWriter = new PrintWriter(socket.getOutputStream(),
 				true);
 
 		printWriter.println("GET " + downloadableFile.getRealFileName()
 				+ Settings.getSplitChar() + curBlock);
 
 		byte[] byteArray = new byte[size];
 
 		int n = -1;
 		while (n < 1) {
 			n = bufferedInputStream.read(byteArray, 0, size);
 		}
 
 		return byteArray;
 	}
 
 	public static boolean setRouterSettings() {
 		try {
 			String result = router.sendCommand("IS ROUTER");
 			if (result.equals("ok"))
 				return true;
 		} catch (CommunicationException | IOException e) {
 			e.printStackTrace();
 		}
 		// TODO change to false
 		return true;
 	}
 
 	public static void startRouterDownload() {
 		// TODO
 	}
 
 	public static void stopRouterDownload() {
 		// TODO
 		mergeRouterDownload();
 	}
 
 	private static void mergeRouterDownload() {
 		// TODO
 	}
 
 	public static boolean testStorageServer() {
 		try {
 			String result = storageServer.sendCommand("IS STORAGE SERVER");
 			if (result.equals("ok"))
 				return true;
 		} catch (CommunicationException | IOException e) {
 			e.printStackTrace();
 		}
 		// TODO change to false
 		return true;
 	}
 
 	public static void searchPeerForBlock(DownloadableFile downloadableFile,
 			int curBlock, int size) {
 		for (Peer peer : peers) {
 			try {
 				downloadableFile.setPeer(peer);
 				GetBlockFromPeer(downloadableFile, curBlock, size);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
