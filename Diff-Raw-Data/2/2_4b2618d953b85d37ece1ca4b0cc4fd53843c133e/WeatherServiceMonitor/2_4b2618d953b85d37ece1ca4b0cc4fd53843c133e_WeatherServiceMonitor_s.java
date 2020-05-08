 package com.citytechinc.monitoring.sample.weatherservice;
 
 import com.citytechinc.monitoring.constants.Constants;
 import com.citytechinc.monitoring.domain.ServiceMonitorResponse;
 import com.citytechinc.monitoring.domain.ServiceMonitorResponseType;
 import com.citytechinc.monitoring.services.ServiceMonitor;
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.apache.felix.scr.annotations.Activate;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Modified;
 import org.apache.felix.scr.annotations.Properties;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.ReferenceCardinality;
 import org.apache.felix.scr.annotations.ReferencePolicy;
 import org.apache.felix.scr.annotations.Service;
 import org.apache.sling.commons.osgi.OsgiUtil;
 
 import java.util.Map;
 
 /**
  *
  * This monitor keeps tabs on the "WeatherService".
  *
  * It is important to note that typically the ServiceMonitor's that keep track of other services have their references
  *   to those services defined as optional and dynamic. If this is not done and the "WeatherService" (in this case)
  *   goes offline, this WeatherServiceMonitor will too go offline. Such an event would remove the record and accounting
  *   information from the ServiceMonitorManager and the responsible parties would not be notified of the outage.
  *
  * It is also important to note that you can use this service to defined values that could change. For instance,
  *   instead of hard coding the values for Chicago weather, we'll make them OSGi service parameters. By making your test
  *   parameters configurable, you can easy change the test values on the fly.
  *
  * @author CITYTECH, INC. 2013
  *
  */
 @Component(label = "Weather Service Monitor", description = "")
 @Service
 @Properties({
         @Property(name = org.osgi.framework.Constants.SERVICE_VENDOR, value = Constants.CITYTECH_SERVICE_VENDOR_NAME) })
 public final class WeatherServiceMonitor implements ServiceMonitor {
 
     // IMPORTANT TO NOTE THIS REFERENCE IS OPTIONAL AND DYNAMIC
     @Reference(cardinality = ReferenceCardinality.OPTIONAL_UNARY, policy = ReferencePolicy.DYNAMIC)
     WeatherService weatherService;
 
     private static final String DEFAULT_TEST_ZIP = "60606";
 
    @Property(label = "Test Zip", value = DEFAULT_TEST_ZIP, description = "The zip code to use for forecase lookup")
     private static final String TEST_ZIP_PROPERTY = "testZip";
     private String testZip;
 
     @Activate
     @Modified
     protected void activate(final Map<String, Object> properties) throws Exception {
 
         testZip = OsgiUtil.toString(properties.get(TEST_ZIP_PROPERTY), DEFAULT_TEST_ZIP);
     }
 
     @Override
     public ServiceMonitorResponse poll() {
 
         // DEFAULT, HOPEFUL RESPONSE TYPE
         ServiceMonitorResponse serviceMonitorResponse = new ServiceMonitorResponse(ServiceMonitorResponseType.SUCCESS);
 
         try {
 
             if (weatherService == null) {
 
                 // NOTICE THAT THE SERVICE_UNAVAILABLE RESPONSE IS TYPICALLY USED TO DENOTE THE SERVICE REFERENCE IS NULL
                 serviceMonitorResponse = new ServiceMonitorResponse(ServiceMonitorResponseType.SERVICE_UNAVAILABLE);
             } else {
 
                 // TEST ZIP CODE SHOULD NEVER RETURN NULL FORECAST. A NULL RESPONSE WOULD INDICATE A POTENTIAL PROBLEM SOMEWHERE UP THE CHAIN
                 if (weatherService.getForecastForZipCode(testZip) != null) {
 
                     serviceMonitorResponse = new ServiceMonitorResponse(ServiceMonitorResponseType.UNEXPECTED_SERVICE_RESPONSE);
                 }
             }
 
         } catch (final Exception exception) {
 
             // SELF EXPLANATORY EXCEPTION
             serviceMonitorResponse = new ServiceMonitorResponse(ExceptionUtils.getStackTrace(exception));
         }
 
         return serviceMonitorResponse;
     }
 }
