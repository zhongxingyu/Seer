 /*
  * Copyright 2012 Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package fr.jamgotchian.jabat.job;
 
 import fr.jamgotchian.jabat.util.JabatException;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 import org.antlr.runtime.ANTLRInputStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 
 /**
  *
  * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  */
 public class JobUtil {
 
     private JobUtil() {
     }
 
     static interface JobProperties {
 
         String getProperty(String name);
 
     }
 
     private static class JobPropertiesImpl implements JobProperties {
 
         /* current node */
         private final Node node;
 
         /* job parameters */
         private final Properties parameters;
 
         JobPropertiesImpl(Node node, Properties parameters) {
             this.node = node;
             this.parameters = parameters;
         }
 
         private String getProperty(Node node, String name) {
             String value = node.getProperties().getProperty(name);
             if (value != null) {
                return JobUtil.substitute(value, parameters, new JobPropertiesImpl(node, parameters), name);
             } else {
                 if (node.getContainer() != null) {
                     return getProperty(node.getContainer(), name);
                 }
                 return null;
             }
         }
 
         @Override
         public String getProperty(String name) {
             if (node.getContainer() != null) {
                 return getProperty(node.getContainer(), name);
             }
             return null;
         }
 
     }
 
     public static void substitute(Node node, Properties jobParameters) {
         substitute(node, jobParameters, node);
         for (Artifact artifact : node.getArtifacts()) {
             substitute(artifact, jobParameters, node);
         }
     }
 
     /* for test only */
     public static String substitute(String value, Properties jobParameters, final Properties jobProperties) {
         return substitute(value, jobParameters, new JobProperties() {
             @Override
             public String getProperty(String name) {
                 return jobProperties.getProperty(name);
             }
        }, null);
     }
 
     public static Properties substitute(final Properties properties, Properties jobParameters, Node node) {
         final Properties substitutedProperties = new Properties();
         substitute(new Propertiable() {
             @Override
             public Properties getProperties() {
                 return properties;
             }
 
             @Override
             public Properties getSubstitutedProperties() {
                 return substitutedProperties;
             }
         }, jobParameters, node);
         return substitutedProperties;
     }
 
     private static void substitute(Propertiable propertiable, Properties jobParameters, Node node) {
         JobProperties jobProperties = new JobPropertiesImpl(node, jobParameters);
         propertiable.getSubstitutedProperties().clear();
         for (String name : propertiable.getProperties().stringPropertyNames()) {
             String value = propertiable.getProperties().getProperty(name);
            String substitutedValue = substitute(value, jobParameters, jobProperties, name);
             propertiable.getSubstitutedProperties().setProperty(name, substitutedValue);
         }
     }
 
    private static String substitute(String value, Properties jobParameters, JobProperties jobProperties, String propertyName) {
         String result = null;
         try {
             InputStream is = new ByteArrayInputStream(value.getBytes("UTF-8"));
             try {
                 JobXmlSubstitutionLexer lexer = new JobXmlSubstitutionLexer(new ANTLRInputStream(is, "UTF-8"));
                 CommonTokenStream tokens = new CommonTokenStream(lexer);
                 JobXmlSubstitutionParser parser = new JobXmlSubstitutionParser(tokens);
                parser.propertyName = propertyName;
                 parser.jobParameters = jobParameters;
                 parser.jobProperties = jobProperties;
                 result = parser.attributeValue();
             } finally {
                 is.close();
             }
         } catch (IOException e) {
             throw new JabatException(e);
         } catch (RecognitionException e) {
             throw new JabatException(e);
         }
         return result;
     }
 }
