 package velo.reconciliation.utils;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.zip.GZIPInputStream;
 
 import javax.xml.namespace.QName;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.ws.WebServiceException;
 import javax.xml.ws.soap.SOAPFaultException;
 
 import org.apache.commons.digester.Digester;
 import org.apache.commons.digester.ExtendedBaseRules;
 import org.apache.commons.lang.time.StopWatch;
 import org.apache.log4j.Logger;
 import org.milyn.Smooks;
 import org.milyn.container.ExecutionContext;
 import org.milyn.payload.JavaResult;
 import org.openspml.v2.msg.spml.Request;
 import org.tempuri.IWindowsGatewayApi;
 import org.tempuri.WindowsGatewayApi;
 import org.xml.sax.SAXException;
 
 import sun.misc.BASE64Decoder;
 import velo.action.ResourceOperation;
 import velo.actions.readyActions.ReadyActionAPI;
 import velo.collections.Accounts;
 import velo.collections.ResourceGroups;
 import velo.contexts.OperationContext;
 import velo.entity.Account;
 import velo.entity.Attribute;
 import velo.entity.ReconcileProcessSummary;
 import velo.entity.Resource;
 import velo.entity.ResourceAttribute;
 import velo.entity.ResourceGroup;
 import velo.entity.ResourceReconcileTask;
 import velo.entity.ResourceTypeOperation;
 import velo.entity.ReconcileProcessSummary.ReconcileProcesses;
 import velo.exceptions.DataTransformException;
 import velo.exceptions.FactoryException;
 import velo.exceptions.ObjectsConstructionException;
 import velo.exceptions.OperationException;
 import velo.exceptions.ResourceDescriptorException;
 import velo.query.Query;
 import velo.resource.operationControllers.GroupMembershipSpmlResourceOpreationController;
 import velo.resource.operationControllers.SpmlResourceOperationController;
 import velo.windowsGateway.VeloDataContainerProxy;
 
 /**
  * Import data required for the reconciliation process
  * This importer supports different type of formats including: direct import from controller, import from Windows gateway, XLS, etc...
  * 
  * @author Asaf Shakarchi
  */
 public class ReconcileDataImportManager {
 	private static Logger log = Logger.getLogger(ReconcileDataImportManager.class.getName());
 	StopWatch stopWatch = new StopWatch();
 
 
 
 
 	public Accounts importIdentitiesFromResource(ResourceReconcileTask reconcileTask) throws DataTransformException {
 		Resource resource = reconcileTask.getResource();
 
 		log.debug("Importing all identities from resource '" + resource.getDisplayName() + "'");
 		stopWatch.start();
 		//Determine the data fetch type
 		//if automatic, then fetch content from SPML controller
 		if (resource.isAutoFetch()) {
 			log.debug("Resource 'Auto fetch' was set to -true-, fetching data directly from resource...");
 
 			if (resource.getResourceType().isGatewayRequired()) {
 				return importAccountsFromWindowsGateway(resource);
 			}else {
 				SpmlResourceOperationController roc = (SpmlResourceOperationController)resource.getResourceType().factoryResourceOperationsController();
 				if (roc == null) {
 					throw new DataTransformException("Could not factor resource operation controller for class '" + resource.getResourceType().getResourceControllerClassName() + "'");
 				}
 				roc.setResource(resource);
 
 
 				//Prepare operation context
 				OperationContext context = new OperationContext();
 				context.addVar("queryManager", new Query());
 
 				log.trace("Factoring ResourceOperation object...");
 				ResourceOperation ro = resource.factoryResourceOperation(context, reconcileTask.getResourceTypeOperation());
 
 				try {
 					if (reconcileTask.getResourceTypeOperation().getResourceGlobalOperation().getUniqueName().equals("RESOURCE_IDENTITIES_RECONCILIATION_FULL")) {
 						executePre(ro);
 
 						stopWatch.stop();
 						log.debug("Finished importing accounts for FULL RECONCILIATION PROCESS in '" + stopWatch.getTime()/1000 + "' seconds.");
 						return roc.listIdentitiesFull(ro, reconcileTask);
 					}else if (reconcileTask.getResourceTypeOperation().getResourceGlobalOperation().getUniqueName().equals("RESOURCE_IDENTITIES_RECONCILIATION_INCREMENTAL")) {
 						ReconcileProcessSummary rps = ReadyActionAPI.getInstance().getResourceManager().findLatestSuccessfullReconcileProcessSummary(ReconcileProcesses.RECONCILE_IDENTITIES_INCREMENTAL);
 
 						//might be null first time...
 						if (rps != null) {
 							context.addVar("lastReconcileTime",rps.getStartDate());
 						} else {
 							rps = ReadyActionAPI.getInstance().getResourceManager().findLatestSuccessfullReconcileProcessSummary(ReconcileProcesses.RECONCILE_IDENTITIES_FULL);
 							if (rps != null) {
 								context.addVar("lastReconcileTime",rps.getStartDate());
 							} else {
 								throw new DataTransformException("Accounts reconciliation was never executed for this resource, please execute full reconciliation first.");
 							}
 						}
 
 						//pre must be executed after initializing the lastReconcileTime in context.
 						executePre(ro);
 
 						stopWatch.stop();
 						log.debug("Finished importing accounts for INCREMENTAL RECONCILIATION PROCESS in '" + stopWatch.getTime()/1000 + "' seconds.");
 						return roc.listIdentitiesIncrementally(ro, reconcileTask);
 					} else {
 						throw new DataTransformException("Could not fetch identities for resource operation type '" + reconcileTask.getResourceTypeOperation().getResourceGlobalOperation().getUniqueName() + "'");
 					}
 				}
 				catch (OperationException e) {
 					throw new DataTransformException(e.getMessage());
 				}
 			}
 			//TODO: Implement auto_fetch=false
 		} else {
 			log.debug("Resource 'Auto fetch' was set to -false-, fetching data via documents imports...");
 			//determine fetch type
 			//use smooks to transform the content to the standard objects
 			throw new DataTransformException("AutoFech=false is not supported yet!");
 			//return accounts;
 		}
 	}
 
 
 
 	private Accounts importAccountsFromWindowsGateway(Resource resource) throws DataTransformException {
 		VeloDataContainerProxy vdcp = new VeloDataContainerProxy();
 
 		//add attrs to sync to the vdc
 		for (ResourceAttribute currRA : resource.getAttributesToSync()) {
 			vdcp.addAttribute(currRA.getUniqueName(), "");
 		}
 
 
 		try {
 			IWindowsGatewayApi iface = factoryWindowsGateway(resource, null, null, null, vdcp);
 			String gzippedBase64Data = iface.performOperation("LIST_IDENTITIES_ALL", resource.getResourceType().getResourceControllerClassName(), vdcp.factoryVeloDataContainer());
 			log.trace("Size of gzipped base64 data: " + gzippedBase64Data.length());
 
 			byte[] gzippedBase64Bytes = new byte[gzippedBase64Data.length()];
 			gzippedBase64Bytes = new BASE64Decoder().decodeBuffer(gzippedBase64Data);
 
 			ByteArrayInputStream bais = new ByteArrayInputStream(gzippedBase64Bytes);
 			GZIPInputStream gis = new GZIPInputStream(bais);
 			//OutputStream out = new FileOutputStream(activeDataFileName);
 
 
 			ByteArrayOutputStream out = new ByteArrayOutputStream();
 			//byte[] buf = new byte[2051648];  //size can be changed according to programmer's need.
 			byte[] buf = new byte[gzippedBase64Data.length()*1000];
 			int len;
 			while ((len = gis.read(buf)) > 0) {
 				out.write(buf, 0, len);
 			}
 			gis.close();
 			out.close();
 
 
 			ByteArrayInputStream baisClear = new ByteArrayInputStream(out.toByteArray());
 
 
 			//log.debug("---START of accounts recived from Gateway!");
 			//log.debug(out);
 			//log.debug("---END of accounts recived from Gateway!");
 
 			Smooks smooks = null;
 			try {
 				//smooks = new Smooks("velo/reconciliation/utils/identities_smooks-config.xml");
				smooks = new Smooks("META-INF/smooks/reconcile/identities_smooks-config.xml");
 			} catch (SAXException e) {
 				throw new DataTransformException(e.getMessage());
 			}
 			ExecutionContext executionContext = smooks.createExecutionContext();
 			executionContext.setAttribute("resource", resource);
 			JavaResult result = new JavaResult();
 
 			//byte[] messageIn = StreamUtils.readStream(new FileInputStream("velo/reconciliation/utils/testapp1_identities.xml"));
 			//smooks.filter(new StreamSource(new ByteArrayInputStream(messageIn)), result, executionContext);
 			smooks.filter(new StreamSource(baisClear), result, executionContext);
 
 			Accounts accs =  (Accounts) result.getBean("reconIdentities");
 
 
 			//HACK FOR NOW AS SMOOKS DOESNT TAKE CARE OF SETTING THE RESOURCE
 			for (Account currAcc : accs) {
 				currAcc.setResource(resource);
 				//currAcc.initNameByAttribute();
 				try {
 					//we trust that smooks loaded active attributes...
 					currAcc.setActiveAttributesLoaded(true);
 					
 					currAcc.initFullAccountByLoadedActiveAttributes();
 				} catch (ObjectsConstructionException e) {
 					throw new DataTransformException(e.getMessage());
 				}
 
 				for (Attribute currAttr : currAcc.getActiveAttributes().values()) {
 					System.out.println("YEYYYYY: " + currAttr.getDisplayable());
 
 				}
 			}
 
 			return accs;
 		} catch (FactoryException e) {
 			throw new DataTransformException(e.getMessage());
 		} catch (IOException e) {
 			throw new DataTransformException(e.getMessage());
 		}
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	public ResourceGroups importGroupsFromResource(ResourceReconcileTask reconcileTask) throws DataTransformException {
 		Resource resource = reconcileTask.getResource();
 
 		log.debug("Importing all identities from resource '" + resource.getDisplayName() + "'");
 		stopWatch.start();
 
 		if (resource.getResourceType().isGatewayRequired()) {
 			return importGroupsFromWindowsGateway(resource);
 		}
 
 		
 		
 		if (resource.isAutoFetch()) {
 			log.debug("Resource 'Auto fetch' was set to -true-, fetching data directly from resource...");
 
 			SpmlResourceOperationController roc = (SpmlResourceOperationController)resource.getResourceType().factoryResourceOperationsController();
 			if (roc == null) {
 				throw new DataTransformException("Could not factor resource operation controller for class '" + resource.getResourceType().getResourceControllerClassName() + "'");
 			}
 			roc.setResource(resource);
 
 			//Prepare operation context
 			OperationContext context = new OperationContext();
 			context.addVar("queryManager", new Query());
 
 			log.trace("Factoring ResourceOperation object...");
 			ResourceOperation ro = resource.factoryResourceOperation(context, reconcileTask.getResourceTypeOperation());
 
 			try {
 				if (reconcileTask.getResourceTypeOperation().getResourceGlobalOperation().getUniqueName().equals("RESOURCE_GROUPS_RECONCILIATION_FULL")) {
 					executePre(ro);
 
 					stopWatch.stop();
 					log.debug("Finished importing groups for FULL groups reconciliation PROCESS in '" + stopWatch.getTime()/1000 + "' seconds.");
 					return roc.listGroupsFull(ro, reconcileTask);
 				} else {
 					throw new DataTransformException("Operation '" + reconcileTask.getResourceTypeOperation().getResourceGlobalOperation().getUniqueName() + "' is not supported by group imports!");
 				}
 			} catch (OperationException e) {
 				throw new DataTransformException(e.getMessage());
 			}
 		} else {
 			throw new DataTransformException("AutoFech=false is not supported yet!");
 		}
 	}
 
 
 
 	private ResourceGroups importGroupsFromWindowsGateway(Resource resource) throws DataTransformException {
 		VeloDataContainerProxy vdcp = new VeloDataContainerProxy();
 
 
 		try {
 			IWindowsGatewayApi iface = factoryWindowsGateway(resource, null, null, null, vdcp);
 			String gzippedBase64Data = iface.performOperation("LIST_GROUPS_ALL", resource.getResourceType().getResourceControllerClassName(), vdcp.factoryVeloDataContainer());
 			log.trace("Size of gzipped base64 data: " + gzippedBase64Data.length());
 
 			byte[] gzippedBase64Bytes = new byte[gzippedBase64Data.length()];
 			gzippedBase64Bytes = new BASE64Decoder().decodeBuffer(gzippedBase64Data);
 
 			ByteArrayInputStream bais = new ByteArrayInputStream(gzippedBase64Bytes);
 			GZIPInputStream gis = new GZIPInputStream(bais);
 			//OutputStream out = new FileOutputStream(activeDataFileName);
 
 
 			String fileName = resource.factorySyncFileName();
 			ByteArrayOutputStream out = new ByteArrayOutputStream();
 			//OutputStream out = new FileOutputStream(fileName);
 			//byte[] buf = new byte[2051648];  //size can be changed according to programmer's need.
 			byte[] buf = new byte[gzippedBase64Data.length()*1000];
 			int len;
 			while ((len = gis.read(buf)) > 0) {
 				out.write(buf, 0, len);
 			}
 			gis.close();
 			out.close();
 
 
 
 			ByteArrayInputStream baisClear = new ByteArrayInputStream(out.toByteArray());
 
 			log.debug("---START of accounts recived from Gateway!");
 			//log.debug(out);
 			log.debug("---END of accounts recived from Gateway!");
 
 
 			Smooks smooks = null;
 			try {
				smooks = new Smooks("META-INF/smooks/reconcile/groups_smooks-config.xml");
 			} catch (SAXException e) {
 				throw new DataTransformException(e.getMessage());
 			}
 			ExecutionContext executionContext = smooks.createExecutionContext();
 			executionContext.setAttribute("resource", resource);
 			JavaResult result = new JavaResult();
 
 			//byte[] messageIn = StreamUtils.readStream(new FileInputStream("velo/reconciliation/utils/testapp1_identities.xml"));
 			//smooks.filter(new StreamSource(new ByteArrayInputStream(messageIn)), result, executionContext);
 			smooks.filter(new StreamSource(baisClear), result, executionContext);
 
 			ResourceGroups groups =  (ResourceGroups) result.getBean("reconGroups");
 
 
 			//Hack, as smooks's conf does not support setting resource on the fly 
 			for (ResourceGroup currGRP : groups) {
 				currGRP.setResource(resource);
 			}
 
 
 			//ResourceGroups groups = importGroupsByDigester(new File(fileName), resource);
 
 
 			return groups;
 		} catch (FactoryException e) {
 			throw new DataTransformException(e.getMessage());
 		} catch (IOException e) {
 			throw new DataTransformException(e.getMessage());
 		}
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	public ResourceGroups importGroupMembership(ResourceReconcileTask reconcileTask) throws DataTransformException {
 		Resource resource = reconcileTask.getResource();
 
 		log.debug("Importing all group membership from resource '" + resource.getDisplayName() + "'");
 		stopWatch.start();
 
 		if (resource.isAutoFetch()) {
 			log.debug("Resource 'Auto fetch' was set to -true-, fetching data directly from resource...");
 			if (resource.getResourceType().isGatewayRequired()) {
 				return importGroupMembersFromWindowsGateway(resource);
 			} else {
 				GroupMembershipSpmlResourceOpreationController roc = (GroupMembershipSpmlResourceOpreationController)resource.getResourceType().factoryResourceOperationsController();
 				if (roc == null) {
 					throw new DataTransformException("Could not factor resource operation controller for class '" + resource.getResourceType().getResourceControllerClassName() + "'");
 				}
 				roc.setResource(resource);
 
 				//Prepare operation context
 				OperationContext context = new OperationContext();
 				context.addVar("queryManager", new Query());
 
 				log.trace("Factoring ResourceOperation object...");
 				ResourceOperation ro = resource.factoryResourceOperation(context, reconcileTask.getResourceTypeOperation());
 
 				try {
 					if (reconcileTask.getResourceTypeOperation().getResourceGlobalOperation().getUniqueName().equals("RESOURCE_GROUP_MEMBERSHIP_RECONCILIATION_FULL")) {
 						executePre(ro);
 
 						stopWatch.stop();
 						log.debug("Finished importing groups for FULL groups reconciliation PROCESS in '" + stopWatch.getTime()/1000 + "' seconds.");
 						return roc.listGroupMembershipFull(ro, reconcileTask);
 					} else {
 						throw new DataTransformException("Operation '" + reconcileTask.getResourceTypeOperation().getResourceGlobalOperation().getUniqueName() + "' is not supported by group membership imports!");
 					}
 				} catch (OperationException e) {
 					throw new DataTransformException(e.getMessage());
 				}
 			}
 		} else {
 			throw new DataTransformException("AutoFech=false is not supported yet!");
 		}
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	private ResourceGroups importGroupMembersFromWindowsGateway(Resource resource) throws DataTransformException {
 		VeloDataContainerProxy vdcp = new VeloDataContainerProxy();
 
 
 		try {
 			IWindowsGatewayApi iface = factoryWindowsGateway(resource, null, null, null, vdcp);
 			String gzippedBase64Data = iface.performOperation("LIST_GROUPMEMBERSHIP_ALL", resource.getResourceType().getResourceControllerClassName(), vdcp.factoryVeloDataContainer());
 			log.trace("Size of gzipped base64 data: " + gzippedBase64Data.length());
 
 			byte[] gzippedBase64Bytes = new byte[gzippedBase64Data.length()];
 			gzippedBase64Bytes = new BASE64Decoder().decodeBuffer(gzippedBase64Data);
 
 			ByteArrayInputStream bais = new ByteArrayInputStream(gzippedBase64Bytes);
 			GZIPInputStream gis = new GZIPInputStream(bais);
 			//OutputStream out = new FileOutputStream(activeDataFileName);
 
 
 			String fileName = resource.factorySyncFileName();
 			ByteArrayOutputStream out = new ByteArrayOutputStream();
 			//OutputStream out = new FileOutputStream(fileName);
 			//byte[] buf = new byte[2051648];  //size can be changed according to programmer's need.
 			byte[] buf = new byte[gzippedBase64Data.length()*1000];
 			int len;
 			while ((len = gis.read(buf)) > 0) {
 				out.write(buf, 0, len);
 			}
 			gis.close();
 			out.close();
 
 
 
 			ByteArrayInputStream baisClear = new ByteArrayInputStream(out.toByteArray());
 
 			log.debug("---START of accounts recived from Gateway!");
 			//log.debug(out);
 			log.debug("---END of accounts recived from Gateway!");
 
 
 			Smooks smooks = null;
 			try {
 				smooks = new Smooks("velo/reconciliation/utils/group-membership_smooks-config.xml");
 			} catch (SAXException e) {
 				throw new DataTransformException(e.getMessage());
 			}
 			ExecutionContext executionContext = smooks.createExecutionContext();
 			executionContext.setAttribute("resource", resource);
 			JavaResult result = new JavaResult();
 
 			//byte[] messageIn = StreamUtils.readStream(new FileInputStream("velo/reconciliation/utils/testapp1_identities.xml"));
 			//smooks.filter(new StreamSource(new ByteArrayInputStream(messageIn)), result, executionContext);
 			smooks.filter(new StreamSource(baisClear), result, executionContext);
 
 			ResourceGroups groups =  (ResourceGroups) result.getBean("reconGroups");
 
 
 			//Hack, as smooks's conf does not support setting resource on the fly 
 			for (ResourceGroup currGRP : groups) {
 				currGRP.setResource(resource);
 			}
 
 
 			//ResourceGroups groups = importGroupsByDigester(new File(fileName), resource);
 
 
 			return groups;
 		} catch (FactoryException e) {
 			throw new DataTransformException(e.getMessage());
 		} catch (IOException e) {
 			throw new DataTransformException(e.getMessage());
 		}
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 
 	public void executePre(ResourceOperation ro) throws DataTransformException {
 		boolean invocationStatus;
 		//perform execution
 		log.trace("Started invocation of ResourceOperation...");
 
 		invocationStatus = ro.preActionOperation();
 		if (!invocationStatus) {
 			log.error("PreExecution invocation failed, indication the whole process as a failure...");
 			throw new DataTransformException("PreAction invocation has failed: " + ro.getErrorMessage());
 		}
 	}
 
 
 
 
 
 
 
 
 
 	private IWindowsGatewayApi factoryWindowsGateway(Resource resource, ResourceTypeOperation rto, Request request, ResourceOperation ro, VeloDataContainerProxy vdcp) throws FactoryException {
 		if (resource.getGateway() == null) {
 			throw new FactoryException("Gateway must be specified for resource '" + resource.getDisplayName() + "'");
 		}
 
 
 		String wsdlUrl = "http://" + resource.getGateway().getHostName() + ":" + resource.getGateway().getPort() + "/ServiceModelSamples/service?wsdl";
 		String serviceName = "WindowsGatewayApi";
 		String namespace = "http://tempuri.org/";
 
 		if (log.isTraceEnabled()) {
 			log.trace("Gateway display name: " + resource.getGateway().getDisplayName());
 			log.trace("Gateway WSDL Url: " + wsdlUrl);
 			log.trace("Gateway serviceName: " + serviceName);
 			log.trace("Gateway namespace: " + namespace);
 
 			if (rto != null) {
 				log.trace("Operation Unique Name:" + rto.getResourceGlobalOperation().getUniqueName());
 			}
 			log.trace("Resource Type Controller Class Name: " + resource.getResourceType().getResourceControllerClassName());
 		}
 
 		QName serviceQN = new QName(namespace,serviceName);
 
 		//throws a WebServiceException if URL does not exist
 		WindowsGatewayApi wgi = null;
 		IWindowsGatewayApi iface = null;
 
 		try {
 			log.trace("Factoring a windows gateway service manager...");
 			wgi = new WindowsGatewayApi(new URL(wsdlUrl), serviceQN);
 			//TODO: Replace basic http binding to a more secured one!
 			log.trace("Binding Windows Gateway via Basic Http Binding...");
 			iface = wgi.getBasicHttpBindingIWindowsGatewayApi();
 		} catch (WebServiceException e) {
 			log.trace("Performing execution via Windows Gateway, please wait...");
 			throw new FactoryException(e.getMessage());
 		} catch (MalformedURLException e) {
 			throw new FactoryException(e.getMessage());
 			//Hopefully encapsulate all exceptions
 			/*Already catched by WebServiceException
 		} catch (SOAPFaultException e) {
 			throw new OperationException(e.getMessage());*/
 		}
 
 
 
 		//Prepare the DateContainer for the windows gateway
 		//pass by reference from outside: vdcp = new VeloDataContainerProxy();
 
 		try {
 			vdcp.importResourceConfParams(resource.factoryResourceDescriptor());
 			if (request != null) {
 				vdcp.importDataFromRequest(resource, request);
 			}
 			if (ro != null) {
 				vdcp.importDataFromResourceOperation(ro);
 			}
 		}catch (ResourceDescriptorException e) {
 			throw new FactoryException(e.toString());
 		}catch (ObjectsConstructionException e) {
 			throw new FactoryException(e.toString());
 		}
 
 		try {
 			iface = wgi.getBasicHttpBindingIWindowsGatewayApi();
 			return iface;
 		} catch (SOAPFaultException e) {
 			log.error("Could not perform operation via windows gateway: " + e.getMessage());
 			throw new FactoryException(e.getMessage());
 		}
 	}
 
 
 
 
 
 
 
 
 
 
 	//private ResourceGroups importGroupsByDigester(InputStream is, Resource resource) throws DataTransformException {
 	private ResourceGroups importGroupsByDigester(File f, Resource resource) throws DataTransformException {
 		Digester d = new Digester();
 		try {
 			ResourceGroups groups = new ResourceGroups();
 			d.push(groups);
 			d.setValidating(false);
 			d.setRules(new ExtendedBaseRules());
 			d.addObjectCreate("groups/group", ResourceGroup.class);
 			d.addSetProperties("groups/group", "display_name", "displayName");
 			d.addSetProperties("groups/group", "description", "description");
 			d.addSetProperties("groups/group", "unique_id", "uniqueId");
 			d.addSetNext("groups/group", "add");
 			//d.parse(is);
 			d.parse(f);
 
 
 			System.out.println("PRASED GROUPS!!!!!!!!!!: '" + groups.size() + "'");
 			for (ResourceGroup currRG : groups) {
 				System.out.println("CURR GRP: '" + currRG.getUniqueId() + "'");
 				currRG.setResource(resource);
 			}
 			return groups;
 		} catch (SAXException e) {
 			//throw new SyncListImporterException("There was a SAX exception while trying to import the sync XML file, failed with message: " + saxe.getMessage());
 			throw new DataTransformException(e.getMessage());
 		} catch (IOException e) {
 			//throw new SyncListImporterException("Cannot import sync list file, an IO exception has occured, failed with message: " + ioe.getMessage());
 			throw new DataTransformException(e.getMessage());
 		}
 	}
 }
