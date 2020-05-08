 /*
  Copyright (c) 2009-2013 Flavio Miguel ABREU ARAUJO (flavio.abreuaraujo@uclouvain.be)
                          Yannick GILLET (yannick.gillet@uclouvain.be)
 
 Universit catholique de Louvain, Louvain-la-Neuve, Belgium
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 
 1. Redistributions of source code must retain the above copyright
 notice, this list of conditions, and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions, and the disclaimer that follows
 these conditions in the documentation and/or other materials
 provided with the distribution.
 
 3. The names of the author may not be used to endorse or promote
 products derived from this software without specific prior written
 permission.
 
 In addition, we request (but do not require) that you include in the
 end-user documentation provided with the redistribution and/or in the
 software itself an acknowledgement equivalent to the following:
 "This product includes software developed by the
 Abinit Project (http://www.abinit.org/)."
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED.  IN NO EVENT SHALL THE JDOM AUTHORS OR THE PROJECT
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.
 
 For more information on the Abinit Project, please see
 <http://www.abinit.org/>.
  */
 
 package abinitgui;
 
 import com.jcraft.jsch.*;
 import java.io.*;
 import java.util.StringTokenizer;
 import javax.swing.JFileChooser;
 import jsshterm.XDetails;
 
 //@SuppressWarnings("unchecked")
 public class RemoteExec {
 
     private Session session;
     private InputStream in;
     private InputStream err;
     private String ruser;
     private String rhost;
     private int rport;
     private String succesMSG = null;
     private String errorMSG = null;
     private static boolean DEBUG = false;
     private MainFrame mainFrame;
     private String pass;
     private String prvkey;
     private boolean useKey;
 
     public RemoteExec(MainFrame parent, String ruser, String rhost, int rport) {
         mainFrame = parent;
         this.ruser = ruser;
         this.rhost = rhost;
         this.rport = rport;
     }
 
     public void setPrvkeyOrPwdOnly(String prvkey, String pwd, boolean useKey) {
         if (useKey) {
             this.useKey = true;
             this.pass = pwd;
             if (prvkey == null) {
                 JFileChooser chooser = new JFileChooser();
                 chooser.setDialogTitle("Choose your privatekey(ex. ~/.ssh/id_dsa)");
                 chooser.setFileHidingEnabled(false);
                 int returnVal = chooser.showOpenDialog(null);
                 String pvrK;
                 if (returnVal == JFileChooser.APPROVE_OPTION) {
                     System.out.println("You choose "
                             + chooser.getSelectedFile().getAbsolutePath() + ".");
                     this.prvkey = chooser.getSelectedFile().getAbsolutePath();
                 } else {
                     this.prvkey = null;
                 }
             } else {
                 this.prvkey = prvkey;
             }
         } else {
             this.useKey = false;
             this.pass = pwd;
         }
     }
 
     public boolean start() {
         try {
             if (mainFrame.DEBUG) {
                 JSch.setLogger(new MyLogger(mainFrame));
             }
             JSch jsch = new JSch();
 
             MyUserInfo ui = new MyUserInfo();
 
             if (this.useKey) {
                 if (prvkey == null) {
                     return false;
                 } else {
                     jsch.addIdentity(prvkey, pass);
                 }
             } else {
                 ui.setPassword(pass);
             }
 
             session = jsch.getSession(ruser, rhost, rport);
 
             session.setX11Host("127.0.0.1");
             session.setX11Port(6000);
 
             XDetails xd = new XDetails();
             String X11Cookie = null;
             try {
                 X11Cookie = jsshterm.Utils.byteArrayToHexString(
                         xd.getX11Cookie());
                 session.setX11Cookie(X11Cookie);
             } catch (Exception ex) {
                 mainFrame.printDEB("X11 Cookie: " + X11Cookie);
             }
 
             // username and password will be given via UserInfo interface.
             session.setUserInfo(ui);
             session.connect();
 
             //System.out.println("DEBUG connect RemoteExec");
             if (session.isConnected()) {
                 return true;
             } else {
                 return false;
             }
         } catch (Exception e) {
             mainFrame.printERR(e.getMessage());
             return false;
         }
     }
 
     public void stop() {
         try {
             if (session != null) {
                 session.disconnect();
             }
         } catch (Exception e) {
             mainFrame.printERR(e.getMessage());
         }
     }
 
     synchronized public RetMSG sendCommand(String CMD) {
 
         if (CMD.equals("")) {
             return new RetMSG(-2, "Empty command !", CMD);
         }
         java.util.Vector cmds = new java.util.Vector();
         cmds.removeAllElements();
 
         StringTokenizer st = new StringTokenizer(CMD);
         while (st.hasMoreTokens()) {
             cmds.addElement(st.nextToken());
         }
 
         String cmd = (String) cmds.elementAt(0);
 
         if (cmd.equals("put") || cmd.equals("PUT")) {
             if (cmds.size() == 2) {
                 String localfile = (String) cmds.elementAt(1);
                 String remotefile = ".";
                 return scpL2R(localfile, remotefile);
             } else if (cmds.size() == 3) {
                 String localfile = (String) cmds.elementAt(1);
                 String remotefile = (String) cmds.elementAt(2);
                 return scpL2R(localfile, remotefile);
             } else {
                 return new RetMSG(-2, "Bad number of parameters !", CMD);
             }
         } else if (cmd.equals("get") || cmd.equals("GET")) {
             if (cmds.size() == 2) {
                 String remotefile = (String) cmds.elementAt(1);
                 String localfile = ".";
                 return scpR2L(remotefile, localfile);
             } else if (cmds.size() == 3) {
                 String remotefile = (String) cmds.elementAt(1);
                 String localfile = (String) cmds.elementAt(2);
                 return scpR2L(remotefile, localfile);
             } else {
                 return new RetMSG(-2, "Bad number of parameters !", CMD);
             }
         } else {
             Channel channel;
             try {
                 channel = session.openChannel("exec");
                 channel.setXForwarding(true);
             } catch (JSchException e) {
                 //mainFrame.printERR(e.getMessage());
                 return new RetMSG(-1, e.toString(), CMD);
             }
             ((ChannelExec) channel).setCommand(CMD);
 
             channel.setInputStream(null);
 
             try {
                 err = ((ChannelExec) channel).getErrStream();
                 in = channel.getInputStream();
             } catch (IOException e) {
                 //mainFrame.printERR(e.getMessage());
                 return new RetMSG(-1, e.toString(), CMD);
             }
             try {
                 channel.connect();
             } catch (JSchException e) {
                 //mainFrame.printERR(e.getMessage());
                 return new RetMSG(-1, e.toString(), CMD);
             }
             byte[] tmp = new byte[1024];
 
             try {
                 succesMSG = "";
                 // TODO ceci peut boucler trs longtemps en attendant qu'une commande se termine !
                 while (true) {
                     while (true) {
                         int len = in.read(tmp, 0, tmp.length);
                         if (len <= 0) {
                             break;
                         }
                         succesMSG += new String(tmp, 0, len);
                     }
                     if (channel.isClosed()) {
                         //mainFrame.printDEB("Exit-status: " + channel.getExitStatus());
                         int retCode = channel.getExitStatus();
                         if (retCode != 0) {
                            errorMSG = "";
                            while(true) {
                                int i = err.read(tmp, 0, tmp.length);
                                if (i < 0) {
                                    break;
                                }
                                errorMSG += new String(tmp,0,i);
                             }
                            return new RetMSG(retCode, errorMSG, CMD);
                         }
                         return new RetMSG(retCode, succesMSG, CMD);
                     }
                 }
             } catch (IOException e) {
                 //mainFrame.printERR(e.getMessage());
                 return new RetMSG(-1, e.toString(), CMD);
             }
         }
     }
 
     public RetMSG scpL2R(String lfile, String rfile) {
         FileInputStream fis = null;
         try {
             int retVal = 0;
             // exec 'scp -t rfile' remotely
             String command = "scp -p -t " + rfile;
             Channel channel = session.openChannel("exec");
             ((ChannelExec) channel).setCommand(command);
 
             // get I/O streams for remote scp
             OutputStream out = channel.getOutputStream();
             in = channel.getInputStream();
 
             channel.connect();
 
             long t1 = System.nanoTime();
 
             retVal = checkAck(in);
             if (retVal != 0) {
                 return new RetMSG(retVal, errorMSG, "scp " + lfile + " " + rfile);
             }
             // send "C0644 filesize filename", where filename should not include '/'
             long filesize = (new File(lfile)).length();
             command = "C0644 " + filesize + " ";
             if (lfile.lastIndexOf('/') > 0) {
                 command += lfile.substring(lfile.lastIndexOf('/') + 1);
             } else {
                 command += lfile;
             }
             command += "\n";
             out.write(command.getBytes());
             out.flush();
 
             retVal = checkAck(in);
             if (retVal != 0) {
                 return new RetMSG(retVal, errorMSG, "scp " + lfile + " " + rfile);
             }
 
             // send a content of lfile
             fis = new FileInputStream(lfile);
             byte[] buf = new byte[1024];
             while (true) {
                 int len = fis.read(buf, 0, buf.length);
                 if (len <= 0) {
                     break;
                 }
                 out.write(buf, 0, len); //out.flush();
             }
             fis.close();
             fis = null;
             // send '\0'
             buf[0] = 0;
             out.write(buf, 0, 1);
             out.flush();
 
             retVal = checkAck(in);
             if (retVal != 0) {
                 return new RetMSG(retVal, errorMSG, "scp " + lfile + " " + rfile);
             }
 
             out.close();
 
             long t2 = System.nanoTime();
             mainFrame.printDEB("File " + lfile + " was uploaded in " + Utils.formatTime(((double) (t2 - t1)) / 1000000000.0));
 
             channel.disconnect();
 
             return new RetMSG(RetMSG.SUCCES, "", "scp " + lfile + " " + rfile);
         } catch (Exception e) {
             //mainFrame.printERR(e.getMessage());
             try {
                 if (fis != null) {
                     fis.close();
                 }
             } catch (Exception ee) {
                 mainFrame.printERR(e.getMessage());
             }
             return new RetMSG(-1, e.toString(), "scp " + lfile + " " + rfile);
         }
     }
 
     public RetMSG scpR2L(String rfile, String lfile) {
         FileOutputStream fos = null;
         try {
             int retVal = 0;
             // exec 'scp -f rfile' remotely
             String command = "scp -f " + rfile;
             Channel channel = session.openChannel("exec");
             ((ChannelExec) channel).setCommand(command);
 
             String prefix = null;
             if (new File(lfile).isDirectory()) {
                 prefix = lfile + File.separator;
             }
 
             // get I/O streams for remote scp
             OutputStream out = channel.getOutputStream();
             in = channel.getInputStream();
 
             channel.connect();
 
             long t1 = System.nanoTime();
 
             byte[] buf = new byte[1024];
 
             // send '\0'
             buf[0] = 0;
             out.write(buf, 0, 1);
             out.flush();
 
             // Variable servant à tester si un fichier a été reçu !
             int test = 0;
 
             while (true) {
                 int c = checkAck(in);
 
                 if (c != 'C') {
                     break;
                 } else {
                     test++;
                 }
 
                 // read '0644 '
                 in.read(buf, 0, 5);
 
                 long filesize = 0L;
                 while (true) {
                     if (in.read(buf, 0, 1) < 0) {
                         // error
                         return new RetMSG(-2, "Reading filesize", "scp " + rfile + " " + lfile);
                     }
                     if (buf[0] == ' ') {
                         break;
                     }
                     filesize = filesize * 10L + (long) (buf[0] - '0');
                 }
 
                 String file = null;
                 for (int i = 0;; i++) {
                     in.read(buf, i, 1);
                     if (buf[i] == (byte) 0x0a) {
                         file = new String(buf, 0, i);
                         break;
                     }
                 }
 
                 //mainFrame.printDEB("filesize="+filesize+", file="+file);
 
                 // send '\0'
                 buf[0] = 0;
                 out.write(buf, 0, 1);
                 out.flush();
 
                 // write in local file lfile
                 fos = new FileOutputStream(prefix == null ? lfile : prefix + file);
                 int foo;
                 while (true) {
                     if (buf.length < filesize) {
                         foo = buf.length;
                     } else {
                         foo = (int) filesize;
                     }
                     foo = in.read(buf, 0, foo);
                     if (foo < 0) {
                         // error
                         return new RetMSG(-2, "Error: reading incomming stream", "scp " + rfile + " " + lfile);
                     }
                     fos.write(buf, 0, foo);
                     filesize -= foo;
                     if (filesize == 0L) {
                         break;
                     }
                 }
                 fos.close();
                 fos = null;
 
                 retVal = checkAck(in);
                 if (retVal != 0) {
                     return new RetMSG(retVal, errorMSG, "scp " + rfile + " " + lfile);
                 }
 
                 // send '\0'
                 buf[0] = 0;
                 out.write(buf, 0, 1);
                 out.flush();
             }
 
             if (test != 0) {
                 long t2 = System.nanoTime();
                 mainFrame.printDEB("File " + lfile + " was downloaded in " + Utils.formatTime(((double) (t2 - t1)) / 1000000000.0));
 
                 channel.disconnect();
 
                 return new RetMSG(RetMSG.SUCCES, "", "scp " + rfile + " " + lfile);
             } else {
                 return new RetMSG(-2, "File " + rfile + " doesn't exist", "scp " + rfile + " " + lfile);
             }
         } catch (Exception e) {
             //printERR(e.getMessage());
             try {
                 if (fos != null) {
                     fos.close();
                 }
             } catch (Exception ee) {
                 mainFrame.printERR(e.getMessage());
             }
             return new RetMSG(-1, e.toString(), "scp " + rfile + " " + lfile);
         }
     }
 
     int checkAck(InputStream in) throws IOException {
         int b = in.read();
         // b may be 0 for success,
         //          1 for error,
         //          2 for fatal error,
         //          -1
         if (b == 0) {
             return b;
         }
         if (b == -1) {
             return b;
         }
 
         if (b == 1 || b == 2) {
             StringBuffer sb = new StringBuffer();
             int c;
             do {
                 c = in.read();
                 sb.append((char) c);
             } while (c != '\n');
             if (b == 1) { // error
                 errorMSG = sb.toString();
                 //printERR(sb.toString());
             }
             if (b == 2) { // fatal error
                 errorMSG = sb.toString();
                 //printERR(sb.toString());
             }
         }
         return b;
     }
 }
