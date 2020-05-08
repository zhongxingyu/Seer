 package de.loggi.service.impl;
 
 import de.loggi.exceptions.ConfigurationException;
 import de.loggi.model.Column;
 import de.loggi.model.Template;
 import de.loggi.processors.ColumnProcessor;
 import de.loggi.service.ConfigurationService;
 import de.loggi.util.FileUtils;
 import org.apache.commons.cli.CommandLine;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.nio.file.*;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.List;
 
 public class ConfigurationServiceImpl implements ConfigurationService {
 
     static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);
     private ObjectMapper mapper = new ObjectMapper();
     private Template template;
     private List<ColumnProcessor> processors;
     private List<Path> sourceFiles;
     private CommandLine commandLine;
 
 
     @Override
     public void initialize(String templateLocation) throws ConfigurationException {
         if (templateLocation == null || templateLocation.isEmpty()) {
             throw new ConfigurationException("Template file path is required");
         }
         Path templatePath = FileSystems.getDefault().getPath(templateLocation);
 
         if (!templatePath.toFile().exists()) {
             throw new ConfigurationException("Cannot find template file: " + templatePath.toString());
         }
         try {
             template = mapper.readValue(templatePath.toFile(), Template.class);
             processors = new ArrayList<>();
 
             for (Column column : template.getColumns()) {
                 initializeColumn(column);
             }
 
         } catch (Throwable ex) {
             logger.error("Exception reading configuration from template", ex);
             throw new ConfigurationException("Exception reading configuration from template [" + templatePath.toString() + "]", ex);
         }
     }
 
     private void initializeColumn(Column column) {
         try {
             Class processorClass = Class.forName(column.getProcessorName());
             ColumnProcessor processor = (ColumnProcessor) processorClass.getConstructor(Column.class).newInstance(column);
             processors.add(processor);
 
         } catch (Exception e) {
             logger.error("Exception instantiating column, name:" + column.getName() + " processor:" + column.getProcessorName() + ". Skipped.", e);
         }
     }
 
     @Override
     public List<ColumnProcessor> getProcessors() {
         return processors;
     }
 
     @Override
     public Template getTemplate() {
         return template;
     }
 
     @Override
     public CommandLine getCommandLine() {
         return commandLine;
     }
 
     @Override
     public void setCommandLine(CommandLine commandLine) {
         this.commandLine = commandLine;
     }
 
     @Override
     public List<Path> getSources() {
         if (sourceFiles == null) {
             sourceFiles = new ArrayList<>();
         }
         return sourceFiles;
     }
 
     @Override
     public void setSource(String source) throws ConfigurationException {
         if (source == null || source.isEmpty()) {
             throw new ConfigurationException("Source file path is null or empty");
         }
 
         // remove wrapping quotes preventing java from expanding arguments
 
         /*
         case1 - simple - some-file.ext
         */
 
        source = source.replace("'","").replace(";","");
        logger.debug("source:{}",source);
         final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/" + source);
         Path userDir = FileSystems.getDefault().getPath(System.getProperty("user.dir"));
         Path searchPath = FileUtils.getAbsolutePathFromPath(source, userDir);
        logger.debug("path:{}", searchPath);
         try {
             Files.walkFileTree(searchPath, EnumSet.of(FileVisitOption.FOLLOW_LINKS), 1, new FileVisitor<Path>() {
                 @Override
                 public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                     return FileVisitResult.CONTINUE;
                 }
 
                 @Override
                 public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                     if(matcher.matches(file)){
                         getSources().add(file);
                     }
                     return FileVisitResult.CONTINUE;
                 }
 
                 @Override
                 public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                     logger.warn("Cannot access file: " + file + ". Skipped.", exc);
                     return FileVisitResult.CONTINUE;
                 }
 
                 @Override
                 public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                     return FileVisitResult.CONTINUE;
                 }
             });
         } catch (IOException e) {
             throw new ConfigurationException("Source files lookup exception", e);
         }
     }
 
     @Override
     public String currentConfigToString() throws ConfigurationException {
         if (template == null) {
             return "null";
         }
         try {
             return mapper.writeValueAsString(template);
         } catch (Exception ex) {
             logger.error("Exception writing configuration template to string", ex);
             return "null";
         }
     }
 }
