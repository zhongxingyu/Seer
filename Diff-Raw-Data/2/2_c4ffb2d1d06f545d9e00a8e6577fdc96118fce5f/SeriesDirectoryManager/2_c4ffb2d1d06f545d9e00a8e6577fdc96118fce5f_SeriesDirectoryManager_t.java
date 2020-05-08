 /**
  * Copyright (C) 2011 (nick @ objectdefinitions.com)
  *
  * This file is part of JTimeseries.
  *
  * JTimeseries is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * JTimeseries is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with JTimeseries.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.od.jtimeseries.server;
 
 import com.od.jtimeseries.context.TimeSeriesContext;
 import com.od.jtimeseries.server.serialization.FileHeader;
 import com.od.jtimeseries.server.serialization.SerializationException;
 import com.od.jtimeseries.server.serialization.TimeSeriesSerializer;
 import com.od.jtimeseries.server.util.FileReaper;
 import com.od.jtimeseries.component.util.path.PathMapper;
 import com.od.jtimeseries.component.util.path.PathMappingResult;
 import com.od.jtimeseries.timeseries.IdentifiableTimeSeries;
 import com.od.jtimeseries.util.logging.LogMethods;
 import com.od.jtimeseries.util.logging.LogUtils;
 import com.od.jtimeseries.util.time.Time;
 
 import java.io.File;
 import java.io.FilenameFilter;
 
 /**
  * Created by IntelliJ IDEA.
  * User: nick
  * Date: 20-May-2009
  * Time: 22:15:31
  * To change this template use File | Settings | File Templates.
  */
 public class SeriesDirectoryManager {
 
     private static LogMethods logMethods = LogUtils.getLogMethods(SeriesDirectoryManager.class);
 
     private File seriesDirectory;
     private TimeSeriesSerializer timeseriesSerializer;
     private TimeSeriesContext rootContext;
     private PathMapper pathMapper;
     private String seriesFileSuffix;
     private int maxFileCount;
     private int maxDiskSpaceForSeriesMb;
     private int maxSeriesFileAgeDays;
     private int loadCount;
     private FileReaper reaper;
 
     public SeriesDirectoryManager(File seriesDirectory, TimeSeriesSerializer timeseriesSerializer, TimeSeriesContext rootContext, PathMapper pathMapper, String seriesFileSuffix, int maxFileCount, int maxDiskSpaceForSeriesMb, int maxSeriesFileAgeDays) {
         this.seriesDirectory = seriesDirectory;
         this.timeseriesSerializer = timeseriesSerializer;
         this.rootContext = rootContext;
         this.pathMapper = pathMapper;
         this.seriesFileSuffix = seriesFileSuffix;
         this.maxFileCount = maxFileCount;
         this.maxDiskSpaceForSeriesMb = maxDiskSpaceForSeriesMb;
         this.maxSeriesFileAgeDays = maxSeriesFileAgeDays;
 
         createFileReaper(seriesDirectory, seriesFileSuffix);
     }
 
     private void createFileReaper(File seriesDirectory, String seriesFileSuffix) {
         this.reaper = new FileReaper(
             "Timeseries File Reaper",
             seriesDirectory,
             ".*" + seriesFileSuffix,
             maxFileCount,
             maxDiskSpaceForSeriesMb * 1000000,
             Time.days(maxSeriesFileAgeDays).getLengthInMillis()
         );
     }
 
     public void loadExistingSeries() {
         logMethods.logInfo("Loading series from directory " + seriesDirectory);
         File[] candidateFiles = getCandidateSeriesFiles();
         logMethods.logInfo("Found " + candidateFiles.length + " possible timeseries files, about to commence loading..");
         long startTime = System.currentTimeMillis();
         for (File f : candidateFiles) {
             if ( ! f.isDirectory()) {
                 if ( f.canRead() ) {
                     loadTimeSeries(f);
                 } else {
                     logMethods.logInfo("Cannot read time series file " + f + " - will skip loading this one");
                 }
             }
 
         }
         long loadTime = System.currentTimeMillis() - startTime;
         logMethods.logInfo("Loaded " + loadCount + " series in " + loadTime + " milliseconds");
 
     }
 
     private void loadTimeSeries(File f) {
         try {
             FileHeader header = timeseriesSerializer.readHeader(f);
             String path = header.getPath();
             PathMappingResult r = pathMapper.getPathMapping(path);
             if (r.getType() == PathMappingResult.ResultType.DENY) {
                logMethods.logWarning("Not loading series at path " + path + " since this path is denied by path mapping rules");
             } else if ( ! r.getNewPath().equals(path)) {
                 logMethods.logInfo("Migrating series at path " + path + " to " + r.getNewPath() + " since this path is migrated by path mapping rules");
                 timeseriesSerializer.migratePath(header, r.getNewPath());
                 loadSeriesFile(header);
             } else {
                 loadSeriesFile(header);
             }
 
         } catch (SerializationException e) {
             logMethods.logError("Failed to read series file " + f + ", this series is possibly corrupted, and will not be loaded, please remove it", e);
         }
     }
 
     private void loadSeriesFile(FileHeader header) {
         //the type of time series which will be created depends on the TimeSeriesFactory set on the context
         //we are expecting FilesystemTimeSeries
         if ( ! rootContext.contains(header.getPath())) { //this may be a server metrics series which has already been created
             logMethods.logInfo("Setting up series " + header.getPath() + " with current size " + header.getCurrentSeriesSize());
             rootContext.create(header.getPath(), header.getDescription(), IdentifiableTimeSeries.class, header);
             loadCount++;
         }
     }
 
     private File[] getCandidateSeriesFiles() {
         return seriesDirectory.listFiles(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.endsWith(seriesFileSuffix);
             }
         });
     }
 
     public void removeOldTimeseriesFiles() {
         logMethods.logInfo("Removing old timeseries files");
         reaper.reap();
     }
 }
