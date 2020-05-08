 package versionThreads;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import rrd4jLib.ThreadsTest2;
 
 /**
  * Malware Capture Facility CVUT University, Prague, Czech Republic
  * 
  * These files were generated as part of a research project in the CVUT
  * University, Prague, Czech Republic. The goal is to store long-lived real
  * botnet traffic and to generate labeled netflows files. Any question feel free
  * to contact us: Sebastian Garcia, sebastian.garcia@agents.fel.cvut.cz Vojtech
  * Uhlir <vojtech.uhlir@agents.fel.cvut.cz
  * 
  */
 public class ReadParseAndUpdateVariablesThread extends Thread {
     String frame[];
 
     @Override
     public void run() {
 	BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
 
 	String line;
 
 	try {
 	    while ((line = input.readLine()) != null) {
 
 		ThreadsTest2.lock.writeLock().lock();
 		try {
 		    read(line);
		    sleep(1);
 		} catch (Exception ex) {
 		    ex.printStackTrace();
 		} finally {
 		    // printVariableRRDfile();
 		    ThreadsTest2.lock.writeLock().unlock();
 		}
 	    }
 	} catch (IOException e) {
 	    e.printStackTrace();
 	}
 
     }
 
     private void read(String line) {
 	frame = line.split("-");
 	for (int i = 0; i < frame.length; i++) {
 	    if (!frame[i].isEmpty()) {
 		parseFrame(frame, i);
 	    }
 
 	}
     }
 
     private void parseFrame(String[] frame, int i) {
 	switch (i) {
 	case 0:
 	    parseTime(frame[0]);
 	    break;
 	case 1:
 	    parseSize(frame[1]);
 	    break;
 	case 2:
 	    parseNetworkType(frame[2]);
 	    break;
 	case 3:
 	    parseTCPportDst(frame[3], frame[4]);
 	    break;
 	case 5:
 	    parseUDPportDst(frame[5]);
 	    break;
 	default:
 	    break;
 	}
     }
 
     private static void parseTime(String dateInString) {
 	dateInString = dateInString.substring(0, dateInString.length() - 6);
 	// Oct 4, 2013 13:57:41.242398000
 	// Sep 18, 2013 13:54:47.509564000
 	String pattern = "MMM dd, yyyy HH:mm:ss.SSS";
 	SimpleDateFormat formatter = new SimpleDateFormat(pattern);
 
 	Date date;
 	try {
 	    date = formatter.parse(dateInString);
 	    // System.out.println(date);
 	    // System.out.println(date.getTime());
 	    DataSummary.lastFrameTime = date.getTime();
 
 	    if (DataSummary.lastFrameTime < 15000000000L) {
 		// System.out.println(new Date(lastFrameTime));
 		// 15000000000 = GMT: Tue, 23 Jun 1970 14:40:00 GMT in
 		// MILICSECONDS!!!!
 		// timeToAdd = System.currentTimeMillis();
 		DataSummary.lastFrameTime = DataSummary.lastFrameTime + DataSummary.timeToAdd;
 		// System.out.println(new Date(lastFrameTime));
 	    } else {
 		// timeToAdd = 0;
 		// lastFrameTime = lastFrameTime + timeToAdd;
 
 		DataSummary.lastFrameTime = DataSummary.lastFrameTime + 0;
 	    }
 
 	    // if (DataSummary.lastEndOfWindow == 0) {
 	    // DataSummary.lastEndOfWindow = DataSummary.lastFrameTime;
 	    // }
 
 	} catch (ParseException e) {
 	    e.printStackTrace();
 	}
 
     }
 
     private void parseSize(String bytesOfFrame) {
 	DataSummary.window_sizeOfAll_frames_inbytes = DataSummary.window_sizeOfAll_frames_inbytes
 		+ Long.parseLong((String) bytesOfFrame);
     }
 
     private void parseNetworkType(String ethType) {
 	switch (ethType) {
 	case "0x0800":
 	    DataSummary.window_packetSum_IPv4++;
 	    break;
 	case "0x08dd":
 	    DataSummary.window_packetSum_IPv6++;
 	    break;
 	}
 
     }
 
     private void parseTCPportDst(String tcpSyn, String tcpPort) {
 	if (tcpSyn.contentEquals("1")) {
//	    for (int i = 0; i < frame.length; i++) {
//		System.out.print(frame[i]);
//	    }	    
//	    System.out.println("tedddddddddd");
 	    DataSummary.window_datagramSumTCP_all++;
 	    switch (tcpPort) {
 	    case "443":
 		DataSummary.window_datagramSumTCP_SYN_port443++;
 		break;
 	    case "80":
 		DataSummary.window_datagramSumTCP_SYN_port80++;
 		break;
 	    case "25":
 		DataSummary.window_datagramSumTCP_SYN_port25++;
 		break;
 	    case "22":
 		DataSummary.window_datagramSumTCP_SYN_port22++;
 		break;
 	    case "587":
 		DataSummary.window_datagramSumTCP_SYN_port587++;
 		break;
 	    }
 	} 
 //	else{
 //	    DataSummary.window_datagramSumTCP_all++;
 //	}
     }
 
     private void parseUDPportDst(String udpPort) {
	DataSummary.window_datagramSumUDP_all++;
 	switch (udpPort) {
 	case "53":
 	    DataSummary.window_datagramSumUDP_port53++;
 	    break;
 	}
 
     }
 
     private void printVariableRRDfile() {
 	StringBuilder s = new StringBuilder();
 	s.append("update ");
 	s.append(DataSummary.rrdFileToUpdate);
 	s.append(" ");
 	// String x = String.valueOf(DataSummary.lastEndOfWindow);
 	// s.append(x.substring(0, x.length() - 3));
 	s.append(new Date().getTime() / 1000);
 	s.append(":");
 	s.append(Long.valueOf(DataSummary.window_datagramSumUDP_port53));
 	s.append(":");
 	s.append(Long.valueOf(DataSummary.window_datagramSumTCP_SYN_port25
 		+ DataSummary.window_datagramSumTCP_SYN_port587));
 	s.append(":");
 	s.append(Long.valueOf(DataSummary.window_datagramSumTCP_SYN_port80));
 	s.append(":");
 	s.append(Long.valueOf(DataSummary.window_datagramSumTCP_SYN_port443));
 	s.append(":");
 	s.append(Long.valueOf(DataSummary.window_datagramSumTCP_SYN_port22));
 	s.append(":");
 	s.append(Long.valueOf(DataSummary.window_datagramSumTCP_all));
 	s.append(":");
 	s.append(Long.valueOf(DataSummary.window_datagramSumUDP_all));
 	s.append(":");
 	s.append(Long.valueOf(DataSummary.window_packetSum_IPv6));
 
 	System.out.println(s.toString());
 
     }
 
 }
