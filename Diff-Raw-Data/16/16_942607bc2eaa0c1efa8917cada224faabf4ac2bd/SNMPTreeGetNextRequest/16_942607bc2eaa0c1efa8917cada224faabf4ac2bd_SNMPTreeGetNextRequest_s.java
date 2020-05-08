 package ucv.tesis.tesisandroid;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 public class SNMPTreeGetNextRequest<T> implements Iterable<SNMPTreeGetNextRequest<T>> {
 
 	
 	    T data;
 	    boolean tableOrEntry;
 	    SNMPTreeGetNextRequest<T> parent;
 	    SNMPTreeGetNextRequest<T> nNext;
 	    List<SNMPTreeGetNextRequest<T>> children;
 	    private List<SNMPTreeGetNextRequest<T>> elementsIndex;
 	    
 	    public SNMPTreeGetNextRequest(T data) {
 	        this.data = data;
 	        this.children = new LinkedList<SNMPTreeGetNextRequest<T>>();
 	    }
 	    public SNMPTreeGetNextRequest(SNMPTreeGetNextRequest<T> node) {
 	        this.data = node.data;
 	        this.tableOrEntry = node.tableOrEntry;
 	        this.parent = node.parent;
 	        this.nNext = node.nNext;
 	        this.children = node.children;
 	        this.elementsIndex = node.elementsIndex;	        
 	    }
 
 	   /* public SNMPTreeGetNextRequest<T> addNode(T child) {
 	    	this.tableOrEntry = false;
 	    	SNMPTreeGetNextRequest<T> childNode = new SNMPTreeGetNextRequest<T>(child);
 	        childNode.parent = this;
 	        childNode.nNext = null;
 	        if(this.children.size()>0)
 	        	this.children.get(this.children.size()-1).nNext = childNode;
 	        this.children.add(childNode);
 	        return childNode;
 	    }*/
 	    
 	    public SNMPTreeGetNextRequest<T> addNode(T child, boolean TOrE) {
 	    	this.tableOrEntry = TOrE;
 	    	SNMPTreeGetNextRequest<T> childNode = new SNMPTreeGetNextRequest<T>(child);
 	        childNode.parent = this;
 	        childNode.tableOrEntry = TOrE;
 	        childNode.nNext = null;
 	        if(this.children.size()>0)
 	        	this.children.get(this.children.size()-1).nNext = childNode;
 	        this.children.add(childNode);
 	        return childNode;
 	    }
 	    
 	    public void addLeaf(T child) {
 	    	this.tableOrEntry = true;
 	    	SNMPTreeGetNextRequest<T> childNode = new SNMPTreeGetNextRequest<T>(child);
 	        childNode.parent = this;
 	        //childNode.nNext = null;
 	        childNode.tableOrEntry = false;
 	        if(this.children.size()>0)
 	        	this.children.get(this.children.size()-1).nNext = childNode;
 	        /*
 	        if(this.nNext == null){
 	        	SNMPTreeGetNextRequest<T> node = this.parent;
 	        	if(node != null){
 		        	while(node.nNext == null && node.parent != null){
 		        		node = node.parent;
 		        	}
 		        	childNode.nNext = node.nNext;
 		        }else{
 		        	childNode.nNext = null;
 		        }
 	        }else{
 	        	childNode.nNext = this.nNext;
 	        }*/
 	        
 	        this.children.add(childNode);
 	    }
 	    public boolean isRoot() {
             return parent == null;
 	    }
 	
 	    public boolean isLeaf() {
 	            return children.size() == 0;
 	    }
 
 	    public int getLevel() {
             if (this.isRoot())
                     return 0;
             else
                     return parent.getLevel() + 1;
 	    }
 	
 	    private void registerChildForSearch(SNMPTreeGetNextRequest<T> node) {
 	            elementsIndex.add(node);
 	            if (parent != null)
 	                    parent.registerChildForSearch(node);
 	    }
 	
 	    public SNMPTreeGetNextRequest<T> findTreeNode(Comparable<T> cmp) {
 	            for (SNMPTreeGetNextRequest<T> element : this.elementsIndex) {
 	                    T elData = element.data;
 	                    if (cmp.compareTo(elData) == 0)
 	                            return element;
 	            }
 	            return null;
 	    }
 	
 	    public String GetNext(){
	    	
 	    	if(this.tableOrEntry){
 	    		if(this.children != null && this.children.size() > 0){
 	    			SNMPTreeGetNextRequest<T> node = this.children.get(0);
 	    			while(node.tableOrEntry){
 	    				if(node.children != null && node.children.size() > 0){
 	    	    			node = node.children.get(0);
 	    				}
 	    			}
 	    			return node.data.toString();
 	    		}	    			
 	    	}else{
 	    		if(this.nNext != null){
 	    			SNMPTreeGetNextRequest<T> node = this.nNext;
 	    			while(node.tableOrEntry){
 	    				if(node.children != null && node.children.size() > 0){
 	    	    			node = node.children.get(0);
 	    				}else{ // no tiene hijos pero tiene siguiente caso de los Entry del Grupo At
 	    					if(node.nNext != null){
 	    						System.out.println(node.data.toString());
 	    						node = node.nNext;
 	    					}
 	    				}
 	    			}
 	    			return node.data.toString();
 	    		}else{
 	    			return this.data.toString();
 	    		}
 	    	}
 	    	
 	    	return "";
 	    }
 	    
 	    
 	    @Override
 	    public String toString() {
 	            return data != null ? data.toString() : "[data null]";
 	    }
 	
 	    @Override
 	    public Iterator<SNMPTreeGetNextRequest<T>> iterator() {
 	    		SNMPTreeGetNextRequestIter<T> iter = new SNMPTreeGetNextRequestIter<T>(this);
 	            return iter;
 	    }
 	    
 	    public static SNMPTreeGetNextRequest<String> SNMPTreeLoad()
 	    {
 	    	SNMPTreeGetNextRequest<String> root = new SNMPTreeGetNextRequest<String>("1.3.6.1.2.1");
 	    	{
 	    		/**
 	    		 * SYSTEM
 	    		 */	    		
	    		SNMPTreeGetNextRequest<String> system = root.addNode("1.3.6.1.2.1.1.0", true);
 	    		system.addLeaf("1.3.6.1.2.1.1.1.0"); //sysDescr
 	    		system.addLeaf("1.3.6.1.2.1.1.2.0"); //sysObjectID
 	    		system.addLeaf("1.3.6.1.2.1.1.3.0"); //sysUpTime
 	    		system.addLeaf("1.3.6.1.2.1.1.4.0"); //sysContact
 	    		system.addLeaf("1.3.6.1.2.1.1.5.0"); //sysName
 	    		system.addLeaf("1.3.6.1.2.1.1.6.0"); //sysLocation
 	    		system.addLeaf("1.3.6.1.2.1.1.7.0"); //sysServices
 	    		
 	    		/**
 	    		 * INTERFACES
 	    		 */
 	    		SNMPTreeGetNextRequest<String> interfaces = root.addNode("1.3.6.1.2.1.2", true);
 	    		interfaces.addLeaf("1.3.6.1.2.1.2.1.0"); //ifNumber
 	    		SNMPTreeGetNextRequest<String> iftable = interfaces.addNode("1.3.6.1.2.1.2.2", true); //ifTable
 	    		{
     				int nIf = 0; // numero de interface
                     Process p=null;
                     try {
 						p = Runtime.getRuntime().exec("ls /sys/class/net/");
 						BufferedReader in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 						String line;
 				        while ((line = in2.readLine()) != null) {  
 				        	nIf += 1; 						        	
 				        }
                     }catch(Exception e){
                     	e.printStackTrace();
                     }
                     
 	    			SNMPTreeGetNextRequest<String> ifEntry = iftable.addNode("1.3.6.1.2.1.2.2.1", true); // ifEntry
 	    			{
 	    				/**ifIndex*/
 						SNMPTreeGetNextRequest<String> ifIndex = ifEntry.addNode("1.3.6.1.2.1.2.2.1.1", true);
 	    				for(int iface = 1 ; iface <= nIf ; iface++ ){
 							ifIndex.addLeaf("1.3.6.1.2.1.2.2.1.1."+iface);
 						}
 						/**ifDescr*/
 						SNMPTreeGetNextRequest<String> ifDescr = ifEntry.addNode("1.3.6.1.2.1.2.2.1.2", true);
 	    				for(int iface = 1 ; iface <= nIf ; iface++ ){
 							ifDescr.addLeaf("1.3.6.1.2.1.2.2.1.2."+iface);
 						}
 	    				/**ifType*/
 	    				SNMPTreeGetNextRequest<String> ifType = ifEntry.addNode("1.3.6.1.2.1.2.2.1.3", true);
 	    				for(int iface = 1 ; iface <= nIf ; iface++ ){
 	    					ifType.addLeaf("1.3.6.1.2.1.2.2.1.3."+iface);
 						}
 	    				/**ifMtu*/
 	    				SNMPTreeGetNextRequest<String> ifMtu = ifEntry.addNode("1.3.6.1.2.1.2.2.1.4", true);
 	    				for(int iface = 1 ; iface <= nIf ; iface++ ){
 	    					ifMtu.addLeaf("1.3.6.1.2.1.2.2.1.4."+iface);
 						}
 	    				/**ifSpeed*/
 	    				SNMPTreeGetNextRequest<String> ifSpeed = ifEntry.addNode("1.3.6.1.2.1.2.2.1.5", true);
 	    				for(int iface = 1 ; iface <= nIf ; iface++ ){
 	    					ifSpeed.addLeaf("1.3.6.1.2.1.2.2.1.5."+iface);
 	    				} 
 	    				/**ifPhysAddress*/
 	    				SNMPTreeGetNextRequest<String> ifPhysAddress = ifEntry.addNode("1.3.6.1.2.1.2.2.1.6", true);
 	    				for(int iface = 1 ; iface <= nIf ; iface++ ){
 	    					ifPhysAddress.addLeaf("1.3.6.1.2.1.2.2.1.6."+iface);
 	    				}
 	    				/**ifAdminStatus*/
 	    				SNMPTreeGetNextRequest<String> ifAdminStatus = ifEntry.addNode("1.3.6.1.2.1.2.2.1.7", true);
 	    				for(int iface = 1 ; iface <= nIf ; iface++ ){
 	    					ifAdminStatus.addLeaf("1.3.6.1.2.1.2.2.1.7."+iface);
 	    				} 
 	    				/**ifOperStatus*/
 	    				SNMPTreeGetNextRequest<String> ifOperStatus = ifEntry.addNode("1.3.6.1.2.1.2.2.1.8", true);
 	    				for(int iface = 1 ; iface <= nIf ; iface++ ){
 	    					ifOperStatus.addLeaf("1.3.6.1.2.1.2.2.1.8."+iface);
 	    				} 
 	    				/**ifLastChange*/
 	    				SNMPTreeGetNextRequest<String> ifLastChange = ifEntry.addNode("1.3.6.1.2.1.2.2.1.9", true);
 	    				for(int iface = 1 ; iface <= nIf ; iface++ ){
 	    					ifLastChange.addLeaf("1.3.6.1.2.1.2.2.1.9."+iface);
 	    				}
 	    				/**ifInOctets*/
 	    				SNMPTreeGetNextRequest<String> ifInOctets = ifEntry.addNode("1.3.6.1.2.1.2.2.1.10", true);
 	    				for(int iface = 1 ; iface <= nIf ; iface++ ){
 	    					ifInOctets.addLeaf("1.3.6.1.2.1.2.2.1.10."+iface);
 	    				}
 	    				/**ifInUcastPkts*/
 	    				SNMPTreeGetNextRequest<String> ifInUcastPkts = ifEntry.addNode("1.3.6.1.2.1.2.2.1.11", true);
 	    				for(int iface = 1 ; iface <= nIf ; iface++ ){
 	    					ifInUcastPkts.addLeaf("1.3.6.1.2.1.2.2.1.11."+iface);
 	    				}
 	    				/**ifInNUcastPkts*/
 	    				SNMPTreeGetNextRequest<String> ifInNUcastPkts = ifEntry.addNode("1.3.6.1.2.1.2.2.1.12", true);
 	    				for(int iface = 1 ; iface <= nIf ; iface++ ){
 	    					ifInNUcastPkts.addLeaf("1.3.6.1.2.1.2.2.1.12."+iface);
 	    				} 
 	    				/**ifInDiscards*/
 	    				SNMPTreeGetNextRequest<String> ifInDiscards = ifEntry.addNode("1.3.6.1.2.1.2.2.1.13", true);
 	    				for(int iface = 1 ; iface <= nIf ; iface++ ){
 	    					ifInDiscards.addLeaf("1.3.6.1.2.1.2.2.1.13."+iface);
 	    				}
 	    				/**ifInErrors*/
 	    				SNMPTreeGetNextRequest<String> ifInErrors = ifEntry.addNode("1.3.6.1.2.1.2.2.1.14", true);
 	    				for(int iface = 1 ; iface <= nIf ; iface++ ){
 	    					ifInErrors.addLeaf("1.3.6.1.2.1.2.2.1.14."+iface);
 	    				}
 	    				/**ifInUnknownProtos*/
 	    				SNMPTreeGetNextRequest<String> ifInUnknownProtos = ifEntry.addNode("1.3.6.1.2.1.2.2.1.15", true);
 	    				for(int iface = 1 ; iface <= nIf ; iface++ ){
 	    					ifInUnknownProtos.addLeaf("1.3.6.1.2.1.2.2.1.15."+iface);
 	    				}
 	    				/**ifOutOctets*/
 	    				SNMPTreeGetNextRequest<String> ifOutOctets = ifEntry.addNode("1.3.6.1.2.1.2.2.1.16", true);
 	    				for(int iface = 1 ; iface <= nIf ; iface++ ){
 	    					ifOutOctets.addLeaf("1.3.6.1.2.1.2.2.1.16."+iface);
 	    				}
 	    				/**ifOutUcastPkts*/
 	    				SNMPTreeGetNextRequest<String> ifOutUcastPkts = ifEntry.addNode("1.3.6.1.2.1.2.2.1.17", true);
 	    				for(int iface = 1 ; iface <= nIf ; iface++ ){
 	    					ifOutUcastPkts.addLeaf("1.3.6.1.2.1.2.2.1.17."+iface);
 	    				}
 	    				/**ifOutNUcastPkts*/
 	    				SNMPTreeGetNextRequest<String> ifOutNUcastPkts = ifEntry.addNode("1.3.6.1.2.1.2.2.1.18", true);
 	    				for(int iface = 1 ; iface <= nIf ; iface++ ){
 	    					ifOutNUcastPkts.addLeaf("1.3.6.1.2.1.2.2.1.18."+iface);
 	    				}
 	    				/**ifOutDiscards*/
 	    				SNMPTreeGetNextRequest<String> ifOutDiscards = ifEntry.addNode("1.3.6.1.2.1.2.2.1.19", true);
 	    				for(int iface = 1 ; iface <= nIf ; iface++ ){
 	    					ifOutDiscards.addLeaf("1.3.6.1.2.1.2.2.1.19."+iface);
 	    				} 
 	    				/**ifOutErrors*/
 	    				SNMPTreeGetNextRequest<String> ifOutErrors = ifEntry.addNode("1.3.6.1.2.1.2.2.1.20", true);
 	    				for(int iface = 1 ; iface <= nIf ; iface++ ){
 	    					ifOutErrors.addLeaf("1.3.6.1.2.1.2.2.1.20."+iface);
 	    				} 
 	    				/**ifOutQLen*/
 	    				SNMPTreeGetNextRequest<String> ifOutQLen = ifEntry.addNode("1.3.6.1.2.1.2.2.1.21", true);
 	    				for(int iface = 1 ; iface <= nIf ; iface++ ){
 	    					ifOutQLen.addLeaf("1.3.6.1.2.1.2.2.1.21."+iface);
 	    				}
 	    				/**ifSpecific*/
 	    				SNMPTreeGetNextRequest<String> ifSpecific = ifEntry.addNode("1.3.6.1.2.1.2.2.1.22", true);
 	    				for(int iface = 1 ; iface <= nIf ; iface++ ){
 	    					ifSpecific.addLeaf("1.3.6.1.2.1.2.2.1.22."+iface);
 	    				}
 	    			}// ifEntry	    			
 	    			
 	    		}//ifTable
 	    		
 	    		/**
 	    		 * AT
 	    		 */
 	    		SNMPTreeGetNextRequest<String> at = root.addNode("1.3.6.1.2.1.3", true);
 	    		SNMPTreeGetNextRequest<String> atTable = at.addNode("1.3.6.1.2.1.3.1", true); //atTable
 	    		{
 	    			SNMPTreeGetNextRequest<String> atEntry = atTable.addNode("1.3.6.1.2.1.3.1.1", true); //atEntry
 	    			{
 	    				SNMPTreeGetNextRequest<String> atIfIndex = atEntry.addNode("1.3.6.1.2.1.3.1.1.1", true); //atIfIndex
 	    				SNMPTreeGetNextRequest<String> atPhysAddress = atEntry.addNode("1.3.6.1.2.1.3.1.1.2", true); //atPhysAddress
 	    				SNMPTreeGetNextRequest<String> atNetAddress = atEntry.addNode("1.3.6.1.2.1.3.1.1.3", true); //atNetAddress
 	    			}
 	    		}
 	    		
 	    		/**
 	    		 * IP
 	    		 */
 	    		SNMPTreeGetNextRequest<String> ip = root.addNode("1.3.6.1.2.1.4.0", true);
 	    		{
 	    			ip.addLeaf("1.3.6.1.2.1.4.1.0");// ipForwarding
 	    			ip.addLeaf("1.3.6.1.2.1.4.2.0");// ipDefaultTTL
 					ip.addLeaf("1.3.6.1.2.1.4.3.0");// ipInReceives
 					ip.addLeaf("1.3.6.1.2.1.4.4.0");// ipInHdrErrors
 					ip.addLeaf("1.3.6.1.2.1.4.5.0");// ipInAddrErrors
 					ip.addLeaf("1.3.6.1.2.1.4.6.0");// ipForwDatagrams
 					ip.addLeaf("1.3.6.1.2.1.4.7.0");// ipInUnknownProtos
 					ip.addLeaf("1.3.6.1.2.1.4.8.0");// ipInDiscards
 					ip.addLeaf("1.3.6.1.2.1.4.9.0");// ipInDelivers
 					ip.addLeaf("1.3.6.1.2.1.4.10.0");// ipOutRequests
 					ip.addLeaf("1.3.6.1.2.1.4.11.0");// ipOutDiscards
 					ip.addLeaf("1.3.6.1.2.1.4.12.0");// ipOutNoRoutes
 					ip.addLeaf("1.3.6.1.2.1.4.13.0");// ipReasmTimeout
 					ip.addLeaf("1.3.6.1.2.1.4.14.0");// ipReasmReqds
 					ip.addLeaf("1.3.6.1.2.1.4.15.0");// ipReasmOKs
 					ip.addLeaf("1.3.6.1.2.1.4.16.0");// ipReasmFails
 					ip.addLeaf("1.3.6.1.2.1.4.17.0");// ipFragOKs
 					ip.addLeaf("1.3.6.1.2.1.4.18.0");// ipFragFails
 					ip.addLeaf("1.3.6.1.2.1.4.19.0");// ipFragCreates
 	    			
 					SNMPTreeGetNextRequest<String> ipAddrTable = ip.addNode("1.3.6.1.2.1.4.20", true); // ipAddrTable
 					
 					SNMPTreeGetNextRequest<String> ipAddrEntry = ipAddrTable.addNode("1.3.6.1.2.1.4.20.1", true); //ipAddrEntry
 					{
 						String cmd="ip address show"+"\n";
 		            	String line = "";
 		            	Process p=null;
 		            	ArrayList<String> lines = new ArrayList<String>();
 		        		BufferedReader in2=null;
 		        		ArrayList<String> table = new ArrayList<String>();
 		            	try
 		                {
 		            		p = Runtime.getRuntime().exec(cmd);
 		        			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 		        			
 		        			while ((line =in2.readLine()) != null) {
 		        				lines.add(line);
 		        			}
 		        			
 		        			int x = 0;
 		        			String cadena = "";
 		        			cadena = lines.get(x);
 		        			
 		        			while ( x < lines.size()) {
 
 		        				if(cadena.contains("mtu") && cadena.contains("qdisc") && cadena.contains("state") ){
 
 		        					String arrTmp[] = cadena.split(" ");
 		        					do{
 		        						if(!cadena.contains("inet6") && cadena.contains("inet")){
 		                					arrTmp = cadena.split(" ");
 		                					String arrTmp1[] = arrTmp[5].split("/");
 		                					String ipAddr = arrTmp1[0];
 		                					table.add(ipAddr);
 		        						}
 		        						x++;
 		        						if (x >= lines.size()) break; 
 		        						cadena = lines.get(x);
 		        					}while(!(cadena.contains("mtu") && cadena.contains("qdisc") && cadena.contains("state")));
 		        				} // if
 		        			} // while ( x < lines.size()) 
 		                }catch(Exception e){
 		                	e.printStackTrace();
 		                }
 		            	
 						/** ipAdEntAddr*/
 		            	SNMPTreeGetNextRequest<String> ipAdEntAddr = ipAddrEntry.addNode("1.3.6.1.2.1.4.20.1.1", true);
 		            	for (Iterator<String> iterator = table.iterator(); iterator.hasNext();) {
 							String str = (String) iterator.next();
 							ipAdEntAddr.addLeaf("1.3.6.1.2.1.4.20.1.1."+str);
 						}
 		            	/** ipAdEntIfIndex*/
 		            	SNMPTreeGetNextRequest<String> ipAdEntIfIndex = ipAddrEntry.addNode("1.3.6.1.2.1.4.20.1.2", true);
 		            	for (Iterator<String> iterator = table.iterator(); iterator.hasNext();) {
 							String str = (String) iterator.next();
 							ipAdEntIfIndex.addLeaf("1.3.6.1.2.1.4.20.1.2."+str);
 						}
 		            	/** ipAdEntNetMask*/
 		            	SNMPTreeGetNextRequest<String> ipAdEntNetMask = ipAddrEntry.addNode("1.3.6.1.2.1.4.20.1.3", true);
 		            	for (Iterator<String> iterator = table.iterator(); iterator.hasNext();) {
 							String str = (String) iterator.next();
 							ipAdEntNetMask.addLeaf("1.3.6.1.2.1.4.20.1.3."+str);
 						}
 		            	/** ipAdEntBcastAddr*/
 		            	SNMPTreeGetNextRequest<String> ipAdEntBcastAddr = ipAddrEntry.addNode("1.3.6.1.2.1.4.20.1.4", true);
 		            	for (Iterator<String> iterator = table.iterator(); iterator.hasNext();) {
 							String str = (String) iterator.next();
 							ipAdEntBcastAddr.addLeaf("1.3.6.1.2.1.4.20.1.4."+str);
 						}
 		            	/** ipAdEntReasmMaxSize*/
 		            	SNMPTreeGetNextRequest<String> ipAdEntReasmMaxSize = ipAddrEntry.addNode("1.3.6.1.2.1.4.20.1.5", true);
 		            	for (Iterator<String> iterator = table.iterator(); iterator.hasNext();) {
 							String str = (String) iterator.next();
 							ipAdEntReasmMaxSize.addLeaf("1.3.6.1.2.1.4.20.1.5."+str);
 						}
 	            	
 					}
 					
 					SNMPTreeGetNextRequest<String> ipRouteTable = ip.addNode("1.3.6.1.2.1.4.21", true); // ipRouteTable
 					{
 						SNMPTreeGetNextRequest<String> ipRouteEntry = ipRouteTable.addNode("1.3.6.1.2.1.4.21.1", true); // ipRouteEntry
 						{
 							String cmd="cat /proc/net/route\n";
 		                	String line = "";
 		                	Process p=null;
 		                	ArrayList<String> lines = new ArrayList<String>();
 		            		BufferedReader in2=null;
 		            		ArrayList<String> table = new ArrayList<String>();
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
 									String ipAddr = "";
 								
 									for(int i1 = hexValue.length(); i1 > 0; i1 = i1 - 2) {
 										ipAddr = ipAddr + Integer.valueOf(hexValue.substring(i1-2, i1 ), 16) + ".";
 									}
 									ipAddr= (ipAddr.substring(0, ipAddr.length()-1));
 									table.add(ipAddr);
 		            			}
 		                    }catch(Exception e){
 		                    	e.printStackTrace();
 		                    }
 		                	
 		                	/**ipRouteDest*/
 			            	SNMPTreeGetNextRequest<String> ipRouteDest = ipRouteEntry.addNode("1.3.6.1.2.1.4.21.1.1", true);
 			            	for (Iterator<String> iterator = table.iterator(); iterator.hasNext();) {
 								String str = (String) iterator.next();
 								ipRouteDest.addLeaf("1.3.6.1.2.1.4.21.1.1."+str);
 							}
 			            	/**ipRouteIfIndex*/
 			            	SNMPTreeGetNextRequest<String> ipRouteIfIndex = ipRouteEntry.addNode("1.3.6.1.2.1.4.21.1.2", true);
 			            	for (Iterator<String> iterator = table.iterator(); iterator.hasNext();) {
 								String str = (String) iterator.next();
 								ipRouteIfIndex.addLeaf("1.3.6.1.2.1.4.21.1.2."+str);
 							}
 			            	/**ipRouteMetric1*/
 			            	SNMPTreeGetNextRequest<String> ipRouteMetric1 = ipRouteEntry.addNode("1.3.6.1.2.1.4.21.1.3", true);
 			            	for (Iterator<String> iterator = table.iterator(); iterator.hasNext();) {
 								String str = (String) iterator.next();
 								ipRouteMetric1.addLeaf("1.3.6.1.2.1.4.21.1.3."+str);
 							}
 			            	/**ipRouteNextHop*/
 			            	SNMPTreeGetNextRequest<String> ipRouteNextHop = ipRouteEntry.addNode("1.3.6.1.2.1.4.21.1.4", true);
 			            	for (Iterator<String> iterator = table.iterator(); iterator.hasNext();) {
 								String str = (String) iterator.next();
 								ipRouteNextHop.addLeaf("1.3.6.1.2.1.4.21.1.4."+str);
 							}
 			            	/**ipRouteMask*/
 			            	SNMPTreeGetNextRequest<String> ipRouteMask = ipRouteEntry.addNode("1.3.6.1.2.1.4.21.1.5", true);
 			            	for (Iterator<String> iterator = table.iterator(); iterator.hasNext();) {
 								String str = (String) iterator.next();
 								ipRouteMask.addLeaf("1.3.6.1.2.1.4.21.1.5."+str);
 							}
 						}
 					}
 					
 					SNMPTreeGetNextRequest<String> ipNetToMediaTable = ip.addNode("1.3.6.1.2.1.4.22", true); // ipNetToMediaTable
 					
 					SNMPTreeGetNextRequest<String> ipNetToMediaEntry = ipNetToMediaTable.addNode("1.3.6.1.2.1.4.22.1", true); // ipNetToMediaTable
 					{
 						String cmd="ip address show"+"\n";
 		            	String line = "";
 		            	Process p=null;
 		            	ArrayList<String> lines = new ArrayList<String>();
 		        		BufferedReader in2=null;
 		        		ArrayList<String> table = new ArrayList<String>();
 		            	try
 		                {
 		            		p = Runtime.getRuntime().exec(cmd);
 		        			in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
 		        			
 		        			while ((line =in2.readLine()) != null) {
 		        				lines.add(line);
 		        			}
 		        			
 		        			int x = 0;
 		        			String cadena = "";
 		        			cadena = lines.get(x);
 		        			
 		        			while ( x < lines.size()) {
 
 		        				if(cadena.contains("mtu") && cadena.contains("qdisc") && cadena.contains("state") ){
 
 		        					String arrTmp[] = cadena.split(" ");
 		        					do{
 		        						if(!cadena.contains("inet6") && cadena.contains("inet")){
 		                					arrTmp = cadena.split(" ");
 		                					String arrTmp1[] = arrTmp[5].split("/");
 		                					String ipAddr = arrTmp1[0];
 		                					table.add(ipAddr);
 		        						}
 		        						x++;
 		        						if (x >= lines.size()) break; 
 		        						cadena = lines.get(x);
 		        					}while(!(cadena.contains("mtu") && cadena.contains("qdisc") && cadena.contains("state")));
 		        				} // if
 		        			} // while ( x < lines.size()) 
 		                }catch(Exception e){
 		                	e.printStackTrace();
 		                }
 		            	
 		            	/**ipNetToMediaIfIndex*/
 		            	SNMPTreeGetNextRequest<String> ipNetToMediaIfIndex = ipNetToMediaEntry.addNode("1.3.6.1.2.1.4.22.1.1", true);
 		            	for (Iterator<String> iterator = table.iterator(); iterator.hasNext();) {
 							String str = (String) iterator.next();
 							ipNetToMediaIfIndex.addLeaf("1.3.6.1.2.1.4.22.1.1."+str);
 						}
 		            	/**ipNetToMediaPhysAddress*/
 		            	SNMPTreeGetNextRequest<String> ipNetToMediaPhysAddress = ipNetToMediaEntry.addNode("1.3.6.1.2.1.4.22.1.2", true);
 		            	for (Iterator<String> iterator = table.iterator(); iterator.hasNext();) {
 							String str = (String) iterator.next();
 							ipNetToMediaPhysAddress.addLeaf("1.3.6.1.2.1.4.22.1.2."+str);
 						}
 		            	/**ipNetToMediaNetAddress*/
 		            	SNMPTreeGetNextRequest<String> ipNetToMediaNetAddress = ipNetToMediaEntry.addNode("1.3.6.1.2.1.4.22.1.3", true);
 		            	for (Iterator<String> iterator = table.iterator(); iterator.hasNext();) {
 							String str = (String) iterator.next();
 							ipNetToMediaNetAddress.addLeaf("1.3.6.1.2.1.4.22.1.3."+str);
 						}
 		            	/**ipNetToMediaType*/
 		            	SNMPTreeGetNextRequest<String> ipNetToMediaType = ipNetToMediaEntry.addNode("1.3.6.1.2.1.4.22.1.4", true);
 		            	for (Iterator<String> iterator = table.iterator(); iterator.hasNext();) {
 							String str = (String) iterator.next();
 							ipNetToMediaType.addLeaf("1.3.6.1.2.1.4.22.1.4."+str);
 						}
 						
 					}
 					
 	    			ip.addLeaf("1.3.6.1.2.1.4.23.0");// ipRoutingDiscards
 	    			
 	    		} // ip
 	    		
 	    		/**
 	    		 * ICMP
 	    		 */
 	    		SNMPTreeGetNextRequest<String> icmp = root.addNode("1.3.6.1.2.1.5", true);
 	    		{
 	    			icmp.addLeaf("1.3.6.1.2.1.5.1.0");// icmpInMsgs
 	    			icmp.addLeaf("1.3.6.1.2.1.5.2.0");// icmpInErrors
 					icmp.addLeaf("1.3.6.1.2.1.5.3.0");// icmpInDestUnreachs
 					icmp.addLeaf("1.3.6.1.2.1.5.4.0");// icmpInTimeExcds
 					icmp.addLeaf("1.3.6.1.2.1.5.5.0");// icmpInParmProbs
 					icmp.addLeaf("1.3.6.1.2.1.5.6.0");// icmpInSrcQuenchs
 					icmp.addLeaf("1.3.6.1.2.1.5.7.0");// icmpInRedirects
 					icmp.addLeaf("1.3.6.1.2.1.5.8.0");// icmpInEchos
 					icmp.addLeaf("1.3.6.1.2.1.5.9.0");// icmpInEchoReps
 					icmp.addLeaf("1.3.6.1.2.1.5.10.0");// icmpInTimestamps
 					icmp.addLeaf("1.3.6.1.2.1.5.11.0");// icmpInTimestampReps
 					icmp.addLeaf("1.3.6.1.2.1.5.12.0");// icmpInAddrMasks
 					icmp.addLeaf("1.3.6.1.2.1.5.13.0");// icmpInAddrMaskReps
 					icmp.addLeaf("1.3.6.1.2.1.5.14.0");// icmpOutMsgs
 					icmp.addLeaf("1.3.6.1.2.1.5.15.0");// icmpOutErrors
 					icmp.addLeaf("1.3.6.1.2.1.5.16.0");// icmpOutDestUnreachs
 					icmp.addLeaf("1.3.6.1.2.1.5.17.0");// icmpOutTimeExcds
 					icmp.addLeaf("1.3.6.1.2.1.5.18.0");// icmpOutParmProbs
 					icmp.addLeaf("1.3.6.1.2.1.5.19.0");// icmpOutSrcQuenchs
 					icmp.addLeaf("1.3.6.1.2.1.5.20.0");// icmpOutRedirects
 					icmp.addLeaf("1.3.6.1.2.1.5.21.0");// icmpOutEchos
 					icmp.addLeaf("1.3.6.1.2.1.5.22.0");// icmpOutEchoReps
 					icmp.addLeaf("1.3.6.1.2.1.5.23.0");// icmpOutTimestamps
 					icmp.addLeaf("1.3.6.1.2.1.5.24.0");// icmpOutTimestampReps
 					icmp.addLeaf("1.3.6.1.2.1.5.25.0");// icmpOutAddrMasks
 					icmp.addLeaf("1.3.6.1.2.1.5.26.0");// icmpOutAddrMaskReps
 	    		}
 	    	
 	    		/**
 	    		 * TCP
 	    		 */
 	    		SNMPTreeGetNextRequest<String> tcp = root.addNode("1.3.6.1.2.1.6", true);
 	    		{
 	    			tcp.addLeaf("1.3.6.1.2.1.6.1.0");// tcpRtoAlgorithm
 	    			tcp.addLeaf("1.3.6.1.2.1.6.2.0");// tcpRtoMin
 	    			tcp.addLeaf("1.3.6.1.2.1.6.3.0");// tcpRtoMax
 	    			tcp.addLeaf("1.3.6.1.2.1.6.4.0");// tcpMaxConn
 					tcp.addLeaf("1.3.6.1.2.1.6.5.0");// tcpActiveOpens
 	    			tcp.addLeaf("1.3.6.1.2.1.6.6.0");// tcpPassiveOpens
 					tcp.addLeaf("1.3.6.1.2.1.6.7.0");// tcpAttemptFails
 	    			tcp.addLeaf("1.3.6.1.2.1.6.8.0");// tcpEstabResets
 	    			tcp.addLeaf("1.3.6.1.2.1.6.9.0");// tcpCurrEstab
 	    			tcp.addLeaf("1.3.6.1.2.1.6.10.0");// tcpInSegs
 	    			tcp.addLeaf("1.3.6.1.2.1.6.11.0");// tcpOutSegs
 	    			tcp.addLeaf("1.3.6.1.2.1.6.12.0");// tcpRetransSegs
 	    			
 	    			SNMPTreeGetNextRequest<String> tcpConnTable = tcp.addNode("1.3.6.1.2.1.6.13", true); // tcpConnTable
 	    			
 	    			SNMPTreeGetNextRequest<String> tcpConnEntry = tcpConnTable.addNode("1.3.6.1.2.1.6.13.1", true);
 	    			{
 	            		String cmd="netstat\n", line, oidConn="";
 	            		Process p=null;
 	                	ArrayList<String> lines = new ArrayList<String>();
 	            		BufferedReader in2=null;
 	            		int stateConn=0;
 	            		ArrayList<String> table = new ArrayList<String>();
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
 	            				} // endif
 	            				
 		            			table.add(oidConn);
 		            			
 							} // for
 	            			
 	            			java.util.Collections.sort(table);	            			
 	                    }
 	            		catch (Exception e)
 	   	                {
 	   	                	 e.printStackTrace();
 	   	                }
 	                	
 	                	/**tcpConnState*/
 		            	SNMPTreeGetNextRequest<String> tcpConnState = tcpConnEntry.addNode("1.3.6.1.2.1.6.13.1.1", true);
 		            	for (Iterator<String> iterator = table.iterator(); iterator.hasNext();) {
 							String str = (String) iterator.next();
 							tcpConnState.addLeaf("1.3.6.1.2.1.6.13.1.1."+str);
 						}
 		            	/**tcpConnLocalAddress*/
 		            	SNMPTreeGetNextRequest<String> tcpConnLocalAddress = tcpConnEntry.addNode("1.3.6.1.2.1.6.13.1.2", true);
 		            	for (Iterator<String> iterator = table.iterator(); iterator.hasNext();) {
 							String str = (String) iterator.next();
 							tcpConnLocalAddress.addLeaf("1.3.6.1.2.1.6.13.1.2."+str);
 						}
 		            	/**tcpConnLocalPort*/
 		            	SNMPTreeGetNextRequest<String> tcpConnLocalPort = tcpConnEntry.addNode("1.3.6.1.2.1.6.13.1.3", true);
 		            	for (Iterator<String> iterator = table.iterator(); iterator.hasNext();) {
 							String str = (String) iterator.next();
 							tcpConnLocalPort.addLeaf("1.3.6.1.2.1.6.13.1.3."+str);
 						}
 		            	/**tcpConnRemAddress*/
 		            	SNMPTreeGetNextRequest<String> tcpConnRemAddress = tcpConnEntry.addNode("1.3.6.1.2.1.6.13.1.4", true);
 		            	for (Iterator<String> iterator = table.iterator(); iterator.hasNext();) {
 							String str = (String) iterator.next();
 							tcpConnRemAddress.addLeaf("1.3.6.1.2.1.6.13.1.4."+str);
 						}
 		            	/**tcpConnRemPort*/
 		            	SNMPTreeGetNextRequest<String> tcpConnRemPort = tcpConnEntry.addNode("1.3.6.1.2.1.6.13.1.5", true);
 		            	for (Iterator<String> iterator = table.iterator(); iterator.hasNext();) {
 							String str = (String) iterator.next();
 							tcpConnRemPort.addLeaf("1.3.6.1.2.1.6.13.1.5."+str);
 						}
 	    				
 	    			}
 	    			
 	    			tcp.addLeaf("1.3.6.1.2.1.6.14.0");// tcpInErrs
 	    			tcp.addLeaf("1.3.6.1.2.1.6.15.0");// tcpOutRsts
 	    		}
 	    	
 	    		/**
 	    		 * UDP
 	    		 */
 	    		SNMPTreeGetNextRequest<String> udp = root.addNode("1.3.6.1.2.1.7", true);
 	    		{
 	    			udp.addLeaf("1.3.6.1.2.1.7.1.0");// udpInDatagrams
 					udp.addLeaf("1.3.6.1.2.1.7.2.0");// udpNoPorts
 					udp.addLeaf("1.3.6.1.2.1.7.3.0");// udpInErrors
 					udp.addLeaf("1.3.6.1.2.1.7.4.0");// udpOutDatagrams
 					SNMPTreeGetNextRequest<String> udpTable = udp.addNode("1.3.6.1.2.1.7.5", true);// udpTable
 					
 					SNMPTreeGetNextRequest<String> udpEntry = udpTable.addNode("1.3.6.1.2.1.7.5.1", true);
 					{
 				  		String cmd="netstat\n", line, oidConn = "";
 		        		String ipAddress1="", portLocal="";
 		        		Process p=null;
 		            	ArrayList<String> lines = new ArrayList<String>();
 		        		BufferedReader in2=null;
 		        		ArrayList<String> table = new ArrayList<String>();
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
 		        			
 		        				table.add(oidConn);
 							}
 		        			
 		                }
 		        		catch (Exception e)
 		                {
 		                	 e.printStackTrace();
 		                }
 		            	
 		            	/**udpLocalAddress*/
 		            	SNMPTreeGetNextRequest<String> udpLocalAddress = udpEntry.addNode("1.3.6.1.2.1.7.5.1.1", true);
 		            	for (Iterator<String> iterator = table.iterator(); iterator.hasNext();) {
 							String str = (String) iterator.next();
 							udpLocalAddress.addLeaf("1.3.6.1.2.1.7.5.1.1."+str);
 						}
 		            	/**udpLocalPort*/
 		            	SNMPTreeGetNextRequest<String> udpLocalPort = udpEntry.addNode("1.3.6.1.2.1.7.5.1.2", true);
 		            	for (Iterator<String> iterator = table.iterator(); iterator.hasNext();) {
 							String str = (String) iterator.next();
 							udpLocalPort.addLeaf("1.3.6.1.2.1.7.5.1.2."+str);
 						}
 		            	
 					}//udpEntry
 					
 					/**
 		    		 * SNMP
 		    		 */
		    		SNMPTreeGetNextRequest<String> snmp = root.addNode("1.3.6.1.2.1.11", false);
 		    		
 	    		}
 	    		
 	    	} // root
 	    	
 	    	for (SNMPTreeGetNextRequest<String> node : root) {
                 //String indent = createIndent(node.getLevel());
                 if(node.nNext == null){
                 	SNMPTreeGetNextRequest<String> padre = node.parent;
                 	if(padre != null){
 	                    if(padre.nNext == null && padre.parent != null){
 	                    	while( padre != null && padre.nNext == null){
 	                    		padre = padre.parent;
 	                    	}
 	                    	if(padre != null){	          
 	                    		node.nNext = padre.nNext;
 	                    	}
 	                    }else{
 	                    	node.nNext = padre.nNext;	                    	
 	                    }
                 	}
                 }
                 //System.out.println(indent + node.data + " | " + node.tableOrEntry + " | " + node.nNext);                
 	    	}
 	    	
 	    	return root;	    	
 	    	
 	    }
 	    
 		private static String createIndent(int depth) {
 	        StringBuilder sb = new StringBuilder();
 	        for (int i = 0; i < depth; i++) {
 	                sb.append("_|");
 	        }
 	        return sb.toString();
 		}
 	    
 }
