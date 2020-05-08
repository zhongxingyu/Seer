 package dk.statsbiblioteket.doms.transformers.fileenricher;
 
 import dk.statsbiblioteket.doms.central.CentralWebservice;
 import dk.statsbiblioteket.doms.central.InvalidCredentialsException;
 import dk.statsbiblioteket.doms.central.InvalidResourceException;
 import dk.statsbiblioteket.doms.central.MethodFailedException;
 import dk.statsbiblioteket.doms.client.exceptions.NotFoundException;
 import dk.statsbiblioteket.doms.transformers.common.DomsConfig;
 import dk.statsbiblioteket.doms.transformers.common.FileRecordingObjectListHandler;
 import dk.statsbiblioteket.doms.transformers.common.ObjectHandler;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 /**
  * Created by IntelliJ IDEA.
  * User: abr
  * Date: 7/17/12
  * Time: 11:36 AM
  * To change this template use File | Settings | File Templates.
  */
 public class DomsFFProbeFileEnricherObjectHandler implements ObjectHandler{
 
     private final DomsConfig config;
     private final CentralWebservice webservice;
 
     private final String ffprobeDir;
 
     private static final Logger log = LoggerFactory.getLogger(DomsFFProbeFileEnricherObjectHandler.class);
     private static final Logger ffProbeLog = LoggerFactory.getLogger("ffprobe");
 
 
     /**
      * Initialise object handler.
      * @param config Configuration.
      * @param webservice The DOMS WebService.
      */
     public DomsFFProbeFileEnricherObjectHandler(FileEnricherConfig config, CentralWebservice webservice){
         this.config = config;
         this.webservice = webservice;
         ffprobeDir = config.getFFprobeFilesLocation();
     }
 
 
 
     @Override
     public void transform(String uuid) throws Exception {
         try {
             getFFProbeXml(uuid);
         } catch (FileNotFoundException e) {
             FileRecordingObjectListHandler.recordFailure("Missing ffprobe data for " + uuid, e);
             ffProbeLog.warn("Missing ffprobe data for " + uuid, e);
         }
     }
 
     private  String getFFProbeXml(String uuid) throws InvalidCredentialsException, MethodFailedException, InvalidResourceException, IOException {
         String ffprobe;
         String ffprobeErrors;
         String incompleteFilePath = getFFProbeBaseName(uuid);
         String stdoutFilePath = incompleteFilePath + ".stdout";
         String stderrFilePath = incompleteFilePath + ".stderr";
 
 
         try {
             ffprobe = getFFProbeFromObject(uuid);
             log.info(String.format("ffprobe data for %s already exists, not updating.", uuid));
         } catch (NotFoundException e) {
             ffprobe = getFFProbeXMLFromFileName(stdoutFilePath);
             addFFProbeToObject(uuid, ffprobe);
         }
 
         try {
             ffprobeErrors = getFFProbeErrorsFromObject(uuid);
             log.info(String.format("ffprobe error data for %s already exists, not updating.", uuid));
         } catch (NotFoundException e) {
             ffprobeErrors = getFFProbeErrorsXMLFromFileName(stderrFilePath);
            addFFProbeToObject(uuid, ffprobeErrors);
         }
 
         return ffprobe;
     }
 
     private  String getFFProbeXMLFromFile(String uuid) throws IOException {
         File ffprobeFile = new File(ffprobeDir, uuid + ".stdout");
         String ffprobeContents = org.apache.commons.io.IOUtils.toString(new FileInputStream(ffprobeFile));
         ffprobeContents = ffprobeContents.substring(ffprobeContents.indexOf("<ffprobe"));
         return ffprobeContents;
     }
 
     private void addFFProbeToObject(String uuid, String ffprobe) throws InvalidCredentialsException, MethodFailedException, InvalidResourceException {
         webservice.modifyDatastream(uuid,"FFPROBE",ffprobe,"Adding ffprobe as part of the radio/tv datamodel upgrade");
     }
 
     private void addFFProbeErrorsToObject(String uuid, String ffprobe) throws InvalidCredentialsException, MethodFailedException, InvalidResourceException {
         webservice.modifyDatastream(uuid,"FFPROBE_ERROR_LOG", ffprobe, "Adding ffprobe errors as part of the radio/tv datamodel upgrade");
     }
 
     private String getFFProbeFromObject(String uuid) throws NotFoundException, InvalidCredentialsException, MethodFailedException {
         try {
             String contents = webservice.getDatastreamContents(uuid, "FFPROBE");
             return contents;
         } catch (InvalidResourceException e) {
             throw new NotFoundException("Failed to retrieve FFPROBE datastream ",e);
         }
 
     }
 
     public static String getFFProbeXMLFromFileName(String fileName) throws IOException {
         File ffprobeFile = new File(fileName);
         String ffprobeContents = org.apache.commons.io.IOUtils.toString(new FileInputStream(ffprobeFile));
         ffprobeContents = ffprobeContents.substring(ffprobeContents.indexOf("<ffprobe"));
         return ffprobeContents;
 
     }
 
     private String getFFProbeErrorsFromObject(String uuid) throws NotFoundException, InvalidCredentialsException, MethodFailedException {
         try {
             String contents = webservice.getDatastreamContents(uuid, "FFPROBE_ERROR_LOG");
             return contents;
         } catch (InvalidResourceException e) {
             throw new NotFoundException("Failed to retrieve FFPROBE_ERROR_LOG datastream ",e);
         }
 
     }
 
     public static String getFFProbeErrorsXMLFromFileName(String fileName) throws IOException {
         File ffprobeErrorFile = new File(fileName);
         String ffprobeContents = org.apache.commons.io.IOUtils.toString(new FileInputStream(ffprobeErrorFile));
 
         return "<ffprobe:ffprobeStdErrorOutput \n" +
                 "xmlns:ffprobe='http://www.ffmpeg.org/schema/ffprobe'><![CDATA[\n" +
                 ffprobeContents +
                 "]]></ffprobe:ffprobeStdErrorOutput>";
     }
 
     private String getFFProbeBaseName(String uuid) throws InvalidCredentialsException, MethodFailedException, InvalidResourceException {
         String fileName = getFileNameFromUuid(webservice, uuid);
         if (fileName != null) {
             return ffprobeDir + fileName;
         } else {
             return null;
         }
     }
 
     public static String getFileNameFromUuid(CentralWebservice webservice, String uuid) throws InvalidCredentialsException, MethodFailedException, InvalidResourceException {
         String url = webservice.getObjectProfile(uuid).getTitle();
         if (url != null && url.contains(".")) {
             String[] parts = url.split("/");
             return parts[parts.length-1];
         } else {
             return null;
         }
     }
 }
