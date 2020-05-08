 /*
  * Sonar Switch Off Violations Plugin
  * Copyright (C) 2011 SonarSource
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
 
 package org.sonar.plugins.switchoffviolations;
 
 import org.sonar.api.Properties;
 import org.sonar.api.Property;
 import org.sonar.api.PropertyField;
 import org.sonar.api.PropertyType;
 import org.sonar.api.SonarPlugin;
 import org.sonar.plugins.switchoffviolations.pattern.PatternsInitializer;
 import org.sonar.plugins.switchoffviolations.scanner.RegexpScanner;
 import org.sonar.plugins.switchoffviolations.scanner.SourceScanner;
 
 import java.util.Arrays;
 import java.util.List;
 
 @Properties({
   @Property(
     key = Constants.PATTERNS_MULTICRITERIA_KEY,
     name = "Multi-criteria exclusion patterns",
    description = "Patterns used to identify which violations to switch off." +
       "More information on the <a href=\"http://docs.codehaus.org/display/SONAR/Switch+Off+Violations+Plugin\">documentation page of the plugin</a>.<br/>",
     project = true,
     global = true,
     fields = {
       @PropertyField(
         key = Constants.RESOURCE_KEY,
         name = "Resource Key Pattern",
         description = "Pattern used to match resources which should be ignored.",
         type = PropertyType.STRING,
         indicativeSize = 20),
       @PropertyField(
         key = Constants.RULE_KEY,
         name = "Rule Key Pattern",
         description = "Pattern used to match rules which should be ignored.",
         type = PropertyType.STRING,
         indicativeSize = 20),
       @PropertyField(
         key = Constants.LINE_RANGE_KEY,
         name = "Line Range",
         description = "Range of lines that should be ignored.",
         type = PropertyType.STRING,
         indicativeSize = 10)}),
   @Property(
     key = Constants.PATTERNS_BLOCK_KEY,
     name = "Bloc exclusion patterns",
    description = "Patterns used to identify blocks in which violations are switched off." +
       "More information on the <a href=\"http://docs.codehaus.org/display/SONAR/Switch+Off+Violations+Plugin\">documentation page of the plugin</a>.<br/>",
     project = true,
     global = true,
     fields = {
       @PropertyField(
         key = Constants.BEGIN_BLOCK_REGEXP,
         name = "Regular expression for start of block",
         description = "If this regular expression is found in a resource, then following lines are ignored until end of block.",
         type = PropertyType.STRING,
         indicativeSize = 20),
       @PropertyField(
         key = Constants.END_BLOCK_REGEXP,
         name = "Regular expression for end of block",
         description =
        "If specified, this regular expression is used to determine the end of code blocks to ignore. If not, then block ends at the end of file",
         type = PropertyType.STRING,
         indicativeSize = 20)}),
   @Property(
     key = Constants.PATTERNS_ALLFILE_KEY,
     name = "File exclusion patterns",
    description = "Patterns used to identify files in which violations are switched off." +
       "More information on the <a href=\"http://docs.codehaus.org/display/SONAR/Switch+Off+Violations+Plugin\">documentation page of the plugin</a>.<br/>",
     project = true,
     global = true,
     fields = {
       @PropertyField(
         key = Constants.FILE_REGEXP,
         name = "Regular expression",
         description = "If this regular expression is found in a resource, then this resource is ignored.",
         type = PropertyType.STRING,
         indicativeSize = 20)}),
   @Property(
     key = Constants.PATTERNS_PARAMETER_KEY,
     defaultValue = "",
     name = "Exclusion patterns (Deprecated)",
    description = "Patterns used to identify which violations to switch off." +
       "Example:<br/><pre># exclude a specific rule on a specific file on specific lines\ncom.foo.Bar;pmd:AvoidPrintStackTrace;[10,25,90]</pre>" +
       "<br/>More information on the <a href=\"http://docs.codehaus.org/display/SONAR/Switch+Off+Violations+Plugin\">documentation page of the plugin</a>.<br/><br/>",
     project = true,
     global = true,
     type = PropertyType.TEXT),
   @Property(
     key = Constants.LOCATION_PARAMETER_KEY,
     defaultValue = "",
     name = "Configuration file for exclusion patterns (Deprecated)",
     description = "Location of a file that would contain the exclusion patterns and that would be stored along with the source code.",
     project = true,
     global = true)
 })
 public final class SwitchOffViolationsPlugin extends SonarPlugin {
 
   @SuppressWarnings({"rawtypes", "unchecked"})
   public List getExtensions() {
     return Arrays.asList(
         PatternsInitializer.class,
         RegexpScanner.class,
         SourceScanner.class,
         SwitchOffViolationsFilter.class);
   }
 
 }
