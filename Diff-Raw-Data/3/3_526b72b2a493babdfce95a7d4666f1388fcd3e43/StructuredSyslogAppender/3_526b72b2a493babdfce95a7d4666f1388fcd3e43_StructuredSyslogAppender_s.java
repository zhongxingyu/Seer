 /**
  * Copyright (C) 2011 Ness Computing, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.nesscomputing.log4j;
 
 import java.nio.charset.Charset;
 import java.util.Map;
 import java.util.UUID;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.AppenderSkeleton;
 import org.apache.log4j.spi.LoggingEvent;
 
 import com.google.common.base.Charsets;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableMap.Builder;
 import com.nesscomputing.syslog4j.Syslog;
 import com.nesscomputing.syslog4j.SyslogConfigIF;
 import com.nesscomputing.syslog4j.SyslogFacility;
 import com.nesscomputing.syslog4j.SyslogIF;
 import com.nesscomputing.syslog4j.SyslogLevel;
 import com.nesscomputing.syslog4j.impl.message.processor.structured.StructuredSyslogMessageProcessor;
 import com.nesscomputing.syslog4j.impl.message.structured.StructuredSyslogMessage;
 
 /**
  * A ness specific syslog appender to log information into logstash.<p>
  *
  * This appender generates RFC5424 compatible log messages and uses structured logging to capture information.<p>
  *
  * Every message has a message id (32 char UUID) associated.<p>
  *
  * Structured fields supported:<p>
  *
  * <ul>
  * <li>'l' is the fully qualified logger name.</li>
  * <li>'c' is the current line count. For single line messages, this is always 0. For multiline messages (e.g. stack traces), this count allows recombination of log messages.</li>
  * <li>'si' is the service id (if configured).</li>
  * <li>'sc' is the service configuration (if configured).</li>
  * <li>'t' is the track token if the MDC contains a 'track' value.</li>
  * </ul>
  *
  * All relevant fields of a structured syslog message are set:<p>
  *
  * <ul>
  * <li>HOSTNAME - see below.</li>
  * <li>APP-NAME - see below.</li>
  * <li>PROCID - thread name from the log4j logging event.</li>
  * <li>MSGID - 32 byte UUID.</li>
  * </ul>
  *
  * <pre>
   &lt;appender name="Syslog4j" class="com.nesscomputing.log4j.StructuredSyslogAppender"&gt;
     &lt;param name="threshold" value="debug" /&gt;
 
     &lt;param name="facility" value="local0"/&gt;
     &lt;param name="protocol" value="udp"/&gt;
     &lt;param name="syslogHost" value="localhost"/&gt;
     &lt;param name="syslogPort" value="514"/&gt;
     &lt;param name="charset" value="UTF-8"/&gt;
     &lt;param name="hostname" value="#{env.machine}" /&gt;
     &lt;param name="appname" value="#{deploy.type}" /&gt;
     &lt;param name="serviceId" value="#{env.agent_id}" /&gt;
     &lt;param name="serviceConfiguration" value="#{deploy.config}" /&gt;
     &lt;param name="maxMessageLength" value="1023"/&gt;
     &lt;param name="ianaIdentifier" value="12345" /&gt;
   &lt;/appender&gt;
  * </pre>
  */
 public class StructuredSyslogAppender extends AppenderSkeleton
 {
     private final AtomicReference<SyslogIF> syslogHolder = new AtomicReference<SyslogIF>();
 
     private SyslogFacility facility = SyslogFacility.local0;
     private String protocol = "udp";
     private String syslogHost = "localhost";
     private Charset charset = Charsets.UTF_8;
     private String hostname = "no-host-name";
     private String appname = "no-app-name";
     private String ianaIdentifier = "s@0";
     private String serviceId = null;
     private String serviceConfiguration = null;
 
     private int syslogPort = 514;
     private int maxMessageLength = 1023;
 
     @Override
     public boolean requiresLayout()
     {
         return false;
     }
 
     @Override
     protected void append(final LoggingEvent event)
     {
         final SyslogIF syslog = getSyslog();
         final SyslogLevel level = SyslogLevel.forValue(event.getLevel().getSyslogEquivalent());
 
         final String messageId = UUID.randomUUID().toString().replace("-", "");
 
         final String [] messageLines = StringUtils.split((layout != null) ? layout.format(event) : event.getRenderedMessage(), "\n\r");
 
         for (int i = 0; i < messageLines.length; i++) {
             final Builder<String, String> b = ImmutableMap.builder();
             b.put("l", event.getLoggerName());
             b.put("c", Integer.toString(i));
 
             if (serviceId != null) {
                 b.put("si", serviceId);
             }
 
             if (serviceConfiguration != null) {
                 b.put("sc", serviceConfiguration);
             }
 
             Object trackToken = event.getMDC("track");
             if (trackToken != null) {
                 b.put("t", trackToken.toString());
             }
 
 
             Map<String, String> payload = b.build();
 
            final StructuredSyslogMessage structuredMessage = new StructuredSyslogMessage(messageId, event.getThreadName(), ImmutableMap.of(ianaIdentifier, payload), messageLines[i]);
             syslog.log(level, structuredMessage);
         }
     }
 
     @Override
     public void close()
     {
         final SyslogIF syslog = getSyslog();
         syslog.flush();
     }
 
     private SyslogIF getSyslog()
     {
         SyslogIF syslog = syslogHolder.get();
         if (syslog == null) {
             syslog = Syslog.getInstance(protocol);
             Preconditions.checkState(syslog != null, "No syslog for protocol '%s' found!", protocol);
 
 
             final SyslogConfigIF config = syslog.getConfig();
             config.setUseStructuredData(true);
             config.setTruncateMessage(true);
             config.setMaxMessageLength(maxMessageLength);
             config.setCharSet(charset);
 
             config.setFacility(facility);
             config.setHost(syslogHost);
             config.setPort(syslogPort);
             config.setIdent("");
             config.setLocalName(hostname);
 
             final StructuredSyslogMessageProcessor messageProcessor = new StructuredSyslogMessageProcessor(appname);
             syslog.setStructuredMessageProcessor(messageProcessor);
 
             if (!syslogHolder.compareAndSet(null, syslog)) {
                 syslog = syslogHolder.get();
             }
         }
 
         return syslog;
     }
 
     /**
      * The charset to use for logging. Usually UTF-8 or ISO-8859-1.
      *
      * Default is UTF-8.
      */
     public void setCharset(final String charsetName)
     {
         this.charset = Charset.forName(charsetName);
     }
 
     /**
      * Sets the maximum length of a log message. Messages longer than this are truncated.
      *
      * Defaults to 1023 bytes, the actual message length is shorter because this includes the (generated) headers.
      */
     public void setMaxMessageLength(final int maxMessageLength)
     {
         this.maxMessageLength = maxMessageLength;
     }
 
     /**
      * The syslog facility. Valid names are in {@link SyslogFacility}.
      *
      * Default is local0.
      */
     public void setFacility(final String facilityName)
     {
         final SyslogFacility facility = SyslogFacility.forName(facilityName);
         Preconditions.checkArgument(facility != null, "Facility '%s' is unknown!", facilityName);
         this.facility = facility;
     }
 
     /**
      * The Syslog protocol. Usually udp or tcp, can be unix_syslog and unix_socket if JNA is present.
      *
      * Default is UDP.
      */
     public void setProtocol(final String protocol)
     {
         this.protocol = protocol;
     }
 
     /**
      * Name of the syslog target host.
      *
      * Default is localhost.
      */
     public void setSyslogHost(final String syslogHost)
     {
         this.syslogHost = syslogHost;
     }
 
     /**
      * Port of the syslog target host.
      *
      * Default is 514.
      */
     public void setSyslogPort(final int syslogPort)
     {
         this.syslogPort = syslogPort;
     }
 
     /**
      * The Hostname to use.
      *
      * Default is 'no-host-name'.
      */
     public void setHostname(final String hostname)
     {
         this.hostname = hostname;
     }
 
     /**
      * The Application name to use.
      *
      * Default is 'no-app-name'.
      */
     public void setAppname(String appname)
     {
         this.appname = appname;
     }
 
     /**
      * The local service id. If set, this is reflected in the "si" key of the structured log message.
      *
      * Default: unset.
      */
     public void setServiceId(String serviceId)
     {
         this.serviceId = serviceId;
     }
 
     /**
      * The local service configuration. If set, this is reflected in the "sc" key of the structured log message.
      *
      * Default: unset.
      */
     public void setServiceConfiguration(String serviceConfiguration)
     {
         this.serviceConfiguration = serviceConfiguration;
     }
 
     /**
      * The iana identifier for the structured log message. An IANA number must be used to tag the log messages. All messages are
      * in the 's@<this id>' part of the log message.
      *
      * Default is 0.
      */
     public void setIanaIdentifier(int ianaIdentifier)
     {
         this.ianaIdentifier = "s@" + Integer.toString(ianaIdentifier);
     }
 
     public String getFacility()
     {
         return facility.name();
     }
 
     public String getProtocol()
     {
         return protocol;
     }
 
     public String getSyslogHost()
     {
         return syslogHost;
     }
 
     public String getCharset()
     {
         return charset.name();
     }
 
     public String getHostname()
     {
         return hostname;
     }
 
     public String getAppname()
     {
         return appname;
     }
 
     public String getServiceId()
     {
         return serviceId;
     }
 
     public String getServiceConfiguration()
     {
         return serviceConfiguration;
     }
 
     public int getSyslogPort()
     {
         return syslogPort;
     }
 
     public int getMaxMessageLength()
     {
         return maxMessageLength;
     }
 
     public int getIanaIdentifier()
     {
         return Integer.parseInt(ianaIdentifier.substring(2));
     }
 }
