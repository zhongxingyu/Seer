 package com.yuktix.bencode.ds;
 
 public class StringType implements IBencodeType{
 	private String value ;
 	
 	public StringType(String s) {
 		this.value = s ;
 	}
 	
 	public StringType(byte[] bytes) {
 		this.value = new String(bytes);
 	}
 	public String getValue() {
 		return value;
 	}
 	public void setValue(String value) {
 		this.value = value;
 	}
 	
 	@Override
 	public String toString() {
		return "[string:" + this.value + "]\n" ;
 	}
 
 	@Override
 	public String bencode() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(this.value.length());
 		sb.append(":");
 		sb.append(this.value);
 		return sb.toString();
 	}
 }
