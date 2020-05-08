 package fi.vincit.jmobster;
 /*
  * Copyright 2012 Juha Siponen
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
 
 import fi.vincit.jmobster.processor.FieldScanMode;
 import fi.vincit.jmobster.processor.FieldValueConverter;
 import fi.vincit.jmobster.processor.ModelFactory;
 import fi.vincit.jmobster.processor.defaults.validator.JSR303ValidatorFactory;
 import fi.vincit.jmobster.processor.frameworks.backbone.BackboneModelProcessor;
 import fi.vincit.jmobster.processor.frameworks.backbone.DefaultValueProcessor;
 import fi.vincit.jmobster.processor.frameworks.backbone.ValidatorProcessor;
 import fi.vincit.jmobster.processor.languages.javascript.JavaToJSValueConverter;
 import fi.vincit.jmobster.processor.languages.javascript.valueconverters.ConverterMode;
 import fi.vincit.jmobster.processor.languages.javascript.valueconverters.EnumConverter;
 import fi.vincit.jmobster.processor.languages.javascript.writer.JavaScriptWriter;
 import fi.vincit.jmobster.processor.languages.javascript.writer.OutputMode;
 import fi.vincit.jmobster.processor.model.Model;
 import fi.vincit.jmobster.util.groups.GroupMode;
 import fi.vincit.jmobster.util.writer.CachedModelProvider;
 import fi.vincit.jmobster.util.writer.DataWriter;
 import fi.vincit.jmobster.util.writer.StringBufferWriter;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 @SuppressWarnings( "ALL" )
 public class TestMain {
 
     public static void main(String[] args) throws IOException, InterruptedException {
         // Analyze models
         ModelFactory factory = JMobsterFactory.getModelFactoryBuilder()
                 .setFieldScanMode( FieldScanMode.DIRECT_FIELD_ACCESS )
                 .setFieldGroups( GroupMode.ANY_OF_REQUIRED )
                 .setValidatorGroups( GroupMode.ANY_OF_REQUIRED, String.class, Integer.class )
                 .setValidatorFactory( new JSR303ValidatorFactory() )
                 .build();
 
         System.out.println("Generate test classes");
        final int n = 1;
         Collection<Class> classesToConvert = new ArrayList<Class>(n);
         for( int i = 0; i < n; ++i ) {
             if( i % 2 == 0 ) {
                 classesToConvert.add(DemoClasses.BeanPropertyDemo.class);
             } else {
                 classesToConvert.add(DemoClasses.MyModelDto.class);
             }
         }
         System.out.print("Press any key to start covert classes");
         //System.in.read();
         System.out.println("Convert classes");
         Collection<Model> models = factory.createAll(
                 classesToConvert
         );
 
         // Setup writers
         DataWriter modelWriter = new StringBufferWriter();
         CachedModelProvider provider1 = new CachedModelProvider(
                 CachedModelProvider.WriteMode.PRETTY,
                 modelWriter
         );
         JavaScriptWriter javaScriptWriter = new JavaScriptWriter(provider1.getDataWriter());
 
         // Setup generator
         FieldValueConverter converter = new JavaToJSValueConverter(
                 ConverterMode.NULL_AS_DEFAULT,
                 EnumConverter.EnumMode.STRING,
                 JavaToJSValueConverter.ISO_8601_DATE_TIME_TZ_PATTERN
         );
 
         BackboneModelProcessor backboneModelProcessor =
                 new BackboneModelProcessor
                     .Builder(javaScriptWriter, OutputMode.JSON)
                     .setValueConverter(converter)
                     .setModelProcessors(
                             new DefaultValueProcessor.Builder().build(),
                             new ValidatorProcessor.Builder().build()
                     )
                     .build();
         ModelGenerator generator = JMobsterFactory.getModelGenerator( backboneModelProcessor );
 
         System.out.print("Press any key to start generating models");
         //System.in.read();
         System.out.println("Generate models");
         // Generate models
         generator.processAll( models );
 
         System.out.println(modelWriter.toString());
         System.out.println(" - Done");
     }
 }
