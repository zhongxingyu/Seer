 package dat255.grupp06.bibbla.utils;
 
 public enum Error {
 	LOGIN_FAILED,
	SEARCH_FAILED,
	RENEW_FAILED,
 	MISSING_CREDENTIALS, // Some user credentials are missing.
	LOGIN_NEEDED, // Used by other jobs, if they detect they're not logged in.

	// Session.fetchUserUrl() returned an empty string.
	// May indicate incorrect details, no network, you name it.
	FETCHING_USER_URL_FAILED	 
 }
