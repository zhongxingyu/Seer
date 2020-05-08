 package com.deluan.jenkins.plugins.rtc;
 
 import com.deluan.jenkins.plugins.rtc.changelog.JazzChangeSet;
 import hudson.AbortException;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.Launcher.ProcStarter;
 import hudson.model.Computer;
 import hudson.model.TaskListener;
 import hudson.util.ArgumentListBuilder;
 import hudson.util.ForkOutputStream;
 import org.apache.commons.lang.StringUtils;
 import org.kohsuke.stapler.framework.io.WriterOutputStream;
 
 import java.io.*;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Encapsulates the invocation of RTC's SCM Command Line Interface, "scm".
  *
  * @author Deluan Quintao
  */
 public class JazzClient {
     protected static final Logger logger = Logger.getLogger(JazzClient.class.getName());
 
     private static final int TIMEOUT = 60 * 5; // in seconds
     private static final String DATE_FORMAT = "yyyy-MM-dd-HH:mm:ss";
     private static final String CONTRIBUTOR_FORMAT = "|{name}|{email}|";
     private final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
 
 
     private final ArgumentListBuilder base;
     private final Launcher launcher;
     private final TaskListener listener;
     private String repositoryLocation;
     private String workspaceName;
     private String streamName;
     private String username;
     private String password;
     private FilePath jobWorkspace;
 
 
     public JazzClient(Launcher launcher, TaskListener listener, FilePath jobWorkspace, String jazzExecutable,
                       String user, String password, String repositoryLocation,
                       String streamName, String workspaceName) {
         base = new ArgumentListBuilder(jazzExecutable);
         this.launcher = launcher;
         this.listener = listener;
         this.username = user;
         this.password = password;
         this.repositoryLocation = repositoryLocation;
         this.streamName = streamName;
         this.workspaceName = workspaceName;
         this.jobWorkspace = jobWorkspace;
     }
 
     private ArgumentListBuilder addAuthInfo(ArgumentListBuilder args) {
         if (StringUtils.isNotBlank(username)) {
             args.add("-u", username);
         }
         if (StringUtils.isNotBlank(password)) {
             args.add("-P", password);
         }
 
         return args;
     }
 
     public boolean hasChanges() throws IOException, InterruptedException {
         Map<String, JazzChangeSet> changes = compare();
 
         return !changes.isEmpty();
     }
 
     public boolean load() throws IOException, InterruptedException {
         ArgumentListBuilder args = new ArgumentListBuilder();
         args.add("load", workspaceName);
         addAuthInfo(args);
         args.add("-r", repositoryLocation);
         args.add("-d");
         args.add(jobWorkspace);
         args.add("-f");
 
         logger.log(Level.FINER, args.toStringWithQuote());
 
         return (joinWithPossibleTimeout(run(args), true, listener) == 0);
     }
 
     public boolean isLoaded() throws IOException, InterruptedException {
         ArgumentListBuilder args = new ArgumentListBuilder();
         args.add("history");
         addAuthInfo(args);
         args.add("-m", "1");
         args.add("-d");
         args.add(jobWorkspace);
 
         logger.log(Level.FINER, args.toStringWithQuote());
 
         return (joinWithPossibleTimeout(run(args), true, listener) == 0);
     }
 
     public boolean accept() throws IOException, InterruptedException {
         return accept(null);
     }
 
     public boolean accept(Collection<JazzChangeSet> changes) throws IOException, InterruptedException {
         ArgumentListBuilder args = new ArgumentListBuilder();
         args.add("accept");
         addAuthInfo(args);
         args.add("-d");
         args.add(jobWorkspace);
         args.add("--flow-components", "-o", "-v");
 
         if (changes != null && !changes.isEmpty()) {
             args.add("-c");
             for (JazzChangeSet changeSet : changes) {
                 args.add(changeSet.getRev());
             }
         }
 
         logger.log(Level.FINER, args.toStringWithQuote());
 
         return (joinWithPossibleTimeout(run(args), true, listener) == 0);
     }
 
     public List<JazzChangeSet> getChanges() throws IOException, InterruptedException {
         Map<String, JazzChangeSet> compareCmdResults = compare();
 
         if (!compareCmdResults.isEmpty()) {
             Map<String, JazzChangeSet> listCmdResults = list(compareCmdResults.keySet());
 
             for (Map.Entry<String, JazzChangeSet> entry : compareCmdResults.entrySet()) {
                 JazzChangeSet changeSet1 = entry.getValue();
                 JazzChangeSet changeSet2 = listCmdResults.get(entry.getKey());
                 changeSet1.copyItemsFrom(changeSet2);
             }
         }
 
         return new ArrayList<JazzChangeSet>(compareCmdResults.values());
     }
 
     private Map<String, JazzChangeSet> compare() throws IOException, InterruptedException {
         Map<String, JazzChangeSet> result = new HashMap<String, JazzChangeSet>();
 
         ArgumentListBuilder args = new ArgumentListBuilder();
         args.add("compare");
         args.add("ws", workspaceName);
         args.add("stream", streamName);
         addAuthInfo(args);
         args.add("-r", repositoryLocation);
         args.add("-I", "s");
         args.add("-C", '"' + CONTRIBUTOR_FORMAT + '"');
         args.add("-D", "\"|" + DATE_FORMAT + "|\"");
 
         logger.log(Level.FINER, args.toStringWithQuote());
 
         BufferedReader in = new BufferedReader(new InputStreamReader(
                 new ByteArrayInputStream(popen(args).toByteArray())));
         try {
             String line;
             while ((line = in.readLine()) != null) {
                 JazzChangeSet changeSet = new JazzChangeSet();
                 System.out.println(line);
                 String[] parts = line.split("\\|");
                 String rev = parts[0].trim().substring(1);
                 rev = rev.substring(0, rev.length() - 1);
                 changeSet.setRev(rev);
                 changeSet.setUser(parts[1].trim());
                 changeSet.setEmail(parts[2].trim());
                 changeSet.setMsg(parts[3].trim());
                 try {
                     changeSet.setDate(sdf.parse(parts[4].trim()));
                 } catch (ParseException e) {
                     logger.log(Level.WARNING, "Error parsing date '" + parts[4].trim() + "' for revision (" + rev + ")");
                 }
                 result.put(rev, changeSet);
             }
         } finally {
             in.close();
         }
 
         return result;
     }
 
     private Map<String, JazzChangeSet> list(Collection<String> changeSets) throws IOException, InterruptedException {
         ArgumentListBuilder args = new ArgumentListBuilder();
         args.add("list");
         args.add("changesets");
         addAuthInfo(args);
         args.add("-d");
         args.add(jobWorkspace);
         for (String changeSet : changeSets) {
             args.add(changeSet);
         }
 
         logger.log(Level.FINER, args.toStringWithQuote());
 
         Map<String, JazzChangeSet> result = new HashMap<String, JazzChangeSet>();
 
         BufferedReader in = new BufferedReader(new InputStreamReader(
                 new ByteArrayInputStream(popen(args).toByteArray())));
 
         try {
             String line;
             JazzChangeSet changeSet = null;
             Pattern startChangesetPattern = Pattern.compile("^\\s{2}\\((\\d+)\\)\\s*---[$]\\s*(\\D*)\\s+(.*)$");
             Pattern filePattern = Pattern.compile("^\\s{6}(.{5})\\s(\\S*)\\s+(.*)$");
             Pattern workItemPattern = Pattern.compile("^\\s{6}\\((\\d+)\\)\\s+(.*)$");
             Matcher matcher;
 
             while ((line = in.readLine()) != null) {
 
                 if ((matcher = startChangesetPattern.matcher(line)).matches()) {
                     if (changeSet != null) {
                         result.put(changeSet.getRev(), changeSet);
                     }
                     changeSet = new JazzChangeSet();
                     changeSet.setRev(matcher.group(1));
                 } else if ((matcher = filePattern.matcher(line)).matches()) {
                     assert changeSet != null;
                    String action = "edit";
                     String path = matcher.group(3).replaceAll("\\\\", "/").trim();
                    String flag = matcher.group(1).substring(2);
                     if ("a".equals(flag)) {
                        action = "added";
                     } else if ("d".equals(flag)) {
                        action = "deleted";
                     }
                     changeSet.addItem(path, action);
                 } else if ((matcher = workItemPattern.matcher(line)).matches()) {
                     assert changeSet != null;
                     changeSet.addWorkItem(matcher.group(2));
                 }
             }
 
             if (changeSet != null) {
                 result.put(changeSet.getRev(), changeSet);
             }
         } finally {
             in.close();
         }
 
         return result;
     }
 
 
     private ArgumentListBuilder seed() {
         return base.clone();
     }
 
     private ProcStarter l(ArgumentListBuilder args) {
         // set the default stdout
         return launcher.launch().cmds(args).stdout(listener);
     }
 
     private ProcStarter run(ArgumentListBuilder args) {
         return l(seed().add(args.toCommandArray()));
     }
 
     private int joinWithPossibleTimeout(ProcStarter proc, boolean useTimeout, final TaskListener listener) throws IOException, InterruptedException {
         return useTimeout ? proc.start().joinWithTimeout(TIMEOUT, TimeUnit.SECONDS, listener) : proc.join();
     }
 
     /**
      * Runs the command and captures the output.
      */
     private ByteArrayOutputStream popen(ArgumentListBuilder args)
             throws IOException, InterruptedException {
 
         // scm produces text in the platform default encoding, so we need to convert it back to UTF-8
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         WriterOutputStream o = new WriterOutputStream(new OutputStreamWriter(baos, "UTF-8"),
                 Computer.currentComputer().getDefaultCharset());
 
         PrintStream output = listener.getLogger();
         ForkOutputStream fos = new ForkOutputStream(o, output);
         if (joinWithPossibleTimeout(run(args).stdout(fos), true, listener) == 0) {
             o.flush();
             return baos;
         } else {
             listener.error("Failed to run " + args.toStringWithQuote());
             throw new AbortException();
         }
     }
 
 
 }
