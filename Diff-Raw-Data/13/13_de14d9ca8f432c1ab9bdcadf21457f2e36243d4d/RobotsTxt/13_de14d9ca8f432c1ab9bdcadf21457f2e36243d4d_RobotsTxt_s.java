 package org.paxle.filter.robots.impl;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 public class RobotsTxt implements Serializable {
	public static final long RELOAD_INTERVAL_DEFAULT = TimeUnit.DAYS.toMillis(7);
	public static final long RELOAD_INTERVAL_ERROR = TimeUnit.DAYS.toMillis(1);
	public static final long RELOAD_INTERVAL_TEMP_ERROR = TimeUnit.HOURS.toMillis(1);
 	
 	private boolean accessRestricted = false;
 	private String downloadStatus = "200 OK";
 	private String hostPort = null;
 	private Date loadedDate = null;
 	private long reloadInterval = RELOAD_INTERVAL_DEFAULT;	
 	private HashMap<String, RuleBlock> ruleBlockMap = new HashMap<String, RuleBlock>();
 	private List<RuleBlock> ruleBlocks = new ArrayList<RuleBlock>();
 	
 	public RobotsTxt(String hostPort, long reloadInterval, String downloadStatus) {
 		this(hostPort, reloadInterval, downloadStatus, false);
 	}
 	
 	public RobotsTxt(String hostPort, long reloadInterval, String downloadStatus, boolean accessRestricted) {
 		this.hostPort = hostPort;
 		this.loadedDate = new Date();
 		this.reloadInterval = (reloadInterval < 0) ? RELOAD_INTERVAL_DEFAULT : reloadInterval;
 		this.downloadStatus = downloadStatus;
 		this.accessRestricted = accessRestricted;
 	}
 	
 	/** Return the hostname and possibly the port for this robots.txt definition, e.g. "www.example.org:888" */
 	public String getHostPort() {
 		return this.hostPort;
 	}
 	
 	public Date getLoadedDate() {
 		return this.loadedDate;
 	}
 	
 	/**
 	 * Return the date when the cache robots.txt file expires.
 	 * @return Date the cached file expires
 	 */
 	public Date getExpirationDate() {
 		return new Date(this.loadedDate.getTime() + this.getReloadInterval());
 	}
 	
 	/**
 	 * Returns the reload interval of this file in milliseconds
 	 * @return
 	 */
 	public long getReloadInterval() {
 		return this.reloadInterval;
 	}
 	
 	void addRuleBlock(RuleBlock ruleBlock) {
 		this.ruleBlocks.add(ruleBlock);
 		if (this.ruleBlockMap.size() > 0) this.ruleBlockMap.clear();
 	}
 	
 	public boolean isDisallowed(String agent, String path) {
 		if (path != null && path.equals("/robots.txt")) return Boolean.FALSE;
 		if (this.accessRestricted) return Boolean.TRUE;		
 		if (this.ruleBlocks == null || this.ruleBlocks.size() == 0) return Boolean.FALSE;
 		
 		if (this.ruleBlockMap.size() == 0) this.buildRuleBlockMap();
 		
 		RuleBlock ruleBlock = null;
 		if (this.ruleBlockMap.containsKey(agent)) {
 			ruleBlock = ruleBlockMap.get(agent);
 		} else if (this.ruleBlockMap.containsKey("*")) {
 			ruleBlock = ruleBlockMap.get("*");
 		}
 		
 		if (ruleBlock != null) {
 			Boolean isDisallowed = ruleBlock.isDisallowed(agent, path);
 			if (isDisallowed != null) return isDisallowed;
 		}
 		
 		return Boolean.FALSE;
 	}
 	
 	private void buildRuleBlockMap() {
 		for (RuleBlock ruleBlock : this.ruleBlocks) {
 			for (String agent : ruleBlock.getAgents()) {
 				this.ruleBlockMap.put(agent, ruleBlock);
 			}
 		}
 	}
 	
 	@Override
 	public String toString() {
 		StringBuilder str = new StringBuilder();
 		
 		str.append(String.format("# Robots.txt of host: %s \r\n",this.hostPort))
 		   .append(String.format("# Downloaded date: %s \r\n", this.loadedDate))
 		   .append(String.format("# Download status: %s \r\n", this.downloadStatus))
 		   .append(String.format("# Expiration date: %s \r\n", this.getExpirationDate()))
 		   .append("\r\n");
 		
 		if (this.accessRestricted) {
 			str.append("User-agent: *\r\n")
 			   .append("Disallow: /\r\n");		
 		} else if (this.ruleBlocks.size() == 0) {
 			str.append("User-agent: *\r\n")
 			   .append("Disallow: \r\n");				
 		} else {
 			for (RuleBlock ruleBlock : this.ruleBlocks) {
 				str.append(ruleBlock).append("\r\n");
 			}			
 		}
 		
 		return str.toString();
 	}
 }
