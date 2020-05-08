 package virt.vmware.vcli;
 
 import java.io.*;
 import java.net.*;
 
 import javax.net.ssl.*;
 import javax.xml.bind.*;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.xml.sax.SAXException;
 
 import virt.vmware.vcli.soap.*;
 import virt.vmware.vcli.soap.send.*;
 
 import com.vmware.vim25.*;
 import com.vmware.vim25.mo.*;
 
 
 import java.rmi.RemoteException;
 import java.security.*;
 import java.security.cert.*;
 import java.util.*;
 import java.util.concurrent.*;
 
 
 public final class VIClient
 {
 	private String url;
 	private URLConnection connection;
 	private VICookieManager vicm;
 	
 	private ServiceInstance si;
 	
 	public final static String VICLI_USERAGENT = "VMware VI Client/4.0.0";
 	
 	static
 	{
 		System.setProperty("http.keepAlive", "true");
 		System.setProperty("http.maxConnections", "200");
 		/**
 		 * Do *NOT* set our custom VICookieManager as the default CookieManager, it breaks VIJava API calls
 		 * Use VICookieManager just for custom calls with VIClient.send(SOAPMessage)
 		 * - cm = new CookieManager();
 		 * - cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
 		 * - CookieHandler.setDefault(cm);
 		 */
 		
 		// Create a trust manager that does not validate certificate chains
 		TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager()
 		{
 				public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
 				public void checkClientTrusted(X509Certificate[] certs, String authType) { }
 				public void checkServerTrusted(X509Certificate[] certs, String authType) { }
 			}
 		};
 		try
 		{
 			// Install the all-trusting trust manager
 			SSLContext sc = SSLContext.getInstance("SSL");
 			sc.init(null, trustAllCerts, new java.security.SecureRandom());
 			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 		} catch (NoSuchAlgorithmException nsae) {
 			nsae.printStackTrace();
 		} catch (KeyManagementException kme) {
 			kme.printStackTrace();
 		}
 
 		// Create all-trusting host name verifier
 		HostnameVerifier allHostsValid = new HostnameVerifier()
 		{
 			public boolean verify(String hostname, SSLSession session) { return true; }
 		};
 
 		// Install the all-trusting host verifier
 		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
 	}
 	
 	public VIClient(String url) throws VIClientException
 	{
 		try {
 			new URL(url);
 			this.url = url;
 			this.vicm = new VICookieManager();
 		} catch (MalformedURLException murle) {
 			throw new VIClientException("Invalid URL format '" + url + "'.");
 		}
 	}
 	
 	private URLConnection connect() throws IOException
 	{
 		connection = new URL(url).openConnection();
 		connection.setDoInput(true);
 		connection.setDoOutput(true);
 		connection.setRequestProperty("User-Agent", VICLI_USERAGENT);
 		connection.setRequestProperty("SOAPAction", "\"urn:internalvim25/4.1\"");
 		connection.setRequestProperty("Connection", "Keep-Alive");
 		connection.setRequestProperty("Content-Type", "text/xml; charset=\"utf-8\"");
 		
 		vicm.setCookies(connection);
 		
 		connection.connect();
 		
 		return connection;
 	}
 	
 	@SuppressWarnings("unused")
 	private void disconnect() throws IOException
 	{
 		connection.getOutputStream().close();
 		connection.getInputStream().close();
 	}
 
 	/**
 	 * Logs in the VIClient to the esxi system
 	 * 
 	 * @param username Username used to log in to the esxi system
 	 * @param password Password used to log in to the esxi system
 	 * @throws VIClientException
 	 */
 	public void login(String username, String password) throws VIClientException
 	{
 		try
 		{
 			SOAPMessage message = new SOAPMessage();
 			message.header = null;
 			message.body.content = new Login(username, password);
 			send(message);
 			org.w3c.dom.Node node = (org.w3c.dom.Node) xpath(connection.getInputStream(),
 					"//LoginResponse/returnval/userName",
 					javax.xml.xpath.XPathConstants.NODE
 					);
 			try
 			{
 				if(!username.equals(node.getTextContent()))
 					throw new VIClientException("Login unsuccessful.");
 			} catch (NullPointerException npe) {
 				throw new VIClientException("Login unsuccessful.");
 			}
 			si = new ServiceInstance(new URL(url), username, password, true); //TODO Maybe remove dependency on VIJava libs
 		} catch (ConnectException ce) {
 			ce.printStackTrace();
 			throw new VIClientException("Login unsuccessful");
 		} catch (RemoteException re) {
 			re.printStackTrace();
 			throw new VIClientException("Login unsuccessful");
 		} catch (MalformedURLException murle) {
 			murle.printStackTrace();
 			throw new VIClientException("Login unsuccessful");
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 			throw new VIClientException("Login unsuccessful");
 		} catch (VIClientException vice) {
 			vice.printStackTrace();
 			throw new VIClientException("Login unsuccessful");
 		}
 	}
 
 	/**
 	 * Logs out from the esxi system
 	 * 
 	 * @throws VIClientException
 	 */
 	public void logout() throws VIClientException
 	{
 		try
 		{
 			SOAPMessage message = new SOAPMessage();
 			message.body.content = new Logout();
 			send(message);
 			org.w3c.dom.Node node = (org.w3c.dom.Node) xpath(connection.getInputStream(),
 					"//LogoutResponse",
 					javax.xml.xpath.XPathConstants.NODE
 					);
 			if(node == null)
 			{
 				throw new VIClientException("Invalid LogoutResponse from server.");
 			} else {
 				return;
 			}
 		} catch (Exception e) {
 			org.w3c.dom.Node node = (org.w3c.dom.Node) xpath(((HttpURLConnection)connection).getErrorStream(),
 					"//faultstring",
 					javax.xml.xpath.XPathConstants.NODE
 					);
 			if(node != null)
 			{
 				throw new VIClientException(node.getTextContent());
 			} else {
 				throw new VIClientException("Invalid FaultResponse from server.");
 			}
 		}
 	}
 
 	/**
 	 * Mounts a NAS Datastore
 	 * 
 	 * @param host  Remote host address exporting the NFS share ("192.168.1.50")
 	 * @param share NFS share name ("/backup")
 	 * @param name  Name of mounted Datastore ("nfs-backup")
 	 * @return Name of mounted Datastore
 	 * @throws VIClientException
 	 */
 	public String mount(String host, String share, String name) throws VIClientException
 	{
 		try
 		{
 			SOAPMessage message = new SOAPMessage();
 			CreateNasDatastore query = new CreateNasDatastore(host,
 					share,
 					name);
 			message.body.content = query;
 			send(message);
 			org.w3c.dom.Node node = (org.w3c.dom.Node) xpath(connection.getInputStream(),
 					"//CreateNasDatastoreResponse/returnval[@type='Datastore']",
 					javax.xml.xpath.XPathConstants.NODE
 					);
 			if(node == null)
 			{
 				throw new VIClientException("Invalid CreateNasDatastoreResponse from server.");
 			} else {
 				return node.getTextContent();
 			}
 		} catch (IOException ioe)
 		{
 			org.w3c.dom.Node node = (org.w3c.dom.Node) xpath(((HttpURLConnection)connection).getErrorStream(),
 					"//faultstring",
 					javax.xml.xpath.XPathConstants.NODE
 					);
 			if(node != null)
 			{
 				// Check wheter Datastore is already mounted: in this case we can ignore this exception
 				Datastore ds = null;
 				InventoryNavigator in = new InventoryNavigator(si.getRootFolder());
 				try {
 					for(com.vmware.vim25.mo.ManagedEntity me : in.searchManagedEntities("Datastore"))
 					{
 						if(!(me instanceof Datastore))
 							continue;
 						com.vmware.vim25.mo.Datastore ds0 = (com.vmware.vim25.mo.Datastore)me;
 						if(ds0.getName().equals(name))
 						{
 							ds = ds0;
 							break;
 						}
 					}
 				} catch (Exception e) {
 					throw new VIClientException(e);
 				}
 				if(ds != null)
 					return ds.getName();
 				throw new VIClientException(node.getTextContent());
 			} else {
 				throw new VIClientException("Invalid FaultResponse from server.");
 			}
 		}
 	}
 
 	/**
 	 * Unmounts a NAS Datastore
 	 * 
 	 * @param datastore Datastore to be unmounted ("192.168.1.50:/backup")
 	 * @return Name of unmounted Datastore
 	 * @throws VIClientException
 	 */
 	public String unmount(String datastore) throws VIClientException
 	{
 		try
 		{
 			SOAPMessage message = new SOAPMessage();
 			RemoveDatastore query = new RemoveDatastore(datastore);
 			message.body.content = query;
 			send(message);
 			org.w3c.dom.Node node = (org.w3c.dom.Node) xpath(connection.getInputStream(),
 					"//RemoveDatastoreResponse",
 					javax.xml.xpath.XPathConstants.NODE
 					);
 			if(node == null)
 			{
 				throw new VIClientException("Invalid RemoveDatastoreResponse from server.");
 			} else {
 				return node.getTextContent();
 			}
 		} catch (IOException ioe)
 		{
 			org.w3c.dom.Node node = (org.w3c.dom.Node) xpath(((HttpURLConnection)connection).getErrorStream(),
 					"//faultstring",
 					javax.xml.xpath.XPathConstants.NODE
 					);
 			if(node != null)
 			{
 				throw new VIClientException(node.getTextContent());
 			} else {
 				throw new VIClientException("Invalid FaultResponse from server.");
 			}
 		}
 	}
 	
 	/**
 	 * Find a VM in the Inventory
 	 * 
 	 * @param name The name of the VM to be found
 	 * @param uuid The UUID of the VM to be found
 	 * @throws VIClientException
 	 */
 	public boolean findvm(String name, String uuid) throws VIClientException
 	{
 		boolean found = false;
 		try
 		{
 			String vmid = null;
 			InventoryNavigator in = new InventoryNavigator(si.getRootFolder());
 			for(com.vmware.vim25.mo.ManagedEntity me : in.searchManagedEntities("VirtualMachine"))
 			{
 				if(!(me instanceof VirtualMachine))
 					continue;
 				com.vmware.vim25.mo.VirtualMachine vm = (com.vmware.vim25.mo.VirtualMachine)me;
 				if(vm.getName().equalsIgnoreCase(name) || vm.getConfig().getUuid().equalsIgnoreCase(uuid))
 				{
 					vmid = vm.getSummary().getVm().get_value();
 					break;
 				}
 			}
 			if(vmid != null)
 				return true;
 		} catch (IOException ioe) {
 			throw new VIClientException(ioe.getMessage());
 		}
 		return found;
 	}
 	
 	/**
 	 * Close a HttpNfcLease session
 	 * 
 	 * @param sessionId The NFC session Id
 	 * @throws VIClientException
 	 */
 	public void nfsclose(String sessionId) throws VIClientException
 	{
 		try
 		{
 			SOAPMessage message = new SOAPMessage();
 			HttpNfcLeaseComplete query = new HttpNfcLeaseComplete(sessionId);
 			message.body.content = query;
 			send(message);
 			org.w3c.dom.Node node = (org.w3c.dom.Node) xpath(connection.getInputStream(),
 					"//HttpNfcLeaseCompleteResponse",
 					javax.xml.xpath.XPathConstants.NODE
 					);
 			if(node == null)
 			{
 				throw new VIClientException("Invalid HttpNfcLeaseCompleteResponse from server.");
 			}
 		} catch (IOException ioe)
 		{
 			org.w3c.dom.Node node = (org.w3c.dom.Node) xpath(((HttpURLConnection)connection).getErrorStream(),
 					"//faultstring",
 					javax.xml.xpath.XPathConstants.NODE
 					);
 			if(node != null)
 			{
 				throw new VIClientException(node.getTextContent());
 			} else {
 				throw new VIClientException("Invalid FaultResponse from server.");
 			}
 		}
 	}
 	
 	/**
 	 * Export a VM from the Inventory
 	 * 
 	 * @param vmName The name of the VM to be exported
 	 * @return An ExportVMResponse
 	 * @throws VIClientException
 	 */
 	public ExportVMResponse exportvm(String vmName) throws VIClientException
 	{
 		try
 		{
 			String vmid = null;
 			InventoryNavigator in = new InventoryNavigator(si.getRootFolder());
 			for(com.vmware.vim25.mo.ManagedEntity me : in.searchManagedEntities("VirtualMachine"))
 			{
 				if(!(me instanceof VirtualMachine))
 					continue;
 				com.vmware.vim25.mo.VirtualMachine vm = (com.vmware.vim25.mo.VirtualMachine)me;
 				if(vm.getName().equalsIgnoreCase(vmName))
 				{
 					vmid = vm.getSummary().getVm().get_value();
 					break;
 				}
 			}
 			
 			// Is the VM present on the remote server?
 			if(vmid == null)
 				throw new VIClientException("VirtualMachine '" + vmName + "' was not found in the Inventory.");
 			
 			String ovfDescriptor = "";
 			String nfcSession = "";
 			ExportVMResponse.HttpNfcLease.Manifest nfcManifest = null;
 			ExportVMResponse.HttpNfcLease.Info nfcInfo = null;
 			
 			// Get OVF Descriptor
 			{
 				SOAPMessage message = new SOAPMessage();
 				CreateDescriptor query = new CreateDescriptor(vmid);
 				message.body.content = query;
 				send(message);
 				org.w3c.dom.Node node = (org.w3c.dom.Node) xpath(connection.getInputStream(),
 						"//CreateDescriptorResponse/returnval/ovfDescriptor",
 						javax.xml.xpath.XPathConstants.NODE
 						);
 				if(node == null)
 				{
 					throw new VIClientException("Invalid CreateDescriptorResponse from server.");
 				} else {
 					ovfDescriptor = node.getTextContent();
 				}
 			}
 			
 			// Get HttpNfc Lease
 			{
 				SOAPMessage message = new SOAPMessage();
 				ExportVm query = new ExportVm(vmid);
 				message.body.content = query;
 				send(message);
 				org.w3c.dom.Node node = (org.w3c.dom.Node) xpath(connection.getInputStream(),
 						"//ExportVmResponse/returnval[@type='HttpNfcLease']",
 						javax.xml.xpath.XPathConstants.NODE
 						);
 				if(node == null)
 				{
 					throw new VIClientException("Invalid ExportVmResponse from server.");
 				} else {
 					nfcSession = node.getTextContent();
 				}
 			}
 			
 			// Wait for HttpNfc Lease to be "ready"
 			int tries = 0;
 			String nfcState = "";
 			while(true)
 			{
 				SOAPMessage message = new SOAPMessage();
 				RetrieveProperties query = new RetrieveProperties();
 				query.specs.props.type = "HttpNfcLease";
 				query.specs.props.all = false;
 				query.specs.props.paths.add(new RetrieveProperties.pathSet("state"));
 				RetrieveProperties.objectSet o = new RetrieveProperties.objectSet();
 				o.obj.type = "HttpNfcLease";
 				o.obj.value = nfcSession;
 				o.skip = false;
 				query.specs.objs.add(o);
 				message.body.content = query;
 				send(message);
 				org.w3c.dom.Node node = (org.w3c.dom.Node) xpath(connection.getInputStream(),
 						"//RetrievePropertiesResponse/returnval/propSet/val[@type='HttpNfcLeaseState']",
 						javax.xml.xpath.XPathConstants.NODE
 						);
 				if(node == null)
 				{
 					throw new VIClientException("Invalid RetrievePropertiesResponse from server.");
 				} else {
 					nfcState = node.getTextContent();
 				}
 				if(nfcState.equalsIgnoreCase("ready"))
 					break;
 				if(tries++ > 5)
 				{
 					throw new VIClientException("Timeout while waiting for HttpNfcLease to be ready.");
 				} else
 				{
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException ie) {
 						throw new VIClientException("InterruptedException while waiting for HttpNfcLease to be ready.");
 					}
 				}
 			}
 			
 			// Get HttpNfcLease Manifest
 			{
 				SOAPMessage message = new SOAPMessage();
 				HttpNfcLeaseGetManifest query = new HttpNfcLeaseGetManifest(nfcSession);
 				message.body.content = query;
 				send(message);
 				org.w3c.dom.NodeList nodes = (org.w3c.dom.NodeList) xpath(connection.getInputStream(),
 						"//HttpNfcLeaseGetManifestResponse/returnval",
 						javax.xml.xpath.XPathConstants.NODESET
 						);
 				if(nodes == null)
 				{
 					throw new VIClientException("Invalid HttpNfcLeaseGetManifestResponse from server.");
 				} else {
 					/**
 					 * Do we really need this info?
 					 */
 //					for(int index = 0; index < nodes.getLength(); index++)
 //						System.out.println(nodes.item(index).getTextContent());
 					nfcManifest = new ExportVMResponse.HttpNfcLease.Manifest(new HashSet<ExportVMResponse.HttpNfcLease.Manifest.Entry>());
 				}
 			}
 			
 			// Get HttpNfcLease Info
 			{
 				SOAPMessage message = new SOAPMessage();
 				RetrieveProperties query = new RetrieveProperties();
 				query.specs.props.type = "HttpNfcLease";
 				query.specs.props.all = false;
 				query.specs.props.paths.add(new RetrieveProperties.pathSet("info"));
 				RetrieveProperties.objectSet o = new RetrieveProperties.objectSet();
 				o.obj.type = "HttpNfcLease";
 				o.obj.value = nfcSession;
 				o.skip = false;
 				query.specs.objs.add(o);
 				message.body.content = query;
 				send(message);
 				org.w3c.dom.NodeList nodes = (org.w3c.dom.NodeList) xpath(connection.getInputStream(),
 						"//RetrievePropertiesResponse/returnval/propSet/val[@type='HttpNfcLeaseInfo']/deviceUrl",
 						javax.xml.xpath.XPathConstants.NODESET
 						);
 				if(nodes == null)
 				{
 					throw new VIClientException("Invalid HttpNfcLeaseGetManifestResponse from server.");
 				} else {
 					try {
 						javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
 						factory.setNamespaceAware(false);
 						javax.xml.parsers.DocumentBuilder builder;
 						org.w3c.dom.Document docManifest = null;
 						javax.xml.xpath.XPathExpression expr = null;
 						builder = factory.newDocumentBuilder();
 						docManifest = builder.parse(new ByteArrayInputStream(ovfDescriptor.getBytes()));
 						javax.xml.xpath.XPathFactory xfactory = javax.xml.xpath.XPathFactory.newInstance();
 						
 						Set<ExportVMResponse.HttpNfcLease.Info.DeviceUrl> urls = new HashSet<ExportVMResponse.HttpNfcLease.Info.DeviceUrl>();
 						for(int index = 0; index < nodes.getLength(); index++)
 						{
 							try
 							{
 								String key;
 								String targetId;
 								org.w3c.dom.Node node = nodes.item(index);
 								javax.xml.xpath.XPath xpath = xfactory.newXPath();
 								ExportVMResponse.HttpNfcLease.Info.DeviceUrl url = new ExportVMResponse.HttpNfcLease.Info.DeviceUrl(
 								key =      ((org.w3c.dom.Node)     xpath.compile("key").evaluate(node, javax.xml.xpath.XPathConstants.NODE)).getTextContent(),
 								           ((org.w3c.dom.Node)     xpath.compile("importKey").evaluate(node, javax.xml.xpath.XPathConstants.NODE)).getTextContent(),
 								           ((org.w3c.dom.Node)     xpath.compile("url").evaluate(node, javax.xml.xpath.XPathConstants.NODE)).getTextContent()
								              .replaceAll("^http(s)?:\\/\\/\\*\\/ha-nfc\\/", connection.getURL().getProtocol() + "://" + connection.getURL().getHost() + "/ha-nfc/"),
 								           ((org.w3c.dom.Node)     xpath.compile("sslThumbprint").evaluate(node, javax.xml.xpath.XPathConstants.NODE)).getTextContent(),
 								           Boolean.getBoolean(
 								              ((org.w3c.dom.Node)  xpath.compile("disk").evaluate(node, javax.xml.xpath.XPathConstants.NODE)).getTextContent()),
 								targetId = ((org.w3c.dom.Node)     xpath.compile("targetId").evaluate(node, javax.xml.xpath.XPathConstants.NODE)).getTextContent(),
 								           ((org.w3c.dom.Node)     xpath.compile("datastoreKey").evaluate(node, javax.xml.xpath.XPathConstants.NODE)).getTextContent()
 								);
 								urls.add(url);
 								
 								/**
 								 *  OVF Manifest need manual patching from:
 								 *  
 								 * <File ovf:href="/16/ParaVirtualSCSIController0:0" ovf:id="file1"/>
 								 * <File ovf:href="/16/ParaVirtualSCSIController3:0" ovf:id="file2"/>
 								 * 
 								 * To:
 								 * 
 								 * <File ovf:href="VM-disk1.vmdk" ovf:id="file1"/>
 								 * <File ovf:href="VM-disk2.vmdk" ovf:id="file2"/>
 								 */
 								expr = xfactory.newXPath().compile("//Envelope/References/File[@href='" + key + "']");
 								org.w3c.dom.Node ovffile = (org.w3c.dom.Node) expr.evaluate(docManifest, javax.xml.xpath.XPathConstants.NODE);
 								org.w3c.dom.NamedNodeMap attrs = ovffile.getAttributes();
 								attrs.getNamedItem("ovf:href").setNodeValue(targetId);
 								
 							} catch (XPathExpressionException xpathe) {
 								throw new VIClientException(xpathe);
 							}
 						}
 						nfcInfo = new ExportVMResponse.HttpNfcLease.Info(urls);
 						
 						// org.w3c.dom.Document -> String
 						Transformer transformer = TransformerFactory.newInstance().newTransformer();
 						StreamResult result = new StreamResult(new StringWriter());
 						DOMSource source = new DOMSource(docManifest);
 						transformer.transform(source, result);
 						ovfDescriptor = result.getWriter().toString();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			
 			return new ExportVMResponse(ovfDescriptor,
 				new ExportVMResponse.HttpNfcLease(
 					nfcSession,
 					nfcManifest,
 					nfcInfo));
 		} catch (IOException ioe) {
 			org.w3c.dom.Node node = (org.w3c.dom.Node) xpath(((HttpURLConnection)connection).getErrorStream(),
 					"//faultstring",
 					javax.xml.xpath.XPathConstants.NODE
 					);
 			if(node != null)
 			{
 				throw new VIClientException(node.getTextContent());
 			} else {
 				throw new VIClientException("Invalid FaultResponse from server.");
 			}
 		}
 	}
 	
 	/**
 	 * Download a file VMware network file copy (NFC) protocol.
 	 * 
 	 * @param nfcurl  The URL to download from ("http://esxi/nfc-session-ticket/disk-0.vmdk")
 	 * @param dest    Destination file ("/path/to/vmfiles/disk-0.vmdk")
 	 * @throws VIClientException
 	 */
 	public void nfcget(String nfcurl, File dest) throws VIClientException
 	{
 		try
 		{
 			URLConnection connection = new URL(nfcurl).openConnection();
 			connection.setRequestProperty("User-Agent", VICLI_USERAGENT);
 			InputStream in = connection.getInputStream();
 			OutputStream out = new FileOutputStream(dest);
 			byte[] buf = new byte[102400];
 			int len = 0;
 			while ((len = in.read(buf)) > 0) 
 			{
 				out.write(buf, 0, len);
 			}
 			in.close();
 			out.close();
 		} catch (IOException ioe)
 		{
 			throw new VIClientException(ioe.getMessage());
 		}
 	}
 	
 	/**
 	 * Power off a VM
 	 * 
 	 * @param name The name of the VM to be powered off
 	 * @param uuid The UUID of the VM to be powered off
 	 * @throws VIClientException
 	 */
 	public void poweroff(String name, String uuid) throws VIClientException
 	{
 		try
 		{
 			VirtualMachine vmTarget = null;
 			String vmid = null;
 			InventoryNavigator in = new InventoryNavigator(si.getRootFolder());
 			for(com.vmware.vim25.mo.ManagedEntity me : in.searchManagedEntities("VirtualMachine"))
 			{
 				if(!(me instanceof VirtualMachine))
 					continue;
 				com.vmware.vim25.mo.VirtualMachine vm = (com.vmware.vim25.mo.VirtualMachine)me;
 				if(vm.getName().equalsIgnoreCase(name) || vm.getConfig().getUuid().equalsIgnoreCase(uuid))
 				{
 					if(vm.getRuntime().getPowerState() != VirtualMachinePowerState.poweredOn)
 						throw new RuntimeException("VirtualMachine '" + name + "' is not powered on.");
 					vmTarget = vm;
 					vmid = vm.getSummary().getVm().get_value();
 					break;
 				}
 			}
 			
 			// Is the VM present on the remote server?
 			if(vmid == null)
 				throw new VIClientException("VirtualMachine '" + name + "' was not found in the Inventory.");
 			
 			SOAPMessage message = new SOAPMessage();
 			PowerOffVM_Task query = new PowerOffVM_Task(vmid); 
 			message.body.content = query;
 			send(message);
 			consume();
 			
 			final VirtualMachine vm = vmTarget;
 			ExecutorService exec = Executors.newSingleThreadExecutor();
 		    final Callable<VirtualMachinePowerState> call = new Callable<VirtualMachinePowerState>() {
 		        @Override
 		        public VirtualMachinePowerState call()
 		        {
 		        	while(vm.getRuntime().getPowerState() != VirtualMachinePowerState.poweredOff)
 		        		try { Thread.sleep(1000); } catch (InterruptedException ie) {}
 		        	return vm.getRuntime().getPowerState();
 		        }
 		    };
 		    final Future<VirtualMachinePowerState> future = exec.submit(call);
 		    try {
 		        future.get(5000, TimeUnit.MILLISECONDS);
 		    } catch (TimeoutException te) {
 		    	throw new VIClientException("TimeoutException while powering off VirtualMachine '" + name + "'.");
 		    } catch (InterruptedException ie) {
 		    	throw new VIClientException("InterruptedException while powering off VirtualMachine '" + name + "'.");
 			} catch (ExecutionException ee) {
 				throw new VIClientException("ExecutionException while powering off VirtualMachine '" + name + "'.");
 			}
 		} catch (IOException ioe) {
 			throw new VIClientException(ioe.getMessage());
 		}
 	}
 	
 	/**
 	 * Shutdown a VM (Guest tools must be installed and running)
 	 * 
 	 * @param name The name of the VM to be shutdown
 	 * @param uuid The UUID of the VM to be shutdown
 	 * @throws VIClientException
 	 */
 	public void shutdown(String name, String uuid, int timeout) throws VIClientException
 	{
 		try
 		{
 			VirtualMachine vmTarget = null;
 			String vmid = null;
 			InventoryNavigator in = new InventoryNavigator(si.getRootFolder());
 			for(com.vmware.vim25.mo.ManagedEntity me : in.searchManagedEntities("VirtualMachine"))
 			{
 				if(!(me instanceof VirtualMachine))
 					continue;
 				com.vmware.vim25.mo.VirtualMachine vm = (com.vmware.vim25.mo.VirtualMachine)me;
 				if(vm.getName().equalsIgnoreCase(name) || vm.getConfig().getUuid().equalsIgnoreCase(uuid))
 				{
 					if(vm.getRuntime().getPowerState() != VirtualMachinePowerState.poweredOn)
 						throw new RuntimeException("VirtualMachine '" + name + "' is not powered on.");
 					vmTarget = vm;
 					String guestState = vm.getGuest().getGuestState();
 					if(!guestState.equals("running"))
 						throw new VIClientException("VirtualMachine '" + name + "' tools state is '" + guestState + "'.");
 					vmid = vm.getSummary().getVm().get_value();
 					break;
 				}
 			}
 			
 			// Is the VM present on the remote server?
 			if(vmid == null)
 				throw new VIClientException("VirtualMachine '" + name + "' was not found in the Inventory.");
 			
 			SOAPMessage message = new SOAPMessage();
 			ShutdownGuest query = new ShutdownGuest(vmid); 
 			message.body.content = query;
 			send(message);
 			consume();
 			
 			final VirtualMachine vm = vmTarget;
 			ExecutorService exec = Executors.newSingleThreadExecutor();
 		    final Callable<VirtualMachinePowerState> call = new Callable<VirtualMachinePowerState>() {
 		        @Override
 		        public VirtualMachinePowerState call()
 		        {
 		        	while(vm.getRuntime().getPowerState() != VirtualMachinePowerState.poweredOff)
 		        		try { Thread.sleep(1000); } catch (InterruptedException ie) {}
 		        	return vm.getRuntime().getPowerState();
 		        }
 		    };
 		    final Future<VirtualMachinePowerState> future = exec.submit(call);
 		    try {
 		        future.get(timeout, TimeUnit.MILLISECONDS);
 		    } catch (TimeoutException te) {
 		    	throw new VIClientException("TimeoutException while shutting down VirtualMachine '" + name + "'.");
 		    } catch (InterruptedException ie) {
 		    	throw new VIClientException("InterruptedException while shutting down VirtualMachine '" + name + "'.");
 			} catch (ExecutionException ee) {
 				throw new VIClientException("ExecutionException while shutting down VirtualMachine '" + name + "'.");
 			}
 		} catch (IOException ioe) {
 			throw new VIClientException(ioe.getMessage());
 		}
 	}
 	
 	/**
 	 * Power on a VM
 	 * 
 	 * @param name The name of the VM to be powered on
 	 * @param uuid The UUID of the VM to be powered on
 	 * @throws VIClientException
 	 */
 	public void poweron(String name, String uuid) throws VIClientException
 	{
 		try
 		{
 			VirtualMachine vmTarget = null;
 			String vmid = null;
 			InventoryNavigator in = new InventoryNavigator(si.getRootFolder());
 			for(com.vmware.vim25.mo.ManagedEntity me : in.searchManagedEntities("VirtualMachine"))
 			{
 				if(!(me instanceof VirtualMachine))
 					continue;
 				com.vmware.vim25.mo.VirtualMachine vm = (com.vmware.vim25.mo.VirtualMachine)me;
 				if(vm.getName().equalsIgnoreCase(name) || vm.getConfig().getUuid().equalsIgnoreCase(uuid))
 				{
 					if(vm.getRuntime().getPowerState() != VirtualMachinePowerState.poweredOff)
 						throw new VIClientException("VirtualMachine '" + name + "' is not powered off.");
 					vmTarget = vm;
 					vmid = vm.getSummary().getVm().get_value();
 					break;
 				}
 			}
 			
 			// Is the VM present on the remote server?
 			if(vmid == null)
 				throw new VIClientException("VirtualMachine '" + name + "' was not found in the Inventory.");
 			
 			SOAPMessage message = new SOAPMessage();
 			PowerOnVM_Task query = new PowerOnVM_Task(vmid); 
 			message.body.content = query;
 			send(message);
 			consume();
 			
 			final VirtualMachine vm = vmTarget;
 			ExecutorService exec = Executors.newSingleThreadExecutor();
 		    final Callable<VirtualMachinePowerState> call = new Callable<VirtualMachinePowerState>() {
 		        @Override
 		        public VirtualMachinePowerState call()
 		        {
 		        	while(vm.getRuntime().getPowerState() != VirtualMachinePowerState.poweredOn)
 		        		try { Thread.sleep(1000); } catch (InterruptedException ie) {}
 		        	return vm.getRuntime().getPowerState();
 		        }
 		    };
 		    final Future<VirtualMachinePowerState> future = exec.submit(call);
 		    try {
 		        future.get(25000, TimeUnit.MILLISECONDS);
 		    } catch (TimeoutException te) {
 		    	throw new VIClientException("TimeoutException while powering on VirtualMachine '" + name + "'.");
 		    } catch (InterruptedException ie) {
 		    	throw new VIClientException("InterruptedException while powering on VirtualMachine '" + name + "'.");
 			} catch (ExecutionException ee) {
 				throw new VIClientException("ExecutionException while powering on VirtualMachine '" + name + "'.");
 			}
 		} catch (IOException ioe) {
 			throw new VIClientException(ioe.getMessage());
 		}
 	}
 	
 	/**
 	 * Extract the FileLayout of a VM
 	 * 
 	 * @param vmname Target VM
 	 * @return Data containing VM FileLayout in this format:
 	 * 		Set<Object[]> {
 	 * 			Object[] { String:Name, String:Type, Integer:Size }
 	 * 		}
 	 * @throws VIClientException
 	 */
 	public Set<Object[]> layoutvm(String name, String uuid) throws VIClientException
 	{
 		try
 		{
 			Set<Object[]> data = new HashSet<Object[]>();
 			
 			InventoryNavigator in = new InventoryNavigator(si.getRootFolder());
 			for(com.vmware.vim25.mo.ManagedEntity me : in.searchManagedEntities("VirtualMachine"))
 			{
 				if(!(me instanceof VirtualMachine))
 					continue;
 				com.vmware.vim25.mo.VirtualMachine vm = (com.vmware.vim25.mo.VirtualMachine)me;
 				if(vm.getName().equalsIgnoreCase(name) || vm.getConfig().getUuid().equalsIgnoreCase(uuid))
 				{
 					vm.refreshStorageInfo();
 					
 					Map<Integer, Long> sizekb = new HashMap<Integer, Long>();
 					for(VirtualDevice vdev : vm.getConfig().getHardware().getDevice())
 					{
 						if(!(vdev instanceof VirtualDisk))
 							continue;
 						VirtualDisk vdisk = (VirtualDisk) vdev;
 						for(VirtualMachineFileLayoutExDiskLayout disklayout : vm.getLayoutEx().getDisk())
 						{
 							if(vdisk.getKey() == disklayout.getKey())
 							for(VirtualMachineFileLayoutExDiskUnit diskunit : disklayout.getChain())
 							{
 								for(int filekey : diskunit.getFileKey())
 								{
 									sizekb.put(filekey, vdisk.getCapacityInKB() * 1024);
 								}
 							}
 						}
 					}
 					
 					VirtualMachineFileLayoutEx vmfl = vm.getLayoutEx();
 					for(VirtualMachineFileLayoutExFileInfo vmflefi : vmfl.getFile())
 					{
 						data.add(new Object[]{
 							vmflefi.getName(),
 							vmflefi.getType(),
 							(vmflefi.getType().equals("diskExtent") ||
 							 vmflefi.getType().equals("diskDescriptor")) ? sizekb.get(vmflefi.getKey()) : vmflefi.getSize()
 						});
 					}
 					break;
 				}
 			}
 			return data;
 		} catch (Exception e) {
 			throw new VIClientException(e);
 		}
 	}
 	
 	/**
 	 * Copy a file.
 	 * To copy vmdk files use VIClient.copydisk(String, String) instead.
 	 * 
 	 * @param src  Source file ("[local-datastore] VM1/VM1.vmx")
 	 * @param dest Destination file ("[backup-datastore] VM1/VM1.vmx")
 	 * @return Copy_TaskResponse id
 	 * @throws VIClientException
 	 */
 	public String copyfile(String src, String dest) throws VIClientException
 	{
 		try
 		{
 			SOAPMessage message = new SOAPMessage();
 			Copy_Task query = new Copy_Task(src,
 					dest,
 					Copy_Task.FileType.File);
 			query.force = true;
 			message.body.content = query;
 			send(message);
 			org.w3c.dom.Node node = (org.w3c.dom.Node) xpath(connection.getInputStream(),
 					"//Copy_TaskResponse/returnval[@type='Task']",
 					javax.xml.xpath.XPathConstants.NODE
 					);
 			if(node == null)
 			{
 				throw new VIClientException("Invalid Copy_TaskResponse from server.");
 			} else {
 				return node.getTextContent();
 			}
 		} catch (IOException ioe)
 		{
 			org.w3c.dom.Node node = (org.w3c.dom.Node) xpath(((HttpURLConnection)connection).getErrorStream(),
 					"//faultstring",
 					javax.xml.xpath.XPathConstants.NODE
 					);
 			if(node != null)
 			{
 				throw new VIClientException(node.getTextContent());
 			} else {
 				throw new VIClientException("Invalid FaultResponse from server.");
 			}
 		}
 	}
 	
 	/**
 	 * Copy a Virtual Disk file (vmdk)
 	 * 
 	 * @param src  Source vmdk ("[local-datastore] VM1/VM1.vmdk")
 	 * @param dest Destination vmdk ("backup-datastore] VM1/VM1.vmdk")
 	 * @return Copy_TaskResponse id
 	 * @throws VIClientException
 	 */
 	public String copydisk(String src, String dest) throws VIClientException
 	{
 		try
 		{
 			SOAPMessage message = new SOAPMessage();
 			Copy_Task query = new Copy_Task(src,
 					dest,
 					Copy_Task.FileType.VirtualDisk);
 			query.force = false;
 			message.body.content = query;
 			send(message);
 			org.w3c.dom.Node node = (org.w3c.dom.Node) xpath(connection.getInputStream(),
 					"//Copy_TaskResponse/returnval[@type='Task']",
 					javax.xml.xpath.XPathConstants.NODE
 					);
 			if(node == null)
 			{
 				throw new VIClientException("Invalid Copy_TaskResponse from server.");
 			} else {
 				return node.getTextContent();
 			}
 		} catch (IOException ioe)
 		{
 			org.w3c.dom.Node node = (org.w3c.dom.Node) xpath(((HttpURLConnection)connection).getErrorStream(),
 					"//faultstring",
 					javax.xml.xpath.XPathConstants.NODE
 					);
 			if(node != null)
 			{
 				throw new VIClientException(node.getTextContent());
 			} else {
 				throw new VIClientException("Invalid FaultResponse from server.");
 			}
 		}
 	}
 	
 	/**
 	 * Creates a directory on the specified Datastore
 	 * 
 	 * @param datastore Target Datastore ("[datastore]")
 	 * @param name      Name of the directory to be created ("VM1")
 	 * @throws VIClientException
 	 */
 	public void mkdir(String datastore, String name) throws VIClientException
 	{
 		try
 		{
 			SOAPMessage message = new SOAPMessage();
 			MakeDirectory query = new MakeDirectory(datastore, name); 
 			message.body.content = query;
 			send(message);
 			org.w3c.dom.Node node = (org.w3c.dom.Node) xpath(connection.getInputStream(),
 					"//MakeDirectoryResponse",
 					javax.xml.xpath.XPathConstants.NODE
 					);
 			if(node == null)
 			{
 				throw new VIClientException("Invalid MakeDirectoryResponse from server.");
 			}
 		} catch (IOException ioe)
 		{
 			org.w3c.dom.Node node = (org.w3c.dom.Node) xpath(((HttpURLConnection)connection).getErrorStream(),
 					"//faultstring",
 					javax.xml.xpath.XPathConstants.NODE
 					);
 			if(node != null)
 			{
 				throw new VIClientException(node.getTextContent());
 			} else {
 				throw new VIClientException("Invalid FaultResponse from server.");
 			}
 		}
 	}
 	
 	/**
 	 * Delete a file from a Datastore
 	 * 
 	 * @param path File path ("[datastore] PATH/TO/FILE.vmx")
 	 * @throws VIClientException
 	 */
 	public String rmfile(String path) throws VIClientException
 	{
 		try
 		{
 			SOAPMessage message = new SOAPMessage();
 			Delete_Task query = new Delete_Task(path, Delete_Task.FileType.File);
 			message.body.content = query;
 			send(message);
 			org.w3c.dom.Node node = (org.w3c.dom.Node) xpath(connection.getInputStream(),
 					"//Delete_TaskResponse/returnval[@type='Task']",
 					javax.xml.xpath.XPathConstants.NODE
 					);
 			if(node == null)
 			{
 				throw new VIClientException("Invalid Delete_TaskResponse from server.");
 			} else {
 				return node.getTextContent();
 			}
 		} catch (IOException ioe)
 		{
 			org.w3c.dom.Node node = (org.w3c.dom.Node) xpath(((HttpURLConnection)connection).getErrorStream(),
 					"//faultstring",
 					javax.xml.xpath.XPathConstants.NODE
 					);
 			if(node != null)
 			{
 				throw new VIClientException(node.getTextContent());
 			} else {
 				throw new VIClientException("Invalid FaultResponse from server.");
 			}
 		}
 	}
 	
 	/**
 	 * Delete a disk file from a Datastore
 	 * 
 	 * @param path File path ("[datastore] PATH/TO/FILE.vmdk")
 	 * @throws VIClientException
 	 */
 	public String rmdisk(String path) throws VIClientException
 	{
 		try
 		{
 			SOAPMessage message = new SOAPMessage();
 			Delete_Task query = new Delete_Task(path, Delete_Task.FileType.VirtualDisk);
 			message.body.content = query;
 			send(message);
 			org.w3c.dom.Node node = (org.w3c.dom.Node) xpath(connection.getInputStream(),
 					"//Delete_TaskResponse/returnval[@type='Task']",
 					javax.xml.xpath.XPathConstants.NODE
 					);
 			if(node == null)
 			{
 				throw new VIClientException("Invalid Delete_TaskResponse from server.");
 			} else {
 				return node.getTextContent();
 			}
 		} catch (IOException ioe)
 		{
 			org.w3c.dom.Node node = (org.w3c.dom.Node) xpath(((HttpURLConnection)connection).getErrorStream(),
 					"//faultstring",
 					javax.xml.xpath.XPathConstants.NODE
 					);
 			if(node != null)
 			{
 				throw new VIClientException(node.getTextContent());
 			} else {
 				throw new VIClientException("Invalid FaultResponse from server.");
 			}
 		}
 	}
 	
 	/**
 	 * Check a Task status
 	 * 
 	 * @param taskid The Task it to be polled
 	 * @return Whether the task is completed
 	 * @throws VIClientException
 	 */
 	public boolean polltask(String taskid) throws VIClientException
 	{
 		SOAPMessage message = new SOAPMessage();
 		RetrieveProperties query = new RetrieveProperties();
 		query.specs.props.type = "Task";
 		query.specs.props.all = false;
 		query.specs.props.paths.add(new RetrieveProperties.pathSet("info"));
 		RetrieveProperties.objectSet o = new RetrieveProperties.objectSet();
 		o.obj.type = "Task";
 		o.obj.value = taskid;
 		o.skip = false;
 		query.specs.objs.add(o);
 		message.body.content = query;
 		send(message);
 
 		try
 		{
 			InputStream is = connection.getInputStream();
 			byte buff[] = new byte[0x800];
 			int read;
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			while((read = is.read(buff)) != -1)
 				baos.write(buff, 0, read);
 			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray()); //FIXME expect OOM Exception here on low-end boxes
 			
 			org.w3c.dom.Node node = (org.w3c.dom.Node) xpath(bais,
 					"//RetrievePropertiesResponse/returnval/propSet/val/completeTime",
 					javax.xml.xpath.XPathConstants.NODE
 					);
 			if(node == null)
 			{
 				return false;
 			} else {
 				// Task is completed. Did it fail?
 				bais.reset();
 				org.w3c.dom.Node node2 = (org.w3c.dom.Node) xpath(bais,
 						"//error/localizedMessage",
 						javax.xml.xpath.XPathConstants.NODE
 						);
 				if(node2 != null)
 				{
 					throw new VIClientException(node2.getTextContent());
 				} else {
 					return true;
 				}
 			}
 		} catch (IOException ioe)
 		{
 			org.w3c.dom.Node node = (org.w3c.dom.Node) xpath(((HttpURLConnection)connection).getErrorStream(),
 					"//faultstring",
 					javax.xml.xpath.XPathConstants.NODE
 					);
 			if(node != null)
 			{
 				throw new VIClientException(node.getTextContent());
 			} else {
 				throw new VIClientException("Invalid FaultResponse from server.");
 			}
 		}
 	}
 	
 	private void send(SOAPMessage message) throws VIClientException
 	{
 		try
 		{
 			connection = connect();
 
 			OutputStream out = connection.getOutputStream();
 			/**
 			 * Testing UTF-8 BOM
 			 * Nothing to see here, move along ...
 			 * - out.write(0xef);
 			 * - out.write(0xbb);
 			 * - out.write(0xbf);
 			*/
 			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes());
 
 			JAXBContext context = JAXBContext.newInstance( SOAPMessage.class );
 		    Marshaller m = context.createMarshaller();
 		    m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
 		    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
 		    m.marshal( message , out );
 		    out.flush();
 		    
 		    vicm.storeCookies(connection);
 
 		    SOAPSession.operationNum ++;
 		} catch (JAXBException jaxbe) {
 			jaxbe.printStackTrace();
 			throw new VIClientException("Send(JAXB) Exception");
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 			throw new VIClientException("Send(IO) Exception");
 		}
 	}
 	
 	//TODO Remove this method ASAP
 	@SuppressWarnings("unused")
 	private void consume() throws IOException
 	{
 	    int read;
 	    byte buff[] = new byte[0x800];
 	    InputStream in = connection.getInputStream();
 	    while((read = in.read(buff)) != -1)
 	    	;
 	}
 	
 	private Object xpath(InputStream in, String path, javax.xml.namespace.QName type) throws VIClientException
 	{
 		try {
 			// Standard of reading a XML file
 			javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
 			factory.setNamespaceAware(false);
 			javax.xml.parsers.DocumentBuilder builder;
 			org.w3c.dom.Document doc = null;
 			javax.xml.xpath.XPathExpression expr = null;
 			builder = factory.newDocumentBuilder();
 			doc = builder.parse(in);
 	
 			// Create a XPathFactory
 			javax.xml.xpath.XPathFactory xFactory = javax.xml.xpath.XPathFactory.newInstance();
 	
 			// Create a XPath object
 			javax.xml.xpath.XPath xpath = xFactory.newXPath();
 	
 			// Compile the XPath expression
 			expr = xpath.compile(path);
 			// Run the query and get a nodeset
 			Object result = expr.evaluate(doc, type);
 			
 			return result;
 		} catch (ParserConfigurationException pce) {
 			pce.printStackTrace();
 			throw new VIClientException("XPath(ParserConfiguration) Exception");
 		} catch (XPathExpressionException xpee) {
 			xpee.printStackTrace();
 			throw new VIClientException("XPath(XPathExpression) Exception");
 		} catch (SAXException saxe) {
 			saxe.printStackTrace();
 			throw new VIClientException("XPath(SAX) Exception");
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 			throw new VIClientException("XPath(IO) Exception");
 		}
 	}
 	
 	/**
 	 * Unlimited Nesting Works
 	 */
 	public static final class ExportVMResponse
 	{
 		public static final class HttpNfcLease
 		{
 			public static final class Manifest
 			{
 				public static final class Entry
 				{
 					public final String key;
 					public final String sha1;
 					public final long size;
 					public final boolean disk;
 					
 					private Entry(String key, String sha1, long size, boolean disk)
 					{
 						this.key = key;
 						this.sha1 = sha1;
 						this.size = size;
 						this.disk = disk;
 					}
 				}
 				
 				public final Set<Manifest.Entry> entries;
 				
 				private Manifest(Set<Manifest.Entry> entries)
 				{
 					this.entries = entries;
 				}
 			}
 			public static final class Info
 			{
 				public static final class DeviceUrl
 				{
 					public final String key;
 					public final String importKey;
 					public final String url;
 					public final String sslThumbprint;
 					public final boolean disk;
 					public final String targetId;
 					public final String datastoreKey;
 					
 					private DeviceUrl(String key,
 							String importKey,
 							String url,
 							String sslThumbprint,
 							boolean disk,
 							String targetId,
 							String datastoreKey)
 					{
 						this.key = key;
 						this.importKey = importKey;
 						this.url = url;
 						this.sslThumbprint = sslThumbprint;
 						this.disk = disk;
 						this.targetId = targetId;
 						this.datastoreKey = datastoreKey;
 					}
 				}
 				
 				public final Set<Info.DeviceUrl> urls;
 				
 				private Info(Set<Info.DeviceUrl> urls)
 				{
 					this.urls = urls;
 				}
 			}
 			
 			public final String sessionId;
 			public final Manifest nfcManifest;
 			public final Info nfcInfo;
 			
 			private HttpNfcLease(String sessionId, Manifest manifest, Info info)
 			{
 				this.sessionId = sessionId;
 				this.nfcManifest = manifest;
 				this.nfcInfo = info;
 			}
 		}
 		
 		public final String ovfDescriptor;
 		public final HttpNfcLease nfcLease;
 		
 		private ExportVMResponse(String descriptor, HttpNfcLease lease)
 		{
 			ovfDescriptor = descriptor;
 			nfcLease = lease;
 		}
 	}
 }
