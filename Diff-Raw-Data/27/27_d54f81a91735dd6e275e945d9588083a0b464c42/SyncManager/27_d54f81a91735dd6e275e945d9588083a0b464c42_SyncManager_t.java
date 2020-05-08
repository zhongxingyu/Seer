 package ece454p1;
 
 import java.io.*;
 import java.util.*;
 
 public class SyncManager {
 	private LocalFileManager local;
 	private HashSet<ChunkedFile> globalFiles;
 	
 	public SyncManager() {
 		local = new LocalFileManager();
 		globalFiles = local.getLocalFiles();
 	}
 	
 	/**
 	 * Imports the file at filename into the global system file list, then pushes the new list to any peers
 	 */
 	public synchronized int addFileToSystem(String filename) {
 		if (filename.contains(":")) {
 			System.out.println("Illegal ':' in filename " + filename);
 			return -1;
 		}
 		File file = new File(filename);
 		
 		// Don't allow duplicate filenames
 		for (ChunkedFile globalFile : globalFiles) {
 			if (globalFile.getName().equals(file.getName())) {
 				System.out.println("File " + file.getName() + " already exists in system");
 				return 1;
 			}
 		}
 		
 		// Import file
 		ChunkedFile newChunkedFile = local.importFile(filename);
 		if (newChunkedFile == null) {
 			return -1;
 		}
 		globalFiles.add(newChunkedFile);
 		
 		if (Peer.currentState == Peer.State.connected){
 			for (ProxyPeer p : Peer.proxyPeerList){
 				if (p.connected){
 					p.send("update");
 				}
 			}
 		}
 		return 0;
 	}
 	
 	/**
 	 * @return A string of colon separated items consisting of <file, size> pairs
 	 */
 	public synchronized String getFileList() {
 		ArrayList<ChunkedFile> files = new ArrayList<ChunkedFile>(globalFiles);
 		if (files.size() == 0) {
 			return "";
 		}
 		
 		StringBuffer buffer = new StringBuffer();
 		buffer.append(files.get(0).getName());
 		buffer.append(":");
 		buffer.append(Long.toString(files.get(0).getSize()));
 		for (int i = 1; i < files.size(); i++) {
 			buffer.append(":");
 			buffer.append(files.get(i).getName());
 			buffer.append(":");
 			buffer.append(Long.toString(files.get(i).getSize()));
 		}
 		return buffer.toString();
 	}
 	
 	/**
 	 * @return A string of colon separated chunk identifiers
 	 */
 	public synchronized String getChunkList() {
 		ArrayList<String> chunks = new ArrayList<String>(local.getLocalChunks());
 		if (chunks.size() == 0) {
 			return "";
 		}
 		
 		StringBuffer buffer = new StringBuffer();
 		buffer.append(chunks.get(0));
 		for (int i = 1; i < chunks.size(); i++) {
 			buffer.append(":");
 			buffer.append(chunks.get(i));
 		}
 		return buffer.toString();
 	}
 	
 	/**
 	 *
 	 */
 	public synchronized void parseFileList(String fileList) {
 		String[] items = fileList.split(":");
 		HashSet<ChunkedFile> files = new HashSet<ChunkedFile>();
 		
 		// Validate the file list
 		if (items.length % 2 != 0) {
 			System.out.println("Missing item in pair<file, size> list");
 			return;
 		}
 		for (int i = 0; i < items.length; i += 2) {
 			long size = 0;
 			try {
 				size = Long.parseLong(items[i + 1]);
 			} catch (NumberFormatException e) {
 				System.out.println("Invalid file size " + items[i + 1]);
 				return;
 			}
 			files.add(new ChunkedFile(items[i], size));
 		}
 		for (ChunkedFile file : files) {
 			for (ChunkedFile globalFile : globalFiles) {
 				if (globalFile.getName().equals(file.getName())) {
 					if (globalFile.getSize() != file.getSize()) { // Check that file sizes match
 						System.out.println("File sizes don't match: " + globalFile.toString() + " (existing), and " + file.toString() + " (new)");
 						return;
 					}
 				}
 			}
 		}
 		
 		// TODO:
 	}
 	
 	/**
 	 * 
 	 */
 	public synchronized void parseChunkList(String ip, int port, String chunkList) {
 		String[] chunks = chunkList.split(":");
 		
 		// Validate the chunk names
 		for (String chunk : chunks) {
 			String filename = ChunkedFile.filenameFromChunkName(chunk);
 			int cn = ChunkedFile.numberFromChunkName(chunk);
 			if (filename == null || cn < 0) {
 				System.out.println("Invalid chunk " + chunk + " in list");
 				return;
 			}
 		}
 		
 		// TODO:
 	}
 	
 	/**
 	 * @return A variably sized array of bytes or null if the chunk cannot be read
 	 */
 	public synchronized byte[] readChunkData(String chunk) {
 		// Parse the chunk name
 		String filename = ChunkedFile.filenameFromChunkName(chunk);
 		int cn = ChunkedFile.numberFromChunkName(chunk);
 		if (filename == null || cn < 0) {
 			System.out.println("Error parsing chunk name " + chunk);
 			return null;
 		}
 		
 		// Check that there is a global file for this chunk
 		ChunkedFile chunkedFile = null;
 		for (ChunkedFile file : globalFiles) {
 			if (file.getName().equals(filename)) {
 				chunkedFile = file;
 				break;
 			}
 		}
 		if (chunkedFile == null) {
 			System.out.println("Chunk " + chunk + " does not have corresponding file in system");
 			return null;
 		}
 		if (cn >= chunkedFile.numberOfChunks()) {
 			System.out.println("Chunk number " + cn + " out of bounds for file " + chunkedFile.toString());
 			return null;
 		}
 		
 		// Check that it is available
 		HashSet<String> localChunks = local.getLocalChunks();
 		if (!localChunks.contains(chunk)) {
 			System.out.println("Error chunk " + chunk + " not available locally");
 			return null;
 		}
 		
 		return local.readChunk(chunkedFile, cn);
 	}
 	
 	public void writeChunkData(String chunk, byte[] data) {
 		// Parse the chunk name
 		String filename = ChunkedFile.filenameFromChunkName(chunk);
 		int cn = ChunkedFile.numberFromChunkName(chunk);
 		if (filename == null || cn < 0) {
 			System.out.println("Error parsing chunk name " + chunk);
 			return;
 		}
 		
 		// Check that there is a global file for this chunk
 		ChunkedFile chunkedFile = null;
 		for (ChunkedFile file : globalFiles) {
 			if (file.getName().equals(filename)) {
 				chunkedFile = file;
 				break;
 			}
 		}
 		if (chunkedFile == null) {
 			System.out.println("Chunk " + chunk + " does not have corresponding file in system");
 			return;
 		}
 		if (cn >= chunkedFile.numberOfChunks()) {
 			System.out.println("Chunk number " + cn + " out of bounds for file " + chunkedFile.toString());
 			return;
 		}
 		
 		local.writeChunk(chunkedFile, cn, data);
 		
		// Send updates
 		if (Peer.currentState == Peer.State.connected){
 			for (ProxyPeer p : Peer.proxyPeerList){
 				if (p.connected){
 					p.send("update");
 				}
 			}
 		}
		
		// TODO: decide next chunks to download
 	}
 }
