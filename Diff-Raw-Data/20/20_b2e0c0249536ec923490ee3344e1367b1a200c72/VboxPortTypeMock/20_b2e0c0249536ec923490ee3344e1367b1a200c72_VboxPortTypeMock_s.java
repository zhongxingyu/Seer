 /**
  * 
  */
 package com.abiquo.vbox.mock;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.Set;
 import java.util.UUID;
 
 import javax.jws.HandlerChain;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.virtualbox.AccessMode;
 import org.virtualbox.CPUPropertyType;
 import org.virtualbox.DeviceType;
 import org.virtualbox.InvalidObjectFaultMsg;
 import org.virtualbox.LockType;
 import org.virtualbox.MachineState;
 import org.virtualbox.MediumState;
 import org.virtualbox.RuntimeFaultMsg;
 import org.virtualbox.SessionState;
 import org.virtualbox.StorageBus;
 
 import com.abiquo.mock.configuration.ConfigurationService;
 import com.abiquo.mock.configuration.Constants;
 import com.abiquo.mock.domain.DomainService;
 import com.abiquo.mock.model.Medium;
 import com.abiquo.mock.model.NetworkAdapter;
 import com.abiquo.mock.model.NetworkInterface;
 import com.abiquo.mock.model.Progress;
 import com.abiquo.mock.model.Session;
 import com.abiquo.mock.model.Storage;
 import com.abiquo.mock.model.VirtualMachineInfo;
 import com.abiquo.vbox.VboxPortTypeImpl;
 
 /**
  * @author <a href="mailto:serafin.sedano@abiquo.com">Serafin Sedano</a>
  * @author <a href="mailto:enric.ruiz@abiquo.com">Enric Ruiz</a>
  */
 @HandlerChain(file = "conf/handlers-chain.xml")
 @javax.jws.WebService(serviceName = "vboxService", portName = "vboxServicePort", targetNamespace = "http://www.virtualbox.org/Service", wsdlLocation = "wsdl/vboxwebService.wsdl", endpointInterface = "org.virtualbox.VboxPortType")
 public class VboxPortTypeMock extends VboxPortTypeImpl
 {
 
     private final static Logger LOG = LoggerFactory.getLogger(VboxPortTypeMock.class);
 
     private static final Map<String, Long> cache = Collections
         .synchronizedMap(new HashMap<String, Long>());
 
     /*
      * (non-Javadoc)
      * @see org.virtualbox.VboxPortType#iMachineGetState(java.lang.String _this )*
      */
     @Override
     public org.virtualbox.MachineState iMachineGetState(final java.lang.String _this)
         throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         // LOG.info("Executing operation iMachineGetState");
         // System.out.println(_this);
         VirtualMachineInfo vm = DomainService.getInstance().getVirtualMachines().get(_this);
         if (vm == null || !vm.isSaved())
         {
             throw new InvalidObjectFaultMsg("iMachineGetState: InvalidObjectFaultMsg...");
         }
         org.virtualbox.MachineState _return = vm.getMachineState();
         return _return;
     }
 
     /*
      * (non-Javadoc)
      * @see org.virtualbox.VboxPortType#iVirtualBoxGetMachines(java.lang.String _this )*
      */
     @Override
     public java.util.List<java.lang.String> iVirtualBoxGetMachines(final java.lang.String _this)
         throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         // LOG.info("Executing operation iVirtualBoxGetMachines");
         // System.out.println(_this);
         try
         {
             java.util.List<java.lang.String> _return = new ArrayList<String>();
             for (Entry<String, VirtualMachineInfo> e : DomainService.getInstance()
                 .getVirtualMachines().entrySet())
             {
                 if (e.getValue().isSaved())
                 {
                     _return.add(e.getKey());
                 }
             }
             return _return;
         }
         catch (java.lang.Exception ex)
         {
             ex.printStackTrace();
             throw new RuntimeFaultMsg(ex.getMessage(), ex);
         }
         // throw new RuntimeFaultMsg("RuntimeFaultMsg...");
         // throw new InvalidObjectFaultMsg("InvalidObjectFaultMsg...");
     }
 
     /*
      * (non-Javadoc)
      * @see org.virtualbox.VboxPortType#iWebsessionManagerLogon(java.lang.String username
      * ,)java.lang.String password )*
      */
     @Override
     public java.lang.String iWebsessionManagerLogon(final java.lang.String username,
         final java.lang.String password) throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         // LOG.info("Executing operation iWebsessionManagerLogon");
         // System.out.println(username);
         // System.out.println(password);
         try
         {
             Session session = new Session();
             java.lang.String _return = session.getId();
             if (!DomainService.getInstance().getSession().containsKey(session.getId()))
             {
                 DomainService.getInstance().getSession().put(_return, session);
             }
             return _return;
         }
         catch (java.lang.Exception ex)
         {
             ex.printStackTrace();
             throw new RuntimeFaultMsg(ex.getMessage(), ex);
         }
         // throw new RuntimeFaultMsg("RuntimeFaultMsg...");
         // throw new InvalidObjectFaultMsg("InvalidObjectFaultMsg...");
     }
 
     /*
      * (non-Javadoc)
      * @see org.virtualbox.VboxPortType#iVirtualBoxFindMachine(java.lang.String _this
      * ,)java.lang.String nameOrId )*
      */
     @Override
     public java.lang.String iVirtualBoxFindMachine(final java.lang.String _this,
         final java.lang.String nameOrId) throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         // LOG.info("Executing operation iVirtualBoxFindMachine");
         // System.out.println("This: " + _this);
         // System.out.println("Name: " + nameOrId);
         try
         {
             Map<String, VirtualMachineInfo> virtualMachines =
                 DomainService.getInstance().getVirtualMachines();
             for (Entry<String, VirtualMachineInfo> e : virtualMachines.entrySet())
             {
                 if (nameOrId.equals(e.getValue().getName()) && e.getValue().isSaved())
                 {
                     java.lang.String _return = e.getValue().getId();
                     return _return;
                 }
             }
             return "";
         }
         catch (java.lang.Exception ex)
         {
             ex.printStackTrace();
             throw new RuntimeFaultMsg(ex.getMessage(), ex);
         }
         // throw new RuntimeFaultMsg("RuntimeFaultMsg...");
     }/*
       * (non-Javadoc)
       * @see org.virtualbox.VboxPortType#iVirtualBoxRegisterMachine(java.lang.String _this
       * ,)java.lang.String machine )*
       */
 
     @Override
     public void iVirtualBoxRegisterMachine(final java.lang.String _this,
         final java.lang.String machine) throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         // LOG.info("Executing operation iVirtualBoxRegisterMachine");
         // System.out.println(_this);
         // System.out.println(machine);
         VirtualMachineInfo virtualMachine =
             DomainService.getInstance().getVirtualMachines().get(machine);
         if (virtualMachine == null)
         {
             throw new InvalidObjectFaultMsg("InvalidObjectFaultMsg...");
         }
         virtualMachine.setRegistered(Boolean.TRUE);
         // throw new RuntimeFaultMsg("RuntimeFaultMsg...");
     }/*
       * (non-Javadoc)
       * @see org.virtualbox.VboxPortType#iHostNetworkInterfaceGetHardwareAddress(java.lang.String
       * _this )*
       */
 
     @Override
     public java.lang.String iHostNetworkInterfaceGetHardwareAddress(final java.lang.String _this)
         throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         // LOG.info("Executing operation iHostNetworkInterfaceGetHardwareAddress");
         // System.out.println(_this);
         try
         {
             List<NetworkInterface> networkInterfaces =
                 DomainService.getInstance().getNetworkInterfaces();
             for (NetworkInterface n : networkInterfaces)
             {
                 if (n.getId().equals(_this))
                 {
                     return n.getMac();
                 }
             }
             java.lang.String _return = "";
             return _return;
         }
         catch (java.lang.Exception ex)
         {
             ex.printStackTrace();
             throw new RuntimeFaultMsg(ex.getMessage(), ex);
         }
         // throw new RuntimeFaultMsg("RuntimeFaultMsg...");
         // throw new InvalidObjectFaultMsg("InvalidObjectFaultMsg...");
     }
 
     @Override
     public String iWebsessionManagerGetSessionObject(final String refIVirtualBox)
         throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         Session session = DomainService.getInstance().getSession().get(refIVirtualBox);
         if (session == null)
         {
             // No session, log again
             throw new InvalidObjectFaultMsg("iWebsessionManagerGetSessionObject: No session. InvalidObjectFaultMsg...");
         }
         return session.getId();
     }
 
     /*
      * (non-Javadoc)
      * @see org.virtualbox.VboxPortType#iHostGetProcessorOnlineCount(java.lang.String _this )*
      */
     @Override
     public long iHostGetProcessorOnlineCount(final java.lang.String _this) throws RuntimeFaultMsg,
         InvalidObjectFaultMsg
     {
         // LOG.info("Executing operation iHostGetProcessorOnlineCount");
         // System.out.println(_this);
         try
         {
             Integer cores = DomainService.getInstance().getCores();
             long _return = Long.valueOf(cores);
             return _return;
         }
         catch (java.lang.Exception ex)
         {
             ex.printStackTrace();
             throw new RuntimeFaultMsg(ex.getMessage(), ex);
         }
         // throw new RuntimeFaultMsg("RuntimeFaultMsg...");
         // throw new InvalidObjectFaultMsg("InvalidObjectFaultMsg...");
     }/*
       * (non-Javadoc)
       * @see org.virtualbox.VboxPortType#iVirtualBoxGetHost(java.lang.String _this )*
       */
 
     @Override
     public java.lang.String iVirtualBoxGetHost(final java.lang.String _this)
         throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         // LOG.info("Executing operation iVirtualBoxGetHost");
         // System.out.println(_this);
         try
         {
             java.lang.String _return = DomainService.getInstance().getName();
             return _return;
         }
         catch (java.lang.Exception ex)
         {
             ex.printStackTrace();
             throw new RuntimeFaultMsg(ex.getMessage(), ex);
         }
         // throw new RuntimeFaultMsg("RuntimeFaultMsg...");
         // throw new InvalidObjectFaultMsg("InvalidObjectFaultMsg...");
     }/*
       * (non-Javadoc)
       * @see org.virtualbox.VboxPortType#iWebsessionManagerLogoff(java.lang.String refIVirtualBox )*
       */
 
     @Override
     public void iWebsessionManagerLogoff(final java.lang.String refIVirtualBox)
         throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         try
         {
             DomainService.getInstance().getSession().remove(refIVirtualBox);
         }
         catch (java.lang.Exception ex)
         {
             ex.printStackTrace();
             throw new RuntimeFaultMsg(ex.getMessage(), ex);
         }
     }/*
       * (non-Javadoc)
       * @see org.virtualbox.VboxPortType#iHostGetMemorySize(java.lang.String _this )*
       */
 
     @Override
     public long iHostGetMemorySize(final java.lang.String _this) throws RuntimeFaultMsg,
         InvalidObjectFaultMsg
     {
         // LOG.info("Executing operation iHostGetMemorySize");
         // System.out.println(_this);
         try
         {
             Integer memory = DomainService.getInstance().getMemory();
             long _return = Long.valueOf(memory);
             return _return;
         }
         catch (java.lang.Exception ex)
         {
             ex.printStackTrace();
             throw new RuntimeFaultMsg(ex.getMessage(), ex);
         }
         // throw new RuntimeFaultMsg("RuntimeFaultMsg...");
         // throw new InvalidObjectFaultMsg("InvalidObjectFaultMsg...");
     } /*
        * (non-Javadoc)
        * @see org.virtualbox.VboxPortType#iMachineGetName(java.lang.String _this )*
        */
 
     @Override
     public java.lang.String iMachineGetName(final java.lang.String _this) throws RuntimeFaultMsg,
         InvalidObjectFaultMsg
     {
         // LOG.info("Executing operation iMachineGetName");
         // System.out.println(_this);
         VirtualMachineInfo vm = DomainService.getInstance().getVirtualMachines().get(_this);
         if (vm == null || !vm.isSaved())
         {
             throw new InvalidObjectFaultMsg("iMachineGetName: InvalidObjectFaultMsg...");
         }
         java.lang.String _return = vm.getName();
         return _return;
    }/*
      * (non-Javadoc)
      * @see org.virtualbox.VboxPortType#iHostGetNetworkInterfaces(java.lang.String _this )*
      */
 
     @Override
     public java.util.List<java.lang.String> iHostGetNetworkInterfaces(final java.lang.String _this)
         throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         // LOG.info("Executing operation iHostGetNetworkInterfaces");
         // System.out.println(_this);
         try
         {
             java.util.List<java.lang.String> _return = new ArrayList<String>();
             List<NetworkInterface> interfaces = DomainService.getInstance().getNetworkInterfaces();
             for (NetworkInterface n : interfaces)
             {
                 _return.add(n.getId());
             }
             return _return;
         }
         catch (java.lang.Exception ex)
         {
             ex.printStackTrace();
             throw new RuntimeFaultMsg(ex.getMessage(), ex);
         }
         // throw new RuntimeFaultMsg("RuntimeFaultMsg...");
         // throw new InvalidObjectFaultMsg("InvalidObjectFaultMsg...");
     }/*
       * (non-Javadoc)
       * @see org.virtualbox.VboxPortType#iHostNetworkInterfaceGetName(java.lang.String _this )*
       */
 
     @Override
     public java.lang.String iHostNetworkInterfaceGetName(final java.lang.String _this)
         throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         // LOG.info("Executing operation iHostNetworkInterfaceGetName");
         // System.out.println(_this);
         try
         {
             List<NetworkInterface> networkInterfaces =
                 DomainService.getInstance().getNetworkInterfaces();
             for (NetworkInterface n : networkInterfaces)
             {
                 if (n.getId().equals(_this))
                 {
                     return n.getName();
                 }
             }
             java.lang.String _return = "";
             return _return;
         }
         catch (java.lang.Exception ex)
         {
             ex.printStackTrace();
             throw new RuntimeFaultMsg(ex.getMessage(), ex);
         }
         // throw new RuntimeFaultMsg("RuntimeFaultMsg...");
         // throw new InvalidObjectFaultMsg("InvalidObjectFaultMsg...");
     }/*
       * (non-Javadoc)
       * @see org.virtualbox.VboxPortType#iVirtualBoxGetVersion(java.lang.String _this )*
       */
 
     @Override
     public java.lang.String iVirtualBoxGetVersion(final java.lang.String _this)
         throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         // LOG.info("Executing operation iVirtualBoxGetVersion");
         // System.out.println(_this);
         java.lang.String _return = DomainService.getInstance().getVersion();
         return _return;
         // throw new RuntimeFaultMsg("RuntimeFaultMsg...");
         // throw new InvalidObjectFaultMsg("InvalidObjectFaultMsg...");
     }
 
     /*
      * Only one virtual machine per session allowed. (non-Javadoc)
      * @see org.virtualbox.VboxPortType#iVirtualBoxCreateMachine(java.lang.String _this
      * ,)java.lang.String settingsFile ,)java.lang.String name ,)java.lang.String osTypeId
      * ,)java.lang.String id ,)boolean forceOverwrite )*
      */
     @Override
     public java.lang.String iVirtualBoxCreateMachine(final java.lang.String _this,
         final java.lang.String settingsFile, final java.lang.String name,
         final java.lang.String osTypeId, final java.lang.String id, final boolean forceOverwrite)
         throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         VirtualMachineInfo vm = new VirtualMachineInfo();
         vm.setSettingsFile(settingsFile);
         vm.setName(name);
         vm.setOsType(osTypeId);
         vm.setId(id);
         vm.setForceOverwrite(forceOverwrite);
         Session session = DomainService.getInstance().getSession().get(_this);
         if (session == null)
         {
             // No session, log again
             throw new InvalidObjectFaultMsg("iVirtualBoxCreateMachine: No session. InvalidObjectFaultMsg...");
         }
 
         if (session.getVirtualMachine() != null)
         {
             throw new InvalidObjectFaultMsg("iVirtualBoxCreateMachine: No more virtual machines allowed per session. InvalidObjectFaultMsg...");
         }
         session.setVirtualMachine(vm);
         DomainService.getInstance().getVirtualMachines().put(vm.getId(), vm);
         java.lang.String _return = id;
         return _return;
     }
 
     // @Override
     // public void ivrdeServerSetVRDEProperty(String _this, String key, String value)
     // throws RuntimeFaultMsg, InvalidObjectFaultMsg
     // {
     // VirtualMachineInfo virtualMachine =
     // DomainService.getInstance().getVirtualMachines().get(_this);
     // if (virtualMachine == null)
     // {
     // throw new InvalidObjectFaultMsg("iMachinecpu no such machine id" + _this);
     // }
     // virtualMachine.setRemoteDesktopPort(Integer.valueOf(value));
     // }
 
     @Override
     public String iMachineGetNetworkAdapter(final String _this, final long slot)
         throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         VirtualMachineInfo virtualMachine =
             DomainService.getInstance().getVirtualMachines().get(_this);
         if (virtualMachine == null)
         {
             throw new InvalidObjectFaultMsg("iMachinecpu no such machine id" + _this);
         }
 
         if (!DomainService.getInstance().getNetworkInterfaces().isEmpty())
         {
             NetworkInterface networkInterface =
                 DomainService
                     .getInstance()
                     .getNetworkInterfaces()
                     .get(
                         new Random().nextInt(DomainService.getInstance().getNetworkInterfaces()
                             .size()));
 
             NetworkAdapter net = new NetworkAdapter();
             net.setSlot(slot);
             net.setNetworkInterface(networkInterface);
             virtualMachine.getNetworkAdapters().put(net.getId(), net);
             return net.getId();
         }
 
         throw new InvalidObjectFaultMsg("iMachineGetNetworkAdapter the mock lacks of network"
             + _this);
     }
 
     @Override
     public void iMachineAttachDevice(final String _this, final String name,
         final int controllerPort, final int device, final DeviceType type, final String medium)
         throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         VirtualMachineInfo virtualMachine =
             DomainService.getInstance().getVirtualMachines().get(_this);
         if (virtualMachine == null)
         {
             throw new InvalidObjectFaultMsg("iMachinecpu no such machine id" + _this);
         }
         Medium mmedium = DomainService.getInstance().getMediums().get(medium);
         if (mmedium == null)
         {
             throw new InvalidObjectFaultMsg("iMedium: No medium " + medium
                 + ". InvalidObjectFaultMsg...");
         }
         mmedium.getAttachedTo().add(virtualMachine.getId());
     }
 
     @Override
     public String iMachineGetVRDEServer(final String _this) throws RuntimeFaultMsg,
         InvalidObjectFaultMsg
     {
         VirtualMachineInfo virtualMachine =
             DomainService.getInstance().getVirtualMachines().get(_this);
         if (virtualMachine == null)
         {
             throw new InvalidObjectFaultMsg("iMachinecpu no such machine id" + _this);
         }
 
         String remote = UUID.randomUUID().toString();
         virtualMachine.setRemoteDesktop(remote);
 
         return remote;
     }
 
     @Override
     public String iMediumGetId(final String _this) throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         Medium medium = DomainService.getInstance().getMediums().get(_this);
         if (medium == null)
         {
             throw new InvalidObjectFaultMsg("iMedium: No medium " + _this
                 + ". InvalidObjectFaultMsg...");
         }
         return _this;
     }
 
     @Override
     public void iMachineSetCPUProperty(final String _this, final CPUPropertyType property,
         final boolean value) throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         VirtualMachineInfo virtualMachine =
             DomainService.getInstance().getVirtualMachines().get(_this);
         if (virtualMachine == null)
         {
             throw new InvalidObjectFaultMsg("iMachinecpu no such machine id" + _this);
         }
         virtualMachine.getCpuProps().put(property, value);
     }
 
     @Override
     public String iMachineAddStorageController(final String _this, final String name,
         final StorageBus connectionType) throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         VirtualMachineInfo virtualMachine =
             DomainService.getInstance().getVirtualMachines().get(_this);
         if (virtualMachine == null)
         {
             throw new InvalidObjectFaultMsg("iMachinecpu no such machine id" + _this);
         }
 
         Storage storage = new Storage();
         storage.setName(name);
         storage.setBus(connectionType);
         virtualMachine.getStorages().put(storage.getId(), storage);
         DomainService.getInstance().getMediums().put(storage.getId(), storage);
         return storage.getId();
     }
 
     @Override
     public void iMachineSetCPUCount(final String _this, final long cpuCount)
         throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         VirtualMachineInfo virtualMachine =
             DomainService.getInstance().getVirtualMachines().get(_this);
         if (virtualMachine == null)
         {
             throw new InvalidObjectFaultMsg("iMachinecpu no such machine id" + _this);
         }
         virtualMachine.setCpu(cpuCount);
     }
 
     @Override
     public void iMachineSetMemorySize(final String _this, final long memorySize)
         throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         VirtualMachineInfo virtualMachine =
             DomainService.getInstance().getVirtualMachines().get(_this);
         if (virtualMachine == null)
         {
             throw new InvalidObjectFaultMsg("iMachineMemory no such machine id" + _this);
         }
         virtualMachine.setMemory(memorySize);
     }
 
     @Override
     public void iMediumClose(final String _this) throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         Medium medium = DomainService.getInstance().getMediums().get(_this);
         if (medium == null)
         {
             throw new InvalidObjectFaultMsg("iMedium: No medium " + _this
                 + ". InvalidObjectFaultMsg...");
         }
         if (!medium.getAttachedTo().isEmpty())
         {
             throw new InvalidObjectFaultMsg("iMedium: attached medium " + _this
                 + ". InvalidObjectFaultMsg...");
         }
         medium.setClosed(Boolean.TRUE);
     }
 
     /** Return the progress to the task */
     @Override
     public String iMediumCloneTo(final String _this, final String target, final long variant,
         final String parent) throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         Medium medium = DomainService.getInstance().getMediums().get(_this);
         Medium mtarget = DomainService.getInstance().getMediums().get(target);
         if (medium == null || mtarget == null)
         {
             throw new InvalidObjectFaultMsg("iMediumCloneTo: No such mediums " + _this + ", "
                 + target + " . InvalidObjectFaultMsg...");
         }
         mtarget.setVariant(variant);
 
         Progress progress = new Progress(getTimeToComplete("iMediumCloneTo"));
         DomainService.getInstance().getTasks().put(progress.getId(), progress);
         return progress.getId();
     }
 
     @Override
     public void iProgressWaitForCompletion(final String _this, final int timeout)
         throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
     }
 
     @Override
     public String iVirtualBoxCreateHardDisk(final String _this, final String format,
         final String location) throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         Session session = DomainService.getInstance().getSession().get(_this);
         if (session == null)
         {
             // No session, log again
             throw new InvalidObjectFaultMsg("iVirtualBoxCreateHardDisk: No session. InvalidObjectFaultMsg...");
         }
 
         Set<Object> set = session.getData();
         Medium medium = new Medium();
         medium.setLocation(location);
         medium.setDeviceType(DeviceType.HARD_DISK);
         medium.setClosed(Boolean.FALSE);
 
         if (session.getVirtualMachine() == null)
         {
 
             throw new InvalidObjectFaultMsg("iVirtualBoxCreateHardDisk: No virtual machine session. InvalidObjectFaultMsg...");
         }
         DomainService.getInstance().getMediums().put(medium.getId(), medium);
         set.add(medium);
         session.getVirtualMachine().setDiskSourceLocation(location);
         return medium.getId();
     }
 
     @Override
     public int iProgressGetResultCode(final String _this) throws RuntimeFaultMsg,
         InvalidObjectFaultMsg
     {
         Progress progress = DomainService.getInstance().getTasks().get(_this);
         if (progress == null)
         {
             // No session, log again
             throw new InvalidObjectFaultMsg("iProgressGetResultCode: No task. InvalidObjectFaultMsg...");
         }
         return 0;
     }
 
     @Override
     public boolean iProgressGetCompleted(final String _this) throws RuntimeFaultMsg,
         InvalidObjectFaultMsg
     {
         Progress session = DomainService.getInstance().getTasks().get(_this);
         if (session == null)
         {
             // No session, log again
             throw new InvalidObjectFaultMsg("iProgressGetCompleted: No task. InvalidObjectFaultMsg...");
         }
         return session.isCompleted();
     };
 
     @Override
     public String iProgressGetDescription(final String _this) throws RuntimeFaultMsg,
         InvalidObjectFaultMsg
     {
         return iProgressGetOperationDescription(_this);
     }
 
     @Override
     public String iProgressGetOperationDescription(final String _this) throws RuntimeFaultMsg,
         InvalidObjectFaultMsg
     {
 
         Progress progress = DomainService.getInstance().getTasks().get(_this);
         if (progress == null)
         {
             // No session, log again
             throw new InvalidObjectFaultMsg("iProgressGetOperationDescription: No task. InvalidObjectFaultMsg...");
         }
 
         return progress.getDescription();
     }
 
     @Override
     public long iProgressGetOperationPercent(final String _this) throws RuntimeFaultMsg,
         InvalidObjectFaultMsg
     {
         Progress progress = DomainService.getInstance().getTasks().get(_this);
         if (progress == null)
         {
             // No session, log again
             throw new InvalidObjectFaultMsg("iProgressGetOperationPercent: No task. InvalidObjectFaultMsg...");
         }
 
         return progress.getProgress();
     }
 
     @Override
     public String iVirtualBoxOpenMedium(final String _this, final String location,
         final DeviceType deviceType, final AccessMode accessMode, final boolean forceNewUuid)
         throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         Session session = DomainService.getInstance().getSession().get(_this);
         if (session == null)
         {
             // No session, log again
             throw new InvalidObjectFaultMsg("iVirtualBoxOpenMedium: No session. InvalidObjectFaultMsg...");
         }
         Set<Object> set = session.getData();
         Medium medium = new Medium();
         medium.setLocation(location);
         medium.setDeviceType(deviceType);
         medium.setClosed(Boolean.FALSE);
 
         set.add(medium);
         DomainService.getInstance().getMediums().put(medium.getId(), medium);
         return medium.getId();
     }
 
     @Override
     public String iMediumGetFormat(final String _this) throws RuntimeFaultMsg,
         InvalidObjectFaultMsg
     {
         Medium medium = DomainService.getInstance().getMediums().get(_this);
         if (medium == null)
         {
             throw new InvalidObjectFaultMsg("iMediumGetFormat: No medium " + _this
                 + ". InvalidObjectFaultMsg...");
         }
         return medium.getFormat();
     }
 
     @Override
     public long iMediumGetVariant(final String _this) throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         Medium medium = DomainService.getInstance().getMediums().get(_this);
 
         if (medium == null)
         {
             throw new InvalidObjectFaultMsg("iMediumGetFormat: No medium " + _this
                 + ". InvalidObjectFaultMsg...");
         }
         return medium.getVariant();
     }
 
     @Override
     public SessionState iSessionGetState(final String _this) throws RuntimeFaultMsg,
         InvalidObjectFaultMsg
     {
         Session session = DomainService.getInstance().getSession().get(_this);
 
         if (session == null)
         {
             // No session, log again
             throw new InvalidObjectFaultMsg("iSessionGetState: No session. InvalidObjectFaultMsg...");
         }
         return session.getState();
     }
 
     @Override
     public String iSessionGetMachine(final String _this) throws RuntimeFaultMsg,
         InvalidObjectFaultMsg
     {
 
         Session session = DomainService.getInstance().getSession().get(_this);
         if (session == null)
         { // No session, log again
             throw new InvalidObjectFaultMsg("iSessionGetMachine: No session. InvalidObjectFaultMsg...");
         }
 
         if (session.getVirtualMachine() == null)
         {
 
             throw new InvalidObjectFaultMsg("iSessionGetMachine: No more virtual machines. InvalidObjectFaultMsg...");
         }
         return session.getVirtualMachine().getId();
     }
 
     @Override
     public MediumState iMediumGetState(final String _this) throws RuntimeFaultMsg,
         InvalidObjectFaultMsg
     {
         Medium medium = DomainService.getInstance().getMediums().get(_this);
         if (medium == null)
         {
             throw new InvalidObjectFaultMsg("iMediumGetState: No medium " + _this
                 + ". InvalidObjectFaultMsg...");
         }
 
         return medium.getState();
     }
 
     @Override
     // Cannot find dispatch method for
     // {http://www.virtualbox.org/}INetworkAdapter_attachToBridgedInterface
     public void iNetworkAdapterAttachToBridgedInterface(final java.lang.String _this)
         throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
     };
 
     // Cannot find dispatch method for {http://www.virtualbox.org/}INetworkAdapter_setHostInterface
 
     @Override
     public void iNetworkAdapterSetHostInterface(final java.lang.String _this,
         final java.lang.String hostInterface) throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
     }
 
     @Override
     public void iMachineSaveSettings(final String _this) throws RuntimeFaultMsg,
         InvalidObjectFaultMsg
     {
         VirtualMachineInfo virtualMachine =
             DomainService.getInstance().getVirtualMachines().get(_this);
         if (virtualMachine == null)
         {
             throw new InvalidObjectFaultMsg("iMachineMemory no such machine id" + _this);
         }
         virtualMachine.setSaved(Boolean.TRUE);
     }
 
     @Override
     public String iMachineLaunchVMProcess(final String _this, final String session,
         final String type, final String environment) throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
 
         VirtualMachineInfo virtualMachine =
             DomainService.getInstance().getVirtualMachines().get(_this);
         if (virtualMachine == null)
         {
             throw new InvalidObjectFaultMsg("iMachineLaunchVMProcess no such machine id" + _this);
         }
         virtualMachine.setMachineState(MachineState.RUNNING);
         Progress progress = new Progress(getTimeToComplete("iMachineLaunchVMProcess"));
         progress.setDescription(type + " " + environment);
         DomainService.getInstance().getTasks().put(progress.getId(), progress);
         return progress.getId();
     }
 
     @Override
     public void iConsolePowerButton(final String _this) throws RuntimeFaultMsg,
         InvalidObjectFaultMsg
     {
         // TODO Auto-generated method stub
         super.iConsolePowerButton(_this);
     }
 
     @Override
     public String iConsolePowerDown(final String _this) throws RuntimeFaultMsg,
         InvalidObjectFaultMsg
     {
         VirtualMachineInfo virtualMachine =
             DomainService.getInstance().getVirtualMachines().get(_this);
         if (virtualMachine == null)
         {
             throw new InvalidObjectFaultMsg("iConsolePowerDown no such machine id" + _this);
         }
         virtualMachine.setMachineState(MachineState.POWERED_OFF);
         Progress progress = new Progress(getTimeToComplete("iConsolePowerDown"));
         progress.setDescription("PowerOff ");
         DomainService.getInstance().getTasks().put(progress.getId(), progress);
         return progress.getId();
     }
 
     @Override
     public String iConsolePowerUp(final String _this) throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         VirtualMachineInfo virtualMachine =
             DomainService.getInstance().getVirtualMachines().get(_this);
         if (virtualMachine == null)
         {
             throw new InvalidObjectFaultMsg("iConsolePowerUp no such machine id" + _this);
         }
         virtualMachine.setMachineState(MachineState.RUNNING);
         Progress progress = new Progress(getTimeToComplete("iConsolePowerUp"));
         progress.setDescription("Power");
         DomainService.getInstance().getTasks().put(progress.getId(), progress);
         return progress.getId();
     }
 
     @Override
     public void iConsoleResume(final String _this) throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         VirtualMachineInfo virtualMachine =
             DomainService.getInstance().getVirtualMachines().get(_this);
         if (virtualMachine == null)
         {
             throw new InvalidObjectFaultMsg("iConsoleResume no such machine id" + _this);
         }
         virtualMachine.setMachineState(MachineState.RUNNING);
         Progress progress = new Progress(getTimeToComplete("iConsoleResume"));
         progress.setDescription("Resume");
         DomainService.getInstance().getTasks().put(progress.getId(), progress);
     }
 
     @Override
     public void iConsolePause(final String _this) throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         VirtualMachineInfo virtualMachine =
             DomainService.getInstance().getVirtualMachines().get(_this);
         if (virtualMachine == null)
         {
             throw new InvalidObjectFaultMsg("iConsolePause no such machine id" + _this);
         }
         virtualMachine.setMachineState(MachineState.PAUSED);
         Progress progress = new Progress(getTimeToComplete("iConsolePause"));
         progress.setDescription("Pause");
         DomainService.getInstance().getTasks().put(progress.getId(), progress);
     }
 
     @Override
     public void iConsoleReset(final String _this) throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         // TODO Auto-generated method stub
         super.iConsoleReset(_this);
     }
 
     @Override
     public String iConsolePowerUpPaused(final String _this) throws RuntimeFaultMsg,
         InvalidObjectFaultMsg
     {
         VirtualMachineInfo virtualMachine =
             DomainService.getInstance().getVirtualMachines().get(_this);
         if (virtualMachine == null)
         {
             throw new InvalidObjectFaultMsg("iConsolePowerUpPaused no such machine id" + _this);
         }
         virtualMachine.setMachineState(MachineState.RUNNING);
         Progress progress = new Progress(getTimeToComplete("iConsolePowerUpPaused"));
         progress.setDescription("Resume");
         DomainService.getInstance().getTasks().put(progress.getId(), progress);
         return progress.getId();
     }
 
     @Override
     public void iMachineLockMachine(final String _this, final String session,
         final LockType lockType) throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         Session msession = DomainService.getInstance().getSession().get(session);
         if (msession == null)
         { // No session, log again
             throw new InvalidObjectFaultMsg("iSessionGetMachine: No session. InvalidObjectFaultMsg...");
         }
         VirtualMachineInfo virtualMachine =
             DomainService.getInstance().getVirtualMachines().get(_this);
         if (virtualMachine == null)
         {
             throw new InvalidObjectFaultMsg("iMachineMemory no such machine id" + _this);
         }
         msession.setVirtualMachine(virtualMachine);
     }
 
     @Override
     public void iSessionUnlockMachine(final String _this) throws RuntimeFaultMsg,
         InvalidObjectFaultMsg
     {
         Session session = DomainService.getInstance().getSession().get(_this);
         if (session == null)
         { // No session, log again
             throw new InvalidObjectFaultMsg("iSessionGetMachine: No session. InvalidObjectFaultMsg...");
         }
         session.setVirtualMachine(null);
     }
 
     @Override
     public String iSessionGetConsole(final String _this) throws RuntimeFaultMsg,
         InvalidObjectFaultMsg
     {
         Session session = DomainService.getInstance().getSession().get(_this);
         if (session == null)
         { // No session, log again
             throw new InvalidObjectFaultMsg("iSessionGetMachine: No session. InvalidObjectFaultMsg...");
         }
         return session.getConsole();
     }
 
     @Override
     public long iSystemPropertiesGetNetworkAdapterCount(final String _this) throws RuntimeFaultMsg,
         InvalidObjectFaultMsg
     {
         return Long.valueOf(DomainService.getInstance().getNetworkInterfaces().size());
     }
 
     @Override
     public String iVirtualBoxGetSystemProperties(final String _this) throws RuntimeFaultMsg,
         InvalidObjectFaultMsg
     {
         return DomainService.getInstance().getName();
     }
 
     @Override
     public String iNetworkAdapterGetHostInterface(final String _this) throws RuntimeFaultMsg,
         InvalidObjectFaultMsg
     {
         LOG.debug("iNetworkAdapterGetHostInterface " + _this);
 
         for (NetworkInterface networkInterface : DomainService.getInstance().getNetworkInterfaces())
         {
             if (networkInterface.getId().equalsIgnoreCase(_this))
             {
                 return networkInterface.getName();
             }
         }
 
         return "";
     }
 
     @Override
     public String iMachineDelete(final String _this, final List<String> aMedia)
         throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         DomainService.getInstance().getVirtualMachines().remove(_this);
 
         Progress progress = new Progress(getTimeToComplete("iMachineDelete"));
         progress.setDescription("Delete");
         DomainService.getInstance().getTasks().put(progress.getId(), progress);
         return progress.getId();
     }
 
     @Override
     public String iMachineGetStorageControllerByName(final String _this, final String name)
         throws RuntimeFaultMsg, InvalidObjectFaultMsg
     {
         VirtualMachineInfo virtualMachine =
             DomainService.getInstance().getVirtualMachines().get(_this);
         if (virtualMachine == null)
         {
             throw new InvalidObjectFaultMsg("iMachineGetStorageControllerByName no such machine id"
                 + _this);
         }
 
         for (Entry<String, Storage> e : virtualMachine.getStorages().entrySet())
         {
             if (e.getValue().getName().equals(name))
             {
                 return e.getValue().getId();
             }
         }
         return "";
 
     }
 
     @Override
     public long iStorageControllerGetPortCount(final String _this) throws RuntimeFaultMsg,
         InvalidObjectFaultMsg
     {
         Medium medium = DomainService.getInstance().getMediums().get(_this);
         if (medium == null || !(medium instanceof Storage))
         {
             throw new InvalidObjectFaultMsg("iStorageControllerGetPortCount no such medium id"
                 + _this);
         }
 
         return ((Storage) medium).getPortCount();
     }
 
     private long getTimeToComplete(final String method)
     {
 
         Long d = cache.get(method);
         if (d == null)
         {
             d = Long.valueOf(0);
             Number methodDelay =
                 ConfigurationService.getInstance().pathvalue(Number.class, Constants.BEHAVIOR,
                     method, Constants.TIME_TO_COMPLETE);
             if (methodDelay != null)
             {
                 d = methodDelay.longValue();
             }
 
             cache.put(method, d);
         }
 
         return d;
     }
 
 }
