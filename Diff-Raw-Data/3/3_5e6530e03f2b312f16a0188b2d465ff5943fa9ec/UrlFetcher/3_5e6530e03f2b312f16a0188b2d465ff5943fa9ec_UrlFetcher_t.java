 package application;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.SQLException;
 import java.util.Calendar;
 
 import org.apache.solr.client.solrj.SolrServerException;
 
 public class UrlFetcher {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		long sTime = System.currentTimeMillis();
 		System.out.println("OntoStarUrlFetcher started");
 
 		File stopFile = new File(Configuration.stopFile);
 		File stoppedFile = new File(Configuration.stoppedFile);
 		File runFile = new File(Configuration.runFile);
 
 		UrlMySQLInterface db = UrlMySQLInterface.getinstance();
 		SolrQueryMaker solr = SolrQueryMaker.getInstance();
 
 		System.out.println("Fetching URLs starting from "
 				+ solr.getSolrStringDate(solr.getDateInMillis()) + ".");
 		boolean upToDate = false;
 		long lastDate = solr.getDateInMillis();
 		if (lastDate >= System.currentTimeMillis()) {
 			upToDate = true;
 		}
 		while (!stopFile.exists() && !upToDate) {
 			System.out.println("========================= Time: " + getTimestamp(lastDate));
 			try {
 				db.insertUrls(solr.getNextUrls());
 			}
 			catch (SolrServerException e) {
 				e.printStackTrace();
 			}
 
 			lastDate = solr.getDateInMillis();
 			if (lastDate >= System.currentTimeMillis()) {
 				upToDate = true;
 			}
 		}
 
 		try {
 			if (upToDate) {
 				System.out.println("Fetching done.");
 			}
 			else {
 				System.out.println("Received stop command.");
 			}
 			
			db.closeConnection();
 			
 			if (!runFile.exists()) {
 				runFile.createNewFile();
 			}
 			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(runFile)));
 			writer.println(SolrQueryMaker.getInstance().getDateInMillis());
 			writer.close();
 			stoppedFile.createNewFile();
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 			System.out.println("Something went wrong during the creation of command files.");
 
 		}
 		catch (SQLException e) {
 			System.out.println("Cannot close connection.");
 			e.printStackTrace();
 		}
 
 		System.out.println("OntoStarUrlFetcher stopped.");
 		long eTime = System.currentTimeMillis();
 		System.out.println("All done in " + (((double) eTime) - sTime) / 1000 + " seconds.");
 		sTime = UrlMySQLInterface.sTime;
 		eTime = UrlMySQLInterface.eTime;
 		System.out.println("Time to insert URLs: " + (((double) eTime) - sTime) / 1000
 				+ " seconds.");
 
 		System.err.println("\n============================================\nEnd: " + getTimestamp()
 				+ "\n============================================\n");
 	}
 
 	public static String getTimestamp() {
 		return getTimestamp(Calendar.getInstance().getTimeInMillis());
 	}
 	
 	public static String getTimestamp(long timeInMillis) {
 		String timestamp = String.format("%tA %tF h%tR", timeInMillis, timeInMillis, timeInMillis);
 
 		return timestamp;
 	}
 }
