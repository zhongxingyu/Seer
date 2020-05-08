 /**
    This file is part of GoldenGate Project (named also GoldenGate or GG).
 
    Copyright 2009, Frederic Bregier, and individual contributors by the @author
    tags. See the COPYRIGHT.txt in the distribution for a full listing of
    individual contributors.
 
    All GoldenGate Project is free software: you can redistribute it and/or 
    modify it under the terms of the GNU General Public License as published 
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
 
    GoldenGate is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with GoldenGate .  If not, see <http://www.gnu.org/licenses/>.
  */
 package openr66.r66gui;
 
 import goldengate.common.database.exception.GoldenGateDatabaseException;
import goldengate.common.database.exception.GoldenGateDatabaseNoConnectionException;
import goldengate.common.database.exception.GoldenGateDatabaseSqlException;
 import goldengate.common.logging.GgInternalLogger;
 import goldengate.common.logging.GgInternalLoggerFactory;
 import goldengate.common.logging.GgSlf4JLoggerFactory;
 
 import javax.swing.JEditorPane;
 import javax.swing.JProgressBar;
 
 import openr66.client.Message;
 import openr66.configuration.FileBasedConfiguration;
 import openr66.context.ErrorCode;
 import openr66.context.R66Result;
 import openr66.database.DbConstant;
 import openr66.database.data.DbHostAuth;
 import openr66.database.data.DbRule;
 import openr66.protocol.configuration.Configuration;
 import openr66.protocol.localhandler.packet.TestPacket;
 import openr66.protocol.localhandler.packet.ValidPacket;
 import openr66.protocol.networkhandler.NetworkTransaction;
 import openr66.protocol.utils.ChannelUtils;
 import openr66.protocol.utils.R66Future;
 
 import org.jboss.netty.logging.InternalLoggerFactory;
 import org.slf4j.LoggerFactory;
 
 import ch.qos.logback.classic.Level;
 import ch.qos.logback.classic.Logger;
 
 /**
  * @author Frederic Bregier
  *
  */
 public class R66Environment {
     /**
      * Internal Logger
      */
     static protected volatile GgInternalLogger logger;
 
 
     public String ruleId = null;
     public String hostId = null;
     public String information = null;
     public String filePath = null;
     public boolean isMD5 = false;
     public boolean isInRequest = false;
     public boolean isClientSending = false;
     public NetworkTransaction networkTransaction = null;
     public String GuiResultat;
     
     public void initialize(String []args) {
         InternalLoggerFactory.setDefaultFactory(new GgSlf4JLoggerFactory(null));
         if (logger == null) {
             logger = GgInternalLoggerFactory.getLogger(R66ClientGui.class);
         }
         if (args.length < 1) {
             System.err.println("Need client with no database support configuration file as argument");
             System.exit(2);
         }
         if (! FileBasedConfiguration
                 .setClientConfigurationFromXml(Configuration.configuration, args[0])) {
             logger
                     .error("Needs a correct configuration file as first argument");
             if (DbConstant.admin != null && DbConstant.admin.isConnected) {
                 DbConstant.admin.close();
             }
             ChannelUtils.stopLogger();
             System.exit(2);
         }
         Configuration.configuration.pipelineInit();
         networkTransaction = new NetworkTransaction();
     }
     
     public void exit() {
         if (networkTransaction != null) {
             networkTransaction.closeAll();
             networkTransaction = null;
         }
         //System.exit(0);
     }
 
     public void debug(boolean isDebug) {
         Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
         if (isDebug) {
             logger.setLevel(Level.DEBUG);
         } else {
             logger.setLevel(Level.WARN);
         }
     }
     public boolean checkConnection() {
         R66Future result = new R66Future(true);
         TestPacket packet = new TestPacket("MSG", "TestConnection", 100);
         Message transaction = new Message(networkTransaction, result,
                 hostId, packet);
         transaction.run();
         result.awaitUninterruptibly(30000);
         if (result.isSuccess()) {
             R66Result r66result = result.getResult();
             ValidPacket info = (ValidPacket) r66result.other;
             GuiResultat = "<html>Test Message    SUCCESS<br>    " +
                     info.getSheader();
         } else {
             GuiResultat = "<html>Test Message    FAILURE<br>    " +
                     result.getResult().toString();
         }
         return result.isSuccess();
     }
 
     public boolean startsTransfer(JProgressBar progressBar, JEditorPane textFieldStatus) {
         long time1 = System.currentTimeMillis();
         R66Future future = new R66Future(true);
         ProgressDirectTransfer transaction = new ProgressDirectTransfer(future, hostId,
                 filePath, ruleId, information, isMD5, Configuration.configuration.BLOCKSIZE, 
                 DbConstant.ILLEGALVALUE, networkTransaction, 500,
                 progressBar, textFieldStatus);
         logger.debug("Launch transfer: "+hostId+":"+ruleId+":"+filePath);
         transaction.run();
         future.awaitUninterruptibly();
         progressBar.setIndeterminate(true);
         progressBar.setValue(0);
         progressBar.setVisible(false);
         long time2 = System.currentTimeMillis();
         long delay = time2 - time1;
         R66Result result = future.getResult();
         if (future.isSuccess()) {
             if (result.runner.getErrorInfo() == ErrorCode.Warning) {
                 GuiResultat = "<html>WARNED<br>    " +
                         result.runner.toShortNoHtmlString("<br>") +
                         "<br>    REMOTE: " +
                         hostId +
                         (result.file != null? result.file.toString() +
                                 "" : "no file") + "    delay: " +
                         delay;
             } else {
                 GuiResultat = "<html>SUCCESS<br>    " +
                     result.runner.toShortNoHtmlString("<br>") +
                     "<br>    REMOTE: " +
                     hostId +
                     (result.file != null? result.file.toString() +
                             "" : "no file") + "    delay: " +
                     delay;
             }
         } else {
             if (result == null || result.runner == null) {
                 GuiResultat = "<html>Transfer in FAILURE with no Id"+
                     "<br>    REMOTE: " +
                     hostId + "     "+ future
                         .getCause().getMessage();
             } else if (result.runner.getErrorInfo() == ErrorCode.Warning) {
                 GuiResultat = "<html>Transfer is WARNED<br>    " +
                     result.runner.toShortNoHtmlString("<br>") +
                     "<br>    REMOTE: " +
                     hostId +"    "+ future.getCause().getMessage();
             } else {
                 GuiResultat = "<html>Transfer in FAILURE<br>    " +
                     result.runner.toShortNoHtmlString("<br>")+
                     "<br>    REMOTE: " +
                     hostId +"    "+ future.getCause().getMessage();
             }
         }
         return future.isSuccess();
     }
     public static String [] getHostIds() {
         String []results = null;
         DbHostAuth[] dbHostAuths;
         try {
             dbHostAuths = DbHostAuth.getAllHosts(null);
        } catch (GoldenGateDatabaseNoConnectionException e) {
             results = new String[1];
             results[0] = "NoHostFound";
             return results;
        } catch (GoldenGateDatabaseSqlException e) {
             results = new String[1];
             results[0] = "NoHostFound";
             return results;
         }
         if (dbHostAuths.length == 0) {
             results = new String[1];
             results[0] = "NoHostFound";
             return results;
         }
         results = new String[dbHostAuths.length];
         for (int i = 0; i < dbHostAuths.length; i++) {
             results[i] = dbHostAuths[i].getHostid();
         }
         return results;
     }
     public static String [] getRules() {
         String []results = null;
         DbRule[] dbRules;
         try {
             dbRules = DbRule.getAllRules(null);
        } catch (GoldenGateDatabaseNoConnectionException e) {
             results = new String[1];
             results[0] = "NoRuleFound";
             return results;
        } catch (GoldenGateDatabaseSqlException e) {
             results = new String[1];
             results[0] = "NoRuleFound";
             return results;
         }
         if (dbRules.length == 0) {
             results = new String[1];
             results[0] = "NoRuleFound";
             return results;
         }
         results = new String[dbRules.length];
         for (int i = 0; i < dbRules.length; i++) {
             results[i] = dbRules[i].idRule;
         }
         return results;
     }
 
     public static String getHost(String id) {
         DbHostAuth host = null;
         try {
             host = new DbHostAuth(null, id);
         } catch (GoldenGateDatabaseException e) {
         }
         if (host != null) {
             String hosthtml = "<table border=1 cellpadding=0 cellspacing=0 style=border-collapse: collapse bordercolor=#111111 width=100% id=AutoNumber1>" +
             		"<tr><td width=13% align=center><b>Host ID</b></td><td width=13% align=center><b>Address</b></td>" +
             		"<td width=13% align=center><b>Port</b></td><td width=5% align=center><b>SSL</b></td>" +
             		"<td width=13% align=center><b>HostKey</b></td><td width=5% align=center><b>Admin Role</b></td>" +
             		"<td width=5% align=center><b>IsClient</b></td></tr>" +
             		"<tr><td width=13% align=center>XXXHOSTXXX</td>"+
                 "<td width=13% align=center>XXXADDRXXX</td>"+
                 "<td width=13% align=center>XXXPORTXXX</td>"+
                 "<td width=5% align=center><input type=checkbox name=ssl value=on XXXSSLXXX disabled readonly></td>"+
                 "<td width=13% align=center>XXXKEYXXX</td>"+
                 "<td width=5% align=center><input type=checkbox name=admin value=on XXXADMXXX disabled readonly></td>"+
                 "<td width=5% align=center><input type=checkbox name=isclient value=on XXXISCXXX disabled readonly></td></tr></table>";
             return host.toSpecializedHtml(null, hosthtml, true);
         }
         return "HostId: "+id;
     }
     public static String getRule(String id) {
         DbRule rule = null;
         try {
             rule = new DbRule(null, id);
         } catch (GoldenGateDatabaseException e) {
         }
         if (rule != null) {
             String rulehtml = "<table border=1 cellpadding=0 cellspacing=0 style=border-collapse: collapse bordercolor=#111111 width=100% id=AutoNumber1>" +
                         "<tr><td width=6% align=center><b>Rule Id</b></td><td width=6% align=center><b>Mode</b></td>" +
             		"<td width=6% align=center><b>Host Ids</b></td><td width=6% align=center><b>RecvPath</b></td>" +
             		"<td width=6% align=center><b>SendPath</b></td><td width=7% align=center><b>ArchivePath</b></td>" +
             		"<td width=7% align=center><b>WorkPath</b></td></tr><tr>" +
             		"<td width=6% align=center>XXXRULEXXX</td>" +
             		"<td width=6% align=center><input type=radio value=send name=mode XXXSENDXXX disabled>SEND" +
             		"<input type=radio name=mode value=recv XXXRECVXXX disabled readonly>RECV<br>" +
             		"<input type=radio name=mode value=sendmd5 XXXSENDMXXX disabled readonly>SENDMD5" +
             		"<input type=radio name=mode value=recvmd5 XXXRECVMXXX disabled readonly>RECVMD5<br>" +
             		"<input type=radio value=sendth name=mode XXXSENDTXXX disabled readonly>SENDTHROUGH<br>" +
             		"<input type=radio name=mode value=recvth XXXRECVTXXX disabled readonly>RECVTHROUGH<br>" +
             		"<input type=radio name=mode value=sendthmd5 XXXSENDMTXXX disabled readonly>SENDMD5THROUGH<br>" +
             		"<input type=radio name=mode value=recvthmd5 XXXRECVMTXXX disabled readonly>RECVMD5THROUGH</td>" +
             		"<td width=6% align=center><PRE>XXXIDSXXX</PRE></td>" +
             		"<td width=6% align=center><PRE>XXXRPXXX</PRE></td>" +
             		"<td width=6% align=center><PRE>XXXSPXXX</PRE></td>" +
             		"<td width=7% align=center><PRE>XXXAPXXX</PRE></td>" +
             		"<td width=7% align=center><PRE>XXXWPXXX</PRE></td></tr><tr>" +
             		"<td width=7% align=center><b>Recv Pre</b></td><td width=7% align=center><b>Recv Post</b></td>" +
             		"<td width=7% align=center><b>Recv Error</b></td><td width=7% align=center><b>Send Pre</b></td>" +
             		"<td width=7% align=center><b>Send Post</b></td><td width=7% align=center><b>Send Error</b></td>" +
             		"<td width=8%>&nbsp;</td></tr><tr>" +
             		"<td width=7%><PRE>XXXRPTXXX</PRE></td>" +
             		"<td width=7%><PRE>XXXRSTXXX</PRE></td>" +
             		"<td width=7%><PRE>XXXRETXXX</PRE></td>" +
             		"<td width=7%><PRE>XXXSPTXXX</PRE></td>" +
             		"<td width=8%><PRE>XXXSSTXXX</PRE></td>" +
             		"<td width=8%><PRE>XXXSETXXX</PRE></td>" +
             		"</tr></table>";
             return rule.toSpecializedHtml(null, rulehtml);
         }
         return "RuleId: "+id;
     }
     public void about() {
         GuiResultat = "<HTML><P ALIGN=CENTER><FONT SIZE=5 STYLE=\"font-size: 22pt\"><SPAN>R66 Client GUI Version: "+Version.ID+"</SPAN></FONT></P>"+
             "<P ALIGN=CENTER><FONT SIZE=4 STYLE=\"font-size: 16pt\"><SPAN>This graphical user interface is intend to provide an easy way to use R66 for:</SPAN></FONT></P>"+
             "<UL><LI><P ALIGN=LEFT><FONT SIZE=4 STYLE=\"font-size: 16pt\"><SPAN>Testing new Rules, Hosts or connectivity</SPAN></FONT></P>"+
             "<LI><P ALIGN=LEFT<FONT SIZE=4 STYLE=\"font-size: 16pt\"><SPAN>Exchanging files between a PC and a R66 Server</SPAN></FONT></P>"+
             "<LI><P ALIGN=LEFT<FONT SIZE=4 STYLE=\"font-size: 16pt\"><SPAN>Provide an example on how to use the R66 API in an application</SPAN></FONT></P>"+
             "</UL>";
     }
 }
