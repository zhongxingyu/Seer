 /*
  * Copyright (C) 2008-2012 - Thomas Santana <tms@exnebula.org>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>
  */
 package org.exnebula.bootstrap;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.LinkedList;
 import java.util.List;
 
 public class BootConfigLoader {
 
   private BootConfig configuration = null;
   //  private String errorMessage = null;
   private List<String> classPath = null;
   private String entryPoint = null;
 
   public boolean load(InputStream is) throws IOException {
     checkDoubleExecution();
     List<String> lines = readLines(new BufferedReader(new InputStreamReader(is)));
     checkForInvalidLines(lines);
     collectEntryPointLine(lines);
     collectClassPaths(lines);
     buildConfiguration();
     return true;
   }
 
   private void checkDoubleExecution() {
     if (classPath != null) {
       throw new IllegalStateException("BootConfigLoader can only load once");
     }
   }
 
   private void collectClassPaths(List<String> lines) {
     classPath = filterLinesByPrefix("cp=", lines);
     if (classPath.size() == 0) {
       throw new InvalidConfigurationException("Must have at least one 'cp' entry");
     }
   }
 
   private void collectEntryPointLine(List<String> lines) {
     List<String> eps = filterLinesByPrefix("ep=", lines);
     if (eps.size() == 1)
       entryPoint = eps.get(0);
     else
       throw new InvalidConfigurationException("Must have exactly one 'ep' entry");
   }
 
   private void checkForInvalidLines(List<String> lines) {
     for (String line : lines) {
       if (!line.startsWith("ep=") && !line.startsWith("cp="))
         throw new InvalidConfigurationException("Illegal line '" + line + "'");
     }
   }
 
   private List<String> filterLinesByPrefix(String prefix, List<String> a) {
     LinkedList<String> out = new LinkedList<String>();
     for (String line : a) {
       if (line.startsWith(prefix))
         out.add(line.substring(prefix.length()));
     }
     return out;
   }
 
   private List<String> readLines(BufferedReader reader) throws IOException {
     LinkedList<String> lines = new LinkedList<String>();
     String line = readNonCommentLine(reader);
     while (line != null) {
       lines.add(line);
       line = readNonCommentLine(reader);
     }
     return lines;
   }
 
   private void buildConfiguration() {
       configuration = new BootConfig(entryPoint, classPath.toArray(new String[classPath.size()]));
   }
 
   private String readNonCommentLine(BufferedReader reader) throws IOException {
     String line = reader.readLine();
     while (line != null && line.startsWith("#")) {
       line = reader.readLine();
     }
     return line;
   }
 
   public BootConfig getConfiguration() {
     return configuration;
   }
 
   public class InvalidConfigurationException extends RuntimeException {
     public InvalidConfigurationException(String errorMessage) {
       super(errorMessage);
     }
   }
 }
