 /*
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.laxser.blitz.web.taglibs;
 
 import java.io.IOException;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.tagext.TagSupport;
 
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.laxser.blitz.web.Invocation;
 import com.laxser.blitz.web.InvocationUtils;
 
 /**
 * 在jsp中支持 flash标签，其实这里只是临时解决策略，计划使用jsp taglib支持更多更灵活的标签形式
  * 
 *@author laxser  Date 2012-4-5 上午11:29:10
@contact [duqifan@gmail.com]
@FlashTag.java

  * 
  */
 public class FlashTag extends TagSupport {
 
     private static final long serialVersionUID = -1951600183488515087L;
 
     private static Log logger = LogFactory.getLog(FlashTag.class);
 
     private String key;
 
     private String prefix = "";
 
     private String suffix = "";
 
     public void setKey(String key) {
         this.key = key;
     }
 
     public void setPrefix(String prefix) {
         this.prefix = prefix;
     }
 
     public void setSuffix(String suffix) {
         this.suffix = suffix;
     }
 
     @Override
     public int doStartTag() throws JspException {
         Invocation invocation = InvocationUtils.getCurrentThreadInvocation();
         if (invocation != null) {
             String msg = invocation.getFlash().get(key);
             if (logger.isDebugEnabled()) {
                 logger.debug("getFlashMessage: " + key + "=" + msg);
             }
             if (msg != null) {
                 try {
                     if (StringUtils.isNotEmpty(prefix)) {
                         pageContext.getOut().print(prefix);
                     }
                     pageContext.getOut().print(msg);
                     if (StringUtils.isNotEmpty(suffix)) {
                         pageContext.getOut().print(suffix);
                     }
                 } catch (IOException e) {
                     throw new JspException("", e);
                 }
             }
         }
         return super.doStartTag();
     }
 }
