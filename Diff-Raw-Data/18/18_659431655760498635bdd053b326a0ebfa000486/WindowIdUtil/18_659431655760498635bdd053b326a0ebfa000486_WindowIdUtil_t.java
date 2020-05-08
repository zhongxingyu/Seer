 /*
  * Copyright 2004-2008 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package javax.faces.internal;
 
 import java.util.Map;
import java.util.Random;
 
 import javax.faces.FacesException;
 import javax.faces.context.ExternalContext;
 
 import org.seasar.framework.util.StringUtil;
 import org.seasar.teeda.core.JsfConstants;
 
 /**
  * @author higa
  *
  */
 public abstract class WindowIdUtil {
 
     public static final String NEWWINDOW = "newwindow";
 
     public static final String WID = "wid";
 
     private static final String BLANK = "_blank";
 
    private static Random random = new Random(System.currentTimeMillis());
 
     protected WindowIdUtil() {
     }
 
     public static String setupWindowId(final ExternalContext externalContext)
             throws FacesException {
         String wid = null;
         if (needNewWindow(externalContext.getRequestParameterMap())) {
             wid = createWindowId();
         } else {
             Map paramMap = externalContext.getRequestParameterMap();
             wid = (String) paramMap.get(WID);
         }
         setWindowId(externalContext, wid);
         return wid;
     }
 
     public static synchronized String createWindowId() {
        return Long.toString(random.nextLong());
     }
 
     public static String getWindowId(final ExternalContext externalContext)
             throws FacesException {
         Map requestMap = externalContext.getRequestMap();
         return (String) requestMap.get(WindowIdUtil.WID);
     }
 
     public static void setWindowId(final ExternalContext externalContext,
             String wid) throws FacesException {
         Map requestMap = externalContext.getRequestMap();
         requestMap.put(WID, wid);
     }
 
     public static boolean isNewWindowTarget(String target) {
         if (StringUtil.isEmpty(target)) {
             return false;
         }
         return BLANK.equals(target) || target.charAt(0) != '_';
     }
 
     public static boolean needNewWindow(final Map parameterMap) {
         final String newwindow = (String) parameterMap.get(NEWWINDOW);
         return JsfConstants.TRUE.equals(newwindow);
     }
 }
