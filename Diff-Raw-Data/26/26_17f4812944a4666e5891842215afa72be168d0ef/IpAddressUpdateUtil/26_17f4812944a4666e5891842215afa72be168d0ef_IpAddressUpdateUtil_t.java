 package de.mobile.olaf.server.esper.eventlistener.external;
 
 import com.espertech.esper.client.EPOnDemandQueryResult;
 import com.espertech.esper.client.EPServiceProvider;
 import com.espertech.esper.client.EventBean;
 
 import de.mobile.olaf.api.IpAddressStatus;
 import de.mobile.olaf.server.domain.RatedIpAddress;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class IpAddressUpdateUtil {
 	private final static Logger logger = LoggerFactory.getLogger(IpAddressUpdateUtil.class);
 	
 	/**
 	 * Updates the status respecting the relations between the status types.
 	 *  
 	 * @param ipAddress
 	 * @param status
 	 * @param epServiceProvider
 	 */
 	
 	public static void update(String ipAddress, IpAddressStatus status, EPServiceProvider epServiceProvider){
		String query = "select * from RatedIpAddressWindow where address='" + ipAddress + "'";
 		EPOnDemandQueryResult result = epServiceProvider.getEPRuntime().executeQuery(query);
 		EventBean[] ratedIpAddressEventBeans = result.getArray();
 		
 		if (ratedIpAddressEventBeans == null || ratedIpAddressEventBeans.length == 0){
 			RatedIpAddress ratedIpAddress = new RatedIpAddress(ipAddress, status);
 			// store new ip status
 			epServiceProvider.getEPRuntime().sendEvent(ratedIpAddress);
			logger.info("Set status of ip {} to {}.", ipAddress, status);
 		} else {
 			for (EventBean ratedIpAddressEventBean: ratedIpAddressEventBeans){
 				RatedIpAddress ratedIpAddress = (RatedIpAddress)ratedIpAddressEventBean.getUnderlying();
 				if (ratedIpAddress.getStatus() != status){
 					/*
 					 * either the status gets 'up' or the new status is FRAUD 
 					 */
 					if (ratedIpAddress.getStatus().compareTo(status)<0 || status == IpAddressStatus.USED_FOR_FRAUD) {
 						ratedIpAddress.setStatus(status);
 						// store new ip status
 						epServiceProvider.getEPRuntime().sendEvent(ratedIpAddress);
						logger.info("Set status of ip {} to {}.", ipAddress, status);
 						
 					}
 				}
 			}
 		}
 	}
 
 }
