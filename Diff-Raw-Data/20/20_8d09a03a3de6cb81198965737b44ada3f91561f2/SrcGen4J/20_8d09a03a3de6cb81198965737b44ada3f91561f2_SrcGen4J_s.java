 /**
  * Copyright (C) 2013 Future Invent Informationsmanagement GmbH. All rights
  * reserved. <http://www.fuin.org/>
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 3 of the License, or (at your option) any
  * later version.
  *
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.fuin.srcgen4j.commons;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.fuin.utils4j.Utils4J;
 
 /**
  * Parses models based on a configuration and generates something with it.
  */
 public final class SrcGen4J {
 
     private final SrcGen4JConfig config;
 
     /**
      * Constructor with configuration.
      * 
      * @param config
      *            Configuration - Cannot be NULL.
      */
     public SrcGen4J(final SrcGen4JConfig config) {
         super();
         if (config == null) {
             throw new IllegalArgumentException("Argument 'config' cannot be NULL");
         }
         if (!config.isInitialized()) {
             throw new IllegalArgumentException("The configuration is not initialized");
         }
         this.config = config;
     }
 
     private void enhanceClasspath() {
         final Classpath cp = config.getClasspath();
         if (cp != null) {
             final List<BinClasspathEntry> binList = cp.getBinList();
             if (binList != null) {
                 for (final BinClasspathEntry entry : binList) {
                     final String url;
                     if (entry.getPath().startsWith("file:")) {
                         url = entry.getPath();
                     } else {
                         url = "file:///" + entry.getPath();
                     }
                     Utils4J.addToClasspath(url);
                 }
             }
         }
     }
 
     private void cleanFolders() {
         final List<Project> projects = config.getProjects();
        for (final Project project : projects) {
            final List<Folder> folders = project.getFolders();
            for (final Folder folder : folders) {
                final File dir = new File(folder.getDirectory());
                if (folder.isClean() && dir.exists()) {
                    try {
                        FileUtils.cleanDirectory(dir);
                    } catch (final IOException ex) {
                        throw new RuntimeException("Error cleaning: " + dir, ex);
                     }
                 }
             }
         }
     }
 
     /**
      * Parse and generate.
      * 
      * @throws ParseException
      *             Error during parse process.
      * @throws GenerateException
      *             Error during generation process.
      */
     public final void execute() throws ParseException, GenerateException {
 
         enhanceClasspath();
 
         cleanFolders();
 
         // Parse models & generate
         final List<ParserConfig> parserConfigs = config.getParsers();
         if (parserConfigs != null) {
             for (final ParserConfig pc : parserConfigs) {
                 final Parser<Object> parser = pc.getParser();
                 final Object model = parser.parse(pc);
                 final List<GeneratorConfig> generatorConfigs = config.findGeneratorsForParser(pc
                         .getName());
                 for (final GeneratorConfig gc : generatorConfigs) {
                     final Generator<Object> generator = gc.getGenerator();
                     generator.generate(gc, model);
                 }
             }
         }
 
     }
 
 }
