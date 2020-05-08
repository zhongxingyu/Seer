 package pl.project13.protoscala.gen;
 
 import com.google.protobuf.DescriptorProtos;
 import google.protobuf.compiler.Plugin;
 import pl.project13.protoscala.utils.CommentsGenerator;
 import pl.project13.protoscala.utils.ScalaNameMangler;
 import pl.project13.protoscala.utils.SourceStringBuilder;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 import static com.google.common.collect.Lists.newArrayList;
 
 /**
  * Date: 3/27/11
  *
  * @author Konrad Malawski
  */
 public class ScalaProtoBufPluginInJava {
 
   Logger log = Logger.getLogger(getClass().getSimpleName());
 
   private SourceStringBuilder sourceStringBuilder = new SourceStringBuilder(); // todo may get more specialized?
 
   private ScalaNameMangler  nameManglerNameMangler = new ScalaNameMangler();
   private CommentsGenerator commentsGenerator      = new CommentsGenerator();
 
   // code generators
   private MessageFieldGenerator  messageFieldGenerator;
   private RepeatedFieldGenerator repeatedFieldGenerator;
 
   public Plugin.CodeGeneratorResponse handle(Plugin.CodeGeneratorRequest request) {
     Plugin.CodeGeneratorResponse.Builder responseBuilder = Plugin.CodeGeneratorResponse.newBuilder();
 
     try {
       // todo the app's heart and soul
       for (DescriptorProtos.FileDescriptorProto protoFile : request.getProtoFileList()) {
         log.info("handleOneProtoFile: name: " + protoFile.getName() + ", package: " + protoFile.getPackage());
         handleOneProtoFile(responseBuilder, protoFile);
       }
 
     } catch (Exception ex) {
       responseBuilder.setError("An '" + ex.getClass().getSimpleName() + "' exception occurred, could not compile proto file!");
     }
 
     log.info("Done.");
     return responseBuilder.build();
   }
 
   private void handleOneProtoFile(Plugin.CodeGeneratorResponse.Builder responseBuilder, DescriptorProtos.FileDescriptorProtoOrBuilder protoFile) {
     Plugin.CodeGeneratorResponse.File.Builder fileBuilder = Plugin.CodeGeneratorResponse.File.getDefaultInstance().newBuilderForType();
 
     handleInitialComments(protoFile);
 
     handlePackage(protoFile);
 
     handleDependencies(protoFile);
 
     handleClassBody(protoFile);
 
     String fileName = nameManglerNameMangler.escapeFileName(protoFile.getName());
     fileBuilder.setName(fileName);
 
     String sourceCode = sourceStringBuilder.toString();
     fileBuilder.setContent(sourceCode);
 
     responseBuilder.addFile(fileBuilder);
   }
 
   private void handleInitialComments(DescriptorProtos.FileDescriptorProtoOrBuilder protoFile) {
     commentsGenerator.initialComment(sourceStringBuilder, protoFile);
   }
 
   private void handlePackage(DescriptorProtos.FileDescriptorProtoOrBuilder protoFile) {
     String javaPackage = protoFile.getOptions().getJavaPackage();
     sourceStringBuilder.declarePackage(javaPackage);
   }
 
   private void handleDependencies(DescriptorProtos.FileDescriptorProtoOrBuilder protoFile) {
     // todo that's most probably wrong ;-)
     for (String dependency : protoFile.getDependencyList()) {
       log.info("Add dependency + " + dependency);
       sourceStringBuilder.importThe(dependency);
     }
   }
 
   /*
    * todo this will have a better architecture (a waaaay batter one, I'm just looking at what we have to code against :-))
    */
   private void handleClassBody(DescriptorProtos.FileDescriptorProtoOrBuilder protoFile) {
     //todo fix this, inner loops suck
 
     for (DescriptorProtos.DescriptorProto descriptorProto : protoFile.getMessageTypeList()) {
 
       // declare class declaration
       handleClassDeclaration(descriptorProto, protoFile);
 
       // handle enum types
       for (DescriptorProtos.EnumDescriptorProto enumDescriptor : descriptorProto.getEnumTypeList()) {
         handleEnumType(enumDescriptor, protoFile);
       }
 
       // handle nested types
       // todo this would be generated inner classes etc, hm...
       for (DescriptorProtos.DescriptorProto nestedType : descriptorProto.getNestedTypeList()) {
         handleNestedType(sourceStringBuilder, nestedType); // for example like this
       }
 
       // handle extensions
       // todo maybe later on
       for (DescriptorProtos.FieldDescriptorProto fieldDescriptorProto : descriptorProto.getExtensionList()) {
         handleExtension(fieldDescriptorProto, protoFile);
       }
 
       // handle extension ranges
       // todo maybe later on
       for (DescriptorProtos.DescriptorProto.ExtensionRange extensionRange : descriptorProto.getExtensionRangeList()) {
         handleExtensionRange(extensionRange, protoFile); //todo not sure what "extension range" is for now
       }
     }
   }
 
   private void handleClassDeclaration(DescriptorProtos.DescriptorProto descriptorProto, DescriptorProtos.FileDescriptorProtoOrBuilder protoFile) {
     String className = descriptorProto.getName();
     log.info("Generating class: " + className);
 
     // handle all fields
     List<String> params = handleFields(descriptorProto.getFieldList());
 
     sourceStringBuilder.declareCaseClass(className, params);
   }
 
   private void handleNestedType(SourceStringBuilder sourceStringBuilder, DescriptorProtos.DescriptorProto nestedType) {
     // todo handle this with THIS class? Rename it to Type generator maybe etc?
   }
 
   private void handleExtensionRange(DescriptorProtos.DescriptorProto.ExtensionRange extensionRange, DescriptorProtos.FileDescriptorProtoOrBuilder protoFile) {
     //todo handle me (use *FieldGenerator)
   }
 
   private void handleExtension(DescriptorProtos.FieldDescriptorProto field, DescriptorProtos.FileDescriptorProtoOrBuilder protoFile) {
     //todo handle me (use *FieldGenerator)
   }
 
  private void handleEnumType(DescriptorProtos.EnumDescriptorProto enumDesc, DescriptorProtos.FileDescriptorProtoOrBuilder protoFile) {
     //todo handle me (use *FieldGenerator)
   }
 
 
   /**
    * Returns a list of field definitions ready to be joined and inserted into the case class definition
    *
    * @param fieldList a list of all fields to be prepared
    * @return a list containing prepared definitions, such as: "name: Type" or "name: Type = defaultVal"
    */
   // todo this will look good rewritten in scala :d
   private List<String> handleFields(List<DescriptorProtos.FieldDescriptorProto> fieldList) {
     List<String> params = newArrayList();
     for (DescriptorProtos.FieldDescriptorProto field : fieldList) {
       String fieldName = field.getName();
       String typeName = field.getTypeName();
 
       String parameter;
       if (field.hasDefaultValue()) {
         String defaultValue = field.getDefaultValue();
         parameter = parameterDefinition(fieldName, typeName, defaultValue);
       } else {
         parameter = parameterDefinition(fieldName, typeName);
       }
 
       params.add(parameter);
     }
 
     return params;
   }
 
   // todo externalize
   private String parameterDefinition(String name, String type, String defaultValue) {
     String parameter = parameterDefinition(name, type);
     return String.format("%s = %s", parameter, defaultValue);
   }
 
   // todo externalize, and have fun with currying
   private String parameterDefinition(String name, String type) {
     return String.format("%s: %s", name, type);
   }
 }
