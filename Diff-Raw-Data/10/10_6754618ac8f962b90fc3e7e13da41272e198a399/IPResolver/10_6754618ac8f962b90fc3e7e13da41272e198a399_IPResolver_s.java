 package com.gmsxo.domains.imports;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.nio.charset.StandardCharsets;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 import java.nio.file.StandardCopyOption;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import javax.naming.NamingException;
 
 import org.apache.log4j.Logger;
 
 import com.gmsxo.domains.data.DNSServer;
 import com.gmsxo.domains.data.Domain;
 import com.gmsxo.domains.dns.DNSLookup;
 import com.gmsxo.domains.helpers.DNSHelper;
 import com.gmsxo.domains.helpers.FileHelper;
 
 public class IPResolver {
   public IPResolver(String workingDir) { this.workingDir=workingDir; }
 
   public static void main(String[] args) throws IOException, NamingException {
    /*args=new String[3];
     args[0]="C:\\Temp\\domains\\import\\";
     args[1]="5";
    args[2]="50";*/
     for (String str:args) System.out.println(str);
     if (args.length!=3) {
       System.err.println("Usage IPResolver workingDir threadCount domainsForFile");
       System.exit(-1);
     }
     new IPResolver(args[0]).doJob(Integer.parseInt(args[1]),Integer.parseInt(args[2]));
   }
   //private static final String REGEX="^[A-Z0-9]([A-Z0-9\\-\\.]*){1}( NS | IN NS ){1}[A-Z0-9]([A-Z0-9\\-\\.]*){1}$";
   
   private static final Logger   LOG = Logger.getLogger(IPResolver.class);
   private static final String[] TOPLEVELS={".biz",".com",".info",".net",".org",".us"};
   private static final String   BACKUP= "backup" +File.separator;
   private static final String   EXPORT= "export" +File.separator;
   private static final String   WORKING="working"+File.separator;
   
   private static final String CUT_FILE_EXT=".cut";
   private static final String BAK_FILE_EXT=".bak";
   private static final String PART_FILE_EXT=".part";
   private static final String RES_FIL_EXT=".res";
   
   private String workingDir;
   private BufferedReader reader;
   private Domain lastDomain;
   private String topLevel;
   private static long requestCounter=0;
   private static long resultCounter=0;
   private String line="";
   private int dnsSplit=2;
   
   private String getTopLevel(String fileName) {
     for (String topLevel:TOPLEVELS) if (fileName.contains(topLevel)) return topLevel;
     return null;
   }
   
   public void doJob(final int poolSize, final int domainsInfCutFile) throws IOException {
     final int  DNS_LOOKUP_POOL_SIZE=poolSize;
 
     checkDirs();
     cutFiles(domainsInfCutFile);
     
     while (true) {
       long startTime=new Date().getTime();
       String domainFileName=getNextFile(); // get the next file from working directory
       if (domainFileName==null) break;     // if there is none -> end
       topLevel=getTopLevel(domainFileName); // init topLevel domain from the file name
      if (topLevel.equals(".us")) dnsSplit=3;
       else dnsSplit=2;
       lastDomain=new Domain();
       
       if (topLevel==null) { // if there is no tld in the file name (e.g. ".info"), rename the file and continue with next
         Files.move(Paths.get(domainFileName), Paths.get(domainFileName+BAK_FILE_EXT), StandardCopyOption.REPLACE_EXISTING);
         continue;
       }
       
       // create thread pool and result list
       ExecutorService nsLookupPool=Executors.newFixedThreadPool(DNS_LOOKUP_POOL_SIZE);
       List<Future<NSLookupThread.Result>> nsLookupResultList=new ArrayList<>();
       
       // create and start export thread which will save the results into a file
       ExportToFileThread export = new ExportToFileThread(workingDir+WORKING,domainFileName, workingDir+EXPORT,PART_FILE_EXT,RES_FIL_EXT);
       Thread exportThread = new Thread(export);
       exportThread.start();
 
       try (BufferedReader reader=Files.newBufferedReader(Paths.get(workingDir+WORKING+domainFileName),StandardCharsets.UTF_8)) {
         line="";
         this.reader=reader;
         
         // fill the thread pool
         for (int i=0;i<DNS_LOOKUP_POOL_SIZE&&line!=null;i++) {
           nsLookupResultList.add(nsLookupPool.submit(new NSLookupThread(getNextDomain())));
           requestCounter++;
         }
         
         // finish the input file and continuously refill the thread pool
         // also add results to the export thread to be saved into a file
         for (int i=0;;) { // just loop
           if (nsLookupResultList.get(i).isDone()) { // if the thread is finished
             try { // try to get results
               NSLookupThread.Result result=nsLookupResultList.get(i).get();
               if (result.getDomain()!=null) export.addDomains(result.getDomain()); // if there is a result send it to the export thread
               LOG.debug(resultCounter+" / "+result.getDomain());
               resultCounter++;
               if (line!=null) { // was there still a line in the import file?
                 nsLookupResultList.set(i,nsLookupPool.submit(new NSLookupThread(getNextDomain()))); // replace finished thread with a new one
                 requestCounter++;
               }
               else { // there are no other domains in the file
                 nsLookupResultList.remove(i); // remove finished thread
                 if (nsLookupResultList.size()==0) break; // if there are no more threads, just finish
               }
             } catch (InterruptedException | ExecutionException e) { LOG.error("isDone",e); }
           }
           if (++i>=nsLookupResultList.size()) i=0; // and start again
         }
       } catch (IOException e) { LOG.error("doThreadJob1",e); }
       finally { nsLookupPool.shutdown(); }
       while (!nsLookupPool.isTerminated())
         ;
       exportThread.interrupt();
       Files.delete(Paths.get(workingDir+WORKING+domainFileName));
       BigDecimal totalTime=BigDecimal.valueOf((new Date().getTime()-startTime)/1000d).setScale(2, BigDecimal.ROUND_HALF_EVEN);
       BigDecimal perSecond=BigDecimal.valueOf(requestCounter).divide(totalTime, 2, BigDecimal.ROUND_HALF_EVEN);
       LOG.warn("Total time:"+totalTime+" per sec: "+perSecond +" "+domainFileName);
     }
   }
   private Domain getNextDomain() throws IOException {
     LOG.debug("getNextDomain()");
     Domain returnDomain = null;
     DNSServer dnsServer=null;
     String domainName=null;
     while (true) {
       line = reader.readLine();
       LOG.debug(line);
       if (line!=null) {
         if (!line.matches(DNSHelper.DOMAIN_REGEXP)) continue;
         String[] split=line.split(" ",-1);
         if (split.length<2) return null;
         domainName=DNSLookup.formatDomain(split[0],topLevel);
         dnsServer = new DNSServer(DNSLookup.formatDNS(split[dnsSplit],topLevel));
       }
       if (line!=null&&domainName.equals(lastDomain.getDomainName())) { // it is the same domain, add DNS server and continue
         lastDomain.getDnsServer().add(dnsServer);
         continue;
       } else { // it is the next domain, create new domain, update return domain and update the last domain with the new one
         Domain domain=new Domain();
         domain.setDomainName(domainName);
         domain.getDnsServer().add(dnsServer);
         
         if (lastDomain.getDomainName()==null) { // the first line
           lastDomain=domain;
           continue;
         }
         returnDomain=lastDomain;
         lastDomain=domain;
         break;
       }
     }
     LOG.debug("returned: "+returnDomain);
     return returnDomain;
   }
   private void checkDirs() throws IOException {
     Files.createDirectories(Paths.get(workingDir+BACKUP));
     Files.createDirectories(Paths.get(workingDir+EXPORT));
     Files.createDirectories(Paths.get(workingDir+WORKING));
   }
   private void cutFiles(int domainsInFile) throws IOException {
     for (String fileName:FileHelper.getFiles(workingDir, "")) {
       FileHelper.cutDomainFile(workingDir, fileName, workingDir+WORKING, domainsInFile, topLevel, CUT_FILE_EXT);
       Files.move(Paths.get(workingDir+fileName), Paths.get(workingDir+BACKUP+fileName), StandardCopyOption.REPLACE_EXISTING);
     }
   }
   private String getNextFile() throws IOException {
     List<String> files = FileHelper.getFiles(workingDir+WORKING, CUT_FILE_EXT);
     Collections.sort(files);
     if (files==null||files.size()<1) return null;
     return files.get(0);
   }
 }
