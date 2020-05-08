 package com.spartansoftwareinc.globalsight.gscli;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 
 /**
  * Create a job.  By default, use all the target locales
  * indicated by the file profile (selected by name or id).  
  * Generate a job name based on the name of the first file uploaded,
  * unless one is specified on the command line.
  */
 @SuppressWarnings("static-access")
 public class CreateJobCommand extends WebServiceCommand {
 
     @Override
     protected void execute(CommandLine command, UserData userData,
             WebService webService) throws Exception {
         // Make sure we have at least one file to upload
         if (command.getArgs().length == 0) {
            usage("Must specify at least one file to import.");
         }
         
         List<File> files = getFileList(command);
         
         int count = 1;
         if (command.hasOption(REPEAT)) {
             String v = command.getOptionValue(REPEAT);
             try {
                 count = Integer.valueOf(v);
                 if (count <= 0) {
                     die(REPEAT + " option value must be >= 1");
                 }
             }
             catch (NumberFormatException e) {
                 die(REPEAT + " option requires a numeric argument");
             }
         }
         
         if (command.hasOption(FILEPROFILE) && 
             command.hasOption(FILEPROFILEID)) {
             usage("Can't specify both " + FILEPROFILE + 
                   " and " + FILEPROFILEID + " options.");
         }
         FileProfileResolver fpResolver = 
             new FileProfileResolver(webService.getFileProfiles());
         
         Map<File, List<FileProfile>> fileMap = 
                     new HashMap<File, List<FileProfile>>();
         for (File f : files) {
             fileMap.put(f, findProfiles(f, fpResolver, command));
         }
         List<String> errors = new ArrayList<String>();
         for (Map.Entry<File, List<FileProfile>> e : fileMap.entrySet()) {
             List<FileProfile> profiles = e.getValue();
             if (profiles.size() == 0) {
                 errors.add("Couldn't find a file profile for " + e.getKey());
             }
             else if (profiles.size() > 1) {
                 errors.add("Multiple file profiles available for " + 
                         e.getKey() + ": " + printProfileList(profiles));
             }
         }
         if (errors.size() > 0) {
             for (String s : errors) {
                 warn(s);
             }
             die("Unable to resolve file profiles for job.");
         }
 
         if (count > 1) {
             verbose("Creating " + count + " jobs:");
         }
         for (int i = 0; i < count; i++) {
             // Get a job name either from command line or first file
             // uploaded, then uniquify
             String baseJobName = files.get(0).getName();
             if (command.hasOption(JOBNAME)) {
                 baseJobName = command.getOptionValue(JOBNAME);
             }
             String jobName = webService.getUniqueJobName(baseJobName);
             verbose("Got unique job name: " + jobName);
             List<String> filePaths = new ArrayList<String>();
             List<FileProfile> fileProfiles = new ArrayList<FileProfile>();
             List<Collection<String>> targetLocales = new ArrayList<Collection<String>>();
             for (File f : files) {
                 FileProfile fp = fileMap.get(f).get(0);
                 fileProfiles.add(fp);
                 filePaths.add(uploadFile(f, jobName, fp, webService));
                 verbose(fp.getName() + " <-- " + f);
                 if (command.hasOption(TARGET)) {
                     targetLocales.add(Arrays.asList(command.getOptionValues(TARGET)));
                 } else {
                     targetLocales.add(fp.getTargetLocales());
                 }            
             }
             webService.createJob(jobName, filePaths, fileProfiles, targetLocales);
         }
     }
 
     List<File> getFileList(CommandLine command) throws IOException {
         // Convert all remaining arguments to files
         List<File> files = new ArrayList<File>();
         for (String path : command.getArgs()) {
             File f = new File(path);
             if (!f.exists() || f.isDirectory()) {
                 die("Not a file: " + f);
             }
             files.add(f.getCanonicalFile());
         }
         return files;
     }
 
     List<FileProfile> findProfiles(File f, FileProfileResolver fpResolver,
                                     CommandLine command) {
         if (command.hasOption(FILEPROFILE)) {
             String fpName = command.getOptionValue(FILEPROFILE);
             FileProfile fp = fpResolver.findByName(fpName);
             if (fp == null) {
                 die("No such file profile: '" + fpName + "'");
             }
             // TODO: make sure there's an extension match
             return Collections.singletonList(fp);
         }
         else if (command.hasOption(FILEPROFILEID)) {
             String fpId = command.getOptionValue(FILEPROFILEID);
             FileProfile fp = fpResolver.findById(fpId);
             if (fp == null) {
                 die("No such file profile id: '" + fpId + "'");
             }
             // TODO: make sure there's an extension match
             return Collections.singletonList(fp);
         }
         return fpResolver.findByFileExtension(f);
     }
     
     private static long MAX_SEND_SIZE = 5 * 1000 * 1024; // 5M
     
     // Returns the filepath that was sent to the server
     String uploadFile(File file, String jobName, FileProfile fileProfile,
                     WebService webService) throws Exception {
         String filePath = file.getAbsolutePath();
         // XXX This is so janky - why do we have to do this?
         filePath = filePath.substring(filePath.indexOf(File.separator) + 1);     
         verbose("Uploading " + filePath + " to job " + jobName);
         InputStream is = null;
         try {
             long bytesRemaining = file.length();
             is = new BufferedInputStream(new FileInputStream(file));
             while (bytesRemaining > 0) {
                 // Safe cast because it's bounded by MAX_SEND_SIZE
                 int size = (int)Math.min(bytesRemaining, MAX_SEND_SIZE);
                 byte[] bytes = new byte[size];
                 int count = is.read(bytes);
                 if (count <= 0) {
                     break;
                 }
                 bytesRemaining -= count;
                 verbose("Uploading chunk: " + size + " bytes");
                 webService.uploadFile(filePath, jobName, fileProfile.getId(), bytes);
             }
             verbose("Finished uploading " + filePath);
         }
         catch (IOException e) {
             throw new RuntimeException(e);
         }      
         finally {
             if (is != null) {
                 is.close();
             }
         }
         return filePath;
     }
     
     String printProfileList(List<FileProfile> profiles) {
         List<String> s = new ArrayList<String>();
         for (FileProfile fp : profiles) {
             s.add(fp.getName());
         }
         return Util.join(", ", s);
     }
     
     static final String TARGET = "targetlocale",
                         FILEPROFILE = "fileprofile",
                         FILEPROFILEID = "fileprofileid",
                         JOBNAME = "name",
                         REPEAT = "repeat";
     static final Option TARGET_OPT = OptionBuilder
         .withArgName("targetLocale")
         .hasArg()
         .withDescription("target locale code")
         .create(TARGET);
     static final Option FILEPROFILE_OPT = OptionBuilder
         .withArgName("fileProfile")
         .hasArg()
         .withDescription("file profile to use")
         .create(FILEPROFILE);
     static final Option FILEPROFILEID_OPT = OptionBuilder
         .withArgName("fileProfileId")
         .hasArg()
         .withDescription("numeric ID file profile to use")
         .create(FILEPROFILEID);
     static final Option JOBNAME_OPT = OptionBuilder
         .withArgName("jobName")
         .hasArg()
         .withDescription("job name")
         .create(JOBNAME);
     static final Option REPEAT_OPT = OptionBuilder
         .withArgName("repeat")
         .hasArg()
         .withDescription("create this job <n> times")
         .create(REPEAT);
 
     @Override
     public Options getOptions() {
         Options opts = super.getOptions();
         opts.addOption(TARGET_OPT);
         opts.addOption(FILEPROFILE_OPT);
         opts.addOption(FILEPROFILEID_OPT);
         opts.addOption(JOBNAME_OPT);
         opts.addOption(REPEAT_OPT);
         return opts;
     }
     
     @Override
     public String getDescription() {
         return "create a job in GlobalSight";
     }
 
 }
