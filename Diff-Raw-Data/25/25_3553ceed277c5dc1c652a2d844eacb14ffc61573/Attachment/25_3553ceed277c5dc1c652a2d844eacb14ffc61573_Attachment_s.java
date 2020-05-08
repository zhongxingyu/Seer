 package com.omkbron.sendemail.model;
 
 public class Attachment extends MailFile {
 	public Attachment(String pathFile, String fileName) {
 		super(pathFile, fileName);
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
		builder.append("MailFile [fileName=");
 		builder.append(fileName);
 		builder.append("]");
 		return builder.toString();
 	}
 
 }
