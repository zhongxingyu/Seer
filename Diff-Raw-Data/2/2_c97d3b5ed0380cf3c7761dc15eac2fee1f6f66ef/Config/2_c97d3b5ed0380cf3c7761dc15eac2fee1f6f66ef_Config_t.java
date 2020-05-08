 package com.vaguehope.senkyou;
 
 public interface Config {
 
 	// Server.
 	int SERVER_ACCEPTORS = 2;
 	int SERVER_MAX_IDLE_TIME_MS = 25000; // 25 seconds in milliseconds.
 	int SERVER_SESSION_INACTIVE_TIMEOUT_SECONDS = 60 * 60; // 60 minutes in seconds.
 	int SERVER_LOW_RESOURCES_CONNECTIONS = 100;
 	int SERVER_LOW_RESOURCES_MAX_IDLE_TIME_MS = 5000; // 5 seconds in milliseconds.
 
 	// Users.
 	int USER_COUNT_MAX = 100;
 	int USER_AGE_MAX_MIN = 10; // 10 minutes.
 
 	// General feed fetching.
 	int TWEET_FETCH_PAGE_SIZE = 40;
 	long TWEET_FETCH_RETRY_WAIT_MS = 60000L; // 60 seconds.
 
 	// All per user.
 	long HOME_TIMELINE_MAX_AGE_MS = 30000L; // 30 seconds.
 	long MENTIONS_MAX_AGE_MS = 30000L; // 30 seconds.
 	long ME_MAX_AGE_MS = 30000L; // 30 seconds.
 	int TWEET_CACHE_MAX_COUNT = 100;
 	int TWEET_CACHE_MAX_AGE_MIN = 60; // 60 minutes.
 
 	// Feed lengths.
 	int HOME_TIMELINE_LENGTH = 40;
 	int MENTIONS_LENGTH = 15;
	int MY_REPLIES_LENGTH = 10;
 
 	// Session cookie.
 	String COOKIE_SENKYOU_SESSION = "SenkyouSession";
 	int COOKIE_EXPIRY = 60 * 60 * 24 * 3; // 3 days in seconds.
 
 	// Session data store.
 	int DATASTORE_SESSION_EXPIRY = 60 * 60 * 24 * 3; // 3 days in seconds.
 
 }
