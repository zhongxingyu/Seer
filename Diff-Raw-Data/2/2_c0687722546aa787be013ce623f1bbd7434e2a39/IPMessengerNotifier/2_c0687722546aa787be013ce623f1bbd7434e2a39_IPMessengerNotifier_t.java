 package org.jenkinsci.plugins;
 
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Notifier;
 import hudson.tasks.Publisher;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 
 import net.sf.json.JSONObject;
 
 import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
 import org.jenkinsci.plugins.tokenmacro.TokenMacro;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 
 public class IPMessengerNotifier extends Notifier {
 
     private String fromHost = "";
     private final String charset = "MS932";
     private final int port = 2425;
     private final String messageTemplate;
     private final String recipientHosts;
 
     @DataBoundConstructor
     public IPMessengerNotifier(String messageTemplate, String recipientHosts) {
         this.messageTemplate = messageTemplate;
         this.recipientHosts = recipientHosts;
     }
 
     public String getRecipientHosts() {
         return recipientHosts;
     }
 
     public String getMessageTemplate() {
         return messageTemplate;
     }
 
     public BuildStepMonitor getRequiredMonitorService() {
         return BuildStepMonitor.NONE;
     }
 
     @Override
     public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
             BuildListener listener) {
 
         PrintStream logger = listener.getLogger();
        String message = "BUILD " + build.getResult().toString() + "\n";
 
         try {
             fromHost = InetAddress.getLocalHost().getHostName();
         } catch (UnknownHostException e) {
             logger.println(this.getClass().getSimpleName()
                     + ": Can't get hostname of jenkins.");
         }
         try {
             message += TokenMacro.expandAll(build, listener, messageTemplate);
             sendNooperation(logger);
             Thread.sleep(1500);
             for (String toHost : createRecipientHostList(recipientHosts)) {
                 sendMsg(message, toHost, logger);
             }
         } catch (MacroEvaluationException e) {
             logger.println(this.getClass().getSimpleName()
                     + "IPMessengerNotifier: MacroEvaluationException happened. "
                     + "Is message template correct ?");
         } catch (IOException e) {
             logger.println(this.getClass().getSimpleName()
                     + "IPMessengerNotifier: IOException happened. ");
         } catch (InterruptedException e) {
             logger.println(this.getClass().getSimpleName()
                     + "IPMessengerNotifier: InterruptedException happened. ");
         }
 
         // always return true;
         return true;
     }
 
     @Extension
     public static final class DescriptorImpl extends
             BuildStepDescriptor<Publisher> {
 
         private String jenkinsUserName;
 
         @Override
         public boolean isApplicable(Class<? extends AbstractProject> jobType) {
             return true;
         }
 
         @Override
         public String getDisplayName() {
             return "Notify by IPMessenger";
         }
 
         @Override
         public boolean configure(StaplerRequest req, JSONObject formData)
                 throws FormException {
             jenkinsUserName = formData.getString("jenkinsUserName");
             save();
             return super.configure(req, formData);
         }
 
         public String getJenkinsUserName() {
             return jenkinsUserName;
         }
 
     }
 
     @Override
     public DescriptorImpl getDescriptor() {
         return (DescriptorImpl) super.getDescriptor();
     }
 
     private ArrayList<String> createRecipientHostList(String recipientHosts) {
         ArrayList<String> result = new ArrayList<String>();
         for (String s : recipientHosts.split("\n")) {
             result.add(s.replaceAll("\\s+", ""));
         }
         return result;
     }
 
     private void sendMsg(String message, String toHost, PrintStream logger) {
         message = createTeregram(0x00000020, message);
         sendPacket(message, toHost, logger);
     }
 
     private void sendNooperation(PrintStream logger) {
         String message = createTeregram(0x00000000, null);
         sendPacket(message, "255.255.255.255", logger);
     }
 
     private String createTeregram(int command, String message) {
         String userName = getDescriptor().getJenkinsUserName();
         if (userName == null || "".equals(userName)) {
             userName = "jenkins-ci";
         }
         StringBuffer sb = new StringBuffer();
         sb.append(1);// ipmessenger protocol version
         sb.append(":");
         // packet serial number
         sb.append((int) Math.floor(Math.random() * Integer.MAX_VALUE));
         sb.append(":");
         sb.append(userName);// sender username
         sb.append(":");
         sb.append(fromHost);// sender hostname
         sb.append(":");
         sb.append(command);// command number
         sb.append(":");
         sb.append(message);
         return sb.toString();
     }
 
     private void sendPacket(String message, String toHost, PrintStream logger) {
         byte[] byteMsg = null;
         DatagramPacket packet = null;
         DatagramSocket socket = null;
         try {
             byteMsg = message.getBytes(charset);
             socket = new DatagramSocket(port);
             packet = new DatagramPacket(byteMsg, byteMsg.length,
                     InetAddress.getByName(toHost), port);
             socket.send(packet);
         } catch (UnsupportedEncodingException e) {
             logger.println(this.getClass().getSimpleName()
                     + ": UnsupportedEncodingException happened. You should change message template.");
         } catch (SocketException e) {
             logger.println(this.getClass().getSimpleName()
                     + ": SocketException happened");
         } catch (UnknownHostException e) {
             logger.println(this.getClass().getSimpleName()
                     + ": UnknownHostException: " + toHost);
         } catch (IOException e) {
             logger.println(this.getClass().getSimpleName()
                     + ": IOException happened");
         } finally {
             if (socket != null) {
                 socket.close();
             }
         }
     }
 }
