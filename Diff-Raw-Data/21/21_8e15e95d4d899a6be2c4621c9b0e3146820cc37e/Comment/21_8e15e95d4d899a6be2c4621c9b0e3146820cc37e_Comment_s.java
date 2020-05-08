 /*
  * Sonar Scala Plugin
  * Copyright (C) 2011 Felix Müller
  * felix.mueller.berlin@googlemail.com
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
 package org.sonar.plugins.scala.language;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.sonar.plugins.scala.util.StringUtils;
 
 /**
  * This class implements a Scala comment and the computation
  * of several base metrics for a comment.
  *
  * @author Felix Müller
  * @since 0.1
  */
 public class Comment {
 
   private final String content;
   private final CommentType type;
   private final List<String> lines;
 
   public Comment(String content, CommentType type) throws IOException {
     super();
     this.content = content;
     this.type = type;
     lines = StringUtils.convertStringToListOfLines(content);
   }
 
   public String getContent() {
     return content;
   }
 
   public CommentType getType() {
     return type;
   }
 
   public int getNumberOfLines() {
    int numberOfLines = lines.size();
    return numberOfLines - getNumberOfBlankLines() - getNumberOfCommentedOutLinesOfCode();
   }
 
   public int getNumberOfBlankLines() {
     int numberOfBlankLines = 0;
     for (String comment : lines) {
       boolean isBlank = true;
 
      for (int i = 0; i < comment.length(); i++) {
         char character = comment.charAt(i);
         if (!Character.isWhitespace(character) && character != '*' && character != '/') {
           isBlank = false;
          break;
         }
       }
 
       if (isBlank) {
         numberOfBlankLines++;
       }
     }
     return numberOfBlankLines;
   }
 
   public int getNumberOfCommentedOutLinesOfCode() {
     // TODO add implementation
     return 0;
   }
 
   public boolean isDocComment() {
     return type == CommentType.DOC;
   }
 
   public boolean isHeaderComment() {
     return type == CommentType.HEADER;
   }
 }
