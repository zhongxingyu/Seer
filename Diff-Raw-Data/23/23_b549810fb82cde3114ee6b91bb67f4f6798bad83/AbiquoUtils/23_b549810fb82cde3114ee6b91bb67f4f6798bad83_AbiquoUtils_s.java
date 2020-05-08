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
 package portal.util;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.jclouds.abiquo.AbiquoContext;
 import org.jclouds.abiquo.domain.cloud.VirtualAppliance;
 import org.jclouds.abiquo.domain.cloud.VirtualDatacenter;
 import org.jclouds.abiquo.domain.cloud.VirtualMachine;
 import org.jclouds.abiquo.domain.enterprise.Enterprise;
 import org.jclouds.abiquo.domain.enterprise.User;
 import org.jclouds.abiquo.domain.task.AsyncJob;
 import org.jclouds.abiquo.domain.task.AsyncTask;
 import org.jclouds.abiquo.features.services.AdministrationService;
 import org.jclouds.abiquo.features.services.CloudService;
 
 import play.Logger;
 import play.Play;
 
 import com.abiquo.server.core.task.enums.TaskState;
 
 /**
  * 
  * @author David López This class includes methods that return Abiquo cloud
  *         and admin service , resources like enterprise, vdc, vapp and virtual
  *         machine
  */
 public class AbiquoUtils {
 
 	private static AbiquoContext context;
 
 	public static void setAbiquoUtilsContext(final AbiquoContext contextt) {
 
 		context = contextt;
 		Logger.info(" AbiquoUtils context set to :" + context);
 	}
 
 	public static CloudService getCloud() {
 		System.out.println(" AbiquoUtils getCloud() context: " + context);
 		CloudService cloudService = null;
 		if (context != null) {
 			cloudService = context.getCloudService();
 			return cloudService;
 		} else {
 			Logger.warn(" Unable to create context in AbiquoUtils.");
 			return null;
 		}
 
 	}
 
 	public static AdministrationService getAdmin() {
 		System.out.println(" AbiquoUtils getAdmin() context: " + context);
 		AdministrationService adminService = null;
 		if (context != null) {
 			adminService = context.getAdministrationService();
 
 		} else {
 			Logger.warn(" Unable to create context in AbiquoUtils.");
 
 		}
 		return adminService;
 	}
 
 	public static Iterable<Enterprise> getAllEnterprises() {
 		System.out.println(" AbiquoUtils getAllEnterprises() context: "
 				+ context);
 		AdministrationService adminService = getAdmin();
 		Iterable<Enterprise> enterpriseList = null;
 		if (adminService != null) {
 			enterpriseList = adminService.listEnterprises();
 
 		} else {
 			Logger.warn(" Unable to get AdminService in AbiquoUtils.");
 
 		}
 		return enterpriseList;
 	}
 
 	public static Enterprise getEnterprise(final Integer id) {
 		System.out.println(" AbiquoUtils getAllEnterprises() context: "
 				+ context);
 		AdministrationService adminService = getAdmin();
 		Enterprise enterprise = null;
 		if (adminService != null) {
 			enterprise = adminService.getEnterprise(id);
 
 		} else {
 			Logger.warn(" Unable to get AdminService in AbiquoUtils.");
 
 		}
 		return enterprise;
 	}
 
 	public static VirtualDatacenter getVDCDetails(
 			final Integer virtualDatacenterId) {
 		VirtualDatacenter vdc = null;
 		if (virtualDatacenterId != null) {
 			System.out.println(" AbiquoUtils getVDCDetails() Context: "
 					+ context);
 
 			if (context != null) {
 				CloudService cloudService = getCloud();
 				if (cloudService != null) {
 					vdc = cloudService
 							.getVirtualDatacenter(virtualDatacenterId);
 				}
 			}
 		}
 		return vdc;
 	}
 	
 	public static VirtualDatacenter getMarketplaceDetails(
 			final Integer virtualDatacenterId) {
 		
 		VirtualDatacenter vdc = null;
 		if (virtualDatacenterId != null) {
 			System.out.println(" AbiquoUtils getVDCDetails() Context: "
 					+ context);
 			
 			Properties props = new Properties();
 			 //load a properties file
 			try {
 				props.load(new FileInputStream(Play.getFile("conf/config.properties")));
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}           
 			final String admin =  props.getProperty("admin");
 			final String password =  props.getProperty("password");
 		
 			AbiquoContext adminContext = Context.getApiClient(admin, password);
 			vdc = adminContext.getCloudService().getVirtualDatacenter(virtualDatacenterId);
 			
 		}
 		return vdc;
 	}
 
 	public static VirtualAppliance getVADetails(
 			final Integer virtualDatacenterId, final Integer va_id) {
 		VirtualDatacenter vdc = null;
 		VirtualAppliance va = null;
 		if (virtualDatacenterId != null && va_id != null) {
 			vdc = getVDCDetails(virtualDatacenterId);
 			if (vdc != null) {
 				va = vdc.getVirtualAppliance(va_id);
 			}
 		}
 		return va;
 	}
 
 	public static VirtualMachine getVMDetails(
 			final Integer virtualDatacenterId, final Integer va_id,
 			final Integer vm_id) {
 		VirtualAppliance va = null;
 		VirtualMachine vm = null;
 		if (virtualDatacenterId != null && va_id != null && vm_id != null) {
 			va = getVADetails(virtualDatacenterId, va_id);
 			if (va != null) {
 				vm = va.getVirtualMachine(vm_id);
 			}
 		}
 		return vm;
 	}
 
 	public static Iterable<VirtualDatacenter> getAllVDC() {
 		Iterable<VirtualDatacenter> vdc_list = null;
 		CloudService cloudService = getCloud();
 		if (cloudService != null) {
 			vdc_list = cloudService.listVirtualDatacenters();
 			System.out.println("vdc_list size: " + vdc_list);
 		}
 		return vdc_list;
 	}
 
 	public static Iterable<VirtualAppliance> getAllVA() {
 		Iterable<VirtualAppliance> va_list = null;
 		// System.out.println(" AbiquoUtils getAllVA() Context: " + context);
 		CloudService cloudService = getCloud();
 		if (cloudService != null) {
 			va_list = cloudService.listVirtualAppliances();
 		}
 		return va_list;
 	}
 
 	public static Enterprise getCurrentUserEnterprise() {
 		Enterprise enterprise = null;
 		AdministrationService adminService = getAdmin();
 		if (adminService != null) {
 			User user = adminService.getCurrentUser();
 			if (user != null) {
 				enterprise = user.getEnterprise();
 			}
 
 		}
 		return enterprise;
 	}
 	
 	public static void deleteVirtualDatacenter(final Integer vdcId) {			
 		try {
 			CloudService cloudService = getCloud();
 		} catch (Exception e) {
 			Logger.error("Unable to delete vdc: " + e.getCause() );
 		}
 	}
 	
 	public static void checkErrorsInTasks(final AsyncTask[] undeployTasks) {			
 		try {
 			for (AsyncTask task : undeployTasks) {
 				task.refresh();
 				if (task.getState() != TaskState.FINISHED_SUCCESSFULLY) {
 					for (AsyncJob job : task.getJobs()) {
 						Logger.info("JOB State: " + job.getState().name());
 					}
 				}
 			}
 		} catch (Exception e) {
 			Logger.error("Unable to check tasks state: " + e.getCause() );
 		}
 	}
 	
 	public static String getVAPrice(final Integer virtualDatacenterId, final Integer va_id) {
 		VirtualDatacenter vdc = null;
 		VirtualAppliance va = null;
		String Price = "0";
 		if (virtualDatacenterId != null && va_id != null) {			
 			vdc = getVDCDetails(virtualDatacenterId);
 			if (vdc != null) {
 				va = vdc.getVirtualAppliance(va_id);
 				//TODO Price from vApp
				//Price = va.getPrice();
 			}
 		}
 		return Price;
 	}
 }
