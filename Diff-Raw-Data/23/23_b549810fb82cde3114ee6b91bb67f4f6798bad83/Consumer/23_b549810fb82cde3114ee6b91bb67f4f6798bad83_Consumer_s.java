 /*******************************************************************************
  * Abiquo community edition
  * cloud management application for hybrid clouds
  *  Copyright (C) 2008-2010 - Abiquo Holdings S.L.
  * 
  *  This application is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU LESSER GENERAL PUBLIC
  *  LICENSE as published by the Free Software Foundation under
  *  version 3 of the License
  * 
  *  This software is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  LESSER GENERAL PUBLIC LICENSE v.3 for more details.
  * 
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the
  *  Free Software Foundation, Inc., 59 Temple Place - Suite 330,
  *  Boston, MA 02111-1307, USA.
  ******************************************************************************/
 package controllers;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import models.Deploy_Bundle;
 import models.Deploy_Bundle_Nodes;
 import models.Deploy_Nodes_Resources;
 import models.Nodes;
 import models.Nodes_Resources;
 import models.Offer;
 import models.OfferPurchased;
 import models.UserPortal;
 import monitor.VmEventHandler;
 
 import org.jclouds.abiquo.AbiquoApi;
 import org.jclouds.abiquo.AbiquoAsyncApi;
 import org.jclouds.abiquo.AbiquoContext;
 import org.jclouds.abiquo.domain.cloud.HardDisk;
 import org.jclouds.abiquo.domain.cloud.VirtualAppliance;
 import org.jclouds.abiquo.domain.cloud.VirtualDatacenter;
 import org.jclouds.abiquo.domain.cloud.VirtualMachine;
 import org.jclouds.abiquo.domain.cloud.VirtualMachineTemplate;
 import org.jclouds.abiquo.domain.enterprise.Enterprise;
 import org.jclouds.abiquo.domain.enterprise.User;
 import org.jclouds.abiquo.domain.infrastructure.Datacenter;
 import org.jclouds.abiquo.domain.network.PrivateNetwork;
 import org.jclouds.abiquo.domain.task.AsyncTask;
 import org.jclouds.abiquo.features.services.CloudService;
 import org.jclouds.abiquo.monitor.VirtualApplianceMonitor;
 import org.jclouds.abiquo.monitor.VirtualMachineMonitor;
 import org.jclouds.rest.AuthorizationException;
 import org.jclouds.rest.RestContext;
 
 import play.Logger;
 import play.mvc.Controller;
 import portal.util.AbiquoUtils;
 import portal.util.Context;
 
 import com.abiquo.model.enumerator.HypervisorType;
 import com.abiquo.server.core.cloud.VirtualApplianceState;
 import com.abiquo.server.core.cloud.VirtualMachineState;
 
 /**
  * @author Harpreet Kaur This class is invoked when a user with role USER logs
  *         in. User is served with pre-defined service catalog defined for his
  *         enterprise. He can browse through various service catalog offers and
  *         can buy them . If user selects to buy the offer, the selected offer
  *         gets deployed and an email is sent specifiying ip, port to access the
  *         deployed virtualmachine.
  */
 public class Consumer extends Controller {
 	/**
 	 * Displays service level ( i.e VDC) for current user's enterprise.
 	 * 
 	 * @param enterpriseID
 	 */
 	public static void ServiceCatalog(final Integer enterpriseID) {
 
 		Logger.info("---------INSIDE CONSUMER SERVICECATALOG()------------");
 		Logger.info("Enterprie ID for current User " + enterpriseID);
 		String user = session.get("username");
 	    String password = session.get("password");
 
 		if (user != null) {
 			List<Offer> result1 = ProducerDAO.groupByVDC_EnterpriseView(enterpriseID);
 			/*
 			 * List<sc_offer> result2 = ProducerDAO
 			 * .getVappListForVDC_EnterpriseView(enterpriseID, vdc_name_param);
 			 */
 			Logger.info("------------EXITING CONSUMER SERVICECATALOG()--------------");
 			
 			AbiquoContext contextt = Context.getApiClient(user, password);
             if (contextt != null) {
                 AbiquoUtils.setAbiquoUtilsContext(contextt);
                 final User userAbiquo = contextt.getAdministrationService().getCurrentUser();
             	final Integer numOffers = ProducerDAO.getOffersPurchasedFromEnterpriseId(userAbiquo.getEnterprise().getId()).size();
             	render(result1, user, enterpriseID, numOffers);            
 			} else {
 
 				flash.error("You are not connected.Please Login");
 				Login.login_page();
 			}
 		}
 	}
 
 	/**
 	 * Displays service catalog offers for selected service level
 	 * 
 	 * @param vdc_name_param
 	 * @param enterpriseID
 	 */
 	public static void availableOffers(final String vdc_name_param,
 			final Integer enterpriseID) {
 
 		Logger.info("---------INSIDE CONSUMER AVAILABLEOFFERS()---------------");
 		Logger.info("Enterprie ID for current User " + enterpriseID);
 		String user = session.get("username");
 		if (user != null) {
 			Logger.info("CURRENT USER EMAIL ID: " + user);
 			/*
 			 * List<sc_offer> result1 = ProducerDAO
 			 * .groupByVDC_EnterpriseView(enterpriseID);
 			 */
 			List<Offer> result2 = ProducerDAO
 					.getVappListForVDC_EnterpriseView(enterpriseID,
 							vdc_name_param);
 
 			Logger.info("------------EXITING CONSUMER AVAILABLEOFFERS()--------------");
 			render("/Consumer/ListServiceCatalog.html", result2, user,
 					enterpriseID);
 		} else {
 
 			flash.error("You are not connected.Please Login");
 			Login.login_page();
 		}
 	}
 	
 	public static void availableOffersAll(final Integer enterpriseID) {
 
 		Logger.info("---------INSIDE CONSUMER AVAILABLEOFFERS()---------------");
 		Logger.info("Enterprie ID for current User " + enterpriseID);
 		String user = session.get("username");
 		if (user != null) {
 			Logger.info("CURRENT USER EMAIL ID: " + user);
 			/*
 			 * List<sc_offer> result1 = ProducerDAO
 			 * .groupByVDC_EnterpriseView(enterpriseID);
 			 */
 			List<Offer> result2 = ConsumerDAO.getPublishedOffers();
 			//List<OfferPurchased> result = ProducerDAO.getSubscribedOffersGroupByServiceLevels();
 
 			Logger.info("------------EXITING CONSUMER AVAILABLEOFFERS()--------------");
 			render("/Consumer/ListServiceCatalog.html", result2, user,
 					enterpriseID);
 		} else {
 
 			flash.error("You are not connected.Please Login");
 			Login.login_page();
 		}
 	}
 
 	public static void purchasedOffers(final Integer enterpriseID) {
 
     Logger.info("---------INSIDE CONSUMER PURCHASEDOFFERS()---------------");
     Logger.info("Enterprie ID for current User " + enterpriseID);
     String user = session.get("username");
     String password = session.get("password");
 
     if (user != null) {
             Logger.info("CURRENT USER EMAIL ID: " + user);
             AbiquoContext contextt = Context.getApiClient(user, password);
             if (contextt != null) {
                     AbiquoUtils.setAbiquoUtilsContext(contextt);
                     final User userAbiquo = contextt.getAdministrationService().getCurrentUser();
                     final CloudService cloudService = contextt.getCloudService();
                     final Integer idEnterprise = userAbiquo.getEnterprise().getId();
 
                     /* ---------------------------- */
                     /*
                      * Retrieve the deploy username and password for current user's
                      * Enterprise.
                      */
                     
                     //List<OfferPurchased> offersPurchased = ProducerDAO.getOffersPurchasedFromUserId(userAbiquo.getId());
                     List<OfferPurchased> offersPurchased = ProducerDAO.getOffersPurchasedFromEnterpriseId(idEnterprise);
                     for (OfferPurchased offerPurchased : offersPurchased) {                    	
                     	VirtualDatacenter vdc = cloudService.getVirtualDatacenter(offerPurchased.getIdVirtualDatacenterUser());
                     	VirtualAppliance vapp = vdc.getVirtualAppliance(offerPurchased.getIdVirtualApplianceUser());
 						offerPurchased.setVirtualApplianceState(vapp != null ? vapp.getState() : VirtualApplianceState.UNKNOWN );
 						offerPurchased.save();
 					}
                      
 
                     /*
                      * List<sc_offer> result1 = ProducerDAO
                      * .groupByVDC_EnterpriseView(enterpriseID);
                      */
                     //List<Offer> result2 = ProducerDAO.groupByVDC_EnterpriseView(enterpriseID);
                                    
                     //List<OfferPurchased> offersPurchased = ProducerDAO.getOffersPurchasedFromUserId(user);
 
                     /*
                      * for (VirtualAppliance virtualAppliance : listvApp) {
                      * result2.fvirtualAppliance.getId(); }
                      */
 
                     Logger.info("------------EXITING CONSUMER PURCHASEDOFFERS()--------------");
                     render(offersPurchased, user, enterpriseID);
             }
 
     } else {
 
             flash.error("You are not connected.Please Login");
             Login.login_page();
     }
 
 }
 
 	/**
 	 * Displays selected Service catalog offer details
 	 *  
 	 * @param offer_id
 	 */
 	public static void purchaseConfirmation(final Integer offer_id) {
 		Logger.info("---------INSIDE CONSUMER PURCHASECONFIMATION()---------------");
 		String user = session.get("username");
 		Logger.info("CURRENT USER EMAIL ID: " + user);
 		if (user != null) {			
 			Offer offer = Offer.findById(offer_id);
 			Logger.info("------------EXITING CONSUMER PURCHASECONFIRMATION()--------------");
 			render(offer, user);
 		} else {
 
 			flash.error("You are not connected.Please Login");
 			Login.login_page();
 		}
 	}
 
 	/**
 	 * 1. Customer buy offer as a User. Deployment needs CLOUD_ADMIN privilege.
 	 * Hence,require deploy user setup for the enterprise that consumer belongs
 	 * to.2 users - session user and deploy user . 2. Save the deployment
 	 * details such as user, vdc created, SC offer id , lease etc into database.
 	 * 3. Destroy date needs to be updated with the date when offer is
 	 * undeployed after lease has expired (in future releases). For now, its
 	 * null. 4. Refer portal-schema if needed.
 	 * 
 	 * @param id_datacenter
 	 *            The datacenter id to be used for deployment.
 	 * @param vdc_id_param
 	 *            The id of virtual datacenter to be created.
 	 * @param sc_offer_id
 	 *            The id of virtual appliance to be deployed.
 	 * @param va_param
 	 *            The virtual appliance name.
 	 * @param lease_period
 	 */
 	@SuppressWarnings("null")
 	public static void Deploy(final Integer id_datacenter,
 			final Integer vdc_id_param, final Integer sc_offer_id,
 			final String va_param, final String lease_period) {
 		Logger.info("---------INSIDE CONSUMER DEPLOY()---------------");
 		Logger.info(" DEPLOY( INTEGER ID_DATACENTER:: " + id_datacenter
 				+ ", INTEGER VDC_ID_PARAM :: " + vdc_id_param
 				+ ", INTEGER SC_OFFER_ID :: " + sc_offer_id
 				+ " , String va_param:: " + va_param + ")");
 
 		String deploy_username = null;
 		String deploy_password = null;
 		Integer deploy_enterprise_id = null;
 
 		String user = session.get("username");
 		String password = session.get("password");
 
 		AbiquoContext contextt = Context.getApiClient(user, password);
 		if (contextt != null) {
 			AbiquoUtils.setAbiquoUtilsContext(contextt);
 
 			/* ---------------------------- */
 			/*
 			 * Retrieve the deploy username and password for current user's
 			 * Enterprise.
 			 */
 			Enterprise current_enterprise = AbiquoUtils
 					.getCurrentUserEnterprise();
 			Integer enterprise_id = current_enterprise.getId();
 
 			/*for (MKT_Configuration mkt : mkt_conf) {
 				deploy_username = mkt.getMkt_deploy_user();
 				deploy_password = mkt.getMkt_deploy_pw();
 				deploy_enterprise_id = mkt.getDeploy_enterprise_id();
 			}*/
 			
 			deploy_username = user;
 			deploy_password = password;
 			deploy_enterprise_id = current_enterprise.getId();
 			
 			Logger.info(" DEPLOY ENTERPRISE ID  + USERNAME + PASSWORD :"
 					+ deploy_enterprise_id + "  " + deploy_username + "  "
 					+ deploy_password);
 			/* ---------------------------- */
 			
 			
 			/* Create context with deploy username and password for deployments */
 			AbiquoContext context = Context.getApiClient(deploy_username,
 					deploy_password);
 
 			VirtualDatacenter vdc_toDeploy = null;
 			VirtualAppliance virtualapp_todeploy = null;
 			VirtualMachine vm_todeploy = null;
 			VirtualDatacenter virtualDC = null;
 			String vdc_name = null;
 			try {
 				//AbiquoUtils.setAbiquoUtilsContext(context);
 				Enterprise enterprise = AbiquoUtils
 						.getEnterprise(deploy_enterprise_id);
 				String useremail = session.get("email");
 				String vdc_user = session.get("username");
 				String vdcname = Helper.vdcNameGen(vdc_user);
 				Logger.info("CURRENT USER EMAIL ID: " + useremail);
 				Logger.info(" vdcname : " + vdcname);
 
 			
 				virtualDC = AbiquoUtils.getMarketplaceDetails(vdc_id_param);
 				Logger.info(" VDC to deploy: ", virtualDC);
 				vdc_name = virtualDC.getName();
 				HypervisorType hypervisor = virtualDC.getHypervisorType();
 				Logger.info(" Hypervisor to deploy: ", hypervisor);
 
 				// get first datacenter allowed. For developement only will be one.
 				// get first datacenter allowed. For developement only will be one.
 				Datacenter datacenter = virtualDC.getDatacenter(); 
 						//enterprise.listAllowedDatacenters().get(0);
 				Logger.info(" Datacenter to deploy: ", datacenter);
 
 				PrivateNetwork network = PrivateNetwork.builder(context.getApiContext())
 						.name("10.80.0.0").gateway("10.80.0.1")
 						.address("10.80.0.0").mask(22).build();
 				Logger.info(" Network Built");
 
 				vdc_toDeploy = VirtualDatacenter
 						.builder(context.getApiContext(), datacenter, enterprise).name(vdcname)
 						.cpuCountLimits(0, 0).hdLimitsInMb(0, 0)
 						.publicIpsLimits(0, 0).ramLimits(0, 0)
 						.storageLimits(0, 0).vlansLimits(0, 0)
 						.hypervisorType(hypervisor).network(network).build();
 
 				Logger.info("VDC built  ");
 				vdc_toDeploy.save();
 				Logger.info(" 1. VDC CREATED ");
 				virtualapp_todeploy = VirtualAppliance
 						.builder(context.getApiContext(), vdc_toDeploy).name(va_param).build();
 				virtualapp_todeploy.save();
 
 				Logger.info(" 2. VAPP CREATED ");
 
 				/* Save the deploy info to the portal database : user, vdc etc */
 				final User userAbiquo = contextt.getAdministrationService().getCurrentUser();
 				final Integer idUser = userAbiquo.getId();
 				final OfferPurchased offerPurchased = new OfferPurchased();
 				UserPortal userToSave = UserPortal.findById(idUser);
 				
 				if (userToSave == null) {
 					// Try to recover from jClouds					
 					final String nickUser = userAbiquo.getNick();
 					final String emailUser = userAbiquo.getEmail();
 					final Integer idEnterprise = userAbiquo.getEnterprise().getId();
 					userToSave = new UserPortal(idUser,nickUser, emailUser, idEnterprise);				
 					userToSave.save();
 				}
 				
 				offerPurchased.setUser(userToSave);
 				
 				Date current = new Date();
 				Calendar cal = Calendar.getInstance();
 				if (lease_period.contentEquals("30 days")) {
 					Logger.info("case1 : 30 days lease ");
 					cal.add(Calendar.DATE, 30);
 				} else if (lease_period.contentEquals("60 days")) {
 					Logger.info("case2 : 60 days lease");
 					cal.add(Calendar.DATE, 60);
 				} else if (lease_period.contentEquals("90 days")) {
 					Logger.info("case3 : 90 days lease ");
 					cal.add(Calendar.DATE, 90);
 
 				}
 				Logger.info("--------------------");
 				offerPurchased.setStart(current);
 				offerPurchased.setExpiration(cal.getTime());
 				// user_consumption.setVdc_name(vdc_toDeploy.getName());
 				offerPurchased.setLeasePeriod(lease_period);
 				offerPurchased.setIdVirtualDatacenterUser(vdc_toDeploy.getId());
 				offerPurchased.setIdVirtualApplianceUser(virtualapp_todeploy.getId());
 				
 				final Offer offer = Offer.findById(sc_offer_id);
 				//offer.setVirtualDatacenter(vdc_toDeploy.getId());
 				offerPurchased.setOffer(offer);				
 
 				Set<Deploy_Bundle> deploy_bundle_set = new HashSet<Deploy_Bundle>();
 				Deploy_Bundle deploy_Bundle = new Deploy_Bundle();
 				deploy_Bundle.setDeploy_datacenter(datacenter.getId());
 				deploy_Bundle.setDeploy_hypervisorType(hypervisor.toString());
 				deploy_Bundle.setDeploy_network("");
 				deploy_Bundle.setVapp_name(virtualapp_todeploy.getName());
 				deploy_Bundle.setVdc_name(vdc_toDeploy.getId());
 				deploy_Bundle.setOfferPurchased(offerPurchased);
 				deploy_Bundle.setVapp_id(virtualapp_todeploy.getId());
 				deploy_bundle_set.add(deploy_Bundle);
 				/*
 				 * String query =
 				 * "select p from sc_offer as p where p.sc_offer_id = ?1";
 				 * JPAQuery result = sc_offer.find(query, sc_offer_id);
 				 */List<Offer> nodes = ProducerDAO
 						.getOfferDetails(sc_offer_id);
 				for (Offer node : nodes) {
 					/////Set<Deploy_Bundle_Nodes> deploy_Bundle_Nodes_list = new HashSet<Deploy_Bundle_Nodes>();
 					
 					/// Retrieve nodes from jClouds
 					Set<Nodes> vmlist_todeploy = node.getNodes();			
 					
 					Set<Deploy_Bundle_Nodes> deploy_Bundle_Nodes_list = new HashSet<Deploy_Bundle_Nodes>();
 					for (Nodes aVM : vmlist_todeploy) {
 						String vmName = aVM.getNode_name();
						VirtualMachineTemplate vm_template_todeploy = enterprise.getTemplateInRepository(datacenter, aVM.getIdImage());
 						int cpu = aVM.getCpu();
 						int ram = aVM.getRam();
 						// String description = aVM.getDescription();
 
 						vm_todeploy = VirtualMachine
 								.builder(context.getApiContext(), virtualapp_todeploy,
 										vm_template_todeploy).nameLabel(vmName)
 								.cpu(cpu).ram(ram).password("vmpassword")
 								.build();
 						vm_todeploy.save();
 						Logger.info(" 3. VM CREATED");
 						Deploy_Bundle_Nodes deploy_Bundle_Nodes = new Deploy_Bundle_Nodes();
 						deploy_Bundle_Nodes.setCpu(cpu);
 						deploy_Bundle_Nodes.setNode_name(vmName);
 						deploy_Bundle_Nodes.setNode_name(vm_todeploy.getNameLabel());
 						deploy_Bundle_Nodes.setNode_id(vm_todeploy.getId());
 						deploy_Bundle_Nodes.setRam(ram);
 						deploy_Bundle_Nodes.setVdrp_password("");
 						deploy_Bundle_Nodes.setVdrpIP("");
 						deploy_Bundle_Nodes.setVdrpPort(0);
 						deploy_Bundle_Nodes_list.add(deploy_Bundle_Nodes);
 						// deploy_Bundle_Nodes.setResources(resources);
 
 						List<HardDisk> hardDisk_toattach = new ArrayList<HardDisk>();
 						Set<Deploy_Nodes_Resources> deploy_Nodes_Resources_list = new HashSet<Deploy_Nodes_Resources>();
 						Set<Nodes_Resources> resources = aVM.getResources();
 						for (Nodes_Resources resource : resources) {
 							Long size = resource.getValue();
 							HardDisk disk = HardDisk
 									.builder(context.getApiContext(), vdc_toDeploy)
 									.sizeInMb(size).build();
 							disk.save();
 							hardDisk_toattach.add(disk);
 							Deploy_Nodes_Resources deploy_Nodes_Resources = new Deploy_Nodes_Resources();
 							deploy_Nodes_Resources.setResourceType(resource
 									.getResourceType());
 							deploy_Nodes_Resources.setResourceType(resource
 									.getSequence());
 							deploy_Nodes_Resources
 									.setValue(resource.getValue());
 							deploy_Nodes_Resources_list
 									.add(deploy_Nodes_Resources);
 						}
 						deploy_Bundle_Nodes
 								.setResources(deploy_Nodes_Resources_list);
 						HardDisk[] disks = new HardDisk[hardDisk_toattach
 								.size()];
 						for (int i = 0; i < hardDisk_toattach.size(); i++) {
 							disks[i] = hardDisk_toattach.get(i);
 						}
 						vm_todeploy.attachHardDisks(disks);
 						Logger.info(" 4. HARDDISKS ATTACHED ");
 						VmEventHandler handler = new VmEventHandler(context,
 								vm_todeploy);
 						Logger.info(" Handler created :");
 						VirtualMachineMonitor monitor = context
 								.getMonitoringService()
 								.getVirtualMachineMonitor();
 						monitor.register(handler);
 						vm_todeploy.deploy();
 						Logger.info("STARTING MONITORING ......");
 						monitor.monitorDeploy(vm_todeploy);
 
 					}
 					Logger.info("SAVING DEPLOY INFORMATION ......");
 					deploy_Bundle.setNodes(deploy_Bundle_Nodes_list);
 
 					offerPurchased.setNodes(deploy_bundle_set);
 					offerPurchased.setServiceLevel(offer.getDefaultServiceLevel());
 					offerPurchased.save();
 					Logger.info("DEPLOY INFO SAVED ......");
 					Logger.info("------------EXITING CONSUMER DEPLOY()--------------");
 					render(vdc_name, enterprise_id);
 				}
 
 			} catch (AuthorizationException ae) {
 
 				Logger.warn(ae, "EXCEPTION OCCURED IN deploy()");
 				String message = "Deployment cannot proceed further. Please Check deploy user and password for your enterprise.";
 				render("/errors/error.html", message);
 			} catch (Exception ae) {
 
 				Logger.warn(ae, "EXCEPTION OCCURED  IN deploy()");
 				String message = "Deployment cannot proceed further. Please contact your System Administrator.";
 				render("/errors/error.html", message);
 				if (context != null) {
 					context.close();
 				}
 			}
 
 		} else {
 
 			flash.error("You are not connected.Please Login");
 			Login.login_page();
 		}
 	}
 
 	public static void offerDetails(final Integer offer_id) {
 		Logger.info("---------INSIDE CONSUMER OFFERDETAILS()---------------");
 
 		String user = session.get("username");
 		if (user != null) {
 			Set<Nodes> nodes_list = null;
 			Set<Nodes_Resources> nodes_resources = null;			
 			
 			Offer offer = Offer.findById(offer_id);	
 			nodes_list = offer.getNodes();
 
 			for (Nodes node : nodes_list) {
 				nodes_resources = node.getResources();
 			}
 			
 			Logger.info("------------EXITING CONSUMER OFFERDETAILS()--------------");
 			render(offer, nodes_list, nodes_resources, user);
 
 		} else {
 
 			flash.error("You are not connected.Please Login");
 			Login.login_page();
 		}
 	}
 	
 	public static void offerDetailsPurchased(final Integer offer_id) {
 		Logger.info("---------INSIDE CONSUMER OFFERDETAILS()---------------");
 
 		String user = session.get("username");
 		if (user != null) {
 			Set<Nodes> nodes_list = null;
 			Set<Nodes_Resources> nodes_resources = null;			
 			
 			Offer offer = Offer.findById(offer_id);	
 			nodes_list = offer.getNodes();
 
 			for (Nodes node : nodes_list) {
 				nodes_resources = node.getResources();
 			}
 			
 			Logger.info("------------EXITING CONSUMER OFFERDETAILS()--------------");
 			render(offer, nodes_list, nodes_resources, user);
 
 		} else {
 
 			flash.error("You are not connected.Please Login");
 			Login.login_page();
 		}
 	}
 	
 	/**
 	 * 1. Customer buy offer as a User. Deployment needs CLOUD_ADMIN privilege.
 	 * Hence,require deploy user setup for the enterprise that consumer belongs
 	 * to.2 users - session user and deploy user . 2. Save the deployment
 	 * details such as user, vdc created, SC offer id , lease etc into database.
 	 * 3. Destroy date needs to be updated with the date when offer is
 	 * undeployed after lease has expired (in future releases). For now, its
 	 * null. 4. Refer portal-schema if needed.
 	 * 
 	 * @param id_datacenter
 	 *            The datacenter id to be used for deployment.
 	 * @param vdc_id_param
 	 *            The id of virtual datacenter to be created.
 	 * @param sc_offer_id
 	 *            The id of virtual appliance to be deployed.
 	 * @param va_param
 	 *            The virtual appliance name.
 	 * @param lease_period
 	 */
 	public static void Undeploy(final Integer purchasedOfferId) {
 		/*Logger.info("---------INSIDE CONSUMER DEPLOY()---------------");
 		Logger.info(" DEPLOY( INTEGER ID_DATACENTER:: " 
 				+ ", INTEGER SC_OFFER_ID :: " + sc_offer_id
 				+ " , String va_param:: " + vappId + ")");*/
 
 		String deploy_username = null;
 		String deploy_password = null;
 		Integer deploy_enterprise_id = null;
 
 		String user = session.get("username");
 		String password = session.get("password");
 
 		AbiquoContext contextt = Context.getApiClient(user, password);
 		if (contextt != null) {
 			AbiquoUtils.setAbiquoUtilsContext(contextt);
 
 			/* ---------------------------- */
 			/*
 			 * Retrieve the deploy username and password for current user's
 			 * Enterprise.
 			 */
 			Enterprise current_enterprise = AbiquoUtils
 					.getCurrentUserEnterprise();
 			Integer enterprise_id = current_enterprise.getId();
 			/*List<MKT_Configuration> mkt_conf = MarketDAO
 					.getMKTConfiguration(enterprise_id);
 
 			for (MKT_Configuration mkt : mkt_conf) {
 				deploy_username = mkt.getMkt_deploy_user();
 				deploy_password = mkt.getMkt_deploy_pw();
 				deploy_enterprise_id = mkt.getDeploy_enterprise_id();
 			}*/
 			
 			deploy_username = user;
 			deploy_password = password;
 			deploy_enterprise_id = current_enterprise.getId();
 			
 			Logger.info(" UNDEPLOY ENTERPRISE ID  + USERNAME + PASSWORD :"
 					+ deploy_enterprise_id + "  " + deploy_username + "  "
 					+ deploy_password);
 			/* ---------------------------- */
 
 			/* Create context with deploy username and password for deployments */
 			AbiquoContext context = Context.getApiClient(deploy_username,
 					deploy_password);
 
 			VirtualDatacenter vdc_toDeploy = null;
 			VirtualAppliance virtualapp_todeploy = null;
 			VirtualMachine vm_todeploy = null;
 			VirtualDatacenter virtualDC = null;
 			String vdc_name = null;
 			try {			
 				Enterprise enterprise = AbiquoUtils
 						.getEnterprise(deploy_enterprise_id);
 				String useremail = session.get("email");
 				String vdc_user = session.get("username");
 				String vdcname = Helper.vdcNameGen(vdc_user);
 				Logger.info("CURRENT USER EMAIL ID: " + useremail);
 				Logger.info(" vdcname : " + vdcname);
 				
 				final OfferPurchased offerPurchased = OfferPurchased.findById(purchasedOfferId);				
 				VirtualDatacenter vdc =  context.getCloudService().getVirtualDatacenter(offerPurchased.getIdVirtualDatacenterUser());
 				VirtualAppliance vapp = vdc.getVirtualAppliance(offerPurchased.getIdVirtualApplianceUser());
 				
 				VirtualApplianceMonitor monitorVapp = context.getMonitoringService().getVirtualApplianceMonitor();
 				AsyncTask[] undeployTasks = vapp.undeploy();			
 				monitorVapp.awaitCompletionUndeploy(vapp);
 				
 				if (vapp.getState() == VirtualApplianceState.NOT_DEPLOYED) {
 					Logger.info("OFFER UNDEPLOYED SUCCESSFULLY");
 				} else {
 					
 					AbiquoUtils.checkErrorsInTasks(undeployTasks);
 					Logger.info("Tasks Checked");
 					
 				}
 				Logger.info("DEPLOY INFO SAVED ......");
 				Logger.info("------------EXITING CONSUMER DEPLOY()--------------");
 				render(vdc_name, enterprise_id);				
 
 			} catch (AuthorizationException ae) {
 
 				Logger.warn(ae, "EXCEPTION OCCURED IN deploy()");
 				String message = "Deployment cannot proceed further. Please Check deploy user and password for your enterprise.";
 				render("/errors/error.html", message);
 			} catch (Exception ae) {
 
 				Logger.warn(ae, "EXCEPTION OCCURED  IN deploy()");
 				String message = "Deployment cannot proceed further. Please contact your System Administrator.";
 				render("/errors/error.html", message);
 				if (context != null) {
 					context.close();
 				}
 
 			}
 
 		} else {
 
 			flash.error("You are not connected.Please Login");
 			Login.login_page();
 		}
 	}
 	
 	public static void deleteOffer(final Integer purchasedOfferId) {
 		/*Logger.info("---------INSIDE CONSUMER DEPLOY()---------------");
 		Logger.info(" DEPLOY( INTEGER ID_DATACENTER:: " 
 				+ ", INTEGER SC_OFFER_ID :: " + sc_offer_id
 				+ " , String va_param:: " + vappId + ")");*/
 
 		String deploy_username = null;
 		String deploy_password = null;
 		Integer deploy_enterprise_id = null;
 
 		String user = session.get("username");
 		String password = session.get("password");
 
 		AbiquoContext contextt = Context.getApiClient(user, password);
 		if (contextt != null) {
 			AbiquoUtils.setAbiquoUtilsContext(contextt);
 
 			/* ---------------------------- */
 			/*
 			 * Retrieve the deploy username and password for current user's
 			 * Enterprise.
 			 */
 			Enterprise current_enterprise = AbiquoUtils
 					.getCurrentUserEnterprise();
 			Integer enterprise_id = current_enterprise.getId();
 			/*List<MKT_Configuration> mkt_conf = MarketDAO
 					.getMKTConfiguration(enterprise_id);
 
 			for (MKT_Configuration mkt : mkt_conf) {
 				deploy_username = mkt.getMkt_deploy_user();
 				deploy_password = mkt.getMkt_deploy_pw();
 				deploy_enterprise_id = mkt.getDeploy_enterprise_id();
 			}*/
 			
 			deploy_username = user;
 			deploy_password = password;
 			deploy_enterprise_id = current_enterprise.getId();
 			
 			Logger.info(" UNDEPLOY ENTERPRISE ID  + USERNAME + PASSWORD :"
 					+ deploy_enterprise_id + "  " + deploy_username + "  "
 					+ deploy_password);
 			/* ---------------------------- */
 
 			/* Create context with deploy username and password for deployments */
 			AbiquoContext context = Context.getApiClient(deploy_username,
 					deploy_password);
 
 			VirtualDatacenter vdc_toDeploy = null;
 			VirtualAppliance virtualapp_todeploy = null;
 			VirtualMachine vm_todeploy = null;
 			VirtualDatacenter virtualDC = null;
 			String vdc_name = null;
 			try {			
 				Enterprise enterprise = AbiquoUtils
 						.getEnterprise(deploy_enterprise_id);
 				String useremail = session.get("email");
 				String vdc_user = session.get("username");
 				String vdcname = Helper.vdcNameGen(vdc_user);
 				Logger.info("CURRENT USER EMAIL ID: " + useremail);
 				Logger.info(" vdcname : " + vdcname);
 
 				
 				final OfferPurchased offerPurchased = OfferPurchased.findById(purchasedOfferId);				
 				VirtualDatacenter vdc =  context.getCloudService().getVirtualDatacenter(offerPurchased.getIdVirtualDatacenterUser());
 				VirtualAppliance vapp = vdc.getVirtualAppliance(offerPurchased.getIdVirtualApplianceUser());
 //				List<VirtualMachine> lvm = vapp.listVirtualMachines();
 //				
 //				VirtualMachineMonitor monitor = context.getMonitoringService().getVirtualMachineMonitor();
 //				for (VirtualMachine virtualMachine : lvm) {
 //					virtualMachine.undeploy();					
 //				}
 //				
 //				VirtualMachine[] arr = new VirtualMachine[lvm.size()];
 //				monitor.awaitCompletionUndeploy(lvm.toArray(arr));
 //				
 //				for (VirtualMachine virtualMachine : lvm) {
 //					virtualMachine.delete();					
 //				
 //				}
 				
 				VirtualApplianceMonitor monitorVapp = context.getMonitoringService().getVirtualApplianceMonitor();
 				AsyncTask[] undeployTasks = vapp.undeploy();			
 				monitorVapp.awaitCompletionUndeploy(vapp);
 				
 				if (vapp.getState() == VirtualApplianceState.NOT_DEPLOYED) {
 					vapp.delete();
 					vdc.delete();
 					offerPurchased.delete();
 				} else {
 					
 					AbiquoUtils.checkErrorsInTasks(undeployTasks);
 					Logger.info("Tasks Checked");
 					
 				}
 				
 				
 				Logger.info("OFFER DELETED ......");
 				Logger.info("------------EXITING CONSUMER DEPLOY()--------------");
 				render(vdc_name, enterprise_id);				
 
 			} catch (AuthorizationException ae) {
 
 				Logger.warn(ae, "EXCEPTION OCCURED IN deploy()");
 				String message = "Deployment cannot proceed further. Please Check deploy user and password for your enterprise.";
 				render("/errors/error.html", message);
 			} catch (Exception ae) {
 
 				Logger.warn(ae, "EXCEPTION OCCURED  IN deploy()");
 				String message = "Deployment cannot proceed further. Please contact your System Administrator.";
 				render("/errors/error.html", message);
 				if (context != null) {
 					context.close();
 				}
 
 			}
 
 		} else {
 
 			flash.error("You are not connected.Please Login");
 			Login.login_page();
 		}
 	}
 	
 	public static void resumeOffer(final Integer purchasedOfferId) {
 		/*Logger.info("---------INSIDE CONSUMER DEPLOY()---------------");
 		Logger.info(" DEPLOY( INTEGER ID_DATACENTER:: " 
 				+ ", INTEGER SC_OFFER_ID :: " + sc_offer_id
 				+ " , String va_param:: " + vappId + ")");*/
 
 		String deploy_username = null;
 		String deploy_password = null;
 		Integer deploy_enterprise_id = null;
 
 		String user = session.get("username");
 		String password = session.get("password");
 
 		AbiquoContext contextt = Context.getApiClient(user, password);
 		if (contextt != null) {
 			AbiquoUtils.setAbiquoUtilsContext(contextt);
 
 			/* ---------------------------- */
 			/*
 			 * Retrieve the deploy username and password for current user's
 			 * Enterprise.
 			 */
 			Enterprise current_enterprise = AbiquoUtils
 					.getCurrentUserEnterprise();
 			Integer enterprise_id = current_enterprise.getId();
 			/*List<MKT_Configuration> mkt_conf = MarketDAO
 					.getMKTConfiguration(enterprise_id);
 
 			for (MKT_Configuration mkt : mkt_conf) {
 				deploy_username = mkt.getMkt_deploy_user();
 				deploy_password = mkt.getMkt_deploy_pw();
 				deploy_enterprise_id = mkt.getDeploy_enterprise_id();
 			}*/
 			
 			deploy_username = user;
 			deploy_password = password;
 			deploy_enterprise_id = current_enterprise.getId();
 			
 			Logger.info(" UNDEPLOY ENTERPRISE ID  + USERNAME + PASSWORD :"
 					+ deploy_enterprise_id + "  " + deploy_username + "  "
 					+ deploy_password);
 			/* ---------------------------- */
 
 			/* Create context with deploy username and password for deployments */
 			AbiquoContext context = Context.getApiClient(deploy_username,
 					deploy_password);
 
 			VirtualDatacenter vdc_toDeploy = null;
 			VirtualAppliance virtualapp_todeploy = null;
 			VirtualMachine vm_todeploy = null;
 			VirtualDatacenter virtualDC = null;
 			String vdc_name = null;
 			try {			
 				Enterprise enterprise = AbiquoUtils
 						.getEnterprise(deploy_enterprise_id);
 				String useremail = session.get("email");
 				String vdc_user = session.get("username");
 				String vdcname = Helper.vdcNameGen(vdc_user);
 				Logger.info("CURRENT USER EMAIL ID: " + useremail);
 				Logger.info(" vdcname : " + vdcname);
 
 				
 				final OfferPurchased offerPurchased = OfferPurchased.findById(purchasedOfferId);				
 				VirtualDatacenter vdc =  context.getCloudService().getVirtualDatacenter(offerPurchased.getIdVirtualDatacenterUser());
 				VirtualAppliance vapp = vdc.getVirtualAppliance(offerPurchased.getIdVirtualApplianceUser());
 
 				VirtualApplianceMonitor monitorVapp = context.getMonitoringService().getVirtualApplianceMonitor();
 				AsyncTask[] deployTasks = vapp.deploy();			
 				monitorVapp.awaitCompletionDeploy(vapp);
 				
 				if (vapp.getState() == VirtualApplianceState.DEPLOYED) {
 					Logger.info("OFFER DEPLOYED SUCCESSFULLY");
 				} else {					
 					AbiquoUtils.checkErrorsInTasks(deployTasks);
 					Logger.info("Tasks Checked");
 					
 				}				
 				
 				Logger.info("OFFER RESUMED ......");
 				Logger.info("------------EXITING CONSUMER DEPLOY()--------------");
 				render(vdc_name, enterprise_id);				
 
 			} catch (AuthorizationException ae) {
 
 				Logger.warn(ae, "EXCEPTION OCCURED IN deploy()");
 				String message = "Deployment cannot proceed further. Please Check deploy user and password for your enterprise.";
 				render("/errors/error.html", message);
 			} catch (Exception ae) {
 
 				Logger.warn(ae, "EXCEPTION OCCURED  IN deploy()");
 				String message = "Deployment cannot proceed further. Please contact your System Administrator.";
 				render("/errors/error.html", message);
 				if (context != null) {
 					context.close();
 				}
 
 			}
 
 		} else {
 
 			flash.error("You are not connected.Please Login");
 			Login.login_page();
 		}
 	}
 	
 	public static void resetOffer(final Integer purchasedOfferId) {
 		Logger.info("---------INSIDE CONSUMER DEPLOY()---------------");
 		/*Logger.info(" DEPLOY( INTEGER ID_DATACENTER:: " 
 				+ ", INTEGER SC_OFFER_ID :: " + sc_offer_id
 				+ " , String va_param:: " + vappId + ")");*/
 
 		String deploy_username = null;
 		String deploy_password = null;
 		Integer deploy_enterprise_id = null;
 
 		String user = session.get("username");
 		String password = session.get("password");
 
 		AbiquoContext contextt = Context.getApiClient(user, password);
 		if (contextt != null) {
 			AbiquoUtils.setAbiquoUtilsContext(contextt);
 
 			/* ---------------------------- */
 			/*
 			 * Retrieve the deploy username and password for current user's
 			 * Enterprise.
 			 */
 			Enterprise current_enterprise = AbiquoUtils
 					.getCurrentUserEnterprise();
 			Integer enterprise_id = current_enterprise.getId();
 			/*List<MKT_Configuration> mkt_conf = MarketDAO
 					.getMKTConfiguration(enterprise_id);
 
 			for (MKT_Configuration mkt : mkt_conf) {
 				deploy_username = mkt.getMkt_deploy_user();
 				deploy_password = mkt.getMkt_deploy_pw();
 				deploy_enterprise_id = mkt.getDeploy_enterprise_id();
 			}*/
 			
 			deploy_username = user;
 			deploy_password = password;
 			deploy_enterprise_id = current_enterprise.getId();
 			
 			Logger.info(" UNDEPLOY ENTERPRISE ID  + USERNAME + PASSWORD :"
 					+ deploy_enterprise_id + "  " + deploy_username + "  "
 					+ deploy_password);
 			/* ---------------------------- */
 
 			/* Create context with deploy username and password for deployments */
 			AbiquoContext context = Context.getApiClient(deploy_username,
 					deploy_password);
 
 			VirtualDatacenter vdc_toDeploy = null;
 			VirtualAppliance virtualapp_todeploy = null;
 			VirtualMachine vm_todeploy = null;
 			VirtualDatacenter virtualDC = null;
 			String vdc_name = null;
 			try {			
 				Enterprise enterprise = AbiquoUtils
 						.getEnterprise(deploy_enterprise_id);
 				String useremail = session.get("email");
 				String vdc_user = session.get("username");
 				String vdcname = Helper.vdcNameGen(vdc_user);
 				Logger.info("CURRENT USER EMAIL ID: " + useremail);
 				Logger.info(" vdcname : " + vdcname);
 
 				
 				
 				final OfferPurchased offerPurchased = OfferPurchased.findById(purchasedOfferId);				
 				VirtualDatacenter vdc =  context.getCloudService().getVirtualDatacenter(offerPurchased.getIdVirtualDatacenterUser());
 				VirtualAppliance vapp = vdc.getVirtualAppliance(offerPurchased.getIdVirtualApplianceUser());
 				
 				List<VirtualMachine> lvm = vapp.listVirtualMachines();
 				VirtualMachine[] arr = new VirtualMachine[lvm.size()];				
 				VirtualMachineMonitor monitor = context.getMonitoringService().getVirtualMachineMonitor();
 				
 				for (VirtualMachine virtualMachine : lvm) {
 					virtualMachine.changeState(VirtualMachineState.OFF);
 				}
 				monitor.awaitState(VirtualMachineState.OFF,lvm.toArray(arr));
 				
 				for (VirtualMachine virtualMachine : lvm) {
 					virtualMachine.changeState(VirtualMachineState.ON);
 				}
 				monitor.awaitState(VirtualMachineState.ON,lvm.toArray(arr));
 						
 				if (vapp.getState() == VirtualApplianceState.DEPLOYED) {
 					Logger.info("OFFER RESET SUCCESSFULLY");
 				} else {					
 					//AbiquoUtils.checkErrorsInTasks(deployTasks);
 					Logger.info("Tasks Checked");					
 				}				
 				
 				Logger.info("OFFER RESUMED ......");
 				Logger.info("------------EXITING CONSUMER DEPLOY()--------------");
 				render(vdc_name, enterprise_id);				
 
 			} catch (AuthorizationException ae) {
 
 				Logger.warn(ae, "EXCEPTION OCCURED IN deploy()");
 				String message = "Deployment cannot proceed further. Please Check deploy user and password for your enterprise.";
 				render("/errors/error.html", message);
 			} catch (Exception ae) {
 
 				Logger.warn(ae, "EXCEPTION OCCURED  IN deploy()");
 				String message = "Deployment cannot proceed further. Please contact your System Administrator.";
 				render("/errors/error.html", message);
 				if (context != null) {
 					context.close();
 				}
 
 			}
 
 		} else {
 
 			flash.error("You are not connected.Please Login");
 			Login.login_page();
 		}
 	}
 
 }
