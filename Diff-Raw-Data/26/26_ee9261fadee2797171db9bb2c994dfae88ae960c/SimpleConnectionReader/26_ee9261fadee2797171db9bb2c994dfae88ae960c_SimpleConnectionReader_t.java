 /**********************************************************************
  $Id: SimpleConnectionReader.java,v 1.11 2002/09/27 02:58:54 vpapad Exp $
 
 
   NIAGARA -- Net Data Management System                                 
                                                                         
   Copyright (c)    Computer Sciences Department, University of          
                        Wisconsin -- Madison                             
   All Rights Reserved.                                                  
                                                                         
   Permission to use, copy, modify and distribute this software and      
   its documentation is hereby granted, provided that both the           
   copyright notice and this permission notice appear in all copies      
   of the software, derivative works or modified versions, and any       
   portions thereof, and that both notices appear in supporting          
   documentation.                                                        
                                                                         
   THE AUTHORS AND THE COMPUTER SCIENCES DEPARTMENT OF THE UNIVERSITY    
   OF WISCONSIN - MADISON ALLOW FREE USE OF THIS SOFTWARE IN ITS "        
   AS IS" CONDITION, AND THEY DISCLAIM ANY LIABILITY OF ANY KIND         
   FOR ANY DAMAGES WHATSOEVER RESULTING FROM THE USE OF THIS SOFTWARE.   
                                                                         
   This software was developed with support by DARPA through             
    Rome Research Laboratory Contract No. F30602-97-2-0247.  
 **********************************************************************/
 
 
 package niagara.client;
 
 import java.net.*;
 import java.io.*;
 
 import gnu.regexp.*;
 import niagara.utils.PEException;
 class SimpleConnectionReader extends AbstractConnectionReader 
     implements Runnable {
     UIDriverIF ui;
 
     public SimpleConnectionReader(String hostname, int port, 
 				  UIDriverIF ui, DTDCache dtdCache) {
 	super(hostname, port, ui, dtdCache);
 	this.ui = ui;
     }
 
     StringBuffer results = new StringBuffer();
     
     synchronized private void addResult(String line) {
 	    results.append(line);
     }
     
     synchronized public String getResults() {
 	String resultStr = results.toString();
         results.setLength(0);
 	return resultStr;
     }
 
     synchronized public void appendResults(StringBuffer sb) {
         sb.append(results);
         results.setLength(0);
     }
     
     /**
      * The run method accumulates result strings
      */
     public void run()   {
 	int local_id = -1, server_id = -1;
 	// Read the connection and throw the callbacks
 	
 	try {
            ((SimpleClient) ui).setConnectionReader(this);
 	    BufferedReader br = new BufferedReader(cReader);
 	    String line;
 	    RE re = new RE("<responseMessage localID\\s*=\\s*\"([0-9]*)\"\\s*serverID\\s*=\\s*\"([0-9]*)\"\\s*responseType\\s*=\\s*\"server_query_id\"");
 	    boolean registered = false;
 	    line = br.readLine();
 	    while (line != null) {
 		if (line.indexOf("<response") == 0  || 
 		    line.indexOf("</response") == 0) {
 		    if (line.indexOf("\"parse_error\"") != -1) {
 			ui.errorMessage("Syntax error in query!\n");
 		    }
 		    if (!registered && line.indexOf("\"server_query_id\"") != -1) {
 			REMatch m = re.getMatch(line);
 			local_id = Integer.parseInt(m.substituteInto("$1"));
 			server_id = Integer.parseInt(m.substituteInto("$2"));
 			queryRegistry.setServerId(local_id, server_id);
 		    }
 		    if (line.indexOf("\"end_result\">") != -1) {
                         addResult("</niagara:results>");
                         ui.notifyNew(local_id);
 			ui.notifyFinalResult(local_id);
                     }
 		}
 		else {
 		    addResult(line);
                     if (line.indexOf("<?xml") != -1)
                         addResult("<niagara:results>");
                     ui.notifyNew(local_id);
 		}
 
 		line = br.readLine();
 	    }
 	}
 	catch(gnu.regexp.REException e){
 	    throw new PEException("Invalid response message reg exception " +
 			       e.getMessage());
 	    
 	} catch (java.io.IOException ioe) {
 	    System.err.println("Unable to read from server " + ioe.getMessage());
 	    ioe.printStackTrace();
 	    throw new PEException("Unable to read from server");
 	}
     }
 }
 
 
 
