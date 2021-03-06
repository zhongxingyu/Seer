 package org.eclipse.jdi.internal.connect;
 
 /*
  * (c) Copyright IBM Corp. 2000, 2001.
  * All Rights Reserved.
  */
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.List;
 
 import org.eclipse.jdi.internal.VirtualMachineImpl;
 import org.eclipse.jdi.internal.VirtualMachineManagerImpl;
 
 import com.sun.jdi.VirtualMachine;
 import com.sun.jdi.connect.Connector;
 import com.sun.jdi.connect.Transport;
 
 
 /**
  * this class implements the corresponding interfaces
  * declared by the JDI specification. See the com.sun.jdi package
  * for more information.
  *
  */
 public abstract class ConnectorImpl implements Connector {
 	/** Manager for receiving packets from the Virtual Machine. */
 	private PacketReceiveManager fPacketReceiveManager;
 	/** Coresponding receiving thread. */
 	private Thread fThreadReceiveMgr;
 	/** Manager for sending packets to the Virtual Machine. */
 	private PacketSendManager fPacketSendManager;
 	/** Coresponding sending thread. */
 	private Thread fThreadSendMgr;
 	/** Virtual machine manager that created this connector. */
 	private VirtualMachineManagerImpl fVirtualMachineManager;
 
 	/** Transport that is used for communication. */
 	protected TransportImpl fTransport;
 	/** Virtual Machine that is connected. */
 	protected VirtualMachineImpl fVirtualMachine;
 	
 	/**
 	 * Creates a new Connector.
 	 */	
 	public ConnectorImpl(VirtualMachineManagerImpl virtualMachineManager) {
 		fVirtualMachineManager = virtualMachineManager;
 	}
 
 	/**
 	 * @return Returns Virtual Machine Manager.
 	 */
 	public VirtualMachineManagerImpl virtualMachineManager() {
 		return fVirtualMachineManager;
 	}
 	
 	/**
 	 * @return Returns Virtual Machine Manager.
 	 */
 	public VirtualMachineImpl virtualMachine() {
 		return fVirtualMachine;
 	}
 
 	/**
 	 * @return Returns a human-readable description of this connector and its purpose.
 	 */	
 	public abstract String description();
 	
 	/**
 	 * @return Returns a short identifier for the connector.
 	 */	
 	public abstract String name();
 	
 	/**
 	 * Assigns Transport.
 	 */	
 	/*package*/ void setTransport(TransportImpl transport) {
 		fTransport = transport;
 	}
 
 	/**
 	 * @return Returns the transport mechanism used by this connector to establish connections with a target VM.
 	 */	
 	public Transport transport() {
 		return fTransport;
 	}
 	
 	/**
 	 * @return Returns the manager for receiving packets from the Virtual Machine.
 	 */	
 	public PacketReceiveManager packetReceiveManager() {
 		return fPacketReceiveManager;
 	}
 	
 	/**
 	 * @return Returns the manager for sending packets to the Virtual Machine.
 	 */	
 	public PacketSendManager packetSendManager() {
 		return fPacketSendManager;
 	}
 	
 	/**
 	 * Closes connection with Virtual Machine.
 	 */	
 	/*package*/ synchronized void close() {
 		virtualMachineManager().removeConnectedVM(fVirtualMachine);
 		fTransport.close();
 	}
 
 	/**
 	 * @return Returns InputStream from Virtual Machine.
 	 */	
 	protected InputStream getInputStream() throws IOException {
 		return fTransport.getInputStream();
 	}
 	
 	/**
 	 * @return Returns OutputStream to Virtual Machine.
 	 */	
 	protected OutputStream getOutputStream() throws IOException {
 		return fTransport.getOutputStream();
 	}
 
 	/**
 	 * Initializes receiving and sending threads.
 	 */	
 	protected void startIOManagers() {
 		fPacketReceiveManager = new PacketReceiveManager(this);
 		fPacketSendManager = new PacketSendManager(this);
 		fThreadReceiveMgr = new Thread(fPacketReceiveManager, ConnectMessages.getString("ConnectorImpl.Packet_Receive_Manager_1")); //$NON-NLS-1$
 		fThreadSendMgr = new Thread(fPacketSendManager, ConnectMessages.getString("ConnectorImpl.Packet_Send_Manager_2")); //$NON-NLS-1$
 		fPacketReceiveManager.setPartnerThread(fThreadSendMgr);
 		fPacketSendManager.setPartnerThread(fThreadReceiveMgr);
 		fThreadReceiveMgr.start();
 		fThreadSendMgr.start();
 
 	}
 	
 	/**
 	 * @return Returns a connected Virtual Machine.
 	 */	
 	protected VirtualMachine establishedConnection() {
 		fVirtualMachine = new VirtualMachineImpl(this);
 		startIOManagers();
 		virtualMachineManager().addConnectedVM(fVirtualMachine);
 		return fVirtualMachine;
 	}
 	
 	/**
 	 * Argument class for arguments that are used to establish a connection.
 	 */
 	public abstract class ArgumentImpl implements com.sun.jdi.connect.Connector.Argument {
 		private String fName;
 		private String fDescription;
 		private String fLabel;
 		private boolean fMustSpecify;
 
 	 	protected ArgumentImpl(String name, String description, String label, boolean mustSpecify) {
 	 		fName = name;
 	 		fDescription = description;
 	 		fMustSpecify = mustSpecify;
 	 	}
 
 		public String name() { 
 			return fName;
 		}
 		
 		public String description() {
 			return fDescription;
 		}
 		
 		public String label() {
 			return fLabel;
 		}
 		
 		public boolean mustSpecify() {
 			return fMustSpecify;
 		}
 		
 		public abstract String value();
 		public abstract void setValue(String value);
 		public abstract boolean isValid(String value);
 		public abstract String toString();
 	}
 	
 	public class StringArgumentImpl extends ArgumentImpl implements com.sun.jdi.connect.Connector.StringArgument {
 		private String fValue;
 
 	 	protected StringArgumentImpl(String name, String description, String label, boolean mustSpecify) {
 	 		super(name, description, label, mustSpecify);
 	 	}
 	 	
 		public String value() {
 			return fValue;
 		}
 		
 		public void setValue(String value) {
 			fValue = value;
 		}
 
 		public boolean isValid(String value) {
 			return true;
 		}
 
 		public String toString() {
 			return fValue;
 		}
 
 	}
 	
 	public class IntegerArgumentImpl extends ArgumentImpl implements com.sun.jdi.connect.Connector.IntegerArgument {
 		private Integer fValue;
 		private int fMin;
 		private int fMax;
 
 	 	protected IntegerArgumentImpl(String name, String description, String label, boolean mustSpecify, int min, int max) {
 	 		super(name, description, label, mustSpecify);
 	 		fMin = min;
 	 		fMax = max;
 	 	}
 	 	
 		public String value() {
 			return fValue.toString();
 		}
 		
 		public void setValue(String value) {
 			fValue = new Integer(value);
 		}
 
 		public boolean isValid(String value) {
 			Integer val;
 			try {
 				val = new Integer(value);
 			} catch (NumberFormatException e) {
 				return false;
 			}
 			return isValid(val.intValue());
 		}
 
 		public String toString() {
 			return value();
 		}
 
 		public int intValue() {
 			return fValue.intValue();
 		}
 		
 		public void setValue(int value) {
 			fValue = new Integer(value);
 		}
 
 		public int min() {
 			return fMin;
 		}
 		
 		public int max() {
 			return fMax;
 		}
 		
 		public boolean isValid(int value) {
 			return fMin <= value && value <= fMax;
 		}
 
 		public String stringValueOf(int value) {
 			return new Integer(value).toString();
 		}
 	}
 	
 	public class BooleanArgumentImpl extends ArgumentImpl implements com.sun.jdi.connect.Connector.BooleanArgument {
 		private Boolean fValue;
 		
 	 	protected BooleanArgumentImpl(String name, String description, String label, boolean mustSpecify) {
 	 		super(name, description, label, mustSpecify);
 	 	}
 	 	
 		public String value() {
 			return fValue.toString();
 		}
 		
 		public void setValue(String value) {
 			fValue = new Boolean(value);
 		}
 
 		public boolean isValid(String value) {
 			return true;
 		}
 		
 		public String toString() {
 			return value();
 		}
 
 		public boolean booleanValue() {
 			return fValue.booleanValue();
 		}
 
 		public void setValue(boolean value) {
 			fValue = new Boolean(value);
 		}
 		
 		public String stringValueOf(boolean value) {
 			return new Boolean(value).toString();
 		}
 	}
 	
 	public class SelectedArgumentImpl extends StringArgumentImpl implements com.sun.jdi.connect.Connector.SelectedArgument {
 		private List fChoices;
 		
 	 	protected SelectedArgumentImpl(String name, String description, String label, boolean mustSpecify, List choices) {
 	 		super(name, description, label, mustSpecify);
 	 		fChoices = choices;
 	 	}
 	 	
 		public List choices() {
 			return fChoices;
 		}
 		
 		public boolean isValid(java.lang.String value) {
 			return fChoices.contains(value);
 		}
 	}
 }
