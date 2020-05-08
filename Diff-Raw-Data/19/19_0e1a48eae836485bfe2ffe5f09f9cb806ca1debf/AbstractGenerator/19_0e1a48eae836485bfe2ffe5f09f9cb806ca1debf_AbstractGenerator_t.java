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
 package org.fuin.srcgen4j.core.base;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 
 import org.fuin.objects4j.common.Contract;
 import org.fuin.srcgen4j.commons.Folder;
 import org.fuin.srcgen4j.commons.GenerateException;
 import org.fuin.srcgen4j.commons.GeneratedArtifact;
 import org.fuin.srcgen4j.commons.Generator;
 import org.fuin.srcgen4j.commons.GeneratorConfig;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Base class for concrete generators.
  * 
  * @param <MODEL>
  *            Type of the model.
  * @param <CONFIG>
  *            Type of the generator specific configuration - Use 'Object' if the
  *            generator does not require any configuration.
  */
 public abstract class AbstractGenerator<MODEL, CONFIG> implements Generator<MODEL> {
 
     private static final Logger LOG = LoggerFactory.getLogger(AbstractGenerator.class);
 
     private String name;
 
     private GeneratorConfig config;
 
     private MODEL model;
 
     /**
      * Default constructor.
      */
     public AbstractGenerator() {
         super();
     }
 
     @Override
     public final void initialize(final GeneratorConfig config) {
 
         Contract.requireArgNotNull("config", config);
         Contract.requireValid(config);
 
         this.config = config;
         name = config.getName();
 
         LOG.debug("Initialize generator: " + name);
 
         final Object obj = config.getConfig().getConfig();
         if (getSpecificConfigClass() == null) {
             if (obj != null) {
                 throw new IllegalStateException("No configuration is expected, but was: "
                         + obj.getClass());
             }
         } else {
             if (obj == null) {
                 throw new IllegalStateException("The configuration is expected to be of type '"
                         + getSpecificConfigClass().getName() + "', but was: null");
             }
             if (!getSpecificConfigClass().isAssignableFrom(obj.getClass())) {
                 throw new IllegalStateException("The configuration is expected to be of type '"
                         + getSpecificConfigClass().getName() + "', but was: "
                         + obj.getClass().getName());
             }
         }
 
         init();
 
     }
 
     /**
      * Extension point for specific initialization. Sub classes can use this to
      * initialize something after the main initialization process is done. Does
      * nothing by default.
      */
     protected void init() {
         // May be overwritten by sub classes
     }
 
     @Override
     public final void generate(final MODEL model, final boolean incremental)
             throws GenerateException {
 
         Contract.requireArgNotNull("model", model);
         Contract.requireValid(model);
 
         this.model = model;
 
         LOG.info("Starting generation: " + name);
         generate(incremental);
         LOG.info("Generation finished: " + name);
 
     }
 
     /**
      * Returns the configuration.
      * 
      * @return Configuration.
      */
     public final GeneratorConfig getGeneratorConfig() {
         return config;
     }
 
     /**
      * Returns the model.
      * 
      * @return Model.
      */
     public final MODEL getModel() {
         return model;
     }
 
     /**
      * Returns the class of the expected configuration class. This method can be
      * overridden to make sure the configuration is valid.
      * 
      * @return Specific configuration class or NULL if no configuration is
      *         required.
      */
     // CHECKSTYLE:OFF Empty methods do not violate the 'design for extension'
     // principle
     public Class<CONFIG> getSpecificConfigClass() {
         // CHECKSTYLE:ON
         return null;
     }
 
     /**
      * Returns the specific configuration. This is a shortcut for
      * {@link #getConfig().getConfig().getConfig()}.
      * 
      * @return Generator configuration or NULL.
      */
     @SuppressWarnings("unchecked")
     public final CONFIG getSpecificConfig() {
         return (CONFIG) config.getConfig().getConfig();
     }
 
     /**
      * Returns the target file for a given artifact type and filename. This
      * method takes care about eventually creating non existing directories or
      * protect existing files to be overridden.
      * 
      * @param artifactName
      *            Type of the artifact to create - This has to refer to an
      *            existing entry in the configuration (Like '&lt;artifact
      *            name="<i>artifactName</i>" ..&gt;').
      * @param filename
      *            Name of the target file (without a path).
      * @param logInfo
      *            Addition information to log.
      * 
      * @return File to write to or NULL if nothing should be written.
      */
     protected final GeneratedFile getTargetFile(final String artifactName, final String filename,
             final String logInfo) {
 
         final Folder folder = getGeneratorConfig().findTargetFolder(artifactName);
        final File dir = folder.getCanonicalDir();
         final File file = new File(dir, filename);
 
        // Make sure the folder exists
        if (!dir.exists()) {
             if (folder.isCreate()) {
                dir.mkdirs();
             } else {
                throw new IllegalStateException("Directory '" + dir
                         + "' does not exist, but configuration does not allow creation: "
                         + "<folder name=\"" + folder.getName() + "\" create=\"false\" ... />");
             }
         }
        // Make sure the parent directory for the file exists
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
         if (file.exists() && !folder.isOverride()) {
             // Skip file because override is not allowed
             return new GeneratedFile(file, logInfo, true);
         }
 
         return new GeneratedFile(file, logInfo);
 
     }
 
     /**
      * Writes a generated artifact to a file.
      * 
      * @param artifact
      *            Artifact to persist.
      * 
      * @throws GenerateException
      *             Error writing the artifact.
      */
     protected final void write(final GeneratedArtifact artifact) throws GenerateException {
 
         final GeneratedFile genFile = getTargetFile(artifact.getName(), artifact.getPathAndName(),
                 null);
         if (genFile.isSkip()) {
             LOG.debug("Omitted already existing file: " + genFile + " [" + artifact + "]");
         } else {
             LOG.debug("Writing file: " + genFile + " [" + artifact + "]");
             try {
                 final Writer writer = new FileWriter(genFile.getFile());
                 try {
                     writer.write(artifact.getSource());
                 } finally {
                     writer.close();
                 }
                 genFile.persist();
             } catch (final IOException ex) {
                 throw new GenerateException("Error writing artifact '" + artifact + "' to '"
                         + artifact.getPathAndName() + "'!", ex);
             }
         }
     }
 
     /**
      * Generates something using the given configuration and model.
      * 
      * @param incremental
      *            If this is an incremental build TRUE, else FALSE (full build).
      * 
      * @throws GenerateException
      *             Error when generating.
      */
     protected abstract void generate(boolean incremental) throws GenerateException;
 
 }
