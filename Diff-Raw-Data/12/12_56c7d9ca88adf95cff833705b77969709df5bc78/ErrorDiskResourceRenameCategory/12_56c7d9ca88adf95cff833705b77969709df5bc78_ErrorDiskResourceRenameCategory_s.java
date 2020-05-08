 package org.iplantc.core.uidiskresource.client.services.errors.categories;
 
 import org.iplantc.core.uicommons.client.util.DiskResourceUtil;
 import org.iplantc.core.uidiskresource.client.services.errors.ErrorDiskResourceRename;
 
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.web.bindery.autobean.shared.AutoBean;
 
 public class ErrorDiskResourceRenameCategory {
 
     public static SafeHtml generateErrorMsg(AutoBean<ErrorDiskResourceRename> instance) {
         ErrorDiskResourceRename error = instance.as();
 
         return ErrorDiskResourceCategory.getErrorMessage(
                 ErrorDiskResourceCategory.getDiskResourceErrorCode(error.getErrorCode()),
                 DiskResourceUtil.parseNameFromPath(error.getPath()));
     }
 }
