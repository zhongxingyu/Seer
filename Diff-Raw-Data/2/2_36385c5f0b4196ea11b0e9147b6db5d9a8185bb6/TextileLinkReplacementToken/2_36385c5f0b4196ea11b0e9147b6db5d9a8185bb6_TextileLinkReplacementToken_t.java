 /** *****************************************************************************
  * Copyright © 2010 Leanne Northrop
  *
  * This file is part of Samye Content Management System.
  *
  * Samye Content Management System is free software: you can redistribute it
  * and/or modify it under the terms of the GNU General Public License as
  * published by the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  *
  * Samye Content Management System is distributed in the hope that it will be
  * useful,but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Samye Content Management System.
  * If not, see <http://www.gnu.org/licenses/>.
  *
  * BT plc, hereby disclaims all copyright interest in the program
  * “Samye Content Management System” written by Leanne Northrop.
  ***************************************************************************** */
 package org.samye.dzong.london.codec.textile;
 
 import org.eclipse.mylyn.wikitext.core.parser.markup.PatternBasedElement;
 import org.eclipse.mylyn.wikitext.core.parser.markup.PatternBasedElementProcessor;
 import org.eclipse.mylyn.wikitext.core.parser.Attributes;
 import java.util.*;
 
 /**
  * Link markup to html generator. This class provides additional markup
  * types for links to refer to 'internal' site pages such as teacher,
  * event, file video and more.
  *
  * @author Leanne Northrop
  * @since 1.0.0-SNAPSHOT, Dec 6, 2009 18:29:51
  */
 public class TextileLinkReplacementToken extends PatternBasedElement {
     private static Map urlBases = new HashMap();
 
     public TextileLinkReplacementToken(final Map urls) {
         urlBases = new HashMap(urls);
     }
 
     @Override
     protected String getPattern(int groupOffset) {
        return "(?:(?:(?<=\\W)|^)(\\[[a-zA-Z0-9 ,._-]{3,}\\])\\(([^\\)]+)\\))";
     }
 
     @Override
     protected int getPatternGroupCount() {
         return 2;
     }
 
     @Override
     protected PatternBasedElementProcessor newProcessor() {
         return new LinkReplacementTokenProcessor();
     }
 
     private static class LinkReplacementTokenProcessor extends PatternBasedElementProcessor {
 
         @Override
         public void emit() {
             String name = group(1).substring(1, group(1).length()-1);
             String type = group(2);
             if (type.equals("image")) {
                 String[] attributes = name.split(",");
                 Attributes a = new Attributes();
                 String style = "";
                 if (attributes.length >= 2) {
                     if (!"normal".equals(attributes[1])) {
                         style += "float:" + attributes[1] + ";";
                     }
                 }
                 style += attributes.length >= 3 ? "width:" + attributes[2] + ";" : "";
                 style += attributes.length >= 4 ? "height:" + attributes[3] + ";" : "";
                 a.setCssStyle(style);
                 a.setTitle(attributes[0]);
                 String href = urlBases.get(type) + "/" + attributes[0];
                 builder.image(a, href);
             } else if (type.equals("video")) {
                 String[] attributes = name.split(",");
 				Attributes attr = new Attributes();
 				String href = (String)urlBases.get(type) + "/" + attributes[0];
                 ((org.samye.dzong.london.codec.textile.HtmlDocumentBuilder)builder).video(attr, href);	
 			} else if (type.equals("file")) {
                 String[] attributes = name.split(",");
 				Attributes attr = new Attributes();				
 				String href = (String)urlBases.get(type) + "/" + attributes[0];
                 builder.link(href, name);
 			} else {
                 String href = (String)urlBases.get(type);
                 builder.link(href, name);
             }
         }
 
     }
 }
