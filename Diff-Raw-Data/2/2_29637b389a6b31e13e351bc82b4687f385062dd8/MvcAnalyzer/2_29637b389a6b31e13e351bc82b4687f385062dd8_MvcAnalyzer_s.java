 package mx.itesm.web2mexadl.mvc;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import mx.itesm.web2mexadl.dependencies.ClassDependencies;
 import mx.itesm.web2mexadl.dependencies.DependenciesUtil;
 import mx.itesm.web2mexadl.dependencies.DependencyAnalyzer;
 import mx.itesm.web2mexadl.util.Util;
 import mx.itesm.web2mexadl.util.Util.Variable;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang3.StringUtils;
 
 import weka.core.FastVector;
 import weka.core.Instance;
 import weka.core.Instances;
 
 /**
  * Classify components in a web project according to the MVC pattern.
  * 
  * @author jccastrejon
  * 
  */
 public class MvcAnalyzer {
 
     /**
      * Class logger.
      */
     private static Logger logger = Logger.getLogger(MvcAnalyzer.class.getName());
 
     /**
      * Classify each class within the specified path into one of the layers of
      * the MVC pattern.
      * 
      * @param path
      *            Path to the directory containing the classes.
      * @param includeExternal
      *            Should the external dependencies be exported.
      * @param outputFile
      *            File where to export the classification results.
      * @return Map containing the classification results for each class.
      * @throws Exception
      *             If an Exception occurs during classification.
      */
     public static Map<String, Layer> classifyClassesInDirectory(final File path, final boolean includeExternal,
             final File outputFile) throws Exception {
         Map<String, Layer> returnValue;
         List<ClassDependencies> dependencies;
         Map<String, Set<String>> internalPackages;
 
         // Classify each class in the specified path
         dependencies = DependencyAnalyzer.getDirectoryDependencies(path.getAbsolutePath(), new MvcDependencyCommand());
         internalPackages = DependenciesUtil.getInternalPackages(dependencies,
                 Util.getPropertyValues(Util.Variable.Type.getVariableName()));
         returnValue = MvcAnalyzer.generateArchitecture(dependencies, internalPackages, outputFile.getParentFile());
 
         if (outputFile != null) {
             DependenciesUtil.exportDependenciesToSVG(dependencies, includeExternal, outputFile, internalPackages,
                     new MvcExportCommand(returnValue));
         }
 
         return returnValue;
     }
 
     /**
      * Classify each class within the specified WAR file into one of the layers
      * of the MVC pattern.
      * 
      * @param file
      *            Path to the WAR file.
      * @param includeExternal
      *            Should the external dependencies be exported.
      * @param outputFile
      *            File where to export the classification results.
      * @return Map containing the classification results for each class.
      * @throws Exception
      *             If an Exception occurs during classification.
      */
     public static Map<String, Layer> classifyClassesinWar(final File file, final boolean includeExternal,
             final File outputFile) throws Exception {
         Map<String, Layer> returnValue;
         List<ClassDependencies> dependencies;
         Map<String, Set<String>> internalPackages;
 
         // Classify each class in the specified war
         dependencies = DependencyAnalyzer.getWarDependencies(file.getAbsolutePath(), new MvcDependencyCommand());
 
         // Remove the WEB-INF.classes prefix
         for (ClassDependencies dependency : dependencies) {
             dependency.setClassName(dependency.getClassName().replace("WEB-INF.classes.", ""));
             dependency.setPackageName(dependency.getPackageName().replace("WEB-INF.classes.", ""));
         }
 
         internalPackages = DependenciesUtil.getInternalPackages(dependencies,
                 Util.getPropertyValues(Util.Variable.Type.getVariableName()));
         returnValue = MvcAnalyzer.generateArchitecture(dependencies, internalPackages, outputFile.getParentFile());
 
         if (outputFile != null) {
             DependenciesUtil.exportDependenciesToSVG(dependencies, includeExternal, outputFile, internalPackages,
                     new MvcExportCommand(returnValue));
         }
 
         return returnValue;
     }
 
     /**
      * Classify each class in the specified List into one of the layers of the
      * MVC pattern.
      * 
      * @param dependencies
      *            List containing the dependencies for each class to classify.
      * @param internalPackages
      *            Project's internal packages.
      * @return Map containing the classification layer for each class.
      * @throws Exception
      *             If an Exception occurs during classification.
      */
     private static Map<String, Layer> generateArchitecture(final List<ClassDependencies> dependencies,
             final Map<String, Set<String>> internalPackages, final File outputDir) throws Exception {
         int viewCount;
         int modelCount;
         int instanceLayer;
         Instance instance;
         boolean valueFound;
         int controllerCount;
         Instances instances;
         String instanceType;
         String[] typeValues;
         Layer componentLayer;
         String[] suffixValues;
         Layer dependencyLayer;
         FastVector attributes;
         String[] externalApiValues;
         Map<String, Layer> returnValue;
         Set<String> currentPackageContent;
         Map<String, Layer> packagesClassification;
         Map<String, String[]> externalApiPackages;
         StringBuilder modelPackages;
         StringBuilder viewPackages;
         StringBuilder controllerPackages;
 
         // Model variables
         attributes = new FastVector();
         for (Variable variable : Variable.values()) {
             attributes.addElement(variable.getAttribute());
         }
 
         // Layer variable
         attributes.addElement(Layer.attribute);
 
         // Set the test instances, the Layer variable is unknown
         instances = new Instances("mvc", attributes, 0);
         instances.setClassIndex(Variable.values().length);
 
         // Valid suffixes to look for in the class names
         suffixValues = Util.getPropertyValues(Util.Variable.Suffix.getVariableName());
 
         // Valid file types to look for in the component names
         typeValues = Util.getPropertyValues(Util.Variable.Type.getVariableName());
 
         // Valid external api packages to look for in the classes dependencies
         externalApiValues = Util.getPropertyValues(Util.Variable.ExternalAPI.getVariableName());
         externalApiPackages = new HashMap<String, String[]>(externalApiValues.length);
         for (int i = 0; i < externalApiValues.length; i++) {
             if (!externalApiValues[i].equals("none")) {
                 externalApiPackages.put(externalApiValues[i],
                         Util.getPropertyValues("externalApi." + externalApiValues[i] + ".packages"));
             }
         }
 
         returnValue = new HashMap<String, Layer>(dependencies.size());
         for (ClassDependencies classDependencies : dependencies) {
             // Variables + Layer
             instance = new Instance(Variable.values().length + 1);
 
             // Type
             instanceType = "java";
             for (String validType : typeValues) {
                 if (classDependencies.getClassName().endsWith("." + validType)) {
                     instanceType = validType;
                     break;
                 }
             }
             instance.setValue(Variable.Type.getAttribute(), instanceType);
 
             // ExternalAPI
             valueFound = false;
             externalApi: for (String externalApi : externalApiValues) {
                 if (externalApi.equals("none")) {
                     continue;
                 }
 
                 // Check if any of the class' external dependencies match with
                 // one of the key external dependencies
                 if (classDependencies.getExternalDependencies() != null) {
                     for (String externalDependency : classDependencies.getExternalDependencies()) {
                         for (String externalPackage : externalApiPackages.get(externalApi)) {
                             if (externalDependency.toLowerCase().startsWith(externalPackage)) {
                                 valueFound = true;
                                 instance.setValue(Variable.ExternalAPI.getAttribute(), externalApi);
                                 break externalApi;
                             }
                         }
                     }
                 }
             }
 
             // No key external dependency found
             if (!valueFound) {
                 instance.setValue(Variable.ExternalAPI.getAttribute(), "none");
             }
 
             // Suffix
             valueFound = false;
             for (String suffix : suffixValues) {
                 if (classDependencies.getClassName().toLowerCase().endsWith(suffix)) {
                     valueFound = true;
                     instance.setValue(Variable.Suffix.getAttribute(), suffix);
                     break;
                 }
             }
 
             // No key suffix found
             if (!valueFound) {
                 instance.setValue(Variable.Suffix.getAttribute(), "none");
             }
 
             // Layer, the unknown variable
             instance.setMissing(Layer.attribute);
             instances.add(instance);
             instance.setDataset(instances);
 
             try {
                 instanceLayer = (int) Util.classifier.classifyInstance(instance);
             } catch (Exception e) {
                 // Default value
                 instanceLayer = 0;
                 logger.severe("Unable to classify: " + instance);
             }
 
             returnValue.put(classDependencies.getClassName(), Layer.values()[instanceLayer]);
             logger.info(classDependencies.getClassName() + " : " + returnValue.get(classDependencies.getClassName()));
         }
 
         // Check for any invalid relation
         viewPackages = new StringBuilder();
         modelPackages = new StringBuilder();
         controllerPackages = new StringBuilder();
         packagesClassification = new HashMap<String, Layer>(internalPackages.size());
         for (String currentPackage : internalPackages.keySet()) {
             modelCount = viewCount = controllerCount = 0;
             currentPackageContent = internalPackages.get(currentPackage);
 
             for (String component : currentPackageContent) {
                 componentLayer = returnValue.get(component);
                 if (componentLayer == Layer.Model) {
                     modelCount++;
                 } else if (componentLayer == Layer.View) {
                     viewCount++;
                 } else if (componentLayer == Layer.Controller) {
                     controllerCount++;
                 }
             }
 
             if ((modelCount > viewCount) && (modelCount > controllerCount)) {
                 packagesClassification.put(currentPackage, Layer.Model);
                 Util.addImplementationPackage(modelPackages, currentPackage);
             } else if ((viewCount > modelCount) && (viewCount > controllerCount)) {
                 packagesClassification.put(currentPackage, Layer.View);
                 Util.addImplementationPackage(viewPackages, currentPackage);
             } else if ((controllerCount > viewCount) && (controllerCount > modelCount)) {
                 packagesClassification.put(currentPackage, Layer.Controller);
                 Util.addImplementationPackage(controllerPackages, currentPackage);
             } else {
                 packagesClassification.put(currentPackage, null);
             }
         }
 
         for (ClassDependencies classDependencies : dependencies) {
             // Code relations
             valueFound = false;
             componentLayer = returnValue.get(classDependencies.getClassName());
             if (classDependencies.getInternalDependencies() != null) {
                 for (String internalDependency : classDependencies.getInternalDependencies()) {
                     dependencyLayer = returnValue.get(internalDependency);
 
                     if (!componentLayer.isValidRelation(dependencyLayer)) {
                         valueFound = true;
                         returnValue.put(classDependencies.getClassName(), Layer.valueOf("Invalid" + componentLayer));
                         logger.info("Invalid relation detected between: " + classDependencies.getClassName() + " and "
                                 + internalDependency);
                     }
                 }
             }
 
             // Package relations
             if (!valueFound) {
                 dependencyLayer = packagesClassification.get(classDependencies.getPackageName());
 
                 if ((dependencyLayer != null) && (componentLayer != dependencyLayer)) {
                     returnValue.put(classDependencies.getClassName(), Layer.valueOf("Invalid" + componentLayer));
                 }
             }
         }
 
         // Export MexADL architecture
         MvcAnalyzer.exportToMexADL(outputDir, modelPackages.toString(), controllerPackages.toString(),
                 viewPackages.toString());
 
         return returnValue;
     }
 
     /**
      * 
      * @param modelPackages
      * @param controllerPackages
      * @param viewPackages
      * @throws IOException
      */
     public static void exportToMexADL(final File outputDir, final String modelPackages,
             final String controllerPackages, final String viewPackages) throws IOException {
         File outputFile;
         String outputContents;
 
         // Initial template
         outputFile = new File(outputDir, "architecture.xml");
         FileUtils.deleteQuietly(outputFile);
         FileUtils.copyInputStreamToFile(
                MvcAnalyzer.class.getResourceAsStream("/mx/itesm/web2mexadl/templates/ArchitectureTemplate.xml"), outputFile);
 
         // Update template with implementation packages
         outputContents = FileUtils.readFileToString(outputFile, "UTF-8");
         outputContents = StringUtils.replace(outputContents, "<!-- Model implementation -->", modelPackages);
         outputContents = StringUtils.replace(outputContents, "<!-- View implementation -->", viewPackages);
         outputContents = StringUtils.replace(outputContents, "<!-- Controller implementation -->", controllerPackages);
 
         // Final architecture
         FileUtils.write(outputFile, outputContents);
     }
 }
