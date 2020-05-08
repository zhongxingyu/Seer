 /*
  * Copyright 2011 JBoss Inc
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.drools.semantics.lang.dl;
 
 import org.drools.KnowledgeBase;
 import org.drools.KnowledgeBaseFactory;
 import org.drools.io.Resource;
 import org.drools.io.ResourceFactory;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.drools.semantics.builder.DLFactory;
 import org.drools.semantics.builder.DLFactoryBuilder;
 import org.drools.semantics.builder.model.*;
 import org.drools.semantics.builder.model.compilers.ModelCompiler;
 import org.drools.semantics.builder.model.compilers.ModelCompilerFactory;
 import org.drools.semantics.util.SemanticWorkingSetConfigData;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import static org.junit.Assert.fail;
 
 
 /**
  * This is a sample class to launch a rule.
  */
 @SuppressWarnings("restriction")
 public class DL_99_KMRModelTest {
 
 
 
     protected DLFactory factory = DLFactoryBuilder.newDLFactoryInstance();
     
     @Rule
     public TemporaryFolder folder = new TemporaryFolder();
 
 
     @Test
     public void testDRLModelGeneration() {
         String source = "kmr2/kmr2_miniExample.manchester";
         Resource res = ResourceFactory.newClassPathResource( source );
         KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
         StatefulKnowledgeSession kSession = kbase.newStatefulKnowledgeSession();
 
         OntoModel results = factory.buildModel( res, kSession );
 
         ModelCompiler compiler = ModelCompilerFactory.newModelCompiler( ModelFactory.CompileTarget.DRL );
         DRLModel drlModel = (DRLModel) compiler.compile( results );
 
         System.err.println( drlModel.getDRL() );
 
 
         ModelCompiler jcompiler = ModelCompilerFactory.newModelCompiler( ModelFactory.CompileTarget.JAR );
         JarModel jarModel = (JarModel) jcompiler.compile( results );
 
         System.err.println( jarModel.save( "gen-sources" ) );
 
         try {
             FileOutputStream fos = new FileOutputStream("gen-sources/test.jar");
             byte[] content = jarModel.buildJar().toByteArray();
 
             fos.write( content, 0, content.length );
             fos.flush();
             fos.close();
         } catch ( IOException e ) {
             fail( e.getMessage() );
         }
 
 
 
         System.err.println( results );
 
     }
 
 
 
     @Test
     public void testGraphModelGeneration() {
 //        String source = "org/drools/semantics/lang/dl/kmr2_miniExample.manchester";
          String source = "kmr2/Kmr2.ttl";
         Resource res = ResourceFactory.newClassPathResource( source );
         KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
         StatefulKnowledgeSession kSession = kbase.newStatefulKnowledgeSession();
 
         factory.setInferenceStrategy( DLFactory.INFERENCE_STRATEGY.EXTERNAL );
         OntoModel results = factory.buildModel( res, kSession );
 
         ModelCompiler compiler = ModelCompilerFactory.newModelCompiler( ModelFactory.CompileTarget.GRAPH );
         GraphModel gModel = (GraphModel) compiler.compile( results );
 
         gModel.display();
 
         System.err.println( gModel );
 
 
         try {
             while (true) {
                 Thread.sleep(10000);
             }
         } catch (InterruptedException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
     }
 
 
 
 
 
     @Test
     public void testXSDModelGeneration() {
         String source = "kmr2/kmr2_miniExample.manchester";
         Resource res = ResourceFactory.newClassPathResource( source );
         KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
         StatefulKnowledgeSession kSession = kbase.newStatefulKnowledgeSession();
 
         factory.setInferenceStrategy( DLFactory.INFERENCE_STRATEGY.EXTERNAL );
 
         OntoModel results = factory.buildModel( res, kSession );
 
 
         ModelCompiler compiler = ModelCompilerFactory.newModelCompiler( ModelFactory.CompileTarget.XSD );
         XSDModel xsdModel = (XSDModel) compiler.compile( results );
 
         xsdModel.stream( System.out );
 
     }
 
 
 
     @Test
     public void testXSDExternalModelGeneration() {
         String source = "kmr2/kmr2_miniExample.manchester";
         Resource res = ResourceFactory.newClassPathResource(source);
 
         OntoModel results = factory.buildModel( res );
 
 
         ModelCompiler compiler = ModelCompilerFactory.newModelCompiler( ModelFactory.CompileTarget.XSD );
         XSDModel xsdModel = (XSDModel) compiler.compile( results );
 
         xsdModel.stream( System.out );
 
     }
 
 
 
 
 
     @Test
     public void testWorkingSetModelGeneration() {
         String source = "kmr2/kmr2_miniExample.manchester";
         Resource res = ResourceFactory.newClassPathResource( source );
 
         OntoModel results = factory.buildModel( res );
 
 
         ModelCompiler compiler = ModelCompilerFactory.newModelCompiler( ModelFactory.CompileTarget.WORKSET );
         WorkingSetModel wsModel = (WorkingSetModel) compiler.compile( results );
 
         SemanticWorkingSetConfigData ws = wsModel.getWorkingSet();
 
         System.out.println(ws);
 
     }
 
 
     @Test
     public void testFullXSDModelGeneration() {
         String source = "kmr2/Kmr2.ttl";
         Resource res = ResourceFactory.newClassPathResource( source );
         KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
         StatefulKnowledgeSession kSession = kbase.newStatefulKnowledgeSession();
 
         factory.setInferenceStrategy( DLFactory.INFERENCE_STRATEGY.EXTERNAL );
 
         OntoModel results = factory.buildModel( res, kSession );
 
 
         ModelCompiler jcompiler =  ModelCompilerFactory.newModelCompiler( ModelFactory.CompileTarget.JAR );
         JarModel jarModel = (JarModel) jcompiler.compile( results );
 
         try {
             FileOutputStream fos = new FileOutputStream("/home/davide/Projects/KMR2/workspace/Factz/lib/kmr2.jar");
             byte[] content = jarModel.buildJar().toByteArray();
 
             fos.write( content, 0, content.length );
             fos.flush();
             fos.close();
         } catch ( IOException e ) {
             fail( e.getMessage() );
         }
 
 
         /**************************************************************************************************************/
 
 
         ModelCompiler compiler = ModelCompilerFactory.newModelCompiler( ModelFactory.CompileTarget.XSDX );
         SemanticXSDModel xsdModel = (SemanticXSDModel) compiler.compile( results );
 
 //        xsdModel.stream( System.out );
 //        xsdModel.streamBindings( System.out );
 
 
 
         try {
             FileOutputStream fos = new FileOutputStream("/home/davide/Projects/KMR2/workspace/Factz/src/main/resources/kmr2.xsd");
             xsdModel.stream( fos );
             fos.flush();
             fos.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
 
 
         try {
             FileOutputStream fos = new FileOutputStream("/home/davide/Projects/KMR2/workspace/Factz/src/main/resources/bindings.xjb");
             xsdModel.streamBindings( fos );
             fos.flush();
             fos.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
 
     }
 
 
 
 //    @Test
 //    public void testJAVAModelGeneration() {
 //        String source = "org/drools/semantics/lang/dl/kmr2.manchester";
 //        Resource res = ResourceFactory.newClassPathResource( source );
 //        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
 //        StatefulKnowledgeSession kSession = kbase.newStatefulKnowledgeSession();
 //
 //        OntoModel results = factory.buildModel( res, kSession );
 //
 //        ModelCompiler compiler = ModelCompilerFactory.newModelCompiler( ModelFactory.CompileTarget.JAVA );
 //        JavaModel javaModel = (JavaModel) compiler.compile( results );
 //
 //        System.err.println( javaModel.save( "gen-sources" ) );
 //
 //    }
 //
 //
 //
 //
 //    @Test
 //    public void testJarModelGeneration() {
 //        String source = "org/drools/semantics/lang/dl/kmr2.manchester";
 //        Resource res = ResourceFactory.newClassPathResource( source );
 //        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
 //        StatefulKnowledgeSession kSession = kbase.newStatefulKnowledgeSession();
 //
 //        OntoModel results = factory.buildModel( res, kSession );
 //
 //        ModelCompiler compiler = ModelCompilerFactory.newModelCompiler( ModelFactory.CompileTarget.JAR );
 //        JarModel jarModel = (JarModel) compiler.compile( results );
 //
 //        try {
 //            FileOutputStream fos = new FileOutputStream("gen-sources/test.jar");
 //            byte[] content = jarModel.buildJar().toByteArray();
 //
 //            fos.write( content, 0, content.length );
 //            fos.flush();
 //            fos.close();
 //        } catch ( IOException e ) {
 //            fail( e.getMessage() );
 //        }
 //
 //    }
 //
 //
 
 }
