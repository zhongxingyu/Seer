 /*
  * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
  * Author: Konstantin S. Vishnivetsky
  * E-mail: info@siplabs.ru
  * Contributors retain copyright to elements licensed under a Contributor Agreement.
  * Licensed to the User under the LGPL license.
  *
 */
 
 package org.sipfoundry.sipxconfig.web.plugin;
 
 /* Tapestry 4 page API imports */
 import org.apache.tapestry.annotations.Bean;
 import org.apache.tapestry.annotations.InjectObject;
 import org.apache.tapestry.annotations.Persist;
 import org.apache.tapestry.event.PageBeginRenderListener;
 import org.apache.tapestry.event.PageEvent;
 
 /*sipXecs WEB components API imports */
 import org.sipfoundry.sipxconfig.components.PageWithCallback;
 import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
 import org.sipfoundry.sipxconfig.components.TapestryUtils;
 
 /*sipXecs WEB settings API imports */
 import org.sipfoundry.sipxconfig.freeswitchcustomextensions.CustomFreeswitchExtension;
 import org.sipfoundry.sipxconfig.freeswitchcustomextensions.CustomFreeswitchExtensionsContext;
 
 public abstract class CustomFreeswitchExtensionsEditExtension extends PageWithCallback implements PageBeginRenderListener {
     public static final String PAGE = "plugin/CustomFreeswitchExtensionsEditExtension";
 
     /* Properties */
     @InjectObject("spring:customFreeswitchExtensionsContext")
     public abstract CustomFreeswitchExtensionsContext getCustomFreeswitchExtensionsContext();
 
     @Persist
     public abstract Integer getObjectId();
 
     public abstract void setObjectId(Integer id);
 
     @Persist
     public abstract CustomFreeswitchExtension getObject();
 
     public abstract void setObject(CustomFreeswitchExtension obj);
 
     @Bean
     public abstract SipxValidationDelegate getValidator();
 
     /*  Methods */
 
     public void pageBeginRender(PageEvent event) {
         if (!TapestryUtils.isValid(this)) {
             return;
         }
 
         CustomFreeswitchExtension obj = getObject();
         if (null != obj) {
             return;
         }
 
         Integer id = getObjectId();
         if (null != id) {
             CustomFreeswitchExtensionsContext context = getCustomFreeswitchExtensionsContext();
             obj = context.getFreeswitchExtensionById(id);
         } else {
             obj = getCustomFreeswitchExtensionsContext().newFreeswitchExtension();
         }
         setObject(obj);
 
         if (getCallback() == null) {
             setReturnPage(CustomFreeswitchExtensionsPage.PAGE);
         }
     }
 
     public void commit() {
         if (isValid()) {
             saveValid();
         }
     }
 
     private boolean isValid() {
         return TapestryUtils.isValid(this);
     }
 
     private void saveValid() {
         CustomFreeswitchExtensionsContext context = getCustomFreeswitchExtensionsContext();
         CustomFreeswitchExtension obj = getObject();
         // call set extension - hack to regenerate FS dialplan
 //        obj.setExtension(obj.getExtension());
         context.saveFreeswitchExtension(obj);
         Integer id = getObject().getId();
         setObjectId(id);
     }
 }
