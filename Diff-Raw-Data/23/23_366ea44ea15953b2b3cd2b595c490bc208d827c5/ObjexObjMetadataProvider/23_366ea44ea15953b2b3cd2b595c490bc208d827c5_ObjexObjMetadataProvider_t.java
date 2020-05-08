 package org.tpspencer.tal.objexj.roo;
 
 import org.springframework.roo.addon.tostring.ToStringMetadataProvider;
 import org.springframework.roo.classpath.PhysicalTypeIdentifier;
 import org.springframework.roo.classpath.PhysicalTypeMetadata;
 import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
 import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
 import org.springframework.roo.metadata.MetadataDependencyRegistry;
 import org.springframework.roo.metadata.MetadataService;
 import org.springframework.roo.model.JavaType;
 import org.springframework.roo.process.manager.FileManager;
 import org.springframework.roo.project.Path;
 import org.springframework.roo.support.lifecycle.ScopeDevelopment;
 import org.springframework.roo.support.util.Assert;
 import org.tpspencer.tal.objexj.annotations.ObjexObj;
 
 /**
  * This class links an class with the {@link ObjexObj} annotation
  * to a {@link ObjexObjMetadata} instance. 
  * 
  * @author Tom Spencer
  */
 @ScopeDevelopment
 public class ObjexObjMetadataProvider extends AbstractItdMetadataProvider {
     
     public ObjexObjMetadataProvider(MetadataService metadataService, MetadataDependencyRegistry metadataDependencyRegistry, FileManager fileManager, ToStringMetadataProvider toStringProvider) {
         super(metadataService, metadataDependencyRegistry, fileManager);
         addMetadataTrigger(new JavaType(ObjexObj.class.getName()));
         toStringProvider.addMetadataTrigger(new JavaType(ObjexObj.class.getName()));
     }
 
     @Override
     protected ItdTypeDetailsProvidingMetadataItem getMetadata(
             String metadataIdentificationString, JavaType aspectName,
             PhysicalTypeMetadata governorPhysicalTypeMetadata,
             String itdFilename) {
         
         ObjexObjAnnotationValues values = new ObjexObjAnnotationValues(governorPhysicalTypeMetadata);
         Assert.notNull(values.getValue(), "Error, type does not appear to have ObjexObj annotation or a ObjexObj StateBean value: " + governorPhysicalTypeMetadata);
         
         String stateMetadataKey = ObjexStateBeanMetadata.createIdentifier(values.getValue(), Path.SRC_MAIN_JAVA);
         ObjexStateBeanMetadata stateMetadata = (ObjexStateBeanMetadata)metadataService.get(stateMetadataKey);
        Assert.notNull(stateMetadata, "Error, cannot find the state bean metadata for the ObjexObj state bean class: " + values.getValue() + " [" + stateMetadataKey + "]");
         
         return new ObjexObjMetadata(metadataIdentificationString, aspectName, governorPhysicalTypeMetadata, values, stateMetadata.getProperties());
     }
     
     public String getItdUniquenessFilenameSuffix() {
         return "ObjexObj";
     }
     
     public String getProvidesType() {
         return ObjexObjMetadata.getMetadataIdentiferType();
     }
     
     @Override
     protected String getGovernorPhysicalTypeIdentifier(String metadataIdentificationString) {
         JavaType javaType = ObjexObjMetadata.getJavaType(metadataIdentificationString);
         Path path = ObjexObjMetadata.getPath(metadataIdentificationString);
         String physicalTypeIdentifier = PhysicalTypeIdentifier.createIdentifier(javaType, path);
         return physicalTypeIdentifier;
     }
     
     @Override
     protected String createLocalIdentifier(JavaType javaType, Path path) {
         return ObjexObjMetadata.createIdentifier(javaType, path);
     }
 }
