 package com.weather.percussion.extensions.transformers;
 
 import java.io.File;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.percussion.data.PSConversionException;
 import com.percussion.extension.IPSExtensionDef;
 import com.percussion.extension.IPSFieldInputTransformer;
 import com.percussion.extension.PSDefaultExtension;
 import com.percussion.extension.PSExtensionException;
 import com.percussion.pso.utils.PSOExtensionParamsHelper;
 import com.percussion.server.IPSRequestContext;
 
 public class TWCTransformFieldDateAppender extends PSDefaultExtension implements
         IPSFieldInputTransformer {
     
     public TWCTransformFieldDateAppender() {
         extDef = null;
     }
 
     @Override
     public Object processUdf(Object[] params, IPSRequestContext request)
             throws PSConversionException {
        String actionType = request.getParameter(DB_ACTION_TYPE_PARAM); 
         
         PSOExtensionParamsHelper paramsHelper = new PSOExtensionParamsHelper(extDef, params, request, log);
         
         String fieldName = paramsHelper.getRequiredParameter(FIELD_NAME_EXT_PARAM);
        log.debug("Field: " + fieldName);
         String replaceWithField = paramsHelper.getParameter(REPLACE_WITH_FIELD_PARAM);
        log.debug("replace with field: " + replaceWithField);
         String preOrPost = paramsHelper.getRequiredParameter(PRE_OR_POST_EXT_PARAM);
        log.debug("pre or post: " + preOrPost);
        
        //Return true if action type not valid
        if(actionType == null || !actionType.equals(ACTION_TYPE_INSERT)) {
            log.debug("Action type is not insert, no key transformation");
            return request.getParameter(fieldName);       
        }        
         
         String fieldValue = "";
         if (StringUtils.isBlank(replaceWithField)) {
             fieldValue = request.getParameter(fieldName);
            log.debug("Field value: " + fieldValue);
         }
         else {
             fieldValue = request.getParameter(replaceWithField);
            log.debug("Overriding field, found value: " + fieldValue);
         }
         
         Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
         String dateFormatted = dateFormat.format(now);
         
         if (PRE_PARAM_VALUE.equals(preOrPost)) {
             fieldValue = (new StringBuilder(dateFormatted)).append(DELIMITER).append(fieldValue).toString();    
         }
         else if (POST_PARAM_VALUE.equals(preOrPost)) {
             fieldValue = (new StringBuilder(fieldValue)).append(DELIMITER).append(dateFormatted).toString();
         }
         else if (BOTH_PARAM_VALUE.equals(preOrPost)) {
             fieldValue = (new StringBuilder(dateFormatted)).append(DELIMITER).append(fieldValue).append(DELIMITER).append(dateFormatted).toString();
         }
         
        log.debug("returning field value: " + fieldValue);
         return fieldValue;
     }
     
     public void init(IPSExtensionDef def, File ifile) throws PSExtensionException {
         super.init(def, ifile);
         extDef = def;
     }    
     
     private IPSExtensionDef extDef;
     
     private static final String DB_ACTION_TYPE_PARAM = "DBActionType";
     private static final String ACTION_TYPE_INSERT = "INSERT";
     private static final String FIELD_NAME_EXT_PARAM = "fieldName";
     private static final String PRE_OR_POST_EXT_PARAM = "preOrPost";
     private static final String REPLACE_WITH_FIELD_PARAM = "replaceWithField";
     private static final String PRE_PARAM_VALUE = "pre";
     private static final String POST_PARAM_VALUE = "post";
     private static final String BOTH_PARAM_VALUE = "both";
     private static final String DELIMITER = "-";
     private static final Log log = LogFactory.getLog(com.weather.percussion.extensions.transformers.TWCTransformFieldDateAppender.class);
 
 }
