 package eu.alertproject.iccs.mlsensor.subscribers.kde.internal;
 
 import eu.alertproject.iccs.mlsensor.connector.producer.MLMessagePublisher;
 import eu.alertproject.iccs.mlsensor.connector.producer.MLRealTimeMessagePublisher;
 import eu.alertproject.iccs.mlsensor.subscribers.api.CompoundVisitor;
 import eu.alertproject.iccs.mlsensor.subscribers.api.MLMessagePublisherVisitor;
 import eu.alertproject.iccs.mlsensor.subscribers.api.MailParser;
 import eu.alertproject.iccs.mlsensor.subscribers.api.SimpleLoggerVisitor;
 import eu.alertproject.iccs.mlsensor.subscribers.kde.api.KdeDownloader;
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.io.LineIterator;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mail.javamail.JavaMailSender;
 import org.springframework.stereotype.Service;
 
 import java.io.*;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.GZIPInputStream;
 
 /**
  * User: fotis
  * Date: 02/11/11
  * Time: 20:14
  */
 @Service("kdeDownloader")
 public class KdeDownloaderImpl implements KdeDownloader {
 
     private Logger logger = LoggerFactory.getLogger(KdeDownloaderImpl.class);
 
     private Pattern compile = Pattern.compile("^\\s+<td><A href=\"(.*)\">.*</a></td>");
 
     @Autowired
     private JavaMailSender mailSender;
 
     @Autowired
     MailParser mailParser;
 
     @Autowired
     MLRealTimeMessagePublisher realTimeMessagePublisher;
 
     @Autowired
     MLMessagePublisher mlMessagePublisher;
 
     @Override
     public List<URL> fetchUrls(String url) throws IOException{
 
         List<URL> urls=new ArrayList<URL>();
 
         InputStream inputStream = null;
         try {
             URL u = new URL(url);
 
             inputStream = u.openStream();
 
             LineIterator it = IOUtils.lineIterator(inputStream,"us-ascii");
 
             while(it.hasNext()){
 
                 String line = it.nextLine();
 
                 logger.trace("Line = {}",line);
                 Matcher matcher = compile.matcher(line);
                 if(matcher.matches()){
 
                     String link = matcher.group(1);
                     logger.debug("Link = {}",link);
                     urls.add(new URL(url+"/"+link));
                 }
             }
 
 
         } catch (IOException e) {
             logger.warn("Couldn't load url ({})  provided",url);
             throw e;
         } finally {
             IOUtils.closeQuietly(inputStream);
         }
 
         //urls are backwards
         Collections.reverse(urls);
 
         return urls;
     }
 
     @Override
     public void loadMessages(List<URL> urls) throws IOException {
 
         realTimeMessagePublisher.setRealTimeEnabled(false);
 
         String tmpDirectory = System.getProperty("java.io.tmpdir");
 
         logger.trace("void loadMessages() Temporary directory {} ",tmpDirectory);
 
         File f= new File(tmpDirectory,"iccs-"+System.currentTimeMillis());
 
         if(!f.mkdir()){
             throw new IOException("Couldn't create temporary directory");
         }
 
         FileUtils.forceDeleteOnExit(f);
 
 
         for(URL u : urls){
             logger.debug("URL = ({})",u.toString());
 
             //download the file
             String data = StringUtils.substringAfterLast(u.toString(), "/");
 
             logger.debug("Data = ({})", data);
             String fileId = DigestUtils.md5Hex(data);
 
 
             InputStream is = null;
 
             try{
 
                 is = u.openStream();
 
 
                 //create the temporary file
                 File iccs = File.createTempFile(fileId, ".gz",f);
 
                 logger.debug("Downloading {} to file {} ",u,iccs.getAbsolutePath());
                 long l = IOUtils.copyLarge(is, new FileOutputStream(iccs));
 
                 if(l <= 0L){
 
                     throw new IOException("The url {} was not downloaded properly");
                 } else{
                     logger.info("Downloaded {} ({}) bytes",u,l);
                 }
 
 
                 InputStream gis = null;
                 FileOutputStream output = null;
 
                 try {
 
                     if (data.endsWith(".gz")) {
                         //now we need to extract it
                         gis = new GZIPInputStream(new FileInputStream(iccs));
 
 
                     } else {
                         gis = new FileInputStream(iccs);
                     }
 
                     logger.trace("void loadMessages() Extracting {}", iccs.getName());
                     File extractedFile = new File(iccs.getParent(),
                             StringUtils.substringBeforeLast(iccs.getName(), ".") + ".txt"
                     );
                     output = new FileOutputStream(
                             extractedFile
                     );
 
                     IOUtils.copyLarge(gis, output);
 
                     mailParser.parse(
                             FileUtils.lineIterator(extractedFile),
                             new CompoundVisitor(
                                 new MLMessagePublisherVisitor(mlMessagePublisher),
                                new SimpleLoggerVisitor(),
                                new ForwardGoogleVisitor(mailSender)
                             )
                     );
                 } catch (IOException e) {
                     throw e;
                 } finally {
                     IOUtils.closeQuietly(gis);
                     IOUtils.closeQuietly(output);
                 }
 
 
             }catch (IOException e){
                 logger.warn("Could load the file for {}",u.toString());
                 throw e;
             }finally {
                 IOUtils.closeQuietly(is);
             }
 
         }
 
         realTimeMessagePublisher.setRealTimeEnabled(true);
 
     }
 
 }
