 package com.rcs.liferaysense.portlet.senseliferaysensor;
 
 import com.liferay.portal.kernel.events.Action;
 import com.liferay.portal.kernel.exception.PortalException;
 import com.liferay.portal.kernel.exception.SystemException;
 import com.liferay.portal.kernel.log.Log;
 import com.liferay.portal.kernel.log.LogFactoryUtil;
 import com.liferay.portal.kernel.util.WebKeys;
 import com.liferay.portal.theme.ThemeDisplay;
 import com.liferay.portal.util.PortalUtil;
 import com.rcs.common.service.ServiceActionResult;
 import static com.rcs.liferaysense.common.Constants.ADMIN_CONFIGURATION_DEFAULT_SENSE_LIFERAYSENSORDATA_ID;
 import com.rcs.liferaysense.entities.SenseConfiguration;
 import com.rcs.liferaysense.portlet.common.Utils;
 import com.rcs.liferaysense.service.commonsense.CommonSenseService;
 import com.rcs.liferaysense.service.commonsense.CommonSenseSession;
 import com.rcs.liferaysense.service.commonsense.LiferaySensorData;
 import com.rcs.liferaysense.service.local.SenseConfigurationService;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
 /**
  * @author Prj.M@x <pablo.rendon@rotterdam-cs.com>
  */
 public class Hooksensestoredata extends Action {
     private static Log log = LogFactoryUtil.getLog(Hooksensestoredata.class);
 
     @Autowired
     private Utils utils;    
     @Autowired
     private CommonSenseService commonSenseService; 
     @Autowired
     private SenseConfigurationService senseConfigurationService;
     
     @Override
     public void run(HttpServletRequest request, HttpServletResponse response) {
         ServletContext servletContext = request.getSession().getServletContext();
        servletContext = servletContext.getContext("/rcs-liferay-real-time-portlet");
         WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
         springContext.getAutowireCapableBeanFactory().autowireBean(this);           
         try {
             doRun(request, response);
         } catch (Exception e) {
             log.error(e.getMessage() + " - " + e);
         }
     }
 
     protected void doRun (HttpServletRequest request, HttpServletResponse response) {
         ThemeDisplay themeDisplay= (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
         String fullURLRequest = PortalUtil.getCurrentCompleteURL(request);
         //Only store data for front end pages (Not control panel pages)
         if (!themeDisplay.getLayout().isTypeControlPanel() && !fullURLRequest.contains("/c/") ) {
             HttpSession httpSession = request.getSession();            
 
             long groupId = themeDisplay.getScopeGroupId();
             long companyId = themeDisplay.getCompanyId(); 
             
             String pageTitle = themeDisplay.getLayout().getName(themeDisplay.getLocale()); 
             long pageId = themeDisplay.getLayout().getLayoutId();    
             String previousPageTitle = (String) httpSession.getAttribute("pageTitle"); 
 
             long previousPageId = 0;
             if (httpSession.getAttribute("pageId") != null) {
                 previousPageId = (Long) httpSession.getAttribute("pageId");    
             }           
             
             // To avoid multiple calls of the same request
             boolean ftime = true;
             if (httpSession.getAttribute("currentTime") != null) {
                 Calendar gc = GregorianCalendar.getInstance();
                 gc.setTime(new Date());         
                 gc.add(Calendar.SECOND, -10);        
                 Long toCompare = gc.getTimeInMillis();
                 Long previousTime = (Long) httpSession.getAttribute("currentTime");
                 if (previousPageId == pageId && previousTime > toCompare) {
                     ftime = false;
                 }
             }            
             if (ftime) {
                 
                 log.info("liferay sense hook logger " + fullURLRequest);                
                 httpSession.setAttribute("currentTime", new Date().getTime());                
                 httpSession.setAttribute("pageTitle", pageTitle);
                 httpSession.setAttribute("pageId", pageId);
                 String userAgent = request.getHeader("User-Agent");
                 String userIP = request.getRemoteAddr();
                 CommonSenseSession commonSenseSession = null;
                 try {                
                     commonSenseSession = utils.getDefaultUserCommonSenseSession(groupId, companyId);                
                     if (commonSenseSession != null) {
                         long liferayUserId = 0;
                         if (themeDisplay.isSignedIn()) {
                             liferayUserId = utils.getUserId(request);
                         }                        
                         LiferaySensorData liferaySensorData = new LiferaySensorData();
                         liferaySensorData.setIp(userIP);
                         liferaySensorData.setPageId(pageId);
                         liferaySensorData.setPrevious_pageId(previousPageId);
                         liferaySensorData.setPage(pageTitle);
                         liferaySensorData.setPrevious_page(previousPageTitle);
                         liferaySensorData.setUserAgent(userAgent);
                         liferaySensorData.setLiferayUserId(liferayUserId);
                         ServiceActionResult<SenseConfiguration> serviceActionResultLiferaySensorData = senseConfigurationService.findByProperty(groupId, companyId, ADMIN_CONFIGURATION_DEFAULT_SENSE_LIFERAYSENSORDATA_ID);
                         if (serviceActionResultLiferaySensorData.isSuccess()) {
                             String liferaySensorId = serviceActionResultLiferaySensorData.getPayload().getPropertyValue(); 
                             commonSenseService.addLiferaySensorData(commonSenseSession, liferaySensorId, liferaySensorData);                    
                         }                       
 //                        //Store ClientLocation Data
 //                        Gson gson = new Gson();
 //                        ClientLocation clientLocation = gson.fromJson(clientlocation, ClientLocation.class);
 //                        clientLocation.setIp(userIP);                
 //                        ServiceActionResult<SenseConfiguration> serviceActionResultClientlocationSensor = senseConfigurationService.findByProperty(groupId, companyId, ADMIN_CONFIGURATION_DEFAULT_SENSE_CLIENTLOCATIONSENSOR_ID);
 //                        if (serviceActionResultClientlocationSensor.isSuccess()) {
 //                            String clientLocationSensorId = serviceActionResultClientlocationSensor.getPayload().getPropertyValue();  
 //                            commonSenseService.addClientLocationData(commonSenseSession, clientLocationSensorId, clientLocation);
 //                        }
                     } else {
                         log.error("commonSense Session null");
                     }            
                 } catch(PortalException e) {
                     log.error("No CommonSense Session: " + e);
                 } catch(SystemException e) {
                     log.error("No CommonSense Session: " + e);
                 }
             }
         }
     }
        
 }
