 package dbs.api;
 
 import java.sql.PreparedStatement;
 import java.io.Writer;
 
 public class QueryThread extends Thread {
 	private Writer out;
 	private String query;
 	private PreparedStatement statement;
 	private DBSApiExecuteQuery querier;
 	public QueryThread(Writer out, DBSApiExecuteQuery querier, String query, PreparedStatement statement) {
 		this.out = out;
 		this.query = query;
 		this.statement = statement;
		this. querier = querier;
 		
 	}
 	public void run() {
 		String threadName = Thread.currentThread().getName();
 		try {
 			querier.runQuery(out, query, statement);
 			System.out.println("Thread " + threadName + " completed");
 		} catch (Exception e) {
 			System.out.println("\tInterruptedException in thread " + threadName);
 		}
 	}
 
 }
