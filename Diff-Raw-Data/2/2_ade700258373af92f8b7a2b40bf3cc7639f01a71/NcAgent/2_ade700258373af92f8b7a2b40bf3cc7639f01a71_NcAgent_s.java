 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.ooici.eoi.datasetagent.impl;
 
 import ion.core.IonException;
 import ion.core.utils.GPBWrapper;
 import ion.core.utils.IonUtils;
 import ion.core.utils.ProtoUtils;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.TreeMap;
 
 import net.ooici.eoi.crawler.DataSourceCrawler;
 import net.ooici.eoi.crawler.impl.DapDataSourceCrawler;
 import net.ooici.eoi.crawler.impl.FtpAccessClient;
 import net.ooici.eoi.crawler.impl.FtpDataSourceCrawler;
 import net.ooici.eoi.crawler.util.UrlParser;
 import net.ooici.eoi.datasetagent.AbstractNcAgent;
 import net.ooici.eoi.datasetagent.AgentFactory;
 import net.ooici.eoi.datasetagent.AgentUtils;
 import net.ooici.eoi.datasetagent.IDatasetAgent;
 import net.ooici.eoi.netcdf.NcDumpParse;
 import net.ooici.services.dm.IngestionService.DataAcquisitionCompleteMessage.StatusCode;
 import net.ooici.services.sa.DataSource;
 import net.ooici.services.sa.DataSource.EoiDataContextMessage;
 import net.ooici.services.sa.DataSource.RequestType;
 import net.ooici.services.sa.DataSource.SourceType;
 
 import org.apache.commons.httpclient.Credentials;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.auth.AuthScheme;
 import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
 import org.apache.commons.httpclient.auth.CredentialsProvider;
 import org.apache.commons.httpclient.auth.RFC2617Scheme;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ucar.ma2.InvalidRangeException;
 import ucar.ma2.Range;
 import ucar.nc2.constants.AxisType;
 import ucar.nc2.dataset.CoordinateAxis;
 import ucar.nc2.dataset.CoordinateAxis1DTime;
 import ucar.nc2.dataset.NetcdfDataset;
 import ucar.nc2.util.net.HttpClientManager;
 
 /**
  * The NcAgent class is designed to fulfill updates for datasets which originate as Netcdf files (*.nc). Ensure the update context (
  * {@link EoiDataContextMessage}) to be passed to {@link #doUpdate(EoiDataContextMessage, HashMap)} has been constructed for NC agents by
  * checking the result of {@link EoiDataContextMessage#getSourceType()}
  * 
  * @author cmueller
  * @author tlarocque (documentation)
  * @version 1.0
  * @see {@link EoiDataContextMessage#getSourceType()}
  * @see {@link AgentFactory#getDatasetAgent(net.ooici.services.sa.DataSource.SourceType)}
  */
 public class NcAgent extends AbstractNcAgent {
 
     private static final Logger log = LoggerFactory.getLogger(NcAgent.class);
     private Date sTime = null, eTime = null;
 
     /**
      * Constructs a local reference to an NCML file which acts as an access point to the <code>NetcdfDataset</code> which can provide
      * updates for the given <code>context</code>. The resultant filepath may subsequently be passed through {@link #acquireData(String)} to
      * procure updated data according to the <code>context</code> given here.
      * 
      * @param context
      *            the current or required state of an NC dataset providing context for building data requests to fulfill dataset updates
      * @return A filepath pointing to an NCML file built from the given <code>context</code>.
      * 
      * @see #buildNcmlMask(String, String)
      */
     @Override
     public String buildRequest() {
 
         String result = null;
 
         RequestType type = context.getRequestType();
         if (context.hasStartDatetimeMillis()) {
             sTime = new Date(context.getStartDatetimeMillis());
         }
         if (context.hasEndDatetimeMillis()) {
             eTime = new Date(context.getEndDatetimeMillis());
         }
         switch (type) {
             case FTP:
                 result = buildRequest_ftpMask();
                 break;
             case DAP:
             /* FALL_THROUGH */
             case NONE:
             /* FALL_THROUGH */
             default:
 //                if (context.getSourceType() == SourceType.NETCDF_C) {
 //                    result = buildRequest_dynamicDapMask();
 //                } else {
 //                    result = buildRequest_dapMask();
 //                }
                 result = buildRequest_dapMask();
                 break;
         }
 
 
         return result;
     }
 
     public String buildRequest_dapMask() {
         String ncmlTemplate = context.getNcmlMask();
         String ncdsLoc = context.getDatasetUrl();
 
         String openLoc;
         if (ncmlTemplate.isEmpty()) {
             openLoc = ncdsLoc;
         } else {
             openLoc = buildNcmlMask(ncmlTemplate, ncdsLoc);
         }
         if (log.isDebugEnabled()) {
             log.debug(openLoc);
         }
         return openLoc;
     }
 
     public String buildRequest_dynamicDapMask() {
         /* TODO: merge this code with buildRequest_ftp so that we arent duplicating code */
         /** Build an FTP-like mask to aggregate all files included in the temporal extents */
         /* Get data from the context to build a request */
         long startTime = context.getStartDatetimeMillis();
         long endTime = context.getEndDatetimeMillis();
 
         /* Get the host and directory from the base_url field */
         UrlParser p = new UrlParser(context.getBaseUrl());
         String host = p.getHost();
         String baseDir = p.getDirectory();
 
         /* Get the pattern parameters from the search_pattern field (CASRef) */
         String filePattern = "";
         String dirPattern = "";
         String joinDim = "";
         if (context.hasSearchPattern()) {
             final net.ooici.services.sa.DataSource.SearchPattern pattern = (net.ooici.services.sa.DataSource.SearchPattern) structManager.getObjectWrapper(context.getSearchPattern()).getObjectValue();
 
             filePattern = pattern.getFilePattern();
             dirPattern = pattern.getDirPattern();
             joinDim = pattern.getJoinName();
         }
 
 
         /** Get a list of files at the FTP host between the start and end times */
         DataSourceCrawler crawler = new DapDataSourceCrawler(host, baseDir, filePattern, dirPattern);
 
         Map<String, Long> remoteFiles = null;
         try {
 
             remoteFiles = crawler.getTargetFilesFullPath(startTime, endTime);
 
         } catch (IOException e) {
             // TODO handle this -- failure to gather target files for time range and datasource
             log.error("Failed to gather target files from datasource for given time range.", e);
         }
 
 
         /** Generating an NCML to aggregate all files (via unions/joins) */
         File temp = null;
         try {
             temp = File.createTempFile("ooi-", ".ncml");
             if (!log.isDebugEnabled()) {
                 temp.deleteOnExit();
             }
             DataSourceCrawler.generateNcml(temp, remoteFiles, joinDim, context.getNcmlMask());
         } catch (IOException ex) {
             // TODO: handle this -- failure to generate NCML aggregation for DAP files..
             log.error("Failed to generate NCML aggregation for DAP files.", ex);
         }
 
         String filepath = temp.getAbsolutePath();
         if (log.isDebugEnabled()) {
             log.debug("\n\nGenerated NCML aggregation...\n\t\"{}\"", filepath);
         }
 
 
         return filepath;
     }
 
     public String buildRequest_ftpMask() {
         /** Get data from the context to build a the "request" */
         /* -- with the FTP client, the request is actually the resultant
          *    data after it has been downloaded, unzipped, and aggregated.
          *    In the eyes of OOICI, the data being request is a pointer to
          *    the aggregation NCML after this process completes -- this
          *    will be the result of buildRequest()
          */
         long startTime = context.getStartDatetimeMillis();
         long endTime = context.getEndDatetimeMillis();
 
         /* Get the host and directory from the base_url field */
         UrlParser p = new UrlParser(context.getBaseUrl());
         String host = p.getHost();
         String baseDir = p.getDirectory();
 
         /* Get the pattern parameters from the search_pattern field (CASRef) */
         String filePattern = "";
         String dirPattern = "";
         String joinDim = "";
         if (context.hasSearchPattern()) {
             final net.ooici.services.sa.DataSource.SearchPattern pattern = (net.ooici.services.sa.DataSource.SearchPattern) structManager.getObjectWrapper(context.getSearchPattern()).getObjectValue();
 
             filePattern = pattern.getFilePattern();
             dirPattern = pattern.getDirPattern();
             joinDim = pattern.getJoinName();
         }
 
 
         /** Get a list of files at the FTP host between the start and end times */
         DataSourceCrawler crawler = new FtpDataSourceCrawler(host, baseDir, filePattern, dirPattern);
 
         Map<String, Long> remoteFiles = null;
         try {
             remoteFiles = crawler.getTargetFilesRelativeToBase(startTime, endTime);
         } catch (IOException ex) {
             throw new IonException("Failure to gather target files from datasource for given time range.", ex);
         }
 
 
         /** Download all necessary files */
         if (log.isDebugEnabled()) {
             log.debug("\n\nDOWNLOADING...");
         }
         Map<String, Long> localFiles = new TreeMap<String, Long>();
         File tempFile = null;
         String TEMP_DIR = null;
         List<String> existing = null;
         try {
             FtpAccessClient ftp = new FtpAccessClient(host);
             ftp.cd(baseDir);
 
             tempFile = File.createTempFile("prefix", "");
 //            TEMP_DIR = tempFile.getParent() + File.separatorChar + UUID.randomUUID().toString() + File.separatorChar;
             TEMP_DIR = tempFile.getParent() + File.separatorChar + context.getDatasetId() + File.separatorChar;
             File outDirF = new File(TEMP_DIR);
             if (!outDirF.exists()) {
                 boolean success = outDirF.mkdirs();
                 if (log.isDebugEnabled()) {
                     log.debug("{} temp directory: {}", (success) ? "Successfully made" : "Failed to make", outDirF.getAbsolutePath());
                 }
             }
             existing = Arrays.asList(new File(TEMP_DIR).list(new FilenameFilter() {
 
                 @Override
                 public boolean accept(File dir, String name) {
                     return true;
                 }
             }));
             tempFile.delete();
             for (String key : remoteFiles.keySet()) {
                 if (log.isDebugEnabled()) {
                     log.debug("\n\n====> Acquiring file '{}'", key);
                 }
                 String unzipped = null;
                 /* Download the file if we don't already have it */
                 String download = null;
                 if (existing.contains(key)) {
                     download = TEMP_DIR.concat(key);
                     File f = new File(download);
                     long lsize = f.length();
                     long rsize = ftp.getFileSize(key);
                     if (rsize == lsize) {
                         if (log.isDebugEnabled()) {
                             log.debug("\n====> Existing file found: {}", download);
                         }
                     } else {
                         if (log.isDebugEnabled()) {
                             log.debug("\n====> File sizes differ, reacquire the file [remote:{} bytes; local:{} bytes]", rsize, lsize);
                         }
                         download = null;
                     }
                 }
                 if (download == null) {
                     int i = 0;
                     while (i <= 2) {
                         try {
                             download = ftp.download(key, TEMP_DIR);
                             if (log.isDebugEnabled()) {
                                 log.debug("\n====> Downloaded file: {}", download);
                             }
                             i = 3;
                         } catch (IOException ex) {
                             if (log.isWarnEnabled()) {
                                 log.warn("\n====> Attempt {} of 3 to download file failed: '{}'.  Cause: {}", new Object[]{i + 1, key, ex});
                             }
                         }
                         i++;
                     }
                 }
 
                 if (download != null) {
                     /* Unzipping... */
                     unzipped = FtpAccessClient.unzip(download, !log.isDebugEnabled()).get(0);
                     if (log.isDebugEnabled()) {
                         log.debug("\n====> Unzipped: {}", unzipped);
                     }
 
                     /* Insert the new output name back into the map */
                     Long val = remoteFiles.get(key);
                     localFiles.put(unzipped, val);
                 } else {
                     throw new IOException("Unable to download file: '" + key + "'");
                 }
             }
         } catch (IOException ex) {
             throw new IonException("Failed to access datasource via FTP", ex);
         }
 
         String filepath = null;
 //        /** Generating an NCML to aggregate all files (via unions/joins) */
 //        File temp = null;
 //        try {
 //            temp = File.createTempFile("ooi-", ".ncml");
 //            if (!log.isDebugEnabled()) {
 //                temp.deleteOnExit();
 //            }
 //            DataSourceCrawler.generateNcml(temp, localFiles, joinDim, context.getNcmlMask());
 //        } catch (IOException ex) {
 //            // TODO: handle this -- failure to generate NCML aggregation for FTP files..
 //            log.error("Failed to generate NCML aggregation for FTP files", ex);
 //        }
 //        filepath = temp.getAbsolutePath();
 
         filepath = buildNcmlMask(context.getNcmlMask(), TEMP_DIR);
 
         if (log.isDebugEnabled()) {
             log.debug("\n\nGenerated NCML aggregation...\n\t\"{}\"", filepath);
         }
 
 
 
         return filepath;
     }
 
     /**
      * Satisfies the given <code>request</code> by interpreting it as a Netcdf ncml file and then, by constructing a {@link NetcdfDataset}
      * object from that file. Requests are built dynamically in
      * {@link #buildRequest(net.ooici.services.sa.DataSource.EoiDataContextMessage)}. This method is a convenience for opening
      * {@link NetcdfDataset} objects from the result of the {@link #buildRequest(net.ooici.services.sa.DataSource.EoiDataContextMessage)}
      * method.
      * 
      * @param request
      *            an ncml filepath as built from {@link IDatasetAgent#buildRequest(net.ooici.services.sa.DataSource.EoiDataContextMessage)}
      * @return the response of the given <code>request</code> as a {@link NetcdfDataset} object
      * 
      * @see #buildRequest(net.ooici.services.sa.DataSource.EoiDataContextMessage)
      * @see NetcdfDataset#openDataset(String)
      */
     @Override
     public Object acquireData(String request) {
         NetcdfDataset ncds = null;
         try {
             if (context.hasAuthentication()) {
                 /* Get the authentication object from the structure */
                 final net.ooici.services.sa.DataSource.ThreddsAuthentication tdsAuth = (net.ooici.services.sa.DataSource.ThreddsAuthentication) structManager.getObjectWrapper(context.getAuthentication()).getObjectValue();
 
                 CredentialsProvider provider = new CredentialsProvider() {
 
                     @Override
                     public Credentials getCredentials(AuthScheme scheme, String host, int port, boolean proxy) throws CredentialsNotAvailableException {
                         if (scheme == null) {
                             throw new CredentialsNotAvailableException("Null authentication scheme");
                         }
 
                         if (!(scheme instanceof RFC2617Scheme)) {
                             throw new CredentialsNotAvailableException("Unsupported authentication scheme: "
                                     + scheme.getSchemeName());
                         }
 
                         return new UsernamePasswordCredentials(tdsAuth.getName(), tdsAuth.getPassword());
                     }
                 };
 
                 /*  */
                 HttpClientManager.init(provider, "OOICI-ION");
             }
 
             if (log.isDebugEnabled()) {
                 log.debug("Opening NetcdfDataset '{}'", request);
             }
             ncds = NetcdfDataset.openDataset(request, EnumSet.of(NetcdfDataset.Enhance.CoordSystems), -1, null, null);
             if (log.isDebugEnabled()) {
                 log.debug("NetcdfDataset '{}' opened successfully!", request);
             }
         } catch (IOException ex) {
             log.error("Error opening dataset \"" + request + "\"", ex);
         }
         return ncds;
     }
 
     /**
      * Adds subranges to the datasets dimensions as appropriate, breaks that dataset into manageable sections
      * and sends those data "chunks" to the ingestion service.
      * 
      * @param ncds
      *            the {@link NetcdfDataset} to process
      *            
      * @return TODO:
      * 
      * @see #addSubRange(ucar.ma2.Range)
      * @see #sendNetcdfDataset(NetcdfDataset, String)
      * @see #sendNetcdfDataset(NetcdfDataset, String, boolean)
      */
     @Override
     public String[] processDataset(NetcdfDataset ncds) {
         if (sTime != null | eTime != null) {
             /** TODO: Figure out how to deal with sTime and eTime.
              * Ideally, we'd find a way to 'remove' the unwanted times from the dataset, but not sure if this is possible
              * This would allow the 'sendNetcdfDataset' method to stay very generic (since obs requests will already have dealt with time)
              */
             if (log.isDebugEnabled()) {
                 log.debug("Start Time: {}", (sTime != null) ? AgentUtils.ISO8601_DATE_FORMAT.format(sTime) : "no start time specified, use 0");
                 log.debug("End Time: {}", (eTime != null) ? AgentUtils.ISO8601_DATE_FORMAT.format(eTime) : "no end time specified, use 'now'");
             }
 
             if (log.isDebugEnabled()) {
                 log.debug("IsInitial : {}", context.getIsInitial());
             }
 
             int sti = -1, eti = -1;
             String tdim = "";
             CoordinateAxis ca = ncds.findCoordinateAxis(AxisType.RunTime);
             if (ca == null) {
                 ca = ncds.findCoordinateAxis(AxisType.Time);
             }
             CoordinateAxis1DTime cat = null;
             boolean warn = false;
             Throwable thrown = null;
             ucar.ma2.Range trng = null;
             if (ca != null) {
 //                if(ca instanceof CoordinateAxis2D) {
 //                    ca = ncds.findCoordinateAxis(AxisType.RunTime);
 //                }
                 if (ca instanceof CoordinateAxis1DTime) {
                     cat = (CoordinateAxis1DTime) ca;
                 } else {
                     try {
                         cat = CoordinateAxis1DTime.factory(ncds, new ucar.nc2.dataset.VariableDS(null, ncds.findVariable(ca.getName()), true), null);
                     } catch (IOException ex) {
                         warn = true;
                         thrown = ex;
                     }
                 }
                 if (cat != null) {
                     tdim = cat.getName();
                     if (sTime != null) {
                         sti = cat.findTimeIndexFromDate(sTime);
                     } else {
                         sti = 0;
                     }
                     if (eTime != null) {
                         eti = cat.findTimeIndexFromDate(eTime);
                     } else {
                         eti = cat.findTimeIndexFromDate(new Date());
                     }
                     try {
                         /* Only if this is a supplement rather than an initial update:
                          * Adjust the start time index +1 to avoid duplicate data
                          * Bail if (eti - sti <= 0) */
                         if (!context.getIsInitial()) {
                             /* Adjust the start time index +1 to avoid duplicate data */
                             sti++;
                            if (eti - sti <= 0) {
                                 /* Bail if (eti - sti <= 0) */
                                 String err = new StringBuilder("Abort from this update:: The time subrange [").append(tdim).append(":").append(sti).append(":").append(eti).append(":").append("] indicates that there is no new data").toString();
                                 log.warn(err);
                                 this.sendDataErrorMsg(StatusCode.NO_NEW_DATA, err);
                                 return new String[]{err};
                             }
                         }
                         trng = new ucar.ma2.Range(tdim, sti, eti);
                     } catch (InvalidRangeException ex) {
                         warn = true;
                         thrown = ex;
                     }
                     this.addSubRange(trng);
                     if (log.isInfoEnabled()) {
                         log.info("Applied subrange - {}:{}:{}", new Object[]{tdim, sti, eti});
                     }
                 } else {
                     warn = true;
                 }
             } else {
                 warn = true;
             }
             if (warn) {
                 if (thrown != null) {
                     log.warn("Error determining time axis - full time range will be used", thrown);
                 } else {
                     log.warn("Error determining time axis - full time range will be used");
                 }
             }
         }
         /* If the subranges field has items */
         if (context.getSubRangesCount() > 0) {
             /* Iterate over the list of subranges and add them */
             String dimName;
             for (DataSource.SubRange sr : context.getSubRangesList()) {
                 dimName = sr.getDimName();
                 Range rng = null;
                 try {
                     if (ncds.findDimension(dimName) == null) {
                         throw new Exception("Dataset does not contain dimension named \"" + dimName + "\"");
                     }
                     rng = new ucar.ma2.Range(dimName, sr.getStartIndex(), sr.getEndIndex());
                     this.addSubRange(rng);
                     if (log.isInfoEnabled()) {
                         log.info("Applied subrange - {}:{}:{}", new Object[]{dimName, sr.getStartIndex(), sr.getEndIndex()});
                     }
                 } catch (Exception ex) {
                     log.warn("There was a problem generating subrange for \"" + dimName + "\" with start:end indices " + sr.getStartIndex() + ":" + sr.getEndIndex() + ", the full extent of the dimension will be used", ex);
                     continue;
                 }
             }
         }
 
         String response = this.sendNetcdfDataset(ncds, "ingest");
 
         return new String[]{response};
     }
 
     private String buildNcmlMask(String content, String ncdsLocation) {
         BufferedWriter writer = null;
         String temploc = null;
         try {
             content = content.replace("***lochold***", ncdsLocation);
             File tempFile = File.createTempFile("ooi", ".ncml");
 //            tempFile.deleteOnExit();
             temploc = tempFile.getCanonicalPath();
             writer = new BufferedWriter(new FileWriter(tempFile));
             writer.write(content);
         } catch (IOException ex) {
             log.error("Error generating ncml mask for dataset at \"" + ncdsLocation + "\"");
         } finally {
             try {
                 writer.close();
             } catch (IOException ex) {
                 log.error("Error closing ncml template writer");
             }
         }
 
         return temploc;
     }
 
     public static void main(String[] args) throws IOException {
         try {
             ion.core.IonBootstrap.bootstrap();
         } catch (Exception ex) {
             log.error("Error bootstrapping", ex);
         }
 
         manualTesting();
 
 //        writeNcdsForNcml();
 
 //        generateSamples();
 
 //        generateMetadata();
 
     }
 
     private static void writeNcdsForNcml() throws IOException {
 
         String ncml = "file:/Users/tlarocque/cfoutput/cfout-cgsn/ismt2-cr1000.ncml";
         String out = "/Users/tlarocque/Desktop/ismt2-cr1000.nc";
 
         System.out.println("Starting ncds write");
         NetcdfDataset ncds = NetcdfDataset.openDataset(ncml, false, null);
 
         ucar.nc2.FileWriter.writeToFile(ncds, out);
         System.out.println("Write complete!");
 
     }
 
     private static void generateMetadata() throws IOException {
         /** For each of the "R1" netcdf datasets (either local or remote)
          *
          * 1. get the last timestep of the data
          * 2. get the list of global-attributes
          * 3. build a delimited string with the following structure:
          *      attribute_1, attribute_2, attribute_3, ..., attribute_n
          *      value_1, value_2, value_3, ..., value_n
          *
          */
 //        String[] datasetList = new String[]{"http://nomads.ncep.noaa.gov:9090/dods/nam/nam20110303/nam1hr_00z",
 //                                            "http://thredds1.pfeg.noaa.gov/thredds/dodsC/satellite/GR/ssta/1day",
 //                                            "http://tashtego.marine.rutgers.edu:8080/thredds/dodsC/cool/avhrr/bigbight/2010"};
         File metaIn = new File("netcdf_metadata_input.txt");
         if (!metaIn.exists()) {
             System.out.println("The file specifying the datasets (\"netcdf_metadata_input.txt\") cannot be found: cannot continue processing");
             System.exit(1);
         }
         FileReader rdr = new FileReader(metaIn);
         Properties props = new Properties();
         props.load(rdr);
 
 
         Map<String, Map<String, String>> datasets = new TreeMap<String, Map<String, String>>(); /* Maps dataset name to an attributes map */
         List<String> metaLookup = new ArrayList<String>();
 
         /* Front-load the metadata list with the existing metadata headers - preserves order of the spreadsheet */
         File headIn = new File("netcdf_metadata_headers.txt");
         if (!headIn.exists()) {
             System.out.println("The file specifying the existing metadata (\"metadata_headers.txt\") cannot be found: continuing with only \"OOI Minimum\" metadata specified");
             metaLookup.add("title");
             metaLookup.add("institution");
             metaLookup.add("source");
             metaLookup.add("history");
             metaLookup.add("references");
             metaLookup.add("Conventions");
             metaLookup.add("summary");
             metaLookup.add("comment");
             metaLookup.add("data_url");
             metaLookup.add("ion_time_coverage_start");
             metaLookup.add("ion_time_coverage_end");
             metaLookup.add("ion_geospatial_lat_min");
             metaLookup.add("ion_geospatial_lat_max");
             metaLookup.add("ion_geospatial_lon_min");
             metaLookup.add("ion_geospatial_lon_max");
             metaLookup.add("ion_geospatial_vertical_min");
             metaLookup.add("ion_geospatial_vertical_max");
             metaLookup.add("ion_geospatial_vertical_positive");
         }
         BufferedReader headRdr = new BufferedReader(new FileReader(headIn));
         String line;
         while ((line = headRdr.readLine()) != null) {
             metaLookup.add(line.trim());
         }
         headRdr.close();
 
         /* For now, don't add anything - this process will help us figure out what needs to be added */
         String ncmlmask = "<netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\" location=\"***lochold***\"></netcdf>";
         String src = null;
         String url = null;
         String usrHome = System.getProperty("user.home");
         for (Object o : props.keySet()) {
 //        for (String dsUrl : datasetList) {
             /* Get the K/V pair */
             src = o.toString();
             url = props.getProperty(src);
             url = (url.startsWith("~")) ? url.replace("~", usrHome) : url;
 
             System.out.println("Getting ncdump for dataset @ " + url);
 
             /* Acquire metadata for the datasource's url */
             net.ooici.services.sa.DataSource.EoiDataContextMessage.Builder cBldr = net.ooici.services.sa.DataSource.EoiDataContextMessage.newBuilder();
             cBldr.setSourceType(net.ooici.services.sa.DataSource.SourceType.NETCDF_S);
 //            cBldr.setDatasetUrl(dsUrl).setNcmlMask(ncmlmask);
             cBldr.setDatasetUrl(url).setNcmlMask(ncmlmask);
 //            cBldr.setStartTime("");
 //            cBldr.setEndTime("");
 
             /* Wrapperize the context object */
             GPBWrapper contextWrap = GPBWrapper.Factory(cBldr.build());
             /* Generate an ionMsg with the context as the messageBody */
             net.ooici.core.message.IonMessage.IonMsg ionMsg = net.ooici.core.message.IonMessage.IonMsg.newBuilder().setIdentity(java.util.UUID.randomUUID().toString()).setMessageObject(contextWrap.getCASRef()).build();
             /* Create a Structure and add the objects */
             net.ooici.core.container.Container.Structure.Builder sBldr = net.ooici.core.container.Container.Structure.newBuilder();
             /* Add the eoi context */
             ProtoUtils.addStructureElementToStructureBuilder(sBldr, contextWrap.getStructureElement());
             /* Add the IonMsg as the head */
             ProtoUtils.addStructureElementToStructureBuilder(sBldr, GPBWrapper.Factory(ionMsg).getStructureElement(), true);
 
             String[] resp = null;
             try {
                 resp = runAgent(sBldr.build(), AgentRunType.TEST_NO_WRITE);
             } catch (Exception e) {
                 log.error("Exception encountered while Running agent with param 'TEST_NO_WRITE'", e);
                 datasets.put(src + " (FAILED)", null);
                 continue;
             }
 
 
             System.out.println(".....");
 //            System.out.println("\n\nDataSource:\t" + src + "\n-------------------------------------\n" + NcDumpParse.parseToDelimited(resp[0]));
 //            Map<String, String> metadataMap = NcDumpParse.parseToMap(resp[0]);
 //            TreeMap<String, String> sortedMetadata = new TreeMap<String, String>(metadataMap);
 //            
 //            for (Object key : sortedMetadata.keySet()) {
 //                System.out.println(key.toString());
 //            }
             Map<String, String> dsMeta = NcDumpParse.parseToMap(resp[0]);
             datasets.put(src, dsMeta);
 
 
             /* TODO: Eventually we can make this loop external and perform a sort beforehand.
              *       this sort would frontload attributes which are found more frequently
              *       across multiple datasets
              */
             for (String key : dsMeta.keySet()) {
                 if (!metaLookup.contains(key)) {
                     metaLookup.add(key);
                 }
             }
 
         }
 
 
         /** Write the CSV output */
         String NEW_LINE = System.getProperty("line.separator");
         StringBuilder sb = new StringBuilder();
 
         /* TODO: Step 1: add header data here */
         sb.append("Dataset Name");
         for (String metaName : metaLookup) {
             sb.append("|");
             sb.append(metaName);
 //            sb.append('"');
 //            sb.append(metaName.replaceAll(Pattern.quote("\""), "\"\""));
 //            sb.append('"');
         }
 
         /* Step 2: Add each row of data */
         for (String dsName : datasets.keySet()) {
             Map<String, String> dsMeta = datasets.get(dsName);
             sb.append(NEW_LINE);
             sb.append(dsName);
 //            sb.append('"');
 //            sb.append(dsName.replaceAll(Pattern.quote("\""), "\"\""));
 //            sb.append('"');
             String metaValue = null;
             for (String metaName : metaLookup) {
                 sb.append("|");
                 if (null != dsMeta && null != (metaValue = dsMeta.get(metaName))) {
                     sb.append(metaValue);
                     /* To ensure correct formatting, change all existing double quotes
                      * to two double quotes, and surround the whole cell value with
                      * double quotes...
                      */
 //                    sb.append('"');
 //                    sb.append(metaValue.replaceAll(Pattern.quote("\""), "\"\""));
 //                    sb.append('"');
                 }
             }
 
         }
 
         /* writer the metadata headers to the "headers" file */
         headIn.delete();
         BufferedWriter writer = new BufferedWriter(new FileWriter(headIn));
         String nl = System.getProperty("line.seperator");
         for (int i = 0; i < metaLookup.size() - 1; i++) {
             writer.write(metaLookup.get(i));
             writer.write(nl);
         }
         writer.write(metaLookup.get(metaLookup.size() - 1));
         writer.flush();
         writer.close();
 
 
         System.out.println(NEW_LINE + NEW_LINE + "********************************************************");
         System.out.println(sb.toString());
         System.out.println(NEW_LINE + "********************************************************");
 
     }
 
     private static void generateSamples() {
     }
 
     private static void manualTesting() throws IOException {
         /* the ncml mask to use*/
         String ncmlmask = "";
         String dataurl = "";
         String baseUrl = "";
         String sTime = "";
         String eTime = "";
         String uname = null;
         String pass = null;
         String filePattern = null;
         String dirPattern = null;
         String joinName = null;
         List<String> subDims = new ArrayList<String>();
         List<int[]> subIndices = new ArrayList<int[]>();
         net.ooici.services.sa.DataSource.RequestType requestType = net.ooici.services.sa.DataSource.RequestType.DAP;
 //        long maxSize = -1;
 
 
         /** ******************** */
         /*  DAP Request Testing  */
         /** ******************** */
         /* NAM */
         /* WARNING!!  This is a HUGE file... must utilize the new "SubRange" capabilities to "trim" the dataset... */
 //        dataurl = "http://nomads.ncep.noaa.gov:9090/dods/nam/nam20110606/nam_00z";
 //        sTime = "2011-06-06T00:00:00Z";
 //        eTime = "2011-06-06T03:00:00Z";
 //        subDims.add("lev");
 //        subIndices.add(new int[]{2,2});
 //        subDims.add("lat");
         /* NAM - FMRC */
 //        dataurl = "http://motherlode.ucar.edu:8080/thredds/dodsC/fmrc/NCEP/NAM/CONUS_12km/NCEP-NAM-CONUS_12km-noaaport_fmrc.ncd";
 //        sTime = "2011-08-05T00:00:00Z";
 //        eTime = "2011-08-06T03:00:00Z";
 //        subDims.add("pressure");
 //        subIndices.add(new int[]{0,0});
 //        subDims.add("pressure1");
 //        subIndices.add(new int[]{0,0});
 //        subDims.add("pressure2");
 //        subIndices.add(new int[]{0,0});
 //        subDims.add("pressure_difference_layer");
 //        subIndices.add(new int[]{0,0});
 //        subDims.add("lat");
 
         /* for HiOOS Gliders */
 //        ncmlmask = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\" location=\"***lochold***\"><variable name=\"pressure\"><attribute name=\"coordinates\" value=\"time longitude latitude depth\"/></variable><variable name=\"temp\"><attribute name=\"coordinates\" value=\"time longitude latitude depth\"/></variable><variable name=\"conductivity\"><attribute name=\"coordinates\" value=\"time longitude latitude depth\"/></variable><variable name=\"salinity\"><attribute name=\"coordinates\" value=\"time longitude latitude depth\"/></variable><variable name=\"density\"><attribute name=\"coordinates\" value=\"time longitude latitude depth\"/></variable></netcdf>";
 //        dataurl = "http://oos.soest.hawaii.edu/thredds/dodsC/hioos/glider/sg139_8/p1390001.nc";
 //        sTime = "";
 //        eTime = "";
 //        /* for HiOOS Gliders Aggregate!!  :-) NOTE: ***lochold*** inside aggregation element and dataurl is to the parent directory, not the dataset */
 //        /* TODO: This appears to be an issue as the 'scan' ncml element doesn't appear to work against remote directories...  need to manually create list!! */
 //        ncmlmask = "<netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\"><aggregation dimName=\"time\" type=\"joinExisting\"><scan location=\"***lochold***\" suffix=\".nc\" /></aggregation></netcdf>";
 //        dataurl = "http://oos.soest.hawaii.edu/thredds/dodsC/hioos/glider/sg139_8/";
 //        sTime = "";
 //        eTime = "";
 
         /* HiOOS HFRADAR */
 //        ncmlmask = "<netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\" location=\"***lochold***\"></netcdf>";
 //        dataurl = "http://oos.soest.hawaii.edu/thredds/dodsC/hioos/hfr/kak/2011/02/RDL_kak_2011_032_0000.nc";
         /* Generic testing */
 //        ncmlmask = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\" location=\"***lochold***\"></netcdf>";
 //        dataurl = "http://thredds1.pfeg.noaa.gov/thredds/dodsC/satellite/GR/ssta/1day";
 //        sTime = "2011-02-01T00:00:00Z";
 //        eTime = "2011-02-02T00:00:00Z";
 //        maxSize = 33554432;//for pfeg ==> all geospatial (1 time) = 32mb
 
         /* CODAR - marcoora */
 //        ncmlmask = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\" location=\"***lochold***\"><attribute name=\"title\" value=\"HFRADAR-CODAR\"/></netcdf>";
 //        dataurl = "http://tashtego.marine.rutgers.edu:8080/thredds/dodsC/cool/codar/totals/macoora6km";
 //        sTime = "2011-03-24T00:00:00Z";
 //        eTime = "2011-03-27T00:00:00Z";
 
         /* UOP - NTAS 1 */
 //        ncmlmask = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\" location=\"***lochold***\"><variable name=\"AIRT\"><attribute name=\"coordinates\" value=\"time depth lat lon\" /></variable><variable name=\"ATMS\"><attribute name=\"coordinates\" value=\"time depth lat lon\" /></variable><variable name=\"RELH\"><attribute name=\"coordinates\" value=\"time depth lat lon\" /></variable><variable name=\"LW\"><attribute name=\"coordinates\" value=\"time depth lat lon\" /></variable><variable name=\"RAIT\"><attribute name=\"coordinates\" value=\"time depth lat lon\" /></variable><variable name=\"TEMP\"><attribute name=\"coordinates\" value=\"time depth lat lon\" /></variable><variable name=\"SW\"><attribute name=\"coordinates\" value=\"time depth lat lon\" /></variable><variable name=\"UWND\"><attribute name=\"coordinates\" value=\"time depth lat lon\" /></variable><variable name=\"VWND\"><attribute name=\"coordinates\" value=\"time depth lat lon\" /></variable><variable name=\"PSAL\"><attribute name=\"coordinates\" value=\"time depth lat lon\" /></variable></netcdf>";
 //        ncmlmask = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\" location=\"***lochold***\"></netcdf>";
 //        ncmlmask = "";
 //        dataurl = "http://uop.whoi.edu/oceansites/ooi/OS_NTAS_2010_R_M-1.nc";
 //        sTime = "2011-05-23T00:00:00Z";
 //        eTime = "2011-05-24T00:00:00Z";
 
         /* UOP - NTAS 2 */
 //        ncmlmask = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\" location=\"***lochold***\"></netcdf>";
 //        ncmlmask = "";
 //        dataurl = "http://uop.whoi.edu/oceansites/ooi/OS_NTAS_2010_R_M-2.nc";
 //        sTime = "2011-05-01T00:00:00Z";
 //        eTime = "2011-05-15T00:00:00Z";
 
         /* UOP - WHOTS 1 */
 //        ncmlmask = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\" location=\"***lochold***\"></netcdf>";
 //        ncmlmask = "";
 //        dataurl = "http://uop.whoi.edu/oceansites/ooi/OS_WHOTS_2010_R_M-1.nc";
 //        sTime = "2011-05-01T00:00:00Z";
 //        eTime = "2011-05-15T00:00:00Z";
 
         /* UOP - WHOTS 2 */
 //        ncmlmask = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\" location=\"***lochold***\"></netcdf>";
 //        ncmlmask = "";
 //        dataurl = "http://uop.whoi.edu/oceansites/ooi/OS_WHOTS_2010_R_M-2.nc";
 //        sTime = "2011-05-01T00:00:00Z";
 //        eTime = "2011-05-15T00:00:00Z";
 
         /* GFS */
 //        ncmlmask = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\" location=\"***lochold***\"><attribute name=\"title\" value=\"NCEP GFS4\"/></netcdf>";
 //        dataurl = "http://nomads.ncdc.noaa.gov/thredds/dodsC/gfs4/201104/20110417/gfs_4_20110417_0600_180.grb2";
 //        sTime = "";//forecast, get it all
 //        eTime = "";//forecast, get it all
         /* HYCOM */
 //        ncmlmask = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\" location=\"***lochold***\"></netcdf>";
 //        dataurl = "/Users/cmueller/Development/JAVA/workspace_nb/eoi-agents/out/ftp/909_archv_agg_1time.ncml";
 //        dataurl = "/Users/cmueller/Development/JAVA/workspace_nb/eoi-agents/out/ftp/909_archv.2011041118_2011041100_idp_EastCst1.nc";
 //        dataurl = "/Users/cmueller/Development/JAVA/workspace_nb/eoi-agents/out/ftp/909_archv.2011041118_2011041100_sal_EastCst1.nc";
 //        dataurl = "/Users/cmueller/Development/JAVA/workspace_nb/eoi-agents/out/ftp/909_archv.2011041118_2011041100_ssh_EastCst1.nc";
 //        dataurl = "/Users/cmueller/Development/JAVA/workspace_nb/eoi-agents/out/ftp/909_archv.2011041118_2011041100_tem_EastCst1.nc";
 //        dataurl = "/Users/cmueller/Development/JAVA/workspace_nb/eoi-agents/out/ftp/909_archv.2011041118_2011041100_uvl_EastCst1.nc";
 //        dataurl = "/Users/cmueller/Development/JAVA/workspace_nb/eoi-agents/out/ftp/909_archv.2011041118_2011041100_vvl_EastCst1.nc";
 
         /* Local testing */
 //        ncmlmask = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\" location=\"***lochold***\"></netcdf>";
 //        dataurl = "/Users/cmueller/Development/JAVA/workspace_nb/eoi-agents/output/usgs/USGS_Test.nc";
 //        sTime = "2011-01-29T00:00:00Z";
 //        eTime = "2011-01-31T00:00:00Z";
 
         /* More Local testing */
 //        ncmlmask = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\" location=\"***lochold***\"><attribute name=\"title\" value=\"NCOM-Sample\"/></netcdf>";
 //        dataurl = "/Users/cmueller/User_Data/Shared_Datasets/NCOM/ncom_glb_scs_2007050700.nc";
 //        sTime = "2007-05-07T00:00:00Z";
 //        eTime = "2007-05-09T00:00:00Z";
 
         /* CGSN test */
 //        ncmlmask = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\" location=\"***lochold***\"><variable name=\"stnId\" shape=\"\" type=\"int\"><attribute name=\"standard_name\" value=\"station_id\"/><values>1</values></variable></netcdf>";
 //        ncmlmask = "";
 //        uname = "cgsn";
 //        pass = "ISMT2!!";
 //        dataurl = "http://ooi.coas.oregonstate.edu:8080/thredds/dodsC/OOI/ISMT2/ISMT2_Timing.nc";
 //        dataurl = "http://ooi.coas.oregonstate.edu:8080/thredds/dodsC/OOI/ISMT2/ISMT2_SBE16.nc";
 //        dataurl = "http://ooi.coas.oregonstate.edu:8080/thredds/dodsC/OOI/ISMT2/ISMT2_Motion.nc";
 //        dataurl = "http://ooi.coas.oregonstate.edu:8080/thredds/dodsC/OOI/ISMT2/ISMT2_Iridium.nc";
 //        dataurl = "http://ooi.coas.oregonstate.edu:8080/thredds/dodsC/OOI/ISMT2/ISMT2_ECO-VSF.nc";
 //        dataurl = "http://ooi.coas.oregonstate.edu:8080/thredds/dodsC/OOI/ISMT2/ISMT2_ECO-DFL.nc";
 //        dataurl = "http://ooi.coas.oregonstate.edu:8080/thredds/dodsC/OOI/ISMT2/ISMT2_CR1000.nc";
 
         /* Rutgers ROMS */
 //        ncmlmask = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\" location=\"***lochold***\"></netcdf>";
 //        dataurl = "http://tashtego.marine.rutgers.edu:8080/thredds/dodsC/roms/espresso/2009_da/his";
         /* NDBC HFRADAR */
 //        ncmlmask = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\" location=\"***lochold***\"><attribute name=\"data_url\" value=\"http://sdf.ndbc.noaa.gov/thredds/dodsC/hfradar_usegc_6km\"/><attribute name=\"CF:featureType\" value=\"grid\"/></netcdf>";
 //        dataurl = "http://sdf.ndbc.noaa.gov/thredds/dodsC/hfradar_usegc_6km";
 //        sTime = "2011-08-02T16:00:00Z";
 //        eTime = "2011-08-02T18:09:00Z";
         /** **************************** */
         /*  Dynamic DAP Request Testing  */
         /** **************************** */
         /* MODIS A test (pull 10 minutes of data -- 2 files) */
 //        requestType = net.ooici.services.sa.DataSource.RequestType.FTP;
 //        ncmlmask = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\" location=\"***lochold***\"><attribute name=\"title\" value=\"NCEP GFS3\"/></netcdf>";
 //        sTime = "2011-04-20T05:50:00Z";
 //        eTime = "2011-04-20T18:10:00Z";
 //        baseUrl = "http://nomads.ncdc.noaa.gov/thredds/dodsC/gfs-hi/";
 //        dirPattern = "%yyyy%%MM%/%yyyy%%MM%%dd%/";
 //        filePattern = "gfs_3_%yyyy%%MM%%dd%_%HH%%mm%_[\\d]{3}\\.grb";
 //        joinName = "time";
         /*
          * 
         Example:        http://nomads.ncdc.noaa.gov/thredds/dodsC/gfs-hi/201104/20110422/gfs_3_20110422_1800_180.grb
         NCML Mask:      <?xml version=\"1.0\" encoding=\"UTF-8\"?><netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\" location=\"***lochold***\"><attribute name=\"title\" value=\"NCEP GFS4\"/></netcdf>
         dir_pattern:    
         file_pattern:   
         Base URL:       
         Base Dir:       
          */
         /* GFS 3 test */
 //        requestType = net.ooici.services.sa.DataSource.RequestType.DAP;
 //        ncmlmask = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\" enhance=\"true\"><attribute name=\"title\" value=\"NCEP GFS3\"/>***fmrchold***</netcdf>";
 //        sTime = "2011-08-08T00:00:00Z";
 //        eTime = "2011-08-09T00:00:00Z";
 //        baseUrl = "http://nomads.ncdc.noaa.gov/thredds/dodsC/gfs-hi/";
 //        dirPattern = "%yyyy%%MM%/%yyyy%%MM%%dd%/";
 ////        filePattern = "gfs_3_%yyyy%%MM%%dd%_%HH%%mm%_[\\d]{3}\\.grb";
 //        filePattern = "gfs_3_%yyyy%%MM%%dd%_0000_[\\d]{3}\\.grb";
 //        joinName = "_%HHH%.grb";
         /** ******************** */
         /*  FTP Request Testing  */
         /** ******************** */
         requestType = net.ooici.services.sa.DataSource.RequestType.FTP;
         sTime = "2011-08-10T21:14:47Z";
         eTime = "2011-08-11T21:14:47Z";
         baseUrl = "ftp://ftp7300.nrlssc.navy.mil/pub/smedstad/ROMS/";
         ncmlmask = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\"><attribute name=\"institution\" value=\"NAVY NRL-SSC\"/><aggregation dimName=\"runtime\" type=\"forecastModelRunCollection\"><scanFmrc location=\"***lochold***\" suffix=\".nc\" subdirs=\"false\" runDateMatcher=\"#909_archv.#yyyyMMddHH\" forecastDateMatcher=\"#_#yyyyMMddHH#_#\"/></aggregation></netcdf>";
         dirPattern = "";
         filePattern = "909_archv.%yyyy%%MM%%dd%%HH%_[0-9]+_[a-zA-Z]+_EastCst1.nc";
         joinName = "MT";
 
 
 
         /* MODIS A test (pull 10 minutes of data -- 2 files) */
 //         requestType = net.ooici.services.sa.DataSource.RequestType.FTP;
 //         sTime = "2011-04-20T12:00:00Z";
 //         eTime = "2011-04-20T12:10:00Z";
 //         baseUrl = "ftp://podaac.jpl.nasa.gov/allData/ghrsst/data/L2P/MODIS_A/JPL/";
 //         dirPattern = "%yyyy%/%DDD%/";
 //         filePattern = "%yyyy%%MM%%dd%-MODIS_A-JPL-L2P-A%yyyy%%DDD%%HH%%mm%%ss%\\.L2_LAC_GHRSST_[a-zA-Z]-v01\\.nc\\.bz2";
 //         joinName = "time";
         /*
         dir_pattern:    "%yyyy%/%DDD%/"
         file_pattern:   "%yyyy%%MM%%dd%-MODIS_A-JPL-L2P-A%yyyy%%DDD%%HH%%mm%%ss%\\.L2_LAC_GHRSST_[a-zA-Z]-v01\\.nc\\.bz2"
         Base URL:       ftp://podaac.jpl.nasa.gov
         Base Dir:       ./allData/ghrsst/data/L2P/MODIS_A/JPL/
          */
 
         /* MODIS T test (pull 10 minutes of data -- 2 files) */
 //         requestType = net.ooici.services.sa.DataSource.RequestType.FTP;
 //         sTime = "2011-04-20T12:00:00Z";
 //         eTime = "2011-04-20T12:10:00Z";
 //         baseUrl = "ftp://podaac.jpl.nasa.gov/allData/ghrsst/data/L2P/MODIS_T/JPL/";
 //         dirPattern = "%yyyy%/%DDD%/";
 //         filePattern = "%yyyy%%MM%%dd%-MODIS_T-JPL-L2P-T%yyyy%%DDD%%HH%%mm%%ss%\\.L2_LAC_GHRSST_[a-zA-Z]-v01\\.nc\\.bz2";
 //         joinName = "time";
 //         ncmlmask = "<netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\">\n   <variable name=\"lat\">\n      <attribute name=\"moto\" type=\"string\" value=\"GO TEAM!\" />\n   </variable>\n   <variable name=\"lon\">\n      <attribute name=\"moto\" type=\"string\" value=\"GO TEAM!\" />\n   </variable>\n</netcdf>\n";
         /*
         dir_pattern:    "%yyyy%/%DDD%/"
         file_pattern:   "%yyyy%%MM%%dd%-MODIS_A-JPL-L2P-A%yyyy%%DDD%%HH%%mm%%ss%\\.L2_LAC_GHRSST_[a-zA-Z]-v01\\.nc\\.bz2"
         Base URL:       ftp://podaac.jpl.nasa.gov
         Base Dir:       ./allData/ghrsst/data/L2P/MODIS_A/JPL/
          */
 
         /* OSTIA test (pull 2 days of data -- 2 files) */
 //         requestType = net.ooici.services.sa.DataSource.RequestType.FTP;
 //         sTime = "2011-04-20T12:30:00Z";
 //         eTime = "2011-04-21T12:30:00Z";
 //         baseUrl = "ftp://podaac.jpl.nasa.gov/allData/ghrsst/data/L4/GLOB/UKMO/OSTIA/";
 //         dirPattern = "%yyyy%/%DDD%/";
 //         filePattern = "%yyyy%%MM%%dd%-UKMO-L4HRfnd-GLOB-v01-fv02-OSTIA\\.nc\\.bz2";
 //         joinName = "time";
 
         /*
         Base URL:       ftp://podaac.jpl.nasa.gov
         Base Dir:       /allData/ghrsst/data/L4/GLOB/UKMO/OSTIA
         Native Format:  .nc.bz2
         dir_pattern:    "%yyyy%/%DDD%/"
         file_pattern:   "%yyyy%%MM%%dd%-UKMO-L4HRfnd-GLOB-v01-fv02-OSTIA\\.nc\\.bz2"
         join_dimension: "time"
          */
         /* AVHRR19_L test (pull 15 mins of data -- ~2 files) */
 //        requestType = net.ooici.services.sa.DataSource.RequestType.FTP;
 //        sTime = "2011-01-09T04:25:00Z";
 //        eTime = "2011-01-09T04:40:00Z";
 //        baseUrl = "ftp://podaac.jpl.nasa.gov/allData/ghrsst/data/L2P/AVHRR19_L/NAVO/";
 //        dirPattern = "%yyyy%/%DDD%/";
 //        filePattern = "%yyyy%%MM%%dd%-AVHRR19_L-NAVO-L2P-SST_s%HH%%mm%_e[0-9]{4}-v01\\.nc\\.bz2";
 //        joinName = "time";
 
         /*
         Base URL:      ftp://podaac.jpl.nasa.gov
         Base Dir:      /allData/ghrsst/data/L2P/AVHRR19_L/NAVO/
         Native Format:  .nc.bz2
         dir_pattern:    "%yyyy%/%DDD%/"
         file_pattern:   "%yyyy%%MM%%dd%-AVHRR19_L-NAVO-L2P-SST_s%HH%%mm%_e[0-9]{4}-v01\\.nc\\.bz2"
         join_dimension: "time"
         
         %yyyy%%MM%%dd%-AVHRR19_L-NAVO-L2P-SST_s%HH%%mm%_e[0-9]{4}-v01\\.nc\\.bz2
         2011  01  09 -AVHRR19_L-NAVO-L2P-SST_s 01  01 _e0109-v01.nc.bz2
         
          */
         /* AVHRR_METOP_A test */
 //        requestType = net.ooici.services.sa.DataSource.RequestType.FTP;
 //        sTime = "2011-05-22T04:30:00Z";
 //        eTime = "2011-05-22T04:40:00Z";
 //        baseUrl = "ftp://podaac-ftp.jpl.nasa.gov/allData/ghrsst/data/L2P/AVHRR_METOP_A/EUR/";
 //        dirPattern = "%yyyy%/%DDD%/";
 //        filePattern = "%yyyy%%MM%%dd%-EUR-L2P_GHRSST-SSTsubskin-AVHRR_METOP_A-eumetsat_sstmgr_metop02_%yyyy%%MM%%dd%_%HH%%mm%%ss%-v01\\.7-fv01.0\\.nc\\.bz2";
 //        joinName = "time";
         
         
         
         
         
         List<GPBWrapper<?>> addlObjects = new ArrayList<GPBWrapper<?>>();
         net.ooici.services.sa.DataSource.EoiDataContextMessage.Builder cBldr = net.ooici.services.sa.DataSource.EoiDataContextMessage.newBuilder();
         net.ooici.services.sa.DataSource.SourceType sourceType = net.ooici.services.sa.DataSource.SourceType.NETCDF_S;
         cBldr.setIsInitial(true);
         cBldr.setSourceType(sourceType);
         cBldr.setRequestType(requestType);
         cBldr.setDatasetUrl(dataurl).setNcmlMask(ncmlmask).setBaseUrl(baseUrl);
         cBldr.setDatasetId("TESTDATASET");
         try {
             if (sTime != null && !sTime.isEmpty()) {
                 long st = AgentUtils.ISO8601_DATE_FORMAT.parse(sTime).getTime();
                 cBldr.setStartDatetimeMillis(st);
             }
             if (eTime != null && !eTime.isEmpty()) {
                 long et = AgentUtils.ISO8601_DATE_FORMAT.parse(eTime).getTime();
                 cBldr.setEndDatetimeMillis(et);
             }
         } catch (ParseException ex) {
             throw new IOException("Error parsing time strings", ex);
         }
         if (uname != null && pass != null) {
             /* Add ThreddsAuthentication */
             net.ooici.services.sa.DataSource.ThreddsAuthentication tdsAuth = net.ooici.services.sa.DataSource.ThreddsAuthentication.newBuilder().setName(uname).setPassword(pass).build();
             GPBWrapper tdsWrap = GPBWrapper.Factory(tdsAuth);
             addlObjects.add(tdsWrap);
             cBldr.setAuthentication(tdsWrap.getCASRef());
         }
         if (null != dirPattern && null != filePattern && null != joinName) {
             /* Add SearchPattern */
             net.ooici.services.sa.DataSource.SearchPattern pattern = null;
             net.ooici.services.sa.DataSource.SearchPattern.Builder patternBldr = net.ooici.services.sa.DataSource.SearchPattern.newBuilder();
 
             patternBldr.setDirPattern(dirPattern);
             patternBldr.setFilePattern(filePattern);
             patternBldr.setJoinName(joinName);
 
             pattern = patternBldr.build();
 
             GPBWrapper<?> patternWrap = GPBWrapper.Factory(pattern);
             addlObjects.add(patternWrap);
             cBldr.setSearchPattern(patternWrap.getCASRef());
         }
         if (!subDims.isEmpty() && !subIndices.isEmpty() && subDims.size() == subIndices.size()) {
             DataSource.SubRange sr;
             int[] indices;
             for (int i = 0; i < subIndices.size(); i++) {
                 indices = subIndices.get(i);
                 sr = DataSource.SubRange.newBuilder().setDimName(subDims.get(i)).setStartIndex(indices[0]).setEndIndex(indices[1]).build();
                 addlObjects.add(GPBWrapper.Factory(sr));
                 cBldr.addSubRanges(sr);
             }
         }
         net.ooici.core.container.Container.Structure struct = AgentUtils.getUpdateInitStructure(GPBWrapper.Factory(cBldr.build()), addlObjects.toArray(new GPBWrapper<?>[]{}));
 //        runAgent(struct, AgentRunType.TEST_WRITE_NC);
         runAgent(struct, AgentRunType.TEST_WRITE_OOICDM);
     }
 
     private static String[] runAgent(net.ooici.core.container.Container.Structure structure, AgentRunType agentRunType) throws IOException {
         net.ooici.eoi.datasetagent.IDatasetAgent agent = net.ooici.eoi.datasetagent.AgentFactory.getDatasetAgent(net.ooici.services.sa.DataSource.SourceType.NETCDF_S);
         agent.setAgentRunType(agentRunType);
 
         /* Set the maximum size for retrieving/sending - default is 5mb */
 //        agent.setMaxSize(1048576);//1mb
 //        agent.setMaxSize(67874688);//~64mb
 //        agent.setMaxSize(30000);//pretty small
 //        agent.setMaxSize(1500);//very small
 //        agent.setMaxSize(150);//super small
 
 //        agent.setMaxSize(maxSize);//ds defined
 
 //        java.util.HashMap<String, String> connInfo = new java.util.HashMap<String, String>();
 //        connInfo.put("exchange", "eoitest");
 //        connInfo.put("service", "eoi_ingest");
 //        connInfo.put("server", "localhost");
 //        connInfo.put("topic", "magnet.topic");
         java.util.HashMap<String, String> connInfo = null;
         try {
             connInfo = IonUtils.parseProperties();
         } catch (IOException ex) {
             log.error("Error parsing \"ooici-conn.properties\" cannot continue.", ex);
             System.exit(1);
         }
         String[] result = agent.doUpdate(structure, connInfo);
         for (String s : result) {
             if (log.isDebugEnabled()) {
                 log.debug(s);
             }
         }
         return result;
     }
 }
