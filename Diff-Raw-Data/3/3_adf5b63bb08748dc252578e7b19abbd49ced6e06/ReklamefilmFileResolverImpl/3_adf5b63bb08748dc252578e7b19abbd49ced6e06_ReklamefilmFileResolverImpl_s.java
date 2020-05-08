 package dk.statsbiblioteket.broadcasttranscoder.reklamefilm;
 
 import dk.statsbiblioteket.broadcasttranscoder.cli.Context;
 import dk.statsbiblioteket.broadcasttranscoder.processors.ProcessorException;
 import dk.statsbiblioteket.broadcasttranscoder.util.CentralWebserviceFactory;
 import dk.statsbiblioteket.broadcasttranscoder.util.ExternalJobRunner;
 import dk.statsbiblioteket.broadcasttranscoder.util.ExternalProcessTimedOutException;
 import dk.statsbiblioteket.doms.central.CentralWebservice;
 import dk.statsbiblioteket.doms.central.DatastreamProfile;
 import dk.statsbiblioteket.doms.central.ObjectProfile;
 import dk.statsbiblioteket.doms.central.Relation;
 import dk.statsbiblioteket.util.Files;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.filefilter.IOFileFilter;
 import org.apache.commons.io.filefilter.NameFileFilter;
 import org.apache.commons.io.filefilter.TrueFileFilter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.Collection;
 import java.util.List;
 
 /**
  *
  */
 public class ReklamefilmFileResolverImpl implements ReklamefilmFileResolver {
 
     private static Logger logger = LoggerFactory.getLogger(ReklamefilmFileResolverImpl.class);
     private static final String HAS_FILE_RELATION = "http://doms.statsbiblioteket.dk/relations/default/0/1/#hasFile";
 
     private Context context;
 
     public ReklamefilmFileResolverImpl(Context context) {
         this.context = context;
     }
 
     @Override
     public File resolverPidToLocalFile(String domsReklamePid) {
         logger.debug("Resolving pid " + domsReklamePid + " to a file.");
         CentralWebservice domsApi = CentralWebserviceFactory.getServiceInstance(context);
         String fileObjectPid = null;
         List<Relation> relations = null;
         try {
             relations = domsApi.getRelations(domsReklamePid);
         } catch (Exception e) {
              throw new RuntimeException("", e);
         }
         for (Relation relation: relations) {
             if (relation.getPredicate().equals(HAS_FILE_RELATION)) {
                 fileObjectPid = relation.getObject();
             }
         }
         ObjectProfile fileProfile = null;
         try {
             fileProfile = domsApi.getObjectProfile(fileObjectPid);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
         String locationUrl = null;
         for (DatastreamProfile datastreamProfile: fileProfile.getDatastreams()) {
             if (datastreamProfile.getId().equals("CONTENTS")) {
                 locationUrl = datastreamProfile.getUrl();
             }
         }
         URL url = null;
         try {
             url = new URL(locationUrl);
         } catch (MalformedURLException e) {
             throw new RuntimeException(e);
         }
         String[] pathElements = url.getPath().split(File.separator);
         String filename = pathElements[pathElements.length -1];
         filename = URLDecoder.decode(filename);
         String filenameEscaped = filename.replaceAll("\\?", "\\?").replaceAll("\\*", "\\*").replaceAll("\\[","\\[").replaceAll("\\]","\\]");
         for (String rootDir: context.getReklamefileRootDirectories()) {
             String cmd = "bash -c \"find " + rootDir + " -name " + "'" + filenameEscaped + "'\"";
             ExternalJobRunner runner = null;
             try {
                 runner = new ExternalJobRunner(30000L, "find", rootDir, "-name", filenameEscaped);
                 String output = runner.getOutput();
                 if (output != null && !output.trim().equals("")) {
                    final File file = new File(output.trim());
                     logger.info("Resolved " + domsReklamePid + " to " + file.getAbsolutePath());
                     return file;
                 }
             } catch (Exception e) {
                 logger.warn("Attempt to find file with find command '" + runner.toString() + " / " + cmd + "' failed.", e);
             }
         }
 
         for (String rootDir: context.getReklamefileRootDirectories()) {
             logger.debug("Looking for " + filename + " in " + rootDir);
             IOFileFilter nameFilter = new NameFileFilter(filename);
             Collection<File> files = FileUtils.listFiles(new File(rootDir), nameFilter, TrueFileFilter.INSTANCE);
             if (!files.isEmpty()) {
                 if (files.size() == 1) {
                     File file = files.iterator().next();
                     logger.info("Resolved " + domsReklamePid + " to " + file.getAbsolutePath());
                     return file;
                 } else {
                     throw new RuntimeException("Found more than one matching file for " + domsReklamePid + " / " + filename);
                 }
             }
         }
         logger.warn("Could not resolve " + domsReklamePid + " / " + filename);
         return null;
     }
 }
