 /**
  * Copyright (C) 2011 BonitaSoft S.A.
  * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 2.0 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.bonitasoft.theme.css.impl;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bonitasoft.theme.builder.impl.ThemeDescriptorBuilderImpl;
 import org.bonitasoft.theme.css.CSSProperties;
 import org.bonitasoft.theme.css.CSSRule;
 import org.bonitasoft.theme.exception.CSSFileIsEmpty;
 import org.bonitasoft.theme.exception.CSSFileNotFoundException;
 
 /**
  * @author Romain Bioteau
  * 
  */
 public class CSSPropertiesImpl implements CSSProperties {
 
     /**
      * the regex of removing the comments and paramContent in the CSS file
      */
     public static final String COMMENTS_REGEX = "/\\*(?:.|[\\n\\r])*?\\*/";
 
     public static final String PARAM_REGEX = "@(?:.|[\\n\\r])*?;";
 
    public static final String MEDIA_REGEX = "@media.*\\{(?:(?:.|[\\r\\n])*?\\{(?:.|[\\r\\n])*?\\}(?:.|[\\r\\n])*?)*?\\}";
 
     /**
      * new line character
      */
     public static final String NEWLINE = "\n";
 
     /**
      * TAB character
      */
     public static final String TAB = "\t";
 
     /**
      * Logger
      */
     protected static Logger LOGGER = Logger.getLogger(ThemeDescriptorBuilderImpl.class.getName());
 
     /**
      * map to cache the CSS file rules
      */
     private final List<CSSRule> rules = new ArrayList<CSSRule>();
 
     @Override
     public void load(final InputStream input) throws IOException {
         if (input == null) {
             throw new IOException("Input is null");
         }
         rules.clear();
         String cssContent = getCSSContent(input);
         // remove media sections are they are not handled by the theme parser
         cssContent.replaceAll(MEDIA_REGEX, "");
         while (cssContent.trim().length() != 0) {
             if (cssContent.indexOf("{") < 0) {
                 break;
             }
             final String ruleAndComment = cssContent.substring(0, cssContent.indexOf("{")).trim();
             final String ruleName = ruleAndComment.replaceAll(COMMENTS_REGEX, "").replaceAll(PARAM_REGEX, "").trim();
             final String comment = ruleAndComment.replace(ruleName, "").trim();
 
             final CSSRule rule = new CSSRuleImpl(ruleName, comment);
             final String value = cssContent.substring(cssContent.indexOf("{") + 1, cssContent.indexOf("}")).trim();
             if (!"".equals(value.trim())) {
                 final String[] temp = value.replaceAll(COMMENTS_REGEX, "").replaceAll(PARAM_REGEX, "").split(";");
                 for (int i = 0; i < temp.length; i++) {
                     final String[] temps = temp[i].trim().split(":");
                     if (temps.length > 1) {
                         rule.put(temps[0].trim(), temps[1].trim());
                     }
                 }
             }
             cssContent = cssContent.substring(cssContent.indexOf("}") + 1);
             rules.add(rule);
         }
     }
 
     @Override
     public List<String> getAllRules() {
         return toRuleNameList(rules);
     }
 
     @Override
     public void addRule(final String rule, final String comment) {
         CSSRule r = getRuleByName(rules, rule);
         if (r == null) {
             r = new CSSRuleImpl(rule, comment);
             rules.add(r);
         } else {
             r.setComment(comment);
         }
     }
 
     @Override
     public void put(final String rule, final String key, final String value) {
         CSSRule r = getRuleByName(rules, rule);
         if (r == null) {
             r = new CSSRuleImpl(rule, null);
             rules.add(r);
         }
         if (key != null) {
             r.put(key, value);
         } else {
             r.remove(key);
         }
 
     }
 
     @Override
     public String get(final String rule, final String key) {
         final CSSRule r = getRuleByName(rules, rule);
         if (r != null) {
             return r.get(key);
         }
         return null;
     }
 
     @Override
     public void removeRule(final String rule) {
         final CSSRule r = getRuleByName(rules, rule);
         if (r != null) {
             rules.remove(r);
         }
     }
 
     @Override
     public void save(final OutputStream output) throws IOException {
         if (output == null) {
             throw new IOException("Output is null");
         }
 
         final StringBuffer cssContent = new StringBuffer("");
 
         for (final CSSRule rule : rules) {
             final String ruleName = rule.getName();
             final Map<String, String> cssProperties = rule.getAllProperties();
             if (rule.getComment() != null && !rule.getComment().isEmpty()) {
                 cssContent.append(rule.getComment() + NEWLINE);
             }
             cssContent.append(ruleName + " {" + NEWLINE);
             for (final String propertyName : cssProperties.keySet()) {
                 final String propertieValue = cssProperties.get(propertyName);
                 if (propertieValue != null) {
                     cssContent.append(TAB + propertyName + ": " + propertieValue + ";" + NEWLINE);
                 }
             }
             cssContent.append("}");
             cssContent.append(NEWLINE);
             cssContent.append(NEWLINE);
         }
 
         updateCssContentToFile(cssContent.toString(), output);
 
     }
 
     /**
      * get the content of CSS file
      * 
      * @param filePath
      *            path of CSS file
      * @return result css content which is converted into a string
      * @throws IOException
      * @throws CSSFileNotFoundException
      * @throws CSSFileIsEmpty
      */
     private String getCSSContent(final InputStream in) throws IOException {
         final StringBuffer result = new StringBuffer();
         final InputStreamReader inReader = new InputStreamReader(in, "UTF-8");
         final BufferedReader bufReader = new BufferedReader(inReader);
         try {
             String s;
             while ((s = bufReader.readLine()) != null) {
                 result.append(s + NEWLINE);
             }
         } finally {
             inReader.close();
             bufReader.close();
         }
 
         return result.toString();
     }
 
     private List<String> toRuleNameList(final List<CSSRule> ruleList) {
         final List<String> result = new ArrayList<String>();
         final Iterator<CSSRule> iterator = ruleList.iterator();
         while (iterator.hasNext()) {
             final CSSRule rule = iterator.next();
             result.add(rule.getName());
         }
         Collections.sort(result);
         return result;
     }
 
     private CSSRule getRuleByName(final List<CSSRule> ruleList, final String rule) {
         final Iterator<CSSRule> it = ruleList.iterator();
         while (it.hasNext()) {
             final CSSRule cssRule = it.next();
             if (cssRule.getName().equals(rule)) {
                 return cssRule;
             }
         }
         return null;
     }
 
     /**
      * write the changed CSS content to File
      * 
      * @param content
      *            the changed CSS content which has to write into CSS file
      * @param filePath
      *            path of CSS file
      * @throws CSSFileNotFoundException
      */
     private void updateCssContentToFile(final String content, final OutputStream output) throws IOException {
         PrintWriter writer = null;
         try {
             writer = new PrintWriter(output);
             writer.write(content);
             writer.flush();
         } finally {
             if (writer != null) {
                 writer.close();
                 writer = null;
             }
         }
     }
 
     @Override
     public String getComment(final String rule) {
         final CSSRule r = getRuleByName(rules, rule);
         if (r != null) {
             return r.getComment();
         }
         return null;
     }
 }
