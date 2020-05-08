 package com.hsun324.ftplite;
 
 import java.nio.charset.Charset;
 
 public class FTPFile {
 	private final boolean isBinary;
 	private final byte[] source;
 	private final String text;
 	private final Charset encoding;
 
 	public FTPFile (byte[] data) {
 		this.isBinary = true;
 		this.source = data;
 		
 		this.text = null;
 		this.encoding = null;
 	}
 	public FTPFile (String text, Charset encoding) {
 		this.isBinary = false;
 		this.text = text;
 		this.encoding = encoding;
 		
 		this.source = text.getBytes(encoding);
 	}
 	public FTPFile(byte[] data, Charset encoding) {
 		this.isBinary = false;
 		this.text = new String(data, encoding).replaceAll("\r(\n|(?!\n))", "\n");
 		this.encoding = encoding;
 		
 		this.source = data;
 	}
 	
 	public boolean isBinary() {
 		return isBinary;
 	}
 	public boolean isText() {
 		return !isBinary;
 	}
 	public byte[] getBytes() {
 		return source;
 	}
 	public String getText() {
 		return text;
 	}
 	public Charset getEncoding() {
 		return encoding;
 	}
 }
