 package com.bretth.osmosis.git_migration;
 
 import java.nio.charset.Charset;
 
 
 public class RevisionRenumberTask implements LineSink {
 	private LineSink sink;
 	private RevisionMapper revisionMapper;
 	private String repository;
 	private Charset utf8Charset;
 
 
 	public RevisionRenumberTask(LineSink sink, RevisionMapper revisionMapper, String repository) {
 		this.sink = sink;
 		this.revisionMapper = revisionMapper;
 		this.repository = repository;
 
 		utf8Charset = Charset.forName("UTF-8");
 	}
 
 
 	private byte[] toBytes(String data) {
 		return data.getBytes(utf8Charset);
 	}
 
 
 	private String toString(byte[] data) {
 		return new String(data, utf8Charset);
 	}
 
 
 	private boolean doesPrefixMatch(byte[] prefix, byte[] data) {
 		if (data.length < prefix.length) {
 			return false;
 		}
 		for (int i = 0; i < prefix.length; i++) {
 			if (prefix[i] != data[i]) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 
 	@Override
 	public void processLine(byte[] data) {
 		String revisionPrefix = "Revision-number: ";
 		String copyfromRevisionPrefix = "Node-copyfrom-rev: ";
 
 		if (doesPrefixMatch(toBytes(revisionPrefix), data)) {
 			String revisionStr = toString(data).substring(revisionPrefix.length());
 			int sourceRevision = Integer.parseInt(revisionStr);
 			int targetRevision = revisionMapper.addRevision(repository, sourceRevision);
 
 			sink.processLine(toBytes(revisionPrefix + targetRevision));
 
 		} else if (doesPrefixMatch(toBytes(copyfromRevisionPrefix), data)) {
 			String revisionStr = toString(data).substring(copyfromRevisionPrefix.length());
 			int sourceRevision = Integer.parseInt(revisionStr);
 			int targetRevision = revisionMapper.getTargetRevision(repository, sourceRevision);
 
			sink.processLine(toBytes(revisionPrefix + targetRevision));
 			
 		} else {
 			sink.processLine(data);
 		}
 	}
 
 
 	@Override
 	public void complete() {
 		sink.complete();
 	}
 }
