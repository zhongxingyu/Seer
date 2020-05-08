 package com.oltpbenchmark;
 
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Savepoint;
 import java.sql.Statement;
 import java.util.*;
 import java.io.*;
 import java.net.*;
 
 import main.*;
 import java.lang.Math;
 
 public class DBConnect {
     private Socket socket;
     private PrintWriter out;
     private BufferedReader in;
     private ServerAddress address;
     private ArrayList<ServerAddress> contacts;
     private RPC rpc;
 
     // for current transaction
     int tid;
     ServerAddress currentSA;
     
     public DBConnect(String url, int port) {
 	address = new ServerAddress(2, "C", 8002);
 	RPC rpc = new RPC(address);
 	contacts = new ArrayList<ServerAddress>();
 	contacts.add(new ServerAddress(0, "S0", 4444));
 	contacts.add(new ServerAddress(1, "S1", 4445));
 	contacts.add(new ServerAddress(2, "S2", 4446));
 
 	tid = 0;
 	currentSA = null;
     }
 
     public void commit() throws SQLException {
 	System.out.println("Committing");
 	PartitionTest.commit(currentSA, address, tid, rpc);
 	currentSA = null;
     }
 
     public boolean executeQuery(HashMap<String, HashMap<String, String> > writes, HashMap<String, HashMap<String, String> > reads) {
 	if (currentSA == null) {
 	    tid++;
 	    currentSA = contacts.get((int) (Math.random() * contacts.size()));
 	}
 	if (writes == null) {
 	    writes = new HashMap<String, HashMap<String, String> >();
 	}
 	if (reads == null) {
 	    reads = new HashMap<String, HashMap<String, String> >();	    
 	}
 	while (!PartitionTest.startTxn(currentSA, address, tid, writes, reads, rpc)) {
 	}
 	return true;
     }
 
    public void setAutoCommit(boolean ac) {
 	
    }
 
    public boolean getAutoCommit() {
    	return false;
    }
 
     public void rollback() throws SQLException {
 	PartitionTest.abort(currentSA, address, tid, rpc);
 	currentSA = null;
     }
 
     public void close() throws SQLException {
     	System.out.println("closing");
     }
 
     public void executeBatch(HashMap<String, HashMap<String, String> > writes, HashMap<String, HashMap<String, String> > reads) {
 	if (currentSA == null) {
 	    tid++;
 	    currentSA = contacts.get((int) (Math.random() * contacts.size()));
 	}
 	if (writes == null) {
 	    writes = new HashMap<String, HashMap<String, String> >();
 	}
 	if (reads == null) {
 	    reads = new HashMap<String, HashMap<String, String> >();	    
 	}
 	while (!PartitionTest.startTxn(currentSA, address, tid, writes, reads, rpc)) {
 	}
     }
 
     public Statement createStatement() {
 	return null;
     }
 
     public void setTransactionIsolation(int isolationMode) {
 		
     }
 
     public void rollback(Savepoint savepoint) throws SQLException {
 	
     }
 
     public PreparedStatement prepareStatement(String sql) {
 	return null;
     }
 
 
     public PreparedStatement prepareStatement(String sql, int[] is) {
 	return null;
     }
 }
