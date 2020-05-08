 package org.filterinterceptor.management;
 
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 
 import javax.management.InstanceAlreadyExistsException;
 import javax.management.MBeanRegistrationException;
 import javax.management.MBeanServer;
 import javax.management.MalformedObjectNameException;
 import javax.management.NotCompliantMBeanException;
 import javax.management.ObjectName;
 
 import org.filterinterceptor.FilterService;
 import org.filterinterceptor.spi.Filter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
 * This class exposes {@link FilterServiceManagement} methods in a MBean and
 * register MBean component
  */
 
 public class FilterServiceManagement implements FilterServiceManagementMBean {
 	private static final Logger logger = LoggerFactory.getLogger(FilterServiceManagement.class);
 
 	private final FilterService filterService;
 
 	// TODO tests JMX
 
 	@SuppressWarnings("serial")
 	public static void initMBean(FilterService filterService) {
 		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
 		// MBeanServerFactory.createMBeanServer(filterService.getClass().getName());
 		try {
 			logger.info("Register MBean FilterServiceManagement");
 			String domain = "FilterService";
 			mbs.registerMBean(new FilterServiceManagement(filterService), new ObjectName(domain, "type", "Management"));
 
 			logger.info("Register MBeans FilterManagement");
 			for (final Filter<?> filter : filterService.getAllFilters()) {
 				mbs.registerMBean(new FilterManagement(filter, filterService), new ObjectName(domain,
 						new Hashtable<String, String>() {
 							{
 								put("filter", filter.getService().getSimpleName());
 								put("label", filter.getDescription());
 							}
 						}));
 			}
 			logger.info("MBeans registered successfully");
 		} catch (InstanceAlreadyExistsException e) {
 			// TODOJ7: exception management
 			logger.warn("Bean already registered: " + e.getMessage(), e);
 		} catch (MBeanRegistrationException e) {
 			logger.warn("Can't register bean: " + e.getMessage(), e);
 		} catch (NotCompliantMBeanException e) {
 			logger.warn("Bean is not compliant: " + e.getMessage(), e);
 		} catch (MalformedObjectNameException e) {
 			logger.warn("Bean name is not compliant: " + e.getMessage(), e);
 		}
 	}
 
 	private FilterServiceManagement(FilterService filterService) {
 		this.filterService = filterService;
 	}
 
 	@Override
 	public List<String> getFilters() {
 		List<String> ret = new ArrayList<String>();
 		for (Filter<?> filter : filterService.getAllFilters())
 			ret.add(filter.toString());
 		return ret;
 	}
 
 	@Override
 	public List<String> getActiveFilters() {
 		List<String> ret = new ArrayList<String>();
 		for (Map.Entry<String, Filter<?>> filterMap : filterService.getAllActiveFiltersUsed().entrySet())
 			ret.add(String.format("%s: %s", filterMap.getKey(), filterMap.getValue().toString()));
 		return ret;
 	}
 
 	@Override
 	public void reinitFilters() throws IOException {
 		filterService.initFilters();
 	}
 
 }
