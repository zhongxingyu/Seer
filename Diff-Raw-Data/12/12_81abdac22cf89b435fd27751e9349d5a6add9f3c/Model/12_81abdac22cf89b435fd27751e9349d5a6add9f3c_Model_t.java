 package com.kdcloud.client.gwt.mvc;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.kdcloud.server.entity.Dataset;
 
 public class Model {
 
 	public static final List<String> APPS = Arrays.asList("ECG Peak Detection");
 
 	List<String> apps = APPS;
 	
 	List<Dataset> data = new LinkedList<Dataset>();
 	
 	Dataset selectedDataset = null;
 	
 	String user;
 
 }
