 /*
  * Defect Prediction Plugin
  * Copyright (C) 2013 Revitas, Inc.
  * dev@sonar.codehaus.org
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 package com.revitasinc.sonar.dp.batch.parser;
 
 import com.revitasinc.sonar.dp.batch.RevisionInfo;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.io.LineIterator;
 import org.apache.commons.lang.CharEncoding;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Extracts revision data from a Git log.
  *
  * @author John Amos (jamos@revitasinc.com)
  */
 public class GitLogParser implements ScmLogParser {
   private static final String TAB = "\t";
 
   private static final String FILE_LINE_REGEX = "\\d*\t\\d*\t.*";
 
   private static final String EMAIL_START = " <";
 
   private static final String EMPTY_STRING = "";
 
   private static final String COMMIT = "commit ";
 
   private static final String AUTHOR = "Author: ";
 
   private static final String DATE = "Date: ";
 
   private static Logger logger = LoggerFactory.getLogger(GitLogParser.class);
 
   public boolean isRecognized(String command) {
     return command.startsWith("git ");
   }
 
   public Map<String, List<RevisionInfo>> parse(File workingDir, String command) {
     return parse(ParserHelper.getLogStream(workingDir, command));
   }
 
   public Map<String, List<RevisionInfo>> parse(InputStream inputStream) {
     Map<String, List<RevisionInfo>> result = new HashMap<String, List<RevisionInfo>>();
    // Wed May 2 15:24:20 2012 -0700
    DateFormat df = new SimpleDateFormat("EEE MMM dd hh:mm:ss yyyy");
     try {
       RevisionInfo revInfo = null;
       String author = null;
       Date date = null;
       for (LineIterator iterator = IOUtils.lineIterator(inputStream, CharEncoding.UTF_8); iterator.hasNext();) {
         String line = iterator.nextLine();
         if (line.startsWith(AUTHOR)) {
           author = line.split(EMAIL_START)[0].substring(AUTHOR.length());
           revInfo = null;
         }
         else if (line.startsWith(DATE)) {
           date = df.parse(line.substring(DATE.length()).trim());
         }
         else if (!StringUtils.isBlank(line) && !line.startsWith(COMMIT)) {
           if (revInfo == null) {
             revInfo = new RevisionInfo(author, date, line.trim(), 0);
             author = null;
             date = null;
           }
           else {
             processFileLine(line, revInfo, result);
           }
         }
       }
     } catch (NumberFormatException e) {
       logger.error(EMPTY_STRING, e);
     } catch (IOException e) {
       logger.error(EMPTY_STRING, e);
     } catch (ParseException e) {
       logger.error(EMPTY_STRING, e);
     }
     return result;
   }
 
   /**
    * If the supplied line matches FILE_LINE_REGEX, then a new RevisionInfo is built using
    * the data in the supplied RevisionInfo and then added to the supplied Map.
    *
    * @param line
    * @param revInfo
    * @param result
    */
   private void processFileLine(String line, RevisionInfo revInfo, Map<String, List<RevisionInfo>> result) {
     if (line.matches(FILE_LINE_REGEX)) {
       String[] segs = line.split(TAB);
       List<RevisionInfo> list = result.get(segs[2]);
       if (list == null) {
         list = new ArrayList<RevisionInfo>();
         result.put(segs[2], list);
       }
       list.add(new RevisionInfo(revInfo.getAuthor(), revInfo.getDate(), revInfo.getComment(), Integer
           .parseInt(segs[0])));
     }
   }
 }
