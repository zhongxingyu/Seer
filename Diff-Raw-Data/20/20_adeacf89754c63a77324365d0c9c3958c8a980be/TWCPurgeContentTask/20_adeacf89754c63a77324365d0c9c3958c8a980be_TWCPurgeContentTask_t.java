 package com.weather.percussion.extensions.schedule;
 
 import java.io.File;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.percussion.extension.IPSExtensionDef;
 import com.percussion.extension.PSExtensionException;
 import com.percussion.services.schedule.IPSTask;
 import com.percussion.services.schedule.IPSTaskResult;
 import com.percussion.services.schedule.data.PSTaskResult;
 import com.percussion.services.schedule.impl.PSScheduleUtils;
 import com.weather.percussion.content.ContentPurgeUtil;
 
 public class TWCPurgeContentTask implements IPSTask {
     
     private static final Log log = LogFactory.getLog(TWCPurgeContentTask.class);
     public static final String USER_IDS_PARAM = "userIds";
     public static final String FOLDER_PATHS_PARAM = "folderPaths";
     public static final String CONTENT_TYPES_PARAM = "contentTypes";
     public static final String NUMBER_OF_DAYS = "numberOfDays";
    public static final String CONTENT_STATE_IDS_PARAM = "workflowStateIds";
     
     public void init(IPSExtensionDef extensionDef, File file) throws PSExtensionException {
     } 
     
     public IPSTaskResult perform(Map<String, String> params) {
         boolean isSuccessful = true;
         String returnMsg = null;
         long startTimeMillisecs = System.currentTimeMillis();
         long endTimeMillisecs = 0L;
         try {
             ContentPurgeUtil contentPurgeUtil = new ContentPurgeUtil(params);
             contentPurgeUtil.purge();            
         }
         catch(Exception exception) {
             isSuccessful = false;
             log.error("Failed to run TWCPurgeContentTask", exception);
             returnMsg = getExceptionCauseLocalizedMsg(exception);
         }
         endTimeMillisecs = System.currentTimeMillis();
         return new PSTaskResult(isSuccessful, returnMsg, 
                 PSScheduleUtils.getContextVars(params, startTimeMillisecs, endTimeMillisecs));
     }
     
     private String getExceptionCauseLocalizedMsg(Exception exception) {
         Throwable causeException = exception;
         if (exception.getCause() != null) {
             causeException = exception.getCause();
         }
         return causeException.getLocalizedMessage();        
     }
     
 }
