 package com.gmail.sikakura;
 
 import hudson.*;
 import hudson.cli.*;
 import hudson.model.*;
 import hudson.tasks.*;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.util.logging.*;
 
 import javax.mail.*;
 import javax.mail.Flags.*;
 import javax.mail.internet.*;
 
 import org.kohsuke.stapler.*;
 
 /**
  * @author Naoto Shikakura
  */
 public class MailCommandBuilder extends Builder {
 
     private static final Logger LOGGER = Logger.getLogger(MailCommandBuilder.class.getName());
 
     private final String address;
     private final String port;
     private final String username;
     private final String password;
 
     String to_address;
 
     @DataBoundConstructor
     public MailCommandBuilder(String address, String port, String username, String password) {
         this.address = address;
         this.port = port;
         this.username = username;
         this.password = password;
     }
 
     public String getAddress() {
         return address;
     }
 
     public String getPort() {
         return port;
     }
 
     public String getUsername() {
         return username;
     }
 
     public String getPassword() {
         return password;
     }
 
     public String getTo_address() {
         return to_address;
     }
 
     public String receive(AbstractBuild build, BuildListener listener) {
 
         StringBuffer commands = new StringBuffer();
         for (CLICommand c : CLICommand.all())
             commands.append(c.getName());
 
         Properties props = new Properties();
         Session sess = Session.getDefaultInstance(props);
         String subject = null;
 
         Store store;
         try {
             store = sess.getStore("pop3");
         } catch (NoSuchProviderException e) {
             e.printStackTrace();
             return null;
         }
 
         try {
             store.connect(address, Integer.valueOf(port), username, password);
 
             Folder rootFolder = store.getDefaultFolder();
             Folder inbox = rootFolder.getFolder("INBOX");
             inbox.open(Folder.READ_WRITE);
 
             Message[] messages = null;
             if (inbox.getMessageCount() > 10)
                 messages = inbox.getMessages(inbox.getMessageCount() - 10 + 1, inbox.getMessageCount());
             else
                 messages = inbox.getMessages();
             FetchProfile fp = new FetchProfile();
             fp.add("Subject");
             inbox.fetch(messages, fp);
             for (Message message : messages) {
                 String findStr = message.getSubject().split(" ")[0];
                 if (commands.indexOf(findStr) != -1) {
                     subject = message.getSubject();
                     InternetAddress[] addresses = (InternetAddress[]) message.getFrom();
                     saveFile(build, "tmp.address", addresses[0].getAddress());
                     if ("groovy".indexOf(findStr) != -1)
                         saveFile(build, "groovy.script", (String) message.getContent());
                     message.setFlag(Flag.DELETED, true);
                     break;
                 }
             }
             inbox.close(true);
             store.close();
         } catch (MessagingException e) {
             e.printStackTrace();
             return null;
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         return subject;
     }
 
     private void saveFile(AbstractBuild build, String filename, String contents) {
         try {
             FileOutputStream fos = new FileOutputStream(new File(build.getRootDir(), filename));
             OutputStreamWriter osw = new OutputStreamWriter(fos);
             BufferedWriter bw = new BufferedWriter(osw);
             bw.write(contents);
             bw.close();
             osw.close();
             fos.close();
         } catch (IOException ioe) {
             System.err.println(ioe.getMessage());
         } finally {
         }
     }
 
     @Override
     public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
 
         String command = receive(build, listener);
 
         if (Hudson.getInstance().getRootUrl() == null) {
             listener.getLogger().println("Please save a once System property!");
             return false;
         }
 
         if (command == null) {
             listener.getLogger().println("Don't find command mail.");
             return true;
         }
 
        if (command.compareTo("groovy") == 0) {
            command = command + " " + build.getRootDir() + "/groovy.script";
            System.out.println(command);
        }

         StringBuilder sb = new StringBuilder("-s");
         sb.append(" ");
         sb.append(Hudson.getInstance().getRootUrl());
         sb.append(" ");
         sb.append(command);
         List<String> args = Arrays.asList(sb.toString().split(" "));
 
         String url = System.getenv("JENKINS_URL");
 
         while (!args.isEmpty()) {
             String head = args.get(0);
             if (head.equals("-s") && args.size() >= 2) {
                 url = args.get(1);
                 args = args.subList(2, args.size());
                 continue;
             }
             break;
         }
 
         if (url == null) {
             return false;
         }
 
         if (args.isEmpty())
             args = Arrays.asList("help"); // default to help
 
         CLI cli = null;
         int resultInt = -1;
         try {
             cli = new CLI(new URL(url));
             // execute the command
             // Arrays.asList is not serializable --- see 6835580
             args = new ArrayList<String>(args);
             resultInt = cli.execute(args, System.in, listener.getLogger(), listener.getLogger());
             cli.close();
         } catch (MalformedURLException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (InterruptedException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } finally {
             ;
         }
 
         if (resultInt == 0)
             return true;
         else
             return false;
     }
 
     @Override
     public DescriptorImpl getDescriptor() {
         return (DescriptorImpl) super.getDescriptor();
     }
 
     @Extension
     public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
 
         public boolean isApplicable(Class<? extends AbstractProject> aClass) {
             return true;
         }
 
         public String getDisplayName() {
             return "Mail Commander";
         }
 
     }
 
 }
