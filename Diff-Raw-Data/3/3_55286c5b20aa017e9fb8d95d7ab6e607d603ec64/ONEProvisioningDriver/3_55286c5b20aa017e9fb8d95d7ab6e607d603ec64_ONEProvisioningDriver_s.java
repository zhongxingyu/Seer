 /*
  * Claudia Project
  * http://claudia.morfeo-project.org
  *
  * (C) Copyright 2010 Telefonica Investigacion y Desarrollo
  * S.A.Unipersonal (Telefonica I+D)
  *
  * See CREDITS file for info about members and contributors.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the Affero GNU General Public License (AGPL) as
  * published by the Free Software Foundation; either version 3 of the License,
  * or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the Affero GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  *
  * If you want to use this software an plan to distribute a
  * proprietary application in any way, and you are not licensing and
  * distributing your source code under AGPL, you probably need to
  * purchase a commercial license of the product. Please contact
  * claudia-support@lists.morfeo-project.org for more information.
  */
 package com.telefonica.claudia.smi.provisioning;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.FactoryConfigurationError;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.apache.xmlrpc.client.XmlRpcClient;
 import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
 import org.dmtf.schemas.ovf.envelope._1.ContentType;
 import org.dmtf.schemas.ovf.envelope._1.DiskSectionType;
 import org.dmtf.schemas.ovf.envelope._1.EnvelopeType;
 import org.dmtf.schemas.ovf.envelope._1.FileType;
 import org.dmtf.schemas.ovf.envelope._1.ProductSectionType;
 import org.dmtf.schemas.ovf.envelope._1.RASDType;
 import org.dmtf.schemas.ovf.envelope._1.ReferencesType;
 import org.dmtf.schemas.ovf.envelope._1.VirtualDiskDescType;
 import org.dmtf.schemas.ovf.envelope._1.VirtualHardwareSectionType;
 import org.dmtf.schemas.ovf.envelope._1.VirtualSystemCollectionType;
 import org.dmtf.schemas.ovf.envelope._1.VirtualSystemType;
 import org.dmtf.schemas.ovf.envelope._1.ProductSectionType.Property;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import com.abiquo.ovf.OVFEnvelopeUtils;
 import com.abiquo.ovf.exceptions.EmptyEnvelopeException;
 import com.abiquo.ovf.section.OVFProductUtils;
 import com.abiquo.ovf.xml.OVFSerializer;
 import com.telefonica.claudia.smi.DataTypesUtils;
 import com.telefonica.claudia.smi.Main;
 import com.telefonica.claudia.smi.TCloudConstants;
 import com.telefonica.claudia.smi.URICreation;
 import com.telefonica.claudia.smi.task.Task;
 import com.telefonica.claudia.smi.task.TaskManager;
 
 
 public class ONEProvisioningDriver implements ProvisioningDriver {
 
 	public static enum ControlActionType {shutdown, hold, release, stop, suspend, resume, finalize};
 
 	//public static enum VmStateType {INIT, PENDING, HOLD, ACTIVE, STOPPED, SUSPENDED, DONE, FAILED};
 	private final static int INIT_STATE = 0;
 	private final static int PENDING_STATE = 1;
 	private final static int HOLD_STATE = 2;
 	private final static int ACTIVE_STATE = 3;
 	private final static int STOPPED_STATE = 4;
 	private final static int SUSPENDED_STATE = 5;
 	private final static int DONE_STATE = 6;
 	private final static int FAILED_STATE = 7;
 
 	// LCM_INIT, PROLOG, BOOT, RUNNING, MIGRATE, SAVE_STOP, SAVE_SUSPEND, SAVE_MIGRATE, PROLOG_MIGRATE, EPILOG_STOP, EPILOG, SHUTDOWN, CANCEL
 	private final static int INIT_SUBSTATE = 0;
 	private final static int PROLOG_SUBSTATE = 1;
 	private final static int BOOT_SUBSTATE = 2;
 	private final static int RUNNING_SUBSTATE = 3;
 	private final static int MIGRATE_SUBSTATE = 4;
 	private final static int SAVE_STOP_SUBSTATE = 5;
 	private final static int SAVE_SUSPEND_SUBSTATE = 6;
 	private final static int SAVE_MIGRATE_SUBSTATE = 7;
 	private final static int PROLOG_MIGRATE_SUBSTATE = 8;
 	private final static int PROLOG_RESUME_SUBSTATE = 9;
 	private final static int EPILOG_STOP_SUBSTATE = 10;
 	private final static int EPILOG_SUBSTATE = 11;
 	private final static int SHUDTOWN_SUBSTATE = 12;
 	private final static int CANCEL_SUBSTATE = 13;
 
 	private HashMap<String, String> text_migrability = new HashMap();
 
 
 
 	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("com.telefonica.claudia.smi.provisioning.ONEProvisioningDriver");
 
 	// Tag names of the returning info doc for Virtual machines
 	public static final String VM_STATE = "STATE";
 	public static final String VM_SUBSTATE = "LCM_STATE";
 
 	// XMLRPC commands to access OpenNebula features
 	private final static String VM_ALLOCATION_COMMAND = "one.vm.allocate";
 	private final static String VM_UPDATE_COMMAND = "one.vm.action";
 	private final static String VM_GETINFO_COMMAND = "one.vm.info";
 	private final static String VM_GETALL_COMMAND = "one.vmpool.info";
 	private final static String VM_DELETE_COMMAND = "one.vm.delete";
 
 	private final static String NET_ALLOCATION_COMMAND = "one.vn.allocate";
 	private final static String NET_GETINFO_COMMAND = "one.vn.info";
 	private final static String NET_GETALL_COMMAND = "one.vnpool.info";
 	private final static String NET_DELETE_COMMAND = "one.vn.delete";
 
 	//private final static String DEBUGGING_CONSOLE = "RAW = [ type =\"kvm\", data =\"<devices><serial type='pty'><source path='/dev/pts/5'/><target port='0'/></serial><console type='pty' tty='/dev/pts/5'><source path='/dev/pts/5'/><target port='0'/></console></devices>\" ]";
 
 	/**
 	 * Connection URL for OpenNebula. It defaults to localhost, but can be
 	 * overriden with the property oneURL of the server configuration file.
 	 */
 	private String oneURL = "http://localhost:2633/RPC2";
 
 	/**
 	 * Server configuration file URL property identifier.
 	 */
 	private final static String URL_PROPERTY = "oneUrl";
 	private final static String USER_PROPERTY = "oneUser";
 	private final static String PASSWORD_PROPERTY = "onePassword";
 
 	private static final String KERNEL_PROPERTY = "oneKernel";
 	private static final String INITRD_PROPERTY = "oneInitrd";
 	private static final String ARCH_PROPERTY = "arch";
 	private static final String ENVIRONMENT_PROPERTY = "oneEnvironmentPath";
 
 	private final static String SSHKEY_PROPERTY = "oneSshKey";
 
 	private final static String SCRIPTPATH_PROPERTY = "oneScriptPath";
 
 	private final static String ETH0_GATEWAY_PROPERTY = "eth0Gateway";
 	private final static String ETH0_DNS_PROPERTY = "eth0Dns";
 	private final static String ETH1_GATEWAY_PROPERTY = "eth1Gateway";
 	private final static String ETH1_DNS_PROPERTY = "eth1Dns";
 
 	private final static String NET_INIT_SCRIPT0 = "netInitScript0";
 	private final static String NET_INIT_SCRIPT1 = "netInitScript1";
 
 	private String oneSession = "oneadmin:5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8";
 
 
 
 	private XmlRpcClient xmlRpcClient = null;
 
 	private static final String NETWORK_BRIDGE = "oneNetworkBridge";
 	private static final String XEN_DISK = "xendisk";
 
 
 	/**
 	 * Collection containing the mapping from fqns to ids. This mapped is used as a cache
 	 * of the getVmId method (vm ids never change once assigned).
 	 */
 	private Map<String, Integer> idVmMap = new HashMap<String, Integer>();
 
 	/**
 	 * Collection containing the mapping from fqns to ids. This mapped is used as a cache
 	 * of the getVmId method (vm ids never change once assigned).
 	 */
 	private Map<String, Integer> idNetMap = new HashMap<String, Integer>();
 	private String hypervisorInitrd="";
 	private String arch="";
 	private String hypervisorKernel="";
 	private String customizationPort;
 	private String environmentRepositoryPath;
 	private static String networkBridge="";
 	private static String xendisk="";
 	private static String oneSshKey="";
 	private static String oneScriptPath="";
 	private static String eth0Gateway="";
 	private static String eth1Gateway="";
 	private static String eth0Dns="";
 	private static String eth1Dns="";
 	private static String netInitScript0="";
 	private static String netInitScript1="";
 
 	public static final String ASSIGNATION_SYMBOL = "=";
 	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
 
 	public static final String ONE_VM_ID = "NAME";
 	public static final String ONE_VM_TYPE = "TYPE";
 	public static final String ONE_VM_STATE = "STATE";
 	public static final String ONE_VM_MEMORY = "MEMORY";
 	public static final String ONE_VM_NAME = "NAME";
 	public static final String ONE_VM_UUID = "UUID";
 	public static final String ONE_VM_CPU = "CPU";
 	public static final String ONE_VM_VCPU = "VCPU";
 
 	public static final String ONE_VM_RAW_VMI = "RAW_VMI";
 
 	public static final String ONE_VM_OS = "OS";
 	public static final String ONE_VM_OS_PARAM_KERNEL = "kernel";
 	public static final String ONE_VM_OS_PARAM_INITRD = "initrd";
 	public static final String ONE_VM_OS_PARAM_ROOT = "root";
 	public static final String ONE_VM_OS_PARAM_BOOT = "boot";
 
 	public static final String ONE_VM_GRAPHICS = "GRAPHICS";
 	public static final String ONE_VM_GRAPHICS_TYPE = "type";
 	public static final String ONE_VM_GRAPHICS_LISTEN = "listen";
 	public static final String ONE_VM_GRAPHICS_PORT = "port";
 
 	public static final String ONE_VM_DISK_COLLECTION = "DISKS";
 	public static final String ONE_VM_DISK = "DISK";
 	public static final String ONE_VM_DISK_PARAM_IMAGE = "source";
 	public static final String ONE_VM_DISK_PARAM_FORMAT = "format";
 	public static final String ONE_VM_DISK_PARAM_SIZE = "size";
 	public static final String ONE_VM_DISK_PARAM_TARGET = "target";
 	public static final String ONE_VM_DISK_PARAM_DIGEST = "digest";
 	public static final String ONE_VM_DISK_PARAM_TYPE = "type";
 	public static final String ONE_VM_DISK_PARAM_DRIVER = "driver";
 
 
 	public static final String ONE_VM_NIC_COLLECTION = "NICS";
 	public static final String ONE_VM_NIC = "NIC";
 	public static final String ONE_VM_NIC_PARAM_IP = "ip";
 	public static final String ONE_VM_NIC_PARAM_NETWORK = "NETWORK";
 
 	public static final String ONE_NET_ID = "ID";
 	public static final String ONE_NET_NAME = "NAME";
 	public static final String ONE_NET_BRIDGE = "BRIDGE";
 	public static final String ONE_NET_TYPE = "TYPE";
 	public static final String ONE_NET_ADDRESS = "NETWORK_ADDRESS";
 	public static final String ONE_NET_SIZE = "NETWORK_SIZE";
 	public static final String ONE_NET_LEASES = "LEASES";
 	public static final String ONE_NET_IP = "IP";
 	public static final String ONE_NET_MAC = "MAC";
 
 
 
 	public static final String ONE_DISK_ID = "ID";
 	public static final String ONE_DISK_NAME = "NAME";
 	public static final String ONE_DISK_URL = "URL";
 	public static final String ONE_DISK_SIZE = "SIZE";
 
 	public static final String ONE_OVF_URL = "OVF";
 	public static final String ONE_CONTEXT = "CONTEXT";
 
 	public static final String RESULT_NET_ID = "ID";
 	public static final String RESULT_NET_NAME = "NAME";
 	public static final String RESULT_NET_ADDRESS = "NETWORK_ADDRESS";
 	public static final String RESULT_NET_BRIDGE = "BRIDGE";
 	public static final String RESULT_NET_TYPE = "TYPE";
 
 	public static final String MULT_CONF_LEFT_DELIMITER = "[";
 	public static final String MULT_CONF_RIGHT_DELIMITER = "]";
 	public static final String MULT_CONF_SEPARATOR = ",";
 	public static final String QUOTE = "\"";
 
 	private static final int ResourceTypeCPU = 3;
 	private static final int ResourceTypeMEMORY = 4;
 	private static final int ResourceTypeNIC = 10;
 	private static final int ResourceTypeDISK = 17;
 
 
 	public class DeployVMTask extends Task {
 
 		public static final long POLLING_INTERVAL= 10000;
 
 		private static final int MAX_CONNECTION_ATEMPTS = 5;
 
 		String fqnVm;
 		String ovf;
 
 		public DeployVMTask(String fqn, String ovf) {
 			super();
 
 			this.fqnVm = fqn;
 			this.ovf = ovf;
 		}
 
 		@Override
 		public void execute() {
 			this.status = TaskStatus.RUNNING;
 			this.startTime = System.currentTimeMillis();
 
 			try {
 				// Create the Virtual Machine
 				String result = createVirtualMachine();
 				if (result==null) {
 					this.status= TaskStatus.ERROR;
 					this.endTime = System.currentTimeMillis();
 					return;
 				}
 
 				// Wait until the state is RUNNING
 				this.status = TaskStatus.WAITING;
 
 				int connectionAttempts=0;
 				while (true) {
 					try {
 						Document vmInfo = getVirtualMachineState(result);
 
 						Integer state = Integer.parseInt(vmInfo.getElementsByTagName(VM_STATE).item(0).getTextContent());
 						Integer subState = Integer.parseInt(vmInfo.getElementsByTagName(VM_SUBSTATE).item(0).getTextContent());
 
 						if (state == ACTIVE_STATE  && subState == RUNNING_SUBSTATE ) {
 							this.status= TaskStatus.SUCCESS;
 							this.endTime = System.currentTimeMillis();
 							break;
 						} else if (state ==FAILED_STATE) {
 							this.status= TaskStatus.ERROR;
 							this.endTime = System.currentTimeMillis();
 							break;
 						}
 
 						connectionAttempts=0;
 
 					} catch (IOException ioe) {
 						if (connectionAttempts> MAX_CONNECTION_ATEMPTS) {
 							this.status= TaskStatus.ERROR;
 							this.endTime = System.currentTimeMillis();
 							break;
 						} else
 							connectionAttempts++;
 
 						log.warn("Connection exception accessing ONE. Trying again. Error: " + ioe.getMessage());
 					}
 
 					Thread.sleep(POLLING_INTERVAL);
 				}
 
 			} catch (IOException e) {
 				log.error("Error connecting to ONE: " + e.getMessage());
 				this.error = new TaskError();
 				this.error.message = e.getMessage();
 
 				this.status = TaskStatus.ERROR;
 				this.endTime = System.currentTimeMillis();
 				return;
 			} catch (Exception e) {
 				log.error("Unexpected error creating VM: " + e.getMessage());
 				this.error = new TaskError();
 				this.error.message = e.getMessage();
 
 				this.status = TaskStatus.ERROR;
 				this.endTime = System.currentTimeMillis();
 				e.printStackTrace();
 				return;
 			}
 		}
 
 		public String createVirtualMachine() throws Exception {
 
 			List<String> rpcParams = new ArrayList<String>();
 			rpcParams.add(oneSession);
 			rpcParams.add(TCloud2ONEVM(ovf, fqnVm));
 			Object[] result = null;
 			try {
 				result = (Object[])xmlRpcClient.execute(VM_ALLOCATION_COMMAND, rpcParams);
 			} catch (XmlRpcException ex) {
 				throw new IOException ("Error on allocation of VEE replica , XMLRPC call failed: " + ex.getMessage(), ex);
 			}
 
 			boolean success = (Boolean)result[0];
 
 			if(success) {
 				log.debug("Request succeded. Returining: \n\n" + ((Integer)result[1]).toString() + "\n\n");
 				this.returnMsg = "Virtual machine internal id: " + ((Integer)result[1]).toString();
 				return ((Integer)result[1]).toString();
 			} else {
 				log.error("Error recieved from ONE: " + (String)result[1]);
 				this.error = new TaskError();
 				this.error.message = (String)result[1];
 				return null;
 			}
 		}
 	}
 
 	public class DeployNetworkTask extends Task {
 
 		String fqnNet;
 		String ovf;
 
 		public DeployNetworkTask(String netFqn, String ovf) {
 			this.fqnNet = netFqn;
 			this.ovf = ovf;
 		}
 
 		@Override
 		public void execute() {
 			this.status = TaskStatus.RUNNING;
 			this.startTime = System.currentTimeMillis();
 
 			try {
 				if (!createNetwork()) {
 					this.status= TaskStatus.ERROR;
 					return;
 				}
 
 				this.status= TaskStatus.SUCCESS;
 
 			} catch (IOException e) {
 				log.error("Error connecting to ONE: " + e.getMessage());
 				this.error = new TaskError();
 				this.error.message = e.getMessage();
 				this.endTime = System.currentTimeMillis();
 
 				this.status = TaskStatus.ERROR;
 				return;
 			} catch (Exception e) {
 				log.error("Unknown error creating network: " + e.getMessage());
 				this.error = new TaskError();
 				this.error.message = e.getMessage();
 				this.endTime = System.currentTimeMillis();
 
 				this.status = TaskStatus.ERROR;
 			}
 		}
 
 		public boolean createNetwork() throws Exception {
 
 			List<String> rpcParams = new ArrayList<String>();
 			rpcParams.add(oneSession);
 			rpcParams.add(TCloud2ONENet(ovf));
 			Object[] result = null;
 			try {
 				result = (Object[])xmlRpcClient.execute(NET_ALLOCATION_COMMAND, rpcParams);
 			} catch (XmlRpcException ex) {
 				log.error("Connection error. Could not reach ONE host: " + ex.getMessage());
 				throw new IOException ("Error on allocation of the new network , XMLRPC call failed", ex);
 			}
 
 			boolean success = (Boolean)result[0];
 
 			if(success) {
 				log.debug("Network creation request succeded: " + result[1]);
 				this.returnMsg = ((Integer)result[1]).toString();
 				return true;
 			} else {
 				log.error("Error recieved from ONE: " + (String)result[1]);
 				this.error = new TaskError();
 				this.error.message = (String)result[1];
 				return false;
 			}
 		}
 	}
 
 	public class UndeployVMTask extends Task {
 
 		String fqnVm;
 
 		public UndeployVMTask(String vmFqn) {
 			this.fqnVm = vmFqn;
 		}
 
 		@Override
 		public void execute() {
 
 			this.status= TaskStatus.RUNNING;
 			this.startTime = System.currentTimeMillis();
 
 			// Undeploy the VM
 			try {
 				String id = getVmId(fqnVm).toString();
 				deleteVirtualMachine(id);
 
 				this.status= TaskStatus.SUCCESS;
 				this.endTime = System.currentTimeMillis();
 			} catch (IOException e) {
 				log.error("Error connecting to ONE: " + e.getMessage());
 				this.error = new TaskError();
 				this.error.message = e.getMessage();
 
 				this.status = TaskStatus.ERROR;
 				this.endTime = System.currentTimeMillis();
 				return;
 			} catch (Exception e) {
 				log.error("Unknown error undeploying VM: " + e.getMessage());
 				this.error = new TaskError();
 				this.error.message = e.getMessage();
 
 				this.status = TaskStatus.ERROR;
 				this.endTime = System.currentTimeMillis();
 				return;
 			}
 		}
 
 		@SuppressWarnings("unchecked")
 		public boolean deleteVirtualMachine(String id) throws IOException {
 			List rpcParams = new ArrayList ();
 
 			ControlActionType controlAction = ControlActionType.finalize;
 			log.info("PONG deleteVirtualMachine id: "+ id);
 			rpcParams.add(oneSession);
 			rpcParams.add(controlAction.toString());
 			rpcParams.add(Integer.parseInt(id));
 
 			Object[] result = null;
 
 			try {
 				result = (Object[])xmlRpcClient.execute(VM_UPDATE_COMMAND, rpcParams);
 
 			} catch (XmlRpcException ex) {
 				log.error("Connection error trying to update VM: " + ex.getMessage());
 				throw new IOException ("Error updating VEE replica , XMLRPC call failed", ex);
 			}
 
 			if (result==null) {
 				throw new IOException("No result returned from XMLRPC call");
 			} else {
 				return (Boolean)result[0];
 			}
 		}
 	}
 
 	public class UndeployNetworkTask extends Task {
 
 		String fqnNet;
 
 		public UndeployNetworkTask(String netFqn) {
 			this.fqnNet = netFqn;
 		}
 
 		@Override
 		public void execute() {
 
 			this.status= TaskStatus.RUNNING;
 			this.startTime = System.currentTimeMillis();
 
 			// Undeploy the VM
 			try {
 				deleteNetwork(getNetId(fqnNet).toString());
 
 				this.status= TaskStatus.SUCCESS;
 				this.endTime = System.currentTimeMillis();
 			} catch (IOException e) {
 				log.error("Error connecting to ONE: " + e.getMessage());
 				this.error = new TaskError();
 				this.error.message = e.getMessage();
 
 				this.status = TaskStatus.ERROR;
 				this.endTime = System.currentTimeMillis();
 				return;
 			} catch (Exception e) {
 				log.error("Unknown error undeploying Network: " + e.getMessage());
 				this.error = new TaskError();
 				this.error.message = e.getMessage();
 
 				this.status = TaskStatus.ERROR;
 				this.endTime = System.currentTimeMillis();
 				return;
 			}
 		}
 
 		@SuppressWarnings("unchecked")
 		public void deleteNetwork(String id) throws IOException {
 
 			List rpcParams = new ArrayList<String>();
 			rpcParams.add(oneSession);
 			rpcParams.add(new Integer(id) );
 			Object[] result = null;
 			try {
 				result = (Object[])xmlRpcClient.execute(NET_DELETE_COMMAND, rpcParams);
 			} catch (XmlRpcException ex) {
 				throw new IOException ("Error deleting the network , XMLRPC call failed", ex);
 			}
 
 			boolean success = (Boolean)result[0];
 
 			if(success) {
 			} else {
 				throw new IOException("Unknown error trying to delete network: " + (String)result[1]);
 			}
 		}
 	}
 
 	public class ActionVMTask extends Task {
 
 		String fqnVM;
 		String action;
 
 		String errorMessage="";
 
 		public ActionVMTask(String fqnVM, String action) {
 			this.fqnVM = fqnVM;
 			this.action = action;
 		}
 
 		@Override
 		public void execute() {
 
 			this.status= TaskStatus.RUNNING;
 			this.startTime = System.currentTimeMillis();
 
 			// Undeploy the VM
 			try {
 				boolean result = doAction();
 
 				if (result)
 					this.status= TaskStatus.SUCCESS;
 				else {
 					this.status = TaskStatus.ERROR;
 
 					this.error = new TaskError();
 					this.error.message = errorMessage;
 				}
 
 				this.endTime = System.currentTimeMillis();
 			} catch (IOException e) {
 				log.error("Error connecting to VMWare: " + e.getMessage());
 				this.error = new TaskError();
 				this.error.message = e.getMessage();
 
 				this.status = TaskStatus.ERROR;
 				this.endTime = System.currentTimeMillis();
 				return;
 			} catch (Exception e) {
 				log.error("Unknown error executing action" + action + ": " + e.getMessage() + " -> " + e.getClass().getCanonicalName());
 				this.error = new TaskError();
 				this.error.message = e.getMessage();
 
 				this.status = TaskStatus.ERROR;
 				this.endTime = System.currentTimeMillis();
 				return;
 			}
 		}
 
 		public boolean doAction() throws Exception {
 
 			System.out.println("Executing action: " + action);
 
 			return true;
 		}
 	}
 
 
 	protected String TCloud2ONEVM(String xml, String veeFqn) throws Exception {
 
 		try {
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 			factory.setNamespaceAware(true);
 			DocumentBuilder builder = factory.newDocumentBuilder();
 			Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
 
 			if (!doc.getFirstChild().getNodeName().equals(TCloudConstants.TAG_INSTANTIATE_OVF)) {
 				log.error("Element <"+TCloudConstants.TAG_INSTANTIATE_OVF+"> not found.");
 				throw new Exception("Element <"+TCloudConstants.TAG_INSTANTIATE_OVF+"> not found.");
 			}
 
 			Element root = (Element) doc.getFirstChild();
 
 			String replicaName = root.getAttribute("name");
 
 			NodeList envelopeItems = doc.getElementsByTagNameNS("*", "Envelope");
 
 			if (envelopeItems.getLength() != 1) {
 				log.error("Envelope items not found.");
 				throw new Exception("Envelope items not found.");
 			}
 
 			// Extract the IP from the aspects section
 			Map<String, String> ipOnNetworkMap = new HashMap<String, String>();
 
 			NodeList aspects = doc.getElementsByTagNameNS("*", "Aspect");
 
 			for (int i=0; i < aspects.getLength(); i++) {
 				Element aspect = (Element) aspects.item(i);
 
 				if (aspect.getAttribute("name").equals("IP Config")) {
 
 					NodeList properties = aspect.getElementsByTagNameNS("*", "Property");
 
 					for (int j=0; j < properties.getLength(); j++) {
 						Element property = (Element) properties.item(j);
 
 						NodeList keys = property.getElementsByTagNameNS("*", "Key");
 						NodeList values = property.getElementsByTagNameNS("*", "Value");
 
 						if (keys.getLength() >0 && values.getLength()>0) {
 							ipOnNetworkMap.put(keys.item(0).getTextContent(), values.item(0).getTextContent());
 						}
 					}
 				}
 			}
 
 			// Extract the ovf sections and pass them to the OVF manager to be processed.
 			Document ovfDoc = builder.newDocument();
 			ovfDoc.appendChild(ovfDoc.importNode(envelopeItems.item(0), true));
 			OVFSerializer ovfSerializer = OVFSerializer.getInstance();
 			ovfSerializer.setValidateXML(false);
 			EnvelopeType envelope = ovfSerializer.readXMLEnvelope(new ByteArrayInputStream(DataTypesUtils.serializeXML(ovfDoc).getBytes()));
 
 			ContentType entityInstance = OVFEnvelopeUtils.getTopLevelVirtualSystemContent(envelope);
 
 			if (entityInstance instanceof VirtualSystemType) {
 
 				VirtualSystemType vs = (VirtualSystemType) entityInstance;
 
 				VirtualHardwareSectionType vh = OVFEnvelopeUtils.getSection(vs, VirtualHardwareSectionType.class);
 				String virtualizationType = vh.getSystem().getVirtualSystemType().getValue();
 
 				String scriptListProp = null;
 				String scriptListTemplate = "";
 
 				String hostname=null;
 
 				ProductSectionType productSection;
 				productSection = OVFEnvelopeUtils.getSection(vs, ProductSectionType.class);
 				try
 				{
 					Property prop = OVFProductUtils.getProperty(productSection, "SCRIPT_LIST");     			
 					scriptListProp = prop.getValue().toString();
 
 					String[] scriptList = scriptListProp.split("/");
 
 					scriptListTemplate = "";
 
 					for (String scrt: scriptList){
 
 						scriptListTemplate = scriptListTemplate + " "+oneScriptPath+"/"+scrt;
 					}
 
 
 				}
 				catch (Exception e) 
 				{
 					//TODO throw PropertyNotFoundException
 					//logger.error(e);
 					scriptListProp="";
 					scriptListTemplate = "";
 				}
 
 				String netcontext=getNetContext(vh, veeFqn,xml, scriptListProp);
 
 
 
 				try
 				{
 					Property prop = OVFProductUtils.getProperty(productSection, "HOSTNAME");     			
 					hostname = prop.getValue().toString();
 
 
 				}
 				catch (Exception e) 
 				{
 					//TODO throw PropertyNotFoundException
 					//logger.error(e);
 					hostname="";
 				}
 
 
 				StringBuffer allParametersString  = new StringBuffer();
 
 				// Migrability ....
 
 				allParametersString.append(ONE_VM_NAME).append(ASSIGNATION_SYMBOL).append(replicaName).append(LINE_SEPARATOR);
 
 				if (virtualizationType.toLowerCase().equals("kvm")) {
 
 					allParametersString.append("REQUIREMENTS").append(ASSIGNATION_SYMBOL).append("\"HYPERVISOR=\\\"kvm\\\"\"").append(LINE_SEPARATOR);
 				} else if (virtualizationType.toLowerCase().equals("xen")) {
 					allParametersString.append("REQUIREMENTS").append(ASSIGNATION_SYMBOL).append("\"HYPERVISOR=\\\"xen\\\"\"").append(LINE_SEPARATOR);
 				}
 				allParametersString.append(ONE_VM_OS).append(ASSIGNATION_SYMBOL).append(MULT_CONF_LEFT_DELIMITER);
 
 
 				String diskRoot;
 				if (virtualizationType.toLowerCase().equals("kvm")) {
 					diskRoot = "h";
 					allParametersString.append(ONE_VM_OS_PARAM_BOOT).append(ASSIGNATION_SYMBOL).append("hd").append(MULT_CONF_SEPARATOR).append(LINE_SEPARATOR);
 				} else {
 					diskRoot = xendisk;
 					allParametersString.append(ONE_VM_OS_PARAM_INITRD).append(ASSIGNATION_SYMBOL).append(hypervisorInitrd).append(MULT_CONF_SEPARATOR).append(LINE_SEPARATOR);
 					allParametersString.append(ONE_VM_OS_PARAM_KERNEL).append(ASSIGNATION_SYMBOL).append(hypervisorKernel).append(MULT_CONF_SEPARATOR).append(LINE_SEPARATOR);
 				}
 
 				if(arch.length()>0)
 					allParametersString.append("ARCH").append(ASSIGNATION_SYMBOL).append("\"").append(arch).append("\"").append(MULT_CONF_SEPARATOR).append(LINE_SEPARATOR);
 				allParametersString.append(ONE_VM_OS_PARAM_ROOT).append(ASSIGNATION_SYMBOL).append(diskRoot + "da1").append(MULT_CONF_RIGHT_DELIMITER).append(LINE_SEPARATOR);
 
 				allParametersString.append(ONE_CONTEXT).append(ASSIGNATION_SYMBOL).append(MULT_CONF_LEFT_DELIMITER);
 
 				if(hostname.length()>0) {
 					allParametersString.append("hostname  = \""+hostname+"\"").append(MULT_CONF_SEPARATOR).append(LINE_SEPARATOR);
 				}
 				allParametersString.append(netcontext);
 				allParametersString.append("public_key").append(ASSIGNATION_SYMBOL).append(oneSshKey).append(MULT_CONF_SEPARATOR).append(LINE_SEPARATOR);
 				allParametersString.append("CustomizationUrl").append(ASSIGNATION_SYMBOL).append("\"" + Main.PROTOCOL + Main.serverHost + ":" + customizationPort + "/"+ replicaName+ "\"").append(MULT_CONF_SEPARATOR).append(LINE_SEPARATOR);
 				allParametersString.append("files").append(ASSIGNATION_SYMBOL).append("\"" + environmentRepositoryPath + "/"+ replicaName + "/ovf-env.xml" +scriptListTemplate+ "\"").append(MULT_CONF_SEPARATOR).append(LINE_SEPARATOR);
 
 				allParametersString.append("target").append(ASSIGNATION_SYMBOL).append("\"" + diskRoot + "dd"+ "\"").append(MULT_CONF_RIGHT_DELIMITER).append(LINE_SEPARATOR);
 
 				if (vh.getSystem() != null && vh.getSystem().getVirtualSystemType()!= null &&
 						vh.getSystem().getVirtualSystemType().getValue() != null &&
 						vh.getSystem().getVirtualSystemType().getValue().equals("vjsc"))
 				{
 					allParametersString.append("HYPERVISOR").append(ASSIGNATION_SYMBOL).append("VJSC").append(LINE_SEPARATOR);
 				}
 
 
 				char sdaId = 'a';
 
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
 
 						//  for (int k = 0; k < quantity; k++) {
 						allParametersString.append(ONE_VM_CPU).append(ASSIGNATION_SYMBOL).append(quantity).append(LINE_SEPARATOR);
 						allParametersString.append(ONE_VM_VCPU).append(ASSIGNATION_SYMBOL).append(quantity).append(LINE_SEPARATOR);
 						//  }
 
 						break;
 
 					case ResourceTypeDISK:
 
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
 						String capacity = null;
 						String format = null;
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
 									capacity = disk.getCapacity();
 									format = disk.getFormat();
 
 									break;
 								}
 							}
 						} else {
 							throw new IllegalArgumentException("File type not supported in Disk sections.");
 						}
 
 						/* Throw exceptions in the case of missing information */
 						if (fileRef == null) {
 							log.warn("file reference can not be found for disk: " + hostRes);
 						}
 
 
 						URL url = null;
 						String digest = null;
 						String driver = null;
 
 						ReferencesType ref = envelope.getReferences();
 						List<FileType> files = ref.getFile();
 
 
 
 						for (Iterator<FileType> iteratorFl = files.iterator(); iteratorFl.hasNext();) {
 							FileType fl = iteratorFl.next();
 							if (fileRef!=null)
 							{
 
 								if (fl.getId().equals(fileRef)) {
 									try {
 										url = new URL(fl.getHref());
 									} catch (MalformedURLException e) {
 										throw new IllegalArgumentException("problems parsing disk href: " + e.getMessage());
 									}
 								}
 
 								/*
 								 * If capacity was not set using ovf:capacity in
 								 * <Disk>, try to get it know frm <File>
 								 * ovf:size
 								 */
 								if (capacity == null && fl.getSize() != null) {
 									capacity = fl.getSize().toString();
 								}
 
 								/* Try to get the digest */
 								Map<QName, String> attributesFile = fl.getOtherAttributes();
 								QName digestAtt = new QName("http://schemas.telefonica.com/claudia/ovf","digest");
 								digest = attributesFile.get(digestAtt);
 
 								Map<QName, String> attributesFile2 = fl.getOtherAttributes();
 								QName driverAtt = new QName("http://schemas.telefonica.com/claudia/ovf","driver");
 								driver = attributesFile.get(driverAtt);
 
 
 								break;
 							}
 						}
 
 						/* Throw exceptions in the case of missing information */
 						if (capacity == null) {
 							throw new IllegalArgumentException("capacity can not be set for disk " + hostRes);
 						}
 						if (url == null && fileRef!=null) {
 							throw new IllegalArgumentException("url can not be set for disk " + hostRes);
 						}
 
 						if (digest == null) {
 							log.debug("md5sum digest was not found for disk " + hostRes);
 						}
 
 
 						String urlDisk = null;
 
 						if (url != null)  
 						{
 							urlDisk = url.toString();
 
 							if (urlDisk.contains("file:/"))
 								urlDisk = urlDisk.replace("file:/", "file:///");
 						}
 
 						File filesystem = new File("/dev/" + diskRoot + "d" + sdaId);
 
 						allParametersString.append(ONE_VM_DISK).append(ASSIGNATION_SYMBOL).append(MULT_CONF_LEFT_DELIMITER);
 						if (urlDisk!= null)
 							allParametersString.append(ONE_VM_DISK_PARAM_IMAGE).append(ASSIGNATION_SYMBOL).append(urlDisk).append(MULT_CONF_SEPARATOR);
 
 						if (virtualizationType.toLowerCase().equals("kvm")) {
 							allParametersString.append(ONE_VM_DISK_PARAM_TARGET).append(ASSIGNATION_SYMBOL).append(diskRoot + "d" + sdaId).append(MULT_CONF_SEPARATOR);
 						} else
 							allParametersString.append(ONE_VM_DISK_PARAM_TARGET).append(ASSIGNATION_SYMBOL).append(filesystem.getAbsolutePath()).append(MULT_CONF_SEPARATOR);
 
 						if (format!=null)
 						{
 
 							if (format.equals("ext3"))
 							{
 								allParametersString.append(ONE_VM_DISK_PARAM_TYPE).append(ASSIGNATION_SYMBOL).append("fs").append(MULT_CONF_SEPARATOR); 
 							}
 							allParametersString.append(ONE_VM_DISK_PARAM_FORMAT).append(ASSIGNATION_SYMBOL).append(format).append(MULT_CONF_SEPARATOR);
 						}
 						if (driver!=null)
 							allParametersString.append(ONE_VM_DISK_PARAM_DRIVER).append(ASSIGNATION_SYMBOL).append(driver).append(MULT_CONF_SEPARATOR);
 
 
 
 
 						if (digest!=null)
 							allParametersString.append(ONE_VM_DISK_PARAM_DIGEST).append(ASSIGNATION_SYMBOL).append(digest).append(MULT_CONF_SEPARATOR);
 						allParametersString.append(ONE_VM_DISK_PARAM_SIZE).append(ASSIGNATION_SYMBOL).append(capacity);
 						allParametersString.append(MULT_CONF_RIGHT_DELIMITER).append(LINE_SEPARATOR);
 
 						sdaId++;
 						break;
 
 					case ResourceTypeMEMORY:
 						allParametersString.append(ONE_VM_MEMORY).append(ASSIGNATION_SYMBOL).append(quantity).append(LINE_SEPARATOR);
 						break;
 
 					case ResourceTypeNIC:
 						String fqnNet = URICreation.getService(veeFqn) + ".networks." + item.getConnection().get(0).getValue();
 						allParametersString.append(ONE_VM_NIC).append(ASSIGNATION_SYMBOL).append(MULT_CONF_LEFT_DELIMITER).append(LINE_SEPARATOR);
 
 						allParametersString.append(ONE_NET_BRIDGE).append(ASSIGNATION_SYMBOL).append(networkBridge).append(MULT_CONF_SEPARATOR).append(LINE_SEPARATOR).
 						append(ONE_VM_NIC_PARAM_NETWORK).append(ASSIGNATION_SYMBOL).append(fqnNet).append(MULT_CONF_SEPARATOR).append(LINE_SEPARATOR).
 						append(ONE_VM_NIC_PARAM_IP).append(ASSIGNATION_SYMBOL).append(ipOnNetworkMap.get(fqnNet)).append(LINE_SEPARATOR).
 						append(MULT_CONF_RIGHT_DELIMITER).append(LINE_SEPARATOR);
 
 						break;
 					default:
 						throw new IllegalArgumentException("unknown hw type: " + rsType);
 					}
 				}
 
 				//allParametersString.append(LINE_SEPARATOR).append(DEBUGGING_CONSOLE).append(LINE_SEPARATOR);
 
 
 				log.debug("VM data sent:\n\n" + allParametersString.toString() + "\n\n");
 				System.out.println("VM data sent:\n\n" + allParametersString.toString() + "\n\n");
 				return allParametersString.toString();
 
 
 			} else {
 				throw new IllegalArgumentException("OVF malformed. No VirtualSystemType found.");
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
 
 	protected static String ONEVM2TCloud(String ONETemplate) {
 		// TODO: ONE Template to TCloud translation
 		return "";
 	}
 
 	protected static String getNetContext(VirtualHardwareSectionType vh, String veeFqn,String xml, String scriptListProp) throws Exception {
 
 		//	log.debug("PONG2 xml" +xml+ "\n");
 
 		StringBuffer allParametersString  = new StringBuffer();
 
 
 
 		List<RASDType> items = vh.getItem();
 		int i=0;
 		for (Iterator<RASDType> iteratorRASD = items.iterator(); iteratorRASD.hasNext();) {
 			RASDType item = (RASDType) iteratorRASD.next();
 
 			/* Get the resource type and process it accordingly */
 			int rsType = new Integer(item.getResourceType().getValue());
 
 			int quantity = 1;
 			if (item.getVirtualQuantity() != null) {
 				quantity = item.getVirtualQuantity().getValue().intValue();
 			}
 
 			switch (rsType) {
 			case ResourceTypeNIC:
 
 				try {
 
 
 					//					log.debug("PONG eth0Dns" + eth0Dns + "\n");
 					//					log.debug("PONG eth0Gateway" + eth0Gateway + "\n");
 					//					log.debug("PONG eth1Dns" + eth1Dns + "\n");
 					//					log.debug("PONG eth1Gateway" + eth1Gateway + "\n");
 
 
 					String fqnNet = URICreation.getService(veeFqn) + ".networks." + item.getConnection().get(0).getValue();
 
 					allParametersString.append("ip_eth"+i).append(ASSIGNATION_SYMBOL).append("\"$NIC[IP, NETWORK=\\\""+fqnNet+"\\\"]\"").append(MULT_CONF_SEPARATOR).append(LINE_SEPARATOR);
 					String dns="";
 					String gateway="";
 					if(i==0){
 						dns=eth0Dns;
 						gateway=eth0Gateway;
 					}
 					if(i==1){
 						dns=eth1Dns;
 						gateway=eth1Gateway;
 					}
 
 					if(dns.length()>0)
 					{
 						allParametersString.append("dns_eth"+i).append(ASSIGNATION_SYMBOL).append(dns).append(MULT_CONF_SEPARATOR).append(LINE_SEPARATOR);
 					}
 					if(gateway.length()>0)
 					{
 						allParametersString.append("gateway_eth"+i).append(ASSIGNATION_SYMBOL).append(gateway).append(MULT_CONF_SEPARATOR).append(LINE_SEPARATOR);
 					}
 
 					i++;
 
 
 				} catch (FactoryConfigurationError e) {
 					log.error("Error retrieving parser: " + e.getMessage());
 					throw new Exception("Error retrieving parser: " + e.getMessage());
 				} catch (Exception e) {
 					log.error("Error configuring a XML Builder.");
 					throw new Exception("Error configuring a XML Builder: " + e.getMessage());
 				}
 
 				break;
 			default:
 				//throw new IllegalArgumentException("unknown hw type: " + rsType);
 			}
 
 		}
 
 		if (i==1){
 			if(netInitScript0.length()>0) {
 				allParametersString.append("SCRIPT_EXEC=\""+netInitScript0);	
 			}
 		}
 		if (i==2){
 			if(netInitScript1.length()>0) {
 				allParametersString.append("SCRIPT_EXEC=\""+netInitScript1);	
 			}
 		}
 
 		if (scriptListProp != null & scriptListProp.length()!=0)
 		{
 			String[] scriptList = scriptListProp.split("/");
 
 			String scriptListTemplate = "";
 
 			for (String scrt: scriptList){
 
 				if (scrt.indexOf(".py")!=-1)
 				{
 					if (scrt.equals("OVFParser.py")) {
 						System.out.println ("python /mnt/stratuslab/"+scrt);
 						allParametersString.append("; python /mnt/stratuslab/"+scrt+"");
 					}
 					if (scrt.equals("restful-server.py")) {
 						System.out.println ("/etc/init.d/lb_server start");
 						allParametersString.append("; /etc/init.d/lb_server start");
 					}
 					if (scrt.equals("torqueProbe.py")) {
 						System.out.println ("/etc/init.d/probe start");
 						allParametersString.append("; /etc/init.d/probe start");
 					}
 				}
 			}
 		}
 		allParametersString.append("\"").append(MULT_CONF_SEPARATOR).append(LINE_SEPARATOR);
 		return allParametersString.toString();
 
 	}
 
 	protected static String TCloud2ONENet(String xml) throws Exception {
 
 
 		try {
 			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 			Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
 
 			//	log.debug("PONG3 doc" +doc.getTextContent()+ "\n");
 
 			Element root = (Element) doc.getFirstChild();
 			String fqn = root.getAttribute(TCloudConstants.ATTR_NETWORK_NAME);
 			//	log.debug("PONG3 fqn" + fqn + "\n");
 
 			NodeList macEnabled = doc.getElementsByTagName(TCloudConstants.TAG_NETWORK_MAC_ENABLED);
 			Element firstmacenElement = (Element)macEnabled.item(0);
 			NodeList textMacenList = firstmacenElement.getChildNodes();
 			String macenabled= ((Node)textMacenList.item(0)).getNodeValue().trim();
 
 			NodeList netmaskList = doc.getElementsByTagName(TCloudConstants.TAG_NETWORK_NETMASK);
 			NodeList baseAddressList = doc.getElementsByTagName(TCloudConstants.TAG_NETWORK_BASE_ADDRESS);
 
 			StringBuffer allParametersString  = new StringBuffer();
 
 			String privateNet =  null;
 
 			if (root.getAttribute(TCloudConstants.ATTR_NETWORK_TYPE).equals("true"))
 				privateNet =   "private";
 			else
 				privateNet ="public";
 
 
 
 
 			// If there is a netmask, calculate the size of the net counting it's bits.
 			if (netmaskList.getLength() >0) {
 				Element netmask = (Element) netmaskList.item(0);
 
 				if (!netmask.getTextContent().matches("\\d+\\.\\d+\\.\\d+\\.\\d+"))
 					throw new IllegalArgumentException("Wrong IPv4 format. Expected example: 192.168.0.0 Got: " + netmask.getTextContent());
 
 				String[] ipBytes = netmask.getTextContent().split("\\.");
 
 				short[] result = new short[4];
 				for (int i=0; i < 4; i++) {
 					try {
 						result[i] = Short.parseShort(ipBytes[i]);
 						if (result[i]>255) throw new NumberFormatException("Should be in the range [0-255].");
 					} catch (NumberFormatException nfe) {
 						throw new IllegalArgumentException("Number out of bounds. Bytes should be on the range 0-255.");
 					}
 				}
 
 				// The network can host 2^n where n is the number of bits in the network address,
 				// substracting the broadcast and the network value (all 1s and all 0s).
 				int size = (int) Math.pow(2, 32.0-getBitNumber(result));
 
 				if (size < 8)
 					size = 8;
 				else
 					size -= 2;
 				if (macenabled.equals("false"))
 					allParametersString.append(ONE_NET_SIZE).append(ASSIGNATION_SYMBOL).append(size).append(LINE_SEPARATOR);
 			}
 
 			if (baseAddressList.getLength()>0) {
 				if (macenabled.equals("false"))
 					allParametersString.append(ONE_NET_ADDRESS).append(ASSIGNATION_SYMBOL).append(baseAddressList.item(0).getTextContent()).append(LINE_SEPARATOR);
 			}
 
 			// Translate the simple data to RPC format
 			allParametersString.append(ONE_NET_NAME).append(ASSIGNATION_SYMBOL).append(fqn).append(LINE_SEPARATOR);
 
 			// Add the net Type
 			if (macenabled.equals("false")) {
 				allParametersString.append(ONE_NET_TYPE).append(ASSIGNATION_SYMBOL).append("RANGED").append(LINE_SEPARATOR);
 			}
 			else {
 				allParametersString.append(ONE_NET_TYPE).append(ASSIGNATION_SYMBOL).append("FIXED").append(LINE_SEPARATOR);
 			}
 			allParametersString.append(ONE_NET_BRIDGE).append(ASSIGNATION_SYMBOL).append(networkBridge).append(LINE_SEPARATOR);
 
 
 			NodeList ipLeaseList = doc.getElementsByTagName(TCloudConstants.TAG_NETWORK_IPLEASES);
 
 
 			for (int i=0; i<ipLeaseList .getLength(); i++){
 
 				Node firstIpLeaseNode = ipLeaseList.item(i);
 				if (firstIpLeaseNode.getNodeType() == Node.ELEMENT_NODE){
 
 					Element firstIpLeaseElement = (Element)firstIpLeaseNode;
 					NodeList ipList =firstIpLeaseElement.getElementsByTagName(TCloudConstants.TAG_NETWORK_IP);
 					Element firstIpElement = (Element)ipList.item(0);
 					NodeList textIpList = firstIpElement.getChildNodes();
 					String ipString = ("IP="+((Node)textIpList.item(0)).getNodeValue().trim());
 
 					NodeList macList =firstIpLeaseElement.getElementsByTagName(TCloudConstants.TAG_NETWORK_MAC);
 					Element firstMacElement = (Element)macList.item(0);
 					NodeList textMacList = firstMacElement.getChildNodes();
 					String macString = ("MAC="+((Node)textMacList.item(0)).getNodeValue().trim());
 
 
 					allParametersString.append(ONE_NET_LEASES).append(ASSIGNATION_SYMBOL).append(MULT_CONF_LEFT_DELIMITER);
 					allParametersString.append(ipString).append(MULT_CONF_SEPARATOR).append(macString).append(MULT_CONF_RIGHT_DELIMITER).append(LINE_SEPARATOR);
 
 				}
 
 			}
 
 
 
 
 			log.debug("Network data sent:\n\n" + allParametersString.toString() + "\n\n");
 
 			return allParametersString.toString();
 
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
 
 	protected static String ONENet2TCloud(String ONETemplate) {
 		return "";
 	}
 
 	/**
 	 * Get the number of bits with value 1 in the given IP.
 	 *
 	 * @return
 	 */
 	public static int getBitNumber (short[] ip) {
 
 		if (ip == null || ip.length != 4)
 			return 0;
 
 		int bits=0;
 		for (int i=0; i < 4; i++)
 			for (int j=0; j< 15; j++)
 				bits += ( ((short)Math.pow(2, j))& ip[i]) / Math.pow(2, j);
 
 		return bits;
 	}
 
 	/**
 	 * Retrieve the virtual network id given its fqn.
 	 *
 	 * @param fqn
 	 *         FQN of the Virtual Network (mapped to its name property in ONE).
 	 *
 	 * @return
 	 *         The internal id of the Virtual Network if it exists or -1 otherwise.
 	 *
 	 * @throws Exception
 	 *
 	 */
 	protected Integer getNetId(String fqn) throws Exception {
 		if (!idNetMap.containsKey(fqn))
 			idNetMap = getNetworkIds();
 
 		if (idNetMap.containsKey(fqn))
 			return idNetMap.get(fqn);
 		else
 			return -1;
 	}
 
 	/**
 	 * Retrieve the vm's id given its fqn.
 	 *
 	 * @param fqn
 	 *         FQN of the Virtual Machine (mapped to its name property in ONE).
 	 *
 	 * @return
 	 *         The internal id of the Virtual Machine if it exists or -1 otherwise.
 	 *
 	 * @throws Exception
 	 *
 	 */
 	protected Integer getVmId(String fqn) throws Exception {
 
 		if (!idVmMap.containsKey(fqn))
 			idVmMap = getVmIds();
 
 		if (idVmMap.containsKey(fqn))
 			return idVmMap.get(fqn);
 		else
 			return -1;
 	}
 
 	/**
 	 * Retrieve a map of the currently deployed VMs, and its ids.
 	 *
 	 * @return
 	 *         A map where the key is the VM's FQN and the value the VM's id.
 	 * @throws Exception
 	 */
 	@SuppressWarnings("unchecked")
 	protected Map<String, Integer> getVmIds() throws Exception {
 
 		List rpcParams = new ArrayList<String>();
 		rpcParams.add(oneSession);
 		rpcParams.add(-2);
 
 		HashMap<String, Integer> mapResult = new HashMap<String, Integer>();
 
 		Object[] result = null;
 		try {
 			result = (Object[])xmlRpcClient.execute(VM_GETALL_COMMAND, rpcParams);
 		} catch (XmlRpcException ex) {
 			throw new IOException ("Error obtaining the VM list: " + ex.getMessage(), ex);
 		}
 
 		boolean success = (Boolean)result[0];
 
 		if(success) {
 			String resultList = (String) result[1];
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 
 			try {
 				DocumentBuilder builder = factory.newDocumentBuilder();
 				Document doc = builder.parse(new ByteArrayInputStream(resultList.getBytes()));
 
 				NodeList vmList = doc.getElementsByTagName("VM");
 
 				for (int i=0; i < vmList.getLength(); i++) {
 
 					Element vm = (Element) vmList.item(i);
 
 					String fqn = ((Element)vm.getElementsByTagName("NAME").item(0)).getTextContent();
 					try {
 						Integer value = Integer.parseInt(((Element)vm.getElementsByTagName("ID").item(0)).getTextContent());
 						mapResult.put(fqn, value);
 					} catch(NumberFormatException nfe) {
 						log.warn("Numerical id expected, got [" + ((Element)vm.getElementsByTagName("ID").item(0)).getTextContent() + "]");
 						continue;
 					}
 				}
 
 				return mapResult;
 
 			} catch (ParserConfigurationException e) {
 				log.error("Parser Configuration Error: " + e.getMessage());
 				throw new IOException ("Parser Configuration Error", e);
 			} catch (SAXException e) {
 				log.error("Parse error reading the answer: " + e.getMessage());
 				throw new IOException ("XML Parse error", e);
 			}
 
 		} else {
 			log.error("Error recieved from ONE: " + result[1]);
 			throw new Exception("Error recieved from ONE: " + result[1]);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	protected HashMap<String, Integer> getNetworkIds() throws IOException {
 		List rpcParams = new ArrayList();
 		rpcParams.add(oneSession);
 		rpcParams.add(-2);
 
 		Object[] result = null;
 		try {
 			result = (Object[])xmlRpcClient.execute(NET_GETALL_COMMAND, rpcParams);
 		} catch (XmlRpcException ex) {
 			throw new IOException ("Error obtaining the network list", ex);
 		}
 
 		boolean success = (Boolean)result[0];
 
 		if(success) {
 
 			HashMap<String, Integer> mapResult = new HashMap<String, Integer>();
 			String resultList = (String) result[1];
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 
 			try {
 				DocumentBuilder builder = factory.newDocumentBuilder();
 				Document doc = builder.parse(new ByteArrayInputStream(resultList.getBytes()));
 
 				NodeList vmList = doc.getElementsByTagName("VNET");
 
 				for (int i=0; i < vmList.getLength(); i++) {
 
 					Element vm = (Element) vmList.item(i);
 					String fqn = ((Element)vm.getElementsByTagName("NAME").item(0)).getTextContent();
 					try {
 						Integer value = Integer.parseInt(((Element)vm.getElementsByTagName("ID").item(0)).getTextContent());
 						mapResult.put(fqn, value);
 					} catch(NumberFormatException nfe) {
 						log.warn("Numerical id expected, got [" + ((Element)vm.getElementsByTagName("ID").item(0)).getTextContent() + "]");
 						continue;
 					}
 				}
 
 				return mapResult;
 
 			} catch (ParserConfigurationException e) {
 				throw new IOException ("Parser Configuration Error", e);
 			} catch (SAXException e) {
 				throw new IOException ("XML Parse error", e);
 			}
 
 		} else {
 			throw new IOException("Error recieved from ONE: " +(String)result[1]);
 		}
 	}
 
 
 	public ONEProvisioningDriver(Properties prop) {
 		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
 
 		log.info("Creating OpenNebula conector");
 
 		if (prop.containsKey(URL_PROPERTY)) {
 			oneURL = (String) prop.get(URL_PROPERTY);
 			log.info("URL created: " + oneURL);
 		}
 
 		if (prop.containsKey(USER_PROPERTY)&&prop.containsKey(PASSWORD_PROPERTY)) {
 			oneSession = ((String) prop.get(USER_PROPERTY)) + ":" + ((String) prop.get(PASSWORD_PROPERTY));
 			log.info("Session created: " + oneSession);
 		}
 
 		if (prop.containsKey(KERNEL_PROPERTY)) {
 			hypervisorKernel = ((String) prop.get(KERNEL_PROPERTY));
 		}
 
 		if (prop.containsKey(INITRD_PROPERTY)) {
 			hypervisorInitrd = ((String) prop.get(INITRD_PROPERTY));
 		}
 
 		if (prop.containsKey(ARCH_PROPERTY)) {
 			arch = ((String) prop.get(ARCH_PROPERTY));
 		}
 
 		if (prop.containsKey(Main.CUSTOMIZATION_PORT_PROPERTY)) {
 			customizationPort = ((String) prop.get(Main.CUSTOMIZATION_PORT_PROPERTY));
 		}
 
 		if (prop.containsKey(ENVIRONMENT_PROPERTY)) {
 			environmentRepositoryPath = (String) prop.get(ENVIRONMENT_PROPERTY);
 		}
 
 		if (prop.containsKey(NETWORK_BRIDGE)) {
 			networkBridge = ((String) prop.get(NETWORK_BRIDGE));
 		}
 
 		if (prop.containsKey(XEN_DISK)) {
 			xendisk = ((String) prop.get(XEN_DISK));
 		}
 
 		if (prop.containsKey(SSHKEY_PROPERTY)) {
 			oneSshKey = ((String) prop.get(SSHKEY_PROPERTY));
 		}
 
 		if (prop.containsKey(SCRIPTPATH_PROPERTY)) {
 			oneScriptPath = ((String) prop.get(SCRIPTPATH_PROPERTY));
 		}
 		if (prop.containsKey(ETH0_GATEWAY_PROPERTY)) {
 			eth0Gateway= ((String) prop.get(ETH0_GATEWAY_PROPERTY));
 		}
 		if (prop.containsKey(ETH0_DNS_PROPERTY)) {
 			eth0Dns = ((String) prop.get(ETH0_DNS_PROPERTY));
 		}
 		if (prop.containsKey(ETH1_GATEWAY_PROPERTY)) {
 			eth1Gateway = ((String) prop.get(ETH1_GATEWAY_PROPERTY));
 		}
 		if (prop.containsKey(ETH1_DNS_PROPERTY)) {
 			eth1Dns = ((String) prop.get(ETH1_DNS_PROPERTY));
 		}
 		if (prop.containsKey(NET_INIT_SCRIPT0)) {
 			netInitScript0 = ((String) prop.get(NET_INIT_SCRIPT0));
 		}
 		if (prop.containsKey(NET_INIT_SCRIPT1)) {
 			netInitScript1 = ((String) prop.get(NET_INIT_SCRIPT1));
 		}
 
 		try {
 			config.setServerURL(new URL(oneURL));
 		} catch (MalformedURLException e) {
 			log.error("Malformed URL: " + oneURL);
 			throw new RuntimeException(e);
 		}
 
 		xmlRpcClient = new XmlRpcClient();
 		log.info("XMLRPC client created");
 		xmlRpcClient.setConfig(config);
 		log.info("XMLRPC client configured");
 
 		/* MIGRABILITY TAG */
 		text_migrability.put("cross-host", "HOST");
 		text_migrability.put("cross-sitehost", "SITE");
 		text_migrability.put("none", "NONE");
 
 		//  FULL??
 	}
 
 	@SuppressWarnings("unchecked")
 	public Document getVirtualMachineState(String id) throws IOException {
 
 		List rpcParams = new ArrayList ();
 		rpcParams.add(oneSession);
 		rpcParams.add(new Integer(id));
 
 		log.debug("Virtual machine info requested for id: " + id);
 
 		Object[] result = null;
 		try {
 			result = (Object[])xmlRpcClient.execute(VM_GETINFO_COMMAND, rpcParams);
 		} catch (XmlRpcException ex) {
 			log.error("Connection error trying to get VM information: " + ex.getMessage());
 			throw new IOException ("Error on reading VM state , XMLRPC call failed", ex);
 		}
 
 		boolean completed = (Boolean) result[0];
 
 		if (completed) {
 
 			String resultList = (String) result[1];
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 
 			// RESERVOIR ONLY: the info cames with a XML inside the element RAW_VMI, WITH HEADERS
 			if (resultList.contains("<RAW_VMI>")) {
 				resultList = resultList.replace(resultList.substring(resultList.indexOf("<RAW_VMI>"), resultList.indexOf("</RAW_VMI>") + 10), "");
 			}
 
 			try {
 				DocumentBuilder builder = factory.newDocumentBuilder();
 				Document doc = builder.parse(new ByteArrayInputStream(resultList.getBytes()));
 
 				log.debug("VM Info request succeded");
 				return doc;
 
 			} catch (ParserConfigurationException e) {
 				log.error("Error configuring parser: " + e.getMessage());
 				throw new IOException ("Parser Configuration Error", e);
 			} catch (SAXException e) {
 				log.error("Parse error obtaining info: " + e.getMessage());
 				throw new IOException ("XML Parse error", e);
 			}
 
 		} else {
 			log.error("VM Info request failed: " + result[1]);
 			return null;
 		}
 	}
 
 	public String getAtributeVirtualSystem(VirtualSystemType vs, String attribute) throws NumberFormatException {
 		Iterator itr = vs.getOtherAttributes().entrySet().iterator();
 		while (itr.hasNext()) {
 			Map.Entry e = (Map.Entry)itr.next();
 
 			if ((e.getKey()).equals(new QName ("http://schemas.telefonica.com/claudia/ovf", attribute)))
 				return (String)e.getValue();
 
 
 		}
 		return "";
 
 	}
 
 	public ArrayList<VirtualSystemType> getVirtualSystem (EnvelopeType envelope) throws Exception {
 
 		ContentType entityInstance = null;
 		ArrayList<VirtualSystemType> virtualSystems = new ArrayList ();
 		try {
 			entityInstance = OVFEnvelopeUtils.getTopLevelVirtualSystemContent(envelope);
 		} catch (EmptyEnvelopeException e) {
 
 			log.error(e);
 		}
 
 		HashMap<String,VirtualSystemType> virtualsystems =  new HashMap();
 
 		if (entityInstance instanceof VirtualSystemType) {
 
 			virtualSystems.add((VirtualSystemType)entityInstance);
 
 		} else if (entityInstance instanceof VirtualSystemCollectionType) {
 			VirtualSystemCollectionType virtualSystemCollectionType = (VirtualSystemCollectionType) entityInstance;
 
 			for (VirtualSystemType vs : OVFEnvelopeUtils.getVirtualSystems(virtualSystemCollectionType))
 			{
 
 
 				virtualSystems.add(vs);
 			}
 
 		}//End for
 		return virtualSystems;
 	}
 
 
 
 
 	@Override
 	public long deleteNetwork(String netFqn) throws IOException {
 		return TaskManager.getInstance().addTask(new UndeployNetworkTask(netFqn), URICreation.getVDC(netFqn)).getTaskId();
 	}
 
 	@Override
 	public long deleteVirtualMachine(String vmFqn) throws IOException {
 		return TaskManager.getInstance().addTask(new UndeployVMTask(vmFqn), URICreation.getVDC(vmFqn)).getTaskId();
 	}
 
 	@Override
 	public long deployNetwork(String netFqn, String ovf) throws IOException {
 		return TaskManager.getInstance().addTask(new DeployNetworkTask(netFqn, ovf), URICreation.getVDC(netFqn)).getTaskId();
 	}
 
 	@Override
 	public long deployVirtualMachine(String fqn, String ovf) throws IOException {
 		return TaskManager.getInstance().addTask(new DeployVMTask(fqn, ovf), URICreation.getVDC(fqn)).getTaskId();
 	}
 
 	public long powerActionVirtualMachine(String fqn, String action) throws IOException {
 		return TaskManager.getInstance().addTask(new ActionVMTask(fqn, action), URICreation.getVDC(fqn)).getTaskId();
 	}
 
 
 	public String getNetwork(String fqn) throws IOException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String getNetworkList() throws IOException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String getVirtualMachine(String fqn) throws IOException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 }
