 /*
  * Copyright (c) 2009, GoodData Corporation. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided
  * that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice, this list of conditions and
  *        the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
  *        and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *     * Neither the name of the GoodData Corporation nor the names of its contributors may be used to endorse
  *        or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
  * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.gooddata.config;
 
 import com.thoughtworks.xstream.XStream;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class NotificationConfig {
 
     // initial XML config comment
     public static String CONFIG_INITIAL_COMMENT = "<!-- See documentation at " +
            "https://github.com/gooddata/GoodData-CL/blob/master/cli-distro/doc/XML.md -->\n\n";
 
 
     private List<NotificationMessage> messages = new ArrayList<NotificationMessage>();
 
     /**
      * Default constructor
      */
     public NotificationConfig() {
 
     }
 
     /**
      * Constructor
      *
      * @param messages config messages (elements)
      */
     public NotificationConfig(List<NotificationMessage> messages) {
         setMessages(messages);
     }
 
     public void addMessage(NotificationMessage msg) {
         messages.add(msg);
     }
 
     public List<NotificationMessage> getMessages() {
         return messages;
     }
 
     public void setMessages(List<NotificationMessage> messages) {
         this.messages = messages;
     }
 
     /**
      * Serializes the schema to XML
      *
      * @return the xml representation of the object
      * @throws java.io.IOException in case of an IO issue
      */
     protected String toXml() throws IOException {
         XStream xstream = new XStream();
         xstream.alias("metric", Metric.class);
         xstream.alias("report", Report.class);
         xstream.alias("message", NotificationMessage.class);
         xstream.alias("notification", NotificationConfig.class);
         return xstream.toXML(this);
     }
 
     /**
      * Deserializes the config from XML
      *
      * @param configFile the file with the XML definition
      * @throws IOException in case of an IO issue
      */
     public static NotificationConfig fromXml(File configFile) throws IOException {
         return fromXml(new FileInputStream(configFile));
     }
 
     /**
      * Deserializes the config from an XML stream
      *
      * @param is the stream of the XML definition
      * @throws IOException in case of an IO issue
      */
     public static NotificationConfig fromXml(InputStream is) throws IOException {
         XStream xstream = new XStream();
         xstream.alias("metric", Metric.class);
         xstream.alias("report", Report.class);
         xstream.alias("message", NotificationMessage.class);
         xstream.alias("notification", NotificationConfig.class);
         Reader r = new InputStreamReader(is, "utf8");
         NotificationConfig schema = (NotificationConfig) xstream.fromXML(r);
         r.close();
         return schema;
     }
 
     /**
      * Write the config file
      *
      * @param configFile the config file
      * @throws IOException in case of an IO issue
      */
     public void writeConfig(File configFile) throws IOException {
         Writer w = new OutputStreamWriter(new FileOutputStream(configFile), "utf8");
         w.write(CONFIG_INITIAL_COMMENT + toXml());
         w.flush();
         w.close();
     }
 
 }
