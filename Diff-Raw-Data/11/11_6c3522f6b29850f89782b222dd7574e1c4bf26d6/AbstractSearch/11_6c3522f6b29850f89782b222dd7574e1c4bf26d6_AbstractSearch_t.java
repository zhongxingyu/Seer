 package com.silverwzw.api;
 
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 
 public abstract class AbstractSearch implements Search {
 	protected String q;
 	public void setSearchTerm(String searchTerm) {
		if (searchTerm == null) {
			q = null;
			return;
		}
 		try {
 			q = java.net.URLEncoder.encode(searchTerm, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	public List<URL> asUrlList(int docNum) {
 		List<URL> ul = new LinkedList<URL>();
 		for (String u : asUrlStringList(docNum)) {
 			try{
 				ul.add(new URL(u));
 			} catch (MalformedURLException e) {
 				System.err.println("Cannot recognize the link returned by Query:" + u);
 			}
 		}
 		return ul;
 	}
 }
