 // Copyright (c) 2012, Daniel Andersen (dani_ande@yahoo.dk)
 // All rights reserved.
 //
 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions are met:
 //
 // 1. Redistributions of source code must retain the above copyright notice, this
 //    list of conditions and the following disclaimer.
 // 2. Redistributions in binary form must reproduce the above copyright notice,
 //    this list of conditions and the following disclaimer in the documentation
 //    and/or other materials provided with the distribution.
 // 3. The name of the author may not be used to endorse or promote products derived
 //    from this software without specific prior written permission.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 // ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 // DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 // ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 // LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 // ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 // (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 // SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package com.trollsahead.qcumberless.util;
 
 import com.trollsahead.qcumberless.device.Device;
 import com.trollsahead.qcumberless.engine.DesignerEngine;
 import com.trollsahead.qcumberless.engine.FeatureBuilder;
 import com.trollsahead.qcumberless.gui.elements.BaseBarElement;
 import com.trollsahead.qcumberless.gui.elements.Element;
 import com.trollsahead.qcumberless.gui.elements.FeatureElement;
 import com.trollsahead.qcumberless.model.*;
 
 import java.io.*;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class HistoryHelper {
     public static final String RUN_HISTORY_DIR = "runhistory";
 
     public static final String COMMENT_DELIMITER_START = "<#";
     public static final String COMMENT_DELIMITER_END = "#>";
     public static final Pattern PATTERN_DELIMITER_START = Pattern.compile("<#");
     public static final Pattern PATTERN_DELIMITER_END = Pattern.compile("#>");
 
     public static final String COMMENT_QCUMBERLESS = "# qcumberless run: ";
     public static final Pattern PATTERN_QCUMBERLESS = Pattern.compile("# qcumberless run: ");
 
     public static final String COMMENT_STATUS = COMMENT_DELIMITER_START + "status: $status" + COMMENT_DELIMITER_END;
     public static final Pattern PATTERN_STATUS = Pattern.compile(".*" + PATTERN_DELIMITER_START + "status: (.*?)" + PATTERN_DELIMITER_END + ".*");
 
     public static final String COMMENT_DATE = COMMENT_DELIMITER_START + "date: $date" + COMMENT_DELIMITER_END;
     public static final Pattern PATTERN_DATE = Pattern.compile(".*" + PATTERN_DELIMITER_START + "date: (.*?)" + PATTERN_DELIMITER_END + ".*");
 
     public static final String COMMENT_ERROR_MESSAGE = COMMENT_DELIMITER_START + "errmsg: $errmsg" + COMMENT_DELIMITER_END;
     public static final Pattern PATTERN_ERROR_MESSAGE = Pattern.compile(".*" + PATTERN_DELIMITER_START + "errmsg: (.*?)" + PATTERN_DELIMITER_END + ".*");
 
     public static final String COMMENT_SCREENSHOT = COMMENT_DELIMITER_START + "screenshot: $screenshot" + COMMENT_DELIMITER_END;
     public static final String PREFIX_SCREENSHOT = PATTERN_DELIMITER_START + "screenshot: ";
 
     private static final String PATTERN_FILE_DATE = ".*(\\d{4}-\\d{2}-\\d{2})[\\\\/](\\d{2}_\\d{2}_\\d{2}).*";
 
     public static String getRunOutcomeComment(BaseBarElement element, long time) {
         StringBuilder sb = new StringBuilder();
         sb.append(COMMENT_QCUMBERLESS);
         sb.append(COMMENT_DATE.replaceAll("\\$date", Long.toString(time)));
         if (element.getPlayResult().isFailed()) {
             sb.append(COMMENT_STATUS.replaceAll("\\$status", "failed"));
             if (element.getPlayResult().hasErrorMessage()) {
                 sb.append(COMMENT_ERROR_MESSAGE.replaceAll("\\$errmsg", element.getPlayResult().getErrorMessage()));
             }
             if (element.getPlayResult().hasScreenshots()) {
                 sb.append(getRunOutcomeScreenshotComment(element));
             }
         } else if (element.getPlayResult().isSuccess()) {
             sb.append(COMMENT_STATUS.replaceAll("\\$status", "success"));
         } else {
             sb.append(COMMENT_STATUS.replaceAll("\\$status", "not yet played"));
         }
         sb.append("\n");
         return sb.toString();
     }
 
     public static List<String> findFeatureFiles() {
         return FileUtil.getFeatureFiles(RUN_HISTORY_DIR);
     }
 
     public static List<String> findHistoryDirs() {
         List<String> features = findFeatureFiles();
         Set<String> dirSet = new HashSet<String>();
         for (String feature : features) {
             dirSet.add(FileUtil.getPath(feature));
         }
         List<String> dirList = new LinkedList<String>();
         dirList.addAll(dirSet);
         return dirList;
     }
 
     public static File saveRunOutcome(File dir, Device device, List<BaseBarElement> features, long startTime, String tags) {
         for (BaseBarElement element : features) {
             String filename = FileUtil.addSlashToPath(dir.getAbsolutePath()) + ElementHelper.suggestFilenameIfNotPresent(element) + ".feature";
             File file = FileUtil.writeToFile(filename, FeatureBuilder.buildFeature(element, new FeatureBuildState(startTime, FeatureBuildState.ADD_STATE_RUN_OUTCOME)));
             RunHistory.addFeature(file.getAbsolutePath());
         }
         String filename = FileUtil.addSlashToPath(dir.getAbsolutePath()) + FileUtil.toFilename(device.name());
         device.getConsoleOutput().exportLog(filename + ".log", ConsoleOutput.getPreample(device, new Date(startTime), tags));
         writeRunParameters(filename + ".history", device, startTime, tags);
         return dir;
     }
 
     private static void writeRunParameters(String filename, Device device, long date, String tags) {
         Properties properties = new Properties();
         properties.put("devicename", device.name());
         properties.put("date", Long.toString(date));
         properties.put("tags", tags);
         try {
             properties.store(new FileOutputStream(filename), "Q-Cumberless Testing history info");
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public static boolean hasErrorInFeatureFile(String filename) {
         BufferedReader in = null;
         try {
             in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"));
             String line;
             while ((line = in.readLine()) != null) {
                 if (line.startsWith(COMMENT_QCUMBERLESS)) {
                     Matcher matcher = HistoryHelper.PATTERN_ERROR_MESSAGE.matcher(line);
                     if (matcher.matches()) {
                         return true;
                     }
                 }
             }
             return false;
         } catch (Exception e) {
             throw new RuntimeException("Error reading supported feature file " + filename, e);
         } finally {
             FileUtil.close(in);
         }
     }
 
     public static PlayResult getPlayResultFromComment(String line) {
         Matcher matcher = PATTERN_STATUS.matcher(line);
         if (!matcher.find()) {
             return new PlayResult(PlayResult.State.NOT_PLAYED);
         }
         if ("success".equalsIgnoreCase(matcher.group(1))) {
             return new PlayResult(PlayResult.State.SUCCESS);
         } else if ("failed".equalsIgnoreCase(matcher.group(1))) {
             return extractFailedPlayStateFromComment(line);
         } else {
             return new PlayResult(PlayResult.State.NOT_PLAYED);
         }
     }
 
     private static PlayResult extractFailedPlayStateFromComment(String line) {
         PlayResult playResult = new PlayResult(PlayResult.State.FAILED);
         Matcher matcher = PATTERN_ERROR_MESSAGE.matcher(line);
         if (matcher.find()) {
             playResult.setErrorMessage(matcher.group(1));
         }
         int startIdx;
         while ((startIdx = line.indexOf(PREFIX_SCREENSHOT)) != -1) {
             line = line.substring(startIdx);
             int endIdx = line.indexOf(COMMENT_DELIMITER_END);
             String filename = line.substring(PREFIX_SCREENSHOT.length(), endIdx);
             playResult.addScreenshots(new Screenshot(filename));
             line = line.substring(endIdx + COMMENT_DELIMITER_END.length());
         }
         return playResult;
     }
 
     private static String getRunOutcomeScreenshotComment(BaseBarElement element) {
         if (!element.getPlayResult().hasScreenshots()) {
             return "";
         }
         StringBuilder sb = new StringBuilder();
         for (Screenshot screenshot : element.getPlayResult().getScreenshots()) {
             if (!Util.isEmpty(screenshot.getFilename())) {
                 sb.append(COMMENT_SCREENSHOT.replaceAll("\\$screenshot", screenshot.getFilename()));
             }
         }
         return sb.toString();
     }
 
     public static List<String> sortDirs(List<String> historyDirs) {
         String[] dirs = historyDirs.toArray(new String[0]);
         Arrays.sort(dirs, historyDirComparator);
         List<String> dirList = new LinkedList<String>();
         dirList.addAll(Arrays.asList(dirs));
         return dirList;
     }
     
     public static Date extractDateFromDir(String historyDir) {
         Properties properties = getRunProperties(historyDir);
         return new Date(Long.parseLong((String) properties.get("date")));
     }
 
     public static Comparator historyDirComparator = new Comparator<String>() {
         public int compare(String s1, String s2) {
             return extractDateFromDir(s2).compareTo(extractDateFromDir(s1));
         }
     };
 
     public static List<String> filterByTags(List<String> dirs, String tags) {
         if (Util.isEmpty(tags)) {
             return dirs;
         }
         List<String> resultDirs = new LinkedList<String>();
         for (String dir : dirs) {
             Properties properties = getRunProperties(dir);
             String runTags = (String) properties.get("tags");
             if (Util.isEmpty(runTags)) {
                 continue;
             }
             if (containsAnyOfTags(tags, runTags)) {
                 resultDirs.add(dir);
             }
         }
         return resultDirs;
     }
 
     private static boolean containsAnyOfTags(String tags1, String tags2) {
         List<String> tagList1 = Util.stringToTagList(tags1);
         List<String> tagList2 = Util.stringToTagList(tags2);
         for (String tag : tagList1) {
             if (tagList2.contains(tag)) {
                 return true;
             }
         }
         return false;
     }
     
     public static Properties getRunProperties(String dir) {
         List<String> historyFiles = FileUtil.getHistoryFiles(dir);
         if (Util.isEmpty(historyFiles) || historyFiles.size() != 1) {
             return new Properties();
         }
         try {
             Properties properties = new Properties();
             properties.load(new FileInputStream(historyFiles.get(0)));
             return properties;
         } catch (Exception e) {
             e.printStackTrace();
         }
         return new Properties();
     }
 
     public static File createHistoryDir(long startTime) {
         File dir = new File(RUN_HISTORY_DIR + "/" + FileUtil.prettyFilenameDate(new Date(startTime)) + "/" + FileUtil.prettyFilenameTime(new Date(startTime)));
        for (int i = 1; i < 100; i++) {
            if (dir.mkdirs()) {
                return dir;
             }
            dir = new File(RUN_HISTORY_DIR + "/" + FileUtil.prettyFilenameDate(new Date(startTime)) + "/" + FileUtil.prettyFilenameTime(new Date(startTime)) + "_" + i);
         }
        throw new RuntimeException("Could not create directory for run history at: " + dir.getAbsolutePath());
     }
 
     public static List<FeatureElement> featuresRootToFeatureList() {
         List<FeatureElement> features = new LinkedList<FeatureElement>();
         for (Element element : DesignerEngine.featuresRoot.children) {
             features.add((FeatureElement) element);
         }
         return features;
     }
 }
