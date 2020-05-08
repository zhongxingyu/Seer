 /***********************************************************************
  * Copyright (c) 2006 The Apache Software Foundation.                  *
  * All rights reserved.                                                *
  * ------------------------------------------------------------------- *
  * Licensed under the Apache License, Version 2.0 (the "License"); you *
  * may not use this file except in compliance with the License. You    *
  * may obtain a copy of the License at:                                *
  *                                                                     *
  *     http://www.apache.org/licenses/LICENSE-2.0                      *
  *                                                                     *
  * Unless required by applicable law or agreed to in writing, software *
  * distributed under the License is distributed on an "AS IS" BASIS,   *
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or     *
  * implied.  See the License for the specific language governing       *
  * permissions and limitations under the License.                      *
  ***********************************************************************/
 
 
 package org.apache.james.postage.smtpserver;
 
 import org.apache.avalon.framework.configuration.DefaultConfiguration;
 import org.apache.avalon.framework.configuration.Configuration;
 import org.apache.james.smtpserver.HeloCmdHandler;
 import org.apache.james.smtpserver.EhloCmdHandler;
 import org.apache.james.smtpserver.AuthCmdHandler;
 import org.apache.james.smtpserver.VrfyCmdHandler;
 import org.apache.james.smtpserver.ExpnCmdHandler;
 import org.apache.james.smtpserver.MailCmdHandler;
 import org.apache.james.smtpserver.RcptCmdHandler;
 import org.apache.james.smtpserver.DataCmdHandler;
 import org.apache.james.smtpserver.RsetCmdHandler;
 import org.apache.james.smtpserver.HelpCmdHandler;
 import org.apache.james.smtpserver.QuitCmdHandler;
 
 public class SimpleSMTPServerConfiguration extends DefaultConfiguration {
     private int m_smtpListenerPort;
     private String m_authorizedAddresses = "127.0.0.0/8";
     private String m_authorizingMode = "false";
 
     public static Configuration getValuedConfiguration(String name, String value) {
         DefaultConfiguration defaultConfiguration = new DefaultConfiguration(name);
         defaultConfiguration.setValue(value);
         return defaultConfiguration;
     }
 
    public static DefaultConfiguration createRemoteManagerHandlerChainConfiguration() {
         DefaultConfiguration handlerChainConfig = new DefaultConfiguration("handlerchain");
         handlerChainConfig.addChild(createCommandHandlerConfiguration("HELO", HeloCmdHandler.class));
         handlerChainConfig.addChild(createCommandHandlerConfiguration("EHLO", EhloCmdHandler.class));
         handlerChainConfig.addChild(createCommandHandlerConfiguration("AUTH", AuthCmdHandler.class));
         handlerChainConfig.addChild(createCommandHandlerConfiguration("VRFY", VrfyCmdHandler.class));
         handlerChainConfig.addChild(createCommandHandlerConfiguration("EXPN", ExpnCmdHandler.class));
         handlerChainConfig.addChild(createCommandHandlerConfiguration("MAIL", MailCmdHandler.class));
         handlerChainConfig.addChild(createCommandHandlerConfiguration("RCPT", RcptCmdHandler.class));
         handlerChainConfig.addChild(createCommandHandlerConfiguration("DATA", DataCmdHandler.class));
         handlerChainConfig.addChild(createCommandHandlerConfiguration("RSET", RsetCmdHandler.class));
         handlerChainConfig.addChild(createCommandHandlerConfiguration("HELP", HelpCmdHandler.class));
         handlerChainConfig.addChild(createCommandHandlerConfiguration("QUIT", QuitCmdHandler.class));
         return handlerChainConfig;
     }
 
     private static DefaultConfiguration createCommandHandlerConfiguration(String command, Class commandClass) {
         DefaultConfiguration cmdHandlerConfig = new DefaultConfiguration("handler");
         cmdHandlerConfig.setAttribute("command", command);
         String classname = commandClass.getName();
         cmdHandlerConfig.setAttribute("class", classname);
         return cmdHandlerConfig;
     }
     public SimpleSMTPServerConfiguration(int smtpListenerPort) {
         super("smptserver");
 
         m_smtpListenerPort = smtpListenerPort;
     }
 
     public String getAuthorizedAddresses() {
         return m_authorizedAddresses;
     }
 
     public void setAuthorizedAddresses(String authorizedAddresses) {
         m_authorizedAddresses = authorizedAddresses;
     }
 
     public void setAuthorizingNotRequired() {
         m_authorizingMode = "false";
     }
 
     public void setAuthorizingRequired() {
         m_authorizingMode = "true";
         //m_verifyIdentity = true;
     }
 
     public void setAuthorizingAnnounce() {
         m_authorizingMode = "announce";
         //m_verifyIdentity = true;
     }
 
     public void init() {
 
         setAttribute("enabled", true);
 
         addChild(getValuedConfiguration("port", "" + m_smtpListenerPort));
 
         DefaultConfiguration handlerConfig = new DefaultConfiguration("handler");
         handlerConfig.addChild(getValuedConfiguration("helloName", "myMailServer"));
         handlerConfig.addChild(getValuedConfiguration("connectiontimeout", "360000"));
         handlerConfig.addChild(getValuedConfiguration("authorizedAddresses", m_authorizedAddresses));
         handlerConfig.addChild(getValuedConfiguration("maxmessagesize", "" + 0));
         handlerConfig.addChild(getValuedConfiguration("authRequired", m_authorizingMode));
 
        handlerConfig.addChild(createRemoteManagerHandlerChainConfiguration());
         addChild(handlerConfig);
     }
 
 }
