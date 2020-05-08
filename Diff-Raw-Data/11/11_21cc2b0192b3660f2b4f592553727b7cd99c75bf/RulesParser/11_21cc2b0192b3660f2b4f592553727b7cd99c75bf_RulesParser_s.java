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
 package org.sonar.plugins.secrules;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
import org.sonar.api.profiles.RulesProfile;
 import org.sonar.api.rules.Rule;
 import org.sonar.api.rules.RulesManager;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 public class RulesParser {
 
   private List<Rule> rulesList;
 
  protected RulesParser(String resourcePath, RulesManager rulesManager) {
     InputStream input = getClass().getResourceAsStream(resourcePath);
     if (input == null) {
       throw new IllegalArgumentException();
     }
     try {
       List<String> fullRules = IOUtils.readLines(input);
       for (String fullRule : fullRules) {
         String[] s = StringUtils.split(fullRule, ",");
         Rule rule = rulesManager.getPluginRule(s[0], s[1]);
         rulesList.add(rule);
       }
     }
     catch (IOException e) {
       throw new RuntimeException("Unable to load " + resourcePath, e);
     }
     finally {
       IOUtils.closeQuietly(input);
     }
   }
 
   public List<Rule> getRulesList() {
     return rulesList;
   }
 
 }
