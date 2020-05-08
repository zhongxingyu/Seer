 package org.CMCSToolsSet.Configure;
 
 public class SolidConfigure {
 
 	public final static String PaxosOrderSplitor = "\r\r\n\n";
 
 	public final static String BakUpFiller = "BUF";
 
 	public final static String NONE = "none";
 
 	public final static String OrderHead = "PAXOS_BABY";
 
 	public final static String OdrPkgPrefix = "org.CMCSToolsSet.OrdersConstructors.";
 
 	/**
 	 * TODO watcher对PaxosNodes的心跳包handler还没有
 	 * 		
 	 */
 	public final static String[] HandlerKeys = { "Confirm", "Delete",
 			"DesignateLeader", "DesignateLeaderReceipt", "DesignateWatcher",
 			"DesignateWatcherReceipt", "GetValue", "LeaderAS", "LeaderASK",
 			"OriginOrderExecute", "Refuse", "SetKeyValue", "VersioREQ",
 			"VersionRESP", "VotLeader", "VotLeaderReceipt", "VotWatcher",
 			"VotWatcherReceipt", "WatcherAS", "WatcherASK" };
 
 }
