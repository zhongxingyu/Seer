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
 	
 	OneOperations operations = null;
 	OneNetUtilities netUtils = null;
 
 
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
 	
 	public static final String ONE_VERSION = "ONEVERSION";
 
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
 	
 	private String oneversion = "2.2";
 
 
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
 
 			String idvm = null; 
 			try
 			{
 			   idvm = operations.deployVirtualMachine (ovf, fqnVm );
 			   this.returnMsg = "Virtual machine internal id: " + idvm;
 			}
 			catch (Exception e)
 			{
 				this.error = new TaskError();
 				this.error.message = e.getMessage();
 				
 			}
 			return idvm;
 
 		}
 	}
 
 	public class DeployNetworkTask extends Task {
 
 		String fqnNet;
 		String ovf;
 
 		public DeployNetworkTask(String netFqn, String ovf) {
 			this.fqnNet = netFqn;
 			this.ovf = ovf;
 		//	execute();
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
 
 			String idvm = null; 
 			String xml = netUtils.TCloud2ONENet(ovf);
 			try
 			{
 			   idvm = operations.deployNetwork(xml);
 			   this.returnMsg = "Virtual network machine internal id: " + idvm;
 			}
 			catch (Exception e)
 			{
 				this.error = new TaskError();
 				this.error.message = e.getMessage();
 				return false;
 				
 			}
 			return true;
 			
 			
 			
 		/*	List<String> rpcParams = new ArrayList<String>();
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
 			}*/
 		}
 	}
 
 	public class UndeployVMTask extends Task {
 
 		String fqnVm;
 
 		public UndeployVMTask(String vmFqn) {
 			this.fqnVm = vmFqn;
 			execute() ;
 		}
 
 		@Override
 		public void execute() {
 
 			this.status= TaskStatus.RUNNING;
 			this.startTime = System.currentTimeMillis();
 
 			// Undeploy the VM
 			try {
 				String id = getVmId(fqnVm).toString();
 			//	deleteVirtualMachine(id);
 				
 				operations.deleteVirtualMachine(id);
 
 				this.status= TaskStatus.SUCCESS;
 				this.endTime = System.currentTimeMillis();
 			} catch (IOException e) {
 				System.out.println ( e.getMessage());
 				log.error("Error connecting to ONE: " + e.getMessage());
 				this.error = new TaskError();
 				this.error.message = e.getMessage();
 
 				this.status = TaskStatus.ERROR;
 				this.endTime = System.currentTimeMillis();
 				return;
 			} catch (Exception e) {
 				System.out.println ( e.getMessage());
 				log.error("Unknown error undeploying VM: " + e.getMessage());
 				this.error = new TaskError();
 				this.error.message = e.getMessage();
 
 				this.status = TaskStatus.ERROR;
 				this.endTime = System.currentTimeMillis();
 				return;
 			}
 		}
 
 	/*	@SuppressWarnings("unchecked")
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
 		}*/
 	}
 
 	public class UndeployNetworkTask extends Task {
 
 		String fqnNet;
 
 		public UndeployNetworkTask(String netFqn) {
 			this.fqnNet = netFqn;
 			execute() ;
 		}
 
 		@Override
 		public void execute() {
 
 			this.status= TaskStatus.RUNNING;
 			this.startTime = System.currentTimeMillis();
 
 			// Undeploy the VM
 			try {
 				//deleteNetwork(getNetId(fqnNet).toString());
 				
 				operations.deleteNetwork(getNetId(fqnNet).toString());
 
 				this.status= TaskStatus.SUCCESS;
 				this.endTime = System.currentTimeMillis();
 			} catch (IOException e) {
 				log.error("Error connecting to ONE: " + e.getMessage());
 				this.error = new TaskError();
 				this.error.message = e.getMessage();
 				System.out.println ( e.getMessage());
 
 				this.status = TaskStatus.ERROR;
 				this.endTime = System.currentTimeMillis();
 				System.out.println ( e.getMessage());
 				return;
 			} catch (Exception e) {
 				log.error("Unknown error undeploying Network: " + e.getMessage());
 				this.error = new TaskError();
 				this.error.message = e.getMessage();
 				System.out.println ( e.getMessage());
 
 				this.status = TaskStatus.ERROR;
 				this.endTime = System.currentTimeMillis();
 				return;
 			}
 		}
 
 /*		@SuppressWarnings("unchecked")
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
 		}*/
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
 				boolean result = operations.doAction(getVmId(fqnVM).toString(), action);
 
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
 		
 	}
 
 
 
 
 	
 
 	protected static String ONEVM2TCloud(String ONETemplate) {
 		// TODO: ONE Template to TCloud translation
 		return "";
 	}
 
 /*	protected static String getNetContext(VirtualHardwareSectionType vh, String veeFqn,String xml, String scriptListProp) throws Exception {
 
 		//	log.debug("PONG2 xml" +xml+ "\n");
 
 		StringBuffer allParametersString  = new StringBuffer();
 
 
 
 		List<RASDType> items = vh.getItem();
 		int i=0;
 		for (Iterator<RASDType> iteratorRASD = items.iterator(); iteratorRASD.hasNext();) {
 			RASDType item = (RASDType) iteratorRASD.next();
 
 			// Get the resource type and process it accordingly 
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
 
 		StringBuffer scriptexec=new StringBuffer();;
 		if (i==1){
 			if(netInitScript0.length()>0) {
 				scriptexec.append("SCRIPT_EXEC=\""+netInitScript0);	
 			}
 		}
 		if (i==2){
 			if(netInitScript1.length()>0) {
 				scriptexec.append("SCRIPT_EXEC=\""+netInitScript1);	
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
 						scriptexec.append("; python /mnt/stratuslab/"+scrt+"");
 					}
 					if (scrt.equals("restful-server.py")) {
 						System.out.println ("/etc/init.d/lb_server start");
 						scriptexec.append("; /etc/init.d/lb_server start");
 					}
 					if (scrt.equals("torqueProbe.py")) {
 						System.out.println ("/etc/init.d/probe start");
 						scriptexec.append("; /etc/init.d/probe start");
 					}
 				}
 			}
 		}
 		if (scriptexec.length()>0){
 			scriptexec.append("\"").append(MULT_CONF_SEPARATOR).append(LINE_SEPARATOR);
 		}
 		else {
 			scriptexec.append("");
 		}
 		allParametersString.append(scriptexec);
 		return allParametersString.toString();
 
 	}
 */
 	/*protected  String TCloud2ONENet(String xml) throws Exception {
 
 
 		try {
 			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 			Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
 
 
 			Element root = (Element) doc.getFirstChild();
 			String fqn = root.getAttribute(TCloudConstants.ATTR_NETWORK_NAME);
 			StringBuffer allParametersString  = new StringBuffer();
 
 			NodeList macEnabled = doc.getElementsByTagName(TCloudConstants.TAG_NETWORK_MAC_ENABLED);
 			Element firstmacenElement = (Element)macEnabled.item(0);
 			String macenabled = null;
 			if (firstmacenElement!=null)
 			{
 				NodeList textMacenList = firstmacenElement.getChildNodes();
 				if (((Node)textMacenList.item(0))!=null)
 					macenabled= ((Node)textMacenList.item(0)).getNodeValue().trim();
 			}
 
 			NodeList netmaskList = doc.getElementsByTagName(TCloudConstants.TAG_NETWORK_NETMASK);
 			NodeList baseAddressList = doc.getElementsByTagName(TCloudConstants.TAG_NETWORK_BASE_ADDRESS);
 
 			NodeList ipLeaseList = doc.getElementsByTagName(TCloudConstants.TAG_NETWORK_IPLEASES);
 
 			if (baseAddressList.getLength()==0)
 			{
 				allParametersString.append(getTCloud2FixedONENet (fqn));
 			}
 			else if (ipLeaseList.getLength()==0)
 			{
 				int size = 0;
 				if (netmaskList.getLength() >0)
 				{
 					size = getSizeNetwork ((Element) netmaskList.item(0));
 				}
 				allParametersString.append(getTCloud2RangedONENet (fqn, size, baseAddressList.item(0).getTextContent()));
 			}
 			else if (ipLeaseList.getLength()>0)
 			{
 				int size = 0;
 				if (netmaskList.getLength() >0)
 				{
 					size = getSizeNetwork ((Element) netmaskList.item(0));
 				}
 				allParametersString.append(getTCloud2IPElasedONENet (fqn, size, baseAddressList.item(0).getTextContent(), ipLeaseList));
 			}
 
 			System.out.println("Network data sent:\n\n" + allParametersString.toString() + "\n\n");
 
 			return allParametersString.toString();
 
 		} catch (IOException e1) {
 			System.out.println("OVF of the virtual machine was not well formed or it contained some errors.");
 			throw new Exception("OVF of the virtual machine was not well formed or it contained some errors: " + e1.getMessage());
 		} catch (ParserConfigurationException e) {
 			System.out.println("Error configuring parser: " + e.getMessage());
 			throw new Exception("Error configuring parser: " + e.getMessage());
 		} catch (FactoryConfigurationError e) {
 			System.out.println("Error retrieving parser: " + e.getMessage());
 			throw new Exception("Error retrieving parser: " + e.getMessage());
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println("Error configuring a XML Builder.");
 			throw new Exception("Error configuring a XML Builder: " + e.getMessage());
 		}
 	}
 */
 	/*public String getTCloud2FixedONENet (String fqn)
 	{
 		StringBuffer allParametersString  = new StringBuffer();
 		// Translate the simple data to RPC format
 		allParametersString.append(ONE_NET_NAME).append(ASSIGNATION_SYMBOL).append(fqn).append(LINE_SEPARATOR);
 		allParametersString.append(ONE_NET_TYPE).append(ASSIGNATION_SYMBOL).append("FIXED").append(LINE_SEPARATOR);
 		allParametersString.append(ONE_NET_BRIDGE).append(ASSIGNATION_SYMBOL).append(networkBridge).append(LINE_SEPARATOR);
 		return allParametersString.toString();
 	}
 
 	public String getTCloud2RangedONENet (String fqn, int size, String network)
 	{
 
 		StringBuffer allParametersString  = new StringBuffer();
 		// Translate the simple data to RPC format
 		allParametersString.append(ONE_NET_NAME).append(ASSIGNATION_SYMBOL).append(fqn).append(LINE_SEPARATOR);
 		allParametersString.append(ONE_NET_TYPE).append(ASSIGNATION_SYMBOL).append("RANGED").append(LINE_SEPARATOR);
 		allParametersString.append(ONE_NET_BRIDGE).append(ASSIGNATION_SYMBOL).append(networkBridge).append(LINE_SEPARATOR);
 		if (size != 0)
 			allParametersString.append(ONE_NET_SIZE).append(ASSIGNATION_SYMBOL).append(size).append(LINE_SEPARATOR);
 		allParametersString.append(ONE_NET_ADDRESS).append(ASSIGNATION_SYMBOL).append(network).append(LINE_SEPARATOR);
 		return allParametersString.toString();
 	}
 
 	public String getTCloud2IPElasedONENet (String fqn, int size, String network, NodeList ipLeaseList)
 	{
 		StringBuffer allParametersString  = new StringBuffer();
 		// Translate the simple data to RPC format
 		allParametersString.append(ONE_NET_NAME).append(ASSIGNATION_SYMBOL).append(fqn).append(LINE_SEPARATOR);
 		allParametersString.append(ONE_NET_TYPE).append(ASSIGNATION_SYMBOL).append("FIXED").append(LINE_SEPARATOR);
 		allParametersString.append(ONE_NET_BRIDGE).append(ASSIGNATION_SYMBOL).append(networkBridge).append(LINE_SEPARATOR);
 		if (size != 0)
 			allParametersString.append(ONE_NET_SIZE).append(ASSIGNATION_SYMBOL).append(size).append(LINE_SEPARATOR);
 		//	allParametersString.append(ONE_NET_ADDRESS).append(ASSIGNATION_SYMBOL).append(network).append(LINE_SEPARATOR);
 
 
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
 		return allParametersString.toString();
 	}*/
 
 /*	public static int getSizeNetwork (Element netmask)
 	{
 
 
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
 
 		return size ;
 	}*/
 
 	protected static String ONENet2TCloud(String ONETemplate) {
 		return "";
 	}
 
 	/**
 	 * Get the number of bits with value 1 in the given IP.
 	 *
 	 * @return
 	 */
 /*	public static int getBitNumber (short[] ip) {
 
 		if (ip == null || ip.length != 4)
 			return 0;
 
 		int bits=0;
 		for (int i=0; i < 4; i++)
 			for (int j=0; j< 15; j++)
 				bits += ( ((short)Math.pow(2, j))& ip[i]) / Math.pow(2, j);
 
 		return bits;
 	}*/
 
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
 		
 		
 		if (this.oneversion.equals("3.0"))
 		{
 		  
 		  rpcParams.add(-1);
 		  rpcParams.add(-1);
 		  rpcParams.add(-2);
 		}
 
 		HashMap<String, Integer> mapResult = new HashMap<String, Integer>();
 
 		Object[] result = null;
 		try {
 			result = (Object[])xmlRpcClient.execute(VM_GETALL_COMMAND, rpcParams);
 		} catch (XmlRpcException ex) {
 			System.out.println (" getVmIds" + ex.getMessage());
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
 		
 		if (this.oneversion.equals("3.0"))
 		{
 		  rpcParams.add(-1);
 		  rpcParams.add(-1);
 		}
 
 
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
 
 		if (prop.containsKey("com.telefonica.claudia.customization.port")) {
 			customizationPort = ((String) prop.get(Main.CUSTOMIZATION_PORT_PROPERTY));
 		}
 
 		if (prop.containsKey(ENVIRONMENT_PROPERTY)) {
 			environmentRepositoryPath = (String) prop.get(ENVIRONMENT_PROPERTY);
 		}
 
 		if (prop.containsKey("oneNetworkBridge")) {
 			networkBridge = ((String) prop.get("oneNetworkBridge"));
 		}
 		
 		if (prop.containsKey(this.ONE_VERSION)) {
 			oneversion = ((String) prop.get(ONE_VERSION));
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
 		
 		String server = null;
 		if (prop.containsKey("com.telefonica.claudia.server.host")) {
 			server = ((String) prop.get("com.telefonica.claudia.server.host"));
 		}
 		
 
 		xmlRpcClient = new XmlRpcClient();
 		log.info("XMLRPC client created");
 		xmlRpcClient.setConfig(config);
 		log.info("XMLRPC client configured");
 
 		/* MIGRABILITY TAG */
 		text_migrability.put("cross-host", "HOST");
 		text_migrability.put("cross-sitehost", "SITE");
 		text_migrability.put("none", "NONE");
 		operations = new OneOperations(oneSession, xmlRpcClient);
 
 		operations.configOperations(oneversion, networkBridge, environmentRepositoryPath, oneSshKey, customizationPort, hypervisorInitrd, hypervisorKernel, xendisk, ARCH_PROPERTY, server);
 		netUtils = new OneNetUtilities(networkBridge);
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
 		String id = null;
 		try {
 			id = getVmId(fqn).toString();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		//	deleteVirtualMachine(id);
 		String xml = null;
 		try
 		{
 		String result = operations.getVirtualMachine(id);
 		ONEUtilities utils = new ONEUtilities ();
 		HashMap data = utils.getCpuRamDiskIp (result);
 	
 		 xml = utils.generateXMLVEE (fqn, (String) data.get("IP"), (String)data.get("CPU"), (String)data.get("MEMORY"), (String)data.get("DISK"));
 		}
 		catch (Exception e)
 		{
 			return null;
 		}
 		return xml;
 		
 		
 		
 	}
 
 
 }
