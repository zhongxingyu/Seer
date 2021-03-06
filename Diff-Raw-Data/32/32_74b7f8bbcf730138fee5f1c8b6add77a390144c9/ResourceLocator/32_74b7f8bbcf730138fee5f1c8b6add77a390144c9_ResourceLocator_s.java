 /*
  * Copyright (c) 2007-2012 The Broad Institute, Inc.
  * SOFTWARE COPYRIGHT NOTICE
  * This software and its documentation are the copyright of the Broad Institute, Inc. All rights are reserved.
  *
  * This software is supplied without any warranty or guaranteed support whatsoever. The Broad Institute is not responsible for its use, misuse, or functionality.
  *
  * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
  * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
  */
 package org.broad.igv.util;
 
 import org.apache.log4j.Logger;
 import org.broad.igv.gs.GSUtils;
 
 import java.awt.*;
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Map;
 
 /**
  * Represents a data file or other resource, which might be local file or remote resource.
  *
  * @author jrobinso
  */
 public class ResourceLocator {
 
     private static Logger log = Logger.getLogger(ResourceLocator.class);
 
     /**
      * Display name
      */
     String name;
 
     /**
      * The local path or url (http, https, or ftp) for the resource.
      */
     String path;
 
     /**
      * URL to a database server
      */
     String dbURL;
 
 
     /**
      * Optional path to an associated index file
      */
     String indexPath;
 
 
     String trackInforURL; // A hyperlink to general information about the track.
 
     String featureInfoURL; //A URL pattern (UCSC convention) to a specific URL applicable to each feature
 
     String description; //Descriptive text
 
     /**
      * The type of resource (generally this refers to the file format)
      */
     String type;
 
     /**
      * Path to an associated density file.  This is used primarily for sequence alignments
      */
     String coverage;
 
     /**
      * A UCSC style track line.  Overrides value in file, if any.
      */
     String trackLine;  //
 
     /**
      * Color for features or data.  Somewhat redundant with trackLine.
      */
     Color color;
 
 
     String sampleId;
 
     String username;
 
     String password;
 
     /**
      * Constructor for local files
      *
      * @param path
      */
     public ResourceLocator(String path) {
         this.setPath(path);
     }
 
     /**
      * Constructor for database resources
      *
      * @param dbURL
      * @param path
      */
     public ResourceLocator(String dbURL, String path) {
         this.dbURL = dbURL;
         this.setPath(path);
     }
 
     /**
      * Determines if the resource actually exists.
      *
      * @return true if resource was found.
      */
     public boolean exists() {
         return ParsingUtils.pathExists(path);
     }
 
 
     public void setType(String type) {
         this.type = type;
     }
 
     public String getType() {
         return type;
     }
 
     public String getTypeString() {
         if (type != null) {
             return type;
         } else {
 
             String typeString = path.toLowerCase();
             if (path.startsWith("http://") || path.startsWith("https://")) {
                 try {
                     URL url = new URL(path);
 
                     typeString = url.getPath().toLowerCase();
                     String query = url.getQuery();
                     if (query != null) {
                         Map<String, String> queryMap = HttpUtils.parseQueryString(query);
                         // If type is set explicitly use it
                        if (queryMap.containsKey("dataformat")) {
                            String format = queryMap.get("dataformat");
                             if (format.contains("genomespace")) {
                                 typeString = GSUtils.parseDataFormatString(format);
                             } else {
                                 typeString = format;
                             }
                        } else if (queryMap.containsKey("file")) {
                            typeString = queryMap.get("file");
                         }
                     }
 
                 } catch (MalformedURLException e) {
                     log.error("Error interpreting url: " + path, e);
                     typeString = path;
                 }
             }
 
             // Strip .txt, .gz, and .xls extensions.  (So  foo.cn.gz => a .cn file)
             if (!typeString.endsWith("_sorted.txt") &&
                     (typeString.endsWith(".txt") || typeString.endsWith(
                             ".xls") || typeString.endsWith(".gz"))) {
                 typeString = typeString.substring(0, typeString.lastIndexOf("."));
             }
 
             return typeString;
 
         }
     }
 
     public String toString() {
         return path + (dbURL == null ? "" : " " + dbURL);
     }
 
     public String getPath() {
         return path;
     }
 
     public String getFileName() {
         return (new File(path)).getName();
     }
 
 
     public String getDBUrl() {
         return dbURL;
     }
 
     public boolean isLocal() {
         return !(FileUtils.isRemote(path));
     }
 
     public void setTrackInforURL(String trackInforURL) {
         this.trackInforURL = trackInforURL;
     }
 
     public String getTrackInforURL() {
         return trackInforURL;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public String getTrackName() {
         return name != null ? name : new File(getPath()).getName();
     }
 
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getName() {
         return name;
     }
 
     public String getCoverage() {
         return coverage;
     }
 
     public void setCoverage(String coverage) {
         this.coverage = coverage;
     }
 
     public Color getColor() {
         return color;
     }
 
     public void setColor(Color color) {
         this.color = color;
     }
 
 
     public String getFeatureInfoURL() {
         return featureInfoURL;
     }
 
     public void setFeatureInfoURL(String featureInfoURL) {
         this.featureInfoURL = featureInfoURL;
     }
 
     public void setPath(String path) {
         if (path != null && path.startsWith("file://")) {
             this.path = path.substring(7);
         } else {
             this.path = path;
         }
     }
 
     public String getTrackLine() {
         return trackLine;
     }
 
     public void setTrackLine(String trackLine) {
         this.trackLine = trackLine;
     }
 
     public String getSampleId() {
         return sampleId;
     }
 
     public void setSampleId(String sampleId) {
         this.sampleId = sampleId;
     }
 
     public String getIndexPath() {
         return indexPath;
     }
 
     public void setIndexPath(String indexPath) {
         this.indexPath = indexPath;
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         ResourceLocator that = (ResourceLocator) o;
 
         if (dbURL != null ? !dbURL.equals(that.dbURL) : that.dbURL != null) return false;
         if (!path.equals(that.path)) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = path.hashCode();
         result = 31 * result + (dbURL != null ? dbURL.hashCode() : 0);
         return result;
     }
 
 
     public String getBamIndexPath() {
 
         if (indexPath != null) return indexPath;
 
         if (path.toLowerCase().startsWith("http://") || path.toLowerCase().startsWith("https://")) {
             // See if bam file is specified by parameter
             try {
                 URL url = new URL(path);
                 String queryString = url.getQuery();
                 if (queryString != null) {
                     Map<String, String> parameters = HttpUtils.parseQueryString(queryString);
                     if (parameters.containsKey("index")) {
                         return parameters.get("index");
                     } else if (parameters.containsKey("file")) {
                         String bamFile = parameters.get("file");
                         String bamIndexFile = bamFile + ".bai";
                         String newQueryString = queryString.replace(bamFile, bamIndexFile);
                         return path.replace(queryString, newQueryString);
                     }
                 }
             } catch (MalformedURLException e) {
                 log.error(e);
             }
 
         }
 
         return path + ".bai";
     }
 
 
     /**
      * FOR LOAD FROM SERVER
      */
     public static enum AttributeType {
 
         DB_URL("serverURL"),
         PATH("path"),
         DESCRIPTION("description"),
         HYPERLINK("hyperlink"),
         INFOLINK("infolink"),
         ID("id"),
         SAMPLE_ID("sampleId"),
         NAME("name"),
         URL("url"),
         RESOURCE_TYPE("resourceType"),
         TRACK_LINE("trackLine"),
         COVERAGE("coverage"),
         COLOR("color");
 
         private String name;
 
         AttributeType(String name) {
             this.name = name;
         }
 
         public String getText() {
             return name;
         }
 
         @Override
         public String toString() {
             return getText();
         }
 
     }
 }
