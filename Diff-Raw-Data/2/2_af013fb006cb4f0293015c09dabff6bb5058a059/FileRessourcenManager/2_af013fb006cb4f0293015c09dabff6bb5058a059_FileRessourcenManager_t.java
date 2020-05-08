 package de.ids2011.core;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 public class FileRessourcenManager implements RessourcenManager
 {
 	public static final String PREFIX = "./data/";
 	public static final String SUFFIX = ".tmp";
 	
 	private Map<Integer, Map<Integer, String>> _buffer;
 	private Map<Integer, Set<Integer>> _preparedPages;
 
 	public FileRessourcenManager()
 	{
 		_buffer = new HashMap<Integer, Map<Integer, String>>();
 		_preparedPages = new HashMap<Integer, Set<Integer>>();
 	}
 
 	@Override
 	public boolean write(int taid, int pid, String data)
 	{
 		Map<Integer, String> bufferData;
 
 		if (!_buffer.containsKey(taid)) {
 			bufferData = new HashMap<Integer, String>();
 		} else {
 			bufferData = _buffer.get(taid);
 		}
 
 		bufferData.put(pid, data);
 
 		_buffer.put(taid, bufferData);
 
 		return true;
 	}
 
 	@Override
 	public boolean prepare(int taid)
 	{
 		if ((new Random()).nextInt(100) < 80) {
 			return false;
 		}
 		
 		Map<Integer, String> bufferData;
 		Set<Integer> preparedPages = new HashSet<Integer>();
 
 		if (_buffer.containsKey(taid)) {
 			bufferData = _buffer.get(taid);
 			
 			for (Map.Entry<Integer, String> entry : bufferData.entrySet()) {
 				try {
 					String fileName = FileRessourcenManager.PREFIX + entry.getKey() + FileRessourcenManager.SUFFIX;
 					String fileData = entry.getKey()+","+entry.getValue();
 					
 					// write temporary file to disk
 					FileWriter fw = new FileWriter(fileName);
 					fw.write(fileData);
 					fw.close();
 					
 					// remember prepared page
 					preparedPages.add(entry.getKey());
 				} catch (IOException e) {
 					return false;
 				}
 				
 			}
 			
 			if (!_preparedPages.containsKey(taid)) {
 				_preparedPages.put(taid, new HashSet<Integer>());
 			}
 			
 			_preparedPages.get(taid).addAll(preparedPages);
 		}
 
 		return true;
 	}
 
 	@Override
 	public boolean commit(int taid)
 	{
 		boolean commitSuccessful = true;
 		Set<Integer> preparedPages = _preparedPages.get(taid);
 		
 		for (int page : preparedPages) {
 			String fileName = FileRessourcenManager.PREFIX + page;
 			File existing = new File(fileName);
 			File temp = new File(fileName + FileRessourcenManager.SUFFIX);
 			
 			// remove existing file if any
 			if (existing.exists()) {
 				existing.delete();
 			}
 			
 			// rename temporary file
 			commitSuccessful = commitSuccessful && temp.renameTo(existing);
 		}
 		
 		// remove data from memory
 		if (commitSuccessful) {
 			_preparedPages.remove(taid);
 			_buffer.remove(taid);
 		}
 		
 		return commitSuccessful;
 	}
 
 	@Override
 	public boolean rollback(int taid)
 	{
 		boolean rollbackSuccessful = true;
 		
 		if (_preparedPages.containsKey(taid)) {
 			Set<Integer> preparedPages = _preparedPages.get(taid);
 			
 			for (int page : preparedPages) {
				boolean deleted = (new File(FileRessourcenManager.PREFIX + page + FileRessourcenManager.SUFFIX).delete());
 				
 				if (deleted) {
 					preparedPages.remove(page);
 				}
 				
 				rollbackSuccessful = rollbackSuccessful && deleted;
 			}
 			
 			// remove deleted pages from memory
 			_preparedPages.put(taid, preparedPages);
 		}
 		
 		if (rollbackSuccessful) {
 			System.err.println("TA "+taid+" abgebrochen.");
 		}
 		
 		return rollbackSuccessful;
 	}
 }
