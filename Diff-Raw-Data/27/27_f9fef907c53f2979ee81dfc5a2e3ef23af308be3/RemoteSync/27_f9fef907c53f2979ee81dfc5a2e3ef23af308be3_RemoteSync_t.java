 package net.daum.remotesync;
 
 /** 
  * 패키지 상수값.
  * @author dante
  */
 public interface RemoteSync {
 	int DEFAULT_BLOCK_SIZE = 4096;
	long DEFAULT_RAW_LIMIT = (1 << 22) - 1;
 }
