 package com.github.picologger.syslog;
 
 /**
  * Implement RFC5424 and RFC3164
  * 
  * @author kyanhe
  * 
  */
 public class Syslog
 {
     private int facility;
     
     private int severity;
     
     private int version;
     
     private String timestamp;
     
     private String hostname;
     
     private String appname;
     
     private String procid;
     
     private String msgid;
     
     //TODO: define structured-data struct
     private String sd;
     
     private String msg;
     
     // Indicates this is RFC3164 or RFC5424
     private boolean bsd;
     
     public int getFacility()
     {
         return facility;
     }
     
     public void setFacility(int facility)
     {
         this.facility = facility;
     }
     
     public int getSeverity()
     {
         return severity;
     }
     
     public void setSeverity(int severity)
     {
         this.severity = severity;
     }
     
     public int getVersion()
     {
         return version;
     }
     
     public void setVersion(int version)
     {
         this.version = version;
     }
     
     public String getTimestamp()
     {
         return timestamp;
     }
     
     public void setTimestamp(String timestamp)
     {
         this.timestamp = timestamp;
     }
     
     public String getHostname()
     {
         return hostname;
     }
     
     public void setHostname(String hostname)
     {
         this.hostname = hostname;
     }
     
     public String getAppname()
     {
         return appname;
     }
     
     public void setAppname(String appname)
     {
         this.appname = appname;
     }
     
     public String getProcid()
     {
         return procid;
     }
     
     public void setProcid(String procid)
     {
         this.procid = procid;
     }
     
     public String getMsgid()
     {
         return msgid;
     }
     
     public void setMsgid(String msgid)
     {
         this.msgid = msgid;
     }
     
     public String getSd()
     {
         return sd;
     }
     
     public void setSd(String sd)
     {
         this.sd = sd;
     }
     
     public String getMsg()
     {
         return msg;
     }
     
     public void setMsg(String msg)
     {
         this.msg = msg;
     }
     
     public Syslog(String record)
     {
         decode(record);
     }
     
     public Syslog()
     {
         // Local use 0 (local0).
         facility = 16;
         
         // Debug.
         severity = 7;
         
         // RFC Specified.
         version = 1;
         timestamp = "-";
         hostname = "-";
         appname = "-";
         procid = "-";
         msgid = "-";
         sd = "-";
         msg = "";
         
     }
     
     public String encode()
     {
         String str = "";
         
         // Generates PRI.
        int pri = (facility << 3) + severity;
         str += "<" + pri + ">";
         
         // Generates version.
         str += version + " ";
         str += timestamp + " ";
         str += hostname + " ";
         str += appname + " ";
         str += procid + " ";
         str += msgid + " ";
         str += sd;
         
         if (!msg.isEmpty())
         {
             str += " " + msg;
         }
         
         return str;
     }
     
     @Override
     public String toString()
     {
         String str = "";
         
         str += "facility: " + facility + "\n";
         str += "severity: " + severity + "\n";
         str += "version: " + version + "\n";
         str += "timestamp: " + timestamp + "\n";
         str += "hostname: " + hostname + "\n";
         str += "appname: " + appname + "\n";
         str += "procid: " + procid + "\n";
         str += "msgid: " + msgid + "\n";
         str += "sd: " + sd + "\n";
         str += "msg: " + msg + "\n";
         
         return str;
     }
     
     private void decode(String record) throws IllegalArgumentException
     {
         int pos0 = 0;
         int pos = 0;
         
         // Validate format.
         pos = record.indexOf('>');
         if (record.charAt(0) != '<' || pos > 4)
         {
             throw new IllegalArgumentException("Malformed syslog record.");
         }
         
         // Parse Header.
         
         // Parse facility and severity.
         int pri = Integer.decode(record.substring(1, pos));
         facility = pri >> 3;
         severity = pri & 0x7;
         
         // Parse Version.
         ++pos;
         version = record.charAt(pos) - 0x30;
         
         String[] token = record.split(" +", 7);
         
         timestamp = token[1];
         hostname = token[2];
         appname = token[3];
         procid = token[4];
         msgid = token[5];
         
         // Parse SD
         if (token[6].charAt(0) == '[')
         {
             while (true)
             {
                 pos0 = token[6].indexOf(']', pos0);
                 if (pos0 == -1)
                 {
                     break;
                 }
                 
                 ++pos0;
                 
                 // Record the index.
                 if (token[6].charAt(pos0 - 2) != '\\')
                 {
                     // Make sure it's not a escaped "]".
                     pos = pos0;
                 }
             }
         }
         else
         {
             // NILVAULE, "-".
             
             pos = 1;
         }
         sd = token[6].substring(0, pos);
         
         // Parse message.
         if (pos > token[6].length())
         {
             msg = token[6].substring(pos + 1);
         }
         else
         {
             msg = "";
         }
     }
 }
