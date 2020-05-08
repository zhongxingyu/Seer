 package com.wotifgroup.swaggerstandalonegenerator;
 
 import com.fasterxml.jackson.core.JsonFactory;
 import com.fasterxml.jackson.core.JsonGenerator;
 import com.sun.mirror.apt.AnnotationProcessorEnvironment;
 import com.sun.mirror.apt.Filer;
import com.wotifgroup.swaggerstandalonegenerator.model.ErrorResponse;
 import com.wotifgroup.swaggerstandalonegenerator.model.Operation;
 import com.wotifgroup.swaggerstandalonegenerator.model.Parameter;
 import com.wotifgroup.swaggerstandalonegenerator.model.ResourcePath;
 import com.wotifgroup.swaggerstandalonegenerator.model.Resource;
 import java.io.File;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.apache.commons.lang3.StringUtils;
 
 /**
  * Writes the json files for the swagger api. flushAndClose() must be called on this class to ensure that the
  * final, root-level json file is written.
  *
  * User: Adam
  * Date: 26/07/12
  * Time: 8:08 PM
  */
 public class SwaggerJsonWriter {
 
     public static final String API_VERSION_OPTION = "apiVersion";
     public static final String BASE_PATH_OPTION = "basePath";
 
     private static final String OUTPUT_PACKAGE = "swagger";
     private static final String OUTPUT_CHARSET = "UTF-8";
     private static final String ROOT_RESOURCES_FILE_NAME = "resources";
     private static final String SWAGGER_VERSION = "1.0";
 
     private AnnotationProcessorEnvironment annotationProcessorEnvironment;
     private Collection<Resource> resources;
     private JsonFactory jsonFactory;
 
     public SwaggerJsonWriter(AnnotationProcessorEnvironment annotationProcessorEnvironment) {
         this.annotationProcessorEnvironment = annotationProcessorEnvironment;
         this.resources = new ArrayList<Resource>();
         this.jsonFactory = new JsonFactory();
     }
 
     public void flushAndClose() {
         JsonGenerator writer = null;
         try {
             writer = getJsonGenerator(getOutputWriter(ROOT_RESOURCES_FILE_NAME));
             writer.writeStartObject();
 
             writeFileHeader(writer);
 
             writer.writeFieldName("apis");
             writer.writeStartArray();
             for (Resource nextResource : this.resources) {
                 writer.writeStartObject();
                 writer.writeStringField("path", nextResource.getPath());
                 writeOptionalField(writer, "description", nextResource.getDescription());
                 writer.writeEndObject();
             }
             writer.writeEndArray();
 
             writer.writeEndObject();
         } catch (IOException e) {
             throw new RuntimeException("Failed to write root resource file", e);
         } finally {
             close(writer);
         }
     }
 
     public void write(Resource resource) {
         this.resources.add(resource);
 
         JsonGenerator writer = null;
         try {
             writer = getJsonGenerator(getOutputWriter(getFileName(resource)));
             writer.writeStartObject();
 
             writeFileHeader(writer);
             writer.writeStringField("resourcePath", resource.getPath());
 
             writer.writeFieldName("apis");
             writer.writeStartArray();
             for (ResourcePath nextResourcePath : resource.getResourcePaths()) {
                 write(writer, nextResourcePath, resource.getDescription());
             }
             writer.writeEndArray();
 
             writer.writeEndObject();
         } catch (IOException e) {
             throw new RuntimeException("Failed to write root resource file", e);
         } finally {
             close(writer);
         }
     }
 
     protected void close(JsonGenerator writer) {
         if (writer != null) {
             try {
                 writer.flush();
                 writer.close();
             } catch (IOException e) {
                 throw new RuntimeException("Failed to close writer", e);
             }
         }
     }
 
     protected String getFileName(Resource resource) {
         String fileName = resource.getPath();
 
         if (fileName.contains("/")) {
             fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
         }
 
         return fileName;
     }
 
     protected JsonGenerator getJsonGenerator(Writer writer) throws IOException {
         JsonGenerator jsonGenerator = this.jsonFactory.createJsonGenerator(writer);
         jsonGenerator.useDefaultPrettyPrinter();
         return jsonGenerator;
     }
 
     protected String getOption(String optionName){
         Pattern optionPattern = Pattern.compile("-A" + optionName + "=(.+)");
         for (Map.Entry<String, String> nextOption : this.annotationProcessorEnvironment.getOptions().entrySet()) {
             Matcher matcher = optionPattern.matcher(nextOption.getKey());
             if (matcher.matches()) {
                 return matcher.group(1);
             }
         }
 
         return null;
     }
 
     protected Writer getOutputWriter(String fileName) {
         try {
             return annotationProcessorEnvironment.getFiler().createTextFile(Filer.Location.SOURCE_TREE,
                                                                             OUTPUT_PACKAGE,
                                                                             new File(fileName + ".json"),
                                                                             OUTPUT_CHARSET);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     protected void write(JsonGenerator writer, ResourcePath resourcePath, String resourceDescription) throws IOException {
         writer.writeStartObject();
         writer.writeStringField("path", resourcePath.getPath());
         writeOptionalField(writer, "description", resourceDescription);
 
         writer.writeFieldName("operations");
         writer.writeStartArray();
         for (Operation nextOperation : resourcePath.getOperations()) {
             write(writer, nextOperation);
         }
         writer.writeEndArray();
 
         writer.writeEndObject();
     }
 
     protected void write(JsonGenerator writer, Operation operation) throws IOException {
         writer.writeStartObject();
         writer.writeStringField("nickname", operation.getNickname());
         writer.writeStringField("httpMethod", operation.getHttpMethod());
         writer.writeStringField("responseClass", operation.getResponseType().getId());
         writeOptionalField(writer, "summary", operation.getSummary());
         writeOptionalField(writer, "notes", operation.getNotes());
 
         if (!operation.getParameters().isEmpty()) {
             writer.writeFieldName("parameters");
             writer.writeStartArray();
             for (Parameter nextParameter : operation.getParameters()) {
                 write(writer, nextParameter);
             }
             writer.writeEndArray();
         }

        if (!operation.getErrorResponses().isEmpty()) {
            writer.writeFieldName("errorResponses");
            writer.writeStartArray();
            for (ErrorResponse nextErrorResponse : operation.getErrorResponses()) {
                write(writer, nextErrorResponse);
            }
            writer.writeEndArray();
        }

         writer.writeEndObject();
     }
 
     protected void write(JsonGenerator writer, Parameter parameter) throws IOException {
         writer.writeStartObject();
         writer.writeStringField("name", parameter.getName());
         writer.writeStringField("description", parameter.getDescription());
         writer.writeStringField("paramType", parameter.getParamType().getSwaggerParamType());
         writer.writeStringField("dataType", parameter.getModelType().getId());
         writer.writeBooleanField("required", parameter.isRequired());
         writer.writeBooleanField("allowMultiple", parameter.isAllowMultiple());
         writer.writeEndObject();
     }
 
    protected void write(JsonGenerator writer, ErrorResponse errorResponse) throws IOException {
        writer.writeStartObject();
        writer.writeNumberField("code", errorResponse.getStatusCode());
        writer.writeStringField("reason", errorResponse.getReason());
        writer.writeEndObject();
    }

     protected void writeFileHeader(JsonGenerator writer) throws IOException {
         writer.writeStringField("swaggerVersion", SWAGGER_VERSION);
         writeOptionalField(writer, "apiVersion", getOption(API_VERSION_OPTION));
         writeOptionalField(writer, "basePath", getOption(BASE_PATH_OPTION));
     }
 
     protected void writeOptionalField(JsonGenerator writer, String name, String value) throws IOException {
         if (!StringUtils.isBlank(value)) {
             writer.writeStringField(name, value);
         }
     }
 }
