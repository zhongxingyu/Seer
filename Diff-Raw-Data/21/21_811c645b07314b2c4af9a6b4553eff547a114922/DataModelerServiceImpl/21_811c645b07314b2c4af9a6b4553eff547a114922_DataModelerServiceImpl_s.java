 package org.jbpm.datamodeler.editor.backend.server;
 
 import org.jboss.errai.bus.server.annotations.Service;
 import org.jbpm.datamodeler.codegen.GenerationContext;
 import org.jbpm.datamodeler.codegen.GenerationEngine;
 import org.jbpm.datamodeler.codegen.GenerationListener;
 import org.jbpm.datamodeler.commons.NamingUtils;
 import org.jbpm.datamodeler.commons.file.FileScanner;
 import org.jbpm.datamodeler.commons.file.ScanResult;
 import org.jbpm.datamodeler.core.AnnotationDefinition;
 import org.jbpm.datamodeler.core.DataModel;
 import org.jbpm.datamodeler.core.PropertyType;
 import org.jbpm.datamodeler.core.impl.DataModelImpl;
 import org.jbpm.datamodeler.core.impl.PropertyTypeFactoryImpl;
 import org.jbpm.datamodeler.driver.impl.DataModelOracleDriver;
 import org.jbpm.datamodeler.editor.model.AnnotationDefinitionTO;
 import org.jbpm.datamodeler.editor.model.DataModelTO;
 import org.jbpm.datamodeler.editor.model.DataObjectTO;
 import org.jbpm.datamodeler.editor.model.PropertyTypeTO;
 import org.jbpm.datamodeler.editor.service.DataModelerService;
 import org.jbpm.datamodeler.editor.service.ServiceException;
 import org.jbpm.datamodeler.validation.ValidationUtils;
 import org.kie.commons.io.IOService;
 import org.kie.commons.java.nio.IOException;
 import org.kie.commons.java.nio.file.Files;
 import org.kie.guvnor.datamodel.events.InvalidateDMOProjectCacheEvent;
 import org.kie.guvnor.datamodel.model.ModelField;
 import org.kie.guvnor.datamodel.oracle.ProjectDataModelOracle;
 import org.kie.guvnor.datamodel.service.DataModelService;
 import org.kie.guvnor.project.service.ProjectService;
 import org.kie.guvnor.services.metadata.MetadataService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.uberfire.backend.server.util.Paths;
 import org.uberfire.backend.vfs.Path;
 import org.uberfire.client.workbench.widgets.events.ChangeType;
 import org.uberfire.client.workbench.widgets.events.ResourceBatchChangesEvent;
 import org.uberfire.client.workbench.widgets.events.ResourceChange;
 
 import javax.enterprise.context.ApplicationScoped;
 import javax.enterprise.event.Event;
 import javax.inject.Inject;
 import javax.inject.Named;
 import java.util.*;
 
 @Service
 @ApplicationScoped
 public class DataModelerServiceImpl implements DataModelerService {
 
     private static final Logger logger = LoggerFactory.getLogger(DataModelerServiceImpl.class);
 
     private static final String MAIN_JAVA_PATH = "src/main/java";
     private static final String MAIN_RESOURCES_PATH = "src/main/resources";
     private static final String TEST_JAVA_PATH        = "src/test/java";
     private static final String TEST_RESOURCES_PATH   = "src/test/resources";
 
     private static final String DEFAULT_GUVNOR_PKG = "defaultpkg";
 
     @Inject
     @Named("ioStrategy")
     IOService ioService;
 
     @Inject
     private Paths paths;
 
     @Inject
     private ProjectService projectService;
 
     @Inject
     private MetadataService metadataService;
 
     @Inject
     private DataModelService dataModelService;
 
 
     @Inject
     private Event<InvalidateDMOProjectCacheEvent> invalidateDMOProjectCache;
 
     @Inject
     private Event<ResourceBatchChangesEvent> resourceBatchChangesEvent;
 
 
     public DataModelerServiceImpl() {
     }
 
     @Override
     public DataModelTO loadModel(Path path) {
 
         if (logger.isDebugEnabled()) logger.debug("Loading data model from path: " + path);
 
         DataModel dataModel = null;
         Path projectPath = null;
 
         try {
              projectPath = projectService.resolveProject(path);
             if (logger.isDebugEnabled()) logger.debug("Current project path is: " + projectPath);
 
             dataModel = new DataModelImpl();
 
             ProjectDataModelOracle projectDataModelOracle = dataModelService.getProjectDataModel(projectPath);
             DataModelOracleDriver driver = new DataModelOracleDriver();
             driver.addOracleModel(dataModel, projectDataModelOracle);
 
             //Objects read from persistent .java format are tagged as PERSISTENT objects
             DataModelTO dataModelTO = DataModelerServiceHelper.getInstance().domain2To(dataModel, DataObjectTO.PERSISTENT);
             dataModelTO.setDefaultPackage(DEFAULT_GUVNOR_PKG);
 
             printProjectDataModelOracle(path);
 
             return dataModelTO;
 
         } catch (Exception e) {
             logger.error("Data model couldn't be loaded, path: " + path + ", projectPath: " + projectPath + ".", e);
             throw new ServiceException("Data model couldn't be loaded, path: " + path + ", projectPath: " + projectPath + ".", e);
         }
     }
 
     @Override
     public void saveModel(DataModelTO dataModel, final Path path) {
         
         try {
 
             //get the path to project root directory (the main pom.xml directory) and calculate
             //the java sources path
             Path projectPath = projectService.resolveProject(path);
 
             //ensure java sources directory exists.
             org.kie.commons.java.nio.file.Path javaPath = ensureProjectJavaPath(paths.convert(projectPath));
 
             //clean the files that needs to be deleted
             List<FileChangeDescriptor> fileChanges = cleanupFiles(dataModel, javaPath);
 
             //convert to domain model
             DataModel dataModelDomain = DataModelerServiceHelper.getInstance().to2Domain(dataModel);
 
             GenerationContext generationContext = new GenerationContext(dataModelDomain);
             ServiceGenerationListener generationListener = new ServiceGenerationListener(javaPath);
             generationContext.setGenerationListener(generationListener);
 
            //This output path is only for testing purposes, TODO REMOVE IT!
            generationContext.setOutputPath("/tmp");

             invalidateDMOProjectCache.fire( new InvalidateDMOProjectCacheEvent( projectPath ) );
 
             GenerationEngine generationEngine = GenerationEngine.getInstance();
             generationEngine.generate(generationContext);
 
             fileChanges.addAll(generationListener.getFileChanges());
             notifyFileChanges(fileChanges);
 
         } catch (Exception e) {
             logger.error("An error was produced during data model generation, dataModel: " + dataModel + ", path: " + path, e);
             throw new ServiceException("Data model: " + dataModel.getName() + ", couldn't be generated due to the following error. " + e);
         }
     }
 
     private void notifyFileChanges(List<FileChangeDescriptor> fileChanges) {
 
         //keep this class FileChangeDescriptor and this iteration until we are sure we don't need to manage
         //any other information that can't be stored in the ResourceChange definition.
         Set<ResourceChange> batchChanges = new HashSet<ResourceChange>();
         for (FileChangeDescriptor fileChange : fileChanges) {
             switch (fileChange.action) {
                 case FileChangeDescriptor.ADD:
                     logger.debug("Notifying file created: " + fileChange.getPath());
                     batchChanges.add(new ResourceChange(ChangeType.ADD, fileChange.getPath()));
                     break;
                 case FileChangeDescriptor.DELETE:
                     logger.debug("Notifying file deleted: " + fileChange.getPath());
                     batchChanges.add(new ResourceChange(ChangeType.DELETE, fileChange.getPath()));
                     break;
                 case FileChangeDescriptor.UPDATE:
                     logger.debug("Notifying file updated: " + fileChange.getPath());
                     batchChanges.add(new ResourceChange(ChangeType.UPDATE, fileChange.getPath()));
                     break;
             }
         }
         if (batchChanges.size() > 0) {
             resourceBatchChangesEvent.fire(new ResourceBatchChangesEvent(batchChanges));
         }
     }
 
 
     /**
      * This auxiliary method deletes the files that belongs to data objects that was removed in memory.
      *
      */
     private List<FileChangeDescriptor> cleanupFiles(DataModelTO dataModel, org.kie.commons.java.nio.file.Path javaPath) {
 
         List<DataObjectTO> currentObjects = dataModel.getDataObjects();
         List<DataObjectTO> deletedObjects = dataModel.getDeletedDataObjects();
         List<FileChangeDescriptor> fileChanges = new ArrayList<FileChangeDescriptor>();
         org.kie.commons.java.nio.file.Path filePath;
 
         //process deleted persistent objects.
         for (DataObjectTO dataObject : deletedObjects) {
             if (dataObject.isPersistent()) {
                 filePath = calculateFilePath(dataObject.getOriginalClassName(), javaPath);
                if (dataModel.getDataObjectByClassName(dataObject.getClassName()) != null) {
                     //TODO check if we need to have this level of control or instead we remove this file too.
                     //very particular case a persistent object was deleted in memory and a new one with the same name
                     //was created. At the end we will have a file update instead of a delete.
 
                     //do nothing, the file generator will notify that the file changed.
                     //fileChanges.add(new FileChangeDescriptor(paths.convert(filePath), FileChangeDescriptor.UPDATE));
                 } else {
                     fileChanges.add(new FileChangeDescriptor(paths.convert(filePath), FileChangeDescriptor.DELETE));
                     ioService.delete(filePath);
                 }
             }
         }
 
         //process package or class name changes for persistent objects.
         for (DataObjectTO dataObject : currentObjects) {
             if (dataObject.isPersistent() && dataObject.classNameChanged()) {
                 //if the className changes the old file needs to be removed
                 filePath = calculateFilePath(dataObject.getOriginalClassName(), javaPath);
 
                if (dataModel.getDataObjectByClassName(dataObject.getClassName()) != null) {
                     //TODO check if we need to have this level of control or instead we remove this file too.
                     //very particular case of change, a persistent object changes the name to the name of another
                     //object. A kind of name swapping...
 
                     //do nothing, the file generator will notify that the file changed.
                     //fileChanges.add(new FileChangeDescriptor(paths.convert(filePath), FileChangeDescriptor.UPDATE));
                 } else {
                     fileChanges.add(new FileChangeDescriptor(paths.convert(filePath), FileChangeDescriptor.DELETE));
                     ioService.delete(filePath);
                 }
             }
         }
 
         return  fileChanges;
     }
 
     @Override
     public Path createModel(Path context, String fileName) {
 
         //TODO remove this method.
         //with the last definition we never create a model file.
 
         return null;
     }
 
     @Override
     public Path resolveProject(Path path) {
         return projectService.resolveProject(path);
     }
 
     public List<PropertyTypeTO> getBasePropertyTypes() {
         List<PropertyTypeTO> types = new ArrayList<PropertyTypeTO>();
         
         for (PropertyType baseType : PropertyTypeFactoryImpl.getInstance().getBasePropertyTypes()) {
             types.add(new PropertyTypeTO(baseType.getName(), baseType.getClassName()));
         }
         return types;
     }
 
     public class ServiceGenerationListener implements GenerationListener {
 
         org.kie.commons.java.nio.file.Path output;
         List<FileChangeDescriptor> fileChanges = new ArrayList<FileChangeDescriptor>();
 
         public ServiceGenerationListener(org.kie.commons.java.nio.file.Path output) {
             this.output = output;
         }
 
         @Override
         public void assetGenerated(String fileName, String content) {
 
             String subDir;
             org.kie.commons.java.nio.file.Path subDirPath;
             org.kie.commons.java.nio.file.Path destFilePath;
             StringTokenizer dirNames;
 
             subDirPath = output;
             int index = fileName.lastIndexOf("/");
             if (index == 0) {
                 //the file names was provided in the form /SomeFile.java
                 fileName = fileName.substring(1, fileName.length());
             } else if (index > 0) {
                 //the file name was provided in the most common form /dir1/dir2/SomeFile.java
                 String dirNamesPath = fileName.substring(0, index);
                 fileName = fileName.substring(index+1, fileName.length());
                 dirNames = new StringTokenizer(dirNamesPath, "/");
                 while (dirNames.hasMoreElements()) {
                     subDir = dirNames.nextToken();
                     subDirPath = subDirPath.resolve(subDir);
                     if (!ioService.exists(subDirPath)) {
                         ioService.createDirectory(subDirPath);
                     }
                 }
             }
 
             //the last subDirPath is the directory to crate the file.
             destFilePath = subDirPath.resolve(fileName);
             boolean exists = ioService.exists(destFilePath);
 
             ioService.write(destFilePath, content);
 
             if (!exists) {
                 logger.debug("Genertion listener created a new file: " + destFilePath);
                 fileChanges.add(new FileChangeDescriptor(paths.convert(destFilePath), FileChangeDescriptor.ADD));
             } else {
                 logger.debug("Generation listener modified file: " + destFilePath);
                 fileChanges.add(new FileChangeDescriptor(paths.convert(destFilePath), FileChangeDescriptor.UPDATE));
             }
         }
 
         public List<FileChangeDescriptor> getFileChanges() {
             return fileChanges;
         }
     }
 
     public Path resolveResourcePackage(final Path resource) {
 
         //TODO this method should be moved to the ProjectService class
         //Null resource paths cannot resolve to a Project
         if ( resource == null ) {
             return null;
         }
 
         //If Path is not within a Project we cannot resolve a package
         final Path projectRoot = projectService.resolveProject(resource);
         if ( projectRoot == null ) {
             return null;
         }
 
         //The Path must be within a Project's src/main/resources or src/test/resources path
         boolean resolved = false;
         org.kie.commons.java.nio.file.Path path = paths.convert( resource ).normalize();
         final org.kie.commons.java.nio.file.Path srcResourcesPath = paths.convert( projectRoot ).resolve(MAIN_RESOURCES_PATH);
         final org.kie.commons.java.nio.file.Path testResourcesPath = paths.convert( projectRoot ).resolve( TEST_RESOURCES_PATH );
 
         if ( path.startsWith( srcResourcesPath ) ) {
             resolved = true;
         } else if ( path.startsWith( testResourcesPath ) ) {
             resolved = true;
         }
         if ( !resolved ) {
             return null;
         }
 
         //If the Path is already a folder simply return it
         if ( Files.isDirectory(path) ) {
             return resource;
         }
 
         path = path.getParent();
 
         return paths.convert( path );
     }
 
     @Override
     public Boolean isValidIdentifier(String identifier) {
         return ValidationUtils.isJavaIdentifier(identifier);
     }
 
     @Override
     public Map<String, Boolean> evaluateIdentifiers(String[] identifiers) {
         Map<String, Boolean> result = new HashMap<String, Boolean>(identifiers.length);
         if (identifiers != null && identifiers.length > 0) {
             for (String s : identifiers) {
                 result.put(s, ValidationUtils.isJavaIdentifier(s));
             }
         }
         return result;
     }
 
     @Override
     public Map<String, AnnotationDefinitionTO> getAnnotationDefinitions() {
         Map<String, AnnotationDefinitionTO> annotations = new HashMap<String, AnnotationDefinitionTO>();
         DataModelOracleDriver oracleDriver = new DataModelOracleDriver();
         List<AnnotationDefinition> annotationDefinitions = oracleDriver.getConfiguredAnnotations();
         AnnotationDefinitionTO annotationDefinitionTO;
         DataModelerServiceHelper serviceHelper = DataModelerServiceHelper.getInstance();
 
         for (AnnotationDefinition annotationDefinition : annotationDefinitions) {
             annotationDefinitionTO = serviceHelper.domain2To(annotationDefinition);
             annotations.put(annotationDefinitionTO.getClassName(), annotationDefinitionTO);
         }
         return annotations;
     }
 
     private org.kie.commons.java.nio.file.Path existsProjectJavaPath(org.kie.commons.java.nio.file.Path projectPath) {
         org.kie.commons.java.nio.file.Path javaPath = projectPath.resolve("src").resolve("main").resolve("java");
         if (ioService.exists(javaPath)) {
             return javaPath;
         }
         return null;
     }
 
     private org.kie.commons.java.nio.file.Path ensureProjectJavaPath(org.kie.commons.java.nio.file.Path projectPath) {
         org.kie.commons.java.nio.file.Path javaPath = projectPath.resolve("src");
         if (!ioService.exists(javaPath)) {
             javaPath = ioService.createDirectory(javaPath);
         }
         javaPath = javaPath.resolve("main");
         if (!ioService.exists(javaPath)) {
             javaPath = ioService.createDirectory(javaPath);
         }
         javaPath = javaPath.resolve("java");
         if (!ioService.exists(javaPath)) {
             javaPath = ioService.createDirectory(javaPath);
         }
 
         return javaPath;
     }
 
     /**
      * Given a className calculates the path to the java file allocating the corresponding pojo.
      *
      */
     private org.kie.commons.java.nio.file.Path calculateFilePath(String className, org.kie.commons.java.nio.file.Path javaPath) {
 
         String name = NamingUtils.getInstance().extractClassName(className);
         String packageName = NamingUtils.getInstance().extractPackageName(className);
         org.kie.commons.java.nio.file.Path filePath = javaPath;
 
         if (packageName != null) {
             List<String> packageNameTokens = NamingUtils.getInstance().tokenizePackageName(packageName);
             for (String token : packageNameTokens) {
                 filePath = filePath.resolve(token);
             }
         }
 
         filePath = filePath.resolve(name + ".java");
         return filePath;
     }
 
     private String calculateDefaultPackageName(Path resourceFilePath) {
         String packageName = null;
 
         packageName = projectService.resolvePackageName(resourceFilePath);
 
         if (packageName == null) {
             return DEFAULT_GUVNOR_PKG;
         } else {
             return packageName;
         }
     }
 
     private List<Path> calculateProjectPackages(IOService ioService, Path path) throws IOException {
             
         Collection<ScanResult> scanResults;
         List<Path> results = new ArrayList<Path>();
 
         FileScanner fileScanner = new FileScanner();
 
         Path projectHome = projectService.resolveProject(path);
         org.kie.commons.java.nio.file.Path javaPath = existsProjectJavaPath(paths.convert(projectHome));
         if (javaPath != null) {
             scanResults = fileScanner.scanDirectories(ioService, javaPath, false, true);
             for (ScanResult scanResult : scanResults) {
                 results.add(paths.convert(scanResult.getFile()));
             }
 
         }
         return results;
     }
 
 
     public void printProjectDataModelOracle(Path projectPath) {
 
         if (dataModelService != null) {
             //DataModelOracle dataModelOracle = dataModelService.getDataModel(projectService.resolveProject(projectPath));
             ProjectDataModelOracle dataModelOracle = dataModelService.getProjectDataModel(projectPath);
 
             if (dataModelOracle != null) {
                 String[] factTypes = dataModelOracle.getFactTypes();
                 logger.debug("*********************** Project Fact types");
                 for (int i = 0; factTypes != null && i < factTypes.length; i++) {
                     logger.debug(factTypes[i]);
                 }
 
                 logger.debug("*********************** External Fact types");
                 //String externalFactTypes[] = dataModelOracle..getExternalFactTypes();
                 //or (int i = 0; externalFactTypes != null && i < externalFactTypes.length; i++) {
                     //logger.debug(externalFactTypes[i]);
                 //}
                 /**
                 logger.debug("*********************** All Fact types");
                 String allFactTypes[] = dataModelOracle.getAllFactTypes();
                 for (int i = 0; allFactTypes != null && i < allFactTypes.length; i++) {
                     logger.debug(allFactTypes[i]);
                 } **/
 
                 logger.debug("*********************** Project Fact types structure");
                 Map<String, ModelField[]> fields = dataModelOracle.getModelFields();
                 for (int i = 0; factTypes != null && i < factTypes.length; i++) {
                     logger.debug("******* Fields for: " +  factTypes[i]);
                     ModelField[] factFields = fields.get(factTypes[i]);
                     logger.debug("******* fields size: " + (factFields != null ? factFields.length : 0));
                     for (int j = 0; factFields != null && j < factFields.length; j++) {
                         logger.debug("******** " + factFields[j]);
                     }
                 }
             }
         } else {
             logger.error("dataModelService wasn't Injected");
         }
     }
 
 
 }
