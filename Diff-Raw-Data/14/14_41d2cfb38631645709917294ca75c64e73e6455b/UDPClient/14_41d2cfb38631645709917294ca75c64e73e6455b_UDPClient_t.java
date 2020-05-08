 //
 import java.net.*;
 import java.io.*;
 import javax.swing.*;
 
 public class UDPClient {
 	
 	public static void main (String args[]) throws Exception {
 		
 		//Use DataGramSocket for UDP connection
 		DatagramSocket s = new DatagramSocket();
 		
 		//Addr and port number created
 		InetAddress addr = InetAddress.getByName("localhost");
 		int port = 9001;
 		 try {
 	     		UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
 	     	} catch (Exception e) {
 	     		System.out.println("Unable to load Windows look and feel");
 	     	}
 		//Lets the user choose the file
 		JFileChooser chooser = new JFileChooser(".");
         chooser.showOpenDialog(null);
         File myFile = chooser.getSelectedFile();
         String filename = myFile.getName();
 		
         //Converts filename to byte for sending via DatagramPacket
         byte[] filenameByte = filename.getBytes();
        
         //Created a packet ( with destination addr and port )
         //Sends over the filename first
         DatagramPacket outFilename = new DatagramPacket(filenameByte, filenameByte.length,
         		addr, port);
         
         s.send(outFilename);
         
      // create a packet buffer to store data from packets received.
      		byte infilenameReply[] = new byte[1000];
      		DatagramPacket infilename = new DatagramPacket(infilenameReply, infilenameReply.length);
 
      		// receive the reply from server.
      		s.receive(infilename);
 
      		// convert reply to string and print to System.out
      		String reply = new String(infilename.getData(), 0, infilename.getLength());
      		if (reply.equalsIgnoreCase("Filename received!")) {    			
      			System.out.println(reply);
      			//Convert the file into byteArray
      			FileInputStream fis =  new FileInputStream(myFile);
      			long length = myFile.length();
      			if (length > Integer.MAX_VALUE){
      				System.out.println("File is too large");
      				System.exit(0);
      			}
      			
      			/**************************************************************
      			 * Our packets have a byte size of 65502
      			 * 65500 for data only
      			 * We use 2 extra bytes for 
      			 * 1. Counter for packet number
      			 * 2. Flag to indicate if the packet is the last packet or not 
      			 *************************************************************/
      			
      			// total number of packets to send
      			int total_packets = ((int)length / 65500) + 1;   					
      			// flag for indication of last packet
      			int last_packet = -1;    			
      			// 1 byte is used for the last packet flag
      			byte last_packetByte;
      			byte[] previousPacket = new byte[65502];
      			int packet_number = 0;
      			boolean canSend = true;
      			ACKTimer t = new ACKTimer();
      			
      	     			   			
      			while ( total_packets > 0) {
      				if (canSend) {
 	     				byte[] outBuf = new byte[65502];
 	     				packet_number++;
 	     				// packet numbering will start from 1
 	     				// 1 byte is used for keeping track of packet number
 	         			outBuf[65500] = (byte) packet_number;
 	         				// checks if the packet is the last packet
 	         				if (total_packets == 1) {
 	         				last_packet = 1;
 	         				last_packetByte = (byte) last_packet;
 	         				outBuf[65501] = last_packetByte;
 	         				}
 	         				else {
 	         					last_packet = 0;
 	         					last_packetByte = (byte) last_packet;
 	         					outBuf[65501] = last_packetByte;
 	         					
 	         				}
 	     				fis.read(outBuf,0,(int)length%65500);
 	    			
 	     			// Now create a packet (with destination addr and port)
 	     			// Sends over the actual file
 	     				DatagramPacket outPkt = new DatagramPacket(outBuf, outBuf.length,
 	     						addr, port);
 	     				s.send(outPkt);
	     				t = new ACKTimer(2); // 2 sec timeout
 	     				canSend = false;
 	     				previousPacket = outBuf;
 	     				
 	//     				System.out.println(packet_number+"");
 	     				total_packets--;
      				}
      				
      				byte inACKbyte[] = new byte[1000];
          			DatagramPacket inACK = new DatagramPacket(inACKbyte, inACKbyte.length);
          			s.receive(inACK); // receive the ack from server.
          			
 					String ACK = new String(inACK.getData(), 0, inACK.getLength());
  					int integer_ACK = Integer.parseInt(ACK);
  					
      				if (integer_ACK == packet_number+1) { // ack received and ack number is the next packet to send
      					t.StopTimer();
      					canSend = true;
      				}
 
      				if (t.isTimeOut()) {
      					DatagramPacket outPkt = new DatagramPacket(previousPacket, previousPacket.length,
 	     						addr, port);
 	     				s.send(outPkt);
	     				t = new ACKTimer(2); // 2 sec timeout
 	     				canSend = false;
      				}
      			}
      			
      			// create a packet buffer to store data from packets received.
      			byte inBuf[] = new byte[1000];
      			DatagramPacket inPkt = new DatagramPacket(inBuf, inBuf.length);
 
      			// receive the reply from server.
      			s.receive(inPkt);
 
      			// convert reply to string and print to System.out
      			String reply2 = new String(inPkt.getData(), 0, inPkt.getLength());
      			System.out.println(reply2);
      		}
      		else {
      			System.out.println("No acknowledgement from server that filename is received");
      			System.exit(0);
      		}
 	}
 }
 
 
