 package com.telefonica.claudia.smi.provisioning;
 
 
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.SortedSet;
 import java.util.StringTokenizer;
 import java.util.TreeSet;
 
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.FactoryConfigurationError;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.rpc.ServiceException;
 import javax.xml.ws.Response;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.apache.xmlrpc.client.XmlRpcClient;
 import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
 import org.dmtf.schemas.ovf.envelope._1.ContentType;
 import org.dmtf.schemas.ovf.envelope._1.DiskSectionType;
 import org.dmtf.schemas.ovf.envelope._1.EnvelopeType;
 import org.dmtf.schemas.ovf.envelope._1.FileType;
 import org.dmtf.schemas.ovf.envelope._1.NetworkSectionType;
 import org.dmtf.schemas.ovf.envelope._1.OperatingSystemSectionType;
 import org.dmtf.schemas.ovf.envelope._1.RASDType;
 import org.dmtf.schemas.ovf.envelope._1.ReferencesType;
 import org.dmtf.schemas.ovf.envelope._1.VirtualDiskDescType;
 import org.dmtf.schemas.ovf.envelope._1.VirtualHardwareSectionType;
 import org.dmtf.schemas.ovf.envelope._1.VirtualSystemCollectionType;
 import org.dmtf.schemas.ovf.envelope._1.VirtualSystemType;
 import org.dmtf.schemas.ovf.envelope._1.NetworkSectionType.Network;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 import com.abiquo.ovf.OVFEnvelopeUtils;
 import com.abiquo.ovf.exceptions.EmptyEnvelopeException;
 import com.abiquo.ovf.exceptions.InvalidSectionException;
 import com.abiquo.ovf.exceptions.SectionNotPresentException;
 import com.abiquo.ovf.xml.OVFSerializer;
 import com.flexiant.extility.*;
 /*import com.flexiscale.api2.FlexiScaleServiceLocator;
 import com.flexiscale.api2.FlexiScaleSoapBindingStub;
 import com.flexiscale.api2.ProductComponent;
 import com.flexiscale.api2.ProductOffer;
 import com.flexiscale.api2.RuntimeMetadata;
 import com.flexiscale.api2.Server;
 import com.flexiscale.api2.ServerMetadata;
 import com.flexiscale.api2.Subnet;
 import com.flexiscale.api2.VDC;
 import com.flexiscale.api2.Vlan;*/
 import com.sun.org.apache.xml.internal.serialize.OutputFormat;
 import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
 
 import com.telefonica.claudia.smi.DataTypesUtils;
 import com.telefonica.claudia.smi.Main;
 import com.telefonica.claudia.smi.TCloudConstants;
 import com.telefonica.claudia.smi.TCloudException;
 import com.telefonica.claudia.smi.URICreation;
 //import com.telefonica.claudia.smi.provisioning.ONEProvisioningDriver.ControlActionType;
 //import com.telefonica.claudia.smi.provisioning.ONEProvisioningDriver.DeployNetworkTask;
 import com.telefonica.claudia.smi.provisioning.GetOperationsUtils;
 import com.telefonica.claudia.smi.provisioning.ProvisioningDriver;
 import com.telefonica.claudia.smi.task.Task;
 import com.telefonica.claudia.smi.task.TaskManager;
 import com.telefonica.claudia.smi.task.Task.TaskError;
 import com.telefonica.claudia.smi.task.Task.TaskStatus;
 
 public class FlexiscaleDriver implements ProvisioningDriver{
 
 	public static enum ControlActionType {shutdown, hold, release, stop, suspend, resume, finalize};
 		
 	
 	private final static String USER_PROPERTY = "flexiscaleUser";
 	private final static String PASSWORD_PROPERTY = "flexiscalePassword";	
 	private final static String URL_PROPERTY = "flexiscaleAddresss";
 	//private final static String username = "eod@tid.es";
 	//private final static String password = "5rCnVqie";
 	
 	
 	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("com.telefonica.claudia.smi.provisioning.FlexiscaleDriver");
 	private FlexiScaleSoapBindingStub service = null;
 	private String user;
 	private String pass;
 	private String endpointAddress;
 	private Map<String, Long> VDCMap = new HashMap<String, Long>();
 	private Map<String, Long> ServerMap = new HashMap<String, Long>();
 	private Map<String, Long> VLAN = new HashMap<String, Long>();
 	private Map<String, Long> ImageTemplateMap = new HashMap<String, Long>();
 
 	//Lo modifiqué
 	
 	//private Map<String, String> VLAN = new HashMap<String, String>();
 	private static final int ResourceTypeDisk=17;
 	private static final int ResourceTypeCPU = 3;
 	private static final int ResourceTypeMEMORY = 4;
 
 
 	
 	
 	public String createMetaData(String fqn) throws Exception{
 		
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		   DocumentBuilder builder;
 		try {
 		   builder = factory.newDocumentBuilder();
 		   Document docXML=builder.newDocument();
 		   Element root=docXML.createElement("CONFIG");
 		   Element meta=docXML.createElement("meta");
 		   Element customer=docXML.createElement("customer");
 		   Element public1=docXML.createElement("public");
 		   Element server=docXML.createElement("server");
 		   Element public2=docXML.createElement("public");
 		   Element fqn1=docXML.createElement("fqn");
 		   fqn1.setTextContent(fqn);
 		   public2.appendChild(fqn1);
 		   server.appendChild(public2);
 		   customer.appendChild(public1);
 		   meta.appendChild(customer);
 		   meta.appendChild(server);
 		   root.appendChild(meta);
 		   docXML.appendChild(root);
 		   StringWriter strWriter = null;
 		    XMLSerializer xmlSerializer = null;
 		    OutputFormat outFormat = null;
 		    
 		      xmlSerializer = new XMLSerializer();
 		      strWriter = new StringWriter();
 		      outFormat = new OutputFormat();
 
 		      //outFormat.setEncoding(XML_ENCODING);
 		      //outFormat.setVersion(XML_VERSION);
 		      outFormat.setIndenting(true);
 		      outFormat.setIndent(4);
 
 		      xmlSerializer.setOutputCharStream(strWriter);
 		      xmlSerializer.setOutputFormat(outFormat);
 		      xmlSerializer.serialize(docXML);
 		      strWriter.close();
 		    
 		    System.out.println( strWriter.toString());
 		    return strWriter.toString();
 		} catch (IOException ioEx) {
 		    throw new Exception (ioEx.getMessage());  
 			//System.out.println("Error : " + ioEx);
 		    }
 		
 	}
 	//yo
 	//protected String getNetId(String fqnNet) throws Exception { //(String fqnNet)
 	protected Long getNetId(String fqnNet) throws Exception { //(String fqnNet)
 		//System.out.println("fqnNet " + fqnNet + " " );
 		/*String sCadena;
 		FileReader fr = new FileReader("./conf/networks.txt");
 
 		BufferedReader bf = new BufferedReader(fr);
 		sCadena = bf.readLine();
 		System.out.println(sCadena);
 		String codigo = sCadena.substring(0,3);
 		System.out.println(codigo);
 		fr.close();
 		*/
 		System.out.println("fqnNet..."+"#"+fqnNet+"#");
 		//YO
 		//String nuevo=fqnNet.trim();
 		
 		 try {
 	            // Open the file c:\test.txt as a buffered reader
 	            BufferedReader bf = new BufferedReader(new FileReader("./conf/networks.txt"));
 	            
 	            // Start a line count and declare a string to hold our current line.
 	            int linecount = 0;
 	                String line;
 	 
 	            // Let the user know what we are searching for
 	               // String a="texto";
 	            //System.out.println("Searching for " + a + " in file...");
 	 
 	            // Loop through each line, stashing the line into our line variable.
 	            while (( line = bf.readLine()) != null)
 	            {
                  // Increment the count and find the index of the word
 	                    linecount++;
 	                    boolean indexfound = line.contains(fqnNet);
 	                    System.out.println("line"+line);
 	                    //System.out.println(line==nuevo);
 	                    System.out.println(line==fqnNet);
 	                    System.out.println(indexfound);
 	                    if(indexfound)
 	                   
 	                    {   //yo
 	                    	//linecount++;
 	                    	
 	                    	String codigo = line.substring(0,3);
 	                    	System.out.println(codigo);
 	                    	 Long lObj1 = new Long(codigo);
 	                    	 VLAN.put(fqnNet,lObj1);
 	                 		//System.out.println(lObj1);
 	                 		
 	                    	
 	                    }
 	 
 	                    // If greater than -1, means we found the word
 	                    /*if (indexfound > -1) {
 	                         System.out.println("Word was found at position " + indexfound + " on line " + linecount);
 	                    }*/
 	            }
 	           
 	 
 	            // Close the file after done searching
 	            bf.close();
 	            
 	        }
 	        catch (IOException e) {
 	            System.out.println("IO Error Occurred: " + e.toString());
 	        }
 		
 		/*
 		String sCadena;
 		sCadena = bf.readLine();
 		String sCadena1  = bf.readLine();
 		
 
 		while ((sCadena = bf.readLine())!=null) {
 
 		System.out.println(k);
 		
 		Map mapa1 = new HashMap();
 
 		mapa1.put(sCadena);
 
 		mapa1.putAll(m)put("clave2", "elemento2");
 
 		mapa1.put("clave3", "elemento3");
 
 		System.out.println("clave" + mapa1.keySet() + "valor" + mapa1.values());
 
 		} */
 	       
 		
 		
 		
 		//int codigo = 827;
 		//Long lObj1 = new Long(codigo);
 		//System.out.println(lObj1);
 		//VLAN.put(fqnNet,lObj1);
 	       
 		//me falta mostrar el hashmap
 	      
 		System.out.println("lee");
 if (VLAN.containsKey(fqnNet))
 			return VLAN.get(fqnNet);
 
 
 		else throw new Exception("Fqn does not match whith any vlan");
 
 		
 	}
 	
 	
 	//deleteVM
 	protected Long getVDCId(String fqnVDC) throws Exception{
 		long VDCid = -1;
 		String mapResult = null;
 		//cambio
 	
 		if (!VDCMap.containsKey(fqnVDC))
 			try {
 				
 				VDCMap = getVDCs();
 				System.out.println("VDCMap"+VDCMap);
 			} catch (RemoteException e) {
 				e.printStackTrace();
 				throw new Exception(e.getMessage());
 				
 			}
 
 		if (VDCMap.containsKey(fqnVDC))
 			
 			VDCid= VDCMap.get(fqnVDC).longValue();
 		
 		
 		log.info("Get VDC Id " +VDCid+ " for " + fqnVDC);
 		Long id = new Long(VDCid);
 		return id;
 	}
 	
 	protected long getVLANId (String network) throws Exception
 	{
 		if (network == null)
 			return -1;
 		String sCadena;
 		FileReader fr = new FileReader("./conf/networks.txt");
 
 		BufferedReader bf = new BufferedReader(fr);
 		sCadena = bf.readLine();
 		System.out.println(sCadena);
 		if (sCadena==null || sCadena.indexOf(network) == -1)
 		{
 			return -1;
 			
 		}
		String codigo = sCadena.substring(0,sCadena.indexOf("="));
	
 		fr.close();
 		
 		Long vlan_id = new Long(codigo);
 		System.out.println("Vlan id " + vlan_id);
 		return vlan_id;
 	}
 	
 	
 	protected Map<String, Long> getVDCs() throws RemoteException {
 		
 		VDC[] vdcs = null;
 		HashMap<String, Long> mapResult = null;
 		
 		try {
 			vdcs = service.listVDCs();
 			System.out.println("vdcs"+vdcs);
 			mapResult=new HashMap<String, Long>();
 			for(VDC v : vdcs) {
 				mapResult.put(v.getVdc_name(),v.getVdc_id());
 				System.out.println ("vdc " + v.getVdc_name() + " " + v.getVdc_id());
 			}
 		} catch (RemoteException e) {
 			e.printStackTrace();
 			throw new RemoteException(e.getMessage());
 		}
 		//yo
 		System.out.println("return mapResult"+ mapResult);
 		return mapResult;
 	}
 	
 	protected Long getServerId(String fqnVm) throws Exception{ //String fqn
 		
 		
 		System.out.println("ServerMap1" + ServerMap.containsKey(fqnVm));//fqn
 		System.out.println("SServerMap" + ServerMap);
 		System.out.println("fqnVm"+fqnVm);
 		
 		
 		 try {
 	            // Open the file c:\test.txt as a buffered reader
 	            BufferedReader bf = new BufferedReader(new FileReader("./conf/servers.txt"));
 	            
 	            // Start a line count and declare a string to hold our current line.
 	            int linecount = 0;
 	                String line;
 	 
 	            // Let the user know what we are searching for
 	               // String a="texto";
 	            //System.out.println("Searching for " + a + " in file...");
 	 
 	            // Loop through each line, stashing the line into our line variable.
 	            while (( line = bf.readLine()) != null)
 	            {
               // Increment the count and find the index of the word
 	                    linecount++;
 	                    boolean indexfound = line.contains(fqnVm);
 	                    System.out.println("line"+line);
 	                    //System.out.println(line==nuevo);
 	                    System.out.println(line==fqnVm);
 	                    System.out.println(indexfound);
 	                    if(indexfound)
 	                   
 	                    {   //yo
 	                    	//linecount++;
 	                    	
 	                    	String codigo = line.substring(0,line.indexOf("="));
 	                    	System.out.println(codigo);
 	                    	Long server_id = new Long(codigo);
 	                		ServerMap.put(fqnVm, server_id);
 	                 		//System.out.println(lObj1);
 	                 		
 	                    	
 	                    }
 	 
 	                    // If greater than -1, means we found the word
 	                    /*if (indexfound > -1) {
 	                         System.out.println("Word was found at position " + indexfound + " on line " + linecount);
 	                    }*/
 	            }
 	           
 	 
 	            // Close the file after done searching
 	            bf.close();
 	            
 	        }
 	        catch (IOException e) {
 	            System.out.println("IO Error Occurred: " + e.toString());
 	        }
 		
 	        
 		/*String sCadena;
 		FileReader fr = new FileReader("./conf/servers.txt");
 
 		BufferedReader bf = new BufferedReader(fr);
 		sCadena = bf.readLine();
 		System.out.println(sCadena);
 		String codigo = sCadena.substring(0,4);
 		System.out.println(codigo);
 		fr.close();
 		
 		Long server_id = new Long(codigo);
 		ServerMap.put(fqnVm, server_id);
 		System.out.println("ServerMap3"+ServerMap);
 		System.out.println("ServerMap4" + ServerMap.containsKey(fqnVm));
 		System.out.println("lee");
 		*/
 		/*if (!ServerMap.containsKey(fqnVm)) //fqn
 			//yo
 			System.out.println("ServerMap" + ServerMap.containsKey(fqnVm));//fqn
 			try {
 				String fqnVDC=URICreation.getVDC("es.tid.customers.cc1.services.ss1.vapp.vapp1.replicas.1");
 				System.out.println("fqnVDC" + fqnVDC);
 				
 				//ServerMap = getServers(getVDCId(fqnVDC));
 				//YO
 				Long VDCId = getVDCId(fqnVDC);
 				System.out.println("VDCId" + VDCId);
 				
 				ServerMap = getServers(VDCId);
 				System.out.println("ServerMap2" + ServerMap);
 			} catch (Exception e) {
 				e.printStackTrace();
 				throw new Exception(e.getMessage());
 				
 			}
 		
 			System.out.println("FQN " + fqnVm); //fqn
 			Iterator itr = ServerMap.entrySet().iterator();
 			while (itr.hasNext()) {
 				Map.Entry e = (Map.Entry)itr.next();
 				System.out.println("clave: "+e.getKey()+" valor:"+e.getValue());
 			}
 			*/
 			
 		if (ServerMap.containsKey(fqnVm))//fqn
		{
 			System.out.println("ServerMap1" + ServerMap.containsKey(fqnVm));//fqn	
 			return ServerMap.get(fqnVm);//fqn
		}
		return null;
	
 	}
 	
 	protected Map<String, Long> getServers(Long vdc_id) throws Exception {
 		
 		Server[] servers = null;
 		HashMap<String, Long> mapResult = null;
 		
 		try {
 			servers = service.listServers(vdc_id);
 			System.out.println("Servers:" + servers);
 			mapResult=new HashMap<String, Long>();
 			for(Server s : servers) {
 				ServerMetadata sm=service.getServerMetadata(s.getServer_id());
 				//System.out.println("sm" + sm.getPublic_metadata());
 			
 				//yo
 				//System.out.println("s" + s.getServer_id());
 				
 				//DOMParser parser = new DOMParser();
 				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 			
 				DocumentBuilder builder = factory.newDocumentBuilder();
 				Document doc=builder.parse(new InputSource(new java.io.StringReader(sm.getPublic_metadata()))); // xml is a
 				
 				String expression = "/CONFIG/meta/server/public/fqn";      
 				XPath xpath=XPathFactory.newInstance().newXPath();
 				NodeList nodes = (NodeList) xpath.evaluate(expression, doc.getDocumentElement(), XPathConstants.NODESET);
 				//System.out.println("nodes" + nodes);
 				String fqnVM=nodes.item(0).getTextContent();
 				//System.out.println("primero" + fqnVM);
 				//System.out.println("nodes" + nodes);
 
 				//original
 				mapResult.put(fqnVM,s.getServer_id());
 				System.out.println("mapResult"+mapResult);
 			}
 		} catch (RemoteException e) {
 			
 			throw new Exception(e.getMessage());
 			
 		}
 		System.out.println("mapResult"+mapResult);
 		
 		return mapResult;
 		
 	}
 	
 	//---------------------------------------------
 	
 	public class DeployVMTask extends Task {
 
 		public static final long POLLING_INTERVAL= 10000;
 
 		private static final int MAX_CONNECTION_ATEMPTS = 5;
 		
 		String fqnVm;
 		String ovf;
 		
 		public DeployVMTask(String fqn, String ovf) {
 			
 			super();
 
 		
 			this.ovf = ovf;
 			System.out.println("ovf"+ovf);
 		
 		}
 		
 		@Override
 		public void execute() {
 			setStatus(TaskStatus.RUNNING);
 			setStartTime(System.currentTimeMillis());
 			System.out.println("llega dentro del execute");
 			try {
 				System.out.println("llega dentro del execute1");
 				// Create the Virtual Machine
 				long idVM = createVirtualMachine();
 				System.out.println ("Virtual machine created idVM" + idVM);
 				// Wait until the state is RUNNING
 				setStatus(TaskStatus.WAITING);
 				while (true) {
 						try {
 							
 							int state= service.getServer(idVM).getStatus();
 							
 							if (state == 2) {
 								setStatus(TaskStatus.SUCCESS);
 								setEndTime(System.currentTimeMillis());
 								break;
 							} /*else if (state ==FAILED_STATE) {
 								setStatus(TaskStatus.ERROR);
 								setEndTime(System.currentTimeMillis());
 								break;
 							}*/
 							
 							//connectionAttempts=0;
 						
 						} catch (RemoteException ioe) {
 								log.error("Flexiscale driver error"+ioe.getMessage());
 								setStatus(TaskStatus.ERROR);
 								setEndTime(System.currentTimeMillis());
 								break;	
 						}
 					
 					Thread.sleep(POLLING_INTERVAL);
 				}
 				
 			} catch (Exception e) {
 				log.error("Unexpected error creating VM: " + e.getMessage());
 				//yo
 				e.printStackTrace();
 				TaskError taskError = new TaskError();
 				taskError.message = e.getMessage();
 				setError(taskError);
 				
 				setStatus(TaskStatus.ERROR);
 				setEndTime(System.currentTimeMillis());
 				return;
 			}
 		}
 		
 		private long createVirtualMachine() throws Exception {
 			System.out.println("llega a createVirtualMachine");
 			
 			 
 			try {
 				
 				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 				factory.setNamespaceAware(true);
 			    DocumentBuilder builder = factory.newDocumentBuilder();
 			    Document doc = builder.parse(new ByteArrayInputStream(ovf.getBytes()));
 			    
 			    //Document doc = builder.parse(new InputSource("D:\\Codigos\\flexiscale\\conf\\ovf"));
 				  
 
 			
 				if (!doc.getFirstChild().getNodeName().equals(TCloudConstants.TAG_INSTANTIATE_OVF)) {
 					//no entra en este nodo
 					log.error("Element <"+TCloudConstants.TAG_INSTANTIATE_OVF+"> not found.");
 					throw new Exception("Element <"+TCloudConstants.TAG_INSTANTIATE_OVF+"> not found.");
 				}
 				//original
 				Element root = (Element) doc.getFirstChild();
 							
 				String replicaName = root.getAttribute("name");
 				System.out.println("replicaName"+replicaName);
 				fqnVm = replicaName;
 				
 				NodeList envelopeItems = doc.getElementsByTagNameNS("*", "Envelope");
 				
 				if (envelopeItems.getLength() != 1) {
 					log.error("Envelope items not found.");
 					throw new Exception("Envelope items not found.");
 				}
 				// Extract the ovf sections and pass them to the OVF manager to be processed.
 				Document ovfDoc = builder.newDocument();
 				ovfDoc.appendChild(ovfDoc.importNode(envelopeItems.item(0), true));
 				//aquí estaba el error
 				OVFSerializer ovfSerializer = OVFSerializer.getInstance();
 				ovfSerializer.setValidateXML(false);
 				EnvelopeType envelope = ovfSerializer.readXMLEnvelope(new ByteArrayInputStream(DataTypesUtils.serializeXML(ovfDoc).getBytes()));
 				
 				//no lee
 				long vdc_id=getVDCId(URICreation.getVDC(fqnVm));
 				if (vdc_id==-1)
 				{
 					log.error("The VDC " +  URICreation.getVDC(fqnVm) + "does not exist") ;
 					throw new Exception ("The VDC " +  URICreation.getVDC(fqnVm) + "does not exist");
 					
 				}
 								
 				String net = getNetworkName (envelope);
 	            long vlan_id = getVLANId (net);
 	            
 	            if (vlan_id==-1)
 				{
 	              log.error("The VM cannot be deployed. There is not any network "+ net + "deployed");
 				  throw new Exception("The VM cannot be deployed. There is not any network deployed");
 				}  
 				
 				
 				// obtener el id de la red de la VM (Debe estar desplegada...)
 				String name=replicaName;
 				System.out.println("replicaName" + replicaName);  
 				
 
                 String imag = getImageName (envelope);
                 long image_id = 635;
                 if (imag!=null)
                   image_id = getImageTemplateId(imag);
                 if (image_id==-1)
                 	image_id = 635;
                 System.out.println("image_id" + image_id);
                 
 				
 				int memory = 0;
 				int cpu = 0;
 				int capacity = 0;
 				
 				
 				
 				ContentType entityInstance = OVFEnvelopeUtils.getTopLevelVirtualSystemContent(envelope);
 				
 				if (entityInstance instanceof VirtualSystemType) {
 					
 					VirtualSystemType vs = (VirtualSystemType) entityInstance;
 					
 					VirtualHardwareSectionType vh = OVFEnvelopeUtils.getSection(vs, VirtualHardwareSectionType.class);
 				//	String virtualizationType = vh.getSystem().getVirtualSystemType().getValue();
 		
 					String diskRoot;
 					
 					
 					
 
 					List<RASDType> items = vh.getItem();
 					for (Iterator<RASDType> iteratorRASD = items.iterator(); iteratorRASD.hasNext();) {
 						RASDType item = (RASDType) iteratorRASD.next();
 
 						/* Get the resource type and process it accordingly */
 						int rsType = new Integer(item.getResourceType().getValue());
 
 						int quantity = 1;
 						if (item.getVirtualQuantity() != null) {
 							quantity = item.getVirtualQuantity().getValue().intValue();
 						}
 
 						switch (rsType) {
 							case ResourceTypeCPU:
 								cpu=quantity;
 								break;
 								
 							case ResourceTypeDisk:
 								
 								/*
 								 * The rasd:HostResource will follow the pattern
 								 * 'ovf://disk/<id>' where id is the ovf:diskId of some
 								 * <Disk>
 								 */
 								String hostRes = item.getHostResource().get(0).getValue();
 								StringTokenizer st = new StringTokenizer(hostRes, "/");
 		
 								/*
 								 * Only ovf:/<file|disk>/<n> format is valid, accodring
 								 * OVF spec
 								 */
 								if (st.countTokens() != 3) {
 									throw new IllegalArgumentException("malformed HostResource value (" + hostRes + ")");
 								}
 								if (!(st.nextToken().equals("ovf:"))) {
 									throw new IllegalArgumentException("HostResource must start with ovf: (" + hostRes + ")");
 								}
 								String hostResType = st.nextToken();
 								if (!(hostResType.equals("disk") || hostResType.equals("file"))) {
 									throw new IllegalArgumentException("HostResource type must be either disk or file: (" + hostRes + ")");
 								}
 								String hostResId = st.nextToken();
 		
 								String fileRef = null;
 								
 								if (hostResType.equals("disk")) {
 									/* This type involves an indirection level */
 									DiskSectionType ds = null;
 									ds = OVFEnvelopeUtils.getSection(envelope, DiskSectionType.class);
 									List<VirtualDiskDescType> disks = ds.getDisk();
 									
 									for (Iterator<VirtualDiskDescType> iteratorDk = disks.iterator(); iteratorDk.hasNext();) {
 										VirtualDiskDescType disk = iteratorDk.next();
 		
 										String diskId = disk.getDiskId();
 										if (diskId.equals(hostResId)) {
 											
 											fileRef = disk.getFileRef();
 											capacity = Integer.parseInt(disk.getCapacity());
 											
 											break;
 										}
 									}
 								} else {
 									throw new IllegalArgumentException("File type not supported in Disk sections.");
 								}
 		
 								/* Throw exceptions in the case of missing information */
 								if (fileRef == null) {
 									throw new IllegalArgumentException("file reference can not be found for disk: " + hostRes);
 								}
 
 								
 								URL url = null;
 								String digest = null;
 		
 								ReferencesType ref = envelope.getReferences();
 								List<FileType> files = ref.getFile();
 		
 								for (Iterator<FileType> iteratorFl = files.iterator(); iteratorFl.hasNext();) {
 									FileType fl = iteratorFl.next();
 									if (fl.getId().equals(fileRef)) {
 										try {
 											url = new URL(fl.getHref());
 										} catch (MalformedURLException e) {
 											throw new IllegalArgumentException("problems parsing disk href: " + e.getMessage());
 										}
 		
 										/*
 										 * If capacity was not set using ovf:capacity in
 										 * <Disk>, try to get it know frm <File>
 										 * ovf:size
 										 */
 										if (capacity == 0 && fl.getSize() != null) {
 											capacity = Integer.parseInt(fl.getSize().toString());
 										}
 										
 										/* Try to get the digest */
 							            Map<QName, String> attributesFile = fl.getOtherAttributes();
 										QName digestAtt = new QName("http://schemas.telefonica.com/claudia/ovf","digest");
 										digest = attributesFile.get(digestAtt);
 					
 										break;
 									}
 								}
 		
 								/* Throw exceptions in the case of missing information */
 								
 								if (url == null) {
 									throw new IllegalArgumentException("url can not be set for disk " + hostRes);
 								}
 								
 								if (digest == null) {
 									log.debug("md5sum digest was not found for disk " + hostRes);
 								}
 								
 								String urlDisk = url.toString();
 								
 								if (urlDisk.contains("file:/"))
 									urlDisk = urlDisk.replace("file:/", "file:///");
 								
 								break;	
 								
 							case ResourceTypeMEMORY:
 								memory=quantity;
 								break;
 							
 							/*default:
 								throw new IllegalArgumentException("unknown hw type: " + rsType);*/
 						}
 					}
 					
 					
 					
 					
 					//---	
 					log.info("cpu " + cpu + " memory " + memory + " capacity " + capacity);
 					long server_productoffer_id = obtainServerOffer (cpu, memory);
 					long disk_productoffers [] = {20};
 					disk_productoffers = obtainDiskOffer (capacity);
 					log.info("server_productoffer_id " + server_productoffer_id + " disk_productoffers " + disk_productoffers[0]);
 					Server s=service.createServer(vdc_id, vlan_id, name, image_id, server_productoffer_id, disk_productoffers);	
 					log.info("s"+s);
 					RuntimeMetadata rm = new RuntimeMetadata();
 					service.startServer(s.getServer_id(),rm);
 					
 					
 					//yo
 					/*PrintWriter writer = new PrintWriter("./conf/servers.txt");
 					System.out.println("writer"+writer);
 					writer.print(s.getServer_id()+ "="+fqnVm );
 					writer.close();*/
 					 BufferedWriter writer = new BufferedWriter(new FileWriter("./conf/servers.txt", true));
 		             writer.write(s.getServer_id()+ "="+fqnVm+"\n");
 		             writer.close();
 		           //yo
 					ServerMap.put(fqnVm, s.getServer_id());
 					System.out.println("prueba"+ServerMap);
 
 					String public_metadata=createMetaData(fqnVm);
 					//rm.setPublic_metadata(public_metadata);
 					
 					ServerMetadata sm= new ServerMetadata(s.getServer_id(),public_metadata,"","","");
 					service.setServerMetadata(sm);
 					
 					
 					return s.getServer_id();
 			} else {
 				throw new IllegalArgumentException("OVF malformed. No VirtualSystemType found.");
 			}
 			
 		} catch (IOException e1) {
 			log.error("OVF of the virtual machine was not well formed or it contained some errors.");
 			e1.printStackTrace();
 			
 			throw new Exception("OVF of the virtual machine was not well formed or it contained some errors: " + e1.getMessage());
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 			log.error("Error configuring parser: " + e.getMessage());
 			throw new Exception("Error configuring parser: " + e.getMessage());
 		} catch (FactoryConfigurationError e) {
 			log.error("Error retrieving parser: " + e.getMessage());
 			e.printStackTrace();
 			throw new Exception("Error retrieving parser: " + e.getMessage());
 		
 		} catch (Exception e) {
 			log.error("Error "+  e.getMessage());
 			e.printStackTrace();
 			throw new Exception("Error " + e.getMessage());
 		}	
 			
 //---
 								
 		}	
 		
 	private int obtainServerOffer (int cpu, int memory)
 	{
 
 		int server_productoffer_id =0;
 		switch (cpu) {
 		 
 		case 1: if (memory==1||memory==1024) server_productoffer_id=18;
 		      else if (memory==2) server_productoffer_id=19;
 				else  server_productoffer_id=17;
 				break;
 				
 		case 2: if (memory==4) server_productoffer_id=21;
 				else  server_productoffer_id=115;
 				break;
 				
 		case 3: if (memory==6) server_productoffer_id=22;
 				else  server_productoffer_id=116;
 				break;
 			
 		case 4: if (memory==4) server_productoffer_id=117;
 				else if (memory==6) server_productoffer_id=118;
 				else  server_productoffer_id=23;
 				break;
 			
 		case 5: if (memory==6) server_productoffer_id=119;
 				else  server_productoffer_id=120;
 				break;
 				
 		case 6: if (memory==6) server_productoffer_id=121;
 				else  server_productoffer_id=122;
 				break;
 				
 		case 7: server_productoffer_id=123;
 				break;
 				
 		case 8: server_productoffer_id=124;
 				break;
 
 		}
 		return server_productoffer_id;
 	}
 	
 	private long [] obtainDiskOffer (int capacity)
 	{
 		
 		long [] disk_productoffers = {0};
 		switch (capacity) {
 	    case 0: disk_productoffers[0]=31;
 	     break;
 		case 20: disk_productoffers[0]=31;
 				 break;
 				 
 		case 50: disk_productoffers[0]=30;
 		 		 break;
 		 		 
 		case 100: disk_productoffers[0]=29;
 		 		  break;
 		 		  
 		case 250: disk_productoffers[0]=28;
 		 		  break;
 		 		  
 		case 500: disk_productoffers[0]=49;
 		          break;
 		          
 		case 1000:disk_productoffers[0]=50;
 		          break;
 		          
 		
 	   }
 		return disk_productoffers;
 	}
 			
 			
 	}
 	
 	
 	//---------------------------------------------
 	
 	public class UndeployVMTask extends Task {
 
 		String fqnVm;
 		
 		public UndeployVMTask(String fqn) {//String vmFqn
 			//this.fqnVm = vmFqn;
 			//yo
 			this.fqnVm = fqn;
 			System.out.println("coincide con log?"+fqn);
 			//execute();
 		}
 
 		@Override
 		public void execute() {
 			
 			setStatus(TaskStatus.RUNNING);
 			setStartTime(System.currentTimeMillis());
 			
 			// Undeploy the VM
 			try {
 				//yo
 				//long id= 8557;
 				Long id = getServerId(fqnVm);
 				//yo
 				log.info("Delete VM id" + id + " for fqn"+ fqnVm);
 	
 				deleteVirtualMachine(id);
 				//yo (NO LO IMPRIME)
 			
 				
 				setStatus(TaskStatus.SUCCESS);
 		
 				setEndTime(System.currentTimeMillis());
 			} catch (RemoteException e) {
 				log.error("Error connecting to Flexiscale: " + e.getMessage());
 				
 				TaskError taskError = new TaskError();
 				taskError.message = e.getMessage();
 				setError(taskError);
 				
 				setStatus(TaskStatus.ERROR);
 				setEndTime(System.currentTimeMillis());
 				return;
 			} catch (Exception e) {
 				log.error("Unknown error undeploying VM: " + e.getMessage());
 				e.printStackTrace();
 				e.toString();
 				
 				TaskError taskError = new TaskError();
 				taskError.message = e.getMessage();
 				setError(taskError);
 				
 				setStatus(TaskStatus.ERROR);
 				setEndTime(System.currentTimeMillis());
 				return;
 			}
 		}
 		
 		//@SuppressWarnings("unchecked")
 		public void deleteVirtualMachine(Long id) throws Exception {
 
 				ServerMetadata meta = service.getServerMetadata(id);
 				System.out.println (meta.getPublic_metadata());
 				
 				try {
 					if (service.getServer(id).getStatus() == 2) {
 						log.info("Stoping service id" + id);
 						service.stopServer(id, 1);
 						while (service.getServer(id).getStatus() != 5)
 							try {
 								Thread.sleep(1);
 								
 							} catch (InterruptedException e) {
 								throw new Exception(e.getMessage());
 							}
 					}
 					if (service.getServer(id).getStatus() == 5)
 					{
 						log.info("Destroying service id" + id);
 						service.destroyServer(id);
 						
 					}
 					else throw new Exception ("The vm cannot be destroyed because is not stopped");
 					
 				} catch(RemoteException re) {
 					throw new RemoteException(re.getMessage());
 				}
 				
 				log.info("Update Status Services");
 
 				updateStatusService (id);
 
 			
 		}
 		
 		private void updateStatusService (Long id) throws Exception
 		{
 			//File inFile = new File(file);
 		    File inFile = new File("./conf/servers.txt");
 		    System.out.println("inFile"+inFile);
 		    if (!inFile.isFile()) {
 		       System.out.println("Parameter is not an existing file");
 		       return;
 		     }
 		      
 		     //Construct the new file that will later be renamed to the original filename.
 		     //File tempFile = new File(inFile.getAbsolutePath() + ".tmp");
 		     //yo
 		    File tempFile = new File("./conf/servers.tmp");
 			        
 		    
 		    BufferedReader br;
 			
 			br = new BufferedReader(new FileReader("./conf/servers.txt"));
 			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
 				  
 			String line = null;
 
 			     //Read from the original file and write to the new
 			     //unless content matches data to be removed.
 			while ((line = br.readLine()) != null) {
 			      
 					
 			  String str = new Long(id).toString();
 
 		   	  if (!line.contains(str)) {
 			         pw.println(line);
 			         pw.flush();
 			       }
 			 }
 			 pw.close();
 			 br.close();
 			     
 			  //Delete the original file
 			 if (!inFile.delete()) {
 			      throw new Exception("Could not delete file");
 			     
 			 }
 			     
 			//Rename the new file to the filename the original file had.
 			if (!tempFile.renameTo(inFile))
 			    	 throw new Exception("Could not rename file");
 		}
 	}
 	
 	
 	
 	@SuppressWarnings("serial")
 	public class DeployNetworkTask extends Task {
 		
 		private Long VDC_Id;
 		private String _ovf;
 		private String ipAddress;
 		private String netName;
 		private String fqnNet;
 	
 		private void TCloud2Flexiscale() throws Exception {
 			
 			try {
 			    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 			    Document doc = builder.parse(new ByteArrayInputStream(_ovf.getBytes()));
 				
 			    Element root = (Element) doc.getFirstChild();
 			    netName = root.getAttribute(TCloudConstants.ATTR_NETWORK_NAME);
 			    
 				//NodeList netmaskList = doc.getElementsByTagName(TCloudConstants.TAG_NETWORK_NETMASK);
 				NodeList baseAddressList = doc.getElementsByTagName(TCloudConstants.TAG_NETWORK_BASE_ADDRESS);
 				
 				
 				
 				// If there is a netmask, calculate the size of the net counting it's bits. 
 				if (baseAddressList.getLength()>0) {
 					ipAddress=baseAddressList.item(0).getTextContent();
 				}
 				
 				
 				
 			
 			} catch (IOException e1) {
 				log.error("OVF of the virtual machine was not well formed or it contained some errors.");
 				throw new Exception("OVF of the virtual machine was not well formed or it contained some errors: " + e1.getMessage());
 			} catch (ParserConfigurationException e) {
 				log.error("Error configuring parser: " + e.getMessage());
 				throw new Exception("Error configuring parser: " + e.getMessage());
 			} catch (FactoryConfigurationError e) {
 				log.error("Error retrieving parser: " + e.getMessage());
 				throw new Exception("Error retrieving parser: " + e.getMessage());
 			} catch (Exception e) {
 				log.error("Error configuring a XML Builder.");
 				throw new Exception("Error configuring a XML Builder: " + e.getMessage());
 			}
 			
 		}
 		
 		public DeployNetworkTask(String netFqn, String ovf) {
 			
 			
 			fqnNet=netFqn;
 			System.out.println("fqnNet"+fqnNet);
 			_ovf=ovf;
 			//execute();
 			
 			
 		}
 
 		@Override
 		public void execute() {
 			setStatus(TaskStatus.RUNNING);
 			setStartTime(System.currentTimeMillis());
 			long vdc_id;
 			String VDC_name=URICreation.getVDC(fqnNet);
 			System.out.println("fqnNet" + fqnNet);
 			System.out.println("VDC_name" + VDC_name);
 			System.out.println("no hace el try" );
 			try {
 				vdc_id=getVDCId(VDC_name);
 				//log.info( "q"+vdc_id);
 				
 				System.out.println("vdc_id imprime" + vdc_id);
 				
 				Vlan v=service.createVLAN(vdc_id,false);
 				System.out.println ("Red creada " + v.getVlan_name() + "Vlan_uuid " + v.getVlan_uuid()+ "vlan_id"+v.getVlan_id()+"getVdc_id"+ v.getVdc_id());
 			
 				
 				//yo
 				/*PrintWriter writer = new PrintWriter("./conf/networks.txt");
 				writer.print(v.getVlan_id()+ "="+ fqnNet + "Vlan_name" + v.getVlan_name());
 				writer.close();*/
 				 BufferedWriter writer = new BufferedWriter(new FileWriter("./conf/networks.txt", true));
 	             //writer.write(v.getVlan_id()+ "="+ fqnNet + "Vlan_name" + v.getVlan_name()+"\n");
 				 if (fqnNet.indexOf("public")!=-1)
 					 writer.write(v.getVlan_id()+ "="+ "public" +"="+ v.getVlan_name()+"\n");
 				 else
 				    writer.write(v.getVlan_id()+ "="+ fqnNet +"="+ v.getVlan_name()+"\n");
 	             writer.close();
 
 								
 				
 				setStatus(TaskStatus.SUCCESS);
 				
 				setEndTime(System.currentTimeMillis());
 				System.out.println("no llega a success" );
 				
 				
 			} catch (RemoteException e) {
 				log.error("Error connecting to Flexiscale: " + e.getMessage());
 				
 				TaskError taskError = new TaskError();
 				taskError.message = e.getMessage();
 				setError(taskError);
 
 				setEndTime(System.currentTimeMillis());
 				
 				setStatus(TaskStatus.ERROR);
 				return;
 			} catch (Exception e) {
 				log.error("Unknown error creating network: " + e.getMessage());
 				
 				TaskError taskError = new TaskError();
 				taskError.message = e.getMessage();
 				setError(taskError);
 				
 				setEndTime(System.currentTimeMillis());
 				
 				setStatus(TaskStatus.ERROR);
 			}
 	
 		}
 		
 	}
 	
 	//Yo
 	@SuppressWarnings("serial")
 public class UndeployNetworkTask extends Task 
 {
 		
 	
 	private String fqnNet;
 	
 	
 	public UndeployNetworkTask (String netFqn)
 	{
 		this.fqnNet=netFqn;
 		System.out.println("Net?"+fqnNet);
 		System.out.println("Net?"+netFqn);
 		//execute();
 	}
 	
 	@Override
 	public void execute() 
 	{
 		setStatus(TaskStatus.RUNNING);
 		setStartTime(System.currentTimeMillis());
 		
 		//Undeploy Net
 		try
 	{   
 			
 			
 			System.out.println("Net?"+fqnNet);
 			Long id = getNetId(fqnNet);
 			System.out.println("fqnNet"+ fqnNet);
 			deleteNetwork(id);
 			//no lo lee
 			System.out.println("hace delete");
 
 			
 			
 			
 			
 			setStatus(TaskStatus.SUCCESS);
 			setEndTime(System.currentTimeMillis());
 			
 		}
 		catch (RemoteException e) {
 			log.error("Error connecting to Flexiscale: " + e.getMessage());
 			
 			TaskError taskError = new TaskError();
 			taskError.message = e.getMessage();
 			setError(taskError);
 			
 			setStatus(TaskStatus.ERROR);
 			setEndTime(System.currentTimeMillis());
 			return;
 		} catch (Exception e) {
 			log.error("Unknown error undeploying Net: " + e.getMessage());
 			e.printStackTrace();
 			e.toString();
 			
 			TaskError taskError = new TaskError();
 			taskError.message = e.getMessage();
 			setError(taskError);
 			setStatus(TaskStatus.ERROR);
 			setEndTime(System.currentTimeMillis());
 			return;
 		}
 		
 	}
 	public void deleteNetwork(Long id) throws Exception
 	{
 		//yo
 		
 		try {
 
 		      //File inFile = new File(file);
 		      File inFile = new File("D:\\Codigos\\tcloud-server\\target\\tcloud-server-0.1.1-environment\\tcloud-server\\conf\\networks.txt");
 		     System.out.println("inFile"+inFile);
 		      if (!inFile.isFile()) {
 		        System.out.println("Parameter is not an existing file");
 		        return;
 		      }
 		       
 		      //Construct the new file that will later be renamed to the original filename.
 		      //File tempFile = new File(inFile.getAbsolutePath() + ".tmp");
 		      //yo
 		      File tempFile = new File("D:\\Codigos\\tcloud-server\\target\\tcloud-server-0.1.1-environment\\tcloud-server\\conf\\networks.tmp");
 			    
 		      System.out.println("tempFile"+tempFile);
 		      
 		     
 		      BufferedReader br = new BufferedReader(new FileReader("D:\\Codigos\\tcloud-server\\target\\tcloud-server-0.1.1-environment\\tcloud-server\\conf\\networks.txt"));
 		      System.out.println("br"+br);
 		      PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
 		      System.out.println("pw"+pw);
 		      String line = null;
 
 		      //Read from the original file and write to the new
 		      //unless content matches data to be removed.
 		      while ((line = br.readLine()) != null) {
 		       
 				
 		    	  String str = id.toString();
 		    	  
 		        
 		    	  //yo
 		    	  if (!line.contains(str)) {
 		          pw.println(line);
 		          pw.flush();
 		        }
 		      }
 		      pw.close();
 		      br.close();
 		      
 		      //Delete the original file
 		      if (!inFile.delete()) {
 		        System.out.println("Could not delete file");
 		        return;
 		      }
 		      
 		      //Rename the new file to the filename the original file had.
 		      if (!tempFile.renameTo(inFile))
 		        System.out.println("Could not rename file");
 		      
 		    }
 		    catch (FileNotFoundException ex) {
 		      ex.printStackTrace();
 		    }
 		    catch (IOException ex) {
 		      ex.printStackTrace();
 		    }
 		System.out.println("lee1");
 		//laura
 		//ServerMetadata meta= service.getServerMetadata(id);
 		//System.out.println("es" + meta.getPublic_metadata());
 		System.out.println("id"+id);
 		service.deleteVLAN(id);
 		
 		
 		
 		/*Map mapa1 = new HashMap();
 		//v.getVlan_name()+ "="+VDC_name
 		mapa1.put(v.getVlan_name(), "VDC_name");
 
 		mapa1.put("clave2", "elemento2");
 
 		mapa1.put("clave3", "elemento3");
 
 		System.out.println("clave" + mapa1.keySet() + "valor" + mapa1.values());
 		*/
 	}
 	
 	/*public Long getNetworkId (String fqnNet)
 	{
 		
 		
 		//Leer un fichero
 		/*StringBuffer lineaTotal="";
 		
 		StringBuffer linea = reader.readLine();
 		while (linea != null)
 		{
 		   lineaTotal.append(linea);
 		   lineaTotal.append(System.getProperty("line.separator"));
 		   linea = reader.readLine();
 		}
 		areaTexto.setText(lineaTotal.toString());
 		reader.close();
 		
 		
 		//service.listVLANs(vdc_id);
 	}*/
 }
 	//YO
 	public class PowerActionVirtualMachineTask extends Task 
 	{
 			
 		String fqnVm;
 		String action;
 		
 	public PowerActionVirtualMachineTask(String fqn, String action) 
 	{
 		
 		
 		this.fqnVm=fqn;
 		this.action=action;
 		
 		
 	}
 
 	@Override
 	public void execute() 
 	{
 
 		setStatus(TaskStatus.RUNNING);
 		setStartTime(System.currentTimeMillis());
 		try {
 			Long id = getServerId(fqnVm);
 			System.out.println("id"+id);
 			System.out.println(action.contentEquals("powerOff"));
 			System.out.println(action.contentEquals("powerOn"));
 			System.out.println("status"+service.getServer(id).getStatus());
 			if(action.contentEquals("powerOff"))
 			{   
 				System.out.println(action.contentEquals("powerOff"));
 				 System.out.println("status"+service.getServer(id).getStatus());
 				//Comprobamos estado 2 es arrancado
 				try
 				{	 System.out.println("4status"+service.getServer(id).getStatus());
 					System.out.println(service.getServer(id).getStatus() == 2);
 				  if (service.getServer(id).getStatus() == 2) 
 				 {
 					// Paramos la VM
 					  System.out.println("Paramos la VM");
 					 service.stopServer(id, 1);
 					 System.out.println("stop");
 
 				 }
 				 else if (service.getServer(id).getStatus() == 5)
 				 {
 					 System.out.println("else if");
 					 log.error("Ensure the server is running before continuing. The server is in state stopped");
 				   throw new Exception ("Ensure the server is running before continuing. The server is in state stopped");
 				  
 				 }
 				  
 				 else {
 					 System.out.println("else ");
 				  System.out.println("1status"+service.getServer(id).getStatus());
 				     log.error("The virtual machine is in another status");
 					 throw new Exception ("The virtual machine is in another status");
 				 } 
 					 
 				}
 				catch (RemoteException e)
 				{
 					e.printStackTrace();
 					System.out.println("powerOff"+service.stopServer(id,1));
 					throw new RemoteException(e.getMessage());
 				}
 			}
 			
 			else	if(action.contentEquals("powerOn")){
 				System.out.println("2status"+service.getServer(id).getStatus());
 				System.out.println("?"+action.contentEquals("powerOn"));
 			
 			{
 					try 
 					{
 						
 			
 						if (service.getServer(id).getStatus() == 5) {
 							RuntimeMetadata rmd = new RuntimeMetadata();
 							System.out.println("rmd"+rmd);
 							service.startServer(id, rmd);
 							System.out.println("powerOn");
 					
 						}
 					else if (service.getServer(id).getStatus() == 2)
 					{
 						log.error("Ensure the server has stopped before running");
 						throw new Exception ("Ensure the server has stopped before running");
 					}
 					 else{
 						 System.out.println("else ");
 						System.out.println("3status"+service.getServer(id).getStatus());
 					     log.error("The virtual machine is in another status.");
 						 throw new Exception ("The virtual machine is in another status.");
 					 }
 				
 					} catch(RemoteException re) {
 						System.out.println("else if?");
 						throw new RemoteException(re.getMessage());
 			} 
 				
 			
 			
 		//doAction(id);
 		 
 			}
 			//añadido
 			}
 			
 		} catch (Exception e) {
 			System.out.println("error");
 			log.error("error" + e.getMessage());
 			e.printStackTrace();
 			e.toString();
 			
 			TaskError taskError = new TaskError();
 			taskError.message = e.getMessage();
 			setError(taskError);
 			
 			setStatus(TaskStatus.ERROR);
 			setEndTime(System.currentTimeMillis());
 			return;
 			// TODO: handle exception
 		}
 		
 			
 		//borrar
 		//ControlActionType tdc = ControlActionType.stop;
 		//System.out.println("tdc" + tdc);
 
 		
 
 	}
 	}
 	
 	
 	//borrar
 	/*@Override
 	public void execute() 
 	{
 
 		setStatus(TaskStatus.RUNNING);
 		setStartTime(System.currentTimeMillis());
 		try {
 			Long id = getServerId(fqnVm);
 			System.out.println("id"+id);
 			if(action.contentEquals("powerOff"))
 			{   
 				
 					// Paramos la VM
 					 service.stopServer(id, 1);
 
 				 }
 				 
 			
 			else	if(action.contentEquals("powerOn"))
 			
 			{
 					
 							RuntimeMetadata rmd = new RuntimeMetadata();
 							System.out.println("rmd"+rmd);
 							service.startServer(id, rmd);
 							System.out.println("powerOn");
 					
 			} 
 				
 			
 			System.out.println("fqnVm" + fqnVm);
 		//doAction(id);
 		 
 		
 			
 			
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 		
 			
 		//borrar
 		//ControlActionType tdc = ControlActionType.stop;
 		//System.out.println("tdc" + tdc);
 
 		
 
 	}
 	}*/
 	
 	
 	public void doAction(Long id) throws Exception 
 	{
 		
 		
 		//YO
 		/*ControlActionType controlAction = ControlActionType.stop;
 		System.out.println("controlActiion" + controlAction);
 		service.rebootServer(server_id)
 		service.startServer(server_id, runtime_metadata)
 		service.stopServer(server_id, stop_method)
 		
 		shutdown, hold, release, stop, suspend, resume, finalize
 		*/
 	}
 	
 	
 	
 	
 	
 	
 	
 	public FlexiscaleDriver(Properties prop) {
 		
 	    try {
 	    	user=prop.getProperty(USER_PROPERTY);
 	    	pass=prop.getProperty(PASSWORD_PROPERTY);
 	    	endpointAddress=prop.getProperty(URL_PROPERTY);
 	    } catch (Exception e) {
 	    	log.error("Property not found");
 	    }
 		FlexiScaleServiceLocator locator = new FlexiScaleServiceLocator();
 		locator.setFlexiScaleEndpointAddress(endpointAddress);
 		try {
 			service =(FlexiScaleSoapBindingStub) locator.getFlexiScale();
 		} catch (ServiceException e) {
 			e.printStackTrace();
 			log.error("Flexiscale error: "+e.getMessage());
 			return;
 			
 		}
 		System.out.println ("user" + user);
 		System.out.println ("paas" + pass);
 		endpointAddress="https://api2.flexiscale.com/?wsdl";
 		System.out.println ("url" + endpointAddress);
 		
 		
 		service.setUsername(user);
 		service.setPassword(pass);
 		
 		
 		
 		
 		try {
 			service.listVDCs();
 			//System.out.println("VDCs");
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			log.error("Error in the authetication: "+e.getMessage());
 			return;
 		}
 		
 	}
 	
 	//@Override
 	public long deleteNetwork(String netFqn) throws IOException { //(String fqn)
 		/*UndeployNetworkTask unt=new UndeployNetworkTask(netFqn);
 		unt.execute();
 		// TODO Auto-generated method stub
 		return 0;*/
 		System.out.println("aquí llega el delete?");
 		
 		return TaskManager.getInstance().addTask(new UndeployNetworkTask(netFqn), URICreation.getVDC(netFqn)).getTaskId();
 	}
 
 	//@Override
 	public long deleteVirtualMachine(String fqn) throws IOException {
 		//YO
 		
 		/*System.out.println("imprime:" + fqn);
 		UndeployVMTask uvt=new UndeployVMTask(fqn);
 		//YO
 		System.out.println("imprimeS:" + uvt);
 		uvt.execute();
 		//YO
 		System.out.println("imprimeN:" + uvt);
 		return 0;
 		// TODO Auto-generated method stub*/
 		return TaskManager.getInstance().addTask(new UndeployVMTask(fqn), URICreation.getVDC(fqn)).getTaskId();
 	
 	}
 
 	//@Override
 	public long deployNetwork(String netFqn, String ovf) throws IOException {
 		//return TaskManager().addTask(new DeployNetworkTask(netFqn, ovf), URICreation.getVDC(netFqn)).getTaskId();
 		// TODO Auto-generated method stub
 	/*	DeployNetworkTask dnt=new DeployNetworkTask(netFqn, ovf);
 		dnt.execute();
 		return 0;*/
 		System.out.println("aquí llega?");
 		return TaskManager.getInstance().addTask(new DeployNetworkTask(netFqn, ovf), URICreation.getVDC(netFqn)).getTaskId();
 	}
 
 	//@Override
 	public long deployVirtualMachine(String fqn, String ovf) throws IOException {
 		//yo
 		/*System.out.println ("fqn" + fqn + "ovf" + ovf);
 		DeployVMTask dvt=new DeployVMTask(fqn,ovf);
 		
 		dvt.execute();
 	
 		return 0;*/
 		System.out.println("aquí llega el deployVirtualMachine?");
 		return TaskManager.getInstance().addTask(new DeployVMTask(fqn, ovf), URICreation.getVDC(fqn)).getTaskId();
 		
 	}
 	
 
 	//@Override
 	public String getNetworkList(String fqn) throws IOException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	//yo
 	public String getNetwork(String fqn) throws IOException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	//@Override
 	public String getNetworkList() throws IOException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	//@Override
 	public String getSnapshot(String fqnSnapshot) throws IOException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	//@Override
 	public String getSnapshotList(String fqnVM) throws IOException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	//@Override
 	public String getVirtualMachine(String fqn) throws IOException {
 		
		log.info("Get Virtual Machine " + fqn);
 		long server_id;
 		try {
 			 server_id = getServerId(fqn).longValue();
			 log.info("Service Id" + server_id);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			throw new IOException ("The fqn " + fqn + " does not correspond to any VM deployed"); 
 		}
		long vdc_id = 0;
		try {
			 vdc_id=getVDCId(URICreation.getVDC(fqn));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			
		}
		Server [] dd = service.listServers(vdc_id);
 		
 		for (Server ddd: dd)
 		{
 			System.out.println (ddd.getServer_id());
 		}
 		Server server = service.getServer(server_id);
 		
 		
 		
 		String ip = "";
 		for (NetworkInterface n : server.getNetwork_interfaces())
 		{
 			String ips [] = n.getIp_list();
 			if (ips != null && ips.length>0)
 			{
 				ip= ips [0];
 			}
 		}
 		String user = server.getInitial_password();
 		String password = server.getInitial_user();
 		
 		System.out.println (server.getInitial_password() + "  " + server.getInitial_user() +  " " + ip);
 		
 		
 		// obtener la cpu, ram y disco
 		
 		// obtener stado VM
 		
 		return generateXMLVEE (fqn, ip, user, password);
 
 	}
 	
 	public static void main (String [] args)
 	{
 		Properties prop = new Properties();
 		try {
 			prop.load(new FileInputStream("./conf/tcloud.properties"));
 		} catch (FileNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		FlexiscaleDriver fd=new FlexiscaleDriver(prop);
 		try {
 			fd.getVirtualMachine("es.tid.customers.cc1.services.m2.vees.vm");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	//@Override
 	public long powerActionVirtualMachine(String fqn, String action)
 			throws IOException {
 		/*System.out.println ("fqn" + fqn + "action" + action);
 		PowerActionVirtualMachineTask svt=new PowerActionVirtualMachineTask(fqn,action);
 		
 		
 		svt.execute();
 	
 		
 		// TODO Auto-generated method stub
 		return 0;*/
 		return TaskManager.getInstance().addTask(new PowerActionVirtualMachineTask(fqn, action), URICreation.getVDC(fqn)).getTaskId();
 
 	}
 
 	//@Override
 	public void removeSnapshot(String fqnSnapshot) throws IOException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	//@Override
 	public long restoreSnapshot(String fqnSnapshot) throws IOException {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	//@Override
 	public long takeSnapshot(String fqnVM, String snapshotName,
 			String snapshotDescription) throws IOException {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	
 	protected String getImageName (EnvelopeType envelope) throws SAXException, IOException, ParserConfigurationException
 	{
 		ContentType entityInstance = null;
 		try {
 			entityInstance = OVFEnvelopeUtils.getTopLevelVirtualSystemContent(envelope);
 		} catch (EmptyEnvelopeException e) {
 			e.printStackTrace();
 		}
 		if (entityInstance instanceof VirtualSystemType) {
 
 			OperatingSystemSectionType vh = null;
 			try {
 				vh = OVFEnvelopeUtils.getSection(entityInstance, OperatingSystemSectionType.class);
 			} catch (SectionNotPresentException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				log.warn("No Operative Section included");
 				return null;
 			} catch (InvalidSectionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				log.warn("No Operative Section included");
 				return null;
 			}
 			if (vh.getDescription()!=null)
 			return vh.getDescription().getValue();
 
 		} else if (entityInstance instanceof VirtualSystemCollectionType) {
 			VirtualSystemCollectionType virtualSystemCollectionType = (VirtualSystemCollectionType) entityInstance;
 			Map<String, String> balancedVEEs = new HashMap<String, String>();
 
 			for (VirtualSystemType vs : OVFEnvelopeUtils.getVirtualSystems(virtualSystemCollectionType)) {
 
 				OperatingSystemSectionType vh = null;;
 				try {
 					vh = OVFEnvelopeUtils.getSection(vs, OperatingSystemSectionType.class);
 				} catch (SectionNotPresentException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					log.warn("No Operative Section included");
 					return null;
 				} catch (InvalidSectionException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					log.warn("No Operative Section included");
 					return null;
 				}
 				if (vh.getDescription()!=null)
 				return vh.getDescription().getValue();
 				
 			}
 		}
 		return null;
 		
 			
 		/*DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db = dbf.newDocumentBuilder();
         Document document = db.parse(new ByteArrayInputStream(ovf.getBytes()));
         System.out.println("document" + document);
         document.getDocumentElement().normalize();
         System.out.println("Root element "+ document.getDocumentElement());
         System.out.println("Root element "+ document.getDocumentElement().getNodeName());
         NodeList nodeLst = document.getElementsByTagName("ovf:OperatingSystemSection");
         if (nodeLst.getLength()<1)
         	return null;
         Node fstNode = nodeLst.item(0);
         System.out.println("fstNode " + fstNode);
         Element fstElmnt = (Element) fstNode;
         System.out.println("fstElmnt " + fstElmnt);
         NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("ovf:Description");
         System.out.println("fstNmElmntLst" + fstNmElmntLst);
         Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
         System.out.println("fstNmElmnt" + fstNmElmnt);
         NodeList fstNm = fstNmElmnt.getChildNodes();
         System.out.println("Description: "+ ((Node) fstNm.item(0)).getNodeValue());
 
         String imagen = fstNm.item(0).getNodeValue();
         System.out.println("imag" + imagen);
         return imagen;*/
 	}
 	
 	protected String getNetworkName (EnvelopeType envelope) throws SAXException, IOException, ParserConfigurationException
 	{
 		
 		NetworkSectionType net = null;;
 		try {
 			net = OVFEnvelopeUtils.getSection(envelope, NetworkSectionType.class);
 		} catch (SectionNotPresentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			log.warn("No Network Section included");
 			return null;
 		} catch (InvalidSectionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			log.warn("No Network Section included");
 			return null;
 			
 		}
 		
 		for (Network network: net.getNetwork())
 		{
 			return network.getName();
 		}
 		
 		return null;
 		
 		/*DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db = dbf.newDocumentBuilder();
         Document document = db.parse(new ByteArrayInputStream(ovf.getBytes()));
         System.out.println("document" + document);
         document.getDocumentElement().normalize();
         System.out.println("Root element "+ document.getDocumentElement());
         System.out.println("Root element "+ document.getDocumentElement().getNodeName());
         NodeList nodeLst = document.getElementsByTagName("ovf:OperatingSystemSection");
         if (nodeLst.getLength()<1)
         	return null;
         Node fstNode = nodeLst.item(0);
         System.out.println("fstNode " + fstNode);
         Element fstElmnt = (Element) fstNode;
         System.out.println("fstElmnt " + fstElmnt);
         NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("ovf:Description");
         System.out.println("fstNmElmntLst" + fstNmElmntLst);
         Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
         System.out.println("fstNmElmnt" + fstNmElmnt);
         NodeList fstNm = fstNmElmnt.getChildNodes();
         System.out.println("Description: "+ ((Node) fstNm.item(0)).getNodeValue());
 
         String imagen = fstNm.item(0).getNodeValue();
         System.out.println("imag" + imagen);
         return imagen;*/
 	}
 	
 	protected Long getImageTemplateId(String imag) throws Exception {
         System.out.println("imagen " + imag + " ");
         String mapResult = null;
         long id = -1;
 
         System.out.println("ImageTemplateMap" + ImageTemplateMap);
         System.out.println(!ImageTemplateMap.containsKey(imag));
         if (!ImageTemplateMap.containsKey(imag))
                 try {
                         ImageTemplateMap = getImageTemplates();
                         System.out.println("ImageTemplateMap" + ImageTemplateMap);
                 } catch (RemoteException e) {
                         e.printStackTrace();
                         throw new Exception(e.getMessage());
 
                 }
 
         if (ImageTemplateMap.containsKey(imag))
 
                id =ImageTemplateMap.get(imag).longValue();
         Long lid = new Long(id);
         return lid;
 }
 
        protected Map<String, Long> getImageTemplates() throws RemoteException {
 
         ImageTemplate[] imageTemplate = null;
         HashMap<String, Long> mapResult = null;
 
         try {
 
                 imageTemplate = service.listImageTemplates();
                 System.out.println("imageTemplate" + imageTemplate);
                 mapResult = new HashMap<String, Long>();
                 for (ImageTemplate i : imageTemplate) {
                         mapResult.put(i.getImage_template_name(), i.getImage_template_id());
                         System.out.println("Image_template_name"+ i.getImage_template_name() + ""
                                         + " Image_template_id" + i.getImage_template_id());
                 }
         } catch (RemoteException e) {
                 e.printStackTrace();
 
         }
 
         System.out.println("return mapResult" + mapResult);
         return mapResult;
 }
        
        String generateXMLVEE (String fqn, String ip, String user, String password)
        {
     	   DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
 	        DocumentBuilder docBuilder;
 	        Document doc = null;
 	    	
 	        String organizationId =  URICreation.getOrg(fqn).replace(".", "_");
 	        String vdcid = URICreation.getVDC(fqn).substring(fqn.indexOf("customers")+"customers".length()+1);
 	        
 	        String vappid =  URICreation.getService(fqn).substring(fqn.indexOf("services")+"services".length()+1);
 	        String veeid =  URICreation.getVEE(fqn).substring(fqn.indexOf("vees")+"vees".length()+1);
 	        
 	        try {
 				docBuilder = dbfac.newDocumentBuilder();
 				
 				doc = docBuilder.newDocument();
 				
 	
 		    			
 		    	Element veeReplicaElement = doc.createElement("VApp");
 		    	doc.appendChild(veeReplicaElement);
 		    			
 		    	veeReplicaElement.setAttribute("name", fqn);
 
 		    	veeReplicaElement.setAttribute("href", "@HOSTNAME/api/org/" + organizationId + "/vdc/" +   vdcid + "/vapp/" +
 		    			vappid + "/" + veeid + "/" + 1);
 		    			
 		    	Element linkVeeReplica = doc.createElement("Link");
 		    	veeReplicaElement.appendChild(linkVeeReplica);
 		    			
 		    	linkVeeReplica.setAttribute("rel", "monitor:measures");
 		    	linkVeeReplica.setAttribute("type", "application/vnc.telefonica.tcloud. measureDescriptorList+xml");
 		    	linkVeeReplica.setAttribute("href", "@HOSTNAME/api/org/" + organizationId + "/vdc/" +   
 		    			vdcid + "/vapp/" + vappid + "/" + veeid + "/" + "1" + "/monitor");
 		    	
 		    	Element productvm = GetOperationsUtils.getInstallProductInVirtualMachine(doc, user, password);
 		    	veeReplicaElement.appendChild(productvm);
 
 		    			
 		    	Element virtuahardware = GetOperationsUtils.getVirtualHardwareSystem(doc, "@HOSTNAME/api/org/" + organizationId + "/vdc/" +  
 		    					vdcid + "/vapp/" + 
 		    					vappid +"/" + veeid + "/" + 1, 1,512,2,ip);
 		    	
 		    	veeReplicaElement.appendChild(virtuahardware);
 
 		    		
 		    	
 		    	
 		    	
 
 			} catch (Exception e) {
 				log.error("Error " + e.getMessage());
 			}
 
 			OutputFormat format    = new OutputFormat (doc); 
             // as a String
             StringWriter stringOut = new StringWriter ();    
             XMLSerializer serial   = new XMLSerializer (stringOut, 
                                                         format);
             try {
     			serial.serialize(doc);
     		} catch (IOException e) {
     			// TODO Auto-generated catch block
     			e.printStackTrace();
     		}
             // Display the XML
             System.out.println("XML " + stringOut.toString());
             return  stringOut.toString();
        }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 }
