 package com.gh.jordner.jpa;
 
 public interface JPAConstants {
 
 	
 	public static final String JPA_UNIT = "Accounts";
 	public static final String H2_URL_EMBEDDED="jdbc:h2:${java.io.tmpdir}/h2jpa;AUTO_SERVER=TRUE;ACCESS_MODE_DATA=rw;TRACE_LEVEL_FILE=4";
 	public static final String H2_URL_EMBEDDED_NOTWORKED="jdbc:h2:~/test";
 	public static final String H2_URL_NOTWORKED="jdbc:h2:tcp://localhost/~/test";
	public static final String H2_URL_EMBEDDED_HOME_DIR="jdbc:h2:/~/test";
 }
