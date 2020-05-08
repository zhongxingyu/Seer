 package framework.utils;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.tools.ant.taskdefs.optional.ssh.SSHExec;
 import org.openspaces.grid.gsm.machines.plugins.ElasticMachineProvisioningException;
 import org.testng.Assert;
 
 import com.jcraft.jsch.Channel;
 import com.jcraft.jsch.ChannelExec;
 import com.jcraft.jsch.JSch;
 import com.jcraft.jsch.JSchException;
 import com.jcraft.jsch.Session;
 import com.jcraft.jsch.UserInfo;
 
 public class SSHUtils {
     
     public static class SSHExecutor implements Runnable {
         
         JSch jsch = null;
         Session session;
         Channel channel;
         InputStream commandInputStream;
         StringBuilder sb = new StringBuilder();
         
         String username;
         String host;
         String password;
         String command;
         
         public SSHExecutor(String username, String host, String password) {
             this.username = username;
             this.host = host;
             this.password = password;
             jsch = new JSch();
         }
         
         public void open() {
             try {
                 session = jsch.getSession(username , host, 22);
                 UserInfo ui = new MyUserInfo(password);
                 session.setUserInfo(ui);
                 session.connect();
             } catch (Exception e) {
                 Assert.fail();
             }
         }
     
         public void exec() {
             try {
                 channel = session.openChannel("exec");
                 ((ChannelExec) channel).setCommand(command);
                 channel.setInputStream(null);
                 ((ChannelExec) channel).setErrStream(System.err);
                 commandInputStream = channel.getInputStream();
                 channel.connect();
             } catch (Exception e) {
                 Assert.fail();
             }
         }
         
         public void close() {
             channel.disconnect();
             session.disconnect();
         }
         
         public void openExecuteAndClose() {
             open();
             exec();
             getCommandOutput();
             close();
         }
         
         public String getCommandOutput() {
             try {
                 byte[] tmp = new byte[1024];
                 while (true) {
                     while (commandInputStream.available() > 0) {
                         int i = commandInputStream.read(tmp, 0, 1024);
                         if (i < 0) {
                             break;
                         }
                         sb.append(new String(tmp, 0, i));
                     }
                     if (channel.isClosed()) {
                         break;
                     }
                     Thread.sleep(1000);
                 }
                 return sb.toString();}
             catch (Exception e) {
                Assert.fail();
             }
             return "";
         }
         
         public void setCommand(String command) {
             this.command = command;
         }
         
         public void run() {
             try {
                 open();
                 exec();
                 byte[] tmp = new byte[1024];
                 while (true) {
                     while (commandInputStream.available() > 0) {
                         int i = commandInputStream.read(tmp, 0, 1024);
                         if (i < 0) {
                             break;
                         }
                         sb.append(new String(tmp, 0, i));
                     }
                     if (channel.isClosed()) {
                         break;
                     }
                     Thread.sleep(1000);  
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         
         
         /***
          * 
          * @return PID of running gsc/gsm/... (not a general use method)
          * @throws IOException
          * @throws InterruptedException
          */
         public int getJavaProcessID() {
             String scriptOutput = sb.toString();
             int pid = -1; 
             
             //This regular expression captures the script's pid
             String regex = "(?:Log file:.+-)([0-9]+)(?:\\.log)";
             Pattern pattern = Pattern.compile(regex);
             Matcher matcher = pattern.matcher(scriptOutput);
             if (matcher.find()) {
                 pid = Integer.parseInt(matcher.group(1));
             }
             return pid;
         }
     }
     
     public static class SSHFileCopy {
         
         private JSch jsch = null;
         private Session session;
         private Channel channel;
         private InputStream channelIn;
         private OutputStream channelOut;
         private FileInputStream fis;
 
         private String localPath;
         private String username;
         private String host;
         private String password;
         private String remotePath;
         
         public SSHFileCopy(String username, String host, String password, String localPath, String remotePath) {
             this.username = username;
             this.host = host;
             this.password = password;
             this.localPath = localPath;
             this.remotePath = remotePath;
             jsch = new JSch();
         }
         
         public void open() throws JSchException {
             session = jsch.getSession(username , host, 22);
             UserInfo ui = new MyUserInfo(password);
             session.setUserInfo(ui);
             session.connect();
         }
         
         public void copy() throws Exception {
             // exec 'scp -t rfile' remotely
             String command = "scp -p -t " + remotePath;
             channel = session.openChannel("exec");
             ((ChannelExec) channel).setCommand(command);
 
             // get I/O streams for remote scp
             channelOut = channel.getOutputStream();
             channelIn = channel.getInputStream();
 
             channel.connect();
 
             if (checkAck(channelIn) != 0) {
                 throw new SSHException();
             }
 
             // send "C0644 filesize filename", where filename should not include
             // '/'
             long filesize = (new File(localPath)).length();
             command = "C0644 " + filesize + " ";
             if (localPath.lastIndexOf('/') > 0) {
                 command += localPath.substring(localPath.lastIndexOf('/') + 1);
             } else {
                 command += localPath;
             }
             command += "\n";
             channelOut.write(command.getBytes());
             channelOut.flush();
             if (checkAck(channelIn) != 0) {
                 throw new SSHException();
             }
 
             // send a content of lfile
             fis = new FileInputStream(localPath);
             byte[] buf = new byte[1024];
             while (true) {
                 int len = fis.read(buf, 0, buf.length);
                 if (len <= 0)
                     break;
                 channelOut.write(buf, 0, len); // out.flush();
             }
             fis.close();
             fis = null;
             // send '\0'
             buf[0] = 0;
             channelOut.write(buf, 0, 1);
             channelOut.flush();
             if (checkAck(channelIn) != 0) {
                 throw new SSHException();
             }
             channelOut.close();
         }
         
         public void close() {
             channel.disconnect();
             session.disconnect();
         }
         
         public void openCopyAndClose() throws Exception {
             open();
             copy();
             close();
         }
         
         static int checkAck(InputStream in) throws IOException {
             int b = in.read();
             // b may be 0 for success,
             // 1 for error,
             // 2 for fatal error,
             // -1
             if (b == 0)
                 return b;
             if (b == -1)
                 return b;
 
             if (b == 1 || b == 2) {
                 StringBuffer sb = new StringBuffer();
                 int c;
                 do {
                     c = in.read();
                     sb.append((char) c);
                 } while (c != '\n');
                 if (b == 1) { // error
                     System.out.print(sb.toString());
                 }
                 if (b == 2) { // fatal error
                     System.out.print(sb.toString());
                 }
             }
             return b;
         }
         
     }
     
     private static class MyUserInfo implements UserInfo {
         
         private String password;
         
         MyUserInfo(String password) {
             this.password = password;
         }
         
         public String getPassword() { return password; }
         public boolean promptYesNo(String s) { return true; }
         public String getPassphrase() { return null; }
         public boolean promptPassphrase(String m) { return true; }
         public boolean promptPassword(String m) { return true; }
         public void showMessage(String m) { }
     }
  
     @SuppressWarnings("serial")
     private static class SSHException extends Exception {}
 
     
     /* 
      * Kill process on a linux machine (lab) by passing its PID. Will ignore requests on different machines 
      * (i.e: windows)
      */
     public static boolean killProcess(String ipAddress, int pid) {
         
         String labPrefix = SetupUtils.LINUX_HOST_PREFIX;
         if (!ipAddress.startsWith(labPrefix)) {
             return false;
         }
         
         long timeoutMilliseconds = 1000;
         String username = SetupUtils.USERNAME;
         String password = SetupUtils.PASSWORD;
         String command = "kill -9 " + pid;
         
         runCommand(ipAddress, timeoutMilliseconds, command, username, password);
         
         return true;
     }
     
     public static void waitForSSH(String ipAddress,
             String username, String password, long timeout, TimeUnit unit) throws ElasticMachineProvisioningException , InterruptedException{
 
         long timestamp = System.currentTimeMillis() + unit.toMillis(timeout); 
         String result = "";
         while (!result.contains("ping")) {
             try {
                 result = runCommand(ipAddress,5000,"echo ping", username, password);
             } catch (Exception e) {
                 if (System.currentTimeMillis() > timestamp) {
                     Assert.fail("Timeout connecting to SSH server",e);
                 }
             }
             LogUtils.log("SSH server is not responding, retrying");
             Thread.sleep(5000);
         }
     }
     
     public static String runCommand(String ipAddress, long timeoutMilliseconds, String command,
             String username, String password) {
         try {
             final File output = File.createTempFile("sshCommand", ".txt");
             try {
                 final SSHExec task = new SSHExec();
                 task.setOutput(output);
                 // ssh related parameters
                task.setFailonerror(false);
                 task.setCommand(command);
                 task.setHost(ipAddress);
                 task.setTrust(true);
                 task.setUsername(username);
                 task.setPassword(password);
                 task.setTimeout(timeoutMilliseconds);
                 task.execute();
                 String response = readFileAsString(output);
                 return response;
             } finally {
                 output.delete();
             }
         } catch(Exception e) {
             Assert.fail("Failed running ssh command: '" + command + "' on " + ipAddress);
         }
         return null;
     }
     
     private static String readFileAsString(File file) throws IOException {
         StringBuffer fileData = new StringBuffer(1000);
         BufferedReader reader = new BufferedReader(new FileReader(file));
         char[] buf = new char[1024];
         int numRead = 0;
         while ((numRead = reader.read(buf)) != -1) {
             String readData = String.valueOf(buf, 0, numRead);
             fileData.append(readData);
             buf = new char[1024];
         }
         reader.close();
         return fileData.toString();
     }
     
     public static String runGroovyFile(String host, long timeoutMilliseconds, String username, String password, String groovyFilePath){
 		String path = ScriptUtils.getBuildPath() + "tools/groovy/bin" ;
 		return SSHUtils.runCommand(host, timeoutMilliseconds, 
 				"cd " + path + ";./groovy " + groovyFilePath, username , password);
 	}
     
     public static void validateSSHUp(String  host, String username, String password) throws JSchException{
     	JSch jsch = new JSch();
     	Session session = jsch.getSession(username , host, 22);
         UserInfo ui = new MyUserInfo(password);
         session.setUserInfo(ui);
         session.connect();
     }
 }
