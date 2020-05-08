 package controllers;
 
 import java.sql.Timestamp;
 import java.util.List;
 import java.util.Properties;
 
 
 import models.MKT_Configuration;
 import models.sc_offer;
 
 import org.apache.ivy.ant.IvyAntSettings.Credentials;
 import org.jclouds.abiquo.AbiquoContext;
 import org.jclouds.abiquo.AbiquoContextFactory;
 import org.jclouds.abiquo.domain.cloud.VirtualAppliance;
 import org.jclouds.abiquo.domain.cloud.VirtualDatacenter;
 import org.jclouds.abiquo.domain.cloud.VirtualMachine;
 import org.jclouds.abiquo.domain.enterprise.Enterprise;
 import org.jclouds.abiquo.domain.enterprise.User;
 import org.jclouds.abiquo.domain.task.AsyncTask;
 import org.jclouds.abiquo.features.services.AdministrationService;
 import org.jclouds.abiquo.features.services.CloudService;
 import org.jclouds.abiquo.predicates.cloud.VirtualMachinePredicates;
 
 import play.Logger;
 import play.cache.Cache;
 import play.mvc.Controller;
 import portal.util.AbiquoUtils;
 import portal.util.Context;
 
 public class Helper extends Controller{
 	
 	
 	static String vdcNameGen(String username ){
 		
 		//POR-%user-%userdefinedname-%UTC
 		java.util.Date date= new java.util.Date();
 		Timestamp tstamp = new Timestamp(date.getTime());
 		String prefix = "POR";
 		String vdc_generated_name = prefix +"-"+ username +"-"+ tstamp;
 		 		
 		return vdc_generated_name;
 	}
 	
 	/**
 	 * Displays Icon for Service catalog offer.
 	 * @param id The Service catlog offer id
 	 */
	public static void displayIcon(Integer id, String refresh)
 	{
 		
 			Logger.info("------------------------- INSIDE displayIcon()---------" );
 			Logger.info(" OfferID for icon " + id );
 			   final sc_offer offer = sc_offer.findById(id);
 			   notFoundIfNull(offer);
 			   response.setContentTypeIfNotSet(offer.getIcon().type());
			   response.setHeader("cache-control", "no-cache");
			   response.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
 			   Logger.info("------------------------- EXITING displayIcon()---------" );
 			   renderBinary(offer.getIcon().get());
 	}
 	/**
 	 * Displays Image for Service catalog offer.
 	 * @param id The Service catlog offer id
 	 */
 	public static void displayImage (Integer id)
 	{
 		
 			Logger.info("------------------------- INSIDE displayImage()-------"  );
 			Logger.info(" OfferID for image " + id );
 			   final sc_offer offer = sc_offer.findById(id);
 			   notFoundIfNull(offer);
 			   response.setContentTypeIfNotSet(offer.getImage().type());
 			   Logger.info("-------------------------  EXITING  displayIcon()---------" );
 			   renderBinary(offer.getImage().get());
 	}
 
 
 	/**
 	 * Returns Virtual machine details. After deployment portal database needs to be updated . 
 	 * But after deployment session user is lost. To make updation the virtual machine details are needed.
 	 * Retrieve the deployment user for the user's enterprise and get the virtual machine details.
 	 * @param vdc_id
 	 * @param vapp_id
 	 * @param vm_id
 	 * @param enterprise
 	 * @return
 	 */
 	static VirtualMachine getVMDetails(Integer vdc_id ,Integer vapp_id,  Integer vm_id, Enterprise enterprise )
 	{
 			Logger.info("------------------------- INSIDE getVMDetais()----------");
 			Logger.info(" getVMDetais() params : vdc_id " + vdc_id + " vapp_id : "+ vapp_id + " vm-id : " + vm_id);
 			Integer enterprise_id = null;
 		    if ( enterprise != null)
 		    {
 		    	enterprise_id = enterprise.getId();
 		    }
 		    List<MKT_Configuration> mkt_conf = MarketDAO.getDeployEnterprise(enterprise_id);
 			String username = null, password = null;
 			for ( MKT_Configuration mkt : mkt_conf )
 			{
 				username= mkt.getMkt_deploy_user();
 				password = mkt.getMkt_deploy_pw();
 				
 			}
 			Logger.info(" DEPLOY  USERNAME + PASSWORD :" + username +"  " + password );
 			AbiquoContext context = Context.getContext(username, password);
 			AbiquoUtils.setAbiquoUtilsContext(context);
 			VirtualMachine vm = AbiquoUtils.getVMDetails(vdc_id, vapp_id, vm_id);
 			return vm;
 	}
 
 		
 }
