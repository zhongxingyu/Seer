 /*******************************************************************************
  * Copyright (c) 2010 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Thomas Schuetz and Henrik Rentz-Reichert (initial contribution)
  * 
  *******************************************************************************/
 
 
 package org.eclipse.etrice.runtime.java.debugging;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Thomas Schuetz
  *
  * MSCLogger implements logging of sync and async MSCs into a file 
  * MSC-format is that of Trace2UML (http://trace2uml.tigris.org/)
  * 
  */
 public class MSCLogger {
 
 	public void setMSC(String msc_name, String path){
 		this.msc_name = msc_name;
 		this.path = path;
 		filter = new MSCFilter();
 	}
 
 	public synchronized void open(){
 		is_open = true;
 	}
 	
 	
	public synchronized void addMessageAsyncOut(String source, String target, String message){
 		createLine(source, " >-- ", target, message);
 	}
 
	public synchronized void addMessageAsyncIn(String source, String target, String message){
 		createLine(source, " --> ", target, message);
 	}
 
 
	public synchronized void addMessageSyncCall(String source, String target, String message){
 		createLine(source, " ==> ", target, message);
 	}
 
	public synchronized void addMessageSyncReturn(String source, String target, String message){
 		createLine(source, " <== ", target, message);
 	}
 
 	
	public synchronized void addActorState(String actor, String state){
 		if (filter.applyTo(actor))
 			getCommandList().add( new String ("\t"+filter.reduceString(actor)+" >>> "+state) );
 	}
 
 	private void createLine(String source, String mid, String target, String message) {
 		if (filter.applyTo(source) && filter.applyTo(target)) {
 			getCommandList().add( new String ("\t"+filter.reduceString(source)+mid+filter.reduceString(target)+ " "  +message) );
 		}
 	}
 	
 	public synchronized void close(){
 	    if (is_open){
 		    try{
 			    // Create file 
 			    FileWriter fstream = new FileWriter("tmp/log/"+path+msc_name+".seq");
 			    BufferedWriter out = new BufferedWriter(fstream);
 			    //saveMSC(out);
 			    saveMSCforTrace2UML(out);
 			    out.close();
 		    }catch (Exception e){//Catch exception if any
 		    	System.err.println("Error: " + e.getMessage());
 		    }
 	    }
 		is_open = false;
 	}
 
 	private void saveMSCforTrace2UML(BufferedWriter out){
 		try {
 			out.write("#generated MSC for Trace2UML");
 	    	out.newLine();
 		    for (String cmd : getCommandList()){
 		    	out.write(cmd);
 		    	out.newLine();
 		    }
 		} catch (IOException e) {
 	    	System.err.println("Error: " + e.getMessage());
 		}
 		
 	}
 	
 	public List<String> getCommandList() {
 		return commandList;
 	}
 
 	public MSCFilter getMSCFilter(){
 		return filter;
 	}
 	
 	private List<String> commandList = new ArrayList<String>();
 	
 	private MSCFilter filter = null;
 	
 	private String path = null;
 	private String msc_name = null;
 	private boolean is_open = false;
 	
 }
