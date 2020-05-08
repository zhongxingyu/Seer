 package com.uwusoft.timesheet.util;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;
 
 import com.uwusoft.timesheet.extensionpoint.EmptyHolidayService;
 import com.uwusoft.timesheet.extensionpoint.HolidayService;
 import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
 import com.uwusoft.timesheet.extensionpoint.StorageService;
 import com.uwusoft.timesheet.extensionpoint.SubmissionService;
 import com.uwusoft.timesheet.submission.LocalSubmissionService;
 
 public class ExtensionManager<T> {
 
 	private String serviceId;
 	private static Map<String, Map<String, Object>> services = new HashMap<String, Map<String, Object>>();
 	private static Map<String, Object> fallbackServices = new HashMap<String, Object>();
 	
 	/**
 	 * @param serviceId
 	 */
 	public ExtensionManager(String serviceId) {
 		this.serviceId = serviceId;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public T getService(String contributorName) {
 		if (contributorName == null) return getFallbackService();
 		if (services.get(serviceId) == null) {
 			services.put(serviceId, new HashMap<String, Object>());
 			for (IConfigurationElement e : Platform.getExtensionRegistry().getConfigurationElementsFor(serviceId)) {
 				if (e.getContributor().getName().equals(contributorName))
 					try {
 						services.get(serviceId).put(contributorName, e.createExecutableExtension("class"));
 					} catch (CoreException e1) {
 						MessageBox.setError(this.getClass().getSimpleName(), e1.getMessage() + "\nTry to load fall back service");
 						return getFallbackService();
 					}
 			}
 		}
 		else if (services.get(serviceId).get(contributorName) == null) {
 			return getFallbackService();
 		}
 		return (T) services.get(serviceId).get(contributorName);
 	}
 	
 	/**
 	 * lazy load fall back service
 	 */
 	@SuppressWarnings("unchecked")
 	private T getFallbackService() {
 		if (fallbackServices.get(serviceId) == null) {
 			if (StorageService.SERVICE_ID.equals(serviceId))
				fallbackServices.put(serviceId, LocalStorageService.getInstance());
 			else if (SubmissionService.SERVICE_ID.equals(serviceId))
 				fallbackServices.put(serviceId, new LocalSubmissionService());
 			else if (HolidayService.SERVICE_ID.equals(serviceId))
 				fallbackServices.put(serviceId, new EmptyHolidayService());
 		}
 		return (T) fallbackServices.get(serviceId);
 	}
 }
