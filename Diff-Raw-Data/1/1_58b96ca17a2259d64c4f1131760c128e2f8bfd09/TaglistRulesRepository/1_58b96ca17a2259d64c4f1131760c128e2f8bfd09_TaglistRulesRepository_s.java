 /*
  * Sonar, open source software quality management tool.
  * Copyright (C) 2009 SonarSource SA
  * mailto:contact AT sonarsource DOT com
  *
  * Sonar is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 package org.sonar.plugins.taglist;
 
 import org.apache.commons.io.IOUtils;
 import org.sonar.api.rules.*;
 import org.sonar.api.resources.Language;
 import org.sonar.api.resources.Java;
 import org.sonar.api.profiles.RulesProfile;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 import java.util.List;
 import java.util.ArrayList;
 
 public class TaglistRulesRepository implements RulesRepository {
 
   public Language getLanguage() {
     return new Java();
   }
 
   public List<Rule> getInitialReferential() {
     List<Rule> rules = new ArrayList<Rule>();
     Properties tags = new Properties();
     readTaglistFile("/org/sonar/plugins/taglist/default-tags.properties", tags);
     readTaglistFile("/extensions/plugins/taglist.properties", tags);
     for (Object tag : tags.keySet()) {
       String tagName = "Tag " + tag;
       String tagKey = (String) tag;
       String tagDescription = "Detection of keyword '" + tagKey + "' in the source code";
       RulesCategory category = new RulesCategory(tags.getProperty(tagKey));
 
       // We set the priority to MINOR by default for all tags.
       Rule rule = new Rule(TaglistPlugin.KEY, tagKey, tagName, category, RulePriority.MINOR);
       rule.setDescription(tagDescription);
       rules.add(rule);
     }
     return rules;
   }
 
   private void readTaglistFile(String resourcePath, Properties tags) {
     InputStream input = getClass().getResourceAsStream(resourcePath);
     if (input == null) {
       return;
     }
     try {
       tags.load(input);
     }
     catch (IOException e) {
       throw new RuntimeException("Unable to load " + resourcePath, e);
     }
     finally {
       IOUtils.closeQuietly(input);
     }
   }
 
   public List<RulesProfile> getProvidedProfiles() {
     return new ArrayList<RulesProfile>();
   }
 
   public List<Rule> parseReferential(String fileContent) {
     return new StandardRulesXmlParser().parse(fileContent);
   }
 }
