 package org.concord.CCProbe;
 
 import graph.*;
 import waba.util.*;
import waba.io.*;
import extra.io.*;
 
 public class DataExport 
 {
     static public void writeString(DataStream ds, String s)
     {
 		ds.writeFixedString(s, s.length());
     }
 
 	static String create2DigitString(int n){
 		String retValue = "";
 		if(n < 10) retValue += "0";
 		retValue += n;
 		return retValue;
 	}
 	static  String createNameFile(Bin b){
 		if(b == null ||  b.time == null) return null;
 		String retValue = "Data-" + b.time.year;
 		retValue += "_";
 		retValue += create2DigitString(b.time.month);
 		retValue += "_";
 		retValue += create2DigitString(b.time.day);
 		retValue += "-";
 		retValue += create2DigitString(b.time.hour);
 		retValue += "_";
 		retValue += create2DigitString(b.time.minute);
 		retValue += ".txt";
 		return retValue;
 	} 
 
     static public void export(Bin b, Vector points)
     {
 		int i;
 		if(b == null ||
 		   b.time == null) return;
 		//	String name = "Data-" + b.time.year + "_" + b.time.month + "_" + b.time.day + "-" +
 		//	    b.time.hour + "_" + b.time.minute + ".txt";
 		String name = createNameFile(b);
 		if(name == null) return;
 		File file = new File(name, File.CREATE);
 		file.close();
 		file = new File(name, File.READ_WRITE);
 
 		DataStream ds = new DataStream(file);
 	
 		writeString(ds, b.time.month + "/" + b.time.day + "/" + b.time.year + " " +
 					b.time.hour + ":" + b.time.minute + "\r\n");
 		writeString(ds, b.description + "\r\n");
 		if(points != null){
 			writeString(ds, "Marks:\r\n");
 			writeString(ds, "label\ttime\tvalue\r\n");
 			for(i=0; i < points.getCount(); i++){
 				DecoratedValue pt = (DecoratedValue)points.get(i);
 				writeString(ds, pt.getLabel() + "\t" + 
 							pt.getTime() + "\t" + 
 							pt.getValue() + "\r\n");
 			}
 
 		}
 
 		writeString(ds, b.label + "\r\n");
 		writeString(ds, "time\tvalue\r\n");
 		float curTime = 0f;
 		for(i=0; i < b.lfArray.getCount(); i++){
 			writeString(ds, curTime + "\t" + (b.lfArray.getFloat(i)+b.lfArray.ref) + "\r\n");
 			curTime += b.dT;
 		}
 
 		file.close();
 	
 		
     }
 
 
 
 }
