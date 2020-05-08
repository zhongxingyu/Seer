 package niagara.firehose;
 
 import java.lang.*;
 import java.io.*;
 import java.net.*;
 
 class ClientMain {
   class Timer {
     private long start_tp;
     private long stop_tp;
     private boolean running;
 
     Timer() {running = false; }
     void start() {
       running = true;
       start_tp = System.currentTimeMillis();
     }
     void stop() {
       stop_tp = System.currentTimeMillis();
       running = false;
     }
     void print() {
       if (running == true) {
         System.err.println("Must stop timer before printing");
         return;
       }
 
       long total = stop_tp - start_tp;
       StringBuffer stTime = new StringBuffer("Seconds: ");
       stTime.append(String.valueOf(total / 1000));
       stTime.append(".");
       long msTotal = total % 1000;
       if (msTotal < 10)
         stTime.append("0");
       if (msTotal < 100)
         stTime.append("0");
       stTime.append(String.valueOf(msTotal));
       System.out.println(stTime.toString());
     }
   }
     
     String host_name;
     int port_num = FirehoseConstants.LISTEN_PORT;
     int stype = FirehoseConstants.AUCTION;
     String stream_descriptor = "";
     String stream_descriptor2 = "bidperson";
     int client_wait = -1;
     int numGenCalls = 1;
     int numTLElts = 1;
     boolean streaming = true;
     boolean prettyPrint = false;
    int rate = 0;
    String trace = ""; 
     boolean quiet = false;
     boolean shutdown = false;
 
     boolean init(String args[]) {
 	int idx = 0;
 	boolean ok = true;
 	Integer temp;
 	
 	try {
 	    host_name = InetAddress.getLocalHost().getHostName();
 	} catch (UnknownHostException ex) {
 	    host_name = new String("chinook.cse.ogi.edu");
 	}
 	
 	//Iterate through the arguments
 	while (idx < args.length && ok == true) {
 	    ok = false;
 	    
 	    //get port number
 	    if ((idx+1) < args.length && args[idx].compareTo("-p") == 0) {
 		temp = new Integer(args[idx+1]);
 		port_num = temp.intValue();
 		idx += 2;
 		ok = true;
 	    }
 
 	    //get host name
 	    if ((idx+1) < args.length && args[idx].compareTo("-h") == 0) {
 		host_name = args[idx+1];
 		idx += 2;
 		ok = true;
 	    }
 	    
 	    //get type 
 	    if ((idx+1) < args.length && args[idx].compareTo("-s") == 0) {
 		for(int j = 0; j<FirehoseConstants.numDataTypes && !ok; j++) {
 		    if (args[idx+1].compareTo(FirehoseConstants.typeNames[j]) == 0) {
 			stype = j;
 			ok = true;
 			idx += 2;
 		    }
 		} 
 	    }
 	    
 	    //get descriptor
 	    if ((idx+1) < args.length && args[idx].compareTo("-d") == 0) {
 		stream_descriptor = args[idx+1];
 		idx += 2;
 		ok = true;
 		if(stype == FirehoseConstants.XMLB) {
 		    System.err.println("WARNING: stream descriptor ignored for XMLB");
 		}
 	    }
 
 	    //get descriptor2
 	    if ((idx+1) < args.length && args[idx].compareTo("-d2") == 0) {
 		stream_descriptor2 = args[idx+1];
 		idx += 2;
 		ok = true;
 		if(stype == FirehoseConstants.XMLB) {
 		    System.err.println("WARNING: stream descriptor ignored for XMLB");
 		}
 	    }
 
 	    //get numgencalls
 	    if ((idx+1) < args.length && args[idx].compareTo("-ng") == 0) {
 		temp = new Integer(args[idx+1]);
 		numGenCalls = temp.intValue();
 		idx += 2;
 		ok = true;
 	    }
 	    
 	    //get num top level elts
 	    if ((idx+1) < args.length && args[idx].compareTo("-tl") == 0) {
		if(stype != FirehoseConstants.XMLB && 
 		   stype != FirehoseConstants.AUCTION) {
 		    System.err.println("Warning -tl numTopLevelElts will be ignored");
 		}
 		temp = new Integer(args[idx+1]);
 		numTLElts = temp.intValue();
 		idx += 2;
 		ok = true;
 	    }
 	    
 	    //get client wait
 	    if ((idx+1) < args.length && args[idx].compareTo("-cw") == 0) {
 		temp = new Integer(args[idx+1]);
 		client_wait = temp.intValue();
 		idx += 2;
 		ok = true;
 	    }
 	    
 	    //get quiet mode
 	    if (idx < args.length && args[idx].compareTo("-q") == 0) {
 		quiet = true;
 		idx++;
 		ok = true;
 	    }
 
 	    //get streaming mode
 	    if (idx < args.length && args[idx].compareTo("-ns") == 0) {
 		streaming = false;
 		idx++;
 		ok = true;
 	    }
 	    
 	    // get prettyprint mode
 	    if (idx < args.length && args[idx].compareTo("-pp") == 0) {
 		prettyPrint = true;
 		idx++;
 		ok = true;
 	    }
    
            //get rate
            if ((idx+1) < args.length && args[idx].equals("-r")) {
                temp = new Integer(args[idx+1]);
                rate = temp.intValue();
                idx += 2;
                ok = true;
            }
 
             // get trace
 	    if(idx < args.length && args[idx].compareTo("-tr") == 0) {
                trace = args[idx+1];
	       idx += 2;
 	       ok = true;
 	    }
 	    
 	    //get shutdown
 	    if (idx < args.length && args[idx].compareTo("-shutdown") == 0) {
 		shutdown = true;
 		idx++;
 		ok = true;
 	    }
 	}
 
     if (ok == false) {
       StringBuffer stUsage = new StringBuffer("Usage: \n\t");
       stUsage.append("SimpleClient -s type [-d descriptor] [-p port_number]");
       stUsage.append(" [-h host_name] [-ng numGenCalls]");
       stUsage.append(" [-tl numTopLevelElts] [-cw client_wait]");
       stUsage.append(" [-ns ] (no streaming) [-q] (quiet)");
      stUsage.append(" [-pp ] (prettyprint) [-r rate] [-tr trace_filename] (trace) [-shutdown]");
       System.out.println(stUsage.toString());
     }
 
   return ok;
   }
 
   void run() {
     try {
       Timer tm = new Timer();
       FirehoseClient fhclient = new FirehoseClient();
       if (shutdown == true) {
         shutdown_server(port_num, host_name);
         return;
       }
 
       tm.start();
       FirehoseSpec fhSpec = new FirehoseSpec(port_num, host_name,
 					     stype, stream_descriptor,
 					     stream_descriptor2,
					     numGenCalls, numTLElts,
					     rate, streaming, prettyPrint,
 					     trace);
 
       InputStream is = fhclient.open_stream(fhSpec);
       byte buffer[] = new byte[1024];
       int numRead = 0;
       int count = 0;
 
       // KT - not sure if this is the right way to transfer from
       // is to output - use char reader?? or buffered reader??
       numRead = is.read(buffer, 0, 1024);
       while (numRead >0) {
 	  if(!quiet)
 	      System.out.write(buffer, 0, numRead);
 	  count++;
           if (count == client_wait && client_wait != -1) {
 	      fhclient.close_stream();
           } else {
 	      numRead = is.read(buffer, 0, 1024);
 	  }
       }
 
       tm.stop();
       tm.print();
 
       System.out.println("Received " + count + " messages");
     } catch (Exception ex) {
       System.out.print("SimpleClient.run() - Exception caught : '");
       System.out.print(ex.getMessage());
       System.out.println("'");
     }
   }
 
   int shutdown_server(int _port_num, String _host_name) {
     FirehoseClient fhclient = new FirehoseClient();
     fhclient.shutdown_server();
     return 0;
   }
 }
 
 class SimpleClient {
   public static void main(String args[]) {
     ClientMain cm = new ClientMain();
 
     if (cm.init(args) == true)
       cm.run();
   }
 }
 
 
