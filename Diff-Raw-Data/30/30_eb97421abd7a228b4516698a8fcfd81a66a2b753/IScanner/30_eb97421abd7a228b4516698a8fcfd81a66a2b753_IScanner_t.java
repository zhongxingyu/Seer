package com.yuktix.bencode;;
 
 public interface IScanner {
 	void ping(IScanner child);
 	long getValue();
 }
