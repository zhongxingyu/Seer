 package ucv.tesis.tesisandroid;
 
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;

 
 
 
 import snmp.SNMPBERCodec;
 import snmp.SNMPBadValueException;
 import snmp.SNMPCounter32;
 import snmp.SNMPGauge32;
 import snmp.SNMPGetException;
 import snmp.SNMPIPAddress;
 import snmp.SNMPInteger;
 import snmp.SNMPObject;
 import snmp.SNMPObjectIdentifier;
 import snmp.SNMPOctetString;
 import snmp.SNMPPDU;
 import snmp.SNMPRequestException;
 import snmp.SNMPRequestListener;
 import snmp.SNMPSequence;
 import snmp.SNMPSetException;
 import snmp.SNMPTimeTicks;
 import snmp.SNMPVariablePair;
 
 public class Mibs implements SNMPRequestListener {
 
 	
 	SNMPOctetString storedSNMPValue;
 	
 	@Override
 	public SNMPSequence processRequest(SNMPPDU pdu, String communityName)
 			throws SNMPGetException, SNMPSetException {
 		// TODO Auto-generated method stub
 	    byte pduType = pdu.getPDUType();
         switch (pduType)
         {
             case SNMPBERCodec.SNMPGETREQUEST:
             {
             	 System.out.println("SNMPGETREQUEST\n");
                 break;
             }
             
             case SNMPBERCodec.SNMPGETNEXTREQUEST:
             {
             	 System.out.println("SNMPGETNEXTREQUEST\n");
                 break;
             }
             
             case SNMPBERCodec.SNMPSETREQUEST:
             {
             	 System.out.println("SNMPSETREQUEST\n");
                 break;
             }
             
             case SNMPBERCodec.SNMPGETRESPONSE:
             {
             	 System.out.println("SNMPGETRESPONSE\n");
                 break;
             }
             
             case SNMPBERCodec.SNMPTRAP:
             {
             	 System.out.println("SNMPTRAP\n");
                 break;
             }
             
             default:
             {
             	 System.out.println("unknown\n");
                 break;
             }
             
             
         }
         
         
         
         SNMPSequence varBindList = pdu.getVarBindList();
         SNMPSequence responseList = new SNMPSequence();
         
         for (int i = 0; i < varBindList.size(); i++)
         {
             SNMPSequence variablePair = (SNMPSequence)varBindList.getSNMPObjectAt(i);
             SNMPObjectIdentifier snmpOID = (SNMPObjectIdentifier)variablePair.getSNMPObjectAt(0);
             SNMPObject snmpValue = (SNMPObject)variablePair.getSNMPObjectAt(1);
             
             System.out.println("       OID:           " + snmpOID + "\n");
             System.out.println("       value:         " + snmpValue + "\n");
             
             
             // check to see if supplied community name is ours; if not, we'll just silently
             /* ignore the request by not returning anything
             if (!communityName.equals(this.communityName))
             {
                 continue;
             }*/
             
             
             if (snmpOID.toString().equals("1.3.6.1.2.1.1.4.0"))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                     // got a get-request for our variable; send back a value - just a string
                     try
                     {
                         SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPOctetString("Fernando Hidalgo"));
                         //SNMPVariablePair newPair = new SNMPVariablePair(snmpOID, new SNMPOctetString("Boo"));
                         responseList.addSNMPObject(newPair);
                     }
                     catch (SNMPBadValueException e)
                     {
                         // won't happen...
                     }
                 } 
                 
             }
             
             if (snmpOID.toString().equals("1.3.6.1.2.1.1.5.0"))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; supplied value must be a string
                     if (snmpValue instanceof SNMPOctetString)
                     {
                         // assign new value
                         storedSNMPValue = (SNMPOctetString)snmpValue;
                         
                         // return SNMPVariablePair to indicate we've handled this OID
                         try
                         {
                             SNMPVariablePair newPair = new SNMPVariablePair(snmpOID, storedSNMPValue);
                             responseList.addSNMPObject(newPair);
                         }
                         catch (SNMPBadValueException e)
                         {
                             // won't happen...
                         }
                     
                     }
                     else
                     {
                         int errorIndex = i+1;
                         int errorStatus = SNMPRequestException.BAD_VALUE;
                         throw new SNMPSetException("Supplied value must be SNMPOctetString", errorIndex, errorStatus);
                     }
                     
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                     // got a get-request for our variable; send back a value - just a string
                     try
                     {
                         SNMPVariablePair newPair = new SNMPVariablePair(snmpOID, storedSNMPValue);
                         responseList.addSNMPObject(newPair);
                     }
                     catch (SNMPBadValueException e)
                     {
                         // won't happen...
                     }
                 } 
                 
             } // if 
             
             
             
             /******************************
              *
              *   MIB-II | the Interfaces Group
              * 
              ******************************/
         
             /* ifNumber Integer | Read-Only | Mandatory */
             if (snmpOID.toString().equals("1.3.6.1.2.1.2.1.0"))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                     int nIf = 0; // numero de interfaces
                 	ArrayList<String> arrIf = new ArrayList<String>();
                 	String cmd="";
                 	try
                     {
                         Process p=null;
                         try {
 							p = Runtime.getRuntime().exec("ls /sys/class/net/");
 							BufferedReader in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 							String line;
 					        while ((line = in2.readLine()) != null) {  
 					        	nIf += 1; 
 					        	arrIf.add(line);
 					        }
 
 					        /** Crear tabla de if  **/
 					        
 					        String [] arrEntry = {
 					        		"type", /* ifType */
 					        		"mtu", /* ifMtu */
 					        		"speed", /* IfSpeed */
 					        		"address", /* ifPhyAddress */
 					        		"operstate", /* ifOperStatus */  
 					        		/**  ###FALTA### IfAdminStatus **/
 					        		"power/runtime_active_time", /* ifLastChange */
 					        		"statistics/rx_bytes", /*ifInOctets*/
 					        		"statistics/rx_packets", /*IfInUcastPkts*/
 					        		"statistics/multicast", /* ifInNUcastPkts*/
 					        		"statistics/rx_dropped", /* ifInDiscards*/
 					        		"statistics/rx_errors", /* ifInErrors*/
					        		"statistics/rx_missed", /* ifInUnknownProtos */
 					        		"statistics/tx_bytes", /* ifOutOctets */
 					        		"statistics/tx_packets", /* ifOutUcastPkts */
 					        		/** ###FALTA### ifOutNUcastPkts */
 					        		"statistics/tx_dropped", /* ifOutDiscards */
 					        		"statistics/tx_errors", /* ifOutErrors */
 					        		"tx_queue_len", /* ifOutErrors */
 						        	};
 					        
 					        for (String entry : arrEntry) {
 								System.out.println("TABLE ENTRY => "+entry);
 						        for (String eth : arrIf) {
 						        	cmd="cat /sys/class/net/"+eth+"/"+entry+"\n";
 						        	//System.out.println(cmd);
 						        	p = Runtime.getRuntime().exec(cmd);
 									in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 							        System.out.println("if: \""+eth+"\" => "+in2.readLine()); 
 						            cmd="";
 						            
 							    }
 					        }
 					        
 					        p = Runtime.getRuntime().exec("cat /proc/sys/net/ipv4/ip_default_ttl"+"\n");
 							in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 					        System.out.println("DEFAUL_TTL_IPv4 => "+in2.readLine()); 
 				            
 					        
 					        p.destroy();
 					        
 					    } catch (Exception e) {
 							// TODO: handle exception
 						}
                         
                         SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPInteger(nIf));
                         responseList.addSNMPObject(newPair);
                         
                     }
                     catch (SNMPBadValueException e)
                     {
                         // won't happen...
                     }
                 } 
                     
             }
             
             
             // ifEntry Integer | Read-Only | Mandatory | 1.3.6.1.2.1.2.2.1.1.X -> es el numero de interfaz
             if (snmpOID.toString().startsWith("1.3.6.1.2.1.2.2.1.1."))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                 	int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
 
                 	int eth=0; // interfaz solicitada
                 	int nIf =0; // numero de interfaces
                  	String cmd="ls /sys/class/net/"; // comando para listar las interfaces en el dispositivo.
 					Process p=null;
 					// Busco cuantas interfaces existen
 					try {
 						p = Runtime.getRuntime().exec(cmd);
 						BufferedReader in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 						
 					    while (in2.readLine() != null) {  
 					    	nIf += 1;  					        	
 					    }
 					} catch (Exception e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
 					
 					// Pregunto cual interfaz que esta solicitando
 					try {
 						eth =  Integer.parseInt(snmpOID.toString().substring(20, snmpOID.toString().length()));
 					} catch (NumberFormatException e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
                 		
                 	// si el numero de interfaz solicitada esta busco el valor y lo envio
 					if( 1 <= eth && eth <= nIf ){
 	                    try
 	                    {
 	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPInteger(eth));
 	                        responseList.addSNMPObject(newPair);
 	                    }
 	                    catch (SNMPBadValueException e)
 	                    {
 	                        // won't happen...
 	                    }
 					}else{
 						int errorIndex = i+1;
 	                    int errorStatus = SNMPRequestException.VALUE_NOT_AVAILABLE;
 	                    throw new SNMPSetException("Valor fuera de rango del nmero interfaces", errorIndex, errorStatus);						
 					}
                 	
                 	
                 } 
                 
             } // ifEntry
             
          // ifDescr DisplayString | Read-Only | Mandatory | 1.3.6.1.2.1.2.2.1.2.X -> X es el numero de interfaz
             if (snmpOID.toString().startsWith("1.3.6.1.2.1.2.2.1.2."))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                 	int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
 
                 	int eth=0; // interfaz solicitada
                 	int nIf =0; // numero de interfaces
                  	String cmd="ls /sys/class/net/"; // comando para listar las interfaces en el dispositivo.
 					Process p=null;
 					ArrayList<String> arrIf = new ArrayList<String>();
 					// Busco cuantas interfaces existen
 					try {
 						p = Runtime.getRuntime().exec(cmd);
 						BufferedReader in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 						String line="";
 						
 					    while ((line = in2.readLine()) != null) {  
 					    	nIf += 1;
 					    	arrIf.add(line);
 					    }
 					} catch (Exception e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
 					
 					// Pregunto cual interfaz que esta solicitando
 					try {
 						eth =  Integer.parseInt(snmpOID.toString().substring(20, snmpOID.toString().length()));
 					} catch (NumberFormatException e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
                 		
                 	// si el numero de interfaz solicitada esta busco el valor y lo envio
 					if( 1 <= eth && eth <= nIf ){
 	                    try
 	                    {
 	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPOctetString(arrIf.get(eth-1).toString()));
 	                        responseList.addSNMPObject(newPair);
 	                    }
 	                    catch (SNMPBadValueException e)
 	                    {
 	                        // won't happen...
 	                    }
 					}else{
 						int errorIndex = i+1;
 	                    int errorStatus = SNMPRequestException.VALUE_NOT_AVAILABLE;
 	                    throw new SNMPSetException("Valor fuera de rango del nmero interfaces", errorIndex, errorStatus);						
 					}
                 	                	
                 } 
                 
             } // ifDescr
             
             
             // ifType Integer | Read-Only | Mandatory | 1.3.6.1.2.1.2.2.1.3.X -> X es el numero de interfaz
             if (snmpOID.toString().startsWith("1.3.6.1.2.1.2.2.1.3."))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                 	int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
 
                 	int eth=0; // interfaz solicitada
                 	int nIf =0; // numero de interfaces
                  	String cmd="ls /sys/class/net/"; // comando para listar las interfaces en el dispositivo.
 					Process p=null;
 					ArrayList<String> arrIf = new ArrayList<String>();
 					// Busco cuantas interfaces existen
 					try {
 						p = Runtime.getRuntime().exec(cmd);
 						BufferedReader in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 						String line="";
 						
 					    while ((line = in2.readLine()) != null) {  
 					    	nIf += 1;
 					    	arrIf.add(line);
 					    }
 					} catch (Exception e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
 					
 					// Pregunto cual interfaz que esta solicitando
 					try {
 						eth =  Integer.parseInt(snmpOID.toString().substring(20, snmpOID.toString().length()));
 					} catch (NumberFormatException e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
                 		
                 	// si el numero de interfaz solicitada esta, busco el valor y lo envio
 					if( 1 <= eth && eth <= nIf ){
 	                    try
 	                    {   
 	                    	SNMPVariablePair newPair;
 	                    	if(arrIf.get(eth-1).toString().equalsIgnoreCase("lo")||
 	                    	   arrIf.get(eth-1).toString().equalsIgnoreCase("lo0")|| // soporte a varias Loopback
 	                    	   arrIf.get(eth-1).toString().equalsIgnoreCase("lo1")){
 	                        	newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPInteger(24));		                        
 	                    	}else{
 	                    		newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPInteger(1));		                        		                    		
 	                    	}
 	                    	responseList.addSNMPObject(newPair);
 	                    }
 	                    catch (SNMPBadValueException e)
 	                    {
 	                        // won't happen...
 	                    }catch (Exception e)
 	                    {
 	                    	// TODO: handle exception
 							e.printStackTrace();
 	                    }
 					}else{
 						int errorIndex = i+1;
 	                    int errorStatus = SNMPRequestException.VALUE_NOT_AVAILABLE;
 	                    throw new SNMPSetException("Valor fuera de rango del nmero interfaces", errorIndex, errorStatus);						
 					}
                 	                	
                 } 
                 
             } // ifType
             
             // ifMtu Integer | Read-Only | Mandatory | 1.3.6.1.2.1.2.2.1.4.X -> X es el numero de interfaz
             if (snmpOID.toString().startsWith("1.3.6.1.2.1.2.2.1.4."))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                 	int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
 
                 	int eth=0; // interfaz solicitada
                 	int nIf=0; // numero de interfaces
                 	int mtu=0; // Max Trafer Unit 
                  	String cmd="ls /sys/class/net/", line=""; // comando para listar las interfaces en el dispositivo.
 					Process p=null;
 					BufferedReader in2=null;
 					ArrayList<String> arrIf = new ArrayList<String>();
 					// Busco cuantas interfaces existen
 					try {
 						p = Runtime.getRuntime().exec(cmd);
 						in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 						line="";
 						
 					    while ((line = in2.readLine()) != null) {  
 					    	nIf += 1;
 					    	arrIf.add(line);					    	
 					    }
 					} catch (Exception e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
 					
 					// Pregunto cual interfaz que esta solicitando
 					try {
 						eth =  Integer.parseInt(snmpOID.toString().substring(20, snmpOID.toString().length()));
 					}catch (NumberFormatException e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
                 		
                 	// si el numero de interfaz solicitada esta, busco el valor y lo envio
 					if( 1 <= eth && eth <= nIf ){
 	                    try
 	                    {
 	                    	cmd="cat /sys/class/net/"+arrIf.get(eth-1)+"/mtu\n";
 	                    	p = Runtime.getRuntime().exec(cmd);
 							in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 							mtu = Integer.parseInt(in2.readLine());
 	                    	
 	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPInteger(mtu));
 	                        responseList.addSNMPObject(newPair);
 	                    }
 	                    catch (SNMPBadValueException e)
 	                    {
 	                        // won't happen...
 	                    }catch (Exception e)
 	                    {
 	                    	// TODO: handle exception
 							e.printStackTrace();
 	                    }
 					}else{
 						int errorIndex = i+1;
 	                    int errorStatus = SNMPRequestException.VALUE_NOT_AVAILABLE;
 	                    throw new SNMPSetException("Valor fuera de rango del nmero interfaces", errorIndex, errorStatus);						
 					}
                 	                	
                 } 
                 
             } // ifMtu
             
             
          // ifSpeed Gauge | Read-Only | Mandatory | 1.3.6.1.2.1.2.2.1.5.X -> X es el numero de interfaz
             if (snmpOID.toString().startsWith("1.3.6.1.2.1.2.2.1.5."))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                 	int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
 
                 	int eth=0; // interfaz solicitada
                 	int nIf=0; // numero de interfaces
                 	int speed=0; // bandwidth bits per second
                  	String cmd="ls /sys/class/net/", line=""; // comando para listar las interfaces en el dispositivo.
 					Process p=null;
 					BufferedReader in2=null;
 					ArrayList<String> arrIf = new ArrayList<String>();
 					// Busco cuantas interfaces existen
 					try {
 						p = Runtime.getRuntime().exec(cmd);
 						in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 						line="";
 						
 					    while ((line = in2.readLine()) != null) {  
 					    	nIf += 1;
 					    	arrIf.add(line);					    	
 					    }
 					} catch (Exception e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
 					
 					// Pregunto cual interfaz que esta solicitando
 					try {
 						eth =  Integer.parseInt(snmpOID.toString().substring(20, snmpOID.toString().length()));
 					}catch (NumberFormatException e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
                 		
                 	// si el numero de interfaz solicitada esta, busco el valor y lo envio
 					if( 1 <= eth && eth <= nIf ){
 	                    try
 	                    {
 	                    	cmd="cat /sys/class/net/"+arrIf.get(eth-1)+"/speed\n";
 	                    	p = Runtime.getRuntime().exec(cmd);
 							in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 							line = in2.readLine();
 							if(line == null) speed = 0;
 							else speed = Integer.parseInt(line);
 	                    	
 	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPGauge32(speed));
 	                        responseList.addSNMPObject(newPair);
 	                    }
 	                    catch (SNMPBadValueException e)
 	                    {
 	                        // won't happen...
 	                    }catch (Exception e)
 	                    {
 	                    	// TODO: handle exception	                    	
 							e.printStackTrace();
 	                    }
 					}else{
 						int errorIndex = i+1;
 	                    int errorStatus = SNMPRequestException.VALUE_NOT_AVAILABLE;
 	                    throw new SNMPSetException("Valor fuera de rango del nmero interfaces", errorIndex, errorStatus);						
 					}
                 	                	
                 } 
                 
             } // ifSpeed
             
             
          // ifPhysAddress PhysAddress | Read-Only | Mandatory | 1.3.6.1.2.1.2.2.1.6.X -> X es el numero de interfaz
             if (snmpOID.toString().startsWith("1.3.6.1.2.1.2.2.1.6."))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                 	int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
 
                 	int eth=0; // interfaz solicitada
                 	int nIf=0; // numero de interfaces
                 	String address=""; // PhysAddress
                  	String cmd="ls /sys/class/net/", line=""; // comando para listar las interfaces en el dispositivo.
 					Process p=null;
 					BufferedReader in2=null;
 					ArrayList<String> arrIf = new ArrayList<String>();
 					// Busco cuantas interfaces existen
 					try {
 						p = Runtime.getRuntime().exec(cmd);
 						in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 						line="";
 						
 					    while ((line = in2.readLine()) != null) {  
 					    	nIf += 1;
 					    	arrIf.add(line);					    	
 					    }
 					} catch (Exception e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
 					
 					// Pregunto cual interfaz que esta solicitando
 					try {
 						eth =  Integer.parseInt(snmpOID.toString().substring(20, snmpOID.toString().length()));
 					}catch (NumberFormatException e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
                 		
                 	// si el numero de interfaz solicitada esta, busco el valor y lo envio
 					if( 1 <= eth && eth <= nIf ){
 	                    try
 	                    {
 	                    	cmd="cat /sys/class/net/"+arrIf.get(eth-1)+"/address\n";
 	                    	p = Runtime.getRuntime().exec(cmd);
 							in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 							address = in2.readLine();
 							
 	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPOctetString(address));
 	                        responseList.addSNMPObject(newPair);
 	                    }
 	                    catch (SNMPBadValueException e)
 	                    {
 	                        // won't happen...
 	                    }catch (Exception e)
 	                    {
 	                    	// TODO: handle exception	                    	
 							e.printStackTrace();
 	                    }
 					}else{
 						int errorIndex = i+1;
 	                    int errorStatus = SNMPRequestException.VALUE_NOT_AVAILABLE;
 	                    throw new SNMPSetException("Valor fuera de rango del nmero interfaces", errorIndex, errorStatus);						
 					}
                 	                	
                 } 
                 
             } // ifPhysAddress
             
             /**
              * 
              *	FALTA ifAdminStatus -> acceso a BD porque es Read-Write
              * 
              */
             
             // ifOperStatus Integer | Read-Only | Mandatory | 1.3.6.1.2.1.2.2.1.8.X -> X es el numero de interfaz
             if (snmpOID.toString().startsWith("1.3.6.1.2.1.2.2.1.8."))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                 	int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
 
                 	int eth=0; // interfaz solicitada
                 	int nIf=0; // numero de interfaces
                 	String status="";
                 	int oper=0; // operational state
                  	String cmd="ls /sys/class/net/", line=""; // comando para listar las interfaces en el dispositivo.
 					Process p=null;
 					BufferedReader in2=null;
 					ArrayList<String> arrIf = new ArrayList<String>();
 					// Busco cuantas interfaces existen
 					try {
 						p = Runtime.getRuntime().exec(cmd);
 						in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 						line="";
 						
 					    while ((line = in2.readLine()) != null) {  
 					    	nIf += 1;
 					    	arrIf.add(line);					    	
 					    }
 					} catch (Exception e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
 					
 					// Pregunto cual interfaz que esta solicitando
 					try {
 						eth =  Integer.parseInt(snmpOID.toString().substring(20, snmpOID.toString().length()));
 					}catch (NumberFormatException e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
                 		
                 	// si el numero de interfaz solicitada esta, busco el valor y lo envio
 					if( 1 <= eth && eth <= nIf ){
 	                    try
 	                    {
 	                    	cmd="cat /sys/class/net/"+arrIf.get(eth-1)+"/operstate\n";
 	                    	p = Runtime.getRuntime().exec(cmd);
 							in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 							status = in2.readLine();
 							// el comando devuelve String lo busco segun el RFC2863
 							if(status.equalsIgnoreCase("up")){
 								oper = 1;
 							}else if(status.equalsIgnoreCase("down")){
 								oper = 2;
 							}else if(status.equalsIgnoreCase("testing")){								
 								oper = 3;
 							}else if(status.equalsIgnoreCase("unknown")){
 								oper = 4;
 							}else if(status.equalsIgnoreCase("dormant")){
 								oper = 5;
 							}else if(status.equalsIgnoreCase("notPresent")){
 								oper = 6;
 							}else{ // lowerLayerDown
 								oper = 7;
 							}
 							
 	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPInteger(oper));
 	                        responseList.addSNMPObject(newPair);
 	                    }
 	                    catch (SNMPBadValueException e)
 	                    {
 	                        // won't happen...
 	                    }catch (Exception e)
 	                    {
 	                    	// TODO: handle exception	                    	
 							e.printStackTrace();
 	                    }
 					}else{
 						int errorIndex = i+1;
 	                    int errorStatus = SNMPRequestException.VALUE_NOT_AVAILABLE;
 	                    throw new SNMPSetException("Valor fuera de rango del nmero interfaces", errorIndex, errorStatus);						
 					}
                 	                	
                 } 
                 
             } // ifOperStatus
             
             // ifLastChange TimeTicks | Read-Only | Mandatory | 1.3.6.1.2.1.2.2.1.9.X -> X es el numero de interfaz
             if (snmpOID.toString().startsWith("1.3.6.1.2.1.2.2.1.9."))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                 	int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
 
                 	int eth=0; // interfaz solicitada
                 	int nIf=0; // numero de interfaces
                 	int time = 0;
                  	String cmd="ls /sys/class/net/", line=""; // comando para listar las interfaces en el dispositivo.
 					Process p=null;
 					BufferedReader in2=null;
 					ArrayList<String> arrIf = new ArrayList<String>();
 					// Busco cuantas interfaces existen
 					try {
 						p = Runtime.getRuntime().exec(cmd);
 						in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 						line="";
 						
 					    while ((line = in2.readLine()) != null) {  
 					    	nIf += 1;
 					    	arrIf.add(line);					    	
 					    }
 					} catch (Exception e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
 					
 					// Pregunto cual interfaz que esta solicitando
 					try {
 						eth =  Integer.parseInt(snmpOID.toString().substring(20, snmpOID.toString().length()));
 					}catch (NumberFormatException e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
                 		
                 	// si el numero de interfaz solicitada esta, busco el valor y lo envio
 					if( 1 <= eth && eth <= nIf ){
 	                    try
 	                    {
 	                    	cmd="cat /sys/class/net/"+arrIf.get(eth-1)+"/power/runtime_active_time\n";
 	                    	p = Runtime.getRuntime().exec(cmd);
 							in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 							time = Integer.parseInt(in2.readLine());
 							
 	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPTimeTicks(time));
 	                        responseList.addSNMPObject(newPair);
 	                    }
 	                    catch (SNMPBadValueException e)
 	                    {
 	                        // won't happen...
 	                    }catch (Exception e)
 	                    {
 	                    	// TODO: handle exception	                    	
 							e.printStackTrace();
 	                    }
 					}else{
 						int errorIndex = i+1;
 	                    int errorStatus = SNMPRequestException.VALUE_NOT_AVAILABLE;
 	                    throw new SNMPSetException("Valor fuera de rango del nmero interfaces", errorIndex, errorStatus);						
 					}
                 	                	
                 } 
                 
             } // ifLastChange
             
 
             // ifInOctets Counter32 | Read-Only | Mandatory | 1.3.6.1.2.1.2.2.1.10.X -> X es el numero de interfaz
             if (snmpOID.toString().startsWith("1.3.6.1.2.1.2.2.1.10."))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                 	int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
 
                 	int eth=0; // interfaz solicitada
                 	int nIf=0; // numero de interfaces
                 	long octets = 0; //octets
                  	String cmd="ls /sys/class/net/", line=""; // comando para listar las interfaces en el dispositivo.
 					Process p=null;
 					BufferedReader in2=null;
 					ArrayList<String> arrIf = new ArrayList<String>();
 					// Busco cuantas interfaces existen
 					try {
 						p = Runtime.getRuntime().exec(cmd);
 						in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 						line="";
 						
 					    while ((line = in2.readLine()) != null) {  
 					    	nIf += 1;
 					    	arrIf.add(line);					    	
 					    }
 					} catch (Exception e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
 					
 					// Pregunto cual interfaz que esta solicitando
 					try {
 						eth =  Integer.parseInt(snmpOID.toString().substring(21, snmpOID.toString().length()));
 					}catch (NumberFormatException e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
                 		
                 	// si el numero de interfaz solicitada esta, busco el valor y lo envio
 					if( 1 <= eth && eth <= nIf ){
 	                    try
 	                    {
 	                    	cmd="cat /sys/class/net/"+arrIf.get(eth-1)+"/statistics/rx_bytes\n";
 	                    	p = Runtime.getRuntime().exec(cmd);
 							in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 							octets = Long.parseLong(in2.readLine());
 							
 	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(octets));
 	                        responseList.addSNMPObject(newPair);
 	                    }
 	                    catch (SNMPBadValueException e)
 	                    {
 	                        // won't happen...
 	                    }catch (Exception e)
 	                    {
 	                    	// TODO: handle exception	                    	
 							e.printStackTrace();
 	                    }
 					}else{
 						int errorIndex = i+1;
 	                    int errorStatus = SNMPRequestException.VALUE_NOT_AVAILABLE;
 	                    throw new SNMPSetException("Valor fuera de rango del nmero interfaces", errorIndex, errorStatus);						
 					}
                 	                	
                 } 
                 
             } // ifInOctets
             
             
             // ifInUcastPkts Counter32 | Read-Only | Mandatory | 1.3.6.1.2.1.2.2.1.11.X -> X es el numero de interfaz
             if (snmpOID.toString().startsWith("1.3.6.1.2.1.2.2.1.11."))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                 	int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
 
                 	int eth=0; // interfaz solicitada
                 	int nIf=0; // numero de interfaces
                 	long packets = 0; //packets
                  	String cmd="ls /sys/class/net/", line=""; // comando para listar las interfaces en el dispositivo.
 					Process p=null;
 					BufferedReader in2=null;
 					ArrayList<String> arrIf = new ArrayList<String>();
 					// Busco cuantas interfaces existen
 					try {
 						p = Runtime.getRuntime().exec(cmd);
 						in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 						line="";
 						
 					    while ((line = in2.readLine()) != null) {  
 					    	nIf += 1;
 					    	arrIf.add(line);					    	
 					    }
 					} catch (Exception e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
 					
 					// Pregunto cual interfaz que esta solicitando
 					try {
 						eth =  Integer.parseInt(snmpOID.toString().substring(21, snmpOID.toString().length()));
 					}catch (NumberFormatException e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
                 		
                 	// si el numero de interfaz solicitada esta, busco el valor y lo envio
 					if( 1 <= eth && eth <= nIf ){
 	                    try
 	                    {
 	                    	cmd="cat /sys/class/net/"+arrIf.get(eth-1)+"/statistics/rx_packets\n";
 	                    	p = Runtime.getRuntime().exec(cmd);
 							in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 							packets = Long.parseLong(in2.readLine());
 							
 	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(packets));
 	                        responseList.addSNMPObject(newPair);
 	                    }
 	                    catch (SNMPBadValueException e)
 	                    {
 	                        // won't happen...
 	                    }catch (Exception e)
 	                    {
 	                    	// TODO: handle exception	                    	
 							e.printStackTrace();
 	                    }
 					}else{
 						int errorIndex = i+1;
 	                    int errorStatus = SNMPRequestException.VALUE_NOT_AVAILABLE;
 	                    throw new SNMPSetException("Valor fuera de rango del nmero interfaces", errorIndex, errorStatus);						
 					}
                 	                	
                 } 
                 
             } // ifInUcastPkts
             
             // ifInNUcastPkts Counter32 | Read-Only | Mandatory | 1.3.6.1.2.1.2.2.1.12.X -> X es el numero de interfaz
             if (snmpOID.toString().startsWith("1.3.6.1.2.1.2.2.1.12."))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                 	int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
 
                 	int eth=0; // interfaz solicitada
                 	int nIf=0; // numero de interfaces
                 	long packets = 0; //packets
                  	String cmd="ls /sys/class/net/", line=""; // comando para listar las interfaces en el dispositivo.
 					Process p=null;
 					BufferedReader in2=null;
 					ArrayList<String> arrIf = new ArrayList<String>();
 					// Busco cuantas interfaces existen
 					try {
 						p = Runtime.getRuntime().exec(cmd);
 						in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 						line="";
 						
 					    while ((line = in2.readLine()) != null) {  
 					    	nIf += 1;
 					    	arrIf.add(line);					    	
 					    }
 					} catch (Exception e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
 					
 					// Pregunto cual interfaz que esta solicitando
 					try {
 						eth =  Integer.parseInt(snmpOID.toString().substring(21, snmpOID.toString().length()));
 					}catch (NumberFormatException e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
                 		
                 	// si el numero de interfaz solicitada esta, busco el valor y lo envio
 					if( 1 <= eth && eth <= nIf ){
 	                    try
 	                    {
 	                    	cmd="cat /sys/class/net/"+arrIf.get(eth-1)+"/statistics/multicast\n";
 	                    	p = Runtime.getRuntime().exec(cmd);
 							in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 							packets = Long.parseLong(in2.readLine());
 							
 	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(packets));
 	                        responseList.addSNMPObject(newPair);
 	                    }
 	                    catch (SNMPBadValueException e)
 	                    {
 	                        // won't happen...
 	                    }catch (Exception e)
 	                    {
 	                    	// TODO: handle exception	                    	
 							e.printStackTrace();
 	                    }
 					}else{
 						int errorIndex = i+1;
 	                    int errorStatus = SNMPRequestException.VALUE_NOT_AVAILABLE;
 	                    throw new SNMPSetException("Valor fuera de rango del nmero interfaces", errorIndex, errorStatus);						
 					}
                 	                	
                 } 
                 
             } // ifInNUcastPkts
             
             
             // ifInDiscards Counter32 | Read-Only | Mandatory | 1.3.6.1.2.1.2.2.1.13.X -> X es el numero de interfaz
             if (snmpOID.toString().startsWith("1.3.6.1.2.1.2.2.1.13."))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                 	int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
 
                 	int eth=0; // interfaz solicitada
                 	int nIf=0; // numero de interfaces
                 	long packets = 0; //packets
                  	String cmd="ls /sys/class/net/", line=""; // comando para listar las interfaces en el dispositivo.
 					Process p=null;
 					BufferedReader in2=null;
 					ArrayList<String> arrIf = new ArrayList<String>();
 					// Busco cuantas interfaces existen
 					try {
 						p = Runtime.getRuntime().exec(cmd);
 						in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 						line="";
 						
 					    while ((line = in2.readLine()) != null) {  
 					    	nIf += 1;
 					    	arrIf.add(line);					    	
 					    }
 					} catch (Exception e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
 					
 					// Pregunto cual interfaz que esta solicitando
 					try {
 						eth =  Integer.parseInt(snmpOID.toString().substring(21, snmpOID.toString().length()));
 					}catch (NumberFormatException e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
                 		
                 	// si el numero de interfaz solicitada esta, busco el valor y lo envio
 					if( 1 <= eth && eth <= nIf ){
 	                    try
 	                    {
 	                    	cmd="cat /sys/class/net/"+arrIf.get(eth-1)+"/statistics/rx_dropped\n";
 	                    	p = Runtime.getRuntime().exec(cmd);
 							in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 							packets = Long.parseLong(in2.readLine());
 							
 	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(packets));
 	                        responseList.addSNMPObject(newPair);
 	                    }
 	                    catch (SNMPBadValueException e)
 	                    {
 	                        // won't happen...
 	                    }catch (Exception e)
 	                    {
 	                    	// TODO: handle exception	                    	
 							e.printStackTrace();
 	                    }
 					}else{
 						int errorIndex = i+1;
 	                    int errorStatus = SNMPRequestException.VALUE_NOT_AVAILABLE;
 	                    throw new SNMPSetException("Valor fuera de rango del nmero interfaces", errorIndex, errorStatus);						
 					}
                 	                	
                 } 
                 
             } // ifInDiscards
             
             // ifInErrors Counter32 | Read-Only | Mandatory | 1.3.6.1.2.1.2.2.1.14.X -> X es el numero de interfaz
             if (snmpOID.toString().startsWith("1.3.6.1.2.1.2.2.1.14."))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                 	int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
 
                 	int eth=0; // interfaz solicitada
                 	int nIf=0; // numero de interfaces
                 	long packets = 0; //packets
                  	String cmd="ls /sys/class/net/", line=""; // comando para listar las interfaces en el dispositivo.
 					Process p=null;
 					BufferedReader in2=null;
 					ArrayList<String> arrIf = new ArrayList<String>();
 					// Busco cuantas interfaces existen
 					try {
 						p = Runtime.getRuntime().exec(cmd);
 						in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 						line="";
 						
 					    while ((line = in2.readLine()) != null) {  
 					    	nIf += 1;
 					    	arrIf.add(line);					    	
 					    }
 					} catch (Exception e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
 					
 					// Pregunto cual interfaz que esta solicitando
 					try {
 						eth =  Integer.parseInt(snmpOID.toString().substring(21, snmpOID.toString().length()));
 					}catch (NumberFormatException e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
                 		
                 	// si el numero de interfaz solicitada esta, busco el valor y lo envio
 					if( 1 <= eth && eth <= nIf ){
 	                    try
 	                    {
 	                    	cmd="cat /sys/class/net/"+arrIf.get(eth-1)+"/statistics/rx_errors\n";
 	                    	p = Runtime.getRuntime().exec(cmd);
 							in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 							packets = Long.parseLong(in2.readLine());
 							
 	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(packets));
 	                        responseList.addSNMPObject(newPair);
 	                    }
 	                    catch (SNMPBadValueException e)
 	                    {
 	                        // won't happen...
 	                    }catch (Exception e)
 	                    {
 	                    	// TODO: handle exception	                    	
 							e.printStackTrace();
 	                    }
 					}else{
 						int errorIndex = i+1;
 	                    int errorStatus = SNMPRequestException.VALUE_NOT_AVAILABLE;
 	                    throw new SNMPSetException("Valor fuera de rango del nmero interfaces", errorIndex, errorStatus);						
 					}
                 	                	
                 } 
                 
             } // ifInErrors
             
             
             // ifInUnknownProtos Counter32 | Read-Only | Mandatory | 1.3.6.1.2.1.2.2.1.15.X -> X es el numero de interfaz
             if (snmpOID.toString().startsWith("1.3.6.1.2.1.2.2.1.15."))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                 	int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
 
                 	int eth=0; // interfaz solicitada
                 	int nIf=0; // numero de interfaces
                 	long packets = 0; //packets
                  	String cmd="ls /sys/class/net/", line=""; // comando para listar las interfaces en el dispositivo.
 					Process p=null;
 					BufferedReader in2=null;
 					ArrayList<String> arrIf = new ArrayList<String>();
 					// Busco cuantas interfaces existen
 					try {
 						p = Runtime.getRuntime().exec(cmd);
 						in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 						line="";
 						
 					    while ((line = in2.readLine()) != null) {  
 					    	nIf += 1;
 					    	arrIf.add(line);					    	
 					    }
 					} catch (Exception e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
 					
 					// Pregunto cual interfaz que esta solicitando
 					try {
 						eth =  Integer.parseInt(snmpOID.toString().substring(21, snmpOID.toString().length()));
 					}catch (NumberFormatException e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
                 		
                 	// si el numero de interfaz solicitada esta, busco el valor y lo envio
 					if( 1 <= eth && eth <= nIf ){
 	                    try
 	                    {
 	                    	cmd="cat /sys/class/net/"+arrIf.get(eth-1)+"/statistics/rx_missed\n";
 	                    	p = Runtime.getRuntime().exec(cmd);
 							in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 							packets = Long.parseLong(in2.readLine());
 							
 	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(packets));
 	                        responseList.addSNMPObject(newPair);
 	                    }
 	                    catch (SNMPBadValueException e)
 	                    {
 	                        // won't happen...
 	                    }catch (Exception e)
 	                    {
 	                    	// TODO: handle exception	                    	
 							e.printStackTrace();
 	                    }
 					}else{
 						int errorIndex = i+1;
 	                    int errorStatus = SNMPRequestException.VALUE_NOT_AVAILABLE;
 	                    throw new SNMPSetException("Valor fuera de rango del nmero interfaces", errorIndex, errorStatus);						
 					}
                 	                	
                 } 
                 
             } // ifInUnknownProtos
             
             
             // ifOutOctets Counter32 | Read-Only | Mandatory | 1.3.6.1.2.1.2.2.1.16.X -> X es el numero de interfaz
             if (snmpOID.toString().startsWith("1.3.6.1.2.1.2.2.1.16."))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                 	int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
 
                 	int eth=0; // interfaz solicitada
                 	int nIf=0; // numero de interfaces
                 	long octets = 0; //octets
                  	String cmd="ls /sys/class/net/", line=""; // comando para listar las interfaces en el dispositivo.
 					Process p=null;
 					BufferedReader in2=null;
 					ArrayList<String> arrIf = new ArrayList<String>();
 					// Busco cuantas interfaces existen
 					try {
 						p = Runtime.getRuntime().exec(cmd);
 						in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 						line="";
 						
 					    while ((line = in2.readLine()) != null) {  
 					    	nIf += 1;
 					    	arrIf.add(line);					    	
 					    }
 					} catch (Exception e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
 					
 					// Pregunto cual interfaz que esta solicitando
 					try {
 						eth =  Integer.parseInt(snmpOID.toString().substring(21, snmpOID.toString().length()));
 					}catch (NumberFormatException e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
                 		
                 	// si el numero de interfaz solicitada esta, busco el valor y lo envio
 					if( 1 <= eth && eth <= nIf ){
 	                    try
 	                    {
 	                    	cmd="cat /sys/class/net/"+arrIf.get(eth-1)+"/statistics/tx_bytes\n";
 	                    	p = Runtime.getRuntime().exec(cmd);
 							in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 							octets = Long.parseLong(in2.readLine());
 							
 	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(octets));
 	                        responseList.addSNMPObject(newPair);
 	                    }
 	                    catch (SNMPBadValueException e)
 	                    {
 	                        // won't happen...
 	                    }catch (Exception e)
 	                    {
 	                    	// TODO: handle exception	                    	
 							e.printStackTrace();
 	                    }
 					}else{
 						int errorIndex = i+1;
 	                    int errorStatus = SNMPRequestException.VALUE_NOT_AVAILABLE;
 	                    throw new SNMPSetException("Valor fuera de rango del nmero interfaces", errorIndex, errorStatus);						
 					}
                 	                	
                 } 
                 
             } // ifOutOctets
             
             
          // ifOutUcastPkts Counter32 | Read-Only | Mandatory | 1.3.6.1.2.1.2.2.1.17.X -> X es el numero de interfaz
             if (snmpOID.toString().startsWith("1.3.6.1.2.1.2.2.1.17."))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                 	int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
 
                 	int eth=0; // interfaz solicitada
                 	int nIf=0; // numero de interfaces
                 	long packets = 0; //packets
                  	String cmd="ls /sys/class/net/", line=""; // comando para listar las interfaces en el dispositivo.
 					Process p=null;
 					BufferedReader in2=null;
 					ArrayList<String> arrIf = new ArrayList<String>();
 					// Busco cuantas interfaces existen
 					try {
 						p = Runtime.getRuntime().exec(cmd);
 						in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 						line="";
 						
 					    while ((line = in2.readLine()) != null) {  
 					    	nIf += 1;
 					    	arrIf.add(line);					    	
 					    }
 					} catch (Exception e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
 					
 					// Pregunto cual interfaz que esta solicitando
 					try {
 						eth =  Integer.parseInt(snmpOID.toString().substring(21, snmpOID.toString().length()));
 					}catch (NumberFormatException e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
                 		
                 	// si el numero de interfaz solicitada esta, busco el valor y lo envio
 					if( 1 <= eth && eth <= nIf ){
 	                    try
 	                    {
 	                    	cmd="cat /sys/class/net/"+arrIf.get(eth-1)+"/statistics/tx_packets\n";
 	                    	p = Runtime.getRuntime().exec(cmd);
 							in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 							packets = Long.parseLong(in2.readLine());
 							
 	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(packets));
 	                        responseList.addSNMPObject(newPair);
 	                    }
 	                    catch (SNMPBadValueException e)
 	                    {
 	                        // won't happen...
 	                    }catch (Exception e)
 	                    {
 	                    	// TODO: handle exception	                    	
 							e.printStackTrace();
 	                    }
 					}else{
 						int errorIndex = i+1;
 	                    int errorStatus = SNMPRequestException.VALUE_NOT_AVAILABLE;
 	                    throw new SNMPSetException("Valor fuera de rango del nmero interfaces", errorIndex, errorStatus);						
 					}
                 	                	
                 } 
                 
             } // ifOutUcastPkts
             
             // ifOutNUcastPkts Counter32 | Read-Only | Mandatory | 1.3.6.1.2.1.2.2.1.18.X -> X es el numero de interfaz
             if (snmpOID.toString().startsWith("1.3.6.1.2.1.2.2.1.18."))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                 	int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
 
                 	int eth=0; // interfaz solicitada
                 	int nIf=0; // numero de interfaces
                 	long packets = 0; //packets
                  	String cmd="ls /sys/class/net/", line=""; // comando para listar las interfaces en el dispositivo.
 					Process p=null;
 					BufferedReader in2=null;
 					ArrayList<String> arrIf = new ArrayList<String>();
 					// Busco cuantas interfaces existen
 					try {
 						p = Runtime.getRuntime().exec(cmd);
 						in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 						line="";
 						
 					    while ((line = in2.readLine()) != null) {  
 					    	nIf += 1;
 					    	arrIf.add(line);					    	
 					    }
 					} catch (Exception e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
 					
 					// Pregunto cual interfaz que esta solicitando
 					try {
 						eth =  Integer.parseInt(snmpOID.toString().substring(21, snmpOID.toString().length()));
 					}catch (NumberFormatException e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
                 		
                 	// si el numero de interfaz solicitada esta, busco el valor y lo envio
 					if( 1 <= eth && eth <= nIf ){
 	                    try
 	                    {
 	                    	cmd="cat /sys/class/net/"+arrIf.get(eth-1)+"/statistics/OUTMULTICAST\n";
 	                    	p = Runtime.getRuntime().exec(cmd);
 							in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 							packets = Long.parseLong(in2.readLine());
 							
 	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(packets));
 	                        responseList.addSNMPObject(newPair);
 	                    }
 	                    catch (SNMPBadValueException e)
 	                    {
 	                        // won't happen...
 	                    }catch (Exception e)
 	                    {
 	                    	// TODO: handle exception	                    	
 							e.printStackTrace();
 	                    }
 					}else{
 						int errorIndex = i+1;
 	                    int errorStatus = SNMPRequestException.VALUE_NOT_AVAILABLE;
 	                    throw new SNMPSetException("Valor fuera de rango del nmero interfaces", errorIndex, errorStatus);						
 					}
                 	                	
                 } 
                 
             } // ifOutNUcastPkts
             
             
          // ifOutDiscards Counter32 | Read-Only | Mandatory | 1.3.6.1.2.1.2.2.1.19.X -> X es el numero de interfaz
             if (snmpOID.toString().startsWith("1.3.6.1.2.1.2.2.1.19."))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                 	int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
 
                 	int eth=0; // interfaz solicitada
                 	int nIf=0; // numero de interfaces
                 	long packets = 0; //packets
                  	String cmd="ls /sys/class/net/", line=""; // comando para listar las interfaces en el dispositivo.
 					Process p=null;
 					BufferedReader in2=null;
 					ArrayList<String> arrIf = new ArrayList<String>();
 					// Busco cuantas interfaces existen
 					try {
 						p = Runtime.getRuntime().exec(cmd);
 						in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 						line="";
 						
 					    while ((line = in2.readLine()) != null) {  
 					    	nIf += 1;
 					    	arrIf.add(line);					    	
 					    }
 					} catch (Exception e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
 					
 					// Pregunto cual interfaz que esta solicitando
 					try {
 						eth =  Integer.parseInt(snmpOID.toString().substring(21, snmpOID.toString().length()));
 					}catch (NumberFormatException e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
                 		
                 	// si el numero de interfaz solicitada esta, busco el valor y lo envio
 					if( 1 <= eth && eth <= nIf ){
 	                    try
 	                    {
 	                    	cmd="cat /sys/class/net/"+arrIf.get(eth-1)+"/statistics/tx_dropped\n";
 	                    	p = Runtime.getRuntime().exec(cmd);
 							in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 							packets = Long.parseLong(in2.readLine());
 							
 	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(packets));
 	                        responseList.addSNMPObject(newPair);
 	                    }
 	                    catch (SNMPBadValueException e)
 	                    {
 	                        // won't happen...
 	                    }catch (Exception e)
 	                    {
 	                    	// TODO: handle exception	                    	
 							e.printStackTrace();
 	                    }
 					}else{
 						int errorIndex = i+1;
 	                    int errorStatus = SNMPRequestException.VALUE_NOT_AVAILABLE;
 	                    throw new SNMPSetException("Valor fuera de rango del nmero interfaces", errorIndex, errorStatus);						
 					}
                 	                	
                 } 
                 
             } // ifOutDiscards
             
             // ifOutErrors Counter32 | Read-Only | Mandatory | 1.3.6.1.2.1.2.2.1.20.X -> X es el numero de interfaz
             if (snmpOID.toString().startsWith("1.3.6.1.2.1.2.2.1.20."))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                 	int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
 
                 	int eth=0; // interfaz solicitada
                 	int nIf=0; // numero de interfaces
                 	long packets = 0; //packets
                  	String cmd="ls /sys/class/net/", line=""; // comando para listar las interfaces en el dispositivo.
 					Process p=null;
 					BufferedReader in2=null;
 					ArrayList<String> arrIf = new ArrayList<String>();
 					// Busco cuantas interfaces existen
 					try {
 						p = Runtime.getRuntime().exec(cmd);
 						in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 						line="";
 						
 					    while ((line = in2.readLine()) != null) {  
 					    	nIf += 1;
 					    	arrIf.add(line);					    	
 					    }
 					} catch (Exception e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
 					
 					// Pregunto cual interfaz que esta solicitando
 					try {
 						eth =  Integer.parseInt(snmpOID.toString().substring(21, snmpOID.toString().length()));
 					}catch (NumberFormatException e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
                 		
                 	// si el numero de interfaz solicitada esta, busco el valor y lo envio
 					if( 1 <= eth && eth <= nIf ){
 	                    try
 	                    {
 	                    	cmd="cat /sys/class/net/"+arrIf.get(eth-1)+"/statistics/tx_errors\n";
 	                    	p = Runtime.getRuntime().exec(cmd);
 							in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 							packets = Long.parseLong(in2.readLine());
 							
 	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(packets));
 	                        responseList.addSNMPObject(newPair);
 	                    }
 	                    catch (SNMPBadValueException e)
 	                    {
 	                        // won't happen...
 	                    }catch (Exception e)
 	                    {
 	                    	// TODO: handle exception	                    	
 							e.printStackTrace();
 	                    }
 					}else{
 						int errorIndex = i+1;
 	                    int errorStatus = SNMPRequestException.VALUE_NOT_AVAILABLE;
 	                    throw new SNMPSetException("Valor fuera de rango del nmero interfaces", errorIndex, errorStatus);						
 					}
                 	                	
                 } 
                 
             } // ifOutErrors
             
             
            // ifOutQLen Counter32 | Read-Only | Mandatory | 1.3.6.1.2.1.2.2.1.21.X -> X es el numero de interfaz
            if (snmpOID.toString().startsWith("1.3.6.1.2.1.2.2.1.21."))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                 	int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
 
                 	int eth=0; // interfaz solicitada
                 	int nIf=0; // numero de interfaces
                 	long qLen = 0; //Queue Length
                  	String cmd="ls /sys/class/net/", line=""; // comando para listar las interfaces en el dispositivo.
 					Process p=null;
 					BufferedReader in2=null;
 					ArrayList<String> arrIf = new ArrayList<String>();
 					// Busco cuantas interfaces existen
 					try {
 						p = Runtime.getRuntime().exec(cmd);
 						in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 						line="";
 						
 					    while ((line = in2.readLine()) != null) {  
 					    	nIf += 1;
 					    	arrIf.add(line);					    	
 					    }
 					} catch (Exception e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
 					
 					// Pregunto cual interfaz que esta solicitando
 					try {
 						eth =  Integer.parseInt(snmpOID.toString().substring(21, snmpOID.toString().length()));
 					}catch (NumberFormatException e) {
 						// TODO: handle exception
 						e.printStackTrace();
 					}
                 		
                 	// si el numero de interfaz solicitada esta, busco el valor y lo envio
 					if( 1 <= eth && eth <= nIf ){
 	                    try
 	                    {
 	                    	cmd="cat /sys/class/net/"+arrIf.get(eth-1)+"/tx_queue_len\n";
 	                    	p = Runtime.getRuntime().exec(cmd);
 							in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 							qLen = Long.parseLong(in2.readLine());
 							
 	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPGauge32(qLen));
 	                        responseList.addSNMPObject(newPair);
 	                    }
 	                    catch (SNMPBadValueException e)
 	                    {
 	                        // won't happen...
 	                    }catch (Exception e)
 	                    {
 	                    	// TODO: handle exception	                    	
 							e.printStackTrace();
 	                    }
 					}else{
 						int errorIndex = i+1;
 	                    int errorStatus = SNMPRequestException.VALUE_NOT_AVAILABLE;
 	                    throw new SNMPSetException("Valor fuera de rango del nmero interfaces", errorIndex, errorStatus);						
 					}
                 	                	
                 } 
                 
             } // ifOutQLen
             
             
             /******************************
             *
             *   MIB-II | the Address Translation Group
             * 
             ******************************/
             
             /******************************
             *
             *   MIB-II | the IP Group
             * 
             ******************************/
             
             // ipForwarding | Integer | Read-Write | Mandatory |
             if (snmpOID.toString().equals("1.3.6.1.2.1.4.1.0"))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "Forwarding";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(0).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(1).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				System.out.println(stra);
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long forw = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(forw));
                         responseList.addSNMPObject(newPair);
                      }
                      catch (SNMPBadValueException e)
                      {
                          // won't happen...
 	                 }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 } 
                 
             } // ipForwarding
             
          // ipDefaultTTL | Integer | Read-Write | Mandatory |
             if (snmpOID.toString().equals("1.3.6.1.2.1.4.2.0"))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "DefaultTTL";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(0).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(1).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				System.out.println(stra);
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long ttl = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(ttl));
                         responseList.addSNMPObject(newPair);
                      }
                      catch (SNMPBadValueException e)
                      {
                          // won't happen...
 	                 }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 } 
                 
             } // ipDefaultTTL
             
             
          // ipInReceives | Counter | Read-Only | Mandatory |
             if (snmpOID.toString().equals("1.3.6.1.2.1.4.3.0"))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "InReceives";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(0).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(1).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				System.out.println(stra);
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                      }
                      catch (SNMPBadValueException e)
                      {
                          // won't happen...
 	                 }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // ipInReceives 
             
             // ipInHdrErrors | Counter | Read-Only | Mandatory |
             if (snmpOID.toString().equals("1.3.6.1.2.1.4.4.0"))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "InHdrErrors";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(0).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(1).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				System.out.println(stra);
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                      }
                      catch (SNMPBadValueException e)
                      {
                          // won't happen...
 	                 }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // ipInHdrErrors 
             
             
             // ipInAddrErrors | Counter | Read-Only | Mandatory |
             if (snmpOID.toString().equals("1.3.6.1.2.1.4.5.0"))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "InAddrErrors";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(0).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(1).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				System.out.println(stra);
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                      }
                      catch (SNMPBadValueException e)
                      {
                          // won't happen...
 	                 }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // ipInAddrErrors 
             
             
          // ipForwDatagrams | Counter | Read-Only | Mandatory |
             if (snmpOID.toString().equals("1.3.6.1.2.1.4.6.0"))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "ForwDatagrams";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(0).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(1).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				System.out.println(stra);
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                      }
                      catch (SNMPBadValueException e)
                      {
                          // won't happen...
 	                 }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // ipForwDatagrams 
             
          // ipInUnknownProtos | Counter | Read-Only | Mandatory |
             if (snmpOID.toString().equals("1.3.6.1.2.1.4.7.0"))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "InUnknownProtos";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(0).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(1).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				System.out.println(stra);
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                      }
                      catch (SNMPBadValueException e)
                      {
                          // won't happen...
 	                 }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // ipInUnknownProtos 
       
             
          // ipInDiscards | Counter | Read-Only | Mandatory |
             if (snmpOID.toString().equals("1.3.6.1.2.1.4.8.0"))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "InDiscards";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(0).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(1).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				System.out.println(stra);
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                      }
                      catch (SNMPBadValueException e)
                      {
                          // won't happen...
 	                 }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // ipInDiscards 
             
             
             // ipInDelivers | Counter | Read-Only | Mandatory |
             if (snmpOID.toString().equals("1.3.6.1.2.1.4.9.0"))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "InDelivers";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(0).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(1).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				System.out.println(stra);
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                      }
                      catch (SNMPBadValueException e)
                      {
                          // won't happen...
 	                 }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // ipInDelivers 
             
             
             // ipOutRequests | Counter | Read-Only | Mandatory |
             if (snmpOID.toString().equals("1.3.6.1.2.1.4.10.0"))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "OutRequests";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(0).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(1).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				System.out.println(stra);
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                      }
                      catch (SNMPBadValueException e)
                      {
                          // won't happen...
 	                 }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // ipOutRequests
             
             
             // ipOutDiscards | Counter | Read-Only | Mandatory |
             if (snmpOID.toString().equals("1.3.6.1.2.1.4.11.0"))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "OutDiscards";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(0).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(1).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				System.out.println(stra);
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                      }
                      catch (SNMPBadValueException e)
                      {
                          // won't happen...
 	                 }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // ipOutDiscards
             
             
             // ipOutNoRoutes | Counter | Read-Only | Mandatory |
             if (snmpOID.toString().equals("1.3.6.1.2.1.4.12.0"))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "OutNoRoutes";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(0).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(1).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				System.out.println(stra);
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                      }
                      catch (SNMPBadValueException e)
                      {
                          // won't happen...
 	                 }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // ipOutNoRoutes
             
             
             // ipReasmTimeout | Integer | Read-Only | Mandatory |
             if (snmpOID.toString().equals("1.3.6.1.2.1.4.13.0"))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "ReasmTimeout";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(0).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(1).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				System.out.println(stra);
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPInteger(recv));
                         responseList.addSNMPObject(newPair);
                      }
                      catch (SNMPBadValueException e)
                      {
                          // won't happen...
 	                 }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // ipReasmTimeout
             
             
          // ipReasmReqds | Counter | Read-Only | Mandatory |
             if (snmpOID.toString().equals("1.3.6.1.2.1.4.14.0"))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "ReasmReqds";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(0).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(1).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				System.out.println(stra);
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                      }
                      catch (SNMPBadValueException e)
                      {
                          // won't happen...
 	                 }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // ipReasmReqds
             
             
             // ipReasmOKs | Counter | Read-Only | Mandatory |
             if (snmpOID.toString().equals("1.3.6.1.2.1.4.15.0"))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "ReasmOKs";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(0).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(1).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				System.out.println(stra);
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                      }
                      catch (SNMPBadValueException e)
                      {
                          // won't happen...
 	                 }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // ipReasmOKs
             
             
             // ipReasmFails | Counter | Read-Only | Mandatory |
             if (snmpOID.toString().equals("1.3.6.1.2.1.4.16.0"))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "ReasmFails";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(0).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(1).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				System.out.println(stra);
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                      }
                      catch (SNMPBadValueException e)
                      {
                          // won't happen...
 	                 }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // ipReasmFails
             
             // ipFragOKs | Counter | Read-Only | Mandatory |
             if (snmpOID.toString().equals("1.3.6.1.2.1.4.17.0"))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "FragOKs";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(0).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(1).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				System.out.println(stra);
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                      }
                      catch (SNMPBadValueException e)
                      {
                          // won't happen...
 	                 }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // ipFragOKs
             
          // ipFragFails | Counter | Read-Only | Mandatory |
             if (snmpOID.toString().equals("1.3.6.1.2.1.4.18.0"))
             {
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "FragFails";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(0).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(1).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				System.out.println(stra);
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                      }
                      catch (SNMPBadValueException e)
                      {
                          // won't happen...
 	                 }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // ipFragFails
             
          // ipFragCreates | Counter | Read-Only | Mandatory |
             if (snmpOID.toString().equals("1.3.6.1.2.1.4.19.0"))
             {
 
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "FragCreates";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(0).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(1).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				System.out.println(stra);
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                      }
                      catch (SNMPBadValueException e)
                      {
                          // won't happen...
 	                 }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // ipFragCreates
             
             /**
              * 
              *  the IP Address Table
              *  ipAddrTable 1.3.6.1.2.1.4.20
              *  
              * */
          
             if (snmpOID.toString().startsWith(("1.3.6.1.2.1.4.20.1.")))
             {
             	String cmd="ip address show"+"\n";
             	String line = ""; // deafult_ttl
             	Process p=null;
             	ArrayList<String> lines = new ArrayList<String>();
         		BufferedReader in2=null;
 
             	try
                 {
             		p = Runtime.getRuntime().exec(cmd);
         			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
         			
         			while ((line =in2.readLine()) != null) {
         				lines.add(line);
         			}
 
         			String ifIndex;
         			int x = 0;
         			String cadena = "";
         			cadena = lines.get(x);
         			ArrayList<ArrayList<String>> table = new ArrayList<ArrayList<String>>();
         			while ( x < lines.size()) {
 
         				if(cadena.contains("mtu") && cadena.contains("qdisc") && cadena.contains("state") ){
 
         					String arrTmp[] = cadena.split(" ");
         					ifIndex = arrTmp[0].substring(0, arrTmp[0].length()-1);
         					
         					do{
         						
         						if(!cadena.contains("inet6") && cadena.contains("inet")){
 
                 					arrTmp = cadena.split(" ");	
                 					
                 					String arrTmp1[] = arrTmp[5].split("/");
                 					String ipAddr = arrTmp1[0], strMask = arrTmp1[1] ;
                 					int mask = Integer.parseInt(strMask);
                 					
                 						if (mask <= 8){
                 							strMask = (256 - (Math.pow(2,(8 - mask)))) + ".0.0";
                 						}else if (mask <= 16){
                 							strMask =  "255." + (256 - (Math.pow(2,(16 - mask)))) +".0";
                 						}else if (mask <= 24){
                 							strMask =  "255.255." + (256 - (Math.pow(2,(24 - mask))));
                 						}else if (mask <= 32){
                 							strMask = "255.255.255." + (256 - (Math.pow(2,(32 - mask))));
                 						}	
                 						
                 						ArrayList<String> rowTable = new ArrayList<String>();
                 						rowTable.add(ipAddr);
                 						rowTable.add(ifIndex);
                 						rowTable.add(strMask);
                 						System.out.println(strMask);
                 						rowTable.add("1"); //BroadCast
                 						rowTable.add("65535"); //
                 						
                 						table.add(rowTable);
                 				}
         						x++;
         						if (x >= lines.size()) break; 
         						cadena = lines.get(x);
         					}while(!(cadena.contains("mtu") && cadena.contains("qdisc") && cadena.contains("state")));
         					
         				} // if
         				
         			} // while ( x < lines.size()) {
         			
         			for (int j = 0; j < table.size(); j++) {
         				ArrayList<String> arrElement = table.get(j);
         				
         				// ipAdEntAddr | IpAddress | Read-Only | Mandatory
         				if(snmpOID.toString().equals(("1.3.6.1.2.1.4.20.1.1."+arrElement.get(0)))){
 
         	                if (pduType == SNMPBERCodec.SNMPSETREQUEST)
         	                {
         	                    int errorIndex = i+1;
         	                    int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
         	                    throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
         	                }
         	                else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
 	        	            {
         	                	try
 	    	                    {
 	    	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPIPAddress(arrElement.get(0)));
 	    	                        responseList.addSNMPObject(newPair);
 	    	                        break;
 	    	                    }
 	    	                    catch (SNMPBadValueException e)
 	    	                    {
 	    	                        // won't happen...
 	    	                    }
 	        	            }
         				}// ipAdEntAddr
         				
         				// ipAdEntIfIndex | Integer | Read-Only | Mandatory
         				if(snmpOID.toString().equals(("1.3.6.1.2.1.4.20.1.2."+arrElement.get(0)))){
 
         	                if (pduType == SNMPBERCodec.SNMPSETREQUEST)
         	                {
         	                    int errorIndex = i+1;
         	                    int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
         	                    throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
         	                }
         	                else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
 	        	            {
         	                	try
 	    	                    {
 	    	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPInteger(Integer.parseInt(arrElement.get(1))));
 	    	                        responseList.addSNMPObject(newPair);
 	    	                        break;
 	    	                    }
 	    	                    catch (SNMPBadValueException e)
 	    	                    {
 	    	                        // won't happen...
 	    	                    }
 	        	            }
         				}// ipAdEntIfIndex
         				
         				// ipAdEntNetMask | IpAddress | Read-Only | Mandatory
         				if(snmpOID.toString().equals(("1.3.6.1.2.1.4.20.1.3."+arrElement.get(0)))){
 
         	                if (pduType == SNMPBERCodec.SNMPSETREQUEST)
         	                {
         	                    int errorIndex = i+1;
         	                    int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
         	                    throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
         	                }
         	                else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
 	        	            {
         	                	try
 	    	                    {
         	                		System.out.println(arrElement.get(2));
 	    	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPIPAddress(arrElement.get(2)));
 	    	                        responseList.addSNMPObject(newPair);
 	    	                        break;
 	    	                    }
 	    	                    catch (SNMPBadValueException e)
 	    	                    {
 	    	                        // won't happen...
 	    	                    	e.printStackTrace();
 	    	                    }
 	        	            }
         				}// ipAdEntNetMask
         				
         				// ipAdEntBcastAddr | Integer | Read-Only | Mandatory
         				if(snmpOID.toString().equals(("1.3.6.1.2.1.4.20.1.4."+arrElement.get(0)))){
 
         	                if (pduType == SNMPBERCodec.SNMPSETREQUEST)
         	                {
         	                    int errorIndex = i+1;
         	                    int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
         	                    throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
         	                }
         	                else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
 	        	            {
         	                	try
 	    	                    {
 	    	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPInteger(Integer.parseInt(arrElement.get(3))));
 	    	                        responseList.addSNMPObject(newPair);
 	    	                        break;
 	    	                    }
 	    	                    catch (SNMPBadValueException e)
 	    	                    {
 	    	                        // won't happen...
 	    	                    }
 	        	            }
         				}// ipAdEntBcastAddr
         				
         				// ipAdEntBcastAddr | Integer | Read-Only | Mandatory
         				if(snmpOID.toString().equals(("1.3.6.1.2.1.4.20.1.5."+arrElement.get(0)))){
 
         	                if (pduType == SNMPBERCodec.SNMPSETREQUEST)
         	                {
         	                    int errorIndex = i+1;
         	                    int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
         	                    throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
         	                }
         	                else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
 	        	            {
         	                	try
 	    	                    {
 	    	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPInteger(Integer.parseInt(arrElement.get(4))));
 	    	                        responseList.addSNMPObject(newPair);
 	    	                        break;
 	    	                    }
 	    	                    catch (SNMPBadValueException e)
 	    	                    {
 	    	                        // won't happen...
 	    	                    }
 	        	            }
         				}// ipAdEntBcastAddr
 						
 					} 
         			
         			
                 }
                 catch (Exception e)
                 {
                     e.printStackTrace();
                 }
             	
             }
             
             
             /**
              * 
              *  the IP Routing Table
              *  ipAddrTable 1.3.6.1.2.1.4.21
              *  
              * */
          
             if (snmpOID.toString().startsWith(("1.3.6.1.2.1.4.21.1.")))
             {	//  0        1         2      3     4     5     6    7    8    9     10
             	//Iface Destination Gateway Flags RefCnt Use Metric Mask MTU Window IRTT
 	            	
             	if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/route\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			for(int x = 1 ; x < lines.size() ; x++){
 							String arrTmp[] = lines.get(x).split("\t");
 							String hexValue = arrTmp[1];
 							String ip = "";
 						
 							for(int i1 = hexValue.length(); i1 > 0; i1 = i1 - 2) {
 							    ip = ip + Integer.valueOf(hexValue.substring(i1-2, i1 ), 16) + ".";
 							}
 							ip= (ip.substring(0, ip.length()-1));
 							// ipRouteDest | ipAddress | Read-Write | Mandatory
 							if (snmpOID.toString().equals(("1.3.6.1.2.1.4.21.1.1."+ip)))
 			                {
 								SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPIPAddress(ip));
 		                        responseList.addSNMPObject(newPair);
 		                        
 		                    // ipRouteIfIndex | Integer | Read-Write | Mandatory
 			                }else if (snmpOID.toString().equals(("1.3.6.1.2.1.4.21.1.2."+ip)))
 			                {
 			                	cmd="ls /sys/class/net/";;
 			                	line = "";
 			                	int iFace=1;
 			                	try
 			                    {
 			                		p = Runtime.getRuntime().exec(cmd);
 			            			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 			            			
 			            			while ((line =in2.readLine()) != null) {
 			            				if(line.equals(arrTmp[0])){
 			            					break;
 			            				}
 			            				iFace++;
 			            			}
 			            		}catch (Exception e)
 				                 {
 				                	 e.printStackTrace();
 				                 }
 			                	
 								SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPInteger(iFace));
 		                        responseList.addSNMPObject(newPair);
 		                        
 		                    // ipRouteMetric1 | Integer | Read-Write | Mandatory
 			                }else if (snmpOID.toString().equals(("1.3.6.1.2.1.4.21.1.3."+ip)))
 			                {
 			                	SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPInteger(Integer.parseInt(arrTmp[6])));
 		                        responseList.addSNMPObject(newPair);
 		                    // ipRouteNextHop | ipAddress | Read-Write | Mandatory    
 			                }else if (snmpOID.toString().equals(("1.3.6.1.2.1.4.21.1.4."+ip)))
 			                {
 			                	hexValue = arrTmp[2];
 								ip = "";
 							
 								for(int i1 = hexValue.length(); i1 > 0; i1 = i1 - 2) {
 								    ip = ip + Integer.valueOf(hexValue.substring(i1-2, i1 ), 16) + ".";
 								}
 								ip= (ip.substring(0, ip.length()-1));
 			                	
 								SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPIPAddress(ip));
 		                        responseList.addSNMPObject(newPair);
 		                    // ipRouteMask | ipAddress | Read-Write | Mandatory    
 			                }else if (snmpOID.toString().equals(("1.3.6.1.2.1.4.21.1.5."+ip)))
 			                {
 			                	hexValue = arrTmp[7];
 								ip = "";
 							
 								for(int i1 = hexValue.length(); i1 > 0; i1 = i1 - 2) {
 								    ip = ip + Integer.valueOf(hexValue.substring(i1-2, i1 ), 16) + ".";
 								}
 								ip= (ip.substring(0, ip.length()-1));
 			                	
 								SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPIPAddress(ip));
 		                        responseList.addSNMPObject(newPair);
 			                }
 							
 						}
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
                 
             }
             
             
             
             /**
              * 
              *  the IP Address Translation table
              *  ipAddrTable 1.3.6.1.2.1.4.22
              *  
              * */
          
             if (snmpOID.toString().startsWith(("1.3.6.1.2.1.4.22.1.")))
             {
             	// ipNetToMediaIfIndex 
             	String cmd="ip address show"+"\n";
             	String line = ""; // deafult_ttl
             	Process p=null;
             	ArrayList<String> lines = new ArrayList<String>();
         		BufferedReader in2=null;
 
             	try
                 {
             		p = Runtime.getRuntime().exec(cmd);
         			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
         			
         			while ((line =in2.readLine()) != null) {
         				lines.add(line);
         			}
 
         			String ifIndex="",cadena="",phyIFace="";
         			int x = 0;
         			cadena = lines.get(x);
         			ArrayList<ArrayList<String>> table = new ArrayList<ArrayList<String>>();
         			
         			while ( x < lines.size()) {
 
         				if(cadena.contains("mtu") && cadena.contains("qdisc") && cadena.contains("state") ){
 
         					String arrTmp[] = cadena.split(" ");
         					ifIndex = arrTmp[0].substring(0, arrTmp[0].length()-1);
         					
         					do{
         						
         						if(cadena.contains("link/ether") || cadena.contains("link/loopback")){ /*|| cadena.contains("link/ppp") ||
         						   cadena.contains("link/tunnel6") || cadena.contains("link/sit") ||
         						   cadena.contains("link/loopback") ){ // si y solo si se puede colocar la ip 127.0.0.1*/
         							arrTmp = cadena.split(" ");
         							phyIFace = arrTmp[5];
         						}
         						
         						if(!cadena.contains("inet6") && cadena.contains("inet")){
 
                 					arrTmp = cadena.split(" ");	
                 					
                 					String arrTmp1[] = arrTmp[5].split("/");
                 					String ipAddr = arrTmp1[0];	
                 						
                 						ArrayList<String> rowTable = new ArrayList<String>();
                 						rowTable.add(ifIndex); // 0
                 						rowTable.add(phyIFace);// 1
                 						rowTable.add(ipAddr);  // 2
                 						rowTable.add("1");     // 3  Other always 
                 						
                 						table.add(rowTable);
                 				}
         						x++;
         						if (x >= lines.size()) break; 
         						cadena = lines.get(x);
         					}while(!(cadena.contains("mtu") && cadena.contains("qdisc") && cadena.contains("state")));
         					
 
         				} // if
         				
         			} // while ( x < lines.size()) {
         			
         			for (int j = 0; j < table.size(); j++){
         				ArrayList<String> arrElement = table.get(j);
         				
         				// ipNetToMediaIfIndex | INTEGER | read-write | mandatory
         				if(snmpOID.toString().equals(("1.3.6.1.2.1.4.22.1.1."+arrElement.get(2)))){
 
         	                if (pduType == SNMPBERCodec.SNMPSETREQUEST)
         	                {
         	                    int errorIndex = i+1;
         	                    int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
         	                    throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
         	                }
         	                else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
 	        	            {
         	                	try
 	    	                    {
 	    	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPInteger(Integer.parseInt(arrElement.get(0))));
 	    	                        responseList.addSNMPObject(newPair);
 	    	                        break;
 	    	                    }
 	    	                    catch (SNMPBadValueException e)
 	    	                    {
 	    	                        // won't happen...
 	    	                    }
 	        	            }
         				}// ipNetToMediaIfIndex
         				
         				// ipNetToMediaPhysAddress  | PhysAddress | read-write | mandatory
         				if(snmpOID.toString().equals(("1.3.6.1.2.1.4.22.1.2."+arrElement.get(2)))){
 
         	                if (pduType == SNMPBERCodec.SNMPSETREQUEST)
         	                {
         	                    int errorIndex = i+1;
         	                    int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
         	                    throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
         	                }
         	                else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
 	        	            {
         	                	try
 	    	                    {
 	    	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPOctetString(arrElement.get(1)));
 	    	                        responseList.addSNMPObject(newPair);
 	    	                        break;
 	    	                    }
 	    	                    catch (SNMPBadValueException e)
 	    	                    {
 	    	                    	e.printStackTrace();
 	    	                    }
 	        	            }
         				}// ipNetToMediaPhysAddress 
         				
         				// ipNetToMediaNetAddress   | PhysAddress | read-write | mandatory
         				if(snmpOID.toString().equals(("1.3.6.1.2.1.4.22.1.3."+arrElement.get(2)))){
 
         	                if (pduType == SNMPBERCodec.SNMPSETREQUEST)
         	                {
         	                    int errorIndex = i+1;
         	                    int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
         	                    throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
         	                }
         	                else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
 	        	            {
         	                	try
 	    	                    {
 	    	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()),new SNMPIPAddress(arrElement.get(2))); 
 	    	                        responseList.addSNMPObject(newPair);
 	    	                        break;
 	    	                    }
 	    	                    catch (SNMPBadValueException e)
 	    	                    {
 	    	                        e.printStackTrace();
 	    	                    }
 	        	            }
         				}// ipNetToMediaNetAddress  
         				
         				// ipNetToMediaType    | PhysAddress | read-write | mandatory
         				if(snmpOID.toString().equals(("1.3.6.1.2.1.4.22.1.4."+arrElement.get(2)))){
 
         	                if (pduType == SNMPBERCodec.SNMPSETREQUEST)
         	                {
         	                    int errorIndex = i+1;
         	                    int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
         	                    throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
         	                }
         	                else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
 	        	            {
         	                	try
 	    	                    {
 	    	                        SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPInteger(Integer.parseInt(arrElement.get(3))));
 	    	                        responseList.addSNMPObject(newPair);
 	    	                        break;
 	    	                    }
 	    	                    catch (SNMPBadValueException e)
 	    	                    {
 	    	                        // won't happen...
 	    	                    }
 	        	            }
         				}// ipNetToMediaType  
         				
         			} // for (int j = 0; j < table.size(); j++)
         			
         			
                 }
                 catch (Exception e)
                 {
                     e.printStackTrace();
                 }
             	
             }
 
             /**
              * 
              * the ICMP Group
              * 
              */
             
             // icmpInMsgs | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.5.1.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "InMsgs";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(2).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(3).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // icmpInMsgs
             
          // icmpInErrors | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.5.2.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "InErrors";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(2).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(3).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // icmpInErrors
             
             
             // icmpInDestUnreachs | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.5.3.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "InDestUnreachs";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(2).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(3).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // icmpInDestUnreachs
             
             
          // icmpInTimeExcds | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.5.4.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "InTimeExcds";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(2).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(3).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // icmpInTimeExcds
             
             
             // icmpInParmProbs | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.5.5.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "InParmProbs";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(2).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(3).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // icmpInParmProbs
             
             
          // icmpInSrcQuenchs | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.5.6.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "InSrcQuenchs";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(2).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(3).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // icmpInSrcQuenchs
             
             
          // icmpInRedirects | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.5.7.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "InRedirects";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(2).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(3).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // icmpInRedirects
             
             // icmpInEchos | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.5.8.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "InEchos";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(2).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(3).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // icmpInEchos
             
             // icmpInEchoReps | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.5.9.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "InEchoReps";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(2).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(3).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // icmpInEchoReps
             
             
             // icmpInTimestamps | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.5.10.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "InTimestamps";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(2).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(3).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // icmpInTimestamps
             
             
             // icmpInTimestampReps | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.5.11.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "InTimestampReps";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(2).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(3).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // icmpInTimestampReps
             
             
             // icmpInAddrMasks | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.5.12.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "InAddrMasks";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(2).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(3).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // icmpInAddrMasks
             
             
          // icmpInAddrMaskReps | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.5.13.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "InAddrMaskReps";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(2).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(3).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // icmpInAddrMaskReps
             
             
             // icmpOutMsgs | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.5.14.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "OutMsgs";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(2).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(3).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // icmpOutMsgs
             
             
          // icmpOutErrors | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.5.15.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "OutErrors";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(2).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(3).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // icmpOutErrors
             
             
             // icmpOutDestUnreachs | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.5.16.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "OutDestUnreachs";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(2).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(3).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // icmpOutDestUnreachs
             
             
          // icmpOutTimeExcds | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.5.17.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "OutTimeExcds";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(2).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(3).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // icmpOutTimeExcds
             
             
             // icmpOutParmProbs | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.5.18.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "OutParmProbs";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(2).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(3).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // icmpOutParmProbs
             
             
             // icmpOutSrcQuenchs | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.5.19.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "OutSrcQuenchs";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(2).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(3).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // icmpOutSrcQuenchs
             
             
             // icmpOutRedirects | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.5.20.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "OutRedirects";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(2).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(3).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // icmpOutRedirects
             
             // icmpOutEchos | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.5.21.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "OutEchos";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(2).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(3).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // icmpOutEchos
             
             
          // icmpOutEchoReps | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.5.22.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "OutEchoReps";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(2).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(3).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // icmpOutEchoReps
             
             
             // icmpOutTimestamps | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.5.23.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "OutTimestamps";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(2).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(3).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // icmpOutTimestamps
             
             
             // icmpOutTimestampReps | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.5.24.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "OutTimestampReps";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(2).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(3).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // icmpOutTimestampReps
             
             // icmpOutAddrMasks | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.5.25.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "OutAddrMasks";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(2).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(3).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // icmpOutAddrMasks
             
             
             // icmpOutAddrMaskReps | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.5.26.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "OutAddrMaskReps";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(2).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(3).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			long recv = Long.parseLong( arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // icmpOutAddrMaskReps
             
             /**
              * 
              * the TCP Group 
              *
              */
             
             // tcpRtoAlgorithm | Integer | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.6.1.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
 
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "RtoAlgorithm";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(6).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(7).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			int recv = Integer.parseInt(arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPInteger(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // tcpRtoAlgorithm
             
             // tcpRtoMin | Integer | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.6.2.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
 
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "RtoMin";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(6).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(7).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			int recv = Integer.parseInt(arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPInteger(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // tcpRtoMin
             
             
             // tcpRtoMax | Integer | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.6.3.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
 
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "RtoMax";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(6).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(7).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			int recv = Integer.parseInt(arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPInteger(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // tcpRtoMax
             
             
             // tcpMaxConn | Integer | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.6.4.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
 
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "MaxConn";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(6).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(7).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			int recv = Integer.parseInt(arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPInteger(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // tcpMaxConn
             
             
             // tcpActiveOpens | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.6.5.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
 
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "ActiveOpens";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(6).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(7).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			Long recv = Long.parseLong(arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // tcpActiveOpens
             
             
             // tcpPassiveOpens | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.6.6.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
 
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "PassiveOpens";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(6).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(7).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			Long recv = Long.parseLong(arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // tcpPassiveOpens
             
             
             // tcpAttemptFails | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.6.7.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
 
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "AttemptFails";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(6).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(7).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			Long recv = Long.parseLong(arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // tcpAttemptFails
             
             
             // tcpEstabResets | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.6.8.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
 
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "EstabResets";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(6).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(7).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			Long recv = Long.parseLong(arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // tcpEstabResets
             
             
             // tcpCurrEstab | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.6.9.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
 
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "CurrEstab";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(6).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(7).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			Long recv = Long.parseLong(arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // tcpCurrEstab
             
             
             // tcpInSegs | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.6.10.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
 
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "InSegs";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(6).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(7).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			Long recv = Long.parseLong(arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // tcpInSegs
             
             
             // tcpOutSegs | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.6.11.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
 
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "OutSegs";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(6).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(7).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			Long recv = Long.parseLong(arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // tcpOutSegs
             
             
             // tcpRetransSegs | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.6.12.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
 
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "RetransSegs";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(6).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(7).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			Long recv = Long.parseLong(arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // tcpRetransSegs
             
             /** the TCP Connection Table 
              * tcpConnTable 1.3.6.1.2.1.6.13
              * */
             // tcpConnEntry
             if (snmpOID.toString().startsWith("1.3.6.1.2.1.6.13.1."))
             {
             	// tcpConnState | Integer | Read-Write | Mandatory
             	if (snmpOID.toString().startsWith("1.3.6.1.2.1.6.13.1.1"))
                 {
             		String cmd="netstat\n", line, oidConn="";
             		Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
             		int stateConn=0;
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			/* Comienzo en 1 para saltar esta linea:
             			 * Proto Recv-Q Send-Q Local_Address Foreign_Address State
             			 */
     		
 						String matNetstat[][] = new String[lines.size()-1][6];
 
             			for (int j = 1; j < lines.size(); j++) {
 							String arrTmp[] = lines.get(j).split(" ");
 							for (int k = 0, x = 0; k < arrTmp.length; k++) {
 								if(!arrTmp[k].equalsIgnoreCase("")){
 									matNetstat[j-1][x] = arrTmp[k];
 									x++;
 								}
 							}
 						}
             			
             			for (int j = 0; j < matNetstat.length; j++) {
 							
             				if(matNetstat[j][0].equalsIgnoreCase("tcp6")){
             					
             					String arr[] = matNetstat[j][3].split(":");
             					// :::* -> [][][][*]
             					if(arr[arr.length-2].equalsIgnoreCase("") && arr[arr.length-1].equalsIgnoreCase("*")){
             						oidConn = "0.0.0.0.0"; // 0.0.0.0:0
             					}else{ // ::1:25 -> [][][1][25]
             						if(arr[arr.length-2].equalsIgnoreCase("1") && !arr[arr.length-1].equalsIgnoreCase("")){
             							oidConn = "127.0.0.1."+arr[arr.length-1]; // 127.0.0.1:25
             						}else{ // 0.0.0.0:* -> [0.0.0.0][*]
             							if(arr[arr.length-2].equalsIgnoreCase("0.0.0.0") && arr[arr.length-1].equalsIgnoreCase("*")){
                 							oidConn = "0.0.0.0.0"; // 0.0.0.0.0
             							}else{
             								oidConn = arr[arr.length-2]+"."+ arr[arr.length-1];
             							}
             						}
             					}
             					 
 								arr = matNetstat[j][4].split(":");
 								
 								if(arr[arr.length-2].equalsIgnoreCase("") && arr[arr.length-1].equalsIgnoreCase("*")){
             						oidConn = ".0.0.0.0.0"; // 0.0.0.0:0
             					}else{ // ::1:25 -> [][][1][25]
             						if(arr[arr.length-2].equalsIgnoreCase("1") && !arr[arr.length-1].equalsIgnoreCase("")){
             							oidConn = ".127.0.0.1."+arr[arr.length-1]; // 127.0.0.1:25
             						}else{ // 0.0.0.0:* -> [0.0.0.0][*]
             							if(arr[arr.length-2].equalsIgnoreCase("0.0.0.0") && arr[arr.length-1].equalsIgnoreCase("*")){
                 							oidConn = ".0.0.0.0.0"; // 0.0.0.0.0
             							}else{
             								oidConn += "."+arr[arr.length-2]+"."+ arr[arr.length-1];
             							}
             						}
             					}			
 								
             				}else if(matNetstat[j][0].equalsIgnoreCase("tcp")){
             					
             					String arr[] = matNetstat[j][3].split(":");
             					// 0.0.0.0:* -> [0.0.0.0][*]
             					if(arr[arr.length-2].equalsIgnoreCase("0.0.0.0") && arr[arr.length-1].equalsIgnoreCase("*")){
         							oidConn = "0.0.0.0.0"; 
     							}else{
     								oidConn = arr[arr.length-2]+"."+ arr[arr.length-1];
     							}
             					 
 								arr = matNetstat[j][4].split(":");
 								// 0.0.0.0:* -> [0.0.0.0][*]
             					if(arr[arr.length-2].equalsIgnoreCase("0.0.0.0") && arr[arr.length-1].equalsIgnoreCase("*")){
         							oidConn = ".0.0.0.0.0"; 
     							}else{
     								oidConn += "."+arr[arr.length-2]+"."+ arr[arr.length-1];
     							}
             					 								
             				}else if(matNetstat[j][0].equalsIgnoreCase("udp") || matNetstat[j][0].equalsIgnoreCase("udp6")){
             					continue;
             				}
             				
 	            			if(snmpOID.toString().equalsIgnoreCase(("1.3.6.1.2.1.6.13.1.1."+oidConn))){
 	            				
 	            				if(matNetstat[j][5].equalsIgnoreCase("closed")) {
 	            					stateConn=1;
 	            				}else if(matNetstat[j][5].equalsIgnoreCase("listen")) {
 	            					stateConn=2;
 	            				}else if(matNetstat[j][5].equalsIgnoreCase("syn_sent")) {
 	            					stateConn=3;
 	            				}else if(matNetstat[j][5].equalsIgnoreCase("syn_received")) {
 	            					stateConn=4;
 	            				}else if(matNetstat[j][5].equalsIgnoreCase("established")) {
 	            					stateConn=5;
 	            				}else if(matNetstat[j][5].equalsIgnoreCase("fin_wait_1")) {
 	            					stateConn=6;
 	            				}else if(matNetstat[j][5].equalsIgnoreCase("fin_wait_2")) {
 	            					stateConn=7;
 	            				}else if(matNetstat[j][5].equalsIgnoreCase("close_wait")) {
 	            					stateConn=8;
 	            				}else if(matNetstat[j][5].equalsIgnoreCase("last_ack")) {
 	            					stateConn=9;
 	            				}else if(matNetstat[j][5].equalsIgnoreCase("closing")) {
 	            					stateConn=10;
 	            				}else if(matNetstat[j][5].equalsIgnoreCase("time_wait")) {
 	            					stateConn=11;
 	            				}else if(matNetstat[j][5].equalsIgnoreCase("delete_tcb")) {
 	            					stateConn=12;
 	            				}
 										
 	            				try {
 	            					
 	            					SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPInteger(stateConn));
 	                                responseList.addSNMPObject(newPair);
 								} catch (Exception e2) {
 									e2.printStackTrace();
 								}
 	            			}
 						}
             			
                     }
             		catch (Exception e)
    	                {
    	                	 e.printStackTrace();
    	                }
             	
                 // tcpConnLocalAddress | IPAddress | Read-Only | Mandatory
                 }else if (snmpOID.toString().startsWith("1.3.6.1.2.1.6.13.1.2"))
                 {
             		String cmd="netstat\n", line, oidConn = "";
             		String ipAddress1="", ipAddress2="", portLocal="", portForeign="";
             		Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
             		
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			/* Comienzo en 1 para saltar esta linea:
             			 * Proto Recv-Q Send-Q Local_Address Foreign_Address State
             			 */
     		
 						String matNetstat[][] = new String[lines.size()-1][6];
 
             			for (int j = 1; j < lines.size(); j++) {
 							String arrTmp[] = lines.get(j).split(" ");
 							for (int k = 0, x = 0; k < arrTmp.length; k++) {
 								if(!arrTmp[k].equalsIgnoreCase("")){
 									matNetstat[j-1][x] = arrTmp[k];
 									x++;
 								}
 							}
 						}
             			
             			for (int j = 0; j < matNetstat.length; j++) {
 							
             				if(matNetstat[j][0].equalsIgnoreCase("tcp6")){
             					
             					String arr[] = matNetstat[j][3].split(":");
             					// :::* -> [][][][*]
             					if(arr[arr.length-2].equalsIgnoreCase("") && arr[arr.length-1].equalsIgnoreCase("*")){
             						ipAddress1="0.0.0.0"; portLocal="0";
             					}else{ // ::1:25 -> [][][1][25]
             						if(arr[arr.length-2].equalsIgnoreCase("1") && !arr[arr.length-1].equalsIgnoreCase("")){
             							ipAddress1="127.0.0.1"; portLocal=arr[arr.length-1];
             						}else{ // 0.0.0.0:* -> [0.0.0.0][*]
             							if(arr[arr.length-2].equalsIgnoreCase("0.0.0.0") && arr[arr.length-1].equalsIgnoreCase("*")){
             								ipAddress1="0.0.0.0"; portLocal="0";
             							}else{
             								ipAddress1=arr[arr.length-2]; portLocal=arr[arr.length-1];
             							}
             						}
             					}
             					 
 								arr = matNetstat[j][4].split(":");
 								
 								if(arr[arr.length-2].equalsIgnoreCase("") && arr[arr.length-1].equalsIgnoreCase("*")){
             						ipAddress2="0.0.0.0"; portForeign="0";
             					}else{ // ::1:25 -> [][][1][25]
             						if(arr[arr.length-2].equalsIgnoreCase("1") && !arr[arr.length-1].equalsIgnoreCase("")){
             							ipAddress2="127.0.0.1"; portForeign=arr[arr.length-1];
             						}else{ // 0.0.0.0:* -> [0.0.0.0][*]
             							if(arr[arr.length-2].equalsIgnoreCase("0.0.0.0") && arr[arr.length-1].equalsIgnoreCase("*")){
             								ipAddress2="0.0.0.0"; portForeign="0";
             							}else{
             								ipAddress2=arr[arr.length-2]; portForeign=arr[arr.length-1];
             							}
             						}
             					}			
 								
             				}else if(matNetstat[j][0].equalsIgnoreCase("tcp")){
             					
             					String arr[] = matNetstat[j][3].split(":");
             					// 0.0.0.0:* -> [0.0.0.0][*]
             					if(arr[arr.length-2].equalsIgnoreCase("0.0.0.0") && arr[arr.length-1].equalsIgnoreCase("*")){
             						ipAddress1="0.0.0.0"; portLocal="0"; 
     							}else{
     								ipAddress1=arr[arr.length-2]; portLocal=arr[arr.length-1];
     							}
             					 
 								arr = matNetstat[j][4].split(":");
 								// 0.0.0.0:* -> [0.0.0.0][*]
             					if(arr[arr.length-2].equalsIgnoreCase("0.0.0.0") && arr[arr.length-1].equalsIgnoreCase("*")){
             						ipAddress2="0.0.0.0"; portForeign="0";
     							}else{
     								ipAddress2=arr[arr.length-2]; portForeign=arr[arr.length-1];
     							}
             					 								
             				}else if(matNetstat[j][0].equalsIgnoreCase("udp") || matNetstat[j][0].equalsIgnoreCase("udp6")){
             					continue;
             				}
             				
             				oidConn = ipAddress1 + "." + portLocal + "." + ipAddress2 + "." + portForeign; 
             				
 	            			if(snmpOID.toString().equalsIgnoreCase(("1.3.6.1.2.1.6.13.1.2."+oidConn))){            					
 	            				try {
 	            					SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPIPAddress(ipAddress1));
 	                                responseList.addSNMPObject(newPair);
 								} catch (Exception e2) {
 									e2.printStackTrace();
 								}
 	            			}
 						}
             			
                     }
             		catch (Exception e)
    	                {
    	                	 e.printStackTrace();
    	                }
             	
                 // tcpConnLocalPort | Integer | Read-Only | Mandatory
                 }else if (snmpOID.toString().startsWith("1.3.6.1.2.1.6.13.1.3"))
                 {
             		String cmd="netstat\n", line, oidConn = "";
             		String ipAddress1="", ipAddress2="", portLocal="", portForeign="";
             		Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
             		
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			/* Comienzo en 1 para saltar esta linea:
             			 * Proto Recv-Q Send-Q Local_Address Foreign_Address State
             			 */
     		
 						String matNetstat[][] = new String[lines.size()-1][6];
 
             			for (int j = 1; j < lines.size(); j++) {
 							String arrTmp[] = lines.get(j).split(" ");
 							for (int k = 0, x = 0; k < arrTmp.length; k++) {
 								if(!arrTmp[k].equalsIgnoreCase("")){
 									matNetstat[j-1][x] = arrTmp[k];
 									x++;
 								}
 							}
 						}
             			
             			for (int j = 0; j < matNetstat.length; j++) {
 							
             				if(matNetstat[j][0].equalsIgnoreCase("tcp6")){
             					
             					String arr[] = matNetstat[j][3].split(":");
             					// :::* -> [][][][*]
             					if(arr[arr.length-2].equalsIgnoreCase("") && arr[arr.length-1].equalsIgnoreCase("*")){
             						ipAddress1="0.0.0.0"; portLocal="0";
             					}else{ // ::1:25 -> [][][1][25]
             						if(arr[arr.length-2].equalsIgnoreCase("1") && !arr[arr.length-1].equalsIgnoreCase("")){
             							ipAddress1="127.0.0.1"; portLocal=arr[arr.length-1];
             						}else{ // 0.0.0.0:* -> [0.0.0.0][*]
             							if(arr[arr.length-2].equalsIgnoreCase("0.0.0.0") && arr[arr.length-1].equalsIgnoreCase("*")){
             								ipAddress1="0.0.0.0"; portLocal="0";
             							}else{
             								ipAddress1=arr[arr.length-2]; portLocal=arr[arr.length-1];
             							}
             						}
             					}
             					 
 								arr = matNetstat[j][4].split(":");
 								
 								if(arr[arr.length-2].equalsIgnoreCase("") && arr[arr.length-1].equalsIgnoreCase("*")){
             						ipAddress2="0.0.0.0"; portForeign="0";
             					}else{ // ::1:25 -> [][][1][25]
             						if(arr[arr.length-2].equalsIgnoreCase("1") && !arr[arr.length-1].equalsIgnoreCase("")){
             							ipAddress2="127.0.0.1"; portForeign=arr[arr.length-1];
             						}else{ // 0.0.0.0:* -> [0.0.0.0][*]
             							if(arr[arr.length-2].equalsIgnoreCase("0.0.0.0") && arr[arr.length-1].equalsIgnoreCase("*")){
             								ipAddress2="0.0.0.0"; portForeign="0";
             							}else{
             								ipAddress2=arr[arr.length-2]; portForeign=arr[arr.length-1];
             							}
             						}
             					}			
 								
             				}else if(matNetstat[j][0].equalsIgnoreCase("tcp")){
             					
             					String arr[] = matNetstat[j][3].split(":");
             					// 0.0.0.0:* -> [0.0.0.0][*]
             					if(arr[arr.length-2].equalsIgnoreCase("0.0.0.0") && arr[arr.length-1].equalsIgnoreCase("*")){
             						ipAddress1="0.0.0.0"; portLocal="0"; 
     							}else{
     								ipAddress1=arr[arr.length-2]; portLocal=arr[arr.length-1];
     							}
             					 
 								arr = matNetstat[j][4].split(":");
 								// 0.0.0.0:* -> [0.0.0.0][*]
             					if(arr[arr.length-2].equalsIgnoreCase("0.0.0.0") && arr[arr.length-1].equalsIgnoreCase("*")){
             						ipAddress2="0.0.0.0"; portForeign="0";
     							}else{
     								ipAddress2=arr[arr.length-2]; portForeign=arr[arr.length-1];
     							}
             					 								
             				}else if(matNetstat[j][0].equalsIgnoreCase("udp") || matNetstat[j][0].equalsIgnoreCase("udp6")){
             					continue;
             				}
             				
             				oidConn = ipAddress1 + "." + portLocal + "." + ipAddress2 + "." + portForeign; 
             				
 	            			if(snmpOID.toString().equalsIgnoreCase(("1.3.6.1.2.1.6.13.1.3."+oidConn))){            					
 	            				try {
 	            					SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPInteger(Integer.parseInt(portLocal)));
 	                                responseList.addSNMPObject(newPair);
 								} catch (Exception e2) {
 									e2.printStackTrace();
 								}
 	            			}
 						}
             			
                     }
             		catch (Exception e)
    	                {
    	                	 e.printStackTrace();
    	                }
             		
                 // tcpConnRemAddress | IPAddress | Read-Only | Mandatory	
                 }else if (snmpOID.toString().startsWith("1.3.6.1.2.1.6.13.1.4"))
                 {
             		String cmd="netstat\n", line, oidConn = "";
             		String ipAddress1="", ipAddress2="", portLocal="", portForeign="";
             		Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
             		
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			/* Comienzo en 1 para saltar esta linea:
             			 * Proto Recv-Q Send-Q Local_Address Foreign_Address State
             			 */
     		
 						String matNetstat[][] = new String[lines.size()-1][6];
 
             			for (int j = 1; j < lines.size(); j++) {
 							String arrTmp[] = lines.get(j).split(" ");
 							for (int k = 0, x = 0; k < arrTmp.length; k++) {
 								if(!arrTmp[k].equalsIgnoreCase("")){
 									matNetstat[j-1][x] = arrTmp[k];
 									x++;
 								}
 							}
 						}
             			
             			for (int j = 0; j < matNetstat.length; j++) {
 							
             				if(matNetstat[j][0].equalsIgnoreCase("tcp6")){
             					
             					String arr[] = matNetstat[j][3].split(":");
             					// :::* -> [][][][*]
             					if(arr[arr.length-2].equalsIgnoreCase("") && arr[arr.length-1].equalsIgnoreCase("*")){
             						ipAddress1="0.0.0.0"; portLocal="0";
             					}else{ // ::1:25 -> [][][1][25]
             						if(arr[arr.length-2].equalsIgnoreCase("1") && !arr[arr.length-1].equalsIgnoreCase("")){
             							ipAddress1="127.0.0.1"; portLocal=arr[arr.length-1];
             						}else{ // 0.0.0.0:* -> [0.0.0.0][*]
             							if(arr[arr.length-2].equalsIgnoreCase("0.0.0.0") && arr[arr.length-1].equalsIgnoreCase("*")){
             								ipAddress1="0.0.0.0"; portLocal="0";
             							}else{
             								ipAddress1=arr[arr.length-2]; portLocal=arr[arr.length-1];
             							}
             						}
             					}
             					 
 								arr = matNetstat[j][4].split(":");
 								
 								if(arr[arr.length-2].equalsIgnoreCase("") && arr[arr.length-1].equalsIgnoreCase("*")){
             						ipAddress2="0.0.0.0"; portForeign="0";
             					}else{ // ::1:25 -> [][][1][25]
             						if(arr[arr.length-2].equalsIgnoreCase("1") && !arr[arr.length-1].equalsIgnoreCase("")){
             							ipAddress2="127.0.0.1"; portForeign=arr[arr.length-1];
             						}else{ // 0.0.0.0:* -> [0.0.0.0][*]
             							if(arr[arr.length-2].equalsIgnoreCase("0.0.0.0") && arr[arr.length-1].equalsIgnoreCase("*")){
             								ipAddress2="0.0.0.0"; portForeign="0";
             							}else{
             								ipAddress2=arr[arr.length-2]; portForeign=arr[arr.length-1];
             							}
             						}
             					}			
 								
             				}else if(matNetstat[j][0].equalsIgnoreCase("tcp")){
             					
             					String arr[] = matNetstat[j][3].split(":");
             					// 0.0.0.0:* -> [0.0.0.0][*]
             					if(arr[arr.length-2].equalsIgnoreCase("0.0.0.0") && arr[arr.length-1].equalsIgnoreCase("*")){
             						ipAddress1="0.0.0.0"; portLocal="0"; 
     							}else{
     								ipAddress1=arr[arr.length-2]; portLocal=arr[arr.length-1];
     							}
             					 
 								arr = matNetstat[j][4].split(":");
 								// 0.0.0.0:* -> [0.0.0.0][*]
             					if(arr[arr.length-2].equalsIgnoreCase("0.0.0.0") && arr[arr.length-1].equalsIgnoreCase("*")){
             						ipAddress2="0.0.0.0"; portForeign="0";
     							}else{
     								ipAddress2=arr[arr.length-2]; portForeign=arr[arr.length-1];
     							}
             					 								
             				}else if(matNetstat[j][0].equalsIgnoreCase("udp") || matNetstat[j][0].equalsIgnoreCase("udp6")){
             					continue;
             				}
             				
             				oidConn = ipAddress1 + "." + portLocal + "." + ipAddress2 + "." + portForeign; 
             				
 	            			if(snmpOID.toString().equalsIgnoreCase(("1.3.6.1.2.1.6.13.1.4."+oidConn))){            					
 	            				try {
 	            					SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPIPAddress(ipAddress2));
 	                                responseList.addSNMPObject(newPair);
 								} catch (Exception e2) {
 									e2.printStackTrace();
 								}
 	            			}
 						}
             			
                     }
             		catch (Exception e)
    	                {
    	                	 e.printStackTrace();
    	                }
             		
                 // tcpConnRemPort | Integer | Read-Only | Mandatory	
                 }else if (snmpOID.toString().startsWith("1.3.6.1.2.1.6.13.1.5"))
                 {
             		String cmd="netstat\n", line, oidConn = "";
             		String ipAddress1="", ipAddress2="", portLocal="", portForeign="";
             		Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
             		
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
             			/* Comienzo en 1 para saltar esta linea:
             			 * Proto Recv-Q Send-Q Local_Address Foreign_Address State
             			 */
     		
 						String matNetstat[][] = new String[lines.size()-1][6];
 
             			for (int j = 1; j < lines.size(); j++) {
 							String arrTmp[] = lines.get(j).split(" ");
 							for (int k = 0, x = 0; k < arrTmp.length; k++) {
 								if(!arrTmp[k].equalsIgnoreCase("")){
 									matNetstat[j-1][x] = arrTmp[k];
 									x++;
 								}
 							}
 						}
             			
             			for (int j = 0; j < matNetstat.length; j++) {
 							
             				if(matNetstat[j][0].equalsIgnoreCase("tcp6")){
             					
             					String arr[] = matNetstat[j][3].split(":");
             					// :::* -> [][][][*]
             					if(arr[arr.length-2].equalsIgnoreCase("") && arr[arr.length-1].equalsIgnoreCase("*")){
             						ipAddress1="0.0.0.0"; portLocal="0";
             					}else{ // ::1:25 -> [][][1][25]
             						if(arr[arr.length-2].equalsIgnoreCase("1") && !arr[arr.length-1].equalsIgnoreCase("")){
             							ipAddress1="127.0.0.1"; portLocal=arr[arr.length-1];
             						}else{ // 0.0.0.0:* -> [0.0.0.0][*]
             							if(arr[arr.length-2].equalsIgnoreCase("0.0.0.0") && arr[arr.length-1].equalsIgnoreCase("*")){
             								ipAddress1="0.0.0.0"; portLocal="0";
             							}else{
             								ipAddress1=arr[arr.length-2]; portLocal=arr[arr.length-1];
             							}
             						}
             					}
             					 
 								arr = matNetstat[j][4].split(":");
 								
 								if(arr[arr.length-2].equalsIgnoreCase("") && arr[arr.length-1].equalsIgnoreCase("*")){
             						ipAddress2="0.0.0.0"; portForeign="0";
             					}else{ // ::1:25 -> [][][1][25]
             						if(arr[arr.length-2].equalsIgnoreCase("1") && !arr[arr.length-1].equalsIgnoreCase("")){
             							ipAddress2="127.0.0.1"; portForeign=arr[arr.length-1];
             						}else{ // 0.0.0.0:* -> [0.0.0.0][*]
             							if(arr[arr.length-2].equalsIgnoreCase("0.0.0.0") && arr[arr.length-1].equalsIgnoreCase("*")){
             								ipAddress2="0.0.0.0"; portForeign="0";
             							}else{
             								ipAddress2=arr[arr.length-2]; portForeign=arr[arr.length-1];
             							}
             						}
             					}			
 								
             				}else if(matNetstat[j][0].equalsIgnoreCase("tcp")){
             					
             					String arr[] = matNetstat[j][3].split(":");
             					// 0.0.0.0:* -> [0.0.0.0][*]
             					if(arr[arr.length-2].equalsIgnoreCase("0.0.0.0") && arr[arr.length-1].equalsIgnoreCase("*")){
             						ipAddress1="0.0.0.0"; portLocal="0"; 
     							}else{
     								ipAddress1=arr[arr.length-2]; portLocal=arr[arr.length-1];
     							}
             					 
 								arr = matNetstat[j][4].split(":");
 								// 0.0.0.0:* -> [0.0.0.0][*]
             					if(arr[arr.length-2].equalsIgnoreCase("0.0.0.0") && arr[arr.length-1].equalsIgnoreCase("*")){
             						ipAddress2="0.0.0.0"; portForeign="0";
     							}else{
     								ipAddress2=arr[arr.length-2]; portForeign=arr[arr.length-1];
     							}
             					 								
             				}else if(matNetstat[j][0].equalsIgnoreCase("udp") || matNetstat[j][0].equalsIgnoreCase("udp6")){
             					continue;
             				}
             				
             				oidConn = ipAddress1 + "." + portLocal + "." + ipAddress2 + "." + portForeign; 
             				
 	            			if(snmpOID.toString().equalsIgnoreCase(("1.3.6.1.2.1.6.13.1.5."+oidConn))){            					
 	            				try {
 	            					SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPInteger(Integer.parseInt(portForeign)));
 	                                responseList.addSNMPObject(newPair);
 								} catch (Exception e2) {
 									e2.printStackTrace();
 								}
 	            			}
 						}
             			
                     }
             		catch (Exception e)
    	                {
    	                	 e.printStackTrace();
    	                }
             		
                 }
 	
             	
             }
             
             
             // tcpInErrs | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.6.14.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
 
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "InErrs";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(6).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(7).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			Long recv = Long.parseLong(arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // tcpInErrs
             
             
             // tcpOutRsts | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.6.15.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
 
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "OutRsts";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(6).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(7).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			Long recv = Long.parseLong(arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // tcpOutRsts
             
             
             /**
              * 
              * the UDP Group
              * 
              */
             
             // udpInDatagrams | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.7.1.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
 
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "InDatagrams";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(8).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(9).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			Long recv = Long.parseLong(arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // udpInDatagrams
             
             // udpNoPorts | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.7.2.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
 
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "NoPorts";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(8).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(9).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			Long recv = Long.parseLong(arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // udpNoPorts
             
             
             // udpInErrors | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.7.3.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
 
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "InErrors";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(8).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(9).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			Long recv = Long.parseLong(arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // udpInErrors
             
             
             // udpOutDatagrams | Counter | Read-Only | Mandatory 
             if (snmpOID.toString().equals("1.3.6.1.2.1.7.4.0"))
             {
             	
                 if (pduType == SNMPBERCodec.SNMPSETREQUEST)
                 {
                     // got a set-request for our variable; throw an exception to indicate the 
                     // value is read-only - the SNMPv1AgentInterface will create the appropriate
                     // error message using our supplied error index and status
                     // note that error index starts at 1, not 0, so it's i+1
                     int errorIndex = i+1;
                     int errorStatus = SNMPRequestException.VALUE_READ_ONLY;
                     throw new SNMPSetException("Trying to set a read-only variable!", errorIndex, errorStatus);
                 }
                 else if (pduType == SNMPBERCodec.SNMPGETREQUEST)
                 {
                 	String cmd="cat /proc/net/snmp\n";
                 	String line = "";
                 	Process p=null;
                 	ArrayList<String> lines = new ArrayList<String>();
             		BufferedReader in2=null;
 
                 	try
                     {
                 		p = Runtime.getRuntime().exec(cmd);
             			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
             			while ((line =in2.readLine()) != null) {
             				lines.add(line);
             			}
 
             			ArrayList<String> arrValue = new ArrayList<String>(), arrName = new ArrayList<String>();
             			String value = "OutDatagrams";
             			arrName= new ArrayList<String>(Arrays.asList(lines.get(8).split(" "))); // IP Names
             			arrValue= new ArrayList<String>(Arrays.asList(lines.get(9).split(" "))); // IP Values
             			int pos = 0;
             			for (String stra : arrName) {
             				if(stra.equalsIgnoreCase(value)) break;
             				else pos++;
             			}
             			
             			Long recv = Long.parseLong(arrValue.get(pos));
             			SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPCounter32(recv));
                         responseList.addSNMPObject(newPair);
                         
                      }
 	                 catch (Exception e)
 	                 {
 	                	 e.printStackTrace();
 	                 }
                 }
                 
             } // udpOutDatagrams
             
             
             /** the TCP Connection Table 
              * udpConnTable 1.3.6.1.2.1.7.5
              * */
             // udpConnEntry
             if (snmpOID.toString().startsWith("1.3.6.1.2.1.7.5.1."))
             {
             	// udpLocalAddress | IPAddress | Read-Only | mandatory
 	            if (snmpOID.toString().startsWith("1.3.6.1.2.1.7.5.1.1."))
 	            {
 	        		String cmd="netstat\n", line, oidConn = "";
 	        		String ipAddress1="", portLocal="";
 	        		Process p=null;
 	            	ArrayList<String> lines = new ArrayList<String>();
 	        		BufferedReader in2=null;
 	        		
 	            	try
 	                {
 	            		p = Runtime.getRuntime().exec(cmd);
 	        			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 	        			while ((line =in2.readLine()) != null) {
 	        				lines.add(line);
 	        			}
 	        			/* Comienzo en 1 para saltar esta linea:
 	        			 * Proto Recv-Q Send-Q Local_Address Foreign_Address State
 	        			 */
 			
 						String matNetstat[][] = new String[lines.size()-1][6];
 	
 	        			for (int j = 1; j < lines.size(); j++) {
 							String arrTmp[] = lines.get(j).split(" ");
 							for (int k = 0, x = 0; k < arrTmp.length; k++) {
 								if(!arrTmp[k].equalsIgnoreCase("")){
 									matNetstat[j-1][x] = arrTmp[k];
 									x++;
 								}
 							}
 						}
 	        			
 	        			for (int j = 0; j < matNetstat.length; j++) {
 							
 	        				if(matNetstat[j][0].equalsIgnoreCase("udp6")){
 	        					
 	        					String arr[] = matNetstat[j][3].split(":");
 	        					// :::161 -> [][][][161]
 	        					if(arr[arr.length-2].equalsIgnoreCase("") && !arr[arr.length-1].equalsIgnoreCase("")){
 	        						ipAddress1="0.0.0.0"; portLocal=arr[arr.length-1];
 	        					}else{ // :::25 -> [][][1][25]
 	        						if(arr[arr.length-2].equalsIgnoreCase("1") && !arr[arr.length-1].equalsIgnoreCase("")){
 	        							ipAddress1="127.0.0.1"; portLocal=arr[arr.length-1];
 	        						}else{ // 0.0.0.0:* -> [0.0.0.0][*]
 	        							if(arr[arr.length-2].equalsIgnoreCase("0.0.0.0") && arr[arr.length-1].equalsIgnoreCase("*")){
 	        								ipAddress1="0.0.0.0"; portLocal="0";
 	        							}else{ //192.168.0.110:161 -> [192.168.0.110][161]
 	        								ipAddress1=arr[arr.length-2]; portLocal=arr[arr.length-1];
 	        							}
 	        						}
 	        					}		
 								
 	        				}else if(matNetstat[j][0].equalsIgnoreCase("udp")){
 	        					
 	        					String arr[] = matNetstat[j][3].split(":");
 	        					// 0.0.0.0:* -> [0.0.0.0][*]
 	        					if(arr[arr.length-2].equalsIgnoreCase("0.0.0.0") && arr[arr.length-1].equalsIgnoreCase("*")){
 	        						ipAddress1="0.0.0.0"; portLocal="0"; 
 								}else{
 									ipAddress1=arr[arr.length-2]; portLocal=arr[arr.length-1];
 								}
 	        					 								
 	        				}else if(matNetstat[j][0].equalsIgnoreCase("tcp") || matNetstat[j][0].equalsIgnoreCase("tcp6")){
 	        					continue;
 	        				}
 	        				
 	        				oidConn = ipAddress1 + "." + portLocal; 
 	        				System.out.println("1.3.6.1.2.1.7.5.1.1."+oidConn);
 	            			if(snmpOID.toString().equalsIgnoreCase(("1.3.6.1.2.1.7.5.1.1."+oidConn))){            					
 	            				try {
 	            					SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPIPAddress(ipAddress1));
 	                                responseList.addSNMPObject(newPair);
 								} catch (Exception e2) {
 									e2.printStackTrace();
 								}
 	            			}
 						}
 	        			
 	                }
 	        		catch (Exception e)
 		                {
 		                	 e.printStackTrace();
 		                }
 	            	
 	            // udpLocalPort | Integer | Read-Only | Mandatory
 	            }else if (snmpOID.toString().startsWith("1.3.6.1.2.1.7.5.1.2."))
 	            {
 	        		String cmd="netstat\n", line, oidConn = "";
 	        		String ipAddress1="", portLocal="";
 	        		Process p=null;
 	            	ArrayList<String> lines = new ArrayList<String>();
 	        		BufferedReader in2=null;
 	        		
 	            	try
 	                {
 	            		p = Runtime.getRuntime().exec(cmd);
 	        			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 	        			while ((line =in2.readLine()) != null) {
 	        				lines.add(line);
 	        			}
 	        			/* Comienzo en 1 para saltar esta linea:
 	        			 * Proto Recv-Q Send-Q Local_Address Foreign_Address State
 	        			 */
 			
 						String matNetstat[][] = new String[lines.size()-1][6];
 	
 	        			for (int j = 1; j < lines.size(); j++) {
 							String arrTmp[] = lines.get(j).split(" ");
 							for (int k = 0, x = 0; k < arrTmp.length; k++) {
 								if(!arrTmp[k].equalsIgnoreCase("")){
 									matNetstat[j-1][x] = arrTmp[k];
 									x++;
 								}
 							}
 						}
 	        			
 	        			for (int j = 0; j < matNetstat.length; j++) {
 							
 	        				if(matNetstat[j][0].equalsIgnoreCase("udp6")){
 	        					
 	        					String arr[] = matNetstat[j][3].split(":");
 	        					// :::161 -> [][][][161]
 	        					if(arr[arr.length-2].equalsIgnoreCase("") && !arr[arr.length-1].equalsIgnoreCase("")){
 	        						ipAddress1="0.0.0.0"; portLocal=arr[arr.length-1];
 	        					}else{ // ::1:25 -> [][][1][25]
 	        						if(arr[arr.length-2].equalsIgnoreCase("1") && !arr[arr.length-1].equalsIgnoreCase("")){
 	        							ipAddress1="127.0.0.1"; portLocal=arr[arr.length-1];
 	        						}else{ // 0.0.0.0:* -> [0.0.0.0][*]
 	        							if(arr[arr.length-2].equalsIgnoreCase("0.0.0.0") && arr[arr.length-1].equalsIgnoreCase("*")){
 	        								ipAddress1="0.0.0.0"; portLocal="0";
 	        							}else{ //192.168.0.110:161 -> [192.168.0.110][161]
 	        								ipAddress1=arr[arr.length-2]; portLocal=arr[arr.length-1];
 	        							}
 	        						}
 	        					}		
 								
 	        				}else if(matNetstat[j][0].equalsIgnoreCase("udp")){
 	        					
 	        					String arr[] = matNetstat[j][3].split(":");
 	        					// 0.0.0.0:* -> [0.0.0.0][*]
 	        					if(arr[arr.length-2].equalsIgnoreCase("0.0.0.0") && arr[arr.length-1].equalsIgnoreCase("*")){
 	        						ipAddress1="0.0.0.0"; portLocal="0"; 
 								}else{
 									ipAddress1=arr[arr.length-2]; portLocal=arr[arr.length-1];
 								}
 	        					 								
 	        				}else if(matNetstat[j][0].equalsIgnoreCase("tcp") || matNetstat[j][0].equalsIgnoreCase("tcp6")){
 	        					continue;
 	        				}
 	        				
 	        				oidConn = ipAddress1 + "." + portLocal;  
 	        				System.out.println("1.3.6.1.2.1.7.5.1.2."+oidConn);
 	            			if(snmpOID.toString().equalsIgnoreCase(("1.3.6.1.2.1.7.5.1.2."+oidConn))){            					
 	            				try {
 	            					SNMPVariablePair newPair = new SNMPVariablePair(new SNMPObjectIdentifier(snmpOID.toString()), new SNMPInteger(Integer.parseInt(portLocal)));
 	                                responseList.addSNMPObject(newPair);
 								} catch (Exception e2) {
 									e2.printStackTrace();
 								}	            				
 	            			}
 						}
 	        			
 	                }
 	        		catch (Exception e)
 		                {
 		                	 e.printStackTrace();
 		                }
 	        	
 	            }
             	
             }
             
             
             
         } // for
         
         System.out.println("\n");
         
         
         // return the created list of variable pairs
         return responseList;
         
     
 	}
 
 	@Override
 	public SNMPSequence processGetNextRequest(SNMPPDU requestPDU,
 			String communityName) throws SNMPGetException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
