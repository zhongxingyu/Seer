 package com.cj.scmconduit.core.git;
 
 import java.io.BufferedReader;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Pattern;
 
 public class GitRevisionInfo {
 
 	public enum ChangeType {
 		A, D, M, R
 	}
 	
 	public static class Change {
 		public final ChangeType kind;
 		public final String path;
 		public final String destPath;
 		
 		public Change(ChangeType kind, String path, String destPath) {
 			super();
 			this.kind = kind;
 			this.path = path;
 			this.destPath = destPath;
 		}
 
 		public Change(ChangeType kind, String path) {
 			this(kind, path, null);
 		}
 	}
 
 	public final String author;
 	public final String date;
 	public final String commit;
 	public final String message;
 	public final List<Change> changes;
 	
 	public GitRevisionInfo(
 			String author,
 			String date,
 			String commit,
 			String message,
 			Change ... changes){
 		super();
 		this.author = author;
 		this.date = date;
 		this.commit = commit;
 		this.message = message;
 		this.changes = Arrays.asList(changes);
 	}
 	
 	public GitRevisionInfo(String log) {
 		try{

 			String author = null;
 			String commit = null;
 			String date = null;
 			StringBuilder message = null;
 			StringBuilder text;
 			
 			boolean readMessage = false;
 			try{
 				text = new StringBuilder();
 				BufferedReader r = new BufferedReader(new StringReader(log));
 				for(String line = r.readLine(); line!=null; line = r.readLine()){
 					if(line.startsWith("Author")){
 						author = line.replaceFirst(Pattern.quote("Author:"), "").trim();
 					}else if(line.startsWith("commit ")){
 						commit = line.replaceFirst(Pattern.quote("commit "), "").trim();
 					} else if(line.startsWith("Date:")){
 						date = line.replaceFirst(Pattern.quote("Date:"), "").trim();
 					} else if(line.trim().isEmpty()){
 						if(message ==null){
 							readMessage = true;
 							message = new StringBuilder();
 						}else{
 							readMessage = false;
 						}
 					}else if(line.startsWith("    ")){
 						if(readMessage){
 							if(message.length()>0){
 								message.append("\n");
 							}
 							message.append(trimLeft(line));
 						}
 						// do nothing
 					}else{
 						text.append(line);
 						text.append("\n");
 					}
 				}
 			}catch(Exception e){
 				throw new RuntimeException(e);
 			}
 			List<Change> changes = new ArrayList<Change>();
 			
 			BufferedReader reader = new BufferedReader(new StringReader(text.toString()));
 			
 			for(String changeLine = reader.readLine();changeLine!=null;changeLine = reader.readLine()){
 				final TextWalker w = new TextWalker(changeLine);
 				
 				ChangeType gitOp = ChangeType.valueOf(w.currentChar());
 
 				w.scanUntil(IS_WHITESPACE);
 				w.scanUntil(IS_NOT_WHITESPACE);
 				String file = w.scanUntil(IS_WHITESPACE);
 				w.scanUntil(IS_NOT_WHITESPACE);
 				String destFile = w.scanUntil(IS_WHITESPACE);
 				changes.add(new Change(gitOp, file, (destFile.isEmpty()?null:destFile)));
 			}
 			
 			this.message = message.toString();
 			this.date = date;
 			this.commit = commit;
 			this.author = author;
 			this.changes = changes;
 		}catch(Exception e){
 			throw new RuntimeException(e);
 		}
 	}
 	private static String trimLeft(String s) {
 	    return s.replaceAll("^\\s+", "");
 	}
 	
 	TextWalker.Condition IS_WHITESPACE = new TextWalker.Condition(){
 		@Override
 		public boolean isTrue(TextWalker w) {
 			String current = w.currentChar();
 			return current.trim().isEmpty();
 		}
 	};
 	TextWalker.Condition IS_NOT_WHITESPACE = new TextWalker.Condition(){
 		@Override
 		public boolean isTrue(TextWalker w) {
 			String current = w.currentChar();
 			return !current.trim().isEmpty();
 		}
 	};
 	TextWalker.Condition END_OF_LINE = new TextWalker.Condition(){
 		@Override
 		public boolean isTrue(TextWalker w) {
 			String current = w.currentChar();
 			return current ==null;
 		}
 	};
 	
 	private static class TextWalker {
 		static interface Condition {
 			boolean isTrue(TextWalker w);
 		}
 		final String text;
 		int pos = 0;
 		
 		private TextWalker(String text) {
 			super();
 			this.text = text;
 		}
 
 		public String currentChar() {
 
 			if(pos<text.length()){
 				return text.substring(pos, pos+1);
 			}else{
 				return null;
 			}
 		}
 		
		public void next(){
			pos++;
		}
		
 		public String scanUntil(Condition c) {
 			StringBuilder text = new StringBuilder();
 			while(currentChar()!=null && !c.isTrue(this)){
 				text.append(currentChar());
 				pos++;
 			}
 			return text.toString();
 		}
		
		
		
 	}
 	
 //	static class XYZ <T>{
 //		final String body;
 //		final T results;
 //		private XYZ(String body, T results) {
 //			super();
 //			this.body = body;
 //			this.results = results;
 //		}
 //		
 //	}
 //	
 //	private XYZ<Map<String, String>> readHeader(String log){
 //		
 //	}
 }
