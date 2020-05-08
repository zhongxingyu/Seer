 package fr.imag.model2roo.addon.graph;
 
 import static org.springframework.roo.model.JavaType.OBJECT;
 import static org.springframework.roo.model.RooJavaType.ROO_JAVA_BEAN;
 import static org.springframework.roo.model.RooJavaType.ROO_TO_STRING;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.Service;
 import org.springframework.roo.classpath.PhysicalTypeCategory;
 import org.springframework.roo.classpath.PhysicalTypeIdentifier;
 import org.springframework.roo.classpath.TypeLocationService;
 import org.springframework.roo.classpath.TypeManagementService;
 import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
 import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
 import org.springframework.roo.classpath.details.FieldMetadataBuilder;
 import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
 import org.springframework.roo.model.DataType;
 import org.springframework.roo.model.JavaSymbolName;
 import org.springframework.roo.model.JavaType;
 import org.springframework.roo.process.manager.FileManager;
 import org.springframework.roo.project.Dependency;
 import org.springframework.roo.project.Path;
 import org.springframework.roo.project.PathResolver;
 import org.springframework.roo.project.ProjectOperations;
 import org.springframework.roo.support.util.FileUtils;
 import org.springframework.roo.support.util.XmlUtils;
 import org.w3c.dom.Element;
 
 /**
  * 
  * @author jccastrejon
  * 
  */
 @Component
 @Service
 public class GraphOperationsImpl implements GraphOperations {
 
     /**
      * 
      */
     private static final AnnotationMetadataBuilder ROO_JAVA_BEAN_BUILDER = new AnnotationMetadataBuilder(ROO_JAVA_BEAN);
 
     /**
      * 
      */
     private static final AnnotationMetadataBuilder ROO_TO_STRING_BUILDER = new AnnotationMetadataBuilder(ROO_TO_STRING);
 
     /**
      * 
      */
     @Reference
     private FileManager fileManager;
 
     /**
      * 
      */
     @Reference
     private PathResolver pathResolver;
 
     /**
      * Use ProjectOperations to install new dependencies, plugins, properties,
      * etc into the project configuration
      */
     @Reference
     private ProjectOperations projectOperations;
 
     /**
      * 
      */
     @Reference
     TypeManagementService typeManagementService;
 
     /**
      * 
      */
     @Reference
     private TypeLocationService typeLocationService;
 
     /**
      * 
      */
     public boolean isGraphSetupAvailable() {
         return (!fileManager.exists(this.getContextPath()));
     }
 
     /**
      * 
      */
     public void graphSetup(final GraphProvider graphProvider, final String dataStoreLocation) {
         this.addDependencies(graphProvider);
         this.addConfiguration(graphProvider, dataStoreLocation);
     }
 
     /**
      * 
      */
     public boolean isNewEntityAvailable() {
         return (fileManager.exists(this.getContextPath()));
     }
 
     /**
      * 
      */
     public void newEntity(final JavaType name, final JavaType superClass, final boolean isAbstract) {
         int modifier;
         String entityId;
         GraphProvider graphProvider;
         FieldMetadataBuilder fieldBuilder;
         ClassOrInterfaceTypeDetails entityDetails;
         ClassOrInterfaceTypeDetails superClassDetails;
         ClassOrInterfaceTypeDetailsBuilder entityBuilder;
 
         // Class modifier
         modifier = Modifier.PUBLIC;
         if (isAbstract) {
             modifier = Modifier.ABSTRACT;
         }
 
         // Create entity class
         entityId = PhysicalTypeIdentifier.createIdentifier(name, pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));
         entityBuilder = new ClassOrInterfaceTypeDetailsBuilder(entityId, modifier, name, PhysicalTypeCategory.CLASS);
 
         // Base class
         if (!superClass.equals(OBJECT)) {
             superClassDetails = typeLocationService.getTypeDetails(superClass);
             if (superClassDetails != null) {
                 entityBuilder.setSuperclass(new ClassOrInterfaceTypeDetailsBuilder(superClassDetails));
             }
         }
         entityBuilder.setExtendsTypes(Arrays.asList(superClass));
 
         // Associate appropriate annotations
         graphProvider = this.getCurrentGraphProvider();
         entityBuilder.setAnnotations(graphProvider.getClassAnnotations());
         entityBuilder.addAnnotation(ROO_JAVA_BEAN_BUILDER);
         entityBuilder.addAnnotation(ROO_TO_STRING_BUILDER);
         typeManagementService.createOrUpdateTypeOnDisk(entityBuilder.build());
 
         // Add id field
         entityDetails = typeLocationService.getTypeDetails(name);
         fieldBuilder = new FieldMetadataBuilder(entityDetails.getDeclaredByMetadataId(), Modifier.PRIVATE,
                 graphProvider.getIdAnnotations(), new JavaSymbolName("nodeId"), JavaType.LONG_OBJECT);
         typeManagementService.addField(fieldBuilder.build());
     }
 
     /**
      * 
      */
     public boolean isNewRepositoryAvailable() {
         return (fileManager.exists(this.getContextPath()));
     }
 
     /**
      * 
      */
     public void newRepository(final JavaType name, final JavaType domainType) {
         String entityId;
         GraphProvider graphProvider;
         List<JavaType> repositoryParameters;
         ClassOrInterfaceTypeDetailsBuilder entityBuilder;
 
         // Create repository class
         entityId = PhysicalTypeIdentifier.createIdentifier(name, pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));
         entityBuilder = new ClassOrInterfaceTypeDetailsBuilder(entityId, Modifier.PUBLIC, name,
                PhysicalTypeCategory.INTERFACE);
 
         // Add neo4j repository base class
         repositoryParameters = new ArrayList<JavaType>();
         repositoryParameters.add(domainType);
         graphProvider = this.getCurrentGraphProvider();
         for (String baseClass : graphProvider.getRepositoryBaseClasses()) {
             entityBuilder.addExtendsTypes(new JavaType(baseClass, 0, DataType.TYPE, null, repositoryParameters));
         }
 
         // Save repository
         typeManagementService.createOrUpdateTypeOnDisk(entityBuilder.build());
     }
 
     /**
      * 
      * @return
      */
     public boolean isNewRelationshipEntityAvailable() {
         return (fileManager.exists(this.getContextPath()));
     }
 
     /**
      * 
      * @param name
      */
     public void newRelationshipEntity(final JavaType name, final String type, final JavaType startNode,
             final JavaType endNode, List<String> properties) {
         String entityId;
         GraphProvider graphProvider;
         FieldMetadataBuilder fieldBuilder;
         ClassOrInterfaceTypeDetails entityDetails;
         ClassOrInterfaceTypeDetailsBuilder entityBuilder;
         List<AnnotationMetadataBuilder> relationshipAnnotations;
 
         // Create relationship class
         entityId = PhysicalTypeIdentifier.createIdentifier(name, pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));
         entityBuilder = new ClassOrInterfaceTypeDetailsBuilder(entityId, Modifier.PUBLIC, name,
                 PhysicalTypeCategory.CLASS);
 
         // Associate appropriate annotations
         graphProvider = this.getCurrentGraphProvider();
         relationshipAnnotations = graphProvider.getRelationshipEntityAnnotations();
         for (AnnotationMetadataBuilder annotation : relationshipAnnotations) {
             annotation.addStringAttribute("type", type);
         }
         entityBuilder.setAnnotations(relationshipAnnotations);
         entityBuilder.addAnnotation(ROO_JAVA_BEAN_BUILDER);
         entityBuilder.addAnnotation(ROO_TO_STRING_BUILDER);
         typeManagementService.createOrUpdateTypeOnDisk(entityBuilder.build());
 
         // Id
         entityDetails = typeLocationService.getTypeDetails(name);
         fieldBuilder = new FieldMetadataBuilder(entityDetails.getDeclaredByMetadataId(), Modifier.PRIVATE,
                 graphProvider.getIdAnnotations(), new JavaSymbolName("relationshipId"), JavaType.LONG_OBJECT);
         entityBuilder.addField(fieldBuilder.build());
 
         // Start node
         fieldBuilder = new FieldMetadataBuilder(entityDetails.getDeclaredByMetadataId(), Modifier.PRIVATE,
                 graphProvider.getRelationshipStartNodeAnnotations(), new JavaSymbolName("startNode"), startNode);
         entityBuilder.addField(fieldBuilder.build());
 
         // End node
         fieldBuilder = new FieldMetadataBuilder(entityDetails.getDeclaredByMetadataId(), Modifier.PRIVATE,
                 graphProvider.getRelationshipEndNodeAnnotations(), new JavaSymbolName("endNode"), endNode);
         entityBuilder.addField(fieldBuilder.build());
 
         // Properties
         if (properties != null) {
             // One field of type String for each property
             for (String property : properties) {
                 fieldBuilder = new FieldMetadataBuilder(entityDetails.getDeclaredByMetadataId(), Modifier.PRIVATE,
                         new ArrayList<AnnotationMetadataBuilder>(), new JavaSymbolName(property), JavaType.STRING);
                 entityBuilder.addField(fieldBuilder.build());
             }
         }
 
         typeManagementService.createOrUpdateTypeOnDisk(entityBuilder.build());
     }
 
     /**
      * 
      */
     public boolean isNewRelationshipAvailable() {
         return (fileManager.exists(this.getContextPath()));
     }
 
     /**
      * 
      */
     public void newRelationship(final JavaType fromNode, final JavaType relationNode, final boolean isVia,
             final String type, final Direction direction, final String fieldName,
             final RelationshipType relationshipType) {
         List<JavaType> parameters;
         GraphProvider graphProvider;
         FieldMetadataBuilder fieldBuilder;
         ClassOrInterfaceTypeDetails entityDetails;
         List<AnnotationMetadataBuilder> fieldAnnotations;
 
         fieldBuilder = null;
         entityDetails = typeLocationService.getTypeDetails(fromNode);
         switch (relationshipType) {
         case SINGLE:
             fieldBuilder = new FieldMetadataBuilder(entityDetails.getDeclaredByMetadataId(), Modifier.PRIVATE,
                     new ArrayList<AnnotationMetadataBuilder>(), new JavaSymbolName(fieldName), relationNode);
             break;
         case MODIFIABLE:
             parameters = new ArrayList<JavaType>();
             parameters.add(relationNode);
             fieldBuilder = new FieldMetadataBuilder(entityDetails.getDeclaredByMetadataId(), Modifier.PRIVATE,
                     new ArrayList<AnnotationMetadataBuilder>(), new JavaSymbolName(fieldName), new JavaType(
                             "java.util.Set", 0, DataType.TYPE, null, parameters));
             break;
         case READ_ONLY:
             parameters = new ArrayList<JavaType>();
             parameters.add(relationNode);
             fieldBuilder = new FieldMetadataBuilder(entityDetails.getDeclaredByMetadataId(), Modifier.PRIVATE,
                     new ArrayList<AnnotationMetadataBuilder>(), new JavaSymbolName(fieldName), new JavaType(
                             "java.lang.Iterable", 0, DataType.TYPE, null, parameters));
             break;
         }
 
         if (fieldBuilder != null) {
             // Associate appropriate annotations
             graphProvider = this.getCurrentGraphProvider();
             fieldAnnotations = graphProvider.getRelationshipAnnotations();
             if (isVia) {
                 fieldAnnotations = graphProvider.getRelationshipViaAnnotations();
             }
             for (AnnotationMetadataBuilder annotation : fieldAnnotations) {
                 if (type != null) {
                     annotation.addStringAttribute("type", type);
                 }
                 if (direction != null) {
                     annotation.addEnumAttribute("direction", Direction.class.getCanonicalName(), direction.toString());
                 }
                 fieldBuilder.addAnnotation(annotation);
             }
 
             typeManagementService.addField(fieldBuilder.build());
         }
     }
 
     /**
      * 
      * @param provider
      */
     private void addDependencies(final GraphProvider graphProvider) {
         Element configuration;
         List<Dependency> dependencies;
         List<Element> providerDependences;
 
         dependencies = new ArrayList<Dependency>();
         configuration = XmlUtils.getConfiguration(this.getClass());
 
         // Recover the dependencies associated to the specified provider
         providerDependences = XmlUtils.findElements(graphProvider.getConfigPrefix(), configuration);
         for (Element dependency : providerDependences) {
             dependencies.add(new Dependency(dependency));
         }
 
         // Add all new dependencies to pom.xml
         projectOperations.addDependencies("", dependencies);
     }
 
     /**
      * 
      * @param provider
      * @param dataStoreLocation
      */
     private void addConfiguration(final GraphProvider graphProvider, final String dataStoreLocation) {
         String contextPath;
         String outputContents;
         InputStream templateStream;
         OutputStream configurationStream;
 
         // Create a new graph configuration file every time
         contextPath = this.getContextPath();
         if (this.fileManager.exists(contextPath)) {
             this.fileManager.delete(contextPath);
         }
 
         // The new file content is based on the appropriate provider template
         templateStream = null;
         configurationStream = null;
         try {
             // Read template contents and update required variables
             templateStream = FileUtils.getInputStream(this.getClass(), "applicationContext-graph-"
                     + graphProvider.name().toLowerCase() + ".xml");
             outputContents = IOUtils.toString(templateStream);
             outputContents = outputContents.replace("${store.location}", dataStoreLocation);
             outputContents = outputContents.replace("TO_BE_CHANGED_BY_ADDON",
                     projectOperations.getTopLevelPackage(projectOperations.getFocusedModuleName())
                             .getFullyQualifiedPackageName());
             configurationStream = this.fileManager.createFile(contextPath).getOutputStream();
             IOUtils.write(outputContents, configurationStream);
         } catch (IOException e) {
             throw new IllegalStateException(e);
         } finally {
             IOUtils.closeQuietly(templateStream);
             IOUtils.closeQuietly(configurationStream);
         }
     }
 
     /**
      * 
      * @return
      */
     private GraphProvider getCurrentGraphProvider() {
         int providerIndex;
         String graphConfiguration;
         GraphProvider returnValue;
 
         returnValue = GraphProvider.Neo4j;
         try {
             graphConfiguration = IOUtils.toString(new FileInputStream(this.fileManager.readFile(this.getContextPath())
                     .getFile()));
             providerIndex = graphConfiguration.indexOf("GraphProvider:") + "GraphProvider:".length();
             returnValue = GraphProvider.valueOf(graphConfiguration.substring(providerIndex,
                     graphConfiguration.indexOf("-", providerIndex)).trim());
         } catch (IOException e) {
         }
 
         return returnValue;
     }
 
     /**
      * 
      * @return
      */
     private String getContextPath() {
         return pathResolver.getFocusedIdentifier(Path.SPRING_CONFIG_ROOT, "applicationContext-graph.xml");
     }
 }
