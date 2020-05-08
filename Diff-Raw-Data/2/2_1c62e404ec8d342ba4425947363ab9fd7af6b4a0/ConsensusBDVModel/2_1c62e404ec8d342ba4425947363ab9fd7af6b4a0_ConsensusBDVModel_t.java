 /*
  * Copyright (C) 2008-2009 Institute for Computational Biomedicine,
  *                         Weill Medical College of Cornell University
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License along
  *  with this program; if not, write to the Free Software Foundation, Inc.,
  *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 package org.bdval;
 
 import edu.cornell.med.icb.geo.GEOPlatformIndexed;
 import edu.cornell.med.icb.geo.tools.ClassificationTask;
 import edu.cornell.med.icb.geo.tools.FixedGeneList;
 import edu.mssm.crover.tables.ColumnTypeException;
 import edu.mssm.crover.tables.InvalidColumnException;
 import edu.mssm.crover.tables.Table;
 import edu.mssm.crover.tables.TypeMismatchException;
 import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
 import it.unimi.dsi.fastutil.doubles.DoubleList;
 import it.unimi.dsi.fastutil.objects.ObjectArrayList;
 import it.unimi.dsi.fastutil.objects.ObjectList;
 import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
 import it.unimi.dsi.fastutil.objects.ObjectSet;
 import it.unimi.dsi.util.Properties;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.BooleanUtils;
 import org.apache.commons.lang.SystemUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.Set;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipOutputStream;
 
 /**
 * A model that is created based on the consensus of other "juror" models.
  * @author Fabien Campagne
  *         Date: Jun 8, 2008
  *         Time: 11:59:53 AM
  */
 public class ConsensusBDVModel extends BDVModel {
     /**
      * Used to log debug and informational messages.
      */
     private static final Log LOG = LogFactory.getLog(ConsensusBDVModel.class);
 
     /**
      * The file names of the juror models.
      */
     private final ObjectList<String> jurorModelFilenamePrefixes = new ObjectArrayList<String>();
 
     /**
      * The juror models.
      */
     private final ObjectList<BDVModel> jurorModels = new ObjectArrayList<BDVModel>();
 
     /**
      * Indicates that the juror models have been loaded.
      */
     private boolean jurorModelsAreLoaded;
 
     /**
      * Subdirectory that juror models will exist in.
      */
     private static final String JUROR_MODEL_DIRECTORY = "models";
 
     /**
      * Create a model that is a consensus of a number of given juror models.
      * @param modelFilenamePrefix Prefix to use for all files associated with this BDVModel
      */
     public ConsensusBDVModel(final String modelFilenamePrefix) {
         super(modelFilenamePrefix);
     }
 
     /**
      * Create a model that is a consensus of a number of given juror models.
      * @param modelFilenamePrefix Prefix to use for all files associated with this BDVModel
      * @param jurorModelFilenamePrefixes The prefix to use for the juror models
      */
     public ConsensusBDVModel(final String modelFilenamePrefix,
                              final String... jurorModelFilenamePrefixes) {
         super(modelFilenamePrefix);
 
         if (LOG.isDebugEnabled()) {
             LOG.debug("Creating consensus model with prefix: " + modelFilenamePrefix);
             LOG.debug("Composed of the following juror models");
             LOG.debug(ArrayUtils.toString(jurorModelFilenamePrefixes));
         }
 
         for (final String jurorModelFilenamePrefix : jurorModelFilenamePrefixes) {
             // remove ".model" or ".zip" extensions
             String prefix = removeSuffix(jurorModelFilenamePrefix, ".model");
             prefix = removeSuffix(prefix, ".zip");
             this.jurorModelFilenamePrefixes.add(prefix);
         }
     }
 
     @Override
     protected synchronized Table loadTestSet(final DAVMode mode, final DAVOptions options,
                                              final FixedGeneList geneList,
                                              final List<Set<String>> labelValueGroups,
                                              final ObjectSet<String> testSampleIds)
             throws TypeMismatchException, InvalidColumnException, ColumnTypeException, IOException,
             ClassNotFoundException {
 
         if (!jurorModelsAreLoaded) {
             throw new IllegalStateException("juror models have not been loaded yet");
         }
 
         final GEOPlatformIndexed oldTrainingPlatform = options.trainingPlatform;
         int index = 0;
         for (final BDVModel jurorModel : jurorModels) {
             System.out.println("processing dataset for component model " + index++);
             options.datasetName = jurorModel.datasetName;
 
             Table processedTable = mode.processTable(jurorModel.getGeneList(),
                     options.inputTable, options, labelValueGroups, true, jurorModel.splitId, "predict");
 
             // several prediction datasets may exist (e.g., test, validation), so we clear the
             // cache from these data:
             // TODO enable cache removal. Commented to speed up debugging only.
             mode.removeFromCache(jurorModel.splitId, "predict", jurorModel.datasetName);
 
             options.probesetScaleMeanMap = jurorModel.probesetScaleMeanMap;
             options.probesetScaleRangeMap = jurorModel.probesetScaleRangeMap;
             mode.scaleFeatures(options, true, processedTable);
             // reload the platform. We changed it in processTable.
             options.trainingPlatform = oldTrainingPlatform;
 
             if (testSampleIds != null) {
                 // focus on a subset of samples in the input table: those in test-samples
                 processedTable = mode.filterSamples(processedTable, testSampleIds);
             }
 
             jurorModel.splitSpecificTestSet = processedTable;
         }
 
         // return the first test set, we just need it to extract sample ids..
         return jurorModels.get(0).splitSpecificTestSet;
     }
 
     /**
      * Add properties specific to the model type.
      * @param modelProperties The property object to add properties to.
      */
     @Override
     protected void addProperties(final Properties modelProperties) {
         // indicate this model is a consensus of juror models
         modelProperties.addProperty("bdval.consensus.model", true);
 
         // new consensus models have the juror models included in them
         modelProperties.addProperty("bdval.consensus.jurors.embedded", true);
 
         // names of the juror models
         int index = 0;
         for (final String prefix : jurorModelFilenamePrefixes) {
             modelProperties.addProperty("bdval.consensus.model." + Integer.toString(index++), prefix);
         }
 
         modelProperties.setProperty("scaling.implementation.classname",
                 "edu.cornell.med.icb.learning.NoScalingFeatureScaler");
     }
 
     /**
      * Loads a BDVal consensus model from disk. All juror models are loaded.
      * @param options specific options to use when loading the model
      * @throws IOException if there is a problem accessing the model
      * @throws ClassNotFoundException if the type of the model is not recognized
      */
     @Override
     public void load(final DAVOptions options) throws IOException, ClassNotFoundException {
         loadProperties(options);
         loadJurorModels(options);
     }
 
     /**
      * Loads the juror models used for consensus.
      * @param options specific options to use when loading the model
      * @throws IOException if there is a problem accessing the model
      * @throws ClassNotFoundException if the type of the model is not recognized
      */
     private void loadJurorModels(final DAVOptions options) throws IOException, ClassNotFoundException {
         jurorModels.clear();
 
         final String pathToModel = FilenameUtils.getFullPath(modelFilename);
         final String endpointName =
                 FilenameUtils.getBaseName(FilenameUtils.getPathNoEndSeparator(pathToModel));
 
         if (properties.getBoolean("bdval.consensus.jurors.embedded", false)) {
             final File tmpdir = File.createTempFile("juror-models", "");
             tmpdir.delete();
             tmpdir.mkdir();
 
             try {
                 // load juror models from the zip file
                 final ZipFile zipFile = new ZipFile(zipFilename);
                 for (final String jurorPrefix : jurorModelFilenamePrefixes) {
                     // zip files should always use "/" as a separator
                     final String jurorFilename =
                             "models/" + endpointName + "/" + jurorPrefix + ".zip";
                     LOG.debug("Loading juror model " + jurorFilename);
                     final InputStream jurorStream =
                             zipFile.getInputStream(zipFile.getEntry(jurorFilename));
 
                     final File jurorFile =
                             new File(FilenameUtils.concat(tmpdir.getPath(), jurorFilename));
 
                     // put the juror model to disk so it can be loaded with existing code
                     IOUtils.copy(jurorStream, FileUtils.openOutputStream(jurorFile));
 
                     final BDVModel jurorModel = new BDVModel(jurorFile.getPath());
                     jurorModel.load(options);
                     jurorModels.add(jurorModel);
                 }
             } finally {
                 FileUtils.forceDeleteOnExit(tmpdir);
             }
         } else {
             // load juror models from disk
             final File finalModelPath = new File(pathToModel);
             final File finalModelParentPath = new File(finalModelPath.getParent());
             // assume the model is under a directory "models" at the same level as a models
             // directory which contains the model components.
             for (final String jurorPrefix : jurorModelFilenamePrefixes) {
                 final String modelComponentFilename = finalModelParentPath.getParent()
                         + SystemUtils.FILE_SEPARATOR + "models" + SystemUtils.FILE_SEPARATOR
                         + endpointName + SystemUtils.FILE_SEPARATOR + jurorPrefix;
                 LOG.debug("Loading model component " + modelComponentFilename);
                 final BDVModel jurorModel = new BDVModel(modelComponentFilename);
                 jurorModel.load(options);
                 jurorModels.add(jurorModel);
             }
         }
 
         if (jurorModels.size() < 1) {
             throw new IllegalStateException("No juror models could be found");
         }
 
         jurorModelsAreLoaded = true;
     }
 
     /**
      * @param options specific options to use when loading the properties
      * @throws IOException if there is a problem accessing the properties
      */
     private void loadProperties(final DAVOptions options) throws IOException {
         final boolean zipExists = new File(zipFilename).exists();
         if (LOG.isDebugEnabled()) {
             LOG.debug("model zip file exists: " + BooleanUtils.toStringYesNo(zipExists));
         }
 
         properties.clear();
 
         // check to see if a zip file exists - if it doesn't we assume it's an old binary format
         if (zipModel && zipExists) {
             LOG.info("Reading model from filename: " + zipFilename);
 
             final ZipFile zipFile = new ZipFile(zipFilename);
             try {
                 final ZipEntry propertyEntry =
                         zipFile.getEntry(FilenameUtils.getName(modelPropertiesFilename));
                 // load properties
                 properties.clear();
                 properties.addAll(loadProperties(zipFile.getInputStream(propertyEntry), options));
             } finally {
                 try {
                     zipFile.close();
                 } catch (IOException e) {
                     // NOPMD - ignore since there is not much we can do anyway
                 }
             }
         } else {
             final File propertyFile =
                     new File(modelFilenamePrefix + "." + ModelFileExtension.props.toString());
             if (propertyFile.exists() && propertyFile.canRead()) {
                 LOG.debug("Loading properties from " + propertyFile.getAbsolutePath());
                 properties.addAll(loadProperties(FileUtils.openInputStream(propertyFile), options));
             }
         }
     }
 
     /**
      * Save the model to a set of files. The files will contain all the information needed to
      * apply the BDVal model to new samples.
      *
      * NOTE: For consensus models, the juror models must be loaded by calling
      * {@link #load(DAVOptions)} before this method is called
      *
      * @param options The options associated with this model
      * @param task The classification task used for this model
      * @param splitPlan The split plan used to generat this model
      * @param writeModelMode The mode saving the model
      * @throws IOException if there is a problem writing to the files
      */
     @Override
     public void save(final DAVOptions options, final ClassificationTask task,
                      final SplitPlan splitPlan, final WriteModel writeModelMode)
             throws IOException {
         if (zipModel) {
             LOG.info("Writing model to filename: " + zipFilename);
             ZipOutputStream zipStream = null;
             try {
                 // Create the ZIP file
                 zipStream = new ZipOutputStream(new FileOutputStream(zipFilename));
                 setZipStreamComment(zipStream);
 
                 // Add ZIP entry for the model properties to output stream.
                 saveProperties(zipStream, options, task, splitPlan, writeModelMode);
 
                 // the juror models will actually be contained within the consensus model
                 saveJurorModels(zipStream, options, task, splitPlan, writeModelMode);
             } finally {
                 IOUtils.closeQuietly(zipStream);
             }
         } else {
             LOG.info("Writing model properties to filename: " + modelPropertiesFilename);
             saveProperties(FileUtils.openOutputStream(new File(modelPropertiesFilename)),
                     options, task, splitPlan, writeModelMode);
             // NOTE: old code never saved the juror models
         }
     }
 
     /**
      * Save the juror models to a set of files. The files will contain all the information needed
      * to apply the BDVal model to new samples.
      *
      * @param zipStream The stream used to write the models to
      * @param options The options associated with this model
      * @param task The classification task used for this model
      * @param splitPlan The split plan used to generat this model
      * @param writeModelMode The mode saving the model
      * @throws IOException if there is a problem writing to the files
      */
     private synchronized void saveJurorModels(final ZipOutputStream zipStream,
                                               final DAVOptions options,
                                               final ClassificationTask task,
                                               final SplitPlan splitPlan,
                                               final WriteModel writeModelMode) throws IOException {
         if (!jurorModelsAreLoaded) {
             throw new IllegalStateException("juror models must be loaded before save");
         }
 
         // add the models directory entry for juror models (must end in "/")
         zipStream.putNextEntry(new ZipEntry(JUROR_MODEL_DIRECTORY + "/"));
         zipStream.closeEntry();
 
         final String jurorModelDirectory = JUROR_MODEL_DIRECTORY + "/" + FilenameUtils.getBaseName(
                 FilenameUtils.getPathNoEndSeparator(modelFilenamePrefix)) + "/";
         zipStream.putNextEntry(new ZipEntry(jurorModelDirectory));
         zipStream.closeEntry();
 
         for (final BDVModel jurorModel : jurorModels) {
             // NOTE: for zip files, the directory MUST use "/" regardless of OS
 
             final String jurorModelFilename = jurorModelDirectory
                     + FilenameUtils.getName(jurorModel.getModelFilenamePrefix()) + ".zip";
 
             // Add ZIP entry for the model to output stream.
             zipStream.putNextEntry(new ZipEntry(jurorModelFilename));
 
             // Create the ZIP file for the juror
             LOG.debug("Writing juror model as entry: " + jurorModelFilename);
             final ZipOutputStream jurorZipStream = new ZipOutputStream(zipStream);
             jurorZipStream.setComment("Juror model: " + jurorModel.getModelFilenamePrefix());
             jurorModel.save(jurorZipStream, options, task, splitPlan, writeModelMode);
             jurorZipStream.finish(); // NOTE: we don't close the stream, that will close everything
 
             zipStream.closeEntry();
         }
     }
 
     @Override
     public double predict(final int sampleIndex, final double[] probabilities) {
         for (int j = 0; j < probabilities.length; j++) {
             probabilities[j] = 0;
         }
         final double[] localProbs = new double[probabilities.length];
         final DoubleList jurorDecisionPositive = new DoubleArrayList();
         final DoubleList jurorDecisionNegative = new DoubleArrayList();
         for (final BDVModel jurorModel : jurorModels) {
             final double jurorDecision =
                     jurorModel.predict(jurorModel.modelSpecificProblem, sampleIndex, localProbs);
             final double probability = Math.max(localProbs[0], localProbs[1]);
 
             if (jurorDecision > 0) {
                 jurorDecisionPositive.add(probability);
             } else {
                 jurorDecisionNegative.add(probability);
             }
 
         }
         final double consensusDecision =
                 jurorDecisionPositive.size() > jurorDecisionNegative.size() ? 1 : -1;
         final DoubleList toSum =
                 consensusDecision == 1 ? jurorDecisionPositive : jurorDecisionNegative;
         double consensusProbability = 0;
         for (final double value : toSum) {
             consensusProbability += value;
         }
         consensusProbability /= (double) toSum.size();
         probabilities[0] = consensusProbability;
 
         return consensusDecision;
     }
 
     /**
      * Return the average number of features in the juror models.
      *
      * @return number of features averaged over juror models.
      */
     @Override
     public int getNumberOfFeatures() {
         double numFeatures = 0;
         for (final BDVModel jurorModel : jurorModels) {
             numFeatures += jurorModel.getGeneList().getNumberOfProbesets();
         }
         numFeatures /= (double) jurorModels.size();
         return (int) numFeatures;
     }
 
     @Override
     public void prepareClassificationProblem(final Table testSet)
             throws InvalidColumnException, TypeMismatchException {
         for (final BDVModel jurorModel : jurorModels) {
             assert jurorModel.splitSpecificTestSet != null
                     : "split-specific test set must have been pre-populated.";
 
             jurorModel.checkReOrderTestSet(jurorModel.splitSpecificTestSet);
             jurorModel.modelSpecificProblem =
                     loadProblem(jurorModel.getHelper(), jurorModel.splitSpecificTestSet);
         }
     }
 
     /**
      * Calculate the union of gene lists used by the juror models.
      *
      * @param options specific options to use when loading the model
      * @return A gene list built from the juror models
      */
     @Override
     protected FixedGeneList convertTrainingPlatformToGeneList(final DAVOptions options) {
         final ObjectSet<String> probeIds = new ObjectOpenHashSet<String>();
 
         for (final BDVModel jurorModel : jurorModels) {
             for (int probeIndex = 0; probeIndex < jurorModel.trainingPlatform.getNumProbeIds(); probeIndex++) {
                 probeIds.add(jurorModel.trainingPlatform.getProbesetIdentifier(probeIndex).toString());
             }
         }
         return new FixedGeneList(probeIds.toArray(new String[probeIds.size()]));
     }
 
     /**
      * Is this model an consensus of other models?
      *
      * @return true if this model is a consensus model
      */
     @Override
     public boolean isConsensusModel() {
         return true;
     }
 }
