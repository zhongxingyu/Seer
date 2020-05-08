 package com.casualcoding.fortressdb;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Stack;
 
 public class PersistanceManager {
 	private static final int MAX_BUFFER_SIZE = 5;
 	
 	private static PersistanceManager _pmngr;
 	private Map<Integer, Object[]> _buffer; // pageId -> (transactionId, data)
 	private LogFile _log;
 	private Storage _disk;
 	private Stack<Integer> _committedTransactions;
 	private int _currentTransactionId;
 
 	
 	private PersistanceManager() {
 		_buffer = new HashMap<Integer, Object[]>();
 		_log = new LogFile("fortress.log");
 		_disk = new Storage("fortress.db");
 		_currentTransactionId = 0;
 		_committedTransactions = new Stack<Integer>();
 		
 	}
 	
 	public int beginTransaction() {
 		_currentTransactionId++;
 		return _currentTransactionId;
 	}
 	
 	public void commit(int transactionId) {
 		// list.add(tid)
 		_committedTransactions.push(transactionId);
 	}
 	
 	public void write(int transactionId, int pageId, String data) {
 		
 		_buffer.put(pageId, new Object[]{ transactionId, data });
 		if (_buffer.size() > MAX_BUFFER_SIZE) {
 			persistTransactions();
 		}
 	}
 	
 	private void persistTransactions() {
 		// get all (pageId, data) for transactionID
 		while (!_committedTransactions.isEmpty()) {
 			int tid = _committedTransactions.pop();
			_buffer.
 		}
 		
		new list
		for(Object[] tuple: _buffer.values()) {
			int tid = (Integer) tuple[0];
			if(_committedTransactions.contains(tid)) (
				list.add()	)
		}
 		
 		// write all tuples to disk
 		// remove tuples from buffer
 	}
 	
 	public static PersistanceManager getPersistanceManager() {
 		if (_pmngr == null) {
 			_pmngr = new PersistanceManager();
 		}
 		return _pmngr;
 	}
 	
 
 	
 	
 	
 }
