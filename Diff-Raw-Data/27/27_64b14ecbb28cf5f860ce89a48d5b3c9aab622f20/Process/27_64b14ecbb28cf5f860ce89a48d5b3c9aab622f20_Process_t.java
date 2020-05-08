 package gov.usgs.cida.gdp.coreprocessing;
 
 import gov.usgs.cida.gdp.utilities.FileHelper;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Formatter;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.AddressException;
 
 
 import ucar.nc2.constants.FeatureType;
 import ucar.nc2.dt.grid.GridDataset;
 import ucar.nc2.ft.FeatureDataset;
 import ucar.nc2.ft.FeatureDatasetFactoryManager;
 import ucar.nc2.ft.FeatureDatasetPoint;
 
 import gov.usgs.cida.gdp.communication.EmailHandler;
 import gov.usgs.cida.gdp.communication.bean.EmailMessage;
 import gov.usgs.cida.gdp.coreprocessing.writer.CSVWriter;
 import gov.usgs.cida.gdp.utilities.HTTPUtils;
 import org.geotools.feature.FeatureCollection;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import ucar.nc2.NetcdfFile;
 
 public class Process {
 
     static {
         try {
             NetcdfFile.registerIOProvider(ucar.nc2.iosp.geotiff.GeoTiffIOServiceProvider.class);
         } catch (Exception e) {
         }
     }
 
     public static String process(ProcessInputs inputs) throws Exception {
 
         // Check for upload directory. If not found, return null
         // TODO- return error
        String uploadDirectoryPath = System.getProperty("applicationWorkDir");
         File uploadDirectory = FileHelper.createFileRepositoryDirectory(uploadDirectoryPath);
 
         DelimiterOption delimiterOption = null;
         try {
            // TODO
            inputs.delimId = "[comma]";
             delimiterOption = DelimiterOption.valueOf(inputs.delimId);
         } catch (IllegalArgumentException e) {
             delimiterOption = DelimiterOption.getDefault();
         }
 
         String dataTypes[];
         File wcsCoverage;
         if ("WCS".equals(inputs.dataSetInterface)) {
             wcsCoverage = wcs(inputs.wcsURL, inputs.wcsCoverage,
                     inputs.wcsBoundingBox, inputs.wcsGridCRS, inputs.wcsGridOffsets,
                     inputs.wcsResampleFilter, inputs.wcsResampleFactor);
 
             dataTypes = new String[] { "I0B0" };
         } else { // THREDDS
            // Passing in String[0] forces toArray to allocate a new array. This
            // is how the javadocs say to do it.
            dataTypes = inputs.threddsDataTypes.toArray(new String[0]);
         }
 
        String[] outputStats = inputs.outputStats.toArray(new String[0]);

         DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
         df.setTimeZone(TimeZone.getTimeZone("UTC"));
 
         Date fromDate, toDate;
         try{
             toDate = df.parse(inputs.threddsToDate);
             fromDate = df.parse(inputs.threddsFromDate);
         } catch (ParseException e1) {
             throw new Exception("Unable to parse dates. Must be in 'yyyy-MM-dd' format");
         }
 
         Formatter errorLog = new Formatter();
         FeatureDataset featureDataset = FeatureDatasetFactoryManager.open(
                FeatureType.ANY, inputs.threddsDataset, null, errorLog);
 
         try {
 
             FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = null;
            String outputFile = ""; //Integer.toString(inputs.hashCode());
 
             if (featureDataset.getFeatureType() == FeatureType.GRID && featureDataset instanceof GridDataset) {
                 boolean categorical = false;
                 CSVWriter.grid(featureDataset, categorical,
                         featureCollection, inputs.attribute, delimiterOption,
                         fromDate, toDate, dataTypes, inputs.threddsGroupBy,
                        outputStats, outputFile);
 
             } else if (featureDataset.getFeatureType() == FeatureType.STATION && featureDataset instanceof FeatureDatasetPoint) {
                 CSVWriter.station(featureDataset, featureCollection,
                         fromDate, toDate, delimiterOption, dataTypes,
                         inputs.threddsGroupBy, outputFile);
             }
         } catch (Exception ex) {
             throw ex;
         }
 
         // Move completed file to the upload repository
         FileHelper.copyFileToFile(
                 new File(System.getProperty("applicationWorkDir")
                 + inputs.outputFile), uploadDirectory.getPath(), true);
 
         File outputDataFile = new File(uploadDirectory.getPath(),
                 inputs.outputFile);
         if (!outputDataFile.exists()) {
             throw new Exception("Unable to create output file.");
         }
 
         String outputFileURL = "";
         sendEmail(inputs.email, outputFileURL);
 
         return outputDataFile.getName();
     }
 
     private static File wcs(String wcsServer, String wcsCoverage,
             String wcsBoundingBox, String wcsGridCRS, String wcsGridOffsets,
             String wcsResmapleFilter, String wcsResampleFactor)
             throws MalformedURLException, IOException {
 
         int wcsRequestHashCode = 7;
         wcsRequestHashCode = 31 * wcsRequestHashCode
                 + (wcsServer == null ? 0 : wcsServer.hashCode());
         wcsRequestHashCode = 31 * wcsRequestHashCode
                 + (wcsCoverage == null ? 0 : wcsCoverage.hashCode());
         wcsRequestHashCode = 31 * wcsRequestHashCode
                 + (wcsBoundingBox == null ? 0 : wcsBoundingBox.hashCode());
         wcsRequestHashCode = 31 * wcsRequestHashCode
                 + (wcsGridCRS == null ? 0 : wcsGridCRS.hashCode());
         wcsRequestHashCode = 31 * wcsRequestHashCode
                 + (wcsGridOffsets == null ? 0 : wcsGridOffsets.hashCode());
         wcsRequestHashCode = 31
                 * wcsRequestHashCode
                 + (wcsResmapleFilter == null ? 0 : wcsResmapleFilter.hashCode());
         wcsRequestHashCode = 31
                 * wcsRequestHashCode
                 + (wcsResampleFactor == null ? 0 : wcsResampleFactor.hashCode());
         File wcsRequestOutputFile = new File(
                 System.getProperty("applicationWorkDir"),
                 Integer.toHexString(wcsRequestHashCode) + ".tiff");
         FileOutputStream fos = null;
         InputStream coverageIStream = null;
         try {
             // Create WCS request
             String getCoverageRequest = wcsServer + "?service=WCS"
                     + "&version=1.1.1" + "&request=GetCoverage"
                     + "&identifier=" + wcsCoverage + "&boundingBox="
                     + wcsBoundingBox + "," + wcsGridCRS + "&gridBaseCRS="
                     + wcsGridCRS + "&gridOffsets=" + wcsGridOffsets
                     + "&format=image/GeoTIFF";
             // Call getCoverage
             HttpURLConnection httpConnection = HTTPUtils.openHttpConnection(
                     new URL(getCoverageRequest), "GET");
             coverageIStream = HTTPUtils.getHttpConnectionInputStream(httpConnection);
             Map<String, List<String>> headerFields = HTTPUtils.getHttpConnectionHeaderFields(httpConnection);
             // TODO: check for error response from server
             String boundaryString = null;
             String[] contentType = headerFields.get("Content-Type").get(0).split(" *; *");
             for (int i = 0; i < contentType.length; i++) {
                 String[] field = contentType[i].split("=");
                 if ("boundary".equals(field[0])) {
                     boundaryString = field[1].substring(1,
                             field[1].length() - 1); // remove quotes
                 }
             }
             if (boundaryString == null) {
                 // TODO: probably change exception thrown
                 throw new IOException();
             }
             int part = 0;
             // find second part (coverage) of multi-part response
             while (part < 2) {
                 String s = readLine(coverageIStream);
                 if (s == null) {
                     throw new IOException();
                 }
                 if (("--" + boundaryString).equals(s)) {
                     part++;
                 }
             }
             // actual coverage starts immediately after blank line
             String line;
             do {
                 line = readLine(coverageIStream);
             } while (!"".equals(line));
             // write coverage bytes to file
             fos = new FileOutputStream(wcsRequestOutputFile);
             String closeTag = "--" + boundaryString + "--\n";
             byte[] b = new byte[1024];
             int bytesRead;
             while ((bytesRead = coverageIStream.read(b, 0, 1024)) > 0) {
                 String endString = new String(Arrays.copyOfRange(b, bytesRead
                         - closeTag.length(), bytesRead));
                 if (closeTag.equals(endString)) {
                     // Don't write close tag to file
                     fos.write(b, 0, bytesRead - closeTag.length());
                     break;
                 }
                 fos.write(b, 0, bytesRead);
             }
         } finally {
             if (fos != null) {
                 try {
                     fos.close();
                 } catch (IOException e) {
                     /* don't care, unrecoverable */
                 }
             }
             if (coverageIStream != null) {
                 try {
                     coverageIStream.close();
                 } catch (IOException e) {
                     /* don't care, unrecoverable */
                 }
             }
         }
 
         return wcsRequestOutputFile;
 
     }
 
     private static String readLine(InputStream is) throws IOException {
 
         StringBuilder sb = new StringBuilder();
 
         int b;
         while ((b = is.read()) != -1) {
 
             char c = (char) b; // TODO: convert properly
 
             if (c == '\n') {
                 return sb.toString();
             } else {
                 sb.append(c);
             }
         }
 
         return null; // new line not found
     }
 
     private static void sendEmail(String email, String finalUrlEmail)
             throws AddressException, MessagingException {
 
         String from = "gdp_data@usgs.gov";
         String subject = "Your file is ready";
         String content = "Your file is ready: " + finalUrlEmail;
         EmailMessage emBean = new EmailMessage(from, email,
                 new ArrayList<String>(), subject, content);
         
         EmailHandler.sendMessage(emBean);
     }
 }
