 package com.wk.rivers;
 
 import twitter4j.Status;
 import twitter4j.json.DataObjectFactory;
 
 public class Tweets extends RedisModel {
 	private Tweets(String river, String ks, String redisURI) {
 		super(river, ks, redisURI);
 	}
 
 	public static Tweets on(String river, String redisURI) {
 		return new Tweets(river, "tweets", redisURI);
 	}
 	
 	public boolean contains(Status status) {
 		return this.contains(new Long(status.getId()).toString());
 	}
 	
 	public void add(Status status) {
 		Double rank = new Double(status.getId());
 		String member = toJson(status);
		this.add(rank, member, new Long(status.getId()).toString());
 	}
 	
 	private String toJson(Status status) {
		System.out.println(DataObjectFactory.getRawJSON(status));
 		return DataObjectFactory.getRawJSON(status);
 	}
 }
