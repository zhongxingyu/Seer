 package org.ybiquitous.messages.generator;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.nio.charset.Charset;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.TimeZone;
 import java.util.regex.Pattern;
 
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.log.NullLogChute;
 import org.ybiquitous.messages.Constants;
 import org.ybiquitous.messages.Utils;
 
 public final class MessageKeyGenerator {
 
     public static final class Parameter {
 
         public String packageName;
         public String className;
         public String description;
         public File messageResourceFile;
         public Charset messageResourceFileCharset;
         public File templateFile;
         public Charset templateFileCharset;
         public File outputDirectory;
         public Charset outputCharset;
         public List<Class<?>> importClasses;
 
         public void initialize() {
             if (this.packageName == null) {
                 this.packageName = "message";
             }
             if (this.className == null) {
                 this.className = "MessageKeys";
             }
             if (this.description == null) {
                 this.description = "This is an auto generated class.";
             }
             if (this.messageResourceFile == null) {
                 this.messageResourceFile = Utils.getResourceFile(getClass(),
                         '/' + Constants.DEFAULT_MESSAGE_RESOURCE);
             }
             if (this.messageResourceFileCharset == null) {
                 this.messageResourceFileCharset = Constants.DEFAULT_CHARSET;
             }
             if (this.templateFile == null) {
                 this.templateFile = Utils.getResourceFile(getClass(),
                         "MessageKeys.vm");
             }
             if (this.templateFileCharset == null) {
                 this.templateFileCharset = Constants.DEFAULT_CHARSET;
             }
             if (this.outputDirectory == null) {
                 this.outputDirectory = new File(".");
             }
             if (this.outputCharset == null) {
                 this.outputCharset = Constants.DEFAULT_CHARSET;
             }
             if (this.importClasses == null) {
                 this.importClasses = new ArrayList<Class<?>>();
             }
         }
 
         public void validate() throws IllegalArgumentException {
             final StringBuilder error = new StringBuilder();
             final String separator = "\n";
 
             String tempError = validateFile(this.messageResourceFile);
             if (tempError != null) {
                 error.append(separator);
                 error.append(tempError);
             }
 
             tempError = validateFile(this.templateFile);
             if (tempError != null) {
                 error.append(separator);
                 error.append(tempError);
             }
 
             if (error.length() > 0) {
                 throw new IllegalArgumentException(error.toString());
             }
         }
 
         private static String validateFile(File file) {
             if (file != null && !file.isFile()) {
                 return file + " not found";
             }
             return null;
         }
     }
 
     public static File generate(Parameter parameter) {
         Utils.notNull(parameter, "parameter");
         parameter.initialize();
         parameter.validate();
 
         try {
             final Properties messageResource = loadMessageResource(parameter);
             return renderTemplate(parameter, messageResource);
        } catch (Exception e) {
             throw new IllegalStateException(e);
         }
     }
 
     private static Properties loadMessageResource(Parameter parameter)
             throws IOException {
         final InputStreamReader reader = new InputStreamReader(
                 new FileInputStream(parameter.messageResourceFile),
                 parameter.messageResourceFileCharset);
         try {
             final Properties result = new Properties();
             result.load(reader);
             return result;
         } finally {
             reader.close();
         }
     }
 
     private static File buildJavaFile(Parameter parameter) {
         final File dir;
         if (parameter.packageName == null) {
             dir = parameter.outputDirectory;
         } else {
             dir = new File(parameter.outputDirectory,
                     parameter.packageName.replace('.', File.separatorChar));
         }
         if (!dir.exists()) {
             if (!dir.mkdirs()) {
                 throw new IllegalStateException("failed mkdir " + dir);
             }
         }
         return new File(dir, parameter.className + ".java");
     }
 
     private static File renderTemplate(Parameter parameter,
            Properties messageResource) throws Exception {
 
        final Properties props = new Properties();
        props.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH,
                 parameter.templateFile.getParent());
        props.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS,
                NullLogChute.class.getName());

        final VelocityEngine engine = new VelocityEngine();
        engine.init(props);
 
         final Template template = engine.getTemplate(
                 parameter.templateFile.getName(),
                 parameter.templateFileCharset.name());
 
         final File outputFile = buildJavaFile(parameter);
         final BufferedWriter writer = new BufferedWriter(
                 new OutputStreamWriter(new FileOutputStream(outputFile),
                         parameter.outputCharset));
         try {
             template.merge(buildContext(parameter, messageResource), writer);
         } finally {
             writer.close();
         }
         return outputFile;
     }
 
     private static VelocityContext buildContext(Parameter parameter,
             Properties messageResource) {
         final VelocityContext context = new VelocityContext();
         context.put("packageName", parameter.packageName);
         context.put("className", parameter.className);
         context.put("description", parameter.description);
         context.put("generator", MessageKeyGenerator.class.getCanonicalName());
         context.put("timestamp", getTimestamp());
         context.put("messageKeys", buildMessageKeys(messageResource));
         context.put("importClasses", parameter.importClasses);
         context.put(
                 "messageResource",
                 parameter.messageResourceFile.getName().replace(
                         '.' + Constants.DEFAULT_MESSAGE_RESOURCE_EXTENSION, ""));
         return context;
     }
 
     private static String getTimestamp() {
         Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
         sdf.setTimeZone(cal.getTimeZone());
         return sdf.format(cal.getTime());
     }
 
     private static final Pattern REPLACE_PATTERN = Pattern
             .compile("[.\\-\\[\\]]");
     private static final String REPLACE_STRING = "_";
 
     private static List<Map<String, Object>> buildMessageKeys(
             Properties messageResource) {
 
         final List<String> keys = new ArrayList<String>(
                 messageResource.stringPropertyNames());
         Collections.sort(keys);
 
         final List<Map<String, Object>> result = new ArrayList<Map<String, Object>>(
                 messageResource.size());
         for (String key : keys) {
             final Map<String, Object> keyInfo = new HashMap<String, Object>(2);
             keyInfo.put("real", key);
             keyInfo.put("identifier",
                     REPLACE_PATTERN.matcher(key).replaceAll(REPLACE_STRING));
             result.add(keyInfo);
         }
         return result;
     }
 }
