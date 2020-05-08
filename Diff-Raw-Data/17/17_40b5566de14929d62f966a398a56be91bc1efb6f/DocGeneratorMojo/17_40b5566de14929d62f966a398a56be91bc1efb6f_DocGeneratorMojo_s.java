 /**
  *
  *     Copyright (C) norad.fr
  *
  *     Licensed under the Apache License, Version 2.0 (the "License");
  *     you may not use this file except in compliance with the License.
  *     You may obtain a copy of the License at
  *
  *             http://www.apache.org/licenses/LICENSE-2.0
  *
  *     Unless required by applicable law or agreed to in writing, software
  *     distributed under the License is distributed on an "AS IS" BASIS,
  *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *     See the License for the specific language governing permissions and
  *     limitations under the License.
  */
 package fr.norad.jaxrs.doc.plugin;
 
 import java.io.File;
 import java.util.Arrays;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugins.annotations.LifecyclePhase;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 import org.apache.maven.plugins.annotations.ResolutionScope;
 import com.fasterxml.jackson.annotation.JsonInclude.Include;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
 import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
 import com.fasterxml.jackson.databind.type.TypeFactory;
 import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
 import fr.norad.jaxrs.doc.JaxrsDocProcessorFactory;
 import fr.norad.jaxrs.doc.ParserHolder;
 import fr.norad.jaxrs.doc.domain.ProjectDefinition;
 import fr.norad.jaxrs.doc.parserapi.ApiParser;
 import fr.norad.jaxrs.doc.parserapi.ModelParser;
 import fr.norad.jaxrs.doc.parserapi.OperationParser;
 import fr.norad.jaxrs.doc.parserapi.ParameterParser;
 import fr.norad.jaxrs.doc.parserapi.ProjectParser;
 import fr.norad.jaxrs.doc.parserapi.PropertyParser;
 import fr.norad.jaxrs.doc.processor.ApiProcessor;
 import fr.norad.jaxrs.doc.processor.ModelProcessor;
 import fr.norad.jaxrs.doc.processor.OperationProcessor;
 import fr.norad.jaxrs.doc.processor.ParameterProcessor;
 import fr.norad.jaxrs.doc.processor.ProjectProcessor;
 import fr.norad.jaxrs.doc.processor.PropertyProcessor;
 
 @Mojo(name = "generate-doc", defaultPhase = LifecyclePhase.PROCESS_CLASSES,
       configurator = "include-project-dependencies",
       requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
 public class DocGeneratorMojo extends AbstractMojo {
     @Parameter(defaultValue = "${project.build.directory}/jaxrs-doc-${version}.json", property = "outputFile",
                required = false)
     private File outputFile;
     @Parameter(defaultValue = "${project.artifactId}", property = "artifactId", required = false)
     private String name;
     @Parameter(defaultValue = "${project.version}", property = "version", required = false)
     private String version;
     @Parameter(defaultValue = "${packageIncludes}", property = "packageIncludes", required = true)
     private String[] packageIncludes;
     @Parameter(property = "projectParsers", required = false)
     private String[] projectParsers;
     @Parameter(property = "apiParsers", required = false)
     private String[] apiParsers;
     @Parameter(property = "operationParsers", required = false)
     private String[] operationParsers;
     @Parameter(property = "parameterParsers", required = false)
     private String[] parameterParsers;
     @Parameter(property = "modelParsers", required = false)
     private String[] modelParsers;
     @Parameter(property = "propertyParsers", required = false)
     private String[] propertyParsers;
    @Parameter(property = "AdditionalProjectParsers", required = false)
     private String[] AdditionalProjectParsers;
    @Parameter(property = "AdditionalApiParsers", required = false)
     private String[] AdditionalApiParsers;
    @Parameter(property = "AdditionalOperationParsers", required = false)
     private String[] AdditionalOperationParsers;
    @Parameter(property = "AdditionalParameterParsers", required = false)
     private String[] AdditionalParameterParsers;
    @Parameter(property = "AdditionalModelParsers", required = false)
     private String[] AdditionalModelParsers;
    @Parameter(property = "AdditionalPropertyParsers", required = false)
     private String[] AdditionalPropertyParsers;
     private ObjectMapper objectMapper;
 
     public DocGeneratorMojo() {
         objectMapper = new ObjectMapper();
         objectMapper.setSerializationInclusion(Include.NON_NULL);
         objectMapper.setAnnotationIntrospector(new AnnotationIntrospectorPair(new JacksonAnnotationIntrospector(),
                                                                               new JaxbAnnotationIntrospector(
                                                                                       TypeFactory.defaultInstance())));
     }
 
     @Override
     public void execute() throws MojoExecutionException {
         outputFile = new File(outputFile.getAbsolutePath().replace("${version}", version));
         try {
             JaxrsDocProcessorFactory processorFactory = prepareFactory();
             writeDefinitionToFile(processorFactory.getProjectProcessor().process());
         } catch (Exception e) {
             getLog().error(e);
             throw new MojoExecutionException("Jaxrs doc generation fail", e);
         }
     }
 
     private JaxrsDocProcessorFactory prepareFactory() {
         JaxrsDocProcessorFactory factory = new JaxrsDocProcessorFactory(Arrays.asList(packageIncludes),
                                                                         name, version);
         if (projectParsers.length > 0) {
             factory.setProjectProcessor(new ProjectProcessor(factory, new ParserHolder<ProjectParser>(projectParsers)));
         } else if (AdditionalProjectParsers.length > 0) {
             factory.getProjectProcessor().getParsers().addAll(AdditionalProjectParsers);
         }
 
         if (apiParsers.length > 0) {
             factory.setApiProcessor(new ApiProcessor(factory, new ParserHolder<ApiParser>(apiParsers)));
         } else if (AdditionalApiParsers.length > 0) {
             factory.getApiProcessor().getParsers().addAll(AdditionalApiParsers);
         }
 
         if (operationParsers.length > 0) {
             factory.setOperationProcessor(new OperationProcessor(factory,
                                                                  new ParserHolder<OperationParser>(operationParsers)));
         } else if (AdditionalOperationParsers.length > 0) {
             factory.getOperationProcessor().getParsers().addAll(AdditionalOperationParsers);
         }
 
         if (parameterParsers.length > 0) {
             factory.setParameterProcessor(new ParameterProcessor(factory,
                                                                  new ParserHolder<ParameterParser>(parameterParsers)));
         } else if (AdditionalParameterParsers.length > 0) {
             factory.getParameterProcessor().getParsers().addAll(AdditionalParameterParsers);
         }
 
         if (modelParsers.length > 0) {
             factory.setModelProcessor(new ModelProcessor(factory, new ParserHolder<ModelParser>(modelParsers)));
         } else if (AdditionalModelParsers.length > 0) {
             factory.getModelProcessor().getParsers().addAll(AdditionalModelParsers);
         }
 
         if (propertyParsers.length > 0) {
             factory.setPropertyProcessor(
                     new PropertyProcessor(factory, new ParserHolder<PropertyParser>(propertyParsers)));
         } else if (AdditionalPropertyParsers.length > 0) {
             factory.getPropertyProcessor().getParsers().addAll(AdditionalPropertyParsers);
         }
         return factory;
     }
 
     private void writeDefinitionToFile(ProjectDefinition definition) {
         if (!outputFile.getParentFile().exists()) {
             outputFile.getParentFile().mkdirs();
         }
 
         try {
             getLog().info("Writing project definition to file : " + outputFile);
             objectMapper.writer().writeValue(outputFile, definition);
         } catch (Exception e) {
             throw new IllegalStateException("Cannot write project definition to file : " + outputFile, e);
         }
     }
 
 }
