 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Random;
 
 import javax.swing.JOptionPane;
 
 
 public class DataSend implements Runnable{
 	 
 	static Socket clientSocket = null;
      static PrintStream out = null;
      static InputStreamReader in = null;
      static ByteArrayOutputStream f;
      static boolean closed = false;
  
 	
 	public static void main(ArrayList<MessageStorage> messages) throws IOException {
 		
		int sleepTime = randomizeStartTime(10,1);
 		try {
			Thread.sleep(sleepTime*60*1000);
 		} catch (InterruptedException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 			
 		while (!socket_connect()){
 			
 		}
 		 
 		f = new ByteArrayOutputStream(); 
 	    // If everything has been initialized then we want to write some data
 	    // to the socket we have opened a connection to on port port_number
 
 	    if (clientSocket != null && out != null && in != null)
 	    {
 	      try
 	      {
 
 	        // Create a thread to read from the server
 
 	        new Thread(new DataSend()).start();
 	       
 	      
 	        for (int i=0;i<=messages.size()-1;i++){
 	        	
 	        	if (clientSocket != null && out != null && in != null){
 	        		if(f.size()==0){
 	        			out.write(create_message(messages.get(i))); 
 	        			System.out.println("Message "+(i+1)+ " is sended.");
 	        		}
 	        		else{
 	        			System.out.println("Sending messages from blackbox");
 	        			f.write(create_black_message(messages.get(i)));
 	        			out.write(send_black_message(f.toByteArray(),f.size()));
 	        			f.reset();
 	        		}
 		 		}
 	        	
 	        	else{
 	        		System.out.println("Message "+(i+1)+ " is not sended.Adding to blackbox");
 	        		f.write(create_black_message(messages.get(i)));
 	        		
 	        	}
 		 		
 		 		  
 		 		  
 			 		 try {
 			 			if (messages.get(i).getSpeed()==0){
 			 				Thread.sleep(120*1000);
 			 			}
 			 			else{
 			 				Thread.sleep(30*1000);
 			 			}
 						
 					} catch (InterruptedException e) {
 					
 						System.out.print("Timer error:" + e);	
 						e.printStackTrace();
 					}
 			 	
 			  
 	        
 	        }
 	        if(f.size()!=0){
 	        	while (clientSocket == null && out == null && in == null){
 	        	
 	        	}
 	        	
 	        	out.write(send_black_message(f.toByteArray(),f.size()));
     			f.reset();
 	        }
     		
 	        
 	 		 System.out.print("All data is sended.");	
 		     
 		        out.close();
 		        in.close();
		        clientSocket.close();
		        System.exit(1);
 	       }   
 	       
 	       catch (IOException e)
 	      {
 	        System.err.println("IOException:  " + e);
 	      }
 	    }
 	  }
 		
 	
 	@Override
 	public void run() {
 		 {
 			    int responseLine;
 
 			    // Keep on reading from the socket till we receive the "Bye" from the
 			    // server,once we received that then we want to break.
 			    try
 			    {
 			      while (!closed)
 			      {
 			    	char[] cbuf= new char[1024];
 			    	if ((responseLine = in.read(cbuf)) != -1){
 			    		/* responseLine = in.read();
 			    		 System.out.println("Answer:" + responseLine);
 					     responseLine = 0; */
 			    		
 				    	
 				    		 for (int i=0; i<=responseLine-1;i++){
 				    			 
 				    			 System.out.print("Answer: " + String.valueOf((byte)cbuf[i]+" "));
 				    			 System.out.println("");
 				    		 }
 				  
 			    	}
 			    	
 			       
 			      }
 			      closed = true;
 			    } catch (IOException e)
 			    {
 			    	System.err.println("IOException:  " + e);
 			    	while (!socket_connect()){
 						
 					}
 			    	  new Thread(new DataSend()).start();
 			    }
 			  }
 		
 	}	
 		
 	public static boolean socket_connect(){
 		int port_number = 20268;
 	    String host = "193.193.165.165";
 	    if (clientSocket != null && out != null && in != null){
 	    	
 	        try {
 	        	out.close();
 	        	out =null;
 				in.close();
 				in = null;
 				clientSocket.close();
 				clientSocket = null;
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	       
 	    }
 	   
 	    // Initialization section:
 	    // Try to open a socket on a given host and port
 	    // Try to open input and output streams
 	    try
 	    {
 	      clientSocket = new Socket(host, port_number);
 	      in = new InputStreamReader(
 	    		  clientSocket.getInputStream());
 	      out = new PrintStream(clientSocket.getOutputStream());
 	     
 	     
 	    } catch (UnknownHostException e)
 	    {
 	      System.err.println("Don't know about host " + host);
 	      return false;
 	    } catch (IOException e)
 	    {
 	      System.err.println("Couldn't get I/O for the connection to the host " + host);
 	      return false;
 	    }
 	
 	return true;}
 		
 	
 	
 	
 	
 public static byte[] send_black_message (byte[] bm, int size){
 		
 		byte[] black_message_end = new byte [5+size];
 		
 		black_message_end[0]=0x01;
 		
 		
 		
 		//  2 !
 			byte[] message_length = intToByteArray(size);
 			for (int i=0;i<=1;i++){
 				black_message_end[i+1]=message_length[3-i];
 			}	
 			
			black_message_end[2] &= (byte) 0x80;
 			
 			for (int i=0;i<=size-1;i++){
 				black_message_end[i+3] = bm[i];
 			}
 		
 			CRC16 crc1 = new CRC16();
 			 crc1.reset();
 		     crc1.update(black_message_end,0,black_message_end.length-2);
 		     long crc16 = crc1.getValue();
 			
 			
 			
 			byte[] crc16_byte = intToByteArray((int)crc16);
 			
 			for (int i=0;i<=1;i++){
 				black_message_end[i+black_message_end.length-2]=crc16_byte[3-i];
 				}
 		
 			/* for (int i=0;i<=black_message_end.length-1;i++){
 					System.out.print(Integer.toHexString((char)black_message_end[i])+" ");
 				}
 				System.out.println();	*/
 			
 			
 		return black_message_end;}
 		
 	
 	
 	
 	//    
 		
 	public static byte[] create_black_message (MessageStorage messageStorage){
 		byte[] black_message = new byte[63]; 
 		
 		
 		String imei = messageStorage.getImei();
 		  
 		  int latitude;
 			  if (messageStorage.getLat()==0){
 				  latitude= messageStorage.getLat();
 		      }
 		      else{
 		    	  latitude= messageStorage.getLat()+ randomizeMe(30,0);
 		      }
 		  
 		  int longitude;
 			  if (messageStorage.getLon()==0){
 				  longitude= messageStorage.getLon();
 		      }
 		      else{
 		    	  longitude= messageStorage.getLon()+ randomizeMe(30,0);
 		      }
 		  
 		  int speed;
 			  if (messageStorage.getSpeed()==0){
 		    	  speed= messageStorage.getSpeed();
 		      }
 		      else{
 		    	  speed= messageStorage.getSpeed()+ randomizeMe(5,0); 
 		      }
 	      
 		  int height = messageStorage.getHeight()+ randomizeMe(5,0); 
 	      int hdop = messageStorage.getHdop()+ randomizeMe(2,0);
 	      
 	      int pwr_ext;
 		      if (messageStorage.getPwr_ext()==0){
 		    	  pwr_ext= messageStorage.getPwr_ext();
 		      }
 		      else{
 		    	  pwr_ext= messageStorage.getPwr_ext()+ randomizeMe(200,0); 
 		      }
 	      
 		  int pwr_int;
 		      if (messageStorage.getPwr_int()==0){
 		    	  pwr_int= messageStorage.getPwr_int();
 		      }
 		      else{
 		    	  pwr_int= messageStorage.getPwr_int()+ randomizeMe(200,0);
 		      }
 	      
 		  int adc1;
 			  if (messageStorage.getAdc1()==0){
 				  adc1= messageStorage.getAdc1();
 		      }
 		      else{
 		    	  adc1= messageStorage.getAdc1()+ randomizeMe(200,0);
 		      }
 	     
 		  int adc2;
 			  if (messageStorage.getAdc2()==0){
 				  adc2= messageStorage.getAdc2();
 		      }
 		      else{
 		    	  adc2= messageStorage.getAdc2()+ randomizeMe(200,0);
 		      }
 	      int sats = messageStorage.getSats()+ randomizeMe(2,0);
 		
 	
 	    //  
 	      black_message[0]= 0x01;
 		//  1 
 	      black_message[1]= 14;
 			
 		//  	
 	      black_message[2]= 0x02;
 		//  1 	 
 	      black_message[3]=-99;
 			
 		
 		// Imei - 1 
 	      black_message[4]=0x03;
 			// Imei - 15 
 			byte[] b;
 				try {
 					b = imei.getBytes("US-ASCII");
 					
 					for (int i=0;i<=14;i++){
 						black_message[i+5]=b[i];
 					}
 				} catch (UnsupportedEncodingException e) {
 					JOptionPane.showMessageDialog(null, "   IMEI!");
 					e.printStackTrace();
 				}
 				
 				black_message[20]=04;
 				
 				    byte[] id_byte = intToByteArray(50);
 					for (int i=0;i<=1;i++){
 						black_message[i+21]=id_byte[3-i];
 					}	
 				
 		//  UTC - 1 
 					black_message[23]=0x20;
 		
 			// UTC - 4  (20,21,22,23)
 		
 			byte[] time_utc = intToByteArray((int) (System.currentTimeMillis()/1000));
 			
 			for (int i=0;i<=3;i++){
 				black_message[i+24]=time_utc[3-i];
 			}
 		
 		//  +  +  
 			black_message[28]=0x30;
 		//  +  +  - 9 
 			//  + 
 			black_message[29]=(byte) sats;
 			// 26-29
 			byte[] lon = intToByteArray(longitude);
 			for (int i=0;i<=3;i++){
 				black_message[i+30]=lon[3-i];
 			}
 			
 			byte[] lat = intToByteArray(latitude);
 			for (int i=0;i<=3;i++){
 				black_message[i+34]=lat[3-i];
 			}
 			// 30-33
 			
 		
 		// + 1 
 			black_message[38]=0x33;
 			//  - 2   35-36
 			byte[] speed_byte = intToByteArray(speed);
 			for (int i=0;i<=1;i++){
 				black_message[i+39]=speed_byte[3-i];
 			}
 			
 			//  - 2   37-38
 			black_message[41]=00;
 			black_message[42]=00;
 		
 		// 	
 			black_message[43]=0x34;
 			//  - 2   40-41
 			byte[] height_byte = intToByteArray(height);
 			for (int i=0;i<=1;i++){
 				black_message[i+44]=height_byte[3-i];
 		}
 		
 		
 		// Hdop				
 			black_message[46]=0x35;
 			// Hdop - 2   43-44
 			
 			byte[] hdop_byte = intToByteArray(hdop);
 			
 			black_message[47]=hdop_byte[3];		
 		
 		
 		// pwr_ext
 			black_message[48]=0x41;
 			// Pwr_ext - 2   46-47
 			
 			byte[] pwr_ext_byte = intToByteArray(pwr_ext);
 			for (int i=0;i<=1;i++){
 				black_message[i+49]=pwr_ext_byte[3-i];		
 		
 		}
 		// pwr_int
 			black_message[51]=0x42;
 			// pwr_int - 2   49-50
 			
 			byte[] pwr_int_byte = intToByteArray(pwr_int);
 			for (int i=0;i<=1;i++){
 				black_message[i+52]=pwr_int_byte[3-i];		
 				
 		}
 		
 		//  adc1  adc2
 			black_message[54]=0x46;
 			//c  adc1  adc2 , ( )	
 			black_message[55]=01; // Adc2 
 			black_message[56]=10; // Adc1 
 		
 		
 		
 		// adc1 
 			black_message[57]=0x50;
 			//c  adc1 2  55-56
 			
 			byte[] adc1_int_byte = intToByteArray(adc1);
 			for (int i=0;i<=1;i++){
 				black_message[i+58]=adc1_int_byte[3-i];	
 			}
 		// adc2
 			black_message[60]=0x51;
 			//c  adc1 2  58-59
 			
 			byte[] adc2_int_byte = intToByteArray(adc2);
 			for (int i=0;i<=1;i++){
 				black_message[i+61]=adc2_int_byte[3-i];   
 			}
 	      
 	      return black_message;}
 	
        
         
 	
 	
 	
 	// 
 	
 	public static byte[] create_message(MessageStorage messageStorage) {
 			
 		 
 		  
 		  String imei = messageStorage.getImei();
 		  
 		  int latitude;
 			  if (messageStorage.getLat()==0){
 				  latitude= messageStorage.getLat();
 		      }
 		      else{
 		    	  latitude= messageStorage.getLat()+ randomizeMe(30,0);
 		      }
 		  
 		  int longitude;
 			  if (messageStorage.getLon()==0){
 				  longitude= messageStorage.getLon();
 		      }
 		      else{
 		    	  longitude= messageStorage.getLon()+ randomizeMe(30,0);
 		      }
 		  
 		  int speed;
 			  if (messageStorage.getSpeed()==0){
 		    	  speed= messageStorage.getSpeed();
 		      }
 		      else{
 		    	  speed= messageStorage.getSpeed()+ randomizeMe(5,0); 
 		      }
 	      
 		  int height = messageStorage.getHeight()+ randomizeMe(5,0); 
 	      int hdop = messageStorage.getHdop()+ randomizeMe(2,0);
 	      
 	      int pwr_ext;
 		      if (messageStorage.getPwr_ext()==0){
 		    	  pwr_ext= messageStorage.getPwr_ext();
 		      }
 		      else{
 		    	  pwr_ext= messageStorage.getPwr_ext()+ randomizeMe(200,0); 
 		      }
 	      
 		  int pwr_int;
 		      if (messageStorage.getPwr_int()==0){
 		    	  pwr_int= messageStorage.getPwr_int();
 		      }
 		      else{
 		    	  pwr_int= messageStorage.getPwr_int()+ randomizeMe(200,0);
 		      }
 	      
 		  int adc1;
 			  if (messageStorage.getAdc1()==0){
 				  adc1= messageStorage.getAdc1();
 		      }
 		      else{
 		    	  adc1= messageStorage.getAdc1()+ randomizeMe(200,0);
 		      }
 	     
 		  int adc2;
 			  if (messageStorage.getAdc2()==0){
 				  adc2= messageStorage.getAdc2();
 		      }
 		      else{
 		    	  adc2= messageStorage.getAdc2()+ randomizeMe(200,0);
 		      }
 	      int sats = messageStorage.getSats()+ randomizeMe(2,0);
 		
 		
 
 		byte[] message = new byte[68];
 	//      byte[] message = new byte[28];
 		
 		//   1 
 			
 			message[0]=0x01;
 			
 			
 			
 		//  2 !
 			byte[] message_length = intToByteArray(message.length-5);
 			for (int i=0;i<=1;i++){
 				message[i+1]=message_length[3-i];
 			}	
 			
 		//  
 			message[3]= 0x01;
 		//  1 
 			message[4]= 14;
 			
 		//  	
 			message[5]= 0x02;
 		//  1 	 
 			message[6]=-99;
 			
 		
 		// Imei - 1 
 			message[7]=0x03;
 			// Imei - 15 
 			byte[] b;
 				try {
 					b = imei.getBytes("US-ASCII");
 					
 					for (int i=0;i<=14;i++){
 						message[i+8]=b[i];
 					}
 				} catch (UnsupportedEncodingException e) {
 					JOptionPane.showMessageDialog(null, "   IMEI!");
 					e.printStackTrace();
 				}
 				
 				message[23]=04;
 				
 				    byte[] id_byte = intToByteArray(50);
 					for (int i=0;i<=1;i++){
 						message[i+24]=id_byte[3-i];
 					}	
 				
 		//  UTC - 1 
 			message[26]=0x20;
 		
 			// UTC - 4  (20,21,22,23)
 		
 			byte[] time_utc = intToByteArray((int) (System.currentTimeMillis()/1000));
 			
 			for (int i=0;i<=3;i++){
 				message[i+27]=time_utc[3-i];
 			}
 		
 		//  +  +  
 			message[31]=0x30;
 		//  +  +  - 9 
 			//  + 
 			message[32]=(byte) sats;
 			// 26-29
 			byte[] lon = intToByteArray(longitude);
 			for (int i=0;i<=3;i++){
 				message[i+33]=lon[3-i];
 			}
 			
 			byte[] lat = intToByteArray(latitude);
 			for (int i=0;i<=3;i++){
 				message[i+37]=lat[3-i];
 			}
 			// 30-33
 			
 		
 		// + 1 
 			 message[41]=0x33;
 			//  - 2   35-36
 			byte[] speed_byte = intToByteArray(speed);
 			for (int i=0;i<=1;i++){
 				message[i+42]=speed_byte[3-i];
 			}
 			
 			//  - 2   37-38
 			message[44]=00;
 			message[45]=00;
 		
 		// 	
 			 message[46]=0x34;
 			//  - 2   40-41
 			byte[] height_byte = intToByteArray(height);
 			for (int i=0;i<=1;i++){
 				message[i+47]=height_byte[3-i];
 		}
 		
 		
 		// Hdop				
 			message[49]=0x35;
 			// Hdop - 2   43-44
 			
 			byte[] hdop_byte = intToByteArray(hdop);
 			
 			message[50]=hdop_byte[3];		
 		
 		
 		// pwr_ext
 			message[51]=0x41;
 			// Pwr_ext - 2   46-47
 			
 			byte[] pwr_ext_byte = intToByteArray(pwr_ext);
 			for (int i=0;i<=1;i++){
 				message[i+52]=pwr_ext_byte[3-i];		
 		
 		}
 		// pwr_int
 			message[54]=0x42;
 			// pwr_int - 2   49-50
 			
 			byte[] pwr_int_byte = intToByteArray(pwr_int);
 			for (int i=0;i<=1;i++){
 			message[i+55]=pwr_int_byte[3-i];		
 				
 		}
 		
 		//  adc1  adc2
 			message[57]=0x46;
 			//c  adc1  adc2 , ( )	
 			message[58]=01; // Adc2 
 			message[59]=10; // Adc1 
 		
 		
 		
 		// adc1 
 			message[60]=0x50;
 			//c  adc1 2  55-56
 			
 			byte[] adc1_int_byte = intToByteArray(adc1);
 			for (int i=0;i<=1;i++){
 			message[i+61]=adc1_int_byte[3-i];	
 			}
 		// adc2
 			message[63]=0x51;
 			//c  adc1 2  58-59
 			
 			byte[] adc2_int_byte = intToByteArray(adc2);
 			for (int i=0;i<=1;i++){
 			message[i+64]=adc2_int_byte[3-i];   
 			}
 		
 			
 			
 		// CRC16 ModBus 2 
 			
 					
 			 CRC16 crc1 = new CRC16();
 			 crc1.reset();
 		     crc1.update(message,0,message.length-2);
 		     long crc16 = crc1.getValue();
 			
 			
 			
 			byte[] crc16_byte = intToByteArray((int)crc16);
 			
 			for (int i=0;i<=1;i++){
 				message[i+message.length-2]=crc16_byte[3-i];
 				}
 			
 	/*		 for (int i=0;i<=message.length-1;i++){
 				System.out.print(Integer.toHexString((char)message[i])+" ");
 			}
 			System.out.println();	*/
 	return message;}
 	
 	
 	//    
 	
 	 public static byte[] intToByteArray(int value) {
 		    return new byte[] {
 		            (byte)(value >>> 24),
 		            (byte)(value >>> 16),
 		            (byte)(value >>> 8),
 		            (byte)value};
 		}
 
 	 public static int randomizeMe(int max,int min) {
 		 Random r = new Random();
 		 int random_number = r.nextInt(max - min + 1) + min;
 		 int plus_minus = r.nextInt(max - min + 1) + min;
 		 if (0<=(random_number-plus_minus)){
 			 return random_number;
 		 }
 		 else {
 			 random_number = 0 -  random_number;
 			 return random_number;
 		 }
 		}
	 
	public static int randomizeStartTime (int max,int min) {
		 Random r = new Random();
		 int random_number = r.nextInt(max - min + 1) + min;
		 return random_number;}
 }
