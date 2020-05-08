 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package iso8583decoder;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Date;  
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.Scanner;
 import org.jnetpcap.Pcap;  
 import org.jnetpcap.packet.PcapPacket;  
 import org.jnetpcap.packet.PcapPacketHandler;
 import org.jnetpcap.packet.format.JFormatter;
 import org.jnetpcap.packet.format.TextFormatter;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.jnetpcap.packet.Payload;
 /**
  *
  * @author vili
  */
 public class Iso8583decoder {
     public static class ISO8583Packet{
         public int packetNo;
         public long timestamp;
         public String srcIP;
         public String dstIP;
         public String header;
         public String type;
         public String pmap;
         public String smap;
         public String p2;
         public String p3;
         public String p4;
         public String p5;
         public String p6;
         public String p7;
         public String p8;
         public String p9;
         public String p10;
         public String p11;
         public String p12;
         public String p13;
         public String p14;
         public String p15;
         public String p16;
         public String p17;
         public String p18;
         public String p19;
         public String p20;
         public String p21;
         public String p22;
         public String p23;
         public String p24;
         public String p25;
         public String p26;
         public String p27;
         public String p32;
         public String p35;
         public String p37;
         ISO8583Packet(){
             packetNo=0;
             srcIP="";
             dstIP="";
             timestamp=0;
             header="";
             type="";
             pmap="";
             smap="";
             p2="";
             p3="";
             p4="";
             p5="";
             p6="";
             p7="";
             p8="";
             p9="";
             p10="";
             p11="";
             p12="";
             p13="";
             p14="";
             p15="";
             p16="";
             p17="";
             p18="";
             p19="";
             p20="";
             p21="";
             p22="";
             p23="";
             p24="";
             p25="";
             p26="";
             p27="";
             p32="";
             p35="";        
             p37="";     
         }
         
     }
     static int Count=0;
     static File fpcktsAnalysis;
     static FileWriter fwpcktsAnalysis;
     static BufferedWriter bwpcktsAnalysis;
     
     private static final String IP_SEPERATOR = "(\\p{Space}|\\p{Punct})";
     private static final String IP_COMPONENT = "(1?[0-9]{1,2}|2[0-4][0-9]|25[0-5])";
     private static final Pattern IP_PATTERN = Pattern.compile("(?<=(^|[" + IP_SEPERATOR +"]))(" + IP_COMPONENT + "\\.){3}" + IP_COMPONENT + "(?=([" + IP_SEPERATOR + "]|$))");    
     static ArrayList<ISO8583Packet> ISOpackets0200 = new ArrayList<ISO8583Packet>();
     static ArrayList<ISO8583Packet> ISOpackets0210 = new ArrayList<ISO8583Packet>();
     
     public static void main(String[] args) throws IOException {
         System.out.println("*******************************************************");
         System.out.println("* ISO8583 Decoder for Piraeus Bank                    *");
         System.out.println("*                                                     *");
         System.out.println("* This decoder has been developed to facilitate       *");
         System.out.println("* troubleshooting of ISO8583 protocol in Piraeus Bank *");
         System.out.println("*                                                     *");
         System.out.println("* copyright (c) March 2013 - Vangelis Iliakis         *");
         System.out.println("* Version 1.0                                         *");
         System.out.println("*******************************************************");        
         System.out.println("");
         System.out.println("[>] Open pcap file for decoding");
         // TODO code application logic here
         /*************************************************************************** 
          * First we setup error buffer and name for our file 
          **************************************************************************/          
         final StringBuilder errbuf = new StringBuilder(); // For any error msgs  
         final String file = System.getProperty("user.dir")+ "\\Mar_27_14_54_14_Mar_29_09_53_12_countries.pcap";          
         final String pcktsAnalysis = System.getProperty("user.dir")+ "\\PacketsAnalysis.txt";          
         final String outputfile0200 = System.getProperty("user.dir")+ "\\TransactionsAnalysisBasedOnRequests0200.csv";          
         final String missing = System.getProperty("user.dir")+ "\\Missing.csv";          
 
         System.out.println("Working Directory = " + System.getProperty("user.dir"));
         System.out.printf("Opening file for reading: %s%n", file);  
   
         /*************************************************************************** 
          * Second we open up the selected file using openOffline call 
          **************************************************************************/  
         Pcap pcap = Pcap.openOffline(file, errbuf);  
   
         if (pcap == null) {  
             System.err.printf("Error while opening device for capture: "  
                 + errbuf.toString());  
             return;  
         }  
   
         /*************************************************************************** 
          * Third we create a packet handler which will receive packets from the 
          * libpcap loop. 
          **************************************************************************/  
         PcapPacketHandler<String> jpacketHandler = new PcapPacketHandler<String>() { 
             public String hexToBin(String s) {
                 return new BigInteger(s, 16).toString(2);
             }
             public String hex2bin(String userInput)
             {
                 String[]hex={"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};              
                 String[]binary={"0000","0001","0010","0011","0100","0101","0110","0111","1000","1001","1010","1011","1100","1101","1110","1111"};  
                 String result="";  
                 for(int i=0;i<userInput.length();i++)  
                 {  
                     char temp=userInput.charAt(i);
                     String temp2=""+temp+"";
                     for(int j=0;j<hex.length;j++)  
                     {  
                         if(temp2.equalsIgnoreCase(hex[j]))  
                         {  
                             result=result+binary[j];  
                         }  
                     }  
                 }  
                 return result;
             }
  
                
             public String findIP(String s)
             {
                 String ip ="";
                 Matcher m = IP_PATTERN.matcher(s);
                 while (m.find())
                 {
                     ip = m.group();                    
                 }                  
                 return ip;
             }
   
             public void nextPacket(PcapPacket packet,String tmpStr) {  
                 ISO8583Packet pck = new ISO8583Packet();
                 Count++;
                 String wStr="";
                 wStr="<PACKET>\n";
                 try {bwpcktsAnalysis.write(wStr);}
                 catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}
                 
                 wStr="PacketNo " +Count+" Received at"+new Date(packet.getCaptureHeader().timestampInMicros())+" caplen="+ packet.getCaptureHeader().caplen()+" len="+packet.getCaptureHeader().wirelen()+"\n";
                 try {bwpcktsAnalysis.write(wStr);}
                 catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}
                 
                 pck.timestamp = packet.getCaptureHeader().timestampInMicros();
                 pck.packetNo=Count;
 
                 
                 String packetString = packet.toString();
                 Scanner scanner = new Scanner(packetString);
                 while(scanner.hasNext())    
                 {
                     String tmp = scanner.nextLine();
                     if(tmp.contains("Ip:") && tmp.contains("source"))
                     {
                         pck.srcIP = findIP(tmp);
                         wStr="Source IP="+ pck.srcIP;
                         try {bwpcktsAnalysis.write(wStr);}
                         catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}
                         //System.out.print("Source IP="+ pck.srcIP);                        
                     }
                     if(tmp.contains("Ip:") && tmp.contains("destination"))
                     {
                         pck.dstIP = findIP(tmp);
                         wStr="  ------>  Destination IP="+pck.dstIP+"\n";
                         try {bwpcktsAnalysis.write(wStr);}
                         catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                        
                         //System.out.println("  ------>  Destination IP="+pck.dstIP);
                     }
                 }
                 Payload payload = new Payload();
                 if ( packet.hasHeader(payload) & payload.getLength()>50 ) {
                     wStr="payload length="+payload.getLength()+"\n";
                     try {bwpcktsAnalysis.write(wStr);}
                     catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                    
                     //System.out.printf("payload length=%d\n", payload.getLength());
                     //byte [] payloadContent = payload.getPayload();
                     byte[] payloadContent = payload.getByteArray(0, payload.size());
                     String strPayloadContent = new String(payloadContent);
 
                     String header=strPayloadContent.substring(2, 14);                    
                     String type=strPayloadContent.substring(14, 18);
                     pck.header=header;
                     pck.type=type;
                     //if( (type.matches("0200") && header.matches("ISO015000077")) | (type.matches("0210") && header.matches("ISO015000075")) )
                     if(type.matches("0210") | type.matches("0200"))
                     {     
                         wStr="HEADER="+header+"\n";
                         try {bwpcktsAnalysis.write(wStr);}
                         catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}
                         //System.out.println("HEADER="+header);
                         wStr="TYPE="+type+"\n";
                         try {bwpcktsAnalysis.write(wStr);}
                         catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                        
                         //System.out.println("TYPE="+type);
                         String PMAP = strPayloadContent.substring(18, 34);
                         pck.pmap=PMAP;
                         wStr="PMAP="+PMAP+"\n";
                         try {bwpcktsAnalysis.write(wStr);}
                         catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                        
 
                         //System.out.println("PMAP="+PMAP);
                         String PMAPbin=hex2bin(PMAP);
                         wStr="PMAP Bin="+PMAPbin+"\n";
                         try {bwpcktsAnalysis.write(wStr);}
                         catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                        
 
                         //System.out.println(PMAPbin);
                         //find which fields we should check
                         //
                         //                      P2   P3   P4   P5*  P6*  P7  P8*  P9*  P10* P11 P12 P13 P14  P15 P16 P17 P18 P19* P20* P21* P22 P23 P24* P25 P26* P27
             //* =not used by Base24
             //P28,29,30,31 not defined
             //if found please skip packet proc
                         int P_fields_len[]={    16 , 6  , 12 , 12 , 12 , 10 , 8  , 8  ,8  , 6  ,6  ,4  ,4   ,4  ,4  ,4 , 4  ,3   ,3    ,3  ,3   ,3  ,3  ,2   ,2  ,1  };
                         int pad=0;
                         int offset=0;                        
                         offset=34;
                         if(PMAP.startsWith("8") | PMAP.startsWith("9") | PMAP.startsWith("A") | PMAP.startsWith("B") | PMAP.startsWith("C") | PMAP.startsWith("D") | PMAP.startsWith("E") | PMAP.startsWith("F"))
                         {
                             //Secondary Bitmap exists                                    
                             pad=16;
                             pck.smap=strPayloadContent.substring(offset, offset+pad);
                             wStr="SMAP="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                    
                             //System.out.println("SMAP="+strPayloadContent.substring(offset, offset+pad)); 
                             offset+=pad;
                         }
                         //Check PMAPbin to find what to read
                         //check if P2 is present
                         if(PMAPbin.charAt(1)=='1')
                         {
                             pad=2;
                             int P2count=Integer.parseInt(strPayloadContent.substring(offset, offset+pad));
                             wStr="Count for P2="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                                                
                             //System.out.println("Count for P2="+strPayloadContent.substring(offset, offset+pad));
                             offset+=pad;
                             
                             pad=P2count;
                             pck.p2=strPayloadContent.substring(offset, offset+pad);
                             wStr="P2 (CARD NUMBER)="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                                                                            
                             //System.out.println("P2 (CARD NUMBER)="+strPayloadContent.substring(offset, offset+pad));
                             offset+=pad;                            
                         }                        
                         //check if P3 is present
                         if(PMAPbin.charAt(2)=='1')
                         { 
                             pad=6; 
                             pck.p3=strPayloadContent.substring(offset, offset+pad);
                             wStr="P3 (Processing Code)="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                                                                            
                             //System.out.println("P3 (Processing Code)="+strPayloadContent.substring(offset, offset+pad));
                             offset+=pad;
                         }
                         
                         //check if P4 is present
                         if(PMAPbin.charAt(3)=='1')
                         {
                             pad=12;
                             pck.p4=strPayloadContent.substring(offset, offset+pad);
                             wStr="P4 (Transaction Amount)="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                                                                                                        
                             //System.out.println("P4 (Transaction Amount)="+strPayloadContent.substring(offset, offset+pad));
                             offset+=pad;
                         }
                         
                         //check if P5 is present
                         if(PMAPbin.charAt(4)=='1')
                         {
                             pad=12;
                             pck.p5=strPayloadContent.substring(offset, offset+pad);
                             wStr="P5="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                                                                            
                             //System.out.println("P5="+strPayloadContent.substring(offset, offset+pad));
                             offset+=pad;
                         }
 
                         //check if P6 is present
                         if(PMAPbin.charAt(5)=='1')
                         {
                             pad=12;
                             pck.p6=strPayloadContent.substring(offset, offset+pad);
                             wStr="P6="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                                                                                                        
                             //System.out.println("P6="+strPayloadContent.substring(offset, offset+pad));
                             offset+=pad;
                         }                        
                         
                         //check if P7 is present
                         if(PMAPbin.charAt(6)=='1')
                         {
                             pad=10;
                             pck.p7=strPayloadContent.substring(offset, offset+pad);
                             wStr="P7 (Transmission Date and Time)="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                                                                                                                                    
                             //System.out.println("P7 (Transmission Date and Time)="+strPayloadContent.substring(offset, offset+pad));
                             offset+=pad;
                         }
                         
                         //check if P8 is present
                         if(PMAPbin.charAt(7)=='1')
                         {
                             pad=8;
                             pck.p8=strPayloadContent.substring(offset, offset+pad);
                             wStr="P8="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                                                                                                        
                             //System.out.println("P8="+strPayloadContent.substring(offset, offset+pad));
                             offset+=pad;
                         }                           
                         
                         //check if P9 is present
                         if(PMAPbin.charAt(8)=='1')
                         { 
                             pad=8;
                             pck.p9=strPayloadContent.substring(offset, offset+pad); 
                             wStr="P9="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                                                                                                                                    
                             //System.out.println("P9="+strPayloadContent.substring(offset, offset+pad)); 
                             offset+=pad; 
                         }                                                   
                         
                         //check if P10 is present
                         if(PMAPbin.charAt(9)=='1')
                         { 
                             pad=8; 
                             pck.p10=strPayloadContent.substring(offset, offset+pad); 
                             wStr="P10="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                                                                                                                                                                
                             //System.out.println("P10="+strPayloadContent.substring(offset, offset+pad)); 
                             offset+=pad; 
                         }      
                         
                         //check if P11 is present
                         if(PMAPbin.charAt(10)=='1')
                         { 
                             pad=6; 
                             pck.p11=strPayloadContent.substring(offset, offset+pad); 
                             wStr="P11 (Systems Trace Audit Number)="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                                                                                                                                                                                            
                             //System.out.println("P11 (Systems Trace Audit Number)="+strPayloadContent.substring(offset, offset+pad)); 
                             offset+=pad;
                         }
                         
                         //check if P12 is present
                         if(PMAPbin.charAt(11)=='1')
                         { 
                             pad=6; 
                             pck.p12=strPayloadContent.substring(offset, offset+pad); 
                             wStr="P12 (Local Transaction Time)="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                                                                                                                                                                                                                        
                             //System.out.println("P12 (Local Transaction Time)="+strPayloadContent.substring(offset, offset+pad)); 
                             offset+=pad; 
                         }
                         
                         //check if P13 is present
                         if(PMAPbin.charAt(12)=='1')
                         { 
                             pad=4; 
                             pck.p13=strPayloadContent.substring(offset, offset+pad); 
                             wStr="P13 (Local Transaction Date)="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                                                                                                                                                                                                                                                    
                             //System.out.println("P13 (Local Transaction Date)="+strPayloadContent.substring(offset, offset+pad)); 
                             offset+=pad;
                         }
                         
                         //check if P14 is present
                         if(PMAPbin.charAt(13)=='1')
                         {
                             pad=4; 
                             pck.p14=strPayloadContent.substring(offset, offset+pad); 
                             wStr="P14="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                                                                                                                                                                                                                                                                                
                             //System.out.println("P14="+strPayloadContent.substring(offset, offset+pad)); 
                             offset+=pad; 
                         }
                         
                         //check if P15 is present
                         if(PMAPbin.charAt(14)=='1')
                         { 
                             pad=4; 
                             pck.p15=strPayloadContent.substring(offset, offset+pad); 
                             wStr="P15="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                                                                                                                                                                                                                                                                                                            
                             //System.out.println("P15="+strPayloadContent.substring(offset, offset+pad));
                             offset+=pad; 
                         }
 
                         //check if P16 is present
                         if(PMAPbin.charAt(15)=='1')
                         { 
                             pad=4; 
                             pck.p16=strPayloadContent.substring(offset, offset+pad); 
                             wStr="P16="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}
                             //System.out.println("P16="+strPayloadContent.substring(offset, offset+pad)); 
                             offset+=pad; 
                         }
                         
                         //check if P17 is present
                         if(PMAPbin.charAt(16)=='1')
                         { 
                             pad=4; 
                             pck.p17=strPayloadContent.substring(offset, offset+pad); 
                             wStr="P17 (Expiration Date)="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                            
                             //System.out.println("P17 (Expiration Date)="+strPayloadContent.substring(offset, offset+pad)); 
                             offset+=pad; 
                         }
 
                         //check if P18 is present
                         if(PMAPbin.charAt(17)=='1')
                         { 
                             pad=4; 
                             pck.p18=strPayloadContent.substring(offset, offset+pad); 
                             wStr="P18="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                        
                             //System.out.println("P18="+strPayloadContent.substring(offset, offset+pad)); 
                             offset+=pad; 
                         }
 
                         //check if P19 is present
                         if(PMAPbin.charAt(18)=='1')
                         { 
                             pad=3; 
                             pck.p19=strPayloadContent.substring(offset, offset+pad); 
                             wStr="P19="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                                                    
                             //System.out.println("P19="+strPayloadContent.substring(offset, offset+pad)); 
                             offset+=pad; 
                         }                        
 
                         //check if P20 is present
                         if(PMAPbin.charAt(19)=='1')
                         { 
                             pad=3; 
                             pck.p20=strPayloadContent.substring(offset, offset+pad); 
                             wStr="P20="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                                                                                
                             //System.out.println("P20="+strPayloadContent.substring(offset, offset+pad)); 
                             offset+=pad; 
                         }                        
                         
                         //check if P21 is present
                         if(PMAPbin.charAt(20)=='1')
                         { 
                             pad=3; 
                             pck.p21=strPayloadContent.substring(offset, offset+pad); 
                             wStr="P21="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                                                                                                            
                             //System.out.println("P21="+strPayloadContent.substring(offset, offset+pad)); 
                             offset+=pad; 
                         }                        
 
                         //check if P22 is present
                         if(PMAPbin.charAt(21)=='1')
                         { 
                             pad=3; 
                             pck.p22=strPayloadContent.substring(offset, offset+pad); 
                             wStr="P22="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                                                                                                                                        
                             //System.out.println("P22="+strPayloadContent.substring(offset, offset+pad)); 
                             offset+=pad; 
                         }                        
 
                         //check if P23 is present
                         if(PMAPbin.charAt(22)=='1')
                         { 
                             pad=3; 
                             pck.p23=strPayloadContent.substring(offset, offset+pad); 
                             wStr="P23="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                                                                                                                                                                    
                             //System.out.println("P23="+strPayloadContent.substring(offset, offset+pad)); 
                             offset+=pad; 
                         }                                                
 
                         //check if P24 is present
                         if(PMAPbin.charAt(23)=='1')
                         { 
                             pad=3; 
                             pck.p24=strPayloadContent.substring(offset, offset+pad); 
                             wStr="P24="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                            
                             //System.out.println("P24="+strPayloadContent.substring(offset, offset+pad)); 
                             offset+=pad; 
                         }                                                
                         
                         //check if P25 is present
                         if(PMAPbin.charAt(24)=='1')
                         { 
                             pad=2; 
                             pck.p25=strPayloadContent.substring(offset, offset+pad); 
                             wStr="P25="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                        
                             //System.out.println("P25="+strPayloadContent.substring(offset, offset+pad)); 
                             offset+=pad; 
                         }                                                
 
                         //check if P26 is present
                         if(PMAPbin.charAt(25)=='1')
                         { 
                             pad=2; 
                             pck.p26=strPayloadContent.substring(offset, offset+pad); 
                             wStr="P26="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                                                    
                             //System.out.println("P26="+strPayloadContent.substring(offset, offset+pad)); 
                             offset+=pad; 
                         }                                                
 
                         //check if P27 is present
                         if(PMAPbin.charAt(26)=='1')
                         { 
                             pad=1; 
                             pck.p27=strPayloadContent.substring(offset, offset+pad); 
                             wStr="P27="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                                                                                
                             //System.out.println("P27="+strPayloadContent.substring(offset, offset+pad)); 
                             offset+=pad; 
                         }                                                
                         
                         //Check if P28/P29/P30/P31 - if one of them is present please skip cause we do not know how to handle
                         if(PMAPbin.charAt(27)=='1' | PMAPbin.charAt(28)=='1' | PMAPbin.charAt(29)=='1' | PMAPbin.charAt(30)=='1')
                         {
                             System.out.println("Processing skipped...");
                         }
                         else
                         {
                             pad=2;
                             wStr="Count for P32="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                                                                                                            
                             //System.out.println("Count for P32="+strPayloadContent.substring(offset, offset+pad));
                             int P32count=Integer.parseInt(strPayloadContent.substring(offset, offset+pad)); 
                             offset+=pad;
                             
                             pad=P32count;
                             pck.p32=strPayloadContent.substring(offset, offset+pad);
                             wStr="P32 (Acquiring Institution Identification Code)="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                                                                                                            
                             //System.out.println("P32 (Acquiring Institution Identification Code)="+strPayloadContent.substring(offset, offset+pad)); 
                             offset+=pad;
                             pad=2;
                             wStr="Count for P35="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                                                                                                                                        
                             //System.out.println("Count for P35="+strPayloadContent.substring(offset, offset+pad)); 
                                                         
                             int P35count=Integer.parseInt(strPayloadContent.substring(offset, offset+pad)); 
                             offset+=pad;
                             pad=P35count;
                             pck.p35=strPayloadContent.substring(offset, offset+pad);
                             wStr="P35 (Track 2 Data)="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                                                                                                                                                                    
                             //System.out.println("P35 (Track 2 Data)="+strPayloadContent.substring(offset, offset+pad)); 
                             offset+=pad;
                             pad=12;
                             pck.p37=strPayloadContent.substring(offset, offset+pad);
                             wStr="P37 (Retrieval Reference Number)="+strPayloadContent.substring(offset, offset+pad)+"\n";
                             try {bwpcktsAnalysis.write(wStr);}
                             catch (IOException ex) {System.out.println("Cannot write Packets Analysis file");}                                                                                                                                                                                                                                
                             //System.out.println("P37 (Retrieval Reference Number)="+strPayloadContent.substring(offset, offset+pad));                             
                         } 
                         //add to the appropriate list
                         if(pck.type.matches("0200"))
                         {
                             ISOpackets0200.add(pck);
                         }
                         else if (pck.type.matches("0210"))
                         {
                             ISOpackets0210.add(pck);
                         }
                     }                 
                 }
                 wStr="</PACKET>\n\n";
                 try {bwpcktsAnalysis.write(wStr);}
                catch (IOException ex) {System.out.println("Cannot write Packets Analysis file. Error in I/O");}                                                                                                                                                                                                                
                 //System.out.println("</PACKET>\n");                
             }  
         };  
   
         /*************************************************************************** 
          * Fourth we enter the loop and tell it to capture 10 packets. The loop 
          * method does a mapping of pcap.datalink() DLT value to JProtocol ID, which 
          * is needed by JScanner. The scanner scans the packet buffer and decodes 
          * the headers. The mapping is done automatically, although a variation on 
          * the loop method exists that allows the programmer to sepecify exactly 
          * which protocol ID to use as the data link type for this pcap interface. 
          **************************************************************************/  
 
         fpcktsAnalysis = new File(pcktsAnalysis);
         fwpcktsAnalysis = new FileWriter(fpcktsAnalysis.getAbsoluteFile());
         bwpcktsAnalysis = new BufferedWriter(fwpcktsAnalysis);
         try {  
             pcap.loop(-1, jpacketHandler,"");  
         } finally {  
         /*************************************************************************** 
          * Last thing to do is close the pcap handle 
          **************************************************************************/  
            pcap.close();  
         }
         bwpcktsAnalysis.close();
         
         
         System.out.println("[>] Analyze transactions based on 0200 type");
         File file0200 = new File(outputfile0200);
         FileWriter fw = new FileWriter(file0200.getAbsoluteFile());
         BufferedWriter bw = new BufferedWriter(fw);
         
 			
         //Check data in lists
         String str="";
         str="From;To;RRN Request;Packet No Request;RRN Response;Packet No Response;Request Time;Response Time;Time Difference\n";
         //System.out.print(str);
         bw.write(str);
         for(int i=0;i<ISOpackets0200.size();i++)
         {
             //get first packet
             String p37_0200 = ISOpackets0200.get(i).p37;   
             //str=ISOpackets0200.get(i).srcIP+";"+ISOpackets0200.get(i).dstIP+";"+p37_0200+";"+ISOpackets0200.get(i).packetNo+";";
             //System.out.print(str);
             //bw.write(str);
             boolean found=false;
             for(int j=0;j<ISOpackets0210.size();j++)
             {                                
                 String p37_0210 = ISOpackets0210.get(j).p37;
                 if(p37_0200.matches(p37_0210) & !found)
                 {
                     long diff = (ISOpackets0210.get(j).timestamp-ISOpackets0200.get(i).timestamp)/1000; //in ms
                     if(diff>=0)
                     {
                         str=ISOpackets0200.get(i).srcIP+";"+ISOpackets0200.get(i).dstIP+";"+p37_0200+";"+ISOpackets0200.get(i).packetNo+";"+p37_0210+";"+ISOpackets0210.get(j).packetNo+";"+ISOpackets0200.get(i).timestamp+";"+ISOpackets0210.get(j).timestamp+";"+diff+"\n";
                         //System.out.print(str);
                         bw.write(str);
                         found=true;
                     }
                     else
                     {
                         System.out.println("ISO (0200) Packet No "+ISOpackets0200.get(i).packetNo+" and ISO (0210) Packet No " + ISOpackets0210.get(j).packetNo + " match but time elapsed is negative with RRNs:"+ISOpackets0200.get(i).p37+"=="+ISOpackets0210.get(j).p37);
                     }
                     
                 }
             }
             if(!found)
             {
                 //str="N/A"+";"+ISOpackets0200.get(i).timestamp+";"+"N/A"+";"+"N/A"+"\n";
                 //System.out.print(str);
                 //bw.write(str);
             }                        
         }
         bw.close();
 
         
         System.out.println("[>] Find mismatches between requests - responses");
         File fmissing = new File(missing);
         FileWriter fwmissing = new FileWriter(fmissing.getAbsoluteFile());
         BufferedWriter bwmissing = new BufferedWriter(fwmissing);        
         //str="Start by checking 0200 and find which transactions are not followed by response of type 0210\n";
         str="Transactions of type 0200 where we cannot find response of type 0210\n";
         bwmissing.write(str);        
         str="Packet No Request;From;To;RRN\n";
         bwmissing.write(str);
         for(int i=0;i<ISOpackets0200.size();i++)
         {
             //get first packet
             String p37_0200 = ISOpackets0200.get(i).p37;   
             boolean found=false;
             for(int j=0;j<ISOpackets0210.size();j++)
             {                                
                 String p37_0210 = ISOpackets0210.get(j).p37;
                 if(p37_0200.matches(p37_0210) & !found)
                 {
                     found=true;
                 }
             }
             if(!found)
             {
                 
                 str=ISOpackets0200.get(i).packetNo+";"+ISOpackets0200.get(i).srcIP+";"+ISOpackets0200.get(i).dstIP+";"+ISOpackets0200.get(i).p37+"\n";
                 bwmissing.write(str);
                 //System.out.print(str);
             }                        
         }
         
         str="Transactions of type 0210 where we cannot find request of type 0200\n";
         bwmissing.write(str);        
         str="Packet No Request;From;To;RRN\n";
         bwmissing.write(str);
         for(int i=0;i<ISOpackets0210.size();i++)
         {
             String p37_0210 = ISOpackets0210.get(i).p37;   
             boolean found=false;
             for(int j=0;j<ISOpackets0200.size();j++)
             {
                 String p37_0200 = ISOpackets0200.get(j).p37;
                 if(p37_0200.matches(p37_0210) & !found)
                 {
                     found=true;
                 }                
             }    
             if(!found)
             {                
                 str=ISOpackets0210.get(i).packetNo+";"+ISOpackets0210.get(i).srcIP+";"+ISOpackets0210.get(i).dstIP+";"+ISOpackets0210.get(i).p37+"\n";
                 bwmissing.write(str);
                 //System.out.print(str);
             }                                    
         }              
         bwmissing.close();
         
 
     }        
 }
 
 
 
 
 
 
 
 
 
 /********************************************************************************************************************************************/
 /*  COMMENTS SECTION                                                                                                                        */
 /********************************************************************************************************************************************/
 
 //                            pad=2;
 //                            System.out.println("Dummy="+strPayloadContent.substring(offset, offset+pad)); offset+=pad;
 //                            pad=16;
 //                            System.out.println("CARD NUMBER="+strPayloadContent.substring(offset, offset+pad)); offset+=pad;
 //                            pad=6;
 //                            System.out.println("P3 (Processing Code)="+strPayloadContent.substring(offset, offset+pad)); offset+=pad;
 //                            pad=12;
 //                            System.out.println("P4 (Transaction Amount)="+strPayloadContent.substring(offset, offset+pad)); offset+=pad;
 //                            pad=10;
 //                            System.out.println("P7 (Transmission Date and Time)="+strPayloadContent.substring(offset, offset+pad)); offset+=pad;
 //                            pad=6;
 //                            System.out.println("P11 (Systems Trace Audit Number)="+strPayloadContent.substring(offset, offset+pad)); offset+=pad;
 //                            pad=6;
 //                            System.out.println("P12 (Local Transaction Time)="+strPayloadContent.substring(offset, offset+pad)); offset+=pad;
 //                            pad=4;
 //                            System.out.println("P13 (Local Transaction Date)="+strPayloadContent.substring(offset, offset+pad)); offset+=pad;
 //                            pad=4;
 //                            System.out.println("P17 (Expiration Date)="+strPayloadContent.substring(offset, offset+pad)); offset+=pad;
 //                            pad=2;
 //                            System.out.println("Count for P32="+strPayloadContent.substring(offset, offset+pad));
 //                            int P32count=Integer.parseInt(strPayloadContent.substring(offset, offset+pad)); offset+=pad;
 //                            pad=P32count;
 //                            System.out.println("P32 (Acquiring Institution Identification Code)="+strPayloadContent.substring(offset, offset+pad)); offset+=pad;
 //                            pad=2;
 //                            System.out.println("Count for P35="+strPayloadContent.substring(offset, offset+pad)); 
 //                            int P35count=Integer.parseInt(strPayloadContent.substring(offset, offset+pad)); offset+=pad;
 //                            pad=P35count;
 //                            System.out.println("P35 (Track 2 Data)="+strPayloadContent.substring(offset, offset+pad)); offset+=pad;
 //                            pad=12;
 //                            System.out.println("P37 (Retrieval Reference Number)="+strPayloadContent.substring(offset, offset+pad)); 
 
 
 
 //                    if(type.matches("0210") && header.matches("ISO015000077"))
 //                    {
 //                        System.out.println("HEADER="+header);
 //                        System.out.println("TYPE="+type);                        
 //                        System.out.println("PMAP="+strPayloadContent.substring(18, 34));
 //                        System.out.println("SMAP="+strPayloadContent.substring(34, 50));                                                
 //                        System.out.println("Dummy="+strPayloadContent.substring(50, 52));                        
 //                    }
                     
                     
 //                    HEADER [ISO015000077]
 //                    TYPE   [0200]
 //                    PMAP   [B238800128E18010]
 //                    SMAP   [0000000000004004]
 //                    P-3    [010000]
 //                    P-4    [000000011600]
 //                    P-7    [1228141304]
 //                    P-11   [940134]
 //                    P-12   [141304]
 //                    P-13   [1228]
 //                    P-17   [1228]
 //                    P-32   {06}[888888]
 //                    P-35   {37}[                                     ]
 //                    P-37   [236314006941]
 //                    P-41   [00102008        ]
 //                    P-42   [000000001709628]
 //                    P-43   [LONGCHAMP HELLAS SA    PIRAEUS        GR]
 //                    P-48   {044}[A                       40000030000000000000]
 //                    P-49   [978]
 //                    P-60   {012}[            ]
 //                    S-114  {004}[V   ]
 //                    S-126  {300}[& 0000400300! B200158 7FF9000000000000E0008D9C034B3459C49200
 //                    00000116000000000000007C00012E3009781212280021AC99E4001406000A03A42002000
 //                    00000000000000000000000000000000000000000000000! B300080 FF0000000000E0F8
 //                    C000000000000000000000000000000000000000000000000000000000000000! B400020
 //                     05151000000000000000] 
 
 
 //                    HEADER [ISO015000075]
 //                    TYPE   [0210]
 //                    PMAP   [F23880012ED10010]
 //                    SMAP   [0000000000004004]
 //                    P-2    {16}[****************]
 //                    P-3    [012000]
 //                    P-4    [000000005120]
 //                    P-7    [1228121214]
 //                    P-11   [940131]
 //                    P-12   [141304]
 //                    P-13   [1228]
 //                    P-17   [1228]
 //                    P-32   {06}[888888]
 //                    P-35   {37}[*************************************]
 //                    P-37   [236314342019]
 //                    P-38   [422435]
 //                    P-39   [00]
 //                    P-41   [00056403        ]
 //                    P-42   [BORICA BULGARIA]
 //                    P-44   {25}[4000000044261000000003157]
 //                    P-48   {044}[A                       40000030000000000000]
 //                    P-60   {012}[            ]
 //                    S-114  {004}[V   ]
 //                    S-126  {348}[& 0000500348! B200158 7FF900000080800400006D18E245C739C93900
 //                    00000100000000000000003C00006A100975121228019642A38D001406000A03A02000000
 //                    00000000000000000000000000000000000000000000000! B300080 FF00000000006040
 //                    2000000000000000000000000000000000000000000000000000000000000000! B400020
 //                    05151000000000000040! B500038 001061BB093768CB88DA3030000000000000NN]
 
 
                 //ALL PACKET
                 //System.out.println(packet.toString());
 
                     //System.out.println("payload content = [" + strPayloadContent + "]");
 
                     //for (byte b:payloadContent) {
                     //    System.out.print(b+",");
                     //}
 
 
                 /*System.out.printf("%d Received at %s caplen=%-4d len=%-4d\n",Count,   
                     new Date(packet.getCaptureHeader().timestampInMillis()),   
                     packet.getCaptureHeader().caplen(), // Length actually captured  
                     packet.getCaptureHeader().wirelen() // Original length  
                     );  */
