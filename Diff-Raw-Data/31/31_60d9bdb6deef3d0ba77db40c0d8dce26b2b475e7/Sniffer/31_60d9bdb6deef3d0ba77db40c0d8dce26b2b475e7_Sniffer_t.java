 import jpcap.*;
 import jpcap.packet.*;
 
 class Sniffer implements PacketReceiver {
     static int protocol; // chikka = 0, meebo = 1
    static JpcapCaptor jpcap;  
     public void receivePacket(Packet packet) {
         String data = new String(packet.data);
         String pack = new String(packet.toString());
 
 
         if (protocol == 0){
            		if(data.indexOf("message")!=-1){
            		    System.out.println("-----------------------START------------------------");
            		    System.out.println(pack);
                     System.out.println(data);
            		    System.out.println("-----------------------END------------------------");
            		}
        	}
        	else if(protocol ==1){

           	    if((data.indexOf("sender=")!=-1) ||
           	       ((data.indexOf("clientId=")!=-1) && (data.indexOf("GET")==-1))){
           	        System.out.println("-----------------------START------------------------");
               		System.out.println("SENT!!!"); 
               	    System.out.println(pack);
                	    System.out.println(data);
           	        System.out.println("-----------------------END--------------------------");  
            	    }
           	    if (data.startsWith("HTTP/1.1 200")&& (data.indexOf("sender")!=-1)){
                		System.out.println("-----------------------START------------------------");
               		System.out.println("RECEIVED!!!");
                	    System.out.println(data);
               	    
                    System.out.println("-----------------------END--------------------------");
                }
        	}
 
    	
     }
     public static void main(String[] args) throws Exception {
 		NetworkInterface[] devices = JpcapCaptor.getDeviceList();
 		if (args.length<2) {
 			System.out.println("usage: sudo java Sniffer <select a number of desired device> <chikka or meebo>");
 			for (int i = 0; i < devices.length; i++) {
 				System.out.println(i + " :" + devices[i].name + "(" + devices[i].description + ")");
 				System.out.println("    data link:" + devices[i].datalink_name + "(" + devices[i].datalink_description + ")");
 				System.out.print("    MAC address:");
 				for (byte b : devices[i].mac_address) {
 					System.out.print(Integer.toHexString(b&0xff) + ":");
 				}
 				System.out.println();
 				for (NetworkInterfaceAddress a : devices[i].addresses) {
 					System.out.println("    address:"+a.address + " " + a.subnet + " " + a.broadcast);
 				}
 			}
 		}
 		else {
			jpcap = JpcapCaptor.openDevice(devices[Integer.parseInt(args[0])], 2000, true, 20);
 			if (args[1].equals("chikka")) {
 				jpcap.setFilter("host chikka.com", true);
 				protocol = 0;
 				//jpcap.setFilter("port 80", true);
 				//File f = new File("meebo");
 				//f.mkdir();
 			}
 			else if (args[1].equals("meebo")) {
    				protocol = 1;
 				jpcap.setFilter("host meebo.com", true);   				
 				//jpcap.setFilter("port 5050", true);
 				//jpcap.setFilter("port 20", true);
 				//File f = new File("ym");
 				//f.mkdir();
 			}/*
 			else {
 				System.err.println("Choose between meebo or ym.");
 				System.exit(1);
 			}*/
 			jpcap.loopPacket(-1, new Sniffer());
 		}
 	}
 }
