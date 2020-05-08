 /*
  * Sonar Email Plugin
  * Copyright (C) 2011 SonarSource
  * dev@sonar.codehaus.org
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.plugins.email;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.mail.EmailException;
 import org.apache.commons.mail.SimpleEmail;
 import org.sonar.api.batch.PostJob;
 import org.sonar.api.batch.SensorContext;
 import org.sonar.api.platform.Server;
 import org.sonar.api.resources.Project;
 import org.sonar.api.utils.SonarException;
 
 /**
  * @author Evgeny Mandrikov
  */
 public class EmailPublisher implements PostJob {
 
   public static final String HOST_PROPERTY = "sonar.email.smtp_host.secured";
   public static final String SMTP_HOST_DEFAULT_VALUE = "localhost";
 
   public static final String PORT_PROPERTY = "sonar.email.smtp_port.secured";
   public static final String PORT_DEFAULT_VALUE = "25";
 
   public static final String TLS_PROPERTY = "sonar.email.password.secured";
   public static final boolean TLS_DEFAULT_VALUE = false;
 
   public static final String USERNAME_PROPERTY = "sonar.email.smtp_username.secured";
 
   public static final String PASSWORD_PROPERTY = "sonar.email.smtp_password.secured";
 
   public static final String FROM_PROPERTY = "sonar.email.from.secured";
 
   public static final String TO_PROPERTY = "sonar.email.to";
 
   private static final String PROJECT_INDEX_URI = "/project/index/";
 
   private Server server;
 
   public EmailPublisher(Server server) {
     this.server = server;
   }
 
   public void executeOn(Project project, SensorContext context) {
     Configuration configuration = project.getConfiguration();
     SonarEmail email = new SonarEmail();
     String host = configuration.getString(HOST_PROPERTY, SMTP_HOST_DEFAULT_VALUE);
     String port = configuration.getString(PORT_PROPERTY, PORT_DEFAULT_VALUE);
     String username = configuration.getString(USERNAME_PROPERTY);
     String password = configuration.getString(PASSWORD_PROPERTY);
     boolean withTLS = configuration.getBoolean(TLS_PROPERTY, TLS_DEFAULT_VALUE);
     String from = configuration.getString(FROM_PROPERTY);
     String to = configuration.getString(TO_PROPERTY);
     try {
       email.setHostName(host);
       email.setSmtpPort(port);
      email.setAuthentication(username, password);
       email.setTLS(withTLS);
       email.setFrom(from);
 
      String[] addrs = StringUtils.split(to, '\n');
       for (String addr : addrs) {
        email.addTo(addr);
       }
       email.setSubject(getSubject(project));
       email.setMsg(getMessage(project));
       email.send();
     } catch (EmailException e) {
       throw new SonarException("Unable to send email", e);
     }
   }
 
   String getSubject(Project project) {
     return String.format("Sonar analysis of %s", project.getName());
   }
 
   String getMessage(Project project) {
     StringBuilder url = new StringBuilder(server.getURL()).append(PROJECT_INDEX_URI).append(project.getKey());
     return String.format("Sonar analysis of %s available %s", project.getName(), url);
   }
 
   static class SonarEmail extends SimpleEmail {
     public void setSmtpPort(String smtpPort) {
       this.smtpPort = smtpPort;
     }
   }
 
 }
