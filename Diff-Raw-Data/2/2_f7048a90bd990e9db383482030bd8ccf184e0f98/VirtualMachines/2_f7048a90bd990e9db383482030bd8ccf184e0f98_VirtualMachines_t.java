 /**
  * Copyright (C) 2011-2012 enStratus Networks Inc
  *
  * ====================================================================
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * ====================================================================
  */
 
 package org.dasein.cloud.opsource.compute;
 
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 
 import org.apache.log4j.Logger;
 import org.dasein.cloud.*;
 import org.dasein.cloud.compute.*;
 
 import org.dasein.cloud.dc.Region;
 import org.dasein.cloud.identity.ServiceAction;
 import org.dasein.cloud.opsource.CallCache;
 import org.dasein.cloud.opsource.OpSource;
 import org.dasein.cloud.opsource.OpSourceMethod;
 import org.dasein.cloud.opsource.Param;
 import org.dasein.util.CalendarWrapper;
 import org.dasein.util.uom.storage.Gigabyte;
 import org.dasein.util.uom.storage.Megabyte;
 import org.dasein.util.uom.storage.Storage;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class VirtualMachines implements VirtualMachineSupport {
 	static public final Logger logger = OpSource.getLogger(VirtualMachines.class);
 
 	static private final String DESTROY_VIRTUAL_MACHINE = "delete";
 	static private final String CLEAN_VIRTUAL_MACHINE = "clean";
 	static private final String REBOOT_VIRTUAL_MACHINE = "reboot";
 	static private final String START_VIRTUAL_MACHINE = "start";
 	static private final String PAUSE_VIRTUAL_MACHINE = "shutdown";
     static private final String HARD_STOP_VIRTUAL_MACHINE = "poweroff";
     static private final String ADD_LOCAL_STORAGE = "addLocalStorage";
 	/** Node tag name */
 	//static private final String Deployed_Server_Tag = "Server";
 	static private final String Pending_Deployed_Server_Tag = "PendingDeployServer";
 
 	long waitTimeToAttempt = 30000L;
 
 	private OpSource provider;
 
 	public VirtualMachines(OpSource provider) {
 		this.provider = provider;
 	}    
 
 	public boolean attachDisk(String serverId, int sizeInGb) throws InternalException, CloudException {
 		HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 		Param param = new Param(OpSource.SERVER_BASE_PATH, null);
 		parameters.put(0, param);
 
 		param = new Param(serverId, null);
 		parameters.put(1, param);   
 
 		param = new Param("amount", String.valueOf(sizeInGb));
 		parameters.put(2, param);      	
 
 		OpSourceMethod method = new OpSourceMethod(provider, 
 				provider.buildUrl("addLocalStorage",true, parameters),
 				provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET", null));
 
 		Document doc = method.invoke();
 
 		return method.parseRequestResult("Attaching disk", doc , "result","resultDetail");
 	}
 
     @Override
 	public void start(@Nonnull String serverId) throws InternalException, CloudException {
 		if( logger.isTraceEnabled() ) {
 			logger.trace("ENTER: " + VirtualMachine.class.getName() + ".start()");
 		}
 		try{
 			HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 
 			Param param = new Param(OpSource.SERVER_BASE_PATH, null);
 			parameters.put(0, param);
 			param = new Param(serverId, null);
 			parameters.put(1, param);
 
 			OpSourceMethod method = new OpSourceMethod(provider,
 					provider.buildUrl(START_VIRTUAL_MACHINE,true, parameters),
 					provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET",null));
 			method.parseRequestResult("Booting vm",method.invoke(), "result", "resultDetail");
 		}
         finally{
 			if( logger.isTraceEnabled() ) {
 				logger.trace("EXIT: " + VirtualMachine.class.getName() + ".start()");
 			}
 		}
 	}
 
     private boolean cleanFailedVM(String serverId) throws InternalException, CloudException {
 		if( logger.isTraceEnabled() ) {
 			logger.trace("ENTER: " + VirtualMachine.class.getName() + ".cleanFailedVM()");
 		}
 		try{
 			HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 			Param param = new Param(OpSource.SERVER_BASE_PATH, null);
 			parameters.put(0, param);
 			param = new Param(serverId, null);
 			parameters.put(1, param);
 
 			OpSourceMethod method = new OpSourceMethod(provider,
 					provider.buildUrl(CLEAN_VIRTUAL_MACHINE,true, parameters),
 					provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET",null));
 			return method.parseRequestResult("Clean failed vm",method.invoke(),"result", "resultDetail");
 		}finally{
 			if( logger.isTraceEnabled() ) {
 				logger.trace("EXIT: " + VirtualMachine.class.getName() + ".cleanFailedVM()");
 			}
 		}
 	}
 
 
     @Override
     public VirtualMachine alterVirtualMachine(@Nonnull String serverId, @Nonnull VMScalingOptions vmScalingOptions) throws InternalException, CloudException {
         if(logger.isTraceEnabled()){
             logger.trace("ENTER: " + VirtualMachine.class.getName() + ".alterVirtualMachine()");
         }
         try{
             String[] parts;
             if(vmScalingOptions.getProviderProductId().contains(":")){
                 parts = vmScalingOptions.getProviderProductId().split(":");
             }
             else parts = new String[]{vmScalingOptions.getProviderProductId()};
 
             HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
             Param param = new Param(OpSource.SERVER_BASE_PATH, null);
             parameters.put(0, param);
             param = new Param(serverId, null);
             parameters.put(1, param);
 
             String requestBody = "";
             if(parts.length >= 1){
                 try{
                     int cpuCount = Integer.parseInt(parts[0]);
                     if(cpuCount > 0 && cpuCount <= 8){
                         requestBody = "cpuCount=" + cpuCount;
                     }
                     else throw new CloudException("Invalid CPU value. CPU count can only be up to 8.");
                 }
                 catch(Exception ex){
                     throw new CloudException("Invalid CPU value. Ensure you are using the format CPU:RAM:HDD");
                 }
             }
             if(parts.length >= 2){
                 try{
                     int memory = Integer.parseInt(parts[1]);
                     //TODO: This is temporary - RAM should always be in MB
                     if(memory < 100) memory = memory * 1024;
                     if(memory > 0 && memory <= 65536){
                        requestBody += "&memory=" + (memory);//Required to be in MB
                     }
                     else throw new CloudException("Invalid RAM value. RAM can only go up to 64GB.");
                 }
                 catch(Exception ex){
                     throw new CloudException("Invalid RAM value. Ensure you are using the format CPU:RAM:HDD");
                 }
             }
             OpSourceMethod method = new OpSourceMethod(provider,
                     provider.buildUrl(null, true, parameters),
                     provider.getBasicRequestParameters(OpSource.Content_Type_Value_Modify, "POST", requestBody));
             boolean success =  method.parseRequestResult("Alter vm", method.invoke(), "result", "resultDetail");
 
             if(success){
                 VirtualMachine vm = getVirtualMachine(serverId);
 
                 String currentProductId = vm.getProductId();
 
                 final int currentHDD = Integer.parseInt(currentProductId.substring(currentProductId.lastIndexOf(":") + 1));
                 if(parts.length >= 3){
                     try{
                         final int newHDD = Integer.parseInt(parts[2]);
                         if(newHDD > currentHDD){
                             final String fServerId = serverId;
                             Thread t = new Thread(){
                                 public void run(){
                                     provider.hold();
                                     try{
                                         try{
                                             int deltaHDD = newHDD - currentHDD;
                                             addLocalStorage(fServerId, deltaHDD);
                                         }
                                         catch (Throwable th){
                                             logger.debug("Alter VM failed while adding storage. CPU and RAM alteration may have been sucessful.");
                                         }
                                     }
                                     finally {
                                         provider.release();
                                     }
                                 }
                             };
                             t.setName("Alter OpSource VM: " + vm.getProviderVirtualMachineId());
                             t.setDaemon(true);
                             t.start();
                         }
                     }
                     catch(NumberFormatException ex){
                         throw new CloudException("Invalid format for HDD in product description.");
                     }
                 }
                 return getVirtualMachine(serverId);
             }
             else throw new CloudException("The attempt to alter the VM failed for an unknown reason");
         }
         finally {
             if(logger.isTraceEnabled()){
                 logger.trace("EXIT: " + VirtualMachine.class.getName() + ".alterVirtualMachine()");
             }
         }
     }
 
     private void addLocalStorage(String serverId, int storageSize){
         HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
         Param param = new Param(OpSource.SERVER_BASE_PATH, null);
         parameters.put(0, param);
         param = new Param(serverId, null);
         parameters.put(1, param);
 
         long timeout = System.currentTimeMillis() + (CalendarWrapper.MINUTE * 20L);
         Exception currentException = null;
         while( timeout > System.currentTimeMillis() ) {
             try{
                 OpSourceMethod method = new OpSourceMethod(provider,
                         provider.buildUrl(ADD_LOCAL_STORAGE + "&amount=" + storageSize, true, parameters),
                         provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET", null));
                 if(method.parseRequestResult("Alter vm - HDD", method.invoke(), "result", "resultDetail")){
                     currentException = null;
                     break;
                 }
                 else{
                     currentException = new CloudException("Modification failed without explanation");
                 }
             }
             catch (Exception ex){
                 logger.warn("Modification of local storage failed: " + ex.getMessage());
                 currentException = ex;
             }
             try { Thread.sleep(30000L); }
             catch( InterruptedException ignore ) { }
         }
         if( currentException == null ) {
             logger.info("Modification succeeded");
         }
         else {
             logger.error("Server could not be modified: " + currentException.getMessage());
             currentException.printStackTrace();
         }
     }
 
     @Override
 	public @Nonnull VirtualMachine clone(@Nonnull String serverId, @Nonnull String intoDcId, @Nonnull String name, @Nonnull String description, boolean powerOn, String ... firewallIds) throws InternalException, CloudException {
 		throw new OperationNotSupportedException("Instances cannot be cloned.");
 	}
 
     @Nullable
     @Override
     public VMScalingCapabilities describeVerticalScalingCapabilities() throws CloudException, InternalException {
         return VMScalingCapabilities.getInstance(false, true, Requirement.OPTIONAL, Requirement.OPTIONAL);//TODO: Check that this is correct for 2013.02
     }
 
     @Override
 	public void disableAnalytics(String vmId) throws InternalException, CloudException {
         // NO-OP
 	}
 
 	@Override
 	public void enableAnalytics(String vmId) throws InternalException, CloudException {
         // NO-OP
 	}
 
 	@Override
 	public @Nonnull String getConsoleOutput(@Nonnull String serverId) throws InternalException, CloudException {
 		return "";
 	}
 
     @Override
     public int getCostFactor(@Nonnull VmState vmState) throws InternalException, CloudException {
         return 0;  //TODO: Implement for 2013.01
     }
 
     @Override
     public int getMaximumVirtualMachineCount() throws CloudException, InternalException {
         return -2;
     }
 
     @Override
 	public @Nullable VirtualMachineProduct getProduct(@Nonnull String productId) throws InternalException, CloudException {
 		for( Architecture architecture : Architecture.values() ) {
 			for( VirtualMachineProduct product : listProducts(architecture) ) {
 				if( product.getProviderProductId().equals(productId) ) {
 					return product;
 				}
 			}
 		}
 		if( logger.isDebugEnabled() ) {
 			logger.debug("Unknown product ID for cloud.com: " + productId);
 		}
 		return null;
 	}
 
 	@Override
 	public @Nonnull String getProviderTermForServer(@Nonnull Locale locale) {
 		return "Server";
 	}
 
 	@Override
 	public VirtualMachine getVirtualMachine(@Nonnull String serverId) throws InternalException, CloudException {
 		HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 		Param param = new Param(OpSource.SERVER_BASE_PATH, null);
 		parameters.put(0, param);
 
 		param = new Param(serverId, null);
 		parameters.put(1, param);
 
 		OpSourceMethod method = new OpSourceMethod(provider,
 				provider.buildUrl(null,true, parameters),
 				provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET",null));
 
 		Document doc = method.invoke();
 
 		NodeList  matches = doc.getElementsByTagName("Server");
 		if(matches != null){
 			return toVirtualMachine(matches.item(0), false, "");
 		}
 		if( logger.isDebugEnabled() ) {
 			logger.debug("Can not identify VM with ID " + serverId);
 		}
 		return null;
 	}
 
 	public VirtualMachine getVirtualMachineByName(String name) throws InternalException, CloudException {
 		if( logger.isDebugEnabled() ) {
 			logger.debug("Identify VM with VM Name " + name);
 		}
 
 		ArrayList<VirtualMachine> list = (ArrayList<VirtualMachine>) listPendingServers();
 		for(VirtualMachine vm : list ){
 			if(vm.getName().equals(name)){
 				return vm;
 			}
 		}
 		list = (ArrayList<VirtualMachine>) listDeployedServers();
 		for(VirtualMachine vm : list ){
 			if(vm.getName().equals(name)){
 				return vm;
 			}
 		}
 		if( logger.isDebugEnabled() ) {
 			logger.debug("Can not identify VM with VM Name " + name);
 		}
 		return null;
 	}
 
 	@Override
 	public VmStatistics getVMStatistics(String serverId, long startTimestamp, long endTimestamp) throws InternalException, CloudException {
 		return new VmStatistics();
 	}
 
 	@Override
 	public @Nonnull Iterable<VmStatistics> getVMStatisticsForPeriod(@Nonnull String arg0, long arg1, long arg2) throws InternalException, CloudException {
 		return Collections.emptyList();
 	}
 
     @Nonnull
     @Override
     public Requirement identifyImageRequirement(@Nonnull ImageClass imageClass) throws CloudException, InternalException {
         return null;  //TODO: Implement for 2013.01
     }
 
     @Override
     public @Nonnull Requirement identifyPasswordRequirement() throws CloudException, InternalException {
         return identifyPasswordRequirement(Platform.UNKNOWN);
     }
 
     @Override
     public @Nonnull Requirement identifyPasswordRequirement(Platform platform) throws CloudException, InternalException{
         return Requirement.REQUIRED;
     }
 
     @Override
     public @Nonnull Requirement identifyRootVolumeRequirement() throws CloudException, InternalException {
         return Requirement.NONE;
     }
 
     @Override
     public @Nonnull Requirement identifyShellKeyRequirement() throws CloudException, InternalException {
         return identifyShellKeyRequirement(Platform.UNKNOWN);
     }
 
     @Override
     public @Nonnull Requirement identifyShellKeyRequirement(Platform platform) throws CloudException, InternalException{
         return Requirement.NONE;
     }
 
     @Nonnull
     @Override
     public Requirement identifyStaticIPRequirement() throws CloudException, InternalException {
         return Requirement.OPTIONAL;
     }
 
     @Override
     public @Nonnull Requirement identifyVlanRequirement() throws CloudException, InternalException {
         return Requirement.REQUIRED;
     }
 
     @Override
     public boolean isAPITerminationPreventable() throws CloudException, InternalException {
         return false;
     }
 
     @Override
     public boolean isBasicAnalyticsSupported() throws CloudException, InternalException {
         return false;
     }
 
     @Override
     public boolean isExtendedAnalyticsSupported() throws CloudException, InternalException {
         return false;
     }
 
     @Override
 	public boolean isSubscribed() throws CloudException, InternalException {
 		return true;
 	}
 
     @Override
     public boolean isUserDataSupported() throws CloudException, InternalException {
         return false;
     }
 
     @Override
     public @Nonnull VirtualMachine launch(final VMLaunchOptions withLaunchOptions) throws CloudException, InternalException {
         if( logger.isTraceEnabled() ) {
             logger.trace("ENTER - " + VirtualMachines.class.getName() + ".launch(" + withLaunchOptions + ")");
         }
         try {
             //VirtualMachineProduct product = getProduct(withLaunchOptions.getStandardProductId());
             String imageId = withLaunchOptions.getMachineImageId();
             String inZoneId = withLaunchOptions.getDataCenterId();
             final String name = withLaunchOptions.getHostName();
             String description = withLaunchOptions.getDescription();
             String withVlanId = withLaunchOptions.getVlanId();
             
             /** First step get the target image */
             if( logger.isInfoEnabled() ) {
                 logger.info("Fetching deployment information from the target image: " + imageId);
             }
             ServerImage imageSupport = provider.getComputeServices().getImageSupport();
             MachineImage origImage = imageSupport.getOpSourceImage(imageId);
             
             
             String productString = withLaunchOptions.getStandardProductId();
             // product id format cpu:ram:disk
             String cpuCount;
             String ramSize;
             String volumeSizes;
             String[] productIds = productString.split(":");
             if (productIds.length == 3) {
             	cpuCount = productIds[0];
             	ramSize = productIds[1];
                 try{
                     //TODO: This is temporary - All ram should be in MB
                     if(Integer.parseInt(ramSize) < 100){
                         ramSize = (Integer.parseInt(ramSize) * 1024) + "";
                     }
                 }
                 catch(NumberFormatException ex){
                     throw new InternalException("Invalid value specified for RAM in product id string");
                 }
             	volumeSizes = productIds[2];
             }
             else {
                 throw new InternalError("Invalid product id string");
             }
             
             
             if( origImage == null ) {
                 logger.error("No such image to launch VM: " + imageId);
                 throw new CloudException("No such image to launch VM: " + imageId);
             }
 
             final int targetCPU = Integer.parseInt(cpuCount);
             final int targetMemory = Integer.parseInt(ramSize);
             final int targetDisk = Integer.parseInt(volumeSizes);
 
             final int currentCPU = (origImage.getTag("cpuCount") == null) ? 0 : Integer.valueOf((String)origImage.getTag("cpuCount"));
             final int currentMemory = (origImage.getTag("memory") == null) ? 0 : Integer.valueOf((String)origImage.getTag("memory"));
             final int currentDisk = 10;
             
             if( logger.isDebugEnabled() ) {
                 logger.debug("Launch request for " + targetCPU + "/" + targetMemory + "/" + targetDisk + " against " + currentCPU + "/" + currentMemory);
             }
 
             String password = getRandomPassword();
             if( targetDisk == 0 && currentCPU == targetCPU && currentMemory == targetMemory ){
                 if( deploy(origImage.getProviderMachineImageId(), inZoneId, name, description, withVlanId, password, "true") ) {
                     return getVirtualMachineByName(name);
                 }
                 else {
                     throw new CloudException("Fail to launch the server");
                 }
 
             }
             else if( targetDisk == 0 && ((targetCPU == 1 && targetMemory == 2048) || (targetCPU == 2 && targetMemory == 4096) || (targetCPU == 4 && targetMemory == 6144))){
                 /**  If it is Opsource OS, then get the target image with the same cpu and memory */
                 MachineImage targetImage = imageSupport.searchImage(origImage.getPlatform(), origImage.getArchitecture(), targetCPU, targetMemory);
 
                 if(targetImage != null) {
                     if( deploy(targetImage.getProviderMachineImageId(), inZoneId, name, description, withVlanId, password, "true") ){
                         return getVirtualMachineByName(name);
                     }
                     else {
                         throw new CloudException("Fail to launch the server");
                     }
                 }
             }
             logger.info("Need to modify server after deployment, pursuing a multi-step deployment operation");
             /** There is target image with the CPU and memory required, then need to modify the server after deploying */
 
             /** Second step deploy VM */
 
             if( !deploy(imageId, inZoneId, name, description, withVlanId, password, "false") ) {
                 throw new CloudException("Fail to deploy VM without further information");
             }
 
             final VirtualMachine server = getVirtualMachineByName(name);
 
             /** update the hardware (CPU, memory configuration)*/
             if(server == null){
                 throw new CloudException("Server failed to deploy without explaination");
             }
             Thread t = new Thread() {
                 public void run() {
                     provider.hold();
                     try {
                         try {
                             configure(server, name, currentCPU, currentMemory, currentDisk, targetCPU, targetMemory, targetDisk);
                         }
                         catch( Throwable t ) {
                             logger.error("Failed to complete configuration of " + server.getProviderVirtualMachineId() + " in OpSource: " + t.getMessage());
                             t.printStackTrace();
                         }
                     }
                     finally {
                         provider.release();
                     }
                 }
             };
             t.setName("Configure OpSource VM " + server.getProviderVirtualMachineId());
             t.setDaemon(true);
             t.start();
 
             return server;
         }
         finally{
             CallCache.getInstance().resetCacheTimer(OpSource.LOCATION_BASE_PATH);
             if( logger.isTraceEnabled() ) {
                 logger.trace("EXIT: " + VirtualMachine.class.getName() + ".launch()");
             }
         }
     }
 
     private void configure(VirtualMachine server, String name, int currentCPU, int currentMemory, int currentDisk, int targetCPU, int targetMemory, int targetDisk) {
         if( logger.isTraceEnabled() ) {
             logger.trace("ENTER - " + VirtualMachines.class.getName() + ".configure(" + server + "," + name + "," + currentCPU + "," + currentMemory + "," + currentDisk + "," + targetCPU + "," + targetMemory + "," + targetDisk + ")");
         }
         try {
             if( logger.isInfoEnabled() ) {
                 logger.info("Configuring " + server.getName() + " [#" + server.getProviderVirtualMachineId() + "] - " + server.getCurrentState());
             }
             if( currentCPU != targetCPU || currentMemory != targetMemory ) {
                 if( logger.isInfoEnabled() ) {
                     logger.info("Need to reconfigure CPU and/or memory");
                 }
                 /** Modify server to target cpu and memory */
 
                 /** VM has finished deployment before continuing, therefore wait 15s */
                 try {
                     server = getVirtualMachineByName(name);
                 }
                 catch( Exception e ) {
                     logger.warn("Unable to load server for configuration: " + e.getMessage());
                 }
                 if( server == null ) {
                     logger.error("Server disappeared while waiting for deployment to complete");
                     return;
                 }
                 currentCPU = Integer.valueOf((String) server.getTag("cpuCount"));
                 currentMemory = Integer.valueOf((String) server.getTag("memory"));
 
                 if( currentCPU != targetCPU || currentMemory != targetMemory ) {
                     long timeout = System.currentTimeMillis() + (CalendarWrapper.MINUTE * 20L);
 
                     Exception currentException = null;
 
                     logger.info("Beginning modification process...");
                     while( timeout > System.currentTimeMillis() ) {
                         try {
                             if( modify(server.getProviderVirtualMachineId(), targetCPU, targetMemory) ) {
                                 currentException = null;
                                 break;
                             }
                             else {
                                 currentException = new CloudException("Modification failed without explanation");
                             }
                         }
                         catch( Exception e ) {
                             logger.warn("Modification of CPU and Memory failed: " + e.getMessage());
                             currentException = e;
                         }
                         try { Thread.sleep(30000L); }
                         catch( InterruptedException ignore ) { }
                     }
                     if( currentException == null ) {
                         logger.info("Modification of CPU and Memory succeeded");
                     }
                     else {
                         logger.error("Server could not be modified: " + currentException.getMessage());
                         currentException.printStackTrace();
                     }
                 }
             }
             /** Third Step: attach the disk */
             if( targetDisk != currentDisk) {
                 if( logger.isInfoEnabled() ) {
                     logger.info("Need to reconfigure for disk: " + currentDisk + " vs " + targetDisk);
                 }
                 long timeout = System.currentTimeMillis() + (CalendarWrapper.MINUTE * 20L);
                 Exception currentException = null;
 
                 /** Update usually take another 6 mins */
                 while( System.currentTimeMillis() < timeout ) {
                     try {
                         server = getVirtualMachineByName(name);
                         if( server == null ) {
                             logger.error("Lost access to server while attempting to attach disk");
                             return;
                         }
                     }
                     catch( Exception e ) {
                         logger.warn("Unable to load the server's current state, praying the old one works: " + e.getMessage());
                     }
 
                     if( server.getProductId() != null ) {
                         try {
                             VirtualMachineProduct prd = getProduct(server.getProductId());
 
                             if( prd != null && prd.getRootVolumeSize().intValue() == targetDisk ) {
                                 if( logger.isInfoEnabled() ) {
                                     logger.info("Target match, aborting attachment for " + server.getProviderVirtualMachineId());
                                 }
                                 break;
                             }
                             if( attachDisk(server.getProviderVirtualMachineId(), targetDisk) ){
                                 if( logger.isInfoEnabled() ) {
                                     logger.info("Attach succeeded for " + server.getProviderVirtualMachineId());
                                 }
                                 break;
                             }
                         }
                         catch( Exception e ) {
                             logger.warn("Error during attach: " + e.getMessage());
                             currentException = e;
                         }
                     }
                     try { Thread.sleep(30000L); }
                     catch( InterruptedException ignore ) { }
                 }
                 if( currentException != null ) {
                     logger.error("Unable to attach disk: " + currentException.getMessage());
                     currentException.printStackTrace();
                 }
             }
             /**  Fourth Step: boot the server */
             /** Update usually take another 10 mins, wait 5 minutes first */
             long timeout = System.currentTimeMillis() + (CalendarWrapper.MINUTE * 15L);
 
             if( logger.isInfoEnabled() ) {
                 logger.info("Booting " + server.getProviderVirtualMachineId());
             }
             while( System.currentTimeMillis() < timeout ) {
                 try {
                     /** Begin to start the VM */
                     server = getVirtualMachineByName(name);
                     if( server == null ) {
                         logger.error("Server disappeared while performing bootup");
                         return;
                     }
                     if( server.getCurrentState().equals(VmState.RUNNING)) {
                         if( logger.isInfoEnabled() ) {
                             logger.info(server.getProviderVirtualMachineId() + " is now RUNNING");
                         }
                         return;
                     }
                     else if( server.getCurrentState().equals(VmState.STOPPED) ) {
                         start(server.getProviderVirtualMachineId());
                     }
                 }
                 catch( Exception e ) {
                     logger.warn("Error during boot process, maybe retry?: " + e.getMessage());
                 }
                 try { Thread.sleep(15000L); }
                 catch( InterruptedException ignore ) { }
             }
         }
         finally {
             if( logger.isTraceEnabled() ) {
                 logger.trace("EXIT - " + VirtualMachines.class.getName() + ".configure()");
             }
         }
     }
 
     @Deprecated
     @Override
 	public @Nonnull VirtualMachine launch(@Nonnull String imageId, @Nonnull VirtualMachineProduct product, @Nonnull String inZoneId, @Nonnull String name, @Nonnull String description, String usingKey, String withVlanId, boolean withMonitoring, boolean asSandbox, String... protectedByFirewalls) throws InternalException, CloudException {
 		return launch(imageId, product, inZoneId, name, description, usingKey, withVlanId, withMonitoring, asSandbox, protectedByFirewalls, new Tag[0]);
 	}
 
     @Deprecated
 	public @Nonnull VirtualMachine launch(@Nonnull String fromMachineImageId, @Nonnull VirtualMachineProduct product, @Nonnull String dataCenterId, @Nonnull String name, @Nonnull String description, @Nullable String withKeypairId, @Nullable String inVlanId, boolean withMonitoring, boolean asSandbox, @Nullable String[] firewalls, @Nullable Tag ... tags) throws InternalException, CloudException {
         VMLaunchOptions options;
 
         if( inVlanId == null ) {
             options = VMLaunchOptions.getInstance(product.getProviderProductId(), fromMachineImageId, name, description).inDataCenter(dataCenterId);
         }
         else {
             options = VMLaunchOptions.getInstance(product.getProviderProductId(), fromMachineImageId, name, description).inVlan(null, dataCenterId, inVlanId);
         }
         if( withKeypairId != null ) {
             options = options.withBoostrapKey(withKeypairId);
         }
         if( tags != null ) {
             for( Tag t : tags ) {
                 options = options.withMetaData(t.getKey(), t.getValue());
             }
         }
         if( firewalls != null ) {
             options = options.behindFirewalls(firewalls);
         }
         return launch(options);
 	}
 
 	private boolean deploy(@Nonnull String imageId, String inZoneId, String name, String description, String withVlanId, String adminPassword, String isStart) throws InternalException, CloudException {
 		inZoneId = translateZone(inZoneId);
 		/** Create post body */
 		Document doc = provider.createDoc();
 		Element server = doc.createElementNS("http://oec.api.opsource.net/schemas/server", "Server");
 
 		Element nameElmt = doc.createElement("name");
 		nameElmt.setTextContent(name);
 
 		Element descriptionElmt = doc.createElement("description");
 		descriptionElmt.setTextContent(description);
 
 		if(withVlanId == null){
 			withVlanId = provider.getDefaultVlanId();
 		}
 
 		Element vlanResourcePath = doc.createElement("vlanResourcePath");
 		vlanResourcePath.setTextContent(provider.getVlanResourcePathFromVlanId(withVlanId));
 
 		Element imageResourcePath = doc.createElement("imageResourcePath");
 		imageResourcePath.setTextContent(provider.getImageResourcePathFromImaged(imageId));
 
         if(adminPassword == null){
             adminPassword = getRandomPassword();
         }
         else{
             if(adminPassword.length() < 8){
                 throw new InternalException("Password require a minimum of 8 characters!!!");
             }
         }
 
         Element administratorPassword = doc.createElement("administratorPassword");
         administratorPassword.setTextContent(adminPassword);
 
 		Element isStartElmt = doc.createElement("isStarted");
 
 		isStartElmt.setTextContent(isStart);
 
 		server.appendChild(nameElmt);
         server.appendChild(descriptionElmt);
         server.appendChild(vlanResourcePath);
         server.appendChild(imageResourcePath);
         server.appendChild(administratorPassword);
 
         server.appendChild(isStartElmt);
         doc.appendChild(server);
 
         HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 
 		Param param = new Param(OpSource.SERVER_BASE_PATH, null);
 		parameters.put(0, param);
 
 		OpSourceMethod method = new OpSourceMethod(provider,
 				provider.buildUrl(null,true, parameters),
 				provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "POST", provider.convertDomToString(doc)));
 		return method.parseRequestResult("Deploying server",method.invoke(), "result", "resultDetail");
 	}
 
 
 	@Override
 	public @Nonnull Iterable<String> listFirewalls(@Nonnull String vmId) throws InternalException, CloudException {
 		/** Firewall Id is the same as the network ID*/
 		VirtualMachine vm = this.getVirtualMachine(vmId);
 
 		if(vm == null){
 			return Collections.emptyList();
 		}
 		String networkId = vm.getProviderVlanId();
 		if(networkId != null){
 			ArrayList<String> list = new ArrayList<String>();
 			list.add(networkId);
 			return list;
 		}
 		return Collections.emptyList();
 	}
 
 	@Override
 	public Iterable<VirtualMachineProduct> listProducts(Architecture architecture) throws InternalException, CloudException {
 		List<VirtualMachineProduct> products = new ArrayList<VirtualMachineProduct>();
 
 		VirtualMachineProduct product;
 		/** OpSource enables any combination of CPU (1 -8 for East 1-4 or west) and RAM (1 - 64G for East and 1-32G for west) */
 
 		int maxCPUNum = 0, maxMemInGB =0,  diskSizeInGB = 0, maxMemInMB = 0;
 
 		/** Obtain the maximum CPU and Memory for each data center */
 		String regionId = provider.getDefaultRegionId();
 		HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 		Param param = new Param(OpSource.LOCATION_BASE_PATH, null);
 		parameters.put(0, param);
 
 		/*OpSourceMethod method = new OpSourceMethod(provider,
 				provider.buildUrl(null,true, parameters),
 				provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET",null));
 
 		Document doc = method.invoke();*/
         Document doc = CallCache.getInstance().getAPICall(OpSource.LOCATION_BASE_PATH, provider, parameters, "");
         String sNS = "";
         try{
             sNS = doc.getDocumentElement().getTagName().substring(0, doc.getDocumentElement().getTagName().indexOf(":") + 1);
         }
         catch(IndexOutOfBoundsException ex){}
 		NodeList blocks = doc.getElementsByTagName(sNS + "datacenterWithLimits");
 
 		if(blocks != null){
 			for(int i=0; i< blocks.getLength();i++){
 				Node item = blocks.item(i);
 
 				RegionComputingPower r = toRegionComputingPower(item, sNS);
 				if( r.getProviderRegionId().equals(regionId)){
 					maxCPUNum = r.getMaxCPUNum();
 					maxMemInMB = r.getMaxMemInMB();
 				}
 			}
 		}
 
 		for( int disk = 0 ; disk < 6; disk ++ ){
 			diskSizeInGB = disk * 50;
 
 			for(int cpuNum =1;cpuNum <= maxCPUNum;cpuNum ++){
 				/**
 				 * Default cpuNum = 1, 2, max ram = 8
 				 * cpuNum = 3, 4, min ram 4, max ram = 32
 				 * cpuNum = 1, 2, max ram = 8
 				 */
 				int ramInMB = 1024*cpuNum;
 				if(cpuNum <=2){
 					ramInMB = 1024;
 				}
 				while((ramInMB/1024) <= 4*cpuNum && ramInMB <=  maxMemInMB){
 					product = new VirtualMachineProduct();
 					product.setProviderProductId(cpuNum + ":" + ramInMB + ":" + diskSizeInGB);
 					product.setName(" (" + cpuNum + " CPU/" + ramInMB + " MB RAM/" + diskSizeInGB + " GB Disk)");
 					product.setDescription(" (" + cpuNum + " CPU/" + ramInMB + " MB RAM/" + diskSizeInGB + " GB Disk)");
 					product.setRamSize(new Storage<Megabyte>(ramInMB, Storage.MEGABYTE));
 					product.setCpuCount(cpuNum);
 					product.setRootVolumeSize(new Storage<Gigabyte>(diskSizeInGB, Storage.GIGABYTE));
 					products.add(product);
 
 					if(cpuNum <=2){
 						ramInMB = ramInMB + 1024;
 					}else{
 						ramInMB = ramInMB + ramInMB;
 					}
 				}
 			}
 		}
 		return products;
 	}
 
     @Override
     public Iterable<Architecture> listSupportedArchitectures() throws InternalException, CloudException {
         return Collections.singletonList(Architecture.I64);
     }
 
     @Nonnull
     @Override
     public Iterable<ResourceStatus> listVirtualMachineStatus() throws InternalException, CloudException {
         return null;  //TODO: Implement for 2013.01
     }
 
     @Override
 	public @Nonnull Iterable<VirtualMachine> listVirtualMachines() throws InternalException, CloudException {
 		ArrayList<VirtualMachine> allList = new ArrayList<VirtualMachine>();
 		/** List the pending Server first */
 		ArrayList<VirtualMachine> list = (ArrayList<VirtualMachine>) listPendingServers();
 
 		if(list != null){
 			allList.addAll(list);
 		}
 		/** List the deployed Server */
 		list = (ArrayList<VirtualMachine>) listDeployedServers();
 		if(list != null){
 			allList.addAll(list);
 		}
 		return allList;
 	}
 
     @Override
     public void pause(@Nonnull String vmId) throws InternalException, CloudException {
         throw new OperationNotSupportedException("Pause/unpause is not supported");
     }
 
     private Iterable<VirtualMachine> listDeployedServers() throws InternalException, CloudException {
 		ArrayList<VirtualMachine> list = new ArrayList<VirtualMachine>();
 
 		/** Get deployed Server */
 		HashMap<Integer, Param> parameters = new HashMap<Integer, Param>();
 		Param param = new Param(OpSource.SERVER_BASE_PATH, null);
 		parameters.put(0, param);
 		param = new Param("deployed", null);
 		parameters.put(1, param);
 
 		OpSourceMethod method = new OpSourceMethod(provider,
 				provider.buildUrl(null, true, parameters),
 				provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET",null));
 
 		Document doc = method.invoke();
 
 		NodeList matches = doc.getElementsByTagName("DeployedServer");
 
 		if(matches != null){
 			for( int i=0; i<matches.getLength(); i++ ) {
 				Node node = matches.item(i);
 				VirtualMachine vm = this.toVirtualMachine(node, false, "");
 				if( vm != null ) {
 					list.add(vm);
 				}
 			}
 		}
 		return list;
 	}
 
 
 	private Iterable<VirtualMachine> listPendingServers() throws InternalException, CloudException {
 		ArrayList<VirtualMachine> list = new ArrayList<VirtualMachine>();
 
 		/** Get pending deploy server */
 		HashMap<Integer, Param> parameters = new HashMap<Integer, Param>();
 		Param param = new Param(OpSource.SERVER_BASE_PATH, null);
 		parameters.put(0, param);
 		param = new Param("pendingDeploy", null);
 		parameters.put(1, param);
 
 		OpSourceMethod method = new OpSourceMethod(provider,
 				provider.buildUrl(null, true, parameters),
 				provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET",null));
 
 		Document doc = method.invoke();
 
 		NodeList matches = doc.getElementsByTagName(Pending_Deployed_Server_Tag);
 		if(matches != null){
 			for( int i=0; i<matches.getLength(); i++ ) {
 				Node node = matches.item(i);
 				VirtualMachine vm = this.toVirtualMachine(node, true, "");
 				if( vm != null ) {
 					list.add(vm);
 				}
 			}
 		}
 		return list;
 	}
 
 
 	@Override
 	public @Nonnull String[] mapServiceAction(@Nonnull ServiceAction action) {
 		return new String[0];
 	}
 
 	/** Modify VM with the cpu and memory */
 	private boolean modify(String serverId, int cpuCount, int memoryInMb ) throws InternalException, CloudException {
 
 		HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 		Param param = new Param(OpSource.SERVER_BASE_PATH, null);
 		parameters.put(0, param);
 		param = new Param(serverId, null);
 		parameters.put(1, param);
 
 		/** Create post body */
 		String requestBody = "cpuCount=";
 		requestBody += cpuCount;
 		requestBody += "&memory=" + memoryInMb;
 
 		OpSourceMethod method = new OpSourceMethod(provider,
 				provider.buildUrl(null,true, parameters),
 				provider.getBasicRequestParameters(OpSource.Content_Type_Value_Modify, "POST", requestBody));
 		return method.parseRequestResult("Modify vm",method.invoke(), "result", "resultDetail");
 	}
 
 	@Override
 	public void stop(@Nonnull String serverId) throws InternalException, CloudException {
 		HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 		Param param = new Param(OpSource.SERVER_BASE_PATH, null);
 		parameters.put(0, param);
 		param = new Param(serverId, null);
 		parameters.put(1, param);
 
 		/** Gracefully power off */
 		OpSourceMethod method = new OpSourceMethod(provider,
 				provider.buildUrl(PAUSE_VIRTUAL_MACHINE,true, parameters),
 				provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET",null));
 		method.parseRequestResult("Pausing vm",method.invoke(),"result","resultDetail");
 	}
 
     @Override
     public void stop(@Nonnull String serverId, boolean hardOff) throws InternalException, CloudException {
         if(!hardOff)stop(serverId);
         else{
             HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
             Param param = new Param(OpSource.SERVER_BASE_PATH, null);
             parameters.put(0, param);
             param = new Param(serverId, null);
             parameters.put(1, param);
 
             OpSourceMethod method = new OpSourceMethod(provider,
                     provider.buildUrl(HARD_STOP_VIRTUAL_MACHINE,true, parameters),
                     provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET", null));
             method.parseRequestResult("Stopping vm",method.invoke(),"result","resultDetail");
         }
     }
 
 
     @Override
 	public void reboot(@Nonnull String serverId) throws CloudException, InternalException {
 		HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 		Param param = new Param(OpSource.SERVER_BASE_PATH, null);
 		parameters.put(0, param);
 		param = new Param(serverId, null);
 		parameters.put(1, param);
 
 		OpSourceMethod method = new OpSourceMethod(provider,
 				provider.buildUrl(REBOOT_VIRTUAL_MACHINE,true, parameters),
 				provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET",null));
 		method.parseRequestResult("Rebooting vm",method.invoke(),"result","resultDetail");
 	}
 
     @Override
     public void resume(@Nonnull String vmId) throws CloudException, InternalException {
         throw new OperationNotSupportedException("Suspend/resume is not supported");
     }
 
     @Override
 	public boolean supportsAnalytics() throws CloudException, InternalException {
 		return false;
 	}
 
     @Override
     public boolean supportsPauseUnpause(@Nonnull VirtualMachine vm) {
         return false;
     }
 
     @Override
     public boolean supportsStartStop(@Nonnull VirtualMachine vm) {
         return true;
     }
 
     @Override
     public boolean supportsSuspendResume(@Nonnull VirtualMachine vm) {
         return false;
     }
 
     @Override
     public void suspend(@Nonnull String vmId) throws CloudException, InternalException {
         throw new OperationNotSupportedException("Suspend/resume is not supported");
     }
 
     @Override
 	public void terminate(@Nonnull String serverId) throws InternalException, CloudException {
         if( logger.isTraceEnabled() ) {
             logger.trace("ENTER - " + VirtualMachines.class.getName() + ".terminate(" + serverId + ")");
         }
         try {
             if( logger.isInfoEnabled() ) {
                 logger.info("Beginning termination process for server " + serverId);
             }
             VirtualMachine server = getVirtualMachine(serverId);
 
             if( logger.isInfoEnabled() ) {
                 logger.info("Current state for " + serverId + ": " + (server == null ? "TERMINATED" : server.getCurrentState()));
             }
             if( server == null ) {
                 return;
             }
 
             /** Release public IP first */
             if( logger.isInfoEnabled() ) {
                 logger.info("Releasing public IP prior to termination...");
             }
             if( server.getPublicIpAddresses() != null ) {
                 for(String addressId : server.getPublicIpAddresses()){
                     provider.getNetworkServices().getIpAddressSupport().releaseFromServer(addressId);
                 }
             }
 
             /** Now Stop the vm */
             if( logger.isInfoEnabled() ) {
                 logger.info("Stopping the server " + serverId + " prior to termination...");
             }
             long timeout = System.currentTimeMillis() + (CalendarWrapper.MINUTE * 20L);
 
             while( System.currentTimeMillis() < timeout ) {
                 try {
                     /** If it is pending, means it is in deployment process, need around 6 mins */
                     server = getVirtualMachine(serverId);
 
                     if( server == null ) {
                         /** VM already killed */
                         return;
                     }
                     if( server.getCurrentState().equals(VmState.STOPPED) ) {
                         break;
                     }
                     if( server.getCurrentState().equals(VmState.RUNNING) ){
                         stop(serverId);
                         break;
                     }
 
                 }
                 catch( Throwable t ) {
                     logger.warn("Error stopping VM: " + t.getMessage());
                 }
                 try { Thread.sleep(30000L); }
                 catch( InterruptedException ignore ) { }
             }
             if( logger.isInfoEnabled() ) {
                 logger.info("Waiting for server " + serverId + " to be STOPPED...");
             }
             timeout = System.currentTimeMillis() + (CalendarWrapper.MINUTE * 10L);
             while( System.currentTimeMillis() < timeout ) {
                 try {
                     server = getVirtualMachine(serverId);
                     if( server == null ) {
                         return;
                     }
                     if( server.getCurrentState().equals(VmState.TERMINATED) ) {
                         return;
                     }
                     if( server.getCurrentState().equals(VmState.STOPPED) ) {
                         break;
                     }
                 }
                 catch( Throwable t ) {
                     logger.warn("Error stopping VM: " + t.getMessage());
                 }
                 try { Thread.sleep(30000L); }
                 catch( InterruptedException ignore ) { }
             }
             if( logger.isInfoEnabled() ) {
                 logger.info("Finally terminating " + serverId + " now that it is STOPPED");
             }
             timeout = System.currentTimeMillis() + (CalendarWrapper.MINUTE * 10L);
             while( System.currentTimeMillis() < timeout ) {
                 try {
                     String  resultCode = killVM(serverId);
 
                     if( logger.isDebugEnabled() ) {
                         logger.debug("Server " + serverId + " termination result: " + resultCode);
                     }
                     if( resultCode.equals("REASON_0") ){
                         break;
                     }
                     else if( resultCode.equals("REASON_395") ){
                         logger.error(resultCode + ": Could not find VM " + serverId);
                         throw new CloudException(resultCode + ": Could not find VM " + serverId);
                     }
                     else if(resultCode.equals("REASON_100")){
                         logger.error(resultCode + ": Illegal access");
                         throw new CloudException(resultCode + ": Illegal access");
                     }
                     else if(resultCode.equals("REASON_393")){
                         logger.error("The server with " + serverId + " is associated with a Real-Server in load balancer");
                         throw new CloudException("The server with " + serverId + " is associated with a Real-Server in load balancer");
                     }
                     else {
                         try {
                             Thread.sleep(waitTimeToAttempt);
                             logger.info("Cleaning failed deployment for " + serverId);
                             cleanFailedVM(serverId);
                         }
                         catch( Throwable ignore ) {
                             // ignore
                         }
                     }
                 }
                 catch( CloudException e ) {
                     logger.warn("Failed termination attempt: " + e.getMessage());
                     try{
                         Thread.sleep(waitTimeToAttempt);
                         logger.info("Cleaning failed deployment for " + serverId);
                         cleanFailedVM(serverId);
                     }
                     catch( Throwable ignore ) {
                         // ignore
                     }
                 }
             }
             if( logger.isInfoEnabled() ) {
                 logger.info("Waiting for " + serverId + " to be TERMINATED...");
             }
             while( System.currentTimeMillis() < timeout ) {
                 VirtualMachine vm = getVirtualMachine(serverId);
 
                 if( vm == null || VmState.TERMINATED.equals(vm.getCurrentState()) ) {
                     if( logger.isInfoEnabled() ) {
                         logger.info("VM " + serverId + " successfully TERMINATED");
                     }
                     return;
                 }
                 try { Thread.sleep(30000L); }
                 catch( InterruptedException ignore ) { }
             }
             logger.warn("System timed out waiting for " + serverId + " to complete termination");
         }
         finally {
             if( logger.isTraceEnabled() ) {
                 logger.trace("EXIT - " + VirtualMachines.class.getName() + ".terminate()");
             }
         }
 	}
 
     @Override
     public void unpause(@Nonnull String vmId) throws CloudException, InternalException {
         throw new OperationNotSupportedException("Pause/unpause is not supported");
     }
 
     @Override
     public void updateTags(@Nonnull String s, @Nonnull Tag... tags) throws CloudException, InternalException {
         //TODO: Implement for 2013.01
     }
 
     private String killVM(String serverId) throws InternalException, CloudException {
 		HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 		Param param = new Param(OpSource.SERVER_BASE_PATH, null);
 		parameters.put(0, param);
 		param = new Param(serverId, null);
 		parameters.put(1, param);
 
 		OpSourceMethod method = new OpSourceMethod(provider,
 				provider.buildUrl(DESTROY_VIRTUAL_MACHINE,true, parameters),
 				provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET",null));
         CallCache.getInstance().resetCacheTimer(OpSource.LOCATION_BASE_PATH);
 		return method.requestResultCode("Terminating vm",method.invoke(),"resultCode");
 	}
 
 	private String translateZone(String zoneId) throws InternalException, CloudException {
 		if( zoneId == null ) {
 			for( Region r : provider.getDataCenterServices().listRegions() ) {
 				zoneId = r.getProviderRegionId();
 				break;
 			}
 		}
 		/*if(zoneId.endsWith("a")){
 			zoneId = zoneId.substring(0, zoneId.length()-1);       	
 		}*/
 		return zoneId;
 	}
 
 	private VirtualMachineProduct getProduct(Architecture architecture, int cpuCout, int memoryInSize, int diskInGB) throws InternalException, CloudException{
 
 		for( VirtualMachineProduct product : listProducts(architecture) ) {
 			if( product.getCpuCount() == cpuCout && product.getRamSize().intValue() == memoryInSize  && diskInGB == product.getRootVolumeSize().intValue() ) {
 				return product;
 			}
 		}      
 		return null;
 	}
 
 	private VirtualMachine toVirtualMachine(Node node, Boolean isPending, String nameSpace) throws CloudException, InternalException {
 		if( node == null ) {
 			return null;
 		}
 		HashMap<String,String> properties = new HashMap<String,String>();
 		VirtualMachine server = new VirtualMachine();
 		NodeList attributes = node.getChildNodes();
 
 		Architecture bestArchitectureGuess = Architecture.I64;
 
 		server.setTags(properties);
 
 		if(isPending){
 			server.setCurrentState(VmState.PENDING);
 			server.setImagable(false);
 		}else{
 			server.setCurrentState(VmState.RUNNING);
 			server.setImagable(true);
 		}        
 		server.setProviderOwnerId(provider.getContext().getAccountNumber());
 		server.setClonable(false);        
 		server.setPausable(true);
 		server.setPersistent(true);
 
 		server.setProviderRegionId(provider.getContext().getRegionId());
 
 		for( int i=0; i<attributes.getLength(); i++ ) {
 			Node attribute = attributes.item(i);
 			if(attribute.getNodeType() == Node.TEXT_NODE) continue;
 			String name = attribute.getNodeName();
 			String value;
 
 			if( attribute.getChildNodes().getLength() > 0 ) {
 				value = attribute.getFirstChild().getNodeValue();                
 			}
 			else {
 				continue;
 			}
 			/** Specific server node information */
         
         
             String nameSpaceString = "";
             if(!nameSpace.equals("")) nameSpaceString = nameSpace + ":";
 			if( name.equals(nameSpaceString + "id") || name.equals("id") ) {
 				server.setProviderVirtualMachineId(value);                
 			}
 			else if( name.equalsIgnoreCase(nameSpaceString + "name") ) {
 				server.setName(value);
 			}
 			else if( name.equalsIgnoreCase(nameSpaceString + "description") ) {
 				server.setDescription(value);
 			}
 			else if( name.equalsIgnoreCase(nameSpaceString + "vlanResourcePath") ) {                
 				String vlanId = provider.getVlanIdFromVlanResourcePath(value);
 				if(!provider.isVlanInRegion(vlanId)){
 					return null;
 				}
 				server.setProviderVlanId(vlanId); 
 			}
             else if( name.equalsIgnoreCase(nameSpaceString + "operatingSystem") ) {
                 NodeList osAttributes  = attribute.getChildNodes();
                 for(int j=0;j<osAttributes.getLength();j++ ){
                     Node os = osAttributes.item(j);
                     String osName = os.getNodeName();
                     String osValue ;
                     if( osName.equals(nameSpaceString + "displayName") && os.getChildNodes().getLength() > 0 ) {
                         osValue = os.getFirstChild().getNodeValue();
                     }else{
                         osValue = null ;
                     }
 
                     if( osValue != null && osValue.contains("64") ) {
                         bestArchitectureGuess = Architecture.I64;
                     }
                     else if( osValue != null && osValue.contains("32") ) {
                         bestArchitectureGuess = Architecture.I32;
                     }
                     if( osValue != null ) {
                         server.setPlatform(Platform.guess(osValue));
                         break;
                     }
                 }
             }
 			else if( name.equalsIgnoreCase(nameSpaceString + "cpuCount") ) {
 				server.getTags().put("cpuCount", value);
 			}
 			else if( name.equalsIgnoreCase(nameSpaceString + "memory") ) { 
 				server.getTags().put("memory", value);
 			}
 			else if( name.equalsIgnoreCase(nameSpaceString + "osStorage") ) { 
 				server.getTags().put("osStorage", value);
 			}
 			else if( name.equalsIgnoreCase(nameSpaceString + "additionalLocalStorage") ) { 
 				server.getTags().put("additionalLocalStorage", value);
 			}
 			else if(name.equals(nameSpaceString + "machineName") ) { 
 				//equal to private ip address
 			}
 			else if( name.equalsIgnoreCase(nameSpaceString + "privateIPAddress") ) { 
 				if( value != null ) {
 					server.setPrivateIpAddresses(new String[] { value });  
 					server.setProviderAssignedIpAddressId(value);
 				}          
 			}
 			//DeployedServer
 			else if( name.equalsIgnoreCase(nameSpaceString + "publicIpAddress") ) { 
 				server.setPublicIpAddresses(new String[] { value });               
 			}
 			else if( name.equalsIgnoreCase(nameSpaceString + "isDeployed") ) {
 				if(value.equalsIgnoreCase("false")){
 					server.setCurrentState(VmState.PENDING); 
 					isPending = true;
 				}else{
 					isPending = false;
 				}         
 			}
 			else if( name.equalsIgnoreCase(nameSpaceString + "isStarted") ) {
 				if( value.equalsIgnoreCase("false") ){
 					server.setCurrentState(VmState.STOPPED);
 				}           
 			}
 			else if( name.equalsIgnoreCase(nameSpaceString + "created") ) {
 				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); 
 				/** 2012-05-08T02:23:16.999Z */
 				try {
 					if(value.contains(".")){
 						String newvalue = value.substring(0,value.indexOf("."))+"Z";
 						server.setCreationTimestamp(df.parse(newvalue).getTime());                		
 					}else{
 						server.setCreationTimestamp(df.parse(value).getTime());                		
 					}                    
 				}
 				catch( ParseException e ) {
 					logger.warn("Invalid date: " + value);
 					server.setLastBootTimestamp(0L);
 				}
 			}
 			//From here is the deployed server, or pending server
 			else if(name.equalsIgnoreCase(nameSpaceString + "machineSpecification") ) {
 				NodeList machineAttributes  = attribute.getChildNodes();
 				for(int j=0;j<machineAttributes.getLength();j++ ){
 					Node machine = machineAttributes.item(j);
 					if(machine.getNodeType() == Node.TEXT_NODE) continue;
 
 					if(machine.getNodeName().equalsIgnoreCase(nameSpaceString + "operatingSystem") ){
 						NodeList osAttributes  = machine.getChildNodes();
 						for(int k=0;k<osAttributes.getLength();k++ ){
 							Node os = osAttributes.item(k);
 
 							if(os.getNodeType() == Node.TEXT_NODE) continue;
 							String osName = os.getNodeName();
 							String osValue = null ;
 
 							if(osName.equalsIgnoreCase(nameSpaceString + "displayName") && os.hasChildNodes()) {
 								osValue = os.getFirstChild().getNodeValue();
                                 Platform platform = Platform.guess(osValue);
                                 if(platform.equals(Platform.UNKNOWN)){
                                     platform = Platform.UNIX;
                                 }
                                 server.setPlatform(platform);
 
                                 if(osValue != null && osValue.contains("64") ) {
                                     bestArchitectureGuess = Architecture.I64;
                                 }
                                 else if(osValue != null && osValue.contains("32") ) {
                                     bestArchitectureGuess = Architecture.I32;
                                 }
 							}
 						}
 					}else if( machine.getNodeName().equalsIgnoreCase(nameSpaceString + "cpuCount") && machine.getFirstChild().getNodeValue() != null ) {
 						server.getTags().put("cpuCount", machine.getFirstChild().getNodeValue());
 					}
 					/** memoryMb pendingDeploy deployed */
 					else if( (machine.getNodeName().equalsIgnoreCase(nameSpaceString + "memory") || machine.getNodeName().equalsIgnoreCase(nameSpaceString + "memoryMb"))&& machine.getFirstChild().getNodeValue() != null ) {
 						server.getTags().put("memory", machine.getFirstChild().getNodeValue());
 					}
 					/** deployedserver osStorageGb */
 					else if( (machine.getNodeName().equalsIgnoreCase(nameSpaceString + "osStorage") ||machine.getNodeName().equalsIgnoreCase(nameSpaceString + "osStorageGb"))&& machine.getFirstChild().getNodeValue() != null) {
 						server.getTags().put("osStorage", machine.getFirstChild().getNodeValue());
 					}
 					/** additionalLocalStorageGb pendingDeploy */
 					else if((machine.getNodeName().equalsIgnoreCase(nameSpaceString + "additionalLocalStorage") || machine.getNodeName().equalsIgnoreCase(nameSpaceString + "additionalLocalStorageGb") ) && machine.getFirstChild().getNodeValue() != null ) {
 						server.getTags().put("additionalLocalStorage", machine.getFirstChild().getNodeValue());
 					}                     
 				}           
 			}
 			/** pendingDeploy or Deployed */
 			else if( name.equalsIgnoreCase(nameSpaceString + "sourceImageId") ) {
 				server.setProviderMachineImageId(value);
 			}
 			/** pendingDeploy or Deployed */
 			else if( name.equalsIgnoreCase(nameSpaceString + "networkId") ) {
 				server.setProviderVlanId(value);        	   
 				if(!provider.isVlanInRegion(value)){
 					return null;
 				}         
 			}
 			/** From here is the specification for pending deployed server */
 			else if( name.equalsIgnoreCase(nameSpaceString + "status") ) {
 				NodeList statusAttributes  = attribute.getChildNodes();
 				for(int j=0;j<statusAttributes.getLength();j++ ){
 					Node status = statusAttributes.item(j);
 					if(status.getNodeType() == Node.TEXT_NODE) continue;
 					if( status.getNodeName().equalsIgnoreCase(nameSpaceString + "step") ){
 						//TODO
 						/** If it is this status means it is pending */
 						server.setCurrentState(VmState.PENDING);
 					}
 					else if( status.getNodeName().equalsIgnoreCase(nameSpaceString + "requestTime") && status.getFirstChild().getNodeValue() != null ) {
 						DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"); //2009-02-03T05:26:32.612278
 
 						try {
 							if(value.contains(".")){
 								String newvalue = value.substring(0,status.getFirstChild().getNodeValue().indexOf("."))+"Z";
 								server.setCreationTimestamp(df.parse(newvalue).getTime());                		
 							}else{
 								server.setCreationTimestamp(df.parse(status.getFirstChild().getNodeValue()).getTime());                		
 							}                    
 						}
 						catch( ParseException e ) {
 							logger.warn("Invalid date: " + value);
 							server.setLastBootTimestamp(0L);
 						}
 					}  
 					else if( status.getNodeName().equalsIgnoreCase(nameSpaceString + "userName") && status.getFirstChild().getNodeValue() != null ) {
 						//This seems to break the cloud syncing operation - removed for now.
 						//server.setProviderOwnerId(status.getFirstChild().getNodeValue());
 					}
 					else if( status.getNodeName().equalsIgnoreCase(nameSpaceString + "numberOfSteps") ) {
 
 					}
 					else if( status.getNodeName().equalsIgnoreCase(nameSpaceString + "action") ) {
 						String action = status.getFirstChild().getNodeValue();
 						if(action.equalsIgnoreCase("CLEAN_SERVER")){
 							/** Means failed deployed */
 							server.setCurrentState(VmState.PENDING);	   
 						}
 					}
 				}
 			}
         }
         if( isPending ) {
             server.setCurrentState(VmState.PENDING);
         }
 		if( server.getName() == null ) {
 			server.setName(server.getProviderVirtualMachineId());
 		}
 		if( server.getDescription() == null ) {
 			server.setDescription(server.getName());
 		}
 	
 		if( server.getProviderDataCenterId() == null ) {        	
 			server.setProviderDataCenterId(provider.getDataCenterId(server.getProviderRegionId()));
 		}
 
 		if( server.getArchitecture() == null ) {
 			server.setArchitecture(bestArchitectureGuess);
 		}
 
 		VirtualMachineProduct product = null;
 
 		if(server.getTag("cpuCount") != null && server.getTag("memory") != null ){
 			int cpuCout = Integer.valueOf((String) server.getTag("cpuCount"));
 			int memoryInMb = Integer.valueOf((String) server.getTag("memory"));
             int diskInGb = 1;
 
 			if(server.getTag("additionalLocalStorage") == null){
 				product = getProduct(bestArchitectureGuess, cpuCout, (memoryInMb/1024), 0);
 			}
             else{
 				diskInGb = Integer.valueOf((String) server.getTag("additionalLocalStorage"));
 				product = getProduct(bestArchitectureGuess, cpuCout, (memoryInMb/1024), diskInGb);
 			}
             if( product == null ) {
                 product = new VirtualMachineProduct();
                 product.setName(cpuCout + " CPU/" + memoryInMb + "MB RAM/" + diskInGb + "GB HD");
                 product.setProviderProductId(cpuCout + ":" + memoryInMb + ":" + diskInGb);
                 product.setRamSize(new Storage<Megabyte>((memoryInMb), Storage.MEGABYTE));
                 product.setRootVolumeSize(new Storage<Gigabyte>(diskInGb, Storage.GIGABYTE));
                 product.setCpuCount(cpuCout);
                 product.setDescription(cpuCout + " CPU/" + memoryInMb + "MB RAM/" + diskInGb + "GB HD");
             }
 		}
         if( product == null ) {
             product = new VirtualMachineProduct();
             product.setName("Unknown");
             product.setProviderProductId("unknown");
             product.setRamSize(new Storage<Megabyte>(1024, Storage.MEGABYTE));
             product.setRootVolumeSize(new Storage<Gigabyte>(1, Storage.GIGABYTE));
             product.setCpuCount(1);
             product.setDescription("Unknown product");
         }
 		/**  Set public address */
 		/**        String[] privateIps = server.getPrivateIpAddresses();
 
         if(privateIps != null){
             IpAddressImplement ipAddressSupport = new IpAddressImplement(provider);
             String[] publicIps = new String[privateIps.length];
             for(int i= 0; i< privateIps.length; i++){
             	NatRule rule = ipAddressSupport.getNatRule(privateIps[i], server.getProviderVlanId());
             	if(rule != null){
             		publicIps[i] = rule.getNatIp();
             	}               
             }
             server.setPublicIpAddresses(publicIps);
         }*/
 
 		server.setProductId(product.getProviderProductId());
 		return server;
 	}
 
 	private RegionComputingPower toRegionComputingPower(Node node, String nameSpace){
 
 		if(node == null){
 			return null;
 		}
 
 		NodeList data;
 
 		data = node.getChildNodes();
 
 		RegionComputingPower r = new RegionComputingPower();
 		for( int i=0; i<data.getLength(); i++ ) {
 			Node item = data.item(i);
 
             
 			if(item.getNodeType() == Node.TEXT_NODE) continue;
 
 			if( item.getNodeName().equals(nameSpace + "location") ) {
 				r.setProviderRegionId(item.getFirstChild().getNodeValue());
 			}
 			else if( item.getNodeName().equals(nameSpace + "displayName") ) {
 				r.setName(item.getFirstChild().getNodeValue());
 			}
 			else if( item.getNodeName().equals(nameSpace + "maxCpu") ) {
 				r.setMaxCPUNum(Integer.valueOf(item.getFirstChild().getNodeValue()));
 			}
 			else if( item.getNodeName().equals(nameSpace + "maxRamMb") ) {
 				r.setMaxMemInMB(Integer.valueOf(item.getFirstChild().getNodeValue()));
 			}
 		}
 		return r;
 	}
 
     static private final Random random = new Random();
     static public String alphabet = "ABCEFGHJKMNPRSUVWXYZabcdefghjkmnpqrstuvwxyz0123456789#@()=+/{}[],.?;':|-_!$%^&*~`";
     public String getRandomPassword() {
         StringBuilder password = new StringBuilder();
         int rnd = random.nextInt();
         int length = 17;
 
         if( rnd < 0 ) {
             rnd = -rnd;
         }
         length = length + (rnd%8);
         while( password.length() < length ) {
             char c;
 
             rnd = random.nextInt();
             if( rnd < 0 ) {
                 rnd = -rnd;
             }
             c = (char)(rnd%255);
             if( alphabet.contains(String.valueOf(c)) ) {
                 password.append(c);
             }
         }
         return password.toString();
     }
 
 
 	@SuppressWarnings("serial")
 	public class RegionComputingPower extends Region{
 
 		public int maxCPUNum;
 		public int maxMemInMB;
 
 		public int getMaxMemInMB(){
 			return maxMemInMB;
 		}
 
 		public int getMaxCPUNum(){
 			return maxCPUNum;
 		}
 
 		public void setMaxMemInMB(int maxMemInMB){
 			this.maxMemInMB = maxMemInMB;
 		}
 
 		public void setMaxCPUNum(int maxCPUNum){
 			this.maxCPUNum = maxCPUNum;
 		}
 	}
 }
