 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 package org.geoserver.wfs.response;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.geotools.util.logging.Logging;
 
 /**
  * Helper used to invoke ogr2ogr
  * 
  * @author Andrea Aime - OpenGeo
  * 
  */
 public class OGRWrapper {
 
     private static final Logger LOGGER = Logging.getLogger(OGRWrapper.class);
 
     private String ogrExecutable;
     private String gdalData;
 
     public OGRWrapper(String ogrExecutable, String gdalData) {
         this.ogrExecutable = ogrExecutable;
         this.gdalData = gdalData;
     }
 
     public void convert(File inputData, File outputDirectory, String typeName,
             OgrFormat format, String crsName) throws IOException, InterruptedException {
         // build the command line
         List<String> cmd = new ArrayList<String>();
         cmd.add(ogrExecutable);
         cmd.add("-f");
        cmd.add("\"" + format.ogrFormat + "\"");
         if (crsName != null) {
             cmd.add("-a_srs");
             cmd.add(crsName);
         }
         if (format.options != null) {
             for (String option : format.options) {
                 cmd.add(option);
             }
         }
         String outFileName = typeName;
         if (format.fileExtension != null)
             outFileName += format.fileExtension;
         cmd.add(new File(outputDirectory, outFileName).getAbsolutePath());
         cmd.add(inputData.getAbsolutePath());
 
         StringBuilder sb = new StringBuilder();
         int exitCode = run(cmd, sb);
 
         if (exitCode != 0)
             throw new IOException("ogr2ogr did not terminate successfully, exit code " + exitCode
                     + ". Was trying to run: " + cmd + "\nResulted in:\n" + sb);
     }
 
     /**
      * Returns a list of the ogr2ogr supported formats
      * 
      * @return
      */
     public Set<String> getSupportedFormats() {
         try {
             List<String> commands = new ArrayList<String>();
             commands.add(ogrExecutable);
             commands.add("--help");
 
             StringBuilder sb = new StringBuilder();
             // can't trust the exit code, --help exits with -1 on my pc
             run(commands, sb);
 
             Set<String> formats = new HashSet<String>();
             String[] lines = sb.toString().split("\n");
             for (String line : lines) {
                 if (line.matches("\\s*-f \".*")) {
                     String format = line.substring(line.indexOf('"') + 1, line.lastIndexOf('"'));
                     formats.add(format);
                 }
             }
 
             return formats;
         } catch (Exception e) {
             LOGGER.log(Level.SEVERE,
                     "Could not get the list of output formats supported by ogr2ogr", e);
             return Collections.emptySet();
         }
     }
 
     /**
      * Returns true if ogr2ogr is available, that is, if executing
      * "ogr2ogr --version" returns 0 as the exit code
      * 
      * @return
      */
     public boolean isAvailable() {
         List<String> commands = new ArrayList<String>();
         commands.add(ogrExecutable);
         commands.add("--version");
 
         try {
             return run(commands, null) == 0;
         } catch(Exception e) {
             LOGGER.log(Level.SEVERE, "Ogr2Ogr is not available", e);
             return false;
         }
     }
 
     /**
      * Runs the specified command appending the output to the string builder and
      * returning the exit code
      * 
      * @param cmd
      * @param sb
      * @return
      * @throws IOException
      * @throws InterruptedException
      */
     int run(List<String> cmd, StringBuilder sb) throws IOException, InterruptedException {
         // run the process and grab the output for error reporting purposes
         ProcessBuilder builder = new ProcessBuilder(cmd);
         if(gdalData != null)
             builder.environment().put("GDAL_DATA", gdalData);
         builder.redirectErrorStream(true);
         Process p = builder.start();
         BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
         String line = null;
         while ((line = reader.readLine()) != null) {
             if (sb != null) {
                 sb.append("\n");
                 sb.append(line);
             }
         }
         return p.waitFor();
     }
 }
