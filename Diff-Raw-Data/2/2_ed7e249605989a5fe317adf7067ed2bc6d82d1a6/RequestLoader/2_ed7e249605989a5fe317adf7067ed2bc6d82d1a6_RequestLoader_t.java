 package com.xrath.benchmark.http;
 
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 
 public class RequestLoader {
 	private File directory;
 	
 	public RequestLoader() {
 		
 	}
 
 	public void setDirectory(File directory) {
 		this.directory = directory;
 	}
 
 	public File getDirectory() {
 		return directory;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<Map<String, Object>> load() throws IOException {
 		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
 		
 		File[] files = directory.listFiles();
 		long l0 = System.currentTimeMillis();
 		for(File f : files) {
 			FileInputStream fis = new FileInputStream(f);
 			while(true) {
 				try {
 					ObjectInputStream ois = new ObjectInputStream(fis);
 					Map<String, Object> req = (Map<String, Object>)ois.readObject();
 					ret.add(req);
 				} catch( ClassNotFoundException e ) { 
 				} catch( EOFException e ) {
 					break;
 				}
 			}
 			fis.close();
 		}
 		long l1 = System.currentTimeMillis();
		System.out.println( ret.size() + " http requests were loaded in " + (l1-l0) + "ms");
 		l0 = System.currentTimeMillis();
 		Collections.sort(ret, new Comparator<Map<String, Object>>() {
 			@Override
 			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
 				Long l1 = (Long)o1.get("TIMESTAMP");
 				Long l2 = (Long)o2.get("TIMESTAMP");
 				return (int)(l1-l2);
 			}
 		});
 		l1 = System.currentTimeMillis();
 		System.out.println( "sorted in " + (l1-l0) + "ms");
 		return Collections.unmodifiableList(ret);
 	}
 	
 	public static void main( String[] args ) throws Exception {
 		RequestLoader rl = new RequestLoader();
 		rl.setDirectory(new File("samples/20100107"));
 		rl.load();
 	}
 }
